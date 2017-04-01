
#include <cstdlib>
#include <cmath>
#include <sstream>
#include "CameraProjection.hh"
#include "Exception.hh"

using namespace std;


//----------------------------------------------------------------------

/**
 * Creates a new CameraProjection instance with the specified parameters.
 * \param opticalAxis the pixel at which the optical axis of the lens 
 * intersects with the image plain
 * \param focalLength the focal length of the camera (typically between 500 
 * and 1000)
 * \param principalPoint the principal point of the camera's tilt 
 * (intersection of the heights of the vanishing points).
 * \param horizontalVPDistance the distance between the principal point and
 * and the horizontal vanishing point.
 * \param verticalVPDistance the distance between the principal point and
 * the vertical vanishing point.
 * \param transformationX the final transformation for the x coordinate
 * from rectified image coordinates to the scene coordinate frame.
 * \param transformationY the same for y coordinates.
 * \param calibrationResolution the resolution for which the 
 * calibration parameters were created.
 */
CameraProjection::CameraProjection(int calibrationWidth, int calibrationHeight, Point opticalAxis, double focalLength, Point principalPoint, double horizontalVPDistance, double verticalVPDistance, AffineTransformation1D transformationX, AffineTransformation1D transformationY)
  :  m_optAxisX(opticalAxis.x),
     m_optAxisY(opticalAxis.y),
     m_focalLength(focalLength),
     m_principPtX(principalPoint.x),
     m_principPtY(principalPoint.y),
     m_vanishP(1.0/horizontalVPDistance),
     m_vanishQ(1.0/verticalVPDistance),
     m_scaleX(transformationX.scale),
     m_offsetX(transformationX.offset),
     m_scaleY(transformationY.scale),
     m_offsetY(transformationY.offset),
     m_calibWidth(calibrationWidth),
     m_calibHeight(calibrationHeight)
{
  // Validate and initialize
  if ( validate() )
    updateScale();
}

//----------------------------------------------------------------------

/**
 * Creates a new CameraProjection instance with parameters from an 
 * OptionsReader. Use isValid() to dertermine whether the parameters were
 * complete and the instance is usable.
 *
 * \param calibration an OptionsReader instance with the required 
 * calibration parameters.
 */
CameraProjection::CameraProjection(const OptionsReader& calibration)
{
  try
    {
      // Read in the member variables
      m_calibWidth = atoi(calibration.value("resolution.width").c_str());
      m_calibHeight = atoi(calibration.value("resolution.height").c_str());
      m_optAxisX = parseDouble(calibration.value("optical_axis.x"));
      m_optAxisY = parseDouble(calibration.value("optical_axis.y"));
      m_focalLength = parseDouble(calibration.value("focal_length"));
      m_principPtX = parseDouble(calibration.value("principal_point.x"));
      m_principPtY = parseDouble(calibration.value("principal_point.y"));
      m_vanishP = 1.0 / parseDouble(calibration.value("vanish_distance.horizontal"));
      m_vanishQ = 1.0 / parseDouble(calibration.value("vanish_distance.vertical"));
      m_scaleX = parseDouble(calibration.value("translate.x.scale"));
      m_offsetX = parseDouble(calibration.value("translate.x.offset"));
      m_scaleY = parseDouble(calibration.value("translate.y.scale"));
      m_offsetY = parseDouble(calibration.value("translate.y.offset"));
    }
  catch (int ex)
    {
      // Parsing error or entry missing
      _WARN_("CameraProjection configuration file is invalid or incomplete.");
      m_complete = false;
      return;
    }
  
  _DEBUG_("CameraProjection settings loaded:\n"
	  << "    calibRes=" << m_calibWidth << "x" << m_calibHeight
	  << "    optAxis=(" << m_optAxisX << "," << m_optAxisY << "), "
	  << "    focalLength=" << m_focalLength << ",\n"
	  << "\t prinipPt=(" << m_principPtX << "," << m_principPtY << "), "
	  << "p=" << m_vanishP << ", q=" << m_vanishQ << ",\n"
	  << "    scale=(*" << m_scaleX << " +" << m_offsetX 
	  << ", *" << m_scaleY << " +" << m_offsetY << ")\n");
  
  // Validation and initialization
  if ( validate() )
    updateScale();
}

