
#include <stdlib.h>
#include <unistd.h>
#include <sys/times.h>
#include <sys/param.h>

#include "Tracker.hh"
#include "OptionsReader.hh"
#include "Webcam.hh"
#include "ImageFormat.hh"
#include "ClusteringImageProcessor.hh"
#include "RobotManager.hh"
#include "Robot.hh"
#include "PostTrackProcessor.hh"
#include "Exception.hh"

#include "ccvt.h"  // colour conversion routines
#include "opencv/highgui.h"  // gui windows

#ifdef DEBUG
#include "Timer.hh"
#endif

#define WIN_TITLE "NRS.camtrack (click to terminate)"

using namespace std;


// Define static variable to stop the tracker
bool Tracker::m_exitNow = false;

//----------------------------------------------------------------------

// Handle mouse events occurring on the GUI
void cvMouseCallback(int event, int x, int y, int flags)
{
  if (event == CV_EVENT_LBUTTONUP)
    {
      x = y = flags = 0; // removes compiler warning about unused ints
      Tracker::halt();
      _INFO_("User clicked to terminate");
    }
}

//----------------------------------------------------------------------

Tracker::Tracker(const OptionsReader& options, 
		 ClusteringImageProcessor& cip, 
		 CameraProjection& proj)
  : m_cam(NULL),
    m_width(0), m_height(0), m_fps(0),
    m_buf(NULL),
    m_ip(cip),
    m_defaultClip(2),
    m_proj(proj),
    m_rm(NULL),
    m_pingRate(0),
    m_imageRate(0)
{
  // Init camera (this sets m_cam, m_height, m_width, m_fps and m_buf)
  initCamera(options);


  // Inform CameraProjection about resolution
  if (!proj.setResolution(m_width, m_height))
    _ABORT_("Resolution " << m_width << "x" << m_height 
	    << " not in resolution calibration file.");
  
  // Initialize robot manager
  m_rm = new RobotManager(options, cip.getPalette(), proj);


  // Assemble a list of all spot colours and size, find maximum clip
  list<int> colours, sizes;
  for (RobotManager::RobotIterator robot = m_rm->begin(); robot != m_rm->end(); robot++)
    for (Robot::AnchorIterator anchor = (*robot)->begin(); anchor != (*robot)->end(); anchor++)
      {
	colours.push_back((*anchor)->colour);
	sizes.push_back((*anchor)->pixelSize());
	m_defaultClip = max(m_defaultClip, (*anchor)->pixelClip());
      }
  colours.sort();
  colours.unique();
  sizes.sort();
  sizes.unique();
  
  _INFO_("Default clip is " << m_defaultClip);
  
  
  // Read the settings for the edge observation and the whole image scanning
  bool observeEdge = options.value(SCAN_EDGE) == "true";
  int scanDuration = atoi(options.value(SCAN_WHOLE).c_str());
  
  // Pass image buffer, spot statistics and scan settings to image processor
  m_ip.init(*m_buf, m_width, m_height, colours, sizes, observeEdge, scanDuration);


  // Read textual output rate
  m_pingRate = atoi(options.value(PING_RATE).c_str());
  
  // Read image output rate
  m_imageRate = atoi(options.value(IMAGE_RATE).c_str());

  // Read the image diplay settings
  m_ip.showClassification = options.value(IMAGE_CLASSIFIED) == "true";
  m_ip.classifyAll = options.value(CLASSIFY_ALL) == "true";

  // Initialize CV font
  cvInitFont(&m_font, CV_FONT_VECTOR0, 0.25, 0.25, 0.0, 1);

  // Open the windows for image output
  if (m_imageRate)
  {
    cvNamedWindow(WIN_TITLE, 1);
    cvSetMouseCallback(WIN_TITLE, cvMouseCallback);
  }
}

//----------------------------------------------------------------------

Tracker::~Tracker()
{
  // Delete webcam
  delete m_cam;
  
  // Release image buffer
  if (m_buf) cvReleaseImage(&m_buf);

  // Delete RobotManager
  delete m_rm;
}

//----------------------------------------------------------------------

