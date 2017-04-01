package nrs.csl;

import java.util.logging.Logger;

/**
 * Logger object to use for classes within this package. Classes within
 * this package can use the public static Logger defined below rather
 * than requesting their own Logger. Example usage is:
 * <br><br>
 * <code>PackageLogger.log.warning("This is a warning");</code>
 *
 * @author Darren Smith
 */
public abstract class PackageLogger
{
  /** 
   * Reference to the Logger available for classes within this package
   */
  public static final Logger log = Logger.getLogger("nrs.csl"); 
}
