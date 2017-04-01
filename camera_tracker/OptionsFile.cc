#include <fstream>
#include <iostream>

#include "OptionsFile.hh"
#include "Exception.hh"

using namespace std;

//----------------------------------------------------------------------
OptionsFile::OptionsFile(const char* filename)
  : m_filename(filename)
{
}
//----------------------------------------------------------------------
OptionsFile::~OptionsFile()
{
}
//----------------------------------------------------------------------
std::string OptionsFile::trim(std::string s) const
{
  if (s.size())
    {
      // erase leading spaces
      s.erase(0, s.find_first_not_of(" "));

      // erase training spaces
      s.erase(s.find_last_not_of(" ")+1, string::npos);
    }

  return s;
}
//----------------------------------------------------------------------
void OptionsFile::read()
{
  ifstream in(m_filename.c_str());
  int lineNumber = 0;

  m_pairs.clear();  // because read() could be called multiple times

  while (!in.eof())
    { 
      string line;
      lineNumber++;
      
      getline(in, line); // read into a string

      // trim leading whitespace & ignore empty lines
      if (line.erase(0, line.find_first_not_of(" ")).size() == 0) continue;

      if (line[0] == '#') continue; // ignore comments      

      string::size_type delimPos = line.find_first_of('=');
      if (delimPos == string::npos) 
        {
          NRS::Base::Exception err(_FL_);
          err << "Bad line" 
                 << " (" << m_filename << ":" << lineNumber << ")"
                 << ", not in name=value format";
          throw err;
        }

      // Insert the trimmed name & value into the map
      m_pairs[trim(line.substr(0, delimPos))] 
        = trim(line.substr(delimPos+1, string::npos));
    }
}
//----------------------------------------------------------------------
bool OptionsFile::contains(const std::string& key) const
{
  //  _DEBUG_("contains(" << key << ")");
  return m_pairs.find(key) != m_pairs.end();
}
//----------------------------------------------------------------------
std::string OptionsFile::value(const std::string& key) const
{
  //  _DEBUG_("value(" << key << ")");
  if (contains(key))
    return m_pairs.find(key)->second;
  else
    return string("");
}
