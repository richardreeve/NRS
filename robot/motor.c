/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     11/2004
    Purpose:  Handles Motor node functions
    Software: GCC to compile
    Hardware: Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#include "motor.h"

const char pm_motor_num[] PROGMEM = ".num";
const char pm_motor_mode[] PROGMEM = ".mode";
const char pm_motor_speed[] PROGMEM = ".speed";
const char pm_motor_pos[] PROGMEM = ".pos";
const char pm_motor_setpos[] PROGMEM = ".setpos";
const char pm_motor_mspeed[] PROGMEM = ".mspeed";
const char pm_motor_macc[] PROGMEM = ".macc";
const char pm_motor_kp[] PROGMEM = ".kp";
const char pm_motor_ki[] PROGMEM = ".ki";
const char pm_motor_kd[] PROGMEM = ".kd";
#ifdef DEBUG
const char pm_motor_sens[] PROGMEM = ".sens";
#endif
const char pm_motor_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_motorVarsVNNames[NUM_MOTOR_VARS+1] PROGMEM = {pm_motor_num, pm_motor_mode, 
						       pm_motor_speed, pm_motor_pos, pm_motor_setpos,
						       pm_motor_mspeed, pm_motor_macc,
						       pm_motor_kp, pm_motor_ki, pm_motor_kd,
						       #ifdef DEBUG 
						       pm_motor_sens, 
						       #endif
						       pm_motor_end};

//basic variables
ring * motorVNNames[MAX_MOTOR+1];
t_int motorVnids [MAX_MOTOR*NUM_MOTOR_VARS];
t_int motorVnidsTypes[NUM_MOTOR_VARS];
t_int motorFuses;

//local variables
t_int last[MAX_MOTOR];
int32 motor_status[MAX_MOTOR];


t_int motor_getNum(t_int which)
{
	if GET_BIT(motorFuses, which)
	{
		return intVars[motorVnids[MOTOR_NUM]];
	}
	return 0;
}

t_int motor_getMode(t_int which)
{
	if GET_BIT(motorFuses, which)
	{
		return intVars[motorVnids[MOTOR_MODE]];
	}
	return 0;
}

t_int motor_getSpeed(t_int which)
{
	if GET_BIT(motorFuses, which)
	{
		return intVars[motorVnids[MOTOR_SPEED]];
	}
	return 0;
}

t_int motor_getPos(t_int which)
{
	if GET_BIT(motorFuses, which)
	{
		return intVars[motorVnids[MOTOR_POS]];
	}
	return 0;
}

t_int motor_getMspeed(t_int which)
{
	if GET_BIT(motorFuses, which)
	{
		return intVars[motorVnids[MOTOR_MSPEED]];
	}
	return 0;
}

t_int motor_getMacc(t_int which)
{
	if GET_BIT(motorFuses, which)
	{
		return intVars[motorVnids[MOTOR_MACC]];
	}
	return 0;
}

s08 motorCheckSetpos (t_int which)
{
	if GET_BIT(motorFuses, which)
	{
		return getVoidEvent(motorVnids[MOTOR_SETPOS]);
	}
	return 0;
}

t_int motor_getKp(t_int which)
{
	if GET_BIT(motorFuses, which)
	{
		return intVars[motorVnids[MOTOR_KP]];
	}
	return 0;
}

t_int motor_getKi(t_int which)
{
	if GET_BIT(motorFuses, which)
	{
		return intVars[motorVnids[MOTOR_KI]];
	}
	return 0;
}

t_int motor_getKd(t_int which)
{
	if GET_BIT(motorFuses, which)
	{
		return intVars[motorVnids[MOTOR_KD]];
	}
	return 0;
}

#ifdef DEBUG
s08 motorCheckVoidEvent (t_int which)
{
	if GET_BIT(motorFuses, which)
	{
		return getVoidEvent(motorVnids[MOTOR_SENS]);
	}
	return 0;
}
#endif

