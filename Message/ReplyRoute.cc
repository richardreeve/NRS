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

#include "BooleanSegment.hh"
#include "Callback.hh"
#include "CallbackDirector.hh"
#include "IntegerSegment.hh"
#include "ReplyRoute.hh"
#include "ReplyRouteManager.hh"
#include "RouteSegment.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"

NRS::Message::ReplyRoute::ReplyRoute( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::REPLYROUTE_VARIABLE() ) ),
  iInMsgID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						   getSegment( 0 ) ).
	    iValue ),
  iInForwardRoute( dynamic_cast< Type::RouteSegment& >( iInputLink.
							getSegment( 1 ) ).
	   iValue ),
  iInReturnRoute( dynamic_cast< Type::RouteSegment& >( iInputLink.
						       getSegment( 2 ) ).
		  iValue ),
  iInTranslationCount( dynamic_cast< Type::IntegerSegment& >
		       ( iInputLink.getSegment( 3 ) ).iValue ),
  iOutMsgID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
						    getSegment( 0 ) ).
	     iValue ),
  iOutForwardRoute( dynamic_cast< Type::RouteSegment& >( iOutputLink.
							 getSegment( 1 ) ).
		    iValue ),
  iOutReturnRoute( dynamic_cast< Type::RouteSegment& >( iOutputLink.
							getSegment( 2 ) ).
		   iValue ),
  iOutTranslationCount( dynamic_cast< Type::IntegerSegment& >
			( iOutputLink.getSegment( 3 ) ).iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::ReplyRoute::~ReplyRoute()
{
}

void NRS::Message::ReplyRoute::reply( Base::Target &theTarget,
				      integer_t aMessageID,
				      route_t &forwardRoute,
				      integer_t translationCount )
{
  _DEBUG_( "" );
  if (theTarget.isLocal())
    { // then the reply has arrived
      // get the callback director
      Base::CallbackDirector &theCD =
	Base::CallbackDirector::getDirector();

  _DEBUG_( "" );
      if ( theCD.hasCallback( aMessageID ) )
	{
	  // add the routes to this variables messageID -> Route map
	  iMessageIDRoutesMap[ aMessageID ] =
	    std::pair< route_t, route_t >( forwardRoute,
					   theTarget.getRoute() );

	  iMessageIDTranslationCountMap[ aMessageID ] = translationCount;
	  
	  // tell the variable that is expecting the just-received
	  // VNID that this VNID is available from the Reply variable
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

  _DEBUG_( "" );
      // create message
      iOutMsgID = aMessageID;
      iOutForwardRoute = forwardRoute;
      iOutReturnRoute = theTarget.getRoute();
      iOutTranslationCount = translationCount;

      if (theTarget.getVNID() == 0)
	{
	  Type::StringSegment tvnn;
	  tvnn.iValue = Literals::Object::REPLYROUTE_VARIABLE();
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

      if (iOutReturnRoute == getEmptyRoute())
	{
	  Type::RouteSegment rrs;
	  rrs.iValue = getEmptyRoute();
	  iOutputLink.setIntelligentSegment( Enum::ForwardRoute, &rrs );
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


void NRS::Message::ReplyRoute::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      Base::CallbackDirector &theCD =
	Base::CallbackDirector::getDirector();
      
      // check if there is a callback associated with this message id
      if (iInMsgID != 0)
	{
	  if (theCD.hasCallback( iInMsgID ))
	    {
	      route_t returnRoute = iInReturnRoute;
	      const Type::RouteSegment *frs =
		dynamic_cast< const Type::RouteSegment* >
		( iInputLink.getIntelligentSegment( Enum::ForwardRoute ) );
	      if (frs != NULL)
		{
		  returnRoute = frs->iValue;
		}
	      
	      // add the routes to this variables messageID -> Route map
	      iMessageIDRoutesMap[ iInMsgID ] =
		std::pair< route_t, route_t >( iInForwardRoute,
					       returnRoute );
	      
	      iMessageIDTranslationCountMap[ iInMsgID ] = iInTranslationCount;
	      
	      // tell the variable that is expecting the just-received Route
	      // that this Route is available from the Reply variable
	      Base::Callback *theCallbackObj = theCD.getCallback( iInMsgID );
	      theCallbackObj->callback( iInMsgID );
	    }
	}

      // and send on to anyone connected...
      iOutMsgID = iInMsgID;
      iOutForwardRoute = iInForwardRoute;
      iOutReturnRoute = iInReturnRoute;
      iOutTranslationCount = iInTranslationCount;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}
