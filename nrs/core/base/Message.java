package nrs.core.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Iterator;
import nrs.core.message.Constants;

/**
 * <p>Represents an NRS message for communication between components and
 * variable/nodes.
 *
 * <p>Two kinds of fields are contained within a <tt>Message</tt>
 * object: NRS fields and default fields.
 *
 * <p>NRS fields are a small number of fields that are defined in the
 * reserved NRS-Attribute namespace. These fields include:
 * <tt>iForwardRoute</tt>, <tt>iHopCount</tt>, <tt>iIsBroadcast</tt>,
 * <tt>iReturnRoute</tt>, <tt>iSourceCID</tt>, <tt>iTargetCID</tt>,
 * <tt>iTargetVNName</tt>, <tt>iTranslationCount</tt>,
 * <tt>intelligent</tt>, <tt>route</tt> and <tt>toVNID</tt>.
 *
 * <p>Typically for user applications it is the default fields that will
 * be used.
 *
 * <p>To access NRS fields use the methods appropriate to them: {@link
 * #getNRSField(String)}, {@link #hasNRSField(String)} and {@link
 * #checkNRSField(String)}. To set NRS fields use {@link
 * #setNRSField(String, String)}.
 *
 * <p>To access the default fields, use the other methods: {@link
 * #getField(String)}, {@link #hasField(String)} and {@link
 * #checkField(String)}. To set default fields use {@link
 * #setField(String, String)}.
 *
 * Code can be written in a variety of ways to compose messages, making
 * use of methods from this class and from derived classes. The
 * following guideline can be followed to standardise on writing such
 * code. Adopting the guideline should result in less bugs, allow for
 * more quicker programming, and, most importantly, greatly enhance
 * readability.
 *
 * <h3>Guidelines for composing messages</h3>
 *
 * <ol>
 *
 * <li>Start by creating the required message type, or clearing an
 * existing message variable. Use <code>m</code> as the
 * <code>Message</code> variable. Eg: <br><br> <center><code>DeleteLink
 * m = new DeleteLink();</code></center> <br>
 *
 * <li>Use a local variable for each default message field that will be
 * set. Give such variables the same names as the default fields. One
 * pitfall to avoid is static-importing the
 * <tt>Constant.MessageFields</tt> constants. Don't import them, since
 * they would clash with your local variables; however, you should
 * static-import the required <tt>Constant.MessageTypes</tt>
 * constants.</li>
 *
 * <li>Set these local variables in the order in which the message
 * fields appear in the <i>NRS 2.0 Design Document</i> (this order is
 * repeated in the constructor for message classes). During this stage
 * you can also start adding any intelligent message fields that maybe
 * needed.</li>
 *
 * <li>If appropraite values for message fields can't be determined,
 * throw a {@link MessageException}</li>
 *
 * <li>Try to use the static methods provided by the {@link
 * MessageTools} class. This class provides a variety of methods for
 * adding and fixing routing and addressing fields. For example, it has
 * a method to add all the fields needed to make your message a
 * broadcast.</li>
 *
 * <li>Make a stereotypical call to the <code>setFields</code> method of
 * your message object. Basically just pass in all of the local
 * variables that you earlier created and set (see example below). </li>
 *
 * <li>Now attempt to <i>fix</i> any message fields. This means calling
 * {@link MessageTools} methods to add appropriate intelligent message
 * fields for routing and addressing if the standard NRS fields are
 * empty or missing.</li>
 *
 * <li>Your message is now complete. Either submit to an outbound
 * pipeline, or return to a caller, etc.  </li>
 *
 * </ol>

 <h4>Example</h4>
<table>
<tr>
<td>
<br>
 <code>
    <i>// Create message</i><br>
    DeleteLink m = new DeleteLink();<br>
<br>
    <i>// Determine routing information</i><br>
    String route = link.getSource().getParent().getComponent().getRoute();<br>
<br>
    <i>// Determine addressing - use intelligent addressing</i><br>
    MessageTools.addIntelligentAddressing(m, DeleteLink);<br>
    int toVNID = 0;<br>
<br>
    <i>// Determine the cid and vnid of link source</i><br>
    String cid = link.getSource().getParent().getComponent().getCID();<br>
    if (!link.getSource().remote().isKnown())<br>
    {<br>
    &nbsp;&nbsp;throw new MessageException(Constants.MessageFields.vnid,<br>
     &nbsp;&nbsp;                            m.getType(),<br>
     &nbsp;&nbsp;                            "VNID not known for source node");<br>
    }<br>
    int vnid = link.getSource().remote().VNID();<br>
<br>
    <i>// Determine the cid and vnid of link target</i><br>
    String targetCID = link.getTarget().getParent().getComponent().getCID();<br>
    if (!link.getTarget().remote().isKnown())<br>
    {<br>
      &nbsp;&nbsp;throw new MessageException(Constants.MessageFields.targetVNID,<br>
                             &nbsp;&nbsp;     m.getType(),<br>
                             &nbsp;&nbsp;    "VNID not known for target node");<br>
    }<br>
    int targetVNID = link.getTarget().remote().VNID();<br>
<br>
    <i>// Other fields</i><br>
    boolean sourceNotTarget = true;<br>
    String logPort = "";<br>
<br>
    <i>// Set all fields</i><br>
    m.setFields(route,<br>
        &nbsp;&nbsp;        toVNID,<br>
        &nbsp;&nbsp;        sourceNotTarget,<br>
       &nbsp;&nbsp;         cid,<br>
        &nbsp;&nbsp;        vnid,<br>
        &nbsp;&nbsp;        targetCID,<br>
        &nbsp;&nbsp;        targetVNID,<br>
        &nbsp;&nbsp;        logPort);<br>
<br>
    <i>// Fixes</i><br>
    MessageTools.fixRoute(m,<br>
      &nbsp;&nbsp;   link.getSource().getParent().getComponent().getCID());<br>
<br>
    <i>// Send to source</i><br>
    if (m_outPipeline != null) m_outPipeline.submit(m);<br>
<br>
</code>

</td>
</tr>
</table>



 * @author Darren Smith
 */
