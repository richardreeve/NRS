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

#ifndef _CALLBACK_HH
#define _CALLBACK_HH

#include <list>
#include <string>

#include "Exception.hh"
#include "Types.hh"

#pragma interface

namespace NRS
{
  namespace Base
  {
    /// This class is the base class for all elements which require
    /// callbacks
    class Callback
    {
    public:
      /// Dummy Destructor
      virtual ~Callback()
      {
      }

      /// Handle a callback
      /**
       *
       * \param msgID the message id the callback refers to
       *
       **/
      virtual void callback( const unsigned_t msgID ) = 0;

      /// Query whether object is waiting for callbacks
      /**
       *
       * \returns whether any callbacks are outstanding
       *
       **/      
      virtual bool queryWaiting() = 0;
    };
  }
}

#endif //ndef _CALLBACK_HH
