/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     5/2005
    Purpose:  Handles ATMEL link functions
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#include "link.h"
#include "nrscomm.h"

link * link_head;
t_int numLinks;

link * create_link(ring ** ring_head)
{
	t_int tempSource = get_bool(ring_head);
	ring * tempCIDSource = NULL;
	get_ringValue( &tempCIDSource, ring_head);
	t_int tempVNIDSource = get_int(ring_head);
	ring * tempCIDTarget = NULL;
	get_ringValue( &tempCIDTarget, ring_head);
	t_int tempVNIDTarget = get_int(ring_head);
	t_int temporary = get_bool(ring_head);
	
	#ifdef DEBUG
	println("<info extracted>");
	#endif
	
	if (temporary)
	{
		#ifdef DEBUG
		println("<broadcast>");
		#endif
		//	ini_commRegs done before to set TVVname
		if (tempVNIDSource >= HARDMEMORY)
		{
			ini_commRegs();
			SET_BIT(SRNC, CREATE_INT);				// Create intelligent
			SET_BIT(PRNC, MERGEPORTS);				// I am creating the broadcast
			SET_BIT(MRNC, BROADC);					// Broadcast message
			SET_BIT(MRNC, HOPC);					// Hop Counter
			hop_count = INIHOPCOUNT;
			del_all_ring( &target_component_cid);
			copy_rings( &target_component_cid, tempCIDTarget);		// cid given
			SET_BIT(SRNC, TARG_CID);								// TargetCID

			add_ring(&uartout, EMPTY);				// empty route
			add_int( &uartout, tempVNIDTarget);	//to vnid
		
			if (get_value_vnid (tempVNIDSource, &uartout))
			{
				add_ring(&uartout, BREAK);
				flush_uartout();
			}
		}
		del_all_ring( &tempCIDSource );
		del_all_ring( &tempCIDTarget );
		return NULL;
	}
	
	link * temp;			//pointer to null
	MALLOC (temp, link);	//allocate mem, return here if fails
	
	temp->source = tempSource;	//set values
	temp->route = NULL;
	temp->requestedRoute = NO_ROUTE_REQUESTED;
	temp->sourceVNID = tempVNIDSource;
	temp->targetVNID = tempVNIDTarget;
	temp->prevValue = NULL;
	
	if (tempSource)
	{
		del_all_ring( &tempCIDSource );
		temp->othersCID = tempCIDTarget;
	}
	else
	{
		del_all_ring( &tempCIDTarget );
		temp->othersCID = tempCIDSource;
	}

	temp->next = link_head;	//new next = null
	link_head = temp;
	numLinks++;
	
	return temp;

}

void delete_link(ring ** ring_head)
{
	t_int tempSource = get_bool(ring_head);
	
	ring * othersCID = NULL;
	ring * myTempCID = NULL;
	t_int tempVNIDsource;
	t_int tempVNIDtarget;
	if (tempSource)
	{
		get_ringValue( &myTempCID, ring_head);
	}
	else
	{
		get_ringValue( &othersCID, ring_head);
	}
	tempVNIDsource = get_int(ring_head);
	if (tempSource)
	{
		get_ringValue( &othersCID, ring_head);
	}
	else
	{
		get_ringValue( &myTempCID, ring_head);
	}
	tempVNIDtarget = get_int(ring_head);
	
	link * iterator = link_head;
	while(iterator)
	{
		if ( (iterator->sourceVNID == tempVNIDsource) && 
			(iterator->targetVNID == tempVNIDtarget) &&
			(is_equal(iterator->othersCID, othersCID)) )
		{
			iterator->requestedRoute = DESTROY_LINK;
		}
		iterator = iterator->next;
	}
	
	del_all_ring (&othersCID);
	del_all_ring (&myTempCID);
	
}


void deleteAllLinks()
{
	link * iterator = link_head;
	while(iterator)
	{
		iterator->requestedRoute = DESTROY_LINK;
		iterator = iterator->next;
	}
}

