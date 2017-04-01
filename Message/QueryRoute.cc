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
#include <list>
#include <string>

#include "BooleanSegment.hh"
#include "Executive.hh"
#include "ExternalInterface.hh"
#include "ExternalInterfaceDirector.hh"
#include "IntegerSegment.hh"
#include "QueryRoute.hh"
#include "QueryRouteManager.hh"
#include "ReplyRoute.hh"
#include "RouteSegment.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "VariableNodeDirector.hh"

NRS::Message::QueryRoute::QueryRoute( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::QUERYROUTE_VARIABLE() ) ),
  iReplyRoutePtr( NULL ),
  iInReturnRoute( dynamic_cast< Type::RouteSegment& >( iInputLink.
						       getSegment( 0 ) ).
		 iValue ),
  iInReturnVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
							getSegment( 1 ) ).
		 iValue ),
  iInMsgID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						   getSegment( 2 ) ).
		 iValue ),
  iInForwardRoute( dynamic_cast< Type::RouteSegment& >( iInputLink.
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
  iOutForwardRoute( dynamic_cast< Type::RouteSegment& >( iOutputLink.
							 getSegment( 3 ) ).
		    iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::QueryRoute::~QueryRoute()
{
}

void NRS::Message::QueryRoute::request( Base::Target &theTarget,
					unsigned_t aMessageID,
					route_t returnRoute )
{
  if (theTarget.isLocal())
    { // then the request has arrived
      // Set the Message ID on the ReplyRoute
      Type::IntegerSegment &aMsgID = dynamic_cast< Type::IntegerSegment& >
	( iReplyRoutePtr->getInputLink().getSegment( 0 ) );
      aMsgID.iValue = aMessageID;

      // Set the forward route on the ReplyRoute
      Type::RouteSegment &aFR = dynamic_cast< Type::RouteSegment& >
	( iReplyRoutePtr->getInputLink().getSegment( 1 ) );
      aFR.iValue = theTarget.getRoute();

      // Set the return route on the ReplyRoute
      Type::RouteSegment &aRR = dynamic_cast< Type::RouteSegment& >
	( iReplyRoutePtr->getInputLink().getSegment( 2 ) );
      aRR.iValue = returnRoute;

      // Set the translation count on the ReplyRoute
      Type::IntegerSegment &aTC = dynamic_cast< Type::IntegerSegment& >
	( iReplyRoutePtr->getInputLink().getSegment( 3 ) );
      aTC.iValue = 0;

      // Add return reply to ReplyRoute
      iReplyRoutePtr->getInputLink().receiveMessage();
    }
  else
    { // Send it off
      bool discard = false;
      iOutputLink.addTarget( theTarget );

      // Add target
      _DEBUG_( "" );
      // create message
      iOutReturnVNID = iReplyRoutePtr->getVNID();
      iOutReturnRoute = returnRoute;
      iOutMsgID = aMessageID;
      iOutForwardRoute = theTarget.getRoute();

      if (iOutForwardRoute == getEmptyRoute())
	{ // Need intelligent forward route
	  Type::RouteSegment fr;
	  fr.iValue = getEmptyRoute();
	  iOutputLink.setIntelligentSegment( Enum::ForwardRoute, &fr );
	  if (theTarget.getCID().empty())
	    {
	      _ERROR_( "No route or cid!" );
	      discard = true;
	    }
	}

      if (theTarget.getVNID() == 0)
	{
	  Type::StringSegment tvnn;
	  tvnn.iValue = Literals::Object::QUERYROUTE_VARIABLE();
	  iOutputLink.setIntelligentSegment( Enum::TargetVNName, &tvnn );
	}

      if (!theTarget.getCID().empty())
	{
	  _DEBUG_( "" );
	  Type::StringSegment cid;
	  cid.iValue = theTarget.getCID();
	  iOutputLink.setIntelligentSegment( Enum::TargetCID, &cid );
	  Type::BooleanSegment isB;
	  isB.iValue = true;
	  iOutputLink.setIntelligentSegment( Enum::IsBroadcast, &isB );
	  Type::IntegerSegment hc;
	  hc.iValue = 9;
	  iOutputLink.setIntelligentSegment( Enum::HopCount, &hc );

	  Type::StringSegment rcid;
	  rcid.iValue = Base::Executive::getExecutive().getCID();
	  iOutputLink.setIntelligentSegment( Enum::SourceCID, &rcid );

	  Type::RouteSegment rr;
	  rr.iValue = getEmptyRoute();
	  iOutputLink.setIntelligentSegment( Enum::ReturnRoute, &rr );
	}

      // Add in translation count
      Type::IntegerSegment tc;
      tc.iValue = 0;
      iOutputLink.setIntelligentSegment( Enum::TranslationCount, &tc );

      // Send message
      iSendMessage = true;
      update();
      sendMessages();
      iOutputLink.clearIntelligentSegments();

      // and remove target
      iOutputLink.removeTarget( theTarget );
    }
}


void NRS::Message::QueryRoute::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      std::string cid;
      route_t retRoute = iInReturnRoute;
      route_t forRoute = iInForwardRoute;
      integer_t tc = 0;

      const Type::RouteSegment *rrs = dynamic_cast< const Type::RouteSegment* >
	( iInputLink.getIntelligentSegment( Enum::ReturnRoute ) );
      if (rrs != NULL)
	{
	  retRoute = rrs->iValue;
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
				 &iReplyRoutePtr->getVariableManager().
				 getMessageDescription() );

      
      const Type::RouteSegment *frs = dynamic_cast< const Type::RouteSegment* >
	( iInputLink.getIntelligentSegment( Enum::ForwardRoute ) );
      if (frs != NULL)
	{
	  forRoute = frs->iValue;
	}

      const Type::IntegerSegment *tcs =
	dynamic_cast< const Type::IntegerSegment* >
	( iInputLink.getIntelligentSegment( Enum::TranslationCount ) );
      if (tcs != NULL)
	{
	  tc = tcs->iValue;
	}

      iReplyRoutePtr->reply( returnTarget, iInMsgID, forRoute, tc );

      // and send on to anyone connected...
      iOutReturnVNID = iInReturnVNID;
      iOutReturnRoute = iInReturnRoute;
      iOutMsgID = iInMsgID;
      iOutForwardRoute = iInForwardRoute;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}

bool NRS::Message::QueryRoute::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iReplyRoutePtr)
    {
      if (theVND.hasVariable( Literals::Object::REPLYROUTE_VARIABLE() ) )
	{
	  iReplyRoutePtr = dynamic_cast< ReplyRoute* >
	    ( theVND.getVariable( Literals::Object::REPLYROUTE_VARIABLE() ) );
	}
    }
  return (iReplyRoutePtr != NULL);
}
