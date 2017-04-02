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

#ifndef _SPECIALISED_MFO_HH
#define _SPECIALISED_MFO_HH

#include "MessageFunctionObject.hh"

namespace NRS
{
  namespace Base
  {
    class Variable;
    /// This class provides an dummy extension to a variable where the
    /// variable does require a new class to be built; the extension
    /// allows the Variable to correctly handle incoming connections
    /// of specialised types which need to be handled by the
    /// variable's overriden doReact() method
    class SpecialisedMFO : public Base::MessageFunctionObject
    {
    public:
      /// Constructor
      /**
       *
       * \param sourceName name of source of link
       * \param targetPtr pointer to target of link
       *
       **/
      SpecialisedMFO( std::string sourceName, Variable *targetPtr );

      /// Default destructor
      virtual ~SpecialisedMFO();

      /// Call react() on Variable
      virtual void operator()();

    protected:
      /// Call addReact() on Variable
      virtual void doReset();
    };

    /// Generator for SpecialisedMFOs
    class SpecialisedMFOGenerator : public Base::MFOGenerator
    {
    public:
      /// Empty constructor
      SpecialisedMFOGenerator();

      /// Empty destructor
      virtual ~SpecialisedMFOGenerator();

      /// Create a new instance of the SpecialisedMFO
      /**
       *
       * \param sourceName name of source of link
       * \param targetPtr pointer to target of link
       *
       * \return a new SpecialisedMFO, ownership of which belongs to the
       * caller.
       *
       **/
      virtual MessageFunctionObject *createMFO( std::string sourceName,
						Variable *targetPtr );
    };
  }
}

#endif //ndef _SPECIALISED_MFO_HH
