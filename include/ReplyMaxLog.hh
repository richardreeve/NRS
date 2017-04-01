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

#ifndef _REPLY_MAXLOG_HH
#define _REPLY_MAXLOG_HH

#include <string>
#include <map>
#include "Variable.hh"

#pragma interface

namespace NRS
{
  namespace Message
  {
    class ReplyMaxLogManager;
    /// A special variable type to handle ReplyMaxLog messages.
    /**
     *
     * The ReplyMaxLog class represents all message senders and receivers
     * which deal with ReplyMaxLog messages
     *
     **/
    class ReplyMaxLog : public NRS::Base::Variable

    {
    public:
      /// Constructor for ReplyMaxLog Variables.
      /**
       *
       * \param aName the name of the ReplyMaxLog Variable being built.
       *
       **/
      ReplyMaxLog( std::string aName );
      
      /// Destructor.
      virtual ~ReplyMaxLog();

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message. Since
       * the Variable is a Number, the message is a double.
       *
       * \param aMessageID message ID, 0 if no id
       * \param aMaxLog number of logs on queried component
       *
       **/
      virtual void receiveMessage( unsigned_t aMessageID, 
				   unsigned_t maxLog );


      /// Send message as response to QueryMaxLog.
      /**
       *
       * This virtual method tells the ReplyMaxLogManager to send a reply
       * message in response to a QueryMaxLog
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param aMaxLog number of logs available
       *
       **/
      virtual void sendMessage( Base::Target &theTarget,
				unsigned_t aMessageID,
				unsigned_t maxLog );

      /// Get an MaxLog from the MessageID -> MaxLog map
      /**
       *
       * Note that ownership of the memory is handed over here!
       *
       * \param aMessageID the ID of the message containing the MaxLog
       * \param aMaxLog the component type contained in the message identified
       * by aMessageID. Ownership of the memory is handed over.
       * \param aCVersion the version of the component identified
       * above. Ownership of the memory is handed over.
       *
       * \return bool true if the MaxLog was found, false otherwise
       *
       **/
      bool getMaxLogFromMessageID( unsigned_t aMessageID,
				   unsigned_t &maxLog )
      {
	std::map< unsigned_t, unsigned_t >::iterator it =
	  iMessageIDMaxLogMap.find( aMessageID );

	if ( it == iMessageIDMaxLogMap.end()) 
	  return false;
	else 
	  {
	    maxLog = it->second;
	    
	    // as the MaxLog has been retrieved, remove it from the map
	    iMessageIDMaxLogMap.erase( it );
	    return true;
	  }
      }

    protected:
      /// A map of message ids to MaxLogs
      std::map< unsigned_t, unsigned_t > iMessageIDMaxLogMap;
    };
  }
}

#endif //ndef _REPLY_MAXLOG_HH
