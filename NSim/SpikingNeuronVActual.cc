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

#include <cmath>

#include "SpikingNeuronVActual.hh"
#include "FloatSegment.hh"
#include "MessageFunctionObject.hh"
#include "VariableNodeDirector.hh"


NRS::Simulator::SpikingNeuronVActual::SpikingNeuronVActual(std::string aName):
  Unit::Voltage( aName ), 
  iTimestepReference(0),iTimestepValuePtr(NULL),iTimestepValue(0),
  iRefractoryReference(0),iRefractoryValuePtr(NULL),iRefractoryValue(0),
  iBaseVReference(0),iBaseVValuePtr(NULL),iBaseVValue(0),
  iSpikeVReference(0),iSpikeVValuePtr(NULL),iSpikeVValue(0),
  iResetVReference(0),iResetVValuePtr(NULL),iResetVValue(0),
  iBaseGReference(0),iBaseGValuePtr(NULL),iBaseGValue(0),
  iMembCReference(0),iMembCValuePtr(NULL),iMembCValue(0),
  iGTotalReference(0),iGTotalValuePtr(NULL),iGTotalValue(0),
  iITotalReference(0),iITotalValuePtr(NULL),iITotalValue(0),
  iDecayValue ( 0 ), iVTargetValue ( 0 ), 
  iInRefractoryPeriod ( false ), iTimeInTRefract(0)
{
}

NRS::Simulator::SpikingNeuronVActual::~SpikingNeuronVActual()
{
}
      
