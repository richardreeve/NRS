package nrs.core.comms;

import java.io.ByteArrayInputStream;

/**
 * The provides an extra method to the {@link ByteArrayInputStream} Java
 * API class. The underlying byte array on which the stream is based can
 * now be changed. Previous to this, separate instances of {@link
 * ByteArrayInputStream} would need to be created in order to process
 * various byte arrays (because the byte array could only be specified
 * through the constructor), and of course, having to create lots of
 * objects is going to slow down message processing somewhat. So now a
 * single instance of {@link NRSByteStream} should suffice, since its
 * internal byte array can be reset.
 *
 * @see  #setBuffer(byte[])
 * @see  #setBuffer(byte[], int, int)
 *
 * @author Darren Smith
 */
public class NRSByteStream extends ByteArrayInputStream
{
  /**
   * Defers to base class constructor,  {@link
   * ByteArrayInputStream#ByteArrayInputStream(byte[])}.
   */
  public NRSByteStream(byte buf[])
  {
    super(buf);
  }
  
  /**
   * Defers to base class constructor, {@link
   * ByteArrayInputStream#ByteArrayInputStream(byte[], int, int)}.
   */
  public NRSByteStream(byte buf[], int offset, int length)
  {
    super(buf, offset, length);
  }

  /**
   * Set the underlying byte array to <tt>buf</tt>
   */
  public void setBuffer(byte buffer[])
  {
    buf = buf;
    pos = 0;
    count = buf.length;
  }

  /**
   * Set the underlying byte array to <tt>buf</tt>, starting at position
   * <tt>offset</tt> at lasting for <tt>length</tt> bytes.
   */
  public void setBuffer(byte buffer[], int offset, int length)
  {
    buf = buf;
    pos = offset;
    count = Math.min(offset + length, buf.length);
    mark = offset;
  }
}
