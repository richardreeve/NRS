package nrs.core.comms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class is a concrete implementation of the {@link CommsRoute}
 * interface using named pipes (FIFOs) to transmit and receive sequences
 * of bytes for the underlying communication mechanism to exchange
 * messages a local NRS component.
 *
 * @author Darren Smith
 */
public class FIFOCommsRoute extends CommsRoute 
  implements FIFODataListener, FIFOConnectedListener
{
  private InputFIFO m_in;
  private OutputFIFO m_out;

  //----------------------------------------------------------------------
  /**
   * Create {@link FIFOCommsRoute} using the specified filenames
   *   
   * @param ID the locally unique ID associated with this route / port 
   * and used for wormhole routing.
   * @param name a text label to associate with this route / port
   * @param inboundFilename name of FIFO which provides data to component
   * @param outboundFilename name of FIFO which component writes to
   *
   * @throws IOException if either FIFO failed to be opened
   */
  public FIFOCommsRoute(int ID, 
                        String name,
                        String inboundFilename,
                        String outboundFilename) 
    throws NullPointerException, IOException
  {
    super(ID,name);

    IOException toThrow = null;

    if (inboundFilename == null) 
      throw new NullPointerException("inboundFilename can't be null");
    if (outboundFilename == null) 
      throw new NullPointerException("outboundFilename can't be null");

    // Create FIFOs
    m_in = new InputFIFO(inboundFilename, this, this);
    m_out = new OutputFIFO(outboundFilename, this); 

    // Now ensure FIFO's are in such a state that they can soon be opened
    if (m_out.getState() == OutputFIFO.FIFO_NOT_FOUND)
    {
      String errStr = "Failed to open outbound FIFO"
        + " communications using: " + outboundFilename;
      
      PackageLogger.log.warning(errStr);
      toThrow = new IOException(errStr);    
    }

    if (m_in.getState() == InputFIFO.FIFO_NOT_FOUND)
    {
      String errStr = "Failed to open inbound FIFO"
        + " communications using: " + inboundFilename;
      
      PackageLogger.log.warning(errStr);
      toThrow = new IOException(errStr);    
    }

    if (toThrow != null) throw toThrow;    
  }  
  //----------------------------------------------------------------------
  /**
   * {@inheritDoc}
   */
  public void open()
  {
    m_out.openFifo();
    m_in.openFifo();
  }
  //----------------------------------------------------------------------
  /**
   * {@inheritDoc}
   */
  public void dataAvailable(InputFIFO fifo,
                            byte[] buffer,
                            int size)
  {
    PackageLogger.log.finest("Message received on port \""
                             + this + "\" ( length " + size + " bytes)");

    if (m_msgListener != null)
    {
      m_msgListener.messageEvent(buffer, size, this);
    }
    else
    {
      PackageLogger.log.warning("Message received on port \""
                                + this + "\" discarded because no"
                                +" callback has been registered");
    }

    // callback has expired, so restart the FIFO
    fifo.startRead();
  }
  //----------------------------------------------------------------------
  /**
   * {@inheritDoc}
   */
  public void fifoConnected(Object fifo)
  {
    if (fifo == m_in)
    {
      m_in.startRead();
    }

    if (isUp())
    {
      generateConnectionEvent(true);
    }
  }
  //----------------------------------------------------------------------
  /**
   * {@inheritDoc}
   */
  public void fifoClosed(Object fifo)
  {
    if (fifo == m_in)
    {
      //generateConnectionEvent(false); // remove from PortManager?

      PackageLogger.log.warning("Will try to reopen the closed FIFO\""
                                + fifo + "\"");
      m_in.openFifo();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Return <tt>true</tt> if this communications port is UP. This means
   * the port can be used for both sending and receiving data.
   */ 
  public boolean isUp()
  {
    return (((m_in.getState() == InputFIFO.OPENED_OK) ||
             (m_in.getState() == InputFIFO.WAITING_FOR_DATA) ||
             (m_in.getState() == InputFIFO.DATA_AVAILABLE))
            &&
            (m_out.getState() == OutputFIFO.OPENED_OK));
  }
  //----------------------------------------------------------------------
  /**
   * Write the message held within the {@link ByteArrayOutputStream} to
   * the port.
   *
   * @param message a {@link ByteArrayOutputStream} containing the data
   * message
   *
   * @throws IllegalStateException} if the port is not opened for writing
   *
   * @throws IOException if there is any other kind of IO problem
   */
  public void write(ByteArrayOutputStream message)
    throws IllegalStateException, IOException
  {
    m_out.write(message);
  }
}
