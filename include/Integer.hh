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

#ifndef _INTEGER_HH
#define _INTEGER_HH

#pragma interface

#include <string>
#include <vector>

#include "VariableManager.hh"
#include "Variable.hh"
#include "Types.hh"

namespace NRS
{
  namespace Type
  {
    class IntegerManager;
    /// A variable type for truth value message handlers.
    /**
     *
     * The Integer class represents all message senders and receivers
     * which send messages consisting of one truth value
     *
     **/
    class Integer : public Base::Variable
    {
    public:
      /// Constructor for Integer Variables.
      /**
       *
       * \param aName the name of the Integer Variable being built.
       *
       **/
      Integer( std::string aName );
      
      /// Constructor for Variables derived from Integer.
      /**
       *
       * \param aName the name of the Variable being built.
       * \param theVMRef reference to the VariableManager which manages
       * that class.
       *
       **/
      Integer( std::string aName, const Base::VariableManager &theVMRef );
      
      /// Destructor.
      virtual ~Integer();

      /// Set default value of Integer
      /**
       *
       * \param aDefault the default value
       *
       **/
      void setDefault( integer_t aDefault );

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

      /// the value stored by this Integer (from the OutputLink's Segment)
      integer_t &iValue;

      /// the default value for this Integer
      integer_t iDefault;

      /// vector of input variables
      std::vector< const integer_t* > iInput;
    };
  }
}

#endif //ndef _INTEGER_HH
