package nrs.pml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import nrs.core.base.AuxillaryInfo;
import nrs.core.base.Message;
import nrs.core.base.MessageProcessor;
import nrs.core.comms.CommsRoute;
import nrs.core.message.Constants;
import nrs.csl.CSL_Element_Registry;
import nrs.csl.CSL_Message_Registry;
import nrs.csl.CSL_Node_Registry;
import nrs.csl.CSL_Unit_Registry;
import nrs.csl.CSL_XMLEventProcessor;
import org.xml.sax.Attributes;

/** <p>Implement the element stack used when processing PML/XML input,
 * and dispatch constructed message objects ({@link
 * nrs.core.base.Message}) for both preprocessing and processing.
 *
 * @author Darren Smith
 **/
class PML_XMLEventProcessor
{
  private Stack m_stack;

  /** Registry of supported XML/PML elements */
  private PML_Element_Registry m_registry;

  /** Port message was received from; needed for getting the port number */
  private nrs.core.comms.CommsRoute m_srcPort;

  /** Object to deliver message too once it is ready */
  private MessageProcessor m_msgProcessor;

  /** The CSL processor which should be used when CSL is detected. */
  private CSL_XMLEventProcessor m_cslProc;

  /** List of the PML element names recognised */
  static private ArrayList m_pmlElements;

  /** Store the message being built in response to the latest XML
   * element processed */
  private Message m_message;

  CSL_Unit_Registry m_unitReg;
  CSL_Message_Registry m_msgReg;
  CSL_Node_Registry m_nodeReg;
  CSL_Element_Registry m_elementReg;

  // Used to flag whether a ReplyCSL message didn't contain any CSL data
  private boolean csl_not_present;

