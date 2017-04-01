package nrs.csl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Represent a NRS/CSL node-description element */
public class CSL_Element_NodeDescription extends CSL_Element
{
  ///// Attributes

  private String m_name;
  private String m_displayName;
  private boolean m_rootNode;
  private int m_rootMinOccurs;
  private int m_rootMaxOccurs;

  ///// Attribute Names

  /** XML attribute to specify the name */
  public static String ATTR_NAME = "name";

  /** XML attribute to specify the display name */
  public static String ATTR_DISPLAY_NAME = "displayName";

  /** XML attribute for 'rootNode' */
  public static String ATTR_ROOT_NODE = "rootNode";

  /** XML attribute for 'rootMinOccurs' */
  public static String ATTR_ROOT_MIN_OCCURS = "rootMinOccurs";

  /** XML attribute for 'rootMacOccurs' */
  public static String ATTR_ROOT_MAX_OCCURS = "rootMaxOccurs";

  public static String ATTRVAL_UNBOUNDED = "unbounded";

  ///// Sub-elements

  /** Sub-element description within this message. */
  private CSL_Element_Description m_desc;

  /** Sub-element InGroups within this node. Each element of the array
      is of type {@link CSL_Element_InGroup}. */
  private ArrayList m_inGroups;

  /** Sub-element Attributes within this node. Each element of the array
      is of type {@link CSL_Element_Attribute}. */
  private ArrayList m_attributes;

  /** Sub-element Contains within this node. Each element of the
      array is of type {@link CSL_Element_Contains}. */
  private ArrayList m_contains;

  /** Sub-element Variables within this node. Each element of the
      array is of type {@link CSL_Element_Variable}. */
  private ArrayList m_variables;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "NodeDescription";

  /** The CID of the component which provides this element */
  private String m_sourceCID;

