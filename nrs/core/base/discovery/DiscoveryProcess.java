package nrs.core.base.discovery;

import java.util.ArrayList;
import nrs.core.base.CIDManager;
import nrs.core.base.Message;
import nrs.core.base.MessageTools;
import nrs.core.base.OutboundPipeline;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;
import nrs.core.comms.CommsRoute;
import nrs.core.comms.PortManager;
import nrs.core.message.QueryCID;

import static nrs.core.base.discovery.DiscoveredRoute.State.*;
import static nrs.core.message.Constants.MessageTypes.*;

/**
 * Manage the process of discovering routes and components of a
 * connected NRS system.
 *
 * @author Darren Smith
 */
public class DiscoveryProcess
{
  private ArrayList<DiscoveredRoute> m_routes;
  private boolean m_continueLoop;
  private boolean m_runDiscovery = false;
  private VariableManager m_vm;
  private CIDManager m_cm;
  private Variable m_replyCID;
  private OutboundPipeline m_outPipeline;

  static private int m_instanceCount = 0;

  //----------------------------------------------------------------------
  /**
   * Creates a new DiscoveryProcess object.
   *
   * @param pm the PortManager which will provide the inital routes to check
   *
   * @param vm the VariableManager to which new variables created by
   * this object will be registered
   *
   * @param cm the CIDManager which is needed for obtaining CID values
   * for the QueryCID messages this components sends out
   *
   * @param outPipeline the OutboundPipeline used for submitting messages
   */
  public DiscoveryProcess(PortManager pm,
                          VariableManager vm,
                          CIDManager cm,
                          OutboundPipeline outPipeline)
  {
    m_instanceCount++;
    m_vm = vm;
    m_cm = cm;
    m_outPipeline = outPipeline;

    // The first DiscoveredRoute objects we create are those leading
    // directly from this component (via the provided port manager)
    int maxPortID = pm.getMaxPortID();
    PackageLogger.log.severe("!!! maxPortID=" + maxPortID);

    m_routes = new ArrayList<DiscoveredRoute>();
    for (int i = 0; i <= maxPortID; i++)
    {
      CommsRoute route = pm.getPort(i);
      if (route != null)
      {

        PackageLogger.log.severe("!!! Have local route" + route);
        m_routes.add(new DiscoveredRoute(route.getID().getEncoding()));
      }
    }

    // Build local variables needed
    m_replyCID = new Variable(m_vm,
                              "DiscoveryProcess_" + ReplyCID + "_"
                              + Integer.toString(m_instanceCount),
                              false,
                              false,
                              ReplyCID)
      {
        public void deliver(Message m)
        {
          handleReplyCID(m, this, false);
        }
      };

  }
  //----------------------------------------------------------------------
  public void handleReplyCID(Message m, Variable v, boolean diff)
  {
  }
  //----------------------------------------------------------------------
  /**
   * Trigger the start of component discovery.
   */
  public void startDiscovery()
  {
    m_runDiscovery = true;
    loopOverDRs();
  }
  //----------------------------------------------------------------------
  /**
   * Terminate component discovery.
   */
  public void stopDiscovery()
  {
    m_runDiscovery = false;
  }
  //----------------------------------------------------------------------
  /**
   * This is the main state-to-action loop
   */
  private void loopOverDRs()
  {
    while (m_runDiscovery)
    {
      for (DiscoveredRoute route : m_routes)
      {
        switch(route.getState())
        {
          case QCID_NOT_SENT: sendQCID(route); break;
          //        case QCID_SENT: doSentQCID(route); break;

          // send QCID
        }
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Send a QueryCID message to the specified route, and set state to
   */
  private void sendQCID(DiscoveredRoute dr)
  {
    QueryCID m = new QueryCID();

    String route = dr.getRoute();
    int toVNID = 0;
    String returnRoute = "";
    int returnToVNID = m_replyCID.getVNID();
    String msgID = "-1";
    String cid = "";

    MessageTools.addIntelligentAddressing(m);
    MessageTools.addIntelligentReturnRoute(m);

    if (m_cm.isAvailable(route))
    {
      cid = route;
    }
    else
    {
      cid = m_cm.obtain();

      PackageLogger.log.warning("Unable to use route " + route +
                                " for suggested"
                                + " CID. It seems to be already in use. Using "
                                + cid + " instead");
    }

    m.setFields(route,
                toVNID,
                returnRoute,
                returnToVNID,
                msgID,
                cid);

    m_outPipeline.submit(m);

    dr.setState(AWAITING_RCID);
  }
  //----------------------------------------------------------------------
  /**
   *
   */
  void start()
  {
    m_continueLoop = true;
  }
  //----------------------------------------------------------------------
  /**
   *
   */
  void stop()
  {
    m_continueLoop = false;
  }
}
