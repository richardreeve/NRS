package nrs.core.comms;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Observable;

/**
 * This encapsulates the operations for reading bytes from a Linux named
 * pipe (FIFO).
 *
 * This FIFO has the following state change model:<br>
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
 * 6. Upon calling startRead(), state changes to WAITING_FOR_DATA<br>
 *
 * 7. When data is available, state changes to DATA_AVAILABLE, and
 *    callback is called (and polling expires)<br>
 *
 * 8. If ever an open FIFO is closed by the other process, the state is
 *    set to UNOPENED, and the closed callback is triggered.<br>
 */
public class InputFIFO extends Observable
{
  // FIFO state constants
  public static final String UNOPENED =  "FIFO not opened";
  public static final String FIFO_NOT_FOUND = "FIFO not found";
  public static final String OPENING_BLOCKED = "Opening blocked";
  public static final String FAILED = "FIFO failed to open";
  public static final String OPENED_OK = "Opened OK - ready for reading";
  public static final String WAITING_FOR_DATA = "Listening for data";
  public static final String DATA_AVAILABLE = "Data received";

  /** The byte value which indicates the completion of a message */
  public static final byte DELIM = 0;

  /** Default size of the input buffer, and also the size at which it
   * grows if ever it becomes full. */
  public static final int DEFAULT_SIZE = 1024;

  private FileInputStream m_in;
  private File m_file;
  private String m_state;
  private Thread m_fifoOpener;
  private FIFODataListener m_listener;
  private FIFOConnectedListener m_conListener;

  /** New data from the FIFO is read into this buffer */
  private byte[] m_buffer = new byte[DEFAULT_SIZE];

  /** This buffer is used to stored completed messages */
  private byte[] m_outBuf = new byte [DEFAULT_SIZE];

  /** Index at which a message begins, within m_buffer */
  private int m_msgStart;

  /** Index at which next free sequence exits in m_buffer, and at which
   * data will stored when received from the FIFO. */
  private int m_freeStart;

  /** Buffer index which where the previous delim search ended */
  private int m_dpos;

  /** Index to the oldest data in the buffer which is not part of a yet
   * identified message. This can be set to -1, to indicate that no data
   * has yet been read.*/
  private int m_oldest;

  /** Indicies of the locations of the buffer at which the most recent
   * bytes that were received from the FIFO have been stored at, and
   * that have not yet been scanned for a delimitor */
  private int m_lastReadStart, m_lastReadEnd;

  /** Store the size of the most recent complete message recieved */
  private int m_msgSize;

