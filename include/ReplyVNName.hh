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

#ifndef _REPLY_VNNAME_HH
#define _REPLY_VNNAME_HH

#pragma interface
#include <string>
#include <map>

#include "Target.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    class ReplyVNNameManager;
    /// A special variable type to handle ReplyVNName messages.
    /**
     *
     * The ReplyVNName class represents all message senders and receivers
     * which deal with ReplyVNName messages
     *
     **/
    class ReplyVNName : public Base::Variable

    {
    public:
      /// Constructor for ReplyVNName Variables.
      /**
       *
       * \param aName the name of the ReplyVNName Variable being built.
       *
       **/
      ReplyVNName( std::string aName );
      
      /// Destructor.
      virtual ~ReplyVNName();

      /// Send message as response to QueryVNName.
      /**
       *
       * This virtual method tells the ReplyVNNameManager to send a reply
       * message in response to a QueryVNName
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param aVNName variable or node type to return
       *
       **/
      void reply( Base::Target &theTarget,
		  unsigned_t aMessageID,
		  std::string aVNName );

      /// Get an VNName from the MessageID -> VNName map
      /**
       *
       * \param aMessageID the ID of the message containing the VNName
       * \param aVNName the ID contained in the message identified by
       * aMessageID
       *
       * \return bool true if the VNName was found, false otherwise
       *
       **/
      bool getVNNameFromMessageID ( unsigned_t aMessageID,
				    std::string &aVNName )
      {
	std::map< unsigned_t, std::string >::iterator it =
	  iMessageIDVNNameMap.find( aMessageID );

	if ( it == iMessageIDVNNameMap.end()) 
	  return false;

	aVNName = iMessageIDVNNameMap[ aMessageID ];
	
	//as the VNName has been retrieved, remove it from the map
	iMessageIDVNNameMap.erase ( it );
	
	return true;
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
      const std::string &iInVNName;

      /// Outbound Message ID
      integer_t &iOutMsgID;

      /// Outbound VNID
      std::string &iOutVNName;

      /// A map of messageIDs to VNNames
      std::map< unsigned_t, std::string > iMessageIDVNNameMap;
    };
  }
}

#endif //ndef _REPLY_VNNAME_HH
