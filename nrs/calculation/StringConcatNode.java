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

/** Represents StringConcatNodes for the calculation component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/

package nrs.calculation;

import nrs.core.base.Node;
import nrs.core.base.Variable;
import nrs.core.base.VariableManager;
import nrs.core.base.Message;

import nrs.core.type.StringType;

public class StringConcatNode extends Node {
    

    // Inputs
    private StringType m_varInput1, m_varInput2;

    // Output
    private StringType m_varOut;
    
    // Names of inputs
    private String varName1, varName2;

    public StringConcatNode(VariableManager vm, String vnName, String nodeType) {
        super(vm, vnName, nodeType);

        // Name must be the same as the one given in CSL file
        varName1 = vnName + ".Input1";

        m_varInput1 = new StringType(vm, varName1, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), varName1);
                }
                public void deliver(String s){
                    handleMessageAt_Input(s, varName1);
                }
                
            };


        // Add to the variable manager
        addVariable(varName1, m_varInput1);

        varName2 = vnName + ".Input2";

        m_varInput2 = new StringType(vm, varName2, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), varName2);
                }
                public void deliver(String s){
                    handleMessageAt_Input(s, varName2);
                }
                
            };

        // Add to the variable manager
        addVariable(varName2, m_varInput2);

        String varName = vnName + ".Output";

        m_varOut = new StringType(vm, varName, true, false){
		public void deliver(Message m)
		{
		    handleMessageAt_Output();
		}
		public void deliver(String s){
                    
		    handleMessageAt_Output();
		}
		
	    };

        // Add to the variable manager
        addVariable(varName, m_varOut);

        
    }

    
    private void setOutput() {

        m_varOut.setValue(m_varInput1.getValue() + m_varInput2.getValue());
        
        PackageLogger.log.fine("\nSet Output to '" + m_varOut.getValue() + "'");

    }

     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at FloatNode: " 
			       + getVNName());
        
        if (m.hasField("Input1") && m.hasField("Input2")) {

            String s = m.getField("Input1");
            String s1 = m.getField("Input2");
            
            m_varInput1.setValue(s);
            m_varInput2.setValue(s1);
            
            setOutput();
            
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
     * Process the receipt of a message at the StringConcatNode Output variable.
     *
     *
     */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }



   //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the StringConcatNode Input variable.
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
        
	String s = "";
        if (varName.equals(varName1)) {
             
            s = m_varInput1.extractData(m);

        } else if (varName.equals(varName2)){
            
            s = m_varInput2.extractData(m);
        }
        if (s != null ) {
            handleMessageAt_Input(s, varName);
        } else {
            PackageLogger.log.warning("Null value received at " + varName);
        }
    }

     /**
     * Process the receipt of a message at the StringConcatNode Input variable.
     *
     * @param s string value received
     * @param varName the name of the input variable the message was received on
     *
     */
    public void handleMessageAt_Input(String s, String varName){ 
	PackageLogger.log.fine("Received message at Input variable!");

        if (varName.equals(varName1)) {
             
            m_varInput1.setValue(s);

        } else if (varName.equals(varName2)){
            
            m_varInput2.setValue(s);
        }
        setOutput();
    }
}
