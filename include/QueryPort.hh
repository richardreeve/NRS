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

#ifndef _QUERY_PORT_HH
#define _QUERY_PORT_HH

#pragma interface
#include <string>

#include "Target.hh"
#include "Types.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    class ReplyPort;

    /// A special variable type to handle QueryPort messages.
    /**
     *
     * The QueryPort class represents all message senders and receivers
     * which deal with QueryPort messages
     *
     **/
    class QueryPort : public Base::Variable

    {
    public:
      /// Constructor for QueryPort Variables.
      /**
       *
       * \param aName the name of the QueryPort Variable being built.
       *
       **/
      QueryPort( std::string aName );
      
      /// Destructor.
      virtual ~QueryPort();

      /// Request a Port from a Port
      /**
       *
       * This method requests the QueryPort variable to give the
       * type of another variable
       *
       * \param theTarget the variable to be queried
       * \param aMessageID id representing message, 0 if no data
       * \param aPort port to query route of
       *
       **/
      void request( Base::Target &theTarget,
		    unsigned_t aMessageID,
		    unsigned_t aPort );

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

      /// Pointer to ReplyPort variable, with which QueryPort communicates
      ReplyPort *iReplyPortPtr;

      /// Inbound return route
      const route_t &iInReturnRoute;

      /// Inbound return VNID
      const integer_t &iInReturnVNID;

      /// Inbound message id
      const integer_t &iInMsgID;

      /// Inbound Port number
      const integer_t &iInPort;

      /// Outbound return route
      route_t &iOutReturnRoute;

      /// Outbound return VNID
      integer_t &iOutReturnVNID;

      /// Outbound message id
      integer_t &iOutMsgID;

      /// Outbound Port number
      integer_t &iOutPort;
    };
  }
}

#endif //ndef _QUERY_PORT_HH
