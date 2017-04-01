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

import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.FloatControl;

/* Super class for SpeakerNodes & ContinuousSpeakers
 *  
 * @author Sarah Cope
*/

public abstract class Speaker extends Node{


    private FloatType balance;
    
    protected SourceDataLine line;

    protected AudioFormat audioFormat;
    
    private FloatControl control;

    /** Constructor. 
     *
     * @param vmMan {@link VariableManager} to register node with
     * @param vnName vnname of the node
     * @param type this nodes type
    */

    public Speaker(VariableManager vmMan, String vnName, String type){
        super(vmMan, vnName, type);

         // Balance
        String balanceName = vnName + ".Balance";
        
        balance = new FloatType(vmMan, balanceName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this);
                }
                public void deliver(double d){
                    handleMessageAt_Input(d);
                }
                
            };
        
        // Add to the variable manager
        addVariable(balanceName, balance);


        audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100.0f, 
                                      16, 2, 4, 44100.0f, false);
        
        // set line with default balance
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        try {
            line = (SourceDataLine)SoundNode.getMixer().getLine(info);
            line.open(audioFormat);
            control = (FloatControl) line.getControl(FloatControl.Type.PAN);
            control.setValue(balance.getValue().floatValue());
        } catch (LineUnavailableException e ) {
            PackageLogger.log.severe("\n" + e.getMessage() + "\n");
        }
    }

    /* Start playing ToneNodes 
     */
    public abstract void play();

    /* Add ToneNode to Speaker object
     */
    public abstract void addTone(ToneNode t);

    /* Remove ToneNode from Speaker object 
     */
    public abstract void removeTone(ToneNode t);

    
    /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
   
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at soundNode: " 
			       + getVNName());
        
        if (m.hasField("Balance")) {
            Double d = new Double(m.getField("Balance")); 
            if (d >= -1.0 && d <= 1.0) {
                balance.setDefault(d);
                if(control != null){
                    control.setValue(d.floatValue());
                } else {
                    PackageLogger.log.severe("\nAudio Device Unavailable\n");
                }
            } else {
                PackageLogger.log.warning("Balance value must be between 1.0 & -1.0");
            }
        }
    }   


    /**
     * Process the receipt of a message at the Speaker Input variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     *
     */

    public void handleMessageAt_Input(Message m, Variable v) { 
	PackageLogger.log.fine("Received message at Input variable!"); 
        // Check message is not for SpeakerNode ordering variable(string);
        if (m.getType().equals("float")) {
            Double d = balance.extractData(m);
            if (d != null ) {
                handleMessageAt_Input(d.doubleValue());
            } else {
                PackageLogger.log.warning("Null value received at Balance variable");
            }	
        }
    }

      /**
     * Process the receipt of a message at the Speaker Input variable.
     *
     * @param d double value received
     *
     */
    public void handleMessageAt_Input(double d){ 
	PackageLogger.log.fine("Received message at Input variable!");
        if (d >= -1.0 && d <= 1.0) {
            balance.setValue(d);
            control.setValue((float)d);
        } else {
            PackageLogger.log.warning("Balance value must be between 1.0 & -1.0");
        }
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
