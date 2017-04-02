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

#include <dlfcn.h>
#include <map>
#include <xercesc/sax/SAXParseException.hpp>
#include <xercesc/util/PlatformUtils.hpp>

#include "CSLParser.hh"
#include "Exception.hh"
#include "Executive.hh"
#include "ExternalInterface.hh"
#include "ExternalInterfaceDirector.hh"
#include "IntegerSegment.hh"
#include "PMLParser.hh"
#include "RouteSegment.hh"
#include "StringLiterals.hh"
#include "StringSegment.hh"
#include "Target.hh"
#include "Variable.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"

namespace NRS
{
  namespace Interface
  {
    static void *sXercesHandle;
    
    __attribute__ ((constructor)) void sax_init()
    {
      sXercesHandle = dlopen( "libxerces-c.so", RTLD_NOW | RTLD_GLOBAL );
      if (!sXercesHandle)
	{
      sXercesHandle = dlopen( "libxerces-c.dylib", RTLD_NOW | RTLD_GLOBAL );
      if (!sXercesHandle)
	{
	  _ABORT_( "xerces-c library not found" );
	}
  }
      // XERCES_CPP_NAMESPACE::XMLPlatformUtils::Initialize();
    }
    
    __attribute__ ((destructor)) void sax_exit()
    {
      XERCES_CPP_NAMESPACE::XMLPlatformUtils::Terminate();
      dlclose( sXercesHandle );
    }
  }
}

NRS::Interface::PMLParser::PMLParser( std::string aName ) :
  XERCES_CPP_NAMESPACE::SAXParser(),
  XERCES_CPP_NAMESPACE::ErrorHandler(),
  MessageParser(), iMBIS( NULL ),
  iParserName( aName ), iInMessages( false ), iInContents( false ),
  iTargetPtr( NULL), iISPtr( NULL ), iMSPtr( NULL ),
  iInContentSegment( false ), iSegNum( 0 ), iEltDepth( 0 ),
  iDiscard( false )
{
  setDoNamespaces( true );
  //setDoValidation( false );
  setDoSchema( false );
  setExitOnFirstFatalError( true );
  installAdvDocHandler( this );
  setErrorHandler( this );
}

NRS::Interface::PMLParser::~PMLParser()
{
  delete iMBIS;
}

bool NRS::Interface::PMLParser::parseNew( uint8_t *aBuffer )
{
  _DEBUG_( "New buffer to parse on port " << iPort << " of length "
	   << strlen( (const char *) aBuffer ) << ", and first byte "
	   << *(const char *)aBuffer );
  iMBIS = new XERCES_CPP_NAMESPACE::
    MemBufInputSource( (XMLByte*) aBuffer,
		       strlen( (const char *) aBuffer ),
		       iParserName.c_str(), false );
  if (!parseFirst( *iMBIS, iXPST ))
    {
      delete iMBIS;
      iMBIS = NULL;
      return false;
    }
  if (!parseNext( iXPST ))
    {
      delete iMBIS;
      iMBIS = NULL;
      return false;
    }
  return true;
}

bool NRS::Interface::PMLParser::parseAgain()
{
  if (!parseNext( iXPST ))
    {
      delete iMBIS;
      iMBIS = NULL;
      return false;
    }
  return true;
}

void NRS::Interface::PMLParser::startDocument()
{
}

void NRS::Interface::PMLParser::endDocument()
{
}

void NRS::Interface::PMLParser::resetDocument()
{
}

