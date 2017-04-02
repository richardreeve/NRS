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

#include "Target.hh"
#include "DeleteLink.hh"
#include "Executive.hh"
#include "ExternalInterface.hh"
#include "ExternalInterfaceDirector.hh"
#include "IntegerSegment.hh"
#include "MessageStore.hh"
#include "RouteSegment.hh"
#include "StringLiterals.hh"
#include "VariableNodeDirector.hh"

NRS::Base::Target::
Target( const std::string &theCID, const route_t &theRoute, integer_t theVNID,
	const MessageDescription *aMDPtr ) :
  Location( theCID, theRoute, theVNID, false ), iMDPtr( aMDPtr ), iPort( 0 ),
  iMessageStore( Const::SegNum, Enum::BMF )
{
  Type::RouteSegment routeSeg;
  Type::IntegerSegment vnidSeg;
  Enum::EncodingType encType = Enum::BMF;

  vnidSeg.iValue = iVNID;
  routeSeg.iValue = iRoute;

  _ASSERT_( !iRoute.empty(), "Cannot create blank route, must be valid" );

  if (iRoute != getEmptyRoute())
    { // specific route, so extract port, and encode abbreviated route
      uint8_t ch = iRoute[0];
      bool ongoing = true;
      bool lastByte = false;
      int bitInNibble = 0;
      int bitsInChar;
  
      // First byte of unknown length
      bitsInChar = 7;
      while (!(ch & 1<<7))
	{
	  ch <<= 1;
	  bitsInChar--;
	}
      
      do
	{
	  // new char, check whether it's the end
	  lastByte = ((routeSeg.iValue[1] & 1<<7) == 0);
	  
	  // fill up the last nibble
	  while ((bitsInChar > 0) && (bitInNibble != 0))
	    {
	      bitInNibble--;
	      iPort <<= 1;
	      if (ch & 1<<6)
		iPort++;
	      bitsInChar--;
	      ch <<= 1;
	    }
	  
	  // okay, now we've got rid of the last nibble, now for
	  // the next 1 or 2 potentially
	  while ((ongoing) && (bitsInChar > 0))
	    {
	      if (!(ch & 1<<6))
		ongoing = false;
	      
	      // move into new nibble
	      bitInNibble = 3;
	      bitsInChar--;
	      ch <<= 1;
	      
	      // fill up the last nibble
	      while ((bitsInChar > 0) && (bitInNibble != 0))
		{
		  bitInNibble--;
		  iPort <<= 1;
		  if (ch & 1<<6)
		    iPort++;
		  bitsInChar--;
		  ch <<= 1;
		}
	    }
	  if (ongoing || (bitInNibble != 0))
	    {
	      routeSeg.iValue.erase( routeSeg.iValue.begin() );
	      ch = routeSeg.iValue[0];
	      bitsInChar = 7;
	    }
	} while (ongoing || (bitInNibble != 0));
      
      routeSeg.iValue[0] = (ch | (1<<7)) >> (7 - bitsInChar);

      encType = ExternalInterfaceDirector::getDirector().
	getExternalInterface( iPort )->getEncoding();
    }
  if (encType != iMessageStore.getEncoding())
    iMessageStore = MessageStore( 2, encType );
  
  routeSeg.createSegment( iMessageStore, Const::RoutePos );
  vnidSeg.createSegment( iMessageStore, Const::VNIDPos );
}

