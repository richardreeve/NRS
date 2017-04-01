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



package nrs.calculation;

import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;
import nrs.core.base.Message;

import nrs.core.type.VoidType;
import nrs.core.type.BooleanType;
import nrs.core.type.IntegerType;

import java.util.Timer;
import java.util.TimerTask;

/** Represents VoidToBooleanDelayNodes for the calculation component.This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/

public class VoidToBooleanDelayNode extends Node {

     // Inputs
    private VoidType m_varInput;

    private IntegerType m_delay;

    // Output
    private BooleanType m_varOut;

    // Number of milliseconds delay
    private long delay;

    private Timer timer;

    public VoidToBooleanDelayNode(VariableManager vm, String vnName, String nodeType) {
        super(vm, vnName, nodeType);

        // Name must be the same as the one given in CSL file
        String varName = vnName + ".Input";

        m_varInput = new VoidType(vm, varName){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this));
                }
                public void deliver(){
                    handleMessageAt_Input();
                }
                
            };

        // Add to the variable manager
        addVariable(varName, m_varInput);

        String delayName = vnName + ".msDelay";

        m_delay = new IntegerType(vm, delayName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this));
                }
                public void deliver(int i){
                    handleMessageAt_Input(i);
                }
                
            };

        // Add to the variable manager
        addVariable(delayName, m_delay);



        String outName = vnName + ".Output";

        m_varOut = new BooleanType(vm, outName, false, false){
		public void deliver(Message m)
		{
		    handleMessageAt_Output();
		}
		public void deliver(boolean b){
                    
		    handleMessageAt_Output();
		}
		
	    };

        // Add to the variable manager
        addVariable(outName, m_varOut);

        
    }

    class DelayTask extends TimerTask {
        public void run() {
            m_varOut.onEvent(false);
            PackageLogger.log.fine("\nSet Output to FALSE");
	    timer.cancel(); //Terminate the timer thread
        }
    }
 
     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at VoidToBooleanDelayNode: " 
			       + getVNName());     
        
        if (m.hasField("msDelay")) {
            
            try {
                
                 Integer i = new Integer(m.getField("msDelay"));
                 m_delay.setValue(i.intValue());
                 delay = (long) m_delay.getValue();

                 PackageLogger.log.fine("Set delay to " + delay + " ms.");
                 
            }
            catch (NumberFormatException nfe){
                PackageLogger.log.warning("Error with message - not the right"
                                          + " format!");
            }
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

     /**
     * Process the receipt of a message at the VoidToBooleanDelayNode Output variable.
     *
     *
     */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }


    
   //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the VoidToBooleanDelayNode Input variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * types
     *
     */

    public void handleMessageAt_Input(Message m, Variable v, boolean diff) { 
	PackageLogger.log.fine("Received message at Input variable!"); 
        
        if (m.getType().equals("integer")) {
            Integer i = m_delay.extractData(m);
            if (i != null  ) {
                handleMessageAt_Input(i.intValue());
            }
            
        } else if (m.getType().equals("void") ) {
            handleMessageAt_Input();
            
        } else {
            PackageLogger.log.warning("Message is not handled by this variable");
        }
       	
    }

  
    //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the VoidToBooleanDelayNode Input variable.
     *
     *
     */

    public void handleMessageAt_Input() { 
       
        m_varOut.onEvent(true);

        PackageLogger.log.fine("\nSet Output to TRUE");
        
        timer = new Timer();
        timer.schedule(new DelayTask(), delay);
        
    }
    //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the VoidToBooleanDelayNode Input variable.
     * @param i integer the value received
     *
     */

    public void handleMessageAt_Input(int i) { 

        m_delay.setValue(i);
        delay = (long) i;
        PackageLogger.log.fine("Set delay to " + delay + " ms.");
        
    }

     
}
