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

#include <string>

#include "AttributeLiterals.hh"
#include "DeleteNode.hh"
#include "DeleteNodeManager.hh"
#include "Executive.hh"
#include "VariableNodeDirector.hh"

namespace
{
  NRS::Message::DeleteNodeManager sCNM;
}

NRS::Message::DeleteNodeManager::DeleteNodeManager() : 
  Base::VariableManager( NRS::Literals::Object::DELETENODE_VARIABLE(), "", 1 )
{
  Base::Executive::getExecutive().addStartupVariable( getMessageName(),
						      getMessageName() );
  iDescription = "The built-in type for messages which delete nodes.";
}

void NRS::Message::DeleteNodeManager::doConfigure()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  iSegmentDescription[0] =
    theVND.getVariableManager( Literals::Type::INTEGER() ).
    getSegmentDescription( 0 );
  iSegmentDescription[0].
    setAttributeName( Literals::Attribute::VNID(),
		      "VNID of node" );
}

void
NRS::Message::DeleteNodeManager::createVariable( const std::string &aName )
  const
{
  new DeleteNode( aName );
}
