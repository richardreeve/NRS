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

#pragma implementation
#include <map>
#include <string>

#include "MessageFunctionObject.hh"
#include "NodeFactory.hh"
#include "Sample.hh"
#include "Activate.hh"

namespace NRS
{
  namespace Sample
  {
    /// Factory for Sample nodes, the root node for a sample timed simulator
    class SampleFactory : public Base::NodeFactory
    {
    public:
      /// Default constructor
      SampleFactory() : NRS::Base::NodeFactory( "Sample", "", 
					      false, false, 0, 1 )
      {
      }

      /// Empty destructor
      virtual ~SampleFactory()
      {
      }

    protected:
      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription()
      {
	iDescription = "The root node of a sample timed simulator";
      }

      /// Virtual method to set what group the node is in for the CSL.
      virtual void setGroups()
      {
      }

      /// Virtual method to describe the variables which will be
      /// contained in the node
      void setVariables()
      {
	std::map< std::string, InterfaceDescription > anIDMap;
	std::map< std::string, std::string > aSegAttrRefMap;
	std::map< std::string, SegmentConstraint > aSegConsMap;
	std::map< std::string, Base::MFOGenerator* > aMFOGenMap;

	anIDMap[ "in" ] = makeID( 1, 1, false );
	anIDMap[ "out" ] = makeID( 1, -1, true );
	anIDMap[ "log" ] = makeID( 0, 0, false );
	
	// The MainLoop variable is the only compulsory variable in
	// the system, and must appear in the root node. It is the
	// only connection between the main NRS.component C++ code and
	// the plugin. It is called as soon as all dependencies in the
	// system are satisfied (normally just that each variable has
	// the correct number of connections and each node has the
	// correct number of child nodes), and is then called in a
	// tight loop alternating with the external i/o of the system.
	addVariable( "MainLoop", "Main Loop", "MainLoop", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	anIDMap[ "out" ] = makeID( 0, -1, true );

	// Timestep of the simulation, value is set by Timer child node
	addVariable( "Timestep", "", "Time", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	
	// Current simulation time, value is set by Timer child node
	addVariable( "SimTime", "Simulation Time", "Time", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	
	// Signal to update state of variables in all nodes, triggered
	// by Timer child node. This alternates with the MessageSender
	// variable to ensure that all calculations are done before
	// messages are sent out so that there are no issues with the
	// order in which calculations are made.
	addVariable( "Updater", "", "Updater", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	// Signal to send messages from variables in all nodes,
	// triggered by Timer child node. This alternates with the
	// Updater variable to ensure that all calculations are done
	// before messages are sent out so that there are no issues
	// with the order in which calculations are made.
	addVariable( "MessageSender", "", "MessageSender", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	anIDMap[ "in" ] = makeID( 0, -1, false );
	// The Activate variable does not have to be present in the
	// root node, and so can be deleted here and in the doCreate
	// method below if not required. It does however allow the
	// component to be switched on and off at will instead of just
	// running the moment the node is created. Setting the
	// Activate variable to true will satisfy a dependency in the
	// system, and allow the MainLoop variable to be called,
	// setting it to false will do the reverse. Consequently a
	// boolean GUI control can be connected to the variable to
	// manually control the component activation.
	addVariable( "Activate", "", "boolean", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
      }

      /// Virtual method to describe the expected attributes
      virtual void setAttributes()
      {
	addAttribute( "name", "Name", "vnname", false, true,
		      false, true, false );
      }

      /// Virtual method to describe content description for the node.
      virtual void setContents()
      {
	std::map< std::string, AttributeRestriction > anARMap;

	// If all standard nodes in the sample component are declared
	// to be part of the "SampleGroup" group (in setGroups), then
	// they will appear in the GUI as child nodes of this parent.
	addContent( "SampleGroup", 0, -1, anARMap );

	// This component has a timer in it, probably as part of a
	// simulator; this has to be created as a node inside the root
	// node, and has to have a MainLoop variable as input, and
	// produce a Timestep, SimTime, Updater and MessageSender as
	// output. See the NSim directory for examples of these. The
	// system dependencies will not be satisfied unless there is
	// exactly one member of the TimerGroup as a child of the root
	// node.
	addContent( "TimerGroup", 1, 1, anARMap );

	// Some nodes may be allowed inside which feed back
	// information to a GA or other optimisation algorithm, for an
	// example of some of these look in the NSim directory
	addContent( "EvaluatorGroup", 0, -1, anARMap );
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
	new Sample( nodeName, this );
	new Message::Activate( nodeName + ".Activate" );

	return true;
      }
    };
  }
}



namespace
{
  NRS::Simulator::SampleFactory sSampleF;
}
