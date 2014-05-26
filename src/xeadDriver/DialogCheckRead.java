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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DialogCheckRead extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel jPanelMain = new JPanel();
	private JLabel jLabelNextRow = new JLabel();
	private JTextField jTextFieldNextRow = new JTextField();
	private JLabel jLabelCountUnit = new JLabel();
	private JTextField jTextFieldCountUnit = new JTextField();
	private JRadioButton jRadioButtonContinueScan = new JRadioButton();
	private JRadioButton jRadioButtonRestartScan = new JRadioButton();
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JTextArea jTextAreaMessage = new JTextArea();
	private JScrollPane jScrollPaneMessage = new JScrollPane();
	private JButton jButtonOK = new JButton();
	private JButton jButtonCancel = new JButton();
	private Session session_;
	private boolean repliedOK = false;

	public DialogCheckRead(Session session) {
		super(session, "", true);
		try {
			
			session_ = session;

			this.setTitle(XFUtility.RESOURCE.getString("DialogCheckReadTitle"));
			jPanelMain.setBorder(BorderFactory.createEtchedBorder());
			jPanelMain.setPreferredSize(new Dimension(550, 190));
			jPanelMain.setLayout(null);

			jRadioButtonContinueScan.setBounds(new Rectangle(5, 9, 270, 25));
			jRadioButtonContinueScan.setFont(new java.awt.Font(session.systemFont, 0, XFUtility.FONT_SIZE));
			jRadioButtonContinueScan.setText(XFUtility.RESOURCE.getString("DialogCheckReadContinue"));
			jRadioButtonContinueScan.addChangeListener(new DialogCheckRead_jRadioButton_changeAdapter(this));
			buttonGroup.add(jRadioButtonContinueScan);

			jRadioButtonRestartScan.setBounds(new Rectangle(275, 9, 270, 25));
			jRadioButtonRestartScan.setFont(new java.awt.Font(session.systemFont, 0, XFUtility.FONT_SIZE));
			jRadioButtonRestartScan.setText(XFUtility.RESOURCE.getString("DialogCheckReadRestart"));
			buttonGroup.add(jRadioButtonRestartScan);

			jLabelNextRow.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelNextRow.setBounds(new Rectangle(5, 40, 130, 25));
			jLabelNextRow.setFont(new java.awt.Font(session.systemFont, 0, XFUtility.FONT_SIZE));
			jLabelNextRow.setText(XFUtility.RESOURCE.getString("DialogCheckReadNextRow"));
			jTextFieldNextRow.setFont(new java.awt.Font(session.systemFont, 0, XFUtility.FONT_SIZE));
			jTextFieldNextRow.setBounds(new Rectangle(140, 37, 120, 27));

			jLabelCountUnit.setHorizontalAlignment(SwingConstants.RIGHT);
			jLabelCountUnit.setBounds(new Rectangle(280, 40, 120, 25));
			jLabelCountUnit.setFont(new java.awt.Font(session.systemFont, 0, XFUtility.FONT_SIZE));
			jLabelCountUnit.setText(XFUtility.RESOURCE.getString("DialogCheckReadCountUnit"));
			jTextFieldCountUnit.setFont(new java.awt.Font(session.systemFont, 0, XFUtility.FONT_SIZE));
			jTextFieldCountUnit.setBounds(new Rectangle(405, 37, 100, 27));

			jTextAreaMessage.setFont(new java.awt.Font(session.systemFont, 0, XFUtility.FONT_SIZE));
			jTextAreaMessage.setBackground(SystemColor.control);
			jTextAreaMessage.setEditable(false);
			jTextAreaMessage.setLineWrap(true);
			jTextAreaMessage.setWrapStyleWord(true);
			jScrollPaneMessage.setBounds(new Rectangle(5, 71, 540, 75));
			jScrollPaneMessage.getViewport().add(jTextAreaMessage);

			jButtonCancel.setBounds(new Rectangle(50, 152, 150, 30));
			jButtonCancel.setFont(new java.awt.Font(session.systemFont, 0, XFUtility.FONT_SIZE));
			jButtonCancel.setText(XFUtility.RESOURCE.getString("Cancel"));
			jButtonCancel.addActionListener(new DialogCheckRead_jButtonClose_actionAdapter(this));
			jButtonOK.setBounds(new Rectangle(350, 152, 150, 30));
			jButtonOK.setFont(new java.awt.Font(session.systemFont, 0, XFUtility.FONT_SIZE));
			jButtonOK.setText("OK");
			jButtonOK.addActionListener(new DialogCheckRead_jButtonOK_actionAdapter(this));

			this.setResizable(false);
			this.getContentPane().add(jPanelMain,  BorderLayout.CENTER);
			jPanelMain.add(jLabelNextRow, null);
			jPanelMain.add(jTextFieldNextRow, null);
			jPanelMain.add(jLabelCountUnit, null);
			jPanelMain.add(jTextFieldCountUnit, null);
			jPanelMain.add(jRadioButtonContinueScan, null);
			jPanelMain.add(jRadioButtonRestartScan, null);
			jPanelMain.add(jScrollPaneMessage, null);
			jPanelMain.add(jButtonCancel, null);
			jPanelMain.add(jButtonOK, null);
			jPanelMain.getRootPane().setDefaultButton(jButtonOK);
			this.pack();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean request(int startRow, int selectRow, int nextRow, int countUnit) {
		repliedOK = false;
		Dimension dlgSize = this.getPreferredSize();
        Rectangle screenRect = session_.getMenuRectangle();
		this.setLocation(((screenRect.width - dlgSize.width) / 2) + screenRect.x, ((screenRect.height - dlgSize.height) / 2) + screenRect.y);

		jTextFieldNextRow.setText(Integer.toString(nextRow+1));
		jTextFieldCountUnit.setText(Integer.toString(countUnit));
		jTextAreaMessage.setText(XFUtility.RESOURCE.getString("DialogCheckReadMessage1")
				+ (startRow+1) + XFUtility.RESOURCE.getString("DialogCheckReadMessage2")
				+ nextRow + XFUtility.RESOURCE.getString("DialogCheckReadMessage3")
				+ selectRow + XFUtility.RESOURCE.getString("DialogCheckReadMessage4")
				+ XFUtility.RESOURCE.getString("DialogCheckReadMessage5"));
		jRadioButtonContinueScan.setSelected(true);
		super.setVisible(true);
		return repliedOK;
	}
	
	public boolean isRestarting() {
		return jRadioButtonRestartScan.isSelected();
	}
	
	public int getNextRow() {
		return Integer.parseInt(jTextFieldNextRow.getText()) - 1;
	}
	
	public int getCountUnit() {
		return Integer.parseInt(jTextFieldCountUnit.getText());
	}

	void jRadioButton_stateChanged(ChangeEvent e) {
		if (jRadioButtonContinueScan.isSelected()) {
			jTextFieldNextRow.setEditable(false);
		} else {
			jTextFieldNextRow.setEditable(true);
		}
	}

	void jButtonOK_actionPerformed(ActionEvent e) {
		repliedOK = true;
		this.setVisible(false);
	}

	void jButtonClose_actionPerformed(ActionEvent e) {
		this.setVisible(false);
	}
}

class DialogCheckRead_jRadioButton_changeAdapter implements ChangeListener {
	DialogCheckRead adaptee;
	DialogCheckRead_jRadioButton_changeAdapter(DialogCheckRead adaptee) {
		this.adaptee = adaptee;
	}
	public void stateChanged(ChangeEvent e) {
		adaptee.jRadioButton_stateChanged(e);
	}
}

class DialogCheckRead_jButtonOK_actionAdapter implements java.awt.event.ActionListener {
	DialogCheckRead adaptee;
	DialogCheckRead_jButtonOK_actionAdapter(DialogCheckRead adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonOK_actionPerformed(e);
  }
}

class DialogCheckRead_jButtonClose_actionAdapter implements java.awt.event.ActionListener {
	DialogCheckRead adaptee;
	DialogCheckRead_jButtonClose_actionAdapter(DialogCheckRead adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jButtonClose_actionPerformed(e);
  }
}