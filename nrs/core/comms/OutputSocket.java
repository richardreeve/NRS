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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.Observable;

/**
 * This encapsulates the operations for writing to an {@link OutputStream} for 
 * a {@link java.net.Socket} using a {@link BufferedOutputStream}.
 *
 */
public class OutputSocket
{
    private OutputStream m_outStream;
    private BufferedOutputStream m_out;
    private SocketConnectedListener m_conListener;
    
    //----------------------------------------------------------------------
    /**
     * Construct an OutputSocket for an {@link OutputStream}.
     *
     * @param out {@link OutputStream} for socket.
     *
     * @param cLstr callback for when the stream is setup. Can be null.
     *
     */
    public OutputSocket(OutputStream out, SocketConnectedListener cLstr)
    {
	m_outStream = out;
	m_conListener = cLstr;
    }
    //----------------------------------------------------------------------
    
    
    /**
     * Write the message held within the {@link ByteArrayOutputStream} to
     * the Socket.
     *
     * @param message a {@link ByteArrayOutputStream} containing the data
     * message
     *
     * @throws IOException if there is any other kind of IO problem
     */
    public void write(ByteArrayOutputStream message) throws IOException
    {
	PackageLogger.log.finer("Attempting to write "
				 + message.size()
				 + " bytes to Socket.");
	message.writeTo(m_out);
	m_out.flush();
    }
    //----------------------------------------------------------------------
    /**
     * Write a sub-selection of a byte array to the Socket.
     *
     * @param b byte array containing data to be written
     * @param off start writing from this byte in the array
     * @param len write this number of bytes
     *
     * @throws IOException if there is any other kind of IO problem
     *
     */
    public void write(byte[] b, int off, int len) throws IOException
    {
	PackageLogger.log.finer("Attempting to write "
				 + len
				 + " bytes to Socket,");
	try
	    {
		m_out.write(b, off, len);
		m_out.flush();
	    }
	catch (IOException e)
	    {
		PackageLogger.log.warning("Failed to write to Socket.");
		throw e;
	    }
    }
    //----------------------------------------------------------------------
    /**
     * Write a byte array to the {@link OutputStream}.
     *
     * @param b byte array containing data to be written
     *
     * @throws IOException if there is any other kind of IO problem
     *
     */
    public void write(byte[] b) throws IOException
    {
	write(b, 0, b.length);
    }
    //----------------------------------------------------------------------
 
    /**
     * Setup {@link OutputStream} as a {@link BufferedOutputStream}.
    */
    public void setup()
    {
	PackageLogger.log.fine("Attempting to setup OutputStream.");
	
	m_out = new BufferedOutputStream(m_outStream);
	if (m_conListener != null) m_conListener.socketConnected(this);
	
	PackageLogger.log.fine("OutputStream opened.");
    }
    
    /** Close Stream. */
    public void close() throws IOException
    {
	m_out.close();
	m_out = null;
    }
    
}
