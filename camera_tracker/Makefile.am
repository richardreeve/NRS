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

bin_PROGRAMS = NRS.camtrack rescalib
NRS_camtrack_SOURCES = OptionsFile.cc \
	ColourCluster.cc ColourClusterSet.cc \
	FixedColourClusterSet.cc ColourClusterSetLoader.cc \
	ConvMask.cc ConvMaskManager.cc \
	ProcessedImage.cc ClusteringImageProcessor.cc \
	CameraProjection.cc \
	Robot.cc RobotManager.cc \
	PostTrackProcessor.cc \
	Timer.cc Tracker.cc \
	Configuration.cc \
	camtrack.cc
NRS_camtrack_LDFLAGS = $(OPENCV_LIBRARY) $(CCVT_LIBRARY) \
	 -L../camera -L../Base -L../Type -L../Unit -L../Message -lWebcam \
	-lopencv -lhighgui -lccvt -lBase -lTypes -lUnits -lMessages -lDummyPlugin
rescalib_SOURCES = ColourCluster.cc ColourClusterSet.cc \
	FixedColourClusterSet.cc ColourClusterSetLoader.cc \
	ConvMask.cc ConvMaskManager.cc \
	ProcessedImage.cc ClusteringImageProcessor.cc \
	rescalib.cc
rescalib_LDFLAGS = $(OPENCV_LIBRARY) $(CCVT_LIBRARY) \
	 -L../camera -L../Base -L../Type -L../Unit -L../Message -lWebcam \
	-lopencv -lhighgui -lccvt -lBase -lTypes -lUnits -lMessages -lDummyPlugin
AM_CPPFLAGS = -I$(top_srcdir)/include $(OPENCV_INCLUDE) $(CCVT_INCLUDE)
SUBDIRS =
