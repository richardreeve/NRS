/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     8/2004
    Purpose:  This is used for nrs communication process
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/
#ifndef NRSCOMMUSED
#define NRSCOMMUSED

#include "comm.h"
#include "nrstools.h"
#include "queries.h"

#ifdef __ROBOT_kteam
#define flush_uartout() create_outBuffers(0)
#endif

//initialized nrs communication
void nrscomm_ini ( void );

void ini_commRegs(void);

// processes information hold in UARTIN(n)
void process_uart ( msg ** m_msg );

// process intelligent ringed list
void process_intelligent ( ring ** ring_head);

// process non-intelligent ringed list
void process_fast ( ring ** ring_head );

// update intelligent msg (if exist), create if not and requires to be
ring * update_intelligent(s08 outPort);

#ifndef __ROBOT_kteam
// flush uart ports
void flush_uartout(void);
#else
void create_outBuffers(s08 port);
#endif

// process the return route and set the output port
void retRoute_processing (ring ** ring_head);

#endif 
