# Copyright (C) 2004 Richard Reeve, Darren Smith, Matthew Szenher
#                    and Edinburgh University
#
#    This program is free software; you can redistribute it and/or
#    modify it under the terms of the GNU General Public License as
#    published by the Free Software Foundation; either version 2 of
#    the License, or (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public
#    License along with this program; if not, write to the Free
#    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
#    MA 02111-1307 USA
#
# For further information in the first instance contact:
# Richard Reeve <richardr@inf.ed.ac.uk>

lib_LTLIBRARIES = libTypes.la
libTypes_la_SOURCES = IntegerRestriction.cc FloatRestriction.cc		\
	StringRestriction.cc VectorRestriction.cc IntegerSegment.cc	\
	FloatSegment.cc StringSegment.cc BooleanSegment.cc		\
	RouteSegment.cc IVectorSegment.cc BVectorSegment.cc		\
	FVectorSegment.cc Void.cc VoidManager.cc Boolean.cc		\
	BooleanManager.cc String.cc StringManager.cc  Float.cc		\
	FloatManager.cc Integer.cc IntegerManager.cc Route.cc		\
	RouteManager.cc #Vector.cc VectorManager.cc

#libTypes_la_LDFLAGS = -static
INCLUDES = -I$(top_srcdir)/include
SUBDIRS =
