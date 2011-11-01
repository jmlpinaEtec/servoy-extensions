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

package com.servoy.extensions.plugins.window.popup.wicket;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;

import com.servoy.extensions.plugins.window.popup.IPopupShower;
import com.servoy.j2db.IForm;
import com.servoy.j2db.dataprocessing.IRecord;
import com.servoy.j2db.plugins.IClientPluginAccess;
import com.servoy.j2db.server.headlessclient.IPageContributor;
import com.servoy.j2db.server.headlessclient.IRepeatingView;
import com.servoy.j2db.server.headlessclient.IWebClientPluginAccess;
import com.servoy.j2db.server.headlessclient.dataui.WebEventExecutor;
import com.servoy.j2db.ui.IComponent;

/**
 * @author jcompagner
 *
 */
public class WicketPopupShower implements IPopupShower
{
	private final Component elementToShowRelatedTo;
	private final IForm form;
	private final IRecord record;
	private final String dataprovider;
	private final IClientPluginAccess clientPluginAccess;

	/**
	 * @param elementToShowRelatedTo
	 * @param form
	 * @param record
	 * @param dataprovider
	 * @param clientPluginAccess 
	 */
	@SuppressWarnings("nls")
	public WicketPopupShower(IComponent elementToShowRelatedTo, IForm form, IRecord record, String dataprovider, IClientPluginAccess clientPluginAccess)
	{
		if (!(elementToShowRelatedTo instanceof Component)) throw new IllegalArgumentException("element to show the popup on is not a wicket Component: " +
			elementToShowRelatedTo);
		this.elementToShowRelatedTo = (Component)elementToShowRelatedTo;
		this.form = form;
		this.record = record;
		this.dataprovider = dataprovider;
		this.clientPluginAccess = clientPluginAccess;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#show()
	 */
	@SuppressWarnings("nls")
	public void show()
	{
		IPageContributor pageContributor = ((IWebClientPluginAccess)clientPluginAccess).getPageContributor();
		IRepeatingView repeatingView = pageContributor.getRepeatingView();

		if (repeatingView.getComponent("popup") != null)
		{
			repeatingView.removeComponent("popup");
		}
		repeatingView.addComponent("popup", new PopupPanel(repeatingView.newChildId(), form, elementToShowRelatedTo));

		final WebMarkupContainer container = new WebMarkupContainer(repeatingView.newChildId());
		container.add(new SimpleAttributeModifier("style", "position:absolute;z-index:990;top:0px;right:0px;bottom:0px;left:0px"));
		container.add(new AjaxEventBehavior("onclick")
		{
			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				Page page = container.getPage();
				close();
				WebEventExecutor.generateResponse(target, page);
			}
		});
		repeatingView.addComponent("blocker", container);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see com.servoy.extensions.plugins.window.popup.IPopupShower#close(java.lang.Object)
	 */
	@SuppressWarnings("nls")
	public void close(Object retval)
	{
		if (record.startEditing())
		{
			record.setValue(dataprovider, retval);
		}
		else
		{
			throw new RuntimeException("record dataprovider " + dataprovider + " can't be set to " + retval +
				" in a form popup close because it can't be editted");
		}

		close();

	}

	/**
	 * 
	 */
	private void close()
	{
		IPageContributor pageContributor = ((IWebClientPluginAccess)clientPluginAccess).getPageContributor();
		IRepeatingView repeatingView = pageContributor.getRepeatingView();
		repeatingView.removeComponent("popup");
		repeatingView.removeComponent("blocker");
	}

}