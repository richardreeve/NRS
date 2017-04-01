package nrs.tracker.robottrack;

import java.awt.geom.Point2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.util.Vector;
import java.util.Properties;


/**
 * Provides methods to determine point locations on a plane from a camera's view onto that plane. 
 * Once the <code>CameraProjection</code> is fully configured the {@link #transform(Point2D)} 
 * translates a point in <i>image coordinates</i> into real-world or <i>scene coordinates</i>. The 
 * translation does the following steps:
 * <ul>
 *    <p><li>Rectification of <b>radial distortion</b>. Especially cheap or wide-angle cameras suffer
 *    from that distortion. It makes straight lines appear curved if they are not in the center of
 *    the image. This effect is caused by the camera's lenses. To correct it two bits of information
 *    are needed. First the pixel at which the <i>optical axis</i> intersects with the image plane.
 *    The middle of the image is a good guess, but often the CCD chip (the chip that converts light
 *    into electronic signals) is not centered exaclty behind the lenses. Secondly, the <i>focal 
 *    length</i> of the camera lenses. The smaller that value is, the stronger is the distortion. 
 *    I'm actually not sure about the unit of the focal length, but that's not too much of an issue 
 *    (the calibration process is described later). After this step straight lines in the real world 
 *    are also straight in the image.
 *
 *    <p><li>Rectification of <b>tilt distortion</b>. It is very unlikely (and acually not desirable,
 *    but more about this later) that the camera is not at all tilted relative to the plane it is
 *    facing. Depending on how much the camera is tilted, things will look smaller at one side of
 *    the image than at the other. This can be corrected if the distance to the <i>horizontal and 
 *    vertical vanishing point</i> is known. The vanishing points are the points where all 
 *    originally parallel lines meet - in the image from a tilted camera will not be parallel and 
 *    hence their extensions do meet. One gets to choose the horizontal and vertical direction. 
 *    The directions should be perpendicular (in the scene), because they will be the x and y axis 
 *    of the scene coordinate system.
 *    
 *    <p><li>Rectification of <b>rotation</b>. The rotation is corrected in one step together with 
 *    the tilt. The extra information needed for that is the <i>principal point</i>. The prinicipal 
 *    point is the intersection of a line that is horizontal on the screen (!) and goes through the 
 *    horizontal vanishing point with a line vertical on the screen through the vertical vanishing 
 *    point. 
 *    <p>The original tilt distortion algorithm (see paper reference below) does only work if the 
 *    image was not rotated, which requires prior rotation correction. (Not rotated means that 
 *    lines in the "horizontal" direction in the scene are fairly horizontal in the image and 
 *    vertical lines fairly verical.) The principal point in that implementation was then assumed to
 *    be in the center of the image - which is roughly true, the horizontal vanishing point will be 
 *    left or right of the image and the vertical one above or below. However if the principal point
 *    is calculated as described in the previous paragraph, the same formulas will correct tilt and
 *    rotation at the same time. To understand that concept, imagine that the current image is a 
 *    rectangular section of a bigger image from a camera with much wider viewing angle. Now image 
 *    that the wide angle camera has a view so that its image has the principal point is in the 
 *    center (try to draw it: draw the original image with two horizontal and two vertical lines, 
 *    construct the vanishing points, construct the principal point and finally draw an image around
 *    that point big enough to enclose the original image; add some more lines each through one of 
 *    the vanishing point). If the wide angle image now is tilt 
 *    rectified, all originally horizonal (vertical) lines will be horizontal (vertical) in the 
 *    resulting image, hence also in every part of it. So what has been done on the actual image is
 *    tilt rectification with the principal point outside the center, and it corrected tilt 
 *    distortion and rotation.
 *    <p>One might wonder how the rotation was eliminated. Actually, there is no rotation. Everyone 
 *    would agree that the (imagined/drawn) bigger image with the principal point in its center is 
 *    not rotated. But the actual image is a part of the big image so it can be considered to be not 
 *    rotated either. That is exaclty what the algorithm does so by definition there is only tilt,
 *    whose correction also makes the apparet rotation disappear. It would do no harm to rotation 
 *    the image before doing the integrated tilt and rotation rectification. However is is not 
 *    always possible to force the principal point into the center of the image by rotation, so the 
 *    original "tilt-only" algorithm is insufficient for many situations. The only case in which my
 *    approach will no work if there is no tilt, i.e. one of the vanishing points disappears into
 *    infinity.
 *
 *    <p><li>The last step is a pair of simple one-dimensional affine transformation. They are totally
 *    separate for the x and the y coordinate, because the tilt and rotation rectification ensures
 *    that the respective axes of the image coordinate system and the scene coordinate system are
 *    parallel. So all this step does is scale and shift the values from the previous transformation
 *    steps so that the result is in the desired scene coordinate system.
 * </ul>
 *
 * Since the parameters for the calibration are far from trivial, <code>CameraProjection</code> also 
 * provides a set of tools to acqire them:
 * <ul>
 *    <p><li>In theory all straight lines (in the scene) have the <i>optical axis</i> on the inside
 *    of the curvature (a line through the optical axis should be dead straight despite radial 
 *    distortion). There is a method that indicates the curvature of a line, given three points on 
 *    that line. Unfortunately the method is only suitable for an initial estimate, because the curvature near
 *    the optical axis is minimal and so easily affected by errors in the point locations. Once the calibration process
 *    is finished and the scene coordinate grid can be drawn onto the image, it might be necessary
 *    to refine the optical axis. Move it towards the direction where the drawn grid seems to be 
 *    curved too much, since this will reduce the curvature there (and increase it on the opposite
 *    side).
 *
 *    <p><li>There is a method that searches for the <i>focal length</i> parameter. It takes again 
 *    three points on a theoretically straight line and finds a value for that focal length so that 
 *    those three points are on a straight line after the radial distortion rectification. It is 
 *    a good idea to use lines away from the optical axis, because they are more curved and so 
 *    small errors will have less impact. The result from that method will of course only be correct
 *    if the optical axis is correct, so running it with different lines and averaging the result 
 *    will give a better initial estimate. For this value it might also be necessary to some manual 
 *    fine tuning after everything has been (roughly) calibrated. Smaller values will make the test 
 *    grid curve more.
 *    
 *    <p><li>The vanishing points can be easily and reliably aquired through a further method of this
 *    class. From those points the principal point and the (inverse of) the distance of the 
 *    vanishing points (from the principal point) is calculated automatically. The method takes
 *    two parallel lines, each defined by two points. As a matter of fact it doesn't matter which 
 *    direction in the real world is chosen to be the vertical direction and which the horizontal 
 *    one. They should be perpendicular of course, since they will be the y and x axes of the scene
 *    coordinate system (but don't have to, actually).
 *
 *    <p><li>Since it is not reasonable to enter the final affine transformations directly (the 
 *    tilt recification step shifts the values for x and y to very strange ranges), there is an 
 *    alternative, and actually very convenient way. The user specifies how points in the
 *    image map to points in the scene coordinate system. Since it is often much easier to specify
 *    only one of the coordinates in the scene (e.g. "this pixel is on the x-axis"), this is allowed 
 *    to do. In order to determine the affine transformation for x (y) there must be at least two 
 *    entries that specify the x (y) scene coordinate. If the scaling works out to be imprecise, one 
 *    can just go ahead and add more map entries (rather than trying to identify which entries are 
 *    responsible for that). The transformations will be calculated through a least-square line 
 *    fitting algorithm so that possible errors should eventually cancel out.
 *    <p>Programmatically, the map entries are accessible through the nested class
 *    {@link CameraProjection.CoordinateMap CoordinateMap}.
 * </ul>
 * <p>Once all those parameters have been calibrated, it is possible to draw the scene coordinate 
 * grid onto the image. This shows the quality of the calibration. Often it is necessary to adjust 
 * the optical axis and the focal length. Unfortunately changing either affects the vanishing 
 * points, so they have to be re-calibrated as well. The <code>CoordinateMap</code> however ensures
 * that this is not necessary for the affine transform/"scaling and offset" step.
 *
 * <p>As a general approach, I recommend to first run the calibration process on a snapshot of a 
 * chessboard or evenly spaced grid. This provides a lot of straight lines and so allows the optical 
 * axis and focal length to be calibrated precisely. Those two values are specific to a camera (and 
 * independant of where the camera is mounted etc.). Once this is done, calibrate the vanishing 
 * points and the scaling for the real scene, using an image from the final camera location.
 *
 * <p>Tobias Oberlies, Edinburgh, 2004
 * <p>
 *
 * <dl>
 * <dt><b>References:</b></dt>
 * <dd>
 *     Gregor Klancar, Matej Kristan, Rihard Karba: "Wide-angle camera distortions and non-uniform
 *     illumination in mobile robot tracking", Robotics and Autonomous Systems 46 (2004) 125-133.
 * </dd>
 * </dl>
 * 
 * @see CalibrationWindow
 *
 * @author Tobias Oberlies
 */
