
#ifndef _ROBOT_HH_
#define _ROBOT_HH_

#include <string>
#include <vector>
#include <list>
#include <sstream>

#include "ImageSpot.hh"
#include "ClusteringImageProcessor.hh"
#include "CameraProjection.hh"

// Classes/structs defined in this file
class Robot;
class Anchor;
struct AnchorRelation;

// Also contains definition (!) of helper functions
double __length(double x, double y);
double __angle(double x, double y);
double __x(double radius, double angle);
double __y(double radius, double angle);

// Forward declaration
struct MatchingTolerance;


using namespace std;


//----------------------------------------------------------------------


/**
 * Class that represents a robot. Each robot has a set of anchors which have
 * known position on the robot (in a robot-centered coordinate system). Each
 * anchor also stores the size and colour of the spot that is attached on the
 * robot at that known position. When the tracker starts up it looks for 
 * spots in the entire image. The result of that is a list of ImageSpot
 * objects. Those will be matches with the robot's anchors (see matchSpots()).
 * In the main tracking loop, the robot updates the spots associated with
 * it's anchors and calculates its position from that.
 */
class Robot
{
  /** Anchors of the robot. */
  std::vector<Anchor*> m_anchors;
  /** Number of located anchors. */
  int m_located;

public:
  /** Name of the robot. */
  const std::string label;

  /** Robot has only one anchor. */
  const bool singleSpot;

  /** Heading of the robot in radiants. */
  double heading;

  /** Location of the robot in scene coordinates. */
  double x, y;

public:
  typedef std::vector<Anchor*>::const_iterator AnchorIterator;

  /**
   * Creates a new robot. If singleSpot is true, there is only one spot attached
   * to the robot. Hence the heading can't be determined.
   * The robot's only anchor can *not* have an offset from the robot's centre, 
   * because this would require the heading to be computable.
   */
  Robot(std::string label, bool singleSpot = false);

  /** 
   * Delete all anchors.
   */
  ~Robot();

  /**
   * Add an anchor to the robot. Function for setup through RobotManager.
   */
  void addAnchor(double x, double y, double radius, 
		 int colour, std::string colourName, double clip,
		 const MatchingTolerance& tol, const CameraProjection& cam);


  /**
   * Returns whether the robot has been located. This property will only 
   * become true once all achors of the robot have been matched to spots.
   * However it will stay true as long as at least two anchors are located.
   * The robot will automatically try to relocate spots that have been lost.
   * If there is only one anchor, located is true if that anchor is located.
   */
  inline bool located() const { return m_located>=2 || (singleSpot && m_located==1); };
  
  /**
   * Matches (unlocated) anchors with spots from the list. The function 
   * implements a standard interpretation tree algorithm. Each matching step
   * checks the following conditions:
   * - spot.colour == anchor.colour,
   * - spot.radius == anchor.m_sc_radius (i.e. the radius in pixels must 
   *   match),
   * - the spot must have the expected location relative to other located 
   *   anchors of the same robot (uses locations in scene coordinates).
   *
   * The last condition is the harder to meet the more anchors are located. 
   * In fact one can only be fairly sure that a set of spots was matched 
   * correctly if all anchors have a match. Therefore the function will either
   * match each anchor with one of the spots from the list, or none at all.
   * The matched ImageSpots objects are removed from the list.
   *
   * WARNING: The ownership of the ImageSpots removed from the list will be 
   * taken.
   *
   * NB: All ImageSpot objects in the list must have valid scene 
   * coordinates (sc_x and sc_y).
   */
  void matchSpots(std::list<ImageSpot*>& spots);

  /**
   * Displays a table that sums up relevant information about this robot's 
   * anchors (colour, size and relation between anchors). This is useful to 
   * debug the robot setup ("Were the anchor locations defined correctly?")
   * and the camera calibration. If either is too inprecise, the matching of
   * spots to robots may fail.
   */
  void printSummary() const;
  
