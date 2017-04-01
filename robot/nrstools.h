/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     8/2004
    Purpose:  These tools are used for basic nrs communication
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#ifndef NRSTOOLSUSED
#define NRSTOOLSUSED


#include "comm.h"
#include "auxiliar.h"
#include "tools.h"
#include "memory.h"
#include "queries.h"
#include "creates.h"

//GENERAL CONSTANTS

#define EMPTY 0x01		//empty byte
#define STARTSTRING 0x01	//empty byte
#define INIHOPCOUNT 10
#define NRSZERO 0x40		//char zero in NRS format (bit7 set)
#define NAMEBREAKVAL WITH_BITSET('.',7)
#define LISTBREAKVAL WITH_BITSET(',',7)
//#define LISTBREAK ","
#define LISTBREAKSTR ","
//#define LISTBREAK ','
#define UNSZERO 0x40		//number zero

#define NRS_TRUE 0x03	
#define NRS_FALSE 0x02	

//	COMMUNICATION REGISTERS
extern s08 MRNC;			//Main Register for NRS Communication
//Flags of the Main Register NRS Communication
#define FROUTE 0			//forward route received
#define RROUTE 1			//return route received
#define BROADC 2			//is broadcast
#define WASZERO 3			//hop count was zero
#define HOPC 4				//hop count received
#define ISMINE 5			//msg for me				(if the target CID its mine, if route is empty, if its broadcast)
#define TVVN 6				//target vnname given
#define SCID 7				//scid given

extern s08 SRNC;						//Secundary Register for NRS Communication
//Flags of the Secondary NRS Communication
#define RECEIVED_INT	0			//intelligent msg received
#define CREATE_INT	1				//create an intelligent msg
#define TRANSC 2							//translation count given
#define TARG_CID 3						//if the micro will taget CID

extern s08 PRNC;						//Ports Register for NRS Comm
//Flags of the Port register
#define PORTUSED	0x03		//0 1
#define MERGEPORTS	2			//if ports are to be merge
#define NOINPUT		3			//if there were no input
#define PORTOUT		0x30		//4 5
//#define NOTTUSED	6
//#define NOTTUSED	7

// 'functions' used for the port register
#define SET_POUT(x) SET_BYTE(PRNC,PORTOUT&((x)<<4))
#define SET_PIN(x) SET_BYTE(PRNC,PORTUSED&(x))
#define GET_POUT BYTE_UPTO(PRNC>>4,1)
#define GET_PIN BYTE_UPTO(PRNC,1)

//	INTELLIGENT FIELDS
#define EOIM 1
#define FORWARD_ROUTE 2
#define RETURN_ROUTE 3
#define IS_BROADCAST 4
#define HOP_COUNT 5
#define TARGET_COMPONENT_CID 6
#define ACKNOWLEDGE_MESSAGE 7
#define ACKNOWLEDGE_TARGET 8
#define FAILED_ROUTE_MESSAGE 9
#define FAILED_ROUTE_TARGET 10
#define MESSAGE_ID 11
#define TARGET_VNNAME 15
#define SOURCE_COMPONENT_CID 14
#define TRANSLATION_COUNT 13

//	headers
#define QUERY 0
#define REPLY 1
#define CREATE 2
#define DELETE 3
#define NUMHEADERNAME 4			//this is always the last, #elements 

extern ring * uartout;			//tmp variables where the msg is built
extern ring * CompID;			//this is my CID

//this are just names
extern PGM_P pm_header_vnnames[NUMHEADERNAME+1] PROGMEM;

extern ring * forward_route;		//forward route
extern ring * return_route;		//return route
extern t_int hop_count;			//hop counter
extern ring * target_component_cid;	//target comp CID
extern ring * target_vnname;		//vnname
extern ring * source_component_cid;	//source comp CID
extern t_int translation_count;		//translation count
extern ring * acknowledge_target;	//ack target
extern ring * failed_route_target;	//failed route target
extern t_int imsgID;
extern t_int VNIDID;			//VNIDID found during int msg

/*
	NOTE:
	if not stated as opposite, values are removed from the head and added to the tail
*/

/*
	get a number value from a ringed list
*/
t_int get_int ( ring ** val);

t_int get_bool ( ring ** val);

/* 
	a segment is removed from the value and added to the variable
*/
ring * get_ringValue ( ring ** variable, ring ** value );

/* 
	a segment is read from the value and copied to the variable
*/
ring * copy_ringValue(ring ** variable, ring * value);

/*
	The number value is added to the ring chain
*/
ring *  add_int ( ring ** ring_head, t_int value );

/* from the VNName, find the VNID
	@param	ring_vnname	VNName
*/
t_int get_vnidid ( ring * ring_vnname );

/* 
	from the VNID, get the VNName
	@param	vnidid
*/
ring * get_vnname ( t_int vnidid );

/*
	remove my route and return the port
*/
s08 remove_my_route ( ring ** ring_head );

/*
	add port at the beginning of the route
*/
void add_route_first (ring ** ring_head, s08 port);

/*
	add port at the end of the route
*/
void add_route_last (ring ** ring_head, s08 port);

/*
	find the position of the string in the list, sample string string is used from
	the position given
*/
#ifdef __MICRO_avr
t_int pgm_find_string ( PGM_P list[], ring * ring_string, s08 from);
#else
t_int pgm_find_string ( const s08 * list[], ring * ring_string, s08 from);
#endif
t_int find_string ( ring * list[], ring * ring_string, s08 from);
t_int contain_string ( ring * list[], ring * ring_string);
t_int find_allString ( ring * list[], ring * ring_string);
t_int find_lastPeriod (ring * ring_string);

s08 position_one (s08 value);

/*
	initialized nrstools
*/
void nrstools_ini(void);

#endif
