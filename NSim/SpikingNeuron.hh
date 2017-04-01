/*
 * Copyright (C) 2004-5 Richard Reeve, Matthew Szenher and Edinburgh University
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

#ifndef _SPIKING_NEURON_HH
#define _SPIKING_NEURON_HH

#pragma interface
#include <string>

#include "Node.hh"
#include "Types.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Base
  {
    class NodeFactory;
  }
  namespace Simulator
  {
    /// Class for spiking neurons, a leaky integrate and fire model of
    /// biological neurons
    class SpikingNeuron : public Base::Variable, public Base::Node
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of timer
       * \param aNF pointer to NodeFactory
       *
       **/
      SpikingNeuron( std::string aName, const Base::NodeFactory *aNF );
      
      /// Destructor
      virtual ~SpikingNeuron();


    protected:
      /// Reset the system to its initial state
      virtual void doReset();

      /// New data is available to observe from a source
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doObserve( integer_t aRef );

    private:
      /// Refractory Period
      const float_t &iInRefractT;

      /// Reversal Potential for neural membrane and initial state of neuron
      const float_t &iInBaseV;

      /// Spike threshold
      const float_t &iInSpikeV;

      /// Reset potential after spike
      const float_t &iInResetV;

      /// Base conductance
      const float_t &iInBaseG;

      /// Membrane Capacitance
      const float_t &iInMembC;

      /// Input current
      const float_t &iInIIn;

      /// Membrane potential
      const float_t &iInMembV;

      /// Refractory Period
      float_t &iOutRefractT;

      /// Reversal Potential for neural membrane and initial state of neuron
      float_t &iOutBaseV;

      /// Spike threshold
      float_t &iOutSpikeV;

      /// Reset potential after spike
      float_t &iOutResetV;

      /// Base conductance
      float_t &iOutBaseG;

      /// Membrane Capacitance
      float_t &iOutMembC;

      /// Input Current
      float_t &iOutIIn;

      /// Membrane Potential
      float_t &iOutMembV;
    };
  }
}

#endif //ndef _SPIKING_NEURON_HH
