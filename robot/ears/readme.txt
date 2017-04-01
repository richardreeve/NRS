CRICKET EARS SYSTEM SOFTWARE:  PIC1 SOFTWARE.
Written by John Hallam and Richard Reeve

This directory contains the current debugged version of the ears PIC
code. There are now five versions of the file owncode\pic1-own.asm.
The main version which will build normally is in owncode\ with a copy
in calibration\ allows saving of data in EEPROM, switching ear delays
and increased time delays. change_times\ does not have the EEPROM
code, switch_ears\ just allows you to switch between 2 and 4 ear mode
using mode commands described below, but does not allow the extended
range of delays. In the directory two_ears\ is a version which only
allows two ears, and in four_ears\ is a version which only allows four
ears. Obviously in most of these codes various of the mode commands do
not work.

Ear summary
-----------

For the simple two ear model, we imagine the ears are 1/4 wavelength
apart, and the delay to be the time it takes to travel 1/4 wavelength,
with the gains +1 and -1. This should give perfect cancellation on
one side, and perfect summation on the other, with them 90 degrees out
of phase straight ahead.

At 4.7kHz, a 1/4 wavelength separation between the ears is (with sound
at 350m/s) sep=350000/(4700*4)=18mm.

To achieve a 1/4 wavelength separation in the synths, we need
delay=1000000/(4700*4)=53.1915 microseconds. The synth measures in
units of 0.625 microseconds, so the delay needs to be set to
val=53.1915/0.625=85.1=0x55.

For the more realistic 4 ear model, the diagram in cricket.png shows
the layout of the microphones (from Michelsen et al. 1994). The same
paper suggests typical amplitudes of 1 for IT, 1.5 for IS and 0.44
for CS. Average delays suggested are -147 degrees for IT (relative to
IS), and +99 degrees for CS (relative to IS). However they then go on
to suggest figures which provide a "nice" directional pattern -154
degrees and 54 degrees respectively, so the latter may be a better
choice of values. These figures are given for a 4.5kHz sound.

At 4.5kHz, -154 degrees -> -154/(4500*360) s = -95.1 us -> -152.1
(0x98) delay units on the pic, and 54 degrees -> 54/(4500*360) s =
33.3 us -> 53.3 (0x35) delay units.

Note: the ANxx multipiers can be all reversed in sign simultaneously,
as the output is the RMS of the sum on the ANxx outputs.

Two ear model:
-------------

One ear on each side, cross connected by AN2s:

   (Left Ear 1) (Right Ear 1)
        |             |      
        |             |      
        v             v      
       PR1L          PR1R    
       gain          gain    
        |             |      
       / \           / \     
      /   \         /   \    
     /     \       /     \   
    v       v     v       v  
  Fixed  Synth1 Synth1  Fixed
   (0)     pos   pos     (0) 
  delay   delay delay   delay
    |        \   /        |  
    |         \ /         |  
    |          x          |  
    |         / \         |  
    |        /   \        |  
    v       v     v       v  
   AN1L    AN2R  AN2L    AN1R
   mult    mult  mult    mult
    |       /      \       | 
    |      /        \      | 
    |     /          \     | 
    |    /            \    | 
    |   /              \   |
    |  /                \  |
    | /                  \ |
    |/                    \|
Left Output           Right Output

Four ear model:
--------------

Two ears on each side, cross connected by AN2s:

(Left Ear 2) (Left Ear 1) (Right Ear 1) (Right Ear 2)
     |            |             |             |
     |            |             |             |
     v            v             v             v
    PR2L         PR1L          PR1R          PR2R
    gain         gain          gain          gain
     |            |             |             |
     |           / \           / \            |
     |          /   \         /   \           |
     |         /     \       /     \          |
     v        v       v     v       v         v
  Synth2    Fixed  Synth1 Synth1  Fixed    Synth2
   delay    delay   delay delay   delay     delay
     |        |        \   /        |         |
     |        |         \ /         |         |
     |        |          x          |         |
     |        |         / \         |         |
     |        |        /   \        |         |
     v        v       v     v       v         v
    AN3L     AN1L    AN2R  AN2L    AN1R      AN3R
    mult     mult    mult  mult    mult      mult
      \       |       /      \       |       /
       \      |      /        \      |      /
        \     |     /          \     |     /
         \    |    /            \    |    /
          \   |   /              \   |   /
           \  |  /                \  |  /
            \ | /                  \ | /
             \|/                    \|/
          Left Output           Right Output

Wiring for the Koala ears is as follows:
---------------------------------------

        Lead no.        Koala                   Ears
        1               Analogue I/P 0          CH3
	2               Analogue I/P 1          CH4
(       3               Analogue I/P 2          CH5 - non-functional reference)
        4               V REF.                  V REF.
        5               CMOS Output 0           D8
	6               CMOS Output 1           D9
	7               CMOS Output 2           D10
	8               CMOS Output 3           D11
	9               +5 Volts.               +5V.
	10              Gnd.                    0 Volts.
	11              Open Collector O/P 0    (Reset)
	12              Open Collector O/P 1    (Pulse)

