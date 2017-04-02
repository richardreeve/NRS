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

#include "Counter.hh"
#include "MainLoopMFO.hh"
#include "Node.hh"
#include "NodeFactory.hh"
#include "Sum.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Factory for nodes which count void events
    class CounterFactory : public Base::NodeFactory
    {
    public:
      /// Default Constructor
      CounterFactory() : Base::NodeFactory( "Counter", "", false )
      {
      }

      /// Empty destructor
      ~CounterFactory()
      {
      }

    protected:
      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription()
      {
	iDescription = "A node which counts spikes.";
      }

      /// Virtual method to set what group the node is in for the CSL.
      virtual void setGroups()
      {
	addGroup( "EvaluatorGroup", "Evaluation of simulation run" );
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
	anIDMap[ "out" ] = makeID( 0, -1, false );
	anIDMap[ "log" ] = makeID( 0, 0, false );

	// Spike triggers an increment in Sum
	aMFOGenMap["Spike"] = new Message::MainLoopMFOGenerator();
	addVariable( "Sum", "", "float", true, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aMFOGenMap.clear();

	anIDMap[ "in" ] = makeID( 0, -1, true );
	anIDMap[ "out" ] = makeID( 0, 0, false );
	anIDMap[ "log" ] = makeID( 0, 0, false );

	addVariable( "Spike", "Spike input", "Spike", false, false,
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
      }

      /// Virtual method to describe content description for the node.
      virtual bool doCreate( const std::string &nodeName ) const
      {
	new Counter( nodeName, this );
	new Sum( nodeName + ".Sum" );

	return true;
      }
    };
  }
}

namespace
{
  NRS::Simulator::CounterFactory sCF;
}
