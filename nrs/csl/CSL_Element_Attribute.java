package nrs.csl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/** Represent a NRS/CSL attribute element */
public class CSL_Element_Attribute extends CSL_Element
{
  ///// Attributes

  private String m_name;
  private String m_displayName;
  private String m_unitName;
  private String m_defaultValue;
  private boolean m_mustHave;
  private boolean m_isConst;
  private String m_liveChange;
  private boolean m_inNRSNamespace;
  private boolean m_noSend;

  ///// Attribute Names

  /** XML attribute to specify the name */
  public static String ATTR_NAME = "name";

  /** XML attribute to specify the inNRSNamespace */
  public static String ATTR_INNRSNAMESPACE = "inNRSNamespace";

  /** XML attribute to specify the display name */
  public static String ATTR_DISPLAY_NAME = "displayName";

  /** XML attribute to specify the mustHave */
  public static String ATTR_MUST_HAVE = "mustHave";

  /** XML attribute to specify the isConst */
  public static String ATTR_IS_CONST = "isConst";

  /** XML attribute to specify the liveChange */
  public static String ATTR_IS_LIVE_CHANGE = "liveChange";

  /** XML attribute to specify the type of attribute (ie which unit type
   * it corresponds to) */
  public static String ATTR_UNIT_NAME = "unitName";

  /** XML attribute to specify the default value */
  public static String ATTR_DEFAULT_VALUE = "defaultValue";

  /** XML attribute to specify the noSend */
  public static String ATTR_NOSEND = "noSend";

  //
  // Some expected values for the "name" attribute of a CSL <Attribute>
  // instance
  //
  public static String NODE_ATTRIBUTE_NAME = "name";

  ///// Sub-elements

  /** Sub-element description within this message. */
  private CSL_Element_Description m_desc;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "Attribute";

  //
  // Other
  //

  // The resolved unit type
  CSL_Element_Unit m_resolvedUnit;

  //----------------------------------------------------------------------
  /** Constructor for prototype instance. */
  CSL_Element_Attribute()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor */
  public CSL_Element_Attribute(HashMap atts)
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

    m_unitName = (String) atts.get(ATTR_UNIT_NAME);
    if (m_unitName == null)
    {
      PackageLogger.log.warning(NAME + " \"" +
                                m_name + "\" has not been provied with a"
                                + " value for XML-attribute \""
                                + ATTR_UNIT_NAME + "\"");
      m_unitName = "UNKNOWN";
    }

    String inNRSNamespace = (String) atts.get(ATTR_INNRSNAMESPACE);
    if (inNRSNamespace == null)
    {
      PackageLogger.log.warning(NAME + " \"" +
                                m_name + "\" has not been provied with a"
                                + " value for XML-attribute \""
                                + ATTR_INNRSNAMESPACE + "\"");
      m_inNRSNamespace = false;
    }
    else
    {
      m_inNRSNamespace = Boolean.valueOf(inNRSNamespace).booleanValue();
    }

    if (atts.containsKey(ATTR_DEFAULT_VALUE))
      {
        m_defaultValue = (String) atts.get(ATTR_DEFAULT_VALUE);
      }
    else
      {
        m_defaultValue = "";
      }

    m_mustHave = Boolean.valueOf((String)
                                 atts.get(ATTR_MUST_HAVE)).booleanValue();
    m_isConst = Boolean.valueOf((String)
                                atts.get(ATTR_IS_CONST)).booleanValue();

    if (atts.containsKey(ATTR_NOSEND))
    {
      m_noSend = Boolean.valueOf((String)
                                 atts.get(ATTR_NOSEND)).booleanValue();
    }
    else
    {
      m_noSend = false;
    }

    // Default values
    m_desc = CSL_Element_Description.defaultInstance();
  }
  //----------------------------------------------------------------------
  /** Return the name of this element, as specified by the value of the
   * name attribute of the corresponding XML element. */
  public String getAttributeName() { return m_name; }
  //----------------------------------------------------------------------
  /** Return the value of the 'mustHave' attribute. */
  public boolean getMustHave() { return m_mustHave; }
  //----------------------------------------------------------------------
  /** Return the value of the 'isConst' attribute. */
  public boolean getIsConst() { return m_isConst; }
  //----------------------------------------------------------------------
  /** Return the value of the 'noSend' attribute. */
  public boolean getNoSend() { return m_noSend; }
  //----------------------------------------------------------------------
  /** Return the value of the 'inNRSNamespace' attribute. */
  public boolean getInNRSNamespace() { return m_inNRSNamespace; }
  //----------------------------------------------------------------------
  /** Return the value of the 'unitName' attribute. */
  public String getUnitNameStr() { return m_unitName; }
  //----------------------------------------------------------------------
  /** Return the resolved 'unitName' attribute. This can return null if
   * the unit failed to resolve. */
  public CSL_Element_Unit getResolvedUnit() { return m_resolvedUnit; }
  //----------------------------------------------------------------------
  /** Return the {@link nrs.csl.CSL_Element_Description
   * CSL_Element_Description } instance associated with self, or null if
   * none was provided by the CSL. */
  public CSL_Element_Description getDesc() { return m_desc; }
  //----------------------------------------------------------------------
  /** Return the value of the 'defaultValue' attribute.  */
  public String getDefaultValue() { return m_defaultValue; }
  //----------------------------------------------------------------------
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_Attribute(atts);
  }
  //----------------------------------------------------------------------
  /** Accept <code>Description</code> and <code>Default</code>
   * elements. */
  public void addElement(CSL_Element element)
  {
    if (element instanceof CSL_Element_Description)
      {
        m_desc = (CSL_Element_Description) element;
      }
   else
     {
        // Invoke overridden method
        super.addElement(element);
      }
  }
  //----------------------------------------------------------------------
  /** Return the display name of this Attribute. */
  public String toString()
  {
    return m_displayName;
  }
  //----------------------------------------------------------------------
  /**
   * Resolve the {@link CSL_Element_Unit} this attribute refers to as
   * for the type of the value it holds.
   */
  public void resolveUnit(CSL_Unit_Registry unitReg)
  {
    m_resolvedUnit = unitReg.getUnit(m_unitName);

    // If the above call returns null, then the message type could not
    // be resolved, so lets show an error.
    if (m_resolvedUnit == null)
      {
        PackageLogger.log.warning("Failed to resolve unit-type '" + m_unitName
                                  + "' for attribute '" + m_name
                                  + "'. No such"
                                  + " type found in the list of"
                                  + " available units.");
      }
  }
}
