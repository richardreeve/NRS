/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     1/2005
    Purpose:  Handles ATMEL node functions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#ifndef KH2USED
#define KH2USED

#include "tools.h"
#include "nrstools.h"

#define MAX_KH2 1
//#define NUM_KH2_VARS 2			//node itself plus number of variables
#define NUM_KH2_VARS 1			//node itself plus number of variables

#define NUM_KH2_BOOL_VARS 1
#define NUM_KH2_INT_VARS 0
#define NUM_KH2_RING_VARS 0
#define NUM_KH2_VOID_VARS 0

#define MAX_KH2_BOOL_VARS MAX_KH2*NUM_KH2_BOOL_VARS
#define MAX_KH2_INT_VARS MAX_KH2*NUM_KH2_INT_VARS
#define MAX_KH2_RING_VARS MAX_KH2*NUM_KH2_RING_VARS
#define MAX_KH2_VOID_VARS MAX_KH2*NUM_KH2_VOID_VARS

#define KH2_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*KH2_VNIDPOS)
#define KH2_ENABLE 0
//#define KH2_RESET 1

extern const char pm_kh2_enable[] PROGMEM;
//extern const char pm_kh2_reset[] PROGMEM;
extern const char pm_kh2_end[] PROGMEM;
extern PGM_P pm_kh2VarsVNNames[NUM_KH2_VARS+1] PROGMEM;

//basic variables
extern ring * kh2VNNames[MAX_KH2+1];
extern t_int kh2Vnids [NUM_KH2_VARS];
extern t_int kh2VnidsTypes[NUM_KH2_VARS];
extern t_int kh2Fuses;

//	Functions returning variables
s08 kh2_getEnable(void);

//s08 kh2CheckReset(void);

void kh2_update(void);

void kh2_ini(void);

#endif

