/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     06/2004
    Purpose:  Handles CLOCK node functions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/
#ifndef __ROBOT_kteam
#include "timer.h"

const char pm_timer_num[] PROGMEM = ".num";
const char pm_timer_run[] PROGMEM = ".run";
const char pm_timer_period[] PROGMEM = ".period";
const char pm_timer_trigger[] PROGMEM = ".trigger";
const char pm_timer_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_timerVarsVNNames[NUM_TIMER_VARS+1] PROGMEM = {pm_timer_num, pm_timer_run, pm_timer_period, 
									pm_timer_trigger, pm_timer_end};

//basic variables
ring * timerVNNames[MAX_TIMER+1];
t_int timerVnids [MAX_TIMER*NUM_TIMER_VARS];
t_int timerVnidsTypes[NUM_TIMER_VARS];
t_int timerFuses;

//local variables
t_int timerCounters[MAX_TIMER];

t_int timer_getNum(t_int which)
{
	if GET_BIT(timerFuses, which)
	{
		return intVars[timerVnids[TIMER_NUM]];
	}
	return 0;
}

s08 timer_getRun(t_int which)
{
	if GET_BIT(timerFuses, which)
	{
		return boolVars[timerVnids[TIMER_RUN]];
	}
	return 0;
}

t_int timer_getPeriod(t_int which)
{
	if GET_BIT(timerFuses, which)
	{
		return intVars[timerVnids[TIMER_PERIOD]];
	}
	return 0;
}

s08 timerCheckVoidEvent (t_int which)
{
	if GET_BIT(timerFuses, which)
	{
		return getVoidEvent(timerVnids[TIMER_TRIGGER]);
	}
	return 0;
}

void timer_update()
{

}

void timer_ini()
{
	ini_node(TIMER_VNIDPOS);
	
	timerVnidsTypes[0] = INT_VAR;
	timerVnidsTypes[1] = BOOL_VAR;
	timerVnidsTypes[2] = INT_VAR;
	timerVnidsTypes[3] = VOID_VAR;
    
    #ifdef DEBUG
		println ("<INI TIMER NODE>");
    #endif
}
#endif

