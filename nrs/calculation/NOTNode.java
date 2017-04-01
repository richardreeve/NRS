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

import nrs.core.type.BooleanType;


/** Represents NOTNodes for the calculation component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/

public class NOTNode extends Node {

     // Inputs
    private BooleanType m_varInput;

    // Output
    private BooleanType m_varOut;

    public NOTNode(VariableManager vm, String vnName, String nodeType) {
        super(vm, vnName, nodeType);

        // Name must be the same as the one given in CSL file
        String varName = vnName + ".Input";

        m_varInput = new BooleanType(vm, varName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this));
                }
                public void deliver(boolean b){
                    handleMessageAt_Input(b);
                }
                
            };

        // Add to the variable manager
        addVariable(varName, m_varInput);

        String varName1 = vnName + ".Output";

        m_varOut = new BooleanType(vm, varName1, false, false){
		public void deliver(Message m)
		{
		    handleMessageAt_Output();
		}
		public void deliver(boolean b){
                    
		    handleMessageAt_Output();
		}
		
	    };

        // Add to the variable manager
        addVariable(varName1, m_varOut);

        
    }

   
     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at NOTNode: " 
			       + getVNName());     

         if (m.hasField("Input")) {
             
             // If input is not a boolean, Boolean constructor 
             // will provide value 'false'
             Boolean b = new Boolean (m.getField("Input"));

             m_varInput.setValue(b);
             m_varOut.onEvent(!b);
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
     * Process the receipt of a message at the NOTNode Output variable.
     *
     *
     */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }



   //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the NOTNode Input variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * types
     *
     */

    public void handleMessageAt_Input(Message m, Variable v, boolean diff) { 
	PackageLogger.log.fine("Received message at Input variable!");
      
        Boolean b = m_varInput.extractData(m);
        if (b!= null ) {
            handleMessageAt_Input(b.booleanValue());
        } else {
            PackageLogger.log.warning("Null value received at input");
        }	
    }

    
     /**
     * Process the receipt of a message at the NOTNode Input variable.
     *
     * @param b boolean value received
     *
     */
    public void handleMessageAt_Input(boolean b){ 
	PackageLogger.log.fine("Received message at Input variable!");
       
        PackageLogger.log.fine("Set input to " + b);

        m_varInput.setValue(b);
    
        // Set Add variable to the output
        m_varOut.onEvent(!m_varInput.getValue().booleanValue());
        
    }
}
