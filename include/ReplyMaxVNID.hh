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

#ifndef _REPLY_MAXVNID_HH
#define _REPLY_MAXVNID_HH

#include <string>
#include <map>
#include "Variable.hh"

#pragma interface

namespace NRS
{
  namespace Message
  {
    class ReplyMaxVNIDManager;
    /// A special variable type to handle ReplyMaxVNID messages.
    /**
     *
     * The ReplyMaxVNID class represents all message senders and receivers
     * which deal with ReplyMaxVNID messages
     *
     **/
    class ReplyMaxVNID : public NRS::Base::Variable

    {
    public:
      /// Constructor for ReplyMaxVNID Variables.
      /**
       *
       * \param aName the name of the ReplyMaxVNID Variable being built.
       *
       **/
      ReplyMaxVNID( std::string aName );
      
      /// Destructor.
      virtual ~ReplyMaxVNID();

      /// Send message as response to QueryMaxPort.
      /**
       *
       * This method tells the ReplyMaxPortManager to send a reply
       * message in response to a QueryMaxPort
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param maxVNID number of vnids on queried component
       *
       **/
      virtual void reply( Base::Target &theTarget,
			  integer_t aMessageID,
			  integer_t maxVNID );

      /// Get an MaxVNID from the MessageID -> MaxVNID map
      /**
       *
       * Note that ownership of the memory is handed over here!
       *
       * \param aMessageID the ID of the message containing the MaxVNID
       * \param aMaxVNID the max vnid contained in the message identified
       * by aMessageID.
       *
       * \return bool true if the MaxVNID was found, false otherwise
       *
       **/
      bool getMaxVNIDFromMessageID( unsigned_t aMessageID,
				    unsigned_t &aMaxVNID )
      {
	std::map< unsigned_t, unsigned_t >::iterator it =
	  iMessageIDMaxVNIDMap.find( aMessageID );

	if ( it == iMessageIDMaxVNIDMap.end()) 
	  return false;
	else 
	  {
	    aMaxVNID = iMessageIDMaxVNIDMap[ aMessageID ];
	    
	    // as the MaxVNID has been retrieved, remove it from the map
	    iMessageIDMaxVNIDMap.erase( it );
	    
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

      /// Inbound max vnid
      const integer_t &iInMaxVNID;

      /// Outbound message id
      integer_t &iOutMsgID;

      /// Outbound max vnid
      integer_t &iOutMaxVNID;

      /// A map of message ids to MaxVNIDs
      std::map< unsigned_t, unsigned_t > iMessageIDMaxVNIDMap;
    };
  }
}

#endif //ndef _REPLY_MAXVNID_HH
