/*
 * Copyright (C) 2005 Richard Reeve and Edinburgh University
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

#ifndef _CSL_PARSER_HH
#define _CSL_PARSER_HH

#pragma interface

#include <string>
#include <vector>
#include <xercesc/framework/LocalFileInputSource.hpp>
#include <xercesc/framework/XMLPScanToken.hpp>
#include <xercesc/parsers/SAXParser.hpp>
#include <xercesc/sax/ErrorHandler.hpp>

#include "CSLVariableManager.hh"
#include "SegmentDescription.hh"

namespace NRS
{
  namespace Base
  {
    class VariableManager;

    /// This is the CSL Parser class
    class CSLParser : public XERCES_CPP_NAMESPACE::SAXParser,
		      public XERCES_CPP_NAMESPACE::ErrorHandler
    {
    public:
      /// Simple constructor
      /**
       *
       * \param aName name of variable to look for
       * \param aFilename name of filename to look in, or leave empty
       * to take name from variable name
       *
       **/
      CSLParser( std::string aName, std::string aFilename = "" );

      /// null destructor
      virtual ~CSLParser();
      
      /// Parse a new buffer
      /**
       *
       * \returns VariableManager created from parse
       *
       **/
      Base::VariableManager *createManager();

      /// callback for document start
      /**
       * This method is used to report the start of the parsing process.
       *
       */
      void startDocument();

      /// callback for document end
      /**
       * This method is used to indicate the end of root element
       * was just scanned by the parser.
       */
      void endDocument();

      /// callback to reset document handler
      /**
       * This method allows the advanced callback handlers to 'reset'
       * itself.
       */
      void resetDocument();

      /// callback for element start
      /**
       * This method is used to report the start of an element. It is
       * called at the end of the element, by which time all attributes
       * specified are also parsed.
       *
       * \param elemDecl A const reference to the object containing element
       *                 declaration information.
       * \param urlId    An id referring to the namespace prefix, if
       *                 namespaces setting is switched on.
       * \param elemPrefix A const pointer to a Unicode string containing
       *                   the namespace prefix for this element. Applicable
       *                   only when namespace processing is enabled.
       * \param attrList  A const reference to the object containing the
       *                  list of attributes just scanned for this element.
       * \param attrCount A count of number of attributes in the list
       *                  specified by the parameter 'attrList'.
       * \param isEmpty  A flag indicating whether this is an empty element
       *                 or not.
       * \param isRoot   A flag indicating whether this element was the
       *                 root element.
       */
      void startElement( const XERCES_CPP_NAMESPACE::XMLElementDecl &elemDecl,
			 const unsigned int urlId,
			 const XMLCh * const elemPrefix,
			 const XERCES_CPP_NAMESPACE::
			 RefVectorOf< XERCES_CPP_NAMESPACE::XMLAttr >
			 &attrList,
			 const unsigned int attrCount,
			 const bool isEmpty,
			 const bool isRoot );

      /// callback for element end
      /**
       * This method is used to indicate the end tag of an element.
       *
       * \param elemDecl A const reference to the object containing element
       *                 declaration information.
       * \param urlId    An id referring to the namespace prefix, if
       *                 namespaces setting is switched on.
       * \param isRoot   A flag indicating whether this element was the
       *                 root element.
       * \param elemPrefix A const pointer to a Unicode string containing
       *                   the namespace prefix for this element. Applicable
       *                   only when namespace processing is enabled.
       */
      void endElement( const XERCES_CPP_NAMESPACE::XMLElementDecl& elemDecl,
		       const unsigned int urlId,
		       const bool isRoot,
		       const XMLCh* const elemPrefix );

      /// callback for start of entity reference
      void startEntityReference( const XERCES_CPP_NAMESPACE::XMLEntityDecl
				 &entDecl );

      /// callback for end of entity reference
      /**
       * This method is used to indicate that an end of an entity reference
       * was just scanned.
       *
       *
       * \param entDecl A const reference to the object containing the
       *                entity declaration information.
       */
      void endEntityReference( const XERCES_CPP_NAMESPACE::XMLEntityDecl
			       &entDecl );
      
      /// callback for a comment
      /**
       *
       * This method is used to indicate that a comment has been scanned.
       *
       * \param comment A const reference to the text in the comment.
       *
       **/

      void docComment( const XMLCh *const comment );

      /// callback for character data
      /**
       * This method is used to report all the characters scanned
       * by the parser.
       *
       * \param chars   A const pointer to a Unicode string representing the
       *                character data.
       * \param length  The length of the Unicode string returned in 'chars'.
       * \param cdataSection  A flag indicating if the characters represent
       *                      content from the CDATA section.
       */
      void docCharacters( const XMLCh* const chars,
			  const unsigned int length,
			  const bool cdataSection );

      /// callback for ignorable whitespace
      /**
       * This method is used to report all the whitespace characters,
       * which are determined to be 'ignorable'. This distinction
       * between characters is only made, if validation is enabled.
       *
       * Any whitespace before content is not reported to the SAX
       * Document Handler method, as per the SAX specification.
       * However, if any advanced callback handlers are installed, the
       * corresponding 'ignorableWhitespace' method is invoked.
       *
       * \param chars   A const pointer to a Unicode string representing the
       *                ignorable whitespace character data.
       * \param length  The length of the Unicode string 'chars'.
       * \param cdataSection  A flag indicating if the characters represent
       *                      content from the CDATA section.
       */
      void ignorableWhitespace( const XMLCh* const chars,
				const unsigned int length,
				const bool cdataSection );

      /// callback for processing instruction
      /**
       * This method is used to report any PI scanned by the parser.
       *
       * Any PI's occurring before any 'content' are not reported
       * to any SAX handler as per the specification. However, all
       * PI's within content are reported.
       *
       * \param target A const pointer to a Unicode string representing the
       *               target of the PI declaration.
       * \param data   A const pointer to a Unicode string representing the
       *               data of the PI declaration. See the PI production rule
       *               in the XML specification for details.
       *
       */
       void docPI( const XMLCh* const target,
		  const XMLCh* const data );

      /// callback for parser warning
      /**
       * Receive notification of a warning.
       *
       * SAX parsers will use this method to report conditions that
       * are not errors or fatal errors as defined by the XML 1.0
       * recommendation.
       *
       * The SAX parser must continue to provide normal parsing events
       * after invoking this method: it should still be possible for the
       * application to process the document through to the end.
       *
       * \param exc The warning information encapsulated in a
       *            SAX parse exception.
       * \exception SAXException Any SAX exception, possibly
       *            wrapping another exception.
       */
      void warning( const XERCES_CPP_NAMESPACE::SAXParseException &exc );

      /// callback for parser error
      /**
       * Receive notification of a recoverable error.
       *
       * This corresponds to the definition of "error" in section 1.2
       * of the W3C XML 1.0 Recommendation.  For example, a validating
       * parser would use this callback to report the violation of a
       * validity constraint.
       *
       * The SAX parser must continue to provide normal parsing events
       * after invoking this method: it should still be possible for the
       * application to process the document through to the end.  If the
       * application cannot do so, then the parser should report a fatal
       * error even if the XML 1.0 recommendation does not require it to
       * do so.
       *
       * \param exc The error information encapsulated in a
       *            SAX parse exception.
       * \exception SAXException Any SAX exception, possibly
       *            wrapping another exception.
       */
      void error( const XERCES_CPP_NAMESPACE::SAXParseException &exc );

      /// callback for fatal parser error
      /**
       * Receive notification of a non-recoverable error.
       *
       * This corresponds to the definition of "fatal error" in
       * section 1.2 of the W3C XML 1.0 Recommendation.  For example, a
       * parser would use this callback to report the violation of a
       * well-formedness constraint.
       *
       * The application must assume that the document is unusable
       * after the parser has invoked this method, and should continue
       * (if at all) only for the sake of collecting addition error
       * messages: in fact, SAX parsers are free to stop reporting any
       * other events once this method has been invoked.
       *
       * \param exc The error information encapsulated in a
       *            SAX parse exception.
       * \exception SAXException Any SAX exception, possibly
       *            wrapping another exception.
       */
      void fatalError( const XERCES_CPP_NAMESPACE::SAXParseException &exc );
      
      /// callback to reset errors
      /**
       * Reset the Error handler object on its reuse
       *
       * This method helps in reseting the Error handler object
       * implementational defaults each time the Error handler is begun.
       *
       */
      void resetErrors();

    private:

      /// Token showing position in parse
      XERCES_CPP_NAMESPACE::XMLPScanToken iXPST;

      /// The memory input source for the parser
      std::auto_ptr< XERCES_CPP_NAMESPACE::LocalFileInputSource > iLFIS;

      /// Name of Variable type
      std::string iName;

      /// Name of file to open
      std::string iFilename;

      /// Are we inside the Message envelope?
      bool iInMessage;

      /// Are we inside the Message envelope?
      bool iInSegment;

      /// Did we find it?
      bool iFound;

      /// vector of SegmentDescriptions so far
      std::vector< SegmentDescription > iSegmentDescription;
    };
  }
}
#endif //ndef _CSL_PARSER_HH
