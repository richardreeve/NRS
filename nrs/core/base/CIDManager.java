package nrs.core.base;

import java.util.HashSet;

/**
 * Manage the allocation of CID values, so that calls to {@link
 * #obtain()} never return a CID which has already been offered.
 */
public class CIDManager
{
  /**
   * CIDs that have been handled out
   */
  private HashSet<String> m_cids = new HashSet();

  //----------------------------------------------------------------------
  /**
   * Reserved the specified CID value so that it can not be offered to
   * another component.
   */
  public void reserve(String cid)
  {
    m_cids.add(cid);
  }
  //----------------------------------------------------------------------
  /**
   * Obtain a CID that can be used for an NRS component. This value will
   * be automatically reserved.
   */
  public String obtain()
  {
    int i = 0;
    String s;
    do
    {
      s = Integer.toString(++i);
    }
    while (m_cids.contains(s));

    reserve(s);
    return s;
  }
  //----------------------------------------------------------------------
  /**
   * Return true is the cid is available, or false if it is already in use
   */
  public boolean isAvailable(String cid)
  {
    return !m_cids.contains(cid);
  }
}

