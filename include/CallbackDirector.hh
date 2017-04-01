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

#ifndef _CALLBACK_DIRECTOR_HH
#define _CALLBACK_DIRECTOR_HH

#include <vector>
#include "Exception.hh"
#include "Types.hh"

#pragma interface

namespace NRS
{

  namespace Base
  {
    class Callback;

    /// This class holds the framework within which NRS sits.
    /**
     * 
     * A single CallbackDirector, created at initialisation, holds all
     * of the callback information in system, and acts as the
     * framework within which Callbacks are created and managed.
     * 
     **/
    class CallbackDirector
    {
    public:
      /// static accessor for the CallbackDirector.
      /**
       *
       * \return A reference to the CallbackDirector.
       *
       **/
      static CallbackDirector &getDirector()
      {
	static CallbackDirector sCD;
	return sCD;
      }
    
      /// Register all Managers and Factories
      /// signed up with the system
      void registration();


      /// Check for a callback for this message id
      /**
       *
       * \param msgid id of message
       *
       * \returns whether a callback request exists
       *
       **/
      bool hasCallback( const unsigned_t msgid )
      {
	if (msgid >= iMsgIDCallbackVector.size())
	  return false;
	return ( iMsgIDCallbackVector[ msgid ] != NULL );
      }

      /// Get callback for this message id
      /**
       *
       * \param msgid id of message
       *
       * \returns callback requester
       *
       **/
      Callback *getCallback( const unsigned_t msgid )
      {
	return iMsgIDCallbackVector[ msgid ];
      }

      /// Remove callback for this message id
      /**
       *
       * \param msgid id of message
       *
       **/
      void removeCallback( const unsigned_t msgid )
      {
	iMsgIDCallbackVector[ msgid ] = NULL;
      }

      /// Remove callback for this message id
      /**
       *
       * \param aCallback the callback to be associated with this message
       *
       * \returns id for message
       *
       **/
      const unsigned_t addCallback( Callback *aCallback )
      {
	unsigned_t msgid = getNextMsgID();

	if (msgid >= iMsgIDCallbackVector.capacity() + 1)
	  iMsgIDCallbackVector.reserve( iMsgIDCallbackVector.capacity() * 2 );
	if (msgid >= iMsgIDCallbackVector.size() )
	  iMsgIDCallbackVector.resize( msgid + 1 );

	iMsgIDCallbackVector[ msgid ] = aCallback;
	return msgid;
      }


    private:

      /// Destructor
      /**
       *
       * This is a private destructor as no-one can create or destroy
       * CallbackDirectors.
       *
       **/
      ~CallbackDirector ()
      {
      }

      /// Constructor
      /**
       *
       * This is a private constructor as no-one can create or destroy
       * CallbackDirectors.
       *
       **/
      CallbackDirector() : iNextMsgID( 1 )
      {
      }

      /// Gets next msg id for an Callback
      /**
       *
       * This method gets the next free msg id for a Callback
       * being created.
       *
       **/
      unsigned_t getNextMsgID()
      {
	if (iNextMsgID >= UNSIGNED_T_MAX)
	  _ABORT_( "MsgID wraparound - cannot handle\n\t" );
	return iNextMsgID++;

      }

      /// The next free msg id to be allocated.
      unsigned_t iNextMsgID;

      /// A vector mapping a msg id onto its Callback pointer (if appropriate).
      std::vector< Callback* > iMsgIDCallbackVector;
    };
  }
}
#endif //ndef _CALLBACK_DIRECTOR_HH
