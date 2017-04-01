package nrs.tracker.robottrack;

import javax.swing.JComponent;
import java.awt.Graphics;
//import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D;
//import java.awt.geom.Point2D.Double;
//import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
//import javax.swing.KeyStroke;


import java.util.Vector;


/**
 * <p> A custom component that displays an image and allows the user to make various point and line
 * selections for the camera calibration process. The user interacts with this component using mouse
 * and keyboard. An instance of the nested class CalibrationCanvas.EventListenerImpl
 * does the event handling.
 *
 * <p>There are two different ways of selecting points in the image. This can be set through the
 * <i>point selection mode</i>:
 * <ul>
 *    <li><code>POINT_DIRECT</code>: Points are selected by single clicks. More precisely the point
 *    will be selected on the <code>mouseReleased</code> event. To aid precise placing a cross will
 *    be displayed when the mouse button is held down.
 *    <li><code>POINT_EDGE</code>: Points are selected with the help of an edge detection algorithm.
 *    For that the user specifies a line, on which the algorithm will search for the edge. The line
 *    can either be selected by two clicks or by dragging the mouse.
 * </ul>
 * To undo the last selections, the user can press the <code>BACKSPACE</code> key. <code>DELETE</code>
 * also removes single points, but the oldest selection first. <code>ESCAPE</code> cancels all
 * selections.
 *
 * <p>The <i>edge detection algorithm</i> only works on edges that separate uniformly coloured areas
 * with distinct colours. This is because does not look for the edge in the usual sense, i.e. the
 * steepest gradient. It rather searches for a grayscale value that is the mean of the values at the
 * two endpoints. The granularity of the search space is significantly increased by simple linear
 * interpolation between the pixels. Yet this means that the algorithm may not find edge in the
 * sense of the steepest gradient exactly. The offset, if there is one, is however repeatable. Points
 * on a straight edge found by the algorithm are on a straight line parallel to the edge. The
 * variation is about a tenth of the pixel resolution, even on a very blurred edge.
  *
 * <p>Selected points are always marked by a cross. Additionally lines connecting the points give a
 * clue about the purpose of the selection. This is determined by the <i>selection set mode</i>:
 * <ul>
 *    <li><code>SELECT_POINTS</code> - unrelated points,
 *    <li><code>SELECT_POINTS_ON_LINE</code> - all points are connected,
 *    <li><code>SELECT_PARALLEL_LINES</code> - points are connected in groups of two.
 * </ul>
 * The last two modes are used when the user is selecting a straight line or parallel lines,
 * respectively. Straight refers to straight in the scene. A straight line is likely to be slightly
 * curved in the image due to radial distortion.
 *
 * @see nrs.tracker.robottrack.CalibrationWindow
 *
 * @author Tobias Oberlies
 */
public class CalibrationCanvas extends JComponent implements CalibrationDataInputInterface
{

	// Selection mode member
	/** Current point selection mode. */
	private int m_pointSelectMode = POINT_DIRECT;
	/** Current selection set mode. */
	private int m_selectionSetMode = SELECT_POINTS;

	/// Selection state

	/** Start point of the line on which to look for the edge. This variable is only used in point
		selection mode <code>POINT_EDGE</code>. */
	private Point m_edgeSearchStart;

	/** Current location of the mouse pointer. This variable is used for two things: in point
		selection mode <code>POINT_DIRECT</code> a cross is displayed at the mouse pointer location
		to aid the user with the precise selection of points. Secondly in point selection mode
		<code>POINT_EDGE</code> a line is drawn from <code>m_edgeSearchStart</code> to
		<code>m_mousePoint</code>. */
	private Point m_mousePoint;

	/** The mouse is being dragged. If the point select mode is <code>POINT_EDGE</code> and the
		start point of the line for edge detection has <i>not</i> been selected, this is considered
		to be true only after the mouse has been moved 5 pixels away from the location of the
		<code>mousePressed</code> event. Otherwise <code>m_isDragged</code> is true as soon as the
		mouse button is pressed. */
	private boolean m_isDragging;

