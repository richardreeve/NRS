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

#include "Exception.hh"
#include "FloatSegment.hh"
#include "Node.hh"
#include "NodeFactory.hh"
#include "Time.hh"
#include "Timer.hh"
#include "Variable.hh"
#include "VariableManager.hh"

NRS::Simulator::Timer::Timer( std::string aName,
			      const Base::NodeFactory *aNF ) :
  Base::Variable( aName, Base::VariableNodeDirector::getDirector().
		  getVariableManager( aNF->getType() ) ),
  Base::Node( aName, Base::Variable::getVNID(), aNF ),
  iInTimestep( dynamic_cast< Type::FloatSegment& >( iInputLink.
						    getSegment( 0 ) ).
	       iValue ),
  iInSimTime( dynamic_cast< Type::FloatSegment& >( iInputLink.
						   getSegment( 1 ) ).
	      iValue ),
  iOutTimestep( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						     getSegment( 0 ) ).
		iValue ),
  iOutSimTime( dynamic_cast< Type::FloatSegment& >( iOutputLink.
						    getSegment( 1 ) ).
	       iValue )
{
}

NRS::Simulator::Timer::~Timer()
{
}

void NRS::Simulator::Timer::doReset()
{
  if (iVariableMap.find( "Timestep" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Time* >( iVariableMap[ "Timestep" ] )->
	setDefault( iOutTimestep );
    }
  if (iVariableMap.find( "SimTime" ) != iVariableMap.end())
    {
      dynamic_cast< Unit::Time* >( iVariableMap[ "SimTime" ] )->
	setDefault( iOutSimTime );
    }
}

void NRS::Simulator::Timer::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      iSendMessage = true;
      iOutTimestep = iInTimestep;
      iOutSimTime = iInSimTime;
      reset();
    }
  else
    _ABORT_( "Don't know how to handle incoming links" );
}
