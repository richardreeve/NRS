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

#ifndef _NODE_FACTORY_HH
#define _NODE_FACTORY_HH

#pragma interface
#include <iostream>
#include <list>
#include <map>
#include <string>
#include <vector>

#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    class MFOGenerator;
    class Segment;

    /// This is the base class for Node Factorys.
    class NodeFactory
    {
    public:
      /// The description of the attributes for this node
      class AttributeDescription
      {
      public:
	/// Default Constructor
	AttributeDescription() : iDefaultValuePtr( NULL )
	{
	}

	/// Default destructor
	~AttributeDescription()
	{
	}

	/// Name of attribute
	std::string iName;
	/// Pretty print name of attribute for display
	std::string iDisplayName;
	/// Message type of attribute (must be a Unit or Type)
	std::string iMessageType;
	/// Is the value of this attribute fixed in advance
	bool iIsConst;
	/// Is the attribute name in the reserved NRS Attribute namespace
	bool iNRSNamespace;
	/// Is there a default value
	bool iHasDefault;
	/// What is the default value if it exists
	Segment* iDefaultValuePtr;
	/// Can the value of the attribute be changed at runtime
	bool iLiveChange;
	/// Should this attribute actually be sent?
	bool iNoSend;
      };

      /// The description of restrictions on the attributes for this
      /// node. This is used by nodes to restrict the subnodes which
      /// they can contain.
      class AttributeRestriction
      {
      public:
	/// Default constructor
	AttributeRestriction() : iDefaultValuePtr( NULL )
	{
	}

	/// Default destructor
	~AttributeRestriction()
	{
	}

	/// Is the attribute restricted to be const
	bool iRestrictIsConst;
	/// Is the attribute default value changed from original settings
	bool iChangeDefaultValue;
	/// Do we want to stop this attribute being set at runtime
	bool iRestrictNoLiveChange;
	/// If there is a default value, it will be here
	Segment* iDefaultValuePtr;
      };

      /// The description of one particular potential content of a node
      class NodeContent
      {
      public:
	/// Default constructor
	NodeContent()
	{
	}

	/// Default Destructor
	~NodeContent()
	{
	}

	/// Copy constructor
	NodeContent( const NodeContent &rhs )
	{
	  *this = rhs;
	}

	/// name of node type which can be contained here
	std::string iNodeType;
	/// Minimum number of node instances
	int iMinOccurs;
	/// Maximum number of node instances
	int iMaxOccurs;
	/// Restrictions on node attributes
	std::map< std::string, AttributeRestriction > iARMap;
      };

      /// Description of interface constraints for variables. How many
      /// input or output interfaces can ther be?
      class InterfaceDescription
      {
      public:
	/// How few variables can we have?
	int iMinOccurs;
	/// How many variables can we have (-1 is unbounded)
	int iMaxOccurs;
	/// Do we connect automatically?
	bool iAutoConnect;
      };

      /// Constraint on attribute value (floats only)
      class SegmentConstraint
      {
      public:
	/// Is there a minimum value for the attribute?
	bool iMinValConstraint;
	/// If so, what is the minimum value?
	float_t iMinVal;
	/// Is there a maximum value?
	bool iMaxValConstraint;
	/// If so, what is the maximum value?
	float_t iMaxVal;
      };

      /// The description of the attributes for this node
      class VariableDescription
      {
      public:
	/// Default constructor
	VariableDescription()
	{
	}

	/// Default Destructor
	~VariableDescription()
	{
	}

	/// Copy constructor
	VariableDescription( const VariableDescription &rhs )
	{
	  *this = rhs;
	}

	/// Name of variable
	std::string iName;
	/// Pretty version of variable name for display
	std::string iDisplayName;
	/// Type of message which the variable handles
	std::string iMessageType;
	/// Is the variable self-updating - ie does it do internal
	/// calculations, or does it just pass on the value it is set
	/// to (or receives)
	bool iSelfUpdating;
	/// When it is not transmitting a value, does it hold its
	/// current state, or is there no value (eg a spike)
	bool iStateHolding;
	/// Can the message type be a derivative of this type?
	bool iVariableMessageType;
	/// Which attribute holds the actual message type then?
	std::string iVariableMessageTypeRef;
	/// A map of InterfaceDescriptions telling how many interfaces
	/// there can be - possible interface types are in, out and log.
	std::map< std::string, InterfaceDescription > iIDMap;
	/// A map of variable segment names to attribute names
	std::map< std::string, std::string > iSegAttrRefMap;
	/// A map of segment names to their contraints
	std::map< std::string, SegmentConstraint > iSegConsMap;
	/// A map of node-local dependencies and the generator for the
	/// MessageFunctionObjects which will handle them
	std::map< std::string, MFOGenerator* > iMFOGenMap;
      };

      /// Constructor.
      /**
       *
       * \param aName The name of the type of Node being managed.
       * \param aDisplayName The prettified version of the node name
       * for display purposes (default is empty = reuse aName)
       * \param selfUpdating whether the node might change its own value
       * \param stateHolding whether the node keeps its state
       * \param rootMinOccurs minimum number of these objects at root
       * level
       * \param rootMaxOccurs maximum number of these objects at root
       * level
       *
       **/
      NodeFactory( std::string aName, std::string aDisplayName = "", 
		   bool selfUpdating = false, bool stateHolding = true,
		   unsigned_t rootMinOccurs = 0,
		   unsigned_t rootMaxOccurs = 0 );

      /// Destructor.
      virtual ~NodeFactory();

      /// Configure the internal records of the message type.
      /**
       *
       * This method configures the internal records of the factory so
       * that it can correctly output its CSL description on
       * request. This cannot conveniently be done at construction
       * time since too much work would be devolved to disparate
       * constructors.
       *
       **/
      void configureCSL();

      /// Register the NodeFactory with the VariableNodeDirector if not
      /// already registered.
      /**
       *
       * \exception Exception if a NodeFactory of that type is already
       * added.
       *
       **/
      void doRegister();
      
      /// Unregister the NodeFactory from the VariableNodeDirector.
      /**
       *
       * \exception Exception if the NodeFactory is not already added.
       *
       **/
      void unRegister();

      /// Get type of Node being managed.
      /**
       *
       * \return the name of the type of the node being managed.
       *
       **/
      const std::string &getType() const
      {
	return iName;
      }

      /// Is the NodeFactory for a root node?
      bool isRootNode() const
      {
	return iIsRootNode;
      }

      /// Create a default node
      /**
       *
       * Note that when you create a Node it is only necessary to
       * create the Node and the Variables and register the Variables
       * with the Node. Registration with the VariableNodeDirector
       * happens automatically, and the pointers can thus be discarded
       * after creation.
       *
       * \param nodeName VNName of node to be created
       *
       * \return whether the creation was successful
       *
       **/
      bool createNode( std::string nodeName );

      /// Get the CSL description of a Node type.
      /**
       *
       * \param csl the output stream to which the CSL description
       * should be output.
       *
       * \return the output stream to which the CSL description was
       * output.
       *
       **/
      std::ostream &getCSL( std::ostream& csl ) const;

      /// Get plugin
      /**
       *
       * \returns the plugin this NodeFactory is part of
       *
       **/
      std::string getPlugin()
      {
	return iPlugin;
      }

      /// Get list of variable names inside a Node
      /**
       *
       * Used by Node during construction of Variables
       *
       * \return vector of names
       *
       **/
      std::vector< std::string >
      NRS::Base::NodeFactory::getVariableNames() const;


      /// Get VariableDescription for a specific variable
      /**
       *
       * \param aName name of variable to look for
       *
       * \return copy of VariableDescription for that variable
       *
       **/
      VariableDescription
      getVariableDescription( const std::string &aName ) const;

      /// Get NodeContent list
      /**
       *
       * \return copy of NodeContent list
       *
       **/
      std::list< NodeContent > getNodeContents() const
      {
	return iNodeContentList;
      }

      /// Get Group map
      /**
       *
       * \return copy of Group map
       *
       **/
      std::map< std::string, std::string > getGroupMap() const
      {
	return iGroupMap;
      }
    protected:
      /// Virtual method to set the description of the node for the
      /// CSL description.
      virtual void setDescription() = 0;

      /// Virtual method to set what group the node is in for the CSL.
      virtual void setGroups() = 0;

      /// Virtual method to describe the variables which will be
      /// contained in the node
      virtual void setVariables() = 0;

      /// Virtual method to describe the expected attributes
      virtual void setAttributes() = 0;

      /// Virtual method to describe content description for the node.
      virtual void setContents() = 0;

      /// Create the variables and node for a node
      /**
       *
       * Any non-standard variables, which is to say, any
       * self-updating variables in general, have to be created by the
       * factory itself - they are created here, which is called by
       * createNode before configuring them. Any variables not created
       * here are created in createNode. Variables and Nodes are just
       * created using new, there is no need to do anything with the
       * pointers generated as they register themselves automatically
       * with the VariableNodeDirector.
       *
       * \param nodeName node of name to create
       *
       * \return success of operation
       *
       **/
      virtual bool doCreate( const std::string &nodeName ) const = 0;

      /// Method to make AttributeRestriction struct
      /**
       *
       * \param changeDefaultValue new default value
       * \param defaultValuePtr new default value for this attribute
       * \param restrictIsConst value is now const
       * \param restrictNoLiveChange value cannot be changed at runtime
       *
       **/
      AttributeRestriction makeAR( bool changeDefaultValue = false,
				   Segment *defaultValuePtr = NULL,
				   bool restrictIsConst = false,
				   bool restrictNoLiveChange = false );

      /// Method to make a SegmentConstraint struct
      /**
       *
       * \param minValConstraint Is there a minimum value constraint
       * \param minVal the minimum value constraint
       * \param maxValConstraint Is there a maximum value constraint
       * \param maxVal the maximum value constraint
       *
       **/
      SegmentConstraint makeSC( bool minValConstraint = false,
				double minVal = 0,
				bool maxValConstraint = false,
				double maxVal = 0 );

      /// Method to make an InterfaceDescription struct
      /**
       *
       * \param minOccurs minimum number of occurences of interface
       * \param maxOccurs maximum number of occurences of interface
       * (-1 for unbounded)
       *
       **/
      InterfaceDescription makeID( int minOccurs, int maxOccurs,
				   bool autoConnect );

      /// method to set a group the node is in for the CSL.
      /**
       *
       * \param aGroup the name of the group
       * \param aDisplayName the prettified name for the group
       *
       **/
      void addGroup( std::string aGroup, std::string aDisplayName );

      /// method to add an attribute to the list for the CSL
      /**
       *
       * \param aName name of the attribute
       * \param aDisplayName prettified name of the attribute for display
       * \param aMessageType name of the messageType of the attribute
       * \param isConst whether this value is fixed in advance to a value
       * \param inNRSNamespace whether this attribute is in the NRS namespace
       * \param liveChange whether this value can be changed at runtime
       * \param noSend whether this attribute should actually be
       * sent in any message - in practice this is usually only used
       * by the name attribute
       * \param hasDefault whether the attribute has a default value
       * \param defaultValuePtr default value for this attribute (or NULL)
       *
       **/
      void addAttribute( std::string aName, std::string aDisplayName,
			 std::string aMessageType,
			 bool isConst, bool inNRSNamespace,
			 bool liveChange, bool noSend,
			 bool hasDefault,
			 Segment *defaultValuePtr = NULL );



      /// method to add content description to node for CSL
      /**
       *
       * \param aNodeType The type of the node to be allowed
       * \param minOccurs minimum number of occurences of node
       * \param maxOccurs maximum number of occurences of nodes (-1
       * for unbounded)
       * \param anARMap a map of restrictions on the attributes of the node
       *
       **/
      void addContent( std::string aNodeType, int minOccurs, int maxOccurs,
		       std::map< std::string, AttributeRestriction > anARMap );

      /// method to add variable description to node for CSL
      /**
       *
       * \param aName name of the variable
       * \param aDisplayName prettified name of the variable for display
       * \param aMessageType name of the messageType of the attribute
       * \param selfUpdating whether the variable might change its own value
       * \param stateHolding whether the variable keeps its state
       * (true) or has a value only when a message is sent (e.g. a
       * spike) (false)
       * \param variableMessageType can the variable be a derived type
       * of this variable type, or is it fixed
       * \param variableMessageTypeRef the attribute in which the type
       * of the variable is stored if variableMessageType is true
       * \param floatNotInt The type of numbers the node uses. Options
       * are floating point (true, default) or integral
       * (false). Floating point variables should also be able to
       * handle integers, but not vice-versa (optional).
       * \param anIDMap a map of the Interfaces available to the
       * variable, referenced by interface name
       * \param aSegAttrRefMap a map of the attributes in the the
       * initial values of the segments are stored, referenced by
       * segment name
       * \param aSegConsMap a map of the constraints on numerical
       * segments, referenced by segment name
       * \param aMFOGenMap a map of node-local dependencies, with the
       * name of the variable to connect to this one and the generator
       * for the MessageFunctionObject to handle this connection.
       **/
      void addVariable( std::string aName, std::string aDisplayName,
			std::string aMessageType, bool selfUpdating,
			bool stateHolding,
			std::map< std::string, InterfaceDescription > anIDMap,
			std::map< std::string, std::string > aSegAttrRefMap,
			std::map< std::string, SegmentConstraint > aSegConsMap,
			std::map< std::string, MFOGenerator* > aMFOGenMap,
			bool variableMessageType = false,
			std::string variableMessageTypeRef = "" );

      /// The type of the Node being managed.
      std::string iName;

      /// The prettified version of the name of the Node being managed.
      std::string iDisplayName;

      /// The description of the Node type.
      std::string iDescription;

      /// bool representing whether the node can be a root node (at the
      /// top level of the network tree).
      bool iIsRootNode;

      /// minimum number of these at root level
      unsigned_t iRootMinOccurs;

      /// Maximum number of these at root level
      unsigned_t iRootMaxOccurs;

      /// Is the node itself self-updating? (almost inevitably not)
      bool iSelfUpdating;

      /// Is the node itself state-holding? (yes if there are attributes)
      bool iStateHolding;

      /// The group(s) the node type falls into, if any, with their
      /// prettified display names.
      std::map< std::string, std::string > iGroupMap;

      /// The attributes the node has, along with the description of them.
      std::map< std::string, AttributeDescription > iAttributeMap;

      /// The nodes which the node can contain, along with any
      /// restrictions on them.
      std::list< NodeContent > iNodeContentList;

      /// The variables which the node contains.
      std::map< std::string, VariableDescription > iVariableMap;

      /// The name of the plugin the NodeFactory is part of
      std::string iPlugin;

      /// bool representing whether the factory is registered with the
      /// VariableNodeDirector.
      bool iRegistered;
    };     
  }
}
#endif //ndef _NODE_FACTORY_HH
