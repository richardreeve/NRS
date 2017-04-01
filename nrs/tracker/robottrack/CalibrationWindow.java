package nrs.tracker.robottrack;


// GUI Components
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JFormattedTextField;
import javax.swing.SpringLayout;
import javax.swing.text.DefaultFormatter;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
//import javax.swing.AbstractListModel;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;
import javax.swing.BorderFactory;

// Layout Managers
import javax.swing.BoxLayout;
import nrs.toolboxes.MacroSpringLayout;
import javax.swing.Spring;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
//import java.awt.GridBagLayout;
//import java.awt.GridBagConstraints;

// Other GUI stuff
import java.awt.Graphics;
import java.awt.Dimension;
//import java.awt.Insets;
import java.awt.Point;
import java.awt.Color;
import java.awt.geom.Point2D;

// Events
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

// Formatting
import java.text.NumberFormat;
import java.text.ParseException;

// File IO
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

// Other
import nrs.toolboxes.Toolbox;
import nrs.toolboxes.ToolboxParent;
import java.util.prefs.Preferences;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.Properties;

/**
 * <p>A subclass of <code>JFrame</code> that displays the camera calibration user interface. The 
 * actual calibration algorithms are all provided by the <code>CameraProjection</code> class. The 
 * nested class <code>EventListenerImpl</code> handles all the events triggered by the 
 * <code>JButton</code>s and <code>JFormattedTextField</code>s. 
 *
 * <p>The documentation of <code>CameraProjection</code> explains in detail how the calibration 
 * process works. Running that process through the UI provided by this class should be 
 * straighforward. The only issue you might find surprising is that points in the image have to be 
 * selected <i>before</i> you can click on buttons like <code>Select</code> etc. How points are 
 * selected is explained in the documentation of <code>CalibrationCanvas</code>, and images are 
 * loaded in the main window.
 *
 * @see CameraProjection
 * @see CalibrationCanvas
 *
 * @author Tobias Oberlies
 */
public class CalibrationWindow extends Toolbox
{
	// Dialog title
	public static String TITLE = "Camera Calibration";
	
	// GUI text fields
	/** Text field for the optical axis. */
	private JFormattedTextField m_ftx_optAxisX, m_ftx_optAxisY;
	/** Text field for the focal length. */
	private JFormattedTextField m_ftx_focalLength;
	/** Text field for the vanishing points. */
	private JFormattedTextField m_ftx_vanishPtHorX, m_ftx_vanishPtHorY, m_ftx_vanishPtP, m_ftx_vanishPtVertX, m_ftx_vanishPtVertY, m_ftx_vanishPtQ, m_ftx_principalPtX, m_ftx_principalPtY;
	/** Text field for scaling entries. */
	private JFormattedTextField m_ftx_scaleImX, m_ftx_scaleImY, m_ftx_scaleReX, m_ftx_scaleReY;
	/** List for the scaling map. */
	private JList m_lst_scaleMap;
	/** List model for scaling map list. */
	private CameraProjection.CoordinateMap m_lm_scaleMap;
	/** Text area for the test panel. */
	private JTextArea m_txa_transformed;
	/** Text field for the test results. */
	private JFormattedTextField m_ftx_testGrid;
	/** Text field for the camera descriptions. */
	private JTextField m_txt_cameraType, m_txt_cameraLoc;
	/** Text field for the resolution. */
	private JFormattedTextField m_ftx_resolution;
	/** Text fields for the scene coordinate system descriptions. */
	private JTextField m_txt_coordDescr, m_txt_coordUnit, m_txt_coordHeight;
	/** File chooser for loading/saving calibration files. */
	private JFileChooser m_fileChooser = new JFileChooser();
	
	
	// GUI button identifiers. NB: This is used to identify the actionEvent source rather than the 
	// text because the latter is ambiguous. Alternatively storing the object references and 
	// comparing them with ActionEvent.getSource() would have worked, too.
	
	/** Identifier for the 'Direct' radiobutton. */
	private String BTN_PSM_DIRECT = "Point Select Mode/Direct";
	/** Identifier for the 'Edge' radiobutton. */
	private String BTN_PSM_EDGE = "Point Select Mode/Edge";
	/** Identifier for the 'Show Curvature' button. */
	private String BTN_OA_CURVATURE = "Optical Axis/Show Curvature";
	/** Identifier for the 'Select' button on the 'Optical Axis' panel. */
	private String BTN_OA_SELECT = "Optical Axis/Select";
	/** Identifier for the 'Find' button on the 'Focal Length' panel. */
	private String BTN_FL_FIND = "Focal Length/Find";
	/** Identifier for the 'Find' button on the 'Horizontal Vanishing Point' panel. */
	private String BTN_HVP_FIND = "Horizontal Vanishing Point/Find";
	/** Identifier for the 'Find' button on the 'Vertical Vanishing Point' panel. */
	private String BTN_VVP_FIND = "Vertical Vanishing Point/Find";
	/** Identifier for the 'Select' button on the 'Scaling' panel. */
	private String BTN_SC_SELECT = "Scaling/Select";
	/** Identifier for the 'Add' button on the 'Scaling' panel. */
	private String BTN_SC_ADD = "Scaling/Add";
	/** Identifier for the 'Remove' button on the 'Scaling' panel. */
	private String BTN_SC_REMOVE = "Scaling/Remove";
	/** Identifier for the 'Transform Selected' button on the 'Test' panel. */
	private String BTN_TST_TRANSFORM = "Test/Transform Selected";
	/** Identifier for the 'Show Grid' button on the 'Test' panel. */
	private String BTN_TST_GRID = "Test/Show Grid";
	/** Identifier for the 'Get' button on the 'Load&Save' panel. */
	private String BTN_LS_RESOLUTION = "Camera/Get Resolution";
	/** Identifier for the 'Load' button on the 'Load&Save' panel. */
	private String BTN_LS_LOAD = "Load";
	/** Identifier for the 'Save' button on the 'Load&Save' panel. */
	private String BTN_LS_SAVE = "Save";
	
	
	/** Listener class for events. */
	private CalibrationWindow.EventListenerImpl m_eventListener = new CalibrationWindow.EventListenerImpl();
	
	/** Reference to the user interface for calibration data input. */
	private CalibrationDataInputInterface m_dataInput;
	/** <code>true</code> if marks have been painted into the CalibrationDataInputInterface. */
	private boolean m_dataInputPainted = false;
	
	/** Class providing the implementation of the calibration functionality. */
	private CameraProjection m_core;
	
	/** File which is affiliated with the current calibration parameters. This is the file from 
		which they were loaded or last saved to. */
	private File m_fromFile;
	
	/** Logger for calibration messages. */
	private static Logger m_log = Logger.getLogger("nrs.tracker.calibration");
	

	
	/**
	 * Default constructor.
	 *
	 * @param owner the <code>java.awt.Frame</code> which owns this window. Can
	 * be set to null.
	 *
	 * @param parent the <code>ToolboxParent</code> object to notify when the
	 * visibility of this dialog changes. Can be set to null.
	 *
	 * @param dataInput A component that implements <code>CalibrationDataInputInterface</code>. This 
	 * component will be controlled by the calibration window and will provide the data the user has
	 * provided (by point & click).
	 *
	 * @see nrs.toolboxes.Toolbox
	 * @see nrs.toolboxes.ToolboxParent
	 *
	 */
	public CalibrationWindow(Frame owner, ToolboxParent parent, CalibrationDataInputInterface dataInput)
	{
		super(owner, TITLE, parent, "Calibration_");

		// Initialize data input component
		m_dataInput = dataInput;
		m_dataInput.setSelectionSetMode(CalibrationDataInputInterface.SELECT_POINTS_ON_LINE);
		m_dataInput.setForeground(Color.RED);
		
		// Initialize the calibration core
		m_core = new CameraProjection();
		m_core.useCoordinateMap();
		
		// Initialize GUI
		guiInit();
	}
	
	
	/* Restore settings. Overloaded method from Toolbox. */
	public void restoreSettings(Preferences prefs)
	{
		// Restore window position
		super.restoreSettings(prefs);
		
		// Restore calibration file directory
		try
		{
			File directory = new File(prefs.get("Calibration_CalibrationFilesDirectory", null));
			if (directory.isDirectory())
			{
				m_fileChooser.setCurrentDirectory(directory);
			}
		}
		catch (NullPointerException e)
		{
			// Preference key doesn't exist, so don't set the calibration file directory
		}					
	}
	
