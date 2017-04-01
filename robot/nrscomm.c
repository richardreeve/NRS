/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     8/2004
    Purpose:  This is used for nrs communication process
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#include "nrscomm.h"

// processes information hold in UARTIN(n)
void process_uart (msg ** msg_head)
{	
	if ( *msg_head == NULL ) return;				//if it hasn't been initialized, return
	if ( (*msg_head)->msg_ring == NULL ) return;
	if ( (*msg_head)->msg_ring->tail->value != BREAK ) return;	//if the msg is incomplete, return
	
	#ifdef DEBUG
	println ("<UART PROCESS>");
	println ("<reading msg>");
	#endif
	ini_commRegs();
	
	#ifdef __MICRO_avr	
	s08 spu = (*msg_head == UARTIN0)?0:1;		//port used is set depending with UART was sent
	SET_PIN(spu);
	#else
	SET_PIN(0);
	#endif
	
	ring * temp = rm_msg ( msg_head );		//remove the first message

	#ifdef BRIEFDEBUG
	println("<msg received");
	printRing (temp);
	println(">");
	#endif
	#ifdef DEBUG
	println("<msg received");
	printRing (temp);
	println(">");	
	#endif

	if ( GET_BIT(temp->value, 7) )				//if intelligent bit is set, process as intelligent, otherwise is normal msg
	{
		process_intelligent (&temp);
	}
	else
	{
		process_fast (&temp);
	}
	
	#ifdef BRIEFDEBUG	
	println("<msg to 0>");
	printRing((ring*)UARTOUT0);
	println(">");
	#ifdef __MICRO_avr
	println("<msg to 1 ");
      	printRing((ring*)UARTOUT1);
	println("<\n END OF UART PROCESSING>");
	#else
	printf("\n");
	#endif
	#endif
		
	#ifdef DEBUG
	println("<msg to 0>");
	printRing((ring*)UARTOUT0);
	#ifdef __MICRO_avr			//no uartout1 on khepera II
	println("");
	println("<msg to 1>");
       	printRing((ring*)UARTOUT1);
	#else
	printf("\n");
	#endif
	println("<END OF UART PROCESSING>");
	printl("\n");
	printl("\n");
	#endif
	
	del_all_ring(&temp);
	return;
}

void ini_commRegs()
{	
	del_all_ring(&uartout);
	MRNC = 0;				//Main register initialized
	SRNC = 0;				//Secondary register initialized
	PRNC = 0;				//Port register initialized
}

// process intelligent ringed list
void process_intelligent (ring ** ring_head)
{
	#ifdef DEBUG
	println ("<INT>");
	#endif
	SET_BIT(SRNC, RECEIVED_INT);
	s08 tmp_val;
	while ( (tmp_val = BYTE_UPTO(rm_ring(ring_head),6) ) != EOIM )		//take the first byte, continue until End Of Intelligent Message is Found
	{
		#ifdef DEBUG
		printByte (tmp_val);
		#endif
		switch (tmp_val)						//find type of intelligent message
		{
			case FORWARD_ROUTE:
				get_ringValue ( &forward_route, ring_head);	//extract forward route
				SET_BIT(MRNC, FROUTE);				//indicate forward route received
			break;
			case RETURN_ROUTE:
				get_ringValue ( &return_route, ring_head);	//extract return route
				SET_BIT(MRNC, RROUTE);				//indicate return route received
				add_route_first ( &return_route, GET_PIN );	//add at the beginning port used
				#ifdef DEBUG
				println("<retroute ");
				printRing(return_route);
				println(">");
				#endif
			break;
			case IS_BROADCAST:
				if (get_bool(ring_head)==1){
					SET_BIT (MRNC, BROADC);			//a broadcast was received
				}
				#ifdef DEBUG
				println ("<is broad>");
				#endif
			break;
			case HOP_COUNT:
				hop_count = get_int (ring_head);			//get hop_count
				SET_BIT (MRNC, HOPC);					//hop count was received
				if (hop_count == 0)										//if hop count is zero, indicate so, otherwise decrease it
				{	
					SET_BIT (MRNC, WASZERO);
				}
				else
				{
					hop_count--;
				}
			break;
			case TARGET_COMPONENT_CID:
				#ifdef DEBUG
				println ("<targ comp cid>");
				#endif
				get_ringValue ( &target_component_cid, ring_head);		//extract the component cid
				if (CompID)							//if I have, check if its for me
				{
					if ( is_equal ( target_component_cid, CompID ) )	//if its me, indicate that on the Main Register
					{
						SET_BIT (MRNC, ISMINE);
					}
				}
			break;
			case TARGET_VNNAME:
				SET_BIT (MRNC, TVVN);					//indicate target VNName given
				get_ringValue ( &target_vnname, ring_head);		//extract the VNName
				rm_ring ( &target_vnname );				//remove the first empty byte
				VNIDID = get_vnidid ( target_vnname );			//convert the VNName into a VNIDID from my vars
				#ifdef DEBUG
				printl ("<target vnname>");
				printText(target_vnname);
				println(">");
				#endif
			break;
			case SOURCE_COMPONENT_CID:
				SET_BIT(MRNC, SCID);					//indicate Source CID given
				get_ringValue ( &source_component_cid, ring_head);	//extract Source CID
			break;
			case MESSAGE_ID:
				imsgID = get_int( ring_head);				//extract msgID
			break;			
			case TRANSLATION_COUNT:
				SET_BIT(SRNC, TRANSC);
				translation_count = get_int (ring_head);
			break;
			case ACKNOWLEDGE_MESSAGE:
				//not implemented yet
				get_bool(ring_head);
			break;
			case ACKNOWLEDGE_TARGET:
				get_ringValue ( &acknowledge_target, ring_head);
			break;
			case FAILED_ROUTE_MESSAGE:
				//not implemented yet
				get_bool(ring_head);
			break;
			case FAILED_ROUTE_TARGET:
				get_ringValue ( &failed_route_target, ring_head);
			break;
			default:
				#ifdef DEBUG
				println ("<COMM NOT KNOWN>");
				#endif
				del_all_ring (ring_head);
				return;
			break;
		}
	}
	#ifdef DEBUG
	println ("<EINT>");
	#endif
	process_fast (ring_head);
}

