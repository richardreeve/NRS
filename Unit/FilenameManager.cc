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

#include "Filename.hh"
#include "FilenameManager.hh"
#include "StringLiterals.hh"
#include "StringManager.hh"
#include "StringRestriction.hh"
#include "Types.hh"

namespace
{
  NRS::Unit::FilenameManager sFilenameM;
}

NRS::Unit::FilenameManager::FilenameManager( std::string aMessageName,
					     std::string aDisplayName ) :
  Type::StringManager( aMessageName, aDisplayName, false )
{
}

NRS::Unit::FilenameManager::FilenameManager() : 
  NRS::Type::StringManager( Literals::String::FILENAME(), "", true )
{
  iDescription = "A string restricted to an alphanumeric sequence of "
    "characters which can also contain any valid characters for a filename.";
  iSegmentDescription[0].setType( Enum::String, Literals::String::FILENAME() );
  iSegmentDescription[0].
    setRestriction( new Type::StringRestriction( Enum::Filename ) );
}

void 
NRS::Unit::FilenameManager::createVariable( const std::string &aName ) const
{
  new Filename( aName );
}
