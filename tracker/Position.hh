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

#ifndef _POSITION_HH
#define _POSITION_HH

#include <string>

#include "Node.hh"
#include "Types.hh"
#include "Variable.hh"
#include "PositionManager.hh"
#include "FloatSegment.hh"
#include "IntegerSegment.hh"

namespace NRS
{
  namespace Tracker
  {
    class Position : public Base::Variable
    {
    public:
      /// Constructor
      /**
       * \param aName name of position
       **/
      Position( std::string aName ) :
        Variable( aName, Base::VariableNodeDirector::getDirector().
          getVariableManager( PositionManager::getName() ) ),
        iInX( dynamic_cast<Type::FloatSegment&>(iInputLink.getSegment(0)).iValue ),
        iInY( dynamic_cast<Type::FloatSegment&>(iInputLink.getSegment(1)).iValue ),
        iInHeading( dynamic_cast<Type::FloatSegment&>(iInputLink.getSegment(2)).iValue ),
        iInTimeStep( dynamic_cast<Type::IntegerSegment&>(iInputLink.getSegment(3)).iValue ),
        iInFrame( dynamic_cast<Type::IntegerSegment&>(iInputLink.getSegment(4)).iValue ),
        iOutX( dynamic_cast<Type::FloatSegment&>(iOutputLink.getSegment(0)).iValue ),
        iOutY( dynamic_cast<Type::FloatSegment&>(iOutputLink.getSegment(1)).iValue ),
        iOutHeading( dynamic_cast<Type::FloatSegment&>(iOutputLink.getSegment(2)).iValue ),
        iOutTimeStep( dynamic_cast<Type::IntegerSegment&>(iOutputLink.getSegment(3)).iValue ),
        iOutFrame( dynamic_cast<Type::IntegerSegment&>(iOutputLink.getSegment(4)).iValue )
      {

      }
      
      /// Destructor
      virtual ~Position() {}

      /// Action to take every update cycle
      void doUpdate() {}

    protected:
      std::string mLabel;

      const float_t &iInX;
      const float_t &iInY;
      const float_t &iInHeading;
      const integer_t &iInTimeStep;
      const integer_t &iInFrame;

      float_t &iOutX;
      float_t &iOutY;
      float_t &iOutHeading;
      integer_t &iOutTimeStep;
      integer_t &iOutFrame;

      /// Reset the system to its initial state
      void doReset() {}
    };
  }
}

#endif //ndef _POSITION_HH
