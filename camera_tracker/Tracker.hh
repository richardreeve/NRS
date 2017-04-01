#ifndef _TRACKER_HH_
#define _TRACKER_HH_

#include <vector>
#include <list>

#include "opencv/cv.h"

// Forward declarations
class OptionsReader;
class Webcam;
class ClusteringImageProcessor;
class CameraProjection;
class RobotManager;
class ImageSpot;
class Robot;
class PostTrackProcessor;

// Keys associated with name & value pairs
#define CAM_DEVICE "camera_device"
#define CAM_RESOLUTION "image_format"
#define CAM_FPS "image_fps"
#define PING_RATE "ping_rate"
#define IMAGE_RATE "display_image_rate"
#define IMAGE_CLASSIFIED "display_image_classified"
#define CLASSIFY_ALL "classify_complete_image"
#define SCAN_EDGE "scan_edge"
#define SCAN_WHOLE "scan_image_rate"

/**
 * The class which coordinates image processing and robot tracking
 */
class Tracker
{
  // Tracker configuration file
  //  const OptionsReader& m_conf;

  // Module to capture the images
  Webcam* m_cam;
  // Camera image attributes
  int m_width, m_height, m_fps;
  // Buffer used for image capture
  IplImage* m_buf;

  // Module used to process images into cluster locations
  ClusteringImageProcessor& m_ip;
  // Clip for spots not associated with any robot
  int m_defaultClip;
  
  // Module used to translate pixel coordinates into image coordinates
  CameraProjection& m_proj;
  
  // Manager of the tracked robots
  RobotManager* m_rm;

  // Modules for screen or file output
  std::vector<PostTrackProcessor*> m_ptp;

  // Exit signal - causes processing to halt when set to true. Set by halt().
  static bool m_exitNow;

  // Rate at which to display textual output (0 for never)
  int m_pingRate;
  // Rate at which to display the image (0 for never)
  int m_imageRate;
  // OpenCV font used for image annotation
  CvFont m_font;


public:
  /** 
   * Create a tracker object, with configuration specified in the supplied 
   * options object. The initialization is complete if the constructor throws
   * no exception (may throw NRS::Base::Exception).
   * The caller retains responsibility for deleting the for the objects 
   * passed in as argument.
   */
  Tracker(const OptionsReader& options, 
	  ClusteringImageProcessor& cip, 
	  CameraProjection& proj);
  
  /**
   * Destructor. Deletes the Webcam and RobotManager objects and releases the
   * buffer for image capturing.
   */
  ~Tracker();
  
  /**
   * Start the tracker. This first scans the entire image for spots and tries
   * to match them to the robots. Then it starts the main tracking loop, which
   * continues until halt() is called.
   */
  void start();

  /**
   * Halt the continual image processing and robot tracking loops
   */
  static void halt();

  /**
   * Add a post track processor object. The caller retains responsibility 
   * to delete the PTP objects.
   */
  void addPTP(PostTrackProcessor& ptp);
  
  /**
   * Returns the Tracker's RobotManager.
   */
  RobotManager& getRobotManager() { return *m_rm; }

private:
  /** Copy constructor prohibited */
  Tracker(const Tracker&);

  /** Assignment operator prohibited */
  Tracker& operator=(const Tracker&);

  /** 
   * Attempts to initialize and open the camera. Also creates the buffer for
   * the captured images. Throws NRS::Base::Exception if things go wrong.
   */
  void initCamera(const OptionsReader& options);

  /**
   * Get image from camera, and convert to BGR24
   */
  inline void getFrame(IplImage& dest) const;

  /**
   * Draws a marker around all matched and unmatched spots. Estimated spots 
   * show only their clip region. 
   */
  void annotateImage(const std::list<ImageSpot*>& unmatched);

  /**
   * Displays and error message and control exits of the tracker.
   * NOT USED AT THE MOMENT
   */
  void trackerExit();
};

#endif
