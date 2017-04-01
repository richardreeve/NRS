/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     11/2004
    Purpose:  Handles Variable Bandpass functions
    Software: GCC to compile
    Hardware: Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#include "bandpass.h"

const char pm_bpass_num[] PROGMEM = ".num";
const char pm_bpass_high[] PROGMEM = ".high";
const char pm_bpass_low[] PROGMEM = ".low";
const char pm_bpass_in[] PROGMEM = ".in";
const char pm_bpass_bout[] PROGMEM = ".bout";
const char pm_bpass_iout[] PROGMEM = ".iout";
const char pm_bpass_inv[] PROGMEM = ".binv";
#ifdef DEBUG
const char pm_bpass_sens[] PROGMEM = ".sens";
#endif
const char pm_bpass_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_bpassVarsVNNames[NUM_BPASS_VARS+1] PROGMEM = {pm_bpass_num,pm_bpass_high, pm_bpass_low, 
						       pm_bpass_in, pm_bpass_bout, pm_bpass_iout,
						       pm_bpass_inv,
						       #ifdef DEBUG
						       pm_bpass_sens,
						       #endif
						       pm_bpass_end};

//basic variables
ring * bpassVNNames[MAX_BPASS+1];
t_int bpassVnids [MAX_BPASS*NUM_BPASS_VARS];
t_int bpassVnidsTypes[NUM_BPASS_VARS];
t_int bpassFuses;

//local variables
//(none)

t_int bpass_getNum(t_int which)
{
	if GET_BIT(bpassFuses, which)
	{
		return intVars[bpassVnids[BPASS_NUM]];
	}
	return 0;
}

//nrs variable accessors
t_int bpass_getHigh(t_int which)
{
	if GET_BIT(bpassFuses, which)
	{	
		return intVars[bpassVnids[BPASS_HIGH]];
	}
	return 0;
}

t_int bpass_getLow(t_int which)
{
	if GET_BIT(bpassFuses, which)
	{
		return intVars[bpassVnids[BPASS_LOW]];
	}
	return 0;
}

t_int bpass_getIn(t_int which)
{
	if GET_BIT(bpassFuses, which)
	{
		return intVars[bpassVnids[BPASS_IN]];		
	}
	return 0;
}

void bpass_setBout(t_int which, t_int out)	//sets nrs bpass prox variable
{
	if GET_BIT(bpassFuses, which)
	{
		boolVars[bpassVnids[BPASS_BOUT]] = out;		
	}
}

void bpass_setIout(t_int which, t_int out)	//sets nrs bpass prox variable
{
	if GET_BIT(bpassFuses, which)
	{
		intVars[bpassVnids[BPASS_IOUT]] = out;		
	}
}

t_int bpass_getInv(t_int which)
{
	if GET_BIT(bpassFuses, which)
	{
		return boolVars[bpassVnids[BPASS_INV]];		
	}
	return 0;
}

#ifdef DEBUG
t_int bpass_getBout(t_int which)
{
	if GET_BIT(bpassFuses, which)
	{
		return boolVars[bpassVnids[BPASS_BOUT]];		
	}
	return 0;
}

t_int bpass_getIout(t_int which)
{
	if GET_BIT(bpassFuses, which)
	{
		return intVars[bpassVnids[BPASS_IOUT]];		
	}
	return 0;
}
s08 bpassCheckVoidEvent (t_int which)
{
	if GET_BIT(bpassFuses, which)
	{
		return getVoidEvent(bpassVnids[BPASS_SENS]);
	}
	return 0;
}
#endif

void bpass_update()
{
	t_int which;
	t_int tmpbpass;
	
	t_int high;
	t_int low;	
	t_int in;
	
	for(tmpbpass = 0;tmpbpass<MAX_BPASS;tmpbpass++)			//for each bpass
	{	
		which = bpass_getNum(tmpbpass);			//get the bpass number
		
		if ((which > 0) && (which <= MAX_BPASS))	// if a valid bpass number (i.e. being used)
		{		
			high = bpass_getHigh(tmpbpass);
			low = bpass_getLow(tmpbpass);
			in = bpass_getIn(tmpbpass);
				
			if(bpass_getInv(tmpbpass)) //if inverted filter
			{	
			         if(in <= low || in >= high) // mprowse
				{
					bpass_setBout(tmpbpass,BP_TRUE);
					bpass_setIout(tmpbpass,in);
				}
				else
				{
					bpass_setBout(tmpbpass,BP_FALSE);
					bpass_setIout(tmpbpass,-1);		//if out of range, set to -1 (error)
				}
			}
			else
			{
			        if(in >= low && in <= high) // mprowse
				{
					bpass_setBout(tmpbpass,BP_TRUE);
					bpass_setIout(tmpbpass,in);
				}
				else if(in < low)
				{
					bpass_setBout(tmpbpass,BP_FALSE);
					bpass_setIout(tmpbpass,low);		//if below out range, set to low
				}
				else if(in > high)
				{
					bpass_setBout(tmpbpass,BP_FALSE);
					bpass_setIout(tmpbpass,high);		//if above range, set to high
				}
			}
			
			
			
			#ifdef DEBUG			
			if ( bpassCheckVoidEvent(tmpbpass) )	// if a void event occurs
			{
				printf("BPASS");		
				printf("%d: ",tmpbpass);
				printf("[high low in bout iout inv]\n");
				printf("%d ",bpass_getHigh(tmpbpass));
				printf("%d ",bpass_getLow(tmpbpass));
				printf("%d ",bpass_getIn(tmpbpass));
				printf("%d ",bpass_getBout(tmpbpass));
				printf("%d ",bpass_getIout(tmpbpass));
				printf("%d\n\n",bpass_getInv(tmpbpass));
			}
			#endif
		}
	}
}

void bpass_ini()
{
	ini_node(BPASS_VNIDPOS);
	
	bpassVnidsTypes[0] = INT_VAR;	//num
	bpassVnidsTypes[1] = INT_VAR;	//high
	bpassVnidsTypes[2] = INT_VAR;	//low
	bpassVnidsTypes[3] = INT_VAR;	//in
	bpassVnidsTypes[4] = BOOL_VAR;	//bout
	bpassVnidsTypes[5] = INT_VAR;	//iout
	bpassVnidsTypes[6] = BOOL_VAR;	//inv
	#ifdef DEBUG
	bpassVnidsTypes[7] = VOID_VAR;	//sens
	#endif
		
    	#ifdef DEBUG
	println ("<INI BPASS NODE>");
    	#endif
}


