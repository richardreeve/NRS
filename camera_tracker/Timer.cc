
#include <iomanip>
#include <time.h>

#include "Timer.hh"
#include "Exception.hh"

//----------------------------------------------------------------------
void Profile::start()
{
  times(&m_OldTime);
  m_oldClock = clock();
}
//----------------------------------------------------------------------
/**
 * End timer
 */
void Profile::end()
{
  int elap;
  
  m_newClock = clock();
  times(&m_NewTime);
  elap = (int)(m_NewTime.tms_utime - m_OldTime.tms_utime);
  
  _DEBUG_("Profile: [" << m_title << "]"
          << " loop=" << std::setw(6)  << std::setprecision(4)
          << (float)(1000 * elap) / (m_loops * HZ) << " msec"
          << ", clock=" << m_newClock - m_oldClock);
}
