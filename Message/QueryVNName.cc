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
#include <string>

#include "BooleanSegment.hh"
#include "Executive.hh"
#include "IntegerSegment.hh"
#include "NodeFactory.hh"
#include "QueryVNName.hh"
#include "QueryVNNameManager.hh"
#include "ReplyVNName.hh"
#include "RouteSegment.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"

NRS::Message::QueryVNName::QueryVNName( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::QUERYVNNAME_VARIABLE() ) ),
  iReplyVNNamePtr( NULL ),
  iInReturnRoute( dynamic_cast< Type::RouteSegment& >( iInputLink.
						       getSegment( 0 ) ).
		 iValue ),
  iInReturnVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
							getSegment( 1 ) ).
		 iValue ),
  iInMsgID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						   getSegment( 2 ) ).
		 iValue ),
  iInVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
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
  iOutVNID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
						   getSegment( 3 ) ).
	    iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::QueryVNName::~QueryVNName()
{
}

bool NRS::Message::QueryVNName::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iReplyVNNamePtr)
    {
      if (theVND.hasVariable( Literals::Object::REPLYVNNAME_VARIABLE() ) )
	{
	  iReplyVNNamePtr = dynamic_cast< ReplyVNName* >
	    ( theVND.getVariable( Literals::Object::REPLYVNNAME_VARIABLE() ) );
	}
    }
  return (iReplyVNNamePtr != NULL);
}

void NRS::Message::QueryVNName::request( Base::Target &theTarget,
					 unsigned_t aMessageID,
					 unsigned_t aVNID )
{
  if (theTarget.isLocal())
    { // then the request has arrived
      // get the VND
      NRS::Base::VariableNodeDirector &theVND =
	NRS::Base::VariableNodeDirector::getDirector();
      
      // Set the Message ID on the ReplyVNName
      Type::IntegerSegment &aMsgID = dynamic_cast< Type::IntegerSegment& >
	( iReplyVNNamePtr->getInputLink().getSegment( 0 ) );
      aMsgID.iValue = aMessageID;
      
      // Set the VNName on the ReplyVNName
      Type::StringSegment &aVNName = dynamic_cast< Type::StringSegment& >
	( iReplyVNNamePtr->getInputLink().getSegment( 1 ) );
      if (theVND.hasVariable( aVNID ))
	aVNName.iValue = theVND.getVariableManager( aVNID ).getMessageName();
      else
	aVNName.iValue.clear();

      // Add return reply to ReplyVNName
      iReplyVNNamePtr->getInputLink().receiveMessage();
    }
  else
    { // Send it off
      // Add target
      iOutputLink.addTarget( theTarget );

      // create message
      iOutReturnVNID = iReplyVNNamePtr->getVNID();
      iOutReturnRoute = getEmptyRoute();
      iOutMsgID = aMessageID;
      iOutVNID = aVNID;

      if (theTarget.getVNID() == 0)
	{
	  Type::StringSegment tvnn;
	  tvnn.iValue = Literals::Object::QUERYVNNAME_VARIABLE();
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

void NRS::Message::QueryVNName::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      Base::VariableNodeDirector &theVND =
	Base::VariableNodeDirector::getDirector();
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
				 &iReplyVNNamePtr->getVariableManager().
				 getMessageDescription() );
      
      std::string aVNName;

      if (theVND.hasVariable( iInVNID ))
	aVNName = theVND.getVariable( iInVNID )->getName();
      
      iReplyVNNamePtr->reply( returnTarget, iInMsgID, aVNName );

      // and send on to anyone connected...
      iOutReturnVNID = iInReturnVNID;
      iOutReturnRoute = iInReturnRoute;
      iOutMsgID = iInMsgID;
      iOutVNID = iInVNID;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}