	/** Selected points, stored in a <code>Vector</code> of <code>Point2D</code> objects. */
	private Vector m_selectedPoints = new Vector();


	/** The image shown in the component. Some methods paint directly into <code>m_image</code> -
		rather than remembering the marks in a different data structure (which would be necessary
		for {@link #paintComponent(Graphics)}). Hence a backup of the image is needed. */
	private BufferedImage m_image, m_imageBackup;

	/** The handler for mouse and keyboard events. */
	private CalibrationCanvas.EventListenerImpl m_eventHandler = new CalibrationCanvas.EventListenerImpl();


	/**
	 * Creates an empty <code>CalibrationCanvas</code>.
	 */
	public CalibrationCanvas()
	{
		// Register KeyListener to handle Escape and Backspace
		addKeyListener(m_eventHandler);
		setFocusable(true);

		// Set component size
		setMinimumSize(new Dimension(0,0));
		setPreferredSize(new Dimension(0,0));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
	}

	/**
	 * Creates a <code>CalibrationCanvas</code> that displays a given image.
	 */
	public CalibrationCanvas(BufferedImage image)
	{
		// Call the empty constuctor
		this();

		// Set the image
		setImage(image);
	}



	//------------------------------------------------------------------------------------------------
	// Implemented methods of CalibrationDataInputInterface (see for javadoc).
	// NB: setForeground(Color) and repaint() are inherited from JComponent.


	/* Get/set how points are selected */
	public void setPointSelectMode(int mode)
	{
		m_pointSelectMode = mode;

		// Reset half selections
		m_edgeSearchStart = null;
		m_isDragging = false;
		repaint();
	}
	public int getPointSelectMode()
	{
		return m_pointSelectMode;
	}


	/* Get/set what set of points is selected */
	public void setSelectionSetMode(int mode)
	{
		m_selectionSetMode = mode;

		// Reset selection state
		resetSelection();
		repaint();
	}
	public int getSelectionSetMode()
	{
		return m_selectionSetMode;
	}


	/* Get result. Also allow to set/restore result */
	public Vector getPoints()
	{
		return m_selectedPoints;
	}
	public void setPoints(Vector points)
	{
		// Reset selection
		resetSelection();

		// Ensure that m_selectedPoints is not null, but no check of content of Vector
		if (points != null)
			m_selectedPoints = points;

		repaint();
	}


	/* Set image. This also resets the selection */
	public void setImage(BufferedImage image)
	{
		// Check argument
		if (image == null)
		{
			// No image
			m_imageBackup = null;
			m_image = null;

			// Remove event handler
			removeMouseListener(m_eventHandler);
			removeMouseMotionListener(m_eventHandler);

			// Set preferred size to nothing
			setPreferredSize(new Dimension(0, 0));
		}
		else
		{
			// Register event handler (now that canvas has an image)
			if (m_image == null)
			{
				addMouseListener(m_eventHandler);
				addMouseMotionListener(m_eventHandler);
			}

			// Store image reference
			m_imageBackup = image;

			// Create a copy of the image. This is the one that will be worked with, i.e. lines are
			// painted into it. Therefore make sure the images don't share the same DataBuffer.
			m_image = new BufferedImage(image.getColorModel(), image.copyData(null), image.isAlphaPremultiplied(), null);

			// Set preferred size to show the entire image
			setPreferredSize(new Dimension(m_image.getWidth(), m_image.getHeight()));
		}

		// Reset selection (which is assumed to make no more sense)
		resetSelection();

		// Repaint component
		repaint();
	}

	/* Return the image resolution, or null if there is no image.*/
	public Point getImageResolution()
	{
		// Return null if there is no image
		if (m_image == null)
			return null;

		// Otherwise return the resolution
		return new Point(m_image.getWidth(), m_image.getHeight());
	}

