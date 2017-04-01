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
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.* ;

public class DSEarEdit extends JFrame implements Serializable
{
    private static final String TITLE = "Ear Details" ;
    private static final int	WIDTH = 200,
	HEIGHT = 375 ;
    
    private static boolean firstUse = true ;    
    private JFrame frame ;	
    private Container contentPane ;
    
    private JPanel earPanel ;
    
    private JButton saveButton,
	closeButton ;
    
    private JTextField 	gain1Field,
	gain2Field,
	gain3Field,
	delay1Field,
	delay2Field,
	delay3Field ;
    
    private DSEarComponent earComponent ;
    
    private double[] gains, delays, initialGains, initialDelays ;
    
    private JRadioButton 	leftButton, 
	rightButton,
	bothButton ;
    private ButtonGroup group ;
    private boolean left = true, 
	right = false, 
	both = false ;
    
    private int side ;
    
    private JPanel	sidePanel,
	delayPanel,
	gainPanel ;
    
    
    /*
     * Nice Windows extra...
     */
    static
    {
	try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
	catch(Exception e) { }
    }
    
    public DSEarEdit(DSEarComponent earComponent, double[] gains, double[] delays, int side)
    {
	super() ;
	frame = this ;
	this.earComponent = earComponent ;
	
	this.side = side ;
	this.gains = gains ;
	this.delays = delays ;
	
	setTitle(TITLE) ;
	setSize(WIDTH,HEIGHT) ;	
	
	addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent e)
		{
		    frame.setVisible(false) ;	
		}
	    });
	
	contentPane = getContentPane() ;
	contentPane.add(getSidePanel(), BorderLayout.NORTH) ;
	contentPane.add(getEarConfigPanel()) ;
	contentPane.add(getButtonPanel(), BorderLayout.SOUTH) ;	
	
	System.out.println("gains: (1)" +gains) ;
    }
    
    private JPanel getEarConfigPanel()
    {
	earPanel = new JPanel() ;
	earPanel.setBorder(new TitledBorder(new EtchedBorder(),"Ear config")) ;
	earPanel.setLayout(new GridLayout(2,1)) ;
	
	earPanel.add(getGainPanel()) ;
	earPanel.add(getDelayPanel()) ;
	
	return earPanel ;	
    }
    
    private JPanel getSidePanel()
    {
	sidePanel =new JPanel() ;
	sidePanel.setBorder(new TitledBorder(new EtchedBorder(),"Side")) ;
	
	MyBListener blist = new MyBListener() ;
	
	leftButton = new JRadioButton("Left") ;
	rightButton = new JRadioButton("Right") ;
	bothButton = new JRadioButton("Both") ;
	leftButton.addActionListener(blist) ;
	rightButton.addActionListener(blist) ;
	bothButton.addActionListener(blist) ;
	
	switch(side)
	    {
	    case DSEarComponent.LEFT : leftButton.setSelected(true) ; break ;
	    case DSEarComponent.RIGHT : rightButton.setSelected(true) ; break ;
	    case DSEarComponent.BOTH : bothButton.setSelected(true) ; break ;	
	    }
	
	group = new ButtonGroup() ;
	group.add(leftButton) ;
	group.add(rightButton) ;
	//group.add(bothButton) ;
	
	sidePanel.add(leftButton) ;
	sidePanel.add(rightButton) ;
	//sidePanel.add(bothButton) ;		
        
        return sidePanel ;			
    }
    
    private JPanel getGainPanel()
    {
	gainPanel =new JPanel() ;
	gainPanel.setBorder(new TitledBorder(new EtchedBorder(),"Gains")) ;
	
	JPanel sp = new JPanel(new GridLayout(0,2,5,5));
	sp.setBorder(new EmptyBorder(0,5,5,5));	
	
	sp.add(new JLabel("Gain IT")) ;
	gain1Field = new JTextField(3);
	sp.add(gain1Field) ;
	sp.add(new JLabel("Gain IS -> IT")) ;
	gain2Field = new JTextField(3) ;		
	sp.add(gain2Field) ;
	sp.add(new JLabel("Gain CS -> IT")) ;
	gain3Field = new JTextField(3) ;		
	sp.add(gain3Field) ;
	
	if(side == DSEarComponent.LEFT)
	    {
		gain1Field.setText(""+gains[0]) ;	
		gain2Field.setText(""+gains[1]) ;
		gain3Field.setText(""+gains[2]) ;
	    }
	else if(side == DSEarComponent.RIGHT)
	    {
		gain1Field.setText(""+gains[3]) ;	
		gain2Field.setText(""+gains[4]) ;
		gain3Field.setText(""+gains[5]) ;			
	    }
	else
	    {
		gain1Field.setText(""+gains[0]) ;	
		gain2Field.setText(""+gains[1]) ;
		gain3Field.setText(""+gains[2]) ;			
	    }		
        
        gainPanel.add(sp, BorderLayout.NORTH) ;	
        
        return gainPanel ;		
    }
    
    private JPanel getDelayPanel()
    {
	delayPanel =new JPanel() ;
	delayPanel.setBorder(new TitledBorder(new EtchedBorder(),"Delays")) ;
	
	JPanel sp = new JPanel(new GridLayout(0,2,5,5));
	sp.setBorder(new EmptyBorder(0,5,5,5));	
	
	sp.add(new JLabel("Delay IT (/us)")) ;
	delay1Field = new JTextField(3) ;
	sp.add(delay1Field) ;
	sp.add(new JLabel("Delay IS -> IT (/us)")) ;
	delay2Field = new JTextField(3) ;
	sp.add(delay2Field) ;
	sp.add(new JLabel("Delay CS -> IT (/us)")) ;
	delay3Field = new JTextField(3) ;
	sp.add(delay3Field) ;	
	
	if(side == DSEarComponent.LEFT)
	    {
		delay1Field.setText(""+ delays[0]) ;
		delay2Field.setText(""+ delays[1]) ;
		delay3Field.setText(""+ delays[2]) ;	
	    }
	else if(side == DSEarComponent.RIGHT)
	    {
		delay1Field.setText(""+ delays[3]) ;
		delay2Field.setText(""+ delays[4]) ;
		delay3Field.setText(""+ delays[5]) ;			
	    }
	else
	    {
		delay1Field.setText(""+ delays[0]) ;
		delay2Field.setText(""+ delays[1]) ;
		delay3Field.setText(""+ delays[2]) ;
	    }	
        
        delayPanel.add(sp, BorderLayout.NORTH) ;	
        
        return delayPanel ;		
    }	
    
    private JPanel getButtonPanel()
    {
	JPanel retPanel = new JPanel() ;
	
	MyBListener listener = new MyBListener() ;
	closeButton = new JButton("Close") ;
	closeButton.addActionListener(listener) ;
	
	saveButton = new JButton("Save") ;
	saveButton.addActionListener(listener) ;	
	
	retPanel.add(saveButton, BorderLayout.CENTER) ;
	retPanel.add(closeButton, BorderLayout.CENTER) ;	      
	
	return retPanel	; 
    }
    
    private class MyBListener implements ActionListener
    {
	public void actionPerformed(ActionEvent e)
	{
	    Object source = e.getSource() ;
	    
	    if(source == closeButton)
		{
		    frame.setVisible(false) ;	
		}
	    if(source == saveButton)
		{
		    saveSettings() ;
		    
		    if(rightButton.isSelected() || leftButton.isSelected())
			{
			    // LEFT EAR setting
			    earComponent.setGains(new double[]{gains[0],gains[1],gains[2]}, DSEarComponent.LEFT) ;
			    earComponent.setDelays(new double[]{delays[0],delays[1],delays[2]}, DSEarComponent.LEFT) ;
			    
			    // RIGHT EAR settings
			    earComponent.setGains(new double[]{gains[3],gains[4],gains[5]}, DSEarComponent.RIGHT) ;
			    earComponent.setDelays(new double[]{delays[3],delays[4],delays[5]}, DSEarComponent.RIGHT) ;
			}
		    
		    if(bothButton.isSelected())
			{
			    
			}
		    
		    //frame.setVisible(false) ;	
		}
	    
	    if(source == leftButton || source == rightButton || source == bothButton)
		{
		    saveSettings() ;
		    
		    // now update the side variable
		    if(source == leftButton)
			side = DSEarComponent.LEFT ;
		    if(source == rightButton)
			side = DSEarComponent.RIGHT ;
		    if(source == bothButton)
			side = DSEarComponent.BOTH ;
		    
		    // repaint the panels	
		    superRepaint() ;
		}
	}	
    }
    
    private void saveSettings()
    {
	switch(side)
	    {
	    case DSEarComponent.LEFT :
		{
		    System.out.println("save left") ;
		    gains[0] = Double.parseDouble(gain1Field.getText()) ;
		    gains[1] = Double.parseDouble(gain2Field.getText()) ;
		    gains[2] = Double.parseDouble(gain3Field.getText()) ;
		    earComponent.setGains(gains, DSEarComponent.LEFT) ;
		    
		    delays[0] = Double.parseDouble(delay1Field.getText());
		    delays[1] = Double.parseDouble(delay2Field.getText());
		    delays[2] = Double.parseDouble(delay3Field.getText());
		} ; break ;	
	    case DSEarComponent.RIGHT :
		{
		    System.out.println("save right") ;
		    gains[3] = Double.parseDouble(gain1Field.getText()) ;
		    gains[4] = Double.parseDouble(gain2Field.getText()) ;
		    gains[5] = Double.parseDouble(gain3Field.getText()) ;
		    earComponent.setGains(gains, DSEarComponent.RIGHT) ;
		    
		    delays[3] = Double.parseDouble(delay1Field.getText());
		    delays[4] = Double.parseDouble(delay2Field.getText());
		    delays[5] = Double.parseDouble(delay3Field.getText());
		} ; break ;	
	    case DSEarComponent.BOTH :
		{
		    System.out.println("save both") ;
		    gains[0] = Double.parseDouble(gain1Field.getText()) ;
		    gains[1] = Double.parseDouble(gain2Field.getText()) ;
		    gains[2] = Double.parseDouble(gain3Field.getText()) ;
		    earComponent.setGains(gains, DSEarComponent.LEFT) ;
		    
		    delays[0] = Double.parseDouble(delay1Field.getText());
		    delays[1] = Double.parseDouble(delay2Field.getText());
		    delays[2] = Double.parseDouble(delay3Field.getText()); 
		    
		    gains[3] = Double.parseDouble(gain1Field.getText()) ;
		    gains[4] = Double.parseDouble(gain2Field.getText()) ;
		    gains[5] = Double.parseDouble(gain3Field.getText()) ;
		    earComponent.setGains(gains, DSEarComponent.RIGHT) ;
		    
		    delays[3] = Double.parseDouble(delay1Field.getText());
		    delays[4] = Double.parseDouble(delay2Field.getText());
		    delays[5] = Double.parseDouble(delay3Field.getText());
		} ; break ;	
	    }		
    }
    
    private void setLabels()
    {
	if(side == DSEarComponent.LEFT)
	    {
		gain1Field.setText(""+gains[0]) ;	
		gain2Field.setText(""+gains[1]) ;
		gain3Field.setText(""+gains[2]) ;
	    }
	else if(side == DSEarComponent.RIGHT)
	    {
		gain1Field.setText(""+gains[3]) ;	
		gain2Field.setText(""+gains[4]) ;
		gain3Field.setText(""+gains[5]) ;			
	    }
	else
	    {
		gain1Field.setText(""+gains[0]) ;	
		gain2Field.setText(""+gains[1]) ;
		gain3Field.setText(""+gains[2]) ;			
	    }
	
	if(side == DSEarComponent.LEFT)
	    {
		delay1Field.setText(""+ (delays[0])) ;
		delay2Field.setText(""+ (delays[1])) ;
		delay3Field.setText(""+ (delays[2])) ;	
	    }
	else if(side == DSEarComponent.RIGHT)
	    {
		delay1Field.setText(""+ (delays[3])) ;
		delay2Field.setText(""+ (delays[4])) ;
		delay3Field.setText(""+ (delays[5])) ;			
	    }
	else
	    {
		delay1Field.setText(""+ (delays[0])) ;
		delay2Field.setText(""+ (delays[1])) ;
		delay3Field.setText(""+ (delays[2])) ;
	    }		
    }
    
    private void superRepaint()
    {
	setLabels() ;
	gainPanel.repaint() ;
	delayPanel.repaint() ;
	sidePanel.repaint() ;
	frame.repaint() ;		
    }
}

