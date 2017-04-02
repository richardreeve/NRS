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

#include "ExternalInterface.hh"
#include "ExternalInterfaceDirector.hh"
#include "RouteSegment.hh"

NRS::Base::ExternalInterface::
ExternalInterface( Enum::ConnectionType aConnectionType,
		   Enum::EncodingType anEncodingType,
		   Interface::MessageParser *aParserPtr,
		   bool doRead, bool doWrite,
		   bool instantTransmit,
		   bool receiveAll,
		   int maxReceive ) :
  iConnectionType( aConnectionType ), iEncodingType( anEncodingType ),
  iParserPtr( aParserPtr ), iRead( doRead ), iWrite( doWrite ),
  iInstantTransmit( instantTransmit ), iReceiveAll( receiveAll ),
  iMaxReceive( maxReceive ), iIsLogging( false )
{
  _ASSERT_( iParserPtr.get() != NULL, "No parser passed" );
  iPort = 
    ExternalInterfaceDirector::getDirector().addExternalInterface( this );
  iParserPtr->setPort( iPort );

  Type::RouteSegment rs;
  iParserPtr->routePushPortEnd( rs );
  iPortRoute = rs.iValue;
}

NRS::Base::ExternalInterface::~ExternalInterface()
{
  ExternalInterfaceDirector::getDirector().removeExternalInterface( this );
}

void NRS::Base::ExternalInterface::setLoggingPort()
{
  if (!iIsLogging)
    {
      iIsLogging = true;
      ExternalInterfaceDirector::getDirector().addLoggingPort( iPort );
    }
}
