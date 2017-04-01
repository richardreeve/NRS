package nrs.nrsgui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import nrs.core.base.ComponentInfo;
import nrs.core.base.Message;
import nrs.core.comms.CommsRoute;
import nrs.core.message.Constants;
import nrs.csl.CSL_ContentReader;
import nrs.csl.CSL_Element_Registry;
import nrs.csl.CSL_Global_Registry;
import nrs.csl.CSL_Message_Registry;
import nrs.csl.CSL_Node_Registry;
import nrs.csl.CSL_Unit_Registry;
import nrs.csl.CSL_XMLEventProcessor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This collects together all of the information so far discovered about
 * an available NRS component.
 *
 * @author Darren Smith
 */
public class NRSComponent extends ComponentInfo
{

  /** Default parser name. */
  protected static final String DEFAULT_PARSER_NAME
    = "org.apache.xerces.parsers.SAXParser";

  // TODO - move these, finally, to somewhere else
  /** Namespaces feature id (http://xml.org/sax/features/namespaces). */
  static final String NAMESPACES_FEATURE_ID
    = "http://xml.org/sax/features/namespaces";

  /** Namespace prefixes feature id
   * (http://xml.org/sax/features/namespace-prefixes). */
  static final String NAMESPACE_PREFIXES_FEATURE_ID
    = "http://xml.org/sax/features/namespace-prefixes";

  private CommsRoute m_port;
  private String m_cslFilename;

  private CSL_Unit_Registry m_unitReg;
  private CSL_Message_Registry m_messageReg;
  private CSL_Node_Registry m_nodeReg;
  private CSL_Element_Registry m_elementReg;

  /** Determines whether the CSL for this component has yet been obtained */
  private boolean m_cslFound = false;

