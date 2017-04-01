/*
  Title:    NRS program for the Atmel ATMEGA128
  Author:   Matthew Prowse
  Date:     09/2005
  Purpose:  Handles Servo node functions
  Software: GCC to compile
  Hardware: Atmel ATMEGA128 with a simple dir/step servo driver
  Note:     contact me at: mprowse@inf.ed.ac.uk
*/

#include "servo.h"

const char pm_servo_num[] PROGMEM = ".num";
const char pm_servo_enable[] PROGMEM = ".enable";
const char pm_servo_position[] PROGMEM = ".position";
const char pm_servo_end[] PROGMEM = LISTBREAKSTR;

PGM_P pm_servoVarsVNNames[NUM_SERVO_VARS+1] PROGMEM = {pm_servo_num, pm_servo_enable, pm_servo_position, pm_servo_end};

//basic variables
ring * servoVNNames[MAX_SERVO+1];
t_int servoVnids [MAX_SERVO*NUM_SERVO_VARS];
t_int servoVnidsTypes[NUM_SERVO_VARS];
t_int servoFuses;

int micros, delay, restdelay, xxx;

t_int servo_getNum(t_int which) {
  if GET_BIT(servoFuses, which)
    return intVars[servoVnids[SERVO_NUM]];
  return 0;
}

s08 servo_getEnable(t_int which) {
  if GET_BIT(servoFuses, which)
    return boolVars[servoVnids[SERVO_ENABLE]];
  return 0;
}

t_int servo_getPosition(t_int which) {
  if GET_BIT(servoFuses, which)
    return intVars[servoVnids[SERVO_POSITION]];
  return 0;
}

void servo_update()
{
#define S_PORT PORTE	
#define S_SERVO 5

  t_int which;
  t_int tmpservo;
  t_int tmpposition;

  for(tmpservo = 0;tmpservo<MAX_SERVO;tmpservo++)			//for each servo
    {
      DDRE = 0xff;
      which = servo_getNum(tmpservo);			//get the servo number
	
      if ((which > 0) && (which <= MAX_SERVO))	// if a valid servo number
	{	
	  if (servo_getEnable(which-1)) {
	    tmpposition = servo_getPosition(which-1);
	    delay = 500 + (7.843 * tmpposition);
	    restdelay = 20000 - delay;
	    stepper_enableInterrupts();
	  } else {
	    stepper_disableInterrupts();
	  }
	}

    }
}

void servo_ini()
{
  ini_node(SERVO_VNIDPOS);
	
  xxx=0;
  micros = 0;
  delay = 1500;

  servoVnidsTypes[0] = INT_VAR; //num
  servoVnidsTypes[1] = BOOL_VAR;//enable
  servoVnidsTypes[2] = INT_VAR; //position
	
#ifdef DEBUG
  println ("<INI SERVO NODE>");
#endif

  OCR0 = 0xf0;
  //TCCR0 = (0<<FOC0)|(0<<WGM01)|(0<<COM01)|(0<<COM00)|(0<<WGM00)|(0<<CS02)|(1<<CS01)|(0<<CS00);
  servo_disableInterrupts();
}

void servo_enableInterrupts(void) { TIMSK |= (1<<TOIE0); }
void servo_disableInterrupts(void) { TIMSK &= ~(1<<TOIE0); }

void servo_handleOverflow0(void) {
  TIFR |= (1<<TOV0); // clear interrupt flag
  if (!xxx) {
    micros += 128;
    if (micros >= delay) {
      micros = 0;
      TOGGLE_SIGNAL;
      xxx = 1;
    }
  } else {
    micros += 128;
    if (micros >= restdelay) {
      micros = 0;
      TOGGLE_SIGNAL;
      xxx = 0;
    }
  }
}
               
//SIGNAL(SIG_OVERFLOW0) {
//  servo_handleOverflow0();
//}                          

