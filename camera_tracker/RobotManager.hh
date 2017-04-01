#ifndef _ROBOT_MANAGER_HH_
#define _ROBOT_MANAGER_HH_

#include "Robot.hh"
#include <list>

#define KEY_ROBOT_COUNT "robots"
#define KEY_ROBOT "robot"
#define KEY_NAME "label"
#define KEY_SPOT_COUNT "spots"
#define KEY_SPOT "spot"
#define KEY_X "x"
#define KEY_Y "y"
#define KEY_COLOUR "colour"
#define KEY_RADIUS "radius"
#define KEY_CLIP "clip"
#define KEY_TOL_ANGLE "tolerance.angle"
#define KEY_TOL_DIST_ABS "tolerance.distance.abs"
#define KEY_TOL_DIST_REL "tolerance.distance.percent"

// In this file
class RobotManager;
struct MatchingTolerance;

// Forward declarations
class OptionsReader;
class ColourClusterSet;
class CameraProjection;


/**
 * Manage the construction, access and clean up of Robot objects.
 */
class RobotManager
{
  /** List of the robots managed by this instance. */
  std::vector<Robot*> m_robots;

public:
  typedef std::vector<Robot*>::const_iterator RobotIterator;
  
  /**
   * Creates a RobotManager and initializes the robots defined in the options
   * file.
   */
  RobotManager(const OptionsReader& config, 
	       const ColourClusterSet& colours, 
	       const CameraProjection& cam);

  /**
   * Destructor
   */
  ~RobotManager();
  
  /**
   * Match a list of spots to the robots. This is for the initial match of 
   * spots to robots. The function removes the matched spots from the list.
   * Warning: Spots that are not returned via the arguments will be deleted.
   */
  void matchSpots(list<ImageSpot*>& spots);

  /**
   * Print out a table with spot properties (colour, size, and the relation
   * between spots). This is useful to identify potential errors in the 
   * anchor specification or the camera calibration. Together with the
   * output of Robot::operator<< it enables to manually pursue the matching
   * process.
   */
  static void printSpotSummary(list<ImageSpot*>& spots);
  
  /**
   * Update the location of the robots. This function tracks all spots that
   * associated with anchors and which were found or estimated in the last
   * frame.
   * This method is part of the main loop, hence inlined. 
   */
  inline void trackRobots(ClusteringImageProcessor& ip)
  {
    for (RobotIterator robot = m_robots.begin(); robot != m_robots.end(); robot++)
      (*robot)->track(ip);
  }
  
  /**
   * Get an STL iterator pointing to first robot in list
   */
  inline RobotIterator begin() { return m_robots.begin(); }

  /**
   * Get an STL iterator pointing to end of robot list
   */
  inline RobotIterator end() { return m_robots.end(); }

private:
  /**
   * Loads the settings (name, spots, etc.) for a robot.
   */
  Robot* loadRobot(const OptionsReader& config, 
		   string prefix,
		   const ColourClusterSet& colours,
		   const MatchingTolerance& tolerance,
		   const CameraProjection& cam);
  
  /** Prohibit copying and assignment. */
  RobotManager(const RobotManager&);
  RobotManager& operator=(const RobotManager&);
};


struct MatchingTolerance
{
  /** Tolerances in the matching process (angular, distance, distance 
      relative to spot distances). */
  double angle;
  double distance;
  double relativeDistance;
};


#endif
