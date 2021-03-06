/*
 This file belongs to the Servoy development and deployment environment, Copyright (C) 1997-2011 Servoy BV

 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU Affero General Public License as published by the Free
 Software Foundation; either version 3 of the License, or (at your option) any
 later version.

 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License along
 with this program; if not, see http://www.gnu.org/licenses or write to the Free
 Software Foundation,Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301
 */

package com.servoy.extensions.plugins.window.popup.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;

import org.mozilla.javascript.Scriptable;

import com.servoy.extensions.plugins.window.popup.IPopupShower;
import com.servoy.j2db.IForm;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.plugins.ISmartRuntimeWindow;
import com.servoy.j2db.ui.IComponent;
import com.servoy.j2db.ui.IFormUI;
import com.servoy.j2db.ui.IWindowVisibleChangeListener;
import com.servoy.j2db.ui.IWindowVisibleChangeNotifier;
import com.servoy.j2db.util.Debug;
import com.servoy.j2db.util.ServoyException;
import com.servoy.j2db.util.Utils;

/**
 * @author jcompagner
 *
 */
public class SwingPopupShower implements IPopupShower
{
	private final IClientPluginAccess clientPluginAccess;
	private JComponent elementToShowRelatedTo;
	private final IForm form;
	private final Scriptable scope;
	private final String dataprovider;
	private Container parent;
	private JDialog window;
	private Component glassPane;
	private PopupMouseListener mouseListener;
	private WindowListener windowListener;
	private final int width, height;

