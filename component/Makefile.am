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

bin_PROGRAMS = NRS.component 

NRS_component_SOURCES = component.cc
NRS_component_LDADD = ../Type/libTypes.la ../Unit/libUnits.la \
	../Message/libMessages.la ../Base/libBase.la \
	../Message/libMessages.la ../Type/libTypes.la ../Base/libPlugin.la \
	../PML/libPMLInterface.la ../BMF/libBMFInterface.la \
	@DLOPEN@ ../Base/dummy.la

NRS_component_LDFLAGS = -export-dynamic -lltdl -ldl -lxerces-c

AM_CPPFLAGS = -I$(top_srcdir)/include
SUBDIRS =

# INSERT: add new component alias copying one of the links below. The
# suffix of the new name of the component must be exactly the same as
# the name of the shared library (so nsim.so produces NRS.nsim)
install-exec-local:
	ln -f $(DESTDIR)$(prefix)/bin/NRS.component \
	      $(DESTDIR)$(prefix)/bin/NRS.nsim
	ln -f $(DESTDIR)$(prefix)/bin/NRS.component \
	      $(DESTDIR)$(prefix)/bin/NRS.audiogen
	ln -f $(DESTDIR)$(prefix)/bin/NRS.component \
	      $(DESTDIR)$(prefix)/bin/NRS.tracker
	ln -f $(DESTDIR)$(prefix)/bin/NRS.component \
	      $(DESTDIR)$(prefix)/bin/NRS.coordinator



