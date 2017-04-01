#ifndef _COLOURCLUSTERSETLOADER_HH_
#define _COLOURCLUSTERSETLOADER_HH_

#include <string>

// Forward
class ColourClusterSet;

/**
 * Provides loading and parsing of a disk file to return a set of colour
 * sets.
 */
class ColourClusterSetLoader
{
public:

  /**
   * Creates an instance to load the file with the specified filename.
   */
  ColourClusterSetLoader(const std::string& filename);

  /**
   * Attempt to load and parse the file
   *
   * \return True if there was an error reading the file (in which case
   * the datae was not correctly loaded), or false if there were no
   * problems.
   */
  bool load(ColourClusterSet& set);

private:

  /**
   * Read and ingore a string from the file, and check it for equality
   * against the given parameter.
   *
   * \return True if there was a file read or data error, or false if
   * there were no problems
   */
  bool fSkip(std::ifstream& in, const std::string& expected);

  /**
   * Read and ingore a string from the file, and check it for equality
   * with the given parameter.
   */
  bool fSkip(std::ifstream& in);

private:
  /// Store the filename to load
  const std::string m_filename;

  /// Count of number of data points comprising cluster
  int m_population;
};

#endif
