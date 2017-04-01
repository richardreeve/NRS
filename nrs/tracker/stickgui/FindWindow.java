//$Id: FindWindow.java,v 1.6 2005/05/09 22:05:12 hlrossano Exp $
package nrs.tracker.stickgui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import nrs.toolboxes.LayerPanel;
import nrs.toolboxes.Toolbox;
import nrs.toolboxes.ToolboxParent;
import nrs.tracker.palette.ExampleFileFilter;
import nrs.tracker.sticktrack.Group;
import nrs.tracker.sticktrack.Matrix;
import nrs.tracker.jointmanager.JointSequenceManager;
//import nrs.tracker.sticktrack.Vector2D;

public class FindWindow extends Toolbox implements FindJointsInterface
{
  /** Title of the dialog */
  public static String TITLE = "Joint Finder";
  private static final String PREF_KEY_CURR_DIR = "FINDWINDOW_CURR_DIR";

  // Main menu bar
  private JMenuBar m_menuBar = new JMenuBar();

  // Menus
  private JMenu m_menuFile = new JMenu();
  private JMenu m_menuGrid = new JMenu();
  private JMenu m_menuCalibration = new JMenu();

  // Menu options
  private final String CLOSE = "Close";
  private final String GRID = "Set Reference Grid";
  private final String GRIDTOOLS =  "Grid";
  private final String CALIBTOOLS =  "Calibration";
  private final String SAVEAS = "Save Sequence As...";
  private final String EXPORT = "Save Grid As...";
  private final String LOADGRID = "Load Grid...";
  private final String CALIB = "Calibrate with...";
  private final String SAVECAL = "Save weights As...";
  private final String LOADCAL = "Load weights...";
  private final String ADJU = "Adjust Coordinates";

  JFileChooser m_fc_m = new JFileChooser();
  JFileChooser m_fc_cal = new JFileChooser();

  // Gui elements
  private JTextField m_scanSize;
  private JTextField m_scanStep;
  private JTextField m_scale;
  private JTextField m_offset;
  private JTextField m_minSizeGroup;

  private JCheckBoxMenuItem m_cbm_adju;

  private JCheckBox m_cb_find;
  private JCheckBox m_cb_improve;
  private JCheckBox m_cb_doubleScale;

  private JRadioButton m_rb_all;
  private JRadioButton m_rb_prevRegion;
  private JRadioButton m_rb_one;
  private JRadioButton m_rb_many;

  private JLabel m_jl_coords;
  private MyJLabel goToFrame;

  // Button labels
  private final String BTN_BACK = "<<";
  private final String BTN_FORW = ">>";
  private final String BTN_FIND = "Find";
  private final String BTN_MANUAL = "Manual";
  private final String BTN_DISPLAY = "Transform Image";

  private final String CHBTN_FIND = "Find Cursor Active";
  private final String CHBTN_IMPROVE = "Improve?";
  private final String CHBTN_DOUBLESCALE = "Double scale";

  private final String RDBTN_ALL = "Find in all image";
  private final String RDBTN_PREVIOUS = "Where Previous";
  private final String RDBTN_ONE = "Frame by frame";
  private final String RDBTN_MANY = "Many";

  private ExampleFileFilter mfilter = new ExampleFileFilter();
  private ExampleFileFilter cfilter= new ExampleFileFilter();

  private final String LABEL_GO = "";
  
  // Keys used in saving to Preferences
  String PREF_KEY_DOUBLE_SIZE = "DOUBLE_SIZE";
  String PREF_KEY_BEST_QUALITY = "BEST_QUALITY";
  String PREF_KEY_SCAN_SIZE = "SCAN_SIZE";
  String PREF_KEY_SCAN_STEP = "SCAN_STEP";
  String PREF_KEY_SCALE = "SCALE";
  String PREF_KEY_OFFSET = "OFFSET";
  String PREF_KEY_COUNTER = "COUNTER";
  String PREF_KEY_MINSIZE = "MINSIZEGROUP";
  String PREF_KEY_FIND_ACTIVE = "FIND_ACTIVE";