public class CameraProjection
{
	/** Constant for {@link #findVanishingPoint(int,Point2D,Point2D,Point2D,Point2D)}. Warning: the values of this constant must not be changed! */
	public final int VP_HORIZONTAL = 0, VP_VERTICAL = 1;
	/** Constant for {@link #validateState(int)}. Warning: the values of this constant must not be changed! */
	public final int OPT_AXIS = 0,
					 FOCAL_LENGTH = 1,
					 VANISH_PTS = 2,
					 ALL = 4;
	
	/** Optical axis. Either both or none of the coordinates are valid. <code>Double.NaN</code> is 
		used to flag invalid parameters. */
	private double m_optAxisX = Double.NaN, m_optAxisY = Double.NaN;
	
	/** Focal length. */
	private double m_focalLength = Double.NaN;
	
	/** Vanishing points, indexed with <code>VP_HORIZONTAL</code> and <code>VP_VERTICAL</code>. Either both or none of the 
		coordinates of each vanishing point are valid. */
	private double[] m_vanishPtX = {Double.NaN, Double.NaN}, m_vanishPtY = {Double.NaN, Double.NaN};
	/** Inverse of the distance of the horizontal vanishing point. This value depends on the the
		vanishing points, but is <i>not</i> updated on every change. */
	private double m_vanishP = Double.NaN;
	/** Inverse of the distance of the vertical vanishing point. This value is computed from the
		vanishing points, but is <i>not</i> updated on every change. */
	private double m_vanishQ = Double.NaN;
	
	/** Affine transformation from rectified image coordinates to real-world coordinates. */
	private double[] m_transformX = {Double.NaN, Double.NaN}, m_transformY = {Double.NaN, Double.NaN};
	/** The coordinate map (if used) to calculate the affine transformation. */
	private CameraProjection.CoordinateMap m_coordMap = null;
	
	/** Image resolution used for the calibration. */
	private int m_resWidth = 0, m_resHeight = 0;
	
	/** Camera type. */
	private String m_cameraType = "";
	/** Description of camera location. */
	private String m_cameraLoc = "";
	/** Description of the coordinate system. */
	private String m_coordDescr = "";
	/** Base unit of the coordinate system. */
	private String m_coordUnit = "";
	/** Height above the ground for which the coordinate system is calibrated. */
	private String m_coordHeight = "";

	
	/**
	 * Creates a new <code>CameraProjection</code> object with unspecified parameters.
	 */
	public CameraProjection()
	{
	}
	
	/**
	 * Creates a new <code>CameraProjection</code> object and loads the parameters from the 
	 * specified <code>Properties</code> object (see {@link #load(Properties)}).
	 * @param parameters an initialized <code>Properties</code> instance.
	 */
	public CameraProjection(Properties parameters)
	{
		load(parameters);
	}
	
	/**
	 * Restores calibration parameters from the specified <code>Properties</code> instance. The 
	 * following keys are recognized: 
	 * <ul>
	 *     <li>"optical_axis.x" and "optical_axis.y" - the camera's optical axis,
	 *     <li>"focal_length" - the camera's focal length,
	 *     <li>"principal_point.x" and "principal_point.y" - the principal point of the tilt,
	 *     <li>"vanish_distance.horizontal" - the distance of the horizontal vanishing point to the 
	 *     principal point (<i>not</i> the inverse of the distance, because the former is an integer 
	 *     which is less likely to cause encoding problems),
	 *     <li>"vanish_distance.vertical" - the distance of the vertical vanishing point, 
	 *     <li>"translate.x.scale", "translate.x.offset", "translate.y.scale" and 
	 *     "translate.y.offset" - the final affine transformation to real-world coordinates.
	 *     <li>"resolution.width" and "resolution.height" - the resolution of the image used for
	 *     the calibration.
	 *     <li>"camera_type" - the camera's type
	 *     <li>"camera_location" - description of the location where the camera is mounted
	 *     <li>"scene_coord_system.desc" and "scene_coord_system.unit" - description of the scene 
	 *     coordinate system and the base unit of it (e.g. mm)
	 *     <li>"scene_coord_system.height" - the height above the ground/base at which the 
	 *     scene coordinate system is calibrated (measured or descriptive).
	 * </ul>
	 * Note that key names are case-sensitive. 
	 * <p>Parameters that are not found in the map or have an empty string <code>""</code> as value 
	 * are considered to be unspecified. If a numerical value can't be parsed, a 
	 * <code>NumberFormatException</code> is thrown.
	 *
	 * @param p an initialized <code>Properties</code> object containing the parameters as described 
	 * above.
	 * @throws NumberFormatException if the map contains an incorrect type for one of the parameters.
	 */
	public void load(Properties p) throws NumberFormatException
	{
		// Optical axis
		setOpticalAxis(new Point2D.Double( parseDouble(p.getProperty("optical_axis.x")),
										   parseDouble(p.getProperty("optical_axis.y")) ));
		
		// Focal lenght
		setFocalLength(parseDouble(p.getProperty("focal_length")));
		
		// Vanishing points
		double principalX = parseDouble(p.getProperty("principal_point.x"));
		double principalY = parseDouble(p.getProperty("principal_point.y"));
		double vanishHorX = principalX + parseDouble(p.getProperty("vanish_distance.horizontal"));
		double vanishVertY = principalY + parseDouble(p.getProperty("vanish_distance.vertical"));
		setVanishingPoint(VP_HORIZONTAL, new Point2D.Double(vanishHorX, principalY));
		setVanishingPoint(VP_VERTICAL, new Point2D.Double(principalX, vanishVertY));		
		
		// Scaling
		double scaleX = parseDouble(p.getProperty("translate.x.scale"));
		double offsetX = parseDouble(p.getProperty("translate.x.offset"));
		double scaleY = parseDouble(p.getProperty("translate.y.scale"));
		double offsetY = parseDouble(p.getProperty("translate.y.offset"));
		setAffineTransforms(scaleX, offsetX, scaleY, offsetY);
		
		// Restore coordinate map (if used)
		if (m_coordMap != null)
			m_coordMap.restoreFromTransforms();
		
		// Resolution
		int width = (int) parseDouble(p.getProperty("resolution.width"));
		int height = (int) parseDouble(p.getProperty("resolution.height"));
		setResolution(width, height);
		
		// Camera type and location
		setCameraType(p.getProperty("camera_type", ""));
		setCameraLocation(p.getProperty("camera_location", ""));
		
		// Scene coordinate system descriptions
		setSceneCoordDescription(p.getProperty("scene_coord_system.desc", ""));
		setSceneCoordUnit(p.getProperty("scene_coord_system.unit", ""));
		setSceneCoordHeight(p.getProperty("scene_coord_system.height", ""));
	}
	
