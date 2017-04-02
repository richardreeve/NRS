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

#ifndef _VARIABLE_NODE_DIRECTOR_HH
#define _VARIABLE_NODE_DIRECTOR_HH

#include <iostream>
#include <list>
#include <map>
#include <string>
#include <vector>

#include "ArgumentFielder.hh"
#include "Exception.hh"
#include "Types.hh"
#include "VariableManager.hh"

namespace NRS
{
  namespace Base
  {
    class NodeFactory;
    class Node;
    class Variable;

    /// This class holds the framework within which NRS sits.
    /**
     * 
     * A single VariableNodeDirector, created at initialisation, holds all
     * of the variable/node information about the system, 
     * and acts as the framework
     * within which Nodes and Variables are created and managed.
     * 
     **/
    class VariableNodeDirector : public ArgumentFielder
    {
    public:
      /// static accessor for the VariableNodeDirector.
      /**
       *
       * \return A reference to the VariableNodeDirector.
       *
       **/
      static VariableNodeDirector &getDirector ()
      {
	static VariableNodeDirector sVND;
	return sVND;
      }
    
      /// Register all Managers and Factories
      /// signed up with the system
      void registration();

     /// Outputs the CSL description of the current component
      /**
       *
       * This method outputs the full CSL description of everything
       * which has been loaded into the Director.
       *
       * \return the CSL description in a string.
       *
       **/
      std::string getCSLDescription();

      /// Sign up a VariableManager for registration
      /**
       *
       * \param aMessageName The name of the Message controlled by the
       * VariableManager
       * \param aVM The pointer to the VariableManager to be added.
       *
       **/

      void signupVariableManager( const std::string &aMessageName,
				  VariableManager *aVM );

      
      /// Check for a specific VariableManager.
      /**
       *
       * This method checks whether a VariableManager of a given name
       * exists.
       *
       * \param aName The name of the VariableManager to check for.
       *
       * \return bool signifying whether the VariableManager exists.
       *
       **/
      bool hasVariableManager( std::string aName ) const
      {
	return (( iNameVMMap.find( aName ) !=
		  iNameVMMap.end() ) ||
		( iPendingVMMap.find( aName ) !=
		  iPendingVMMap.end() ));
      }

      /// Adds a VariableManager to the records.
      /**
       *
       * This method adds a VariableManager to the VariableNodeDirector's
       * records.
       *
       * \param aVM The pointer to the VariableManager to be added.
       *
       **/
      void addVariableManager( VariableManager *aVM );

      /// Gets the VariableManager for a specific Variable type.
      /**
       *
       * This method returns a VariableManager associated with a
       * Variable type. Several such VariableManagers may be defined
       * for the same Variable type, as every plugin using a Variable
       * type should supply a manager for that type. Any of these
       * VariableManagers could be returned.
       *
       * \param aType The type of the Variable whose manager is being
       * sought.
       *
       * \return a const reference to the manager for that variable type.
       *
       * \exception Exception thrown if the VariableManager for that
       * type does not exist.
       *
       **/
      const VariableManager &getVariableManager( std::string aType ) const
      {
	if (iNameVMMap.find( aType ) == iNameVMMap.end())
	  return getPendingVariableManager( aType );

	return *iNameVMMap.find( aType )->second;
      }

     /// Gets a VariableManager pointer for a specific Variable vnid.
      /**
       *
       * \param vnid The vnid of the Variable whose manager is being
       * sought.
       *
       * \return a const reference to the VariableManager
       *
       * \exception Exception thrown if the Variable does not exist.
       **/
      const VariableManager &getVariableManager( const unsigned_t vnid ) const
      {
	if ((vnid >= iVNIDVMVector.size()) || (!iVNIDVMVector[ vnid ]))
	  throw Exception( "Variable does not exist", _FL_ );
	    
	return *iVNIDVMVector[ vnid ];
      }

      /// Remove a VariableManager from the VariableNodeDirector's records
      /**
       *
       * This method removes a VariableManager from the
       * VariableNodeDirector's records.
       *
       * \param aVMPtr The pointer to the VariableManager to be removed.
       *
       * \exception Exception thrown if the VariableManager is not
       * registered with the VariableNodeDirector.
       *
       **/
      void removeVariableManager( const VariableManager *aVMPtr );


      /// Sign up a NodeFactory for registration
      /**
       *
       * \param aNF The pointer to the NodeFactory to be added.
       *
       **/

