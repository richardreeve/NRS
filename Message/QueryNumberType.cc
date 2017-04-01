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
#include "VariableNodeDirector.hh"
#include "StringLiterals.hh"
#include "NodeFactory.hh"
#include "Node.hh"
#include "Variable.hh"
#include "Target.hh"
#include "ReplyNumberType.hh"
#include "QueryNumberTypeManager.hh"
#include "Types.hh"

#pragma implementation
#include "QueryNumberType.hh"

NRS::Message::QueryNumberType::QueryNumberType( std::string aName ) :
  Variable( aName, NRS::Literals::Object::QUERYNUMBERTYPE_VARIABLE() ),
  iReplyNumberType( NULL )
{
}

NRS::Message::QueryNumberType::~QueryNumberType()
{
}

bool NRS::Message::QueryNumberType::resolveComplexDependencies()
{
  Base::VariableNodeDirector &theVND =
    Base::VariableNodeDirector::getDirector();

  if (!iReplyNumberType)
    {
      if (theVND.hasVariable( Literals::Object::REPLYNUMBERTYPE_VARIABLE() ) )
	{
	  iReplyNumberType = dynamic_cast< ReplyNumberType* >
	    ( theVND.getVariable( Literals::Object::
				  REPLYNUMBERTYPE_VARIABLE() ) );
	}
    }
  return (iReplyNumberType != NULL);
}

void NRS::Message::QueryNumberType::sendMessage( Base::Target &theTarget,
						 Base::Target &returnTarget,
						 unsigned_t aMessageID )
{
  returnTarget.setTargetVNID( iReplyNumberType->getVNID() );

  dynamic_cast< QueryNumberTypeManager* >( iVM )->sendMessage( theTarget,
							     returnTarget,
							     aMessageID );
}


void NRS::Message::QueryNumberType::receiveMessage( Base::Target &returnTarget,
						    unsigned_t aMessageID )
{
  // send the vnid to ReplyNumberType variable
  iReplyNumberType->sendMessage( returnTarget, aMessageID, NUMBER_BITS, true );
}