	/**
	 * Parses a string, whereby <code>null</code> and the empty string is interpreted as Double.NaN.
	 * @param s a string containing a number.
	 * @return the number or Double.NaN if the argument was <code>null</code> or the empty string.
	 */
	private double parseDouble(String s) throws NumberFormatException
	{
		return (s==null || s.trim().equals("")) ? Double.NaN : Double.parseDouble(s);
	}
	
	
	/**
	 * Stores the calibration parameters into a <code>Properies</code> object. The following key 
	 * values are used: <ul>
	 *     <li>"optical_axis.x" and "optical_axis.y",
	 *     <li>"focal_length", 
	 *     <li>"principal_point.x", "principal_point.y", "vanish_distance.horizontal" and 
	 *     "vanish_distance.vertical", 
	 *     <li>"translate.x.scale", "translate.x.offset", "translate.y.scale" and "translate.y.offset".
	 *     <li>"resolution.width" and "resolution.height",
	 *     <li>"camera_type" and "camera_location",
	 *     <li>"scene_coord_system.desc", "scene_coord_system.unit" and "scene_coord_system.height".
	 * </ul>
	 * Undefinded values are stored as empty strings.
	 */
	public Properties save()
	{
		// Update state if using a CoordinateMap
		if (m_coordMap != null)
			m_coordMap.commitIfPossible();
		
		/// Store state
		Properties m = new Properties();
		
		// Optical axis
		m.setProperty("optical_axis.x", toStringOrEmpty(m_optAxisX));
		m.setProperty("optical_axis.y", toStringOrEmpty(m_optAxisY));
		
		// Focal lenght
		m.setProperty("focal_length", toStringOrEmpty(m_focalLength));
		
		// Vanishing points
		m.setProperty("principal_point.x", toStringOrEmpty(m_vanishPtX[VP_VERTICAL]));
		m.setProperty("principal_point.y", toStringOrEmpty(m_vanishPtY[VP_HORIZONTAL]));
		m.setProperty("vanish_distance.horizontal", toStringOrEmpty(m_vanishPtX[VP_HORIZONTAL] - m_vanishPtX[VP_VERTICAL]));
		m.setProperty("vanish_distance.vertical", toStringOrEmpty(m_vanishPtY[VP_VERTICAL] - m_vanishPtY[VP_HORIZONTAL]));
		
		// Scaling
		m.setProperty("translate.x.scale", toStringOrEmpty(m_transformX[0]));
		m.setProperty("translate.x.offset", toStringOrEmpty(m_transformX[1]));
		m.setProperty("translate.y.scale", toStringOrEmpty(m_transformY[0]));
		m.setProperty("translate.y.offset", toStringOrEmpty(m_transformY[1]));
		
		// Resolution
		m.setProperty("resolution.width", Integer.toString(m_resWidth));
		m.setProperty("resolution.height", Integer.toString(m_resHeight));
		
		// Descriptions
		m.setProperty("camera_type", m_cameraType);
		m.setProperty("camera_location", m_cameraLoc);
		m.setProperty("scene_coord_system.desc", m_coordDescr);
		m.setProperty("scene_coord_system.unit", m_coordUnit);
		m.setProperty("scene_coord_system.height", m_coordHeight);
		
		return m;
	}


	/**
	 * Converts a double value it's string representation, or the empty string "" if the argument is
	 * <code>Double.NaN</code>.
	 * @param v the value to convert.
	 * @return the string representation of the value, with "" for undefined.
	 */
	private String toStringOrEmpty(double v)
	{
		return Double.isNaN(v) ? "" : Double.toString(v);
	}



	/**
	 * Sets the resolution that is used for the calibration. This is typically the size of the 
	 * captured image from which the calibration information is gathered. The resolution is 
	 * important to know because the camera tracker might be running in a lower resolution (for 
	 * higher frame rates). If a different resolution is used, the first step of the transformation 
	 * is to translate the pixel coordinates in tracker resolution to the pixel coordinate in
	 * the resolution used for calibration. There is a separate tool <code>rescalib</code> to 
	 * measure the transformation between resolutions.
	 * @param width the image width.
	 * @param height the image height.
	 */
	public void setResolution(int width, int height)
	{
		// Don't allow negative values
		m_resWidth = width<0 ? 0 : width;
		m_resHeight = height<0 ? 0 : height;
	}

	/**
	 * Returns the calibration resolution.
	 * @return a <code>Point</code> containing the width and heigth.
	 */
	public Point getResolution()
	{
		return new Point(m_resWidth, m_resHeight);
	}

	/**
	 * Sets the camera type described by this <code>CameraProjection</code> instance.
	 * @param type the camera type
	 */
	public void setCameraType(String type)
	{
		// Validate arguments
		if (type == null) 
			throw new NullPointerException();
		
		m_cameraType = type;
	}
	
	/**
	 * Returns the camera type described by this <code>CameraProjection</code> instance.
	 * @return the camera type.
	 */
	public String getCameraType()
	{
		return m_cameraType;
	}

	/**
	 * Sets the description of the camera's location.
	 * @param descr the camera location.
	 */
	public void setCameraLocation(String descr)
	{
		// Validate arguments
		if (descr == null) 
			throw new NullPointerException();
		
		m_cameraLoc = descr;
	}

	/**
	 * Returns the description of the camera's location.
	 * @return the camera location.
	 */
	public String getCameraLocation()
	{
		return m_cameraLoc;
	}

	/**
	 * Sets the description of the scene coordinate system. This could contain information like 
	 * the coordinate system origin and orientation.
	 * @param descr description of the scene coordinate system.
	 */
	public void setSceneCoordDescription(String descr)
	{
		// Validate arguments
		if (descr == null) 
			throw new NullPointerException();
		
		m_coordDescr = descr;
	}

	/**
	 * Returns the description of the scene coordinate system.
	 * @return the description of the scene coordinate system.
	 */
	public String getSceneCoordDescription()
	{
		return m_coordDescr;
	}

	/**
	 * Sets the base unit of the scene coordinate system. This is the unit of measurements, e.g. mm
	 * for millimeters.
	 * @param unit the scene coordinate sytem base unit.
	 */
	public void setSceneCoordUnit(String unit)
	{
		// Validate argument
		if (unit == null)
			throw new NullPointerException();
		
		m_coordUnit = unit;
	}

	/**
	 * Returns the base unit of the scene coordinate system.
	 * @return the scene coordinate sytem base unit.
	 */
	public String getSceneCoordUnit()
	{
		return m_coordUnit;
	}

	/**
	 * <p>Sets the height above the floor or base at which the scene coordinate system is calibrated. 
	 * This should be the height at which the coloured spots are when they are attached to the 
	 * robot. If the robot height does not match this value, the spots will appear to the camera
	 * to be in a different location (a spot that is lifted off the ground looks the same as a spot
	 * on the ground further outwards). As a consequence the tracked locations will be correct in
	 * center, but slightly offset towards the edge of the tracking area. 
	 * <p>A descriptive height like "Koala height" is reasonable, no numeric value is required.
	 */
	public void setSceneCoordHeight(String height)
	{
		// Validate argument
		if (height == null)
			throw new NullPointerException();
		
		m_coordHeight = height;
	}

	/**
	 * Returns the height of the scene coordinate system above the ground/base.
	 * @return the height in numbers or descriptions.
	 */
	public String getSceneCoordHeight()
	{
		return m_coordHeight;
	}



	/**
	 * Sets the optical axis of this CameraProjection.
	 *
	 * @param p the intersection of the image plane with the optical axis.
	 */
	public void setOpticalAxis(Point2D p)
	{
		if (p == null || Double.isNaN(p.getX()) || Double.isNaN(p.getY()))
		{
			// Invalid optical axis
			m_optAxisX = Double.NaN;
			m_optAxisY = Double.NaN;
		}
		else
		{
			// Set optical axis
			m_optAxisX = p.getX();
			m_optAxisY = p.getY();
		}
	}

	/**
	 * Returns the optical axis of this CameraProjection.
	 *
	 * @return the point where the image plane and the optical axis intersects, or <code>null</code>
	 * if the optical axis is undefined.
	 */
	public Point2D getOpticalAxis()
	{
		if (Double.isNaN(m_optAxisX))
			return null;
		else
			return new Point2D.Double(m_optAxisX, m_optAxisY);
	}


	/**
	 * Finds a focal length so that the three specified points are on a straight line in the rectified
	 * image. The result is also stored in this CameraProjection object.
	 * 
	 * @param endPoint1 the first end point of the line.
	 * @param endPoint2 the second end point of the line.
	 * @param middle a point on the line between the two end points.
	 *
	 * @return the focal length that "straightens" the specified line. If the three points
	 * are on a straight without any rectification, the method returns 
	 * <code>Double.POSITIVE_INFINITY</code>, because a camera with an infinite focal length would 
	 * produce no radial distortion.
	 *
	 * @throws IllegalStateException if the optical axis property of this CameraCalibration object 
	 * is unspecified.
	 * @throws IllegalArgumentException if the three points are not on a curved line that has the 
	 * optical axis on the inside of the curvature. In this case the three points can not have been
	 * on an originally straight line or the optical axis is set incorrectly.
	 */
	public double findFocalLength(Point2D endPoint1, Point2D endPoint2, Point2D middle) throws IllegalStateException, IllegalArgumentException
	{
		// Validate state
		validateState(OPT_AXIS);
		
				
		// Compute the sign of the curvature without any rectification
		int curvUncorrected = curvatureSign(endPoint1, endPoint2, middle);
		
		// Compute the curvature for a large focal length (i.e. little distortion)
		double h2 = 32768;
		if (curvUncorrected != curvatureSign(radial(endPoint1, h2), radial(endPoint2, h2), radial(middle, h2)))
		{
			// The focal length is 32768 or above. Hence radial distortion is negliable.
			return Double.POSITIVE_INFINITY;
		}
		
		
		/// Find the root of the curvature vs focal-length graph
		double h1 = h2/2;
		while (h1>32)
		{
			// Curvature changed?
			if (curvUncorrected != curvatureSign(radial(endPoint1, h1), radial(endPoint2, h1), radial(middle, h1)))
				break;
			
			// Decrease h
			h2 = h1;
			h1 /= 2;
		}
		
		if (h1<=32)
			// The curvature didn't change sign (even for a focal length with extreme distortion). 
			// Hence the given points were not on an originally straight line.
			throw new IllegalArgumentException("The specified points were not on a straight line or the optical axis is configured incorrecly.");
		
		// Binary search for the exact root
		while (h2-h1>1)
		{
			// Halve interval
			double hMiddle = (h1+h2)/2;
			
			// Drop half that does not contain the root
			if (curvUncorrected == curvatureSign(radial(endPoint1, hMiddle), radial(endPoint2, hMiddle), radial(middle, hMiddle)))
				h2 = hMiddle;
			else
				h1 = hMiddle;
		}
		
		// Strip decimal digits to not suggest that the value has a particularly high precision. 
		// Wee errors in the specified points result in large errors of the focal length.
		m_focalLength = Math.rint((h1+h2)/2);
		return m_focalLength;
	}