      void signupNodeFactory( NodeFactory *aNF );

      
      /// Check for a specific NodeFactory.
      /**
       *
       * This method checks whether a NodeFactory of a given name
       * exists.
       *
       * \param aName The name of the NodeFactory to check for.
       *
       * \return bool signifying whether the NodeFactory exists.
       *
       **/
      bool hasNodeFactory( std::string aName ) const
      {
	return ( iNameNFMap.find( aName ) !=
		 iNameNFMap.end() );
      }

      /// Adds a NodeFactory to the records.
      /**
       *
       * This method adds a NodeFactory to the Director's
       * records.
       *
       * \param aNF The pointer to the NodeFactory to be added.
       *
       * \exception Exception thrown if a NodeFactory of this type has
       * already been registered.
       *
       **/
      void addNodeFactory( NodeFactory *aNF );

      /// Gets the NodeFactory for a specific Node type.
      /**
       *
       * This method returns a NodeFactory associated with a
       * Node type.
       *
       * \param aName The type of the Node whose manager is being
       * sought.
       *
       * \return a pointer to the manager for that Node type.
       *
       * \exception Exception thrown if the NodeFactory for that type
       * does not exist.
       *
       **/
      NodeFactory *getNodeFactory( std::string aName ) const
      {
	if ( !hasNodeFactory( aName ) )
	  {
	    _ABORT_( "No such NodeFactory as '" << aName << "'\n\t" );
	  }
	return iNameNFMap.find( aName )->second;
      }

      /// Gets a NodeFactory pointer for a specific Node vnid.
      /**
       *
       * \param vnid The vnid of the Node whose manager is being
       * sought.
       *
       * \return a pointer to the NodeFactory or NULL.
       *
       **/
      const NodeFactory *getNodeFactory( const unsigned_t vnid ) const
      {
	if (vnid >= iVNIDNFVector.size())
	  {
	    return NULL;
	  }
	return iVNIDNFVector[ vnid ];
      }

      /// Remove a NodeFactory from the Director's records
      /**
       *
       * This method removes a NodeFactory from the Director's
       * records.
       *
       * \param aName The name of the NodeFactory to be removed.
       *
       * \exception Exception thrown if no such NodeFactory is
       * registered with the Director.
       *
       **/
      void removeNodeFactory( std::string aName );

      /// Remove a NodeFactory from the Director's records
      /**
       *
       * This method removes a NodeFactory from the Director's
       * records.
       *
       * \param aNF The pointer to the NodeFactory to be removed.
       *
       * \exception Exception thrown if the NodeFactory is not
       * registered with the Director.
       *
       **/
      void removeNodeFactory( NodeFactory *aNF );


      /// Check for a specific Variable.
      /**
       *
       * This method checks whether an Variable of a given
       * name exists.
       *
       * \param aName The name of the Variable to check for.
       *
       * \return bool signifying whether the Variable exists.
       *
       **/
      bool hasVariable( const std::string &aName ) const
      {
	return ( iVariableNameVNIDMap.find( aName ) !=
		 iVariableNameVNIDMap.end() );
      }
      
      /// Check for a specific Variable.
      /**
       *
       * This method checks whether an Variable of a given
       * vnid exists.
       *
       * \param vnid vnid of variable to be checked for
       *
       * \return bool signifying whether the Variable exists.
       *
       **/
      bool hasVariable( unsigned_t vnid ) const
      {
	if (vnid >= iVNIDVariableVector.size())
	  {
	    return false;
	  }
	return (iVNIDVariableVector[ vnid ] != NULL);
      }

      /// Adds a Variable to the records.
      /**
       *
       * This method adds a Variable to the VariableNodeDirector's records.
       *
       * \param aVar The pointer to the Variable to be added.
       *
       * \exception Exception thrown if a Variable of this name has
       * already been registered.
       *
       **/
      unsigned_t addVariable( Variable* aVar );
      
      /// Gets the vnid of a Variable with a specific name
      /**
       *
       * \param aName The name of the Variable being sought.
       *
       * \return the vnid of the Variable.
       *
       * \exception Exception thrown if a Variable of that name does
       * not exist.
       *
       **/
      unsigned_t getVariableVNID( const std::string &aName ) const
      {
	if (!hasVariable( aName ))
	  {
	    _ABORT_( "No such Variable as '" << aName << "'\n\t" );
	  }
	return iVariableNameVNIDMap.find( aName )->second;
      }
      
