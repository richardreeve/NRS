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

lib_LTLIBRARIES = libUnits.la
libUnits_la_SOURCES = Capacitance.cc CapacitanceManager.cc	\
	Conductance.cc ConductanceManager.cc			\
	Current.cc CurrentManager.cc				\
	Frequency.cc FrequencyManager.cc			\
	Time.cc TimeManager.cc					\
	Voltage.cc VoltageManager.cc				\
	Filename.cc FilenameManager.cc				\
	Token.cc TokenManager.cc				\
	VNName.cc VNNameManager.cc				\
	Compass.cc CompassManager.cc
#libUnits_la_LDFLAGS = -static
AM_CPPFLAGS = -I$(top_srcdir)/include
SUBDIRS =
