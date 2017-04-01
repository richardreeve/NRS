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
#include "Executive.hh"
#include "ExternalInterface.hh"
#include "ExternalInterfaceDirector.hh"
#include "IntegerSegment.hh"
#include "NodeFactory.hh"
#include "QueryPort.hh"
#include "QueryPortManager.hh"
#include "ReplyPort.hh"
#include "RouteSegment.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"

NRS::Message::QueryPort::QueryPort( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::QUERYPORT_VARIABLE() ) ),
  iReplyPortPtr( NULL ),
  iInReturnRoute( dynamic_cast< Type::RouteSegment& >( iInputLink.
						       getSegment( 0 ) ).
		 iValue ),
  iInReturnVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
							getSegment( 1 ) ).
		 iValue ),
  iInMsgID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						   getSegment( 2 ) ).
		 iValue ),
  iInPort( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						  getSegment( 3 ) ).
	   iValue ),
  iOutReturnRoute( dynamic_cast< Type::RouteSegment& >( iOutputLink.
							getSegment( 0 ) ).
		   iValue ),
  iOutReturnVNID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
							 getSegment( 1 ) ).
		  iValue ),
  iOutMsgID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
						    getSegment( 2 ) ).
	     iValue ),
  iOutPort( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
						   getSegment( 3 ) ).
	    iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::QueryPort::~QueryPort()
{
}

bool NRS::Message::QueryPort::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iReplyPortPtr)
    {
      if (theVND.hasVariable( Literals::Object::REPLYPORT_VARIABLE() ) )
	{
	  iReplyPortPtr = dynamic_cast< ReplyPort* >
	    ( theVND.getVariable( Literals::Object::REPLYPORT_VARIABLE() ) );
	}
    }
  return (iReplyPortPtr != NULL);
}

void NRS::Message::QueryPort::request( Base::Target &theTarget,
				       unsigned_t aMessageID,
				       unsigned_t aPort )
{
  if (theTarget.isLocal())
    { // then the request has arrived
      // get the EID
      Base::ExternalInterfaceDirector &theEID = 
	Base::ExternalInterfaceDirector::getDirector();
      
      // Set the Message ID on the ReplyPort
      Type::IntegerSegment &aMsgID = dynamic_cast< Type::IntegerSegment& >
	( iReplyPortPtr->getInputLink().getSegment( 0 ) );
      aMsgID.iValue = aMessageID;
      
      // Set the PortRoute on the ReplyPort
      Type::RouteSegment &aPortRoute = dynamic_cast< Type::RouteSegment& >
	( iReplyPortPtr->getInputLink().getSegment( 1 ) );
      if (theEID.hasExternalInterface( aPort - 1 ))
	aPortRoute.iValue =
	  theEID.getExternalInterface( aPort - 1 )->getPortRoute();
      else
	aPortRoute.iValue = getEmptyRoute();

      // Add return reply to ReplyPort
      iReplyPortPtr->getInputLink().receiveMessage();
    }
  else
    { // Send it off
      // Add target
      iOutputLink.addTarget( theTarget );

      // create message
      iOutReturnVNID = iReplyPortPtr->getVNID();
      iOutReturnRoute = getEmptyRoute();
      iOutMsgID = aMessageID;
      iOutPort = aPort;

      if (theTarget.getVNID() == 0)
	{
	  Type::StringSegment tvnn;
	  tvnn.iValue = Literals::Object::QUERYVNID_VARIABLE();
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

      Type::StringSegment rcid;
      rcid.iValue = Base::Executive::getExecutive().getCID();
      iOutputLink.setIntelligentSegment( Enum::SourceCID, &rcid );

      Type::RouteSegment rr;
      rr.iValue = getEmptyRoute();
      iOutputLink.setIntelligentSegment( Enum::ReturnRoute, &rr );

      // Send message
      iSendMessage = true;
      update();
      sendMessages();
      iOutputLink.clearIntelligentSegments();

      // and remove target
      iOutputLink.removeTarget( theTarget );
    }
}

void NRS::Message::QueryPort::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      Base::ExternalInterfaceDirector &theEID = 
	Base::ExternalInterfaceDirector::getDirector();
      std::string cid;
      route_t retRoute = iInReturnRoute;

      const Type::RouteSegment *rs = dynamic_cast< const Type::RouteSegment* >
	( iInputLink.getIntelligentSegment( Enum::ReturnRoute ) );
      if (rs != NULL)
	{
	  retRoute = rs->iValue;
	}
      else
	{
	  const Type::StringSegment *cids =
	    dynamic_cast< const Type::StringSegment* >
	    ( iInputLink.getIntelligentSegment( Enum::SourceCID ) );
	  if (cids != NULL)
	    {
	      cid = cids->iValue;
	    }
	}
      Base::Target returnTarget( cid, retRoute, iInReturnVNID,
				 &iReplyPortPtr->getVariableManager().
				 getMessageDescription() );
      
      route_t aPortRoute = getEmptyRoute();
      if (theEID.hasExternalInterface( iInPort - 1 ))
	aPortRoute =
	  theEID.getExternalInterface( iInPort - 1 )->getPortRoute();
      
      iReplyPortPtr->reply( returnTarget, iInMsgID, aPortRoute );

      // and send on to anyone connected...
      iOutReturnVNID = iInReturnVNID;
      iOutReturnRoute = iInReturnRoute;
      iOutMsgID = iInMsgID;
      iOutPort = iInPort;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}
