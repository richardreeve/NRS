package nrs.pml;

import java.util.HashMap;
import nrs.core.message.Constants;
import org.xml.sax.Attributes;

/** Register the various classes which correspond to PML XML
 * elements. Instances of this class cannot be constructed; use the
 * {@link #getInstance()} method to obtain a reference to the singleton
 * instance.
 */
public class PML_Element_Registry
{
  /** Set of pairs, consisting of a element name and an instance of the
   * class corresponding to that element (which is of type {@link
   * PML_Element}) which deals which handling specific kinds of XML
   * elements.
   */
  private HashMap m_map;

  /** Pairs of element names and {@link Boolean} objects, which indicate
   * whether a particular message can be processed immediately by the
   * PML/XML parser, or whether a representative object is available to
   * do more sophisticated parsing operations.
   */
  static private HashMap m_processNow;

  /**
   * Singleton instance.
   */
  static private PML_Element_Registry m_singleton;

  //----------------------------------------------------------------------
  /**
   * Obtain a reference to the singleton instance.
   */
  public static PML_Element_Registry getInstance()
  {
    if (m_singleton == null) m_singleton = new PML_Element_Registry();

    return m_singleton;
  }
  //----------------------------------------------------------------------
  /** Constructor

      Todo - this is not nice, having to provide various registries to
      the constructor of PML_Element_Registry. They are actually
      required by PML_Element_Root. So really I should sent them
      directly there, instead of through this call. Perhaps it would be
      better for the PML_Element_Root to create them itself, and
      providing accessor methods to there for the rest of the program to
      use.

      Also note - although these four registries have similar format
      names, there is an importance difference between this one, and the
      other three. This registry stores a list of *known* PML XML
      element types, whereas the other three store the results form the
      processing of the PML file. This registry is used to create
      instances of other objects.

      It initially seems plausible to design PML_Element_Root as a
      singleton - which owns the three dynamic registries. However,
      during the course of the GUI session, several different PML files
      might be loaded from different sources, so making it a singleton
      isn't such a good idea. Also, this leads to the question: do we
      want separate sources of PML inputs to result in separate sets of
      the dynamic-registries? Probably no - easier just to stick with
      three dynamic registries.
  */

  private PML_Element_Registry()
  {
    m_map = new HashMap();
    m_processNow = new HashMap();
    createPrototypes();
  }
  //----------------------------------------------------------------------
  /** Create one prototype of each element class. This is the act of
   * registering a class with the registry, so that further instances of
   * that class can be constructed via the factory method in {@link
   * PML_Element}. */
  private void createPrototypes()
  {
    registerNullPrototype(Constants.MessageTypes.AcknowledgeMessage);
    registerNullPrototype(Constants.MessageTypes.CreateLink);
    registerNullPrototype(Constants.MessageTypes.CreateNode);
    registerNullPrototype(Constants.MessageTypes.DeleteLink);
    registerNullPrototype(Constants.MessageTypes.DeleteNode);
    registerNullPrototype(Constants.MessageTypes.FailedRoute);
    registerNullPrototype(Constants.MessageTypes.QueryCID);
    registerNullPrototype(Constants.MessageTypes.QueryCSL);
    registerNullPrototype(Constants.MessageTypes.QueryCType);
    registerNullPrototype(Constants.MessageTypes.QueryConnectedCIDs);
    registerNullPrototype(Constants.MessageTypes.QueryLanguage);
    registerNullPrototype(Constants.MessageTypes.QueryMaxVNID);
    registerNullPrototype(Constants.MessageTypes.QueryNumberType);
    registerNullPrototype(Constants.MessageTypes.QueryRoute);
    registerNullPrototype(Constants.MessageTypes.QueryVNID);
    registerNullPrototype(Constants.MessageTypes.QueryVNName);
    registerNullPrototype(Constants.MessageTypes.QueryVNType);
    registerNullPrototype(Constants.MessageTypes.ReplyCID);
    registerNullPrototype(Constants.MessageTypes.ReplyCSL);
    registerNullPrototype(Constants.MessageTypes.ReplyCType);
    registerNullPrototype(Constants.MessageTypes.ReplyConnectedCIDs);
    registerNullPrototype(Constants.MessageTypes.ReplyLanguage);
    registerNullPrototype(Constants.MessageTypes.ReplyMaxVNID);
    registerNullPrototype(Constants.MessageTypes.ReplyNumberType);
    registerNullPrototype(Constants.MessageTypes.ReplyRoute);
    registerNullPrototype(Constants.MessageTypes.ReplyVNID);
    registerNullPrototype(Constants.MessageTypes.ReplyVNName);
    registerNullPrototype(Constants.MessageTypes.ReplyVNType);
  }
  //----------------------------------------------------------------------
  /**
   * Registers the <tt>elementName</tt> within the NRS namespace as
   * having no prototype object.
   */
  private void registerNullPrototype(String elementName)
  {
    registerPrototype(elementName, Constants.Namespace.NRS_PML, null);
  }
  //----------------------------------------------------------------------
  /*
   * Register a PML XML element type that this registry will handle. If
   * a {@link PML_Element} class is available for decoding such an
   * element, then it can be supplied through
   * <tt>protoType</tt>. Otherwise, if <tt>protoType</tt> is
   * <tt>null</tt>, the element is registered as being able to be
   * processed immediately.
   *
   * @param elementName the simple name of the element, such as "ReplyCID"
   *
   * @param namespace the namespace which defines the element
   *
   * @param protoType a protoType object, or <tt>null</null>
   **/
  private void registerPrototype(String elementName,
                                 String namespace,
                                 PML_Element protoType)
  {
//    if (protoType != null)
//     {
//       PackageLogger.log.fine("Registering PML element <"
//                              + elementName + "> (with prototype class)");
//     }
//     else
//     {
//       PackageLogger.log.fine("Registering PML element <"
//                              + elementName + "> (without prototype class)");
//     }

    String fqn = formQName(namespace, elementName);
    m_processNow.put(fqn, new Boolean(protoType == null));
    m_map.put(fqn, protoType);
  }
  //----------------------------------------------------------------------
  /**
   * Return <tt>true</tt> if the named element can be process
   * immediately, or <tt>false</tt> if a new {@link PML_Element} object
   * is needed to process it.
   */
  public boolean canProcessImmediately(String URI, String localName)
  {
    Boolean b = (Boolean) m_processNow.get(formQName(URI, localName));
    return b.booleanValue();
  }
  //----------------------------------------------------------------------
  /** Return true if the supplied element is known about in this
   * registry */
  public boolean elementSupported(String URI, String localName)
  {
    // look in m_processNow, because not all elements will exists in m_map
    return m_processNow.containsKey(formQName(URI, localName));
  }
  //----------------------------------------------------------------------
  /** Construct and return an instance of the class corresponding to the
   * named PML XML element, or null if an element with name
   * <code>name</code> is not recognised. */
  public PML_Element buildElement(String URI, String name, Attributes atts)
  {
    PML_Element element = (PML_Element) m_map.get(formQName(URI, name));

    if (element == null)
      {
        return null;
      }
    else
      {
        return element.createInstance(atts);
      }
  }
  //----------------------------------------------------------------------
  /**
   * @return the fixed string "PML_Element_Registry"
   */
  public String toString()
  {
    return "PML_Element_Registry";
  }
  //----------------------------------------------------------------------
  /** Combine an XML elment namespace URI and local name in the
   * conventional way to allow comparison to internal and other URI/name
   * pairs. */
  private String formQName(String URI, String localName)
  {
    return URI.toUpperCase() + localName.toUpperCase();
  }
}