  //----------------------------------------------------------------------
  /** Constructor */
  public PML_XMLEventProcessor()
  {
    m_stack = new Stack();

    m_registry = PML_Element_Registry.getInstance();

    if (m_pmlElements == null)
    {
      m_pmlElements = new ArrayList();
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.AcknowledgeMessage);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.CreateLink);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.CreateNode);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.DeleteLink);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.DeleteNode);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.FailedRoute);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.QueryCID);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.QueryCSL);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.QueryCType);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.QueryConnectedCIDs);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.QueryLanguage);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.QueryMaxVNID);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.QueryNumberType);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.QueryRoute);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.QueryVNID);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.QueryVNName);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.QueryVNType);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.ReplyCID);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.ReplyCSL);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.ReplyCType);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.ReplyConnectedCIDs);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.ReplyLanguage);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.ReplyMaxVNID);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.ReplyNumberType);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.ReplyRoute);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.ReplyVNID);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.ReplyVNName);
      m_pmlElements.add(Constants.Namespace.NRS_PML +
                        Constants.MessageTypes.ReplyVNType);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Provide the {@link MessageProcessor} object to be used to
   * preprocess and process decoded messages
   *
   * @param portCallback the {@link nrs.core.comms.CommsRoute} port that
   * this message was received on. Thus there is a close relationship
   * between a {@link nrs.core.comms.CommsRoute} instance and an
   * instance of this class. When {@link Message} objects are generated
   * by this class, their received port auxillary data field is set to
   * the {@link nrs.core.comms.CommsRoute} provided here.
   */
  void setMessageCallbacks(nrs.core.comms.CommsRoute portCallback,
                           MessageProcessor msgProcessor)
  {
    m_srcPort = portCallback;
    m_msgProcessor = msgProcessor;
  }
  //----------------------------------------------------------------------
  /** Process occurrance of an XML event - character data*/
  public void characterData(String data)
  {
    if (m_cslProc != null)
    {
      m_cslProc.characterData(data);
      return;
    }

    m_stack.push(data);
  }
  //----------------------------------------------------------------------
  /** Process occurrance of an XML event - startElement */
  public void startElement(String URI, String name, Attributes atts)
  {
    //PackageLogger.log.fine("!!! XML(PML) start <"+ name + ">");

    if ((m_cslProc != null) && (m_cslProc.elementRecognised(URI, name)))
    {
      HashMap map = attributesToMap(atts);
      m_cslProc.startElement(URI, name, map);
      csl_not_present = false;
      return;
    }

    // Is the received element known to this class?
    //    if (!m_pmlElements.contains(URI+name))
    //    {
    //      PackageLogger.log.warning("XML(PML) start element <"
    //                                + name + " xmlns='"
    //                                + URI + "'> not recognised, ignored.");
    //    }
    //    else
    //    {

      if (m_message != null)
      {
        PackageLogger.log.warning("Processing XML(PML) element <"
                                  + name + "> while previous message"
                                  + " still present! Previous message will"
                                  + " be discarded");
      }

      // Create the new message object. It is dispatched upon receiving the
      // end element tag
      m_message = buildMessage(name, atts, null);

      // Handle special case of ReplyCSL message
      if (name.equals(Constants.MessageTypes.ReplyCSL))
      {
        m_unitReg = new CSL_Unit_Registry();
        m_msgReg = new CSL_Message_Registry();
        m_nodeReg = new CSL_Node_Registry();

        m_elementReg = new CSL_Element_Registry(m_unitReg,
                                                m_msgReg,
                                                m_nodeReg);

        csl_not_present = true;

        // Create a CSL processor, and assign it to class member. This will
        // now take over processing of XML callbacks
        m_cslProc = new CSL_XMLEventProcessor(m_elementReg);
      }
      //    }
  }
  //----------------------------------------------------------------------
  /** Process occurrance of an XML event - endElement.
   *
   * @throws Exception An exception is thrown if the endElement can
   * not be matched up with a start element. */
  public void endElement(String URI, String name) throws Exception
  {
    //PackageLogger.log.fine("!!! XML(PML) end <"+ name + ">");

    if ((m_cslProc != null) && (m_cslProc.elementRecognised(URI, name)))
    {
      m_cslProc.endElement(URI, name);
      return;
    }

    //    if (!m_pmlElements.contains(URI+name))
    //    {
    //      PackageLogger.log.warning("XML(PML) end element <"
    //                                + name + "> not recognised, ignored.");
    //      return;
    //    }

    if (m_message == null)
    {
      PackageLogger.log.warning("Processing XML(PML) close element <"
                                + name + "/> but no message ready!");
    }

    // Special treatment for ReplyCSL
    if ((m_cslProc != null) &&
        (m_message.getType().equals(Constants.MessageTypes.ReplyCSL)))
    {
      m_unitReg.addImplicitUnits(m_elementReg);
      m_msgReg.addImplicitMessagesFromUnits(m_elementReg, m_unitReg);

      if (!csl_not_present)
      {
        // registries only added if CSL was actually found
        m_message.aux().setUnitRegistry(m_unitReg);
        m_message.aux().setMessageRegistry(m_msgReg);
        m_message.aux().setNodeRegistry(m_nodeReg);
      }
      m_cslProc = null;
    }

    // Free up the class member variable for the next XML processing event
    Message localMessage = m_message;
    m_message = null;

    if (m_srcPort != null)
    {
      localMessage.aux().setReceivedPort(m_srcPort);
      m_srcPort.handleMessage(localMessage);
    }
    //if (m_msgProcessor != null) m_msgProcessor.handleMessage(localMessage);
    if ( m_msgProcessor != null ) m_msgProcessor.deliver(localMessage, null);
  }
  //----------------------------------------------------------------------
  /**
   * Convert an XML element tag and set of XML attributes into a raw
   * message, raw in the sense that the intelligent fields have not
   * received any processing. This routine sets both the message type
   * and the field name/value pairs.
   *
   * @param type the XML element name
   * @param atts the set of XML attributes belonging to the element
   * @param msg the {@link Message} instance to use for storing the name
   * and value pairs. Can be <tt>null</tt> in which case a new instance
   * is created; otherwise the contents of the existing message is
   * removed.
   *
   * @return the {@link Message} instance used to store the name and
   * value pairs
   */
  private Message buildMessage(String type, Attributes atts, Message msg)
  {
    if (msg == null)
    {
      msg = new Message(type);
    }

    msg.clear();
    msg.setType(type);
    msg.aux().setDirection(true);
    msg.aux().setReceivedPort(m_srcPort);

    for (int i = 0; i <  atts.getLength(); i++)
    {
      String URI = atts.getURI(i);
      String fieldname = atts.getLocalName(i);
      String fieldvalue = atts.getValue(i);

      //      System.out.println("!!! URI=" + URI);
      //      System.out.println("!!! field=" + fieldname);
      //      System.out.println("!!! value=" + fieldvalue);

      if (URI.equalsIgnoreCase(Constants.Namespace.NRSA))
      {
        msg.setNRSField(fieldname, fieldvalue);
      }
      else if ((URI.equalsIgnoreCase(Constants.Namespace.NRS_PML))
               || (URI.length() == 0))
      {
        // PML (default) namespace
        msg.setField(fieldname, fieldvalue);
      }
      else
      {
        PackageLogger.log.warning("Message '" + type + "' contains field '"
                                  + fieldname + "' that is not in a supported"
                                  + " namespace; ignoring");
      }
    }

//     for (Iterator i = atts.keySet().iterator(); i.hasNext(); )
//     {
//       String qName = (String) i.next();
//       String value = (String) atts.get(qName);
//       msg.setField(qName, value);
//       //      msg.setField(atts.getQName(i), atts.getValue(i));
//     }

    // PRINT OUT ATTRIBUTES
    //      System.out.println("getLocalName(i)=" + atts.getLocalName(i) +","
    //                         + "getQName(i)=" + atts.getQName(i) + ","
    //                         + "getType(i)=" + atts.getType(i) + ","
    //                         + "getURI(i)=" + atts.getURI(i) + ","
    //                         + "getValue(i)=" + atts.getValue(i));

    // PRINT OUT CONSTRUCTED MESSAGE
    //    System.out.println(msg.diagString());

    return msg;
  }
  //----------------------------------------------------------------------
  /** Determine whether the element named <code>localName</code> with
   * namespace <code>URI</code> is recognised. Ie it can be handled,
   * which means there is a class which corresponds to the element, and
   * that class has been correctly registered. */
  boolean elementRecognised(String URI, String localName)
  {
    if (URI.length() == 0)
    {
      PackageLogger.log.warning("XML element '" + localName
                                + "' defined without a namespace.");
    }
    return m_registry.elementSupported(URI, localName);
  }
  //----------------------------------------------------------------------
  /** Override the standard toString conversion to provide a debug style
   * output which outputs the present entries in the stack. */
  public String toString()
  {
    String retVal = "";

    for (int i = 0; i < m_stack.size(); i++)
    {
      retVal += m_stack.elementAt(i) + "\n";
    }

    return retVal;
  }
  //----------------------------------------------------------------------
  private HashMap attributesToMap(Attributes atts)
  {
    HashMap map = new HashMap();

    // Note: this method is very similar, but crucially different to, a
    // method in the CSL_ContentReader class
    for (int i = 0; i < atts.getLength(); i++)
      {
        map.put(atts.getQName(i), atts.getValue(i));
      }

    return map;
  }
  //   //----------------------------------------------------------------------
  //   /** Process all the XML entries that were found between a pair of
  //    * start and end elements. The top element in the stack is the one
  //    * which the following elements will be applied to.
  //    *
  //    * @return The CSL_Element at the top of <code>temp</code> after all
  //    * the other elements in the stack have been applied to it. If there
  //    * was a problem, the return value is null. */
  //   CSL_Element processTempStack(Stack temp)
  //   {
  //     if (temp.empty())
  //       {
  //         PackageLogger.log.warning("CSL temporary unwind stack is empty");

  //         return null;
  //       }

  //     Object o;
  //     o = temp.pop();

  //     if (o instanceof CSL_Element)
  //       {
  //         CSL_Element top = (CSL_Element) o;

  //         while (!temp.empty())
  //           {
  //             o = temp.pop();

  //             if (o instanceof CSL_Element)
  //               {
  //                 top.addElement((CSL_Element) o);
  //               }
  //             else if (o instanceof String)
  //               {
  //                 top.addCharacterData((String) o);
  //               }
  //             else
  //               {
  //                 PackageLogger.log.warning("Object found in CSL unwind stack is neither corresponding to a XML sub-element or character data. Object=" + o);
  //               }
  //           }

  //         return top;
  //       }
  //     else
  //       {
  //          PackageLogger.log.warning("Top Object in CSL temporary "
  //                        + "unwind stack is not a"
  //                        +" CSL_Element type. Object="
  //                        + o);
  //          return null;
  //       }
  //   }
  //   //----------------------------------------------------------------------
  //   /** Unwind the main stack (m_stack) until the element with the
  //    * specified name has been found. When unwinding, elements popped
  //    * off are pushed onto the temporary stack.
  //    *
  //    * @return whether the desired element was found. */
  //   boolean unwindStack(Stack temp, String eName)
  //   {
  //     boolean elementFound = false;
  //     Object o;
  //     CSL_Element el;

  //    while ((!m_stack.empty()) && (!elementFound))
  //       {
  //         o = m_stack.pop();
  //         temp.push(o);

  //         PackageLogger.log.finest("Pushing [" + o + "] onto unwind stack");

  //         // Is the popped object a CSL element? - if so we check
  //         // if its the one we are looking for.
  //         if (o instanceof CSL_Element)
  //           {
  //             el = (CSL_Element) o;
  //             elementFound = eName.equalsIgnoreCase(el.getElementName());
  //           }
  //       }

  //     return elementFound;
  //   }
}
