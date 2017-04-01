package nrs.oscilloscope.oscilloscopeGUI;


public class DigitalOscilloscope extends Oscilloscope {


    public DigitalOscilloscope (String title) {
	super (title);
	_screen = new DigitalScreen (this.getSize());
	this.getContentPane().add (_screen);
    }

    public void nextPoint (double value, double time) {
	if (value > 0.5) nextPoint(true,time);
	else nextPoint(false,time);
    }

    public void nextPoint (boolean spiking, double time) {
	manageTimestep(time);
	if (! (canScreenDisplay (time))) {
	    _screen.shiftX(timestep);
	}
	if (spiking) {
	    _screen.addPoint(time,1.0);
	}
	_screen.repaint ();
    }
}
