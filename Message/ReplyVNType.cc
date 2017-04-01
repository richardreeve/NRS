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

#pragma implementation
#include "BooleanSegment.hh"
#include "Callback.hh"
#include "CallbackDirector.hh"
#include "IntegerSegment.hh"
#include "QueryVNType.hh"
#include "ReplyVNType.hh"
#include "RouteSegment.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"

NRS::Message::ReplyVNType::ReplyVNType( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::REPLYVNTYPE_VARIABLE() ) ),
  iInMsgID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						   getSegment( 0 ) ).
	    iValue ),
  iInVNType( dynamic_cast< Type::StringSegment& >( iInputLink.
						   getSegment( 1 ) ).
	     iValue ),
  iOutMsgID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
						    getSegment( 0 ) ).
	     iValue ),
  iOutVNType( dynamic_cast< Type::StringSegment& >( iOutputLink.
						    getSegment( 1 ) ).
	      iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::ReplyVNType::~ReplyVNType()
{
}

bool NRS::Message::ReplyVNType::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iQueryVNTypePtr)
    {
      if (theVND.hasVariable( Literals::Object::QUERYVNTYPE_VARIABLE() ) )
	{
	  iQueryVNTypePtr = dynamic_cast< QueryVNType* >
	    ( theVND.getVariable( Literals::Object::QUERYVNTYPE_VARIABLE() ) );
	}
    }
  return (iQueryVNTypePtr != NULL);
}

void NRS::Message::ReplyVNType::reply( Base::Target &theTarget,
				       unsigned_t aMessageID,
				       std::string aVNType )
{
  if (theTarget.isLocal())
    { // then the reply has arrived
      // cache it
      iQueryVNTypePtr->cache( aMessageID, aVNType );
      // and get the callback director
      Base::CallbackDirector &theCD =
	Base::CallbackDirector::getDirector();

      if ( theCD.hasCallback( aMessageID ) )
	{
	  // add the VNType to this variables messageID -> VNType map
	  iMessageIDVNTypeMap[ aMessageID ] = aVNType;
	  
	  // tell the variable that is expecting the just-received
	  // VNType that this VNType is available from the Reply variable
	  NRS::Base::Callback *theCallbackObj =
	    theCD.getCallback( aMessageID );
	  if (!theCallbackObj)
	    _WARN_( "No callback found for vnid reply\n\t" );
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
      iOutVNType = aVNType;

      if (theTarget.getVNID() == 0)
	{
	  Type::StringSegment tvnn;
	  tvnn.iValue = Literals::Object::REPLYVNTYPE_VARIABLE();
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

void NRS::Message::ReplyVNType::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      // get the callback director 
      Base::CallbackDirector &theCD =
	Base::CallbackDirector::getDirector();
      
      // check if there is a callback associated with this message id
      if (iInMsgID != 0) 
	{
	  // cache it
	  iQueryVNTypePtr->cache( iInMsgID, iInVNType );
	  // and deal with the callback
	  if ( theCD.hasCallback( iInMsgID ) )
	    {
	      // add the VNType to this variables messageID -> VNType map
	      iMessageIDVNTypeMap[ iInMsgID ] = iInVNType;
	      
	      // tell the variable that is expecting the just-received
	      // VNType that this VNType is available from the Reply variable
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
      iOutVNType = iInVNType;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}
