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

#pragma implementation
#include "Voltage.hh"
#include "VoltageManager.hh"
#include "FloatManager.hh"
#include "FloatRestriction.hh"

namespace
{
  NRS::Unit::VoltageManager sVoltageM;
}

NRS::Unit::VoltageManager::VoltageManager( std::string aMessageName,
					   std::string aDisplayName ) :
  Type::FloatManager( aMessageName, aDisplayName, false )
{
}

NRS::Unit::VoltageManager::VoltageManager() : 
  NRS::Type::FloatManager( getName(), "", true )
{
  iDescription = "Voltage is measured in Volts.";
  iSegmentDescription[0].setType( Enum::Float, getName() );
   iSegmentDescription[0].
     setRestriction( new Type::FloatRestriction( "V", "m" ) );
}

void 
NRS::Unit::VoltageManager::createVariable( const std::string &aName ) const
{
  new Voltage( aName );
}
