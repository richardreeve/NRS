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
#include "Activate.hh"
#include "ArgumentDirector.hh"
#include "ArgumentFielder.hh"
#include "MainLoopMFO.hh"
#include "Tracker.hh"
#include "TrackerUpdater.hh"
#include "TrackerSingleton.hh"
#include "CameraSingleton.hh"
#include "TrackingData.hh"

namespace NRS
{
  namespace Tracker
  {
    class TrackerFactory : public Base::NodeFactory, public Base::ArgumentFielder
    {
    public:
      TrackerFactory() :
          Base::NodeFactory( "Tracker", "", true, true, 0, 20 )
      {
        Base::ArgumentDirector &theAD =
          Base::ArgumentDirector::getDirector();

        theAD.addArgumentFielder( 'T', "type",
                                  "\t\t[ -T|--type <trackertype> ]\n",
                                  "\t-T|--type <trackertype>\t\t"
                                  "The name of the tracker type to use\n",
                                  this );
      }

      virtual ~TrackerFactory() {}

      std::string queryFielder() const
      {
        return "TrackerFactory";
      }

      void fieldArguments( std::list< std::string > &arguments )
      {
        std::string arg = *arguments.begin();
        std::string trackerType;

        arguments.pop_front();
        if (arg == "type")
        {
          if (arguments.size() != 1)
          {
              _ABORT_( "Option --type requires exactly one argument\n\t" );
          }
          trackerType = *arguments.begin();

          // Create corresponding tracker class
          if ( trackerType == "camera" )
          {
            TrackerSingleton::setTracker( CameraSingleton::getTracker() );
          }
          else
          {
            _ABORT_("Tracker type '" << trackerType << "' not recognised. Available types are: camera");
          }
        }
        arguments.pop_front();
      }

      void setDescription()
      {
	iDescription = "The root node of a tracking device.";
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
        addVariable( "TrackerUpdater", "Tracker Interface", "TrackerUpdater", true, true,
                     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );

	anIDMap[ "in" ] = makeID( 0, -1, false );
	anIDMap[ "out" ] = makeID( 0, -1, true );
        aMFOGenMap.clear();
	addVariable( "Activate", "", "boolean", false, true,
		     anIDMap, aSegAttrRefMap, aSegConsMap, aMFOGenMap );
      }

      void setAttributes()
      {
	addAttribute( "name", "Name", "vnname", false, true,
		      false, true, false );
      }

      void setGroups() {}

      void setContents()
      {
        std::map< std::string, AttributeRestriction > anARMap;
        addContent( "Robot", 1, -1, anARMap );
      }

      bool doCreate( const std::string &nodeName ) const
      {
        // Check singleton has been created
        TrackerSingleton::getTracker();

        // Create tracker node
	new Tracker( nodeName, this );
        new TrackerUpdater( nodeName + ".TrackerUpdater" );
	new Message::Activate( nodeName + ".Activate" );

	return true;
      }
    };
  }
}

namespace {
  NRS::Tracker::TrackerFactory sTrackerF;
}

