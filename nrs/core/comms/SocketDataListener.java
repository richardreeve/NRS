/*
 * Copyright (C) 2004 Edinburgh University
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
package nrs.core.comms;

/**
 * Derived classes should implement this class so that they can be
 * registered with an InputSocket and receive data available events
 */
public interface SocketDataListener
{
  /** Indicates the <code>socket</code> has received a complete data message.
   *
   * @param socket the Socket which received the data
   *
   * @param buffer contains the bytes of the received message, including
   * the null terminating character
   *
   * @param size the size of the data message, excluding the null
   * character. The data message starts at offset <tt>0</tt> and runs
   * for <tt>size</tt> number of bytes.
   **/
  public void dataAvailable(InputSocket socket,
                            byte[] buffer,
                            int size);
};
