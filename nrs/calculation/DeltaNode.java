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

/** Represents DeltaNodes for the calculation component.  This class
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

import nrs.core.type.FloatType;


public class DeltaNode extends Node {

     // Input
    private FloatType m_varInput, m_delta;

    // Output
    private FloatType m_varOut;
    
    // Names of inputs
    private String varNameIn, varNameOut, deltaName;

    public DeltaNode(VariableManager vm, String vnName, String nodeType) {
        super(vm, vnName, nodeType);

        // Name must be the same as the one given in CSL file
        varNameIn = vnName + ".Input";

        m_varInput = new FloatType(vm, varNameIn, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), varNameIn);
                }
                public void deliver(double d){
                    handleMessageAt_Input(d, varNameIn);
                }
                
            };

        // Add to the variable manager
        addVariable(varNameIn, m_varInput);

        deltaName = vnName + ".Delta";

        m_delta = new FloatType(vm, deltaName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), deltaName);
                }
                public void deliver(double d){
                    handleMessageAt_Input(d, deltaName);
                }
                
            };


        // Add to the variable manager
        addVariable(deltaName, m_delta);

       
        varNameOut = vnName + ".Output";

        m_varOut = new FloatType(vm, varNameOut, true, false){
		public void deliver(Message m)
		{
		    handleMessageAt_Output();
		}
		public void deliver(double d){
                    
		    handleMessageAt_Output();
		}
		
	    };

        // Add to the variable manager
        addVariable(varNameOut, m_varOut);

        
    }

    
     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at DeltaNode: " 
			       + getVNName());     

         if (m.hasField("Input") && m.hasField("Delta")) {

             try {

                 Double d = new Double(m.getField("Input"));
                 Double d2 = new Double(m.getField("Delta"));
                 
                 m_varInput.setValue(d);
                 m_delta.setValue(d2);
             } 
             catch (NumberFormatException nfe){
                 PackageLogger.log.warning("Error with message - not the right"
                                           + " format!");
                 return;
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
     * Process the receipt of a message at the DeltaNode Output variable.
     *
     *
     */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }


     //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the DeltaNode Input variable.
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
        if (varName.equals(varNameIn)) {
            
            d = m_varInput.extractData(m);
            
        } else {
            
            d = m_delta.extractData(m);
        }

        if (d != null ) { 
            handleMessageAt_Input(d.doubleValue(), varName);
        } else {
            PackageLogger.log.warning("Null value received at " + varName);
        }
    }

    
     /**
     * Process the receipt of a message at the DeltaNode Input variable.
     *
     * @param d double value received
     * @param varName the name of the input variable the message was received on
     *
     */
    public void handleMessageAt_Input(double d, String varName){ 

         if (varName.equals(varNameIn)) {
             
            m_varInput.setValue(d);

        } else {
            
            m_delta.setValue(d);
        }

        PackageLogger.log.fine("Set " + varName + " to " + d);
        
        double out = m_varOut.getValue().doubleValue();
        double delta = m_delta.getValue().doubleValue();

        double diff;

        if (d > out) {
            diff = d - out;
        } else {
            diff = out - d;
        }
        
        PackageLogger.log.fine("Delta = " + delta + ", |Output-Input| = " + diff); 

        // Set output to input if there is a significant difference
        if (diff >= delta) {
            
            out = d;
            PackageLogger.log.fine("Set " + varNameOut + " to " + out);
            // Set Add variable to the output
            m_varOut.setValue(out);

        }
        
	
        
        
    }
}
