/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     11/2004
    Purpose:  Handles LED node functions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#include "led.h"

const char pm_led_num[] PROGMEM = ".num";
const char pm_led_on[] PROGMEM = ".on";
const char pm_led_holdtime[] PROGMEM = ".holdTime";
const char pm_led_blink[] PROGMEM = ".blink";
const char pm_led_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_ledVarsVNNames[NUM_LED_VARS+1] PROGMEM = {pm_led_num, pm_led_on, pm_led_holdtime, pm_led_blink, pm_led_end};

//basic variables
ring * ledVNNames[MAX_LED+1];
t_int ledVnids [MAX_LED*NUM_LED_VARS];
t_int ledVnidsTypes[NUM_LED_VARS];
t_int ledFuses;

//local variables
t_int ledcounters[MAX_LED];
t_int ledblink[MAX_LED];


#ifdef __ROBOT_kteam
s08 PORTG;
#endif

t_int led_getNum(t_int which)
{
	if GET_BIT(ledFuses, which)
	{
		return intVars[ledVnids[LED_NUM]];
	}
	return 0;
}

s08 led_getOn(t_int which)
{
	if GET_BIT(ledFuses, which)
	{
		return boolVars[ledVnids[LED_ON]];
	}
	return 0;
}

t_int led_getHoldTime(t_int which)
{
	if GET_BIT(ledFuses, which)
	{
		return intVars[ledVnids[LED_HOLDTIME]];
	}
	return 0;
}

s08 ledCheckVoidEvent (t_int which)
{
	if GET_BIT(ledFuses, which)
	{
		return getVoidEvent(ledVnids[LED_BLINK]);
	}
	return 0;
}

void led_update()
{
	t_int which;
	t_int tmpled;
	
	s08 tmpportg = PORTG & 0x10;
	
	for(tmpled = 0;tmpled<MAX_LED;tmpled++)
	{
		which = led_getNum(tmpled);
		if ((which > 0) && (which <= MAX_LED))		
		{
			if ( ledCheckVoidEvent(tmpled) )	
			{
				ledblink[tmpled] = 1;		
			}
			if (ledcounters[tmpled] >= led_getHoldTime(tmpled))
			{
				ledblink[tmpled] = 0;		
				ledcounters[tmpled] = 0;	
			}
			if (ledblink[tmpled]){		
				ledcounters[tmpled]++;
			}
			if (led_getOn(tmpled) )		
			{
				SET_BIT(tmpportg, which-1);	
			}
			if (ledblink[tmpled])
			{
				XOR_BIT(tmpportg, which-1);
			}
		}
	}
	
	PORTG = tmpportg;
	
	#ifdef __ROBOT_kteam
	
	//Switch led on/off according to on/off bits in PORTG
	if(GET_BIT(PORTG, 0)){
		var_on_led(0);
	}else{
		var_off_led(0);
	}
	
	if(GET_BIT(PORTG, 1)){
		var_on_led(1);
	}else{
		var_off_led(1);
	}
	
	#endif

}

void led_ini()
{
	ini_node(LED_VNIDPOS);
	
	ledVnidsTypes[0] = INT_VAR;
	ledVnidsTypes[1] = BOOL_VAR;
	ledVnidsTypes[2] = INT_VAR;
	ledVnidsTypes[3] = VOID_VAR;
    
    #ifdef DEBUG
		println ("<INI LED NODE>");
    #endif
}






