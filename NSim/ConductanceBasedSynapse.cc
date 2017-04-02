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

#include "Capacitance.hh"
#include "Conductance.hh"
#include "ConductanceBasedSynapse.hh"
#include "Exception.hh"
#include "FloatSegment.hh"
#include "Node.hh"
#include "NodeFactory.hh"
#include "Time.hh"
#include "Variable.hh"
#include "VariableManager.hh"
#include "Voltage.hh"


NRS::Simulator::ConductanceBasedSynapse::ConductanceBasedSynapse( std::string aName,
					      const Base::NodeFactory *aNF ):
  Base::Variable( aName, Base::VariableNodeDirector::getDirector().
		  getVariableManager( aNF->getType() ) ),
  Base::Node( aName, Base::Variable::getVNID(), aNF ),
  iInIntrinsicConductance(dynamic_cast< Type::FloatSegment& >( iInputLink.
							       getSegment( 0 ) ).
			  iValue ),
  iInReversalPotential(dynamic_cast< Type::FloatSegment& >( iInputLink.
							    getSegment( 1 ) ).
		       iValue ),
  iInDecayHalfLife(dynamic_cast< Type::FloatSegment& >( iInputLink.
							getSegment( 2 ) ).
		   iValue ),  
  iInFacCond(dynamic_cast< Type::FloatSegment& >( iInputLink.
						  getSegment( 3 ) ).
	     iValue ),
  iInFacHalfLife(dynamic_cast< Type::FloatSegment& >( iInputLink.
						      getSegment( 4 ) ).
		 iValue ),
  iInDepFrac( dynamic_cast< Type::FloatSegment& >( iInputLink.
						   getSegment( 5 ) ).
	      iValue ),
  iInDepHalfLife( dynamic_cast< Type::FloatSegment& >( iInputLink.
						       getSegment( 6 ) ).
		  iValue ),
  iOutIntrinsicConductance( dynamic_cast< Type::FloatSegment& >( iOutputLink.
								 getSegment( 0 ) ).
			    iValue ),
  iOutReversalPotential( dynamic_cast< Type::FloatSegment& >( iOutputLink.
							      getSegment( 1 ) ).
			 iValue ),
  iOutDecayHalfLife( dynamic_cast< Type::FloatSegment& >( iOutputLink.
							  getSegment( 2 ) ).
		     iValue ),      
  iOutFacCond( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						    getSegment( 3 ) ).
	       iValue ),     
  iOutFacHalfLife( dynamic_cast< Type::FloatSegment& >( iOutputLink.
							getSegment( 4 ) ).
		   iValue ),      
  iOutDepFrac( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						    getSegment( 5 ) ).
	       iValue ),      
  iOutDepHalfLife( dynamic_cast< Type::FloatSegment& >( iOutputLink.
							getSegment( 6 ) ).
		   iValue )
{
}


NRS::Simulator::ConductanceBasedSynapse::~ConductanceBasedSynapse()
{
}

void NRS::Simulator::ConductanceBasedSynapse::doReset(){
  
  if (iVariableMap.find( "GSynI" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Conductance* >( iVariableMap[ "GSynI" ] ) -> 
	setDefault( iOutIntrinsicConductance );
    }

  if (iVariableMap.find( "ESyn" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Voltage* >( iVariableMap[ "ESyn" ] ) -> 
	setDefault( iOutReversalPotential );
    }

  if (iVariableMap.find( "DecayHalfLife" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Time* >( iVariableMap[ "DecayHalfLife" ] ) -> 
	setDefault( iOutDecayHalfLife );
    }

  if (iVariableMap.find( "GFac" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Conductance* >( iVariableMap[ "GFac" ] ) -> 
	setDefault( iOutFacCond );
    }

  if (iVariableMap.find( "FacHalfLife" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Time* >( iVariableMap[ "FacHalfLife" ] ) -> 
	setDefault( iOutFacHalfLife );
    }

  if (iVariableMap.find( "DepFrac" ) != iVariableMap.end())
    {
      dynamic_cast< Type::Float* >( iVariableMap[ "DepFrac" ] ) -> 
	setDefault( iOutDepFrac );
    }

  if (iVariableMap.find( "DepHalfLife" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Time* >( iVariableMap[ "DepHalfLife" ] ) -> 
	setDefault( iOutDepHalfLife );
    }
}


void NRS::Simulator::ConductanceBasedSynapse::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      iSendMessage = true;
      iOutIntrinsicConductance = iInIntrinsicConductance;
      iOutReversalPotential = iInReversalPotential;
      iOutDecayHalfLife = iInDecayHalfLife;
      iOutFacCond = iInFacCond;
      iOutFacHalfLife = iInFacHalfLife;
      iOutDepFrac = iInDepFrac;
      iOutDepHalfLife = iInDepHalfLife;
      reset();
    }
  else
    _ABORT_( "Don't know how to handle incoming links" );
}
