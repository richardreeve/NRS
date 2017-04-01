/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     8/2004
    Purpose:  These tools are used for basic nrs communication
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#include "nrstools.h"

//header names: "query" "reply" "create" "delete"
const char pm_hvnnames_query[] PROGMEM = "Query";
const char pm_hvnnames_reply[] PROGMEM = "Reply";
const char pm_hvnnames_create[] PROGMEM = "Create";
const char pm_hvnnames_delete[] PROGMEM = "Delete";
const char pm_hvnnames_end[] PROGMEM = LISTBREAKSTR;
PGM_P pm_header_vnnames[NUMHEADERNAME+1] PROGMEM = {pm_hvnnames_query, pm_hvnnames_reply, pm_hvnnames_create, pm_hvnnames_delete, pm_hvnnames_end};

s08 MRNC;				//Main Register for NRS Communication
s08 SRNC;				//Secondary Register for NRS Communication
s08 PRNC;				//Ports Register for NRS Comm
ring * uartout;				//tmp variable where the msg is built
ring * CompID;				//this is my CID

ring * forward_route;			//forward route
ring * return_route;			//return route
t_int hop_count;			//hop counter
ring * target_component_cid;		//target comp CID
ring * target_vnname;			//vnname
ring * source_component_cid;		//source comp CID
t_int translation_count;		//translation count
ring * acknowledge_target;		//ack target
ring * failed_route_target;		//failed route target
t_int imsgID;
t_int VNIDID;				//VNIDID found during int msg

/*
	get a  value from a ringed list
*/
t_int get_int ( ring ** val )
{
	signed char charRes = BYTE_UPTO(rm_ring (val), 5);		//the first bit is always zero, the second is one, third is sign
	if (GET_BIT(charRes, 5))				//negative int
	{
		charRes |= 0xc0;
	}
	t_int res = charRes;
	while (	 (*val) && GET_BIT( (*val)->value, 7)	)	//if the ring still start with one, take value
	{
		res <<= 7;
		res |= BYTE_UPTO(rm_ring(val),6) ;		//include the next 7 bits (upto bit 6)
	}
	return res;
}

t_int get_bool ( ring ** val)
{
	return BYTE_UPTO(rm_ring (val), 0);		//
}

/* 
	a segment is removed from the value and added to the variable
	it takes the variable as an argument to avoid memory leak
*/
ring * get_ringValue (ring ** variable, ring ** val)
{
	del_all_ring ( variable );				//make sure the variable is empty
	if ( *val == NULL)					//is no val, return
	{
		return * variable;
	}	
	do
	{
		add_ring ( variable, rm_ring (val) );		//take value, and add it to the variable ring
	}  while ( GET_BIT ( (*val)->value, 7 ) );		//repeat while the next value starts with one
	return * variable;
}

ring * copy_ringValue (ring ** variable, ring * val)
{
	del_all_ring ( variable );				//make sure the variable is empty
	if ( val == NULL)					//is no val, return
	{
		return * variable;
	}	
	do
	{
		add_ring ( variable, val->value );		//take value, and add it to the variable ring
		val = val->next;
	}  while ( GET_BIT ( val->value, 7 ) );			//repeat while the next value starts with one
	return * variable;
}
/*
	The int value is added to the ring chain
*/
ring * add_int ( ring ** ring_head, t_int val )
{
	ring * tmp_ring = NULL;		//the lower bits are taken first, a temporal ring is necessary
	s08 tmp_val;				//the first byte can hold only 6 bits data, so its taking up to bit 5 the first time
	for (;;)
	{
		tmp_val = BYTE_UPTO(val, 6);	//take up to bit 6
		val >>= 7;						//move the rest
		if ((val == 0) || (val == -1))					//if int if empty, add first one and finish
		{
			if ((GET_BIT(tmp_val, 6) == (val+1)<<6 )||(GET_BIT(tmp_val, 5) == (val+1)<<5 ))		//if bit 6 is used, add new byte
			{
				s08 headMask = (val==0) ? BIT(6) : ~BIT(7);
				add_ring_head(&tmp_ring, WITH_BITSET (tmp_val, 7)  );		//add tmp_val, set one
				add_ring_head(&tmp_ring, headMask );						//start of segment
				break;
			}
			add_ring_head ( &tmp_ring, WITH_BITSET (tmp_val, 6)  );			//set bit 6 and add
			break;
		}
		add_ring_head ( &tmp_ring, WITH_BITSET (tmp_val, 7) );				//otherwise, add tmp_val and continue
	}
	return join_rings (ring_head, &tmp_ring);		//the result is the ringhead join to the tmp_result
}