void NRS::Simulator::SpikingNeuronVActual::addReaction( integer_t aReference )
{
  bool success = false;

  for (unsigned_t num = 0; num < iMFOPtr.size(); ++num )
    {
      if (aReference == iMFOPtr[num]->getReference())
	{
	  std::string aName = getParentName() + ".Timestep";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iTimestepReference = aReference;
	      iTimestepValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".Timestep" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".RefractoryPeriod";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iRefractoryReference = aReference;
	      iRefractoryValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".RefractoryPeriod" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".BaseV";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iBaseVReference = aReference;
	      iBaseVValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".BaseV" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".SpikeV";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iSpikeVReference = aReference;
	      iSpikeVValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".SpikeV" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".ResetV";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iResetVReference = aReference;
	      iResetVValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".ResetV" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".BaseG";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iBaseGReference = aReference;
	      iBaseGValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".BaseG" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".MembC";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iMembCReference = aReference;
	      iMembCValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".MembC" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".GTotal";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iGTotalReference = aReference;
	      iGTotalValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".GTotal" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".ITotal";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iITotalReference = aReference;
	      iITotalValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".ITotal" )->getOutputLink().
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

void NRS::Simulator::SpikingNeuronVActual::react( integer_t aReference )
{
  _DEBUG_( "***react*** ");
  if (aReference == iTimestepReference)
    {
      _ASSERT_( iTimestepValuePtr != NULL, "illegal call from NULL" );
      iTimestepValue = *iTimestepValuePtr;
      if (iMembCValue != 0)
	{
	  iDecayValue = exp (-(iTimestepValue/1000) * iGTotalValue /
			     iMembCValue);
	}
    }
  else if (aReference == iRefractoryReference)
    {
      _ASSERT_( iRefractoryValuePtr != NULL, "illegal call from NULL" );
      iRefractoryValue = *iRefractoryValuePtr;
    }
  else if (aReference == iBaseVReference)
    {
      // Original code doesn't observe BaseV. Should this just be 
      // an initialisation value?
      _ASSERT_( iBaseVValuePtr != NULL, "illegal call from NULL" );
      iBaseVValue = *iBaseVValuePtr;
    }
  else if (aReference == iSpikeVReference)
    {
      _ASSERT_( iSpikeVValuePtr != NULL, "illegal call from NULL" );
      iSpikeVValue = *iSpikeVValuePtr;
    }
  else if (aReference == iResetVReference)
    {
      _ASSERT_( iResetVValuePtr != NULL, "illegal call from NULL" );
      iResetVValue = *iResetVValuePtr;
    }
  else if (aReference == iBaseGReference)
    {
      // Maybe don't even need this
      _ASSERT_( iBaseGValuePtr != NULL, "illegal call from NULL" );
      iBaseGValue = *iBaseGValuePtr;
    }
  else if (aReference == iMembCReference)
    {
      _ASSERT_( iMembCValuePtr != NULL, "illegal call from NULL" );
      iMembCValue = *iMembCValuePtr;
      if (iMembCValue != 0)
	{
	  iDecayValue = exp (-(iTimestepValue/1000) * iGTotalValue /
			     iMembCValue);
	}
    }
  else if (aReference == iGTotalReference)
    {
      _ASSERT_( iGTotalValuePtr != NULL, "illegal call from NULL" );
      iGTotalValue = *iGTotalValuePtr;
      iVTargetValue = iITotalValue / iGTotalValue;
      if (iMembCValue != 0)
	{
	  iDecayValue = exp (-(iTimestepValue/1000) * iGTotalValue /
			     iMembCValue);
	}
    }
  else if (aReference == iITotalReference)
    {
      _ASSERT_( iITotalValuePtr != NULL, "illegal call from NULL" );
      iITotalValue = *iITotalValuePtr;
      iVTargetValue = iITotalValue / iGTotalValue;
    }
  else
    _WARN_( "Unrecognised observable " << aReference );
}



void NRS::Simulator::SpikingNeuronVActual::doRemoveSource( integer_t aReference )
{
  if (aReference == iTimestepReference)
    {
      iTimestepValuePtr = NULL;
      iTimestepValue = 0;
      iTimestepReference = 0;
    }
  else if (aReference == iRefractoryReference)
    {
      iRefractoryValuePtr = NULL;
      iRefractoryValue = 0;
      iRefractoryReference = 0;
    }
  else if (aReference == iBaseVReference)
    {
      iBaseVValuePtr = NULL;
      iBaseVValue = 0;
      iBaseVReference = 0;
    }
  else if (aReference == iSpikeVReference)
    {
      iSpikeVValuePtr = NULL;
      iSpikeVValue = 0;
      iSpikeVReference = 0;
    }
  else if (aReference == iResetVReference)
    {
      iResetVValuePtr = NULL;
      iResetVValue = 0;
      iResetVReference = 0;
    }
  else if (aReference == iBaseGReference)
    {
      iBaseGValuePtr = NULL;
      iBaseGValue = 0;
      iBaseGReference = 0;
    }
  else if (aReference == iMembCReference)
    {
      iMembCValuePtr = NULL;
      iMembCValue = 0;
      iMembCReference = 0;
    }
  else if (aReference == iGTotalReference)
    {
      iGTotalValuePtr = NULL;
      iGTotalValue = 0;
      iGTotalReference = 0;
    }
  else if (aReference == iITotalReference)
    {
      iITotalValuePtr = NULL;
      iITotalValue = 0;
      iITotalReference = 0;
    }
  else
    Type::Float::doRemoveSource( aReference );
}    

  
void NRS::Simulator::SpikingNeuronVActual::doUpdate()
{
  if (iInRefractoryPeriod)
    {
      _DEBUG_( "In refractory period");
      iTimeInTRefract += iTimestepValue;
      if (iTimeInTRefract > iRefractoryValue)
	iInRefractoryPeriod = false;		 
    }

  if (!iInRefractoryPeriod) 
    {
      _DEBUG_( "Not in refractory period");
      if (iValue > iSpikeVValue) // a spike has occured
	{
	  iValue = iResetVValue;
	  iInRefractoryPeriod = true;
	  iTimeInTRefract = 0;
	}
      else
	{
	  _DEBUG_( "DecayValue=" << iDecayValue );
	  _DEBUG_( "TimestepValue=" << iTimestepValue );
	  _DEBUG_( "GTotalValue=" << iGTotalValue );
	  _DEBUG_( "MembCValue=" << iMembCValue );

	  _DEBUG_( "TargetValue=" << iVTargetValue );
	  _DEBUG_( "ITotalValue=" << iITotalValue );
	  if ((iDecayValue < 1) && (iVTargetValue != 0))
	    iValue = iValue - ((iValue - iVTargetValue) * 
			       (1.0 - iDecayValue));
	}
      iSendMessage = true;
    }
  
}

/// Reset the system to its initial state
/*virtual void reset()
  {
  if (iLocalSourceList.empty() && iDistalSourceList.empty())
  if (iValue != iDefault)
  {
  iValue = iDefault;
  }
  }*/