//----------------------------------------------------------------------

/**
 * Validates the current parameter and sets m_complete to true if everything
 * seems alright. NB: most parameters can not be validated formally.
 * \return m_complete
 */
bool CameraProjection::validate()
{
  // Validate the focal length
  if (m_focalLength < 32)
    {
      // This can't be valid
      _WARN_("CameraProjection configuration is invalid (focal length < 32)");
      m_complete = false;
    }
  
  // Validate the calibration resolution
  else if (m_calibWidth <= 0 || m_calibHeight <= 0)
    {
      _WARN_("The calibration resolution is invalid (width or height <= 0)");
      m_complete = false;
    }
  
  // The rest can't be validated formally
  else
    {
      // Create and use default resolution map
      m_resMaps.push_front(ResolutionMap(m_calibWidth, m_calibHeight, 
					 1.0, 0.0, 1.0, 0.0));
      m_resMap = &m_resMaps.front();
      
      // Object valid
      m_complete = true;
    }
  
  return m_complete;
}

//----------------------------------------------------------------------

/**
 * Updates the scaling approximation. 
 */
void CameraProjection::updateScale()
{
  // Use the length from the optical axis (in current resolution) to the top 
  // of the image
  double optAxisX = (m_optAxisX - m_resMap->offsetX) / m_resMap->scaleX;
  double optAxisY = (m_optAxisY - m_resMap->offsetY) / m_resMap->scaleY;
  
  // Calculate scaling approximation
  Point top = transform(optAxisX, 0.0);
  Point axis = transform(optAxisX, optAxisY);
  m_scaleApprox = optAxisY / sqrt(pow(top.x - axis.x, 2) 
				    + pow(top.y - axis.y, 2));
}

//----------------------------------------------------------------------

/**
 * Parses a double value from a string (using strtod) and throws an int if 
 * the expression does obviously not contain a double. This is the case if 
 * the string is the empty string "".
 * \param expr the expression to be parsed.
 * \return the parsed value.
 * \trow int if the parsing (obviously) fails
 */
double CameraProjection::parseDouble(string expr) throw (int)
{
  // Parse a double value
  const char *expr_cstr = expr.c_str();
  char *parseEnd;
  double parsed = strtod(expr_cstr, &parseEnd);
  
  // Check whether anything was parsed
  if (expr_cstr == parseEnd)
    throw 0;
  else
    return parsed;
}

//----------------------------------------------------------------------

/**
 * Reads the resolution mapping from the specified OptionsReader. Resolution 
 * mapping ensures that any resolution can be used, even if the camera
 * calibration was only done for one resolution.
 * \param maps the OptionsReader containing the resolution maps.
 */
