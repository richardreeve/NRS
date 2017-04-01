/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     07/2005
    Purpose:  Handles Ear node functions
    Software: GCC to compile
    Hardware: Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#include "earsys.h"

/*
Four ear model:
--------------

Two ears on each side, cross connected by AN2s:

(Left Ear 2) (Left Ear 1) (Right Ear 1) (Right Ear 2)
     |            |             |             |
     |            |             |             |
     v            v             v             v
    PR2L         PR1L          PR1R          PR2R
    gain         gain          gain          gain
     |            |             |             |
     |           / \           / \            |
     |          /   \         /   \           |
     |         /     \       /     \          |
     v        v       v     v       v         v
  Synth2    Fixed  Synth1 Synth1  Fixed    Synth2
   delay    delay   delay delay   delay     delay
     |        |        \   /        |         |
     |        |         \ /         |         |
     |        |          x          |         |
     |        |         / \         |         |
     |        |        /   \        |         |
     v        v       v     v       v         v
    AN3L     AN1L    AN2R  AN2L    AN1R      AN3R
    mult     mult    mult  mult    mult      mult
      \       |       /      \       |       /
       \      |      /        \      |      /
        \     |     /          \     |     /
         \    |    /            \    |    /
          \   |   /              \   |   /
           \  |  /                \  |  /
            \ | /                  \ | /
             \|/                    \|/
          Left Output           Right Output
*/

const char pm_esys_enable[] PROGMEM = ".enable";
//const char pm_esys_num[] PROGMEM = ".num";

//outputs
const char pm_esys_lsig[] PROGMEM = ".lsig";
const char pm_esys_rsig[] PROGMEM = ".rsig";

//pre gains
const char pm_esys_pr1l[] PROGMEM = ".pr1l";
const char pm_esys_pr2l[] PROGMEM = ".pr2l";
const char pm_esys_pr1r[] PROGMEM = ".pr1r";
const char pm_esys_pr2r[] PROGMEM = ".pr2r";

//mix gains
const char pm_esys_an1l[] PROGMEM = ".an1l";
const char pm_esys_an2l[] PROGMEM = ".an2l";
const char pm_esys_an3l[] PROGMEM = ".an3l";
const char pm_esys_an1r[] PROGMEM = ".an1r";
const char pm_esys_an2r[] PROGMEM = ".an2r";
const char pm_esys_an3r[] PROGMEM = ".an3r";

//synth delays
const char pm_esys_sdel1[] PROGMEM = ".sdel1";
const char pm_esys_sdel2[] PROGMEM = ".sdel2";
#ifdef DEBUG
const char pm_esys_sens[] PROGMEM = ".sens";
#endif
const char pm_esys_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_esysVarsVNNames[NUM_ESYS_VARS+1] PROGMEM = {pm_esys_enable, /*pm_esys_num, */pm_esys_lsig, pm_esys_rsig,
						     pm_esys_pr1l, pm_esys_pr2l, pm_esys_pr1r, pm_esys_pr2r,
						     pm_esys_an1l, pm_esys_an2l, pm_esys_an3l,
						     pm_esys_an1r, pm_esys_an2r, pm_esys_an3r,
						     pm_esys_sdel1, pm_esys_sdel2, 
						     #ifdef DEBUG
						     pm_esys_sens,
						     #endif
						     pm_esys_end};

//basic variables
ring * esysVNNames[MAX_ESYS+1];
t_int esysVnids [MAX_ESYS*NUM_ESYS_VARS];
t_int esysVnidsTypes[NUM_ESYS_VARS];
t_int esysFuses;

//local variables
t_int preg[NUM_SIDES][NUM_PREGS_PER_SIDE];		//preamp gains
t_int nrs_preg[NUM_SIDES][NUM_PREGS_PER_SIDE];
t_int mixg[NUM_SIDES][NUM_MIXGS_PER_SIDE];		//mix gains
t_int nrs_mixg[NUM_SIDES][NUM_MIXGS_PER_SIDE];
t_int sdel[NUM_SDELS];					//synth delays
t_int nrs_sdel[NUM_SDELS];
t_int which = 0;
//s08 num;

s08 esys_getEnable()
{
	if GET_BIT(esysFuses, 0)
	{	
		return boolVars[esysVnids[ESYS_ENABLE]];
	}
	return 0;
}
/*
//find out if in 2- or 4-ear mode
s08 esysCheckNum()
{
	if GET_BIT(esysFuses, 0)
	{
		return getVoidEvent(esysVnids[ESYS_NUM]);
	}
	return 0;
}
*/
void esys_setLsig(t_int sig)	
{
	if GET_BIT(esysFuses, 0)
	{
		intVars[esysVnids[ESYS_LSIG]] = sig;		
	}
}

