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

#include <string>

#include "BooleanSegment.hh"
#include "Callback.hh"
#include "CallbackDirector.hh"
#include "IntegerSegment.hh"
#include "ReplyCType.hh"
#include "ReplyCTypeManager.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"

NRS::Message::ReplyCType::ReplyCType( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::REPLYCTYPE_VARIABLE() ) ),
  iInMsgID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						   getSegment( 0 ) ).
	    iValue ),
  iInCType( dynamic_cast< Type::StringSegment& >( iInputLink.
						  getSegment( 1 ) ).
	  iValue ),
  iInCVersion( dynamic_cast< Type::StringSegment& >( iInputLink.
						     getSegment( 2 ) ).
	       iValue ),
  iOutMsgID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
						    getSegment( 0 ) ).
	     iValue ),
  iOutCType( dynamic_cast< Type::StringSegment& >( iOutputLink.
						   getSegment( 1 ) ).
	     iValue ),
  iOutCVersion( dynamic_cast< Type::StringSegment& >( iOutputLink.
						      getSegment( 2 ) ).
		iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::ReplyCType::~ReplyCType()
{
}

void NRS::Message::ReplyCType::reply( Base::Target &theTarget,
				      integer_t aMessageID,
				      const std::string &aCType,
				      const std::string &aCVersion )
{
  if (theTarget.isLocal())
    { // then the reply has arrived
      // get the callback director
      Base::CallbackDirector &theCD =
	Base::CallbackDirector::getDirector();
      
      if ( theCD.hasCallback( aMessageID ) )
	{
	  // add the CType to this variables messageID -> CType map
	  iMessageIDCTypeMap[ aMessageID ] = 
	    std::pair< std::string, std::string >( aCType, aCVersion );
	  
	  // tell the variable that is expecting the just-received
	  // CID that this CID is available from the Reply variable
	  NRS::Base::Callback *theCallbackObj =
	    theCD.getCallback( aMessageID );
	  if (!theCallbackObj)
	    _WARN_( "No callback found for CType reply\n\t" );
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
      iOutCType = aCType;
      iOutCVersion = aCVersion;

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

void NRS::Message::ReplyCType::doObserve( integer_t aRef )
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
	      // add the CType to this variables messageID -> CType map
	      iMessageIDCTypeMap[ iInMsgID ] = 
		std::pair< std::string, std::string >( iInCType, iInCVersion );
	      
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
      iOutCType = iInCType;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}
