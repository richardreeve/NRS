/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     1/2005
    Purpose:  Handles ATMEL node functions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/
#ifndef __ROBOT_kteam
#include "atmel.h"

const char pm_atmel_enable[] PROGMEM = ".enable";
const char pm_atmel_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_atmelVarsVNNames[NUM_ATMEL_VARS+1] PROGMEM = {pm_atmel_enable, pm_atmel_end};

//basic variables
ring * atmelVNNames[MAX_ATMEL+1];
t_int atmelVnids [NUM_ATMEL_VARS];
t_int atmelVnidsTypes[NUM_ATMEL_VARS];
t_int atmelFuses;			//keeps track of e.g. which leds have been used (a byte, where each '1' bit represents an led used)

//	Functions returning variables
s08 atmel_getEnable()
{
	if GET_BIT(atmelFuses, 0)
	{
		return boolVars[atmelVnids[ATMEL_ENABLE]];
	}
	return 0;
}

void atmel_update(void)
{
	link_update();
	
	if(atmel_getEnable())
	{
		#ifdef USING_LED_NODE
		led_update();
		#endif
		
		#ifdef USING_ADC_NODE 
		adc_update();
		#endif

		#ifdef USING_OPTOMOTOR_NODE 
		optomotor_update();
		#endif

		#ifdef USING_OFFSETGAIN_NODE 
		offsetgain_update();
		#endif

		#ifdef USING_STEPPER_NODE
		stepper_update();
		#endif

		#ifdef USING_SERVO_NODE
		servo_update();
		#endif

		#ifdef USING_INTEG_NODE
		integ_update();
		#endif

                #ifdef USING_BPASS_NODE
                bpass_update();
                #endif
                                                                                                                                                             
                #ifdef USING_DELTA_NODE
                delta_update();
                #endif

	}
}

void atmel_ini(void)
{
	ini_node(ATMEL_VNIDPOS);
	
	atmelVnidsTypes[0] = BOOL_VAR;
    
    	#ifdef DEBUG
		println ("<INI ATMEL NODE>");
    	#endif	
}
#endif
