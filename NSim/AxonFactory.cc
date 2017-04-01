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

#include <iostream>
#include <string>
#include "Exception.hh"
#include "NodeFactory.hh"
#include "MessageSender.hh"
#include "Updater.hh"
#include "Node.hh"
#include "Target.hh"
#include "Time.hh"
#include "Spike.hh"

#include "Axon.hh"
#include "AxonSpike.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Factory for Axon nodes, which delay the transmission of spikes
    class AxonFactory : public Base::NodeFactory
    {
    public:
      /// Default constructor
      AxonFactory() : NRS::Base::NodeFactory( "Axon", "Fixed Axon", 0, 0 )
      {
      }

      /// Empty destructor
      ~AxonFactory()
      {
      }

    protected:
      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription()
      {
	iDescription = "An axon with fixed delays.";
      }

      /// Virtual method to set what group the node is in for the CSL.
      virtual void setGroups()
      {
	addGroup( "SimulatorGroup", "Element of Simulator" );
      }

      /// Virtual method to describe the variables which will be
      /// contained in the node
      virtual void setVariables()
      {
	std::map< std::string, InterfaceDescription > anIDMap;
	std::map< std::string, std::string > aSegAttrRefMap;
	std::map< std::string, SegmentConstraint > aSegConsMap;

	
	// variables for Poisson Neuron

	anIDMap[ "in" ] = makeID( 1, 1, true );
	anIDMap[ "out" ] = makeID( 0, 0, false );
	anIDMap[ "log" ] = makeID( 0, 0, false );

	addVariable( "MessageSender", "", "MessageSender", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap );

	addVariable( "Updater", "", "Updater", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap );

	addVariable( "Timestep", "", "Time", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap );

	addVariable ( "InSpike", "Input Spike", "Spike",
		      false, false,
		      anIDMap, aSegAttrRefMap, aSegConsMap );

	anIDMap[ "in" ] = makeID( 0, 1, false );
	aSegAttrRefMap[ "Delay" ] = "t_delay";
	addVariable ( "Delay", "Axonal Delay", "Time",
		      false, true,
		      anIDMap, aSegAttrRefMap, aSegConsMap );
	aSegAttrRefMap.clear();

	anIDMap[ "in" ] = makeID( 0, 0, false );
	anIDMap[ "out" ] = makeID( 0, -1, true );
	anIDMap[ "log" ] = makeID( 0, 1, false );

	addVariable ( "OutSpike", "Output Spike", "Spike",
		      true, false,
		      anIDMap, aSegAttrRefMap, aSegConsMap );
      }

      /// Virtual method to describe the expected attributes
      virtual void setAttributes()
      {
	addAttribute( "name", "Name", "token", false, true,
		      false, true, false );
	addAttribute( "t_delay", "Axonal Delay", "Time",
		      false, false, true, false, true, "5" );
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
	if (theCreation.hasTargetVNID())
	  {
	    _ERROR_( "Trying to create a node with a vnid" );
	    return false;
	  }

	std::string aName( theCreation.getTargetVNName( true ) );

	new Axon( aName, this );

	new Message::MessageSender( aName + ".MessageSender" );
	new Message::Updater( aName + ".Updater" );
	new Unit::Time( aName + ".Timestep" );
	new Unit::Time( aName + ".Delay" );
	new Spike ( aName + ".InSpike" );
	new AxonSpike ( aName + ".OutSpike" );

	return true;
      }
    };
  }
}

namespace
{
  NRS::Simulator::AxonFactory sAF;
}
