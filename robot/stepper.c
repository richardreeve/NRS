/*
  Title:    NRS program for the Atmel ATMEGA128
  Author:   Matthew Prowse
  Date:     09/2005
  Purpose:  Handles Stepper node functions
  Software: GCC to compile
  Hardware: Atmel ATMEGA128 with a simple dir/step stepper driver
  Note:     contact me at: mprowse@inf.ed.ac.uk
*/

#include "stepper.h"

const char pm_stepper_num[] PROGMEM = ".num";
const char pm_stepper_enable[] PROGMEM = ".enable";
const char pm_stepper_microsteps[] PROGMEM = ".microsteps";
const char pm_stepper_speed[] PROGMEM = ".speed";
const char pm_stepper_step[] PROGMEM = ".step";
const char pm_stepper_sensitivity[] PROGMEM = ".sensitivity";
const char pm_stepper_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_stepperVarsVNNames[NUM_STEPPER_VARS+1] PROGMEM = {pm_stepper_num, pm_stepper_enable, pm_stepper_microsteps, pm_stepper_speed, pm_stepper_step, pm_stepper_sensitivity, pm_stepper_end};

//basic variables
ring * stepperVNNames[MAX_STEPPER+1];
t_int stepperVnids [MAX_STEPPER*NUM_STEPPER_VARS];
t_int stepperVnidsTypes[NUM_STEPPER_VARS];
t_int stepperFuses;

//stepper variables
t_int delay;
float precise_delay, fdelay;

t_int stepper_getNum(t_int which) {
  if GET_BIT(stepperFuses, which)
    return intVars[stepperVnids[STEPPER_NUM]];
  return 0;
}

s08 stepper_getEnable(t_int which) {
  if GET_BIT(stepperFuses, which)
    return boolVars[stepperVnids[STEPPER_ENABLE]];
  return 0;
}

t_int stepper_getMicroSteps(t_int which) {
  if GET_BIT(stepperFuses, which)
    return intVars[stepperVnids[STEPPER_MICROSTEPS]];
  return 0;
}

t_int stepper_getSpeed(t_int which) {
  if GET_BIT(stepperFuses, which)
    return intVars[stepperVnids[STEPPER_SPEED]];
  return 0;
}

s08 stepper_CheckVoidEvent (t_int which) {
  if GET_BIT(stepperFuses, which)
    return getVoidEvent(stepperVnids[STEPPER_STEP]);
  return 0;
}

t_int stepper_getSensitivity(t_int which) {
  if GET_BIT(stepperFuses, which)
    return intVars[stepperVnids[STEPPER_SENSITIVITY]];
  return 0;
}

void stepper_update()
{
#define S_PORT PORTE	
#define S_ENABLE 7
#define S_DIR	 6
#define	S_STEP	 5
#define MICROSTEPS stepper_getMicroSteps(which-1)

  t_int which;
  t_int tmpstepper;
  t_int tmpspeed;
  t_int tmpsensitivity;
	
  for(tmpstepper = 0;tmpstepper<MAX_STEPPER;tmpstepper++)			//for each stepper
    {
      DDRE = 0xff;
      which = stepper_getNum(tmpstepper);			//get the stepper number
	
      if ((which > 0) && (which <= MAX_STEPPER))	// if a valid stepper number
	{	

	  if (stepper_getEnable(which-1)) {
	    SET_BIT(S_PORT,S_ENABLE);  // enable the stepper

	    tmpspeed = stepper_getSpeed(which-1);
	    tmpsensitivity = stepper_getSensitivity(which-1);
	    if (abs(tmpspeed)<tmpsensitivity) // (tmpspeed!=0)
	      stepper_disableInterrupts();
	    else {
	      if (tmpspeed < 0) CLEAR_BIT(S_PORT,S_DIR);
	      if (tmpspeed > 0) SET_BIT(S_PORT,S_DIR);

	      precise_delay = (1000/((abs(tmpspeed)*MICROSTEPS)/1.8));
	      
	      //stepspeed = (abs(tmpspeed)*MICROSTEPS)/1.8;
	      //tmpdelay = 1000/stepspeed; // in ms per step

	      // TMR0 prescaler (when TCNT0=0) gives interrupts every...
	      // 1    (001): 16us
	      // 8    (010): 128us
	      // 32   (011): 512us
	      // 64   (100): 1ms (1.024 ms)
	      // 128  (101): 2ms
	      // 256  (110): 4ms
	      // 1024 (111): 16ms
	      //delay = (int) tmpdelay/2;
	      //delay = abs(tmpspeed);

	      stepper_enableInterrupts();
	    }
	    


	  } else {
	    CLEAR_BIT(S_PORT,S_ENABLE);
	    stepper_disableInterrupts();
	  }
	}

      if (stepper_CheckVoidEvent(which-1)) {
      }
    }
}

void stepper_ini()
{
  ini_node(STEPPER_VNIDPOS);
	
  stepperVnidsTypes[0] = INT_VAR; //num
  stepperVnidsTypes[1] = BOOL_VAR;//enable
  stepperVnidsTypes[2] = INT_VAR; //microsteps
  stepperVnidsTypes[3] = INT_VAR; //speed
  stepperVnidsTypes[4] = VOID_VAR;//STEP!
	
#ifdef DEBUG
  println ("<INI STEPPER NODE>");
#endif

  OCR0 = 0xf0;
  TCCR0 = (0<<FOC0)|(0<<WGM01)|(0<<COM01)|(0<<COM00)|(0<<WGM00);//|(0<<CS02)|(1<<CS01)|(1<<CS00);
  stepper_setTimerResolution(8);
  stepper_disableInterrupts();
}

void stepper_enableInterrupts(void) {
	TIMSK |= (1<<TOIE0);
}
void stepper_disableInterrupts(void) { TIMSK &= ~(1<<TOIE0); }

void stepper_setTimerResolution(int r) {
                // 1    (001): 16us
              // 8    (010): 128us
              // 32   (011): 512us
              // 64   (100): 1ms (1.024 ms)
              // 128  (101): 2ms
              // 256  (110): 4ms
              // 1024 (111): 16ms
 switch (r) {
  case 1://16us
	TCCR0 &= 0xf8; TCCR0 |= 1; break;
  case 8://128us
	TCCR0 &= 0xf8; TCCR0 |= 2; break;
  case 32://512us
	TCCR0 &= 0xf8; TCCR0 |= 3; break;
  case 64://1ms
	TCCR0 &= 0xf8; TCCR0 |= 4; break;
  case 128://2ms
	TCCR0 &= 0xf8; TCCR0 |= 5; break;
  case 256://4ms
	TCCR0 &= 0xf8; TCCR0 |= 6; break;
  case 1024://16ms
	TCCR0 &= 0xf8; TCCR0 |= 7; break;
  }
}

void stepper_handleOverflow0(void) {
  TIFR |= (1<<TOV0); // clear interrupt flag

  //TCNT0 = delay;
  fdelay += 0.25;
  if (fdelay >= precise_delay) {
    fdelay = 0;
    TOGGLE_STEP;
  }
}
                      
SIGNAL(SIG_OVERFLOW0) {
  stepper_handleOverflow0();
}                      
