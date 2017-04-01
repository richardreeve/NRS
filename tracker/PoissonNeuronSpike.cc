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
#include "BooleanSegment.hh"
#include "Exception.hh"
#include "FloatSegment.hh"
#include "MessageFunctionObject.hh"
#include "Time.hh"
#include "VariableNodeDirector.hh"
#include "PoissonNeuronSpike.hh"

NRS::Simulator::PoissonNeuronSpike::PoissonNeuronSpike( std::string aName ) :
  Spike( aName ),
  iTReference( 0 ), iTimestepValuePtr( NULL ), iTimestepValue( 0 ),
  iEReference( 0 ), iEnableValuePtr( NULL ), iEnableValue( false ),
  iMReference( 0 ), iMeanSGValuePtr( NULL ), iMeanSGValue( 0 )
{
}

NRS::Simulator::PoissonNeuronSpike::~PoissonNeuronSpike()
{
}

void NRS::Simulator::PoissonNeuronSpike::addReaction( integer_t aReference )
{
  bool success = false;

  for (unsigned_t num = 0; num < iMFOPtr.size(); ++num )
    {
      if (aReference == iMFOPtr[num]->getReference())
	{
	  std::string aName = getParentName() + ".Timestep";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iTReference = aReference;
	      iTimestepValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".Timestep" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".MeanSG";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iMReference = aReference;
	      iMeanSGValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".MeanSG" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".enable";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iEReference = aReference;
	      iEnableValuePtr = &dynamic_cast< Type::BooleanSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".enable" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	}
    }
  if (!success)
    _ERROR_( "Cannot identify reaction from " << aReference );
}

void NRS::Simulator::PoissonNeuronSpike::react( integer_t aReference )
{
  if (aReference == iTReference)
    {
      _ASSERT_( iTimestepValuePtr != NULL, "illegal call from NULL" );
      iTimestepValue = *iTimestepValuePtr;
    }
  else if (aReference == iEReference)
    {
      _ASSERT_( iEnableValuePtr != NULL, "illegal call from NULL" );
      iEnableValue = *iEnableValuePtr;
    }
  else if (aReference == iMReference)
    {
      _ASSERT_( iMeanSGValuePtr != NULL, "illegal call from NULL" );
      iMeanSGValue = *iMeanSGValuePtr;
    }
  else
    _WARN_( "Unrecognised observable " << aReference );
}

void NRS::Simulator::PoissonNeuronSpike::doRemoveSource( integer_t aReference )
{
  if (aReference == iTReference)
    {
      iTimestepValuePtr = NULL;
      iTimestepValue = 0;
      iTReference = 0;
    }
  else if (aReference == iEReference)
    {
      iEnableValuePtr = NULL;
      iEnableValue = false;
      iEReference = 0;
    }
  else if (aReference == iMReference)
    {
      iMeanSGValuePtr = NULL;
      iMeanSGValue = 0;
      iMReference = 0;
    }
  else
    Type::Void::doRemoveSource( aReference );
}

void NRS::Simulator::PoissonNeuronSpike::doUpdate()
{
  if (iEnableValue && (drand48() < iTimestepValue / iMeanSGValue))
    iSendMessage = true;
}
