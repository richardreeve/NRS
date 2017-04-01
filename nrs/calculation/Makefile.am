# $Id: Makefile.am,v 1.14 2005/08/12 17:16:31 SCope Exp $
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

csldir = $(prefix)/share/$(PACKAGE)-$(VERSION)/csl

message_files = csl/message/CalculationNode.xml \
	csl/message/FloatAddNode.xml \
	csl/message/IntAddNode.xml \
	csl/message/FloatSubtractNode.xml \
	csl/message/IntSubtractNode.xml \
	csl/message/FloatDivideNode.xml \
	csl/message/IntDivideNode.xml \
	csl/message/FloatMultiplyNode.xml \
	csl/message/IntMultiplyNode.xml \
	csl/message/ANDBooleanNode.xml \
	csl/message/ORBooleanNode.xml \
	csl/message/XORBooleanNode.xml \
	csl/message/NOTNode.xml \
	csl/message/DeltaNode.xml \
	csl/message/IntForLoopNode.xml \
	csl/message/FloatForLoopNode.xml \
	csl/message/IntLatchNode.xml \
	csl/message/FloatLatchNode.xml \
	csl/message/BooleanLatchNode.xml \
	csl/message/VoidLatchNode.xml \
	csl/message/IntDelayNode.xml \
	csl/message/FloatDelayNode.xml \
	csl/message/BooleanDelayNode.xml \
	csl/message/VoidDelayNode.xml \
	csl/message/EqualsIntCondNode.xml \
	csl/message/LessThanIntCondNode.xml \
	csl/message/EqualsFloatCondNode.xml \
	csl/message/LessThanFloatCondNode.xml \
	csl/message/FloatToIntConversionNode.xml \
	csl/message/FloatToStringConversionNode.xml \
	csl/message/FloatToBooleanConversionNode.xml \
	csl/message/FloatToTimeConversionNode.xml \
	csl/message/FloatToCapacitConversionNode.xml \
	csl/message/FloatToVoltageConversionNode.xml \
	csl/message/FloatToConductConversionNode.xml \
	csl/message/FloatToCurrentConversionNode.xml \
	csl/message/FloatToFreqConversionNode.xml \
	csl/message/BooleanToIntConversionNode.xml \
	csl/message/BooleanToStringConversionNode.xml \
	csl/message/BooleanToFloatConversionNode.xml \
	csl/message/BooleanToVoidConversionNode.xml \
	csl/message/StringConcatNode.xml \
	csl/message/IntToFloatConversionNode.xml \
	csl/message/IntToStringConversionNode.xml \
	csl/message/IntToBooleanConversionNode.xml \
	csl/message/VoidToBooleanNode.xml \
	csl/message/VoidToBooleanDelayNode.xml \
	csl/message/StringToFilenameConversionNode.xml 

csl_DATA = calculation.xml \
	$(message_files)

noinst_PROGRAMS = calculation.xml

# concatenate xml sources files to create one xml file.
calculation_xml_SOURCES = csl/preamble.xml \
	$(message_files) \
	csl/nodeDescription/CalculationNode.xml \
	csl/nodeDescription/FloatAddNode.xml \
	csl/nodeDescription/IntAddNode.xml \
	csl/nodeDescription/FloatSubtractNode.xml \
	csl/nodeDescription/IntSubtractNode.xml \
	csl/nodeDescription/FloatDivideNode.xml \
	csl/nodeDescription/IntDivideNode.xml \
	csl/nodeDescription/FloatMultiplyNode.xml \
	csl/nodeDescription/IntMultiplyNode.xml \
	csl/nodeDescription/ANDBooleanNode.xml \
	csl/nodeDescription/ORBooleanNode.xml \
	csl/nodeDescription/XORBooleanNode.xml \
	csl/nodeDescription/NOTNode.xml \
	csl/nodeDescription/DeltaNode.xml \
	csl/nodeDescription/IntForLoopNode.xml \
	csl/nodeDescription/FloatForLoopNode.xml \
	csl/nodeDescription/IntLatchNode.xml \
	csl/nodeDescription/FloatLatchNode.xml \
	csl/nodeDescription/BooleanLatchNode.xml \
	csl/nodeDescription/VoidLatchNode.xml \
	csl/nodeDescription/IntDelayNode.xml \
	csl/nodeDescription/FloatDelayNode.xml \
	csl/nodeDescription/BooleanDelayNode.xml \
	csl/nodeDescription/VoidDelayNode.xml \
	csl/nodeDescription/EqualsIntCondNode.xml \
	csl/nodeDescription/LessThanIntCondNode.xml \
	csl/nodeDescription/EqualsFloatCondNode.xml \
	csl/nodeDescription/LessThanFloatCondNode.xml \
	csl/nodeDescription/FloatToIntConversionNode.xml \
	csl/nodeDescription/FloatToStringConversionNode.xml \
	csl/nodeDescription/FloatToBooleanConversionNode.xml \
	csl/nodeDescription/FloatToTimeConversionNode.xml \
	csl/nodeDescription/FloatToConductConversionNode.xml \
	csl/nodeDescription/FloatToCapacitConversionNode.xml \
	csl/nodeDescription/FloatToVoltageConversionNode.xml \
	csl/nodeDescription/FloatToCurrentConversionNode.xml \
	csl/nodeDescription/FloatToFreqConversionNode.xml \
	csl/nodeDescription/BooleanToIntConversionNode.xml \
	csl/nodeDescription/BooleanToStringConversionNode.xml \
	csl/nodeDescription/BooleanToFloatConversionNode.xml \
	csl/nodeDescription/BooleanToVoidConversionNode.xml \
	csl/nodeDescription/StringConcatNode.xml \
	csl/nodeDescription/IntToFloatConversionNode.xml \
	csl/nodeDescription/IntToStringConversionNode.xml \
	csl/nodeDescription/IntToBooleanConversionNode.xml \
	csl/nodeDescription/VoidToBooleanNode.xml \
	csl/nodeDescription/VoidToBooleanDelayNode.xml \
	csl/nodeDescription/StringToFilenameConversionNode.xml \
	csl/end.xml

calculation.xml$(EXEEXT): $(calculation_xml_SOURCES)
	cat $^ > $@


javadir = $(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@/nrs/calculation

java_JAVA = nrscalculation.class \
	AppManager.class \
	CalculationComponent.class \
	PackageLogger.class \
	CalculationNode.class \
	FloatNode.class	\
	IntNode.class \
	BooleanNode.class \
	NOTNode.class \
	DeltaNode.class \
	ForLoopNode.class \
	FloatCondNode.class \
	IntCondNode.class \
	LatchNode.class \
	FloatConversionNode.class \
	StringConcatNode.class \
	StringConversionNode.class \
	IntConversionNode.class \
	VoidToBooleanNode.class \
	BooleanConversionNode.class \
	VoidToBooleanDelayNode.class \
	DelayNode.class

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
