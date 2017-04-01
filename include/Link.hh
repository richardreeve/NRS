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

#ifndef _LINK_HH
#define _LINK_HH

#include <memory>
#include <string>
#include <vector>

#pragma interface

#include "Encoding.hh"
#include "Exception.hh"
#include "Message.hh"
#include "Observable.hh"
#include "SegmentDescription.hh"
#include "Location.hh"
#include "Types.hh"
#include "VariableManager.hh"

namespace NRS
{
  namespace Base
  {
    class IntelligentStore;
    class Log;
    class MessageStore;
    class Segment;

    /** 
     *
     *  This class contains all the information for message passing
     *  and creation of messages in external languages.
     *
     *  When a link is made from a Variable to another Variable, first
     *  the CreateLink variable checks whether the types are
     *  compatible by querying the VNType of the recipient, and seeing
     *  whether the sending type is derived from, or the same as, the
     *  the recipient type. If it is, then it can make a link, so it
     *  calls the Variable's method to create a link. This in turn
     *  calls the input Link for addSource, or the output Link for
     *  addTarget. Local connections are initiated by the target
     *  Variable via an Observer-Observable relationship on the source
     *  Link, so the input and output Links just store the information
     *  about who connects to it, to make removal of links possible
     *  later. Distal connections are made by the target Link, which
     *  creates a Message object for each link; this contains all the
     *  information about the target necessary to send it a message,
     *  and pointers to the data structures storing the message to be
     *  sent (in the Encoding object). The Encoding object in turn is
     *  updated to make sure it is producing the message in a format
     *  which is needed by the Message objects.
     *
     *  To send a message a Variable sets all of the Segments of the
     *  message using the getSegment method, or, more typically, via
     *  references to the variables in the Segments which it has
     *  acquired earlier and cached. It then calls update() on the
     *  output Link to translate the messages into any required output
     *  languages, and prime the Link so that sendMessages() will send
     *  out messages. Then the Variable calls sendMessages() to
     *  trigger the messages actually being sent. Note that the
     *  Variable itself sends intra-Node fixed connection messages
     *  during the update phase.
     *
     *  When receiving a message, local messages are communicated
     *  directly to the Variable via its observe() method; this avoids
     *  the Link partly in order to be able to determine the exact
     *  source, which can sometimes be useful, and also because
     *  intra-node fixed connections are often between non-matching
     *  Variable types, and so the data could not be passed through
     *  the Link anyway. These observe calls are guaranteed to happen
     *  during the sendMessages phase for all extra-Node connections.
     *  For intra-Node connections, messages need to be sent during
     *  the update phase, so that all the calculations that a Node
     *  needs to do to calculate an output, can be achieved in one
     *  2-phase cycle.
     *
     *  For external connections, messages come into the input Link
     *  via the receiveMessage() method, which decodes them, and then
     *  sends them on to the Variable via the observe() call. Any
     *  message received in this manner will not be acted on until the
     *  next update phase of the Variable.
     *
     *  This two phase strategy completely isolates the messages being
     *  sent from the messages being received, so that no ambiguities
     *  can arise from the order of message passing.
     *
     **/
    class Link : public Observable
    {
    public:
      /// Default constructor
      /** 
       * 
       * Default constructor for Links, assigned to a Variable type
       * by Manager pointer.
       *
       * \param theVMRef reference to variable manager for type
       * \param aVNID VNID of attached variable (or zero if none)
       * 
      **/
      Link( const VariableManager &theVMRef, integer_t aVNID = 0 );

      /// Default destructor
      ~Link();

      /// Receive an external message
      /**
       *
       * \param aMessageStore message to receive
       * \param intelligentPtr intelligent store pointer
       *
       **/
      void receiveMessage( const MessageStore &aMessageStore,
			   const IntelligentStore *intelligentPtr = NULL );

      /// Note reception of an internal message
      /**
       *
       * This method tells the Link that a one-off message has been
       * delivered to it (NOT from a link) and should be instantly
       * transmitted to the Variable
       *
       **/
      void receiveMessage();

      /// Set instant transmission status
      /**
       *
       * \param instantTransmit should arriving messages be sent on
       * immediately, or should we wait for a sendMessages call? Used
       * by non self-updating Variables to eliminate delays in passing
       * data through them.
       *
       **/
      void setInstantTransmission( bool instantTransmit );

      /// Add a Source
      /**
       *
       * \param aSourceRef the source of the message
       *
       **/
      void addSource( const Location &aSourceRef );

      /// Remove a Source
      /**
       *
       * \param aSource the source of the message
       *
       **/
      bool removeSource( const Location &aSourceRef );

