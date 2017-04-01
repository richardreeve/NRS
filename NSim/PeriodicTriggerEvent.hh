/*
 * Copyright (C) 2004 Richard Reeve, Matthew Szenher and Edinburgh University
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

#ifndef _PERIODIC_TRIGGER_EVENT_HH
#define _PERIODIC_TRIGGER_EVENT_HH

#pragma interface

#include "Event.hh"
#include "Time.hh"
#include "VariableManager.hh"
#include "Updater.hh"
#include "MessageSender.hh"
#include "Exception.hh"
#include "Observable.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Output of periodic events
    class PeriodicTriggerEvent : public Event
    {
    public:
      PeriodicTriggerEvent ( std::string aName ) : Simulator::Event ( aName ),
						   iUpdater ( NULL ), 
						   iMessageSender ( NULL ),
						   iTimesteper( NULL ),
						   iTimestep( 1 ),
						   iSimTimer( NULL ),
						   iSimTime( 0 ),
						   iOffseter( NULL ),
						   iOffset( 0 ),
						   iPerioder( NULL ),
						   iPeriod( 10 ),
						   iNumberer( NULL ),
						   iNumber( -1 ),
						   iActive( false ),
						   iJustActive( true ),
						   iStartTime( 0 ),
						   iNextEvent( 0 )
      {
      }
      
      virtual ~PeriodicTriggerEvent()
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
	if (iPerioder != NULL)
	  {
	    iPerioder->removeObserver( this, 5 );
	  }
	if (iNumberer != NULL)
	  {
	    iNumberer->removeObserver( this, 6 );
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
		iStartTime = iSimTime;
		iNextEvent = iStartTime + iOffset;
		if (iNumber == 0)
		  iActive = false;
	      }
	    if (iActive && (iNextEvent <= iSimTime) && 
		(iNextEvent > iSimTime - iTimestep))
	      {
		iReceivedMessage = true;
		alertObservers();
		iNextEvent += iPeriod;
		if ((iStartTime + (iNumber - 1) * iPeriod + iOffset + iTimestep
		     < iNextEvent) && (iNumber >= 0))
		  iActive = false;
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
		  calcNext();
	      }
	    break;
	  case 4: // offset
	    iOffset = iOffseter->getValue();
	    if (iActive)
	      calcNext();
	    break;
	  case 5:
	    iPeriod = iPerioder->getValue();
	    if (iActive)
	      calcNext();
	    break;
	  case 6:
	    iNumber = (int) iNumberer->getValue();
	    if (iActive)
	      calcNext();
	    break;
	  default:
	    _ABORT_( "Unrecognised observable" );
	  }
      }

      /// Calculate next event timing.
      void calcNext()
      {
	int count = 0;
	iNextEvent = 0;
	while ((count != iNumber) && (iNextEvent <= iSimTime))
	  {
	    if (iSimTime < iStartTime + iOffset + (iPeriod * count))
	      iNextEvent = iStartTime + iOffset + (iPeriod * count);
	    count++;
	  }
	if (count == iNumber)
	  iActive = false;
      }

      /// Reset variable - active if no inputs, otherwise made active by inputs
      virtual void reset()
      {
	iJustActive = iLocalSourceList.empty() && iDistalSourceList.empty();
      }

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message. Since
       * the Variable is a Void, the message has no content.
       *
       **/
      virtual void receiveMessage()
      {
	iJustActive = true;
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
	    iPerioder = NULL;
	    break;
	  case 6:
	    iNumberer = NULL;
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

	if (iPerioder == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".Period" ))
              {
                iPerioder =
                  dynamic_cast< Unit::Time* >
                  ( theVND.getVariable( getParentName() + ".Period" ));
                iPerioder->addObserver( this, 5 );
              }
            else
              {
                _DEBUG_( "Unresolved period" );
                return false;
              }
          }

	if (iNumberer == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".Number" ))
              {
                iNumberer =
                  dynamic_cast< Type::Number* >
                  ( theVND.getVariable( getParentName() + ".Number" ));
                iNumberer->addObserver( this, 6 );
              }
            else
              {
                _DEBUG_( "Unresolved message sender" );
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
      Unit::Time *iPerioder;
      double iPeriod;
      Type::Number *iNumberer;
      int iNumber;
      /// Are events active?
      bool iActive;
      /// Have events just been activated?
      bool iJustActive;
      double iStartTime;
      double iNextEvent;
    };
  }
}

#endif //ndef _PERIODIC_TRIGGER_EVENT_HH
