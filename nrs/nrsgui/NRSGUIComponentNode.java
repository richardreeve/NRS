package nrs.nrsgui;

import java.util.Collection;
import java.util.Iterator;
import nrs.core.base.BaseComponent;
import nrs.core.base.ComponentInfo;
import nrs.core.base.Message;
import nrs.core.base.OutboundPipeline;
import nrs.core.base.RouteManager;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;
import nrs.core.message.Constants;
import nrs.core.message.ReplyVNType;

/**
 * This extends {@link BaseComponent} to provide the code which
 * represents the GUI as an NRS component.
 *
 * @author Darren Smith
 */
public class NRSGUIComponentNode extends BaseComponent
{
  //  private NRSComponent m_me; // _TODO_(Get rid of this line)
  //  private VariableManager m_vMan;
  private NRSComponentManager m_cMan;

  //----------------------------------------------------------------------
  /**
   * Constructor
   */
  public NRSGUIComponentNode(NRSComponentManager cMan,
                             VariableManager vMan,
                             RouteManager rMan)
  {
    super(vMan,
          "NRS.gui",
          "NRS.gui",
          false,
          true,
          false,
          "1.0",
          AppManager.getInstance().getCIDManager().obtain());

    // Note: this is a hack. The CID for this component is fixed to
    // "1". I can't make a call to the obtainCID() method, since that
    // method belongs to the PortManager class which does not yet
    // exist. The solution is to make a CID manager. That will be a very

    //    m_vMan = vMan;
    m_cMan = cMan;

    //    m_me = new NRSComponent("NRS.gui", "0", "", null, true, false, "1.0");
  }
  //----------------------------------------------------------------------
//   /**
//    * Return the {@link NRSComponent} which corresponds to this
//    * component.
//    */
//   public NRSComponent getNRSComponent()
//   {
//     return m_me;
//   }
  //----------------------------------------------------------------------
  /**
   * Pass the message onto an internal {@link NRSComponentManager} instance
   */
  public void handleMessageAt_ReplyCID(Message m,
                                       Variable v,
                                       boolean diff)
  {
    m_cMan.handleReplyCID(m);
  }
  //----------------------------------------------------------------------
  /**
   * Pass the message onto an internal {@link NRSComponentManager} instance
   */
  public void handleMessageAt_ReplyCType(Message m,
                                         Variable v,
                                         boolean diff)
  {
    m_cMan.handleReplyCType(m);
  }
  //----------------------------------------------------------------------
  /**
   * Pass the message onto an internal {@link NRSComponentManager} instance
   */
  public void handleMessageAt_QueryCID(Message m,
                                       Variable v,
                                       boolean diff)
  {
    m_cMan.handleQueryCID(m);
  }
  //----------------------------------------------------------------------
  public void handleMessageAt_ReplyCSL(Message m,
                                       Variable v,
                                       boolean diff)
  {
    m_cMan.handleReplyCSL(m);
  }
  //----------------------------------------------------------------------
  public void handleMessageAt_ReplyVNID(Message m,
                                        Variable v,
                                        boolean diff)
  {
    AppManager.getInstance().getNodeManager().deliver(m, null);
  }
   //----------------------------------------------------------------------
//   public void handleMessageAt_QueryVNID(Message m, Variable v, boolean diff)
//   {
//     if (diff == true)
//     {
//       PackageLogger.log.warning("Variable " + v + " does not support messages "
//                                 + " of type " + m + "; message ignored");
//       return;
//     }

//     /**
//        _TODO_(How do I handle QueryVNIDs interrogating Nodes?)

//     */

//     // This is a standard query; pass to VariableManager, which knows
//     // how to handle it.
//     m_vMan.replyToQueryVNID(m);
//   }
  //----------------------------------------------------------------------
  /**
   *
   */
 //  public void handleMessageAt_QueryVNType(Message m, Variable v, boolean diff)
//   {
//     if (diff == true)
//     {
//       PackageLogger.log.warning("Variable " + v + " does not support messages "
//                                 + " of type " + m + "; message ignored");
//       return;
//     }

//     // _TODO_(Put this route searching code in the Message class)
//     String route = m.getNRSField(Constants.MessageFields.iReturnRoute);
//     if (route == null)
//     {
//        route = m.getField(Constants.MessageFields.returnRoute);
//     }
//     if (route == null)
//     {
//       PackageLogger.log.warning("No return route in " + m + " query:"
//                                 + " unable to reply");
//       return;
//     }

//     // _TODO_(Put something generic in the Message class)
//     int toVNID = -1;
//     if (m.hasField(Constants.MessageFields.returnToVNID))
//     {
//       try
//       {
//         toVNID =
//           Integer.parseInt(m.getField(Constants.MessageFields.returnToVNID));
//       }
//       catch (NumberFormatException e)
//       {
//         PackageLogger.log.warning(Constants.MessageFields.toVNID + " field in"
//                                   + " " + m + " is not numeric: can't parse"
//                                   + "; unable to reply");
//       }
//     }
//     if (toVNID == -1) return;


//     // _TODO_(Put something generic in the Message class)
//     String replyMsgID = m.getField(Constants.MessageFields.msgID);
//     if (replyMsgID == null) replyMsgID = "";

//     // _TODO_(Put something generic in the Message class)
//     int vnid = -1;
//     if (m.hasField(Constants.MessageFields.vnid))
//     {
//       try
//       {
//         vnid =
//           Integer.parseInt(m.getField(Constants.MessageFields.vnid));
//       }
//       catch (NumberFormatException e)
//       {
//         PackageLogger.log.warning(Constants.MessageFields.vnid + " field in"
//                                   + " " + m + " is not numeric: can't parse"
//                                   + "; unable to reply");
//       }
//     }
//     if (vnid == -1) return;

//     Variable vFound = m_vMan.get(vnid);
//     String vnType = null;
//     if (vFound == null)
//     {
//       vnType = "";
//     }
//     else
//     {
//       vnType = vFound.getVNName();
//     }

//     ReplyVNType r = new ReplyVNType();
//     r.setFields(route, toVNID, replyMsgID, vnType);

//     if (getOutboundDest() != null) getOutboundDest().deliver(r, this);
//   }
  //----------------------------------------------------------------------
  /**
   * Pass the message onto an internal {@link NRSComponentManager} instance
   */
  public void handleMessageAt_ReplyMaxPort(Message m,
                                           Variable v,
                                           boolean diff)
  {
    m_cMan.handleReplyMaxPort(m);
  }
  //----------------------------------------------------------------------
  /**
   * Pass the message onto an internal {@link NRSComponentManager} instance
   */
  public void handleMessageAt_ReplyPort(Message m,
                                        Variable v,
                                        boolean diff)
  {
    m_cMan.handleReplyPort(m);
  }
}
