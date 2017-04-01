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

#ifndef _BUFFERED_INTERFACE_HH
#define _BUFFERED_INTERFACE_HH

#pragma interface
#include <map>

#include "Callback.hh"
#include "ExternalInterface.hh"
#include "IntelligentStore.hh"
#include "MessageStore.hh"
#include "Target.hh"
#include "Types.hh"

namespace NRS
{
  namespace Interface
  {
    class MessageParser;

    /// This class instantiates buffered readers and writers
    class BufferedInterface : public Base::ExternalInterface,
                              public Base::Callback
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
       * \param initBufferSize initial buffer size
       *
       **/
      BufferedInterface( Enum::ConnectionType aConnectionType,
			 Enum::EncodingType anEncodingType,
			 Interface::MessageParser *aParserPtr,
			 bool doRead, bool doWrite,
			 bool instantTransmit, bool receiveAll,
			 int maxReceive, size_t initBufferSize );
      
      /// Destructor.
      virtual ~BufferedInterface();
      
      /// Do an update, sending and receiving any outstanding data
      /**
       *
       * \return whether the connection is still open
       *
       **/
      virtual bool update();

      /// Check for new input
      /**
       *
       * \return whether the connection is still open
       *
       **/
      virtual bool pollInput() = 0;

      /// Flush any output
      /**
       *
       * \return whether the connection is still open
       *
       **/
      virtual bool flushOutput() = 0;

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
      virtual void sendMessage( Base::Target &aTarget, 
				const Base::MessageStore &aMessageStore,
				const Base::IntelligentStore *intelligentPtr );

      /// Field the executable name of the program, and deal with appropriately
      /**
       *
       * \param msgID the message id the callback refers to
       *
       **/
      virtual void callback( const unsigned_t msgID );

      /// Query whether object is waiting for callbacks
      /**
       *
       * \returns whether any callbacks are outstanding
       *
       **/      
      virtual bool queryWaiting();

    protected:
      /// Class to temporarily store messages before onward transmission
      class Store
      {
      public:
	/// Constructor
	/**
	 *
	 * \param target the target
	 * \param store the message
	 * \param intPtr the intelligent segments (or NULL if none)
	 * \param encoding the desired encoding of the message
	 *
	 **/
	Store( const Base::Target &target,
	       const Base::MessageStore &store,
	       const Base::IntelligentStore *intPtr,
	       Enum::EncodingType encoding ) :
	  iTarget( target ),
	  iMS( store ),
	  iISPtr( (intPtr == NULL) ? NULL :
		  new Base::IntelligentStore( *intPtr, encoding ) ),
	  iEncoding( encoding )
	{
	}
	
	/// Target for message
	Base::Target iTarget;

	/// MessageStore for message
	Base::MessageStore iMS;

	/// IntelligentStore pointer for message
	std::auto_ptr< Base::IntelligentStore > iISPtr;

	/// Encoding to translate to
	Enum::EncodingType iEncoding;

      private:
	/// No default constructor
	Store();

	/// No copy constructor
	Store( const Store & );

	/// No assignment
	Store &operator=( const Store & );
      };

      /// Extend the size of the output buffer
      void extendOutputBuffer();

      /// Is the interface configured?
      bool iIsConfigured;

      /// input memory buffers
      uint8_t *iInBuffer[2];

      /// allocated size of buffers
      size_t iInAllocSize;

      /// pointer to first data byte in buffers
      uint8_t *iInStartPtr;
      
      /// pointer to first byte not to parse in buffers - this will be
      /// the first free byte when there are only complete messages in
      /// the buffer, but when there is an incomplete message, it will
      /// be the start of that message, as it shouldn't be parsed
      /// until the message is complete.
      uint8_t *iInStopPtr;
      
      /// pointer to first free byte (last data byte + 1) in buffers
      uint8_t *iInEndPtr[2];
      
      /// Which input buffer is currently being written to?
      int iInBufferActiveWrite;

      /// Which input buffer is currently being read from?
      int iInBufferActiveRead;
      
      /// output memory buffer
      uint8_t *iOutBuffer;

      /// allocated output buffer size (total)
      size_t iOutAllocSize;
      
      /// pointer to first data byte in buffer
      uint8_t *iOutStartPtr;
      
      /// pointer to first free byte (last data byte + 1) in buffer
      uint8_t *iOutEndPtr;

      /// map of all outstanding Stores waiting to be translated
      /// referenced by message id of callback
      std::map< unsigned_t, Store* > iMsgIDStorePtrMap;
    };
  }
}
#endif //ndef _BUFFERED_INTERFACE_HH
