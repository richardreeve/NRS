#include "opencv/cv.h"
#include "opencv_subroutines.h"
#include "ImageUtils.hh"

//----------------------------------------------------------------------
void convertYUV420ImageGreyscale(IplImage * dest,
                                 const unsigned char* src,
                                 int plane,
                                 int bytes,
                                 int width,
                                 int height,
                                 int depth)
{
  const unsigned char *ptr = src;
  int pixX, pixY;
  
  for (int j = 0; j < height; j++) 
    {
      for (int i = 0; i < (width - width % bytes) / bytes; i++)
        {
          for (int k = 0; k < bytes; k++)
            {
              pixX = i * bytes + k;
              pixY = j;
              
              ((uchar*)(dest->imageData + dest->widthStep*pixY))[pixX] 
                = ptr[k+plane];
            }
          ptr += depth;
        }
    }
}
//----------------------------------------------------------------------
void convertYUV420ImageRGB(IplImage * dest,
                           unsigned char* src,
                           int width,
                           int height,
                           int format)
{
  yuv420p_to_rgb(width, 
                 height, 
                 src, 
                 (unsigned char*) dest->imageData, 
                 icvVideoFormat2Depth(format));
}
//----------------------------------------------------------------------
IplImage* makeIplImageFromRGB24(int width, int height, char* rgbData)
{
  CvSize size;
  size.width = width;
  size.height = height;

  IplImage *cvImg = cvCreateImage(size, IPL_DEPTH_8U, 3);
  cvImg->imageData = rgbData;
  return cvImg;
}