	/* Save settings. Overloaded method from Toolbox. */
	public void saveSettings(Preferences prefs)
	{
		// Save window position
		super.restoreSettings(prefs);
		
		// Save calibration file directory
		prefs.put("Calibration_CalibrationFilesDirectory", m_fileChooser.getCurrentDirectory().getAbsolutePath());
	}
	
	
	//------------------------------------------------------------------------------------------------
	// Build the GUI

	/**
	 * Builds the GUI. This method should only be called by the constructor.
	 */
	private void guiInit()
	{
		// Create tabbed pane
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.addTab("Opt. Axis", initOpticalAxisPanel());
		tabbedPane.addTab("Focal Length", initFocalLengthPanel());
		tabbedPane.addTab("Vanishing Pt.", initVanishingPointPanel());
		tabbedPane.addTab("Scaling", initScalingPanel());
		tabbedPane.addTab("Test", initTestPanel());
		tabbedPane.addTab("Load & Save", initSavePanel());
		tabbedPane.addChangeListener(m_eventListener);

		// Create point selector panel
		JComponent pointSel = initPointSelectorPanel();
		
		// Overall layout: calibration tabs above point selector panel
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(tabbedPane, BorderLayout.CENTER);
		contentPane.add(pointSel, BorderLayout.PAGE_END);
		
		//Default size
        this.pack();
 
	}
	
