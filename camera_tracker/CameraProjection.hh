
#ifndef _CAMERA_PROJECTION_HH_
#define _CAMERA_PROJECTION_HH_

// Standard output macros and exception
#include "Exception.hh"

// Property reader
#include "OptionsFile.hh"

#include <list>

using namespace std;


struct Point
{
  double x;
  double y;
  
  Point() {};
  Point(double x, double y) : x(x), y(y) {};
};

struct AffineTransformation1D
{
  double scale;
  double offset;
  
  AffineTransformation1D() {};
  AffineTransformation1D(double scale, double offset) : 
    scale(scale), offset(offset) {};
};

struct ResolutionMap
{
  int width;
  int height;
  double scaleX;
  double offsetX;
  double scaleY;
  double offsetY;
  
  ResolutionMap() {};
  ResolutionMap(int width, int height, double scaleX, double offsetX, 
		                       double scaleY, double offsetY) :
    width(width), height(height),
    scaleX(scaleX), offsetX(offsetX), scaleY(scaleY), offsetY(offsetY) {};
};



/**
 * Class providing translation between image coordinates and scene coordinates.
 * There is a JAVA gui to create the calibration file and a C++ program 
 * (rescalib) to create the resolution mapping file.
 */
class CameraProjection
{
  // Calibration parameters
  /** Optical axis. */
  double m_optAxisX, m_optAxisY;
  /** Focal length. */
  double m_focalLength;
  /** Principal point. */
  double m_principPtX, m_principPtY;
  /** Inverse of the distance of the horizontal vanishing point. */
  double m_vanishP;
  /** Inverse of the distance of the vertical vanishing point. */
  double m_vanishQ;
  /** Affine transformation from rectified image coordinates to scene 
      coordinates. */
  double m_scaleX, m_offsetX, m_scaleY, m_offsetY;
  
  /** Calibration is complete. */
  bool m_complete;
  
  // Resolution mapping
  /** Resolution at which the projection was calibrated. */
  int m_calibWidth, m_calibHeight;
  /** List of resolution maps. WARNING: must use list because m_resMap points 
      to an element of the list. */
  list<ResolutionMap> m_resMaps;
  /** Current resolution map. */
  const ResolutionMap* m_resMap;
  
  /** Scaling approximation for length transformations. */
  double m_scaleApprox;
  
public:
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
  CameraProjection(int calibrationWidth, int calibrationHeight,
		   Point opticalAxis, double focalLength, 
		   Point principalPoint, 
		   double horizontalVPDistance, 
		   double verticalVPDistance, 
		   AffineTransformation1D transformationX, 
		   AffineTransformation1D transformationY);
	 
  /**
   * Creates a new CameraProjection instance with parameters from an 
   * OptionsReader. Use isValid() to dertermine whether the parameters were
   * complete and the instance is usable.
   *
   * \param calibration an OptionsReader instance with the required 
   * calibration parameters.
   */
  CameraProjection(const OptionsReader& calibration);
  
  /**
   * Returns true if this CameraProjection instance is usable for coordinate
   * transformations. This is if the calibration parameters are complete.
   * \return true if the CameraProjection instance is initialized.
   */
  inline bool isValid() const { return m_complete; }
  
  /**
   * Reads the resolution mapping from the specified OptionsReader. Resolution 
   * mapping ensures that any resolution can be used, even if the camera
   * calibration was only done for one resolution.
   * \param maps the OptionsReader containing the resolution maps.
   */
  void readResolutionMaps(const OptionsReader& maps);
  
  /**
   * Sets the current camera resolution. This installs the resolution map for
   * the specified resolution. If that map is not available, the default map
   * is used (no scaling, no offset).
   * \return true if a resolution map is available for that resolution.
   */
  bool setResolution(int width, int height);
  
  /**
   * Transforms a point in image coordinates into scene coordinates. 
   * Note: This method requires isValid() == true.
   * \param image the point in image coordinates.
   * \return the point in scene coordinates.
   */
  void transform(double x, double y, double& xTransformed, double& yTransformed) const;
  
  inline Point transform(const double& x, const double& y) const
  {
    Point p;
    transform(x, y, p.x, p.y);
    return p;
  }
  
  /**
   * Transforms a point in scene coordinates into image coordinates. 
   * This is the inverse function of transform(Point). Note: This method 
   * requires isValid() == true.
   * \param real the point in scene coordinates.
   * \return the point in image coordinates.
   */
  void inverseTransform(double x, double y, double& xTransformed, double& yTransformed) const;
  inline Point inverseTransform(const double& x, const double& y) const
  {
    Point p;
    inverseTransform(x, y, p.x, p.y);
    return p;
  }
  
  
  /**
   * Transforms a length in pixels into the equivalent (approximately) in 
   * scene coordinates. The approximation is good if the projection has little
   * tilt.
   * \param pixel the length in pixels.
   * \return the approximate equivalent in scene coordinate system units.
   */
  inline double transformLength(double pixel) const 
    { return pixel / m_scaleApprox; };

  /**
   * Transofrs a length in scene cooridinate units into the equivalent
   * length in pixels (approximately). The approximation is good if the 
   * projecion has little tilt. 
   * \param lenght the length in scene coordinate units.
   * \return the approximate equivalent in scene coordinate system units.
   */
  inline double inverseTransformLength(double length) const 
    { return length * m_scaleApprox; };

private:
  /**
   * Validates the current parameter and sets m_complete to true if everything
   * seems alright. NB: most parameters can not be validated formally.
   * Also calculates the scaling approximation.
   * \return m_complete
   */
  bool validate();
  
  /**
   * Updates the scaling approximation. 
   */
  void updateScale();
  
  /**
   * Parses a double value from a string (using strtod) and throws an int if 
   * the expression does obviously not contain a double. This is the case if 
   * the string is the empty string "".
   * \param expr the expression to be parsed.
   * \return the parsed value.
   * \trow int if the parsing (obviously) fails
   */
  double parseDouble(string expr) throw (int);
};


#endif
