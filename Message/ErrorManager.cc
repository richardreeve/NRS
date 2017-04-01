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

#include <sstream>
#include <string>

#pragma implementation
#include "ErrorManager.hh"

#include "Error.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"
#include "TokenManager.hh"
#include "Exception.hh"
#include "AttributeLiterals.hh"
#include "UnsignedManager.hh"
#include "ExternalInterfaceDirector.hh"
#include "Interface.hh"
#include "Target.hh"
#include "Executive.hh"

namespace
{
  NRS::Message::ErrorManager sEM;
}

NRS::Message::ErrorManager::ErrorManager() : 
  NRS::Base::VariableManager( NRS::Literals::Object::ERROR_VARIABLE(),
			      "", 3, "", false )
{
  Base::Executive::getExecutive().addStartupVariable( getType(), getType() );
}

void NRS::Message::ErrorManager::setSegments()
{
  NRS::Base::VariableManager::setSegments();

  iSegTypeVector[ 0 ] = NRS::Type::UnsignedManager::getName();
  iSegNameVector[ 0 ] = NRS::Literals::Attribute::PRIORITY();
  iSegDisplayNameVector[ 0 ] = "Priority of error message";

  iSegTypeVector[ 1 ] = NRS::Type::UnsignedManager::getName();
  iSegNameVector[ 1 ] = NRS::Literals::Attribute::ERRID();
  iSegDisplayNameVector[ 1 ] = "ID of error message";

  iSegTypeVector[ 2 ] = NRS::Type::StringManager::getName();
  iSegIsUnitVector[ 2 ] = false;
  iSegIsStringVector[ 2 ] = true;
  iSegStringHasRestrictionVector[ 2 ] = false;
  iSegNameVector[ 2 ] = NRS::Literals::Attribute::ERRSTRING();
  iSegDisplayNameVector[ 2 ] = "Description of error";

  for ( int i = 0; i < iNumSeg; i++ )
    iSegNRSANamespaceVector[i] = false;
}

void NRS::Message::ErrorManager::setDescription()
{
  iDescription = "The built-in type for error messages.";
}

void NRS::Message::ErrorManager::createVariable( std::string aName )
{
  new Error( aName );
}

bool 
NRS::Message::ErrorManager::deliverMessage( unsigned_t vnid,
					    std::map< std::string,
					    std::string > &NRSAttrMap,
					    std::map< std::string,
					    std::string > &attrMap,
					    bool intelligent,
					    const char *contents )
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  // determine if the message has the necessary priority
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::PRIORITY() ) )
    return false;
  unsigned_t aPriority =
    atol( attrMap[ Literals::Attribute::PRIORITY() ].c_str() );

  // determine if the message has the necessary errID
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::ERRID() ) )
    return false;
  unsigned_t anErrID = atol( attrMap[ Literals::Attribute::ERRID() ].c_str() );

  // determine if the message has the necessary vnName
  if ( !msgHasAttribute( attrMap, Literals::Attribute::ERRSTRING() ) )
    return false;

  const char *anError = attrMap[ NRS::Literals::Attribute::ERRSTRING() ].c_str();

  dynamic_cast< Error* >( theVND.getVariable( vnid ))->
    receiveMessage( aPriority, anErrID, anError );

  // remove attributes from attrMap
  attrMap.erase( NRS::Literals::Attribute::PRIORITY() );
  attrMap.erase( NRS::Literals::Attribute::ERRID() );
  attrMap.erase( NRS::Literals::Attribute::ERRSTRING() );
  
  return true;
}

