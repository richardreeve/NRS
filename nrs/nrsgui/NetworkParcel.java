package nrs.nrsgui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Aggregation of data destined for or retrieved from a data file. Both
 * exporting and importing a network involve the construction of a
 * <tt>NetworkParcel</tt> object. The object is a container for all the
 * information that is being sent to (or received from) the file storage
 * (hence it is analogous to a parcel sent through the post).
 *
 * <p>Note, check if the above comment is true. Is this class really
 * used for loading? It's definately used for saving, but not sure about
 * loading.
 *
 * @see ParcelXMLExporter
 */
class NetworkParcel
{
  private double m_progVersion;
  private String m_progName;

  private String m_summary;
  private String m_author;
  private String m_networkName;

  /** List of the nodes that exist at the base level of the network */
  private List m_nodes;

  private List m_NRScomponents = new ArrayList();

  private List m_links = new ArrayList();

  //----------------------------------------------------------------------
  /**
   * Constructor for building a parcel that will later be saved to file.
   * <p>Will throw an exception if parcel couldn't be assembled
   * accurately.
   *
   * @param progVersion version of the program creating this parcel
   *
   * @param progName name of the program creating this parcel
   *
   * @param nodes the {@link List} of nodes to save. Each instance of the
   * list should be of type {@link Node}.
   *
   * @throws Exception if the data required for a save couldn't be
   * collected. Examing the message in the exception object for precise
   * details.
   *
   */
  NetworkParcel(double progVersion,
                String progName,
                List   nodes) throws Exception

  {
    m_progVersion = progVersion;
    m_progName = progName;
    m_nodes = nodes;

    assembleComponentsList();
    collectLinks();
  }
  //----------------------------------------------------------------------
  /**
   * Constructor for building a parcel that will later be saved to file.
   * <p>Will throw an exception if parcel couldn't be assembled
   * accurately.
   *
   * @param progVersion version of the program creating this parcel
   *
   * @param progName name of the program creating this parcel
   *
   * @param network the {@link Network} to save
   *
   * @throws Exception if the data required for a save couldn't be
   * collected. Examing the message in the exception object for precise
   * details.
   */
  NetworkParcel(double progVersion,
                String progName,
                Network network) throws Exception
  {
    m_progVersion = progVersion;
    m_progName = progName;

    m_nodes = new ArrayList();
    for (Iterator i = network.nodeIterator();
         i.hasNext(); )
    {
      m_nodes.add(i.next());
    }

    m_author = network.getAuthor();
    m_networkName = network.getName();
    m_summary = network.getDescription();

    assembleComponentsList();
    collectLinks();
  }
  //----------------------------------------------------------------------
  /**
   * Returns the list of {@link Link} instances that exist in this network
   */
  List getLinks()
  {
    return m_links;
  }
  //----------------------------------------------------------------------
  /**
   * Returns the list of {@link Node} instances that are at the base of
   * the network
   */
  List getNodes()
  {
    return m_nodes;
  }
  //----------------------------------------------------------------------
  /**
   * Returns the author details, or <tt>null</tt> if none available
   */
  String getAuthor()
  {
    return m_author;
  }
  //----------------------------------------------------------------------
  /**
   * Returns the network name, or <tt>null</tt> if none available
   */
  String getName()
  {
    return m_networkName;
  }
  //----------------------------------------------------------------------
  /**
   * Returns the network description, or <tt>null</tt> if none available
   */
  String getDescription()
  {
    return m_summary;
  }
  //----------------------------------------------------------------------
  /**
   * Add all the links belonging to the nodes to this class's links list
   */
  private void collectLinks()
  {
    for (Iterator k = m_nodes.iterator(); k.hasNext(); )
    {
      collectLinks((Node) k.next());
    }
  }
  //----------------------------------------------------------------------
  /**
   * Add all the links belonging to the specified node to this class's
   * links list
   */
  private void collectLinks(Node n)
  {
    // Add links to and from this node
    VariableCollection vars = n.getVariables();
    if (vars != null)
    {
      for (Iterator i = vars.variableIterator(); i.hasNext(); )
      {
        NodeVariable variable = (NodeVariable) i.next();

        for (Iterator j = variable.linkIterator(); j.hasNext(); )
        {
          Link link = (Link) j.next();

          if (!m_links.contains(link))
          {
            m_links.add(link);
          }
        }
      }
    }

    // Add links for child nodes of the current node
    NodeCollection children = n.getChildNodes();
    if (children != null)
    {
      for (Iterator k = children.nodeIterator(); k.hasNext(); )
      {
        collectLinks((Node) k.next());
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Assembles a list of the NRS components (and versions) needed to
   * support the list of nodes that are going to be saved.
   */
  private void assembleComponentsList() throws Exception
  {
    for (Iterator i = m_nodes.iterator(); i.hasNext(); )
    {
      Object o = i.next();
      if (o instanceof Node)
      {
        Node n = (Node) o;

        // Get source CID
        String sourceCID = n.getType().getSourceCID();
        if (sourceCID == null)
        {
          Exception ex = new Exception("Node " + n.VNName() + " has null"
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
          Exception ex = new Exception("Node " + n.VNName() + " with CID "
                                       + sourceCID + " does not refer to"
                                       + " a known component");
          PackageLogger.log.warning(ex.getMessage());
          throw ex;
        }

        m_NRScomponents.add(component);

        PackageLogger.log.fine("### need component " + component);
      }
    }
  }
}