	/**
	 * Returns the sign of the curvature of a specified curve line. Note that all points are given
	 * in screen coordinates where the positive direction of x is right and y is <i>down</i>.
	 *
	 * @param endPoint1 the first end point of the line.
	 * @param endPoint2 the second end point of the line.
	 * @param middle a point on the curve between the two end points.
	 * 
	 * @return <code>-1</code> if the line curves to the right, or 1 otherwise. In theory the sign of
	 * the curvature could be 0, but as this is very unlikely due to numeric inprecision, this case is 
	 * excluded. 
	 */
	private static int curvatureSign(Point2D endPoint1, Point2D endPoint2, Point2D middle)
	{
		// Normal vector of straigt line between endpoints (not in unit length), sticking out to 
		// the left when seen from endPoint1.
		double normalX = endPoint2.getY() - endPoint1.getY();
		double normalY = -(endPoint2.getX() - endPoint1.getX());
		
		// Dot product with the vector from endPoint1 to middle. This is positive if middle is left 
		// of the straight line connecting the endpoints, and this is when the curved line bends to
		// the right.
		double dotProduct = normalX * (middle.getX() - endPoint1.getX()) +
							normalY * (middle.getY() - endPoint1.getY());
		
		// Dot product is positive if line curves to the right, so return -1 in that case
		return dotProduct>0 ? -1 : 1;
	}

	/**
	 * Sets the focal length of this CameraProjection.
	 * @param h the new focal length. May be Double.POSITIVE_INFINITY to flag no radial distortion.
	 */
	public void setFocalLength(double h)
	{
		m_focalLength = h;
	}

	/**
	 * Returns the focal length of this CameraProjection.
	 * @return the current focal length. If the camera image is not radially distorted (or to a 
	 * negliable amount) <code>Double.POSITIVE_INFINITY</code> is returned. 
	 */
	public double getFocalLength()
	{
		return m_focalLength;
	}

	/**
	 * <p>Rectifies radial distortion of the specified coordinates. The only fixpoint of this 
	 * transformation is the optical axis. 
	 * <p>Warning: This method requires <code>validateState(OPT_AXIS) == true</code>.
	 * @param coord the distorted coordinates.
	 * @return a new instance of <code>Point2D.Double</code> containing the coordinates without 
	 * radial distortion.
	 */
	private Point2D.Double radial(Point2D coord, double h)
	{
		// Translate origin to optical axis
		double x = coord.getX() - m_optAxisX;
		double y = coord.getY() - m_optAxisY;
		
		// To polar coordinates
		double radius = Math.sqrt(x*x + y*y);
		double angle = Math.atan(y/x);
		angle = x<0 ? angle+Math.PI : angle;
		
		// Rectify radius ( = h * sinh(radius/h) )
		radius = radius/h;
		radius = h * (Math.exp(radius)-Math.exp(-radius)) / 2;
		
		// Back to cartesian coordinates and translate back to normal origin
		x = radius*Math.cos(angle) + m_optAxisX;
		y = radius*Math.sin(angle) + m_optAxisY;
		return new Point2D.Double(x, y);
	}


	/** 
	 * <p>Computes the vanishing point of the projection from the two specified lines. The lines have
	 * to be parallel in the scene. In the projection however they won't be parallel if the camera
	 * is tilted. The result from this method will also e stored in this CameraProjection.
	 *
	 * @param direction specifies the orientation of the lines. Has to be one of the following 
	 * constants: <code>VP_HORIZONTAL</code> or <code>VP_VERTICAL</code>.
	 * @param l1p1 the first point on the first line. The points can be permuted as long as points
	 * from the same line are l<i>X</i>p1 and l<i>X</i>p2 for a constant <i>X</i>.
	 * @param l1p2 the seconde point on the first line. 
	 * @param l2p1 the first point on the second line.
	 * @param l2p2 the second point on the second line.
	 * 
	 * @throws IllegalStateException if the optical axis or focal length property of this 
	 * CameraProjection object is unspecified.
	 * @throws IllegalArgumentException if <code>direction</code> is not VP_HORIZONTAL or VP_VERTICAL.
	 */
	public Point2D findVanishingPoint(int direction, Point2D l1p1, Point2D l1p2, Point2D l2p1, Point2D l2p2) throws IllegalStateException, IllegalArgumentException
	{
		// Validate state
		validateState(FOCAL_LENGTH);
		
		// Validate arguments
		if (direction != VP_HORIZONTAL && direction != VP_VERTICAL)
			throw new IllegalArgumentException("The argument direction can only be VP_HORIZONTAL or VP_VERTICAL.");

		
		// Rectify radial distortion of points
		l1p1 = radial(l1p1, m_focalLength);
		l1p2 = radial(l1p2, m_focalLength);
		l2p1 = radial(l2p1, m_focalLength);
		l2p2 = radial(l2p2, m_focalLength);
		
		// Copy points into an array with the following semantics: 
		//  - the first dimension index stands for x (0) and y (1),
		//  - the second dimension index is first (0) or second (1) line,
		//  - the third dimension index is the line's point index.
		double[][][] point = { { {l1p1.getX(), l1p2.getX()}, {l2p1.getX(), l2p2.getX()} }, 
								{ {l1p1.getY(), l1p2.getY()}, {l2p1.getY(), l2p2.getY()} } };
		
		// The computation for the vertical vanishing point is the same as the computation for the 
		// horizontal vanishing point with x and y coordinates swapped. This is exploit by the 
		// following algorithm. It bases on a coordinate system with an axis parallel and and axis
		// perpendicular to the horizontal/vertical direction. Those virtual axes are mapped to the
		// real x and y axes through the variables and parAx and perpAx, which are indices on the 
		// first dimension of the array defined above.
		// To read the code assume the case VP_HORIZONTAL (constant 0) where parAxis is the x axis
		// and perpAxis is the y axis.
		int parAxis = direction;
		int perpAxis = 1-direction;
		
		// Gradient of lines (difference in perpAxis direction divided by difference in parAxis direction)
		double m0 = (point[perpAxis][0][1] - point[perpAxis][0][0]) / (point[parAxis][0][1] - point[parAxis][0][0]);
		double m1 = (point[perpAxis][1][1] - point[perpAxis][1][0]) / (point[parAxis][1][1] - point[parAxis][1][0]);
		
		// Difference in perpAxis direction at the root of the parAxis
		double difference = (point[perpAxis][1][0] - point[parAxis][1][0] * m1)
						  - (point[perpAxis][0][0] - point[parAxis][0][0] * m0);
		
		// Use the "angle" between the two lines (the differences in gradient) to compute where in 
		// parAxis direction the difference would be zero
		double[] vanishPoint = new double[2];
		vanishPoint[parAxis] = difference / (m0-m1);
		
		// The line equation of either lines gives the perpAxis component of the vanishing point
		vanishPoint[perpAxis] = (vanishPoint[parAxis] - point[parAxis][0][0]) * m0 + point[perpAxis][0][0];
		
		
		// Store and return result
		m_vanishPtX[direction] = vanishPoint[0];
		m_vanishPtY[direction] = vanishPoint[1];
		return new Point2D.Double(m_vanishPtX[direction], m_vanishPtY[direction]);
	}

