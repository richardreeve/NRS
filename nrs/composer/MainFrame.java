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

import nrs.toolboxes.SocketConnectionWindow;
import nrs.toolboxes.FIFOConnectionWindow;

import nrs.core.comms.FIFOCommsRoute;
import nrs.core.base.Message;
import nrs.core.base.OutboundPipeline;
import nrs.core.base.MessageTools;
import nrs.core.base.message.*;
import nrs.core.message.*;

import nrs.util.ExampleFileFilter;

import nrs.pml.PMLParser;
import nrs.pml.PML_Element_Registry;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JSplitPane;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.ListSelectionModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Properties;
import java.util.prefs.Preferences;



/** Build GUI of Composer component node.
 *
 * @author Sarah Cope
 * @author Thomas French
 */
public class MainFrame implements ActionListener, 
                                  ItemListener{

    /** Singleton instance of <tt>MainFrame</tt> class. */
    private static MainFrame m_singletonInstance = null;

    private static final int WIDTH = 700;
    private static final int HEIGHT = 350;
    private static final String WIN_X = "MAINF_BROSWER_DIALOG_WIN_X";
    private static final String WIN_Y = "MAINF_BROSWER_DIALOG_WIN_Y";
    private static final String WIN_WIDTH = "MAINF_BROSWER_DIALOG_WIN_WIDTH";
    private static final String WIN_HEIGHT = "MAINF_BROSWER_DIALOG_WIN_HEIGHT";
    private static final Rectangle maxSize = new Rectangle(700, 400);
    private static final int numOfTypes = 12;
    private static final String [] messageTypes = {"CreateNode", "DeleteNode", "CreateLink", "DeleteLink",
                                                   "QueryMaxLink", "ReplyMaxLink", "QueryVNID", "ReplyVNID", 
                                                   "QueryVNName", "ReplyVNName", "QueryVNType", "ReplyVNType"};
    

    protected Message m_message;
    private OutboundPipeline m_outPipeline;
    private MessageInterceptor m_messageInterceptor;
    private JButton m_messageButton, m_nextButton, m_selectButton;
    private JPanel m_topPanel, m_bottomPanel,
                   m_savePanel, m_loadPanel,
                   m_templatePanel, m_mainPanel,
                   m_listPanel;
    private JComboBox m_messageTypes;
    private JLabel m_templateLabel, m_typeLabel, 
                   m_receivedLabel, m_sentLabel;
    private JTextField m_typeField;
    private JTable m_table;
    private JScrollPane m_mainPane, m_listPane, 
                        m_tablePane, 
                        m_recListPane, m_sentListPane;
    private JFrame m_frame;
    private JSplitPane m_splitPane;
    private JPopupMenu m_editRows;
    private JCheckBox m_checkBox, m_checkBox2;
    private JMenuBar m_menuBar;
    private JMenu m_file, m_connect;
    private JMenuItem m_exit, m_fifo, m_socket,
                      m_save, m_load, m_clearTable, 
                      m_clearSent,
                      m_addRow, m_deleteRow, m_clearRows;
    protected JList m_receivedList, m_sentList;
    protected DefaultListModel m_recListModel, m_sentListModel;
    private AppManager m_appManager;
    private MessageToXML toXMLConvert;
    private XMLToMessage toMessageConvert;

    private final int COLUMNS = 3;
    private int ROWS = 12; 
    private int m_unique_id = 0;

    // Indicates when the table has been cleared
    private boolean tableEmpty;
    private boolean connectedFIFOs = false;
    private boolean connectedSocket = false;
    
    // Indicates whether the message was
    // intercepted or composed by the user
    protected boolean intercepted = false;

    // Indicates whether the messages should 
    // be displayed in the table
    protected boolean intercepting = true;

    // Indicates whether an indicated message was sent
    // or the user cleared the table
    protected boolean wasSent = false;

    // Indicates whether user must click 
    // m_nextButton to view next message
    private boolean clearBetween = false;

    private String cid = "";
    private HashMap m_messageTemplate; 
    private static JFileChooser m_fileDialog;
    

    
    /**
     * Return the singleton instance of class AppManager.
     *
     * @return singleton instance of class AppManager.
     */
    public static MainFrame getInstance(){
    	if (m_singletonInstance == null) new MainFrame();
	return m_singletonInstance;
    }

    /**
     * Constructor - uses singleton pattern.
     */
    private MainFrame(){
   	if (m_singletonInstance == null) m_singletonInstance = this;

	m_appManager = AppManager.getInstance();

        m_outPipeline = m_appManager.getOutboundPipeline();

        if (m_fileDialog == null)
            {
                m_fileDialog = new JFileChooser();
                ExampleFileFilter filter = new ExampleFileFilter();
                filter.addExtension("xml");
                filter.setDescription("NRS Messages");
                m_fileDialog.setFileFilter(filter);
            }    
        toXMLConvert = MessageToXML.getInstance();
        toMessageConvert = XMLToMessage.getInstance();

	m_frame = new JFrame("NRS.composer");
	loadPrefs();
	populateHardcoded();
    }

    /** Builds GUI for <code>MainFrame</code> */
    public void buildGUI(){
	m_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	//Add window listener to shutdown properly
	m_frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    shutdown(0);
		}
	    });

	//build menubar, menu and menuitems
	m_menuBar = new JMenuBar();
	m_file = new JMenu("File");
	m_file.setMnemonic(KeyEvent.VK_F);

        m_save = new JMenuItem("Save Message As...", KeyEvent.VK_S);
        m_save.addActionListener(this);


        m_load = new JMenuItem("Load Message", KeyEvent.VK_O);
        m_load.addActionListener(this);

        m_clearTable = new JMenuItem("Clear Table", KeyEvent.VK_C);
        m_clearTable.addActionListener(this);

        m_clearSent = new JMenuItem("Clear Sent Messages");
        m_clearSent.addActionListener(this);

	m_connect = new JMenu("Connect");
	m_connect.setMnemonic(KeyEvent.VK_C);

	m_fifo = new JMenuItem("Local (FIFO)...", KeyEvent.VK_L);
	m_fifo.addActionListener(this); 

	m_socket = new JMenuItem("Remote (TCP/IP)...", KeyEvent.VK_R);
	m_socket.addActionListener(this);

	m_exit = new JMenuItem("Exit", KeyEvent.VK_E);
	m_exit.addActionListener(this);
       

        // Build checkbox
        m_checkBox = new JCheckBox("Intercepting");
        m_checkBox.setSelected(true);
        m_checkBox.addItemListener(this);

        // Build checkbox
        m_checkBox2 = new JCheckBox("Clear table between");
        m_checkBox2.setSelected(false);
        m_checkBox2.addItemListener(this);

        // Build right side of split panel
        m_typeLabel = new JLabel("Message Type: ");
        m_typeField = new JTextField(20); 
        
        m_topPanel = new JPanel();
        m_topPanel.add(m_typeLabel);
        m_topPanel.add(m_typeField);
        m_topPanel.add(m_checkBox);
        
        m_table = new JTable(new MessageTableModel());
        m_tablePane = new JScrollPane(m_table);
        m_tablePane.setPreferredSize(new Dimension(200, 130));

        m_templateLabel = new JLabel("Templates: ");
        m_messageTypes = new JComboBox(messageTypes);
        m_messageTypes.addActionListener(this);

        m_templatePanel = new JPanel();
        m_templatePanel.add(m_templateLabel);
        m_templatePanel.add(m_messageTypes);

        m_nextButton = new JButton("View Next");
        m_nextButton.addActionListener(this);

        m_messageButton = new JButton("Send");
        m_messageButton.addActionListener(this);
        
        m_bottomPanel = new JPanel();
        m_bottomPanel.setLayout(new BoxLayout(m_bottomPanel, BoxLayout.X_AXIS));
        m_bottomPanel.add(m_templatePanel);
        m_bottomPanel.add(m_nextButton);
        m_bottomPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        m_bottomPanel.add(m_messageButton);

        createPopupMenu();
       
        m_mainPanel = new JPanel();
        m_mainPanel.setLayout(new BorderLayout());
        m_mainPanel.add(m_topPanel, BorderLayout.NORTH);
        m_mainPanel.add(m_tablePane, BorderLayout.CENTER);
        m_mainPanel.add(m_bottomPanel, BorderLayout.SOUTH);

        m_mainPane = new JScrollPane(m_mainPanel);

        // Build left side of split panel
        m_receivedLabel = new JLabel("Messages Received: ");
        m_recListModel = new DefaultListModel();
        m_receivedList = new JList(m_recListModel);
        m_recListPane = new JScrollPane(m_receivedList);
        m_recListPane.setMaximumSize(new Dimension(150, 100));
       
        m_sentLabel = new JLabel("Messages Sent: ");
        m_sentListModel = new DefaultListModel();
        m_sentList = new JList(m_sentListModel);
        m_sentListPane = new JScrollPane(m_sentList);
        m_sentListPane.setMaximumSize(new Dimension(150, 100));

        m_selectButton = new JButton("View selected");
        m_selectButton.addActionListener(this);

        m_listPanel = new JPanel();
        m_listPanel.setLayout(new BoxLayout(m_listPanel, BoxLayout.Y_AXIS));
        m_listPanel.add(m_receivedLabel);
        m_recListPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        m_listPanel.add(m_recListPane);
        m_listPanel.add(Box.createRigidArea(new Dimension(10, 10)));
        m_listPanel.add(m_sentLabel);
        m_sentListPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        m_listPanel.add(m_sentListPane);
        m_listPanel.add(m_selectButton);
        m_listPanel.add(m_checkBox2);
        m_listPanel.setPreferredSize(new Dimension(180, 350));

        m_listPane = new JScrollPane(m_listPanel);

       
        //setup split panel
        m_splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
                                     m_listPane, m_mainPane);
        m_splitPane.setOneTouchExpandable(true);
        m_splitPane.setDividerLocation(200);
        
        //Provide minimum sizes for the two components in the split pane.
        Dimension minimumSize = new Dimension(100, 50);
        m_listPane.setMinimumSize(minimumSize);
        m_mainPane.setMinimumSize(minimumSize);
        m_mainPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        m_splitPane.setBorder(BorderFactory.createEtchedBorder());
        
        // Add menu items to menus                                                   
        m_file.add(m_save);
        m_file.add(m_load);
        m_file.add(m_clearTable);
        m_file.add(m_clearSent);
        m_file.add(m_exit);
	m_connect.add(m_fifo);
	m_connect.add(m_socket);

        // Add menus to menubar
	m_menuBar.add(m_file);
	m_menuBar.add(m_connect);

        m_splitPane.setOpaque(true);
             
	m_frame.setJMenuBar(m_menuBar);
	m_frame.setContentPane(m_splitPane);  
        m_frame.setMaximizedBounds(maxSize);
	m_frame.setSize(MainFrame.WIDTH,MainFrame.HEIGHT);
	m_frame.setVisible(true);
    }
        

    // Handles CheckBox events
    public void itemStateChanged(ItemEvent e) {
        boolean result;
        if (e.getStateChange() == ItemEvent.DESELECTED) {
            result  = false;
        } else {
            result = true;
        }
        // Intercepting checkbox
        if (e.getSource() == m_checkBox) {
            intercepting = result;

        // clearBetween checkbox
        } else if (e.getSource() == m_checkBox2){
            clearBetween = result;
        }
    }


    public void actionPerformed(ActionEvent e) {

        // Handle load button action.
        if (e.getSource() == m_load) {
            
            try {
            
            loadMessage();
            
            } catch (IOException ex) {
                PackageLogger.log.warning(ex.getMessage());
            }

            // Handle save button action.
        } else if (e.getSource() == m_save) {

            try {

            saveMessage();
           
            } catch (IOException ex) {
                PackageLogger.log.warning(ex.getMessage());
            }

            // Clear the table
        } else if (e.getSource() == m_clearTable) {
            
            if (intercepted && !wasSent) {
                m_recListModel.remove(0);
            }

            clearTable();
           
            // Clear sent messages list
        } else if (e.getSource() == m_clearSent) {
            
            m_sentListModel.clear();

            // Local connection
        } else if (e.getSource() == m_fifo) {
            if (!connectedFIFOs ){
                if (!setupLocalConnection() ){
                    return;			   
                }
                //else{
                //  fifosUp();
                //}
            }

            // Remote connection
        } else if (e.getSource() == m_socket) {
            if (!connectedSocket ){
                if (!setupRemoteConnection() ){
                    return;			   
                }
                //else{
                //  socketsUp();
                //}
            }
            
            // Exit
        } else if (e.getSource() == m_exit) {
            shutdown(0);
           
            // Fills in Message template selected in JComboBox
        } else if (e.getSource() ==  m_messageTypes) {

            if (!tableEmpty & intercepted && !wasSent) {
                 m_recListModel.remove(0);
            }

            // Get the message type
            String messType = (String) m_messageTypes.getSelectedItem();
            ArrayList fieldEntries = (ArrayList) m_messageTemplate.get(messType);
            Iterator it = fieldEntries.iterator();
            
            
            // Check the table curently has enough rows to hold the data
            MessageTableModel m = (MessageTableModel) m_table.getModel();
            int currentRows = m.getRowCount();
            int rowEntries = fieldEntries.size();
            
            
            // Cannot use "row = new Object[3]" as 
            // null pointer exception from getClass()
            // in the TableModel
            Object[] row = {"", "", new Boolean(false)};
            
            if( rowEntries > currentRows) {
                int diff = rowEntries - currentRows;
                for (int i = 0; i < diff; i++ ){  
                    m.addRowAt(i, row);
                }
            } else {
                int diff = currentRows - rowEntries;
                for (int i = diff; i > 0; i-- ){
                    m.deleteRowAt(i);
                }
            }
            
            m_typeField.setText(messType);
            intercepted = false;
            
            // Fill in the table from the message template
            for (int i = 0; i < ROWS; i++) {
                
                if (it.hasNext() ) {
                    FieldEntry f = (FieldEntry) it.next();
                    Object[] row1 = {f.getFieldName(), "", f.getInNRS()};
                    m.setRowAt(i, row1);
                }
            }
            tableEmpty = false;
            m_table.repaint();


        } else if (e.getSource() == m_nextButton) {

           
            if(tableEmpty) {            
                m_messageInterceptor.canEnter = true;
            }

            // Send the message
        } else if (e.getSource() == m_messageButton) {

           
             TableCellEditor c = m_table.getCellEditor();
             if (c != null) {
                 tableEmpty = false;
             }

            // Checks that another message has been  
            // accepted into the table after clearing
            if (!tableEmpty) {

                if (!intercepted) {
                    
                    Message m = readFromTable();

                    // Fix the route
                    MessageTools.fixRoute(m, cid);
                    
                    // Submit to OutboundPipeline
                    if (m_outPipeline != null) {
                        m_outPipeline.deliver(m, null);
                    }

                    PackageLogger.log.info("Sent composed message: " + m.diagString());
                    m_sentListModel.insertElementAt(m, 0);

                    clearTable();
                    
                } else { 
        
                    m_messageInterceptor.sendEditedMessage(editFromTable(m_message));
                    wasSent = true;
                    if (!clearBetween) {
                        m_messageInterceptor.canEnter = true;
                    } else {
                        clearTable();
                    }
                     
                }
            }
            
            // Display selected message from sent list
        } else if (e.getSource() == m_selectButton) {
                                   
            Message selected = (Message) m_sentList.getSelectedValue();
            if (selected != null) {
                displayMessage(selected);
                tableEmpty = false;
            }
    
            // Add row
        } else if (e.getSource() == m_addRow) {

            int index = m_table.getSelectedRow();
            
            // Add a new blank row to the table at index
            if (index != -1) {
                Object[] row = {"", "", new Boolean(false)};
                MessageTableModel m = (MessageTableModel) m_table.getModel();
                m.addRowAt(index, row);
                m_table.repaint();
            }

            // Delete row
        } else if (e.getSource() == m_deleteRow) {
            int index = m_table.getSelectedRow();
            
            // Delete the row at index
            if (index != -1) {
                MessageTableModel m = (MessageTableModel) m_table.getModel();
                m.deleteRowAt(index);
                m_table.repaint();
            }

            // Clear Rows
        } else if (e.getSource() == m_clearRows) {
            
            MessageTableModel m = (MessageTableModel) m_table.getModel();
            int [] indices = m_table.getSelectedRows();
            m.clearRows(indices);
            m_table.repaint();
        }
            
    }

 /** Creates a new {@link Message} object from the table entries */
    public Message readFromTable() {

        TableCellEditor c = m_table.getCellEditor();
        if (c != null) {
            c.stopCellEditing();
        }
        
        // Create new message object
        Message message = new Message();  

        message.setType((String) m_typeField.getText());

        // Boolean to check that the message has a route field
        Boolean routeGiven = false; 


        // Set message object fields from the table entries                    
        for(int i = 0; i < ROWS; i++){
            
            Boolean nrsField = (Boolean) m_table.getValueAt(i, COLUMNS-1);
            String fieldValue = (String) m_table.getValueAt(i, COLUMNS-2);
            String fieldName = (String) m_table.getValueAt(i, COLUMNS-3);
            
            if (nrsField != null && fieldValue != null && fieldName != null 
                && !fieldName.equals("")) {
                
                if (fieldName.equals("route")) {

                    routeGiven = true;
                }

                // Used for fixRoute if the message is composed
                // and not intercepted
                if (fieldName.equals("CID")) {
                    cid = fieldValue;
                }

                if(!nrsField){
                    message.setField(fieldName, fieldValue);
                } else {
                    message.setNRSField(fieldName, fieldValue);
                }
            }   
        }
        
        // If no route is given, set to the empty string 
        if (!routeGiven) {
            message.setNRSField("route", " ");
        }

        return message;
    }

    /** Clears all rows in the table */
    public void clearTable() {

        MessageTableModel m = (MessageTableModel) m_table.getModel();
        int [] allRows = new int[ROWS];
        for(int i = 0; i < ROWS; i++) {
            allRows[i] = i;
        }
        // Clear the message from the table
        m.clearRows(allRows);
        m_typeField.setText("");
        tableEmpty = true;
        intercepted = false;
        m_table.repaint();

    }


    // Edits an existing intercepted Message object 
    // so that AuxillaryInfo is kept
    private Message editFromTable(Message message) {
        TableCellEditor c = m_table.getCellEditor();
        if (c != null) {
            c.stopCellEditing();
        }

        // Set message object fields from the table entries                    
        for(int i = 0; i < ROWS; i++){
            
            Boolean nrsField = (Boolean) m_table.getValueAt(i, COLUMNS-1);
            String fieldValue = (String) m_table.getValueAt(i, COLUMNS-2);
            String fieldName = (String) m_table.getValueAt(i, COLUMNS-3);
            
            if (nrsField != null && fieldValue != null && fieldName != null 
                && !fieldName.equals("")) {
                
                
                if(!nrsField){
                    message.setField(fieldName, fieldValue);
                } else {
                    message.setNRSField(fieldName, fieldValue);
                }
            }   
        }
       
        return message;
    }
        
    /** Displays a {@link Message} object in the table */
    public void displayMessage(Message m) {

        // Required when processing intercepted messages
        // to indicate when the table can accept a new message
        // using the m_nextButtton
        tableEmpty = false;

        HashMap fieldsMap = m.getFields();
        HashMap nrsFieldsMap = m.getNRSFields();

        Iterator fields = fieldsMap.keySet().iterator();
        Iterator nrsFields = nrsFieldsMap.keySet().iterator();

        
        //Check the table currently has enough rows to hold the data
        MessageTableModel tabModel = (MessageTableModel) m_table.getModel();
        int currentRows = tabModel.getRowCount();
        int rowEntries = fieldsMap.size() + nrsFieldsMap.size();
        if( rowEntries > currentRows) {
            int diff = rowEntries - currentRows;
            for (int i = 0; i < diff; i++ ){

                // Cannot use "row = new Object[3]" as 
                // null pointer exception from getClass()
                // in the TabelModel
                Object [] row1 = {"", "", new Boolean(false)};
                tabModel.addRowAt(i, row1);
            }
        } else {
            int diff = currentRows - rowEntries;
            for (int i = diff; i > 0; i-- ){
                tabModel.deleteRowAt(i);
            }
        }
         
        // Set the field values in the table
        int index = 0;
        Boolean nrs = new Boolean(true);

        while(nrsFields.hasNext()) {
            String fieldName = (String) nrsFields.next();
            String fieldValue = (String) nrsFieldsMap.get(fieldName);
            if (fieldName.startsWith("nrsa:")) {
                fieldName = fieldName.substring(5);
            }
            Object[] row = {fieldName, fieldValue, nrs};

            tabModel.setRowAt(index, row);
            index++;
        }

        nrs = new Boolean(false);

        while(fields.hasNext()) {
            String fieldName = (String) fields.next();
            Object[] row = {fieldName, fieldsMap.get(fieldName), nrs};

            tabModel.setRowAt(index, row);
            index++;
        }
        
      
        

        // Set the message type
        m_typeField.setText(m.getType());
        
        m_table.repaint();

    }

    

    // Load a Message into the table
    private void loadMessage() throws IOException {
            
        m_savePanel = new JPanel();
        
        int returnVal = m_fileDialog.showOpenDialog(m_savePanel);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = m_fileDialog.getSelectedFile();
            Message m = toMessageConvert.convert(file);
            
            
            if (m != null ) {
                
                displayMessage(m);
                
            } else { 
                PackageLogger.log.warning("No such file");
            }
            
        } else {
            PackageLogger.log.info("Open command cancelled by user.");
        }    
        
    }
    
    // Save the current Message
    private void saveMessage() throws IOException{
        
         m_loadPanel = new JPanel();

            int returnVal = m_fileDialog.showSaveDialog(m_loadPanel);

            if (returnVal == JFileChooser.APPROVE_OPTION) {

                FileWriter target = new FileWriter(m_fileDialog.getSelectedFile());
                
                if (target != null) {
                    
                    Message m = readFromTable();
                    
                    System.out.println(m.diagString());

                    toXMLConvert.convert(m, target);
                    

                }

                
            } else {

                 PackageLogger.log.info("Save command cancelled by user.");
            }
    }

    // TableModel for JTable
    class MessageTableModel extends AbstractTableModel {
       
        private String[] m_columnNames = {"FieldName",
                                        "FieldValue",
                                        "NRS Field"};


        private Object[][] m_tableData = new Object[ROWS][COLUMNS];


        private MessageTableModel() {
            
            super();
            
            // Initialise table data to allow
            // getColumnClass to specify the default 
            // renderer for each cell
            for (int i = 0; i < ROWS; i++){

                Object[] row = {"", "", new Boolean(false)};
                m_tableData[i] = row;
            }

        }
            
        public int getColumnCount() {
            return m_columnNames.length;
        }

        public int getRowCount() {
            return m_tableData.length;
        }

        public String getColumnName(int col) {
            return m_columnNames[col];
        }


        public Object getValueAt(int row, int col) {
            return m_tableData[row][col];
        }

        // Required to display checkboxes
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        
        public boolean isCellEditable(int row, int col) {
            return true;   
        }
        
        public void setValueAt(Object value, int row, int col) {     
            m_tableData[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        public void setRowAt(int index, Object[] row) {
            m_tableData[index] = row;
        }
        
        public Object[] getRowAt(int index) {
            return m_tableData[index];
        }
        
        public void clearRows(int[] indices) {
            for (int i = 0; i < indices.length; i++ ) {
                Object [] row = {"", "", new Boolean(false)};
                m_tableData[indices[i]] = row;
            }
        }

        public void deleteRowAt(int index) {
            // First row is used for getColumnClass can't be deleted
            // Minimum table size 2 row, to avoid null pointer exceptions
            if (ROWS > 2  &&  index > 0) {
                Object[][] tableData = new Object[--ROWS][COLUMNS];
                for (int i = 0; i < ROWS; i++) {
                    if (i != index) {
                        tableData[i] = m_tableData[i];
                    }
                }
                m_tableData = tableData;
            }
        }

        public void addRowAt(int index, Object[] row) {
            Object[][] tableData = new Object [++ROWS][COLUMNS];
            int temp = 0;
            for (int i = 0; (i < ROWS) && (temp < ROWS-1); i++){
                if (i != index) {
                    tableData[i] = m_tableData[temp];
                    temp++;
                } else {
                    tableData[i] = m_tableData[temp];
                    tableData[++i] = row;
                    temp++;
                }
            }
            m_tableData = tableData;
        }
    }

    // Creates menu for adding and deleting rows
    private void createPopupMenu() {
        m_editRows = new JPopupMenu();
        m_addRow = new JMenuItem("Add row");
        m_addRow.addActionListener(this);

        m_deleteRow = new JMenuItem("Delete selected row");
        m_deleteRow.addActionListener(this);

        m_clearRows = new JMenuItem("Clear selected rows");
        m_clearRows.addActionListener(this);

        m_editRows.add(m_addRow);
        m_editRows.add(m_deleteRow);
        m_editRows.add(m_clearRows);

        MouseListener popupListener = new PopupListener(m_editRows);
        m_table.addMouseListener(popupListener);
    }



    // Listener for popup menu
    class PopupListener extends MouseAdapter {
        JPopupMenu editRows;

        PopupListener(JPopupMenu m_editRows) {
            editRows = m_editRows;
        }

        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        public void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                editRows.show(e.getComponent(),
                           e.getX(), e.getY());
            }
        }
    }

   
    public MessageInterceptor getMessageInterceptor() {
        return m_messageInterceptor;
    }

    public void setMessageInterceptor(MessageInterceptor m) {
        m_messageInterceptor = m;
    }
   
    public void setVisible(){
	m_frame.setVisible(true);
    }

    public void setTitle(String title){
	m_frame.setTitle(title);
    }

    public void addBt(JComponent b){
	m_splitPane.add(b);
	m_splitPane.add(Box.createRigidArea(new Dimension(0, 10)));
	m_frame.pack();
    }
    public void removeBt(JComponent b){
	m_splitPane.remove(b);

	removeBoxes();

	if ( m_splitPane.getComponentCount() == 0 ){
	    m_frame.setSize(MainFrame.WIDTH,MainFrame.HEIGHT);
	    m_frame.validate();
	}
	else
	    m_frame.pack();
    }

    private void removeBoxes(){
	Component[] c = m_splitPane.getComponents();
	
	//only need to remove one Box component
	if ( (c[c.length-1] instanceof Box) && !(c[c.length-2] instanceof Box) ){
	    m_splitPane.remove(c.length-1);
	    return;
	}

	//probably need to remove all objects
	for(int i = 0; i < c.length;i++){
	    if ( !(c[i] instanceof Box) ) return;
	}
	m_splitPane.removeAll();
    }

    public void packFrame(){
	m_frame.pack();
    }

    private void shutdown(int i){
	PackageLogger.log.info("Shutting down application!");
	savePrefs();
	System.exit(i);
    }
    
    private void savePrefs(){
	Preferences prefs = AppManager.getInstance().getPreferences();
	// Store the location and size of the unit browser toolbox
	Point p = m_frame.getLocation();
	prefs.putInt(MainFrame.WIN_X, p.x);
	prefs.putInt(MainFrame.WIN_Y, p.y);
    }

    private void loadPrefs(){
	Preferences prefs = AppManager.getInstance().getPreferences();
	Point p = new Point();
	p.x = prefs.getInt(MainFrame.WIN_X, 0);
	p.y = prefs.getInt(MainFrame.WIN_Y, 0);
	m_frame.setLocation(p);
    }

    /** Case user supplies fifos in command line arguments. */
    public void fifosUp(){
	connectedFIFOs = true;
	m_fifo.setEnabled(false);
    }
    /** Case user supplies socket commands as command line arguments. */
    public void socketsUp(){
	connectedSocket = true;
	m_socket.setEnabled(false);
    }
    //----------------------------------------------------------------------
    /**
     * Coordinate the start up of objects and processes to initiate
     * connection to local NRS component using FIFOs.
     *
     * @return <tt>true</tt> if the user selected the cancel button,
     * <tt>false</tt> otherwise or if the selected connection could not be
     * opened.
     */
    private boolean setupLocalConnection()
    {
	int ID = m_appManager.getPortManager().getFreePortID();

	Preferences prefs = m_appManager.getPreferences();

	FIFOConnectionWindow conWin = new FIFOConnectionWindow(m_frame, null);
	conWin.restoreSettings(prefs);

	conWin.setPortDescription("Local FIFO", ID, true);
	conWin.setPortParameters(prefs.get ("DEFAULT_FIFO_IN",
					    "/tmp/NRS_PML_nsim2skel"),
				 prefs.get ("DEFAULT_FIFO_OUT",
					    "/tmp/NRS_PML_skel2nsim"));
	conWin.setBrowseDir(prefs.get("DEFAULT_FIFO_DIR", "/tmp"));
	conWin.setModal(true);

	int portNumber;
	boolean enterAgain = true;

	while (enterAgain)
	    {
		// show dialog
		conWin.setVisible(true);

		// immediately save its new position
		conWin.saveSettings(prefs);

		if (conWin.getReturnValue() == FIFOConnectionWindow.CANCEL)
		    {
			enterAgain = false;
		    }
		else if (conWin.getReturnValue() == FIFOConnectionWindow.OK)
		    {
			try
			    {
				portNumber = conWin.getPortNumber();
				enterAgain = false;

				// save these settings
				prefs.put("DEFAULT_FIFO_IN", conWin.getInbound());
				prefs.put("DEFAULT_FIFO_OUT", conWin.getOutbound());
				prefs.put("DEFAULT_FIFO_DIR", conWin.getBrowseDir());
			    }
			catch (NumberFormatException e){
			    JOptionPane.showMessageDialog(null,
							  "\"Port Number\" must a number",
							  "Invalid entry",
							  JOptionPane.ERROR_MESSAGE);
			}
		    }
	    }
	
	if (conWin.getReturnValue() == FIFOConnectionWindow.OK)
	    {
		try
		    {
			m_appManager.getPortManager().
			    openFifoPml(conWin.getInbound(), 
					conWin.getOutbound());

			return true;
		    }
		catch (Exception e)
		    {
			e.printStackTrace();
		    }
	    }
	return false;
    }

    //----------------------------------------------------------------------
    /**
     * Coordinate the start up of objects and processes to initiate
     * a remote connection to other NRS component using TCP/IP
     *
     * @return <tt>true</tt> if the user selected the cancel button,
     * <tt>false</tt> otherwise or if the selected connection could not be
     * opened.
     */
    private boolean setupRemoteConnection()
    {
	int ID = m_appManager.getPortManager().getFreePortID();
	Preferences prefs = m_appManager.getPreferences();

	SocketConnectionWindow socWin = new 
	SocketConnectionWindow(m_frame, null, false);
	socWin.restoreSettings(prefs);

	socWin.setPortDescription("Remote Socket", ID, true);
	socWin.setSocketParameters(prefs.get ("DEFAULT_REMOTE_HOST", 
					      "localhost"),
				 prefs.get ("DEFAULT_PORT", "5000"));
	socWin.setModal(true);

	int portNumber;
	boolean enterAgain = true;

	while (enterAgain)
	    {
		// show dialog
		socWin.setVisible(true);

		// immediately save its new position
		socWin.saveSettings(prefs);

		if (socWin.getReturnValue() == SocketConnectionWindow.CANCEL)
		    {
			enterAgain = false;
		    }
		else if (socWin.getReturnValue() == SocketConnectionWindow.OK)
		    {
			try
			    {
				portNumber = socWin.getPortNumber();
				enterAgain = false;

				// save these settings
				prefs.put("DEFAULT_REMOTE_HOST", 
					  socWin.getRemoteHost());
				prefs.put("DEFAULT_PORT", 
					  socWin.getRemotePort());
			    }
			catch (NumberFormatException e){
			    JOptionPane.showMessageDialog(null,
							  "\"Port Number\" must a number",
							  "Invalid entry",
							  JOptionPane.ERROR_MESSAGE);
			}
		    }
	    }
	
	if (socWin.getReturnValue() == SocketConnectionWindow.OK)
	    {
		try
		    {
			if ( socWin.isClient() )
			    m_appManager.getPortManager().openClient(ID, 
								     socWin.getRemoteHost(), Integer.parseInt(socWin.getRemotePort()));
			else
			    m_appManager.getPortManager().openServer(Integer.parseInt(socWin.getRemotePort()));
								     

			return true;
		    }
		catch (Exception e)
		    {
			e.printStackTrace();
		    }
	    }
	return false;
    }
    
    // Fills a hashmap with empty message templates
     private void populateHardcoded() {
        
        m_messageTemplate = new HashMap(numOfTypes);
        String name ="";
        ArrayList fieldEntries = new ArrayList();

        for (int i = 0; i < numOfTypes; i++ ) {
            
            switch (i){
                
            case 0:  
                name = "CreateNode";
                fieldEntries = new ArrayList(5);
                fieldEntries.add(new FieldEntry("route", "String", true));
                fieldEntries.add(new FieldEntry("toVNID", "int", true)); 
                fieldEntries.add(new FieldEntry("vnType", "String", false)); 
                fieldEntries.add(new FieldEntry("vnid", "int", false)); 
                fieldEntries.add(new FieldEntry("vnName", "String", false));
                break;
                
            case 1:
                name = "DeleteNode";
                fieldEntries = new ArrayList(3);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true));  
                fieldEntries.add(new FieldEntry("vnid", "int", false));
                break;
                
            case 2:
                name = "CreateLink";
                fieldEntries = new ArrayList(8);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true));
                fieldEntries.add(new FieldEntry("sourceNotTarget", "boolean", false));
                fieldEntries.add(new FieldEntry("cid", "String", false));
                fieldEntries.add(new FieldEntry("vnid", "int", false)); 
                fieldEntries.add(new FieldEntry("targetCID", "String", false)); 
                fieldEntries.add(new FieldEntry("targetVNID", "int", false)); 
                fieldEntries.add(new FieldEntry("temporary", "boolean", false));
                break;

            case 3:

                name = "DeleteLink";
                fieldEntries = new ArrayList(7);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true)); 
                fieldEntries.add(new FieldEntry("sourceNotTarget", "boolean", false));
                fieldEntries.add(new FieldEntry("cid", "String", false));
                fieldEntries.add(new FieldEntry("vnid", "int", false)); 
                fieldEntries.add(new FieldEntry("targetCID", "String", false)); 
                fieldEntries.add(new FieldEntry("targetVNID", "int", false));
                break;
                
            case 4:

                name = "QueryMaxLink";
                fieldEntries = new ArrayList(7);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true)); 
                fieldEntries.add(new FieldEntry("returnRoute", "String", false));
                fieldEntries.add(new FieldEntry("returnToVNID", "int", false));
                fieldEntries.add(new FieldEntry("msgID", "String", false)); 
                fieldEntries.add(new FieldEntry("vnid", "int", false)); 
                fieldEntries.add(new FieldEntry("sourceNotTarget", "boolean", false));
                break;

            case 5:

                name = "ReplyMaxLink";
                fieldEntries = new ArrayList(4);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true)); 
                fieldEntries.add(new FieldEntry("replyMsgID", "String", false)); 
                fieldEntries.add(new FieldEntry("link", "int", false));
                break;
                
            case 6:

                name = "QueryVNID";
                fieldEntries = new ArrayList(6);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true)); 
                fieldEntries.add(new FieldEntry("returnRoute", "String", false));
                fieldEntries.add(new FieldEntry("returnToVNID", "int", false));
                fieldEntries.add(new FieldEntry("msgID", "String", false)); 
                fieldEntries.add(new FieldEntry("vnName", "String", false));
                break;

            case 7:

                name = "ReplyVNID";
                fieldEntries = new ArrayList(4);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true)); 
                fieldEntries.add(new FieldEntry("replyMsgID", "String", false)); 
                fieldEntries.add(new FieldEntry("vnid", "int", false));
                break;

            case 8:

                name = "QueryVNName";
                fieldEntries = new ArrayList(6);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true));
                fieldEntries.add(new FieldEntry("returnRoute", "String", false));
                fieldEntries.add(new FieldEntry("returnToVNID", "int", false));
                fieldEntries.add(new FieldEntry("msgID", "String", false));
                fieldEntries.add(new FieldEntry("vnid", "int", false));
                break;

            case 9:
                
                name = "ReplyVNName";
                fieldEntries = new ArrayList(4);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true));
                fieldEntries.add(new FieldEntry("replyMsgID", "String", false));
                fieldEntries.add(new FieldEntry("vnName", "String", false));
                break;

            case 10: 
                
                name = "QueryVNType";
                fieldEntries = new ArrayList(6);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true));
                fieldEntries.add(new FieldEntry("returnRoute", "String", false));
                fieldEntries.add(new FieldEntry("returnToVNID", "int", false));
                fieldEntries.add(new FieldEntry("msgID", "String", false));
                fieldEntries.add(new FieldEntry("vnid", "int", false));
                break;

            case 11:

                name = "ReplyVNType";
                fieldEntries = new ArrayList(4);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true));
                fieldEntries.add(new FieldEntry("replyMsgID", "String", false));
                fieldEntries.add(new FieldEntry("vnType", "String", false));
                break;
                
            case 12:
                
                name = "QueryCID";
                fieldEntries = new ArrayList(6);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true));
                fieldEntries.add(new FieldEntry("returnRoute", "String", false));
                fieldEntries.add(new FieldEntry("returnToVNID", "int", false));
                fieldEntries.add(new FieldEntry("msgID", "String", false));
                fieldEntries.add(new FieldEntry("port", "int", false));
                fieldEntries.add(new FieldEntry("cid", "String", false));
                break;
                
            case 13:
                
                name = "ReplyCID";
                fieldEntries = new ArrayList(6);
                fieldEntries.add(new FieldEntry("route", "String", true)); 
                fieldEntries.add(new FieldEntry("toVNID", "int", true));
                fieldEntries.add(new FieldEntry("replyMsgID", "String", false));
                fieldEntries.add(new FieldEntry("cid", "String", false));
                fieldEntries.add(new FieldEntry("port", "int", false));
                break;
                


            }
            m_messageTemplate.put(name, fieldEntries);
        }
    }
    
}
