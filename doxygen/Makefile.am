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

SUBDIRS =

if DOXYGEN_ACTIVE

htmldir = $(prefix)/share/$(PACKAGE)-$(VERSION)/html/doxygen

pdfdir = $(prefix)/share/$(PACKAGE)-$(VERSION)/doc

noinst_PROGRAMS = doc.timestamp

doc_timestamp_SOURCES = $(filter-out ${top_srcdir}/camera_tracker/%	\
	${top_srcdir}/camera/%,						\
	$(wildcard ${top_srcdir}/*/*.hh ${top_srcdir}/*/*.cc))		\
	${srcdir}/Doxyfile

doc.timestamp$(EXEEXT): $(doc_timestamp_SOURCES)
	cp -a $^ .
	@DOXYGEN@
	cd doc/latex; make; cd ../..
	touch $@

install-data-local:
	mkdir -p $(htmldir)
	cp -a doc/html/ $(htmldir)/
	mkdir -p $(pdfdir)
	cp -r doc/latex/refman.pdf $(pdfdir)
	mkdir -p $(mandir)
	cp -r doc/man/* $(mandir)

endif