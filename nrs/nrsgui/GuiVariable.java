package nrs.nrsgui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Iterator;
import nrs.csl.CSL_Element_Interface;
import nrs.csl.CSL_Element_Variable;

/*
 * A {@link DisplayDelegate} implementation for drawing a grahical
 * representation of an internal variable of a node.
 *
 * _BUG_(Display update during dragging is buggy)
 */
class GuiVariable implements DisplayDelegate
{

  /** Dynamic bounds of the invisible rectangle enclosing the
   * variable. It is dynamic in the sense that the x and y fields of the
   * {@link Rectangle} contain the screen coordinates of the
   * rectangle */
  private final Rectangle m_rect;

  // Offset between bounding rect location, and internal frame
  private int m_offsetH;
  private int m_offsetV;

  /**
   * Bounds of the circle to draw, which is the main representation of a
   * variable. The x and y location are set to 0,0 - it represents the
   * original of the internal frame.
   */
  private Rectangle m_circle = new Rectangle();

  /**
   * The rectangle that bounds all graphics items. The full bounding box
   * is just this core box but expanded to include the buffer region.
   */
  Rectangle m_core = new Rectangle();

  /** List of the pins -  each object should be of type NetworkObjectPin */
  private ArrayList m_pins = new ArrayList();

  /** Show input pin? */
  private boolean m_vis_ip = true;

  /** Show out pin? */
  private boolean m_vis_op = true;

  /** Show log pin? */
  private boolean m_vis_lp = true;

  /** Control whether a layout is needed */
  private boolean m_layoutNeeded = true;

  /** Selected state */
  private boolean m_selected = false;

  /**
   * Bounding box of the text label, with respect to, the internal frame
   */
  private Rectangle m_label = new Rectangle();

  /**
   * Point at which to draw the label - this is different from the
   * m_label bounding box, because the latter will take in the ascent
   * and descent heights.
   */
  private Point m_labelPos = new Point();

  /**
   * Controls whether labels are visible
   */
  private static boolean sm_labelVisible = true;

  /** Reference to the underlying node variable */
  private final NodeVariable m_nodeVar;

  /** Used for stacking of display objects one above the other */
  private int m_displayHeight = DisplayDelegate.NOHEIGHT;

  // Colours for the various sections of the output. These are
  // deliberately non-private so that they can be controlled from
  // outside.
  public static Color VARIABLE_FILL_COLOR = Color.blue;
  public static Color VARIABLE_OUTLINE_COLOR = Color.black;
  public static Color VARIABLE_TEXT_COLOR = Color.black;
  public static Color IPIN_COLOR = Color.green;
  public static Color OPIN_COLOR = Color.green;
  public static Color LPIN_COLOR = Color.green;
  public static Color USEDPIN_COLOR = Color.orange;

  /* ----- Layout constants ----- */

  // Pin dimensions
  public static int PIN_VERT_LENGTH = 15;
  public static int PIN_HORZ_LENGTH = 15;

  // Buffer size
  public static int VBUFFER = 5;
  public static int HBUFFER = 5;
  public static int SELECT_ZONE = 2;  // space for appearence of selector box

  // Label offset
  public static int NAME_V_OFFSET = -10;
  public static int NAME_H_OFFSET = 0;

  // Radius of principle circle
  public static int CIRCLE_RADIUS = 20;

  // Radius of pins
  public static int PIN_RADIUS = 5;

  // Initial placement
  public static int INITIAL_X = 10;
  public static int INITIAL_Y = 10;
  public static int VERT_SEPARATION = 10;

  // Estimates of the font dimensions
  private static int m_fontAscent = 10;
  private static int m_fontHeight = 10;

  private static Stroke sm_stroke = new BasicStroke(1.0f);

