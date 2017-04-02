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

#ifndef _REPLY_MAXPORT_MANAGER_HH
#define _REPLY_MAXPORT_MANAGER_HH

#include <string>

#include "VariableManager.hh"

namespace NRS
{
  namespace Message
  {
    /// The manager for the ReplyMaxPort Variable type.
    /** 
     *
     * This is the base class for all managers of Variables which can
     * query node ids - it calls the node factories to do the construction
     *
     **/
    class ReplyMaxPortManager : public Base::VariableManager
    {
    public:
      /// Default constructor.
      /**
       *
       * This is the constructor for the basic ReplyMaxPortManager.
       *
       **/
      ReplyMaxPortManager();

      /// Destructor.
      virtual ~ReplyMaxPortManager()
      {
      }

      /// Create a new Variable
      /**
       *
       * \param aName name of new variable to create
       *
       **/
      virtual void createVariable( const std::string &aName ) const;

    protected:
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
      virtual void doConfigure();
    };
  }
}
#endif //ndef _REPLY_MAXPORT_MANAGER_HH
