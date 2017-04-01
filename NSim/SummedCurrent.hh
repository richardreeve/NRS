/*
 * Copyright (C) 2004-5 Richard Reeve, Matthew Szenher and Edinburgh University
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

#ifndef _SUMMED_CURRENT_HH
#define _SUMMED_CURRENT_HH


#pragma interface

#include "Current.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Class derived from current which sums inputs instead of taking last
    class SummedCurrent : public Unit::Current
    {
    public:
      /// Default constructor
      /**
       *
       * \param aName name of variable to construct
       *
       **/
      SummedCurrent( std::string aName ) : Unit::Current( aName )
      {
	iCache.resize( 1 );
	iCache[0] = *iInput[0];
      }
      
      /// Empty destructor
      virtual ~SummedCurrent()
      {
      }

    protected:
      /// Implement any variable specific behaviour for adding a Source
      /**
       *
       * \param aSourceRef the source of the message
       * \param aRef reference for message source
       *
       **/
      virtual void doAddSource( const Base::Location &aSourceRef,
				integer_t aRef );

      /// New data is available to observe from a source
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doObserve( integer_t aRef );

      /// Cache of local values of inputs
      std::vector< float_t > iCache;
    };
  }
}

#endif //ndef _SUMMED_CURRENT_HH