//NOTE if route so far is 0000 001X, ti crashes if no further bytes sent
s08 remove_my_route (ring ** ring_head)
{
	s08 pos = position_one( (*ring_head)->value );
	s08 route;
	if (pos>=2)
	{
		route = (*ring_head)->value >> (pos-2);
		if (pos==2)
		{
			if(  (*ring_head)->next == NULL) 
			{
				(*ring_head)->value = EMPTY;			
			}
			else
			{
				rm_ring (ring_head);
			}
		}
		else
		{
			(*ring_head)->value = WITH_BITSET( BYTE_UPTO((*ring_head)->value, pos-3), pos-2);
		}
		return BYTE_UPTO(route, 1);
	}
	route = (*ring_head)->value << (2-pos);
	rm_ring (ring_head);
	route |= BYTE_UPTO((*ring_head)->value,6) >> (pos+5) ;
	(*ring_head)->value = WITH_BITSET( BYTE_UPTO((*ring_head)->value,pos+4), pos+5);
	return BYTE_UPTO(route, 1);
}

void add_route_first (ring ** ring_head, s08 port)
{
	SET_BIT(port, 2);
	s08 pos = position_one( (*ring_head)->value );
	(*ring_head)->value = (port<<pos) | BYTE_UPTO((*ring_head)->value, pos-1);
	if (pos<6)
	{
		return;
	}
	SET_BIT ( (*ring_head)->value, 7);
	add_ring_head (ring_head, port>>(7-pos));
}

void add_route_last (ring ** ring_head, s08 port)
{
	s08 pos = position_one( (*ring_head)->value );
	ring * it;
	if (pos>5)
	{
		add_ring_head (ring_head, (*ring_head)->value >> 5 );
		it = (*ring_head)->next;
		it->value = WITH_BITSET( it->value<<2, 7);
	}
	else
	{
		it = * ring_head;
		it->value <<=2;
	}
	while ( (it->next) && GET_BIT( it->next->value, 7)  )
	{
		it->value |= BYTE_UPTO(it->next->value>>5, 1);
		it = it->next;
		it->value = WITH_BITSET( it->value<<2, 7);
	}
	it->value |= BYTE_UPTO(port, 1);
}

#ifdef __MICRO_avr
t_int pgm_find_string ( PGM_P list[], ring * ring_string, s08 from)
{	
	t_int cnter2;
	for (cnter2=0;cnter2<from;cnter2++)
	{
		ring_string =ring_string->next;
	}
    PGM_P p;
    memcpy_P(&p, &list[0], sizeof(PGM_P));
	for (cnter2=0; pgm_read_byte(p) != ',' ;cnter2++)
	{   
	    memcpy_P(&p, &list[cnter2], sizeof(PGM_P));
		if (pgm_start_with( p, ring_string) )
		{
			return cnter2;
		}
	}
	return -1;
} 
#else
t_int pgm_find_string ( const s08 * list[], ring * ring_string, s08 from)
{	
	t_int cnter2;
	for (cnter2=0;cnter2<from;cnter2++)
	{
		ring_string =ring_string->next;
	}
	for (cnter2=0; list[cnter2][0] != ',' ;cnter2++)
	{   
		if (pgm_start_with( &(list[cnter2][0]), ring_string) )
		{
			return cnter2;
		}
	}
	return -1;
} 
#endif

t_int find_string ( ring * list[], ring * ring_string, s08 from)
{
	t_int cnter2;
	for (cnter2=0;cnter2<from;cnter2++)
	{
		ring_string =ring_string->next;
	}
	for (cnter2=0; list[cnter2]->value != LISTBREAKVAL ;cnter2++)
	{	
		if (start_with(list[cnter2], ring_string))
		{
			return cnter2;
		}
	}
	return -1;
} 

