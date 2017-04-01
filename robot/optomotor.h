/*
    Title:    Optomotor Node
    Author:   Matthew Prowse
    Date:     8/2005
    Purpose:  Performs PE and PI control for gaze stabilisation behaviour   
    Software: GCC to compile
    Hardware: Atmel ATMEGA128
    Note:     contact me at: mprowse@inf.ed.ac.uk
*/

#ifndef OPTOMOTORUSED
#define OPTOMOTORUSED

#include "tools.h"
#include "nrstools.h"


// CALIBRATION VALUES

#define PI_CONTROL 1

#define COMMAND_OFFSET -133
#define COMMAND_BIAS 0
#define COMMAND_GAIN 1

#define FEEDBACK_OFFSET -131
#define FEEDBACK_BIAS 0
#define FEEDBACK_GAIN 3

#define PI_GAIN 1


#define MAX_OPTOMOTOR 2			// maximum number of optomotor nodes
#define NUM_OPTOMOTOR_VARS 4            // total number of optomotor variables

#define OPTOMOTOR_VNIDSTARTPOS (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*OPTOMOTOR_VNIDPOS)

#define OPTOMOTOR_NUM      which*NUM_OPTOMOTOR_VARS+0
#define OPTOMOTOR_COMMAND  which*NUM_OPTOMOTOR_VARS+1
#define OPTOMOTOR_FEEDBACK which*NUM_OPTOMOTOR_VARS+2
#define OPTOMOTOR_OUTPUT   which*NUM_OPTOMOTOR_VARS+3

extern const char pm_optomotor_num[] PROGMEM;
extern const char pm_optomotor_command[] PROGMEM;
extern const char pm_optomotor_feedback[] PROGMEM;
extern const char pm_optomotor_output[] PROGMEM;

extern const char pm_optomotor_end[] PROGMEM;
extern PGM_P pm_optomotorVarsVNNames[NUM_OPTOMOTOR_VARS+1] PROGMEM;

extern ring * optomotorVNNames[MAX_OPTOMOTOR+1];
extern t_int optomotorVnids [MAX_OPTOMOTOR*NUM_OPTOMOTOR_VARS];
extern t_int optomotorVnidsTypes[NUM_OPTOMOTOR_VARS];
extern t_int optomotorFuses;

//	Functions returning variables
t_int optomotor_getNum(t_int which);
t_int optomotor_getCommand(t_int which);
t_int optomotor_getFeedback(t_int which);
void optomotor_setOut(t_int which, t_int out);

void optomotor_update(void);

void optomotor_ini(void);

#endif
