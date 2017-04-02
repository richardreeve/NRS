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

#ifndef _TRACKERUPDATER_HH
#define _TRACKERUPDATER_HH

#include <string>
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"
#include "Void.hh"
#include "StringLiterals.hh"
#include "TrackerSingleton.hh"

namespace NRS
{
  namespace Tracker
  {
    /// A variable type for time message handlers.
    /**
     * The TrackerUpdater class represents all message senders and receivers
     * which send messages consisting of one time
     **/
    class TrackerUpdater : public Type::Void
    {
    public:
      /// Constructor for TrackerUpdater Variables.
      /**
       * \param aName the name of the TrackerUpdater Variable being built.
       **/
      TrackerUpdater( std::string aName ) :
        Type::Void( aName, Base::VariableNodeDirector::getDirector().
                      getVariableManager( Literals::Object::UPDATER_VARIABLE() ) )
      {}
      
      /// Destructor.
      virtual ~TrackerUpdater() {}

      /// Action to take every update cycle
      void doUpdate()
      {
        TrackerSingleton::getTracker().update();
      }

    };
  }
}

#endif //ndef _TRACKERUPDATER_HH
