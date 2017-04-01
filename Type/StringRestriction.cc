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
#include "StringRestriction.hh"

NRS::Type::StringRestriction::
StringRestriction( Enum::StringRestrictionType type ) :
  Restriction( Enum::String ), iStringType( type )
{}

NRS::Base::Restriction *NRS::Type::StringRestriction::clone() const
{
  return new StringRestriction( *this );
}

std::ostream &NRS::Type::StringRestriction::getCSL( std::ostream& csl ) const
{
  if ( iStringType != Enum::NoStringRestriction )
    {
      csl << "<StringInfo restriction='" 
	  << Literals::String::getRestrictionName( iStringType )
	  << "' ";
      if (iStringType == Enum::List)
	{
	  csl << ">" << std::endl;
	  for ( ListMemberConstIter iter = iListMemberSet.begin();
		iter != iListMemberSet.end(); iter++ )
	    {
	      csl << " <ListMember value='"
		  << *iter << "' />" << std::endl;
	    }
	  csl << "</StringInfo>" << std::endl;
	}
      else
	csl << "/>" << std::endl;
	  }
  return csl;
}
