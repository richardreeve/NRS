#ifndef AUXILIARUSED
#define AUXILIARUSED

#ifdef HAVE_CONFIG_H
# include "config.h"
#endif

#ifdef __ROBOT_kteam
/*
* KTEAM INCLUDES
*/
# include "khepera_compatibility.h"

# define USING_KH2_NODE
# define USING_LED_NODE
# define USING_MOTOR_NODE
# define USING_IR_NODE
# define USING_ODO_NODE
# define USING_ESYS_NODE
# define USING_INTEG_NODE
# define USING_BPASS_NODE
# define USING_DELTA_NODE
#else
/*
* ATMEL INCLUDES
*/
# include <avr/io.h>
# include <avr/interrupt.h>
# include <avr/signal.h>
# include <avr/pgmspace.h>

# define USING_LED_NODE
# define USING_ADC_NODE
# define USING_OPTOMOTOR_NODE
# define USING_ATMEL_NODE
# define USING_STEPPER_NODE
# define USING_SERVO_NODE
# define USING_INTEG_NODE
# define USING_BPASS_NODE
# define USING_DELTA_NODE
# define USING_OFFSETGAIN_NODE
# define USING_TIMER_NODE
#endif

#include <stdlib.h>

#define F_CPU            16000000      /* 16Mhz */
#define SET_BYTE(p,m) ((p) |= (m))
#define AND_BYTE(p,m) ((p) &= ~(m))
#define XOR_BYTE(p,m) ((p) ^= (m))

#define SET_BIT(p,m) ((p) |= (0x01 << (m)))
#define WITH_BITSET(p,m) ((p)|(0x01<<(m)))
#define CLEAR_BIT(p,m) ((p) &= ~(0x01 << (m)))
#define XOR_BIT(p,m) ((p) ^= (0x01 << (m)))
#define WITH_XORBIT ((p)^(0x01 << (m)))

#define BYTE_UPTO(B,x) (x<0?0:((B)&((0x02<<(x))-1)))
#define BIT(x) (0x01 << (x))
#define BYTE_GET(p,m) ((p) & (m))
#define GET_BIT(p,x) ((p)&(0x01<<(x)))

#ifdef __MICRO_avr
//#define MALLOC(VAR, SIZE) if(((VAR)=malloc(sizeof(SIZE)))==NULL){CLEAR_BIT(PORTG,4);return NULL;}
# define MALLOC(VAR, SIZE) if(((VAR)=malloc(sizeof(SIZE)))==NULL){CLEAR_BIT(PORTG,4);s0out(0x12);s0out(0x5e);s0out('#');s0out(0x00);for(;;){};return NULL;}
#else
# define MALLOC(VAR, SIZE) (VAR)=malloc(sizeof(SIZE));
#endif

#define OK 1
//#define DEBUG
//#define BRIEFDEBUG
//#define HEADERDEBUG

//#undef DEBUG

#ifdef DEBUG
# define COMMDEBUG
#endif
#ifdef BRIEFDEBUG
# define COMMDEBUG
#endif


typedef unsigned long my_unsigned;
typedef int t_int;
#define T_INTSIZE (sizeof(t_int)*8)
typedef unsigned char  u08;
typedef unsigned char  s08;
typedef unsigned short u16;
typedef           short s16;

typedef unsigned char bool;	//for ears. remove later?
typedef unsigned long u32;	//for ears. remove later?
typedef long s32;	//for motors

#endif //ndef AUXILIARUSED
