/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     8/2004
    Purpose:  MAIN LOOP
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#include "auxiliar.h"

#ifdef __ROBOT_kteam
#include <sys/kos.h>
#endif

#include <stdio.h>

#include "comm.h"

#ifdef USING_LED_NODE
#include "led.h"
#endif
#ifdef USING_ADC_NODE
#include "adc.h"
#endif
#ifdef USING_OPTOMOTOR_NODE
#include "optomotor.h"
#endif
#ifdef USING_OFFSETGAIN_NODE
#include "offsetgain.h"
#endif
#ifdef USING_TIMER_NODE
#include "timer.h"
#endif
#ifdef USING_ATMEL_NODE
#include "atmel.h"
#endif
#ifdef USING_MOTOR_NODE
#include "motor.h"
#endif
#ifdef USING_IR_NODE
#include "ir.h"
#endif
#ifdef USING_ODO_NODE
#include "odometer.h"
#endif
#ifdef USING_ESYS_NODE
#include "earsys.h"
#endif
#ifdef USING_INTEG_NODE
#include "integrator.h"
#endif
#ifdef USING_BPASS_NODE
#include "bandpass.h"
#endif
#ifdef USING_DELTA_NODE
#include "delta.h"
#endif
#ifdef USING_STEPPER_NODE
#include "stepper.h"
#endif
#ifdef USING_SERVO_NODE
#include "servo.h"
#endif


#include "queries.h"
#include "creates.h"
#include "memory.h"
#include "nrstools.h"
#include "nrscomm.h"

int main(void)
{
    #ifdef __ROBOT_kteam
    //call built-in koala initialization code
    bios_reset (); //init the resources of the bios manager
    com_reset (); //init the resources of the communications (i/o) manager
    tim_reset (); //init the resources of the mulit-tasking kernel
    //mot_reset (); //init the resources of the movement manager -- done in motor_ini()
    //sens_reset (); //init the resources of the sensors manager -- done in ir_ini()
    //ser_reset (); //init the resources of the serial manager -- this is done in comm_ini()
    var_reset (); //init the resources of the low-level resources (leds, etc) mananger
    #ifdef __ROBOT_koa  
    ctr_reset (); //init the resources of the control signal (batteries, etc) manager
    #endif
    str_reset (); //init the resources of the string conversion manager
    #endif
  
    #ifdef __MICRO_avr
    DDRF = 0x00;
    DDRC = 0x00;
    DDRD = 0x04;
    DDRE = 0xe1; //(bits 7:5 are used for stepper)
    DDRG = 0xff;
    #endif
    
    // INI ALL INCLUDE FILES, <filename>_ini(), except tools
    comm_ini();
    memory_ini();
    queries_ini();
    creates_ini();
    nrstools_ini();
    nrscomm_ini();
    
    #ifdef USING_ATMEL_NODE	
    atmel_ini();
    #endif
    
    #ifdef __ROBOT_kh2
    kh2_ini();
    #endif

    #ifdef USING_LED_NODE
    led_ini();
    #endif    
    
    #ifdef USING_ADC_NODE
    adc_ini();
    #endif
    
    #ifdef USING_OPTOMOTOR_NODE
    optomotor_ini();
    #endif
    
    #ifdef USING_OFFSETGAIN_NODE
    offsetgain_ini();
    #endif
    
    #ifdef USING_MOTOR_NODE
    motor_ini();
    #endif
    
    #ifdef USING_STEPPER_NODE
    stepper_ini();
    #endif
    
    #ifdef USING_SERVO_NODE
    servo_ini();
    #endif
    
    #ifdef USING_IR_NODE
    ir_ini();
    #endif
  
    #ifdef USING_ODO_NODE
    odo_ini();
    #endif
    
    #ifdef USING_ESYS_NODE
    esys_ini();
    #endif
    
    #ifdef USING_INTEG_NODE
    integ_ini();
    #endif

    #ifdef USING_BPASS_NODE
    bpass_ini();
    #endif
    
    #ifdef USING_DELTA_NODE
    delta_ini();
    #endif
    
    #ifdef __MICRO_avr
    // enable interrupts
    sei();
	
    //show is on
    SET_BIT(PORTG, 4);
    #endif
    
    #ifdef __ROBOT_kteam
    rm_srec_junk();
    #endif    	
    
    // main eternal loop
    while (1) 
	{
		#ifdef __ROBOT_kteam
		uart_receive();
		#endif
		process_uart ( (msg**) (&UARTIN0) );
		uart_send();
		
		#ifdef __MICRO_avr
		process_uart ( (msg**) (&UARTIN1) );
		uart_send();
		#endif
		
		//link_update();
		
	    	#ifdef USING_ATMEL_NODE	
		atmel_update();
		#endif
		
	    	#ifdef __ROBOT_kh2
		kh2_update();
		#endif			
	}
}

