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

#ifndef _LIMIT_HH
#define _LIMIT_HH

#include <iostream>
#include <string>

#include "Exception.hh"
#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    /// This is a trivial class to contain a potential limit
    template < typename Value > class Limit
    {
    public:
      /// Default constructor
      Limit() :	iHasLimit( false )
      {}

      /// Constructor for case where limit exists
      /**
       *
       * \param limit the limit
       *
       **/
      Limit( Value limit ) : iHasLimit ( true ), iLimit( limit )
      {}

      /// Destructor
      ~Limit()
      {}

      /// Does the limit exist?
      /**
       *
       * \return whether the limit exists
       *
       **/
      bool hasLimit() const
      {
	return iHasLimit;
      }

      /// What is the limit?
      /**
       *
       * \return the limit if it exists
       *
       **/
      Value get() const
      {
	return iLimit;
      }
      
      /// set the limit
      /**
       *
       * \param limit the limit
       *
       **/
      void set( Value limit )
      {
	iLimit = limit;
	iHasLimit = true;
      }

      /// clears the limit
      void clear()
      {
	iHasLimit = false;
      }

    private:
      /// Does the limit exist?
      bool iHasLimit;

      /// The limit if it exists
      Value iLimit;
    };
  }
}

#endif //ndef _LIMIT_HH
