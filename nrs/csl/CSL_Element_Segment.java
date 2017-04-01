package nrs.csl;

import java.util.*;


/** Represent a NRS/CSL segment element */
public class CSL_Element_Segment extends CSL_Element
{
  ////// Attributes

  private String m_name;
  private String m_displayName;
  private String m_unit;
  private boolean m_segmentInContents;

  /////  Sub-elements

  private CSL_Element_Description m_desc;
  private CSL_Element_StringInfo  m_stringInfo;
  private CSL_Element_FloatInfo   m_floatInfo;
  private CSL_Element_IntegerInfo m_integerInfo;

  /* Reference to the CSL_Element_Unit once it has been resolved. */
  private CSL_Element_Unit m_resolvedUnit;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "Segment";

  ///// XML Attribute names

  /** XML attribute to specify the name */
  public static String ATTR_NAME = "name";

  /** XML attribute to specify the display name */
  public static String ATTR_DISPLAY_NAME = "displayName";

  /** XML attribute to specify whether segment is included in contents */
  public static String ATTR_SEGMENT_IN_CONTENTS = "segmentInContents";

  /** XML attribute to specify the unit */
  public static String ATTR_UNIT = "unit";

  //----------------------------------------------------------------------
  /** Constructor  */
  CSL_Element_Segment()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor  */
  public CSL_Element_Segment(HashMap atts)
  {
    super(NAME);

    String s;

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

    // Set the unit, mandatory
    m_unit = (String) atts.get(ATTR_UNIT);

    // Set whether segment in contents, optional, default is false
    if (atts.containsKey(ATTR_SEGMENT_IN_CONTENTS))
      {
        m_segmentInContents = 
          Boolean.valueOf((String)atts.get(ATTR_SEGMENT_IN_CONTENTS)).booleanValue();
      }
    else
    {
      m_segmentInContents = false;
    }
  }
  //----------------------------------------------------------------------
  public String getSegmentName() { return m_name; }
  //----------------------------------------------------------------------
  /** Return the string-based reference to the unit underlying this
   * message segment. */
  public String getSegmentUnit() { return m_unit; }
  //----------------------------------------------------------------------
  /** Return the object-based reference to the unit underlying this
   * message segment. This can return null if this message segment has
   * not yet had its unit resolved.
   *
   * @see CSL_Element_Segment#resolveUnit(CSL_Unit_Registry) resolveUnit
   **/
  public CSL_Element_Unit getSegmentResolvedUnit()
  {
    return m_resolvedUnit;
  }
  //----------------------------------------------------------------------
  /** Return whether segment will appear as a sub-element of the base 
   * Message in PML with an appropriate element name instead of attribute 
   name. */
  public boolean getSegmentInContents()
  {
    return m_segmentInContents;
  }
  //----------------------------------------------------------------------
  /** Return the {@link nrs.csl.CSL_Element_Description
   * CSL_Element_Description } instance associated with self, or null if
   * none was provided by the CSL. */
  public CSL_Element_Description getDesc()
  {
    return m_desc;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link CSL_Element_StringInfo} that has been specified
   * for this segment, or <tt>null</tt> if none has been
   * specified. Note, as well as this segment specific constraint, there
   * might also be a constraint on the underlying unit type, which the
   * caller should also check.
   */
  public CSL_Element_StringInfo getSegmentStringInfo()
  {
    return m_stringInfo;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link CSL_Element_FloatInfo} that has been specified
   * for this segment, or <tt>null</tt> if none has been
   * specified. Note, as well as this segment specific constraint, there
   * might also be a constraint on the underlying unit type, which the
   * caller should also check.
   */
  public CSL_Element_FloatInfo getSegmentFloatInfo()
  {
    return m_floatInfo;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link CSL_Element_IntegerInfo} that has been specified
   * for this segment, or <tt>null</tt> if none has been
   * specified. Note, as well as this segment specific constraint, there
   * might also be a constraint on the underlying unit type, which the
   * caller should also check.
   */
  public CSL_Element_IntegerInfo getSegmentIntegerInfo()
  {
    return m_integerInfo;
  }
  //----------------------------------------------------------------------
  /** Resolve the CSL unit this message segment references. Before this
   * call the reference to the CSL unit is in string form, resulting
   * directly from the XML specification.  This call provides for an
   * object reference, via a lookup into the unit registry. */
  public void resolveUnit(CSL_Unit_Registry unitReg)
  {
    m_resolvedUnit = unitReg.getUnit(m_unit);

    // If the above call returns null, then the unit could not be
    // resolved,so lets show an error.
    if (m_resolvedUnit == null)
      {
        PackageLogger.log.warning("Failed to resolve unit-type '"
                                  + m_unit + "'. No such"
                                  + " type found in the list"
                                  + " of available units.");
      }
  }
  //----------------------------------------------------------------------
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_Segment(atts);
  }
  //----------------------------------------------------------------------
  /** Accept <code>Description</code> and <code>Constraint</code> *
  elements. */
  public void addElement(CSL_Element element)
  {

    if (element instanceof CSL_Element_Description)
      {
        m_desc = (CSL_Element_Description) element;
      }
    else if (element instanceof CSL_Element_StringInfo)
      {
        m_stringInfo = (CSL_Element_StringInfo) element;
      }
    else if (element instanceof CSL_Element_FloatInfo)
      {
        m_floatInfo = (CSL_Element_FloatInfo) element;
      }
    else if (element instanceof CSL_Element_IntegerInfo)
      {
        m_integerInfo = (CSL_Element_IntegerInfo) element;
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
    String retVal =  NAME + "[" + m_name + ", " + m_unit  + ", " + m_segmentInContents;

    if (m_desc != null)  retVal += ", " + m_desc.diagString();
    if (m_stringInfo != null) retVal += ", " + m_stringInfo.diagString();
    if (m_floatInfo != null) retVal += ", " + m_floatInfo.diagString();
    if (m_integerInfo != null) retVal += ", " + m_integerInfo.diagString();

    retVal += "]";

    return retVal;
  }
  //----------------------------------------------------------------------
  public String toString()
  {
    return m_displayName;
  }
}