t_int esys_getLsig()
{
	if GET_BIT(esysFuses, 0)
	{
		return intVars[esysVnids[ESYS_LSIG]];
	}
	return 0;
}

void esys_setRsig(t_int sig)	
{
	if GET_BIT(esysFuses, 0)
	{
		intVars[esysVnids[ESYS_RSIG]] = sig;		
	}
}

t_int esys_getRsig()
{
	if GET_BIT(esysFuses, 0)
	{
		return intVars[esysVnids[ESYS_RSIG]];
	}
	return 0;
}

void esys_getPreg()
{
	if GET_BIT(esysFuses, 0)
	{
		nrs_preg[K_LEFT][PR1L_I] = intVars[esysVnids[ESYS_PR1L]];
		nrs_preg[K_LEFT][PR2L_I] = intVars[esysVnids[ESYS_PR2L]];
		nrs_preg[K_RIGHT][PR1R_I] = intVars[esysVnids[ESYS_PR1R]];
		nrs_preg[K_RIGHT][PR2R_I] = intVars[esysVnids[ESYS_PR2R]];
	}
}

void esys_getMixg()
{
	if GET_BIT(esysFuses, 0)
	{
		nrs_mixg[K_LEFT][AN1L_I] = intVars[esysVnids[ESYS_AN1L]];
		nrs_mixg[K_LEFT][AN2L_I] = intVars[esysVnids[ESYS_AN2L]];
		nrs_mixg[K_LEFT][AN3L_I] = intVars[esysVnids[ESYS_AN3L]];
		nrs_mixg[K_RIGHT][AN1R_I] = intVars[esysVnids[ESYS_AN1R]];
		nrs_mixg[K_RIGHT][AN2R_I] = intVars[esysVnids[ESYS_AN2R]];
		nrs_mixg[K_RIGHT][AN3R_I] = intVars[esysVnids[ESYS_AN3R]];
	}
}

void esys_getDelays()
{
	if GET_BIT(esysFuses, 0)
	{
		nrs_sdel[SDEL1_I] = intVars[esysVnids[ESYS_SDEL1]];
		nrs_sdel[SDEL2_I] = intVars[esysVnids[ESYS_SDEL2]];
	}
}

#ifdef DEBUG
s08 esysCheckVoidEvent()
{
	if GET_BIT(esysFuses, 0)
	{
		return getVoidEvent(esysVnids[ESYS_SENS]);
	}
	return 0;
}
#endif

void esys_update()
{
	if(esys_getEnable())
	{
		
/*		if(esysCheckNum())	//if num is changed
		{
			if(num == TWO_EARS)	//if in two ear mode
			{
				set_num_ears(4);		//set to four ear mode
				num = FOUR_EARS;
				set_LED(EAR_LED1_ON, EAR_LED2_OFF);	//show mode using leds

				#ifdef DEBUG
				printf("ESYS: four ear mode\n\n");
				#endif
			}
			else if(num == FOUR_EARS)	//vice versa
			{
				set_num_ears(2);
				num = TWO_EARS;
				set_LED(EAR_LED1_OFF, EAR_LED2_ON);
				
				#ifdef DEBUG
				printf("ESYS: two ear mode\n\n");
				#endif
			}

		}
*/		
		esys_setLsig(read_ear_out(K_LEFT));	//set nrs variable
		esys_setRsig(read_ear_out(K_RIGHT));	//set nrs variable
		
		set_pregains();
		set_mixgains();
		set_delays();
						
		#ifdef DEBUG
		if ( esysCheckVoidEvent() )	// if a void event occurs
		{
			printf("ESYS:\n");		
			
			printf("[enable] ");
			printf("%d\n",esys_getEnable());
			//printf("%d\n",num);
			
			printf("[pr1l pr2l pr1r pr2r] ");
			printf("%d ",nrs_preg[K_LEFT][PR1L_I]);
			printf("%d ",nrs_preg[K_LEFT][PR2L_I]);
			printf("%d ",nrs_preg[K_RIGHT][PR1R_I]);
			printf("%d\n",nrs_preg[K_RIGHT][PR2R_I]);
			
			printf("[an1l an2l an1r an2r] ");
			printf("%d ",nrs_mixg[K_LEFT][AN1L_I]);
			printf("%d ",nrs_mixg[K_LEFT][AN2L_I]);
			printf("%d ",nrs_mixg[K_LEFT][AN3L_I]);
			printf("%d ",nrs_mixg[K_RIGHT][AN1R_I]);
			printf("%d ",nrs_mixg[K_RIGHT][AN2R_I]);
			printf("%d\n",nrs_mixg[K_RIGHT][AN3R_I]);;
			
			printf("[sdel1 sdel2] ");
			printf("%d ",nrs_sdel[SDEL1_I]);
			printf("%d\n",nrs_sdel[SDEL2_I]);
			
			printf("\n[recorded: lsig rsig] ");
			printf("%d ",esys_getLsig());
			printf("%d\n",esys_getRsig());
			
			printf("[actual: lsig rsig] ");
			printf("%d ",(t_int)read_ear_out(K_LEFT));
			printf("%d\n",(t_int)read_ear_out(K_RIGHT));
			
		}
		#endif
	}
}


