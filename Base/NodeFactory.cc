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
#include "MessageFunctionObject.hh"
#include "MessageStore.hh"
#include "Node.hh"
#include "NodeFactory.hh"
#include "PluginDirector.hh"
#include "Segment.hh"
#include "StringLiterals.hh"
#include "Variable.hh"
#include "VariableNodeDirector.hh"

NRS::Base::NodeFactory::
NodeFactory( std::string aName, std::string aDisplayName,
	     bool selfUpdating, bool stateHolding,
	     unsigned_t rootMinOccurs, unsigned_t rootMaxOccurs ) :
  iName( aName ), iDisplayName( aDisplayName ),
  iRootMinOccurs( rootMinOccurs ), iRootMaxOccurs( rootMaxOccurs ),
  iSelfUpdating( selfUpdating ), iStateHolding( stateHolding ),
  iRegistered( false )
{
  iIsRootNode = ((rootMinOccurs != 0) || (rootMaxOccurs != 0));
  VariableNodeDirector::getDirector().signupNodeFactory( this );
  iPlugin = PluginDirector::getDirector().getCurrentPlugin();
}

NRS::Base::NodeFactory::~NodeFactory()
{
  unRegister();
  for ( typeof(iAttributeMap.begin()) anIter = iAttributeMap.begin();
	    anIter != iAttributeMap.end(); ++anIter )
    {
      delete anIter->second.iDefaultValuePtr;
    }
  for ( typeof(iNodeContentList.begin()) anIter = iNodeContentList.begin();
	anIter != iNodeContentList.end(); ++anIter )
    {
      for ( typeof(anIter->iARMap.begin()) anIter2 =
	      anIter->iARMap.begin();
	    anIter2 != anIter->iARMap.end(); ++anIter2 )
	{
	  delete anIter2->second.iDefaultValuePtr;
	}
    }
  for ( typeof(iVariableMap.begin()) anIter = iVariableMap.begin();
	anIter != iVariableMap.end(); ++anIter )
    {
      for ( typeof(anIter->second.iMFOGenMap.begin()) anIter2 =
	      anIter->second.iMFOGenMap.begin();
	    anIter2 != anIter->second.iMFOGenMap.end(); ++anIter2 )
	{
	  delete anIter2->second;
	}
    }
}

void NRS::Base::NodeFactory::configureCSL()
{
  setDescription();
  setGroups();
  setAttributes();
  setContents();
  setVariables();
}

void NRS::Base::NodeFactory::doRegister()
{
  if (!iRegistered)
    {
      configureCSL();
      _TRY_( VariableNodeDirector::getDirector().
	     addNodeFactory( this ), _RETHROW_ );
      iRegistered = true;
    }
}

void NRS::Base::NodeFactory::unRegister()
{
  if (iRegistered)
    {
      _TRY_( VariableNodeDirector::getDirector().
	     removeNodeFactory( this ), _RETHROW_ );
      iRegistered = false;
    }
}

NRS::Base::NodeFactory::AttributeRestriction 
NRS::Base::NodeFactory::makeAR( bool changeDefaultValue,
				Segment *defaultValuePtr,
				bool restrictIsConst,
				bool restrictNoLiveChange )
						     
{
  AttributeRestriction anAR;
  
  anAR.iChangeDefaultValue = changeDefaultValue;
  if (changeDefaultValue)
    anAR.iDefaultValuePtr = defaultValuePtr;
  else
    anAR.iDefaultValuePtr = NULL;
  anAR.iRestrictIsConst = restrictIsConst;
  anAR.iRestrictNoLiveChange = restrictNoLiveChange;

  return anAR;
}

NRS::Base::NodeFactory::SegmentConstraint
NRS::Base::NodeFactory::makeSC( bool minValConstraint,
				double minVal,
				bool maxValConstraint,
				double maxVal )
{
  SegmentConstraint aSC;
  
  aSC.iMinValConstraint = minValConstraint;
  if (minValConstraint)
    aSC.iMinVal = minVal;

  aSC.iMaxValConstraint = maxValConstraint;
  if (maxValConstraint)

    aSC.iMaxVal = maxVal;

  return aSC;
}

NRS::Base::NodeFactory::InterfaceDescription
NRS::Base::NodeFactory::makeID( int minOccurs,
				int maxOccurs,
				bool autoConnect )
{
  InterfaceDescription anID;
  
  anID.iMinOccurs = minOccurs;
  anID.iMaxOccurs = maxOccurs;
  anID.iAutoConnect = autoConnect;

  return anID;
}

