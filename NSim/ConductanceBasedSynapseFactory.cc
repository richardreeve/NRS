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

#include "ConductanceBasedSynapse.hh"
//#include "ConductanceBasedSynapseGActual.hh"
//#include "ConductanceBasedSynapseOutput.hh"
#include "FloatSegment.hh"
#include "MessageSenderMFO.hh"
#include "NodeFactory.hh"
#include "SpecialisedMFO.hh"
#include "UpdaterMFO.hh"
#include "Node.hh"
#include "Spike.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Factory for ConductancebasedSynapse, which receives spikes and
    /// alters the conductance of a neural membrane.
    class ConductanceBasedSynapseFactory : public Base::NodeFactory
    {
    public:
      /// Default constructor
      ConductanceBasedSynapseFactory():NodeFactory( "ConductanceBasedSynapse", 
				"Conductance Based Synapse")
      {
      }

      /// Empty destructor
      ~ConductanceBasedSynapseFactory()
      {
      }
      
    protected:
      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription()
      {
	iDescription = "The conductance based synapse.";
      }

      /// Virtual method to set what group the node is in for the CSL.
      virtual void setGroups()
      {
	addGroup( "SimulatorGroup", "Element of Simulator" );
	addGroup( "SynapseGroup", "Type of synapse" );
      }

      /// Virtual method to describe the variables which will be
      /// contained in the node
      virtual void setVariables()
      {
	std::map< std::string, InterfaceDescription > anIDMap;
	std::map< std::string, std::string > aSegAttrRefMap;
	std::map< std::string, SegmentConstraint > aSegConsMap;
	std::map< std::string, Base::MFOGenerator* > aMFOGenMap;
	
	// variables for ConductanceBasedSynapse

	/*anIDMap[ "in" ] = makeID( 0, 0, false );
	anIDMap[ "out" ] = makeID( 0, 1, false );
	anIDMap[ "log" ] = makeID( 0, 0, false );

	addVariable ( "GActual", "Synaptic Conductance",
		      "Conductance", true, true,
		      anIDMap, aSegAttrRefMap, aSegConsMap );*/

	anIDMap[ "in" ] = makeID( 1, 1, true );
	anIDMap[ "out" ] = makeID( 0, 0, false );
	anIDMap[ "log" ] = makeID( 0, 0, false );

	addVariable( "MessageSender", "", "MessageSender", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap);

	addVariable( "Updater", "", "Updater", false, false,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	addVariable( "Timestep", "", "Time", false, true,
                     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	anIDMap[ "in" ] = makeID( 0, 1, false );
	aSegAttrRefMap[ "GSynI" ] = "g_syni";
	addVariable ( "GSynI", "Intrinsic Synaptic Conductance",
		      "Conductance", false, true,
		      anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "ESyn" ] = "e_syn";
	addVariable ( "ESyn",
		      "Synaptic Reversal Potential", "Voltage", false, true,
		      anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "DecayHalfLife" ] = "decay_half_life";
	addVariable ("DecayHalfLife",
		     "Synaptic conductance decay half-life", "Time",
		     false, true, anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "GFac" ] = "g_fac";
	addVariable ( "GFac", "Facilitation Conductance",
		      "Conductance", false, true,
		      anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "FacHalfLife" ] = "fac_half_life";
	addVariable ("FacHalfLife",
		     "Synaptic facilitation decay half-life", "Time",
		     false, true, anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "DepFrac" ] = "dep_frac";
	addVariable ( "DepFrac", "Depression Fraction",
		      "float", false, true,
		      anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aSegAttrRefMap.clear();

	aSegAttrRefMap[ "DepHalfLife" ] = "dep_half_life";
	addVariable ("DepHalfLife",
		     "Synaptic depression decay half-life", "Time",
		     false, true, anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
	aSegAttrRefMap.clear();

	/*anIDMap[ "in" ] = makeID( 0, 0, true );
	anIDMap[ "out" ] = makeID( 0, 1, false );
	anIDMap[ "log" ] = makeID( 0, 0, false );
	addVariable ( "SynapticOutput", "Synaptic Output",
		      "ReversibleConductance", true, true,
		      anIDMap, aSegAttrRefMap, aSegConsMap );
		      aSegAttrRefMap.clear();*/

	/*anIDMap[ "in" ] = makeID( 0, -1, true );
	anIDMap[ "out" ] = makeID( 0, 0, false );
	anIDMap[ "log" ] = makeID( 0, 0, false );
	addVariable ( "InputSpike", "Input Spike",
		      "Spike", false, false,
		      anIDMap, aSegAttrRefMap, aSegConsMap );
		      aSegAttrRefMap.clear();*/
      }

      /// Virtual method to describe the expected attributes
      virtual void setAttributes()
      {
	addAttribute( "name", "Name", "token", false, true,
		      false, true, false );

	Type::FloatSegment *aGSynISegPtr = new Type::FloatSegment();
	aGSynISegPtr->iValue = 1;
	addAttribute( "g_syni", "Intrinsic Synaptic conductance", "Conductance", 
		      false, false, true, false, true, aGSynISegPtr );

	Type::FloatSegment *aESynSegPtr = new Type::FloatSegment();
	aESynSegPtr->iValue = 0;
	addAttribute( "e_syn", "Synaptic reversal potential", "Voltage",
		      false, false, true, false, true, aESynSegPtr );
	
	Type::FloatSegment *aDHLSegPtr = new Type::FloatSegment();
	aDHLSegPtr->iValue = 5;
	addAttribute( "decay_half_life", "Half life of synapse conductance", "Time",
		      false, false, true, false, true, aDHLSegPtr );

	Type::FloatSegment *aGFacSegPtr = new Type::FloatSegment();
	aGFacSegPtr->iValue = 0;
	addAttribute( "g_fac", "Facilitation Conductance",
		      "Conductance", false, false, true, false, true, aGFacSegPtr );

	Type::FloatSegment *aFHLSegPtr = new Type::FloatSegment();
	aFHLSegPtr->iValue = 3;
	addAttribute( "fac_half_life",
		      "Half life of facilitation conductance",
		      "Time", false, false, true, false, true, aFHLSegPtr);

	Type::FloatSegment *aDepFracSegPtr = new Type::FloatSegment();
	aDepFracSegPtr->iValue = 0;
	addAttribute( "dep_frac", "Depression Fraction",
		      "float", false, false, true, false, true, aDepFracSegPtr );

	Type::FloatSegment *aDepHLSegPtr = new Type::FloatSegment();
	aDepHLSegPtr->iValue = 3;
	addAttribute( "dep_half_life",
		      "Half life of depression conductance",
		      "Time", false, false, true, false, true, aDepHLSegPtr );
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

	new ConductanceBasedSynapse(nodeName, this);

	// GActual should be created after ESyn,
	// or ESyn may be slightly out of date

	// new ConductanceBasedSynapseGActual( aName + ".GActual" );
	// new ConductanceBasedSynapseOutput( aName + ".SynapticOutput" );

	return true;
      }
    };
  }
}

namespace
{
  NRS::Simulator::ConductanceBasedSynapseFactory sCBSF;
}