// process non-intelligent ringed list
void process_fast (ring ** ring_head)
{
	#ifdef DEBUG
	println ("<FAST>");
	#endif
	if ( (ring_head == NULL) || ( (*ring_head)->next->value == BREAK) )
	{
		#ifdef DEBUG
		println ("<EMPTY>");
		#endif
		return;
	}
	ring * route = NULL;					//route
	get_ringValue (&route, ring_head);			//take the route from the msg
	del_all_ring(&uartout);					//delete uartout
	
	/*
					ROUTE CHECK START HERE
	*/
	
	#ifdef DEBUG
	printl("<route ");
	printRing(route);
	println(">");
	#endif
	
	if (GET_BIT(MRNC, BROADC) )		//if its broadcast ignore route
	{
		#ifdef DEBUG
		println ("<was broad cast>");
		#endif
		del_all_ring (&route);				//delete route
		// when a hop count is zero when received and so far the message is not mine, the message should be discard
		if ( GET_BIT(MRNC, HOPC)  &&  GET_BIT(MRNC, WASZERO)  && ~GET_BIT(MRNC, ISMINE) )
		{
			del_all_ring (ring_head);		//delete rest of the message
			return;					//no further action require
		}
		//SET_BIT(MRNC, ISMINE);			//its mine, its still a valid broadcast
	}
	else if ( (route->value == EMPTY ) && ( route->next == NULL ) )		//if the route is one segment & empty, its mine
	{
		#ifdef DEBUG
		println ("<empty route>");
		#endif
		rm_ring (&route);				//remove that ring empty ring
		SET_BIT(MRNC, ISMINE);				//its mine
	}	

	if ( GET_BIT(MRNC, ISMINE) == 0 )			// if the message is not for me at all, send it out through the specified port
	{
		if (GET_BIT (MRNC, BROADC))
		{
			#ifdef __ROBOT_kteam
			del_all_ring (&uartout);
			del_all_ring (&route);
			del_all_ring (ring_head);
			return;
			#else
			XOR_BIT(PRNC, 0);
			SET_POUT(PRNC);
			#endif
		}
		else
		{	
			SET_POUT(remove_my_route(&route));
		}
		#ifdef DEBUG
		printl("<this is the new route  ");
		printRing(route);
		println(">");
		#endif				
		move_rings (&uartout, &route);			//include new route
		move_rings (&uartout, ring_head);		//move everything as it is in the non-intelligent part
		#ifdef DEBUG
		printl("<new msg   ");
		printRing(uartout);
		println("  >");
		#endif 
		flush_uartout();
		return;																//just exit
	}

	/*
		UP TO HERE, the microcontroller knows the message is intended for it
		memory allocated to the route has been liberated
	*/

	t_int toVNID = get_int (ring_head);		//extract toVNID data
		
	if (GET_BIT (MRNC, TVVN) )			//if I received a TVVName, use that information instead
	{
		#ifdef DEBUG
		println ("<replacing vnid with tvvn>");
		#endif
		toVNID = VNIDID;			//VNIDID has the Vnid matching the vnname	
		CLEAR_BIT(MRNC, TVVN);			//clear flag cause not longer of use, and avoid confusion while creating new int	
		del_all_ring(&target_vnname);		//free space
	}
	
	/*
		Here the process of the VNID start
	*/
		
	if (toVNID < NUMHEADERNAME*TYPEOFQRCD)		//iff the vnid is targeting standard field, Query, Reply, Create, Delete
	{
		#ifdef DEBUG
		println ("<QRCD>");
		#endif
		t_int tmpval = toVNID % TYPEOFQRCD;	//this is type of field, e.g. CID, Route, Node, Language
		
		switch (toVNID/TYPEOFQRCD)		//check which standard field to process
		{
			case QUERY:
				retRoute_processing(ring_head);		//verify return route and include it
				process_query(tmpval, ring_head);	//process queries, build reply and return message so far
				flush_uartout();
			break;
			case REPLY:
				process_reply(tmpval, ring_head);	//process replies, build reply and return message so far
			break;
			case CREATE:
				process_create(tmpval, ring_head);			//process creation, build reply and return message so far
			break;
			case DELETE:
				process_delete(tmpval, ring_head);			//process deletion, build  reply and return message so far
			break;
		}
	}
	else
	{
		#ifdef DEBUG
		printl ("<set value vnid ");
		printByte(toVNID);
		println (">");
		#endif
		set_value_vnid (toVNID, ring_head );
	}
	#ifdef DEBUG
	println ("<EFAST>");
	#endif
	return;
}

