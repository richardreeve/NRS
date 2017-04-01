package nrs.csl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** Represent a NRS/CSL variable element */
public class CSL_Element_Variable extends CSL_Element
{
  ///// Attributes

  private String m_name;
  private String m_displayName;
  private boolean m_selfUpdating;
  private boolean m_stateHolding;
  private String m_messageType;

  ///// Attribute Names

  /** XML attribute to specify the name */
  public static String ATTR_NAME = "name";

  /** XML attribute to specify the display name */
  public static String ATTR_DISPLAY_NAME = "displayName";

  /** XML attribute for 'selfUpdating' */
  public static String ATTR_SELF_UPDATING = "selfUpdating";

  /** XML attribute for 'stateHolding' */
  public static String ATTR_STATE_HOLDING = "stateHolding";

  /** XML attribute for 'messageType' */
  public static String ATTR_MESSAGE_TYPE = "messageType";

   ///// Sub-elements

  /** Sub-element Interfaces within this variable. Each element of the
      array is of type {@link nrs.csl.CSL_Element_Interface}/ */
  private ArrayList m_interfaces;

  /** Sub-element InitialValues within this variable. Each element of the
      array is of type {@link nrs.csl.CSL_Element_InitialValue}/ */
  private ArrayList m_initialValues;

  /** Message type of this variable */
  private CSL_Element_Message m_type;

  /** Sub-element Constraints within this variable. Each element of the
      array is of type {@link nrs.csl.CSL_Element_Constraint}/ */
  private ArrayList m_constraints;

 /** Name of the XML element to which this class corresponds. */
  public static String NAME = "Variable";

  //----------------------------------------------------------------------
  /** Constructor for prototype instance. */
  CSL_Element_Variable()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor */
  public CSL_Element_Variable(HashMap atts)
  {
    super(NAME);

    m_name = (String) atts.get(ATTR_NAME);

    // Set the display name, optional
    if (atts.containsKey(ATTR_DISPLAY_NAME))
      {
        m_displayName = (String) atts.get(ATTR_DISPLAY_NAME);
      }
    else
      {
        m_displayName = m_name;
      }

    m_selfUpdating =
      Boolean.valueOf((String) atts.get(ATTR_SELF_UPDATING)).booleanValue();

    m_stateHolding =
      Boolean.valueOf((String) atts.get(ATTR_STATE_HOLDING)).booleanValue();

    m_messageType = (String) atts.get(ATTR_MESSAGE_TYPE);

    // Create the ArrayLists
    m_interfaces = new ArrayList();
    m_initialValues = new ArrayList();
    m_constraints = new ArrayList();
  }
  //----------------------------------------------------------------------
  /** Return the name of this element, as specified by the value of the
   * name attribute of the corresponding XML element. */
  public String getVariableName() { return m_name; }
  //----------------------------------------------------------------------
  /** Return the value of the 'selfUpdating' attribute. */
  public boolean getSelfUpdating() { return m_selfUpdating; }
  //----------------------------------------------------------------------
  /** Return the value of the 'stateHolding' attribute. */
  public boolean getStateHolding() { return m_stateHolding; }
  //----------------------------------------------------------------------
  /** Return the value of the 'messageType' attribute. */
  public String getMessageType() { return m_messageType; }
  //----------------------------------------------------------------------
  /**
   * Return the message type
   */
  public CSL_Element_Message getType()
  {
    return m_type;
  }
  //----------------------------------------------------------------------
  /** Return an iterator over the list of Interfaces. The iterator
   * is used to access objects of type {@link
   * nrs.csl.CSL_Element_Interface}. */
  public Iterator getInterfaceIterator()
  {
    return m_interfaces.iterator();
  }
  //----------------------------------------------------------------------
  /** Return an iterator over the list of InitialValues. The iterator is
   * used to access objects of type {@link CSL_Element_InitialValue}. */
  public Iterator getInitialValuesIterator()
  {
    return m_initialValues.iterator();
  }
  //----------------------------------------------------------------------
  /** Return an iterator over the list of Constraints. The iterator
   * is used to access objects of type {@link
   * nrs.csl.CSL_Element_Constraint}. */
  public Iterator getConstraintsIterator()
  {
    return m_constraints.iterator();
  }
  //----------------------------------------------------------------------
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_Variable(atts);
  }
  //----------------------------------------------------------------------
  /** Accept sub-elements to a NodeDescription object. */
  public void addElement(CSL_Element element)
  {
    if (element instanceof CSL_Element_Interface)
      {
        m_interfaces.add(element);
      }
    else if (element instanceof CSL_Element_InitialValue)
      {
        m_initialValues.add(element);
      }
    else if (element instanceof CSL_Element_Constraint)
      {
        m_constraints.add(element);
      }
   else
     {
        // Invoke overridden method
        super.addElement(element);
      }
  }
  //----------------------------------------------------------------------
  /** Return the display name of this NodeDescription. */
  public String toString()
  {
    return m_displayName;
  }
  //----------------------------------------------------------------------
  void resolveMessageType(CSL_Message_Registry reg)
  {
    CSL_Element_Message type = reg.getMessage(m_messageType);

    if (type == null)
    {
      PackageLogger.log.warning("CSL variable " + getVariableName()
                                + " could not resolve its message type "
                                + m_messageType);
    }
    else
    {
      m_type = type;
    }
  }
}
