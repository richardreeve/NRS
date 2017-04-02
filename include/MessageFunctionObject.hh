/*
 * Copyright (C) 2005 Richard Reeve and Edinburgh University
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

#ifndef _MESSAGE_FUNCTION_OBJECT_HH
#define _MESSAGE_FUNCTION_OBJECT_HH

#include <string>

#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    class Variable;

    /// This is the base class for function objects which allow any
    /// variable to handle a specific (presumably generic) internal
    /// connection without having to be a specialised sub-class.
    class MessageFunctionObject
    {
    public:
      /// Constructor
      /**
       *
       * \param sourceName name of source of link
       * \param targetPtr pointer to target of link
       *
       **/
      MessageFunctionObject( std::string sourceName, Variable *targetPtr );

      /// Default destructor
      virtual ~MessageFunctionObject();

      /// set the MFO to be attached to a new Variable
      /**
       * 
       * Calls doReset() to do FO specific work
       *
       * \param aRef reference which will be used by variable observe calls.
       *
       **/
      void reset( integer_t aRef = 0 );

      /// get reference use by observe()
      /**
       * 
       * \return (negative) reference which is being used by variable
       * observe calls, 0 if not configured
       *
       **/
      integer_t getReference() const
      {
	return iReference;
      }

      /// Get the source name of the link
      /**
       *
       * \return the name of the source of the link
       *
       **/
      const std::string &getSourceName() const
      {
	return iSourceName;
      }
      
      
      /// Carry out the action associated with handling this message
      /**
       *
       * \param aRef reference used in call to observe
       *
       **/
      virtual void operator()() = 0;

      /// Resolve dependencies with finding 

    protected:
      /// Do any specialised work to attach to Variable, called by reset()
      virtual void doReset();

      /// Name of source
      std::string iSourceName;

      /// Pointer to the Variable this MFO is attached to.
      Variable *iTargetPtr;

      /// Reference used in observe call for link
      integer_t iReference;

    };

    /// Generator for MessageFunctionObjects
    class MFOGenerator
    {
    public:
      /// Empty constructor
      MFOGenerator();

      /// Empty destructor
      virtual ~MFOGenerator();

      /// Create a new instance of the MessageFunctionObject
      /**
       *
       * \param sourceName name of source of link
       * \param targetPtr pointer to target of link
       *
       * \return a new MessageFunctionObject, ownership of which
       * belongs to the caller.
       *
       **/
      virtual MessageFunctionObject *createMFO( std::string sourceName,
						Variable *targetPtr ) = 0;
    };
  }
}

#endif //ndef _MESSAGE_FUNCTION_OBJECT_HH