  /**
   * This function updates this Robot instance to comply with the new frame 
   * from the camera. This includes several steps: First all anchors are asked 
   * to locate themselves. If at least two of them were successful, the robot 
   * heading and location are calculated, and the location of missing spots is 
   * estimated. (The anchor's located property will stay false.) Otherwise
   * it is not possible to estimate the missing spots, so they will be deleted
   * from the respective anchors. Usually there will still be one located 
   * anchor, which will keep tracking it's ImageSpot.
   *
   * After this method, each anchor is in one of the following three states:
   * - .located==true: the associated spot has been found in the current frame.
   *   It also means that .spot->sc_x and .spot->sc_y are up to date.
   * - .located==false and .spot!=NULL: the associated ImageSpot has not been 
   *   found in the current frame, but was estimated. The spot location in 
   *   scene coordinates is not calculated for estimated spots.
   * - .spot==NULL: the spot was neither found nor estimated. Since there is
   *   no estimate, the next call of Robot::track() will not look attempt to
   *   located those anchors.
   *
   * A robot can be in two states
   * - .located()==true: The robot's position and heading could be computed.
   *   This also means that all anchors will have anchor.spot!=NULL, that is
   *   their associated spots are at least estimated.
   * - .located()==false: The robot's position and heading are invalid. 
   *   Unlocated anchors will have anchor.spot==NULL and at most one anchor is
   *   located.
   * 
   * In the special case of a single anchor the heading is never computed.
   * The robot is located if the spot associated with the only anchor was found
   * in the new frame. The location of the robot is the anchor's location (the
   * anchor must be in the center of the robot).
   *
   * This method will be called once for every frame on every robot, so it needs
   * to be inlined. The implementation is at the bottom of this file.
   */
  inline void track(ClusteringImageProcessor& ip);
  
  /**
   * Get an STL iterator pointing to first anchor of this robot.
   */
  inline AnchorIterator begin() { return m_anchors.begin(); }

  /**
   * Get an STL iterator pointing to last anchor of this robot.
   */
  inline AnchorIterator end() { return m_anchors.end(); }
 
private:
  /**
   * Recursive function to match ImageSpots to Anchors. Returns true if all
   * non-located anchors of this robot have been matched successfully. In 
   * that case the matched ImageSpots are removed from the list.
   */
  bool matchSpotRecursion(std::list<ImageSpot*>& spots, 
			  std::vector<Anchor*>::iterator current);

  /**
   * Compute the robot's location and heading (each if possible) from the 
   * anchor's locations.
   */
  inline bool update();
  
  
  /** Probhibit copying and assignment. */
  Robot(const Robot&);
  Robot operator=(const Robot&);
};


//----------------------------------------------------------------------


/**
 * This class represents an anchor on the robot, i. e. a location on the robot
 * at which a spot is attached. If the robot is located successfully, each of 
 * it's anchors will have an associated ImageSpot object.
 * 
 * Instances of this class will not be created or deleted during the tracking 
 * process. ImageSpot instances however need to be dynamic in a system that 
 * allows a robot to be tracked with several cameras, because spots can't be 
 * matched to robots until an unambiguous set of spots (e.g. all of them) 
 * has appeared in the image.
 * Therefore this class was introduced as an "abstract spot". It also does much
 * of the transition between the image and scene coordinate system. To avoid 
 * confusion, variables are flagged by sc_ or im_ to indicate which coordinate
 * system they apply to.
 */
class Anchor
{
  /** The offset from the robot center in scene units. This is defined through 
      a (mathmatical) vector from the centre to the anchor, i.e. the anchor
      position *minus* the vector is the centre. */
  const double m_centreAngle, m_sc_centreDistance;
  
  /** The radius of the expected spot. */
  const double m_sc_radius;
  int m_im_radius;
  
  /** The clipping radius (search area around previous spot location). */
  int m_im_clip;
  
  /** Relation to other anchors of that robot. */
  vector<AnchorRelation> m_relatives;

  /** Maximum tolerance for the angles (defined by three anchors of one robot)
      in the matching process. */
  const double m_angleTolerance;

  
  /** The current CameraProjection. Used for translation of between image and 
      scene coordinates, and between pixel and scene units (only in constructor). */
  const CameraProjection& m_cam;
  
public:
  /** The index number of the anchor. */
  const unsigned int index;
  
  /** The colour of the expected spot. */
  const int colour;
  const std::string colourName;
  
  /** Associated spot. Warning: may be NULL if no spot has been associated. */
  ImageSpot* spot;
  
  /** Anchor is located (because the associated spot is located). spot mustn't
      be null if located == true! */
  bool located;
  
  
  /** Returns radius in pixels */
  inline int pixelSize() const { return m_im_radius; }
  /** Returns clip in pixels */
  inline int pixelClip() const { return m_im_clip; }
  

public:
  /**
   * Creates and initializes an anchor instance. The initialization needs to be
   * completed through calls to Anchor::addRelation().
   */
  Anchor(int index, double x, double y, double radius, 
	 int colour, std::string colourName, double clip, 
	 const MatchingTolerance& tol, const CameraProjection& cam);
  
