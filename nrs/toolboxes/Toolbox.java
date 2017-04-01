// $Id: Toolbox.java,v 1.1 2004/11/15 09:47:01 darrens Exp $
package nrs.toolboxes;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JDialog;

/** 
 * Base class for all toolboxes. This is here to basically automate
 * various kinds of communication that take place between a toolbox
 * object and the parent class. If a ToolboxParent object has been
 * provided, then it will be automatically notified of particular state
 * changes to a toolbox.
 */
abstract public class Toolbox extends JDialog
{
  // Class logger
  Logger m_log = Logger.getLogger("nrs.toolboxes");
  
  ToolboxParent m_toolboxParent;

  String PREF_KEY_WIN_X = "DIALOG_WIN_X";
  String PREF_KEY_WIN_Y = "DIALOG_WIN_Y";
  String PREF_KEY_WIN_WIDTH = "DIALOG_WIN_WIDTH";
  String PREF_KEY_WIN_HEIGHT = "DIALOG_WIN_HEIGHT";
  String PREF_KEY_WIN_VISIBLE = "DIALOG_WIN_VISIBLE";

  //----------------------------------------------------------------------
  /** 
   * Constructor. Creates a toolbox where the owner is a {@link Dialog}.
   */
  public Toolbox(Dialog owner,
                 String title,
                 ToolboxParent parent,
                 String prefPrefix)
  {
    super(owner, title);

    m_toolboxParent = parent;
    if (parent != null) parent.toolboxAdded(this);

    // Append the preference prefix to each key.
    if (prefPrefix != null)
      {
        PREF_KEY_WIN_X = prefPrefix + PREF_KEY_WIN_X;
        PREF_KEY_WIN_Y = prefPrefix + PREF_KEY_WIN_Y;
        PREF_KEY_WIN_WIDTH = prefPrefix + PREF_KEY_WIN_WIDTH;
        PREF_KEY_WIN_HEIGHT = prefPrefix + PREF_KEY_WIN_HEIGHT;
        PREF_KEY_WIN_VISIBLE = prefPrefix + PREF_KEY_WIN_VISIBLE;
      }
    else
      {
        m_log.warning("A null string has been provided for" +
                      " the preference prefix. This is likely a" +
                      " programming error, which might result in erratic" +
                      " program behaviour.");
      }

    // Set initial visibility to false. Doing this is useful because it
    // preempts a call to the parent callback. Also, if the intial value
    // is to be visible, then that can simply be done after this class
    // has been constructed.
    setVisible(false);
  }
  //----------------------------------------------------------------------
  /** 
   * Constructor. A null reference can be provided for 'parent' if need
   * be, in which case no toolbox-parent can be called when the toolbox
   * changes its state (such as changing its visibility). The 'title'
   * parameter determines the window title. The 'prefPrefix' should be a
   * unqiue string for use with storing the default preferences
   * information. Ie, by default this base class can save and restore
   * a range of preference information. Each bit of information requires
   * a unique key. In order for these keys to be different for each
   * derived class, derived classes must provide a preference prefix,
   * which is prefixed to each of the preference keys this base class
   * uses. Thus the set of derived classes must ensure all 'prefPrefix'
   * values are different.
   *
   * @param owner the {@link java.awt.Frame} which owns this window. Can
   * be set to null.
   *
   * @param title the string title to use for this window.
   *
   * @param parent the {@link ToolboxParent} object to notify when the
   * visibility of this dialog changes. Can be set to null.
   *
   * @param prefPrefix the prefix string to prepend to each entry made
   * by self into a {@link Preferences} object. Can be set to null.
   */
  public Toolbox(Frame owner,
                 String title,
                 ToolboxParent parent,
                 String prefPrefix)
  {
    super(owner, title);

    m_toolboxParent = parent;  
    if (parent != null) parent.toolboxAdded(this);

    // Append the preference prefix to each key.
    if (prefPrefix != null)
      {
        PREF_KEY_WIN_X = prefPrefix + PREF_KEY_WIN_X;
        PREF_KEY_WIN_Y = prefPrefix + PREF_KEY_WIN_Y;
        PREF_KEY_WIN_WIDTH = prefPrefix + PREF_KEY_WIN_WIDTH;
        PREF_KEY_WIN_HEIGHT = prefPrefix + PREF_KEY_WIN_HEIGHT;
        PREF_KEY_WIN_VISIBLE = prefPrefix + PREF_KEY_WIN_VISIBLE;
      }
    else
      {
        m_log.warning("A null string has been provided for" +
                      " the preference prefix. This is likely a" +
                      " programming error, which might result in erratic" +
                      " program behaviour.");
      }

    // Set initial visibility to false. Doing this is useful because it
    // preempts a call to the parent callback. Also, if the intial value
    // is to be visible, then that can simply be done after this class
    // has been constructed.
    setVisible(false);
  }
  //----------------------------------------------------------------------
  /** Inform self who the toolbox parent is. Null is an acceptible
   * value. */
  public void setToolboxParent(ToolboxParent parent)
  {
    m_toolboxParent = parent;
    if (parent != null) parent.toolboxAdded(this);
  }
  //----------------------------------------------------------------------
  /** Overide the standard method for altering visibility of a
   * window. The extra functionality introduced here is to notiy the
   * parent of the toolbox that the visibility state has altered. */
  public void setVisible(boolean b)
  {
    super.setVisible(b);
    
    if (m_toolboxParent != null) 
      m_toolboxParent.toolboxVisibilityChanged(this);
  }
  //----------------------------------------------------------------------
//   /** Overide the standard method for altering visibility of a
//    * window. The extra functionality introduced here is to notiy the
//    * parent of the toolbox that the visibility state has altered. 
//    *
//    * @deprecated  As of JDK 1.1
//    *
//    */
//   public void show()
//   {
//     super.show();
    
//     if (m_toolboxParent != null) 
//       m_toolboxParent.toolboxVisibilityChanged(this);
//   }
  //----------------------------------------------------------------------
//   /** Overide the standard method for altering visibility of a
//    * window. The extra functionality introduced here is to notiy the
//    * parent of the toolbox that the visibility state has altered.  
//    *
//    * @deprecated  As of JDK 1.1
//    *
//    */
//   public void hide() 
//   { 
//     super.hide();
    
//     if (m_toolboxParent != null) 
//       m_toolboxParent.toolboxVisibilityChanged(this);
//   }
  //----------------------------------------------------------------------
  /**
   * Restores the window location from the supplied {@link Preferences}
   */
  protected void restoreLocation(Preferences props)
  {
    Point p = new Point();
    p.x = props.getInt(PREF_KEY_WIN_X, 0);
    p.y = props.getInt(PREF_KEY_WIN_Y, 0);
    setLocation(p);
  }
  //----------------------------------------------------------------------
  /**
   * Restores the window dimesion from the supplied {@link Preferences}
   */
  protected void restoreDimension(Preferences props)
  {
    Dimension d = new Dimension();
    d.width = props.getInt(PREF_KEY_WIN_WIDTH, 100);
    d.height = props.getInt(PREF_KEY_WIN_HEIGHT, 300);
    setSize(d);
  }
  //----------------------------------------------------------------------
  /**
   * Restores the window visibility from the supplied {@link Preferences}. 
   *
   * <b>Note</b> that the result of this call could be to display the
   * window (if the restored value is set to <code>true</code>/
   */
  protected void restoreVisibility(Preferences props)
  {

    // Retrieve the visibility
    setVisible(props.getBoolean(PREF_KEY_WIN_VISIBLE, false));
  }
  //----------------------------------------------------------------------
  /**
   * Provide a {@link java.util.prefs.Preferences} object which allows
   * self to save various settings for future sessions.
   */
  public void saveSettings(Preferences props)
  {
    // Store the location and size of the unit browser toolbox
    Point p = getLocation();
    props.putInt(PREF_KEY_WIN_X, p.x);
    props.putInt(PREF_KEY_WIN_Y, p.y);

    Dimension d = getSize();
    props.putInt(PREF_KEY_WIN_WIDTH, d.width);
    props.putInt(PREF_KEY_WIN_HEIGHT, d.height);

    // Store the visibility
    props.putBoolean(PREF_KEY_WIN_VISIBLE, isVisible());
  }
  //----------------------------------------------------------------------
  /** 
   * Provide a {@link java.util.prefs.Preferences} object which allows
   * self to restore various settings saved from previous sessions.
   *
   * This routine works by first calling {@link #restoreLocation}
   * followed by a call to {@link #restoreVisibility}.
   */
  public void restoreSettings(Preferences props)
  {
    restoreLocation(props);
    restoreDimension(props);
    restoreVisibility(props);
  }
}
