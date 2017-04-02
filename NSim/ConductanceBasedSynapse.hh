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

#ifndef _CONDUCTANCEBASEDSYNAPSE_HH
#define _CONDUCTANCEBASEDSYNAPSE_HH

#include <string>

#include "Variable.hh"
#include "Types.hh"
#include "Node.hh"

namespace NRS
{
  namespace Base
  {
    class NodeFactory;
  }
  namespace Simulator
  {
    /// Node for conductance-based synapses, which connect spikes to
    /// neurons
    class ConductanceBasedSynapse : public Base::Variable, public Base::Node
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of node to be created
       * \param aNFptr pointer to NodeFactory which created the Node
       *
       **/      
      ConductanceBasedSynapse ( std::string aName,
				const Base::NodeFactory *aNFptr );
      
      /// Destructor
      virtual ~ConductanceBasedSynapse();

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

      /// The intrinsic conductance of the synapse
      const float_t &iInIntrinsicConductance;

      /// The reversal potential of the synapse
      const float_t &iInReversalPotential;

      /// The decay half life of the synapse
      const float_t &iInDecayHalfLife;

      const float_t &iInFacCond;

      const float_t &iInFacHalfLife;

      const float_t &iInDepFrac;

      const float_t &iInDepHalfLife;

      float_t &iOutIntrinsicConductance;

      float_t &iOutReversalPotential;
      
      float_t &iOutDecayHalfLife;
      
      float_t &iOutFacCond;
      
      float_t &iOutFacHalfLife;
      
      float_t &iOutDepFrac;
      
      float_t &iOutDepHalfLife;
    };
  }
}

#endif //ndef _CONDUCTANCEBASEDSYNAPSE_HH
