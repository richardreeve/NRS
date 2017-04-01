/*
    Title:    NRS program for the ATMEL MEGA128 & Kteam Khepera II
    Author:   Hugo Rosano and Matthew Howard
    Date:     8/2004
    Purpose:  Process creates and deletions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128/Kteam Khepera II
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#include "creates.h"

const char pm_node[] PROGMEM = "Node";
const char pm_link[] PROGMEM = "Link";
const char pm_createend[] PROGMEM = LISTBREAKSTR;
PGM_P pm_create_delete[NUMCREATEDELETE+1] PROGMEM = {pm_node, pm_link, pm_createend};
	
#ifdef USING_ATMEL_NODE	
const char pm_atmel[] PROGMEM = "Atmel";
#endif
#ifdef USING_KH2_NODE	
const char pm_kh2[] PROGMEM = "Khepera2";
#endif
#ifdef USING_LED_NODE
const char pm_led[] PROGMEM = "LED";
#endif
#ifdef USING_ADC_NODE
const char pm_adc[] PROGMEM = "ADC";
#endif
#ifdef USING_OPTOMOTOR_NODE
const char pm_optomotor[] PROGMEM = "Optomotor";
#endif
#ifdef USING_OFFSETGAIN_NODE
const char pm_offsetgain[] PROGMEM = "OffsetGain";
#endif
#ifdef USING_STEPPER_NODE
const char pm_stepper[] PROGMEM = "Stepper";
#endif
#ifdef USING_SERVO_NODE
const char pm_servo[] PROGMEM = "Servo";
#endif
#ifdef USING_TIMER_NODE
const char pm_timer[] PROGMEM = "Timer";
#endif
#ifdef USING_MOTOR_NODE
const char pm_motor[] PROGMEM = "Motor";
#endif
#ifdef USING_IR_NODE
const char pm_ir[] PROGMEM = "IR";
#endif
#ifdef USING_ODO_NODE
const char pm_odo[] PROGMEM = "Odometer";
#endif
#ifdef USING_ESYS_NODE
const char pm_esys[] PROGMEM = "Earsys";
#endif
#ifdef USING_INTEG_NODE
const char pm_integ[] PROGMEM = "Integrator";
#endif
#ifdef USING_BPASS_NODE
const char pm_bpass[] PROGMEM = "Bandpass";
#endif
#ifdef USING_DELTA_NODE
const char pm_delta[] PROGMEM = "Delta";
#endif



const char pm_nodeend[] PROGMEM = LISTBREAKSTR;
PGM_P pm_node_type_vnname[NUMNODETYPES+1] PROGMEM = {
#ifdef USING_ATMEL_NODE	
pm_atmel, 
#endif
#ifdef USING_KH2_NODE	
pm_kh2, 
#endif
#ifdef USING_LED_NODE
pm_led,
#endif
#ifdef USING_ADC_NODE	
pm_adc, 
#endif
#ifdef USING_OPTOMOTOR_NODE	
pm_optomotor, 
#endif
#ifdef USING_STEPPER_NODE	
pm_stepper, 
#endif
#ifdef USING_SERVO_NODE	
pm_servo, 
#endif
#ifdef USING_MOTOR_NODE
pm_motor,
#endif
#ifdef USING_IR_NODE
pm_ir,
#endif
#ifdef USING_ODO_NODE
pm_odo,
#endif
#ifdef USING_ESYS_NODE
pm_esys,
#endif
#ifdef USING_INTEG_NODE
pm_integ,
#endif
#ifdef USING_BPASS_NODE
pm_bpass,
#endif
#ifdef USING_DELTA_NODE
pm_delta,
#endif
#ifdef USING_OFFSETGAIN_NODE	
pm_offsetgain, 
#endif
#ifdef USING_TIMER_NODE	
pm_timer, 
#endif

pm_nodeend
};


/*
	Processes creations
	@param		idtype	type of creation
	@ring_head	the rest of the message, at this point route & toVNID has already been used
*/

