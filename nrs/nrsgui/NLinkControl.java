 package nrs.nrsgui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import javax.swing.JComponent;


/**
 * Draw an interactive link control, for use with the {@link NLinkList}
 * component.
 */
class NLinkControl implements DisplayDelegate
{
  // Link self represents
  private Link m_link;

  /** Dynamic bounds, in the sense that the x and y fields of the
   * Rectangle contain the screen coordinates of the rectangle */
  private Rectangle m_rect;

  /** Text description of the link */
  private String m_name;

  /** Determines whether background is shaded */
  private boolean m_isEven = true;

  /** Is this object selected? */
  private boolean m_highlighted = false;

  // Graphical parent
  private final NLinkList m_linkViewer;

  /** Location where label is displayed, relative to internal origin */
  private final Point m_labelPos = new Point(0,0);

  /**
   * Bounding box of the text label, with respect to, the internal origin
   */
  private Rectangle m_label = new Rectangle();

  /**
   * Bounding box of the icon, relative to the internal origin
   */
  private Rectangle m_icon = new Rectangle();

  /**
   * Bounding box around the icon, but extended in size to make
   */
  private Rectangle m_iconHitRegion = new Rectangle();

  private static BasicStroke m_stroke = new BasicStroke(1.0f);

  /** Component on which this is displayed */
  private JComponent m_owner;

  /* ----- Layout constants ----- */

  // Buffer size
  public static int VBUFFER = 7;
  public static int HBUFFER = 7;

  // Space between icon and textual description
  public static int HSPACE1 = 10;

  // Colors
  private static final Color SHADED = new Color(224, 224, 224);
  private static final Color SELECTED = Color.blue;
  private static final Color EXISTS = Color.green;
  private static final Color DOESNT_EXIST = Color.black;

  //////////////////////////////////////////////////////////////////

  // Offsets to use for the string segment
  static final int YBUFFER = 6;
  static final int XBUFFER = 10;
  static final int VSCROLL_UNIT_INCR = 5;
  static final int VSCROLL_BLOCK_INCR = 5;
  private int m_fontAscent;

  // Indicate if its location is valid. Initially this is set to false,
  // because the component has not been placed at a particular location
  // on the canvas.
  private boolean m_valid = false;