void CameraProjection::readResolutionMaps(const OptionsReader& maps)
{
  // Reset current resolution map list
  m_resMaps.clear();
  m_resMaps.push_front(ResolutionMap(m_calibWidth, m_calibHeight, 1.0, 0.0, 1.0, 0.0));
  m_resMap = &m_resMaps.front();
  
  // Read the reference resolution
  int refWidth = atoi(maps.value("reference.width").c_str());
  int refHeight = atoi(maps.value("reference.height").c_str());
  if (refWidth * refHeight == 0)
    {
      _ERROR_("The resolution calibration file is invalid (doesn't contain"
		<< " reference resolution)!");
      return;
    }
  
  // Check that the resolution maps have been edited by hand
  if (maps.value("edited_by_hand") != "true")
    _WARN_("The resolution calibration file was not edited by hand. This is"
	   << " strongly recommended to correct the noisy result of the resolution"
	   << " calibration tool.");
  
  // Translation from reference to calibration resolution
  double calibScaleX, calibOffsetX;
  double calibScaleY, calibOffsetY;
  if (m_calibWidth == refWidth and m_calibHeight == refHeight)
    {
      calibScaleX = 1.0; calibOffsetX = 0.0;
      calibScaleY = 1.0; calibOffsetY = 0.0;
    }
  else
    // Look for map from calibration to reference resolution
    for (int i = 0; true; i++)
      {
	// Assemble identifier string
	ostringstream os;
	os << "res." << i << ".";
	
	// Map exists?
	if (!maps.contains(os.str()+"width") or !maps.contains(os.str()+"height"))
	  {
	    // Not found
	    _ERROR_("The resolution calibration file does not cover the resolution "
		   << m_calibWidth << "x" << m_calibHeight
		   << " for which the camera was calibrated!");
	    return;
	  }
	
	// Check if right map
	if (m_calibWidth == atoi(maps.value(os.str()+"width").c_str()) and
	    m_calibHeight == atoi(maps.value(os.str()+"height").c_str()))
	  try
	    {
	      // Read map from calibration to reference resolution
	      double scaleX = parseDouble(maps.value(os.str()+"map.x.scale"));
	      double offsetX = parseDouble(maps.value(os.str()+"map.x.offset"));
	      double scaleY = parseDouble(maps.value(os.str()+"map.y.scale"));
	      double offsetY = parseDouble(maps.value(os.str()+"map.y.offset"));
	      
	      // Invert map (the inverse was the one needed)
	      calibScaleX = 1/scaleX;
	      calibOffsetX = -offsetX/scaleX;
	      calibScaleY = 1/scaleY;
	      calibOffsetY = -offsetY/scaleY;
	      
	      // Create map for reference resolution
	      m_resMaps.push_back(ResolutionMap(refWidth, refHeight, 
                calibScaleX, calibOffsetX, calibScaleY, calibOffsetY));
	      
	      break;
	    }
	  catch (int exception)
	    {
	      _WARN_("The resolution calibration file is invalid (keys "
		     << " starting with \"" << os.str() << ".map.\" missing).");
	    }
      
      }

  // Load all resolution maps
  for (int i = 0; true; i++)
    
    {
      // Assemble identifier string
      ostringstream os;
      os << "res." << i << ".";
	
      // Map exists?
      if (!maps.contains(os.str()+"width") or !maps.contains(os.str()+"height"))
	// Exit loop
	break;
	
      // Read resolution
      ResolutionMap res;
      res.width = atoi(maps.value(os.str()+"width").c_str());
      res.height = atoi(maps.value(os.str()+"height").c_str());
	
      // Read map from this to reference resolution
      try
	{
	  double scaleX = parseDouble(maps.value(os.str()+"map.x.scale"));
	  double offsetX = parseDouble(maps.value(os.str()+"map.x.offset"));
	  double scaleY = parseDouble(maps.value(os.str()+"map.y.scale"));
	  double offsetY = parseDouble(maps.value(os.str()+"map.y.offset"));
	
	  // Calculate map from this to calibration resolution
	  res.scaleX = scaleX * calibScaleX;
	  res.offsetX = offsetX * calibScaleX + calibOffsetX;
	  res.scaleY = scaleY * calibScaleY;
	  res.offsetY = offsetY * calibScaleY + calibOffsetY;
	  
	  // Add to list
	  m_resMaps.push_back(res);

#ifdef DEBUG
	  _DEBUG_("Added resolution map for " << res.width << "x" << res.height
		  << ": scaleX=" << res.scaleX << ", offsetX=" << res.offsetX 
		  << ", scaleY=" << res.scaleY << ", offsetY=" << res.offsetY);
#endif
	}
      catch (int exception)
	{
	  _WARN_("The resolution calibration file is invalid (keys "
		 << " starting with \"" << os.str() << ".map.\" missing).");
	}
    }
}

