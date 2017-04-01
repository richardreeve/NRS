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

#ifndef _BOOLEAN_HH
#define _BOOLEAN_HH

#pragma interface

#include <string>
#include <vector>

#include "VariableManager.hh"
#include "Variable.hh"

namespace NRS
{
  namespace Type
  {
    class BooleanManager;
    /// A variable type for truth value message handlers.
    /**
     *
     * The Boolean class represents all message senders and receivers
     * which send messages consisting of one truth value
     *
     **/
    class Boolean : public Base::Variable
    {
    public:
      /// Constructor for Boolean Variables.
      /**
       *
       * \param aName the name of the Boolean Variable being built.
       *
       **/
      Boolean( std::string aName );
      
      /// Constructor for Variables derived from Boolean.
      /**
       *
       * \param aName the name of the Variable being built.
       * \param theVMRef reference to the VariableManager which manages
       * that class.
       *
       **/
      Boolean( std::string aName, const Base::VariableManager &theVMRef );
      
      /// Destructor.
      virtual ~Boolean();

      /// Set default value of Boolean
      /**
       *
       * \param aDefault the default value
       *
       **/
      void setDefault( bool aDefault );

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

      /// the value stored by this Boolean (from the OutputLink's Segment)
      bool &iValue;

      /// the default value for this Boolean
      bool iDefault;

      /// vector of input variables
      std::vector< const bool* > iInput;
    };
  }
}

#endif //ndef _BOOLEAN_HH
