package nrs.nrsgui;

import nrs.util.job.Job;
import nrs.core.base.MessageException;

/**
 * A job for creating a local and remote link between two NRS variables
 */
class CreateLinkJob extends nrs.util.job.Job
{
  private NodeVariable m_source;
  private NodeVariable m_target;
  private NodeManager nMan = AppManager.getInstance().getNodeManager();

  private final String m_sVNName, m_tVNName;
  private final Programmer m_prog;
  private final Link m_link;

  public static final String PHASE_GET_VNIDS = "Obtaining VNIDs";
  public static final String PHASE_WAIT_VNIDS = "Waiting for VNID";
  public static final String PHASE_SEND_CL_MSG = "Sending CreateLink";

  //----------------------------------------------------------------------
  /**
   * Constructs an instance for a job to create a remote link between
   * the named variables.
   *
   * <p>During the various steps of the job, a check is made to ensure
   * the source and target variables of the <tt>link</tt> still exist
   * (this is done by VN-name lookup into the {@link NodeManager}). If
   * at any stage they are not found, this job aborts.
   */
  CreateLinkJob(Programmer prog, Link link)
  {
    m_prog = prog;
    m_link = link;

    m_sVNName = link.getSource().VNName();
    m_tVNName = link.getTarget().VNName();

    setPhase(PHASE_GET_VNIDS);
  }

  //----------------------------------------------------------------------
  /**
   * This called by the <tt>JobManager</tt> when it is this job's turn
   * to run
   */
  public void run()
  {
    if (getPhase() == PHASE_GET_VNIDS) do_Phase_Get_VNIDs();
    else if (getPhase() == PHASE_WAIT_VNIDS) do_Wait_VNIDs();
    else if (getPhase() == PHASE_SEND_CL_MSG) do_Send_CreateLink_Msg();
  }
  //----------------------------------------------------------------------
  private void do_Send_CreateLink_Msg()
  {
    if (checkExistence()) return;

//     // Verify our attempt to send a create link message uses valid VNID
//     // values (ie, they are all non-zero)
//     String errorReport = "";
//     if (m_source == null) errorReport = errorReport + "m_source is null ";
//     if (m_source.remote() == null) errorReport = errorReport + "m_source.remote() is null ";
//     if (m_source.remote().VNID() == 0) errorReport = errorReport + "m_source.remote() vnid=0 ";

//     if (m_target == null) errorReport = errorReport + "m_target is null ";
//     if (m_target.remote() == null) errorReport = errorReport + "m_target.remote() is null ";
//     if (m_target.remote().VNID() == 0) errorReport = errorReport + "m_target.remote() vnid=0 ";

//     if (m_link.getSource() == null) errorReport = errorReport + "m_link.getSource() is null ";
//     if (m_link.getSource().remote() == null) errorReport = errorReport + "m_link.getSource().remote() is null ";
//     if (m_link.getSource().remote().VNID() == 0) errorReport = errorReport + "m_link.getSource() vnid=0";

//     if (m_link.getTarget() == null) errorReport = errorReport + "m_link.getTarget() is null ";
//     if (m_link.getTarget().remote() == null) errorReport = errorReport + "m_link.getTarget().remote() is null ";
//     if (m_link.getTarget().remote().VNID() == 0) errorReport = errorReport + "m_link.getTarget() vnid=0 ";

//     if (errorReport != "")
//     {
//       PackageLogger.log.warning("CreateLinkJob error: " + errorReport
//                                 + ", when attempting to connect variable "
//                                 + m_link.getSource() + " to "
//                                 + m_link.getTarget());
//     }

    try
    {
      m_prog.onlineCreate(m_link);
    }
    catch (MessageException me)

    {
      String msg = "CreateLink failed: " +  me.toString();
      setResult(msg);
      setPhase(ABORT);
    }

    setPhase(COMPLETE);
  }
  //----------------------------------------------------------------------
  private void do_Wait_VNIDs()
  {
    if (checkExistence()) return;

    if (m_source.remote().isKnown() && m_target.remote().isKnown())
    {
      setPhase(PHASE_SEND_CL_MSG);
    }
  }
  //----------------------------------------------------------------------
  private void do_Phase_Get_VNIDs()
  {
    if (checkExistence()) return;

    if (m_source.remote().isKnown() && m_target.remote().isKnown())
    {
      setPhase(PHASE_SEND_CL_MSG);
    }
    else
    {
      // send requests
      m_prog.remoteVerify(m_source);
      m_prog.remoteVerify(m_target);
      setPhase(PHASE_WAIT_VNIDS);
    }
  }
  //----------------------------------------------------------------------
  public String toString()
  {
    return "Create link from " + m_sVNName + " to " + m_tVNName;
  }
  //----------------------------------------------------------------------
  private boolean checkExistence()
  {
    m_source = null;
    m_target = null;
    UserVN userVN;

    if (nMan.exists(m_sVNName))
    {
      userVN = nMan.getUserVN(m_sVNName);

      if (userVN instanceof NodeVariable)
      {
        m_source = (NodeVariable) userVN;
      }
    }

    if (nMan.exists(m_tVNName))
    {
      userVN = nMan.getUserVN(m_tVNName);

      if (userVN instanceof NodeVariable)
      {
        m_target = (NodeVariable) userVN;
      }
    }

    if (m_source == null)
    {
      setResult("Source variable " + m_sVNName + " does not exist");
    }
    else if (m_target == null)
    {
      setResult("Target variable " + m_tVNName + " does not exist");
    }

    if (m_source == null || m_target == null)
    {
      PackageLogger.log.warning("CreateLink job failed: " + getResult());
      setPhase(ABORT);
      return true;
    }
    return false;
  }
}
