/*
 * Copyright (C) 2004 Edinburgh University
 *
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation; either version 2 of
 *    the License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public
 *    License along with this program; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307 USA
 *
 * For further information in the first instance contact:
 * Richard Reeve <richardr@inf.ed.ac.uk>
 *
 */
package nrs.core.comms;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Observable;

/**
 * This encapsulates the operations for reading bytes from a 
 * {@link java.net.Socket} {@link InputStream} as a {@link BufferedInputStream}.
 *
 * @author Thomas French
 * @author Darren Smith
 *
 */
public class InputSocket extends Observable
{
    
  /** The byte value which indicates the completion of a message */
  public static final byte DELIM = 0;
  
  /** Default size of the input buffer, and also the size at which it
   * grows if ever it becomes full. */
  public static final int DEFAULT_SIZE = 1024;
  
  private InputStream m_inputStream;
  private BufferedInputStream m_in;
  private SocketDataListener m_listener;
  private SocketConnectedListener m_conListener;
  
  public enum State {OPENED_OK, CLOSED, DATA_AVAILABLE, WAITING_FOR_DATA};
  private State m_state;    
  
  /** New data from the SOCKET is read into this buffer */
  private byte[] m_buffer = new byte[DEFAULT_SIZE];
  
  /** This buffer is used to stored completed messages */
  private byte[] m_outBuf = new byte [DEFAULT_SIZE];
  
  /** Index at which a message begins, within m_buffer */
  private int m_msgStart;
  
  /** Index at which next free sequence exits in m_buffer, and at which
   * data will stored when received from the SOCKET. */
  private int m_freeStart;
  
  /** Buffer index which where the previous delim search ended */
  private int m_dpos;
  
  /** Index to the oldest data in the buffer which is not part of a yet
   * identified message. This can be set to -1, to indicate that no data
   * has yet been read.*/
  private int m_oldest;
  
  /** Indicies of the locations of the buffer at which the most recent
   * bytes that were received from the SOCKET have been stored at, and
   * that have not yet been scanned for a delimitor */
  private int m_lastReadStart, m_lastReadEnd;
  
  /** Store the size of the most recent complete message recieved */
  private int m_msgSize;
  
  //----------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param input {@link InputStream} for {@link java.net.Socket}. Must be provided.
   *
   * @param lstr callback for whenever data has been received from the
   * {@link java.net.Socket}. Can be null.
   *
   * @param cLstr callback for when the {@link InputStream} is setup. 
   * Can be null.
   *
   * @throws NullPointerException if input is null.
   */
    public InputSocket(InputStream input,
		     SocketDataListener lstr,
		     SocketConnectedListener cLstr)
    throws NullPointerException
  {
      if ( input == null )
	  throw new NullPointerException("InputStream must be provided.");
      
      m_inputStream = input;
      
      m_listener = lstr;
      m_conListener = cLstr;
      
      initBufferVars();
      
      m_state = State.CLOSED;
  }
  /**
   * This creates a separate thread which continually polls the input
   * stream to see if it has data, and, when data is
   * available, reads it in, and keeps doing this until a complete
   * message has been received (indicated by the byte 0). <br><br>
   *
   * <b>NOTE,</b> polling ceases once a complete message has been
   * read. Thus the listener should be sure to call {@link #startRead()}
   * once more if more messages are expected from the SOCKET. A future
   * upgrade to this class should remove the need to keep creating new
   * Thread classes, and instead use the Object.wait() approach.
   */
  public void startRead() throws IllegalStateException
  {
    if ( (m_state != State.OPENED_OK) && (m_state != State.DATA_AVAILABLE) )
      throw new IllegalStateException("Stream not ready for reading");
      
    Thread worker = new Thread (new Runnable()
      {
        public void run()
        {
          m_state = State.WAITING_FOR_DATA;
          
          while ( m_state == 
                  State.WAITING_FOR_DATA )
          {
            readLoop();
          }
          
          // If loop has expired, then maybe a
          // callback is due
          if ( m_state == State.DATA_AVAILABLE ){
            m_listener.
              dataAvailable(InputSocket.this,
                            m_outBuf,
                            m_msgSize);
          } 
        }
      });
    worker.start();
  }
  //----------------------------------------------------------------------
  /**
   * Make a blocking attempt to read from this InputStream of the Socket.
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

      m_state = State.DATA_AVAILABLE;

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
        // error has occured change state
        //m_state = State.CLOSED;

        PackageLogger.log.warning("Error when reading from InputStream " + e);
        e.printStackTrace();
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

    PackageLogger.log.finest("InputStream \""
                             + this + "\" buffer expanded from "
                             + oldSize +" to "
                             + m_buffer.length + " bytes");


    return m_buffer.length - m_freeStart;
  }
  //----------------------------------------------------------------------
  /**
   * Attempt a blocking read from the Socket. The call to the underlying
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
    int dataCount = 0;
    
    // fix bug: Connection Reset loop. T. French
    try{
      dataCount = m_in.read(m_buffer, m_freeStart, maxRead);
    }
    catch(java.net.SocketException se){
      if (m_conListener != null) m_conListener.socketClosed(this);
      return;
    }
    
    if (dataCount > 0)
    {
      m_lastReadStart = m_freeStart;
      m_lastReadEnd = m_freeStart + dataCount;

      PackageLogger.log.finer("Read from InputStream, #bytes = " + dataCount);
      //      dumpBuffer("current", m_buffer);

      // set the initial non-minus-one value of m_oldest
      if (m_oldest == -1) m_oldest = m_freeStart;

      // Advance the free startp pointer
      m_freeStart += dataCount;
      if (m_freeStart >= m_buffer.length) m_freeStart = 0;
    }
    else if ( dataCount == 0 ){ //no data
	m_state = State.WAITING_FOR_DATA;
    }
    else //dataCount == -1 : end of stream
    {
      if (m_conListener != null) m_conListener.socketClosed(this);
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
    
    /** Close Stream. */
    public void close() throws IOException
    {
	m_state = State.CLOSED;
	m_in.close();
	m_in = null;
    }
    
    public State getState(){
	return m_state;
    }
    
    public void setState(State s){
	if ( m_state == s )return;
	
	m_state = s;
    }
    
    /**
     * Attempt to open the InputStream. This will block until there is some other
     * system process reading from the SOCKET. That's why this routine is
     * called by a separate thread, created in openSocket() above. Also note
     * that the reader object is null until the blocking call is complete.
     */
    public void setup()
    {
	PackageLogger.log.fine("Attempting to open InputStream: ");
	
	m_in = new BufferedInputStream(m_inputStream);
	m_state = State.OPENED_OK;
	
	PackageLogger.log.fine("InputStream opened.");

	if (m_conListener != null) m_conListener.socketConnected(this);
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
