package nrs.nrsgui;

import java.util.Iterator;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import nrs.core.message.Constants;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Provides functions for creating an XML-document object ({@link
 * Document}) to represent the contents of {@link NetworkParcel}
 * instances.
 */
class ParcelXMLExporter extends SharedXMLResources
{
  private DOMImplementation m_docBuilder;

  //----------------------------------------------------------------------
  /**
   * Constructor. Note the exceptions that are thrown. If any occur then
   * there will be no XML generation for the duration of the application
   * session (a serious error).
   */
  ParcelXMLExporter() throws FactoryConfigurationError,
                             ParserConfigurationException
  {
    m_docBuilder = builder().getDOMImplementation();
  }
  //----------------------------------------------------------------------
  /**
   * Create an XML document of the specifed {@link NetworkParcel}
   * instance.
   *
   * @throws DOMException if the document could not be created. More
   * specific reasons for such failed are likey contained within the
   * exception message.
   *
   * @throws Exception for all other cases when the document could not
   * be created
   */
  Document export(NetworkParcel n) throws DOMException, Exception
  {
    Document d = newDocument();

    addNamspaceAttribute(d);

    addAuthorInfo(d, n);
    addNameInfo(d, n);
    addDescriptionInfo(d, n);
    addNodes(d, n);
    addLinks(d, n);

    return d;
  }
  //----------------------------------------------------------------------
  /**
   * Explicitly add the XMLNS namespace attribute to the root element of
   * the document. This is done because that Java JAXP to-text
   * transformer does not add namepace information. So as hack we just
   * add it manually.
   */
  private void addNamspaceAttribute(Document d)
  {
    d.getDocumentElement().setAttribute("xmlns",
                                        Constants.Namespace.NRS_DNL);
  }
  //----------------------------------------------------------------------
  /**
   * Represent a single NRS {@link nrs.nrsgui.Node} as an XML {@link
   * org.w3c.dom.Node} and add it to the specified <tt>node</tt>
   */
  private void addNode(nrs.nrsgui.Node node,
                       org.w3c.dom.Node dest,
                       Document doc)
  {
    Element e = newElement(doc, node.getType().getNodeDescriptionName());
    dest.appendChild(e);

    for (Iterator i = node.attributeIterator(); i.hasNext(); )
    {
      NodeAttribute attr = (NodeAttribute) i.next();

      e.setAttribute(attr.getCSL().getAttributeName(),
                     attr.getValue().toString());
    }

    // Supplimentary info _TODO_(qualify with a namespace)
    e.setAttribute(Constants.DNL.x, Integer.toString(node.getPainter().x()));
    e.setAttribute(Constants.DNL.y, Integer.toString(node.getPainter().y()));

    for (Iterator i = node.getChildNodes().nodeIterator(); i.hasNext(); )
    {
      nrs.nrsgui.Node child = (nrs.nrsgui.Node) i.next();
      addNode(child, e, doc);
    }
  }
  //----------------------------------------------------------------------
  private NRSComponent getNRSComponent(nrs.nrsgui.Node node) throws Exception
  {
    // Get source CID
    String sourceCID = node.getType().getSourceCID();

    if (sourceCID == null)
    {
      Exception ex = new Exception("Node " + node.VNName() + " has null"
                                   + " CID for source component");
      PackageLogger.log.warning(ex.getMessage());
      throw ex;
    }

    // Look up the corresponding component
    NRSComponentManager cMan
      = AppManager.getInstance().getComponentManager();

    NRSComponent component = cMan.getComponent(sourceCID);
    if (component == null)
    {
      Exception ex = new Exception("Node " + node.VNName() + " with CID "
                                   + sourceCID + " does not refer to"
                                   + " a known component");
      PackageLogger.log.warning(ex.getMessage());
      throw ex;
    }

    return component;
  }
  //----------------------------------------------------------------------
  /**
   * Convert a subnetwork into a heirarchy of XML elements, begining
   * with the <tt>Component</tt> element.
   */
  private void addAddressedNode(Document d, nrs.nrsgui.Node node,
                                org.w3c.dom.Node parent)
    throws Exception
  {
    NRSComponent component = getNRSComponent(node);

    Element componentElement = buildComponentElement(d, component);
    parent.appendChild(componentElement);

    addNode(node, componentElement, d);
  }
  //----------------------------------------------------------------------
  private Element buildComponentElement(Document d,
                                        NRSComponent component)
  {
    Element e = newElement(d, Constants.DNL.Component);
    e.setAttribute(Constants.DNL.cType, component.getCType());
    e.setAttribute(Constants.DNL.cVersion, component.getCVersion());
    return e;
  }
  //----------------------------------------------------------------------
  /**
   * Add the <tt>Nodes</tt> element
   */
  private void addNodes(Document d, NetworkParcel n)
    throws DOMException, Exception
  {
    Element e = newElement(d, Constants.DNL.Nodes);
    d.getDocumentElement().appendChild(e);
    for (Iterator i = n.getNodes().iterator(); i.hasNext(); )
    {
      PackageLogger.log.fine("### adding subnode");
      addAddressedNode(d, (nrs.nrsgui.Node) i.next(), e);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Add the <tt>Links</tt> element
   */
  private void addLinks(Document d, NetworkParcel n)
  {
    Element e = newElement(d, Constants.DNL.Links);
    d.getDocumentElement().appendChild(e);
    for (Iterator i = n.getLinks().iterator(); i.hasNext(); )
    {
      addLink(d, (nrs.nrsgui.Link) i.next(), e);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Add a single <tt>Link</tt> element
   */
  private void addLink(Document d, nrs.nrsgui.Link link, Element parent)
  {
    Element e = newElement(d, Constants.DNL.Link);
    parent.appendChild(e);

    NodeVariable source = link.getSource();
    NodeVariable target = link.getTarget();

    e.setAttribute(Constants.DNL.source, source.VNName());
    e.setAttribute(Constants.DNL.target, target.VNName());
  }
  //----------------------------------------------------------------------
  /**
   * Generates a new, empty, XML document
   */
  private Document newDocument() throws DOMException
  {
    return m_docBuilder.createDocument(Constants.Namespace.NRS_DNL,
                                       Constants.DNL.Network,
                                       null);


  }
  //----------------------------------------------------------------------
  /**
   * Generates a new XML element of the specified tag name
   */
  private Element newElement(Document d, String tagName) throws DOMException
  {
    Element e = d.createElementNS(Constants.Namespace.NRS_DNL, tagName);

    //    PackageLogger.log.fine("Node.nodeName=" + e.getNodeName());
    //    PackageLogger.log.fine("Node.namespaceURI=" + e.getNamespaceURI());
    //    PackageLogger.log.fine("Node.prefix=" + e.getPrefix());
    //    PackageLogger.log.fine("Node.localName=" + e.getLocalName());
    //    PackageLogger.log.fine("---------------");

    return e;
  }
  //----------------------------------------------------------------------
  /**
   * Generates a new XML text section
   */
  private Text newTextNode(Document d, String text) throws DOMException
  {
    return d.createTextNode(text);
  }
  //----------------------------------------------------------------------
  /**
   * Creates an <tt>&lt;Author&gt;</tt> element and adds it to the root
   * element of the document
   */
  private void addAuthorInfo(Document d,
                             NetworkParcel n) throws DOMException
  {
    if (n.getAuthor() != null)
    {
      Element e = newElement(d, Constants.DNL.Author);
      e.setAttribute(Constants.DNL.name, n.getAuthor());
      d.getDocumentElement().appendChild(e);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Creates a <tt>&lt;Name&gt;</tt> element and adds it to the root
   * element of the document
   */
  private void addNameInfo(Document d,
                           NetworkParcel n) throws DOMException
  {
    if (n.getName() != null)
    {
      Element e = newElement(d, Constants.DNL.Name);
      e.setAttribute(Constants.DNL.name, n.getName());
      d.getDocumentElement().appendChild(e);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Creates a <tt>&lt;Description&gt;</tt> element and adds it to the root
   * element of the document
   */
  private void addDescriptionInfo(Document d,
                                  NetworkParcel n) throws DOMException
  {
    if (n.getDescription() != null)
    {
      Element e = newElement(d, Constants.DNL.Description);
      e.appendChild(newTextNode(d, n.getDescription()));
      d.getDocumentElement().appendChild(e);
    }

  }
}
