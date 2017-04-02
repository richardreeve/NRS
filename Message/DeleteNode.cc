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

#include "BooleanSegment.hh"
#include "DeleteNode.hh"
#include "StringLiterals.hh"
#include "VariableNodeDirector.hh"
#include "Node.hh"
#include "DeleteNodeManager.hh"
#include "Target.hh"
#include "IntegerSegment.hh"
#include "StringSegment.hh"

NRS::Message::DeleteNode::DeleteNode( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::DELETENODE_VARIABLE() ) ),
  iInVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
						  getSegment( 0 ) ).iValue ),
  iOutVNID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
						   getSegment( 0 ) ).iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::DeleteNode::~DeleteNode()
{
}

void NRS::Message::DeleteNode::deleteNode( Base::Target &theTarget,
					   integer_t aVNID )
{
  if (theTarget.isLocal())
    { // then the command has arrived
      Base::VariableNodeDirector &theVND =
	Base::VariableNodeDirector::getDirector();
      
      _DEBUG_( "Deleting Node " << aVNID << "\n\t" );

      if (!theVND.hasNode( aVNID ))
	{
	  _ERROR_( "Node to delete does not exist\n\t" );
	}
      else
	{
	  delete theVND.getNode( aVNID );
	}
    }
  else
    { // Send it off
      // Add target
      iOutputLink.addTarget( theTarget );

      // create message
      iOutVNID = aVNID;

      if (theTarget.getVNID() == 0)
	{
	  Type::StringSegment tvnn;
	  tvnn.iValue = Literals::Object::DELETENODE_VARIABLE();
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

void NRS::Message::DeleteNode::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      Base::VariableNodeDirector &theVND =
	Base::VariableNodeDirector::getDirector();
      
      _DEBUG_( "Deleting Node " << iInVNID << "\n\t" );

      if (!theVND.hasNode( iInVNID ))
	{
	  _ERROR_( "Node to delete does not exist\n\t" );
	}
      else
	{
	  _DEBUG_( "" );
	  delete theVND.getNode( iInVNID );
	  _DEBUG_( "" );
	}

      // and send on to anyone connected...
      iOutVNID = iInVNID;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}
