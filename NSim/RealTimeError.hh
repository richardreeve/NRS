/*
 * Copyright (C) 2005 Richard Reeve, Matthew Szenher, Mark Payne and Edinburgh University
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

#ifndef _REAL_TIME_ERROR_HH
#define _REAL_TIME_ERROR_HH

#include <string>
#include <sys/time.h>
#include <unistd.h>

#include "Time.hh"
#include "Types.hh"

namespace NRS
{
  namespace Simulator
  {
    /// A specialisation of time, intended to be linked to SimTime within the NSim node to report any divergence from real time on a single time step
    class RealTimeError : public Unit::Time
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of variable to be constructed
       *
       **/
      RealTimeError( std::string aName);
      
      /// Empty destructor
      virtual ~RealTimeError();

      /// Note arrival of specialised connection - the real time error
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void addReaction( integer_t aReference );

      /// Real time error has been updated
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void react( integer_t aReference );

    protected:
      /// Remove link to observable
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doRemoveSource( integer_t aReference );

      /// Update state of RealTimeError variable
      virtual void doUpdate();

      /// RTE reference
      integer_t iSimTimeReference;

      /// Remote RTE value pointer (NULL if missing)
      float_t *iSimTimeErrorValuePtr;

      /// A local cache of the RTE
      float_t iSimTimeErrorValue;
    };
  }
}

#endif //ndef _SIM_TIME_HH
