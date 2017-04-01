package nrs.nrsgui;

import java.util.ArrayList;
import java.util.Iterator;
import nrs.core.base.Message;
import nrs.core.message.Constants;
import nrs.csl.CSL_Element_Variable;

/*
 * A single instance of a CSL-style variable, belonging to a {@link
 * Node} instance.
 */
class NodeVariable implements UserVN
{
  /** Fixed type details of this variable */
  private CSL_Element_Variable m_type;

  /** Object used for display */
  private DisplayDelegate m_dd;

  /** Node which owns self */
  private Node m_parent;

  /** Info known about remote object */
  private RemoteVNInfo m_remoteInfo;

  /** List of links associated with this variable */
  private ArrayList m_links = new ArrayList();

  // Summary of links counts
  private int m_inboundCount, m_outboundCount, m_logCount;

  private ArrayList<VariableListener> m_listeners = new ArrayList();

  //----------------------------------------------------------------------
  /**
   * Constructor. Create a new instance of a variable of type defined by
   * the {@link CSL_Element_Variable} parameter.
   *
   * @param type a {@link CSL_Element_Variable} which describes the type
   * of the variable.
   *
   * @param x the initial x-coordinate of the graphical representation.
   *
   * @param y the initial y-coordinate of the graphical representation.
   */
  NodeVariable(CSL_Element_Variable type, int x, int y, Node parent)
  {
    if (type == null) throw new NullPointerException("type is null");
    if (parent == null) throw new NullPointerException("parent is null");

    m_type = type;
    m_parent = parent;
    m_remoteInfo = new RemoteVNInfo(this);
    m_dd = new GuiVariable(this, x, y);
  }
  //----------------------------------------------------------------------
  /**
   * Add a {@link VariableListener} to this variable
   */
  void addListener(VariableListener l)
  {
    m_listeners.add(l);
  }
  //----------------------------------------------------------------------
  /**
   * Remove a {@link VariableListener} from this variable
   */
  void removeListener(VariableListener l)
  {
    m_listeners.remove(l);
  }
  //----------------------------------------------------------------------
  /**
   * Provide summary information about the inbound links to this variable
   */
  int getInboundLinksCount()
  {
    return m_inboundCount;
  }
  //----------------------------------------------------------------------
  /**
   * Provide summary information about the inbound links to this variable
   */
  int getOutboundLinksCount()
  {
    return m_outboundCount;
  }
  //----------------------------------------------------------------------
  /**
   * Provide summary information about the inbound links to this variable
   */
  int getLogLinksCount()
  {
    return m_logCount;
  }
  //----------------------------------------------------------------------
  /**
   * Returns type information
   */
  CSL_Element_Variable getType()
  {
    return m_type;
  }
  //----------------------------------------------------------------------
  /**
   * Update the link-count summary information
   */
  private void updateCounts()
  {
    m_inboundCount = 0;
    m_outboundCount = 0;
    m_logCount = 0;
    for (Iterator i = linkIterator(); i.hasNext(); )
    {
      Link link = (Link) i.next();

      if (link.getSource() == this) m_outboundCount++;
      if (link.getTarget() == this) m_inboundCount++;
      // _TODO_(How do I detect links which are of log type?)
    }
    //    PackageLogger.log.fine("!!! " + this + " inboundCount=" + m_inboundCount);
    //    PackageLogger.log.fine("!!! " + this + " outboundCount=" + m_outboundCount);
  }
  //----------------------------------------------------------------------
  /**
   * Associate a link with this variable
   */
  void addLink(Link link)
  {
    m_links.add(link);
    updateCounts();
    for (VariableListener l : m_listeners) l.linkAdded(this);
  }
  //----------------------------------------------------------------------
  /**
   * Get a standard iterator over the {@link Link} objects associated
   * with this variable
   */
  public Iterator linkIterator()
  {
    return m_links.iterator();
  }
  //----------------------------------------------------------------------
  /**
   * Remove an existing link
   */
  void removeLink(Link link)
  {
    // TODO - DONE? T.French
    m_links.remove(link);
    updateCounts();
    for (VariableListener l : m_listeners) l.linkRemoved(this);
  }
  //----------------------------------------------------------------------
  /**
   * String name of underlying variable
   */
  public String toString()
  {
    return m_type.toString();
  }
  //----------------------------------------------------------------------
  // Implements base class
  public DisplayDelegate getPainter()
  {
    return m_dd;
  }
  //----------------------------------------------------------------------
  // Implements base class
  public Node getParent()
  {
    return m_parent;
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public String VNName()
  {
    if (m_parent != null)
    {
      return m_parent.VNName() + "." + name();
    }
    else
    {
      return name();
    }
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public String name()
  {
    return m_type.getVariableName();
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public RemoteVNInfo remote()
  {
    return m_remoteInfo;
  }
  //----------------------------------------------------------------------
  public void handleReplyVNID(Message m)
  {
    String vnid = m.getField(Constants.MessageFields.vnid);
    if (vnid == null) return;

    try
    {
      m_remoteInfo.setVNID(Integer.parseInt(vnid));
    }
    catch (NumberFormatException exception)
    {
      PackageLogger.log.warning("Expected integer value for VNID"
                                + " but received " + vnid);
      m_remoteInfo.reset();
    }
  }
}