  //----------------------------------------------------------------------
  /**
   * Constructor.  An {@link IllegalArgumentException} is thrown if
   * filename is null.
   *
   * @param filename the filename of the FIFO. Must be provided.
   *
   * @param lstr callback for whenever data has been received from the
   * FIFO. Can be null.
   *
   * @param cLstr callback for when the FIFO is opened. Can be null.
   */
  public InputFIFO(String filename,
                   FIFODataListener lstr,
                   FIFOConnectedListener cLstr)
    throws IllegalArgumentException
  {
    setState(UNOPENED);

    if (filename == null)
      throw new IllegalArgumentException("filename can't be null");

    m_file = new File(filename);
    m_listener = lstr;
    m_conListener = cLstr;

    if (!m_file.exists())
    {
      PackageLogger.log.warning("Failed to create input FIFO because"
                                + " file not found: "
                                + filename);

      setState(FIFO_NOT_FOUND);
    }

    initBufferVars();
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to open the FIFO, to allow later input. Because opening the
   * FIFO can result in an IO block (as would happen if no process was
   * writing to the FIFO), a separate thread is created and
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
   * Return the current state of this FIFO.
   */
  public String getState()
  {
    return m_state;
  }
  //----------------------------------------------------------------------
  /**
   * This creates a separate thread which continually polls the input
   * stream (the FIFO) to see if it has data, and, when data is
   * available, reads it in, and keeps doing this until a complete
   * message has been received (indicated by the byte 0). <br><br>
   *
   * <b>NOTE,</b> polling ceases once a complete message has been
   * read. Thus the listener should be sure to call {@link #startRead()}
   * once more if more messages are expected from the FIFO. A future
   * upgrade to this class should remove the need to keep creating new
   * Thread classes, and instead use the Object.wait() approach.
   */
  public void startRead() throws IllegalStateException
  {
    if ((getState() != OPENED_OK) && (getState() != DATA_AVAILABLE))
    throw new IllegalStateException("FIFO not ready for reading");
    
    Thread worker = new Thread (new Runnable()
      {
        public void run()
        {
          setState(WAITING_FOR_DATA);
          while (getState() == WAITING_FOR_DATA)
          {
            readLoop();
          }
          
          // If loop has expired, then maybe a
          // callback is due
            if (m_state == DATA_AVAILABLE)
            {
              m_listener.
                dataAvailable(InputFIFO.this,
                              m_outBuf,
                              m_msgSize);
            }
        }
      });
    
    worker.start();
  }
  //----------------------------------------------------------------------
  /**
   * Make a blocking attempt to read from this FIFO.
   */
  private synchronized void readLoop()
  {
    if (scanMsgDelim())
    {
      m_msgSize = 0;
      int dest = 0;

      // Copy the found messsage into the output buffer
      if (m_oldest > m_dpos)
      {
        // here, message is in format: buffer[BBBAAA0] but need to be in
        // output format: out[AAABBB]. Here we copy the A' part into the
        // beginning of the output array
        dest = m_buffer.length - m_oldest;
        arrayCopy(m_oldest, dest, 0);
        m_oldest = 0;
        m_msgSize += dest;
      }
      // the +1 is because we want the delim byte to also be copied
      arrayCopy(m_oldest, m_dpos - m_oldest+1, dest);

      // no -1 here because we don't want the null to contribute to the
      // message size
      m_msgSize += m_dpos - m_oldest;

      // Now update the position of oldest
      m_oldest = m_dpos + 1;

      // if oldest goes outside boundary, reset
      if (m_oldest >= m_buffer.length) m_oldest = 0;

      // if oldest is equal to the m_free, then it means there is no
      // data left in the buffer which has not been read. When there is
      // no data left, reset the buffer points to start at zero again,
      // just for simplicity
      if (m_oldest == m_freeStart) initBufferVars();

      setState(DATA_AVAILABLE);

      // note, we don't issue the callback just yet... that will the
      // last thing this thread does, just before it expires.
    }
    else
    {
      try
      {
        attemptBlockingRead();
      }
      catch (IOException e)
      {
        PackageLogger.log.warning("Error when reading from input FIFO \""
                                  + m_file + "\". " + e);
      }
    }
  }
  //----------------------------------------------------------------------
  /**
   * Scan the current data content in the buffer for the presence of the
   * message deliminator byte. Not all of the data content is
   * scanned. Only the data content that hasn't already been scanned is
   * now scanned during this call.
   *
   * @return true if the message deliminator has been found, false
   * otherwise.
   */
  private boolean scanMsgDelim()
  {
    //    PackageLogger.log.finer("Scanning for message delimitor (from="
    //                           + m_lastReadStart
    //                           + " to "
    //                           + m_lastReadEnd + ")");
    m_dpos = m_lastReadStart;
    while (m_dpos < m_lastReadEnd)
    {
      if (m_buffer[m_dpos] == DELIM)
      {
        m_lastReadStart = m_dpos+1;
        //        PackageLogger.log.finer("Delim found at " + m_dpos);
        return true;
      }

      m_dpos++;
    }

    return false;
  }
  //----------------------------------------------------------------------
  /**
   * Ensure there exists free space in the internal byte buffers,
   * growing them if necessary.
   *
   * @return space available in the current buffer for the next read
   * operation
   */
  private int ensureFreeSpace()
  {
    int maxRead;

    // Determine how many bytes we can read into the buffer
    if (m_freeStart <= m_oldest)
      maxRead = m_oldest - m_freeStart;
    else
      maxRead = m_buffer.length - m_freeStart;

    if (maxRead != 0)
      return maxRead;
    else
      return growBuffers();
  }
  //----------------------------------------------------------------------
  /**
   * Grow the buffers. The current contents are preserved.
   *
   * @return space available in the current buffer for the next read
   * operation
   */
  private int growBuffers()
  {
    // store the old size of the buffer
    int oldSize = m_buffer.length;
    //dumpBuffer("old", m_buffer);

    byte[] buffer = new byte[oldSize + DEFAULT_SIZE];
    m_outBuf = new byte[buffer.length];

    if (m_oldest == 0)
    {
      // simplest case
      System.arraycopy(m_buffer, 0, buffer, 0, oldSize);
    }
    else
    {
      System.arraycopy(m_buffer, m_oldest,
                       buffer, 0,
                       oldSize - m_oldest);

      System.arraycopy(m_buffer, 0,
                       buffer, oldSize - m_oldest,
                       m_oldest);
      m_oldest = 0;
    }
    m_freeStart = oldSize;

    m_buffer = buffer;
    //dumpBuffer("new", m_buffer);

    PackageLogger.log.finest("InputFIFO \""
                             + this + "\" buffer expanded from "
                             + oldSize +" to "
                             + m_buffer.length + " bytes");


    return m_buffer.length - m_freeStart;
  }
  //----------------------------------------------------------------------
  /**
   * Attempt a blocking read from the FIFO. The call to the underlying
   * stream will block the current thread, so ensure a thread separate
   * from the main thread is processing this code! The thread continues
   * when data is available.
   *
   * @throws IOException if the <code>read()</code> operation encounters
   * an IO error
   */
  private synchronized void attemptBlockingRead() throws IOException
  {
    int maxRead = ensureFreeSpace();

    // This call will block until data is ready to be read
    int dataCount = m_in.read(m_buffer, m_freeStart, maxRead);

    if (dataCount > 0)
    {
      m_lastReadStart = m_freeStart;
      m_lastReadEnd = m_freeStart + dataCount;
      PackageLogger.log.finer("Read from input FIFO, #bytes = " + dataCount);
      //      dumpBuffer("current", m_buffer);

      // set the initial non-minus-one value of m_oldest
      if (m_oldest == -1) m_oldest = m_freeStart;

      // Advance the free startp pointer
      m_freeStart += dataCount;
      if (m_freeStart >= m_buffer.length) m_freeStart = 0;
    }
    else
    {
      PackageLogger.log.warning("Input FIFO \"" +
                                m_file +"\" closed (read() == "
                                + dataCount + ")");

      setState(UNOPENED);
      if (m_conListener != null) m_conListener.fifoClosed(this);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Sets the intial state of the various member variables used as
   * indicies into the read buffer.
   */
  private void initBufferVars()
  {
    m_freeStart = 0;
    m_oldest = -1;
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
   * Attempt to open the FIFO. This will block until there is some other
   * system process reading from the FIFO. That's why this routine is
   * called by a separate thread, created in openFifo() above. Also note
   * that the reader object is null until the blocking call is complete.
   */
  private void open()
  {
    PackageLogger.log.fine("Attempting to open input FIFO: " + m_file);

    setState(OPENING_BLOCKED);
    try
    {
      m_in = new FileInputStream(m_file);
      PackageLogger.log.fine("Input FIFO opened: " + m_file);
      setState(OPENED_OK);
      if (m_conListener != null) m_conListener.fifoConnected(this);
    }
    catch (IOException e)
    {
      PackageLogger.log.warning("Failed to open input FIFO. " + e);
      setState(FAILED);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Copy bytes from the read buffer to the output buffer.
   * <code>length<code> bytes are copied from the read buffer (reading
   * starts at position <code>start</code>) to the output buffer
   * (writing starts at position <code>dest</code>).
   */
  private void arrayCopy(int start, int length, int dest)
  {
    //    PackageLogger.log.fine("in arrayCopy("
    //                           + start + ", "
    //                           + length + ", "
    //                           + dest + ")");
    //    PackageLogger.log.fine("m_outBuf.length=" + m_outBuf.length
    //                           + ", m_buffer.length=" + m_buffer.length);

    for (int i = 0; i < length; i++)
    {
      m_outBuf[dest] = m_buffer[start];
      dest++;
      start++;
    }
  }
  //----------------------------------------------------------------------
  private void dumpBuffer(String title, byte buffer[])
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < buffer.length; i++)
    {
      char c = (char) buffer[i];
      sb.append(c);
    }
    PackageLogger.log.fine(title + " : [" + sb.toString() + "]");
  }
}
