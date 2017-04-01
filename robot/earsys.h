/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     6/2005
    Purpose:  Handles Ear node functions
    Software: GCC to compile
    Hardware: Kteam Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#ifndef ESYSUSED
#define ESYSUSED

#include "tools.h"
#include "nrstools.h"
#include "ears/ears.h"

#define MAX_ESYS 1			//maximum number of esys nodes
#define NUM_ESYS_VARS 16			//node itself plus number of variables

#define NUM_ESYS_BOOL_VARS 1
#define NUM_ESYS_INT_VARS 14
#define NUM_ESYS_RING_VARS 0
#define NUM_ESYS_VOID_VARS 1

#define MAX_ESYS_BOOL_VARS MAX_ESYS*NUM_ESYS_BOOL_VARS
#define MAX_ESYS_INT_VARS MAX_ESYS*NUM_ESYS_INT_VARS
#define MAX_ESYS_RING_VARS MAX_ESYS*NUM_ESYS_RING_VARS
#define MAX_ESYS_VOID_VARS MAX_ESYS*NUM_ESYS_VOID_VARS


#define ESYS_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*ESYS_VNIDPOS)

#define ESYS_ENABLE which*NUM_ESYS_VARS+0
//#define ESYS_NUM which*NUM_ESYS_VARS+1
#define ESYS_LSIG which*NUM_ESYS_VARS+1
#define ESYS_RSIG which*NUM_ESYS_VARS+2
#define ESYS_PR1L which*NUM_ESYS_VARS+3
#define ESYS_PR2L which*NUM_ESYS_VARS+4
#define ESYS_PR1R which*NUM_ESYS_VARS+5
#define ESYS_PR2R which*NUM_ESYS_VARS+6
#define ESYS_AN1L which*NUM_ESYS_VARS+7
#define ESYS_AN2L which*NUM_ESYS_VARS+8
#define ESYS_AN3L which*NUM_ESYS_VARS+9
#define ESYS_AN1R which*NUM_ESYS_VARS+10
#define ESYS_AN2R which*NUM_ESYS_VARS+11
#define ESYS_AN3R which*NUM_ESYS_VARS+12
#define ESYS_SDEL1 which*NUM_ESYS_VARS+13
#define ESYS_SDEL2 which*NUM_ESYS_VARS+14
#ifdef DEBUG
# define ESYS_SENS which*NUM_ESYS_VARS+15
#endif

//local defines
#define NUM_SIDES 2

//pregain indices
#define PR1L_I 0
#define PR2L_I 1
#define PR1R_I 0
#define PR2R_I 1
#define NUM_PREGS_PER_SIDE 2

//mixer gain indices
#define AN1L_I 0
#define AN2L_I 1
#define AN3L_I 2
#define AN1R_I 0
#define AN2R_I 1
#define AN3R_I 2
#define NUM_MIXGS_PER_SIDE 3

//synth delay indices
#define SDEL1_I 0
#define SDEL2_I 1
#define NUM_SDELS 2

//defaults (for initialisation)
#define DEFAULT_PREG 0
#define DEFAULT_MIXG 0
#define DEFAULT_SDEL 0

//#define FOUR_EARS 0
//#define TWO_EARS 1

#define EAR_LED1_ON 0
#define EAR_LED1_OFF 1
#define EAR_LED2_ON 6
#define EAR_LED2_OFF 0

extern const char pm_esys_enable[] PROGMEM;
//extern const char pm_esys_num[] PROGMEM;
//outputs
extern const char pm_esys_lsig[] PROGMEM;
extern const char pm_esys_rsig[] PROGMEM;
//pre gains
extern const char pm_esys_pr1l[] PROGMEM;
extern const char pm_esys_pr2l[] PROGMEM;
extern const char pm_esys_pr1r[] PROGMEM;
extern const char pm_esys_pr2r[] PROGMEM;
//mix gains
extern const char pm_esys_an1l[] PROGMEM;
extern const char pm_esys_an2l[] PROGMEM;
extern const char pm_esys_an3l[] PROGMEM;
extern const char pm_esys_an1r[] PROGMEM;
extern const char pm_esys_an2r[] PROGMEM;
extern const char pm_esys_an3r[] PROGMEM;
//synth delays
extern const char pm_esys_sdel1[] PROGMEM;
extern const char pm_esys_sdel2[] PROGMEM;
#ifdef DEBUG
extern const char pm_esys_sens[] PROGMEM;
#endif
extern const char pm_esys_end[] PROGMEM;
extern PGM_P pm_esysVarsVNNames[NUM_ESYS_VARS+1] PROGMEM;

extern ring * esysVNNames[MAX_ESYS+1];
extern t_int esysVnids [MAX_ESYS*NUM_ESYS_VARS];
extern t_int esysVnidsTypes[NUM_ESYS_VARS];
extern t_int esysFuses;

//local variables
t_int preg[NUM_SIDES][NUM_PREGS_PER_SIDE];		//preamp gains
t_int nrs_preg[NUM_SIDES][NUM_PREGS_PER_SIDE];
t_int mixg[NUM_SIDES][NUM_MIXGS_PER_SIDE];		//mix gains
t_int nrs_mixg[NUM_SIDES][NUM_MIXGS_PER_SIDE];		
t_int sdel[NUM_SDELS];					//synth delays
t_int nrs_sdel[NUM_SDELS];


//Functions returning (nrs) variables
s08 esys_getEnable();
//s08 esys_getNum();
s08 esysCheckNum();
void esys_setLsig(t_int sig);
t_int esys_getLsig();		//debug only
void esys_setRsig(t_int sig);	
t_int esys_getRsig();		//debug only
void esys_getPreg();		//preg accessor
void esys_getMixg();		//mixg accessor
void esys_getDelays();		//delay accessor
#ifdef DEBUG
s08 esysCheckVoidEvent();	//sens
#endif

//parameter update functions
void set_pregains();
void set_mixgains();
void set_delays();

//node update
void esys_update(void);

//ini
void esys_ini(void);

#endif
