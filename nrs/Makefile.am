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

SUBDIRS = nrsgui csl toolboxes tracker pml control core audio ga util datalogger composer wrapper calculation sound oscilloscope

bin_PROGRAMS = @JAVA_SCRIPTS@

EXTRA_PROGRAMS = NRS.gui NRS.RobotTrackerGUI NRS.StickTrackerGUI \
	NRS.control NRS.audiosim NRS.ga NRS.datalogger NRS.composer \
	NRS.wrapper NRS.calculation NRS.sound NRS.oscilloscope

NRS_gui_SOURCES =

NRS.gui$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.gui$(EXEEXT)
	echo "@JAVA@ -DNRS_CSL_REPOSITORY=\$$NRS_CSL_REPOSITORY -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.nrsgui.nrsgui \$$*" >> NRS.gui$(EXEEXT)

NRS_RobotTrackerGUI_SOURCES =

NRS.RobotTrackerGUI$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.RobotTrackerGUI$(EXEEXT)
	echo "@JAVA@ -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.tracker.robottrack.trackergui" >> NRS.RobotTrackerGUI$(EXEEXT)

NRS_StickTrackerGUI_SOURCES =

NRS.StickTrackerGUI$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.StickTrackerGUI$(EXEEXT)
	echo "@JAVA@ -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.tracker.stickgui.stickgui" >> NRS.StickTrackerGUI$(EXEEXT)

NRS_control_SOURCES =

NRS.control$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.control$(EXEEXT)
	echo "@JAVA@ -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.control.nrscontrol \$$*" >> NRS.control$(EXEEXT)

NRS_calculation_SOURCES =

NRS.calculation$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.calculation$(EXEEXT)
	echo "@JAVA@ -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.calculation.nrscalculation \$$*" >> NRS.calculation$(EXEEXT)

NRS_sound_SOURCES =

NRS.sound$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.sound$(EXEEXT)
	echo "@JAVA@ -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.sound.nrssound \$$*" >> NRS.sound$(EXEEXT)

NRS_composer_SOURCES =

NRS.composer$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.composer$(EXEEXT)
	echo "@JAVA@ -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.composer.nrscomposer \$$*" >> NRS.composer$(EXEEXT)


NRS_datalogger_SOURCES =

NRS.datalogger$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.datalogger$(EXEEXT)
	echo "@JAVA@ -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.datalogger.nrsdatalogger \$$*" >> NRS.datalogger$(EXEEXT)

NRS_oscilloscope_SOURCES =

NRS.oscilloscope$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.oscilloscope$(EXEEXT)
	echo "@JAVA@ -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.oscilloscope.nodes.nrsoscilloscope \$$*" >> NRS.oscilloscope$(EXEEXT) 

NRS_audiosim_SOURCES =

NRS.audiosim$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.audiosim$(EXEEXT)
	echo "@JAVA@ -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.audio.simulator.DSFrame" >> NRS.audiosim$(EXEEXT)

NRS_ga_SOURCES =

NRS.ga$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.ga$(EXEEXT)
	echo "@JAVA@ -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.ga.base.nrsga \$$*" >> NRS.ga$(EXEEXT)

NRS_wrapper_SOURCES =

NRS.wrapper$(EXEEXT): Makefile
	echo "#!/bin/sh" > NRS.wrapper$(EXEEXT)
	echo "@JAVA@ -cp @CLASSPATH@:$(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@ nrs.wrapper.nrswrapper \$$*" >> NRS.wrapper$(EXEEXT)

if JAVADOC_ACTIVE

htmldir = $(prefix)/share/$(PACKAGE)-$(VERSION)/html/javadoc

noinst_PROGRAMS = doc.timestamp

doc_timestamp_SOURCES = $(shell find ${srcdir}/.. -name "*.java")

# Options for controlling the javadoc output. Refer to the tool
# documentation supplied by Sun.

JAVAD_OPTIONS = -version -author -package -quiet -use -doctitle "NRS 2" -header "<b>NRS 2</b>" -linkoffline "http://java.sun.com/j2se/1.4.2/docs/api" ${srcdir}/..

# export INSTALL_DATA=${INSTALL} -m 644 -d

# Now using javadoc by specifying the subpackages, which will
# recursively include all of the java files under the nrs
# directory. There is still a problem though in that the javadoc is
# notrebuild when java files two levels deep are modified.
doc.timestamp$(EXEEXT): $(doc_timestamp_SOURCES)
	@JAVAD@ ${JAVAD_OPTIONS} -d doc -subpackages nrs -sourcepath ${srcdir}/.. -exclude nrs.audio -exclude nrs.skeleton
	touch $@

install-data-local:
	mkdir -p $(htmldir)
	cp -r doc/* $(htmldir)

endif
