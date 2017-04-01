/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     11/2004
    Purpose:  Handles Variable Multiplication and Addition functions
    Software: GCC to compile
    Hardware: Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#include "delta.h"

const char pm_delta_num[] PROGMEM = ".num";
const char pm_delta_del[] PROGMEM = ".del";
const char pm_delta_in[] PROGMEM = ".in";
const char pm_delta_out[] PROGMEM = ".out";
#ifdef DEBUG
const char pm_delta_sens[] PROGMEM = ".sens";
#endif
const char pm_delta_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_deltaVarsVNNames[NUM_DELTA_VARS+1] PROGMEM = {pm_delta_num, pm_delta_del, 
						       pm_delta_in,  pm_delta_out, 
						       #ifdef DEBUG
						       pm_delta_sens, 
						       #endif
						       pm_delta_end};

//basic variables
ring * deltaVNNames[MAX_DELTA+1];
t_int deltaVnids [MAX_DELTA*NUM_DELTA_VARS];
t_int deltaVnidsTypes[NUM_DELTA_VARS];
t_int deltaFuses;

//local variables
t_int last_out[MAX_DELTA];

//nrs accessors
t_int delta_getNum(t_int which)
{
	if GET_BIT(deltaFuses, which)
	{
		return intVars[deltaVnids[DELTA_NUM]];
	}
	return 0;
}

t_int delta_getDel(t_int which)
{
	if GET_BIT(deltaFuses, which)
	{
		return intVars[deltaVnids[DELTA_DEL]];
	}
	return 0;
}

t_int delta_getIn(t_int which)
{
	if GET_BIT(deltaFuses, which)
	{
		return intVars[deltaVnids[DELTA_IN]];
	}
	return 0;
}

void delta_setOut(t_int which, t_int out)	//sets nrs delta prox variable
{
	if GET_BIT(deltaFuses, which)
	{
		intVars[deltaVnids[DELTA_OUT]] = out;		
	}
}

#ifdef DEBUG
//out accessor (for debugging)
t_int delta_getOut(t_int which)
{
	if GET_BIT(deltaFuses, which)
	{
		return intVars[deltaVnids[DELTA_OUT]];		
	}
	return 0;
}
//sens void (for debugging)
s08 deltaCheckVoidEvent (t_int which)
{
	if GET_BIT(deltaFuses, which)
	{
		return getVoidEvent(deltaVnids[DELTA_SENS]);
	}
	return 0;
}
#endif

void delta_update()
{
	t_int which;
	t_int tmpdelta;
	t_int tmpdel;
	t_int in;
	
	for(tmpdelta = 0;tmpdelta<MAX_DELTA;tmpdelta++)		//for each delta
	{
		which = delta_getNum(tmpdelta);			//get the delta number
		
		if ((which > 0) && (which <= MAX_DELTA))	// if a valid delta number (i.e. being used)
		{					
			tmpdel = delta_getDel(tmpdelta);
			in = delta_getIn(tmpdelta);
			
			if(abs(in - last_out[tmpdelta]) > tmpdel)
			{
				delta_setOut(tmpdelta, in);
				last_out[tmpdelta] = in;
			}
			
			#ifdef DEBUG			
			if ( deltaCheckVoidEvent(tmpdelta) )	// if a void event occurs
			{
				printf("DELTA");		
				printf("%d: ",tmpdelta);
				printf("[del in out]\n");
				printf("%d ",delta_getDel(tmpdelta));
				printf("%d ",delta_getIn(tmpdelta));
				printf("%d\n",delta_getOut(tmpdelta));
			}
			#endif
		}
	}
}

void delta_ini()
{
	ini_node(DELTA_VNIDPOS);
	
	deltaVnidsTypes[0] = INT_VAR;	//num
	deltaVnidsTypes[1] = INT_VAR;	//del
	deltaVnidsTypes[2] = INT_VAR;	//in
	deltaVnidsTypes[3] = INT_VAR;	//out
	#ifdef DEBUG
	deltaVnidsTypes[4] = VOID_VAR;	//sens
	#endif
	
	for(t_int i = 0; i<MAX_DELTA; i++)		//init local variables
	{
		last_out[i] = 0;
	}
	
    	#ifdef DEBUG
	println ("<INI DELTA NODE>");
    	#endif
}

