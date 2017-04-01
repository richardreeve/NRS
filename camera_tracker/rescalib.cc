
#include "Exception.hh"
#include "Webcam.hh"
#include "ImageFormat.hh"
#include "ImageSpot.hh"
#include "ClusteringImageProcessor.hh"
#include "ccvt.h"  // colour conversion routines
#include "opencv/highgui.h"  // gui windows

#include <vector>
#include <list>
#include <utility>
#include <cmath>
#include <fstream>
#include <iomanip>

#define APP_NAME "rescalib"
#define MAX_SPOT_SIZE 40
#define WIN_TITLE "Resolution calibration (click to continue)"

using namespace std;


// Forward declaration
bool setResolution(const ImageFormat& format, Webcam& cam, IplImage*& buffer);
inline void processFrame(IplImage* buffer, Webcam& cam, const ImageFormat&, 
			 ClusteringImageProcessor& ip, list<ImageSpot*>& spots);
inline void display(IplImage* buffer, const list<ImageSpot*>& spots,
		    const ImageFormat& format);
void cvMouseCallback(int event, int x, int y, int flags);
bool matchSpots(list<ImageSpot*>& spots, const list<ImageSpot*>& refSpots,
		list< pair<ImageSpot*,ImageSpot*> >& pairs);
bool matchSpotRecursion(list<ImageSpot*>& spots, 
		list<ImageSpot*>::const_iterator refSpot, 
		list<ImageSpot*>::const_iterator refSpotsEnd,
			list< pair<ImageSpot*,ImageSpot*> >& pairs);
bool match(const ImageSpot& spot, const ImageSpot& refSpot, 
	   list< pair<ImageSpot*,ImageSpot*> > pairs);
float lineAngle(const ImageSpot& spot1, const ImageSpot& spot2);
void leastSquareFit(const list< pair<ImageSpot*,ImageSpot*> >& pairs, 
		    int ImageSpot::* memberVariable, double& offset, double& scale);


// Global variable to register mouse click on image
bool g_continue;

//----------------------------------------------------------------------

/**
 * Resolution calibration tool.
 *
 * This tool measures, how different resolutions of the same camera scale the
 * image. Additionally the image might be shifted by an offset. For resolutions
 * that are simple fractions of other resolutions this might be obvious (for
 * example - relative to 640x480 - 320x240 has typically no offset and a 
 * scaling factor of 2). But this is not always the case.
 *
 * The tool attempts to set different resolutions and searches coloured spots
 * in each of them. The first resolution that is set successfully is called
 * reference resolution (implicitly scaled by 1, no offset). In each  
 * subsequent resolutions a new search for spots is done and the result is
 * matched to the spots from the reference resolution. Once the locations of
 * spots are known for the current and the reference resolution, the offset and
 * scaling between them can be calculated (the algorithm is equivalent to line 
 * fitting).
 *
 *
 * To run the resolution calibration tool, type in 

 rescalib COLOUR_FILE COLOUR OUTPUT
 
 * where  
 *  - COLOUR_FILE is the name of the colour calibration file, ending usually '.m',
 *  - COLOUR is the colour of the spots used as defined in COLOUR_FILE,
 *  - OUTPUT is the output file.
 * 
 * Then place at least two COLOUR-coloured spots in the view of the camera. 
 * To yield good results make sure that at least one pair of spots have a big 
 * difference in their x coordinates (on the screen), and one pair in their y
 * coordinates (i.e. one spot in the top-left and one spot in the bottom-right
 * corner). The classified camera image is shown in a window.
 * 
 * It is fairly important that the spots appear round in the classified image. 
 * Otherwise it can happen that very small spots are found next to larger 
 * spots. This is a problem because the tool insists on identifying all spots 
 * that were found in the reference resolution, and the tiny attached spots 
 * tend to disappear in lower resolutions. If attached spots only appear every 
 * now and then, just try to run the tool again if problems occur.
 * 
 * Click into the window or press ENTER in the console to runs through all 
 * resolutions and measures the scaling and offset as described. The process
 * should not stop unless there were tiny attached spots in the image in 
 * reference resolution (solution see above), or spots that were visible in 
 * the reference resolution can indeed not be seen in the current resolution. 
 * When this happens, either type in 'i' and ENTER in the console to ignore that 
 * resolution or move the spots so that they can be seen and start again (by
 * clicking or ENTER).
 * 
 * Once that process is completed successfully, it is strongly recommended to 
 * edit the output file by hand to straighten noisy values. An offset up to 3 
 * is likely to be a noisy 0. Scale factors have much smaller errors, they
 * are usually clearly less than 0.1. But values like 1.98 should obviously
 * be 2. For odd values it is worth checking whether the correct value can be
 * calculated by dividing the resolution's height (width) by the reference
 * height (width).
 * 
 * @author Tobias Oberlies
 */
