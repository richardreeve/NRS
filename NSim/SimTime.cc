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
#include <cmath>

#include "Exception.hh"
#include "FloatSegment.hh"
#include "MessageSender.hh"
#include "Observable.hh"
#include "Time.hh"
#include "Updater.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"
#include "SimTime.hh"

NRS::Simulator::SimTime::SimTime( std::string aName, bool realTime ) :
  Unit::Time( aName ), iTReference( 0 ), iTimestepValuePtr( NULL ),
  iTimestepValue( 0 ), iRealTimeErrorOnStep( 0 ), iLastLoopRealTimeError( 0 ), iRealTime( realTime )
{
}

NRS::Simulator::SimTime::~SimTime()
{
}

void NRS::Simulator::SimTime::addReaction( integer_t aReference )
{
  iTReference = aReference;
  iTimestepValuePtr = &dynamic_cast< Type::FloatSegment& >
    ( Base::VariableNodeDirector::getDirector().
      getVariable( iParentName + ".Timestep" )->getOutputLink().
      getSegment( 0 ) ).
    iValue;
}

void NRS::Simulator::SimTime::react( integer_t aReference )
{
  if (aReference == iTReference)
    {
      _ASSERT_( iTimestepValuePtr != NULL, "illegal call from NULL" );
      iTimestepValue = *iTimestepValuePtr;
    }
  else
    _WARN_( "Unrecognised observable " << aReference );
}

NRS::float_t * NRS::Simulator::SimTime::getRealTimeErrorValuePtr(){
  
  return (&iRealTimeErrorOnStep);
}

void NRS::Simulator::SimTime::doRemoveSource( integer_t aReference )
{
  if (aReference == iTReference)
    {
      iTimestepValuePtr = NULL;
      iTimestepValue = 0;
      iTReference = 0;
    }
  else
    Type::Float::doRemoveSource( aReference );
}

void NRS::Simulator::SimTime::doReset()
{
  struct timezone currTZ;
  
  Type::Float::doReset();
  if (iRealTime)
    gettimeofday( &iStartTV, &currTZ );
}

void NRS::Simulator::SimTime::doUpdate()
{
  struct timeval currTV;
  struct timezone currTZ;
  long val; //microseconds underrun
  
  _DEBUG_( "update " << iName );
  if (iTimestepValue != 0)
    {
      iValue += iTimestepValue;
      if (iRealTime)
	{
	  gettimeofday( &currTV, &currTZ );
	  val = ((long) 
		 (((double)(iStartTV.tv_sec - currTV.tv_sec) *
			 1000.0) +
		  iValue) * 1000) - currTV.tv_usec +
	    iStartTV.tv_usec;
	  if (val > 0){
	    usleep( val ); //val is microseconds underrun
	  } 
	  iRealTimeErrorOnStep = -val;//report underruns as negative
	  //}else if(iLastLoopRealTimeError>=0){ //Last loop was an underrun or just in time, so current val represents error on a single loop
	  // iRealTimeErrorOnStep = -val;
	  //}else{//Last loop was also an overrun, so the error on the current loop is:
	  //iRealTimeErrorOnStep = -(val-iLastLoopRealTimeError);
	  //}
	  iLastLoopRealTimeError=val;
	}
      
      iSendMessage = true;
    }
  _DEBUG_( iTimestepValue << ":" << iValue );
  if (fmod( iValue, 1000 ) < iTimestepValue ) 
    _INFO_( "Timestep " << iValue  << " realtime: " << std::boolalpha << iRealTime );
}
