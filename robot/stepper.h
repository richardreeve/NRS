/*
    Title:    NRS program for the Atmel ATMEGA128
    Author:   Matthew Howard (Prowse)
    Date:     6/2005
    Purpose:  Handles STEPPER node functions
    Software: GCC to compile
    Hardware: Atmel ATMEGA128
    Note:     contact me at: s0459419@ed.ac.uk
*/

#ifndef STEPPERUSED
#define STEPPERUSED

#include "tools.h"
#include "nrstools.h"

#define MAX_STEPPER 1			//maximum number of stepper nodes
#define NUM_STEPPER_VARS 6 			//node itself plus number of variables

#define STEPPER_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*STEPPER_VNIDPOS)

#define STEPPER_NUM which*NUM_STEPPER_VARS+0
#define STEPPER_ENABLE which*NUM_STEPPER_VARS+1
#define STEPPER_MICROSTEPS which*NUM_STEPPER_VARS+2
#define STEPPER_SPEED which*NUM_STEPPER_VARS+3
#define STEPPER_STEP which*NUM_STEPPER_VARS+4
#define STEPPER_SENSITIVITY which*NUM_STEPPER_VARS+5

#define TOGGLE_STEP (GET_BIT(S_PORT,S_STEP))?CLEAR_BIT(S_PORT,S_STEP):SET_BIT(S_PORT,S_STEP)



extern const char pm_stepper_num[] PROGMEM;
extern const char pm_stepper_enable[] PROGMEM;
extern const char pm_stepper_microsteps[] PROGMEM;
extern const char pm_stepper_speed[] PROGMEM;
extern const char pm_stepper_direction[] PROGMEM;
extern const char pm_stepper_step[] PROGMEM;
extern const char pm_stepper_sensitivity[] PROGMEM;

extern const char pm_stepper_end[] PROGMEM;
extern PGM_P pm_stepperVarsVNNames[NUM_STEPPER_VARS+1] PROGMEM;

extern ring * stepperVNNames[MAX_STEPPER+1];
extern t_int stepperVnids [MAX_STEPPER*NUM_STEPPER_VARS];
extern t_int stepperVnidsTypes[NUM_STEPPER_VARS];
extern t_int stepperFuses;

//	Functions returning variables
t_int stepper_getNum(t_int which);
s08 stepper_getEnable(t_int which);
t_int stepper_getMicroSteps(t_int which);
t_int stepper_getSpeed(t_int which);
s08 stepper_getDirection(t_int which);
s08 stepper_CheckVoidEvent (t_int which);
t_int stepper_getSensitivity(t_int which);

void stepper_enableInterrupts(void);
void stepper_disableInterrupts(void);

void stepper_setTimerResolution(t_int r);

void stepper_update(void);

void stepper_ini(void);

#endif
