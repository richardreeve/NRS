/*
 * Copyright (C) 2005 Richard Reeve and Edinburgh University
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

#ifndef _STRING_RESTRICTION_HH
#define _STRING_RESTRICTION_HH

#pragma interface
#include <memory>
#include <iostream>
#include <string>
#include <set>

#include "Exception.hh"
#include "Types.hh"
#include "Restriction.hh"
#include "StringLiterals.hh"


namespace NRS
{
  namespace Type
  {
    /// This is the base class for restrictions
    class StringRestriction : public Base::Restriction
    {
    public:
      typedef std::set< std::string >::const_iterator ListMemberConstIter;

      /// Default constructor
      StringRestriction( Enum::StringRestrictionType type =
			 Enum::NoStringRestriction );

      /// Destructor
      ~StringRestriction()
      {
      }
      
      /// Add a list member
      /**
       *
       * \param member a member of the list restriction
       *
       **/
      void addListMember( std::string member )
      {
	_ASSERT_( getType() == Enum::String,
		  "Trying to add a list member to a non-list restriction" );
	iListMemberSet.insert( member );
      }

      /// Is this a member of the list?
      /**
       *
       * \return whether this element is a member of the allowed list
       *
       **/
      bool isListmember( const std::string &member ) const
      {
	_ASSERT_( getType() == Enum::String,
		  "Trying to get a list member from a non-list restriction" );
	return (iListMemberSet.find( member ) != iListMemberSet.end());
      }

      /// Get list member begin iterator
      /**
       *
       * \return begin() iterator of list members
       *
       **/
      ListMemberConstIter getListBegin() const
      {
	_ASSERT_( getType() == Enum::String,
		  "Trying to get a list member from a non-list restriction" );
	return iListMemberSet.begin();
      }

      /// Get list member end iterator
      /**
       *
       * \return end() iterator of list members
       *
       **/
      ListMemberConstIter getListEnd() const
      {
	_ASSERT_( getType() == Enum::String,
		  "Trying to get a list member from a non-list restriction" );
	return iListMemberSet.end();
      }

      /// Get string restriction type
      /**
       *
       * \return the string restriction type
       *
       **/
      Enum::StringRestrictionType getStringType() const
      {
	return iStringType;

      }

      /// Clone the restriction, pass over ownership to caller
      /**
       *
       * \return a copy of the restriction (not necessarily the base
       * class, otherwise we could just use the copy constructor)
       *
       **/
      virtual Restriction *clone() const;

      /// get CSL for restriction
      /**
       *
       * \param csl the output stream to which the CSL description
       * should be output.
       *
       * \return the output stream to which the CSL description was
       * output.
       *
       **/
      virtual std::ostream &getCSL( std::ostream& csl ) const;

    private:
      /// Type of string restriction
      Enum::StringRestrictionType iStringType;

      /// List of possible entries for string restrictions
      std::set< std::string > iListMemberSet;
    };
  }
}

#endif //ndef _STRING_RESTRICTION_HH
