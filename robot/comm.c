/*
    Title:    NRS program for the ATMEL MEGA128 & Kteam Khepera II
    Author:   Hugo Rosano & Matthew Howard
    Date:     6/2005
    Purpose:  This is used for basic serial communication functions
    Software: AVR-GCC/GCC to compile
    Hardware: AT MEGA 128/Khepera II
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#include "comm.h"

s08 receiveEnable;

volatile s08 msggot; //remove this later

// this variable hold incoming information arriving at serial 0
volatile msg * UARTIN0;

// this variable hold outgoing information through serial 0
volatile ring * UARTOUT0;

#ifdef __MICRO_avr
// this variable hold incoming information arriving at serial 1
volatile msg * UARTIN1;

// this variable hold outgoing information through serial 1
volatile ring * UARTOUT1;
#endif

#ifdef __ROBOT_kteam
char inchar;

// Code for receiving bytes. 
// If a byte is received, it is added to a chain of rings.
void uart_receive()
{	
	
	int32 status = ser_receive_byte();
	
	//given some input, inchar... 
	while(status != -1){
	
		inchar = (char)status;
						
		//...add inchar to msg
		add_msg_value ( (msg**)(&UARTIN0), inchar );	
		
		if (inchar == BREAK)		//if end of msg received, 0
		{				
			add_msg ( (msg**)(&UARTIN0) );	//create new msg
		}
		
		//get next byte
		status = ser_receive_byte();
		
	}
	
}

// Send out variable UARTOUT0
void uart_send()
{	
	/*while ( UARTOUT0 != NULL )			//while there is data in the UARTOUT ring
	{
		printf ("%c", rm_ring((ring**)(&UARTOUT0)) );		//remove and output
	}
	return;
	*/
	while (UARTOUT0)			//while there is data in the UARTOUT ring
	{
		#ifdef DEBUG
		rm_ring((ring**)(&UARTOUT0));
		#endif
		/*
		#ifndef DEBUG
		printf ("%c\n",rm_ring((ring**)(&UARTOUT0)));		//remove and output
		#endif
		*/
		
		#ifndef DEBUG
		u08 out = rm_ring((ring**)(&UARTOUT0));
		ser_send_buffer(&out,sizeof(u08));
		#endif
		
	}
	return;
	
}

//	Initialise serial
void comm_ini(void)
{
	
	//INI BUFFERS
	receiveEnable = 0;
	//UARTIN0 = NULL;
	//UARTOUT0 = NULL;
	
        //initialises serial manager (sets baudrate to 9600bps)
	ser_reset();
	
	//set the baudrate to 115.2kbps
	//ser_config(UART_BAUD_SELECT0);
	
	//if config_status < 0 then ser_config has failed
	//int32 config_status = ser_config(UART_BAUD_SELECT0);
	//if(config_status < 0) return -1;	
	
	//intialise inchar
	inchar = -1;
	
	#ifdef DEBUG
	println ("\n<INI COMM>");
	#endif
	
}

void rm_srec_junk()
{
	int32 status = ser_receive_byte();
	
	//given some input, inchar... 
	while(status != -1){
		
		#ifdef DEBUG
		inchar = (char)status;
		printf("%c\n",inchar);
		#endif
					
		//get next byte
		status = ser_receive_byte();
	}   
}



#endif

#ifdef __MICRO_avr
void uart_send()
{
	uart_send0();
	uart_send1();
}

/*
	Code for UART 0 interruption
*/
SIGNAL(SIG_UART0_RECV)
{
    s08 inchar = UDR0;			//take byte from the UDR register
	add_msg_value ( (msg**)(&UARTIN0), inchar );	//add to the tail of the last message received (FIFO)
	
    if (inchar == BREAK)		//if end of msg received, 0
    {
		add_msg ( (msg**)(&UARTIN0) );	//create new msg
    }
}

// Interruption called whenever a byte is send
SIGNAL(SIG_UART0_TRANS)
{
}

