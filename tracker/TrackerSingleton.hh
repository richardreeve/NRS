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

#ifndef _TRACKERSINGLETON_HH
#define _TRACKERSINGLETON_HH

#pragma interface
#include <iostream>
#include <string>
#include <map>
#include "StringLiterals.hh"
#include "TrackingData.hh"

/// The namespace for all NRS components.
namespace NRS
{
  namespace Tracker
  {
    /**
     * A class that holds a reference to a tracker and its data.
     */
    class TrackerSingleton
    {
    private:
      /// A reference to the subclass being used to obtain tracking data.
      static TrackerSingleton* sTrackerSingleton;

    protected:
      /// Constructor
      /**
       * This is a private constructor as no-one can create or destroy
       * TrackerSingletons.
       */
      TrackerSingleton() {}

      /// Destructor
      /**
       * This is a private destructor as no-one can create or destroy
       * TrackerSingletons.
       */
      virtual ~TrackerSingleton() {}

      /// The most recent tracking data given by this tracker.
      std::map< std::string, TrackingData* > mTrackingDataMap;

    public:
      /// Sets the type of tracker to use.
      static void setTracker( TrackerSingleton& tracker ) {
        sTrackerSingleton = &tracker;
      }

      /// Gets a reference to the tracker singleton.
      static TrackerSingleton& getTracker()
      {
        if (sTrackerSingleton == NULL)
        {
          _ABORT_("Attempting to use uninitialised TrackerSingleton");
        }
        return *sTrackerSingleton;
      }

      /// Get a copy of the latest data for a specific robot.
      /**
       * Gets the X, Y position, heading, time step and frame for the
       * specified robot.
       */
      TrackingData& getData( std::string robotLabel )
      {
        return *(mTrackingDataMap[ robotLabel ]);
      }

      /// Update the tracking data with new information from the tracker.
      /**
       * This method must be overridden to get data from a specific
       * tracking device.
       */
      virtual void update()
      {
        _ABORT_("Abstract method update called in base class TrackerSingleton.");
      }

    };
  }
}
#endif //ndef _TRACKERSINGLETON_HH
