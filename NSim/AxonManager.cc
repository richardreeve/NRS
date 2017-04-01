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

#include <iostream>
#include <string>

#include "TimeManager.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Manager for Axon nodes, which delay spikes in transmission
    class AxonManager : public Unit::TimeManager
    {
    public:
      /// Default constructor
      AxonManager() :
	Unit::TimeManager( "Axon",
			   "Fixed Axon for neural simulator" )
      {
      }

      /// Destructor
      virtual ~AxonManager()
      {
      }

      /// Setup segment records.
      /**
       *
       * This method sets up the segment descriptions for the CSL
       * output
       *
       **/
      virtual void setSegments()
      {
	Unit::TimeManager::setSegments();
	iSegNameVector[ 0 ] = "t_delay";

      }

      /// Set Description entries for CSL output
      virtual void setDescription()
      {
	iDescription = "This variable/message type is for an axon "
	  "of the simulation";
      }
    };
  }
}

namespace
{
  NRS::Simulator::AxonManager sAM;
}
