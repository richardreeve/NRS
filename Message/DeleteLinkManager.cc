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
#include "DeleteLink.hh"
#include "DeleteLinkManager.hh"
#include "Executive.hh"
#include "VariableNodeDirector.hh"

namespace
{
  NRS::Message::DeleteLinkManager sCNM;
}

NRS::Message::DeleteLinkManager::DeleteLinkManager() : 
  VariableManager( Literals::Object::DELETELINK_VARIABLE(), "", 5 )
{
  Base::Executive::getExecutive().addStartupVariable( getMessageName(),
						      getMessageName() );
  iDescription = "The built-in type for messages which delete links.";
}

void NRS::Message::DeleteLinkManager::doConfigure()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  iSegmentDescription[0] =
    theVND.getVariableManager( Literals::Type::BOOLEAN() ).
    getSegmentDescription( 0 );
  iSegmentDescription[0].
    setAttributeName( Literals::Attribute::SOURCENOTTARGET(),
		      "Aimed at the source not the target" );

  iSegmentDescription[1] =
    theVND.getVariableManager( Literals::String::TOKEN() ).
    getSegmentDescription( 0 );
  iSegmentDescription[1].
    setAttributeName( Literals::Attribute::CID(),
		      "CID of source" );

  iSegmentDescription[2] =
    theVND.getVariableManager( Literals::Type::INTEGER() ).
    getSegmentDescription( 0 );
  iSegmentDescription[2].
    setAttributeName( Literals::Attribute::VNID(),
		      "VNID of source" );

  iSegmentDescription[3] =
    theVND.getVariableManager( Literals::String::TOKEN() ).
    getSegmentDescription( 0 );
  iSegmentDescription[3].
    setAttributeName( Literals::Attribute::TARGETCID(),
		      "CID of target" );

  iSegmentDescription[4] =
    theVND.getVariableManager( Literals::Type::INTEGER() ).
    getSegmentDescription( 0 );
  iSegmentDescription[4].
    setAttributeName( Literals::Attribute::TARGETVNID(),
		      "VNID of target" );
}

void
NRS::Message::DeleteLinkManager::createVariable( const std::string &aName )
  const
{
  new DeleteLink( aName );
}