void NRS::Interface::PMLParser::startElement( const XERCES_CPP_NAMESPACE::
					      XMLElementDecl &elemDecl,
					      const unsigned int urlId,
					      const XMLCh * const elemPrefix,
					      const XERCES_CPP_NAMESPACE::
					      RefVectorOf< 
					      XERCES_CPP_NAMESPACE::
					      XMLAttr > &attrList,
					      const unsigned int attrCount,
					      const bool isEmpty,
					      const bool isRoot )
{
  char *eltName =
    XERCES_CPP_NAMESPACE::XMLString::transcode( elemDecl.getBaseName() );
  Type::StringSegment cidS;
  Type::RouteSegment routeS;
  Type::IntegerSegment vnidS;
  vnidS.iValue = 0;
  
  std::map< std::string, std::string > attrMap;

#ifdef DEBUG
  std::stringstream anSS;
  anSS << "<" << eltName << " ";
#endif

  _DEBUG_( "Starting " << anSS.str() << "[from port " << iPort << "]" );

  if (isRoot)
    { // must be a Messages element or a message
      // So take intelligent segments and route
      // and put into iTargetPtr and iISPtr

      // This information only ever occurs at root level...
      
      // Collect attributes
      iISPtr.reset();
      iMSPtr.reset();
      iTargetPtr.reset();
      iDiscard = false;
      _ASSERT_( iEltDepth == 0, "Lost track of depth of XML" );

      for ( unsigned int i = 0; i < attrCount; ++i )
	{
	  const XERCES_CPP_NAMESPACE::XMLAttr *attr = attrList.elementAt( i );
	  char *anURIName = XERCES_CPP_NAMESPACE::
	    XMLString::transcode( getURIText( attr->getURIId() ) );
	  char *anAttName = XERCES_CPP_NAMESPACE::
	    XMLString::transcode( attr->getName() );
	  char *anAttValue = XERCES_CPP_NAMESPACE::
	    XMLString::transcode( attr->getValue() );

	  if (!strcmp( Literals::XML::ATT_NAMESPACE(), anURIName))
	    { // NRS specfic attributes
#ifdef DEBUG
	      anSS << "nrsa:" << anAttName << "='" << anAttValue << "' ";
#endif
	      if (!strcmp( anAttName, Literals::XML::ROUTE() ) )
		{ // it's the route...
		  routeS.useRawData( (const uint8_t *)anAttValue,
				     Enum::PML );
		}
	      else if (!strcmp( anAttName, Literals::XML::TOVNID() ) )
		{ // it's the vnid of the target
		  vnidS.useRawData( (const uint8_t *)anAttValue,
				    Enum::PML );
		}
	      else
		{ // Intelligent attribute
		  Enum::IntelligentType it = 
		    Literals::XML::getIntelligentType( anAttName );
		  if (it == Enum::InvalidIType)
		    {
		      _ERROR_( "Junk attribute: " << anAttName );
		    }
		  else
		    {
		      // Create IntelligentStore if necessary
		      if (!iISPtr.get())
			iISPtr.reset( new
				      Base::IntelligentStore( Enum::PML ) );

		      iISPtr->setSegment( it,
					  (const uint8_t *) anAttValue,
					  segLength( anAttValue ) );

		      if (it == Enum::TargetCID)
			{
			  cidS.useRawData( (const uint8_t *)anAttValue,
					   Enum::PML );
			}
		    }
		}
	    }
	  else 
	    { // normal PML attributes in another, and ignore the rest
#ifdef DEBUG
	      anSS << anAttName << "='" << anAttValue << "' ";
#endif
	      if ((*anURIName==0) && (strcmp( anAttName, "xmlns" )))
		attrMap[ anAttName ] = anAttValue;
	    }

	  XERCES_CPP_NAMESPACE::XMLString::release( &anURIName );
	  XERCES_CPP_NAMESPACE::XMLString::release( &anAttName );
	  XERCES_CPP_NAMESPACE::XMLString::release( &anAttValue );
	}
#ifdef DEBUG
      anSS << ">" << std::endl;
      _DEBUG_( "Received " << anSS.str() << "[from port " << iPort << "]" );
#endif
      // Did we get any intelligent segments?
      if (iISPtr.get())
	{ // Yes so check for rubbish and look for hop count and return route

	  // Check for illegal combinations
	  _ASSERT_( (iISPtr->hasSegment( Enum::IsBroadcast ) &&
		     iISPtr->hasSegment( Enum::HopCount ) &&
		     iISPtr->hasSegment( Enum::TargetCID )) ||
		    (!iISPtr->hasSegment( Enum::IsBroadcast ) &&
		     !iISPtr->hasSegment( Enum::TargetCID )),
		    "Must be broadcast with CID or non-broadcast without" );
	  
	  _ASSERT_( (iISPtr->hasSegment( Enum::AckMsg ) &&
		     iISPtr->hasSegment( Enum::AckVNID ) &&
		     iISPtr->hasSegment( Enum::MsgID ) &&
		     (iISPtr->hasSegment( Enum::SourceCID ) ||
		      iISPtr->hasSegment( Enum::ReturnRoute ) ) ) ||
		    (!iISPtr->hasSegment( Enum::AckMsg ) &&
		     !iISPtr->hasSegment( Enum::AckVNID )),
		    "Must be asking for an acknowledge with data to return" );
	  
	  _ASSERT_( (iISPtr->hasSegment( Enum::FailedRouteMsg ) &&
		     iISPtr->hasSegment( Enum::FailedRouteVNID ) &&
		     iISPtr->hasSegment( Enum::MsgID ) &&
		     (iISPtr->hasSegment( Enum::SourceCID ) ||
		      iISPtr->hasSegment( Enum::ReturnRoute ) ) ) ||
		    (!iISPtr->hasSegment( Enum::FailedRouteMsg ) &&
		     !iISPtr->hasSegment( Enum::FailedRouteVNID )),
		    "Must be asking for failed route info w/data to return" );
      
	  // If there's a hop count, decrement it
	  if (iISPtr->hasSegment( Enum::HopCount ))
	    {
	      Type::IntegerSegment hopS;
	      hopS.useData( iISPtr->getMessageStore(),
			    (uint8_t) Enum::HopCount );
	      hopS.iValue--;
	      hopS.createSegment( iISPtr->getMessageStore(),
				  (uint8_t) Enum::HopCount );
	    }
	  
	  // If there's a return route extend it
	  if (iISPtr->hasSegment( Enum::ReturnRoute ))
	    {
	      Type::RouteSegment rs;
	      rs.useData( iISPtr->getMessageStore(),
			  (uint8_t) Enum::ReturnRoute );
	      routePushPortFront( rs );
	      rs.createSegment( iISPtr->getMessageStore(),
				(uint8_t) Enum::ReturnRoute );
	    }
	}
      // Okay, we've extracted all the information from the root node,
      // now let's find out what we had!
      if ( !strcmp( eltName, Literals::XML::MESSAGES() ) )
	{ // Outer bracket, so set up what we can, and stop...
	  iTargetPtr.reset( new Base::Target( cidS.iValue, routeS.iValue,
					      vnidS.iValue, NULL ) );
	  if (!attrMap.empty())
	    _ERROR_( "Unwanted attribute in Messages element" );
	  iInMessages = true;
	}
      else
	{
	  Base::VariableNodeDirector &theVND =
	    Base::VariableNodeDirector::getDirector();
	  if (theVND.hasVariableManager( eltName ))
	    { // Okay we have a VariableManager, so we can do this properly
	      const Base::MessageDescription &aMD =
		theVND.getVariableManager( eltName ).getMessageDescription();
		
	      iTargetPtr.reset( new Base::Target( cidS.iValue, routeS.iValue,
						  vnidS.iValue, &aMD ) );
	      iMSPtr.reset( new Base::MessageStore( aMD.iSegmentDescription.
						    size(), Enum::PML ) );
	      for ( unsigned_t segNum = 0; segNum < iMSPtr->getNumSegments();
		    ++segNum )
		{
		  if (!aMD.iSegmentDescription[segNum]->isInContents())
		    {
		      if (attrMap.find( aMD.iSegmentDescription[segNum]->
					getAttributeName() ) != attrMap.end())
			{
			  std::string &val =
			    attrMap[ aMD.iSegmentDescription[segNum]->
				     getAttributeName() ];
			  iMSPtr->setSegment( segNum,
					      (const uint8_t *) val.c_str(),
					      segLength( val.c_str() ) );
			  attrMap.
			    erase( attrMap.find( aMD.
						 iSegmentDescription[segNum]->
						 getAttributeName() ) );
			}
		      else
			{
			  _ERROR_( "Missing segment: " 
				   << aMD.iSegmentDescription[segNum]->
				   getAttributeName() );
			  iDiscard = true;
			}
		    }
		}
	      for ( std::map< std::string, std::string >::iterator iter = 
		      attrMap.begin(); iter != attrMap.end(); ++iter )
		_WARN_( "Unknown segment [" << iter->first << "]='"
			<< iter->second << "'\n\t" );
	    }
	  else
	    { // Try to build a VariableManager
	      Base::CSLParser cslP( eltName );
	      Base::VariableManager *vmPtr = cslP.createManager();
	      if (vmPtr != NULL)
		{ // Success, so use it!
		  const Base::MessageDescription &aMD =
		    vmPtr->getMessageDescription();
		  
		  iTargetPtr.reset( new Base::Target( cidS.iValue,
						      routeS.iValue,
						      vnidS.iValue, &aMD ) );
		  iMSPtr.reset( new Base::MessageStore( aMD.
							iSegmentDescription.
							size(), Enum::PML ) );
		  for ( unsigned_t segNum = 0;
			segNum < iMSPtr->getNumSegments();
			++segNum )
		    {
		      if (!aMD.iSegmentDescription[segNum]->isInContents())
			{
			  if (attrMap.find( aMD.iSegmentDescription[segNum]->
					    getAttributeName() ) !=
			      attrMap.end())
			    {
			      std::string &val =
				attrMap[ aMD.iSegmentDescription[segNum]->
					 getAttributeName() ];
			      iMSPtr->setSegment( segNum,
						  (const uint8_t *) val.
						  c_str(),
						  segLength( val.c_str() ) );
			      attrMap.
				erase( attrMap.
				       find( aMD.iSegmentDescription[segNum]->
					     getAttributeName() ) );
			    }
			  else
			    {
			      _ERROR_( "Missing segment: " 
				       << aMD.iSegmentDescription[segNum]->
				       getAttributeName() );
			      iDiscard = true;
			    }
			}
		    }
		  for ( std::map< std::string, std::string >::iterator iter = 
			  attrMap.begin(); iter != attrMap.end(); ++iter )
		    _WARN_( "Unknown segment [" << iter->first << "]='"
			    << iter->second << "'\n\t" );
		}
	      else
		{ // No VariableManager, so shove in element, then
		  // name-value pairs
		  _WARN_( "Don't know the Variable type: " << eltName );
		  iTargetPtr.reset( new Base::Target( cidS.iValue,
						      routeS.iValue,
						      vnidS.iValue, NULL ) );
		  iMSPtr.reset( new Base::MessageStore( attrMap.size() * 2 + 1,
							Enum::PML ) );
		  iMSPtr->setSegment( 0, (const uint8_t *) eltName,
				      segLength( eltName ) );
		  unsigned_t segNum = 1;
		  for ( typeof( attrMap.begin() ) iter = attrMap.begin();
			iter != attrMap.end(); ++iter )
		    {
		      iMSPtr->setSegment( segNum++,
					  (const uint8_t *) iter->first.
					  c_str(),
					  segLength( iter->first.c_str() ) );
		      iMSPtr->setSegment( segNum++,
					  (const uint8_t *) iter->second.
					  c_str(),
					  segLength( iter->second.c_str() ) );
		    }
		}
	    }
	  iInContents = true;
	}
    }
  else
    { // inside a message - either in Messages or in contents
      if (!iInContents)
	{ // Message inside a Messages envelope
	  for ( unsigned int i = 0; i < attrCount; ++i )
	    {
	      const XERCES_CPP_NAMESPACE::XMLAttr *attr =
		attrList.elementAt( i );
	      char *anURIName = XERCES_CPP_NAMESPACE::
		XMLString::transcode( getURIText( attr->getURIId() ) );
	      char *anAttName = XERCES_CPP_NAMESPACE::
		XMLString::transcode( attr->getName() );
	      char *anAttValue = XERCES_CPP_NAMESPACE::
		XMLString::transcode( attr->getValue() );

	      if (!strcmp( Literals::XML::ATT_NAMESPACE(), anURIName))
		{ // NRS specfic attributes
		  _ERROR_( "Junk special attribute: " << anAttName );
		}
	      else 
		{ // normal PML attributes in another, and ignore the rest
		  if ((*anURIName==0) && (strcmp( anAttName, "xmlns" )))
		    attrMap[ anAttName ] = anAttValue;
		}
	      
	      XERCES_CPP_NAMESPACE::XMLString::release( &anURIName );
	      XERCES_CPP_NAMESPACE::XMLString::release( &anAttName );
	      XERCES_CPP_NAMESPACE::XMLString::release( &anAttValue );
	    }

	  Base::VariableNodeDirector &theVND =
	    Base::VariableNodeDirector::getDirector();
	  if (theVND.hasVariableManager( eltName ))
	    { // Okay we have a VariableManager, so we can do this properly
	      const Base::MessageDescription &aMD =
		theVND.getVariableManager( eltName ).getMessageDescription();
		
	      iTargetPtr.reset( new Base::Target( cidS.iValue, routeS.iValue,
						  vnidS.iValue, &aMD ) );
	      iMSPtr.reset( new Base::MessageStore( aMD.iSegmentDescription.
						    size(), Enum::PML ) );
	      for ( unsigned_t segNum = 0; segNum < iMSPtr->getNumSegments();
		    ++segNum )
		{
		  if (!aMD.iSegmentDescription[segNum]->isInContents())
		    {
		      if (attrMap.find( aMD.iSegmentDescription[segNum]->
					getAttributeName() ) != attrMap.end())
			{
			  std::string &val =
			    attrMap[ aMD.iSegmentDescription[segNum]->
				     getAttributeName() ];
			  iMSPtr->setSegment( segNum,
					      (const uint8_t *) val.c_str(),
					      segLength( val.c_str() ) );
			}
		      else
			{
			  _ERROR_( "Missing segment: " 
				   << aMD.iSegmentDescription[segNum]->
				   getAttributeName() );
			}
		    }
		}
	    }
	  else
	    { // No VariableManager, so shove in element, then name-value pairs
	      _TODO_( "Create translation managers from CSL" );
	      iTargetPtr.reset( new Base::Target( cidS.iValue, routeS.iValue,
						  vnidS.iValue, NULL ) );
	      iMSPtr.reset( new Base::MessageStore( attrMap.size() * 2 + 1,
						    Enum::PML ) );
	      iMSPtr->setSegment( 0, (const uint8_t *) eltName,
				  segLength( eltName ) );
	      unsigned_t segNum = 1;
	      for ( typeof( attrMap.begin() ) iter = attrMap.begin();
		    iter != attrMap.end(); ++iter )
		{
		  iMSPtr->setSegment( segNum++,
				      (const uint8_t *) iter->first.c_str(),
				      segLength( iter->first.c_str() ) );
		  iMSPtr->setSegment( segNum++,
				      (const uint8_t *) iter->second.c_str(),
				      segLength( iter->second.c_str() ) );
		}
	    }
	  iInContents = true;
	}
      else // inside contents
	{ // Do we know which segment this is?
	  if (!iInContentSegment) // No
	    { // Okay, find which segment, and then put it in
	      _ASSERT_( iTargetPtr.get() != NULL, "Target not set" );
	      iInContentSegment = true;
	      if (iTargetPtr->getMessageDescriptionPtr() != NULL)
		{
		  const Base::MessageDescription &aMD =
		    *iTargetPtr->getMessageDescriptionPtr();
		  bool found = false;
		  for ( unsigned_t segNum = 0;
			segNum < aMD.iSegmentDescription.size();
			++segNum )
		    {
		      if ( aMD.iSegmentDescription[ segNum ]->
			   getAttributeName() == eltName )
			{
			  iSegNum = segNum;
			  found = true;
			}
		    }
		  if (!found)
		    {
		      _ERROR_( "Junk segment: " << eltName );
		      iDiscard = true;
		    }
		}
	      else
		{ // No VM, so stick it on the end...
		  std::auto_ptr< Base::MessageStore > oldMSPtr( iMSPtr );
		  iMSPtr.reset( new Base::MessageStore( oldMSPtr->
							getNumSegments() +
							2, Enum::PML ) );
		  unsigned_t segNum;
		  for ( segNum = 0;
			segNum < oldMSPtr->getNumSegments(); ++segNum )
		    {
		      iMSPtr->setSegment( segNum,
					  oldMSPtr->getSegment( segNum ),
					  segLength( oldMSPtr->
						     getSegment( segNum ) ) );
		    }
		  iMSPtr->setSegment( segNum, (const uint8_t *) "<", 2 );
		  iMSPtr->appendSegment( segNum, (const uint8_t *) eltName,
					 segLength( eltName ) );
		  iSegNum = segNum + 1;
		}
	    }
		  
	  if (!iDiscard)
	    { // We are inside a particular segment, so copy it in
	      data_t data = iMSPtr->getSegment( iSegNum );
	      data.push_back( '<' );
	      data += (const uint8_t *) eltName;
	      data.push_back( ' ' );
	      
	      for ( unsigned int i = 0; i < attrCount; ++i )
		{
		  const XERCES_CPP_NAMESPACE::XMLAttr *attr =
		    attrList.elementAt( i );
		  char *anURIName = XERCES_CPP_NAMESPACE::
		    XMLString::transcode( getURIText( attr->getURIId() ) );
		  char *anAttName = XERCES_CPP_NAMESPACE::
		    XMLString::transcode( attr->getName() );
		  char *anAttValue = XERCES_CPP_NAMESPACE::
		    XMLString::transcode( attr->getValue() );
		  
		  if (!strcmp( Literals::XML::ATT_NAMESPACE(), anURIName))
		    { // NRS specfic attributes
		      _ERROR_( "Junk attribute: " << anAttName );
		    }
		  else 
		    { // normal attributes in another
		      if (*anURIName==0)
			{
			  data += (const uint8_t *) anAttName;
			  data.push_back( '=' );
			  data.push_back( '"' );
			  data += (const uint8_t *) anAttValue;
			  data.push_back( '"' );
			  data.push_back( ' ' );
			}
		      else // report the rest
			_ERROR_( "Junk attribute: " << anAttName );
		    }
		  
		  XERCES_CPP_NAMESPACE::XMLString::release( &anURIName );
		  XERCES_CPP_NAMESPACE::XMLString::release( &anAttName );
		  XERCES_CPP_NAMESPACE::XMLString::release( &anAttValue );
		}
	      data.push_back( '>' );
	      data.push_back( '\n' );
	      iMSPtr->setSegment( iSegNum,
				  (const uint8_t *) data.c_str(),
				  data.size() );
	    }
	}
    }
  ++iEltDepth;
  XERCES_CPP_NAMESPACE::XMLString::release( &eltName );
}

