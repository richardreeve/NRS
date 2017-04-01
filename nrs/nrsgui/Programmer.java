package nrs.nrsgui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import nrs.core.base.Message;
import nrs.core.base.MessageException;
import nrs.core.base.MessageTools;
import nrs.core.base.OutboundPipeline;
import nrs.core.message.Constants;
import nrs.core.message.CreateLink;
import nrs.core.message.CreateNode;
import nrs.core.message.DeleteLink;
import nrs.core.message.DeleteNode;
import nrs.core.message.QueryVNID;

import static nrs.core.message.Constants.MessageTypes.CreateLink;
import static nrs.core.message.Constants.MessageTypes.DeleteLink;
import static nrs.core.message.Constants.MessageTypes.ReplyVNID;

/**
 * This class implements the collection of operations that allow a
 * network design, as represented by the nodes created and linked
 * together by the user, to be programmed into remote NRS components.
 *
 * <p>This class is designed for use only within the nrs.nrsgui package:
 * hence it does not have public accessibility.</p>
 *
 * @author Darren Smith
 */
class Programmer
{
  /** Pipeline to use for sending messages */
  private OutboundPipeline m_outPipeline;

  // Default hop count number
  private final String HOP = "10";

  // List of links to create
  private Set m_linksTodos = new HashSet();

  // Online/offline indicator
  private ConfigParameterBoolean m_lineStatus;

