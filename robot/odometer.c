/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     11/2004
    Purpose:  Handles odometer node functions
    Software: GCC to compile
    Hardware: Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#include "odometer.h"

const char pm_odo_num[] PROGMEM = ".num";
const char pm_odo_count[] PROGMEM = ".count";
const char pm_odo_setval[] PROGMEM = ".setval";
const char pm_odo_set[] PROGMEM = ".set";
const char pm_odo_reset[] PROGMEM = ".reset";
#ifdef DEBUG
const char pm_odo_sens[] PROGMEM = ".sens";
#endif
const char pm_odo_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_odoVarsVNNames[NUM_ODO_VARS+1] PROGMEM = {pm_odo_num, pm_odo_count, pm_odo_setval, 
						   pm_odo_set, pm_odo_reset,
						   #ifdef DEBUG 
						   pm_odo_sens, 
						   #endif
						   pm_odo_end};

//basic variables
ring * odoVNNames[MAX_ODO+1];
t_int odoVnids [MAX_ODO*NUM_ODO_VARS];
t_int odoVnidsTypes[NUM_ODO_VARS];
t_int odoFuses;

t_int odo_getNum(t_int which)
{
	if GET_BIT(odoFuses, which)
	{
		return intVars[odoVnids[ODO_NUM]];
	}
	return 0;
}

void odo_setCount(t_int which, t_int count)
{
	if GET_BIT(odoFuses, which)
	{
		intVars[odoVnids[ODO_COUNT]] = count;		
	}
}

t_int odo_getSetval(t_int which)
{
	if GET_BIT(odoFuses, which)
	{
		return intVars[odoVnids[ODO_SETVAL]];
	}
	return 0;
}

s08 odoCheckSet (t_int which)
{
	if GET_BIT(odoFuses, which)
	{
		return getVoidEvent(odoVnids[ODO_SET]);
	}
	return 0;
}

s08 odoCheckReset (t_int which)
{
	if GET_BIT(odoFuses, which)
	{
		return getVoidEvent(odoVnids[ODO_RESET]);
	}
	return 0;
}

#ifdef DEBUG
t_int odo_getCount(t_int which)
{
	if GET_BIT(odoFuses, which)
	{
		return intVars[odoVnids[ODO_COUNT]];
	}
	return 0;
}

s08 odoCheckVoidEvent (t_int which)
{
	if GET_BIT(odoFuses, which)
	{
		return getVoidEvent(odoVnids[ODO_SENS]);
	}
	return 0;
}
#endif


void odo_update()
{
	t_int which;
	t_int tmpodo;
	t_int tmpcount;
	
	for(tmpodo = 0;tmpodo<MAX_ODO;tmpodo++)			//for each odo
	{
		
		which = odo_getNum(tmpodo);			//get the odo number
		
		
		if ((which > 0) && (which <= MAX_ODO))	// if a valid odo number
		{	
		
			if ( odoCheckReset(tmpodo) )	// if reset is sent
			{
				mot_put_sensors_1m(which-1,0);	//set counters to 0
				
				#ifdef DEBUG
				printf("ODO");		
				printf("%d ",tmpodo);
				printf("reset\n");
				printf("current value: ");
				printf("%d\n", (t_int)mot_get_position(which-1));
				#endif
			}
			
			if ( odoCheckSet(tmpodo) )	// if set is sent
			{
				mot_put_sensors_1m(which-1,odo_getSetval(tmpodo));	//set counters
				
				#ifdef DEBUG
				printf("ODO");		
				printf("%d ",tmpodo);
				printf("reqd value: ");
				printf("%d ", odo_getSetval(tmpodo));
				printf("actual: ");
				printf("%d\n", (t_int)mot_get_position(which-1));
				#endif
			}
			
			tmpcount = mot_get_position(which-1);	//get count value
			
			odo_setCount(tmpodo, tmpcount);	//set nrs value to current actual value
			
			#ifdef DEBUG
			if ( odoCheckVoidEvent(tmpodo) )	// if a void event occurs (sens)
			{
				printf("ODO");		
				printf("%d ",tmpodo);
				printf("recorded: ");
				printf("%d ", odo_getCount(tmpodo));
				printf("actual: ");
				printf("%d\n",tmpcount);
			}
			#endif
		}
	}
}

void odo_ini()
{
	ini_node(ODO_VNIDPOS);
	
	odoVnidsTypes[0] = INT_VAR;	//num
	odoVnidsTypes[1] = INT_VAR;	//count
	odoVnidsTypes[2] = INT_VAR;	//setval
	odoVnidsTypes[3] = VOID_VAR;	//set
	odoVnidsTypes[4] = VOID_VAR;	//reset
	#ifdef DEBUG
	odoVnidsTypes[5] = VOID_VAR;	//sens
	#endif
	
	mot_put_sensors_2m(0,0);	//initialise sensors to zero
	
    	#ifdef DEBUG
	println ("<INI ODO NODE>");
    	#endif
	

}






