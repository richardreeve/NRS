package nrs.nrsgui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This is the status bar that appears on windows which are viewing a
 * network.
 *
 * @author Darren Smith
 */
class NetworkBrowserStatusBar extends JPanel implements Observer
{
  private final String ONLINE = "online";
  private final String OFFLINE = "offline";

  private final String NAME = "Name: ";
  private final String VNID = ", VNID: ";
  
  private JLabel m_line;
  private JLabel m_desc;

  private ConfigParameterBoolean m_lineStatus;

  //----------------------------------------------------------------------
  /**
   * Constucts a status bar
   *
   * @param lineStatus the {@link ConfigParameterBoolean} which stores
   * the online / offline status.
   */
  NetworkBrowserStatusBar(ConfigParameterBoolean lineStatus)
  {
    super(new BorderLayout());

    m_lineStatus = lineStatus;

    initGUI();
  }
  //----------------------------------------------------------------------
  /**
   * Set the object which is selected, and for which information should
   * be displyed in the description part of the status bar. Set to
   * <tt>null</tt> to diplay nothing.
   */
  void setSelected(UserVN vn)
  {
    StringBuffer newText = new StringBuffer("");
    if (vn != null)
    {
      newText.append(NAME + vn.name());

      if (vn.remote().isKnown())
      {
        newText.append(VNID + " " + vn.remote().VNID());
      }
    }
    else
    {
      newText.append(NAME + "N/A");
    }
    
    m_desc.setText(newText.toString());
  }
  //----------------------------------------------------------------------
  /**
   * Arranges the various gui components that appear on the status bar
   */
  private void initGUI()
  {
    if ( AppManager.getInstance().isDebugMode() ){
      add(createOnlineOfflinePanel(), BorderLayout.EAST);
    }
    add(createSelectedInfoPanel(), BorderLayout.CENTER);
  }
  //----------------------------------------------------------------------
  /**
   * Creates and returns the panel used for indicating the online /
   * offline status.
   */
  JPanel createOnlineOfflinePanel()
  {
    JPanel p = new JPanel();

    m_line = new JLabel(ONLINE);
    updateLineStatus();
    p.add(m_line);
    m_lineStatus.addObserver(this);

    m_line.addMouseListener(new MouseAdapter()
      {
        public void mouseClicked(MouseEvent e)
        {
          m_lineStatus.toggle();
        }
      });

    p.setBorder(Resources.getInstance().getLoweredBevelBorder());

    return p;
  }
  //----------------------------------------------------------------------
  /**
   * Create and returns the panel used for describing the currently
   * selected object.
   */
  JPanel createSelectedInfoPanel()
  {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));

    m_desc = new JLabel(NAME + "N/A");
    p.add(m_desc);

    return p;
  }
  //----------------------------------------------------------------------
  /**
   * Updates the online / offline display
   */
  private void updateLineStatus()
  {
    if (m_lineStatus.value())
    {
      m_line.setText(ONLINE);
    }
    else
    {
      m_line.setText(OFFLINE);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Implements the {@link Observer} interface.
   */
  public void update(Observable o, Object arg)
  {
    if (o == m_lineStatus)
    {
      updateLineStatus();
    }
  }
}
