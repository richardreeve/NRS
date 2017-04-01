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
#include <cstring>
#include <iomanip>
#include <iostream>
#include <sstream>

#include "BufferedInterface.hh"
#include "CallbackDirector.hh"
#include "CSLParser.hh"
#include "IntelligentStore.hh"
#include "MessageParser.hh"
#include "QueryRoute.hh"
#include "QueryVNID.hh"
#include "QueryVNType.hh"
#include "ReplyRoute.hh"
#include "ReplyVNID.hh"
#include "ReplyVNType.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"

NRS::Interface::BufferedInterface::
BufferedInterface( Enum::ConnectionType aConnectionType,
		   Enum::EncodingType anEncodingType,
		   Interface::MessageParser *aParserPtr,
		   bool doRead, bool doWrite, bool instantTransmit,
		   bool receiveAll, int maxReceive, size_t initBufferSize ) :
  Base::ExternalInterface( aConnectionType, anEncodingType, aParserPtr,
			   doRead, doWrite, instantTransmit, receiveAll,
			   maxReceive ),
  iIsConfigured( false ),
  iInBufferActiveWrite( 0 ), iInBufferActiveRead( 0 )
{
  // Create buffers for incoming data storage
  iInBuffer[0] = (uint8_t *) malloc( initBufferSize );
  iInBuffer[1] = (uint8_t *) malloc( initBufferSize );
  iInAllocSize = initBufferSize;
  iInStartPtr = iInBuffer[0];
  iInStopPtr = iInBuffer[0];
  iInEndPtr[0] = iInBuffer[0];
  iInEndPtr[1] = iInBuffer[1];
  // Create buffers for outgoing data storage
  iOutBuffer = (uint8_t*) malloc( initBufferSize );
  iOutAllocSize = initBufferSize;
  iOutStartPtr = iOutBuffer;
  iOutEndPtr = iOutBuffer;
}


NRS::Interface::BufferedInterface::~BufferedInterface()
{
  free( iInBuffer[0] );
  free( iInBuffer[1] );
  free( iOutBuffer );
  for ( typeof( iMsgIDStorePtrMap.begin() ) iter = iMsgIDStorePtrMap.begin();
	iter != iMsgIDStorePtrMap.end(); ++iter )
    {
      delete iter->second;
    }
}

bool NRS::Interface::BufferedInterface::update()
{
  bool running = false;
  bool finished = false;
  int received = 0;
  bool isOpen;

  isOpen = pollInput();

  while (!finished)
    {
      if (iIsConfigured)
	{
	  running = true;
	  if (iReceiveAll)
	    {
	      while (running)
		{
		  running = iParserPtr->parseAgain();
		  if (!running)
		    {
		      iIsConfigured = false;
		      // Go to character beyond next end of message
		      iInStartPtr += strlen( (char*) iInStartPtr ) + 1;
		    }
		}
	    }
	  else
	    {
	      for ( finished = true; 
		    ( ( received < iMaxReceive ) && running ); 
		    received++ )
		{
		  running = iParserPtr->parseAgain();
		  if (!running)
		    {
		      finished = false;
		      iIsConfigured = false;
		      // Go to character beyond next end of message
		      iInStartPtr += strlen( (char*) iInStartPtr ) + 1;
		    }
		}
	    }
	}
      
      if (!iIsConfigured)
	{
	  // Move onto next buffer, if finished current
	  if ( (iInBufferActiveWrite != iInBufferActiveRead) &&
	       (iInStartPtr == iInEndPtr[iInBufferActiveRead]) )
	    {
	      iInBufferActiveRead = iInBufferActiveWrite;
	      iInStartPtr = iInBuffer[ iInBufferActiveRead ];
	    }
	  // Are the buffers empty?
	  if (iInStartPtr == iInStopPtr)
	    {
	      finished = true;
	    }
	  else // use next buffer
	    {
	      if (iParserPtr->parseNew( iInStartPtr ))
		{
		  received++;
		  iIsConfigured = true;
		}
	      else
		{
		  // Go to character beyond next end of message
		  iInStartPtr += strlen( (char*) iInStartPtr ) + 1;
		}
	    }
	}
    }
  
  isOpen |= flushOutput();
  
  return isOpen;
}

