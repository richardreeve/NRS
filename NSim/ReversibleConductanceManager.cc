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

#include <string>

#include "ReversibleConductanceManager.hh"
#include "ReversibleConductance.hh"
#include "VoltageManager.hh"
#include "ConductanceManager.hh"
#include "VariableManager.hh"
#include "Exception.hh"
#include "VariableNodeDirector.hh"

namespace
{
  NRS::Message::ReversibleConductanceManager sReversibleConductanceM;
}


NRS::Message::ReversibleConductanceManager::ReversibleConductanceManager() : 
  NRS::Base::VariableManager( "ReversibleConductance", 
			      "Reversible Conductance",
			      2)
{
  iDescription = "The message type for transmitting synaptic reversal potentials and conductances to neurons";
  
}


void NRS::Message::ReversibleConductanceManager::createVariable( std::string aName )
{
  new ReversibleConductance ( aName );
}


void NRS::Message::ReversibleConductanceManager::doConfigure(){

  iSegmentDescription[0] = theVND.getVariableManager( Unit::VoltageManager::getName() ).
    getSegmentDescription( 0 );
  iSegmentDescription[0].setAttributeName("revV", "Reversal Potential");
  
  iSegmentDescription[1] = theVND.getVariableManager( Unit::ConductanceManager::getName() ).
    getSegmentDescription( 0 );
  iSegmentDescription[1].setAttributeName("synG", "Synaptic Conductance");
  
}