void process_create (t_int idtype, ring ** ring_head)
{
	t_int tmpnum = 0;
	ring * tmpring = NULL;
	ring * tmpVnname = NULL;
	#ifdef DEBUG
	printl ("<Create>");
	#endif
	
	switch (idtype)
	{
		case NODE:
			get_ringValue (&tmpring, ring_head);		//type of node to be created
			#ifdef DEBUG
			printl("<node ");
			printText (tmpring);
			println(">");
			#endif
			rm_ring ( &tmpring );				//remove the first empty byte	
			t_int node_typeid = pgm_find_string ( (PGM_P  *) pm_node_type_vnname, tmpring, 0);	//get which node of that type
			tmpnum = get_int (ring_head);			//suggested vnid
			get_ringValue (&tmpVnname, ring_head);		//vnname		(gets name of node (?))
			rm_ring ( &tmpVnname);				//remove the first empty byte
			
			switch (node_typeid){

				#ifdef USING_ATMEL_NODE
				case ATMEL_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node(tmpVnname, atmelVNNames, atmelVnids, atmelVnidsTypes, NUM_ATMEL_VARS, &atmelFuses, MAX_ATMEL);				
				break;
				#endif
				
				#ifdef USING_KH2_NODE
				case KH2_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node(tmpVnname, kh2VNNames, kh2Vnids, kh2VnidsTypes, NUM_KH2_VARS, &kh2Fuses, MAX_KH2);				
				break;
				#endif
				
				#ifdef USING_LED_NODE
				case LED_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, ledVNNames, ledVnids, ledVnidsTypes, NUM_LED_VARS, &ledFuses, MAX_LED);
				break;
				#endif
				
				#ifdef USING_MOTOR_NODE
				case MOTOR_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, motorVNNames, motorVnids, motorVnidsTypes, NUM_MOTOR_VARS, &motorFuses, MAX_MOTOR);
				break;
				#endif
				
				#ifdef USING_STEPPER_NODE
				case STEPPER_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, stepperVNNames, stepperVnids, stepperVnidsTypes, NUM_STEPPER_VARS, &stepperFuses, MAX_STEPPER);
				break;
				#endif
				
				#ifdef USING_SERVO_NODE
				case SERVO_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, servoVNNames, servoVnids, servoVnidsTypes, NUM_SERVO_VARS, &servoFuses, MAX_SERVO);
				break;
				#endif
				
				#ifdef USING_IR_NODE
				case IR_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, irVNNames, irVnids, irVnidsTypes, NUM_IR_VARS, &irFuses, MAX_IR);
				break;
				#endif
			
				#ifdef USING_ODO_NODE
				case ODO_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, odoVNNames, odoVnids, odoVnidsTypes, NUM_ODO_VARS, &odoFuses, MAX_ODO);
				break;
				#endif
				
				#ifdef USING_ESYS_NODE
				case ESYS_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, esysVNNames, esysVnids, esysVnidsTypes, NUM_ESYS_VARS, &esysFuses, MAX_ESYS);
				break;
				#endif
				
				#ifdef USING_INTEG_NODE
				case INTEG_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, integVNNames, integVnids, integVnidsTypes, NUM_INTEG_VARS, &integFuses, MAX_INTEG);
				break;
				#endif
				
				#ifdef USING_BPASS_NODE
				case BPASS_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, bpassVNNames, bpassVnids, bpassVnidsTypes, NUM_BPASS_VARS, &bpassFuses, MAX_BPASS);
				break;
				#endif
				
				#ifdef USING_DELTA_NODE
				case DELTA_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, deltaVNNames, deltaVnids, deltaVnidsTypes, NUM_DELTA_VARS, &deltaFuses, MAX_DELTA);
				break;
				#endif
				
				#ifdef USING_ADC_NODE
				case ADC_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, adcVNNames, adcVnids, adcVnidsTypes, NUM_ADC_VARS, &adcFuses, MAX_ADC);
				break;
				#endif
				
				#ifdef USING_OPTOMOTOR_NODE
				case OPTOMOTOR_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, optomotorVNNames, optomotorVnids, optomotorVnidsTypes, NUM_OPTOMOTOR_VARS, &optomotorFuses, MAX_OPTOMOTOR);
				break;
				#endif
				
				#ifdef USING_OFFSETGAIN_NODE
				case OFFSETGAIN_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, offsetgainVNNames, offsetgainVnids, offsetgainVnidsTypes, NUM_OFFSETGAIN_VARS, &offsetgainFuses, MAX_OFFSETGAIN);
				break;
				#endif
				
				#ifdef USING_TIMER_NODE
				case TIMER_VNIDPOS:
				#ifdef DEBUG
				printl("<creation ok>");
				#endif
				create_node( tmpVnname, timerVNNames, timerVnids, timerVnidsTypes, NUM_TIMER_VARS, &timerFuses, MAX_TIMER);
				break;
				#endif
			}
			
			del_all_ring(&tmpring);
			del_all_ring(&tmpVnname);
			break;
		case LINK:
			#ifdef DEBUG
			println("<link>");
			#endif
			create_link(ring_head);
			#ifdef DEBUG
			println ("link exit");
			#endif	
	}
	return;
}

/*
	Processes deletions
	@param		idtype	type of deletion
	@ring_head	the rest of the message, at this point route & toVNID has already been used
*/
void process_delete (t_int idtype, ring ** ring_head)
{
			#ifdef DEBUG
			printl ("<ring head ");
			printRing (*ring_head);
			println (">");
			#endif	
	t_int tmpnum=0;
	switch (idtype)
	{
		case NODE:
			tmpnum = get_int(ring_head);
			t_int nodetype = (tmpnum - TYPEOFQRCD*NUMHEADERNAME)/INSTANCESPERNODE;
			tmpnum -= (nodetype*INSTANCESPERNODE +TYPEOFQRCD*NUMHEADERNAME);
			#ifdef DEBUG
			printl ("<nodetype ");
			printByte (nodetype);
			printl (" - node num");
			printByte (tmpnum);
			println(">");
			#endif			
			switch (nodetype){
				#ifdef USING_ATMEL_NODE
				case ATMEL_VNIDPOS:
				#ifdef DEBUG
				println("<deleting ATMEL>");
				#endif
				delete_node(tmpnum, NUM_ATMEL_VARS, atmelVnids, atmelVNNames, atmelVnidsTypes, &atmelFuses);
				break;
				#endif
				
				#ifdef USING_KH2_NODE
				case KH2_VNIDPOS:
				#ifdef DEBUG
				println("<deleting KH2>");
				#endif
				delete_node(tmpnum, NUM_KH2_VARS, kh2Vnids, kh2VNNames, kh2VnidsTypes, &kh2Fuses);
				break;
				#endif
				
				#ifdef USING_LED_NODE
				case LED_VNIDPOS:
				#ifdef DEBUG
				println("<deleting LED>");
				#endif
				delete_node(tmpnum, NUM_LED_VARS, ledVnids, ledVNNames, ledVnidsTypes, &ledFuses);
				break;
				#endif
				
				#ifdef USING_STEPPER_NODE
				case STEPPER_VNIDPOS:
				#ifdef DEBUG
				println("<deleting Stepper>");
				#endif
				delete_node(tmpnum, NUM_STEPPER_VARS, stepperVnids, stepperVNNames, stepperVnidsTypes, &stepperFuses);
				break;
				#endif
				
				#ifdef USING_SERVO_NODE
				case SERVO_VNIDPOS:
				#ifdef DEBUG
				println("<deleting Servo>");
				#endif
				delete_node(tmpnum, NUM_SERVO_VARS, servoVnids, servoVNNames, servoVnidsTypes, &servoFuses);
				break;
				#endif
				
				#ifdef USING_TIMER_NODE
				case TIMER_VNIDPOS:
				#ifdef DEBUG
				println("<deleting TIMER>");
				#endif
				delete_node(tmpnum, NUM_TIMER_VARS, timerVnids, timerVNNames, timerVnidsTypes, &timerFuses);
				break;
				#endif
				
				#ifdef USING_MOTOR_NODE
				case MOTOR_VNIDPOS:
				#ifdef DEBUG
				println("<deleting MOTOR>");
				#endif
				delete_node(tmpnum, NUM_MOTOR_VARS, motorVnids, motorVNNames, motorVnidsTypes, &motorFuses);
				break;
				#endif

				#ifdef USING_IR_NODE
				case IR_VNIDPOS:
				#ifdef DEBUG
				println("<deleting IR>");
				#endif
				delete_node(tmpnum, NUM_IR_VARS, irVnids, irVNNames, irVnidsTypes, &irFuses);
				break;
				#endif
				
				#ifdef USING_ODO_NODE
				case ODO_VNIDPOS:
				#ifdef DEBUG
				println("<deleting ODO>");
				#endif
				delete_node(tmpnum, NUM_ODO_VARS, odoVnids, odoVNNames, odoVnidsTypes, &odoFuses);
				break;
				#endif

				#ifdef USING_ESYS_NODE
				case ESYS_VNIDPOS:
				#ifdef DEBUG
				println("<deleting ESYS>");
				#endif
				delete_node(tmpnum, NUM_ESYS_VARS, esysVnids, esysVNNames, esysVnidsTypes, &esysFuses);
				break;
				#endif
				
				#ifdef USING_INTEG_NODE
				case INTEG_VNIDPOS:
				#ifdef DEBUG
				println("<deleting INTEG>");
				#endif
				delete_node(tmpnum, NUM_INTEG_VARS, integVnids, integVNNames, integVnidsTypes, &integFuses);
				break;
				#endif
				
				#ifdef USING_BPASS_NODE
				case BPASS_VNIDPOS:
				#ifdef DEBUG
				println("<deleting BPASS>");
				#endif
				delete_node(tmpnum, NUM_BPASS_VARS, bpassVnids, bpassVNNames, bpassVnidsTypes, &bpassFuses);
				break;
				#endif
								
				#ifdef USING_DELTA_NODE
				case DELTA_VNIDPOS:
				#ifdef DEBUG
				println("<deleting DELTA>");
				#endif
				delete_node(tmpnum, NUM_DELTA_VARS, deltaVnids, deltaVNNames, deltaVnidsTypes, &deltaFuses);
				break;
				#endif
								
				#ifdef USING_ADC_NODE
				case ADC_VNIDPOS:
				#ifdef DEBUG
				println("<deleting ADC>");
				#endif
				delete_node(tmpnum, NUM_ADC_VARS, adcVnids, adcVNNames, adcVnidsTypes, &adcFuses);
				break;
				#endif

				#ifdef USING_OPTOMOTOR_NODE
				case OPTOMOTOR_VNIDPOS:
				#ifdef DEBUG
				println("<deleting OPTOMOTOR>");
				#endif
				delete_node(tmpnum, NUM_OPTOMOTOR_VARS, optomotorVnids, optomotorVNNames, optomotorVnidsTypes, &optomotorFuses);
				break;
				#endif

				#ifdef USING_OFFSETGAIN_NODE
				case OFFSETGAIN_VNIDPOS:
				#ifdef DEBUG
				println("<deleting OFFSETGAIN>");
				#endif
				delete_node(tmpnum, NUM_OFFSETGAIN_VARS, offsetgainVnids, offsetgainVNNames, offsetgainVnidsTypes, &offsetgainFuses);
				break;
				#endif
			}
			
			break;
		case LINK:
			#ifdef DEBUG
			println("<link>");
			#endif		
			delete_link(ring_head);
			#ifdef DEBUG
			println("link exit");
			#endif
			break;
	}
	#ifdef DEBUG
	println ("end deletion");
	#endif
	del_all_ring(ring_head);
	return;
}

