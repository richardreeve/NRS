# $Id: Makefile.am,v 1.1 2006/07/25 16:45:22 theo Exp $
# Copyright (C) 2006 Richard Reeve, Darren Smith, Matthew Szenher
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

csldir = $(prefix)/share/$(PACKAGE)-$(VERSION)/csl

message_files = csl/message/OscilloscopeNode.xml csl/message/FloatNode.xml csl/message/VoidNode.xml

csl_DATA = oscilloscope.xml ${message_files}

noinst_PROGRAMS = oscilloscope.xml

# concatenate xml sources files to create one xml file.
oscilloscope_xml_SOURCES = csl/preamble.xml ${message_files} \
	csl/nodeDescription/OscilloscopeNode.xml \
	csl/nodeDescription/FloatNode.xml \
	csl/nodeDescription/VoidNode.xml \
	csl/end.xml

oscilloscope.xml$(EXEEXT): $(oscilloscope_xml_SOURCES)
	cat $^ > $@


# Need this to prevent the previous (i.e, from parent directory) value
# of SUBDIRS propagating through to the Makefile.in & Makefile

SUBDIRS = nodes oscilloscopeGUI

