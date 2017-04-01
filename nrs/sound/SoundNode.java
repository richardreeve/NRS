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
import nrs.core.type.BooleanType;
import nrs.core.type.VoidType;

import javax.sound.sampled.Mixer;
import javax.sound.sampled.AudioSystem;
import java.util.Vector;


/** Represents top-level Node for the component.
 *
 * @author Sarah Cope
*/

public class SoundNode extends Node{


      // Inputs
    private BooleanType m_enabled, m_repeating;
    private VoidType m_varInput;

    private static boolean enabled;
    private static Mixer mixer;
    private int started;
    protected static boolean running;
    // Speakers contained in this SoundNode
    private Vector<Speaker> speakers;
    private String enabledName, repeatingName;

    /** Constructor. 
     *
     * @param vmMan {@link VariableManager} to register node with
     * @param name vnname of the node
    */
    public SoundNode(VariableManager vmMan, String vnName){
	super(vmMan, vnName, "SoundNode");
        started = 0;
        running = false;
        speakers = new Vector<Speaker>();
        
        // Void Input
        String inName = vnName + ".Go";
            
        m_varInput = new VoidType(vmMan, inName){
                public void deliver(Message m)
                {
                    handleMessageAt_Input();
                }
                public void deliver()
                {
                    handleMessageAt_Input();
                }
            };
        
        // Add to the variable manager
        addVariable(inName, m_varInput);
        

        // Name must be the same as the one given in CSL file
        enabledName = vnName + ".Enabled";

        m_enabled = new BooleanType(vmMan, enabledName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), enabledName);
                }
                public void deliver(boolean b){
                    handleMessageAt_Input(b, enabledName);
                }
                
            };

        // Add to the variable manager
        addVariable(enabledName, m_enabled);

           // Name must be the same as the one given in CSL file
        repeatingName = vnName + ".Repeating";

        m_repeating = new BooleanType(vmMan, repeatingName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), repeatingName);
                }
                public void deliver(boolean b){
                    handleMessageAt_Input(b, repeatingName);
                }
                
            };

        // Add to the variable manager
        addVariable(repeatingName, m_repeating);


        Mixer.Info[] info = AudioSystem.getMixerInfo();
        for (int i = 0; i < info.length; i++ ) {
            if (info[i].getName().equals("Java Sound Audio Engine")) {
                mixer = AudioSystem.getMixer(info[i]);
                break;
            }
        }
        if (mixer == null) {
            if (info.length > 0) {
                mixer = AudioSystem.getMixer(info[0]);
                PackageLogger.log.warning("Using mixer " + info[0].getName() + 
                                          "- Java Sound Audio Engine not found");
            } else {
                PackageLogger.log.severe("\nNO MIXER FOUND\n");
            }
       
        }
    }

    /* Returns the Mixer for this SoundNode
     */
    public static Mixer getMixer() {
        return mixer;
    }

    /* Adds  
     * @param Speaker s 
     * to this SoundNodes Vector recording Speakers it contains
     */
    public void addSpeaker(Speaker s) {
        speakers.add(s);
    }

    /* Removes  
     * from this SoundNodes Vector recording Speakers it contains
     */
    public void removeSpeaker(Speaker s) {
        speakers.remove(s);
    }


     /** Deliver a {@link Message} to this <code>Node</code>.
     * @param Speaker s
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at soundNode: " 
			       + getVNName());     

         if (m.hasField("Enabled")) {
             
             // If input is not a boolean, Boolean constructor 
             // will provide value 'false'
             Boolean b = new Boolean (m.getField("Enabled"));

             m_enabled.setDefault(b);
             enabled = b;
         } else if (m.hasField("Repeating")) {
              // If input is not a boolean, Boolean constructor 
             // will provide value 'false'
             Boolean b = new Boolean (m.getField("Repeating"));

             m_repeating.setDefault(b);
         }
    } 

    /**
     * Process the receipt of a message at the SoundNode Input variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * @param name String name of the variable 
     * types
     *
     */

    public void handleMessageAt_Input(Message m, Variable v, boolean diff, String name) { 
	PackageLogger.log.fine("Received message at Input variable!");
      
        Boolean b = null;
        if (name.equals(enabledName)) {
            
            b = m_enabled.extractData(m);
            
        } else if (name.equals(repeatingName)) {
            
            b = m_repeating.extractData(m);
        }

        if (b != null ) {
            handleMessageAt_Input(b.booleanValue(), name);
        } else {
            PackageLogger.log.warning("Null value received at " + name);
        }
	
    }

    
     /**
     * Process the receipt of a message at the SoundNode enabled variable.
     *
     * @param b boolean value received
     * @param name String name of the variable
     *
     */
    public void handleMessageAt_Input(boolean b, String name){ 
	PackageLogger.log.fine("Received message at Input variable!");
        
        if (name.equals(enabledName)) {
             
            m_enabled.setValue(b);
            enabled = b;

        } else if(name.equals(repeatingName)){
            
            m_repeating.setValue(b);
        }
       
        PackageLogger.log.fine("Set " + name + " to " + b);

    }

    /* Returns current value of this SoundNodes
     * enabled Variable
     */
    public static boolean getEnabled() {
        return enabled;
    }

  
     /**
      * Process the receipt of a message at the SoundNode Input variable.
      *
     */

    public void handleMessageAt_Input(){
        if (!running && enabled) {
            CheckThread chk = new CheckThread();
            running = true;
            for (Speaker s : speakers) {
                PlayThread t = new PlayThread(s);
                t.start();
            }
            chk.start();
        }
    }

    // Seperate threads for each Speaker to play in
    public class PlayThread extends Thread {
        private Speaker s;
        public PlayThread(Speaker s) {
            this.s = s;
        }
        public void run() {
            if (s instanceof SpeakerNode){
                ++started;
                s.play();
                --started;
            } else {
                s.play();
            }
        }
    }

    // Thread to check when the SoundNode should repeat a cycle
    public class CheckThread extends Thread {
       
        public void run() {  
            while(started != 0) {}
            // Wait until all speakers have finished
            running = false;
            if(m_repeating.getValue()){
                handleMessageAt_Input();
            }
        }
    }
    
    
     /**
      * Process the receipt of a message at the SoundNode Output variable.
      *
      */
    public void handleMessageAt_Output(){
        	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
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
