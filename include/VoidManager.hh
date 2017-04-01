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

#ifndef _VOID_MANAGER_HH
#define _VOID_MANAGER_HH

#include <string>

#pragma interface
#include "VariableManager.hh"


namespace NRS
{
  namespace Base
  {
    class Target;
  }
  /// Namespace for all the fundamental types
  namespace Type
  {
    /// The manager for the Void Variable type.
    /** 
     *
     * This is the base class for all managers of Variables with no
     * arguments - Variables of Void type.
     *
     **/
    class VoidManager : public NRS::Base::VariableManager
    {
    public:
      /// Constructor.
      /**
       *
       * This is the constructor for a Manager derived from the
       * VoidManager.
       *
       * \param aMessageName Name of the Message type that will be
       * managed.
       * \param aDisplayName Prettified name of the Message type
       * (leave empty for same as aMessageName).
       *
       **/
      VoidManager( std::string aMessageName, std::string aDisplayName );

      /// Default constructor.
      /**
       *
       * This is the constructor for the basic VoidManager.
       *
       **/
      VoidManager();

      /// Destructor.
      virtual ~VoidManager()
      {
      }

      /// Repository for text string "void"
      /**
       *
       * \return the string "void"
       *
       **/
      static const char *getName()
      {
	static const char *name = "void";
	return name;
      }

      /// Create a default variable of this type
      /**
       *
       * \param aName name of new variable to create
       *
       **/
      virtual void createVariable( const std::string &aName ) const;

      /// Configure the internal records of the message type.
      /**
       *
       * This method configures the internal records of the manager so
       * that it can correctly output its CSL description on
       * request. This cannot conveniently be done at construction
       * time since too much work would be devolved to disparate
       * constructors. Called by configure(). Must create all of the
       * SegmentDescriptions for the manager.
       *
       **/
      virtual void doConfigure()
      {
      }
    };
  }
}
#endif //ndef _VOID_MANAGER_HH
