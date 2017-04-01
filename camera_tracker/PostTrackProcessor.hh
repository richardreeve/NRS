#ifndef _POSTTRACK_PROCESSOR_HH_
#define _POSTTRACK_PROCESSOR_HH_

#include <string>
#include <fstream>

#include "CameraProjection.hh"
#include "Robot.hh"
#include "RobotManager.hh"


/**
 * Abstract base class, or interface, which a class must implement so
 * that it can be added to a Tracker to receive post image tracking
 * information.
 *
 * Note that the Tracker will need to call a virtual function, after
 * each image is processed. Although this is costly, it is only done, at
 * most, 30 times per second (ie at the fps rate)
 */
class PostTrackProcessor
{
public:
  typedef RobotManager::RobotIterator RobotIterator;
  typedef Robot::AnchorIterator SpotIterator;

  /**
   * Provide a virtual destructor, since this class is used for inheritance.
   */
  virtual ~PostTrackProcessor() {};

  /**
   * The Tracker calls this callback after every image-frame is
   * processed. The pair of iterators can be used to iterator through
   * the robots which were tracked. The current frame number processed
   * is accessible via argument frameNumber.
   */
  virtual void process(RobotIterator begin,
                       RobotIterator end,
                       int frameNumber,
		       double time) = 0;
};



/**
 * A simple implementation of the PostTrackProcessor interface which
 * simply prints to console the locations of all the tracked robots.
 */
class DefaultPostTrackProcessor : public PostTrackProcessor
{
  // Rate at which images are shown on the console
  int m_rate;

public:

  /** Constructor */
  DefaultPostTrackProcessor(int rate) : m_rate(rate) {};

  /** Implement PostTrackProcessor interface. Just output details to
      console. */
  virtual void process(RobotIterator begin,
                       RobotIterator end,
                       int frameNumber,
		       double time);

  /** Destructor (nothing to do) */
  ~DefaultPostTrackProcessor() {};
};



/**
 * An implementation of the PostTrackProcessor interface which writes to
 * file the locations of the robots. It creates two files, one with the 
 * pure data and one with the column headers.
 */
class FilePTP : public PostTrackProcessor
{
  // The file for the output of the pure data
  std::ofstream m_out;
  
public:
  /**
   * Create an instance that writes to the named files. 
   * \dataFile the name of the file into which the data is written.
   * \headerFile the name of the file into which the column headers are 
   * written.
   */
  FilePTP(std::string dataFile, std::string labelFile,
	  RobotIterator begin, RobotIterator end);

  /** Destructor (close the files)  */
  ~FilePTP();

  /** Implement PostTrackProcessor interface */
  virtual void process(RobotIterator begin,
                       RobotIterator end,
                       int frameNumber,
		       double time);
  
private:
  // Prohibit copying
  FilePTP(const FilePTP&);
  FilePTP& operator=(const FilePTP&);    
};

#endif