      /// Gets a Variable pointer for a specific variable name.
      /**
       *
       * \param aName The name of the Variable being sought.
       *
       * \return a pointer to the Variable or NULL.
       *
       **/
      Variable *getVariable( const std::string &aName ) const
      {
	if (!hasVariable( aName ))
	  {
	    _ABORT_( "No such Variable as '" << aName << "'\n\t" );
	  }
	return iVNIDVariableVector[ iVariableNameVNIDMap.
				    find( aName )->second ];
      }

      /// Gets a Variable pointer for a specific vnid.
      /**
       *
       * \param vnid The vnid of the Variable being sought.
       *
       * \return a pointer to the Variable or NULL.
       *
       **/
      Variable *getVariable( const unsigned_t vnid ) const
      {
	if (vnid >= iVNIDVariableVector.size())
	  {
	    return NULL;
	  }
	return iVNIDVariableVector[ vnid ];
      }
      
     /// Remove a Variable from the VariableNodeDirector's records
      /**
       *
       * \param aName The name of the Variable to be removed.
       *
       * \exception Exception thrown if no such Variable is registered
       * with the VariableNodeDirector.
       *
       **/
      void removeVariable( const std::string &aName );

      /// Remove a Variable from the VariableNodeDirector's records
      /**
       *
       * \param vnid The vnid of the Variable to be removed.
       *
       * \exception Exception thrown if no such Variable vnid is
       * registered with the VariableNodeDirector.
       *
       **/
      void removeVariable( const unsigned_t vnid );
      
      /// Outputs a debugging list of the currently defined Variables.
      /**
       *
       * \param aStream A reference to the output stream to which to
       * send the variable list.
       *
       * \return the output stream reference.
       *
       **/
      std::ostream &displayVariables( std::ostream &aStream ) const;


      /// Check for a specific Node.
      /**
       *
       * This method checks whether an Node of a given
       * name exists.
       *
       * \param aName The name of the Node to check for.
       *
       * \return bool signifying whether the Node exists.
       *
       **/
      bool hasNode( const std::string &aName ) const
      {
	return ( iNodeNameVNIDMap.find( aName ) !=
		 iNodeNameVNIDMap.end() );
      }
      
      /// Check for a specific Node.
      /**
       *
       * This method checks whether an Node of a given
       * vnid exists.
       *
       * \param vnid The vnid of the Node to check for.
       *
       * \return bool signifying whether the Node exists.
       *
       **/
      bool hasNode( unsigned_t vnid ) const
      {
	if (vnid >= iVNIDNodeVector.size())
	  {
	    return false;
	  }
	return (iVNIDNodeVector[ vnid ] != NULL);
	
      }
      
      /// Adds a Node to the records.
      /**
       *
       * This method adds a Node to the Director's records.
       *
       * \param aNode The pointer to the Node to be added.
       * \param vnid vnid already associated with variable version of node
       *
       * \exception Exception thrown if a Node of this name has
       * already been registered.
       *
       **/
      void addNode( Node* aNode, unsigned_t vnid );
      
      /// Gets the vnid of a Node with a specific name
      /**
       *
       * \param aName The name of the Node being sought.
       *
       * \return the vnid of the Node.
       *
       * \exception Exception thrown if a Node of that name does
       * not exist.
       *
       **/
      unsigned_t getNodeVNID( const std::string &aName ) const
      {
	if (!hasNode( aName ))
	  {
	    _ABORT_( "No such Node as '" << aName << "'\n\t" );
	  }
	return iNodeNameVNIDMap.find( aName )->second;
      }
      
      /// Gets a Node pointer for a specific node name.
      /**
       *
       * \param aName The name of the Node being sought.
       *
       * \return a pointer to the Node
       *
       * \exception Exception thrown if a Node of that name does
       * not exist.
       *
       **/
      Node *getNode( const std::string &aName ) const
      {
	if (!hasNode( aName ))
	  {
	    _ABORT_( "No such Node as '" << aName << "'\n\t" );
	  }
	return iVNIDNodeVector[ iNodeNameVNIDMap.find( aName )->second ];
      }

      /// Gets a Node pointer for a specific vnid.
      /**
       *
       * \param vnid The vnid of the Node being sought.
       *
       * \return a pointer to the Node or NULL.
       *
       **/
      Node *getNode( const unsigned_t vnid ) const
      {
	if (vnid >= iVNIDNodeVector.size())
	  {
	    return NULL;
	  }
	return iVNIDNodeVector[ vnid ];
      }
      
      /// Outputs a debugging list of the currently defined Nodes.
      /**
       *
       * \param aStream A reference to the output stream to which to
       * send the node list.
       *
       * \return the output stream reference.
       *
       **/
      std::ostream &displayNodes( std::ostream &aStream ) const;

