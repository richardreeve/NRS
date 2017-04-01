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

message_files = csl/message/base/AddCalcNode.xml \
	csl/message/base/BooleanValueNode.xml \
	csl/message/base/DummyValueNode.xml \
	csl/message/base/EvaluationNode.xml \
	csl/message/base/FloatValueNode.xml \
	csl/message/base/FunctionCalcNode.xml \
	csl/message/base/GANode.xml \
	csl/message/base/FloatGeneNode.xml \
	csl/message/base/IntGeneNode.xml \
	csl/message/base/GenomeNode.xml \
	csl/message/base/IntegerValueNode.xml \
	csl/message/crossover/NPointCrossoverNode.xml \
	csl/message/crossover/OnePointCrossoverNode.xml \
	csl/message/crossover/RandomMultiPointCrossoverNode.xml \
	csl/message/crossover/TwoPointCrossoverNode.xml \
	csl/message/crossover/UniformCrossoverNode.xml \
	csl/message/mutation/GaussianMutationNode.xml \
	csl/message/mutation/SimpleMutationNode.xml \
	csl/message/population/GenerationalPopNode.xml \
	csl/message/population/SteadyStatePopNode.xml \
	csl/message/replacement/RandomReplacementNode.xml \
	csl/message/replacement/WorstReplacementNode.xml \
	csl/message/rng/UniformRNGNode.xml \
	csl/message/selection/RouletteWheelSelectionNode.xml \
	csl/message/selection/TournamentSelectionNode.xml

csl_DATA = ga.xml \
	${message_files}

noinst_PROGRAMS = ga.xml

# concatenate xml sources files to create one xml file.
ga_xml_SOURCES = csl/preamble.xml ${message_files} \
	csl/nodeDescription/base/AddCalcNode.xml \
	csl/nodeDescription/base/BooleanValueNode.xml \
	csl/nodeDescription/base/DummyValueNode.xml \
	csl/nodeDescription/base/EvaluationNode.xml \
	csl/nodeDescription/base/FloatValueNode.xml \
	csl/nodeDescription/base/FunctionCalcNode.xml \
	csl/nodeDescription/base/GANode.xml \
	csl/nodeDescription/base/FloatGeneNode.xml \
	csl/nodeDescription/base/IntGeneNode.xml \
	csl/nodeDescription/base/GenomeNode.xml \
	csl/nodeDescription/base/IntegerValueNode.xml \
	csl/nodeDescription/crossover/NPointCrossoverNode.xml \
	csl/nodeDescription/crossover/OnePointCrossoverNode.xml \
	csl/nodeDescription/crossover/RandomMultiPointCrossoverNode.xml \
	csl/nodeDescription/crossover/TwoPointCrossoverNode.xml \
	csl/nodeDescription/crossover/UniformCrossoverNode.xml \
	csl/nodeDescription/mutation/GaussianMutationNode.xml \
	csl/nodeDescription/mutation/SimpleMutationNode.xml \
	csl/nodeDescription/population/GenerationalPopNode.xml \
	csl/nodeDescription/population/SteadyStatePopNode.xml \
	csl/nodeDescription/replacement/RandomReplacementNode.xml \
	csl/nodeDescription/replacement/WorstReplacementNode.xml \
	csl/nodeDescription/rng/UniformRNGNode.xml \
	csl/nodeDescription/selection/RouletteWheelSelectionNode.xml \
	csl/nodeDescription/selection/TournamentSelectionNode.xml \
	csl/end.xml

ga.xml$(EXEEXT): $(ga_xml_SOURCES)
	cat $^ > $@

SUBDIRS = base crossover mutation population rng selection replacement
