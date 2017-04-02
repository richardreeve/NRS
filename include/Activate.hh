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

#ifndef _ACTIVATE_HH
#define _ACTIVATE_HH

#include <string>

#include "Boolean.hh"

namespace NRS
{
  namespace Message
  {
    /// This class is a specialisation of of the boolean type which
    /// will start a stop a component from running through the
    /// dependency mechanism when it receives a true or false message.
    /// The class is not a derived type of boolean, but a true boolean
    /// so that any boolean variable can connect to it.
    class Activate : public Type::Boolean
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of boolean variable to create
       *
       **/
      Activate( std::string aName );

      /// Empty destructor      
      virtual ~Activate();

    protected:
      /// New data is available to observe from a source
      /**
       *
       * If the variable receives a true it will satisfy a dependency,
       * if false, it will create one, and stop experiments from
       * starting up
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doObserve( integer_t aRef );

      /// resolve dependencies iff state is true
      virtual bool resolveComplexDependencies();
    };
  }
}

#endif //ndef _ACTIVATE_HH
