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
#include <sstream>

#include "QueryLog.hh"
#include "VariableNodeDirector.hh"
#include "ExternalInterfaceDirector.hh"
#include "UnsignedManager.hh"
#include "Exception.hh"
#include "AttributeLiterals.hh"
#include "Target.hh"
#include "ExternalInterface.hh"
#include "Interface.hh"
#include "QueryLogManager.hh"

namespace
{
  NRS::Message::QueryLogManager sQLogM;
}

NRS::Message::QueryLogManager::QueryLogManager() : 
  QueryManager( Literals::Object::QUERYLOG_VARIABLE(), "", 4 )
{
  
}

void NRS::Message::QueryLogManager::setSegments()
{
  QueryManager::setSegments();

  iSegTypeVector[ 3 ] = NRS::Type::UnsignedManager::getName();
  iSegNameVector[ 3 ] = NRS::Literals::Attribute::LOG();
  iSegDisplayNameVector[ 3 ] = "log number";
}

void NRS::Message::QueryLogManager::setDescription()
{
  iDescription = "The built-in message for requests for logs";
}

void NRS::Message::QueryLogManager::createVariable( std::string aName )
{
  new QueryLog ( aName );
}

// tells the manager to deliver data derived from a PML message to QueryLog
bool
NRS::Message::QueryLogManager::deliverMessage( unsigned_t vnid,
					       std::map< std::string,
					       std::string > &NRSAttrMap,
					       std::map< std::string,
					       std::string > &attrMap,
					       bool intelligent,
					       const char *contents )
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();
  Base::Target returnTarget;
  unsigned_t msgID;

  if (!extractReturnTarget( NRSAttrMap, attrMap, intelligent, returnTarget ))
    return false;

  if (!extractMsgID( attrMap, msgID ))
    return false;

  // determine if the message has the necessary log
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::LOG() ) )
    return false;
  unsigned_t aLog = atol( attrMap[ Literals::Attribute::LOG() ].c_str() );
  dynamic_cast< QueryLog* >( theVND.getVariable( vnid ))->
    receiveMessage( returnTarget, msgID, aLog );
  
  // remove extracted attributes from attrMap
  attrMap.erase ( NRS::Literals::Attribute::LOG() );
  return true;
}

bool 
NRS::Message::QueryLogManager::translateMessage( unsigned_t port,
						 Base::Target &theTarget,
						 std::map< std::string,
						 std::string > 
						 &NRSAttrMap,
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
  Base::Target returnTarget;
  unsigned_t msgID;

  Interface::PML::intelligentToBMFM( intMap, NRSAttrMap, intelligent );

  if (!extractReturnTarget( NRSAttrMap, attrMap, intelligent, returnTarget ))
    return false;
  else
    insertReturnTarget( msg, alloc, data, size,
			intelligent, intMap, returnTarget );

  if (!extractMsgID( attrMap, msgID ))
    return false;
  else
    insertMsgID( msg, alloc, data, size, msgID );

  // determine if the message has the necessary log
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::LOG() ) )
    return false;

  while ( !Interface::BMF::
	  segFromUnsignedM( data, 
			    atol( attrMap[ Literals::Attribute::LOG() ].
				  c_str() ), size ) )
    Interface::BMF::segReallocM( msg, data, alloc, size, alloc );

  Interface::BMF::segFromFinishedM( data, size );

  if (!theEID.hasExternalInterface( port ))
    _ABORT_( "Port not found\n\t" );
  theEID.getExternalInterface( port )->sendMessage( theTarget,
						    msg,
						    alloc - size,
						    intelligent,
						    (const char**) intMap );

  for ( int i = 0; i < Interface::BMF::IMSize; i++ )
    free( intMap[ i ] );
  return true;
}


bool NRS::Message::QueryLogManager::deliverMessage( unsigned_t vnid,
						    char *&data,
						    bool intelligent,
						    const char* intMap[] )
{
  int segment = 3;
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();
  Base::Target returnTarget;
  unsigned_t msgID;

  if (!extractReturnTarget( data, intelligent, intMap, returnTarget ))
    return false;

  if (!extractMsgID( data, msgID ))
    return false;

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;

  unsigned_t aLog = Interface::BMF::segToUnsignedM( data );

  dynamic_cast< QueryLog* >( theVND.getVariable( vnid ))->
    receiveMessage( returnTarget, msgID, aLog );
  
  return true;
}

bool 
NRS::Message::QueryLogManager::translateMessage( unsigned_t port,
						 Base::Target &theTarget,
						 char *&data,
						 bool intelligent,
						 const char* intMap[] )
{
  int segment = 3;
  Base::ExternalInterfaceDirector &theEID = 
    Base::ExternalInterfaceDirector::getDirector();
  Base::Target returnTarget;
  unsigned_t msgID;
  std::map< std::string, std::string > NRSAttrMap;
  std::map< std::string, std::string > attrMap;
  
  Interface::BMF::intelligentToPMLM( NRSAttrMap, intMap, intelligent );

  if (!extractReturnTarget( data, intelligent, intMap, returnTarget ))
    return false;
  else
    insertReturnTarget( NRSAttrMap, attrMap, intelligent, returnTarget );
  
  if (!extractMsgID( data, msgID ))
    return false;
  else
    insertMsgID( attrMap, msgID );

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;
  
  std::stringstream anSS;
  anSS << Interface::BMF::segToUnsignedM( data );
  
  attrMap[ NRS::Literals::Attribute::LOG() ] = anSS.str();

  if (!theEID.hasExternalInterface( port ))
    _ABORT_( "Port not found\n\t" );
  
  theEID.getExternalInterface( port )->sendMessage( theTarget,
						    getType().c_str(),
						    NRSAttrMap, attrMap,
						    intelligent );

  return true;
}

void 
NRS::Message::QueryLogManager::sendMessage( Base::Target &theTarget,
					    Base::Target &returnTarget,
					    unsigned_t msgID,
					    unsigned_t aLog )
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

  if (isBroadcast || isPMLNotBMF)
    {
      insertReturnTarget( NRSAttrMap, attrMap, intelligent, returnTarget );
      insertMsgID( NRSAttrMap, msgID );

      std::stringstream anSS;
      anSS << aLog;
      attrMap[ NRS::Literals::Attribute::LOG() ] = anSS.str();

      if (!isBroadcast)
	eI->sendMessage( theTarget, iMessage.c_str(), NRSAttrMap, attrMap,
			 intelligent );
    }

  if (isBroadcast || !isPMLNotBMF)
    {
      insertReturnTarget( msg, alloc, data, size,
			  intelligent, intMap, returnTarget );
      insertMsgID( msg, alloc, data, size, msgID );

      while (!Interface::BMF::segFromUnsignedM( data, aLog, size ))
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
