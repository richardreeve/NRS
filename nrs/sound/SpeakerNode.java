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

import nrs.core.base.Message;
import nrs.core.base.Variable;
import nrs.core.base.Node;
import nrs.core.base.VariableManager;
import nrs.core.type.StringType;

import java.util.Arrays;
import java.util.Vector;
import java.util.HashMap;


/** Represents SpeakerNodes for the component.
 *
 * @author Sarah Cope
*/

public class SpeakerNode extends Speaker{

    // All ToneNodes in this SpeakerNode
    private HashMap children;
    // Names of all ToneNodes in this SpeakerNode
    private Vector<String> names;
    // Names of all ToneNodes being played
    private Vector<String> currentNames;
    // Variable
    private StringType ordering;

    /** Constructor. 
     *
     * @param vmMan {@link VariableManager} to register node with
     * @param name vnname of the node
    */
    public SpeakerNode(VariableManager vmMan, String vnName){
	super(vmMan, vnName, "SpeakerNode");

        children = new HashMap();
        names = new Vector();
        currentNames = new Vector();

        String orderingName = vnName + ".ToneOrdering";

        ordering = new StringType(vmMan, orderingName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this));
                }
                public void deliver(String s){
                    handleMessageAt_Input(s);
                }
                
            };


        // Add to the variable manager
        addVariable(orderingName, ordering);

    }

    /* Returns the last part of the name of 
     * @param ToneNode t
     * i.e. a node named SoundNode1.SpeakerNode1.Tone1
     * will return the string "Tone1"
     */

    public String getToneName(ToneNode t) {
        String s = t.getVNName();
        int i = s.lastIndexOf(46)+1;
        if (i <= s.length()){
            s = s.substring(i);
        } else {
            s = "";
        }
        return s;
    }
              
    /*
     * Adds
     * @param ToneNode t
     * to this SpeakerNodes HashMap of children
     */

    public void addTone(ToneNode t) {
        String s = getToneName(t);
        children.put(s, t);
        names.add(s);
        currentNames.add(s);
        setOrdering();
    }

     /*
     * Removes
     * @param ToneNode t
     * from this SpeakerNodes HashMap of children
     */

    public void removeTone(ToneNode t) {
        String s = getToneName(t);
        children.remove(s);
        names.remove(s);
        currentNames.remove(s);
        setOrdering();
    }

    /* 
     * Plays the ToneNodes in this SpeakerNode
     * in the oreder specified by the ordering variable
     */

    public void play() {
        
        for (String s : currentNames) {
           
            if (children.containsKey(s)) {
                ToneNode t = (ToneNode)children.get(s);
                t.play();
            } else {
                PackageLogger.log.warning("'" + s + "' " + "is not a valid ToneNode name");
            }
        }
    }


    /**
     * Process the receipt of a message at the SpeakerNode Input variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different types
     *
     */
    public void handleMessageAt_Input(Message m, Variable v, boolean diff) {
        // Check message is not for balance variable (floating)
        if (!SoundNode.running && m.getType().equals("string")) {
            PackageLogger.log.fine("Received message at ToneOrdering variable");
            String s = ordering.extractData(m);
            if (!s.equals("")) {
                handleMessageAt_Input(s);
            }
        } else {
            PackageLogger.log.warning("Cannot alter order while sound is playing. " + 
                                      "Message type must be string.");
        }
    }

    // Set the order of the names of ToneNodes to be played
    private void setOrdering() {
        String s = currentNames.toString();
        String sub = s.substring(1, s.length()-1);
        ordering.setValue(sub);
        PackageLogger.log.fine("Set ordering to: " + sub);
    }


    /**
     * Process the receipt of a message at the SpeakerNode Input variable.
     *
     * @param s the String received
     *
     */

    public void handleMessageAt_Input(String s){
       
        // Not an attribute as attribute displays are not updated
        boolean invalid = false;
        String [] substrings = s.split(",");
        for (int i = 0; i < substrings.length; i++){
            substrings[i] = substrings[i].trim();
            if (!names.contains(substrings[i])){
                invalid = true;
                break;
            }
        }
        if(!invalid){ 
            currentNames = new Vector(Arrays.asList(substrings));
            setOrdering();
        } else {
            PackageLogger.log.warning("Input contains invlaid ToneNode name. " +
                                      "Using previous value");
        }            
    }
   
}
