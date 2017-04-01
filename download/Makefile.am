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

bin_PROGRAMS = NRS.download NRS.genBMF
NRS_download_SOURCES = download.cc
NRS_download_LDADD = -L../Base -L../Type -L../Unit -L../Message \
	-lBase -lTypes -lUnits -lMessages -lDummyPlugin -lxerces-c
NRS_download_LDFLAGS = -export-dynamic -lltdl -ldl -lxerces-c

NRS_genBMF_SOURCES = gen_binary.cc
NRS_genBMF_LDADD = -L../Base -L../Type -L../Unit -L../Message \
	-lBase -lTypes -lUnits -lMessages -lDummyPlugin -lxerces-c
NRS_genBMF_LDFLAGS = -export-dynamic -lltdl -ldl -lxerces-c

INCLUDES = -I$(top_srcdir)/include
SUBDIRS =
