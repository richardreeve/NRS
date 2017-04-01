#ifndef _CLUSTERINGIMAGEPROCESSOR_HH_
#define _CLUSTERINGIMAGEPROCESSOR_HH_

#include <string>
#include <vector>
#include <list>
#include <valarray>

#include "opencv/cv.h"
#include "ColourCluster.hh"
#include "ColourClusterSet.hh"
#include "ProcessedImage.hh"
#include "ConvMaskManager.hh"
#include "ImageSpot.hh"


using namespace std;

class ClusteringImageProcessor
{
public:
  /**
   * Create a new image processor, based on the colour cluster
   * information contained in the specified file.
   * \param filename The name of the colours file to load
   */
  ClusteringImageProcessor(const char* filename);
  
  /** 
   * Destructor. Deletes the ProcessesImage
   */
  ~ClusteringImageProcessor();
  
  /**
   * Set the buffer containing the raw image. Also set the colours and sizes 
   * of potential spots.
   */
  void init(IplImage& cvImage, unsigned int width, unsigned int height, 
	    const std::list<int>& colours, const std::list<int>& sizes,
	    bool observeEdge = true, int completeScanDuration = 1); 
  
  /**
   * Should be called at the start of each image processing
   * cycle. Resets self, and optionally will classify the entire image.
   */
  void processImage();
  
  /**
   * Perform just-in-time-classification (jitc) of the specified pixel
   * location.
   */
  inline int jitc(const int& x, const int& y);
  
  /*
   * Finds all spots with any of the potential spot colours and sizes (must
   * be set through init()) anywhere in the image. Bigger spots will only be 
   * reported with the largest size they match. Found spots are added to the 
   * specified list.
   */
  void findSpots(std::list<ImageSpot*>& spots);
  
  /**
   * Attempt to a find a spot that has been found in the last frame. The search
   * is done around the last location, and the spot's clip property determines
   * the size of the search area. 
   *
   * Returns true if the spot was located successfully.
   */
  bool trackSpot(ImageSpot& spot, int clip);
  
  /**
   * Look at the edge of the image for new spots appearing. The search is 
   * actually done far enough away from the image edge, so that only complete 
   * spots will be discovered. The new spots will be added to the argument list.
   *
   * The search is done in an efficient way: Since the spots have a minimum size
   * not every pixel on the edge needs to be investigated. The function just
   * looks at enough pixels so that that smallest spots are discovered. (There
   * is one limitation to that: the investigated pixels are on straight lines
   * near the edge of the image. If the frame rate is too low it can indeed
   * happen that a spot gets across that line without touching it at any time.
   * It is recommended to increase the frame rate, e.g. by choosing a lower
   * resolution so that a clip smaller than a radius is sufficient.)
   *
   * Inlined to increase performance, because is part of the main loop (as 
   * normal function the tracker is about 8 ms slower per frame). Definition 
   * at the end of the file.
   */
  inline void observeEdge(int maxClip, std::list<ImageSpot*>& spots);
  
  /**
   * Scan the inside of the image to rediscover spots that were lost completely
   * (e.g. if two out of three spots of a robot were occluded). This is done in
   * an efficient way, most pixels can be skipped while still guaranteeing that 
   * points are discovered. 
   * A call to this function only scans a part of the image, and subsequent calls
   * will scan a different section. Eventually the entire image is scanned. How 
   * many calls this will take can be configured through init().
   */
  inline void partialScan(std::list<ImageSpot*>& spots);


  /**
   * Get the processed image
   */
  inline const ProcessedImage& getPI() const { return *m_pi; }

  /**
   * Return the colour palette define for this image processing
   */
  const ColourClusterSet& getPalette() const { return m_pal; }

  /**
   * Flag whether the complete image should be classified at the start
   * of each image processing step. This results in very slow
   * performance, but can be useful for debugging.
   */
  bool classifyAll;

  /**
   * Flag whether classification result (only for pixels that are classified)
   * should be displayed. Set to false for better performance. Is ignored if
   * classifyAll == true.
   */
  bool showClassification;

  /**
   * The threshold (percentage of circle filled with pixels of the desired
   * colour) for the search for spots. Ranges from 0 to 100 (percent). For most 
   * applications the default value is very reasonable. However if spot sizes
   * are very close, one might consider a higher value.
   */
  int threshold;

private:
  /**
   * Transform the image into a version which indicates the results of a
   * colour classification operation.
   */
  void classifyImage(IplImage& cvImg);

