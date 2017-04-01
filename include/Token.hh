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

#ifndef _TOKEN_HH
#define _TOKEN_HH

#include <string>

#pragma interface
#include "VariableManager.hh"
#include "String.hh"


namespace NRS
{
  namespace Unit
  {
    /// A variable type for token message handlers.
    /**
     *
     * The Token class represents all message senders and receivers
     * which send messages consisting of one token
     *
     **/
    class Token : public Type::String
    {
    public:
      /// Constructor for Token Variables.
      /**
       *
       * \param aName the name of the Token Variable being built.
       *
       **/
      Token( std::string aName );
      
      /// Constructor for Variables derived from Token.
      /**
       *
       * \param aName the name of the Variable being built.
       * \param aVMRef reference to the VariableManager which manages
       * that class.
       *
       **/
      Token( std::string aName,
		const Base::VariableManager &aVMRef );
      
      /// Destructor.
      virtual ~Token();
    };
  }
}

#endif //ndef _TOKEN_HH
