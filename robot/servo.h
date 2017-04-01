/*
    Title:    NRS program for the Atmel ATMEGA128
    Author:   Matthew Howard (Prowse)
    Date:     6/2005
    Purpose:  Handles SERVO node functions
    Software: GCC to compile
    Hardware: Atmel ATMEGA128
    Note:     contact me at: s0459419@ed.ac.uk
*/

#ifndef SERVOUSED
#define SERVOUSED

#include "tools.h"
#include "nrstools.h"

#define MAX_SERVO 1			//maximum number of servo nodes
#define NUM_SERVO_VARS 3 			//node itself plus number of variables

#define SERVO_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*SERVO_VNIDPOS)

#define TOGGLE_SIGNAL (GET_BIT(S_PORT,S_SERVO))?CLEAR_BIT(S_PORT,S_SERVO):SET_BIT(S_PORT,S_SERVO)

#define SERVO_NUM which*NUM_SERVO_VARS+0
#define SERVO_ENABLE which*NUM_SERVO_VARS+1
#define SERVO_POSITION which*NUM_SERVO_VARS+2

extern const char pm_servo_num[] PROGMEM;
extern const char pm_servo_enable[] PROGMEM;
extern const char pm_servo_position[] PROGMEM;

extern const char pm_servo_end[] PROGMEM;
extern PGM_P pm_servoVarsVNNames[NUM_SERVO_VARS+1] PROGMEM;

extern ring * servoVNNames[MAX_SERVO+1];
extern t_int servoVnids [MAX_SERVO*NUM_SERVO_VARS];
extern t_int servoVnidsTypes[NUM_SERVO_VARS];
extern t_int servoFuses;

//	Functions returning variables
t_int servo_getNum(t_int which);
s08 servo_getEnable(t_int which);
t_int servo_getPosition(t_int which);

void servo_enableInterrupts(void);
void servo_disableInterrupts(void);

void servo_update(void);

void servo_ini(void);

#endif
