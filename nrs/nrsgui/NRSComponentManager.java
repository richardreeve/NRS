package nrs.nrsgui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import nrs.core.base.Message;
import nrs.core.base.MessageTools;
import nrs.core.base.OutboundPipeline;
import nrs.core.base.BaseComponent;
import nrs.core.message.Constants;
import nrs.core.message.QueryCID;
import nrs.core.message.QueryCSL;
import nrs.core.message.QueryCType;
import nrs.core.message.QueryConnectedCIDs;
import nrs.core.message.QueryMaxPort;
import nrs.core.message.QueryPort;
import nrs.core.message.QueryVNType;
import nrs.core.message.QueryVNType;
import nrs.core.message.ReplyCID;

import static nrs.core.message.Constants.MessageTypes.QueryMaxPort;
import static nrs.core.message.Constants.MessageTypes.QueryPort;
import static nrs.core.message.Constants.MessageTypes.ReplyCID;
import static nrs.core.message.Constants.MessageTypes.ReplyMaxPort;
import static nrs.core.message.Constants.MessageTypes.ReplyPort;

/**
 * Manages the collection of {@link NRSComponent} instances which
 * represent all of the discovered NRS components connected to this
 * application.
 *
 * @author Darren Smith
 */
public class NRSComponentManager
{
  /** Components stored by their CID. Each type of value in this hash
   * map is of type {@link NRSComponent} */
  private HashMap m_byCID;

  /** Pipeline to use for sending messages */
  private OutboundPipeline m_outPipeline;

  /** Needed for access VNIDs */
  private BaseComponent m_defNode;

  /** Location of the CSL repository */
  private String m_cslRepository;

