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

#ifndef _SET_ERROR_ROUTE_HH
#define _SET_ERROR_ROUTE_HH

#include <string>
#include "Variable.hh"
#include "Target.hh"

namespace NRS
{
  namespace Message
  {
    class SetErrorRouteManager;
    /// A special variable type to handle SetErrorRoute messages.
    /**
     *
     * The SetErrorRoute class represents all message senders and receivers
     * which deal with SetErrorRoute messages
     *
     **/
    class SetErrorRoute : public NRS::Base::Variable
    {
    public:
      /// Constructor for SetErrorRoute Variables.
      /**
       *
       * \param aName the name of the SetErrorRoute Variable being built.
       *
       **/
      SetErrorRoute( std::string aName );
      
      /// Destructor.
      virtual ~SetErrorRoute();

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message.
       *
       * \param priority minimum priority for messages to be sent
       * \param theRoute the route for the error messages to take
       *
       **/
      virtual void receiveMessage( unsigned_t priority,
				   Base::Target &theRoute );

      /// Send a SetErrorRoute message.
      /**
       *
       * \param theTarget the route for the reply to follow
       * \param priority minimum priority for messages to be sent
       * \param theRoute the route for the error messages to take
       *
       **/
      virtual void sendMessage( Base::Target &theTarget,
				unsigned_t priority,
				Base::Target &theRoute );

      /// Is an error route set?
      /**
       *
       * \returns whether an error route is set
       *
       **/
      bool hasErrorRoute()
      {
	return iHasErrorRoute;
      }

      /// Get minimum priority of messages to be transmitted
      /**
       *
       * \returns minimum priority of messages to be transmitted
       *
       **/
      unsigned_t getMinPriority()
      {
	return iMinPriority;
      }

      /// Get route to send error messages along
      /**
       *
       * \returns route to send error messages along
       *
       **/
      Base::Target &getErrorRoute()
      {
	return iErrorRoute;
      }

    private:
      /// The minimum priority message which should be sent on
      unsigned_t iMinPriority;

      /// Whether there is an error route set
      bool iHasErrorRoute;

      /// The target for error messages
      Base::Target iErrorRoute;
    };
  }
}

#endif //ndef _SET_ERROR_ROUTE_HH
