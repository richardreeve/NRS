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

#ifndef _MESSAGE_SENDER_HH
#define _MESSAGE_SENDER_HH

#include <string>

#include "Void.hh"

namespace NRS
{
  namespace Message
  {
    /// This is a fundamental variable type which allows variables
    /// which update their internal state to send off their outputs
    class MessageSender : public Type::Void
    {
    public:
      /// A simple constructor
      /**
       *
       * \param aName name of variable
       *
       **/
      MessageSender( std::string aName );
      
      /// Default destructor
      virtual ~MessageSender()
      {}
    };
  }
}

#endif //ndef _MESSAGE_SENDER_HH