int main(int argc, char* argv[])
{
  if (argc < 4)
    {
      cout << "Usage: " << APP_NAME << " COLOURFILE COLOUR OUTFILE\n"
	   << "Measures the pixel mapping between different resolutions of a camera.\n"
	   << "Place spots of COLOUR (as defined in COLOURFILE) in the camera view!\n";
      exit(EXIT_FAILURE);
    }


  // Open output file
  ofstream out(argv[3]);
  if (!out)
    _ABORT_("Failed to open file \"" << argv[3] << "\" for writing the results to.");


  // Initialize webcam
  Webcam cam;
  if (!cam.openDevice())
    _ABORT_("Failed to open camera");
  
  // Diagnostics
  string model;
  int w, h;
  if (cam.getModel(model) && cam.getMaxResolution(w,h))
    {
      _INFO_(" ** Detected: \"" << model << "\", supports max image size"
	     << ": " << w << " x " << h << "\n");
      out << "camera_model=" << model << "\nedited_by_hand=false\n\n";
    }
  else
    out << "camera_model=unknown\nedited_by_hand=false\n\n";


  // Create list of formats
  vector<ImageFormat> formats(6, VGA);
  formats[0] = VGA;   // 640x480
  formats[1] = SIF;   // 320x240
  formats[2] = QSIF;  // 160x120
  formats[3] = CIF;   // 352x288
  formats[4] = QCIF;  // 176x144
  formats[5] = sQCIF; // 128x96


  // Prepare image processing
  ClusteringImageProcessor ip(argv[1]);
  ip.classifyAll = true;
  ip.threshold = 80;

  // Just one colour
  list<int> colour(1, ip.getPalette().cluster(argv[2]));
  if (colour.front() == -1)
    _ABORT_("Colour '" << argv[2] << "' not defined in colour file '" 
	   << argv[1] << "'");

  // Any size
  list<int> sizes;
  for (int i = 2; i <= MAX_SPOT_SIZE; i++)
    sizes.push_back(i);


  // Initialize window for output
  cvNamedWindow(WIN_TITLE, 1);
  cvSetMouseCallback(WIN_TITLE, cvMouseCallback);
  

  // For the loop
  const ImageFormat* refFormat = NULL;  // Not to be deleted, will point at formats[x]
  list<ImageSpot*> refSpots;
  IplImage* buffer = NULL;
  int index = 0;
  
  // Try each of the formats
  for (vector<ImageFormat>::const_iterator format = formats.begin();
       format != formats.end(); format++)
    if (setResolution(*format, cam, buffer))
      {
	// Set image processor to that resolution
	ip.init(*buffer, format->getWidth(), format->getHeight(), colour, sizes);
	
	if (refFormat == NULL)
	  {
	    // Use this resolution as reference resolution
	    refFormat = &(*format);
	    
	    // User instruction
	    _INFO_("** Place at least two " << argv[2] 
		   << " spots in the view of the camera. It is strongly recommended"
		   << " to place them in opposite corners of the image.\n");
	    
	    // Show image until user clicks
	    g_continue = false;
	    char c;
	    while (!g_continue)
	      {
		// Grab, process and show frame
		processFrame(buffer, cam, *format, ip, refSpots);
		display(buffer, refSpots, *format);
		
		// Check user input
		while (cin.rdbuf()->in_avail())
		  if (cin.get(c) and c=='\n')
		    {
		      g_continue = true;
		      break;
		    }
	      }
	    
	    // Assemble list of spots
	    ostringstream spotList;
	    for (list<ImageSpot*>::const_iterator spot = refSpots.begin(); 
		 spot != refSpots.end(); spot++)
	      spotList << "(" << (*spot)->x << "/" << (*spot)->y << "),";
	    _INFO_("** Found " << refSpots.size() << " spots: " 
		    << spotList.str() << "\b\n");
	    
	    // Save reference format
	    out << "reference.width=" << refFormat->getWidth() << "\n"
		<< "reference.height=" << refFormat->getHeight() << "\n\n";
	    
	    _INFO_("** Using " << refFormat->getWidth() << " x " 
		   << refFormat->getHeight() << " as reference resolution.\n");
	  }
	
	else    // Reference format has been set
	  {
	    // Find spots (skip 10 frames, might contain glitches)
	    list<ImageSpot*> spots;
	    for (int i = 0; i < 10; i++)
	      {
		// Grab, process and show frame
		processFrame(buffer, cam, *format, ip, spots);
		display(buffer, spots, *format);
	      }
	    
	    // Print spots
	    ostringstream spotList;
	    for (list<ImageSpot*>::const_iterator spot = spots.begin(); 
		 spot != spots.end(); spot++)
	      spotList << "(" << (*spot)->x << "/" << (*spot)->y << "),";
	    _INFO_("** Found " << spots.size() << " spots: " 
		   << spotList.str() << "\b\n");
	    
	    // Match them to the reference spots
	    list< pair<ImageSpot*,ImageSpot*> > pairs;
	    if ( !matchSpots(spots, refSpots, pairs) )
	      {
		_INFO_("** The " << refSpots.size() 
		       << " spots from the image in the reference resolution could"
		       << " not be identified in the image with resolution "
		       << format->getWidth() << " x " << format->getHeight() 
		       << "! If " << refSpots.size() 
		       << " is not the number of spots placed under the camera,"
		       << " click onto the image and start again."
		       << " Otherwise either press 'i' and ENTER in the console to"
		       << " skip this resolution or move the spots so that they are"
		       << " visible and click on the image.\n");
		
		// Show image until user clicks or presses 'i'
		char c = 0;
		g_continue = false;
		while (!g_continue)
		  {
		    // Grab, process and display frame
		    processFrame(buffer, cam, *format, ip, spots);
		    display(buffer, spots, *format);
		    
		    // Check user input
		    while (cin.rdbuf()->in_avail())
		      if (cin.get(c) and c=='i' or c=='\n')
			{
			  g_continue = true;
			  break;
			}
		  }
		
		// Abort if not ignored
		if (c != 'i')
		  {		  
		    out.close();
		    _EXIT_("** Please re-start the resolution calibration tool!\n");
		  }
	      }
	    
	    else    // spots matched refSpots
	      {
		// Write the resolution to file
		out << "res." << index << ".width=" << format->getWidth() << "\n" 
		    << "res." << index << ".height=" << format->getHeight() << "\n";
		
		// Calculate the mapping between the resolutions
		double offset, scale;
		leastSquareFit(pairs, &ImageSpot::x, offset, scale);
		out << "res." << index << ".map.x.offset=" << offset << "\n"
		    << "res." << index << ".map.x.scale=" << scale <<  "\n";
		
		leastSquareFit(pairs, &ImageSpot::y, offset, scale);
		out << "res." << index << ".map.y.offset=" << offset << "\n"
		    << "res." << index << ".map.y.scale=" << scale <<  "\n\n";
		out.flush();
		
		// Increase index (to ensure consecutive numbering starting with 0)
		index++;
	      }
	  }
      }
  
  // Close output file
  out.close();
  _INFO_("** Resolution calibration successful.");
  _INFO_("** It is strongly recommended to edit the output file by hand to straighten"
	 << " noisy values. An offset up to 3 is likely to be a noisy 0. Scale"
	 << " factors have much smaller errors, usually clearly less than 0.1."
	 << " But values like 1.98 obviously stand for 2. For odd values it is worth"
	 << " checking whether the correct value can be calculated by dividing the"
	 << " resolution's height (width) by the reference height (width).\n");
}

