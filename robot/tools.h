/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     8/2004
    Purpose:  This is used for basic ringed list and other tools
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#ifndef TOOLSUSED
#define TOOLSUSED

#include "auxiliar.h"
#ifndef __MICRO_avr
#define add_string_P add_string
#endif

#ifdef __MICRO_avr
#define PGM_copy_fromlist(r, l, i) {PGM_P _xk_;memcpy_P(&_xk_,&l[i],sizeof(PGM_P));pgm_copy_rings(r,_xk_);};
#endif

#ifdef __ROBOT_kteam
#define PGM_copy_fromlist(r, l, i) {pgm_copy_rings(r,l[i]);};
#endif

#ifdef __MICRO_avr
#define ADD_string_P(a,x) add_string_P(a,PSTR(x));
#else
#define ADD_string_P(a,x) add_string(a,x);
#endif


/*
	rings are used to dynamically handle a chain of chars.
	The head ring is the only pointer pointing to the last ring (tail)
	Each ring points to the next ring, and the ring before
	tail->next always points to NULL, as well as head->before = NULL
*/
struct struct_ring{
	s08 value;			//byte contained in this ring
	struct struct_ring * next;	//pointer to the next ring
	struct struct_ring * before;	//points to the ring before 
	struct struct_ring * tail;	//points to the tail (only if its the head)
};
typedef struct struct_ring ring;

/*
	Messages are used to dynamically handle a chain of rings
	It behaves as the ring struct, but its field is a pointer to the head of a ring chain
	The head msg is the only pointer pointing to the last msg (tail)
	Each msg points to the next msg, and the msg before
	tail->next always points to NULL, as well as head->before = NULL
*/
struct struct_msg{
	ring * msg_ring;		//pointer pointing to the head of a chain ring
	struct struct_msg * next;	//pointer to the next msg
	struct struct_msg * before;	//pointer to the msg before
	struct struct_msg * tail;	//pointer to the tail (only of its the head)
};
typedef struct struct_msg msg;

/*
	Funtions for using ring structs.
	del_all_ring		deletes all elements attached to the ring_head
	add_ring		creates and add new ring to the tail (FIFO)
	add_ring_head	creates and add new ring to the head	(LIFO)
	add_string		chars of the string are added to the ring chain, bit 7 is set in all of them
	create_string	from the string given alone, a new ring chain is created
	join_rings		ring_head tail points to ring_tail head (& all that implies)
	copy_ring		elements from the origin ring are copied to the tail of the destination ring
	move_ring		elements from the origin ring are moved to the tail of the destination ring
	rm_ring		removes and returns ring (head), (first in)
	rm_ring_tail		removes and returns the tail ring, (last in)
	rm_rings		removes the number of rings specified from the head
	start_with		check if ring_2 starts with ring_1
	is_equal		check if all elements of ring_1 and ring_2 match
*/
//Funtions for adding rings, description above
ring * add_ring_head (ring ** ring_head, s08 val);
ring * add_ring (ring ** ring_head, s08 val);
#ifdef __MICRO_avr
ring * add_string_P (ring ** ring_head, PGM_P text);
#endif
ring * add_string (ring ** ring_head, s08 * text);

ring * create_string (s08 * text);
//Functions for adding many rings at once
ring * join_rings ( ring ** ring_head, ring ** ring_tail);
#ifdef __MICRO_avr
//elements from the origin ring are copied to the tail of the destination ring
ring * pgm_copy_rings (ring ** ring_head_dest, PGM_P ring_head_orig);
#else
//elements from the origin ring are copied to the tail of the destination ring
ring * pgm_copy_rings (ring ** ring_head_dest, const s08 * ring_head_orig);
#endif
ring * copy_rings (ring ** ring_head_dest, ring * ring_head_orig);
ring * move_rings (ring ** ring_head_dest, ring ** ring_head_orig);
//Functions for removing rings
void del_all_ring (ring ** ring_head);
s08 rm_ring (ring ** ring_head);
s08 rm_ring_tail (ring ** ring_head);
ring * rm_rings (ring ** ring_head, t_int nrings);
//Comparison functions
s08 start_with ( ring * ring_1, ring * ring_2);
s08 pgm_start_with (PGM_P ring_1, ring * ring_2);
s08 is_equal (ring * ring_1, ring * ring_2);

/*
	Functions for using msg structs
	add_msg		creates and add a new msg to the tail, the ring pointer points to NULL
	add_msg_value	the byte is addead according to add_ring(s08) to the tial msg ring pointer
	rm_msg			removes the head message, return its ring pointer
*/
msg * add_msg (msg ** msg_head);
msg * add_msg_value (msg ** msg_head, s08 val);
ring * rm_msg (msg ** msg_head);

#endif //ndef TOOLSUSED
