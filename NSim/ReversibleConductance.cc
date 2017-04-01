/*
 * Copyright (C) 2004 Richard Reeve, Matthew Szenher and Edinburgh University
 *
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation; either version 2 of
 *    the License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public
 *    License along with this program; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307 USA
 *
 * For further information in the first instance contact:
 * Richard Reeve <richardr@inf.ed.ac.uk>
 *
 */

#include <list>
#include <string>
#pragma implementation
#include "ReversibleConductance.hh"
#include "ReversibleConductanceManager.hh"

NRS::Message::ReversibleConductance::
ReversibleConductance( std::string aName ) :
  Base::Variable( aName, Message::ReversibleConductanceManager::getName() ),
  iReversalPotential( 0 ), iSynapticConductance( 0 ),
  iDefaultReversalPotential( 0 ), iDefaultSynapticConductance( 0 ),
  iManager( dynamic_cast< ReversibleConductanceManager* >( iVM ) )
{
}

NRS::Message::ReversibleConductance::
ReversibleConductance( std::string aName, Base::VariableManager *aVM ) :
  Base::Variable( aName, aVM ),
  iReversalPotential( 0 ), iSynapticConductance( 0 ),
  iDefaultReversalPotential( 0 ), iDefaultSynapticConductance( 0 ),
  iManager( dynamic_cast< ReversibleConductanceManager* >( iVM ) )
{
}


NRS::Message::ReversibleConductance::
ReversibleConductance( std::string aName, std::string aType ) :
  Base::Variable( aName, aType ),
  iReversalPotential( 0 ),iSynapticConductance( 0 ),
  iDefaultReversalPotential( 0 ), iDefaultSynapticConductance( 0 ),
  iManager( dynamic_cast< ReversibleConductanceManager* >( iVM ) )
{}

NRS::Message::ReversibleConductance::~ReversibleConductance()
{
  while (!iLocalSourceList.empty())
    {
      iLocalSourceList.front()->removeTarget( this );
      iLocalSourceList.pop_front();
    }
  while (!iLocalTargetList.empty())
    {
      iLocalTargetList.front()->removeSource( this );
      iLocalTargetList.pop_front();
    }
}

void NRS::Message::ReversibleConductance::sendMessages()
{
  for ( std::list< ReversibleConductance* >::iterator
	  anIter = iLocalTargetList.begin();
	anIter != iLocalTargetList.end(); anIter++ )
    {
      (*anIter)->receiveMessage( iReversalPotential, iSynapticConductance );
    }
  for ( std::list< Base::Target* >::iterator anIter =
	  iDistalTargetList.begin();
	anIter != iDistalTargetList.end(); anIter++ )
    {
      iManager->sendMessage( **anIter, iReversalPotential,
			     iSynapticConductance );
    }
  for ( std::map< unsigned_t, Base::Target* >::iterator anIter =
	  iLoggingTargetMap.begin();
	anIter != iLoggingTargetMap.end(); anIter++ )
    {
      iManager->sendMessage( *(anIter->second), iReversalPotential,
			     iSynapticConductance );
    }
}
