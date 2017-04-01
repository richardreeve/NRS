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

#include <iostream>
#include <string>
#include <limits>
#include "Executive.hh"
#include "VariableManager.hh"
#include "UnsignedManager.hh"
#include "RouteManager.hh"
#include "Exception.hh"
#include "AttributeLiterals.hh"
#include "Interface.hh"
#include "Target.hh"
#include "Executive.hh"

#pragma implementation
#include "QueryManager.hh"


NRS::Message::QueryManager::QueryManager( std::string aMessage,
					  std::string displayName, 
					  int numSeg ) : 
  Base::VariableManager( aMessage, displayName, numSeg, "", false )
{
  Base::Executive::getExecutive().addStartupVariable( getType(), getType() );
}

void NRS::Message::QueryManager::setSegments()
{

  Base::VariableManager::setSegments();

  iSegTypeVector[ 0 ] = Type::RouteManager::getName();
  iSegNameVector[ 0 ] = Literals::Attribute::RETURNROUTE();
  iSegDisplayNameVector[ 0 ] = "return route";

  iSegTypeVector[ 1 ] = Type::UnsignedManager::getName();
  iSegNameVector[ 1 ] = Literals::Attribute::RETURNTOVNID();
  iSegDisplayNameVector[ 1 ] = "return VNID";

  iSegTypeVector[ 2 ] = Type::UnsignedManager::getName();
  iSegNameVector[ 2 ] = Literals::Attribute::MSGID();
  iSegDisplayNameVector[ 2 ] = "Message ID";

  for ( int i = 0; i < iNumSeg; i++ )
    iSegNRSANamespaceVector[i] = false;
}

bool 
NRS::Message::QueryManager::extractReturnTarget( std::map< std::string,
						 std::string > &NRSAttrMap,
						 std::map< std::string,
						 std::string > &attrMap,
						 bool intelligent,
						 Base::Target &returnTarget )
{
  if ( intelligent )
    {
      if ( NRSAttrMap.find( Literals::XML::IRETURNROUTE() ) != 
	   NRSAttrMap.end() )
	{
	  Base::Target 
	    theTarget( true, 
		       NRSAttrMap[ Literals::XML::IRETURNROUTE() ].c_str() );
	  returnTarget.takeRoutes( theTarget );
	}
      if ( NRSAttrMap.find( Literals::XML::ISOURCECID() ) != 
	   NRSAttrMap.end() )
	{
	  Base::Target 
	    theTarget( NRSAttrMap[ Literals::XML::ISOURCECID() ].c_str(),
		       true, false );
	  returnTarget.takeCID( theTarget, true );
	}
    }

  if (!returnTarget.isValid())
    {
      if (!msgHasAttribute( attrMap, Literals::Attribute::RETURNROUTE() ))
	return false;
      
      Base::Target 
	theTarget( true, 
		   attrMap[ Literals::Attribute::RETURNROUTE() ].c_str() );
      returnTarget.takeRoutes( theTarget );
    }


  if (!msgHasAttribute( attrMap, Literals::Attribute::RETURNTOVNID() ))
    return false;

  returnTarget.
    setTargetVNID( atol( attrMap[ Literals::Attribute::
				     RETURNTOVNID() ].c_str() ) );
		    
  
  // Don't remove intelligent fields as they may need to continue on...
  //  NRSAttrMap.erase( Literals::XML::IRETURNROUTE() );
  //  NRSAttrMap.erase( Literals::XML::ISOURCECID() );
  // remove extracted attributes from attrMap
  attrMap.erase( Literals::Attribute::RETURNROUTE() );
  attrMap.erase( Literals::Attribute::RETURNTOVNID() );
 
  return true;
}

bool 
NRS::Message::QueryManager::extractMsgID( std::map< std::string,
					  std::string > &attrMap,
					  unsigned_t& msgID )
{
  if (!msgHasAttribute( attrMap, Literals::Attribute::MSGID() ))
    return false;

  msgID = atol( attrMap[ Literals::Attribute::MSGID() ].c_str() );
  
  // remove attributes from attrMap
  attrMap.erase( Literals::Attribute::MSGID() );
  
  return true;
}

bool 
NRS::Message::QueryManager::extractReturnTarget( char *&data,
						 bool intelligent,
						 const char* intMap[],
						 Base::Target &returnTarget )
{
  if (intelligent)
    {
      if (intMap[ (int) Interface::BMF::ReturnRoute ])
	{ // has a return route
	  Base::Target aTarget( false,
				intMap[ (int) Interface::BMF::ReturnRoute ] );
	  returnTarget.takeRoutes( aTarget );
	}
      else if (intMap[ (int) Interface::BMF::SourceCID ])
	{ // has a source CID
	  Base::Target aTarget( intMap[ (int) Interface::BMF::SourceCID ],
				false, false );
	  returnTarget.takeCID( aTarget );
	}
    }

  if (!msgHasAttribute( data, Literals::Attribute::RETURNROUTE() ))
    return false;

  if (!returnTarget.isValid())
    {
      Base::Target aTarget( false, data );
      returnTarget.takeRoutes( aTarget );
    }

  Interface::BMF::segSkipM( data );

  if (!msgHasAttribute( data, Literals::Attribute::RETURNTOVNID() ))
    return false;

  returnTarget.setTargetVNID( Interface::BMF::segToUnsignedM( data ) );
  
  return true;
}

