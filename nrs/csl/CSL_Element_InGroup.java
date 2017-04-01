// $Id: CSL_Element_InGroup.java,v 1.3 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

import java.util.HashMap;

/** Represent a NRS/CSL in-group element */
public class CSL_Element_InGroup extends CSL_Element
{
  ///// Attributes

  private String m_name;
  private String m_displayName;

  ///// Attribute Names

  /** XML attribute to specify the name */
  public static String ATTR_NAME = "name";

  /** XML attribute to specify the display name */
  public static String ATTR_DISPLAY_NAME = "displayName";

 /** Name of the XML element to which this class corresponds. */
  public static String NAME = "InGroup";

  //----------------------------------------------------------------------
  /** Constructor for prototype instance. */
  CSL_Element_InGroup()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor */
  public CSL_Element_InGroup(HashMap atts)
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
  }
  //----------------------------------------------------------------------
  /** Return the name of this element, as specified by the value of the
   * name attribute of the corresponding XML element. */
  public String getInGroupName() { return m_name; }
  //----------------------------------------------------------------------
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_InGroup(atts);
  }
  //----------------------------------------------------------------------
  /** Return the display name of this InGroup. */
  public String toString()
  {
    return m_displayName;
  }
}
