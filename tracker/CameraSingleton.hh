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

#ifndef _CAMERASINGLETON_HH
#define _CAMERASINGLETON_HH

#include <iostream>
#include <string>
#include <map>
#include "TrackingData.hh"
#include "TrackerSingleton.hh"

/// The namespace for all NRS components.
namespace NRS
{
  namespace Tracker
  {
    /**
     * A class that holds a reference to a camera tracker and its data.
     */
    class CameraSingleton : public TrackerSingleton
    {
    private:
      /// Constructor
      /**
       * This is a protected constructor so only the TrackerSingleton
       * can create the CameraSingleton.
       **/
      CameraSingleton(){}

      /// Destructor
      /**
       * This is a private destructor as no-one can destroy
       * CameraSingletons.
       **/
      virtual ~CameraSingleton(){}

    public:
      /// Gets a reference to this tracker singleton.
      static CameraSingleton &getTracker()
      {
        static CameraSingleton camera;
        return camera;
      }

      /// Update the tracking data with new information from the tracker.
      void update()
      {
        _WARN_("Updating camera");
        mTrackingDataMap["Koala1"] = new TrackingData(1.11,55.4,12.5,5,30);
      }

    };
  }
}
#endif //ndef _CAMERASINGLETON_HH
