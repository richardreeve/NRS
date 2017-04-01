package nrs.tracker.robottrack;

import java.util.Vector;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * <p>Methods to be implemented by a user interface to acquire camera calibration data.
 *
 * @author Tobias Oberlies
 */

public interface CalibrationDataInputInterface
{
	/** Point selection direct. Single click selects a point. */
	public static final int POINT_DIRECT = 0;
	/** Point selection through edge detection. A point is found by a edge search on a line defined
		by two clicks or mouse dragging. */
	public static final int POINT_EDGE = 1;
	
	/** Select a set of points. The points have no special relation. */
	public static final int SELECT_POINTS = 0;
	/** Select point on a straight line.*/
	public static final int SELECT_POINTS_ON_LINE = 1;
	/** Select parallel lines. Each of the lines is definde by a pair of points, i.e. 1-2, 3-4, etc. */
	public static final int SELECT_PARALLEL_LINES = 2;

	
	
	//----------------------------------------------------------------
	// Mode methods
	
	/**
	 * Sets how points are selected.
	 * @param mode <code>POINT_DIRECT</code> to select points by single clicks, or 
	 * <code>POINT_EDGE</code> to use edge detection.
	 */
	public void setPointSelectMode(int mode);
	
	/**
	 * Returns the current point selection mode.
	 * @return one of the following: <code>POINT_DIRECT</code> or <code>POINT_EDGE</code>.
	 */
	public int getPointSelectMode();
	
	/**
	 * Sets what set of points should be selected. This also resets the selected points. 
	 * @param mode <code>SELECT_POINTS</code> to select (unrelated) points, 
	 * <code>SELECT_POINTS_ON_LINE</code>: select (three) points on a straight line, or
	 * <code>SELECT_PARALLEL_LINES</code>: select (two) parallel lines, defined by two points each.
	 */
	public void setSelectionSetMode(int mode);
	
	/**
	 * Returns the current selection set mode.
	 * @return one of the following: <code>SELECT_POINTS</code>, <code>SELECT_POINTS_ON_LINE</code>
	 * or <code>SELECT_PARALLEL_LINES.
	 */
	public int getSelectionSetMode();


	
	//------------------------------------------------------------------------------------------------
	// Result retrieval
	
	/**
	 * Returns the selected points. Note that the result is not checked for suitability for the 
	 * selection set mode, i.e. the vector could contain an odd number of points in mode
	 * <code>SELECT_PARALLEL_LINES</code>.
	 *
	 * @return a <code>Vector</code> of {@link java.awt.geom.Point2D} objects that represent the 
	 * chosen points. In case there are no points selected, the <code>Vector</code> will have 
	 * <code>size() == 0</code>.
	 */
	public Vector getPoints();
	
	
	/**
	 * Sets the selected points. This method can be used to show previous user input.
	 *
	 * @param points A <code>Vector</code> of {@link java.awt.geom.Point2D} objects, or <code>null</code>
	 * to reset the selection.
	 */
	public void setPoints(Vector points);



	//------------------------------------------------------------------------------------------------
	// Set image & access graphical output
	
	/**
	 * Sets the image displayed by the component. This also resets the selected points.
	 *
	 * @param image the image to be displayed, or <code>null</code> to display no image.
	 */
	public void setImage(BufferedImage image);
	
	/**
	 * Returns the resolution of the image or <code>null</code> if no image is currently displayed.
	 *
	 * @return the resolution wrapped in a <code>Point</code> object or <code>null</code>.
	 */
	public Point getImageResolution();
	
	/**
	 * Sets the colour with which the current selection is marked.
	 */
	public void setForeground(Color fg); 
	
	/**
	 * <p>Returns a <code>Graphics2D</code> object that allows to draw onto the displayed image.
	 * This can be used to draw special marks on the screen that are not the current selection. The 
	 * method {@link #reset()} restores the original image.
	 * <p>The object's clipping area is set to the boundary of the image.
	 *
	 * @return the {@link java.awt.Graphics2D Graphics2D} object to paint onto the image or null if
	 * the component doesn't have an image displayed.
	 */
	public Graphics getGraphics();
	
	/**
	 * Enforces changes in the image to be displayed. This method should be called after using paint 
	 * methods on the <code>Graphics</code> object obtained by {@link #getGraphics()}.
	 */
	public void repaint();
	 
	
	/**
	 * Resets the selection and restores the original image.
	 */
	public void reset();
}	