//----------------------------------------------------------------------

/**
 * Sets the resolution to the specified format. Also adusts the image buffer 
 * size.
 */
bool setResolution(const ImageFormat& format, Webcam& cam, IplImage*& buffer)
{
  // Release previous image buffer
  if (buffer)
    {
      cvReleaseImage(&buffer);
      buffer = NULL;
    }
  
  cam.closeDevice();
  if (!cam.openDevice())
    _ABORT_("Failed to re-open camera!");
  
  // Attempt to set the resolution
  cam.setResolution(format, 5);
  
  // See if successful
  int w, h, fps;
  cam.getResolution(w, h, fps);
  if (w == format.getWidth() && h == format.getHeight())
    _INFO_("Resolution set to " << w << " x " << h << " @ " << fps << " fps");
  else
    return false;
  
  // initial camera shared memory
  if (!cam.initCapture())
    return false;
  
  // Create image buffer
  CvSize size;
  size.height = h;
  size.width = w;
  buffer = cvCreateImage(size, IPL_DEPTH_8U, 3);
  if (!buffer)
    _EXIT_("Failed to create buffer for image capturing.");
  
  return true;
}

//----------------------------------------------------------------------

/**
 * Grabs and classifies a frame and finds the spots in it.
 */
inline void processFrame(IplImage* buffer, Webcam& cam, const ImageFormat& format,
			 ClusteringImageProcessor& ip, list<ImageSpot*>& spots)
{
  // Grab frame
  static unsigned char* rawI;
  rawI = cam.nextFrame();

  // Convert to BGR24
  ccvt_420p_bgr24(format.getWidth(), format.getHeight(), rawI, buffer->imageData);
  
  // Classify image
  ip.processImage();
  
  // Find spots
  spots.clear();
  ip.findSpots(spots);
}

