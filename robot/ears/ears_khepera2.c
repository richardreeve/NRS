// Low level code for khepera for ears.c
// $Id: ears_khepera2.c,v 1.2 2005/07/13 17:52:58 s0459419 Exp $
// Richard Reeve

#include 	<stdlib.h>
#include 	<stdio.h>
#include	<sys/kos.h>
#include "../auxiliar.h"

#define KHE_CH_DATA 48
#define KHE_CH_LOAD 52
#define KHE_LOAD 1

void send_4_raw(u08 bits)
{
/*
#ifdef DEBUG
  //RESERVE_SER
    printf("Sending %d\n",bits);
  //RELEASE_SER
#endif */
    tim_suspend_task(2);
  var_put_extension(KHE_CH_DATA,bits);
  var_put_extension(KHE_CH_LOAD,KHE_LOAD);
  tim_suspend_task(2);
}

u32 read_ear_out(s16 number)
{
  // K_LEFT=0
  // K_RIGHT=1
  return sens_get_ana_value(4-number);
}


