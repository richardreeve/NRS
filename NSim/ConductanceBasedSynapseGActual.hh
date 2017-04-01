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

#ifndef _CONDUCTANCE_BASED_SYNAPSE_GACTUAL_HH
#define _CONDUCTANCE_BASED_SYNAPSE_GACTUAL_HH

#pragma interface

#include <cmath>
#include "Time.hh"
#include "Updater.hh"
#include "MessageSender.hh"
#include "Conductance.hh"
#include "Spike.hh"
#include "VariableManager.hh"
#include "Exception.hh"
#include "Observable.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Variable which measures current conductance of synapse
    class ConductanceBasedSynapseGActual : public Unit::Conductance,
					   public Base::Observer
    {
    public:
      ConductanceBasedSynapseGActual( std::string aName ) :
	Unit::Conductance( aName ),
	iTimestep( NULL ), iUpdater( NULL ),
	iMessageSender( NULL ),
	iDecayHalfLife( NULL ),
	iIntrinsicConductance( NULL ),
	iInputSpike( NULL ),
	iFacilitationConductance( NULL ),
	iFacDecayHalfLife( NULL ),
	iDepFrac( NULL ),
	iDepDecayHalfLife( NULL ),
	iTimestepValue( 1 ),
	iDecayHalfLifeValue( 1 ),
	iIntrinsicConductanceValue( 0 ),
	iBaseFacilitationConductance( 0 ),
	iFacilitationConductanceValue( 0 ),
	iFacDecayHalfLifeValue( 1 ),
	iBaseDepFrac( 1 ),
	iDepFracValue( 1 ),
	iDepDecayHalfLifeValue( 1 ),
	iStoredValue( 0 )
      {
	iDecay = exp (-log(2.0) * iTimestepValue / iDecayHalfLifeValue);
	iFacDecay = exp (-log(2.0) * iTimestepValue / 
			 iFacDecayHalfLifeValue);
	iDepDecay = exp (-log(2.0) * iTimestepValue / 
			 iDepDecayHalfLifeValue);
      }
      
      virtual ~ConductanceBasedSynapseGActual()
      {
	if (iTimestep != NULL)
	  {
	    iTimestep->removeObserver( this, 0 );
	  }
	if (iUpdater != NULL)
	  {
	    iUpdater->removeObserver( this, 1 );
	  }
	if (iMessageSender != NULL)
	  {
	    iMessageSender->removeObserver( this, 2 );
	  }
	if (iDecayHalfLife != NULL)
	  {
            iDecayHalfLife->removeObserver( this, 3 );
          }
        if (iIntrinsicConductance != NULL)
          {
            iIntrinsicConductance->removeObserver( this, 4 );
          }
        if (iInputSpike != NULL)
          {
            iInputSpike->removeObserver( this, 5 );
          }
	if (iFacilitationConductance != NULL)
	  {
	    iFacilitationConductance->removeObserver( this, 6 );
	  }
	if (iFacDecayHalfLife != NULL)
	  {
	    iFacDecayHalfLife->removeObserver( this, 7 );
	  }
	if (iDepFrac != NULL)
	  {
	    iDepFrac->removeObserver( this, 8 );
	  }
	if (iDepDecayHalfLife != NULL)
	  {
	    iDepDecayHalfLife->removeObserver( this, 9 );
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

        switch (aReference)
          {
          case 0: // timestep
            iTimestepValue = iTimestep->getValue();
	    iDecay = exp (-log(2.0) * iTimestepValue / iDecayHalfLifeValue);
	    iFacDecay = exp (-log(2.0) * iTimestepValue / 
			     iFacDecayHalfLifeValue);
	    iDepDecay = exp (-log(2.0) * iTimestepValue / 
			     iDepDecayHalfLifeValue);
            break;
          case 1: // updater
	    // decay the value of GSynActual (iValue) here
            iValue *= iDecay;
	    iFacilitationConductanceValue *= iFacDecay;
	    iDepFracValue = (iDepFracValue - 1) * iDepDecay + 1;
	    iReceivedMessage = false;
            break;
          case 2: // send messages
	    alertObservers();
	    sendMessages();
	    iReceivedMessage = true;
	    iValue += iStoredValue;
	    iStoredValue = 0;
            break;
          case 3: // half life
            iDecayHalfLifeValue = iDecayHalfLife->getValue();
	    iDecay = exp (-log(2.0) * iTimestepValue / iDecayHalfLifeValue);
            break;
          case 4: // Intrinsic conductance
            iIntrinsicConductanceValue = iIntrinsicConductance->getValue();
            break;
          case 5: // incoming spike
	    if (iReceivedMessage)
	      {
		iValue += (iIntrinsicConductanceValue + 
			   iFacilitationConductanceValue) * iDepFracValue;
	      }
	    else
	      {
		iStoredValue += (iIntrinsicConductanceValue + 
				 iFacilitationConductanceValue) * 
		  iDepFracValue;
	      }
	    iDepFracValue *= iBaseDepFrac;
	    iFacilitationConductanceValue += iBaseFacilitationConductance;
            break;
	  case 6: // FacCond
	    iBaseFacilitationConductance =
	      iFacilitationConductance->getValue();
	    break;
          case 7: // FacHalfLife
            iFacDecayHalfLifeValue = iFacDecayHalfLife->getValue();
	    iFacDecay = exp (-log(2.0) * iTimestepValue / 
			     iFacDecayHalfLifeValue);
            break;
	  case 8: // DepFrac
	    iBaseDepFrac = iDepFrac->getValue();
	    break;
          case 9: // DepHalfLife
            iDepDecayHalfLifeValue = iDepDecayHalfLife->getValue();
	    iDepDecay = exp (-log(2.0) * iTimestepValue / 
			     iDepDecayHalfLifeValue);
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
	    iTimestep = NULL;
	    break;
	  case 1:
	    iUpdater = NULL;
	    break;
	  case 2:
	    iMessageSender = NULL;
	    break;
          case 3:
            iDecayHalfLife = NULL;
            break;
	  case 4:
            iIntrinsicConductance = NULL;
            break;
          case 5:
            iInputSpike = NULL;
            break;
	  case 6:
	    iFacilitationConductance = NULL;
	    break;
	  case 7:
	    iFacDecayHalfLife = NULL;
	    break;
	  case 8:
	    iDepFrac = NULL;
	    break;
	  case 9:
	    iDepDecayHalfLife = NULL;
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
	
        if (iTimestep == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".Timestep" ))
              {
                iTimestep =
                  dynamic_cast< Unit::Time* >
                  ( theVND.getVariable( getParentName() + ".Timestep" ));
                iTimestep->addObserver( this, 0 );

		observe ( 0 );
              }
            else
              {
                _ERROR_( "Unresolved timestep" );
                return false;
              }
          }

        if (iUpdater == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".Updater" ))
              {
                iUpdater =
                  dynamic_cast< Message::Updater* >
                  ( theVND.getVariable( getParentName() + ".Updater" ));
                iUpdater->addObserver( this, 1 );
		
              }
            else
              {
                _ERROR_( "Unresolved updater" );
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
                iMessageSender->addObserver( this, 2 );
		
              }
            else
              {
                _ERROR_( "Unresolved messsagesender" );
                return false;
              }
          }
	
        if (iDecayHalfLife == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".DecayHalfLife"))
              {
                iDecayHalfLife =
                  dynamic_cast< Unit::Time* >
                  ( theVND.getVariable( getParentName() + ".DecayHalfLife"));
                iDecayHalfLife->addObserver( this, 3 );
		
		observe ( 3 );
              }
            else
              {
                _ERROR_( "Unresolved decay half life" );
                return false;
              }
          }
	
        if (iIntrinsicConductance == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".GSynI"))
              {
                iIntrinsicConductance =
                  dynamic_cast< Unit::Conductance* >
                  ( theVND.getVariable( getParentName() + ".GSynI"));
                iIntrinsicConductance->addObserver( this, 4 );
		
		observe ( 4 );
              }
            else
              {
                _ERROR_( "Unresolved Intrinsic Conductance" );
                return false;
              }
          }
	
        if (iInputSpike == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".InputSpike"))
              {
                iInputSpike =
                  dynamic_cast< Spike * >
                  ( theVND.getVariable( getParentName() + ".InputSpike"));
                iInputSpike->addObserver( this, 5 );
		
              }
            else
              {
                _ERROR_( "Unresolved conductance based spike" );
                return false;
              }
          }
	
        if (iFacilitationConductance == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".GFac"))
              {
                iFacilitationConductance =
                  dynamic_cast< Unit::Conductance * >
                  ( theVND.getVariable( getParentName() + ".GFac"));
                iFacilitationConductance->addObserver( this, 6 );
		
              }
            else
              {
                _ERROR_( "Unresolved conductance based spike" );
                return false;
              }
          }

        if (iFacDecayHalfLife == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".FacHalfLife"))
              {
                iFacDecayHalfLife =
                  dynamic_cast< Unit::Time * >
                  ( theVND.getVariable( getParentName() + ".FacHalfLife"));
                iFacDecayHalfLife->addObserver( this, 7 );
		
              }
            else
              {
                _ERROR_( "Unresolved conductance based spike" );
                return false;
              }
          }

        if (iDepFrac == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".DepFrac"))
              {
                iDepFrac =
                  dynamic_cast< Type::Number * >
                  ( theVND.getVariable( getParentName() + ".DepFrac"));
                iDepFrac->addObserver( this, 8 );
		
              }
            else
              {
                _ERROR_( "Unresolved conductance based spike" );
                return false;
              }
          }

        if (iDepDecayHalfLife == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".DepHalfLife"))
              {
                iDepDecayHalfLife =
                  dynamic_cast< Unit::Time * >
                  ( theVND.getVariable( getParentName() + ".DepHalfLife"));
                iDepDecayHalfLife->addObserver( this, 9 );
		
              }
            else
              {
                _ERROR_( "Unresolved conductance based spike" );
                return false;
              }
          }
	return true;
      }

      /// The timestep variable in the node
      Unit::Time *iTimestep;
      
      /// The updater variable in the node
      Message::Updater *iUpdater;

      /// The MessageSender variable in the node
      Message::MessageSender *iMessageSender;

      /// The synapse's decay half life
      Unit::Time *iDecayHalfLife;

      /// The synapse intrinsic conductance (to which it returns after a spike)
      Unit::Conductance *iIntrinsicConductance;

      /// The synapse's incoming spike
      Spike *iInputSpike;

      Unit::Conductance *iFacilitationConductance;

      Unit::Time *iFacDecayHalfLife;

      Type::Number *iDepFrac;

      Unit::Time *iDepDecayHalfLife;

      /// A local cache of the timestep
      double iTimestepValue;

      /// A local cache value of the membrane capacitance
      double iDecayHalfLifeValue;

      /// A local cache value of intrinsic conductance
      double iIntrinsicConductanceValue;

      double iBaseFacilitationConductance;

      double iFacilitationConductanceValue;

      double iFacDecayHalfLifeValue;

      double iBaseDepFrac;

      double iDepFracValue;

      double iDepDecayHalfLifeValue;

      /// Decay constant
      double iDecay;

      double iFacDecay;

      double iDepDecay;

      /// Temporary stored value
      double iStoredValue;

    };
  }
}

#endif //ndef _CONDUCTANCE_BASED_SYNAPSE_GACTUAL_HH
