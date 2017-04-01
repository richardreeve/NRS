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
#include "SetErrorRoute.hh"

#include "StringLiterals.hh"
#include "SetErrorRouteManager.hh"
#include "Target.hh"
#include "Exception.hh"

NRS::Message::SetErrorRoute::SetErrorRoute( std::string aName ) :
  Variable( aName, NRS::Literals::Object::SET_ERROR_ROUTE_VARIABLE() ),
  iMinPriority( 0 ), iHasErrorRoute( false )
{
}

NRS::Message::SetErrorRoute::~SetErrorRoute()
{
}

void NRS::Message::SetErrorRoute::receiveMessage( unsigned_t priority,
						  Base::Target &theRoute )
{
  iMinPriority = priority;
  iHasErrorRoute = !theRoute.hasArrived();
  iErrorRoute = theRoute;
}

void NRS::Message::SetErrorRoute::sendMessage( Base::Target &theTarget,
					       unsigned_t priority,
					       Base::Target &theRoute )
{
  dynamic_cast< SetErrorRouteManager* >( iVM )->sendMessage( theTarget,
							     priority,
							     theRoute );
}