void check_vnidNodeType (t_int vnid, ring ** answer)
{
	t_int vnidtype = (vnid - TYPEOFQRCD*NUMHEADERNAME)/INSTANCESPERNODE;
	vnid -= vnidtype*INSTANCESPERNODE+TYPEOFQRCD*NUMHEADERNAME;
	PGM_copy_fromlist (answer, pm_node_type_vnname, vnid);
}

void set_node_values (ring ** ring_head, t_int vnid)
{
	t_int vnidtype = (vnid - TYPEOFQRCD*NUMHEADERNAME)/INSTANCESPERNODE;
	t_int which = vnid - vnidtype*INSTANCESPERNODE - TYPEOFQRCD*NUMHEADERNAME;	//it HAS to be named 'which'

	switch (vnidtype){
		#ifdef USING_ATMEL_NODE
		case ATMEL_VNIDPOS:
			#ifdef DEBUG
			println ("<INI ATMEL>");
			#endif
			boolVars[atmelVnids[ATMEL_ENABLE]] = get_bool(ring_head);	
		break;
		#endif
		
		#ifdef USING_KH2_NODE
		case KH2_VNIDPOS:
			#ifdef DEBUG
			println ("<INI KH2>");
			#endif
			boolVars[kh2Vnids[KH2_ENABLE]] = get_bool(ring_head);	
		break;
		#endif

		#ifdef USING_LED_NODE
		case LED_VNIDPOS:
			#ifdef DEBUG
			println ("<INI LED>");
			#endif
			intVars[ledVnids[LED_NUM]] = get_int(ring_head);
			boolVars[ledVnids[LED_ON]] = get_bool(ring_head);
			intVars[ledVnids[LED_HOLDTIME]] = get_int(ring_head);
		break;
		#endif
		
		#ifdef USING_ADC_NODE
		case ADC_VNIDPOS:
			#ifdef DEBUG
			println ("<INI ADC>");
			#endif
			intVars[adcVnids[ADC_NUM]] = get_int(ring_head);
//			intVars[adcVnids[ADC_READING]] = get_int(ring_head);
		break;
		#endif

		#ifdef USING_OPTOMOTOR_NODE
		case OPTOMOTOR_VNIDPOS:
			#ifdef DEBUG
			println ("<INI OPTOMOTOR>");
			#endif
			intVars[optomotorVnids[OPTOMOTOR_NUM]] = get_int(ring_head);
			//intVars[optomotorVnids[OPTOMOTOR_COMMANDOFFSET]] = get_int(ring_head);
			//intVars[optomotorVnids[OPTOMOTOR_COMMANDGAIN]] = get_int(ring_head);
			//intVars[optomotorVnids[OPTOMOTOR_FEEDBACKOFFSET]] = get_int(ring_head);
			//intVars[optomotorVnids[OPTOMOTOR_FEEDBACKGAIN]] = get_int(ring_head);
		break;
		#endif

		#ifdef USING_OFFSETGAIN_NODE
		case OFFSETGAIN_VNIDPOS:
			#ifdef DEBUG
			println ("<INI OFFSETGAIN>");
			#endif
			intVars[offsetgainVnids[OFFSETGAIN_NUM]] = get_int(ring_head);
			intVars[offsetgainVnids[OFFSETGAIN_OFFSET]] = get_int(ring_head);
			intVars[offsetgainVnids[OFFSETGAIN_GAIN]] = get_int(ring_head);
		break;
		#endif

		#ifdef USING_STEPPER_NODE
		case STEPPER_VNIDPOS:
			#ifdef DEBUG
			println ("<INI STEPPER>");
			#endif
			intVars[stepperVnids[STEPPER_NUM]] = get_int(ring_head);
			boolVars[stepperVnids[STEPPER_ENABLE]] = get_bool(ring_head);
			intVars[stepperVnids[STEPPER_MICROSTEPS]] = get_int(ring_head);
			intVars[stepperVnids[STEPPER_SENSITIVITY]] = get_int(ring_head);
		break;
		#endif
		
		#ifdef USING_SERVO_NODE
		case SERVO_VNIDPOS:
			#ifdef DEBUG
			println ("<INI SERVO>");
			#endif
			intVars[servoVnids[SERVO_NUM]] = get_int(ring_head);
			boolVars[servoVnids[SERVO_ENABLE]] = get_bool(ring_head);
		break;
		#endif
		
		#ifdef USING_TIMER_NODE
		case TIMER_VNIDPOS:
			#ifdef DEBUG
			println ("<INI TIMER>");
			#endif
			intVars[timerVnids[TIMER_NUM]] = get_int(ring_head);
			boolVars[timerVnids[TIMER_RUN]] = get_bool(ring_head);
			intVars[timerVnids[TIMER_PERIOD]] = get_int(ring_head);
		break;
		#endif		
		
		#ifdef USING_MOTOR_NODE
		case MOTOR_VNIDPOS:
			#ifdef DEBUG
			println ("<INI MOTOR>");
			#endif
			
			intVars[motorVnids[MOTOR_NUM]] = get_int(ring_head);
			intVars[motorVnids[MOTOR_MODE]] = get_int(ring_head);
			#ifdef DEBUG
			println ("<Initial Values [num mode]>");
			printf("%d ",intVars[motorVnids[MOTOR_NUM]]);
			printf("%d\n",intVars[motorVnids[MOTOR_MODE]]);
			#endif
			
			intVars[motorVnids[MOTOR_SPEED]] = get_int(ring_head);
			intVars[motorVnids[MOTOR_POS]] = get_int(ring_head);
			#ifdef DEBUG
			println ("<Initial Values [speed pos]>");
			printf("%d ",intVars[motorVnids[MOTOR_SPEED]]);
			printf("%d\n",intVars[motorVnids[MOTOR_POS]]);
			#endif
			
			intVars[motorVnids[MOTOR_MSPEED]] = get_int(ring_head);
			intVars[motorVnids[MOTOR_MACC]] = get_int(ring_head);
					
			#ifdef DEBUG
			println ("<Initial Values [mspeed macc]>");
			printf("%d ",intVars[motorVnids[MOTOR_MSPEED]]);
			printf("%d\n",intVars[motorVnids[MOTOR_MACC]]);
			#endif
			/*
			intVars[motorVnids[MOTOR_KP]] = get_int(ring_head);
			intVars[motorVnids[MOTOR_KI]] = get_int(ring_head);
			intVars[motorVnids[MOTOR_KD]] = get_int(ring_head);
			*/

				
			if(intVars[motorVnids[MOTOR_MODE]] == SPEED_CTRL)
			{
				intVars[motorVnids[MOTOR_KP]] = DEFAULT_SPEED_KP;
				intVars[motorVnids[MOTOR_KI]] = DEFAULT_SPEED_KI;
				intVars[motorVnids[MOTOR_KD]] = DEFAULT_SPEED_KD;
			}
			else
			{
				intVars[motorVnids[MOTOR_KP]] = DEFAULT_POS_KP;
				intVars[motorVnids[MOTOR_KI]] = DEFAULT_POS_KI;
				intVars[motorVnids[MOTOR_KD]] = DEFAULT_POS_KD;			
			}
			
			#ifdef DEBUG
			println ("<Initial Values [kp ki kd]>");
			printf("%d ",intVars[motorVnids[MOTOR_KP]]);
			printf("%d ",intVars[motorVnids[MOTOR_KI]]);
			printf("%d\n",intVars[motorVnids[MOTOR_KD]]);
			#endif
		break;
		#endif
		
		#ifdef USING_IR_NODE
		case IR_VNIDPOS:
			#ifdef DEBUG
			println ("<INI IR>");
			#endif
			intVars[irVnids[IR_NUM]] = get_int(ring_head);
			#ifdef DEBUG
			println ("<Initial Values [num]>");
			printf("%d ",intVars[irVnids[IR_NUM]]);
			printf("\n");
			#endif
		break;
		#endif

		#ifdef USING_ODO_NODE
		case ODO_VNIDPOS:
			#ifdef DEBUG
			println ("<INI ODO>");
			#endif
			intVars[odoVnids[ODO_NUM]] = get_int(ring_head);
			intVars[odoVnids[ODO_SETVAL]] = get_int(ring_head);
			#ifdef DEBUG
			println ("<Initial Values [num setval]>");
			printf("%d ",intVars[odoVnids[ODO_NUM]]);			
			printf("%d ",intVars[odoVnids[ODO_SETVAL]]);
			printf("\n");
			#endif
		break;
		#endif
		
		#ifdef USING_ESYS_NODE
		case ESYS_VNIDPOS:
			#ifdef DEBUG
			println ("<INI ESYS>");
			#endif
			boolVars[esysVnids[ESYS_ENABLE]] = get_bool(ring_head);
			//boolVars[esysVnids[ESYS_NUM]] = get_bool(ring_head);
			
			intVars[esysVnids[ESYS_PR1L]] = get_int(ring_head);
			intVars[esysVnids[ESYS_PR2L]] = get_int(ring_head);
			intVars[esysVnids[ESYS_PR1R]] = get_int(ring_head);
			intVars[esysVnids[ESYS_PR2R]] = get_int(ring_head);
			
			intVars[esysVnids[ESYS_AN1L]] = get_int(ring_head);
			intVars[esysVnids[ESYS_AN2L]] = get_int(ring_head);
			intVars[esysVnids[ESYS_AN3L]] = get_int(ring_head);
			intVars[esysVnids[ESYS_AN1R]] = get_int(ring_head);
			intVars[esysVnids[ESYS_AN2R]] = get_int(ring_head);
			intVars[esysVnids[ESYS_AN3R]] = get_int(ring_head);
			
			intVars[esysVnids[ESYS_SDEL1]] = DEFAULT_SDEL;
			intVars[esysVnids[ESYS_SDEL2]] = DEFAULT_SDEL;
			
			#ifdef DEBUG
			println ("<Initial Values [enable num preg mixg sdel]>");
			printf("%d ",boolVars[esysVnids[ESYS_ENABLE]]);
		//	printf("%d ",boolVars[esysVnids[ESYS_NUM]]);
			printf("%d ",intVars[esysVnids[ESYS_PR1L]]);
			printf("%d ",intVars[esysVnids[ESYS_AN1L]]);
			printf("%d ",intVars[esysVnids[ESYS_SDEL1]]);			
			printf("\n");
			#endif
		break;
		#endif
		
		#ifdef USING_INTEG_NODE
		case INTEG_VNIDPOS:
			#ifdef DEBUG
			println ("<INI INTEG>");
			#endif
			
			intVars[integVnids[INTEG_NUM]] = get_int(ring_head);
			intVars[integVnids[INTEG_DPS]] = get_int(ring_head);
			#ifdef DEBUG
			println ("<Initial Values [num dps]>");
			printf("%d ",intVars[integVnids[INTEG_NUM]]);
			printf("%d ",intVars[integVnids[INTEG_DPS]]);
			printf("\n");
			#endif
			
			intVars[integVnids[INTEG_X]] = get_int(ring_head);
			intVars[integVnids[INTEG_Y]] = get_int(ring_head);
			intVars[integVnids[INTEG_Z]] = get_int(ring_head);									
			#ifdef DEBUG
			println ("<Initial Values [x y z]>");
			printf("%d ",intVars[integVnids[INTEG_X]]);
			printf("%d ",intVars[integVnids[INTEG_Y]]);
			printf("%d ",intVars[integVnids[INTEG_Z]]);
			printf("\n");
			#endif
		break;
		#endif
		
		#ifdef USING_BPASS_NODE
		case BPASS_VNIDPOS:
			#ifdef DEBUG
			println ("<INI BPASS>");
			#endif
			
			intVars[bpassVnids[BPASS_NUM]] = get_int(ring_head);
			intVars[bpassVnids[BPASS_HIGH]] = get_int(ring_head);
			intVars[bpassVnids[BPASS_LOW]] = get_int(ring_head);
			boolVars[bpassVnids[BPASS_INV]] = get_bool(ring_head);
			
			#ifdef DEBUG
			println ("<Initial Values [num high low]>");
			printf("%d ",intVars[bpassVnids[BPASS_NUM]]);
			printf("%d ",intVars[bpassVnids[BPASS_HIGH]]);
			printf("%d ",intVars[bpassVnids[BPASS_LOW]]);
			printf("\n");
			#endif
		break;
		#endif

		#ifdef USING_DELTA_NODE
		case DELTA_VNIDPOS:
			#ifdef DEBUG
			println ("<INI DELTA>");
			#endif
			
			intVars[deltaVnids[DELTA_NUM]] = get_int(ring_head);
			intVars[deltaVnids[DELTA_DEL]] = get_int(ring_head);
			
			#ifdef DEBUG
			println ("<Initial Values [num del]>");
			printf("%d ",intVars[deltaVnids[DELTA_NUM]]);
			printf("%d ",intVars[deltaVnids[DELTA_DEL]]);
			printf("\n");
			#endif
		break;
		#endif
	}	
}

