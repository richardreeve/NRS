// $Id: ScanWindow.java,v 1.2 2005/04/29 11:21:03 hlrossano Exp $
package nrs.tracker.palette;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
//import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import nrs.toolboxes.Toolbox;
import nrs.toolboxes.ToolboxParent;

public class ScanWindow extends Toolbox implements ClassifierInterface
{
  /** Title of the dialog */
  public static String TITLE = "Pixel Selector";

  // Gui elements
  private JTextField m_growingThres;
  private JTextField m_currSelectedThres;
  private JRadioButton m_rb_grow;
  private JRadioButton m_rb_square;
  private JRadioButton m_rb_circle;
  private JRadioButton m_rb_highprob;
  private JRadioButton m_rb_selcolour;

  // Button labels
  private final String BTN_SCAN = "Scan";
  private final String BTN_FIND = "Find";
  private final String BTN_CLOSE = "Close";
  private final String BTN_ERASE = "Erase";
  private final String RDBTN_GROW = "Growing";
  private final String RDBTN_SQUARE = "Square";
  private final String RDBTN_CIRCLE = "Circle";
  private final String RDBTN_HIGHPROB = "Highest probability";
  private final String RDBTN_SELCOLOUR = "Selected colour";

  // Keys used in saving to Preferences
  String PREF_KEY_GROW_THRESH = "GROW_THRESH";
  String PREF_KEY_COLOUR_THRESH = "COLOUR_THRESH";

  // Class logger
  private static Logger m_log = Logger.getLogger("nrs.tracker.palette");

  // Scan events are delegated to this object
  private ScanInterface m_scanDelegate;

  // Find events are delegated to this object
  private ScanInterface m_findDelegate;

