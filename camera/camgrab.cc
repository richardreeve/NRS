#include <iostream>
#include <ctime>
#include <linux/videodev.h>

#include "opencv/highgui.h"

#include "ImageFormat.hh"
#include "Webcam.hh"
#include "ImageUtils.hh"

#define VERSION "1.1"

#ifndef PORT
#define PORT "no build environment info available"
#endif


// These trace macros are copied from NRS's Exception.hh

#define _WARN_(X) \
std::cout << "WARNING: " << X << " (" << __FILE__ << ":" << __LINE__ << ")\n"

#define _ERROR_(X) \
std::cout << "ERROR: " << X << " (" << __FILE__ << ":" << __LINE__ << ")\n"

#define _INFO_(X) \
std::cout << "INFO: " << X << " (" << __FILE__ << ":" << __LINE__ << ")\n"

using namespace std;

#define CAM_DEVICE "/dev/video0"
#define DEFAULT_FILE "image.jpg"

/**
 * Output usage information
 */
void usage()
{
  cout << "Usage: camgrab [--help] [--version] [-d device]"
       << " [--delay n (in seconds)]"
       <<" [file]" << endl;
}

int main(int argc, char** argv)
{
  char* devParam = CAM_DEVICE;
  char* fileParam = DEFAULT_FILE;
  time_t delaySecs = 0;
  
  for (int i = 1; i < argc; i++)
    {
      if (strcmp(argv[i], "--version") == 0)
        {
          cout << VERSION << endl << PORT << endl;
          return EXIT_SUCCESS;
        }
      else if (strcmp(argv[i], "--help") == 0)
        {
          cout << "Capture camera output and save as a JPEG file\n";
          usage();
          return EXIT_SUCCESS;
        }
      else if (strcmp(argv[i], "-d") == 0)
        {
          i++;
          if (i < argc)
            {
              devParam = argv[i];
            }
          else
            {
              _ERROR_("Missing device name");
              usage();
              return EXIT_FAILURE;
            }
        }
      else if (strcmp(argv[i], "--delay") == 0)
        {
          i++;
          if (i < argc)
            {
              delaySecs= atoi(argv[i]);
            }
          else
            {
              _ERROR_("Missing number of seconds for delay option");
              usage();
              return EXIT_FAILURE;
            }
       }
      else
        {
          fileParam = argv[i];
         if ((i+1) < argc) // error, trailing args after final arg
            {
              _ERROR_("Too many arguments");
              usage();
              return EXIT_FAILURE;
            }
        }
    }
  if (!fileParam)
    {
      _ERROR_("No output filename specified");
      usage();
      return EXIT_FAILURE;
    }
 
  // Create camera interface
  Webcam cam(devParam);
    
  // Attempt open
  if (!cam.openDevice()) { return 1; }

  if (!cam.initCapture())

    {
      _ERROR_("Image capture could not be initiated");
    }

  CvSize imgSize;
  int fps;
  const ImageFormat* format;
  if (cam.setResolutionMaxSize(format, fps))
    {
      _INFO_("Image size is " << format->getName()
             << " (" << format->getWidth() << "x" 
             << format->getHeight() << ")");
      imgSize.width = format->getWidth();
      imgSize.height = format->getHeight();
    }
  else
    {
      _ERROR_("Failed to set camera resolution");
      exit(1);
    }

  
  if (delaySecs)
  {
    _INFO_("Delaying for " << delaySecs << " seconds");
    time_t start = time(0);
    while (time(0) - start <= delaySecs)
      {
        cam.nextFrame();
      }
  }

  unsigned char* rawImage = cam.nextFrame();
  if (rawImage) 
    {
      _INFO_("CLICK!");
    }

  int pc = cam.getPaletteCode();

  IplImage *cvImg;

  switch (pc)
    {
    case VIDEO_PALETTE_RGB24 :
      {
        _INFO_("Camera source format identified as 24bit RGB");
        cvImg = makeIplImageFromRGB24(format->getWidth(),
                                      format->getHeight(),
                                      (char*) rawImage);
        break;
      }
    case VIDEO_PALETTE_YUV420P :
      {
        _INFO_("Camera source format identified as YUV 4:2:0 Planar");
        cvImg = cvCreateImage(imgSize, IPL_DEPTH_8U, 3);
        convertYUV420ImageRGB(cvImg, rawImage, imgSize.width, imgSize.height);
        break;
      }
      
    default :
      {
        _ERROR_("Camera palette code not recognised - can't convert image");
        exit(1);
      }
    }


  // Call to OpenCV("test.jpeg", img);
  _INFO_("Saving image to " << fileParam);
  cvSaveImage(fileParam, cvImg);

  cvReleaseImage(&cvImg);

  cam.closeDevice();


  return(0);
}
