package nrs.core.comms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Observable;

/**
 * This encapsulates the operations for writing to a Linux named pipe
 * (FIFO).
 *
 * This FIFO has the following state model:<br>
 * 
 * 1. Initial state is UNOPENED<br>
 * 
 * 2. During construction, the state may change to FIFO_NOT_FOUND, which is
 *    an error condition. No further state-changing calls to the object should
 *    be made.<br>
 * 
 * 3. During call to openFifo(), state does: UNOPENED -> OPENING_BLOCKED<br>
 * 
 * 4. If the FIFO opens, then state does: OPENING_BLOCKED -> OPENED_OK<br>
 *
 * 5. If the FIFO fails to open, then: OPENING_BLOCKED -> FAILED<br>
 *
 * Only if the FIFO is in the state OPENED_OK can it be used for writing out
 * data.<br>
 */
public class OutputFIFO extends Observable
{
  // FIFO state constants
  public static final String UNOPENED = "FIFO not opened";
  public static final String FIFO_NOT_FOUND = "FIFO not found";  
  public static final String FAILED = "FIFO failed to open";
  public static final String OPENING_BLOCKED = "Opening blocked";
  public static final String OPENED_OK = "Opened OK - ready for writing";

  // Exception string
  private static final String FIFO_NOT_OPENED 
    = "FIFO has not been opened for writing";

  private FileOutputStream m_out;
  private File m_file;
  private String m_state;
  private Thread m_fifoOpener;
  private FIFOConnectedListener m_conListener;

  //----------------------------------------------------------------------
  /**
   * Construct an output FIFO using a named pipe.  An {@link
   * IllegalArgumentException} is thrown if filename is null.
   *
   * @param filename the filename of the FIFO. Must be provided.
   *
   * @param cLstr callback for when the FIFO is opened. Can be null.
   *
   * @throws IllegalArgumentException if <code>filename</code> is null.
   */
  public OutputFIFO(String filename,
                    FIFOConnectedListener cLstr)
  {
    setState(UNOPENED);

    if (filename == null) 
      throw new IllegalArgumentException("filename can't be null");

    m_file = new File(filename);
    m_conListener = cLstr;

    if (!m_file.exists())
    {
      PackageLogger.log.warning("Failed to create output FIFO because"
                                + " file not found: " 
                                + filename);

      setState(FIFO_NOT_FOUND);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to open the FIFO, to allow later output. Because opening
   * the FIFO can result in an IO block (as would happen if no process
   * was reading the FIFO), a separate thread is created and
   * started. This new thread can then block without affecting the rest
   * of the application.
   */
  public void openFifo()
  {
    m_fifoOpener = new Thread(
                              new Runnable()
                              {
                                public void run()
                                {
                                  open();
                                }
                              }, "FIFO opener");
    m_fifoOpener.start();
  }
  //----------------------------------------------------------------------
  /**
   * Return the filename associated with this FIFO
   */
  public String toString()
  {
    return m_file.toString();
  }
  //----------------------------------------------------------------------
  /**
   * Return the file associated with this FIFO. May return null.
   */
  public File getFile()
  {
    return m_file;
  }
  //----------------------------------------------------------------------
  /**
   * Write the message held within the {@link ByteArrayOutputStream} to
   * the FIFO.
   *
   * @param message a {@link ByteArrayOutputStream} containing the data
   * message
   *
   * @throws IllegalStateException} if the FIFO has not already been
   * opened.
   *
   * @throws IOException if there is any other kind of IO problem
   */
  public void write(ByteArrayOutputStream message)
    throws IllegalStateException, IOException
  {
    PackageLogger.log.finer("Attempting to write " + message + " of size "
                            + message.size()
                            + " bytes to FIFO: " + m_file);

    if (m_state != OPENED_OK) throw new IllegalStateException(FIFO_NOT_OPENED);
    
    message.writeTo(m_out);
  }
  //----------------------------------------------------------------------
  /**
   * Write a sub-selection of a byte array to the FIFO.
   *
   * @param b byte array containing data to be written
   * @param off start writing from this byte in the array
   * @param len write this number of bytes
   *
   * @throws IllegalStateException} if the FIFO has not already been
   * opened.
   *
   * @throws IOException if there is any other kind of IO problem
   *
   */
  public void write(byte[] b, int off, int len) 
    throws IllegalStateException, IOException
  {
    PackageLogger.log.finer("Attempting to write "
                            + len
                            + " bytes to FIFO: " + m_file);

    if (m_state != OPENED_OK) throw new IllegalStateException(FIFO_NOT_OPENED);

    try
    {
      m_out.write(b, off, len);
    }
    catch (IOException e)
    {
      PackageLogger.log.warning("Failed to write to FIFO \""
                                + m_file + "\", " + e);
      throw e;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Write a byte array to the FIFO.
   *
   * @param b byte array containing data to be written
   *
   * @throws IllegalStateException} if the FIFO has not already been
   * opened.
   *
   * @throws IOException if there is any other kind of IO problem
   *
   */
  public void write(byte[] b) throws IllegalStateException, IOException
  {
    write(b, 0, b.length);
  }
  //----------------------------------------------------------------------
  /**
   * Return the current state of this FIFO.
   */
  public String getState()
  {
    return m_state;
  }
  //----------------------------------------------------------------------
  private void setState(String newState)
  {
    if (m_state == newState) return;

    m_state = newState;
    setChanged();
    notifyObservers();
    clearChanged();
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to open the FIFO. This will block until there is some
   * process reading from the FIFO. That's why this routine is called by
   * a separate thread, created in openFifo() above. Also note that the
   * object 'out' is null until the blocking call is complete.
   */
  private void open()
  {
    PackageLogger.log.fine("Attempting to open output FIFO: " + m_file);

    setState(OPENING_BLOCKED);
    try
    {
      m_out = new FileOutputStream(m_file);
      PackageLogger.log.fine("Output FIFO opened: " + m_file);
      setState(OPENED_OK);
      if (m_conListener != null) m_conListener.fifoConnected(this);
   }
    catch (IOException e)
    {
      PackageLogger.log.warning("Failed to open output FIFO. " + e);
      setState(FAILED);
    }
  }
}