  // Class logger
  private static Logger m_log = Logger.getLogger("nrs.tracker.palette");

  // Scan events are delegated to this object
  private FindInterface m_findDelegate;

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
  public FindWindow(Frame owner, ToolboxParent parent)
  {
    super(owner, TITLE, parent, "FindWindow_");
    guiInit();

	mfilter.addExtension("m");
	mfilter.setDescription("Joints");

	cfilter.addExtension("cal");
	cfilter.setDescription("Calibration");

  }
  //----------------------------------------------------------------------
  /**
   * Build the GUI by placing various components on the root
   * panel. Should only be called once.
   */
  private void guiInit()
  {

    m_menuFile.setText("File");
	m_menuGrid.setText(GRIDTOOLS);
	m_menuCalibration.setText(CALIBTOOLS);

    m_menuBar.add(m_menuFile);
    m_menuBar.add(m_menuGrid);
	m_menuBar.add(m_menuCalibration);
    this.setJMenuBar(m_menuBar);

    // Configure the main menu
    m_menuFile.add(makeMenuItem(SAVEAS));
	m_menuFile.addSeparator();
    m_menuFile.add(makeMenuItem(CLOSE));

	m_menuGrid.add(makeMenuItem(GRID));
	m_menuGrid.add(makeMenuItem(EXPORT));
	m_menuGrid.add(makeMenuItem(LOADGRID));

	m_menuCalibration.add(makeMenuItem(CALIB));
	m_menuCalibration.add(makeMenuItem(SAVECAL));
	m_menuCalibration.add(makeMenuItem(LOADCAL));
	m_cbm_adju = new JCheckBoxMenuItem(ADJU, false);
	m_menuCalibration.add(m_cbm_adju);

	LayerPanel mainPane = new LayerPanel();
	LayerPanel stepCtrl = new LayerPanel();
	LayerPanel scanCtrl = new LayerPanel();
	LayerPanel control = new LayerPanel();
	LayerPanel image = new LayerPanel();
	LayerPanel calibration = new LayerPanel();

    m_cb_find = makeJCheckBox(CHBTN_FIND, false);

	control.add(m_cb_find);
	control.add(makeJButton(BTN_MANUAL));
	control.createRow();
	control.add(makeJButton(BTN_FIND));
	control.add(new JLabel("Min size"));
	m_minSizeGroup = new JTextField("5", 5);
	control.add(m_minSizeGroup);
	control.createRow();
	control.setBorder();

    m_cb_improve = makeJCheckBox(CHBTN_IMPROVE, false);
	m_cb_doubleScale = makeJCheckBox(CHBTN_DOUBLESCALE, false);
	image.add(makeJButton(BTN_BACK));
	goToFrame = new MyJLabel(LABEL_GO, 0);
	image.add(goToFrame);
	image.add(makeJButton(BTN_FORW));
	image.createRow();
	image.add(m_cb_improve);
	image.add(m_cb_doubleScale);
	image.setBorder();

	m_scanStep = new JTextField("15", 5);
	ButtonGroup bg1 = new ButtonGroup();
	m_rb_one = makeJRadioButton(bg1, RDBTN_ONE, true);
	m_rb_many = makeJRadioButton(bg1, RDBTN_MANY, false);

	stepCtrl.add(m_rb_one);
	stepCtrl.createRow();
	stepCtrl.add(m_rb_many);
	stepCtrl.add(m_scanStep);
	stepCtrl.setBorder();

	m_scanSize = new JTextField("15", 5);
	ButtonGroup bg2 = new ButtonGroup();
	m_rb_all = makeJRadioButton(bg2, RDBTN_ALL, false);
	m_rb_prevRegion = makeJRadioButton(bg2, RDBTN_PREVIOUS, true);

	scanCtrl.add(m_rb_all);
	scanCtrl.createRow();
	scanCtrl.add(m_rb_prevRegion);
	scanCtrl.add(m_scanSize);
	scanCtrl.setBorder();

	m_jl_coords = new JLabel("(0,0)");
	calibration.add(new JLabel("Coordinates"));
	calibration.add(m_jl_coords);
	calibration.createRow();
	calibration.add(new JLabel("scale"));
	m_scale = new JTextField("20", 5);
	calibration.add(m_scale);
	calibration.add(new JLabel("offset"));
	m_offset = new JTextField("30", 5);
	calibration.add(m_offset);
	calibration.add(makeJButton(BTN_DISPLAY));
	calibration.setBorder();

	mainPane.add(control);
	mainPane.add(image);
	mainPane.createRow();
	mainPane.add(calibration);
	mainPane.createRow();
	mainPane.add(stepCtrl);
	mainPane.add(scanCtrl);

    // Root panel
    Container root = getContentPane();
    root.setLayout(new BorderLayout());
    root.add(mainPane, BorderLayout.CENTER);
    setSize(500,300);
    this.pack();
    setResizable(false);
  }

