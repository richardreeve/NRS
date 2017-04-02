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
#include "MessageFunctionObject.hh"
#include "Time.hh"
#include "VariableNodeDirector.hh"
#include "TimeChecker.hh"

NRS::Simulator::TimeChecker::TimeChecker( std::string aName, bool invert ) :
  Type::Boolean( aName ), iInvert( invert ),
  iSTReference( 0 ), iSimTimeValuePtr( NULL ), iSimTimeValue( 0 ),
  iETReference( 0 ), iEndTimeValuePtr( NULL ), iEndTimeValue( 0 )
{
}

NRS::Simulator::TimeChecker::~TimeChecker()
{
}

void NRS::Simulator::TimeChecker::addReaction( integer_t aReference )
{
  bool success = false;

  for (unsigned_t num = 0; num < iMFOPtr.size(); ++num )
    {
      if (aReference == iMFOPtr[num]->getReference())
	{
	  std::string aName = getParentName() + ".SimTime";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iSTReference = aReference;
	      iSimTimeValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".SimTime" )->getOutputLink().
		  getSegment( 0 ) ).
		iValue;
	      _DEBUG_( "Added " << aName );
	      success = true;
	    }
	  aName = getParentName() + ".EndTime";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iETReference = aReference;
	      iEndTimeValuePtr = &dynamic_cast< Type::FloatSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".EndTime" )->getOutputLink().
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

void NRS::Simulator::TimeChecker::react( integer_t aReference )
{
  if (aReference == iSTReference)
    {
      _ASSERT_( iSimTimeValuePtr != NULL, "illegal call from NULL" );
      iSimTimeValue = *iSimTimeValuePtr;
      doUpdate();
    }
  else if (aReference == iETReference)
    {
      _ASSERT_( iEndTimeValuePtr != NULL, "illegal call from NULL" );
      iEndTimeValue = *iEndTimeValuePtr;
      doUpdate();
    }
  else
    _WARN_( "Unrecognised observable " << aReference );
}

void NRS::Simulator::TimeChecker::doRemoveSource( integer_t aReference )
{
  if (aReference == iSTReference)
    {
      iSimTimeValuePtr = NULL;
      iSimTimeValue = 0;
      iSTReference = 0;
    }
  else if (aReference == iETReference)
    {
      iEndTimeValuePtr = NULL;
      iEndTimeValue = 0;
      iETReference = 0;
    }
  else
    Type::Boolean::doRemoveSource( aReference );
}

void NRS::Simulator::TimeChecker::doReset()
{
  doUpdate();
}

void NRS::Simulator::TimeChecker::doUpdate()
{
  bool newValue = (iSimTimeValue < iEndTimeValue) ^ iInvert;
  if (iValue != newValue)
    {
      iValue = newValue;
      iSendMessage = true;
    }
}
