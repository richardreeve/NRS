/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     8/2004
    Purpose:  Process queries & replies
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#ifndef QUERIESUSED
#define QUERIESUSED

#include "comm.h"
#include "memory.h"
#include "nrstools.h"

#define VNID 0
#define VNNAME 1
#define VNTYPE 2
#define MAXVNID 3
#define NUMBERTYPE 4
#define LANGUAGE 5
#define CID 6
#define MAXPORT 7
#define PORT 8
#define CTYPE 9
#define CSL 10
#define ROUTE 11
#define NUMQUERYREPLY 12//this is always the last, #elements 

#define NETWORKCIDS_NOTINITIALIZED 0
#define NETWORKCIDS_UNKNOWN 1
#define NETWORKCIDS_REQUESTED 2
#define NETWORKCIDS_RECEIVED 3

#define NETWORKSIZE 10

struct struct_networkCIDs{
	ring * componentCID;
	ring * route;
	s08 status;
	t_int QR_msgID;
	t_int routeComplexity;
};
typedef struct struct_networkCIDs str_networkCIDs;

extern t_int total_msgID_count;
extern str_networkCIDs networkCIDs[NETWORKSIZE];
extern PGM_P pm_query_reply[NUMQUERYREPLY+1] PROGMEM;

/*
	Processes queries
	@param		idtype	type of query
	@ring_head	the rest of the message, at this point route & toVNID has already been used
*/
void process_query (t_int idtype, ring ** ring_head);

/*
	Processes replies
	@param		idtype	type of reply
	@ring_head	the rest of the message, at this point route & toVNID has already been used
*/
void process_reply (t_int idtype, ring ** ring_head);

void getRouteToCID(ring ** answer, ring * tryCID);

void queries_ini(void);

#endif

