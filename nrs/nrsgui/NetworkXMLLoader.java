package nrs.nrsgui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import nrs.core.message.Constants;
import nrs.csl.CSL_Element_Attribute;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.csl.CSL_Global_Registry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * Provides functions for loading and parsing an XML document that
 * contains a saved network.
 *
 * @author Darren Smith
 * @author Thomas French
 */
class NetworkXMLLoader extends SharedXMLResources
{
  JFrame m_gui;
  //----------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param frame the {@link JFrame} upon which error dialogs will
   * appear. Can be set to null.
   */
  NetworkXMLLoader(JFrame frame) throws FactoryConfigurationError,
                                        ParserConfigurationException
  {
    m_gui = frame;
  }
  //----------------------------------------------------------------------
  /**
   * Load and parse a file which contains an XML description of a saved
   * network, and then build the network described by the contents. If
   * the load and parse are successful, then the network elements are
   * actually created, which also results in calls to the appropriate
   * online-create functions.
   *
   * @param file the {@link File} to load
   *
   * @param dest the {@link Network} into which the new network will be
   * placed. Can be set to <tt>null</tt> in which case a new network is
   * created.
   *
   * @param isNew indicates whether the network being loaded is new in
   * the sense that it is a new root network that has been
   * constructed. If <tt>true</tt> then the network summary information
   * will be added to <tt>dest</tt>
   *
   * @throws SAXException if any IO errors occur
   * @throws IOException if any SAX parse errors occur
   * @throws IllegalArgumentException if the file is null
   *
   * @return the created network
   */
  Network parse(File file, Network dest, boolean isNew)
    throws SAXException, IOException, IllegalArgumentException
  {
    PackageLogger.log.fine("Loading and parsing file: " + file.toString());
    Document document = builder().parse(file);

    // If we haven't been given a destination network, then create a new
    // one and give it the summary info obtained from the loaded
    // network.
    if (dest == null)
    {
      dest = new Network();
      addSummaryInfo(document.getDocumentElement() , dest);
    }
    else if (isNew)
    {
      addSummaryInfo(document.getDocumentElement() , dest);
    }

    processNodes(document.getDocumentElement(), dest);
    processAllLinks(document.getDocumentElement(), dest);

    return dest;
  }
  //----------------------------------------------------------------------
  /**
   * Process all the <tt>Links</tt> elements that occur at the base of
   * the document
   *
   * @param root the root element
   *
   * @param superNetwork the {@link Network} to put results into
   */
  private void processAllLinks(Element root, Network superNetwork)
  {
    NodeList nl = root.getElementsByTagNameNS(Constants.Namespace.NRS_DNL,
                                              Constants.DNL.Links);

    for (int i = 0; i < nl.getLength(); i++)
    {
      Node xmlNode = nl.item(i);

      if (xmlNode instanceof Element)
      {
        processLinks((Element) xmlNode, superNetwork);
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Process a single <tt>Links</tt> element
   *
   * @param e the <tt>Links</tt> element to process
   *
   * @param superNetwork the {@link Network} to put results into
   */
  private void processLinks(Element e, Network superNetwork)
  {
    //    PackageLogger.log.fine("### processing <" + e.getTagName()+">");
    NodeList nl = e.getElementsByTagNameNS(Constants.Namespace.NRS_DNL,
                                           Constants.DNL.Link);

    for (int i = 0; i < nl.getLength(); i++)
    {
      Node xmlNode = nl.item(i);

      if (xmlNode instanceof Element)
      {
        processLink((Element) xmlNode, superNetwork);
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Process a single <tt>Link</tt> element
   *
   * @param element the {@link Element}
   *
   * @param superNetwork the {@link Network} to put results into
   */
  private void processLink(Element element, Network superNetwork)
  {
    //    PackageLogger.log.fine("### processing <" + element.getTagName()+">");

    String source;
    String target;

    if (element.hasAttribute(Constants.DNL.source))
    {
      source = element.getAttribute(Constants.DNL.source);
    }
    else
    {
      PackageLogger.log.warning("Ignored <Link>, due to missing '"
                                + Constants.DNL.source + "' attribute");
      return;
    }

    if (element.hasAttribute(Constants.DNL.target))
    {
      target = element.getAttribute(Constants.DNL.target);
    }
    else
    {
      PackageLogger.log.warning("Ignored <Link>, due to missing '"
                                + Constants.DNL.target + "' attribute");
      return;
    }

    if ((source == null) || (source.length() == 0))
    {
      PackageLogger.log.warning("Ignored <Link>, due to empty '"
                                + Constants.DNL.source + "' attribute");
      return;
    }
    if ((target == null) || (target.length() == 0))
    {
      PackageLogger.log.warning("Ignored <Link>, due to empty '"
                                + Constants.DNL.target + "' attribute");
      return;
    }

    UserVN sourceUVN = superNetwork.resolveVNName(source);
    UserVN targetUVN = superNetwork.resolveVNName(target);

    if (sourceUVN == null)
    {
      PackageLogger.log.warning("Can't build link from " + source + " to "
                                + target + ": source not found");
      return;
    }

    if (targetUVN == null)
    {
      PackageLogger.log.warning("Can't build link from " + source + " to "
                                + target + ": target not found");
      return;
    }

    if (!(sourceUVN instanceof NodeVariable))
    {
      PackageLogger.log.warning("Can't build link from " + source + " to "
                                + target + ": source is not a variable");
      return;
    }

    if (!(targetUVN instanceof NodeVariable))
    {
      PackageLogger.log.warning("Can't build link from " + source + " to "
                                + target + ": target is not a variable");
      return;
    }

    AppManager.getInstance().accessLinkManager()
      .makeLink((NodeVariable) sourceUVN, (NodeVariable) targetUVN);
  }
  //----------------------------------------------------------------------
  /**
   * Process the <tt>&lt;Nodes&gt;</tt> elements present within a
   * specified element
   *
   * @param base the {@link Element} whose children are to be processed
   *
   * @param superNetwork the {@link Network} to put results into
   */
  private void processNodes(Element base, Network superNetwork)
  {
    NodeList nl = base.getElementsByTagNameNS(Constants.Namespace.NRS_DNL,
                                              Constants.DNL.Nodes);

    for (int i = 0; i < nl.getLength(); i++)
    {
      Node xmlNode = nl.item(i);

      if (xmlNode instanceof Element)
      {
        processNodesContent((Element) xmlNode, superNetwork);
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Process the contents of the <tt>Nodes</tt> element present as
   * children of the element specified by <tt>base</tt>
   *
   * @param base the {@link Element} which contains NRS nodes
   *
   * @param superNetwork the {@link Network} to put results into
   */
  private void processNodesContent(Element base, Network superNetwork)
  {
    NodeList nl = base.getElementsByTagNameNS(Constants.Namespace.NRS_DNL,
                                              Constants.DNL.Component);

    HashMap targetComps = new HashMap();

    // _TODO_(No need to do this if the superNetwork is already attached to a component)
    for (int i = 0; i < nl.getLength(); i++)
    {
      Node xmlNode = nl.item(i);

      if (xmlNode instanceof Element)
      {
        NRSComponent c = identifyNRSComponent((Element) xmlNode);
        targetComps.put(xmlNode, c);
        PackageLogger.log.fine("Matched to component " + c);
      }
    }

    // Loop over the node, and for those with NRS components attached to
    // the GUI, add their networks
    for (Iterator i = targetComps.keySet().iterator(); i.hasNext(); )
    {
      Node xmlNode = (Node) i.next();
      NRSComponent component = (NRSComponent) targetComps.get(xmlNode);

      if (component != null)
      {
        addNetwork((Element) xmlNode, component, superNetwork);
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Parse the DOM tree beginning at <tt>base</tt> and add all found
   * nodes to the specified {@link NodeCollection}, where all nodes
   * found are to be associated with the remote {@link NRSComponent}
   * indicated by <tt>comp</tt>. This is the method that does the actual
   * creation of NRS nodes, and, makes a call to the online create
   * service.
   */
  private void addNetwork(Element base, NRSComponent comp,
                          NodeCollection network)
  {
    //    CSL_Global_Registry.getInstance().comp.getCID();
    NodeList list = base.getChildNodes();

    for (int i = 0; i < list.getLength(); i++)
    {
      if (list.item(i) instanceof Element)
      {
        Element element = (Element) list.item(i);

        CSL_Element_NodeDescription type =
          CSL_Global_Registry.getInstance().findType(element.getTagName(),
                                                     comp);

        if (type == null)
        {
          PackageLogger.log.warning("Ignoring <" + element.getTagName() +
                                    ">, no corresponding node type on "
                                    +"component " + comp);
        }
        else
        {
          if (network.canAdd(type,1))
          {
            List attrList = buildAttributes(element, type);

            int x = getIntAttribute(element, Constants.DNL.x, 0);
            int y = getIntAttribute(element, Constants.DNL.y, 0);

            if (attrList != null)
            {
              DefaultNode newNode = new DefaultNode(type,
                                                    attrList, x, y,
                                                    network,
                                                    network.getParent());     
              
              //AppManager.getInstance().getProgrammer().onlineCreate(newNode);
              AppManager.getInstance().getJobManager()
                .add(new CreateNodeJob(AppManager.getInstance()
                                       .getProgrammer(), newNode));
              
              // recurse into the XML sub-nodes
              addNetwork(element, comp, newNode.getChildNodes());
            }
          }
          else
          {
            PackageLogger.log.warning("Can't add "
                                      + type +
                                      " node - limit reached");
          }
        }
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to extract an integer value from the named attribute in the
   * specified XML element
   *
   * @return the integer value extracted, or the default value provided
   * if there was any kind of problem.
   */
  private int getIntAttribute(Element element, String attrName,
                              final int defaultValue)
  {
    if (element.hasAttribute(attrName))
    {
      try
      {
        return Integer.parseInt(element.getAttribute(attrName));
      }
      catch (NumberFormatException e)
      {
      }
    }

    return defaultValue;
  }
  //----------------------------------------------------------------------
  /**
   * Creates and returns a {@link List} of {@link NodeAttribute}
   * instances based on the XML attributes of the specified XML element.
   *
   * @param element the XML {@link Element} whose attributes will be read
   *
   * @param type a {@link CSL_Element_NodeDescription} which is the type
   * of NRS <tt>node</tt> being created
   *
   * @return the list of attributes, or <tt>null</tt> if that list could
   * not be correctly formed.
   */
  private List buildAttributes(Element element,
                               CSL_Element_NodeDescription type)
  {
    List list = new ArrayList();

    for (Iterator i = type.getAttributesIterator(); i.hasNext(); )
    {
      CSL_Element_Attribute attr = (CSL_Element_Attribute) i.next();
      String value;

      if (attr.getIsConst())
      {
        value = attr.getDefaultValue();
      }
      else
      {
        if (element.hasAttribute(attr.getAttributeName()))
        {
          value = element.getAttribute(attr.getAttributeName());
        }
        else
        {
          value = attr.getDefaultValue();
        }
      }

      // verify
      if (attr.getMustHave() && value == null)
      {
        PackageLogger.log.warning("Attribute '"
                                  + attr.getAttributeName()
                                  + "' of node '"
                                  + type.getNodeDescriptionName()
                                  + "' must be provided, but no value"
                                  + " found in XML. Can't create node.");
        return null;
      }

      if (value != null)
      {
        NodeAttribute na = new NodeAttribute(attr);
        na.getValueDelegate().set(value);
        list.add(na);
      }
    }

    return list;
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to identify an {@link NRSComponent} which corresponds to
   * the specified {@link Element}
   */
  private NRSComponent identifyNRSComponent(Element element)
  {

    String cType = element.getAttribute(Constants.DNL.cType);
    String cVersion = element.getAttribute(Constants.DNL.cVersion);

    if ((cType == null) || (cVersion == null))
    {
      PackageLogger.log.warning("Missing cType and/or cVersion in <"
                                + Constants.DNL.Component + ">");
      return null;
    }

    List components = AppManager.getInstance()
      .getComponentManager().getComponents(cType, cVersion);

    //    PackageLogger.log.fine("### matching components=" + components.size());

    if (components.size() == 0)
    {
      final String errMsg = "No component '"
        + cType + " (" + cVersion + ")' attached.\nUnable to load network.";

      PackageLogger.log.warning(errMsg);
      
      new Thread(){
        public void run(){
          JOptionPane.showMessageDialog(m_gui,
                                        errMsg,
                                        "Open Network",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }.start();
      return null;
    }
    else if (components.size() == 1)
    {
      return (NRSComponent) components.get(0);
    }
    else // component size > 1
    {
      HashMap map = new HashMap();
      for (Iterator i = components.iterator(); i.hasNext(); )
      {
        NRSComponent c = (NRSComponent) i.next();
        map.put(c.toString(), c);
      }

      String s = (String)
        JOptionPane.showInputDialog(m_gui,
                                    "Select the NRS component to provide:\n"
                                    + "'" + cType + " (" + cVersion + ")'",
                                    "Open Network",
                                    JOptionPane.PLAIN_MESSAGE,
                                    null,
                                    map.keySet().toArray(),
                                    map.keySet().toArray()[0]);

      // user clicked 'cancel'
      if (s == null) return null;

      return (NRSComponent) map.get(s);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Extract the summary information from the XML document and add it to
   * the <tt>network</tt>
   */
  private void addSummaryInfo(Element root, Network n)
  {
    n.setAuthor(extractAuthor(root));
    n.setName(extractName(root));
  }
  //----------------------------------------------------------------------
  /**
   * Extract and return the author string from the specified root
   * element. Can return null, to indicate no author information was
   * extracted.
   */
  private String extractAuthor(Element root)
  {
    return extractAttributeFromElement(root,
                                       Constants.DNL.Author,
                                       Constants.DNL.name);
  }
  //----------------------------------------------------------------------
  /**
   * Extract and return the name string from the specified root
   * element. Can return null, to indicate no information was extracted.
   */
  private String extractName(Element root)
  {
    return extractAttributeFromElement(root,
                                       Constants.DNL.Name,
                                       Constants.DNL.name);
  }
  //----------------------------------------------------------------------
  /**
   * Extract and return the value of a named attribute from a named
   * element belonging to the specified element, or <tt>null</tt> if
   * there is no match.
   */
  private String extractAttributeFromElement(Element owner,
                                             String elementName,
                                             String attributeName)
  {
    String value = null;

    NodeList nl = owner.getElementsByTagNameNS(Constants.Namespace.NRS_DNL,
                                               elementName);

    if (nl.getLength() > 0)
    {
      if (nl.item(0) instanceof Element)
      {
        Element e = (Element) nl.item(0);
        value = e.getAttribute(attributeName);

        PackageLogger.log.fine("Node.nodeName=" + e.getNodeName());
        PackageLogger.log.fine("Node.namespaceURI=" + e.getNamespaceURI());
        PackageLogger.log.fine("Node.prefix=" + e.getPrefix());
        PackageLogger.log.fine("Node.localName=" + e.getLocalName());
        PackageLogger.log.fine("---------------");
      }
    }
    else
    {
      PackageLogger.log.warning("Element <" + owner.getTagName()  + "> has no "
                                + "<" + elementName + "> child");
    }

    return value;
  }
}