  /**
   * Deletes the associated ImageSpot if existant.
   */
  ~Anchor();
  
  /**
   * Stores the relation of this anchor to the other anchors of the same
   * robot ("neighbours"). This information is used to validate a match
   * between robots and anchors. 
   */
  void addRelation(Anchor& relative, const MatchingTolerance& tol);
  
  /** 
   * Returns the relation to another anchor. Note that the index is the 
   * sequence number in insertion order (see Robot::addAnchor code). 
   */
  AnchorRelation getRelation(unsigned int storeIndex) const;
  
  /**
   * Check whether a spot matches this anchor. The function compares colour,
   * size and the location relative to other anchors of the same robot (using
   * the array of AnchorRelation objects). If the location of the other anchors
   * is unknown, the location of the candidate is by default assumed to be valid.
   * The location comparison includes distances and angles, because distances
   * anlone can't disambiguate mirror-symmetric anchor patterns.
   */
  inline bool matches(ImageSpot& candidate);
  
  /**
   * Updates the anchor location by trying to find the associated spot near where
   * it was last time. The last location might be estimated (in that case "located"
   * was false). 
   * If a spot is actually found from an estimated spot location (i.e. it wasn't 
   * found in the last frame), "located" is set to true and the function returns 
   * 1. If a spot can't  be found even though it was found last time, "located"
   * is set to false and the funciton returns -1. Otherwise 0 is returned.
   */
  inline int track(ClusteringImageProcessor& ip);
  // at the bottom of this file

  /**
   * Calculate the robot rotation from the relations of this anchor to other 
   * anchors of the same robot. Only consider the anchors with lower index than
   * this anchor to avoid considering pairs of anchors twice. The way the 
   * relations are added (increasing indices; see Robot::addAnchor) those are
   * the first index-1 relations. 
   */
  inline void sumRotation(double& rotationSum, int& nominator, double& closeTo) const;
  // at the bottom of this file
  
  /**
   * Calculate the centre from the anchor location and the robot rotation. 
   */
  inline void sumLocation(double& xSum, double& ySum, int& nominator, 
			  const double& rotation) const;
  // at the bottom of this file
  
  /**
   * Estimates the location of the associated spot from the robot center and 
   * heading. This allows the spot to be found in the next frame, even if the
   * robot has moved a significant distance away from where the spot was 
   * occluded.
   * This method obvously requires that the robot can be located, i.e. at least
   * two of it's spots still need to be visible.
   */
  void estimateSpot(double robotX, double robotY, double heading);
};


//----------------------------------------------------------------------


/**
 * This structure stores the relationship between two anchors, the distance
 * between them and the angle of the line through them. 
 */
struct AnchorRelation
{
  /** The distance  between the anchors. */
  double sc_distance;
  /** The tolerated variation in the distance. */
  double sc_tolerance;
  
  /** The angle of the connecting line relative to the robot heading. */
  double angle;
  
  /* Instances will belong  to an anchor, so target only is sufficient. */
  Anchor* target;
};


//----------------------------------------------------------------------
//----------------------------------------------------------------------


inline void Robot::track(ClusteringImageProcessor& ip)
{
  // Attempt to locate anchors
  int locatedBefore = m_located;
  for (AnchorIterator anchor = m_anchors.begin(); anchor != m_anchors.end(); anchor++)
    {
      // Find spot that is associated with that anchor
      m_located += (*anchor)->track(ip);
    }
  
  // Compute location and heading
  if ( update() )
    {    
      
      // Estimate spots that were not found...
      if ( m_located < static_cast<int>(m_anchors.size()) )
	for (AnchorIterator anchor = m_anchors.begin();
	     anchor != m_anchors.end(); anchor++)
	  {
	    if ( !(*anchor)->located )
	      // Estimate location using the robot heading
	      (*anchor)->estimateSpot(x, y, heading);
	  }
    }
  
  // ... or delete them
  else if (m_located < locatedBefore)
    {
      // Remove the spots that were not found because there is no way to 
      // estimate them
      for (AnchorIterator anchor = m_anchors.begin(); 
	   anchor != m_anchors.end(); anchor++)
	{
	  if ( !(*anchor)->located )
	    {
	      // Delete the object, anchors have ownership over associated spots
	      delete (*anchor)->spot;
	      (*anchor)->spot = NULL;
	    }
	}
    }
}

//----------------------------------------------------------------------

