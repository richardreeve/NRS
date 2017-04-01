#include "Robot.hh"
#include "RobotManager.hh"
#include "CameraProjection.hh"
#include "Exception.hh"

#include <cmath>
#include <iostream>
#include <stdexcept>
#include <iomanip>

using namespace std;


//----------------------------------------------------------------------

Robot::Robot(string label, bool singleSpot)
  : m_located(0), 
    label(label),
    singleSpot(singleSpot),
    heading(0.0),
    x(0.0),
    y(0.0)
{
}

//----------------------------------------------------------------------

Robot::~Robot()
{
  // Delete the anchors
  for (vector<Anchor*>::iterator i = m_anchors.begin(); i != m_anchors.end(); i++)
    {
      Anchor* ptr = *i;
      delete ptr;
    }
}

//----------------------------------------------------------------------

void Robot::addAnchor(double x, double y, double radius, 
		      int colour, string colourName, double clip,
		      const MatchingTolerance& tol,
		      const CameraProjection& cam)
{
  // Check for contradiction with singleSpot
  if (singleSpot)
    if (m_anchors.size() > 0)
      {
	_WARN_("Robot '" << label << "' is configured to have only one anchor. "
	       << "Ignoring new anchor at " << x << "/" << y);
	return;
      }
    else if (x!=0.0 or y!=0.0)
      {
	_WARN_("Robot '" << label << "' is configured to have only one anchor. "
	       << "This anchor must be in the center of the robot (0, 0)! "
	       << "Ignoring the specified offset.");
	x = 0.0;
	y = 0.0;
      }
  
  // Create a new instance of anchor
  Anchor* newAnchor = new Anchor(m_anchors.size(), x, y, radius, 
				 colour, colourName, clip, tol, cam);
  
  // Set relation to all existing anchors, and from the existing to the new one
  for (vector<Anchor*>::iterator a = m_anchors.begin(); a != m_anchors.end(); a++)
    {
      newAnchor->addRelation(**a, tol);
      (*a)->addRelation(*newAnchor, tol);
    }
  // WARNING: the method Anchor::sumAngles requires that Anchors have no relation
  // to themselves and that the relations are stored in the order in which the 
  // anchors are added. For example the anchor with index 2 needs to have relations
  // to 0, 1, 3, 4, 5, etc and in that order.
  
  // Add new anchor to list
  m_anchors.push_back(newAnchor);
}

//----------------------------------------------------------------------

void Robot::matchSpots(list<ImageSpot*>& spots)
{
  int unlocated = static_cast<int>(m_anchors.size()) - m_located;

#ifdef DEBUG
  if (unlocated < 0)
    {
      _WARN_("m_located greater that number of anchors!!");
      return;
    }
#endif

  // Only start interpretation tree if necessary and chance of success
  if (unlocated > 0 and static_cast<int>(spots.size()) >= unlocated )
    {

#ifdef DEBUG
      _DEBUG_("Trying to match spots to robot '" << label << "'");
#endif
      
       // Call recursive method
      if (matchSpotRecursion(spots, m_anchors.begin()))
	{
	  // Calculate robot location & heading
	  update();
	  
	  _INFO_("Robot '" << label << "' found! ");
	}
#ifdef DEBUG
      else
	_DEBUG_("No spots matched to robot '" << label << "'");
#endif
    }
}

//----------------------------------------------------------------------

bool Robot::matchSpotRecursion(list<ImageSpot*>& spots,
			       vector<Anchor*>::iterator anchor)
{
  // Match succeeded if all anchors have been matched
  if (m_located == static_cast<int>(m_anchors.size()))
    return true;

#ifdef DEBUG
  if (anchor == m_anchors.end())
    {
      _WARN_("matchSpotRecursion was about to exceede m_anchors.end(), m_located too small.");
      return true;
    }
#endif
  
  // Is current anchor already located?
  if ( (*anchor)->located )
    {
      // Just step on if anchor is already located
      return matchSpotRecursion(spots, ++anchor);
    }
  else
    {
      // Save previous spot for backtracking (might be an estimated spot)
      ImageSpot* spotBackup = (*anchor)->spot;
      
      // Assume the matching succeeds
      (*anchor)->located = true;
      m_located++;
      
      // Loop through the spots and try to match them
      for (list<ImageSpot*>::iterator candidate = spots.begin(); 
	   candidate != spots.end(); candidate++)
	{
	  // Compare colour, size and relative position
	  if ( (*anchor)->matches(**candidate) )
	    {
	      // Move spot from the list to the anchor
	      (*anchor)->spot = *candidate;
	      candidate = spots.erase(candidate);
	      
	      // Try to match the remaining anchors
	      if (matchSpotRecursion(spots, ++anchor))
		{
		  // The match of this anchor is final so delete the previously
		  // associated spot (might be NULL but delete NULL is ok)
		  delete spotBackup;
		  
		  // All remaining anchors have also been matched successfully
		  // so report success. Note that this means that *all* recursive 
		  // calls will return.
		  return true;
		}
	      
	      // If the program execution gets here the remaining anchors could 
	      // not be matched to spots. This might be because the spot matched 
	      // to this anchor was wrong, so try a different match.
	      
	      // Backtrack: reset anchor iterator and reinsert spot into the list
	      anchor--;
	      candidate = spots.insert(candidate, (*anchor)->spot);
	    }
	}
      
      // If the execution gets here, the spots don't match this and the remaining
      // anchors. So report failure, maybe the caller can do an alterernative 
      // match which will lead to success.
      
      // Backtrack: matching didn't succeede
      (*anchor)->located = false;
      m_located--;
      
      // Backtrack: Restore original spot
      (*anchor)->spot = spotBackup;
      
      return false;
    }
}

