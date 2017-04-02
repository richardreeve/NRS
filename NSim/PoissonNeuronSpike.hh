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

#ifndef _POISSON_NEURON_SPIKE_HH
#define _POISSON_NEURON_SPIKE_HH

#include "Spike.hh"
#include "Boolean.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Class derived from spike which handles spikes inside a
    /// PoissonNeuron, deciding probabilistically whether to spike
    /// based on mean spike gap.
    class PoissonNeuronSpike : public Spike
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of variable to be constructed
       *
       **/
      PoissonNeuronSpike ( std::string aName );
      
      /// Empty destructor
      virtual ~PoissonNeuronSpike();

      /// Note arrival of specialised connection - the timestep
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void addReaction( integer_t aReference );

      /// Timestep has changed
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

      /// Timestep reference
      integer_t iTReference;

      /// Remote timestep value pointer (NULL if missing)
      float_t *iTimestepValuePtr;

      /// A local cache of the timestep
      float_t iTimestepValue;

      /// Enable reference
      integer_t iEReference;

      /// Enable value pointer (NULL if missing)
      bool *iEnableValuePtr;

      /// A local cache of the enable boolean
      bool iEnableValue;

      /// MeanSG reference
      integer_t iMReference;

      /// Remote mean spike gap value pointer (NULL if missing)
      float_t *iMeanSGValuePtr;

      /// A local cache of the mean spike gap
      float_t iMeanSGValue;
    };
  }
}

#endif //ndef _POISSON_NEURON_SPIKE_HH
