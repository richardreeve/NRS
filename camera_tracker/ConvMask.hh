#ifndef _CONVMASK_HH_
#define _CONVMASK_HH_

#include <valarray>

// Forward
class ConvMaskManager;

/**
 * Represent the locations involved in a circlular convolution operation.
 */
class ConvMask
{
  friend class ConvMaskManager;

public: 
  /**
   * Return the radius of the central postive zone
   */
  inline unsigned int getPosRadius() const { return m_pRad; }

  /**
   * Return the outermost radius, which is the radius of the central
   * positive zone added with the width of the negative outer ring.
   */
  inline unsigned int getNegRadius() const { return m_nRad; }

  /**
   * Return the width of the outer negative ring
   */
  inline unsigned int getNegWidth() const { return m_nWidth; }

  /**
   * Access the array of X components of locations within the positive
   * central region
   */
  inline const std::valarray<int>& posX() const { return m_posX; }

  /**
   * Access the array of Y components of locations within the positive
   * central region
   */
  inline const std::valarray<int>& posY() const { return m_posY; }

  /**
   * Access the array of X components of locations within the negative
   * outer ring
   */
  inline const std::valarray<int>& negX() const { return m_negX; }

  /**
   * Access the array of Y components of locations within the negative
   * outer ring
   */
  inline const std::valarray<int>& negY() const { return m_negY; }

  /**
   * Add a fixed value to each element of the posX() array, and return the
   * result
   */
  inline const std::valarray<int>& addPosX(int i)
  { 
    m_regPosX = m_posX;
    return (m_regPosX += i);
  }

  /**
   * Add a fixed value to each element of the posY() array, and return the
   * result
   */
  inline const std::valarray<int>& addPosY(int i)
  { 
    m_regPosY = m_posY;
    return (m_regPosY += i);
  }

  /**
   * Add a fixed value to each element of the posX() array, and return the
   * result
   */
  inline const std::valarray<int>& addNegX(int i)
  { 
    m_regNegX = m_posX;
    return (m_regNegX += i);
  }

  /**
   * Add a fixed value to each element of the posY() array, and return the
   * result
   */
  inline const std::valarray<int>& addNegY(int i)
  { 
    m_regNegY = m_posY;
    return (m_regNegY += i);
  }

  /**
   * Print the conv maks offsets to stdout
   */
  void dump() const;

private:
  /**
   * Construct a convolution mask
   */
  ConvMask(unsigned int positiveRadius, unsigned int negativeWidth = 0);

  /**
   * Private destructor - means only friends can assume memory deletion
   * responsibilities.
   */
  ~ConvMask();

private:
  ConvMask(const ConvMask&);
  ConvMask& operator=(const ConvMask&);

  /// Radius of the positive circle
  unsigned int m_pRad;

  /// Radius of the negative area
  unsigned int m_nRad;

  /// Width of the negative outer ring
  unsigned int m_nWidth;

  /// Number of locations within the positive radius
  int m_posNum;

  /// Number of locations with the outer negative radius
  int m_negNum;

  /// X offsets within the positive radius
  std::valarray<int> m_posX;

  /// Y offsets within the positive radius
  std::valarray<int> m_posY;

  /// X offsets within the negative radius
  std::valarray<int> m_negX;

  /// Y offsets within the negative radius
  std::valarray<int> m_negY;

  /// Register used for X-positive calculations
  std::valarray<int> m_regPosX;

  /// Register used for Y-positive calculations
  std::valarray<int> m_regPosY;

  /// Register used for X-negative calculations
  std::valarray<int> m_regNegX;

  /// Register used for Y-negative calculations
  std::valarray<int> m_regNegY;
};

#endif