void NRS::Interface::PMLParser::endElement( const XERCES_CPP_NAMESPACE::
					    XMLElementDecl &elemDecl,
					    const unsigned int urlId,
					    const bool isRoot,
					    const XMLCh* const elemPrefix )
{
  char *eltName =
    XERCES_CPP_NAMESPACE::XMLString::transcode( elemDecl.getBaseName() );
  --iEltDepth;

  _ASSERT_( iEltDepth >= 0, "Lost track of depth!" );

  if (iInContentSegment)
    {
      if (!iDiscard)
	{
	  data_t data = iMSPtr->getSegment( iSegNum );
	  data.push_back( '<' );
	  data.push_back( '/' );
	  data += (const uint8_t *) eltName;
	  data.push_back( '>' );
	  data.push_back( '\n' );
	  iMSPtr->setSegment( iSegNum, data.c_str(), data.size() );
	}
      if ((iInMessages && (iEltDepth == 2)) ||
	  ((!iInMessages) && (iEltDepth == 1)))
	{
	  iInContentSegment = false;
	}
    }
  else if (iInContents)
    {
      if ((!iInMessages && (iEltDepth > 1)) ||
	  (iInMessages && (iEltDepth > 2)))
	_ABORT_( "In content, endElement:" << eltName );
      iInContents = false;

      if (!iDiscard)
	{
	  bool acknowledge = false;
	  bool failedRoute = false;
	  Base::Executive &theE = Base::Executive::getExecutive();
	  Base::VariableNodeDirector &theVND =
	    Base::VariableNodeDirector::getDirector();
	  Base::ExternalInterfaceDirector &theEID =
	    Base::ExternalInterfaceDirector::getDirector();
	  _ASSERT_( iTargetPtr.get() != NULL, "Target has disappeared!" );
	  if (iISPtr.get() != NULL)
	    {
	      // If there's a failed route instruction, note it
	      if (iISPtr->hasSegment( Enum::FailedRouteMsg ) &&
		  iISPtr->hasSegment( Enum::FailedRouteVNID ) &&
		  iISPtr->hasSegment( Enum::MsgID ))
		{
		  failedRoute = true;
		}
	      
	      // If there's a acknowledge instruction, note it
	      if (iISPtr->hasSegment( Enum::AckMsg ) &&
		  iISPtr->hasSegment( Enum::AckVNID ) &&
		  iISPtr->hasSegment( Enum::MsgID ))
		{
		  acknowledge = true;
		}
	      if (iISPtr->hasSegment( Enum::HopCount ))
		{
		  Type::IntegerSegment hopS;
		  hopS.useData( iISPtr->getMessageStore(),
				(uint8_t) Enum::HopCount );
		  if (hopS.iValue < 0)
		    {
		      iDiscard = true;
		    }
		}
	    }
	  if (iTargetPtr->getRoute() != getEmptyRoute())
	    { // simple transfer
	      if (!iDiscard)
		{
		  theEID.getExternalInterface( iTargetPtr->getPort() )->
		    sendMessage( *iTargetPtr, *iMSPtr, iISPtr.get() );
		}
	      else
		{
		  if (failedRoute)
		    {
		      _TODO_( "Note failure" );
		    }
		}
	    }
	  else if (iTargetPtr->getCID().empty() ||
		   (iTargetPtr->getCID() == theE.getCID()))
	    { // arrived!
	      if (acknowledge)
		{
		  _TODO_( "Acknowledge receipt" );
		}
	      unsigned_t targetVNID = 0;
	      if ((iISPtr.get() != NULL) && 
		  iISPtr->hasSegment( Enum::TargetVNName ))
		{
		  Type::StringSegment ss;
		  ss.useData( iISPtr->getMessageStore(),
			      (uint8_t) Enum::TargetVNName );
		  if (!theVND.hasVariable( ss.iValue ))
		    {
		      _ERROR_( "Illegal variable name: "
			      << ss.iValue << "\n\t" );
		      iDiscard = true;
		    }
		  else
		    {
		      targetVNID = theVND.getVariableVNID( ss.iValue );
		    }
		}
	      else // use VNID
		{
		  targetVNID = iTargetPtr->getVNID();
		  if (!theVND.hasVariable( targetVNID ))
		    {
		      _ERROR_( "Illegal variable vnid: "
			      << targetVNID << "\n\t" );
		      iDiscard = true;
		    }
		}
	      if (!iDiscard)
		{
		  theVND.getVariable( targetVNID )->getInputLink().
		    receiveMessage( *iMSPtr, iISPtr.get() );
		}
	    }
	  else // broadcast
	    {
	      Type::IntegerSegment hopS;
	      hopS.useData( iISPtr->getMessageStore(),
			    (uint8_t) Enum::HopCount );
	      if (hopS.iValue >= 0)
		{
		  // Send to everyone
		  for ( unsigned_t port = 0; port < theEID.getMaxPort();
			++port )
		    {
		      // Except self
		      if ( (port != iPort) &&
			   theEID.hasExternalInterface( port ) )
			{
			  Base::ExternalInterface *anEI =
			    theEID.getExternalInterface( port );
			  
			  // and logs
			  if (!anEI->isLoggingPort())
			    anEI->sendMessage( *iTargetPtr, *iMSPtr,
					       iISPtr.get() );
			}
		    }
		}
	      else
		{
		  iDiscard = true;
		  if (failedRoute)
		    {
		      _TODO_( "Note failure" );
		    }
		}

	    }
	}
      iMSPtr.reset();
      if (!iInMessages)
	{
	  iTargetPtr.reset();
	  iISPtr.reset();
	}
    }
  else
    {
      iInMessages = false;
      iTargetPtr.reset();
      iISPtr.reset();
    }
}

