package nrs.core.base;

import nrs.core.message.Constants;

/**
 * This collects together all of the most basic information known about
 * an NRS component.
 */
public class ComponentInfo
{
  private String m_cType;
  private String m_cid;
  private boolean m_speaksBMF;
  private boolean m_speaksPML;
  private String m_version;
  private String m_route;

  //----------------------------------------------------------------------
  /**
   * Create an instance based on the contents of an NRS message which
   * should contain a {@link
   * nrs.core.message.Constants.MessageFields#cid} field.
   */
  public ComponentInfo(Message m)
  {
    m_cType = "unknown component";

    if (m == null) throw new NullPointerException("message can't be null");

    m_cid = m.getField(Constants.MessageFields.cid);
  }
  //----------------------------------------------------------------------
  /**
   * Fully specifiy the state the instance created
   */
  public ComponentInfo(String cType,
                       String cid,
                       String route,
                       boolean speaksBMF,
                       boolean speaksPML,
                       String version)
  {
   m_cType = cType;
   m_cid = cid;
   m_route = route;
   m_speaksBMF = speaksBMF;
   m_speaksPML =speaksPML;
   m_version = version;
  }
  //----------------------------------------------------------------------
  /**
   * Get the version value
   *
   * @return the version value or <tt>null</tt> if this has not yet been
   * obtained.
   */
  public String getCVersion()
  {
    return m_version;
  }
  //----------------------------------------------------------------------
  /**
   * Set the version value
   *
   * @param newVersion The new version value
   */
  public void setCVersion(String newVersion)
  {
    m_version = newVersion;
  }
  //----------------------------------------------------------------------
  /**
   * Get the cType of this NRS component.
   *
   * @return the cType of the component or <tt>null</tt> if the cType is
   * not yet known
   */
  public String getCType()
  {
    return m_cType;
  }
  //----------------------------------------------------------------------
   /**
   * Set the cType of this NRS component.
   */
  public void setCType(String cType)
  {
    m_cType = cType;
  }
  //----------------------------------------------------------------------
  /**
   * Get the unique component identifier, or <tt>null</tt> if one has
   * not yet been provided.
   */
  public String getCID()
  {
    return m_cid;
  }
  //----------------------------------------------------------------------
  /**
   * Set the unique component identifier
   */
  public void setCID(String cid)
  {
    m_cid = cid;
  }
  //----------------------------------------------------------------------
  /**
   * Get the route to this component.
   *
   * @return the route, either full or partial, or <tt>null</tt> if the
   * route is not yet known.
   */
  public String getRoute()
  {
    return m_route;
  }
  //----------------------------------------------------------------------
  /**
   * Set the route to this component.
   */
  public void setRoute(String route)
  {
    m_route = route;
  }
  //----------------------------------------------------------------------
  /**
   * Return the name (if known) and the unique ID. The format of the
   * returned string is :
   *
   *<p> name ( CID )</p>
   */
  public String toString()
  {
    return m_cType+ " ( " + m_cid + " )";
  }
  //----------------------------------------------------------------------
  /**
   * Get whether this component supports BMF messages.
   */
  public boolean getSpeaksBMF()
  {
    return m_speaksBMF;
  }
  //----------------------------------------------------------------------
  /**
   * Set whether this component supports BMF messages.
   */
  public void setSpeaksBMF(boolean b)
  {
    m_speaksBMF = b;
  }
  //----------------------------------------------------------------------
  /**
   * Get whether this component supports PML messages.
   */
  public boolean getSpeaksPML()
  {
    return m_speaksPML;
  }
  //----------------------------------------------------------------------
  /**
   * Set whether this component supports PML messages.
   */
  public void setSpeaksPML(boolean b)
  {
    m_speaksPML = b;
  }
}

