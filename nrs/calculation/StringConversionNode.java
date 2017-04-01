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
import nrs.core.type.StringType;
import nrs.core.unit.Filename;
import nrs.core.type.StringRestriction;

/** Represents FloatConversionNodes for the calculation component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/

public class StringConversionNode extends Node {
    

    // Input
    private StringType m_Input;

    // Outputs
    private Filename m_fileOut;
   

    private int type;

    private String varName;

    public StringConversionNode(VariableManager vm, String vnName, int type, String nodeType) {
        super(vm, vnName, nodeType);

        this.type = type;

        // Name must be the same as the one given in CSL file
        varName = vnName + ".Input";

        m_Input = new StringType(vm, varName, false, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this));
                }
                public void deliver(String s){
                    handleMessageAt_Input(s);
                }
                
            };


        // Add to the variable manager
        addVariable(varName, m_Input);

        instantiateVariables(vnName, vm);
        
        
    }

    private void setOutput(String s) {
        
        switch(type) {

        case CalculationComponent.FILENAME:

            StringRestriction st = m_fileOut.getRestriction();
            if(st.valid(s)){
                
                m_fileOut.onEvent(s);
                PackageLogger.log.fine("Set Output to '" + s + "'");
            } else {
                PackageLogger.log.warning("Input string does not meet restrictions");
            }

        }
    }

   
    private void instantiateVariables(String vnName, VariableManager vm) {

        switch(type) {

      
        case CalculationComponent.FILENAME:
            
            String fileName = vnName + ".Output";

            m_fileOut = new Filename(vm, fileName, false, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(String s){
                        
                        handleMessageAt_Output();
                    }
                    
                };
            
            // Add to the variable manager
            addVariable(fileName, m_fileOut);

       
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

                String s = m.getField("Input");
                
                m_Input.onEvent(s);
                setOutput(s);

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
        
	String s = m_Input.extractData(m);
        if (s != null ) {
            handleMessageAt_Input(s);
        }  else {
            PackageLogger.log.warning("Null value received at " + varName);
        }	
    }

     /**
     * Process the receipt of a message at the FloatConversionNode Input variable.
     *
     * @param s String value received
     *
     */
    public void handleMessageAt_Input(String s){ 
	PackageLogger.log.fine("Received message at Input variable!");
        setOutput(s);
        
    }
}
