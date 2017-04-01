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
#include <string>

#include "VoidManager.hh"
#include "TrackerUpdater.hh"

namespace NRS
{
  namespace Tracker
  {
    /// The manager for the TrackerUpdater Units.
    /** 
     *
     * This is the class for the TrackerUpdater Manager
     *
     **/
    class TrackerUpdaterManager : public Type::VoidManager
    {
    public:
      /// Constructor.
      
      /// Default constructor.
      /**
       *
       * This is the constructor for the basic TrackerUpdaterManager.
       *
       **/
      TrackerUpdaterManager() :
        Type::VoidManager( "TrackerUpdater",
                     "Generic interface for trackers" )
      {
        iDescription = "Reference to Tracker Singleton.";
      }
      
      /// Constructor.
      /**
       * This is the constructor for a Manager derived from the
       * FloatManager.
       *
       * \param aMessageName Name of the Message type that will be
       * managed.
       * \param aDisplayName Prettified name of the Message type
       * (leave empty for same as aMessageName).
       **/
      TrackerUpdaterManager( std::string aMessageName, std::string aDisplayName ) :
        Type::VoidManager( aMessageName, aDisplayName )
      {
      }

      /// Destructor.
      virtual ~TrackerUpdaterManager()
      {
      }

      /// Repository for text string "TrackerUpdater"
      /**
       * \return the string "TrackerUpdater"
       **/
      static const char *getName()
      {
	static const char *name = "TrackerUpdater";
	return name;
      }

      /// Create a new Variable
      /**
       * \param aName name of new variable to create
       **/
      void createVariable( const std::string &aName ) const
      {
        new TrackerUpdater( aName );
      }
    };
  }
}

namespace
{
  NRS::Tracker::TrackerUpdaterManager sTrackerUpdaterM;
}