bool 
NRS::Message::ErrorManager::translateMessage( unsigned_t port,
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

  Interface::PML::intelligentToBMFM( intMap, NRSAttrMap, intelligent );

  // determine if the message has the necessary priority
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::PRIORITY() ) )
    return false;

  while ( !Interface::BMF::
	  segFromUnsignedM( data, 
			    atol( attrMap[ Literals::Attribute::
					      PRIORITY() ].c_str() ),
			    size ) )
    Interface::BMF::segReallocM( msg, data, alloc, size, alloc );

  // determine if the message has the necessary errID
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::ERRID() ) )
    return false;

  while ( !Interface::BMF::
	  segFromUnsignedM( data, 
			    atol( attrMap[ Literals::Attribute::
					      ERRID() ].c_str() ),
			    size ) )
    Interface::BMF::segReallocM( msg, data, alloc, size, alloc );

  // determine if the message has the necessary vnName
  if ( !msgHasAttribute( attrMap, Literals::Attribute::ERRSTRING() ) )
    return false;

  while (!Interface::BMF::segFromStringM( data,
					  attrMap[ Literals::Attribute::
						      ERRSTRING() ].c_str(),
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
  attrMap.erase( Literals::Attribute::PRIORITY() );
  attrMap.erase( Literals::Attribute::ERRID() );
  attrMap.erase( Literals::Attribute::ERRSTRING() );
  
  for ( int i = 0; i < Interface::BMF::IMSize; i++ )
    free( intMap[ i ] );
  return true;
}

bool NRS::Message::ErrorManager::deliverMessage( unsigned_t vnid,
						 char *&data,
						 bool intelligent,
						 const char* intMap[] )
{
  int segment = 0;
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;
  unsigned_t aPriority = Interface::BMF::segToUnsignedM( data );

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;
  unsigned_t anErrID = Interface::BMF::segToUnsignedM( data );

  if (!msgHasAttribute( data , iSegNameVector[ segment++ ].c_str() ))
    return false;
  const char *anError = Interface::BMF::segToStringM( data );

  dynamic_cast< Error* >( theVND.getVariable( vnid ) )->
    receiveMessage( aPriority, anErrID, anError );
  
  return true;
}

bool 
NRS::Message::ErrorManager::translateMessage( unsigned_t port,
					      Base::Target &theTarget,
					      char *&data,
					      bool intelligent,
					      const char* intMap[] )
{
  int segment = 0;
  Base::ExternalInterfaceDirector &theEID =
    Base::ExternalInterfaceDirector::getDirector();

  std::map< std::string, std::string > NRSAttrMap;
  std::map< std::string, std::string > attrMap;

  Interface::BMF::intelligentToPMLM( NRSAttrMap, intMap, intelligent );

  if (!msgHasAttribute( data, iSegNameVector[ segment++ ].c_str() ))
    return false;

  std::stringstream anSS;
  anSS << Interface::BMF::segToUnsignedM( data );

  attrMap[ NRS::Literals::Attribute::PRIORITY() ] = anSS.str();

  if (!msgHasAttribute( data, iSegNameVector[ segment++ ].c_str() ))
    return false;

  anSS.str( std::string() );
  anSS << Interface::BMF::segToUnsignedM( data );

  attrMap[ NRS::Literals::Attribute::ERRID() ] = anSS.str();

  if (!msgHasAttribute( data, iSegNameVector[ segment++ ].c_str() ))
    return false;

  attrMap[ Literals::Attribute::ERRSTRING() ] =
    Interface::BMF::segToStringM( data );

  if (!theEID.hasExternalInterface( port ))
    _ABORT_( "Port not found\n\t" );

  theEID.getExternalInterface( port )->sendMessage( theTarget,
						    getType().c_str(),
						    NRSAttrMap, attrMap,
						    intelligent );
  
  return true;
}

void 
NRS::Message::ErrorManager::sendMessage( Base::Target &theTarget,
					 unsigned_t priority,
					 unsigned_t errID,
					 const char* errorMessage )
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
      std::stringstream anSS;
      anSS << priority;
      attrMap[ NRS::Literals::Attribute::PRIORITY() ] = anSS.str();

      anSS.str( std::string() );
      anSS << errID;
      attrMap[ NRS::Literals::Attribute::ERRID() ] = anSS.str();
      attrMap[ Literals::Attribute::ERRSTRING() ] = errorMessage;

      if (!isBroadcast)
	eI->sendMessage( theTarget, iMessage.c_str(), NRSAttrMap, attrMap,
			 intelligent );
    }

  if (isBroadcast || !isPMLNotBMF)
    {
      while (!Interface::BMF::segFromUnsignedM( data, priority, size ))
	Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
      
      while (!Interface::BMF::segFromUnsignedM( data, errID, size ))
	Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
      
      while (!Interface::BMF::segFromStringM( data, errorMessage, size ))
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
