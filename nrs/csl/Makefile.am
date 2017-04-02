# $Id: Makefile.am,v 1.2 2004/12/15 16:37:39 darrens Exp $
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

javadir = $(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@/nrs/csl

java_JAVA = CSL_BasicType.class \
	CSL_BasicType_Float.class \
	CSL_Element_Constraint.class \
	CSL_Element_Interface.class \
	CSL_Element_InGroup.class \
	CSL_Element_Message.class \
	CSL_Element_Unit.class \
	CSL_BasicType_Boolean.class \
	CSL_Element_NodeDescription.class \
	CSL_Element_Segment.class \
	CSL_Message_Registry.class \
	CSL_BasicType_String.class \
	CSL_Exception.class \
	CSL_Unit_Registry.class \
	CSL_Element_Contains.class \
	CSL_XMLEventProcessor.class \
	CSL_Element_InitialValue.class \
	CSL_Element_Registry.class \
	CSL_Element_Description.class \
	CSL_Element.class \
	CSL_ContentReader.class \
	CSL_Element_ListMember.class \
	CSL_Element_Attribute.class \
	CSL_Element_Variable.class \
	CSL_Element_Root.class \
	CSL_Node_Registry.class \
	CaseInsensitiveMessageComparator.class \
	CaseInsensitiveNodeComparator.class \
	CaseInsensitiveUnitComparator.class \
	CSL_Element_StringInfo.class \
	CSL_Registry_Utils.class \
        PackageLogger.class \
        CSL_Global_Registry.class \
        CSL_BasicType_Integer.class \
        CSL_BasicType_Route.class \
        ContainmentRule.class \
	CSL_BasicType_Void.class \
	CSL_Element_FloatInfo.class \
	CSL_Element_IntegerInfo.class

# Not sure if this is needed, or what use is made of it. For a hint to
# its use, see the Makefile eventually produced, and search for its
# existence there.
#JAVAROOT = .

# Need this to prevent the previous (i.e, from parent directory) value
# of SUBDIRS propagating through to the Makefile.in & Makefile
SUBDIRS =

SUFFIXES = .java .class

# Specify the source code path to search for class or interface
# definitions. Source path entries are separated by colons (:) and can
# be directories, JAR archives, or ZIP archives.
SOURCEPATH = ${top_srcdir}/@JAVA_GUI_PATH@

# Set the destination directory for class files. Currently I want the
# class files to be placed in the gui directory, which we can locate via
# the 'top_builddir' variable
DESTPATH = ${top_builddir}/@JAVA_GUI_PATH@


%.class: %.java
	@JAVAC@ -d $(DESTPATH) -sourcepath $(SOURCEPATH) $<

classjava.stamp: $(java_JAVA)
	echo timestamp > classjava.stamp
