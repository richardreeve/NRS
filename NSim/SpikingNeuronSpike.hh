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

#ifndef _SPIKING_NEURON_SPIKE_HH
#define _SPIKING_NEURON_SPIKE_HH

#include "Spike.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Class derived from spike which handles spikes inside a
    /// SpikingNeuron; spikes when voltage exceeds a threshold
    class SpikingNeuronSpike : public Spike
    {
    public:
      /// Default constructor
      /**
       *
       * \param aName name of variable to construct
       *
       **/
      SpikingNeuronSpike ( std::string aName );
      
      /// Simple destructor
      virtual ~SpikingNeuronSpike();

      /// Arrival of connection to the membrane potential
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void addReaction( integer_t aReference );

      /// Membrane Potential has Changed
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

      /// Update state of Spike variable
      virtual void doUpdate();

      /// Membrane potential reference
      integer_t iVActualReference;

      /// Remote membrane potential  value pointer
      float_t *iVActualValuePtr;

      /// A local cache of the membrane potential
      float_t iVActualValue;

      /// Threshold reference
      integer_t iThresholdReference;

      /// Threshold  value pointer
      float_t *iThresholdValuePtr;

      /// A local cache of the threshold
      float_t iThresholdValue;
    };
  }
}

#endif //ndef _SPIKING_NEURON_SPIKE_HH
