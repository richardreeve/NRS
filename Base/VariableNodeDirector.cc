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

#include <algorithm>
#include <dlfcn.h>
#include <libgen.h>

#pragma implementation
#include "VariableNodeDirector.hh"

#include "VariableManager.hh"
#include "NodeFactory.hh"
#include "Variable.hh"
#include "Node.hh"
#include "StringLiterals.hh"
#include "ArgumentDirector.hh"
#include "PluginDirector.hh"

namespace
{
  static NRS::Base::VariableNodeDirector &sVND =
  NRS::Base::VariableNodeDirector::getDirector();
}

NRS::Base::VariableNodeDirector::VariableNodeDirector() :
  iNextVNID( 1 ), iMaxVNID( 0 )
{
  ArgumentDirector::getDirector().
    addArgumentFielder( (char)0, "csl", "\t\t[ --csl ]\n",
			"\t--csl\t\t\t\toutputs the csl description for\n"
			"\t\t\t\t\tthe component\n", this );
}

NRS::Base::VariableNodeDirector::~VariableNodeDirector()
{
  while (iNameVMMap.begin() != iNameVMMap.end())
    {
      iNameVMMap.begin()->second->unRegister();
    }

  while (iNameNFMap.begin() != iNameNFMap.end())
    {
      iNameNFMap.begin()->second->unRegister();
    }
}

void NRS::Base::VariableNodeDirector::registration()
{
  for( typeof( iPendingVMMap.begin()) iter = iPendingVMMap.begin();
       iter != iPendingVMMap.end(); iter++ )
    iter->second->doRegister();
  iPendingVMMap.clear();

  std::for_each( iPendingNFList.begin(), iPendingNFList.end(),
		 std::mem_fun( &NodeFactory::doRegister ) );
  iPendingNFList.clear();

  resolveDependencies();
}

void
NRS::Base::VariableNodeDirector::
signupVariableManager( const std::string &aMessageName, VariableManager *aVM )
{
  iPendingVMMap.insert( std::pair< std::string, 
			VariableManager* >( aMessageName, aVM ) );
}

void
NRS::Base::VariableNodeDirector::addVariableManager( VariableManager *aVM )
{
  std::string aName = aVM->getMessageName();

  if ( hasVariableManager( aName ) )
    {
      if ( aVM->differ( getVariableManager( aName ) ) )
	{
	  _ABORT_( "Insertion attempt for different VariableManager '"
		   << aName << "'\n\t" );
	}
    }
  iNameVMMap.insert( std::pair< std::string, VariableManager* >( aName,
								 aVM ) );
}

void NRS::Base::VariableNodeDirector::
removeVariableManager( const VariableManager *aVM )
{
  std::string aName = aVM->getMessageName();

  typedef std::multimap< std::string, VariableManager* >::iterator
    iter;
  
  std::pair< iter, iter > range = iNameVMMap.equal_range( aName );
  
  iter theIter;
  for ( iter anIter = range.first; anIter != range.second; ++anIter )
    {
      if (anIter->second == aVM)
	theIter = anIter;
    }
  iNameVMMap.erase( theIter );
}

void NRS::Base::VariableNodeDirector::signupNodeFactory( NodeFactory *aNF )
{
  iPendingNFList.push_back( aNF );
}

void
NRS::Base::VariableNodeDirector::addNodeFactory( NodeFactory *aNF )
{
  std::string aName = aNF->getType();
  if ( hasNodeFactory( aName ) )
    {
      _ABORT_( "Reinsertion attempt for NodeFactory '"
	       << aName << "'\n\t" );
    }
  
  iNameNFMap[ aName ] = aNF;
}

void NRS::Base::VariableNodeDirector::removeNodeFactory( NodeFactory *aNF )
{
  if ( getNodeFactory( aNF->getType() ) != aNF )
    {
      _ABORT_( "NodeFactory not registered" );
    }
  removeNodeFactory( aNF->getType() );
}

void NRS::Base::VariableNodeDirector::removeNodeFactory( std::string aName )
{
  iNameNFMap.erase( aName );
}

