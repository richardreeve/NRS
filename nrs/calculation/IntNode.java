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

import nrs.core.type.IntegerType;


/** Represents IntNodes for the calculation component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/



public class IntNode extends Node {
    

    // Inputs
    private IntegerType m_varInput1, m_varInput2;

    // Output
    private IntegerType m_varOut;
    
    // Names of inputs
    private String varName1, varName2;

    private int type;

    public IntNode(VariableManager vm, String vnName, int type, String nodeType) {
        super(vm, vnName, nodeType);

        this.type = type;

        // Name must be the same as the one given in CSL file
        varName1 = vnName + ".Input1";

        m_varInput1 = new IntegerType(vm, varName1, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), varName1);
                }
                public void deliver(int i){
                    handleMessageAt_Input(i, varName1);
                }
                
            };


        // Add to the variable manager
        addVariable(varName1, m_varInput1);

        varName2 = vnName + ".Input2";

        m_varInput2 = new IntegerType(vm, varName2, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), varName2);
                }
                public void deliver(int i){
                    handleMessageAt_Input(i, varName2);
                }
                
            };


        // Add to the variable manager
        addVariable(varName2, m_varInput2);

        String varName = vnName + ".Output";

        m_varOut = new IntegerType(vm, varName, true, false){
		public void deliver(Message m)
		{
		    handleMessageAt_Output();
		}
		public void deliver(int i){
                    
		    handleMessageAt_Output();
		}
		
	    };

        // Add to the variable manager
        addVariable(varName, m_varOut);

        
    }

   

    // Sets the output according to the node's type
    private void setOutput() {
        int total = 0;

        switch(type) {

        case CalculationComponent.ADD:
            total = m_varInput1.getValue().intValue() + m_varInput2.getValue().intValue();
            break;

        case CalculationComponent.SUBTRACT:
            total = m_varInput1.getValue().intValue() - m_varInput2.getValue().intValue();
            break;

        case CalculationComponent.MULTIPLY:
            total = m_varInput1.getValue().intValue() * m_varInput2.getValue().intValue();
            break;

        case CalculationComponent.DIVIDE:
            total = m_varInput1.getValue().intValue() + m_varInput2.getValue().intValue();
        }
       

       
        // Set Add variable to the output
        m_varOut.setValue(total);
    }

    
     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at IntNode: " 
			       + getVNName());  

         if (m.hasField("Input1") && m.hasField("Input2")) {

             try {
                 Integer i = new Integer(m.getField("Input1"));
                 Integer i2 = new Integer(m.getField("Input2"));
                 
                 m_varInput1.setValue(i);
                 m_varInput2.setValue(i2);
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
     * Process the receipt of a message at the IntNode Output variable.
     *
     *
     */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }



   //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the IntNode Input variable.
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

        Integer i;

        if (varName.equals(varName1)) {
            
            i = m_varInput1.extractData(m);
            
        } else {
            
            i = m_varInput2.extractData(m);
        }
        if (i != null ) {
            handleMessageAt_Input(i.intValue(), varName);
        } else {
            PackageLogger.log.warning("Null value received at " + varName);
        }
    }

    
     /**
     * Process the receipt of a message at the IntNode Input variable.
     *
     * @param i int value received
     * @param varName the name of the input variable the message was received on
     *
     */
    public void handleMessageAt_Input(int i, String varName){ 
	PackageLogger.log.fine("Received message at Input variable!");

        if (varName.equals(varName1)) {
             
            m_varInput1.setValue(i);

        } else {
            
            m_varInput2.setValue(i);
        }
       
        PackageLogger.log.fine("Set " + varName + " to " + i);

        setOutput();
        
    }
}
