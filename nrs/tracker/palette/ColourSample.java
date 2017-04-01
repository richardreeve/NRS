// $Id: ColourSample.java,v 1.2 2005/05/09 22:03:55 hlrossano Exp $
package nrs.tracker.palette;

import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;

/**
 * A simple component which just paints itself with a particular colour
 */
public class ColourSample extends JComponent
{
  private int m_R = 255;
  private int m_G = 255;
  private int m_B = 255;

  //----------------------------------------------------------------------
  /**
   * Constructor. Sets the preferred, minimum and maximum size to the
   * same size as a {@link JLabel} representng the supplied
   * text. Ofcourse the client is free to set these using the
   * setPreferredSize(), setMinimumSize() and setMaximumSize() inherited
   * methods.
   */
  ColourSample(String text)
  {
    JComponent b = new JLabel(text);
    
    setPreferredSize(b.getPreferredSize());
    setMinimumSize(b.getMinimumSize());
    setMaximumSize(b.getMaximumSize());
  }
  //----------------------------------------------------------------------
  /** 
   * Override the Swing paintComponent method for custom painting
   */
  protected void paintComponent(Graphics g)
  { 
    Rectangle region = g.getClipBounds();
    if (region == null)
      region = new Rectangle(0, 0, getWidth(), getHeight());
    
    g.setColor(new Color(m_R, m_G, m_B));
    g.fillRect(region.x, region.y, region.width, region.height);
  }
  //----------------------------------------------------------------------
  /**
   * Set new RGB values, and cause a repaint().
   */
  public void setRGB(int r, int g, int b)
  {
    m_R = r;
    m_G = g;
    m_B = b;
    repaint();
  }
}
