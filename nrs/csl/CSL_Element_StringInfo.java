package nrs.csl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

/** Represent a NRS/CSL string-info element. */
public class CSL_Element_StringInfo extends CSL_Element
{
  // Attributes
  private String m_restriction;

  // Sub-elements, each of type CSL_Element_ListMember
  private ArrayList m_listMembers;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "StringInfo";

  /** XML attribute to specify the string restriction */
  public static String ATTR_RESTRICTION = "restriction";

  /** Value of restriction attribute to indicate a list */
  public static String RESTRICTION_LIST = "list";

  /** Value of restriction attribute to indicate a token */
  public static String RESTRICTION_TOKEN = "token";

  /** Value of restriction attribute to indicate a vnname */
  public static String RESTRICTION_NODENAME = "vnname";

  /** Value of restriction attribute to indicate a filename */
  public static String RESTRICTION_FILENAME = "filename";

  // Class logger
  Logger m_log = Logger.getLogger("nrs.csl");

  //----------------------------------------------------------------------
  /** Constructor for prototype class. */
  CSL_Element_StringInfo()
  {
    super(NAME);
  }
  //----------------------------------------------------------------------
  /** Constructor
   *
   * Used to create an object with a set of XML name & value attributes.
   *
   * @param atts a HashMap containing the set of name and value attributes.
   */
  public CSL_Element_StringInfo(HashMap atts)
  {
    super(NAME);

    // Set the restriction, mandatory
    m_restriction = (String) atts.get(ATTR_RESTRICTION);

    // Create an empty list of restriction strings
    m_listMembers = new ArrayList();
  }
  //----------------------------------------------------------------------
  /** Return the string restriction. Will not return null. */
  public String getStringInfoRestriction() { return m_restriction; }
  //----------------------------------------------------------------------
  /* Create and return an object of this class, with a specified set of
   * XML name & value attributes.
   *
   * @param atts a HashMap containing the set of name and value
   * attributes.
   */
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_StringInfo(atts);
  }
  //----------------------------------------------------------------------
  /** Accept {@link CSL_Element_ListMember} elements. */
  public void addElement(CSL_Element element)
  {
    if (element instanceof CSL_Element_ListMember)
      {
        m_listMembers.add(element);
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
    String retVal =  NAME + "[" + m_restriction;

    for (Iterator i = m_listMembers.iterator(); i.hasNext(); )
      {
        CSL_Element_ListMember lm = (CSL_Element_ListMember) i.next();

        retVal += ", " + lm.diagString();
      }

    return retVal;
  }
  //----------------------------------------------------------------------
  /** Returns the name of the element, {@link #getElementName()} */
  public String toString()
  {
    return getElementName();
  }
  //----------------------------------------------------------------------
  /** Return a string which is used as a short identification string for
   * an instance, for use in debugging statements etc. */
  public String getIDString()
  {
    return getElementName();
  }
  //----------------------------------------------------------------------
  /** Return a string which summarises the content of self */
  public String getSummary()
  {
    String retVal = "";

    if (m_restriction != null) retVal += "restiction=" + m_restriction;

    if (m_listMembers != null)
      {
        retVal += ", value can be one of [";
        for (Iterator i = m_listMembers.iterator(); i.hasNext(); )
          {
            CSL_Element_ListMember lm = (CSL_Element_ListMember) i.next();

            retVal += lm.getValue();
            if (i.hasNext()) retVal += ", ";
          }
        retVal += "]";
      }
    retVal += "\n";

    return retVal;
  }
  //---------------------------------------------------------------------
  /**
   * Evalute whether the specified {@link CSL_Element_ListMember} is
   * effectively contained within the string restriction info for self
   *
   * @return true if a CSL_Element_ListMember equivalent to
   * <code>lhs</code> is contained within self (ie by equivalent we mean
   * the comparisons are not based on object references, but instead on
   * similar content), false otherwise.
   */
  private boolean restrictionContains(CSL_Element_ListMember lhs)
  {
    for (Iterator i = m_listMembers.iterator(); i.hasNext(); )
    {
      CSL_Element_ListMember lm = (CSL_Element_ListMember) i.next();
      if (lm.similarTo(lhs)) return true;
    }

    // list search was exhausted implying nothing similar found
    return false;
  }
  //----------------------------------------------------------------------
  /**
   * Compare self with another {@link CSL_Element_StringInfo} object.
   *
   * @return true if this and <code>lhs</lhs> effectively define the
   * same string-info, false otherwise.
   */
  public boolean similarTo(CSL_Element_StringInfo lhs)
  {
    if (m_restriction.compareTo(lhs.m_restriction) != 0)
    {
      return false;
    }

    if (m_listMembers.size() != lhs.m_listMembers.size())
    {
      return false;
    }

    // now compare each element of the array
    for (Iterator i = m_listMembers.iterator(); i.hasNext(); )
      if (!lhs.restrictionContains((CSL_Element_ListMember)i.next()))
      {
        return false;
      }

    return true;
  }
}