void create_node(ring * tmpvnname, ring * nodevnnames[], t_int nodevnids[], 
		t_int nodevnidstype[], t_int num_vars, 
		t_int * nodefuses, t_int maxNodes)
{
	t_int which =	getNextNode(nodefuses, maxNodes);
    	copy_rings(&nodevnnames[which], tmpvnname);
	for(t_int cnter=0;cnter<num_vars;cnter++)
	{
		nodevnids[which*num_vars+cnter] = getNextVar(nodevnidstype[cnter]);
	}
}

void delete_node(t_int which, t_int num_vars, t_int * nodevnids,
				ring * nodevnnames[], t_int * nodevnidstype, t_int * fusesNode)
{
	#ifdef DEBUG
	printl("<deleting ");
	printText(nodevnnames[which]);
	println(">");
	#endif
	//freeVarNode(fusesNode, intVars[nodevnids[which]]);
	freeVarNode (fusesNode, which);
	for(t_int cnter=0;cnter<num_vars;cnter++)
	{		
		freeVar (nodevnidstype[cnter], nodevnids[which*num_vars+cnter]);
		nodevnids[which*num_vars+cnter] = 0;
	}
	del_all_ring(&nodevnnames[which]);
}

s08 have_vnname(t_int tmpvnid, ring ** tmpvnname, t_int maxNodes, 
		t_int num_vars, PGM_P varsvnnames[],ring * nodevnnames[], t_int vnidstypes[], t_int nodevnids[])
{
	#ifdef DEBUG
	println("<variable vnname>");
	#endif
	t_int vnidtype = (tmpvnid-HARDMEMORY)/VARIABLESPERTYPE;
	tmpvnid -= HARDMEMORY + vnidtype*VARIABLESPERTYPE;
	if (tmpvnid == 0)				//memory space reserved for default value
	{
		return 0;
	}
	for (t_int tmpvar=0;tmpvar<num_vars;tmpvar++)
	{
		if (vnidstypes[tmpvar] == vnidtype)
		{
			for(t_int ind=tmpvar;ind<maxNodes*num_vars;ind+=num_vars)
			{
				if(nodevnids[ind] == tmpvnid)
				{
					copy_rings(tmpvnname, nodevnnames[ind/num_vars]);
					PGM_copy_fromlist ( tmpvnname, varsvnnames, ind%num_vars );
					return 1;
				}
			}			
		}
	}
	return 0;
}

