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

#include "DeleteLink.hh"
#include "Executive.hh"
#include "Link.hh"
#include "Location.hh"
#include "MessageFunctionObject.hh"
#include "Node.hh"
#include "StringLiterals.hh"
#include "Variable.hh"
#include "VariableNodeDirector.hh"

NRS::Base::Variable::Variable( std::string aName,
			       const VariableManager &theVMRef ) :
  Observer(), Observable(), iVM( theVMRef ), iName( aName ),
  iSendMessage( false ), iNextLog( 0 ),
  iObserveVal( 0 ), iStateHolding( false ), iSelfUpdating( false ),
  iNextObserver( 1 ),
  iVNID( VariableNodeDirector::getDirector().addVariable( this ) ),
  iInputLink( iVM, iVNID ), iOutputLink( iVM, iVNID )
{
  iInputLink.addObserver( this, 0 );

  if (iName.find('.') == std::string::npos)
    {
      iParentName = "";
      iLocalName = iName;
    }
  else
    {
      iParentName = iName.substr( 0, iName.rfind('.') );
      iLocalName = iName.substr( iName.rfind('.') + 1, std::string::npos );
    }

  VariableNodeDirector::getDirector().addUnresolvedDep( this );

  _DEBUG_( "Created " << iName << " with vnid " << iVNID << "\n\t" );
}

NRS::Base::Variable::~Variable()
{
  _DEBUG_( "Deleting " << iName );
  for ( integer_t num = 0; num < (integer_t) iObserved.size(); ++num )
    {
      if (iObserved[num])
	iObserved[num]->removeObserver( this, num );
    }
  VariableNodeDirector::getDirector().removeVariable( iVNID );
  _DEBUG_( "Deleted " << iName );
}

void NRS::Base::Variable::addSource( const Location &aSourceRef )
{
  if (aSourceRef.isLocal())
    {
      integer_t aRef = getNextObserver();
      doAddSource( aSourceRef, aRef );
      Link *ptr = &VariableNodeDirector::getDirector().
	getVariable( aSourceRef.getVNID() )->getOutputLink();

      ptr->addObserver( this, aRef );
      if ((integer_t) iObserved.size() <= aRef)
	{
	  iObserved.resize( aRef + 1 );
	  iObserved[aRef] = ptr;
	}

      // Force the variable to send its current state if it is
      // stateholding
      VariableNodeDirector::getDirector().
	getVariable( aSourceRef.getVNID() )->forceSend( false );
    }
  else
    doAddSource( aSourceRef, 0 );

  iInputLink.addSource( aSourceRef );
  VariableNodeDirector::getDirector().addUnresolvedDep( this );
}

bool NRS::Base::Variable::removeSource( const Location &aSourceRef )
{
  bool success = iInputLink.removeSource( aSourceRef );
  if (success && aSourceRef.isLocal())
    {
      Observable *ptr = &VariableNodeDirector::getDirector().
	getVariable( aSourceRef.getVNID() )->getOutputLink();
      success = false;

      for ( integer_t num = 0; num < (integer_t) iObserved.size(); ++num )
	{
	  if (iObserved[num] == ptr)
	    {
	      iObserved[num]->removeObserver( this, num );
	      iObserved[num] = NULL;
	      success = true;
	    }
	}

      if (!success)
	_ABORT_( "Failed to find connection to remove for existing link" );
    }

  VariableNodeDirector::getDirector().addUnresolvedDep( this );
  return success;
}

void NRS::Base::Variable::addTarget( const Target &aTarget )
{
  iOutputLink.addTarget( aTarget );
  VariableNodeDirector::getDirector().addUnresolvedDep( this );
}

bool NRS::Base::Variable::removeTarget( const Target &aTarget )
{
  bool success = iOutputLink.removeTarget( aTarget );
  VariableNodeDirector::getDirector().addUnresolvedDep( this );
  return success;
}

void NRS::Base::Variable::addLog( Log *aLogPtr )
{
  iOutputLink.addLog( aLogPtr );
  VariableNodeDirector::getDirector().addUnresolvedDep( this );
}

bool NRS::Base::Variable::removeLog( const Log *aLogPtr )
{
  bool success = iOutputLink.removeLog( aLogPtr );
  VariableNodeDirector::getDirector().addUnresolvedDep( this );
  return success;
}

void NRS::Base::Variable::addMFO( MessageFunctionObject* aMFOPtr )
{
  VariableNodeDirector &theVND = VariableNodeDirector::getDirector();
  iMFOPtr.push_back( aMFOPtr );
  integer_t num = -iMFOPtr.size();

  if (theVND.hasVariable( aMFOPtr->getSourceName() ))
    {
      Observable *srcPtr =
	theVND.getVariable( aMFOPtr->getSourceName() );
      srcPtr->addObserver( this, num );
      aMFOPtr->reset( num );
    }
}

