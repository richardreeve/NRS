
#include <unistd.h>
#include <sys/times.h>
#include <iomanip>
#include <cmath>

#include "PostTrackProcessor.hh"
#include "Exception.hh"

using namespace std;

//----------------------------------------------------------------------
void DefaultPostTrackProcessor::process(RobotIterator begin,
					RobotIterator end,
					int frameNumber,
					double time)
{

  if (m_rate and (frameNumber % m_rate == 0))
    {
      ostringstream status;
      status << "Track status for all robots at time "
	     << setprecision(3) << fixed << time << ":\n";
      
      // Assemble status report
      for (RobotIterator robot = begin; robot != end; robot++)
        {
	  // Robot name
          status << "\t" << (*robot)->label;
	  
	  // Location
	  if ((*robot)->located())
	    status << " x=" << (*robot)->x << ", y=" << (*robot)->y
		   << ", heading=" << (*robot)->heading * 180 / M_PI << "º\n";
	  else
	    status << " not located\n";
        }
      _INFO_(status.str());
    }
}

//----------------------------------------------------------------------

FilePTP::FilePTP(string dataFile, string namesFile, 
		 RobotIterator begin, RobotIterator end)
  : m_out(dataFile.c_str())
{
  // Check file for labels
  ofstream outNames(namesFile.c_str());
  if (!outNames)
    _ABORT_("Failed to open file \"" << namesFile << "\"");

  // Check file for data
  if (!m_out)
    _ABORT_("Failed to open file \"" << dataFile << "\"");
    
  
  // Write out the labels
  outNames << "Time Frame ";
  
  for (RobotIterator robot = begin; robot != end; robot++)
    {
      // Angle
      outNames << (*robot)->label << "_heading ";
      
      // Position
      outNames << (*robot)->label << "_x "
	       << (*robot)->label << "_y ";
    }
  outNames << "\n";
  
  // Done
  outNames.close();
  _INFO_("Column headers for tracking data wrote to file \"" << namesFile << "\"");
  _INFO_("Opened  \"" << dataFile << "\" for writing tracking data to");
}

//----------------------------------------------------------------------

FilePTP::~FilePTP()
{
  // Close ofstream if open
  if (m_out) m_out.close();
}

//----------------------------------------------------------------------

void FilePTP::process(RobotIterator begin,
                      RobotIterator end,
                      int frameNumber,
		      double time)
{
  m_out << time << " " << frameNumber << " ";  // fixed?

  for (RobotIterator robot = begin; robot != end; robot++)
  {
    // Angle and location
    if ((*robot)->located())
      m_out << (*robot)->heading << " " << (*robot)->x << " " << (*robot)->y << " ";
    else
      m_out << "NaN NaN NaN ";
  }
  m_out << "\n";
}
