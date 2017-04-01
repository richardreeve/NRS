#include <math.h>

#include "ColourCluster.hh"
#include "ColourClusterSet.hh"

// Include an NRS source file
#include "Exception.hh"

//----------------------------------------------------------------------
ColourCluster::ColourCluster(int population,
              const double covInvs[],
              double meanR, double meanG, double meanB,
              const std::string& label)
  : m_population(population),
    m_means(3),
    m_cov(covInvs, 9),
    m_label(label),
    m_ID(-1)
{
    m_means[0] = meanR;
    m_means[1] = meanG;
    m_means[2] = meanB;

    // scale covariance values upfront, rather than - in prob(...) -
    // doing: return (-0.5 * retVal)
    m_cov *= -0.5;
}
//----------------------------------------------------------------------
double ColourCluster::prob(int R, int G, int B) const
{
  return exp(logProb(R,G,B));
}
//----------------------------------------------------------------------
double ColourCluster::logProb(int R, int G, int B) const
{
  static double retVal;
  static double r[3];

  // init variables
  r[0] = m_means[0] - R;
  r[1] = m_means[1] - G;
  r[2] = m_means[2] - B;

  // This was a for loop, but I unrolled it for greatest performance -
  // and it does actually increase runtime speed
  retVal = 
    r[0] * (m_cov[0] * r[0] + m_cov[1] * r[1] + m_cov[2] * r[2]) +
    r[1] * (m_cov[3] * r[0] + m_cov[4] * r[1] + m_cov[5] * r[2]) +
    r[2] * (m_cov[6] * r[0] + m_cov[7] * r[1] + m_cov[8] * r[2]);

  return retVal;
}
//----------------------------------------------------------------------
void ColourCluster::dump() const
{
  _DEBUG_("Label=" << m_label);

  for (int i = 0; i < 9; i++)  // 9 = size of covariance array
    _DEBUG_("cov[" << i << "]=" << m_cov[i]);

  for (int i = 0; i < 3; i++)
    _DEBUG_("means[" << i << "]=" << m_means[i]);

  _DEBUG_("Cluster size=" << m_population);
}
