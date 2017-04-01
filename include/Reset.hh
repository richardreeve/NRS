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

#ifndef _RESET_HH
#define _RESET_HH

#include <string>
#include "Void.hh"

#pragma interface

namespace NRS
{
  namespace Message
  {
    class ResetManager;
    /// A special variable type to handle Reset messages.
    /**
     *
     * The Reset class represents all message senders and receivers
     * which deal with Reset messages
     *
     **/
    class Reset : public Type::Void
    {
    public:
      /// Constructor for Reset Variables.
      /**
       *
       * \param aName the name of the Reset Variable being built.
       *
       **/
      Reset( std::string aName );
      
      /// Destructor.
      virtual ~Reset();

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message.
       *
       **/
      virtual void receiveMessage();

      /// Send a Reset message.
      /**
       *
       * \param theTarget the route for the reply to follow
       *
       **/
      virtual void sendMessage( Base::Target &theTarget );

    };
  }
}

#endif //ndef _RESET_HH
