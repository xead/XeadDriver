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
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.PlainDocument;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.ResourceBundle;

public class LoginDialog extends JDialog {
	/**
	 * Static constants
	 */
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	public static final String APPLICATION_NAME  = "XEAD Driver 1.0";
	public static final String FULL_VERSION  = "V1.R0.M5";
	public static final String FORMAT_VERSION  = "1.0";
	public static final String PRODUCT_NAME = "XEAD[zi:d] Driver";
	public static final String COPYRIGHT = "Copyright 2011 DBC,Ltd.";
	public static final String URL_DBC = "http://homepage2.nifty.com/dbc/";
	/**
	 * Private variants
	 */
	private JPanel jPanelMain = new JPanel();
	private JButton jButtonOK = new JButton();
	private JButton jButtonClose = new JButton();
	private JButton jButtonAbout = new JButton();
	private JLabel jLabelUserID = new JLabel();
	private JLabel jLabelPassword = new JLabel();
	private JTextField jTextFieldUserID = new JTextField();
	private JPasswordField jPasswordField = new JPasswordField();
	private Session session = null;
	private String userID, userName, userEmployeeNo, userMenus = "";
	private Connection connection = null;
	private Statement statement = null;
	private ResultSet result = null;
	private boolean validated = false;
	private About about;

