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
#include "Reset.hh"
#include "ResetManager.hh"
#include "StringLiterals.hh"
#include "VariableNodeDirector.hh"
#include "Target.hh"

NRS::Message::Reset::Reset( std::string aName ) :
  Type::Void( aName, Literals::Object::RESET_VARIABLE() )
{
}

NRS::Message::Reset::~Reset()
{
}

void NRS::Message::Reset::receiveMessage()
{
  Base::VariableNodeDirector::getDirector().reset();
}

void NRS::Message::Reset::sendMessage( Base::Target &theDestination )
{
  if (!theDestination.hasTarget())
    theDestination.setTargetVNName( Literals::Object::RESET_VARIABLE(),
				    false );
  
  dynamic_cast< ResetManager* >( iVM )->sendMessage( theDestination );
}
