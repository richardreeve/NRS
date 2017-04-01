package nrs.nrsgui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.swing.DebugGraphics;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JViewport;

/**
 * This is the starting point for a gui element which will display and
 * allow manipulation of neurons, synapses etc
 * @author  Darren Smith
 */
class NetworkCanvas extends JComponent
  implements MouseListener, MouseMotionListener, ComponentListener
{
  // When true, this indicates that a user has depressed the mouse
  // button over a selection and started dragging the mouse
  boolean m_selectedDragged = false;

  /** List of graphical objects displayed on the canvas. Each object is
   * of type {@link DisplayDelegate}. The objects are painted onto the
   * display in the order in which they appear in this list. */
  LinkedList m_objects;
  NetworkEventsListener m_eventHandler;

  Stroke m_defaultStroke = new BasicStroke(2.0f);
  Stroke m_selectStroke  = new BasicStroke(1.0f);

  // Object which is currently selected. Can be null. TODO: change this
  // into an ArrayList when I wish to allow mutiple selections.
  DisplayDelegate m_selected;

  /** Store the position of the mouse when a mouseMoved operation began.
    * The coordinate is relative to the origin of the network object
    * which was directly under the mouse pointer when the button was
    * pressed. */
  int m_dragStartX, m_dragStartY;
  Rectangle m_lastBounds;

  /** Data context - ie the node and network currently in view */
  NetworkCanvasDataContext m_ctx;

  /** Link to parent frame */
  private final JFrame m_parent;

  /** Effective boundary of the canvas - which is calculated from the
   * boundary needed to enclose all network objects */
  private Rectangle m_bounds;

  private static final Rectangle m_defaultBounds = new Rectangle(0,0,200,200);

  private static final int BOUND_GUTTER = 0;

  /** The object which is the currently dynamically-highlighted. Ie, the
    * object which will be selected next if the user were to click the
    * mouse. */
  private DisplayDelegate m_dynHighlight;

  // Color settings are non-private to allow outside control
  static Color DYNAMIC_HIGHLIGHT_COLOR = new Color(180,180,255,210);

  // Used for various temporary calculates during rendering, to avoid
  // object creation.
  private Rectangle m_tempR = new Rectangle();

  static int GRID_SIZE = 20;

  private TexturePaint m_texture;
  static private TexturePaint m_disabledTexture;
  static private TexturePaint m_enabledTexture;

  //---------------------------------------------------------------------------
  /** Constructor */
  public NetworkCanvas(NetworkEventsListener eventHandler,
                       JFrame parent,
                       NetworkCanvasDataContext context)
  {
    m_eventHandler = eventHandler;
    m_parent = parent;
    m_ctx = context;

    // Register event handlers
    addMouseMotionListener(this);
    addMouseListener(this);

    m_objects = new LinkedList();

    m_selected = null;

    m_bounds = new Rectangle(m_defaultBounds);
    m_bounds = new Rectangle(calcNetworkBounds(null));
    initTexturePaint();
    initDisabledTexturePaint();
    m_texture = m_disabledTexture;
    setBackground(Color.white);
    setMinimumSize(m_bounds.getSize());
    setPreferredSize(m_bounds.getSize());

    setOpaque(true);

    addComponentListener(this);
  }
  //----------------------------------------------------------------------
  //  /**
  //   * Set the scroll bar which provides user scrolling control over the
  //   * pane in which this {@link NetworkCanvas} object appears. This class
  //   * needs this access because it may need to change the bounds on the
  //   * {@link JScrollBar}.
  //   */
  //  void setHScrollBar(JScrollBar vsb)
  //  {
  //    m_vsb = vsb;
  //  }
  //----------------------------------------------------------------------
  /**
   * Assemble the internal list of objects to display from the list of
   * objects in <code>dds</code>, taking into account the display height
   * of each object.  Each object in the collection should be of type
   * {@link DisplayDelegate}.
   */
  private void buildDisplayList(Collection dds)
  {
    // Clear the preset contents of the display list. Better than
    // constructing new list (and forgetting the old). Record this as a
    // useful programming idiom.
    m_objects.clear();

    // defensive
    if (dds == null) return;

    // Take each component from the source list in turn, and then work
    // out where to insert it into the display list, so that components
    // with the largest display height are towards the head of the
    // display list, and those with lowest height are toward the tail
    // (since the list is displayed in reverse order).
    for (Iterator i = dds.iterator(); i.hasNext(); )
    {
      DisplayDelegate nextDD = (DisplayDelegate) i.next();
      int sourceHeight = nextDD.getDisplayHeight();

      // Handle those objects that have no height
      if (sourceHeight == DisplayDelegate.NOHEIGHT)
      {
        nextDD.setDisplayHeight(1); // put at bottom
        sourceHeight = nextDD.getDisplayHeight();
      }

      // default is to insert at head of list
      int insertIndex = 0;

      for (Iterator j = m_objects.iterator(); j.hasNext(); )
      {
        DisplayDelegate listMember = (DisplayDelegate) j.next();

        // if object to insert is highter than next in list, then insert
        // ahead of the next in list
        if (sourceHeight > listMember.getDisplayHeight())
        {
          insertIndex = m_objects.indexOf(listMember);
          System.out.println("inserting "
                             + sourceHeight
                             + " before "
                             + listMember.getDisplayHeight());
          break;
        }
        else
        {
          // ...make sure we don't insert before next in list (as it is
          // higher than, or equal to, our new-object).
          insertIndex = m_objects.indexOf(listMember) + 1;
        }
      }

      m_objects.add(insertIndex, nextDD);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Provide the collection of simulation objects which should be
   * displayed in the current view. Each object in the collection should
   * be of type {@link DisplayDelegate}.
   */
  void setCollection(Collection dds)
  {
    // Extract the new list of painter objects
    buildDisplayList(dds);

    // Since the collection has now changed, recheck if the node current
    // indicated as selected exists in the new collection.
    if (m_selected != null)
    {
      if (!m_objects.contains(m_selected))   // TODO - broke, needs updating
      {
        m_selected = null;
      }
    }

    validateCanvasSize();
    repaint();
  }
  //----------------------------------------------------------------------
  /**
   * Build the {@link TexturePaint} object to use for painting the
   * grid when no network is present.
   */
  private void initDisabledTexturePaint()
  {
    // Make a smallish grid - use the integer to change the grid size
    // used for the texture paint. Currently I think 1 gives the
    // smoothest responses.
    int w = GRID_SIZE * 1;
    int h = GRID_SIZE * 1;

    // Make a grid with an alpha (transparancy) channel
    BufferedImage tile = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g2dGrid = tile.createGraphics();

    g2dGrid.setBackground(new Color(224,224,224));
    g2dGrid.clearRect(0,0,w,h);
    g2dGrid.setColor(new Color(200, 200, 200));

    // Now draw the grid lines
    for (int x = 0; x < w; x += GRID_SIZE) g2dGrid.drawLine(x, 0, x, h);
    for (int y = 0; y < h; y += GRID_SIZE) g2dGrid.drawLine(0, y, w, y);

    m_disabledTexture = new TexturePaint(tile, new Rectangle(w,h));

    // Dispose of the context created locally. Here's what the Javadoc
    // says about dispose: "Graphics objects which are provided as
    // arguments to the paint and update methods of components are
    // automatically released by the system when those methods
    // return. For efficiency, programmers should call dispose when
    // finished using a Graphics object only if it was created directly
    // from a component or another Graphics object."
    g2dGrid.dispose();
  }
  //----------------------------------------------------------------------
  /**
   * Build the {@link TexturePaint} object to use for painting the
   * grid. Previously I have tried using a {@link BufferedImage} of the
   * entire grid, which was used to get subImages for drawing onto the
   * screen. However using the {@link TexturePaint} method seems to give
   * the smoothest results, especially for large canvases.
   */
  private void initTexturePaint()
  {
    // Make a smallish grid - use the integer to change the grid size
    // used for the texture paint. Currently I think 1 gives the
    // smoothest responses.
    int w = GRID_SIZE * 1;
    int h = GRID_SIZE * 1;

    // Make a grid with an alpha (transparancy) channel
    BufferedImage tile = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
    Graphics2D g2dGrid = tile.createGraphics();

    // Make the grid background transparent. Note, setting transparency
    // to 0 makes the colour completely transparent. Setting it to 255
    // makes is completely opaque.
    g2dGrid.setBackground(new Color(255,255,255));
    g2dGrid.clearRect(0,0,w,h);

    // Make the grid lines semi-opaque
    g2dGrid.setColor(new Color(192,192,192));

    // Now draw the grid lines
    for (int x = 0; x < w; x += GRID_SIZE) g2dGrid.drawLine(x, 0, x, h);
    for (int y = 0; y < h; y += GRID_SIZE) g2dGrid.drawLine(0, y, w, y);

    m_enabledTexture = new TexturePaint(tile, new Rectangle(w,h));

    // Dispose of the context created locally. Here's what the Javadoc
    // says about dispose: "Graphics objects which are provided as
    // arguments to the paint and update methods of components are
    // automatically released by the system when those methods
    // return. For efficiency, programmers should call dispose when
    // finished using a Graphics object only if it was created directly
    // from a component or another Graphics object."
    g2dGrid.dispose();
  }
  //----------------------------------------------------------------------
//   /**
//    * In order to optimise the drawing of the grid, the grid is first
//    * drawn onto an off-screen buffer (using a BufferedImage). During
//    * subsequent JPanel repaints, the grid is then draw by drawing the
//    * off-screen buffer onto the JPanel, at an appropriate location.
//    *
//    * @deprecated Now using the TexturedPaint approach
//    */
//   private void drawOffScreenGrid()
//   {
//     if (true) return;
//     // make grid larger than needed, in order to avoid too many redraws
//     // as user expands panel size
//     int w = (int) (getWidth() * 1.20);
//     int h = (int) (getHeight() * 1.20);

//     // Make a grid with an alpha (transparancy) channel
//     m_grid = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
//     Graphics2D g2dGrid = m_grid.createGraphics();

//     // Make the grid background transparent. Note, setting transparency
//     // to 0 makes the colour completely transparent. Setting it to 255
//     // makes is completely opaque.
//     g2dGrid.setBackground(new Color(255,255,255,255));
//     g2dGrid.clearRect(0,0,w,h);

//     // Make the grid lines semi-opaque
//     g2dGrid.setColor(new Color(200,200,200,150));

//     // Now draw the grid lines
//     for (int x = 0; x < w; x += GRID_SIZE)
//     {
//       g2dGrid.drawLine(x, 0, x, h);
//     }

//     for (int y = 0; y < h; y += GRID_SIZE)
//     {
//       g2dGrid.drawLine(0, y, w, y);
//     }

//     // Dispose of the context created locally. Here's what the Javadoc
//     // says about dispose: "Graphics objects which are provided as
//     // arguments to the paint and update methods of components are
//     // automatically released by the system when those methods
//     // return. For efficiency, programmers should call dispose when
//     // finished using a Graphics object only if it was created directly
//     // from a component or another Graphics object."
//     g2dGrid.dispose();
//   }
  //----------------------------------------------------------------------
//   /**
//    * @deprecated Now using the TexturedPaint approach
//    */
//   private void drawGrid(Graphics2D g2)
//   {
//     if (m_grid == null) drawOffScreenGrid();

//     // warning, m_tempR is volatile, so use it or lose it!
//     m_tempR = g2.getClipBounds(m_tempR);

//     // P is the upper-left corner of the clip
//     Point P = m_tempR.getLocation();

//     Point D = new Point(P.x - P.x % GRID_SIZE,
//                         P.y - P.y % GRID_SIZE);

//     /* Explanation
//        -----------

//                    012345678901234567890

//                 0  T----D----T----T----T--
//                 1  |    |    |    |    |
//                 2  |    |    |    |    |
//                 3  |    | P..|....|....|..
//                 4  |    | ...|....|....|..
//                 5  +----+----|----+----+--
//                 6  |    | ...|....|....|..
//                 7  |    | ...|....|....|..
//                 8  |    | ...|....|....|..
//                 9  |    | ...|....|....|..
//                10  +----+----+----+----+--
//                11  |    | ...|....|....|..


//        In the above diagram, the dotted region represents the clip
//        rectangle of Graphics object, which is the area of the JPanel
//        requiring painting. The overlaid grid is the visible grid that
//        appears on the screen. As show, the problem is to work out where
//        to start drawing the fixed BufferredImage grid.

//        P is the upper-left corner of the clip rectangle.

//        To ensure the grid printed into the clip region matches up with
//        the existing visible grid outside the clip region the following
//        strategy is adopted. The bufferred grid image will only be draw
//        at a location which is the intersection of X and Y
//        gridlines. I.e, the "T" and "+" pixels in the above
//        diagram. Thus, given P, the location of the clip, the task is to
//        work out D, the destination to which the buffered grid will be
//        drawn. Looking at the diagram, we see it is simply the
//        intersection of the next left, and next upper gridlines.
//     */

//     // Obtain a subImage which very closely (although not exactly)
//     // matches the clip region. Note this comment from the Javadoc for
//     // getSubimage(...): "Returns a subimage defined by a specified
//     // rectangular region. The returned BufferedImage shares the same
//     // data array as the original image."
//     BufferedImage subImage = m_grid.getSubimage(0,0,
//                                                 m_tempR.width + GRID_SIZE,
//                                                 m_tempR.height + GRID_SIZE);
//     g2.drawImage(subImage, null, D.x, D.y);
//   }
  //---------------------------------------------------------------------------
  /** Override the Swing paintComponent method for custom painting */
  protected void paintComponent(Graphics g)
  {
    /* WHY WAS I CREATING MY OWN GRAPHICS CONTEXT?
    // Create a copy of the graphics context, to avoid changing the context
    // received.
    Graphics2D g2 = (Graphics2D) g.create();
    */
    Graphics2D g2 = (Graphics2D) g;

    //System.out.println("repaint needed for " + g2.getClipBounds());

    // Paint background if component is opaque
    if (isOpaque())
    {
      /*
        Insets insets = getInsets();
        int actualWidth = getWidth() - insets.left - insets.right;
        int actualHeight = getHeight() - insets.top - insets.bottom;

        g.setColor(getBackground());

        System.out.println("background is being repainted : "
        + "left=" + insets.left
        + ", right=" + insets.right
        + ", width=" + actualWidth
        + ", height=" + actualHeight);
        g.clearRect(insets.left, insets.right, actualWidth, actualHeight);
      */

      g2.setPaint(m_texture);
      g2.fillRect(g.getClipBounds().x, g.getClipBounds().y,
                   g2.getClipBounds().width, g.getClipBounds().height);
    }


    /*
      _TODO_(Allow user to control antialiasing vai GUI option)
    */

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);


    if (m_dynHighlight != null)
    {
      m_dynHighlight.bounds(m_tempR);
      if (g2.hitClip(m_tempR.x, m_tempR.y, m_tempR.width, m_tempR.height))
      {
        g2.setPaint(DYNAMIC_HIGHLIGHT_COLOR);
        g2.fill(m_dynHighlight.bounds(null));
      }
    }

    // Display objects in reverse order
    DisplayDelegate networkObject;
    for (ListIterator i= m_objects.listIterator(m_objects.size());
         i.hasPrevious();)
    {
      networkObject = (DisplayDelegate) i.previous();

      networkObject.bounds(m_tempR);
      if (g2.hitClip(m_tempR.x, m_tempR.y, m_tempR.width, m_tempR.height))
      {
        networkObject.paint(g2);
      }
    }

    if (m_selected != null)
    {
      m_selected.bounds(m_tempR);
      if (g2.hitClip(m_tempR.x, m_tempR.y, m_tempR.width, m_tempR.height))
      {
        g2.setStroke(m_selectStroke);
        m_selected.paintSelected(g2);
        g2.setStroke(m_defaultStroke);
      }
    }

    /* WHY WAS I CREATING MY OWN GRAPHICS CONTEXT?
    // Need to do clean up because g2 was created in this method.
    g2.dispose();
    */
  }
  //---------------------------------------------------------------------------
  /**
   * Return the display delegate object which is directly underneath the
   * given mouse position. This method returns <code>null</code> if
   * there is no object under the mouse pointer.
   */
  public DisplayDelegate getObjectUnderMouse(Point p)
  {
    // First look for direct object hits - ie mouse is over part of the
    // visible object
    for (Iterator i = m_objects.iterator(); i.hasNext();)
    {
      DisplayDelegate dd = (DisplayDelegate) i.next();
      if (dd.contains(p)) return dd;
    }

    // If we failed to find an object under the mouse, return the object
    // which is dynamically highlighed
    if (m_dynHighlight != null) return m_dynHighlight;

    // Default case
    return null;
  }
  //---------------------------------------------------------------------------
  /** Required by MouseMotionListener interface. Invoked when a mouse
   *  button is pressed on a component and then dragged.
   */
  public void mouseDragged(MouseEvent e)
  {
    if ((m_selected != null) && (m_selectedDragged))
    {
      // Attempt to clear old position. Note how, in this function, we
      // use the getSelectedBounds(...) function, since the object being
      // dragged will also be selected
      Rectangle temp = m_selected.bounds(null); // was getSelectedBounds
      repaint(temp);

      // Determine if the top-left corner of the bounding box for
      // currently selected node lies within the top-left canvas
      // boundary
      temp = new Rectangle();
      m_selected.bounds(temp);

      int targetX = e.getX() - m_dragStartX ;
      int targetY = e.getY() - m_dragStartY;

      // clip to the top-left 0,0 boundary


      if (targetX + temp.x < 0) targetX = -temp.x;
      if (targetY + temp.y < 0) targetY = -temp.y;

      m_selected.setLocation(targetX, targetY);

      // do we need to resize the canvas?
      validateCanvasSize();

      m_selected.bounds(temp); // was getSelectedBounds
      scrollRectToVisible(temp);
      repaint(temp);
    }
  }
  //---------------------------------------------------------------------------
  /**
   * Required by MouseMotionListener interface. Invoked when the mouse
   * cursor has been moved onto a component but no buttons have been
   * pushed.
   */
  public void mouseMoved(MouseEvent e)
  {
    DisplayDelegate newHighlight = null;

    Rectangle tmpR = new Rectangle();

    for (Iterator i = m_objects.iterator(); i.hasNext();)
    {
      DisplayDelegate dd = (DisplayDelegate) i.next();
      dd.bounds(tmpR);

      if (tmpR.contains(e.getPoint()))
      {
        newHighlight = dd;
        break;
      }
    }

    if (newHighlight != m_dynHighlight)
    {
      if (m_dynHighlight != null)
      {
        m_dynHighlight.bounds(tmpR);
        repaint(tmpR);
      }
      if (newHighlight != null)
      {
        newHighlight.bounds(tmpR);
        repaint(tmpR);
      }
      m_dynHighlight = newHighlight;
    }
  }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when the mouse button
   * has been clicked (pressed and released) on a component.
   */
  public void mouseClicked(MouseEvent e)
  {
    DisplayDelegate hitDD = getObjectUnderMouse(e.getPoint());

    // If the event is the popup trigger, then don't process. Let
    // another handler deal with it.
    if (e.isPopupTrigger()) raisePopupEvent(hitDD, e);

    // If event is a double click, then treat this as a request to open
    // a node. We also cause the current node to be selected by dropping
    // down into the remainder of this routine.
    if (((e.getClickCount() == 2) || (e.getButton() == MouseEvent.BUTTON2))
        && (hitDD != null))
    {
      if (hitDD.getObject() instanceof Node)
      {
        m_eventHandler.openNode((Node) hitDD.getObject());
      }
    }
    else if ((e.getClickCount() == 1) && (m_selected != null))
    {
      raiseObjectSelectedEvent();
    }

//     THIS METHOD NO LONGER INVOKES A NODE SELECTION CHANGE, SINCE
//     WHENEVER JAVA INITIATES A mouseClicked EVENT, A mousePressed
//     EVENT WILL HAVE PRECEEDED IT WHICH ITSELF CAUSES A NODE SELECTION
//     CHANGE

//     // Now check if the first mouse button was pressed. If it was then
//     // the selection might have changed.
//     if (e.getButton() == MouseEvent.BUTTON1)
//     {
//       // Default case of no significant selection change - i.e., no change
//       // between the previous selection and the potential next
//       // selection. This often occurs because the mousePressed(...) event
//       // handler has acheived the appropriate selection change. However
//       // that method doesn't handle the case of a deselection.
//       if (hitDD != m_selected)
//       {
//         m_selected = hitDD;
//         repaint();
//       }

//       raiseObjectSelectedEvent();
//     }
  }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when a mouse button
   * has been pressed on a component.
   */
  public void mousePressed(MouseEvent e)
  {
    // Has the button been pressed over an object?
    DisplayDelegate hitDD = getObjectUnderMouse(e.getPoint());
    if (hitDD != null)
      {
        // This now checks that the button pressed is the first mouse
        // button. Other roles are assigned to the third button.
        if (e.getButton() == MouseEvent.BUTTON1)
        {
          // Set to true to indicate that the user maybe about to drag a
          // selection
          m_selectedDragged = true;

          m_dragStartX = e.getX() - hitDD.x();
          m_dragStartY = e.getY() - hitDD.y();

          // Has the selection changed
          if (hitDD != m_selected)
          {
            DisplayDelegate prevSelection = m_selected;
            m_selected = hitDD;

            if (prevSelection != null)
            {
              prevSelection.bounds(m_tempR); // was getSelectedBounds
              repaint(m_tempR);
            }
            if (m_selected != null)
            {
              m_selected.bounds(m_tempR); // was getSelectedBounds
              repaint(m_tempR);
            }
          }
          //raiseObjectSelectedEvent();
        }
      }
    else
      {
        m_selectedDragged = false;
      }

    // This is a hack. If the event is the popup trigger, then don't
    // process. Let another handler deal with it.
    if (e.isPopupTrigger()) raisePopupEvent(hitDD, e);
  }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface.Invoked when a mouse button has
   * been released on a component.
   */
  public void mouseReleased(MouseEvent e)
  {
    // The object which, potentially, is underneath the mouse
    DisplayDelegate hitDD = getObjectUnderMouse(e.getPoint());
    if (hitDD == null) return;

    // If the event is the popup trigger, then don't process. Let
    // another handler deal with it.
    if (e.isPopupTrigger()) raisePopupEvent(hitDD, e);
  }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when the mouse enters
   * a component.
   */
  public void mouseEntered(MouseEvent e) { }
  //---------------------------------------------------------------------------
  /* Required by MouseListener interface. Invoked when the mouse exits a
   * component.
   */
  public void mouseExited(MouseEvent e) { }
  //----------------------------------------------------------------------
  /**
   * Transform the network object coordinates (which permit X and Y to
   * take on negative values) to a zero based framework.
   */
  //  void Point zero(Point p)
  ///  {
  //    Rectangle bounds = calcNetworkBounds(null);
  //
  //    p.x -= bounds.x;
  //    p.y -= bounds.y;
  //  }
  //---------------------------------------------------------------------------
  /**
   * Return the bounds of the network
   */
  private Rectangle calcNetworkBounds(Rectangle r)
  {
    Rectangle retVal = new Rectangle(m_bounds);

    for (Iterator i = m_objects.iterator(); i.hasNext();)
    {
      DisplayDelegate gn = (DisplayDelegate) i.next();
      retVal = retVal.union(gn.bounds(null));
    }

    // Add on a fixed size gutter
    retVal.x -= BOUND_GUTTER;
    retVal.y -= BOUND_GUTTER;
    retVal.width += 2 * BOUND_GUTTER;
    retVal.height += 2 * BOUND_GUTTER;

    if (r == null)
    {
      return retVal;
    }
    else
    {
      r.setBounds(retVal);
      return r;
    }
  }
  //---------------------------------------------------------------------------
  /**
   * Principle method for coordinating change to the canvas size if and
   * when network objects lie outside the scrollpane viewport
   */
  private void validateCanvasSize()
  {
    Rectangle newBounds = calcNetworkBounds(null);

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

      if (m_bounds.x < 0) width -= m_bounds.x;
      if (m_bounds.y < 0) height -= m_bounds.y;

      setPreferredSize(new Dimension(width, height));


      //      if (m_vsb != null) m_vsb.setMinimum(m_bounds.x);

      // Let any scroll bars know they should update themselves - ie,
      // Swing does this canvas resizing automatically
      revalidate();
    }
  }
  //---------------------------------------------------------------------------
  /**
   * Implement {@link ComponentListener} interface.  Currently does nothing.
   */
  public void componentHidden(ComponentEvent e) { }
  //---------------------------------------------------------------------------
  /**
   * Implement {@link ComponentListener} interface.  Currently does nothing.
   */
  public void componentMoved(ComponentEvent e) { }
  //---------------------------------------------------------------------------
  /**
   * Implement {@link ComponentListener} interface. Causes the off
   * screen grid to be redrawn (if the component has enlarged).
   */
  public void componentResized(ComponentEvent e)
  {
//     // If there is an existing grid, then only make a new grid if the
//     // component has grown too larger in either X or Y.
//     if ((m_grid != null)
//         && ((m_grid.getWidth() < this.getWidth())
//             || (m_grid.getHeight() < this.getHeight())))
//     {
//       drawOffScreenGrid();
//     }
  }
  //---------------------------------------------------------------------------
  /**
   * Implement {@link ComponentListener} interface.  Currently does nothing.
   */
  public void componentShown(ComponentEvent e) { }
  //---------------------------------------------------------------------------
  /**
   * Raise an object selected event, based on the current contents of
   * m_selected.
   */
  private void raiseObjectSelectedEvent()
  {
    if (m_selected == null)
    {
      m_eventHandler.objectSelected(null);
    }
    else
    {
      Object o = m_selected.getObject();
      m_eventHandler.objectSelected(o);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Override base class method so that the grid texture can be altered
   * depending on the enabled or disabled state
   */
  public void setEnabled(boolean b)
  {
    if (b)
    {
      m_texture = m_enabledTexture;
    }
    else
    {
      m_texture = m_disabledTexture;
    }
    super.setEnabled(b);
    repaint();
  }
  //---------------------------------------------------------------------------
  /**
   * Raise an object selected event, based on the current contents of
   * m_selected.
   */
  private void raisePopupEvent(DisplayDelegate dd, MouseEvent e)
  {
    if (dd == null)
    {
      m_eventHandler.activatePopup(e);
      return;
    }

    Object o = dd.getObject();

    // Raise a selection event
    if (o instanceof Node)
    {
      m_eventHandler.activatePopup((Node) o, e);
    }
    else if (o instanceof NodeVariable)
    {
      m_eventHandler.activatePopup((NodeVariable) o, e);
    }
  }

}
