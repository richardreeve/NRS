/*
 * Copyright (C) 2005 Richard Reeve and Edinburgh University
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

#ifndef _UPDATER_MFO_HH
#define _UPDATER_MFO_HH

#pragma interface
#include "MessageFunctionObject.hh"

namespace NRS
{
  namespace Base
  {
    class Variable;
  }
  namespace Message
  {
    /// This class provides an extension to a standard variable
    /// without requiring a new class to be built; the extension
    /// allows the Variable to correctly handle incoming connections
    /// from Updater variables (by calling update()).
    class UpdaterMFO : public Base::MessageFunctionObject
    {
    public:
      /// Default constructor
      /**
       *
       * \param sourceName name of source of link
       * \param targetPtr pointer to target of link
       *
       **/
      UpdaterMFO( std::string sourceName, Base::Variable *targetPtr );

      /// Default destructor
      virtual ~UpdaterMFO();

      /// Call update() on Variable
      virtual void operator()();
    };

    /// Generator for UpdaterMFOs
    class UpdaterMFOGenerator : public Base::MFOGenerator
    {
    public:
      /// Empty constructor
      UpdaterMFOGenerator();

      /// Empty destructor
      virtual ~UpdaterMFOGenerator();

      /// Create a new instance of the UpdaterMFO
      /**
       *
       * \param sourceName name of source of link
       * \param targetPtr pointer to target of link
       *
       * \return a new UpdaterMFO, ownership of which belongs to the
       * caller.
       *
       **/
      virtual Base::MessageFunctionObject *
      createMFO( std::string sourceName, Base::Variable *targetPtr );
    };
  }
}

#endif //ndef _UPDATER_MFO_HH