  /**
   * TODO - add a comment
   */
  int convolve(const std::valarray<int>& maskX,
               const std::valarray<int>& maksY,
               int colour);

  int convolve(int x, int y, const ImageSpot& spot);
  int convolve(int x, int y, int radius, int colour);

  /**
   * Set a pixel in the raw image - used for debugging
   */
  inline void setRawPixel(int x, int y, int r, int g, int b);

  /**
   * Find the highest convolution score in the region to the right of (x, y).
   * The result is stored in spot. Important: spot must be initialized with
   * it's colour and radius.
   */
  int findCenter(const int x, const int y, ImageSpot& spot);

  /**
   * Erase a located spot from the processing image buffer (to prevent
   * other searches from finding it)
   */
  void erase(int x, int y, int r);

  /**
   * Find a spot with maximal size and given colour in the specified area. The
   * search is done in the box defined by (x0, y0) and (x1, y1), inclucing the
   * border. The found spot (if there is one) is added to the list.
   */
  void findSpot(int x0, int x1, int y0, int y1, int colour, 
		std::list<ImageSpot*>& spots);

  
  // Prohibit copying and assignment
  ClusteringImageProcessor(const ClusteringImageProcessor&);
  ClusteringImageProcessor& operator=(const ClusteringImageProcessor&);


private:
  /// Name of the cluster file
  std::string m_cfile;

  /// The colour palette
  ColourClusterSet m_pal;

  /// Array indexed by colour that stores whether spots can have that colour. 
  //  m_spotColours[c]==true means that there are spots with colour c.
  std::vector<bool> m_spotColours;
  
  /// Sizes that spots can have
  std::vector<int> m_spotSizes;
  int m_minSpotSize;
  int m_maxSpotSize;

  /// The raw image which is to be processed. Caller responsible for resource.
  IplImage* m_cvImg;

  /// Used to store the results of image processing. Owned by this object.
  ProcessedImage* m_pi;

  /// Convolution masks
  ConvMaskManager m_convList;
  
  /// Observe the edge of the image. If set to false observeEdge() returns
  //  immediately.
  bool m_observeEdge;
  
  /// Variables for the partial image scan (number of iterations for a complete
  //  scan, current iteration, scanning plan). The plan contains begin and end
  //  y coordinate for each scanning iteration. Some iterations may not scan 
  //  any lines, in which case begin == end.
  unsigned int m_completeScanIn, m_scanIndex;
  vector< pair<unsigned int, unsigned int> > m_scanPlan;
};

//----------------------------------------------------------------------
//----------------------------------------------------------------------

inline int ClusteringImageProcessor::jitc(const int& x, const int& y)
{
  static unsigned char* pBlue;
  static unsigned char* pGreen;
  static unsigned char* pRed;
  
  int pixColour = m_pi->at(x,y);
  
  // If this pixel has already been classified, then return the known
  // result
  if (pixColour >= 0) 
    return pixColour;
  
  pBlue = reinterpret_cast<unsigned char*>( m_cvImg->imageData 
                                            + 3 * (y * m_pi->width() + x) );
  pGreen = pBlue + 1;
  pRed = pBlue + 2;
  
  // Classify pixel
  const ColourCluster& colClass = m_pal.classify(*pRed, *pGreen, *pBlue);
  
  // Store the classification at the pixel location 
  m_pi->set(x, y, colClass.getID());

  // Reassign the pixel to indicate the colour class identified
  if (showClassification)
    {
      *pBlue = static_cast<unsigned char>(colClass.blue());
      *pGreen = static_cast<unsigned char>(colClass.green());
      *pRed = static_cast<unsigned char>(colClass.red());
    }
  
  return colClass.getID();
}

//----------------------------------------------------------------------

