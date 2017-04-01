/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     8/2004
    Purpose:  Process creates and deletions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#ifndef CREATESUSED
#define CREATESUSED

#include "comm.h"
#include "memory.h"
#include "nrstools.h"
#include "link.h"

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

#ifdef USING_STEPPER_NODE
#include "stepper.h"
#endif

#ifdef USING_SERVO_NODE
#include "servo.h"
#endif

#ifdef USING_KH2_NODE
#include "kh2.h"
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

// NODES
#ifdef __MICRO_avr
	#ifdef USING_ATMEL_NODE
	#define ATMEL_VNIDPOS 0
	#endif

	#ifdef USING_LED_NODE
	#define LED_VNIDPOS 1
	#endif

	#ifdef USING_ADC_NODE
	#define ADC_VNIDPOS 2
	#endif
	
	#ifdef USING_OPTOMOTOR_NODE
	#define OPTOMOTOR_VNIDPOS 3
	#endif
	
	#ifdef USING_STEPPER_NODE
	#define STEPPER_VNIDPOS 4
	#endif
	
	#ifdef USING_SERVO_NODE
	#define SERVO_VNIDPOS 5
	#endif
	
	#ifdef USING_INTEG_NODE
	#define INTEG_VNIDPOS 6
	#endif
	
	#ifdef USING_BPASS_NODE
	#define BPASS_VNIDPOS 7
	#endif
	
	#ifdef USING_DELTA_NODE
	#define DELTA_VNIDPOS 8
	#endif

	#ifdef USING_OFFSETGAIN_NODE
	#define OFFSETGAIN_VNIDPOS 9
	#endif

#ifdef USING_TIMER_NODE
#define TIMER_VNIDPOS 10
#endif
	
	#define NUMNODETYPES 11
#endif

#ifdef __ROBOT_kteam
	#ifdef USING_KH2_NODE
	#define KH2_VNIDPOS 0
	#endif
	
	#ifdef USING_LED_NODE
	#define LED_VNIDPOS 1
	#endif
	
	#ifdef USING_MOTOR_NODE
	#define MOTOR_VNIDPOS 2
	#endif
	
	#ifdef USING_IR_NODE
	#define IR_VNIDPOS 3
	#endif
	
	#ifdef USING_ODO_NODE
	#define ODO_VNIDPOS 4
	#endif
	
	#ifdef USING_ESYS_NODE
	#define ESYS_VNIDPOS 5
	#endif
	
	#ifdef USING_INTEG_NODE
	#define INTEG_VNIDPOS 6
	#endif
	
	#ifdef USING_BPASS_NODE
	#define BPASS_VNIDPOS 7
	#endif
	
	#ifdef USING_DELTA_NODE
	#define DELTA_VNIDPOS 8
	#endif

	//#define NUMNODETYPES 8
	#define NUMNODETYPES 9
	
#endif

#define NODE 0
#define LINK 1
#define NUMCREATEDELETE 2	//this is always the last, #elements


extern PGM_P pm_create_delete[NUMCREATEDELETE+1] PROGMEM;
extern PGM_P pm_node_type_vnname[NUMNODETYPES+1] PROGMEM;

/*
	Processes creations
	@param		idtype	type of creation
	@ring_head	the rest of the message, at this point route & toVNID has already been used
*/
void process_create (t_int idtype, ring ** ring_head);

/*
	Processes deletions
	@param		idtype	type of deletion
	@ring_head	the rest of the message, at this point route & toVNID has already been used
*/
void process_delete (t_int idtype, ring ** ring_head);

void delete_node(t_int which, t_int num_vars, t_int * nodevnids,
				ring * nodevnnames[], t_int * nodevnidstype, t_int * fusesNode);


				
void set_node_values (ring ** ring_head, t_int vnid);

t_int scan_node_vnnames(ring * tmpvnname);

s08 scan_node_vnids(t_int vnidid, ring ** res);

void check_vnidNodeType (t_int vnid, ring ** answer);

void create_node(ring * tmpvnname, ring * nodevnnames[], t_int nodevnids[], 
		t_int nodevnidstype[], t_int num_vars, t_int * nodefuses, 
		t_int maxNodes);

t_int node_get_vnidid( ring * tmpvnname, PGM_P * pm_varsvnnames);

void deleteAllNodes(void);

void ini_node(t_int nodetype);

void creates_ini(void);

#endif
