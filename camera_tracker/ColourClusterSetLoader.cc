#include <fstream>

#include "Exception.hh"
#include "ColourClusterSet.hh"
#include "ColourClusterSetLoader.hh"

//----------------------------------------------------------------------
ColourClusterSetLoader::ColourClusterSetLoader(const std::string& filename)
  : m_filename(filename),
    m_population(0)
{
}
//----------------------------------------------------------------------
bool ColourClusterSetLoader::load(ColourClusterSet& set)
{
  std::ifstream in(m_filename.c_str());
  
  in >> m_population;
  
  if (fSkip(in, "invCovariances=[")) return true;
  
  // read in each covariance array
  double matrix[m_population][9];  // 9 = entries per single covar array
  for (int i = 0; i < m_population; i++)
    {
      for (int j = 0; j < 9; j++)   // 9 = entries per single covar array
        in >> matrix[i][j];
      
      if (fSkip(in, ";")) return true;
    }
  if (fSkip(in, "];")) return true;
  
  // read in each means vector
  if (fSkip(in, "means=[")) return true;
  double means[m_population][3];   // 3  = entries per single vector
  for (int i = 0; i < m_population; i++)
    {
      for (int j = 0; j < 3; j++)   // 3  = entries per single vector
        in >> means[i][j];
      
      if (fSkip(in, ";")) return true;
    }
  if (fSkip(in, "];")) return true;
  
  // read in the cluster sizes
  if (fSkip(in, "numbers=[")) return true;
  int numbers[m_population];
  for (int i = 0; i < m_population; i++)
    {
      in >> numbers[i];
      if (fSkip(in, ";")) return true;
    }
  if (fSkip(in, "];")) return true;
  
  // read in the data labels
  if (fSkip(in, "%labels")) return true;
  std::vector<std::string> labels;
  for (int i = 0; i < m_population; i++)
    {
      std::string temp;
      in >> temp;
      labels.push_back(temp);
      if (fSkip(in, "=") or fSkip(in) or fSkip(in, ";"))
        return true;
    }
  
  // Now load the data
  for (int i = 0; i < m_population; i++)
    set.add(&matrix[i][0], &means[i][0], numbers[i], labels[i].c_str());
  
  return false;
}
//----------------------------------------------------------------------
bool ColourClusterSetLoader::fSkip(std::ifstream& in, 
                                   const std::string& expected)
{
  std::string temp;
  
  in >> temp;
  
  if (temp != expected)
    {
      _WARN_("File format error. Expected \""
             << expected << "\" but read \"" << temp << "\"");
      return true;
    }
  return false;
}
//----------------------------------------------------------------------
bool ColourClusterSetLoader::fSkip(std::ifstream& in)
{
  std::string temp;
  
  in >> temp;
  
  return false;
}
