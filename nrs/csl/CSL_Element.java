package nrs.csl;

import java.util.*;
import java.util.logging.*;
import nrs.core.message.Constants;

/** Base class for all CSL element classes, each of which corresponds to
 * type of XML element found in the CSL. XML elements in the CSL source
 * are eventually processed by and stored in set of CSL element
 * objects. Hence the class structure - particularly the member classes
 * of each CSL element class - correspond very closely to the CSL
 * schema. Modifications to the CSL schema will need to be reflected by
 * updates to CSL element classes. The version number shown indicates
 * which version of NRS/CSL this applies to.
 * @author  Darren Smith
 * @version 1.0 */
abstract public class CSL_Element
{
  /** Name of the XML element which created self  */
  private String m_elementName;

  //----------------------------------------------------------------------
  /** Constructor 
        
  @param elementName the name of the XML element which an instance
  of this class is to represent.
  */
  public CSL_Element(String elementName)
  {
    if (elementName == null) {
      Logger.getLogger("nrs.csl")
        .warning("CSL element object created with no name");
      
      m_elementName = "";
    }
    else m_elementName = elementName;
  }
  //----------------------------------------------------------------------
  /** Virtual constructor - return an instance of the dynamic
   * class. Must be overidden by each base class. */
  abstract public CSL_Element createInstance(HashMap atts);
  //----------------------------------------------------------------------
  /** Return the local name of the XML element which self corresponds
   * to. */
  public final String getElementName() { return m_elementName; }
  //----------------------------------------------------------------------
  /** Return the Namespace URI which applies to an XML element. Classes
   * derived from this class should not really to override this, but it
   * is here if they need to. The default return value is NAMESPACE } */
  public String getNamespaceURI() { return Constants.Namespace.NRS_CSL; }
  //----------------------------------------------------------------------
  /** Return a completely uppercase qualified name of the XML element
   * this class corresponds to. The return value is composed by
   * concatenating uppercase versions of the Namespace URI with the
   * local element name. This return value should be the basis of
   * comparisons with data from XML reader events to identify XML
   * elements.*/
  public final String getQName()
  { 
    return getNamespaceURI().toUpperCase() + getElementName().toUpperCase();
  }
  //----------------------------------------------------------------------
  /** Add XML character data to this element. */
  public void addCharacterData(String s)
  {
    // by default silently ignore
  }
  //----------------------------------------------------------------------
  /** Add a CSL element as a sub-element of this. */
  public void addElement(CSL_Element element)
  {
    // by default, raise a warning that 'element' is not needed by self
    Logger.getLogger("nrs.csl")
      .warning("CSL sub-element '" + element.getElementName()
               + "' not required by element '"
               + getIDString() + "'. Ignoring.");
  }
  //----------------------------------------------------------------------
  /** Return a string which is used as a short identification string for
   * an element instance, for use in debugging statement. Derived
   * classes should typically override this method. */
  public String getIDString()
  {
    return getElementName();
  }
 //----------------------------------------------------------------------
  /** Return a useful diagnostic string which presents a brief summary
   * of the current state of the class. Derived classes should typically
   * override this method. */
  String diagString() { return "CSL_Element (base class)"; }
  //----------------------------------------------------------------------
  /** Convert this class to a string by returning the name of the
   * corresponding XML element. */
  public String toString() { return m_elementName; }
}
