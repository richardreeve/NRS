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

#include "ImageFormat.hh"

const ImageFormat VGA("VGA", 640, 480);
const ImageFormat CIF("CIF", 352, 288);
const ImageFormat QCIF("QCIF", 176, 144);
const ImageFormat sQCIF("sQCIF", 128, 96);
const ImageFormat SIF("SIF", 320, 240);
const ImageFormat QSIF("QSIF", 160, 120);
