package nrs.csl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import nrs.core.message.Constants;

/** Register the various NRS message types defined in CSL input */
public class CSL_Message_Registry
{
  /** Set of pairs, each consisting of a message name and an instance of
      {@link CSL_Element_Unit}. */
  HashMap m_map;

  // Class logger
  private Logger m_log = Logger.getLogger("nrs.csl");

  /** The source CID of the NRS component which provided the messages */
  private String m_sourceCID;

  //----------------------------------------------------------------------
  /** Constructor for when CID is not known */
  public CSL_Message_Registry()
  {
    m_map = new HashMap();
    m_sourceCID = "";
  }
  //----------------------------------------------------------------------
  /** Constructor for when CID is known */
  public CSL_Message_Registry(String sourceCID)
  {
    m_map = new HashMap();
    m_sourceCID = sourceCID;
  }
  //----------------------------------------------------------------------
  /** Add a new message type to the registry. */
  public void add(CSL_Element_Message eMessage)
  {
    if (m_map.containsKey(eMessage.getMessageName()))
      {
        Logger.getLogger("nrs.csl")
          .warning("Ignoring attempt to add message-type '"
                   + eMessage.getMessageName()
                   + "' to registry since it already exists.");
      }
    else
      {
        m_map.put(eMessage.getMessageName(), eMessage);
      }
  }
  //----------------------------------------------------------------------
  /** This attempts to create all possible <i>implicit messages</i> and
   * add them to the message registry (ie self). An implicit message is
   * a simple message which contains a single message segment. The name
   * and unit attributes of that single segment, and also the name of
   * the message itself, are all copied from the name of the source unit
   * (refer to the section for "Types, units and messages" in the design
   * document). Implicit messages can be conceptually divided into two
   * categories: (1) those derived from the implicit units (<i>e.g.</i>
   * number, string, boolean <i>etc</i>); and (2) those derived from
   * user (<i>i.e.</i> CSL) specified units (<i>e.g.</i> Voltage,
   * Conductance <i>etc</i>). This method will attempt to create both
   * sets of implicit messages. This is the difference between this
   * method and the deprecated {@link
   * #addImplicitMessages(CSL_Element_Registry)} - the latter only
   * creates and add the first kind of implicit messages. Note that
   * implicit messages are not created if a message of the same name
   * already exists within the registry.
   */
  public void addImplicitMessagesFromUnits(CSL_Element_Registry elementReg,
                                           CSL_Unit_Registry unitReg)
  {
    // Loop over every unit defined in the unit registry...
    for (Iterator it = unitReg.getUnitIterator(); it.hasNext(); )
      {
        CSL_Element_Unit unit = (CSL_Element_Unit) it.next();
        addImplicitMessage(unit.getUnitName(), elementReg);
      }
  }
  //----------------------------------------------------------------------
  /** Add the implicit messasge types to the registry. The
   * <code>elementReg</code> is used to construct each message. The
   * implicit messages are those which simply encapsule the basic types
   * without adding any further meaning.
   *
   * <br/>This method is now <b>deprecated</b>. Use instead
   * addImplicitMessagesFromUnits(...). This latter method will attempt
   * to add all implicit messages, which includes those derived from the
   * implicit units (<i>i.e.</i>, those derive from the basic-types),
   * and those derived from user-defined units (<i>i.e.</i>, those
   * described in CSL input).
   *
   * @deprecated use instead {@link #addImplicitMessagesFromUnits(CSL_Element_Registry,CSL_Unit_Registry)} */
  public void addImplicitMessages(CSL_Element_Registry elementReg)
  {
    addImplicitMessage(CSL_BasicType_Float.NAME, elementReg);
    addImplicitMessage(CSL_BasicType_String.NAME, elementReg);
    addImplicitMessage(CSL_BasicType_Boolean.NAME, elementReg);
    addImplicitMessage(CSL_BasicType_Integer.NAME, elementReg);
    addImplicitMessage(CSL_BasicType_Route.NAME, elementReg);
  }
  //----------------------------------------------------------------------
  /** Add a single implicit message type. The name of the message, and
   * its unit, will be assigned from the 'basicType' parameter. There
   * are two kind of implicit unit: those set from the basic-type-units,
   * and those set from user-defined-units. If the message already
   * exists in the registry, this routine makes no changes - i.e., a new
   * message is not added. */
  void addImplicitMessage(String basicType,
                                  CSL_Element_Registry elementReg)
  {
    // Check whether the message registry already contains a message of
    // the specified name. If so, immediately exit.
    if (getMessage(basicType) != null)
      {
       m_log.fine("Aborting request to add implicit message for unit='"
                  + basicType + "', because that message has already" +
                  " been defined - so implicit message not needed.");
       return;
      }

    HashMap atts = new HashMap();
    atts.put(CSL_Element_Unit.ATTR_NAME, basicType);

    // This message is implicit, so we add an appropriate indicator into
    // the list of attributes.
    atts.put(CSL_Element_Message.INT_ATTR_IMPLICIT, Boolean.toString(true));

    // Create the message
    CSL_Element newMessage
      = elementReg.buildElement(Constants.Namespace.NRS_CSL,
                                CSL_Element_Message.NAME,
                                atts);

    // Now give this new message a single segment, which also must be
    // constructed
    atts.clear();
    atts.put(CSL_Element_Segment.ATTR_NAME, basicType);
    atts.put(CSL_Element_Segment.ATTR_UNIT, basicType);

    // Create the segment
    CSL_Element newSegment
      = elementReg.buildElement(Constants.Namespace.NRS_CSL,
                                CSL_Element_Segment.NAME,
                                atts);

    // Now add this segment to the new message
    newMessage.addElement(newSegment);

    // Finally add the new unit just created to the internal list
    add((CSL_Element_Message) newMessage);

    m_log.fine("Added implicit message with name='"
               + basicType
               + "'");
  }
  //----------------------------------------------------------------------
  /** Get a message by name, or return null if no message with the specified
   * name is found in the registry. */
  public CSL_Element_Message getMessage(String name)
  {
    return (CSL_Element_Message) m_map.get(name);
  }
  //----------------------------------------------------------------------
  /**
   * Instruct all message objects within the registry to resolve their
   * reference to CSL units. This means each message within the registry
   * will try to get a reference to an actual {@link CSL_Element_Unit},
   * instead of just holding the name to the unit. The list of units
   * available for the process is supplied through the {@link
   * CSL_Unit_Registry} parameter.
   */
  public void resolveUnits(CSL_Unit_Registry unitReg)
  {
    Iterator it = m_map.values().iterator();

    while (it.hasNext())
      {
        CSL_Element_Message e = (CSL_Element_Message) it.next();
        e.resolveUnits(unitReg);
      }
  }
  //----------------------------------------------------------------------
  /** Return an iterator over the messages within the registry. */
  public Iterator getMessageIterator()
  {
    return m_map.values().iterator();
  }
  //----------------------------------------------------------------------
  public String toString()
  {
    String retVal = "Message Registry contents (" + m_map.size() + "):\n";

    Iterator it = m_map.values().iterator();
    while (it.hasNext())
      {
        CSL_Element_Message eu = (CSL_Element_Message) it.next();
        retVal += eu.getMessageName() + "\n";
     }

    return retVal;
  }
  //----------------------------------------------------------------------
  /**
   * Return the CID of the NRS component which is the source for the
   * messages in this registry.
   */
  public String getSourceCID()
  {
    return m_sourceCID;
  }
  //----------------------------------------------------------------------
  /**
   * Set the CID of the NRS component which is the source for the
   * units in this registry.
   */
  public void setSourceCID(String cid)
  {
    m_sourceCID = cid;
  }
}
