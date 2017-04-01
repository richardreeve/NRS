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

#ifndef _REPLY_CTYPE_HH
#define _REPLY_CTYPE_HH

#pragma interface
#include <map>
#include <string>

#include "Target.hh"
#include "Types.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    /// A special variable type to handle ReplyCType messages.
    /**
     *
     * The ReplyCType class represents all message senders and receivers
     * which deal with ReplyCType messages
     *
     **/
    class ReplyCType : public Base::Variable

    {
    public:
      /// Constructor for ReplyCType Variables.
      /**
       *
       * \param aName the name of the ReplyCType Variable being built.
       *
       **/
      ReplyCType( std::string aName );
      
      /// Destructor.
      virtual ~ReplyCType();

      /// Send message as response to QueryCType.
      /**
       *
       * This method tells the ReplyCTypeManager to send a reply
       * message in response to a QueryCType
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param aCType component type to return
       * \param aCVersion version of component to return
       *
       **/
      virtual void reply( Base::Target &theTarget,
			  integer_t aMessageID,
			  const std::string &aCType,
			  const std::string &aCVersion );

      /// Get an CType from the MessageID -> CType map
      /**
       *
       * Note that ownership of the memory is handed over here!
       *
       * \param aMessageID the ID of the message containing the CType
       * \param aCType the component type contained in the message identified
       * by aMessageID.
       * \param aCVersion the version of the component identified
       * above.
       *
       * \return bool true if the CType was found, false otherwise
       *
       **/
      bool getCTypeFromMessageID( integer_t aMessageID,
				  std::string &aCType,
				  std::string &aCVersion )
      {
	std::map< integer_t,
	  std::pair< std::string, std::string > >::iterator it =
	  iMessageIDCTypeMap.find( aMessageID );

	if ( it == iMessageIDCTypeMap.end()) 
	  return false;

	aCType = iMessageIDCTypeMap[ aMessageID ].first;
	aCVersion = iMessageIDCTypeMap[ aMessageID ].second;
	
	// as the CType has been retrieved, remove it from the map
	iMessageIDCTypeMap.erase( it );
	
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

      /// Inbound message id
      const integer_t &iInMsgID;

      /// Inbound ctype
      const std::string &iInCType;

      /// Inbound version
      const std::string &iInCVersion;

      /// Outbound message id
      integer_t &iOutMsgID;

      /// Outbound ctype
      std::string &iOutCType;

      /// Outbound version
      std::string &iOutCVersion;

      /// A map of message ids to CTypes
      std::map< integer_t,
		std::pair< std::string, std::string > > iMessageIDCTypeMap;
    };
  }
}

#endif //ndef _REPLY_CTYPE_HH