	/* Return a {@link java.awt.Graphics2D} object that paints directly into m_image. This is easier than to remember
	 * all (nonstandard) marks for the drawComponent method. */
	public Graphics getGraphics()
	{
		if (m_image == null)
			return null;
		else
		{
			Graphics g = m_image.createGraphics();
			g.setColor(getForeground());
			g.setClip(0, 0, m_image.getWidth(), m_image.getHeight());
			return g;
		}
	}


	/* Reset image and selection */
	public void reset()
	{
		// Reset selection state
		resetSelection();

		// Reset marks painted on the canvas
		if (m_imageBackup != null)
			m_image = new BufferedImage(m_imageBackup.getColorModel(), m_imageBackup.copyData(null), m_imageBackup.isAlphaPremultiplied(), null);

		// Repaint component
		repaint();
	}

	/**
	 * Resets the selection.
	 */
	private void resetSelection()
	{
		m_selectedPoints.clear();
		m_edgeSearchStart = null;
		m_mousePoint = null;
		m_isDragging = false;
	}


	//------------------------------------------------------------------------------------------------
	// Customized painting

	/**
	 * <p>Paints the image and marks the current selection. Every selected point is marked with a
	 * cross. Depending on the selection set mode the points might also be connected by lines. Also
	 * draws a cross for precise point placing (<code>POINT_DIRECT</code>) or the line for the edge
	 * detection (<code>POINT_EDGE</code>).
	 * <p>The components is not expected to have a border, insets are not taken into account.
	 */
	protected void paintComponent(Graphics g)
	{
		// Paint background
        //g.setColor(getBackground());
        //g.fillRect(0, 0, getWidth(), getHeight());

		// Only draw if there is an image
		if (m_image != null)
		{
			// Display image
			g.drawImage(m_image, 0, 0, getBackground(), null);


			/// Draw volatile marks (i.e. the marks that are not painted into the image)
			g.setColor(getForeground());

			// Lines connecting selected points
			switch (m_selectionSetMode)
			{
				case SELECT_POINTS_ON_LINE:
					drawPolyLineFromVector(g, m_selectedPoints);
				break;

				case SELECT_PARALLEL_LINES:
					drawLinesFromVector(g, m_selectedPoints);
			}

			// Cross for all points
			for (int i = 0; i < m_selectedPoints.size(); i++)
				drawMark(g, (Point2D) m_selectedPoints.get(i), 'x');


			// Marks related to current mouse cursor positoin
			g.setColor(Color.BLACK);
			g.setXORMode(Color.WHITE);

			if (m_pointSelectMode == POINT_EDGE && m_edgeSearchStart != null && m_mousePoint != null)
			{
				// Line for edge finding
				drawLineFromDouble(g, m_edgeSearchStart.getX(), m_edgeSearchStart.getY(), m_mousePoint.getX(), m_mousePoint.getY());
			}
			else if (m_pointSelectMode == POINT_DIRECT && m_isDragging && m_mousePoint != null)
			{
				// Cross
				drawMark(g, m_mousePoint, 'x');
			}
        }
	}

	/**
	 * Draws a consecutive line connecting a list of points. Used for <code>SELECT_POINTS_ON_LINE</code>
	 * selection set mode.
	 * @param g the <code>Graphics</code> instance for the output.
	 * @param v a <code>Vector</code> of <code>Point2D</code> objects.
	 */
	private void drawPolyLineFromVector(Graphics g, Vector v)
	{
		// Validate arguments
		if (v.size() < 1)
			return;

		// Draw consecutive line
		Point2D lastPoint = (Point2D) v.get(0);
		for (int i=1; i < v.size(); i++)
		{
			Point2D nextPoint = (Point2D) v.get(i);
			drawLineFromDouble(g, lastPoint.getX(), lastPoint.getY(), nextPoint.getX(), nextPoint.getY());
			lastPoint = nextPoint;
		}
	}

