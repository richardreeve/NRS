package nrs.nrsgui;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import nrs.csl.CSL_Element_Attribute;
import nrs.csl.CSL_Element_NodeDescription;

/**
 * Class for displaying the root level of a network. This has been added
 * because I couldn't use the other class, BaseNetworkBrowser. The
 * reason that can't be used is that it expects to contain a single
 * Node, which indicates the current context: however there is currently
 * no class for representing the "root node". It might be possible to
 * pursue that option - ie creating a RootNode which extends Node etc -
 * but that requires deciding what to do about its CSL, attributes,
 * variables, DisplayDelegate requiremente etc.
 *
 * If this class persists then is should be possible to merge some of
 * its code with {@link BaseNetworkBrowser} into a base class.  Or
 * significant aspects could be kept separate
 *
 * @author  Darren Smith
 */
public class RootNetworkBrowser extends BaseNetworkBrowser
{
  //---------------------------------------------------------------------
  /**
   * Constructor
   */
  RootNetworkBrowser(BrowserManager browserMan)
  {
    super("root", new RootNetworkBrowserPopupMenu(), browserMan);
  }
  //-------------------------------------------------------------------------
  /**
   */
  public void closeView()
  {
    // nothing to do - this should not be called, since root browsers do
    // not have a "close" menu item
  }
}
