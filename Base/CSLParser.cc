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
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/types.h>

#include <xercesc/sax/SAXParseException.hpp>
#include <xercesc/util/PlatformUtils.hpp>

#include "CSLParser.hh"
#include "CSLVariableManager.hh"
#include "StringLiterals.hh"
#include "Types.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"

namespace NRS
{
  namespace Base
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

NRS::Base::CSLParser::CSLParser( std::string aName, std::string aFilename ) :
  XERCES_CPP_NAMESPACE::SAXParser(), XERCES_CPP_NAMESPACE::ErrorHandler(),
  iLFIS( NULL ), iName( aName ), iFilename( aFilename ),
  iInMessage( false ), iInSegment( false ), iFound( false )
{
  setDoNamespaces( true );
  //setDoValidation( false );
  setDoSchema( false );
  setExitOnFirstFatalError( true );
  installAdvDocHandler( this );
  setErrorHandler( this );
  if (iFilename.empty())
    iFilename = iName + ".xml";
}

NRS::Base::CSLParser::~CSLParser()
{
}

NRS::Base::VariableManager *NRS::Base::CSLParser::createManager()
{
  std::string fileName = PREFIX;
  fileName += "/share/";
  fileName += PACKAGE;
  fileName += "-";
  fileName += VERSION;
  fileName += "/csl/";
  fileName += iFilename;

  _DEBUG_( fileName );

  // Try opening the file to see if it exists
  int fd = ::open( fileName.c_str(), O_RDONLY );
  _DEBUG_( fd );
  if (fd == -1)
    {
      return NULL;
    }
  ::close( fd );

  XMLCh *ptr = XERCES_CPP_NAMESPACE::XMLString::transcode( fileName.c_str() );

  iLFIS.reset( new XERCES_CPP_NAMESPACE::LocalFileInputSource( ptr ) );

  XERCES_CPP_NAMESPACE::XMLString::release( &ptr );

  iSegmentDescription.clear();

  if (!parseFirst( *iLFIS, iXPST ))
    {
      return NULL;
    }

  while (parseNext( iXPST ));

  if (!iFound)
    return NULL;

  std::auto_ptr< VariableManager > 
    vmPtr( new CSLVariableManager( iName, iSegmentDescription ) );
  VariableNodeDirector::getDirector().registration();
  return vmPtr.release();
}

void NRS::Base::CSLParser::startDocument()
{
}

void NRS::Base::CSLParser::endDocument()
{
}

void NRS::Base::CSLParser::resetDocument()
{
}

void NRS::Base::CSLParser::startElement( const XERCES_CPP_NAMESPACE::
					      XMLElementDecl &elemDecl,
					      const unsigned int urlId,
					      const XMLCh * const elemPrefix,
					      const XERCES_CPP_NAMESPACE::
					      RefVectorOf< 
					      XERCES_CPP_NAMESPACE::
					      XMLAttr > &attrList,
					      const XMLSize_t attrCount,
					      const bool isEmpty,
					      const bool isRoot )
{
  char *ptr =
    XERCES_CPP_NAMESPACE::XMLString::transcode( elemDecl.getBaseName() );
  std::string eltName = ptr;

  XERCES_CPP_NAMESPACE::XMLString::release( &ptr );

  std::map< std::string, std::string > attrMap;

  for ( unsigned int i = 0; i < attrCount; ++i )
    {
      const XERCES_CPP_NAMESPACE::XMLAttr *attr = attrList.elementAt( i );
      char *anURIName = XERCES_CPP_NAMESPACE::
	XMLString::transcode( getURIText( attr->getURIId() ) );
      char *anAttName = XERCES_CPP_NAMESPACE::
	XMLString::transcode( attr->getName() );
      char *anAttValue = XERCES_CPP_NAMESPACE::
	XMLString::transcode( attr->getValue() );

      attrMap[ anAttName ] = anAttValue;
  
      XERCES_CPP_NAMESPACE::XMLString::release( &anURIName );
      XERCES_CPP_NAMESPACE::XMLString::release( &anAttName );
      XERCES_CPP_NAMESPACE::XMLString::release( &anAttValue );
    }

  if (!iInMessage)
    {
      if (eltName == "Message")
	_DEBUG_( attrMap[ "name" ] );
      if ((eltName == "Message") && (attrMap[ "name" ] == iName))
	{
	  iFound = true;
	  iInMessage = true;
	}
    }
  else if (!iInSegment)
    {
      if (eltName == "Segment")
	{
	  _DEBUG_( attrMap[ "name" ] );
	  _DEBUG_( attrMap[ "unit" ] );
	  const VariableManager &theVM = VariableNodeDirector::getDirector().
	    getVariableManager( attrMap[ "unit" ] );
	  iSegmentDescription.push_back( theVM.getSegmentDescription( 0 ) );
	  iSegmentDescription.back().setAttributeName( attrMap[ "name" ] );
	  iSegmentDescription.back().
	    setInContents( (attrMap[ "segmentInContents" ] == "true") );
	}
    }
}

void NRS::Base::CSLParser::endElement( const XERCES_CPP_NAMESPACE::
					    XMLElementDecl &elemDecl,
					    const unsigned int urlId,
					    const bool isRoot,
					    const XMLCh* const elemPrefix )
{
  char *ptr =
    XERCES_CPP_NAMESPACE::XMLString::transcode( elemDecl.getBaseName() );
  std::string eltName = ptr;

  XERCES_CPP_NAMESPACE::XMLString::release( &ptr );

  if (iInSegment)
    {
      if (eltName == "Segment")
	iInSegment = false;
    }
  else if (iInMessage)
    {
      if (eltName == "Message")
	iInMessage = false;
    }
}

void NRS::Base::CSLParser::startEntityReference( const
						      XERCES_CPP_NAMESPACE::
						      XMLEntityDecl &entDecl )
{
}

void NRS::Base::CSLParser::endEntityReference( const
						    XERCES_CPP_NAMESPACE::
						    XMLEntityDecl &entDecl )
{
}

void NRS::Base::CSLParser::docComment( const XMLCh *const comment )
{
}

void NRS::Base::CSLParser::docCharacters( const XMLCh* const chars,
					       const XMLSize_t length,
					       const bool cdataSection )
{
}

void NRS::Base::CSLParser::ignorableWhitespace( const XMLCh* const chars,
						     const XMLSize_t length,
						     const bool cdataSection )
{
}

void NRS::Base::CSLParser::docPI( const XMLCh* const target,
				       const XMLCh* const data )
{
}

void NRS::Base::CSLParser::warning( const XERCES_CPP_NAMESPACE::
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

void NRS::Base::CSLParser::error( const XERCES_CPP_NAMESPACE::
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

void NRS::Base::CSLParser::fatalError( const XERCES_CPP_NAMESPACE::
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

void NRS::Base::CSLParser::resetErrors()
{
}
