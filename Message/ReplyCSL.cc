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

#pragma implementation
#include <string>

#include "BooleanSegment.hh"
#include "Callback.hh"
#include "CallbackDirector.hh"
#include "IntegerSegment.hh"
#include "ReplyCSL.hh"
#include "ReplyCSLManager.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"

NRS::Message::ReplyCSL::ReplyCSL( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( NRS::Literals::Object::REPLYCSL_VARIABLE() ) ),
  iInMsgID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						   getSegment( 0 ) ).
	    iValue ),
  iInCSL( dynamic_cast< Type::StringSegment& >( iInputLink.
						getSegment( 1 ) ).
	  iValue ),
  iOutMsgID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
						    getSegment( 0 ) ).
	     iValue ),
  iOutCSL( dynamic_cast< Type::StringSegment& >( iOutputLink.
						 getSegment( 1 ) ).
	   iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::ReplyCSL::~ReplyCSL()
{
}

void NRS::Message::ReplyCSL::reply( Base::Target &theTarget,
				    integer_t aMessageID,
				    const std::string &aCSL )
{
  if (theTarget.isLocal())
    { // then the reply has arrived
      // get the callback director
      Base::CallbackDirector &theCD =
	Base::CallbackDirector::getDirector();
      
      if ( theCD.hasCallback( aMessageID ) )
	{
	  // add the CSL to this variables messageID -> CSL map
	  iMessageIDCSLMap[ aMessageID ] = aCSL;
	  
	  // tell the variable that is expecting the just-received
	  // CID that this CID is available from the Reply variable
	  NRS::Base::Callback *theCallbackObj =
	    theCD.getCallback( aMessageID );
	  if (!theCallbackObj)
	    _WARN_( "No callback found for CSL reply\n\t" );
	  else
	    theCallbackObj->callback( aMessageID );
	}
    }
  else
    { // Send it off
      // Add target
      iOutputLink.addTarget( theTarget );

      // create message
      iOutMsgID = aMessageID;
      iOutCSL = aCSL;

      if (theTarget.getVNID() == 0)
	{
	  Type::StringSegment tvnn;
	  tvnn.iValue = Literals::Object::REPLYCID_VARIABLE();
	  iOutputLink.setIntelligentSegment( Enum::TargetVNName, &tvnn );
	}

      if (!theTarget.getCID().empty())
	{
	  Type::StringSegment cid;
	  cid.iValue = theTarget.getCID();
	  iOutputLink.setIntelligentSegment( Enum::TargetCID, &cid );
	  Type::BooleanSegment isB;
	  isB.iValue = true;
	  iOutputLink.setIntelligentSegment( Enum::IsBroadcast, &isB );
	  Type::IntegerSegment hc;
	  hc.iValue = 9;
	  iOutputLink.setIntelligentSegment( Enum::HopCount, &hc );
	}

      // Send message
      iSendMessage = true;
      update();
      sendMessages();
      iOutputLink.clearIntelligentSegments();

      // and remove target
      iOutputLink.removeTarget( theTarget );
      
    }
}

void NRS::Message::ReplyCSL::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      // get the callback director 
      Base::CallbackDirector &theCD =
	Base::CallbackDirector::getDirector();
      
      // check if there is a callback associated with this message id
      if (iInMsgID != 0) 
	{
	  if ( theCD.hasCallback( iInMsgID ) )
	    {
	      // add the CSL to this variables messageID -> CSL map
	      iMessageIDCSLMap[ iInMsgID ] =  iInCSL;
	      
	      // tell the variable that is expecting the just-received
	      // CID that this CID is available from the Reply variable
	      NRS::Base::Callback *theCallbackObj =
		theCD.getCallback( iInMsgID );
	      if (!theCallbackObj)
		_WARN_( "No callback found for vnid reply\n\t" );
	      else
		theCallbackObj->callback( iInMsgID );
	    }
	}

      // and send on to anyone connected...
      iOutMsgID = iInMsgID;
      iOutCSL = iInCSL;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}