void Tracker::initCamera(const OptionsReader& options)
{
  // Create a Webcam instance
  if (options.contains(CAM_DEVICE))
    m_cam = new Webcam(options.value(CAM_DEVICE));
  else
    m_cam = new Webcam();
  
  // Try to open camera
  if (!m_cam->openDevice())
    _ABORT_("Failed to open camera");
  
  // Diagnostics
  std::string model;
  int w, h;
  if (m_cam->getModel(model) && m_cam->getMaxResolution(w,h))
    _INFO_("Detected: \"" << model << "\", supports max image size"
	   << ": " << w << " x " << h);
  
  // Read image format and frame rate from configuration
  int fps = (options.contains(CAM_FPS)) ? atoi(options.value(CAM_FPS).c_str()) : 15;

  string imfStr = (options.contains(CAM_RESOLUTION)) ?
    options.value(CAM_RESOLUTION) : sQCIF.getName();

  // Set the resultion on the camera
  if (imfStr == VGA.getName())
    m_cam->setResolution(VGA, fps);
  else if (imfStr == CIF.getName())
    m_cam->setResolution(CIF, fps);
  else if (imfStr == QCIF.getName())
    m_cam->setResolution(QCIF, fps);
  else if (imfStr == sQCIF.getName())
    m_cam->setResolution(sQCIF, fps);
  else if (imfStr == SIF.getName())
    m_cam->setResolution(SIF, fps);
  else if (imfStr == QSIF.getName())
    m_cam->setResolution(QSIF, fps);
  else if (imfStr == "MAX_IMAGE")
    m_cam->setResolutionMaxSize();
  else if (imfStr == "MAX_FPS")
    m_cam->setResolutionMaxFPS();

  // Now read which resolution was acutally set
  m_cam->getResolution(m_width, m_height, m_fps);

  // initial camera shared memory
  m_cam->initCapture();
  
  // Create image buffer
  CvSize size;
  size.height = m_height;
  size.width = m_width;
  m_buf = cvCreateImage(size, IPL_DEPTH_8U, 3);
  if (!m_buf)
    _ABORT_("Failed to create buffer for image capturing.");
}

//----------------------------------------------------------------------

inline void Tracker::getFrame(IplImage& dest) const
{
  static unsigned char* rawI;

  rawI = m_cam->nextFrame();

  // Okay, this is a problem. Not all raw images are in bgr24
  // format. Need to make this generic somehow. Perhaps base which
  // routine to use on the palette code
  ccvt_420p_bgr24(m_width, m_height, rawI, dest.imageData);
}

//----------------------------------------------------------------------

void Tracker::start()
{
  _INFO_(" *** Pretrack *** ");
  
  // Get camera image
  getFrame(*m_buf);
  
  // Process raw image
  m_ip.processImage();

  // Find all spots in the image
  list<ImageSpot*> unmatchedSpots;
  m_ip.findSpots(unmatchedSpots);
  _INFO_("Search in initial image yielded " << unmatchedSpots.size() << " spots");
  
  // Calculate the scene coordinates
  for (list<ImageSpot*>::iterator spot = unmatchedSpots.begin(); 
       spot != unmatchedSpots.end(); 
       spot++)
    {
      m_proj.transform((*spot)->x, (*spot)->y, (*spot)->sc_x, (*spot)->sc_y);
    }
  RobotManager::printSpotSummary(unmatchedSpots);
  
  // Match the spots to robots (unmatched remain in the list)
  m_rm->matchSpots(unmatchedSpots);


  // Display image?
  if (m_imageRate)
    cvShowImage(WIN_TITLE, m_buf);
  
  // Enter into the OpenCV event dispatcher. Need this for window
  // events to be processed (such as the clicks on the control window)
  cvWaitKey(1);



  // Main tracking part
  _INFO_(" *** Starting tracker *** ");
  int frameCount = 0;

  // Used for calculating the effective frame rate
  clock_t timerStart;
  clock_t timerEnd;
  timerStart = times(0);
  
  // Used for calculating the total elapsed time
  clock_t trackerStart = times(0);
  
  
  // this is the main tracking loop
  while (!m_exitNow)
  {
    // Get next image from the camera
    getFrame(*m_buf);
    double elapsed = (times(0) - trackerStart) / sysconf(_SC_CLK_TCK);
    
    // Reset just-in-time classification (or classify all if configured to)
    m_ip.processImage();
    
    
    // ** Update all anchor and robot locations (where possible) **
    m_rm->trackRobots(m_ip);
    
    // Track the locations of umatched spots
    for (list<ImageSpot*>::iterator spot = unmatchedSpots.begin(); 
	 spot != unmatchedSpots.end(); )
      {
	bool found = m_ip.trackSpot(**spot, m_defaultClip);
	if (!found)
	  {
	    // Discard the spot (also set iterator to next spot)
	    ImageSpot* ptr = *spot;
	    spot = unmatchedSpots.erase(spot);
	    delete ptr;
	  }
	else
	  spot++;
      }


    // Remember number of spots in the list before the search
    unsigned int listSize = unmatchedSpots.size();
    
    // Look for new spots at the edge of the image
    m_ip.observeEdge(m_defaultClip, unmatchedSpots);
    
    // Scan part of the inside of the image for new spots
    m_ip.partialScan(unmatchedSpots);
    
    // Try to match the new spots to robots
    if (unmatchedSpots.size() > listSize)
      {
	// Calculate the scene coordinates of the spots
	for (list<ImageSpot*>::iterator spot = unmatchedSpots.begin(); 
	     spot != unmatchedSpots.end(); 
	     spot++)
	  {
	    m_proj.transform((*spot)->x, (*spot)->y, (*spot)->sc_x, (*spot)->sc_y);
	  }
	
	// ... and try to match them
	m_rm->matchSpots(unmatchedSpots);
      }


    // Display frame number and fps
    if (m_pingRate and (frameCount % m_pingRate == 0))
      {
	// Frame number
	_INFO_("Tracker: frame #" << frameCount);
	
	// Calculate fps
	if (frameCount)
	  {
	    timerEnd = times(0);
	    _INFO_("Actual fps = "<< 
		   (double) (m_pingRate * sysconf(_SC_CLK_TCK)) /
		   (timerEnd - timerStart));
	    timerStart = timerEnd;
	  }
      }
    
    // Display the image
    if (m_imageRate and !(frameCount % m_imageRate))
      {
        annotateImage(unmatchedSpots);
        cvShowImage(WIN_TITLE, m_buf);
      };

    // Pass control to the OpenCV event dispatcher. Need this for window
    // events to be processed (such as the clicks on the control window)
    cvWaitKey(1);


    // Finally invoke the PostTrackProcessors update function
    for (vector<PostTrackProcessor*>::iterator iter = m_ptp.begin();
         iter != m_ptp.end(); iter++)
      (*iter)->process(m_rm->begin(), m_rm->end(), frameCount, elapsed);
    
    // Increase frame counter
    frameCount++;
  }
}

