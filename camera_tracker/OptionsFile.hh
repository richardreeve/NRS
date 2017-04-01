#ifndef _OPTIONSFILE_HH_
#define _OPTIONSFILE_HH_

#include <map>
#include <string>

#include "OptionsReader.hh"

/**
 * Reads configuration options from a plain text file containing simple
 * name & value pairs.
 */
class OptionsFile : public OptionsReader
{
public:
  /**
   * Constructor. Creates an instance to read the file with the
   * specified filename. The constructor does not cause the actual file
   * reading to take place.
   */
  OptionsFile(const char* filename);

  /**
   * Causes the file to be read and parsed. Name-value pairs are loaded
   * into internal store, and can be queried later. This method can be
   * called multiple times to allow later changes in the options file to
   * be loaded into the internal name-value memory.
   *
   * Note, this method throws an exception of type NRS::Base::Exception for
   * file or file-content errors.
   */
  void read();

  /**
   * Implement OptionsReader interface
   */
  virtual bool contains(const std::string& key) const;

  /**
   * Implement OptionsReader interface
   */
  std::string value(const std::string& key) const;

  /**
   * Provide virtual destructor since this class inherits an interface.
   */
  virtual ~OptionsFile();

  /**
   * Return the name of the options file
   */
  std::string getFilename() const { return m_filename; }

private:
  OptionsFile(const OptionsFile&);
  OptionsFile& operator=(const OptionsFile&);

  /**
   * Trim the leading and trailing whitespaces from a string
   */
  std::string trim(std::string s) const;

  /// Name of options file
  std::string m_filename;

  /// Name & value pairs
  std::map<std::string, std::string> m_pairs;
};

#endif
