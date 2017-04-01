package nrs.util.job;

import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.AbstractListModel;
import java.util.Observable;
import java.util.Observer;

/**
 * Manages a set of {@link Job} objects.
 *
 * <p>This is very light-wieght form of multi-tasking. Each {@link Job}
 * object represents a sequence of operations that need to be performed
 * (ie a job). The <tt>JobManager</tt> maintains this list. When
 * instructed it selects the next job object can executes it (ie calls
 * the job's <tt>run()</tt> method).
 *
 * <p>Typically the <tt>JobManager</tt> should be run continually in a
 * separate thread.
 *
 * @author Darren Smith
 * @author Thomas French
 */
public class JobManager implements Observer
{
  private ArrayList<Job> m_jobs = new ArrayList<Job>();

  /**
   * TODO - at some point the jobs in this list need to be
   * deleted... but is there an issue about them having observers
   */
  private ArrayList<Job> m_jobsDone = new ArrayList<Job>();

  private Job m_nextJob = null;

  private boolean m_stopNow = false;

  private String m_name;

  private DefaultListModel m_listModel = new DefaultListModel();

  private int m_index = 0;

  private Boolean m_running = Boolean.FALSE;

  //----------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param name a text name for this <tt>JobManager</tt>
   */
  public JobManager(String name)
  {
    m_name = name;
  }
  //----------------------------------------------------------------------
  /**
   * Returns wether this manager is running within a thread created by
   * {@link #startJobManagerThread(JobManager jm)}
   */
  public boolean isThreaded()
  {
    return m_stopNow;
  }
  //----------------------------------------------------------------------
  /**
   * Stop any <tt>JobManager</tt> threads. This refers to threads
   * created by {@link #startJobManagerThread(JobManager)}.
   */
  public void stopThreaded()
  {
    m_stopNow = true;
  }
  //----------------------------------------------------------------------
  /**
   * Create a separate thread to run the specified <tt>JobManager</tt>.
   *
   *<p>Once started the thread runs until {@link #stopThreaded()} is
   *called for the <tt>JobManager</tt>
   *
   * @return the {@link Thread} object created. This <tt>Thread</tt> can
   * be started by calling its <tt>start()</tt> method.
   */
  static public Thread startJobManagerThread(final JobManager jm)
  {
    jm.m_stopNow = false;
    Thread jmThread = new Thread()
      {
        public void run()
        {
          while (!jm.m_stopNow) // could use interrupts instead?
          {
            jm.run();
            yield();
          }
        }
      };

    return jmThread;
  }

  /** Attempt to run jobs in queue.
   * Will wait if queue is empty. 
  */
  public synchronized void run(){
    while( m_jobs.size() == 0 )
    {
      try{
        wait(); // wait to be notified of new jobs
      }
      catch(InterruptedException ie){return;}
    }
    execute();
  }

  //----------------------------------------------------------------------
  /**
   * Add the specified <tt>Job</tt> to the manager
   */
  public synchronized void add(Job job)
  {
    PackageLogger.log.fine("Job [" + job + "] added to " + this);
    m_jobs.add(job);
    m_listModel.add(0, job);
    job.addObserver(this);

    notifyAll(); // notify threads that are waiting
  }
  //----------------------------------------------------------------------
  /**
   * Removes the specified <tt>Job</tt> from the manager
   */
  public synchronized void remove(Job job)
  {
    if (job != null)
    {
      job.deleteObserver(this);
      m_jobs.remove(job);
      m_listModel.removeElement(job);
      PackageLogger.log.fine("Job [" + job + "] remove from " + this);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Instructs the manager to select one of the managed jobs, and call
   * its <tt>run()</tt> method.
   */
  public void execute()
  {
    // TODO - job reaping. After jobs have been completed, or aborted,
    // they should really be removed. How to do this? Problem is that
    // they might be observed, or viewed, by someone else. So have to
    // have some kind of 'forget' me signal, before deletion. Could also
    // maybe move them to a finished list, and periodically delete that
    // when it gets above a particular number.
    if (m_nextJob == null) setNextJob();

    if (m_nextJob != null)
    {
      if (m_nextJob.getPhase() != Job.ABORT &&
          m_nextJob.getPhase() != Job.COMPLETE)
      {
        m_nextJob.run();
      }
      else
        {
          // move the finished job to the complete list          
          m_jobsDone.add(m_nextJob);
          m_jobs.remove(m_nextJob); 
        }
    }

    setNextJob();
  }
  //----------------------------------------------------------------------
  private void setNextJob()
  {
    if (m_nextJob != null)
    {
      m_index = m_jobs.indexOf(m_nextJob);

      if (m_index == -1)
      {
        m_nextJob = null;
      }
      else
      {
        m_index++;
        if (m_index < m_jobs.size())
        {
          m_nextJob = m_jobs.get(m_index);
        }
        else
        {
          m_nextJob = null;
        }
      }
    }

    if (m_nextJob == null)
     {
       if (m_jobs.size() > 0){
         m_nextJob = m_jobs.get(0);
       }
     }
  }
  //----------------------------------------------------------------------
  /**
   * Return a list model representation of the jobs within this manager
   */
  public AbstractListModel getListModel()
  {
    return m_listModel;
  }
  //----------------------------------------------------------------------
  /**
   * Inherited from Observer. Called whenever a watched job (one that
   * appears in the lower window) has changed its state.   */
  public void update(Observable o, Object arg)
  {
    int index = m_listModel.indexOf(o);

    if (index != -1)
    {
      // Set the object to the same position - this is enough to
      // generate a list change event, which, will cause any JList
      // observing the list model to be visually updated
      m_listModel.set(index, o);
    }
    else
    {
      PackageLogger.log.warning("Received observer notification from "
                                + o + " but not in listModel???");
      o.deleteObserver(this);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Returns the name
   */
  public String toString()
  {
    return m_name;
  }
}