t_int find_allString ( ring * list[], ring * ring_string)
{
	for (t_int cnter2=0; list[cnter2]->value != LISTBREAKVAL ;cnter2++)
	{	
		if (is_equal(list[cnter2], ring_string))
		{
			return cnter2;
		}
	}
	return -1;
} 

t_int contain_string ( ring * list[], ring * ring_string)
{
	t_int cnter = 0, cnter2 = 0;
	ring * tmpperiod = NULL;
	cnter2 = find_lastPeriod (ring_string);

	for (cnter=0; cnter<cnter2; cnter++)
	{
		add_ring( &tmpperiod, ring_string->value);
		ring_string = ring_string->next;
	}
	for (cnter2=0; list[cnter2]->value != LISTBREAKVAL ;cnter2++)
	{
		if (is_equal(list[cnter2], tmpperiod))
		{
			del_all_ring(&tmpperiod);
			return cnter2;
		}			
	}
	del_all_ring(&tmpperiod);
	return -1;
}

t_int find_lastPeriod (ring * ring_string)
{
	t_int cnter = 0, cnter2 = 0;
	ring * tmpperiod = ring_string;
	while (tmpperiod)
	{
		if ( tmpperiod->value == NAMEBREAKVAL )
		{
			cnter2 = cnter;
		}
		tmpperiod = tmpperiod->next;
		cnter++;
	}
	return (cnter2==0)? cnter : cnter2;
}

/* from the VNName, find the VNID
	@param	ring_vnname	VNName
*/
t_int get_vnidid ( ring * vnname )
{
	t_int tmp = pgm_find_string ( (PGM_P * ) pm_header_vnnames, vnname , 0), tmpval = 0;
	if (tmp==-1)
	{
		#ifdef DEBUG
		println("<my_var>");
		#endif
		//printf("dog dirt");
		//printf("%d", scan_node_vnnames(vnname));
		return scan_node_vnnames(vnname);
	}
	else
	{
		#ifdef DEBUG
		println("<standard>");
		#endif
		if ((tmp==QUERY)||(tmp==REPLY))
		{
			tmpval = pgm_find_string ( (PGM_P *) pm_query_reply, vnname , 5);//ignore "query or reply" on vnname
		}
		else if ((tmp==CREATE)||(tmp==DELETE))
		{
			tmpval = pgm_find_string ( (PGM_P *) pm_create_delete, vnname, 6);//ignore "create" or "delete" on vnname
		}
		return TYPEOFQRCD * tmp + tmpval;
	}
}

/* from the VNID, get the VNName
	@param	vnidid
*/
ring * get_vnname (t_int vnidid)
{
	t_int tmp = 0, tmpval;
	ring * res = NULL;
	add_ring(&res, STARTSTRING);
	if (vnidid < NUMHEADERNAME*TYPEOFQRCD)
	{
		PGM_copy_fromlist( &res, pm_header_vnnames, tmp);
		tmpval = vnidid - tmp*TYPEOFQRCD;
		if ((tmp==QUERY)||(tmp==REPLY))
		{
			PGM_copy_fromlist( &res, pm_query_reply, tmpval);
		}
		else
		{
			#ifdef DEBUG
			println("<cd>");
			#endif
			PGM_copy_fromlist ( &res, pm_create_delete, tmpval );
		}
		#ifdef DEBUG
		printl("<vnname found");
		printText(res);
		println(">");
		#endif			
		return res;
	}
	
	tmp = scan_node_vnids(vnidid, &res);
	
	#ifdef DEBUG
	printl("<vnname found");
	printText(res);
	println(">");
	#endif	
	return res;
}

s08 position_one (s08 value)
{
	s08 n = 0;
	if (value==0)
	{
		return n;
	}
	while ( GET_BIT(value ,7) ==0 )
	{
		value <<=1;
		n++;
	}	
	return 7-n;
}

void nrstools_ini()
{	
	forward_route = NULL;
	return_route = NULL;
	hop_count = 0;
	target_component_cid = NULL;
	target_vnname = NULL;
	source_component_cid = NULL;
	translation_count = 0;
	acknowledge_target = NULL;
	failed_route_target = NULL;

	uartout = NULL;
	MRNC = 0;
	SRNC = 0;
	VNIDID = 0;
	CompID = NULL;
}

