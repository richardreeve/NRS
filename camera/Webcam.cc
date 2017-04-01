/*
 * Copyright (C) 2004 Darren Smith
 *
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation; either version 2 of
 *    the License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public
 *    License along with this program; if not, write to the Free
 *    Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 *    MA 02111-1307 USA
 *
 * For further information in the first instance contact:
 * Richard Reeve <richardr@inf.ed.ac.uk>
 *
 */

#include <fcntl.h>
#include <sys/ioctl.h>
#include <sys/mman.h>
#include <iostream>
#include <string>
#include <iostream>
#include <unistd.h>  // for close(...)

#include "pwc-ioctl.h"
#include "ImageFormat.hh"
#include "Webcam.hh"
#include "Exception.hh"

#define DEFAULT_DEVICE "/dev/video"
#define INITIAL_LAST_FRAME -1

//----------------------------------------------------------------------
// Constructor
Webcam::Webcam()
  : m_device(DEFAULT_DEVICE),
    m_isOpen(false),
    m_isSharedMemAvail(false),
    m_memoryMap(MAP_FAILED),
    m_lastFrame(INITIAL_LAST_FRAME)
{
}
//----------------------------------------------------------------------
// Constructor
Webcam::Webcam(const std::string &device)
  : m_device(device),
    m_isOpen(false),
    m_isSharedMemAvail(false),
    m_memoryMap(MAP_FAILED),
    m_lastFrame(INITIAL_LAST_FRAME)
{
}
//----------------------------------------------------------------------
// Destructor
Webcam::~Webcam()
{
  closeDevice();
}
//----------------------------------------------------------------------
bool Webcam::openDevice()
{
  m_cfd = open(m_device.c_str(), O_RDWR);

  if (m_cfd < 0)
    {
      _ERROR_("Failed to open camera device \"" << m_device << "\""
              << " (errno=" << errno 
              << ", " << strerror(errno)
              << ")");
      return false;
    }
  else
    {
      _INFO_("Camera device \"" << m_device << "\" opened ok");
      m_isOpen = true;
    }

  // Finally attempt acquire the picture information. We do this here
  // because later methods will require the palette information
  return aquirePictureInformation();
}
//----------------------------------------------------------------------
void Webcam::closeDevice()
{
  // Unmap any shared capture memory
  if (m_memoryMap >= 0)
    {
      munmap(m_memoryMap, m_memoryBuffer.size);
    }
  
  m_isSharedMemAvail = false;
  m_memoryMap = MAP_FAILED;
  m_lastFrame = INITIAL_LAST_FRAME;

  if (m_isOpen)
    {
      m_isOpen = false;
      close(m_cfd);
    }
}
//----------------------------------------------------------------------
void Webcam::setResolution(int width, int height, int fps)
{
  if (!m_isOpen)
    {
      _ERROR_("Video device has not been opened");
      return;
    }

  video_window vwin;

  // First fill in the vwin data structure
  if (ioctl(m_cfd, VIDIOCGWIN, &vwin) < 0)
    {
      _ERROR_("Failed to retrieve camera video settings");
      return;
    }

  // Now copy in the user resolution settings
  vwin.width=width;
  vwin.height=height;

  // zero out the subset of bits in the flags variable which represent
  // the fps value
  vwin.flags &= ~PWC_FPS_MASK;

  // now add in the desired fps (ie set the fps bits) by shifting the
  // user supplied fps value to the position where the fps bits reside
  // (refer to pwc-ioctl.h)
  vwin.flags |= (fps << PWC_FPS_SHIFT);

  // Now set this property...
  if (ioctl(m_cfd, VIDIOCSWIN, &vwin) < 0)
    {
      _ERROR_("Failed to apply resolution " 
              << "(" << width << "x" << height << "@" << fps << ")");
      return;
    }
}
//----------------------------------------------------------------------
void Webcam::setResolution(const ImageFormat &format, int fps)
{
  setResolution(format.getWidth(), format.getHeight(), fps);
}
//----------------------------------------------------------------------
void Webcam::getResolution(int &width, int &height, int &fps)
{
  if (!m_isOpen)
    {
      _ERROR_("Video device has not been opened");
      return;
    }

  video_window vwin;

  // First fill in the vwin data structure
  if (ioctl(m_cfd, VIDIOCGWIN, &vwin) < 0)
    {
      _ERROR_("Failed to retrieve camera video settings");
      return;
    }

  // Now copy in the user resolution settings
  width = vwin.width;
  height = vwin.height;
  fps = vwin.flags;

  // Bit juggling for the fps... first apply the fps-bit mask, and then
  // shuffle the bits down (refer to pwc-ioctl.h)
  fps &= PWC_FPS_MASK;
  fps = fps >> PWC_FPS_SHIFT;
}
//----------------------------------------------------------------------
int Webcam::getPaletteCode() const
{
  return m_vp.palette;
}
//----------------------------------------------------------------------
int Webcam::getDepth() const
{
  return m_vp.depth;
}
//----------------------------------------------------------------------
bool Webcam::aquirePictureInformation()
{
  if (ioctl(m_cfd, VIDIOCGPICT, &m_vp) < 0)
    {
      _ERROR_("Failed to retrieve picture information");
      return false;
    }

  return true;
}
//----------------------------------------------------------------------
bool Webcam::initCapture()
{
  if (!m_isOpen)
    {
      _ERROR_("Video device has not been opened");
      return false;
    }

  // Obtain information from the VFL device needed for MMIO (Memory
  // Mapped Input/Output). The structure used to receive information
  // contains the size in bytes of the memory mapped area, the number of
  // frames bufferred by the capture device, and an array of offsets
  // into the memory mapped area for each of the frames.
  if (ioctl(m_cfd, VIDIOCGMBUF, &m_memoryBuffer) == -1)
    {
      _ERROR_("Failed to retrieve MMIO information");
      return false;
    }
  m_numFrames = m_memoryBuffer.frames;

  //  _DEBUG_("Frames=" << m_memoryBuffer.frames);
  //  _DEBUG_("Size=" << m_memoryBuffer.size);
  //  _DEBUG_("Offset[1]=" << m_memoryBuffer.offsets[1]);

  // The next step is to get a pointer to the memory mapped area. The
  // kernel will put camera data directly into this memory, and this
  // program, because it will be sharing thay memory, will be able to
  // read it out
  m_memoryMap = (char*) mmap(0, m_memoryBuffer.size,
                             PROT_READ | PROT_WRITE, MAP_SHARED,
                             m_cfd, 0);
  if ((m_memoryMap == MAP_FAILED) || (m_memoryMap < 0))
    {
      _ERROR_("Failed to obtain access to the memory used to receive"
              << " camera images (errno=" << errno << ")");
      return false;
    }

  // Attempt to initiate image capture into each frame buffer
  int width, height, fps;

  getResolution(width, height, fps);

  for (int i=0; i < m_memoryBuffer.frames; i++)
    {
      m_vmmap.frame  = i;
      m_vmmap.height = height;
      m_vmmap.width  = width;
      m_vmmap.format = m_vp.palette;

      if (ioctl(m_cfd, VIDIOCMCAPTURE, &m_vmmap) < 0)
	{
          _ERROR_("Failed to initiate image capture");
          return false;
	}
    }

  // I can't be sure that m_vmmap was not changed during the final call
  // of ioctl... so call it assign to it once more to store a copy of
  // the picture details. It will be used again during the synchronous
  // image capture requests.
  m_vmmap.height = height;
  m_vmmap.width  = width;
  m_vmmap.format = m_vp.palette;

  m_isSharedMemAvail = true;
  return true;
}
//----------------------------------------------------------------------
unsigned char* Webcam::nextFrame()
{
  static int syncFrame;
  struct video_mmap vmmap;

  if (!m_isSharedMemAvail)
    {
      _ERROR_("The MMIO shared memory capture has not been initialised");
      return 0;
    }

  // Determine the frame that we will attempt to fully aquire. This
  // frame is m_lastFrame + 1, because the capture for frame number
  // m_lastFrame has previously been initiated
  syncFrame = m_lastFrame + 1;

  if (syncFrame >= m_numFrames)
    {
      syncFrame = 0;
    }

  // If last frame is a useable index, then start capturing to it.
  if (m_lastFrame >= 0)
    {
      memcpy(&vmmap, &m_vmmap, sizeof (video_mmap));
      vmmap.frame = m_lastFrame;

      if (ioctl(m_cfd, VIDIOCMCAPTURE, &vmmap) < 0)
	{
          _ERROR_("Failed to initiate capture of the next frame");
          return 0;
	}
    }

  // Now perform the image sync for the current frame. This is a
  // synchronous request, so following the return of the ioctl, and
  // image will be available
  memcpy(&vmmap, &m_vmmap, sizeof (video_mmap));
  vmmap.frame = syncFrame;
  if (ioctl (m_cfd, VIDIOCSYNC, &vmmap) < 0)
    {
      _ERROR_("Failed to complete capture (i.e., to synchronize)"
              << " image frame");
      return 0;
    }
  else
    {
      m_lastFrame = syncFrame;
      return (unsigned char*) m_memoryMap 
        + m_memoryBuffer.offsets[syncFrame];
    }
}
//----------------------------------------------------------------------
bool Webcam::getCompressionFactor(int& compression)
{
  if (ioctl(m_cfd, VIDIOCPWCGCQUAL, &compression) == -1)
    {
      _WARN_("Failed to retrieve compression factor from camera");
      return false;
    }

  return true;
}
//----------------------------------------------------------------------
bool Webcam::setCompressionFactor(int compression)
{
  if (ioctl(m_cfd, VIDIOCPWCSCQUAL, &compression) == -1)
    {
      _WARN_("Failed to set compression factor");
      return false;
    }

  return true;
}
//----------------------------------------------------------------------
bool Webcam::blinkLED(int led_on, int led_off)
{
  pwc_leds blink;
  blink.led_on = led_on;
  blink.led_off = led_off;

  if (ioctl(m_cfd, VIDIOCPWCSLED, &blink) == -1)
    {
      _WARN_("Failed apply blink setting to LED");
      return false;
    }

  //  _DEBUG_("led_on=" << blink.led_on << ", led_off=" << blink.led_off);

  // Test to see of the blink settings indicate a lack of
  // configurability. The -1 value comes from previous testing.
  if (ioctl(m_cfd, VIDIOCPWCGLED, &blink) != -1)
    {
      if ((blink.led_on == -1) && (blink.led_off == -1))
        {
          _WARN_("Camera might not support LED operation");
          return false;
        }
    }

  return true;
}
//----------------------------------------------------------------------
bool Webcam::getModel(std::string& model)
{
  struct video_capability vidCap;

  if (ioctl(m_cfd, VIDIOCGCAP, &vidCap) == -1)
    {
      _WARN_("Failed to retrieve name of camera model");
      return false;
    }

  model = vidCap.name;

  return true;
}
//----------------------------------------------------------------------
bool Webcam::getMaxResolution(int& width, int& height)
{
  struct video_capability vidCap;

  if (ioctl(m_cfd, VIDIOCGCAP, &vidCap) == -1)
    {
      _WARN_("Failed to retrieve the maximum image size of the camera");
      return false;
    }

  width  = vidCap.maxwidth;
  height = vidCap.maxheight;

  return true;
}
//----------------------------------------------------------------------
const char* Webcam::decodePaletteCode(int pc) const
{
  // this should be kept upto date with videodev.h
  switch (pc)
    {
    case VIDEO_PALETTE_GREY : return "Linear greyscale";
    case VIDEO_PALETTE_HI240 : return "High 240 cube (BT848)";
    case VIDEO_PALETTE_RGB565 : return "565 16 bit RGB";
    case VIDEO_PALETTE_RGB24 : return "24bit RGB";
    case VIDEO_PALETTE_RGB32 : return "32bit RGB";
    case VIDEO_PALETTE_RGB555 : return "555 15bit RGB";
    case VIDEO_PALETTE_YUV422 : return "YUV422 capture";
    case VIDEO_PALETTE_YUYV : return "VIDEO_PALETTE_YUYV";
    case VIDEO_PALETTE_UYVY : return "VIDEO_PALETTE_UYVY";
    case VIDEO_PALETTE_YUV420 : return "VIDEO_PALETTE_YUV420";
    case VIDEO_PALETTE_YUV411 : return "YUV411 capture";
    case VIDEO_PALETTE_RAW : return "RAW capture (BT848)";
    case VIDEO_PALETTE_YUV422P : return "YUV 4:2:2 Planar";
    case VIDEO_PALETTE_YUV411P : return "YUV 4:1:1 Planar";
    case VIDEO_PALETTE_YUV420P : return "YUV 4:2:0 Planar";
    case VIDEO_PALETTE_YUV410P : return "YUV 4:1:0 Planar";
    default : return "not recognised";
    };
}
//----------------------------------------------------------------------
bool Webcam::attemptMode(const ImageFormat &format,
                         int fps,
                         bool echo)
{
  int _w, _h, _fps;
  
  setResolution(format, fps);
  getResolution(_w, _h, _fps); 

  bool matched = ((_w == format.getWidth()) 
                  && (_h == format.getHeight())
                  && (_fps == fps));

  if (echo)
    {
      _INFO_("Attempted " << format.getName()
             << " @ " << fps << " fps : Achieved " << _w 
             << " x " << _h << " @ " << _fps
             << ((matched)? " [ OK ]" : " [ FAIL ]")); 
    }
  
  return matched;
}
//----------------------------------------------------------------------
void Webcam::displayModeScan()
{
  int* fps = fpsRange();
  const ImageFormat** sizes = sizeRange();

  int maxWidth, maxHeight;
  bool limitToMax = getMaxResolution(maxWidth, maxHeight);
  
  if (!m_isOpen) 
    {
      _ERROR_("Video device has not been opened");
      return;
    }

  for (int i = 0; fps[i] !=0; i++)
    for (int j = 0; sizes[j] !=0; j++)
      {
        if (((limitToMax)
             and (sizes[j]->getWidth() <= maxWidth)
             and (sizes[j]->getHeight() <= maxHeight)) 
            or (!limitToMax))
          {            
            attemptMode(*(sizes[j]), fps[i], true);
          }
      }
}
//----------------------------------------------------------------------
bool Webcam::setResolutionMaxFPS()
{
  const ImageFormat* imageFormat;
  int fps;

  return setResolutionMaxFPS(imageFormat, fps);
}
//----------------------------------------------------------------------
bool Webcam::setResolutionMaxFPS(const ImageFormat* &imageFormat,
                                 int& fps)
{
  int* _fps = fpsRange();
  const ImageFormat** sizes = sizeRange();  

  int maxWidth, maxHeight;
  bool limitToMax = getMaxResolution(maxWidth, maxHeight);

  for (int i = 0;  _fps[i] !=0 ; i++)
    for (int j = 0; sizes[j] != 0; j++)
      if (((limitToMax)
           and (sizes[j]->getWidth() <= maxWidth)
           and (sizes[j]->getHeight() <= maxHeight)) 
          or (!limitToMax))
        {
          if (attemptMode(*(sizes[j]), _fps[i], false))
            {
              _INFO_("Resolution set to " << sizes[j]->getName()
                     << " @ " << _fps[i] << " fps");
              imageFormat = sizes[j];
              fps = _fps[i];
              return true;
            }
        }
  
  _WARN_("Could not find a resolution for maximum fps");
  return false;
}
//----------------------------------------------------------------------
bool Webcam::setResolutionMaxSize()
{
  const ImageFormat* imageFormat;
  int fps;
  
  return setResolutionMaxSize(imageFormat, fps);
}

