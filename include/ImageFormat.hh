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

#ifndef __IMAGEFORMAT_HH__
#define __IMAGEFORMAT_HH__

#include <string>

/// class for store of image format
class ImageFormat
{
public:

  ImageFormat(const char* name, int w, int h);

  int getWidth() const;
  int getHeight() const;
  const std::string& getName() const;

private:
  int m_width;
  int m_height;
  std::string m_name;
};

// Note to clients: if you make use of these globals, remeber to link in
// the ImageFormat.o to your binary (or a library containing that object file)
extern const ImageFormat VGA;   // 640x480
extern const ImageFormat CIF;   // 352x288
extern const ImageFormat QCIF;  // 176x144
extern const ImageFormat sQCIF; // 128x96
extern const ImageFormat SIF;   // 320x240
extern const ImageFormat QSIF;  // 160x120

///// Inline defintions of above class

inline ImageFormat::ImageFormat(const char* name, int w, int h)
  : m_width(w),  m_height(h), m_name(name) { }

inline int ImageFormat::getWidth() const { return m_width; }

inline int ImageFormat::getHeight() const { return m_height; }

inline const std::string& ImageFormat::getName() const {  return m_name; }

#endif
