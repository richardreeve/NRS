/*
 * Copyright (C) 2004 Edinburgh University
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
package nrs.sound;

import nrs.core.base.Node;
import nrs.core.base.VariableManager;
import nrs.core.base.Message;
import nrs.core.base.Variable;
import nrs.core.type.FloatType;
import nrs.core.type.IntegerType;
import nrs.core.type.StringType;
import nrs.core.type.BooleanType;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;

/** Represents ToneNode for the component.
 *  Warning: Timing for msOn & msOff is not precise
 * 
 *
 * @author Sarah Cope
*/

public class ToneNode extends Node {

    // Variables
    private FloatType signalFreq, amplitude;
    private IntegerType secsOn, secsOff;    
    private StringType waveFormType;
    private BooleanType offFirstVar;
    // Oscillator
    private AudioInputStream oscillator;
    // Names
    private String signalName, ampName, sideName, 
        waveName, onName, offName, offFirstName;

    protected Speaker parent;
    private boolean cycleRunning, go;
    private final boolean continuous;
    private int waveformtype;
    private long msOn, msOff;
    private boolean offFirst;
    private byte[] data;

    /** Constructor. 
     *
     * @param vmMan {@link VariableManager} to register node with
     * @param name vnname of the node
     * @param parent Speaker parent of this node
    */
    public ToneNode(VariableManager vmMan, String vnName, Speaker parent){
	super(vmMan, vnName, "ToneNode");

        this.parent = parent;
        continuous = (parent instanceof ContinuousSpeaker);
        waveName = vnName + ".WaveFormType";

        waveFormType = new StringType(vmMan, waveName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), waveName);
                }
                public void deliver(String s){
                    handleMessageAt_Input(s);
                }
                
            };


        // Add to the variable manager
        addVariable(waveName, waveFormType);

        setWaveForm(waveFormType.getValue());

       
        //Signal Frequency
        signalName = vnName + ".SignalFreq";

        signalFreq = new FloatType(vmMan, signalName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), signalName);
                }
                public void deliver(double d){
                    handleMessageAt_Input(d, signalName);
                }
                
            };


        // Add to the variable manager
        addVariable(signalName, signalFreq);


        // Amplitude
        ampName = vnName + ".Amplitude";

        amplitude = new FloatType(vmMan, ampName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), ampName);
                }
                public void deliver(double d){
                    handleMessageAt_Input(d, ampName);
                }
                
            };


        // Add to the variable manager
        addVariable(ampName, amplitude);


        if (!continuous) {

            // secsOn
            onName = vnName + ".secsOn";
            
            secsOn = new IntegerType(vmMan, onName, true, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Input(m, this, checkType(m, this), onName);
                    }
                    public void deliver(int i){
                        handleMessageAt_Input(i, onName);
                    }
                    
                };
            
            
            // Add to the variable manager
            addVariable(onName, secsOn);
            
            // secsOff
            offName = vnName + ".secsOff";
            
            secsOff = new IntegerType(vmMan, offName, true, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Input(m, this, checkType(m, this), offName);
                    }
                    public void deliver(int i){
                        handleMessageAt_Input(i, offName);
                    }
                    
                };
            
            
            // Add to the variable manager
            addVariable(offName, secsOff);
            
            
            
            // Off first (true) or on first (false)
            offFirstName = vnName + ".OffFirst";
            
            offFirstVar = new BooleanType(vmMan, offFirstName, true, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Input(m, this, checkType(m, this), offName);
                    }
                    public void deliver(boolean b){
                        handleMessageAt_Input(b);
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(offFirstName, offFirstVar);
            

        }
        
        // Sets default values for the oscillator
        oscillator = new Oscillator(Oscillator.WAVEFORM_TRIANGLE, 
                                    1000.0f,
                                    0.7f,
                                    parent.audioFormat, 
                                    AudioSystem.NOT_SPECIFIED);        
      
    }

     /**
      * Process the receipt of a message at the ToneNode Output variable.
      *
      *
      */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }


    private void resetOscillator() {
        oscillator = new Oscillator (waveformtype,
                                     signalFreq.getValue().floatValue(),
                                     amplitude.getValue().floatValue(),
                                     parent.audioFormat,
                                     AudioSystem.NOT_SPECIFIED);
       
    }


     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at ToneNode: " 
			       + getVNName());
        boolean lessThan = false;
        if (m.hasField("secsOn")) {
             Integer i = new Integer(m.getField("secsOn")); 
             if (i > 0) {
                 secsOn.setDefault(i);
             } else {
                 lessThan = true;
             }
        } 
        if (m.hasField("secsOff")) {
            Integer i = new Integer(m.getField("secsOff"));     
            if (i > 0) {
                secsOff.setDefault(i);
            } else {
                 lessThan = true;
            }
        }
        if (m.hasField("WaveFormType")) {
            String s = m.getField("WaveFormType");                
            setWaveForm(s);
        }
        if (m.hasField("SignalFreq")) {
            Double d = new Double(m.getField("SignalFreq"));       
            if (d > 0) {
                signalFreq.setDefault(d);
            } else {
                 lessThan = true;
            }
        }
        if (m.hasField("Amplitude")) {
            Double d = new Double(m.getField("Amplitude"));
            if (d > 0 ){
                amplitude.setDefault(d);
            } else {
                 lessThan = true;
            }
        }
        if (m.hasField("OffFirst")) {
            Boolean b = new Boolean(m.getField("OffFirst"));
            offFirstVar.setDefault(b);
            offFirst = b;
        }
        resetOscillator();
        if (lessThan) {
            PackageLogger.log.warning("Value must be greater than 0");
        }
   
    } 

    /**
     * Process the receipt of a message at the ToneNode Input variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different types
     * @param name last part of the name of the Variable receiving the message
     *
     */

    public void handleMessageAt_Input(Message m, Variable v, boolean diff, String name) { 
	PackageLogger.log.fine("Received message at Input variable!");

        if(m.getType().equals("float")) {
            Double d = null;
            if(name.equals(signalName)) {
                d = signalFreq.extractData(m);
            } else if(name.equals(ampName)) {
                d = amplitude.extractData(m);
            }
            if (d != null && d > 0) {
                handleMessageAt_Input(d.doubleValue(), name);
            } else {
                PackageLogger.log.warning("Value must be greater than 0 and must not be null");
            }

        } else if (m.getType().equals("integer")) {
            Integer i = null;
            if(name.equals(onName)) {
                i = secsOn.extractData(m);
            } else if(name.equals(offName)) {
                i = secsOff.extractData(m);
            }
            if (i != null && i > 0) {
                handleMessageAt_Input(i.intValue(), name);
            } else {
                PackageLogger.log.warning("Value must be greater than 0 and must not be null");
            }

        } else if(m.getType().equals("string")) {
            String s = waveFormType.extractData(m);
            handleMessageAt_Input(s);
        } else if(m.getType().equals("boolean")) {
            Boolean b = offFirstVar.extractData(m);
            handleMessageAt_Input(b);
        } else {
            PackageLogger.log.warning("Message type not handled by this node.");
        }
        
    }



    private void setWaveForm(String s) {
        
        if (s.equals("sawtooth")) {
            waveformtype = Oscillator.WAVEFORM_SAWTOOTH;
        } else if (s.equals("triangle")) {
            waveformtype = Oscillator.WAVEFORM_TRIANGLE;
        } else if (s.equals("square")) {
            waveformtype = Oscillator.WAVEFORM_SQUARE;
        } else {
            waveformtype = Oscillator.WAVEFORM_SINE;
            if (!s.equals("sine")) {
                PackageLogger.log.warning(s + " not a valid waveform type. Using default(sine).");
                s = "sine";
            }
        }
        waveFormType.setValue(s);
        

    }
                        
   
     /**
     * Plays the tone
     *
     *
     */
    public synchronized void play(){ 
        if (SoundNode.getEnabled()){
            data = new byte[1000];
            parent.line.start();
            if (!continuous) {
                OnThread on = new OnThread();
                msOn = 1000*secsOn.getValue().longValue();
                msOff = 1000*secsOff.getValue().longValue();
                if (msOn <= 20) {
                    msOn = 20;
                }
                if(offFirst){
                    try {
                        Thread.currentThread().sleep(msOff);
                    } catch (InterruptedException e) {}
                }
                try {
                    go = true;
                    on.start();
                    while(go){
                        int read = oscillator.read(data);
                        parent.line.write(data, 0, read);         
                    }
                } catch (IOException e1) {
                    PackageLogger.log.warning(e1.getMessage());
                }
                if(offFirst) { return; }
                try {
                    Thread.currentThread().sleep(msOff);
                } catch (InterruptedException e) {}
            } else {
                while(SoundNode.getEnabled()){
                    try {
                        int read = oscillator.read(data);
                        parent.line.write(data, 0, read);
                    } catch (IOException e) {
                        PackageLogger.log.warning(e.getMessage());
                    }  
                    
                }
                parent.line.stop();
                parent.line.flush();
            }
        }
    }

    
    public class OnThread extends Thread {
        public void run() {            
            try {
                sleep(msOn-20);
                parent.line.stop();
                parent.line.flush();
                go = false;
            } catch (InterruptedException e) {}
        }
    }

     /**
     * Process the receipt of a message at the ToneNode Input variable.
     *
     * @param d double value received
     * @param name the last part of the name of the Variable the message was received on
     *
     */
    public void handleMessageAt_Input(double d, String name){ 
	PackageLogger.log.fine("Received message at Input variable!");

        if(name.equals(signalName) && d != 0.0) {
            signalFreq.setValue(d);
        } else if(name.equals(ampName)) {
            amplitude.setValue(d);
        }

        PackageLogger.log.fine("Set " + name + " to " + d);

        resetOscillator();
    }

     /**
     * Process the receipt of a message at the ToneNode Input variable.
     *
     * @param i int value received
     * @param name the last part of the name of the Variable the message was received on
     *
     */
    public void handleMessageAt_Input(int i, String name){ 
	PackageLogger.log.fine("Received message at Input variable!");

        if(name.equals(onName)) {
            secsOn.setValue(i);
        } else if(name.equals(offName)) {
            secsOff.setValue(i);
        }

        PackageLogger.log.fine("Set " + name + " to " + i);

        resetOscillator();
    }
   
    /**
     * Process the receipt of a message at the ToneNode Input variable.
     *
     * @param s String value received
     *
     */
    public void handleMessageAt_Input(String s){ 
	PackageLogger.log.fine("Received message at Input variable!");
        setWaveForm(s);
        resetOscillator();
       
    }


    /**
     * Process the receipt of a message at the ToneNode Input variable.
     *
     * @param b Boolean value received
     *
     */
    public void handleMessageAt_Input(boolean b){ 
	PackageLogger.log.fine("Received message at Input variable!");
        
        offFirstVar.setValue(b);
        offFirst = b;
    }
   
    
    /**
     * Compares the {@link Message} type and the {@link Variable}
     * type. Returns true if they appear different (based on a
     * case-sensitive string comparison); returns false otherwise.
     */
    protected boolean checkType(Message m, Variable v){
      return !(m.getType().equals(v.getVNName()));
    }

}
