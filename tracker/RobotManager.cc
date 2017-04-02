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

#include "TokenManager.hh"

namespace NRS
{
  namespace Tracker
  {
    class RobotManager : public Unit::TokenManager
    {
    public:
      /// Default constructor
      RobotManager() :
	Unit::TokenManager( "Robot", "Robot root variable" )
      {
	iDescription = "This variable/message type is the empty type for "
	  "configuration messages to the robot component";
        iSegmentDescription[0].setType( Enum::String, "token" );
        iSegmentDescription[0].setAttributeName( "label" );
      }

      /// Destructor
      virtual ~RobotManager()
      {
      }

      void doConfigure() {
      }
    };
  }
}

namespace
{
  NRS::Tracker::RobotManager sRobotM;
}


