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

package com.servoy.extensions.plugins.spellcheck;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.servoy.j2db.util.gui.JEscapeDialog;

public class SpellCheckerGUI extends JEscapeDialog implements ActionListener, WindowListener
{
	private final SpellCheckClientPlugin plugin;
	private final SpellCheckerForm spellCheckerForm;

	private SpellCheckEvent spellEvent;
	private final ResourceBundle messages;

	public SpellCheckerGUI(SpellCheckClientPlugin plugin, JDialog w, ResourceBundle messages, boolean modal)
	{
		super(w, modal);
		this.plugin = plugin;
		this.spellCheckerForm = new SpellCheckerForm(messages);
		this.messages = messages;
		init();
	}

	public SpellCheckerGUI(SpellCheckClientPlugin plugin, JFrame w, ResourceBundle messages, boolean modal)
	{
		super(w, modal);
		this.plugin = plugin;
		this.spellCheckerForm = new SpellCheckerForm(messages);
		this.messages = messages;
		init();
	}


	private void init()
	{
		this.setTitle(messages.getString(SpellCheckerUtils.SPELLING));
		this.getContentPane().add(spellCheckerForm);
		spellCheckerForm.addActionListener(this);
		this.addWindowListener(this);
		pack();

	}

	public void check(SpellCheckEvent spellCheckEvent)
	{
		if (spellCheckEvent.getSuggestions().size() <= 0) //this does not seem ok
		{
			this.setVisible(false);
		}
		spellCheckerForm.addSpellEvent(spellCheckEvent);

	}

	public void setTheCurrentSpellCheckEvent(SpellCheckEvent spellEvent)
	{
		this.spellEvent = spellEvent;
		spellCheckerForm.setSpellEvent(spellEvent);
		highlightInvalidWordOfSpellEvent(spellEvent);
	}

	public void highlightInvalidWordOfSpellEvent(SpellCheckEvent spellEvent)
	{
		//get location of spellEvent word in the text to check;
		String invalidWord = spellEvent.getInvalidWord();
		String checkedText = plugin.getCheckedComponent().getText();
		int currentCaretPosition = plugin.getCheckedComponent().getCaretPosition();
		if (currentCaretPosition >= checkedText.length())
		{
			plugin.getCheckedComponent().setCaretPosition(0);
			currentCaretPosition = plugin.getCheckedComponent().getCaretPosition();
		}
		int start = checkedText.indexOf(invalidWord, currentCaretPosition);
		int end = spellEvent.getInvalidWord().length() + start;

		plugin.getCheckedComponent().setSelectionStart(start);
		plugin.getCheckedComponent().setSelectionEnd(end);
	}

	public void unhighlight()
	{
		//we unhighlight (or put the caret to position zero) 
		plugin.getCheckedComponent().setCaretPosition(0);
	}

	public void setTheFirstSpellEvent()
	{
		//we have at least one spellCheckEvent
		if (spellCheckerForm.getSpellEventsList().size() > 0)
		{
			SpellCheckEvent sce = spellCheckerForm.getSpellEventsList().get(0);
			setTheCurrentSpellCheckEvent(sce);
		}
		else
		{
			//nothing to spell check - the text is correct.
			this.setVisible(false);
			JOptionPane.showMessageDialog(this, messages.getString(SpellCheckerUtils.SPELLING_CHECK_COMPLETE), messages.getString(SpellCheckerUtils.SPELLING),
				JOptionPane.INFORMATION_MESSAGE);
			plugin.unsetTheEditFormatter();
		}
	}

	public boolean hasAtLeastOneSpellEvent()
	{
		if (spellCheckerForm.getSpellEventsList().size() > 0) return true;
		return false;
	}

	private void emptySpellCheckEvents()
	{
		spellCheckerForm.getSpellEventsList().clear();
	}

	private void emptyCurrentSpellCheckEventIfAny()
	{
		spellCheckerForm.setCurrentSpellCheckEvent(null);
	}


	private void clearTheGUIForm()
	{
		spellCheckerForm.clearWrongWordLabel();
		spellCheckerForm.clearSuggestionsList();
		spellCheckerForm.clearCheckedText();
	}

	public void removeEventsAndGuiForm()
	{
		//clear events 
		emptySpellCheckEvents();
		emptyCurrentSpellCheckEventIfAny();

		//clear entire gui spellcheck form
		clearTheGUIForm();
	}


	@Override
	protected void cancel()
	{
		this.setVisible(false);
	}

	public void actionPerformed(ActionEvent e)
	{
		//TODO check the status below and act accordingly
		plugin.fireAndHandleEvent(spellEvent);

		// remove former spell check event(s) (which have been consumed)
		if (spellEvent.getAction() == SpellCheckEvent.REPLACEALL) spellCheckerForm.removeAllOccurencesOfCurrentSpellCheckEvent(spellEvent);
		else if (spellEvent.getAction() == SpellCheckEvent.CANCEL) emptySpellCheckEvents();
		else spellCheckerForm.removeSpellEvent(spellEvent);

		// set next spell check event
		if (spellCheckerForm.getSpellEventsList().size() > 0)
		{
			setTheCurrentSpellCheckEvent(spellCheckerForm.getSpellEventsList().get(0));
			this.setVisible(true);
		}
		else
		{
			//no more spell events, therefore the spell check is complete
			this.setVisible(false);
			//we do not want the message to show up if we clicked "Cancel"
			if (spellEvent.getAction() != SpellCheckEvent.CANCEL) JOptionPane.showMessageDialog(this,
				messages.getString(SpellCheckerUtils.SPELLING_CHECK_COMPLETE), messages.getString(SpellCheckerUtils.SPELLING), JOptionPane.INFORMATION_MESSAGE);
			plugin.unsetTheEditFormatter();
			unhighlight();
		}
	}

	public void windowActivated(WindowEvent e)
	{
	}

	public void windowClosed(WindowEvent e)
	{
		unhighlight();
	}

	//called when the Spellchecker window is closed via X-button
	public void windowClosing(WindowEvent e)
	{
		plugin.unsetTheEditFormatter();
		unhighlight();
	}

	public void windowDeactivated(WindowEvent e)
	{
	}

	public void windowDeiconified(WindowEvent e)
	{
	}

	public void windowIconified(WindowEvent e)
	{
	}

	public void windowOpened(WindowEvent e)
	{
	}
}
