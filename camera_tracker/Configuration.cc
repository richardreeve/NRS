#include "Configuration.hh"
#include "Exception.hh"
#include "ImageFormat.hh"

#define _INVALID_OPTION_(OPTION, VALUE) \
_WARN_("Invalid value (" << VALUE << ") for option " << OPTION)

#define _OPTION_(OPTION, VALUE) \
_INFO_("Loaded option " << OPTION << "=" << VALUE)


// Boolean string values
#define TRUE "true"
#define FALSE "false"

using namespace std;

/** Constructor */
Configuration::Configuration(const std::string configFile)
  : m_fileName(configFile),
    m_imageSize(QSIF),
    m_frameCountRate(50),
    m_cameraDisplayRate(3),
    m_preprocessDebug(false),
    m_whiteLowerThresh(130),
    m_whiteHigherThresh(150),
    m_debugDisplayRate(5),
    m_circleRadius(10),
    m_featureThresh(40),
    m_combRadius(10),
    m_postConvThreshold(100),
    m_gaussianRadius(21),
    singleChannelThreshold(120),
    feature_radius(13),
    vector_display_rate(0),
    m_numTrackPoints(2)
{
}
//----------------------------------------------------------------------
Configuration:: ~Configuration()
{
  if (m_fs.is_open()) m_fs.close();
}
//----------------------------------------------------------------------
void Configuration::readUntilEOL()
{
  char c;
  //  _INFO_("Ignoring rest of line");
  do
    {
      c = m_fs.get();
    }
  while ((!m_fs.eof()) && (c != '\n'));
}
//----------------------------------------------------------------------
void Configuration::setImageSizeOption(const std::string& value)
{
  bool recognised = false;

  if (value == VGA.getName())
    {
      m_imageSize = VGA;
      recognised = true;
    }

  if (value == CIF.getName())
    {
      m_imageSize = CIF;
      recognised = true;
    }

  if (value == QCIF.getName())
    {
      m_imageSize = QCIF;
      recognised = true;
    }

  if (value == sQCIF.getName())
    {
      m_imageSize = sQCIF;
      recognised = true;
    }

  if (value == SIF.getName())
    {
      m_imageSize = SIF;
      recognised = true;
    }

  if (value == QSIF.getName())
    {
      m_imageSize = QSIF;
      recognised = true;
    }

  if (recognised)
    {
      _OPTION_(IMAGE_SIZE, m_imageSize.getName());
    }
  else
    {
      _INVALID_OPTION_(IMAGE_SIZE, value);;
    }

}
//----------------------------------------------------------------------
void Configuration::loadConfigFile()
{
  // Attempt to open the file for reading
  m_fs.open(m_fileName.c_str(), ios::in);

  // Verify
  if (!m_fs.is_open())
    {
      _WARN_("Failed to open configuration file " << m_fileName);
    }



  string keyword, value;


  while (!m_fs.eof())
    {
      m_fs >> keyword;
      if (!m_fs.good()) break;

      // Read and throw away the "=" delimator
      string dummy; m_fs >> dummy;
      if (!m_fs.good()) break;
      if (!m_fs.good()) break;

      //      _INFO_("keyword=[" << keyword << "]");

      /** Handle possible keywords */

      if (keyword == COMMENT)  // Ignore comments
        {
          readUntilEOL();
          continue;
        }

      if (keyword == FPS)
        {
          m_fs >> m_fps;
          _OPTION_(FPS, m_fps);
          continue;
        }



      if (keyword == NUM_TRACK_POINTS)
        {
          m_fs >> m_numTrackPoints;
          _OPTION_(NUM_TRACK_POINTS, m_numTrackPoints);
          continue;
        }

      if (keyword == CIRCLE_RADIUS)
        {
          m_fs >> m_circleRadius;
          _OPTION_(CIRCLE_RADIUS, m_circleRadius);
          continue;
        }

      if (keyword == FEATURE_THRESH)
        {
          m_fs >> m_featureThresh;
          _OPTION_(FEATURE_THRESH, m_featureThresh);
          continue;
        }

      if (keyword == FEATURE_RADIUS)
        {
          m_fs >> feature_radius;
          _OPTION_(FEATURE_RADIUS, feature_radius);
          continue;
        }

      if (keyword == VECTOR_DISPLAY_RATE)
        {
          m_fs >> vector_display_rate;
          _OPTION_(VECTOR_DISPLAY_RATE, vector_display_rate);
          continue;
        }

      if (keyword == COMB_RADIUS)
        {
          m_fs >> m_combRadius;
          _OPTION_(COMB_RADIUS, m_combRadius);
          continue;
        }

      if (keyword == SINGLE_CHANNEL_THRESHOLD)
        {
          m_fs >> singleChannelThreshold;
          _OPTION_(SINGLE_CHANNEL_THRESHOLD, singleChannelThreshold);
          continue;
        }

      if (keyword == POST_CONV_THRESHOLD)
        {
          m_fs >> m_postConvThreshold;
          _OPTION_(POST_CONV_THRESHOLD, m_postConvThreshold);
          continue;
        }

      if (keyword == GAUSSIAN_RADIUS)
        {
          m_fs >> m_gaussianRadius;
          _OPTION_(GAUSSIAN_RADIUS, m_gaussianRadius);
          continue;
        }











      if (keyword == FRAME_COUNTER_RATE)
        {
          m_fs >> m_frameCountRate;
          _OPTION_(FRAME_COUNTER_RATE, m_frameCountRate);
          continue;
        }

      if (keyword == WHITEOUT_LOWER_THRESHOLD )
        {
          m_fs >> m_whiteLowerThresh;

          _OPTION_(WHITEOUT_LOWER_THRESHOLD, m_whiteLowerThresh);
          continue;
        }

      if (keyword == WHITEOUT_HIGHER_THRESHOLD)
        {
          m_fs >> m_whiteHigherThresh;

          _OPTION_(WHITEOUT_HIGHER_THRESHOLD, m_whiteHigherThresh);
          continue;
        }

      if (keyword == DEBUG_DISPLAY_RATE)
        {
          m_fs >> m_debugDisplayRate;

          _OPTION_(DEBUG_DISPLAY_RATE, m_debugDisplayRate);
          continue;
        }

      if (keyword == CAMERA_DISPLAY_RATE)
        {
          m_fs >> m_cameraDisplayRate;

          _OPTION_(CAMERA_DISPLAY_RATE, m_cameraDisplayRate);
          continue;
        }

      if (keyword == IMAGE_SIZE)  // Set size of the desired camera image
        {
          m_fs >> value;
          setImageSizeOption(value);
          continue;
        }

      if (keyword == PREPROCESS_DEBUG)  // Set size of the desired camera image
        {
          m_fs >> value;
          if (value == TRUE)
            {
              m_preprocessDebug = true;
              _OPTION_(PREPROCESS_DEBUG, TRUE);
            }
          else
            {
              m_preprocessDebug = false;
              _OPTION_(PREPROCESS_DEBUG, FALSE);
            }
          continue;
        }
    }

  m_fs.close();
}

