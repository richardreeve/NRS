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

#include <string>

#include "TimeManager.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Manager for the message type associated with nodes of type
    /// PoissonNeuron. Details message format, one argument.
    class PoissonNeuronManager : public Unit::TimeManager
    {
    public:
      /// Default constructor
      PoissonNeuronManager() :
	Unit::TimeManager( "PoissonNeuron",
			   "Poisson neuron for neural simulator" )
      {
	iDescription = "This variable/message type of the poisson neuron "
	  "of the simulation";
	iSegmentDescription[0].setType( Enum::Float, getName() );
	iSegmentDescription[0].setAttributeName( "t_sg" );
      }

      /// Empty destructor
      virtual ~PoissonNeuronManager()
      {
      }

    protected:
      /// Configure the internal records of the message type.
      /**
       *
       * No internal configuration needed.
       *
       **/
      virtual void doConfigure()
      {
      }
    };
  }
}

namespace
{
  NRS::Simulator::PoissonNeuronManager sPoissonNeuronM;
}
