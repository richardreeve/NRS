#ifndef _TIMER_HH_
#define _TIMER_HH_

#include <sys/times.h>
#include <sys/param.h>

class Profile
{
public:
  /**
   * Constructor
   */
  Profile(const char* title, int loops = 1)
    : m_title(title),
      m_loops(loops)
  {
  }
  
  /**
   * Begin timer
   */
  void start();

  /**
   * End timer
   */
  void end();

private:
  tms m_OldTime, m_NewTime;
  clock_t m_oldClock, m_newClock;
  const char * m_title;
  int m_loops;
};


#endif//
