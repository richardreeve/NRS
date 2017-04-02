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
plugin_LTLIBRARIES = nsim.la
nsim_la_SOURCES = NSim.cc NSimFactory.cc NSimManager.cc RealTimeError.cc \
	SimTime.cc Timer.cc TimerManager.cc TimerFactory.cc		 \
	FullSpeedTimerManager.cc FullSpeedTimerFactory.cc		 \
	NetworkFactory.cc NetworkManager.cc Network.cc			 \
	TimeCheckManager.cc TimeCheckFactory.cc	                 	 \
	TimeCheck.cc TimeChecker.cc Spike.cc SpikeManager.cc		 \
	PoissonNeuron.cc PoissonNeuronManager.cc PoissonNeuronFactory.cc \
	PoissonNeuronSpike.cc						 \
	Sum.cc Counter.cc CounterManager.cc CounterFactory.cc		 \
	Evaluation.cc Evaluator.cc EvaluatorManager.cc			 \
	EvaluatorFactory.cc SpikingNeuron.cc SpikingNeuronFactory.cc	 \
	SummedCurrent.cc						 \
	SpikingNeuronManager.cc NSimLibrary.cc	SpikingNeuronVActual.cc	 \
	SpikingNeuronGTotal.cc SpikingNeuronITotal.cc 			 \
	SpikingNeuronSpike.cc ConductanceBasedSynapse.cc		 \
	ConductanceBasedSynapseManager.cc ConductanceBasedSynapseFactory.cc

#	ReversibleConductance.cc ReversibleConductanceManager.cc \
# 	Axon.cc AxonManager.cc AxonFactory.cc AxonSpike.cc \
# 	ConductanceBasedSynapseGActual.cc \
# 	ConductanceBasedSynapseOutput.cc \
# 	PeriodicTrigger.cc PeriodicTriggerFactory.cc \
# 	PeriodicTriggerManager.cc Event.cc EventManager.cc \
# 	PeriodicTriggerEvent.cc \
# 	BooleanEnvelopeManager.cc BooleanEnvelopeFactory.cc \
# 	BooleanEnvelope.cc BooleanEnvelopeBoolean.cc

nsim_la_LDFLAGS = -module -avoid-version
AM_CPPFLAGS = -I$(top_srcdir)/include
SUBDIRS =