	public void increaseCount(){
		int i = (int)goToFrame.getDouble();
		i++;
		m_findDelegate.setImage(i);
		goToFrame.setInt(i);
	}

	public void decreaseCount(){
		int i = (int)goToFrame.getDouble();
		i--;
		m_findDelegate.setImage(i);
		goToFrame.setInt(i);
	}


  //----------------------------------------------------------------------
  /**
   *
   */
  private void handleMenuEvent(ActionEvent e)
  {
    JMenuItem source = (JMenuItem)(e.getSource());
    
    // Defensive programming
    if (source == null) return;

    if (source.getText() == CLOSE)
      {
		  setVisible(false);
		  return;
      }
    if (source.getText() == GRID)
      {
		  m_findDelegate.setGrid();
		  return;
      }
    if (source.getText() == CALIB)
      {
		m_fc_cal.setFileFilter(cfilter);
		m_findDelegate.calibrate(Group.getGroupFromFile(getFileName(1, "cal", m_fc_cal)));

    	return;
      }
    if (source.getText() == LOADGRID)
      {
		m_fc_m.setFileFilter(mfilter);
		m_findDelegate.loadGrid(Group.getGroupFromFile(getFileName(1, "m", m_fc_m)));
		return;
      }

    if (source.getText() == SAVECAL)
      {
		m_fc_m.setFileFilter(mfilter);
		m_findDelegate.saveCalibration(getFileName(0, "m", m_fc_m));
		return;
      } 

    if (source.getText() == LOADCAL)
      {
		m_fc_m.setFileFilter(mfilter);
		m_findDelegate.loadWeights(Matrix.getMatrixFromFile(getFileName(1, "m", m_fc_m)));
		return;
      } 

    if (source.getText() == EXPORT)
      {
		m_fc_m.setFileFilter(mfilter);
		m_findDelegate.exportGrid(getFileName(0, "m", m_fc_m));
		return;
      }  

  }
  
