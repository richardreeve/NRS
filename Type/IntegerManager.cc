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

#include <string>

#include "IntegerManager.hh"
#include "IntegerSegment.hh"
#include "Integer.hh"
#include "VariableManager.hh"
#include "Types.hh"
#include "SegmentDescription.hh"
#include "StringLiterals.hh"

namespace
{
  NRS::Type::IntegerManager sIntegerM;
}

NRS::Type::IntegerManager::IntegerManager() : 
  Base::VariableManager( Enum::Integer )
{
  iDescription = "The built-in type for messages with one integer value";
  iSegmentDescription[0].setType( Enum::Integer );
}

NRS::Type::IntegerManager::IntegerManager( std::string aMessageName,
					   std::string aDisplayName,
					   bool isUnit) :
  Base::VariableManager( aMessageName, aDisplayName, 1, isUnit, Enum::Integer )
{
  iParentName = Literals::Type::getTypeName( iRootType );
}


NRS::Base::Segment *
NRS::Type::IntegerManager::
createSegment( const Base::SegmentDescription &aSDRef ) const
{
  return new IntegerSegment();
}

void 
NRS::Type::IntegerManager::createVariable( const std::string &aName ) const
{
  new Integer( aName );
}
