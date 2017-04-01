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
package nrs.composer;

import nrs.core.base.MessageProcessor;
import nrs.core.base.BaseAppManager;
import nrs.core.base.Message;

import java.util.Vector;

/** 
 *  Intercepts {@link Message} objects from the InboundPipeline
 *  and hands them to the {@link MainFrame} to be displayed in the table
 *
 *  @author Sarah Cope
*/


class MessageInterceptor extends MessageProcessor implements Runnable {

    private MainFrame mainFrame;
    private Vector<Message> messagesReceived; 
    private volatile Thread thread = null;
    protected boolean canEnter = true;

    /** Constructor */
    public MessageInterceptor(MainFrame m) {
        super();
        messagesReceived = new Vector<Message>();
        mainFrame = m;
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
      }
    }

    /** Adds received messages to a Vector until they are processed*/
    public void deliver(Message m, MessageProcessor sender) {
        if (m != null ) {
            if (mainFrame.intercepting) {
                synchronized (messagesReceived) {
                    messagesReceived.add(m);
                    mainFrame.m_recListModel.addElement(m);
                }
            } else {
                while(!messagesReceived.isEmpty()) {
                    Message mess = (Message) messagesReceived.remove(0);
                    mainFrame.m_recListModel.remove(0);
                    mainFrame.m_sentListModel.insertElementAt(mess, 0);
                    next(mess);
                }
                mainFrame.m_sentListModel.insertElementAt(m, 0);
                next(m);
            }
        }
    }
   
    /** Removes received {@link Message} from the Vector for processing */
    public void run() {

        while (true) {
            while (!messagesReceived.isEmpty() && canEnter) {
                
                synchronized(messagesReceived) {   
                    canEnter = false;
                    Message m = (Message) messagesReceived.remove(0);
                    mainFrame.m_message = m;
                    mainFrame.intercepted = true;
                    mainFrame.m_receivedList.setSelectedIndex(0);
                    mainFrame.wasSent = false;
                    mainFrame.displayMessage(m);
                }
                   
            }
        }
    }

    // Called by MainFrame once the message has been displayed
    // and the send button has been pressed
    protected void sendEditedMessage(Message m) {
        
        if (m.aux().getReceivedPort() != null)
            {
                PackageLogger.log.fine("Received " + m.diagString()
                                       + " on port:" + m.aux().getReceivedPort());
            }
        else
            {
                PackageLogger.log.fine("Received " + m.diagString()
                                       + " on port: NOT AVAILABLE");
            }
        
        PackageLogger.log.info("Sent edited message: " + m.diagString());

        if(!mainFrame.m_recListModel.isEmpty()) {
            Message message = (Message) mainFrame.m_recListModel.remove(0);
            mainFrame.m_sentListModel.insertElementAt(message, 0);
        }
        next(m);
    }


}
