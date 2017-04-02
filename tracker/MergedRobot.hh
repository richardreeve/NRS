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

#ifndef _MERGEDROBOT_HH
#define _MERGEDROBOT_HH

#include "Void.hh"
#include "Node.hh"
#include "NodeFactory.hh"
#include "VariableManager.hh"


namespace NRS
{
  namespace Tracker
  {
    class MergedRobot : public Type::Void, public Base::Node
    {
    public:
      /// A Node representing a robot's merged position
      MergedRobot( std::string aName, const Base::NodeFactory *aNF ) :
        Type::Void( aName, Base::VariableNodeDirector::getDirector().getVariableManager( aNF->getType() ) ),
        Base::Node( aName, Type::Void::getVNID(), aNF )
      {
      }

      /// Default destructor
      virtual ~MergedRobot() {};
    };
  }
}

#endif //ndef _MERGEDROBOT_HH
