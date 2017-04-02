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

#ifndef _AXON_SPIKE_HH
#define _AXON_SPIKE_HH

#include <queue>
#include <cmath>

#include "Spike.hh"
#include "Time.hh"
#include "VariableManager.hh"
#include "MessageSender.hh"
#include "Updater.hh"
#include "Exception.hh"
#include "Observable.hh"

namespace NRS
{
  namespace Simulator
  {
    /// A variable derived from Spike which handles delayed spikes in an Axon
    class AxonSpike : public Spike
    {
    public:
      /// Default constructor
      /**
       *
       * \param aName name of variable
       *
       **/
      AxonSpike ( std::string aName ) : Simulator::Spike ( aName ),
						 iMessageSender ( NULL ),
						 iUpdater( NULL ),
						 iTimesteper( NULL ),
						 iDelayer( NULL ),
						 iSpiker( NULL ),
						 iTimestep( 1.0 ),
						 iDelay( 1.0 ),
						 iInState( false ),
						 iLength( 2 )
						 
      {}
      
      /// Simple destructor
      virtual ~AxonSpike()
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
	if (iDelayer != NULL)
	  {
	    iDelayer->removeObserver( this, 3 );
	  }
	if (iSpiker != NULL)
	  {
	    iSpiker->removeObserver( this, 4 );
	  }
	for ( int i = 0; i < iLength; ++i )
	  iSpikes.push( false );
      }

      /// New data is available to observe from a source
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void observe( integer_t aReference )
      {
	int len;
	switch (aReference)
	  {
	  case 0:
	    iSpikes.push( iInState );
	    iInState = false;
	    break;
	  case 1:
	    if ( iSpikes.front() ) 
	      {
		sendMessages();
	      }
	    iSpikes.pop();
	    break;
	  case 2:
	    iTimestep = iTimesteper->getValue();
	  case 3:
	    iDelay = iDelayer->getValue();
		if (iTimestep > 0)
		  {
		    len = (int) ceil( iDelay / iTimestep ) + 1;
		    if (iLength > len)
		      {
			for ( int i = len; i < iLength; ++i )
			  iSpikes.pop();
		      }
		    else if (iLength < len)
		      {
			for ( int i = iLength; i < len; ++i )
			  iSpikes.push( false );
		      }
		    iLength = len;
		  }
	    break;
	  case 4:
	    iInState = true;
	    break;
	  default:
	    _ABORT_( "Unrecognised observable" );
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
	    Base::VariableNodeDirector::getDirector().addUnresolvedDep( this );
	    break;

	  case 1:
	    iMessageSender = NULL;
	    Base::VariableNodeDirector::getDirector().addUnresolvedDep( this );
	    break;

	  case 2:
	    iTimesteper = NULL;
	    Base::VariableNodeDirector::getDirector().addUnresolvedDep( this );
	    break;

	  case 3:
	    iDelayer = NULL;
	    Base::VariableNodeDirector::getDirector().addUnresolvedDep( this );
	    break;

	  case 4:
	    iSpiker = NULL;
	    Base::VariableNodeDirector::getDirector().addUnresolvedDep( this );
	    break;

	  default:
	    _ABORT_( "Unrecognised observable" );
	  }
      }
      
      /// Reset the system to its initial state
      virtual void reset()
      {
	while (!iSpikes.empty())
	  iSpikes.pop();
	for ( int i = 0; i < iLength; ++i )
	  iSpikes.push( false );
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
                _DEBUG_( "Unresolved Updater" );
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
		iTimestep = iTimesteper->getValue();
                iTimesteper->addObserver( this, 2 );
		if (iTimestep > 0)
		  {
		    int len = (int) ceil( iDelay / iTimestep ) + 1;
		    if (iLength > len)
		      {
			for ( int i = len; i < iLength; ++i )
			  iSpikes.pop();
		      }
		    else if (iLength < len)
		      {
			for ( int i = len; i < iLength; ++i )
			  iSpikes.push( false );
		      }
		    iLength = len;
		  }
              }
            else
              {
                _DEBUG_( "Unresolved Timestep" );
                return false;
              }
          }

	if (iDelayer == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".Delay" ))
              {
                iDelayer =
                  dynamic_cast< Unit::Time* >
                  ( theVND.getVariable( getParentName() + ".Delay" ));
		iDelay = iDelayer->getValue();
                iDelayer->addObserver( this, 3 );
		if (iTimestep > 0)
		  {
		    int len = (int) ceil( iDelay / iTimestep ) + 1;
		    if (iLength > len)
		      {
			for ( int i = len; i < iLength; ++i )
			  iSpikes.pop();
		      }
		    else if (iLength < len)
		      {
			for ( int i = len; i < iLength; ++i )
			  iSpikes.push( false );
		      }
		    iLength = len;
		  }
	      }
            else
              {
                _DEBUG_( "Unresolved Delay" );
                return false;
              }
          }

	if (iSpiker == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".InSpike" ))
              {
                iSpiker =
                  dynamic_cast< Spike* >
                  ( theVND.getVariable( getParentName() + ".InSpike" ));
                iSpiker->addObserver( this, 4 );
              }
            else
              {
                _DEBUG_( "Unresolved Spike" );
                return false;
              }
          }
	
	return true;
      }

      Message::MessageSender *iMessageSender;
      Message::Updater *iUpdater;
      Unit::Time *iTimesteper;
      Unit::Time *iDelayer;
      Spike *iSpiker;
      double iTimestep;
      double iDelay;
      bool iInState;
      std::queue< bool > iSpikes;
      int iLength;
    };
  }
}

#endif //ndef _AXON_SPIKE_HH
