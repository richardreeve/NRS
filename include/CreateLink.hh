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

#ifndef _CREATE_LINK_HH
#define _CREATE_LINK_HH

#pragma interface
#include <string>

#include "Callback.hh"
#include "Location.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    /// A special variable type to handle CreateLink messages.
    /**
     *
     * The CreateLink class represents all message senders and receivers
     * which deal with CreateLink messages
     *
     **/
    class CreateLink : public Base::Variable, public Base::Callback
    {
    public:
      /// Constructor for CreateLink Variables.
      /**
       *
       * \param aName the name of the CreateLink Variable being built.
       *
       **/
      CreateLink( std::string aName );
      
      /// Destructor.
      virtual ~CreateLink();

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
       * \param temporary is the link temporary?
       *
       **/
      void createLink( Base::Target &theTarget,
		       bool sourceNotTarget, const std::string &sCID,
		       integer_t sVNID, const std::string &tCID,
		       integer_t tVNID, bool temporary );

      /// Handle a callback
      /**
       *
       * \param msgID the message id the callback refers to
       *
       **/
      virtual void callback( const unsigned_t msgID );

      /// Query whether object is waiting for callbacks
      /**
       *
       * \returns whether any callbacks are outstanding
       *
       **/      
      virtual bool queryWaiting();

    protected:
      /// New data is available to observe from a source
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doObserve( integer_t aRef );

      /// Class for temporary storage of callback-stalled data
      class LinkData
      {
      public:
	/// Constructor
	/**
	 *
	 * \param sourceNotTarget is the local variable the source or
	 * the target of the link
	 * \param aVNID the vnid of the local end of the link
	 * \param theOther the Location of the other end of the link
	 * \param temporary is the link temporary?
	 *
	 **/
	LinkData( bool sourceNotTarget, integer_t aVNID,
		  const Base::Location &theOther,
		  bool temporary ) :
	  iSNT( sourceNotTarget ),
	  iLocalVNID( aVNID ),
	  iRemote( theOther ),
	  iTemp( temporary )
	{}

	/// Default constructor
	LinkData() : iRemote( std::string(), getEmptyRoute(), 0 )
	{}

	/// Source not target of link
	bool iSNT;

	/// VNID of local end of link
	integer_t iLocalVNID;

	/// other end of link
	Base::Location iRemote;

	/// Is connection temporary?
	bool iTemp;
      };

      /// resolve complex dependencies of derived classes
      virtual bool resolveComplexDependencies();

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

      /// Incoming is the link temporary?
      const bool &iInTemporary;

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

      /// Outgoing is the link temporary?
      bool &iOutTemporary;

      /// A map of callback ids against incomplete link route information
      std::map< unsigned_t, LinkData > iCallbackRouteLinkMap;

      /// A map of callback ids against incomplete link type information
      std::map< unsigned_t, LinkData > iCallbackTypeLinkMap;
    };
  }
}

#endif //ndef _CREATE_LINK_HH
