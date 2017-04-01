package nrs.pml;

import java.io.StringReader;
import nrs.core.base.MessageProcessor;
import nrs.core.comms.CommsRoute;
import nrs.core.comms.MessageListener;
import nrs.core.comms.NRSByteStream;
import nrs.core.comms.NRSMessageCallback;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * <p>This coordinates the process of transforming low level byte
 * sequences representing PML/XML messages into Java
 * message-objects ({@link nrs.core.base.Message}).</p>
 *
 * <p>This class implements the {@link MessageListener} interface, which
 * is used to receive raw messages in byte-stream format. A typical
 * provider of such input are classes derived from {@link
 * nrs.core.comms.CommsRoute}, which represent communication
 * ports to other NRS components.</p>
 *
 * <p>When message-objects are available, after processing byte-stream
 * input, they are dispatched for both preprocessing (which should be
 * performed by the originating port), and then processing (by NRS style
 * variables). Preprocessing is handled by using 
 * {@link nrs.core.comms.NRSMessageCallback} interface.
 * The processing is done using {@link MessageProcessor} interface. 
 * Note, the actual dispatching of {@link nrs.core.base.Message} 
 * objects is performed in {@link PML_XMLEventProcessor}. </p>
 * <p>Because a {@link PMLParser} instance needs to be given a {@link
 * nrs.core.comms.CommsRoute} to use for message preprocessing,
 * typically several instances of {@link PMLParser} will be needed: one
 * for each {@link nrs.core.comms.CommsRoute} instance.
 *
 * @author Darren Smith
 */
public class PMLParser implements MessageListener
{
  private XMLReader m_parser;
  private PML_XMLEventProcessor m_proc;
  private PML_ContentReader m_reader;
  private InputSource m_source;
  //  private NRSByteStream m_byteStream;

  /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
  public static final String NAMESPACES_FEATURE_ID
    = "http://xml.org/sax/features/namespaces";

  /** Namespace prefixes feature id
   * (http://xml.org/sax/features/namespace-prefixes). */
  public static final String NAMESPACE_PREFIXES_FEATURE_ID
    = "http://xml.org/sax/features/namespace-prefixes";

  /** Default parser name. */
  public static final String DEFAULT_PARSER_NAME
    = "org.apache.xerces.parsers.SAXParser";

  //----------------------------------------------------------------------
  /**
   * Create an instance of the PMLParser
   *
   * @throws Exception if the XML parser could not be created and configured
   */
  public PMLParser() throws Exception
  {
    init();
  }
  //----------------------------------------------------------------------
  /**
   * {@inheritDoc}
   */
  public void messageEvent(byte[] buffer, int size,
                           nrs.core.comms.CommsRoute port)
  {
    try
    {
      String s = new String(buffer, 0, size, "US-ASCII");
      StringReader sr = new StringReader(s); // todo, make an NRS string reader
      m_source.setCharacterStream(sr);


      /* This is the preferred way of parsing - using a byte stream, but
       * it is currently not working.

      m_byteStream.setBuffer(buffer, 0, size);
      m_source.setByteSteam(m_byteStream);
      m_source.setEncoding("UTF-8");

      */
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return;
    }
    try
    {
      m_parser.parse(m_source);
    }
    catch(SAXParseException spe)
    {
      StringBuffer sb = new StringBuffer( spe.toString() );
      sb.append("\n Line number: " + spe.getLineNumber());
      sb.append("\nColumn number: " + spe.getColumnNumber() );
      sb.append("\n Public ID: " + spe.getPublicId() );
      sb.append("\n System ID: " + spe.getSystemId() + "\n");
      PackageLogger.log.warning("Parser error, " + sb.toString() );
      spe.printStackTrace();
    }
    catch (Exception e)
    {
      PackageLogger.log.warning("Failed to parse PML message, " + e);
      e.printStackTrace();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Initialise the XML parser
   *
   * @throws Exception if the XML parser could not be created and configured
   */
  private void init() throws Exception
  {
    // Attempt to create the parser
    try
    {
      m_parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
    }
    catch (SAXException e)
    {
      String errMsg = "Cannot create an XML parser ("
        + DEFAULT_PARSER_NAME +"),\n"
        + "because the class cannot be loaded,"
        + " instantiated, and cast to XMLReader.\n"
        + "One possible problem is that the"
        + " Xerces class/jar files are not\n"
        + "included on the user class path.\n"
        + "Sax-error=" + e;

      throw new Exception(errMsg);
    }

    // Attempt to configure the parser
    try
    {
      // Support XML Namespaces
      m_parser.setFeature(NAMESPACES_FEATURE_ID, true);
    }
    catch (SAXException e)
    {
      String errMsg = "Parser does not support feature ("
        + NAMESPACES_FEATURE_ID + "): " + e;
      throw new Exception(errMsg);
    }
    try
    {
      // Don't need the prefixes, since the qualified local names
      // are not that useful for testing (since things like n:node
      // can just as easily be z:node - i.e, the user creating the
      // XML file can decide the alias for themselves
      m_parser.setFeature(NAMESPACE_PREFIXES_FEATURE_ID, false);
    }
    catch (SAXException e)
    {
      String errMsg = "Parser does not support feature ("
        + NAMESPACE_PREFIXES_FEATURE_ID + "): " + e;
      throw new Exception(errMsg);
    }

    m_proc = new PML_XMLEventProcessor();
    m_reader = new PML_ContentReader(m_proc);
    m_parser.setContentHandler(m_reader);
    m_source = new InputSource();
  }
  //----------------------------------------------------------------------
  /**
   * Provide the {@link nrs.core.comms.CommsRoute} and {@link
   * nrs.core.base.MessageProcessor} objects to be used for
   * preprocessing and processing messages once they have been decoded
   * and represented as {@link nrs.core.base.Message} objects.
   */
  public void setMessageCallbacks(nrs.core.comms.CommsRoute portCallback,
                                  MessageProcessor msgProcessor)
  {
    m_proc.setMessageCallbacks(portCallback, msgProcessor);
  }
}
