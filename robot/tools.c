/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     8/2004
    Purpose:  This is used for basic ringed list and other tools
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/
#include "tools.h"
#include "comm.h"

//deletes all elements attached to the ring_head
void del_all_ring (ring ** ring_head)
{
	while (*ring_head)		//while not end of ring chain
	{
		rm_ring (ring_head);				//remove ring	
	}
}

#ifdef __MICRO_avr
//chars of the string are added to the ring chain, bit 7 is set in all of them
ring * add_string_P (ring ** ring_head, PGM_P text)
{
	t_int cnter=0;
	while ( pgm_read_byte(text+cnter) != '\0')		//while the string does not end
	{	
		add_ring( ring_head, pgm_read_byte(text+cnter++) | BIT(7) );			//output value
	}
	return * ring_head;								//return new ring_head (same as sent)
}
#endif

//chars of the string are added to the ring chain, bit 7 is set in all of them
ring * add_string (ring ** ring_head, s08 * text)
{
	t_int cnter=0;
	while ( text[cnter] != '\0')				//while not the end of the string
	{
		add_ring (ring_head, text[cnter++] | BIT(7) );	//add ring setting BIT 7, (NRS thing)
	}
	return * ring_head;					//return new ring_head (same as sent)
}


//from the string given alone, a new ring chain is created
ring * create_string (s08 * text)
{	ring * tmp = NULL;			//pointer to null
	return add_string ( &tmp, text);	//add string to that and return that pointer
}

//creates and add new ring to the head	(LIFO)
ring * add_ring_head (ring ** ring_head, s08 val)
{
	ring * temp;			//pointer to ring
	MALLOC (temp, ring);		//allocate memory, if fails returns here
	temp->value = val;		//set value
	temp->next = * ring_head;	//new next = head (old)
	temp->before = NULL;		//new before = null
	if (temp->next == NULL)		//first ring
	{
		temp->tail = temp;	//tail is new
		* ring_head = temp;	//head is new
		return temp;
	}
	(* ring_head)->before = temp;		//old before = new
	temp->tail = (* ring_head)->tail;	//new tail = old tail
	(* ring_head)->tail = NULL;		//old tail null
	* ring_head = temp;				//head = new
	return temp;
}
//creates and add new ring to the tail (FIFO)
ring * add_ring (ring ** ring_head, s08 val)
{
	ring * temp;				//pointer to null
	MALLOC (temp, ring);			//allocate mem, return here if fails
	temp->value = val;			//set value
	temp->next = NULL;			//new next = null
	if ( *ring_head == NULL )		//if first ring
	{
		temp->before = NULL;		//new before = null
		temp->tail = temp;		//tail = new
		* ring_head = temp;		//head = new
		return temp;
	}
	(* ring_head)->tail->next = temp;	//old tail next = new
	temp->before = (* ring_head)->tail;	//new before = old tail
	(* ring_head)->tail = temp;		//old tail = new
	return temp;
}
//ring_head tail points to ring_tail head (& all that implies)
ring * join_rings (ring ** ring_head, ring ** ring_tail)
{
	if ( *ring_head ==NULL)
	{
		*ring_head = *ring_tail;
		return *ring_head;
	}
	if(*ring_tail == NULL)
	{
		*ring_tail = *ring_head;
		return *ring_head;
	}
	(* ring_head)->tail->next = (*ring_tail);	//headring next point to new ring chain
	(* ring_tail)->before = (* ring_head)->tail;	//new ring chain before = old tail
	(* ring_head)->tail = (* ring_tail)->tail;	//old tail is now new tail	
	(* ring_tail)->tail = NULL;			//tailring not longer points to its tail
	* ring_tail = * ring_head;			//both head rings point to the same big ring chain
	return * ring_head;
}
#ifdef __MICRO_avr
//elements from the origin ring are copied to the tail of the destination ring
ring * pgm_copy_rings (ring ** ring_head_dest, PGM_P ring_head_orig)
{	
	t_int cnter = 0;
	while (pgm_read_byte(ring_head_orig+cnter) != '\0')
	{
		add_ring (ring_head_dest, pgm_read_byte(ring_head_orig + cnter++) | BIT(7) );	//add value
	}
	return * ring_head_dest;
}
#else
//elements from the origin ring are copied to the tail of the destination ring
ring * pgm_copy_rings (ring ** ring_head_dest, const s08 * ring_head_orig)
{	
	for (t_int cnter = 0; ring_head_orig[cnter] != '\0'; cnter++)
	{
		add_ring (ring_head_dest, ring_head_orig[cnter]  | BIT(7) );	//add value
	}
	return * ring_head_dest;
}
#endif
//elements from the origin ring are copied to the tail of the destination ring
ring * copy_rings (ring ** ring_head_dest, ring * ring_head_orig)
{
	while (ring_head_orig)						//while not end of chain
	{
		add_ring (ring_head_dest, ring_head_orig->value);	//add value
		ring_head_orig = ring_head_orig->next;			//point to the next value
	}
	return * ring_head_dest;
}
//elements from the origin ring are moved to the tail of the destination ring
ring * move_rings (ring ** ring_head_d, ring ** ring_head_o)
{
	join_rings (ring_head_d, ring_head_o);			//join ring chains
	*ring_head_o = NULL;									//chain origin is now empty
	return * ring_head_d;
}
//removes and returns ring (head), (first in)
s08 rm_ring (ring ** ring_head)
{
	if(*ring_head==NULL)	//if the head is already empty, return 
	{
		return -1;
	}
	s08 res = (* ring_head)->value;		//take the value from head
	ring * tmp = (*ring_head)->next;	//temporarily save pointer to next ring
	tmp->tail = (*ring_head)->tail;		//new head tail points to tail
	tmp->before = NULL;
	free ( (*ring_head) );			//free memory where the head is
	(*ring_head) = tmp;			//new head points to old next
	return res;
}
//removes the int of rings specified from the head
ring *  rm_rings (ring ** ring_head, t_int nrings)
{
	t_int cnter;
	for (cnter=0;cnter<nrings;cnter++)	
	{
		rm_ring (ring_head);				//remove rings the specified number of times
	}
	return * ring_head;
}
//removes and returns the tail ring, (last in)
s08 rm_ring_tail (ring ** ring_head)
{
	s08 res = (* ring_head)->tail->value;						//take value
	(* ring_head)->tail = (* ring_head)->tail->before;		//pointer to tail is now the ring before the tail
	if ( (*ring_head)->tail == NULL)								//if it was the only ring
	{
		free ( * ring_head );			//free only ring
		* ring_head = NULL;			//pointer is now to null
	}
	else
	{
		free ( (* ring_head)->tail->next);		//free the old tail
		(*ring_head)->tail->next = NULL;		//new tail next points to null
	}
	return res;
}
/*	
	this function tests if two rings start with the same information,
	WARNING pointer to the head is moved, so at the end it will point to the last ring checked
						intended to be used by start_with(2) & is_equal(2)
*/
s08 same_start (ring ** ring_1, ring ** ring_2)
{
	if ( (*ring_1 == NULL) | (*ring_2 == NULL))
	{
		return 0;
	}
	while ( (*ring_1) && (*ring_2) ) {
		if ((*ring_1)->value != (*ring_2)->value)
		{
			return 0;
		}
		*ring_1 = (*ring_1)->next;
		*ring_2 = (*ring_2)->next;
	}
	return 1;
}

