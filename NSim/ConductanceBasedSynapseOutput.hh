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

#ifndef _CONDUCTANCE_BASED_SYNAPSE_OUTPUT_HH
#define _CONDUCTANCE_BASED_SYNAPSE_OUTPUT_HH

#pragma interface

#include "ConductanceBasedSynapseGActual.hh"
#include "Voltage.hh"

#include "ReversibleConductance.hh"
#include "ReversibleConductanceManager.hh"
#include "Exception.hh"
#include "Observable.hh"

namespace NRS
{
  namespace Simulator
  {

    /// Variable, based on reversible conductance, which outputs
    /// current state of a synapse
    class ConductanceBasedSynapseOutput :
      public Message::ReversibleConductance
    {
    public:
      /// Constructor
      ConductanceBasedSynapseOutput ( std::string aName ) :
	Message::ReversibleConductance( aName ), 
	iConductanceBasedSynapseGActual ( NULL ),
	iConductanceBasedSynapseVReversal ( NULL )
      {
	
      }
      
      /// Destructor
      virtual ~ConductanceBasedSynapseOutput()
      {
	if (iConductanceBasedSynapseGActual != NULL)
	  {
	    iConductanceBasedSynapseGActual->removeObserver( this, 0 );
	  }
	if (iConductanceBasedSynapseVReversal != NULL)
	  {
	    iConductanceBasedSynapseVReversal->removeObserver( this, 1 );
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
	  case 0:
	    iSynapticConductance = iConductanceBasedSynapseGActual->getValue();
	    alertObservers();
	    sendMessages();
	    break;
	  case 1:
	    iReversalPotential = iConductanceBasedSynapseVReversal->getValue();
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
            iConductanceBasedSynapseGActual = NULL;
            Base::VariableNodeDirector::getDirector().addUnresolvedDep( this );
            break;
          case 1:
            iConductanceBasedSynapseVReversal = NULL;
            Base::VariableNodeDirector::getDirector().addUnresolvedDep( this );
            break;
          default:
            _ABORT_( "Unrecognised observable" );
          }

      }

    protected:

      /// resolve complex dependencies of derived classes
      virtual bool resolveComplexDependencies()
      {
        Base::VariableNodeDirector &theVND =
          Base::VariableNodeDirector::getDirector();
	
        if (iConductanceBasedSynapseGActual == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".GActual" ))
              {
                iConductanceBasedSynapseGActual =
                  dynamic_cast< ConductanceBasedSynapseGActual* >
                  ( theVND.getVariable( getParentName() + ".GActual" ));
                iConductanceBasedSynapseGActual->addObserver( this, 0 );
		observe( 0 );
              }
            else
              {
                _ERROR_( "Unresolved GActual" );
                return false;
              }
          }

        if (iConductanceBasedSynapseVReversal == NULL)
          {
            if (theVND.hasVariable( getParentName() + ".ESyn" ))
              {
                iConductanceBasedSynapseVReversal =
                  dynamic_cast< Unit::Voltage* >
                  ( theVND.getVariable( getParentName() + ".ESyn" ));
		iConductanceBasedSynapseVReversal->addObserver( this, 1 );
		observe( 1 );
              }
            else
              {
                _ERROR_( "Unresolved reversal potential" );
                return false;
              }
          }

        return true;
      }
      /// A local cache of the synapse's current conductance
      ConductanceBasedSynapseGActual* iConductanceBasedSynapseGActual;

      /// A local cache of the synapse's reversal potential
      Unit::Voltage* iConductanceBasedSynapseVReversal;
    };
  }
}

#endif //ndef _CONDUCTANCE_BASED_SYNAPSE_OUTPUT_HH
