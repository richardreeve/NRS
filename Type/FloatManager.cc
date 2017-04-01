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

#pragma implementation
#include "FloatManager.hh"
#include "FloatSegment.hh"
#include "Float.hh"
#include "VariableManager.hh"
#include "Types.hh"
#include "SegmentDescription.hh"
#include "StringLiterals.hh"

namespace
{
  NRS::Type::FloatManager sFloatM;
}

NRS::Type::FloatManager::FloatManager() : 
  Base::VariableManager( Enum::Float )
{
  iDescription="The built-in type for messages with one floating point value";
  iSegmentDescription[0].setType( Enum::Float );
}

NRS::Type::FloatManager::FloatManager( std::string aMessageName,
				       std::string aDisplayName,
				       bool isUnit ) :
  Base::VariableManager( aMessageName, aDisplayName, 1, isUnit, Enum::Float )
{
  iParentName = Literals::Type::getTypeName( iRootType );
}


NRS::Base::Segment *
NRS::Type::FloatManager::
createSegment( const Base::SegmentDescription &aSDRef ) const
{
  return new FloatSegment();
}

void 
NRS::Type::FloatManager::createVariable( const std::string &aName ) const
{
  new Float( aName );
}