//----------------------------------------------------------------------

void Robot::printSummary() const
{ 
  // Robot name
  ostringstream ss;
  ss << "Setup of \"" << label << "\":";

  // Indices
  ss << "\n   Anchor     |";
  for (vector<Anchor*>::const_iterator anchor = m_anchors.begin(); 
       anchor != m_anchors.end(); anchor++)
    ss << "\t #" << (*anchor)->index;
  
  // Separator line
  string separator = string(m_anchors.size()*8, '-');
  ss << "\n  ------------|-" << separator;
  
  // Colours
  ss << "\n   Colour     |";
  for (vector<Anchor*>::const_iterator anchor = m_anchors.begin(); 
       anchor != m_anchors.end(); anchor++)
    ss << "\t" << (*anchor)->colourName;
  
  // Size
  ss << "\n   Size       |";
  for (vector<Anchor*>::const_iterator anchor = m_anchors.begin(); 
       anchor != m_anchors.end(); anchor++)
    ss << "\t" << (*anchor)->pixelSize() << "px";
  
  
  if ( !singleSpot )
    {
      // Table of distances
      ss << "\n  ------------|-" << separator;
      ss << "\n   Distances  |" << setprecision(2) << fixed;
      for (vector<Anchor*>::const_iterator anchor = m_anchors.begin(); 
	   anchor != m_anchors.end(); anchor++)
	{
	  // Anchor index at start of line
	  ss << "\n     #" << setw(7) << left << (*anchor)->index << " |";
	  
	  // Line for this anchor
	  bool passedDiagonal = false;
	  for (unsigned int i = 0; i < m_anchors.size(); i++)
	    if (i == (*anchor)->index)
	      {
		passedDiagonal = true;
		ss << "\t";
	      }
	    else
	      {
		AnchorRelation relation = (*anchor)->getRelation(i-passedDiagonal);
		ss << "\t" << relation.sc_distance;
	      }
	}
      
      // Table of orientations
      ss << "\n  ------------|-" << separator;
      ss << "\n   Orientation|";
      for (vector<Anchor*>::const_iterator anchor = m_anchors.begin(); 
	   anchor != m_anchors.end(); anchor++)
	{
	  // Anchor index at start of line
	  ss << "\n     #" << setw(7) << left << (*anchor)->index << " |" << right;
	  
	  // Line for this anchor
	  bool passedDiagonal = false;
	  for (unsigned int i = 0; i < m_anchors.size(); i++)
	    if (i == (*anchor)->index)
	      {
		passedDiagonal = true;
		ss << setw(8) << " ";
	      }
	    else
	      {
		AnchorRelation relation = (*anchor)->getRelation(i-passedDiagonal);
		ss << setw(7) << (int) (relation.angle*180/M_PI) << "º";
	      }
	}
    }
  
  // Print out
  _INFO_(ss.str() << "\n");
  return;
}

//----------------------------------------------------------------------
//----------------------------------------------------------------------

Anchor::Anchor(int index, double x, double y, double radius, 
	       int colour, string colourName, double clip, 
	       const MatchingTolerance& tol, const CameraProjection& cam)
  : m_centreAngle(__angle(x, y)),
    m_sc_centreDistance(__length(x, y)),
    m_sc_radius(radius),
    m_angleTolerance(tol.angle),
    m_cam(cam),
    index(index),
    colour(colour),
    colourName(colourName),
    spot(NULL),
    located(false)
{
  // Translate radius and clip into image coordinates
  m_im_radius = static_cast<int>(round(m_cam.inverseTransformLength(m_sc_radius)));
  m_im_clip = static_cast<int>(round(m_cam.inverseTransformLength(clip)));
  
#ifdef DEBUG
  _DEBUG_("Anchor added r=" << m_sc_centreDistance 
	 << ", phi=" << m_centreAngle * 180 / M_PI
	 << ", colour=" << this->colourName 
	 << ", radius=" << m_sc_radius << " (" << m_im_radius << "px)" );
#endif
}

//----------------------------------------------------------------------

Anchor::~Anchor()
{
  // Delete the associated spot (unless spot is NULL)
  delete spot;
}

//----------------------------------------------------------------------

