package xeadDriver;

/*
 * Copyright (c) 2014 WATANABE kozo <qyf05466@nifty.com>,
 * All rights reserved.
 *
 * This file is part of XEAD Editor.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the XEAD Project nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.*;
import javax.swing.undo.UndoManager;

public class XFLongTextEditor extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final int DIALOG_WIDTH = 600;
	private static final int DIALOG_HEIGHT = 300;
	private JScrollPane jScrollPane = new JScrollPane();
	private JTextArea jTextArea = new JTextArea();
	private ArrayList<String> dataTypeOptionList_ = null;
	private Action undoAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			undo();
		}
	};
	private Action redoAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			redo();
		}
	};
	private UndoManager undoManager = new UndoManager();
	private Session session_;
	
	public XFLongTextEditor(Session session) {
		super(session, "", true);
		session_ = session;
		try {
			init();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	private void init() throws Exception {
		this.getContentPane().setLayout(new BorderLayout());
		jTextArea.addFocusListener(new ComponentFocusListener());
		jTextArea.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTextArea.getDocument().addUndoableEditListener(undoManager);
		jScrollPane.getViewport().setView(jTextArea);
		InputMap inputMap = jScrollPane.getInputMap(JSplitPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.clear();
		ActionMap actionMap = jScrollPane.getActionMap();
		actionMap.clear();
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "UNDO");
		actionMap.put("UNDO", undoAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "REDO");
		actionMap.put("REDO", redoAction);

		this.setResizable(true);
		this.setPreferredSize(new Dimension(DIALOG_WIDTH, DIALOG_HEIGHT));
		this.getContentPane().add(jScrollPane,  BorderLayout.CENTER);
		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((scrSize.width - DIALOG_WIDTH) / 2, (scrSize.height - DIALOG_HEIGHT) / 2);
		this.pack();
	}

	public String request(String functionTitle, String fieldCaption, ArrayList<String> dataTypeOptionList, String text, boolean isEditable) {
		this.setTitle(functionTitle + " - " +  fieldCaption);
		dataTypeOptionList_ = dataTypeOptionList;
		jTextArea.setText(text);
		jTextArea.setCaretPosition(0);
		jTextArea.setEditable(isEditable);
		undoManager.discardAllEdits();
		super.setVisible(true);
		return jTextArea.getText();
	}

	class ComponentFocusListener implements FocusListener{
		public void focusLost(FocusEvent event){
			if (getInputContext() != null) {
				getInputContext().setCompositionEnabled(false);
			}
		}
		public void focusGained(FocusEvent event){
			Character.Subset[] subsets  = new Character.Subset[] {java.awt.im.InputSubset.LATIN_DIGITS};
			String lang = Locale.getDefault().getLanguage();
			if (dataTypeOptionList_.contains("KANJI") || dataTypeOptionList_.contains("ZIPADRS")) {
				if (lang.equals("ja")) {
					subsets = new Character.Subset[] {java.awt.im.InputSubset.KANJI};
				}
				if (lang.equals("ko")) {
					subsets = new Character.Subset[] {java.awt.im.InputSubset.HANJA};
				}
				if (lang.equals("zh")) {
					subsets = new Character.Subset[] {java.awt.im.InputSubset.TRADITIONAL_HANZI};
				}
				getInputContext().setCharacterSubsets(subsets);
				getInputContext().setCompositionEnabled(true);
			} else {
				if (dataTypeOptionList_.contains("KATAKANA") && lang.equals("ja")) {
					subsets = new Character.Subset[] {java.awt.im.InputSubset.HALFWIDTH_KATAKANA};
					getInputContext().setCharacterSubsets(subsets);
					getInputContext().setCompositionEnabled(true);
				} else {
					getInputContext().setCharacterSubsets(subsets);
					getInputContext().setCompositionEnabled(false);
				}
			}
		}
	}
	
	public void undo() {
		if (undoManager.canUndo()) {
			undoManager.undo();
		}
	}
	
	public void redo() {
		if (undoManager.canRedo()) {
			undoManager.redo();
		}
	}
}
