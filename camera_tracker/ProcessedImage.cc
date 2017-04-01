#include "ProcessedImage.hh"
#include "Exception.hh"

#ifdef DEBUG
#include <iomanip>
#endif


//----------------------------------------------------------------------

ProcessedImage::ProcessedImage(unsigned int width, unsigned int height,
			       storage_type colours)
  : m_width(width),
    m_height(height),
    m_pixCount(width*height),
    m_mem(static_cast<storage_type>(0), width*height),
    noLabel(colours)
{
#ifdef DEBUG
  _DEBUG_((int) colours << " colours");
#endif

  // Determine number of bits necessary to store the colours and noLabel
  // (the colours are indexed 0..colours-1, noLabel is colours)
  int bits = 0;
  for (; colours; colours = colours>>1)
    bits++;

#ifdef DEBUG
  _DEBUG_(bits << " bits");
#endif
  
  // Create the bit patterns for the storage range
  m_rangeIncr = 1<<bits;
  m_rangeMax = ~( ~static_cast<storage_type>(0) << sizeof(storage_type)*8-1 )
               & ~(m_rangeIncr - 1);  // all ones except MSB and last "bits" bits
  
  // Set m_range so that the value 0 is unclassified
  m_range = m_rangeIncr;
  
#ifdef DEBUG
  _DEBUG_("m_rangeIncr " << std::showbase << std::hex << (int) m_rangeIncr);
  _DEBUG_("m_rangeMax "  << (int) m_rangeMax << std::dec);
  
  _DEBUG_("Created processed image buffer, size " << width << " x " << height);
#endif
}

//----------------------------------------------------------------------
 
void ProcessedImage::resize(int width, int height)
{
  // Resize the image
  m_width = width;
  m_height = height;
  m_pixCount = width*height;
  
  // Resize the memory
  m_mem.resize(m_pixCount, static_cast<storage_type>(0));
  
  // Reset the storing range
  m_range = m_rangeIncr;

  _DEBUG_("Resized processed image buffer, size " << width << " x " << height);
}
