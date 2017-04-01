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
#include "SetErrorRouteManager.hh"

#include "SetErrorRoute.hh"
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
  NRS::Message::SetErrorRouteManager sSERM;
}

NRS::Message::SetErrorRouteManager::SetErrorRouteManager() : 
  NRS::Base::VariableManager( Literals::Object::SET_ERROR_ROUTE_VARIABLE(),
			      "", 3, "", false )
{
  Base::Executive::getExecutive().addStartupVariable( getType(), getType() );
}

void NRS::Message::SetErrorRouteManager::setSegments()
{
  NRS::Base::VariableManager::setSegments();

  iSegTypeVector[ 0 ] = NRS::Type::UnsignedManager::getName();
  iSegNameVector[ 0 ] = NRS::Literals::Attribute::PRIORITY();
  iSegDisplayNameVector[ 0 ] = "Min priority of error to transmit";

  iSegTypeVector[ 1 ] = NRS::Type::StringManager::getName();
  iSegIsUnitVector[ 1 ] = true;
  iSegUnitVector[ 1 ] = NRS::Unit::TokenManager::getName();
  iSegIsStringVector[ 1 ] = true;
  iSegStringHasRestrictionVector[ 1 ] = true;
  iSegStringRestrictionVector[ 1 ] = 
    NRS::Literals::StringRestriction::TOKEN();
  iSegNameVector[ 1 ] = NRS::Literals::Attribute::CID();
  iSegDisplayNameVector[ 1 ] = "CID of target error component";

  iSegTypeVector[ 2 ] = NRS::Type::UnsignedManager::getName();
  iSegNameVector[ 2 ] = NRS::Literals::Attribute::VNID();
  iSegDisplayNameVector[ 2 ] = "VNID of target error variable";

  for ( int i = 0; i < iNumSeg; i++ )
    iSegNRSANamespaceVector[i] = false;
}

void NRS::Message::SetErrorRouteManager::setDescription()
{
  iDescription = "The built-in type for setting error routes.";
}

void NRS::Message::SetErrorRouteManager::createVariable( std::string aName )
{
  new SetErrorRoute( aName );
}

bool 
NRS::Message::SetErrorRouteManager::deliverMessage( unsigned_t vnid,
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

  // determine if the message has the necessary CID
  if ( !msgHasAttribute( attrMap, Literals::Attribute::CID() ) )
    return false;

  Base::Target theTarget( attrMap[ Literals::Attribute::CID() ].c_str(),
			  true, true );

  // determine if the message has the necessary vnid
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::VNID() ) )
    return false;

  unsigned_t aVNID = atol( attrMap[ Literals::Attribute::VNID() ].c_str() );
  if (aVNID != 0)
    theTarget.setTargetVNID( aVNID );

  dynamic_cast< SetErrorRoute* >( theVND.getVariable( vnid ))->
    receiveMessage( aPriority, theTarget );
  
  // remove attributes from attrMap
  attrMap.erase( NRS::Literals::Attribute::PRIORITY() );
  attrMap.erase( NRS::Literals::Attribute::CID() );
  attrMap.erase( NRS::Literals::Attribute::VNID() );
  
  return true;
}

bool 
NRS::Message::SetErrorRouteManager::
translateMessage( unsigned_t port,
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

  // determine if the message has the necessary CID
  if ( !msgHasAttribute( attrMap, Literals::Attribute::CID() ) )
    return false;

  while (!Interface::BMF::segFromStringM( data,
					  attrMap[ Literals::Attribute::
						      CID() ].c_str(), size ))
    Interface::BMF::segReallocM( msg, data, alloc, size, alloc );

  // determine if the message has the necessary vnid
  if ( !msgHasAttribute( attrMap, NRS::Literals::Attribute::VNID() ) )
    return false;

  while ( !Interface::BMF::
	  segFromUnsignedM( data, 
			    atol( attrMap[ Literals::Attribute::
					      VNID() ].c_str() ),
			    size ) )
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
  attrMap.erase( Literals::Attribute::CID() );
  attrMap.erase( Literals::Attribute::VNID() );
  
  for ( int i = 0; i < Interface::BMF::IMSize; i++ )
    free( intMap[ i ] );
  return true;
}

bool NRS::Message::SetErrorRouteManager::deliverMessage( unsigned_t vnid,
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

  if (!msgHasAttribute( data, iSegNameVector[ segment++ ].c_str() ))
    return false;
  Base::Target theTarget( data, false, false );
  Interface::BMF::segSkipM( data );

  if (!msgHasAttribute( data, iSegNameVector[ segment++ ].c_str() ))
    return false;
 unsigned_t aVNID = Interface::BMF::segToUnsignedM( data );
 if (aVNID != 0)
   theTarget.setTargetVNID( aVNID );

  dynamic_cast< SetErrorRoute* >( theVND.getVariable( vnid ) )->
    receiveMessage( aPriority, theTarget );
  
  return true;
}

bool 
NRS::Message::SetErrorRouteManager::translateMessage( unsigned_t port,
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

  attrMap[ Literals::Attribute::CID() ] = Interface::BMF::segToStringM( data );

  if (!msgHasAttribute( data, iSegNameVector[ segment++ ].c_str() ))
    return false;

  anSS.str( std::string() );
  anSS << Interface::BMF::segToUnsignedM( data );

  attrMap[ NRS::Literals::Attribute::VNID() ] = anSS.str();

  if (!theEID.hasExternalInterface( port ))
    _ABORT_( "Port not found\n\t" );

  theEID.getExternalInterface( port )->sendMessage( theTarget,
						    getType().c_str(),
						    NRSAttrMap, attrMap,
						    intelligent );
  
  return true;
}

void 
NRS::Message::SetErrorRouteManager::sendMessage( Base::Target &theTarget,
						 unsigned_t priority,
						 Base::Target &theRoute )
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

      attrMap[ Literals::Attribute::CID() ] = theRoute.getCID( true );

      attrMap[ NRS::Literals::Attribute::VNID() ] = 
	theRoute.getTargetVNIDArray( true );

      if (!isBroadcast)
	eI->sendMessage( theTarget, iMessage.c_str(), NRSAttrMap, attrMap,
			 intelligent );
    }

  if (isBroadcast || !isPMLNotBMF)
    {
      while (!Interface::BMF::segFromUnsignedM( data, priority, size ))
	Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
      
      size_t len;

      len = theRoute.getCIDLength();
      if (len >= size)
	{
	  Interface::BMF::segReallocM( msg, data, alloc, size, len + 1 );
	}
      Interface::BMF::segCopyF( data, theRoute.getCID( false ), len );
      size -= len;
      data += len;

      len = theRoute.getTargetVNIDLength( false );
      if (len >= size)
	{
	  Interface::BMF::segReallocM( msg, data, alloc, size, len + 1 );
	}
      Interface::BMF::segCopyF( data,
				theRoute.getTargetVNIDArray( false ), len );
      size -= len;
      data += len;
      
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
