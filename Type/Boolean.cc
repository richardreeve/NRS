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

#include <list>
#include <string>

#include "Boolean.hh"
#include "BooleanManager.hh"
#include "BooleanSegment.hh"
#include "Link.hh"
#include "Location.hh"
#include "StringLiterals.hh"
#include "VariableNodeDirector.hh"

NRS::Type::Boolean::Boolean( std::string aName ) :
  Base::Variable( aName, Base::VariableNodeDirector::getDirector().
		  getVariableManager( Literals::Type::
				      getTypeName( Enum::Boolean ) ) ),
  iValue( dynamic_cast< BooleanSegment& >( iOutputLink.
					   getSegment( 0 ) ).iValue ),
  iDefault( false )
{
  iInput.resize( 1 );
  iInput[0] = 
    &dynamic_cast< BooleanSegment& >( getInputLink().
				      getSegment( 0 ) ).iValue;
}

NRS::Type::Boolean::Boolean( std::string aName,
			     const Base::VariableManager &theVMRef ) :
  Base::Variable( aName, theVMRef ),
  iValue( dynamic_cast< BooleanSegment& >( iOutputLink.
					   getSegment( 0 ) ).iValue ),
  iDefault( false )
{
  iInput.resize( 1 );
  iInput[0] = 
    &dynamic_cast< BooleanSegment& >( getInputLink().
				      getSegment( 0 ) ).iValue;
}

NRS::Type::Boolean::~Boolean()
{
}

void NRS::Type::Boolean::setDefault( bool aDefault )
{
  iDefault = aDefault;
  reset();
}

void NRS::Type::Boolean::doAddSource( const Base::Location &aSourceRef,
				      integer_t aRef )
{
  if ((integer_t) iInput.size() <= aRef)
    {
      iInput.resize( aRef + 1 );
    }
  if (aSourceRef.isLocal())
    iInput[aRef] =
      &dynamic_cast< BooleanSegment& >( Base::VariableNodeDirector::
					getDirector().
					getVariable( aSourceRef.getVNID() )->
					getOutputLink().
					getSegment( 0 ) ).iValue;
}

void NRS::Type::Boolean::doRemoveSource( integer_t aRef )
{
  if (aRef > 0)
    {
      // Only remove pointer if local
      iInput[aRef] = NULL;
    }
}

void NRS::Type::Boolean::doReset()
{
  if (iInputLink.getNumInputs() == 0)
    { // No inputs to override defaults
      iValue = iDefault;
    }
}

void NRS::Type::Boolean::doObserve( integer_t aRef )
{
  if (!iStateHolding || (iValue != *iInput[aRef]))
    {
      iSendMessage = true;
      iValue = *iInput[aRef];
    }
}

