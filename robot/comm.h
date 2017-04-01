/*
    Title:    NRS program for the ATMEL MEGA128 & KTeam Khepera II
    Author:   Hugo Rosano & Matthew Howard
    Date:     6/2005
    Purpose:  This is used for basic serial communication functions
    Software: AVR-GCC/GCC to compile
    Hardware: AT MEGA 128/Khepera II robot
    Note:     contact me at: H.Rosano@ed.ac.uk
*/
#ifndef COMMUSED
#define COMMUSED

#include "auxiliar.h"
#include "tools.h"

/*
*	ROBOT DEFINITIONS
*/
#ifdef __ROBOT_kteam

	#include <stdio.h>
	#include <sys/kos.h>
	
	/*WITH 16MHZ, U2X = 0 
	        2400bps->416    19.2k->51       76.8k->12       0.5M->1
	        4800bps->207    28.8k->34       115.2->8        1M->0
	        9600bps->103    38.4->25        230.4k->3
	        14.4bps->68     57.6->16        250k->3
	*/
	# define UART_BAUD_SELECT0 8	//port configuration for UART0
	
	#define printl(x) printf(x)
	#define println(x) printf(x); printf("\n")
	
	#define BREAK 0
	
	#define s0out(x) printf("%c",x)
	#define MAXNUMPORTS 1
#endif

/*
*	ATMEL DEFINITIONS
*/
#ifdef __MICRO_avr
	#define s0out(X) do{while((0x20&&UCSR0A)==0);{cli();UDR0=X;}while((0x20&&UCSR0A)==0);{sei();}}while(0);		//0x20 represents UDRE USART DATA REGISTER EMPTY
	#define s1out(X) do{while((0x20&&UCSR1A)==0);{cli();UDR1=X;}while((0x20&&UCSR1A)==0);{sei();}}while(0);
	/*WITH 16MHZ, U2X = 0 
	        2400bps->416    19.2k->51       76.8k->12       0.5M->1
	        4800bps->207    28.8k->34       115.2->8        1M->0
	        9600bps->103    38.4->25        230.4k->3
	        14.4bps->68     57.6->16        250k->3
	*/
	#define UART_BAUD_SELECT0 103                       //(F_CPU/(UART_BAUD_RATE*16l)-1)     //port configuration for UART0
	#define UART_BAUD_SELECT1 103                     //(F_CPU/(UART_BAUD_RATE*16l)-1)     //port configuration for UART1
	
	
	#define printl(x) printl_P(PSTR(x))
	#define println(x) println_P(PSTR(x))
	
	#define printf(y,x) printl(x)
	
	#define BREAK 0x00
	#define MAXNUMPORTS 2
	
#endif


extern s08 receiveEnable;

// this variable hold incoming information arriving at serial 0
extern volatile msg * UARTIN0;

// this variable hold outgoing information through serial 0
extern volatile ring * UARTOUT0;

// N.B. these are not needed for the khepera (only one serial port)
#ifdef __MICRO_avr

// this variable hold incoming information arriving at serial 1
extern volatile msg * UARTIN1;

// this variable hold outgoing information through serial 1
extern volatile ring * UARTOUT1;

//	Sends out variable UARTOUT0 
void uart_send0 (void);

//	Sends out variable UARTOUT1
void uart_send1 (void);

#endif

#ifdef __ROBOT_kteam
//method to receive bytes
void uart_receive(void);
void rm_srec_junk();
#endif

//	Sends both
void uart_send(void);

//	Initialize communication 
void comm_ini(void);


#ifdef COMMDEBUG
/*	
	Prints line in serial port 0, ends with \n
	eg println ("hola");	sends  'h' 'o' 'l' 'a' '\n'
*/
//void println (s08 * text);

void println_P (PGM_P text);

//	Prints line in serial port 0, same as println without final \n
//void printl (s08 * text);

/*  
	Prints the value in asci hex using serial port 0
	printbyte (169)		sends	'a' '9'
	printbyte (0x8F)	sends	'8' 'f'
	note the use of low case
*/
void printByte (s08 value);

/*
	Prints strings stored in a ring_head
	only the lowest 7 bits are used
*/
void printText (ring * ring_head);

/*
	Prints the content of the ringed list in byte representation
*/
void printRing (ring * ring_head);

void printl_P (PGM_P text);

void printt_int (t_int val);


#endif


#endif
