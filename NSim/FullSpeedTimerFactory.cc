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
#include "TimerFactory.hh"
#include "SimTime.hh"
#include "Timer.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Factory for timers running at full speed, not constrained by
    /// real-time
    class FullSpeedTimerFactory : public TimerFactory
    {
    public:
      /// Empty constructor
      FullSpeedTimerFactory() : TimerFactory( "FullSpeedTimer", "" )
      {
      }

      /// Empty destructor
      virtual ~FullSpeedTimerFactory()
      {
      }

    protected:
      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription()
      {
	iDescription = "The timer loop which connects to the main "
	  "loop of the simulator, running as fast as possible";
      }

      /// Create the variables and node for a node
      /**
       *
       * Any non-standard variables, which is to say, any
       * self-updating variables in general, have to be created by the
       * factory itself - they are created here, which is called by
       * createNode before configuring them. Any variables not created
       * here are created in createNode. Variables and Nodes are just
       * created using new, there is no need to do anything with the
       * pointers generated as they register themselves automatically
       * with the VariableNodeDirector.
       *
       * \param nodeName node of name to create
       *
       * \return success of operation
       *
       **/
      virtual bool doCreate( const std::string &nodeName ) const
      {
	new Timer( nodeName, this );
	new SimTime( nodeName + ".SimTime", false );

	return true;
      }
    };
  }
}

namespace
{
  NRS::Simulator::FullSpeedTimerFactory sFSTF;
}
