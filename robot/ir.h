/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     6/2005
    Purpose:  Handles IR node functions
    Software: GCC to compile
    Hardware: Kteam Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#ifndef IRUSED
#define IRUSED

#include "tools.h"
#include "nrstools.h"

#define MAX_IR 8			//maximum number of ir nodes

#ifdef DEBUG
# define NUM_IR_VARS 3			//node itself plus number of variables
#else
# define NUM_IR_VARS 2			//node itself plus number of variables
#endif

#define NUM_IR_BOOL_VARS 0
#define NUM_IR_INT_VARS 2
#define NUM_IR_RING_VARS 0
#define NUM_IR_VOID_VARS 1

#define MAX_IR_BOOL_VARS MAX_IR*NUM_IR_BOOL_VARS
#define MAX_IR_INT_VARS MAX_IR*NUM_IR_INT_VARS
#define MAX_IR_RING_VARS MAX_IR*NUM_IR_RING_VARS
#define MAX_IR_VOID_VARS MAX_IR*NUM_IR_VOID_VARS

#define IR_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*IR_VNIDPOS)

#define IR_NUM which*NUM_IR_VARS+0
#define IR_PROX which*NUM_IR_VARS+1
#ifdef DEBUG
# define IR_SENS which*NUM_IR_VARS+2
#endif

extern const char pm_ir_num[] PROGMEM;
extern const char pm_ir_prox[] PROGMEM;
#ifdef DEBUG
extern const char pm_ir_sens[] PROGMEM;
#endif
extern const char pm_ir_end[] PROGMEM;
extern PGM_P pm_irVarsVNNames[NUM_IR_VARS+1] PROGMEM;

extern ring * irVNNames[MAX_IR+1];
extern t_int irVnids [MAX_IR*NUM_IR_VARS];
extern t_int irVnidsTypes[NUM_IR_VARS];
extern t_int irFuses;

//	Functions returning variables
t_int ir_getNum(t_int which);
void ir_setProx(t_int which, t_int prox);
#ifdef DEBUG
t_int ir_getProx(t_int which);
s08 irCheckVoidEvent (t_int which);
#endif

void ir_update(void);

void ir_ini(void);

#endif
