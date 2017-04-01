// $Id: ToolboxParent.java,v 1.1 2004/11/15 09:47:01 darrens Exp $
package nrs.toolboxes;

/** Utility class for better use of the toolboxes also defined within
 * this package. If the parent JFrame of toolbox classes implements this
 * interface, then those toolboxes can automatically communicate with
 * the parent - via the call backs specified below. */
public interface ToolboxParent
{
  /** Notify the owner of a toolbox window that the toolbox has had its
   * visibility altered. Typically the toolbox-parent should now update
   * any state which depends on the visibilty status of its toolboxes,
   * such as checkbox items in menus. */
  public abstract void toolboxVisibilityChanged(Toolbox toolbox);

  /**
   * Notify to the {@link ToolboxParent} that a new {@link Toolbox} has
   * been added.
   */
  public abstract void toolboxAdded(Toolbox toolbox);
}
