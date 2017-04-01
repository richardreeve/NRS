// $Id: CSL_ContentReader.java,v 1.4 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.*;

import java.util.logging.*;
import java.util.*;

/** Provides SAX2 content handler for NRS/CSL XML file */
public class CSL_ContentReader implements ContentHandler
{
  /** Logger for internal use by this class. Set during
   * construction. */
  private Logger m_logger = Logger.getLogger("nrs.csl");

  /** XML event processor, which does the real work of identifying
   * element types and creating the relevant class representation. */
  private CSL_XMLEventProcessor m_proc;

  //----------------------------------------------------------------------
  /** Constructor. */
  public CSL_ContentReader(CSL_XMLEventProcessor proc)
  {
    super();
    m_proc = proc;
  }
  //----------------------------------------------------------------------
  private HashMap attributesToMap(Attributes atts)
  {
    HashMap map = new HashMap();

    for (int i = 0; i < atts.getLength(); i++)
      {
        map.put(atts.getLocalName(i), atts.getValue(i));
      }

    return map;
  }
  //----------------------------------------------------------------------
  /** Receive notification of character data. Inherited from {@link
   * org.xml.sax.ContentHandler ContentHandler}.
   */
  public void characters(char[] ch, int start, int length)
  {
    m_logger.finest("Notified, XML characters");

    String s = "";
    for (int i = 0; i < length; i++) {
      s += ch[start + i];
    }

    m_proc.characterData(s);
  }
  //----------------------------------------------------------------------
  /** Receive notification of the end of a document. Inherited from
   * {@link org.xml.sax.ContentHandler ContentHandler}.
   */
  public void endDocument()
  {
    m_logger.finest("Notified, XML endDocument");
  }
  //----------------------------------------------------------------------
  /** Receive notification of the end of an XML element. Inherited from
   * {@link org.xml.sax.ContentHandler ContentHandler}.
   */
  public void endElement(java.lang.String uri,
                         java.lang.String localName,
                         java.lang.String qName)
  {
    m_logger.finest("Notified, XML endElement, "
                  + "uri=" + uri
                  + ", localName=" + localName
                  + ", qName=" + qName);
    try
      {
        m_proc.endElement(uri, localName);
      }
    catch (CSL_Exception e)
      {
        Logger.getLogger("nrs.csl").finest(e.toString());
      }
  }
  //----------------------------------------------------------------------
  /** End the scope of a prefix-URI mapping. Inherited from {@link
   * org.xml.sax.ContentHandler ContentHandler}.
   */
  public void endPrefixMapping(java.lang.String prefix)
  {
    m_logger.finest("Notified, XML endPrefixMapping");
  }
  //----------------------------------------------------------------------
  /** Receive notification of a processing instruction. Inherited from
   * {@link org.xml.sax.ContentHandler ContentHandler}.
   */
  public void processingInstruction(java.lang.String target,
                                    java.lang.String data)
  {
    m_logger.finest("Notified, XML processingInstruction");
  }
  //----------------------------------------------------------------------
  /** Receive an object for locating the origin of SAX document
   * events.. Inherited from {@link org.xml.sax.ContentHandler
   * ContentHandler}.
   */
  public void setDocumentLocator(Locator locator)
  {
    m_logger.finest("Notified, XML setDocumentLocator");
  }
  //----------------------------------------------------------------------
  /** Receive notification of the beginning of a document. Inherited
   * from {@link org.xml.sax.ContentHandler ContentHandler}.
   */
  public void startDocument()
  {
    m_logger.finest("Notified, XML startDocument");
  }
  //----------------------------------------------------------------------
  /** Receive notification of the begging of an element. Inherited from
   * {@link org.xml.sax.ContentHandler ContentHandler}.
   */
  public void startElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName, Attributes atts)
  {
    m_logger.finest("Notified, XML startElement, "
                  + "uri=" + uri
                  + ", localName=" + localName
                  + ", qName=" + qName);

    HashMap map = attributesToMap(atts);

    // Continue reset of program processing of XML
    m_proc.startElement(uri, localName, map);
  }
  //----------------------------------------------------------------------
  /** Begin the scope of a prefix-URI Namespace mapping.  Inherited from
   * {@link org.xml.sax.ContentHandler ContentHandler}.
   */
  public void startPrefixMapping(java.lang.String prefix,
                                 java.lang.String uri)
  {
    m_logger.finest("Notified, XML startPrefixMapping");
  }
  //----------------------------------------------------------------------
  /** Receive notification of a skipped entity. Inherited from {@link
   * org.xml.sax.ContentHandler ContentHandler}.
   */
  public void skippedEntity(java.lang.String name)
  {
    m_logger.finest("Notified, XML skippedEntity");
  }
  //----------------------------------------------------------------------
  /** Receive notification of ignorable whitespace in element
   * content. Inherited from {@link org.xml.sax.ContentHandler
   * ContentHandler}.
   */
  public void ignorableWhitespace(char[] ch, int start, int length)
  {
    m_logger.finest("Notified, XML ignorableWhitespace");
  }
}
