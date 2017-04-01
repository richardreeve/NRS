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

#ifndef _EVALUATOR_HH
#define _EVALUATOR_HH

#pragma interface
#include "Boolean.hh"
#include "Node.hh"
#include "NodeFactory.hh"
#include "VariableManager.hh"
#include "Void.hh"

namespace NRS
{
  namespace Base
  {
    class NodeFactory;
  }
  namespace Simulator
  {
    /// Node type which sends off an evaluation when a trigger is received
    class Evaluator : public Type::Void, public Base::Node
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of node to be created
       * \param aNFptr pointer to NodeFactory which created the Node
       *
       **/
      Evaluator( std::string aName, const Base::NodeFactory *aNFptr ) :
	Type::Void( aName, Base::VariableNodeDirector::getDirector().
		    getVariableManager( aNFptr->getType() ) ), 
	Base::Node( aName, Type::Void::getVNID(), aNFptr )
      {}
      
      /// Empty destructor
      virtual ~Evaluator()
      {}

    protected:
      /// Reset the system to its initial state
      virtual void doReset()
      {
        if (iVariableMap.find( "Trigger" ) != iVariableMap.end())
          {
            dynamic_cast< Type::Boolean* >( iVariableMap[ "Trigger" ] )->
              setDefault( false );
          }
      }

    };
  }
}

#endif //ndef _EVALUATOR_HH
