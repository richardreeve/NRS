#ifndef __CONFIGURATION_HH__
#define __CONFIGURATION_HH__

#include "ImageFormat.hh"

#include <string>
#include <iostream>
#include <fstream>

/** The followng #define statements specify how options should be
    written in the configuration file  */
#define COMMENT "#"
#define FPS "fps"
#define IMAGE_SIZE "image_size"
#define FRAME_COUNTER_RATE "frame_counter_rate"
#define CAMERA_DISPLAY_RATE "camera_display_rate"
#define PREPROCESS_DEBUG "preprocess_debug"
#define WHITEOUT_LOWER_THRESHOLD "whiteout_lower_threshold"
#define WHITEOUT_HIGHER_THRESHOLD "whiteout_higher_threshold"
#define DEBUG_DISPLAY_RATE "debug_display_rate"
#define NUM_TRACK_POINTS "numTrackPoints"
#define CIRCLE_RADIUS "circleRadius"
#define FEATURE_THRESH "featureThresh"
#define COMB_RADIUS "combRadius"
#define SINGLE_CHANNEL_THRESHOLD "singleChannelThreshold"
#define POST_CONV_THRESHOLD "postConvThreshold"
#define GAUSSIAN_RADIUS "gaussianRadius"
#define FEATURE_RADIUS "feature_radius"
#define VECTOR_DISPLAY_RATE "vector_display_rate"

/** Provide reading from file and accessibility to program configuration
  * options
  *
  * To update this class to support new configuration options, the following
  * set of changes should be made:
  *
  * (1) add a new #define to specify the option keyword
  *
  * (2) add a new member variable of the appropriate type to store the
  *     value of the option
  *
  * (3) set the default value of the new option in all constructors
  *
  * (4) In the method 'loadConfigFile()' add code to detect the new keyword
  *     and parse the option value
  *
  */
class Configuration
{
public:
  /** Class constructor
   *
   * \param configFile name of the of the configuration file to read
   * from.
   *
   **/
  Configuration(const std::string configFile);

  /** Destructor */
  ~Configuration();

  /** Read configuration options from file. Options read in are compared
      against expected strings, and for those that are a match, the
      option value is stored. Strings that do not match will cause a
      warning to be displayed to the console. */
  void loadConfigFile();


  /** Get the frames per second option */
  int getFPS() const { return m_fps; }

  /** Get the desired camera resolution option value */
  const ImageFormat& getImageSize() const { return m_imageSize; }

  /** Get the frame counter update rate option value */
  int getFrameCountRate() const { return m_frameCountRate; }

  /** Get the rate at which to show source camera images */
  int getCameraDisplayRate() const { return m_cameraDisplayRate; }

  /** Get whether we are in image preprocess debug mode */
  bool getPreprocessDebug() const {return m_preprocessDebug; }

  /** Return the lower white threshold level */
  int getWhiteoutLowerThreshold() const { return m_whiteLowerThresh; }

  /** Return the higher white threshold level */
  int getWhiteoutHigherThreshold() const { return m_whiteHigherThresh; }

  /** Return the rate at which debug images should be displayed */
  int getDebugDisplayRate() const { return m_debugDisplayRate; }

protected:

  void readUntilEOL();

  // Parse and set image size option
  void setImageSizeOption(const std::string& value);


private:
  /** Disable the copy constructor */
  Configuration(const Configuration&);

  /** Disable the assignment operator */
  Configuration& operator=(const Configuration&);

private:  std::string m_fileName;
  std::ifstream m_fs;

  // Option values
  int m_fps;
  ImageFormat m_imageSize;
  int m_frameCountRate;
  int m_cameraDisplayRate;
  bool m_preprocessDebug;
  int m_whiteLowerThresh;
  int m_whiteHigherThresh;
  int m_debugDisplayRate;

  int m_circleRadius;
  int m_featureThresh;
  int m_combRadius;
  int m_postConvThreshold;
  int m_gaussianRadius;

public:
  int singleChannelThreshold;
  int feature_radius;
  int vector_display_rate;
  int m_numTrackPoints;

};

#endif
