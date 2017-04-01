package nrs.oscilloscope.oscilloscopeGUI;

import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public abstract class Oscilloscope extends JFrame {
    private static final long serialVersionUID = 4L;
    
    protected GraphicalScreen _screen;
    protected double timestep, lastTime = 0.0;
    
    public Oscilloscope (String title) {
	super (title);
	this.setSize(new Dimension (300,250));
	WindowAdapter wa = new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    System.exit(0);
		}
	    };
	this.addWindowListener(wa);
	new OscilloscopeMenu (this);
	
	
	//_screen = new GraphicalScreen (getSize());
	// this.getContentPane().add (_screen);
	setVisible (true);
    }
    
    protected void manageTimestep (double time) {
	timestep = time - lastTime;
	lastTime = time;
    }
    
    protected boolean canScreenDisplay (double t) {
	if (t >= _screen.getMaxX()) {
	    return false;
	} else return true;
    }

    //-----------------------------------------------------------

    public double getMaxX () {
	return _screen.getMaxX();
    }    
    
    public double getMinX () {
	return _screen.getMinX();
    }    

    public double getMaxY () {
	return _screen.getMaxY();
    }    
    
    public double getMinY () {
	return _screen.getMinY();
    }    

	public void setMaxY (double y) {
		_screen.setMaxY(y+0.1*y);
	}

	public void setMinY (double y) {
		_screen.setMaxY(y-0.1*y);
	}
	
	public double getTimeDisplayed () {
		return (_screen.getXAxisSize());
	}
	
	public void setTimeDisplayed (double l) {
		_screen.setMinX(_screen.getMaxX() - l);
	}
	
    //---------------------------------------------------------------

    public void resize () {
	_screen.autoResizeGraph();
    }

    public abstract void nextPoint (double value, double time);
}
