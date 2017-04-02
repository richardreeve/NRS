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

#include <list>

#include "Node.hh"
#include "Variable.hh"
#include "VariableNodeDirector.hh"

NRS::Base::Node::Node( std::string aName, unsigned_t aVNID,
		       const NodeFactory *aNF ) : 
  iName( aName ), iVNID( aVNID ), iNF( aNF )
{
  VariableNodeDirector &theVND = VariableNodeDirector::getDirector();
  Variable *me = theVND.getVariable( iName );
  std::string aParent = me->getParentName();
  std::string aLocal = me->getLocalName();

  VariableNodeDirector::getDirector().addNode( this, iVNID );
  if (aParent.empty())
    {
      if (!iNF->isRootNode())
	_ABORT_( "Root node creation called on non-root node\n\t" );
      _TODO_( "Not checking for number of root occurs\n\t" );
    }
  else
    {
      if (!theVND.hasNode( aParent ))
	_ABORT_( "Failed to find parent node for " << iName );
      Node *pNode = theVND.getNode( aParent );
      pNode->addVariable( me );
    }

}


NRS::Base::Node::~Node()
{
  _DEBUG_( "Deleting " << iName );
  while (!iVariableMap.empty())
    {
      _DEBUG_( "deleting " << iVariableMap.begin()->first );
      delete iVariableMap.begin()->second;
      std::string name = iVariableMap.begin()->first;
      iVariableMap.erase( iVariableMap.begin()->first );
      _DEBUG_( "deleted " << name );
    }
  _DEBUG_( "Deleted " << iName );
  // Don't need to remove from register, as Variable destructor does this
}

void NRS::Base::Node::addVariable( Variable *aVar )
{
  iVariableMap[aVar->getLocalName()]= aVar;
  VariableNodeDirector::getDirector().
    addUnresolvedDep( VariableNodeDirector::getDirector().
		      getVariable( iName ) );
}

bool NRS::Base::Node::resolveContainsDependencies()
{
  std::map< std::string, Variable* > aVMap = iVariableMap;
  VariableNodeDirector &theVND = VariableNodeDirector::getDirector();

  std::vector< std::string > vNames = iNF->getVariableNames();

  for ( unsigned_t num = 0; num < vNames.size(); ++num )
    {
      NodeFactory::VariableDescription aVD =
	iNF->getVariableDescription( vNames[num] );
      if (!theVND.hasVariable( getName() + "." + vNames[num] ))
	return false;
      else
	{
	  if ( aVMap.find( vNames[num] ) == aVMap.end() )
	    {
	      iVariableMap[ vNames[num] ] =
		theVND.getVariable( getName() + "." + vNames[num] );
	      iVariableMap[ vNames[num] ]->
		setConnections( aVD.iIDMap[ "in" ].iMinOccurs,
				aVD.iIDMap[ "in" ].iMaxOccurs,
				aVD.iIDMap[ "out" ].iMinOccurs,
				aVD.iIDMap[ "out" ].iMaxOccurs,
				aVD.iIDMap[ "log" ].iMinOccurs,
				aVD.iIDMap[ "log" ].iMaxOccurs );
	      iVariableMap[ vNames[num] ]->
		setStateHolding( aVD.iStateHolding );
	      iVariableMap[ vNames[num] ]->
		setSelfUpdating( aVD.iSelfUpdating );
	      theVND.getVariable( iName )->reset();
	    }
	  else
	    {
	      aVMap.erase( vNames[num] );
	    }
	}
    }

  for ( typeof( aVMap.begin() ) lIter = aVMap.begin();
	lIter != aVMap.end(); lIter++ )
    {
      if (!theVND.hasNode( lIter->second->getName() ))
	_ABORT_( "We have a non-node variable in the contents ("
		 << lIter->second->getName() << ")\n\t" );
      Node *him = theVND.getNode( lIter->second->getName() );
      std::string aType = him->getNodeFactory()->getType();
      bool found = false;
      std::list< NodeFactory::NodeContent > ncList = iNF->getNodeContents();
      for ( typeof( ncList.begin() ) anIter = ncList.begin();
	    anIter != ncList.end(); anIter++ )
	{
	  if (aType == anIter->iNodeType)
	    found = true;
	  else
	    {
	      std::map< std::string, std::string > groupMap =
		him->iNF->getGroupMap();

	      // Look at the groups to see if it's one of them
	      for ( typeof( groupMap.begin() ) gIter = groupMap.begin();
		    gIter != groupMap.end(); gIter++ )
		if (anIter->iNodeType == gIter->first)
		  found = true;
	    }
	}
      if (!found)
	return false;
      _TODO_( "Not checking for number of occurs or attribute restrictions" );
    }
  return true;
}
