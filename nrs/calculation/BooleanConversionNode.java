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
import nrs.core.type.VoidType;

/** Represents BooleanConversionNodes for the calculation component.  This
 * class inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/

public class BooleanConversionNode extends Node {
    

    // Input
    private BooleanType m_varInput;

    // Outputs
    private FloatType m_floatOut;

    private StringType m_stringOut;

    private IntegerType m_intOut;

    private VoidType m_voidOut;

    private int type;

    String varName;

    public BooleanConversionNode(VariableManager vm, String vnName, int type, String nodeType) {
        super(vm, vnName, nodeType);

        this.type = type;

        // Name must be the same as the one given in CSL file
        varName = vnName + ".Input";

        m_varInput = new BooleanType(vm, varName, false, false){
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

        instantiateVariables(vnName, vm);
        
        
    }

    private void setOutput(boolean b) {

        switch(type) {

        case CalculationComponent.FLOAT:
            double d = 0.0;
            if (b) {
                d = 1.0;
            }
            m_floatOut.onEvent(d);
            PackageLogger.log.fine("Set Output to '" + d + "'");
            break;
            
        case CalculationComponent.STRING:
            String s = String.valueOf(b);
            m_stringOut.onEvent(s);
            PackageLogger.log.fine("Set Output to '" + s + "'");
            break;

        case CalculationComponent.INT:
            int i = 0;
            if (b) {
                i = 1;
            }
            m_intOut.onEvent(i);
            PackageLogger.log.fine("Set Output to '" + i + "'");
            break;

        case CalculationComponent.VOID:
            if (b) {
                m_voidOut.onEvent();
            }
            
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

        case CalculationComponent.INT:
            
            String intName = vnName + ".Output";
            
            m_intOut = new IntegerType(vm, intName, false, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(int i){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(intName, m_intOut);
            break;
            

        case CalculationComponent.VOID:

            String voidName = vnName + ".Output";

            m_voidOut = new VoidType(vm, voidName){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(voidName, m_voidOut);
            
        }
    }
       




     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at BooleanConversionNode: " 
			       + getVNName());
        
        if (m.hasField("Input")) {

            Boolean b = new Boolean(m.getField("Input"));
            
            m_varInput.onEvent(b);
            setOutput(b.booleanValue());
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
     * Process the receipt of a message at the BooleanConversionNode Output variable.
     *
     *
     */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }



   //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the BooleanConversionNode Input variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * types
     *
     */

    public void handleMessageAt_Input(Message m, Variable v, boolean diff) { 

        Boolean b = m_varInput.extractData(m);
        if (b != null ) {
            handleMessageAt_Input(b.booleanValue());
	} else {
            PackageLogger.log.warning("Null value received at input.");
        }
    }


    /**
     * Process the receipt of a message at the BooleanConversionNode Input variable.
     *
     * @param b boolean value received
     *
     */
    public void handleMessageAt_Input(boolean b){ 
	PackageLogger.log.fine("Received message at Input variable!");

        setOutput(b);
        
    }
}