  //----------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param owner the {@link java.awt.Frame} which owns this window. Can
   * be set to null.
   *
   * @param parent the {@link ToolboxParent} object to notify when the
   * visibility of this dialog changes. Can be set to null.
   *
   * @see Toolbox
   * @see ToolboxParent
   *
   */
  public ScanWindow(Frame owner, ToolboxParent parent)
  {
    super(owner, TITLE, parent, "ScanWindow_");
    guiInit();
  }
  //----------------------------------------------------------------------
  /**
   * Build the GUI by placing various components on the root
   * panel. Should only be called once.
   */
  private void guiInit()
  {
    ButtonGroup regionButtons = new ButtonGroup();
    ButtonGroup findButtons = new ButtonGroup();

    m_rb_grow = makeJRadioButton(regionButtons, RDBTN_GROW, true);
    m_rb_square = makeJRadioButton(regionButtons, RDBTN_SQUARE, false);
    m_rb_circle = makeJRadioButton(regionButtons, RDBTN_CIRCLE, false);
    m_rb_highprob = makeJRadioButton(findButtons, RDBTN_HIGHPROB, true);
    m_rb_selcolour = makeJRadioButton(findButtons, RDBTN_SELCOLOUR, false);

    JPanel topPane = new JPanel(new BorderLayout());

    JPanel scanPane = new JPanel(new BorderLayout());
    topPane.add(scanPane, BorderLayout.NORTH);
    
    scanPane.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createEmptyBorder(5,5,5,5),
                 BorderFactory.createEtchedBorder()));

    JPanel growPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
    growPane.add(m_rb_grow);
    m_growingThres = new JTextField("15", 5);
    growPane.add(m_growingThres);
    scanPane.add(growPane, BorderLayout.NORTH);
    
    // add a new upper panel
    JPanel topPane2 = new JPanel(new BorderLayout());
    scanPane.add(topPane2, BorderLayout.CENTER);

    JPanel sqPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
    sqPane.add(m_rb_square);
    topPane2.add(sqPane, BorderLayout.NORTH);
    
    // add a new upper panel
    JPanel topPane3 = new JPanel(new BorderLayout());
    topPane2.add(topPane3, BorderLayout.CENTER);

    JPanel circlePane = new JPanel(new FlowLayout(FlowLayout.LEFT));
    circlePane.add(m_rb_circle);
    topPane3.add(circlePane, BorderLayout.NORTH);
    
    // add a new upper panel
    JPanel topPane4 = new JPanel(new BorderLayout());
    topPane3.add(topPane4, BorderLayout.CENTER);

    JPanel scanBtnPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
    scanBtnPane.add(makeJButton(BTN_SCAN));
    topPane4.add(scanBtnPane, BorderLayout.NORTH);

    JPanel bottomPane = new JPanel(new BorderLayout());
    topPane.add(bottomPane, BorderLayout.CENTER);
    
    // Now create the pane for the find radio buttons
    JPanel findPane = new JPanel(new BorderLayout());
    findPane.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createEmptyBorder(5,5,5,5),
                 BorderFactory.createEtchedBorder()));
    bottomPane.add(findPane, BorderLayout.NORTH);

    JPanel mostProbPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
    mostProbPane.add(m_rb_highprob);
    findPane.add(mostProbPane, BorderLayout.NORTH);
    
    // add a new upper panel
    JPanel topPane5 = new JPanel(new BorderLayout());
    findPane.add(topPane5, BorderLayout.CENTER);

    JPanel currEditPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
    currEditPane.add(m_rb_selcolour);
    m_currSelectedThres = new JTextField("0.9", 5);
    currEditPane.add(m_currSelectedThres);
    topPane5.add(currEditPane, BorderLayout.NORTH);
    
    // add a new upper panel
    JPanel topPane6 = new JPanel(new BorderLayout());
    topPane5.add(topPane6, BorderLayout.CENTER);

    JPanel findBtnPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
    findBtnPane.add(makeJButton(BTN_FIND));
    topPane6.add(findBtnPane, BorderLayout.NORTH);

    JPanel topPane7 = new JPanel(new BorderLayout());
    bottomPane.add(topPane7, BorderLayout.CENTER);
    
    JPanel closeBtnPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    closeBtnPane.add(makeJButton(BTN_ERASE));
    closeBtnPane.add(makeJButton(BTN_CLOSE));
    topPane7.add(closeBtnPane, BorderLayout.NORTH);
  
    // Root panel
    Container root = getContentPane();
    root.setLayout(new BorderLayout());
    root.add(topPane, BorderLayout.CENTER);
    setSize(300,300);
    this.pack();

    setResizable(false);
  }
  //----------------------------------------------------------------------
  /**
   *
   */
  private JRadioButton makeJRadioButton(ButtonGroup bg,
                                        String s,
                                        boolean selected)
  {
    JRadioButton rb = new JRadioButton(s, selected);
    bg.add(rb);
    return rb;
  }
  //----------------------------------------------------------------------
  /**
   * Utility method to make a {@link JButton} object, with the specified
   * label, which uses {@link #handleEvent} for the action-performed callback.
   */
  private JButton makeJButton(String label)
  {
    JButton b = new JButton(label);
    
    b.addActionListener(
                        new ActionListener()
                        {
                          public void actionPerformed(ActionEvent e)
                          { handleEvent(e); }
                        });
    
    return b;
  }
  //----------------------------------------------------------------------
  /**
   * Handle various GUI events.
   */
  private void handleEvent(ActionEvent e)
  {
    if (e.getActionCommand() == BTN_CLOSE)
    {
      setVisible(false);
      return;
    }

    if ((e.getActionCommand() == BTN_SCAN) && (m_scanDelegate != null))
    {
      m_scanDelegate.regionScan();
      return;
    }

    if ((e.getActionCommand() == BTN_ERASE) && (m_scanDelegate != null))
    {
      m_scanDelegate.eraseTrack();
      return;
    }

    if ((e.getActionCommand() == BTN_FIND) && (m_findDelegate != null))
    {
      try
      {
        m_findDelegate.
          showCandidates(Double.parseDouble(m_currSelectedThres.getText()));
      }
      catch (NumberFormatException ex)
      {
        m_log.warning("Threshold for selected colour is invalid:" + ex);
      }
      return;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Provide a {@link java.util.prefs.Preferences} object which allows
   * self to save various settings for future sessions.
   */
  public void saveSettings(Preferences props)
  {
    super.saveSettings(props); 

    props.put(PREF_KEY_GROW_THRESH, m_growingThres.getText());
    props.put(PREF_KEY_COLOUR_THRESH, m_currSelectedThres.getText()); 
  }
  //----------------------------------------------------------------------
  /**
   * Provide a {@link java.util.prefs.Preferences} object which allows
   * self to restore various settings saved from previous sessions. This
   * overrides the base class method because we don't want to restore
   * the dimension of this window. This window has its size set
   * initially to the packed size.
   */
  public void restoreSettings(Preferences props)
  {
    restoreLocation(props);
    restoreVisibility(props); 
    
    m_growingThres.setText(props.get(PREF_KEY_GROW_THRESH, "15"));
    m_currSelectedThres.setText(props.get(PREF_KEY_COLOUR_THRESH, "0.9"));
  }	
  //----------------------------------------------------------------------
  /**
   * Implement {@link ClassifierInterface} interface
   */
  public int getScanMode()
  {
    if (m_rb_grow.isSelected()) return GROW_REGION_MODE;
    if (m_rb_square.isSelected()) return SQUARE_REGION_MODE;
    if (m_rb_circle.isSelected()) return CIRCLE_REGION_MODE;

    // we should never reach here, but if we do, here's how we handle it
    m_rb_circle.setSelected(true);
    return CIRCLE_REGION_MODE;
  }
  //----------------------------------------------------------------------
  /**
   * Implement {@link ClassifierInterface} interface
   */
  public double getGrowThreshold()
  {
    try
    {
      return Double.parseDouble(m_growingThres.getText());
    }
    catch (NumberFormatException ex)
    {
      m_log.warning("Threshold for growing colour is invalid:" + ex);
      m_growingThres.setText("1.0");
      return 1.0;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Implement {@link ClassifierInterface} interface
   */
  public double getProbThreshold()
  {
    try
    {
	  return Double.parseDouble(m_currSelectedThres.getText());
    }
    catch (NumberFormatException ex)
    {
	  m_log.warning("Threshold for probabilistic is invalid:" + ex);
	  return 0.0;
	}
  }
  //----------------------------------------------------------------------
  /**
   * Implement {@link ClassifierInterface} interface
   */
  public int getFindMode()
  {
    if (m_rb_highprob.isSelected()) return MOST_PROBABLE_COLOUR;
    if (m_rb_selcolour.isSelected()) return COLOUR_ONEDITION_HIGHLY_PROB;

    // we should never reach here, but if we do, here's how we handle it
    m_rb_selcolour.setSelected(true);
    return COLOUR_ONEDITION_HIGHLY_PROB;
  }
  //----------------------------------------------------------------------
  /**
   * Implement {@link ClassifierInterface} interface
   */
  public void setScanCallBack(ScanInterface myScanInterface)
  {
    m_scanDelegate = myScanInterface;
  }
  //----------------------------------------------------------------------
  /**
   * Implement {@link ClassifierInterface} interface
   */
  public void setCandidatesCallBack(ScanInterface myScanInterface)
  {
    m_findDelegate = myScanInterface;
  }
}
