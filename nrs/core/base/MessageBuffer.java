package nrs.core.base;

/** 
 * Represent preallocated memory storage into which PML or BMF messages
 * can be constructed. The idea is to use and reuse instances of {@link
 * MessageBuffer} so that memory allocation and object construction can
 * be avoided when new PML and BMF messages are constructed, which is
 * costly, in terms of time. While such efficiency considerations are
 * much more important for the more real-time C/C++ components of NRS,
 * it is neverthless useful to get used to adopting such programming
 * practises, and ofcourse, should result in slight faster performance
 * of Java based components.
 *
 * Each {@link MessageBuffer} instance can only store a single
 * message. Thus several instances should be used in a coordinated
 * manner if several messages need to be buffered before sending to
 * other components of NRS.
 *
 * @deprecated Not sure if this class is being used; it will likely be
 * removed soon.
 *
 * @author Darren Smith
 */
class MessageBuffer
{
  /** Underlying storage */
  private byte m_mem[];

  /** Indicate whether this buffer contains data content and is thus
   * locked */
  private boolean m_locked;

  //----------------------------------------------------------------------
  /**
   * Creates a {@link MessageBuffer} instance which initially allocates
   * <code>size</code> bytes of storage.
   */
  public MessageBuffer(int size)
  {
    m_mem = new byte[size];
    m_locked = false;
  }
  //----------------------------------------------------------------------
  /**
   * Return the total storage capacity, which is the number of bytes
   * allocated for this buffer.
   */
  public int capacity()
  {
    return m_mem.length;
  }
  //----------------------------------------------------------------------
  /**
   * Return <code>true</code> if this buffer currently contains data
   * content.
   */
  public boolean isLocked()
  {
    return m_locked;
  }
  //----------------------------------------------------------------------
  /**
   * Release the lock on this buffer, so that it can be used for storing
   * new data content.
   */
  public void release()
  {
    m_locked = false;
  }
}
