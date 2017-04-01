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
#ifndef __WEBCAMERA_HH__
#define __WEBCAMERA_HH__

/*
 * Warning: ensure the #include <string> comes before #include
 * <linux/videodev.h>. This situation will avoid a 'conflicting types'
 * error when compiling on Mandrake 10.0 systems (and possibly other Linux
 * distributions)
 * */
#include <string>

#include <linux/videodev.h>

/// Forward declarations. Using these to remove inter-file
/// dependencies. To use any methods which involve any of the
/// forwardly-delcared types below, please ensure you include the
/// appropriate header file which defines the relevant types.
class ImageFormat;

/// Webcam objects open and close connections to webcams, set their
/// configurations
class Webcam
{
public:
  /**
   * Constructor. Will create a Webcam object which will assumes the
   * camera is attached to a default device.
   **/
  Webcam();

  /**
   * Constructor. Accepts the name of the device to which the camera is
   * attached.
   */
  Webcam(const std::string &device);

  /**
   * Destructor. Closes the camera interface.
   */
  ~Webcam();

  /**
   * Attempt to open the video device
   *
   * \return bool value true if the device was opened, otherwise
   * false. If the device couldn't be opened also outputs an error to
   * console. Also outputs to console a message on success.
   */
  bool openDevice();

  /**
   * Attempt to close the video device
   */
  void closeDevice();

  /**
    * Attempt to acquire the model name of the camera
    *
    * \param model Destination to which camera string will be written
    *
    * \return true if call camera request succeeds, false otherwise
    *
    */
  bool getModel(std::string& model);

  /**
    * Attempt to acquire the maximum image size supported by this camera
    *
    * \param width Used to return the maximum width value
    *
    * \param height Used to return the maxium height value
    *
    * \return true if call camera request succeeds, false otherwise
    *
    */
  bool getMaxResolution(int& width, int& height);

  /**
   * Attempt to set the camera resolution
   */
  void setResolution(int width, int height, int fps);

  /**
   * Attempt to set the camera resolution
   */
  void setResolution(const ImageFormat &format, int fps);

  /**
   * Query the camera to retrieve the current camera settings
   */
  void getResolution(int &width, int &height, int &fps);

  /**
   * Return the palette code (from cache). To decode these values please
   * refer to the system file videodev.h and search for the relevant
   * #define VIDEO_PALETTE_. Alternatively use the
   * decodePaletteCode(...) function below.
   */
  int getPaletteCode() const;

  /**
   * Return the depth of images captured by the camera
   */
  int getDepth() const;

  /**
   * Decode a palette code, based on the entries found in videodev.h
   *
   * \return string representation of the palette code. The caller
   * should NOT attempt to delete the memory pointed to by the returned
   * pointer.
   */
  const char* decodePaletteCode(int pc) const;

  /**
   * Perform the necessary Video4Linux API calls to permit the capture
   * of images from the camera using the MMIO shared memory
   * technique. The capture of actual images is also initiated.
   *
   * \return true if capture was initiated, false if it failed
   */
  bool initCapture();

  /**
   * Complete the capture of the next image frame. Following successful
   * return of this method, the captured image is available through the
   * returned pointer. Ownwership of this memory is NOT passed to the
   * caller.
   *
   * \return Pointer to the image memory (ownership not transfered), or
   * 0 if image could not be captured.
   */
  unsigned char* nextFrame();

  /**
   * Set and retrieve the image compression factor. A compression
   * preference of 0 means use uncompressed modes when available; 1 is
   * low compression, 2 is medium and 3 is high compression
   * preferred. Of course, the higher the compression, the lower the
   * bandwidth used but more chance of artefacts in the image. The
   * driver automatically chooses a higher compression when the
   * preferred mode is not available. (Note, these comment are taken
   * from the API calls in pwc-ioctl.h)
   *
   * \return Value true if call completes successfully, false otherwise.
   */
  bool getCompressionFactor(int&);
  bool setCompressionFactor(int);

  /**
   * Attempt to set the camera LED to blinking on Philips Web
   * Cameras. This feature is not supported by all models from
   * Philips. The rate of blinking is specified by the two parameters.
   *
   * \param led_on The duration for which the LED is on
   *
   * \param led_off The duration for which the LED is off
   *
   * \return Value true if call completes successfully, false otherwise.
   */
  bool blinkLED(int led_on = 200, int led_off = 800);

  /**
   * Scans through a set of possible camera modes, and displays whether
   * the camera could be set to each mode. Ie, indicates to the user
   * which image modes the connect camera supports.
   */
  void displayModeScan();

  /**
   * Attempt to set the camera to a resolution which provides a maximum
   * frame rate. The found resolution is returned through the reference
   * arguments. This routine may fail to find a successful resolution,
   * in which case the return value is false. Note, the client should
   * NOT attempt to delete the object pointed to by the imageFormat
   * argument-reference.
   *
   * \return true is resolution set, false if not
   */
  bool setResolutionMaxFPS(const ImageFormat* &imageFormat, int& fps);

  /**
   * Attempt to set the camera to a resolution which provides a maximum
   * frame rate. The found resolution is returned through the reference
   * arguments. This routine may fail to find a successful resolution,
   * in which case the return value is false.
   *
   * \return true is resolution set, false if not
   */
  bool setResolutionMaxFPS();

  /**
   * Attempt to set the camera to a resolution which provides a maximum
   * frame size. The found resolution is returned through the reference
   * arguments. This routine may fail to find a successful resolution,
   * in which case the return value is false. Note, the client should
   * NOT attempt to delete the object pointed to by the imageFormat
   * argument-reference.
   *
   * \return true is resolution set, false if not
   */
  bool setResolutionMaxSize(const ImageFormat* &imageFormat, int& fps);

  /**
   * Attempt to set the camera to a resolution which provides a maximum
   * frame size. The found resolution is returned through the reference
   * arguments. This routine may fail to find a successful resolution,
   * in which case the return value is false.
   *
   * \return true is resolution set, false if not
   */
  bool setResolutionMaxSize();

private:
  /** Request video picture information from the camera and store
      locally */
  bool aquirePictureInformation();

  /** The copy constructor is disabled, to prevent possible problems
      with access to underlying shared memory */
  Webcam(const Webcam&);

  /** The assignment operator is disabled, to prevent possible problems
      with access to underlying shared memory */
  Webcam& operator=(const Webcam&);

  /** Test the specified mode, and return true if successful, false
      otherwise */
  bool attemptMode(const ImageFormat &format, int fps, bool echo = true);

  int* fpsRange() const;
  const ImageFormat** sizeRange() const;

private:
  ///// DATA MEMBERS

  /// String name of the device which camera is connected to
  std::string m_device;

  /// File descriptor of the camera device
  int m_cfd;

  /// Boolean to track if the camera device is open or close
  bool m_isOpen;

  /// Indicate whether the shared memory, used to access images, is available
  bool m_isSharedMemAvail;

  /// Stores information about the shared memory used to receive images
  struct video_mbuf m_memoryBuffer;

  /// Pointer to the start of the shared memory used to receive images
  void* m_memoryMap;

  /// Various picture details, including palette code
  struct video_picture m_vp;

  /// Index of the last frame which was completed captured.
  int m_lastFrame;

  /// Count of the number of frames the device can simultaneously capture
  int m_numFrames;

  /// Used during image capture to indicate to the Video4Linux API which
  /// frame is be retrieved (and also to described the size and palette
  /// of the image)
  struct video_mmap m_vmmap;
};

#endif
