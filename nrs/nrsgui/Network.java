package nrs.nrsgui;

/**
 * A network consists of a collection of nodes together with
 * supplimentary information, such as the filename, the author, etc.
 */
public class Network extends DefaultNodeCollection
{
  /**
   * Name of the network. This is especially meaningful for a root
   * network; however for subnetworks it will typically be the label of
   * the network's parent node.
   */
  private String m_name;

  /** Author information  */
  private String m_author;

  /** Comments */
  private String m_description;

  /** Name of an associated file */
  private String m_filename;

  /** Has this network changed */
  private boolean m_hasChanged = false;

  //----------------------------------------------------------------------
  /**
   * Creates a network with the specified information. Because values
   * have been specied, this network assumes a has-changed status.
   */
  public Network(String name, String author, String description)
  {
    m_name = name;
    m_author = author;
    m_description = description;
    m_hasChanged = true;
  }
  //----------------------------------------------------------------------
  /**
   * Creates a network with no name, author and description
   */
  public Network()
  {
    m_name = "";
    m_author = "";
    m_description = "";
  }
  //----------------------------------------------------------------------
  /**
   * Set the name
   */
  public void setName(String name)
  {
    m_name = name;
  }
  //----------------------------------------------------------------------
  /**
   * Set the description
   */
  public void setDescription(String description)
  {
    m_description = description;
  }
  //----------------------------------------------------------------------
  /**
   * Set the filename
   */
  public void setFilename(String filename)
  {
    m_filename = filename;
  }
  //----------------------------------------------------------------------
  /**
   * Set the author
   */
  public void setAuthor(String author)
  {
    m_author = author;
  }
  //----------------------------------------------------------------------
  /**
   * Get the name
   */
  public String getName()
  {
    return m_name;
  }
  //----------------------------------------------------------------------
  /**
   * Get the author
   */
  public String getAuthor()
  {
    return m_author;
  }
  //----------------------------------------------------------------------
  /**
   * Get the description
   */
  public String getDescription()
  {
    return m_description;
  }
  //----------------------------------------------------------------------
  /**
   * Get the filename
   */
  public String getFilename()
  {
    return m_filename;
  }
  //----------------------------------------------------------------------
  /**
   * Get whether this network has changed
   *
   * @return <tt>true</tt> if there have been changes, <tt>false</tt>
   * otherwise
   *
   * @see #resetChanged()
   */
  boolean hasChanged()
  {
    return m_hasChanged;
  }
  //----------------------------------------------------------------------
  /**
   * Reset the has-changed status of this network, so that it indicates
   * there is no change
   */
  void resetChanged()
  {
    m_hasChanged = false;
  }
  //----------------------------------------------------------------------
  /**
   * Returns the network name. Same as call to {@link #getName()}.
  x */
  public String toString()
  {
    return getName();
  }

}
