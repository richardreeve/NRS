// $Id: CSL_Element_ListMember.java,v 1.4 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

import java.util.logging.*;
import java.util.*;


/** Represent a NRS/CSL segment element */
public class CSL_Element_ListMember extends CSL_Element
{
  ////// Attributes

  private String m_value;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "ListMember";

  ///// XML Attribute names

  /** XML attribute to specify the name */
  public static String ATTR_VALUE = "value";

  //----------------------------------------------------------------------
  /** Constructor  */
  CSL_Element_ListMember()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor  */
  public CSL_Element_ListMember(HashMap atts)
  {
    super(NAME);

    String s;

    // Set the value, mandatory
    m_value = (String) atts.get(ATTR_VALUE);
  }
  //----------------------------------------------------------------------
  /** Return the value associated with a <code>ListMember</code>
   * object. */
  public String getValue()
  {
    return m_value;
  }
  //----------------------------------------------------------------------
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_ListMember(atts);
  }
  //----------------------------------------------------------------------
  /** Return a useful diagnostic string which presents a brief summary
   * of the current state of the class. */
  String diagString()
  {
    return NAME + "[" + m_value + "]";
  }
  //----------------------------------------------------------------------
  public String toString() { return diagString(); }
  //----------------------------------------------------------------------
  /**
   * Compare self with another {@link CSL_Element_ListMember}
   *
   * @return true if this and <code>lhs</code> effectively define the
   * same {@link CSL_Element_ListMember} (ie they both describe the same
   * {@link #getValue()} string), false otherwise.
   */
  public boolean similarTo(CSL_Element_ListMember lhs)
  {
    if (m_value.compareTo(lhs.m_value) == 0)
    {
      return true;
    }
    else
    {
      System.out.println("DEBUG: CSL_Element_ListMember differ on m_value");
      return false;
    }
    //    return (m_value.compareTo(lhs.m_value) == 0)? true : false;
  }
}
