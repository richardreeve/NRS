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

#ifndef _AXON_HH
#define _AXON_HH

#pragma interface

#include "Variable.hh"
#include "Node.hh"

#include "VariableManager.hh"
#include "Exception.hh"
#include "NodeFactory.hh"
#include "Time.hh"

namespace NRS
{
  namespace Base
  {
    class NodeFactory;
  }
  namespace Simulator
  {
    /// Class for Axon nodes, which are nodes which delay spikes on
    /// their way between neurons
    class Axon : public Unit::Time, public Base::Node
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of node to be created
       * \param aNFptr pointer to NodeFactory which created the Node
       *
       **/
      Axon( std::string aName, Base::NodeFactory *aNFptr ) :
	Unit::Time( aName, aNFptr->getType() ),
	Base::Node( aName, Base::Variable::getVNID(), aNFptr )
      {
	_TODO_( "Not getting defaults from NodeFactory" );
	iValue = 1;
      }
      
      /// Empty destructor
      virtual ~Axon()
      {
      }
      /// Receive a message
      virtual void receiveMessage( double aDelay )
      {
	_TODO_( "Not checking attribute restrictions" );
	iValue = aDelay;
	alertObservers();
	sendMessages();
	reset();
      }

      /// Reset the system to its initial state
      virtual void reset()
      {
	if (iVariableMap.find( "Delay" ) != iVariableMap.end())
	  {
	    dynamic_cast< Unit::Time* >( iVariableMap[ "Delay" ] )->
	      setDefault( iValue );
	  }
      }
    };
  }
}

#endif //ndef _AXON_HH
