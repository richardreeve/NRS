/*
 * Copyright (C) 2005 Richard Reeve, Matthew Szenher, Mark Payne and Edinburgh University
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

#pragma implementation
#include <cmath>

#include "Exception.hh"
#include "FloatSegment.hh"
#include "MessageSender.hh"
#include "Observable.hh"
#include "Time.hh"
#include "Updater.hh"
#include "VariableManager.hh"
#include "VariableNodeDirector.hh"
#include "RealTimeError.hh"
#include "SimTime.hh"

NRS::Simulator::RealTimeError::RealTimeError( std::string aName) :
  Unit::Time( aName ), iSimTimeReference( 0 ), iSimTimeErrorValuePtr( NULL ),
  iSimTimeErrorValue( 0 )
{
}

NRS::Simulator::RealTimeError::~RealTimeError()
{
}

void NRS::Simulator::RealTimeError::addReaction( integer_t aReference )
{
  iSimTimeReference = aReference;
  iSimTimeErrorValuePtr = dynamic_cast<NRS::Simulator::SimTime*>(Base::VariableNodeDirector::getDirector().getVariable( iParentName + ".SimTime" ))->getRealTimeErrorValuePtr();
}

void NRS::Simulator::RealTimeError::react( integer_t aReference )
{
  
  if (aReference == iSimTimeReference)
    {
      _ASSERT_( iSimTimeErrorValuePtr != NULL, "illegal call from NULL" );
      iSimTimeErrorValue = *iSimTimeErrorValuePtr;
      _DEBUG_("react " << iName << " retrieving value " << iSimTimeErrorValue);
      iValue=iSimTimeErrorValue;
      iSendMessage=true;
      update();
    }
  else
    _WARN_( "Unrecognised observable " << aReference );
}

void NRS::Simulator::RealTimeError::doRemoveSource( integer_t aReference )
{
  if (aReference == iSimTimeReference)
    {
      iSimTimeErrorValuePtr = NULL;
      iSimTimeErrorValue = 0;
      iSimTimeReference = 0;
    }
  else
    Type::Float::doRemoveSource( aReference );
}

void NRS::Simulator::RealTimeError::doUpdate(){

  
  iValue=iSimTimeErrorValue;
  _DEBUG_("update " << iName << ":"<<iValue);
  iSendMessage = true;
}

