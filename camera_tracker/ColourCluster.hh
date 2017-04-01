#ifndef _COLOURCLUSTER_HH_
#define _COLOURCLUSTER_HH_

#include <valarray>
#include <string>

// Local constants
#define _NRS_RED 0
#define _NRS_GREEN 1
#define _NRS_BLUE 2

/**
 * Represents the numerical description of an identified colour
 * cluster. This class has not been designed or implemented for
 * inheritance.
 */
class ColourCluster
{
public:
  /**
   * Constructor. A copy of the array data is taken by the new object.
   *
   * @param population number of data points comprising cluster
   *
   * @param conInvs array represent the inverse of the covariance matrix
   *
   * @param meanR value of mean for the red component
   *
   * @param meanG value of mean for the green component
   *
   * @param meanB value of mean for the blue component
   *
   * @param label string name for this colour
   */
  ColourCluster(int population,
                const double covInvs[],
                double meanR, double meanG, double meanB,
                const std::string& label);

  /**
   * Return the number of points making up this cluster
   */
  int getPopulation() const
  {
    return m_population;
  }

  /**
   * Return the normalised probability that the given colour, specified
   * by the R, G, B values, belongs to this cluster. The RGB values
   * should be constrainted to the range 0...255.
   */
  double prob(int R, int G, int B) const;

  /**
   * Return a probability ordering value, which can be used instead of
   * the normalised probability to determine which, out of a set of
   * colours, has the highest probability. So while the function doesn't
   * return the probability, it does allow a much faster determination
   * of which colour probability will be highest.
   */
  double logProb(int R, int G, int B) const;

  /**
   * Return the name associated with this colour
   */
  std::string toString() const
  {
    return m_label;
  }

  /**
   * Cause this colour cluster to output all its details
   */
  void dump() const;

  /**
   * Return the red component of the mean colour
   */
  double red() const { return m_means[_NRS_RED]; }

  /**
   * Return the blue component of the mean colour
   */
  double blue() const { return m_means[_NRS_BLUE]; }

  /**
   * Return the green component of the mean colour
   */
  double green() const { return m_means[_NRS_GREEN]; }

  /**
   * Get the ID of this colour
   */
  int getID() const { return m_ID; }


private:
  // Prohibit copy constructor, and assignment operator
  ColourCluster(const ColourCluster&);
  ColourCluster& operator=(const ColourCluster&);

  /**
   * Set the ID of this colour
   */
  void setID(int ID) { m_ID = ID; }

  /// Count of number of data points comprising cluster
  int m_population;

  /// Colour means, in order red, green, blue
  std::valarray<double> m_means;

  /// Store the invariance covariances
  std::valarray<double> m_cov;

  /// User provided labal
  std::string m_label;

  /// Integer ID
  int m_ID;
  
  friend class ColourClusterSet;
};

#endif
