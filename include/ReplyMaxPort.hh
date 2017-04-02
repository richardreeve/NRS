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

#ifndef _REPLY_MAXPORT_HH
#define _REPLY_MAXPORT_HH

#include <string>
#include <map>

#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    class ReplyMaxPortManager;
    /// A special variable type to handle ReplyMaxPort messages.
    /**
     *
     * The ReplyMaxPort class represents all message senders and
     * receivers which deal with ReplyMaxPort messages
     *
     **/
    class ReplyMaxPort : public Base::Variable

    {
    public:
      /// Constructor for ReplyMaxPort Variables.
      /**
       *
       * \param aName the name of the ReplyMaxPort Variable being built.
       *
       **/
      ReplyMaxPort( std::string aName );
      
      /// Destructor.
      virtual ~ReplyMaxPort();

      /// Send message as response to QueryMaxPort.
      /**
       *
       * This method tells the ReplyMaxPortManager to send a reply
       * message in response to a QueryMaxPort
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param maxPort number of ports on queried component
       *
       **/
      virtual void reply( Base::Target &theTarget,
			  integer_t aMessageID,
			  integer_t maxPort );

      /// Get an MaxPort from the MessageID -> MaxPort map
      /**
       *
       * Note that ownership of the memory is handed over here!
       *
       * \param aMessageID the ID of the message containing the MaxPort
       * \param maxPort the maximum port number contained in the
       * message identified by aMessageID.
       *
       * \return bool true if the MaxPort was found, false otherwise
       *
       **/
      bool getMaxPortFromMessageID( unsigned_t aMessageID,
				    unsigned_t &maxPort )
      {
	std::map< unsigned_t, unsigned_t >::iterator it =
	  iMessageIDMaxPortMap.find( aMessageID );

	if ( it == iMessageIDMaxPortMap.end()) 
	  return false;
	else 
	  {
	    maxPort = it->second;
	    
	    // as the MaxPort has been retrieved, remove it from the map
	    iMessageIDMaxPortMap.erase( it );
	    return true;
	  }
      }

    protected:
      /// New data is available to observe from a source
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doObserve( integer_t aRef );

      /// Inbound message id
      const integer_t &iInMsgID;

      /// Inbound max port
      const integer_t &iInMaxPort;

      /// Outbound message id
      integer_t &iOutMsgID;

      /// Outbound max port
      integer_t &iOutMaxPort;

      /// A map of message ids to MaxPorts
      std::map< unsigned_t, unsigned_t > iMessageIDMaxPortMap;
    };
  }
}

#endif //ndef _REPLY_MAXPORT_HH
