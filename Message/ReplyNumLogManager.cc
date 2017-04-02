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

#include "ReplyNumLog.hh"
#include "VariableNodeDirector.hh"
#include "ExternalInterfaceDirector.hh"
#include "UnsignedManager.hh"
#include "Exception.hh"
#include "AttributeLiterals.hh"
#include "ExternalInterface.hh"
#include "Interface.hh"
#include "Target.hh"
#include "ReplyNumLogManager.hh"

namespace
{
  NRS::Message::ReplyNumLogManager sRNumLogM;
}

NRS::Message::ReplyNumLogManager::ReplyNumLogManager() : 
  ReplyManager( Literals::Object::REPLYNUMLOG_VARIABLE(), "", 2 )
{
}

void NRS::Message::ReplyNumLogManager::setSegments()
{

  ReplyManager::setSegments();

  iSegTypeVector[ 1 ] = NRS::Type::UnsignedManager::getName();
  iSegNameVector[ 1 ] = NRS::Literals::Attribute::NUMLOGS();
  iSegDisplayNameVector[ 1 ] = "Number of logs";
}

void NRS::Message::ReplyNumLogManager::setDescription()
{
  iDescription = "The built-in message type for replies to requests "
    "for number of logs.";
}

void NRS::Message::ReplyNumLogManager::createVariable( std::string aName )
{
  new ReplyNumLog ( aName );
}

bool 
NRS::Message::ReplyNumLogManager::deliverMessage( unsigned_t vnid,
						 std::map< std::string,
						 std::string > &NRSAttrMap,
						 std::map< std::string,
						 std::string > &attrMap,
						 bool intelligent,
						 const char *contents )
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  unsigned_t msgID;

  if (!extractMsgID( attrMap, msgID ))
    return false;

  // determine if the message has the necessary cType
  if ( !msgHasAttribute( attrMap, Literals::Attribute::NUMLOGS() ) )
    return false;

  dynamic_cast< ReplyNumLog* >( theVND.getVariable( vnid ))->
    receiveMessage( msgID,
		    atol( attrMap[ NRS::Literals::Attribute::NUMLOGS() ].c_str() )
		    );
  
  // remove attributes from attrMap
  attrMap.erase( NRS::Literals::Attribute::NUMLOGS() );
  
  return true;
}

bool 
NRS::Message::ReplyNumLogManager::translateMessage( unsigned_t port,
						   Base::Target &theTarget,
						   std::map< std::string,
						   std::string > &NRSAttrMap,
						   std::map< std::string,
						   std::string > &attrMap,
						   bool intelligent,
						   const char *contents )
{
  Base::ExternalInterfaceDirector &theEID =
    Base::ExternalInterfaceDirector::getDirector();

  char *intMap[ Interface::BMF::IMSize ];
  for ( int i = 0; i < Interface::BMF::IMSize; i++ )
    intMap[ i ] = NULL;
  static char *msg = (char *) malloc( Interface::BMF::DefaultMessageSize );
  static size_t alloc = Interface::BMF::DefaultMessageSize;
  char *data = msg;
  size_t size = alloc;
  unsigned_t msgID;

  Interface::PML::intelligentToBMFM( intMap, NRSAttrMap, intelligent );

  if (!extractMsgID( attrMap, msgID ))
    return false;
  else
    insertMsgID( msg, alloc, data, size, msgID );

  // determine if the message has the necessary vnName
  if ( !msgHasAttribute( attrMap, Literals::Attribute::NUMLOGS() ) )
    return false;

  while (!Interface::BMF::
	 segFromUnsignedM( data,
			   atol( attrMap[ Literals::Attribute::
					     NUMLOGS() ].c_str() ),
			   size ))
    Interface::BMF::segReallocM( msg, data, alloc, size, alloc );

  Interface::BMF::segFromFinishedM( data, size );
  
  if (!theEID.hasExternalInterface( port ))
    _ABORT_( "Port not found\n\t" );
  theEID.getExternalInterface( port )->sendMessage( theTarget,
						    msg,
						    alloc - size,
						    intelligent,
						    (const char**) intMap );
						   
  
  // remove attributes from attrMap
  attrMap.erase( Literals::Attribute::NUMLOGS() );
  
  for ( int i = 0; i < Interface::BMF::IMSize; i++ )
    free( intMap[ i ] );
  return true;
}