inline void ClusteringImageProcessor::partialScan(std::list<ImageSpot*>& spots)
{
  // Disabled?
  if (!m_completeScanIn)
    return;
  
  // Stay away from the image edge (done by observeEdge)
  unsigned int left = m_maxSpotSize<<1;
  unsigned int width = m_pi->width()-left;
  
  // Search range to confirm initial suspicion
  int minDiameter = m_minSpotSize<<1;

  
  // Do the lines in the plan for this iteration
  for (unsigned int y = m_scanPlan.at(m_scanIndex).first; 
       y < m_scanPlan[m_scanIndex].second; y += m_minSpotSize)
    
    // Scan entire line
    for (unsigned int x = left + m_minSpotSize;
	 x < width; x += m_minSpotSize)
      {
	// Spot colour?
	int colour = jitc(x, y);
	if (m_spotColours[colour])
	  {
	    // Look on a horizonal and a vertical line to see whether there are more
	    // spots of that colour (or if the classification was a bit noisy)
	    int colourPixels = 0;
	    for (int i = -m_minSpotSize; i <= minDiameter; i++)
	      {
		colourPixels += (colour == jitc(x+i, y));
		colourPixels += (colour == jitc(x, y+i));
	      }
	    
	    // If there are more spots of that colour, search for centre
	    if (colourPixels >= m_minSpotSize)
	      findSpot(x-m_minSpotSize, x+m_minSpotSize, 
		       y-m_minSpotSize, y+m_minSpotSize, colour, spots);
	  }
      }
  
  // Increment the index, restart if scan completed
  if (++m_scanIndex == m_completeScanIn)
    m_scanIndex = 0;
}


//----------------------------------------------------------------------

