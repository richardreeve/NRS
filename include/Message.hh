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

#ifndef _MESSAGE_HH
#define _MESSAGE_HH

#pragma interface

#include <vector>

#include "IntelligentStore.hh"
#include "Target.hh"
#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    class Encoding;
    class ExternalInterface;

    /// The Message class stores a target for a link, along with
    /// references to the segment types and names, and to the
    /// MessageStore where data is stored in formats suitable for
    /// sending out, and the ExternalInterfaces to send the messages
    /// to.
    class Message
    {
    public:
      /// Default constructor
      /**
       *
       * \param aTarget the target to which to connect
       *
       **/
      Message( const Target &aTarget );

      /// Default destructor
      ~Message();

      /// Is the destination local?
      /**
       *
       * \return whether the Destination is in the same thread
       *
       **/
      bool isLocal()
      {
	return iIsLocal;
      }

      /// Gets target
      /**
       *
       * \return target of Message
       *
       **/
      const Target &getTarget() const
      {
	return iTarget;
      }

      /// Sets the location of encoded segments for this message
      /**
       *
       * \param theEncoding pointer to the encoding of segments
       *
       **/
      void setEncodingStore( Encoding* theEncodingStorePtr );

      /// Create your own local encoding store
      void createEncodingStore();

      /// Send outbound messages
      void sendMessages();

    private:
      /// No default constructor
      Message();

      /// No copy constructor
      Message( const Message & );
      
      /// Whether the destination is local
      bool iIsLocal;

      /// encoding types required for this target
      encoding_t iEncodings;

      /// pointer to where encoded segments are stored
      Encoding *iEncodingPtr;

      /// Does the Message own the encoding?
      bool iOwnEncoding;

      /// Target for the message
      Target iTarget;

      /// Number of ports for route
      unsigned_t iNumPorts;

      /// Port List
      std::vector< ExternalInterface* > iPortPtr;

      /// List of Port Encodings
      std::vector< Enum::EncodingType > iPortEncoding;
    };
  }
}

#endif //ndef _MESSAGE_HH