t_int node_get_vnidid( ring * tmpvnname, PGM_P * pm_varsvnnames)
{	
	t_int cnter2 = find_lastPeriod (tmpvnname);
	for (t_int cnter=0; cnter<cnter2; cnter++)
	{
		tmpvnname = tmpvnname->next;
	}
	#ifdef DEBUG
	println("<finding ");
	printText(tmpvnname);
	println(">");
	#endif
    t_int tmpval2 = pgm_find_string ( (PGM_P *) pm_varsvnnames, tmpvnname , 0) ;
	#ifdef DEBUG
	printl("< tmpval2 ");
	printByte (tmpval2);
	println(">");
	#endif
	return tmpval2;
}

void deleteAllNodes()
{	
	#ifdef USING_ADC_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_ADC; tmpnum++)
	{
		delete_node(tmpnum, NUM_ADC_VARS, adcVnids, adcVNNames, adcVnidsTypes, &adcFuses);
	}
	#endif

	#ifdef USING_OPTOMOTOR_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_OPTOMOTOR; tmpnum++)
	{
		delete_node(tmpnum, NUM_OPTOMOTOR_VARS, optomotorVnids, optomotorVNNames, optomotorVnidsTypes, &optomotorFuses);
	}
	#endif

	#ifdef USING_OFFSETGAIN_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_OFFSETGAIN; tmpnum++)
	{
		delete_node(tmpnum, NUM_OFFSETGAIN_VARS, offsetgainVnids, offsetgainVNNames, offsetgainVnidsTypes, &offsetgainFuses);
	}
	#endif

	#ifdef USING_TIMER_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_TIMER; tmpnum++)
	{
		delete_node(tmpnum, NUM_TIMER_VARS, timerVnids, timerVNNames, timerVnidsTypes, &timerFuses);
	}
	#endif

	#ifdef USING_LED_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_LED; tmpnum++)
	{
		delete_node(tmpnum, NUM_LED_VARS, ledVnids, ledVNNames, ledVnidsTypes, &ledFuses);
	}
	#endif
	
	#ifdef USING_STEPPER_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_STEPPER; tmpnum++)
	{
		delete_node(tmpnum, NUM_STEPPER_VARS, stepperVnids, stepperVNNames, stepperVnidsTypes, &stepperFuses);
	}
	#endif

	#ifdef USING_SERVO_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_SERVO; tmpnum++)
	{
		delete_node(tmpnum, NUM_SERVO_VARS, servoVnids, servoVNNames, servoVnidsTypes, &servoFuses);
	}
	#endif
	
	#ifdef USING_MOTOR_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_MOTOR; tmpnum++)
	{
		delete_node(tmpnum, NUM_MOTOR_VARS, motorVnids, motorVNNames, motorVnidsTypes, &motorFuses);
	}
	#endif
	
	#ifdef USING_IR_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_IR; tmpnum++)
	{
		delete_node(tmpnum, NUM_IR_VARS, irVnids, irVNNames, irVnidsTypes, &irFuses);
	}
	#endif
	
	#ifdef USING_ODO_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_ODO; tmpnum++)
	{
		delete_node(tmpnum, NUM_ODO_VARS, odoVnids, odoVNNames, odoVnidsTypes, &odoFuses);
	}
	#endif
	
	#ifdef USING_ESYS_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_ESYS; tmpnum++)
	{
		delete_node(tmpnum, NUM_ESYS_VARS, esysVnids, esysVNNames, esysVnidsTypes, &esysFuses);
	}
	#endif
	
	#ifdef USING_INTEG_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_INTEG; tmpnum++)
	{
		delete_node(tmpnum, NUM_INTEG_VARS, integVnids, integVNNames, integVnidsTypes, &integFuses);
	}
	#endif
	
	#ifdef USING_BPASS_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_BPASS; tmpnum++)
	{
		delete_node(tmpnum, NUM_BPASS_VARS, bpassVnids, bpassVNNames, bpassVnidsTypes, &bpassFuses);
	}
	#endif
	
	#ifdef USING_DELTA_NODE
	for(t_int tmpnum = 0; tmpnum<MAX_DELTA; tmpnum++)
	{
		delete_node(tmpnum, NUM_DELTA_VARS, deltaVnids, deltaVNNames, deltaVnidsTypes, &deltaFuses);
	}
	#endif
}

