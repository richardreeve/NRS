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
import nrs.core.type.IntegerType;
import nrs.core.type.FloatType;

import java.util.Timer;
import java.util.TimerTask;

/** Represents DelayNodes for the calculation component.  This class
 * inherits from {@link nrs.core.base.Node} to represent a node of the
 * NRS component.
 *
 * @author Thomas French
 * @author Sarah Cope
*/

public class DelayNode extends Node {

     // Void Input and output
    private VoidType m_voidInput, m_voidOut;

     // Int Input and output
    private IntegerType m_intInput, m_intOut;

     // Float Input and output
    private FloatType m_floatInput, m_floatOut;

    // Boolean Input and output
    private BooleanType m_booleanInput, m_booleanOut;

    // ms delay
    private IntegerType m_delay;
    
    // Names of inputs
    private String varName, delayName;

    private int type;

    private Timer timer;

    private long delay;


    public DelayNode(VariableManager vm, String vnName, int type, String nodeType) {
        super(vm, vnName, nodeType);

        this.type = type;

        instantiateVariables(vnName, vm);

        delayName = vnName + ".msDelay";

        m_delay = new IntegerType(vm, delayName, true, false){
                public void deliver(Message m)
                {
                    handleMessageAt_Input(m, this, checkType(m, this), delayName);
                }
                public void deliver(int i){
                    handleMessageAt_Input(i, delayName);
                }
                
            };
        
        // Add to the variable manager
        addVariable(delayName, m_delay);

        
    }


