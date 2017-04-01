/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     11/2004
    Purpose:  Handles Motor node functions
    Software: GCC to compile
    Hardware: Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#include "ir.h"

const char pm_ir_num[] PROGMEM = ".num";
const char pm_ir_prox[] PROGMEM = ".prox";
#ifdef DEBUG
const char pm_ir_sens[] PROGMEM = ".sens";
#endif
const char pm_ir_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_irVarsVNNames[NUM_IR_VARS+1] PROGMEM = {pm_ir_num, pm_ir_prox, 
						 #ifdef DEBUG
						 pm_ir_sens, 
						 #endif
						 pm_ir_end};

//basic variables
ring * irVNNames[MAX_IR+1];
t_int irVnids [MAX_IR*NUM_IR_VARS];
t_int irVnidsTypes[NUM_IR_VARS];
t_int irFuses;

//local variables
IRSENSOR *sensor;

t_int ir_getNum(t_int which)
{
	if GET_BIT(irFuses, which)
	{
		return intVars[irVnids[IR_NUM]];
	}
	return 0;
}

void ir_setProx(t_int which, t_int prox)	//sets nrs ir prox variable
{
	if GET_BIT(irFuses, which)
	{
		intVars[irVnids[IR_PROX]] = prox;		
	}
}

#ifdef DEBUG
t_int ir_getProx(t_int which)
{
	if GET_BIT(irFuses, which)
	{
		return intVars[irVnids[IR_PROX]];		//IR_PROX = which*NUM_IR_VARS+1
	}
	return 0;
}

s08 irCheckVoidEvent (t_int which)
{
	if GET_BIT(irFuses, which)
	{
		return getVoidEvent(irVnids[IR_SENS]);
	}
	return 0;
}
#endif

void ir_update()
{
	t_int which;
	t_int tmpir;
	t_int tmpprox;
	
	for(tmpir = 0;tmpir<MAX_IR;tmpir++)			//for each ir
	{	
		which = ir_getNum(tmpir);			//get the ir number
		
		tmpprox = (*sensor).oProximitySensor[which-1];	//get prox value, offset it
		
		if ((which > 0) && (which <= MAX_IR))	// if a valid ir number
		{				
			ir_setProx(tmpir, tmpprox);	//set nrs value to current sensor value

			#ifdef DEBUG			
			if ( irCheckVoidEvent(tmpir) )	// if a void event occurs
			{
				printf("IR");		
				printf("%d ",tmpir);
				printf("recorded: ");
				printf("%d ",ir_getProx(tmpir));
				printf("actual: ");
				printf("%d\n",(*sensor).oProximitySensor[which-1]);
			}
			#endif
		}
	}
}

void ir_ini()
{
	ini_node(IR_VNIDPOS);
	
	irVnidsTypes[0] = INT_VAR;	//num
	irVnidsTypes[1] = INT_VAR;	//prox
	#ifdef DEBUG
	irVnidsTypes[2] = VOID_VAR;	//sens
	#endif
	sens_reset (); //init the resources of the sense manager
		
	sensor = sens_get_pointer();	//get sensor pointer
	
    	#ifdef DEBUG
	println ("<INI IR NODE>");
    	#endif
}






