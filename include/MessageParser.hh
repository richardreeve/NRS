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

#ifndef _MESSAGE_PARSER_HH
#define _MESSAGE_PARSER_HH

#include "MessageStore.hh"
#include "RouteSegment.hh"
#include "Target.hh"
#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    class IntelligentStore;
  }

  namespace Interface
  {
    /// This is the Message Parser class
    class MessageParser
    {
    public:
      /// Constructor
      /**
       *
       * \param port the port number of the interface
       *
       **/
      MessageParser() : iPort( 0 )
      {
      }

      /// Dummy destructor
      virtual ~MessageParser()
      {
      }

      /// Set port number
      /**
       *
       * \param port port number of parser
       *
       **/
      void setPort( unsigned_t port );

      /// Parse a new buffer
      /**
       *
       * \param aBuffer a buffer to parse
       *
       * \returns whether the buffer could be parsed
       *
       **/
      virtual bool parseNew( uint8_t* aBuffer ) = 0;
      
      /// Do next parse of existing buffer
      /**
       *
       * \returns whether the parse could be done
       *
       **/
      virtual bool parseAgain() = 0;

      /// Encode a message into a data object
      /**
       *
       * This method tells the Parser to create a message and return it
       *
       * \param data store to put encoded message into
       * \param aTarget target to send message to
       * \param aMessageStore data to send
       * \param intelligentPtr intelligent parts if present
       *
       **/
      virtual void createOutput( data_t &data,
				 Base::Target &aTarget, 
				 const Base::MessageStore &aMessageStore,
				 const Base::IntelligentStore *intelligentPtr )
	= 0;

      /// Add a port to the head of a route
      /**
       * This method is intended for creating a reverse route
       *
       * \param seg the segment containing the route so far
       *
       **/
      void routePushPortFront( Type::RouteSegment &seg );

      /// Add a port to the end of a route
      /**
       * This method is intended for creating a forward route
       *
       * \param seg the segment containing the route so far
       *
       **/
      void routePushPortEnd( Type::RouteSegment &seg );


    protected:
      /// Port number of interface
      unsigned_t iPort;
    };
  }
}
#endif //ndef _MESSAGE_PARSER_HH