void link_connect_internally(t_int vnid_source, t_int vnid_target)
{
	t_int vnidtype = (vnid_source-HARDMEMORY)/VARIABLESPERTYPE;
	vnid_source -= vnidtype*VARIABLESPERTYPE+HARDMEMORY;
	vnid_target -= vnidtype*VARIABLESPERTYPE+HARDMEMORY;	
	switch(vnidtype)
	{
		case BOOL_VAR:
			boolVars[vnid_target] = boolVars[vnid_source];
			return;
		case INT_VAR:	
			intVars[vnid_target] = intVars[vnid_source];
			return;
		case RING_VAR:
			ringVars[vnid_target] = ringVars[vnid_source];
			return;
		case VOID_VAR:
			voidEvent[vnid_target] = voidEvent[vnid_source];
	}
}

void link_update()
{
	link * iterator = link_head;
	link * previous = NULL;
	link * tmp_next;
	
	ring * currentValue = NULL;
	while(iterator)
	{
		if(iterator->source)
		{
			switch (iterator->requestedRoute)
			{
				case DESTROY_LINK:
					#ifdef DEBUG
					println("<Destroying link>");
					#endif
					tmp_next = iterator->next;
					if (previous)
					{
						previous->next = tmp_next;
					}
					del_all_ring( &(iterator->othersCID) );
					del_all_ring( &(iterator->route) );
					del_all_ring( &(iterator->prevValue) );
					free ( iterator );
					if (previous == NULL)
					{
						link_head = tmp_next;
					}
					iterator = tmp_next;
					numLinks--;
					break;
				case INTERNAL_LINK:
					/*#ifdef DEBUG
					printl("<linking:");
					printByte(iterator->sourceVNID);
					printl(" to ");
					printByte(iterator->targetVNID);
					println(">");
					#endif*/
					link_connect_internally (iterator->sourceVNID, iterator->targetVNID );
					break;
				case WAITING_FOR_ROUTE:
					getRouteToCID( &(iterator->route), iterator->othersCID);
					if (iterator->route)
					{
						#ifdef DEBUG
						println("<Route found for link>");
						#endif
						iterator->requestedRoute = ROUTE_RECEIVED;
					}
					break;
				case NO_ROUTE_REQUESTED:
					if ( is_equal(iterator->othersCID, CompID) ){
						iterator->requestedRoute = INTERNAL_LINK;
						#ifdef DEBUG
						println("<Internal link created>");
						#endif
					}
					else
					{
						getRouteToCID(&(iterator->route), iterator->othersCID);
						if (iterator->route)
						{
							iterator->requestedRoute = ROUTE_RECEIVED;
						}
						else
						{
							iterator->requestedRoute = WAITING_FOR_ROUTE;
						}
						#ifdef DEBUG
						println("<Waiting for route>");
						#endif
					}
					break;
				case ROUTE_RECEIVED:
					/* 
					 * this has a bug when requesting a void event..
					 * when flush_uartout() is not used the micro halts forever, therefore it only happens with void when the value is zero
					 * if a prinln is put on memory.c before copying command it works (??)
					 * the only known relation between flush_uart() and uartout is copy_rings(tmpuart, uartout); it only reads uartout (??)
					 */
					get_value_vnid( iterator->sourceVNID, &currentValue );
					if (!is_equal(currentValue, iterator->prevValue))
					{
						if (iterator->sourceVNID >= HARDMEMORY)
						{
							ini_commRegs();					//ini comm registers
							copy_rings(&uartout, iterator->route);		//copy ret route
							s08 port = remove_my_route (&uartout);		//if its not broadcast, check which port to use
							SET_POUT(port);					//update port to be use as output
							add_int( &uartout, iterator->targetVNID);	//to vnid

							del_all_ring( &(iterator->prevValue) );
							move_rings( &(iterator->prevValue), &currentValue );	

							if (get_value_vnid (iterator->sourceVNID, &uartout))
							{
								add_ring(&uartout, BREAK);
								flush_uartout();
							}
						}
					}
					del_all_ring(&currentValue);
					break;
			}
		}
		previous = iterator;
		if (iterator)
		{
			iterator = iterator->next;
		}
	}
}

void link_ini()
{
	link_head = NULL;
	numLinks = 0;
}