public class Message
{
  /** Character encoding to use whenever such a parameter is
      necessary. The current design choice is to use 7-bit ASCII, which
      contains 128 characters. Do 'man ascii' on Linux for more info. */
  public static final String ENCODING = "US-ASCII";

  /** Byte array representing the namespace attributes and value to be
   * inserted into each PML tag. */
  private static final ByteArrayOutputStream NAMESPACE
    = new ByteArrayOutputStream();

  static
  {
    String namespace = new String(" xmlns:nrsa='" +
                                  Constants.Namespace.NRSA + "'"
                                  + " xmlns='" + Constants.Namespace.NRS_PML
                                  + "'");
    try
    {
      NAMESPACE.write(namespace.getBytes(ENCODING));
    }
    catch (java.io.UnsupportedEncodingException e)
    {
      PackageLogger.log.severe("Charset " + ENCODING + " not supported, " + e);
      e.printStackTrace();
    }
    catch (java.io.IOException e)
    {
      PackageLogger.log.severe("Failure to write NAMESPACE byte array, " + e);
      e.printStackTrace();
    }
  }


  /** Byte array storing a whitespace delimitor (ASCII 32) for use in
   * constructing XML strings */
  private static final byte[] SPACE = {32};

  /** Byte array storing the colon [:] which separates an XML
   * attribute name and its namespace qualifier */
  private static final byte[] ATTR_QUAL_DELIM = {58};

  /** Byte array storing the two character [='] which separates an XML
   * attribute name and value pair */
  private static final byte[] NAME_VALUE_DELIM = {61, 39};

  /** Byte array storing the singlecharacter ['] which terminates an XML
   * attribute name and value pair */
  private static final byte[] NAME_VALUE_TERM = {39};

  /** Byte array storing the single character message terminator */
  private static final byte[] MESSAGE_TERM = {0};

  /** Byte representation of 'true' - excluding ticks */
  private static final byte[] TRUE = {116, 114, 117, 101};

  /** Byte representation of 'false' - excluding ticks */
  private static final byte[] FALSE = {102, 97, 108, 115, 101};

  /** Map of PML namespace message fieldnames and values */
  private HashMap m_fields = new HashMap();

  /** Map of NRS-Attribute namespace fieldnames and values. */
  private HashMap m_NRSfields = new HashMap();

  /** Type of this message type */
  private String m_type;

  /** Auxillarly fields */
  private AuxillaryInfo m_aux = new AuxillaryInfo();


