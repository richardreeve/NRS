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
#include "QueryCID.hh"
#include "QueryCIDManager.hh"
#include "ReplyCID.hh"
#include "RouteSegment.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"

NRS::Message::QueryCID::QueryCID( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::QUERYCID_VARIABLE() ) ),
  iReplyCIDPtr( NULL ),
  iInReturnRoute( dynamic_cast< Type::RouteSegment& >( iInputLink.
						       getSegment( 0 ) ).
		  iValue ),
  iInReturnVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
							getSegment( 1 ) ).
		 iValue ),
  iInMsgID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						   getSegment( 2 ) ).
		 iValue ),
  iInCID( dynamic_cast< Type::StringSegment& >( iInputLink.
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
  iOutCID( dynamic_cast< Type::StringSegment& >( iOutputLink.
						 getSegment( 3 ) ).
	   iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::QueryCID::~QueryCID()
{
}

void NRS::Message::QueryCID::request( Base::Target &theTarget,
				      unsigned_t aMessageID,
				      std::string aCID )
{
  if (theTarget.isLocal())
    { // then the request has arrived
      // get the executive
      Base::Executive &theE = Base::Executive::getExecutive();

      //if the E doesn't have a CID assigned, then store the suggestedCID
      if (!theE.hasCID())
	{
	  if ( !aCID.empty() )
	    theE.setCID( aCID );
	  else 
	    _ERROR_( "No current or suggested CID\n\t" );
	}
      // Set the Message ID on the ReplyCID
      Type::IntegerSegment &aMsgIDS = dynamic_cast< Type::IntegerSegment& >
	( iReplyCIDPtr->getInputLink().getSegment( 0 ) );
      aMsgIDS.iValue = aMessageID;
      
      // Set the CID on the ReplyCID
      Type::StringSegment &aCIDS = dynamic_cast< Type::StringSegment& >
	( iReplyCIDPtr->getInputLink().getSegment( 1 ) );
      aCIDS.iValue = theE.getCID();

      // Add return reply to ReplyCID
      iReplyCIDPtr->getInputLink().receiveMessage();
    }
  else
    { // Send it off
      // Add target
      iOutputLink.addTarget( theTarget );

      // create message
      iOutReturnVNID = iReplyCIDPtr->getVNID();
      iOutReturnRoute = getEmptyRoute();
      iOutMsgID = aMessageID;
      iOutCID = aCID;

      if (theTarget.getVNID() == 0)
	{
	  Type::StringSegment tvnn;
	  tvnn.iValue = Literals::Object::QUERYVNID_VARIABLE();
	  iOutputLink.setIntelligentSegment( Enum::TargetVNName, &tvnn );
	}

      if (!theTarget.getCID().empty())
	{
	  _ERROR_( "Bizarre, QueryCID broadcast with CID..." );
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

     
void NRS::Message::QueryCID::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      // get the general manager
      Base::Executive &theE = Base::Executive::getExecutive();
      
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
				 &iReplyCIDPtr->getVariableManager().
				 getMessageDescription() );
      
      //if the E doesn't have a CID assigned, then store the suggestedCID
      if (!theE.hasCID())
	{
	  if ( !iInCID.empty() )
	    theE.setCID( iInCID );
	  else 
	    _ERROR_( "No current or suggested CID\n\t" );
	}
      
      iReplyCIDPtr->reply( returnTarget, iInMsgID, theE.getCID() );

      // and send on to anyone connected...
      iOutReturnVNID = iInReturnVNID;
      iOutReturnRoute = iInReturnRoute;
      iOutMsgID = iInMsgID;
      iOutCID = iInCID;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}

bool NRS::Message::QueryCID::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iReplyCIDPtr)
    {
      if (theVND.hasVariable( Literals::Object::REPLYCID_VARIABLE() ) )
	{
	  iReplyCIDPtr = dynamic_cast< ReplyCID* >
	    ( theVND.getVariable( Literals::Object::REPLYCID_VARIABLE() ) );
	}
    }
  return (iReplyCIDPtr != NULL);
}
