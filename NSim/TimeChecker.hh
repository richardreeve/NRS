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

#ifndef _TIME_CHECKER_HH
#define _TIME_CHECKER_HH

#include "Time.hh"
#include "Boolean.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Variable type which compares two inputs and returns a boolean output
    class TimeChecker : public Type::Boolean
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of variable to be constructed
       * \param invert whether to invert the output
       *
       **/
      TimeChecker( std::string aName, bool invert );
      
      /// Empty destructor
      virtual ~TimeChecker();

      /// Note arrival of specialised connection - the timestep
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void addReaction( integer_t aReference );

      /// Timestep has changed
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void react( integer_t aReference );

    protected:
      /// Remove connection
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doRemoveSource( integer_t aReference );

      /// Reset the system to its initial state
      virtual void doReset();

      /// Update state TimeChecker boolean
      virtual void doUpdate();

      /// do we invert the output
      bool iInvert;

      /// SimTime reference
      integer_t iSTReference;

      /// Remote timestep value pointer (NULL if missing)
      float_t *iSimTimeValuePtr;

      /// A local cache of the timestep
      float_t iSimTimeValue;

      /// EndTime reference
      integer_t iETReference;

      /// Remote timestep value pointer (NULL if missing)
      float_t *iEndTimeValuePtr;

      /// A local cache of the timestep
      float_t iEndTimeValue;
    };
  }
}

#endif //ndef _TIME_CHECKER_HH
