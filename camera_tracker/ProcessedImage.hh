#ifndef _PROCESSED_IMAGE_
#define _PROCESSED_IMAGE_

#include <valarray>

#include "Exception.hh"

/**
 * Container for the classified image. Its main member is a valarray that
 * stores the colour label for each pixel. The array can store two special 
 * labels: unclassified for pixels that have not yet be classified and 
 * nolabel for pixels that have been used and should not be used again.
 *
 * As the just-in-time-classification (see ClusteringImageProcessor) does not
 * classify every pixel in every new frame, the classification has to be reset
 * to unclassified every frame. To save this effort (at least most of the time)
 * the new classification is simply stored in a numerical range above all
 * previously possible values. Array entries that are below that range then
 * represent unclassified.
 */
class ProcessedImage
{
public:
  // The type stored in the valarray (unsigned int seems to be the fastest)
  typedef unsigned int storage_type;


private:
  // Image size
  unsigned int m_width, m_height;
  
  // Number of pixels in the image
  unsigned int m_pixCount;


  // Start of current range. For the same example (5 colours) this always ends
  // with 000
  storage_type m_range;
  
  // Increment of the range start. To reset the classification m_range is
  // increased by this value. For 5 colours this is 8 (0...01000)
  storage_type m_rangeIncr;
  
  // Maximum value for m_range. If this value is reached, the array is acually
  // reset (to avoid underflow in at() and operator[]). For 5 colours this 
  // value would be 01..11000
  storage_type m_rangeMax;


  // Actual image data
  std::valarray<storage_type> m_mem;

public: 
  /** Value that indicates that this pixel has been used up. This is basicly 
      treated as if the pixel has the wrong colour. 
      NOTE: Given that no illegal values are stored (see set()), this is the 
      largest value ever to be returned by at() and operator[]. */
  const int noLabel;
  
  
  /** Get image width */
  inline unsigned int width() const { return m_width; }

  /** Get image height */
  inline unsigned int height() const { return m_height; }
  
  /** Get pixel count */
  inline unsigned int pixelCount() const { return m_pixCount; }


public:
  /**
   * Creates a processed image with the given size. The object needs to 
   * know the number of colours to be stored (requiring that they are indexed
   * from 0 to colours-1).
   */
  ProcessedImage(unsigned int width, unsigned int height, storage_type colours);
  
  /**
   * Resize the current image. Current contents of the image will be lost.
   */
  void resize(int width, int height);

  
  /**
   * Get colour label for pixel (x,y). If the pixel is unclassified the method 
   * returns a negative value (note that noLabel is positive). This call 
   * performs bounds checking (in terms of memory bounds, it wraps around from 
   * right to left), returning 'noLabel' if bounds are exceeded.
   */
  inline int at(unsigned int x, unsigned int y) const
  { 
    // Reuse x as linear index
    x += m_width * y;
    
    if (x < m_pixCount)
      return m_mem[x] - m_range;
    else
      return noLabel;
  }

  /**
   * Same as at() but without boundary checking. Note that it can not be 
   * used for setting a value. Use unchecked_set instead.
   */
  inline int operator[](storage_type n) 
  {
    return m_mem[n] - m_range;
  }
  

  /**
   * Set colour label for pixel (x, y). Boundaries are checked, violations
   * will be treated silently. The value colour must be smaller than the 
   * number of colours specified in the constructor (this is natually true
   * if they are numbered from 0..size-1).
   */
  inline void set(unsigned int x, unsigned int y, storage_type colour)
  { 
    // Reuse x to mean linear subscript
    x += m_width * y;
    
    if ( x < m_pixCount ) 
      m_mem[x] = m_range | colour;
  }
  
  /**
   * Same as set() but without boundary checking.
   */
  inline void unchecked_set(unsigned int n, storage_type colour) 
  {
    m_mem[n] = m_range | colour;
  }

  
  /**
   * Clear the image by increasing the current storing range. As a result
   * all pixels have the label unclassified because no array entries will
   * be in that range.
   */
  inline void clear() 
  {
    if (m_range == m_rangeMax)
      {
	// Now actually reset the image
	m_mem = static_cast<storage_type>(0);
	m_range = m_rangeIncr;

#ifdef DEBUG
	_DEBUG_("ProcessedImage reset");
#endif
      }
    else
      // Increase the storage range start
      m_range += m_rangeIncr;
  }

private:
  // Prohibit copying and assignment
  ProcessedImage(const ProcessedImage&);
  ProcessedImage& operator=(const ProcessedImage&);
  
};

#endif
