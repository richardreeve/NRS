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

#include "FloatSegment.hh"
#include "MessageFunctionObject.hh"
#include "VariableNodeDirector.hh"
#include "SpikingNeuronSpike.hh"

NRS::Simulator::SpikingNeuronSpike::SpikingNeuronSpike( std::string aName ) : 
  Spike(aName),
  iVActualReference(0), iVActualValuePtr(NULL), iVActualValue(0),
  iThresholdReference(0), iThresholdValuePtr(NULL), iThresholdValue(0)
{
}

NRS::Simulator::SpikingNeuronSpike::~SpikingNeuronSpike()
{
}

void NRS::Simulator::SpikingNeuronSpike::addReaction( integer_t aReference )
{
  bool success = false;

  for (unsigned_t num = 0; num < iMFOPtr.size(); ++num )
    {
      if (aReference == iMFOPtr[num]->getReference())
	{
	  std::string aName = getParentName() + ".MembV";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iVActualReference = aReference;
	      iVActualValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".MembV" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".SpikeV";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iThresholdReference = aReference;
	      iThresholdValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".SpikeV" )->getOutputLink().
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

void NRS::Simulator::SpikingNeuronSpike::react( integer_t aReference)
{
  if (aReference == iVActualReference)
    {
      _ASSERT_( iVActualValuePtr != NULL, "illegal call from NULL" );
      iVActualValue = *iVActualValuePtr;
    }
  else if (aReference == iThresholdReference)
    {
      _ASSERT_( iThresholdValuePtr != NULL, "illegal call from NULL" );
      iThresholdValue = *iThresholdValuePtr;
    }
  else
    _WARN_( "Unrecognised observable " << aReference );
}

void NRS::Simulator::SpikingNeuronSpike::doRemoveSource(integer_t aReference)
{
  if (aReference == iVActualReference)
    {
      iVActualValuePtr = NULL;
      iVActualValue = 0;
      iVActualReference = 0;
    }
  else if (aReference == iThresholdReference)
    {
      iThresholdValuePtr = NULL;
      iThresholdValue = 0;
      iThresholdReference = 0;
    }
  else
    Type::Void::doRemoveSource( aReference );
}

void NRS::Simulator::SpikingNeuronSpike::doUpdate()
{
  if (iVActualValue >= iThresholdValue)
    iSendMessage = true;
}


