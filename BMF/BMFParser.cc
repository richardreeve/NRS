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

#include <cstring>
#include <iomanip>
#include <iostream>
#include <sstream>

#include "BMFParser.hh"
#include "BooleanSegment.hh"
#include "Executive.hh"
#include "ExternalInterface.hh"
#include "ExternalInterfaceDirector.hh"
#include "IntegerSegment.hh"
#include "IntelligentStore.hh"
#include "RouteSegment.hh"
#include "Segment.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "Variable.hh"
#include "VariableNodeDirector.hh"

const uint8_t NRS::Interface::BMFParser::True = 3;
const uint8_t NRS::Interface::BMFParser::False = 2;

NRS::Interface::BMFParser::BMFParser() :
  MessageParser(), iCurrentBuffer( NULL )
{
}

NRS::Interface::BMFParser::~BMFParser()
{
}

bool NRS::Interface::BMFParser::parseAgain()
{
  if (iCurrentBuffer == NULL)
    return false;
  
  return parseNew( iCurrentBuffer );
}


bool NRS::Interface::BMFParser::parseNew( uint8_t *aBuffer )
{
  Base::Executive &theE = Base::Executive::getExecutive();
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();
  Base::ExternalInterfaceDirector &theEID =
    Base::ExternalInterfaceDirector::getDirector();
  
  iCurrentBuffer = NULL;
  
  integer_t targetVNID;
  integer_t len;
  Type::IntegerSegment hopS;
  
  hopS.iValue = -1;  
  
  std::auto_ptr< Base::IntelligentStore >( intelligentPtr );
  
  std::string cid;
  
  bool intelligent = false;
  bool arrived = false;
  bool discard = false;
  bool broadcast = false;
  bool acknowledge = false;
  bool failedRoute = false;
  
#ifdef DEBUG
  std::stringstream anSS;
  for ( uint8_t *dptr = aBuffer; *dptr != 0; ++dptr )
    {
      anSS.width(2);
      anSS.fill('0');
      anSS << std::hex << (unsigned_t) dptr[0];
    }
  anSS << "00" << std::dec;
  _DEBUG_( "Received message: [" << anSS.str() << "] (from port "
	   << iPort << ")\n\t" );
#endif //def DEBUG
  
  // If no buffer, or message is empty, ignore it
  if ((!aBuffer) || (aBuffer[0] == 0))
    {
      return false;
    }
  
  // Okay, then is it intelligent?
  if (aBuffer[0] & 1<<7)
    { // It is, so create segments
      
      // First remove intelligent bit
      aBuffer[0] &= (uint8_t) 127;
      
      // Then set intelligent segment
      intelligentPtr.reset( new Base::IntelligentStore( Enum::BMF ) );
      intelligentPtr->setSegment( Enum::IsIntelligent,
				  &True, 1 );
      intelligent = true;
      
      // Then go through any remaining...
      _ASSERT_( (segLength( aBuffer ) == 1) &&
		(aBuffer[0] < NUM_INTELLIGENT_ATT),
		"Illegal intelligent message" );
      
      Enum::IntelligentType type = (Enum::IntelligentType) *(aBuffer++);
      while (type != Enum::EndIMessage)
	{
	  len = segLength( aBuffer );
	  intelligentPtr->setSegment( type, aBuffer, len );
	  aBuffer += len;
	  _ASSERT_( (segLength( aBuffer ) == 1) &&
		    (aBuffer[0] < NUM_INTELLIGENT_ATT),
		    "Illegal intelligent message" );
	  
	  type = (Enum::IntelligentType) *(aBuffer++);
	}
      
      // Check for illegal combinations
      _ASSERT_( (intelligentPtr->hasSegment( Enum::IsBroadcast ) &&
		 intelligentPtr->hasSegment( Enum::HopCount ) &&
		 intelligentPtr->hasSegment( Enum::TargetCID )) ||
		(!intelligentPtr->hasSegment( Enum::IsBroadcast ) &&
		 !intelligentPtr->hasSegment( Enum::TargetCID )),
		"Must be broadcast with CID or non-broadcast without" );
      
      _ASSERT_( (intelligentPtr->hasSegment( Enum::AckMsg ) &&
		 intelligentPtr->hasSegment( Enum::AckVNID ) &&
		 intelligentPtr->hasSegment( Enum::MsgID ) &&
		 (intelligentPtr->hasSegment( Enum::SourceCID ) ||
		  intelligentPtr->hasSegment( Enum::ReturnRoute ) ) ) ||
		(!intelligentPtr->hasSegment( Enum::AckMsg ) &&
		 !intelligentPtr->hasSegment( Enum::AckVNID )),
		"Must be asking for an acknowledge with data to return" );
      
      _ASSERT_( (intelligentPtr->hasSegment( Enum::FailedRouteMsg ) &&
		 intelligentPtr->hasSegment( Enum::FailedRouteVNID ) &&
		 intelligentPtr->hasSegment( Enum::MsgID ) &&
		 (intelligentPtr->hasSegment( Enum::SourceCID ) ||
		  intelligentPtr->hasSegment( Enum::ReturnRoute ) ) ) ||
		(!intelligentPtr->hasSegment( Enum::FailedRouteMsg ) &&
		 !intelligentPtr->hasSegment( Enum::FailedRouteVNID )),
		"Must be asking for failed route info with data to return" );
      
      _ASSERT_( !intelligentPtr->hasSegment( Enum::DummyIntSeg ) &&
		!intelligentPtr->hasSegment( Enum::EndIMessage ),
		"Illegal message" );
      
      // If there's a hop count, decrement it
      if (intelligentPtr->hasSegment( Enum::HopCount ))
	{
	  hopS.useData( intelligentPtr->getMessageStore(),
			(uint8_t) Enum::HopCount );
	  hopS.iValue--;
	  hopS.createSegment( intelligentPtr->getMessageStore(),
			      (uint8_t) Enum::HopCount );
	}
      
      // If there's a return route extend it
      if (intelligentPtr->hasSegment( Enum::ReturnRoute ))
	{
	  Type::RouteSegment rs;
	  rs.useData( intelligentPtr->getMessageStore(),
		      (uint8_t) Enum::ReturnRoute );
	  routePushPortFront( rs );
	  rs.createSegment( intelligentPtr->getMessageStore(),
			    (uint8_t) Enum::ReturnRoute );
	}
      
      // If there's a failed route instruction, note it
      if (intelligentPtr->hasSegment( Enum::FailedRouteMsg ) &&
	  intelligentPtr->hasSegment( Enum::FailedRouteVNID ) &&
	  intelligentPtr->hasSegment( Enum::MsgID ))
	{
	  failedRoute = true;
	}
      
      // If there's a acknowledge instruction, note it
      if (intelligentPtr->hasSegment( Enum::AckMsg ) &&
	  intelligentPtr->hasSegment( Enum::AckVNID ) &&
	  intelligentPtr->hasSegment( Enum::MsgID ))
	{
	  acknowledge = true;
	}
    }
  
  Type::RouteSegment routeS;
  Type::IntegerSegment vnidS;
  // main message. First segment is Route
  routeS.useRawData( aBuffer, Enum::BMF );
  segSkip( aBuffer );
  // Next is vnid
  vnidS.useRawData( aBuffer, Enum::BMF );
  segSkip( aBuffer );
  targetVNID = vnidS.iValue;
  
  _ASSERT_( ((targetVNID > 0) &&
	     ((!intelligent) || 
	      !intelligentPtr->hasSegment( Enum::TargetVNName ))) ||
	    ((targetVNID == 0) && intelligent &&
	     intelligentPtr->hasSegment( Enum::TargetVNName )),
	    "TargetVNName with vnid or neither" );
  
  if (routeS.iValue == getEmptyRoute())
    { // empty route, only chance that it may have arrived
      Type::BooleanSegment bs;
      Type::StringSegment ss;
      
      // Could it be a broadcast
      if (intelligent &&
	  intelligentPtr->hasSegment( Enum::IsBroadcast ) &&
	  intelligentPtr->hasSegment( Enum::HopCount ) &&
	  intelligentPtr->hasSegment( Enum::TargetCID ) )
	{
	  bs.useData( intelligentPtr->getMessageStore(),
		      (uint8_t) Enum::IsBroadcast );
	  
	  // Is it actually a broadcast?
	  if (bs.iValue)
	    {
	      ss.useData( intelligentPtr->getMessageStore(),
			  (uint8_t) Enum::TargetCID );
	      
	      // So has it arrived (or is it really a broadcast)?
	      cid = theE.getCID();
	      arrived = (ss.iValue == cid);
	      
	      if (!arrived)
		{
		  // Really a broadcast, have we run out of hops
		  if (hopS.iValue >= 0)
		    {
		      broadcast = true;
		      // so get correct broadcast address
		      cid = ss.iValue;
		    }
		  else
		    {
		      broadcast = false;
		      discard = true;
		    }
		}
	    }
	  else
	    {
	      arrived = true;
	      broadcast = false;
	    }
	}
      else
	{
	  arrived = true;
	  broadcast = false;
	}
      
      // We can only have arrived if we get here (route must be empty)
      if (arrived)
	{
	  if (intelligent && (targetVNID == 0) &&
	      intelligentPtr->hasSegment( Enum::TargetVNName ) )
	    {
	      ss.useData( intelligentPtr->getMessageStore(),
			  (uint8_t) Enum::TargetVNName );
	      if (!theVND.hasVariable( ss.iValue ))
		{
		  _WARN_( "Illegal variable name: " << ss.iValue << "\n\t" );
		  discard = true;
		  arrived = false;
		}
	      else
		{
		  targetVNID = theVND.getVariableVNID( ss.iValue );
		}
	    }
	  
	  while (!discard)
	    {
	      // Does the variable exist?
	      if (!theVND.hasVariable( targetVNID ))
		{
		  _ERROR_( "Illegal vnid in BMF message" );
		  discard = true;
		  arrived = false;
		}
	      else
		{
		  Base::MessageStore theMS( theVND.
					    getVariableManager( targetVNID ).
					    getNumSegments(), Enum::BMF );
		  
		  for ( unsigned_t segNum = 0;
			segNum < theMS.getNumSegments(); ++segNum )
		    {
		      if (aBuffer[0] != 0)
			{
			  len = segLength( aBuffer );
			  theMS.setSegment( segNum, aBuffer, len );
			  aBuffer += len;
			}
		      else
			{
			  _ERROR_( "Illegal BMF message - too short!" );
			  discard = true;
			  arrived = false;
			}
		    }
		  
		  if (!discard)
		    {
		      theVND.getVariable( targetVNID )->getInputLink().
			receiveMessage( theMS, intelligentPtr.get() );
		      
		      if (acknowledge)
			{
			  _TODO_( "acknowledge not implemented" );
			}

		      // is there another message there (in burst mode)?
		      if (aBuffer[0] != 0)
			{
			  vnidS.useRawData( aBuffer, Enum::BMF );
			  segSkip( aBuffer );
			  targetVNID = vnidS.iValue;
			}
		      else
			discard = true;
		    }
		}
	    }
	}
      else
	{
	  if (discard)
	    {
	      _TODO_( "failed route not implemented" );
	    }

	  if (broadcast)
	    {
	      // Create dummy target (we don't know the type, route is empty)
	      Base::Target theTarget( cid, routeS.iValue,
				      targetVNID, NULL );
	      uint8_t *ptr = aBuffer;
	      unsigned_t count = 0;
	      while (*ptr != 0)
		{
		  ++count;
		  segSkip( ptr );
		}


	      // Store message
	      Base::MessageStore theMessageStore( count, Enum::BMF );
	      for ( unsigned_t segNum = 0; segNum < count; ++segNum )
		{
		  theMessageStore.setSegment( segNum, aBuffer, 
					      segLength( aBuffer ) );
		  segSkip( aBuffer );
		}

	      // Send to everyone
	      for ( unsigned_t port = 0; port < theEID.getMaxPort(); ++port )
		{
		  // Except self
		  if ( (port != iPort) && theEID.hasExternalInterface( port ) )
		    {
		      Base::ExternalInterface *anEI =
			theEID.getExternalInterface( port );
		      
		      // and logs
		      if (!anEI->isLoggingPort())
			anEI->sendMessage( theTarget,
					   theMessageStore,
					   intelligentPtr.get() );
		    }
		}
	    }
	}
    }
  else
    {  // If not empty route, then not arrived, not broadcast

      // Create dummy target (we don't know the type)
      Base::Target theTarget( cid, routeS.iValue, targetVNID, NULL );
      uint8_t *ptr = aBuffer;
      unsigned_t count = 0;
      while (*ptr != 0)
	{
	  ++count;
	  segSkip( ptr );
	}
      // Store message
      Base::MessageStore theMessageStore( count, Enum::BMF );
      for ( unsigned_t segNum = 0; segNum < count; ++segNum )
	{
	  theMessageStore.setSegment( segNum, aBuffer, 
				      segLength( aBuffer ) );
	  segSkip( aBuffer );
	}
      
      // Send to port
      theEID.getExternalInterface( theTarget.getPort() )->
	sendMessage( theTarget, theMessageStore, intelligentPtr.get() );
    }
  return true;
}

