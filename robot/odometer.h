/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     6/2005
    Purpose:  Handles odometer node functions
    Software: GCC to compile
    Hardware: Kteam Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#ifndef ODOUSED
#define ODOUSED

#include "tools.h"
#include "nrstools.h"

#define MAX_ODO 2			//maximum number of odo nodes
#ifdef DEBUG
# define NUM_ODO_VARS 6
#else
# define NUM_ODO_VARS 5	
#endif

#define NUM_ODO_BOOL_VARS 0
#define NUM_ODO_INT_VARS 3
#define NUM_ODO_RING_VARS 0
#define NUM_ODO_VOID_VARS 3

#define MAX_ODO_BOOL_VARS MAX_ODO*NUM_ODO_BOOL_VARS
#define MAX_ODO_INT_VARS MAX_ODO*NUM_ODO_INT_VARS
#define MAX_ODO_RING_VARS MAX_ODO*NUM_ODO_RING_VARS
#define MAX_ODO_VOID_VARS MAX_ODO*NUM_ODO_VOID_VARS


#define ODO_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*ODO_VNIDPOS)

#define ODO_NUM which*NUM_ODO_VARS+0
#define ODO_COUNT which*NUM_ODO_VARS+1
#define ODO_SETVAL which*NUM_ODO_VARS+2
#define ODO_SET which*NUM_ODO_VARS+3
#define ODO_RESET which*NUM_ODO_VARS+4
#ifdef DEBUG
# define ODO_SENS which*NUM_ODO_VARS+5
#endif

extern const char pm_odo_num[] PROGMEM;
extern const char pm_odo_count[] PROGMEM;
extern const char pm_odo_setval[] PROGMEM;
extern const char pm_odo_set[] PROGMEM;
extern const char pm_odo_reset[] PROGMEM;
#ifdef DEBUG
extern const char pm_odo_sens[] PROGMEM;
#endif
extern const char pm_odo_end[] PROGMEM;
extern PGM_P pm_odoVarsVNNames[NUM_ODO_VARS+1] PROGMEM;

extern ring * odoVNNames[MAX_ODO+1];
extern t_int odoVnids [MAX_ODO*NUM_ODO_VARS];
extern t_int odoVnidsTypes[NUM_ODO_VARS];
extern t_int odoFuses;

//	Functions returning variables
t_int odo_getNum(t_int which);
void odo_setCount(t_int which, t_int prox);
t_int odo_getSetval(t_int which);
s08 odoCheckSet (t_int which);
s08 odoCheckReset (t_int which);
t_int odo_getCount(t_int which);
s08 odoCheckVoidEvent (t_int which);

void odo_update(void);

void odo_ini(void);

#endif
