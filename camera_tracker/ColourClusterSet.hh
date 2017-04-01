#ifndef _COLOURCLUSTERSET_HH_
#define _COLOURCLUSTERSET_HH_

#include <valarray>
#include <vector>

// Forward
class ColourCluster;

/**
 * Represent a set of colour clusters
 */
class ColourClusterSet
{
public:

  /**
   * Default constructor.
   */
  ColourClusterSet() {}

  /**
   * Destructor. Not virtual, so don't inherit from this class!
   */
  ~ColourClusterSet();

  /**
   * Add the ColourCluster object to this set.
   */
  void add(ColourCluster& _cluster);

  /**
   * Add colour cluster set from raw arrays. Local copies of all data
   * are taken, so client maintains responsibility for delete.
   */
  void add(double* covar, double* means, int number, const char* label);

  /**
   * Return a cluster by its index.  The caller must ensure the index
   * parameter is in the range 0 ... this.size()-1
   */
  const ColourCluster& cluster(int index) const
  {
    return *(m_set[index]);
  }

  /**
   * Lookup a colour by its name, and return its index number. Returns
   * -1 if the colour was not found.
   */
  int cluster(const std::string& colName) const;

 /**
   * Classify the input colour and return the associated ColourCluster
   * object.  The classification works by seeking for the maximum
   * cluster probability.
   *
   * WARNING: this code will cause application exit if called on a set
   * which contains no colour clusters, ie, size() = 0
   */
  const ColourCluster& classify(int R, int G, int B);

  /**
   * Return the number of colour clusters registered with this class
   */
  int size() const 
  { 
    return m_set.size(); 
  }

  /**
   * Clear all colours from this colour cluster set
   */
  void clear();

public:
  typedef std::vector<ColourCluster*>::iterator iterator;

private:private:
  // Prohibit copy constructor, and assignment operator
  ColourClusterSet(const ColourClusterSet&);
  ColourClusterSet& operator=(const ColourClusterSet&);

  /// The set of ColourCluster objects
  std::vector<ColourCluster*> m_set;
};

#endif
