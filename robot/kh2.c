/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     1/2005
    Purpose:  Handles ATMEL node functions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/


#include "kh2.h"
//Khepera-specific variables
const char pm_kh2_enable[] PROGMEM = ".enable";
//const char pm_kh2_reset[] PROGMEM = ".reset";
const char pm_kh2_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_kh2VarsVNNames[NUM_KH2_VARS+1] PROGMEM = {pm_kh2_enable, /*pm_kh2_reset,*/ pm_kh2_end};

//basic variables
ring * kh2VNNames[MAX_KH2+1];
t_int kh2Vnids [NUM_KH2_VARS];
t_int kh2VnidsTypes[NUM_KH2_VARS];
t_int kh2Fuses;

//	Functions returning variables
s08 kh2_getEnable()
{
	if GET_BIT(kh2Fuses, 0)
	{
		return boolVars[kh2Vnids[KH2_ENABLE]];
	}
	return 0;
}

/*
s08 kh2CheckReset ()
{
	if GET_BIT(kh2Fuses, 0)
	{
		return getVoidEvent(kh2Vnids[KH2_RESET]);
	}
	return 0;
}
*/

void kh2_update(void)
{
	
/*	if(kh2CheckReset()){
		deleteAllLinks();	//del all links
		deleteAllNodes();	//del all nodes (apart from the kh2 itself)
	}*/

	link_update();
	
	if(kh2_getEnable())
	{
		//link_update();
	
		#ifdef USING_LED_NODE
		led_update();
		#endif

		#ifdef USING_MOTOR_NODE
		motor_update();
		#endif

		#ifdef USING_IR_NODE
		ir_update();
		#endif

		#ifdef USING_ODO_NODE
		odo_update();
		#endif

		#ifdef USING_ESYS_NODE
		esys_update();
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
	}else{
		#ifdef USING_MOTOR_NODE
		motor_disable();
		#endif
	}
}

void kh2_ini(void)
{
	ini_node(KH2_VNIDPOS);
	
	kh2VnidsTypes[0] = BOOL_VAR;
	//kh2VnidsTypes[1] = VOID_VAR;
    
    	#ifdef DEBUG
		println ("<INI KH2 NODE>");
    	#endif	
}


