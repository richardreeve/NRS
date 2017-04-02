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

#include "ReplyManager.hh"
#include "VariableManager.hh"
#include "UnsignedManager.hh"
#include "Exception.hh"
#include "AttributeLiterals.hh"
#include "Target.hh"
#include "Executive.hh"


NRS::Message::ReplyManager::ReplyManager( std::string aMessage,
					  std::string displayName, 
					  int numSeg ) : 
  Base::VariableManager( aMessage, displayName, numSeg, "", false )
{
  Base::Executive::getExecutive().addStartupVariable( getType(), getType() );
}

void NRS::Message::ReplyManager::setSegments()
{

  Base::VariableManager::setSegments();

  iSegTypeVector[ 0 ] = Type::UnsignedManager::getName();
  iSegNameVector[ 0 ] = Literals::Attribute::MSGID();
  iSegDisplayNameVector[ 0 ] = "Message ID";

  for ( int i = 0; i < iNumSeg; i++ )
    iSegNRSANamespaceVector[i] = false;
}

bool 
NRS::Message::ReplyManager::extractMsgID( std::map< std::string,
					  std::string > &attrMap,
					  unsigned_t& msgID )
{
  if (!msgHasAttribute( attrMap, Literals::Attribute::REPLYMSGID() ))
    return false;

  msgID = atol( attrMap[ Literals::Attribute::REPLYMSGID() ].c_str() );
  
  // remove attributes from attrMap
  attrMap.erase( Literals::Attribute::REPLYMSGID() );
  
  return true;
}

bool 
NRS::Message::ReplyManager::extractMsgID( char *&data,
					  unsigned_t &msgID )
{
  if (!msgHasAttribute( data, Literals::Attribute::REPLYMSGID() ))
    return false;

  msgID = Interface::BMF::segToUnsignedM( data );

  return true;
}

void
NRS::Message::ReplyManager::insertMsgID( char *&msg, size_t &alloc,
					 char *&data, size_t &size,
					 unsigned_t msgID )
{
  while (!Interface::BMF::segFromUnsignedM( data, msgID, size ))
    Interface::BMF::segReallocM( msg, data, alloc, size, alloc );
}

void
NRS::Message::ReplyManager::insertMsgID( std::map< std::string,
					 std::string > &attrMap,
					 unsigned_t msgID )
{
  std::stringstream anSS;

  anSS << msgID;

  attrMap[ Literals::Attribute::REPLYMSGID() ] = anSS.str();
}
