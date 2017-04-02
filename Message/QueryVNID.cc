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
#include "Executive.hh"
#include "IntegerSegment.hh"
#include "NodeFactory.hh"
#include "QueryVNID.hh"
#include "QueryVNIDManager.hh"
#include "ReplyVNID.hh"
#include "RouteSegment.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"

NRS::Message::QueryVNID::QueryVNID( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::QUERYVNID_VARIABLE() ) ),
  iReplyVNIDPtr( NULL ),
  iInReturnRoute( dynamic_cast< Type::RouteSegment& >( iInputLink.
						       getSegment( 0 ) ).
		 iValue ),
  iInReturnVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
							getSegment( 1 ) ).
		 iValue ),
  iInMsgID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						   getSegment( 2 ) ).
		 iValue ),
  iInVNName( dynamic_cast< Type::StringSegment& >( iInputLink.
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
  iOutVNName( dynamic_cast< Type::StringSegment& >( iOutputLink.
						    getSegment( 3 ) ).
	     iValue )
{
  //iInputLink.setInstantTransmission( true );
}

NRS::Message::QueryVNID::~QueryVNID()
{
}

void NRS::Message::QueryVNID::request( Base::Target &theTarget,
				       unsigned_t aMessageID,
				       std::string aVNName )
{
  if (theTarget.isLocal())
    { // then the request has arrived
      // get the VND
      NRS::Base::VariableNodeDirector &theVND =
	NRS::Base::VariableNodeDirector::getDirector();
      
      // Set the Message ID on the ReplyVNID
      Type::IntegerSegment &aMsgID = dynamic_cast< Type::IntegerSegment& >
	( iReplyVNIDPtr->getInputLink().getSegment( 0 ) );
      aMsgID.iValue = aMessageID;
      
      // Set the VNID on the ReplyVNID
      Type::IntegerSegment &aVNID = dynamic_cast< Type::IntegerSegment& >
	( iReplyVNIDPtr->getInputLink().getSegment( 1 ) );
      if (theVND.hasVariable( aVNName ))
	aVNID.iValue = theVND.getVariableVNID( aVNName );
      else
	aVNID.iValue = 0;
      
      // Add return reply to ReplyVNID
      iReplyVNIDPtr->getInputLink().receiveMessage();
    }
  else
    { // Send it off
      // Add target
      iOutputLink.addTarget( theTarget );

      // create message
      iOutReturnVNID = iReplyVNIDPtr->getVNID();
      iOutReturnRoute = getEmptyRoute();
      iOutMsgID = aMessageID;
      iOutVNName = aVNName;

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

void NRS::Message::QueryVNID::doObserve( integer_t aRef )
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (aRef == 0)
    {
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
				 &iReplyVNIDPtr->getVariableManager().
				 getMessageDescription() );
      
      integer_t aVNID = 0;

      if (theVND.hasVariable( iInVNName ))
	aVNID = theVND.getVariableVNID( iInVNName );

      iReplyVNIDPtr->reply( returnTarget, iInMsgID, aVNID );

      // and send on to anyone connected...
      iOutReturnVNID = iInReturnVNID;
      iOutReturnRoute = iInReturnRoute;
      iOutMsgID = iInMsgID;
      iOutVNName = iInVNName;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive from unrecognised source" );
}

bool NRS::Message::QueryVNID::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iReplyVNIDPtr)
    {
      if (theVND.hasVariable( Literals::Object::REPLYVNID_VARIABLE() ) )
	{
	  iReplyVNIDPtr = dynamic_cast< ReplyVNID* >
	    ( theVND.getVariable( Literals::Object::REPLYVNID_VARIABLE() ) );
	}
    }
  return (iReplyVNIDPtr != NULL);
}

