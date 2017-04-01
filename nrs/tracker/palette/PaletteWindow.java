	// $Id: PaletteWindow.java,v 1.3 2005/05/09 22:03:55 hlrossano Exp $
package nrs.tracker.palette;

import java.awt.BorderLayout;
import java.awt.Container;
//import java.awt.FlowLayout;
import java.awt.Frame;
//import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.NumberFormat;
import java.util.ListIterator;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
//import javax.swing.BorderFactory;
//import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
//import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import nrs.toolboxes.LayerPanel;
import nrs.toolboxes.Toolbox;
import nrs.toolboxes.ToolboxParent;

public class PaletteWindow extends Toolbox implements ListSelectionListener
{
  /** Title of the dialog */
  public static String TITLE = "Palette";

  private static final String PREF_KEY_CURR_DIR = "PALETTEWINDOW_CURR_DIR";

  /** Collection of colours */
  private Palette m_pal;

  /** Filename belonging to the palette */
  private String m_filename = new String("noname");
  private String m_absFilename = new String("noname");


  private final String BTN_UNDO = "Undo";


  // Preferred file extensions
  public static final String palette_ext = new String("plt");
  public static final String statistics_ext = new String("m");
  private ExampleFileFilter filterPal;
  private ExampleFileFilter filterStats;

  /** Collection of CompoundColours present in the palette. Hopefully,
   * sometime in the near future, the Palette can be updated to a
   * vector, rather than linked list. Or... just use the
   * DefaultListModel for storage, and expand the colour compound class,
   * so that it maintains its own numeric id (why is that needed?), and
   * also has a conforming toString() function. */
  private final DefaultListModel m_colours = new DefaultListModel();

  //// GUI components
  private JList m_colList;
  private JPopupMenu m_popupMenu;
  private JLabel m_totalLabel;
  private ColourSample m_sample; // for mean
  private JLabel m_meanLabel;
  private JLabel m_colourPopulation;
  private final JFileChooser m_fc;

  /** Label for close button / menu-item */
  private final String BTN_CLOSE = "Close";

  /** Label for loading palettes */
  private final String BTN_LOAD = "Load...";

  /** Label for saving palettes */
  private final String BTN_SAVE_AS = "Save As...";

  /** Label for saving palettes */
  private final String BTN_SAVE = "Save";

  /** Label for adding new colour */
  private final String BTN_NEW = "New...";

  /** Label for removing colour */
  private final String BTN_REMOVE = "Delete...";

  /** Label for removing all colours */
  private final String BTN_REMOVE_ALL = "Delete All...";

  /** Label for chaning the name of a colour */
  private final String BTN_RENAME = "Rename...";

  /** Label for exporting statistics */
  private final String BTN_EXPORT = "Export Stats...";

  // Class logger
  private static Logger m_log = Logger.getLogger("nrs.tracker.palette");

  // Indicate whether a file name has yet to be provided
  private boolean m_noFilename = true;

