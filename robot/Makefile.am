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

#begin COMMON

csldir = $(prefix)/share/$(PACKAGE)-$(VERSION)/csl

csl_DATA = Khepera2.xml Atmel.xml

INCLUDES = -I$(top_srcdir)/include
SUBDIRS = atmel_csl kh2_csl common_csl

SUFFIXES = .srec .hex .xml

EXTRA_PROGRAMS = component.hex component.elf main.elf main.srec

noinst_PROGRAMS = @AVRBUILD@ @ROBOTBUILD@ Khepera2.xml Atmel.xml

#generate csl including all nodes
Khepera2_xml_SOURCES = common_csl/preamble.xml \
	kh2_csl/message/kh2.xml kh2_csl/nodeDescription/kh2.xml \
	kh2_csl/message/led.xml kh2_csl/nodeDescription/led.xml \
	kh2_csl/message/Motor.xml kh2_csl/nodeDescription/Motor.xml \
	kh2_csl/message/ir.xml kh2_csl/nodeDescription/ir.xml \
	kh2_csl/message/Odometer.xml kh2_csl/nodeDescription/Odometer.xml \
	kh2_csl/message/Earsys.xml kh2_csl/nodeDescription/Earsys.xml \
	common_csl/message/Integrator.xml common_csl/nodeDescription/Integrator.xml \
	common_csl/message/Bandpass.xml common_csl/nodeDescription/Bandpass.xml \
	common_csl/message/Delta.xml common_csl/nodeDescription/Delta.xml \
	common_csl/end.xml

Khepera2.xml$(EXEEXT): $(Khepera2_xml_SOURCES)
	cat $^ > $@

Atmel_xml_SOURCES = common_csl/preamble.xml \
	atmel_csl/message/Atmel.xml atmel_csl/nodeDescription/Atmel.xml \
	atmel_csl/message/LED.xml atmel_csl/nodeDescription/LED.xml \
	atmel_csl/message/ADC.xml atmel_csl/nodeDescription/ADC.xml \
	atmel_csl/message/Optomotor.xml atmel_csl/nodeDescription/Optomotor.xml \
	atmel_csl/message/Stepper.xml atmel_csl/nodeDescription/Stepper.xml \
	atmel_csl/message/Servo.xml atmel_csl/nodeDescription/Servo.xml \
	common_csl/message/Integrator.xml common_csl/nodeDescription/Integrator.xml \
	common_csl/message/Bandpass.xml common_csl/nodeDescription/Bandpass.xml \
	common_csl/message/Delta.xml common_csl/nodeDescription/Delta.xml \
	common_csl/message/OffsetGain.xml common_csl/nodeDescription/OffsetGain.xml \
	common_csl/end.xml

Atmel.xml$(EXEEXT): $(Atmel_xml_SOURCES)
	cat $^ > $@

#end COMMON

# begin AVR stuff

# Debugging format.
# Native formats for AVR-GCC's -g are stabs [default], or dwarf-2.
# AVR (extended) COFF requires stabs, plus an avr-objcopy run.
DEBUG = stabs

# Minimalistic printf version
PRINTF_LIB_MIN = -Wl,-u,vfprintf -lprintf_min

# Floating point printf version (requires MATH_LIB = -lm below)
PRINTF_LIB_FLOAT = -Wl,-u,vfprintf -lprintf_flt

PRINTF_LIB = 

# Minimalistic scanf version
SCANF_LIB_MIN = -Wl,-u,vfscanf -lscanf_min

# Floating point + %[ scanf version (requires MATH_LIB = -lm below)
SCANF_LIB_FLOAT = -Wl,-u,vfscanf -lscanf_flt

SCANF_LIB = 

# External memory options

# 64 KB of external RAM, starting after internal RAM (ATmega128!),
# used for variables (.data/.bss) and heap (malloc()).
#EXTMEMOPTS = -Wl,-Tdata=0x801100,--defsym=__heap_end=0x80ffff

# 64 KB of external RAM, starting after internal RAM (ATmega128!),
# only used for heap (malloc()).
#EXTMEMOPTS = -Wl,--defsym=__heap_start=0x801100,--defsym=__heap_end=0x80ffff

EXTMEMOPTS =

# robot installation directory
avrdir = $(prefix)/share/$(PACKAGE)-$(VERSION)/avr

avr_DATA = @AVREXE@

component_elf_SOURCES = main.c nrscomm.c comm.c creates.c nrstools.c \
	tools.c queries.c memory.c link.c led.c adc.c atmel.c timer.c stepper.c \
	servo.c integrator.c bandpass.c delta.c optomotor.c offsetgain.c

component_hex_SOURCES = component.elf

component_elf_LDFLAGS = -Wl,-Map=component.map,--cref $(EXTMEMOPTS) \
			$(PRINTF_LIB) $(SCANF_LIB)

OBJCOPY = avr-objcopy

component.hex$(EXEEXT): component.elf
	@AVROBJCOPY@ @AVROBJCOPYFLAGS@ $< $@


#end AVR stuff

#begin KTEAM stuff

# robot installation directory
robotdir = $(prefix)/share/$(PACKAGE)-$(VERSION)/@ROBOTDIR@

#noinst_PROGRAMS = braiten.elf braiten.srec robot1.elf robot1.srec main.elf main.srec

#robot_DATA = braiten.srec robot1.srec main.srec
robot_DATA = @ROBOTEXE@

#braiten_srec_SOURCES = braiten.elf$(EXEEXT)

#braiten_elf_LDFLAGS = @ROBOTLDFLAGS@
#braiten_elf_LDADD = @ROBOTLIBS@
#braiten_elf_SOURCES = braiten.c

AM_CFLAGS = -std=gnu99  

#braiten.srec$(EXEEXT): braiten.elf$(EXEEXT)
#	@ROBOTOBJCOPY@ @ROBOTOBJCOPYFLAGS@ $< $@

main_srec_SOURCES = main.elf$(EXEEXT)

main_elf_LDFLAGS = @ROBOTLDFLAGS@
main_elf_LDADD = @ROBOTLIBS@ -lm

# comm debugging:
#main_elf_SOURCES = main.c comm.c tools.c 

# all files:
main_elf_SOURCES = main.c nrscomm.c comm.c creates.c nrstools.c tools.c queries.c memory.c link.c \
		   kh2.c led.c motor.c ir.c odometer.c ears/ears.c ears/ears_khepera2.c integrator.c \
		   bandpass.c earsys.c delta.c

main.srec$(EXEEXT): main.elf$(EXEEXT)
	@ROBOTOBJCOPY@ @ROBOTOBJCOPYFLAGS@ $< $@


#robot1_srec_SOURCES = robot1.elf$(EXEEXT)

#robot1_elf_LDFLAGS = @ROBOTLDFLAGS@
#robot1_elf_LDADD = @ROBOTLIBS@
#robot1_elf_SOURCES = robot1.c

#robot1.srec$(EXEEXT): robot1.elf$(EXEEXT)
#	@ROBOTOBJCOPY@ @ROBOTOBJCOPYFLAGS@ $< $@

#end KTEAM stuff
