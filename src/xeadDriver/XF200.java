package xeadDriver;

/*
 * Copyright (c) 2018 WATANABE kozo <qyf05466@nifty.com>,
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

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.w3c.dom.*;

public class XF200 extends JDialog implements XFExecutable, XFScriptable {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element functionElement_ = null;
	private HashMap<String, Object> parmMap_ = null;
	private HashMap<String, Object> returnMap_ = new HashMap<String, Object>();
	private XF200_PrimaryTable primaryTable_ = null;
	private Session session_ = null;
	private boolean instanceIsAvailable_ = true;
	private boolean hasAutoNumberField = false;
	private boolean isClosing = false;
	private boolean isCheckOnly = true;
	private int instanceArrayIndex_ = -1;
	private int programSequence;
	private StringBuffer processLog = new StringBuffer();
	private String panelMode_ = "";
	private String initialMsg = "";
	private String dataName = "";
	private JPanel jPanelMain = new JPanel();
	private JSplitPane jSplitPaneMain = new JSplitPane();
	private JSplitPane jSplitPaneWithTabbedFields = null;
	private JPanel jPanelFields = new JPanel();
	private JScrollPane jScrollPaneFields = new JScrollPane();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private ArrayList<XFTableOperator> operatorList = new ArrayList<XFTableOperator>();
	private ArrayList<XF200_ReferTable> referTableList = new ArrayList<XF200_ReferTable>();
	private ArrayList<XF200_Field> fieldList = new ArrayList<XF200_Field>();
	private JTabbedPane jTabbedPaneFields = new JTabbedPane();
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private JPanel[] jPanelButtonArray = new JPanel[7];
	private JPanel jPanelInfo = new JPanel();
	private GridLayout gridLayoutButtons = new GridLayout();
	private GridLayout gridLayoutInfo = new GridLayout();
	private ArrayList<String> messageList = new ArrayList<String>();
	private JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JProgressBar jProgressBar = new JProgressBar();
	private JButton[] jButtonArray = new JButton[7];
	private String[] jButtonNumberArray = new String[7];
	private Action checkAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				if (!panelMode_.equals("DISPLAY")) {
					messageList.clear();
					if (panelMode_.equals("ADD") || panelMode_.equals("COPY")) {
						isCheckOnly = true;
						doButtonActionInsert();
					}
					if (panelMode_.equals("EDIT")) {
						isCheckOnly = true;
						doButtonActionUpdate();
					}
					setMessagesOnPanel();
				}
			} finally {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	};
	private Action helpAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			session_.browseHelp();
		}
	};
	private Action focusTabAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			if (jTabbedPaneFields.getTabCount() > 0) {
				jTabbedPaneFields.requestFocus();
			}
		}
	};
	private Action escapeAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			returnTo("MENU");
		}
	};
	private Action[] actionButtonArray = new Action[7];
	private int buttonIndexForF6, buttonIndexForF8;
	private String functionKeyToEdit = "";
	private String actionTextToDelete = "";
	private JButton buttonToEdit = null;
	private JButton buttonToCopy = null;
	private JButton buttonToDelete = null;
	private String[] actionDefinitionArray = new String[7];
	private ScriptEngine scriptEngine;
	private Bindings scriptBindings;
	private String scriptNameRunning = "";
	private ByteArrayOutputStream exceptionLog;
	private PrintStream exceptionStream;
	private String exceptionHeader = "";
	private String functionAfterInsert = "";
	private int firstErrorTabIndex = -1;
	private int biggestWidth = 0;
	private int biggestHeight = 0;
	private int biggestHeightOfTabbedPane = 0;
	private HashMap<String, Object> columnValueMap = new HashMap<String, Object>();
	private HashMap<String, Object> columnOldValueMap = new HashMap<String, Object>();
	private HashMap<String, Object> variantMap = new HashMap<String, Object>();
	
	public XF200(Session session, int instanceArrayIndex) {
		super(session, "", true);
		session_ = session;
		instanceArrayIndex_ = instanceArrayIndex;
		initComponentsAndVariants();
	}

	void initComponentsAndVariants() {
		jPanelMain.setLayout(new BorderLayout());
		jPanelFields.setLayout(null);
		jScrollPaneFields.getViewport().add(jPanelFields, null);

		jTabbedPaneFields.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTabbedPaneFields.setBorder(null);
		jPanelBottom.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setLayout(new BorderLayout());
		jPanelBottom.setBorder(null);
		jLabelFunctionID.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE-2));
		jLabelFunctionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelFunctionID.setForeground(Color.gray);
		jLabelSessionID.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE-2));
		jLabelSessionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelSessionID.setForeground(Color.gray);
		jPanelButtons.setPreferredSize(new Dimension(10, 35));
		jPanelButtons.setBorder(null);
		jPanelButtons.setLayout(gridLayoutButtons);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
		jPanelInfo.setLayout(gridLayoutInfo);
		jPanelInfo.add(jLabelSessionID);
		jPanelInfo.add(jLabelFunctionID);
		jProgressBar.setStringPainted(true);
		jProgressBar.setString(XFUtility.RESOURCE.getString("ChrossCheck"));
		gridLayoutButtons.setColumns(7);
		gridLayoutButtons.setRows(1);

		for (int i = 0; i < 7; i++) {
			jButtonArray[i] = new JButton();
			jButtonArray[i].setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
			jButtonArray[i].setFocusable(false);
			jButtonArray[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Component com = (Component)e.getSource();
					for (int i = 0; i < 7; i++) {
						if (com.equals(jButtonArray[i])) {
							doButtonAction(actionDefinitionArray[i]);
							break;
						}
					}
				}
			});
			jButtonNumberArray[i] = "";
			jPanelButtonArray[i] = new JPanel();
			jPanelButtonArray[i].setLayout(new BorderLayout());
			jPanelButtonArray[i].add(jButtonArray[i], BorderLayout.CENTER);
			actionButtonArray[i] = new ButtonAction(jButtonArray[i]);
			jPanelButtons.add(jPanelButtonArray[i]);
		}

		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jTextAreaMessages.setWrapStyleWord(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);

		jSplitPaneMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPaneMain.addKeyListener(new XF200_Component_keyAdapter(this));
		jSplitPaneMain.add(jScrollPaneMessages, JSplitPane.BOTTOM);

		jPanelMain.add(jSplitPaneMain, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
	}

	public boolean isAvailable() {
		return instanceIsAvailable_;
	}

	public HashMap<String, Object> execute(HashMap<String, Object> parmMap) {
		if (functionElement_ == null) {
			JOptionPane.showMessageDialog(null, "Calling function without specifications.");
			return parmMap;
		} else {
			return this.execute(null, parmMap);
		}
	}

	public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parmMap) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			////////////////////////
			// Process parameters //
			////////////////////////
			parmMap_ = parmMap;
			if (parmMap_ == null) {
				parmMap_ = new HashMap<String, Object>();
			}
			returnMap_.clear();
			returnMap_.putAll(parmMap_);

			///////////////////////////
			// Initializing variants //
			///////////////////////////
			isClosing = false;
			instanceIsAvailable_ = false;
			isCheckOnly = true;
			exceptionLog = new ByteArrayOutputStream();
			exceptionStream = new PrintStream(exceptionLog);
			exceptionHeader = "";
			processLog.delete(0, processLog.length());
			variantMap.clear();
			messageList.clear();
			operatorList.clear();

			///////////////////////////////////////////
			// Setup specifications for the function //
			///////////////////////////////////////////
			if (functionElement != null
					&& (functionElement_ == null || !functionElement_.getAttribute("ID").equals(functionElement.getAttribute("ID")))) {
				setFunctionSpecifications(functionElement);
			}

//			/////////////////////////////////
//			// Write log to start function //
//			/////////////////////////////////
//			programSequence = session_.writeLogOfFunctionStarted(functionElement_.getAttribute("ID"), functionElement_.getAttribute("Name"));
			
			//////////////////////////////
			// Set panel configurations //
			//////////////////////////////
	        Rectangle screenRect = session_.getMenuRectangle();
			if (functionElement_.getAttribute("Size").equals("")) {
				this.setPreferredSize(new Dimension(screenRect.width, screenRect.height));
				this.setLocation(screenRect.x, screenRect.y);
				this.pack();
			} else {
				if (functionElement_.getAttribute("Size").equals("AUTO")) {
					int posX = 0;
					int posY = 0;

					int workWidth = biggestWidth + 60;
					if (workWidth < 1000) {
						workWidth = 1000;
					}
					if (workWidth > screenRect.width) {
						workWidth = screenRect.width;
						posX = screenRect.x;
					} else {
						posX = ((screenRect.width - workWidth) / 2) + screenRect.x;
						if ((posX + workWidth + 10) < screenRect.width) {
							posX = posX + 10;
						}
					}

					int workHeight = biggestHeight + 170;
					if (workHeight > screenRect.height) {
						workHeight = screenRect.height;
						posY = screenRect.y;
					} else {
						posY = ((screenRect.height - workHeight) / 2) + screenRect.y;
					}

					this.setPreferredSize(new Dimension(workWidth, workHeight));
					this.setLocation(posX, posY);
					this.pack();
				} else {
					StringTokenizer workTokenizer = new StringTokenizer(functionElement_.getAttribute("Size"), ";" );
					int width = Integer.parseInt(workTokenizer.nextToken());
					int height = Integer.parseInt(workTokenizer.nextToken());
					this.setPreferredSize(new Dimension(width, height));
					int posX = ((screenRect.width - width) / 2) + screenRect.x;
					int posY = ((screenRect.height - height) / 2) + screenRect.y;
					this.setLocation(posX, posY);
					this.pack();
				}
			}
			jPanelBottom.remove(jProgressBar);
			jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
	        
			/////////////////////////////
			// Initializing panel mode //
			/////////////////////////////
			if (hasParmMapWithCompleteKeySet()) {
				if (parmMap_.containsKey("INSTANCE_MODE") && parmMap_.get("INSTANCE_MODE").toString().equals("EDIT")) {
					panelMode_ = "EDIT";
					returnMap_.put("RETURN_CODE", "21");
					this.setTitle(functionElement_.getAttribute("Name"));
					if (initialMsg.equals("")) {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage24"));
					} else {
						messageList.add(initialMsg);
					}
				} else {
					if (parmMap_.containsKey("INSTANCE_MODE") && parmMap_.get("INSTANCE_MODE").toString().equals("ADD")) {
						panelMode_ = "ADD";
						returnMap_.put("RETURN_CODE", "11");
						this.setTitle(XFUtility.RESOURCE.getString("FunctionMessage25") + dataName + XFUtility.RESOURCE.getString("FunctionMessage26"));
						if (initialMsg.equals("")) {
							messageList.add(XFUtility.RESOURCE.getString("FunctionMessage15"));
						} else {
							messageList.add(initialMsg);
						}
					} else {
						panelMode_ = "DISPLAY";
						returnMap_.put("RETURN_CODE", "00");
						this.setTitle(XFUtility.RESOURCE.getString("FunctionMessage10") + dataName + XFUtility.RESOURCE.getString("FunctionMessage11"));
						if (initialMsg.equals("")) {
							messageList.add(XFUtility.RESOURCE.getString("FunctionMessage12"));
						} else {
							messageList.add(initialMsg);
						}
					}
				}
			} else {
//				if (functionElement_.getAttribute("UpdateOnly").equals("T")
//						|| ((parmMap_.containsKey("INSTANCE_MODE") && !parmMap_.get("INSTANCE_MODE").toString().equals("ADD")))) {
				if ((parmMap_.containsKey("INSTANCE_MODE") && !parmMap_.get("INSTANCE_MODE").toString().equals("ADD"))) {
					panelMode_ = "EDIT";
					returnMap_.put("RETURN_CODE", "21");
					closeFunction();
				} else {
					panelMode_ = "ADD";
					returnMap_.put("RETURN_CODE", "11");
					this.setTitle(XFUtility.RESOURCE.getString("FunctionMessage13") + dataName + XFUtility.RESOURCE.getString("FunctionMessage14"));
					if (initialMsg.equals("")) {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage15"));
					} else {
						messageList.add(initialMsg);
					}
				}
			}

			//////////////////////////////////////////////////
			// Fetch the record and set values on the panel //
			//////////////////////////////////////////////////
			Object value;
			for (int i = 0; i < fieldList.size(); i++) {
				fieldList.get(i).initEditable();
				if (fieldList.get(i).isFieldOnPrimaryTable()) {
					value = parmMap_.get(fieldList.get(i).getFieldID());
					if (value != null) {
						fieldList.get(i).setValue(value);
					}
				}
			}
			if (panelMode_.equals("DISPLAY")) {
				initializeFieldValues();
				fetchTableRecord();
				resetFieldError();
			}
			if (panelMode_.equals("EDIT")) {
				initializeFieldValues();
				fetchTableRecord();
				checkFieldValueErrors("BU");
				resetFieldError();
				for (int i = 0; i < fieldList.size(); i++) {
					fieldList.get(i).setEditMode(panelMode_);
				}
			}
			if (panelMode_.equals("ADD")) {
				initializeFieldValues();
				if (parmMap_.containsKey("INSTANCE_MODE") && parmMap_.get("INSTANCE_MODE").toString().equals("ADD")) {
					fetchTableRecord();
				}
				fetchReferTableRecords("BC", false, "");
				resetFieldError();
			}
			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).getBasicType().equals("BYTEA")) {
					fieldList.get(i).setupByteaTypeField(fieldList);
				}
			}

			//////////////////////////////////////////////
			// Setup function-keys and function-buttons //
			//////////////////////////////////////////////
			setupFunctionKeysAndButtons();

		} catch(Exception e) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		////////////////
		// Show Frame //
		////////////////
		if (this.isClosing) {
			closeFunction();
		} else {
			if (parmMap_.containsKey("INITIAL_MESSAGE")) {
				jTextAreaMessages.setText((String)parmMap_.get("INITIAL_MESSAGE"));
				parmMap_.remove("INITIAL_MESSAGE");
			} else {
				setMessagesOnPanel();
			}
			setFocusOnComponent();
			session_.setMessageComponent(jScrollPaneMessages);
			this.setVisible(true);
		}

		///////////////////////////////
		// Release instance and exit //
		///////////////////////////////
		instanceIsAvailable_ = true;

		return returnMap_;
	}

	public void setFunctionSpecifications(org.w3c.dom.Element functionElement) throws Exception {
		SortableDomElementListModel sortedList;
		String workAlias, workTableID, workFieldID;
		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;

		/////////////////////////////////////////////
		// Set specifications to the inner variant //
		/////////////////////////////////////////////
		functionElement_ = functionElement;

		/////////////////////////////////
		// Write log to start function //
		/////////////////////////////////
		programSequence = session_.writeLogOfFunctionStarted(functionElement_.getAttribute("ID"), functionElement_.getAttribute("Name"));

		////////////////////////////////
		// Setup Panel Configurations //
		////////////////////////////////
		functionAfterInsert = functionElement_.getAttribute("FunctionAfterInsert");
		initialMsg = functionElement_.getAttribute("InitialMsg");
		if (session_.userMenus.equals("ALL")) {
			jLabelSessionID.setText("<html><u><font color='blue'>" + session_.getSessionID());
			jLabelSessionID.addMouseListener(new MouseAdapter() {
				@Override public void mousePressed(MouseEvent e) {
					try {
						HashMap<String, Object> parmMap = new HashMap<String, Object>();
						parmMap.put("NRSESSION", session_.getSessionID());
						HashMap<String, Object> returnMap = session_.executeFunction("ZF051", parmMap);
						if (returnMap.get("RETURN_TO") != null) {
							returnTo(returnMap.get("RETURN_TO").toString());
						}
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(null, "Unable to call the function ZF051.");
					}
				}
				@Override public void mouseEntered(MouseEvent e) {
					setCursor(session_.editorKit.getLinkCursor());
				}
				@Override public void mouseExited(MouseEvent e) {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			});
		} else {
			jLabelSessionID.setText(session_.getSessionID());
		}
		if (instanceArrayIndex_ >= 0) {
			jLabelFunctionID.setText("200" + "-" + instanceArrayIndex_ + "-" + functionElement_.getAttribute("ID"));
		} else {
			jLabelFunctionID.setText("200"+ "-" + functionElement_.getAttribute("ID"));
		}
		FontMetrics metrics = jLabelFunctionID.getFontMetrics(jLabelFunctionID.getFont());
		jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));

		//////////////////////////////////////////////
		// Setup the primary table and refer tables //
		//////////////////////////////////////////////
		primaryTable_ = new XF200_PrimaryTable(functionElement_, this);
		referTableList.clear();
		NodeList referNodeList = primaryTable_.getTableElement().getElementsByTagName("Refer");
		sortedList = XFUtility.getSortedListModel(referNodeList, "Order");
		for (int i = 0; i < sortedList.getSize(); i++) {
			org.w3c.dom.Element element = (org.w3c.dom.Element)sortedList.getElementAt(i);
			referTableList.add(new XF200_ReferTable(element, this));
		}
		if (functionElement_.getAttribute("DataName").equals("")) {
			dataName = primaryTable_.getName();
		} else {
			dataName = functionElement_.getAttribute("DataName");
		}

		/////////////////////////
		// Setup column fields //
		/////////////////////////
		jPanelFields.removeAll();
		fieldList.clear();
		Dimension dimOfPriviousField = new Dimension(0,0);
		Dimension dim = new Dimension(0,0);
		int posX = 0;
		int posY = 0;
		biggestWidth = 300;
		biggestHeight = 50;
		boolean firstVisibleField = true;
		XF200_Field field;
		XF200_Field preField = null;
		SortableDomElementListModel sortableList;
		SortableDomElementListModel sortableList2;
		NodeList functionFieldList = functionElement_.getElementsByTagName("Field");
		sortableList = XFUtility.getSortedListModel(functionFieldList, "Order");
		for (int i = 0; i < sortableList.getSize(); i++) {

			field = new XF200_Field((org.w3c.dom.Element)sortableList.getElementAt(i), this, -1);
			fieldList.add(field);

			if (field.isVisibleOnPanel()) {
				if (field.getTypeOptionList().contains("ZIPADRS") && preField != null) {
					field.setRefferComponent(preField.getComponent());
				}
				if (firstVisibleField) {
					posX = 0;
					posY = XFUtility.FIELD_VERTICAL_MARGIN + 3;
					firstVisibleField = false;
				} else {
					if (field.isHorizontal()) {
						posX = posX + dimOfPriviousField.width + field.getPositionMargin() + XFUtility.FIELD_HORIZONTAL_MARGIN;
					} else {
						posX = 0;
						posY = posY + dimOfPriviousField.height + field.getPositionMargin() + XFUtility.FIELD_VERTICAL_MARGIN;
					}
				}
				preField = field;

				dim = field.getPreferredSize();
				field.setBounds(posX, posY, dim.width, dim.height);
				if (posX + dim.width > biggestWidth) {
					biggestWidth = posX + dim.width;
				}
				if (posY + dim.height > biggestHeight) {
					biggestHeight = posY + dim.height;
				}

				if (field.isHorizontal()) {
					dimOfPriviousField = new Dimension(dim.width, XFUtility.FIELD_UNIT_HEIGHT);
				} else {
					dimOfPriviousField = new Dimension(dim.width, dim.height);
				}
				jPanelFields.add(field);
			}
		}
		int biggestHeightOfMainPanel = biggestHeight;

		biggestHeight = 60;
		biggestHeightOfTabbedPane = 60;
		jTabbedPaneFields.removeAll();
		NodeList tabList = functionElement_.getElementsByTagName("Tab");
		sortableList = XFUtility.getSortedListModel(tabList, "Order");
		for (int i = 0; i < sortableList.getSize(); i++) {
			JPanel jPanel = new JPanel();
			jPanel.setLayout(null);
			jPanel.setBorder(null);
			JScrollPane jScrollPane = new JScrollPane();
			jScrollPane.setBorder(null);
			jScrollPane.getViewport().add(jPanel, null);
			jTabbedPaneFields.add(((org.w3c.dom.Element)sortableList.getElementAt(i)).getAttribute("Caption"), jScrollPane);
			firstVisibleField = true;

			dimOfPriviousField = new Dimension(0,0);
			functionFieldList = ((org.w3c.dom.Element)sortableList.getElementAt(i)).getElementsByTagName("TabField");
			sortableList2 = XFUtility.getSortedListModel(functionFieldList, "Order");
			for (int j = 0; j < sortableList2.getSize(); j++) {

				field = new XF200_Field((org.w3c.dom.Element)sortableList2.getElementAt(j), this, i);
				fieldList.add(field);

				if (field.getTypeOptionList().contains("ZIPADRS") && preField != null) {
					field.setRefferComponent(preField.getComponent());
				}
				if (firstVisibleField) {
					posX = 0;
					posY = XFUtility.FIELD_VERTICAL_MARGIN + 3;
					firstVisibleField = false;
				} else {
					if (field.isHorizontal()) {
						posX = posX + dimOfPriviousField.width + field.getPositionMargin() + XFUtility.FIELD_HORIZONTAL_MARGIN;
					} else {
						posX = 0;
						posY = posY + dimOfPriviousField.height + field.getPositionMargin() + XFUtility.FIELD_VERTICAL_MARGIN;
					}
				}
				preField = field;

				dim = field.getPreferredSize();
				field.setBounds(posX, posY, dim.width, dim.height);
				if (posX + dim.width > biggestWidth) {
					biggestWidth = posX + dim.width;
				}
				if (posY + dim.height > biggestHeight) {
					biggestHeight = posY + dim.height;
				}

				if (field.isHorizontal()) {
					dimOfPriviousField = new Dimension(dim.width, XFUtility.FIELD_UNIT_HEIGHT);
				} else {
					dimOfPriviousField = new Dimension(dim.width, dim.height);
				}
				jPanel.add(field);
			}
			jPanel.setPreferredSize(new Dimension(biggestWidth + 30, biggestHeight + 10));
			if (biggestHeight > biggestHeightOfTabbedPane) {
				biggestHeightOfTabbedPane = biggestHeight;
			}
		}
		if (sortableList.getSize() > 0) {
			jTabbedPaneFields.setPreferredSize(new Dimension(biggestWidth + 30, biggestHeightOfTabbedPane + 50));
			jSplitPaneWithTabbedFields = new JSplitPane();
			jSplitPaneWithTabbedFields.setOrientation(JSplitPane.VERTICAL_SPLIT);
			jSplitPaneWithTabbedFields.setBorder(null);
			jSplitPaneWithTabbedFields.addKeyListener(new XF200_Component_keyAdapter(this));
			jSplitPaneWithTabbedFields.add(jScrollPaneFields, JSplitPane.TOP);
			jSplitPaneWithTabbedFields.add(jTabbedPaneFields, JSplitPane.BOTTOM);
			jSplitPaneWithTabbedFields.setDividerLocation(biggestHeightOfMainPanel + 20);
			jSplitPaneMain.add(jSplitPaneWithTabbedFields, JSplitPane.TOP);
			biggestHeight = biggestHeightOfMainPanel + biggestHeightOfTabbedPane + 50;
		} else {
			jSplitPaneMain.add(jScrollPaneFields, JSplitPane.TOP);
			biggestHeight = biggestHeightOfMainPanel;
		}
		jPanelFields.setPreferredSize(new Dimension(biggestWidth + 30, biggestHeightOfMainPanel + 10));

		///////////////////////////////////////////////
		// Add prompt-exchange-field as HIDDEN field //
		///////////////////////////////////////////////
		for (int i = 0; i < fieldList.size(); i++) {
			for (int j = 0; j < fieldList.get(i).getAdditionalHiddenFieldList().size(); j++) {
				workTokenizer = new StringTokenizer(fieldList.get(i).getAdditionalHiddenFieldList().get(j), "." );
				workAlias = workTokenizer.nextToken();
				workTableID = getTableIDOfTableAlias(workAlias);
				workFieldID = workTokenizer.nextToken();
				if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
					workElement = session_.getFieldElement(workTableID, workFieldID);
					if (workElement == null) {
						String msg = XFUtility.RESOURCE.getString("FunctionError1") + primaryTable_.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + primaryTable_.getScriptList().get(i).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
						JOptionPane.showMessageDialog(this, msg);
						throw new Exception(msg);
					} else {
						if (primaryTable_.isValidDataSource(workTableID, workAlias, workFieldID)) {
							XF200_Field columnField = new XF200_Field(workTableID, workAlias, workFieldID, this);
							fieldList.add(columnField);
						}
					}
				}
			}
		}

		////////////////////////////////////////////////////////////////
		// Add primary table key fields as HIDDEN fields if necessary //
		////////////////////////////////////////////////////////////////
		for (int i = 0; i < primaryTable_.getKeyFieldList().size(); i++) {
			if (!existsInColumnList(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldList().get(i))) {
				XF200_Field columnField = new XF200_Field(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldList().get(i), this);
				fieldList.add(columnField);
			}
		}

		//////////////////////////////////////////////////////////////////////////
		// Add unique key fields of primary table as HIDDEN fields if necessary //
		//////////////////////////////////////////////////////////////////////////
		for (int i = 0; i < primaryTable_.getUniqueKeyList().size(); i++) {
			workTokenizer = new StringTokenizer(primaryTable_.getUniqueKeyList().get(i), ";" );
			while (workTokenizer.hasMoreTokens()) {
				workFieldID = workTokenizer.nextToken();
				if (!existsInColumnList(primaryTable_.getTableID(), "", workFieldID)) {
					XF200_Field columnField = new XF200_Field(primaryTable_.getTableID(), "", workFieldID, this);
					fieldList.add(columnField);
				}
			}
		}

		/////////////////////////////////////////////////////////////////////////
		// Analyze fields in script and add them as HIDDEN fields if necessary //
		/////////////////////////////////////////////////////////////////////////
		for (int i = 0; i < primaryTable_.getScriptList().size(); i++) {
			if	(primaryTable_.getScriptList().get(i).isToBeRunAtEvent("BC", "")
				|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("AC", "")
				|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("BR", "")
				|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("AR", "")
				|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("BU", "")
				|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("AU", "")
				|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("BD", "")
				|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("AD", "")) {
				for (int j = 0; j < primaryTable_.getScriptList().get(i).getFieldList().size(); j++) {
					workTokenizer = new StringTokenizer(primaryTable_.getScriptList().get(i).getFieldList().get(j), "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
						workElement = session_.getFieldElement(workTableID, workFieldID);
						if (workElement == null) {
							String msg = XFUtility.RESOURCE.getString("FunctionError1") + primaryTable_.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + primaryTable_.getScriptList().get(i).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
							JOptionPane.showMessageDialog(this, msg);
							throw new Exception(msg);
						} else {
							if (primaryTable_.isValidDataSource(workTableID, workAlias, workFieldID)) {
								XF200_Field columnField = new XF200_Field(workTableID, workAlias, workFieldID, this);
								fieldList.add(columnField);
							}
						}
					}
				}
			}
		}

		///////////////////////////////////////////////////////////
		// Add BYTEA-type-field for BYTEA field as HIDDEN fields //
		///////////////////////////////////////////////////////////
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getBasicType().equals("BYTEA") && !fieldList.get(i).getByteaTypeFieldID().equals("")) {
				if (!existsInColumnList(primaryTable_.getTableID(), "", fieldList.get(i).getByteaTypeFieldID())) {
					fieldList.add(new XF200_Field(primaryTable_.getTableID(), "", fieldList.get(i).getByteaTypeFieldID(), this));
				}
			}
		}

		/////////////////////////////////////////////////////////////////////////////
		// Analyze refer tables and add their fields as HIDDEN fields if necessary //
		/////////////////////////////////////////////////////////////////////////////
		for (int i = referTableList.size()-1; i > -1; i--) {
			for (int j = 0; j < referTableList.get(i).getFieldIDList().size(); j++) {
				if (existsInColumnList(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j))) {
					referTableList.get(i).setToBeExecuted(true);
					break;
				}
			}
			if (!referTableList.get(i).isToBeExecuted()) {
				for (int j = 0; j < referTableList.get(i).getWithKeyFieldIDList().size(); j++) {
					workTokenizer = new StringTokenizer(referTableList.get(i).getWithKeyFieldIDList().get(j), "." );
					workAlias = workTokenizer.nextToken();
					workFieldID = workTokenizer.nextToken();
					if (workAlias.equals(primaryTable_.getTableID()) && existsInColumnList(primaryTable_.getTableID(), "", workFieldID)) {
						referTableList.get(i).setToBeExecuted(true);
						break;
					}
				}
			}
			if (referTableList.get(i).isToBeExecuted()) {
				for (int j = 0; j < referTableList.get(i).getFieldIDList().size(); j++) {
					if (!existsInColumnList(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j))) {
						XF200_Field columnField = new XF200_Field(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j), this);
						fieldList.add(columnField);
					}
				}
				for (int j = 0; j < referTableList.get(i).getWithKeyFieldIDList().size(); j++) {
					workTokenizer = new StringTokenizer(referTableList.get(i).getWithKeyFieldIDList().get(j), "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
						XF200_Field columnField = new XF200_Field(workTableID, workAlias, workFieldID, this);
						fieldList.add(columnField);
					}
				}
			}
		}

//		////////////////////////////////////////////////////////////////////////
//		// Analyze refer tables and set their fields as Key-Dependent         //
//		// if their With-Key contains primary key field. Key-Dependent fields //
//		// are not to be edited by prompting features in Edit mode            //
//		////////////////////////////////////////////////////////////////////////
//		boolean containsPrimaryKeyWithinWithKey;
//		for (int i = 0; i < referTableList.size(); i++) {
//			containsPrimaryKeyWithinWithKey = false;
//			for (int j = 0; j < referTableList.get(i).getWithKeyFieldIDList().size(); j++) {
//				for (int k = 0; k < primaryTable_.getKeyFieldList().size(); k++) {
//					if (referTableList.get(i).getWithKeyFieldIDList().get(j).equals(primaryTable_.getTableID() + "." + primaryTable_.getKeyFieldList().get(k))) {
//						containsPrimaryKeyWithinWithKey = true;
//						break;
//					}
//				}
//				if (containsPrimaryKeyWithinWithKey) {
//					break;
//				}
//			}
//			if (containsPrimaryKeyWithinWithKey) {
//				workAlias = referTableList.get(i).getTableAlias();
//				for (int j = 0; j < referTableList.get(i).getFieldIDList().size(); j++) {
//					for (int k = 0; k < fieldList.size(); k++) {
//						workFieldID = referTableList.get(i).getFieldIDList().get(j);
//						if (fieldList.get(k).getTableAlias().equals(workAlias) && fieldList.get(k).getFieldID().equals(workFieldID)) {
//							if (fieldList.get(k).isVisibleOnPanel()) {
//								fieldList.get(k).setKeyDependent(true);
//							}
//						}
//					}
//				}
//			}
//		}

		//////////////////////////////////////
		// Setup Script Engine and Bindings //
		//////////////////////////////////////
		scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
		scriptBindings = scriptEngine.createBindings();
		scriptBindings.put("instance", (XFScriptable)this);
		for (int i = 0; i < fieldList.size(); i++) {
			scriptBindings.put(fieldList.get(i).getDataSourceID(), fieldList.get(i));
		}
	}

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			closeFunction();
		}
	}

	public void setErrorAndCloseFunction() {
		isClosing = true;
		returnMap_.put("RETURN_CODE", "99");
		closeFunction();
	}

//	void returnToMenu() {
//		returnMap_.put("RETURN_TO", "MENU");
//		closeFunction();
//	}
	void returnTo(String target) {
		if (!target.equals("") && !target.equals(functionElement_.getAttribute("ID"))) {
			if (target.equals("MENU")) {
				returnMap_.put("RETURN_TO", "MENU");
			}
			closeFunction();
		}
		if (target.equals(functionElement_.getAttribute("ID"))) {
			returnMap_.remove("RETURN_TO");
		}
	}

	void closeFunction() {
		instanceIsAvailable_ = true;
		messageList.clear();
		if (returnMap_.get("RETURN_CODE").equals("00")) {
			if (panelMode_.equals("EDIT")) {
				returnMap_.put("RETURN_CODE", "21");
			}
			if (panelMode_.equals("ADD") || panelMode_.equals("COPY")) {
				returnMap_.put("RETURN_CODE", "11");
			}
		}
		String errorLog = "";
		if (exceptionLog.size() > 0 || !exceptionHeader.equals("")) {
			errorLog = exceptionHeader + exceptionLog.toString();
		}
		session_.removeMessageComponent(jScrollPaneMessages);
		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), processLog.toString(), errorLog);
		this.setVisible(false);
	}
	
	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(this, message);
		}
		if (panelMode_.equals("DISPLAY")) {
			returnMap_.put("RETURN_CODE", "01");
		}
		if (panelMode_.equals("EDIT")) {
			returnMap_.put("RETURN_CODE", "21");
		}
		if (panelMode_.equals("ADD") || panelMode_.equals("COPY")) {
			returnMap_.put("RETURN_CODE", "11");
		}
		isClosing = true;
	}
	
	public void cancelWithScriptException(ScriptException e, String scriptName) {
		JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError7") + scriptName + XFUtility.RESOURCE.getString("FunctionError8"));
		exceptionHeader = "'" + scriptName + "' Script error\n";
		e.printStackTrace(exceptionStream);
		this.rollback();
		setErrorAndCloseFunction();
	}
	
	public void cancelWithException(Exception e) {
		JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5") + "\n" + e.getMessage());
		e.printStackTrace(exceptionStream);
		this.rollback();
		setErrorAndCloseFunction();
	}

	public void callFunction(String functionID) {
		try {
			returnMap_ = session_.executeFunction(functionID, parmMap_);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			exceptionHeader = e.getMessage();
			setErrorAndCloseFunction();
		}
	}

	public void setStatusMessage(String message) {
		setStatusMessage(message, false);
	}
	public void setStatusMessage(String message, boolean isToReplaceLastLine) {
		if (this.isVisible()) {
			jTextAreaMessages.setText(message);
			jScrollPaneMessages.paintImmediately(0,0,jScrollPaneMessages.getWidth(),jScrollPaneMessages.getHeight());
		}
	}
	
	public void commit() {
		session_.commit(true, processLog);
	}
	
	public void rollback() {
		session_.commit(false, processLog);
	}

	public HashMap<String, Object> getParmMap() {
		return parmMap_;
	}

	public String getUserValueOf(String dataSourceName) {
		return session_.getFilterValue(this.getFunctionID(), dataSourceName);
	}

	public void setUserValueOf(String dataSourceName, Object value) {
		session_.setFilterValue(this.getFunctionID(), dataSourceName, value.toString());
	}
	
	public void setProcessLog(String text) {
		XFUtility.appendLog(text, processLog);
	}
	
	public void startProgress(String text, int maxValue) {
		jProgressBar.setMaximum(maxValue);
		jProgressBar.setValue(0);
		jPanelBottom.remove(jPanelInfo);
		jProgressBar.setString(text);
		jProgressBar.setPreferredSize(jPanelInfo.getPreferredSize());
		jPanelBottom.add(jProgressBar, BorderLayout.EAST);
		this.pack();
	}
	
	public void incrementProgress() {
		jProgressBar.setValue(jProgressBar.getValue() + 1);
		jProgressBar.paintImmediately(0,0,jProgressBar.getWidth(), jProgressBar.getHeight());
		if (jProgressBar.getValue() >= jProgressBar.getMaximum()) {
			endProgress();
		}
	}
	
	public void endProgress() {
		jPanelBottom.remove(jProgressBar);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		this.pack();
		jPanelBottom.repaint();
	}

	public XFTableOperator createTableOperator(String oparation, String tableID) {
		XFTableOperator operator = null;
		try {
			operator = new XFTableOperator(session_, processLog, oparation, tableID);
		} catch (Exception e) {
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
		return operator;
	}

	public XFTableOperator createTableOperator(String sqlText) {
		return createTableOperator(sqlText, false);
	}

	public XFTableEvaluator createTableEvaluator(String tableID) {
		return new XFTableEvaluator(this, tableID);
	}

	public XFTableOperator createTableOperator(String sqlText, boolean isUseCash) {
		XFTableOperator operator = null;
		if (isUseCash) {
			for (int i = 0; i < operatorList.size(); i++) {
				if (operatorList.get(i).getSqlText().equals(sqlText)) {
					operator = operatorList.get(i);
					operator.resetCursor();
					break;
				}
			}
			if (operator == null ) {
				operator = new XFTableOperator(session_, processLog, sqlText);
				operatorList.add(operator);
			}
		} else {
			operator = new XFTableOperator(session_, processLog, sqlText);
		}
		return operator;
	}

	public HashMap<String, Object> getReturnMap() {
		return returnMap_;
	}

	void setFocusOnComponent() {
		boolean noFieldFocused = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isVisibleOnPanel() && fieldList.get(i).isEditable() && fieldList.get(i).isComponentFocusable()) {
				fieldList.get(i).requestFocus();
				noFieldFocused = false;
				break;
			}
		}
		if (noFieldFocused) {
			jPanelMain.requestFocus();
		}
	}

	void setupFunctionKeysAndButtons() {
		int workIndex;
		org.w3c.dom.Element element;
		InputMap inputMap  = jPanelMain.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.clear();
		ActionMap actionMap = jPanelMain.getActionMap();
		actionMap.clear();

		for (int i = 0; i < 7; i++) {
			jButtonArray[i].setText("");
			jButtonArray[i].setVisible(false);
			jButtonNumberArray[i] = "";
			actionDefinitionArray[i] = "";
		}

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHECK");
		actionMap.put("CHECK", checkAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMap.put("HELP", helpAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
		actionMap.put("ESCAPE", escapeAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T,ActionEvent.CTRL_MASK), "FOCUS_TAB");
		actionMap.put("FOCUS_TAB", focusTabAction);

		functionKeyToEdit = "";
		actionTextToDelete = "";
		buttonToEdit = null;
		buttonToCopy = null;
		buttonToDelete = null;
		buttonIndexForF6 = -1;
		buttonIndexForF8 = -1;

		NodeList buttonList = functionElement_.getElementsByTagName("Button");
		for (int i = 0; i < buttonList.getLength(); i++) {
			element = (org.w3c.dom.Element)buttonList.item(i);

			if (panelMode_.equals("ADD")
					&& (element.getAttribute("Action").equals("COPY")
							|| element.getAttribute("Action").equals("DELETE"))) {
			} else {
				if (parmMap_.containsKey("INSTANCE_MODE") && parmMap_.get("INSTANCE_MODE").toString().equals("INQ")
						&& (element.getAttribute("Action").equals("EDIT")
								|| element.getAttribute("Action").equals("COPY")
								|| element.getAttribute("Action").equals("DELETE"))) {
//					if (element.getAttribute("Action").equals("EDIT")
//							&& functionElement_.getAttribute("UpdateOnly").equals("T")) {
//						functionKeyToEdit = "F" + element.getAttribute("Number");
//						buttonToEdit.setText(functionKeyToEdit + " " + element.getAttribute("Caption"));
//					}
				} else {
					workIndex = Integer.parseInt(element.getAttribute("Position"));

					jButtonNumberArray[workIndex] = element.getAttribute("Number");
					actionDefinitionArray[workIndex] = element.getAttribute("Action");
					jButtonArray[workIndex].setVisible(true);

					inputMap.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
					actionMap.put("actionButton" + workIndex, actionButtonArray[workIndex]);

					if (element.getAttribute("Action").equals("EDIT")) {
						buttonToEdit = jButtonArray[workIndex];
						functionKeyToEdit = "F" + element.getAttribute("Number");
					}
					if (element.getAttribute("Action").equals("COPY")) {
						buttonToCopy = jButtonArray[workIndex];
					}
					if (element.getAttribute("Action").equals("DELETE")) {
						buttonToDelete = jButtonArray[workIndex];
						actionTextToDelete = element.getAttribute("Caption");
					}

					if (panelMode_.equals("ADD") && element.getAttribute("Action").equals("EDIT")) {
						XFUtility.setCaptionToButton(jButtonArray[workIndex], element, XFUtility.RESOURCE.getString("Add"), this.getPreferredSize().width / 8, session_);
					} else {
						XFUtility.setCaptionToButton(jButtonArray[workIndex], element, "", this.getPreferredSize().width / 8, session_);
						if (element.getAttribute("Action").equals("EDIT") && panelMode_.equals("EDIT")) {
							buttonToEdit.setText(functionKeyToEdit + " " + element.getAttribute("Caption"));
						}
					}

					if (element.getAttribute("Number").equals("6")) {
						buttonIndexForF6 = workIndex;
					}
					if (element.getAttribute("Number").equals("8")) {
						buttonIndexForF8 = workIndex;
					}
				}
			}
		}
		if (panelMode_.equals("EDIT")) {
//			buttonToEdit.setText(functionKeyToEdit + " " + XFUtility.RESOURCE.getString("Update"));
			if (buttonToCopy != null) {
				buttonToCopy.setVisible(false);
			}
			if (buttonToDelete != null) {
				buttonToDelete.setVisible(false);
			}
		}
	}

	void fetchTableRecord() {
		try {

			for (int i = 0; i < fieldList.size(); i++) {
				if (parmMap_.containsKey(fieldList.get(i).getDataSourceName())) {
					fieldList.get(i).setValue(parmMap_.get(fieldList.get(i).getDataSourceName()));
					parmMap_.put(fieldList.get(i).getFieldID(), parmMap_.get(fieldList.get(i).getDataSourceName()));
				} else {
					if (parmMap_.containsKey(fieldList.get(i).getFieldID())) {
						fieldList.get(i).setValue(parmMap_.get(fieldList.get(i).getFieldID()));
					} else {
						fieldList.get(i).setValue(fieldList.get(i).getNullValue());
					}
				}
			}

			primaryTable_.runScript("BR", "");

			XFTableOperator operatorPrimary = createTableOperator(primaryTable_.getSQLToSelect());
			if (operatorPrimary.next()) {

				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).getTableAlias().equals(primaryTable_.getTableID())) {
						fieldList.get(i).setValueOfResultSet(operatorPrimary);
					}
				}

				fetchReferTableRecords("AR", false, "");

				primaryTable_.setUpdateCounterValue(operatorPrimary);

			} else {
				JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError30"));
				returnMap_.put("RETURN_CODE", "01");
				isClosing = true;
			}

			columnOldValueMap.clear();
			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).getTableID().equals(primaryTable_.getTableID())) {
					columnOldValueMap.put(fieldList.get(i).getFieldID(), fieldList.get(i).getInternalValue());
				}
			}

			ArrayList<String> disabledButtonNumberList = new ArrayList<String>();
			if (parmMap_.get("DISABLED_BUTTON_LIST") != null) {
				String wrkStr = parmMap_.get("DISABLED_BUTTON_LIST").toString();
				StringTokenizer tokenizer = new StringTokenizer(wrkStr, ",");
				while (tokenizer.hasMoreTokens()) {
					wrkStr = tokenizer.nextToken();
					disabledButtonNumberList.add(wrkStr);
				}
			}
			for (int i = 0; i < 7; i++) {
				if (disabledButtonNumberList.contains(jButtonNumberArray[i])) {
					jButtonArray[i].setEnabled(false);
				} else {
					jButtonArray[i].setEnabled(true);
				}
			}

		} catch(ScriptException e) {
			this.cancelWithScriptException(e, this.getScriptNameRunning());
		} catch(Exception e) {
			this.cancelWithException(e);
		}
	}

	boolean hasParmMapWithCompleteKeySet() {
		boolean hasCompleteKeySet = true;
		for (int i = 0; i < primaryTable_.getKeyFieldList().size(); i++) {
			if (!parmMap_.containsKey(primaryTable_.getKeyFieldList().get(i)) && !parmMap_.containsKey(primaryTable_.getTableID() + "." + primaryTable_.getKeyFieldList().get(i))) {
				hasCompleteKeySet = false;
				break;
			}
		}
//		if (hasCompleteKeySet && parmMap_.containsKey("INSTANCE_MODE")) {
//			if (parmMap_.get("INSTANCE_MODE").toString().equals("ADD")) {
//				hasCompleteKeySet = false;
//			}
//		}
		return hasCompleteKeySet;
	}
	
	void initializeFieldValues() {
		this.hasAutoNumberField = false;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isAutoNumberField() || fieldList.get(i).isAutoDetailRowNumber()) {
				fieldList.get(i).setValue("*AUTO");
				this.hasAutoNumberField = true;
			} else {
				if (parmMap_.containsKey(fieldList.get(i).getFieldID())) {
					fieldList.get(i).setValue(parmMap_.get(fieldList.get(i).getFieldID()));
					fieldList.get(i).setComponentEditable(false);
				} else {
					if (fieldList.get(i).isEditable() && fieldList.get(i).getComponent() instanceof XF200_ComboBox) {
						XF200_ComboBox comboBox = (XF200_ComboBox)fieldList.get(i).getComponent();
						comboBox.setSelectedIndex(0);
					} else {
						fieldList.get(i).setValue(fieldList.get(i).getNullValue());
					}
				}
			}
		}
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isPromptListField()) {
				fieldList.get(i).setValue(fieldList.get(i).getInternalValue());
			}
		}
	}

	void resetFieldError() {
		columnValueMap.clear();
		for (int i = 0; i < fieldList.size(); i++) {
			fieldList.get(i).setError(false);
			if (fieldList.get(i).getTableID().equals(primaryTable_.getTableID())) {
				columnValueMap.put(fieldList.get(i).getFieldID(), fieldList.get(i).getInternalValue());
			}
		}
		firstErrorTabIndex = -1;
	}

	int checkFieldValueErrors(String event) {
		int countOfErrors = fetchReferTableRecords(event, true, "");
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isNullError(panelMode_) || fieldList.get(i).isTooLong(panelMode_)) {
				countOfErrors++;
			}
		}
		if (this.isClosing) {
			closeFunction();
		}
		return countOfErrors;
	}
	
	synchronized int checkDeleteErrors() {
		int countOfErrors = fetchReferTableRecords("BD", true, "");
		return countOfErrors;
	}
	
	protected int fetchReferTableRecords(String event, boolean toBeChecked, String specificReferTable) {
		int countOfErrors = 0;
		boolean recordNotFound;
		XFTableOperator operator;

		try {
			countOfErrors = countOfErrors + primaryTable_.runScript(event, "BR()");

			for (int i = 0; i < referTableList.size(); i++) {
				if (specificReferTable.equals("") || specificReferTable.equals(referTableList.get(i).getTableAlias())) {

					if (referTableList.get(i).isToBeExecuted()) {

						countOfErrors = countOfErrors + primaryTable_.runScript(event, "BR(" + referTableList.get(i).getTableAlias() + ")");

						for (int j = 0; j < fieldList.size(); j++) {
							if (fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())
									&& !fieldList.get(j).isPromptListField()) {
								fieldList.get(j).setValue(null);
							}
						}

						if (!referTableList.get(i).isKeyNullable() || !referTableList.get(i).isKeyNull()) {
							recordNotFound = true;

							operator = createTableOperator(referTableList.get(i).getSelectSQL(false), true);
							if (operator.next()) {
								recordNotFound = false;
								for (int j = 0; j < fieldList.size(); j++) {
									if (fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
										fieldList.get(j).setValueOfResultSet(operator);
									}
								}
								countOfErrors = countOfErrors + primaryTable_.runScript(event, "AR(" + referTableList.get(i).getTableAlias() + ")");
							}

							if (recordNotFound && toBeChecked && !referTableList.get(i).isOptional()) {
								countOfErrors++;
								referTableList.get(i).setErrorOnRelatedFields();
							}
						}
					}
				}
			}

			///////////////////////////////////////////////
			// Run Script for AfterRead-all-refer-tables //
			///////////////////////////////////////////////
			countOfErrors = countOfErrors + primaryTable_.runScript(event, "AR()");

			//////////////////////////
			// Set fields edit mode //
			//////////////////////////
			for (int i = 0; i < fieldList.size(); i++) {
				fieldList.get(i).setEditMode(panelMode_);
			}

		} catch(ScriptException e) {
			this.cancelWithScriptException(e, this.getScriptNameRunning());
		} catch(Exception e) {
			this.cancelWithException(e);
		}

		if (toBeChecked) {
			return countOfErrors;
		} else {
			return 0;
		}
	}
	
	synchronized boolean hasNoErrorWithKey(String operation) {
		boolean hasNoError = true;
		ArrayList<String> uniqueKeyList = new ArrayList<String>();
		ArrayList<String> keyFieldList = new ArrayList<String>();
		String sql = "";
		XFTableOperator operator;

		try {
			if (operation.equals("INSERT") && !this.hasAutoNumberField) {
				operator = createTableOperator(primaryTable_.getSQLToCheckPKDuplication());
				if (operator.next()) {
					hasNoError = false;
					for (int i = 0; i < fieldList.size(); i++) {
						if (fieldList.get(i).isKey()) {
							fieldList.get(i).setError(XFUtility.RESOURCE.getString("FunctionError31"));
						}
					}
				}
			}

			if (operation.equals("UPDATE") && primaryTable_.hasPrimaryKeyValueAltered()) {
				hasNoError = false;
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).isKey() && !fieldList.get(i).isError()) {
						fieldList.get(i).setError(XFUtility.RESOURCE.getString("FunctionError32"));
					}
				}
			}

			if (hasNoError) {
				StringTokenizer workTokenizer;
				uniqueKeyList = primaryTable_.getUniqueKeyList();
				for (int i = 0; i < uniqueKeyList.size(); i++) {
					keyFieldList.clear();
					workTokenizer = new StringTokenizer(uniqueKeyList.get(i), ";" );
					while (workTokenizer.hasMoreTokens()) {
						keyFieldList.add(workTokenizer.nextToken());
					}
					if (operation.equals("UPDATE")) {
						sql = primaryTable_.getSQLToCheckSKDuplication(keyFieldList, true);
					} else {
						sql = primaryTable_.getSQLToCheckSKDuplication(keyFieldList, false);
					}
					if (!sql.equals("")) {
						operator = createTableOperator(sql);
						if (operator.next()) {
							hasNoError = false;
							for (int j = 0; j < fieldList.size(); j++) {
								if (keyFieldList.contains(fieldList.get(j).getFieldID())) {
									fieldList.get(j).setError(XFUtility.RESOURCE.getString("FunctionError22"));
								}
							}
						}
					}
				}
			}

		} catch(Exception e) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5") + "\n" + e.getMessage());
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
		return hasNoError;
	}

	void setMessagesOnPanel() {
		boolean isFirstErrorField = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isError()) {
				if (isFirstErrorField) {
					fieldList.get(i).requestFocus();
					isFirstErrorField = false;
				}
				messageList.add(fieldList.get(i).getCaption() + XFUtility.RESOURCE.getString("Colon") + fieldList.get(i).getError());
			}
			if (!fieldList.get(i).getWarning().equals("")) {
				messageList.add(fieldList.get(i).getCaption() + XFUtility.RESOURCE.getString("Colon") + XFUtility.RESOURCE.getString("Warning") + fieldList.get(i).getWarning());
			}
			/////////////////////////////////////////////////////////
			// required step to set focus on the first error field //
			/////////////////////////////////////////////////////////
			fieldList.get(i).setFocusable(false);
		}

		jTextAreaMessages.setText("");
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < messageList.size(); i++) {
			if (i > 0) {
				sb.append("\n");
			}
			if (messageList.size() > 1) {
				sb.append("(" + Integer.toString(i+1) + "/"+ Integer.toString(messageList.size()) + ") " + messageList.get(i));
			} else {
				sb.append(messageList.get(i));
			}
		}
		jTextAreaMessages.setText(sb.toString());
		jTextAreaMessages.setCaretPosition(0);

		int heightOfErrorMessages = (messageList.size() + 1) * 25;
		if (heightOfErrorMessages <= 50) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - 150);
		}
		if (heightOfErrorMessages > 50 && heightOfErrorMessages <= 240) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - heightOfErrorMessages - 80);
		}
		if (heightOfErrorMessages > 240) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - 240 - 80);
		}
	}

	void component_keyPressed(KeyEvent e) {
		///////////////////////////////////////////
		// Steps to hack F6 and F8 of JSplitPane //
		///////////////////////////////////////////
		if (e.getKeyCode() == KeyEvent.VK_F6 && buttonIndexForF6 != -1) {
			jButtonArray[buttonIndexForF6].doClick();
		}
		if (e.getKeyCode() == KeyEvent.VK_F8 && buttonIndexForF8 != -1) {
			jButtonArray[buttonIndexForF8].doClick();
		}
	}

	void doButtonAction(String action) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			messageList.clear();

			if (action.equals("EXIT")) {
				closeFunction();
			}

			if (action.equals("EDIT")) {
				if (panelMode_.equals("ADD") || panelMode_.equals("COPY")) {
					isCheckOnly = false;
					doButtonActionInsert();
				}
				if (panelMode_.equals("EDIT")) {
					isCheckOnly = false;
					doButtonActionUpdate();
				}
				if (panelMode_.equals("DISPLAY")) {
					doButtonActionSetEditMode();
				}
			}

			if (action.equals("COPY")) {
				if (panelMode_.equals("DISPLAY")) {
					doButtonActionSetCopyMode();
				}
			}

			if (action.equals("DELETE")) {
				if (panelMode_.equals("DISPLAY")) {
					doButtonActionDelete();
				}
			}

			if (action.equals("OUTPUT")) {
				session_.browseFile(getExcellBookURI());
			}

			if (action.contains("CALL(")) {
				doButtonActionCall(action);
			}

			setMessagesOnPanel();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	void doButtonActionInsert() {
		XFTableOperator operator;

		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			resetFieldError();

			int countOfErrors = checkFieldValueErrors("BC");
			if (countOfErrors == 0) {

				if (hasNoErrorWithKey("INSERT")) {

					if (this.isCheckOnly) {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage16"));
					} else {

						for (int i = 0; i < fieldList.size(); i++) {
							if (fieldList.get(i).isAutoNumberField()) {
								fieldList.get(i).setValue(fieldList.get(i).getAutoNumber());
							}
							if (fieldList.get(i).isAutoDetailRowNumber()) {
								int lastNumber = 0;
								operator = createTableOperator(primaryTable_.getSQLToGetLastDetailRowNumberValue());
								if (operator.next()) {
									lastNumber = Integer.parseInt(operator.getValueOf(primaryTable_.getDetailRowNoID()).toString());
								}
								fieldList.get(i).setValue(lastNumber + 1);
							}
							if (fieldList.get(i).isKey()) {
								returnMap_.put(fieldList.get(i).getFieldID(), fieldList.get(i).getValue());
							}
						}

						operator = createTableOperator(primaryTable_.getSQLToInsert());
						int recordCount = operator.execute();
						if (recordCount == 1) {

							primaryTable_.runScript("AC", "");

							if (this.isClosing) {
								this.rollback();
							} else {
								this.commit();

								if (functionAfterInsert.equals("")) {

									if (functionElement_.getAttribute("ContinueAdd").equals("T")) {
										Object[] bts = {XFUtility.RESOURCE.getString("Exit"), XFUtility.RESOURCE.getString("Continue")} ;
										int reply = JOptionPane.showOptionDialog(jPanelMain, XFUtility.RESOURCE.getString("FunctionMessage17") + dataName + XFUtility.RESOURCE.getString("FunctionMessage18"), XFUtility.RESOURCE.getString("CheckToContinue"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
										if (reply == 1) {
											for (int i = 0; i < fieldList.size(); i++) {
												if (fieldList.get(i).isAutoNumberField() || fieldList.get(i).isAutoDetailRowNumber()) {
													fieldList.get(i).setValue("*AUTO");
												}
											}
											if (initialMsg.equals("")) {
												messageList.add(XFUtility.RESOURCE.getString("FunctionMessage19"));
											} else {
												messageList.add(initialMsg);
											}
										} else {
											returnMap_.put("RETURN_CODE", "10");
											closeFunction();
										}
									} else {
										returnMap_.put("RETURN_CODE", "10");
										closeFunction();
									}

								} else {
									messageList.clear();
									HashMap<String, Object> map = new HashMap<String, Object>();
									for (int i = 0; i < fieldList.size(); i++) {
										if (fieldList.get(i).isKey()) {
											map.put(fieldList.get(i).getFieldID(), fieldList.get(i).getInternalValue());
										}
									}
									try {
										session_.executeFunction(functionAfterInsert, map);
									} catch(Exception e) {
										JOptionPane.showMessageDialog(this, e.getMessage());
										e.printStackTrace(exceptionStream);
									}
									returnMap_.put("RETURN_CODE", "10");
									closeFunction();
								}
							}
						} else {
							String message = XFUtility.RESOURCE.getString("FunctionError50");
							JOptionPane.showMessageDialog(this, message);
							exceptionHeader = message;
							this.rollback();
							setErrorAndCloseFunction();
						}
					}
				}
			}
		} catch(ScriptException e) {
			this.cancelWithScriptException(e, this.getScriptNameRunning());
		} catch(Exception e) {
			this.cancelWithException(e);
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	void doButtonActionUpdate() {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			resetFieldError();

			int countOfErrors = checkFieldValueErrors("BU");
			if (countOfErrors == 0) {
				if (hasNoErrorWithKey("UPDATE")) {
					if (this.isCheckOnly) {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage9"));
					} else {
						XFTableOperator operator = createTableOperator(primaryTable_.getSQLToUpdate());
						int recordCount = operator.execute();
						if (recordCount == 1) {
							primaryTable_.runScript("AU", "");
							if (this.isClosing) {
								this.rollback();
							} else {
								this.commit();
								returnMap_.put("RETURN_CODE", "20");
							}
						} else {
							String errorMessage = XFUtility.RESOURCE.getString("FunctionError19");
							JOptionPane.showMessageDialog(jPanelMain, errorMessage);
							exceptionHeader = errorMessage.replace("\n", " ");
							this.rollback();
							setErrorAndCloseFunction();
						}
						closeFunction();
					}
				}
			}
		} catch(ScriptException e) {
			this.cancelWithScriptException(e, this.getScriptNameRunning());
		} catch(Exception e) {
			this.cancelWithException(e);
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	void doButtonActionSetEditMode() {
		this.setTitle(XFUtility.RESOURCE.getString("FunctionMessage22") + dataName + XFUtility.RESOURCE.getString("FunctionMessage23"));
		panelMode_ = "EDIT";
		returnMap_.put("RETURN_CODE", "21");
		resetFieldError();
		checkFieldValueErrors("BU");
		for (int i = 0; i < fieldList.size(); i++) {
			fieldList.get(i).setEditMode(panelMode_);
		}
		setFocusOnComponent();
		buttonToEdit.setText(functionKeyToEdit + " " + XFUtility.RESOURCE.getString("Update"));
		if (buttonToCopy != null) {
			buttonToCopy.setVisible(false);
		}
		if (buttonToDelete != null) {
			buttonToDelete.setVisible(false);
		}
		messageList.add(XFUtility.RESOURCE.getString("FunctionMessage24"));
	}
		
	void doButtonActionSetCopyMode() {
		this.setTitle(XFUtility.RESOURCE.getString("FunctionMessage25") + dataName + XFUtility.RESOURCE.getString("FunctionMessage26"));
		panelMode_ = "COPY";
		returnMap_.put("RETURN_CODE", "11");

		for (int i = 0; i < fieldList.size(); i++) {
			fieldList.get(i).initEditable();
		}
		fetchReferTableRecords("BC", false, "");
		resetFieldError();

		this.hasAutoNumberField = false;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isAutoNumberField() || fieldList.get(i).isAutoDetailRowNumber()) {
				fieldList.get(i).setValue("*AUTO");
				fieldList.get(i).setEditable(false);
				this.hasAutoNumberField = true;
			}
			fieldList.get(i).setEditMode(panelMode_);
		}

		setFocusOnComponent();

		if (buttonToEdit != null && buttonToCopy != null) {
			buttonToEdit.setText(functionKeyToEdit + " " + XFUtility.RESOURCE.getString("Add"));
			buttonToCopy.setVisible(false);
		}
		if (buttonToDelete != null) {
			buttonToDelete.setVisible(false);
		}

		messageList.clear();
		messageList.add(XFUtility.RESOURCE.getString("FunctionMessage15"));
	}

	void doButtonActionDelete() {
		resetFieldError();
		int countOfErrors = checkDeleteErrors();
		if (countOfErrors == 0) {

			Object[] bts = {XFUtility.RESOURCE.getString("Yes"), XFUtility.RESOURCE.getString("No")} ;
			int reply = JOptionPane.showOptionDialog(jPanelMain, XFUtility.RESOURCE.getString("FunctionMessage27") + actionTextToDelete + XFUtility.RESOURCE.getString("FunctionMessage28"), XFUtility.RESOURCE.getString("CheckToDelete"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
			if (reply == 0) {
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));

					XFTableOperator operator = createTableOperator(primaryTable_.getSQLToDelete());
					int recordCount = operator.execute();
					if (recordCount == 1) {
						primaryTable_.runScript("AD", "");

						if (this.isClosing) {
							this.rollback();
						} else {
							this.commit();
							returnMap_.put("RETURN_CODE", "30");
						}
					} else {
						String errorMessage = XFUtility.RESOURCE.getString("FunctionError33");
						JOptionPane.showMessageDialog(jPanelMain, errorMessage);
						exceptionHeader = errorMessage.replace("\n", " ");
						this.rollback();
						setErrorAndCloseFunction();
					}

					closeFunction();

				} catch(ScriptException e) {
					this.cancelWithScriptException(e, this.getScriptNameRunning());
				} catch(Exception e) {
					this.cancelWithException(e);
				} finally {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}

			} else {
				messageList.add(XFUtility.RESOURCE.getString("ReturnMessage31"));
				setMessagesOnPanel();
			}
		}
	}
	
	void doButtonActionCall(String action) {
		int pos1 = action.indexOf("CALL(", 0);
		if (pos1 >= 0) {
			int pos2 = action.indexOf(")", pos1);
			String functionAndParms = action.substring(pos1+5, pos2);
			messageList.clear();
			try {
				HashMap<String, Object> workMap = new HashMap<String, Object>();
				workMap.putAll(parmMap_);
				workMap.remove("INSTANCE_MODE");
				StringTokenizer workTokenizer1, workTokenizer2, workTokenizer3;
				String parmName, parmValue;
				workTokenizer1 = new StringTokenizer(functionAndParms, "," );
				String functionID = workTokenizer1.nextToken();
				if (workTokenizer1.hasMoreTokens()) {
					workTokenizer2 = new StringTokenizer(workTokenizer1.nextToken(), ";" );
					while (workTokenizer2.hasMoreTokens()) {
						workTokenizer3 = new StringTokenizer(workTokenizer2.nextToken(), ":" );
						if (workTokenizer3.countTokens() == 2) {
							parmName = workTokenizer3.nextToken();
							parmValue = workTokenizer3.nextToken();
							workMap.put(parmName, parmValue);
						}
					}
				}
				HashMap<String, Object> returnMap = session_.executeFunction(functionID, workMap);
				if (returnMap.get("RETURN_TO") != null) {
					returnTo(returnMap.get("RETURN_TO").toString());
				}
				if (returnMap.get("RETURN_MESSAGE") == null) {
					messageList.add(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
				} else {
					messageList.add(returnMap.get("RETURN_MESSAGE").toString());
				}
				returnMap_.put("RETURN_CODE", returnMap.get("RETURN_CODE").toString());
				if (returnMap_.get("RETURN_CODE").toString().equals("10")
						|| returnMap_.get("RETURN_CODE").toString().equals("20")) {
					fetchTableRecord();
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		}
	}

	URI getExcellBookURI() {
		File xlsFile = null;
		String xlsFileName = "";
		FileOutputStream fileOutputStream = null;

		XSSFWorkbook workBook = new XSSFWorkbook();
		String wrkStr = functionElement_.getAttribute("Name").replace("/", "_").replace("�^", "_");
		XSSFSheet workSheet = workBook.createSheet(wrkStr);
		workSheet.setDefaultRowHeight((short)1000);
		Footer workSheetFooter = workSheet.getFooter();
		workSheetFooter.setRight(functionElement_.getAttribute("Name") + "  Page &P / &N");

		XSSFFont fontDefault = workBook.createFont();
		fontDefault = workBook.createFont();
		fontDefault.setFontName(session_.systemFont);
		fontDefault.setFontHeightInPoints((short)11);

		XSSFCellStyle styleHeader = workBook.createCellStyle();
		styleHeader.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		styleHeader.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		styleHeader.setBorderRight(XSSFCellStyle.BORDER_THIN);
		styleHeader.setBorderTop(XSSFCellStyle.BORDER_THIN);
		styleHeader.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
		styleHeader.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		styleHeader.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
		styleHeader.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		styleHeader.setFont(fontDefault);

		int currentRowNumber = -1;
		int mergeRowNumberFrom = -1;

		try {
			xlsFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".xlsx");
			xlsFileName = xlsFile.getPath();
			fileOutputStream = new FileOutputStream(xlsFileName);

			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).isVisibleOnPanel()) {
					for (int j = 0; j < fieldList.get(i).getRows(); j++) {

						currentRowNumber++;
						XSSFRow rowData = workSheet.createRow(currentRowNumber);

						//////////////////////////////////
						// Cells for header field label //
						//////////////////////////////////
						XSSFCell cellHeader = rowData.createCell(0);
						cellHeader.setCellStyle(styleHeader);
						if (j==0) {
							mergeRowNumberFrom = currentRowNumber;
							if (!fieldList.get(i).getFieldOptionList().contains("NO_CAPTION")) {
								cellHeader.setCellValue(new XSSFRichTextString(fieldList.get(i).getCaption()));
							}
						}
						rowData.createCell(1).setCellStyle(styleHeader);

						/////////////////////////////////
						// Cells for header field data //
						/////////////////////////////////
						setupCellAttributesForHeaderField(rowData, workBook, workSheet, fieldList.get(i), currentRowNumber, j);
					}
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 0, 1));
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 2, 8));
				}
			}
			workBook.write(fileOutputStream);
			messageList.add(XFUtility.RESOURCE.getString("XLSComment1"));

		} catch(Exception e) {
			messageList.add(XFUtility.RESOURCE.getString("XLSErrorMessage"));
			e.printStackTrace(exceptionStream);
		} finally {
			try {
				fileOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace(exceptionStream);
			}
		}
		return xlsFile.toURI();
	}

	private void setupCellAttributesForHeaderField(XSSFRow rowData, XSSFWorkbook workBook, XSSFSheet workSheet, XF200_Field object, int currentRowNumber, int rowIndexInCell) {
		String wrkStr;

		XSSFFont font = workBook.createFont();
		font.setFontHeightInPoints((short)11);
		font.setFontName(session_.systemFont);
		if (!object.getColor().equals("black")) {
			font.setColor(new XSSFColor(object.getForeground()));
		}
		XSSFDataFormat format = workBook.createDataFormat();

		XSSFCellStyle style = workBook.createCellStyle();
		style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		style.setBorderRight(XSSFCellStyle.BORDER_THIN);
		style.setBorderTop(XSSFCellStyle.BORDER_THIN);
		style.setFont(font);
		style.setAlignment(XSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
		style.setWrapText(true);
		style.setDataFormat(format.getFormat("text"));

		XSSFCell cellValue = rowData.createCell(2);

		if (object.getBasicType().equals("INTEGER")) {
			if (object.getTypeOptionList().contains("MSEQ") || object.getTypeOptionList().contains("FYEAR")) {
				cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
				cellValue.setCellStyle(style);
				cellValue.setCellValue(new XSSFRichTextString((String)object.getExternalValue()));
			} else {
				style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
				if (object.getTypeOptionList().contains("PERCENT")) {
					style.setDataFormat(format.getFormat("0%"));
				} else {
					if (!object.getTypeOptionList().contains("NO_EDIT")
							&& !object.getTypeOptionList().contains("ZERO_SUPPRESS")) {
						style.setDataFormat(format.getFormat("#,##0"));
					}
				}
				cellValue.setCellStyle(style);
				if (rowIndexInCell==0) {
					if (object.getExternalValue() == null) {
						wrkStr = "";
					} else {
						wrkStr = XFUtility.getStringNumber(object.getExternalValue().toString());
					}
					if (object.getTypeOptionList().contains("PERCENT")) {
						if (wrkStr.equals("")) {
							wrkStr = "0.0";
						} else {
							wrkStr = Float.toString(Float.parseFloat(wrkStr) / 100);
						}
						cellValue.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
						cellValue.setCellValue(Double.parseDouble(wrkStr));
					} else {
						if (wrkStr.equals("") || object.getTypeOptionList().contains("NO_EDIT")) {
							cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
							cellValue.setCellValue(new XSSFRichTextString(wrkStr));
						} else {
							cellValue.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
							cellValue.setCellValue(Double.parseDouble(wrkStr));
						}
					}
				}
			}
		} else {
			if (object.getBasicType().equals("FLOAT")) {
				style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
				if (!object.getTypeOptionList().contains("NO_EDIT")
					&& !object.getTypeOptionList().contains("ZERO_SUPPRESS")) {
					style.setDataFormat(XFUtility.getFloatFormat(workBook, object.getDecimalSize()));
				}
				cellValue.setCellStyle(style);
				if (rowIndexInCell==0) {
					if (object.getExternalValue() == null) {
						wrkStr = "";
					} else {
						wrkStr = XFUtility.getStringNumber(object.getExternalValue().toString());
					}
					if (wrkStr.equals("") || object.getTypeOptionList().contains("NO_EDIT")) {
						cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
						cellValue.setCellValue(new XSSFRichTextString(wrkStr));
					} else {
						cellValue.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
						cellValue.setCellValue(Double.parseDouble(wrkStr));
					}
				}
			} else {
				cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
				cellValue.setCellStyle(style);
				if (rowIndexInCell==0) {
					if (object.getBasicType().equals("STRING")) {
						if (object.isImage()) {
							style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
							style.setVerticalAlignment(XSSFCellStyle.VERTICAL_BOTTOM);
							style.setDataFormat(format.getFormat("text"));
							cellValue.setCellStyle(style);
							cellValue.setCellValue(new XSSFRichTextString((String)object.getInternalValue()));
							try {
								XFUtility.setupImageCell(workBook, workSheet, currentRowNumber, currentRowNumber + object.getRows(), 2, 9,(String)object.getExternalValue());
							} catch(Exception e) {
								e.printStackTrace(exceptionStream);
							}
						} else {
							cellValue.setCellStyle(style);
							cellValue.setCellValue(new XSSFRichTextString((String)object.getExternalValue()));
						}
					}
					if (object.getBasicType().equals("DATE")) {
						java.util.Date utilDate = XFUtility.convertDateFromStringToUtil((String)object.getInternalValue());
						String text = XFUtility.getUserExpressionOfUtilDate(utilDate, session_.getDateFormat(), false);
						cellValue.setCellValue(new XSSFRichTextString(text));
					}
					if (object.getBasicType().equals("DATETIME") || object.getBasicType().equals("TIME")) {
						cellValue.setCellValue(new XSSFRichTextString(object.getInternalValue().toString()));
					}
				}
			}
		}
		rowData.createCell(3).setCellStyle(style);
		rowData.createCell(4).setCellStyle(style);
		rowData.createCell(5).setCellStyle(style);
		rowData.createCell(6).setCellStyle(style);
		rowData.createCell(7).setCellStyle(style);
		rowData.createCell(8).setCellStyle(style);
	}

	public String getFunctionID() {
		return functionElement_.getAttribute("ID");
	}

	public void setFirstErrorTabIndex(int index) {
		if (!isCheckOnly && firstErrorTabIndex == -1 && index > -1) {
			firstErrorTabIndex = index;
			jTabbedPaneFields.setSelectedIndex(firstErrorTabIndex);
		}
	}
	
	public String getScriptNameRunning() {
		return scriptNameRunning;
	}

	public Session getSession() {
		return session_;
	}

	public String getPanelMode() {
		return panelMode_;
	}

	public PrintStream getExceptionStream() {
		return exceptionStream;
	}
	
	public Object getFieldObjectByID(String tableID, String fieldID) {
		String id = tableID + "_" + fieldID;
		if (scriptBindings.containsKey(id)) {
			return scriptBindings.get(id);
		} else {
			JOptionPane.showMessageDialog(null, "Field object " + id + " is not found.");
			return null;
		}
	}
	
	public void evalScript(String scriptName, String scriptText) throws ScriptException {
		if (!scriptText.equals("")) {
			scriptNameRunning = scriptName;
			StringBuffer bf = new StringBuffer();
			bf.append(scriptText);
			bf.append(session_.getScriptFunctions());
			scriptEngine.eval(bf.toString(), scriptBindings);
		}
	}

	public void executeScript(String scriptText) {
		try {
			evalScript("Internal Script", scriptText);
		} catch (Exception e) {}
	}

	public XF200_PrimaryTable getPrimaryTable() {
		return primaryTable_;
	}

	public StringBuffer getProcessLog() {
		return processLog;
	}

	public Object getVariant(String variantID) {
		if (variantMap.containsKey(variantID)) {
			return variantMap.get(variantID);
		} else {
			return "";
		}
	}

	public void setVariant(String variantID, Object value) {
		variantMap.put(variantID, value);
	}

	public ArrayList<XF200_Field> getFieldList() {
		return fieldList;
	}

	public ArrayList<String> getKeyFieldList() {
		return primaryTable_.getKeyFieldList();
	}

	public ArrayList<XF200_ReferTable> getReferTableList() {
		return referTableList;
	}

	public boolean existsInColumnList(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < fieldList.size(); i++) {
			if (tableID.equals("")) {
				if (fieldList.get(i).getTableAlias().equals(tableAlias)
						&& fieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (fieldList.get(i).getTableID().equals(tableID)
						&& fieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (fieldList.get(i).getTableID().equals(tableID)
						&& fieldList.get(i).getTableAlias().equals(tableAlias)
						&& fieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public String getTableIDOfTableAlias(String tableAlias) {
		String tableID = tableAlias;
		XF200_ReferTable referTable;
		for (int j = 0; j < referTableList.size(); j++) {
			referTable = referTableList.get(j);
			referTable.getTableAlias();
			if (referTable.getTableAlias().equals(tableAlias)) {
				tableID = referTable.getTableID();
				break;
			}
		}
		return tableID;
	}

	public Object getValueOfFieldByName(String dataSourceName) {
		Object obj = null;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getDataSourceName().equals(dataSourceName)) {
				obj = fieldList.get(i).getInternalValue();
				break;
			}
		}
		return obj;
	}
}

class XF200_Field extends JPanel implements XFFieldScriptable {
	private static final long serialVersionUID = 1L;
	org.w3c.dom.Element functionFieldElement_ = null;
	org.w3c.dom.Element tableElement = null;
	private String fieldName = "";
	private String fieldRemarks = "";
	private String dataSourceName = "";
	private String tableID_ = "";
	private String tableAlias_ = "";
	private String fieldID_ = "";
	private String fieldCaption = "";
	private String dataType = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private String fieldOptions = "";
	private ArrayList<String> fieldOptionList;
	private String autoNumberKey = "";
	private int fieldRows = 1;
	private int tabIndex = -1;
	private JPanel jPanelField = new JPanel();
	private JLabel jLabelField = new JLabel();
	private JPanel jPanelFieldComment = null;
	private JLabel jLabelFieldComment = null;
	private XFEditableField component = null;
	private XFEditableField refferComponent = null;
	private JButton jButtonToRefferZipNo = null;
	private String byteaTypeFieldID = "";
	private String errorMessage = "";
	private String warningMessage = "";
	private boolean isNullable = true;
	private boolean isKey = false;
	private boolean isNoUpdate = false;
	private boolean isFieldOnPrimaryTable = false;
	private boolean isError = false;
	private boolean isVisibleOnPanel = true;
	private boolean isVirtualField = false;
	private boolean isHorizontal = false;
	private boolean isImage = false;
	private boolean isEnabled = true;
	private boolean isEditable = true;
	private boolean isEditableInitial = true;
	private boolean isAutoDetailRowNumber = false;
	private int positionMargin = 0;
	private ArrayList<String> additionalHiddenFieldList = new ArrayList<String>();
	private Color foreground = Color.black;
	private XF200 dialog_;
	
	public XF200_Field(org.w3c.dom.Element functionFieldElement, XF200 dialog, int tab){
		super();
		tabIndex = tab;
		dialog_ = dialog;
		functionFieldElement_ = functionFieldElement;
		fieldOptions = functionFieldElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);

		dataSourceName = functionFieldElement_.getAttribute("DataSource");
		StringTokenizer workTokenizer = new StringTokenizer(dataSourceName, "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog_.getTableIDOfTableAlias(tableAlias_);
		fieldID_ =workTokenizer.nextToken();
		String wrkStr;

		if (tableID_.equals(dialog_.getPrimaryTable().getTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnPrimaryTable = true;
			ArrayList<String> keyNameList = dialog_.getKeyFieldList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
			if (fieldID_.equals(dialog_.getPrimaryTable().getDetailRowNoID())) {
				isAutoDetailRowNumber = true;
				isEditable = false;
			}
		} else {
			isEditable = false;
		}

		if (fieldOptionList.contains("HORIZONTAL")) {
			isHorizontal = true;
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "HORIZONTAL");
		if (!wrkStr.equals("")) {
			isHorizontal = true;
			positionMargin = Integer.parseInt(wrkStr);
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "VERTICAL");
		if (!wrkStr.equals("")) {
			positionMargin = Integer.parseInt(wrkStr);
		}

		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		fieldName = workElement.getAttribute("Name");
		fieldRemarks = XFUtility.getLayoutedString(workElement.getAttribute("Remarks"), "<br>", dialog_.getSession().systemFont);
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = fieldName;
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}
		if (workElement.getAttribute("NoUpdate").equals("T")) {
			isNoUpdate = true;
		}
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = XFUtility.getCaptionValue(wrkStr, dialog_.getSession());
		}
		jLabelField.setText(fieldCaption);
		jLabelField.setFocusable(false);
		jLabelField.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelField.setVerticalAlignment(SwingConstants.TOP);
		jLabelField.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		if (fieldOptionList.contains("CAPTION_LENGTH_VARIABLE")) {
			FontMetrics metrics = jLabelField.getFontMetrics(jLabelField.getFont());
			jLabelField.setPreferredSize(new Dimension(metrics.stringWidth(fieldCaption), XFUtility.FIELD_UNIT_HEIGHT));
		} else {
			int captionWidth = XFUtility.DEFAULT_LABEL_WIDTH;
			wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION_WIDTH");
			if (!wrkStr.equals("")) {
				captionWidth = Integer.parseInt(wrkStr);
			}
			jLabelField.setPreferredSize(new Dimension(captionWidth, XFUtility.FIELD_UNIT_HEIGHT));
			XFUtility.adjustFontSizeToGetPreferredWidthOfLabel(jLabelField, captionWidth);
		}

		if (fieldOptionList.contains("PROMPT_LIST")) {
			isEditable = true;
			isNoUpdate = false;
			XF200_ReferTable referTable = null;
			ArrayList<XF200_ReferTable> referTableList = dialog_.getReferTableList();
			for (int i = 0; i < referTableList.size(); i++) {
				if (referTableList.get(i).getTableID().equals(tableID_)) {
					if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias_)) {
						referTable = referTableList.get(i);
						break;
					}
				}
			}
			component = new XF200_ComboBox(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, referTable, isNullable);
			component.setLocation(5, 0);
		} else {
			wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
			if (!wrkStr.equals("")) {
				isEditable = true;
				isNoUpdate = false;
				boolean isEditableInEditMode = false;
				if (this.isFieldOnPrimaryTable) {
					isEditableInEditMode = true;
				}
				component = new XF200_PromptCallField(functionFieldElement_, wrkStr, isEditableInEditMode, dialog_);
				component.setLocation(5, 0);
				wrkStr = XFUtility.getOptionValueWithKeyword(wrkStr, "PROMPT_CALL_TO_PUT");
				if (!wrkStr.equals("")) {
					workTokenizer = new StringTokenizer(wrkStr, ";" );
					while (workTokenizer.hasMoreTokens()) {
						additionalHiddenFieldList.add(workTokenizer.nextToken());
					}
				}
				wrkStr = XFUtility.getOptionValueWithKeyword(wrkStr, "PROMPT_CALL_TO_GET_TO");
				if (!wrkStr.equals("")) {
					workTokenizer = new StringTokenizer(wrkStr, ";" );
					while (workTokenizer.hasMoreTokens()) {
						additionalHiddenFieldList.add(workTokenizer.nextToken());
					}
				}
			} else {
				if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN").equals("")
						|| !XFUtility.getOptionValueWithKeyword(dataTypeOptions, "VALUES").equals("")) {
					if (isFieldOnPrimaryTable) {
						component = new XF200_ComboBox(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, null, isNullable);
					} else {
						component = new XF200_CodeText(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_);
					}
					component.setLocation(5, 0);
				} else {
					if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN").equals("")) {
						component = new XFCheckBox(dataTypeOptions);
						component.setLocation(5, 0);
					} else {
						if (dataType.equals("VARCHAR") || dataType.equals("LONG VARCHAR")) {
							component = new XFTextArea(dataSize, dataTypeOptions, fieldOptions, dialog_.getSession().systemFont);
							component.setLocation(5, 0);
						} else {
							if (dataTypeOptionList.contains("URL")) {
								component = new XFUrlField(dataSize, fieldOptions, dialog_.getSession().systemFont);
								component.setLocation(5, 0);
							} else {
								if (dataTypeOptionList.contains("IMAGE")) {
									component = new XFImageField(fieldOptions, dataSize, dialog_.getSession().getImageFileFolder(), dialog_.getSession().systemFont);
									component.setLocation(5, 0);
									isImage = true;
								} else {
									if (dataType.equals("DATE")) {
										component = new XFDateField(dialog_.getSession());
										component.setLocation(5, 0);
									} else {
										if (dataTypeOptionList.contains("YMONTH")) {
											component = new XFYMonthBox(dialog_.getSession());
											component.setLocation(5, 0);
										} else {
											if (dataTypeOptionList.contains("MSEQ")) {
												component = new XFMSeqBox(dialog_.getSession());
												component.setLocation(5, 0);
											} else {
												if (dataTypeOptionList.contains("FYEAR")) {
													component = new XFFYearBox(dialog_.getSession());
													component.setLocation(5, 0);
												} else {
													if (this.getBasicType().equals("BYTEA")) {
														component = new XFByteaField(byteaTypeFieldID, fieldOptions, dialog_.getSession());
														component.setLocation(5, 0);
													} else {
														if (this.getBasicType().equals("BINARY")) {
															component = new XFBlobField(fieldOptions);
															component.setLocation(5, 0);
														} else {
															if (isAutoDetailRowNumber) {
																if (dataSize < 5) { 
																	component = new XFTextField("STRING", 5, 0, "", "", dialog_.getSession().systemFont);
																} else {
																	component = new XFTextField("STRING", dataSize, 0, "", "", dialog_.getSession().systemFont);
																}
																component.setLocation(5, 0);
															} else {
																component = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions, dialog_.getSession().systemFont);
																component.setLocation(5, 0);
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		fieldRows = component.getRows();

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "WIDTH");
		if (!wrkStr.equals("")) {
			component.setWidth(Integer.parseInt(wrkStr));
		}

		jPanelField.setLayout(null);
		jPanelField.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		jPanelField.add((JComponent)component);
		((JComponent)component).addKeyListener(new XF200_Component_keyAdapter(dialog_));

		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		this.add(jPanelField, BorderLayout.CENTER);
		if (fieldOptionList.contains("NO_CAPTION")) {
			this.setPreferredSize(new Dimension(component.getWidth() + 10, component.getHeight()));
		} else {
			this.setPreferredSize(new Dimension(component.getWidth() + jLabelField.getPreferredSize().width + 10, component.getHeight()));
			this.add(jLabelField, BorderLayout.WEST);
		}

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "COMMENT");
		if (!wrkStr.equals("")) {
			jLabelFieldComment = new JLabel();
			jLabelFieldComment.setText(wrkStr);
			//jLabelFieldComment.setForeground(Color.blue);
			jLabelFieldComment.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
			FontMetrics metrics = jLabelFieldComment.getFontMetrics(jLabelFieldComment.getFont());
			this.setPreferredSize(new Dimension(this.getPreferredSize().width + metrics.stringWidth(wrkStr.replaceAll("<.+?>", "")) + 6, this.getPreferredSize().height));
			jLabelFieldComment.setText(wrkStr);
		}

		if (dataTypeOptionList.contains("ZIPADRS")) {
			jButtonToRefferZipNo = new JButton();
			jButtonToRefferZipNo.setText("<");
			jButtonToRefferZipNo.setFont(new java.awt.Font("SansSerif", 0, 9));
			jButtonToRefferZipNo.setPreferredSize(new Dimension(37, this.getPreferredSize().height));
			jButtonToRefferZipNo.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						setCursor(new Cursor(Cursor.WAIT_CURSOR));
						String zipNo = refferComponent.getExternalValue().toString();
						if (refferComponent != null && !zipNo.equals("")) {
							String address = dialog_.getSession().getAddressFromZipNo(zipNo);
							if (!address.equals("")) {
								setValue(address);
							}
						}
					} finally {
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				}
			});
			jButtonToRefferZipNo.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
				    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
						if (e.getKeyCode() == KeyEvent.VK_ENTER) {
							jButtonToRefferZipNo.doClick();
						}
					}
				} 
			});
			this.setPreferredSize(new Dimension(this.getPreferredSize().width + 34, this.getPreferredSize().height));
		}
		if (jButtonToRefferZipNo != null || jLabelFieldComment != null) {
			jPanelFieldComment = new JPanel();
			jPanelFieldComment.setLayout(new BorderLayout());
			int width = 2;
			if (jButtonToRefferZipNo != null) {
				width = width + jButtonToRefferZipNo.getPreferredSize().width;
			}
			if (jLabelFieldComment != null) {
				width = width + jLabelFieldComment.getPreferredSize().width + 3;
				jPanelFieldComment.add(jLabelFieldComment, BorderLayout.CENTER);
			}
			jPanelFieldComment.setPreferredSize(new Dimension(width, component.getHeight()));
			this.add(jPanelFieldComment, BorderLayout.EAST);
		}

		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			autoNumberKey = wrkStr;
			isEditable = false;
			this.setEditable(false);
		}
		isEditableInitial = isEditable;

		if (decimalSize > 0) {
			wrkStr = "<html>" + fieldName + " " + dataSourceName + " (" + dataSize + "," + decimalSize + ")<br>" + fieldRemarks;
		} else {
			wrkStr = "<html>" + fieldName + " " + dataSourceName + " (" + dataSize + ")<br>" + fieldRemarks;
		}
		this.setToolTipText(wrkStr);
		component.setToolTipText(wrkStr);
	}

	public XF200_Field(String tableID, String tableAlias, String fieldID, XF200 dialog){
		super();
		String wrkStr;
		functionFieldElement_ = null;
		fieldOptions = "";
		fieldOptionList = new ArrayList<String>();
		isVisibleOnPanel = false;

		tableID_ = tableID;
		fieldID_ = fieldID;
		dialog_ = dialog;
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}
		dataSourceName = tableAlias_ + "." + fieldID_;

		if (tableID_.equals(dialog_.getPrimaryTable().getTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnPrimaryTable = true;
			ArrayList<String> keyNameList = dialog_.getKeyFieldList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
			if (fieldID_.equals(dialog_.getPrimaryTable().getDetailRowNoID())) {
				isAutoDetailRowNumber = true;
			}
		} else {
			isEditable = false;
		}

		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		fieldName = workElement.getAttribute("Name");
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}
		if (workElement.getAttribute("NoUpdate").equals("T")) {
			isNoUpdate = true;
		}
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();

		component = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions, dialog_.getSession().systemFont);
		component.setLocation(5, 0);

		fieldRows = component.getRows();
		jPanelField.setLayout(null);
		jPanelField.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		jPanelField.add((JComponent)component);

		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(component.getWidth() + 130, component.getHeight()));
		this.add(jPanelField, BorderLayout.CENTER);

		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			autoNumberKey = wrkStr;
			isEditable = false;
			this.setEnabled(false);
		}
		isEditableInitial = isEditable;
	}
	
	public ArrayList<String> getAdditionalHiddenFieldList() {
		return additionalHiddenFieldList;
	}

	public XFEditableField getComponent() {
		return component;
	}

	public void setRefferComponent(XFEditableField compo) {
		refferComponent = compo;
	}
	
	public boolean isPromptListField(){
		return fieldOptionList.contains("PROMPT_LIST");
	}
	
	public void requestFocus(){
		component.requestFocus();
	}

	public String getDataSourceName(){
		return dataSourceName;
	}

	public String getFieldID(){
		return fieldID_;
	}

	public String getByteaTypeFieldID(){
		return byteaTypeFieldID;
	}

	public String getTableAlias(){
		return tableAlias_;
	}

	public String getDataSourceID(){
		return tableAlias_ + "_" + fieldID_;
	}

	public String getCaption(){
		return fieldCaption;
	}

	public int getDecimalSize(){
		return decimalSize;
	}

	public String getTableID(){
		return tableID_;
	}

	public boolean isAutoDetailRowNumber(){
		return isAutoDetailRowNumber;
	}

	public boolean isNullable(){
		return isNullable;
	}

	public boolean isVisibleOnPanel(){
		return isVisibleOnPanel;
	}

	public boolean isVirtualField(){
		return isVirtualField;
	}

	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}

	public boolean isHorizontal(){
		return isHorizontal;
	}

	public int getPositionMargin(){
		return positionMargin;
	}

	public boolean isImage(){
		return isImage;
	}

	public int getRows(){
		return fieldRows;
	}

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}
	
	public ArrayList<String> getTypeOptionList() {
		return dataTypeOptionList;
	}
	
	public ArrayList<String> getFieldOptionList() {
		return fieldOptionList;
	}

	public boolean isFieldOnPrimaryTable(){
		return isFieldOnPrimaryTable;
	}

	public boolean isKey(){
		return isKey;
	}

	public void setError(boolean error) {
		if (error) {
			isError = true;
			if (component.isEditable()) {
				component.setBackground(XFUtility.ERROR_COLOR);
				dialog_.setFirstErrorTabIndex(tabIndex);
			}
		} else {
			isError = false;
			errorMessage = "";
			warningMessage = "";
			if (component.isEditable()) {
				component.setBackground(XFUtility.ACTIVE_COLOR);
			} else {
				component.setBackground(XFUtility.INACTIVE_COLOR);
			}
		}
	}

//	public void setKeyDependent(boolean keyDependent){
//		this.isKeyDependent = keyDependent;
//	}

	public void setEditable(boolean editable){
		this.isEditable = editable;
	}
	
	public void setValueList(String[] valueList) {
		if (component instanceof XFTextField) {
			((XFTextField)component).setValueList(valueList);
		}
	}
	
	public String[] getValueList() {
		String[] valueList = null;
		if (component instanceof XFTextField) {
			valueList = ((XFTextField)component).getValueList();
		}
		return valueList;
	}
	
	public void initEditable() {
		isEditable = isEditableInitial;
	}

	public boolean isComponentFocusable() {
		return component.isComponentFocusable();
	}

	public boolean isEnabled() {
		return this.isEnabled;
	}

	public boolean isNoUpdate() {
		return this.isNoUpdate;
	}

	public void setEnabled(boolean isEnabled){
		this.isEnabled = isEnabled;
		jLabelField.setEnabled(isEnabled);
		component.setEnabled(isEnabled);
	}

	public boolean isEditable() {
		return this.isEditable;
	}

	public void setComponentEditable(boolean editable){
		component.setEditable(editable);
		if (component.isEditable()) {
			component.setFocusable(true);
			if (this.isError()) {
				component.setBackground(XFUtility.ERROR_COLOR);
			} else {
				component.setBackground(XFUtility.ACTIVE_COLOR);
			}
			if (jButtonToRefferZipNo != null) {
				jPanelFieldComment.add(jButtonToRefferZipNo, BorderLayout.WEST);
				jPanelFieldComment.repaint();
			}
		} else {
			component.setFocusable(false);
			component.setBackground(XFUtility.INACTIVE_COLOR);
			if (jButtonToRefferZipNo != null) {
				jPanelFieldComment.remove(jButtonToRefferZipNo);
				jPanelFieldComment.repaint();
			}
		}
	}

	public void setEditMode(String mode){
		this.setComponentEditable(false);
		if (mode.equals("ADD") && this.isEditable && !isAutoDetailRowNumber()) {
			this.setComponentEditable(true);
		}
		if (mode.equals("COPY") && this.isEditable && !isAutoDetailRowNumber()) {
			this.setComponentEditable(true);
		}
		if (mode.equals("EDIT") && this.isEditable && !this.isNoUpdate) {
			if (!this.isKey) {
				this.setComponentEditable(true);
			}
		}
	}

	public void setValueOfResultSet(XFTableOperator operator){
		try {
			if (!this.isVirtualField) {
				String basicType = this.getBasicType();
				if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
					String value = operator.getValueOf(this.getFieldID()).toString();
					if (this.isFieldOnPrimaryTable) {
						if (value == null) {
							component.setValue("0");
						} else {
							component.setValue(value);
						}
					} else {
						if (basicType.equals("INTEGER")) {
							if (value == null || value.equals("")) {
								component.setValue("");
							} else {
								int pos = value.indexOf(".");
								if (pos >= 0) {
									value = value.substring(0, pos);
								}
								component.setValue(Long.parseLong(value));
							}
						}
						if (basicType.equals("FLOAT")) {
							if (value == null || value.equals("")) {
								component.setValue("");
							} else {
								component.setValue(Double.parseDouble(value));
							}
						}
					}
				}
				if (basicType.equals("STRING") || basicType.equals("TIME") || basicType.equals("DATETIME")) {
					Object value = operator.getValueOf(this.getFieldID());
					if (value == null) {
						component.setValue("");
					} else {
						component.setValue(value.toString().trim());
					}
				}
				if (basicType.equals("DATE") || basicType.equals("BYTEA") || basicType.equals("BINARY")) {
					component.setValue(operator.getValueOf(this.getFieldID()));
				}
				component.setOldValue(component.getInternalValue());
			}
		} catch(Exception e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public void setValue(Object object){
		XFUtility.setValueToEditableField(this.getBasicType(), object, component);
	}
	
	public void setupByteaTypeField(ArrayList<XF200_Field> fieldList) {
		if (isVisibleOnPanel) {
			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).getFieldID().equals(byteaTypeFieldID)) {
					((XFByteaField)component).setTypeField((XFFieldScriptable)fieldList.get(i));
					break;
				}
			}
		}
	}
	
	public boolean isControledByFieldOtherThan(XFEditableField component) {
		boolean result = false;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).getComponent() == component) {
				break;
			} else {
				if (dialog_.getFieldList().get(i).getComponent() instanceof XF200_ComboBox) {
					XF200_ComboBox comboBox = (XF200_ComboBox)dialog_.getFieldList().get(i).getComponent();
					if (comboBox.getKeyFieldList().contains(dataSourceName)) {
						result = true;
						break;
					}
				}
				if (dialog_.getFieldList().get(i).getComponent() instanceof XF200_PromptCallField) {
					XF200_PromptCallField promptField = (XF200_PromptCallField)dialog_.getFieldList().get(i).getComponent();
					if (promptField.getControlFieldList().contains(dataSourceName)) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	public Object getNullValue(){
		if (component instanceof XFCheckBox) {
			return ((XFCheckBox)component).getFalseValue(); 
		} else {
			return XFUtility.getNullValueOfBasicType(this.getBasicType());
		}
	}

	public boolean isNull(){
		return XFUtility.isNullValue(this.getBasicType(), component.getInternalValue());
	}

	public Object getInternalValue(){
		Object returnObj = null;
		String basicType = this.getBasicType();
		if (basicType.equals("INTEGER") || basicType.equals("FLOAT") || basicType.equals("STRING")) {
			String value = (String)component.getInternalValue();
			if (value == null) {
				value = "";
			}
			returnObj = value;
		}
		if (basicType.equals("DATE") || basicType.equals("DATETIME") || basicType.equals("TIME")) {
			returnObj = (String)component.getInternalValue();
		}
		if (basicType.equals("BYTEA")) {
			returnObj = component.getInternalValue();
		}
		return returnObj;
	}

	public void setOldValue(Object object){
		XFUtility.setOldValueToEditableField(this.getBasicType(), object, component);
	}

	public Object getOldValue(){
		Object returnObj = null;
		if (this.getBasicType().equals("INTEGER")) {
			returnObj = Long.parseLong((String)component.getOldValue());
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				returnObj = Double.parseDouble((String)component.getOldValue());
			} else {
				if (component.getOldValue() == null) {
					returnObj = "";
				} else {
					returnObj = (String)component.getOldValue();
				}
			}
		}
		return returnObj;
	}
	
	public boolean isValueChanged() {
		return !this.getValue().equals(this.getOldValue());
	}

	public Object getExternalValue(){
		Object returnObj = null;
		String basicType = this.getBasicType();
		if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
			String value = (String)component.getExternalValue();
			if (value == null) {
				value = "0";
			}
			returnObj = value;
		}
		if (basicType.equals("STRING")) {
			String value = (String)component.getExternalValue();
			if (value == null) {
				value = "";
			}
			returnObj = value;
		}
		if (basicType.equals("DATE")
				|| basicType.equals("DATETIME")
				|| basicType.equals("TIME")
				|| basicType.equals("BYTEA")) {
			returnObj = (String)component.getExternalValue();
		}
		return returnObj;
	}

	public String getAutoNumber() {
		String value = "";
		if (!autoNumberKey.equals("") && isFieldOnPrimaryTable) {
			value = dialog_.getSession().getNextNumber(autoNumberKey);
		}
		return value;
	}

	public boolean isAutoNumberField() {
		return !autoNumberKey.equals("") && isFieldOnPrimaryTable;
	}

	public boolean isError() {
		return isError;
	}

	public boolean isNullError(String mode){
		String basicType = this.getBasicType();
		boolean isError = false;

		if (this.isEditable() && this.isVisibleOnPanel && this.isFieldOnPrimaryTable && !this.isError) {
			if (mode.equals("EDIT") || mode.equals("ADD") || mode.equals("COPY")) {

				if (basicType.equals("INTEGER")) {
					if (!this.isNullable) {
						long value = Long.parseLong((String)component.getInternalValue());
						if (value == 0) {
							isError = true;
						}
					}
				}
				if (basicType.equals("FLOAT")) {
					if (!this.isNullable) {
						double value = Double.parseDouble((String)component.getInternalValue());
						if (value == 0) {
							isError = true;
						}
					}
				}
				if (basicType.equals("DATE") || basicType.equals("DATETIME")) {
					if (!this.isNullable) {
						String strDate = (String)component.getInternalValue();
						if (strDate == null || strDate.equals("")) {
							isError = true;
						}
					}
				}
				if (basicType.equals("BYTEA")) {
					if (!this.isNullable) {
						Object value = component.getInternalValue();
						if (value == null || value.toString().equals("")) {
							isError = true;
						}
					}
				}
				if (basicType.equals("STRING")) {
					String strWrk = (String)component.getInternalValue();
					if (!this.isNullable) {
						if (strWrk.equals("")) {
							isError = true;
						}
					}
					if (dataTypeOptionList.contains("YMONTH") && strWrk.length() >= 0 && strWrk.length() < 6) {
						isError = true;
					}
					if (dataTypeOptionList.contains("FYEAR") && strWrk.length() >= 0 && strWrk.length() < 4) {
						isError = true;
					}
				}
				if (isError) {
					this.setError(XFUtility.RESOURCE.getString("FunctionError16"));
				}
			}
		}
		return isError;
	}

	public boolean isTooLong(String mode){
		boolean isError = false;
		if (this.isEditable() && this.isVisibleOnPanel && this.isFieldOnPrimaryTable && !this.isError) {
			if (mode.equals("EDIT") || mode.equals("ADD") || mode.equals("COPY")) {
				if (dataType.equals("CHAR")) {
					String strWrk = (String)component.getInternalValue();
					if (strWrk.length() > this.dataSize) {
						isError = true;
						this.setError(XFUtility.RESOURCE.getString("FunctionError55"));
					}
				}
			}
		}
		return isError;
	}

	public Object getValue() {
		Object returnObj = null;
		if (this.getBasicType().equals("INTEGER")) {
			try {
				returnObj = Long.parseLong((String)component.getInternalValue());
			} catch (NumberFormatException e) {
				returnObj = 0;
			}
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				try {
					returnObj = Double.parseDouble((String)component.getInternalValue());
				} catch (NumberFormatException e) {
					returnObj = 0.0;
				}
			} else {
				if (this.getBasicType().equals("BYTEA")) {
					returnObj = component.getInternalValue();
				} else {
					if (component.getInternalValue() == null) {
						returnObj = "";
					} else {
						returnObj = (String)component.getInternalValue();
					}
				}
			}
		}
		return returnObj;
	}

	public void setError(String message) {
		if (!message.equals("")) {
			setError(true);
			if (errorMessage.equals("")) {
				errorMessage = message;
			} else {
				if (!errorMessage.contains(message)) {
					errorMessage = errorMessage + " " +message;
				}
			}
		}
	}

	public String getError() {
		return errorMessage;
	}

	public void setWarning(String message) {
		if (!message.equals("")) {
			if (warningMessage.equals("")) {
				warningMessage = message;
			} else {
				if (!warningMessage.contains(message)) {
					warningMessage = warningMessage + " " + message;
				}
			}
		}
	}

	public String getWarning() {
		return warningMessage;
	}

	public int getTabIndex() {
		return tabIndex;
	}

	public void setColor(String color) {
		foreground = XFUtility.convertStringToColor(color);
		component.setForeground(foreground);
	}

	public String getColor() {
		return XFUtility.convertColorToString(foreground);
	}
}

class XF200_ComboBox extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private String dataTypeOptions_ = "";
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private int rows_ = 1;
	private String listType = "";
	private ArrayList<String> kubunKeyValueList = new ArrayList<String>();
	private ArrayList<XFHashMap> tableKeyValuesList = new ArrayList<XFHashMap>();
	private JTextField jTextField = new JTextField();
	private JComboBox jComboBox = new JComboBox();
	private boolean isEditable = true;
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private XF200_ReferTable referTable_ = null;
	private XF200 dialog_;
	private String oldValue = "";
	private int indexOfField = -1;

	public XF200_ComboBox(String dataSourceName, String dataTypeOptions, XF200 dialog, XF200_ReferTable chainTable, boolean isNullable){
		super();

		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;
		int fieldWidth = 0;
		String wrk = "";
		String strWrk;

		dataTypeOptions_ = dataTypeOptions;
		workTokenizer = new StringTokenizer(dataSourceName, "." );
		tableAlias = workTokenizer.nextToken();
		tableID = tableAlias;
		referTable_ = chainTable;
		if (referTable_ != null && referTable_.getTableAlias().equals(tableAlias)) {
			tableID = referTable_.getTableID();
		}
		fieldID =workTokenizer.nextToken();
		dialog_ = dialog;

		indexOfField = dialog_.getFieldList().size();
		jTextField.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jTextField.setEditable(false);
		jTextField.setFocusable(false);
		jTextField.setBorder(null);
		FontMetrics metrics = jTextField.getFontMetrics(jTextField.getFont());
		jComboBox.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jComboBox.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
				if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jComboBox.showPopup();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !jComboBox.isPopupVisible()) {
					requestFocus();
					dispatchEvent(e);
				}
			}
		});
		jComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (referTable_ != null && isEditable && jComboBox.getSelectedIndex() >= 0 && dialog_.isVisible()) {
					referTable_.setKeyFieldValues(tableKeyValuesList.get(jComboBox.getSelectedIndex()));
				}
			}
		});
		jComboBox.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				if (referTable_ != null && isEditable && jComboBox.getSelectedIndex() >= 0) {
					referTable_.setKeyFieldValues(tableKeyValuesList.get(jComboBox.getSelectedIndex()));
					XF200_ComboBox comboBoxField;
					for (int i = 0; i < dialog_.getFieldList().size(); i++) {
						if (i > indexOfField) {
							if (dialog_.getFieldList().get(i).getComponent() instanceof XF200_ComboBox) {
								comboBoxField = (XF200_ComboBox)dialog_.getFieldList().get(i).getComponent();
								comboBoxField.setupRecordList();
							}
						}
					}
				}
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				setupRecordList();
			}
		});
		jComboBox.setBorder(null);

		strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "VALUES");
		if (!strWrk.equals("")) {
			listType = "VALUES_LIST";
			if (isNullable) {
				jComboBox.addItem("");
			}
			workTokenizer = new StringTokenizer(strWrk, ";" );
			while (workTokenizer.hasMoreTokens()) {
				wrk = workTokenizer.nextToken();
				jComboBox.addItem(wrk);
				if (metrics.stringWidth(wrk) > fieldWidth) {
					fieldWidth = metrics.stringWidth(wrk);
				}
			}
			fieldWidth = fieldWidth + 30;
		} else {
			strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "KUBUN");
			if (!strWrk.equals("")) {
				listType = "KUBUN_LIST";
				if (isNullable) {
					kubunKeyValueList.add("");
					jComboBox.addItem("");
				}
				try {
					String sql = "select * from " + dialog_.getSession().getTableNameOfUserVariants() + " where IDUSERKUBUN = '" + strWrk + "' order by SQLIST";
					XFTableOperator operator = dialog_.createTableOperator(sql, true);
					while (operator.next()) {
						kubunKeyValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
						wrk = operator.getValueOf("TXUSERKUBUN").toString().trim();
						jComboBox.addItem(wrk);
						if (metrics.stringWidth(wrk) > fieldWidth) {
							fieldWidth = metrics.stringWidth(wrk);
						}
					}
					fieldWidth = fieldWidth + 30;
					if (jComboBox.getItemCount() == 0) {
						JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError24") + dataSourceName + XFUtility.RESOURCE.getString("FunctionError25"));
					}
				} catch(Exception e) {
					e.printStackTrace(dialog_.getExceptionStream());
					dialog_.setErrorAndCloseFunction();
				}
			} else {
				if (referTable_ != null) {
					listType = "RECORDS_LIST";
					keyFieldList = referTable_.getKeyFieldIDList();
					workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
					if (workElement == null) {
						JOptionPane.showMessageDialog(this, tableID + "." + fieldID + XFUtility.RESOURCE.getString("FunctionError11"));
					}
					ArrayList<String> workDataTypeOptionList = XFUtility.getOptionList(workElement.getAttribute("TypeOptions"));
					int dataSize = Integer.parseInt(workElement.getAttribute("Size"));
					if (workDataTypeOptionList.contains("KANJI")) {
						fieldWidth = dataSize * XFUtility.FONT_SIZE + 20;
					} else {
						fieldWidth = dataSize * (XFUtility.FONT_SIZE/2 + 2) + 30;
					}
					if (fieldWidth > 800) {
						fieldWidth = 800;
					}
				}
			}
		}

		this.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		this.setSize(new Dimension(fieldWidth, XFUtility.FIELD_UNIT_HEIGHT));
		this.setLayout(new BorderLayout());
		this.add(jComboBox, BorderLayout.CENTER);
	}

	public void setupRecordList() {
		if (referTable_ != null && listType.equals("RECORDS_LIST")) {

			String selectedItemValue = "";
			if (jComboBox.getSelectedIndex() >= 0) {
				selectedItemValue = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
			}

			tableKeyValuesList.clear();
			jComboBox.removeAllItems();

			//boolean blankItemRequired = false;
			XFHashMap blankKeyValues = new XFHashMap();
			for (int i = 0; i < referTable_.getWithKeyFieldIDList().size(); i++) {
				for (int j = 0; j < dialog_.getFieldList().size(); j++) {
					if (referTable_.getWithKeyFieldIDList().get(i).equals(dialog_.getFieldList().get(j).getTableAlias() + "." + dialog_.getFieldList().get(j).getFieldID())) {
						if (dialog_.getFieldList().get(j).isNullable()) {
							//blankItemRequired = true;
							if (dialog_.getFieldList().get(j).isVisibleOnPanel()
								|| dialog_.getFieldList().get(j).isControledByFieldOtherThan(this)) {
								blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getFieldList().get(j).getValue());
							} else {
								blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getFieldList().get(j).getNullValue());
							}
						} else {
							blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getFieldList().get(j).getValue());
						}
					}
				}
			}
			//if (blankItemRequired) {
				tableKeyValuesList.add(blankKeyValues);
				jComboBox.addItem("");
			//}

			try {
				XFHashMap keyValues;
				XFTableOperator operator = dialog_.createTableOperator(referTable_.getSelectSQL(true));
				while (operator.next()) {
					keyValues = new XFHashMap();
					for (int i = 0; i < keyFieldList.size(); i++) {
						keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), operator.getValueOf(keyFieldList.get(i)).toString());
					}
					tableKeyValuesList.add(keyValues);
					jComboBox.addItem(operator.getValueOf(fieldID).toString().trim());
				}
			} catch(Exception e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}

			jComboBox.setSelectedItem(selectedItemValue);
		}
	}
	
	public ArrayList<String> getKeyFieldList() {
		return keyFieldList;
	}

	public void setFollowingField(XFEditableField field) {
	}

	public void setEditable(boolean editable) {
		this.removeAll();
		if (editable) {
			this.add(jComboBox, BorderLayout.CENTER);
		} else {
			this.add(jTextField, BorderLayout.CENTER);
		}
		isEditable = editable;
	}

	public void setWidth(int width) {
		this.setSize(new Dimension(width, XFUtility.FIELD_UNIT_HEIGHT));
	}

	public void setToolTipText(String text) {
		jComboBox.setToolTipText(text);
		jTextField.setToolTipText(text);
	}

	public Object getInternalValue() {
		String value = "";
		if (jComboBox.getSelectedIndex() >= 0) {
			if (listType.equals("VALUES_LIST")) {
				value = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
			}
			if (listType.equals("KUBUN_LIST")) {
				value = kubunKeyValueList.get(jComboBox.getSelectedIndex());
			}
			if (listType.equals("RECORDS_LIST")) {
				value = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
			}
		}
		return value;
	}

	public Object getExternalValue() {
		return jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
	}

	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public boolean isComponentFocusable() {
		return isEditable;
	}

	public boolean isFocusable() {
		return false;
	}

	public void setSelectedIndex(int index) {
		if (jComboBox.getItemCount() > 0) {
			jComboBox.setSelectedIndex(index);
		}
	}

	public void setValue(Object obj) {
		String value = obj.toString();
		value = value.trim();
		if (listType.equals("VALUES_LIST")) {
			for (int i = 0; i < jComboBox.getItemCount(); i++) {
				if (jComboBox.getItemAt(i).toString().equals(value)) {
					jComboBox.setSelectedIndex(i);
					break;
				}
			}
		}
		if (listType.equals("KUBUN_LIST")) {
			for (int i = 0; i < kubunKeyValueList.size(); i++) {
				if (kubunKeyValueList.get(i).equals(value)) {
					jComboBox.setSelectedIndex(i);
					break;
				}
			}
		}
		if (listType.equals("RECORDS_LIST")) {
			setupRecordList();
			if (jComboBox.getItemCount() > 0) {
				if (value == null || value.equals("")) {
				} else {
					for (int i = 0; i < jComboBox.getItemCount(); i++) {
						if (jComboBox.getItemAt(i).toString().equals(value)) {
							if (jComboBox.getSelectedIndex() != i) {
								jComboBox.setSelectedIndex(i);
							}
							break;
						}
					}
				}
			}
		}
		if (jComboBox.getSelectedIndex() >= 0) {
			jTextField.setText(jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString());
		}
	}

	public void setBackground(Color color) {
		if (jComboBox != null) {
			jComboBox.setBackground(color);
		}
	}

	public void setForeground(Color color) {
		if (jTextField != null) {
			jTextField.setForeground(color);
		}
		if (jComboBox != null) {
			jComboBox.setForeground(color);
		}
	}

	public int getRows() {
		return rows_;
	}
}

class XF200_CodeText extends JTextField implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private String dataTypeOptions_ = "";
	private int fieldWidth = 0;
	private ArrayList<String> codeValueList = new ArrayList<String>();
	private ArrayList<String> textValueList = new ArrayList<String>();
	private XF200 dialog_;
	
	public XF200_CodeText(String dataSourceName, String dataTypeOptions, XF200 dialog){
		super();
		StringTokenizer workTokenizer;
		String wrk = "";
		String strWrk;
		dataTypeOptions_ = dataTypeOptions;
		dialog_ = dialog;
		this.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		this.setEditable(false);
		this.setFocusable(false);
		FontMetrics metrics = this.getFontMetrics(this.getFont());
		strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "VALUES");
		if (!strWrk.equals("")) {
			codeValueList.add("");
			textValueList.add("");
			workTokenizer = new StringTokenizer(strWrk, ";" );
			while (workTokenizer.hasMoreTokens()) {
				wrk = workTokenizer.nextToken();
				if (metrics.stringWidth(wrk) > fieldWidth) {
					fieldWidth = metrics.stringWidth(wrk) + 12;
				}
				codeValueList.add(wrk);
				textValueList.add(wrk);
			}
		} else {
			strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "KUBUN");
			if (!strWrk.equals("")) {
				codeValueList.add("");
				textValueList.add("");
				try {
					String sql = "select * from " + dialog_.getSession().getTableNameOfUserVariants() + " where IDUSERKUBUN = '" + strWrk + "' order by SQLIST";
					XFTableOperator operator = dialog_.createTableOperator(sql, true);
					while (operator.next()) {
						codeValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
						wrk = operator.getValueOf("TXUSERKUBUN").toString().trim();
						textValueList.add(wrk);
						if (metrics.stringWidth(wrk) > fieldWidth) {
							fieldWidth = metrics.stringWidth(wrk);
						}
					}
					fieldWidth = fieldWidth + 10;
					if (codeValueList.size() == 0) {
						JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError24") + dataSourceName + XFUtility.RESOURCE.getString("FunctionError25"));
					}
				} catch(Exception e) {
					e.printStackTrace(dialog_.getExceptionStream());
					dialog_.setErrorAndCloseFunction();
				}
			}
		}
		this.setSize(new Dimension(fieldWidth, XFUtility.FIELD_UNIT_HEIGHT));
		this.setLayout(new BorderLayout());
	}
	
	public void setFollowingField(XFEditableField field) {
	}
	
	public void setWidth(int width) {
		fieldWidth = width;
		this.setSize(new Dimension(fieldWidth, XFUtility.FIELD_UNIT_HEIGHT));
	}

	public Object getInternalValue() {
		return codeValueList.get(textValueList.indexOf(this.getText()));
	}

	public Object getExternalValue() {
		return this.getText();
	}
	
	public void setOldValue(Object obj) {
	}

	public Object getOldValue() {
		return codeValueList.get(textValueList.indexOf(this.getText()));
	}

	public boolean isComponentFocusable() {
		return false;
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		value = value.trim();
		this.setText(textValueList.get(codeValueList.indexOf(value)));
	}

	public int getRows() {
		return 1;
	}
}

class XF200_PromptCallField extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private String dataType = "";
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private int rows_ = 1;
	private XFTextField xFTextField;
	private JButton jButton = new JButton();
	private boolean isEditable = true;
	private boolean isEditableInEditMode_ = false;
    private XF200 dialog_;
    private String functionID_;
    private org.w3c.dom.Element fieldElement_;
    private ArrayList<XF200_ReferTable> referTableList_;
    private String oldValue = "";
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();
    private String kubunValue = "";
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();

	public XF200_PromptCallField(org.w3c.dom.Element fieldElement, String functionID, boolean isEditableInEditMode, XF200 dialog){
		super();
		fieldElement_ = fieldElement;
		functionID_ = functionID;
		isEditableInEditMode_ = isEditableInEditMode;
		dialog_ = dialog;

		String fieldOptions = fieldElement_.getAttribute("FieldOptions");
		StringTokenizer workTokenizer = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		fieldID =workTokenizer.nextToken();
		tableID = tableAlias;
		referTableList_ = dialog_.getReferTableList();
		for (int i = 0; i < referTableList_.size(); i++) {
			if (referTableList_.get(i).getTableAlias().equals(tableAlias)) {
				tableID = referTableList_.get(i).getTableID();
				break;
			}
		}

		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID + "." + fieldID + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		int dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (dataSize > 50) {
			dataSize = 50;
		}
		int decimalSize = 0;
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}

		xFTextField = new XFTextField(XFUtility.getBasicTypeOf(dataType), dataSize, decimalSize, dataTypeOptions, fieldOptions, dialog_.getSession().systemFont);
		xFTextField.setLocation(5, 0);

		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_PUT");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToPutList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_PUT_TO");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToPutToList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_GET");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToGetList_.add(workTokenizer.nextToken());
			}
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_GET_TO");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer.hasMoreTokens()) {
				fieldsToGetToList_.add(workTokenizer.nextToken());
			}
		}

		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			JLabel jLabel = new JLabel();
			FontMetrics metrics = jLabel.getFontMetrics(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
			int fieldWidth = xFTextField.getWidth();
			String wrk = "";
			String userVariantsTableID = dialog_.getSession().getTableNameOfUserVariants();
			String sql = "select * from " + userVariantsTableID + " where IDUSERKUBUN = '" + wrkStr + "' order by SQLIST";
			XFTableOperator operator = dialog_.createTableOperator(sql);
			try {
				while (operator.next()) {
					kubunValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
					wrk = operator.getValueOf("TXUSERKUBUN").toString().trim();
					if (metrics.stringWidth(wrk) + 10 > fieldWidth) {
						fieldWidth = metrics.stringWidth(wrk) + 10;
					}
					kubunTextList.add(wrk);
				}
			} catch (Exception e1) {
			}
			xFTextField.setWidth(fieldWidth);
			isEditableInEditMode_ = false;
		}

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XF200.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value;
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));

					HashMap<String, Object> fieldValuesMap = new HashMap<String, Object>();
					for (int i = 0; i < fieldsToPutList_.size(); i++) {
						value = dialog_.getValueOfFieldByName(fieldsToPutList_.get(i));
						if (value == null) {
							JOptionPane.showMessageDialog(null, "Unable to send the value of field " + fieldsToPutList_.get(i));
						} else {
							fieldValuesMap.put(fieldsToPutToList_.get(i), value);
						}
					}
					fieldValuesMap.put("RETURN_TO", dialog_.getFunctionID());

					HashMap<String, Object> returnMap = dialog_.getSession().executeFunction(functionID_, fieldValuesMap);
					if (returnMap.get("RETURN_TO") != null) {
						dialog_.returnTo(returnMap.get("RETURN_TO").toString());
					}
					if (returnMap.get("RETURN_CODE").equals("00")) {
						HashMap<String, Object> fieldsToGetMap = new HashMap<String, Object>();
						for (int i = 0; i < fieldsToGetList_.size(); i++) {
							value = returnMap.get(fieldsToGetList_.get(i));
							if (value == null) {
								JOptionPane.showMessageDialog(null, "Unable to get the value of field " + fieldsToGetList_.get(i));
							} else {
								fieldsToGetMap.put(fieldsToGetToList_.get(i), value);
							}
						}
						for (int i = 0; i < dialog_.getFieldList().size(); i++) {
							value = fieldsToGetMap.get(dialog_.getFieldList().get(i).getDataSourceName());
							if (value != null) {
								dialog_.getFieldList().get(i).setValue(value);
							}
						}
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex.getMessage());
				} finally {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
		});
		jButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jButton.doClick();
					}
				}
			}
		});

		this.setSize(new Dimension(xFTextField.getWidth() + 27, XFUtility.FIELD_UNIT_HEIGHT));
		this.setLayout(new BorderLayout());
		this.add(xFTextField, BorderLayout.CENTER);
	}
	
	public ArrayList<String> getControlFieldList() {
		return fieldsToGetToList_;
	}

	public void setEditable(boolean editable) {
		if (editable) {
			this.add(jButton, BorderLayout.EAST);
			xFTextField.setBackground(XFUtility.ACTIVE_COLOR);
			xFTextField.setEditable(isEditableInEditMode_);
			xFTextField.setFocusable(isEditableInEditMode_);
			isEditable = true;
		} else {
			this.remove(jButton);
			xFTextField.setBackground(XFUtility.INACTIVE_COLOR);
			xFTextField.setEditable(false);
			xFTextField.setFocusable(false);
			isEditable = false;
		}
	}

	public void setFollowingField(XFEditableField field) {
	}
	
	public void setWidth(int width) {
		xFTextField.setWidth(width - 27);
	}
	
	public void setToolTipText(String text) {
		jButton.setToolTipText(text);
		xFTextField.setToolTipText(text);
	}

	public Object getInternalValue() {
		String text = "";
		String basicType = XFUtility.getBasicTypeOf(dataType);
		if (basicType.equals("INTEGER")
				|| basicType.equals("FLOAT")
				|| dataTypeOptionList.contains("DIAL")
				|| dataTypeOptionList.contains("ZIPNO")) {
			text = XFUtility.getStringNumber(xFTextField.getText());
		} else {
			if (kubunValueList.size() > 0) {
				text = kubunValue;
			} else {
				text = xFTextField.getText();
			}
		}
		return text;
	}

	public Object getExternalValue() {
		return xFTextField.getText();
	}
	
	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public String getValueClass() {
		return "String";
	}
	
	public void setValue(Object obj) {
		if (obj == null) {
			xFTextField.setText("");
		} else {
			if (kubunValueList.size() > 0) {
				kubunValue = obj.toString();
				if (kubunValueList.indexOf(kubunValue) > -1) {
					xFTextField.setText(kubunTextList.get(kubunValueList.indexOf(kubunValue)));
				} else {
					xFTextField.setText("N/A");
				}
			} else {
				xFTextField.setText(obj.toString());
			}
		}
	}

	public void setBackground(Color color) {
		if (xFTextField != null) {
			if (color.equals(XFUtility.ACTIVE_COLOR)) {
				if (xFTextField.isEditable()) {
					xFTextField.setBackground(XFUtility.ACTIVE_COLOR);
				} else {
					xFTextField.setBackground(XFUtility.INACTIVE_COLOR);
				}
			}
			if (color.equals(XFUtility.ERROR_COLOR)) {
				xFTextField.setBackground(XFUtility.ERROR_COLOR);
			}
		}
	}

	public int getRows() {
		return rows_;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public boolean isComponentFocusable() {
		return isEditable;
	}

	public boolean isFocusable() {
		return false;
	}
}

class XF200_PrimaryTable extends Object {
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID = "";
	private String fixedWhere = "";
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private ArrayList<String> orderByFieldList = new ArrayList<String>();
	private ArrayList<String> uniqueKeyList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF200 dialog_;
	private StringTokenizer workTokenizer;
	private String updateCounterID = "";
	private long updateCounterValue = 0;
	private String detailRowNoID = "";
	private String dbName = "";

	public XF200_PrimaryTable(org.w3c.dom.Element functionElement, XF200 dialog){
		super();

		functionElement_ = functionElement;
		dialog_ = dialog;

		tableID = functionElement_.getAttribute("PrimaryTable");
		tableElement = dialog_.getSession().getTableElement(tableID);

		if (tableElement.getAttribute("DB").equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(tableElement.getAttribute("DB"));
		}

		updateCounterID = tableElement.getAttribute("UpdateCounter");
		if (updateCounterID.equals("")) {
			updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
		} else {
			if (updateCounterID.toUpperCase().equals("*NONE")) {
				updateCounterID = "";
			}
		}

		String wrkStr1;
		org.w3c.dom.Element workElement, fieldElement;

		if (functionElement_.getAttribute("KeyFields").equals("")) {
			NodeList nodeList = tableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer.hasMoreTokens()) {
						wrkStr1 = workTokenizer.nextToken();
						keyFieldList.add(wrkStr1);
					}
				}
			}
		} else {
			workTokenizer = new StringTokenizer(functionElement_.getAttribute("KeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				keyFieldList.add(workTokenizer.nextToken());
			}
		}

		NodeList nodeList = tableElement.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("PK")) {
				wrkStr1 = "";
				workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
				if (workTokenizer.countTokens() > 1) {
					while (workTokenizer.hasMoreTokens()) {
						wrkStr1 = workTokenizer.nextToken();
					}
					if (!wrkStr1.equals("")) {
						fieldElement = dialog_.getSession().getFieldElement(tableID, wrkStr1);
						if (XFUtility.getBasicTypeOf(fieldElement.getAttribute("Type")).equals("INTEGER")
								&& tableElement.getAttribute("DetailRowNumberAuto").equals("T")) {
							detailRowNoID = wrkStr1;
						}
					}
				}
			}
			if (workElement.getAttribute("Type").equals("SK")) {
				uniqueKeyList.add(workElement.getAttribute("Fields"));
			}
		}

		workTokenizer = new StringTokenizer(functionElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldList.add(workTokenizer.nextToken());
		}

		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID, element, dialog_.getSession().getTableNodeList()));
		}
	}
	
	public String getName() {
		return tableElement.getAttribute("Name");
	}
	
	public String getDetailRowNoID() {
		return detailRowNoID;
	}

	public void setUpdateCounterValue(XFTableOperator operator) throws Exception {
		if (!updateCounterID.equals("")) {
			updateCounterValue = Long.parseLong(operator.getValueOf(updateCounterID).toString());
		}
	}
	
	public String getSQLToSelect(){
		StringBuffer buf = new StringBuffer();

		buf.append("select ");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isFieldOnPrimaryTable()
			&& !dialog_.getFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					buf.append(",");
				}
				buf.append(dialog_.getFieldList().get(i).getFieldID());
				firstField = false;
			}
		}
		if (!updateCounterID.equals("")) {
			buf.append(",");
			buf.append(updateCounterID);
		}
		buf.append(" from ");
		buf.append(tableID);

		if (orderByFieldList.size() > 0) {
			buf.append(" order by ");
			for (int i = 0; i < orderByFieldList.size(); i++) {
				if (i > 0) {
					buf.append(",");
				}
				buf.append(orderByFieldList.get(i));
			}
		}

		fixedWhere = XFUtility.getFixedWhereValue(functionElement_.getAttribute("FixedWhere"), dialog_.getSession());
		buf.append(" where ") ;
		int orderOfFieldInKey = 0;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (orderOfFieldInKey > 0) {
					buf.append(" and ") ;
				}
				buf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				buf.append("=") ;
				buf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue(), dbName));
				orderOfFieldInKey++;
			}
		}
		if (!fixedWhere.equals("")) {
			buf.append(" and (");
			buf.append(fixedWhere);
			buf.append(") ");
		}

		return buf.toString();
	}
	
	public String getSQLToGetLastDetailRowNumberValue(){
		StringBuffer buf = new StringBuffer();

		if (!detailRowNoID.equals("")) {
			buf.append("select ");
			buf.append(detailRowNoID);
			buf.append(" from ");
			buf.append(tableID);
			buf.append(" where ") ;
			int orderOfFieldInKey = 0;
			for (int i = 0; i < dialog_.getFieldList().size(); i++) {
				if (dialog_.getFieldList().get(i).isKey()
						&& !dialog_.getFieldList().get(i).getFieldID().equals(detailRowNoID)) {
					if (orderOfFieldInKey > 0) {
						buf.append(" and ") ;
					}
					buf.append(dialog_.getFieldList().get(i).getFieldID()) ;
					buf.append("=") ;
					buf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue(), dbName));
					orderOfFieldInKey++;
				}
			}

			buf.append(" order by ");
			buf.append(detailRowNoID);
			buf.append(" desc");
		}

		return buf.toString();
	}

	String getSQLToInsert() {
		StringBuffer statementBuf = new StringBuffer();

		statementBuf.append("insert into ");
		statementBuf.append(tableID);
		statementBuf.append(" (");

		boolean firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isFieldOnPrimaryTable()
			&& !dialog_.getFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ") ;
				}
				statementBuf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				firstField = false;
			}
		}

		statementBuf.append(") values(") ;

		firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isFieldOnPrimaryTable()
			&& !dialog_.getFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ") ;
				}
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue(), dbName)) ;
				firstField = false;
			}
		}

		statementBuf.append(")") ;

		return statementBuf.toString();
	}

	String getSQLToCheckPKDuplication() {
		StringBuffer statementBuf = new StringBuffer();

		statementBuf.append("select ");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isFieldOnPrimaryTable()
					&& !dialog_.getFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(",");
				}
				statementBuf.append(dialog_.getFieldList().get(i).getFieldID());
				firstField = false;
			}
		}
		statementBuf.append(" from ");
		statementBuf.append(tableID);
		statementBuf.append(" where ") ;

		firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (!firstField) {
					statementBuf.append(" and ") ;
				}
				statementBuf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				statementBuf.append("=") ;
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue(), dbName)) ;
				firstField = false;
			}
		}

		return statementBuf.toString();
	}
	
	boolean hasPrimaryKeyValueAltered() {
		boolean altered = false;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (!dialog_.getFieldList().get(i).getValue().equals(dialog_.getFieldList().get(i).getOldValue())) {
					altered = true;
				}
			}
		}
		return altered;
	}

	public ArrayList<String> getUniqueKeyList() {
		return uniqueKeyList;
	}
	
	public String getSQLToCheckSKDuplication(ArrayList<String> keyFieldList, boolean isToUpdate) {
		StringBuffer statementBuf = new StringBuffer();

		statementBuf.append("select ");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isFieldOnPrimaryTable()
					&& !dialog_.getFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(",");
				}
				statementBuf.append(dialog_.getFieldList().get(i).getFieldID());
				firstField = false;
			}
		}
		statementBuf.append(" from ");
		statementBuf.append(tableID);
		statementBuf.append(" where ") ;

		firstField = true;
		for (int j = 0; j < dialog_.getFieldList().size(); j++) {
			if (dialog_.getFieldList().get(j).isFieldOnPrimaryTable()) {
				for (int p = 0; p < keyFieldList.size(); p++) {
					if (dialog_.getFieldList().get(j).getFieldID().equals(keyFieldList.get(p))) {
						if (!isToUpdate && dialog_.getFieldList().get(j).isAutoNumberField()) {
							return "";
						} else {
							if (!firstField) {
								statementBuf.append(" and ") ;
							}
							statementBuf.append(dialog_.getFieldList().get(j).getFieldID()) ;
							statementBuf.append("=") ;
							statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(j).getBasicType(), dialog_.getFieldList().get(j).getInternalValue(), dbName)) ;
							firstField = false;
						}
					}
				}
			}
		}

		if (isToUpdate) {
			firstField = true;
			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
				if (dialog_.getFieldList().get(j).isFieldOnPrimaryTable()) {
					if (dialog_.getFieldList().get(j).isKey()) {
						if (firstField) {
							statementBuf.append(" and (") ;
						} else {
							statementBuf.append(" or ") ;
						}
						statementBuf.append(dialog_.getFieldList().get(j).getFieldID()) ;
						statementBuf.append("!=") ;
						statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(j).getBasicType(), dialog_.getFieldList().get(j).getInternalValue(), dbName)) ;
						firstField = false;
					}
				}
			}
			statementBuf.append(")") ;
		}

		return statementBuf.toString();
	}

	String getSQLToUpdate() {
		StringBuffer statementBuf = new StringBuffer();

		statementBuf.append("update ");
		statementBuf.append(tableID);
		statementBuf.append(" set ");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isFieldOnPrimaryTable()
			&& !dialog_.getFieldList().get(i).isKey()
			&& !dialog_.getFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ") ;
				}
				statementBuf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				statementBuf.append("=") ;
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue(), dbName)) ;
				firstField = false;
			}
		}
		if (!updateCounterID.equals("")) {
			if (!firstField) {
				statementBuf.append(", ") ;
			}
			statementBuf.append(updateCounterID) ;
			statementBuf.append("=") ;
			statementBuf.append(updateCounterValue + 1) ;
		}

		statementBuf.append(" where ") ;
		firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (!firstField) {
					statementBuf.append(" and ") ;
				}
				statementBuf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				statementBuf.append("=") ;
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue(), dbName)) ;
				firstField = false;
			}
		}
		if (!updateCounterID.equals("")) {
			statementBuf.append(" and ") ;
			statementBuf.append(updateCounterID) ;
			statementBuf.append("=") ;
			statementBuf.append(updateCounterValue) ;
		}

		return statementBuf.toString();
	}

	String getSQLToDelete() {
		StringBuffer statementBuf = new StringBuffer();
		statementBuf.append("delete from ");
		statementBuf.append(tableID);
		statementBuf.append(" where ") ;
		boolean firstKey = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (!firstKey) {
					statementBuf.append(" and ") ;
				}
				statementBuf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				statementBuf.append("=") ;
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue(), dbName)) ;
				firstKey = false;
			}
		}
		if (!updateCounterID.equals("")) {
			statementBuf.append(" and ") ;
			statementBuf.append(updateCounterID) ;
			statementBuf.append("=") ;
			statementBuf.append(updateCounterValue) ;
		}
		return statementBuf.toString();
	}
	
	public String getTableID(){
		return tableID;
	}
	
	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}
	
	public ArrayList<String> getKeyFieldList(){
		return keyFieldList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF200_ReferTable referTable;
		org.w3c.dom.Element workElement;
		if (this.getTableID().equals(tableID) && this.getTableID().equals(tableAlias)) {
			NodeList nodeList = tableElement.getElementsByTagName("Field");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("ID").equals(fieldID)) {
					isValid = true;
					break;
				}
			}
		} else {
			for (int i = 0; i < dialog_.getReferTableList().size(); i++) {
				referTable = dialog_.getReferTableList().get(i);
				if (referTable.getTableID().equals(tableID) && referTable.getTableAlias().equals(tableAlias)) {
					for (int j = 0; j < referTable.getFieldIDList().size(); j++) {
						if (referTable.getFieldIDList().get(j).equals(fieldID)) {
							isValid = true;
							break;
						}
					}
				}
				if (isValid) {
					break;
				}
			}
		}
		return isValid;
	}

	public int runScript(String event1, String event2) throws ScriptException {
		int countOfErrors = 0;
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		if (validScriptList.size() > 0) {
			for (int i = 0; i < validScriptList.size(); i++) {
				dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
			}
			for (int i = 0; i < dialog_.getFieldList().size(); i++) {
				if (dialog_.getFieldList().get(i).isError()) {
					countOfErrors++;
				}
			}
		}
		return countOfErrors;
	}
}

class XF200_ReferTable extends Object {
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF200 dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private boolean isOptional = false;
	private String dbName = "";

	public XF200_ReferTable(org.w3c.dom.Element referElement, XF200 dialog){
		super();

		referElement_ = referElement;
		dialog_ = dialog;

		tableID = referElement_.getAttribute("ToTable");
		tableElement = dialog_.getSession().getTableElement(tableID);

		String dbID = tableElement.getAttribute("DB");
		if (dbID.equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(dbID);
		}

		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}

		StringTokenizer workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			fieldIDList.add(workTokenizer.nextToken());
		}

		if (referElement_.getAttribute("ToKeyFields").equals("")) {
			org.w3c.dom.Element workElement = dialog_.getSession().getTablePKElement(tableID);
			workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				toKeyFieldIDList.add(workTokenizer.nextToken());
			}
		} else {
			workTokenizer = new StringTokenizer(referElement_.getAttribute("ToKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				toKeyFieldIDList.add(workTokenizer.nextToken());
			}
		}

		workTokenizer = new StringTokenizer(referElement_.getAttribute("WithKeyFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withKeyFieldIDList.add(workTokenizer.nextToken());
		}

		if (referElement_.getAttribute("Optional").equals("T")) {
			isOptional = true;
		}
	}

	public String getSelectSQL(boolean isToGetRecordsForComboBox){
		org.w3c.dom.Element workElement;
		int count;
		StringBuffer buf = new StringBuffer();

		buf.append("select ");

		count = 0;
		for (int i = 0; i < fieldIDList.size(); i++) {
			workElement = dialog_.getSession().getFieldElement(tableID, fieldIDList.get(i));
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				if (count > 0) {
					buf.append(",");
				}
				count++;
				buf.append(fieldIDList.get(i));
			}
		}
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (count > 0) {
				buf.append(",");
			}
			count++;
			buf.append(toKeyFieldIDList.get(i));
		}

		buf.append(" from ");
		buf.append(tableID);

		StringTokenizer workTokenizer;
		String keyFieldID, keyFieldTableID;
		count = 0;
		boolean isToBeWithValue;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (isToGetRecordsForComboBox) {

				// Value of the field which has either of these conditions should be within WHERE to SELECT records: //
				// 1. The with-key-field is not edit-able //
				// 2. The with-key-field is part of PK of the primary table //
				// 3. The with-key-field is on the primary table and consists of upper part of with-key-fields //
				// 4. The with-key-field is part of PK of the other join table //
				for (int j = 0; j < dialog_.getFieldList().size(); j++) {
					if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getDataSourceName())) {
						isToBeWithValue = false;
						if (dialog_.getFieldList().get(j).isVisibleOnPanel() && !dialog_.getFieldList().get(j).isEditable()) {
							isToBeWithValue = true;
						} else {
							workTokenizer = new StringTokenizer(withKeyFieldIDList.get(i), "." );
							keyFieldTableID = workTokenizer.nextToken();
							keyFieldID = workTokenizer.nextToken();
							if (keyFieldTableID.equals(dialog_.getPrimaryTable().getTableID())) {
								if (withKeyFieldIDList.size() > 1 && i < (withKeyFieldIDList.size() - 1)) {
									isToBeWithValue = true;
								} else {
									for (int k = 0; k < dialog_.getKeyFieldList().size(); k++) {
										if (keyFieldID.equals(dialog_.getKeyFieldList().get(k))) {
											isToBeWithValue = true;
										}
									}
								}
							} else {
								if (!keyFieldTableID.equals(this.tableAlias)) {
									isToBeWithValue = true;
								}
							}
						}
						if (isToBeWithValue) {
							if (count == 0) {
								buf.append(" where ");
							} else {
								buf.append(" and ");
							}
							buf.append(toKeyFieldIDList.get(i));
							buf.append("=");
							buf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(j).getBasicType(), dialog_.getFieldList().get(j).getInternalValue(), dbName)) ;
							count++;
							break;
						}
					}
				}
			} else {
				if (count == 0) {
					buf.append(" where ");
				} else {
					buf.append(" and ");
				}
				buf.append(toKeyFieldIDList.get(i));
				buf.append("=");
				for (int j = 0; j < dialog_.getFieldList().size(); j++) {
					if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getDataSourceName())) {
						buf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(j).getBasicType(), dialog_.getFieldList().get(j).getInternalValue(), dbName)) ;
						break;
					}
				}
				count++;
			}
		}

		return buf.toString();
	}

	public String getTableID(){
		return tableID;
	}

	public String getTableAlias(){
		return tableAlias;
	}

	public boolean isOptional() {
		return isOptional;
	}

	public boolean isKeyNullable() {
		boolean isKeyNullable = false;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getTableAlias() + "." + dialog_.getFieldList().get(j).getFieldID())) {
					if (dialog_.getFieldList().get(j).isNullable()) {
						isKeyNullable = true;
						break;
					}
				}
			}
		}
		return isKeyNullable;
	}

	public boolean isKeyNull() {
		boolean isKeyNull = false;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getTableAlias() + "." + dialog_.getFieldList().get(j).getFieldID())) {
					if (dialog_.getFieldList().get(j).isNull()) {
						isKeyNull = true;
						break;
					}
				}
			}
		}
		return isKeyNull;
	}

	public ArrayList<String> getKeyFieldIDList(){
		return toKeyFieldIDList;
	}

	public ArrayList<String> getFieldIDList(){
		return fieldIDList;
	}

	public ArrayList<String> getWithKeyFieldIDList(){
		return withKeyFieldIDList;
	}

	public void setKeyFieldValues(XFHashMap keyValues){
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
				if (dialog_.getFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))) {
					dialog_.getFieldList().get(j).setValue(keyValues.getValue(withKeyFieldIDList.get(i)));
					break;
				}
			}
		}
	}

	public void setErrorOnRelatedFields() {
		boolean noneOfKeyFieldsWereSetError = true;

		/////////////////////////////////////////////////
		// Set error on the visible editable key field //
		/////////////////////////////////////////////////
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
				if (dialog_.getFieldList().get(j).isVisibleOnPanel()
						&& dialog_.getFieldList().get(j).isEditable()
						&& dialog_.getFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
						&& !dialog_.getFieldList().get(j).isError()) {
					dialog_.getFieldList().get(j).setError(XFUtility.RESOURCE.getString("FunctionError53") + tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
					noneOfKeyFieldsWereSetError = false;
					break;
				}
			}
		}

		if (noneOfKeyFieldsWereSetError) {
			///////////////////////////////////////////////////////
			// Set error on the visible editable attribute field //
			///////////////////////////////////////////////////////
			for (int i = 0; i < fieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getFieldList().size(); j++) {
					if (dialog_.getFieldList().get(j).isVisibleOnPanel()
							&& dialog_.getFieldList().get(j).isEditable()
							&& dialog_.getFieldList().get(j).getFieldID().equals(fieldIDList.get(i))
							&& dialog_.getFieldList().get(j).getTableAlias().equals(this.tableAlias)
							&& !dialog_.getFieldList().get(j).isError()) {
						dialog_.getFieldList().get(j).setError(XFUtility.RESOURCE.getString("FunctionError53") + tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
						noneOfKeyFieldsWereSetError = false;
						break;
					}
				}
			}
		}

		if (noneOfKeyFieldsWereSetError) {
			//////////////////////////////////////
			// Set error on the first key field //
			//////////////////////////////////////
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getFieldList().size(); j++) {
					if (dialog_.getFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
							&& !dialog_.getFieldList().get(j).isError()) {
						dialog_.getFieldList().get(j).setError(XFUtility.RESOURCE.getString("FunctionError53") + tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
						break;
					}
				}
			}
		}
	}
	
	public void setToBeExecuted(boolean executed){
		isToBeExecuted = executed;
	}
	
	public boolean isToBeExecuted(){
		return isToBeExecuted;
	}
}

class XF200_Component_keyAdapter extends java.awt.event.KeyAdapter {
	XF200 adaptee;
	XF200_Component_keyAdapter(XF200 adaptee) {
		this.adaptee = adaptee;
	}
	public void keyPressed(KeyEvent e) {
		adaptee.component_keyPressed(e);
	}
}
