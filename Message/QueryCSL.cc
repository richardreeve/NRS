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
#include "IntegerSegment.hh"
#include "QueryCSL.hh"
#include "QueryCSLManager.hh"
#include "ReplyCSL.hh"
#include "RouteSegment.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"

NRS::Message::QueryCSL::QueryCSL( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( NRS::Literals::Object::QUERYCSL_VARIABLE() ) ),
  iReplyCSLPtr( NULL ),
  iInReturnRoute( dynamic_cast< Type::RouteSegment& >( iInputLink.
						       getSegment( 0 ) ).
		  iValue ),
  iInReturnVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
							getSegment( 1 ) ).
		 iValue ),
  iInMsgID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						   getSegment( 2 ) ).
		 iValue ),
  iOutReturnRoute( dynamic_cast< Type::RouteSegment& >( iOutputLink.
							getSegment( 0 ) ).
		   iValue ),
  iOutReturnVNID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
							 getSegment( 1 ) ).
		  iValue ),
  iOutMsgID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
						    getSegment( 2 ) ).
	     iValue )
{
  iInputLink.setInstantTransmission( true );  
}


NRS::Message::QueryCSL::~QueryCSL()
{
}

void NRS::Message::QueryCSL::request( Base::Target &theTarget,
				      unsigned_t aMessageID )
{
  if (theTarget.isLocal())
    { // then the request has arrived
      Base::VariableNodeDirector &theVND =
	Base::VariableNodeDirector::getDirector();

      // Set the Message ID on the ReplyCSL

      Type::IntegerSegment &aMsgIDS = dynamic_cast< Type::IntegerSegment& >
	( iReplyCSLPtr->getInputLink().getSegment( 0 ) );
      aMsgIDS.iValue = aMessageID;
      
      // Set the CSL on the ReplyCSL
      Type::StringSegment &aCTS = dynamic_cast< Type::StringSegment& >
	( iReplyCSLPtr->getInputLink().getSegment( 1 ) );
      aCTS.iValue = theVND.getCSLDescription();

      // Add return reply to ReplyCSL
      iReplyCSLPtr->getInputLink().receiveMessage();
    }
  else
    { // Send it off
      // Add target
      iOutputLink.addTarget( theTarget );

      // create message
      iOutReturnVNID = iReplyCSLPtr->getVNID();
      iOutReturnRoute = getEmptyRoute();
      iOutMsgID = aMessageID;

      if (theTarget.getVNID() == 0)
	{
	  Type::StringSegment tvnn;
	  tvnn.iValue = Literals::Object::QUERYCTYPE_VARIABLE();
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

void NRS::Message::QueryCSL::doObserve( integer_t aRef )
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
				 &iReplyCSLPtr->getVariableManager().
				 getMessageDescription() );
      
      // send the vnid to ReplyCSL variable
      iReplyCSLPtr->reply( returnTarget, iInMsgID,
			   theVND.getCSLDescription() );

      // and send on to anyone connected...
      iOutReturnVNID = iInReturnVNID;
      iOutReturnRoute = iInReturnRoute;
      iOutMsgID = iInMsgID;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}

bool NRS::Message::QueryCSL::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iReplyCSLPtr)
    {
      if (theVND.hasVariable( Literals::Object::REPLYCSL_VARIABLE() ) )
	{
	  iReplyCSLPtr = dynamic_cast< ReplyCSL* >
	    ( theVND.getVariable( Literals::Object::REPLYCSL_VARIABLE() ) );
	}
    }
  return (iReplyCSLPtr != NULL);
}