void set_pregains()
{	
	esys_getPreg();
	
	for(t_int side = 0; side < NUM_SIDES; side++)
	{
		for(t_int ind = 0; ind < NUM_PREGS_PER_SIDE; ind++)
		{	
			if(preg[side][ind] != nrs_preg[side][ind])
			{
				preg[side][ind] = nrs_preg[side][ind];
				set_preamp_gain(side, ind+1, preg[side][ind]);
			}
		}
	}
}

void set_mixgains()
{
	esys_getMixg();		//get nrs mixgs
	
	for(t_int side = 0; side < NUM_SIDES; side++)	//for each side
	{
		for(t_int ind = 0; ind < NUM_MIXGS_PER_SIDE; ind++)	//for each mixg on that side
		{	
			if(mixg[side][ind] != nrs_mixg[side][ind])	//if nrs has updated that mixg (i.e. if a new value)
			{
				mixg[side][ind] = nrs_mixg[side][ind];		//set the mixg, update local mixg
				set_mix_gain(side, ind+1, mixg[side][ind]);
			}
		}
	}	
}

void set_delays(t_int which, t_int tmpesys)
{
	esys_getDelays();
	
	for(t_int i = 0; i < NUM_SDELS; i++)
	{
		if(sdel[i] != nrs_sdel[i])
		{
			sdel[i] = nrs_sdel[i];
			set_long_delay(i+1, sdel[i]);
		}
	}
}

void esys_ini()
{

	ini_node(ESYS_VNIDPOS);

	esysVnidsTypes[0] = BOOL_VAR;	//enable
	//num ears
	//esysVnidsTypes[1] = VOID_VAR;	//num
	//signals
	esysVnidsTypes[1] = INT_VAR;	//lsig
	esysVnidsTypes[2] = INT_VAR;	//rsig
	//preamp gains
	esysVnidsTypes[3] = INT_VAR;	//pr1l
	esysVnidsTypes[4] = INT_VAR;	//pr2l
	esysVnidsTypes[5] = INT_VAR;	//pr1r
	esysVnidsTypes[6] = INT_VAR;	//pr2r
	//mixer gains
	esysVnidsTypes[7] = INT_VAR;	//an1l
	esysVnidsTypes[8] = INT_VAR;	//an2l
	esysVnidsTypes[9] = INT_VAR;	//an3l
	esysVnidsTypes[10] = INT_VAR;	//an1r
	esysVnidsTypes[11] = INT_VAR;	//an2r
	esysVnidsTypes[12] = INT_VAR;	//an3r
	//delays
	esysVnidsTypes[13] = INT_VAR;	//sdel1
	esysVnidsTypes[14] = INT_VAR;	//sdel2
	#ifdef DEBUG
	esysVnidsTypes[15] = VOID_VAR;	//sens
	#endif
	
	reset_ears();	 
	
	//ini local variables
	
	for(t_int side = 0; side< NUM_SIDES;side++)
	{
		for(t_int pind = 0; pind< NUM_PREGS_PER_SIDE;pind++)
		{
			preg[side][pind] = 0;
			nrs_preg[side][pind] = 0;
		}
		for(t_int mind = 0; mind< NUM_MIXGS_PER_SIDE;mind++)
		{
			mixg[side][mind] = 0;
			nrs_mixg[side][mind] = 0;
		}
	}
	
	for(t_int i = 0; i< NUM_SDELS;i++)
	{
		sdel[i] = 0;
		nrs_sdel[i] = 0;
	}
	
    	#ifdef DEBUG
	println ("<INI ESYS NODE>");
    	#endif
}