  //----------------------------------------------------------------------
  /*
   * Constructor. Accepts a reference to the {@link NodeVariable} which
   * this class will draw a graphical representation of.
   */
  GuiVariable(NodeVariable nodeVar, int x, int y)
  {
    m_nodeVar = nodeVar;

    // m_rect is fully calculated later, and is not fully known until
    // the first call to {@link #paint}.
    m_rect = new Rectangle(x, y, 0, 0);

    layout(null);
  }
  //----------------------------------------------------------------------
  private void addPins()
  {
    CSL_Element_Variable var = m_nodeVar.getType();

    for (Iterator j = var.getInterfaceIterator(); j.hasNext(); )
    {
      addPin((CSL_Element_Interface) j.next(), var);
    }
  }
  //----------------------------------------------------------------------
  private void addPin(CSL_Element_Interface intface,
                      CSL_Element_Variable var)
  {
    Color pinColor;

    if (intface.getMaxOccurs() == 0) return;

    if (intface.isIn())
    {
      Point ps = new Point(m_circle.x,
                           m_circle.y + CIRCLE_RADIUS);
      Point pt = new Point(ps.x - PIN_HORZ_LENGTH, ps.y);

      if (m_nodeVar.getInboundLinksCount() > 0)
      {
        pinColor = USEDPIN_COLOR;
      }
      else
      {
        pinColor = IPIN_COLOR;
      }

      NetworkObjectPin nop = new NetworkObjectPin("in",
                                                  ps,
                                                  pt,
                                                  PIN_RADIUS,
                                                  pinColor,
                                                  true);

      m_pins.add(nop);
      m_nodeVar.addListener(nop);
    }
    else if (intface.isOut())
    {
      Point ps = new Point(m_circle.x + m_circle.width,
                           m_circle.y + CIRCLE_RADIUS);
      Point pt = new Point(ps.x +  PIN_HORZ_LENGTH, ps.y);

      if (m_nodeVar.getOutboundLinksCount() > 0)
      {
        pinColor = USEDPIN_COLOR;
      }
      else
      {
        pinColor = OPIN_COLOR;
      }

      NetworkObjectPin nop = new NetworkObjectPin("out",
                                                  ps,
                                                  pt,
                                                  PIN_RADIUS,
                                                  pinColor,
                                                  true);
      m_pins.add(nop);
      m_nodeVar.addListener(nop);
    }
    else if (intface.isLog())
    {
      Point ps = new Point(m_circle.x + CIRCLE_RADIUS,
                           m_circle.y + m_circle.height);
      Point pt = new Point(ps.x, ps.y + PIN_VERT_LENGTH);

      if (m_nodeVar.getLogLinksCount() > 0)
      {
        pinColor = USEDPIN_COLOR;
      }
      else
      {
        pinColor = LPIN_COLOR;
      }

      m_pins.add(new NetworkObjectPin("log",
                                      ps,
                                      pt,
                                      PIN_RADIUS,
                                      pinColor,
                                      true));
    }
  }
  //----------------------------------------------------------------------
  /*
   * Set display height at which an instance should be
   * displayed. Objects with lower values are drawn first (and so appear
   * at the bottom of a group) while higher values are drawn last (and
   * so appear at the top of a group).
   */
  public void setDisplayHeight(int h)
  {
    m_displayHeight = h;
  }
  //----------------------------------------------------------------------
  /**
   * Return the name of the underlying node variable by calling its
   * <code>toString()</code> method
   */
  public String toString()
  {
    return m_nodeVar.toString();
  }
  //---------------------------------------------------------------------------
  /*
   * Implement the {@link DisplayDelegate} interface
   */
  public void paint(Graphics2D g2d)
  {
    g2d.setStroke(sm_stroke);

    if (m_layoutNeeded) layout(g2d);

    int x = m_rect.x + m_offsetH;
    int y = m_rect.y + m_offsetV;

    // Draw the cricle
    g2d.setPaint(VARIABLE_FILL_COLOR);
    g2d.fillOval(x + m_circle.x, y + m_circle.y,
                 m_circle.width, m_circle.height);

    if (VARIABLE_FILL_COLOR != VARIABLE_OUTLINE_COLOR)
    {
      g2d.setPaint(VARIABLE_OUTLINE_COLOR);
      g2d.drawOval(x + m_circle.x, y + m_circle.y,
                 m_circle.width, m_circle.height);
    }

    g2d.setPaint(VARIABLE_TEXT_COLOR);
    g2d.drawString(m_nodeVar.toString(),
                   x + m_labelPos.x,
                   y + m_labelPos.y);

    for (Iterator i = m_pins.iterator(); i.hasNext();)
    {
      NetworkObjectPin pin = (NetworkObjectPin) i.next();
      pin.paint(g2d, x, y);
    }

    if (m_selected)
    {
      g2d.drawRoundRect(m_rect.x, m_rect.y,
                        m_rect.width - 1, m_rect.height - 1, 2,2);
    }
  }
  //---------------------------------------------------------------------------
  /*
   * Implement the {@link DisplayDelegate} interface
   *
   */
  public void paintSelected(Graphics2D g2d)
  {
    paint(g2d);

    g2d.drawRoundRect(m_rect.x,
                      m_rect.y,
                      m_rect.width - 1,
                      m_rect.height - 1,
                      2,2);
  }
  //---------------------------------------------------------------------------
  // Implement the {@link DisplayDelegate} interface
  public int x()
  {
    return m_rect.x;
  }
  //---------------------------------------------------------------------------
  // Implement the {@link DisplayDelegate} interface
  public int y()
  {
    return m_rect.y;
  }
  //----------------------------------------------------------------------
  /*
   * Return height at which an instance should be displayed. Lower
   * values are drawn first (and so appear at the bottom of a group)
   * while higher values are drawn last (and so appear at the top of a
   * group).
   */
  public int getDisplayHeight()
  {
    return m_displayHeight;
  }
  //---------------------------------------------------------------------------
  /**
   * Return the underlying {@link NodeVariable} this class is drawing.
   */
  NodeVariable getNodeVariable()
  {
    return m_nodeVar;
  }
  //----------------------------------------------------------------------
  /*
   * Implement the {@link DisplayDelegate} interface
   */
  public void setLocation(int x, int y)
  {
    m_rect.x = x;
    m_rect.y = y;
  }
  //----------------------------------------------------------------------
  /*
   * Implement the {@link DisplayDelegate} interface
   */
  public boolean contains(Point p)
  {
    return m_rect.contains(p);
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link DisplayDelegate} interface. Note, the width
   * component of the returned rectangle is not fully known until self
   * has been painted for the first time.
   */
  public Rectangle getBounds(Rectangle r)
  {
    if (r == null)
    {
      return new Rectangle(m_rect);
    }
    else
    {
      r.setBounds(m_rect);
      return r;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link DisplayDelegate} interface
   */
  public Rectangle bounds(Rectangle r)
  {
    if (r == null) r = new Rectangle();

    r.setBounds(m_rect);

    return r;
  }
  //----------------------------------------------------------------------
  /**
   * Calculate the positions of the various graphics elments of this
   * component, and then determine the overall bounding box.
   *
   * <p>Note, because the {@link Graphics2D} parameter is required
   * because the string elements cannot be laid out without font metric
   * information (which we get from the graphics object). The caller
   * should ensure the appropriate font stroke has been set for the
   * graphics object.
   */
  private void layout(Graphics2D g2d)
  {
    m_layoutNeeded = false;

    int circDiam = 2 *  CIRCLE_RADIUS;

    // Arrange main circle - note it is at the origin of internal frame
    m_circle.setBounds(0,0,circDiam+1, circDiam+1);

    // Arrange the label
    m_labelPos.x = m_circle.x + NAME_H_OFFSET;
    m_labelPos.y = m_circle.y + NAME_V_OFFSET;

    if (g2d != null)
    {
      FontMetrics fm = g2d.getFontMetrics();
      m_label.x = m_labelPos.x;
      m_label.y = m_labelPos.y - fm.getMaxAscent();
      m_label.width = fm.stringWidth(m_nodeVar.toString());
      m_label.height = fm.getHeight();

      m_fontAscent = fm.getMaxAscent();
      m_fontHeight = fm.getHeight();
    }
    else
    {
      m_label.x = m_labelPos.x;
      m_label.y = m_labelPos.y - m_fontAscent;
      m_label.width = 100;  // pure guess
      m_label.height = m_fontHeight;

      // set flag that to make sure we come back to this routine
      m_layoutNeeded = true;
    }

    // Arrange pins
    addPins();

    // Determine the m_core boundary
    m_core.setBounds(0,0,0,0);
    m_core = m_core.union(m_circle);
    m_core = m_core.union(m_label);

    for (Iterator i = m_pins.iterator(); i.hasNext();)
    {
      NetworkObjectPin pin = (NetworkObjectPin) i.next();

      m_core = m_core.union(pin.getStaticBounds(null));
    }

    // Determine offsets between bounding box location, and internal
    // frame origin
    m_offsetH = SELECT_ZONE + HBUFFER - m_core.x;
    m_offsetV = SELECT_ZONE + VBUFFER - m_core.y;

    // Set size of bounding box
    m_rect.width = m_core.width + 2 * HBUFFER + 2 * SELECT_ZONE;
    m_rect.height = m_core.height + 2 * VBUFFER + 2 * SELECT_ZONE;

  }
  //----------------------------------------------------------------------
  /*
   * Implement the {@link DisplayDelegate} interface.
   *
   * @return the {@link NodeVariable} object self represents.
   */
  public Object getObject()
  {
    return m_nodeVar;
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link DisplayDelegate} interface
   */
  public void setSelected(boolean value)
  {
    m_selected = value;
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link DisplayDelegate} interface
   */
  public boolean getSelected()
  {
    return m_selected;
  }
}
