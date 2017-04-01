/*
  Title:    NRS program for the Kteam Khepera II
  Author:   Matthew Howard
  Date:     11/2004
  Purpose:  Handles Variable Multiplication and Addition functions
  Software: GCC to compile
  Hardware: Khepera II
  Note:     contact me at: s0459419@ed.ac.uk
*/

#include "optomotor.h"

const char pm_optomotor_num[] PROGMEM = ".num";
const char pm_optomotor_command[] PROGMEM = ".command";
const char pm_optomotor_feedback[] PROGMEM = ".feedback";
const char pm_optomotor_output[] PROGMEM = ".output";
const char pm_optomotor_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_optomotorVarsVNNames[NUM_OPTOMOTOR_VARS+1] PROGMEM = {pm_optomotor_num, pm_optomotor_command, pm_optomotor_feedback, pm_optomotor_output, pm_optomotor_end};

//basic variables
ring * optomotorVNNames[MAX_OPTOMOTOR+1];
t_int optomotorVnids [MAX_OPTOMOTOR*NUM_OPTOMOTOR_VARS];
t_int optomotorVnidsTypes[NUM_OPTOMOTOR_VARS];
t_int optomotorFuses;


int feedback_integral;

t_int optomotor_getNum(t_int which) {
  if GET_BIT(optomotorFuses, which)
    return intVars[optomotorVnids[OPTOMOTOR_NUM]];
  return 0;
}

t_int optomotor_getCommand(t_int which) {
  if GET_BIT(optomotorFuses, which)
    return intVars[optomotorVnids[OPTOMOTOR_COMMAND]];
  return 0;
}

t_int optomotor_getFeedback(t_int which) {
  if GET_BIT(optomotorFuses, which)
    return intVars[optomotorVnids[OPTOMOTOR_FEEDBACK]];
  return 0;
}
void optomotor_setOutput(t_int which, t_int out) {
  if GET_BIT(optomotorFuses, which)
    intVars[optomotorVnids[OPTOMOTOR_OUTPUT]] = out;
}


void optomotor_update()
{
  t_int which;
  t_int tmpoptomotor;
	
  t_int tmpcommand; //, tmpcommandoffset, tmpcommandgain;
  t_int tmpfeedback; //, tmpfeedbackoffset, tmpfeedbackgain;
  t_int newcommand;

  for(tmpoptomotor = 0;tmpoptomotor<MAX_OPTOMOTOR;tmpoptomotor++)			//for each optomotor
    {		
      which = optomotor_getNum(tmpoptomotor);			//get the optomotor number
		
      if ((which > 0) && (which <= MAX_OPTOMOTOR))	// if a valid optomotor number (i.e. being used)
	{
	  tmpcommand = optomotor_getCommand(which-1);
	  tmpcommand = (tmpcommand + COMMAND_OFFSET + COMMAND_BIAS) * COMMAND_GAIN;

	  tmpfeedback = optomotor_getFeedback(which-1);
	  tmpfeedback = (tmpfeedback + FEEDBACK_OFFSET + FEEDBACK_BIAS) * FEEDBACK_GAIN;
		  
	  if (PI_CONTROL) {
	    // PI CONTROL

	    feedback_integral += tmpfeedback; // add the feedback to running total

	    newcommand = tmpcommand - (feedback_integral * PI_GAIN);
	  } else {
	    newcommand = tmpcommand;
	  }

	  optomotor_setOutput(which-1, newcommand);
	}
    }
}

void optomotor_ini()
{
  ini_node(OPTOMOTOR_VNIDPOS);
	
  feedback_integral = 0;

  optomotorVnidsTypes[0] = INT_VAR;	//num
  optomotorVnidsTypes[1] = INT_VAR;	//command
  optomotorVnidsTypes[2] = INT_VAR;	//feedback
  optomotorVnidsTypes[3] = INT_VAR;	//output

#ifdef DEBUG
  println ("<INI OPTOMOTOR NODE>");
#endif
}