	/**
	 * Sets the horizontal or vertical vanishing point.
	 * @param direction the vanishing point to be set: VP_HORIZONTAL or VP_VERTICAL.
	 * @param vp the new vanishing point or <code>null</code> to invalidate the vanishing point.
	 * @throws IllegalArgumentExceptiont if <code>direction</code> is not VP_HORIZONTAL or VP_VERTICAL.
	 */
	public void setVanishingPoint(int direction, Point2D vp) throws IllegalArgumentException
	{
		// Validate arguments
		if (direction != VP_HORIZONTAL && direction != VP_VERTICAL)
			throw new IllegalArgumentException("The argument direction can only be VP_HORIZONTAL or VP_VERTICAL.");
		
		// Use internal double version
		if (vp == null)
			setVanishingPoint(direction, Double.NaN, Double.NaN);
		else
			setVanishingPoint(direction, vp.getX(), vp.getY());
	}

	/**
	 * Sets the horizontal or vertical vanishing point and updates the dependant parameters. This 
	 * method does not verify the argument <code>direction</code>.
	 * @param direction the vanishing point to be set: VP_HORIZONTAL or VP_VERTICAL.
	 * @param x the <i>x</i> coordinate of the vanishing point.
	 * @param y the <i>y</i> coordinate of the vanishing point.
	 */
	private void setVanishingPoint(int direction, double x, double y)
	{
		if (Double.isNaN(x) || Double.isNaN(y))
		{
			// Invalidate both coordinates
			m_vanishPtX[direction] = Double.NaN;
			m_vanishPtY[direction] = Double.NaN;
		}
		else
		{
			// Set values
			m_vanishPtX[direction] = x;
			m_vanishPtY[direction] = y;
		}
	}

	/**
	 * Returns the horizontal or vertical vanishing point.
	 * @param direction the vanishing point to be queried: VP_HORIZONTAL or VP_VERTICAL.
	 * @return the selected vanishing point or null if it is unspecified.
	 */
	public Point2D getVanishingPoint(int direction)
	{
		// Check existance
		if (Double.isNaN(m_vanishPtX[direction]))
			return null;
		
		// Return vanishing point
		return new Point2D.Double(m_vanishPtX[direction], m_vanishPtY[direction]);
	}

	/**
	 * Returns the principal point. The principal point has the same y coordinate as the  horizontal
	 * vanishing point and the same x coordinate as the vertical vanishing point.
	 * @return the principal point or null if one of the vanishing points is not defined in this 
	 * CameraProjection object.
	 */
	public Point2D getPrincipalPoint()
	{
		// Check existance
		if (Double.isNaN(m_vanishPtX[VP_VERTICAL]) || Double.isNaN(m_vanishPtX[VP_HORIZONTAL]))
			return null;
		
		// Return value
		return new Point2D.Double(m_vanishPtX[VP_VERTICAL], m_vanishPtY[VP_HORIZONTAL]);
	}

	/**
	 * Returns the inverse of the distance from the principal point to the vanishing point. The 
	 * distance is given a negative sign if the vanishing point is left or above the principal point
	 * (in consistency with the screen coordinate system).
	 * 
	 * @param direction specifies which of the vanishing point. One of: VP_HORIZONTAL, VP_VERTICAL.
	 * @return the inverse of the distance from the principal point to the vanishing point or 
	 * <code>Double.NaN</code> if one of the vanishing points is not defined in this 
	 * CameraProjection object.
	 * @throws IllegalArgumentException if direction is not VP_HORIZONTAL or VP_VERTICAL.
	 */
	public double getVPDistanceInverse(int direction) throws IllegalArgumentException
	{
		// Validate argument
		if (direction != VP_HORIZONTAL && direction != VP_VERTICAL)
			throw new IllegalArgumentException("The argument direction can only be VP_HORIZONTAL or VP_VERTICAL.");

		// Check existance
		if (Double.isNaN(m_vanishPtX[VP_VERTICAL]) || Double.isNaN(m_vanishPtX[VP_HORIZONTAL]))
			return Double.NaN;
		
		// Compute the inverse of the distance
		if (direction == VP_HORIZONTAL)
			return 1.0 / (m_vanishPtX[VP_HORIZONTAL] - m_vanishPtX[VP_VERTICAL]);
		else
			return 1.0 / (m_vanishPtY[VP_VERTICAL] - m_vanishPtY[VP_HORIZONTAL]);
	}

	/**
	 * <p>Rectifies radial and tilt distortion of a point in image coordinates.
	 * <p>Warning: This method requires <code>validateState(VANISH_PTS) == true</code>.
	 *
	 * @param p the point in image coordinates.
	 * @return a new <code>Point2D.Double</code> object containting the rectified coordinates.
	 */
	private Point2D.Double tilt(Point2D p)
	{
		// Rectify radial distortion (this creates a new Point2D.Double instance)
		Point2D.Double rect = radial(p, m_focalLength);
		
		// Translate point to use principal point as origin (m_vanishPtX[VP_VERTICAL], m_vanishPtY[VP_HORIZONTAL])
		rect.x -= m_vanishPtX[VP_VERTICAL];
		rect.y -= m_vanishPtY[VP_HORIZONTAL];
		
		// Rectify tilt
		double nominator = (1 - m_vanishP*rect.x - m_vanishQ*rect.y);
		rect.x /= nominator;
		rect.y /= nominator;
		
		// Translate back to normal reference point
		rect.x += m_vanishPtX[VP_VERTICAL];
		rect.y += m_vanishPtY[VP_HORIZONTAL];
		return rect;
	}


	/**
	 * Sets the affine tranformation from rectified image coordinates to real-world coordinates.
	 * This method has no (permanent) effect if a coordinate map is used.
	 * @see #useCoordinateMap()
	 */
	public void setAffineTransforms(double scaleX, double offsetX, double scaleY, double offsetY)
	{
		// Store x transformation
		if (Double.isNaN(scaleX) || Double.isNaN(offsetX))
		{
			m_transformX[0] = Double.NaN;
			m_transformX[1] = Double.NaN;
		}
		else
		{
			m_transformX[0] = scaleX;
			m_transformX[1] = offsetX;
		}

		// Store x transformation
		if (Double.isNaN(scaleY) || Double.isNaN(offsetY))
		{
			m_transformY[0] = Double.NaN;
			m_transformY[1] = Double.NaN;
		}
		else
		{
			m_transformY[0] = scaleY;
			m_transformY[1] = offsetY;
		}
	}
	
	/**
	 * Invalidates the affine transform. This method has no (permatent) effect if a coordinate map
	 * is used.
	 * @see #useCoordinateMap()
	 */
	public void invalidateAffineTransforms()
	{
		m_transformX[0] = Double.NaN; m_transformX[1] = Double.NaN;
		m_transformY[0] = Double.NaN; m_transformY[1] = Double.NaN;
	}
	
	/**
	 * Returns the affine transformation from rectified image coordinates to real-world coordinates.
	 * @return an array <code>{a, b}</code> of that define the transformation x' = ax + b, or 
	 * <code>null</code> if the transformation is unspecified.
	 */
	public double[] getAffineTransformX()
	{
		// Update transformation if using a coordinate map
		if (m_coordMap != null)
			m_coordMap.commitIfPossible();
		
		return Double.isNaN(m_transformX[0]) ? null : m_transformX;
	}
	
	/**
	 * Returns the affine transformation from rectified image coordinates to real-world coordinates.
	 * @return an array <code>{a, b}</code> of that define the transformation y' = ay + b, or 
	 * <code>null</code> if the transformation is unspecified.
	 */
	public double[] getAffineTransformY()
	{
		// Update transformation if using a coordinate map
		if (m_coordMap != null)
			m_coordMap.commitIfPossible();
		
		return Double.isNaN(m_transformY[0]) ? null : m_transformY;
	}


	/**
	 * <p>Uses a coordinate map for this <code>CameraProjection</code> instance. If a coordinate map 
	 * is currently in use, this method has no effect. The <code>CameraProjection.CoordinateMap</code> 
	 * instance can be obtained with {@link #getCoordinateMap()}.
	 * <p>If the affine transformations have been defined by {@link #setAffineTransforms(double,
	 * double,double,double)} before, use {@link CameraProjection.CoordinateMap#restoreFromTransforms()}.
	 * @see CameraProjection.CoordinateMap
	 */
	public void useCoordinateMap()
	{
		if (m_coordMap == null)
			m_coordMap = new CameraProjection.CoordinateMap();
	}
	
	/**
	 * Returns the coordinate map for this <code>CameraProjection</code> instance or <code>null</code>
	 * if none is used.
	 * @return the current coordinat map.
	 */
	public CameraProjection.CoordinateMap getCoordinateMap()
	{
		return m_coordMap;
	}
	
