// $Id: ears.c,v 1.2 2005/07/13 17:52:58 s0459419 Exp $

/*
 * Author: Richard Reeve
 *
 * Goal:   This program is the interface to the ears
 *         circuitry. There is no robot specific code here -
 *         send_4_raw, read_ear_out and some #defines are in ears_{robot}.c
 *         ---
 *         See ears.h for the programming interface.
 */

#include 	<stdlib.h>
#include 	<stdio.h>
#include "ears.h"

// Basic commands for ears
#define K_RESYNC 0x0
#define K_WRITE 0x1
#define K_ESCAPE 0x2

#define K_MODE 0x3

#define K_DELAY_1 0x4
#define K_DELAY_2 0x5
#define K_AN3_L 0x6
#define K_AN3_R 0x7
#define K_PR1_L 0x8
#define K_AN1_L 0x9
#define K_PR2_L 0xA
#define K_AN2_L 0xB
#define K_PR1_R 0xC
#define K_AN1_R 0xD
#define K_PR2_R 0xE
#define K_AN2_R 0xF

// Extended mode commands after K_MODE
#define K_MODE_FOUR_EARS 0x3 // now obsolete
#define K_MODE_RESET 0x4
#define K_MODE_LOAD 0x5
#define K_MODE_SAVE 0x6
#define K_MODE_TWO_EARS 0x8 // now obsolete
#define K_MODE_LED 0xC
#define K_MODE_NEG_DELAY 0xE
#define K_MODE_POS_DELAY 0xF

// defined for individual targets separately
void send_4_raw( u08 bits );

void send_4( u08 bits )
{
  if (bits < 3) // needs escape character
    send_4_raw(K_ESCAPE);

  send_4_raw(bits);
}

void send_8(u08 more_bits)
{
  send_4((more_bits&0xf0)>>4);
  send_4(more_bits&0xf);
}

void resynchronize()
{
  send_4_raw(K_RESYNC);
  send_4_raw(K_RESYNC);
}

void send_command(u08 command, u08 data)
{ 
  resynchronize();
  send_4_raw(command);
  send_8(data);
  send_4_raw(K_WRITE);
}

void set_mix_gain(u08 side, u08 number, u08 data)
{
  u08 command;

  if (side==K_LEFT)
    {
      if (number==1)
	command=K_AN1_L;
      else if (number==2)
	command=K_AN2_L;
      else // 3
	command=K_AN3_L;
    }
  else // K_RIGHT
    {
      if (number==1)
	command=K_AN1_R;
      else if (number==2)
	command=K_AN2_R;
      else // 3
	command=K_AN3_R;
    }
  send_command(command, data);
}
void set_preamp_gain(u08 side, u08 number, u08 data)
{
  u08 command;
  
  if (side==K_LEFT)
    {
      if (number==1)
	command=K_PR1_L;
      else // 2
	command=K_PR2_L;
    }
  else // K_RIGHT
    {
      if (number==1)
	command=K_PR1_R;
      else // 2
	command=K_PR2_R;
    }
  send_command(command, data);
}

void set_delay(u08 number, u08 delay)
{
  if (number==1)
    send_command(K_DELAY_1, delay);
  else // 2
    send_command(K_DELAY_2, delay);
}

void set_mode(u08 mode, u08 data)
{
  send_command(K_MODE, (mode<<4)+data);
}

void set_high_delay(bool neg, u08 high)
{
  if (neg)
    set_mode(K_MODE_NEG_DELAY, high);
  else
    set_mode(K_MODE_POS_DELAY, high);
}

void set_long_delay(u08 number, s16 ldelay)
{
  s16 long_d=ldelay;
  bool neg=FALSE;
  u08 high=0;
  u08 delay=0;

  if (long_d<0)
    {
      neg=TRUE;
      long_d=-long_d;
    }

  high=(long_d>>8) & 0xf;
  delay=long_d & 0xff;
  set_high_delay(neg, high);
  set_delay(number, delay);
}

void set_LED(u08 one, u08 two)
{
  set_mode(K_MODE_LED,(one<<3)+two);
}

void save(u08 store_num)
{
  set_mode(K_MODE_SAVE,store_num);
}

void load(u08 store_num)
{
  set_mode(K_MODE_LOAD,store_num);
}

void reset_ears()
{ 
  set_mode(K_MODE_RESET,0);
}

void set_num_ears(u08 number)
{
  if (number==2) // two ears
    set_mode(K_MODE_TWO_EARS,0);
  else // four ears
    set_mode(K_MODE_FOUR_EARS,0);
}

void set_new_ears(u08* data)
{
  int i;
  for (i=1; i<=3; i++)
    set_mix_gain(K_RIGHT,i,data[i-1]);
  for (i=1; i<=3; i++)
    set_mix_gain(K_LEFT,i,data[i+2]);
  for (i=1; i<=2; i++)
    set_preamp_gain(K_RIGHT,i,data[i+5]);
  for (i=1; i<=2; i++)
    set_preamp_gain(K_LEFT,i,data[i+7]);
  for (i=1; i<=2; i++)
    {
      set_high_delay(data[3*i+7], data[3*i+8]);
      set_delay(i,data[3*i+9]);
    }
}

