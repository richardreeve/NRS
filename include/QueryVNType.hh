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

#ifndef _QUERY_VNTYPE_HH
#define _QUERY_VNTYPE_HH

#include <string>

#include "Target.hh"
#include "Types.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    class ReplyVNType;

    /// A special variable type to handle QueryVNType messages.
    /**
     *
     * The QueryVNType class represents all message senders and receivers
     * which deal with QueryVNType messages
     *
     **/
    class QueryVNType : public Base::Variable

    {
    public:
      /// Constructor for QueryVNType Variables.
      /**
       *
       * \param aName the name of the QueryVNType Variable being built.
       *
       **/
      QueryVNType( std::string aName );
      
      /// Destructor.
      virtual ~QueryVNType();

      /// Request a VNType from a VNID
      /**
       *
       * This method requests the QueryVNType variable to give the
       * type of another variable
       *
       * \param theTarget the variable to be queried
       * \param aMessageID id representing message, 0 if no data
       * \param aVNID vnid to query type of
       *
       **/
      void request( Base::Target &theTarget,
		    unsigned_t aMessageID,
		    unsigned_t aVNID );

      /// Cache a vntype for future use
      /**
       *
       * Note that this depends on the fact that vnids will never be
       * reallocated to a variable of a different type
       *
       * \param aMessageID id representing data
       * \param aVNType string containing VNType
       *
       **/
      void cache( unsigned_t aMessageID,
		  std::string aVNType );

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

      /// Pointer to ReplyVNType variable, with which QueryVNType communicates
      ReplyVNType *iReplyVNTypePtr;

      /// Inbound return route
      const route_t &iInReturnRoute;

      /// Inbound return VNID
      const integer_t &iInReturnVNID;

      /// Inbound message id
      const integer_t &iInMsgID;

      /// Inbound vnid
      const integer_t &iInVNID;

      /// Outbound return route
      route_t &iOutReturnRoute;

      /// Outbound return VNID
      integer_t &iOutReturnVNID;

      /// Outbound message id
      integer_t &iOutMsgID;

      /// Outbound vnid
      integer_t &iOutVNID;

      /// A map of message IDs to targets
      std::map< unsigned_t, Base::Location > iMsgIDTargetMap;

      /// Cache of route and vnids against vntype
      std::map< route_t,
		std::map< integer_t, std::string > > iRouteVNIDTypeMap;
    };
  }
}

#endif //ndef _QUERY_VNTYPE_HH
