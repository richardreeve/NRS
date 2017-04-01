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

#ifndef _REPLY_LANGUAGE_HH
#define _REPLY_LANGUAGE_HH

#include <string>
#include <map>
#include "Variable.hh"

#pragma interface

namespace NRS
{
  namespace Message
  {
    class ReplyLanguageManager;
    /// A special variable type to handle ReplyLanguage messages.
    /**
     *
     * The ReplyLanguage class represents all message senders and receivers
     * which deal with ReplyLanguage messages
     *
     **/
    class ReplyLanguage : public NRS::Base::Variable

    {
    public:
      /// Constructor for ReplyLanguage Variables.
      /**
       *
       * \param aName the name of the ReplyLanguage Variable being built.
       *
       **/
      ReplyLanguage( std::string aName );
      
      /// Destructor.
      virtual ~ReplyLanguage();

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message. Since
       * the Variable is a Number, the message is a double.
       *
       * \param aMessageID message ID, 0 if no id
       * \param speaksBMF does the target component speak BMF?
       * \param speaksPML does the target component speak PML?
       *
       **/
      virtual void receiveMessage( unsigned_t aMessageID,
				   bool speaksBMF,
				   bool speaksPML );


      /// Send message as response to QueryLanguage.
      /**
       *
       * This virtual method tells the ReplyLanguageManager to send a reply
       * message in response to a QueryLanguage
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param speaksBMF does the component speak BMF?
       * \param speaksPML does the component speak PML?
       *
       **/
      virtual void sendMessage( Base::Target &theTarget,
				unsigned_t aMessageID,
				bool speaksBMF,
				bool speaksPML );

      /// Get an Language from the MessageID -> Language map
      /**
       *
       * Note that ownership of the memory is handed over here!
       *
       * \param aMessageID the ID of the message containing the Language
       * \param speaksBMF does the target component speak BMF?
       * \param speaksPML does the target component speak PML?
       *
       * \return bool true if the message was found, false otherwise
       *
       **/
      bool getLanguageFromMessageID( unsigned_t aMessageID,
				     bool &speaksBMF,
				     bool &speaksPML )
      {
	std::map< unsigned_t, std::pair< bool ,bool > >::iterator it =
	  iMessageIDLanguageMap.find( aMessageID );

	if ( it == iMessageIDLanguageMap.end()) 
	  return false;
	else 
	  {
	    speaksBMF = it->second.first;
	    speaksPML = it->second.second;
	    
	    // as the Language has been retrieved, remove it from the map
	    iMessageIDLanguageMap.erase( it );
	    
	    return true;
	  }
      }

    protected:
      /// A map of message ids to Languages
      std::map< unsigned_t, std::pair< bool ,bool > > iMessageIDLanguageMap;
    };
  }
}

#endif //ndef _REPLY_LANGUAGE_HH