The leads are numbered 1-12 according to the small seperator strip; the
wire colours are insignificant except Black and Red which are 0 volts and
+5 Volts respectively.

The new ears interface:
----------------------

The ears are driven from the Khepera via a 4-bit write-only interface.
This supplies data to the on-board micro-controller which then programs
the electronics.

For the Koala, the four bits are set in CMOS Outputs 3-0, with Open
Collector O/P 1 pulsed to trigger a read of the bits.

There are 16 possible 4-bit codes that can be written to the
interface, and they have the following meanings:

Hex     Interpretation.

0       Resynchronise interface.  Any pending command is aborted.
1       Write data to register.  This follows the register and the data.
2       Command escape, used to enter data values 0, 1 or 2.

3-F     Data values.

With this scheme an 8-bit datum needs 2.375 writes to transfer, on
average, yet the code can always be resynchronised by writing two
consecutive 0 codes.

Since the microcontroller must be given time to respond, successive
codes should not be written too fast:  leave a delay of 1ms between
codes for now.

The write data to register command code, 1, is preceded by a register
number (and associated data) chosen as follows:

Hex     Interpretation.

0
1
2
3       Extended command set             (4 bits command, 4 bits data)
4       Write delay to synthesiser one   (8 bits data) (*)
5       Write delay to synthesiser two   (8 bits data) (*)
6       Write to Left  Channel, AN3 gain (8 bits data)
7       Write to Right Channel, AN3 gain (8 bits data)
8       Write to Left  Channel, PR1 gain (8 bits data)
9       Write to Left  Channel, AN1 gain (8 bits data)
A       Write to Left  Channel, PR2 gain (8 bits data)
B       Write to Left  Channel, AN2 gain (8 bits data)
C       Write to Right Channel, PR1 gain (8 bits data)
D       Write to Right Channel, AN1 gain (8 bits data)
E       Write to Right Channel, PR2 gain (8 bits data)
F       Write to Right Channel, AN2 gain (8 bits data)

(*) high nibble and sign set in extended command set

Following the register number is the indicated amount of data.

Synthesiser data is the extra delay in microseconds for the synthesised
channel with respect to channel AN1 (synthesiser one does AN2 and
synthesiser two does AN3).  PR? gain is the preamplifier gain for each
channel microphone (with 0..255 mapped into 0..loud) and AN? gain is the
mixing gain for each channel, where the 0..255 range of control is
mapped into [+1,-1] gain multiplier.

The extended command set controls various operations of the system and
the leds. The high nibble of the data code is interpreted thus:

Hex     Interpretation.
0
1
2
3	Set four ear mode                               (switch_ears code only)
4	Reset to power-on defaults
5	Load saved data <val>=[0-3]                  (calibration code onwards)
6	Save current state to saved data <val>=[0-3] (calibration code onwards)
7
8	Set two ear mode                                (switch_ears code only)
9
A
B
C	Set LEDs to <val>
D
E	Set high time delay bits [11:8] to -<val>   (change_times code onwards)
F	Set high time delay bits [11:8] to +<val>   (change_times code onwards)

Note that in switch_ears after changing ear modes, you must actually
change the delays to have an effect - they remain as set until
changed. Likewise changes to time delays in change_times affect only
the next time delay set, and are then reset after it to + and 0
(equivalent to 0xf0).

The low nibble is referred to as <val> above. For saving/loading data,
only the low 2 bits are relevant, as there are only 4 save states. For
the high delay bits, they are multiples of 160us (256 times the base
of .625us).

For the LEDs the nibble <val> encodes what they do. LED1 is on/off if
the high bit is 0/1; LED2 flashes in various ways depending on the
remaining three bits. We use four states to synthesise various flash
patterns. Each state lasts for 256 ticks, and the LED may be on or off
in each state. There are thus six possible patterns of flashing,
determined by bits TT0..TT2 in the Action byte.

TT2..0  S1	  S2	  S3	  S4	Display
0	  Off	  Off	  Off	  Off	Continuous off
1	  On	  Off	  Off	  Off	1:3 on
2	  On	  Off	  Off	  On	1:1 on, slow
3	  On	  Off	  On	  Off	1:1 on, fast
4	  On	  Off	  On	  On	3:1 on
5	  On	  On	  On	  Off	3:1 on
6	  On	  On	  On	  On	Continuous on
7	  On	  On	  On	  Off	3:1 on



Examples:

    To reset the ears to power-up state, send 3 4 1.

    To do that from any state, send 0 0 3 4 1.

    To load saved program 3 into circuit, send 3 7 1.

    To then zero the PR2 gain on the left ear, send A 2 0 2 0 1 -- note that
	the 8 bit quantity 00 is transmitted as 2 0 2 0, the 2 codes being the
	command escapes without which the data would cause the interface
	to abort the command and resynchronise.

    To set LED1 on, send  3 C 8 1.

    To save current program to saved program 1, send 3 9 1.

