/*
    Title:    NRS program for the Atmel ATMEGA128
    Author:   Matthew Howard (Prowse)
    Date:     6/2005
    Purpose:  Handles ADC node functions
    Software: GCC to compile
    Hardware: Atmel ATMEGA128
    Note:     contact me at: s0459419@ed.ac.uk
*/

#ifndef ADCUSED
#define ADCUSED

#include "tools.h"
#include "nrstools.h"

#define MAX_ADC 8			//maximum number of adc nodes
#define NUM_ADC_VARS 2			//node itself plus number of variables

#define ADC_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*ADC_VNIDPOS)

#define ADC_NUM which*NUM_ADC_VARS+0
#define ADC_READING which*NUM_ADC_VARS+1
//#define ADC_SENS which*NUM_ADC_VARS+2

extern const char pm_adc_num[] PROGMEM;
extern const char pm_adc_reading[] PROGMEM;
//extern const char pm_adc_sens[] PROGMEM;
extern const char pm_adc_end[] PROGMEM;
extern PGM_P pm_adcVarsVNNames[NUM_ADC_VARS+1] PROGMEM;

extern ring * adcVNNames[MAX_ADC+1];
extern t_int adcVnids [MAX_ADC*NUM_ADC_VARS];
extern t_int adcVnidsTypes[NUM_ADC_VARS];
extern t_int adcFuses;

//	Functions returning variables
t_int adc_getNum(t_int which);
t_int adc_getReading(t_int which);
void adc_setReading(t_int which, t_int reading);
s08 adcCheckVoidEvent (t_int which);

void adc_update(void);

void adc_ini(void);

#endif
