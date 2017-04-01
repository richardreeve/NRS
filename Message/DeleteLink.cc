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
#include "DeleteLink.hh"
#include "DeleteLinkManager.hh"
#include "Executive.hh"
#include "IntegerSegment.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "Variable.hh"
#include "VariableNodeDirector.hh"

NRS::Message::DeleteLink::DeleteLink( std::string aName ) :
  Variable( aName, Base::VariableNodeDirector::getDirector().
	    getVariableManager( Literals::Object::DELETELINK_VARIABLE() ) ),
  iInSourceNotTarget( dynamic_cast< Type::BooleanSegment& >
		      ( iInputLink.getSegment( 0 ) ).iValue ),
  iInSourceCID( dynamic_cast< Type::StringSegment& >( iInputLink.
						      getSegment( 1 ) ).
		iValue ),
  iInSourceVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
							getSegment( 2 ) ).
		 iValue ),
  iInTargetCID( dynamic_cast< Type::StringSegment& >( iInputLink.
						      getSegment( 3 ) ).
		iValue ),
  iInTargetVNID( dynamic_cast< Type::IntegerSegment& >( iInputLink.
							getSegment( 4 ) ).
		 iValue ),
  iOutSourceNotTarget( dynamic_cast< Type::BooleanSegment& >
		       ( iOutputLink.getSegment( 0 ) ).iValue ),
  iOutSourceCID( dynamic_cast< Type::StringSegment& >( iOutputLink.
						       getSegment( 1 ) ).
		 iValue ),
  iOutSourceVNID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
							 getSegment( 2 ) ).
		  iValue ),
  iOutTargetCID( dynamic_cast< Type::StringSegment& >( iOutputLink.
						       getSegment( 3 ) ).
		 iValue ),
  iOutTargetVNID( dynamic_cast< Type::IntegerSegment& >( iOutputLink.
							 getSegment( 4 ) ).
		 iValue )
{
  iInputLink.setInstantTransmission( true );
}

NRS::Message::DeleteLink::~DeleteLink()
{
}

void NRS::Message::DeleteLink::deleteLink( Base::Target &theTarget,
					   bool sourceNotTarget,
					   const std::string &sCID,
					   integer_t sVNID,
					   const std::string &tCID,
					   integer_t tVNID )
{
  if (theTarget.isLocal())
    { // then the command has arrived
      std::string cid = Base::Executive::getExecutive().getCID();
      Base::VariableNodeDirector &theVND =
	Base::VariableNodeDirector::getDirector();

      if ((sourceNotTarget && (cid != sCID)) ||
	  (!sourceNotTarget && (cid != tCID)))
	_ERROR_( "Failed connection message - no such CID" );
      else
	{
	  if (sourceNotTarget)
	    { // source
	      if (theVND.hasVariable( sVNID ))
		{
		  Base::Target theTarget( tCID, getEmptyRoute(),
					  tVNID, NULL );
		  theVND.getVariable( sVNID )->
		    removeTarget( theTarget );
		}
	      else
		_ERROR_( "No such variable" );
	    }
	  else
	    { // target
	      if (theVND.hasVariable( tVNID ))
		{
		  Base::Target theTarget( sCID, getEmptyRoute(),
					  sVNID, NULL );
		  theVND.getVariable( tVNID )->
		    removeSource( theTarget );
		}
	      else
		_ERROR_( "No such variable" );
	    }
	}
      
    }
  else
    { // Send it off
      // Add target
      iOutputLink.addTarget( theTarget );

      // create message
      iOutSourceNotTarget = sourceNotTarget;
      iOutSourceCID = sCID;
      iOutSourceVNID = sVNID;
      iOutTargetCID = tCID;
      iOutTargetVNID = tVNID;

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

void NRS::Message::DeleteLink::doObserve( integer_t aRef )
{
  if (aRef == 0)
    {
      std::string cid = Base::Executive::getExecutive().getCID();
      Base::VariableNodeDirector &theVND =
	Base::VariableNodeDirector::getDirector();

      if ((iInSourceNotTarget && (cid != iInSourceCID)) ||
	  (!iInSourceNotTarget && (cid != iInTargetCID)))
	_ERROR_( "Failed connection message - no such CID" );
      else
	{
	  if (iInSourceNotTarget)
	    { // source
	      if (theVND.hasVariable( iInSourceVNID ))
		{
		  Base::Target theTarget( iInTargetCID, getEmptyRoute(),
					  iInTargetVNID, NULL );
		  theVND.getVariable( iInSourceVNID )->
		    removeTarget( theTarget );
		}
	      else
		_ERROR_( "No such variable" );
	    }
	  else
	    { // target
	      if (theVND.hasVariable( iInTargetVNID ))
		{
		  Base::Target theTarget( iInSourceCID, getEmptyRoute(),
					  iInSourceVNID, NULL );
		  theVND.getVariable( iInTargetVNID )->
		    removeSource( theTarget );
		}
	      else
		_ERROR_( "No such variable" );
	    }
	}
      // and send on to anyone connected...
      iOutSourceNotTarget = iInSourceNotTarget;
      iOutSourceCID = iInSourceCID;
      iOutSourceVNID = iInSourceVNID;
      iOutTargetCID = iInTargetCID;
      iOutTargetVNID = iInTargetVNID;
      iSendMessage = true;
    }
  else
    _ABORT_( "Being asked to receive unrecognised message" );
}
