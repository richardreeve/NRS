// $Id: CSL_Element_Unit.java,v 1.8 2005/09/16 13:31:36 s0125563 Exp $
package nrs.csl;

import java.util.HashMap;
import java.util.logging.Logger;

/** Represent a NRS/CSL unit element. It extends the {@link
 * java.lang.Comparable} interface so that Units can be placed into
 * sorted structures. 
 *
 * Void is not a basic type.
*/
public class CSL_Element_Unit extends CSL_Element
{
  //
  // Define some static objects which are returned as a result of type
  // enqiry of a unit. By returning these instead of objects created
  // dynamically on a per call basis, we can save memory, and thus
  // optimise performance.
  //
  private static final CSL_BasicType_Float BT_FLOAT
    = new CSL_BasicType_Float();
  private static final CSL_BasicType_String BT_STRING
    = new CSL_BasicType_String();
  private static final CSL_BasicType_Boolean BT_BOOLEAN
    = new CSL_BasicType_Boolean();
  private static final CSL_BasicType_Integer BT_INTEGER
    = new CSL_BasicType_Integer();
  private static final CSL_BasicType_Route BT_ROUTE
    = new CSL_BasicType_Route();
  private static final CSL_BasicType_Void BT_VOID
    = new CSL_BasicType_Void();

  // Attributes
  private String m_name;
  private String m_type;
  private String m_displayName;

  // Is this unit implicitly defined? True = yes, false = no
  private boolean m_implicit;

  // Sub-elements
  private CSL_Element_Description m_desc;
  private CSL_Element_StringInfo  m_stringInfo;
  private CSL_Element_FloatInfo   m_floatInfo;
  private CSL_Element_IntegerInfo m_integerInfo;

  // Store the basicType as an object
  private CSL_BasicType m_basicType;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "Unit";

  /** XML attribute to specify the name */
  public static String ATTR_NAME = "name";

  /** XML attribute to specify the display name */
  public static String ATTR_DISPLAY_NAME = "displayName";

  /** XML attribute to specify the type */
  public static String ATTR_TYPE = "type";

  /** Psuedo-XML attribute to indicate if this element is implicit --
   * which means it hasn't been defined from CSL but instead is built
   * into the GUI. Thus the attribute is psuedo-XML since this attribute
   * should never appear in a CSL description of a unit. */
  public static String INT_ATTR_IMPLICIT = "_implicit_";

  // Class logger
  Logger m_log = Logger.getLogger("nrs.csl");

