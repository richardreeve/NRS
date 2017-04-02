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

#ifndef _FLOAT_HH
#define _FLOAT_HH

#include <string>
#include <vector>

#include "VariableManager.hh"
#include "Variable.hh"
#include "Types.hh"

namespace NRS
{
  namespace Type
  {
    class FloatManager;
    /// A variable type for truth value message handlers.
    /**
     *
     * The Float class represents all message senders and receivers
     * which send messages consisting of one truth value
     *
     **/
    class Float : public Base::Variable
    {
    public:
      /// Constructor for Float Variables.
      /**
       *
       * \param aName the name of the Float Variable being built.
       *
       **/
      Float( std::string aName );
      
      /// Constructor for Variables derived from Float.
      /**
       *
       * \param aName the name of the Variable being built.
       * \param theVMRef reference to the VariableManager which manages
       * that class.
       *
       **/
      Float( std::string aName, const Base::VariableManager &theVMRef );
      
      /// Destructor.
      virtual ~Float();

      /// Set default value of Float
      /**
       *
       * \param aDefault the default value
       *
       **/
      void setDefault( float_t aDefault );

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

      /// Implement any variable specific behaviour to remove Observable
      /**
       *
       * Needs to be overriden if any specific behaviour needs to be
       * done when a source is removed.
       *
       * \param aRef the reference the Observable was given to
       * return
       *
       **/
      virtual void doRemoveSource( integer_t aRef );

      /// Reset the system to its initial state
      virtual void doReset();

      /// New data is available to observe from a source
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doObserve( integer_t aRef );

      /// the value stored by this Float (from the OutputLink's Segment)
      float_t &iValue;

      /// the default value for this Float
      float_t iDefault;

      /// vector of input variables
      std::vector< const float_t* > iInput;
    };
  }
}

#endif //ndef _FLOAT_HH
