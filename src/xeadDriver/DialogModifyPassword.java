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

import java.awt.*;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;
import java.awt.event.*;

public class DialogModifyPassword extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel jPanelMain = new JPanel();
	private JButton jButtonOK = new JButton();
	private JButton jButtonClose = new JButton();
	private JButton jButtonAbout = new JButton();
	private JLabel jLabelUserName = new JLabel();
	private JTextField jTextFieldUserName = new JTextField();
	private JLabel jLabelPasswordCurrent = new JLabel();
	private JPasswordField jPasswordCurrent = new JPasswordField();
	private JLabel jLabelPasswordNew = new JLabel();
	private JPasswordField jPasswordNew = new JPasswordField();
	private Session session = null;
	private String userID = "";
	private boolean modified = false;
	private Image imageTitle;
	private DialogAbout about;

	public DialogModifyPassword(Session session) {
		super(session, "", true);
		try {
			//
		 	imageTitle = Toolkit.getDefaultToolkit().createImage(xeadDriver.DialogModifyPassword.class.getResource("ikey.png"));
		 	this.setIconImage(imageTitle);
			this.session = session;
			this.setTitle(XFUtility.RESOURCE.getString("ModifyPassword"));
			jPanelMain.setBorder(BorderFactory.createEtchedBorder());
			jPanelMain.setPreferredSize(new Dimension(290, 163));
			jPanelMain.setLayout(null);
			//
			jLabelUserName.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelUserName.setBounds(new Rectangle(10, 17, 90, 25));
			jLabelUserName.setFont(new java.awt.Font("Dialog", 0, 12));
			jLabelUserName.setText(XFUtility.RESOURCE.getString("UserName"));
			jTextFieldUserName.setFont(new java.awt.Font("Dialog", 0, 12));
			jTextFieldUserName.setBounds(new Rectangle(105, 17, 160, 25));
			jTextFieldUserName.setEditable(false);
			//
			jLabelPasswordCurrent.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelPasswordCurrent.setBounds(new Rectangle(10, 50, 90, 25));
			jLabelPasswordCurrent.setFont(new java.awt.Font("Dialog", 0, 12));
			jLabelPasswordCurrent.setText(XFUtility.RESOURCE.getString("PasswordCurrent"));
			jPasswordCurrent.setFont(new java.awt.Font("Dialog", 0, 12));
			jPasswordCurrent.setBounds(new Rectangle(105, 50, 160, 25));
			jPasswordCurrent.setDocument(new LimitedDocument(10));
			//
			jLabelPasswordNew.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelPasswordNew.setBounds(new Rectangle(10, 83, 90, 25));
			jLabelPasswordNew.setFont(new java.awt.Font("Dialog", 0, 12));
			jLabelPasswordNew.setText(XFUtility.RESOURCE.getString("PasswordNew"));
			jPasswordNew.setFont(new java.awt.Font("Dialog", 0, 12));
			jPasswordNew.setBounds(new Rectangle(105, 83, 160, 25));
			jPasswordNew.setDocument(new LimitedDocument(10));
			//
			jButtonClose.setBounds(new Rectangle(10, 125, 80, 25));
			jButtonClose.setFont(new java.awt.Font("Dialog", 0, 12));
			jButtonClose.setText(XFUtility.RESOURCE.getString("Close"));
			jButtonClose.addActionListener(new ModifyPasswordDialog_jButtonClose_actionAdapter(this));
			jButtonOK.setBounds(new Rectangle(100, 125, 90, 25));
			jButtonOK.setFont(new java.awt.Font("Dialog", 0, 12));
			jButtonOK.setText(XFUtility.RESOURCE.getString("Modify"));
			jButtonOK.addActionListener(new ModifyPasswordDialog_jButtonOK_actionAdapter(this));
			jButtonAbout.setBounds(new Rectangle(200, 125, 80, 25));
			jButtonAbout.setFont(new java.awt.Font("Dialog", 0, 12));
			jButtonAbout.setText("About");
			jButtonAbout.addActionListener(new ModifyPasswordDialog_jButtonAbout_actionAdapter(this));
			//
			this.getContentPane().add(jPanelMain,  BorderLayout.CENTER);
			jPanelMain.add(jButtonClose, null);
			jPanelMain.add(jButtonOK, null);
			jPanelMain.add(jButtonAbout, null);
			jPanelMain.add(jLabelUserName, null);
			jPanelMain.add(jTextFieldUserName, null);
			jPanelMain.add(jLabelPasswordCurrent, null);
			jPanelMain.add(jPasswordCurrent, null);
			jPanelMain.add(jLabelPasswordNew, null);
			jPanelMain.add(jPasswordNew, null);
			pack();
			//
			about = new DialogAbout(this); 
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean passwordModified() {
		userID = session.getUserID();
		jTextFieldUserName.setText(session.getUserName());
		jPanelMain.getRootPane().setDefaultButton(jButtonOK);
		Dimension dlgSize = this.getPreferredSize();
        Rectangle screenRect = session.getMenuRectangle();
		this.setLocation(((screenRect.width - dlgSize.width) / 2) + screenRect.x, ((screenRect.height - dlgSize.height) / 2) + screenRect.y);
		this.pack();
		//
		jPasswordCurrent.setText("");
		jPasswordNew.setText("");
		jPasswordCurrent.requestFocus();
		super.setVisible(true);
		//
		return modified;
	}

	void jButtonOK_actionPerformed(ActionEvent e) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			if (passwordValidated()) {
				modified = true;
				this.setVisible(false);
			} else {
				jPasswordCurrent.setText("");
				jPasswordNew.setText("");
				jPasswordCurrent.requestFocus();
			}
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	boolean passwordValidated() {
		String passwordNew = "";
		String passwordCurrent = "";
		boolean validated = false;
		//
		passwordCurrent = new String(jPasswordCurrent.getPassword());
		String passwordCurrentDigested = session.getDigestAdapter().digest(passwordCurrent);
		//
		passwordNew = new String(jPasswordNew.getPassword());
		String passwordNewDigested = session.getDigestAdapter().digest(passwordNew);
		//
		if (passwordNew.length() < 5) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("ModifyPasswordError1"));
		} else {
			try {
				StringBuffer statementBuf = new StringBuffer();
				statementBuf.append("update ");
				statementBuf.append(session.getTableNameOfUser());
				statementBuf.append(" set TXPASSWORD = '") ;
				statementBuf.append(passwordNewDigested) ;
				statementBuf.append("' where IDUSER = '") ;
				statementBuf.append(userID) ;
				statementBuf.append("' and TXPASSWORD = '") ;
				statementBuf.append(passwordCurrentDigested);
				statementBuf.append("'") ;
				//
				XFTableOperator operator = new XFTableOperator(session, null, statementBuf.toString(), true);
				int count = operator.execute();
				if (count == 1) {
					validated = true;
				} else {
					JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("ModifyPasswordError2"));
				}
				//
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//
		return validated;
	}

	void jButtonClose_actionPerformed(ActionEvent e) {
		this.setVisible(false);
	}

	void jButtonAbout_actionPerformed(ActionEvent e) {
		about.request();
	}

	class LimitedDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;
		int limit;
		LimitedDocument(int limit) {
			this.limit = limit; 
		}
		public void insertString(int offset, String str, AttributeSet a) {
			if (offset >= limit ) {
				return;
			}
			try {
				super.insertString( offset, str, a );
			} catch(Exception e ) {
			}
		}
	}
}

class ModifyPasswordDialog_jButtonOK_actionAdapter implements java.awt.event.ActionListener {
	DialogModifyPassword adaptee;
	ModifyPasswordDialog_jButtonOK_actionAdapter(DialogModifyPassword adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonOK_actionPerformed(e);
  }
}

class ModifyPasswordDialog_jButtonClose_actionAdapter implements java.awt.event.ActionListener {
	DialogModifyPassword adaptee;
	ModifyPasswordDialog_jButtonClose_actionAdapter(DialogModifyPassword adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonClose_actionPerformed(e);
  }
}

class ModifyPasswordDialog_jButtonAbout_actionAdapter implements java.awt.event.ActionListener {
	DialogModifyPassword adaptee;
	ModifyPasswordDialog_jButtonAbout_actionAdapter(DialogModifyPassword adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonAbout_actionPerformed(e);
  }
}
