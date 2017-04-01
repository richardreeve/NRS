package nrs.util.job;

import java.util.Observable;

/**
 * Base class for all jobs which can be run by the {@link JobManager}
 */
abstract public class Job extends Observable
{
  public static final String STARTING = "Starting";
  public static final String COMPLETE = "Complete";
  public static final String ABORT    = "Aborted";

  private String m_phase = STARTING;

  private String m_result = null;

  /**
   * Returns the phase of this job. Special return values are
   * <tt>STARTING</tt>, <tt>COMPLETE</tt> and <tt>ABORT</tt>.
   */
  public String getPhase()
  {
    return m_phase;
  }

  /**
   * Sets the phase of this job. If the phase has changed, an event is
   * raised to notify observers. Special values are <tt>STARTING</tt>,
   * <tt>COMPLETE</tt> and <tt>ABORT</tt>.
   */
  public void setPhase(String phase)
  {
    if (m_phase != phase)
    {
      m_phase = phase;

      setChanged();
      notifyObservers();
    }
  }

  /**
   * Returns the result of this job (eg the error message if this job
   * aborted or encountered an error). If there is no result, null is
   * returned.
   */
  public String getResult()
  {
    return m_result;
  }

  /**
   * Sets the result of this job
   */
  public void setResult(String result)
  {
    m_result = result;
  }

  /**
   * This called by the <tt>JobManager</tt> when it is this job's turn
   * to run
   */
  public abstract void run();
}

