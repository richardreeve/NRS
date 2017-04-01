package nrs.nrsgui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observer;
import java.util.Observable;
import java.util.Set;
import nrs.core.base.ComponentInfo;
import nrs.core.base.Message;
import nrs.core.message.Constants;
import nrs.csl.CSL_Element_Attribute;
import nrs.csl.CSL_Element_NodeDescription;
import nrs.util.job.Job;
import nrs.util.job.JobManager;

/*
 * Default implementation of a {@link Node} interface.
 */
public class DefaultNode implements Node
{
  /** Reference to the type of this node */
  protected final CSL_Element_NodeDescription m_type;

  /** List of the attributes. Each element is of type {@link
   * NodeAttribute} */
  protected final Collection m_attrs;

  /** Name of the node  */
  protected String m_name;

  /** Object used for display */
  protected DisplayDelegate m_dd;

  /** Internal CSL-style variables */
  protected VariableCollection m_variables;

  /** Child nodes */
  protected NodeCollection m_children = new DefaultNodeCollection(this);

  /** Container in which self in located */
  protected NodeCollection m_container;

  /** Parent node */
  private Node m_parent;

  /** Info known about remote object */
  private RemoteVNInfo m_remoteInfo;

  /** Info about the component this node traces back to */
  private ComponentInfo m_component;

  /** Node state */
  private NodeState m_state;

  /** Listeners, each of type NodeListener */
  private HashSet m_listeners = new HashSet();

