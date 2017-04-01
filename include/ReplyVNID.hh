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

#ifndef _REPLY_VNID_HH
#define _REPLY_VNID_HH

#include <map>
#include <string>
#include "Variable.hh"

#pragma interface

namespace NRS
{
  namespace Base
  {
    class Target;
  }

  namespace Message
  {
    /// A special variable type to handle ReplyVNID messages.
    /**
     *
     * The ReplyVNID class represents all message senders and receivers
     * which deal with ReplyVNID messages
     *
     **/
    class ReplyVNID : public Base::Variable

    {
    public:
      /// Constructor for ReplyVNID Variables.
      /**
       *
       * \param aName the name of the ReplyVNID Variable being built.
       *
       **/
      ReplyVNID( std::string aName );
      
      /// Destructor.
      virtual ~ReplyVNID();

      /// Send message as response to QueryVNID.
      /**
       *
       * This method tells the ReplyVNIDManager to send a reply
       * message in response to a QueryVNID
       *
       * \param returnTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param aVNID id to return
       *
       **/
      void reply( Base::Target &theTarget,
		  unsigned_t aMessageID,
		  unsigned_t aVNID );

      /// Get an VNID from the MessageID -> VNID map
      /**
       *
       * \param aMessageID the ID of the message containing the VNID
       * \param aVNID the ID contained in the message identified by aMessageID
       *
       * \return bool true if the VNID was found, false otherwise
       *
       **/
      bool getVNIDFromMessageID( unsigned_t aMessageID,
				 unsigned_t &aVNID )
      {
	std::map< unsigned_t, unsigned_t>::iterator it =
	  iMessageIDVNIDMap.find( aMessageID );

	if ( it == iMessageIDVNIDMap.end()) 
	  return false;
	else 
	  {
	    aVNID = iMessageIDVNIDMap[ aMessageID ];
	    
	    // as the VNID has been retrieved, remove it from the map
	    iMessageIDVNIDMap.erase( it );
	    
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

      /// Received VNID
      const integer_t &iInVNID;

      /// Outbound Message ID
      integer_t &iOutMsgID;

      /// Outbound VNID
      integer_t &iOutVNID;

      /// A map of message ids to VNIDs
      std::map< unsigned_t, unsigned_t > iMessageIDVNIDMap;
    };
  }
}

#endif //ndef _REPLY_VNID_HH
