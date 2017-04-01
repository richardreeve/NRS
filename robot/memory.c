/*
    Title:    NRS program for the ATMEL MEGA128
    Author:   Hugo Rosano
    Date:     11/2004
    Purpose:  Handles variable allocation
    Software: AVR-GCC to compile
    Hardware: AT MEGA 128
    Note:     contact me at: H.Rosano@ed.ac.uk
*/

#include "memory.h"

s08 boolVars[MAX_MYBOOLVARS+1];		//pointers to my bool vars
t_int intVars[MAX_MYINTVARS+1];		//pointers to my int vars
ring * ringVars[MAX_MYRINGVARS+1];	//pointers to my ring vars
s08 voidEvent[MAX_MYVOIDVARS+1];	//pointers to my void vars


//MEMORYTYPES = 4
//length of t_int is 16 bits (therefore a max of 16 variables of a given type)
//need to have 60 ints for a fully functional khepera II
//therefore for ints, we need 60/16 = 3.75 => 4 t_ints to encode this (3 t_ints + 12 bits)
//
//Set up the array memory fuses so that it is an array of variable length arrays
//e.g. for the kh2, you need memoryfuses[] = {bools[1],ints[4],rings[1],voids[2]}

//each row has fuses indicating which array element has been used
t_int memoryfuses[MEMORYTYPES][VARIABLESPERTYPE/T_INTSIZE+1];// = {boolfuses,intfuses,ringfuses,voidfuses};		

//which_type can be 0,1,2 or 3 -- refers to which array of fuse variables
//var can be 0 to 15 -- tells you which variable (which bit) you are looking at
void freeVar(t_int which_type, t_int var)
{	
	//t_int fuses = memoryfuses[which_type][var/16];	//array of fuse vairables for a given type
	
	//if var is less than 16 then var / 16 = 0
	//if var is more than 16 but less than 32 then var / 16 = 1
	//etc.
	
	freeVarNode(&memoryfuses[which_type][var/T_INTSIZE], var % T_INTSIZE);		
								
	//freeVarNode( &(memoryfuses[which_type]), var);
}

//
void freeVarNode(t_int * fuses, t_int var)
{
	t_int mask = 1;
	mask <<= var;
	(*fuses) &= ~mask;
}

s08 getVoidEvent(t_int voidvnid)
{
	s08 tmpvoid = voidEvent[voidvnid];
	voidEvent[voidvnid] = 0;
	return tmpvoid;
}

t_int getNextVar(t_int which_type)
{
	t_int ind = -1;
	t_int row = 0;
	while(ind == -1)
	{
		//ind = getNextNode( &(memoryfuses[which_type][row++]), T_INTSIZE );
		ind = getNextNode( &(memoryfuses[which_type][row++]), T_INTSIZE );
		
		#ifdef DEBUG
		printl("var type: " );
		printt_int(which_type);
		printl("row ");
		printt_int(row);
		printl("ind: ");
		printt_int(ind);
		printl(" fuses: ");
		printt_int(memoryfuses[which_type][row]);
		printl("\n");
		#endif
		
		if (row > VARIABLESPERTYPE/T_INTSIZE+1)
		{
			#ifdef DEBUG
			println("< memory: max rows reached >");
			#endif

			return -1;
		}
	}
	
	
	return ind + (row-1)*T_INTSIZE;
}

t_int getNextNode(t_int * fuses, t_int maxnum)
{
	t_int mask = 1;
	for(t_int ind = 0; ind<maxnum; ind++)
	{
		if( !((*fuses) & mask) )
		{
			(*fuses) |= mask;
			
			/*
			#ifdef DEBUG
			printt_int(*fuses);
			printf("\n");
			#endif
			*/
			
			return ind;
		}
		mask <<= 1;
	}
	return -1;
}

s08 get_value_vnid (t_int vnid, ring ** ring_head)
{
	t_int vnidtype = (vnid-HARDMEMORY)/VARIABLESPERTYPE;
	vnid -= vnidtype*VARIABLESPERTYPE+HARDMEMORY;
	switch(vnidtype)
	{
		case BOOL_VAR:
			#ifdef DEBUG
			println("<sending bool>");
			#endif
			add_ring ( ring_head, boolVars[vnid]);
			return 1;
		case INT_VAR:	
			#ifdef DEBUG
			printl("<sending int ");
			#ifndef __MICRO_avr
			printf("%d", intVars[vnid]);
			#endif
			println(">");
			#endif
			add_int ( ring_head, intVars[vnid]);
			return 1;
		case RING_VAR:	
			#ifdef DEBUG
			println("<sending ring>");
			#endif
			copy_rings ( ring_head, ringVars[vnid]);
			return 1;
		case VOID_VAR:
			return voidEvent[vnid];
	}
	return 0;
}


