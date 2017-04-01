package nrs.pml;

import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A concrete implementation of the main interface which SAX
 * applications must implement. This class will be registered with an
 * <code>XMLReader</code> to receive basic document-related events like
 * the start and end of elements and character data. It forms a link
 * between the Java <code>XMLReader</code> and the {@link
 * PML_XMLEventProcessor} which actually does all the hard work of
 * identifying known element tags and processing them accordingly.
 *
 * @author Darren Smith
 */
public class PML_ContentReader implements ContentHandler
{
  /** XML event processor, which does the real work of identifying
   *  PMLelement types and creating the relevant class
   *  representation. */
  private PML_XMLEventProcessor m_proc;

  //----------------------------------------------------------------------
  /** Constructor. */
  public PML_ContentReader(PML_XMLEventProcessor proc)
  {
    super();

    m_proc = proc;
  }
  //----------------------------------------------------------------------
  /** Receive notification of character data. Inherited from {@link
   * org.xml.sax.ContentHandler ContentHandler}.
   */
  public void characters(char[] ch, int start, int length)
  {
    String s = "";
    for (int i = 0; i < length; i++) {
      s += ch[start + i];
    }

    m_proc.characterData(s);
  }
  //----------------------------------------------------------------------
  /** Receive notification of the end of a document. Inherited from
   * {@link ContentHandler}.
   */
  public void endDocument()
  {
    //    PackageLogger.log.finest("Notified, XML endDocument");
  }
  //----------------------------------------------------------------------
  /** Receive notification of the end of an XML element. Inherited from
   * {@link org.xml.sax.ContentHandler ContentHandler}.
   */
  public void endElement(java.lang.String uri,
                         java.lang.String localName,
                         java.lang.String qName)
  {
//     PackageLogger.log.finest("Notified, XML endElement, "
//                              + "uri=" + uri
//                              + ", localName=" + localName
//                              + ", qName=" + qName);
    try
    {
      m_proc.endElement(uri, localName);
    }
    catch (Exception e)
    {
      PackageLogger.log.warning(e.toString());
      e.printStackTrace();
    }
  }
  //----------------------------------------------------------------------
  /** End the scope of a prefix-URI mapping. Inherited from {@link
   * org.xml.sax.ContentHandler ContentHandler}.
   */
  public void endPrefixMapping(java.lang.String prefix)
  {
  }
  //----------------------------------------------------------------------
  /** Receive notification of a processing instruction. Inherited from
   * {@link org.xml.sax.ContentHandler ContentHandler}.
   */
  public void processingInstruction(java.lang.String target,
                                    java.lang.String data)
  {
  }
  //----------------------------------------------------------------------
  /** Receive an object for locating the origin of SAX document
   * events.. Inherited from {@link org.xml.sax.ContentHandler
   * ContentHandler}.
   */
  public void setDocumentLocator(Locator locator)
  {
  }
  //----------------------------------------------------------------------
  /** Receive notification of the beginning of a document. Inherited
   * from {@link org.xml.sax.ContentHandler ContentHandler}.
   */
  public void startDocument()
  {
    //    PackageLogger.log.finest("Notified, XML startDocument");
  }
  //----------------------------------------------------------------------
  /** Receive notification of the begging of an element. Inherited from
   * {@link org.xml.sax.ContentHandler ContentHandler}.
   */
  public void startElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName, Attributes atts)
  {
    PackageLogger.log.finest("Notified, XML startElement, "
                             + "uri=" + uri
                             + ", localName=" + localName
                             + ", qName=" + qName);

    //    HashMap map = attributesToMap(atts);

    // Continue reset of program processing of XML
    m_proc.startElement(uri, localName, atts);
  }
  //----------------------------------------------------------------------
  /** Begin the scope of a prefix-URI Namespace mapping.  Inherited from
   * {@link org.xml.sax.ContentHandler ContentHandler}.
   */
  public void startPrefixMapping(java.lang.String prefix,
                                 java.lang.String uri)
  {
  }
  //----------------------------------------------------------------------
  /** Receive notification of a skipped entity. Inherited from {@link
   * org.xml.sax.ContentHandler ContentHandler}.
   */
  public void skippedEntity(java.lang.String name)
  {
  }
  //----------------------------------------------------------------------
  /** Receive notification of ignorable whitespace in element
   * content. Inherited from {@link org.xml.sax.ContentHandler
   * ContentHandler}.
   */
  public void ignorableWhitespace(char[] ch, int start, int length)
  {
  }
  //----------------------------------------------------------------------
//   private HashMap attributesToMap(Attributes atts)
//   {
//     HashMap map = new HashMap();

//     // Note: this method is very similar, but crucially different to, a
//     // method in the CSL_ContentReader class
//     for (int i = 0; i < atts.getLength(); i++)
//       {
//         map.put(atts.getQName(i), atts.getValue(i));
//       }

//     return map;
//   }
}
