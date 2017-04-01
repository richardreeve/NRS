NRS.sound

Types of Node :

ToneNode: A Tone has 6 variables: Amplitude, Frequency, WaveFormType,
SecsOn, SecsOff, OffFirst. The first three variables are used to 
change parameters of the tone played. WaveformType is a String variable
which can have the values sine, square, triangle and sawtooth. Variable
SecsOn specifies how many seconds the tone plays for, and SecOff
specifies how many seconds the tone is off for. The actual timing when
the tone is played may not be accurate - i.e. it can be out by between 
20 and 100ms. If the boolean variable OffFirst is set to true, then the
ToneNode will play silence for the value of SecsOff first, followed by
the tone for the value of SecsOn, rather than the other way around. 

SpeakerNode: A SpeakerNode has 2 variables - Balance and ToneOrdering,
Balance is used to specify the balance between two speakers and can take
any value between 1.0 & -1.0 (1.0 for the right speaker, -1.0 for the
left speaker). The String variable ToneOrdering has an Input and an
Output. The Output can be used to display the current order of ToneNodes
in the SpeakerNode, and the Input can be used to change the ordering of 
the ToneNodes. This ordering reflects the order in which the ToneNodes
will be played. NRS.sound is designed so that you cannot directly link 
ToneNodes into the order you want using variables, as sending messages 
between the nodes further compromises the timing of the tones. 

ContinuousSpeaker: A ContinuousSpeaker has 1 variable -
Balance (explained above). This Node can only contain one ToneNode, 
which has no SecsOn, SecsOff or OffFirst variables, and it will play
the tone specifed by the ToneNode continously until the Enabled
variable of the SoundNode is set to false (see below).

SoundNode: The SoundNode is the root node for the NRS.sound component.
It has 3 variables - boolean variables Enabled, and Repeating, and void
variable Go. Sending a void message to Go will start all SpeakerNodes
playing their sequence of Tones at the same time, as well as starting
the ContinuousSpeakers. If the Repeating variable is set to true, then
once the last SpeakerNode has finished its sequence, the SoundNode will
start all of them again. This variable does not affect any 
ContinuousSpeakers in the SoundNode - these will keep playing until the 
SoundNode enabled variable is set to false again. 

Three examples are given in the examples folder
(local_nrs/nrs2/examples) - ToneExample.net, Repeating2Tones.net
and ChangeToneOrder.net.ToneExample demonstrates changing the tone
played by a ToneNode, Repeating2Tones demonstrates playing tones
out of different speakers in sequence, and playing continuous tones
and ChangeToneOrder demonstrates altering the order of the tones in a 
SpeakerNode.These all require NRS.gui, NRS.control and 
NRS.sound to be running when they are opened. 