
#include <iostream>

#include "Exception.hh"
#include "OptionsFile.hh"
#include "Tracker.hh"
#include "ClusteringImageProcessor.hh"
#include "RobotManager.hh"
#include "PostTrackProcessor.hh"
#include "CameraProjection.hh"

#define APP_NAME "NRS.camtrack"

using namespace std;

int main(int argc, char* argv[])
{
  // Validate number of arguments
  bool colourDebug = false;
  if (argc < 2)
    {
      cout << APP_NAME << ": Please provide name of the configuration file\n";
      exit(EXIT_FAILURE);
    }
  else if (argc > 2)
    {
      _DEBUG_("Classify entire image for colour calibration debugging");
      colourDebug = true;
    }
  
  // Read the options
  OptionsFile options(argv[1]);
  try
    {
      options.read();
      _INFO_("Reading config from \"" << options.getFilename() << "\"");
    }
  catch (const NRS::Base::Exception& ex)
    {
      _ERROR_("File read error:" << ex);
      exit(EXIT_FAILURE);
    }
  
  // Read the camera calibration
  OptionsFile camFile(options.value("camera_calibration_file").c_str());
  try
    {
      // Read calibration into OptionsFile
      camFile.read();
    }
  catch (const NRS::Base::Exception &ex)
    {
      _EXIT_("Error reading camera calibration file: " << ex);
    }

  // Read the resolution calibration
  OptionsFile resFile(options.value("resolution_calibration_file").c_str());
  try
    {
      // Read calibration into OptionsFile
      resFile.read();
    }
  catch (const NRS::Base::Exception &ex)
    {
      _EXIT_("Error reading resolution calibration file: " << ex);
    }

  // Declare components
  try
    {
        // Create CameraProjection
      CameraProjection camProjection(camFile);
      if ( !camProjection.isValid() )
	_EXIT_("Camera calibration file invalid! Can't track!");
      camProjection.readResolutionMaps(resFile);
      
      // Create the image processor, using a config file which is
      // specifed in the main options file
      ClusteringImageProcessor cip(options.value("colours_file").c_str());
      
      // Initialize the tracker
      Tracker tracker(options, cip, camProjection);
      
      if (colourDebug)
	cip.classifyAll = true;
      
      // Add the PTPs
      DefaultPostTrackProcessor dptp(atoi(options.value(PING_RATE).c_str()));
      FilePTP fptp("tracker.txt", "tracker.names", tracker.getRobotManager().begin(), 
		                                   tracker.getRobotManager().end());
      tracker.addPTP(dptp);
      tracker.addPTP(fptp);
      
      // Grab the first image and try to match the robots to spots
      // and start the tracking
      tracker.start();
    }
  catch (const NRS::Base::Exception& ex)
    {
      _ERROR_("Run-time exception:" << ex);
      exit(EXIT_FAILURE);
    }
  
  // normal termination takes this path
  _INFO_(APP_NAME << " exiting");
  exit(EXIT_SUCCESS);
}
