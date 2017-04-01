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

#ifndef _SPIKINGNEURONGTOTAL_HH
#define _SPIKINGNEURONGTOTAL_HH

#pragma interface


#include "Conductance.hh"
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
    /// Variable which measures total conductance of a spiking neuron
    class SpikingNeuronGTotal : public Unit::Conductance
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of the variable to be constructed
       *
       **/
      SpikingNeuronGTotal ( std::string aName );
      
      /// Empty destructor
      virtual ~SpikingNeuronGTotal();

      /// Note arrival of specialised connection
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void addReaction( integer_t aReference );

      /// Value of internally connected variable  has changed
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void react( integer_t aReference );
      
    protected:

      /// Remove internal connection to another variable
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doRemoveSource( integer_t aReference );

      /// Update the state of this variable
      virtual void doUpdate();

      /// The neuron's synaptic input
      // not implementing this for now
      //Message::ReversibleConductance *iConductanceBasedSynapseInput;

      /// The neuron's leak conductance
      //Unit::Conductance *iGLeak;

      /// BaseG reference
      integer_t iBaseGReference;

      /// Remote BaseG pointer
      float_t *iBaseGValuePtr;

      /// Cache of the neuron's leak conductance
      float_t iBaseGValue;

      /// The sum of all input conductances in stored in iValue
    };
  }
}

#endif //ndef _SPIKINGNEURONGTOTAL_HH
