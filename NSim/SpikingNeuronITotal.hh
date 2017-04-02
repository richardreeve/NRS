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

#ifndef _SPIKINGNEURONITOTAL_HH
#define _SPIKINGNEURONITOTAL_HH

#include "Current.hh"
#include "Float.hh"
//#include "Conductance.hh"
//#include "Voltage.hh"
//#include "ReversibleConductance.hh"
//#include "Updater.hh"
//#include "MessageSender.hh"
//#include "VariableManager.hh"
//#include "Exception.hh"
//#include "Observable.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Variable derived from Current which stores the total current
    /// flowing into a spiking neuron
    class SpikingNeuronITotal : public Unit::Current
    {
    public:
      /// Default constructor
      /**
       *
       * \param aName name of variable
       *
       **/
      SpikingNeuronITotal ( std::string aName );
      
      /// Simple destructor
      virtual ~SpikingNeuronITotal();

      /// Note arrival of specialised connection
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void addReaction( integer_t aReference );

      /// Value of an internal connection has changed
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void react( integer_t aReference );

    protected:

      /// Remove source
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doRemoveSource( integer_t aReference );

      /// Update state
      virtual void doUpdate();

   

      /// The neuron's synaptic input
      /// Message::ReversibleConductance *iConductanceBasedSynapseInput;

      /// Reference to neuron's leak conductance
      integer_t iBaseGReference;

      /// Remote leak conductance value pointer
      float_t *iBaseGValuePtr;

      /// Cache of the neuron's leak conductance
      float_t iBaseGValue;

      /// Reference to neuron's resting potential
      integer_t iBaseVReference;

      /// Remote resting potential value pointer
      float_t *iBaseVValuePtr;

      /// Cache of the neuron's resting potential
      float_t iBaseVValue;

      /// Reference to neuron's artificial current input
      integer_t iIInReference;

      /// Remote input current value pointer
      float_t *iIInValuePtr;

      /// Cache of the neuron's input current
      float_t iIInValue;

      /// The sum of all input currents in stored in iValue
    };
  }
}

#endif //ndef _SPIKINGNEURONITOTAL_HH
