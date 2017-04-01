
#include "Exception.hh"
#include "ColourCluster.hh"
#include "ColourClusterSet.hh"

//----------------------------------------------------------------------
ColourClusterSet::~ColourClusterSet()
{
  for (iterator i = m_set.begin(); i != m_set.end(); i++)
    delete *i;
}
//----------------------------------------------------------------------
void ColourClusterSet::add(ColourCluster& _cluster)
{
  if (std::find(m_set.begin(), m_set.end(), &_cluster) != m_set.end())
    {
      _WARN_("Duplicate cluster [" << _cluster.toString() << "] ignored");
    }
  else
    {
      _cluster.setID(m_set.size());
      _INFO_("Registered colour \"" << _cluster.toString() << "\""
             << ", ID=" << m_set.size());
      m_set.push_back(&_cluster);
    }
}
//----------------------------------------------------------------------
void ColourClusterSet::add(double* covar, double* means,
                           int number, const char* label)
{
  ColourCluster* c = new ColourCluster(number, covar,
                                       means[0], means[1], means[2],
                                       label);
  add(*c);
}
//----------------------------------------------------------------------

// TODO - I removed a size==0 test - in order to speed things up
// marginnally. Instead put the test back into the init routine.
/**
 * Note, this routine does not calculate actual classification
 * probabilities. Doing so results in a call to exp(x), which is
 * massively slow! Instead, the argument to exp(x) is used for ordering
 * comparison, and this is okay because exp(x) is a monotonic-increasing
 * function. This kind of optimisation might be available whenever
 * exp(x) is encountered.
 */
const ColourCluster& ColourClusterSet::classify(int R, int G, int B)
{                   
  static double maxProb; 
  static iterator indexOfMax; 
  static double testProb;

  // take maxProb initial value from first colour
  indexOfMax = m_set.begin();
  maxProb = (*indexOfMax)->logProb(R,G,B);

  for (iterator i = m_set.begin()+1; i != m_set.end(); i++)
    {
      testProb = (*i)->logProb(R,G,B);
      if (testProb > maxProb)
        {
          maxProb = testProb;
          indexOfMax = i;
        }
    }

  return *(*indexOfMax);
}
//----------------------------------------------------------------------
void ColourClusterSet::clear()
{
  m_set.clear();
}
//----------------------------------------------------------------------
int ColourClusterSet::cluster(const std::string& colName) const
{
  for (unsigned int i = 0; i < m_set.size(); i++)
    if (m_set[i]->toString() == colName) return i;

  return -1;
}
