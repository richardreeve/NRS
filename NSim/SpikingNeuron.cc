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
#include "Current.hh"
#include "Exception.hh"
#include "FloatSegment.hh"
#include "Node.hh"
#include "NodeFactory.hh"
#include "SpikingNeuron.hh"
#include "Time.hh"
#include "Variable.hh"
#include "VariableManager.hh"
#include "Voltage.hh"

NRS::Simulator::SpikingNeuron::SpikingNeuron( std::string aName,
			      const Base::NodeFactory *aNF ) :
  Base::Variable( aName, Base::VariableNodeDirector::getDirector().
		  getVariableManager( aNF->getType() ) ),
  Base::Node( aName, Base::Variable::getVNID(), aNF ),
  iInRefractT( dynamic_cast< Type::FloatSegment& >( iInputLink.
						    getSegment( 0 ) ).
	       iValue ),
  iInBaseV( dynamic_cast< Type::FloatSegment& >( iInputLink.
						 getSegment( 1 ) ).
	    iValue ),
  iInSpikeV( dynamic_cast< Type::FloatSegment& >( iInputLink.
						  getSegment( 2 ) ).
	     iValue ),
  iInResetV( dynamic_cast< Type::FloatSegment& >( iInputLink.
						  getSegment( 3 ) ).
	     iValue ),
  iInBaseG( dynamic_cast< Type::FloatSegment& >( iInputLink.
						 getSegment( 4 ) ).
	    iValue ),
  iInMembC( dynamic_cast< Type::FloatSegment& >( iInputLink.
						 getSegment( 5 ) ).
	    iValue ),
  iInIIn( dynamic_cast< Type::FloatSegment& >( iInputLink.
						 getSegment( 6 ) ).
	    iValue ),
  iInMembV( dynamic_cast< Type::FloatSegment& >( iInputLink.
						 getSegment( 7 ) ).
	    iValue ),
  iOutRefractT( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						    getSegment( 0 ) ).
		iValue ),
  iOutBaseV( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						  getSegment( 1 ) ).
	     iValue ),
  iOutSpikeV( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						   getSegment( 2 ) ).
	      iValue ),
  iOutResetV( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						   getSegment( 3 ) ).
	      iValue ),
  iOutBaseG( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						  getSegment( 4 ) ).
	     iValue ),
  iOutMembC( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						  getSegment( 5 ) ).
	     iValue ),
  iOutIIn( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						  getSegment( 6 ) ).
	   iValue ),
  iOutMembV( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						  getSegment( 7 ) ).
	     iValue )
{
}

NRS::Simulator::SpikingNeuron::~SpikingNeuron()
{
}

void NRS::Simulator::SpikingNeuron::doReset()
{
  if (iVariableMap.find( "RefractT" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Time* >( iVariableMap[ "RefractT" ] )->
	setDefault( iOutRefractT );
    }
  if (iVariableMap.find( "BaseV" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Voltage* >( iVariableMap[ "BaseV" ] )->
	setDefault( iOutBaseV );
    }
  if (iVariableMap.find( "SpikeV" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Voltage* >( iVariableMap[ "SpikeV" ] )->
	setDefault( iOutSpikeV );
    }
  if (iVariableMap.find( "ResetV" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Voltage* >( iVariableMap[ "ResetV" ] )->
	setDefault( iOutResetV );
    }
  if (iVariableMap.find( "BaseG" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Conductance* >( iVariableMap[ "BaseG" ] )->
	setDefault( iOutBaseG );
    }
  if (iVariableMap.find( "MembC" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Capacitance* >( iVariableMap[ "MembC" ] )->
	setDefault( iOutMembC );
    }
  if (iVariableMap.find("IIn") != iVariableMap.end())
    {
      dynamic_cast< Unit::Current* >( iVariableMap[ "IIn" ] )->
	setDefault( iOutIIn );
    }
  if (iVariableMap.find("MembV") != iVariableMap.end())
    {
      dynamic_cast< Unit::Voltage* >( iVariableMap[ "MembV" ] )->
	setDefault( iOutMembV );
    }
}

void NRS::Simulator::SpikingNeuron::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      iSendMessage = true;
      iOutRefractT = iInRefractT;
      iOutBaseV = iInBaseV;
      iOutSpikeV = iInSpikeV;
      iOutResetV = iInResetV;
      iOutBaseG = iInBaseG;
      iOutMembC = iInMembC;
      iOutIIn = iInIIn;
      iOutMembV = iInMembV;
      reset();
    }
  else
    _ABORT_( "Don't know how to handle incoming links" );
}
