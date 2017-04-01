package nrs.core.base.discovery;

/**
 * Each DiscoveredRoute object is associated with a known route. Also
 * stored is information about the status of the enquiry of that route
 * and the component that exists at the end of the route.
 *
 * DiscoveredRoute objects have the following states:
 *
 * QCID_NOT_SENT - means a QCID message has not yet been sent.
 *
 * AWAITING_RCID - means a QCID has been sent, but a reply has not been
 * received.
 */
class DiscoveredRoute
{
  public enum State { QCID_NOT_SENT,
                      AWAITING_RCID };

  private State m_state = State.QCID_NOT_SENT;
  private String m_route;
  private String m_cid;

  /**
   * Constructor
   *
   * @param route a String representing a route
   */
  DiscoveredRoute(String route)
  {
    m_route = route;
  }

  /** Return the state */
  State getState()
  {
    return m_state;
  }

  /** Set the state */
  void setState(State s)
  {
    m_state = s;
  }

  /** Return the route this object represents */
  String getRoute()
  {
    return m_route;
  }


}
