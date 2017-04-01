package nrs.nrsgui;

import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * Presents a view of a list of inter-node links.
 * @author Darren Smith
 */
class NLinkList extends JComponent implements MouseListener
{
  // List of the node-link components current in view, each object is of
  // type {@link NLinkControl}
  private ArrayList m_links;

  private NLinkControl m_selected = null;

  /** Effective boundary of the canvas - which is calculated from the
   * boundary needed to enclose all  objects */
  private Rectangle m_bounds;

  //----------------------------------------------------------------------
  /**
   * Constructor
   */
  NLinkList()
  {
    // Register event handlers
    addMouseListener(this);

    m_links = new ArrayList();

    m_bounds = new Rectangle(0,0, 0, 0);

    setBackground(Color.white);
    setMinimumSize(new Dimension(100, 300));
    setPreferredSize(new Dimension(100, 300));

    setOpaque(true);
  }
  //----------------------------------------------------------------------
  /**
   * Add a new {@link NLinkControl} object to the current view.
   */
  public void add(NLinkControl p_newLink)
  {
    p_newLink.setRowIsEven(m_links.size() % 2 == 0);
    m_links.add(p_newLink);

    repaint();
  }
  //----------------------------------------------------------------------
  /**
   * Remove all {@link NLinkControl} objects
   */
  public void removeAll()
  {
    m_links.clear();
    repaint();
    validateComponentSize();
  }
  //---------------------------------------------------------------------------
  /**
   * Override the Swing paintComponent method for custom painting
   */
  protected void paintComponent(Graphics p_g)
  {
    // Paint background if component is opaque
    if (isOpaque())
    {
      Insets insets = getInsets();
      int actualWidth = getWidth() - insets.left - insets.right;
      int actualHeight = getHeight() - insets.top - insets.bottom;

      p_g.setColor(getBackground());
      p_g.clearRect(insets.left, insets.right, actualWidth, actualHeight);
    }

    Graphics2D g2d = (Graphics2D) p_g;

    // Display and place components
    for (Iterator i = m_links.iterator(); i.hasNext(); )
    {
      NLinkControl nl = (NLinkControl) i.next();

      // If a component does not yet have a location, then it is placed at
      // the foot of this component.
      if (nl.getValid() == false)
      {
        nl.setLocation(0, m_bounds.height);
        nl.paint(g2d);
        validateComponentSize();
      }
      else
      {
        nl.paint(g2d);
      }
    }

    p_g.dispose();
  }
  //----------------------------------------------------------------------
  /**
   * Set the currently selected {@link NLinkControl} to the specified
   * object. Can be set to null.
   */
  private void setSelected(NLinkControl nlc)
  {
    if (m_selected == nlc) return;

    if (m_selected != null)
    {
      m_selected.setSelected(false);
      Rectangle clip = m_selected.bounds(null);
      clip.width = getWidth();
      repaint(clip);
    }

    m_selected = nlc;

    if (m_selected != null)
    {
      m_selected.setSelected(true);
      Rectangle clip = m_selected.bounds(null);
      clip.width = getWidth();
      repaint(clip);
    }
  }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when the mouse button
   * has been clicked (pressed and released) on a component.
   */
  public void mouseClicked(MouseEvent e)
  {
    //    PackageLogger.log.fine("mouseClicked");
    for (Iterator i = m_links.iterator(); i.hasNext(); )
    {
      NLinkControl nl = (NLinkControl) i.next();

      if (nl.contains(e.getPoint()))
      {
        nl.handleMouseEvent(e);
        if (nl != m_selected) setSelected(nl);
        return;
      }
    }
  }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when a mouse button
   * has been pressed on a component.
   */
  public void mousePressed(MouseEvent e) { }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface.Invoked when a mouse button has
   * been released on a component.
   */
  public void mouseReleased(MouseEvent e) { }
  //----------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when the mouse enters
   * a component.
   */
  public void mouseEntered(MouseEvent e) { }
  //----------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when the mouse exits a
   * component.
   */
  public void mouseExited(MouseEvent e) { }
  //----------------------------------------------------------------------
  /**
   * Calculate and return the enclosing bounds of all objects contained
   * within this component. Returns a new {@link Rectangle} if
   * <code>r</code> is null, otherwise places calculated value into
   * <code>r</code>.
   */
  private Rectangle calcBounds(Rectangle r)
  {
    Rectangle retVal = new Rectangle();

    for (Iterator i = m_links.iterator(); i.hasNext();)
    {
      NLinkControl nlc = (NLinkControl) i.next();

      // We only want to base our calculation on currently valid components.
      if (nlc.getValid())
        retVal = retVal.union(nlc.bounds(null));
    }

    if (r != null) r.setBounds(retVal);

    return retVal;
  }
  //----------------------------------------------------------------------
  /**
   * Calculate the size of this component (using {@link #calcBounds})
   * and resize if it has changed.
   */
  private void validateComponentSize()
  {
    Rectangle newBounds = calcBounds(null);

    if (newBounds.equals(m_bounds))
    {
      // do nothing
    }
    else
    {
      // bounds have changed
      m_bounds = newBounds;

      int width = m_bounds.width;
      int height = m_bounds.height;

      setPreferredSize(new Dimension(width, height));

      // Let any scroll bars know they should update themselves - ie,
      // Swing does this canvas resizing automatically
      revalidate();
    }
  }
}
