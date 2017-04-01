
#include <cstdlib>
#include <string>
#include <sstream>
#include <iomanip>

#include "RobotManager.hh"
#include "Robot.hh"
#include "OptionsReader.hh"
#include "Exception.hh"
#include "ColourClusterSet.hh"
#include "ColourCluster.hh"

using namespace std;

//----------------------------------------------------------------------

/**
 * Creates a RobotManager and initializes the robots defined in the options
 * file.
 */
RobotManager::RobotManager(const OptionsReader& config, 
			   const ColourClusterSet& colours,
			   const CameraProjection& cam)
{
  // Load the tolerances
  MatchingTolerance tol;
  tol.angle = atof(config.value(KEY_TOL_ANGLE).c_str())
                    * M_PI / 180;
  tol.distance = atof(config.value(KEY_TOL_DIST_ABS).c_str());
  tol.relativeDistance = atof(config.value(KEY_TOL_DIST_REL).c_str())
                         / 100;
  
  // Load the robots
  int robotCount = atoi(config.value(KEY_ROBOT_COUNT).c_str());
  for (int i = 0; i < robotCount; i++)
  {
     // Prefix for settings for this robot
    ostringstream os;
    string robotPrefix = (os << KEY_ROBOT << "." << i << ".", os.str());
    
    // Load robot
    m_robots.push_back(loadRobot(config, robotPrefix, colours, tol, cam));
  }
  
  // Warn if no robot has been loaded.
  if (0 == robotCount)
    _WARN_("No robot definitions in options file");
}

//----------------------------------------------------------------------

/**
 * Destructor
 */
RobotManager::~RobotManager()
{
  // Delete robot objects
  for (RobotIterator i = m_robots.begin(); i != m_robots.end(); i++)
    {
      Robot* ptr = *i;
      delete ptr;
    }
}

//----------------------------------------------------------------------

/**
 * Loads the settings (name, spots, etc.) for a robot.
 */
Robot* RobotManager::loadRobot(const OptionsReader& config, 
			       string prefix,
			       const ColourClusterSet& colours,
			       const MatchingTolerance& tolerance,
			       const CameraProjection& cam)
{
#ifdef DEBUG
  _DEBUG_("Load settings for " << prefix << "\b");
#endif
  
  // Create a new robot instance
  string robotName = config.value(prefix + KEY_NAME);
  int anchorCount = atoi(config.value(prefix + KEY_SPOT_COUNT).c_str());
  Robot* robot = new Robot(robotName, anchorCount==1);
  
  // Load setting for the spots (atoi() defaults to 0 so that's fine)
  for (int i = 0; i < anchorCount; i++)
    {
      // Prefix for settings of this spot
      ostringstream os;
      string spotPrefix = (os << prefix << KEY_SPOT << "." << i << ".", os.str());
      
      // Read and validate colour
      int colour = colours.cluster(config.value(spotPrefix + KEY_COLOUR).c_str());
      if (-1 == colour)
        {
	  // Ignore the entire spot
          _WARN_("Colour \"" << config.value(spotPrefix + KEY_COLOUR) << "\" of spot " 
		 << spotPrefix << "\b\" not recognised; spot will be ignored");
	  continue;
        }
      
      // Read the other values
      double x = atof(config.value(spotPrefix + KEY_X).c_str());
      double y = atof(config.value(spotPrefix + KEY_Y).c_str());
      double radius = atof(config.value(spotPrefix + KEY_RADIUS).c_str());
      double clip = atof(config.value(spotPrefix + KEY_CLIP).c_str());
      
      // Store spot
      robot->addAnchor(x, y, radius, colour, colours.cluster(colour).toString(), 
		       clip, tolerance, cam);
    }

  // Print out setup summary
  robot->printSummary();
  return robot;
}

//----------------------------------------------------------------------

/**
 * Match a list of spots to the robots. This is for the initial match of 
 * spots to robots. The function removes the matched spots from the list.
 * Warning: Spots that are not returned via the arguments will be deleted.
 */
void RobotManager::matchSpots(list<ImageSpot*>& spots)
{
  for (RobotIterator robot = m_robots.begin(); robot != m_robots.end(); robot++)
    // Robot currently not located so try to match robot with the spots
    (*robot)->matchSpots(spots);
}

//----------------------------------------------------------------------

void RobotManager::printSpotSummary(list<ImageSpot*>& spots)
{
  // Skip if no spots
  if (spots.size() == 0)
    return;

  // Compose output
  ostringstream ss;
  ss << "Table of spot properties:";

  // Properties
  ss << setprecision(2) << fixed;
  int index = 0;
  for (list<ImageSpot*>::const_iterator spot = spots.begin(); 
       spot != spots.end(); spot++)
    ss << "\n    #" << index++ 
       << " at " << setw(3) << (*spot)->x << "/" << setw(3) << (*spot)->y 
       << " (scene " << setw(7) << (*spot)->sc_x << "/" << setw(7) << (*spot)->sc_y
       << ") colour " << (*spot)->colour << ", radius " << (*spot)->radius;
  
  
  // Indices
  ss << "\nTable of spot relations";
  ss << "\n   Spot       |";
  for (unsigned int i = 0; i < spots.size(); i++)
    ss << "\t #" << i;
  
  // Separator line
  string separator = string(spots.size()*8, '-');
  ss << "\n  ------------|-" << separator;
  
  // Distances
  ss << "\n   Distances  |" << setprecision(2) << fixed;
  index = 0;
  for (list<ImageSpot*>::const_iterator spot1 = spots.begin(); 
       spot1 != spots.end(); spot1++)
    {
      // Spot index at start of line
      ss << "\n     #" << setw(7) << left << index++ << " |";
      
      // Distances from current spot
      for (list<ImageSpot*>::const_iterator spot2 = spots.begin();
	   spot2 != spots.end(); spot2++)
	if (*spot1 == *spot2)
	  ss << "\t";
	else
	  ss << "\t" << __length((*spot2)->sc_x - (*spot1)->sc_x, 
				 (*spot2)->sc_y - (*spot1)->sc_y);
    }
      
  // Table of orientations
  ss << "\n  ------------|-" << separator;
  ss << "\n   Orientation|";
  index = 0;
  for (list<ImageSpot*>::const_iterator spot1 = spots.begin(); 
       spot1 != spots.end(); spot1++)
    {
      // Spot index at start of line
      ss << "\n     #" << setw(7) << left << index++ << " |" << right;
      
      // Orientation of line defined with current spot
      for (list<ImageSpot*>::const_iterator spot2 = spots.begin();
	   spot2 != spots.end(); spot2++)
	if (*spot1 == *spot2)
	  ss << setw(8) << " ";
	else
	  ss << setw(7)
	     << (int) (__angle((*spot2)->sc_x - (*spot1)->sc_x, 
			       (*spot2)->sc_y - (*spot1)->sc_y)*180/M_PI) << "º";
    }

  // Output
  _INFO_(ss.str() << "\n");
}
