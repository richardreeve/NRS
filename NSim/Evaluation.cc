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
#include "Evaluation.hh"
#include "Exception.hh"
#include "MessageFunctionObject.hh"
#include "VariableNodeDirector.hh"

NRS::Simulator::Evaluation::Evaluation( std::string aName ) :
  Type::Float( aName ),
  iEReference( 0 ), iEnableValuePtr( NULL ), iEnableValue( false )
{
}

NRS::Simulator::Evaluation::~Evaluation()
{
}

void NRS::Simulator::Evaluation::addReaction( integer_t aReference )
{
  bool success = false;

  for (unsigned_t num = 0; num < iMFOPtr.size(); ++num )
    {
      if (aReference == iMFOPtr[num]->getReference())
	{
	  std::string aName = getParentName() + ".Trigger";
	  if (aName == iMFOPtr[num]->getSourceName())
	    {
	      iEReference = aReference;
	      iEnableValuePtr = &dynamic_cast< Type::BooleanSegment& >
		( Base::VariableNodeDirector::getDirector().
		  getVariable( iParentName + ".Trigger" )->getOutputLink().
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

void NRS::Simulator::Evaluation::react( integer_t aReference )
{
  if (aReference == iEReference)
    {
      _ASSERT_( iEnableValuePtr != NULL, "illegal call from NULL" );
      iEnableValue = *iEnableValuePtr;
      if (iEnableValue)
	{
	  iSendMessage = true;
	  update();
	  sendMessages();
	}
    }
  else
    _WARN_( "Unrecognised observable " << aReference );
}

void NRS::Simulator::Evaluation::doRemoveSource( integer_t aReference )
{
  if (aReference == iEReference)
    {
      iEnableValuePtr = NULL;
      iEnableValue = false;
      iEReference = 0;
    }
  else
    Type::Float::doRemoveSource( aReference );
}

void NRS::Simulator::Evaluation::doUpdate()
{
}
