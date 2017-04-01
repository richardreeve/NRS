/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     6/2005
    Purpose:  Handles Variable Multiplication and Addition functions   
    Software: GCC to compile
    Hardware: Kteam Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#ifndef INTEGUSED
#define INTEGUSED

//#include <math.h>
//#define MATH

#include "tools.h"
#include "nrstools.h"


#define MAX_INTEG 12			//maximum number of integ nodes

#ifdef DEBUG
# define NUM_INTEG_VARS 7
#else
# define NUM_INTEG_VARS 6
#endif

#define NUM_INTEG_BOOL_VARS 0
#define NUM_INTEG_INT_VARS 6
#define NUM_INTEG_RING_VARS 0
#define NUM_INTEG_VOID_VARS 1

#define MAX_INTEG_BOOL_VARS MAX_INTEG*NUM_INTEG_BOOL_VARS
#define MAX_INTEG_INT_VARS MAX_INTEG*NUM_INTEG_INT_VARS
#define MAX_INTEG_RING_VARS MAX_INTEG*NUM_INTEG_RING_VARS
#define MAX_INTEG_VOID_VARS MAX_INTEG*NUM_INTEG_VOID_VARS


#define INTEG_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*INTEG_VNIDPOS)

#define INTEG_NUM which*NUM_INTEG_VARS+0
#define INTEG_DPS which*NUM_INTEG_VARS+1
#define INTEG_X which*NUM_INTEG_VARS+2
#define INTEG_Y which*NUM_INTEG_VARS+3
#define INTEG_Z which*NUM_INTEG_VARS+4
#define INTEG_OUT which*NUM_INTEG_VARS+5
#ifdef DEBUG
# define INTEG_SENS which*NUM_INTEG_VARS+6
#endif

//local defines
#define DEFAULT_DPS 4

extern const char pm_integ_num[] PROGMEM;
extern const char pm_integ_dps[] PROGMEM;
extern const char pm_integ_x[] PROGMEM;
extern const char pm_integ_y[] PROGMEM;
extern const char pm_integ_z[] PROGMEM;
extern const char pm_integ_out[] PROGMEM;
#ifdef DEBUG
extern const char pm_integ_sens[] PROGMEM;
#endif
extern const char pm_integ_end[] PROGMEM;
extern PGM_P pm_integVarsVNNames[NUM_INTEG_VARS+1] PROGMEM;

extern ring * integVNNames[MAX_INTEG+1];
extern t_int integVnids [MAX_INTEG*NUM_INTEG_VARS];
extern t_int integVnidsTypes[NUM_INTEG_VARS];
extern t_int integFuses;

//	Functions returning variables
t_int integ_getNum(t_int which);
t_int integ_getDps(t_int which);
t_int integ_getX(t_int which);
t_int integ_getY(t_int which);
t_int integ_getZ(t_int which);
void integ_setOut(t_int which, t_int out);

#ifdef DEBUG
t_int integ_getOut(t_int which);
s08 integCheckVoidEvent (t_int which);
#endif

void integ_update(void);

void integ_ini(void);

#ifndef MATH
int pow(int x,int n);
#endif

#endif
