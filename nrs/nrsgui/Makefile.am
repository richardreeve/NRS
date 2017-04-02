# $Id: Makefile.am,v 1.8 2005/04/17 18:08:25 darrens Exp $
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

javadir = $(prefix)/share/$(PACKAGE)-$(VERSION)/@JAVA_GUI_PATH@/nrs/nrsgui

java_JAVA = NRSDefaultDirectories.class \
	NRSCSLRepository.class \
	nrsgui.class \
	MainFrame.class \
	AppManager.class \
	NetworkCanvas.class \
	NetworkBrowser.class \
	NetworkEventsListener.class \
	NetworkObjectPin.class \
	RootNetwork.class \
	NodeAttribute.class \
	AccessDelegate.class \
	NetworkBrowserPopupMenu.class \
	Node.class \
	DisplayDelegate.class \
	DefaultNode.class \
        NodeCollection.class \
        DefaultNodeCollection.class \
        PML_Exporter.class \
	GuiNode.class \
	NetworkCanvasDataContext.class \
	VariableCollection.class \
	DefaultVariableCollection.class \
	NodeVariable.class \
	GuiVariable.class \
	Link.class \
	LinkManager.class \
	NodeLinkMakerWindow.class \
	LinkManagerListener.class \
	NLinkList.class \
	NLinkControl.class \
	IconManager.class \
	PMLOutputStream.class \
	PackageLogger.class \
        BaseNetworkBrowser.class \
	RootNetworkBrowser.class \
	AttributeEditorWrapper.class \
	RootNetworkBrowserPopupMenu.class \
	NRSGUIComponentNode.class \
	NRSComponentManager.class \
	NRSComponent.class \
	BrowserPopupMenu.class \
	Programmer.class \
	NodeManager.class \
	UserVN.class \
	RemoteVNInfo.class \
	GuiPin.class \
	ConfigParameter.class \
	ConfigParameterBoolean.class \
	Settings.class \
	NRSGUIConstants.class \
	NetworkBrowserStatusBar.class \
	Resources.class \
	BrowserManager.class \
	NodeListener.class \
	ValidateNodeName.class \
	NetworkParcel.class \
	FileStorageManager.class \
	ParcelXMLExporter.class \
	NetworkInfoForm.class \
	Network.class \
	NetworkXMLLoader.class \
	SharedXMLResources.class \
	BrowserContext.class \
	VariableListener.class \
	CreateLinkJob.class \
	DeleteLinkJob.class \
	NodeController.class \
	ModelEventListener.class \
	NodeModel.class \
	NRSGUITree.class \
	CustomTreeRenderer.class \
	CreateNodeJob.class \
	DeleteNodeJob.class

BUILT_SOURCES = NRSDefaultDirectories.java \
	NRSCSLRepository.java

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



NRSDefaultDirectories.java: Makefile
	echo "package nrs.nrsgui;" > NRSDefaultDirectories.java
	echo "public class NRSDefaultDirectories" >> NRSDefaultDirectories.java
	echo "{" >> NRSDefaultDirectories.java
	echo "public final static String EXAMPLE_DIR = \"$(prefix)/share/$(PACKAGE)-$(VERSION)/example_xml\";" >> NRSDefaultDirectories.java
	echo "}" >> NRSDefaultDirectories.java

NRSCSLRepository.java: Makefile
	echo "package nrs.nrsgui;" > NRSCSLRepository.java
	echo "public class NRSCSLRepository" >> NRSCSLRepository.java
	echo "{" >> NRSCSLRepository.java
	echo "public final static String CSL_DIR = \"$(prefix)/share/$(PACKAGE)-$(VERSION)/csl\";" >> NRSCSLRepository.java
	echo "}" >> NRSCSLRepository.java

# Note - this rule uses the -classpath (rather than the -sourcepath)
# javac option, and one of the directories is the ${DESTPATH} (which
# contains the created classes, hence the needed for -classpath rather
# than -sourcepath). The reason -classpath is being provided is because
# this makefile generates a .java file in the destination directory. The
# corresponding class also appears in the destination directory, and
# that class file is the one used during java compilation for
# type-resolution.
%.class: %.java
	@JAVAC@ -d $(DESTPATH) -classpath $(SOURCEPATH):${DESTPATH} $<

classjava.stamp: $(java_JAVA)
	echo timestamp > classjava.stamp
