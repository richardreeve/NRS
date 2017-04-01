// $Id: CaseInsensitiveMessageComparator.java,v 1.3 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

import java.util.Comparator;
import java.util.logging.*;

/** Utility class for case-insensitive comparisons of {@link
 * CSL_Element_Message} objects. Comparisons are based on the {@link
 * String} returned by the {@link CSL_Element_Message#getMessageName()}
 * function. */
public class CaseInsensitiveMessageComparator implements Comparator
{
  static private CaseInsensitiveMessageComparator m_inst 
    = new CaseInsensitiveMessageComparator();

  // Class logger
  private Logger m_log = Logger.getLogger("nrs.csl");

  //----------------------------------------------------------------------
  /** Make the constructor private */
  private CaseInsensitiveMessageComparator()
  {
  }
  //----------------------------------------------------------------------
  /** Return an instance of this class. */
  public static CaseInsensitiveMessageComparator getInstance()
  {
    return m_inst;
  }
  //----------------------------------------------------------------------
  /** Inherited from {@link java.util.Comparator}. Compares its two
   * arguments for order by assuming each is of type {@link
   * CSL_Element_Message} and uses a case insensitive test on the {@link
   * String} returned by the {@link
   * CSL_Element_Message#getMessageName()} function.
   *
   * @throws ClassCastException if the arguments are not of type {@link
   * CSL_Element_Message}
   *
   * @return a negative integer, zero, or a positive integer as the
   * first argument is less than, equal to, or greater than the second.
  */
  public int compare(Object o1, Object o2)
  {
    if ((o1 instanceof CSL_Element_Message) == false)
      {
        String s = "Object known as o1 '" + o1 
          + "' can not be cast to CSL_Element_Message";
        m_log.warning(s);
        throw new ClassCastException(s);
      }

    if ((o2 instanceof CSL_Element_Message) == false)
      {
        String s = "Object known as o2 '" + o2 
          + "' can not be cast to CSL_Element_Message";
        m_log.warning(s);
        throw new ClassCastException(s);
      }
    
    CSL_Element_Message so1 = (CSL_Element_Message) o1;
    CSL_Element_Message so2 = (CSL_Element_Message) o2;
    return so1.getMessageName().compareToIgnoreCase(so2.getMessageName());
  }
  //----------------------------------------------------------------------
  /** Inherited from {@link java.util.Comparator}. Compares its two
   * arguments for order by assuming each is of type {@link
   * CSL_Element_Message} and uses a case insensitive test on the
   * message name. */  
  public boolean equals(Object obj) 
  {
    return (compare(this, obj) == 0);
  }
  
}
