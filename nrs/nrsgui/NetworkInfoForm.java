package nrs.nrsgui;

import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Frame;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import nrs.util.GridPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The form for entering data about a new network being constructed.
 *
 * @author Darren Smith
 */
class NetworkInfoForm extends JDialog implements ActionListener
{
  /** Did the user close the dialog by pressing OK */
  private boolean m_userOKed;

  JTextField m_networkName = new JTextField();
  JTextField m_author = new JTextField();
  JTextArea  m_description = new JTextArea();

  //----------------------------------------------------------------------
  /**
   * Constructor
   */
  NetworkInfoForm(Frame owner, String title)
  {
    this(owner, title, "", "", "");
  }
  //----------------------------------------------------------------------
  /**
   * Creates a form with initial values for the input fields
   */
  NetworkInfoForm(Frame owner,
                  String title,
                  String name,
                  String author,
                  String description)
  {
    super(owner, title, true);

    m_networkName.setText(name);
    m_author.setText(author);
    m_description.setText(description);

    configureGUI();

    // Center on screen
    Dimension dim = getToolkit().getScreenSize();
    this.setLocation(dim.width/2 - getWidth()/2,
                     dim.height/2 - getHeight()/2);
  }
  //----------------------------------------------------------------------
  /**
   * Returns <tt>true</tt> if the user closed the dialog by pressing the
   * OK button, otherwise returns <tt>false</tt>
   */
  boolean userOKed()
  {
    return m_userOKed;
  }
  //----------------------------------------------------------------------
  /**
   * Build and arrange the root panel
   */
  JPanel buildRootPanel()
  {
    GridPanel p = new GridPanel();

    p.setBorder(new EmptyBorder(5, 5, 5, 5));

    // Resusable border. Adds spacing between columns
    EmptyBorder border = new EmptyBorder(0,0,0,10);

    // Reusable dimensions
    Dimension longField = new Dimension(240, 20);

    // Network name
    JLabel lbl1 = new JLabel("Network Name");
    lbl1.setBorder(border);
    p.addComponent(lbl1, 1, 1);
    m_networkName.setPreferredSize(longField);

    // Description
    JLabel lbl2 = new JLabel("Description");
    lbl2.setBorder(border);
    p.addComponent(lbl2, 2, 1);
    m_description.setLineWrap(true);
    m_description.setWrapStyleWord(true);
    JScrollPane scroll = new JScrollPane(m_description);
    scroll.setPreferredSize(new Dimension(240,80));

    // Author
    JLabel lbl3 = new JLabel("Author");
    lbl3.setBorder(border);
    p.addComponent(lbl3, 4, 1);
    m_author.setPreferredSize(longField);

    // Add components in desired focus order - this doesn't seem to work!
    p.addFilledComponent(m_networkName,
                         1, 2, 3, 1, GridBagConstraints.HORIZONTAL);
    p.addFilledComponent(scroll, 2, 2, 3, 2, GridBagConstraints.BOTH);
    p.addFilledComponent(m_author, 4, 2, 3, 1, GridBagConstraints.HORIZONTAL);
    p.addFilledComponent(makeButton("OK"), 1, 5);
    p.addFilledComponent(makeButton("Close"), 2, 5);

    return p;
  }
  //----------------------------------------------------------------------
  /**
   * Arrange all elements of the GUI
   */
  private void configureGUI()
  {
    Container cn = getContentPane();

    cn.setLayout(new BorderLayout());
    cn.add(buildRootPanel(), BorderLayout.CENTER);

    pack();
  }
  //-------------------------------------------------------------------------
  private JButton makeButton(String title)
  {
    JButton newItem = new JButton(title);
    newItem.addActionListener(this);
    return newItem;
  }
  //----------------------------------------------------------------------
  public void setVisible(boolean b)
  {
    m_userOKed = false;
    super.setVisible(b);
  }
  //----------------------------------------------------------------------
  // Implements interface
  public void actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals("OK"))
    {
      m_userOKed = true;
      super.setVisible(false);
    }
    if (e.getActionCommand().equals("Close"))
    {
      m_userOKed = false;
      super.setVisible(false);
    }
  }
  //----------------------------------------------------------------------
  /**
   * Return the network name string
   */
  String getNetworkName()
  {
    return m_networkName.getText();
  }
  //----------------------------------------------------------------------
  /**
   * Return the author string
   */
  String getAuthor()
  {
    return m_author.getText();
  }
  //----------------------------------------------------------------------
  /**
   * Return the description string
   */
  String getDescription()
  {
    return m_description.getText();
  }
}
