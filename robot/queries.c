/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     8/2004
    Purpose:  Process queries
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#include "queries.h"
#include "nrscomm.h"

const char pm_vnid[] PROGMEM = "VNID";
const char pm_vnname[] PROGMEM = "VNName";
const char pm_vntype[] PROGMEM = "VNType";
const char pm_maxvnid[] PROGMEM = "MaxVNID";
const char pm_numbertype[] PROGMEM = "NumberType";
const char pm_language[] PROGMEM = "Language";
const char pm_cid[] PROGMEM = "CID";
const char pm_maxport[] PROGMEM = "MaxPort";
const char pm_port[] PROGMEM = "Port";
const char pm_ctype[] PROGMEM = "CType";
const char pm_csl[] PROGMEM = "CSL";
const char pm_route[] PROGMEM = "Route";
const char pm_queryend[] PROGMEM = LISTBREAKSTR;

PGM_P pm_query_reply[NUMQUERYREPLY+1] PROGMEM = {pm_vnid, pm_vnname, pm_vntype, pm_maxvnid, pm_numbertype, pm_language, pm_cid, pm_maxport, pm_port, pm_ctype, pm_csl, pm_route, pm_queryend};

str_networkCIDs networkCIDs[NETWORKSIZE];
t_int total_msgID_count;

/*
	Processes queries
	@param		idtype	type of query
	@ring_head	the rest of the message, at this point route & toVNID has already been used
*/
void process_query (t_int vnidid, ring ** ring_head)
{
	t_int tmpnum = 0;
	ring * tmpring = NULL;
	#ifdef DEBUG
	println ("<processing query, moving rettoVNID & msgID>");
	#endif
	//toVNID and the msgID are just copied like that
	t_int cnter;
	for (cnter=0;cnter<2;cnter++)
	{
		get_ringValue (&tmpring, ring_head);
		move_rings (&uartout, &tmpring);	
	}
	switch (vnidid)
	{
		case VNID:
			get_ringValue (&tmpring, ring_head);					// get vnname
			rm_ring(&tmpring);
			t_int vnidfound = get_vnidid(tmpring);
			add_int (&uartout, vnidfound );		// add the number vnid to the msg
			del_all_ring (&tmpring);								// free mem for tmpvar	
		break;
		case VNNAME:
			#ifdef DEBUG
			printl ("<finding vnname with vnid");
			printRing(*ring_head);
			println (">");
			#endif				
			tmpring = get_vnname ( get_int (ring_head) );		//get vnname from vnid		
			#ifdef DEBUG
			printl ("<found");
			printText(tmpring);
			println (">");
			#endif
			move_rings (&uartout, &tmpring);						//move info to reply
		break;
		case VNTYPE:
			tmpnum = get_int (ring_head);
			if (tmpnum < NUMHEADERNAME*TYPEOFQRCD)
			{
				tmpring = get_vnname (tmpnum );
				move_rings(&uartout, &tmpring);
			}
			else
			{				
				add_ring(&uartout, STARTSTRING);			
				check_vnidType(tmpnum, &uartout);
			}
		break;
		case MAXVNID:
			add_int (&uartout, getMaxVNID());
		break;
		case NUMBERTYPE:
			add_ring(&uartout, WITH_BITSET(32, 6));
			add_ring(&uartout, NRS_FALSE);
		break;
		case LANGUAGE:
			add_ring(&uartout, NRS_TRUE);
			add_ring(&uartout, NRS_FALSE);
		break;
		case CID:
			#ifdef DEBUG
			println ("<CID request>");
			#endif		
			/*
			get_ringValue (&tmpring, ring_head);		//take the port		
			#ifdef DEBUG
			printl ("<port>");
			printRing(tmpring);
			printl("\n");
			#endif					
			move_rings (&uartout, &tmpring);		//move to the out going message
			*/
			get_ringValue (&tmpring, ring_head);		//take the suggested CID
			#ifdef DEBUG
			printl ("<suggested cid>");
			printRing(tmpring);
			printl("\n");
			#endif			
			if ( CompID == NULL )				//if I don't have, use it
			{
				#ifdef DEBUG
				println ("<didn't have cid>");
				#endif		
				CompID = tmpring;
			}
			copy_rings (&uartout, CompID);			//move to the msg my CID
		break;
		case MAXPORT:
			add_int (&uartout, MAXNUMPORTS);
		break;
		case PORT:
			tmpnum = get_int (ring_head);
			if ((tmpnum >0) && (tmpnum<=MAXNUMPORTS))
			{
				add_ring(&uartout, WITH_BITSET(tmpnum - 1, 2));	
			}
			else{
				add_ring(&uartout, EMPTY);
			}
		break;
		case CTYPE:
			#ifdef __MICRO_avr
			add_ring(&uartout, 0x01);
			ADD_string_P(&uartout, "Atmel");
			add_ring(&uartout,0x01);
			ADD_string_P(&uartout, "1.0");
			#endif
			#ifdef __ROBOT_kteam
			add_ring(&uartout, 0x01);
			ADD_string_P(&uartout, "Khepera2");
			add_ring(&uartout,0x01);
			ADD_string_P(&uartout, "1.0");
			#endif
			
		break;
		case CSL:
			add_ring(&uartout, EMPTY);
		break;
		case ROUTE:
			get_ringValue (&tmpring, ring_head);		//take the forward route
			if (GET_BIT(MRNC, FROUTE))					//if I have an iForRoute use it
			{
				del_all_ring(&tmpring);
				copy_rings(&tmpring, forward_route);
			}
			move_rings(&uartout, &tmpring);					//move the forward route
			copy_ringValue(&tmpring, uartout);				//copy route = retroute
			add_route_first(&tmpring, GET_PIN);
			move_rings(&uartout, &tmpring);
			t_int tmpnum = ( GET_BIT(SRNC, TRANSC) ) ? translation_count : 0 ;		//if translation count received, use it, otherwise, empty				
			add_int(&uartout, tmpnum);
			#ifdef DEBUG
			println ("<NOT iSOURCE HANDLING AT THE MOMENT>");
			printRing(uartout);
			#endif			
		break;
	}
	
	add_ring(&uartout, BREAK);													//end of message
	#ifdef DEBUG
	println ("<finished processing query msg so far>");
	printRing(uartout);
	//println ("");
	printl("\n");
	#endif					
}

