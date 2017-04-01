/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     6/2005
    Purpose:  Handles Variable Multiplication and Addition functions   
    Software: GCC to compile
    Hardware: Kteam Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#ifndef DELTAUSED
#define DELTAUSED

#include "tools.h"
#include "nrstools.h"

#define MAX_DELTA 4			//maximum number of delta nodes

#ifdef DEBUG
# define NUM_DELTA_VARS 5			//node itself plus number of variables
#else
# define NUM_DELTA_VARS 4			//node itself plus number of variables
#endif

#define NUM_DELTA_BOOL_VARS 0
#define NUM_DELTA_INT_VARS 4
#define NUM_DELTA_RING_VARS 0
#define NUM_DELTA_VOID_VARS 1

#define MAX_DELTA_BOOL_VARS MAX_DELTA*NUM_DELTA_BOOL_VARS
#define MAX_DELTA_INT_VARS MAX_DELTA*NUM_DELTA_INT_VARS
#define MAX_DELTA_RING_VARS MAX_DELTA*NUM_DELTA_RING_VARS
#define MAX_DELTA_VOID_VARS MAX_DELTA*NUM_DELTA_VOID_VARS


#define DELTA_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*DELTA_VNIDPOS)

#define DELTA_NUM which*NUM_DELTA_VARS+0
#define DELTA_DEL which*NUM_DELTA_VARS+1
#define DELTA_IN which*NUM_DELTA_VARS+2
#define DELTA_OUT which*NUM_DELTA_VARS+3
#ifdef DEBUG
# define DELTA_SENS which*NUM_DELTA_VARS+4
#endif

//local defines
//(none)

extern const char pm_delta_num[] PROGMEM;
extern const char pm_delta_del[] PROGMEM;
extern const char pm_delta_in[] PROGMEM;
extern const char pm_delta_out[] PROGMEM;
#ifdef DEBUG
extern const char pm_delta_sens[] PROGMEM;
#endif
extern const char pm_delta_end[] PROGMEM;
extern PGM_P pm_deltaVarsVNNames[NUM_DELTA_VARS+1] PROGMEM;

extern ring * deltaVNNames[MAX_DELTA+1];
extern t_int deltaVnids [MAX_DELTA*NUM_DELTA_VARS];
extern t_int deltaVnidsTypes[NUM_DELTA_VARS];
extern t_int deltaFuses;

//	Functions returning variables
t_int delta_getNum(t_int which);
t_int delta_getDel(t_int which);
t_int delta_getIn(t_int which);
void delta_setOut(t_int which, t_int out);

#ifdef DEBUG
t_int delta_getOut(t_int which);
s08 deltaCheckVoidEvent (t_int which);
#endif

void delta_update(void);

void delta_ini(void);

#endif
