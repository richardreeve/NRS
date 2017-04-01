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

#ifndef _QUERY_LANGUAGE_HH
#define _QUERY_LANGUAGE_HH

#include <string>
#include "Variable.hh"

#pragma interface

namespace NRS
{
  namespace Base
  {
    class Target;
    class VariableManager;
  }
  namespace Message
  {
    class ReplyLanguage;

    /// A special variable type to handle QueryLanguage messages.
    /**
     *
     * The QueryLanguage class represents all message senders and receivers
     * which deal with QueryLanguage messages
     *
     **/
    class QueryLanguage : public NRS::Base::Variable

    {
    public:
      /// Constructor for QueryLanguage Variables.
      /**
       *
       * \param aName the name of the QueryLanguage Variable being built.
       *
       **/
      QueryLanguage( std::string aName );
      
      /// Destructor.
      virtual ~QueryLanguage();

      /// Send message to get a VNID from off component
      /**
       *
       * This virtual method tells the QueryVNIDManager to send a query
       * message
       *
       * \param theTarget the route through which to send the message
       * \param returnTarget the route via which to return the message
       * \param aMessageID id representing message, 0 if no data
       *
       **/
      virtual void sendMessage( Base::Target &theTarget,
			        Base::Target &returnTarget,
			        unsigned_t aMessageID );

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message. 
       *
       * \param returnTarget the route to send the message back along,
       * with the vnid of the target if available
       * \param aMessageID message ID, 0 if no data
       *
       **/
      virtual void receiveMessage( Base::Target &returnTarget,
				   unsigned_t aMessageID );

      /// resolve any dependency issues
      /**
       *
       * \returns whether all dependencies are resolved
       *
       **/
      virtual bool resolveComplexDependencies();
    protected:
      /// Pointer to ReplyLanguage variable, with which QueryLanguage
      /// communicates
      ReplyLanguage *iReplyLanguage;
    };
  }
}

#endif //ndef _QUERY_LANGUAGE_HH
