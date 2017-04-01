package nrs.core.comms;

/**
 * Classes should implement this method so that they can be registered
 * with various communications classes to receive and handle received
 * byte level messages.
 */
public interface MessageListener
{
  /** Handle the byte level representation of a message
   *
   * @param buffer contains the bytes of the received message, including
   * the null terminating character
   *
   * @param size the number of bytes of actual data in the
   * <tt>buffer</tt> (excluding the null terminating character). Only
   * this number of bytes should be processed from the <tt>buffer</tt>,
   * starting from offset <tt>0</tt>. Use <tt>size</tt> rather that
   * <tt>buffer.length</tt> because generally the buffer's actual size
   * will much larger than the actual data content.
   *
   * @param port the {@link CommsRoute} at which the message
   * arrived. Immediately after the message has been decoded, or during
   * such a process, the port needs to be known so that is can be
   * allowed, ie called, to preprocess any intelligent fields that are
   * present.
   **/
  public void messageEvent(byte[] buffer, int size, CommsRoute port);
};
