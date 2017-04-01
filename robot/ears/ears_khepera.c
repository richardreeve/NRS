// Low level code for khepera for ears.c
// $Id: ears_khepera.c,v 1.1 2005/07/13 16:54:21 s0459419 Exp $
// Richard Reeve
#ifndef __MICRO_avr
#include	<khebios.h>
#include 	<stdlib.h>
#include 	<stdio.h>
#include "base.h"

#define KHE_CH_DATA 48
#define KHE_CH_LOAD 52
#define KHE_LOAD 1

void send_4_raw(UCHAR bits)
{
#ifdef DEBUG
  RESERVE_SER
    printf("Sending %d\n",bits);
  RELEASE_SER
#endif //def DEBUG
    tim_suspend_task(2);
  var_put_extension(KHE_CH_DATA,bits);
  var_put_extension(KHE_CH_LOAD,KHE_LOAD);
  tim_suspend_task(2);
}

LONG read_ear_out(INT number)
{
  // K_LEFT=0
  // K_RIGHT=1
  return sens_get_ana_value(4-number);
}

#endif