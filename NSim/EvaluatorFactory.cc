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

#include "Evaluation.hh"
#include "Evaluator.hh"
#include "Node.hh"
#include "NodeFactory.hh"
#include "SpecialisedMFO.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Factory for nodes which latch an output on a boolean
    class EvaluatorFactory : public NRS::Base::NodeFactory
    {
    public:
      /// Default Constructor
      EvaluatorFactory() :
	NRS::Base::NodeFactory( "Evaluator", "" )
      {
      }

      /// Empty destructor
      virtual ~EvaluatorFactory()
      {
      }

    protected:
      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription()
      {
	iDescription = "A node which evaluates a simulation "
	  "when it has finished.";
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

	anIDMap[ "in" ] = makeID( 0, -1, true );
	anIDMap[ "out" ] = makeID( 0, -1, false );
	anIDMap[ "log" ] = makeID( 0, 1, false );

	// Trigger causes Evaluation to send messages on true
	aMFOGenMap["Trigger"] = new Base::SpecialisedMFOGenerator();
	addVariable( "Evaluation", "", "float", true, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aMFOGenMap.clear();

	anIDMap[ "out" ] = makeID( 0, 0, false );
	anIDMap[ "log" ] = makeID( 0, 0, false );

	addVariable( "Trigger", "Trigger output", "boolean", false,
		     false, anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
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
       * \return success of operation
       *
       **/
      virtual bool doCreate( const std::string &nodeName ) const
      {
	new Evaluator( nodeName, this );
	new Evaluation( nodeName + ".Evaluation" );

	return true;
      }
    };
  }
}

namespace
{
  NRS::Simulator::EvaluatorFactory sEF;
}