s08 isEqualVNID (t_int vnid1, t_int vnid2) //ring prevValue
{
	t_int vnidtype = (vnid1-HARDMEMORY)/VARIABLESPERTYPE;
	vnid1 -= vnidtype*VARIABLESPERTYPE+HARDMEMORY;
	switch(vnidtype)
	{
		case BOOL_VAR:
			if(boolVars[vnid2] == boolVars[vnid1])
			{
				return 1;
			}
			return 0;
		case INT_VAR:
			if (intVars[vnid2] == intVars[vnid1])
			{
				return 1;
			}
			return 0;
		case RING_VAR:
			return is_equal( ringVars[vnid2], ringVars[vnid1]);
		case VOID_VAR:
			if (voidEvent[vnid2] == voidEvent[vnid1])
			{
				return 1;
			}
			return 0;
	}
	return 0;
}


/*	set the int value to that vnid
	@param	value		int value
	@param	vnid		vnid target
*/
void set_value_vnid (t_int vnid, ring ** value)
{
	#ifdef DEBUG
	printl("<setting value to vnid ");
	printByte(vnid);
	println(">");	
	#endif
	if (vnid < HARDMEMORY)
	{
		#ifdef DEBUG
		println("<setting value to NODE>");
		#endif				
		set_node_values (value, vnid);
		return;
	}
	t_int vnidtype = (vnid-HARDMEMORY)/VARIABLESPERTYPE;
	vnid -= vnidtype*VARIABLESPERTYPE+HARDMEMORY;
	if (vnid == 0)		//reserved for default values
	{
		return;
	}
	#ifdef DEBUG
	println("<setting value to LOCAL>");
	#endif		
	switch(vnidtype)
	{
		case BOOL_VAR:
			#ifdef DEBUG
			println("<setting value to s08>");
			#endif
			boolVars[vnid] = BYTE_UPTO(rm_ring(value), 0);	//bool value
			#ifdef DEBUG
			printl("<value set: ");
			if(boolVars[vnid])
			{
				printl("true");	
			}else{
				printl("false");
			}
			printl(">\n");
			#endif
			return;
		case INT_VAR:
			#ifdef DEBUG
			println("<setting value to num>");
			#endif		
			intVars[vnid] = get_int(value);		//int
			#ifdef DEBUG
			printl("<value set: ");
			printt_int(intVars[vnid]);
			printl(">\n");
			#endif	
			return;
		case RING_VAR:
			#ifdef DEBUG
			println("<setting value to token>");
			#endif		
			move_rings( &(ringVars[vnid]), value);			//token
			return;
		case VOID_VAR:
			#ifdef DEBUG
			println("<setting void event>");
			#endif
			voidEvent[vnid] = NRS_TRUE;
	}
}

void check_vnidType(t_int vnid, ring ** answer)
{
	if (vnid < HARDMEMORY)
	{	
		check_vnidNodeType (vnid, answer);
		return;
	}
	t_int vnidtype = (vnid-HARDMEMORY)/VARIABLESPERTYPE;
	switch(vnidtype)
	{
		case BOOL_VAR:
			ADD_string_P (answer, "bool");
			return;
		case INT_VAR:	
			ADD_string_P (answer, "integer");
			return;
		case RING_VAR:	
			ADD_string_P (answer, "token");
			return;
		case VOID_VAR:
			ADD_string_P (answer, "void");
	}	
}

t_int getMaxVNID(){
	return TYPEOFQRCD + NUMNODETYPES*INSTANCESPERNODE + VARIABLESPERTYPE*MEMORYTYPES;
}

void memory_ini()
{
	// enable the following code to use 32KB SRAM on the XMEM interface
	/*
	MCUCR |= (1<<SRE) | (1<<SRW10);
	XMCRA = 0x00;
	XMCRB = 0x01;
	__malloc_heap_start = (void *) 0x1100;
        __malloc_heap_end = (void *) 0x90FF;
	*/

	t_int cnter;
	for(cnter=0;cnter<MEMORYTYPES;cnter++)
	{
		for(t_int cnter2 = 1;cnter2<VARIABLESPERTYPE/T_INTSIZE+1;cnter2++)
		{
			memoryfuses[cnter][cnter2] = 0;
		}
		memoryfuses[cnter][0] = 1;
	}
	for(cnter=0;cnter<MAX_MYBOOLVARS+1;cnter++)
	{
		boolVars[cnter] = 0;			//default bool
	}
	for(cnter=0;cnter<MAX_MYINTVARS+1;cnter++)
	{
		intVars[cnter] = 0;	//default int
	}
	for(cnter=0;cnter<MAX_MYRINGVARS+1;cnter++)
	{
		ringVars[cnter] = NULL;		//default ring
	}
	for(cnter=0;cnter<MAX_MYVOIDVARS+1;cnter++)
	{
		voidEvent[cnter] = 0;		//default ring
	}	
}

