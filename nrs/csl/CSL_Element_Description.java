// $Id: CSL_Element_Description.java,v 1.3 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

import java.util.HashMap;
import java.util.logging.Logger;

/** Represent a NRS/CSL description element. The textual description
 * corresponds to the character data between XML description tags. The
 * default textual description stored by this element - before character
 * data has been provided, is the empty string (""). */
public class CSL_Element_Description extends CSL_Element
{
  /** Text of the description/ */
  private String m_descStr;

  /** Name of the XML element to which this class corresponds. */
  public static String NAME = "Description";

  /** A default instance of this class, for use by other element
   * instances which require an empty description. */
  private static CSL_Element_Description m_defaultInstance;

  //----------------------------------------------------------------------
  /** Constructor  */
  public CSL_Element_Description()
  {
    super(NAME);

    // Default value for dsecription
    m_descStr = "";
  }
  //----------------------------------------------------------------------
  /** Accept character data as the textual description. <b>Todo</b>
   * This simple implementation assumes descriptions are simple text
   * strings. But this might not always be the case, so this code
   * needs to be upgrades at some point to store different data types
   * which can provide a textual description - e.g., XHTML(?), HTML.*/
  public void addCharacterData(String s)
  {
    if (s != null) m_descStr += s.trim();
  }
  //----------------------------------------------------------------------
  public void addElement(CSL_Element element)
  {
    // We don't expect any sub elements of description.
    Logger.getLogger("nrs.csl").warning("Sub-element (of type "
                                        + element + ") "
                                        + "not expected by a description"
                                        + " element. Ignoring.");
  }
  //----------------------------------------------------------------------
  /** Return the textual description. Will <u>not</u> return a null
      object. */
  public String getDescription() { return m_descStr;}
  //----------------------------------------------------------------------
  /** Virtual constructor - return an instance of the dynamic
   * class. Must be overidden by each base class. */
  public CSL_Element createInstance(HashMap atts)
  {
    return new CSL_Element_Description();
  }
  //----------------------------------------------------------------------
  /** Return a useful diagnostic string which presents a brief summary
   * of the current state of the class. */
  String diagString()
  {
    return NAME + "[" + m_descStr + "]";
  }
  //----------------------------------------------------------------------
  /**
   * Return a default instance of this class, which just represents an
   * empty description. The same object is always returned, and the
   * description string is always kept empty.
   */
  static public CSL_Element_Description defaultInstance()
  {
    if (m_defaultInstance == null)
    {
      m_defaultInstance = new CSL_Element_Description();
    }
    m_defaultInstance.m_descStr = "";

    return m_defaultInstance;
  }
}