  //----------------------------------------------------------------------
  /**
   * Constructor
   */
  Programmer(Settings settings)
  {
    m_lineStatus = (ConfigParameterBoolean)
      settings.get(NRSGUIConstants.Settings.ONLINE);
  }
  //----------------------------------------------------------------------
  /**
   * Verify the specified node on the remote NRS component which hosts
   * its. This should result in a set of relies which can be used to
   * determine how accurately the specified node (and its children) is
   * represented on the NRS component.
   */
  void remoteVerify(Node n)
  {
    verify(n, findRoute(n));
  }
  //----------------------------------------------------------------------
  /**
   * Verify the specified variable on the remote NRS component which
   * hosts its.
   */
  void remoteVerify(NodeVariable nv)
  {
    verify(nv, findRoute(nv.getParent()));
  }
  //----------------------------------------------------------------------
  /**
   * Returns true is we are currently in online mode, false otherwise
   */
  private boolean isOnline()
  {
    return (m_lineStatus.value() == true);
  }
  //----------------------------------------------------------------------
  /**
   * Create a remote link if the GUI is online. If not online, nothing
   * is done.
   *
   * @throws a {@link MessageException} if a message could not be
   * constructed
   */
  void onlineCreate(Link link) throws MessageException
  {
    if (isOnline())
    {
      sendCreateLink(link);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Delete a remote link if the GUI is online. If not online, nothing
   * is done.
   *
   * @throws a {@link MessageException} if a message could not be
   * constructed
   */
  void onlineDelete(Link link) throws MessageException
  {
    if (isOnline())
    {
      sendDeleteLink(link);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Create and then verify a node on a remote NRS component only if we
   * are currently online. If we are not online then nothing is done.
   */
  void onlineCreate(Node n)
  {
    if (isOnline())
    {
      createNode(n, findRoute(n));
      //remoteVerify(n); // done by CreateNodeJob
    }
  }
  //----------------------------------------------------------------------
  /**
   * Delete the specified node.
   * Need to delete nodes children, and any links related to variables herein.
   */
  void onlineDelete(Node n)
  {
    if (isOnline())
    {
      remoteDelete(n);

      // _TODO_(Could be useful to do a post delete verify) To get
      // post-delete verification working, I will need to store a list
      // of nodes that have been recently deleted.
      // remoteVerify(n);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Propagate changes made to {@link Node} to the corresponding remote
   * note, if we are currently online. If we are not online then nothing
   * is done.
   */
  void onlineUpdate(Node n)
  {
    if (isOnline())
    {
      sendConfigMessage(n, findRoute(n));
    }
  }
  //----------------------------------------------------------------------
  /**
   * Rebuild the specified node on the remote NRS component which hosts
   * it. This process first deletes the specified node and then
   * recreates it, all on the NRS component to which it is associated.
   */
  void remoteRebuild(Node n)
  {
    // _TODO_(remote-delete the current node)

    createNode(n, findRoute(n));
  }
  //----------------------------------------------------------------------
  /**
   * Rebuild the links of the associated node, and its children
   *
   * @throws a {@link MessageException} if a message could not be
   * constructed
   */
  void remoteRebuildLinks(Node n) throws MessageException
  {
    // _TODO_(remote-delete the current node)

    findLinks(n);
    buildLinks(n);
  }
  //----------------------------------------------------------------------
  /**
   * Delete the specified node on the remote NSR component which hosts
   * it.
   */
  void remoteDelete(Node n)
  {
    String route = findRoute(n);
    int toVNID = 0;
    int vnid = n.remote().VNID();

    if (n.remote().isKnown() == false)
    {
      PackageLogger.log.warning("Can't send delete request for node '"
                                + n + "': don't have its VNID");
      return;
    }

    Message m = new DeleteNode(route, toVNID, vnid);

    // set optional intelligent message fields - using iTargetVNName
    // because toVNID is zero
    m.setNRSField(Constants.MessageFields.iReturnRoute, "");
    m.setNRSField(Constants.MessageFields.iTargetVNName,
               Constants.MessageTypes.DeleteNode);
    m.setNRSField(Constants.MessageFields.intelligent, "true");

    // If there is no route, then make the message broadcast
    if (route == null)
    {
      m.setNRSField(Constants.MessageFields.route, ""); // make field non-null
      m.setNRSField(Constants.MessageFields.iTargetCID,
                 n.getType().getSourceCID());
      m.setNRSField(Constants.MessageFields.iHopCount, HOP);
      m.setNRSField(Constants.MessageFields.iIsBroadcast, "true");
      m.setNRSField(Constants.MessageFields.intelligent, "true");
    }

    // send the message
    if (m_outPipeline != null) m_outPipeline.submit(m);
  }
  //----------------------------------------------------------------------
  /**
   * Create the node on the remote NRS component and recursively create
   * its child nodes.
   *
   * @param route the {@link String} containin the route to the remote
   * NRS component. Can be set to <tt>null</tt>, in which case a
   * broadcast message is constructed.
   */
  private void createNode(Node n, String route)
  {
    if (n.remoteCreated())
    {
      PackageLogger.log.warning("Not creating node " + n + ": remote node"
                                + " already exists (has VNID="
                                + n.remote().VNID() + ")");
    }
    else
    {
      int toVNID = 0;
      String vnType = n.getType().getNodeDescriptionName();
      int vnid = 0;
      String vnName = n.VNName();

      Message m = new CreateNode(route, toVNID, vnType, vnid, vnName);

      // set optional intelligent message fields - using iTargetVNName
      // because toVNID is zero
      m.setNRSField(Constants.MessageFields.iReturnRoute, "");
      m.setNRSField(Constants.MessageFields.iTargetVNName,
                 Constants.MessageTypes.CreateNode);
      m.setNRSField(Constants.MessageFields.intelligent, "true");

      // If there is no route, then make the message be a broadcast
      if (route == null)
      {
        addBroadcastFields(m, n.getType().getSourceCID());
      }
      else
      {
        m.setNRSField(Constants.MessageFields.route, route);
      }

      // Send the message
      if (m_outPipeline != null) m_outPipeline.submit(m);

      /* Next, build and send the configuration message */
      sendConfigMessage(n, route);
    }

    // No longer required. All children are created using CreateNodeJobs.
    // Create children nodes, recursively
//     for (Iterator i = n.getChildNodes().nodeIterator(); i.hasNext(); )
//     {
//       createNode((Node) i.next(), route);
//     }
  }
  //----------------------------------------------------------------------
  /** Build and send a configuration message */
  private void sendConfigMessage(Node n, String route)
  {
    Message configMsg = buildConfigMessage(n, route);
    if (m_outPipeline != null) m_outPipeline.submit(configMsg);
  }
  //----------------------------------------------------------------------
  /**
   * Adds fields to the message so that it can be routed as a broadcast
   * message. The "route" field is also added, but given an empy value.
   *
   * @deprecated use <tt>MessageTools</tt>
   */
  private void addBroadcastFields(Message m, String targetCID)
  {
    m.setNRSField(Constants.MessageFields.route, ""); // make field non-null
    m.setNRSField(Constants.MessageFields.iTargetCID, targetCID);
    m.setNRSField(Constants.MessageFields.iHopCount, HOP);
    m.setNRSField(Constants.MessageFields.iIsBroadcast, "true");
    m.setNRSField(Constants.MessageFields.intelligent, "true");
  }
  //----------------------------------------------------------------------
  /**
   * Add node-addressing fields. This attempts to add the common toVNID
   * field to a message, based on the contents of the supplied node (ie
   * we are assuming the message is being sent to the remote node that
   * corresponds to the node parameter that is supplied). If the remote
   * VNID information is not available for the supplied node, then the
   * appropriate intelligent message fields are added.
   *
   * @deprecated use <tt>MessageTools</tt>
   */
  private void addAddressFields(Message m, Node n)
  {
    int toVNID = 0;

    if ((n.remote() != null) && (n.remote().isKnown()))
    {
      toVNID = n.remote().VNID();
    }

    if (toVNID == 0)
    {
      m.setNRSField(Constants.MessageFields.toVNID, "0");
      m.setNRSField(Constants.MessageFields.iTargetVNName, n.VNName());
      m.setNRSField(Constants.MessageFields.intelligent, "true");
    }
    else
    {
      m.setNRSField(Constants.MessageFields.toVNID, Integer.toString(toVNID));
    }
  }
  //----------------------------------------------------------------------
  private Message buildConfigMessage(Node n, String route)
  {
    Message m = new Message(n.getType().getNodeDescriptionName());

    for (Iterator i = n.attributeIterator(); i.hasNext(); )
    {
      NodeAttribute na = (NodeAttribute) i.next();

      if (na.getCSL().getNoSend() == false)
      {
        String fieldName = na.getCSL().getAttributeName();
        String fieldValue = na.getValue().toString();

        if (na.getCSL().getInNRSNamespace() == true)
        {
          m.setNRSField(fieldName, fieldValue);
        }
        else
        {
          m.setField(fieldName, fieldValue);
        }
      }
    }

    addAddressFields(m, n);
    if (route == null)
    {
      addBroadcastFields(m, n.getType().getSourceCID());
    }
    else
    {
      m.setNRSField(Constants.MessageFields.route, route);
    }

    return m;
  }
  //----------------------------------------------------------------------
  private void sendDeleteLink(Link link) throws MessageException
  {
    // Create message
    DeleteLink m = new DeleteLink();

    // Determine routing information
    String route = link.getSource().getParent().getComponent().getRoute();

    // Determine addressing - use intelligent addressing
    MessageTools.addIntelligentAddressing(m, DeleteLink);
    int toVNID = 0;

    // Determine the cid and vnid of link source
    String cid = link.getSource().getParent().getComponent().getCID();
    if (!link.getSource().remote().isKnown())
    {
      throw new MessageException(m.getType(),
                                 Constants.MessageFields.vnid,
                                 "VNID not known for source variable");
    }
    int vnid = link.getSource().remote().VNID();

    // Determine the cid and vnid of link target
    String targetCID = link.getTarget().getParent().getComponent().getCID();
    if (!link.getTarget().remote().isKnown())
    {
      throw new MessageException(m.getType(),
                                 Constants.MessageFields.targetVNID,
                                 "VNID not known for target variable");
    }
    int targetVNID = link.getTarget().remote().VNID();

    // Other fields
    boolean sourceNotTarget = true;
    //String logPort = "";

    // Set all fields
    m.setFields(route,
                toVNID,
                sourceNotTarget,
                cid,
                vnid,
                targetCID,
                targetVNID);
    //logPort);

    // Fixes
    MessageTools.fixRoute(m,
         link.getSource().getParent().getComponent().getCID());

    // Send to source
    if (m_outPipeline != null) m_outPipeline.submit(m);

    /* Now send to the target  */
    m.clear();

    route = link.getTarget().getParent().getComponent().getRoute();

    /*  Intelligent VN-addressing */
    MessageTools.addIntelligentAddressing(m, DeleteLink);
    toVNID = 0;

    sourceNotTarget = false;

    m.setFields(route,
                toVNID,
                sourceNotTarget,
                cid,
                vnid,
                targetCID,
                targetVNID);//,
	//logPort);

    // Fixes

    MessageTools.fixRoute(m,
                        link.getTarget().getParent().getComponent().getCID());

    // Send to target
    if (m_outPipeline != null) m_outPipeline.submit(m);
   }
  //----------------------------------------------------------------------
  private void sendCreateLink(Link link) throws MessageException
  {
    // Create message
    CreateLink m = new CreateLink();

    // Determine routing information
    String route = link.getSource().getParent().getComponent().getRoute();

    // Determine addressing - use intelligent addressing
    MessageTools.addIntelligentAddressing(m, CreateLink);
    int toVNID = 0;

    // Determine the cid and vnid of link source
    String cid = link.getSource().getParent().getComponent().getCID();
    if (!link.getSource().remote().isKnown())
    {
      throw new MessageException(m.getType(),
                                 Constants.MessageFields.vnid,
                                 "VNID not known for source variable");
    }
    int vnid = link.getSource().remote().VNID();

    // Determine the cid and vnid of link target
    String targetCID = link.getTarget().getParent().getComponent().getCID();
    if (!link.getTarget().remote().isKnown())
    {
      throw new MessageException(m.getType(),
                                 Constants.MessageFields.vnid,
                                 "VNID not known for target variable");
    }
    int targetVNID = link.getTarget().remote().VNID();

    /* Other fields */

    boolean sourceNotTarget = true;
    //String logPort = "";
    boolean temporary = false;

    // Set all fields
    m.setFields(route,
                toVNID,
                sourceNotTarget,
                cid,
                vnid,
                targetCID,
                targetVNID,
                //logPort,
                temporary);

    // Fixes
    MessageTools.fixRoute(m,
      link.getSource().getParent().getComponent().getCID());

    // Send to source
    if (m_outPipeline != null) m_outPipeline.submit(m);

    /* Now send to the target  */
    m.clear();

    sourceNotTarget = false;
    route = link.getTarget().getParent().getComponent().getRoute();

    /*  Intelligent VN-addressing */
    MessageTools.addIntelligentAddressing(m, CreateLink);
    toVNID = 0;

    m.setFields(route,
                toVNID,
                sourceNotTarget,
                cid,
                vnid,
                targetCID,
                targetVNID,
                //logPort,
                temporary);

    // Fixes
    MessageTools.fixRoute(m,
      link.getTarget().getParent().getComponent().getCID());

    // Send to target
    if (m_outPipeline != null) m_outPipeline.submit(m);
  }
  //----------------------------------------------------------------------
  private void verify(NodeVariable v, String route)
  {
    /*
     * Message fields
     */

    String _route = route;
    int _toVNID = 0;        // we will provide a iTargetVNName field
    String _returnRoute = "";  // we will provide iReturnRoute
    int _returnToVNID = AppManager.getInstance().getComponent()
      .getVariable(ReplyVNID).getVNID();
    String _msgID = "-1";         // outbound pipeline will provide one
    String _vnName = v.VNName(); // name being queried

    Message m = new QueryVNID(_route, _toVNID, _returnRoute,
                              _returnToVNID, _msgID, _vnName);

    // set optional intelligent message fields - using iTargetVNName
    // because toVNID is zero
    m.setNRSField(Constants.MessageFields.iReturnRoute, "");
    m.setNRSField(Constants.MessageFields.iTargetVNName,
               Constants.MessageTypes.QueryVNID);
    m.setNRSField(Constants.MessageFields.intelligent, "true");

    // If there is no route, then make the message broadcast
    MessageTools.fixRoute(m, v.getParent().getType().getSourceCID());

    // send the message
    if (m_outPipeline != null) m_outPipeline.submit(m);
  }
  //----------------------------------------------------------------------
  private void verify(Node n, String route)
  {
    /*
     * Message fields
     */

    String _route = route;
    int _toVNID = 0;        // we will provide a iTargetVNName field
    String _returnRoute = "";  // we will provide iReturnRoute
    int _returnToVNID = AppManager.getInstance().getComponent()
      .getVariable(Constants.MessageTypes.ReplyVNID).getVNID();
    String _msgID = "-1";         // outbound pipeline will provide one
    String _vnName = n.VNName(); // name being queried

    Message m = new QueryVNID(_route, _toVNID, _returnRoute,
                              _returnToVNID, _msgID, _vnName);

    // set optional intelligent message fields - using iTargetVNName
    // because toVNID is zero
    m.setNRSField(Constants.MessageFields.iReturnRoute, "");
    m.setNRSField(Constants.MessageFields.iTargetVNName,
               Constants.MessageTypes.QueryVNID);
    m.setNRSField(Constants.MessageFields.intelligent, "true");

    // If there is no route, then make the message broadcast
    if (route == null)
    {
      m.setNRSField(Constants.MessageFields.route, ""); // make field non-null
      m.setNRSField(Constants.MessageFields.iTargetCID,
                 n.getType().getSourceCID());
      m.setNRSField(Constants.MessageFields.iHopCount, HOP);
      m.setNRSField(Constants.MessageFields.iIsBroadcast, "true");
      m.setNRSField(Constants.MessageFields.intelligent, "true");
    }

    // send the message
    if (m_outPipeline != null) m_outPipeline.submit(m);

    // Verify the variables belonging to this node
    for (Iterator<NodeVariable> i = n.getVariables().iterator(); i.hasNext(); )
    {
      verify(i.next(), route);
    }

    // Create children nodes, recursively
    for (Iterator i = n.getChildNodes().nodeIterator(); i.hasNext(); )
    {
      verify((Node) i.next(), route);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to get a route string for the NRS component associated with
   * the specified node.
   */
  private String findRoute(Node n)
  {
    NRSComponent c = AppManager.getInstance().
      getComponentManager().getComponent(n.getType().getSourceCID());

    return c.getRoute();
  }
  //----------------------------------------------------------------------
  /**
   * Set the {@link OutboundPipeline} this class will use when it needs
   * to submit {@link nrs.core.base.Message} objects for transmission to
   * other NRS components.
   */
  public void setOutboundPipeline(OutboundPipeline o)
  {
    m_outPipeline = o;
  }
  //----------------------------------------------------------------------
  /**
   * Find links to build the exists for the specified node and its children
   */
  private void findLinks(Node n)
  {
    m_linksTodos.clear();
    // Next, send out link creations messages for the links associated
    // with each node of the variable
    for (Iterator i = n.getVariables().variableIterator(); i.hasNext(); )
    {
      NodeVariable v = (NodeVariable) i.next();

      for (Iterator j = v.linkIterator();  j.hasNext(); )
      {
        Object link = j.next();
        if (!m_linksTodos.contains(link))
        {
          m_linksTodos.add(j.next());
        }
      }
    }
  }
  //----------------------------------------------------------------------
  private void buildLinks(Node n) throws MessageException
  {
    for (Iterator i = m_linksTodos.iterator(); i.hasNext(); )
    {
      sendCreateLink((Link) i.next());
    }

    m_linksTodos.clear();
  }
}
