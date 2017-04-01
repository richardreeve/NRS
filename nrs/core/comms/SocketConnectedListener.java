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
 * registered when a {@link java.net.Socket} Input or Output stream has become 
 * opened or closed.
 *
 * @author Thomas French
 * @author Darren Smith
 */
public interface SocketConnectedListener
{
  /** Indicates the Sockets streams are open. */
  public void socketConnected(Object socket);

  /** Indicates the Socket streams have been closed by the other host. */
  public void socketClosed(Object socket);
};
