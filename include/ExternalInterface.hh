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

#ifndef _EXTERNAL_INTERFACE_HH
#define _EXTERNAL_INTERFACE_HH

#include <string>

#include "MessageParser.hh"
#include "MessageStore.hh"
#include "Target.hh"
#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    class IntelligentStore;

    /// This is the base class for external interfaces.
    class ExternalInterface
    {
    public:
      /// Constructor.
      /**
       *
       * \param aConnectionType The type of interface connection.
       * \param anEncodingType The type of encoding being sent (PML, BMF).
       * \param aParserPtr pointer to parser for encoding type
       * \param doRead Is the interface an input interface?
       * \param doWrite Is the interface an output interface?
       * \param instantTransmit Does transmission happen instantly, or
       * does it wait for the update step? 
       * \param receiveAll Does the input read in as much as it can,
       * or is there a maximum?
       * \param maxReceive If receiveAll is false what is the maximum
       * number of messages to read in.
       *
       **/
      ExternalInterface( Enum::ConnectionType aConnectionType,
			 Enum::EncodingType anEncodingType,
			 Interface::MessageParser *aParserPtr,
			 bool doRead, bool doWrite,
			 bool instantTransmit, bool receiveAll = true,
			 int maxReceive = 0 );
      
      /// Destructor.
      virtual ~ExternalInterface();
      
      /// Get the connection type of interface.
      /**
       *
       * \return the connection type of the interface.
       *
       **/
      Enum::ConnectionType getConnectionType() const
      {
	return iConnectionType;
      }

      /// Is the connection type PML and not BMF
      /**
       *
       * \returns true if PML and false if BMF
       *
       */
      Enum::EncodingType getEncoding() const
      {
	return iEncodingType;
      }

      /// Get port id of interface.
      /**
       *
       * \return the port id of the interface.
       *
       **/
      unsigned_t getPort() const
      {
	return iPort;
      }

      /// Get route to the port
      /**
       *
       * \return the route to the port
       *
       **/
      const route_t &getPortRoute() const
      {
	return iPortRoute;
      }

      /// Do an update, sending and receiving any outstanding data
      /**
       *
       * \return whether the connection is still open
       *
       **/
      virtual bool update() = 0;

      /// Send a message
      /**
       *
       * This method tells the Interface to transmit a message.
       *
       * \param aTarget target to send message to
       * \param aMessageStore data to send
       * \param intelligentPtr intelligent parts if present
       *
       **/
      virtual void sendMessage( Target &aTarget, 
				const MessageStore &aMessageStore,
				const IntelligentStore *intelligentPtr ) = 0;

      /// Set the port to be a logging port
      /**
       *
       * This has the effect of registering the port with the External
       * interface director as a logging port, so that it will be
       * reported in any ReplyLog replies, and that broadcasts will no
       * longer be sent down this port.
       *
       **/
      void setLoggingPort();

      /// Returns whether the port is logging
      bool isLoggingPort() const
      {
	return iIsLogging;
      }

    protected:
      /// the connection type of the interface.
      Enum::ConnectionType iConnectionType;
      
      /// the message type of the interface.
      Enum::EncodingType iEncodingType;
      
      /// pointer to the parser
      std::auto_ptr< Interface::MessageParser > iParserPtr;

      /// the port number of the interface.
      unsigned_t iPort;

      /// The route for the port
      route_t iPortRoute;

      /// Is the interface an input interface?
      bool iRead;

      /// Is the interface an output interface?
      bool iWrite;

      /// Does transmission happen instantly, or does it wait for the
      /// update step?
      bool iInstantTransmit;

      /// Does the input read in as much as it can, or is there a maximum?
      bool iReceiveAll;

      /// If iReceiveAll is false what is the maximum number of messages to
      /// read in.
      int iMaxReceive;

      /// Is the port a logging port?
      bool iIsLogging;
    };
  }
}
#endif //ndef _EXTERNAL_INTERFACE_HH