NRS::unsigned_t
NRS::Base::VariableNodeDirector::addVariable( Variable *aVar )
{
  std::string aName = aVar->getName();
  const VariableManager &aVMRef = aVar->getVariableManager();

  if (hasVariable( aName ))
    {
      _ABORT_( "Reinsertion attempt for Variable '" << aName << "'\n\t" );
    }
  unsigned_t vnid = getNextVNID();

  iVariableNameVNIDMap[ aName ] = vnid;
  iVNIDIsNodeVector[ vnid ] = false;
  iVNIDVariableVector[ vnid ] = aVar;
  iVNIDNodeVector[ vnid ] = NULL;
  iVNIDVMVector[ vnid ] = &aVMRef;
  iVNIDNFVector[ vnid ] = NULL;

  return vnid;
}

// NRS::Base::Variable*
// NRS::Base::VariableNodeDirector::getVariable( Target &theTarget ) const
// {
//   if (!theTarget.hasArrived())
//     return NULL;

//   if (theTarget.hasTargetVNID())
//     return getVariable( theTarget.getTargetVNID() );
//   else
//     return getVariable( theTarget.getTargetVNName( true ) );
// }

// bool NRS::Base::VariableNodeDirector::hasVariable( Target &theTarget ) const
// {
//   if (!theTarget.hasArrived())
//     return false;

//   if (theTarget.hasTargetVNID())
//     return hasVariable( theTarget.getTargetVNID() );
//   else
//     return hasVariable( theTarget.getTargetVNName( true ) );
// }

// NRS::Base::Node*
// NRS::Base::VariableNodeDirector::getNode( Target &theTarget ) const
// {
//   if (!theTarget.hasArrived())
//     return NULL;

//   if (theTarget.hasTargetVNID())
//     return getNode( theTarget.getTargetVNID() );
//   else
//     return getNode( theTarget.getTargetVNName( true ) );
// }

// bool NRS::Base::VariableNodeDirector::hasNode( Target &theTarget ) const
// {
//   if (!theTarget.hasArrived())
//     return false;

//   if (theTarget.hasTargetVNID())
//     return hasNode( theTarget.getTargetVNID() );
//   else
//     return hasNode(  theTarget.getTargetVNName( true ) );
// }

void
NRS::Base::VariableNodeDirector::removeVariable( const std::string &aName )
{
  if (!hasVariable( aName ))
    {
      _ABORT_( "No such Variable as '" << aName << "'\n\t" );
    }
  unsigned_t vnid = iVariableNameVNIDMap[ aName ];
  //_DEBUG_( "Deleting " << aName );
  while (find( iOrphanList.begin(), iOrphanList.end(),
	       iVNIDVariableVector[ vnid ] ) != iOrphanList.end())
    iOrphanList.erase( find( iOrphanList.begin(), iOrphanList.end(),
			     iVNIDVariableVector[ vnid ] ) );

  iVNIDVariableVector[ vnid ] = NULL;
  iVNIDVMVector[ vnid ] = NULL;
  iVNIDNodeVector[ vnid ] = NULL;
  iVNIDNFVector[ vnid ] = NULL;
  iVariableNameVNIDMap.erase( aName );
  iNodeNameVNIDMap.erase( aName );
}

void
NRS::Base::VariableNodeDirector::removeVariable( const NRS::unsigned_t vnid )
{
  Variable *aVar = getVariable( vnid );
  if (!aVar)
    {
      _ABORT_( "No such Variable vnid: " << vnid << "\n\t" );
    }
  removeVariable( aVar->getName() );
}

std::ostream&
NRS::Base::VariableNodeDirector::
displayVariables( std::ostream &aStream ) const
{
  aStream << "<VariableList>" << "\n";
  for ( typeof( iVariableNameVNIDMap.begin()) anIter = 
	  iVariableNameVNIDMap.begin();
	anIter != iVariableNameVNIDMap.end();
	anIter++ )
    {
      aStream << "\t<Variable name='" << anIter->first 
	      << "' variableType='" 
	      << iVNIDVMVector[anIter->second]->getMessageName()
	      << "' vnid='" << anIter->second
	      << "'/>\n";
    }
  return aStream << "</VariableList>" << std::endl;
}

