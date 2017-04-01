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
#include "CreateNode.hh"
#include "CreateNodeManager.hh"
#include "Executive.hh"
#include "VariableNodeDirector.hh"

namespace
{
  NRS::Message::CreateNodeManager sCNM;
}

NRS::Message::CreateNodeManager::CreateNodeManager() : 
  VariableManager( Literals::Object::CREATENODE_VARIABLE(), "", 3 )
{
  Base::Executive::getExecutive().addStartupVariable( getMessageName(),
						      getMessageName() );
  iDescription = "The built-in type for messages which create nodes.";
}

void NRS::Message::CreateNodeManager::doConfigure()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  iSegmentDescription[0] =
    theVND.getVariableManager( Literals::String::TOKEN() ).
    getSegmentDescription( 0 );
  iSegmentDescription[0].
    setAttributeName( Literals::Attribute::VNTYPE(),
		      "node type to create" );

  iSegmentDescription[1] =
    theVND.getVariableManager( Literals::Type::INTEGER() ).
    getSegmentDescription( 0 );
  iSegmentDescription[1].
    setAttributeName( Literals::Attribute::VNID(),
		      "VNID of node" );

  iSegmentDescription[2] =
    theVND.getVariableManager( Literals::String::VNNAME() ).
    getSegmentDescription( 0 );
  iSegmentDescription[2].
    setAttributeName( Literals::Attribute::VNNAME(),
		      "node name" );
}

void
NRS::Message::CreateNodeManager::createVariable( const std::string &aName )
  const
{
  new CreateNode( aName );
}