//----------------------------------------------------------------------

void Tracker::halt()
{
  m_exitNow = true;
}

//----------------------------------------------------------------------

void Tracker::addPTP(PostTrackProcessor& ptp)
{
  m_ptp.push_back(&ptp);
}

//----------------------------------------------------------------------

void Tracker::annotateImage(const list<ImageSpot*>& unmatched)
{
  static CvPoint ps;
  static CvPoint pt;
  static CvPoint ptext;

  // Loop over each robot, and for each robot, loop over each
  // spot.
  for (RobotManager::RobotIterator r = m_rm->begin(); r != m_rm->end(); r++)
    {
      for (Robot::AnchorIterator s = (*r)->begin(); s != (*r)->end(); s++)
        if ( (*s)->spot != NULL)
          { 
            ps.x = (*s)->spot->x - (*s)->pixelClip();
            ps.y = (*s)->spot->y - (*s)->pixelClip();
            pt.x = (*s)->spot->x + (*s)->pixelClip();
            pt.y = (*s)->spot->y + (*s)->pixelClip();
            ptext.x = ps.x - 2;
            ptext.y = ps.y - 2;
            
            ostringstream os;
            os << (*r)->label << "." << (*s)->index;
            
            if (! (*s)->located)
              {
		// Estimated
                cvRectangle(m_buf, ps, pt, CV_RGB(230,0,0), 1);
                cvPutText(m_buf, os.str().c_str(), 
                          ptext,
                          &m_font, CV_RGB(255,0,0));
              }
            else
              {
		// Located
                cvRectangle(m_buf, ps, pt, CV_RGB(0,230,0), 1);
                cvPutText(m_buf, os.str().c_str(), 
                          ptext,
                          &m_font, CV_RGB(0,255,0));
                
                ps.x = (*s)->spot->x;
                ps.y = (*s)->spot->y;
                cvCircle(m_buf, ps, (*s)->spot->radius, CV_RGB(0, 255, 0), 1);
              }
          }

      if ((*r)->located())
        {
	  Point p = m_proj.inverseTransform((*r)->x, (*r)->y);
	  ps.x = (int) p.x;
	  ps.y = (int) p.y;

          // Circle at the centre
          cvCircle(m_buf, ps, 3, CV_RGB(0,0,255), -1);

	  if ( !(*r)->singleSpot )
	    {
	      p = m_proj.inverseTransform((*r)->x - sin((*r)->heading)*100, 
					  (*r)->y + cos((*r)->heading)*100);
	      pt.x = (int) p.x;
	      pt.y = (int) p.y;

	      // Line pointing forwards
	      cvLine(m_buf, ps, pt, CV_RGB(0,0,255), 1);
	    }
	}
    }

  // Draw circle around unmatched spots
  for (list<ImageSpot*>::const_iterator s = unmatched.begin(); s != unmatched.end(); s++)
    {
      ps.x = (*s)->x;
      ps.y = (*s)->y;
      cvCircle(m_buf, ps, (*s)->radius, CV_RGB(0, 255, 0), 1);
    }
}

//----------------------------------------------------------------------

void Tracker::trackerExit()
{
  _ERROR_("Tracking cannot start until all spots for all robots"
          " are identified in the inital frame. Please adjust"
          " viewing conditions / robot specifications and try again.");

  // if either of the guis are present, they control when the tracker is
  // terminated
  if (m_imageRate)
    {
      _INFO_("Switching to debug mode.\n"
             << " *** CLICK DISPLAY WINDOW TO EXIT ***");

      CvPoint ptext;
      CvFont font;

      ptext.x = 20;
      ptext.y = 60;
      cvInitFont(&font, CV_FONT_VECTOR0, 0.5, 0.5, 0.0, 2);
      ostringstream os;
      os << "DEBUG MODE. CLICK TO EXIT";
    
      while (!m_exitNow)
        {
          // Get next image from the camera
          getFrame(*m_buf);

          // Display processed image?
	  m_ip.processImage();
	  cvPutText(m_buf, 
		    os.str().c_str(), ptext, &font, CV_RGB(255,0,0));
	  cvShowImage(WIN_TITLE, m_buf);
	}

      // Enter into the OpenCV event dispatcher. Need this for window
      // events to be processed (such as the clicks on the control window)
      cvWaitKey(1);
    }
}