inline bool Robot::update()
{
  // If enough anchors located
  if (m_located>=2)
    {
      // Update robot heading
      int nominator = 0;
      double angleSum = 0.0;
      double closeTo = 0.0;
      for (unsigned int i = 0; i < m_anchors.size(); i++)
	m_anchors[i]->sumRotation(angleSum, nominator, closeTo);
      heading = angleSum/nominator;
      
      // Update robot center
      nominator = 0;
      double xSum = 0.0, ySum = 0.0;
      for (unsigned int i = 0; i < m_anchors.size(); i++)
	m_anchors[i]->sumLocation(xSum, ySum, nominator, heading);
      x = xSum/nominator;
      y = ySum/nominator;
      
      return true;
    }
  else if (singleSpot and m_located == 1)
    {
      // Robot location is spot location
      x = m_anchors[0]->spot->sc_x;
      y = m_anchors[0]->spot->sc_y;

      return true;
    }
  
  return false;
}

//----------------------------------------------------------------------
//----------------------------------------------------------------------


inline int Anchor::track(ClusteringImageProcessor& ip)
{
#ifdef DEBUG
  if (NULL == spot and located)
    {
      located = false;
      _WARN_("Anchor located==true but no spot associated!!");
    }
#endif
  
  if (NULL == spot)
    // No spot associated (located must be false)
    return 0;
  
  // Locate the spot
  int locatedBefore = located;
  located = ip.trackSpot(*spot, m_im_clip);
  
  // Translate coordinates
  if (located)
    m_cam.transform(spot->x, spot->y, spot->sc_x, spot->sc_y);
  
  // Return +1 for re-found spot, -1 for lost spot
  return static_cast<int>(located) - locatedBefore;
}

//----------------------------------------------------------------------

inline void Anchor::sumRotation(double& rotationSum, int& nominator, 
				double& closeTo) const
{
  // Do nothing if this anchor is not located
  if (!located)
    return;
  
  // Calculate robot rotation from pairs of anchors and add the up
  for (unsigned int i = 0; i < index; i++)
    {
      Anchor* otherAnchor = m_relatives[i].target;
      if (otherAnchor->located)
	{
	  // The heading of the robot in the scene coordinate system is the 
	  // angle of a line on the robot in scene coordinates minus the angle
	  // of that line in robot coordinates
	  double rotation = (__angle(otherAnchor->spot->sc_x - spot->sc_x, 
				     otherAnchor->spot->sc_y - spot->sc_y)
			     - m_relatives[i].angle);
	  
	  // All angles should be fairly similar. However their numerical
	  // values might be 2*PI or 4*PI away from each other. This is a
	  // problem when calculating the average.
	  for (; rotation - closeTo > M_PI;  rotation -= 2*M_PI);
	  for (; rotation - closeTo < -M_PI; rotation += 2*M_PI);
	  if (nominator==0)
	    closeTo = rotation;
	  
	  // Now the angles can be averaged as normal
	  rotationSum += rotation;
	  nominator++;
	}
    }
}

//----------------------------------------------------------------------

inline void Anchor::sumLocation(double& xSum, double& ySum, int& nominator, 
				const double& rotation) const
{
  // Do nothing if this anchor is not located
  if (!located)
    return;
  
  // Calculate the centre location and add it.
  xSum += spot->sc_x - __x(m_sc_centreDistance, m_centreAngle + rotation);
  ySum += spot->sc_y - __y(m_sc_centreDistance, m_centreAngle + rotation);
  nominator++;
}

//----------------------------------------------------------------------
//----------------------------------------------------------------------


/**
 * Returns the eucledian norm of a vector.
 */
inline double __length(double x, double y)
{
  return sqrt( x*x + y*y );
}

/**
 * Return the angle of the vector (x, y). The returned value is measured in 
 * radians, with range -PI to PI (as according to atan2). An angle of zero is 
 * equivalent to vectors parallel to the y-axis (!! Non-standard but useful 
 * because the robot heading is the y-axis). Angles increase going counter-
 * clockwise (the normal way).
 */
inline double __angle(double x, double y)
{
 return atan2(-x, y);
}

/**
 * Returns the x coordinate from radial coordinates with the angle described
 * above.
 */
inline double __x(double radius, double angle)
{
  return -sin(angle) * radius;
}

/**
 * Returns the y coordinate from radial coordinates with the angle described
 * above.
 */
inline double __y(double radius, double angle)
{
  return cos(angle) * radius;
}



#endif

