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


#pragma implementation
#include "Error.hh"

#include "StringLiterals.hh"
#include "ErrorManager.hh"
#include "Target.hh"
#include "SetErrorRoute.hh"
#include "VariableNodeDirector.hh"
#include "Executive.hh"

NRS::Message::Error::Error( std::string aName ) :
  Variable( aName, NRS::Literals::Object::ERROR_VARIABLE() ),
  iSetErrorRoute( NULL )
{
}

bool NRS::Message::Error::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iSetErrorRoute)
    {
      if (theVND.hasVariable( Literals::Object::SET_ERROR_ROUTE_VARIABLE() ) )
	{
	  iSetErrorRoute = dynamic_cast< SetErrorRoute* >
	    ( theVND.getVariable( Literals::Object::
				  SET_ERROR_ROUTE_VARIABLE() ) );
	}
    }
  return (iSetErrorRoute != NULL);
}

NRS::Message::Error::~Error()
{
}

void NRS::Message::Error::receiveMessage( unsigned_t priority,
					  unsigned_t errID,
					  const char* errorMessage )
{
      _DEBUG_("");
  if (iSetErrorRoute->hasErrorRoute() && 
      (priority >= iSetErrorRoute->getMinPriority()))
    {
      _DEBUG_("");
      sendMessage( iSetErrorRoute->getErrorRoute(),
		   priority, errID, errorMessage );
    }
  else
    {
      _DEBUG_("");
      std::cerr << "Error report (priority " << priority
		<< ", error ID " << errID << ")\n\t"
		<< "'" << errorMessage << "'\n";
    }
      _DEBUG_("");
}

void NRS::Message::Error::sendMessage( Base::Target &theTarget,
				       unsigned_t priority,
				       unsigned_t errID,
				       const char* errorMessage )
{
  dynamic_cast< ErrorManager* >( iVM )->sendMessage( theTarget,
						     priority, errID,
						     errorMessage );
}