  //----------------------------------------------------------------------
  /** Constructor for prototype instance. */
  CSL_Element_NodeDescription()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor */
  public CSL_Element_NodeDescription(Map atts)
  {
    super(NAME);

    // mandatory
    m_name = (String) atts.get(ATTR_NAME);
    if (m_name == null)
    {
      PackageLogger.log.warning(NAME
                                + " element encountered without a name."
                                +" Ensure the"
                                + " mandatory XML attribute \"" + ATTR_NAME
                                + "\" was specified.");
      m_name ="UNKNOWN";
    }

    // Set the display name, optional
    if (atts.containsKey(ATTR_DISPLAY_NAME))
      {
        m_displayName = (String) atts.get(ATTR_DISPLAY_NAME);
      }
    else
      {
        m_displayName = m_name;
      }

    // optional - default is false
    m_rootNode = false;
    if (atts.containsKey(ATTR_ROOT_NODE))
      {
        m_rootNode =
          Boolean.valueOf((String) atts.get(ATTR_ROOT_NODE)).booleanValue();
      }

    // Extract the rootMinOccurs number; optional, default is 0
    m_rootMinOccurs = 0;
    String temp = (String) atts.get(ATTR_ROOT_MIN_OCCURS);
    if (temp != null)
    {
      if (temp.equalsIgnoreCase(ATTRVAL_UNBOUNDED))
      {
        m_rootMinOccurs = -1;
      }
      else
      {
        try
        {
          m_rootMinOccurs = Integer.parseInt(temp);
        }
        catch (NumberFormatException e)
        {
          PackageLogger.log.warning("Could not parse a value for attribute "
                                    + ATTR_ROOT_MIN_OCCURS
                                    + " in XML element " + NAME );
        }
      }
    }

    // Extract the rootMaxOccurs number; optional, default is 1
    m_rootMaxOccurs = 1;
    temp = (String) atts.get(ATTR_ROOT_MAX_OCCURS);
    if (temp != null)
    {
      if (temp.equalsIgnoreCase(ATTRVAL_UNBOUNDED))
      {
        m_rootMaxOccurs = -1;
      }
      else
      {
        try
        {
          m_rootMaxOccurs = Integer.parseInt(temp);
        }
        catch (NumberFormatException e)
        {
          PackageLogger.log.warning("Could not parse a value for attribute "
                                    + ATTR_ROOT_MAX_OCCURS
                                    + " in XML element " + NAME );
        }
      }
    }

    // Create the ArrayLists
    m_inGroups = new ArrayList();
    m_attributes = new ArrayList();
    m_contains = new ArrayList();
    m_variables = new ArrayList();
  }
  //----------------------------------------------------------------------
  /** Return the {@link nrs.csl.CSL_Element_Description
   * CSL_Element_Description } instance associated with self, or null if
   * none was provided by the CSL. */
  public CSL_Element_Description getDesc() { return m_desc; }
  //----------------------------------------------------------------------
  /** Return the name of this node, as specified by the value of the
   * name attribute of the corresponding XML element. */
  public String getNodeDescriptionName() { return m_name; }
  //----------------------------------------------------------------------
  /** Return whether the node can be placed at the top level of the tree
   * structure. Returns true if it can, else false. */
  public boolean getRootNode() { return m_rootNode; }
  //----------------------------------------------------------------------
  /** Return an iterator over the list of InGroups. The iterator
   * is used to access objects of type {@link
   * nrs.csl.CSL_Element_InGroup}. */
  public Iterator getInGroupsIterator()
  {
    return m_inGroups.iterator();
  }
  //----------------------------------------------------------------------
  /** Return an iterator over the list of Attributes. The iterator
   * is used to access objects of type {@link
   * nrs.csl.CSL_Element_Attribute}. */
  public Iterator getAttributesIterator()
  {
    return m_attributes.iterator();
  }
  //----------------------------------------------------------------------
  /** Return an iterator over the list of Contains sub-elements. The
   * iterator is used to access objects of type {@link
   * nrs.csl.CSL_Element_Contains}. */
  public Iterator getContainsIterator()
  {
    return m_contains.iterator();
  }
  //----------------------------------------------------------------------
  /** Return an iterator over the list of Variables. The iterator is
   * used to access objects of type {@link
   * nrs.csl.CSL_Element_Variable}. */
  public Iterator getVariablesIterator()
  {
    return m_variables.iterator();
  }
  //----------------------------------------------------------------------
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_NodeDescription(atts);
  }
  //----------------------------------------------------------------------
  /** Accept sub-elements to a NodeDescription object. */
  public void addElement(CSL_Element element)
  {
    if (element instanceof CSL_Element_Description)
      {
        m_desc = (CSL_Element_Description) element;
      }
    else if (element instanceof CSL_Element_InGroup)
      {
        m_inGroups.add(element);
      }
    else if (element instanceof CSL_Element_Attribute)
      {
        m_attributes.add(element);
      }
    else if (element instanceof CSL_Element_Contains)
      {
        m_contains.add(element);
      }
    else if (element instanceof CSL_Element_Variable)
      {
        m_variables.add(element);
      }
   else
     {
        // Invoke overridden method
        super.addElement(element);
      }
  }
  //---------------------------------------------------------------------
  /**
   * Instruct this node to attempt to resolve the unit type of each of
   * its attributes. This means each {@link CSL_Element_Attribute} owned
   * by self will try to get a reference to an actual {@link
   * CSL_Element_Unit}, instead of just holding the name to a unit. The
   * list of units available for the process is supplied through the
   * {@link CSL_Unit_Registry} parameter.
   */
  public void resolveAttributes(CSL_Unit_Registry unitReg)
  {
    for (Iterator iter = m_attributes.iterator(); iter.hasNext(); )
    {
      CSL_Element_Attribute attribute
        = (CSL_Element_Attribute) iter.next();

      attribute.resolveUnit(unitReg);
    }
  }
  //---------------------------------------------------------------------
  /**
   * Instruct this node to attempt to resolve the message type for each
   * of its variables.
   */
  public void resolveMessageTypes(CSL_Message_Registry msgReg)
  {
    for (Iterator iter = m_variables.iterator(); iter.hasNext(); )
    {
      CSL_Element_Variable v = (CSL_Element_Variable) iter.next();

      v.resolveMessageType(msgReg);
    }
  }
  //----------------------------------------------------------------------
  /** Return the value associated with the 'rootMinOccur' attribute, or
   * -1 if the value represents unbounded. */
  public int getRootMinOccurs() { return m_rootMinOccurs; }
  //----------------------------------------------------------------------
  /** Return the value associated with the 'rootMaxOccur' attribute, or
   * -1 if the value represents unbounded. */
  public int getRootMaxOccurs() { return m_rootMaxOccurs; }
  //----------------------------------------------------------------------
  /** Return the display name */
  public String toString()
  {
    return m_displayName;
  }
  //----------------------------------------------------------------------
  /**
   * Set which component provided this element
   */
  public void setSourceCID(String cid)
  {
    m_sourceCID = cid;
  }
  //----------------------------------------------------------------------
  /**
   * Get which component provided this element
   */
  public String getSourceCID()
  {
    return m_sourceCID;
  }
}
