package nrs.core.comms;

/**
 * Derived classes should implement this class so that they can be
 * registered with an input FIFO and receive data available events
 */
public interface FIFODataListener
{
  /** Indicates the <code>fifo</code> has received a complete data message.
   *
   * @param fifo the FIFO which received the data
   *
   * @param buffer contains the bytes of the received message, including
   * the null terminating character
   *
   * @param size the size of the data message, excluding the null
   * character. The data message starts at offset <tt>0</tt> and runs
   * for <tt>size</tt> number of bytes.
   **/
  public void dataAvailable(InputFIFO fifo,
                            byte[] buffer,
                            int size);
};