void ini_node(t_int nodetype)
{
	t_int max=0, num_vars=0, cnter, cnter2, *nodevnids = NULL;
	ring ** nodevnnames = NULL;
	
	switch (nodetype){
		#ifdef USING_ATMEL_NODE
		case ATMEL_VNIDPOS:
			max = MAX_ATMEL;
			num_vars = NUM_ATMEL_VARS;
			nodevnnames = atmelVNNames;
			nodevnids = atmelVnids;
		break;
		#endif
		
		#ifdef USING_KH2_NODE
		case KH2_VNIDPOS:
			max = MAX_KH2;
			num_vars = NUM_KH2_VARS;
			nodevnnames = kh2VNNames;
			nodevnids = kh2Vnids;
		break;
		#endif
		
		#ifdef USING_LED_NODE
		case LED_VNIDPOS:
			max = MAX_LED;
			num_vars = NUM_LED_VARS;
			nodevnnames = ledVNNames;
			nodevnids = ledVnids;
		break;
		#endif
		
		#ifdef USING_ADC_NODE
		case ADC_VNIDPOS:
			max = MAX_ADC;
			num_vars = NUM_ADC_VARS;
			nodevnnames = adcVNNames;
			nodevnids = adcVnids;
		break;
		#endif

		#ifdef USING_OPTOMOTOR_NODE
		case OPTOMOTOR_VNIDPOS:
			max = MAX_OPTOMOTOR;
			num_vars = NUM_OPTOMOTOR_VARS;
			nodevnnames = optomotorVNNames;
			nodevnids = optomotorVnids;
		break;
		#endif
		
		#ifdef USING_OFFSETGAIN_NODE
		case OFFSETGAIN_VNIDPOS:
			max = MAX_OFFSETGAIN;
			num_vars = NUM_OFFSETGAIN_VARS;
			nodevnnames = offsetgainVNNames;
			nodevnids = offsetgainVnids;
		break;
		#endif
		
		#ifdef USING_STEPPER_NODE
		case STEPPER_VNIDPOS:
			max = MAX_STEPPER;
			num_vars = NUM_STEPPER_VARS;
			nodevnnames = stepperVNNames;
			nodevnids = stepperVnids;
		break;
		#endif
		
		#ifdef USING_SERVO_NODE
		case SERVO_VNIDPOS:
			max = MAX_SERVO;
			num_vars = NUM_SERVO_VARS;
			nodevnnames = servoVNNames;
			nodevnids = servoVnids;
		break;
		#endif
		
		#ifdef USING_TIMER_NODE
		case TIMER_VNIDPOS:
			max = MAX_TIMER;
			num_vars = NUM_TIMER_VARS;
			nodevnnames = timerVNNames;
			nodevnids = timerVnids;
		break;
		#endif		
		
		#ifdef USING_MOTOR_NODE
		case MOTOR_VNIDPOS:
			max = MAX_MOTOR;
			num_vars = NUM_MOTOR_VARS;
			nodevnnames = motorVNNames;
			nodevnids = motorVnids;
		break;
		#endif
		
		#ifdef USING_IR_NODE
		case IR_VNIDPOS:
			max = MAX_IR;
			num_vars = NUM_IR_VARS;
			nodevnnames = irVNNames;
			nodevnids = irVnids;
		break;
		#endif
		
		#ifdef USING_ODO_NODE
		case ODO_VNIDPOS:
			max = MAX_ODO;
			num_vars = NUM_ODO_VARS;
			nodevnnames = odoVNNames;
			nodevnids = odoVnids;
		break;
		#endif
		
		#ifdef USING_ESYS_NODE
		case ESYS_VNIDPOS:
			max = MAX_ESYS;
			num_vars = NUM_ESYS_VARS;
			nodevnnames = esysVNNames;
			nodevnids = esysVnids;
		break;
		#endif
		
		#ifdef USING_INTEG_NODE
		case INTEG_VNIDPOS:
			max = MAX_INTEG;
			num_vars = NUM_INTEG_VARS;
			nodevnnames = integVNNames;
			nodevnids = integVnids;
		break;
		#endif
		
		#ifdef USING_BPASS_NODE
		case BPASS_VNIDPOS:
			max = MAX_BPASS;
			num_vars = NUM_BPASS_VARS;
			nodevnnames = bpassVNNames;
			nodevnids = bpassVnids;
		break;
		#endif
		
		#ifdef USING_DELTA_NODE
		case DELTA_VNIDPOS:
			max = MAX_DELTA;
			num_vars = NUM_DELTA_VARS;
			nodevnnames = deltaVNNames;
			nodevnids = deltaVnids;
		break;
		#endif
		
	}
	
	for(cnter=0;cnter<max;cnter++)
	{
		nodevnnames[cnter] = NULL;
		for(cnter2=0;cnter2<num_vars;cnter2++)
		{
			nodevnids[cnter+cnter2] =0;
		}
	}
	nodevnnames[max] = create_string(LISTBREAKSTR);
}