  //----------------------------------------------------------------------
  /**
   * Constructor. Creates a new object, representing a Node of the
   * specified type. The newly constructed {@link Node} object is
   * automatically added to the {@link NodeManager} instance owned by
   * the {@link AppManager}. All the variables that belong to this node
   * are also added to the <tt>NodeManager</tt>.
   *
   * @param type a {@link CSL_Element_NodeDescription} specifying the
   * type of the node to create
   *
   * @param attrs a {@link Collection} containing a set of {@link
   * NodeAttribute} objects which provide the initial set of attribute
   * values
   *
   * @param x inital X position of the GUI representation of the node
   *
   * @param y inital Y position of the GUI representation of the node
   *
   * @param container the {@link NodeCollection} that is to contain this
   * <tt>node</tt>. During construction this <tt>node</tt> will add
   * itself to the container. The container should never be
   * <tt>null</tt>.
   *
   * @param parent the {@link Node} which will contain self. Can be
   * <tt>null</tt>.
   */
  DefaultNode(CSL_Element_NodeDescription type,
              Collection attrs,
              int x, int y,
              NodeCollection container,
              Node parent)
  {
    m_type = type;
    m_attrs = attrs;
    m_name = resolveName();
    m_container = container;
    m_parent = parent;
    m_remoteInfo = new RemoteVNInfo(this);
    m_state = new NodeState(this, m_listeners);
    m_dd = new GuiNode(this, x, y);
    m_variables = new DefaultVariableCollection(this);

    identifyComponent();

    // UserVN registration
    AppManager.getInstance().getNodeManager().add(this);
    for (Iterator<NodeVariable> i = m_variables.iterator(); i.hasNext(); )
    {
      AppManager.getInstance().getNodeManager().add(i.next());
    }

    m_container.add(this);
  }
  //----------------------------------------------------------------------
  private void identifyComponent()
  {
    m_component = AppManager.
      getInstance().getComponentManager().
      getComponent(m_type.getSourceCID());

    if (m_component == null)
    {
      PackageLogger.log.warning("Cannot identify an NRS component for"
                                + " new node "
                                + this
                                + ", which has CID=" + m_type.getSourceCID());
    }
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public String toString()
  {
    return m_name;
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public String name()
  {
    return m_name;
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public void delete()
  {
    JobManager jobMan = AppManager.getInstance().getJobManager();
    Programmer prog = AppManager.getInstance().getProgrammer();

    ArrayList<Link> links = new ArrayList<Link>();
    // delete any links variables have
    for(Iterator<NodeVariable> i = getVariables().iterator(); 
        i.hasNext(); ){
      NodeVariable nv = i.next();
      
      Iterator<Link> i2 = nv.linkIterator(); 
      while(i2.hasNext())
        {
          Link link = i2.next();

          // Can't use LinkManager.removeLink() because of 
          // ConcurrentModificationException
          jobMan.add(new DeleteLinkJob (prog, link));

          // remove link ref from variable at other end of link
          if ( link.getSource() == nv )
            link.getTarget().removeLink(link);
          else
            link.getSource().removeLink(link);
          
          // add to list to remove after - can't call method to remove 
          // because of ConcurrentModificationException
          links.add(link);

          link.setExists(false);
        }
      
      // remove link references
      for(Link l : links)
      {
        nv.removeLink(l);
      }      
    }
    
    // delete child nodes recursively
    for (Iterator i = m_children.nodeIterator(); i.hasNext(); )
    {
      Node n = (Node) i.next();
      n.delete();
    }

    // Delete myself last - do remote delete job
    final Job job = new DeleteNodeJob(prog, this);

    // add Observer object callback
    job.addObserver(new Observer(){
        public void update(Observable o, Object arg){
          if ( job.getPhase() == Job.COMPLETE ){
            // UserVN de-registration
            AppManager.getInstance().getNodeManager().remove(DefaultNode.this);
            for (Iterator<NodeVariable> i = m_variables.iterator(); 
                 i.hasNext(); )
            {
              AppManager.getInstance().getNodeManager().remove(i.next());
            }
            // Raise events for listeners
            for (Iterator i = m_listeners.iterator(); i.hasNext(); )
            {
              NodeListener l = (NodeListener) i.next();
              
              l.nodeDeleted(DefaultNode.this);
            }          
          }
        }
      });
    // add to jobManager
    jobMan.add(job);
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public void attrsModified()
  {
    m_state.nodeUpdated();
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public CSL_Element_NodeDescription getType()
  {
    return m_type;
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public Iterator attributeIterator()
  {
    return m_attrs.iterator();
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public DisplayDelegate getPainter()
  {
    return m_dd;
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public NodeCollection getChildNodes()
  {
    return m_children;
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public VariableCollection getVariables()
  {
    return m_variables;
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public Node getParent()
  {
    return m_parent;
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public String VNName()
  {
    // danger, recursion!
    if (getParent() == null)
    {
      return toString();
    }
    else
    {
      return getParent().VNName() + "." + toString();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to assign a name label for this node. Kept as private
   * because it's called from the constructor - which means it would be
   * dangerous to allow derived classes to override this, in the hope
   * that the base class constructor would call the overridden
   * resolveName() method.
   */
  private String resolveName()
  {
    // default value
    String retVal = "";

    Iterator iter = m_attrs.iterator();
    NodeAttribute nodeAttr;

    while ((iter.hasNext()) && (m_name == null))
      {
        nodeAttr = (NodeAttribute) iter.next();

        if ((nodeAttr.getCSL().getAttributeName()
             .equals(CSL_Element_Attribute.NODE_ATTRIBUTE_NAME)) &&
            (nodeAttr.getCSL().getInNRSNamespace() == true))
          {
            retVal = nodeAttr.getValue().toString();
          }
      }

    return retVal;
  }
  //----------------------------------------------------------------------
  // Implements base class method
  public RemoteVNInfo remote()
  {
    return m_remoteInfo;
  }
  //----------------------------------------------------------------------
  /**
   * Get {@link ComponentInfo} about the NRS component which this node
   * traces to. Generally this shouldn't return null.
   */
  public ComponentInfo getComponent()
  {
    return m_component;
  }
  //----------------------------------------------------------------------
  public void handleReplyVNID(Message m)
  {
    String vnid = m.getField(Constants.MessageFields.vnid);
    if (vnid == null) return;

    try
    {
      m_remoteInfo.setVNID(Integer.parseInt(vnid));
      m_state.vnidProvided();
    }
    catch (NumberFormatException exception)
    {
      PackageLogger.log.warning("Expected integer value for VNID"
                                + " but received " + vnid);
      m_remoteInfo.reset();
    }
  }
  //----------------------------------------------------------------------
  public void addNodeListener(NodeListener l)
  {
    m_listeners.add(l);
  }
  //----------------------------------------------------------------------
  public void removeNodeListener(NodeListener l)
  {
    m_listeners.remove(l);
  }
  //----------------------------------------------------------------------
  public boolean inSync()
  {
    return m_state.getSyncState();
  }
  //----------------------------------------------------------------------
  /**
   * Indicates whether the remote node has been created. This is
   * achieved by examining the {@link RemoteVNInfo} object.
   */
  public boolean remoteCreated()
  {
    return m_remoteInfo.isKnown();
  }
  //----------------------------------------------------------------------
  public NodeCollection getContainer()
  {
    return m_container;
  }
  //----------------------------------------------------------------------
  public RootNetwork findRoot()
  {
    if (m_container instanceof RootNetwork)
    {
      return (RootNetwork) m_container;
    }

    if (getParent() != null)
    {
      return getParent().findRoot();
    }

    // So, current network is not root, and there is no parent, then
    // something has gone wrong... so return null
    return null;
  }
  //----------------------------------------------------------------------
  public UserVN resolveVNName(String vnName)
  {
    if (vnName.length() == 0)
    {
      PackageLogger.log.warning("VNName resolution failure: vnName"
                                + " is empty inside node " + this);
      return null;
    }

    int dot = vnName.indexOf('.');

    if (dot != -1 )
    {
      // There is a dot, so there is atleast another network to recurse
      // through. So forward to the child node collection

      if (getChildNodes() == null)
      {
        PackageLogger.log.warning("VNName resolution failure: vnName="
                                  + vnName + " inside node " + this
                                  + " but there is no sub-network to"
                                  + " continue search in");
        return null;
      }
      return getChildNodes().resolveVNName(vnName);
    }
    else
    {
      // This is no dot, so, the final part of the name should identify
      // a variable within this node.

      if (getVariables() == null)
      {
        PackageLogger.log.warning("VNName resolution failure: vnName="
                                  + vnName + " inside node " + this
                                  + " but there are no variables to"
                                  + " continue search in");
        return null;
      }
      return getVariables().resolveVNName(vnName);
    }
  }
} // ends DefaultNode class


/**
 * Represents the synchronized state of a {@link Node} instance. The
 * synchronized state indicates whether there are any local changes that
 * have not been propagated to the remote NRS component.
 */
class NodeState
{
  /** List of node listeners, each of type NodeListener - to be notified
   * on state changes*/
  private Set m_listeners;

  /** Link back to the owning node */
  private Node m_node;

  /** State of this node. Will be one of the state constant contained
    * within thiis class. */
  private String m_state;

  static final String S_LCONSTRUCTED = "Constructed locally";
  static final String S_RCONSTRUCTED = "Constructed remotely";
  static final String S_LMODIFIED = "Modified locally";
  static final String S_RMODIFIED = "Modified remotely";

  //----------------------------------------------------------------------
  /** Constructor. The initial state is {@link NodeState#S_LCONSTRUCTED}
   */
  NodeState(Node node, Set listeners)
  {
    m_node = node;
    m_listeners = listeners;
    m_state = S_LCONSTRUCTED;
  }
  //----------------------------------------------------------------------
  /**
   * Return the synchronised state. Returns <tt>true</tt> if this
   * instance is up-to-date with the remote NRS object, <tt>false</tt>
   * otherwise.
   */
  boolean getSyncState()
  {
    if (m_state == S_RCONSTRUCTED) return true;
    if (m_state == S_RMODIFIED) return true;

    return false;
  }
  //----------------------------------------------------------------------
  /**
   * Should be called to indicate that the attribute values of a node
   * have been altered by the user.
   */
  void nodeUpdated()
  {
    if (m_state != S_LMODIFIED)
    {
      boolean prevSyncState = getSyncState();

      m_state = S_LMODIFIED;

      // Raise events for listeners
      for (Iterator i = m_listeners.iterator(); i.hasNext(); )
      {
        NodeListener l = (NodeListener) i.next();

        l.nodeModified(m_node);

        // Now that the node has been modified locally, it will be
        // obviously out of sync. If it was previously in-sync, then
        // change the sync-status.
        if (prevSyncState) l.nodeSyncChanged(m_node);
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Should be called to indicate the remote NRS component has been
   * constructed, and its VNID has been received
   */
  void vnidProvided()
  {
    if (m_state == S_LCONSTRUCTED)
    {
      setState(S_RCONSTRUCTED);

      for (Iterator i = m_listeners.iterator(); i.hasNext(); )
      {
        NodeListener l = (NodeListener) i.next();

        l.nodeSyncChanged(m_node);
      }

    }
  }
  //----------------------------------------------------------------------
  /**
   * Set the state to a new value. If this results in a state change
   * then observers are notified, with the owner {@link Node} passed as
   * the second argument.
   */
  private void setState(String newState)
  {
    if (m_state != newState)
    {
      m_state = newState;
    }
  }
} // ends NodeState class
