#include "ClusteringImageProcessor.hh"
#include "ColourClusterSetLoader.hh"
#include "ColourCluster.hh"
#include "Exception.hh"
#include "ConvMask.hh"

#include <cmath>
#include <utility>

#define THRESHOLD 68

using namespace std;


//----------------------------------------------------------------------
// Maximum & minimum functions to shorten lines.
inline int max(int n1, int n2)
{
  return n1>n2 ? n1 : n2;
}
inline int min(int n1, int n2)
{
  return n1>n2 ? n2 : n1;
}

//----------------------------------------------------------------------
//----------------------------------------------------------------------

ClusteringImageProcessor::ClusteringImageProcessor(const char* filename)
  : classifyAll(false),
    showClassification(false),
    threshold(THRESHOLD),
    m_cfile(filename),
    m_minSpotSize(2),
    m_maxSpotSize(2),
    m_cvImg(NULL),
    m_pi(NULL),
    m_observeEdge(true),
    m_completeScanIn(10),
    m_scanIndex(0)
{
  // Load in the colours
  ColourClusterSetLoader loader(m_cfile);
  m_pal.clear();
  if (loader.load(m_pal))
    _ABORT_("Failed to initialise ClusteringImageProcessor");
}

//----------------------------------------------------------------------

ClusteringImageProcessor::~ClusteringImageProcessor()
{
  // Delete processed image
  delete m_pi;
}

//----------------------------------------------------------------------

void ClusteringImageProcessor::init(IplImage& cvImage, 
				    unsigned int width, unsigned int height, 
				    const list<int>& spotColours, 
				    const list<int>& spotSizes,
				    bool observeEdge,
				    int completeScanDuration)
{
  // Set the image buffer for the raw image
  m_cvImg = &cvImage;

  // Create buffer for processed image in same size
  m_pi = new ProcessedImage(width, height, m_pal.size());


  // Optimize representation for the spot colours: bool array indexed by 
  // colour. The size is defined by the special label ProcessedImage::noLabel
  // which is the higest value to be returned by jitc. NB: if noLabel is the
  // largest index, the size must be noLabel+1
  m_spotColours.clear();
  m_spotColours.resize(m_pi->noLabel+1, false);
  for (list<int>::const_iterator colour = spotColours.begin(); 
       colour != spotColours.end(); colour++)
    {

#ifdef DEBUG
      if (*colour >= (int) m_spotColours.size())
	_ERROR_("Colour index of spot colour larger that expeced. This "
		<< "violates the preconditions of the ProcessesImage "
		<< "constructor!");
#endif
      // Mark colour as potential spot colour
      m_spotColours.at(*colour) = true;
    }


  // Copy spot sizes
  m_spotSizes.clear();
  m_spotSizes.resize(spotSizes.size());
  copy(spotSizes.begin(), spotSizes.end(), m_spotSizes.begin());

  // Find maximum/minimum spot size
  m_minSpotSize = width;   // huge spot
  for (unsigned int i = 0; i < m_spotSizes.size(); i++)
    {
      m_minSpotSize = min(m_minSpotSize, m_spotSizes[i]);
      m_maxSpotSize = max(m_maxSpotSize, m_spotSizes[i]);
    }
  // Ensure minimum size of at least 2
  m_minSpotSize = max(m_minSpotSize, 2);


  // Print out colours and sizes
  ostringstream colourList, sizeList;
  for (list<int>::const_iterator c = spotColours.begin(); c != spotColours.end(); c++)
    colourList << *c << ",";
  for (list<int>::const_iterator s = spotSizes.begin(); s != spotSizes.end(); s++)
    sizeList << *s << ",";
  _INFO_("Watching out for colours {" << colourList.str()
	 << "\b} and sizes {" << sizeList.str() << "\b}");


  // Observe edge?
  m_observeEdge = observeEdge;
  
  // Set up a plan to scan the entire image for spots that have been lost.
  // The scan is done in lines, but not all lines are scanned at each call
  // of partialScan().
  m_completeScanIn = completeScanDuration < 0 ? : completeScanDuration;
  if (m_completeScanIn)
    {
      // Number of lines
      unsigned int numLines = (unsigned int) 
	ceil(static_cast<double>(m_pi->height()-4*m_maxSpotSize) / m_minSpotSize) - 1;
      
      // Per iteration
      double perStep = static_cast<double>(numLines) / m_completeScanIn;
      
      // Fill the array with begin and end indices for each iteration
      m_scanPlan.resize(m_completeScanIn);
      unsigned int first = 2*m_maxSpotSize + m_minSpotSize;
      unsigned int begin = first, end;
      for (unsigned int i = 0; i < m_completeScanIn; i++)
	{
	  // Calculate the line not to be scanned any more in that iteration
	  end = static_cast<unsigned int>(round(perStep * (i+1))) * m_minSpotSize
	        + first;
	  
	  // Store in the scanning plan
	  m_scanPlan[i] = make_pair(begin, end);
	  begin = end;
	}
    }
}