	/**
	 * Sets the coordinate map for this <code>CameraProjection</code> instance. To ensure that 
	 * the affine transformations mirror the changes, use {@link CameraProjection.CoordinateMap#commit()}.
	 * @param map the new coordinate map, or <code>null</code> to use no coordinate map.
	 */
	public void setCoordinateMap(CameraProjection.CoordinateMap map)
	{
		m_coordMap = map;
	}


	/**
	 * Transforms a point in image coordinates into real-world coordinates.
	 * @param p the point in image coordinates.
	 * @return a new <code>Point2D.Double</code> containing the equivalent in real-world coordinates.
	 * @throws IllegalStateException if the optical axis, the focal length, a vanishing point or an 
	 * affine transformation is undefined. If a coordinate map is used, the exception is thrown if 
	 * there insufficient map entries (see {@link CameraProjection.CoordinateMap}).
	 */
	public Point2D.Double transform(Point2D p) throws IllegalStateException
	{
		// Validate entire state
		validateState(ALL);
		
		// Transform point
		return transform(p.getX(), p.getY());
	}
	
	/**
	 * <p>Transforms a point in image coordinates into real-world coordinates. 
	 * <p>Warning: This method requires <code>validateState(ALL) == true</code>.
	 * @param x the <i>x</i> coordinate in the image coordinate frame.
	 * @param y the <i>y</i> coordinate in the image coordinate frame.
	 * @return the coordinates in the real-world coordinate system.
	 */
	private Point2D.Double transform(double x, double y)
	{
		// Translate origin to optical axis
		x -= m_optAxisX;
		y -= m_optAxisY;
		
		// To polar coordinates
		double radius = Math.sqrt(x*x + y*y);
		double angle = Math.atan(y/x);
		angle = x<0 ? angle+Math.PI : angle;
		
		// Rectify radius ( = h * sinh(radius/h) )
		radius = radius/m_focalLength;
		radius = m_focalLength * (Math.exp(radius)-Math.exp(-radius)) / 2;
		
		// Back to cartesian coordinates, translate origin to principal point
		x = radius*Math.cos(angle) + m_optAxisX - m_vanishPtX[VP_VERTICAL];
		y = radius*Math.sin(angle) + m_optAxisY - m_vanishPtY[VP_HORIZONTAL];
		
		// Rectify tilt
		double nominator = (1 - m_vanishP*x - m_vanishQ*y);
		x /= nominator;
		y /= nominator;
		
		// Translate back to normal reference point
		x += m_vanishPtX[VP_VERTICAL];
		y += m_vanishPtY[VP_HORIZONTAL];
		
		// Affine transformations to real-world coordinates
		x = x * m_transformX[0] + m_transformX[1];
		y = y * m_transformY[0] + m_transformY[1];
		
		return new Point2D.Double(x, y);
	}

	/**
	 * <p>Transforms a from real-world coordinates to image coordinates. This is the inverse 
	 * of {@link #transform(Point2D)}. The method uses all 
	 * <p>Warning: This method requires <code>validateState(ALL) == true</code>.
	 * @param x the <i>x</i> coordinate in the real-world coordinate frame.
	 * @param y the <i>y</i> coordinate in the real-world coordinate frame.
	 * @return the coordinates in the image coordinate system.
	 */
	private Point2D.Double inverseTransform(double x, double y)
	{
		// Inverse affine tranformations
		x = (x - m_transformX[1]) / m_transformX[0];
		y = (y - m_transformY[1]) / m_transformY[0];
		
		// Translate origin to principal point
		x -= m_vanishPtX[VP_VERTICAL];
		y -= m_vanishPtY[VP_HORIZONTAL];
		
		// Inverse tilt rectification
		double nominator = (1 + m_vanishP*x + m_vanishQ*y);
		x /= nominator;
		y /= nominator;
		
		// Translate origin to optical axis
		x = x + m_vanishPtX[VP_VERTICAL] - m_optAxisX;
		y = y + m_vanishPtY[VP_HORIZONTAL] - m_optAxisY;
		
		// To polar coordinates
		double radius = Math.sqrt(x*x + y*y);
		double angle = Math.atan(y/x);
		angle = x<0 ? angle+Math.PI : angle;
		
		// Inverse radius rectification ( = h * arcsinh(radius/h) )
		radius = radius/m_focalLength;
		radius = m_focalLength * Math.log(radius + Math.sqrt(radius*radius + 1));
		
		// Back to cartesian coordinates with normal origin
		x = radius*Math.cos(angle) + m_optAxisX;
		y = radius*Math.sin(angle) + m_optAxisY;
		
		return new Point2D.Double(x, y);
	}


	/**
	 * Tests whether calibration parameters are valid up to the specified step. This method also 
	 * updates the derived parameters <code>m_vanishP</code> and <code>m_vanishQ</code>, and the 
	 * affine transformations if a <code>CoordinateMap</code> is used.
	 * 
	 * @param upTo the last calibration step (in the normal order) that is expected to be done.
	 * @return <code>true</code> if the method exits without an exception.
	 * @throws IllegalStateException if parameters are missing. The messages of the exceptions are
	 * "The <i>parameter</i> is undefined.", so that it can be used as part of a message for the 
	 * user.
	 */
	private boolean validateState(int upTo) throws IllegalStateException
	{
		// Special case when validating all and a coordinate map is in use
		if (upTo == ALL && m_coordMap != null)
		{
			// Update the affine transformations (this method is public and therefore does own state 
			// validation, including validateState(VANISH_PTS))
			m_coordMap.commit();
			return true;
		}
		
		
		// Validate up to desired step
		if (upTo >= OPT_AXIS)
		{
			// Optical axis
			if (Double.isNaN(m_optAxisX))
				throw new IllegalStateException("The optical axis is undefined.");
		}
		
		if (upTo >= FOCAL_LENGTH)
		{
			// Focal length
			if (Double.isNaN(m_focalLength))
				throw new IllegalStateException("The focal length is undefined.");
		}
		
		if (upTo >= VANISH_PTS)
		{
			// Vanishing points
			if (Double.isNaN(m_vanishPtX[VP_HORIZONTAL]) && Double.isNaN(m_vanishPtX[VP_VERTICAL]))
				throw new IllegalStateException("The vanishing points are undefined.");
			if (Double.isNaN(m_vanishPtX[VP_HORIZONTAL]))
				throw new IllegalStateException("The horizontal vanishing point is undefined.");
			if (Double.isNaN(m_vanishPtX[VP_VERTICAL]))
				throw new IllegalStateException("The vertical vanishing point is undefined.");
			
			// Calculate p and q
			m_vanishP = getVPDistanceInverse(VP_HORIZONTAL);
			m_vanishQ = getVPDistanceInverse(VP_VERTICAL);
		}
		
		if (upTo == ALL)
		{
			// Scaling
			if (Double.isNaN(m_transformX[0]) || Double.isNaN(m_transformY[0]))
				throw new IllegalStateException("The affine transformations are undefined.");
			if (Double.isNaN(m_transformX[0]))
				throw new IllegalStateException("The affine transformation for x is undefined.");
			if (Double.isNaN(m_transformY[0]))
				throw new IllegalStateException("The affine transformation for y is undefined.");
		}
		
		return true;
	}


	/**
	 * Draws the curved line and marks the inside of the curvature.
	 *
	 * @param endPoint1 the first end point of the line.
	 * @param endPoint2 the second end point of the line.
	 * @param middle a point on the curve between the two end points.
	 * @param output the <code>Graphics</code> object into which the line is drawn.
	 */
	public static void markCurvature(Point2D endPoint1, Point2D endPoint2, Point2D middle, Graphics output)
	{
		// Draw the line 
		drawLine(output, endPoint1, middle);
		drawLine(output, middle, endPoint2);
		
		// Compute the curvature of the curved line
		int curvature = curvatureSign(endPoint1, endPoint2, middle);
		
		// Normal vector sticking out to the left (seen from endPoint1)
		double normalX = endPoint2.getY() - endPoint1.getY();
		double normalY = -(endPoint2.getX() - endPoint1.getX());
		
		// Normalize to 5 pixels length and mirror to the right if curvature negative
		double normalLength = Math.sqrt(Math.pow(normalX,2)+Math.pow(normalY,2));
		normalX = (normalX/normalLength) * 5 * curvature;
		normalY = (normalY/normalLength) * 5 * curvature;

		// Draw perpendicular mark
		drawLine(output, middle.getX(), middle.getY(), middle.getX()+normalX, middle.getY()+normalY);
	}
	