bool 
NRS::Message::QueryManager::extractMsgID( char *&data,
					  unsigned_t &msgID )
{
  if (!msgHasAttribute( data, Literals::Attribute::MSGID() ))
    return false;

  msgID = Interface::BMF::segToUnsignedM( data );

  return true;
}

void
NRS::Message::QueryManager::insertReturnTarget( char *&msg, size_t &alloc,
						char *&data, size_t &size,
						bool &intelligent,
						char* intMap[],
						Base::Target &returnTarget )
{
  Base::Executive &theE = 
    Base::Executive::getExecutive();
  if ( returnTarget.isBroadcast() )
    {
      if (intMap[ (int) Interface::BMF::SourceCID ])
	free( intMap[ (int) Interface::BMF::SourceCID ] );

      intMap[ (int) Interface::BMF::SourceCID ] = 
	(char*) malloc( returnTarget.getCIDLength() + 1 );
      Interface::BMF::segCopyF( intMap[ (int) Interface::BMF::SourceCID ],
				returnTarget.getCID( false ),
				returnTarget.getCIDLength() );
      intelligent = true;
    }

  // If we don't have a valid return route, then put in an intelligent one
  if (!returnTarget.isValid())
    {
      if ( ( !intMap[ (int) Interface::BMF::SourceCID ] ) &&
	   theE.hasCID() )
	{
	  // use CID for source CID
	  size_t size = strlen( theE.getCID() ) + 2;
	  char *ptr = intMap[ (int) Interface::BMF::SourceCID ] = 
	    (char*) malloc( ++size );
	  Interface::BMF::segFromStringM( ptr, theE.getCID(), size );
	  intelligent = true;
	}

      if ( !intMap[ (int) Interface::BMF::ReturnRoute ] )
	{
	  // create empty string for return route
	  size_t size = Interface::BMF::EmptySegmentSize;
	  char *ptr = intMap[ (int) Interface::BMF::ReturnRoute ] =
	    (char*) malloc( size );
	  Interface::BMF::segFromEmptyM( ptr, size );
	  intelligent = true;
	}
    }

  size_t len = returnTarget.getFullLength( false );
  if ( size <= len )
    Interface::BMF::segReallocM( msg, data, alloc, size, len + 1 );

  Interface::BMF::segCopyF( data, returnTarget.getFullRoute( false ), len );
  data += len;
  size -= len;


  len = returnTarget.getTargetVNIDLength( false );
  if ( size <= len )
    Interface::BMF::segReallocM( msg, data, alloc, size, len + 1 );
  Interface::BMF::segCopyF( data, returnTarget.getTargetVNIDArray( false ),
			    len );
  data += len;
  size -= len;

  if (!returnTarget.hasTargetVNID())
    {
      if (returnTarget.hasTargetVNName())
	{
	  _ABORT_( "can't handle VNName for return target\n\t" );
	}
      else
	{
	  _ABORT_( "No return target vnid for return target\n\t" );
	}
    }
}

void
NRS::Message::QueryManager::insertMsgID( char *&msg, size_t &alloc,
					 char *&data, size_t &size,
					 unsigned_t msgID )
{
  while (!Interface::BMF::segFromUnsignedM( data, msgID, size ))
    Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
}

void 
NRS::Message::QueryManager::insertReturnTarget( std::map< std::string,
						std::string > &NRSAttrMap,
						std::map< std::string,
						std::string > &attrMap,
						bool &intelligent,
						Base::Target &returnTarget )
{
  if ( returnTarget.isBroadcast() )
    {
      intelligent = true;
      NRSAttrMap[ Literals::XML::ISOURCECID() ] = 
	returnTarget.getCID( true );
    }

  attrMap[ Literals::Attribute::RETURNROUTE() ] =
    returnTarget.getFullRoute( true );
  
  // If we don't have a valid return route, then put in an intelligent one
  if (!returnTarget.isValid())
    {
      if (NRSAttrMap.find( Literals::XML::ISOURCECID() ) == 
	  NRSAttrMap.end())
	{
	  Base::Executive &theE = 
	    Base::Executive::getExecutive();
	  if ( theE.hasCID() )
	    NRSAttrMap[ Literals::XML::ISOURCECID() ] = theE.getCID();
	}
      if (NRSAttrMap.find( Literals::XML::IRETURNROUTE() ) == 
	  NRSAttrMap.end())
	{
	  NRSAttrMap[ Literals::XML::IRETURNROUTE() ] = "";
	  intelligent = true;
	}
    }

  if (returnTarget.hasTargetVNID())
    {
      attrMap[ Literals::Attribute::RETURNTOVNID() ] =
	returnTarget.getTargetVNIDArray( true );
    }
  else
    {
      _ABORT_( "No return target vnid for return target\n\t" );
    }
}

void
NRS::Message::QueryManager::insertMsgID( std::map< std::string,
					 std::string > &attrMap,
					 unsigned_t msgID )
{
  std::stringstream anSS;

  anSS << msgID;

  attrMap[ Literals::Attribute::MSGID() ] = anSS.str();
}