#ifdef __MICRO_avr
//check if ring_2 starts with ring_1
s08 pgm_start_with (PGM_P ring_1, ring * ring_2)
{	
	if ( ( pgm_read_byte(ring_1) == '\0') | (ring_2 == NULL))
	{
		return 0;
	}
	t_int cnter = 0;
	while ( ( pgm_read_byte(ring_1+cnter) != '\0') && ring_2 ) {
		if ( WITH_BITSET(pgm_read_byte(ring_1+cnter),7) != ring_2->value)
		{
			return 0;
		}
		cnter++;
		ring_2 = ring_2->next;
	}
	return pgm_read_byte(ring_1+cnter) == '\0';
}
#else
//check if ring_2 starts with ring_1
s08 pgm_start_with (const s08 * ring_1, ring * ring_2)
{	
	if ( ( ring_1[0] == '\0') | (ring_2 == NULL))
	{
		return 0;
	}
	t_int cnter = 0;
	while ( ( ring_1[cnter] != '\0') && ring_2 ) {
		if ( WITH_BITSET(ring_1[cnter],7) != ring_2->value)
		{
			return 0;
		}
		cnter++;
		ring_2 = ring_2->next;
	}
	return ring_1[cnter] == '\0';
}
#endif

//check if ring_2 starts with ring_1
s08 start_with (ring * ring_1, ring * ring_2)
{
	//if both functions have the same start and ring_1 entirely used
	return same_start(&ring_1, &ring_2) && (ring_1 == NULL);	
}

//check if all elements of ring_1 and ring_2 match
s08 is_equal (ring * ring_1, ring * ring_2)
{
	//if both functions have the same start and both rings were entirely used
	return same_start(&ring_1, &ring_2) && (ring_1 == NULL) && (ring_2 == NULL);
}

//creates and add a new msg to the tail, the ring pointer points to NULL
msg * add_msg (msg ** msg_head)
{
	msg * temp;				//pointer to msg
	MALLOC (temp, msg);			//allocate mem
	temp->msg_ring = NULL;			//pointer to ring = null
	temp->next = NULL;			//new next = null
	if ( * msg_head == NULL )		//if first msg
	{
		temp->before = NULL;		//new before = null
		temp->tail = temp;		//tail points to itself
		* msg_head = temp;		//head points to new
		return temp;
	}
	(* msg_head)->tail->next = temp;	//old tail next = new
	temp->before = (* msg_head)->tail;	//new before = old tail
	(* msg_head)->tail = temp;		//old tail = new
	return temp;
}

//the byte is added according to add_ring(s08) to the tail msg ring pointer
msg * add_msg_value (msg ** msg_head, s08 val)
{
	if ( *msg_head == NULL)		//if no msg available create one
	{
		add_msg (msg_head);
	}
	add_ring ( &( (*msg_head)->tail->msg_ring ), val);		//add value according to add_ring to the pointer in the msg tail
	return * msg_head;
}

//removes the head message, return its ring pointer
ring * rm_msg (msg ** msg_head)
{
	if ( (*msg_head)==NULL)			//if no msg return 
	{
		return NULL;
	}
	ring * res = (*msg_head)->msg_ring;	//take the ring pointer of the msg about to be deleted
	msg * tmp = (*msg_head)->next;		//temporarily save the pointer to the next msg
	tmp->tail = (*msg_head)->tail;		//new head tail points to tail
	tmp->before = NULL;
	free ( (*msg_head) );			//free memory
	(*msg_head) = tmp;			//head now points to the previous next
	return res;
}

