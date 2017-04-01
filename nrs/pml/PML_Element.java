// $Id: PML_Element.java,v 1.3 2005/05/06 16:07:04 hlrossano Exp $
package nrs.pml;

import nrs.core.message.Constants;
import org.xml.sax.Attributes;

/** 
 * Base class for all PML element classes, each of which corresponds to
 * a type of XML element found in PML. XML elements in the PML source
 * are eventually processed by and stored in set of PML element objects
 * (which all derive from this class). Hence the internal structure of
 * each PML class corresponds very closely to its associated PML
 * message. Thus, modifications to the PML schema will need to be
 * reflected by updates to PML element classes. 
 *
 * <br><br>This class now also does some decoding of the XML message,
 * specifically, for the fields common to many of the PML
 * messages. These are: route, toID, toName, msgID.
 *
 * <br><br>The version number shown indicates which version of NRS/PML this
 * applies to.
 *
 * TODO - update this documentation to reflect that now all PML/XML
 * messages end up with PML_Element objects becoming created
 *
 *
 * @author  Darren Smith
 * @version 1.0
 */
abstract public class PML_Element
{
  /** Name of the XML element which created self  */
  private String m_elementName;

  //----------------------------------------------------------------------
  /** Constructor 
   *
   * @param elementName the name of the XML element which an instance of
   * this class is to represent.
   *
   * @throws NullPointerException if elementName is null
   */
  public PML_Element(String elementName) throws NullPointerException
  {
    if (elementName == null) 
      throw new NullPointerException("elementName can't be null");
    
    m_elementName = elementName;
  }
  //----------------------------------------------------------------------
  /** Virtual constructor - return an instance of the dynamic
   * class. Must be overidden by each base class. */
  abstract public PML_Element createInstance(Attributes atts);
  //----------------------------------------------------------------------
  /** Return the local name of the XML element which self corresponds
   * to. */
  public final String getElementName() { return m_elementName; }
  //----------------------------------------------------------------------
  /** Return a completely uppercase qualified name of the XML element
   * this class corresponds to. The return value is composed by
   * concatenating uppercase versions of the Namespace URI with the
   * local element name. This return value should be the basis of
   * comparisons with data from XML reader events to identify XML
   * elements.*/
  public final String getQName()
  { 
    return Constants.Namespace.NRS_PML.toUpperCase() 
      + getElementName().toUpperCase();
  }
  //----------------------------------------------------------------------
  /** Add XML character data to this element. */
  public void addCharacterData(String s)
  {
    // by default silently ignore
  }
  //----------------------------------------------------------------------
  /** Add a PML element as a sub-element of this. Since {@link
   * PML_Element} is an abstract base class, it doesn't correspond to
   * any actual XML element tag and so thus it can't have any
   * sub-elements. If this routine is called, say by inherited classes,
   * it just outputs a warning. 
   */
  public void addElement(PML_Element element)
  {
    // by default, raise a warning that 'element' is not needed by self
    PackageLogger.log.warning("PML sub-element <" + element.getElementName()
                              + "> not required by element '"
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
  String diagString() { return "PML_Element (base class)"; }
  //----------------------------------------------------------------------
  /** Convert this class to a string by returning the name of the
   * corresponding XML element. */
  public String toString() { return m_elementName; }
}
