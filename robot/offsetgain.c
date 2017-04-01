/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     11/2004
    Purpose:  Handles Variable Multiplication and Addition functions
    Software: GCC to compile
    Hardware: Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#include "offsetgain.h"

const char pm_offsetgain_num[] PROGMEM = ".num";
const char pm_offsetgain_in[] PROGMEM = ".in";
const char pm_offsetgain_offset[] PROGMEM = ".offset";
const char pm_offsetgain_gain[] PROGMEM = ".gain";
const char pm_offsetgain_output[] PROGMEM = ".out";
const char pm_offsetgain_end[] PROGMEM = LISTBREAKSTR;

PGM_P pm_offsetgainVarsVNNames[NUM_OFFSETGAIN_VARS+1] PROGMEM = {pm_offsetgain_num, pm_offsetgain_in, pm_offsetgain_offset, pm_offsetgain_gain, pm_offsetgain_output, pm_offsetgain_end};

//basic variables
ring * offsetgainVNNames[MAX_OFFSETGAIN+1];
t_int offsetgainVnids [MAX_OFFSETGAIN*NUM_OFFSETGAIN_VARS];
t_int offsetgainVnidsTypes[NUM_OFFSETGAIN_VARS];
t_int offsetgainFuses;


t_int offsetgain_getNum(t_int which)
{
	if GET_BIT(offsetgainFuses, which)
	{
		return intVars[offsetgainVnids[OFFSETGAIN_NUM]];
	}
	return 0;
}

t_int offsetgain_getIn(t_int which)
{
	if GET_BIT(offsetgainFuses, which)
	{
		return intVars[offsetgainVnids[OFFSETGAIN_IN]];
	}
	return 0;
}

t_int offsetgain_getOffset(t_int which)
{
	if GET_BIT(offsetgainFuses, which)
	{
		return intVars[offsetgainVnids[OFFSETGAIN_OFFSET]];
	}
	return 0;
}

t_int offsetgain_getGain(t_int which)
{
	if GET_BIT(offsetgainFuses, which)
	{
		return intVars[offsetgainVnids[OFFSETGAIN_GAIN]];
	}
	return 0;
}

void offsetgain_setOut(t_int which, t_int out)
{
	if GET_BIT(offsetgainFuses, which)
	{
		intVars[offsetgainVnids[OFFSETGAIN_OUT]] = out;
	}
}


void offsetgain_update()
{
	t_int which;
	t_int tmpoffsetgain;
	
	t_int tmpin, tmpoffset, tmpgain;
	t_int tmpout;

	for(tmpoffsetgain = 0;tmpoffsetgain<MAX_OFFSETGAIN;tmpoffsetgain++)			//for each offsetgain
	{	
		
		which = offsetgain_getNum(tmpoffsetgain);			//get the offsetgain number
		
		if ((which > 0) && (which <= MAX_OFFSETGAIN))	// if a valid offsetgain number (i.e. being used)
		{		
		  tmpin = offsetgain_getIn(which-1);
		  tmpoffset = offsetgain_getOffset(which-1);
		  tmpgain = offsetgain_getGain(which-1);

		  tmpout = (tmpin + tmpoffset) * tmpgain;

		  offsetgain_setOut(which-1, tmpout);
		}
	}
}

void offsetgain_ini()
{
	ini_node(OFFSETGAIN_VNIDPOS);
	
	offsetgainVnidsTypes[0] = INT_VAR;	//num
	offsetgainVnidsTypes[1] = INT_VAR;	//in
	offsetgainVnidsTypes[2] = INT_VAR;	//offset
	offsetgainVnidsTypes[3] = INT_VAR;	//gain
	offsetgainVnidsTypes[4] = INT_VAR;	//out

    	#ifdef DEBUG
	println ("<INI OFFSETGAIN NODE>");
    	#endif
}
