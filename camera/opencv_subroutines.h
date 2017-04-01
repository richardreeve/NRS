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

#ifndef __OPENCV_SUBROUTINES_H__
#define __OPENCV_SUBROUTINES_H__

// Specify C++ linkage
#ifdef __cplusplus
extern "C" {
#endif

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

/* Converts from planar YUV420 to RGB24. */
void yuv420p_to_rgb(int width, int height,
                    unsigned char *pIn0, unsigned char *pOut0, int bits);


/** Convert a video format into a nu mber of bits. The return result
  * will typically be used by the function yuv420p_to_rgb (for the
  * 'bits' * parameter).  
  *
  * Supported values for 'format' are (as defined in videodev.h):
  *
  *  VIDEO_PALETTE_RGB24
  *
  *  VIDEO_PALETTE_RGB32
  *
  *  VIDEO_PALETTE_RGB565
  *
  *  VIDEO_PALETTE_RGB555
  */
int icvVideoFormat2Depth(int format);

#ifdef __cplusplus
}
#endif

#endif
