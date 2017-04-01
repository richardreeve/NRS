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
#include <map>
#include <string>

#include "FloatSegment.hh"
#include "MessageSenderMFO.hh"
#include "NodeFactory.hh"
#include "PoissonNeuron.hh"
#include "PoissonNeuronSpike.hh"
#include "SpecialisedMFO.hh"
#include "UpdaterMFO.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Factory for creating nodes of type PoissonNeuron, a generator
    /// of poisson spike trains
    class PoissonNeuronFactory : public Base::NodeFactory
    {
    public:
      /// Default Constructor
      PoissonNeuronFactory() : NodeFactory( "PoissonNeuron", "Poisson Neuron" )
      {
      }

      /// Empty destructor
      ~PoissonNeuronFactory()
      {
      }

      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription()
      {
	iDescription = "The spiking neuron.";
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

	// variables for Poisson Neuron

	anIDMap[ "in" ] = makeID( 1, 1, true );
	anIDMap[ "out" ] = makeID( 0, 0, false );
	anIDMap[ "log" ] = makeID( 0, 0, false );

	addVariable( "MessageSender", "", "MessageSender", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	addVariable( "Updater", "", "Updater", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	addVariable( "Timestep", "", "Time", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap , aMFOGenMap);

	anIDMap[ "in" ] = makeID( 0, 1, false );
	aSegAttrRefMap[ "MeanSG" ] = "t_sg";
	addVariable ( "MeanSG", "Mean Inter-Spike Interval", "Time",
		      false, true,
		      anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aSegAttrRefMap.clear();

	addVariable( "enable", "Enable firing in neuron", "boolean",
		     false, true, anIDMap, aSegAttrRefMap, aSegConsMap,
		     aMFOGenMap );

	anIDMap[ "in" ] = makeID( 0, 0, false );
	anIDMap[ "out" ] = makeID( 0, -1, true );
	anIDMap[ "log" ] = makeID( 0, 1, false );

	// Timestep causes Spike to update its cache for next update
	aMFOGenMap["Timestep"] = new Base::SpecialisedMFOGenerator();
	// MeanSG causes Spike to update its cache for next update
	aMFOGenMap["MeanSG"] = new Base::SpecialisedMFOGenerator();
	// Enable causes Spike to update its cache for next update
	aMFOGenMap["enable"] = new Base::SpecialisedMFOGenerator();
	// Updater causes Spike to update its state to the new timestep
	aMFOGenMap["Updater"] = new Message::UpdaterMFOGenerator();
	// MessageSender causes Spike to send its messages
	aMFOGenMap["MessageSender"] = new Message::MessageSenderMFOGenerator();
	addVariable ( "Spike", "", "Spike",
		      true, false,
		      anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aSegAttrRefMap.clear();
      }

      /// Virtual method to describe the expected attributes
      virtual void setAttributes()
      {
	addAttribute( "name", "Name", "token", false, true,
		      false, true, false );
	Type::FloatSegment *anISISegPtr = new Type::FloatSegment();
	anISISegPtr->iValue = 5.0;
	addAttribute( "t_sg", "Inter-Spike Interval", "Time",
		      false, false, true, false, true, anISISegPtr );
      }

      /// Virtual method to describe content description for the node.
      virtual void setContents()
      {
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
	new PoissonNeuron( nodeName, this );
	new PoissonNeuronSpike ( nodeName + ".Spike" );

	return true;
      }
    };
  }
}

namespace
{
  NRS::Simulator::PoissonNeuronFactory sPoissonNeuronF;
}
