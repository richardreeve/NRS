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

#ifndef _RESTRICTION_HH
#define _RESTRICTION_HH

#include <iostream>
#include <string>

#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    /// This is the base class for restrictions
    class Restriction
    {
    public:
      /// Default constructor
      /**
       *
       * \param aType the base type of segment to which the
       * restriction applies
       *
       **/
      Restriction( Enum::Type aType = Enum::NoType );

      /// Destructor
      virtual ~Restriction()
      {
      }

      /// Get type
      /**
       *
       * \return type of segment
       *
       **/
      Enum::Type getType() const
      {
	return iType;
      }
      
      /// Clone the restriction, pass over ownership to caller
      /**
       *
       * \return a copy of the restriction (not necessarily the base
       * class, otherwise we could just use the copy constructor)
       *
       **/
      virtual Restriction *clone() const = 0;

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
      virtual std::ostream &getCSL( std::ostream& csl ) const = 0;

    private:
      /// The basic type of the segment
      Enum::Type iType;
    };
  }
}

#endif //ndef _RESTRICTION_HH
