package nrs.core.base;

import nrs.core.message.Constants;

/**
 * Stores NRS-messages ({@link Message} objects) which have already been
 * sent from this component. Messages are stored and retrieved by using
 * the value of their message ID field. Messages will typically be
 * stored when they might be useful for matching an expected later
 * reply.
 *
 * @author Darren Smith
 */
public class OutboundMessageCache
{
  private Message m_cache[];
  private int m_nextFreeMsgID = 1;

  //----------------------------------------------------------------------
  /** Constructor */
  public OutboundMessageCache()
  {
    m_cache = new Message[10];
  }
  //----------------------------------------------------------------------
  /**
   * Ensure the underlying data array can store <tt>i</tt> elements. If
   * necessary the array will be expanded.
   *
   * _TODO_(This isn't very efficient; the array can be sparse - ie, there
   *        might be empty locations if a high, out of sequence ID comes it)
   *
   */
  private void ensureCapacity(int i)
  {
    if (m_cache.length < i)
    {
      Object oldcache[] = m_cache;
      m_cache = new Message[i * 2];
      System.arraycopy(oldcache, 0, m_cache, 0, oldcache.length);
      
    }
  }
  //----------------------------------------------------------------------
  /**
   * Obtain a unique message ID. The intial value returned is 1.
   */
  public int getFreeID()
  {
    ensureCapacity(m_nextFreeMsgID+1);
    
    if (m_cache[m_nextFreeMsgID] != null)
    {
      PackageLogger.log.warning("Suggested available message ID ("
                                + m_nextFreeMsgID + ") is"
                                + " in use; has unpredictable consequences.");
    }

    return m_nextFreeMsgID++;
  }
  //----------------------------------------------------------------------
  /**
   * Add the {@link Message} to the cache.
   */
  public void add(Message m)
  {
    String value = m.getField(Constants.MessageFields.msgID);

    if (value == null) 
    {
      PackageLogger.log.warning("Message " + m
                                + " has no message ID."
                                + "Unable to cache.");
      return;
    }
      
    try
    {
     int index = Integer.parseInt(value);
     if (index >= m_nextFreeMsgID)
     {
       m_nextFreeMsgID = index+1;
     }
     
     ensureCapacity(index+1);
     m_cache[index] = m;
    
     PackageLogger.log.finest("Caching message " + m + " @ " + index);
    }
    catch (NumberFormatException e)
    {
      PackageLogger.log.warning("Message " + m
                                + " has a non-numeric ID (" + value + ")."
                                + " Unable to cache.");
    }
  }
  //----------------------------------------------------------------------
  /**
   * Get the {@link Message} existing in the cache with ID
   * <tt>messageID</tt>, or <tt>null</tt> if there is no matching {@link
   * Message}. The returned message is not removed from the cache. To
   * remove a message use {@link #remove(int).}
   */
  public Message get(int messageID)
  {
    if ((messageID >= 0) && (messageID < m_cache.length))
      return m_cache[messageID];
    else
      return null;
  }
  //----------------------------------------------------------------------
  /**
   * Returns true if a {@link Message} exists in the cache with a message
   * ID equal to <tt>messageID</tt>.
   */
  public boolean contains(int messageID)
  {
    if ((messageID >= 0) && (messageID < m_cache.length))
      return m_cache[messageID] != null;
    else
      return false;
  }
  //----------------------------------------------------------------------
  /**
   * Remove the {@link Message} with index <tt>messageID</tt> from the cache.
   */
  public void remove(int messageID)
  {
    if ((messageID >= 0) && (messageID < m_cache.length))
      m_cache[messageID] = null;
  }
  //----------------------------------------------------------------------
  /**
   * Return the number of messages held in the cache.
   */
  public int count()
  {
    int count = 0;

    for (int i = 0; i < m_cache.length; i++) 
      count += (m_cache[i] == null)? 0 : 1;

    return count;
  }
}