t_int scan_node_vnnames(ring * tmpvnname)
{
        t_int cnter = 0, tmp = -1, tmpval;
        while ((cnter<NUMNODETYPES) && (tmp == -1))
        {
	    	switch (cnter++){
				#ifdef USING_ATMEL_NODE
				case ATMEL_VNIDPOS:
					tmpval = find_allString ( atmelVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return ATMEL_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( atmelVNNames, tmpvnname );
					if (tmpval != -1)
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_atmelVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + atmelVnidsTypes[tmp]*VARIABLESPERTYPE + atmelVnids[tmpval*NUM_ATMEL_VARS+tmp];
					    }
					}
				break;
				#endif
				#ifdef USING_KH2_NODE
				case KH2_VNIDPOS:
					tmpval = find_allString ( kh2VNNames, tmpvnname );
					if (tmpval != -1)
					{
						return KH2_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( kh2VNNames, tmpvnname );
					if (tmpval != -1)
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_kh2VarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + kh2VnidsTypes[tmp]*VARIABLESPERTYPE + kh2Vnids[tmpval*NUM_KH2_VARS+tmp];
					    }
					}
				break;
				#endif
				#ifdef USING_LED_NODE
				case LED_VNIDPOS:
					tmpval = find_allString ( ledVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return LED_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( ledVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_ledVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + ledVnidsTypes[tmp]*VARIABLESPERTYPE + ledVnids[tmpval*NUM_LED_VARS+tmp];
					    }
					}
				break;
				#endif

				#ifdef USING_STEPPER_NODE
				case STEPPER_VNIDPOS:
					tmpval = find_allString ( stepperVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return STEPPER_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( stepperVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_stepperVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + stepperVnidsTypes[tmp]*VARIABLESPERTYPE + stepperVnids[tmpval*NUM_STEPPER_VARS+tmp];
					    }
					}
				break;
				#endif

				#ifdef USING_SERVO_NODE
				case SERVO_VNIDPOS:
					tmpval = find_allString ( servoVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return SERVO_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( servoVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_servoVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + servoVnidsTypes[tmp]*VARIABLESPERTYPE + servoVnids[tmpval*NUM_SERVO_VARS+tmp];
					    }
					}
				break;
				#endif
				
				#ifdef USING_ADC_NODE
				case ADC_VNIDPOS:
					tmpval = find_allString ( adcVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return ADC_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( adcVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_adcVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + adcVnidsTypes[tmp]*VARIABLESPERTYPE + adcVnids[tmpval*NUM_ADC_VARS+tmp];
					    }
					}
				break;
				#endif
				
				#ifdef USING_OPTOMOTOR_NODE
				case OPTOMOTOR_VNIDPOS:
					tmpval = find_allString ( optomotorVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return OPTOMOTOR_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( optomotorVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_optomotorVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + optomotorVnidsTypes[tmp]*VARIABLESPERTYPE + optomotorVnids[tmpval*NUM_OPTOMOTOR_VARS+tmp];
					    }
					}
				break;
				#endif
				
				#ifdef USING_OFFSETGAIN_NODE
				case OFFSETGAIN_VNIDPOS:
					tmpval = find_allString ( offsetgainVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return OFFSETGAIN_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( offsetgainVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_offsetgainVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + offsetgainVnidsTypes[tmp]*VARIABLESPERTYPE + offsetgainVnids[tmpval*NUM_OFFSETGAIN_VARS+tmp];
					    }
					}
				break;
				#endif
				
				#ifdef USING_TIMER_NODE
				case TIMER_VNIDPOS:
					tmpval = find_allString ( timerVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return TIMER_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( timerVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_timerVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + timerVnidsTypes[tmp]*VARIABLESPERTYPE + timerVnids[tmpval*NUM_TIMER_VARS+tmp];
					    }
					}
				break;
				#endif				
				
				#ifdef USING_MOTOR_NODE
				case MOTOR_VNIDPOS:
					tmpval = find_allString ( motorVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return MOTOR_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( motorVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_motorVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + motorVnidsTypes[tmp]*VARIABLESPERTYPE + motorVnids[tmpval*NUM_MOTOR_VARS+tmp];
					    }
					}
				break;
				#endif
				#ifdef USING_IR_NODE
				case IR_VNIDPOS:
					tmpval = find_allString ( irVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return IR_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( irVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_irVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + irVnidsTypes[tmp]*VARIABLESPERTYPE + irVnids[tmpval*NUM_IR_VARS+tmp];
					    }
					}
				break;
				#endif
				#ifdef USING_ODO_NODE
				case ODO_VNIDPOS:
					tmpval = find_allString ( odoVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return ODO_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( odoVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_odoVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + odoVnidsTypes[tmp]*VARIABLESPERTYPE + odoVnids[tmpval*NUM_ODO_VARS+tmp];
					    }
					}
				break;
				#endif
				#ifdef USING_ESYS_NODE
				case ESYS_VNIDPOS:
					tmpval = find_allString ( esysVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return ESYS_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( esysVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_esysVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + esysVnidsTypes[tmp]*VARIABLESPERTYPE + esysVnids[tmpval*NUM_ESYS_VARS+tmp];
					    }
					}
				break;
				#endif
				#ifdef USING_INTEG_NODE
				case INTEG_VNIDPOS:
					tmpval = find_allString ( integVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return INTEG_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( integVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_integVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + integVnidsTypes[tmp]*VARIABLESPERTYPE + integVnids[tmpval*NUM_INTEG_VARS+tmp];
					    }
					}
				break;
				#endif
				#ifdef USING_BPASS_NODE
				case BPASS_VNIDPOS:
					tmpval = find_allString ( bpassVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return BPASS_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( bpassVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_bpassVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + bpassVnidsTypes[tmp]*VARIABLESPERTYPE + bpassVnids[tmpval*NUM_BPASS_VARS+tmp];
					    }
					}
				break;
				#endif
				#ifdef USING_DELTA_NODE
				case DELTA_VNIDPOS:
					tmpval = find_allString ( deltaVNNames, tmpvnname );
					if (tmpval != -1)
					{
						return DELTA_VNIDSTARTPOS + tmpval;
					}
					tmpval = contain_string ( deltaVNNames, tmpvnname );
					if (tmpval != -1)				
					{
						tmp = node_get_vnidid( tmpvnname, (PGM_P*) pm_deltaVarsVNNames);
					    if (tmp != -1)
					    {
							tmp = HARDMEMORY + deltaVnidsTypes[tmp]*VARIABLESPERTYPE + deltaVnids[tmpval*NUM_DELTA_VARS+tmp];
					    }
					}
				break;
				#endif
	    	}
        }
		return tmp;
}

