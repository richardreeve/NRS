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

#include <map>
#include <string>

#include "FloatSegment.hh"
#include "MainLoopMFO.hh"
#include "MessageSenderMFO.hh"
#include "NodeFactory.hh"
#include "SimTime.hh"
#include "RealTimeError.hh"
#include "SpecialisedMFO.hh"
#include "Timer.hh"
#include "UpdaterMFO.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Factory for Timer nodes which keep track of the time, and
    /// create the message sender and updater calls for the rest of
    /// the system
    class TimerFactory : public Base::NodeFactory
    {
    public:
      /// Default constructor
      TimerFactory() : Base::NodeFactory( "Timer", "" )
      {
      }

      /// Constructor for derived types
      /**
       *
       * \param aName The name of the type of Node being managed.
       * \param aDisplayName The prettified version of the node name
       * for display purposes
       *
       **/
      TimerFactory( std::string aName, std::string aDisplayName ) :
	Base::NodeFactory( aName, aDisplayName )
      {
      }

      /// Empty destructor
      virtual ~TimerFactory()
      {
      }

    protected:
      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription()
      {
	iDescription = "The timer loop which connects to the main "
	  "loop of the simulator, locking to real-time";
      }

      /// Virtual method to set what group the node is in for the CSL.
      virtual void setGroups()
      {
	addGroup( "TimerGroup", "Type of Timer" );
      }

      /// Virtual method to describe the variables which will be
      /// contained in the node
      virtual void setVariables()
      {
	std::map< std::string, InterfaceDescription > anIDMap;
	std::map< std::string, std::string > aSegAttrRefMap;
	std::map< std::string, SegmentConstraint > aSegConsMap;
	std::map< std::string, Base::MFOGenerator* > aMFOGenMap;

	anIDMap[ "in" ] = makeID( 0, 0, false );
	anIDMap[ "out" ] = makeID( 1, -1, true );
	anIDMap[ "log" ] = makeID( 0, 0, false );

	// MainLoop causes MessageSender to update and send all its messages
	aMFOGenMap["MainLoop"] = new Message::MainLoopMFOGenerator();

	addVariable( "MessageSender", "", "MessageSender", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aMFOGenMap.clear();

	// MessageSender causes Updater to update and send all its messages
	aMFOGenMap["MessageSender"] = new Message::MainLoopMFOGenerator();

	addVariable( "Updater", "", "Updater", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aMFOGenMap.clear();

	anIDMap[ "out" ] = makeID( 0, -1, true );

	// Updater causes SimTime to update its state to the new timestep
	aMFOGenMap["Updater"] = new Message::UpdaterMFOGenerator();
	// MessageSender causes SimTime to send its messages
	aMFOGenMap["MessageSender"] = new Message::MessageSenderMFOGenerator();
	// Timestep causes SimTime to update its cache for next update
	aMFOGenMap["Timestep"] = new Base::SpecialisedMFOGenerator();

	aSegAttrRefMap[ "SimTime" ] = "initTime";
	addVariable( "SimTime", "Simulation Time", "Time", true, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aSegAttrRefMap.clear();
	aMFOGenMap.clear();

	// SimTime causes RealTimeError to update its state 
	aMFOGenMap["SimTime"] = new Base::SpecialisedMFOGenerator();
	// MessageSender causes RealTimeError to send its messages
	aMFOGenMap["MessageSender"] = new Message::MessageSenderMFOGenerator();
	// Updater causes RealTimeError to update its state to the new timestep
	//aMFOGenMap["Updater"] = new Message::UpdaterMFOGenerator();

	addVariable("RealTimeError", "Instantaneous Real-Time Error", "Time", true, true,
		    anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap);
	aMFOGenMap.clear();

	aSegAttrRefMap[ "Timestep" ] = "timestep";
	addVariable( "Timestep", "", "Time", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aSegAttrRefMap.clear();

	anIDMap[ "in" ] = makeID( 0, 1, true );
	anIDMap[ "out" ] = makeID( 0, 0, false );
	
	addVariable( "MainLoop", "Main Loop", "MainLoop", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
      }

      /// Virtual method to describe the expected attributes
      virtual void setAttributes()
      {
	addAttribute( "name", "Name", "vnname", false, true,
		      false, true, false );
	Type::FloatSegment *aTimestepSegPtr = new Type::FloatSegment();
	aTimestepSegPtr->iValue = 1;
	addAttribute( "timestep", "Timestep for simulation",
		      "Time", false, false, true, false, true,
		      aTimestepSegPtr );
	Type::FloatSegment *anInitSegPtr = new Type::FloatSegment();
	anInitSegPtr->iValue = 0;
	addAttribute( "initTime", "Initial time for simulation",
		      "Time", false, false, true, false, true,
		      anInitSegPtr );
      }

      /// Virtual method to describe content description for the node.
      virtual void setContents()
      {
	std::map< std::string, AttributeRestriction > anARMap;
	addContent( "TimerControl", 0, -1, anARMap );	
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
	new Timer( nodeName, this );
	new SimTime( nodeName + ".SimTime", true );
	new RealTimeError( nodeName + ".RealTimeError");

	return true;
      }
    };
  }
}
