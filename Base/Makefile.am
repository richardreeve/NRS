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

plugindir = $(prefix)/share/$(PACKAGE)-$(VERSION)/plugins
plugin_LTLIBRARIES = dummy.la
lib_LTLIBRARIES = libBase.la libPlugin.la libDummyPlugin.la
libDummyPlugin_la_SOURCES = DummyPluginDirector.cc
#libDummyPlugin_la_LDFLAGS = -static
libPlugin_la_SOURCES = Plugin.cc PluginDirector.cc
#libPlugin_la_LDFLAGS = -static
libBase_la_SOURCES = SegmentDescription.cc VariableManager.cc		\
	VariableNodeDirector.cc Observable.cc Location.cc Message.cc	\
	Segment.cc ArgumentDirector.cc Restriction.cc Limit.cc		\
	CallbackDirector.cc ExternalInterfaceDirector.cc		\
	ExternalInterface.cc Node.cc NodeFactory.cc Variable.cc		\
	Exception.cc StringLiterals.cc ExternalInterfaceHandler.cc	\
	ArgumentFielder.cc Callback.cc Link.cc MessageStore.cc		\
	Encoding.cc MessageParser.cc BufferedInterface.cc		\
	FileInterface.cc IntelligentStore.cc Executive.cc Target.cc	\
	MessageDescription.cc CSLVariableManager.cc CSLParser.cc	\
	MessageFunctionObject.cc SpecialisedMFO.cc

dummy_la_SOURCES = DummyLibrary.cc
dummy_la_LDFLAGS = -module -avoid-version

#libBase_la_LDFLAGS = -static
AM_CPPFLAGS= -I$(top_srcdir)/include $(STD_CPPFLAGS)
SUBDIRS =
