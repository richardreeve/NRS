#ifndef _OPTIONSREADER_HH_
#define _OPTIONSREADER_HH_

#include <string>

/**
 * Define an interface for accessing runtime options
 */
class OptionsReader
{
public:
  /**
   * Query whether an options value exists for the given key.
   *
   * \return true if an option value (even if the value is an empty
   * string) for the defined key exists, false other wise.
   */
  virtual bool contains(const std::string& key) const = 0;

  /**
   * Retrieve the value string associated with the given key. Returns an
   * empty string if the key was not contained within self.
   */
  virtual std::string value(const std::string& key) const = 0;
};

#endif