	public LoginDialog(Session session, String loginUser, String loginPassword) {
		super(session, "", true);
		try {
			org.w3c.dom.Element fieldElement;
			int fieldSize;
			//
			this.session = session;
			this.connection = session.getConnection();
			this.setTitle(session.getSystemName() + " " + session.getVersion());
			jPanelMain.setBorder(BorderFactory.createEtchedBorder());
			jPanelMain.setPreferredSize(new Dimension(290, 130));
			jPanelMain.setLayout(null);
			//
			jLabelUserID.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelUserID.setBounds(new Rectangle(10, 17, 80, 25));
			jLabelUserID.setFont(new java.awt.Font("Dialog", 0, 14));
			jLabelUserID.setText(res.getString("UserID"));
			fieldElement = this.session.getFieldElement(session.getTableNameOfUser(), "IDUSER");
			fieldSize = Integer.parseInt(fieldElement.getAttribute("Size"));
			jTextFieldUserID.setFont(new java.awt.Font("Dialog", 0, 14));
			jTextFieldUserID.setBounds(new Rectangle(100, 17, fieldSize * 10, 25));
			jTextFieldUserID.setDocument(new LimitedDocument(fieldSize));
			jTextFieldUserID.setText(loginUser);
			//
			jLabelPassword.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelPassword.setBounds(new Rectangle(10, 52, 80, 25));
			jLabelPassword.setFont(new java.awt.Font("Dialog", 0, 14));
			jLabelPassword.setText(res.getString("Password"));
			jPasswordField.setFont(new java.awt.Font("Dialog", 0, 12));
			jPasswordField.setBounds(new Rectangle(100, 52, 140, 25));
			jPasswordField.setDocument(new LimitedDocument(10));
			jPasswordField.setText(loginPassword);
			//
			jButtonClose.setBounds(new Rectangle(10, 92, 80, 25));
			jButtonClose.setFont(new java.awt.Font("Dialog", 0, 12));
			jButtonClose.setText(res.getString("Close"));
			jButtonClose.addActionListener(new LoginDialog_jButtonClose_actionAdapter(this));
			jButtonOK.setBounds(new Rectangle(100, 92, 90, 25));
			jButtonOK.setFont(new java.awt.Font("Dialog", 0, 12));
			jButtonOK.setText(res.getString("LogIn"));
			jButtonOK.addActionListener(new LoginDialog_jButtonOK_actionAdapter(this));
			jButtonAbout.setBounds(new Rectangle(200, 92, 80, 25));
			jButtonAbout.setFont(new java.awt.Font("Dialog", 0, 12));
			jButtonAbout.setText("About");
			jButtonAbout.addActionListener(new LoginDialog_jButtonAbout_actionAdapter(this));
			//
			this.getContentPane().add(jPanelMain,  BorderLayout.CENTER);
			jPanelMain.add(jButtonClose, null);
			jPanelMain.add(jButtonOK, null);
			jPanelMain.add(jButtonAbout, null);
			jPanelMain.add(jLabelUserID, null);
			jPanelMain.add(jLabelPassword, null);
			jPanelMain.add(jTextFieldUserID, null);
			jPanelMain.add(jPasswordField, null);
			pack();
			//
			about = new About(this);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean userIsValidated() {
		jPanelMain.getRootPane().setDefaultButton(jButtonOK);
		Dimension dlgSize = this.getPreferredSize();
		Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((scrSize.width - dlgSize.width) / 2, (scrSize.height - dlgSize.height) / 2);
		this.pack();
		//
		String password = new String(jPasswordField.getPassword());
		if (jTextFieldUserID.getText().equals("") || password.equals("")) {
			super.setVisible(true);
		} else {
			jButtonOK_actionPerformed(null);
		}
		//
		return validated;
	}

	void jButtonOK_actionPerformed(ActionEvent e) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			if (checkUserAndPassword(jTextFieldUserID.getText(), new String(jPasswordField.getPassword()))) {
				this.setVisible(false);
			}
		} finally {
			jPasswordField.setText("");
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	void jButtonAbout_actionPerformed(ActionEvent e) {
		about.request();
	}

	boolean checkUserAndPassword(String userID, String password) {
		if (userID.equals("") || password.equals("")) {
			//
			JOptionPane.showMessageDialog(this, res.getString("LogInComment"));
			//
		} else {
			//
			try {
				//
				boolean isLoginPermitted = true;
				if (session.getSystemVariantString("LOGIN_PERMITTED").equals("F")) {
					isLoginPermitted = false;
				}
				//
				if (isLoginPermitted) {
					String passwordDigested = session.getDigestAdapter().digest(password);
					StringBuffer statementBuf = new StringBuffer();
					statementBuf.append("select * from ");
					statementBuf.append(session.getTableNameOfUser());
					statementBuf.append(" where IDUSER = '") ;
					statementBuf.append(userID) ;
					statementBuf.append("' and TXPASSWORD = '") ;
					statementBuf.append(passwordDigested);
					statementBuf.append("'") ;
					String sql = statementBuf.toString();
					//
					statement = connection.createStatement();
					result = statement.executeQuery(sql);
					if (result.next()) {
						Date resultDateFrom = null;
						Date resultDateThru = null;
						Date today = new Date();
						try {
							resultDateFrom = result.getDate("DTVALID");
							resultDateThru = result.getDate("DTEXPIRE");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						if (today.after(resultDateFrom)) {
							if (resultDateThru == null || today.before(resultDateThru)) {
								this.userID = jTextFieldUserID.getText();
								this.userName = result.getString("TXNAME").trim();
								this.userEmployeeNo = result.getString("NREMPLOYEE").trim();
								this.userMenus = result.getString("TXMENUS").trim();
								validated = true;
							} else {
								JOptionPane.showMessageDialog(this, res.getString("LogInError1"));
							}
						}
					} else {
						JOptionPane.showMessageDialog(this, res.getString("LogInError2"));
					}
					result.close();
					//
				} else {
					JOptionPane.showMessageDialog(this, res.getString("LogInError3"));
				}
				//
			} catch (SQLException e) {
				e.printStackTrace();
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
	LoginDialog adaptee;
	LoginDialog_jButtonOK_actionAdapter(LoginDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonOK_actionPerformed(e);
  }
}

class LoginDialog_jButtonClose_actionAdapter implements java.awt.event.ActionListener {
	LoginDialog adaptee;
	LoginDialog_jButtonClose_actionAdapter(LoginDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonClose_actionPerformed(e);
  }
}

class LoginDialog_jButtonAbout_actionAdapter implements java.awt.event.ActionListener {
	LoginDialog adaptee;
	LoginDialog_jButtonAbout_actionAdapter(LoginDialog adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonAbout_actionPerformed(e);
  }
}