//----------------------------------------------------------------------

void ClusteringImageProcessor::processImage()
{
  // Reset all pixels to unclassified
  m_pi->clear();

  // Classify entire image
  if (classifyAll)
    classifyImage(*m_cvImg);
}

//----------------------------------------------------------------------

/**
 * Transform the image into a version which indicates the results of a
 * colour classification operation.
 */
void ClusteringImageProcessor::classifyImage(IplImage& cvImg)
{ 
  static unsigned char* pBlue;
  static unsigned char* pGreen;
  static unsigned char* pRed;

  pRed = pGreen = pBlue = (unsigned char*) cvImg.imageData;
  pGreen += 1;
  pRed += 2;

  // Loop over every pixel

  // Note - this routine assumes the cvImg is in a format in which the
  // pixels for an image row appear in successive memory locations. This
  // might not be the case for all image encodings. So, add a comment to
  // this routine to note that it works with bgr24 image formats.
  for (unsigned int i = 0; i < m_pi->pixelCount(); i++)
    {
      // Classify
      const ColourCluster& colClass = m_pal.classify(*pRed, *pGreen, *pBlue);

      // Reassign the pixel to indicate the colour class identified TODO
      // - these three operations could be ommitted for fastest
      // performance. Perhaps introduce a compile time flag, which takes
      // out all unneccessary code to the core functionality of tracking
      // and printing results.
      *pBlue = static_cast<unsigned char>(colClass.blue());
      *pGreen = static_cast<unsigned char>(colClass.green());
      *pRed = static_cast<unsigned char>(colClass.red());

      // Store the classification in the structure that is used for
      // future processing
      m_pi->unchecked_set(i, colClass.getID());

      // Move to the next pixel (assuming colour depth is 24 bits)
      pRed += 3;
      pGreen += 3;
      pBlue += 3;
    }
}

//----------------------------------------------------------------------

inline int ClusteringImageProcessor::convolve(int x, int y, const ImageSpot& spot)
{
  return convolve(x, y, spot.radius, spot.colour);
}

//----------------------------------------------------------------------

int ClusteringImageProcessor::convolve(int x, int y, int radius, int colour)
{
  ConvMask& cm = m_convList.getMask(radius, 0);
  
  const valarray<int>& maskX = cm.addPosX(x);
  const valarray<int>& maskY = cm.addPosY(y);
  
  return convolve(maskX, maskY, colour);
}

//----------------------------------------------------------------------

/**
 * TODO - this convolution is using a square region of a particular
 * radius. However, the actual feature points are circular, so something
 * todo here, and perhaps make use of valarrays, is to work out which of
 * the pixels in the square region are outside of the radius (and give
 * them a multiplier of 0. Since the radius is fixed, the convolution
 * masks can be set up at the start.
 */
int ClusteringImageProcessor::convolve(const std::valarray<int>& maskX,
				       const std::valarray<int>& maskY,
				       int colour)
{  
  static int tests, total;

  // Note, this calculation will need to change when I move over to
  // using circular regions
  tests = 0;
  total = 0;

  // assume masks are of same size
  for (unsigned int i = 0; i < maskX.size(); i++)
    {
      tests++;
      if (jitc(maskX[i], maskY[i]) == colour) total++;  // used to use .at
    }

  return  (tests == 0)? 0 : 100 * total / tests;
}

