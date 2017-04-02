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

#ifndef _REPLY_PORT_HH
#define _REPLY_PORT_HH

#include <string>

#include "Target.hh"
#include "Types.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    /// A special variable type to handle ReplyPort messages.
    /**
     *
     * The ReplyPort class represents all message senders and receivers
     * which deal with ReplyPort messages
     *
     **/
    class ReplyPort : public Base::Variable

    {
    public:
      /// Constructor for ReplyPort Variables.
      /**
       *
       * \param aName the name of the ReplyPort Variable being built.
       *
       **/
      ReplyPort( std::string aName );
      
      /// Destructor.
      virtual ~ReplyPort();


      /// Send message as response to QueryVNType.
      /**
       *
       * This virtual method tells the ReplyVNTypeManager to send a reply
       * message in response to a QueryVNType
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param aRoute route for port
       *
       **/
      void reply( Base::Target &theTarget,
		  unsigned_t aMessageID,
		  const route_t &aRoute );

      /// Get an Port from the MessageID -> Port map
      /**
       *
       * Note that ownership of the memory is handed over here!
       *
       * \param aMessageID the ID of the message containing the Port
       * \param aPort port route to fill with captured information
       *
       * \return bool true if the Port was found, false otherwise
       *
       **/
      bool getPortFromMessageID( unsigned_t aMessageID,
				 route_t &aPort )
      {
	std::multimap< unsigned_t, route_t >::iterator it =
	  iMessageIDRouteMap.find( aMessageID );
	
	if ( it == iMessageIDRouteMap.end()) 
	  return false;
	else 
	  {
	    aPort = it->second;
	    // as the Port has been retrieved, remove it from the map
	    iMessageIDRouteMap.erase( it );
	    
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
      
      /// Received Message ID
      const integer_t &iInMsgID;

      /// Received route for port
      const route_t &iInRoute;

      /// Outbound Message ID
      integer_t &iOutMsgID;

      /// Outbound route for port
      route_t &iOutRoute;

      /// A map of message ids to Port routes
      std::map< unsigned_t, route_t > iMessageIDRouteMap;
    };
  }
}

#endif //ndef _REPLY_PORT_HH