	/**
	 * @param elementToShowRelatedTo
	 * @param form
	 * @param record
	 * @param dataprovider
	 */
	@SuppressWarnings("nls")
	public SwingPopupShower(IClientPluginAccess clientPluginAccess, IComponent elementToShowRelatedTo, IForm form, Scriptable scope, String dataprovider,
		int width, int height)
	{
		this.clientPluginAccess = clientPluginAccess;
		if (elementToShowRelatedTo instanceof JComponent)
		{
			this.elementToShowRelatedTo = (JComponent)elementToShowRelatedTo;
		}
		this.form = form;
		this.scope = scope;
		this.dataprovider = dataprovider;
		this.width = width;
		this.height = height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#show()
	 */
	public void show()
	{
		parent = null;
		if (elementToShowRelatedTo != null)
		{
			parent = elementToShowRelatedTo.getParent();
		}
		else
		{
			parent = ((ISmartRuntimeWindow)clientPluginAccess.getCurrentRuntimeWindow()).getWindow();
		}

		while (parent != null && !(parent instanceof Dialog) && !(parent instanceof Frame))
		{
			parent = parent.getParent();
		}

		if (parent instanceof Dialog)
		{
			window = new JDialog((Dialog)parent);
		}
		else if (parent instanceof Frame)
		{
			window = new JDialog((Frame)parent);
		}

		if (window != null)
		{
			windowListener = new WindowListener();
			((Window)parent).addComponentListener(windowListener);
			((Window)parent).addWindowStateListener(windowListener);

			if (parent instanceof IWindowVisibleChangeNotifier)
			{
				((IWindowVisibleChangeNotifier)parent).addWindowVisibleChangeListener(windowListener);
			}

			this.window.setFocusableWindowState(true);
			this.window.setFocusable(true);
			this.window.setUndecorated(true);
			this.window.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
			if (Utils.isAppleMacOS())
			{
				this.window.getRootPane().putClientProperty("Window.shadow", Boolean.FALSE); //$NON-NLS-1$
				this.window.getRootPane().putClientProperty("apple.awt.draggableWindowBackground", Boolean.FALSE);
			}

			final IFormUI formUI = form.getFormUI();

			if (!formUI.isOpaque())
			{
				this.window.getRootPane().setOpaque(false);
				this.window.setBackground(new Color(0, 0, 0, 0));
			}

			window.getContentPane().setLayout(new BorderLayout(0, 0));
			window.getContentPane().add((Component)formUI, BorderLayout.CENTER);
			window.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy()
			{
				@Override
				public Component getComponentBefore(Container aContainer, Component aComponent)
				{
					return ((Container)formUI).getFocusTraversalPolicy().getComponentBefore((Container)formUI, (Component)formUI);
				}
			});

			if (width > -1 && height > -1) window.setSize(width, height);
			else if (width > -1) window.setSize(width, window.getPreferredSize().height);
			else if (height > -1) window.setSize(window.getPreferredSize().width, height);
			else window.pack();

			if (elementToShowRelatedTo != null)
			{
				Point locationOnScreen = elementToShowRelatedTo.getLocationOnScreen();
				locationOnScreen.y += elementToShowRelatedTo.getHeight();

				Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration());
				Rectangle bounds = window.getGraphicsConfiguration().getBounds();

				int screenWidth = (bounds.width + bounds.x - screenInsets.left - screenInsets.right);
				int borerWidth = (parent.getWidth() - elementToShowRelatedTo.getRootPane().getContentPane().getWidth()) / 2;
				int parentRightEdge = parent.getLocation().x + parent.getWidth() - borerWidth;
				//if necessary right align popup on related component
				if (locationOnScreen.x > parentRightEdge) locationOnScreen.x = parentRightEdge - elementToShowRelatedTo.getWidth();
				if ((locationOnScreen.x + window.getSize().width) > parentRightEdge)
				{
					//element to show related to is half off screen or completely off screen to the right
					if (locationOnScreen.x + elementToShowRelatedTo.getWidth() > screenWidth)
					{
						locationOnScreen.x = screenWidth - window.getWidth();
					}
					else
					{// normal case , parent frame  is within the bounds of the screen
						int elementRight = elementToShowRelatedTo.getLocationOnScreen().x + elementToShowRelatedTo.getWidth();
						int halfOfRelatedElementInViewPort = 0;
						//half ot he element is in the viewport , the rest you have to scroll to the right to see it 
						if ((elementRight > parentRightEdge) && (elementToShowRelatedTo.getLocationOnScreen().x < parentRightEdge)) halfOfRelatedElementInViewPort = elementToShowRelatedTo.getLocationOnScreen().x +
							elementToShowRelatedTo.getWidth() - parentRightEdge;
						locationOnScreen.x = locationOnScreen.x - window.getSize().width + elementToShowRelatedTo.getWidth() - halfOfRelatedElementInViewPort;
					}
				}

				int screenHeight = (bounds.height + bounds.y - screenInsets.top - screenInsets.bottom);
				int parentBottomEdge = parent.getLocation().y + parent.getHeight() - borerWidth;
				//if necessary popup on the top of the related component
				if (window.getSize().height + locationOnScreen.y > parentBottomEdge)
				{
					if (locationOnScreen.y - elementToShowRelatedTo.getHeight() - window.getSize().height > screenInsets.top)
					{
						locationOnScreen.y = locationOnScreen.y - elementToShowRelatedTo.getHeight() - window.getSize().height;
					}
					else
					{
						locationOnScreen.y = parentBottomEdge - window.getSize().height;
					}
				}

				if (locationOnScreen.x < bounds.x) locationOnScreen.x = bounds.x;
				if (locationOnScreen.y < bounds.y) locationOnScreen.y = bounds.y;
				//if the related element is way below screen still show the popup on the screen
				if (locationOnScreen.y + window.getSize().height > screenHeight) locationOnScreen.y = screenHeight - window.getSize().height;

				window.setLocation(locationOnScreen);
			}
			else
			{
				window.setLocationRelativeTo(parent);
			}
			window.setVisible(true);

			if (parent instanceof RootPaneContainer)
			{
				glassPane = ((RootPaneContainer)parent).getGlassPane();
				glassPane.setVisible(true);
				mouseListener = new PopupMouseListener();
				glassPane.addMouseListener(mouseListener);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#close(java.lang.Object)
	 */
	@SuppressWarnings("nls")
	public void close(Object retval)
	{
		scope.put(dataprovider, scope, retval);
		cancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#cancel()
	 */
	public void cancel()
	{
		if (window != null && window.isVisible()) closeWindow(true);
	}

	/**
	 * 
	 */
	private void closeWindow(boolean removeMouseListener)
	{
		glassPane.setVisible(false);
		window.setVisible(false);
		if (parent != null)
		{
			parent.removeComponentListener(windowListener);
			if (parent instanceof Window)
			{
				((Window)parent).removeWindowStateListener(windowListener);
			}
			if (parent instanceof IWindowVisibleChangeNotifier)
			{
				((IWindowVisibleChangeNotifier)parent).removeWindowVisibleChangeListener(windowListener);
			}
		}
		window.getContentPane().removeAll();
		window.dispose();
		if (removeMouseListener) glassPane.removeMouseListener(mouseListener);
		try
		{
			form.setUsingAsExternalComponent(false);
		}
		catch (ServoyException e)
		{
			Debug.error(e);
		}
	}

	/**
	 * @author jcompagner
	 *
	 */
	private final class WindowListener implements ComponentListener, WindowStateListener, IWindowVisibleChangeListener
	{
		public void componentShown(ComponentEvent e)
		{
			closeWindow(true);
		}

		public void componentResized(ComponentEvent e)
		{
			closeWindow(true);
		}

		public void componentMoved(ComponentEvent e)
		{
			closeWindow(true);
		}

		public void componentHidden(ComponentEvent e)
		{
			closeWindow(true);
		}

		public void windowStateChanged(WindowEvent e)
		{
			closeWindow(true);
		}

		/*
		 * @see com.servoy.j2db.ui.IVisibleChangeListener#beforeVisibleChange(com.servoy.j2db.ui.ISupportVisibleChangeListener, boolean)
		 */
		public void beforeVisibleChange(IWindowVisibleChangeNotifier component, boolean newVisibleState)
		{
			if (!newVisibleState) closeWindow(true);
		}
	}

	private class PopupMouseListener extends MouseAdapter
	{
		private Component dispatchComponent;
		private boolean mousePressed;

		@Override
		public void mousePressed(MouseEvent e)
		{
			mousePressed = true;
			closeWindow(false);
			Point p2 = SwingUtilities.convertPoint(glassPane, e.getPoint(), parent);
			dispatchComponent = parent.findComponentAt(p2.x, p2.y);
			if (dispatchComponent != null)
			{
				Point p3 = SwingUtilities.convertPoint(parent, p2, dispatchComponent);
				MouseEvent e2 = new MouseEvent(dispatchComponent, e.getID(), e.getWhen(), e.getModifiers(), p3.x, p3.y, e.getClickCount(), e.isPopupTrigger());
				dispatchComponent.dispatchEvent(e2);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if (mousePressed)
			{
				glassPane.removeMouseListener(mouseListener);
				if (dispatchComponent != null)
				{
					Point p2 = SwingUtilities.convertPoint(glassPane, e.getPoint(), parent);
					Point p3 = SwingUtilities.convertPoint(parent, p2, dispatchComponent);
					MouseEvent e2 = new MouseEvent(dispatchComponent, e.getID(), e.getWhen(), e.getModifiers(), p3.x, p3.y, e.getClickCount(),
						e.isPopupTrigger());
					dispatchComponent.dispatchEvent(e2);
				}
			}
		}
	}
}
