#ifndef __IMAGEUTILS_HH__
#define __IMAGEUTILS_HH__

#include <linux/videodev.h>

/** Convert a YUV4:2:0 image into greyscale OpenCV compatible
 * format. This routine makes use of the conversion code written by
 * Richard Reeve for NRS. */
void convertYUV420ImageGreyscale(IplImage * dest,
                                 const unsigned char* src,
                                 int plane,
                                 int bytes,
                                 int width,
                                 int height,
                                 int depth);

/** Convert a YUV4:2:0 image into colour OpenCV compatible format. This
  * routine currently uses OpenCV routines. Possible values for
  * destFormat are (as defined in videodev.h):
  *
  *  VIDEO_PALETTE_RGB24
  *
  *  VIDEO_PALETTE_RGB32
  *
  *  VIDEO_PALETTE_RGB565
  *
  *  VIDEO_PALETTE_RGB555
  */
void convertYUV420ImageRGB(IplImage * dest,
                           unsigned char* src,
                           int width,
                           int height,
                           int format = VIDEO_PALETTE_RGB24);

/**
 * Utility method to create a new OpenCV IplImage object based on the
 * suppied RGB data. A pointer to the new object is returns, and the
 * caller assumes responsibility for later calling:
 *
 *    cvReleaseImage(img)
 *
 * ... to release the resources associated with the object.
 */

IplImage* makeIplImageFromRGB24(int width, int size, char* rgbData);

#endif
