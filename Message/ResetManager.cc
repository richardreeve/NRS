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

#include "ResetManager.hh"

#include "Reset.hh"
#include "StringLiterals.hh"
#include "Executive.hh"
#include "VoidManager.hh"

namespace
{
  NRS::Message::ResetManager sRM;
}

NRS::Message::ResetManager::ResetManager() : 
  Type::VoidManager( Literals::Object::RESET_VARIABLE(), "" )
{
  Base::Executive::getExecutive().addStartupVariable( getType(), getType() );
}

void NRS::Message::ResetManager::setDescription()
{
  iDescription = "The built-in type for messages which reset the component.";
}

void NRS::Message::ResetManager::createVariable( std::string aName )
{
  new Reset( aName );
}
