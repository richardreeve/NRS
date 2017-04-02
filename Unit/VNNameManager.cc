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

#include "StringRestriction.hh"
#include "Types.hh"
#include "VNName.hh"
#include "VNNameManager.hh"

namespace
{
  NRS::Unit::VNNameManager sVNNameM;
}

NRS::Unit::VNNameManager::VNNameManager( std::string aMessageName,
					 std::string aDisplayName ) :
  Type::StringManager( aMessageName, aDisplayName, false )
{
}

NRS::Unit::VNNameManager::VNNameManager() : 
  NRS::Type::StringManager( Literals::String::VNNAME(), "", true )
{
  iDescription = "A string restricted to a sequence of tokens "
    "separated by full stops, used for variable and node names.";
  iSegmentDescription[0].setType( Enum::String, Literals::String::VNNAME() );
  iSegmentDescription[0].
    setRestriction( new Type::StringRestriction( Enum::VNName ) );
}

void 
NRS::Unit::VNNameManager::createVariable( const std::string &aName ) const
{
  new VNName( aName );
}