void NRS::Interface::PMLParser::startEntityReference( const
						      XERCES_CPP_NAMESPACE::
						      XMLEntityDecl &entDecl )
{
}

void NRS::Interface::PMLParser::endEntityReference( const
						    XERCES_CPP_NAMESPACE::
						    XMLEntityDecl &entDecl )
{
}

void NRS::Interface::PMLParser::docComment( const XMLCh *const comment )
{
}

void NRS::Interface::PMLParser::docCharacters( const XMLCh* const chars,
					       const unsigned int length,
					       const bool cdataSection )
{
  char *text = XERCES_CPP_NAMESPACE::XMLString::transcode( chars );

  if (iInContentSegment)
    {
      iMSPtr->appendSegment( iSegNum, (const uint8_t *) text,
			     segLength( text ) );
    }
  else
    {
      bool content = false;
      for ( unsigned int i = 0; i < length; ++i )
	{
	  if ( ( text[i] != '\n' ) && ( text[i] != ' ' ) &&
	       ( text[i] != '\t' ) )
	    {
	      content = true;
	      _DEBUG_( ((int) text[i]) );
	    }
	}
      if (content)
	{
	  _ERROR_( "docCharacters: '" << text << "'" );
	}
    }
  XERCES_CPP_NAMESPACE::XMLString::release( &text );
}