	/**
	 * Draws the real-world coordinate system grid into an image. The lines used for calibration
	 * should clearly match the drawn grid. This is a way to very the calibration.
	 * @param size the size of grid cells. The grid will be evenly spaced in x and y direction.
	 * @param output the <code>Graphics</code> object into which the grid is drawn. If a clipping
	 * area is specified, the grid will fill this entire area. Otherwise a 10x10 grid is drawn.
	 * @throws IllegalStateException if the optical axis, the focal length or one of the vanishing 
	 * points is undefined, or if there are not at least two map entries with defined real-world 
	 * x coordinates and two with real-world y coordinates.
	 */
	public void drawGrid(double size, Graphics output) throws IllegalStateException
	{
		// Validate all parameters
		validateState(ALL);
		
		// Determine boundaries
		int minX, maxX, minY, maxY;

		Rectangle clip = output.getClipBounds();
		if (clip != null)
		{
			// Transform the corners
			Point2D.Double c0 = transform(new Point(clip.x, clip.y));
			inverseTransform(c0.getX(), c0.getY());
			Point2D.Double c1 = transform(new Point(clip.x+clip.width, clip.y));
			inverseTransform(c1.getX(), c1.getY());
			Point2D.Double c2 = transform(new Point(clip.x+clip.width, clip.y+clip.height));
			inverseTransform(c2.getX(), c2.getY());
			Point2D.Double c3 = transform(new Point(clip.x, clip.y+clip.height));
			inverseTransform(c3.getX(), c3.getY());
			
			// Maximum and minimum coordinates
			minX = (int) Math.floor(Math.min(Math.min(Math.min(c0.getX(), c1.getX()), c2.getX()), c3.getX()) / size);
			minY = (int) Math.floor(Math.min(Math.min(Math.min(c0.getY(), c1.getY()), c2.getY()), c3.getY()) / size);
			maxX = (int) Math.ceil(Math.max(Math.max(Math.max(c0.getX(), c1.getX()), c2.getX()), c3.getX()) / size);
			maxY = (int) Math.ceil(Math.max(Math.max(Math.max(c0.getY(), c1.getY()), c2.getY()), c3.getY()) / size);
		}
		else
		{
			// 10x10 grid
			minX = 0; maxX = 10;
			minY = 0; maxY = 10;
		}
		
		// Draw horizontal grid lines
		for (int x = minX; x <= maxX; x++)
		{
			// Start of a line segment
			Point2D.Double p0 = inverseTransform(size*x, size*minY);
			
			for (int y = minY; y < maxY; y++)
			{
				// End of a line segment
				Point2D.Double p1 = inverseTransform(size*x, size*(y+1));
				drawLine(output, p0, p1);
				
				// End is new start
				p0 = p1;
			}
		}

		// Draw vertical grid lines
		for (int y = minY; y <= maxY; y++)
		{
			// Start of a line segment
			Point2D.Double p0 = inverseTransform(size*minX, size*y);
			
			for (int x = minX; x < maxX; x++)
			{
				// End of a line segment
				Point2D.Double p1 = inverseTransform(size*(x+1), size*y);
				drawLine(output, p0, p1);
				
				// End is new start
				p0 = p1;
			}
		}
	}
	