/*
	Processes replies
	@param		idtype	type of reply
	@ring_head	the rest of the message, at this point route & toVNID has already been used
*/
void process_reply (t_int idtype, ring ** ring_head)
{
	#ifdef DEBUG
	printl ("Reply process");
	#endif

	t_int m_msgID = get_int( ring_head );
	ring * tmpring = NULL;
	ring * tmpring2 = NULL;
	
	switch (idtype)
	{
		case ROUTE:
			#ifdef DEBUG
			println (" ROUTE");
			printRing (*ring_head);
			println (">");
			#endif
			
			get_ringValue(&tmpring, ring_head);			//forward route
			get_ringValue(&tmpring2, ring_head);		//return route
			t_int routeCx = get_int(ring_head);	//translation Count

			#ifdef DEBUG
			println ("<info extracted>");
			#endif
			
			for(t_int counter = 0; counter<NETWORKSIZE;counter++)
			{
				if (networkCIDs[counter].status != NETWORKCIDS_NOTINITIALIZED)
				{
					if (networkCIDs[counter].QR_msgID == m_msgID)
					{
						
						if (networkCIDs[counter].status == NETWORKCIDS_RECEIVED)
						{
							if (networkCIDs[counter].routeComplexity > routeCx )
							{
								#ifdef DEBUG
								println ("<better route found>");
								#endif
								del_all_ring( &(networkCIDs[counter].route) );
							}
							else
							{
								#ifdef DEBUG
								println ("<had equal or better>");
								#endif
								break;
							}
						}
						#ifdef DEBUG
						println ("<route stored>");
						#endif
						copy_rings( &(networkCIDs[counter].route), tmpring );
						networkCIDs[counter].status = NETWORKCIDS_RECEIVED;
						networkCIDs[counter].routeComplexity = routeCx;
						break;
					}
				}
				else
				{
					break;
				}
			}
			#ifdef DEBUG
			println ("<->");
			#endif
			del_all_ring(&tmpring);
			del_all_ring(&tmpring2);
		break;
	}
}

