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

#include "QueryMaxLog.hh"
#include "VariableNodeDirector.hh"
#include "ExternalInterfaceDirector.hh"
#include "UnsignedManager.hh"
#include "Exception.hh"
#include "StringLiterals.hh"
#include "Target.hh"
#include "ExternalInterface.hh"
#include "Interface.hh"
#include "QueryMaxLogManager.hh"

namespace
{
  NRS::Message::QueryMaxLogManager sQMaxLogM;
}

NRS::Message::QueryMaxLogManager::QueryMaxLogManager() : 
  QueryManager( Literals::Object::QUERYMAXLOG_VARIABLE(), "", 3 )
{
}

void NRS::Message::QueryMaxLogManager::setSegments()
{
  QueryManager::setSegments();
}

void NRS::Message::QueryMaxLogManager::setDescription()
{
  iDescription = "The built-in message for requests for numbers of logs";
}

void NRS::Message::QueryMaxLogManager::createVariable( std::string aName )
{
  new QueryMaxLog ( aName );
}

// tells the manager to deliver data derived from a PML message to QueryMaxLog
bool
NRS::Message::QueryMaxLogManager::deliverMessage( unsigned_t vnid,
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
  dynamic_cast< QueryMaxLog* >( theVND.getVariable( vnid ))->
    receiveMessage( returnTarget, msgID );
  return true;
}

bool 
NRS::Message::QueryMaxLogManager::translateMessage( unsigned_t port,
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


bool NRS::Message::QueryMaxLogManager::deliverMessage( unsigned_t vnid,
						      char *&data,
						      bool intelligent,
						      const char* intMap[] )
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();
  Base::Target returnTarget;
  unsigned_t msgID;

  if (!extractReturnTarget( data, intelligent, intMap, returnTarget ))
    return false;

  if (!extractMsgID( data, msgID ))
    return false;

  dynamic_cast< QueryMaxLog* >( theVND.getVariable( vnid ))->
    receiveMessage( returnTarget, msgID );
  
  return true;
}

bool 
NRS::Message::QueryMaxLogManager::translateMessage( unsigned_t port,
						   Base::Target &theTarget,
						   char *&data,
						   bool intelligent,
						   const char* intMap[] )
{
  Base::ExternalInterfaceDirector &theEID = 
    Base::ExternalInterfaceDirector::getDirector();
  Base::Target returnTarget;
  unsigned_t msgID;
  std::map< std::string, std::string > NRSAttrMap;
  std::map< std::string, std::string > attrMap;
  
  Interface::BMF::intelligentToPMLM( NRSAttrMap, intMap, intelligent );

  if (!extractReturnTarget( data, intelligent, intMap, returnTarget ))
    {
      return false;
    }
  else
    {
      insertReturnTarget( attrMap, attrMap, intelligent, returnTarget );
    }
  
  if (!extractMsgID( data, msgID ))
    return false;
  else
    insertMsgID( attrMap, msgID );

  if (!theEID.hasExternalInterface( port ))
    _ABORT_( "Port not found\n\t" );
  
  theEID.getExternalInterface( port )->sendMessage( theTarget,
						    getType().c_str(),
						   NRSAttrMap, attrMap,
						   intelligent );

  return true;
}

void 
NRS::Message::QueryMaxLogManager::sendMessage( Base::Target &theTarget,
					       Base::Target &returnTarget,
					       unsigned_t msgID )
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
      insertMsgID( attrMap, msgID );

      if (!isBroadcast)
	eI->sendMessage( theTarget, iMessage.c_str(), NRSAttrMap, attrMap,
			 intelligent );
    }

  if (isBroadcast || !isPMLNotBMF)
    {
      insertReturnTarget( msg, alloc, data, size,
			  intelligent, intMap, returnTarget );
      insertMsgID( msg, alloc, data, size, msgID );

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