void
NRS::Interface::BufferedInterface::
sendMessage( Base::Target &aTarget, 
	     const Base::MessageStore &aMessageStore,
	     const Base::IntelligentStore *intelligentPtr )
{
  _ASSERT_( ((!intelligentPtr) ||
	     (aMessageStore.getEncoding() == intelligentPtr->getEncoding())),
	    "Message types do not match!" );
  if (aMessageStore.getEncoding() != iEncodingType)
    { // Need to translate
      Base::VariableNodeDirector &theVND =
	Base::VariableNodeDirector::getDirector();
      bool escaped = false;
      if (aTarget.getMessageDescriptionPtr() == NULL)
	{ // Don't have the message type
	  // If we have the variable, then we know the type
	  // because variables are unique except for standard variables!
	  if (intelligentPtr &&
	      (intelligentPtr->hasSegment( Enum::TargetVNName )))
	    {
	      Type::StringSegment ss;
	      ss.useRawData( intelligentPtr->
			     getSegment( Enum::TargetVNName ),
			     intelligentPtr->getEncoding() );
	      if (theVND.hasVariable( ss.iValue ))
		{ // success!
		  escaped = true;
		}
	    }
	}
      if ((!escaped) && (aTarget.getMessageDescriptionPtr() == NULL))
	{
	  Base::CallbackDirector &theCD =
	    Base::CallbackDirector::getDirector();

	  unsigned_t cb = theCD.addCallback( this );
	  _DEBUG_( "need to translate " << cb );

	  iMsgIDStorePtrMap[cb] = new Store( aTarget, aMessageStore,
					     intelligentPtr, iEncodingType );
	  // Do we have a route?
	  if (aTarget.getRoute() == getEmptyRoute())
	    {
	      _DEBUG_( "Querying route" );
	      Message::QueryRoute *qRoute =
		dynamic_cast< Message::QueryRoute * >
		( theVND.getVariable( Literals::Object::
				      QUERYROUTE_VARIABLE() ) );
	      _ASSERT_( qRoute != NULL, "QueryRoute disappeared" );
	      if (aTarget.getCID().empty())
		_ERROR_( "Message sent with no route" );
	      else
		{
		  if (aTarget.getRoute() == getEmptyRoute())
		    {
		      Base::Target qTarget( aTarget.getCID(), 
					    iPortRoute, 0,
					    &qRoute->getVariableManager().
					    getMessageDescription() );
		      qRoute->request( qTarget, cb, getEmptyRoute() );
		    }
		  else
		    {
		      Base::Target qTarget( aTarget.getCID(), 
					    aTarget.getRoute(), 0,
					    &qRoute->getVariableManager().
					    getMessageDescription() );
		      qRoute->request( qTarget, cb, getEmptyRoute() );
		    }
		}
	    }
	  else if (aTarget.getVNID() == 0)
	    { // a vnid?
	      _DEBUG_( "Querying vnid" );
	      Message::QueryVNID *qVNID =
		dynamic_cast< Message::QueryVNID * >
		( theVND.getVariable( Literals::Object::
				      QUERYVNID_VARIABLE() ) );
	      _ASSERT_( qVNID != NULL, "QueryVNID disappeared" );
	      if ((!intelligentPtr) ||
		  (!intelligentPtr->hasSegment( Enum::TargetVNName )))
		_ERROR_( "Message sent with no target" );
	      else
		{
		  Type::StringSegment ss;
		  ss.useRawData( intelligentPtr->
				 getSegment( Enum::TargetVNName ),
				 intelligentPtr->getEncoding() );
		  Base::Target qTarget( aTarget.getCID(), 
					aTarget.getRoute(), 0,
					&qVNID->getVariableManager().
					getMessageDescription() );
		  qVNID->request( qTarget, cb, ss.iValue );
		}
	    }
	  else // (aTarget.getMessageDescriptionPtr() == NULL) 
	    { // we know we don't have a message description
	      _DEBUG_( "Querying vnType" );
	      Message::QueryVNType *qVNType =
		dynamic_cast< Message::QueryVNType * >
		( theVND.getVariable( Literals::Object::
				      QUERYVNTYPE_VARIABLE() ) );
	      _ASSERT_( qVNType != NULL, "QueryVNType disappeared" );
	      Base::Target qTarget( aTarget.getCID(), aTarget.getRoute(), 0,
				    &qVNType->getVariableManager().
				    getMessageDescription() );
	      qVNType->request( qTarget, cb, aTarget.getVNID() );
	    }
	}
      else
	{ // translate
	  if (escaped)
	    {
	      Type::StringSegment ss;
	      ss.useRawData( intelligentPtr->
			     getSegment( Enum::TargetVNName ),
			     intelligentPtr->getEncoding() );
	      const Base::VariableManager &vm =
		theVND.getVariableManager( theVND.
					   getVariableVNID( ss.iValue ) );
	      Base::Target newTarget( aTarget, &vm.getMessageDescription() );
	      _DEBUG_( "Doing lucky translation to " << vm.getMessageName() );
	      vm.translateMessage( this, newTarget, aMessageStore,
				   intelligentPtr );
	    }
	  else
	    {
	      _DEBUG_( "Do translation to "
		       << aTarget.getMessageDescriptionPtr()->iName );
	      Base::VariableNodeDirector::getDirector().
		getVariableManager( aTarget.
				    getMessageDescriptionPtr()->iName ).
		translateMessage( this, aTarget, aMessageStore,
				  intelligentPtr );
	    }
	}
    }
  else
    {
      data_t data;
      iParserPtr->createOutput( data, aTarget, aMessageStore, intelligentPtr );
      while ( iOutAllocSize - (iOutEndPtr - iOutBuffer) < 
	      data.size() + 1 )
	{
	  extendOutputBuffer();
	}
      memcpy( iOutEndPtr, data.c_str(), data.size() + 1 );
      iOutEndPtr +=  data.size() + 1;
    }
}

