package nrs.toolboxes;

import javax.swing.JTable;

/**
 * Used in conjunction with {@link AttributeEditor} for providing a
 * callback mechanism to verify the contents of an {@link
 * AttributeEditor} after the user has clicked OK.
 */
public interface Verifier
{
  /**
   * Verify the data contained in the {@link JTable}. Returns
   * <tt>true</tt> if the data is acceptible, <tt>false</tt> otherwise.
   */
  boolean isOkay(JTable table);
}