ring * update_intelligent(s08 portOut)
{
	ring * intelligent = NULL;
	ring * tmp = NULL;
	if (((GET_BIT(MRNC, ISMINE)==0) && GET_BIT(SRNC, RECEIVED_INT)) || GET_BIT(SRNC, CREATE_INT) )
	{
		#ifdef DEBUG
		println("updating intelligent");
		#endif
		if (GET_BIT(MRNC, TVVN))
		{
			add_ring(&intelligent, TARGET_VNNAME);
			add_ring(&intelligent, STARTSTRING);
			copy_rings(&intelligent, target_vnname);
		}
		if (GET_BIT(MRNC,FROUTE))
		{
			add_ring( &intelligent, FORWARD_ROUTE );		//forward route label added
			copy_rings (&tmp, forward_route);
			add_route_last (&tmp, portOut);				//add my output port
			move_rings (&intelligent, &tmp );			//forward route moved to msg
		}
		if (GET_BIT(MRNC,RROUTE))
		{
			add_ring( &intelligent, RETURN_ROUTE );			//return route label added
			if (GET_BIT(PRNC, NOINPUT))
			{
				add_ring(&intelligent, EMPTY);			//empty iRetRoute
			}
			else
			{
				copy_rings(&tmp, return_route);
				/* ret route first update is done in process intelligent function
				   intelligent ret route is processed again for Queries only so far
				   if msg is not for this component then this duplicates first update
				*/
				if ( GET_BIT(MRNC, ISMINE) == 1 )		// if the message is for me at all, send it out through the specified port
				{
					add_route_first (&tmp, GET_PIN);	//port used for input
				}
				move_rings (&intelligent, &tmp );		//return route moved to msg
			}
		}
		if (GET_BIT(MRNC, BROADC))
		{
			add_ring( &intelligent, IS_BROADCAST );			//if its broadcast include that on the msg
			add_ring( &intelligent, NRS_TRUE);
		}
		if (GET_BIT(MRNC, HOPC))															
		{
			add_ring( &intelligent, HOP_COUNT );			//hop count intelligent label
			add_int (&intelligent, hop_count );			//include hop count
		}
		if (GET_BIT(SRNC, TARG_CID))					//if micro is targeting CID
		{
			add_ring(&intelligent, TARGET_COMPONENT_CID);
			copy_rings(&intelligent, target_component_cid);
		}
		
		if (intelligent)
		{
			add_ring (&intelligent, EOIM);								//include end of int msg
			SET_BIT (intelligent->value , 7);							//set the first bit with a one
		}
	}
	return intelligent;
}

/*
	based on the port given, it creates a intelligent message for it and add the non-intelligent built so far
 */