//----------------------------------------------------------------------

void ClusteringImageProcessor::erase(int x, int y, int r)
{
  ConvMask& cm = m_convList.getMask(r, 0);
  const valarray<int>& maskX = cm.addPosX(x);
  const valarray<int>& maskY = cm.addPosY(y);
  
  // assume masks are of same size
  for (unsigned int i = 0; i < maskX.size(); i++)
     m_pi->set(maskX[i], maskY[i], m_pi->noLabel);
}

//----------------------------------------------------------------------

void ClusteringImageProcessor::setRawPixel(int x, int y, int r, int g, int b)
{
  static unsigned char* pBlue;
  static unsigned char* pGreen;
  static unsigned char* pRed;

  pBlue = (unsigned char*) m_cvImg->imageData + 3 * (y * m_pi->width() + x);
  pGreen = pBlue + 1;
  pRed = pBlue + 2;

  *pBlue = static_cast<unsigned char>(b);
  *pGreen = static_cast<unsigned char>(g);
  *pRed = static_cast<unsigned char>(r);      
}

//----------------------------------------------------------------------

int ClusteringImageProcessor::findCenter(const int x, 
					 const int y,
					 ImageSpot& spot)
{
  static int xLeft;
  static int xRight;
  static int yUp;
  static int yDown;

  static int cScore;  // store result of a convolution

  static int csMax;    // highest conv score
  static int xTotal;   // running total of x coordinates of highest conv score
  static int yTotal;   // ditto, for y
  static int maxNum;   // number of pixels with highest conv score
  
  static int halfRadius = (spot.radius+1)>>1;   // ceil(spot.radius/2)

  xTotal = 0;
  yTotal = 0;
  maxNum = 0;

  // Set the running totals to the point passed in as the argument, because
  // it is precondition that this point has at least this->threshold 
  // convolution score.
  csMax = threshold;
  xTotal = x;
  yTotal = y;
  maxNum = 1;
  
  for (int i = 0; i < halfRadius; i++)   // half radius
    for (int j = 0; j <= i; j++)
      {
        xLeft = x + i;
        xRight = x + spot.radius - i;
        yUp = y - j;
        yDown = y + 1 + j;

        /*// Draw the center-search regions
        if (showClassification)
          {
            setRawPixel(xLeft, yUp, 255, 0, 0);
            setRawPixel(xLeft, yDown, 230, 0, 0);
            setRawPixel(xRight, yUp, 210, 0, 0);
            setRawPixel(xRight, yDown, 190, 0, 0);
	    }*/

        // upper left quandrant of diamond
        cScore = convolve(xLeft, yUp, spot);
        if (cScore > csMax)
        {
          csMax = cScore;
          xTotal = xLeft;
          yTotal = yUp;
          maxNum = 1;
        }
        else if (cScore == csMax)
          {
            xTotal += xLeft;
            yTotal += yUp;
            maxNum++;
          }

        // upper right quandrant of diamond
        cScore = convolve(xRight, yUp, spot);
        if (cScore > csMax)
        {
          csMax = cScore;
          xTotal = xRight;
          yTotal = yUp;
          maxNum = 1;
        }
        else if (cScore == csMax)
          {
            xTotal += xRight;
            yTotal += yUp;
            maxNum++;
          }

        // lower right quandrant of diamond
        cScore = convolve(xRight, yDown, spot);
        if (cScore > csMax)
        {
          csMax = cScore;
          xTotal = xRight;
          yTotal = yDown;
          maxNum = 1;
        }
        else if (cScore == csMax)
          {
            xTotal += xRight;
            yTotal += yDown;
            maxNum++;
          }

        // lower left quandrant of diamond
        cScore = convolve(xLeft, yDown, spot);
        if (cScore > csMax)
        {
          csMax = cScore;
          xTotal = xLeft;
          yTotal = yDown;
          maxNum = 1;
        }
        else if (cScore == csMax)
          {
            xTotal += xLeft;
            yTotal += yDown;
            maxNum++;
          }
      }
  
  spot.x = (xTotal / maxNum);
  spot.y = (yTotal / maxNum);
  return csMax;
}

