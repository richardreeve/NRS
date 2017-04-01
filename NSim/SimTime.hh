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

#ifndef _SIM_TIME_HH
#define _SIM_TIME_HH

#pragma interface
#include <string>
#include <sys/time.h>
#include <unistd.h>

#include "Time.hh"
#include "Types.hh"

namespace NRS
{
  namespace Simulator
  {
    /// A specialisation of time, which counts the current simulation
    /// time of the system, and if desired, locks the system to real-time
    class SimTime : public Unit::Time
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of variable to be constructed
       * \param realTime do we lock to real-time?
       *
       **/
      SimTime( std::string aName, bool realTime );
      
      /// Empty destructor
      virtual ~SimTime();

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

      ///Access pointer to real-time error value
      float_t * getRealTimeErrorValuePtr();

    protected:
      /// Remove timestep from 
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doRemoveSource( integer_t aReference );

      /// Reset the system to its initial state
      virtual void doReset();

      /// Update state of SimTime variable, locking to real-time if necessary
      virtual void doUpdate();

      /// Timestep reference
      integer_t iTReference;

      /// Remote timestep value pointer (NULL if missing)
      float_t *iTimestepValuePtr;

      /// A local cache of the timestep
      float_t iTimestepValue;

      /// RealTime Error on this timestep
      float_t iRealTimeErrorOnStep;

      /// Cached value of Realtime Error at last loop
      long iLastLoopRealTimeError;

      /// Predicted time
      struct timeval iStartTV;

      /// Is this timer locked to real time?
      bool iRealTime;
    };
  }
}

#endif //ndef _SIM_TIME_HH
