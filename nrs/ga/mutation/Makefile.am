# $Id: Makefile.am,v 1.2 2005/02/02 22:57:35 s0125563 Exp $
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

javadir = $(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@/nrs/ga/mutation

java_JAVA = MutationNode.class \
	SimpleMutationNode.class \
	GaussianMutationNode.class

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

# Note - this rule uses the -classpath (rather than the -sourcepath)
# javac option, and one of the directories is the ${DESTPATH} (which
# contains the created classes, hence the needed for -classpath rather
# than -sourcepath). The reason -classpath is being provided is because
# this makefile generates a .java file in the destination directory. The
# corresponding class also appears in the destination directory, and
# that class file is the one used during java compilation for
# type-resolution.
%.class : %.java ;
	@JAVAC@ -d $(DESTPATH) -classpath $(SOURCEPATH):${DESTPATH} $<

classjava.stamp: $(java_JAVA)
	echo timestamp > classjava.stamp