	/**
	 * Draws lines between pairs of points. This connects the elements 0-1, 2-3, etc. Used for
	 * <code>SELECT_PARALLEL_LINES</code> selection set mode.
	 * @param g the <code>Graphics</code> instance for the output.
	 * @param v a <code>Vector</code> of <code>Point2D</code> objects. The last element of <code>v</code>
	 * is ignored if the number of points is odd.
	 */
	private void drawLinesFromVector(Graphics g, Vector v)
	{
		// Draw a line between pairs of points (i.e. 0-1, 2-3, 4-5, etc.)
		for (int i=1; i < v.size(); i=i+2)
		{
			Point2D point1 = (Point2D) v.get(i-1);
			Point2D point2 = (Point2D) v.get(i);
			drawLineFromDouble(g, point1.getX(), point1.getY(), point2.getX(), point2.getY());
		}
	}

	/**
	 * Draws a point marker.
	 * @param g the <code>Graphics</code> instance for the output.
	 * @param p the location where the mark is to be drawn.
	 * @param type <code>'x'</code> for a cross or <code>'.'</code> for a dot.
	 */
	private void drawMark(Graphics g, Point2D p, char type)
	{
		// Draw a marker on the point
		switch (type)
		{
			case 'x':
				// Cross
				drawLineFromDouble(g, p.getX()-2, p.getY()-2, p.getX()+2, p.getY()+2);
				drawLineFromDouble(g, p.getX()-2, p.getY()+2, p.getX()+2, p.getY()-2);
				break;
			case '.':
				// Dot
				g.fillRect((int) Math.round(p.getX()), (int) Math.round(p.getY()), 1, 1);
		}
	}

	/**
	 * Equivalent of {@link Graphics#drawLine(int,int,int,int)} for <code>double</code> arguments.
	 */
	private void drawLineFromDouble(Graphics g, double x1, double y1, double x2, double y2)
	{
		// Draw line with rounded values
		g.drawLine((int) Math.round(x1), (int) Math.round(y1), (int) Math.round(x2), (int) Math.round(y2));
	}


	//------------------------------------------------------------------------------------------------
	// Event handling


	/**
	 * Nested class that handles mouse and keyboard events from a <code>CalibrationCanvas</code>.
	 */
	private class EventListenerImpl implements MouseInputListener, KeyListener
	{

		/**
		 * Add the selected point or search for edge.
		 */
		public void mouseReleased(MouseEvent e)
		{
			// Exit if not button1
			if (e.getButton() != MouseEvent.BUTTON1)
				return;
			// Exit if no image
			if (m_image == null)
				return;


			// Mouse button no more held
			//m_isMouseDown = false;
			m_isDragging = false;

			// Determine the newly selected point
			Point2D newPoint;

			if (m_pointSelectMode == POINT_EDGE)
			{
				// Start or end point for edge search?
				if (m_edgeSearchStart == null)
				{
					// Start point (so no newPoint yet)
					m_edgeSearchStart = e.getPoint();
					return;
				}
				else
				{
					// Search for edge (fails if one of the points is outside the image bounds)
					try
					{
						newPoint = findEdge(m_edgeSearchStart, e.getPoint());
					}
					catch (ArrayIndexOutOfBoundsException exception)
					{
						// Ignore
						return;
					}

					m_edgeSearchStart = null;
				}
			}
			else //if (m_pointSelectMode == POINT_DIRECT)
			{
				// Just the clicked point
				newPoint = e.getPoint();
			}


			// Add new point (if it is on the image)
			if (newPoint.getX() < m_image.getWidth() && newPoint.getY() < m_image.getHeight())
			{
				m_selectedPoints.add(newPoint);
				repaint();
				//System.out.println(newPoint);
			}
		}

