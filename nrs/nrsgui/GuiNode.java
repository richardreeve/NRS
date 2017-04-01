package nrs.nrsgui;

import java.awt.BasicStroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.Iterator;
import nrs.csl.CSL_Element_Interface;
import nrs.csl.CSL_Element_Variable;

/*
 * Provides methods for displaying a Node onto a canvas
 */
class GuiNode implements DisplayDelegate
{
  /** Dynamic bounds of the central node rectangle (ie, excluding the
   * pins). It is dynamic in the sense that the x and y fields of the
   * Rectangle contain the screen coordinates of the rectangle */
  private final Rectangle m_rect;

  /** The static bounds of the complete visible GuiNode. It is static in
   * the sense that coordinate values are given in terms of offsets from
   * the top-left corner of the node rectangle. Should only need to be
   * calculate when pins are added, hidden or shown. */
  private Rectangle m_staticBounds;

  /** List of the pins -  each object should be of type NetworkObjectPin */
  private ArrayList m_pins = new ArrayList();

  /** Offsets of the internal label. Should be set accoring to the font
   * in use */
  static int sm_labelOffsetX = 10;
  static int sm_labelOffsetY = 15;
  static boolean sm_labelVisible = true;

  /** Reference to the underlying node */
  private final Node m_node;

  /** Used for stacking of display objects one above the other */
  private int m_displayHeight = DisplayDelegate.NOHEIGHT;

  private int m_pinCount;

  private final Point m_iPin;
  private final Point m_oPin;

  private int m_pinRadius = 5;
  private int m_pinVertSeparation = 18;
  private int m_pinHorzSeparation = 18;

  public static final int GUTTER = 0;
  public static int PIN_VERT_LENGTH = 15;
  public static int PIN_HORZ_LENGTH = 15;

  // Spacings, for controling the layout.  These are
  // deliberately non-private so that they can be controlled from
  // outside.
  static int HORZ_MARGIN = 5;
  static int VERT_MARGIN = 5;

  private final int m_rectWidth = 100;
  private final int m_rectHeight = 50;

  /**
   * The margin is a minimum sized border that should exist around the
   * centre rectangle of the node. It causes the dynamic highlighting to
   * look better.
   */
  private final int m_margin = 5;

  /** Selected state */
  private boolean m_selected = false;

  static int GRID_SIZE = 15;
  private static TexturePaint m_texture;

  // _BUG_(These should be shared across instances, but how? The problem
  //       that GuiNodes may have a range of dimensions, so its not
  //       clear how they can be easily shared).
  private BufferedImage m_outSyncImage;
  private BufferedImage m_inSyncImage;

