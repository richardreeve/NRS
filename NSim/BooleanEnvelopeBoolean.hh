/*
 * Copyright (C) 2005 Richard Reeve and Edinburgh University
 *
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation; either version 2 of
 *    the License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public
 *    License along with this program; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307 USA
 *
 * For further information in the first instance contact:
 * Richard Reeve <richardr@inf.ed.ac.uk>
 *
 */

#ifndef _BOOLEAN_ENVELOPE_BOOLEAN_HH
#define _BOOLEAN_ENVELOPE_BOOLEAN_HH

#include "Boolean.hh"
#include "Time.hh"
#include "VariableManager.hh"
#include "Event.hh"
#include "Updater.hh"
#include "MessageSender.hh"
#include "Exception.hh"
#include "Observable.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Class which outputs a boolean envelope
    class BooleanEnvelopeBoolean : public Type::Boolean
    {
    public:
      BooleanEnvelopeBoolean( std::string aName ) : Type::Boolean ( aName ),
						    iUpdater ( NULL ), 
						    iMessageSender ( NULL ),
						    iTimesteper( NULL ),
						    iTimestep( 1 ),
						    iSimTimer( NULL ),
						    iSimTime( 0 ),
						    iOffseter( NULL ),
						    iOffset( 0 ),
						    iLengther( NULL ),
						    iLength( 10 ),
						    iEventer( NULL ),
						    iActive( false ),
						    iJustActive( true ),
						    iStartTime( 0 ),
						    iEndTime( 0 )
      {
      }
      
      virtual ~BooleanEnvelopeBoolean()
      {
	if (iUpdater != NULL)
	  {
	    iUpdater->removeObserver( this, 0 );
	  }
	if (iMessageSender != NULL)
	  {
	    iMessageSender->removeObserver( this, 1 );
	  }
	if (iTimesteper != NULL)
	  {
	    iTimesteper->removeObserver( this, 2 );
	  }
	if (iSimTimer != NULL)
	  {
	    iSimTimer->removeObserver( this, 3 );
	  }
	if (iOffseter != NULL)
	  {
	    iOffseter->removeObserver( this, 4 );
	  }
	if (iLengther != NULL)
	  {
	    iLengther->removeObserver( this, 5 );
	  }
	if (iEventer != NULL)
	  {
	    iEventer->removeObserver( this, 6 );
	  }
      }

      /// New data is available to observe from a source
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void observe( integer_t aReference )
      {
	double tmp;
	switch (aReference)
	  {
	  case 0: // updater
	    if (iJustActive)
	      {
		iActive = true;
		iJustActive = false;
		iStartTime = iSimTime + iOffset;
		iEndTime = iStartTime + iLength;
		if (iValue)
		  {
		    iValue = false;
		    iReceivedMessage = true;
		  }
	      }
	    if (iActive)
	      {
		if (iValue ? iEndTime <= iSimTime : iStartTime <= iSimTime)
		  {
		    iValue = !iValue;
		    iReceivedMessage = true;
		    alertObservers();
		    if (!iValue)
		      iActive = false;
		  }
	      }
	    break;

	  case 1: // messagesender
	    if (iReceivedMessage)
	      {
		iReceivedMessage = false;
		sendMessages();
	      }
	    break;

	  case 2: // timestep
	    iTimestep = iTimesteper->getValue();
	    break;
	  case 3: // simtime
	    tmp = iSimTime;
	    iSimTime = iSimTimer->getValue();
	    if (iActive)
	      {
		if ((iSimTime < tmp) || (iSimTime > tmp + iTimestep + 0.001))
		  calcState();
	      }
	    break;
	  case 4: // offset
	    iOffset = iOffseter->getValue();
	    if (iActive)
	      calcState();
	    break;
	  case 5:
	    iLength = iLengther->getValue();
	    if (iActive)
	      calcState();
	    break;
	  case 6:
	    iJustActive = true;
	    break;
	  default:
	    _ABORT_( "Unrecognised observable" );
	  }
      }

      /// Calculate current state
      void calcState()
      {
	iStartTime = iSimTime + iOffset;
	iEndTime = iStartTime + iLength;
	if (iValue)
	  {
	    if (iSimTime < iStartTime)
	      {
		iValue = false;
		iReceivedMessage = true;
	      }
	    if (iSimTime > iEndTime)
	      {
		iValue = false;
		iReceivedMessage = true;
		iActive = true;
	      }
	  }
	else
	  {
	    if (iEndTime < iSimTime)
	      {
		iValue = true;
		iReceivedMessage = true;
	      }
	  }
      }

      /// warning that the observable will no longer be available
      /**
       *
       * \param aReference the reference the Observable was given to
       * return
       *
       **/
      virtual void removeObservable( integer_t aReference )
      {
	switch (aReference)
	  {
	  case 0:
	    iUpdater = NULL;
	    break;
	  case 1:
	    iMessageSender = NULL;
	    break;
	  case 2:
	    iTimesteper = NULL;
	    break;
	  case 3:
	    iSimTimer = NULL;
	    break;
	  case 4:
	    iOffseter = NULL;
	    break;
	  case 5:
	    iLengther = NULL;
	    break;
	  case 6:
	    iEventer = NULL;
	    break;
	  default:
	    _ABORT_( "Unrecognised observable" );
	  }
	Base::VariableNodeDirector::getDirector().addUnresolvedDep( this );
      }
      
    protected:
      /// resolve complex dependencies of derived classes
      virtual bool resolveComplexDependencies()
      {
	Base::VariableNodeDirector &theVND =
	  Base::VariableNodeDirector::getDirector();

	if (iUpdater == NULL)
	  {
	    if (theVND.hasVariable( getParentName() + ".Updater" ))
	      {
		iUpdater =
		  dynamic_cast< Message::Updater* >
		  ( theVND.getVariable( getParentName() + ".Updater" ));
		iUpdater->addObserver( this, 0 );

	      }
	    else
	      {
		_DEBUG_( "Unresolved updater" );
		return false;
	      } 
	  }
   
	if (iMessageSender == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".MessageSender" ))
              {
                iMessageSender =
                  dynamic_cast< Message::MessageSender* >
                  ( theVND.getVariable( getParentName() + ".MessageSender" ));
                iMessageSender->addObserver( this, 1 );
              }
            else
              {
                _DEBUG_( "Unresolved message sender" );
                return false;
              }
          }

	if (iTimesteper == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".Timestep" ))
              {
                iTimesteper =
                  dynamic_cast< Unit::Time* >
                  ( theVND.getVariable( getParentName() + ".Timestep" ));
                iTimesteper->addObserver( this, 2 );
              }
            else
              {
                _DEBUG_( "Unresolved timestep" );
                return false;
              }
          }

	if (iSimTimer == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".SimTime" ))
              {
                iSimTimer =
                  dynamic_cast< Unit::Time* >
                  ( theVND.getVariable( getParentName() + ".SimTime" ));
                iSimTimer->addObserver( this, 3 );
              }
            else
              {
                _DEBUG_( "Unresolved simulation time" );
                return false;
              }
          }

	if (iOffseter == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".Offset" ))
              {
                iOffseter =
                  dynamic_cast< Unit::Time* >
                  ( theVND.getVariable( getParentName() + ".Offset" ));
                iOffseter->addObserver( this, 4 );
              }
            else
              {
                _DEBUG_( "Unresolved offset" );
                return false;
              }
          }

	if (iLengther == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".Length" ))
              {
                iLengther =
                  dynamic_cast< Unit::Time* >
                  ( theVND.getVariable( getParentName() + ".Length" ));
                iLengther->addObserver( this, 5 );
              }
            else
              {
                _DEBUG_( "Unresolved length" );
                return false;
              }
          }
	
      	if (iEventer == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".Trigger" ))
              {
                iEventer =
                  dynamic_cast< Simulator::Event* >
                  ( theVND.getVariable( getParentName() + ".Trigger" ));
                iEventer->addObserver( this, 6 );
              }
            else
              {
                _DEBUG_( "Unresolved event" );
                return false;
              }
          }
	
	return true;
      }

      Message::Updater *iUpdater;
      Message::MessageSender *iMessageSender;
      Unit::Time *iTimesteper;
      double iTimestep;
      Unit::Time *iSimTimer;
      double iSimTime;
      Unit::Time *iOffseter;
      double iOffset;
      Unit::Time *iLengther;
      double iLength;
      Simulator::Event *iEventer;
      /// Are booleans active?
      bool iActive;
      /// Have booleans just been activated?
      bool iJustActive;
      double iStartTime;
      double iEndTime;
    };
  }
}

#endif //ndef _BOOLEAN_ENVELOPE_BOOLEAN_HH