void NRS::Base::NodeFactory::addGroup( std::string aGroup,
				       std::string aDisplayName )
{
  if (aDisplayName.empty())
    aDisplayName = aGroup;
  iGroupMap[ aGroup ] = aDisplayName;
}

void NRS::Base::NodeFactory::addAttribute( std::string aName,
					   std::string aDisplayName,
					   std::string aMessageType,
					   bool isConst,
					   bool inNRSNamespace,
					   bool liveChange,
					   bool noSend,
					   bool hasDefault,
					   Segment *defaultValuePtr )
{
  AttributeDescription anAD;
  anAD.iName = aName;
  anAD.iDisplayName = aDisplayName;
  anAD.iMessageType = aMessageType;
  anAD.iIsConst = isConst;
  anAD.iNRSNamespace = inNRSNamespace;
  anAD.iHasDefault = hasDefault;
  if (hasDefault)
    anAD.iDefaultValuePtr = defaultValuePtr;
  else
    anAD.iDefaultValuePtr = NULL;
  anAD.iLiveChange = liveChange;
  anAD.iNoSend = noSend;
  iAttributeMap[ aName ] = anAD;
}

void NRS::Base::NodeFactory::addContent( std::string aNodeType, int minOccurs,
					 int maxOccurs,
					 std::map< std::string, 
					 AttributeRestriction > anARMap )
{
  NodeContent aNC;
  aNC.iNodeType = aNodeType;
  aNC.iMinOccurs = minOccurs;
  aNC.iMaxOccurs = maxOccurs;
  aNC.iARMap = anARMap;

  iNodeContentList.push_back( aNC );
}
void NRS::Base::NodeFactory::addVariable( std::string aName,
					  std::string aDisplayName,
					  std::string aMessageType,
					  bool selfUpdating,
					  bool stateHolding,
					  std::map< std::string, 
					  InterfaceDescription > anIDMap,
					  std::map< std::string,
					  std::string > aSegAttrRefMap,
					  std::map< std::string, 
					  SegmentConstraint > aSegConsMap,
					  std::map< std::string,
					  MFOGenerator* > aMFOGenMap,
					  bool variableMessageType,
					  std::string variableMessageTypeRef )
{
  VariableDescription aVD;

  aVD.iName = aName;
  aVD.iDisplayName = aDisplayName;
  aVD.iMessageType = aMessageType;
  aVD.iSelfUpdating = selfUpdating;
  aVD.iStateHolding = stateHolding;
  aVD.iVariableMessageType = variableMessageType;
  aVD.iVariableMessageTypeRef = variableMessageTypeRef;
  aVD.iIDMap = anIDMap;
  aVD.iSegAttrRefMap = aSegAttrRefMap;
  aVD.iSegConsMap = aSegConsMap;
  aVD.iMFOGenMap = aMFOGenMap;

  iVariableMap[ aName ] = aVD;
}

bool NRS::Base::NodeFactory::createNode( std::string nodeName )
{
  if (!doCreate( nodeName ))
    {
      _ERROR_( "Failed to create variables, not cleaning up" );
      return false;
    }

  VariableNodeDirector &theVND = VariableNodeDirector::getDirector();
  if (!theVND.hasNode( nodeName ))
    {
      _ERROR_( "No such node as " << nodeName );
      return false;
    }
  
  Variable *nodeVar = theVND.getVariable( nodeName );
  nodeVar->setSelfUpdating( iSelfUpdating );
  nodeVar->setStateHolding( iStateHolding );
  
  // Create the default message to send to the node
  MessageStore anMS( iAttributeMap.size() - 1, Enum::PML );
  unsigned_t segNum = 0;
  for ( typeof(iAttributeMap.begin()) anIter = iAttributeMap.begin();
	    anIter != iAttributeMap.end(); ++anIter )
    {
      // We ignore the "name" attribute
      if (anIter->first != "name" )
	{
	  if ((anIter->second.iHasDefault) &&
	      (anIter->second.iDefaultValuePtr != NULL))
	    {
	      anIter->second.iDefaultValuePtr->createSegment( anMS, segNum );
	    }
	  else
	    {
	      const SegmentDescription &aSD =
		theVND.getVariableManager( anIter->second.iMessageType ).
		getSegmentDescription( 0 );
	      std::auto_ptr< Segment > 
		ptr( theVND.getVariableManager( anIter->second.iMessageType ).
		     createSegment( aSD ) );
	      ptr->createSegment( anMS, segNum );
	    }
	  ++segNum;
	}
    }

  // Create all of the variables
  for ( typeof(iVariableMap.begin()) anIter = iVariableMap.begin();
	anIter != iVariableMap.end(); ++anIter )
    {
      std::string varName = nodeName + "." + anIter->first;
      if (!theVND.hasVariable( varName ))
	{ // A standard variable, so just create it
	  if (anIter->second.iSelfUpdating)
	    _ABORT_( "Autogenerating variable, " << varName
		     << ", which is self-updating" );
	  theVND.getVariableManager( anIter->second.iMessageType ).
	    createVariable( varName );
	}
      Variable *var = theVND.getVariable( varName );
      var->setSelfUpdating( anIter->second.iSelfUpdating );
      var->setStateHolding( anIter->second.iStateHolding );
    }

  // Now add in the internal linkages
  for ( typeof(iVariableMap.begin()) anIter = iVariableMap.begin();
	anIter != iVariableMap.end(); ++anIter )
    {
      std::string tgtName = nodeName + "." + anIter->first;
      Variable *tgtPtr = theVND.getVariable( tgtName );
      for ( typeof(anIter->second.iMFOGenMap.begin()) anIter2 =
	      anIter->second.iMFOGenMap.begin();
	    anIter2 != anIter->second.iMFOGenMap.end(); ++anIter2 )
	{
	  std::string srcName = nodeName + "." + anIter2->first;
	  MessageFunctionObject *aMFOPtr =
	    anIter2->second->createMFO( srcName, tgtPtr );
	  tgtPtr->addMFO( aMFOPtr );
	}
    }

  // Okay, now we should be able to resolve all the internal dependencies...
  theVND.resolveDependencies();

  // And now send the default setup to the Node
  nodeVar->getInputLink().receiveMessage( anMS );
  nodeVar->reset();

  return true;
}