//----------------------------------------------------------------------

/**
 * Draws markers around the spots and displays the image in a window.
 * Also draws a line around the image (because the window is not cleared and
 * so previous, larger resolutions are still visible).
 */
inline void display(IplImage* buffer, const list<ImageSpot*>& spots, 
		    const ImageFormat& format)
{
  // Draw spots onto image
  CvPoint p, pp;
  for (list<ImageSpot*>::const_iterator spot = spots.begin();
       spot != spots.end(); spot++)
    {
      p.x = (*spot)->x;
      p.y = (*spot)->y;
      cvCircle(buffer, p, (*spot)->radius, CV_RGB(0, 255, 0), 1);
    }
  
  // Draw a frame around the image
  p.x = 0;
  p.y = 0;
  pp.x = format.getWidth() - 1;
  pp.y = format.getHeight() - 1;
  cvRectangle(buffer, p, pp, CV_RGB(0,0,255), 1);
  
  // Display in the window
  cvShowImage(WIN_TITLE, buffer);
  // Enter into the OpenCV event dispatcher. Need this for window
  // events to be processed (such as the clicks on the control window)
  cvWaitKey(1);
}

//----------------------------------------------------------------------

/**
 * Method called by the event dispatcher when the user clicks on the image.
 */
void cvMouseCallback(int event, int x, int y, int flags)
{
  if (event == CV_EVENT_LBUTTONUP)
    {
      x = y = flags = 0; // removes compiler warning about unused ints
      g_continue = true;
    }
}

//----------------------------------------------------------------------

/**
 * Matches a set of spots found in the current resolution to the reference 
 * spots (i.e. the spots from the reference resolution). The aim is to find 
 * the spots from the reference resolution in the image from the current
 * resolution. Spots don't move between the image captures, so the angle
 * under which other spots appear seen from one spot is the same in both
 * resolutions.
 */
bool matchSpots(list<ImageSpot*>& spots, const list<ImageSpot*>& refSpots,
		list< pair<ImageSpot*,ImageSpot*> >& pairs)
{
  // Compare number of spots (allow extra spots in spots, which might occur
  // if spots don't appera round on the screen)
  if (spots.size() < refSpots.size())
    return false;

  // Match recursively
  return matchSpotRecursion(spots, refSpots.begin(), refSpots.end(), pairs);
}

//----------------------------------------------------------------------

/**
 * The matching is done through an interpretation tree.
 */
