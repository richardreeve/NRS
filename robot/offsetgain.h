/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     6/2005
    Purpose:  Handles Variable Multiplication and Addition functions   
    Software: GCC to compile
    Hardware: Kteam Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#ifndef OFFSETGAINUSED
#define OFFSETGAINUSED

#include "tools.h"
#include "nrstools.h"


#define MAX_OFFSETGAIN 4			//maximum number of offsetgain nodes

#define NUM_OFFSETGAIN_VARS 5

#define OFFSETGAIN_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*OFFSETGAIN_VNIDPOS)

#define OFFSETGAIN_NUM which*NUM_OFFSETGAIN_VARS+0
#define OFFSETGAIN_IN which*NUM_OFFSETGAIN_VARS+1
#define OFFSETGAIN_OFFSET which*NUM_OFFSETGAIN_VARS+2
#define OFFSETGAIN_GAIN which*NUM_OFFSETGAIN_VARS+3
#define OFFSETGAIN_OUT which*NUM_OFFSETGAIN_VARS+4

extern const char pm_offsetgain_num[] PROGMEM;
extern const char pm_offsetgain_in[] PROGMEM;
extern const char pm_offsetgain_offset[] PROGMEM;
extern const char pm_offsetgain_gain[] PROGMEM;
extern const char pm_offsetgain_out[] PROGMEM;

extern const char pm_offsetgain_end[] PROGMEM;
extern PGM_P pm_offsetgainVarsVNNames[NUM_OFFSETGAIN_VARS+1] PROGMEM;

extern ring * offsetgainVNNames[MAX_OFFSETGAIN+1];
extern t_int offsetgainVnids [MAX_OFFSETGAIN*NUM_OFFSETGAIN_VARS];
extern t_int offsetgainVnidsTypes[NUM_OFFSETGAIN_VARS];
extern t_int offsetgainFuses;

//	Functions returning variables
t_int offsetgain_getNum(t_int which);
t_int offsetgain_getIn(t_int which);
t_int offsetgain_getOffset(t_int which);
t_int offsetgain_getGain(t_int which);
void offsetgain_setOut(t_int which, t_int out);

void offsetgain_update(void);

void offsetgain_ini(void);

#endif