    class DelayTask extends TimerTask {
        public void run() {
            switch(type) {
            
            case CalculationComponent.VOID:
                m_voidOut.onEvent();
                break;
            
            case CalculationComponent.FLOAT:
                m_floatOut.onEvent(m_floatInput.getValue().doubleValue());
                break;
                
            case CalculationComponent.INT:
                m_intOut.onEvent(m_intInput.getValue().intValue());  
                break;
                
            case CalculationComponent.BOOL:
                m_booleanOut.onEvent(m_booleanInput.getValue().booleanValue());
            }
            PackageLogger.log.fine("\nSet Output");
	    timer.cancel(); //Terminate the timer thread
        }
    }
 

   
    private void instantiateVariables(String vnName, VariableManager vm) {
        
        switch(type) {
            
        case CalculationComponent.VOID:
            
            // Name must be the same as the one given in CSL file
            varName = vnName + ".Input";
            
            m_voidInput = new VoidType(vm, varName){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Input(m, this, checkType(m, this), varName);
                    }
                    public void deliver()
                    {
                        handleMessageAt_Input();
                    }
                };
            
            
            // Add to the variable managercd ../
            addVariable(varName, m_voidInput);
            
            String varName1 = vnName + ".Output";
            
            m_voidOut = new VoidType(vm, varName1){
                    public void deliver(Message m)
		{
		    handleMessageAt_Output();
		}
                    public void deliver()
                    {
                        handleMessageAt_Output();
                    }
                };
            
            
            // Add to the variable manager
            addVariable(varName1, m_voidOut);
            
            break;
            
        case CalculationComponent.FLOAT:

            varName = vnName + ".Input";

            m_floatInput = new FloatType(vm, varName, true, false){
                    public void deliver(Message m)
                    {
                        handleMessageAt_Input(m, this, checkType(m, this), varName);
                    }
                    public void deliver(double d){
                        handleMessageAt_Input(d);
                    }
                    
            };
            
            // Add to the variable manager
            addVariable(varName, m_floatInput);
                        String varName2 = vnName + ".Output";
            
            m_floatOut = new FloatType(vm, varName2, false, false){
                    public void deliver(Message m)
                    {
		    handleMessageAt_Output();
                    }
                    public void deliver(double d){
                        
                        handleMessageAt_Output();
                    }
                    
                };
        
            // Add to the variable manager
            addVariable(varName2, m_floatOut);

            break;

        case CalculationComponent.INT:

                varName = vnName + ".Input";

                m_intInput = new IntegerType(vm, varName, true, false){
                        public void deliver(Message m)
                        {
                            handleMessageAt_Input(m, this, checkType(m, this), varName);
                        }
                        public void deliver(int i){
                            handleMessageAt_Input(i, varName);
                        }
                        
                    };
                
                // Add to the variable manager
                addVariable(varName, m_intInput);
                
                String varName3 = vnName + ".Output";
                
                m_intOut = new IntegerType(vm, varName3, false, false){
                        public void deliver(Message m)
                        {
                            handleMessageAt_Output();
                        }
                        public void deliver(int i){
                            
                            handleMessageAt_Output();
                        }
                        
                    };
                
                // Add to the variable manager
                addVariable(varName3, m_intOut);
                
                break;

        case CalculationComponent.BOOL:

                varName = vnName + ".Input";

                m_booleanInput = new BooleanType(vm, varName, true, false){
                        public void deliver(Message m)
                        {
                            handleMessageAt_Input(m, this, checkType(m, this), varName);
                        }
                        public void deliver(boolean b){
                            handleMessageAt_Input(b);
                        }
                        
                    };
                
                // Add to the variable manager
                addVariable(varName, m_booleanInput);
                
                String varName4 = vnName + ".Output";
                
                m_booleanOut = new BooleanType(vm, varName4, false, false){
                        public void deliver(Message m)
                        {
                            handleMessageAt_Output();
                        }
                        public void deliver(boolean b){
                            
                            handleMessageAt_Output();
                        }
                        
                    };
                
                // Add to the variable manager
                addVariable(varName4, m_booleanOut);
        }
    }
 
     //----------------------------------------------------------------------//
     /** Deliver a {@link Message} to this <code>Node</code>.
     *
     * @param m Message to deliver to <code>Node</code>.
     */
    public void deliver(Message m){
	PackageLogger.log.fine("Received message at DelayNode: " 
			       + getVNName());   

        if (m.hasField("msDelay")) {
            
            try {
                
                 Integer i = new Integer(m.getField("msDelay"));
                 m_delay.setValue(i.intValue());
                 delay = (long) m_delay.getValue();

                 PackageLogger.log.fine("Set delay to " + delay + " ms.");
                 
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
     * Process the receipt of a message at the DelayNode Output variable.
     *
     *
     */
    public void handleMessageAt_Output(){
	PackageLogger.log.warning("Received message at Output variable."
				  +" This should not happen!");
    }



   //-----------------------------------------------------------------------//
    /**
     * Process the receipt of a message at the DelayNode Input variable.
     *

     * @param m the {@link Message} received
     * @param v the {@link Variable} receiving the message
     * @param diff true if <tt>m</tt> and <tt>v</tt> are of different
     * types
     *
     */

    public void handleMessageAt_Input(Message m, Variable v, boolean diff, String name) { 
	PackageLogger.log.fine("Received message at Input variable!");

        if (m.getType().equals("boolean")) {
            Boolean b = m_booleanInput.extractData(m);
            if (b != null ) {
                handleMessageAt_Input(b.booleanValue());
            }

        } else if (m.getType().equals("float")) {
            Double d = m_floatInput.extractData(m);
            if (d != null) {
                handleMessageAt_Input(d.doubleValue());
            }

        } else if (m.getType().equals("integer")) {
            Integer i;
            if(name.equals(delayName)) {
                i = m_delay.extractData(m);
            } else {
                i = m_intInput.extractData(m);
            }
            if (i != null) {
                handleMessageAt_Input(i.intValue(), name);
            }
        } else if (m.getType().equals("void") ) {
            handleMessageAt_Input();

        } else {
            PackageLogger.log.warning("Message type not handled by this node.");
        }
        
    }
        


    
     /**
     * Process the receipt of a message at the DelayNode Input variable.
     *
     * @param b boolean value received
     *
     */
    public void handleMessageAt_Input(boolean b){ 
	PackageLogger.log.fine("Received message at Input variable!");
        m_booleanInput.setValue(b);

        timer = new Timer();
        timer.schedule(new DelayTask(), delay);
        
    }

    public void handleMessageAt_Input(double d){ 
	PackageLogger.log.fine("Received message at Input variable!");

        m_floatInput.setValue(d);

        timer = new Timer();
        timer.schedule(new DelayTask(), delay);
        
        
     }

     public void handleMessageAt_Input(int i, String name){ 
	PackageLogger.log.fine("Received message at Input variable!");

        if (name.equals(delayName)) {
            m_delay.setValue(i);
            delay = (long)i;
        } else {
            m_intInput.setValue(i);
            
            timer = new Timer();
            timer.schedule(new DelayTask(), delay);
        }
     }



     public void handleMessageAt_Input(){ 
	PackageLogger.log.fine("Received message at Input variable!");

        timer = new Timer();
        timer.schedule(new DelayTask(), delay);
     }
}
