package xeadDriver;

/*
 * Copyright (c) 2016 WATANABE kozo <qyf05466@nifty.com>,
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

import org.apache.commons.lang.RandomStringUtils;

import java.awt.event.*;
import java.util.Date;

public class DialogLogin extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel jPanelMain = new JPanel();
	private JButton jButtonOK = new JButton();
	private JButton jButtonClose = new JButton();
	private JButton jButtonAbout = new JButton();
	private JLabel jLabelUserID = new JLabel();
	private int lengthOfUserID = 0;
	private JLabel jLabelPassword = new JLabel();
	private JTextField jTextFieldUserID = new JTextField();
	private JPasswordField jPasswordField = new JPasswordField();
	private JButton jButtonResetPassword = new JButton();
	private Session session = null;
	private String userID, userName, userEmployeeNo, userEmailAddress, userMenus = "";
	private boolean validated = false;
	private DialogAbout dialogAbout;

	public DialogLogin(Session session, String loginUser, String loginPassword) {
		super(session, "", true);
		try {
			org.w3c.dom.Element fieldElement;

			this.session = session;
			Image imageTitle = Toolkit.getDefaultToolkit().createImage(xeadDriver.Session.class.getResource("title.png"));
			this.setIconImage(imageTitle);
			this.setTitle(session.getSystemName());
			jPanelMain.setBorder(BorderFactory.createEtchedBorder());
			jPanelMain.setPreferredSize(new Dimension(420, 130));
			jPanelMain.setLayout(null);

			fieldElement = this.session.getFieldElement(session.getTableNameOfUser(), "IDUSER");
			lengthOfUserID = Integer.parseInt(fieldElement.getAttribute("Size"));
			jLabelUserID.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelUserID.setBounds(new Rectangle(5, 15, 130, 25));
			jLabelUserID.setFont(new java.awt.Font(session.systemFont, 0, 16));
			//jLabelUserID.setText(XFUtility.RESOURCE.getString("UserID"));
			jLabelUserID.setText(fieldElement.getAttribute("Name"));
			jTextFieldUserID.setFont(new java.awt.Font(session.systemFont, 0, 16));
			jTextFieldUserID.setBounds(new Rectangle(140, 15, lengthOfUserID * 14, 25));
			jTextFieldUserID.setDocument(new LimitedDocument(lengthOfUserID));
			jTextFieldUserID.setText(loginUser);

			fieldElement = this.session.getFieldElement(session.getTableNameOfUser(), "TXPASSWORD");
			jLabelPassword.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelPassword.setBounds(new Rectangle(5, 50, 130, 25));
			jLabelPassword.setFont(new java.awt.Font(session.systemFont, 0, 16));
			//jLabelPassword.setText(XFUtility.RESOURCE.getString("Password"));
			jLabelPassword.setText(fieldElement.getAttribute("Name"));
			jPasswordField.setFont(new java.awt.Font(session.systemFont, 0, 16));
			jPasswordField.setBounds(new Rectangle(140, 50, 180, 25));
			jPasswordField.setDocument(new LimitedDocument(10));
			jPasswordField.setText(loginPassword);
			jButtonResetPassword.setFont(new java.awt.Font(session.systemFont, 0, 16));
			jButtonResetPassword.setText("?");
			jButtonResetPassword.setBounds(new Rectangle(322, 49, 68, 27));
			jButtonResetPassword.addActionListener(new DialogLogin_jButtonResetPassword_actionAdapter(this));

			jButtonClose.setBounds(new Rectangle(30, 90, 100, 29));
			jButtonClose.setFont(new java.awt.Font(session.systemFont, 0, 16));
			jButtonClose.setText(XFUtility.RESOURCE.getString("Close"));
			jButtonClose.addActionListener(new DialogLogin_jButtonClose_actionAdapter(this));
			jButtonOK.setBounds(new Rectangle(150, 90, 120, 29));
			jButtonOK.setFont(new java.awt.Font(session.systemFont, 0, 16));
			jButtonOK.setText(XFUtility.RESOURCE.getString("LogIn"));
			jButtonOK.addActionListener(new DialogLogin_jButtonOK_actionAdapter(this));
			jButtonAbout.setBounds(new Rectangle(290, 90, 100, 29));
			jButtonAbout.setFont(new java.awt.Font(session.systemFont, 0, 16));
			jButtonAbout.setText("About");
			jButtonAbout.addActionListener(new DialogLogin_jButtonAbout_actionAdapter(this));

			this.getContentPane().add(jPanelMain,  BorderLayout.CENTER);
			jPanelMain.add(jButtonClose, null);
			jPanelMain.add(jButtonOK, null);
			jPanelMain.add(jButtonAbout, null);
			jPanelMain.add(jLabelUserID, null);
			jPanelMain.add(jLabelPassword, null);
			jPanelMain.add(jTextFieldUserID, null);
			jPanelMain.add(jPasswordField, null);
			jPanelMain.add(jButtonResetPassword, null);
			pack();

			dialogAbout = new DialogAbout(this, session.systemFont);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean userIsValidated(boolean isOnlineJob) throws Exception {
		String password = new String(jPasswordField.getPassword());
		if (jTextFieldUserID.getText().equals("") || password.equals("")) {
			if (isOnlineJob) {
				jPanelMain.getRootPane().setDefaultButton(jButtonOK);
				Dimension dlgSize = this.getPreferredSize();
				Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
				this.setLocation((scrSize.width - dlgSize.width) / 2, (scrSize.height - dlgSize.height) / 2);
				this.pack();
				super.setVisible(true);
			} else {
				validated = false;
			}
		} else {
			validated = isValidPassword(jTextFieldUserID.getText(), password);
		}
		return validated;
	}

	void jButtonResetPassword_actionPerformed(ActionEvent e) {
		Object[] bts = {XFUtility.RESOURCE.getString("Yes"), XFUtility.RESOURCE.getString("No")};
		int rtn = JOptionPane.showOptionDialog(this, XFUtility.RESOURCE.getString("ResetPasswordMessage1"),
				XFUtility.RESOURCE.getString("ResetPasswordTitle"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
		if (rtn == 0) {
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));

				StringBuffer statementBuf = new StringBuffer();
				statementBuf.append("select * from " + session.getTableNameOfUser());
				statementBuf.append(" where IDUSER = '" + jTextFieldUserID.getText() + "'");
				XFTableOperator operator = new XFTableOperator(session, null, statementBuf.toString(), true);
				if (operator.next()) {
					String emailAddress = operator.getValueOf("TXEMAIL").toString().trim();
					if (emailAddress.equals("")) {
						JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("ResetPasswordMessage6"));
					} else {
						String newPassword = RandomStringUtils.random(7,"abcdefghijkmnprstuvwxyz23456789");
						String digestedValue = session.getDigestedValueForUser(jTextFieldUserID.getText(), newPassword);
						int updCounter = Integer.parseInt(operator.getValueOf("UPDCOUNTER").toString());
						statementBuf = new StringBuffer();
						statementBuf.append("update " + session.getTableNameOfUser());
						statementBuf.append(" set TXPASSWORD = '" + digestedValue + "'");
						statementBuf.append(", UPDCOUNTER = " + (updCounter + 1));
						statementBuf.append(" where IDUSER = '" + jTextFieldUserID.getText() + "'");
						statementBuf.append(" and UPDCOUNTER = " + updCounter);
						operator = new XFTableOperator(session, null, statementBuf.toString(), true);
						int count = operator.execute();
						if (count == 1) {
							String emailMessage = XFUtility.RESOURCE.getString("ResetPasswordMessage4") + newPassword;
							session.sendMail(session.getAdminEmail(), emailAddress, "",
									session.getSystemName() + XFUtility.RESOURCE.getString("ResetPasswordMessage5"), emailMessage);
							JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("ResetPasswordMessage7"));
						} else {
							JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("ResetPasswordMessage3"));
						}
					}
				} else {
					JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("ResetPasswordMessage2"));
				}
			} catch(Exception ex) {
				JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("ResetPasswordMessage3"));
			} finally {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
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
		dialogAbout.request();
	}

	boolean isValidPassword(String originalUserID, String password) throws Exception {
		StringBuffer statementBuf;
		XFTableOperator operator;

		String userID = originalUserID.substring(0, lengthOfUserID);
		if (userID.equals("") || !userID.matches("[0-9a-zA-Z]+") || password.equals("")) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("LogInComment"));
		} else {
			statementBuf = new StringBuffer();
			statementBuf.append("select * from ");
			statementBuf.append(session.getTableNameOfUserVariants());
			statementBuf.append(" where IDUSERKUBUN = 'KBCALENDAR'");
			operator = new XFTableOperator(session, null, statementBuf.toString(), true);
			if (!operator.next()) {
				JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("LogInError5"));
				validated = false;
			} else {
				if (session.getSystemVariantString("LOGIN_PERMITTED").equals("")) {
					JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("LogInError4"));
					validated = false;
				} else {
					if (session.getSystemVariantString("LOGIN_PERMITTED").equals("F")) {
						JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("LogInError3"));
					} else {

						/////////////////////////////////////////////////////
						// Setup select-statement to check login authority //
						/////////////////////////////////////////////////////
						//String passwordDigested = session.getDigestAdapter().digest(password);
						String passwordDigested = session.getDigestedValueForUser(userID, password);
						statementBuf = new StringBuffer();
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
								operator = new XFTableOperator(session, null, statementBuf.toString(), true);
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

class DialogLogin_jButtonResetPassword_actionAdapter implements java.awt.event.ActionListener {
	DialogLogin adaptee;
	DialogLogin_jButtonResetPassword_actionAdapter(DialogLogin adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jButtonResetPassword_actionPerformed(e);
	}
}

class DialogLogin_jButtonOK_actionAdapter implements java.awt.event.ActionListener {
	DialogLogin adaptee;
	DialogLogin_jButtonOK_actionAdapter(DialogLogin adaptee) {
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

class DialogLogin_jButtonClose_actionAdapter implements java.awt.event.ActionListener {
	DialogLogin adaptee;
	DialogLogin_jButtonClose_actionAdapter(DialogLogin adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jButtonClose_actionPerformed(e);
	}
}

class DialogLogin_jButtonAbout_actionAdapter implements java.awt.event.ActionListener {
	DialogLogin adaptee;
	DialogLogin_jButtonAbout_actionAdapter(DialogLogin adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jButtonAbout_actionPerformed(e);
	}
}
