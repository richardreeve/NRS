// $Id: CSL_Exception.java,v 1.3 2005/05/06 15:41:19 hlrossano Exp $
package nrs.csl;

/** This class extends <code>Exception</code> and is thrown to indicate
 * errors when processing CSL data. */
public class CSL_Exception extends Exception
{
  /** Constructs a new exception with the specified detail message. */
  public CSL_Exception(String message)
  {
    super(message);
  }
}