void NRS::Interface::BufferedInterface::extendOutputBuffer()
{
  _DEBUG_( "extending output buffers" );
  uint8_t *buff = iOutBuffer;
  iOutAllocSize <<= 1;
  iOutBuffer = (uint8_t *) realloc( iOutBuffer, iOutAllocSize );
  iOutStartPtr = iOutBuffer + (int) (iOutStartPtr - buff);
  iOutEndPtr = iOutBuffer + (int) (iOutEndPtr - buff);
}


bool  NRS::Interface::BufferedInterface::queryWaiting()
{
  return (!iMsgIDStorePtrMap.empty());
}

void NRS::Interface::BufferedInterface::callback( const unsigned_t msgID )
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();
  Base::CallbackDirector &theCD =
    Base::CallbackDirector::getDirector();
  
  _DEBUG_( "Callback translation: " << msgID );

  if (iMsgIDStorePtrMap.find( msgID ) == iMsgIDStorePtrMap.end())
    {
      _ERROR_( "Unused callback offered" );
    }
  else
    {
      bool result = false;
      std::auto_ptr< Store > storePtr;
      storePtr.reset( iMsgIDStorePtrMap[ msgID ] );
      std::auto_ptr< Base::Target > newTargetPtr;
      _ASSERT_( storePtr.get() != NULL, "Illegal empty store" );
      // Do we have a route?
      if (storePtr->iTarget.getRoute() == getEmptyRoute())
	{
	  Message::ReplyRoute *rRoute =
	    dynamic_cast< Message::ReplyRoute * >
	    ( theVND.getVariable( Literals::Object::
				  REPLYROUTE_VARIABLE() ) );
	  _ASSERT_( rRoute != NULL, "ReplyRoute disappeared" );
	  route_t forwardRoute, returnRoute;
	  unsigned_t translationCount;
	  result = rRoute->getRouteFromMessageID( msgID, forwardRoute,
						  returnRoute,
						  translationCount );
	  if (result)
	    {
	      _DEBUG_( "Got route" );
	      newTargetPtr.
		reset( new Base::Target( storePtr->iTarget.getCID(),
					 forwardRoute,
					 storePtr->iTarget.getVNID(),
					 storePtr->iTarget.
					 getMessageDescriptionPtr() ) );
	      storePtr->iISPtr->clearSegment( Enum::TargetCID );
	      storePtr->iISPtr->clearSegment( Enum::IsBroadcast );
	      if (newTargetPtr->getPort() != iPort)
		{
		  result=false;
		}
	    }
	  else
	    {
	      _ERROR_( "Bizarre failed lookup" );
	    }
	}
      else if (storePtr->iTarget.getVNID() == 0)
	{ // a vnid?
	  _DEBUG_( "Got vnid" );
	  Message::ReplyVNID *rVNID =
	    dynamic_cast< Message::ReplyVNID * >
	    ( theVND.getVariable( Literals::Object::
				  REPLYVNID_VARIABLE() ) );
	  _ASSERT_( rVNID != NULL, "ReplyVNID disappeared" );
	  unsigned_t aVNID = 0;
	  result = rVNID->getVNIDFromMessageID( msgID, aVNID );
	  if (result && (aVNID != 0))
	    {
	      newTargetPtr.
		reset( new Base::Target( storePtr->iTarget.getCID(),
					 storePtr->iTarget.getRoute(),
					 aVNID,
					 storePtr->iTarget.
					 getMessageDescriptionPtr() ) );
	      storePtr->iISPtr->clearSegment( Enum::TargetVNName );
	    }
	  else
	    {
	      _ERROR_( "Bizarre failed lookup" );
	    }
	}
      else if (storePtr->iTarget.getMessageDescriptionPtr() == NULL) 
	{ // we know we don't have a message description
	  _DEBUG_( "Got vnType" );
	  Message::ReplyVNType *rVNType =
	    dynamic_cast< Message::ReplyVNType * >
	    ( theVND.getVariable( Literals::Object::
				  REPLYVNTYPE_VARIABLE() ) );
	  _ASSERT_( rVNType != NULL, "ReplyVNType disappeared" );
	  std::string aType;
	  result = rVNType->getVNTypeFromMessageID( msgID, aType );
	  if (result)
	    {
	      if (theVND.hasVariableManager( aType ))
		{
		  _DEBUG_( "Found " << aType );
		  newTargetPtr.
		    reset( new Base::Target( storePtr->iTarget.getCID(),
					     storePtr->iTarget.getRoute(),
					     storePtr->iTarget.getVNID(),
					     &theVND.
					     getVariableManager( aType ).
					     getMessageDescription() ) );
		}
	      else
		{ // try to construct it
		  Base::CSLParser cslP( aType );
		  _DEBUG_( "Trying to create " << aType );
		  Base::VariableManager *vmPtr = cslP.createManager();
		  if (vmPtr != NULL)
		    {
		      _DEBUG_( "success" );
		      newTargetPtr.
			reset( new Base::Target( storePtr->iTarget.getCID(),
						 storePtr->iTarget.getRoute(),
						 storePtr->iTarget.getVNID(),
						 &vmPtr->
						 getMessageDescription() ) );
		    }
		  else
		    {
		      _ABORT_( "Can't translate " << aType );
		    }
		}
	    }
	}
      // Okay, did it work?
      if (result)
	{ // Then move onto next question...
	  iMsgIDStorePtrMap.erase( msgID );
	  theCD.removeCallback( msgID );
	  unsigned_t cb = theCD.addCallback( this );
	  std::auto_ptr< Store > tmpPtr( new Store( *newTargetPtr,
						    storePtr->iMS,
						    storePtr->iISPtr.get(),
						    storePtr->iEncoding ) );
	  storePtr = tmpPtr;
	  Store *newStorePtr = storePtr.get();
	  iMsgIDStorePtrMap[cb] = storePtr.release();
	  
	  if (newStorePtr->iTarget.getVNID() == 0)
	    { // a vnid?
	      Message::QueryVNID *qVNID =
		dynamic_cast< Message::QueryVNID * >
		( theVND.getVariable( Literals::Object::
				      QUERYVNID_VARIABLE() ) );
	      _ASSERT_( qVNID != NULL, "QueryVNID disappeared" );
	      if ((!newStorePtr->iISPtr.get()) ||
		  (!newStorePtr->iISPtr->hasSegment( Enum::TargetVNName )))
		_ERROR_( "Message sent with no target" );
	      else
		{
		  Type::StringSegment ss;
		  ss.useRawData( newStorePtr->iISPtr->
				 getSegment( Enum::TargetVNName ),
				 newStorePtr->iISPtr->getEncoding() );
		  Base::Target qTarget( newStorePtr->iTarget.getCID(),
					newStorePtr->iTarget.getRoute(), 0,
					&qVNID->getVariableManager().
					getMessageDescription() );
		  qVNID->request( qTarget, cb, ss.iValue );
		}
	    }
	  else if (newStorePtr->iTarget.getMessageDescriptionPtr() == NULL) 
	    { // no message description
	      Message::QueryVNType *qVNType =
		dynamic_cast< Message::QueryVNType * >
		( theVND.getVariable( Literals::Object::
				      QUERYVNTYPE_VARIABLE() ) );
	      _ASSERT_( qVNType != NULL, "QueryVNType disappeared" );
	      Base::Target qTarget( newStorePtr->iTarget.getCID(),
				    newStorePtr->iTarget.getRoute(), 0,
				    &qVNType->getVariableManager().
				    getMessageDescription() );
	      qVNType->request( qTarget, cb, newStorePtr->iTarget.getVNID() );
	    }
	  else
	    { // translate
	      _DEBUG_( "Telling variable manager to translate" );
	      Base::VariableNodeDirector::getDirector().
		getVariableManager( newStorePtr->iTarget.
				    getMessageDescriptionPtr()->iName ).
		translateMessage( this, newStorePtr->iTarget,
				  newStorePtr->iMS,
				  newStorePtr->iISPtr.get() );
	      delete newStorePtr;
	      iMsgIDStorePtrMap.erase( cb );
	      theCD.removeCallback( cb );
	    }
	}
    }
}
