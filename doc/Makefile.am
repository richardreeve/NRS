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

if DOCS_ACTIVE

pdfdir = $(prefix)/share/$(PACKAGE)-$(VERSION)/doc

SUFFIXES = .pdf .tex .eps .dia
# dummy targets to build documentation
noinst_PROGRAMS = images doc

# put all primary documentation here - these are the files to be installed
doc_SOURCES = NRS.pdf NRSga.pdf NRSwrapper.pdf NRSdatalogger.pdf \
	NRScplusplus.pdf

#put all secondary documentation (eg images included in primary docs) here
images_SOURCES = global_var.pdf NRS-Sim.pdf example-sim.pdf \
	node-internals.pdf bmf_message.pdf route_building.pdf \
	route_striping.pdf nrsgui_component_classes.pdf \
	nrsgui_basecompnode_internal.pdf \
	java_fifo_to_message.pdf java_comms_classes.pdf \
	java_inbound_routing.pdf ga_NRSga.pdf ga_hierarchy.pdf \
	ga_prog_flow.pdf

BUILT_SOURCES = $(doc_SOURCES) $(images_SOURCES)

pdf_DATA = $(doc_SOURCES)

# add any necessary new rules here to build documentation

%.eps: %.dia
	@DIA@ -e $@ $<

%.pdf: %.eps
	@EPSTOPDF@ $< --outfile=$@

%.pdf: %.tex %.bbl
	@PDFLATEX@ $<
	@PDFLATEX@ $<

%.pdf: %.tex %.nobib
	@PDFLATEX@ $<
	@PDFLATEX@ $<

%.bbl: %.aux $(srcdir)/%.bib
	if [ ! $(srcdir)/$*.bib -ef $*.bib ]; then cp $(srcdir)/$*.bib .; fi
	@BIBTEX@ $*

%.aux: %.tex $(images_SOURCES)
	@PDFLATEX@ $<

# this is necessary for the build to work

doc$(EXEEXT): $(doc_SOURCES)
	touch doc$(EXEEXT)

images$(EXEEXT): $(images_SOURCES)
	touch images$(EXEEXT)

endif