void request_Route(ring * m_cid)
{
	ini_commRegs();
	
	SET_BIT(SRNC, CREATE_INT);			//Create intelligent
	SET_BIT(PRNC, MERGEPORTS);			//I am creating the broadcast
	
	SET_BIT(MRNC, BROADC);				//Broadcast message

	SET_BIT (MRNC, TVVN);				//target VNNAME
	del_all_ring(&target_vnname);
	PGM_copy_fromlist(&target_vnname, pm_header_vnnames, QUERY);	//"Query"
	PGM_copy_fromlist(&target_vnname, pm_query_reply, ROUTE);		//"Route"
	
	SET_BIT (MRNC,FROUTE);				//iForward Route
	del_all_ring(&forward_route);
	add_ring(&forward_route, EMPTY);

	SET_BIT (MRNC,RROUTE);
	SET_BIT(PRNC, NOINPUT);				//msg came from none input
	
	SET_BIT(MRNC, HOPC);				//Hop Counter
	hop_count = INIHOPCOUNT;

	SET_BIT(SRNC, TARG_CID);					//Target CID
	del_all_ring( &target_component_cid);
	copy_rings( &target_component_cid, m_cid);			//cid given

	SET_BIT(MRNC, SCID);						//Source CID
	del_all_ring( &source_component_cid);
	copy_rings( &source_component_cid, CompID);		//this is the source

	add_ring( &uartout, EMPTY);						//route		broadcast
	add_ring( &uartout, NRSZERO);					//to vnid	iTVNNAME
	add_ring( &uartout, EMPTY);						//ret route	iRetRoute
	add_int( &uartout, REPLY*TYPEOFQRCD+ROUTE);		//ret to replyRoute vnid
	add_int( &uartout, total_msgID_count);			//msg id
	add_ring( &uartout, EMPTY);						//forward route will be ignore by iFR
	add_ring( &uartout, BREAK);						//end message
	
	flush_uartout();
}

void getRouteToCID(ring ** answer, ring * tryCID)
{
	for(t_int counter = 0; counter<NETWORKSIZE;counter++)
	{
		if ( networkCIDs[counter].status == NETWORKCIDS_NOTINITIALIZED)
		{
			#ifdef DEBUG
			println ("<ini connection>");
			#endif
			networkCIDs[counter].status = NETWORKCIDS_UNKNOWN;
			networkCIDs[counter].route = NULL;
			copy_rings( &(networkCIDs[counter].componentCID), tryCID);
		}
		if (is_equal(networkCIDs[counter].componentCID, tryCID))
		{
			switch(networkCIDs[counter].status)
			{
				case NETWORKCIDS_REQUESTED:
					return;
				break;				
				case NETWORKCIDS_UNKNOWN:			
					#ifdef DEBUG
					println ("<requesting Route>");
					#endif
					request_Route(tryCID);
					networkCIDs[counter].status = NETWORKCIDS_REQUESTED;
					networkCIDs[counter].QR_msgID = total_msgID_count++;
					return;
				break;
				case NETWORKCIDS_RECEIVED:
					copy_rings(answer, networkCIDs[counter].route );
					return;
				break;
			}
		}
	}
}

void queries_ini()
{
	total_msgID_count = 1;
	for(t_int counter = 0; counter<NETWORKSIZE;counter++)
	{
		networkCIDs[counter].status = NETWORKCIDS_NOTINITIALIZED;
	}
}
