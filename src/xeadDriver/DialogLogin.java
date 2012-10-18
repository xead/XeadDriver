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
import java.util.Date;

public class DialogLogin extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel jPanelMain = new JPanel();
	private JButton jButtonOK = new JButton();
	private JButton jButtonClose = new JButton();
	private JButton jButtonAbout = new JButton();
	private JLabel jLabelUserID = new JLabel();
	private JLabel jLabelPassword = new JLabel();
	private JTextField jTextFieldUserID = new JTextField();
	private JPasswordField jPasswordField = new JPasswordField();
	private Session session = null;
	private String userID, userName, userEmployeeNo, userEmailAddress, userMenus = "";
	private boolean validated = false;
	private DialogAbout aboutDialog;

	public DialogLogin(Session session, String loginUser, String loginPassword) {
		super(session, "", true);
		try {
			org.w3c.dom.Element fieldElement;
			int fieldSize;

			this.session = session;
			this.setTitle(session.getSystemName() + " " + session.getVersion());
			jPanelMain.setBorder(BorderFactory.createEtchedBorder());
			jPanelMain.setPreferredSize(new Dimension(290, 130));
			jPanelMain.setLayout(null);

			jLabelUserID.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelUserID.setBounds(new Rectangle(10, 17, 80, 25));
			jLabelUserID.setFont(new java.awt.Font("Dialog", 0, 14));
			jLabelUserID.setText(XFUtility.RESOURCE.getString("UserID"));
			fieldElement = this.session.getFieldElement(session.getTableNameOfUser(), "IDUSER");
			fieldSize = Integer.parseInt(fieldElement.getAttribute("Size"));
			jTextFieldUserID.setFont(new java.awt.Font("Dialog", 0, 14));
			jTextFieldUserID.setBounds(new Rectangle(100, 17, fieldSize * 10, 25));
			jTextFieldUserID.setDocument(new LimitedDocument(fieldSize));
			jTextFieldUserID.setText(loginUser);

			jLabelPassword.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelPassword.setBounds(new Rectangle(10, 52, 80, 25));
			jLabelPassword.setFont(new java.awt.Font("Dialog", 0, 14));
			jLabelPassword.setText(XFUtility.RESOURCE.getString("Password"));
			jPasswordField.setFont(new java.awt.Font("Dialog", 0, 12));
			jPasswordField.setBounds(new Rectangle(100, 52, 140, 25));
			jPasswordField.setDocument(new LimitedDocument(10));
			jPasswordField.setText(loginPassword);

			jButtonClose.setBounds(new Rectangle(10, 92, 80, 25));
			jButtonClose.setFont(new java.awt.Font("Dialog", 0, 12));
			jButtonClose.setText(XFUtility.RESOURCE.getString("Close"));
			jButtonClose.addActionListener(new LoginDialog_jButtonClose_actionAdapter(this));
			jButtonOK.setBounds(new Rectangle(100, 92, 90, 25));
			jButtonOK.setFont(new java.awt.Font("Dialog", 0, 12));
			jButtonOK.setText(XFUtility.RESOURCE.getString("LogIn"));
			jButtonOK.addActionListener(new LoginDialog_jButtonOK_actionAdapter(this));
			jButtonAbout.setBounds(new Rectangle(200, 92, 80, 25));
			jButtonAbout.setFont(new java.awt.Font("Dialog", 0, 12));
			jButtonAbout.setText("About");
			jButtonAbout.addActionListener(new LoginDialog_jButtonAbout_actionAdapter(this));

			this.getContentPane().add(jPanelMain,  BorderLayout.CENTER);
			jPanelMain.add(jButtonClose, null);
			jPanelMain.add(jButtonOK, null);
			jPanelMain.add(jButtonAbout, null);
			jPanelMain.add(jLabelUserID, null);
			jPanelMain.add(jLabelPassword, null);
			jPanelMain.add(jTextFieldUserID, null);
			jPanelMain.add(jPasswordField, null);
			pack();

			aboutDialog = new DialogAbout(this);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean userIsValidated() throws Exception {
		jPanelMain.getRootPane().setDefaultButton(jButtonOK);
		Dimension dlgSize = this.getPreferredSize();
		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((scrSize.width - dlgSize.width) / 2, (scrSize.height - dlgSize.height) / 2);
		this.pack();

		String password = new String(jPasswordField.getPassword());
		if (jTextFieldUserID.getText().equals("") || password.equals("")) {
			EventQueue.invokeLater(new Runnable() {
				@Override public void run() {
					session.getApplication().hideSplash();
				}
			});
			super.setVisible(true);
		} else {
			jButtonOK_actionPerformed(null);
		}

		return validated;
	}

	void jButtonOK_actionPerformed(ActionEvent e) throws Exception {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			if (isValidPassword(jTextFieldUserID.getText(), new String(jPasswordField.getPassword()))) {
				this.setVisible(false);
			}
		} finally {
			jPasswordField.setText("");
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			super.processWindowEvent(e);
			jButtonClose_actionPerformed(null);
		} else {
			super.processWindowEvent(e);
		}
	}

	void jButtonAbout_actionPerformed(ActionEvent e) {
		aboutDialog.request();
	}

	boolean isValidPassword(String userID, String password) throws Exception {
		if (userID.equals("") || password.equals("")) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("LogInComment"));
		} else {
			if (session.getSystemVariantString("LOGIN_PERMITTED").equals("F")) {
				JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("LogInError3"));
			} else {

				/////////////////////////////////////////////////////
				// Setup select-statement to check login authority //
				/////////////////////////////////////////////////////
				String passwordDigested = session.getDigestAdapter().digest(password);
				StringBuffer statementBuf = new StringBuffer();
				statementBuf.append("select * from ");
				statementBuf.append(session.getTableNameOfUser());
				statementBuf.append(" where IDUSER = '") ;
				statementBuf.append(userID) ;
				statementBuf.append("' and TXPASSWORD = '") ;
				statementBuf.append(passwordDigested);
				statementBuf.append("'") ;

				///////////////////////////////////////////////
				// Execute select-statement retrying 3 times //
				///////////////////////////////////////////////
				int retryCount = 0;
				while (retryCount < 3) {
					try {
						retryCount++;
						XFTableOperator operator = new XFTableOperator(session, null, statementBuf.toString(), true);
						if (operator.next()) {
							Date resultDateFrom = null;
							Date resultDateThru = null;
							Date today = new Date();
							resultDateFrom = (java.util.Date)operator.getValueOf("DTVALID");
							resultDateThru = (java.util.Date)operator.getValueOf("DTEXPIRE");
							if (today.after(resultDateFrom)) {
								if (resultDateThru == null || today.before(resultDateThru)) {
									this.userID = jTextFieldUserID.getText();
									this.userName = operator.getValueOf("TXNAME").toString().trim();
									this.userEmployeeNo = operator.getValueOf("NREMPLOYEE").toString().trim();
									this.userEmailAddress = operator.getValueOf("TXEMAIL").toString().trim();
									this.userMenus = operator.getValueOf("TXMENUS").toString().trim();
									validated = true;
								} else {
									JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("LogInError1"));
								}
							} else {
								JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("LogInError1"));
							}
						} else {
							JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("LogInError2"));
						}
						retryCount = 3;
					} catch(Exception e) {
						if (retryCount < 3) {
							Thread.sleep(1000);
						} else {
							throw e;
						}
					}
				}
			}
		}
		return validated;
	}

	void jButtonClose_actionPerformed(ActionEvent e) {
		this.setVisible(false);
	}

	String getUserID() {
		return userID;
	}

	String getUserName() {
		return userName;
	}

	String getUserEmployeeNo() {
		return userEmployeeNo;
	}

	String getUserEmailAddress() {
		return userEmailAddress;
	}

	String getUserMenus() {
		return userMenus;
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

class LoginDialog_jButtonOK_actionAdapter implements java.awt.event.ActionListener {
	DialogLogin adaptee;
	LoginDialog_jButtonOK_actionAdapter(DialogLogin adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		try {
			adaptee.jButtonOK_actionPerformed(e);
		} catch(Exception ex) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("LogInError3") + "\n" + ex.getMessage());
		}
	}
}

class LoginDialog_jButtonClose_actionAdapter implements java.awt.event.ActionListener {
	DialogLogin adaptee;
	LoginDialog_jButtonClose_actionAdapter(DialogLogin adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonClose_actionPerformed(e);
  }
}

class LoginDialog_jButtonAbout_actionAdapter implements java.awt.event.ActionListener {
	DialogLogin adaptee;
	LoginDialog_jButtonAbout_actionAdapter(DialogLogin adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonAbout_actionPerformed(e);
  }
}
