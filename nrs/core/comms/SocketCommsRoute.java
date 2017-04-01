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
import java.io.IOException;

import java.net.Socket;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This class is a concrete implementation of the {@link CommsRoute}
 * interface using Sockets (TCP/IP) to transmit and receive sequences
 * of bytes for the underlying communication mechanism to exchange
 * messages to a local/remote NRS component.
 *
 * This class represents a single {@link Socket} only.
 *
 * Based on Darren Smith's FIFOCommsRoute class.
 * 
 * @author Thomas French
 */

public class SocketCommsRoute extends CommsRoute 
    implements SocketConnectedListener, SocketDataListener
{
  /** Socket over which bi-directional communication takes place.*/
  private Socket m_socket = null;
  
  private InputSocket m_input = null;
  private OutputSocket m_output = null;
  
  private String m_host;
  private int m_port;
  
  private boolean m_isClient = false;   
  private ServerSockets m_server = null;
  
  /** Client-side constructor
   * @param ID the locally unique ID associated with this route / port 
   * and used for wormhole routing.
   * @param name a text label to associate with this route / port
   * @param host IP address of remote host to connect to
   * @param port remote port of remote host to connect to 
   *
   * @throws NullPointException if host string is empty, it can't be
   * @throws ConnectException if port number is out of acceptable range
   *
   */
  public SocketCommsRoute(int ID, String name, String host, int port)
    throws NullPointerException, ConnectException{
    super(ID, name);
    
    if (host == null) 
    throw new NullPointerException("Hostname can't be null");
    if ( port < 1 || port > 65535 )
    throw new ConnectException("Port number is out of range:" 
                               + " 1 to 65535");
    m_host = host;
    m_port = port;
    
    m_isClient = true;
  }
  
  /** Server-side constructor - from spawned client Socket.
   * @param ID the locally unique ID associated with this route / port 
   * and used for wormhole routing.
   * @param name a text label to associate with this route / port
   * @param socket {@link Socket} for which communication takes place over
   *
   * @throws NullPointException if socket is null, it can't be.
   * @throws SocketException if socket is non-null but not connected.
   * @throws IOException if can't get streams from socket 
   *
   */
  public SocketCommsRoute(int ID, String name, Socket socket, 
                          ServerSockets server)
    throws NullPointerException, SocketException, IOException
  {
    super(ID, name);
    
    if ( socket == null )
    throw new NullPointerException("Socket can't be null");
    if ( server == null )
    throw new NullPointerException("Server can't be null");
    
    m_socket = socket;
    m_server = server;
    
    //check that socket is connected
    if ( m_socket.isConnected() && !m_socket.isInputShutdown() 
         && !m_socket.isOutputShutdown() ){
      m_input = new InputSocket(m_socket.getInputStream(),this,this);
      m_output = new OutputSocket(m_socket.getOutputStream(),this);
    }
    else
    throw new SocketException("Socket not connected");
  }
  
  /**
   * {@inheritDoc}
   */
  public boolean isUp(){
    if ( m_socket == null ) return false;
    
    return ( m_socket.isConnected() && !m_socket.isInputShutdown() 
             && !m_socket.isOutputShutdown() );
  }
  //----------------------------------------------------------------------
  /**
   * {@inheritDoc}
   */
  public void open(){
    //need to open socket and setup streams
    if ( m_isClient ){
      try
      {
        m_socket = new Socket(m_host, m_port);
        PackageLogger.log.fine("Socket opened on" + m_host 
                               + " on port: " + m_port);
      }
      catch(UnknownHostException uhe){
        PackageLogger.log.warning(uhe.getMessage());
        uhe.printStackTrace();
        
        //System.exit(1); //for debugging.
      }
      catch(IOException ioe){
        PackageLogger.log.warning(ioe.getMessage());
        ioe.printStackTrace();
        
        //System.exit(1); //for debugging
      }
      
      if ( m_socket != null && m_socket.isConnected() &&
           !m_socket.isInputShutdown() && !m_socket.isOutputShutdown() 
           && m_input == null && m_output == null ){
        try{
          m_input = new InputSocket(m_socket.getInputStream(),this,this);
          m_output = new OutputSocket(m_socket.getOutputStream(),this);
        }
        catch(IOException ioe){
          PackageLogger.log.warning(ioe.getMessage());
          ioe.printStackTrace();
        }
      }
    }
    
    //setup streams
    if ( m_socket != null && m_socket.isConnected() && 
         !m_socket.isInputShutdown() && !m_socket.isOutputShutdown() ){
      m_output.setup();
      m_input.setup();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Write the message held within the {@link ByteArrayOutputStream} to
   * the port.
   *
   * @param message a {@link ByteArrayOutputStream} containing the data
   * message
   *
   * @throws IOException if there is any other kind of IO problem
   */
  public void write(ByteArrayOutputStream message)
    throws IOException
  {
    if ( m_output != null )
    m_output.write(message);
    else
    PackageLogger.log.warning("Not able to write data out over socket: " 
                              + getName());
  }
  //----------------------------------------------------------------------
  /**
   * {@inheritDoc}
   */
  public void dataAvailable(InputSocket socket,
                            byte[] buffer,
                            int size)
  {
    PackageLogger.log.fine("Message received on port \""
                           + this + "\" ( length " + size + " bytes)");
    
    if (m_msgListener != null)
    {
      m_msgListener.messageEvent(buffer, size, this);
    }
    else
    {
      PackageLogger.log.warning("Message received on port \""
                                + this + "\" discarded because no"
                                +" callback has been registered");
    }
    
    //callback has expired, so restart the reading in procedure
    socket.startRead();
  }
  //----------------------------------------------------------------------
  /**
   * {@inheritDoc}
   */
  public void socketConnected(Object socket)
  {
    if (socket == m_input)
    {
      PackageLogger.log.finest("Start input read...");
      m_input.startRead();
    }
    
    if (isUp())
    {
      generateConnectionEvent(true);
    }
  }
  //----------------------------------------------------------------------
  /**
   * {@inheritDoc}
   */
  public void socketClosed(Object socket)
  {
    try{
      m_input.close();
      m_output.close();
      m_socket.close();
    }
    catch(IOException ioe){
      PackageLogger.log.warning(ioe.getMessage());
    }
    
    // decrement number of connections server has
    if ( !m_isClient && m_server != null ){
      m_server.decrementNumCons();
    }
    
    PackageLogger.log.info("Socket closed: " + getName() );
    
    // need to remove CommsRoute from PortManager.
    generateConnectionEvent(false);
  }
  //----------------------------------------------------------------------
}