//----------------------------------------------------------------------
bool Webcam::setResolutionMaxSize(const ImageFormat* &imageFormat,
                                  int& fps)
{
  int* _fps = fpsRange();
  const ImageFormat** sizes = sizeRange();

  int maxWidth, maxHeight;
  bool limitToMax = getMaxResolution(maxWidth, maxHeight);

  for (int j = 0; sizes[j] != 0; j++)
    for (int i = 0; _fps[i] !=0 ; i++)
      if (((limitToMax)
           and (sizes[j]->getWidth() <= maxWidth)
           and (sizes[j]->getHeight() <= maxHeight)) 
          or (!limitToMax))
        {
          if (attemptMode(*(sizes[j]), _fps[i], false))
            {
              _INFO_("Resolution set to " << sizes[j]->getName()
                     << " @ " << _fps[i] << " fps");
              imageFormat = sizes[j];
              fps = _fps[i];
              return true;
            }
        }
  
  _WARN_("Could not find a resolution for maximum image size");
  return false;
}
//----------------------------------------------------------------------
int* Webcam::fpsRange() const
{
  static int fpsRange[] = { 30, 15, 10, 8,  5 , 0};
  return fpsRange;
}
//----------------------------------------------------------------------
const ImageFormat** Webcam::sizeRange() const
{
  static const ImageFormat* forms[] = 
    { &VGA, &CIF, &SIF, &QCIF, &QSIF, &sQCIF, 0};
  return forms;
}