inline void ClusteringImageProcessor::observeEdge(int maxClip,
						  std::list<ImageSpot*>& spots)
{
  // Disabled?
  if (!m_observeEdge)
    return;
  
  // Stay away from the image edge so that even the biggest spots are completely 
  // in the image before being discovered (one diameter). Otherwise a big spot
  // could be mistaken as a small spot
  int topLeft = m_maxSpotSize<<1;
  int width = m_pi->width()-topLeft;
  int height = m_pi->height()-topLeft;
  
  // How far the center could have moved towards the inside when it's discovered
  // now for the first time (relateive to search location).
  int inside = maxClip - m_minSpotSize;
  int minDiameter = m_minSpotSize<<1;
  int minHalfRadius = m_minSpotSize>>1;


  /// Search the corners
  // There are several assumptions about where the centers of spots can't be
  // at the eges of the image (see below) which don't hold for the corners.
  // Therefore investigate the corners first.
  
  // Top left corner
  int colour = jitc(topLeft, topLeft);
  if (m_spotColours[colour])
    {
      // Look on a horizonal and a vertical line to see whether there are more
      // spots of that colour (or if the classification was a bit noisy)
      int colourPixels = 0;
      for (int i = -minDiameter; i <= m_minSpotSize; i++)
	{
	  colourPixels += (colour == jitc(topLeft+i, topLeft));
	  colourPixels += (colour == jitc(topLeft, topLeft+i));
	}
      
      // If there are more spots of that colour, search for centre
      if (colourPixels >= m_minSpotSize)
	findSpot(topLeft-m_minSpotSize, topLeft+m_minSpotSize, 
		 topLeft-m_minSpotSize, topLeft+m_minSpotSize, colour, spots);
    }

  // Top right corner
  colour = jitc(width, topLeft);
  if (m_spotColours[colour])
    {
      // Look on a horizonal and a vertical line to see whether there are more
      // spots of that colour (or if the classification was a bit noisy)
      int colourPixels = 0;
      for (int i = -minDiameter; i <= m_minSpotSize; i++)
	{
	  colourPixels += (colour == jitc(width-i, topLeft));
	  colourPixels += (colour == jitc(width, topLeft+i));
	}
      
      // If there are more spots of that colour, search for centre
      if (colourPixels >= m_minSpotSize)
	findSpot(width-m_minSpotSize,   width+m_minSpotSize, 
		 topLeft-m_minSpotSize, topLeft+m_minSpotSize, colour, spots);
    }

  // Bottom right corner
  colour = jitc(width, height);
  if (m_spotColours[colour])
    {
      // Look on a horizonal and a vertical line to see whether there are more
      // spots of that colour (or if the classification was a bit noisy)
      int colourPixels = 0;
      for (int i = -minDiameter; i <= m_minSpotSize; i++)
	{
	  colourPixels += (colour == jitc(width-i, height));
	  colourPixels += (colour == jitc(width, height+i));
	}
      
      // If there are more spots of that colour, search for centre
      if (colourPixels >= m_minSpotSize)
	findSpot(width-m_minSpotSize,  width+m_minSpotSize, 
		 height-m_minSpotSize, height+m_minSpotSize, colour, spots);
    }
  
  // Bottom left corner
  colour = jitc(topLeft, height);
  if (m_spotColours[colour])
    {
      // Look on a horizonal and a vertical line to see whether there are more
      // spots of that colour (or if the classification was a bit noisy)
      int colourPixels = 0;
      for (int i = -minDiameter; i <= m_minSpotSize; i++)
	{
	  colourPixels += (colour == jitc(topLeft+i, height));
	  colourPixels += (colour == jitc(topLeft, height+i));
	}
      
      // If there are more spots of that colour, search for centre
      if (colourPixels >= m_minSpotSize)
	findSpot(topLeft-m_minSpotSize, topLeft+m_minSpotSize, 
		 height-m_minSpotSize,  height+m_minSpotSize, colour, spots);
    }
  
  /// Search the edges
  // There are certain limitation in where the center of a spot can be, given
  // that it is first discovered in iteration i:
  //  - In perpendicular direction it can only be a limited amount towards the 
  //    inside of the image, because otherwise it would have been discovered 
  //    in the previous frame (maximum clip from where it just would not have
  //    been discovered).
  //  - In perpendicular direction towards the outside of the image the code
  //    is only looking one minimum radius away from the search line, because
  //    findSpot first only looks for the smallest spot size (and then checks
  //    whether the spot is actually bigger). And a small spot can be found
  //    one minimum radius away from the search line if there is a big spot.
  //  - In the search direction it is sufficient to look a minimum radius 
  //    away from the discovery point (same argumentation as previous item).
  //  - Opposite the search direction the spot can at most m_minSpotSize/2 
  //    away from the discovery point (otherwise it would have been discovered 
  //    in the previous iteration).
  
  // Search the top edge of the image
  for (int x = topLeft+m_minSpotSize; x < width; x += m_minSpotSize)
    {
      // A spot colour at that point?
      colour = jitc(x, topLeft);
      if (m_spotColours[colour])
	{
	  // Look on a perpendicular line to see whether there are more spots
	  // of that colour (or if the classification was a bit noisy)
	  int colourPixels = 0;
	  for (int i = -minDiameter; i <= m_minSpotSize; i++)
	    colourPixels += (colour == jitc(x, topLeft+i));
	  
	  // If there are more spots of that colour, search for centre
	  if (colourPixels >= m_minSpotSize)
	    findSpot(x-minHalfRadius, x+m_maxSpotSize, 
		     topLeft-m_minSpotSize, topLeft+inside, colour, spots);
	}
    }
  
  // Search right edge
  for (int y = topLeft+m_minSpotSize; y < height; y += m_minSpotSize)
    {
      // A spot colour at that point?
      colour = jitc(width, y);
      if (m_spotColours[colour])
	{
	  // Look on a perpendicular line to see whether there are more spots
	  // of that colour (or if the classification was a bit noisy)
	  int colourPixels = 0;
	  for (int i = -minDiameter; i <= m_minSpotSize; i++)
	    colourPixels += (colour == jitc(width-i, y));
	  
	  // If there are more spots of that colour, search for centre
	  if (colourPixels >= m_minSpotSize)
	    findSpot(width-inside, width+m_minSpotSize, 
		     y-minHalfRadius, y+m_maxSpotSize, colour, spots);
	}
    }
  
  // Search bottom edge
  for (int x = width-m_minSpotSize; x > topLeft; x -= m_minSpotSize)
    {
      // A spot colour at that point?
      colour = jitc(x, height);
      if (m_spotColours[colour])
	{
	  // Look on a perpendicular line to see whether there are more spots
	  // of that colour (or if the classification was a bit noisy)
	  int colourPixels = 0;
	  for (int i = -minDiameter; i <= m_minSpotSize; i++)
	    colourPixels += (colour == jitc(x, height-i));
	  
	  // If there are more spots of that colour, search for centre
	  if (colourPixels >= m_minSpotSize)
	    findSpot(x-m_maxSpotSize, x+minHalfRadius, 
		     height-inside, height+m_minSpotSize, colour, spots);
	}
    }
  
  // Search left edge
  for (int y = height-m_minSpotSize; y > topLeft; y -= m_minSpotSize)
    {
      // A spot colour at that point?
      colour = jitc(topLeft, y);
      if (m_spotColours[colour])
	{
	  // Look on a perpendicular line to see whether there are more spots
	  // of that colour (or if the classification was a bit noisy)
	  int colourPixels = 0;
	  for (int i = -minDiameter; i <= m_minSpotSize; i++)
	    colourPixels += (colour == jitc(topLeft+i, y));
	  
	  // If there are more spots of that colour, search for centre
	  if (colourPixels >= m_minSpotSize)
	    findSpot(topLeft-m_minSpotSize, topLeft+inside, 
		     y-m_maxSpotSize, y+minHalfRadius, colour, spots);
	}
    }
}

#endif