  //----------------------------------------------------------------------
  /**
   * Constructor. This default constructor does not set the message
   * type, so a later call to {@link #setType(String)} should be made.
   *
   */
  public Message()
  {
    m_type = null;
  }
  //----------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param type the type of this message, such as <tt>ReplyCID</tt/ etc
   */
  public Message(String type)
  {
    m_type = type;
  }
  //----------------------------------------------------------------------
  /**
   * Provide a PML representation of the current message.
   *
   * @param buf the {@link ByteArrayOutputStream} where the PML representation
   * will be stored
   *
   * @param appendNull if <tt>true</tt> the messasge terminator byte
   * will be appended to the PML representation
   *
   * @throws UnsupportedEncodingException if fixed string content could
   * not be decoded into a byte a sequence, according to the character
   * code set described by {@link #ENCODING}.
   *
   * @throws IOException if an IO error occurs when operating on the
   * byte arrays
   */
  public void getPML(ByteArrayOutputStream buf, boolean appendNull)
    throws UnsupportedEncodingException, IOException
  {
    appendString("<", buf);
    appendString(m_type,buf);
    appendNamespace(buf);
    getFieldsInPML(buf);
    appendString(" />", buf);

    if (appendNull) appendMessageTerminator(buf);
  }
  //----------------------------------------------------------------------
  /**
   * Appends to a PML buffer the "xmlns" and "xmlns:nrsa"
   * attributes. Spaces are added before and after the appended text.
   *
   * @throws IOException if an IO error occurs
   */
  protected void appendNamespace(ByteArrayOutputStream pmlBuf)
    throws IOException
  {
    NAMESPACE.writeTo(pmlBuf);
  }
//   //----------------------------------------------------------------------
//   /**
//    * Tells whether this is an intelligent message. An intelligent
//    * message is one that contains at least one intelligent field.
//    *
//    * @return <tt>true</tt> if the message is intelligent, <tt>false</tt>
//    * otherwise
//    */
//   public boolean isIntelligent()
//   {
//     return false;
//   }
  //----------------------------------------------------------------------
  /**
   * Append a fixed string sequence to the PML message being construced.
   *
   * @throws UnsupportedEncodingException if the data can't be decoded
   * into a byte a sequence, according to the character code set
   * described by {@link #ENCODING}.
   */
  protected void appendString(String s,
                              ByteArrayOutputStream pmlBuf)
    throws UnsupportedEncodingException
  {
    byte[] sBuf = s.getBytes(ENCODING);

    pmlBuf.write(sBuf, 0, sBuf.length);
  }
  //----------------------------------------------------------------------
  /**
   * Append an XML URI & attribute and value to a PML messge being
   * constructed. Spaces are added before the appended text.
   *
   * @param attributeQualifier the namespace to attach before outputing
   * the attribute name. It should not include the ":" delimator.
   *
   * @param name the name of the attribute
   *
   * @param value the value of the attribute
   *
   * @param pmlBuf the buffer into which the byte representation of the
   * attribute entry will be written
   *
   * @throws UnsupportedEncodingException if the data can't be decoded
   * into a byte a sequence, according to the character code set
   * described by {@link #ENCODING}.
   */
  protected void appendAttribute(String attributeQualifier,
                                 Object name,
                                 Object value,
                                 ByteArrayOutputStream pmlBuf)
    throws UnsupportedEncodingException
  {
    byte[] qual = attributeQualifier.getBytes(ENCODING);
    byte[] nameBuf = name.toString().getBytes(ENCODING);
    byte[] valueBuf = value.toString().getBytes(ENCODING);

    pmlBuf.write(SPACE, 0, 1);
    pmlBuf.write(qual, 0, qual.length);
    pmlBuf.write(ATTR_QUAL_DELIM, 0, 1);
    pmlBuf.write(nameBuf, 0, nameBuf.length);
    pmlBuf.write(NAME_VALUE_DELIM, 0, 2);
    pmlBuf.write(valueBuf, 0, valueBuf.length);
    pmlBuf.write(NAME_VALUE_TERM, 0, 1);
  }
  //----------------------------------------------------------------------
  /**
   * Append an XML attribute and value to a PML messge being
   * constructed. Spaces are added before the appended text.
   *
   * @param name the name of the attribute
   *
   * @param value the value of the attribute
   *
   * @param pmlBuf the buffer into which the byte representation of the
   * attribute entry will be written
   *
   * @throws UnsupportedEncodingException if the data can't be decoded
   * into a byte a sequence, according to the character code set
   * described by {@link #ENCODING}.
   */
  protected void appendAttribute(Object name,
                                 Object value,
                                 ByteArrayOutputStream pmlBuf)
    throws UnsupportedEncodingException
  {
    byte[] nameBuf = name.toString().getBytes(ENCODING);
    byte[] valueBuf = value.toString().getBytes(ENCODING);

    pmlBuf.write(SPACE, 0, 1);
    pmlBuf.write(nameBuf, 0, nameBuf.length);
    pmlBuf.write(NAME_VALUE_DELIM, 0, 2);
    pmlBuf.write(valueBuf, 0, valueBuf.length);
    pmlBuf.write(NAME_VALUE_TERM, 0, 1);
  }
  //----------------------------------------------------------------------
  /**
   * Append an XML attribute and value to a PML messge being
   * constructed. Spaces are added before the appended text.
   *
   * @throws UnsupportedEncodingException if the data can't be decoded
   * into a byte a sequence, according to the character code set
   * described by {@link #ENCODING}.
   */
  protected void appendAttribute(String name,
                                 String value,
                                 ByteArrayOutputStream pmlBuf)
    throws UnsupportedEncodingException
  {
    byte[] nameBuf = name.getBytes(ENCODING);
    byte[] valueBuf = value.getBytes(ENCODING);

    pmlBuf.write(SPACE, 0, 1);
    pmlBuf.write(nameBuf, 0, nameBuf.length);
    pmlBuf.write(NAME_VALUE_DELIM, 0, 2);
    pmlBuf.write(valueBuf, 0, valueBuf.length);
    pmlBuf.write(NAME_VALUE_TERM, 0, 1);
  }
  //----------------------------------------------------------------------
  /**
   * Append the message terminator character to a completed message
   *
   * @throws IOException if there was a problem writing to the {@link
   * ByteArrayOutputStream}
   */
  protected void appendMessageTerminator(ByteArrayOutputStream pmlBuf)
  {
    pmlBuf.write(MESSAGE_TERM, 0, 1);
  }
  //----------------------------------------------------------------------
  /**
   * Encode message fields in NRSA
   *
   * @throws UnsupportedEncodingException if the data can't be decoded
   * into a byte a sequence, according to the character code set
   * described by {@link #ENCODING}.
   */
  protected void getFieldsInPML(ByteArrayOutputStream pmlBuf)
    throws UnsupportedEncodingException
  {
    // First add NRS-Attribute namespace fields
    Iterator keys = m_NRSfields.keySet().iterator();
    Iterator values = m_NRSfields.values().iterator();

    while (keys.hasNext())
    {
      Object name = Constants.Namespace.NRSA_qualifier + ":" + keys.next();
      Object value = values.next();

      if (value == null)
      {
        PackageLogger.log.warning("Field " + name + " has null value;"
                                  +" providing an empty string");
        value = new String();
      }
      appendAttribute(name, value, pmlBuf);
    }

    // Second, add PML namespace fields
    keys = m_fields.keySet().iterator();
    values = m_fields.values().iterator();

    while (keys.hasNext())
    {
      Object name = keys.next();
      Object value = values.next();

      if (value == null)
      {
        PackageLogger.log.warning("Field " + name + " has null value;"
                                  +" providing an empty string");
        value = new String();
      }
      appendAttribute(name, value, pmlBuf);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Removes all message fields and removes any {@link AuxillaryInfo},
   * replacing it with a new instance.
   */
  public void clear()
  {
    m_fields.clear();
    m_NRSfields.clear();
    m_aux = new AuxillaryInfo();
  }
  //----------------------------------------------------------------------
  /**
   * <p>Enter a qualified fieldname and its value into the
   * NRS-Attribute namespace.
   *
   * <p>Note: if an intelligent field is being added then remember to
   * also add the intelligent-message indicator field.</p>
   *
   * @param fieldname the name of the field
   *
   * @param value the field value - should not be passed <tt>null</tt>
   */
  public void setNRSField(String fieldname, String value)
  {
    m_NRSfields.put(fieldname, value);
  }
  //----------------------------------------------------------------------
  /**
   * <p>Enter a qualified fieldname and its value into the (default)
   *  PML namespace.
   *
   * <p>Note: intelligent fields, <tt>route</tt> and <tt>toVNID</tt>
   * should be added as an NRSField (@see{setNRSField(String, String)})</p>
   *
   * @param fieldname the name of the field
   *
   * @param value the field value - should not be passed <tt>null</tt>
   */
  public void setField(String fieldname, String value)
  {
    m_fields.put(fieldname, value);
  }
  //----------------------------------------------------------------------
  /**
   * Returns the value of the field in the (default) PML namespace, or
   * <tt>null</tt> if the field is not found.
   *
   * @see nrs.core.message.Constants
   */
  public String getField(String fieldname)
  {
    return (String) m_fields.get(fieldname);
  }
  //----------------------------------------------------------------------
  /**
   * Returns the value of the field in the NRS-Attribute namespace, or
   * <tt>null</tt> if the field is not found.
   *
   * @see nrs.core.message.Constants
   */
  public String getNRSField(String fieldname)
  {
    return (String) m_NRSfields.get(fieldname);
  }
  //----------------------------------------------------------------------
  /**
   * Return <tt>true</tt> if the field exists for the (default) PML
   * namespace in this message, or <tt>false</tt> otherwise
   *
   * @see #checkField(String fieldname)
   * @see nrs.core.message.Constants
   */
  public boolean hasField(String fieldname)
  {
    return m_fields.containsKey(fieldname);
  }
  //----------------------------------------------------------------------
  /**
   * Return <tt>true</tt> if the field exists for the NRS-Attribute
   * namespace in this message, or <tt>false</tt> otherwise
   *
   * @see #checkNRSField(String fieldname)
   * @see nrs.core.message.Constants
   */
  public boolean hasNRSField(String fieldname)
  {
    return m_NRSfields.containsKey(fieldname);
  }
  //----------------------------------------------------------------------
  /**
   * Set the message type
   */
  public void setType(String type)
  {
    m_type = type;
  }
  //----------------------------------------------------------------------
  /**
   * Return the message type
   */
  public String getType()
  {
    return m_type;
  }
  //----------------------------------------------------------------------
  /** Return a useful diagnostic string, which includes the messasge
   * type and list of the fieldname and value pairs.
   */
  public String diagString()
  {
    StringBuffer sb = new StringBuffer();

    if (m_type == null)
    {
      sb.append("< ??null??");
    }
    else
    {
      sb.append("<" + m_type);
    }

    sb.append(NAMESPACE);

    Iterator keys = m_NRSfields.keySet().iterator();
    Iterator values = m_NRSfields.values().iterator();

    while (keys.hasNext())
    {
      sb.append(" ");
      sb.append(Constants.Namespace.NRSA_qualifier);
      sb.append(":");
      sb.append(keys.next());
      sb.append("='");
      sb.append(values.next());
      sb.append("'");
    }

    keys = m_fields.keySet().iterator();
    values = m_fields.values().iterator();

    while (keys.hasNext())
    {
      sb.append(" ");
      sb.append(keys.next());
      sb.append("='");
      sb.append(values.next());
      sb.append("'");
    }
    sb.append(" />");

    return sb.toString();
  }
  //----------------------------------------------------------------------
  /**
   * Access the auxillary information
   */
  public AuxillaryInfo aux()
  {
    return m_aux;
  }
  //----------------------------------------------------------------------
  /**
   * Return the message type
   */
  public String toString()
  {
    return m_type;
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to locate return route information from the message
   * fields. First the field {@link
   * nrs.core.message.Constants.MessageFields#iReturnRoute} is seached
   * for, and its value is returned if found. Otherwise the value of the
   * {@link nrs.core.message.Constants.MessageFields#returnRoute} is
   * returned. If neither field is found then <tt>null</tt> is returned.
   *
   * @return the return-route string to use, or <tt>null</tt> if none
   * could be found.
   */
  public String getReturnRoute()
  {
    String route = getNRSField(nrs.core.message.Constants.MessageFields.iReturnRoute);

    if (route == null)
    {
       route = getField(nrs.core.message.Constants.MessageFields.returnRoute);
    }

    return route;
  }
  //----------------------------------------------------------------------
  /**
   * Set the {@link nrs.core.message.Constants.MessageFields#route}
   * field by using the {@link #getReturnRoute()} method to extract a
   * return-route from the {@link Message} provided.
   */
  public void setRoute(Message query)
  {
    String route = query.getReturnRoute();
    m_NRSfields.put(Constants.MessageFields.route, route);
  }
  //----------------------------------------------------------------------
  /**
   * Set the {@link nrs.core.message.Constants.MessageFields#replyMsgID}
   * field by copying the value from the {@link
   * nrs.core.message.Constants.MessageFields#msgID} in the {@link
   * Message} provided. If <tt>query</tt> doesn't contain the message ID
   * field, then a warning is logged and a empty reply field inserted.
   */
  public void setReplyMsgID(Message query)
    {   //tf - fixed bug - modified to
        //query.hasField(nrs.core.message.Constants.MessageFields.msgID)
        //instead of
        //hasField(nrs.core.message.Constants.MessageFields.msgID)
      if (query.hasField(nrs.core.message.Constants.MessageFields.msgID))
    {
      m_fields.put(nrs.core.message.Constants.MessageFields.replyMsgID,
                   query.getField(nrs.core.message.Constants.MessageFields.msgID));
    }
    else
    {
      PackageLogger.log.warning("No " + nrs.core.message.Constants.MessageFields.msgID
                                + " field found in " + query
                                + ": setting empty "
                                + nrs.core.message.Constants.MessageFields.replyMsgID);

      m_fields.put(nrs.core.message.Constants.MessageFields.replyMsgID, "");
    }
  }
  //----------------------------------------------------------------------
  /**
   * Checks if the field exists for the (default) PML namespace in this
   * message, throwing an exception if the field is not found
   *
   * @see #hasField(String fieldname)
   * @see nrs.core.message.Constants
   *
   * @return the value of the field in the (default) PML namespace
   */
  public String checkField(String fieldname) throws FieldNotFoundException
  {
    if (!m_fields.containsKey(fieldname))
    {
      throw new FieldNotFoundException(fieldname);
    }

    return (String) m_fields.get(fieldname);
  }
  //----------------------------------------------------------------------
  /**
   * Checks if the field exists for the NRS-Attribute namespace in this
   * message, throwing an exception if the field is not found
   *
   * @see #hasNRSField(String fieldname)
   * @see nrs.core.message.Constants
   *
   * @return the value of the field in the NRS-Attribute namespace
   */
  public String checkNRSField(String fieldname) throws FieldNotFoundException
  {
    if (!m_NRSfields.containsKey(fieldname))
    {
      throw new FieldNotFoundException(fieldname
                                       + " (in NRS-Attribute namespace) ");
    }

    return (String) m_NRSfields.get(fieldname);
  }

  //--------------------------------------------------------------------
  /**
   * Return a HashMap of all the NRS-Attribute namespace field and value
   * pairs
   */
  public HashMap getNRSFields(){
    HashMap h = new HashMap();

    // Add NRS-Attribute namespace fields
    Iterator keys = m_NRSfields.keySet().iterator();
    Iterator values = m_NRSfields.values().iterator();

    while (keys.hasNext())
    {
      Object name = Constants.Namespace.NRSA_qualifier + ":" + keys.next();
      Object value = values.next();

      if (value == null)
      {
        PackageLogger.log.warning("Field " + name + " has null value;"
                                  +" providing an empty string");
        value = new String();
      }
      h.put(name, value);
    }

    return h;
  }
  //----------------------------------------------------------------------
  /**
   *  Return a HashMap of all the default namespace field and value pairs
   */
  public HashMap getFields()
  {
    HashMap h = new HashMap();

    // PML namespace fields
    Iterator keys = m_fields.keySet().iterator();
    Iterator values = m_fields.values().iterator();

    while (keys.hasNext())
    {
      Object name = keys.next();
      Object value = values.next();

      if (value == null)
      {
        PackageLogger.log.warning("Field " + name + " has null value;"
                                  +" providing an empty string");
        value = new String();
      }
      h.put(name, value);
    }

    return h;
  }
  //----------------------------------------------------------------------
  /**
   * Return a HashMap of all the genuine data fields (i.e. ones that are
   * not part of the messaging protocal).
   *
   * NOTE: this is a hack!
   */
  public HashMap<String, String> getPayload()
  {
    HashMap retVal = getFields();

    try
    {
      Class c = Class.forName("nrs.core.message.Constants$Fields");
     
      Field fields[] = c.getFields();

      for (int i = 0; i < fields.length; i++)
      {
        retVal.remove(fields[i].get(null).toString());
      }
    }
    catch (ClassNotFoundException cnde)
    {
      PackageLogger.log.severe("Java reporting the nrs.core.message.Constants.Fields class is not found! This is an important base class. If it has gone missing, this is likely because of a recent redesign, in which case the getPayload() method in Message.java also needs updating");
      cnde.printStackTrace();
    }
    catch (IllegalAccessException iae)
    {
      PackageLogger.log.severe("Java reporting the nrs.core.message.Constants.Fields class is reporting illegal access! This is an important base class. If it has gone missing, this is likely because of a recent redesign, in which case the getPayload() method in Message.java also needs updating");
      iae.printStackTrace();
    }

    return retVal;
  }
}
