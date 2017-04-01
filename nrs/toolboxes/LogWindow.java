// $Id: LogWindow.java,v 1.1 2004/11/15 09:47:01 darrens Exp $
package nrs.toolboxes;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.Dialog;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.*;

public class LogWindow extends Toolbox implements ActionListener
{
  ///// GUI components
  JTextPane m_text;
  JScrollPane m_scrollPane;
  JPanel m_statusPanel;
  JLabel m_warningsCount;
  JLabel m_severeCount;
 
  DefaultStyledDocument m_doc;

  ///// Style names
  protected static final String FS_TRACE_ROLE = "Bold";
  protected static final String FS_WARNING_ROLE = "Red";
  protected static final String FS_WARNING_TEXT = "WarningText";
  protected static final String FS_SEVERE_ROLE = "Severe";
  protected static final String FS_OK_ROLE = "GreenOK";
  
  /** Title of the dialog */
  public static String TITLE = "Log";

  /** Log handler used to provide log records to this class */
  LogWindowHandler m_handler;


  //----------------------------------------------------------------------
  /**
   * Creates a {@link LogWindow} with a {@link Dialog} owner and
   * specified {@link ToolboxParent}.
   */
  public LogWindow(Dialog owner, ToolboxParent parent)
  {
    super((Dialog) owner, TITLE, parent, "LogWindow_");
    init();
  }
  //----------------------------------------------------------------------
  public LogWindow(Frame owner, ToolboxParent parent)
  {
    super((Frame) owner, TITLE, parent, "LogWindow_");
    init();
  } 
  //----------------------------------------------------------------------
  private void init()
  {
    m_handler = new LogWindowHandler(this);    

    ////////// Configure the text area

    // Setup the underlying document model
    m_doc = new DefaultStyledDocument();
    addStyles();

    // Create and configure the textpane gui element
    m_text = new JTextPane(m_doc);
    m_text.setEditable(false);

    ////////// Configure the status bar
    m_statusPanel = new JPanel();
    m_statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

    // Set up the label for storing the number of warnings
    m_warningsCount = new JLabel("0");
    m_warningsCount.setMinimumSize(new Dimension(50,0));
    m_statusPanel.add(new JLabel("Warnings:"));
    m_statusPanel.add(m_warningsCount);

    // Set up the label for storing the number of errors
    m_severeCount = new JLabel("0");
    m_severeCount.setMinimumSize(new Dimension(50,0));
    m_statusPanel.add(new JLabel("Errors:"));
    m_statusPanel.add(m_severeCount);

    // Add a border to the panel
    // m_statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
    m_statusPanel.setBorder(BorderFactory.createEtchedBorder());
   
    ////////// Configure the root panel
    JPanel root = (JPanel) this.getContentPane();
    root.setLayout(new BorderLayout());
    
    // Set text area to be main item, with status bar at base
    m_scrollPane = new JScrollPane(m_text);
    root.add(m_scrollPane, BorderLayout.CENTER);
    root.add(m_statusPanel, BorderLayout.SOUTH);

    setSize(300,300);

    this.pack();

    // Set scroll bar to lowest position
    JScrollBar vsb =  m_scrollPane.getVerticalScrollBar();
    vsb.setValue(vsb.getMaximum());
  }
  //----------------------------------------------------------------------
  /** Add a set of standard styles which will be used when presenting
   * log records.*/
  void addStyles()
  {
    // Set a bold font
    Style s = m_doc.addStyle(FS_TRACE_ROLE, 
                             m_doc.getStyle(StyleContext.DEFAULT_STYLE));
    StyleConstants.setBold(s, true);

    // Set a bold red font
    s = m_doc.addStyle(FS_WARNING_ROLE, 
                       m_doc.getStyle(FS_TRACE_ROLE));  
    StyleConstants.setForeground(s, Color.RED);

    // Set a normal font, but in red
    s = m_doc.addStyle(FS_WARNING_TEXT, 
                       m_doc.getStyle(StyleContext.DEFAULT_STYLE));
    StyleConstants.setForeground(s, Color.RED);

    // Set the style for severe
    s = m_doc.addStyle(FS_SEVERE_ROLE,
                       m_doc.getStyle(StyleContext.DEFAULT_STYLE));
    StyleConstants.setBold(s, true);
    StyleConstants.setForeground(s, Color.YELLOW);
    StyleConstants.setBackground(s, Color.RED);

    // Set the style for things ok
    s = m_doc.addStyle(FS_OK_ROLE,
                       m_doc.getStyle(StyleContext.DEFAULT_STYLE));
    StyleConstants.setForeground(s, Color.GREEN);
  }
  //----------------------------------------------------------------------
  /** Get hold of the handler associated with this window */
  public Handler getHandler()
  {
    return m_handler;
  }
  //----------------------------------------------------------------------
  /** Invoked when an action occurs on one of the GUI components. */
  public void actionPerformed(ActionEvent e)
  {
    // has addActionListener been called?
  }
  //----------------------------------------------------------------------
  /** Accept LogRecord */
  public void publish(LogRecord record)
  {
    if (record == null) return;

    int offs = m_doc.getLength();

    try
      {
        if (record.getLevel() == Level.SEVERE)
          {
            publishSevere(record);
          }
        else  if (record.getLevel() == Level.WARNING)
          {
            publishWarning(record);
          }
        else
          {
             publishDefault(record);
          }
        
      }
    catch (BadLocationException e)
      {
        System.out.println("Exception while trying to add text in LogWindow class: " + e);
      }

    JScrollBar vsb =  m_scrollPane.getVerticalScrollBar();
    vsb.setValue(vsb.getMaximum());
  }
  //----------------------------------------------------------------------
  void publishSevere(LogRecord record) throws BadLocationException
  {
    // Note, instead of having 'SEVERE' appear, I am displaying 'ERROR'
    // instead
    m_doc.insertString(m_doc.getLength(),
                       "ERROR",
                       m_doc.getStyle(FS_SEVERE_ROLE));
    
    m_doc.insertString(m_doc.getLength(),
                       ":" + record.getMessage(),
                       m_doc.getStyle(FS_WARNING_TEXT));

    publishExtras(record, m_doc.getStyle(FS_WARNING_TEXT));

    // Update the warnings label
    int count = Integer.parseInt(m_severeCount.getText());
    m_severeCount.setText(Integer.toString(++count));
  }
  //----------------------------------------------------------------------
  void publishWarning(LogRecord record) throws BadLocationException
  {
    m_doc.insertString(m_doc.getLength(),
                       record.getLevel().toString(),
                       m_doc.getStyle(FS_WARNING_ROLE));
    
    m_doc.insertString(m_doc.getLength(),
                       ":" + record.getMessage(),
                       m_doc.getStyle(FS_WARNING_TEXT));

    publishExtras(record, m_doc.getStyle(FS_WARNING_TEXT));

    // Update the warnings label
    int count = Integer.parseInt(m_warningsCount.getText());
    m_warningsCount.setText(Integer.toString(++count));
  }
  //----------------------------------------------------------------------
  void publishDefault(LogRecord record) throws BadLocationException
  {
    m_doc.insertString(m_doc.getLength(),
                       record.getLevel().toString(),
                       m_doc.getStyle(FS_OK_ROLE));
    

    m_doc.insertString(m_doc.getLength(),
                       ":" + record.getMessage(),
                       m_doc.getStyle(StyleContext.DEFAULT_STYLE));

    publishExtras(record, m_doc.getStyle(StyleContext.DEFAULT_STYLE));
  }
  //----------------------------------------------------------------------
  void publishExtras(LogRecord record, Style style) throws BadLocationException
  {
    String source = new String();
    if (record.getSourceClassName() != null) 
      source = source + record.getSourceClassName();

    if (record.getSourceMethodName() != null) 
      source = source + "#" + record.getSourceMethodName();

    
    if (source.length() > 0)
        m_doc.insertString(m_doc.getLength(), " (" + source + ")\n", style);
    else
        m_doc.insertString(m_doc.getLength(), "\n", style);

  }
}