bool NRS::Message::ReplyNumLogManager::deliverMessage( unsigned_t vnid,
						      char *&data,
						      bool intelligent,
						      const char* intMap[] )
{
  int segment = 1;
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();
  unsigned_t msgID;

  if (!extractMsgID( data, msgID ))
    return false;

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;

  unsigned_t numLogs = Interface::BMF::segToUnsignedM( data );

  dynamic_cast< ReplyNumLog* >( theVND.getVariable( vnid ) )->
    receiveMessage( msgID, numLogs );
  
  return true;
}

bool 
NRS::Message::ReplyNumLogManager::translateMessage( unsigned_t port,
						   Base::Target &theTarget,
						   char *&data,
						   bool intelligent,
						   const char* intMap[] )
{
  int segment = 1;
  Base::ExternalInterfaceDirector &theEID =
    Base::ExternalInterfaceDirector::getDirector();
  unsigned_t msgID;

  std::map< std::string, std::string > NRSAttrMap;
  std::map< std::string, std::string > attrMap;

  Interface::BMF::intelligentToPMLM( NRSAttrMap, intMap, intelligent );

  if (!extractMsgID( data, msgID ))
    return false;
  else
    insertMsgID( attrMap, msgID );

  if (!msgHasAttribute( data, iSegNameVector[ segment++ ].c_str() ))
    return false;

  std::stringstream anSS;
  anSS << Interface::BMF::segToUnsignedM( data );

  attrMap[ Literals::Attribute::NUMLOGS() ] = anSS.str();

  if (!theEID.hasExternalInterface( port ))
    _ABORT_( "Port not found\n\t" );

  theEID.getExternalInterface( port )->sendMessage( theTarget,
						    getType().c_str(),
						    NRSAttrMap, attrMap,
						    intelligent );
  
  return true;
}

void 
NRS::Message::ReplyNumLogManager::sendMessage( Base::Target &theTarget,
					       unsigned_t msgID,
					       unsigned_t numLogs )
{
  Base::ExternalInterfaceDirector &theEID =
    Base::ExternalInterfaceDirector::getDirector();
  unsigned_t port;
  Base::ExternalInterface * eI = NULL;

  char *intMap[ Interface::BMF::IMSize ];
  for ( int i = 0; i < Interface::BMF::IMSize; i++ )
    intMap[ i ] = NULL;
  static char *msg = (char *) malloc( Interface::BMF::DefaultMessageSize );
  static size_t alloc = Interface::BMF::DefaultMessageSize;
  char *data = msg;
  size_t size = alloc;

  bool intelligent = false;
  bool isBroadcast = theTarget.isBroadcast();
  bool isPMLNotBMF = false;
  std::map< std::string, std::string > NRSAttrMap;
  std::map< std::string, std::string > attrMap;

  if (!isBroadcast)
    {
      port = theTarget.getPort();
      if (!theEID.hasExternalInterface( port ))
	_ABORT_( "Message going to non-existent port " << port << "\n\t" );
      eI = theEID.getExternalInterface( port );
      isPMLNotBMF = eI->isPMLNotBMF();
    }
  else
    {
      _DEBUG_("broadcasting");
    }

  if (isBroadcast || isPMLNotBMF)
    {
      insertMsgID( attrMap, msgID );

      std::stringstream anSS;
      anSS << numLogs;

      attrMap[ Literals::Attribute::NUMLOGS() ] = anSS.str();

      if (!isBroadcast)
	eI->sendMessage( theTarget, iMessage.c_str(), NRSAttrMap, attrMap,
			 intelligent );
    }

  if (isBroadcast || !isPMLNotBMF)
    {
      insertMsgID( msg, alloc, data, size, msgID );

      while (!Interface::BMF::segFromUnsignedM( data, numLogs, size ))
	Interface::BMF::segReallocM( msg, data, alloc, size, alloc );

      Interface::BMF::segFromFinishedM( data, size );
      
      size = alloc - size;
      
      if (!isBroadcast)
	eI->sendMessage( theTarget, msg, size, intelligent,
			 (const char **) intMap );
    }
  
  if (isBroadcast)
    {
      for ( port = 0; port < theEID.getMaxPort(); port++ )
	{
	  if (theEID.hasExternalInterface( port ))
	    {
	      eI = theEID.getExternalInterface( port );
	      if (eI->isPMLNotBMF())
		eI->sendMessage( theTarget, iMessage.c_str(),
				 NRSAttrMap, attrMap, intelligent );
	      else
		eI->sendMessage( theTarget, msg, size, intelligent,
				 (const char **) intMap );
	    }
	}
    }

  for ( int i = 0; i < Interface::BMF::IMSize; i++ )
    free( intMap[ i ] );
}
