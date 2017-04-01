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

#ifndef _LOCATION_HH
#define _LOCATION_HH

#pragma interface
#include <string>

#include "MessageStore.hh"
#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    /// Class for locations of links, also derived to form targets as well
    class Location
    {
    public:
      /// Constructor for locations
      /**
       *
       * \param theCID the CID of the target component
       * \param theRoute the route to the target
       * \param theVNID the VNID of the target
       * \param sourceNotTarget whether the location being pointed to
       * is a source or a target
       *
       **/
      Location( const std::string &theCID, const route_t &theRoute,
		integer_t theVNID, bool sourceNotTarget = true );

      /// Default copy constructor
      Location( const Location &rhs )
      {
	*this = rhs;
      }

      /// Default constructor for containers
      Location() : iRoute( getEmptyRoute() ), iVNID( 0 ),
		   iSourceNotTarget( true )
      {
      }

      /// Empty Destructor
      virtual ~Location()
      {
      }

      /// Is the destination local?
      /**
       *
       * \return whether the Destination is in the same thread
       *
       **/
      bool isLocal() const;

      /// Get cid of target component
      /**
       *
       * \return cid of target component
       *
       **/
      const std::string &getCID() const
      {
	return iCID;
      }

      /// Get route to target
      /**
       *
       * \returns const reference to route to target
       *
       **/
      const route_t &getRoute() const
      {
	return iRoute;
      }

      /// Get vnid of target
      /**
       *
       * \return vnid of target
       *
       **/
      integer_t getVNID() const
      {
	return iVNID;
      }

      /// Do the locations match?
      /**
       *
       * \param rhs Location to match
       *
       * \return whether the objects match
       *
       **/
      bool operator==( const Location &rhs ) const;

      /// Do the locations differ?
      /**
       *
       * \param rhs Location to compare with
       *
       * \return whether the objects differ
       *
       **/
      bool operator!=( const Location &rhs ) const
      {
	return !(*this == rhs);
      }

    protected:
      /// cid of target component
      std::string iCID;

      /// route to component
      route_t iRoute;

      /// vnid of target
      integer_t iVNID;

      /// Is this the source or target of a link?
      bool iSourceNotTarget;
    };
  }
}

#endif //ndef _LOCATION_HH
