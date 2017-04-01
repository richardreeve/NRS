/*
    Title:    NRS program for the Kteam Khepera II
    Author:   Matthew Howard
    Date:     11/2004
    Purpose:  Handles Motor node functions
    Software: GCC to compile
    Hardware: Khepera II
    Note:     contact me at: s0459419@ed.ac.uk
*/

#include "adc.h"

const char pm_adc_num[] PROGMEM = ".num";
const char pm_adc_reading[] PROGMEM = ".reading";
//const char pm_adc_sens[] PROGMEM = ".sens";
const char pm_adc_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_adcVarsVNNames[NUM_ADC_VARS+1] PROGMEM = {pm_adc_num, pm_adc_reading, pm_adc_end};

//basic variables
ring * adcVNNames[MAX_ADC+1];
t_int adcVnids [MAX_ADC*NUM_ADC_VARS];
t_int adcVnidsTypes[NUM_ADC_VARS];
t_int adcFuses;

t_int adc_getNum(t_int which)
{
	if GET_BIT(adcFuses, which)
	{
		return intVars[adcVnids[ADC_NUM]];
	}
	return 0;
}

t_int adc_getReading(t_int which)
{
	if GET_BIT(adcFuses, which)
	{
		return intVars[adcVnids[ADC_READING]];		//ADC_READING = which*NUM_ADC_VARS+1
	}
	return 0;
}

void adc_setReading(t_int which, t_int reading)	//sets nrs adc reading variable
{
	if GET_BIT(adcFuses, which)
	{
		intVars[adcVnids[ADC_READING]] = reading;		
	}
}

void adc_update()
{
	t_int which;
	t_int tmpadc;
	t_int tmpreading;
	//t_int tmpamb;
	
	for(tmpadc = 0;tmpadc<MAX_ADC;tmpadc++)			//for each adc
	{	
		which = adc_getNum(tmpadc);			//get the adc number
	
		if ((which > 0) && (which <= MAX_ADC))	// if a valid adc number
		{	
			SET_BIT(ADCSR, ADIF);

			//SET_BYTE(ADMUX, BYTE_UPTO(which-1, 4));				//choose channel
			ADMUX &= 0xf0;
			ADMUX |= which-1;

			SET_BIT(ADCSR, ADIF);
			
			while (GET_BIT(ADCSR, ADIF) == 0);					//wait until conversion is ready
	        	
			tmpreading = ADCH; // because left-adjusted
			adc_setReading(which-1, tmpreading);	//set nrs value to current sensor value
		}
	}
}

void adc_ini()
{
	ini_node(ADC_VNIDPOS);
	
	adcVnidsTypes[0] = INT_VAR;	//num
	adcVnidsTypes[1] = INT_VAR;	//reading
	
    	#ifdef DEBUG
	println ("<INI ADC NODE>");
    	#endif

        //REFS7:6		0 AREF internal vref off, 1 AVCC, 2 reserved, 3internal 2.56
	//ADLAR5		left adjust result
	//MUX4:0			channel, 0 to 7 normal. other difference
	ADMUX = (0<<REFS1) | (1<<REFS0) | (1<<ADLAR);// | (0<<MUX1) | (0<<MUX0); 
	
	//ADEN	7	ADC enable
	//ADSC	6	ADC start conversion
	//ADFR	5	ADC free running selection
	//ADIF	4	ADC int flag
	//ADIE	3	ADC int enable
	//ADPS 2:0	ADC prescaler selector bits	0-2,1-2,2-4,3-8,4-16,5-32,6-64,7-128
	ADCSR = (1<<ADEN) | (1<<ADSC) | (1<<ADFR)|(1<<ADPS2) |(1<<ADPS1)|(1<<ADPS0);	
}






