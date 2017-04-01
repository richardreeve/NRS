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

#ifndef _REPLY_CSL_HH
#define _REPLY_CSL_HH

#pragma interface
#include <string>
#include <map>

#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    class ReplyCSLManager;
    /// A special variable type to handle ReplyCSL messages.
    /**
     *
     * The ReplyCSL class represents all message senders and receivers
     * which deal with ReplyCSL messages
     *
     **/
    class ReplyCSL : public Base::Variable

    {
    public:
      /// Constructor for ReplyCSL Variables.
      /**
       *
       * \param aName the name of the ReplyCSL Variable being built.
       *
       **/
      ReplyCSL( std::string aName );
      
      /// Destructor.
      virtual ~ReplyCSL();

      /// Send message as response to QueryCSL.
      /**
       *
       * This method tells the ReplyCSLManager to send a reply
       * message in response to a QueryCSL
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param aCSL component info to return
       *
       **/
      virtual void reply( Base::Target &theTarget,
			  integer_t aMessageID,
			  const std::string &aCSL );

      /// Get an CSL from the MessageID -> CSL map
      /**
       *
       * Note that ownership of the memory is handed over here!
       *
       * \param aMessageID the ID of the message containing the CSL
       * \param aCSL the CSL contained in the message identified
       * by aMessageID.
       *
       * \return bool true if the CSL was found, false otherwise
       *
       **/
      bool getCSLFromMessageID( unsigned_t aMessageID,
				std::string &aCSL )
      {
	std::map< unsigned_t, std::string >::iterator it =
	  iMessageIDCSLMap.find( aMessageID );

	if ( it == iMessageIDCSLMap.end()) 
	  return false;
	else 
	  {
	    aCSL = iMessageIDCSLMap[ aMessageID ];
	    
	    // as the CSL has been retrieved, remove it from the map
	    iMessageIDCSLMap.erase( it );
	    
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

      /// Inbound CSL
      const std::string &iInCSL;

      /// Outbound message id
      integer_t &iOutMsgID;

      /// Outbound CSL
      std::string &iOutCSL;

       /// A map of message ids to CSLs
      std::map< unsigned_t, std::string > iMessageIDCSLMap;
    };
  }
}

#endif //ndef _REPLY_CSL_HH