void create_outBuffers(s08 tmpport)
{
		#ifdef DEBUG
		println("creating buffer");
		#endif	
		ring * tmp = update_intelligent(tmpport);		//update intelligent msg according to port
		ring ** tmpuart;
		
		
		//only using one port here for khepera II
		#ifndef __ROBOT_kh2
		if (tmpport==1)
		{
			#ifdef DEBUG
			println("output using port 1");
			#endif			
			tmpuart = (ring**)(&UARTOUT1);
		}
		else
		{
			#ifdef DEBUG
			println("output using port 0");
			#endif					
			tmpuart = (ring**)(&UARTOUT0);
		}
		#else
		tmpuart = (ring**)(&UARTOUT0);
		#endif
		
		
		if (tmp)			//if null ignore intelligent
		{
			#ifdef DEBUG
			println("adding intelligent");
			#endif	
			move_rings(tmpuart, &tmp);
		}
		#ifdef DEBUG
		println("move to uartout");
		#endif			
		copy_rings(tmpuart, uartout);			//move the non-intelligent section
}

#ifndef __ROBOT_kh2
/*
	by calling this function it is assumed that all incoming msg has been procesed.
	This function just checks which port to use and creates intelligent msg for it.
 */
void flush_uartout()
{
	#ifdef DEBUG
	println("flushing uart");
	#endif
	if(GET_BIT(PRNC, MERGEPORTS))
	{
		#ifdef DEBUG
		println("flushing two ports");
		#endif
		s08 tmpport;
		for (tmpport=0;tmpport<2;tmpport++)
		{
			create_outBuffers (tmpport);
		}	
	}
	else						//if no merge of port, only one to be used
	{
		#ifdef DEBUG
		println("flushing one port");
		#endif
		create_outBuffers (GET_POUT);
	}
}
#endif

void retRoute_processing(ring ** ring_head)
{
	ring * tmpring = NULL;
	get_ringValue (&tmpring, ring_head);			//this is the return route
	#ifdef DEBUG
	printl("route found ");
	printRing(tmpring);
	//println("");
	printl("\n");
	#endif
	
	if (GET_BIT(MRNC, RROUTE))				//if a intelligent return route was given, use that one
	{
		#ifdef DEBUG
		println ("<int ret route given>");
		#endif	
		CLEAR_BIT(MRNC, RROUTE);
		del_all_ring(&tmpring);				//delete all route
		copy_rings (&tmpring, return_route);		//replace it with an intelligent
	}
	else if (GET_BIT(MRNC, SCID))				// if a CID was given & not return route was found, use that
	{
		#ifdef DEBUG
		println ("SCID given");
		#endif		
		SET_BIT(SRNC, CREATE_INT);			//create intelligent message
		SET_BIT(SRNC, TARG_CID);			//set TargetCID flag, use the source CID as target CID
		move_rings(&target_component_cid, &source_component_cid);
		CLEAR_BIT(MRNC, SCID);				//not include source cid in intelligent
		SET_BIT(MRNC, BROADC);				//is broad cast
		SET_BIT(MRNC, HOPC);				//with a hop count
		hop_count = INIHOPCOUNT;			//initialized hop count
		del_all_ring(&tmpring);				//the non intelligent return route is deleted
		add_ring(&tmpring, EMPTY);			//and set to empty
		SET_BIT(PRNC, MERGEPORTS);			//merge ports
	}
	else
	{
		#ifdef DEBUG
		printl("no int route given, this is ret route< ");
		printRing(tmpring);
		println (" >");
		#endif
	}
	
	#ifdef DEBUG
	println ("<finished processing route>");
	#endif
	
	s08 port = remove_my_route (&tmpring);	//if its not broadcast, check which port to use
	#ifdef DEBUG
	printl ("<port to use ");
	printByte (port);
	println(" >");
	#endif
	
	SET_POUT(port);					//update port to be use as output
	del_all_ring(&return_route);			//delete intelligent return route
	copy_rings(&return_route, tmpring);		//update iRetRoute
	move_rings (&uartout, &tmpring);		//the route is the first element to go in a message
	#ifdef DEBUG
	printl ("<finished processing ret route msg so far>");
	printRing(uartout);
	printl("\n");
	#endif				
}

//	initialized NRS comunication
void nrscomm_ini ()
{
	receiveEnable = 1;
	
	#ifdef BRIEFDEBUG
	println ("<INI_NRSCOMM>");
	#endif	
	#ifdef DEBUG
	println ("<INI_NRSCOMM>");
	#endif
}


