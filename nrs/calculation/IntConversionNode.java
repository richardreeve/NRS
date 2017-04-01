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

import nrs.core.type.FloatType;
import nrs.core.type.IntegerType;
import nrs.core.type.StringType;
import nrs.core.type.BooleanType;

/** Represents IntConversionNodes for the calculation component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/

public class IntConversionNode extends Node {
    

    // Input
    private IntegerType m_varInput;

    // Outputs
    private FloatType m_floatOut;

    private StringType m_stringOut;

    private BooleanType m_boolOut;
    
    private String varName;

    private int type;

    public IntConversionNode(VariableManager vm, String vnName, int type, String nodeType) {
        super(vm, vnName, nodeType);

        this.type = type;

        // Name must be the same as the one given in CSL file
        varName = vnName + ".Input";

        m_varInput = new IntegerType(vm, varName, false, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this));
                }
                public void deliver(int i){
                    handleMessageAt_Input(i);
                }
                
            };


        // Add to the variable manager
        addVariable(varName, m_varInput);

        instantiateVariables(vnName, vm);
        
        
    }

    private void setOutput(int i) {

        switch(type) {

        case CalculationComponent.FLOAT:
            m_floatOut.onEvent((double)i);
            PackageLogger.log.fine("Set Output to '" + (double)i + "'");
            break;
            
        case CalculationComponent.STRING:
            String s = String.valueOf(i);
            m_stringOut.onEvent(s);
            PackageLogger.log.fine("Set Output to '" + s + "'");
            break;

        case CalculationComponent.BOOL:
            boolean b = false;
            if (i > 0) {
                b = true;
            } 
            m_boolOut.onEvent(b);
            PackageLogger.log.fine("Set Output to '" + b + "'");

        }
    }
                              

   
    private void instantiateVariables(String vnName, VariableManager vm) {

        switch(type) {

        case CalculationComponent.FLOAT:

            String floatName = vnName + ".Output";
            
            m_floatOut = new FloatType(vm, floatName, false, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(double d){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(floatName, m_floatOut);
            
            break;
            
        case CalculationComponent.STRING:
            
            String stringName = vnName + ".Output";

            m_stringOut = new StringType(vm, stringName, false, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(String s){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(stringName, m_stringOut);

            break;

        case CalculationComponent.BOOL:
            
            String boolName = vnName + ".Output";
            
            m_boolOut = new BooleanType(vm, boolName, false, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(boolean b){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(boolName, m_boolOut);
            
            
            
        }
    }
       




     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at IntNode: " 
			       + getVNName());
        
        if (m.hasField("Input")) {

            try {

                Integer i = new Integer(m.getField("Input"));
                
                m_varInput.onEvent(i);
                setOutput(i.intValue());

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
     * Process the receipt of a message at the IntConversionNode Input variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     *
     */

    public void handleMessageAt_Input(Message m, Variable v, boolean diff) { 
	PackageLogger.log.fine("Received message at Input variable!"); 
        
	Integer i = m_varInput.extractData(m);
        if (i != null  ) {
            handleMessageAt_Input(i.intValue());
        } else {
            PackageLogger.log.warning("Null value received at input.");
        }	
    }


    /**
     * Process the receipt of a message at the IntConversionNode Input variable.
     *
     * @param i integer value received
     *
     */
    public void handleMessageAt_Input(int i){ 
	PackageLogger.log.fine("Received message at Input variable!");

        setOutput(i);
        
    }
}
