/*
 * Copyright (C) 2004 Ben Torben-Nielsen
 *
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation; either version 2 of
 *    the License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public
 *    License along with this program; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307 USA
 *
 * For further information in the first instance contact:
 * Richard Reeve <richardr@inf.ed.ac.uk>
 *
 */
package nrs.audio.simulator ;

import nrs.audio.simulator.* ;
import java.io.* ;
import java.awt.*;
import java.awt.image.* ;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.* ;
import javax.swing.filechooser.* ;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.geom.* ;

/**
 * Directional hearing simulator. Simplified mathemetical simulation of the
 * directionaility in the ensiferan ear.
 *
 * @author Ben Torben-Nielsen
 **/
public class DSFrame extends JFrame
{
    private static final String TITLE = "Directional Hearing Simulator -- BTN" ;
    private static final int	WIDTH = 850,
	HEIGTH = 650 ;
    
    private static final int SIGNAL_AMPLIFICATION = 100 ;
    private static final int SPEED_OF_SOUND = 344000 ;
    private static final int OFFSET = 30;

    private static final Color magenta_t =
	new Color( Color.magenta.getRed(),
		   Color.magenta.getGreen(),
		   Color.magenta.getBlue(), 128 );
    private static final Color cyan_t =
	new Color( Color.cyan.getRed(),
		    Color.cyan.getGreen(),
		    Color.cyan.getBlue(), 128 );
    
    private JFrame frame ;	
    private Container contentPane ;
    
    private DSDirPanel dirPanel ;
    private JPanel earControlPanel ;
    private JPanel simSettingsPanel ;
    private JPanel controlPanel ;
    
    private JTextField	corField,
	freqField;
    
    private JButton 	runButton,
	saveButton ;
    
    private JCheckBox 	lockButton,
	membraneButton ;
    
    private JLabel	isitLabel,
	csitLabel,
	csctLabel,
	isctLabel ;	
    
    private double gain1, gain2, gain3, delay1, delay2, delay3 ;
    
    private JCheckBoxMenuItem 	viewLeftItem,
	viewRightItem,
	viewLRefItem,
	viewRRefItem,
	viewLMMItem,
	viewRMMItem,
	viewWinnerItem,
	autoBox ;
    
    private JSpinner freqSpinner, 	distSpinner ;
    private SpinnerNumberModel freqModel ;
    private SpinnerNumberModel distModel ;
    private int frequency ;
    
    private boolean autoSave = false ;
    
    /*
     * Nice Windows extra...
     */
    static
    {
	try 
	    {
		UIManager.setLookAndFeel( UIManager.
					  getSystemLookAndFeelClassName() );
	    }
	catch(Exception e)
	    {
	    }
    }
    
