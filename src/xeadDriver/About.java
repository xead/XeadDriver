package xeadDriver;

/*
 * Copyright (c) 2011 WATANABE kozo <qyf05466@nifty.com>,
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

import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;

public class About extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JPanel panelMain = new JPanel();
	private JButton buttonOK = new JButton();
	private JLabel labelProduct = new JLabel();
	private JLabel labelVersion = new JLabel();
	private JLabel labelCopyright = new JLabel();
	private JLabel labelURL = new JLabel();
	private HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
	private Desktop desktop = Desktop.getDesktop();
	private LoginDialog loginDialog;

	public About(LoginDialog parent) {
		super(parent);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			loginDialog = parent;
			jbInit(parent);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit(LoginDialog parent) throws Exception  {
		labelProduct.setFont(new java.awt.Font("Serif", 1, 16));
		labelProduct.setHorizontalAlignment(SwingConstants.CENTER);
		labelProduct.setText(LoginDialog.PRODUCT_NAME);
		labelProduct.setBounds(new Rectangle(0, 11, 230, 18));
		labelVersion.setFont(new java.awt.Font("Dialog", 0, 12));
		labelVersion.setHorizontalAlignment(SwingConstants.CENTER);
		labelVersion.setText(LoginDialog.FULL_VERSION);
		labelVersion.setBounds(new Rectangle(0, 34, 230, 15));
		labelCopyright.setFont(new java.awt.Font("Dialog", 0, 12));
		labelCopyright.setHorizontalAlignment(SwingConstants.CENTER);
		labelCopyright.setText(LoginDialog.COPYRIGHT);
		labelCopyright.setBounds(new Rectangle(0, 55, 230, 15));
		labelURL.setBorder(null);
		labelURL.setFont(new java.awt.Font("Dialog", 0, 12));
		labelURL.setHorizontalAlignment(SwingConstants.CENTER);
		labelURL.setText("<html><u><font color='blue'>" + LoginDialog.URL_DBC);
		labelURL.setBounds(new Rectangle(0, 75, 230, 15));
		labelURL.addMouseListener(new About_labelURL_mouseAdapter(this));
		buttonOK.setFont(new java.awt.Font("Dialog", 0, 12));
		buttonOK.setText("OK");
		buttonOK.setBounds(new Rectangle(86, 105, 56, 22));
		buttonOK.addActionListener(this);
		//
		panelMain.setLayout(null);
		panelMain.add(labelProduct);
		panelMain.add(labelVersion);
		panelMain.add(labelCopyright);
		panelMain.add(labelURL);
		panelMain.add(buttonOK);
		//
		this.setResizable(false);
		this.setTitle("About XEAD Driver");
	 	this.setIconImage(Toolkit.getDefaultToolkit().createImage(xeadDriver.Session.class.getResource("title.png")));
		this.setPreferredSize(new Dimension(233, 167));
		this.getContentPane().add(panelMain, BorderLayout.CENTER);
	}

	public void request() {
		panelMain.getRootPane().setDefaultButton(buttonOK);
		Dimension dlgSize = this.getPreferredSize();
		Dimension frmSize = loginDialog.getSize();
		Point loc = loginDialog.getLocation();
		this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		this.pack();
		super.setVisible(true);
	}

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}

	void cancel() {
		dispose();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonOK) {
			cancel();
		}
	}

	void labelURL_mouseClicked(MouseEvent e) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			desktop.browse(new URI(LoginDialog.URL_DBC));
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "The Site is inaccessible.");
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	void labelURL_mouseEntered(MouseEvent e) {
		setCursor(htmlEditorKit.getLinkCursor());
	}

	void labelURL_mouseExited(MouseEvent e) {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
}

class About_labelURL_mouseAdapter extends java.awt.event.MouseAdapter {
	About adaptee;
	About_labelURL_mouseAdapter(About adaptee) {
		this.adaptee = adaptee;
	}
	public void mouseClicked(MouseEvent e) {
		adaptee.labelURL_mouseClicked(e);
	}
	public void mouseEntered(MouseEvent e) {
		adaptee.labelURL_mouseEntered(e);
	}
	public void mouseExited(MouseEvent e) {
		adaptee.labelURL_mouseExited(e);
	}
}
