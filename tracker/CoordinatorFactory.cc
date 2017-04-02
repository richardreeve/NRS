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


#include <map>
#include <string>

#include "NodeFactory.hh"
#include "Coordinator.hh"
#include "MainLoopMFO.hh"
#include "SpecialisedMFO.hh"
#include "Position.hh"
#include "Token.hh"

namespace NRS
{
  namespace Tracker 
  {
    class CoordinatorFactory : public Base::NodeFactory
    {
    public:
      CoordinatorFactory() :
        Base::NodeFactory( "Coordinator", "", true, true, 0, 1 )
      {}

      virtual ~CoordinatorFactory() {}

      void setDescription()
      {
	iDescription = "Shows merged robot locations.";
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
      }

      void setAttributes()
      {
	addAttribute( "name", "Name", "vnname",
                      false, true, false, true, false );
      }

      void setGroups() {}

      void setContents()
      {
        std::map< std::string, AttributeRestriction > anARMap;
        addContent( "MergedRobot", 1, -1, anARMap );
      }

      bool doCreate( const std::string &nodeName ) const
      {
	new Coordinator( nodeName, this );

	return true;
      }

    };
  }
}

namespace {
  NRS::Tracker::CoordinatorFactory sCoordinatorF;
}

