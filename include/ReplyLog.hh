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

#ifndef _REPLY_LOG_HH
#define _REPLY_LOG_HH

#include <string>
#include <map>
#include "Variable.hh"
#include "Target.hh"

#pragma interface

namespace NRS
{
  namespace Message
  {
    class ReplyLogManager;
    /// A special variable type to handle ReplyLog messages.
    /**
     *
     * The ReplyLog class represents all message senders and receivers
     * which deal with ReplyLog messages
     *
     **/
    class ReplyLog : public NRS::Base::Variable

    {
    public:
      /// Constructor for ReplyLog Variables.
      /**
       *
       * \param aName the name of the ReplyLog Variable being built.
       *
       **/
      ReplyLog( std::string aName );
      
      /// Destructor.
      virtual ~ReplyLog();

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message. Since
       * the Variable is a Number, the message is a double.
       *
       * \param aMessageID message ID, 0 if no id
       * \param aLog Log route received from Reply Message
       * \param isPMLNotBMF language of log
       *
       **/
      virtual void receiveMessage( unsigned_t aMessageID, 
				   Base::Target &aLog,
				   bool isPMLNotBMF );

      /// Send message as response to QueryLog.
      /**
       *
       * This virtual method tells the ReplyLogManager to send a reply
       * message in response to a QueryLog
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param aLog log route to send out
       * \param isPMLNotBMF language of log
       *
       **/
      virtual void sendMessage( Base::Target &theTarget,
				unsigned_t aMessageID,
				Base::Target &aLog,
				bool isPMLNotMBF );

      /// Get an Log from the MessageID -> Log map
      /**
       *
       * Note that ownership of the memory is handed over here!
       *
       * \param aMessageID the ID of the message containing the Log
       * \param aLog log route to fill with captured information
       * \param isPMLNotBMF language of log
       *
       * \return bool true if the Log was found, false otherwise
       *
       **/
      bool getLogFromMessageID( unsigned_t aMessageID,
				Base::Target &aLog,
				bool &isPMLNotBMF )
      {
	std::multimap< unsigned_t, std::pair< Base::Target,
	  bool > >::iterator it =
	  iMessageIDLogMap.find( aMessageID );
	
	if ( it == iMessageIDLogMap.end()) 
	  return false;
	else 
	  {
	    aLog.takeRoutes( it->second.first );
	    isPMLNotBMF = it->second.second;
	    // as the Log has been retrieved, remove it from the map
	    iMessageIDLogMap.erase( it );
	    
	    return true;
	  }
      }
      
    protected:
      /// A map of message ids to Logs
      std::multimap< unsigned_t, std::pair< Base::Target,
					    bool > > iMessageIDLogMap;
    };
  }
}

#endif //ndef _REPLY_LOG_HH
