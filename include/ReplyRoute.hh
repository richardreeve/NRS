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

#ifndef _REPLY_ROUTE_HH
#define _REPLY_ROUTE_HH

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
    /// A special variable type to handle ReplyRoute messages.
    /**
     *
     * The ReplyRoute class represents all message senders and receivers
     * which deal with ReplyRoute messages
     *
     **/
    class ReplyRoute : public Base::Variable

    {
    public:
      /// Constructor for ReplyRoute Variables.
      /**
       *
       * \param aName the name of the ReplyRoute Variable being built.
       *
       **/
      ReplyRoute( std::string aName );
      
      /// Destructor.
      virtual ~ReplyRoute();

      /// Send message as response to a QueryRoute.
      /**
       *
       * This virtual method tells the ReplyRouteManager to send a reply
       * message in response to a QueryRoute
       *
       * \param theTarget the route for the reply to follow
       * \param aMessageID id representing message, 0 if no data
       * \param forwardRoute the forward route from the source to the target
       * \param translationCount a count of the number of translations done
       *
       **/
      void reply( Base::Target &theTarget,
		  integer_t aMessageID,
		  route_t &forwardRoute,
		  integer_t translationCount );

      /// Get an Route from the MessageID -> Route map
      /**
       *
       * \param aMessageID the ID of the message containing the Route
       * \param aForward the forward route
       * \param aReturn the return route
       * \param translationCount a count of the number of translations done
       *
       * \return bool true if the Route was found, false otherwise
       *
       **/
      bool getRouteFromMessageID( unsigned_t aMessageID,
				  route_t &aForward,
				  route_t &aReturn,
				  unsigned_t &translationCount )
      {
	std::map< unsigned_t,
	  std::pair< route_t, route_t > >::iterator it =
	  iMessageIDRoutesMap.find( aMessageID );

	if ( it == iMessageIDRoutesMap.end()) 
	  return false;
	else 
	  {
	    aForward = it->second.first;
	    aReturn = it->second.second;
	    translationCount =
	      iMessageIDTranslationCountMap[ aMessageID ];
	    // as the Route has been retrieved, remove it from the map
	    iMessageIDRoutesMap.erase( it );
	    iMessageIDTranslationCountMap.erase( aMessageID );
	    
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

      /// A map of message ids to forward and reverse routes
      std::map< unsigned_t, 
		std::pair< route_t, route_t > > iMessageIDRoutesMap;
      /// A map of message ids to translationCounts
      std::map< unsigned_t, unsigned_t > iMessageIDTranslationCountMap;

      /// Inbound message id
      const integer_t &iInMsgID;

      /// Inbound forward route
      const route_t &iInForwardRoute;

      /// Inbound return route
      const route_t &iInReturnRoute;

      /// Inbound translation count
      const integer_t &iInTranslationCount;

      /// Outbound message id
      integer_t &iOutMsgID;

      /// Outbound forward route
      route_t &iOutForwardRoute;

      /// Outbound return route
      route_t &iOutReturnRoute;

      /// Outbound translation count
      integer_t &iOutTranslationCount;
    };
  }
}

#endif //ndef _REPLY_ROUTE_HH
