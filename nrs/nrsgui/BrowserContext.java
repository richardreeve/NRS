package nrs.nrsgui;

/**
 * Encapsulates the {@link Node} and {@link NodeCollection} that is the
 * subject of a network viewer.
 *
 * @author Darren Smith
 */
class BrowserContext
{
  private Node m_node;
  private NodeCollection m_nodes;
  private Network m_network;

  //----------------------------------------------------------------------
  /**
   * Creates a context of a specific node.
   */
  BrowserContext(Node node)
  {
    m_node = node;
  }
  //----------------------------------------------------------------------
  /**
   * Creates a context of a specific {@link NodeCollection}
   */
  BrowserContext(NodeCollection nodes)
  {
    m_nodes = nodes;
  }
  //----------------------------------------------------------------------
  /**
   * Creates a context of a specific {@link Network}
   */
  BrowserContext(Network network)
  {
    m_network = network;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link NodeCollection} that should be presented in the
   * browser. Can return <tt>null</tt>, in which there is no subnetwork
   * is show.
   */
  public NodeCollection nodes()
  {
    if (m_node != null)
    {
      return m_node.getChildNodes();
    }

    if (m_nodes != null)
    {
      return m_nodes;
    }

    if (m_network != null)
    {
      return m_network;
    }

    return null;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link Node} that the browser is inspecting. Can return
   * <tt>null</tt>, in which there are no variables to show.
   */
  public Node node()
  {
    if (m_node != null)
    {
      return m_node;
    }

    if (m_nodes != null)
    {
      return m_nodes.getParent();
    }

    if (m_network != null)
    {
      return m_network.getParent();
    }

    return null;
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link Network} that the browser is inspecting, or
   * <tt>null</tt> if there is no {@link Network}.
   */
  public Network network()
  {
    return m_network;
  }
  //----------------------------------------------------------------------
  /**
   * Indicates whether a {@link Node} is available
   */
  public boolean hasNode()
  {
    return (node() != null);
  }
  //----------------------------------------------------------------------
  /**
   * Indicates whether a {@link NodeCollection} is available
   */
  public boolean hasNodes()
  {
    return (nodes() != null);
  }
  //----------------------------------------------------------------------
  /**
   * Indicates whether a {@link Network} is available
   */
  public boolean hasNetwork()
  {
    return (m_network != null);
  }
}
