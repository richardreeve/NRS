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

#ifndef _RESET_MANAGER_HH
#define _RESET_MANAGER_HH

#include <iostream>
#include <string>
#include "VoidManager.hh"

#pragma interface

namespace NRS
{
  namespace Base
  {
    class Target;
  }
  namespace Message
  {
    class Reset;

    /// The manager for the Reset Variable type.
    /** 
     *
     * This is the base class for all managers of Variables which
     * reset the system
     *
     **/
    class ResetManager : public Type::VoidManager
    {
    public:
      /// Default constructor.
      /**
       *
       * This is the constructor for the basic ResetManager.
       *
       **/
      ResetManager();

      /// Destructor.
      virtual ~ResetManager()
      {
      }

      /// Set Description entries for CSL output
      virtual void setDescription();
      
      /// Create a new Variable
      /**
       *
       * \param aName name of new variable to create
       *
       **/
      virtual void createVariable( std::string aName );
    };
  }
}
#endif //ndef _RESET_MANAGER_HH
