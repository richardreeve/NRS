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

#ifndef _ROBOT_HH
#define _ROBOT_HH

#pragma interface
#include "Node.hh"
#include "NodeFactory.hh"
#include "VariableManager.hh"
#include "Token.hh"
#include <string>
#include <iostream>


namespace NRS
{
  namespace Tracker
  {
    class Robot : public Unit::Token, public Base::Node
    {
    public:
      /// A Node representing a robot's position and configuration
      Robot( std::string aName, const Base::NodeFactory *aNF ) :
        Unit::Token( aName, Base::VariableNodeDirector::getDirector().getVariableManager( aNF->getType() ) ),
        Base::Node( aName, Unit::Token::getVNID(), aNF )
      {
      }

      /// Default destructor
      virtual ~Robot() {};

      void doReset()
      {
        if (iVariableMap.find( "label" ) != iVariableMap.end())
        {
          dynamic_cast< Unit::Token* >( iVariableMap[ "label" ] )->
          setDefault( iValue );
        }
      }

      void doObserve( integer_t aRef )
      {
        if (aRef == 0)
        {
          iSendMessage = true;
          iValue = *iInput[0];
          reset();
        }
        else
        _ABORT_( "Don't know how to handle incoming links" );
      }
    };
  }
}

#endif //ndef _ROBOT_HH
