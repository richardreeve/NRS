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
import nrs.core.type.VoidType;
import nrs.core.type.IntegerType;


/** Represents ForLoopNodes for the calculation component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/

public class ForLoopNode extends Node {

     // Values
    private FloatType m_floatFrom, m_floatTo, m_floatAt;

    private IntegerType m_intFrom, m_intTo, m_intAt;

    // Boolean flags
    private BooleanType m_allFinished;

    private VoidType m_reset, m_next;
    
    // Names 
    private String fromName, toName, atName, resetName, nextName, allFinName;

    private int from;
    private int to;
    private int at;

    private int type;


    public ForLoopNode(VariableManager vm, String vnName, int type, String nodeType) {
        super(vm, vnName, nodeType);

        this.type = type;

        instantiateVariables(vnName, vm);

        resetName = vnName + ".Reset";

        m_reset =  new VoidType(vm, resetName){
		public void deliver(Message m)
		{
		    handleMessageAt_Input(m, this, checkType(m, this), resetName);   
		}
		public void deliver()
		{
		    handleMessageAt_Input(resetName);
		}
	    };


        // Add to the variable manager
        addVariable(resetName, m_reset);


        nextName = vnName + ".Next";

        m_next =  new VoidType(vm, nextName){
		public void deliver(Message m)
		{
		    handleMessageAt_Input(m, this, checkType(m, this), nextName);
		}
		public void deliver()
		{
		    handleMessageAt_Input(nextName);
		}
	    };

        // Add to the variable manager
        addVariable(nextName, m_next);

       
        allFinName = vnName + ".AllFinished";

        m_allFinished = new BooleanType(vm, allFinName, true, false){
		public void deliver(Message m)
		{
		    handleMessageAt_Output();
		}
		public void deliver(boolean b){
                    
		    handleMessageAt_Output();
		}
		
	    };

        m_allFinished.setDefault(false);

        // Add to the variable manager
        addVariable(allFinName, m_allFinished);
        
        
    }

    private void instantiateVariables(String vnName, VariableManager vm) {
        
        switch(type) {
            
        case CalculationComponent.FLOAT:

            // Name must be the same as the one given in CSL file
            fromName = vnName + ".From";
            
            m_floatFrom = new FloatType(vm, fromName, true, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Input(m, this, checkType(m, this), fromName);
                    }
                    public void deliver(double d){
                        handleMessageAt_Input(d, fromName);
                    }
                    
                };
            
            
            // Add to the variable manager
            addVariable(fromName, m_floatFrom);
            
            toName = vnName + ".To";
            
            m_floatTo = new FloatType(vm, toName, true, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Input(m, this, checkType(m, this), toName);
                    }
                    public void deliver(double d){
                        handleMessageAt_Input(d, toName);
                    }
                    
                };
            
            
            // Add to the variable manager
            addVariable(toName, m_floatTo);
            
            
            atName = vnName + ".At";
            
            m_floatAt = new FloatType(vm, atName, true, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Output();
                    }
                    public void deliver(double d){
                        handleMessageAt_Output();
                    }
                    
                };
            
            
            // Add to the variable manager
            addVariable(atName, m_floatAt);

            break;
        
            case CalculationComponent.INT:

                // Name must be the same as the one given in CSL file
                fromName = vnName + ".From";
            
                m_intFrom = new IntegerType(vm, fromName, true, false){
                        public void deliver(Message m)
                        {
                            handleMessageAt_Input(m, this, checkType(m, this), fromName);
                        }
                        public void deliver(int i){
                            handleMessageAt_Input(i, fromName);
                        }
                        
                    };
                
                
                // Add to the variable manager
                addVariable(fromName, m_intFrom);
                
                toName = vnName + ".To";
                
                m_intTo = new IntegerType(vm, toName, true, false){
                        public void deliver(Message m)
                        {
                            handleMessageAt_Input(m, this, checkType(m, this), toName);
                        }
                        public void deliver(int i){
                            handleMessageAt_Input(i, toName);
                        }
                        
                    };
                
                
                // Add to the variable manager
                addVariable(toName, m_intTo);
                
                
                atName = vnName + ".At";
                
                m_intAt = new IntegerType(vm, atName, true, false){
                        public void deliver(Message m)
                        {
                            handleMessageAt_Output();
                        }
                        public void deliver(int i){
                            handleMessageAt_Output();
                        }
                        
                    };
                
                
                // Add to the variable manager
                addVariable(atName, m_intAt);
                
        }
    }
    


     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at ForLoopNode: " 
			       + getVNName());   

         if (m.hasField("From") && m.hasField("To")) {
             
             try {
                 
                 switch(type) {
                     
                 case CalculationComponent.FLOAT:

                     Double d = new Double(m.getField("From"));
                     Double d2 = new Double(m.getField("To"));
                     
                     m_floatFrom.setValue(d);
                     from = d.intValue();
                     m_floatTo.setValue(d2);
                     to = d2.intValue();
                     break;
                     
                 case CalculationComponent.INT:
            
                     Integer i = new Integer(m.getField("From"));
                     Integer i2 = new Integer(m.getField("To"));
                     
                     m_intFrom.setValue(i);
                     from = i;
                     m_intTo.setValue(i2);
                     to = i2;
                 }
                 
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


    private void runExp() {
        
        if (!m_allFinished.getValue().booleanValue()){

            if (at < to) {
 
                if (type == CalculationComponent.INT) {
                    m_intAt.setValue(at);
                } else {
                    m_floatAt.setValue((double)at);
                }
                ++at;

            } else {
                m_allFinished.setValue(true);
                PackageLogger.log.fine("\nAll Iterations Finished");
            }
        } else {
            PackageLogger.log.fine("\nAll Iterations Finished");
        }
        
        
    }


    
    /**
     * Process the receipt of a message at the ForLoopNode Output variable.
     *
     *
     */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }



   //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the ForLoopNode Input variable.
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
        
        if (m.getType().equals("float")) {
            Double d;
            if (varName.equals(fromName)) {
                d = m_floatFrom.extractData(m);
            } else {
                d = m_floatTo.extractData(m);
            }
            if (d != null) {
                handleMessageAt_Input(d.doubleValue(), varName);
            }
            
        } else if (m.getType().equals("integer")) {
            Integer i;
            if (varName.equals(fromName)) {
                i = m_intFrom.extractData(m);
            } else {
                i = m_intTo.extractData(m);
            }
            if ( i != null) {
                handleMessageAt_Input(i.intValue(), varName);
            }
        } else if (m.getType().equals("void") ) {
            handleMessageAt_Input(varName);
            
        } else {
            PackageLogger.log.warning("Message type not handled by this node.");
        }
    
    }

    public void handleMessageAt_Input(String varName) {


        if (varName.equals(resetName)) {


            if (type == CalculationComponent.INT) {
                
                m_intFrom.setValue(0);
                from = m_intFrom.getValue();
                to = m_intTo.getValue();
                at = from;
                m_intAt.setValue(at);
            } else {

                m_floatFrom.setValue(0.0);
                from = (int) m_floatFrom.getValue().doubleValue();
                to = (int) m_floatTo.getValue().doubleValue();
                at = from;
                m_floatAt.setValue((double)at);
            }

            m_allFinished.setValue(false);

        } else if (varName.equals(nextName)) {

            runExp();
        }
        
        
    }


    
     /**
     * Process the receipt of a message at the ForLoopNode Input variable.
     *
     * @param b boolean value received
     * @param varName the name of the input variable the message was received on
     *
     */
    public void handleMessageAt_Input(boolean b, String varName){ 
	PackageLogger.log.fine("Received message at ForLoop Input variable!");

        m_allFinished.setValue(b);
    }

    /**
     * Process the receipt of a void message at the ForLoopNode Input variable
     */

    public void handleMessageAt_Input(double d, String varName){ 
	PackageLogger.log.fine("Received message at ForLoop Input variable!");
        
        if (varName.equals(toName)) {
            
            m_floatTo.setValue(d);
            to = (int)d;
            
        } else {
            
            m_floatFrom.setValue(d);
            from = (int)d;
        }
       
    }         
            
     /**
     * Process the receipt of a void message at the ForLoopNode Input variable
     */

    public void handleMessageAt_Input(int i, String varName){ 
	PackageLogger.log.fine("Received message at ForLoop Input variable!");
        
        if (varName.equals(toName)) {
            
            m_intTo.setValue(i);
            to = i;
            
        } else {
            
            m_intFrom.setValue(i);
            from = i;
        }
       
    }     

                
                
                
            
}
