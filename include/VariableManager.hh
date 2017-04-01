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

#ifndef _VARIABLE_MANAGER_HH
#define _VARIABLE_MANAGER_HH

#pragma interface
#include <string>
#include <vector>

#include "Exception.hh"
#include "MessageDescription.hh"
#include "MessageStore.hh"
#include "SegmentDescription.hh"
#include "Target.hh"

namespace NRS
{
  namespace Base
  {
    class Segment;
    class Variable;
    class ExternalInterface;
    class IntelligentStore;

    /// This is the base class for Variable managers.
    class VariableManager
    {
    public:
      /// Constructor for types and units
      /**
       *
       * This constructor is the base constructor for all
       * TypeManagers. VariableManagers are only constructed
       * statically at initialisation time, at which point they
       * register themselves with the VariableNodeDirector. After the
       * initialisation phase, CSL requests are made - the managers
       * must be configured by then.
       *
       * \param rootType root type of type or unit being managed
       *
       **/
      VariableManager( Enum::Type rootType );
      
      /// Constructor for messages
      /**
       *
       * This constructor is the base constructor for all Unit and
       * MessageManagers. VariableManagers are only constructed
       * statically at initialisation time, at which point they
       * register themselves with the VariableNodeDirector. After the
       * initialisation phase, CSL requests are made - the managers
       * must be configured by then.
       *
       * \param aMessageName Name of the Message type that will be
       * managed.
       * \param aDisplayName Prettified name of the Message type
       * (leave empty for same as aMessageName).
       * \param numSegments number of segments in the message.
       * \param isUnit is it a unit (or a message)?
       * \param baseType type this variable is derived from (if any)
       *
       **/
      VariableManager( std::string aMessageName,
		       std::string aDisplayName,
		       unsigned_t numSegments,
		       bool isUnit = false,
		       Enum::Type baseType = Enum::NoType );

      /// Destructor
      /**
       *
       * The destructor deregisters the manager from the
       * VariableNodeDirector.
       *
       **/
      virtual ~VariableManager();

      /// Register the VariableManager with the VariableNodeDirector if not
      /// already registered.
      /**
       *
       * \exception Exception if the VariableManager is already added,
       * or does not match an already inserted one.
       *
       **/
      void doRegister();

      /// Unregister VariableManager from VariableNodeDirector
      void unRegister();

      /// Get the name message, unit or type being managed.
      /**
       *
       * \return the message, unit or type being managed.
       *
       **/
      const std::string &getMessageName() const
      {
	return iMessageDescription.iName;
      }

      /// Get the root unit or type of this message.
      /**
       *
       * \return the root unit or type of that being managed.
       *
       **/
      Enum::Type getRootType() const
      {
	return iRootType;
      }

      /// Is the Variable being managed a basic type?
      /**
       *
       * \return bool representing whether the Variable being managed
       * is a basic type.
       *
       **/
      bool isType() const
      {
	return iIsType;
      }

      /// Is the Variable being managed a unit?
      /**
       *
       * \return bool representing whether the Variable being managed
       * is a unit (simple derived type).
       *
       **/
      bool isUnit() const
      {
	return iIsUnit;
      }

      /// Configure the internal records of the message type.
      /**
       *
       * This method configures the internal records of the manager so
       * that it can correctly output its CSL description on
       * request. This cannot conveniently be done at construction
       * time since too much work would be devolved to disparate
       * constructors.
       *
       **/
      void configure();

      /// Tests whether two VariableManagers are the same.
      /**
       *
       * This method tests whether two VariableManagers are the
       * same. This is necessary since multiple VariableManagers may
       * be defined for the same Variable type, and it is necessary to
       * confirm that they agree about the Variable's definition.
       *
       * \return bool representing whether the VariableManagers are
       * the same.
       *
       **/
      bool differ( const VariableManager &aVM ) const;

      /// Get the CSL description of a Variable/Message type.
      /**
       *
       * \param csl the output stream to which the CSL description
       * should be output.
       *
       * \return the output stream to which the CSL description was
       * output.
       *
       **/
      std::ostream &getCSL( std::ostream& csl ) const;
      
      /// Get number of segments
      /**
       *
       * \return number of segments
       *
       **/
      unsigned_t getNumSegments() const
      {
	return iNumSegments;
      }

      /// Get segment description
      /**
       *
       * \param num number of the segment (starting at 0)
       *
       * \return reference to const SegmentDescription
       *
       **/
      const SegmentDescription &getSegmentDescription( unsigned num ) const;

      /// Get message description
      /**
       *
       * \return reference to const MessageDescription
       *
       **/
      const MessageDescription &getMessageDescription() const
      {
	return iMessageDescription;
      }

      /// Translate an external message to another port
      /**
       *
       * \param anEIPtr pointer to ExternalInterface to which to return message
       * \param aTarget the target to which to route the message
       * \param aMessageStore message to be translated
       * \param intelligentPtr pointer to intelligent message
       * structure if present (or NULL)
       *
       **/
      void translateMessage( ExternalInterface *anEIPtr,
			     Target &aTarget, 
			     const MessageStore &aMessageStore,
			     const IntelligentStore *intelligentPtr ) const;

      /// Get parent variable manager
      /**
       *
       * \return parent variable manager or null if none appropriate
       *
       **/
      virtual const VariableManager *getParent() const;

      /// Create a segment for a link of this type
      /**
       *
       * \param aSDRef segment description for the segment
       * \returns segment of same type as variable, belonging to caller
       *
       **/
      virtual Segment *createSegment( const SegmentDescription &aSDRef ) const;

      /// Create a default variable of this type
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

      /// The name of the parent type
      Enum::Type iRootType;

      /// The prettified name of the Variable or Message type for display.
      std::string iDisplayName;

      /// The description of the message
      std::string iDescription;

      /// The number of segment in the message.
      unsigned_t iNumSegments;

      /// Is the Variable is a basic type?
      bool iIsType;

      /// Is the Variable is a unit?
      bool iIsUnit;

      /// Is the manager registered?
      bool iIsRegistered;

      /// Is the manager configured?
      bool iIsConfigured;

      /// Name of parent type
      std::string iParentName;

      /// Temporary store for SegmentDescriptions before configuration
      std::vector< SegmentDescription > iSegmentDescription;

      /// struct containing total message description in const form
      MessageDescription iMessageDescription;
    };
  }
}
      
#endif //ndef _VARIABLE_MANAGER_HH