void NRS::Interface::PMLParser::ignorableWhitespace( const XMLCh* const chars,
						     const XMLSize_t length,
						     const bool cdataSection )
{
}

void NRS::Interface::PMLParser::docPI( const XMLCh* const target,
				       const XMLCh* const data )
{
}

void NRS::Interface::PMLParser::warning( const XERCES_CPP_NAMESPACE::
					 SAXParseException &exc )
{
  char *aSId = XERCES_CPP_NAMESPACE::XMLString::transcode( exc.getSystemId() );
  char *aMes = XERCES_CPP_NAMESPACE::XMLString::transcode( exc.getMessage() );
  
  _WARN_( aSId << ", line " << exc.getLineNumber()
	  << ", char " << exc.getColumnNumber()
	  << "\n Message: " << aMes );
  
  XERCES_CPP_NAMESPACE::XMLString::release( &aSId );
  XERCES_CPP_NAMESPACE::XMLString::release( &aMes );
}

void NRS::Interface::PMLParser::error( const XERCES_CPP_NAMESPACE::
				       SAXParseException &exc )
{
  char *aSId = XERCES_CPP_NAMESPACE::XMLString::transcode( exc.getSystemId() );
  char *aMes = XERCES_CPP_NAMESPACE::XMLString::transcode( exc.getMessage() );
  
  _ERROR_( aSId << ", line " << exc.getLineNumber()
	   << ", char " << exc.getColumnNumber()
	   << "\n Message: "
	   << aMes );
  
  XERCES_CPP_NAMESPACE::XMLString::release( &aSId );
  XERCES_CPP_NAMESPACE::XMLString::release( &aMes );
}

