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

#ifndef _NODE_HH
#define _NODE_HH

#include <iostream>
#include <string>
#include <map>

#include "Exception.hh"
#include "VariableNodeDirector.hh"
#include "NodeFactory.hh"

#pragma interface

namespace NRS
{
  namespace Base
  {
    class Variable;

    /// This class is a simple container for Variables which do the work.
    /**
     *
     * This is the Node class; no derivation of this class is
     * necessary as it is a very simple container class.
     *
     **/
    class Node
    {
    public:
      /// Constructor.
      /**
       *
       * \param aName the name of the Node being created.
       * \param aNM a pointer to the NodeFactory for this node type.
       *
       **/
      Node( std::string aName, unsigned_t aVNID, const NodeFactory *aNM );

      /// Destructor.
      virtual ~Node();
      
      /// Accessor for the name of node.
      /**
       *
       * \return the name of the Node.
       *
       **/
      const std::string &getName()
      {
	return iName;
      }

      /// Accessor for the VNID of node.
      /**
       *
       * \return the VNID of the Node.
       *
       **/
      unsigned_t getVNID()
      {
	return iVNID;
      }

      /// Accessor for the NodeFactory.
      /**
       *
       * \return the NodeFactory for this Node.
       *
       **/
      const NodeFactory *getNodeFactory()
      {
	return iNF;
      }

      /// Add a Variable to this Node.
      /**
       *
       * \param aVar the pointer to the Variable to be added.
       *
       **/
      void addVariable( Variable *aVar );

      /// Resolve dependencies related to contents of a node
      /**
       *
       * \returns whether all issues related to contents of a node are
       * resolved
       *
       **/
      bool resolveContainsDependencies();

    protected:
      /// The (fully qualified) name of the Node.
      std::string iName;

      /// The VNID of the node.
      unsigned_t iVNID;

      /// A pointer to the NodeFactory for this Node.
      const NodeFactory *iNF;

      /// A map of names to Variables contained inside the Node.
      std::map< std::string, Variable* > iVariableMap;
    };
  }
}
#endif //ndef _NODE_HH
