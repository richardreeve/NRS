package nrs.nrsgui;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 * Provides various shared resources for use throughout the NRS gui.
 *
 * <p>The range of Java borders are currently being provided.
 *
 * <p>A static instance is provided for convenience. See the {@link
 * #getInstance()} method.
 */
class Resources
{
  private static Border m_loweredBevelBorder;
  private static Resources m_instance;

  //----------------------------------------------------------------------
  /**
   * Return the class static instance, which is provided for convenience
   * only. This class is not a singleton, so other instances could be
   * created, although they are really not necessary.
   */
  static Resources getInstance()
  {
    if (m_instance == null)
    {
      m_instance = new Resources();
    }

    return m_instance;
  }
  //----------------------------------------------------------------------
  Border getLoweredBevelBorder()
  {
    if (m_loweredBevelBorder == null)
    {
      m_loweredBevelBorder = BorderFactory.createLoweredBevelBorder();
    }

    return m_loweredBevelBorder;
  }

}
