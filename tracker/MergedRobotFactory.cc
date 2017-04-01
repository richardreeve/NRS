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


#pragma implementation
#include <map>
#include <string>

#include "NodeFactory.hh"
#include "MergedRobot.hh"
#include "MergedRobotPosition.hh"
#include "MainLoopMFO.hh"
#include "SpecialisedMFO.hh"
#include "Token.hh"
#include "StringSegment.hh"

namespace NRS
{
  namespace Tracker 
  {
    class MergedRobotFactory : public Base::NodeFactory
    {
    public:
      MergedRobotFactory() :
        Base::NodeFactory( "MergedRobot", "", true, true, 0, 0 )
      {}

      virtual ~MergedRobotFactory() {}

      void setDescription()
      {
	iDescription = "Creates a robot with merged position data.";
      }

      void setVariables()
      {
	std::map< std::string, InterfaceDescription > anIDMap;
	std::map< std::string, std::string > aSegAttrRefMap;
	std::map< std::string, SegmentConstraint > aSegConsMap;
        std::map< std::string, Base::MFOGenerator* > aMFOGenMap;

        anIDMap[ "in" ] = makeID( 0, -1, false );
        anIDMap[ "out" ] = makeID( 0, -1, true );
        anIDMap[ "log" ] = makeID( 0, 0, false );

        addVariable( "MainLoop", "Main Loop", "MainLoop", false, false,
                     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

        anIDMap[ "out" ] = makeID( 0, 0, false );
        aMFOGenMap["MainLoop"] = new Message::MainLoopMFOGenerator();
        addVariable( "MergedRobotPosition", "Merged Position Data", "Position", true, true,
                     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

        aMFOGenMap.clear();
        addVariable( "label", "Label", "token", false, true,
                     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
      }

      void setAttributes()
      {
	addAttribute( "name", "Name", "vnname",
                      false, true, false, true, false );
        Type::StringSegment *anInitSegPtr = new Type::StringSegment();
        anInitSegPtr->iValue = "unset";
        addAttribute( "label", "Label", "token",
                      false, false, true, false, true,
                      anInitSegPtr );
      }

      void setGroups() {}

      void setContents() {}

      bool doCreate( const std::string &nodeName ) const
      {
	new MergedRobot( nodeName, this );
        new MergedRobotPosition( nodeName + ".MergedRobotPosition" );

	return true;
      }

    };
  }
}

namespace {
  NRS::Tracker::MergedRobotFactory sRobotF;
}