  //----------------------------------------------------------------------
  /**
   * Constructor. Accepts a reference to the {@link Node} which this
   * class will paint.
   */
  GuiNode(Node node, int x, int y)
  {
    m_node = node;
    m_rect = new Rectangle(x, y, m_rectWidth, m_rectHeight);

    //    m_pins.add(new NetworkObjectPin("pin", 10, 0, 10, -15, 7,
    //                                    Color.BLUE, true));
    //    m_pins.add(new NetworkObjectPin("pin", 30, 0, 30, -15, 7,
    //                                    Color.RED, true));
    //    m_pins.add(new NetworkObjectPin("pin", 0, 10, -15, 10, 7,
    //                                    Color.GREEN, true));

    m_iPin = new Point(0, m_rect.height - GUTTER);
    m_oPin = new Point(m_rect.width, GUTTER);

    addPins();
    updateStaticBounds();

    prepareOutSyncImage();
    prepareInSyncImage();
  }
  //----------------------------------------------------------------------
  private void addPins()
  {
    // Loop of each variable
    for (Iterator i = m_node.getType().getVariablesIterator();
         i.hasNext(); )
    {
      CSL_Element_Variable var = (CSL_Element_Variable) i.next();

      // Now loop over each interface of this variable
      for (Iterator j = var.getInterfaceIterator(); j.hasNext(); )
      {
        addPin((CSL_Element_Interface) j.next(), var);
      }
    }
  }
  //----------------------------------------------------------------------
  private Point getNextIPos(Point ps)
  {
    if (ps.y == 0)
    {
      // next pin is to the right, along top edge
      ps.x += m_pinHorzSeparation;
      ps.y = 0;
    }
    else
    {
      // next pin is above, along left side
      ps.x = 0;
      ps.y -= m_pinVertSeparation;

      if (ps.y < 0)
      {
        ps.x = -1 * ps.y;
        ps.y = 0;
      }
    }

    return ps;
  }
  //----------------------------------------------------------------------
  private Point getNextOPos(Point ps)
  {
    if (ps.y == m_rect.height)
    {
      // next pin is to the left
      ps.x -= m_pinHorzSeparation;
      ps.y = m_rect.height;
    }
    else
    {
      // next pin is below
      ps.x = m_rect.width;
      ps.y += m_pinVertSeparation;

      if (ps.y > m_rect.height)
      {
        ps.x = m_rect.width - (ps.y - m_rect.height);
        ps.y = m_rect.height;
      }
    }

    return ps;
  }
  //----------------------------------------------------------------------
  /*
   * Note, am using the argument-rebound idiom
   */
  private Point getPinTarget(Point ps,
                             Point pt,
                             CSL_Element_Interface intface)
  {
    if (pt == null) pt = new Point();

    if (intface.isIn()) /* if input pin */
    {
      if (ps.x == 0)
      {
        // pin goes left
        pt.x = ps.x - PIN_HORZ_LENGTH;
        pt.y = ps.y;
      }
      else
      {
        // pin goes up
        pt.x = ps.x;
        pt.y = pt.y - PIN_VERT_LENGTH;
      }
    }
    else if (intface.isOut())
    {
      if (ps.y == m_rect.height)
      {
        // pin does down
        pt.x = ps.x;
        pt.y = ps.y + PIN_VERT_LENGTH;
      }
      else
      {
        // pin goes right
        pt.x = ps.x + PIN_HORZ_LENGTH;
        pt.y = ps.y;
      }
    }

    return pt;
  }
  //----------------------------------------------------------------------
  private void addPin(CSL_Element_Interface intface, CSL_Element_Variable var)
  {
    Point pt = new Point();

    // Added 31 May 2004, so that when interfaces are drawn, those for
    // which max is 0 are not shown.
    if (intface.getMaxOccurs() == 0) return;

    if (intface.isIn())
    {
      getPinTarget(m_iPin, pt, intface);
      m_pins.add(new NetworkObjectPin("pin",
                                      m_iPin,pt,
                                      m_pinRadius,
                                      Color.GREEN, true));
      getNextIPos(m_iPin);
    }
    else if (intface.isOut())
    {
      getPinTarget(m_oPin, pt, intface);
      m_pins.add(new NetworkObjectPin("pin",
                                      m_oPin,pt,
                                      m_pinRadius,
                                      Color.GREEN, true));
      getNextOPos(m_oPin);
    }

    m_pinCount++;
  }
  //----------------------------------------------------------------------
  /*
   * Set display height at which an instance should be displayed. Node
   * with lower values are drawn first (and so appear at the bottom of a
   * group) while higher values are drawn last (and so appear at the top
   * of a group).
   */
  public void setDisplayHeight(int h)
  {
    m_displayHeight = h;
  }
  //----------------------------------------------------------------------
  /**
   * Return the name of the underlying node by calling its
   * <code>toString()</code> method
   */
  public String toString()
  {
    return m_node.toString();
  }
  //---------------------------------------------------------------------------
  /*
   * Implement the {@link DisplayDelegate} interface
   */
  public void paint(Graphics2D g2d)
  {
    // TODO - create the stroke object within the class
    g2d.setStroke(new BasicStroke(1.0f));

    if (m_node.inSync())
    {
      g2d.drawImage(m_inSyncImage, null, m_rect.x, m_rect.y);
    }
    else
    {
      g2d.drawImage(m_outSyncImage, null, m_rect.x, m_rect.y);
    }

    g2d.setPaint(Color.black);
    g2d.draw(m_rect);

    if (sm_labelVisible)
      {
        int y = m_rect.y + sm_labelOffsetY;
        g2d.drawString(m_node.toString(),
                       m_rect.x + sm_labelOffsetX,
                       y);

        y += g2d.getFontMetrics().getHeight();

        g2d.drawString(m_node.getType().toString(),
                       m_rect.x + sm_labelOffsetX,
                       y);
      }

    for (Iterator i = m_pins.iterator(); i.hasNext(); )
      {
        NetworkObjectPin pin = (NetworkObjectPin) i.next();
        pin.paint(g2d, x(), y());
      }

    if (m_selected)
    {
      Rectangle r = bounds(null);
      g2d.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 2,2);
    }
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
  //---------------------------------------------------------------------------
  /*
   * Implement the {@link DisplayDelegate} interface
   */
  public void paintSelected(Graphics2D g2d)
  {
    // THIS IS A HACK - REMOVE WHEN MULTIPLE SELECTIONS HAVE BEEN DESIGNED
    m_selected = true;
    paint(g2d);
    m_selected = false;
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
   * Return the underlying {@link Node} this class represents
   */
  Node getNode()
  {
    return m_node;
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
    //if (getBounds(null).contains(p)) return true;
    if (m_rect.contains(p)) return true;

    // check my pins also
    for (Iterator i = m_pins.iterator(); i.hasNext(); )
    {
      NetworkObjectPin pin = (NetworkObjectPin) i.next();
      if (pin.contains(p, x(), y())) return true;
    }

    return false;
  }
  //----------------------------------------------------------------------
  private void updateStaticBounds()
  {
    m_staticBounds = new Rectangle(0,0,m_rectWidth, m_rectHeight);

    // Now take into consideration any pins
    Rectangle temp = new Rectangle();
    for (Iterator i = m_pins.iterator(); i.hasNext(); )
    {
      NetworkObjectPin pin = (NetworkObjectPin) i.next();
      pin.getStaticBounds(temp);

      m_staticBounds = m_staticBounds.union(temp);
    }

    temp.x = -m_margin;
    temp.y = -m_margin;
    temp.width = m_rectWidth + m_margin*2;
    temp.height = m_rectHeight + m_margin*2;
    m_staticBounds = m_staticBounds.union(temp);
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link DisplayDelegate} interface
   */
  public Rectangle bounds(Rectangle r)
  {
    if (r == null) r = new Rectangle();


    r.x = m_rect.x + m_staticBounds.x - HORZ_MARGIN;
    r.y = m_rect.y + m_staticBounds.y - VERT_MARGIN;

    // +1 is needed, for some reason, because without it, the component leaves
    // a mess on the screen when it is dragged to the left
    r.width = m_staticBounds.width +1 + (2*HORZ_MARGIN);
    r.height = m_staticBounds.height+1 + (2*VERT_MARGIN);
    return r;
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link DisplayDelegate} interface
   *
   * @return the {@link Node} object self represents.
   */
  public Object getObject()
  {
    return m_node;
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
  }  //----------------------------------------------------------------------
  /**
   * Create a dotted-line pen
   */
  private Stroke createDottedLine()
  {
    float[] pattern = { 8.0f, 2.0f };
    Stroke s = new BasicStroke(1.0f,
                               BasicStroke.CAP_ROUND,
                               BasicStroke.JOIN_ROUND,
                               1.0f,
                               pattern,
                               0);

    return s;
  }
  //----------------------------------------------------------------------
  /**
   * Build the {@link TexturePaint} object to use for painting the the
   * node rectangles. This actually makes use of a static instance, so
   * that all instances of this class can reuse the same texture.
   */
  private TexturePaint createTexturePaint()
  {
    if (m_texture != null) return m_texture;

    // Make a smallish grid - use the integer to change the grid size
    // used for the texture paint. Currently I think 1 gives the
    // smoothest responses.
    int w = GRID_SIZE * 1;
    int h = GRID_SIZE * 1;

    // Make a grid with an alpha (transparancy) channel
    BufferedImage tile =
      new BufferedImage(w, h, BufferedImage.TYPE_INT_BGR);
    Graphics2D g2dGrid = tile.createGraphics();

    // Make the grid background transparent. Note, setting transparency
    // to 0 makes the colour completely transparent. Setting it to 255
    // makes is completely opaque.
    Color background = new Color(191, 127, 31);
    g2dGrid.setBackground(background);
    g2dGrid.clearRect(0,0,w,h);

    // Make the grid lines semi-opaque
    g2dGrid.setColor(background.darker());

    //    g2dGrid.setStroke(createDottedLine());

    // Now draw the grid lines
    g2dGrid.drawLine(w-1, 0, 0, h-1);

    m_texture = new TexturePaint(tile, new Rectangle(w,h));

    // Dispose of the context created locally. Here's what the Javadoc
    // says about dispose: "Graphics objects which are provided as
    // arguments to the paint and update methods of components are
    // automatically released by the system when those methods
    // return. For efficiency, programmers should call dispose when
    // finished using a Graphics object only if it was created directly
    // from a component or another Graphics object."
    g2dGrid.dispose();

    return m_texture;
  }
  //----------------------------------------------------------------------
  private void prepareOutSyncImage()
  {
    m_outSyncImage = new BufferedImage(m_rect.width,
                                m_rect.height,
                                BufferedImage.TYPE_INT_RGB);

    Graphics2D g2 = m_outSyncImage.createGraphics();

    g2.setPaint(createTexturePaint());
    g2.fill(m_rect);

    g2.dispose();
  }
  //----------------------------------------------------------------------
  private void prepareInSyncImage()
  {
    m_inSyncImage = new BufferedImage(m_rect.width,
                                      m_rect.height,
                                      BufferedImage.TYPE_INT_RGB);

    Graphics2D g2 = m_inSyncImage.createGraphics();

    g2.setBackground(new Color(191, 127, 31));
    g2.clearRect(0, 0, m_rect.width, m_rect.height);

    g2.dispose();
  }
}
