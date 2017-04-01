package nrs.oscilloscope.oscilloscopeGUI;

import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import java.awt.event.*;


public class OscilloscopeMenu extends JMenuBar implements ActionListener, 
							  ItemListener {
    private static final long serialVersionUID = 2L;
    
    private Oscilloscope _motherFrame;
    private OscilloscopeParam _paramDialog;
    private boolean _paramDisplayed = false;

    private JOptionPane optionPane = new JOptionPane ();


    private JMenu _fileMenu;
    private JMenu _subMenu;
    private JMenu _parameterMenu;
    
    private JMenuItem _menuItem;
    private JRadioButtonMenuItem _rbMenuItem;
    private JCheckBoxMenuItem _cbMenuItem;
    private ButtonGroup _group;

    public OscilloscopeMenu (Oscilloscope oscillo) {
	_motherFrame = oscillo;

	_fileMenu = new JMenu ("File");
	
	_menuItem = new JMenuItem("Set Title");
	_menuItem.addActionListener(this);
	_fileMenu.add(_menuItem);
	
	_fileMenu.addSeparator();
	
	_menuItem = new JMenuItem("Quit");
	_fileMenu.add(_menuItem);
		
		
		
	this.add(_fileMenu);
	
	
	_parameterMenu = new JMenu ("Settings");
	_subMenu = new JMenu ("Show");


	_group = new ButtonGroup();

	_rbMenuItem = new JRadioButtonMenuItem ("None");
	_rbMenuItem.setSelected(true);
	_group.add(_rbMenuItem);
	_subMenu.add(_rbMenuItem);

	_rbMenuItem = new JRadioButtonMenuItem ("Axis");
	_group.add(_rbMenuItem);
	_subMenu.add(_rbMenuItem);
	
	_rbMenuItem = new JRadioButtonMenuItem ("Scale");
	_group.add(_rbMenuItem);
	_subMenu.add(_rbMenuItem);

	
	_parameterMenu.add(_subMenu);


	_parameterMenu.addSeparator ();
	_cbMenuItem = new JCheckBoxMenuItem ("Display Settings");
	_cbMenuItem.addItemListener(this);
	_parameterMenu.add(_cbMenuItem);
	
		
	
	this.add(_parameterMenu);
	
	
	
	
	oscillo.setJMenuBar(this);
    }
    
    private void manageTitle () {
	String s = (String)JOptionPane.showInputDialog(
                    _motherFrame,
                    "Set a Title :",
                    "Title Dialog",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    _motherFrame.getTitle());
	
	//If a string was returned, say so.
	if ((s != null) && (s.length() > 0)) {
	    _motherFrame.setTitle(s);
	    return;
	}
    }

    private void manageParameters () {
	if (_paramDialog == null) {
	    _paramDialog = new OscilloscopeParam(_motherFrame);
	}
	if (_paramDisplayed) {
	    _paramDisplayed = false;
	    _paramDialog.setVisible(false);
	} else {
	    _paramDisplayed = true;
	    _paramDialog.setVisible(true);

	}
    }

    public void actionPerformed(ActionEvent evt) {
	//...Get information from the action event...
	String choice;
	if (evt.getSource()instanceof JMenuItem) { 
	    choice = evt.getActionCommand();
	    if (choice.equals("Set Title")) {
		manageTitle ();
	    }
	} 
    }
    
    public void itemStateChanged(ItemEvent evt) {
	//...Get information from the item event...
	if (evt.getSource()instanceof JCheckBoxMenuItem) {
	    JCheckBoxMenuItem obj = (JCheckBoxMenuItem)evt.getItem();
	    if (obj.getText().equals("Display Settings")) {
		manageParameters ();
	    }
	} 
    }
    
    
    
    
}
