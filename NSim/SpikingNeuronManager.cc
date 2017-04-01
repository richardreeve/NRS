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

#pragma implementation
#include <string>

#include "VoltageManager.hh"
#include "CapacitanceManager.hh"
#include "ConductanceManager.hh"
#include "CurrentManager.hh"
#include "TimeManager.hh"
#include "StringLiterals.hh"
#include "TimeManager.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Class for managing SpikingNeuron nodes, which just describes the
    /// layout of the SpikingNeuron message
    class SpikingNeuronManager : public Base::VariableManager
    {
    public:
      /// Default constructor
      SpikingNeuronManager() :
	Base::VariableManager( "SpikingNeuron",
			       "Spiking Neuron",
			       8)
      {
	iDescription = "This variable/message type is the initialisation "
	  "for a SpikingNeuron node";
      }

      /// Destructor
      virtual ~SpikingNeuronManager()
      {
      }

    protected:
      /// Configure the internal records of the message type.
      /**
       *
       * This method configures the internal records of the manager so
       * that it can correctly output its CSL description on
       * request. This cannot conveniently be done at construction
       * time since too much work would be devolved to disparate
       * constructors. Called by configure(). Must create all of the
       * SegmentDescriptions for the manager.
       *
       **/
      virtual void doConfigure()
      {
	Base::VariableNodeDirector &theVND =
	  Base::VariableNodeDirector::getDirector();
	
	iSegmentDescription[0] =
	  theVND.getVariableManager( Unit::TimeManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[0].
	  setAttributeName( "refractT", "Refractory Period" );
	
	iSegmentDescription[1] =
	  theVND.getVariableManager( Unit::VoltageManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[1].
	  setAttributeName( "baseV",
			    "Reversal Potential for neural membrane" );

	iSegmentDescription[2] =
	  theVND.getVariableManager( Unit::VoltageManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[2].
	  setAttributeName( "spikeV",
			    "Spike threshold" );

	iSegmentDescription[3] =
	  theVND.getVariableManager( Unit::VoltageManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[3].
	  setAttributeName( "resetV",
			    "Reset potential after spike" );

	iSegmentDescription[4] =
	  theVND.getVariableManager( Unit::ConductanceManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[4].
	  setAttributeName( "baseG",
			    "Base conductance" );

	iSegmentDescription[5] =
	  theVND.getVariableManager( Unit::CapacitanceManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[5].
	  setAttributeName( "membC",
			    "Membrane Capacitance" );

	iSegmentDescription[6] =
	  theVND.getVariableManager( Unit::CurrentManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[6].
	  setAttributeName( "inI",
			    "Input Current" );

	iSegmentDescription[7] =
	  theVND.getVariableManager( Unit::VoltageManager::getName() ).
	  getSegmentDescription( 0 );
	iSegmentDescription[7].
	  setAttributeName( "membV",
			    "Initial Membrane Potential" );

	
      }
    };
  }
}

namespace
{
  NRS::Simulator::SpikingNeuronManager sSpikingNeuronM;
}
