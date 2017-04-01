// Low level code for koala for ears.c
/*         uio_[on/off]_out sets or clears the six digital outputs
 *         (reset and load are the two marked open collector, the
 *         others are the 4 CMOS ones, though they all can be CMOS
 *         or O/C). tim_suspend_task(2) pauses for 2 ms. Finally 
 *         sens_get_ana_value(4+number) gets the analogue inputs
 *         from the three analogue inputs (only two (0 and 1) are 
 *         needed, the third does nothing (oops).
 */

// $Id: ears_koala.c,v 1.1 2005/07/13 16:54:21 s0459419 Exp $
// Richard Reeve
#ifndef __MICRO_avr
#include	<koabios.h>
#include 	<stdlib.h>
#include 	<stdio.h>
#include "base.h"

#define K_CH_RESET 0
#define K_CH_LOAD 1
#define K_CH_BIT_0 8
#define K_CH_BIT_1 9
#define K_CH_BIT_2 10
#define K_CH_BIT_3 11

void send_4_raw(UCHAR bits)
{
#ifdef DEBUG
  RESERVE_SER
    printf("Sending %d\n",bits);
  RELEASE_SER
#endif //def DEBUG
    tim_suspend_task(2);
  uio_off_out(K_CH_RESET);
  uio_off_out(K_CH_LOAD);
  tim_suspend_task(2);
  if ((bits&1)==1)
    uio_on_out(K_CH_BIT_0);
  else
    uio_off_out(K_CH_BIT_0);

  if ((bits&2)==2)
    uio_on_out(K_CH_BIT_1);
  else
    uio_off_out(K_CH_BIT_1);

  if ((bits&4)==4)
    uio_on_out(K_CH_BIT_2);
  else
    uio_off_out(K_CH_BIT_2);

  if ((bits&8)==8)
    uio_on_out(K_CH_BIT_3);
  else
    uio_off_out(K_CH_BIT_3);

  uio_on_out(K_CH_LOAD);
  tim_suspend_task(2);
  uio_off_out(K_CH_LOAD);
}

LONG read_ear_out(INT number)
{
  // K_LEFT=0
  // K_RIGHT=1
  // -1 - V_ref?
  return sens_get_ana_value(5-number);
}
#endif