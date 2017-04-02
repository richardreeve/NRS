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

#ifndef _VECTOR_RESTRICTION_HH
#define _VECTOR_RESTRICTION_HH

#include <memory>
#include <iostream>
#include <string>
#include "Exception.hh"
#include "Types.hh"
#include "Restriction.hh"
#include "Limit.hh"

namespace NRS
{
  namespace Type
  {
    /// This is the base class for restrictions
    class VectorRestriction : public Base::Restriction
    {
    public:
      /// Default constructor
      /**
       *
       * \param isFloat does the vector contain floating point numbers?
       *
       **/
      VectorRestriction( bool isFloat = false ) :
	Restriction( Enum::Vector ),
	iIsFloat( isFloat )
      {
      }

      /// Full constructor
      /**
       *
       * \param isFloat does the vector contain floating point numbers?
       * \param dataBits number of bits in data chunk
       * \param numElements number of elements in vector
       *
       **/
      VectorRestriction( bool isFloat,
			 const Base::Limit< integer_t > &dataBits,
			 const Base::Limit< integer_t > &numElements ) :
	Restriction( Enum::Vector ),
	iIsFloat( isFloat ), iDataBits( dataBits ), iNumElements( numElements )
      {
	if (iIsFloat && (iDataBits.hasLimit()) && 
	    (iDataBits.get() != 32) && (iDataBits.get() != 64))
	  _ABORT_( "Trying to create floating point vector "
		   "of non-standard length" );
      }

      /// Destructor
      ~VectorRestriction()
      {
      }
      
      /// Set number of data bits per chunk
      /**
       *
       * \param bits number of bits per chunk
       *
       **/
      void setDataBits( integer_t bits )
      {
	iDataBits.set( bits );
	if (iIsFloat && (bits != 32) && (bits != 64))
	  _ABORT_( "Trying to create floating point vector "
		   "of non-standard length" );
      }

      /// Clear number of data bits
      void clearDataBits()
      {
	iDataBits.clear();
      }

      /// Does it have a number of data bits?
      /**
       *
       * \return whether there is a value set
       *
       **/
      bool hasDataBits() const
      {
	return iDataBits.hasLimit();
      }

      /// Get number of data bits if known, otherwise default
      /**
       *
       * \return the number of data bits if known, otherwise default
       *
       **/
      integer_t getDataBits() const
      {
	if (hasDataBits())
	  return iDataBits.get();
	else
	  return (iIsFloat ? 32 : 8 );
      }

      /// Set number of elements in data
      /**
       *
       * \param num number of elements
       *
       **/
      void setMax( integer_t num )
      {
	iNumElements.set( num );
      }

      /// Clear number of elements
      void clearNumElements()
      {
	iNumElements.clear();
      }

      /// Does it have a number of elements set?
      /**
       *
       * \return whether there is a number set
       *
       **/
      bool hasNumElements() const
      {
	return iNumElements.hasLimit();
      }

      /// Get number of elements in vector if present
      /**
       *
       * \return the number of elements if present
       *
       **/
      integer_t getNumElements() const
      {
	return iNumElements.get();

      }

      /// Clone the restriction, pass over ownership to caller
      /**
       *
       * \return a copy of the restriction (not necessarily the base
       * class, otherwise we could just use the copy constructor)
       *
       **/
      virtual Restriction *clone() const
      {
	return new VectorRestriction( *this );
      }

      /// get CSL for restriction
      /**
       *
       * \param csl the output stream to which the CSL description
       * should be output.
       *
       * \return the output stream to which the CSL description was
       * output.
       *
       **/
      virtual std::ostream &getCSL( std::ostream& csl ) const
      {
	if ( iIsFloat || iDataBits.hasLimit() || iNumElements.hasLimit() )
	  {
	    csl << "<VectorInfo ";

	    if (iIsFloat)
	      csl << "floatingPoint='true' ";

	    if (iDataBits.hasLimit())
	      csl << "dataBits='" << iDataBits.get() << "' ";

	    if (iNumElements.hasLimit())
	      csl << "numElements='" << iNumElements.get() << "' ";

	    csl << "/>" << std::endl;
	  }
	return csl;
      }

    private:
      /// Whether the vector is restricted to floating point numbers
      bool iIsFloat;

      /// Number of bits per chunk of data
      Base::Limit< integer_t > iDataBits;

      /// Number of elements in vector
      Base::Limit< integer_t > iNumElements;
      
    };
  }
}

#endif //ndef _VECTOR_RESTRICTION_HH