  //----------------------------------------------------------------------
  /**
   * Create an instance based on the contents of an NRS message.
   */
  NRSComponent(Message m)
  {
    super(m);
  }
  //----------------------------------------------------------------------
  /**
   * Fully specifiy the state the instance created
   */
  NRSComponent(String name, String cid, String route,
               CommsRoute port, boolean speaksBMF, boolean speaksPML,
               String version)
  {
    super(name, cid, route, speaksBMF, speaksPML, version);
    m_port = port;
  }
  //----------------------------------------------------------------------
  /**
   * Return whether the CSL for this component has yet been obtained.
   */
  public boolean hasCSL()
  {
    return m_cslFound;
  }
  //----------------------------------------------------------------------
  /**
   * Get the cType of this NRS component.
   *
   * @return the name of the component or null if the name is not yet known
   */
  public String getName()
  {
    return getCType();
  }
  //----------------------------------------------------------------------
  /**
   * Check whether CSL has yet been obtained for this component, and if
   * not, attempt to extract it from the supplied message, or else, load
   * it from the CSL repository. The latter can only be attempted if the
   * cType has been previously specified.
   *
   * @param repository path to CSL repository
   *
   * @param replyCSL a {@link Message} storing a ReplyCSL message. The
   * auxillary data field might contain CSL information. Set to null if
   * no message is available.
   *
   * @see #setCType(String)
   */
  public void acquireCSL(String repository,
                         Message replyCSL)
  {
    if (m_cslFound) return;

    // Is CSL information in the message?
    if ((replyCSL != null) &&
        (replyCSL.aux() != null) &&
        (replyCSL.aux().getUnitRegistry() != null) &&
        (replyCSL.aux().getMessageRegistry() != null) &&
        (replyCSL.aux().getNodeRegistry() != null))
    {
      m_unitReg = replyCSL.aux().getUnitRegistry();
      m_messageReg = replyCSL.aux().getMessageRegistry();
      m_nodeReg = replyCSL.aux().getNodeRegistry();

      m_unitReg.setSourceCID(getCID());
      m_messageReg.setSourceCID(getCID());
      m_nodeReg.setSourceCID(getCID());

      CSL_Global_Registry.getInstance().addRegisties(m_unitReg,
                                                     m_messageReg,
                                                     m_nodeReg);

      // Resolve dependencies within this CSL
      m_messageReg.resolveUnits(m_unitReg);
      m_nodeReg.resolveAttributes(m_unitReg);
      m_nodeReg.resolveMessageTypes(m_messageReg);

      m_cslFound = true;
      PackageLogger.log.info("CSL for component " + this
                             + " acquired from " + replyCSL + " message");

      return;
    }

    if (getCType() == null) {
      PackageLogger.log.warning("Component has no cType: unable to"
                                + " attempt CSL repository retrieval");
      return;
    }

    if (repository == null)
    {
      PackageLogger.log.warning("No CSL repository specified:"
                                + " unable to obtain repository CSL for "
                                + getCType());
      return;
    }

    m_cslFilename = repository + "/" + getCType() + ".xml";


    m_unitReg = new CSL_Unit_Registry(getCID());
    m_messageReg = new CSL_Message_Registry(getCID());
    m_nodeReg = new CSL_Node_Registry(getCID());
    m_elementReg = new CSL_Element_Registry(m_unitReg,
                                            m_messageReg, m_nodeReg);
    m_unitReg.addImplicitUnits(m_elementReg);
    m_messageReg.addImplicitMessagesFromUnits(m_elementReg, m_unitReg);

    PackageLogger.log.info("Attempting to open " + m_cslFilename);
    try
    {
      loadCSL();

      CSL_Global_Registry.getInstance().addRegisties(m_unitReg,
                                                     m_messageReg,
                                                     m_nodeReg);
    }
    catch (Exception e)
    {
      PackageLogger.log.severe("Failed to open/parse CSL file "
                               + m_cslFilename + ". Error=" + e);
      e.printStackTrace();

      m_unitReg = null;
      m_messageReg = null;
      m_nodeReg = null;
      m_elementReg = null;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to load in CSL. An exception is thrown upon encountering
   * any kind of problem.
   */
  private void loadCSL() throws Exception
  {
    StringReader sr = null;

    // create an XML event processor
    CSL_XMLEventProcessor XMLproc = new CSL_XMLEventProcessor(m_elementReg);
    // create a content reader
    CSL_ContentReader reader = new CSL_ContentReader(XMLproc);

    XMLReader parser = null;

    // Attempt to create the parser
    try
    {
      parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
    }
    catch (SAXException e)
    {
      String errMsg = "Cannot create an XML parser ("
	+ DEFAULT_PARSER_NAME + "),\n"
	+ "because the class cannot be loaded,"
	+ " instantiated, and cast to XMLReader.\n"
	+ "One possible problem is that the"
	+ " Xerces class/jar files are not\n"
	+ "included on the user class path.\n" + "Sax-error=" + e;

      throw new Exception (errMsg);
    }

    // Attempt to configure the parser
    try
    {
      // Support XML Namespaces
      parser.setFeature (NAMESPACES_FEATURE_ID, true);
    }
    catch (SAXException e)
    {
      String errMsg = "Parser does not support feature ("
	+ NAMESPACES_FEATURE_ID + "): " + e;
      throw new Exception (errMsg);
    }
    try
    {
      // Don't need the prefixes, since the qualified local names
      // are not that useful for testing (since things like n:node
      // can just as easily be z:node - i.e, the user creating the
      // XML file can decide the alias for themselves
      parser.setFeature (NAMESPACE_PREFIXES_FEATURE_ID, false);
    }
    catch (SAXException e)
    {
      String errMsg = "Parser does not support feature ("
	+ NAMESPACE_PREFIXES_FEATURE_ID + "): " + e;
      throw new Exception (errMsg);
    }

    // parse file
    parser.setContentHandler(reader);
    InputSource inputSource = null;

    if (sr != null)
      {
	inputSource = new InputSource(sr);
      }

    File cslFile = new File(m_cslFilename);
    try
    {
      if (inputSource != null)
	{
	  PackageLogger.log.fine("Parsing XML from internal buffer");
	  parser.parse(inputSource);
	}
      else
	{
	  PackageLogger.log.fine("Parsing XML from file '" + m_cslFilename + "'");
	  parser.parse(cslFile.getAbsolutePath());
	}
    }
    catch (SAXParseException e)
    {
      String errMsg = "Failed to parse NRS/CSL file '"
	+ cslFile.getAbsolutePath () + "'. Error=" + e;
      throw new Exception (errMsg);
    }
    catch (FileNotFoundException e)
    {
      String errMsg = "Failed to load NRS/CSL file '"
	+ cslFile.getAbsolutePath ()
	+ "'. Make sure file exists and can be read.";
      throw new Exception (errMsg);
    }
    catch (Exception e)
    {
      String errMsg = "Parse failed, reason=" + e;
      throw new Exception (errMsg, e);
    }

    PackageLogger.log.fine(m_unitReg.toString());
    PackageLogger.log.fine(m_messageReg.toString());
    PackageLogger.log.fine(m_nodeReg.toString());
    m_messageReg.addImplicitMessagesFromUnits(m_elementReg, m_unitReg);
    m_messageReg.resolveUnits(m_unitReg);
    m_nodeReg.resolveAttributes(m_unitReg);
    m_nodeReg.resolveMessageTypes(m_messageReg);
  }
  //----------------------------------------------------------------------
  /**
   * Get the port to use for sending messages to this component. See
   * {@link #getRoute()} for important information about setting the
   * route and the port.
   */
  public CommsRoute getPort()
  {
    return m_port;
  }
  //----------------------------------------------------------------------
  /**
   * Set the port to use for sending message to this componet. See
   * {@link #getRoute()} for important information about setting the
   * route and the port.
   */
  public void setPort(CommsRoute port)
  {
    m_port = port;
  }
}
