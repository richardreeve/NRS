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
#include "CallbackDirector.hh"
#include "CreateLink.hh"
#include "CreateLinkManager.hh"
#include "Executive.hh"
#include "IntegerSegment.hh"
#include "Location.hh"
#include "QueryRoute.hh"
#include "QueryVNType.hh"
#include "ReplyRoute.hh"
#include "ReplyVNType.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Types.hh"
#include "VariableNodeDirector.hh"

NRS::Message::CreateLink::CreateLink( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::CREATELINK_VARIABLE() ) ),
  iInSourceNotTarget( dynamic_cast< Type::BooleanSegment& >
		      ( iInputLink.getSegment( 0 ) ).iValue ),
  iInSourceCID( dynamic_cast< Type::StringSegment& >( iInputLink.
						      getSegment( 1 ) ).
		iValue ),
  iInSourceVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
							getSegment( 2 ) ).
		 iValue ),
  iInTargetCID( dynamic_cast< Type::StringSegment& >( iInputLink.
						      getSegment( 3 ) ).
		iValue ),
  iInTargetVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
							getSegment( 4 ) ).
		 iValue ),
  iInTemporary( dynamic_cast< Type::BooleanSegment& >( iInputLink.
						       getSegment( 5 ) ).
		iValue ),
  iOutSourceNotTarget( dynamic_cast< Type::BooleanSegment& >
		       ( iOutputLink.getSegment( 0 ) ).iValue ),
  iOutSourceCID( dynamic_cast< Type::StringSegment& >( iOutputLink.
						       getSegment( 1 ) ).
		 iValue ),
  iOutSourceVNID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
							 getSegment( 2 ) ).
		  iValue ),
  iOutTargetCID( dynamic_cast< Type::StringSegment& >( iOutputLink.
						       getSegment( 3 ) ).
		 iValue ),
  iOutTargetVNID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
							 getSegment( 4 ) ).
		  iValue ),
  iOutTemporary( dynamic_cast< Type::BooleanSegment& >( iOutputLink.
							getSegment( 5 ) ).
		 iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::CreateLink::~CreateLink()
{
}

void NRS::Message::CreateLink::createLink( Base::Target &theTarget,
					   bool sourceNotTarget,
					   const std::string &sCID,
					   integer_t sVNID,
					   const std::string &tCID,
					   integer_t tVNID,
					   bool temporary )
{
  if (theTarget.isLocal())
    { // then the command has arrived
      _DEBUG_( "Creating a local link" );
      std::string cid = Base::Executive::getExecutive().getCID();
      Base::VariableNodeDirector &theVND =
	Base::VariableNodeDirector::getDirector();

      if ((sourceNotTarget && (cid != sCID)) ||
	  (!sourceNotTarget && (cid != tCID)))
	_ERROR_( "Failed connection message - no such CID" );
      else
	{
	  if (sourceNotTarget)
	    { // source
	      _DEBUG_( "Source" );
	      if (theVND.hasVariable( sVNID ))
		{
		  if (tCID == cid)
		    {
		      if (theVND.hasVariable( tVNID ))
			{
			  Base::Target cTarget( tCID, getEmptyRoute(),
						tVNID,
						&theVND.getVariable( tVNID )->
						getVariableManager().
						getMessageDescription() );
			  theVND.getVariable( sVNID )->
			    addTarget( cTarget );
			  if (temporary)
			    {
			      theVND.getVariable( sVNID )->
				removeTarget( cTarget );
			    }
			}
		      else
			_ERROR_( "No such variable" );
		    }
		  else
		    {
		      Base::Location cTarget( tCID, getEmptyRoute(),
					      tVNID );
		      QueryRoute *aQRPtr = dynamic_cast< QueryRoute* >
			( theVND.getVariable( Literals::Object::
					      QUERYROUTE_VARIABLE() ) );
		      Base::Target qTarget( tCID, getEmptyRoute(), 0,
					    &aQRPtr->getVariableManager().
					    getMessageDescription() );
		      LinkData ld( sourceNotTarget,
				   sVNID, cTarget, temporary );
		      Base::CallbackDirector &theCD =
			Base::CallbackDirector::getDirector();
		      unsigned_t cbid = theCD.addCallback( this );
		      iCallbackRouteLinkMap[ cbid ] = ld;
		      aQRPtr->request( qTarget, cbid, getEmptyRoute() );
		    }
		}
	      else
		_ERROR_( "No such variable" );
	    }
	  else
	    { // target
	      _DEBUG_( "Target" );
	      if (theVND.hasVariable( tVNID ))
		{
		  if (sCID == cid)
		    {
		      if (theVND.hasVariable( sVNID ))
			{
			  Base::Location cTarget( sCID, getEmptyRoute(),
						  sVNID );
			  theVND.getVariable( tVNID )->addSource( cTarget );
			  if (temporary)
			    {
			      theVND.getVariable( sVNID )->
				removeSource( cTarget );
			    }
			}
		      else
			_ERROR_( "No such variable" );
		    }
		  else
		    {
		      Base::Location cTarget( sCID, getEmptyRoute(),
					      sVNID );
		      QueryRoute *aQRPtr = dynamic_cast< QueryRoute* >
			( theVND.getVariable( Literals::Object::
					      QUERYROUTE_VARIABLE() ) );
		      Base::Target qTarget( sCID, getEmptyRoute(), 0,
					    &aQRPtr->getVariableManager().
					    getMessageDescription() );
		      LinkData ld( sourceNotTarget,
				   tVNID, cTarget, temporary );
		      Base::CallbackDirector &theCD =
			Base::CallbackDirector::getDirector();
		      unsigned_t cbid = theCD.addCallback( this );
		      iCallbackTypeLinkMap[ cbid ] = ld;
		      aQRPtr->request( qTarget, cbid, getEmptyRoute() );
		    }
		}
	      else
		_ERROR_( "No such variable" );
	    }
	}
    }
  else
    { // Send it off
      // Add target
      iOutputLink.addTarget( theTarget );

      // create message
      iOutSourceNotTarget = sourceNotTarget;
      iOutSourceCID = sCID;
      iOutSourceVNID = sVNID;
      iOutTargetCID = tCID;
      iOutTargetVNID = tVNID;
      iOutTemporary = temporary;

      if (theTarget.getVNID() == 0)
	{
	  Type::StringSegment tvnn;
	  tvnn.iValue = Literals::Object::CREATELINK_VARIABLE();
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

void NRS::Message::CreateLink::doObserve( integer_t aRef )
{
  _DEBUG_( std::boolalpha << iInTemporary );
  _DEBUG_( std::boolalpha << iInSourceNotTarget );
  if (aRef == 0)
    {
      std::string cid = Base::Executive::getExecutive().getCID();
      Base::VariableNodeDirector &theVND =
	Base::VariableNodeDirector::getDirector();

      if ((iInSourceNotTarget && (cid != iInSourceCID)) ||
	  (!iInSourceNotTarget && (cid != iInTargetCID)))
	_ERROR_( "Failed connection message - no such CID" );
      else
	{
	  if (iInSourceNotTarget)
	    { // source
	      if (theVND.hasVariable( iInSourceVNID ))
		{
		  if (iInTargetCID == cid)
		    {
		      if (theVND.hasVariable( iInTargetVNID ))
			{
			  Base::Target cTarget( iInTargetCID, getEmptyRoute(),
						iInTargetVNID,
						&theVND.
						getVariable( iInTargetVNID )->
						getVariableManager().
						getMessageDescription() );
			  theVND.getVariable( iInSourceVNID )->
			    addTarget( cTarget );
			  if (iInTemporary)
			    {
			      theVND.getVariable( iInSourceVNID )->
				removeTarget( cTarget );
			    }
			}
		      else
			_ERROR_( "No such variable" );
		    }
		  else
		    {
		      Base::Location cTarget( iInTargetCID, getEmptyRoute(),
					      iInTargetVNID );
		      QueryRoute *aQRPtr = dynamic_cast< QueryRoute* >
			( theVND.getVariable( Literals::Object::
					      QUERYROUTE_VARIABLE() ) );
		      Base::Target qTarget( iInTargetCID, getEmptyRoute(), 0,
					    &aQRPtr->getVariableManager().
					    getMessageDescription() );
		      LinkData ld( iInSourceNotTarget,
				   iInSourceVNID, cTarget, iInTemporary );
		      Base::CallbackDirector &theCD =
			Base::CallbackDirector::getDirector();
		      unsigned_t cbid = theCD.addCallback( this );
		      iCallbackRouteLinkMap[ cbid ] = ld;
		      aQRPtr->request( qTarget, cbid, getEmptyRoute() );
		    }
		}
	      else
		_ERROR_( "No such variable" );
	    }
	  else
	    { // target
	      if (theVND.hasVariable( iInTargetVNID ))
		{
		  if (iInSourceCID == cid)
		    {
		      if (theVND.hasVariable( iInSourceVNID ))
			{
			  Base::Location
			    cTarget( iInSourceCID, getEmptyRoute(),
				     iInSourceVNID );
			  theVND.getVariable( iInTargetVNID )->
			    addSource( cTarget );
			  if (iInTemporary)
			    {
			      theVND.getVariable( iInTargetVNID )->
				removeSource( cTarget );
			    }
			}
		      else
			_ERROR_( "No such variable" );
		    }
		  else
		    {
		      _DEBUG_( iInSourceCID );
		      Base::Location cTarget( iInSourceCID, getEmptyRoute(),
					      iInSourceVNID );
		      QueryRoute *aQRPtr = dynamic_cast< QueryRoute* >
			( theVND.getVariable( Literals::Object::
					      QUERYROUTE_VARIABLE() ) );
		      Base::Target qTarget( iInSourceCID, getEmptyRoute(), 0,
					    &aQRPtr->getVariableManager().
					    getMessageDescription() );
		      LinkData ld( iInSourceNotTarget,
				   iInTargetVNID, cTarget, iInTemporary );
		      Base::CallbackDirector &theCD =
			Base::CallbackDirector::getDirector();
		      unsigned_t cbid = theCD.addCallback( this );
		      iCallbackRouteLinkMap[ cbid ] = ld;
		      _DEBUG_( cbid );
		      aQRPtr->request( qTarget, cbid, getEmptyRoute() );
		    }
		}
	      else
		_ERROR_( "No such variable" );
	    }
	}

      // and send on to anyone connected...
      iOutSourceNotTarget = iInSourceNotTarget;
      iOutSourceCID = iInSourceCID;
      iOutSourceVNID = iInSourceVNID;
      iOutTargetCID = iInTargetCID;
      iOutTargetVNID = iInTargetVNID;
      iOutTemporary = iInTemporary;
      iSendMessage = true;      
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}

void NRS::Message::CreateLink::callback( const unsigned_t msgID )
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();
  _DEBUG_( "" );
  if (iCallbackRouteLinkMap.find( msgID ) != iCallbackRouteLinkMap.end())
    { // a route...
      _DEBUG_( "" );
      route_t forwardRoute;
      route_t returnRoute;
      unsigned_t translationCount;

      if (!dynamic_cast< ReplyRoute* >( theVND.
					getVariable( Literals::Object::
						     REPLYROUTE_VARIABLE() ))->
	  getRouteFromMessageID( msgID, forwardRoute, returnRoute,
				 translationCount ))
	{
	  _ERROR_( "Junk reply" );
	}
      else
	{
	  LinkData &current = iCallbackRouteLinkMap[ msgID ];
	  Base::Location lt( current.iRemote.getCID(),
			     forwardRoute, current.iRemote.getVNID() );
	  if (current.iSNT)
	    { // source, so need to check type
	      QueryVNType *aQVNTPtr = dynamic_cast< QueryVNType* >
		( theVND.getVariable( Literals::Object::
				      QUERYVNTYPE_VARIABLE() ) );
	      LinkData ld( current.iSNT, current.iLocalVNID, lt,
			   current.iTemp );

	      Base::CallbackDirector &theCD =
		Base::CallbackDirector::getDirector();
	      unsigned_t cbid = theCD.addCallback( this );
	      iCallbackTypeLinkMap[ cbid ] = ld;

              std::string theCID;

	      Base::Target
                qTarget(
                  theCID,
                  forwardRoute,
                  0,
                  &aQVNTPtr->getVariableManager().getMessageDescription() );

	      aQVNTPtr->request( qTarget, cbid, ld.iRemote.getVNID() );
	    }
	  else
	    { // target, so add source
	      theVND.getVariable( current.iLocalVNID )->
		addSource( lt );
	      if (current.iTemp)
		{
		  theVND.getVariable( current.iLocalVNID )->
		    removeSource( lt );
		}
	    }
	  iCallbackRouteLinkMap.erase( msgID );
	}
    }
  else if (iCallbackTypeLinkMap.find( msgID ) != iCallbackTypeLinkMap.end())
    { // a type...
      _DEBUG_( "" );
      std::string aType;

      if (!dynamic_cast< ReplyVNType* >
	  ( theVND.getVariable( Literals::Object::
				REPLYVNTYPE_VARIABLE() ) )->
	  getVNTypeFromMessageID( msgID, aType ))
	{
	  _ERROR_( "Junk reply" );
	}
      else
	{
	  if (theVND.hasVariableManager( aType ))
	    {
	      LinkData &current = iCallbackTypeLinkMap[ msgID ];
	      Base::Target theOther( current.iRemote.getCID(),
				     current.iRemote.getRoute(),
				     current.iRemote.getVNID(),
				     &theVND.getVariableManager( aType ).
				     getMessageDescription() );
	      if (theVND.hasVariable( current.iLocalVNID ))
		{
		  if (current.iSNT)
		    { // source, so add target
		      theVND.getVariable( current.iLocalVNID )->
			addTarget( theOther );
		      if (current.iTemp)
			{
			  theVND.getVariable( current.iLocalVNID )->
			    removeTarget( theOther );
			}
		    }
		  else
		    { // target, so add source
		      theVND.getVariable( current.iLocalVNID )->
			addSource( theOther );
		      if (current.iTemp)
			{
			  theVND.getVariable( current.iLocalVNID )->
			    removeSource( theOther );
			}
		    }
		}
	      else
		{
		  _DEBUG_( current.iLocalVNID );
		  _ERROR_( "No such local vnid" );
		}
	    }
	  else
	    _ERROR_( "Connecting to unknown variable type, rejecting!" );;
	  iCallbackTypeLinkMap.erase( msgID );
	}
    }
  else
    {
      _ERROR_( "Junk callback" );
    }
}

bool NRS::Message::CreateLink::queryWaiting()
{
  return (!iCallbackRouteLinkMap.empty() || !iCallbackTypeLinkMap.empty());
}

bool NRS::Message::CreateLink::resolveComplexDependencies()
{
  return !queryWaiting();
}
