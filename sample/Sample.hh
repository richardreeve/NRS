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

#ifndef _SAMPLE_HH
#define _SAMPLE_HH

#pragma interface
#include "Node.hh"
#include "NodeFactory.hh"
#include "VariableManager.hh"
#include "Void.hh"

namespace NRS
{
  /// Namespace for elements of a sample component
  namespace Sample
  {
    /// Class for root Node of sample component
    class Sample : public Type::Void, public Base::Node
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of node to be created
       * \param aNFptr pointer to NodeFactory which created the Node
       *
       **/
      Sample( std::string aName, const Base::NodeFactory *aNFptr ) :
	Type::Void( aName, Base::VariableNodeDirector::getDirector().
		    getVariableManager( aNFptr->getType() ) ), 
	Base::Node( aName, Type::Void::getVNID(), aNFptr )
      {
      }
      
      /// Empty destructor
      virtual ~Sample()
      {}
    };
  }
}

#endif //ndef _SAMPLE_HH
