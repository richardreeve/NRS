/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     11/2004
    Purpose:  Handles Variable Multiplication and Addition functions
    Software: GCC to compile
    Hardware: Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#include "integrator.h"

const char pm_integ_num[] PROGMEM = ".num";
const char pm_integ_dps[] PROGMEM = ".dps";
const char pm_integ_x[] PROGMEM = ".x";
const char pm_integ_y[] PROGMEM = ".y";
const char pm_integ_z[] PROGMEM = ".z";
const char pm_integ_out[] PROGMEM = ".out";
#ifdef DEBUG
const char pm_integ_sens[] PROGMEM = ".sens";
#endif
const char pm_integ_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_integVarsVNNames[NUM_INTEG_VARS+1] PROGMEM = {pm_integ_num, pm_integ_dps, 
						       pm_integ_x, pm_integ_y, pm_integ_z, 
						       pm_integ_out, 
						       #ifdef DEBUG
						       pm_integ_sens, 
						       #endif
						       pm_integ_end};

//basic variables
ring * integVNNames[MAX_INTEG+1];
t_int integVnids [MAX_INTEG*NUM_INTEG_VARS];
t_int integVnidsTypes[NUM_INTEG_VARS];
t_int integFuses;

//local variables
t_int dps[MAX_INTEG];	//store dps to prevent repeated calls of pow(x,y)
t_int deci_fact[MAX_INTEG];

t_int integ_getNum(t_int which)
{
	if GET_BIT(integFuses, which)
	{
		return intVars[integVnids[INTEG_NUM]];
	}
	return 0;
}

t_int integ_getDps(t_int which)
{
	if GET_BIT(integFuses, which)
	{
		return intVars[integVnids[INTEG_DPS]];
	}
	return 0;
}

//nrs accessors
t_int integ_getX(t_int which)
{
	if GET_BIT(integFuses, which)
	{
		return intVars[integVnids[INTEG_X]];
	}
	return 0;
}
t_int integ_getY(t_int which)
{
	if GET_BIT(integFuses, which)
	{
		return intVars[integVnids[INTEG_Y]];
	}
	return 0;
}
t_int integ_getZ(t_int which)
{
	if GET_BIT(integFuses, which)
	{
		return intVars[integVnids[INTEG_Z]];
	}
	return 0;
}

void integ_setOut(t_int which, t_int out)	//sets nrs integ prox variable
{
	if GET_BIT(integFuses, which)
	{
		intVars[integVnids[INTEG_OUT]] = out;		
	}
}

#ifdef DEBUG
t_int integ_getOut(t_int which)
{
	if GET_BIT(integFuses, which)
	{
		return intVars[integVnids[INTEG_OUT]];		
	}
	return 0;
}

s08 integCheckVoidEvent (t_int which)
{
	if GET_BIT(integFuses, which)
	{
		return getVoidEvent(integVnids[INTEG_SENS]);
	}
	return 0;
}
#endif

void integ_update()
{
	t_int which;
	t_int tmpinteg;
	t_int tmpdps;
	t_int x;
	t_int y;
	t_int z;
	t_int out;
	
	for(tmpinteg = 0;tmpinteg<MAX_INTEG;tmpinteg++)			//for each integ
	{	
		
		which = integ_getNum(tmpinteg);			//get the integ number
		
		if ((which > 0) && (which <= MAX_INTEG))	// if a valid integ number (i.e. being used)
		{		
			//out = x*y + z
			//assumes that x,y,z and out are ints representing 
			//a value with 4 decimal places
			//e.g. x = 123456 is equiv to 12.3456
			
			tmpdps = integ_getDps(tmpinteg); 	//get no of decimal places (i.e. unit)
			
			if(tmpdps != dps[tmpinteg])	//if dps changes, recalculate deci_fact
			{	
				deci_fact[tmpinteg] = (t_int)pow(10,tmpdps);
				dps[tmpinteg] = tmpdps;
			}
							
			x = integ_getX(tmpinteg);
			y = integ_getY(tmpinteg);
			z = integ_getZ(tmpinteg);
			
			out = (x*y/deci_fact[tmpinteg]) + z;
			
			if(x*y % deci_fact[tmpinteg] > deci_fact[tmpinteg]/2 )
			{
				out++;	//add one for rounding
			}
			
			integ_setOut(tmpinteg, out);
			
			#ifdef DEBUG			
			if ( integCheckVoidEvent(tmpinteg) )	// if a void event occurs
			{
				printf("INTEG");		
				printf("%d: ",tmpinteg);
				printf("[x y z out]\n");
				printf("%d ",integ_getX(tmpinteg));
				printf("%d ",integ_getY(tmpinteg));
				printf("%d ",integ_getZ(tmpinteg));
				printf("%d\n",integ_getOut(tmpinteg));
				printf("[dps deci_fact]\n");
				printf("%d %d\n",tmpdps,deci_fact[tmpinteg]);
			}
			#endif
		}
	}
}

void integ_ini()
{
	ini_node(INTEG_VNIDPOS);
	
	integVnidsTypes[0] = INT_VAR;	//num
	integVnidsTypes[1] = INT_VAR;	//dps
	integVnidsTypes[2] = INT_VAR;	//x
	integVnidsTypes[3] = INT_VAR;	//y
	integVnidsTypes[4] = INT_VAR;	//z
	integVnidsTypes[5] = INT_VAR;	//out
	#ifdef DEBUG
	integVnidsTypes[6] = VOID_VAR;	//sens
	#endif
	
	t_int tmpdeci_fact = pow(10,DEFAULT_DPS);	//crashes if you use <math.h>
	
	for(t_int i = 0; i<MAX_INTEG; i++)		//init local variables
	{
		dps[i] = 0;
		deci_fact[i] = tmpdeci_fact;
	}
	
    	#ifdef DEBUG
	println ("<INI INTEG NODE>");
    	#endif
}

#ifndef MATH
//if <math.h> not included, define power function
int pow(int x,int n)
{
	int i,p;
	
	p = 1;
	for(i = 1;i<=n;i++)
	{
		p = p * x;
	}
	
	return p;
}
#endif






