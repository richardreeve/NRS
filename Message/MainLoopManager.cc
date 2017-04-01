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
#include "Executive.hh"
#include "MainLoopManager.hh"
#include "MainLoop.hh"
#include "StringLiterals.hh"

namespace 
{
  NRS::Message::MainLoopManager sMLM;
}

NRS::Message::MainLoopManager::MainLoopManager() :
  Type::VoidManager( Literals::Object::MAINLOOP_VARIABLE(),
		     "System main loop" )
{
  Base::Executive::getExecutive().addStartupVariable( getMessageName(),
						      getMessageName() );
  iDescription = "This variable/message type allows recipients to\n"
    "be added to the main loop of the source's program.";
}

void
NRS::Message::MainLoopManager::createVariable( const std::string &aName ) const
{
  MainLoop *aML = new MainLoop( aName );
  aML->setConnections( 0, 0, 1, -1, 0, 0 );
}
