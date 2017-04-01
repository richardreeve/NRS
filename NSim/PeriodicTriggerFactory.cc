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
#include "Boolean.hh"

#include "PeriodicTrigger.hh"
#include "PeriodicTriggerEvent.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Factory for PeriodicTrigger nodes which set off signals at
    /// periodic intervals according to configuration
    class PeriodicTriggerFactory : public Base::NodeFactory
    {
    public:
      /// Default constructor
      PeriodicTriggerFactory() : NRS::Base::NodeFactory( "PeriodicTrigger", 
							 "Periodic Trigger",
							 0, 0 )
      {
      }

      /// Empty destructor
      ~PeriodicTriggerFactory()
      {
      }

    protected:
      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription()
      {
	iDescription = "A periodic trigger set off by a spike "
	  "(or automatically) after a fixed delay, with control "
	  "over the number of repetitions and the period.";
      }

      /// Virtual method to set what group the node is in for the CSL.
      virtual void setGroups()
      {
	addGroup( "SimulatorGroup", "Element of Simulator" );
	addGroup( "PatternGroup", "Pattern Generating Element" );
      }

      /// Virtual method to describe the variables which will be
      /// contained in the node
      virtual void setVariables()
      {
	std::map< std::string, InterfaceDescription > anIDMap;
	std::map< std::string, std::string > aSegAttrRefMap;
	std::map< std::string, SegmentConstraint > aSegConsMap;

	
	// variables for Periodic Trigger

	anIDMap[ "in" ] = makeID( 1, 1, true );
	anIDMap[ "out" ] = makeID( 0, -1, true );
	anIDMap[ "log" ] = makeID( 0, 0, false );

	addVariable( "MessageSender", "", "MessageSender", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap );

	addVariable( "Updater", "", "Updater", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap );

	addVariable( "Timestep", "", "Time", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap );

	addVariable( "SimTime", "", "Time", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap );

	anIDMap[ "in" ] = makeID( 0, 1, false );
	anIDMap[ "out" ] = makeID( 0, 0, false );
	aSegAttrRefMap[ "Offset" ] = "t_offset";
	addVariable ( "Offset", "Offset to first event", "Time",
		      false, true, anIDMap, aSegAttrRefMap, aSegConsMap );
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "Period" ] = "t_period";
	addVariable ( "Period", "Period of events", "Time",
		      false, true, anIDMap, aSegAttrRefMap, aSegConsMap );
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "Number" ] = "count";
	addVariable ( "Number", "Number of events", "number",
		      false, true, anIDMap, aSegAttrRefMap, aSegConsMap );
	aSegAttrRefMap.clear();


	anIDMap[ "in" ] = makeID( 0, -1, true );
	anIDMap[ "out" ] = makeID( 0, -1, true );
	anIDMap[ "log" ] = makeID( 0, 1, false );
	
	addVariable( "Trigger", "", "Event",
		     true, false, anIDMap, aSegAttrRefMap, aSegConsMap );

	aSegAttrRefMap.clear();

	std::map< std::string, AttributeRestriction > anARMap;
	addContent( "PatternGroup", 0, -1, anARMap );
      }

      /// Virtual method to describe the expected attributes
      virtual void setAttributes()
      {
	addAttribute( "name", "Name", "token", false, true,
		      false, true, false );
	addAttribute( "t_offset", "Offset to first event", "Time",
		      false, false, true, false, true, "0" );
	addAttribute( "t_period", "Period of events", "Time",
		      false, false, true, false, true, "10" );
	addAttribute( "count", "Number of events", "number",
		      false, false, true, false, true, "-1" );
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
      virtual bool createNode( Base::Target &theCreation )
      {
	if (theCreation.hasTargetVNID())
	  {
	    _ERROR_( "Trying to create a node with a vnid" );
	    return false;
	  }

	std::string aName( theCreation.getTargetVNName( true ) );

	new PeriodicTrigger( aName, this );

	new Message::MessageSender( aName + ".MessageSender" );
	new Message::Updater( aName + ".Updater" );
	new Unit::Time( aName + ".SimTime" );
	new Unit::Time( aName + ".Timestep" );
	new Unit::Time( aName + ".Offset" );
	new Unit::Time( aName + ".Period" );
	new Type::Number( aName + ".Number" );
	new PeriodicTriggerEvent ( aName + ".Trigger" );

	return true;
      }
    };
  }
}

namespace
{
  NRS::Simulator::PeriodicTriggerFactory sPeriodicTriggerF;
}
