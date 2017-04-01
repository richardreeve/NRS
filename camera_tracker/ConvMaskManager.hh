
#ifndef _CONVMASKMANAGER_HH_
#define _CONVMASKMANAGER_HH_

#include <vector>

// Forward
class ConvMask;

/**
 * Acts as a manager for ConvMask objects. Any ConvMask objects added to
 * this manager are henceforth managed by this class, including,
 * importantly, objects deletion. I.e., once a ConvMask has been added
 * to this class, this class assumes responsibility for later deletion
 * of that object.
 */
class ConvMaskManager
{
public:
  /**
   * Default constructor
   */
  ConvMaskManager();

  /**
   * Destructor (destroys object)
   */
  ~ConvMaskManager();

  /**
   * Obtain a ConvMask object of the specified dimensions. If one
   * already exists in self, then it is returned. Otherwise a new
   * ConvMask is constructed and returned. Note, this class assumes
   * responsibility for future deletion of returned objects.
   */
  ConvMask& getMask(unsigned int posRad, unsigned int negWidth);
 
private:
  ConvMaskManager(const ConvMaskManager&);
  ConvMaskManager& operator=(const ConvMaskManager&);

  /// List of convolution masks that already exist
  typedef std::vector<ConvMask*> vector_list;
  vector_list m_masks;
};

#endif
