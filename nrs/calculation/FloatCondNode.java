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
import nrs.core.type.FloatType;

/** Represents CondNodes for the calculation component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/


public class FloatCondNode extends Node {

     // Inputs
    private FloatType m_varInput, m_compareTo;

    // Output
    private BooleanType m_varOut;
    
    // Names of inputs
    private String inName, compName;

    private int type;

    public FloatCondNode(VariableManager vm, String vnName, int type, String nodeType) {
  
        super(vm, vnName, nodeType);

        this.type = type;

       // Name must be the same as the one given in CSL file
        inName = vnName + ".Input";

        m_varInput = new FloatType(vm, inName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), inName);
                }
                public void deliver(double d){
                    handleMessageAt_Input(d, inName);
                }
                
            };


        // Add to the variable manager
        addVariable(inName, m_varInput);

        compName = vnName + ".CompareTo";

        m_compareTo = new FloatType(vm, compName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), compName);
                }
                public void deliver(double d){
                    handleMessageAt_Input(d, compName);
                }
                
            };


        // Add to the variable manager
        addVariable(compName, m_compareTo);


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

   

    // Sets the output according to the node's type 
    private void setOutput() {
        boolean out = false;
        
        switch(type) {
            
        case CalculationComponent.LESSTHAN:
            if (m_varInput.getValue().doubleValue() < m_compareTo.getValue().doubleValue()) {
                out = true;
            }
            break;

        case CalculationComponent.EQUALTO:
           if (m_varInput.getValue().doubleValue() == m_compareTo.getValue().doubleValue()) {
                out = true;
            }

        }
    
        // Set Add variable to the output
        m_varOut.onEvent(out);

    }



 
     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at ConditionNode: " 
			       + getVNName());     

         if (m.hasField("Input") && m.hasField("CompareTo")) {

             try {
                 Double d = new Double (m.getField("Input"));
                 Double d1 = new Double (m.getField("CompareTo"));
                 
                 m_varInput.setValue(d);
                 m_compareTo.setValue(d1);
                 setOutput();
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
     * Process the receipt of a message at the CondNode Output variable.
     *
     *
     */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }



    //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the CondNode Input variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * @param varName the name of the input variable the message was received on
     * types
     *
     */

    public void handleMessageAt_Input(Message m, Variable v, boolean diff, String varName) { 
	PackageLogger.log.fine("Received message at Input variable!"); 
        
	Double d;
        if (varName.equals(inName)) {
             
            d = m_varInput.extractData(m);
            
        } else {
            
            d = m_compareTo.extractData(m);
        }
        if (d != null ) {
            handleMessageAt_Input(d.doubleValue(), varName);	
        } else {
            PackageLogger.log.warning("Null value received at " + varName);
        }
    }

     /**
     * Process the receipt of a message at the CondNode Input variable.
     *
     * @param d double value received
     * @param varName the name of the input variable the message was received on
     *
     */
    public void handleMessageAt_Input(double d, String varName){ 
	PackageLogger.log.fine("Received message at Input variable!");

        if (varName.equals(inName)) {
             
            m_varInput.setValue(d);

        } else {
            
            m_compareTo.setValue(d);
        }
       
        PackageLogger.log.fine("Set " + varName + " to " + d);

        setOutput();
    }
}
