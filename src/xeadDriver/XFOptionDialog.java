package xeadDriver;

/*
 * Copyright (c) 2012 WATANABE kozo <qyf05466@nifty.com>,
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
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
	private static final int LIST_CELL_HEIGHT = 20;
    private JPanel jPanelMain = new JPanel();
    private JList jList = null;
    private Component parent_;
    private Dimension scrSize, dlgSize;
    private int maxCellWidth = 0;
    private ArrayList<String> optionList = new ArrayList<String>();
    private int selectedIndex = -1;
    private XFOptionDialog_keyAdapter keyListener = new XFOptionDialog_keyAdapter();
    private XFOptionDialog_mouseAdapter mouseListener = new XFOptionDialog_mouseAdapter();
    private JLabel jLabel = new JLabel();
	private FontMetrics metrics = jLabel.getFontMetrics(new java.awt.Font("Dialog", 0, 14));

    public XFOptionDialog(Component parent) {
		super();
		this.setModal(true);
		this.parent_ = parent;
		scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		jPanelMain.setLayout(null);
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

    public int request(String title) {
    	selectedIndex = -1;
    	//
    	int width = maxCellWidth + 10;
    	int height = optionList.size() * LIST_CELL_HEIGHT + 5;
		FontMetrics metricsTitle = jLabel.getFontMetrics(this.getFont());
		int dialogWidth = width + 20;
		int titleWidth = metricsTitle.stringWidth(title) + 50;
		if (dialogWidth < titleWidth) {
			dialogWidth = titleWidth;
			width = dialogWidth - 20;
		}
    	this.setTitle(title);
    	//
    	jPanelMain.removeAll();
    	jList = new JList(optionList.toArray());
		jList.setFont(new java.awt.Font("Dialog", 0, 14));
		jList.setSelectedIndex(0);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.setFixedCellHeight(LIST_CELL_HEIGHT);
		jList.addKeyListener(keyListener);
		jList.addMouseListener(mouseListener);
		jList.setBorder(BorderFactory.createEtchedBorder());
		jList.setBounds(10, 10, width, height);
		jPanelMain.add(jList);
		//
		dlgSize = new Dimension(dialogWidth, height + 20);
		jPanelMain.setPreferredSize(dlgSize);
    	if (parent_ != null && parent_.isValid()) {
    		int posY, posX;
    		Rectangle rec = parent_.getBounds();
    		Point point = parent_.getLocationOnScreen();
    		posX = point.x;
    		posY = point.y + rec.height;
    		if (posY + dlgSize.height > scrSize.height) {
    			posY = point.y - dlgSize.height;
    		}
    		this.setLocation(posX, posY);
    	} else {
    		this.setLocation((scrSize.width - dlgSize.width) / 2, (scrSize.height - dlgSize.height) / 2);
    	}
    	//
		this.pack();
    	this.setVisible(true);
    	//
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
    		if (e.getClickCount() >= 2 && optionList.size() > 0) {
    			selectedIndex = jList.getSelectedIndex();
    			clear();
    			setVisible(false);
    		}
    	}
    }
}
