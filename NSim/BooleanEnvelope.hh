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

#ifndef _BOOLEAN_ENVELOPE_HH
#define _BOOLEAN_ENVELOPE_HH

#pragma interface

#include "Node.hh"
#include "Variable.hh"
#include "VariableManager.hh"
#include "Exception.hh"
#include "NodeFactory.hh"
#include "Time.hh"
#include "Number.hh"

namespace NRS
{
  namespace Base
  {
    class NodeFactory;
  }
  namespace Simulator
  {
    /// Node type which creates a boolean envelope
    class BooleanEnvelope : public Base::Variable, public Base::Node
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of node to be created
       * \param aNFptr pointer to NodeFactory which created the Node
       *
       **/      
      BooleanEnvelope ( std::string aName, const Base::NodeFactory *aNFptr ) :
	Base::Variable( aName, aNF->getType() ), 
	Base::Node( aName, Base::Variable::getVNID(), aNF ),
	iTOffset( 0 ), iTLength( 10 )
      {
	_TODO_( "Not using attribute defaults" );
      }
      
      /// Empty destructor
      virtual ~BooleanEnvelope()
      {}

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message. Since
       * the Variable is a Number, the message is a double.
       *
       * \param
       **/
      virtual void receiveMessage( double aTOffset,
				   double aTLength  )
      {
        _TODO_( "Not checking attribute restrictions" );
	
	iTOffset = aTOffset;
	iTLength = aTLength;

	alertObservers();
	_TODO_( "Implement sendMessages in PT" );
	sendMessages();
        reset();
      }
      
      ///Reset the system to its initial state
      virtual void reset()
      {
	
        if (iVariableMap.find( "Offset" ) != iVariableMap.end())
          {
            dynamic_cast< Unit::Time* >( iVariableMap[ "Offset" ] )->
              setDefault( iTOffset );
          }

        if (iVariableMap.find( "Length" ) != iVariableMap.end())
          {
            dynamic_cast< Unit::Time* >( iVariableMap[ "Length" ] )->
              setDefault( iTLength );
          }
      }


      double iTOffset;
      double iTLength;
    };
  }
}

#endif //ndef _BOOLEAN_ENVELOPE_HH