      /// Returns a number greater than the highest allocated VNID
      /**
       *
       * \returns a number guaranteed to be bigger than the highest
       * allocated VNID
       *
       **/
      unsigned_t getMaxVNID() const
      {
	return iMaxVNID;
      }

      /// Returns whether a given plugin is currently being used
      /**
       *
       * \param aPlugin name of plugin to check
       *
       * \returns whether the plugin is being used
       *
       **/
      bool usingPlugin( std::string aPlugin ) const;

      /// Returns description of fielder
      /**
       *
       * \returns a description of the fielder for debugging
       *
       **/
      std::string queryFielder() const;

      /// Parse requested command line arguments for the program
      /**
       * NB: this method will modify the arguments in place
       *
       * \param arguments the arguments to the system as an array of
       * strings
       *
       **/      
      void fieldArguments( std::list< std::string > &arguments );

      /// Does the component have any unresolved dependencies?
      /**
       *
       * \returns whether the component has any unresolved dependencies
       *
       **/
      bool hasUnresolvedDeps() const
      {
	return iOrphanList.empty();
      }

      /// Resolve any outstanding dependencies
      /**
       *
       * \returns whether the dependencies were all resolved
       *
       **/
      bool resolveDependencies();

      /// Add an unresolved dependency
      /**
       *
       * \param aVariable the variable which has an unresolved depndencies
       *
       **/
      void addUnresolvedDep( Variable *aVariable );

      /// Reset system
      /**
       *
       * Calls reset on all variables in system
       *
       **/
      void reset() const;

    private:
      /// Destructor
      /**
       *
       * This is a private destructor as no-one can create or destroy
       * VariableNodeDirectors.
       *
       **/
      virtual ~VariableNodeDirector();

      /// Constructor
      /**
       *
       * This is a private constructor as no-one can create or destroy
       * VariableNodeDirectors.
       *
       **/
      VariableNodeDirector();

      /// Gets next vnid for a Variable or Node.
      /**
       *
       * This method gets the next free vnid for a Variable or Node
       * being created.
       *
       **/
      unsigned_t getNextVNID();

      /// Gets a VariableManager by name from the pending list.
      /**
       *
       * This method returns a VariableManager associated with a
       * Variable type from the pending list. It is called by
       * getVariableManager if it fails to find a manager.
       *
       * \param aType The type of the Variable whose manager is being
       * sought.
       *
       * \return a const reference to the manager for that variable type.
       *
       * \exception Exception thrown if the VariableManager for that
       * type does not exist.
       *
       **/
      const VariableManager &
      getPendingVariableManager( const std::string &aType ) const;

      /// A list of pending VariableManagers which need to be registered
      std::multimap< std::string, VariableManager* > iPendingVMMap;
      
      /// A map of names of VariableManagers to the VariableManager
      /// pointers themselves.
      std::multimap< std::string, VariableManager* > iNameVMMap;

      /// The next free vnid to be allocated.
      unsigned_t iNextVNID;

      /// A number guaranteed to be at least the largest VNID
      unsigned_t iMaxVNID;

      /// A map of Variable names to their vnids.
      std::map< std::string, unsigned_t > iVariableNameVNIDMap;

      /// A vector mapping a vnid onto its Variable pointer (if appropriate).
      std::vector< Variable* > iVNIDVariableVector;

      /// A vector mapping a vnid onto its VariableNodeDirector pointer (if
      /// appropriate).
      std::vector< const VariableManager* > iVNIDVMVector;

      /// The next free msg id to be allocated.
      unsigned_t iNextMsgID;

      /// A vector mapping whether a vnid refers to a Node as well as
      /// a Variable
      std::vector< bool > iVNIDIsNodeVector;

      /// A map of Node names to their vnids.
      std::map< std::string, unsigned_t > iNodeNameVNIDMap;

      /// A vector mapping a vnid onto its Node pointer (if appropriate).
      std::vector< Node* > iVNIDNodeVector;

      /// A vector mapping a vnid onto its NodeFactory pointer (if
      /// appropriate).
      std::vector< const NodeFactory* > iVNIDNFVector;

      /// A list of pending NodeFactories which need to be registered
      std::list< NodeFactory* > iPendingNFList;

      /// A map of the names of NodeFactorys to the NodeFactory
      /// pointers themselves.
      std::map< std::string, NodeFactory* > iNameNFMap;

      /// A list of the variables with unresolved dependencies
      std::list< Variable* > iOrphanList;
    };
  }
}
#endif //ndef _VARIABLE_NODE_DIRECTOR_HH
