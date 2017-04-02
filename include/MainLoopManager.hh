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
 
#ifndef _MAIN_LOOP_MANAGER_HH
#define _MAIN_LOOP_MANAGER_HH

#include <string>

#include "VoidManager.hh"

namespace NRS
{
  namespace Message
  {
    /// This is the manager for MainLoop variables.
    /**
     *
     * The variables managed by this class send and receive messages
     * telling nodes to run themselves. This is the method that the
     * GeneralManager provides to allow components to connect. This
     * may for instance be a simulator node which will then connect
     * to the Updater and the MessageSender interfaces on all of the
     * simulated nodes.
     *
     **/
    class MainLoopManager : public Type::VoidManager
    {
    public:
      /// Default constructor
      MainLoopManager();

      /// Destructor
      virtual ~MainLoopManager()
      {
      }

      /// Create a new Variable
      /**
       *
       * \param aName name of new variable to create
       *
       **/
      virtual void createVariable( const std::string &aName ) const;
    };
  }
}

#endif //undef _MAIN_LOOP_MANAGER_HH