bool matchSpotRecursion(list<ImageSpot*>& spots, 
		list<ImageSpot*>::const_iterator refSpot, 
		list<ImageSpot*>::const_iterator refSpotsEnd,
		list< pair<ImageSpot*,ImageSpot*> >& pairs)
{
  // All reference spots mached?
  if (refSpot == refSpotsEnd)
    return true;
  
  // Loop through the spots and try to match one of them to the current 
  // reference spot
  for (list<ImageSpot*>::iterator candidate = spots.begin(); 
	   candidate != spots.end(); candidate++)
    {
      // Compare angles to previous matches
      if (match(**candidate, **refSpot, pairs))
	{
	  // Pair spots up and remove candidate from spots (refSpots are  
	  // matched in order, so no need to remove them from the list)
	  pairs.push_back(make_pair(*candidate, *refSpot));
	  candidate = spots.erase(candidate);
	  
	  // Try to match the remaining reference spots
	  if (matchSpotRecursion(spots, ++refSpot, refSpotsEnd, pairs))
	    // All remaining remaining reference spots were also matched 
	    // successfully, so report success.
	    return true;
	  
	  // If the program execution gets here the remaining reference spots 
	  // could not be matched to spots from the list. This might be because 
	  // the match done in this recursion was wrong, so try a different 
	  // match.
	  
	  // Backtrack: reinsert candidate into spots and remove pair
	  refSpot--;
	  candidate = spots.insert(candidate, pairs.back().first);
	  pairs.erase(--pairs.end());
	}
    }
  
  // If the execution gets here, the spots don't match this and the remaining 
  // reference anchors. So report failure, maybe the caller can do an 
  // alterernative match which will lead to success.
  
  return false;
}

//----------------------------------------------------------------------

/**
 * The matching test for the interpretation tree algorithm. The test is whether
 * the relative direction of previous matches is the same in the current and
 * reference resolution.
 */
bool match(const ImageSpot& spot, const ImageSpot& refSpot, 
	   list< pair<ImageSpot*,ImageSpot*> > pairs)
{
  // Loop through the pairs and check that the angles to the other spots match
  for (list< pair<ImageSpot*,ImageSpot*> >::const_iterator pair = pairs.begin();
       pair != pairs.end(); pair++)
    {
      float angle1 = lineAngle(spot, *pair->first);
      float angle2 = lineAngle(refSpot, *pair->second);
      
      // Compare angles
      if (abs(angle1-angle2) > 10.0/180*M_PI)
	return false;
    }
  
  return true;
}

//----------------------------------------------------------------------

/**
 * Calculates the angle of the line defined by two points in the plain.
 */
float lineAngle(const ImageSpot& spot1, const ImageSpot& spot2)
{
  return atan2((float) spot2.y-spot1.y, spot2.x-spot1.x);
}

//----------------------------------------------------------------------

/**
 * The function calculates a 1D affine transformation between the x (or y)
 * coordinates of the spots locations in the current and the reference 
 * resolution. This is done through a standard least-square line fitting 
 * algorithm. The memberVariable argument is a pointer to a data member in 
 * ImageSpot, i.e. x or y. This determines whether the transformation for x
 * or y coordinates is calculated.
 */
void leastSquareFit(const list< pair<ImageSpot*,ImageSpot*> >& pairs, 
		    int ImageSpot::* memberVariable, double& offset, double& scale)
{
  // The rest of the comments of this function ignore that it is used to 
  // calculate an affine transformation. The algorithm is line fitting and 
  // hence the comments are in terms of line fitting. So x and y are the 
  // coordinates of points on the line.
  // The x is found in the first element of the pairs, y in the second.

  // The algorithm is as follows:
  //  - extend vector of x coordinate to a matrix X by adding a column of ones
  //  - calculate inv(X'*X)*X'*y

  // Calculate X'*X and store in matrix ( a b ) 
  //                                    ( b d )
  double a = 0.0, b = 0.0, d = pairs.size();
  for (list< pair<ImageSpot*,ImageSpot*> >::const_iterator pairI = pairs.begin(); 
       pairI != pairs.end(); pairI++)
    {
      a += pow(static_cast<double>(pairI->first->*memberVariable), 2);
      b += pairI->first->*memberVariable;
    }
  
  // Calculate X'*y and store in vector ( e )
  //                                    ( f )
  double e = 0.0, f = 0.0;
  for (list< pair<ImageSpot*,ImageSpot*> >::const_iterator pairI = pairs.begin(); 
       pairI != pairs.end(); pairI++)
    {
      e += pairI->first->*memberVariable * pairI->second->*memberVariable;
      f += pairI->second->*memberVariable;
    }
  
  // Calculate inv(X'*X)*X'*y which is ___1___   (  d -b )   ( e )
  //                                   (ad-bb) * ( -b  a ) * ( f )
  double factor = 1/(a*d-b*b);
  scale = factor*(d*e-b*f);
  offset = factor*(-b*e+a*f);
}
