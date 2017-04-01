package nrs.nrsgui;

/**
 * Each node and variable represented in the GUI should have a
 * counterpart in some remote NRS component. The information about the
 * existance of a remote node or variable is represented by this class.
 *
 * @author Darren Smith
 */
class RemoteVNInfo
{
  /**
   * The VNID of the remote object, if known
   */
  private int m_vnid;

  /**
   * Indicate whether anything is known about the remote object
   */
  private boolean m_known = false;

  /** {@link UserVN} this class referes to */
  private UserVN m_model;

  //----------------------------------------------------------------------
  /**
   * Constructor which takes the {@link UserVN} this class will refer
   * to.
   */
  RemoteVNInfo(UserVN model)
  {
    m_model = model;
  }
  //----------------------------------------------------------------------
  /**
   * Reset the remote information to the default state, which is that
   * nothing is known about it.
   */
  void reset()
  {
    m_known = false;
    m_vnid = 0;
  }
  //----------------------------------------------------------------------
  /**
   * Set the VNID. Calling this also sets the do-I-know-anything flag to
   * true.
   */
  void setVNID(int vnid)
  {
    if (vnid == 0)
    {
      String es = "Ignoring attempt to set VNID to 0 for "
        + m_model.VNName() + "; this value is invalid";

      if (m_known = false)
      {
        es = es + "; this node remains without a VNID";
      }
      else
      {
        es = es + "; this node remains with VNID=" + Integer.toString(m_vnid);
      }
      PackageLogger.log.warning(es);

      return;
    }

    m_vnid = vnid;
    m_known = true;
  }
  //----------------------------------------------------------------------
  /**
   * Return whether anything is known about the remote node or variable
   *
   * @return <tt>true</tt> if something is known, <tt>false</tt> otherwise.
   */
  boolean isKnown()
  {
    return m_known;
  }
  //----------------------------------------------------------------------
  /**
   * Return the VNID of the remote object, if it is known.
   *
   * @return the VNID of the remote object. <tt>null</tt> might be
   * returned even if something is known. However, the return value is
   * not defined if nothing is known about the remote object.
   */
  int VNID()
  {
    return m_vnid;
  }
  //----------------------------------------------------------------------
  /**
   * Return the GUI node / variable this information instance refers to.
   */
  UserVN userVN()
  {
    return m_model;
  }
}
