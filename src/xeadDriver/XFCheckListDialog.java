package xeadDriver;

/*
 * Copyright (c) 2013 WATANABE kozo <qyf05466@nifty.com>,
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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.ListSelectionModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

////////////////////////////////////////////////////////////////
// This is a public class used in Table-Script.               //
// Note that public classes are defined in its own java file. //
////////////////////////////////////////////////////////////////
public class XFCheckListDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final int LIST_CELL_HEIGHT = 22;
    private JPanel jPanelMain = new JPanel();
	private DefaultListModel listModel = new DefaultListModel();
    private JList jList = new JList(listModel);
    private JScrollPane jScrollPane = new JScrollPane();
	private JButton jButtonOK = new JButton();
	private JButton jButtonCancel = new JButton();
	private JPanel jPanelButtons = new JPanel();
    private Dimension scrSize, dlgSize;
    private ArrayList<String> keyList = new ArrayList<String>();
    private ArrayList<String> textList = new ArrayList<String>();
    private ArrayList<String> initialSelectKeyList = new ArrayList<String>();
    private int reply = -1;
    private String checkedKeyList = "";
    private XFCheckListDialog_keyAdapter keyListener = new XFCheckListDialog_keyAdapter();
    private XFCheckListDialog_mouseAdapter mouseListener = new XFCheckListDialog_mouseAdapter();

    public XFCheckListDialog() {
		super();

		this.setModal(true);
		this.setResizable(false);
		this.getContentPane().setLayout(new BorderLayout());
		jPanelMain.setLayout(new BorderLayout());
		jList.setBorder(null);
		jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jList.setFixedCellHeight(LIST_CELL_HEIGHT);
		jList.addKeyListener(keyListener);
		jList.addMouseListener(mouseListener);
		XFCheckListDialog_CheckBoxListRenderer renderer = new XFCheckListDialog_CheckBoxListRenderer();
		jList.setCellRenderer(renderer);
		jList.setBackground(jPanelMain.getBackground());
		jScrollPane.setBorder(BorderFactory.createEtchedBorder());
		jScrollPane.getViewport().add(jList, null);
		jPanelMain.add(jScrollPane, BorderLayout.CENTER);

		jPanelButtons.setBorder(BorderFactory.createEtchedBorder());
		jPanelButtons.setPreferredSize(new Dimension(350, 43));
		jButtonOK.setBounds(new Rectangle(103, 10, 73, 25));
		jButtonOK.setFont(new java.awt.Font("Dialog", 0, 12));
		jButtonOK.setText("OK");
		jButtonOK.addActionListener(new XFCheckListDialog_jButtonOK_actionAdapter(this));
		jButtonCancel.setBounds(new Rectangle(15, 10, 73, 25));
		jButtonCancel.setFont(new java.awt.Font("Dialog", 0, 12));
		jButtonCancel.setText(XFUtility.RESOURCE.getString("Cancel"));
		jButtonCancel.addActionListener(new XFCheckListDialog_jButtonCancel_actionAdapter(this));
		jPanelButtons.setLayout(null);
		jPanelButtons.add(jButtonOK);
		jPanelButtons.add(jButtonCancel);

		scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.getContentPane().add(jPanelButtons,  BorderLayout.SOUTH);
		this.getContentPane().add(jPanelMain,  BorderLayout.CENTER);
		jPanelButtons.getRootPane().setDefaultButton(jButtonOK);
    }

    public void clear() {
    	keyList.clear();
    	textList.clear();
    	initialSelectKeyList.clear();
    }

    public void addRow(String key, String text) {
		keyList.add(key.trim());
		textList.add(text.trim());
    }

    public int request(String title) {
    	reply = -1;
    	checkedKeyList = "";
		listModel.clear();
		for (int i = 0; i < keyList.size(); i++) {
				JCheckBox checkBox = new JCheckBox();
				checkBox.setText(textList.get(i));
				if (initialSelectKeyList.contains(keyList.get(i))) {
					checkBox.setSelected(true);
				}
				listModel.addElement(checkBox);
		}
		jList.setSelectedIndex(0);
		jList.requestFocus();

    	int width = 200;
    	int height = keyList.size() * LIST_CELL_HEIGHT + 70;
    	if (height > 290) {
    		height = 290;
    	}
		dlgSize = new Dimension(width, height + 30);
   		this.setLocation((scrSize.width - dlgSize.width) / 2, (scrSize.height - dlgSize.height) / 2);
		this.setPreferredSize(dlgSize);
    	this.setTitle(title);
		this.pack();
    	this.setVisible(true);

    	return reply;
    }

    protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			clear();
		}
	}

    class XFCheckListDialog_CheckBoxListRenderer extends JCheckBox implements ListCellRenderer{
    	private static final long serialVersionUID = 1L;
    	public XFCheckListDialog_CheckBoxListRenderer() {
    	}
    	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
    		JCheckBox checkBox = (JCheckBox)value;
    		setText(checkBox.getText());
			setFont(new java.awt.Font("Dialog", 0, 14));
    		setSelected(checkBox.isSelected());
    		setEnabled(checkBox.isEnabled());
    		if (isSelected) {
    			setBackground(jList.getSelectionBackground());
    			setForeground(jList.getSelectionForeground());
    		} else {
    			setBackground(jList.getBackground());
    			setForeground(jList.getForeground());
    		}
    		return this;
    	}
    }

    class XFCheckListDialog_keyAdapter extends java.awt.event.KeyAdapter {
    	public void keyPressed(KeyEvent e) {
    		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
    			int index = jList.getSelectedIndex();
    	    	JCheckBox checkBox = (JCheckBox)listModel.getElementAt(index);
        		if (checkBox.isSelected()){
        			checkBox.setSelected(false);
        		} else {
        			checkBox.setSelected(true);
        		}
    	    	jList.repaint();
    		}
    	}
    }

    class XFCheckListDialog_mouseAdapter extends java.awt.event.MouseAdapter {
    	public void mouseClicked(MouseEvent e) {
    		Point p = e.getPoint();
    		int index = jList.locationToIndex(p);
    		if (index > -1) {
    			JCheckBox checkBox = (JCheckBox)listModel.getElementAt(index);
    			if (checkBox.isSelected()){
    				checkBox.setSelected(false);
    			} else {
    				checkBox.setSelected(true);
    			}
    			jList.repaint();
    		}
    	}
    }
    
    public void jButtonOK_actionPerformed(ActionEvent e) {
    	reply = 0;
    	StringBuffer bf = new StringBuffer();
    	for (int i = 0; i < keyList.size(); i++) {
    		JCheckBox checkBox = (JCheckBox)listModel.getElementAt(i);
    		if (checkBox.isSelected()){
    			if (!bf.toString().equals("")) {
    				bf.append(";");
    			}
    			bf.append(keyList.get(i));
    		}
    	}
    	checkedKeyList = bf.toString();
		clear();
    	this.setVisible(false);
    }
    
    public void jButtonCancel_actionPerformed(ActionEvent e) {
    	this.setVisible(false);
    }

    public void setListSelected(String keyList) {
    	initialSelectKeyList.clear();
		StringTokenizer workTokenizer = new StringTokenizer(keyList, ";" );
		while (workTokenizer.hasMoreTokens()) {
			initialSelectKeyList.add(workTokenizer.nextToken());
		}
    }
    
    public String getListSelected() {
    	return checkedKeyList;
    }
}

class XFCheckListDialog_jButtonOK_actionAdapter implements java.awt.event.ActionListener {
	XFCheckListDialog adaptee;
	XFCheckListDialog_jButtonOK_actionAdapter(XFCheckListDialog adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jButtonOK_actionPerformed(e);
	}
}

class XFCheckListDialog_jButtonCancel_actionAdapter implements java.awt.event.ActionListener {
	XFCheckListDialog adaptee;
	XFCheckListDialog_jButtonCancel_actionAdapter(XFCheckListDialog adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jButtonCancel_actionPerformed(e);
	}
}