//----------------------------------------------------------------------

/**
 * Sets the current camera resolution. This installs the resolution map for
 * the specified resolution. If that map is not available, the default map
 * is used (no scaling, no offset).
 * \return true if a resolution map is available for that resolution.
 */
bool CameraProjection::setResolution(int width, int height)
{
  // Reset resolution
  m_resMap = &m_resMaps.front();

  // Look map for resolution
  for (list<ResolutionMap>::const_iterator res = m_resMaps.begin();
       res != m_resMaps.end(); res++)
    {
      // Check if right map
      if (width == res->width and height == res->height)
	{
	  // Use this resolution transformation
	  m_resMap = &(*res);
	  updateScale();
	  return true;
	}
    }

  // No map for this resolution
  _ERROR_("The resolution calibration does not cover the resolution "
	  << width << "x" << height << "! Transformation to scene coordinate"
	  << " system will be incorrect! ");
  return false;
}

//----------------------------------------------------------------------

/**
 * Transforms a point in image coordinates into scene coordinates. 
 * Note: This method requires isValid() == true.
 * \param image the point in image coordinates.
 * \return the point in scene coordinates.
 */
void CameraProjection::transform(double x, double y, double& xRet, double& yRet) const
{
  // Validate state
  if (!isValid())
    return;
  
  // Translate to calibration resolution
  x = x * m_resMap->scaleX + m_resMap->offsetX;
  y = y * m_resMap->scaleY + m_resMap->offsetY;
  
  // Translate origin to optical axis
  x -= m_optAxisX;
  y -= m_optAxisY;
  
  // To polar coordinates
  double radius = sqrt(x*x + y*y);
  double angle = atan2(y, x);
  
  // Rectify radius
  radius = m_focalLength * sinh(radius / m_focalLength);
  
  // Back to cartesian coordinates, translate origin to principal point
  x = radius*cos(angle) + m_optAxisX - m_principPtX;
  y = radius*sin(angle) + m_optAxisY - m_principPtY;
  
  // Rectify tilt
  double nominator = (1 - m_vanishP*x - m_vanishQ*y);
  x /= nominator;
  y /= nominator;
  
  // Translate back to normal reference point
  x += m_principPtX;
  y += m_principPtY;
  
  // Affine transformations to scene coordinates
  xRet = x * m_scaleX + m_offsetX;
  yRet = y * m_scaleY + m_offsetY;
}

//----------------------------------------------------------------------

/**
 * Transforms a point in scene coordinates into image coordinates. 
 * This is the inverse function of transform(Point). Note: This method 
 * requires isValid() == true.
 * \param real the point in scene coordinates.
 * \return the point in image coordinates.
 */
void CameraProjection::inverseTransform(double x, double y, double& xRet, double& yRet) const
{
  // Inverse affine tranformations
  x = (x - m_offsetX) / m_scaleX;
  y = (y - m_offsetY) / m_scaleY;
  
  // Translate origin to principal point
  x -= m_principPtX;
  y -= m_principPtY;
  
  // Inverse tilt rectification
  double nominator = (1 + m_vanishP*x + m_vanishQ*y);
  x /= nominator;
  y /= nominator;
  
  // Translate origin to optical axis
  x = x + m_principPtX - m_optAxisX;
  y = y + m_principPtY - m_optAxisY;
  
  // To polar coordinates
  double radius = sqrt(x*x + y*y);
  double angle = atan2(y, x);
  
  // Inverse radius rectification
  radius = m_focalLength * asinh(radius/m_focalLength);
  
  // Back to cartesian coordinates with normal origin
  x = radius*cos(angle) + m_optAxisX;
  y = radius*sin(angle) + m_optAxisY;

  // Translate to current resolution
  xRet = (x - m_resMap->offsetX) / m_resMap->scaleX;
  yRet = (y - m_resMap->offsetY) / m_resMap->scaleY;
}


