/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     6/2005
    Purpose:  Handles Motor node functions
    Software: GCC to compile
    Hardware: Kteam Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#ifndef MOTORUSED
#define MOTORUSED

#include "tools.h"
#include "nrstools.h"

#define MAX_MOTOR 2			//maximum number of motor nodes

#ifdef DEBUG
# define NUM_MOTOR_VARS 11			//num,speed,pos,kp,ki,kd,pid,setpos
#else
# define NUM_MOTOR_VARS 10			//num,speed,pos,kp,ki,kd,pid,setpos
#endif

#define NUM_MOTOR_BOOL_VARS 0
#define NUM_MOTOR_INT_VARS 9
#define NUM_MOTOR_RING_VARS 0
#define NUM_MOTOR_VOID_VARS 2

#define MAX_MOTOR_BOOL_VARS MAX_MOTOR*NUM_MOTOR_BOOL_VARS
#define MAX_MOTOR_INT_VARS MAX_MOTOR*NUM_MOTOR_INT_VARS
#define MAX_MOTOR_RING_VARS MAX_MOTOR*NUM_MOTOR_RING_VARS
#define MAX_MOTOR_VOID_VARS MAX_MOTOR*NUM_MOTOR_VOID_VARS


#define MOTOR_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*MOTOR_VNIDPOS)

#define MOTOR_NUM which*NUM_MOTOR_VARS+0
#define MOTOR_MODE which*NUM_MOTOR_VARS+1
#define MOTOR_SPEED which*NUM_MOTOR_VARS+2
#define MOTOR_POS which*NUM_MOTOR_VARS+3
#define MOTOR_SETPOS which*NUM_MOTOR_VARS+4
#define MOTOR_MSPEED which*NUM_MOTOR_VARS+5
#define MOTOR_MACC which*NUM_MOTOR_VARS+6
#define MOTOR_KP which*NUM_MOTOR_VARS+7
#define MOTOR_KI which*NUM_MOTOR_VARS+8
#define MOTOR_KD which*NUM_MOTOR_VARS+9
#ifdef DEBUG
# define MOTOR_SENS which*NUM_MOTOR_VARS+10
#endif

//local defines

//kteam recommended values
#define DEFAULT_MSPEED 125
#define DEFAULT_MACC 1
#define DEFAULT_SPEED_KP 3500
#define DEFAULT_SPEED_KI 800
#define DEFAULT_SPEED_KD 100
#define DEFAULT_POS_KP 3000
#define DEFAULT_POS_KI 20
#define DEFAULT_POS_KD 4000

//range of speeds for motors
#define MAX_SPEED 127
#define MIN_SPEED -128

//control modes
#define SPEED_CTRL 0
#define POS_CTRL 1
#define INCR_CTRL 2
			
extern const char pm_motor_num[] PROGMEM;
extern const char pm_motor_mode[] PROGMEM;
extern const char pm_motor_speed[] PROGMEM;
extern const char pm_motor_pos[] PROGMEM;
extern const char pm_motor_setpos[] PROGMEM;
extern const char pm_motor_mspeed[] PROGMEM;
extern const char pm_motor_macc[] PROGMEM;
extern const char pm_motor_kp[] PROGMEM;
extern const char pm_motor_ki[] PROGMEM;
extern const char pm_motor_kd[] PROGMEM;
#ifdef DEBUG
extern const char pm_motor_sens[] PROGMEM;
#endif
extern const char pm_motor_end[] PROGMEM;
extern PGM_P pm_motorVarsVNNames[NUM_MOTOR_VARS+1] PROGMEM;

extern ring * motorVNNames[MAX_MOTOR+1];
extern t_int motorVnids [MAX_MOTOR*NUM_MOTOR_VARS];
extern t_int motorVnidsTypes[NUM_MOTOR_VARS];
extern t_int motorFuses;

//local variables
t_int last[MAX_MOTOR];
int32 motor_status[MAX_MOTOR];

//	Functions returning variables
t_int motor_getNum(t_int which);
t_int motor_getMode(t_int which);
t_int motor_getSpeed(t_int which);
t_int motor_getPos(t_int which);
t_int motor_getMspeed(t_int which);
t_int motor_getMacc(t_int which);
s08 motorCheckSetpos (t_int which);
t_int motor_getKp(t_int which);
t_int motor_getKi(t_int which);
t_int motor_getKd(t_int which);
#ifdef DEBUG
s08 motorCheckVoidEvent (t_int which);
#endif

void motor_update(void);

void motor_disable(void);

void update_pid(t_int which, t_int tmpmotor);
void update_prof(t_int which, t_int tmpmotor);

void motor_ini(void);

#endif
