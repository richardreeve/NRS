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

#include "ExternalInterfaceDirector.hh"
#include "ExternalInterface.hh"
#include "ExternalInterfaceHandler.hh"
#include "StringLiterals.hh"

namespace
{
  static NRS::Base::ExternalInterfaceDirector &sEID =
  NRS::Base::ExternalInterfaceDirector::getDirector();
}

NRS::Base::ExternalInterfaceDirector::ExternalInterfaceDirector() :
  iNextPort( 0 ), iMaxPort( 0 )
{
}

NRS::Base::ExternalInterfaceDirector::~ExternalInterfaceDirector()
{

  for ( typeof( iConnMsgEIHMap.begin() ) anIter = iConnMsgEIHMap.begin();
	anIter != iConnMsgEIHMap.end(); anIter++ )
    {
      for ( typeof( anIter->second.begin() ) anIter2 = anIter->second.begin();
	    anIter2 != anIter->second.end(); anIter2 = anIter->second.begin() )
	{
	  anIter2->second->unRegister();
	}
    }
}

bool NRS::Base::ExternalInterfaceDirector::mainLoop()
{
  bool reasonToLoop = false;
  bool running;
  
  for ( unsigned_t i = 0; i < getMaxPort(); ++i )
    {
      if ( hasExternalInterface( i ) )
	{
	  running = iPortEIVector[i]->update();
	  if (!running)
	    {
	      delete iPortEIVector[i];
	      iPortEIVector[i] = NULL;
	    }
	  reasonToLoop |= running;
	}
    }
  
  return reasonToLoop;
}

bool NRS::Base::ExternalInterfaceDirector::
hasExternalInterfaceHandler( Enum::ConnectionType aConnectionType,
			     Enum::EncodingType anEncodingType ) const
{
  bool present = false;
  typeof( iConnMsgEIHMap.end() ) anEIHIter =
    iConnMsgEIHMap.find( aConnectionType );
  if ( anEIHIter == iConnMsgEIHMap.end() )
    {
      return false;
    }

  if (anEncodingType == Enum::NoEncoding)
    {
      if (!anEIHIter->second.empty())
	{
	  present = true;
	}
    }
  else
    {
      if (anEIHIter->second.find( anEncodingType ) !=
	  anEIHIter->second.end())
	{
	  present = true;
	}
    }
  return present;
}

void NRS::Base::ExternalInterfaceDirector::
addExternalInterfaceHandler( NRS::Base::ExternalInterfaceHandler *anEIH )
{
  Enum::ConnectionType aCT = anEIH->getConnectionType();
  Enum::EncodingType anET = anEIH->getEncodingType();
  if ( hasExternalInterfaceHandler( aCT, anET ) )
    {
      _ABORT_( "Reinsertion attempt for ExternalInterfaceHandler '"
	       << Literals::Connection::getConnectionName( aCT )
	       << ":"
	       << Literals::Message::getEncodingName( anET )
	       << "'\n\t" );
    }
  iConnMsgEIHMap[ aCT ][ anET ] = anEIH;
}

NRS::Base::ExternalInterfaceHandler* NRS::Base::ExternalInterfaceDirector::
getExternalInterfaceHandler( Enum::ConnectionType aConnectionType,
			     Enum::EncodingType anEncodingType )
{
  if ( !hasExternalInterfaceHandler( aConnectionType, anEncodingType ) )
    {
      Exception e( _FL_ );
      e << "No ExternalInterfaceHandler for " 
	<< Literals::Connection::getConnectionName( aConnectionType )
	<< " connection";
      if (anEncodingType != Enum::NoEncoding)
	e << " and of encoding type "
	  << Literals::Message::getEncodingName( anEncodingType );
      throw e << "\n\t";
    }
  ExternalInterfaceHandler *anEIH;
  
  if (anEncodingType == Enum::NoEncoding)
    anEIH = iConnMsgEIHMap[ aConnectionType ].begin()->second;
  else
    anEIH = iConnMsgEIHMap[ aConnectionType ][ anEncodingType ];
  
  return anEIH;
}


void NRS::Base::ExternalInterfaceDirector::
removeExternalInterfaceHandler( ExternalInterfaceHandler *anEIH )
{
  Enum::ConnectionType aCT = anEIH->getConnectionType();
  Enum::EncodingType anET = anEIH->getEncodingType();

  if ( !hasExternalInterfaceHandler( aCT, anET ) )
    {
      _ABORT_( "ExternalInterfaceHandler "
	       << Literals::Connection::getConnectionName( aCT )
	       << ":"
	       <<  Literals::Message::getEncodingName( anET )
	       << " not registered" );
    }

  if ( getExternalInterfaceHandler( aCT, anET ) != anEIH )
    {
      _ABORT_( "Wrong ExternalInterfaceHandler registered" );
    }
  
  iConnMsgEIHMap[ aCT ].erase( iConnMsgEIHMap[ aCT ].find( anET ) );
}

NRS::unsigned_t NRS::Base::ExternalInterfaceDirector::
addExternalInterface( NRS::Base::ExternalInterface *anEI )
{
  unsigned_t port = getNextPort();

  iPortEIVector[ port ] = anEI;
  return port;
}

void
NRS::Base::ExternalInterfaceDirector::
removeExternalInterface( ExternalInterface *anEI )
{
  if ( !hasExternalInterface( anEI->getPort() ) )
  {
    _ABORT_( "ExternalInterface not registered" );
  }
  if ( getExternalInterface( anEI->getPort() ) != anEI )
    {
      _ABORT_( "Wrong ExternalInterface registered" );
    }
  removeExternalInterface( anEI->getPort() );
}

void 
NRS::Base::ExternalInterfaceDirector::
removeExternalInterface( const NRS::unsigned_t port )
{
  if ( !hasExternalInterface( port ) )
    {
      _ABORT_( "No such ExternalInterface as \"" << port << "\"\n\t" );
    }
  
  iPortEIVector[ port ] = NULL;

  if ( find( iLogPortNumList.begin(), iLogPortNumList.end(),
	     port ) != iLogPortNumList.end() )
    iLogPortNumList.erase( find( iLogPortNumList.begin(),
				 iLogPortNumList.end(), port ) );
}

NRS::unsigned_t NRS::Base::ExternalInterfaceDirector::getNextPort()
{
  if (iNextPort >= UNSIGNED_T_MAX)
    _ABORT_( "Port wraparound - cannot handle\n\t" );

  if (iNextPort >= iMaxPort)
    {
      iMaxPort = iNextPort + 1;
    }
  if ( iPortEIVector.capacity() <= iMaxPort )
    iPortEIVector.reserve( 2 * (iMaxPort + 1) );
  if ( iPortEIVector.size() <= iMaxPort )
    iPortEIVector.resize( iMaxPort + 1 );

  return iNextPort++;
}
