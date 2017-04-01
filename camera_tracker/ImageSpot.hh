#ifndef _IMAGE_SPOT_HH_
#define _IMAGE_SPOT_HH_

/**
 * This class represents a spot in the image. Metrics are in pixels unless 
 * marked with sc_. The purpose of this class is to collect a set of image 
 * spots so that spots can be matched to robot anchors. 
 */
struct ImageSpot
{
  /** The centre of the spot. */
  int x, y;
  
  /** The radius of the spot. */
  int radius;
  
  /** The radius of the clipping area. */
  int clip;
  
  /** The colour of the spot. */
  int colour;

  /** The centre of the spot in scene coordinates. */
  double sc_x, sc_y;
};

#endif
