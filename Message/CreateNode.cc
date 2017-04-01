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

#include "BooleanSegment.hh"
#include "CreateLink.hh"
#include "CreateNode.hh"
#include "CreateNodeManager.hh"
#include "Executive.hh"
#include "IntegerSegment.hh"
#include "NodeFactory.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "VariableNodeDirector.hh"

NRS::Message::CreateNode::CreateNode( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::CREATENODE_VARIABLE() ) ),
  iInNodeType( dynamic_cast< Type::StringSegment& >( iInputLink.
						     getSegment( 0 ) ).
	       iValue ),
  iInVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						  getSegment( 1 ) ).
	   iValue ),
  iInNodeName( dynamic_cast< Type::StringSegment& >( iInputLink.
						     getSegment( 2 ) ).
	       iValue ),
  iOutNodeType( dynamic_cast< Type::StringSegment& >( iOutputLink.
						      getSegment( 0 ) ).
		iValue ),
  iOutVNID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
						   getSegment( 1 ) ).
	    iValue ),
  iOutNodeName( dynamic_cast< Type::StringSegment& >( iOutputLink.
						      getSegment( 2 ) ).
		iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::CreateNode::~CreateNode()
{
}

void NRS::Message::CreateNode::createNode( Base::Target &theTarget,
					   const std::string &nodeType,
					   integer_t aVNID,
					   const std::string &aName )
{
  if (theTarget.isLocal())
    { // then the command has arrived
      Base::VariableNodeDirector &theVND = 
	Base::VariableNodeDirector::getDirector();
      theVND.getNodeFactory( nodeType )->createNode( aName );
      
      if ( aName.find( '.' ) == std::string::npos )
	{
	  std::string ml = aName;
	  ml += ".";
	  ml += Literals::Object::MAINLOOP_VARIABLE();
	  if ( !theVND.hasVariable( std::string( Literals::Object::
						 MAINLOOP_VARIABLE() ) ) )
	    _ABORT_( "No true main loop variable\n\t" );
	  integer_t sVNID =
	    theVND.getVariableVNID( std::string ( Literals::Object::
						  MAINLOOP_VARIABLE() ) );
	  if ( !theVND.hasVariable( ml ) )
	    _ABORT_( "Just made a root node with no main loop variable\n\t" );
	  integer_t tVNID = theVND.getVariableVNID( ml );
	  Base::Executive &theE = Base::Executive::getExecutive();
	  std::string cid = theE.getCID();
	  Base::Target here( cid, getEmptyRoute(), 0, NULL );
	  dynamic_cast< CreateLink* >
	    ( theVND.getVariable( Literals::Object::
				  CREATELINK_VARIABLE() ) )->
	    createLink( here, true, cid, sVNID, cid, tVNID, false );
	  dynamic_cast< CreateLink* >
	    ( theVND.getVariable( Literals::Object::
				  CREATELINK_VARIABLE() ) )->
	    createLink( here, false, cid, sVNID, cid, tVNID, false );
	}
    }
  else
    { // Send it off
      // Add target
      iOutputLink.addTarget( theTarget );

      // create message
      iOutNodeType = nodeType;
      iOutVNID = aVNID;
      iOutNodeName = aName;

      if (theTarget.getVNID() == 0)
	{
	  Type::StringSegment tvnn;
	  tvnn.iValue = Literals::Object::DELETELINK_VARIABLE();
	  iOutputLink.setIntelligentSegment( Enum::TargetVNName, &tvnn );
	}

      if (!theTarget.getCID().empty())
	{
	  Type::StringSegment cid;
	  cid.iValue = theTarget.getCID();
	  iOutputLink.setIntelligentSegment( Enum::TargetCID, &cid );
	  Type::BooleanSegment isB;
	  isB.iValue = true;
	  iOutputLink.setIntelligentSegment( Enum::IsBroadcast, &isB );
	  Type::IntegerSegment hc;
	  hc.iValue = 9;
	  iOutputLink.setIntelligentSegment( Enum::HopCount, &hc );
	}
      

      // Send message
      iSendMessage = true;
      update();
      sendMessages();
      iOutputLink.clearIntelligentSegments();

      // and remove target
      iOutputLink.removeTarget( theTarget );
    }
}

void NRS::Message::CreateNode::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      Base::VariableNodeDirector &theVND = 
	Base::VariableNodeDirector::getDirector();
      theVND.getNodeFactory( iInNodeType )->createNode( iInNodeName );
      
      if ( iInNodeName.find( '.' ) == std::string::npos )
	{
	  std::string ml = iInNodeName;
	  ml += ".";
	  ml += Literals::Object::MAINLOOP_VARIABLE();
	  if ( !theVND.hasVariable( std::string( Literals::Object::
						 MAINLOOP_VARIABLE() ) ) )
	    _ABORT_( "No true main loop variable\n\t" );
	  integer_t sVNID =
	    theVND.getVariableVNID( std::string ( Literals::Object::
						  MAINLOOP_VARIABLE() ) );
	  if ( !theVND.hasVariable( ml ) )
	    _ABORT_( "Just made a root node with no main loop variable\n\t" );
	  integer_t tVNID = theVND.getVariableVNID( ml );
	  Base::Executive &theE = Base::Executive::getExecutive();
	  std::string cid = theE.getCID();
	  Base::Target here( cid, getEmptyRoute(), 0, NULL );
	  dynamic_cast< CreateLink* >
	    ( theVND.getVariable( Literals::Object::
				  CREATELINK_VARIABLE() ) )->
	    createLink( here, true, cid, sVNID, cid, tVNID, false );
	  dynamic_cast< CreateLink* >
	    ( theVND.getVariable( Literals::Object::
				  CREATELINK_VARIABLE() ) )->
	    createLink( here, false, cid, sVNID, cid, tVNID, false );
	}

      // and send on to anyone connected...
      iOutNodeType = iInNodeType;
      iOutVNID = iInVNID;
      iOutNodeName = iInNodeName;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}
