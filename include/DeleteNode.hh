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

#ifndef _DELETE_NODE_HH
#define _DELETE_NODE_HH

#pragma interface
#include <string>

#include "Target.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    class DeleteNodeManager;
    /// A special variable type to handle DeleteNode messages.
    /**
     *
     * The DeleteNode class represents all message senders and receivers
     * which deal with DeleteNode messages
     *
     **/
    class DeleteNode : public Base::Variable
    {
    public:
      /// Constructor for DeleteNode Variables.
      /**
       *
       * \param aName the name of the DeleteNode Variable being built.
       *
       **/
      DeleteNode( std::string aName );
      
      /// Destructor.
      virtual ~DeleteNode();

      /// Send a DeleteNode message
      /**
       *
       * \param theTarget target of message
       * \param aVNID VNID of target
       *
       **/
      void deleteNode( Base::Target &theTarget, integer_t aVNID );

    protected:
      /// New data is available to observe from a source
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doObserve( integer_t aRef );

      /// Incoming VNID of deletion
      const integer_t &iInVNID;

      /// Outgoing VNID of deletion
      integer_t &iOutVNID;
    };
  }
}

#endif //ndef _DELETE_NODE_HH