void NRS::Base::VariableNodeDirector::addNode( Node *aNode, unsigned_t vnid )
{
  std::string aName = aNode->getName();
  const NodeFactory *aNF = aNode->getNodeFactory();

  if (hasNode( aName ))
    {
      _ABORT_( "Reinsertion attempt for Node '" << aName << "'\n\t" );
    }

  if ((!hasVariable( aName )) || (iVariableNameVNIDMap[ aName ] != vnid))
    {
      _ABORT_( "Node '" << aName << "' is not registered as a variable\n\t" );
    }

  iNodeNameVNIDMap[ aName ] = vnid;
  iVNIDIsNodeVector[ vnid ] = true;
  iVNIDNodeVector[ vnid ] = aNode;
  iVNIDNFVector[ vnid ] = aNF;
}

std::ostream &
NRS::Base::VariableNodeDirector::displayNodes( std::ostream &aStream ) const
{
  aStream << "<NodeList>" << "\n";
  for ( typeof( iNodeNameVNIDMap.begin() ) anIter = 
	  iNodeNameVNIDMap.begin(); anIter != iNodeNameVNIDMap.end();
	anIter++ )
    {
      aStream << "\t<Node name='" << anIter->first 
	      << "' nodeType='" << iVNIDNFVector[anIter->second]->getType()
	      << "' vnid='" << anIter->second
	      << "'/>\n";
    }
  return aStream << "</NodeList>" << std::endl;
}

std::string NRS::Base::VariableNodeDirector::getCSLDescription()
{
  std::stringstream theCSL;

  theCSL << "<Capabilities"
	 << " xmlns='" << NRS::Literals::XML::CSL_NAMESPACE() << "'"
	 << " cType='" << PluginDirector::getDirector().getCType() << "'"
	 << " cVersion='" << VERSION << "'"
	 << ">\n";

  for ( std::multimap< std::string, VariableManager* >::iterator anIter =
	  iNameVMMap.begin(); anIter != iNameVMMap.end();
	anIter = iNameVMMap.upper_bound( anIter->first ) )
    {
      if (anIter->second->isType())
	anIter->second->getCSL( theCSL );
    }

  for ( std::multimap< std::string, VariableManager* >::iterator anIter =
	  iNameVMMap.begin(); anIter != iNameVMMap.end();
	anIter = iNameVMMap.upper_bound( anIter->first ) )
    {
      if (anIter->second->isUnit())
	anIter->second->getCSL( theCSL );
    }

  for ( std::multimap< std::string, VariableManager* >::iterator anIter =
	  iNameVMMap.begin(); anIter != iNameVMMap.end();
	anIter = iNameVMMap.upper_bound( anIter->first ) )
    {
      if ((!anIter->second->isType()) && (!anIter->second->isUnit()))
	anIter->second->getCSL( theCSL );
    }

  for ( std::map< std::string, NodeFactory* >::iterator anIter =
	  iNameNFMap.begin(); anIter != iNameNFMap.end(); anIter++ )
    {
      anIter->second->getCSL( theCSL );
    }
	  
  theCSL << "</Capabilities>\n";

  return theCSL.str();
}

NRS::unsigned_t NRS::Base::VariableNodeDirector::getNextVNID()
{
  if (iNextVNID >= UNSIGNED_T_MAX)
    _ABORT_( "VNID wraparound - cannot handle\n\t" );

  if (iNextVNID > iMaxVNID)
    iMaxVNID = iNextVNID;

  if ( iVNIDNodeVector.capacity() <= iMaxVNID )
    iVNIDNodeVector.reserve( 2 * (iMaxVNID + 1) );
  if ( iVNIDNodeVector.size() <= iMaxVNID )
    iVNIDNodeVector.resize( iMaxVNID + 1 );
  
  if ( iVNIDIsNodeVector.capacity() <= iMaxVNID )
    iVNIDIsNodeVector.reserve( 2 * (iMaxVNID + 1) );
  if ( iVNIDIsNodeVector.size() <= iMaxVNID )
    iVNIDIsNodeVector.resize( iMaxVNID + 1 );
  
  if ( iVNIDVariableVector.capacity() <= iMaxVNID )
    iVNIDVariableVector.reserve( 2 * (iMaxVNID + 1) );
  if ( iVNIDVariableVector.size() <= iMaxVNID )
    iVNIDVariableVector.resize( iMaxVNID + 1 );
  
  if ( iVNIDNFVector.capacity() <= iMaxVNID )
    iVNIDNFVector.reserve( 2 * (iMaxVNID + 1) );
  if ( iVNIDNFVector.size() <= iMaxVNID )
    iVNIDNFVector.resize( iMaxVNID + 1 );
  
  if ( iVNIDVMVector.capacity() <= iMaxVNID )
    iVNIDVMVector.reserve( 2 * (iMaxVNID + 1) );
  if ( iVNIDVMVector.size() <= iMaxVNID )
    iVNIDVMVector.resize( iMaxVNID + 1 );
  
  return iNextVNID++;
}

