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

/** Represents VoidToBooleanNodes for the calculation component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/


public class VoidToBooleanNode extends Node {

     // Inputs
    private VoidType m_varInput, m_reset;

    // Output
    private BooleanType m_varOut;

    String varName, resetName;

    public VoidToBooleanNode(VariableManager vm, String vnName, String nodeType) {
        super(vm, vnName, nodeType);

        // Name must be the same as the one given in CSL file
        varName = vnName + ".Input";

        m_varInput = new VoidType(vm, varName){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(varName);
                }
                public void deliver(){
                    handleMessageAt_Input(varName);
                }
                
            };

        // Add to the variable manager
        addVariable(varName, m_varInput);

        // Name must be the same as the one given in CSL file
        resetName = vnName + ".Reset";

        m_reset = new VoidType(vm, resetName){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(resetName);
                }
                public void deliver(){
                    handleMessageAt_Input(resetName);
                }
                
            };

        // Add to the variable manager
        addVariable(resetName, m_reset);

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
	PackageLogger.log.fine("Received message at VoidToBooleanNode: " 
			       + getVNName());     

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
     * Process the receipt of a message at the VoidToBooleanNode Output variable.
     *
     *
     */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }



   //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the VoidToBooleanNode Input variable.
     *
     * @param name String name of variable which received the message
     *
     */

    public void handleMessageAt_Input(String name) { 
	PackageLogger.log.fine("Received message at Input variable!");
        
        if (name.equals(resetName)) {
            
            m_varOut.onEvent(false);
        } else {
            m_varOut.onEvent(true);
        }	
    }

    
     
}