	/**
	 * Builds the point selector panel of the UI. The user has options how he wants to select
	 * the points for the various calibration steps.
	 *
	 * @return the panel with the radio buttons.
	 */
	private JComponent initPointSelectorPanel()
	{
		// Grid layout with one column
		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,0,0,0), BorderFactory.createTitledBorder("Point Selection Mode")));
		
		// Create and group radio buttons
		JRadioButton buttonDirect = createJRadioButton("Direct", BTN_PSM_DIRECT);
		JRadioButton buttonEdge = createJRadioButton("Edge", BTN_PSM_EDGE);

		ButtonGroup group = new ButtonGroup();
		group.add(buttonDirect);
		group.add(buttonEdge);
		buttonDirect.setSelected(true);
		
		// Add to the panel
		panel.add(buttonDirect);
		panel.add(buttonEdge);
		
		return panel;
	}
	
	/**
	 * Builds the optical axis panel of the UI.
	 *
	 * @return the panel with all components to find and set the optical axis.
	 */
	private JComponent initOpticalAxisPanel()
	{
		// Initialize variables
		m_ftx_optAxisX = createTextField(3, 6, null, 0);
		m_ftx_optAxisY = createTextField(3, 6, null, 0);
		
		// Curvature panel
		Box cPanel = new Box(BoxLayout.LINE_AXIS);
		cPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Tool"), BorderFactory.createEmptyBorder(0,5,5,5)));
		cPanel.add(Box.createHorizontalGlue());
		cPanel.add(createButton("Show Curvature", BTN_OA_CURVATURE));
		
		// Optical Axis panel
		Box aPanel = new Box(BoxLayout.LINE_AXIS);
		aPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Optical Axis"), BorderFactory.createEmptyBorder(0,5,5,5)));
		aPanel.add(createLabel("X:"));
		aPanel.add(Box.createRigidArea(new Dimension(5,0)));
		aPanel.add(m_ftx_optAxisX);
		aPanel.add(Box.createRigidArea(new Dimension(10,0)));
		aPanel.add(createLabel("Y:"));
		aPanel.add(Box.createRigidArea(new Dimension(5,0)));
		aPanel.add(m_ftx_optAxisY);
		aPanel.add(Box.createRigidArea(new Dimension(10,0)));
		aPanel.add(Box.createHorizontalGlue());
		aPanel.add(createButton("Select", BTN_OA_SELECT));
		
		// Vertical arrangement
		Box panel = new Box(BoxLayout.PAGE_AXIS);
		panel.add(cPanel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(aPanel);
		return panel;
	}
	
	/**
	 * Builds the focal length panel of the UI.
	 *
	 * @return the panel with all components.
	 */
	private JComponent initFocalLengthPanel()
	{
		// Flow layout panel
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Focal Length"), BorderFactory.createEmptyBorder(0,5,5,5)));
		
		// Create and add the components
		JLabel labelH = createLabel("H:");
		m_ftx_focalLength = createTextField(6, null, 2);
		JButton buttonSelect = createButton("Find", BTN_FL_FIND);
		
		panel.add(labelH);
		panel.add(m_ftx_focalLength);
		panel.add(buttonSelect);
		
		return panel;
	}
	
	/**
	 * Builds the vanishing point panel of the UI.
	 *
	 * @return the panel with all components.
	 */
	private JComponent initVanishingPointPanel()
	{
		// Create boxes (panels with box layout) for horizontal and vertical vanishing points
		Box hPanel = new Box(BoxLayout.X_AXIS);
		Box vPanel = new Box(BoxLayout.X_AXIS);
		hPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Horizontal Vanishing Point"), BorderFactory.createEmptyBorder(0,5,5,5)));
		vPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Vertical Vanishing Point"), BorderFactory.createEmptyBorder(0,5,5,5)));
		
		// Initialize member variables
		m_ftx_vanishPtHorX =  createTextField(3, 6, null, 0);
		m_ftx_vanishPtHorY =  createTextField(3, 6, null, 0);
		m_ftx_vanishPtVertX = createTextField(3, 6, null, 0);
		m_ftx_vanishPtVertY = createTextField(3, 6, null, 0);
		m_ftx_vanishPtP =     createTextField(3, 6, null, 6);
		m_ftx_vanishPtQ =     createTextField(3, 6, null, 6);
		m_ftx_principalPtX =  createTextField(3, 6, null, 0);
		m_ftx_principalPtY =  createTextField(3, 6, null, 0);
		m_ftx_vanishPtP.setFocusLostBehavior(JFormattedTextField.REVERT);
		m_ftx_vanishPtQ.setFocusLostBehavior(JFormattedTextField.REVERT);
		m_ftx_principalPtX.setFocusLostBehavior(JFormattedTextField.REVERT);
		m_ftx_principalPtY.setFocusLostBehavior(JFormattedTextField.REVERT);
		
		// Buttons
		JButton buttonFindHor = createButton("Find", BTN_HVP_FIND);
		JButton buttonFindVert = createButton("Find", BTN_VVP_FIND);
				
		// Compose content of panel for horizontal vanishing point
		// (NB don't use createHorizontalStrut if you don't want the box to take up space in 
		// vertical direction. Struts seem to have unlimited maximum size.)
		hPanel.add(createLabel("X:"));
		hPanel.add(Box.createRigidArea(new Dimension(5,0)));
		hPanel.add(m_ftx_vanishPtHorX);
		hPanel.add(Box.createRigidArea(new Dimension(10,0)));
		hPanel.add(createLabel("Y:"));
		hPanel.add(Box.createRigidArea(new Dimension(5,0)));
		hPanel.add(m_ftx_vanishPtHorY);
		hPanel.add(Box.createRigidArea(new Dimension(10,0)));
		hPanel.add(Box.createHorizontalGlue());
		hPanel.add(buttonFindHor);

		// Compose content of panel for vertical vanishing point
		vPanel.add(createLabel("X:"));
		vPanel.add(Box.createRigidArea(new Dimension(5,0)));
		vPanel.add(m_ftx_vanishPtVertX);
		vPanel.add(Box.createRigidArea(new Dimension(10,0)));
		vPanel.add(createLabel("Y:"));
		vPanel.add(Box.createRigidArea(new Dimension(5,0)));
		vPanel.add(m_ftx_vanishPtVertY);
		vPanel.add(Box.createRigidArea(new Dimension(10,0)));
		vPanel.add(Box.createHorizontalGlue());
		vPanel.add(buttonFindVert);
		
		
		// Create panel for calculated parameters (with SpringLayout)
		MacroSpringLayout layout = new MacroSpringLayout();
		JPanel cPanel = new JPanel(layout);
		cPanel.setBorder(BorderFactory.createTitledBorder("Derived Parameters"));
		
		// Create and add components
		JLabel labelP = createLabel("<html>Horizontal VP distance<sup>-1</sup>:</html>");
		JLabel labelQ = createLabel("<html>Vertical VP distance<sup>-1</sup>:</html>");
		JLabel labelPrincipalPt = createLabel("Principal point");
		JLabel labelX = createLabel("X:");
		JLabel labelY = createLabel("Y:");

		cPanel.add(m_ftx_vanishPtP);
		cPanel.add(m_ftx_vanishPtQ);
		cPanel.add(m_ftx_principalPtX);
		cPanel.add(m_ftx_principalPtY);
		cPanel.add(labelP);
		cPanel.add(labelQ);
		cPanel.add(labelPrincipalPt);
		cPanel.add(labelX);
		cPanel.add(labelY);
		
		// Vertical arrangement of components
		layout.putBelow(m_ftx_vanishPtP, 5, null);
		layout.putBelow(m_ftx_vanishPtQ, 5, m_ftx_vanishPtP);
		layout.putBelow(new Component[]{labelPrincipalPt, labelX, m_ftx_principalPtX}, 5, m_ftx_vanishPtQ);
		layout.putBelow(new Component[]{labelY, m_ftx_principalPtY}, 5, m_ftx_principalPtX);
		layout.getConstraints(cPanel).setConstraint(SpringLayout.SOUTH, Spring.sum(layout.getConstraints(m_ftx_principalPtY).getConstraint(SpringLayout.SOUTH), Spring.constant(5)));
		
		// Special placing of the labels with the exponent
		int heightDiff = labelPrincipalPt.getPreferredSize().height - labelP.getPreferredSize().height;
		layout.putBelow(labelP, heightDiff+5, null);
		layout.putBelow(labelQ, heightDiff+5, m_ftx_vanishPtP);
		
		// Horizontal arrangement of components (static part)
		layout.putRightOf(new Component[]{labelP, labelQ, labelPrincipalPt}, 5, null);
		layout.putRightOf(new Component[]{labelX, labelY}, 5, labelPrincipalPt);
		layout.putRightOfSet(new Component[]{m_ftx_vanishPtP, m_ftx_vanishPtQ, m_ftx_principalPtX, m_ftx_principalPtY}, 5, new Component[]{labelP, labelQ, labelX, labelY});

		/// Resizing behaviour (allow the TextFields to be squeezed, but only to a certain extend)
		//
		// To archieve this I create a spring that is the sum of the following
		// - the sum of the (rigid) springs between and over the components left of the resizable 
		//   field. This summed spring is already constructed with the putRightOf statements above 
		//   (see SpringManager.putConstraint()).
		// - a flexible spring.
		// - a (rigid) spring for the space right of the resizable fields.
		//
		// The overall sum is then assigned to the container's width. Because this width is imposed 
		// from the outside, the spring is strained and so will every part of (see Spring.sum()). As 
		// there is only one flexible part in the spring, this part will take all the strain, i.e. 
		// do all the necessary resizing.
		//
		// It is not possible to limit the resizing range of the flexible spring (its size is 
		// computed only from the strain on it and therefore it's maximal and minimal sizes would 
		// be violated when too much force is imposed). However it is possible to construct the 
		// spring we want from that spring, that is by using the Spring.max() function.
		
		// Create the flexible spring
		Spring flexible = layout.createFlexibleWidthSpring(m_ftx_vanishPtP);
		
		// Compute the overall spring
		Spring overall = layout.getConstraints(m_ftx_vanishPtP).getX();
		overall = Spring.sum(overall, flexible);
		overall = Spring.sum(overall, Spring.constant(5));
		
		// Set this spring to the container size (so that the flexible part will be resized)
		layout.getConstraints(cPanel).setConstraint(SpringLayout.EAST, overall);
		
		// Compute the limited flexible spring (limited to respect the components minimum/maximum size)
		Spring respectful = layout.createRespectfulWidthSpring(m_ftx_vanishPtP, flexible);
		
		// Assign this spring to the TextFields (hence they'll all have the same size)
		layout.getConstraints(m_ftx_vanishPtP).setWidth(respectful);
		layout.getConstraints(m_ftx_vanishPtQ).setWidth(respectful);
		layout.getConstraints(m_ftx_principalPtX).setWidth(respectful);
		layout.getConstraints(m_ftx_principalPtY).setWidth(respectful);
		
		
		// Overall layout of panel (with BoxLayout)
		Box panel = new Box(BoxLayout.Y_AXIS);
		panel.add(hPanel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(vPanel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(cPanel);
		
		return panel;
	}

	/**
	 * Builds the scaling panel of the UI.
	 *
	 * @return the panel with all components.
	 */
	private JComponent initScalingPanel()
	{
		// Initialize variables
		m_ftx_scaleImX = createTextField(3, 6, null, 2);
		m_ftx_scaleImY = createTextField(3, 6, null, 2);
		m_ftx_scaleReX = createTextField(3, 6, null, 4);
		m_ftx_scaleReY = createTextField(3, 6, null, 4);
		m_lm_scaleMap = m_core.getCoordinateMap();
		m_lm_scaleMap.setNumberFormat(2, 4);
		m_lst_scaleMap = new JList(m_lm_scaleMap);
		m_lst_scaleMap.setVisibleRowCount(-1);				// Maximum that fits on the screen
		m_lst_scaleMap.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Create the panel for image coordinates
		Box iPanel = new Box(BoxLayout.LINE_AXIS);
		iPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Image Coordinates"), BorderFactory.createEmptyBorder(0,5,5,5)));
		
		iPanel.add(createLabel("X:"));
		iPanel.add(Box.createRigidArea(new Dimension(5,0)));
		iPanel.add(m_ftx_scaleImX);
		iPanel.add(Box.createRigidArea(new Dimension(10,0)));
		iPanel.add(createLabel("Y:"));
		iPanel.add(Box.createRigidArea(new Dimension(5,0)));
		iPanel.add(m_ftx_scaleImY);
		iPanel.add(Box.createRigidArea(new Dimension(10,0)));
		iPanel.add(Box.createHorizontalGlue());
		JButton buttonSelect = createButton("Select", BTN_SC_SELECT);
		iPanel.add(buttonSelect);
		
		// Create the panel for real-world coordinates
		Box rPanel = new Box(BoxLayout.LINE_AXIS);
		rPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Real-World Coordinates"), BorderFactory.createEmptyBorder(0,5,5,5)));
		
		rPanel.add(createLabel("X:"));
		rPanel.add(Box.createRigidArea(new Dimension(5,0)));
		rPanel.add(m_ftx_scaleReX);
		rPanel.add(Box.createRigidArea(new Dimension(10,0)));
		rPanel.add(createLabel("Y:"));
		rPanel.add(Box.createRigidArea(new Dimension(5,0)));
		rPanel.add(m_ftx_scaleReY);
		rPanel.add(Box.createHorizontalGlue());
		rPanel.add(Box.createRigidArea(new Dimension(10,0)));
		rPanel.add(Box.createRigidArea(buttonSelect.getPreferredSize()));
		
		// Create the panel for the mapping list
		Box mPanel = new Box(BoxLayout.PAGE_AXIS);
		mPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Mapping"), BorderFactory.createEmptyBorder(0,5,5,5)));
		Box mSubPanel = new Box(BoxLayout.LINE_AXIS);
		
		mPanel.add(new JScrollPane(m_lst_scaleMap));
		mPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		mPanel.add(mSubPanel);
		mSubPanel.add(Box.createHorizontalGlue());
		mSubPanel.add(createButton("Add", BTN_SC_ADD));
		mSubPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		mSubPanel.add(createButton("Remove", BTN_SC_REMOVE));
		
		// Overall arrangement
		Box panel = new Box(BoxLayout.PAGE_AXIS);
		
		panel.add(iPanel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(rPanel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(mPanel);
		panel.add(Box.createVerticalStrut(5));
		
		return panel;
	}
	
	/**
	 * Builds the test panel of the UI.
	 * @return the composed panel.
	 */
	private JComponent initTestPanel()
	{
		// Compose the transformation test panel
		m_txa_transformed = new JTextArea();
		JScrollPane scrollText = new JScrollPane(m_txa_transformed);
		JButton buttonTransform = createButton("Transform selected", BTN_TST_TRANSFORM);
		scrollText.setAlignmentX(Box.RIGHT_ALIGNMENT);
		buttonTransform.setAlignmentX(Box.RIGHT_ALIGNMENT);

		Box tPanel = new Box(BoxLayout.PAGE_AXIS);
		tPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Test Transformation"), BorderFactory.createEmptyBorder(0,5,5,5)));

		tPanel.add(buttonTransform);
		tPanel.add(Box.createVerticalStrut(5));
		tPanel.add(scrollText);
		
		// Compose the grid panel
		m_ftx_testGrid = createTextField(3, 6, new Integer(100), 2);
		
		Box gPanel = new Box(BoxLayout.LINE_AXIS);
		gPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Grid"), BorderFactory.createEmptyBorder(0,5,5,5)));

		gPanel.add(createLabel("Grid size:"));
		gPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		gPanel.add(m_ftx_testGrid);
		gPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		gPanel.add(Box.createHorizontalGlue());
		gPanel.add(createButton("Show Grid", BTN_TST_GRID));
		
		// Overall arrangement
		Box panel = new Box(BoxLayout.PAGE_AXIS);
		tPanel.setAlignmentX(Box.LEFT_ALIGNMENT);
		gPanel.setAlignmentX(Box.LEFT_ALIGNMENT);
		
		panel.add(tPanel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(gPanel);
		panel.add(Box.createVerticalStrut(5));
		
		return panel;
	}


	/**
	 * Builds the save panel of the UI.
	 * @return the composed panel.
	 */
	private JComponent initSavePanel()
	{
		// Make labels all the same width
		JLabel cameraTypeLabel = createLabel("Type:");
		JLabel cameraLocLabel = createLabel("Location:");
		JLabel resolutionLabel = createLabel("Resolution:");
		JLabel descrLabel = createLabel("Description:");
		JLabel unitLabel = createLabel("Base Unit:");
		JLabel heightLabel = createLabel("Height:");
		Dimension labelSize = cameraTypeLabel.getPreferredSize();
		labelSize.width = Math.max(labelSize.width,
						  Math.max(cameraLocLabel.getPreferredSize().width, 
						  Math.max(resolutionLabel.getPreferredSize().width,
						  Math.max(descrLabel.getPreferredSize().width,
						  Math.max(unitLabel.getPreferredSize().width, 
								   heightLabel.getPreferredSize().width)))));
		cameraTypeLabel.setPreferredSize(labelSize);
		cameraTypeLabel.setMinimumSize(labelSize);
		cameraLocLabel.setPreferredSize(labelSize); 
		cameraLocLabel.setMinimumSize(labelSize); 
		resolutionLabel.setPreferredSize(labelSize);
		resolutionLabel.setMinimumSize(labelSize);
		descrLabel.setPreferredSize(labelSize);
		descrLabel.setMinimumSize(labelSize);
		unitLabel.setPreferredSize(labelSize);
		unitLabel.setMinimumSize(labelSize);
		heightLabel.setPreferredSize(labelSize);
		heightLabel.setMinimumSize(labelSize);
		
		/// Camera panel
		// Create components
		Box cPanel = new Box(BoxLayout.PAGE_AXIS);
	
cPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Camera"), BorderFactory.createEmptyBorder(0,5,5,5)));
		
		m_txt_cameraType = new JTextField();
		m_txt_cameraLoc = new JTextField();
		m_ftx_resolution = new JFormattedTextField(new DefaultFormatter());  // No formatter
		m_ftx_resolution.setFocusLostBehavior(JFormattedTextField.REVERT);	// Don't allow editing
		JButton resolutionButton = createButton("Get", BTN_LS_RESOLUTION);
		
		// Prevent text fields from scaling vertically
		Dimension fieldSize = m_txt_cameraType.getPreferredSize();
		fieldSize.width = Integer.MAX_VALUE;
		m_txt_cameraType.setMaximumSize(fieldSize);
		m_txt_cameraLoc.setMaximumSize(fieldSize);
		m_ftx_resolution.setColumns(8);
		m_ftx_resolution.setMaximumSize(m_ftx_resolution.getPreferredSize());
				
		// Arrange the components
		Box c1Panel = new Box(BoxLayout.LINE_AXIS);
		c1Panel.add(cameraTypeLabel);
		c1Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		c1Panel.add(m_txt_cameraType);
		cPanel.add(c1Panel);
		cPanel.add(Box.createVerticalStrut(5));
		
		Box c2Panel = new Box(BoxLayout.LINE_AXIS);
		c2Panel.add(cameraLocLabel);
		c2Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		c2Panel.add(m_txt_cameraLoc);
		cPanel.add(c2Panel);
		cPanel.add(Box.createVerticalStrut(5));

		Box c3Panel = new Box(BoxLayout.LINE_AXIS);
		c3Panel.add(resolutionLabel);
		c3Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		c3Panel.add(m_ftx_resolution);
		c3Panel.add(Box.createRigidArea(new Dimension(10, 0)));
		c3Panel.add(Box.createHorizontalGlue());
		c3Panel.add(resolutionButton);
		cPanel.add(c3Panel);
		
		/// Scene coordinate system panel
		Box scPanel = new Box(BoxLayout.PAGE_AXIS);
		scPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Coordinate System"), BorderFactory.createEmptyBorder(0,5,5,5)));
		
		// Create & format components
		m_txt_coordDescr = new JTextField();
		m_txt_coordDescr.setMaximumSize(fieldSize);
		m_txt_coordUnit = new JTextField();
		m_txt_coordUnit.setColumns(8);
		m_txt_coordUnit.setMaximumSize(m_txt_coordUnit.getPreferredSize());
		m_txt_coordHeight = new JTextField();
		m_txt_coordHeight.setMaximumSize(fieldSize);
		
		// Arrange the components
		Box sc1Panel = new Box(BoxLayout.LINE_AXIS);
		sc1Panel.add(descrLabel);
		sc1Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		sc1Panel.add(m_txt_coordDescr);
		scPanel.add(sc1Panel);
		scPanel.add(Box.createVerticalStrut(5));
		
		Box sc2Panel = new Box(BoxLayout.LINE_AXIS);
		sc2Panel.add(unitLabel);
		sc2Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		sc2Panel.add(m_txt_coordUnit);
		sc2Panel.add(Box.createHorizontalGlue());
		scPanel.add(sc2Panel);
		scPanel.add(Box.createVerticalStrut(5));
		
		Box sc3Panel = new Box(BoxLayout.LINE_AXIS);
		sc3Panel.add(heightLabel);
		sc3Panel.add(Box.createRigidArea(new Dimension(5, 0)));
		sc3Panel.add(m_txt_coordHeight);
		scPanel.add(sc3Panel);
		scPanel.add(Box.createVerticalStrut(5));
		
		
		// Load&Save panel
		Box sPanel = new Box(BoxLayout.LINE_AXIS);
		sPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Storage"), BorderFactory.createEmptyBorder(0,5,5,5)));
		
		sPanel.add(Box.createHorizontalGlue());
		sPanel.add(createButton("Load", BTN_LS_LOAD));
		sPanel.add(Box.createRigidArea(new Dimension(5,0)));
		sPanel.add(createButton("Save", BTN_LS_SAVE));
		
		// Overall arrangement
		Box panel = new Box(BoxLayout.PAGE_AXIS);
		
		panel.add(cPanel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(scPanel);
		panel.add(Box.createVerticalStrut(5));
		panel.add(sPanel);
		panel.add(Box.createVerticalGlue());
		
		return panel;
	}

	//------------------------------------------------------------------------------------------------
	// Methods to create components with desired properties
	
	/**
	 * Creates a <code>JLabel</code> that won't change in size. Labels with HTML contend seem to 
	 * wrap around and have infinite maximum size if just created with <code>new</code>
.	 */
	private JLabel createLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setMinimumSize(label.getPreferredSize());
		label.setMaximumSize(label.getPreferredSize());
		return label;
	}
	
	/**
	 * Creates a <code>JFormattedTextField</code> with a listener for changes in the value property.
	 */
	private JFormattedTextField createTextField(int preferredColumns, Object value, int maxFractionDigits)
	{
		// Create and format field
		JFormattedTextField field = new JFormattedTextField(new NumberOrNullFormatter(maxFractionDigits));
		field.setValue(value);
		field.setColumns(preferredColumns);
		
		// Add change listener
		field.addPropertyChangeListener("value", m_eventListener);
		
		return field;
	}
	
	/**
	 * Creates a <code>JFormattedTextField</code> that won't change in height and only allows a 
	 * certain variation in width. Very useful for a <code>BoxLayout</code> (which respects maximum
	 * and minimum sizes).
	 */
	private JFormattedTextField createTextField(int minColumns, int maxColumns, Object value, int maxFractionDigits)
	{
		// Create the field
		JFormattedTextField field = createTextField(minColumns, value, maxFractionDigits);
		
		// Set size parameters (setColumns sets the preferred size)
		field.setMinimumSize(field.getPreferredSize());
		field.setColumns(maxColumns);
		field.setMaximumSize(field.getPreferredSize());
		
		return field;
	}
	
	/**
	 * Creates a <code>JButton</code> whith <code>m_eventListener</code> as 
	 * <code>actionListener</code>.
	 * @param text the text the <code>JButton</code> is labeled with.
	 * @param actionCommand the action command associated with the button. This is used to distinguish
	 * the origin of <code>ActionEvents</code> fired.
	 */
	private JButton createButton(String text, String actionCommand)
	{
		JButton b = new JButton(text);
		b.setActionCommand(actionCommand);
		b.addActionListener(m_eventListener);
		return b;
	}
	
	/**
	 * Creates a <code>JRadioButton</code> whith <code>m_eventListener</code> as 
	 * <code>actionListener</code>.
	 * @param text the text the JRadioButton is labeled with.
	 * @param actionCommand the action command associated with the button. This is used to distinguish
	 * the origin of <code>ActionEvents</code> fired.
	 */
	private JRadioButton createJRadioButton(String text, String actionCommand)
	{
		JRadioButton rb = new JRadioButton(text);
		rb.setActionCommand(actionCommand);
		rb.addActionListener(m_eventListener);
		return rb;
	}
	
	
	//------------------------------------------------------------------------------------------------
	// Actions
	
	
	/**
	 * <p>Nested class that handles all events that are dispatched by the components on this 
	 * <code>JFrame</code>. 
	 * <p>Events from buttons are usually very straight forward: they call methods of 
	 * <code>CameraProjection</code> and possibly wrap exceptions in nice messages for the user. The
	 * only notable actions are saving. This is also handled by <code>CameraProjection</code> but 
	 * only very few field values from the UI are explicitly stored in that object before the call 
	 * to {@link CameraProjection#save() save()}. This is because most fields are {@link 
	 * JFormattedTextField}s which save loads of effort. First of all they will reject invalid user 
	 * input (it will show and have a valid value once the user leaves the field), and secondly they
	 * they come with the very useful <code>PropertyChangeEvent</code>. So every time there is a new
	 * valid value, the event is triggered and the code propagates that value straight into the
	 * <code>CameraCalibration</code>.
	 */
	private class EventListenerImpl implements ActionListener, ChangeListener, PropertyChangeListener
	{
		/**
		 * Called when the user selects a different tab of the <code>JTabbedPane</code>. It affects 
		 * the set selection mode of the associated {@link CalibrationDataInputInterface}.
		 */
		public void stateChanged(ChangeEvent e)
		{
			// Reset CalibrationDataInputInterface if necessary
			if (m_dataInputPainted)
				m_dataInput.reset();
			
			// State change of the tabbed pane
			JTabbedPane tab = (JTabbedPane) e.getSource();
			switch (tab.getSelectedIndex())
			{
				// Optical axis
				case 0:
					m_dataInput.setSelectionSetMode(CalibrationDataInputInterface.SELECT_POINTS_ON_LINE);
					break;
				
				// Focal Length
				case 1:
					m_dataInput.setSelectionSetMode(CalibrationDataInputInterface.SELECT_POINTS_ON_LINE);
					break;
				
				// Vanishing Point
				case 2:
					m_dataInput.setSelectionSetMode(CalibrationDataInputInterface.SELECT_PARALLEL_LINES);
					break;
				
				// Others
				default:
					m_dataInput.setSelectionSetMode(CalibrationDataInputInterface.SELECT_POINTS);
					break;
			}
		}
		

		/**
		 * Called when the value property of one of the formatted text fields changes. Updates the 
		 * appropriate values in the associated {@link CameraProjection} object. 
		 */
		public void propertyChange(PropertyChangeEvent event)
		{
			// Setting the value to null while it is null fire this event, so just ignore that
			if (event.getOldValue() == null && event.getNewValue() == null)
				return;
			
			// Get the source
			JFormattedTextField source = (JFormattedTextField) event.getSource();
			m_log.finest("Value change from " + (event.getOldValue()==null ? "null" : event.getOldValue()) + " to "+ (event.getNewValue()==null ? "null" : event.getNewValue()));
			
			try
			{
				// Optical axis
				if (source == m_ftx_optAxisX || source == m_ftx_optAxisY)
					m_core.setOpticalAxis(new Point2D.Double(((Number) m_ftx_optAxisX.getValue()).doubleValue(), ((Number) m_ftx_optAxisY.getValue()).doubleValue()));
			
				// Focal length
				else if (source == m_ftx_focalLength)
					m_core.setFocalLength(((Number) source.getValue()).doubleValue());
				
				// Horizontal vanishing point
				else if (source == m_ftx_vanishPtHorX || source == m_ftx_vanishPtHorY)
					m_core.setVanishingPoint(m_core.VP_HORIZONTAL, new Point2D.Double(((Number) m_ftx_vanishPtHorX.getValue()).doubleValue(), ((Number) m_ftx_vanishPtHorY.getValue()).doubleValue()));
				
				// Vertical vanishing point
				else if (source == m_ftx_vanishPtVertX || source == m_ftx_vanishPtVertY)
					m_core.setVanishingPoint(m_core.VP_VERTICAL, new Point2D.Double(((Number) m_ftx_vanishPtVertX.getValue()).doubleValue(), ((Number) m_ftx_vanishPtVertY.getValue()).doubleValue()));
			}
			catch (NullPointerException e)
			{
				// One of the values that is required to store a parameter was invalid, so 
				// invalidate the parameter that should have been set.
				
				// Optical axis
				if (source == m_ftx_optAxisX || source == m_ftx_optAxisY)
					m_core.setOpticalAxis(null);
			
				// Focal length
				else if (source == m_ftx_focalLength)
					m_core.setFocalLength(Double.NaN);
				
				// Horizontal vanishing point
				else if (source == m_ftx_vanishPtHorX || source == m_ftx_vanishPtHorY)
					m_core.setVanishingPoint(m_core.VP_HORIZONTAL, null);
				
				// Vertical vanishing point
				else if (source == m_ftx_vanishPtVertX || source == m_ftx_vanishPtVertY)
					m_core.setVanishingPoint(m_core.VP_VERTICAL, null);
			}
			
			// Update the derived parameters if vanishing point has been changed
			if (source == m_ftx_vanishPtHorX || source == m_ftx_vanishPtHorY || source == m_ftx_vanishPtVertX || source == m_ftx_vanishPtVertY)
			{
				// Vanishing point distances
				m_ftx_vanishPtP.setValue(new Double(m_core.getVPDistanceInverse(m_core.VP_HORIZONTAL)));
				m_ftx_vanishPtQ.setValue(new Double(m_core.getVPDistanceInverse(m_core.VP_VERTICAL)));
						
				// Principal point
				Point2D principalPoint = nullToNaN(m_core.getPrincipalPoint());
				m_ftx_principalPtX.setValue(new Double(principalPoint.getX()));
				m_ftx_principalPtY.setValue(new Double(principalPoint.getY()));
			}
		}
		
		/**
		 * Method that is invoked on events from Buttons.
		 */
		public void actionPerformed(ActionEvent event)
		{
			// Action identifier
			String action = event.getActionCommand();
			
			
			// Using string constants for the action command allows to use == to compare equality, 
			// assuming that none of the JComponents makes a copy of the string (seems to be true).
			
			// Radio buttons for point selection mode
			if (action == BTN_PSM_DIRECT)
				m_dataInput.setPointSelectMode(CalibrationDataInputInterface.POINT_DIRECT);
			else if (action == BTN_PSM_EDGE)
				m_dataInput.setPointSelectMode(CalibrationDataInputInterface.POINT_EDGE);
				
			// Show curvature
			else if (action == BTN_OA_CURVATURE)
			{
				// Validate selection
				Vector points = m_dataInput.getPoints();
				if (checkPointCount(points, 3, "Show Curvature", "three points on a line that was straight in the scene"))
				{
					// Draw curvature mark
					CameraProjection.markCurvature((Point2D) points.get(0), (Point2D) points.get(2), (Point2D) points.get(1), m_dataInput.getGraphics());
					m_dataInputPainted = true;
					
					// Selected points are therewith used up
					m_dataInput.setPoints(null);
				}
			}
				
			// Select optical axis
			else if (action == BTN_OA_SELECT)
			{
				// Validate selection
				Vector points = m_dataInput.getPoints();
				if (checkPointCount(points, 1, "Select Optical Axis", "one point"))
				{
					// Write point coordinates into field. This triggers change events which store 
					// the optical axis also in the associated CameraProjection object.
					Point2D optAxis = (Point2D) points.get(0);
					m_ftx_optAxisX.setValue(new Double(optAxis.getX()));
					m_ftx_optAxisY.setValue(new Double(optAxis.getY()));
				}
			}
			
			// Find focal length
			else if (action == BTN_FL_FIND)
			{
				// Validate selection
				Vector points = m_dataInput.getPoints();
				if (checkPointCount(points, 3, "Show Curvature", "three points on a line that was straight in the scene"))
				{
					try
					{
						// Find focal length
						double focalLength = m_core.findFocalLength((Point2D) points.get(0), (Point2D) points.get(2), (Point2D) points.get(1));

						// Write into field
						m_ftx_focalLength.setValue(new Double(focalLength));
					}
					catch (IllegalStateException exception)
					{
						// No optical axis specified
						JOptionPane.showMessageDialog(CalibrationWindow.this,
									"The optical axis needs to be calibratied before the focal length can be found.",
									"Find Focal Length", JOptionPane.ERROR_MESSAGE);
					}
					catch (IllegalArgumentException exception)
					{
						// Invalid points selected
						JOptionPane.showMessageDialog(CalibrationWindow.this,
									"The three selected points don't seem to be on an originally straight line.\n" +
									"Alternatively the optical axis could be set incorrectly.",
									"Find Focal Length", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
			// Find vanishing point
			else if (action == BTN_HVP_FIND || action == BTN_VVP_FIND)
			{
				// Validate selection
				Vector points = m_dataInput.getPoints();
				if (checkPointCount(points, 4, "Show Curvature", "two lines that were parallel in the scene (i.e. four points)"))
				{
					try
					{
						// Find find vanishing point
						Point2D vanishPoint = m_core.findVanishingPoint(action==BTN_HVP_FIND ? m_core.VP_HORIZONTAL : m_core.VP_VERTICAL, (Point2D) points.get(0), (Point2D) points.get(1), (Point2D) points.get(2), (Point2D) points.get(3));

						// Write vanishing point into fields. This triggers change events so that 
						// the derived paramerters are also updated.
						if (action == BTN_HVP_FIND)
						{
							m_ftx_vanishPtHorX.setValue(new Double(vanishPoint.getX()));
							m_ftx_vanishPtHorY.setValue(new Double(vanishPoint.getY()));
						}
						else
						{
							m_ftx_vanishPtVertX.setValue(new Double(vanishPoint.getX()));
							m_ftx_vanishPtVertY.setValue(new Double(vanishPoint.getY()));
						}
					}
					catch (IllegalStateException exception)
					{
						// No optical axis specified
						JOptionPane.showMessageDialog(CalibrationWindow.this,
									"The optical axis and the focal length need to be calibratied before the vanishing points can be found.",
									"Find Vanishing Point", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
			// Scaling - select image point
			else if (action == BTN_SC_SELECT)
			{				
				// Validate selection
				Vector points = m_dataInput.getPoints();
				if (checkPointCount(points, 1, "Select Image Coordinates", "one point"))
				{
					// Write point coordinates into fields
					Point2D imCoord = (Point2D) points.get(0);
					m_ftx_scaleImX.setValue(new Double(imCoord.getX()));
					m_ftx_scaleImY.setValue(new Double(imCoord.getY()));
				}
			}
			
			// Scaling - add image/real-world coordinates pair
			else if (action == BTN_SC_ADD)
			{
				try
				{
					// Get field values
					double imX = ((Number) m_ftx_scaleImX.getValue()).doubleValue();
					double imY = ((Number) m_ftx_scaleImY.getValue()).doubleValue();
					double reX = m_ftx_scaleReX.getValue() == null ? Double.NaN : ((Number) m_ftx_scaleReX.getValue()).doubleValue();
					double reY = m_ftx_scaleReY.getValue() == null ? Double.NaN : ((Number) m_ftx_scaleReY.getValue()).doubleValue();
					
					// Add to scaling map
					m_lm_scaleMap.add(new Point2D.Double(imX, imY), new Point2D.Double(reX, reY));
					
					// Reset fields
					m_ftx_scaleImX.setValue(null);
					m_ftx_scaleImY.setValue(null);
					m_ftx_scaleReX.setValue(null);
					m_ftx_scaleReY.setValue(null);
				}
				catch (NullPointerException e)
				{
					// One of the image coordinates has not been specified
					JOptionPane.showMessageDialog(CalibrationWindow.this,
								"Please specify both image coordinates.", 
								"Add coordinate pair", JOptionPane.ERROR_MESSAGE);
				}
				catch (IllegalArgumentException e)
				{
					// Both real-world coordinates have not been specified
					JOptionPane.showMessageDialog(CalibrationWindow.this,
								"Please specify at least one of the real-world coordinates.", 
								"Add coordinate pair", JOptionPane.ERROR_MESSAGE);
				}
			}
			
			// Scaling - remove image/real-world coordinates pair
			else if (action == BTN_SC_REMOVE)
			{
				// Validate list selection
				int index = m_lst_scaleMap.getSelectedIndex();
				if (index != -1)
				{
					// Copy values into fields
					Point2D p = m_lm_scaleMap.getImageCoord(index);
					m_ftx_scaleImX.setValue(new Double(p.getX()));
					m_ftx_scaleImY.setValue(new Double(p.getY()));

					p = m_lm_scaleMap.getRealCoord(index);
					m_ftx_scaleReX.setValue(new Double(p.getX()));
					m_ftx_scaleReY.setValue(new Double(p.getY()));
					
					// Remove from list
					m_lm_scaleMap.remove(index);
				}
			}
			
			// Transform selected points
			else if (action == BTN_TST_TRANSFORM)
			{
				try
				{
					// Formatters for the double values
					NumberFormat twoDecimal = NumberFormat.getInstance();
					twoDecimal.setMaximumFractionDigits(2);
					NumberFormat fourDecimal = NumberFormat.getInstance();
					fourDecimal.setMaximumFractionDigits(2);
					
					// Transform all points
					Vector points = m_dataInput.getPoints();
					String out = "", separator = "";
					for (int i=0; i<points.size(); i++)
					{
						// Transform point
						Point2D iPoint = (Point2D) points.get(i);
						Point2D rPoint = m_core.transform(iPoint);
						out += separator + twoDecimal.format(iPoint.getX()) + ", " + twoDecimal.format(iPoint.getY()) + " is " + fourDecimal.format(rPoint.getX()) + ", " + fourDecimal.format(rPoint.getY());
						separator = ",\n";
					}
					
					// Write result to text field
					if (points.size() == 0)
						m_txa_transformed.setText("");
					else
						m_txa_transformed.setText(out + "\nin real world coordinates");
					
					// Remove points
					m_dataInput.setPoints(null);
				}
				catch (IllegalStateException e)
				{
					// Display error message
					JOptionPane.showMessageDialog(CalibrationWindow.this,
								e.getMessage() + " Please complete the missing calibration and try again.",
								"Transform selected points", JOptionPane.ERROR_MESSAGE);
				}
			}
			
			// Draw test grid
			else if (action == BTN_TST_GRID)
			{
				try
				{
					// Reset a previous grid
					if (m_dataInputPainted)
						m_dataInput.reset();
					
					// Validate field input
					Object size = m_ftx_testGrid.getValue();
					if (size == null || ((Number) size).doubleValue() <= 0)
					{
						// Display error message
						JOptionPane.showMessageDialog(CalibrationWindow.this,
									"Please specify the grid cell size (in real-world coordinate system scale).", 
									"Show grid", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					// Validate image
					Graphics g = m_dataInput.getGraphics();
					if (g == null)
					{
						// Display error message
						JOptionPane.showMessageDialog(CalibrationWindow.this,
									"Please load an image from the appropriate camera at the appropriate location.", 
									"Show grid", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					// Draw grid
					m_core.drawGrid(((Number) size).doubleValue(), g);
					m_dataInput.repaint();
					m_dataInputPainted = true;
					
				}
				catch (IllegalStateException e)
				{
					// Display error message
					JOptionPane.showMessageDialog(CalibrationWindow.this,
								e.getMessage() + " Please complete the missing calibration and try again.",
								"Show grid", JOptionPane.ERROR_MESSAGE);
				}
			}
			
			// Get resolution
			else if (action == BTN_LS_RESOLUTION)
			{
				// Get the resolution from the data input
				Point res = m_dataInput.getImageResolution();
				
				if (res == null)
				{
					// Error message
					JOptionPane.showMessageDialog(CalibrationWindow.this,
								"Please load an image from the camera and try again.",
								"Get resolution", JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					// Store in CameraProjection
					m_core.setResolution(res.x, res.y);
					
					// Display
					m_ftx_resolution.setValue(res.x + "x" + res.y);
				}
			}
			
			// Load
			else if (action == BTN_LS_LOAD)
			{
				try
				{
					// Reset previous selections
					m_fileChooser.setSelectedFile(null);
					
					// Show the file chooser dialog
					if (m_fileChooser.showOpenDialog(CalibrationWindow.this) == JFileChooser.APPROVE_OPTION)
					{
						// Confirm reverting if loading the same file again
						File selected = m_fileChooser.getSelectedFile();
						if (selected.equals(m_fromFile))
							if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(CalibrationWindow.this, 
										"Revert calibration parameters from file '"+ selected +"'?", 
										"Load", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
								return;
						
						
						// Restore the settings into m_core
						Properties prop = new Properties();
						prop.load(new FileInputStream(selected));
						m_core.load(prop);
						
						// Remember file
						m_fromFile = selected;
						
						/// Update text fields. This causes a lot of property change events
						// which then re-update m_core. However these events will only be 
						// dispatched after this method terminates because there is only one 
						// event dispatcher thread. By then the fields will mirror the 
						// parameters in m_core so the events will have no effect.
						
						// Optical axis
						Point2D p = nullToNaN(m_core.getOpticalAxis());
						m_ftx_optAxisX.setValue(new Double(p.getX()));
						m_ftx_optAxisY.setValue(new Double(p.getY()));
						
						// Focal lenght
						m_ftx_focalLength.setValue(new Double(m_core.getFocalLength()));
						
						// Vanishing points
						p = nullToNaN(m_core.getVanishingPoint(m_core.VP_HORIZONTAL));
						m_ftx_vanishPtHorX.setValue(new Double(p.getX()));
						m_ftx_vanishPtHorY.setValue(new Double(p.getY()));
						p = nullToNaN(m_core.getVanishingPoint(m_core.VP_VERTICAL));
						m_ftx_vanishPtVertX.setValue(new Double(p.getX()));
						m_ftx_vanishPtVertY.setValue(new Double(p.getY()));
						
						// Descriptions
						m_txt_cameraType.setText(m_core.getCameraType());
						m_txt_cameraLoc.setText(m_core.getCameraLocation());
						m_txt_coordDescr.setText(m_core.getSceneCoordDescription());
						m_txt_coordUnit.setText(m_core.getSceneCoordUnit());
						m_txt_coordHeight.setText(m_core.getSceneCoordHeight());
						
						// Resolution
						Point res = m_core.getResolution();
						if (res.x*res.y == 0)
							m_ftx_resolution.setValue("");
						else
							m_ftx_resolution.setValue(res.x + "x" + res.y);
					}
				}
				catch (IOException e)
				{
					// Display and log error message
					m_log.warning("IOException while reading "+ m_fileChooser.getSelectedFile());
					JOptionPane.showMessageDialog(CalibrationWindow.this, 
								"IOException while reading '"+ m_fileChooser.getSelectedFile() +"'!", 
								"Load", JOptionPane.ERROR_MESSAGE);
				}
				catch (IllegalArgumentException e)
				{
					// Display error message
					JOptionPane.showMessageDialog(CalibrationWindow.this, 
								"Malformed unicode escape sequence in '"+ m_fileChooser.getSelectedFile() +"'",
								"Load", JOptionPane.ERROR_MESSAGE);
				}
			}
			
			// Save
			else if (action == BTN_LS_SAVE)
			{
				try
				{
					// Suggest the file the parameters were loaded from or last saved to
					if (m_fromFile != null)
						m_fileChooser.setSelectedFile(m_fromFile);
					
					// Show the file chooser dialog
					if (m_fileChooser.showSaveDialog(CalibrationWindow.this) == JFileChooser.APPROVE_OPTION)
					{	
						// Confirm overwriting if file exists (and is not the affiliated one)
						File selected = m_fileChooser.getSelectedFile();
						if (selected.exists() && ! selected.equals(m_fromFile))
							if (JOptionPane.NO_OPTION == JOptionPane.showConfirmDialog(CalibrationWindow.this, 
										"Overwrite existing file '"+ selected +"'?", 
										"Save", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE))
								return;
						
						// Write description into m_core (all other fields are updated automatically)
						m_core.setCameraType(m_txt_cameraType.getText());
						m_core.setCameraLocation(m_txt_cameraLoc.getText());
						m_core.setSceneCoordDescription(m_txt_coordDescr.getText());
						m_core.setSceneCoordUnit(m_txt_coordUnit.getText());
						m_core.setSceneCoordHeight(m_txt_coordHeight.getText());
						
						// Save parameters
						Properties prop = m_core.save();
						prop.store(new FileOutputStream(selected), "Camera calibration settings for NRS2 camera tracker");
						
						// Remember file
						m_fromFile = selected;
					}
				}
				catch (IOException e)
				{
					m_log.warning("IOException while writing "+ m_fileChooser.getSelectedFile());
				}
			}
		}


		/**
		 * Method to check that the user has selected the right number of points.
		 *
		 * @param points a Vector of {@link Point2D} objects, representing the selected points.
		 * @param expectedCount the expected number of points. 
		 * @param action the action that requires that number of points. This String will be displayed
		 * as the title of the dialog that may be displayed.
		 * @param expectationMsg the message to complete the sentence: "Please select ... ."
		 *
		 * @return <code>true</code> if either the right number of points was selected or more
		 * points were selected and the user did not choose "cancel".
		 */
		private boolean checkPointCount(Vector points, int expectedCount, String action, String expectationMsg)
		{
			// Simple case: right number of points
			if (points.size() == expectedCount)
				return true;
			
			// Not enough points
			if (points.size() < expectedCount)
			{
				JOptionPane.showMessageDialog(CalibrationWindow.this, 
							"Insufficient number of points selected. Please select "+expectationMsg+" and try again.",
							action,	JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			// Too many points -> ask whether to continue anyway
			return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(CalibrationWindow.this,
						"More points than necessary were selected. This action only requires "+expectationMsg+".\n" +
						"If you choose 'OK' only the first "+expectedCount+" points will be used.",
						action, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}

		/**
		 * Replaces a <code>null</code> value to a <code>Point2D.Double</code> instance with x and y 
		 * set to <code>Double.NaN</code>. If the argument is not <code>null</code> the method 
		 * returns the original object. The method allows more convenient handling of methods with 
		 * return type <code>Point2D</code>, like several methods of the <code>CameraProjection</code>
		 * class. 
		 * @param p a <code>Point2D</code> instance or <code>null</code>.
		 * @return a <code>Point2D</code> instance, possibly with <code>Double.NaN</code> coordinates.
		 */
		private Point2D nullToNaN(Point2D p)
		{
			return p==null ? new Point2D.Double(Double.NaN, Double.NaN) : p;
		}
	}
	
	
	//------------------------------------------------------------------------------------------------
	// Class to format and parse numbers in the formatted text fields, allowing null values
	
	/**
	 * <p>The formatter used for the formatted text fields in the {@link CalibrationWindow}. The 
	 * problem with other number formatters is, that they do not allow the option to enter 
	 * <code>null</code>. However this is necessary for the real-world coordinate text fields on the 
	 * 'Scaling' panel where it is allowed to enter nothing for x or y. This formatter parses an 
	 * empty string as <code>null</code>.
	 * <p>The constructor conveniently allows to set the maximum number of fraction decimals. 
	 * However the parsing result is always wrapped in a <code>Double</code>, unless it is 
	 * <code>null</code>.
	 */
	private class NumberOrNullFormatter extends JFormattedTextField.AbstractFormatter
	{
		/** Format used to parse and display non-null values. */
		NumberFormat formatter;
		
		/**
		 * Create a formatter that for integer values.
		 */
		public NumberOrNullFormatter()
		{
			// Set up the Format object
			formatter = NumberFormat.getInstance();
			formatter.setMinimumFractionDigits(0);
			formatter.setMaximumFractionDigits(0);
			formatter.setParseIntegerOnly(true);
			formatter.setGroupingUsed(false);
		}
		
		/**
		 * Create a formatter for decimal values. 
		 * @param fractionDigits the maximum number of fraction digits to be displayed.
		 */
		public NumberOrNullFormatter(int fractionDigits)
		{
			// Set up the Format object
			formatter = NumberFormat.getInstance();
			formatter.setMinimumFractionDigits(0);
			formatter.setMaximumFractionDigits(fractionDigits);
			formatter.setGroupingUsed(false);
		}
		
		/**
		 * Parses the specified text. 
		 * @param text String to convert.
		 * @return <code>null</code> if the string is "" or Double otherwise.
		 */
		public Object stringToValue(String text) throws ParseException
		{
			// Return null if the string is empty
			if (text.equals(""))
				return null;
			
			// Use the Format object
			Number n = formatter.parse(text);
			return n instanceof Double ? n : new Double(n.doubleValue());
		}
		
		/**
		 * Returns a string with the formatted <code>value</code>.
		 * @param value the object to be formatted.
		 * @return String representation of value.
		 */
		public String valueToString(Object value) throws ParseException
		{
			// Return empty string if object is null or NaN
			if (value == null)
				return "";
			if (value instanceof Double && ((Double) value).isNaN())
					return "";
			if (value instanceof Float && ((Float) value).isNaN())
					return "";
			
			// Use Format object if a Number
			if (value instanceof Number)
				return formatter.format(((Number) value).doubleValue());
			else
				throw new ParseException("Illegal value. Must be a subclass of Number.", 0);
		}
	}
}



