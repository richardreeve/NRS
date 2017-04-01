package nrs.nrsgui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Interface for all classes which need to display themselves on a canvas
 *
 * @author Darren Smith
 */
interface DisplayDelegate
{
  /**
   * The value an {@link DisplayDelegate} instance height should be set
   * to at initialisation.
   */
  public static final int NOHEIGHT = -1;

  /**
   * Paint the component
   */
  void paint(Graphics2D g2d);

  /**
   * Paint the component with a selection boundary. This is now
   * deprecated. DisplayDelegate objects should know their own selection
   * state, and should paint themselves accordingly. However, before
   * making big changes, think about how to do multiple selections, and
   * think about what feature of multiple selection I wish to
   * include. Maybe something for a later version?
   */
  void paintSelected(Graphics2D g2d);

  /**
   * Retrieve the display height property. This indicates the layering
   * effect of the object with repect to othrs.  Lower values are drawn
   * first (and so appear at the bottom of a group) while higher values
   * are drawn last (and so appear at the top of a group).
   */
  int getDisplayHeight();

  /*
   * Set display height at which an instance should be
   * displayed. Objects with lower values are drawn first (and so appear
   * at the bottom of a group) while higher values are drawn last (and
   * so appear at the top of a group).
   */
  void setDisplayHeight(int h);

  /**
   * Returns true if the {@link Point} is contained within self
   */
  boolean contains(Point e);

  /**
   * Set the location of the object
   */
  void setLocation(int x, int y);

  /**
   * Get the x-coordinate where object is painted at
   */
  int x();

  /**
   * Get the y-coordinate where object is painted at
   */
  int y();

  /**
   * Get the rectangle which bounds the component. The
   * <code>x</code> and <code>y</code> fields of the rectangle give the
   * present location of the bounding-box of this component. The
   * bounding box is the smallest rectangle which is needed to display
   * the object together with its various children objects.
   *
   * The returned {@link Rectangle} is the same object as <code>r</code>
   * if <code>r</code> is non-null. Otherwise a new {@link Rectangle}
   * object is created and returned. This idiom allows retrieval of
   * bounds information without the overhead of creating a new object to
   * hold the data.
   */
  Rectangle bounds(Rectangle r);

  /**
   * Return the object which this {@link DisplayDelegate} graphically
   * represents.
   */
  Object getObject();

  /**
   * Set the selected state.
   *
   * value set to <tt>true</tt> to indicate component is selected, or
   * <tt>false</tt> to indicate it is not selected.
   */
  void setSelected(boolean value);

  /**
   * Get the selected state.
   *
   * @return <tt>true</tt> if component is selected, or <tt>false</tt>
   * if it is not selected.
   */
  boolean getSelected();
}