bool NRS::Base::VariableNodeDirector::usingPlugin( std::string aPlugin ) const
{
  for ( typeof( iNameNFMap.begin() ) anIter = iNameNFMap.begin();
	anIter != iNameNFMap.end(); anIter++ )
    {
      // is the plugin right?
      if ( anIter->second->getPlugin() == aPlugin )
	{
	  // yes, so are we using the NodeFactory?
	  for ( unsigned_t i = 0; i < getMaxVNID(); ++i )
	    {
	      if (iVNIDNFVector[i] == anIter->second)
		return true;
	    }
	}
    }
  return false;
}

std::string NRS::Base::VariableNodeDirector::queryFielder() const
{
  return NRS::Literals::Object::VARIABLENODEDIRECTOR();
}

void NRS::Base::VariableNodeDirector::
fieldArguments( std::list< std::string > &arguments )
{
  std::string arg = *arguments.begin();
  arguments.pop_front();

  if (arg == "csl")
    {
      std::cout << getCSLDescription();
      exit( 0 );
    }
  else
  _ABORT_( "Fielding unknown argument '" << arg << "'\n\t" );
}
 
bool NRS::Base::VariableNodeDirector::resolveDependencies()
{
  std::list< Variable* > stillOrphans;

  for ( typeof( iOrphanList.begin() ) anIter = iOrphanList.begin();
	anIter != iOrphanList.end(); ++anIter )
    {
      if (!(*anIter)->resolveDependencies())
	{
	  stillOrphans.push_back( *anIter );
	}
      else
	{
	  _DEBUG_( "Resolved dependency issues with " << (*anIter)->getName()
		   << "\n\t" );
	}
    }
  if ((!iOrphanList.empty()) && stillOrphans.empty())
    {
      _DEBUG_( "Resolved all dependencies\n\t" );
      reset();
    }

  if ((!stillOrphans.empty()) && (iOrphanList.size() != stillOrphans.size()))
    _DEBUG_("Unresolved dependency issues with " 
	    << (*stillOrphans.begin())->getName() << "\n\t" );

  iOrphanList = stillOrphans;
  return stillOrphans.empty();
}

void NRS::Base::VariableNodeDirector::addUnresolvedDep( Variable* aVariable )
{
  if (find( iOrphanList.begin(), iOrphanList.end(), aVariable ) ==
      iOrphanList.end())
    {
      iOrphanList.push_back( aVariable );
      _DEBUG_( "Added unresolved dependency to list: "
	       << aVariable->getName() << "\n\t" );
    }
}

void NRS::Base::VariableNodeDirector::reset() const
{
  for ( unsigned_t i = 0; i < iMaxVNID; ++i )
    {
      if (iVNIDVariableVector[i] != NULL)
	iVNIDVariableVector[i]->reset();
    }
}

const NRS::Base::VariableManager &
NRS::Base::VariableNodeDirector::
getPendingVariableManager( const std::string &aType ) const
{
  if (iPendingVMMap.find( aType ) == iPendingVMMap.end())
    throw Exception( "Non-existent VariableManager ", _FL_ ) << aType;

  VariableManager &aVM = *iPendingVMMap.find( aType )->second;
  aVM.configure();
  return aVM;
}
