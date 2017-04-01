package nrs.nrsgui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;

/** This class represents a pin which extends from a {@link
    GuiNode}. Pins are used to graphically represent NRS <Interface>
    elements. Connections between different interfaces will be
    represented as lines between pins. */
public class NetworkObjectPin implements VariableListener
{
  static Stroke PinStroke = new BasicStroke(1.0f);
  static Color PinColour  = Color.black;

  /** Generic label associated with the pin. Will generally be derived
   * from the uderlying data mode - ie the NRS interface which this pin
   * represents */
  String m_label;

  /** Offset of the pin origin in the frame of the node. The
   * pin origin is the point where the pin meets the node. */
  private Point m_pinO;

  /** Offset of the pin target location in the frame of the
   * node. The pin target is the point where the pin is
   * furthest from the node. */
  private Point m_pinT;

  /** Size of the anchor which is draw at the target of the pin */
  private final int m_radius;
  private final double m_radiusSq;

  /** Double the radius value */
  private final int m_diameter;

  /** Colour in which the pin's anchor will be filled in */
  private Color m_fill;

  /** Indicate whether the user has elected to hide this pin. If there
   * are many pins associated with node, then for convienence,
   * the user might chose to hide the majority of them and only show the
   * most important (such as those which possess actual links). */
  private boolean m_visible;

  //----------------------------------------------------------------------

  /**
   * Constructor used for test purposes, to create any kind of pin
   *
   * <bold>Note</bold>, the <code>Point</code> objects passed are
   * locally copied.
   *
   * @param label string label of pin
   * @param ps a {@link Point} specifying the source of the pin
   * @param pt a {@link Point} specifying the target of the pin
   * @param radius size of circle to draw around the target point
   * @param fill colour of pin's circle
   * @param visible whether pin should be displayed or not
   **/
  public NetworkObjectPin(String label, Point ps, Point pt,
                          int radius, Color fill, boolean visible)
  {
    m_label = label;
    m_pinO = new Point(ps);
    m_pinT = new Point(pt);
    m_radius = radius;
    m_diameter = 2 * radius;
    m_radiusSq = radius * radius;
    m_fill = fill;
    m_visible = visible;;
  }
  //----------------------------------------------------------------------
  /**
   * Implements {@link DisplayDelegate} interface
   */
  public boolean contains(Point p,
                          int x, int y)
  {
    Point abs_pinT = new Point(x + m_pinT.x,
                               y + m_pinT.y);

    return (abs_pinT.distanceSq(p) < m_radiusSq);
  }
  //----------------------------------------------------------------------
  /** Paint the pin onto the graphical context relative the provided
   * coordinate
   */
  void paint(Graphics2D g2d,
             int x,
             int y)
  {
    if (m_visible)
      {
        // Note, do this for dummy code, but generally paint routines should
        // not feel responsible for do stroke restoration
        g2d.setStroke(PinStroke);

        // draw the line
        g2d.setColor(PinColour);
        g2d.drawLine(x + m_pinO.x, y + m_pinO.y,
                     x + m_pinT.x, y + m_pinT.y);

        // fill the circle
        g2d.setColor(m_fill);
        g2d.fillOval(x + m_pinT.x - m_radius,
                     y + m_pinT.y - m_radius,
                     m_diameter, m_diameter);

        // draw the circle
        g2d.setColor(PinColour);
        g2d.drawOval(x + m_pinT.x - m_radius,
                     y + m_pinT.y - m_radius,
                     m_diameter, m_diameter);
      }
  }
  //----------------------------------------------------------------------
  /**
   * Gets the static bounds of this component in the form of a {@link
   * Rectangle} object. Static bounds are independent of the current
   * position of the pin on the display.
   *
   * The rectangle passed in, if non <code>null</code>, will be updated
   * and passed back as the return parameter.
   */
  Rectangle getStaticBounds(Rectangle r)
  {
    if (r == null) r = new Rectangle();

    if (m_pinO.x < m_pinT.x)
    {
      r.x = m_pinO.x;
      r.y = m_pinT.y - m_radius;
      r.width = m_pinT.x - m_pinO.x + m_radius;
      r.height = m_diameter;
      return r;
    }

    if (m_pinO.x > m_pinT.x)
    {
      r.x = m_pinT.x - m_radius;
      r.y = m_pinT.y - m_radius;
      r.width = m_pinO.x - (m_pinT.x - m_radius);
      r.height = m_diameter;
      return r;
    }

    if (m_pinO.y > m_pinT.y)
    {
      r.x = m_pinT.x - m_radius;
      r.y = m_pinT.y - m_radius;
      r.width = m_diameter;
      r.height = m_pinO.y - m_pinT.y + m_radius;
      return r;
    }

    if (m_pinO.y < m_pinT.y)
    {
      r.x = m_pinT.x - m_radius;
      r.y = m_pinO.y;
      r.width = m_diameter;
      r.height = m_pinT.y - m_pinO.y + m_radius;
      return r;
    }

    // If we are here, then seems like m_pinO == m_pinT
    r.x = m_pinT.x - m_radius;
    r.y = m_pinT.y - m_radius;
    r.width = m_diameter;
    r.height = m_diameter;

    return r;
  }
  //----------------------------------------------------------------------
  public void linkAdded(NodeVariable source)
  {
    if (m_label.equals("in"))
    {
      if (source.getInboundLinksCount() > 0)
      {
        m_fill = GuiVariable.USEDPIN_COLOR;
      }
      else
      {
        m_fill = GuiVariable.IPIN_COLOR;
      }
    }
    else if (m_label.equals("out"))
    {
      if (source.getOutboundLinksCount() > 0)
      {
        m_fill = GuiVariable.USEDPIN_COLOR;
      }
      else
      {
        m_fill = GuiVariable.IPIN_COLOR;
      }
    }

    /** Ok, this is a minor design problem. Since display delegates are
        not inherited from Java GUI classes, there is now no invalidate
        method I can call. Must change this: each display component
        should inherit from JComponent, or something. */

  }
  //----------------------------------------------------------------------
  public void linkRemoved(NodeVariable source)
  {
    linkAdded(source);
  }
}
