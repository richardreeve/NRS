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

#ifndef _DELETE_LINK_HH
#define _DELETE_LINK_HH

#include <string>

#include "Target.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    /// A special variable type to handle DeleteLink messages.
    /**
     *
     * The DeleteLink class represents all message senders and receivers
     * which deal with DeleteLink messages
     *
     **/
    class DeleteLink : public Base::Variable
    {
    public:
      /// Constructor for DeleteLink Variables.
      /**
       *
       * \param aName the name of the DeleteLink Variable being built.
       *
       **/
      DeleteLink( std::string aName );
      
      /// Destructor.
      virtual ~DeleteLink();

      /// Send a DeleteLink message
      /**
       *
       * \param theTarget target of message
       * \param sourceNotTarget whether the message is directed at the
       * source or the target
       * \param sCID source CID
       * \param sVNID source VNID
       * \param tCID target CID
       * \param tVNID target VNID
       *
       **/
      void deleteLink( Base::Target &theTarget, bool sourceNotTarget,
		       const std::string &sCID,
		       integer_t sVNID, const std::string &tCID,
		       integer_t tVNID );

    protected:
      /// New data is available to observe from a source
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doObserve( integer_t aRef );

      /// Incoming source not target info
      const bool &iInSourceNotTarget;

      /// Incoming cid of source
      const std::string &iInSourceCID;

      /// Incoming VNID of source
      const integer_t &iInSourceVNID;

      /// Incoming cid of target
      const std::string &iInTargetCID;

      /// Incoming VNID of target
      const integer_t &iInTargetVNID;

      /// Outgoing source not target info
      bool &iOutSourceNotTarget;

      /// Outgoing cid of source
      std::string &iOutSourceCID;

      /// Outgoing VNID of source
      integer_t &iOutSourceVNID;

      /// Outgoing cid of target
      std::string &iOutTargetCID;

      /// Outgoing VNID of target
      integer_t &iOutTargetVNID;
    };
  }
}

#endif //ndef _DELETE_LINK_HH
