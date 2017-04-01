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
#include "QueryVNType.hh"
#include "QueryVNTypeManager.hh"
#include "ReplyVNType.hh"
#include "RouteSegment.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"

NRS::Message::QueryVNType::QueryVNType( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::QUERYVNTYPE_VARIABLE() ) ),
  iReplyVNTypePtr( NULL ),
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

NRS::Message::QueryVNType::~QueryVNType()
{
}

bool NRS::Message::QueryVNType::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iReplyVNTypePtr)
    {
      if (theVND.hasVariable( Literals::Object::REPLYVNTYPE_VARIABLE() ) )
	{
	  iReplyVNTypePtr = dynamic_cast< ReplyVNType* >
	    ( theVND.getVariable( Literals::Object::REPLYVNTYPE_VARIABLE() ) );
	}
    }
  return (iReplyVNTypePtr != NULL);
}

void NRS::Message::QueryVNType::request( Base::Target &theTarget,
					 unsigned_t aMessageID,
					 unsigned_t aVNID )
{
  if (theTarget.isLocal())
    { // then the request has arrived
      // get the VND
      NRS::Base::VariableNodeDirector &theVND =
	NRS::Base::VariableNodeDirector::getDirector();
      
      // Set the Message ID on the ReplyVNType
      Type::IntegerSegment &aMsgID = dynamic_cast< Type::IntegerSegment& >
	( iReplyVNTypePtr->getInputLink().getSegment( 0 ) );
      aMsgID.iValue = aMessageID;
      
      // Set the VNType on the ReplyVNType
      Type::StringSegment &aVNType = dynamic_cast< Type::StringSegment& >
	( iReplyVNTypePtr->getInputLink().getSegment( 1 ) );
      if (theVND.hasVariable( aVNID ))
	aVNType.iValue = theVND.getVariableManager( aVNID ).getMessageName();
      else
	aVNType.iValue.clear();

      // Add return reply to ReplyVNType
      iReplyVNTypePtr->getInputLink().receiveMessage();
    }
  else
    {
      // Do we already know the answer?
      bool answered = false;
      if (theTarget.getRoute() != getEmptyRoute())
	{
	  if (iRouteVNIDTypeMap.find( theTarget.getRoute() ) !=
	      iRouteVNIDTypeMap.end())
	    {
	      std::map< integer_t, std::string > &vnidTypeMap =
		iRouteVNIDTypeMap[ theTarget.getRoute() ];
	      if (vnidTypeMap.find( aVNID ) !=
		  vnidTypeMap.end())
		{
		  answered = true;
		  // Set the Message ID on the ReplyVNType
		  Type::IntegerSegment &aMsgID =
		    dynamic_cast< Type::IntegerSegment& >
		    ( iReplyVNTypePtr->getInputLink().getSegment( 0 ) );
		  aMsgID.iValue = aMessageID;
		  
		  // Set the VNType on the ReplyVNType
		  Type::StringSegment &aVNType =
		    dynamic_cast< Type::StringSegment& >
		    ( iReplyVNTypePtr->getInputLink().getSegment( 1 ) );
		  aVNType.iValue = vnidTypeMap[ aVNID ];
		  
		  // Add return reply to ReplyVNType
		  iReplyVNTypePtr->getInputLink().receiveMessage();
		}
	    }
	  if (!answered)
	    {
	      // we know there is a route, so remember details for cache
	      Base::Location point( theTarget.getCID(),
				    theTarget.getRoute(),
				    aVNID );
	      iMsgIDTargetMap[aMessageID] = point;
	    }
	}
      if (!answered)
	{
	  // send it off
	  // Add target
	  iOutputLink.addTarget( theTarget );
	  
	  // create message
	  iOutReturnVNID = iReplyVNTypePtr->getVNID();
	  iOutReturnRoute = getEmptyRoute();
	  iOutMsgID = aMessageID;
	  iOutVNID = aVNID;
	  
	  if (theTarget.getVNID() == 0)
	    {
	      Type::StringSegment tvnn;
	      tvnn.iValue = Literals::Object::QUERYVNTYPE_VARIABLE();
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
}

void NRS::Message::QueryVNType::cache( unsigned_t aMessageID,
				       std::string aVNType )
{
  std::map< unsigned_t, Base::Location >::iterator it =
	  iMsgIDTargetMap.find( aMessageID );

  if ( it != iMsgIDTargetMap.end()) 
    {
      iRouteVNIDTypeMap[it->second.getRoute()][it->second.getVNID()] =
	aVNType;
      
      // as the VNType has been retrieved, remove it from the map
      iMsgIDTargetMap.erase( it );
    }
}

void NRS::Message::QueryVNType::doObserve( integer_t aRef )
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
				 &iReplyVNTypePtr->getVariableManager().
				 getMessageDescription() );
      
      std::string aVNType;

      if (theVND.hasVariable( iInVNID ))
	aVNType = theVND.getVariableManager( iInVNID ).getMessageName();
      
      iReplyVNTypePtr->reply( returnTarget, iInMsgID, aVNType );

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