void Anchor::addRelation(Anchor& related, const MatchingTolerance& tolerance)
{
  // Calculate x and y
  double this_x = __x(m_sc_centreDistance, m_centreAngle);
  double this_y = __y(m_sc_centreDistance, m_centreAngle);
  double related_x = __x(related.m_sc_centreDistance, related.m_centreAngle);
  double related_y = __y(related.m_sc_centreDistance, related.m_centreAngle);
  
  // Calculate length and angle of connecting line
  AnchorRelation ar;
  ar.sc_distance = __length(related_x - this_x, related_y - this_y);
  ar.angle = __angle(related_x - this_x, related_y - this_y);
  
  // Set the distance tolerance
  ar.sc_tolerance = ar.sc_distance * tolerance.relativeDistance
                    + tolerance.distance;
  
  // Add to vector (note: makes a copy of ar)
  ar.target = &related;
  m_relatives.push_back(ar);
}

//----------------------------------------------------------------------

AnchorRelation Anchor::getRelation(unsigned int storeIndex) const
{
#ifdef DEBUG
  if ( storeIndex >= m_relatives.size() )
    _ERROR_("Index out of bounds!");
#endif

  // Return the anchor relation
  return m_relatives.at(storeIndex);
}

//----------------------------------------------------------------------

bool Anchor::matches(ImageSpot& candidate)
{
#ifdef DEBUG
  if (colour != candidate.colour)
    _DEBUG_("Colour mismatch between anchor " << index << " and spot "
	    << candidate.x << "," << candidate.y);
  else if (m_im_radius != candidate.radius)
    _DEBUG_("Size mismatch between anchor " << index << " and spot "
	    << candidate.x << "," << candidate.y << " (expected " 
	    << m_im_radius << ", is " << candidate.radius << ")");
#endif

  // Check colour and size
  if (colour != candidate.colour || m_im_radius != candidate.radius)
    return false;

#ifdef DEBUG 
  _DEBUG_("Trying to match anchor " << index << " to spot " 
	  << candidate.x << "," << candidate.y);
#endif
    
  // Check relative position
  double headingRef = 0.0;
  bool noHeadingRef = true; 
  
  for (vector<AnchorRelation>::iterator rel = m_relatives.begin(); 
       rel != m_relatives.end(); rel++)
    {
      // Only if related anchor is located
      if (rel->target->located)
	{
	  
#ifdef DEBUG
	  if (rel->target->spot == 0)
	    _ABORT_("Unexpected null pointer! Indicates inconsistency in anchor "
		    << rel->target->index);
#endif    
	  
	  // Distance between the two spots
	  double dist = __length(rel->target->spot->sc_x - candidate.sc_x, 
				 rel->target->spot->sc_y - candidate.sc_y);
	  // Check that distance is within tolerance
	  if (dist < rel->sc_distance - rel->sc_tolerance or
	      dist > rel->sc_distance + rel->sc_tolerance)
	    {
#ifdef DEBUG
	      _DEBUG_("Distance to anchor " << rel->target->index 
		      << " doesn't match (is " << dist 
		      << ", should be " << rel->sc_distance << ")");
#endif
	      return false;
	    }
	  
	  // Heading if match is correct
	  double heading = (__angle(rel->target->spot->sc_x - candidate.sc_x, 
				    rel->target->spot->sc_y - candidate.sc_y)
			    - rel->angle);
	  
	  if (noHeadingRef)
	    {
	      // Use this result for future comparison
	      headingRef = heading;
	      noHeadingRef = false;
	    }
	  else
	    {
	      // Normalize (to overcome the problem that the angles PI and -PI 
	      // are considered to be very different, when they are in fact the 
	      // same angle)
	      for (; heading - headingRef > M_PI;  heading -= 2*M_PI);
	      for (; heading - headingRef < -M_PI; heading += 2*M_PI);
	      
	      // Compare
	      if (abs(heading - headingRef) > m_angleTolerance)
		{
#ifdef DEBUG
		  _DEBUG_("Heading computed from relation to anchor "
			  << rel->target->index 
			  << " doesn't match previous result (expected " 
			  << headingRef*180/M_PI << ", is "
			  << heading*180/M_PI);
#endif
		  return false;
		}
	    }
	}
    }
  
  // All within the tolerance
#ifdef DEBUG
  _DEBUG_("Matched anchor " << index 
	  << " to spot " << candidate.x << "," << candidate.y 
	  << " (scene " << candidate.sc_x << "," << candidate.sc_y << ")");
#endif
  return true;
}

//----------------------------------------------------------------------

void Anchor::estimateSpot(double xRobot, double yRobot, double heading)
{
#ifdef DEBUG
  if (spot == NULL)
    {
      _ERROR_("Unexpected nullpointer in anchor " << index << "! Spot shouldn't be "
	      << "NULL if it is to be estimated.");
      spot = new ImageSpot();
    }
#endif
  
  // The direction of the anchor from the robot center
  double currentAngle = m_centreAngle + heading;
  
  // Calculate anchor location
  spot->sc_x = xRobot + __x(m_sc_centreDistance, currentAngle);
  spot->sc_y = yRobot + __y(m_sc_centreDistance, currentAngle);
  
  // Translate to image coordinates
  Point p = m_cam.inverseTransform(spot->sc_x, spot->sc_y);
  spot->x = static_cast<int>(p.x);
  spot->y = static_cast<int>(p.y);
}

