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

#include "ReplyNumberType.hh"
#include "VariableNodeDirector.hh"
#include "ExternalInterfaceDirector.hh"
#include "UnsignedManager.hh"
#include "BooleanManager.hh"
#include "Exception.hh"
#include "AttributeLiterals.hh"
#include "ExternalInterface.hh"
#include "Interface.hh"
#include "Target.hh"
#include "ReplyNumberTypeManager.hh"

namespace
{
  NRS::Message::ReplyNumberTypeManager sRLM;
}

NRS::Message::ReplyNumberTypeManager::ReplyNumberTypeManager() : 
  ReplyManager( NRS::Literals::Object::REPLYNUMBERTYPE_VARIABLE(), "", 3 )
{
}

void NRS::Message::ReplyNumberTypeManager::setSegments()
{

  ReplyManager::setSegments();

  iSegTypeVector[ 1 ] = NRS::Type::UnsignedManager::getName();
  iSegNameVector[ 1 ] = NRS::Literals::Attribute::MAXBITS();
  iSegDisplayNameVector[ 1 ] = "Max no. bits in integers";
    
  iSegTypeVector[ 2 ] = NRS::Type::BooleanManager::getName();
  iSegNameVector[ 2 ] = NRS::Literals::Attribute::FLOATINGPOINT();
  iSegDisplayNameVector[ 2 ] = "Handles floating point?";
}

void NRS::Message::ReplyNumberTypeManager::setDescription()
{
  iDescription = "The built-in message type for replies to requests "
    "for language capabilities of a component.";
}

void NRS::Message::ReplyNumberTypeManager::createVariable( std::string aName )
{
  new ReplyNumberType ( aName );
}

bool 
NRS::Message::ReplyNumberTypeManager::deliverMessage( unsigned_t vnid,
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
  unsigned_t maxBits;
  bool floatingPoint = false;

  if (!extractMsgID( attrMap, msgID ))
    return false;
  
  // determine if the message has the necessary maxBits attribute
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::MAXBITS() ) )
    return false;

  maxBits = atol( attrMap[ Literals::Attribute::MAXBITS() ].c_str() );

  // determine if the message has the necessary floatingPoint attribute
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::FLOATINGPOINT() ) )
    return false;

  if ( ( attrMap[ Literals::Attribute::FLOATINGPOINT() ] ==
	 Literals::XML::TRUE() ) ||
       ( attrMap[ Literals::Attribute::FLOATINGPOINT() ] == "1" ) )
    {
      floatingPoint = true;
    }

  dynamic_cast< ReplyNumberType* >( theVND.getVariable( vnid ))->
    receiveMessage( msgID, maxBits, floatingPoint );
  
  // remove attributes from attrMap
  attrMap.erase( NRS::Literals::Attribute::MAXBITS() );
  attrMap.erase( NRS::Literals::Attribute::FLOATINGPOINT() );
  
  return true;
}

bool 
NRS::Message::ReplyNumberTypeManager::
translateMessage( unsigned_t port,
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
  unsigned_t msgID;
  unsigned_t maxBits;
  bool floatingPoint = false;

  Interface::PML::intelligentToBMFM( intMap, NRSAttrMap, intelligent );

  if (!extractMsgID( attrMap, msgID ))
    return false;
  else
    insertMsgID( msg, alloc, data, size, msgID );

  // determine if the message has the necessary maxBits attribute
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::MAXBITS() ) )
    return false;

  maxBits = atol( attrMap[ Literals::Attribute::MAXBITS() ].c_str() );

  while ( !Interface::BMF::segFromUnsignedM( data, maxBits, size ) )
    Interface::BMF::segReallocM( msg, data, alloc, size, alloc );

  // determine if the message has the necessary floatingPoint attribute
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::FLOATINGPOINT() ) )
    return false;

  if ( ( attrMap[ Literals::Attribute::FLOATINGPOINT() ] ==
	 Literals::XML::TRUE() ) ||
       ( attrMap[ Literals::Attribute::FLOATINGPOINT() ] == "1" ) )
    {
      floatingPoint = true;
    }

  while ( !Interface::BMF::segFromBooleanM( data, floatingPoint, size ) )
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
  attrMap.erase( NRS::Literals::Attribute::MAXBITS() );
  attrMap.erase( NRS::Literals::Attribute::FLOATINGPOINT() );
  
  for ( int i = 0; i < Interface::BMF::IMSize; i++ )
    free( intMap[ i ] );
  return true;
}

bool 
NRS::Message::ReplyNumberTypeManager::deliverMessage( unsigned_t vnid,
						   char *&data,
						   bool intelligent,
						   const char* intMap[] )
{
  int segment = 1;
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();
  Base::Target returnTarget;
  unsigned_t msgID;

  if (!extractMsgID( data, msgID ))
    return false;

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;
  unsigned_t maxBits = Interface::BMF::segToUnsignedM( data );

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;
  bool floatingPoint = Interface::BMF::segToBooleanM( data );

  dynamic_cast< ReplyNumberType* >( theVND.getVariable( vnid ))->
    receiveMessage(  msgID, maxBits, floatingPoint );
  
  return true;
}

bool 
NRS::Message::ReplyNumberTypeManager::translateMessage( unsigned_t port,
						      Base::Target &theTarget,
						      char *&data,
						      bool intelligent,
						      const char* intMap[] )
{
  int segment = 1;
  Base::ExternalInterfaceDirector &theEID =
    Base::ExternalInterfaceDirector::getDirector();
  Base::Target returnTarget;
  unsigned_t msgID;
  std::stringstream anSS;

  std::map< std::string, std::string > NRSAttrMap;
  std::map< std::string, std::string > attrMap;

  Interface::BMF::intelligentToPMLM( NRSAttrMap, intMap, intelligent );

  if (!extractMsgID( data, msgID ))
    return false;
  else
    insertMsgID( attrMap, msgID );

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;
  anSS << Interface::BMF::segToUnsignedM( data );
  attrMap[ NRS::Literals::Attribute::MAXBITS() ] = anSS.str();

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;
  anSS.str( std::string() );
  anSS << std::boolalpha << Interface::BMF::segToBooleanM( data );
  attrMap[ NRS::Literals::Attribute::FLOATINGPOINT() ] = anSS.str();
  
  if (!theEID.hasExternalInterface( port ))
    _ABORT_( "Port not found\n\t" );
  
  theEID.getExternalInterface( port )->sendMessage( theTarget,
						    getType().c_str(),
						    NRSAttrMap, attrMap,
						    intelligent );

  return true;
}

void 
NRS::Message::ReplyNumberTypeManager::sendMessage( Base::Target &theTarget,
						   unsigned_t msgID,
						   unsigned_t maxBits,
						   bool floatingPoint )
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
      insertMsgID( attrMap, msgID );

      std::stringstream anSS;
      anSS << maxBits;
      attrMap[ NRS::Literals::Attribute::MAXBITS() ] = anSS.str();
      
      anSS.str( std::string() );
      anSS << std::boolalpha << floatingPoint;
      attrMap[ NRS::Literals::Attribute::FLOATINGPOINT() ] = anSS.str();

      if (!isBroadcast)
	eI->sendMessage( theTarget, iMessage.c_str(), NRSAttrMap, attrMap,
			 intelligent );
    }

  if (isBroadcast || !isPMLNotBMF)
    {
      insertMsgID( msg, alloc, data, size, msgID );

      while (!Interface::BMF::segFromUnsignedM( data, maxBits, size ))
	Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
      
      while (!Interface::BMF::segFromBooleanM( data, floatingPoint, size ))
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
