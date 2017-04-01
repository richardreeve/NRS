/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     6/2005
    Purpose:  Handles Variable Bandpass functions   
    Software: GCC to compile
    Hardware: Kteam Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#ifndef BPASSUSED
#define BPASSUSED

#include "tools.h"
#include "nrstools.h"

#define MAX_BPASS 2			//maximum number of bpass nodes

#ifdef DEBUG
# define NUM_BPASS_VARS 8		//node itself plus number of variables
#else
# define NUM_BPASS_VARS 7		//node itself plus number of variables
#endif

#define NUM_BPASS_BOOL_VARS 2
#define NUM_BPASS_INT_VARS 5
#define NUM_BPASS_RING_VARS 0
#define NUM_BPASS_VOID_VARS 1

#define MAX_BPASS_BOOL_VARS MAX_BPASS*NUM_BPASS_BOOL_VARS
#define MAX_BPASS_INT_VARS MAX_BPASS*NUM_BPASS_INT_VARS
#define MAX_BPASS_RING_VARS MAX_BPASS*NUM_BPASS_RING_VARS
#define MAX_BPASS_VOID_VARS MAX_BPASS*NUM_BPASS_VOID_VARS

#define BPASS_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*BPASS_VNIDPOS)

#define BPASS_NUM which*NUM_BPASS_VARS+0
#define BPASS_HIGH which*NUM_BPASS_VARS+1
#define BPASS_LOW which*NUM_BPASS_VARS+2
#define BPASS_IN which*NUM_BPASS_VARS+3
#define BPASS_BOUT which*NUM_BPASS_VARS+4
#define BPASS_IOUT which*NUM_BPASS_VARS+5
#define BPASS_INV which*NUM_BPASS_VARS+6
#ifdef DEBUG
#define BPASS_SENS which*NUM_BPASS_VARS+7
#endif

//local defines
#define DEFAULT_INV 0
#define BP_FALSE 0
#define BP_TRUE 1

extern const char pm_bpass_num[] PROGMEM;
extern const char pm_bpass_high[] PROGMEM;
extern const char pm_bpass_low[] PROGMEM;
extern const char pm_bpass_in[] PROGMEM;
extern const char pm_bpass_bout[] PROGMEM;
extern const char pm_bpass_iout[] PROGMEM;
extern const char pm_bpass_inv[] PROGMEM;
#ifdef DEBUG
extern const char pm_bpass_sens[] PROGMEM;
#endif
extern const char pm_bpass_end[] PROGMEM;
extern PGM_P pm_bpassVarsVNNames[NUM_BPASS_VARS+1] PROGMEM;

extern ring * bpassVNNames[MAX_BPASS+1];
extern t_int bpassVnids [MAX_BPASS*NUM_BPASS_VARS];
extern t_int bpassVnidsTypes[NUM_BPASS_VARS];
extern t_int bpassFuses;

//	Functions returning variables
t_int bpass_getNum(t_int which);
t_int bpass_getHigh(t_int which);
t_int bpass_getLow(t_int which);
t_int bpass_getIn(t_int which);
void bpass_setBout(t_int which, t_int out);
void bpass_setIout(t_int which, t_int out);
t_int bpass_getInv(t_int which);
#ifdef DEBUG
t_int bpass_getBout(t_int which);
t_int bpass_getIout(t_int which);
s08 bpassCheckVoidEvent (t_int which);
#endif

void bpass_update(void);

void bpass_ini(void);

#endif
