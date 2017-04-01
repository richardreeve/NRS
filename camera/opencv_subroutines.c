/** All the functions below have been copied verbatim from the OpenCV
 * source. They perform image format conversion from YUV4:2:0 format to
 * RGB. YUV is the format of images produced by the Philips 680 web
 * camera, while RGB is that used by OpenCV.
 *
 * The opencv source file these routines are taken from is:
 * 
 *          <OpenCV>/otherlibs/cvcam/src/unix/cvvideo.cpp
 *
 * This kind of source copying is permitted by the OpenCV libraries. The
 * relevant licence agreement is included below (as mandated by that
 * agreement).
 *
 */

///////////////////////////////////////////////////////////////////////////////
//
//  IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.
//
//  By downloading, copying, installing or using the software you agree
//  to this license.  If you do not agree to this license, do not
//  download, install, copy or use the software.
//
//
//                        Intel License Agreement
//                For Open Source Computer Vision Library
//
// Copyright (C) 2000, Intel Corporation, all rights reserved.  Third
// party copyrights are property of their respective owners.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
//
//   * Redistribution's of source code must retain the above copyright
//   notice, this list of conditions and the following disclaimer.
//
//   * Redistribution's in binary form must reproduce the above
//   copyright notice, this list of conditions and the following
//   disclaimer in the documentation and/or other materials provided
//   with the distribution.
//
//   * The name of Intel Corporation may not be used to endorse or
//   promote products derived from this software without specific prior
//   written permission.
//
// This software is provided by the copyright holders and contributors
// "as is" and any express or implied warranties, including, but not
// limited to, the implied warranties of merchantability and fitness for
// a particular purpose are disclaimed.  In no event shall the Intel
// Corporation or contributors be liable for any direct, indirect,
// incidental, special, exemplary, or consequential damages (including,
// but not limited to, procurement of substitute goods or services; loss
// of use, data, or profits; or business interruption) however caused
// and on any theory of liability, whether in contract, strict
// liability, or tort (including negligence or otherwise) arising in any
// way out of the use of this software, even if advised of the
// possibility of such damage.
//

#include <sys/ioctl.h>
#include <linux/videodev.h>

#include "opencv_subroutines.h"

/**********************************************************************
 *
 * Color correction functions
 *
 **********************************************************************/

/*
 * Turn a YUV4:2:0 block into an RGB block
 *
 * Video4Linux seems to use the blue, green, red channel
 * order convention-- rgb[0] is blue, rgb[1] is green, rgb[2] is red.
 *
 * Color space conversion coefficients taken from the excellent
 * http://www.inforamp.net/~poynton/ColorFAQ.html
 * In his terminology, this is a CCIR 601.1 YCbCr -> RGB.
 * Y values are given for all 4 pixels, but the U (Pb)
 * and V (Pr) are assumed constant over the 2x2 block.
 *
 * To avoid floating point arithmetic, the color conversion
 * coefficients are scaled into 16.16 fixed-point integers.
 * They were determined as follows:
 *
 *	double brightness = 1.0;  (0->black; 1->full scale) 
 *	double saturation = 1.0;  (0->greyscale; 1->full color)
 *	double fixScale = brightness * 256 * 256;
 *	int rvScale = (int)(1.402 * saturation * fixScale);
 *	int guScale = (int)(-0.344136 * saturation * fixScale);
 *	int gvScale = (int)(-0.714136 * saturation * fixScale);
 *	int buScale = (int)(1.772 * saturation * fixScale);
 *	int yScale = (int)(fixScale);	
 */

/* LIMIT: convert a 16.16 fixed-point value to a byte, with clipping. */
#define LIMIT(x) ((x)>0xffffff?0xff: ((x)<=0xffff?0:((x)>>16)))

