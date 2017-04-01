package nrs.toolboxes;

import javax.swing.SpringLayout;
import javax.swing.Spring;
import java.awt.Container;
import java.awt.Component;

/**
 * <p>Subclass of SpringLayout, enriched with a set of methods to ease the use of spring layouts. 
 * <p>Spring layout is very useful for tabular structures similar to those possible with 
 * GridBagLayout where maximum and minimum sizes have to be respected. In GridBagLayout components 
 * either grow unlimited if vertical or horizontal fill is enabled, or not at all (more precisely
 * the components are at preferred sizes and instantly resize to minimum size if the container is
 * too small for the former).
 * For a use of this class see {@link nrs.tracker.robottrack.CalibrationWindow}.
 *
 * @author Tobias Oberlies
 */
public class MacroSpringLayout extends SpringLayout
{
	
	/**
	 * Constructs a new MacroSpringLayout.
	 */
	public MacroSpringLayout()
	{
		super();
	}
	
	
	/**
	 * Places a component below another component. 
	 *
	 * @param component the component to be positioned.
	 * @param pad the padding between the components.
	 * @param referenceComponent the reference component.
	 */
	public void putBelow(Component component, int pad, Component referenceComponent)
	{
		// Check arguments
		if (component == null)
			return;
		
		// Impose constraints
		if (referenceComponent == null)
		{
			// Put below the top of the container
			super.putConstraint(SpringLayout.NORTH, component, pad, SpringLayout.NORTH, component.getParent());
		}
		else
		{
			// Put below the given component
			super.putConstraint(SpringLayout.NORTH, component, pad, SpringLayout.SOUTH, referenceComponent);
		}
	}
	

	/**
	 * Places a set of components below another component. 
	 *
	 * @param components the components to be positioned. The components will be aligned at the top. To 
	 * create an array of the required type use <code>new Component[]{component1, component2, <i>...</i>}</code>.
	 * @param pad the padding between the components.
	 * @param referenceComponent the reference components.
	 */
	public void putBelow(Component[] components, int pad, Component referenceComponent)
	{
		// Check arguments
		if (components == null || components.length < 1)
			return;
		
		// Impose constraints for each component
		if (referenceComponent == null)
		{	
			// Put below the top of the container
			for (int i=0; i < components.length; i++)
				super.putConstraint(SpringLayout.NORTH, components[i], pad, SpringLayout.NORTH, components[i].getParent());
		}
		else
		{
			// Put below the given component
			for (int i=0; i < components.length; i++)
				super.putConstraint(SpringLayout.NORTH, components[i], pad, SpringLayout.SOUTH, referenceComponent);
		}
	}
	
	
	/**
	 * Places a component below a set of component. The component will be placed below the lowest
	 * of the reference components.
	 *
	 * @param component the component to be positioned.
	 * @param pad the padding between the components.
	 * @param referenceComponents the set of reference components.
	 */
	public void putBelowSet(Component component, int pad, Component[] referenceComponents)
	{
		// Check arguments
		if (component == null)
			return;
		
		// Cast and use putBelowSet(Component[], Component[])
		putBelowSet(new Component[]{component}, pad, referenceComponents);
	}
	
	/**
	 * Places a set of components below another set of component. The components will be placed below the lowest
	 * of the reference components.
	 *
	 * @param components the components to be positioned.The components will be aligned at the top. 
	 * @param pad the padding between the components.
	 * @param referenceComponents the set of reference components.
	 */
	public void putBelowSet(Component[] components, int pad, Component[] referenceComponents)
	{
		// Check arguments
		if (components == null || components.length < 1)
			return;
		if (referenceComponents == null || referenceComponents.length<1)
		{
			// Use putBelow(Component[], Component)
			putBelow(components, pad, null);
		}
		
		
		// Compute spring that is maximum of the SOUTH anchors of the components
		Spring maxSouthSpring = super.getConstraint(SOUTH, referenceComponents[0]);
		for (int i=1; i<referenceComponents.length; i++)
			maxSouthSpring = Spring.max(maxSouthSpring, super.getConstraint(SOUTH, referenceComponents[1]));
		
		// Add padding
		maxSouthSpring = Spring.sum(maxSouthSpring, Spring.constant(pad));
		
		// Assign computed spring as y coordinate
		for (int i=0; i < components.length; i++)
		{
			super.getConstraints(components[i]).setY(maxSouthSpring);
		}
	}
	
	
	/**
	 * Places a component to the right of another component. 
	 *
	 * @param component the component to be positioned.
	 * @param pad the padding between the components.
	 * @param referenceComponent the reference component.
	 */
	public void putRightOf(Component component, int pad, Component referenceComponent)
	{
		// Check arguments
		if (component == null)
			return;
		
		// Impose constraints
		if (referenceComponent == null)
		{
			// Align with the lefth edge of the container
			super.putConstraint(SpringLayout.WEST, component, pad, SpringLayout.WEST, component.getParent());
		}
		else
		{
			// Put to the right of the given component
			super.putConstraint(SpringLayout.WEST, component, pad, SpringLayout.EAST, referenceComponent);
		}
	}
	