//	Sends out variable UARTOUT0 
void uart_send0 ()
{
	#ifdef DEBUG
	if (UARTOUT0)
	{
		printl("<Sending UART0 ");
		printRing( (ring*) UARTOUT0);
		println(">");
	}
	#endif
	while (UARTOUT0)			//while there is data in the UARTOUT ring
	{
		#ifdef DEBUG
		rm_ring((ring**)(&UARTOUT0));
		#endif
		#ifndef DEBUG
			s0out(rm_ring((ring**)(&UARTOUT0)));		//remove and output
		#endif
	}
	return;
}

/*
	Code for UART 1
*/

void uart_send1()
{
	#ifdef DEBUG
	if (UARTOUT1)
	{
		printl("<Sending UART1 ");
		printRing( (ring*) UARTOUT1);
		println(">");
	}
	#endif
	while (UARTOUT1)			//while there is data in the UARTOUT ring
	{
		s1out ( rm_ring ((ring**)(&UARTOUT1)) );	//remove and output
	}
	return;
}

// Interruption called whenever a byte is send
SIGNAL(SIG_UART1_TRANS)
{
}

/*
	Code for UART 0 interruption
*/
SIGNAL(SIG_UART1_RECV)
{
    s08 inchar = UDR1;								//take byte from UDR
	add_msg_value ( (msg**)(&UARTIN1), inchar );	//add ring to the tail of the last msg
    if (inchar==BREAK)								//if end of msg, 
    {
		add_msg ( (msg**)(&UARTIN1) );				//create new msg
    }
}

/*
	INITIALIZED BOTH SERIALS
*/
void comm_ini(void)
{
		//INI BUFFERS
	receiveEnable = 0;
	UARTIN0 = NULL;
	UARTIN1 = NULL;
	UARTOUT0 = NULL;
	UARTOUT1 = NULL;
	
        //RXCIE RX complete Interrupt Enable
        //TCCIE TX complete Interrupt Enable
		//UDRIE data register empty interrupt enable
        //RXEN  Receiver enable
        //TXEN  Transmitter enable
	UCSR0B = (1<<RXCIE)|(1<<TXCIE)|(1<<RXEN)|(1<<TXEN);
	
        //UPM (UART PARITY MODE) 0 disable, 1 reserve, 2 even, 3 odd
        //USBS (UART Stop Bit Settings) 0->1 bit, 1->2 bits
        //UCSZ2:0 (UART Char size) 0->5bit, 1->6bit, 2->7bit, 3->8bit, 7->9bit
    UCSR0C = (0<<UPM1) | (0<<UPM0) | (1<<USBS)| (1<<UCSZ1) | (1<<UCSZ0);
	
        //SET Baud Rate
    UBRR0H = (u08)UART_BAUD_SELECT0>>8;
    UBRR0L = (u08)UART_BAUD_SELECT0;

        //RXCIE RX complete Interrupt Enable
        //TCCIE TX complete Interrupt Enable
        //RXEN  Receiver enable
        //TXEN  Transmitter enable
    UCSR1B = (1<<RXCIE)|(1<<TXCIE)|(1<<RXEN)|(1<<TXEN);
	
        //UPM (UART PARITY MODE) 0 disable, 1 reserve, 2 even, 3 odd
        //USBS (UART Stop Bit Settings) 0->1 bit, 1->2 bits
        //UCSZ2:0 (UART Char size) 0->5bit, 1->6bit, 2->7bit, 3->8bit, 7->9bit
    UCSR1C = (0<<UPM1) | (0<<UPM0) | (1<<USBS)| (1<<UCSZ1) | (1<<UCSZ0);
	
        //SET Baud Rate
    UBRR1H = (u08)UART_BAUD_SELECT1>>8;
    UBRR1L = (u08)UART_BAUD_SELECT1;

	#ifdef DEBUG
	println ("\n<INI_COMM>");
	#endif
}

#endif

#ifdef COMMDEBUG

void int_printhex (s08 val)
{
	if (val>9){s0out(val + 87);}
	else{s0out(val + 48);}	
}
void int_printByte (s08 val)
{
	s08 low = BYTE_UPTO(val, 3) ;		//low is upto the 3rd position, 4 bits
	s08 high = BYTE_UPTO(val>>4, 3);	//high is the next 4 bits
	int_printhex (high);					//output low byte
	int_printhex (low);						//output high byte
}