  //----------------------------------------------------------------------
  /**
   * Constructor
   *
   * @param owner the {@link java.awt.Frame} which owns this window. Can
   * be set to null.
   *
   * @param parent the {@link ToolboxParent} object to notify when the
   * visibility of this dialog changes.
   */
  public PaletteWindow(Frame owner, ToolboxParent parent)
  {
	  this(owner, parent, new Palette());
  }
  //----------------------------------------------------------------------
  /**
   * Constructor which creates an instance based on the contents of an
   * existing {@link Palette} instance. The supplied instance is also
   * used internally for palette management.
   *
   * @param owner the {@link java.awt.Frame} which owns this window. Can
   * be set to null.
   *
   * @param parent the {@link ToolboxParent} object to notify when the
   * visibility of this dialog changes.
   *
   * @param palette the {@link Palette} instance this class should use
   * for underlying storage and management of colours.
   */
  public PaletteWindow(Frame owner, ToolboxParent parent, Palette palette)
  {
    super(owner, "Palette", parent, "PaletteToolbox_");
    m_pal = palette;

    filterPal = new ExampleFileFilter();
    filterPal.addExtension("plt");
    filterPal.setDescription("Palette");

    filterStats = new ExampleFileFilter();
    filterStats.addExtension("m");
    filterStats.setDescription("Palette");

    m_fc = new JFileChooser();

    if(!palette.isEmpty()) rebuildList();
    guiInit();
  }
  //----------------------------------------------------------------------
  /**
   * Build the GUI by placing various components on the root
   * panel. Should only be called once.
   */
  private void guiInit()
  {
    m_colList = new JList(m_colours);
    m_colList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    m_colList.addListSelectionListener(this);
    m_colList.addMouseListener(
                               new MouseAdapter()
                               {
                                 public void mouseClicked(MouseEvent e)
                                 { if (e.isPopupTrigger()) popup(e); }

                                 public void mousePressed(MouseEvent e)
                                 { if (e.isPopupTrigger()) popup(e); }

                                 public void mouseReleased(MouseEvent e)
                                 { if (e.isPopupTrigger()) popup(e); }
                               });

	LayerPanel main = new LayerPanel();
	LayerPanel displayPane = new LayerPanel();
	JPanel listPane = new JPanel(new BorderLayout());
	LayerPanel info = new LayerPanel();

	listPane.add(new JScrollPane(m_colList), BorderLayout.CENTER);
	info.add(new JLabel("Colours:"));
    m_totalLabel = new JLabel("0");
    info.add(m_totalLabel);
	listPane.add(info, BorderLayout.SOUTH);

	displayPane.add(new JLabel(" Mean colour:"));
    m_sample = new ColourSample("colours");
    m_meanLabel = new JLabel(" ()");
    displayPane.add(m_sample);
	displayPane.createRow();
	displayPane.add(new JLabel(" Colour size:"));
    m_colourPopulation = new JLabel();
	displayPane.add(m_colourPopulation);
	displayPane.createRow();
	displayPane.add(makeJButton(BTN_UNDO));

	main.addHorizontalSplitPane(listPane, displayPane, BorderLayout.CENTER);

    // Root panel
    Container root = getContentPane();
    root.setLayout(new BorderLayout());
	root.add(main);

    m_popupMenu = new JPopupMenu("Palette");
    buildPopupMenu();

	//setResizable(false);
    setSize(300,300);
    this.pack();
  }
  //----------------------------------------------------------------------
  /**
   * Return the internal list of compound colours, so that it can be
   * accessed directly.
   *
   * Client code should attempt to avoid calling this function. As the
   * PaletteWindow class develops, it should provide all of the
   * functionality that Palette currently provided. So where possible,
   * prefer to use methods of {@link PaletteWindow} instead of {@link
   * Palette}.
   */
  public Palette getPalette()
  {
    return m_pal;
  }
  //----------------------------------------------------------------------
  /**
   * Utility method to make a {@link JButton} object, with the specified
   * label, which uses {@link #handleEvent} for the action-performed callback.
   */
  private JButton makeJButton(String label)
  {
    JButton b = new JButton(label);

    b.addActionListener(
                        new ActionListener()
                        {
                          public void actionPerformed(ActionEvent e)
                          { handleEvent(e); }
                        });

    return b;
  }
  //----------------------------------------------------------------------
  /**
   * Handle various GUI events.
   */
  private void handleEvent(ActionEvent e)
  {
    if ((e.getActionCommand() == BTN_UNDO) )
    {
		m_pal.undoOnEdition();
		updateStats();
		return;
    }

    if (e.getActionCommand() == BTN_CLOSE)
    {
      setVisible(false);
      return;
    }

    if (e.getActionCommand() == BTN_NEW)
    {
      String inputValue =
        JOptionPane.showInputDialog("Please enter name of colour");
      if (inputValue == null) return;  // user clicked "Cancel"

      addColour(inputValue);
      return;
    }

    if (e.getActionCommand() == BTN_REMOVE)
    {
      deleteSelectedColour();
      return;
    }

    if (e.getActionCommand() == BTN_REMOVE_ALL)
    {
      if (JOptionPane.showConfirmDialog(this, "Really remove ALL colours?",
                                        "Confirm delete",
                                        JOptionPane.YES_NO_OPTION)
          == JOptionPane.YES_OPTION)
      {
        m_pal.clear();
        m_colours.clear();
        updateTotalLabel();
        m_colList.invalidate();
      }
      return;
    }

    if (e.getActionCommand() == BTN_RENAME)
    {
      renameSelectedColour();
      return;
    }

    if (e.getActionCommand() == BTN_LOAD)
    {
      loadPalette();
      return;
    }

    if (e.getActionCommand() == BTN_SAVE_AS)
    {
      saveAsPalette(true);
      return;
    }

    if (e.getActionCommand() == BTN_SAVE)
    {
      saveAsPalette(m_noFilename);
      return;
    }

    if (e.getActionCommand() == BTN_EXPORT)
    {
      exportStats();
      return;
    }
  }
  //----------------------------------------------------------------------
  /**
   * Implement the {@link ListSelectionListener} interface, to respond
   * to the user clicking on colours in the JList.
   */
  public void valueChanged(ListSelectionEvent e)
  {
    // Ignore some events
    if (e.getValueIsAdjusting()) return;

    // Update the underlying palette with the next colour selection
    CompoundColour colourSelected = getSelectedColour();

    if (colourSelected != null)
    {
     m_pal.setOnEdition(colourSelected.toString());
      m_log.fine("Active colour in palette=\"" + m_pal.onEdit() + "\"");
    }

    updateInfoPane(colourSelected);
  }
  //----------------------------------------------------------------------
  /**
   * Handle a mouse-button event occuring on the {@link JList} section
   * of the window. The event might be a trigger for the {@link
   * JPopupMenu}, in which case the popup menu is displayed.
   */
  private void popup(MouseEvent e)
  {
    updatePopupMenu();
    m_popupMenu.show(m_colList, e.getX(), e.getY());
  }
  //----------------------------------------------------------------------
  /**
   * Build the popup menu
   */
  private void buildPopupMenu()
  {
    m_popupMenu.add(makeMenuItem(BTN_NEW));
    m_popupMenu.addSeparator();
    m_popupMenu.add(makeMenuItem(BTN_RENAME));
    m_popupMenu.add(makeMenuItem(BTN_REMOVE));
    m_popupMenu.add(makeMenuItem(BTN_REMOVE_ALL));
    m_popupMenu.addSeparator();
    m_popupMenu.add(makeMenuItem(BTN_LOAD));
    m_popupMenu.add(makeMenuItem(BTN_SAVE));
    m_popupMenu.add(makeMenuItem(BTN_SAVE_AS));
    m_popupMenu.addSeparator();
    m_popupMenu.add(makeMenuItem(BTN_EXPORT));
    m_popupMenu.addSeparator();
    m_popupMenu.add(makeMenuItem(BTN_CLOSE));
  }
  //----------------------------------------------------------------------
  /**
   * Update the popup menu. I.e, disable or enable various items which
   * depend on the current internal state.
   */
  private void updatePopupMenu()
  {
  }
  //-------------------------------------------------------------------------
  /**
   * Construct and return a {@link JMenuItem} object to represent the selection
   * of a normal menu item (eg, items such as 'cancel' etc), and
   * register the item to use the function {@link #handleEvent} for
   * event handling.
   */
  private JMenuItem makeMenuItem(String menuLabel)
  {
    JMenuItem newItem = new JMenuItem(menuLabel);

    newItem.addActionListener(
                              new ActionListener()
                              {
                                public void actionPerformed(ActionEvent e)
                                { handleEvent(e); }
                              });

    return newItem;
  }
  //----------------------------------------------------------------------
  /**
   * Attempt to add a new colour to the palette.
   *
   * @return true if a new colour was added, or false if a colour of
   * that name already exists within the palette.
   */
  private boolean addColour(String colourName)
  {

	/* alternative option
	if ( !m_pal.add(colourName) ){
        m_log.warning("Colour \"" + colourName + "\" already exists or empty name");
		return false;
	}

    m_log.fine("Created colour \"" + colourName + "\"");

	// Also add to the JList, and select it there
    m_colours.addElement(m_pal.getOnEdition());
	m_colList.setSelectedIndex(m_pal.size()-1);

    updateTotalLabel();

	return true;

	*/

    // Defensive programming
    if (colourName == null) return false;
    if (colourName.equals("")) return false;

    ListIterator li = m_pal.listIterator(0);
    while (li.hasNext())
    {
      CompoundColour cc = (CompoundColour) li.next();
      if (cc.id().equals(colourName))
      {
        m_log.warning("Colour \"" + colourName + "\" already exists");
        return false;
      }
    }

    // Create the new colour here (instead of having Palette do
    // it). Note that the colour has to be converted to lower case, in
    // order to agree with the Palette class convention.
    CompoundColour newColour = new
      CompoundColour(colourName.trim().toLowerCase());
    m_log.fine("Created colour \"" + newColour + "\"");

    // Add to the underlying Palette class, and also select it there
    m_pal.add(newColour);
    m_pal.setOnEdition(colourName);

    m_log.fine("Active colour in palette=\"" + m_pal.onEdit() + "\"");

    // Also add to the JList, and select it there
    m_colours.addElement(newColour);
    m_colList.setSelectedIndex(m_colours.getSize()-1);

    updateTotalLabel();

    return true;
  }
  //----------------------------------------------------------------------
  /**
   * For compatibility with the underlying {@link Palette}
   * class. Rebuilds the {@link JList} representation of {@link
   * CompoundColour} objects based on the contents of the palette.
   */
  private void rebuildList()
  {
    m_colours.clear();

    ListIterator li = m_pal.listIterator(0);
    while (li.hasNext())
    {
      m_colours.addElement(li.next());
    }

    updateTotalLabel();
  }
  //----------------------------------------------------------------------
  /**
   * Provide a {@link Palette} instance this class should use. This
   * instance replaces the current object in used. The visible {@link
   * JList} will be updated to reflect the contents of the new palette.
   */
  public void setPalette(Palette palette)
  {
    m_pal = palette;
    rebuildList();
  }
  //----------------------------------------------------------------------
  /**
   * Return the {@link CompoundColour} currently selected in the {@link
   * JList} view, or null if nothing is selected.
   */
  public CompoundColour getSelectedColour()
  {
    if (m_colList.isSelectionEmpty()) return null;

    return (CompoundColour) m_colList.getSelectedValue();
  }
  //----------------------------------------------------------------------
  /**
   * 
   */
  public void undoFromHere()
  {
    if (!m_colList.isSelectionEmpty())
	    ((CompoundColour)m_colList.getSelectedValue()).undoFromHere();
  }
  //----------------------------------------------------------------------
  /**
   * Returns true if nothing is selected.
   */
  public boolean isSelectionEmpty()
  {
    return m_colList.isSelectionEmpty();
  }
  //----------------------------------------------------------------------
  /**
   * Update the {@link JLabel} displaying the total number of colours.
   */
  private void updateTotalLabel()
  {
    m_totalLabel.setText(Integer.toString(m_pal.size()));
  }
  //----------------------------------------------------------------------
  /**
   * Delete the colour currently highlighted in the {@link JList}. Also
   * makes appropriate changes to underlying {@link Palette} instance.
   */
  private void deleteSelectedColour()
  {
    // ignore if nothing selected
    if (m_colList.isSelectionEmpty()) return;

    if (JOptionPane.showConfirmDialog(this, "Really remove \""
                                      + getSelectedColour() + "\"?",
                                        "Confirm delete",
                                        JOptionPane.YES_NO_OPTION)
          == JOptionPane.YES_OPTION)
    {

      m_pal.remove(getSelectedColour());
      m_colours.removeElement(getSelectedColour());

      updateTotalLabel();
      m_colList.invalidate();
    }
  }
  //----------------------------------------------------------------------
  /**
   */
  private void renameSelectedColour()
  {
    // ignore if nothing selected
    if (m_colList.isSelectionEmpty()) return;

    String inputValue
      = JOptionPane.showInputDialog(this,
                                    "Enter new name",
                                    "Rename colour",
                                    JOptionPane.QUESTION_MESSAGE);

    if (inputValue != null)
    {
      getSelectedColour().rename(inputValue);
      m_colList.invalidate();
    }
  }
  //----------------------------------------------------------------------
  /**
   * Handle user request to load a colour palette
   */
  private void loadPalette()
  {
    m_fc.setFileFilter(filterPal);
    int returnVal = m_fc.showOpenDialog(this);

    if (returnVal != JFileChooser.APPROVE_OPTION) return;

    File file = new File(addFileExt(m_fc.getSelectedFile().getName(),
                                    m_fc.getSelectedFile().getAbsolutePath(),
                                    palette_ext));
      
    m_filename = file.getName();
    m_absFilename = file.getAbsolutePath();
    
    m_pal.loadAbsolute(m_absFilename);
    
    rebuildList();
    updateWindowTitle();
    m_pal.updateColourStatistics();
  }
  //----------------------------------------------------------------------
  /**
   * Handle user request to save a colour palette
   */
  private void saveAsPalette(boolean requestName)
  {
    if (requestName)
    {
      m_fc.setFileFilter(filterPal);
      if (m_fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

      File file = new File(addFileExt(m_fc.getSelectedFile().getName(),
                                      m_fc.getSelectedFile().getAbsolutePath(),
                                      palette_ext));
      
      m_filename = file.getName();
      m_absFilename = file.getAbsolutePath();
      m_noFilename = false;
    }

    m_pal.saveAbsolute(m_absFilename);

    updateWindowTitle();
  }
  //----------------------------------------------------------------------
  /**
   *
   */
  public void updateStats()
  {
    m_pal.updateColourStatistics();
    updateInfoPane(getSelectedColour());
  }
  //----------------------------------------------------------------------
  /**
   *
   */
  private void updateInfoPane(CompoundColour c)
  {
    if (c != null)
    {
      double [] mean = c.getMean();
      double r = mean[0];
      double g = mean[1];
      double b = mean[2];

      NumberFormat nf = NumberFormat.getInstance();
      nf.setMaximumFractionDigits(2);
      nf.setMinimumFractionDigits(2);

      m_meanLabel.setText(" (R="
                          + nf.format(r) + ", G="
                          + nf.format(g) + ", B="
                          + nf.format(b) + ")");


      m_sample.setRGB((int) r, (int) g, (int) b);

      m_colourPopulation.setText(Integer.toString(c.size()));
    }
    else
    {
      m_meanLabel.setText("");
     m_sample.setRGB(255,255,255);
     m_colourPopulation.setText("");
   }
  }
  //----------------------------------------------------------------------
  /**
   *
   */
  private void updateWindowTitle()
  {
    setTitle("Palette (" + m_filename + ")");
  }
  //----------------------------------------------------------------------
  /**
   * Provide a {@link java.util.prefs.Preferences} object which allows
   * self to save various settings for future sessions. This overrides
   * the base class implementation to also save information about the
   * current directory used for saving and loading.
   */
  public void saveSettings(Preferences props)
  {
    super.saveSettings(props);

    props.put(PREF_KEY_CURR_DIR,
              m_fc.getCurrentDirectory().getAbsolutePath());
  }
  //----------------------------------------------------------------------
  /**
   * Provide a {@link java.util.prefs.Preferences} object which allows
   * self to restore various settings saved from previous sessions. This
   * overrides the base class method so that the current directory used
   * for saving and loading is also persisted.
   */
  public void restoreSettings(Preferences props)
  {
    String path = props.get(PREF_KEY_CURR_DIR, "");
    if (path != "") m_fc.setCurrentDirectory(new File(path));

    super.restoreSettings(props);
  }
  //----------------------------------------------------------------------
  private void exportStats()
  {
    m_fc.setFileFilter(filterStats);
    if ( m_fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return; 

    File file = new File(addFileExt(m_fc.getSelectedFile().getName(),
                                    m_fc.getSelectedFile().getAbsolutePath(),
                                    statistics_ext));
      
    String pathName = file.getAbsolutePath();

    m_log.fine("Updating colour statistics");
    m_log.fine("Attempting to write colour stats to file"
               + " \"" + pathName + "\"");
    m_pal.updateColourStatistics();
    m_pal.outputStatsAbsolute(pathName);
  }
  //----------------------------------------------------------------------
  private String addFileExt(String fileName, String pathName, String ext)
  {
    int i = fileName.lastIndexOf('.');

    if (i > 0 && i < fileName.length() - 1)
    {
      return pathName;
    }
    return new String(pathName + "." + ext);
  }
}