	/**
	 * Places a set of components to the right of another component. 
	 *
	 * @param components the components to be positioned. The components will be aligned at the left. To 
	 * create an array of the required type use <code>new Component[]{component1, component2, <i>...</i>}</code>.
	 * @param pad the padding between the components.
	 * @param referenceComponent the reference components.
	 */
	public void putRightOf(Component[] components, int pad, Component referenceComponent)
	{
		// Check arguments
		if (components == null || components.length < 1)
			return;
		
		// Impose constraints for each component
		if (referenceComponent == null)
		{	
			// Align with the lefth edge of the container
			for (int i=0; i < components.length; i++)
				super.putConstraint(SpringLayout.WEST, components[i], pad, SpringLayout.WEST, components[i].getParent());
		}
		else
		{
			// Put to the right of the given component
			for (int i=0; i < components.length; i++)
				super.putConstraint(SpringLayout.WEST, components[i], pad, SpringLayout.EAST, referenceComponent);
		}
	}
	
	
	/**
	 * Places a component to the right of a set of component. The component will be placed to the
	 * right of the rightmost reference component.
	 *
	 * @param component the component to be positioned.
	 * @param pad the padding between the components.
	 * @param referenceComponents the set of reference components.
	 */
	public void putRightOfSet(Component component, int pad, Component[] referenceComponents)
	{
		// Check arguments
		if (component == null)
			return;
		
		// Cast and use putRightOfSet(Component[], Component[])
		putRightOfSet(new Component[]{component}, pad, referenceComponents);
	}
	
	/**
	 * Places a set of components to the right of another set of component. The components will be 
	 * placed to the right of the rightmost reference component.
	 *
	 * @param components the components to be positioned.The components will be aligned at the left. 
	 * @param pad the padding between the components.
	 * @param referenceComponents the set of reference components.
	 */
	public void putRightOfSet(Component[] components, int pad, Component[] referenceComponents)
	{
		// Check arguments
		if (components == null || components.length < 1)
			return;
		if (referenceComponents == null || referenceComponents.length<1)
		{
			// Use putRightOf(Component[], Component)
			putRightOf(components, pad, null);
		}
		
		
		// Compute spring that is maximum of the EAST anchors of the components
		Spring maxEastSpring = super.getConstraint(EAST, referenceComponents[0]);
		for (int i=1; i<referenceComponents.length; i++)
			maxEastSpring = Spring.max(maxEastSpring, super.getConstraint(EAST, referenceComponents[1]));
		
		// Add padding
		maxEastSpring = Spring.sum(maxEastSpring, Spring.constant(pad));
		
		// Assign computed spring as x coordinate
		for (int i=0; i < components.length; i++)
		{
			super.getConstraints(components[i]).setX(maxEastSpring);
		}
	}
	
	
	/**
	 * Create a new flexible <code>Spring</code> with the same preferred size as the specified 
	 * component. The spring is very flexible,, i.e. the minimum size is zero, the maximum size
	 * Integer.MAX_VALUE/4. For some reason using Integer.MAX_VALUE prevents the SpringLayout 
	 * container from being displayed if it is inside a container with BoxLayout.
	 */
	public Spring createFlexibleWidthSpring(Component c)
	{
		// Create a new spring with the same preferred size
		return Spring.constant(0, c.getPreferredSize().width, Integer.MAX_VALUE/4);
	}
	
	/**
	 * Creates a new <code>Spring</code> that resizes like the specified spring but only in the range
	 * specified by the components minimum and maximum size.
	 */
	public Spring createRespectfulWidthSpring(Component c, Spring resizingSpring)
	{
		// Create a new spring that is the minimum of the maximum size of the component and the resizing spring ( min(a,b) = -max(-a,-b) )
		Spring limited = Spring.minus(Spring.max(Spring.minus(resizingSpring), Spring.constant(-c.getMaximumSize().width)));

		// Create a new spring that is the maximum of the minimum size of the component and the spring computed above
		limited = Spring.max(limited, Spring.constant(c.getMinimumSize().width));

		return limited;
	}
	
	
	/*		
	
	private void putSameY(SpringLayout layout, Component newComponent, Component referenceComponent)
	{
		layout.putConstraint(SpringLayout.NORTH, newComponent, 0, SpringLayout.NORTH, referenceComponent);
	}

	private Spring maxWidth(SpringLayout layout, Component[] components)
	{
		//Calculate the spring that is maximum of the widths.
        Spring maxWidthSpring = layout.getConstraints(components(0)).getWidth();
        
		for (int i = 1; i < components.length; i++)
		{
            maxWidthSpring = Spring.max(maxWidthSpring, layout.getConstraints(components(i)).getWidth());
        }
	}
	
	private Spring maxHeight(SpringLayout layout, Component[] components)
	{
		//Calculate the spring that is maximum of the widths.
        Spring maxWidthSpring = layout.getConstraints(components(0)).getHeight();
        
		for (int i = 1; i < components.length; i++)
		{
            maxWidthSpring = Spring.max(maxWidthSpring, layout.getConstraints(components(i)).getHeight());
        }
	}
	
	private void setWidths(SpringLayout layout, Component[] components, Spring spring)
	{
		// Constrain all components with the given spring. This ensures that all components always
		// the same width. Assigning a Spring to a Component doesn't clone any information, but stores a
		// reference to that spring. So by assigning a singleton spring to multiple components fixes 
		// them to a common size.
		for (int i = 0; < components.length; i++)
		{
            layout.getConstraints(components(i)).setWidth(spring);
        }
	}*/
}