//----------------------------------------------------------------------

void ClusteringImageProcessor::findSpots(list<ImageSpot*>& spots)
{
  // Look for spots in the smallest spot size
  int size = m_spotSizes[0]; 

  // Search in entire image
  for (unsigned int x = 0; x < m_pi->width(); x++)
    for (unsigned int y = 0; y < m_pi->height(); y++)
      {
	// Check wether there is a spot around the current position in a colour 
	// we're looking for (only consider the colour of the current pixel)
	int spotColour = jitc(x,y);
	if ( m_spotColours.at(spotColour) )
	  {
	    int rcs = convolve(x,y, size, spotColour);
	    if (rcs > threshold)
	      {
		// Get the center of that spot
		ImageSpot spot;
		spot.colour = spotColour;
		spot.radius = size;
		rcs = findCenter(x, y, spot);
		
		// Check wether there is acutally a bigger spot
		ImageSpot biggerSpot;
		biggerSpot.colour = spotColour;
		for (unsigned int i = 1; i < m_spotSizes.size(); i++)
		  {
		    biggerSpot.radius = m_spotSizes[i];
		    if (findCenter(spot.x, spot.y, biggerSpot) > threshold)
		      // Save bigger spot
		      spot = biggerSpot;
		    else
		      break;
		  }
		
		// Register image locations as "taken"
		erase(spot.x, spot.y, spot.radius);
		
		// Create instance on the heap
		ImageSpot* newSpot = new ImageSpot(spot);
		
		// Add to the result list
		spots.push_back(newSpot);
#ifdef DEBUG
		_DEBUG_("Full image spot search found (" << newSpot->x << "," 
			<< newSpot->y << ") with score=" << rcs);
#endif
	      }
	  }
      }
}

//----------------------------------------------------------------------

void ClusteringImageProcessor::findSpot(int x0, int x1, int y0, int y1, int colour, 
					std::list<ImageSpot*>& spots)
{
  // Search in range
  for (int x = x0; x <= x1; x++)
      for (int y = y0; y <= y1; y++)
	{
	  // Check wether there is a spot around the current position in the 
	  // smallest size.
	  int rcs = convolve(x, y, m_spotSizes.at(0), colour);
	  if (rcs > threshold)
	    {
	      // Get the center of that spot
	      ImageSpot spot;
	      spot.colour = colour;
	      spot.radius = m_spotSizes[0];
	      rcs = findCenter(x, y, spot);
	      
	      // Check wether there is acutally a bigger spot
	      ImageSpot biggerSpot;
	      biggerSpot.colour = colour;
	      for (unsigned int i = 1; i < m_spotSizes.size(); i++)
		{
		  biggerSpot.radius = m_spotSizes[i];
		  if (findCenter(spot.x, spot.y, biggerSpot) > threshold)
		    // Save bigger spot
		    spot = biggerSpot;
		  else
		    break;
		}
	      
	      // Register image locations as "taken"
	      erase(spot.x, spot.y, spot.radius);

	      // Create instance on the heap
	      ImageSpot* newSpot = new ImageSpot(spot);
	      
	      // Add to the result list
	      spots.push_back(newSpot);
#ifdef DEBUG
	      //	      _DEBUG_("Range search found (" << newSpot->x << "," 
	      //      << newSpot->y << ") with score=" << rcs);
#endif
	    }
	}
}

//----------------------------------------------------------------------

bool ClusteringImageProcessor::trackSpot(ImageSpot& spot, int clip)
{
  int xMax = spot.x + clip;
  int yMax = spot.y + clip;

  // Search for the spot in the clipping region
  for (int x = spot.x - clip; x < xMax; x++)
    for (int y = spot.y - clip; y < yMax; y++)
      
      // Check whether there is a spot in the right colour and size
      if (jitc(x,y) == spot.colour and convolve(x,y, spot) > threshold)
        {
          // Find center
          findCenter(x, y, spot);
	  //spot.x = x;
	  //spot.y = y;

          // Register image locations as "taken"
          erase(spot.x, spot.y, spot.radius);
          return true;
	}
  
  // None found
  return false;
}
