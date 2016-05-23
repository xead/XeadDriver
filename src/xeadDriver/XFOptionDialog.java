package xeadDriver;

/*
 * Copyright (c) 2014 WATANABE kozo <qyf05466@nifty.com>,
 * All rights reserved.
 *
 * This file is part of XEAD Driver.
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

////////////////////////////////////////////////////////////////
// This is a public class used in Table-Script.               //
// Note that public classes are defined in its own java file. //
////////////////////////////////////////////////////////////////
public class XFOptionDialog extends JDialog {
	private static final long serialVersionUID = 1L;
    private JPanel jPanelMain = new JPanel();
    private JList jList = null;
	private Session session_;
    private Dimension scrSize, dlgSize;
    private int maxCellWidth = 0;
    private int explicitWidth = 0;
    private ArrayList<String> optionList = new ArrayList<String>();
    private int selectedIndex = -1;
    private XFOptionDialog_keyAdapter keyListener = new XFOptionDialog_keyAdapter();
    private XFOptionDialog_mouseAdapter mouseListener = new XFOptionDialog_mouseAdapter();
    private JLabel jLabel = new JLabel();
	private FontMetrics metrics;

    public XFOptionDialog(Session session) {
		super();
		session_ = session;
		this.setModal(true);
		scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		jPanelMain.setLayout(new BorderLayout());
		metrics = jLabel.getFontMetrics(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		this.getContentPane().add(jPanelMain,  BorderLayout.CENTER);
		this.setResizable(false);
    }

    public void clear() {
    	optionList.clear();
    	maxCellWidth = 0;
    }

    public void addOption(String option) {
		optionList.add(option);
    	int width = metrics.stringWidth(option); 
    	if (width > maxCellWidth) {
    		maxCellWidth = width;
    	}
    }

    public void setWidth(int width) {
    	explicitWidth = width;
    }

    public int request(String title) {
    	selectedIndex = -1;

    	int width = maxCellWidth + 10;
    	int height = optionList.size() * XFUtility.FIELD_UNIT_HEIGHT;
		int dialogWidth = width + 20;
		if (explicitWidth == 0) {
			explicitWidth = dialogWidth;
		}
    	this.setTitle(title);

    	jPanelMain.removeAll();
    	jList = new JList(optionList.toArray());
		jList.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jList.setSelectedIndex(0);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.setFixedCellHeight(XFUtility.FIELD_UNIT_HEIGHT);
		jList.addKeyListener(keyListener);
		jList.addMouseListener(mouseListener);
		jList.setBorder(null);
		jPanelMain.add(jList, BorderLayout.CENTER);

		dlgSize = new Dimension(explicitWidth, height + 10);
		jPanelMain.setPreferredSize(dlgSize);
   		this.setLocation((scrSize.width - dlgSize.width) / 2, (scrSize.height - dlgSize.height) / 2);

		this.pack();
    	this.setVisible(true);

    	return selectedIndex;
    }

    protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			clear();
		}
	}

    class XFOptionDialog_keyAdapter extends java.awt.event.KeyAdapter {
    	public void keyPressed(KeyEvent e) {
    		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
    			selectedIndex = jList.getSelectedIndex();
    			clear();
    			setVisible(false);
    		}
    	}
    }

    class XFOptionDialog_mouseAdapter extends java.awt.event.MouseAdapter {
    	public void mouseClicked(MouseEvent e) {
    		if (optionList.size() > 0) {
    			selectedIndex = jList.getSelectedIndex();
    			clear();
    			setVisible(false);
    		}
    	}
    }
}
