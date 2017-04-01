package nrs.util;

import java.util.List;

/**
 * Allows a class to accept and describe a set of command line options
 * that it can process.
 *
 * @author Darren Smith
 */
public interface ArgumentFielder
{
  /**
   * Display help information to the console listing the options
   * available, specifying additional parameters they take, and a short
   * description of what the option achieves.
   */
  public void displayHelp();

  /**
   * Get the list of options provided by this argument fielder. The
   * returned {@link List} should contain only {@link String} objects,
   * each of which contains a command-line option that is
   * supported. These strings should no contain the hash-prefixes. I.e.,
   * the strings should look like "fifo" and "F", and NOT "--fifo",
   * "-F".
   */
  public List getOptions();

  /**
   * Process a single command-line option.
   *
   * @param option the command-line option to process. This will not
   * contain any "-" or "--" prefix.
   *
   * @param args the complete set of command-line options. If optional
   * arguments are to be processed then the relevant array item in
   * <tt>args</tt> should be set to <tt>null</tt>.
   *
   * @param index the position in the <tt>args</tt> array at which the
   * specified <tt>option</tt> is found.
   */
  public void processOption(String option,
                            String [] args,
                            int index) throws ArgumentException;
}
