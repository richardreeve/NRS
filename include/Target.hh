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

#ifndef _TARGET_HH
#define _TARGET_HH

#pragma interface

#include <string>
#include <vector>

#include "MessageDescription.hh"
#include "Location.hh"
#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    class SegmentDescription;

    /// This class contains information for targets of links
    class Target : public Location
    {
    public:
      /// Default constructor
      /**
       *
       * \param theCID the CID of the target component
       * \param theRoute the route to the target
       * \param theVNID the VNID of the target
       * \param theMDPtr pointer to the MessageDescription for the message type
       *
       **/
      Target( const std::string &theCID, const route_t &theRoute,
	      integer_t theVNID, const MessageDescription *theMDPtr );

      /// Constructor from source
      /**
       *
       * \param theLocation Location for message
       * \param theMDPtr pointer to the MessageDescription for the message type
       *
       **/
      Target( const Location &theLocation,
	      const MessageDescription *theMDPtr );

      /// Empty Destructor
      virtual ~Target()
      {
      }

      /// Get VariableManager
      /**
       *
       * \return const reference to the variable manager
       *
       **/
       const MessageDescription *getMessageDescriptionPtr() const
       {
 	return iMDPtr;
       }

      /// Get port number from route
      /**
       *
       * \return port number acquired from route (if present)
       *
       **/
      const unsigned_t getPort() const
      {
	return iPort;
      }

      /// Send a delete link message to the other end of this connection
      /**
       *
       * \param aVNID vnid of this end of the link
       *
       **/
      void deleteLink( integer_t aVNID ) const;

      /// Get message store
      /**
       * 
       * warning - not const or thread safe, could easily be made so
       * in exchange for storage of each required message type.
       *
       * \param encoding required encoding for message
       *
       * \return a const reference to the required encoding. If this
       * is the cached encoding, then this will be very simple, but if
       * not, then it will be considerably slower. Consequently,
       * broadcasts are slow transmissions. This is deliberate as they
       * should not be required at runtime.
       *
       **/
      const MessageStore &getMessageStore( Enum::EncodingType anEncoding );

       private:
      /// VariableManager reference
      const MessageDescription *iMDPtr;

      /// Port number from route
      unsigned_t iPort;

      /// MessageStore containing encoded route and vnid
      MessageStore iMessageStore;
    };
  }
}

#endif //ndef _TARGET_HH
