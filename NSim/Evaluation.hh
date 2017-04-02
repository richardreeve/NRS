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

#ifndef _EVALUATION_HH
#define _EVALUATION_HH

#include "Float.hh"

namespace NRS
{
  namespace Simulator
  {
    /// Variable type which latches a input based on a boolean
    class Evaluation : public Type::Float
    {
    public:
      /// Constructor
      /**
       *
       * \param aName name of variable to be constructed
       *
       **/
      Evaluation( std::string aName );
      
      /// Empty destructor
      virtual ~Evaluation();

      /// Note arrival of specialised connection - the timestep
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void addReaction( integer_t aReference );

      /// Timestep has changed
      /**
       *
       * \param aReference the reference the Observable was given to return
       *
       **/
      virtual void react( integer_t aReference );

    protected:
      /// Remove timestep from 
      /**
       *
       * \param aRef the reference the Observable was given to return
       *
       **/
      virtual void doRemoveSource( integer_t aReference );

      /// Update internal state of variable
      /**
       *
       * Must be overridden by all self-updating variables
       *
       * This method does non-generic internal update on the
       * variable. It is only called if the method is self-updating
       *
       **/
      virtual void doUpdate();

      /// Enable reference
      integer_t iEReference;

      /// Enable value pointer (NULL if missing)
      bool *iEnableValuePtr;

      /// A local cache of the enable boolean
      bool iEnableValue;
    };
  }
}

#endif //ndef _EVALUATION_HH