  //----------------------------------------------------------------------
  /**
   * Construct an instance to represent the given {@link Link}
   */
  public NLinkControl(Link aLink,
                      NLinkList aParent,
                      JComponent aOwner)
  {
    // parameter validation
    if (aLink == null)
      throw new NullPointerException("Link is null");
    if (aParent == null)
      throw new NullPointerException("Must specify NLinkList parent");
    if (aOwner == null)
      throw new NullPointerException("Must specify JComponent owner");

    m_rect = new Rectangle(0,0,0,0);
    m_linkViewer = aParent;
    m_link = aLink;
    m_owner = aOwner;

    m_name = m_link.getSource().toString() +
      " to " +
      m_link.getTarget().toString();

    layout(null);
  }
  //----------------------------------------------------------------------
  /**
   * Paint the component
   */
  public void paint(Graphics2D g2d)
  {
    Image linkIcon;
    Color color;

    if (m_link.getExists())
    {
      linkIcon = IconManager.getInstance().link().getImage();
      color = EXISTS;
    }
    else
    {
      linkIcon = IconManager.getInstance().unlink().getImage();
      color = DOESNT_EXIST;
    }

    g2d.setStroke(m_stroke);

    // extend the potential clip region to the entire window width
    Rectangle clip = new Rectangle(m_rect);
    if (clip.getWidth() < m_linkViewer.getWidth())
      clip.width = m_linkViewer.getWidth();

    clip = clip.intersection(g2d.getClipBounds());
    if (m_isEven)
    {
      g2d.setColor(Color.white);
    }
    else
    {
      g2d.setColor(SHADED);
    }
    // NOTE: have disabelled link highlighting for now, since it did not
    // correspond to any functionality present or planned
    //    if (m_highlighted) g2d.setColor(SELECTED);

    g2d.fill(clip);

    g2d.setPaint(Color.black);

    g2d.drawImage(linkIcon,
                  m_rect.x + m_icon.x,
                  m_rect.y + m_icon.y,
                  (ImageObserver) null);

    g2d.setColor(color);
    g2d.drawString(m_name,
                   m_rect.x + m_labelPos.x,
                   m_rect.y + m_labelPos.y);
  }
  //----------------------------------------------------------------------
  /**
   * There is currently no requirment to paint {@link NLinkControl}
   * components that are selected. Calling this method will just
   * delegate to the {@link #paint(Graphics2D g2d)} method.
   */
  public void paintSelected(Graphics2D g2d)
  {
    paint(g2d);
  }
  //----------------------------------------------------------------------
  /**
   * Always returns zero, since this method is not really appropriate
   * for {@link NLinkControl}.
   */
  public int getDisplayHeight()
  {
    return 0;
  }
  //----------------------------------------------------------------------
  /*
   * Call has no effect, since this method is not really appropriate for
   * {@link NLinkControl}.
   */
  public void setDisplayHeight(int h)
  {
  }
  //----------------------------------------------------------------------
  /**
   * Returns true if the {@link Point} is contained within self
   */
  public boolean contains(Point e)
  {
    return m_rect.contains(e);
  }
  //----------------------------------------------------------------------
  /**
   * Implement the {@link DisplayDelegate} interface
   */
  public void setLocation(int x, int y)
  {
    m_rect.x = x;
    m_rect.y = y;
    m_valid = true;
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
  /**
   * Implements the {@link DisplayDelegate} interface
   */
  public Rectangle bounds(Rectangle r)
  {
    if (r == null) r = new Rectangle();

    r.x = m_rect.x;
    r.y = m_rect.y;
    r.width = m_rect.width;
    r.height = m_rect.height;

    return r;
  }
  //----------------------------------------------------------------------
  /**
   * Return the object which this {@link DisplayDelegate} graphically
   * represents.
   */
  public Object getObject()
  {
    return null; // todo
  }
  //----------------------------------------------------------------------
  /**
   * Indicate whether self is on an even numbered row (true) or an odd
   * numbered row (false).
   */
  void setRowIsEven(boolean b)
  {
    if (b != m_isEven)
    {
      m_isEven = b;
    }
  }
  //----------------------------------------------------------------------
  private void layout(Graphics g)
  {
    if (g == null) g = m_owner.getGraphics();

    FontMetrics fm = g.getFontMetrics();

    // Arrange the label's vertical alignment
    m_label.y = VBUFFER;
    m_labelPos.y = m_label.y + fm.getMaxAscent();
    m_label.height = fm.getHeight();
    m_label.width = (int) g.getFontMetrics().stringWidth(m_name);

    // Arrange the link icon layout
    m_icon.width = IconManager.getInstance().link().getIconWidth();
    m_icon.height = IconManager.getInstance().link().getIconHeight();
    m_icon.x = HBUFFER;

    // Arrange label's horizontal layout
    m_label.x = m_icon.x + m_icon.width + HSPACE1;
    m_labelPos.x = m_label.x;

    // Set size of bounding box
    m_rect.width = HBUFFER * 2 + m_icon.width + HSPACE1 + m_label.width;
    m_rect.height = VBUFFER * 2 + m_label.height;

    // ...assumes icon is smaller than text - so we now centre it
    m_icon.y = (m_rect.height - m_icon.height) / 2;

    // deteremine the hit region
    m_iconHitRegion.x = 0;
    m_iconHitRegion.y = 0;
    m_iconHitRegion.height = m_rect.height;
    m_iconHitRegion.width = m_label.x;
  }
  //----------------------------------------------------------------------
  /**
   * Returns the status of the components location. Returns true if the
   * component has a location, false otherwise
   */
  public boolean getValid()
  {
    return m_valid;
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link DisplayDelegate} interface
   */
  public void setSelected(boolean b)
  {
    m_highlighted = b;
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link DisplayDelegate} interface
   */
  public boolean getSelected()
  {
    return  m_highlighted;
  }
  //----------------------------------------------------------------------
  /**
   * Pass a mouse event to this component. This component will attempt
   * to determine what effect, is any, the mouse event has on the link.
   */
  public void handleMouseEvent(MouseEvent e)
  {
    Point p = new Point(e.getX() - x(),
                        e.getY() - y());

    if (m_iconHitRegion.contains(p))
    {
      if (m_link.getExists() == false)
      {
        AppManager.getInstance().accessLinkManager().makeLink(m_link);
      }
      else
      {
        AppManager.getInstance().accessLinkManager().removeLink(m_link);
      }
      m_owner.repaint(bounds(null));
    }
  }
}
