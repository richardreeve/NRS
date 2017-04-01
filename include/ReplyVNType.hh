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

#ifndef _REPLY_VNTYPE_HH
#define _REPLY_VNTYPE_HH

#pragma interface
#include <string>
#include <map>

#include "Target.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    class ReplyVNTypeManager;
    class QueryVNType;
    /// A special variable type to handle ReplyVNType messages.
    /**
     *
     * The ReplyVNType class represents all message senders and receivers
     * which deal with ReplyVNType messages
     *
     **/
    class ReplyVNType : public Base::Variable

    {
    public:
      /// Constructor for ReplyVNType Variables.
      /**
       *
       * \param aName the name of the ReplyVNType Variable being built.
       *
       **/
      ReplyVNType( std::string aName );
      
      /// Destructor.
      virtual ~ReplyVNType();

      /// Send message as response to QueryVNType.
      /**
       *
       * This virtual method tells the ReplyVNTypeManager to send a reply
       * message in response to a QueryVNType
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param aVNType variable or node type to return
       *
       **/
      void reply( Base::Target &theTarget,
		  unsigned_t aMessageID,
		  std::string aVNType );

      /// Get an VNType from the MessageID -> VNType map
      /**
       *
       * \param aMessageID the ID of the message containing the VNType
       * \param aVNType the ID contained in the message identified by
       * aMessageID
       *
       * \return bool true if the VNType was found, false otherwise
       *
       **/
      bool getVNTypeFromMessageID ( unsigned_t aMessageID,
				    std::string &aVNType )
      {
	std::map< unsigned_t, std::string >::iterator it =
	  iMessageIDVNTypeMap.find( aMessageID );

	if ( it == iMessageIDVNTypeMap.end()) 
	  return false;

	aVNType = iMessageIDVNTypeMap[ aMessageID ];
	
	//as the VNType has been retrieved, remove it from the map
	iMessageIDVNTypeMap.erase ( it );
	
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

      /// resolve any dependency issues
      /**
       *
       * \returns whether all dependencies are resolved
       *
       **/
      virtual bool resolveComplexDependencies();

      /// Pointer to QueryVNType variable, with which ReplyVNType communicates
      QueryVNType *iQueryVNTypePtr;

      /// Received Message ID
      const integer_t &iInMsgID;

      /// Received VNID
      const std::string &iInVNType;

      /// Outbound Message ID
      integer_t &iOutMsgID;

      /// Outbound VNID
      std::string &iOutVNType;

      /// A map of messageIDs to VNTypes
      std::map< unsigned_t, std::string > iMessageIDVNTypeMap;
    };
  }
}

#endif //ndef _REPLY_VNTYPE_HH
