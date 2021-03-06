/*
 * Copyright (C) 2004 Edinburgh University
 *
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation; either version 2 of
 *    the License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public
 *    License along with this program; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307 USA
 *
 * For further information in the first instance contact:
 * Richard Reeve <richardr@inf.ed.ac.uk>
 *
 */
package nrs.nrsgui;

import nrs.util.job.Job;
import nrs.core.base.MessageException;

/**
 * A job for creating a local and remote node.
 *
 * @author Thomas French
 * @author Darren Smith
 */
class CreateNodeJob extends nrs.util.job.Job
{
  private Node m_node;
  private NodeManager nMan = AppManager.getInstance().getNodeManager();

  private final String m_VNName;
  private final Programmer m_prog;

  public static final String PHASE_GET_VNIDS = "Obtaining VNIDs";
  public static final String PHASE_WAIT_VNIDS = "Waiting for VNID";
  public static final String PHASE_SEND_CN_MSG = "Sending CreateNode";

  //----------------------------------------------------------------------
  /**
   * Constructs an instance for a job to create a remote node.
   *
   * <p>During the various steps of the job, a check is made to ensure
   * the source and target variables of the <tt>link</tt> still exist
   * (this is done by VN-name lookup into the {@link NodeManager}). If
   * at any stage they are not found, this job aborts.
   */
  CreateNodeJob(Programmer prog, Node node)
  {
    m_prog = prog;
    m_node = node;

    m_VNName = node.VNName();

    setPhase(PHASE_SEND_CN_MSG);
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
    else if (getPhase() == PHASE_SEND_CN_MSG) do_Send_CreateNode_Msg();
  }
  //----------------------------------------------------------------------
  private void do_Send_CreateNode_Msg()
  {
    if (checkExistence()) return;

    PackageLogger.log.fine("Online create by Thread: " 
                           + Thread.currentThread().getName());
    m_prog.onlineCreate(m_node);
    
    setPhase(PHASE_GET_VNIDS);
  }
  //----------------------------------------------------------------------
  private void do_Wait_VNIDs()
  {
    if (checkExistence()) return;

    if (m_node.remote().isKnown() )
    {
      setPhase(COMPLETE);
    }
  }
  //----------------------------------------------------------------------
  private void do_Phase_Get_VNIDs()
  {
    if (checkExistence()) return;

    if ( m_node.remote().isKnown() )
    {
      setPhase(COMPLETE);
    }
    else
    {
      // send requests
      PackageLogger.log.fine("Remote Verify by Thread: " 
                             + Thread.currentThread().getName());
      m_prog.remoteVerify(m_node);
      setPhase(PHASE_WAIT_VNIDS);
    }
  }
  //----------------------------------------------------------------------
  public String toString()
  {
    return "Create node with VNName: " + m_VNName;
  }
  //----------------------------------------------------------------------
  private boolean checkExistence()
  {
    Node m_source = null;
    UserVN userVN;

    if (nMan.exists(m_VNName))
    {
      userVN = nMan.getUserVN(m_VNName);

      if (userVN instanceof Node)
      {
        m_source = (Node) userVN;
      }
    }

    if (m_source == null )
    {
      setResult("Node " + m_VNName + " does not exist");
      PackageLogger.log.warning("CreateNode job failed: " + getResult());
      setPhase(ABORT);
      return true;
    }
    return false;
  }
}
