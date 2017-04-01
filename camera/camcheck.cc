#include <iostream>

#include "ImageFormat.hh"
#include "Webcam.hh"

#define CAMCHECK_VERSION "1.1"

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

/**
 * Output usage information
 */
void usage()
{
  cout << "Usage: camcheck            Open camera attached to '" << CAM_DEVICE 
       << "'" << endl
       << "       camcheck [device]   Open camera attached to 'device'" << endl
       << "       camcheck --help     Show this help" << endl
       << "       camcheck --version  Show version" << endl;
}

int main(int argc, char** argv)
{
  char* devName;

  if (argc > 2)
    {
      _ERROR_("Too many arguments");
      usage();
      return 1;
    }

  if ((argc == 2) and (strcmp(argv[1], "--version") == 0))
  {
    cout << CAMCHECK_VERSION << endl << PORT << endl;
    return 0;
  }

  if ((argc == 2) and (strcmp(argv[1], "--help") == 0))
  {
    usage();
    return 0;
  }

  // Create camera object
  if (argc == 2)
    {
      devName = argv[1];
    }
  else
    {
      devName = CAM_DEVICE; 
    }
  Webcam cam(devName);
      
  
  // Attempt open
  if (!cam.openDevice())
    {
      return 1;
    }

  // Get camera information
  std::string model;
  int w, h;
  if (cam.getModel(model) && cam.getMaxResolution(w,h))
    {
      _INFO_("Camera detected is '" << model << "', supports max image size"
             << ": " << w << " x " << h);
    }
  else
    {
      _WARN_("Failed to retreive camera information");
    }

  // Get more information
  int paletteCode = cam.getPaletteCode();
  _INFO_("Camera palette code = " << paletteCode << ", "
         << cam.decodePaletteCode(paletteCode)
         << " (also see system file videodev.h)");

  _INFO_("Testing modes...");
    cam.displayModeScan();

  _INFO_("Setting resolution for highest frame-rate...");
  const ImageFormat* ptr;
  int fps;
  cam.setResolutionMaxFPS(ptr, fps);

  _INFO_("Setting resolution for highest image size...");
  cam.setResolutionMaxSize(ptr, fps);
  return(0);
}