std::ostream &NRS::Base::NodeFactory::getCSL( std::ostream& csl ) const
{
  csl << "<NodeDescription"
      << " name='" << iName << "'";
  if (!iDisplayName.empty())
    csl << " displayName='" << iDisplayName << "'";
  csl << " rootNode='"
      << std::boolalpha << iIsRootNode << "'";
  if (iIsRootNode)
    csl << " rootMinOccurs='" << iRootMinOccurs
	<< "' rootMaxOccurs='" << iRootMaxOccurs << "'";
  if (iDescription.empty() && iGroupMap.empty() && iAttributeMap.empty() &&
      iNodeContentList.empty() && iVariableMap.empty())
    {
      csl << "/>\n";
    }
  else
    {
      csl << ">\n";
      if (!iDescription.empty())
	{
	  csl << " <Description>\n"
	      << iDescription << "\n"
	      << " </Description>\n";
	}
      for ( typeof(iGroupMap.begin()) anIter = iGroupMap.begin();
	    anIter != iGroupMap.end(); ++anIter )
	{
	  csl << " <InGroup name='" << anIter->first;
	  if (!anIter->second.empty())
	    csl << "' displayName='" << anIter->second;
	  csl << "' />\n";
	}

      for ( typeof(iNodeContentList.begin()) anIter = iNodeContentList.begin();
	    anIter != iNodeContentList.end(); ++anIter )
	{
	  csl << " <Contains type='" << anIter->iNodeType 
	      << "' minOccurs='" << anIter->iMinOccurs;
	  if (!anIter->iMaxOccurs == -1)
	    csl << "' maxOccurs='unbounded";
	  else
	    csl << "' maxOccurs='" << anIter->iMaxOccurs;
	  if (anIter->iARMap.empty())
	    csl << "' />\n";
	  else
	    {
	      csl << "' >\n";
	      for ( typeof(anIter->iARMap.begin()) anIter2 =
		      anIter->iARMap.begin();
		    anIter2 != anIter->iARMap.end(); ++anIter2 )
		{
		  csl << " <Attribute name='" << anIter2->first;
		  if (anIter2->second.iChangeDefaultValue)
		    {
		      Segment *dv = anIter2->second.iDefaultValuePtr;
		      if (dv != NULL)
			{
			  MessageStore anMS( 1, Enum::PML );
			  dv->createSegment( anMS, 0 );
			  csl << "' defaultValue='" 
			      << (const char *) anMS.getSegment( 0 );
			  _DEBUG_( (const char *) anMS.getSegment( 0 ) );
			}
		    }
		  if (anIter2->second.iRestrictIsConst)
		    csl << "' isConst='true";
		  if (anIter2->second.iRestrictNoLiveChange)
		    csl << "' liveChange='false";
		  csl << "' />\n";
		}
	      csl << "</Contains>\n";
	    }
	}

      for ( typeof(iAttributeMap.begin()) anIter = iAttributeMap.begin();
	    anIter != iAttributeMap.end(); ++anIter )
	{
	  csl << " <Attribute name='" << anIter->first;
	  if (!anIter->second.iDisplayName.empty())
	    csl << "' displayName='" << anIter->second.iDisplayName;
	  csl << "' unitName='" << anIter->second.iMessageType
	      << "' isConst='" << std::boolalpha
	      << anIter->second.iIsConst
	      << "' inNRSNamespace='" << std::boolalpha
	      << anIter->second.iNRSNamespace;
	  if (anIter->second.iHasDefault)
	    {
	      Segment *dv = anIter->second.iDefaultValuePtr;
	      if (dv != NULL)
		{
		  MessageStore anMS( 1, Enum::PML );
		  dv->createSegment( anMS, 0 );
		  csl << "' defaultValue='" 
		      << (const char *) anMS.getSegment( 0 );
		}
	    }
	  csl << "' liveChange='" << std::boolalpha
	      << anIter->second.iLiveChange
	      << "' noSend='" << std::boolalpha
	      << anIter->second.iNoSend
	      << "' />\n";
	}

      for ( typeof(iVariableMap.begin()) anIter = iVariableMap.begin();
	    anIter != iVariableMap.end(); ++anIter )
	{
	  csl << " <Variable name='" << anIter->first;
	  if (!anIter->second.iDisplayName.empty())
	    csl << "' displayName='" << anIter->second.iDisplayName;
	  csl << "' messageType='" << anIter->second.iMessageType
	      << "' selfUpdating='"
	      << std::boolalpha << anIter->second.iSelfUpdating
	      << "' stateHolding='"
	      << std::boolalpha << anIter->second.iStateHolding;
// 	  csl << "' variableMessageType='"
// 	      << std::boolalpha << anIter->second.iVariableMessageType;
// 	  if (anIter->second.iVariableMessageType)
// 	    csl << "' variableMessageTypeRef='"
// 		<< anIter->second.iVariableMessageTypeRef;
	  if (anIter->second.iIDMap.empty() &&
	      anIter->second.iSegAttrRefMap.empty() &&
	      anIter->second.iSegConsMap.empty())
	    csl << "' />\n";
	  else
	    {
	      csl << "' >\n";
	      for ( typeof(anIter->second.iIDMap.begin()) anIter2 =
		      anIter->second.iIDMap.begin();
		    anIter2 != anIter->second.iIDMap.end(); ++anIter2 )
		{
		  csl << "<Interface direction='" << anIter2->first;
		  csl << "' minOccurs='" << anIter2->second.iMinOccurs;
		  if (anIter2->second.iMaxOccurs == -1)
		    csl << "' maxOccurs='unbounded";
		  else
		    csl << "' maxOccurs='" << anIter2->second.iMaxOccurs;
		  csl << "' autoConnect='"
		      << std::boolalpha << anIter2->second.iAutoConnect;
		  csl << "' />\n";
		}
	      for ( typeof(anIter->second.iSegAttrRefMap.begin()) anIter2 =
		      anIter->second.iSegAttrRefMap.begin();
		    anIter2 != anIter->second.iSegAttrRefMap.end();
		    ++anIter2 )
		{
		  csl << "<InitialValue segmentName='" << anIter2->first
		      << "' attributeReference='" << anIter2->second
		      << "' />\n";
		}
	      for ( typeof(anIter->second.iSegConsMap.begin()) anIter2 =
		      anIter->second.iSegConsMap.begin();
		    anIter2 != anIter->second.iSegConsMap.end();
		    ++anIter2 )
		{
		  if (anIter2->second.iMinValConstraint ||
		      anIter2->second.iMaxValConstraint )
		    {
		      csl << "<Constraint segmentName='" << anIter2->first;
		      if (anIter2->second.iMinValConstraint)
			{
			  csl << "' minVal='" << anIter2->second.iMinVal;
			}
		      if (anIter2->second.iMaxValConstraint)
			{
			  csl << "' maxVal='" << anIter2->second.iMaxVal;
			}
		      csl << "' />\n";
		    }
		}
	      csl << "</Variable>\n";
	    }
	}
      csl << "</NodeDescription>\n";
    }
  return csl;
}

NRS::Base::NodeFactory::VariableDescription
NRS::Base::NodeFactory::getVariableDescription( const std::string &aName )
  const
{
  if (iVariableMap.find( aName ) == iVariableMap.end())
    _ABORT_( "No such Variable as " << aName << "\n\t" );
  return iVariableMap.find( aName )->second;  
}

std::vector< std::string > NRS::Base::NodeFactory::getVariableNames() const
{
  std::vector< std::string > names;
  for ( typeof( iVariableMap.begin() ) anIter = iVariableMap.begin();
	anIter != iVariableMap.end(); anIter++ )
    names.push_back( anIter->first );
  return names;
}
