/*
 * Copyright (C) 2004 Darren Smith, Richard Reeve and Edinburgh University
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

#ifndef _TYPES_HH
#define _TYPES_HH
#include <stdint.h>
#include <bitset>
#include <string>

#ifdef HAVE_CONFIG_H
# include "config.h"
#endif //def HAVE_CONFIG_H

namespace NRS
{
  /// The Unsigned type is a 32 bit unsigned type
  typedef uint32_t unsigned_t;
#define UNSIGNED_T_MAX (4294967295U)
#define UNSIGNED_BITS 32

  /// The Number type is a 32 bit signed type
  typedef int32_t number_t;
#define NUMBER_T_MAX (2147483647)
#define NUMBER_T_MIN (-NUMBER_T_MAX-1)
#define NUMBER_BITS 32

  /// The Integer type is a 32 bit signed type
  typedef int32_t integer_t;
#define INTEGER_T_MAX (2147483647)
#define INTEGER_T_MIN (-INTEGER_T_MAX-1)
#define INTEGER_BITS 32
#define MAX_INTEGER_STORE 10

  /// The Float type is a (32 bit) float
  typedef float float_t;
#define FLOAT_T_BITS 32
#define MAX_FLOAT_STORE 10

  /// A storage type for bmf data
  typedef std::basic_string< uint8_t > data_t;

  /// A storage type for route data
  typedef data_t route_t;

  inline route_t getEmptyRoute()
  {
    return route_t( 1, (uint8_t) 1 );
  }

  /// unsigned type of same length as a float for reading bits
  //  typedef uint32_t float_store_t;
  /// unsigned type of same length as a double for reading bits
  //  typedef uint64_t double_store_t;

  /// Number of encodings
#define NUM_ENCODINGS 2

  /// types of encoding being used
  typedef std::bitset< NUM_ENCODINGS > encoding_t;

  /// Number of intelligent attributes
#define NUM_INTELLIGENT_ATT 16

  /// whether intelligent attributes are present
  typedef std::bitset< NUM_INTELLIGENT_ATT > intelligent_t;
      
  /// namespace for constant values
  namespace Const
  {
    /// Position of route segment in message
    static const unsigned_t RoutePos = 0;
    /// Position of toVNID segment in message
    static const unsigned_t VNIDPos = 1;
    /// Number of compulsory segments in message
    static const unsigned_t SegNum = 2;
  }

  /// namespace for standard enumerations
  namespace Enum
  {
    /// Type of message encoding
    enum EncodingType
      {
	PML = 0,
	BMF = 1,
	NoEncoding = 2
      };

    /// Enumeration of basic types of Message 
    enum Type
      {
	Boolean,
	Integer,
	Float,
	String,
	Route,
	Vector,
	NoType
      };
    
    /// Enumeration of basic types of connection
    enum ConnectionType
      {
	File,
	Log,
	TCPIPClient,
	TCPIPServer,
	Fifo,
	Device
      };
    
    /// Enumeration of string restriction types
    enum StringRestrictionType
      {
	List,
	Token,
	Filename,
	VNName,
	NoStringRestriction
      };
    
    /// Enumeration of intelligent message types
    enum IntelligentType
      {
	IsIntelligent = 0,
	EndIMessage = 1,
	ForwardRoute = 2,
	ReturnRoute = 3,
	IsBroadcast = 4,
	HopCount = 5,
	TargetCID = 6,
	AckMsg = 7,
	AckVNID = 8,
	FailedRouteMsg = 9,
	FailedRouteVNID = 10,
	MsgID = 11,
	DummyIntSeg = 12,
	TranslationCount = 13,
	SourceCID = 14,
	TargetVNName = 15,
	InvalidIType
      };
  }
}

#endif //ndef _TYPES_HH
