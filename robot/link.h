/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     5/2005
    Purpose:  Handles ATMEL link functions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#ifndef LINKUSED
#define LINKUSED

#include "auxiliar.h"
#include "tools.h"
#include "nrstools.h"

#define WAITING_FOR_ROUTE 0
#define NO_ROUTE_REQUESTED 1
#define ROUTE_RECEIVED 2
#define INTERNAL_LINK 3
#define DESTROY_LINK 4

struct struct_link{
	ring * othersCID;
	ring * route;
	s08 requestedRoute;
	s08 source;
	t_int sourceVNID;
	t_int targetVNID;
	ring * prevValue;
	struct struct_link * next;
};
typedef struct struct_link link;

extern t_int numLinks;

link * create_link(ring ** ring_head);

void link_route_received(ring * tmpring, t_int m_msgID);

void link_sendToTarget(ring * m_route, t_int m_targetVNID, t_int m_sourceVNID );

void delete_link(ring ** ring_head);

void deleteAllLinks(void);

void link_update(void);

void link_ini(void);

#endif