s08 scan_node_vnids(t_int vnidid, ring ** res)
{
    t_int cnter = 0, tmp = 0;    
    
     while ((cnter<NUMNODETYPES) && (tmp == 0))
    {
    	switch (cnter++){
			#ifdef USING_ATMEL_NODE
			case ATMEL_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_ATMEL, NUM_ATMEL_VARS, (PGM_P *) pm_atmelVarsVNNames, atmelVNNames, atmelVnidsTypes, atmelVnids);
			break;
			#endif
			#ifdef USING_KH2_NODE
			case KH2_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_KH2, NUM_KH2_VARS, (PGM_P *) pm_kh2VarsVNNames, kh2VNNames, kh2VnidsTypes, kh2Vnids);
			break;
			#endif
			#ifdef USING_LED_NODE
			case LED_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_LED, NUM_LED_VARS, (PGM_P *) pm_ledVarsVNNames, ledVNNames, ledVnidsTypes, ledVnids);
			break;
			#endif
			#ifdef USING_ADC_NODE
			case ADC_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_ADC, NUM_ADC_VARS, (PGM_P *) pm_adcVarsVNNames, adcVNNames, adcVnidsTypes, adcVnids);
			break;
			#endif
			#ifdef USING_OPTOMOTOR_NODE
			case OPTOMOTOR_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_OPTOMOTOR, NUM_OPTOMOTOR_VARS, (PGM_P *) pm_optomotorVarsVNNames, optomotorVNNames, optomotorVnidsTypes, optomotorVnids);
			break;
			#endif
			#ifdef USING_OFFSETGAIN_NODE
			case OFFSETGAIN_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_OFFSETGAIN, NUM_OFFSETGAIN_VARS, (PGM_P *) pm_offsetgainVarsVNNames, offsetgainVNNames, offsetgainVnidsTypes, offsetgainVnids);
			break;
			#endif
			#ifdef USING_STEPPER_NODE
			case STEPPER_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_STEPPER, NUM_STEPPER_VARS, (PGM_P *) pm_stepperVarsVNNames, stepperVNNames, stepperVnidsTypes, stepperVnids);
			break;
			#endif
			#ifdef USING_SERVO_NODE
			case SERVO_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_SERVO, NUM_SERVO_VARS, (PGM_P *) pm_servoVarsVNNames, servoVNNames, servoVnidsTypes, servoVnids);
			break;
			#endif
			#ifdef USING_TIMER_NODE
			case TIMER_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_TIMER, NUM_TIMER_VARS, (PGM_P *) pm_timerVarsVNNames, timerVNNames, timerVnidsTypes, timerVnids);
			break;
			#endif
			#ifdef USING_MOTOR_NODE
			case MOTOR_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_MOTOR, NUM_MOTOR_VARS, (PGM_P *) pm_motorVarsVNNames, motorVNNames, motorVnidsTypes, motorVnids);
			break;
			#endif
			#ifdef USING_IR_NODE
			case IR_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_IR, NUM_IR_VARS, (PGM_P *) pm_irVarsVNNames, irVNNames, irVnidsTypes, irVnids);
			break;
			#endif
			#ifdef USING_ODO_NODE
			case ODO_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_ODO, NUM_ODO_VARS, (PGM_P *) pm_odoVarsVNNames, odoVNNames, odoVnidsTypes, odoVnids);
			break;
			#endif
			#ifdef USING_ESYS_NODE
			case ESYS_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_ESYS, NUM_ESYS_VARS, (PGM_P *) pm_esysVarsVNNames, esysVNNames, esysVnidsTypes, esysVnids);
			break;
			#endif
			#ifdef USING_INTEG_NODE
			case INTEG_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_INTEG, NUM_INTEG_VARS, (PGM_P *) pm_integVarsVNNames, integVNNames, integVnidsTypes, integVnids);
			break;
			#endif
			#ifdef USING_BPASS_NODE
			case BPASS_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_BPASS, NUM_BPASS_VARS, (PGM_P *) pm_bpassVarsVNNames, bpassVNNames, bpassVnidsTypes, bpassVnids);
			break;
			#endif
			#ifdef USING_DELTA_NODE
			case DELTA_VNIDPOS:
			tmp = have_vnname (vnidid, res, MAX_DELTA, NUM_DELTA_VARS, (PGM_P *) pm_deltaVarsVNNames, deltaVNNames, deltaVnidsTypes, deltaVnids);
			break;
			#endif
    		}
	}
	return tmp;
}

void creates_ini(void){
	
}
