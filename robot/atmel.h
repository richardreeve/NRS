/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     1/2005
    Purpose:  Handles ATMEL node functions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#ifndef ATMELUSED
#define ATMELUSED

#include "tools.h"
#include "nrstools.h"

#define MAX_ATMEL 1
#define NUM_ATMEL_VARS 1			//node itself plus number of variables

#define ATMEL_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*ATMEL_VNIDPOS)
#define ATMEL_ENABLE 0

extern const char pm_atmel_enable[] PROGMEM;
extern const char pm_atmel_end[] PROGMEM;
extern PGM_P pm_atmelVarsVNNames[NUM_ATMEL_VARS+1] PROGMEM;

//basic variables
extern ring * atmelVNNames[MAX_ATMEL+1];
extern t_int atmelVnids [NUM_ATMEL_VARS];
extern t_int atmelVnidsTypes[NUM_ATMEL_VARS];
extern t_int atmelFuses;

//	Functions returning variables
s08 atmel_getEnable(void);

void atmel_update(void);

void atmel_ini(void);

#endif