	/**
	 * Equivalent of {@link Graphics#drawLine(int,int,int,int)} for Point2D endpoints.
	 * @param g a valid <code>Graphics</code> object.
	 * @param p1 the first point.
	 * @param p2 the second point.
	 */
	private static void drawLine(Graphics g, Point2D p1, Point2D p2)
	{
		drawLine(g, p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}
	
	/**
	 * Equivalent of {@link Graphics#drawLine(int,int,int,int)} for double coordinates.
	 * @param g a valid <code>Graphics</code> object.
	 * @param x1 the first point's <i>x</i> coordinate.
	 * @param y1 the first point's <i>y</i> coordinate.
	 * @param x2 the second point's <i>x</i> coordinate.
	 * @param y2 the second point's <i>y</i> coordinate.
	 */
	private static void drawLine(Graphics g, double x1, double y1, double x2, double y2)
	{
		// Mathmatical rounding
		g.drawLine((int) Math.round(x1), (int) Math.round(y1), (int) Math.round(x2), (int) Math.round(y2));
	}



	//------------------------------------------------------------------------------------------------
	// ListModel for the list of image/real-world coordinate pairs
	
	/**
	 * <p>Map from image coordinates to real-world coordinates for convenient setting of the affine 
	 * transformations. 
	 * <p>This class provides an alternative way to calibrate the affine transformations. It is 
	 * computed from a map from image coordinates to scene coordinates. Each transformation
	 * needs at least two map entries to be defined (an affine transformation between scalar values
	 * is equivalent two a line). If more map entries are defined, this class uses a least square 
	 * line fitting algorithm to calculate the transformation.
	 * <p>There are two separate affine transformations for x and y coordinates. However every map 
	 * entry (no matter whether it is for the x or the y transformation) need to provide both 
	 * coordinates in the image coordinate system. This is because the transformations are from 
	 * <i>rectified</i> image coordinates to scene coordinates, and the image coordinates can't
	 * be rectified independently. It is possible to create a single map entry for both 
	 * transformations by also providing both coordinates in the scene coordinate system.
	 * 
	 * <p>For convenience reasons this class extends {@link javax.swing.AbstractListModel} and hence
	 * can be used with a {@link javax.swing.JList} to display the map entries.
	 *
	 * @see CameraProjection
	 * @see javax.swing.JList
	 */
	public class CoordinateMap extends javax.swing.AbstractListModel
	{
		// Data members
		/** Image coordinates. */
		private Vector imCoord = new Vector();
		/** Real-world coordinates. */
		private Vector reCoord = new Vector();
		
		// Data statistics
		/** Number of real world <i>x</i> coordinates. Each image/real-world coordinates pair can 
			either specify x and y real world coordinates or only one of them. Both image 
			coordinates have to be valid. */
		private int entryCountX = 0;
		/** Number of real world <i>y</i> coordinates. */
		private int entryCountY = 0;
		
		// Formatting
		
		/** Format for the string representation. */
		java.text.NumberFormat imF, reF;
		
		
		
		/**
		 * Creates a map for image/real-world coordinate pairs to define the affine transformations. 
		 */
		private CoordinateMap()
		{
			// Formatter for image coordinates
			imF = java.text.NumberFormat.getInstance();
			imF.setGroupingUsed(false);
			
			// Formatter for real-world coordinates
			reF = java.text.NumberFormat.getInstance();
			imF.setGroupingUsed(false);
		}
		
		
		/**
		 * Adds a map entry of image/real-world coordinates. The real-world coordinates may specify
		 * x, y or both coordinates. Unspecified coordinates are flagged by Double.NaN.
		 *
		 * @param imageCoord a point in the image coordinate system.
		 * @param realCoord the same point, or one coordinate of it, in the real-world coordinate 
		 * system.
		 *
		 * @throws NullPointerException if any of the parameters is <code>null</code>.
		 * @throws IllegalArgumentException if not both coordinates of <code>imageCoord</code> and at 
		 * least one coordinate of <code>realCoord</code> are specified.
		 */
		public void add(Point2D.Double imageCoord, Point2D.Double realCoord) throws NullPointerException, IllegalArgumentException
		{
			// Validate arguments
			boolean specifiedRealX = ! Double.isNaN(realCoord.x);
			boolean specifiedRealY = ! Double.isNaN(realCoord.y);

			if (Double.isNaN(imageCoord.x) || Double.isInfinite(imageCoord.x) || Double.isNaN(imageCoord.y) || Double.isInfinite(imageCoord.y))
				throw new IllegalArgumentException("The image point coordinates must be finite numbers.");
			if (! specifiedRealX && ! specifiedRealY)
				throw new IllegalArgumentException("At least one of the real-world coordinates must be specified.");
			if (Double.isInfinite(realCoord.x) || Double.isInfinite(realCoord.y))
				throw new IllegalArgumentException("The real-world coordinates can't be infinity.");
			
			// Add map entry
			this.imCoord.add(imageCoord);
			this.reCoord.add(realCoord);
			
			// Increase counters
			if (specifiedRealX)
				this.entryCountX++;
			if (specifiedRealY)
				this.entryCountY++;
			
			// Fire event
			fireIntervalAdded(this, getSize(), getSize());
		}

		/**
		 * Returns the image coordinates from a map entry.
		 * @param index the index of the entry. Must be between 0 and <code>getSize()</code>.
		 * @throws ArrayIndexOutOfBoundsException if the index was invalid.
		 */
		public Point2D.Double getImageCoord(int index) throws ArrayIndexOutOfBoundsException
		{
			return (Point2D.Double) imCoord.get(index);
		}

		/**
		 * Returns the real-world coordinates from a map entry. Unless the map entry is a combined
		 * entry for both transformations, one of the coordinates will be invalid (i.e. Double.NaN)
		 * @param index the index of the entry. Must be in the range of <code>0..getSize()</code>.
		 * @throws ArrayIndexOutOfBoundsException if the index was invalid.
		 */
		public Point2D.Double getRealCoord(int index) throws ArrayIndexOutOfBoundsException
		{
			return (Point2D.Double) reCoord.get(index);
		}

		/**
		 * Removes a map entry.
		 * @param index the index of the entry. Must be in the range of <code>0..getMapCount()</code>.
		 * @throws ArrayIndexOutOfBoundsException if the index was invalid.
		 */
		public void remove(int index) throws ArrayIndexOutOfBoundsException
		{
			// Remove points (retaining the real-world coordinates)
			imCoord.remove(index);
			Point2D.Double realCoord = (Point2D.Double) reCoord.remove(index);

			// Decrease counters
			if (! Double.isNaN(realCoord.x))
				entryCountX--;
			if (! Double.isNaN(realCoord.y))
				entryCountY--;
				
			fireIntervalRemoved(this, index, index);
		}
		
		/**
		 * Returns the number of entries in the map.
		 * @return the number of entries in the map.
		 */
		public int getSize()
		{
			return this.imCoord.size();
		}
		

		/**
		 * Set the format for the string representation of map entries. This method is useful if 
		 * this instance is used as a {@link javax.swing.ListModel} for a {@link javax.swing.JList}.
		 * @param imageFractDigits the maximum number of fraction digits in the formatted output of 
		 * the image coordinates.
		 * @param realFractDigits the same for the real-world coordinates.
		 */
		public void setNumberFormat(int imageFractDigits, int realFractDigits)
		{
			imF.setMinimumFractionDigits(0);
			imF.setMaximumFractionDigits(imageFractDigits);
			reF.setMinimumFractionDigits(0);
			reF.setMaximumFractionDigits(realFractDigits);
		}

		/**
		 * Returns the string representation of a map entry.
		 * @return (image coordinates) <code>mapped to</code> (real-world coordinates).
		 * @throws ArrayIndexOutOfBoundsException if the index was invalid.
		 */
		public Object getElementAt(int index) throws ArrayIndexOutOfBoundsException
		{
			// Get the coordinate pair
			Point2D.Double imPt = (Point2D.Double) this.imCoord.get(index);
			Point2D.Double rePt = (Point2D.Double) this.reCoord.get(index);
			
			// Format entry
		 	String entry =  imF.format(imPt.x) + "/" + imF.format(imPt.y) + " mapped to ";
			if (Double.isNaN(rePt.x))
				entry += "y coord. " + reF.format(rePt.y);
			else if (Double.isNaN(rePt.y))
				entry += "x coord. " + reF.format(rePt.x);
			else
				entry += reF.format(rePt.x) + "/" + reF.format(rePt.y);
			
			return entry;
		}
		
		
		/**
		 * Sets the affine transformations of the parent <code>CameraProjection</code> to comply 
		 * with the stored coordinate map. The result of this method will be different every time
		 * one of optical axis, focal length, the vanishing points, or the coodinate map changes.
		 * It is however not necessary to call this method, the <code>CameraProjection</code> parent
		 * will invoke it whenever the affine transformations are needed.
		 *
		 * @throws IllegalStateException if the optical axis, focal length or one of the vanishing 
		 * points is not defined, or if the map contains less than two entries each for the 
		 * transformations.
		 */
		public void commit() throws IllegalStateException
		{
			// Reset affine transformations
			invalidateAffineTransforms();

			// Validate state of parent
			validateState(VANISH_PTS);

			// Validate number of map entries
			if (this.entryCountX < 2 && this.entryCountY < 2)
				throw new IllegalStateException("The number of image/real-world coordinate pairs is insufficient.");
			if (this.entryCountX < 2)
				throw new IllegalStateException("The number of real-world x coordinates in image/real-world map entries is insufficient.");
			if (this.entryCountY < 2)
				throw new IllegalStateException("The number of real-world y coordinates in image/real-world map entries is insufficient.");


			// Rectify radial and tilt distortion
			int countTotal = this.imCoord.size();
			double[] allImX = new double[countTotal];
			double[] allImY = new double[countTotal];

			for (int i = 0; i < countTotal; i++)
			{
				// Recify and store in array
				Point2D.Double p = tilt((Point2D.Double) this.imCoord.get(i));
				allImX[i] = p.x;
				allImY[i] = p.y;
			}

			// Get map pairs with real-world x coordinate
			double[] imX = new double[this.entryCountX];
			double[] reX = new double[this.entryCountX];
			int writeIndex = 0;

			for (int i = 0; i < countTotal; i++)
			{
				// Copy if real-world x is defined
				double x = ((Point2D.Double) this.reCoord.get(i)).getX();
				if (! Double.isNaN(x))
				{
					imX[writeIndex] = allImX[i];
					reX[writeIndex] = x;
					writeIndex++;
				}
			}

			// The same for real-world y coordinates
			double[] imY = new double[this.entryCountY];
			double[] reY = new double[this.entryCountY];
			writeIndex = 0;

			for (int i = 0; i < countTotal; i++)
			{
				// Copy if real-world y is defined
				double y = ((Point2D.Double) this.reCoord.get(i)).getY();
				if (! Double.isNaN(y))
				{
					imY[writeIndex] = allImY[i];
					reY[writeIndex] = y;
					writeIndex++;
				}
			}

			// The affine transformation descibes a line so that is what is fitted
			m_transformX = fitLine(imX, reX);
			m_transformY = fitLine(imY, reY);
		}
		
		/**
		 * Sets the affine transformations of the parent <code>CameraProjection</code> to comply 
		 * with the stored coordinate map, but only if this is possible. If this object's or the
		 * parent's state is invalid, the affine transformations will be set to unspecified.
		 */
		public void commitIfPossible()
		{
			try
			{
				commit();
			}
			catch (IllegalStateException e)
			{
				// The transformations are invalidated already (first thing in commit())
			}
		}
		
		/**
		 * Least-square line fitting. Standard algorithm using matrix division.
		 * @param x the x coordinates of the points.
		 * @param y the y coordinates of the points.
		 * @return the coefficients {a1, a0} of the fitted line so that y = a1*x + a0.
		 */
		private double[] fitLine(double[] x, double[] y)
		{
			// The algorithm is as follows:
			//  - extend vector x to a matrix X by adding a column of ones
			//  - calculate inv(X'*X)*X'*y

			// Calculate X'*X and store in matrix ( a b ) 
			//                                    ( b d )
			double a = 0.0, b = 0.0, d = x.length;
			for (int i=0; i<x.length; i++)
			{
				a += x[i] * x[i];
				b += x[i];
			}

			// Calculate X'*y and store in vector ( e )
			//                                    ( f )
			double e = 0.0, f = 0.0;
			for (int i=0; i<x.length; i++)
			{
				e += x[i] * y[i];
				f += y[i];
			}

			// Calculate inv(X'*X)*X'*y which is ___1___   (  d -b )   ( e )
			//                                   (ad-bb) * ( -b  a ) * ( f )
			double factor = 1/(a*d-b*b);
			return new double[]{factor*(d*e-b*f), factor*(-b*e+a*f)};
		}


		/**
		 * Creates map entries that mirror the affine transformations stored in the 
		 * <code>CameraProjection</code> parent. Any existing map entries are removed. If any of the
		 * calibration parameters is unspecified, the map will be empty.
		 */
		public void restoreFromTransforms()
		{
			// Reset current map
			imCoord.clear();
			reCoord.clear();
			entryCountX = 0;
			entryCountY = 0;
			
			// Validate state
			try
			{
				// Optical axis, focal length and vanishing points
				validateState(VANISH_PTS);
				
				// Affine transformations
				if (Double.isNaN(m_transformX[0]) || Double.isNaN(m_transformY[0]))
					return;
			}
			catch (IllegalStateException e)
			{
				// Optical axis, focal length or a vanishing point unspecified
				return;
			}
			
			// Add two entries
			add(inverseTransform(0.0, 0.0), new Point2D.Double(0.0, 0.0));
			add(inverseTransform(100.0, 100.0), new Point2D.Double(100.0, 100.0));
		}
	}
}