		/**
		 * Register that the mouse button is held down. When in <code>POINT_DIRECT</code> point
		 * selection mode the canvas will show a cross below the mouse pointer to aid precise
		 * placing of points.
		 */
		public void mousePressed(MouseEvent e)
		{
			// Uptdate mouse pointer position
			m_mousePoint = e.getPoint();

			if (e.getButton() == MouseEvent.BUTTON1)
			{
				// Start dragging if selecting points directly or selecting 2nd point for edge search
				if (m_pointSelectMode == POINT_DIRECT || m_edgeSearchStart != null)
				{
					m_isDragging = true;
					repaint();
				}
			}
		}

		/**
		 * Determine whether in dragging mode. In <code>POINT_EDGE</code> dragging will start once
		 * the cursor has been dragged 5 pixels away from the point where the mouse button was
		 * pressed. This allows the user to select the line for edge search by dragging the mouse
		 * rather than with two clicks, but makes this not too sensitive.
		 */
		public void mouseDragged(MouseEvent e)
		{
			if (m_isDragging)
			{
				// In dragging mode so repaint
				m_mousePoint = e.getPoint();
				repaint();
			}
			else if (m_pointSelectMode == POINT_EDGE && (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
			{
				if (m_mousePoint != null && m_mousePoint.distanceSq(e.getPoint())>25)
				{
					// Start dragging now that the cursor is 5 pixels away from the mousePressed event
					m_isDragging = true;

					// Start of the edge search line is where the mouse was pressed (m_mousePoint will
					// not have been updated since the mousePressed event), the end of the search line
					// will be where the user releases the button button
					m_edgeSearchStart = m_mousePoint;
					m_mousePoint = e.getPoint();
					repaint();
				}
			}
		}

		/**
		 * Show the aiming cross or the edge detection line.
		 */
		public void mouseEntered(MouseEvent e)
		{
			m_mousePoint = e.getPoint();
			if (m_pointSelectMode == POINT_EDGE && m_edgeSearchStart != null)
				repaint();
		}

		/**
		 * Hide the aiming cross or the edge detection line.
		 */
		public void mouseExited(MouseEvent e)
		{
			m_mousePoint = null;
			if (m_pointSelectMode == POINT_EDGE && m_edgeSearchStart != null)
				repaint();
		}

		/**
		 * Relocate the aiming cross or the search for edge line.
		 */
		public void mouseMoved(MouseEvent e)
		{
			m_mousePoint = e.getPoint();
			if (m_pointSelectMode == POINT_EDGE && m_edgeSearchStart != null)
				repaint();
		}

		/** No action. */
		public void mouseClicked(MouseEvent e) {}


		/**
		 * Unselect points if the user presses Escape or Backspace.
		 */
		public void keyPressed(KeyEvent e)
		{
			// React to Escape and Backspace (but only when the mouse button is not pressed because
			// that leads to unexpected behaviour)
			int key = e.getKeyCode();
			if ((key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_BACK_SPACE || key == KeyEvent.VK_DELETE) && !m_isDragging)
			{
				if (m_edgeSearchStart != null)
				{
					// Reset edge search
					m_edgeSearchStart = null;
					m_mousePoint = null;
				}
				else if (m_selectedPoints.size() > 0)
				{
					if (key == KeyEvent.VK_ESCAPE)
					{
						// Reset all selected points
						m_selectedPoints.clear();
					}
					else if (key == KeyEvent.VK_DELETE)
					{
						// Remove first selected point or pair if in SELECT_PARALLEL_LINES selection set mode
						m_selectedPoints.remove(0);
						if (m_selectionSetMode == SELECT_PARALLEL_LINES && m_selectedPoints.size() > 0)
							m_selectedPoints.remove(0);
					}
					else
					{
						// Remove last selected point
						m_selectedPoints.remove(m_selectedPoints.size()-1);
					}
				}

				// Consume event
				e.consume();
				repaint();
			}
		}

		/** No action. */
		public void keyTyped(KeyEvent e) {}

		/** No action. */
		public void keyReleased(KeyEvent e) {}


		//--------------------------------------------------------------------------------------------
		// Point selection methods

		/**
		 * <p>Returns the edge between two uniformly coloured areas. The search is performed on the
		 * line defined by the two endpoints. The search target is the mean colour of the two
		 * endpoints (all colours are converted to grayscale). To get a more precise result, the
		 * colour 'between' pixels is linerarly interpolated. The method works well even with
		 * blurred edges.
		 *
		 * <p>The method does not look for the edge in the usual understanding, i.e. the steepest
		 * gradient. Therefore it might not return the exact edge in this sense. The offset, if
		 * there is one, is however repeatable. Points on a straight edge found by this method are
		 * on a straight line parallel to the edge. The precision is a factor up to 10 better than
		 * the pixel resolution.
		 *
		 * @param p1 the first endpoint of the search space.
		 * @param p2 the second endpoint on the search space.
		 * @return the found edge.
		 *
		 * @throws ArrayIndexOutOfBoundsException if <code>p1</code> or <code>p2</code> are outside
		 * the boundaries of the image.
		 */
		private Point2D.Double findEdge(Point p1, Point p2) throws ArrayIndexOutOfBoundsException
		{
			// Colours of the end points
			double colour1 = toGray(m_imageBackup.getRGB(p1.x, p1.y));
			double colour2 = toGray(m_imageBackup.getRGB(p2.x, p2.y));

			// Search space: x = start + a*vector with a from 0 (dark endpoint) to 1 (light endpoint)
			double startX, startY, vectorX, vectorY;
			if (colour1 < colour2)
			{
				startX = p1.x;
				startY = p1.y;
				vectorX = p2.x-p1.x;
				vectorY = p2.y-p1.y;
			}
			else
			{
				startX = p2.x;
				startY = p2.y;
				vectorX = p1.x-p2.x;
				vectorY = p1.y-p2.y;
			}

			// Target colour
			double targetColour = (colour1+colour2)/2;

			// Binary search
			double lBound = 0.0d, uBound = 1.0d;
			double precision = 0.1d / Math.sqrt(Math.pow(vectorX,2)+Math.pow(vectorY,2)); // 0.1 pixels
			while (uBound-lBound > precision)
			{
				// Point in the middle
				double middle = (lBound+uBound)/2;
				double preciseX = startX + middle*vectorX;
				double preciseY = startY + middle*vectorY;

				// Interpolate colour
				int floorX = (int) Math.floor(preciseX);
				int floorY = (int) Math.floor(preciseY);
				double remainderX = preciseX - floorX;
				double remainderY = preciseY - floorY;
				double preciseColour = toGray(m_imageBackup.getRGB(floorX,   floorY  )) * (1-remainderX) * (1-remainderY)
									 + toGray(m_imageBackup.getRGB(floorX+1, floorY  )) * remainderX     * (1-remainderY)
									 + toGray(m_imageBackup.getRGB(floorX,   floorY+1)) * (1-remainderX) * remainderY
									 + toGray(m_imageBackup.getRGB(floorX+1, floorY+1)) * remainderX     * remainderY;

				// Use as new boundary
				if (preciseColour<targetColour)
					lBound = middle;
				else
					uBound = middle;
			}

			// Result
			double middle = (lBound+uBound)/2;
			return new Point2D.Double(startX+middle*vectorX, startY+middle*vectorY);
		}


		/**
		 * Converts an RGB integer into a grayscale value.
		 * @param rgb an integer pixel in the default RBG color model and default sRGB colorspace.
		 * @return the grayscale value in a range between <code>0.0</code> and <code>1.0</code>.
		 * @see java.awt.image.BufferedImage#getRGB(int, int)
		 */
		private double toGray(int rgb)
		{
			int sum = (rgb & 0xff) + ((rgb>>8) & 0xff) + ((rgb>>16) & 0xff);
			return sum/765d; // 3*255
		}
	}
}
