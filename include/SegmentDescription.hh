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

#ifndef _SEGMENT_DESCRIPTION_HH
#define _SEGMENT_DESCRIPTION_HH

#pragma interface

#include <iostream>
#include <string>

#include "Types.hh"

namespace NRS
{
  namespace Base
  {
    class Restriction;
    class Segment;

    /// This is the class which holds segment descriptions
    class SegmentDescription
    {
    public:
      /// Default constructor
      SegmentDescription();

      /// Copy constructor
      /**
       *
       * The copy constructor has one (optional) argument more than
       * usual, which allows a specialised copy for a segment
       * inherited from an original unit. Since the inheritance
       * mechanism will automatically inherit any restrictions on the
       * original unit, such restrictions are not copied.
       *
       * \param rhs object to be copied
       * \param withRestrictions do we copy the restrictions as
       * well? Usually this is not required as the duplication is for
       * the purposes of creating a new segment inherited from the
       * existing one - in which case it will inherit these
       * restrictions anyway
       *
       * \return a newly created SegmentDescription whose ownership
       * passes to the caller
       *
       **/
      SegmentDescription( const SegmentDescription &rhs,
			  bool withRestrictions = true );

      /// Deep copy required
      /**
       *
       * \param rhs SegmentDescriptino to be copied
       *
       * \return reference to object copied into
       *
       **/
      SegmentDescription &operator=( const SegmentDescription &rhs );

      /// Destructor
      ~SegmentDescription();

      /// Set type and unit (if appropriate)
      /**
       *
       * \param aType base type of segment
       * \param aUnitName unit type of message if a unit, empty if a type
       * \param aDisplayName the pretty-print name if desired
       *
       **/
      void setType( Enum::Type aType,
		    std::string aUnitName = "",
		    std::string aDisplayName = "" );

      /// Get type
      /**
       *
       * \return type of segment
       *
       **/
      Enum::Type getType() const
      {
	return iType;
      }

      /// Is the segment a unit?
      /**
       *
       * \return whether segment is a unit
       *
       **/
      bool isUnit() const
      {
	return iIsUnit;
      }

      /// Get unit name
      /**
       *
       * \return const reference to the unit name
       *
       **/
      const std::string &getUnitName() const
      {
	return iUnitName;
      }

      /// Set attribute name
      /**
       *
       * \param aName attribute name
       * \param aDisplayName the pretty-print name if desired
       *
       **/
      void setAttributeName( std::string aName,
			     std::string aDisplayName="" );

      /// Get attribute name
      /**
       *
       * \return const reference to the attribute name
       *
       **/
      const std::string &getAttributeName() const
      {
	return iName;
      }

      /// Set whether the segment is in message contents
      /**
       *
       * \param inContents whether segment is part of contents
       *
       **/
      void setInContents( bool inContents );

      /// Is the segment in message contents?
      /**
       *
       * \return whether segment is part of contents
       *
       **/
      bool isInContents() const
      {
	return iInContents;
      }

      /// Set description for segment
      /**
       *
       * \param aDescription description of segment
       *
       **/
      void setDescription( std::string aDescription );

      /// Set restriction for segment
      /**
       *
       * \param aRestriction restriction of segment
       *
       **/
      void setRestriction( Restriction *aRestrictionPtr );

      /// Get restriction (if any)
      /**
       *
       * \return restriction for segment (if any)
       *
       **/
      const Restriction *getRestriction() const
      {
	return iRestrictionPtr.get();
      }

      /// Get the CSL description of a single segment
      /**
       *
       * \param csl the output stream to which the CSL description
       * should be output.
       *
       * \return the output stream to which the CSL description was
       * output.
       *
       **/
      std::ostream &getCSLSegment( std::ostream& csl ) const;

      /// Get the CSL description of a unit
      /**
       *
       * \param csl the output stream to which the CSL description
       * should be output.
       *
       * \return the output stream to which the CSL description was
       * output.
       *
       **/
      std::ostream &getCSLUnit( std::ostream& csl ) const;
      
      /// Return a new segment of this type, and hand over ownership to caller
      /**
       *
       * \return a pointer to a segment of this type
       *
       **/
      Segment *createSegment() const;

    private:
      /// Is it configured?
      bool iIsConfigured;

      /// The name of the segment
      std::string iName;

      /// The prettified display name of the segment
      std::string iDisplayName;

      /// The description of the segment of the message.
      std::string iDescription;

      /// The basic type of the segment
      Enum::Type iType;

      /// Whether the segment is a unit (true) or just a basic type (false)
      bool iIsUnit;

      /// Name of the unit for the segment if appropriate
      std::string iUnitName;

      /// Whether the segment appears as an attribute (false) or a
      /// content (true) of the message in PML
      bool iInContents;

      /// Possible restriction information
      std::auto_ptr< Restriction > iRestrictionPtr;

    };
  }
}
      
#endif //ndef _SEGMENT_DESCRIPTION_HH
