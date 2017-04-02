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

#ifndef _MAIN_LOOP_HH
#define _MAIN_LOOP_HH

#include <string>

#include "Void.hh"

namespace NRS
{
  namespace Message
  {
    /// This is the fundamental variable type which the component will
    /// call when it is created to run any given program
    class MainLoop : public Type::Void
    {
    public:
      /// A simple constructor
      /**
       *
       * \param aName name of variable
       *
       **/
      MainLoop( std::string aName );
      
      /// Empty destructor
      virtual ~MainLoop()
      {}

      /// Activate the main loop
      void mainLoop();
    };
  }
}

#endif //ndef _MAIN_LOOP_HH