void motor_update()
{
	t_int which;
	t_int tmpmotor;
	t_int mode;
	
	for(tmpmotor = 0;tmpmotor<MAX_MOTOR;tmpmotor++)			//for each motor
	{
		which = motor_getNum(tmpmotor);				//get the motor number
		
		if ((which > 0) && (which <= MAX_MOTOR))		// if a valid motor number i.e. 1 or 2
		{					
			mode = motor_getMode(tmpmotor);				//get control mode
			
			update_pid(which, tmpmotor);
			
			if(mode == SPEED_CTRL)
			{						
				t_int tmpspeed = mot_get_speed(which-1);	//find current speed
				
				t_int newspeed = motor_getSpeed(tmpmotor);	//find the new speed requested
				
				if (tmpspeed != newspeed)			//if new motor speed not equal to current speed
				{	
					if(newspeed < MAX_SPEED && newspeed > MIN_SPEED) //if speed within range
					{
						while(motor_status[which-1] < 0)
						{
							motor_status[which-1] = mot_new_speed_1m(which-1,newspeed);	//set speed to be new speed
						}
						motor_status[which-1] = -1;
					}
					else	//otherwise flash leds
					{
						var_change_led(0);
						var_change_led(1);
						tim_suspend_task(50);
						var_change_led(0);
						var_change_led(1);
						tim_suspend_task(50);
					}

				}
			}
			else if(mode == POS_CTRL)
			{
				update_prof(which, tmpmotor);

				t_int newpos = motor_getPos(tmpmotor);			//find the new pos requested			
								
				if(last[tmpmotor] != newpos)
				{
					#ifdef DEBUG
					printf("<new position set [motor newpos lastpos]>\n");
					printf("%d ",which-1);
					printf("%d ",newpos);
					printf("%d\n",last[tmpmotor]);
					#endif
					
					while(motor_status[which-1] < 0)
					{
						motor_status[which-1] = mot_new_position_1m(which-1,newpos);
					}
					motor_status[which-1] = -1;
					last[tmpmotor] = newpos;
				}
			}
			else if(mode == INCR_CTRL)
			{
				update_prof(which, tmpmotor);
				
				if(motorCheckSetpos(tmpmotor))
				{
					while(motor_status[which-1] < 0)
					{
						motor_status[which-1] = mot_new_position_1m(which-1, motor_getPos(tmpmotor) + mot_get_position(which-1));
					}
					motor_status[which-1] = -1;
					
					#ifdef DEBUG
					printf("<incr to mot positon [motor incr newpos]>\n");
					printf("%d ",which-1);
					printf("%d ",motor_getPos(tmpmotor));
					printf("%ld\n",motor_getPos(tmpmotor) + mot_get_position(which-1));
					#endif
				}
			}
			
			#ifdef DEBUG			
			if ( motorCheckVoidEvent(tmpmotor) )	// if a void event occurs
			{
				printf("MOTOR");		
				printf("%d: ",tmpmotor);
				printf("[mode speed pos]\n");
				printf("%d ",motor_getMode(tmpmotor));
				printf("%d ",motor_getSpeed(tmpmotor));
				printf("%d\n",motor_getPos(tmpmotor));
				printf("[mspeed macc]\n");
				printf("%d ",motor_getMspeed(tmpmotor));
				printf("%d\n",motor_getMacc(tmpmotor));
				printf("[kp ki kd]\n");
				printf("%d ",motor_getKp(tmpmotor));
				printf("%d ",motor_getKi(tmpmotor));
				printf("%d\n\n",motor_getKd(tmpmotor));
			}
			#endif
		}
	}
}	

void motor_disable()
{
	//stop the motors if kh2 node is disabled
	mot_stop();
}

void update_pid(t_int which, t_int tmpmotor)
{	
	t_int mode = motor_getMode(tmpmotor);
	
	if(mode == SPEED_CTRL)
	{
		while(motor_status[which-1] < 0)
		{
			motor_status[which-1] = mot_config_speed_1m(which-1, motor_getKp(tmpmotor), motor_getKi(tmpmotor), motor_getKd(tmpmotor));
		}
		motor_status[which-1] = -1;
	}
	else if(mode == POS_CTRL || mode == INCR_CTRL)
	{
		while(motor_status[which-1] < 0)
		{
			motor_status[which-1] = mot_config_position_1m(which-1, motor_getKp(tmpmotor), motor_getKi(tmpmotor), motor_getKd(tmpmotor));
		}
		motor_status[which-1] = -1;
	}
}

void update_prof(t_int which, t_int tmpmotor)
{	
	while(motor_status[which-1] < 0)
	{
		motor_status[which-1] = mot_config_profil_1m(which-1,motor_getMspeed(tmpmotor),motor_getMacc(tmpmotor));
	}
	motor_status[which-1] = -1;
}

void motor_ini()
{
	ini_node(MOTOR_VNIDPOS);
	
	motorVnidsTypes[0] = INT_VAR;	//num
	motorVnidsTypes[1] = INT_VAR;	//mode
	motorVnidsTypes[2] = INT_VAR;	//speed
	motorVnidsTypes[3] = INT_VAR;	//pos
	motorVnidsTypes[4] = VOID_VAR;	//setpos
	motorVnidsTypes[5] = INT_VAR;	//mspeed
	motorVnidsTypes[6] = INT_VAR;	//macc
	motorVnidsTypes[7] = INT_VAR;	//kp
	motorVnidsTypes[8] = INT_VAR;	//ki
	motorVnidsTypes[9] = INT_VAR;	//kd
	#ifdef DEBUG
	motorVnidsTypes[10] = VOID_VAR;	//sens
	#endif
	
	mot_reset(); //init the resources of the movement manager
	
	for(t_int i = 0; i<MAX_MOTOR;i++){	//init any local variables for both motors		
		last[i] = 0;		//records last position command
		motor_status[i] = -1;	//used to indicate if movement commands succeeded
	}	
	
    	#ifdef DEBUG
	println ("<INI MOTOR NODE>");
    	#endif
}