/*
	Prints strings stored in a ring_head
	only the lowest 7 bits are used
*/
void printText (ring * ring_head)
{
	#ifdef HEADERDEBUG
	s0out(0x12);
	s0out(0x5e);	
	#endif
	while (ring_head)			//while the head is pointing to info
	{
		s0out( BYTE_UPTO(ring_head->value, 6) );	//output byte upto the 7th bit
		ring_head = ring_head->next;				//point to the next ring
	}
	#ifdef HEADERDEBUG
	s0out(BREAK);
	#endif	
}

/*
	Prints the content of the ringed list in byte representation
*/
void printRing (ring * ring_head)
{
	#ifdef HEADERDEBUG
	s0out(0x12);
	s0out(0x5e);	
	#endif	
	while (ring_head)			//while the head is pointing to info
	{
		int_printByte ( ring_head->value );	//output byte
		ring_head = ring_head->next;	//point to the next ring
	}
	#ifdef HEADERDEBUG
	s0out(BREAK);
	#endif		
}

void println_P (PGM_P text)
{
	t_int cnter=0;
	#ifdef __MICRO_avr
	while ( pgm_read_byte(text+cnter) != '\0')		//while the string does not end
	{	
		s0out( pgm_read_byte(text+cnter++) );			//output value
	}	
	#else
	while ( text[cnter] != '\0')		//while the string does not end
	{	
		s0out( text[cnter++] );			//output value
	}	
	#endif	
	s0out ('\n');			//terminate with a new line
}


/*
	should receive values from 0 to 15
	this funtions prints them as 0 to F
*/
void printhex (s08 val)
{
	#ifdef HEADERDEBUG
	s0out(0x12);
	s0out(0x5e);	
	#endif
	if (val>9)				//if value is above nine use letters
	{
		s0out(val + 87);	//output as char starting with 'a'
	}
	else
	{
		s0out(val + 48);	//output as ASCI char, start at '0'
	}	
	#ifdef HEADERDEBUG
	s0out(BREAK);
	#endif		
}

/*  
	Prints the value in asci hex using serial port 0
	printbyte (169)		sends	'a' '9'
	printbyte (0x8F)	sends	'8' 'f'
	note the use of low case
*/
void printByte (s08 val)
{
	s08 low = BYTE_UPTO(val, 3) ;		//low is upto the 3rd position, 4 bits
	s08 high = BYTE_UPTO(val>>4, 3);	//high is the next 4 bits
	printhex (high);					//output low byte
	printhex (low);						//output high byte
}

/*  
	Prints the value in asci hex using serial port 0
	printbyte (169)		sends	'a' '9'
	printbyte (0x8F)	sends	'8' 'f'
	note the use of low case
*/
void printt_int (t_int val)
{
	s08 lowlow = BYTE_UPTO(val, 3) ;		//low is upto the 3rd position, 4 bits
	s08 lowhigh = BYTE_UPTO(val>>4, 3);	//high is the next 4 bits
	s08 highlow = BYTE_UPTO(val>>8, 3) ;		//low is upto the 3rd position, 4 bits
	s08 highhigh = BYTE_UPTO(val>>12, 3);	//high is the next 4 bits
	printhex (highhigh);					//output low byte
	printhex (highlow);					//output low byte
	printhex (lowhigh);					//output low byte
	printhex (lowlow);						//output high byte
}



//	Prints line in serial port 0, same as println without final \n
void printl_P (PGM_P text)
{	
	t_int cnter=0;
	#ifdef __MICRO_avr
	while ( pgm_read_byte(text+cnter) != '\0')		//while the string does not end
	{	
		s0out( pgm_read_byte(text+cnter++) );			//output value
	}
	#else
	while ( text[cnter] != '\0')		//while the string does not end
	{	
		s0out( text[cnter++] );			//output value
	}
	#endif
}

#endif