      /// Adds a target which will be connected to
      /**
       *
       * \param aTarget the target to which to connect
       *
       **/
      void addTarget( const Target &aTarget );

      /// Remove a target to which the Link was connected
      /**
       *
       * \param aTarget the target from which to disconnect
       *
       **/
      bool removeTarget( const Target &aTarget );

      /// Add a Log
      /**
       *
       * \param aLogPtr the target of the message
       *
       **/
      void addLog( Log *aLogPtr );

      /// Remove a Log
      /**
       *
       * \param aLog the target of the message
       * \param logPort port of log from target component
       *
       **/
      bool removeLog( const Log *aLogPtr );

      /// resolve any dependency issues
      /**
       *
       * Note: by default a variable has no dependency issues
       *
       * \returns whether all dependencies are resolved
       *
       **/
      bool resolveDependencies();

      /// Set number of connections
      /**
       *
       * \param min_in the minimum number of inbound connections
       * \param max_in the maximum number of inbound connections
       * \param min_out the minimum number of outbound connections
       * \param max_out the maximum number of outbound connections
       * \param min_log the minimum number of log connections
       * \param max_log the maximum number of log connections
       *
       **/
      void setConnections( int min_in, int max_in,
			   int min_out, int max_out,
			   int min_log, int max_log );

      /// Send any outstanding messages.
      /**
       *
       * This method inputs any external messages, and outputs any
       * pending messages through the output interface.
       *
       **/
      void sendMessages();

      /// Update state of required encodings
      void update();

      /// How many inputs does the link have?
      /**
       *
       * \return number of inputs
       *
       **/
      unsigned_t getNumInputs() const
      {
	return iNumIn;
      }

      /// How many outputs does the link have?
      /**
       *
       * \return number of outputs
       *
       **/
      unsigned_t getNumOutputs() const
      {
	return iNumOut;
      }

      /// How many logs does the link have?
      /**
       *
       * \return number of logs
       *
       **/
      unsigned_t getNumLogs() const
      {
	return iNumLog;
      }

      /// Return a specified segment
      /**
       *
       * \param num number of segment
       * \return a specified segment
       *
       **/
      const Segment &getSegment( unsigned_t num ) const
      {
	return *iEncoding.getSegment( num );
      }

      /// Return a specified segment
      /**
       *
       * \param num number of segment
       * \return a specified segment
       *
       **/
      Segment &getSegment( unsigned_t num )
      {
	return *iEncoding.getSegment( num );
      }

      /// Clear the intelligent segments
      /**
       *
       * \return the pointer for the intelligent segments
       *
       **/
      void clearIntelligentSegments()
      {
	iEncoding.clearIntelligentSegments();
      }

      /// Get intelligent Segment pointer
      /**
       *
       * \param iType intelligent segment type
       *
       * \return the pointer to this Intelligent Segment or NULL
       *
       **/
      const Segment *getIntelligentSegment( Enum::IntelligentType iType )
      {
	return iEncoding.getIntelligentSegment( iType );
      }

      /// Set intelligent Segment pointer
      /**
       *
       * \param aType intelligent segment type
       *
       * \param intSeg the pointer to this Intelligent Segment or NULL
       * to clear it
       *
       **/
      void setIntelligentSegment( Enum::IntelligentType aType,
				  const Segment *intSeg )
      {
	return iEncoding.setIntelligentSegment( aType, intSeg );
      }

    private:
      /// No default constructor
      Link();

      /// No copy constructor
      Link( const Link & );

      /// No assignment
      Link &operator=( const Link & );

      /// Messages of different types need to be sent
      Encoding iEncoding;

      /// Variable Manager for link message type
      const VariableManager &iVM;

      /// Instant transmission of messages through Links
      bool iInstantTransmit;

      /// Sources of messages
      std::vector< Location > iSource;

      /// Messages to be sent
      std::vector< Message* > iMessagePtr;

      /// Has a new message been received?
      bool iReceivedMessage;

      /// minimum inbound connections
      int iMinIn;

      /// maximum inbound connections
      int iMaxIn;

      /// number of inbound connections
      int iNumIn;

      /// minimum outbound connections
      int iMinOut;

      /// maximum outbound connections
      int iMaxOut;

      /// number of outbound connections
      int iNumOut;

      /// minimum log connections
      int iMinLog;

      /// maximum log connections
      int iMaxLog;

      /// number of log connections
      int iNumLog;

      /// VNID of attached variable (or zero if none)
      integer_t iVNID;
    };
  }
}

#endif //ndef _LINK_HH