  //----------------------------------------------------------------------
  /**
   * Constructor
   */
  public NRSComponentManager(String cslRepository)
  {
    m_cslRepository = cslRepository;
    m_byCID = new HashMap();
  }
  //----------------------------------------------------------------------
  /**
   * Return the components which match the specified type and version
   *
   * @return a {@link List} of {@link NRSComponent} instances which
   * match the specified name and version
   */
  List getComponents(String cType, String cVersion)
  {
    List l = new ArrayList();

    for (Iterator i = m_byCID.values().iterator(); i.hasNext(); )
    {
      NRSComponent c = (NRSComponent) i.next();

      if (c.getCType().equals(cType) && c.getCVersion().equals(cVersion))
      {
        l.add(c);
      }
    }

    return l;
  }
  //----------------------------------------------------------------------
  /**
   * Provide the {@link BaseComponent} this class should use when
   * is needs access to NRS-style variables.
   */
  public void setComponentNode(BaseComponent node)
  {
    m_defNode = node;
    //    m_byCID.put(node.info().getCID(), node.info());
  }
  //----------------------------------------------------------------------
  /**
   * Set the {@link OutboundPipeline} this class will use when it needs
   * to submit {@link Message} objects for transmission to other NRS
   * components.
   */
  public void setOutboundPipeline(OutboundPipeline o)
  {
    m_outPipeline = o;
  }
  //----------------------------------------------------------------------
  /**
   * Handle a ReplyCType {@link Message} which might signify the
   * existence of a new NRS component attached to this
   * application. Handling these really requires access to the message
   * that was sent out.
   */
  public void handleReplyCType(Message m)
  {
    if (m.aux().getOrigMessageRequest() == null)
    {
      PackageLogger.log.warning("Failure to match received " + m
                                + " to any previous message sent;"
                                + m + " message ignored");
      return;
    }

    /* Now we attempt to match this ReplyCType with an NRS component we
       know about - to do that we have to find a CID */
    String cid = null;

    if (m.aux().getOrigMessageRequest() != null)
    {
      cid = m.aux().getOrigMessageRequest().aux().getTargetCID();
    }

    if ((cid == null) && (m.hasNRSField(Constants.MessageFields.iReturnRoute)))
    {
      String knownRoute = m.getNRSField(Constants.MessageFields.iReturnRoute);

      // Try to find an existing component which matches the return
      // route we have available
      for (Iterator i = m_byCID.values().iterator(); i.hasNext(); )
      {
        NRSComponent c = (NRSComponent) i.next();

        if (c.getRoute().equals(knownRoute))
        {
          cid = c.getCID();
          break;
        }
      }
    }

    if (cid == null)
    {
      PackageLogger.log.warning("Failed to match " + m
                                + " with a known component (couldn't"
                                + " discover CID); "
                                + m + " message ignored");
      return;
    }

    if (m_byCID.containsKey(cid) == false)
    {
      PackageLogger.log.warning("Couldn't match CID=" + cid
                                + " to any known component; "
                                + m + " message ignored");
      return;
    }

    NRSComponent c = (NRSComponent) m_byCID.get(cid);

    c.setCType(m.getField(Constants.MessageFields.cType));
    c.setCVersion(m.getField(Constants.MessageFields.cVersion));

    PackageLogger.log.fine("Type update for component: " + c);
  }
  //----------------------------------------------------------------------
  /**
   * Handle a ReplyCID {@link Message} which might signify the existence
   * of a new NRS component attached to this application.
   */
  public void handleReplyCID(Message msg)
  {
    String cidStr = msg.getField(Constants.MessageFields.cid);

    if (m_byCID.containsKey(cidStr)
        || m_defNode.info().getCID().equals(cidStr))
    {
      PackageLogger.log.info("An NRS component with CID="
                                + cidStr
                                + " already exists; ignoring this " + msg);
      return;
    }

    // register the new NRS-component
    NRSComponent comp = new NRSComponent(msg);
    m_byCID.put(cidStr, comp);
    PackageLogger.log.fine("New component discovered (CID=" + cidStr + ")");

    // Attempt set a forward route for the received component
    setForwardRoute(comp, msg);

    queryComponentType(comp);
    //queryConnectedCIDs(comp);
    queryMaxConnections(comp);
    queryCSL(comp);
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to discover a forward route for the specified component
   * from the information contained in the {@link Message} which should
   * be a ReplyCID message.
   */
  private void setForwardRoute(NRSComponent comp, Message msg)
  {
    String route = null;

    // Does the message contain a return route field?
    if (msg.hasNRSField(Constants.MessageFields.iReturnRoute))
    {
      route = msg.getNRSField(Constants.MessageFields.iReturnRoute);
      PackageLogger.log.fine("Setting route for '"
                             + comp
                             + "' from "
                             + Constants.MessageFields.iReturnRoute
                             + " field"
                             + " : " + route);
      comp.setRoute(route);
      return;
    }

    // Was the message in response to a QueryCID and is the forward
    // route for the latter available?
    Message origRequest = msg.aux().getOrigMessageRequest();
    if ((origRequest != null) &&
        (origRequest.getType() == Constants.MessageTypes.QueryCID) &&
        (origRequest.aux().getRoute() != null))
    {
      route = origRequest.aux().getRoute();

      PackageLogger.log.fine("Setting route for '"
                             + comp
                             + "' from "
                             + Constants.MessageFields.route
                             + " of orignal request"
                             + " : " + route);
      comp.setRoute(route);
      return;
    }

    // Was the message in response to a QueryConnectCID?
    Message om = msg.aux().getOrigMessageRequest();
    if ((om != null) &&
        (om.getType() == Constants.MessageTypes.QueryConnectedCIDs) &&
        (om.aux().getRoute() != null)
        && (msg.hasField(Constants.MessageFields.port)))
    {
      route = om.aux().getRoute()
        + msg.getField(Constants.MessageFields.port);
      PackageLogger.log.fine("Setting route for '"
                             + comp
                             + "' from "
                             + Constants.MessageFields.route
                             + " of orignal request and "
                             + Constants.MessageFields.port
                             + " of reply"
                             + " : " + route);
      comp.setRoute(route);
      return;
    }

    PackageLogger.log.warning("Unable to extract forward-route for "
                              + "component '" + comp + "' from received "
                              + msg + " message");
  }
  //----------------------------------------------------------------------
  /**
   * Query the CSL of a component. All components should be capable of a
   * CSL response. If a component has no CSL to offer it should still
   * issue a CSL response but with an empty CSL content.
   */
  private void queryCSL(NRSComponent c)
  {
    if (m_outPipeline == null)
    {
      PackageLogger.log.severe("No outbound pipeline present for"
                               + " submitting messages");
      return;
    }

    if (c.getRoute() == null)
    {
      PackageLogger.log.severe("No route for component " + c
                               + "; unable to query its CSL");
      return;
    }

    QueryCSL q = new QueryCSL();

    q.setFields(
        c.getRoute(), /* route */
        0,                 /* VNID not known */
        "",                 /* don't have a return route */
        m_defNode.getVariable(Constants.MessageTypes.ReplyCSL).getVNID(),
        "-1" /* will get a message ID later */);

    // set optional intelligent message fields
    q.setNRSField(Constants.MessageFields.iReturnRoute, "");
    q.setNRSField(Constants.MessageFields.iTargetVNName,
               Constants.MessageTypes.QueryCSL);
    q.setNRSField(Constants.MessageFields.intelligent, "true");

    // set auxillary data - useful for processing reply
    q.aux().setTargetCID(c.getCID());

    m_outPipeline.submit(q);
  }
  //----------------------------------------------------------------------
  /**
   * Query the type of a component
   */
  private void queryComponentType(NRSComponent c)
  {
    if (m_outPipeline == null)
    {
      PackageLogger.log.severe("No outbound pipeline present for"
                               + " submitting messages");
      return;
    }

    if (c.getRoute() == null)
    {
      PackageLogger.log.severe("No route for component " + c
                               + "; unable to query its type");
      return;
    }

    QueryCType queryMsg = new QueryCType();

    queryMsg.setFields(
        c.getRoute(), /* route */
        0,                 /* VNID not known */
        "",                 /* don't have a return route */
        m_defNode.getVariable(Constants.MessageTypes.ReplyCType).getVNID(),
        "-1" /* will get ID later */ );

    // set optional intelligent message fields
    queryMsg.setNRSField(Constants.MessageFields.intelligent, "true");
    queryMsg.setNRSField(Constants.MessageFields.iForwardRoute, "");
    queryMsg.setNRSField(Constants.MessageFields.iReturnRoute, "");
    queryMsg.setNRSField(Constants.MessageFields.iTargetVNName,
                      Constants.MessageTypes.QueryCType);

    // set auxillary data - useful for processing reply
    queryMsg.aux().setTargetCID(c.getCID());   // target comp.

    m_outPipeline.submit(queryMsg);
  }
  //----------------------------------------------------------------------
  /**
   * Query the port of a component. The message sent will request
   * that replies are sent to the ReplyMaxPort variable.
   */
  private void queryMaxConnections(NRSComponent c)
  {
    if (m_outPipeline == null)
    {
      PackageLogger.log.severe("No outbound pipeline present for"
                               + " submitting messages");
      return;
    }

    // Create message
    QueryMaxPort m = new QueryMaxPort();

    // Local variables, corresponding to message fields
    String route = c.getRoute();
    if (route == null)
    {
      PackageLogger.log.severe("No route for component " + c
                               + "; unable to query its connections");
      return;
    }

    int toVNID = 0;
    MessageTools.addIntelligentAddressing(m, m.getType());

    String returnRoute = ""; // don't have a return route
    MessageTools.addIntelligentReturnRoute(m);

    int returnToVNID = m_defNode.getVariable(ReplyMaxPort).getVNID();

    String msgID = "-1"; // will get ID later

    // assign fields
    m.setFields(route, toVNID, returnRoute, returnToVNID, msgID);

    // submit
    m_outPipeline.submit(m);
  }
  //----------------------------------------------------------------------
  /**
   * Query the connections of a component. The message sent will request
   * that replies are sent to the ReplyCID variable.
   *
   * @deprecated No longer in NRS protocol.
   */
  private void queryConnectedCIDs(NRSComponent c)
  {
    if (m_outPipeline == null)
    {
      PackageLogger.log.severe("No outbound pipeline present for"
                               + " submitting messages");
      return;
    }

    // Query the type of the new component - if we have a route
    if (c.getRoute() == null)
    {
      PackageLogger.log.severe("No route for component " + c
                               + "; unable to query its connections");
      return;
    }

    QueryConnectedCIDs q = new QueryConnectedCIDs();

    q.setFields(
        c.getRoute(), /* route */
        0,                 /* VNID not known */
        "",                 /* don't have a return route */
        m_defNode.getVariable(Constants.MessageTypes.ReplyCID).getVNID(),
        "-1", /* will get ID later */
        c.getCID());

    // set optional intelligent message fields
    q.setNRSField(Constants.MessageFields.intelligent, "true");
    q.setNRSField(Constants.MessageFields.iForwardRoute,"");
    q.setNRSField(Constants.MessageFields.iReturnRoute, "");
    q.setNRSField(Constants.MessageFields.iTargetVNName,
               Constants.MessageTypes.QueryConnectedCIDs);
    q.setNRSField(Constants.MessageFields.iSourceCID,
               m_defNode.info().getCID());

    m_outPipeline.submit(q);
  }
  //----------------------------------------------------------------------
  /**
   * Handle a QueryCID {@link Message} by responding with a ReplyCID.
   */
  public void handleQueryCID(Message m)
  {
    if (m_outPipeline == null)
    {
      PackageLogger.log.severe("No outbound pipeline present for"
                               + " submitting messages");
      return;
    }

    ReplyCID r = new ReplyCID();

    String route = m.getNRSField(Constants.MessageFields.iReturnRoute);
    if (route == null)
    {
       route = m.getField(Constants.MessageFields.returnRoute);
    }
    if (route == null)
    {
      PackageLogger.log.warning("No return route in " + m + " query:"
                                + " unable to reply");
      return;
    }

    int toVNID;
    String toVNName;

    if (m.hasField(Constants.MessageFields.returnToVNID))
    {
      try
      {
        toVNID =
          Integer.parseInt(m.getField(Constants.MessageFields.returnToVNID));
        toVNName = null;
      }
      catch (NumberFormatException e)
      {
        PackageLogger.log.warning(Constants.MessageFields.toVNID + " field in"
                                  + " " + m + " is not numeric: can't parse"
                                  + "; unable to reply");
        return;
      }
    }
    else
    {
      toVNID = 0;  /* zero is reserved; means null */
      toVNName = m.getField(Constants.MessageTypes.ReplyCID);
    }

    // the source message should provide a msg ID: otherwise we can't
    // send back a replyMsgID
    String replyMsgID = m.getField(Constants.MessageFields.msgID);
    if (replyMsgID == null) replyMsgID = "";

    String port = m.getField(Constants.MessageFields.port);
    if (port == null) port = "";

    r.setFields(route,
                toVNID,
                m.getField(Constants.MessageFields.msgID),
                m_defNode.info().getCID());

    if (toVNName != null)
    {
      r.setNRSField(Constants.MessageFields.iTargetVNName,
                 toVNName);
      r.setNRSField(Constants.MessageFields.intelligent, "true");
    }

    m_outPipeline.submit(r);

    //    dummyMessageSender(null);
  }
  //----------------------------------------------------------------------
  /**
   * Handle a ReplyCSL {@link Message} which is the specification of the
   * processing features offered by a component.
   */
  public void handleReplyCSL(Message m)
  {
    if (m.aux().getOrigMessageRequest() == null)
    {
      PackageLogger.log.warning("Failure to match received " + m
                                + " to any previous message sent;"
                                + m + " message ignored");
      return;
    }

    /* Now we attempt to match this ReplyCSL with an NRS component we
       know about - to do that we have to find a CID */
    String cid = null;

    if (m.aux().getOrigMessageRequest() != null)
    {
      cid = m.aux().getOrigMessageRequest().aux().getTargetCID();
    }

    if ((cid == null) && (m.hasNRSField(Constants.MessageFields.iReturnRoute)))
    {
      String knownRoute = m.getNRSField(Constants.MessageFields.iReturnRoute);

      // Try to find an existing component which matches the return
      // route we have available
      for (Iterator i = m_byCID.values().iterator(); i.hasNext(); )
      {
        NRSComponent c = (NRSComponent) i.next();

        if (c.getRoute().equals(knownRoute))
        {
          cid = c.getCID();
          break;
        }
      }
    }

    if (cid == null)
    {
      PackageLogger.log.warning("Failed to match " + m
                                + " with a known component (couldn't"
                                + " discover CID); "
                                + m + " message ignored");
      return;
    }

    if (m_byCID.containsKey(cid) == false)
    {
      PackageLogger.log.warning("Couldn't match CID=" + cid
                                + " to any known component; "
                                + m + " message ignored");
      return;
    }

    NRSComponent c = (NRSComponent) m_byCID.get(cid);

    c.acquireCSL(m_cslRepository, m);
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link NRSComponent} associated with a specified CID, or
   * <tt>null</tt> if there is no match.
   */
  public NRSComponent getComponent(String cid)
  {
    return (NRSComponent) m_byCID.get(cid);
  }
  //----------------------------------------------------------------------
  /**
   * Return a {@link Collection} of the {@link NRSComponent} objects
   * belonging to this manager.
   */
  public Collection getComponents()
  {
    return m_byCID.values();
  }
  //----------------------------------------------------------------------
  /**
   * Handle a ReplyMaxPort {@link Message} which is the trigger to begin
   * querying the ports.
   */
  void handleReplyMaxPort(Message msg)
  {
    if (!msg.hasField(Constants.MessageFields.port))
    {
      PackageLogger.log.info(msg.toString() + " message has no "
                             + Constants.MessageFields.port
                             + " field; ignoring");
    }

    int port
      = Integer.parseInt(msg.getField(Constants.MessageFields.port));

    while (port > 0)
    {
      QueryPort m = new QueryPort();

      // Find a route - preferred is to look in the reply
      String route =
        MessageTools.findReturnRoute(msg);

      // If none found use the route used for the previous request
      if (route == null)
        route = msg.aux().getOrigMessageRequest().aux().getRoute();

      int toVNID = 0;
      MessageTools.addIntelligentAddressing(m, m.getType());

      String returnRoute = "";
      MessageTools.addIntelligentReturnRoute(m);

      m.setNRSField(Constants.MessageFields.iForwardRoute, "");

      int returnToVNID = m_defNode.getVariable(ReplyPort).getVNID();

      String msgID = "-1";

      // set fields
      m.setFields(route,
                  toVNID,
                  returnRoute,
                  returnToVNID,
                  msgID,
                  port);

      // submit
      m_outPipeline.submit(m);

      port--;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Handle a ReplyPort {@link Message} which is the trigger to begin
   * querying about other components.
   */
  void handleReplyPort(Message msg)
  {

    QueryCID m = new QueryCID();

    // Find a route - preferred is to look in the reply
    String route = MessageTools.findReturnRoute(msg);

    // If none found use the route used for the previous request
    if (route == null)
    route = msg.aux().getOrigMessageRequest().aux().getRoute();

    if (route == null)
    {
      PackageLogger.log.warning("Can't respond to a ReplyPort message with"
                                + " a QueryCID: unable to find a forward"
                                + " route");
      return;
    }
    route = route + msg.getField(Constants.MessageFields.portRoute);
    PackageLogger.log.info("Issuing a QueryCID in response to ReplyPort"
                           + "; full route is " + route);

    int toVNID = 0;
    MessageTools.addIntelligentAddressing(m);

    String returnRoute = "";
    MessageTools.addIntelligentReturnRoute(m);

    int returnToVNID = m_defNode.getVariable(ReplyCID).getVNID();

    String msgID = "-1";
    int port = 0;

    String cid;
    if (AppManager.getInstance().getCIDManager().isAvailable(route))
    {
      cid = route;
    }
    else
    {
      cid = AppManager.getInstance().getCIDManager().obtain();

      PackageLogger.log.warning("Unable to use route " + route +
                                " for suggested"
                                + " CID. It seems to be already in use. Using "
                                + cid + " instead");
    }

    // set fields
    m.setFields(route,
                toVNID,
                returnRoute,
                returnToVNID,
                msgID,
                cid);

    // submit
    m_outPipeline.submit(m);

  }
}
