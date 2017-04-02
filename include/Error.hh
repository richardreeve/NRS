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

#ifndef _ERROR_HH
#define _ERROR_HH

#include <string>

#include "Variable.hh"

namespace NRS
{
  namespace Message
  {
    class ErrorManager;
    class SetErrorRoute;
    /// A special variable type to handle Error messages.
    /**
     *
     * The Error class represents all message senders and receivers
     * which deal with Error messages
     *
     **/
    class Error : public NRS::Base::Variable
    {
    public:
      /// Constructor for Error Variables.
      /**
       *
       * \param aName the name of the Error Variable being built.
       *
       **/
      Error( std::string aName );
      
      /// Destructor.
      virtual ~Error();

      /// Receive a message from a source.
      /**
       *
       * This method tells the Variable to receive a message.
       *
       * \param priority priority of the message
       * \param errID the error number
       * \param errorMessage the text error message
       *
       **/
      virtual void receiveMessage( unsigned_t priority,
				   unsigned_t errID,
				   const char* errorMessage );

      /// Send a Error message.
      /**
       *
       * \param theTarget the target for the error message
       * \param priority priority of the message
       * \param errID the error number
       * \param errorMessage the text error message
       *
       **/
      virtual void sendMessage( Base::Target &theTarget,
				unsigned_t priority,
				unsigned_t errID,
				const char* errorMessage );

      /// resolve any dependency issues
      /**
       *
       * \returns whether all dependencies are resolved
       *
       **/
      virtual bool resolveComplexDependencies();
    private:
      /// Pointer to SetErrorRoute variable, with with Error communicates
      SetErrorRoute *iSetErrorRoute;
    };
  }
}

#endif //ndef _ERROR_HH
