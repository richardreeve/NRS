/*
 * Copyright (C) 2005 Richard Reeve and Edinburgh University
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

#include "Encoding.hh"
#include "Exception.hh"
#include "IntelligentStore.hh"
#include "Link.hh"
#include "Message.hh"
#include "Segment.hh"
#include "Location.hh"

NRS::Base::Link::Link( const VariableManager &theVMRef, integer_t aVNID ) :
  iEncoding( theVMRef.getMessageDescription() ),
  iVM( theVMRef ), iInstantTransmit( false ),
  iReceivedMessage( false ),
  iMinIn( 0 ), iMaxIn( 0 ), iNumIn( 0 ),
  iMinOut( 0 ), iMaxOut( 0 ), iNumOut( 0 ),
  iMinLog( 0 ), iMaxLog( 0 ), iNumLog( 0 ),
  iVNID( aVNID )
{
}

NRS::Base::Link::~Link()
{
  _DEBUG_( "Destroying links" );
  if (iVNID > 0)
    {
      _DEBUG_( "Deleting links: " << iSource.size() );
      for ( unsigned i = 0; i < iSource.size(); ++i )
	{
          _DEBUG_( "iSource[i].iVNID = " << iSource[i].getVNID() ); 
	  Target aTarget( iSource[i], &iVM.getMessageDescription() );
	  aTarget.deleteLink( iVNID );
	}
      for ( unsigned i = 0; i < iMessagePtr.size(); ++i )
	iMessagePtr[i]->getTarget().deleteLink( iVNID );
    }
  _DEBUG_( "Deleting message pointers: " << iMessagePtr.size() );
  for ( unsigned i = 0; i < iMessagePtr.size(); ++i )
    delete iMessagePtr[i];
}

void NRS::Base::Link::receiveMessage( const MessageStore &aMessageStore,
				      const IntelligentStore *intelligentPtr )
{
  iEncoding.setMessage( aMessageStore, intelligentPtr );

  receiveMessage();
}

void NRS::Base::Link::receiveMessage()
{
  update();

  //if (iInstantTransmit)
  sendMessages();
}

void NRS::Base::Link::setInstantTransmission( bool instantTransmit )
{
  iInstantTransmit = instantTransmit;
}

void NRS::Base::Link::addSource( const Location &aSourceRef )
{
  iSource.push_back( aSourceRef );
  ++iNumIn;
}

bool NRS::Base::Link::removeSource( const Location &aSourceRef )
{
  _DEBUG_( "Removing some source" );
  typeof( iSource.begin() ) iter;

  for ( iter = iSource.begin();
	(iter != iSource.end()) && (aSourceRef != *iter);
	++iter );

  if (iter != iSource.end())
    {
      --iNumIn;
      iSource.erase( iter );
      return true;
    }
  return false;
}

void NRS::Base::Link::addTarget( const Target &aTarget )
{
  const VariableManager *aVMPtr = &iVM;
  bool found = false;
  const std::string &msgName = aTarget.getMessageDescriptionPtr()->iName;

  while (!found && (aVMPtr))
    {
      _DEBUG_( "testing " << aVMPtr->getMessageDescription().iName
	       << "==" << msgName );
      if (aVMPtr->getMessageDescription().iName != msgName)
	aVMPtr = aVMPtr->getParent();
      else
	found = true;
    }

  if (!found)
    _ABORT_( "Trying to connect to illegal destination" );
  
  ++iNumOut;

  iMessagePtr.push_back( new Message( aTarget ) );
  if (!aTarget.isLocal())
    iMessagePtr.back()->setEncodingStore( &iEncoding );
}

bool NRS::Base::Link::removeTarget( const Target &aTarget )
{
  _DEBUG_( "Removing some target" );
  typeof( iMessagePtr.begin() ) iter = iMessagePtr.begin();

  while ((iter != iMessagePtr.end()) && (aTarget != (*iter)->getTarget()))
    ++iter;
  
  if (iter != iMessagePtr.end())
    {
      --iNumOut;
      delete *iter;
      iMessagePtr.erase( iter );
      return true;
    }
  return false;
}

void NRS::Base::Link::addLog( Log *aLogPtr )
{
  ++iNumLog;
  _BUG_( "Unimplemented" );
}

bool NRS::Base::Link::removeLog( const Log *aLogPtr )
{
  --iNumLog;
  _BUG_( "Unimplemented" );
  return false;
}

bool NRS::Base::Link::resolveDependencies()
{
  if (iMinIn > iNumIn)
    return false;
  if ((iMaxIn != -1) && (iMaxIn < iNumIn))
    return false;
  if (iMinOut > iNumOut)
    return false;
  if ((iMaxOut != -1) && (iMaxOut < iNumOut))
    return false;
  if (iMinLog > iNumLog)
    return false;
  if ((iMaxLog != -1) && (iMaxLog < iNumLog))
    return false;

  return true;
}

void NRS::Base::Link::setConnections( int min_in, int max_in,
				      int min_out, int max_out,
				      int min_log, int max_log )
{
  iMinIn = min_in;
  iMaxIn = max_in;
  iMinOut = min_out;
  iMaxOut = max_out;
  iMinLog = min_log;
  iMaxLog = max_log;
}

void NRS::Base::Link::update()
{
  iReceivedMessage = true;
  iEncoding.update();
}

void NRS::Base::Link::sendMessages()
{
  if (iReceivedMessage)
    {
      _DEBUG_( "link start" );
      iReceivedMessage = false;
      alertObservers();
      for ( unsigned i = 0; i < iMessagePtr.size(); ++i )
	iMessagePtr[i]->sendMessages();
      _DEBUG_( "link end" );
    }
}
