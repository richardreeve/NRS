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


import nrs.core.unit.*;
import nrs.core.type.FloatType;
import nrs.core.type.IntegerType;
import nrs.core.type.StringType;
import nrs.core.type.BooleanType;

/** Represents FloatConversionNodes for the calculation component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/

public class FloatConversionNode extends Node {
    

    // Input
    private FloatType m_varInput;

    // Outputs
    private IntegerType m_intOut;
    private StringType m_stringOut;
    private BooleanType m_boolOut;

    private Time m_timeOut;
    private Capacitance m_capOut;
    private Voltage m_voltageOut;
    private Current m_currentOut;
    private Frequency m_freqOut;
    private Conductance m_conductOut;


    private int type;

    String varName;

    public FloatConversionNode(VariableManager vm, String vnName, int type, String nodeType) {
        super(vm, vnName, nodeType);

        this.type = type;

        // Name must be the same as the one given in CSL file
        varName = vnName + ".Input";

        m_varInput = new FloatType(vm, varName, false, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this));
                }
                public void deliver(double d){
                    handleMessageAt_Input(d);
                }
                
            };


        // Add to the variable manager
        addVariable(varName, m_varInput);

        instantiateVariables(vnName, vm);
        
        
    }

    private void setOutput(double d) {
        
        switch(type) {

        case CalculationComponent.INT:
            m_intOut.onEvent((int)d);
            PackageLogger.log.fine("Set Output to '" + (int)d + "'");
            break;
            
        case CalculationComponent.STRING:
            String s = String.valueOf(d);
            m_stringOut.onEvent(s);
            PackageLogger.log.fine("Set Output to '" + s + "'");
            break;

        case CalculationComponent.BOOL:
            boolean b = false;
            if (d > 0) {
                b = true;
            }
            m_boolOut.onEvent(b);
            PackageLogger.log.fine("Set Output to '" + b + "'");
            break;

        case CalculationComponent.TIME:
            m_timeOut.onEvent(d);
            PackageLogger.log.fine("Set Output to '" + d + "'");
            break;

        case CalculationComponent.CAPACITANCE:
            m_capOut.onEvent(d);
            PackageLogger.log.fine("Set Output to '" + d + "'");
            break;

        case CalculationComponent.VOLTAGE:
            m_voltageOut.onEvent(d);
            PackageLogger.log.fine("Set Output to '" + d + "'");
            break; 

        case CalculationComponent.CURRENT:
            m_currentOut.onEvent(d);
            PackageLogger.log.fine("Set Output to '" + d + "'");
            break;
 
        case CalculationComponent.FREQUENCY:
            m_freqOut.onEvent(d);
            PackageLogger.log.fine("Set Output to '" + d + "'");
            break;

        case CalculationComponent.CONDUCTANCE:
            m_conductOut.onEvent(d);
            PackageLogger.log.fine("Set Output to '" + d + "'");


        }
    }

   
    private void instantiateVariables(String vnName, VariableManager vm) {

        switch(type) {

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
            break;

        case CalculationComponent.TIME:

            String timeName = vnName + ".Output";
            
            m_timeOut = new Time(vm, timeName, false, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(double d){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(timeName, m_timeOut);
            break;

        case CalculationComponent.CAPACITANCE:
            
            String capName = vnName + ".Output";
            
            m_capOut = new Capacitance(vm, capName, false, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(double d){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(capName, m_capOut);
            break;
            
        case CalculationComponent.VOLTAGE:
            
            String voltageName = vnName + ".Output";
            
            m_voltageOut = new Voltage(vm, voltageName, false, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(double d){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(voltageName, m_voltageOut);
            break;

        case CalculationComponent.CURRENT:
            
            String currentName = vnName + ".Output";
            
            m_currentOut = new Current(vm, currentName, false, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(double d){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(currentName, m_currentOut);
            break;

        case CalculationComponent.FREQUENCY:
            
            String freqName = vnName + ".Output";
            
            m_freqOut = new Frequency(vm, freqName, false, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(double d){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(freqName, m_freqOut);
            break;

        case CalculationComponent.CONDUCTANCE:
            
            String conductName = vnName + ".Output";
            
            m_conductOut = new Conductance(vm, conductName, false, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(double d){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(conductName, m_conductOut);
            
        }
    }
       




     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at FloatNode: " 
			       + getVNName());
        
        if (m.hasField("Input")) {

            try {

                Double d = new Double(m.getField("Input"));
                
                m_varInput.onEvent(d);
                setOutput(d.doubleValue());

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
     * Process the receipt of a message at the FloatNode Output variable.
     *
     *
     */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }



   //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the FloatConversionNode Input variable.
     *
     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     *
     */

    public void handleMessageAt_Input(Message m, Variable v, boolean diff) { 
	PackageLogger.log.fine("Received message at Input variable!"); 
        
	Double d = m_varInput.extractData(m);
        if (d != null ) {
            handleMessageAt_Input(d.doubleValue());
        }  else {
            PackageLogger.log.warning("Null value received at " + varName);
        }	
    }

     /**
     * Process the receipt of a message at the FloatConversionNode Input variable.
     *
     * @param d double value received
     *
     */
    public void handleMessageAt_Input(double d){ 
	PackageLogger.log.fine("Received message at Input variable!");
             
        //m_varInput.onEvent(d);

        setOutput(d);

       
        
    }
}