bool initialise_ears( s16 numEars )
{
  u08 setup[16];

  if (numEars == 1)
    {
      setup[0] = 0x0;  // gain 1
      setup[1] = 0x80; // gain 0
      setup[2] = 0x80; // gain 0
      setup[3] = 0x0;  // gain 1
      setup[4] = 0x80; // gain 0
      setup[5] = 0x80; // gain 0
      setup[6] = 0xa0; // Some gains that need to be calibrated!
      setup[7] = 0xa0;
      setup[8] = 0xa0;
      setup[9] = 0xa0;
      setup[10] = (u08) FALSE;  // positive
      setup[11] = 0x0;  // 0 high
      setup[12] = 0xf7; // +261 degrees phase diff.
      setup[13] = (u08) TRUE;  // negative
      setup[14] = 0x0;  // 0 high
      setup[15] = 0x27; //  -41 degrees phase diff.
      set_new_ears( setup );
      return TRUE;
    }
  else if (numEars == 2)
    {
      setup[0] = 0x0;  // gain +1
      setup[1] = 0xff; // gain -1
      setup[2] = 0x80; // gain 0
      setup[3] = 0x0;  // gain +1
      setup[4] = 0xff; // gain -1
      setup[5] = 0x80; // gain 0
      setup[6] = 0xa0; // Some gains that need to be calibrated!
      setup[7] = 0x0;
      setup[8] = 0xa0;
      setup[9] = 0x0;
      setup[10] = (u08) FALSE;  // positive
      setup[11] = 0x0;  // 0 high
      setup[12] = 0x55; // *0.625  =  1/4 wvln@4.7KHz at 350m/s (18mm)
      setup[13] = (u08) FALSE;
      setup[14] = 0x0;
      setup[15] = 0x0;
      set_new_ears( setup );
      return TRUE;
    }
  else if (numEars == 3)
    {
      setup[0] = 255;  // gain +1
      setup[1] = 10; // gain -1
      setup[2] = 128; // gain 0
      setup[3] = 255;  // gain +1
      setup[4] = 0; // gain -1
      setup[5] = 128; // gain 0
      setup[6] = 230;
      setup[7] = 0;
      setup[8] = 255;
      setup[9] = 0;
      setup[10] = (u08) FALSE;  // positive
      setup[11] = 0x0;  // 0 high
      setup[12] = 0x55; // *0.625  =  1/4 wvln@4.7KHz at 350m/s (18mm)
      setup[13] = (u08) FALSE;
      setup[14] = 0x0;
      setup[15] = 0x0;
      set_new_ears( setup );
      return TRUE;
    }
  else if (numEars == 4)
    {
      setup[0] = 0xff;  // gain -1.5
      setup[1] = 0xa5; // gain -0.44
      setup[2] = 0x2a; // gain +1
      setup[3] = 0xff;  // gain -1.5
      setup[4] = 0xa5; // gain -0.44
      setup[5] = 0x2a; // gain +1
      setup[6] = 0xa0; // Some gains that need to be calibrated!
      setup[7] = 0xa0;
      setup[8] = 0xa0;
      setup[9] = 0xa0;
      setup[10] = (u08) FALSE;  // positive
      setup[11] = 0x0;  // 0 high
      setup[12] = 0xf7; // +261 degrees phase diff.
      setup[13] = (u08) TRUE;  // negative
      setup[14] = 0x0;  // 0 high
      setup[15] = 0x27; //  -41 degrees phase diff.
      set_new_ears( setup );
      return TRUE;
    }
  else if (numEars == 5) // ears hanging down from koala (redgreen-2)
    {
      setup[0] = 250;  // gain -1.5
      setup[1] = 165; // gain -0.44
      setup[2] = 250; // gain +1
      setup[3] = 255;  // gain -1.5
      setup[4] = 156; // gain -0.44
      setup[5] = 255; // gain +1
      setup[6] = 255;
      setup[7] = 100;
      setup[8] = 255;
      setup[9] = 100;
      setup[10] = (u08) TRUE;  // negative
      setup[11] = 0x0;  // 0 high
      setup[12] = 53; //  +54 degrees phase diff.
      setup[13] = (u08) FALSE;  // positive
      setup[14] = 0x0;  // 0 high
      setup[15] = 152; // -154 degrees phase diff.
      set_new_ears( setup );
      return TRUE;
    }
  else if (numEars == 6) // normal upright ears on khepera
    {
      setup[0] = 255;  // gain -1.5
      setup[1] = 165; // gain -0.44
      setup[2] = 218; // gain +1
      setup[3] = 255;  // gain -1.5
      setup[4] = 165; // gain -0.44
      setup[5] = 218; // gain +1
      setup[6] = 255;
      setup[7] = 180;
      setup[8] = 210;
      setup[9] = 180;
      setup[10] = (u08) TRUE;  // negative
      setup[11] = 0x0;  // 0 high
      setup[12] = 53; //  +54 degrees phase diff.
      setup[13] = (u08) FALSE;  // positive
      setup[14] = 0x0;  // 0 high
      setup[15] = 152; // -154 degrees phase diff.
      set_new_ears( setup );
      return TRUE;
    }

  return FALSE;
}

