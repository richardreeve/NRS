/*
 * Copyright (C) 2005 Richard Reeve and Edinburgh University
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
#include <map>
#include <string>

#include "FloatSegment.hh"
#include "MessageSenderMFO.hh"
#include "NodeFactory.hh"
#include "SpecialisedMFO.hh"
#include "SpikingNeuron.hh"
#include "SpikingNeuronVActual.hh"
//#include "SummedCurrent.hh"
#include "SpikingNeuronGTotal.hh"
#include "SpikingNeuronITotal.hh"
#include "SpikingNeuronSpike.hh"
#include "UpdaterMFO.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Factory for creating nodes of type SpikingNeuron, a leaky
    /// integrate and fire neural model
    class SpikingNeuronFactory : public Base::NodeFactory
    {
    public:
      /// Default Constructor
      SpikingNeuronFactory() : NodeFactory( "SpikingNeuron", "Spiking Neuron" )
      {
      }

      /// Empty destructor
      ~SpikingNeuronFactory()
      {
      }

      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription()
      {
	iDescription = "A spiking neuron model which implements a "
	  "'leaky integrate and fire' model of a biological neuron.";
      }

      /// Virtual method to set what group the node is in for the CSL.
      virtual void setGroups()
      {
	addGroup( "SimulatorGroup", "Element of Simulator" );
	addGroup( "NeuronGroup", "Type of neuron" );
      }

      /// Virtual method to describe the variables which will be
      /// contained in the node
      virtual void setVariables()
      {
	std::map< std::string, InterfaceDescription > anIDMap;
	std::map< std::string, std::string > aSegAttrRefMap;
	std::map< std::string, SegmentConstraint > aSegConsMap;
	std::map< std::string, Base::MFOGenerator* > aMFOGenMap;

	// variables for Spiking Neuron

	anIDMap[ "in" ] = makeID( 1, 1, true );
	anIDMap[ "out" ] = makeID( 0, 0, false );
	anIDMap[ "log" ] = makeID( 0, 0, false );

	addVariable( "MessageSender", "", "MessageSender", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	addVariable( "Updater", "", "Updater", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	addVariable( "Timestep", "", "Time", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);

	anIDMap[ "in" ] = makeID( 0, 1, true );
	anIDMap[ "out" ] = makeID( 0, -1, false );

	aSegAttrRefMap[ "Time" ] = "refractT";
	addVariable( "RefractoryPeriod", "Refractory Period", "Time", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "Voltage" ] = "baseV";
	addVariable( "BaseV", "Reversal Potential for neural membrane",
		     "Voltage", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "Voltage" ] = "spikeV";
	addVariable( "SpikeV", "Spike threshold", "Voltage", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "Voltage" ] = "resetV";
	addVariable( "ResetV", "Reset potential after spike", "Voltage",
		     false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "Conductance" ] = "baseG";	
	addVariable( "BaseG", "Base conductance", "Conductance", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "Capacitance" ] = "membC";	
	addVariable( "MembC", "Membrane Capacitance", "Capacitance", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);
	aSegAttrRefMap.clear();

	anIDMap["in"] = makeID(0,0,false);

	// Updater links to GTotal
	aMFOGenMap["Updater"] = new Message::UpdaterMFOGenerator();
	// MessageSender links to GTotal
	aMFOGenMap["MessageSender"] = new Message::MessageSenderMFOGenerator();
	// BaseG links to GTotal
	aMFOGenMap["BaseG"] = new Base::SpecialisedMFOGenerator();

	addVariable("GTotal", "Total Conductance", "Conductance", true, true,
		    anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);

	aMFOGenMap.clear();

	anIDMap[ "in" ] = makeID( 0, 1, false );

	aSegAttrRefMap[ "Current" ] = "inI";
	addVariable( "IIn", "Current Input", "Current", false, true,
	anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);
	aSegAttrRefMap.clear();

	anIDMap["in"] = makeID(0,0,false);
	
	// Updater links to ITotal
	aMFOGenMap["Updater"] = new Message::UpdaterMFOGenerator();
	// MessageSender links to ITotal
	aMFOGenMap["MessageSender"] = new Message::MessageSenderMFOGenerator();
	// BaseG links to ITotal
	aMFOGenMap["BaseG"] = new Base::SpecialisedMFOGenerator();
	// BaseV links to ITotal
	aMFOGenMap["BaseV"] = new Base::SpecialisedMFOGenerator();
	//IIn links to ITotal
	aMFOGenMap["IIn"] = new Base::SpecialisedMFOGenerator();

	addVariable("ITotal", "Total Current", "Current", true,true,
		    anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);

	aMFOGenMap.clear();

	// Updater links to Spike
	aMFOGenMap["Updater"] = new Message::UpdaterMFOGenerator();
	// MessageSender links to Spike
	aMFOGenMap["MessageSender"] = new Message::MessageSenderMFOGenerator();
	// MembV links to Spike
	aMFOGenMap["MembV"] = new Base::SpecialisedMFOGenerator();
	// Threshold links to Spike
	aMFOGenMap["SpikeV"] = new Base::SpecialisedMFOGenerator();
	
	addVariable("Spike","Spike","Spike",true,false,
		    anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);

	aMFOGenMap.clear();
	
 	// Updater causes MembV to update its state to the new timestep
 	aMFOGenMap["Updater"] = new Message::UpdaterMFOGenerator();
 	// MessageSender causes MembV to send its messages
 	aMFOGenMap["MessageSender"] = new Message::MessageSenderMFOGenerator();
 	// Timestep causes MembV to update its cache for next update
 	aMFOGenMap["Timestep"] = new Base::SpecialisedMFOGenerator();
 	// RefractoryPeriod causes MembV to update its cache for next update
 	aMFOGenMap["RefractoryPeriod"] = new Base::SpecialisedMFOGenerator();
 	// BaseV causes MembV to update its cache for next update
 	aMFOGenMap["BaseV"] = new Base::SpecialisedMFOGenerator();
	// SpikeV causes MembV to update its cache for the next update
	aMFOGenMap["SpikeV"] = new Base::SpecialisedMFOGenerator();
 	// ResetV causes MembV to update its cache for next update
 	aMFOGenMap["ResetV"] = new Base::SpecialisedMFOGenerator();
 	// BaseG causes MembV to update its cache for next update
 	aMFOGenMap["BaseG"] = new Base::SpecialisedMFOGenerator();
 	// MembC causes MembV to update its cache for next update
 	aMFOGenMap["MembC"] = new Base::SpecialisedMFOGenerator();
	// ITotal causes MembV to update its cache for next update
 	aMFOGenMap["ITotal"] = new Base::SpecialisedMFOGenerator();
 	// GTotal causes MembV to update its cache for next update
 	aMFOGenMap["GTotal"] = new Base::SpecialisedMFOGenerator();
 	// IIn causes MembV to update its cache for next update
 	//aMFOGenMap["IIn"] = new Base::SpecialisedMFOGenerator();
 	// SynIn causes MembV to update its cache for next update
 	//aMFOGenMap["SynIn"] = new Base::SpecialisedMFOGenerator();

	addVariable( "MembV", "Membrane Potential", "Voltage", true, true,
	anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);
	aMFOGenMap.clear();
      }

      /// Virtual method to describe the expected attributes
      virtual void setAttributes()
      {
	addAttribute( "name", "Name", "token", false, true,
		      false, true, false );

	Type::FloatSegment *aRefSegPtr = new Type::FloatSegment();
	aRefSegPtr->iValue = 1.0;
	addAttribute( "refractT", "Refractory Period", "Time",
		      false, false, true, false, true, aRefSegPtr );

	Type::FloatSegment *aBaseVSegPtr = new Type::FloatSegment();
	aBaseVSegPtr->iValue = -80.0;
	addAttribute( "baseV", "Reversal Potential for neural membrane",
		      "Voltage",
		      false, false, true, false, true, aBaseVSegPtr );

	Type::FloatSegment *aSpikeVSegPtr = new Type::FloatSegment();
	aSpikeVSegPtr->iValue = -40.0;
	addAttribute( "spikeV", "Spike threshold",
		      "Voltage",
		      false, false, true, false, true, aSpikeVSegPtr );

	Type::FloatSegment *aResetVSegPtr = new Type::FloatSegment();
	aResetVSegPtr->iValue = -60.0;
	addAttribute( "resetV", "Reset potential after spike",
		      "Voltage",
		      false, false, true, false, true, aResetVSegPtr );

	Type::FloatSegment *aBaseGSegPtr = new Type::FloatSegment();
	aBaseGSegPtr->iValue = 5.0;
	addAttribute( "baseG", "Base conductance",
		      "Conductance",
		      false, false, true, false, true, aBaseGSegPtr );

	Type::FloatSegment *aMembCSegPtr = new Type::FloatSegment();
	aMembCSegPtr->iValue = 10.0;
	addAttribute( "membC", "Membrane Capacitance",
		      "Capacitance",
		      false, false, true, false, true, aMembCSegPtr );

	Type::FloatSegment *aIInSegPtr = new Type::FloatSegment();
	aIInSegPtr->iValue = 1.0;
	addAttribute( "inI", "Input Current",
		      "Current",
		      false, false, true, false, true, aIInSegPtr );

	Type::FloatSegment *aMembVSegPtr = new Type::FloatSegment();
	aMembVSegPtr->iValue = -80.0;
	addAttribute( "membV", "Initial Membrane Potential",
		      "Voltage",
		      false, false, true, false, true, aMembVSegPtr );

      }

      /// Virtual method to describe content description for the node.
      virtual void setContents()
      {
	//	std::map< std::string, AttributeRestriction > anARMap;
	//	addContent( "SimulatorGroup", 0, -1, anARMap );
      }

      /// Create the variables and node for a node
      /**
       *
       * Any non-standard variables, which is to say, any
       * self-updating variables in general, have to be created by the
       * factory itself - they are created here, which is called by
       * createNode before configuring them. Any variables not created
       * here are created in createNode. Variables and Nodes are just
       * created using new, there is no need to do anything with the
       * pointers generated as they register themselves automatically
       * with the VariableNodeDirector.
       *
       * \param nodeName node of name to create
       *
       * \return success of operation
       *
       **/
      virtual bool doCreate( const std::string &nodeName ) const
      {
	new SpikingNeuron( nodeName, this );

	// All self-updating variables go here

	new SpikingNeuronGTotal(nodeName + ".GTotal");
	new SpikingNeuronITotal(nodeName + ".ITotal");
	
	new SpikingNeuronVActual( nodeName + ".MembV" );
	new SpikingNeuronSpike( nodeName + ".Spike" );

	// Also all variables of standard types not using default classes

	//new SummedCurrent( nodeName + ".IIn" );

	return true;
      }
    };
  }
}

namespace
{
  NRS::Simulator::SpikingNeuronFactory sSpikingNeuronF;
}
