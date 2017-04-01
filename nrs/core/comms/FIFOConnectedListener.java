package nrs.core.comms;

/**
 * Derived classes should implement this class so that they can be
 * registered when a FIFO has become opened or closed. This is needed
 * because the code which opens a FIFO can be blocked, and thus runs in
 * a separate thread.
 *
 * @author Darren Smith
 */
public interface FIFOConnectedListener
{
  /** Indicates the <code>fifo</code> has opened */
  public void fifoConnected(Object fifo);

  /** Indicates the <code>fifo</code> has been closed by the other process */
  public void fifoClosed(Object fifo);
};
