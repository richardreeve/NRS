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

#include "SummedCurrent.hh"
#include "FloatSegment.hh"
#include "VariableNodeDirector.hh"

void
NRS::Simulator::SummedCurrent::doAddSource( const Base::Location &aSourceRef,
					    integer_t aRef )
{
  if ((integer_t) iInput.size() <= aRef)
    {
      iInput.resize( aRef + 1 );
      iCache.resize( aRef + 1 );
    }
  if (aSourceRef.isLocal())
    {
      iInput[aRef] =
	&dynamic_cast< Type::FloatSegment& >( Base::VariableNodeDirector::
					      getDirector().
					      getVariable( aSourceRef.
							   getVNID() )->
					      getOutputLink().
					      getSegment( 0 ) ).iValue;
      iCache[aRef] = *iInput[aRef];
    }
}

void NRS::Simulator::SummedCurrent::doObserve( integer_t aRef )
{
  iSendMessage = true;
  float_t val = 0;
  iCache[aRef] = *iInput[aRef];

  for ( unsigned_t num = 0; num < iInput.size(); ++num )
    {
      if (iInput[num])
	val += iCache[num];
    }

  if (!iStateHolding || (val != iValue))
    {
      iSendMessage = true;
      iValue = val;
    }
}