  //----------------------------------------------------------------------
  /** Constructor for prototype class. */
  CSL_Element_Unit()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor */
  public CSL_Element_Unit(HashMap atts)
  {
    super(NAME);

    // Set the name, mandatory
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

    // Set the type, mandatory
    m_type = (String) atts.get(ATTR_TYPE);
    m_basicType = identifyBasicType(m_type);

    // Set the implicitness state
    if (atts.containsKey(INT_ATTR_IMPLICIT))
      {
        m_implicit = Boolean.valueOf((String) atts.get(INT_ATTR_IMPLICIT)).booleanValue();
      }
    else
      {
        /* By default, we assume the unit has been defined in CSL */
        m_implicit = false;
      }
  }
  //----------------------------------------------------------------------
  /** Try to get a basicType object to associate with this unit. This is
   * a helper function called by the constructor.
   *
   * @return an instance of the identified basic type associated with
   * this unit, or null if no object could be associated with the
   * <code>typeStr</code>. */
  private CSL_BasicType identifyBasicType(String typeStr)
  {
    if (typeStr != null)
      {
        if (typeStr.equalsIgnoreCase(CSL_BasicType_Float.NAME))
          return BT_FLOAT;

        if (typeStr.equalsIgnoreCase(CSL_BasicType_String.NAME))
          return BT_STRING;

        if (typeStr.equalsIgnoreCase(CSL_BasicType_Boolean.NAME))
          return BT_BOOLEAN;

        if (typeStr.equalsIgnoreCase(CSL_BasicType_Integer.NAME))
          return BT_INTEGER;

        if (typeStr.equalsIgnoreCase(CSL_BasicType_Route.NAME))
          return BT_ROUTE;

        if (typeStr.equalsIgnoreCase(CSL_BasicType_Void.NAME))
          return BT_VOID;

        Logger.getLogger("nrs.csl").
          warning("Could not create a basic-type with name '" + typeStr
                  + "' for unit '" + m_name + "'. Type not recognised.");
        return null;
      }

    Logger.getLogger("nrs.csl").
      warning("Could not create a basic-type for unit '"
              + m_name
              + "' because type-string is null");

    return null;

  }
  //---------------------------------------------------------------------
  /**
   * Return the {@link CSL_BasicType} of this unit
   */
  public CSL_BasicType getBasicType()
  {
    return m_basicType;
  }
  //---------------------------------------------------------------------
  /** Return whether this unit has been implicitly defined. A value of
   * <code>true</code> means is has, where-as <code>false</code> means
   * it was explicitiy defined in CSL. */
  public boolean isImplicit() { return m_implicit; }
  //----------------------------------------------------------------------
  /** Return the value associated with the name attribute of this
   * unit */
  public String getUnitName() { return m_name; }
  //----------------------------------------------------------------------
  /** Return the value associated with the display name attribute of
   * this unit */
  public String getUnitDisplayName() { return m_displayName; }
  //----------------------------------------------------------------------
  /** Return an optional {@link CSL_Element_IntegerInfo} object
   * associated with this unit. If no object is available, then null is
   * returned. */
  public CSL_Element_IntegerInfo getIntegerInfo() { return m_integerInfo; }
  //----------------------------------------------------------------------
  /** Return an optional {@link CSL_Element_FloatInfo} object
   * associated with this unit. If no object is available, then null is
   * returned. */
  public CSL_Element_FloatInfo getFloatInfo() { return m_floatInfo; }
  //----------------------------------------------------------------------
  /** Return an optional {@link CSL_Element_StringInfo} object
   * associated with this unit. If no object is available, then null is
   * returned. */
  public CSL_Element_StringInfo getStringInfo() { return m_stringInfo; }
  //----------------------------------------------------------------------
  /** Return the value associated with the type attribute of this
   * unit */
  public String getUnitType() { return m_type; }
  //----------------------------------------------------------------------
  /** Return the {@link nrs.csl.CSL_Element_Description
   * CSL_Element_Description } instance associated with self, or null if
   * none was provided by the CSL. */
  public CSL_Element_Description getDesc() { return m_desc; }
  //----------------------------------------------------------------------
  /* Create and return an object of this class, with a specified set of
   * XML name & value attributes.
   *
   * @param atts a HashMap containing the set of name and value
   * attributes.
   */
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_Unit(atts);
  }
  //----------------------------------------------------------------------
  /** Accept{@link CSL_Element_Description}, {@link
   * CSL_Element_FloatInfo}, {@link CSL_Element_IntegerInfo} and {@link
   * CSL_Element_StringInfo} elements. */
  public void addElement(CSL_Element element)
  {
    if (element instanceof CSL_Element_Description)
      {
        m_desc = (CSL_Element_Description) element;
      }
    else if (element instanceof CSL_Element_FloatInfo)
      {
        m_floatInfo = (CSL_Element_FloatInfo) element;
      }
    else if (element instanceof CSL_Element_IntegerInfo)
      {
        m_integerInfo = (CSL_Element_IntegerInfo) element;
      }
    else if (element instanceof CSL_Element_StringInfo)
      {
        m_stringInfo = (CSL_Element_StringInfo) element;
      }
   else
     {
        // Invoke overridden method
        super.addElement(element);
      }
  }
  //----------------------------------------------------------------------
  /** Return a useful diagnostic string which presents a brief summary
   * of the current state of the class. */
  String diagString()
  {
    String retVal =  NAME + "[" + m_name + ", " + m_type;

    if (m_desc != null)  retVal += ", " + m_desc.diagString();
    if (m_floatInfo != null) retVal += ", " + m_floatInfo.diagString();
    if (m_integerInfo != null) retVal += ", " + m_integerInfo.diagString();
    if (m_stringInfo != null) retVal += ", " + m_stringInfo.diagString();
    if (m_implicit) retVal += ",  IMPLICIT";

    retVal += "]";

   return retVal;
  }
  //----------------------------------------------------------------------
  /**
   * Compare self with another {@link CSL_Element_Unit}
   *
   * @return true if this and <code>lhs</lhs> effectively define the
   * same unit-type (eg differences in the description are ignored),
   * false otherwise.
   */
  public boolean similarTo(CSL_Element_Unit lhs)
  {
    // TODO - replace the DEBUG statements with exception throws, so
    // that when a discrepancy occurs, it is easier to locate the problem

    // Compare name
    if (m_name.compareTo(lhs.m_name) != 0)
    {
      System.out.println("DEBUG: CSL_Element_Unit differ on m_name");
      return false;
    }

    // Compare type
    if (m_type.compareTo(lhs.m_type) != 0)
    {
      System.out.println("DEBUG: CSL_Element_Unit differ on m_type");
      return false;
    }

    // Compare string info
    if (m_stringInfo != null)
    {
      if ((lhs.m_stringInfo == null) ||
          (!m_stringInfo.similarTo(lhs.m_stringInfo)))
      {
        System.out.println("DEBUG: CSL_Element_Unit differ on m_stringInfo");
        return false;
      }
    }
    else
    {
      if (lhs.m_stringInfo != null)
      {
        System.out.println("DEBUG: CSL_Element_Unit differ on m_stringInfo");
        return false;
      }
    }

    // Compare integer info
    if (m_integerInfo != null)
    {
      if ((lhs.m_integerInfo == null) ||
          (!m_integerInfo.similarTo(lhs.m_integerInfo)))
      {
        System.out.println("DEBUG: CSL_Element_Unit differ on m_integerInfo");
        return false;
      }
    }
    else
    {
      if (lhs.m_integerInfo != null)
      {
        System.out.println("DEBUG: CSL_Element_Unit differ on m_integerInfo");
        return false;
      }
    }

    // Compare float info
    if (m_floatInfo != null)
    {
      if ((lhs.m_floatInfo == null) ||
          (!m_floatInfo.similarTo(lhs.m_floatInfo)))
      {
        System.out.println("DEBUG: CSL_Element_Unit differ on m_floatInfo");
        return false;
      }
    }
    else
    {
      if (lhs.m_floatInfo != null)
      {
        System.out.println("DEBUG: CSL_Element_Unit differ on m_floatInfo");
        return false;
      }
    }

    return true;
  }
  //----------------------------------------------------------------------
  public String toString()
  {
    return getUnitDisplayName();
  }
  //----------------------------------------------------------------------
  /** Return a string which is used as a short identification string for
   * an instance, for use in debugging statements etc. */
  public String getIDString()
  {
    return getElementName() + "(name ='" + getUnitDisplayName() + "')";
  }
}