inline void move_420_block(int yTL, int yTR, int yBL, int yBR, int u, int v, 
                           int rowPixels, unsigned char * rgb, int bits)
{
	const int rvScale = 91881;
	const int guScale = -22553;
	const int gvScale = -46801;
	const int buScale = 116129;
	const int yScale  = 65536;
	int r, g, b;

	g = guScale * u + gvScale * v;
//	if (force_rgb) {
//		r = buScale * u;
//		b = rvScale * v;
//	} else {
		r = rvScale * v;
		b = buScale * u;
//	}

	yTL *= yScale; yTR *= yScale;
	yBL *= yScale; yBR *= yScale;

	if (bits == 24) {
		/* Write out top two pixels */
		rgb[0] = LIMIT(b+yTL); rgb[1] = LIMIT(g+yTL);
		rgb[2] = LIMIT(r+yTL);

		rgb[3] = LIMIT(b+yTR); rgb[4] = LIMIT(g+yTR);
		rgb[5] = LIMIT(r+yTR);

		/* Skip down to next line to write out bottom two pixels */
		rgb += 3 * rowPixels;
		rgb[0] = LIMIT(b+yBL); rgb[1] = LIMIT(g+yBL);
		rgb[2] = LIMIT(r+yBL);

		rgb[3] = LIMIT(b+yBR); rgb[4] = LIMIT(g+yBR);
		rgb[5] = LIMIT(r+yBR);
	} else if (bits == 16) {
		/* Write out top two pixels */
		rgb[0] = ((LIMIT(b+yTL) >> 3) & 0x1F) 
			| ((LIMIT(g+yTL) << 3) & 0xE0);
		rgb[1] = ((LIMIT(g+yTL) >> 5) & 0x07)
			| (LIMIT(r+yTL) & 0xF8);

		rgb[2] = ((LIMIT(b+yTR) >> 3) & 0x1F) 
			| ((LIMIT(g+yTR) << 3) & 0xE0);
		rgb[3] = ((LIMIT(g+yTR) >> 5) & 0x07) 
			| (LIMIT(r+yTR) & 0xF8);

		/* Skip down to next line to write out bottom two pixels */
		rgb += 2 * rowPixels;

		rgb[0] = ((LIMIT(b+yBL) >> 3) & 0x1F)
			| ((LIMIT(g+yBL) << 3) & 0xE0);
		rgb[1] = ((LIMIT(g+yBL) >> 5) & 0x07)
			| (LIMIT(r+yBL) & 0xF8);

		rgb[2] = ((LIMIT(b+yBR) >> 3) & 0x1F)
			| ((LIMIT(g+yBR) << 3) & 0xE0);
		rgb[3] = ((LIMIT(g+yBR) >> 5) & 0x07)
			| (LIMIT(r+yBR) & 0xF8);
	}
}

/* Converts from planar YUV420 to RGB24. */
void yuv420p_to_rgb(int width, int height,
                    unsigned char *pIn0, unsigned char *pOut0, int bits)
{
  const int numpix = width * height;
  const int bytes = bits >> 3;
  int i, j, y00, y01, y10, y11, u, v;
  unsigned char *pY = pIn0;
  unsigned char *pU = pY + numpix;
  unsigned char *pV = pU + numpix / 4;
  unsigned char *pOut = pOut0;
  
  for (j = 0; j <= height - 2; j += 2) {
    for (i = 0; i <= width - 2; i += 2) {
      y00 = *pY;
      y01 = *(pY + 1);
      y10 = *(pY + width);
      y11 = *(pY + width + 1);
      u = (*pU++) - 128;
      v = (*pV++) - 128;
      
      move_420_block(y00, y01, y10, y11, u, v,
                     width, pOut, bits);
      
      pY += 2;
      pOut += 2 * bytes;
      
    }
    pY += width;
    pOut += width * bytes;
  }
}


///////////////////////////////////////////////////////////////////////////////
int icvVideoFormat2Depth(int format)
{
    switch (format)
    {
    case VIDEO_PALETTE_RGB24:
        return 24;
        
    case VIDEO_PALETTE_RGB32:
        return 32;
        
    case VIDEO_PALETTE_RGB565:
        return 16;
        
    case VIDEO_PALETTE_RGB555:
        return 16;
        
    default:
        return 0;
    }
}