    public DSFrame()
    {
	super() ;
	
	setTitle(TITLE) ;
	setSize(WIDTH,HEIGTH) ;
	
	contentPane = getContentPane() ;
	
	
	addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent e)
		{
		    System.exit(0) ;
		}
	    });
	
	/*
	 * Add the contents to the frame
	 */
	setJMenuBar(getMenuBarr()) ;
	
	// EXPERIMENTAL
	JSplitPane sPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true) ;
	sPane.setDividerLocation(600) ;
	sPane.add(getDirPanel()) ;
	sPane.add(getControlPanel()) ;
	contentPane.add(sPane) ;
	
	frame = this ;
	
	show() ;	
    }
    
    /**
     * Return the menubar
     **/
    private JMenuBar getMenuBarr()
    {
	JMenuBar bar = new JMenuBar() ;
	MyMenuListener mlistener = new MyMenuListener() ;
	
	JMenu fileMenu = new JMenu("File") ;
	JMenuItem exitItem = new JMenuItem("Exit") ;
	exitItem.setName("exit") ;
	exitItem.addActionListener(mlistener) ;
	
	JMenuItem printItem = new JMenuItem("Save data") ;
	printItem.setName("data") ;
	printItem.addActionListener(mlistener) ;
	
	JMenu saveMenu = new JMenu("Export result") ;
	JMenuItem pngItem = new JMenuItem("PNG") ;
	pngItem.setName("pngItem") ;
	pngItem.addActionListener(mlistener) ;
	JMenuItem svgItem = new JMenuItem("SVG") ;
	svgItem.setName("svgItem") ;
	svgItem.addActionListener(mlistener) ;
	saveMenu.add(pngItem) ;
	saveMenu.add(svgItem) ;
	
	autoBox = new JCheckBoxMenuItem("Auto save (SVG)") ;
	autoBox.setName("auto") ;
	autoBox.addActionListener(mlistener) ;
	
	fileMenu.add(saveMenu) ;
	fileMenu.add(printItem) ;
	fileMenu.addSeparator() ;
	fileMenu.add(autoBox) ;
	fileMenu.addSeparator() ;
	fileMenu.add(exitItem) ;
	
	JMenu earMenu = new JMenu("Ears") ;
	
	JMenuItem loadItem = new JMenuItem("Load") ;
	loadItem.setName("load") ;
	loadItem.addActionListener(mlistener) ;			
	
	JMenuItem saveItem = new JMenuItem("Save") ;
	saveItem.setName("save") ;
	saveItem.addActionListener(mlistener) ;	
	
	earMenu.add(loadItem) ;
	earMenu.add(saveItem) ;
	
	JMenu viewMenu = new JMenu("View") ;
	
	viewLeftItem = new JCheckBoxMenuItem("Left ear") ;
	viewLeftItem.setName("leftear") ;
	viewLeftItem.setState(true) ;
	viewLeftItem.addActionListener(mlistener) ;
	
	viewRightItem = new JCheckBoxMenuItem("Right ear") ;
	viewRightItem.setName("rightear") ;
	viewRightItem.setState(true) ;
	viewRightItem.addActionListener(mlistener) ;
	
	viewLRefItem = new JCheckBoxMenuItem("Left reference") ;
	viewLRefItem.setName("lref") ;
	viewLRefItem.addActionListener(mlistener) ;
	
	viewRRefItem = new JCheckBoxMenuItem("Right reference") ;
	viewRRefItem.setName("rref") ;
	viewRRefItem.addActionListener(mlistener) ;	
	
	viewLMMItem = new JCheckBoxMenuItem("Left min max") ;
	viewLMMItem.setName("lmm") ;
	viewLMMItem.addActionListener(mlistener) ;
	
	viewRMMItem = new JCheckBoxMenuItem("Right min max") ;
	viewRMMItem.setName("rmm") ;
	viewRMMItem.addActionListener(mlistener) ;
	
	viewWinnerItem = new JCheckBoxMenuItem("Winning direction") ;
	viewWinnerItem.setName("winner") ;
	viewWinnerItem.addActionListener(mlistener) ;
	
	viewMenu.add(viewLeftItem) ;
	viewMenu.add(viewRightItem) ;
	viewMenu.addSeparator() ;
	viewMenu.add(viewLRefItem) ;
	viewMenu.add(viewRRefItem) ;
	viewMenu.addSeparator() ;
	viewMenu.add(viewLMMItem) ;
	viewMenu.add(viewRMMItem) ;
	viewMenu.addSeparator() ;
	viewMenu.add(viewWinnerItem);
	
	JMenu helpMenu = new JMenu("Help") ;
	JMenuItem aboutItem = new JMenuItem("About") ;
	aboutItem.setName("about") ;
	aboutItem.addActionListener(mlistener) ;
	JMenuItem helpItem = new JMenuItem("Help") ;
	helpItem.setName("help") ;
	helpItem.addActionListener(mlistener) ;	
	
	helpMenu.add(aboutItem)	 ;
	helpMenu.addSeparator() ;
	helpMenu.add(helpItem) ;
	
	bar.add(fileMenu) ;
	bar.add(viewMenu) ;
	bar.add(earMenu) ;
	bar.add(helpMenu) ;
	
	return bar ;	
    }
    
    private JPanel getDirPanel()
    {
	JPanel previewPanel = new JPanel(new GridLayout(1,1)) ;
	previewPanel.
	    setBorder( new TitledBorder( new EtchedBorder(),
					 "Resulting directional pattern" ) );
	
	dirPanel = new DSDirPanel() ;
	previewPanel.add(dirPanel) ;
	
	return previewPanel ;		
    }
    
    private JSplitPane getControlPanel()
    {
	JSplitPane controlPane =
	    new JSplitPane( JSplitPane.VERTICAL_SPLIT, true );
	
	controlPane.setDividerLocation(320) ;
	
	earComponent = new DSEarComponent(this) ;
	controlPane.add(earComponent) ;
	
	controlPane.add(getSimSettingsPanel()) ;
	
	return controlPane ;
    }
    
    private DSEarComponent earComponent ;
    
    private JPanel getSimSettingsPanel()
    {
	simSettingsPanel = new JPanel(new BorderLayout()) ;
	simSettingsPanel.setBorder( new TitledBorder( new EtchedBorder(),
						      "Settings" ) );
	
	JPanel sp = new JPanel(new GridLayout(0,2,5,5));
	sp.setBorder(new EmptyBorder(0,5,5,5));	
	
	sp.add(new JLabel("distance IS -> IT: ")) ;
	isitLabel = new JLabel("") ;
	sp.add(isitLabel) ;
	csitLabel = new JLabel("") ;
	sp.add(new JLabel("distance CS -> IT: ")) ;
	sp.add(csitLabel) ;
	sp.add(new JLabel("distance CS -> CT: ")) ;
	csctLabel = new JLabel("") ;
	sp.add(csctLabel) ;
	sp.add(new JLabel("distance IS -> CT: ")) ;
	isctLabel = new JLabel("") ;
	sp.add(isctLabel) ;
	earComponent.setLabels(isitLabel,csitLabel, csctLabel,isctLabel) ;
	
	JLabel corLabel = new JLabel("CoR:");
	corLabel.setToolTipText("Centre of Rotation");
	corField = new JTextField(3) ;
	corField.setEditable(false) ;
	corField.setText("double click") ;	
	sp.add(corLabel) ;
	sp.add(corField) ;
	
	JLabel freqLabel = new JLabel("Frequency:");
	freqLabel.setToolTipText("The carrier frequency (in Hz)");
	
	freqModel = new SpinnerNumberModel( 4700, 1, 50000, 100 ) ;
	frequency = freqModel.getNumber().intValue();
	freqSpinner = new JSpinner(freqModel) ;
	freqSpinner.
	    addChangeListener( new ChangeListener()
		{
		    public void stateChanged(ChangeEvent ce)
		    {
			frequency = freqModel.getNumber().intValue();
			//runSimulation() ;	
			frame.repaint() ;
		    }	
		}
			       );
	
	sp.add(freqLabel) ;
	sp.add(freqSpinner) ;
	
	JLabel distanceLabel = new JLabel("Distance:");
	distanceLabel.
	    setToolTipText("Distance between the CoR and the speaker (in mm)");

	distModel = new SpinnerNumberModel(400,0,10000,5) ;
	distance = distModel.getNumber().intValue();
	distSpinner = new JSpinner(distModel) ;
	distSpinner.
	    addChangeListener( new ChangeListener()
		{
		    public void stateChanged(ChangeEvent ce)
		    {
			distance = distModel.getNumber().intValue();
			//runSimulation() ;	
			frame.repaint() ;
		    }	
		}
			       );
	sp.add(distanceLabel) ;
	sp.add(distSpinner) ;	
	
	runButton = new JButton("Run") ;
	runButton.
	    addActionListener( new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e) 
		    {
			runSimulation() ;
			dirPanel.repaint() ;
		    }
		}
			       );	
	
	saveButton = new JButton("Save") ;
	saveButton.
	    addActionListener( new ActionListener() 
		{
		    public void actionPerformed(ActionEvent e) 
		    {
			Rectangle r = dirPanel.getBounds() ;
			saveImage(r) ;	
		    }
		}
			       );	
	
	sp.add(runButton) ;	
	//sp.add(saveButton) ;		
	
	simSettingsPanel.add(sp,BorderLayout.NORTH);
	
	return simSettingsPanel ;
    }
    
    public static BufferedImage bi ;
    
    public void saveImage(Rectangle r)
    {
	dirPanel.saveImageAsPNG() ;	
	dirPanel.saveImageAsSVG() ;		
    }
    
    private Point2D.Double is  ;
    private Point2D.Double cs  ;
    private Point2D.Double it  ;
    private Point2D.Double ct  ;
    private Point2D.Double cor  ; 
    private double distance = 0 ;
    private double[] gains, delays ; 	// "inner-ear" delays (angles)
    
    private double[] oDelays ;			// outside ear delays (angles)
    
    private int k = 1 ;
    
    public void runSimulation()
    {
	fetchDataFromGUI() ;	// retrieve information from the gui
	
	// construct the circle around the CoR
	Ellipse2D.Double observations =
	    new Ellipse2D.Double(cor.getX()-distance,
				 cor.getY()-distance,
				 distance*2, distance*2 );
	
	double[] d360_R = new double[360] ;
	double[] d360_L = new double[360] ;
	
	double waveLength = SPEED_OF_SOUND / freqModel.getNumber().intValue() ;
	//Integer.parseInt(freqField.getText()) ;
	//System.out.println("waveLength: " + waveLength) ;
	
	double average = 0 ;
	
	for(int i = 0 ; i < 360 ; i++)
	    {
		double adj = Math.cos(Math.toRadians(-i)) * distance ;
		double opp = Math.sin(Math.toRadians(-i)) * distance ; 

		// loud speaker position
		Point2D.Double ls = new Point2D.Double(cor.getX() + adj,
						       cor.getY() + opp) ;
		
		double dlsit =
		    Math.sqrt( Math.pow( (ls.getX() - it.getX()) ,2) +
			       Math.pow( (ls.getY() - it.getY()),2) );	
		double dlsis =
		    Math.sqrt( Math.pow( (ls.getX() - is.getX()) ,2) +
			       Math.pow( (ls.getY() - is.getY()),2) ) ;	
		double dlscs =
		    Math.sqrt( Math.pow( (ls.getX() - cs.getX()) ,2) +
			       Math.pow( (ls.getY() - cs.getY()),2) ) ;
		double dlsct =
		    Math.sqrt( Math.pow( (ls.getX() - ct.getX()) ,2) +
			       Math.pow( (ls.getY() - ct.getY()),2) ) ;	
		
		oDelays = new double[4] ;
		oDelays[0] = (dlsit*360) / waveLength;
		oDelays[1] = (dlsis*360) / waveLength;
		oDelays[2] = (dlscs*360) / waveLength;
		oDelays[3] = (dlsct*360) / waveLength;
		
		/*
		 * COMPUTE THE LEFT EAR
		 */ 
		double v = Math.pow( gains[0] *
				     Math.cos( Math.toRadians(oDelays[0]) -
					       Math.toRadians(delays[0]*freqModel.getNumber().intValue()*360/1000000) ) +
				     gains[1] *
				     Math.cos( Math.toRadians(oDelays[1]) -
					       Math.toRadians(delays[1]*freqModel.getNumber().intValue()*360/1000000 ) ) +
				     gains[2] *
				     Math.cos( Math.toRadians(oDelays[2]) -
					       Math.toRadians(delays[2]*freqModel.getNumber().intValue()*360/1000000 ) ),
				     2 ) +
		    Math.pow( gains[0] *
			      Math.sin( Math.toRadians(oDelays[0]) -
					Math.toRadians(delays[0]*freqModel.getNumber().intValue()*360/1000000 ) ) +
			      gains[1] *
			      Math.sin( Math.toRadians(oDelays[1]) -
					Math.toRadians(delays[1]*freqModel.getNumber().intValue()*360/1000000 ) ) +
			      gains[2] *
			      Math.sin( Math.toRadians(oDelays[2]) -
					Math.toRadians(delays[2]*freqModel.getNumber().intValue()*360/1000000 ) ),
			      2 );
		
		v = v /2 ;
		v = Math.sqrt(v);
		
		if(i < 0)
		    d360_L[i] = 0 ;
		else
		    d360_L[i] = v * SIGNAL_AMPLIFICATION ; // scale it up!
		
		// dB STUFF
		d360_L[i] = 20* Math.log(d360_L[i]) + OFFSET ; // add OFFSET dB (OFFSETdB is reference, 0-point)
		
		
		/*
		 * COMPUTE THE RIGHT EAR
		 */
		
		//	System.out.println("g1:" + gains[0] + ", " +gains[1] + ", " +gains[2]) ;
		//	System.out.println("g2:" + gains[3] + ", " +gains[4] + ", " +gains[5]) ;
		//	System.out.println("d1:" + delays[0] + ", " +delays[1] + ", " +delays[2]) ;
		//	System.out.println("d2:" + delays[3] + ", " +delays[4] + ", " +delays[5]) ;		 
		
		double v2 =
		    Math.pow( gains[3] *
			      Math.cos( Math.toRadians(oDelays[3]) -
					Math.toRadians(delays[3]*freqModel.getNumber().intValue()*360/1000000 ) ) +
			      gains[4] *
			      Math.cos( Math.toRadians(oDelays[2]) -
					Math.toRadians(delays[4]*freqModel.getNumber().intValue()*360/1000000 ) ) +
			      gains[5] *
			      Math.cos( Math.toRadians(oDelays[1]) -
					Math.toRadians(delays[5] *freqModel.getNumber().intValue()*360/1000000) ),
			      2) +
		    Math.pow( gains[3] *
			      Math.sin( Math.toRadians(oDelays[3]) -
					Math.toRadians(delays[3]*freqModel.getNumber().intValue()*360/1000000 ) ) +
			      gains[4] *
			      Math.sin( Math.toRadians(oDelays[2]) -
					Math.toRadians(delays[4]*freqModel.getNumber().intValue()*360/1000000 ) ) +
			      gains[5] *
			      Math.sin( Math.toRadians(oDelays[1]) -
					Math.toRadians(delays[5]*freqModel.getNumber().intValue()*360/1000000 ) ),
			     2);
		
		v2 = v2 /2 ;
		v2 = Math.sqrt(v2);			 
		
		d360_R[i] = v2 * SIGNAL_AMPLIFICATION ;
		
		// dB STUFF
		d360_R[i] = 20 * Math.log(d360_R[i])  + OFFSET;
	    }
	
	double ll = d360_L[90] - OFFSET;
	double lr = d360_R[90] - OFFSET;
	
	double offset = Math.min( ll, lr );

	double low = offset;
	for (int i = 0 ; i < 360 ; i++)
	    {
		low = Math.min( low, d360_L[i] - OFFSET );
		low = Math.min( low, d360_R[i] - OFFSET );
	    }

	for(int i = 0 ; i < 360 ; i++)
	    {
		d360_L[i] = d360_L[i] - low - 20;
		d360_R[i] = d360_R[i] - low - 20;
	    }
	
	dirPanel.setRightData(d360_R) ;
	dirPanel.setLeftData(d360_L) ;
	
	if(autoSave)
	    {
		long millis = new Date().getTime() ;
		String fileName = "ds_" + millis + "_"+frequency+".svg"	 ;
		dirPanel.saveImageAsSVG(fileName) ;
	    }
	
    }
    
    
    private void fetchDataFromGUI()
    {
        // retrieve data from earComponent
        try
	    {
	        is = earComponent.getISPosition() ;
	        cs = earComponent.getCSPosition() ;
	        it = earComponent.getITPosition() ;
	        ct = earComponent.getCTPosition() ;
	        cor = earComponent.getCoR() ;
	        //System.out.println("is: " + is) ;
	        //System.out.println("cs: " + cs) ;
	        //System.out.println("it: " + it) ;
	        //System.out.println("ct: " + ct) ;
	        //System.out.println("cor: " + cor) ;	
	    }
        catch(NullPointerException npe)
	    {
        	// no CoR set
        	JOptionPane.
		    showMessageDialog( this,
				       "No Center of Rotation (CoR) set.",
				       "Set CoR",
				       JOptionPane.ERROR_MESSAGE );
	    }
        
        try
	    {
        	gains = earComponent.getGains(DSEarComponent.BOTH) ;	
        	delays = earComponent.getDelays(DSEarComponent.BOTH) ;
		//System.out.println( frequency );
// 		for ( int i = 0; i < 6; i++ )
// 		    {
// 			delays[i] = delays[i] * 360 * frequency / 1000000;
// 			System.out.println( delays[ i ] );
// 		    }
	    }
        catch(Exception e)
	    {
        	JOptionPane.
		    showMessageDialog( this,
				       "Could not retrieve all ear details",
				       "Set ear details",
				       JOptionPane.ERROR_MESSAGE );
	    }		
    }
    
    private class MyMenuListener implements ActionListener
    {
	public void actionPerformed(ActionEvent e)
	{
	    Object source = e.getSource();
	    String name = ((JMenuItem)source).getName() ;
	    //System.out.println("name: " +name) ;
	    
	    if(name.equals("exit") )
		{
		    System.exit(0);	
		}
	    
	    if(name.equals("pngItem") )
		{
		    JFileChooser chooser = new JFileChooser();
		    chooser.
			addChoosableFileFilter( new DSEarFileFilter( DSEarFileFilter.PNG ) ) ;
		    
		    int returnVal = chooser.showSaveDialog(frame);
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) 
			{
			    dirPanel.
				saveImageAsPNG(chooser.getSelectedFile().
					       getName()) ;
			}
		}
	    
	    if(name.equals("svgItem") )
		{
		    JFileChooser chooser = new JFileChooser();
		    chooser.
			addChoosableFileFilter(new DSEarFileFilter(DSEarFileFilter.SVG)) ;
		    
		    int returnVal = chooser.showSaveDialog(frame);
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) 
			{
			    //System.out.println("save file to: " +chooser.getSelectedFile().getName()) ;
			    dirPanel.saveImageAsSVG(chooser.getSelectedFile().
						    getName()) ;
			}
		}
	    
	    if(name.equals("about"))
		{
		    String t = "Directional Hearing Simulator\n" +
			"Simulates the directional hearing in the cricket.\n" +
			"Ben Torben-Nielsen\n" +
			"2004" ;
		    JOptionPane.
			showMessageDialog(frame, t,
					  "About BenTaxx",
					  JOptionPane.INFORMATION_MESSAGE) ;
		}
	    
	    if(name.equals("help"))
		{
		    String t = "No \"online\" help for this program " +
			"(Read the documentation)" ;
		    JOptionPane.
			showMessageDialog(frame, t,"Help!",
					  JOptionPane.INFORMATION_MESSAGE) ;
		}
	    
	    if(name.equals("load"))
		{
		    JFileChooser chooser = new JFileChooser();
		    chooser.
			addChoosableFileFilter(new DSEarFileFilter(DSEarFileFilter.EAR)) ;
		    
		    int returnVal = chooser.showOpenDialog(frame);
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) 
			{
			    //System.out.println("You chose to open this file: " +
			    //chooser.getSelectedFile().getName());
			    
			    earComponent.loadSettings(chooser.getSelectedFile().getName()) ;
			}				
		}
	    
	    if(name.equals("save"))
		{
		    JFileChooser chooser = new JFileChooser();
		    chooser.
			addChoosableFileFilter(new DSEarFileFilter(DSEarFileFilter.EAR)) ;
		    int returnVal = chooser.showSaveDialog(frame);
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) 
			{
			    //System.out.println("You chose to save to this file: " +
			    //chooser.getSelectedFile().getName());
			    
			    earComponent.
				saveSettings(chooser.getSelectedFile().
					     getName()) ;
			}				
		}
	    if(name.equals("leftear"))
		{
		    if(viewLeftItem.getState() == true)
			{
			    dirPanel.setDrawLeftData(true) ;	
			}
		    else
			{
			    dirPanel.setDrawLeftData(false) ;
			}
		}
	    
	    if(name.equals("rightear"))
		{
		    if(viewRightItem.getState() == true)
			{
			    dirPanel.setDrawRightData(true) ;	
			}
		    else
			{	
			    dirPanel.setDrawRightData(false) ;	
			}		
		}
	    
	    if(name.equals("lref"))
		{
		    if(viewLRefItem.getState() == true)
			{
			    dirPanel.setDrawLeftRefCircle(true) ;	
			}
		    else
			{	
			    dirPanel.setDrawLeftRefCircle(false) ;	
			}				
		}
	    
	    if(name.equals("rref"))
		{
		    if(viewRRefItem.getState() == true)
			{
			    dirPanel.setDrawRightRefCircle(true) ;	
			}
		    else
			{	
			    dirPanel.setDrawRightRefCircle(false) ;	
			}				
		}
	    
	    if(name.equals("lmm"))
		{
		    if(viewLMMItem.getState() == true)
			{
			    dirPanel.setDrawLeftMinMax(true) ;	
			}
		    else
			{	
			    dirPanel.setDrawLeftMinMax(false) ;	
			}				
		}
	    
	    if(name.equals("rmm"))
		{
		    if(viewRMMItem.getState() == true)
			{
			    dirPanel.setDrawRightMinMax(true) ;	
			}
		    else
			{	
			    dirPanel.setDrawRightMinMax(false) ;	
			}				
		}
	    
	    if(name.equals("winner"))
		{
		    if(viewWinnerItem.getState() == true)
			{
			    dirPanel.setDrawWinnerCircle(true) ;	
			}
		    else
			{	
			    dirPanel.setDrawWinnerCircle(false) ;	
			}				
		}
	    
	    if(name.equals("auto"))
		{
		    if(autoBox.getState() == true)
			{
			    autoSave = true ;	
			}
		    else
			{	
			    autoSave = false ;	
			}				
		}
	    
	    if(name.equals("data"))
		{
		    // write output data to a file
		    
		    JFileChooser chooser = new JFileChooser();
		    chooser.
			addChoosableFileFilter(new DSEarFileFilter(DSEarFileFilter.TXT)) ;
		    
		    int returnVal = chooser.showSaveDialog(frame);
		    
		    if(returnVal == JFileChooser.APPROVE_OPTION) 
			{
			    //System.out.println("You chose to save to this file: " +
			    //chooser.getSelectedFile().getName());
			    
			    dirPanel.
				saveReport(chooser.getSelectedFile().
					   getName()) ;
			}				
		}
	    
	    // always repaint the dirPanel
	    dirPanel.repaint() ;
	}
    };
    
    //test
    public static void main(String[] cmdl)
    {
	DSFrame f = new DSFrame() ;	
    }	
}