void NRS::Interface::PMLParser::fatalError( const XERCES_CPP_NAMESPACE::
					    SAXParseException &exc )
{
  char *aSId = XERCES_CPP_NAMESPACE::XMLString::transcode( exc.getSystemId() );
  char *aMes = XERCES_CPP_NAMESPACE::XMLString::transcode( exc.getMessage() );
  
  _ABORT_( aSId << ", line " << exc.getLineNumber()
	   << ", char " << exc.getColumnNumber()
	   << "\n Message: "
	   << aMes );
  
  XERCES_CPP_NAMESPACE::XMLString::release( &aSId );
  XERCES_CPP_NAMESPACE::XMLString::release( &aMes );
}

void NRS::Interface::PMLParser::resetErrors()
{
}

void NRS::Interface::PMLParser::
createOutput( data_t &data,
	      Base::Target &aTarget, 
	      const Base::MessageStore &aMessageStore,
	      const Base::IntelligentStore *intelligentPtr )
{
  std::auto_ptr< Base::IntelligentStore > newISPtr( NULL );
  const uint8_t *ptr;
  const Base::MessageDescription *mdPtr = aTarget.getMessageDescriptionPtr();

  data = '<';
  // element name
  if (!mdPtr) // first segment is message type if no manager
    data += (const uint8_t *) aMessageStore.getSegment( 0 );
  else
    data += (const uint8_t *) mdPtr->iName.c_str();
  data.push_back( ' ' );

  // default namespace
  data += (const uint8_t *) "xmlns='";
  data += (const uint8_t *) Literals::XML::PML_NAMESPACE();
  data.push_back( '\'' );
  data.push_back( ' ' );

  // reserved namespace
  data += (const uint8_t *) "xmlns:";
  data += (const uint8_t *)  Literals::XML::ATT_PREFIX();
  data.push_back( '=' );
  data.push_back( '\'' );
  data += (const uint8_t *) Literals::XML::ATT_NAMESPACE();
  data.push_back( '\'' );
  data.push_back( ' ' );
  // any intelligent attributes?
  if (intelligentPtr != NULL)
    { // Yes, so is there a forward route?
      if (intelligentPtr->hasSegment( Enum::ForwardRoute ))
	{ // Yes, so create a new IntelligentStore with modified forward route
	  newISPtr.reset( new Base::IntelligentStore( *intelligentPtr ) );
	  intelligentPtr = newISPtr.get();
	  Type::RouteSegment rs;
	  rs.useData( newISPtr->getMessageStore(),
		      (uint8_t) Enum::ForwardRoute );
	  routePushPortEnd( rs );
	  rs.createSegment( newISPtr->getMessageStore(),
			    (uint8_t) Enum::ForwardRoute );
	}
      // Now go through all intelligent segments and print them out
      for ( unsigned_t intSeg = (unsigned_t) Enum::IsIntelligent;
	    intSeg < NUM_INTELLIGENT_ATT; ++intSeg )
	{
	  ptr = intelligentPtr->getSegment( (Enum::IntelligentType) intSeg );
	  if (ptr != NULL)
	    {
	      // in reserved namespace
	      data += (const uint8_t *) Literals::XML::ATT_PREFIX();
	      data.push_back( ':' );
	      data += (const uint8_t *) Literals::XML::
		getIntelligentName( (Enum::IntelligentType) intSeg );
	      data.push_back( '=' );
	      data.push_back( '\'' );
	      data += ptr;
	      data.push_back( '\'' );
	      data.push_back( ' ' );
	    }
	}
    }

  const Base::MessageStore &anMS = aTarget.getMessageStore( Enum::PML );

  // add route
  data += (const uint8_t *) Literals::XML::ATT_PREFIX();
  data.push_back( ':' );
  data += (const uint8_t *) Literals::XML::ROUTE();
  data.push_back( '=' );
  data.push_back( '\'' );
  data += anMS.getSegment( Const::RoutePos );
  data.push_back( '\'' );
  data.push_back( ' ' );

  // add toVNID
  data += (const uint8_t *) Literals::XML::ATT_PREFIX();
  data.push_back( ':' );
  data += (const uint8_t *) Literals::XML::TOVNID();
  data.push_back( '=' );
  data.push_back( '\'' );
  data += anMS.getSegment( Const::VNIDPos );
  data.push_back( '\'' );
  data.push_back( ' ' );

  bool someInContents = false;

  // and now segments in attributes
  if (!mdPtr)
    {
      for ( uint8_t segNum = 1;
	    segNum < aMessageStore.getNumSegments(); segNum += 2 )
	{ // alternate attributes are name and value
	  if (aMessageStore.getSegment( segNum )[0] != (const uint8_t) '<')
	    { // names beginning with '<' are in contents
	      data += aMessageStore.getSegment( segNum );
	      data.push_back( '=' );
	      data.push_back( '\'' );
	      data += aMessageStore.getSegment( segNum + 1 );
	      data.push_back( '\'' );
	      data.push_back( ' ' );
	    }  
	  else
	    someInContents = true;
	}
    }
  else
    {
      for ( uint8_t segNum = 0;
	    segNum < aMessageStore.getNumSegments(); ++segNum )
	{
	  const Base::SegmentDescription &sd =
	    *mdPtr->iSegmentDescription[segNum];
	  if (!sd.isInContents())
	    {
	      data += (const uint8_t *) sd.getAttributeName().c_str();
	      data.push_back( '=' );
	      data.push_back( '\'' );
	      data += aMessageStore.getSegment( segNum );
	      data.push_back( '\'' );
	      data.push_back( ' ' );
	    }  
	  else
	    someInContents = true;
	}
    }

  if (!someInContents)
    {
      data.push_back( '/' );
      data.push_back( '>' );
      data.push_back( '\n' );
    }
  else
    {
      data.push_back( '>' );
      data.push_back( '\n' );
      
      // and now segments in contents
      if (!mdPtr)
	{
	  for ( uint8_t segNum = 1;
		segNum < aMessageStore.getNumSegments(); segNum += 2 )
	    { // alternate attributes are name and value
	      if (aMessageStore.getSegment( segNum )[0] != (const uint8_t) '<')
		{ // names beginning with '<' are in contents
		  data += aMessageStore.getSegment( segNum + 1 );
		  data.push_back( '\n' );
		}  
	      else
		someInContents = true;
	    }
	}
      else
	{
	  for ( uint8_t segNum = 0;
		segNum < aMessageStore.getNumSegments(); ++segNum )
	    {
	      const Base::SegmentDescription &sd =
		*mdPtr->iSegmentDescription[segNum];
	      if (sd.isInContents())
		{
		  data += aMessageStore.getSegment( segNum );
		  data.push_back( '\n' );
		}
	    }
	}
      // and finally element name
      data.push_back( '<' );
      data.push_back( '/' );
      data += (const uint8_t *) mdPtr->iName.c_str();
      data.push_back( '>' );
      data.push_back( '\n' );
    }
  _DEBUG_( "Sending " << (const char *) data.c_str()
	   << " [in port " << iPort << "]" );
}
