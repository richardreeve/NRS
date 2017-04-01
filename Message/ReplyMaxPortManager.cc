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
#include <string>

#include "AttributeLiterals.hh"
#include "Executive.hh"
#include "ReplyMaxPort.hh"
#include "ReplyMaxPortManager.hh"
#include "VariableNodeDirector.hh"

namespace
{
  NRS::Message::ReplyMaxPortManager sRMaxPortM;
}

NRS::Message::ReplyMaxPortManager::ReplyMaxPortManager() : 
  VariableManager( Literals::Object::REPLYMAXPORT_VARIABLE(), "", 2 )
{
  iDescription = "The built-in message type for replies to requests "
    "for number of ports.";
  Base::Executive::getExecutive().addStartupVariable( getMessageName(),
						      getMessageName() );
}

void NRS::Message::ReplyMaxPortManager::doConfigure()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  iSegmentDescription[0] =
    theVND.getVariableManager( Literals::Type::INTEGER() ).
    getSegmentDescription( 0 );
  iSegmentDescription[0].setAttributeName( Literals::Attribute::REPLYMSGID(),
					   "Reply Message ID" );

  iSegmentDescription[1] =
    theVND.getVariableManager( Literals::Type::INTEGER() ).
    getSegmentDescription( 0 );
  iSegmentDescription[1].setAttributeName( Literals::Attribute::PORT(),
					   "Number of ports" );
}

void
NRS::Message::ReplyMaxPortManager::createVariable( const std::string &aName )
  const
{
  new ReplyMaxPort( aName );
}
