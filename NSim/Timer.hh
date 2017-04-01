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

#ifndef _TIMER_HH
#define _TIMER_HH

#pragma interface
#include <string>

#include "Node.hh"
#include "Types.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Base
  {
    class NodeFactory;
  }
  namespace Simulator
  {
    /// Class for timers, keeping track of system time in an experiment
    class Timer : public Base::Variable, public Base::Node
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of timer
       * \param aNF pointer to NodeFactory
       *
       **/
      Timer( std::string aName, const Base::NodeFactory *aNF );
      
      /// Destructor
      virtual ~Timer();


    protected:
      /// Reset the system to its initial state
      virtual void doReset();

      /// New data is available to observe from a source
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doObserve( integer_t aRef );

    private:
      /// Timestep of simulation
      const float_t &iInTimestep;

      /// Initial simulation time
      const float_t &iInSimTime;

      /// Timestep of simulation
      float_t &iOutTimestep;

      /// Initial simulation time
      float_t &iOutSimTime;
    };
  }
}

#endif //ndef _TIMER_HH