void NRS::Interface::BMFParser::
createOutput( data_t &data,
	      Base::Target &aTarget, 
	      const Base::MessageStore &aMessageStore,
	      const Base::IntelligentStore *intelligentPtr )
{
  std::auto_ptr< Base::IntelligentStore > newISPtr( NULL );
  const uint8_t *ptr;

  data.clear();

  // any intelligent attributes?
  if (intelligentPtr != NULL)
    { // Yes, so is there a forward route?
      if (intelligentPtr->hasSegment( Enum::ForwardRoute ))
	{ // Yes, so create a new IntelligentStore with modified forward route
	  newISPtr.reset( new Base::IntelligentStore( *intelligentPtr ) );
	  intelligentPtr = newISPtr.get();
	  Type::RouteSegment rs;
	  rs.useData( newISPtr->getMessageStore(),
		      (uint8_t) Enum::ForwardRoute );
	  routePushPortEnd( rs );
	  rs.createSegment( newISPtr->getMessageStore(),
			    (uint8_t) Enum::ForwardRoute );
	}
      // Now go through all intelligent segments and print them out
      uint8_t intCh = 1<<7;
      for ( uint8_t intSeg = (unsigned_t) Enum::ForwardRoute;
	    intSeg < NUM_INTELLIGENT_ATT; ++intSeg )
	{
	  ptr = intelligentPtr->getSegment( (Enum::IntelligentType) intSeg );
	  if (ptr != NULL)
	    {
	      intCh |= intSeg;
	      data.push_back( intCh );
	      intCh = 0;
	      data.append( ptr, segLength( ptr ) );
	    }
	}
      if (!data.empty())
	data.push_back( (uint8_t) Enum::EndIMessage );
    }

  const Base::MessageStore &anMS = aTarget.getMessageStore( Enum::BMF );

  ptr = anMS.getSegment( Const::RoutePos );
  data.append( ptr, segLength( ptr ) );

  ptr = anMS.getSegment( Const::VNIDPos );
  data.append( ptr, segLength( ptr ) );

  for ( uint8_t segNum = 0;
	segNum < aMessageStore.getNumSegments(); ++segNum )
    {
      ptr = aMessageStore.getSegment( segNum );
      data.append( ptr, segLength( ptr ) );
    }  
  
#ifdef DEBUG
  std::stringstream anSS;
  for ( const uint8_t *dptr = data.c_str(); *dptr != 0; ++dptr )
    {
      anSS.width(2);
      anSS.fill('0');
      anSS << std::hex << (unsigned_t) dptr[0];
    }
  anSS << "00" << std::dec;
  _DEBUG_( "Sending message: [" << anSS.str() << "] (in port "
	   << iPort << ")\n\t" );
#endif //def DEBUG
}
