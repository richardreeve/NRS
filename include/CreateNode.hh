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

#ifndef _CREATE_NODE_HH
#define _CREATE_NODE_HH

#pragma interface
#include <string>

#include "Target.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    /// A special variable type to handle CreateNode messages.
    /**
     *
     * The CreateNode class represents all message senders and receivers
     * which deal with CreateNode messages
     *
     **/
    class CreateNode : public Base::Variable
    {
    public:
      /// Constructor for CreateNode Variables.
      /**
       *
       * \param aName the name of the CreateNode Variable being built.
       *
       **/
      CreateNode( std::string aName );
      
      /// Destructor.
      virtual ~CreateNode();

      /// Send a CreateNode message
      /**
       *
       * \param theTarget target of message
       * \param nodeType name of node type
       * \param aVNID vnid of node to create (0 if not specified)
       * \param aName name of node to create
       *
       **/
      void createNode( Base::Target &theTarget,
		       const std::string &nodeType,
		       integer_t aVNID,
		       const std::string &aName );

    protected:
      /// New data is available to observe from a source
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doObserve( integer_t aRef );

      /// Incoming node type
      const std::string &iInNodeType;

      /// Incoming node vnid
      const integer_t &iInVNID;

      /// Incoming node name
      const std::string &iInNodeName;

      /// Outgoing node type
      std::string &iOutNodeType;

      /// Outgoing node vnid
      integer_t iOutVNID;

      /// Outgoing node name
      std::string &iOutNodeName;
    };
  }
}

#endif //ndef _CREATE_NODE_HH