NRS::Base::Target::Target( const Location &theLocation,
			   const MessageDescription *theMDPtr ) :
  Location( theLocation.getCID(), theLocation.getRoute(),
	    theLocation.getVNID(), false ), iMDPtr( theMDPtr ), iPort( 0 ),
  iMessageStore( Const::SegNum, Enum::BMF )
{
  Type::RouteSegment routeSeg;
  Type::IntegerSegment vnidSeg;
  Enum::EncodingType encType = Enum::BMF;

  vnidSeg.iValue = iVNID;
  routeSeg.iValue = iRoute;

  _ASSERT_( !iRoute.empty(), "Cannot create blank route, must be valid" );

  if (iRoute != getEmptyRoute())
    { // specific route, so extract port, and encode abbreviated route
      uint8_t ch = iRoute[0];
      bool ongoing = true;
      bool lastByte = false;
      int bitInNibble = 0;
      int bitsInChar;
  
      // First byte of unknown length
      bitsInChar = 7;
      while (!(ch & 1<<7))
	{
	  ch <<= 1;
	  bitsInChar--;
	}
      
      do
	{
	  // new char, check whether it's the end
	  lastByte = ((routeSeg.iValue[1] & 1<<7) == 0);
	  
	  // fill up the last nibble
	  while ((bitsInChar > 0) && (bitInNibble != 0))
	    {
	      bitInNibble--;
	      iPort <<= 1;
	      if (ch & 1<<6)
		iPort++;
	      bitsInChar--;
	      ch <<= 1;
	    }
	  
	  // okay, now we've got rid of the last nibble, now for
	  // the next 1 or 2 potentially
	  while ((ongoing) && (bitsInChar > 0))
	    {
	      if (!(ch & 1<<6))
		ongoing = false;
	      
	      // move into new nibble
	      bitInNibble = 3;
	      bitsInChar--;
	      ch <<= 1;
	      
	      // fill up the last nibble
	      while ((bitsInChar > 0) && (bitInNibble != 0))
		{
		  bitInNibble--;
		  iPort <<= 1;
		  if (ch & 1<<6)
		    iPort++;
		  bitsInChar--;
		  ch <<= 1;
		}
	    }
	  if (ongoing || (bitInNibble != 0))
	    {
	      routeSeg.iValue.erase( routeSeg.iValue.begin() );
	      ch = routeSeg.iValue[0];
	      bitsInChar = 7;
	    }
	} while (ongoing || (bitInNibble != 0));
      
      routeSeg.iValue[0] = (ch | (1<<7)) >> (7 - bitsInChar);

      encType = ExternalInterfaceDirector::getDirector().
	getExternalInterface( iPort )->getEncoding();
    }
  if (encType != iMessageStore.getEncoding())
    iMessageStore = MessageStore( 2, encType );
  
  routeSeg.createSegment( iMessageStore, Const::RoutePos );
  vnidSeg.createSegment( iMessageStore, Const::VNIDPos );
}

void NRS::Base::Target::deleteLink( integer_t aVNID ) const
{
  NRS::Message::DeleteLink *dl =
    dynamic_cast< NRS::Message::
    DeleteLink* >( VariableNodeDirector::getDirector().
		   getVariable( Literals::Object::DELETELINK_VARIABLE() ) );

  _ASSERT_( dl != NULL, "DeleteLink variable not DeleteLink!" );

  Target dlTarget( iCID, iRoute, 0,
		   &dl->getVariableManager().getMessageDescription() );

  if (iSourceNotTarget)
    dl->deleteLink( dlTarget, true, iCID, iVNID, 
		    std::string( Executive::getExecutive().getCID() ), aVNID );
  else
    dl->deleteLink( dlTarget, false,
		    std::string( Executive::getExecutive().getCID() ), aVNID,
		    iCID, iVNID );
    
}

const NRS::Base::MessageStore &
NRS::Base::Target::getMessageStore( Enum::EncodingType anEncoding )
{
  if (anEncoding != iMessageStore.getEncoding())
    {
      Type::RouteSegment routeSeg;
      Type::IntegerSegment vnidSeg;
      
      routeSeg.useData( iMessageStore, Const::RoutePos );
      vnidSeg.useData( iMessageStore, Const::VNIDPos );
      
      iMessageStore = MessageStore( Const::SegNum, anEncoding );
      
      routeSeg.createSegment( iMessageStore, Const::RoutePos );
      vnidSeg.createSegment( iMessageStore, Const::VNIDPos );
    }
  return iMessageStore;
}
