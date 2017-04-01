package nrs.core.base;

import java.util.HashMap;
import nrs.core.message.Constants;
import nrs.core.message.QueryRoute;

/**
 * Manages the table of route information to components.
 *
 * <p>Client code can use this class to resolve a component's CID into a
 * route which can be used for addressing messages to that component.
 *
 * <p>Following construction of an instance, the following
 * post-construction configuration methods should be called to ensure
 * proper functioning :
 *
 * <ul>
 * <li type=square>{@link #setOutboundPipeline(OutboundPipeline)}</li>
 * <li type=square>{@link #configureVariables(Variable)}</li>
 * </ul>
 *
 * @author Darren Smith
 */
public class RouteManager extends MessageProcessor
{
  private HashMap m_table;
  private OutboundPipeline m_outboundPipeline;
  private Variable m_ReplyRoute;

  //----------------------------------------------------------------------
  /**
   * Creates the route manager.
   */
  public RouteManager()
  {
    m_table = new HashMap();
  }
  //----------------------------------------------------------------------
  /**
   * Specify the {@link Variable} objects needed by this class. These
   * variables are typically required during the construction of an
   * outbound query-message: such a message will require a
   * <tt>returnToVNID</tt> field, which can only be provided by knowning
   * which {@link Variable} should receive replies.
   *
   * @param vReplyRoute the {@link Variable} to which replies from
   * <tt>QueryRoute</tt> messages should be directed
   */
  public void configureVariables(Variable vReplyRoute)
  {
    m_ReplyRoute = vReplyRoute;
  }
  //----------------------------------------------------------------------
  /**
   * Set the outbound pipeline. This is the {@link MessageProcessor}
   * that will be used to handle remote-discovery messages issued by
   * this manager.
   */
  public void setOutboundPipeline(OutboundPipeline o)
  {
    m_outboundPipeline = o;
  }
  //----------------------------------------------------------------------
  /**
   * Get the outbound pipeline being used for message dispatch.
   */
  public OutboundPipeline getOutboundPipeline()
  {
    return m_outboundPipeline;
  }
  //----------------------------------------------------------------------
  /**
   * Attempts to resolve a route to the specified component. This is
   * done by examining the entries within the internal lookup table
   * which associates CID values to routes.
   *
   * <p>This manager might not contain a route for the specified
   * component (in which case <tt>null</tt> is returned). In the case
   * when a route is not available, the manager can optionally issue a
   * remote route-discovery message request to the specified component, in
   * order to discover a route.
   *
   * <p>If a remote message is made, then, at some later stage, the
   * reply of that request will update the internal information of this
   * table. So while an initial call to the method will likely return
   * <tt>null</tt>, later calls should return a useable route.
   *
   * @param CID identifies the component whose route is being requested
   *
   * @param remoteRequest set to <tt>true</tt> to cause a
   * route-discovery request to be made when a route for the specified
   * component is not known
   *
   * @return the route found or <tt>null</tt> if no route information is
   * available
   */
  public String resolveRoute(String CID, boolean remoteRequest)
  {
    if (m_table.containsKey(CID))
    {
      return (String) m_table.get(CID);
    }
    else if ( remoteRequest )
    {
      performLookup(CID);
    }

    return null;
  }
    //----------------------------------------------------------------------
  /**
   * Remove a route entry for the specified component. One consequence
   * is that later requests for a route to a component may trigger a
   * remote message-based route discovery.
   */
  public void removeRoute(String CID)
  {
    m_table.remove(CID);
  }
  //----------------------------------------------------------------------
  /**
   * Perform a remote message-discovery request in order to obtain a
   * route to the specified component.
   */
  private void performLookup(String CID)
  {
    PackageLogger.log.fine("Issuing a remote route-discovery request");

    QueryRoute m = new QueryRoute();

    int returnToVNID = 0;
    if (m_ReplyRoute == null)
    {
      PackageLogger.log.warning("No variable available for use in "
                                + "constructing message <" + m + ">: "
                                + "unable to set the "
                                + Constants.MessageFields.returnToVNID
                                + " field (check: has RouteManager"
                                + " object been properly configured?)");
    }
    else
    {
      returnToVNID = m_ReplyRoute.getVNID();
    }

    m.setFields("",    // route
                0,     // toVNID
                "",    // returnRoute
                returnToVNID,
                "-1",  // msgID
                ""     // forward route
                );

    MessageTools.addBroadcastFields(m, CID, 10);
    m.setNRSField(Constants.MessageFields.iTargetVNName,
               Constants.MessageTypes.QueryRoute);
    m.setNRSField(Constants.MessageFields.iReturnRoute, "");
    m.setNRSField(Constants.MessageFields.iForwardRoute, "");

    m_outboundPipeline.deliver(m, null);
  }
  //----------------------------------------------------------------------
  /**
   * Handle a <tt>ReplyRoute</tt> message. The contents of the message
   * are used to update the table of routes to components. Messages are
   * passed onto the next component.
   */
  public void deliver(Message m, MessageProcessor sender)
  {
    if (m.getType().equals(Constants.MessageTypes.ReplyRoute))
    {
      Message request = m.aux().getOrigMessageRequest();

      if (request != null)
      {
        // Note: notice this idiom. It's more elegant than a series of
        // if-then checks for testing the presence of required
        // fields. Also it avoids code duplication (i.e. the strings in
        // the error message only appear once).
        try
        {
          request.checkNRSField(Constants.MessageFields.iTargetCID);
          m.checkField(Constants.MessageFields.forwardRoute);

          String CID = request.getNRSField(Constants.MessageFields.iTargetCID);
          String route =  m.getField(Constants.MessageFields.forwardRoute);

          m_table.put(CID, route);
          PackageLogger.log.fine("Route table updated: CID=" + CID + ", route="
                                 + route);
        }
        catch (FieldNotFoundException e)
        {
          PackageLogger.log.warning("Message <" + m + "> received at " +
                                    "RouteManager ignored: " + e);
          PackageLogger.log.warning("Request:" + request.diagString());
          PackageLogger.log.warning("Reply:" + m.diagString());

        }
      }
      else
      {
        PackageLogger.log.warning("Message <" + m + "> received at " +
                                  "RouteManager ignored: no original " +
                                  "request found");
      }
    }

    next(m);
  }

}
