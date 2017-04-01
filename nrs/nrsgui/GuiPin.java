package nrs.nrsgui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

/**
 * This class represents a pin which can extend from either a graphical
 * representation from either a node or variable. Pins represent the
 * interfaces available on variables.
 *
 * <p>This class is meant to replace the existing class
 * NetworkObjectPin.
 *
 * <p>This object maintains a link to the underlying variable, so that when
 * links are created/delted which involve the variable, the gui object
 * is automatically updated.
 *
 * <p>TODO
 * <p>1. Add an observable reference to the variable
 * <p>2. Update the display of the pin when a link is made/removed
 * <p>3. Set the GuiVariable to use this class
 * <p>4. Set the GuiNove to use this class
 * <p>5. Remove old code from build and tree
 * <p>6. Add a text label which is number of connections made
 */
class GuiPin
{
  /* Standard class constants for setting rendering options */
  static Stroke PinStroke = new BasicStroke(1.0f);
  static Color PinOutline  = Color.black;
  static Color PinFill  = Color.green;
  static int PinRadius = 6;
  static int PinDiam = 12;
  static int PinLength = 10;

  /* Constants for describing the direction of then pin */
  static final Object NORTH = "north";
  static final Object SOUTH = "south";
  static final Object EAST = "east";
  static final Object WEST = "west";

  /* Constants for describing the type of the pin */
  static final Object IN = "in";
  static final Object OUT = "out";
  static final Object LOG = "log";

  /** Location of the pin origin, typically with respect to the frame of
   * the node or variable which the pin if part of. The pin origin is
   * the point where the pin meets the node. */
  private Point m_pinO;

  /** Location of the pin target, typically with respect to the frame of
   * the node or variable which the pin if part of. The pin target is
   * the point where the pin is furthest from the node. */
  private Point m_pinT;

  /** Actual fill colour for the pin */
  private Color m_fill;

  /** Direction of the pin */
  private Object m_direction;

  /** Type of the pin */
  private Object m_type;

  /** Indicate whether the user has elected to hide this pin. If there
   * are many pins associated with node, then for convienence, the user
   * might chose to hide the majority of them and only show the most
   * important (such as those which possess actual links). */
  private boolean m_visible;

  // Object for which who's location the pin will be drawn relative to.
  private DisplayDelegate m_frame;

  // Bounding rectangle
  private Rectangle m_bounds = new Rectangle();

  // Reference to underlying variable this pin relates to
  private NodeVariable m_variable;

  //----------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param direction the direction the pin is drawn, either {@link
   * #NORTH}, {@link #SOUTH}, {@link #EAST} or {@link #WEST}.
   *
   * @param type the kind of interface the pin represents, either {@link
   * #IN}, {@link #OUT}, {@link #LOG}
   *
   * @param x the x-coordinate of where the pin's tail ends, relative to
   * an owner
   *
   * @param y the y-coordinate of where the pin's tail ends, relative to
   * an owner
   */
  GuiPin(Object direction,
         Object type,
         int x, int y,
         NodeVariable variable)
  {
    m_fill = PinFill;
    m_direction = direction;
    m_type = type;
    m_pinO = new Point(x,y);
    m_variable = variable;

    configurePin(x,y);
  }
  //----------------------------------------------------------------------
  private void configurePin(int x, int y)
  {
    if (m_direction == NORTH)
    {
      m_pinT = new Point(x, y-PinLength);
      m_bounds.setBounds(x - PinRadius,
                         y - PinLength - PinRadius,
                         PinDiam,
                         PinLength + PinRadius);
    }
    else if (m_direction == SOUTH)
    {
      m_pinT = new Point(x, y+PinLength);
      m_bounds.setBounds(x - PinRadius,
                         y,
                         PinDiam,
                         PinLength + PinRadius);
    }
    else if (m_direction == EAST)
    {
      m_pinT = new Point(x+PinLength, y);
      m_bounds.setBounds(x,
                         y - PinRadius,
                         PinLength + PinRadius,
                         PinDiam);
    }
    else if (m_direction == WEST)
    {
      m_pinT = new Point(x-PinLength, y);
      m_bounds.setBounds(x - PinLength + PinRadius,
                         y - PinRadius,
                         PinLength + PinRadius,
                         PinDiam);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Paint this pin relative to the provided screen coordinates
   */
  public void paint(Graphics2D g2d, int x, int y)
  {
    if (m_visible)
    {
      g2d.setStroke(PinStroke);

      // set colour
      g2d.setColor(m_fill);

      // fill the circle
      g2d.fillOval(x + m_pinT.x - PinRadius,
                   y + m_pinT.y - PinRadius,
                   PinDiam, PinDiam);

      // set colour
      g2d.setColor(PinOutline);

      // draw the line
      g2d.drawLine(x + m_pinO.x, x + m_pinO.y,
                   y + m_pinT.x, y + m_pinT.y);

      // draw the circle
      g2d.drawOval(x + m_pinT.x - PinRadius,
                   y + m_pinT.y - PinRadius,
                   PinDiam, PinDiam);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Return whether the provided point falls within the space taken up
   * by this pin
   */
  public boolean contains(Point e)
  {
    return m_bounds.contains(e);
  }
  //----------------------------------------------------------------------
  /**
   * Get the space taken up by this pin.
   */
  public Rectangle bounds(Rectangle r)
  {
    if (r == null)
    {
      r = new Rectangle(m_bounds);
    }
    else
    {
      r.setBounds(m_bounds);
    }

    return r;
  }
}
