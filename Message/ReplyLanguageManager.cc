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

#include "ReplyLanguage.hh"
#include "VariableNodeDirector.hh"
#include "ExternalInterfaceDirector.hh"
#include "BooleanManager.hh"
#include "Exception.hh"
#include "AttributeLiterals.hh"
#include "ExternalInterface.hh"
#include "Interface.hh"
#include "Target.hh"
#include "ReplyLanguageManager.hh"

namespace
{
  NRS::Message::ReplyLanguageManager sRLM;
}

NRS::Message::ReplyLanguageManager::ReplyLanguageManager() : 
  ReplyManager( NRS::Literals::Object::REPLYLANGUAGE_VARIABLE(), "", 3 )
{
}

void NRS::Message::ReplyLanguageManager::setSegments()
{

  ReplyManager::setSegments();

  iSegTypeVector[ 1 ] = NRS::Type::BooleanManager::getName();
  iSegNameVector[ 1 ] = NRS::Literals::Attribute::SPEAKSBMF();
  iSegDisplayNameVector[ 1 ] = "Speaks BMF?";
    
  iSegTypeVector[ 2 ] = NRS::Type::BooleanManager::getName();
  iSegNameVector[ 2 ] = NRS::Literals::Attribute::SPEAKSPML();
  iSegDisplayNameVector[ 2 ] = "Speaks PML?";
}

void NRS::Message::ReplyLanguageManager::setDescription()
{
  iDescription = "The built-in message type for replies to requests "
    "for language capabilities of a component.";
}

void NRS::Message::ReplyLanguageManager::createVariable( std::string aName )
{
  new ReplyLanguage ( aName );
}

bool 
NRS::Message::ReplyLanguageManager::deliverMessage( unsigned_t vnid,
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
  bool speaksBMF = false;
  bool speaksPML = false;

  if (!extractMsgID( attrMap, msgID ))
    return false;
  
  // determine if the message has the necessary speaksBMF attribute
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::SPEAKSBMF() ) )
    return false;

  if ( ( attrMap[ Literals::Attribute::SPEAKSBMF() ] == Literals::XML::TRUE() ) ||
       ( attrMap[ Literals::Attribute::SPEAKSBMF() ] == "1" ) )
    {
      speaksBMF = true;
    }

  // determine if the message has the necessary speaksPML attribute
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::SPEAKSPML() ) )
    return false;

  if ( ( attrMap[ Literals::Attribute::SPEAKSPML() ] == Literals::XML::TRUE() ) ||
       ( attrMap[ Literals::Attribute::SPEAKSPML() ] == "1" ) )
    {
      speaksPML = true;
    }

  dynamic_cast< ReplyLanguage* >( theVND.getVariable( vnid ))->
    receiveMessage( msgID, speaksBMF, speaksPML );
  
  // remove attributes from attrMap
  attrMap.erase( NRS::Literals::Attribute::SPEAKSBMF() );
  attrMap.erase( NRS::Literals::Attribute::SPEAKSPML() );
  
  return true;
}

bool 
NRS::Message::ReplyLanguageManager::translateMessage( unsigned_t port,
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
  bool speaksBMF = false;
  bool speaksPML = false;

  Interface::PML::intelligentToBMFM( intMap, NRSAttrMap, intelligent );

  if (!extractMsgID( attrMap, msgID ))
    return false;
  else
    insertMsgID( msg, alloc, data, size, msgID );

  // determine if the message has the necessary speaksBMF attribute
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::SPEAKSBMF() ) )
    return false;

  if ( ( attrMap[ Literals::Attribute::SPEAKSBMF() ] == Literals::XML::TRUE() ) ||
       ( attrMap[ Literals::Attribute::SPEAKSBMF() ] == "1" ) )
    {
      speaksBMF = true;
    }

  while ( !Interface::BMF::segFromBooleanM( data, speaksBMF, size ) )
    Interface::BMF::segReallocM( msg, data, alloc, size, alloc );

  // determine if the message has the necessary speaksPML attribute
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::SPEAKSPML() ) )
    return false;

  if ( ( attrMap[ Literals::Attribute::SPEAKSPML() ] == Literals::XML::TRUE() ) ||
       ( attrMap[ Literals::Attribute::SPEAKSPML() ] == "1" ) )
    {
      speaksPML = true;
    }

  while ( !Interface::BMF::segFromBooleanM( data, speaksPML, size ) )
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
  attrMap.erase( NRS::Literals::Attribute::SPEAKSBMF() );
  attrMap.erase( NRS::Literals::Attribute::SPEAKSPML() );
  
  for ( int i = 0; i < Interface::BMF::IMSize; i++ )
    free( intMap[ i ] );
  return true;
}

bool 
NRS::Message::ReplyLanguageManager::deliverMessage( unsigned_t vnid,
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
  bool speaksBMF = Interface::BMF::segToBooleanM( data );

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;
  bool speaksPML = Interface::BMF::segToBooleanM( data );

  dynamic_cast< ReplyLanguage* >( theVND.getVariable( vnid ))->
    receiveMessage(  msgID, speaksBMF, speaksPML );
  
  return true;
}

bool 
NRS::Message::ReplyLanguageManager::translateMessage( unsigned_t port,
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
  anSS << std::boolalpha << Interface::BMF::segToBooleanM( data );
  attrMap[ NRS::Literals::Attribute::SPEAKSBMF() ] = anSS.str();

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;

  anSS.str( std::string() );
  anSS << std::boolalpha << Interface::BMF::segToBooleanM( data );
  attrMap[ NRS::Literals::Attribute::SPEAKSPML() ] = anSS.str();
  
  if (!theEID.hasExternalInterface( port ))
    _ABORT_( "Port not found\n\t" );
  
  theEID.getExternalInterface( port )->sendMessage( theTarget,
						    getType().c_str(),
						   NRSAttrMap, attrMap,
						   intelligent );

  return true;
}

void NRS::Message::ReplyLanguageManager::sendMessage( Base::Target &theTarget,
						      unsigned_t msgID,
						      bool speaksBMF,
						      bool speaksPML )
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
      anSS << std::boolalpha << speaksBMF;
      attrMap[ NRS::Literals::Attribute::SPEAKSBMF() ] = anSS.str();
      
      anSS.str( std::string() );
      anSS << std::boolalpha << speaksPML;
      attrMap[ NRS::Literals::Attribute::SPEAKSPML() ] = anSS.str();

      if (!isBroadcast)
	eI->sendMessage( theTarget, iMessage.c_str(), NRSAttrMap, attrMap,
			 intelligent );
    }

  if (isBroadcast || !isPMLNotBMF)
    {
      insertMsgID( msg, alloc, data, size, msgID );

      while (!Interface::BMF::segFromBooleanM( data, speaksBMF, size ))
	Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
      
      while (!Interface::BMF::segFromBooleanM( data, speaksPML, size ))
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