	  private void findMarks(){
	  	Object[] options = {"Yes", "Yes All", "No", "No All"};
	  	int question = 0;
		int status;
		if (m_rb_one.isSelected() )
			m_findDelegate.getRegionGroups(m_rb_prevRegion.isSelected() );
		if (m_rb_many.isSelected() ){
			int j = (int)goToFrame.getDouble();
			m_findDelegate.visualEnable(false);
			for(int i =0;i<Integer.parseInt(m_scanStep.getText());i++){
				status = m_findDelegate.getRegionGroups(m_rb_prevRegion.isSelected());
				if (status == JOptionPane.NO_OPTION){
					break;
				}
				if((status==JointSequenceManager.MISSING_JOINTS)||(status==JointSequenceManager.EXTRA_JOINTS)){
					if ((question==0)||(question==2)){
						question = JOptionPane.showOptionDialog(null, "Number of sequences changed\n" +
								"Stop for sequence checking?", "Find Problem", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
					}
					if (question < 2){
						m_findDelegate.setImage(j);
						m_findDelegate.visualEnable(true);
						break;
					}
				}
				j++;
				goToFrame.setInt(j);
				this.paint(this.getGraphics());
				m_findDelegate.setImage(j);
			}
			m_findDelegate.visualEnable(true);
		}
		return;
	  }

	private String getFileName(int type, String ext, JFileChooser chooser){
		ext = "."+ext;
		if (type == 0){
			if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)	return "";}
		else{
			if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)	return "";}
		String _filename = chooser.getSelectedFile().getAbsolutePath();
		if (_filename.length() == 0) return "";
		if (!_filename.endsWith(ext.toUpperCase()) && !_filename.endsWith(ext.toLowerCase()) )
			_filename += ext;
		return _filename;
	}	

  //-------------------------------------------------------------------------
  /**
   * Utility method to turn a menu item string (the parameter) into a
   * {@link JMenuItem} which displays that string and has a callback to
   * this class registered.
   */
  private JMenuItem makeMenuItem(String menuLabel)
  {
    JMenuItem newItem = new JMenuItem(menuLabel);

    newItem.addActionListener(
                              new ActionListener()
                              {
                                public void actionPerformed(ActionEvent e)
                                { handleMenuEvent(e); }
                              });

    return newItem;
  }

  //----------------------------------------------------------------------
  /**
   *
   */
  private JCheckBox makeJCheckBox(String s,
                                        boolean selected)
  {
    JCheckBox cb = new JCheckBox(s, selected);
    return cb;
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

    if(e.getActionCommand() == BTN_MANUAL && m_findDelegate != null){
		m_findDelegate.addManual();
	}

    if ((e.getActionCommand() == BTN_FIND) && (m_findDelegate != null))
    {
    	findMarks();
    	return;
    }

    if ((e.getActionCommand() == BTN_FORW) )
    {
	increaseCount();
      return;
    }
    if ((e.getActionCommand() == BTN_BACK) )
    {
	decreaseCount();
      return;
    }

	if ((e.getActionCommand() == BTN_DISPLAY))
	{
		m_findDelegate.displayTransform(Integer.parseInt(m_scale.getText()), Integer.parseInt(m_offset.getText()));
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

    props.put(PREF_KEY_SCAN_SIZE, m_scanSize.getText());
    props.put(PREF_KEY_SCAN_STEP, m_scanStep.getText()); 
    props.put(PREF_KEY_SCALE, m_scale.getText()); 
    props.put(PREF_KEY_OFFSET, m_offset.getText()); 
    props.put(PREF_KEY_MINSIZE, m_minSizeGroup.getText()); 
    props.put(PREF_KEY_CURR_DIR, m_fc_m.getCurrentDirectory().getAbsolutePath());
    props.put(PREF_KEY_CURR_DIR, m_fc_cal.getCurrentDirectory().getAbsolutePath());
    props.put(PREF_KEY_CURR_DIR, m_fc_cal.getCurrentDirectory().getAbsolutePath());
    props.put(PREF_KEY_BEST_QUALITY, String.valueOf(m_cb_improve.isSelected()));
    props.put(PREF_KEY_DOUBLE_SIZE, String.valueOf(m_cb_doubleScale.isSelected()));
    props.put(PREF_KEY_FIND_ACTIVE, String.valueOf(m_cb_find.isSelected()));
    
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
    
    m_scanSize.setText(props.get(PREF_KEY_SCAN_SIZE, "25"));
    m_scanStep.setText(props.get(PREF_KEY_SCAN_STEP, "10"));
    m_scale.setText(props.get(PREF_KEY_SCALE, "20"));
    m_offset.setText(props.get(PREF_KEY_OFFSET, "30"));
	m_minSizeGroup.setText(props.get(PREF_KEY_MINSIZE, "5"));
    String path = props.get(PREF_KEY_CURR_DIR, "");
    m_cb_improve.setSelected(Boolean.valueOf(props.get(PREF_KEY_BEST_QUALITY, String.valueOf(false))).booleanValue());
    m_cb_doubleScale.setSelected(Boolean.valueOf(props.get(PREF_KEY_DOUBLE_SIZE, String.valueOf(false))).booleanValue());
    m_cb_find.setSelected(Boolean.valueOf(props.get(PREF_KEY_FIND_ACTIVE, String.valueOf(false))).booleanValue());
    if (path != "") m_fc_m.setCurrentDirectory(new File(path));
    if (path != "") m_fc_cal.setCurrentDirectory(new File(path));

  }	
  //----------------------------------------------------------------------
  public int getMinSizeGroup(){
		try
		{
		  return Integer.parseInt(m_minSizeGroup.getText());
		}
		catch (NumberFormatException ex)
		{
		  m_log.warning("Error for scan region size" + ex);
		  return -1;
		}
  }
  //----------------------------------------------------------------------
  public int getScanSize()
  {
    try
    {
	  return Integer.parseInt(m_scanSize.getText());
    }
    catch (NumberFormatException ex)
    {
	  m_log.warning("Error for scan region size" + ex);
	  return -1;
	}
  }
  //----------------------------------------------------------------------
  public void setCoords(int x, int y, double x2, double y2){
	  if( m_cbm_adju.isSelected() )
		m_jl_coords.setText("("+x+","+y+")\t-->\t"+"("+x2+","+y2+")");
	  else
		m_jl_coords.setText("("+x+","+y+")");
  }
  //----------------------------------------------------------------------
  public boolean getFindMode()
  {
    return m_cb_find.isSelected();
  }
  //----------------------------------------------------------------------
  public boolean getImproveMode()
  {
    return m_cb_improve.isSelected();
  }
  //----------------------------------------------------------------------
  public boolean getDoubleScale()
  {
    return m_cb_doubleScale.isSelected();
  }
  //----------------------------------------------------------------------
  public boolean getAdjustMode()
  {
    return m_cbm_adju.isSelected();
  }
  //----------------------------------------------------------------------
  public boolean editMassCentres(){
	  return true;
  }
  
  public int getCounter(){
  	return (int)goToFrame.getDouble();
  }
  
  //----------------------------------------------------------------------
  /**
   * Implement {@link FindJointsInterface} interface
   */
  public void setFindCallBack(FindInterface myFindInterface)
  {
    m_findDelegate = myFindInterface;
  }

	class MyJLabel extends JLabel implements MouseListener{

		private String label;
		
		MyJLabel(String label, int value){
			super(label+value);
			this.label=label;
			this.addMouseListener(this);
		}
		
		MyJLabel(String label){
			this(label, 0);
		}
		
		public double getDouble(){
			return Double.parseDouble(getText().substring(label.length()) );
		}
		
		public void setDouble(double value){
			this.setText(label + value);
		}
		
		public void setInt(int value){
			this.setText(label + value);
		}
		
		public void mouseClicked(MouseEvent e) {
			String newVal = JOptionPane.showInputDialog(this, "Type new Value", "Value", JOptionPane.QUESTION_MESSAGE);
			newVal.trim();
			if (newVal == "")
				return;
			try{
				Double.parseDouble(newVal);
			}catch(Exception ex){
				return;
			}
			this.setText(label+newVal);
			m_findDelegate.repaint();
		}

		public void mouseEntered(MouseEvent e) {
			this.setCursor(new Cursor(Cursor.HAND_CURSOR));
		}		
		
		public void mousePressed(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
	}  
  
}
