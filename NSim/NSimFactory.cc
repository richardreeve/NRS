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

#include "MessageFunctionObject.hh"
#include "NodeFactory.hh"
#include "NSim.hh"
#include "Activate.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Factory for NSim nodes, the root node for the neural simulator
    class NSimFactory : public Base::NodeFactory
    {
    public:
      /// Default constructor
      NSimFactory() : NRS::Base::NodeFactory( "NSim", "", 
					      false, false, 0, 1 )
      {
      }

      /// Empty destructor
      virtual ~NSimFactory()
      {
      }

    protected:
      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription()
      {
	iDescription = "The root node of the neural simulator";
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
	anIDMap[ "out" ] = makeID( 0, -1, true );
	anIDMap[ "log" ] = makeID( 0, 0, false );

	addVariable( "MessageSender", "", "MessageSender", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	addVariable( "Updater", "", "Updater", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	addVariable( "Timestep", "", "Time", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	
	addVariable( "SimTime", "Simulation Time", "Time", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	
	anIDMap[ "out" ] = makeID( 1, -1, true );
	
	addVariable( "MainLoop", "Main Loop", "MainLoop", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	anIDMap[ "in" ] = makeID( 0, -1, false );
	anIDMap[ "out" ] = makeID( 0, -1, true );
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
	addContent( "EvaluatorGroup", 0, -1, anARMap );
	addContent( "SimulatorGroup", 0, -1, anARMap );
	addContent( "TimerGroup", 1, 1, anARMap );
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
	new NSim( nodeName, this );
	new Message::Activate( nodeName + ".Activate" );

	return true;
      }
    };
  }
}



namespace
{
  NRS::Simulator::NSimFactory sNSimF;
}
