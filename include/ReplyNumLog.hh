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

#ifndef _REPLY_NUMLOG_HH
#define _REPLY_NUMLOG_HH

#include <string>
#include <map>
#include "Variable.hh"

#pragma interface

namespace NRS
{
  namespace Message
  {
    class ReplyNumLogManager;
    /// A special variable type to handle ReplyNumLog messages.
    /**
     *
     * The ReplyNumLog class represents all message senders and receivers
     * which deal with ReplyNumLog messages
     *
     **/
    class ReplyNumLog : public NRS::Base::Variable

    {
    public:
      /// Constructor for ReplyNumLog Variables.
      /**
       *
       * \param aName the name of the ReplyNumLog Variable being built.
       *
       **/
      ReplyNumLog( std::string aName );
      
      /// Destructor.
      virtual ~ReplyNumLog();

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message. Since
       * the Variable is a Number, the message is a double.
       *
       * \param aMessageID message ID, 0 if no id
       * \param aNumLog number of logs on queried component
       *
       **/
      virtual void receiveMessage( unsigned_t aMessageID, 
				   unsigned_t numLog );


      /// Send message as response to QueryNumLog.
      /**
       *
       * This virtual method tells the ReplyNumLogManager to send a reply
       * message in response to a QueryNumLog
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param aNumLog number of logs available
       *
       **/
      virtual void sendMessage( Base::Target &theTarget,
				unsigned_t aMessageID,
				unsigned_t numLog );

      /// Get an NumLog from the MessageID -> NumLog map
      /**
       *
       * Note that ownership of the memory is handed over here!
       *
       * \param aMessageID the ID of the message containing the NumLog
       * \param aNumLog the component type contained in the message identified
       * by aMessageID. Ownership of the memory is handed over.
       * \param aCVersion the version of the component identified
       * above. Ownership of the memory is handed over.
       *
       * \return bool true if the NumLog was found, false otherwise
       *
       **/
      bool getNumLogFromMessageID( unsigned_t aMessageID,
				   unsigned_t &numLog )
      {
	std::map< unsigned_t, unsigned_t >::iterator it =
	  iMessageIDNumLogMap.find( aMessageID );

	if ( it == iMessageIDNumLogMap.end()) 
	  return false;
	else 
	  {
	    numLog = it->second;
	    
	    // as the NumLog has been retrieved, remove it from the map
	    iMessageIDNumLogMap.erase( it );
	    return true;
	  }
      }

    protected:
      /// A map of message ids to NumLogs
      std::map< unsigned_t, unsigned_t > iMessageIDNumLogMap;
    };
  }
}

#endif //ndef _REPLY_NUMLOG_HH
