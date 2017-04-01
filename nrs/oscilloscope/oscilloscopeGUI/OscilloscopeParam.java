package nrs.oscilloscope.oscilloscopeGUI;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;

public class OscilloscopeParam extends JDialog implements ActionListener {
    private static final long serialVersionUID = 3L;
    
    private Oscilloscope _motherFrame;
    private JPanel _mainPanel;
    private JPanel _myPanel;
    private JButton _myButton;
    private JTextField _timeTextField;
    private JTextField [] _yTextField = new JTextField [2];

    public OscilloscopeParam (Oscilloscope frame) {
	super (frame,"Oscilloscope Parameters",false);

	this.setSize (new Dimension (300,100));
	this.setPreferredSize (new Dimension (300,100));
	this.setResizable (false);

	_motherFrame = frame;

	_timeTextField = new JTextField ();
	
	_yTextField[0] = new JTextField ();
	_yTextField[1] = new JTextField ();

	initDialogPanel ();


    }


    private void initDialogPanel () {

	_mainPanel = new JPanel (new GridLayout(3,1));


	//----
	
	//-----

	_myPanel = new JPanel (new GridLayout(1,2));
	_myPanel.add(createDataPanel("MinY :",_yTextField[0],
			""+_motherFrame.getMinY(),"unit"));
	_myPanel.add(createDataPanel("MaxY :",_yTextField[1],
			""+_motherFrame.getMaxY(),"unit"));
	_mainPanel.add(_myPanel);

	//-----
	_myPanel = new JPanel(new GridLayout(1,2));
	_myPanel.add(createDataPanel("Time :",_timeTextField,
				     ""+_motherFrame.getTimeDisplayed(),"s"));
	_myButton = new JButton ("Automatic scaling...");
	_myButton.addActionListener(this);
	_myPanel.add(_myButton);
	_mainPanel.add(_myPanel);

	//-----

	_myPanel = new JPanel(new GridLayout(1,2));

	_myButton = new JButton("Apply");
	_myPanel.add(_myButton);
	_myButton.addActionListener(this);

	_myButton = new JButton ("Close");
	_myButton.addActionListener(this);
	_myPanel.add(_myButton);

	_mainPanel.add(_myPanel);

	this.getContentPane().add (_mainPanel);
    }

    private JPanel createDataPanel(String label1,JTextField myTextField,
				   String text,String label2) {

	JPanel myPanel = new JPanel(new GridLayout (1,3));
	myPanel.add(new JLabel (label1));
	myTextField.setText(text);
	myPanel.add(myTextField);
	myPanel.add(new JLabel (label2));

	return myPanel;
    }
    private void sendFields () {
		_motherFrame.setTimeDisplayed(new Double(_timeTextField.getText()));
		_motherFrame.setMaxY(new Double(_yTextField[1].getText()));
		_motherFrame.setMinY(new Double(_yTextField[0].getText()));
    }

    public void fieldsUpdate () {
	_timeTextField.setText(""+_motherFrame.getTimeDisplayed());
	_yTextField[0].setText(""+_motherFrame.getMinY());
	_yTextField[1].setText(""+_motherFrame.getMaxY());
    }

    public void actionPerformed(ActionEvent evt) {
	//...Get information from the action event...
	String choice;
	if (evt.getSource()instanceof JButton) { 
	    choice = evt.getActionCommand();
	    if (choice.equals("Automatic scaling...")) {
		_motherFrame.resize();
		fieldsUpdate ();
	    }
	    else if (choice.equals("Apply")) {
		sendFields();
		fieldsUpdate();
	    }
	    else if (choice.equals("Close")) {
		this.setVisible(false);
	    }

	} 
    }
    
    
}
