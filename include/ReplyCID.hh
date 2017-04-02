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

#ifndef _REPLY_CID_HH
#define _REPLY_CID_HH

#include <string>
#include <map>
#include "Variable.hh"
#include "Target.hh"

namespace NRS
{
  namespace Base
  {
    class Target;
  }

  namespace Message
  {
    /// A special variable type to handle ReplyCID messages.
    /**
     *
     * The ReplyCID class represents all message senders and receivers
     * which deal with ReplyCID messages
     *
     **/
    class ReplyCID : public Base::Variable

    {
    public:
      /// Constructor for ReplyCID Variables.
      /**
       *
       * \param aName the name of the ReplyCID Variable being built.
       *
       **/
      ReplyCID( std::string aName );
      
      /// Destructor.
      virtual ~ReplyCID();

      /// Send message as response to QueryCID.
      /**
       *
       * This method tells the ReplyCIDManager to send a reply
       * message in response to a QueryCID
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * of messageID when QueryConnectedCIDs has issued the QueryCID message)
       * \param aCID id to return
       *
       **/
      void reply( Base::Target &theTarget,
		  unsigned_t aMessageID,
		  std::string aCID );


      /// Get an CID from the MessageID -> CID map
      /**
       *
       * Note that ownership of this memory is passed over!
       *
       * \param aMessageID the ID of the message containing the VNID
       * \param aCID the ID contained in the message identified by
       * aMessageID
       *
       * \return bool true if the CID was found, false otherwise
       *
       **/
      bool getCIDFromMessageID( unsigned_t aMessageID,
				std::string &aCID )
      {

	std::map< unsigned_t, std::string >::iterator it =
	  iMessageIDCIDMap.find( aMessageID );
	
	if ( it == iMessageIDCIDMap.end()) 
	  return false;

	aCID = it->second;
	
	//as the CID has been retrieved, remove it from the map
	iMessageIDCIDMap.erase( it );
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

      /// Inbound cid
      const std::string &iInCID;

      /// Outbound message id
      integer_t &iOutMsgID;

      /// Outbound cid
      std::string &iOutCID;

      /// A map of messageIDs to CIDs
      std::map< unsigned_t, std::string > iMessageIDCIDMap;
    };
  }
}

#endif //ndef _REPLY_CID_HH
