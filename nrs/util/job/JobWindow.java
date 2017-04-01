package nrs.util.job;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.AbstractListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import nrs.toolboxes.Toolbox;
import nrs.toolboxes.ToolboxParent;
import java.awt.SystemColor;
import javax.swing.DefaultListCellRenderer;


class MyCellRenderer extends JLabel implements ListCellRenderer
{
  private static Color DefaultSelBackground;

  public MyCellRenderer()
  {
    setOpaque(true);
  }

  public Component getListCellRendererComponent(JList list,
                                                Object value,
                                                int index,
                                                boolean isSelected,
                                                boolean cellHasFocus)
  {
    if (DefaultSelBackground == null)
    {
      DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
      DefaultSelBackground =
        dlcr.getListCellRendererComponent(list,
                                          value,
                                          index,
                                          true,
                                          cellHasFocus).getBackground();

    }

    setText(value.toString());

    Color bk = Color.white;
    Color fg = Color.green;

    if (isSelected)
    {
      bk = DefaultSelBackground;
      if (bk == null) bk = Color.blue;
    }

    if (value instanceof Job)
    {
      Job j = (Job) value;

      if (j.getPhase() == Job.ABORT)
      {
        fg = Color.red;
      }

      if (j.getPhase() == Job.COMPLETE)
      {
        fg = Color.black;
      }
    }

    setBackground(bk);
    setForeground(fg);

    return this;
  }
}


/**
 * Provides a window to watch the jobs being managed by a {@link
 * JobManager} instance.
 *
 * @author Darren Smith
 */
public class JobWindow extends Toolbox implements ActionListener,
                                                  ListSelectionListener,
                                                  Observer
{
  /** Title of the dialog */
  public static String TITLE = "Jobs";

  private JobManager m_jm;

  private JList m_list;

  private JTextArea m_textArea = new JTextArea();

  private Job m_observing;

  //----------------------------------------------------------------------
  /**
   * Creates a {@link JobWindow} with a {@link Dialog} owner and
   * specified {@link ToolboxParent}.
   */
  public JobWindow(Dialog owner,
                   ToolboxParent parent,
                   JobManager jm)
  {
    super((Dialog) owner, TITLE, parent, "JobWindow_");
    m_jm = jm;
    m_list = new JList(m_jm.getListModel());

    init();
  }
  //----------------------------------------------------------------------
  public JobWindow(Frame owner,
                   ToolboxParent parent,
                   JobManager jm)
  {
    super((Frame) owner, TITLE, parent, "JobWindow_");
    m_jm = jm;
    m_list = new JList(m_jm.getListModel());

    init();
  }
  //----------------------------------------------------------------------
  private JPanel createListPanel()
  {
    JPanel p = new JPanel(new BorderLayout());

    p.add(new JScrollPane(m_list), BorderLayout.CENTER);

    return p;
  }
  //----------------------------------------------------------------------
  private JComponent createCenterPanel()
  {
    JSplitPane p = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    p.setTopComponent(new JScrollPane(m_list));
    p.setBottomComponent(new JScrollPane(m_textArea));

    return p;
  }
  //----------------------------------------------------------------------
  private JPanel createJobInfoPanel()
  {
    JPanel p = new JPanel(new BorderLayout());

    p.add(new JScrollPane(m_textArea), BorderLayout.CENTER);

    return p;
  }
  //----------------------------------------------------------------------
  private void init()
  {
    m_list.setCellRenderer(new MyCellRenderer());
    m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    m_textArea.setEditable(false);

    ////////// Configure the root panel
    JPanel root = (JPanel) this.getContentPane();
    root.setLayout(new BorderLayout());

    root.add(createCenterPanel(), BorderLayout.CENTER);
    //    root.add(createJobInfoPanel(), BorderLayout.SOUTH);

    setSize(300,300);

    ////////// Event stuff
    m_list.addListSelectionListener(this);

    this.pack();
  }
  //----------------------------------------------------------------------
  private void updateTextArea()
  {
    StringBuffer sb = new StringBuffer();

    sb.append("Job: " + m_observing.toString() + "\n");
    sb.append("State: " + m_observing.getPhase());

    if (m_observing.getResult() != null)
    {
      sb.append(" -  " + m_observing.getResult());
    }

    m_textArea.setText(sb.toString());
  }
  //----------------------------------------------------------------------
  /**
   * Inhereited from Observer. Called whenever a watched job (one that
   * appears n the lower window) has changed its state.
   */
  public void update(Observable o, Object arg)
  {
    if (o == m_observing)
    {
      updateTextArea();
    }
    else
    {
      PackageLogger.log.warning("Received observer notification from "
                                + o + " but not observing it???");
      o.deleteObserver(this);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Inherited from ListSelectionListener. Invoked when the user selects
   * one of the list items.
   */
  public void valueChanged(ListSelectionEvent e)
  {
    Object selection = m_list.getSelectedValue();

    if (selection == null)
    {
      if (m_observing != null)
      {
        m_observing.deleteObserver(this);
        m_observing = null;
      }
    }
    else if (selection instanceof Job)
    {
      if (m_observing == selection) return;

      if (m_observing != null) m_observing.deleteObserver(this);

      m_observing = (Job) selection;
      m_observing.addObserver(this);

      updateTextArea();
    }
  }
  //----------------------------------------------------------------------
  /** Invoked when an action occurs on one of the GUI components. */
  public void actionPerformed(ActionEvent e)
  {
    // has addActionListener been called?
  }
  //----------------------------------------------------------------------
}
