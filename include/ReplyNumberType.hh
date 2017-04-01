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

#ifndef _REPLY_NUMBERTYPE_HH
#define _REPLY_NUMBERTYPE_HH

#include <string>
#include <map>
#include "Variable.hh"

#pragma interface

namespace NRS
{
  namespace Message
  {
    class ReplyNumberTypeManager;
    /// A special variable type to handle ReplyNumberType messages.
    /**
     *
     * The ReplyNumberType class represents all message senders and receivers
     * which deal with ReplyNumberType messages
     *
     **/
    class ReplyNumberType : public NRS::Base::Variable

    {
    public:
      /// Constructor for ReplyNumberType Variables.
      /**
       *
       * \param aName the name of the ReplyNumberType Variable being built.
       *
       **/
      ReplyNumberType( std::string aName );
      
      /// Destructor.
      virtual ~ReplyNumberType();

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message. Since
       * the Variable is a Number, the message is a double.
       *
       * \param aMessageID message ID, 0 if no id
       * \param maxBits maximum number of bits component can handle
       * \param floatingPoint can the component handle floats and doubles?
       *
       **/
      virtual void receiveMessage( unsigned_t aMessageID,
				   unsigned_t maxBits,
				   bool floatingPoint );


      /// Send message as response to QueryNumberType.
      /**
       *
       * This virtual method tells the ReplyNumberTypeManager to send a reply
       * message in response to a QueryNumberType
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param maxBits maximum number of bits component can handle
       * \param floatingPoint can the component handle floats and doubles?
       *
       **/
      virtual void sendMessage( Base::Target &theTarget,
				unsigned_t aMessageID,
				unsigned_t maxBits,
				bool floatingPoint );

      /// Get an NumberType from the MessageID -> NumberType map
      /**
       *
       * Note that ownership of the memory is handed over here!
       *
       * \param aMessageID the ID of the message containing the NumberType
       * \param maxBits maximum number of bits target component can handle
       * \param floatingPoint can the target component handle floats
       * and doubles?
       *
       * \return bool true if the message was found, false otherwise
       *
       **/
      bool getNumberTypeFromMessageID( unsigned_t aMessageID,
				       unsigned_t &maxBits,
				       bool &floatingPoint )
      {
	std::map< unsigned_t, 
	  std::pair< unsigned_t ,bool > >::iterator it =
	  iMessageIDNumberTypeMap.find( aMessageID );

	if ( it == iMessageIDNumberTypeMap.end()) 
	  return false;
	else 
	  {
	    maxBits = it->second.first;
	    floatingPoint = it->second.second;
	    
	    // as the NumberType has been retrieved, remove it from the map
	    iMessageIDNumberTypeMap.erase( it );
	    
	    return true;
	  }
      }

    protected:
      /// A map of message ids to NumberTypes
      std::map< unsigned_t,
		std::pair< unsigned_t, bool > > iMessageIDNumberTypeMap;
    };
  }
}

#endif //ndef _REPLY_NUMBERTYPE_HH
