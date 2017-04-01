package nrs.oscilloscope.oscilloscopeGUI;

public class AnalogueOscilloscope extends Oscilloscope {

    public AnalogueOscilloscope (String title) {
	super (title);
	_screen = new AnalogueScreen (this.getSize());
	this.getContentPane().add (_screen);
    }


    public void nextPoint (double value, double time) {
	manageTimestep(time);
	if (! (canScreenDisplay (time))) {
	    _screen.shiftX(timestep);
	}
	_screen.addPoint(time,value);
	_screen.repaint ();
    }
}
