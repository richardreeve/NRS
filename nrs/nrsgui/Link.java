package nrs.nrsgui;

/**
 * Represent a link between a pair of {@link NodeVariable} objects.
 *
 * @author  Darren Smith
 */
public class Link
{
  private NodeVariable m_source;
  private NodeVariable m_target;
  private boolean m_exists;

  //----------------------------------------------------------------------
  /**
   * Create a link representing communication from the source variable
   * to the destination variable. Neither parameter should be passed null.
   *
   * @param source the {@link NodeVariable} which is a data sender
   *
   * @param target the {@link NodeVariable} which is a data receiver
   *
   * @param exists whether this link actually exists (or is just a
   * potential link)
   */
  public Link(NodeVariable source,
              NodeVariable target,
              boolean exists)
  {
    if (source == null) throw new NullPointerException("source is null");
    if (target == null) throw new NullPointerException("target is null");

    m_source = source;
    m_target = target;
    m_exists = exists;
  }
  //----------------------------------------------------------------------
  /**
   * Return the source {@link NodeVariable}
   */
  public NodeVariable getSource()
  {
    return m_source;
  }
  //----------------------------------------------------------------------
  /**
   * Return the target {@link NodeVariable}
   */
  public NodeVariable getTarget()
  {
    return m_target;
  }
  //----------------------------------------------------------------------
  /**
   * Return whether this link actually exists
   */
  boolean getExists()
  {
    return m_exists;
  }
  //----------------------------------------------------------------------
  /**
   * Set whether this link actually exists
   */
  void setExists(boolean exists)
  {
    m_exists = exists;
  }
}
