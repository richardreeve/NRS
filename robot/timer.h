/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     06/2004
    Purpose:  Handles TIMER node functions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#ifndef TIMERUSED
#define TIMERUSED

#include "tools.h"
#include "nrstools.h"

#define MAX_TIMER 2			//maximum number of timers nodes
#define NUM_TIMER_VARS 4		//node itself plus number of variables

#define TIMER_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*TIMER_VNIDPOS)

#define TIMER_NUM which*NUM_TIMER_VARS+0
#define TIMER_RUN which*NUM_TIMER_VARS+1
#define TIMER_PERIOD which*NUM_TIMER_VARS+2
#define TIMER_TRIGGER which*NUM_TIMER_VARS+3

extern const char pm_timer_num[] PROGMEM;
extern const char pm_timer_run[] PROGMEM;
extern const char pm_timer_period[] PROGMEM;
extern const char pm_timer_trigger[] PROGMEM;
extern const char pm_timer_end[] PROGMEM;
extern PGM_P pm_timerVarsVNNames[NUM_TIMER_VARS+1] PROGMEM;

extern ring * timerVNNames[MAX_TIMER+1];
extern t_int timerVnids [MAX_TIMER*NUM_TIMER_VARS];
extern t_int timerVnidsTypes[NUM_TIMER_VARS];
extern t_int timerFuses;


/*
	for calculating maximum array sizes on memory.h
	number variables	1
	s08 variables		1
*/

//	Functions returning variables
s08 timer_getRun(t_int which);
t_int timer_getPeriod(t_int which);
t_int timer_getNum(t_int which);
s08 timerCheckVoidEvent (t_int which);

void timer_update(void);

void timer_ini(void);

#endif
