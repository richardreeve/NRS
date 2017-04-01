/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     11/2004
    Purpose:  Handles variable allocation
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#ifndef MEMORYUSED
#define MEMORYUSED

#include "auxiliar.h"
#include "tools.h"
#include "nrstools.h"

//	VARIABLE STORAGE CONSTANTS
//	sizes
#define TYPEOFQRCD 15

/*
*	KH2 ROBOT MEMORY REQUIREMENTS:
*
*	node variables:
*	
*	kh2: 1 x bool
*	motor: 9 x int, 2 x void
*	ir: 2 x int, 1 x void
*	led: 2 x int, 2 x void 
*	odometer: 3 x int, 3 x void
*	earsys: 14 x int, 1 x bool, 2 x void
*	integ: 6 x int, 1 x void
*	bandpass: 5 x int, 1 x bool, 1 x void
*	delta: 5 x int, 1 x bool, 1 x void
*
*	max no of nodes:
*
*	1 x kh2		
*	2 x motor
*	8 x ir
*	2 x led
*	2 x odometer
*	1 x earsys
*	12 x integ
*	2 x bandpass
*	4 x delta
*
*	bool: (1x1)+(2x0)+(8x0)+(2x1)+(2x0)+(1x1)+(12x0)+(2x2)+(4x0) = 1+2+1+4 = 8
*	int:  (1x0)+(2x9)+(8x2)+(2x2)+(2x3)+(1x14)+(12x6)+(2x5)+(4x4) = 18+16+4+6+14+72+10+16 = 156
*	(if DEBUG)
* 	void: (1x0)+(2x2)+(8x1)+(2x1)+(2x3)+(1x2)+(12x1)+(2x1)+(4x1) = 4+8+4+6+2+12+2+4 = 42
*	(else)
* 	void: (1x0)+(2x1)+(8x0)+(2x1)+(2x2)+(1x1)(12x0)+(2x0)+(4x0) = 2+2+4+1 = 9
* 
*/

#ifdef __ROBOT_kh2
	#define INSTANCESPERNODE 12		//max instances of a given node
	#define VARIABLESPERTYPE 156		//
	#define HARDMEMORY (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*NUMNODETYPES)

	//	my vars are divided in this categories
	#define BOOL_VAR 0
	#define INT_VAR 1
	#define RING_VAR 2
	#define VOID_VAR 3
	#define MEMORYTYPES 4

	//	my vars
	#define MAX_MYINTVARS 156	//save memory by simplifing this... vnid
	#define MAX_MYBOOLVARS 8
	#define MAX_MYRINGVARS 5
	#ifdef DEBUG
	# define MAX_MYVOIDVARS 42
	#else
	# define MAX_MYVOIDVARS 9
	#endif

	/*
	//some problem with MAX_LED_XXX_VARS here???
	#define MAX_MYINTVARS MAX_KH2_INT_VARS+MAX_LED_INT_VARS+MAX_BPASS_INT_VARS+MAX_DELTA_INT_VARS+MAX_MOTOR_INT_VARS+MAX_ODO_INT_VARS+MAX_INTEG_INT_VARS+MAX_IR_INT_VARS+MAX_ESYS_INT_VARS
	#define MAX_MYBOOLVARS MAX_KH2_BOOL_VARS+MAX_LED_BOOL_VARS+MAX_BPASS_BOOL_VARS+MAX_DELTA_BOOL_VARS+MAX_MOTOR_BOOL_VARS+MAX_ODO_BOOL_VARS+MAX_INTEG_BOOL_VARS+MAX_IR_BOOL_VARS+MAX_ESYS_BOOL_VARS
	#define MAX_MYRINGVARS MAX_KH2_RING_VARS+MAX_LED_RING_VARS+MAX_BPASS_RING_VARS+MAX_DELTA_RING_VARS+MAX_MOTOR_RING_VARS+MAX_ODO_RING_VARS+MAX_INTEG_RING_VARS+MAX_IR_RING_VARS+MAX_ESYS_RING_VARS
	#define MAX_MYVOIDVARS MAX_KH2_VOID_VARS+MAX_LED_VOID_VARS+MAX_BPASS_VOID_VARS+MAX_DELTA_VOID_VARS+MAX_MOTOR_VOID_VARS+MAX_ODO_VOID_VARS+MAX_INTEG_VOID_VARS+MAX_IR_VOID_VARS+MAX_ESYS_VOID_VARS
	*/

#endif
/*
*	KOALA ROBOT MEMORY REQUIREMENTS:
*
*	node variables:
*	
*	kh2: 1 x bool
*	motor: 9 x int, 3 x void
*	ir: 4 x int, 1 x void
*	led: 2 x int, 2 x void 
*	odometer: 3 x int, 3 x void
*	
*	max no of nodes:
*
*	1 x kh2
*	2 x motor
*	16 x ir
*	2 x led
*	2 x odometer
*
*	=> 1 x bool, 92 x int, 32 x void
*/
#ifdef __ROBOT_koa
	#define INSTANCESPERNODE 16		//max
	#define VARIABLESPERTYPE 92		//at the moments, max 8		
	#define HARDMEMORY (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*NUMNODETYPES)

	//	my vars are divided in this categories
	#define BOOL_VAR 0
	#define INT_VAR 1
	#define RING_VAR 2
	#define VOID_VAR 3
	#define MEMORYTYPES 4

	//	my vars
	#define MAX_MYINTVARS 92	//save memory by simplifing this... vnid
	#define MAX_MYBOOLVARS 5
	#define MAX_MYRINGVARS 5
	#define MAX_MYVOIDVARS 32
#endif

#ifdef __MICRO_avr
	#define INSTANCESPERNODE 8		//max
	#define VARIABLESPERTYPE 10		//at the moments, max 8		?possible bug -- creating two motors with 6 int variables. these variables are not created when this number is 12, but work fine when it is 13. Lucky for some!
	#define HARDMEMORY (TYPEOFQRCD*NUMHEADERNAME+INSTANCESPERNODE*NUMNODETYPES)

	//	my vars are divided in this categories
	#define BOOL_VAR 0
	#define INT_VAR 1
	#define RING_VAR 2
	#define VOID_VAR 3
	#define MEMORYTYPES 4

	//	my vars
	#define MAX_MYINTVARS 10	//save memory by simplifing this... vnid
	#define MAX_MYBOOLVARS 5
	#define MAX_MYRINGVARS 5
	#define MAX_MYVOIDVARS 5
#endif



//this are pointers to the variables
extern s08 boolVars[MAX_MYBOOLVARS+1];		//pointers to my bool vars
extern t_int intVars[MAX_MYINTVARS+1];		//pointers to my int vars
extern ring * ringVars[MAX_MYRINGVARS+1];	//pointers to my ring vars
extern s08 voidEvent[MAX_MYVOIDVARS+1];		//pointers to my void vars


void freeVar(t_int which_type, t_int var);
void freeVarNode(t_int * fuses, t_int var);

t_int getNextVar(t_int which_type);

t_int getNextNode(t_int * fuses, t_int maxnum);

s08 getVoidEvent(t_int voidvnid);

t_int getMaxVNID(void);



/*	set the number value to that vnid
	@param	value		number value
	@param	vnid		vnid target
*/
void set_value_vnid (t_int vnid, ring ** value);

s08 get_value_vnid (t_int vnid, ring ** ring_head);

s08 isEqualVNID (t_int vnid1, t_int vnid2);

void check_vnidType(t_int vnid, ring ** answer);

void memory_ini(void);

#endif // MEMORYUSED
