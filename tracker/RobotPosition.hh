/*
 * Copyright (C) 2004-5 Richard Reeve, Matthew Szenher and Edinburgh University
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

#ifndef _ROBOTPOSITION_HH
#define _ROBOTPOSITION_HH

#pragma interface
#include <string>
#include "StringSegment.hh"
#include "Position.hh"
#include "TrackingData.hh"
#include "TrackerSingleton.hh"

namespace NRS
{
  namespace Tracker
  {
    class RobotPosition : public Position
    {
    public:
      /// Constructor
      /**
       * \param aName name of position
       **/
      RobotPosition( std::string aName ) : Position( aName ) {}
      
      /// Destructor
      virtual ~RobotPosition() {}

      void addReaction( integer_t aReference ) {}

      void react( integer_t aReference )
      {
        mLabel =  dynamic_cast< Type::StringSegment& >( Base::VariableNodeDirector::getDirector().
                  getVariable( iParentName + ".label" )->getOutputLink().
                  getSegment( 0 ) ).iValue;
        _WARN_("mLabel: " + mLabel );
      }
     
      /// Action to take every update cycle
      void doUpdate()
      {
        _WARN_("Setting Robot Position: " + mLabel );
        TrackingData data = TrackerSingleton::getTracker().getData( mLabel );
        _WARN_("Got data." );
        iOutX = data.x;
        iOutY = data.y;
        iOutHeading = data.heading;
        iOutTimeStep = data.timeStep;
        iOutFrame = data.frame;
        _WARN_("Set data." );
        iSendMessage = true;
      }
    };
  }
}

#endif //ndef _ROBOTPOSITION_HH
