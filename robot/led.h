/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     11/2004
    Purpose:  Handles LED node functions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#ifndef LEDUSED
#define LEDUSED

#include "tools.h"
#include "nrstools.h"

#ifdef __ROBOT_kteam
# define MAX_LED 2			//maximum number of led nodes
#else
# define MAX_LED 4			//maximum number of led nodes
#endif

#define NUM_LED_VARS 4			//node itself plus number of variables

#define NUM_LED_BOOL_VARS 1
#define NUM_LED_INT_VARS 2
#define NUM_LED_RING_VARS 0
#define NUM_LED_VOID_VARS 1
        
#define MAX_LED_BOOL_VARS MAX_LED*NUM_LED_BOOL_VARS
#define MAX_LED_INT_VARS MAX_LED*NUM_LED_INT_VARS
#define MAX_LED_RING_VARS MAX_LED*NUM_LED_RING_VARS
#define MAX_LED_VOID_VARS MAX_LED*NUM_LED_VOID_VARS


#define LED_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*LED_VNIDPOS)

#define LED_NUM which*NUM_LED_VARS+0
#define LED_ON which*NUM_LED_VARS+1
#define LED_HOLDTIME which*NUM_LED_VARS+2
#define LED_BLINK which*NUM_LED_VARS+3

extern const char pm_led_num[] PROGMEM;
extern const char pm_led_on[] PROGMEM;
extern const char pm_led_holdtime[] PROGMEM;
extern const char pm_led_blink[] PROGMEM;
extern const char pm_led_end[] PROGMEM;
extern PGM_P pm_ledVarsVNNames[NUM_LED_VARS+1] PROGMEM;

extern ring * ledVNNames[MAX_LED+1];
extern t_int ledVnids [MAX_LED*NUM_LED_VARS];
extern t_int ledVnidsTypes[NUM_LED_VARS];
extern t_int ledFuses;


/*
	for calculating maximum array sizes on memory.h
	number variables	1
	s08 variables		1
*/

//	Functions returning variables
s08 led_getOn(t_int which);
t_int led_getHoldTime(t_int which);
t_int led_getNum(t_int which);
s08 ledCheckVoidEvent (t_int which);

void led_update(void);

void led_ini(void);

#endif