bool NRS::Base::Variable::resolveDependencies()
{
  VariableNodeDirector &theVND = VariableNodeDirector::getDirector();

  if (!iInputLink.resolveDependencies() ||
      !iOutputLink.resolveDependencies())
    return false;

  for ( unsigned_t num = 0; num < iMFOPtr.size(); ++num )
    {
      if (iMFOPtr[num]->getReference() == 0)
	{ // MFO link not yet up
	  if (!theVND.hasVariable( iMFOPtr[num]->getSourceName() ))
	    return false;
	  Observable *srcPtr =
	    theVND.getVariable(iMFOPtr[num]->getSourceName() );
	  srcPtr->addObserver( this, -1-num );
	  iMFOPtr[num]->reset( -1-num );
	}
    }

  Node *ptr = dynamic_cast< Node * >( this );
  if ((ptr!=NULL) && (!ptr->resolveContainsDependencies()))
    return false;

  if (!resolveComplexDependencies())
    return false;

  // Okay, everything's working, so we should update our targets now
  // if we are stateholding
  forceSend( false );

  return true;
}

void NRS::Base::Variable::setConnections( int min_in, int max_in,
					  int min_out, int max_out,
					  int min_log, int max_log )
{
  iInputLink.setConnections( min_in, max_in, 0, 0, 0, 0 );
  iOutputLink.setConnections( 0, 0, min_out, max_out, min_log, max_log );
  if (!resolveDependencies())
    VariableNodeDirector::getDirector().addUnresolvedDep( this );
}

void NRS::Base::Variable::update()
{
  if (iSelfUpdating)
    doUpdate();

  if (iSendMessage)
    {
      iSendMessage = false;
      alertObservers();
      iOutputLink.update();
    }
}

void NRS::Base::Variable::sendMessages()
{
  _DEBUG_( "start send " << iName );  

  iOutputLink.sendMessages();
  _DEBUG_( "end send " << iName );
}

NRS::integer_t NRS::Base::Variable::getNextObserver()
{
  return iNextObserver++;
}
      
void NRS::Base::Variable::setSelfUpdating( bool selfUpdating )
{
  iSelfUpdating = selfUpdating;
  iInputLink.setInstantTransmission( !selfUpdating );
}

void NRS::Base::Variable::removeObservable( integer_t aRef )
{
  doRemoveSource( aRef );
  if (aRef > 0)
    {
      if (iObserved[aRef])
	{
	  iObserved[aRef]->removeObserver( this, aRef );
	  iObserved[aRef] = NULL;
	}
      else
	_ABORT_( "Failed to find connection to remove for existing link" );
    }
  else if (aRef < 0)
    {
      VariableNodeDirector &theVND = VariableNodeDirector::getDirector();
      
      if (theVND.hasVariable( iMFOPtr[-1-aRef]->getSourceName() ))
	{
	  Observable *srcPtr =
	    theVND.getVariable( iMFOPtr[-1-aRef]->getSourceName() );
	  srcPtr->removeObserver( this, aRef );
	}
      iMFOPtr[-1-aRef]->reset();
      theVND.addUnresolvedDep( this );
    }
  else // (aRef == 0)
    {
      // lost input Link, probably in destructor so ignore as we die
    }
}

void NRS::Base::Variable::observe( integer_t aRef )
{
  if (aRef >= 0)
    {
      doObserve( aRef );
      if (!iSelfUpdating)
	{
	  update();
	  sendMessages();
	}
    }
  else
    {
      (*iMFOPtr[-1-aRef])();
    }
}

void NRS::Base::Variable::reset()
{
  doReset();
  iSendMessage = false;
  forceSend( false );
}

void NRS::Base::Variable::forceSend( bool sendEvents, bool doUpdate )
{
  if (sendEvents || iStateHolding)
    { // We do not generate events, but we do change current state
      if (doUpdate)
	{
	  iSendMessage = true;
	  update();
	}
      else
	{
	  alertObservers();
	  iOutputLink.update();
	}
      sendMessages();
    }
}

void NRS::Base::Variable::doAddSource( const Location &aSourceRef,
				       integer_t aRef )
{
}

void NRS::Base::Variable::doRemoveSource( integer_t aRef )
{
}

void NRS::Base::Variable::doReset()
{
}

void NRS::Base::Variable::doObserve( integer_t aRef )
{
  iSendMessage = true;
}

void NRS::Base::Variable::doUpdate()
{
  _ABORT_( "Using base doUpdate in self-updating variable" );
}

void NRS::Base::Variable::addReaction( integer_t aRef )
{
  _ABORT_( "Base class specialised observer method called on " << iName );
}

void NRS::Base::Variable::react( integer_t aRef )
{
  _ABORT_( "Base class specialised observer method called on " << iName );
}

bool NRS::Base::Variable::resolveComplexDependencies()
{
  return true;
}
