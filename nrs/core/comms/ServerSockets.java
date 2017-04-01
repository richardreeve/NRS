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

import java.net.ServerSocket;
import java.net.Socket;
import java.net.ConnectException;

import nrs.pml.PMLParser;
import nrs.core.base.Pipeline;

import java.io.IOException;

/**
 * This class implements Server-side implementation for NRS communication
 * over Sockets (TCP/IP). 
 * 
 * The server listens on a specified port for incoming connections and then
 * creates new SocketCommsRoute objects to represent TCP connections to NRS 
 * components.
 *
 * @author Thomas French
*/

public class ServerSockets extends Thread{
    
  private ServerSocket m_serverSocket;
  private int m_port;
  private boolean m_alive = true;
  
  /** Limit number of client connections can be made. */
  private final int MAX_CONNECTIONS = 4; 
  
  /** Current number of client connections. */
  private int m_numberOfCons = 0;

  private PortManager m_portMan;
  private Pipeline m_inbound;
  private int m_clientID = 0;
  
  /** Constructor
   *
   * @param pm PortManager to register new SocketCommsRoute objects
   * @param port port which server listen on for incoming connections
   * @param inbound {@link Pipeline} used for this component
   *
   * @throws ConnectException if port is out of range
   * @throws IOException if not able to set up {@link ServerSocket}.
   */
  public ServerSockets(PortManager pm, Pipeline inbound, int port)
    throws ConnectException, IOException
  {
    if ( port < 1 || port > 65535 )
      throw new ConnectException("Port number is out of range: 1 to 65535");
    
    m_port = port;
    
    if ( pm == null )
      throw new NullPointerException("PortManager can't be null");
    if ( inbound == null )
      throw new NullPointerException("InboundPipeline can't be null");
    
    m_inbound = inbound;
    m_portMan = pm;
    
    //setup ServerSocket
    m_serverSocket = new ServerSocket(m_port);
    
    PackageLogger.log.fine("ServerSocket setup on port: " + m_port);
    
    if ( m_serverSocket != null )
      this.start();
  }
  
  /* Run as thread, listening for connections. */
  public void run(){
    Socket socket = null;
    String n = null;
    PMLParser pmlParser;
    
    PackageLogger.log.fine("ServerSocket to start listening for" 
                           + " connections on port: " 
                           + m_serverSocket.getLocalPort());
    while(m_alive){
      try{
        //blocks, and waits for connections
        socket = m_serverSocket.accept();
	
        if ( m_numberOfCons == MAX_CONNECTIONS ){
          PackageLogger.log.info("Maximum number of client " 
                                 + "connections has been made.");
          
          //Send message to client explaining situation?
          socket.close();
          
          continue;
        }
        m_numberOfCons++;
        
        PackageLogger.log.fine("ServerSocket accepted connection from: "
                               + socket.getInetAddress() + " on port: " 
                               + socket.getPort());
        
        n = "Socket_"+m_clientID++;
	
        //Every incoming connection gets threaded
        ServerWorker w = new ServerWorker(m_portMan.getFreePortID(), 
                                          n, socket, m_portMan, this,
                                          m_inbound);
      }
      catch(Exception e){
        if ( m_serverSocket == null || m_serverSocket.isClosed() ){
          PackageLogger.log.severe("Server Socket has gone down.");
          break;
        }
	
        e.printStackTrace();
      }
    }
    
    //shutdown severSocket, etc.
    try{
      if ( !m_serverSocket.isClosed() )
        m_serverSocket.close();
    }
    catch(IOException ioe){
      //PackageLogger.log.fine(ioe.getMessage());
      //ioe.printStackTrace(); //only for debugging
    }
  }
  
  /** Shutdown server. */
  public void shutdown(){
    m_alive = false;
  }
  
  /** Decrement number of clients counter when connection is closed. */
  public void decrementNumCons(){
    m_numberOfCons--;
  }
}
/**
   Used by the ServerSocket to spawn threads to do work.
*/
class ServerWorker extends Thread
{
  private SocketCommsRoute scr = null;
  private int c;
  private String name;
  private Socket s;
  private PortManager pm;
  private ServerSockets server;
  private Pipeline inbound;
  
  public ServerWorker(int c, String name, Socket s, 
                      PortManager pm, ServerSockets server,
                      Pipeline inbound){
    this.c = c;
    this.name = name;
    this.s = s;
    this.pm = pm;
    this.server = server;
    this.inbound = inbound;
    
    this.start();
  }
  
  public void run(){
    try
      {
        scr = new SocketCommsRoute(c, name, s, server);
      }
    catch(IOException ioe){
      PackageLogger.log.warning(ioe.getMessage());
      ioe.printStackTrace();
    }
    
    PMLParser pml = null;
    try{
      pml = new PMLParser();
    }catch(Exception e){
      PackageLogger.log.warning(e.getMessage());
      e.printStackTrace();
    }
    
    scr.setMesssageListener(pml);
    pml.setMessageCallbacks(scr, inbound);
    pm.addPort(scr);
    
    scr.open();
    if ( scr.isUp() )
      PackageLogger.log.fine("SocketCommsRoute has been established: " 
                             + name);
  }
}
