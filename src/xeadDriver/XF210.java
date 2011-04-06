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

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.HSSFColor;
import org.w3c.dom.*;

public class XF210 extends JDialog implements XFExecutable, XFScriptable {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element functionElement_ = null;
	private HashMap<String, Object> parmMap_ = null;
	private HashMap<String, Object> returnMap_ = new HashMap<String, Object>();
	private XF210_PrimaryTable primaryTable_ = null;
	private XF210_KeyInputDialog keyInputDialog = null; 
	private Session session_ = null;
	private boolean instanceIsAvailable_ = true;
	private boolean isToBeCanceled = false;
	private int instanceArrayIndex_ = -1;
	private int programSequence;
	private StringBuffer processLog = new StringBuffer();
	private String initialMsg = "";
	private Dimension scrSize;
	private JPanel jPanelMain = new JPanel();
	private JSplitPane jSplitPaneMain = new JSplitPane();
	private JPanel jPanelFields = new JPanel();
	private JScrollPane jScrollPaneFields = new JScrollPane();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private ArrayList<XF210_ReferTable> referTableList = new ArrayList<XF210_ReferTable>();
	private ArrayList<XF210_Field> fieldList = new ArrayList<XF210_Field>();
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private JPanel[] jPanelButtonArray = new JPanel[7];
	private JPanel jPanelInfo = new JPanel();
	private GridLayout gridLayoutButtons = new GridLayout();
	private GridLayout gridLayoutInfo = new GridLayout();
	private ArrayList<String> messageList = new ArrayList<String>();
	private JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JButton[] jButtonArray = new JButton[7];
	private Action checkAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			messageList.clear();
			doButtonActionUpdate(true);
			setMessagesOnPanel();
		}
	};
	private Action helpAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			session_.browseHelp();
		}
	};
	private Action[] actionButtonArray = new Action[7];
	private int buttonIndexForF6, buttonIndexForF8;
	private String[] actionDefinitionArray = new String[7];
	private Connection connection = null;
	private ScriptEngine scriptEngine;
	private Bindings engineScriptBindings;
	private String scriptNameRunning = "";
	private final int FIELD_HORIZONTAL_MARGIN = 1;
	private final int FIELD_VERTICAL_MARGIN = 5;
	private final int FONT_SIZE = 14;
	private ByteArrayOutputStream exceptionLog;
	private PrintStream exceptionStream;
	private String exceptionHeader = "";

	public XF210(Session session, int instanceArrayIndex) {
		super(session, "", true);
		try {
			session_ = session;
			instanceArrayIndex_ = instanceArrayIndex;
			//
			initComponentsAndVariants();
			//
		} catch(Exception e) {
			e.printStackTrace(exceptionStream);
		}
	}

	void initComponentsAndVariants() throws Exception {
		//
		scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		jPanelMain.setLayout(new BorderLayout());
		jPanelFields.setLayout(null);
		jScrollPaneFields.getViewport().add(jPanelFields, null);
		//
		jPanelBottom.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setLayout(new BorderLayout());
		jPanelBottom.setBorder(null);
		jLabelFunctionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelFunctionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelFunctionID.setForeground(Color.gray);
		jLabelSessionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelSessionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelSessionID.setForeground(Color.gray);
		jPanelButtons.setPreferredSize(new Dimension(10, 35));
		jPanelButtons.setBorder(null);
		jPanelButtons.setLayout(gridLayoutButtons);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
		gridLayoutInfo.setVgap(4);
		jPanelInfo.setLayout(gridLayoutInfo);
		jPanelInfo.add(jLabelSessionID);
		jPanelInfo.add(jLabelFunctionID);
		gridLayoutButtons.setColumns(7);
		gridLayoutButtons.setRows(1);
		gridLayoutButtons.setHgap(2);
		//
		for (int i = 0; i < 7; i++) {
			jButtonArray[i] = new JButton();
			jButtonArray[i].setBounds(new Rectangle(0, 0, 90, 30));
			jButtonArray[i].setFocusable(false);
			jButtonArray[i].addActionListener(new XF210_FunctionButton_actionAdapter(this));
			jPanelButtonArray[i] = new JPanel();
			jPanelButtonArray[i].setLayout(new BorderLayout());
			jPanelButtonArray[i].add(jButtonArray[i], BorderLayout.CENTER);
			actionButtonArray[i] = new ButtonAction(jButtonArray[i]);
			jPanelButtons.add(jPanelButtonArray[i]);
		}
		//
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		//
		jSplitPaneMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPaneMain.addKeyListener(new XF210_Component_keyAdapter(this));
		jSplitPaneMain.add(jScrollPaneFields, JSplitPane.TOP);
		jSplitPaneMain.add(jScrollPaneMessages, JSplitPane.BOTTOM);
		//
		jPanelMain.add(jSplitPaneMain, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
		this.setSize(new Dimension(scrSize.width, scrSize.height));
	}

	public boolean isAvailable() {
		return instanceIsAvailable_;
	}

	public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parmMap) {
		SortableDomElementListModel sortedList;
		String workAlias, workTableID, workFieldID;
		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;

		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			///////////////////
			// Process parms //
			///////////////////
			parmMap_ = parmMap;
			if (parmMap_ == null) {
				parmMap_ = new HashMap<String, Object>();
			}
			returnMap_.clear();
			returnMap_.putAll(parmMap_);
			returnMap_.put("RETURN_CODE", "21");

			///////////////////////////
			// Initializing variants //
			///////////////////////////
			isToBeCanceled = false;
			instanceIsAvailable_ = false;
			exceptionLog = new ByteArrayOutputStream();
			exceptionStream = new PrintStream(exceptionLog);
			exceptionHeader = "";
			functionElement_ = functionElement;
			processLog.delete(0, processLog.length());
			messageList.clear();
			connection = session_.getConnection();
			initialMsg = functionElement_.getAttribute("InitialMsg");
			programSequence = session_.writeLogOfFunctionStarted(functionElement_.getAttribute("ID"), functionElement_.getAttribute("Name"));
			keyInputDialog = null;

			//////////////////////////////////////
			// Setup Script Engine and Bindings //
			//////////////////////////////////////
			scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
			engineScriptBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
			engineScriptBindings.clear();
			engineScriptBindings.put("instance", (XFScriptable)this);
			//engineScriptBindings.put("session", (XFScriptableSession)session_);
			
			//////////////////////////////
			// Set panel configurations //
			//////////////////////////////
			jLabelSessionID.setText(session_.getSessionID());
			jLabelFunctionID.setText("210" + "-" + instanceArrayIndex_ + "-" + functionElement_.getAttribute("ID"));
			FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font("Dialog", 0, FONT_SIZE));
			jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));
			if (functionElement_.getAttribute("Size").equals("")) {
				this.setPreferredSize(new Dimension(scrSize.width, scrSize.height));
				this.setLocation(0, 0);
				this.pack();
			} else {
				if (!functionElement_.getAttribute("Size").equals("AUTO")) {
					workTokenizer = new StringTokenizer(functionElement_.getAttribute("Size"), ";" );
					int width = Integer.parseInt(workTokenizer.nextToken());
					int height = Integer.parseInt(workTokenizer.nextToken());
					this.setPreferredSize(new Dimension(width, height));
					int posX = (scrSize.width - width) / 2;
					int posY = (scrSize.height - height) / 2;
					this.setLocation(posX, posY);
					this.pack();
				}
			}

			//////////////////////////////////////////////
			// Setup the primary table and refer tables //
			//////////////////////////////////////////////
			primaryTable_ = new XF210_PrimaryTable(functionElement_, this);
			referTableList.clear();
			NodeList referNodeList = primaryTable_.getTableElement().getElementsByTagName("Refer");
			sortedList = XFUtility.getSortedListModel(referNodeList, "Order");
			for (int i = 0; i < sortedList.getSize(); i++) {
				org.w3c.dom.Element element = (org.w3c.dom.Element)sortedList.getElementAt(i);
				referTableList.add(new XF210_ReferTable(element, this));
			}

			/////////////////////////////
			// Initializing panel mode //
			/////////////////////////////
			this.setTitle(functionElement_.getAttribute("Name"));
			if (initialMsg.equals("")) {
				messageList.add(res.getString("FunctionMessage12"));
			} else {
				messageList.add(initialMsg);
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
			int biggestWidth = 300;
			int biggestHeight = 50;
			boolean firstVisibleField = true;
			NodeList functionFieldList = functionElement_.getElementsByTagName("Field");
			SortableDomElementListModel sortableList = XFUtility.getSortedListModel(functionFieldList, "Order");
			for (int i = 0; i < sortableList.getSize(); i++) {
				//
				fieldList.add(new XF210_Field((org.w3c.dom.Element)sortableList.getElementAt(i), this));
				//
				if (fieldList.get(i).isVisibleOnPanel()) {
					if (firstVisibleField) {
						posX = 0;
						posY = this.FIELD_VERTICAL_MARGIN + 3;
						firstVisibleField = false;
					} else {
						if (fieldList.get(i).isHorizontal()) {
							posX = posX + dimOfPriviousField.width + fieldList.get(i).getPositionMargin() + this.FIELD_HORIZONTAL_MARGIN;
						} else {
							posX = 0;
							posY = posY + dimOfPriviousField.height + fieldList.get(i).getPositionMargin() + this.FIELD_VERTICAL_MARGIN;
						}
					}
					dim = fieldList.get(i).getPreferredSize();
					fieldList.get(i).setBounds(posX, posY, dim.width, dim.height);
					if (posX + dim.width > biggestWidth) {
						biggestWidth = posX + dim.width;
					}
					if (posY + dim.height > biggestHeight) {
						biggestHeight = posY + dim.height;
					}
					//
					if (fieldList.get(i).isHorizontal()) {
						dimOfPriviousField = new Dimension(dim.width, XFUtility.FIELD_UNIT_HEIGHT);
					} else {
						dimOfPriviousField = new Dimension(dim.width, dim.height);
					}
					jPanelFields.add(fieldList.get(i));
				} else {
					//fieldList.get(i).setBounds(2000, 2000, dim.width, dim.height);
				}
				//jPanelFields.add(fieldList.get(i));
			}
			jPanelFields.setPreferredSize(new Dimension(biggestWidth + 30, biggestHeight + 10));
			if (functionElement_.getAttribute("Size").equals("AUTO")) {
				int workWidth = biggestWidth + 50;
				if (workWidth < 800) {
					workWidth = 800;
				}
				int workHeight = biggestHeight + 150;
				if (workWidth > scrSize.width) {
					workWidth = scrSize.width;
					posX = 0;
				} else {
					posX = (scrSize.width - workWidth) / 2;
				}
				if (workHeight > scrSize.height) {
					workHeight = scrSize.height;
					posY = 0;
				} else {
					posY = (scrSize.height - workHeight) / 2;
				}
				this.setPreferredSize(new Dimension(workWidth, workHeight));
				this.setLocation(posX, posY);
				this.pack();
			}
			//
			// Add primary table key fields as HIDDEN column if they are not on the column list //
			for (int i = 0; i < primaryTable_.getKeyFieldList().size(); i++) {
				if (!existsInColumnList(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldList().get(i))) {
					XF210_Field columnField = new XF210_Field(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldList().get(i), this);
					fieldList.add(columnField);
					//columnField.setBounds(2000, 2000, columnField.getPreferredSize().width, columnField.getPreferredSize().height);
					//jPanelFields.add(columnField);
				}
			}
			//
			// Add unique key fields of primary table as HIDDEN column if they are not on the column list //
			for (int i = 0; i < primaryTable_.getUniqueKeyList().size(); i++) {
				workTokenizer = new StringTokenizer(primaryTable_.getUniqueKeyList().get(i), ";" );
				while (workTokenizer.hasMoreTokens()) {
					workFieldID = workTokenizer.nextToken();
					if (!existsInColumnList(primaryTable_.getTableID(), "", workFieldID)) {
						XF210_Field columnField = new XF210_Field(primaryTable_.getTableID(), "", workFieldID, this);
						fieldList.add(columnField);
						//columnField.setBounds(2000, 2000, columnField.getPreferredSize().width, columnField.getPreferredSize().height);
						//jPanelFields.add(columnField);
					}
				}
			}
			//
			// Analyze fields in script and add them as HIDDEN columns if necessary //
			for (int i = 0; i < primaryTable_.getScriptList().size(); i++) {
				if	(primaryTable_.getScriptList().get(i).isToBeRunAtEvent("BR", "")
						|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("AR", "")
						|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("BU", "")
						|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("AU", "")) {
					for (int j = 0; j < primaryTable_.getScriptList().get(i).getFieldList().size(); j++) {
						workTokenizer = new StringTokenizer(primaryTable_.getScriptList().get(i).getFieldList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workTableID = getTableIDOfTableAlias(workAlias);
						workFieldID = workTokenizer.nextToken();
						if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
							workElement = session_.getFieldElement(workTableID, workFieldID);
							if (workElement == null) {
								String msg = res.getString("FunctionError1") + primaryTable_.getTableID() + res.getString("FunctionError2") + primaryTable_.getScriptList().get(i).getName() + res.getString("FunctionError3") + workAlias + "_" + workFieldID + res.getString("FunctionError4");
								JOptionPane.showMessageDialog(null, msg);
								throw new Exception(msg);
							} else {
								if (primaryTable_.isValidDataSource(workTableID, workAlias, workFieldID)) {
									XF210_Field columnField = new XF210_Field(workTableID, workAlias, workFieldID, this);
									fieldList.add(columnField);
									//columnField.setBounds(2000, 2000, columnField.getPreferredSize().width, columnField.getPreferredSize().height);
									//jPanelFields.add(columnField);
								}
							}
						}
					}
				}
			}
			//
			// Analyze refer tables and add their fields as HIDDEN columns if necessary //
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
							XF210_Field columnField = new XF210_Field(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j), this);
							fieldList.add(columnField);
							//columnField.setBounds(2000, 2000, columnField.getPreferredSize().width, columnField.getPreferredSize().height);
							//jPanelFields.add(columnField);
						}
					}
					for (int j = 0; j < referTableList.get(i).getWithKeyFieldIDList().size(); j++) {
						workTokenizer = new StringTokenizer(referTableList.get(i).getWithKeyFieldIDList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workTableID = getTableIDOfTableAlias(workAlias);
						workFieldID = workTokenizer.nextToken();
						if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
							XF210_Field columnField = new XF210_Field(workTableID, workAlias, workFieldID, this);
							fieldList.add(columnField);
							//columnField.setBounds(2000, 2000, columnField.getPreferredSize().width, columnField.getPreferredSize().height);
							//jPanelFields.add(columnField);
						}
					}
				}
			}

			//////////////////////////////////////////////////
			// Fetch the record and set values on the panel //
			//////////////////////////////////////////////////
			fetchTableRecord();

			//////////////////////////////////////////////
			// Setup function-keys and function-buttons //
			//////////////////////////////////////////////
			setupFunctionKeysAndButtons();

		} catch(Exception e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError5"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		////////////////
		// Show Frame //
		////////////////
		if (this.isToBeCanceled) {
			closeFunction();
		} else {
			if (parmMap_.containsKey("INITIAL_MESSAGE")) {
				jTextAreaMessages.setText((String)parmMap_.get("INITIAL_MESSAGE"));
			} else {
				setMessagesOnPanel();
			}
			this.setVisible(true);
		}

		///////////////////////////////
		// Release instance and exit //
		///////////////////////////////
		instanceIsAvailable_ = true;
		return returnMap_;
	}

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			closeFunction();
		}
	}

	void setErrorAndCloseFunction() {
		isToBeCanceled = true;
		returnMap_.put("RETURN_CODE", "99");
		closeFunction();
	}

	void closeFunction() {
		instanceIsAvailable_ = true;
		messageList.clear();
		//
		String wrkStr;
		if (exceptionLog.size() > 0 || !exceptionHeader.equals("")) {
			wrkStr = processLog.toString() + "\nERROR LOG:\n" + exceptionHeader + exceptionLog.toString();
		} else {
			wrkStr = processLog.toString();
		}
		wrkStr = wrkStr.replace("'", "\"");
		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), wrkStr);
		//
		this.setVisible(false);
	}
	
	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(this, message);
		}
		returnMap_.put("RETURN_CODE", "21");
		isToBeCanceled = true;
	}

	public void callFunction(String functionID) {
		try {
			returnMap_ = XFUtility.callFunction(session_, functionID, parmMap_);
		} catch (Exception e) {
			String message = res.getString("FunctionError9") + functionID + res.getString("FunctionError10");
			JOptionPane.showMessageDialog(null,message);
			exceptionHeader = message;
			setErrorAndCloseFunction();
		}
	}

//	void setFocusOnComponent() {
//		boolean noFieldFocused = true;
//		for (int i = 0; i < fieldList.size(); i++) {
//			if (fieldList.get(i).isVisibleOnPanel() && fieldList.get(i).isEditable()) {
//				fieldList.get(i).requestFocus();
//				noFieldFocused = false;
//				break;
//			}
//		}
//		if (noFieldFocused) {
//			jPanelMain.requestFocus();
//		}
//	}

	void setupFunctionKeysAndButtons() {
		//
		InputMap inputMap  = jPanelMain.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.clear();
		ActionMap actionMap = jPanelMain.getActionMap();
		actionMap.clear();
		//
		for (int i = 0; i < 7; i++) {
			jButtonArray[i].setText("");
			jButtonArray[i].setVisible(false);
			actionDefinitionArray[i] = "";
		}
		//
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHECK");
		actionMap.put("CHECK", checkAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMap.put("HELP", helpAction);
		//
		buttonIndexForF6 = -1;
		buttonIndexForF8 = -1;
		int workIndex;
		org.w3c.dom.Element element;
		NodeList buttonList = functionElement_.getElementsByTagName("Button");
		for (int i = 0; i < buttonList.getLength(); i++) {
			element = (org.w3c.dom.Element)buttonList.item(i);
			//
			workIndex = Integer.parseInt(element.getAttribute("Position"));
			//
			actionDefinitionArray[workIndex] = element.getAttribute("Action");
			jButtonArray[workIndex].setVisible(true);
			//
			inputMap.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
			actionMap.put("actionButton" + workIndex, actionButtonArray[workIndex]);
			//
			XFUtility.setCaptionToButton(jButtonArray[workIndex], element, "");
			//
			if (element.getAttribute("Number").equals("6")) {
				buttonIndexForF6 = workIndex;
			}
			if (element.getAttribute("Number").equals("8")) {
				buttonIndexForF8 = workIndex;
			}
		}
	}

	void fetchTableRecord() {
		try {
			//
			boolean recordNotFound = true;
			String inputDialogMessage = "";
			boolean keyInputRequired = false;
			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).isKey()) {
					if (parmMap_.containsKey(fieldList.get(i).getFieldID())) {
						if (parmMap_.get(fieldList.get(i).getFieldID()).equals(fieldList.get(i).getNullValue())) {
							keyInputRequired = true;
						}
					} else {
						keyInputRequired = true;
					}
				} else {
					fieldList.get(i).setValue(fieldList.get(i).getNullValue());
				}
			}
			//
			while (recordNotFound && !isToBeCanceled) {
				//
				if (keyInputRequired) {
					if (keyInputDialog == null) {
						keyInputDialog = new XF210_KeyInputDialog(this);
					}
					parmMap_ = keyInputDialog.requestKeyValues(inputDialogMessage);
					if (parmMap_.size() == 0) {
						isToBeCanceled = true;
						returnMap_.put("RETURN_CODE", "01");
						closeFunction();
						return;
					}
				}
				//
				for (int i = 0; i < fieldList.size(); i++) {
					if (parmMap_.containsKey(fieldList.get(i).getFieldID())) {
						fieldList.get(i).setValue(parmMap_.get(fieldList.get(i).getFieldID()));
					}
				}
				//
				primaryTable_.runScript("BR", "");
				//
				Statement statementForPrimaryTable = connection.createStatement();
				String sql = primaryTable_.getSQLToSelect();
				XFUtility.appendLog(sql, processLog);
				ResultSet resultOfPrimaryTable = statementForPrimaryTable.executeQuery(sql);
				if (resultOfPrimaryTable.next()) {
					//
					recordNotFound = false;
					//
					for (int i = 0; i < fieldList.size(); i++) {
						if (fieldList.get(i).getTableID().equals(primaryTable_.getTableID())) {
							fieldList.get(i).setValueOfResultSet(resultOfPrimaryTable);
						}
					}
					//
					fetchReferTableRecords("AR", false, "");
					//
					primaryTable_.runScript("BU", "AR()");
					//
					for (int i = 0; i < fieldList.size(); i++) {
						if (fieldList.get(i).isError()) {
							JOptionPane.showMessageDialog(null, fieldList.get(i).getError());
							isToBeCanceled = true;
							returnMap_.put("RETURN_CODE", "01");
							closeFunction();
							return;
						}
					}
					//
					primaryTable_.setUpdateCounterValue(resultOfPrimaryTable);
					//
				} else {
					//
					if (keyInputRequired) {
						inputDialogMessage = res.getString("FunctionError37");
					} else {
						JOptionPane.showMessageDialog(this, res.getString("FunctionError38"));
						returnMap_.put("RETURN_CODE", "21");
						isToBeCanceled = true;
					}
				}
				//
				resultOfPrimaryTable.close();
			}
			//
		} catch(SQLException e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError6"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} catch(ScriptException e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
	}

	void resetFieldError() {
		for (int i = 0; i < fieldList.size(); i++) {
			fieldList.get(i).setError(false);
		}
	}

	int checkFieldValueErrorsToUpdate() {
		int countOfErrors = 0;
		//
		countOfErrors = fetchReferTableRecords("BU", true, "");
		//
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isNullError()) {
				countOfErrors++;
			}
		}
		//
		if (this.isToBeCanceled) {
			closeFunction();
		}
		//
		return countOfErrors;
	}

	protected int fetchReferTableRecords(String event, boolean toBeChecked, String specificReferTable) {
		int countOfErrors = 0;
		ResultSet resultOfReferTable = null;
		boolean recordNotFound;
		String sql = "";
		//
		try {
			//
			Statement statementForReferTable = connection.createStatement();
			//
			countOfErrors = countOfErrors + primaryTable_.runScript(event, "BR()");
			//
			for (int i = 0; i < referTableList.size(); i++) {
				if (specificReferTable.equals("") || specificReferTable.equals(referTableList.get(i).getTableAlias())) {
					//
					if (referTableList.get(i).isToBeExecuted()) {
						//
						countOfErrors = countOfErrors + primaryTable_.runScript(event, "BR(" + referTableList.get(i).getTableAlias() + ")");
						//
						for (int j = 0; j < fieldList.size(); j++) {
							if (fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
								fieldList.get(j).setValue(null);
							}
						}
						//
						if (!referTableList.get(i).isKeyNullable() || !referTableList.get(i).isKeyNull()) {
							recordNotFound = true;
							//
							sql = referTableList.get(i).getSelectSQL(false);
							XFUtility.appendLog(sql, processLog);
							resultOfReferTable = statementForReferTable.executeQuery(sql);
							while (resultOfReferTable.next()) {
								//
								if (referTableList.get(i).isRecordToBeSelected(resultOfReferTable)) {
									//
									recordNotFound = false;
									//
									for (int j = 0; j < fieldList.size(); j++) {
										if (fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
											fieldList.get(j).setValueOfResultSet(resultOfReferTable);
										}
									}
									//
									countOfErrors = countOfErrors + primaryTable_.runScript(event, "AR(" + referTableList.get(i).getTableAlias() + ")");
								}
							}
							//
							if (recordNotFound && toBeChecked && !referTableList.get(i).isOptional()) {
								countOfErrors++;
								referTableList.get(i).setErrorOnRelatedFields();
							}
							//
							resultOfReferTable.close();
						}
					}
				}
			}
			//
			countOfErrors = countOfErrors + primaryTable_.runScript(event, "AR()");
			//
			// check if prompt-key is EditControlled or not //
			for (int i = 0; i < fieldList.size(); i++) {
				fieldList.get(i).checkPromptKeyEdit();
			}
			//
		} catch(SQLException e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError6"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} catch(ScriptException e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
		//
		if (toBeChecked) {
			return countOfErrors;
		} else {
			return 0;
		}
	}
	
	boolean hasNoErrorWithKey() {
		boolean hasNoError = true;
		ArrayList<String> uniqueKeyList = new ArrayList<String>();
		ArrayList<String> keyFieldList = new ArrayList<String>();
		ResultSet resultOfPrimaryTable = null;
		String sql = "";
		//
		try {
			Statement statementForPrimaryTable = connection.createStatement();
			//
			if (primaryTable_.hasPrimaryKeyValueAltered()) {
				hasNoError = false;
				messageList.add(res.getString("FunctionError32"));
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).isKey()) {
						fieldList.get(i).setError(true);
					}
				}
			}
			//
			if (hasNoError) {
				StringTokenizer workTokenizer;
				uniqueKeyList = primaryTable_.getUniqueKeyList();
				for (int i = 0; i < uniqueKeyList.size(); i++) {
					keyFieldList.clear();
					workTokenizer = new StringTokenizer(uniqueKeyList.get(i), ";" );
					while (workTokenizer.hasMoreTokens()) {
						keyFieldList.add(workTokenizer.nextToken());
					}
					sql = primaryTable_.getSQLToCheckSKDuplication(keyFieldList);
					XFUtility.appendLog(sql, processLog);
					resultOfPrimaryTable = statementForPrimaryTable.executeQuery(sql);
					if (resultOfPrimaryTable.next()) {
						hasNoError = false;
						messageList.add(res.getString("FunctionError22"));
						for (int j = 0; j < fieldList.size(); j++) {
							if (keyFieldList.contains(fieldList.get(j).getFieldID())) {
								fieldList.get(j).setError(true);
							}
						}
					}
				}
			}
		} catch(SQLException e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError6"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} finally {
			try {
				if (resultOfPrimaryTable != null) {
					resultOfPrimaryTable.close();
				}
			} catch(SQLException e) {
				e.printStackTrace(exceptionStream);
			}
		}
		//
		return hasNoError;
	}

	void setMessagesOnPanel() {
		//
		boolean topErrorField = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isError()) {
				if (topErrorField) {
					fieldList.get(i).requestFocus();
					topErrorField = false;
				}
				messageList.add(fieldList.get(i).getCaption() + res.getString("Colon") + fieldList.get(i).getError());
			}
		}
		//
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
		//
		int heightOfErrorMessages = (messageList.size() + 1) * 20;
		if (heightOfErrorMessages <= 40) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - 40 - 80);
		}
		if (heightOfErrorMessages > 40 && heightOfErrorMessages <= 240) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - heightOfErrorMessages - 80);
		}
		if (heightOfErrorMessages > 240) {
			jSplitPaneMain.setDividerLocation(this.getHeight() - 240 - 80);
		}
	}

	void jFunctionButton_actionPerformed(ActionEvent e) {
		Component com = (Component)e.getSource();
		for (int i = 0; i < 7; i++) {
			if (com.equals(jButtonArray[i])) {
				doButtonAction(actionDefinitionArray[i]);
				break;
			}
		}
	}

	void component_keyPressed(KeyEvent e) {
		//
		// Steps to hack F6 and F8 of JSplitPane //
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
			//
			messageList.clear();
			//
			if (action.equals("EXIT")) {
				closeFunction();
			}
			//
			if (action.equals("UPDATE")) {
				doButtonActionUpdate(false);
			}
			//
			if (action.equals("OUTPUT")) {
				session_.browseFile(getExcellBookURI());
			}
			//
			setMessagesOnPanel();
			//
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	void doButtonActionUpdate(boolean checkOnly) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			//
			resetFieldError();
			//
			int countOfErrors = checkFieldValueErrorsToUpdate();
			if (countOfErrors == 0) {
				//
				if (hasNoErrorWithKey()) {
					//
					if (checkOnly) {
						messageList.add(res.getString("FunctionMessage9"));
					} else {
						//
						Statement statement = connection.createStatement();
						String sql = primaryTable_.getSQLToUpdate();
						XFUtility.appendLog(sql, processLog);
						int recordCount = statement.executeUpdate(sql);
						if (recordCount == 1) {
							primaryTable_.runScript("AU", "");
							//
							if (this.isToBeCanceled) {
								connection.rollback();
							} else {
								connection.commit();
								returnMap_.put("RETURN_CODE", "20");
							}
							closeFunction();
						} else {
							String errorMessage = res.getString("FunctionError19");
							JOptionPane.showMessageDialog(jPanelMain, errorMessage);
							exceptionHeader = errorMessage;
							try {
								connection.rollback();
							} catch(SQLException e) {
								e.printStackTrace(exceptionStream);
							}
							setErrorAndCloseFunction();
						}
					}
				}
			}
		} catch(SQLException e1) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError6"));
			e1.printStackTrace(exceptionStream);
			try {
				connection.rollback();
			} catch(SQLException e2) {
				e2.printStackTrace(exceptionStream);
			}
			setErrorAndCloseFunction();
			//
		} catch(ScriptException e1) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
			e1.printStackTrace(exceptionStream);
			try {
				connection.rollback();
			} catch(SQLException e2) {
				e2.printStackTrace(exceptionStream);
			}
			setErrorAndCloseFunction();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	URI getExcellBookURI() {
		File xlsFile = null;
		String xlsFileName = "";
		FileOutputStream fileOutputStream = null;
		HSSFFont font = null;
		//
		HSSFWorkbook workBook = new HSSFWorkbook();
		HSSFSheet workSheet = workBook.createSheet(functionElement_.getAttribute("Name"));
		workSheet.setDefaultRowHeight((short)1000);
		HSSFFooter workSheetFooter = workSheet.getFooter();
		workSheetFooter.setRight(functionElement_.getAttribute("Name") + "  Page " + HSSFFooter.page() + " / " + HSSFFooter.numPages() );
		//
		HSSFFont fontHeader = workBook.createFont();
		fontHeader = workBook.createFont();
		fontHeader.setFontName(res.getString("XLSFontHDR"));
		fontHeader.setFontHeightInPoints((short)11);
		//
		HSSFFont fontDataBlack = workBook.createFont();
		fontDataBlack.setFontName(res.getString("XLSFontDTL"));
		fontDataBlack.setFontHeightInPoints((short)11);
		HSSFFont fontDataRed = workBook.createFont();
		fontDataRed.setFontName(res.getString("XLSFontDTL"));
		fontDataRed.setFontHeightInPoints((short)11);
		fontDataRed.setColor(HSSFColor.RED.index);
		HSSFFont fontDataBlue = workBook.createFont();
		fontDataBlue.setFontName(res.getString("XLSFontDTL"));
		fontDataBlue.setFontHeightInPoints((short)11);
		fontDataBlue.setColor(HSSFColor.BLUE.index);
		HSSFFont fontDataGreen = workBook.createFont();
		fontDataGreen.setFontName(res.getString("XLSFontDTL"));
		fontDataGreen.setFontHeightInPoints((short)11);
		fontDataGreen.setColor(HSSFColor.GREEN.index);
		HSSFFont fontDataOrange = workBook.createFont();
		fontDataOrange.setFontName(res.getString("XLSFontDTL"));
		fontDataOrange.setFontHeightInPoints((short)11);
		fontDataOrange.setColor(HSSFColor.ORANGE.index);
		//
		HSSFCellStyle styleHeader = workBook.createCellStyle();
		styleHeader.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleHeader.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleHeader.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleHeader.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleHeader.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		styleHeader.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleHeader.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		styleHeader.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		styleHeader.setFont(fontHeader);
		//
		int currentRowNumber = -1;
		int mergeRowNumberFrom = -1;
		//
		try {
			xlsFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".xls");
			xlsFileName = xlsFile.getPath();
			fileOutputStream = new FileOutputStream(xlsFileName);
			//
			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).isVisibleOnPanel()) {
					for (int j = 0; j < fieldList.get(i).getRows(); j++) {
						//
						currentRowNumber++;
						HSSFRow rowData = workSheet.createRow(currentRowNumber);
						//
						//Cells for header field label
						HSSFCell cellHeader = rowData.createCell(0);
						cellHeader.setCellStyle(styleHeader);
						if (j==0) {
							mergeRowNumberFrom = currentRowNumber;
							cellHeader.setCellValue(new HSSFRichTextString(fieldList.get(i).getCaption()));
						}
						rowData.createCell(1).setCellStyle(styleHeader);
						//
						//Cells for header field data
						if (fieldList.get(i).getColor().equals("black")) {
							font = fontDataBlack;
						}
						if (fieldList.get(i).getColor().equals("red")) {
							font = fontDataRed;
						}
						if (fieldList.get(i).getColor().equals("blue")) {
							font = fontDataBlue;
						}
						if (fieldList.get(i).getColor().equals("green")) {
							font = fontDataGreen;
						}
						if (fieldList.get(i).getColor().equals("orange")) {
							font = fontDataOrange;
						}
						setupCellAttributesForHeaderField(rowData, workBook, workSheet, fieldList.get(i), currentRowNumber, j, font);
					}
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 0, 1));
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 2, 8));
				}
			}
			//
			workBook.write(fileOutputStream);
			fileOutputStream.close();
			//
			messageList.add(res.getString("XLSComment1"));
			//
		} catch(Exception e1) {
			messageList.add(res.getString("XLSErrorMessage"));
			e1.printStackTrace(exceptionStream);
			try {
				fileOutputStream.close();
			} catch (Exception e2) {
				e2.printStackTrace(exceptionStream);
			}
		}
		return xlsFile.toURI();
	}

	private void setupCellAttributesForHeaderField(HSSFRow rowData, HSSFWorkbook workBook, HSSFSheet workSheet, XF210_Field object, int currentRowNumber, int rowIndexInCell, HSSFFont font) {
		HSSFCellStyle style = workBook.createCellStyle();
		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		style.setFont(font);
		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		style.setWrapText(true);
		style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
		String wrkStr;
		//
		HSSFCell cellValue = rowData.createCell(2);
		//
		if (object.getBasicType().equals("INTEGER")) {
			cellValue.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
			style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
			style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
			style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
			cellValue.setCellStyle(style);
			if (rowIndexInCell==0) {
				//cellValue.setCellValue((Integer)object.getInternalValue());
				wrkStr = object.getInternalValue().toString().replace(",", "");
				cellValue.setCellValue(Integer.parseInt(wrkStr));
			}
		} else {
			if (object.getBasicType().equals("FLOAT")) {
				cellValue.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
				style.setDataFormat(XFUtility.getFloatFormat(workBook, object.getDecimalSize()));
				cellValue.setCellStyle(style);
				if (rowIndexInCell==0) {
					//cellValue.setCellValue((Double)object.getInternalValue());
					wrkStr = object.getInternalValue().toString().replace(",", "");
					cellValue.setCellValue(Double.parseDouble(wrkStr));
				}
			} else {
				cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
				cellValue.setCellStyle(style);
				if (rowIndexInCell==0) {
					if (object.getBasicType().equals("STRING")) {
						if (object.isImage()) {
							style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
							style.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);
							style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
							HSSFPatriarch patriarch = workSheet.createDrawingPatriarch();
							HSSFClientAnchor anchor = null;
							cellValue.setCellStyle(style);
							cellValue.setCellValue(new HSSFRichTextString((String)object.getInternalValue()));
							int imageType = -1;
							String fileName = (String)object.getExternalValue();
							File imageFile = new File(fileName);
							if (imageFile.exists()) {
								boolean isValidFileType = false;
								if (fileName.contains(".png") || fileName.contains(".PNG")) {
									imageType = HSSFWorkbook.PICTURE_TYPE_PNG;
									isValidFileType = true;
								}
								if (fileName.contains(".jpg") || fileName.contains(".JPG") || fileName.contains(".jpeg") || fileName.contains(".JPEG")) {
									imageType = HSSFWorkbook.PICTURE_TYPE_JPEG;
									isValidFileType = true;
								}
								if (isValidFileType) {
									int pictureIndex;
									FileInputStream fis = null;
									ByteArrayOutputStream bos = null;
									try {
										// read in the image file
										fis = new FileInputStream(imageFile);
										bos = new ByteArrayOutputStream( );
										int c;
										// copy the image bytes into the ByteArrayOutputStream
										while ((c = fis.read()) != -1) {
											bos.write(c);
										}
										// add the image bytes to the workbook
										pictureIndex = workBook.addPicture(bos.toByteArray(), imageType);
										anchor = new HSSFClientAnchor(0,0,0,0,(short)2,currentRowNumber,(short)8,currentRowNumber + object.getRows());
										anchor.setAnchorType(0);
										anchor.setDx1(30);
										anchor.setDy1(30);
										anchor.setDx2(-30);
										anchor.setDy2(-250);
										patriarch.createPicture(anchor, pictureIndex);
										//
									} catch(Exception e) {
										e.printStackTrace(exceptionStream);
									} finally {
										try {
											if (fis != null) {
												fis.close();
											}
											if (bos != null) {
												bos.close();
											}
										} catch(IOException e) {
											e.printStackTrace(exceptionStream);
										}
									}
								}
							}
						} else {
							cellValue.setCellStyle(style);
							cellValue.setCellValue(new HSSFRichTextString((String)object.getExternalValue()));
						}
					}
					if (object.getBasicType().equals("DATE")) {
						java.util.Date utilDate = XFUtility.convertDateFromStringToUtil((String)object.getInternalValue());
						String text = XFUtility.getUserExpressionOfUtilDate(utilDate, session_.getDateFormat(), false);
						cellValue.setCellValue(new HSSFRichTextString(text));
					}
					if (object.getBasicType().equals("DATETIME") || object.getBasicType().equals("TIME")) {
						cellValue.setCellValue(new HSSFRichTextString(object.getInternalValue().toString()));
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

	public HashMap<String, Object> getParmMap() {
		return parmMap_;
	}
	
	public void setProcessLog(String text) {
		XFUtility.appendLog(text, processLog);
	}

	public HashMap<String, Object> getReturnMap() {
		return returnMap_;
	}

	public String getScriptNameRunning() {
		return scriptNameRunning;
	}

	public org.w3c.dom.Element getFunctionElement() {
		return functionElement_;
	}

	public String getFunctionInfo() {
		return jLabelFunctionID.getText();
	}

	//public StringBuffer getProcessLog() {
	//	return processLog;
	//}

	public Session getSession() {
		return session_;
	}

	public PrintStream getExceptionStream() {
		return exceptionStream;
	}

	public Bindings getEngineScriptBindings() {
		return 	engineScriptBindings;
	}
	
	public void evalScript(String scriptName, String scriptText) throws ScriptException {
		if (!scriptText.equals("")) {
			scriptNameRunning = scriptName;
			scriptEngine.eval(scriptText);
		}
	}

	public String getPrimaryTableID() {
		return primaryTable_.getTableID();
	}

	public ArrayList<XF210_Field> getFieldList() {
		return fieldList;
	}

	public ArrayList<String> getKeyFieldList() {
		return primaryTable_.getKeyFieldList();
	}

	public ArrayList<XF210_ReferTable> getReferTableList() {
		return referTableList;
	}

	public boolean existsInColumnList(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < fieldList.size(); i++) {
			if (tableID.equals("")) {
				if (fieldList.get(i).getTableAlias().equals(tableAlias)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (fieldList.get(i).getTableID().equals(tableID) && fieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (fieldList.get(i).getTableID().equals(tableID) && fieldList.get(i).getTableAlias().equals(tableAlias) && fieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public String getTableIDOfTableAlias(String tableAlias) {
		String tableID = tableAlias;
		XF210_ReferTable referTable;
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


class XF210_Field extends JPanel implements XFScriptableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	org.w3c.dom.Element functionFieldElement_ = null;
	org.w3c.dom.Element tableElement = null;
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
	private JPanel jPanelField = new JPanel();
	private JLabel jLabelField = new JLabel();
	private JLabel jLabelFieldComment = new JLabel();
	private XFTextField xFTextField = null;
	private XFImageField xFImageField = null;
	private XFDateField xFDateField = null;
	private XF210_PromptCallField xFPromptCallField = null;
	private XFTextArea xFTextArea = null;
	private XF210_ComboBox xFComboBox = null;
	private XFUrlField xFUrlField = null;
	private XFYMonthBox xFYMonthBox = null;
	private XFMSeqBox xFMSeqBox = null;
	private XFFYearBox xFFYearBox = null;
	private XFEditableField component = null;
	private String errorMessage = "";
	private boolean isNullable = true;
	private boolean isKey = false;
	private boolean isFieldOnPrimaryTable = false;
	private boolean isError = false;
	private boolean isVisibleOnPanel = true;
	private boolean isVirtualField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private boolean isHorizontal = false;
	private boolean isImage = false;
	private boolean isEditable_ = true;
	private int positionMargin = 0;
	private org.w3c.dom.Element promptFunctionElement_ = null;
	private Color foreground = Color.black;
	private XF210 dialog_;

	public XF210_Field(org.w3c.dom.Element functionFieldElement, XF210 dialog){
		super();
		//
		dialog_ = dialog;
		functionFieldElement_ = functionFieldElement;
		fieldOptions = functionFieldElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);
		//
		dataSourceName = functionFieldElement_.getAttribute("DataSource");
		StringTokenizer workTokenizer = new StringTokenizer(dataSourceName, "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog_.getTableIDOfTableAlias(tableAlias_);
		fieldID_ =workTokenizer.nextToken();
		String wrkStr;
		//
		boolean isEditable = true;
		if (tableID_.equals(dialog_.getPrimaryTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnPrimaryTable = true;
			//
			ArrayList<String> keyNameList = dialog_.getKeyFieldList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					isEditable = false;
					break;
				}
			}
		} else {
			isEditable = false;
		}
		//
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
		//
		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + res.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		//if (dataSize > 50) {
		//	dataSize = 50;
		//}
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = wrkStr;
		}
		jLabelField.setText(fieldCaption);
		jLabelField.setPreferredSize(new Dimension(120, XFUtility.FIELD_UNIT_HEIGHT));
		jLabelField.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelField.setVerticalAlignment(SwingConstants.TOP);
		FontMetrics metrics = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		if (metrics.stringWidth(fieldCaption) > 120) {
			jLabelField.setFont(new java.awt.Font("Dialog", 0, 12));
			metrics = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 12));
			if (metrics.stringWidth(fieldCaption) > 120) {
				jLabelField.setFont(new java.awt.Font("Dialog", 0, 10));
			} else {
				jLabelField.setFont(new java.awt.Font("Dialog", 0, 12));
			}
		} else {
			jLabelField.setFont(new java.awt.Font("Dialog", 0, 14));
		}
		//
		if (fieldOptionList.contains("PROMPT_LIST")) {
			isEditable = true;
			//
			XF210_ReferTable referTable = null;
			ArrayList<XF210_ReferTable> referTableList = dialog_.getReferTableList();
			for (int i = 0; i < referTableList.size(); i++) {
				if (referTableList.get(i).getTableID().equals(tableID_)) {
					if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias_)) {
						referTable = referTableList.get(i);
						break;
					}
				}
			}
			//
			xFComboBox = new XF210_ComboBox(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, referTable, isNullable);
			xFComboBox.setLocation(5, 0);
			component = xFComboBox;
		} else {
			wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
			if (!wrkStr.equals("")) {
				isEditable = true;
				NodeList functionList = dialog_.getSession().getFunctionList();
				for (int k = 0; k < functionList.getLength(); k++) {
					workElement = (org.w3c.dom.Element)functionList.item(k);
					if (workElement.getAttribute("ID").equals(wrkStr)) {
						promptFunctionElement_ = workElement;
						break;
					}
				}
				//
				boolean isEditableInEditMode = false;
				if (this.isFieldOnPrimaryTable) {
					isEditableInEditMode = true;
				}
				xFPromptCallField = new XF210_PromptCallField(functionFieldElement_, promptFunctionElement_, isEditableInEditMode, dialog_);
				xFPromptCallField.setLocation(5, 0);
				component = xFPromptCallField;
			} else {
				if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN").equals("") || !XFUtility.getOptionValueWithKeyword(dataTypeOptions, "VALUES").equals("")) {
					xFComboBox = new XF210_ComboBox(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, null, isNullable);
					xFComboBox.setLocation(5, 0);
					component = xFComboBox;
				} else {
					if (dataType.equals("VARCHAR") || dataType.equals("LONG VARCHAR")) {
						xFTextArea = new XFTextArea(dataSize, dataTypeOptions, fieldOptions);
						xFTextArea.setLocation(5, 0);
						component = xFTextArea;
					} else {
						if (dataTypeOptionList.contains("URL")) {
							xFUrlField = new XFUrlField(dataSize);
							xFUrlField.setLocation(5, 0);
							component = xFUrlField;
						} else {
							if (dataTypeOptionList.contains("IMAGE")) {
								xFImageField = new XFImageField(fieldOptions, dialog_.getSession().getImageFileFolder());
								xFImageField.setLocation(5, 0);
								component = xFImageField;
								isImage = true;
							} else {
								if (dataType.equals("DATE")) {
									xFDateField = new XFDateField(dialog_.getSession());
									xFDateField.setLocation(5, 0);
									component = xFDateField;
								} else {
									if (dataTypeOptionList.contains("YMONTH")) {
										xFYMonthBox = new XFYMonthBox(dialog_.getSession());
										xFYMonthBox.setLocation(5, 0);
										component = xFYMonthBox;
									} else {
										if (dataTypeOptionList.contains("MSEQ")) {
											xFMSeqBox = new XFMSeqBox(dialog_.getSession());
											xFMSeqBox.setLocation(5, 0);
											component = xFMSeqBox;
										} else {
											if (dataTypeOptionList.contains("FYEAR")) {
												xFFYearBox = new XFFYearBox(dialog_.getSession());
												xFFYearBox.setLocation(5, 0);
												component = xFFYearBox;
											} else {
												xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions);
												xFTextField.setLocation(5, 0);
												component = xFTextField;
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
		jPanelField.setLayout(null);
		jPanelField.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		jPanelField.add((JComponent)component);
		((JComponent)component).addKeyListener(new XF210_Component_keyAdapter(dialog_));
		//
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		this.add(jPanelField, BorderLayout.CENTER);
		if (fieldOptionList.contains("NO_CAPTION")) {
			this.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		} else {
			this.setPreferredSize(new Dimension(component.getWidth() + 130, component.getHeight()));
			this.add(jLabelField, BorderLayout.WEST);
		}
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "COMMENT");
		if (!wrkStr.equals("")) {
			jLabelFieldComment.setText(wrkStr);
			jLabelFieldComment.setForeground(Color.gray);
			jLabelFieldComment.setFont(new java.awt.Font("Dialog", 0, 12));
			jLabelFieldComment.setVerticalAlignment(SwingConstants.TOP);
			metrics = jLabelFieldComment.getFontMetrics(new java.awt.Font("Dialog", 0, 12));
			this.setPreferredSize(new Dimension(this.getPreferredSize().width + metrics.stringWidth(wrkStr), this.getPreferredSize().height));
			this.add(jLabelFieldComment, BorderLayout.EAST);
		}
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			autoNumberKey = wrkStr;
			isEditable = false;
			this.setEnabled(false);
		}
		//
		if (this.isFieldOnPrimaryTable) {
			Object value = dialog_.getParmMap().get(fieldID_);
			if (value != null) {
				this.setValue(value);
			}
		}
		//
//		if (!this.isEditable) {
//			component.setEditable(false);
//		}
		this.setEditable(isEditable);
		//
		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
		this.setFocusable(true);
		this.addFocusListener(new ComponentFocusListener());
	}

	public XF210_Field(String tableID, String tableAlias, String fieldID, XF210 dialog){
		super();
		//
		String wrkStr;
		functionFieldElement_ = null;
		fieldOptions = "";
		fieldOptionList = new ArrayList<String>();
		//
		isVisibleOnPanel = false;
		//
		tableID_ = tableID;
		fieldID_ = fieldID;
		dialog_ = dialog;
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}
		dataSourceName = tableAlias_ + "." + fieldID_;
		//
		if (tableID_.equals(dialog_.getPrimaryTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnPrimaryTable = true;
			//
			ArrayList<String> keyNameList = dialog_.getKeyFieldList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
		} else {
			isEditable_ = false;
		}
		//
		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + res.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		//if (dataSize > 50) {
		//	dataSize = 50;
		//}
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			StringTokenizer workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}
		//
		xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions);
		xFTextField.setLocation(5, 0);
		component = xFTextField;
		//
		fieldRows = component.getRows();
		jPanelField.setLayout(null);
		jPanelField.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		jPanelField.add((JComponent)component);
		//
		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(component.getWidth() + 130, component.getHeight()));
		this.add(jPanelField, BorderLayout.CENTER);
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			autoNumberKey = wrkStr;
			isEditable_ = false;
			this.setEnabled(false);
		}
		//
		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
	}

	public void checkPromptKeyEdit(){
		if (!XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL").equals("")) {
			if (xFPromptCallField.hasEditControlledKey()) {
				this.setEditable(false);
			}
		}
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

	public String getFieldIDInScript(){
		return tableAlias_ + "_" + fieldID_;
	}

	public String getTableAlias(){
		return tableAlias_;
	}

	public String getCaption(){
		return fieldCaption;
	}

	public String getTableID(){
		return tableID_;
	}

	public int getDecimalSize(){
		return decimalSize;
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

	public boolean isRangeKeyFieldValid(){
		return isRangeKeyFieldValid;
	}

	public boolean isRangeKeyFieldExpire(){
		return isRangeKeyFieldExpire;
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

	public boolean isFieldOnPrimaryTable(){
		return isFieldOnPrimaryTable;
	}

	public boolean isKey(){
		return isKey;
	}

	public boolean isEditable() {
		return component.isEditable();
	}

	public void setError(boolean error) {
		if (error) {
			isError = true;
			if (component.isEditable()) {
				component.setBackground(XFUtility.ERROR_COLOR);
			}
		} else {
			isError = false;
			this.errorMessage = "";
			if (component.isEditable()) {
				component.setBackground(XFUtility.ACTIVE_COLOR);
			} else {
				component.setBackground(XFUtility.INACTIVE_COLOR);
			}
		}
	}

	public void setEditable(boolean isEditable){
		this.isEditable_ = isEditable;
		//
		if (this.isEditable_) {
			if (!this.isKey()) {
				component.setEditable(true);
				component.setFocusable(true);
				if (this.isError()) {
					component.setBackground(XFUtility.ERROR_COLOR);
				} else {
					component.setBackground(XFUtility.ACTIVE_COLOR);
				}
			}
		} else {
			component.setEditable(false);
			component.setFocusable(false);
			component.setBackground(XFUtility.INACTIVE_COLOR);
		}
	}

	public void setValueOfResultSet(ResultSet result){
		try {
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					component.setValue(XFUtility.calculateExpireValue(this.getTableElement(), result, dialog_.getSession()));
				}
			} else {
				String basicType = this.getBasicType();
				//
				if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
					String value = result.getString(this.getFieldID());
					if (this.isFieldOnPrimaryTable) {
						if (value == null) {
							component.setValue("0");
						} else {
							component.setValue(result.getString(this.getFieldID()));
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
								component.setValue(Integer.parseInt(value));
							}
						}
						if (basicType.equals("FLOAT")) {
							if (value == null || value.equals("")) {
								component.setValue("");
							} else {
								component.setValue(Float.parseFloat(value));
							}
						}
					}
				}
				//
				if (basicType.equals("STRING") || basicType.equals("TIME") || basicType.equals("DATETIME")) {
					String value = result.getString(this.getFieldID());
					if (value == null) {
						component.setValue("");
					} else {
						component.setValue(value.trim());
					}
				}
				//
				if (basicType.equals("DATE")) {
					component.setValue(result.getDate(this.getFieldID()));
				}
				//
				component.setOldValue(component.getInternalValue());
			}
		} catch(SQLException e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public void setValue(Object object){
		XFUtility.setValueToEditableField(this.getBasicType(), object, component);
	}

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public boolean isNull(){
		return XFUtility.isNullValue(this.getBasicType(), component.getInternalValue());
	}

	public void setOldValue(Object object){
		XFUtility.setOldValueToEditableField(this.getBasicType(), object, component);
	}

	public Object getOldValue(){
		Object returnObj = null;
		//
		if (this.getBasicType().equals("INTEGER")) {
			returnObj = Integer.parseInt((String)component.getOldValue());
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				returnObj = Float.parseFloat((String)component.getOldValue());
			} else {
				if (component.getOldValue() == null) {
					returnObj = "";
				} else {
					returnObj = (String)component.getOldValue();
				}
			}
		}
		//
		return returnObj;
	}

	public Object getExternalValue(){
		Object returnObj = null;
		String basicType = this.getBasicType();
		//
		if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
			String value = (String)component.getExternalValue();
			if (value == null) {
				value = "0";
			}
			returnObj = value;
		}
		//
		if (basicType.equals("STRING")) {
			String value = (String)component.getExternalValue();
			if (value == null) {
				value = "";
			}
			returnObj = value;
		}
		//
		if (basicType.equals("DATE")) {
			returnObj = (java.sql.Date)component.getExternalValue();
		}
		//
		return returnObj;
	}

	public String getAutoNumber() {
		String value = "";
		if (!autoNumberKey.equals("")) {
			value = dialog_.getSession().getNextNumber(autoNumberKey);
		}
		return value;
	}

	public boolean isAutoNumberField() {
		return !autoNumberKey.equals("");
	}

	public boolean isError() {
		return isError;
	}

	public boolean isNullError(){
		String basicType = this.getBasicType();
		boolean isNullError = false;
		//
		if (this.isEditable() && this.isVisibleOnPanel && this.isFieldOnPrimaryTable) {
			//
			if (basicType.equals("INTEGER")) {
				int value = Integer.parseInt((String)component.getInternalValue());
				if (!this.isNullable) {
					if (value == 0) {
						isNullError = true;
					}
				}
			}
			//
			if (basicType.equals("FLOAT")) {
				double value = Double.parseDouble((String)component.getInternalValue());
				if (!this.isNullable) {
					if (value == 0) {
						isNullError = true;
					}
				}
			}
			//
			if (basicType.equals("DATE")) {
				String strDate = (String)component.getInternalValue();
				if (!this.isNullable) {
					if (strDate == null || strDate.equals("")) {
						isNullError = true;
					}
				}
			}
			//
			if (basicType.equals("STRING")) {
				String strWrk = (String)component.getInternalValue();
				if (!this.isNullable) {
					if (strWrk.equals("")) {
						isNullError = true;
					}
				}
				if (dataTypeOptionList.contains("YMONTH") && strWrk.length() > 0 && strWrk.length() < 6) {
					isNullError = true;
				}
				if (dataTypeOptionList.contains("FYEAR") && strWrk.length() > 0 && strWrk.length() < 4) {
					isNullError = true;
				}
			}
			//
			if (isNullError) { 
				//this.setError(true);
				//msgs.add(this.getCaption() + res.getString("Colon") + res.getString("FunctionError16"));
				this.setError(res.getString("FunctionError16"));
			}
		}
		//
		return isNullError;
	}

	public Object getInternalValue(){
		Object returnObj = null;
		String basicType = this.getBasicType();
		//
		if (basicType.equals("INTEGER") || basicType.equals("FLOAT") || basicType.equals("STRING")) {
			String value = (String)component.getInternalValue();
			if (value == null) {
				value = "";
			}
			returnObj = value;
		}
		//
		if (basicType.equals("DATE")) {
			returnObj = (String)component.getInternalValue();
		}
		//
		if (basicType.equals("DATETIME")) {
			returnObj = (String)component.getInternalValue();
		}
		//
		return returnObj;
	}

	public Object getValue() {
		Object returnObj = null;
		//
		if (this.getBasicType().equals("INTEGER")) {
			returnObj = Integer.parseInt((String)component.getInternalValue());
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				returnObj = Float.parseFloat((String)component.getInternalValue());
			} else {
				if (component.getInternalValue() == null) {
					returnObj = "";
				} else {
					returnObj = (String)component.getInternalValue();
				}
			}
		}
		//
		return returnObj;
	}

	public void setError(String message) {
		if (!message.equals("") && this.errorMessage.equals("")) {
			setError(true);
			this.errorMessage = message;
		}
	}

	public String getError() {
		return errorMessage;
	}

	public void setColor(String color) {
		foreground = XFUtility.convertStringToColor(color);
		component.setForeground(foreground);
	}

	public String getColor() {
		return XFUtility.convertColorToString(foreground);
	}

	class ComponentFocusListener implements FocusListener{
		public void focusLost(FocusEvent event){
		}
		public void focusGained(FocusEvent event){
			if (isEditable_) {
				component.requestFocus();
			} else {
				transferFocus();
			}
		}
	}
}

class XF210_ComboBox extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private String dataTypeOptions_ = "";
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private int rows_ = 1;
	private String listType = "";
	private ArrayList<String> kubunKeyValueList = new ArrayList<String>();
	private ArrayList<XFHashMap> tableKeyValuesList = new ArrayList<XFHashMap>();
	private JTextField jTextField = new JTextField();
	private JPanel jPanelDummy = new JPanel();
	private JComboBox jComboBox = new JComboBox();
	private boolean isEditable = true;
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private XF210_ReferTable referTable_ = null;
	private XF210 dialog_;
	private String oldValue = "";
	
	public XF210_ComboBox(String dataSourceName, String dataTypeOptions, XF210 dialog, XF210_ReferTable chainTable, boolean isNullable){
		//
		super();
		//
		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;
		int fieldWidth = 0;
		String wrk = "";
		String strWrk;
		//
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
		//
		jTextField.setFont(new java.awt.Font("Dialog", 0, 14));
		jTextField.setEditable(false);
		jTextField.setFocusable(false);
		FontMetrics metrics = jTextField.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		jComboBox.setFont(new java.awt.Font("Dialog", 0, 14));
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
				if (referTable_ != null && isEditable) {
					referTable_.setKeyFieldValues(tableKeyValuesList.get(jComboBox.getSelectedIndex()));
				}
			}
		});
		jComboBox.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				setupRecordList();
			}
		});
		jPanelDummy.setPreferredSize(new Dimension(17, XFUtility.FIELD_UNIT_HEIGHT));
		//
		strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "VALUES");
		if (!strWrk.equals("")) {
			//
			listType = "VALUES_LIST";
			//
			if (isNullable) {
				jComboBox.addItem("");
			}
			//
			workTokenizer = new StringTokenizer(strWrk, ";" );
			while (workTokenizer.hasMoreTokens()) {
				wrk = workTokenizer.nextToken();
				jComboBox.addItem(wrk);
				if (metrics.stringWidth(wrk) > fieldWidth) {
					fieldWidth = metrics.stringWidth(wrk);
				}
			}
		} else {
			strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "KUBUN");
			if (!strWrk.equals("")) {
				//
				listType = "KUBUN_LIST";
				//
				if (isNullable) {
					kubunKeyValueList.add("");
					jComboBox.addItem("");
				}
				//
				try {
					Statement statement = dialog_.getSession().getConnection().createStatement();
					ResultSet result = statement.executeQuery("select KBUSERKUBUN,TXUSERKUBUN  from " + dialog_.getSession().getTableNameOfUserVariants() + " where IDUSERKUBUN = '" + strWrk + "' order by SQLIST");
					while (result.next()) {
						//
						kubunKeyValueList.add(result.getString("KBUSERKUBUN").trim());
						wrk = result.getString("TXUSERKUBUN").trim();
						jComboBox.addItem(wrk);
						if (metrics.stringWidth(wrk) > fieldWidth) {
							fieldWidth = metrics.stringWidth(wrk);
						}
					}
					result.close();
					//
					if (jComboBox.getItemCount() == 0) {
						JOptionPane.showMessageDialog(null, res.getString("FunctionError24") + dataSourceName + res.getString("FunctionError25"));
					}
				} catch(SQLException e) {
					e.printStackTrace(dialog_.getExceptionStream());
					dialog_.setErrorAndCloseFunction();
				}
			} else {
				//
				if (referTable_ != null) {
					//
					listType = "RECORDS_LIST";
					//
					keyFieldList = referTable_.getKeyFieldIDList();
					//
					workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
					if (workElement == null) {
						JOptionPane.showMessageDialog(this, tableID + "." + fieldID + res.getString("FunctionError11"));
					}
					ArrayList<String> workDataTypeOptionList = XFUtility.getOptionList(workElement.getAttribute("TypeOptions"));
					int dataSize = Integer.parseInt(workElement.getAttribute("Size"));
					if (workDataTypeOptionList.contains("KANJI")) {
						fieldWidth = dataSize * 14 + 10;
					} else {
						fieldWidth = dataSize * 7 + 10;
					}
					if (fieldWidth > 800) {
						fieldWidth = 800;
					}
				}
			}
		}
		//
		this.setSize(new Dimension(fieldWidth + 30, XFUtility.FIELD_UNIT_HEIGHT));
		this.setLayout(new BorderLayout());
		this.add(jComboBox, BorderLayout.CENTER);
		//this.setFocusable(false);
		this.addFocusListener(new ComponentFocusListener());
	}

	public void setupRecordList() {
		if (referTable_ != null && listType.equals("RECORDS_LIST")) {
			//
			boolean blankItemRequired = true;
			XFHashMap keyValues = new XFHashMap();
			for (int i = 0; i < referTable_.getWithKeyFieldIDList().size(); i++) {
				for (int j = 0; j < dialog_.getFieldList().size(); j++) {
					if (referTable_.getWithKeyFieldIDList().get(i).equals(dialog_.getFieldList().get(j).getTableAlias() + "." + dialog_.getFieldList().get(j).getFieldID())) {
						if (dialog_.getFieldList().get(j).isNullable()) {
							//keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getFieldList().get(j).getNullValue());
							if (dialog_.getFieldList().get(j).isVisibleOnPanel()) {
								keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getFieldList().get(j).getValue());
							} else {
								keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getFieldList().get(j).getNullValue());
							}
						} else {
							blankItemRequired = false;
							break;
						}
					}
				}
				if (!blankItemRequired) {
					break;
				}
			}
			if (blankItemRequired) {
				tableKeyValuesList.add(keyValues);
				jComboBox.addItem("");
			}
			//
			try {
				String wrk = "";
				String sql = referTable_.getSelectSQL(true);
				Statement statement = dialog_.getSession().getConnection().createStatement();
				ResultSet result = statement.executeQuery(sql);
				while (result.next()) {
					if (referTable_.isRecordToBeSelected(result)) {
						//
						keyValues = new XFHashMap();
						for (int i = 0; i < keyFieldList.size(); i++) {
							keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), result.getString(keyFieldList.get(i)));
						}
						tableKeyValuesList.add(keyValues);
						//
						wrk = result.getString(fieldID).trim();
						jComboBox.addItem(wrk);
					}
				}
				result.close();
			} catch(SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
	}
	
	public void setEditable(boolean editable) {
		this.removeAll();
		if (editable) {
			this.add(jComboBox, BorderLayout.CENTER);
		} else {
			this.add(jPanelDummy, BorderLayout.EAST);
			this.add(jTextField, BorderLayout.CENTER);
		}
		isEditable = editable;
	}

	public Object getInternalValue() {
		String value = "";
		//
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
		//
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
	
	public void setValue(Object obj) {
		String value = (String)obj;
		value = value.trim();
		//
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
			//
			if (jComboBox.getItemCount() == 0) {
				setupRecordList();
			}
			//
			if (jComboBox.getItemCount() > 0) {
				if (value == null || value.equals("")) {
					//jComboBox.setSelectedIndex(0);
				} else {
					for (int i = 0; i < jComboBox.getItemCount(); i++) {
						if (jComboBox.getItemAt(i).toString().equals(value)) {
							if (i != jComboBox.getSelectedIndex()) {
								jComboBox.setSelectedIndex(i);
							}
							break;
						}
					}
				}
			}
		}
		//
		if (jComboBox.getSelectedIndex() >= 0) {
			jTextField.setText(jComboBox.getSelectedItem().toString());
		}
	}

	public void setBackground(Color color) {
		if (jComboBox != null) {
			jComboBox.setBackground(color);
		}
	}

	public int getRows() {
		return rows_;
	}

	class ComponentFocusListener implements FocusListener{
		public void focusLost(FocusEvent event){
		}
		public void focusGained(FocusEvent event){
			jComboBox.requestFocus();
		}
	}
}

class XF210_PromptCallField extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private int rows_ = 1;
	private XFTextField xFTextField;
	private JButton jButton = new JButton();
	private boolean isEditable = true;
	private boolean isEditableInEditMode_ = false;
    private XF210 dialog_;
    private org.w3c.dom.Element functionElement_;
    private org.w3c.dom.Element fieldElement_;
    private ArrayList<XF210_ReferTable> referTableList_;
    private String oldValue = "";
    //private Color normalColor;
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();

	public XF210_PromptCallField(org.w3c.dom.Element fieldElement, org.w3c.dom.Element functionElement, boolean isEditableInEditMode, XF210 dialog){
		//
		super();
		//
		fieldElement_ = fieldElement;
		functionElement_ = functionElement;
		isEditableInEditMode_ = isEditableInEditMode;
		dialog_ = dialog;
		//
		String fieldOptions = fieldElement_.getAttribute("FieldOptions");
		StringTokenizer workTokenizer = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		fieldID =workTokenizer.nextToken();
		//
		tableID = tableAlias;
		referTableList_ = dialog_.getReferTableList();
		for (int i = 0; i < referTableList_.size(); i++) {
			if (referTableList_.get(i).getTableAlias().equals(tableAlias)) {
				tableID = referTableList_.get(i).getTableID();
				break;
			}
		}
		//
		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID + "." + fieldID + res.getString("FunctionError11"));
		}
		String dataType = workElement.getAttribute("Type");
		String dataTypeOptions = workElement.getAttribute("TypeOptions");
		int dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (dataSize > 50) {
			dataSize = 50;
		}
		int decimalSize = 0;
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		//
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
		//
		xFTextField = new XFTextField(XFUtility.getBasicTypeOf(dataType), dataSize, decimalSize, dataTypeOptions, fieldOptions);
		xFTextField.setLocation(5, 0);
		//normalColor = xFTextField.getBackground();
		//
		jButton.setText("...");
		jButton.setFont(new java.awt.Font("Dialog", 0, 11));
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value;
				//
				HashMap<String, Object> fieldValuesMap = new HashMap<String, Object>();
				for (int i = 0; i < fieldsToPutList_.size(); i++) {
					value = dialog_.getValueOfFieldByName(fieldsToPutList_.get(i));
					if (value != null) {
						fieldValuesMap.put(fieldsToPutToList_.get(i), value);
					}
				}
				//
				HashMap<String, Object> returnMap = dialog_.getSession().getFunction().execute(functionElement_, fieldValuesMap);
				if (!returnMap.get("RETURN_CODE").equals("99")) {
					HashMap<String, Object> fieldsToGetMap = new HashMap<String, Object>();
					for (int i = 0; i < fieldsToGetList_.size(); i++) {
						value = returnMap.get(fieldsToGetList_.get(i));
						if (value != null) {
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
		//
		this.setSize(new Dimension(xFTextField.getWidth() + 27, XFUtility.FIELD_UNIT_HEIGHT));
		this.setLayout(new BorderLayout());
		this.add(xFTextField, BorderLayout.CENTER);
		this.setFocusable(true);
		this.addFocusListener(new ComponentFocusListener());
	}
	
	public boolean hasEditControlledKey() {
		boolean anyOfKeysAreEditControlled = false;
		//
		for (int i = 0; i < fieldsToGetToList_.size(); i++) {
			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
				if (fieldsToGetToList_.get(i).equals(dialog_.getFieldList().get(j).getTableAlias() + "." + dialog_.getFieldList().get(j).getFieldID())) {
					if (!dialog_.getFieldList().get(j).isEditable()
							&& dialog_.getPrimaryTableID().equals(dialog_.getFieldList().get(j).getTableAlias())) {
						anyOfKeysAreEditControlled = true;
						break;
					}
				}
			}
		}
		//
		return anyOfKeysAreEditControlled;
	}

	public void setEditable(boolean editable) {
		if (editable) {
			this.add(jButton, BorderLayout.EAST);
			xFTextField.setEditable(isEditableInEditMode_);
			xFTextField.setFocusable(isEditableInEditMode_);
		} else {
			this.remove(jButton);
			xFTextField.setEditable(false);
			xFTextField.setFocusable(false);
		}
		isEditable = editable;
	}

	//public void requestFocus() {
	//	xFTextField.requestFocus();
	//}

	public Object getInternalValue() {
		return xFTextField.getText();
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
			xFTextField.setText(obj.toString());
		}
	}

	public void setBackground(Color color) {
		if (xFTextField != null) {
			if (color.equals(XFUtility.ACTIVE_COLOR)) {
				//xFTextField.setBackground(normalColor);
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

	class ComponentFocusListener implements FocusListener{
		public void focusLost(FocusEvent event){
		}
		public void focusGained(FocusEvent event){
			//jButton.requestFocus();
			if (xFTextField.isEditable()) {
				xFTextField.requestFocus();
			} else {
				jButton.requestFocus();
			}
		}
	}
}

class XF210_PrimaryTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID = "";
	private String activeWhere = "";
	private String fixedWhere = "";
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private ArrayList<String> orderByFieldList = new ArrayList<String>();
	private ArrayList<String> uniqueKeyList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF210 dialog_;
	private StringTokenizer workTokenizer;
	private String updateCounterID = "";
	private int updateCounterValue = 0;

	public XF210_PrimaryTable(org.w3c.dom.Element functionElement, XF210 dialog){
		super();
		//
		functionElement_ = functionElement;
		dialog_ = dialog;
		//
		tableID = functionElement_.getAttribute("PrimaryTable");
		tableElement = dialog_.getSession().getTableElement(tableID);
		activeWhere = tableElement.getAttribute("ActiveWhere");
		updateCounterID = tableElement.getAttribute("UpdateCounter");
		if (updateCounterID.equals("")) {
			updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
		}
		fixedWhere = functionElement_.getAttribute("FixedWhere");
		//
		String workString;
		org.w3c.dom.Element workElement;
		//
		if (functionElement_.getAttribute("KeyFields").equals("")) {
			NodeList nodeList = tableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer.hasMoreTokens()) {
						workString = workTokenizer.nextToken();
						keyFieldList.add(workString);
					}
				}
			}
		} else {
			workTokenizer = new StringTokenizer(functionElement_.getAttribute("KeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				keyFieldList.add(workTokenizer.nextToken());
			}
		}
		//
		NodeList nodeList = tableElement.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("SK")) {
				uniqueKeyList.add(workElement.getAttribute("Fields"));
			}
		}
		//
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldList.add(workTokenizer.nextToken());
		}
		//
		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID, element));
		}
	}
	
	public void setUpdateCounterValue(ResultSet result) {
		try {
			updateCounterValue = result.getInt(updateCounterID);
		} catch(SQLException e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}
	
	public String getSQLToSelect(){
		StringBuffer buf = new StringBuffer();
		//
		buf.append("select ");
		//
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
		buf.append(",");
		buf.append(updateCounterID);
		//
		buf.append(" from ");
		buf.append(tableID);
		//
		if (orderByFieldList.size() > 0) {
			buf.append(" order by ");
			for (int i = 0; i < orderByFieldList.size(); i++) {
				if (i > 0) {
					buf.append(",");
				}
				buf.append(orderByFieldList.get(i));
			}
		}
		//
		buf.append(" where ") ;
		//
		int orderOfFieldInKey = 0;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (orderOfFieldInKey > 0) {
					buf.append(" and ") ;
				}
				buf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				buf.append("=") ;
				if (XFUtility.isLiteralRequiredBasicType(dialog_.getFieldList().get(i).getBasicType())) {
					buf.append("'") ;
					buf.append(dialog_.getFieldList().get(i).getInternalValue()) ;
					buf.append("'") ;
				} else {
					buf.append(dialog_.getFieldList().get(i).getInternalValue()) ;
				}
				orderOfFieldInKey++;
			}
		}
		//
		if (!activeWhere.equals("")) {
			buf.append(" and (");
			buf.append(activeWhere);
			buf.append(") ");
		}
		//
		if (!fixedWhere.equals("")) {
			buf.append(" and (");
			buf.append(fixedWhere);
			buf.append(") ");
		}
		//
		return buf.toString();
	}

	String getSQLToInsert() {
		StringBuffer statementBuf = new StringBuffer();
		//
		statementBuf.append("insert into ");
		statementBuf.append(tableID);
		statementBuf.append(" (");
		//
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
		//
		statementBuf.append(") values(") ;
		//
		firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isFieldOnPrimaryTable()
			&& !dialog_.getFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ") ;
				}
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue()));
				firstField = false;
			}
		}
		//
		statementBuf.append(")") ;
		//
		return statementBuf.toString();
	}

	String getSQLToCheckPKDuplication() {
		StringBuffer statementBuf = new StringBuffer();
		//
		statementBuf.append("select ");
		//
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
		//
		statementBuf.append(" from ");
		statementBuf.append(tableID);
		statementBuf.append(" where ") ;
		//
		firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (!firstField) {
					statementBuf.append(" and ") ;
				}
				statementBuf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				statementBuf.append("=") ;
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue()));
				firstField = false;
			}
		}
		//
		return statementBuf.toString();
	}
	
	boolean hasPrimaryKeyValueAltered() {
		boolean altered = false;
		//
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (!dialog_.getFieldList().get(i).getValue().equals(dialog_.getFieldList().get(i).getOldValue())) {
					altered = true;
				}
			}
		}
		//
		return altered;
	}

	public ArrayList<String> getUniqueKeyList() {
		return uniqueKeyList;
	}
	
	public String getSQLToCheckSKDuplication(ArrayList<String> keyFieldList) {
		StringBuffer statementBuf = new StringBuffer();
		//
		statementBuf.append("select ");
		//
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
		//
		statementBuf.append(" from ");
		statementBuf.append(tableID);
		statementBuf.append(" where ") ;
		//
		firstField = true;
		for (int j = 0; j < dialog_.getFieldList().size(); j++) {
			if (dialog_.getFieldList().get(j).isFieldOnPrimaryTable()) {
				for (int p = 0; p < keyFieldList.size(); p++) {
					if (dialog_.getFieldList().get(j).getFieldID().equals(keyFieldList.get(p))) {
						if (!firstField) {
							statementBuf.append(" and ") ;
						}
						statementBuf.append(dialog_.getFieldList().get(j).getFieldID()) ;
						statementBuf.append("=") ;
						statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(j).getBasicType(), dialog_.getFieldList().get(j).getInternalValue()));
						firstField = false;
					}
				}
			}
		}
		//
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
					statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(j).getBasicType(), dialog_.getFieldList().get(j).getInternalValue()));
					firstField = false;
				}
			}
		}
		statementBuf.append(")") ;
		//
		return statementBuf.toString();
	}

	String getSQLToUpdate() {
		StringBuffer statementBuf = new StringBuffer();
		//
		statementBuf.append("update ");
		statementBuf.append(tableID);
		statementBuf.append(" set ");
		//
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
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue()));
				firstField = false;
			}
		}
		statementBuf.append(", ") ;
		statementBuf.append(updateCounterID) ;
		statementBuf.append("=") ;
		statementBuf.append(updateCounterValue + 1) ;
		//
		statementBuf.append(" where ") ;
		//
		firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (!firstField) {
					statementBuf.append(" and ") ;
				}
				statementBuf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				statementBuf.append("=") ;
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue()));
				firstField = false;
			}
		}
		statementBuf.append(" and ") ;
		statementBuf.append(updateCounterID) ;
		statementBuf.append("=") ;
		statementBuf.append(updateCounterValue) ;
		//
		return statementBuf.toString();
	}

	String getSQLToDelete() {
		StringBuffer statementBuf = new StringBuffer();
		//
		statementBuf.append("delete from ");
		statementBuf.append(tableID);
		statementBuf.append(" where ") ;
		//
		boolean firstKey = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (!firstKey) {
					statementBuf.append(" and ") ;
				}
				statementBuf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				statementBuf.append("=") ;
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue()));
				firstKey = false;
			}
		}
		statementBuf.append(" and ") ;
		statementBuf.append(updateCounterID) ;
		statementBuf.append("=") ;
		statementBuf.append(updateCounterValue) ;
		//
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
		XF210_ReferTable referTable;
		org.w3c.dom.Element workElement;
		//
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
		//
		return isValid;
	}

	public int runScript(String event1, String event2) throws ScriptException {
		int countOfErrors = 0;
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		//
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		//
		if (validScriptList.size() > 0) {
			//
			for (int i = 0; i < validScriptList.size(); i++) {
				dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
			}
			//
			for (int i = 0; i < dialog_.getFieldList().size(); i++) {
				if (dialog_.getFieldList().get(i).isError()) {
					countOfErrors++;
				}
			}
		}
		//
		return countOfErrors;
	}
}

class XF210_ReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF210 dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private String activeWhere = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private boolean isOptional = false;
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private String rangeKeyFieldSearch = "";
	private boolean rangeValidated;

	public XF210_ReferTable(org.w3c.dom.Element referElement, XF210 dialog){
		super();
		//
		referElement_ = referElement;
		dialog_ = dialog;
		//
		tableID = referElement_.getAttribute("ToTable");
		tableElement = dialog_.getSession().getTableElement(tableID);
		//
		StringTokenizer workTokenizer;
		String wrkStr = tableElement.getAttribute("RangeKey");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			rangeKeyFieldValid =workTokenizer.nextToken();
			rangeKeyFieldExpire =workTokenizer.nextToken();
			org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				rangeKeyType = 1;
			} else {
				rangeKeyType = 2;
			}
		}
		//
		activeWhere = tableElement.getAttribute("ActiveWhere");
		//
		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			fieldIDList.add(workTokenizer.nextToken());
		}
		//
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
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("WithKeyFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withKeyFieldIDList.add(workTokenizer.nextToken());
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldIDList.add(workTokenizer.nextToken());
		}
		//
		if (referElement_.getAttribute("Optional").equals("T")) {
			isOptional = true;
		}
	}

	public String getSelectSQL(boolean isToGetRecordsForComboBox){
		org.w3c.dom.Element workElement;
		int count;
		StringBuffer buf = new StringBuffer();
		//
		buf.append("select ");
		//
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
		if (!rangeKeyFieldValid.equals("")) {
			if (count > 0) {
				buf.append(",");
			}
			buf.append(rangeKeyFieldValid);
			//
			workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				buf.append(",");
				buf.append(rangeKeyFieldExpire);
			}
		}
		//
		buf.append(" from ");
		buf.append(tableID);
		//
		StringTokenizer workTokenizer;
		String keyFieldID, keyFieldTableID;
		count = 0;
		boolean isToBeWithValue;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				rangeKeyFieldSearch = withKeyFieldIDList.get(i);
			} else {
				if (isToGetRecordsForComboBox) {
					//
					// Value of the field which has either of these conditions should be within WHERE to SELECT records: //
					// 1. The with-key-field is not editable //
					// 2. The with-key-field is part of PK of the primary table //
					// 3. The with-key-field is on the primary table and consists of upper part of with-key-fields //
					// 4. The with-key-field is part of PK of the other join table //
					for (int j = 0; j < dialog_.getFieldList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getDataSourceName())) {
							//
							isToBeWithValue = false;
							//
							if (!dialog_.getFieldList().get(j).isEditable()) {
								isToBeWithValue = true;
							} else {
								workTokenizer = new StringTokenizer(withKeyFieldIDList.get(i), "." );
								keyFieldTableID = workTokenizer.nextToken();
								keyFieldID = workTokenizer.nextToken();
								if (keyFieldTableID.equals(dialog_.getPrimaryTableID())) {
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
							//
							if (isToBeWithValue) {
								if (count == 0) {
									buf.append(" where ");
								} else {
									buf.append(" and ");
								}
								buf.append(toKeyFieldIDList.get(i));
								buf.append("=");
								//buf.append(dialog_.getFieldList().get(j).getTableOperationValue());
								buf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(j).getBasicType(), dialog_.getFieldList().get(j).getInternalValue())) ;
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
							buf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(j).getBasicType(), dialog_.getFieldList().get(j).getInternalValue())) ;
							break;
						}
					}
					count++;
				}
			}
		}
		//
		if (!activeWhere.equals("")) {
			if (count == 0) {
				buf.append(" where ");
			} else {
				buf.append(" and ");
			}
			buf.append(activeWhere);
		}
		//
		if (this.rangeKeyType != 0) {
			buf.append(" order by ");
			buf.append(rangeKeyFieldValid);
			buf.append(" DESC ");
		} else {
			if (orderByFieldIDList.size() > 0) {
				int pos0,pos1;
				buf.append(" order by ");
				for (int i = 0; i < orderByFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					pos0 = orderByFieldIDList.get(i).indexOf(".");
					pos1 = orderByFieldIDList.get(i).indexOf("(A)");
					if (pos1 >= 0) {
						buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
					} else {
						pos1 = orderByFieldIDList.get(i).indexOf("(D)");
						if (pos1 >= 0) {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
							buf.append(" DESC ");
						} else {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, orderByFieldIDList.get(i).length()));
						}
					}
				}
			}
		}
		//
		rangeValidated = false;
		//
		return buf.toString();
	}

	public String getTableID(){
		return tableID;
	}

	public String getTableAlias(){
		return tableAlias;
	}

	public boolean isOptional(){
		return isOptional;
	}

	public boolean isKeyNullable() {
		boolean isKeyNullable = false;
		//
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
		//
		return isKeyNullable;
	}

	public boolean isKeyNull() {
		boolean isKeyNull = false;
		//
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
		//
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
		//
		/////////////////////////////////////////////////
		// Set error on the visible editable key field //
		/////////////////////////////////////////////////
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
				if (dialog_.getFieldList().get(j).isVisibleOnPanel()
						&& dialog_.getFieldList().get(j).isEditable()
						&& dialog_.getFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
						&& !dialog_.getFieldList().get(j).isError()) {
					//dialog_.getFieldList().get(j).setError(dialog_.getFieldList().get(j).getCaption() + res.getString("Colon") + tableElement.getAttribute("Name") + res.getString("FunctionError45"));
					dialog_.getFieldList().get(j).setError(tableElement.getAttribute("Name") + res.getString("FunctionError45"));
					//dialog_.getMessageList().add(dialog_.getFieldList().get(j).getError());
					noneOfKeyFieldsWereSetError = false;
					break;
				}
			}
		}
		//
		if (noneOfKeyFieldsWereSetError) {
			//
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
						//dialog_.getFieldList().get(j).setError(dialog_.getFieldList().get(j).getCaption() + res.getString("Colon") + tableElement.getAttribute("Name") + res.getString("FunctionError45"));
						dialog_.getFieldList().get(j).setError(tableElement.getAttribute("Name") + res.getString("FunctionError45"));
						//dialog_.getMessageList().add(dialog_.getFieldList().get(j).getError());
						noneOfKeyFieldsWereSetError = false;
						break;
					}
				}
			}
		}
		//
		if (noneOfKeyFieldsWereSetError) {
			//
			//////////////////////////////////////
			// Set error on the first key field //
			//////////////////////////////////////
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getFieldList().size(); j++) {
					if (dialog_.getFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
							&& !dialog_.getFieldList().get(j).isError()) {
						//dialog_.getFieldList().get(j).setError(dialog_.getFieldList().get(j).getCaption() + res.getString("Colon") + tableElement.getAttribute("Name") + res.getString("FunctionError45"));
						dialog_.getFieldList().get(j).setError(tableElement.getAttribute("Name") + res.getString("FunctionError45"));
						//dialog_.getMessageList().add(dialog_.getFieldList().get(j).getError());
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
	
	public boolean isRecordToBeSelected(ResultSet result){
		boolean returnValue = false;
		//
		if (rangeKeyType == 0) {
			returnValue = true;
		}
		//
		if (rangeKeyType == 1) {
			try {
				if (!rangeValidated) { 
					// Note that result set is ordered by rangeKeyFieldValue DESC //
					Object valueKey = dialog_.getValueOfFieldByName(rangeKeyFieldSearch);
					Object valueFrom = result.getObject(rangeKeyFieldValid);
					int comp1 = valueKey.toString().compareTo(valueFrom.toString());
					if (comp1 >= 0) {
						returnValue = true;
						rangeValidated = true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
		//
		if (rangeKeyType == 2) {
			try {
				Object valueKey = dialog_.getValueOfFieldByName(rangeKeyFieldSearch);
				Object valueFrom = result.getObject(rangeKeyFieldValid);
				Object valueThru = result.getObject(rangeKeyFieldExpire);
				if (valueThru == null) {
					valueThru = "9999-12-31";
				}
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				int comp2 = valueKey.toString().compareTo(valueThru.toString());
				if (comp1 >= 0 && comp2 < 0) {
					returnValue = true;
				}
			} catch (SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
		//
		return returnValue;
	}
}

class XF210_KeyInputDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private JPanel jPanelMain = new JPanel();
	private Dimension scrSize;
	private JPanel jPanelKeyFields = new JPanel();
	private JPanel jPanelTop = new JPanel();
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private JPanel jPanelInfo = new JPanel();
	private GridLayout gridLayoutInfo = new GridLayout();
	private JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private JButton jButtonCancel = new JButton();
	private JButton jButtonOK = new JButton();
	private XF210 dialog_;
	private final int FIELD_VERTICAL_MARGIN = 5;
	private final int FONT_SIZE = 14;
	private ArrayList<XF210_Field> fieldList = new ArrayList<XF210_Field>();
	private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
	
	public XF210_KeyInputDialog(XF210 dialog) {
		super(dialog, "", true);
		dialog_ = dialog;
		initComponentsAndVariants();
	}

	void initComponentsAndVariants() {
		//
		scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		//
		jPanelMain.setLayout(new BorderLayout());
		jPanelTop.setLayout(new BorderLayout());
		jPanelKeyFields.setLayout(null);
		jPanelKeyFields.setFocusable(false);
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jScrollPaneMessages.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setLayout(new BorderLayout());
		jPanelBottom.setBorder(null);
		jLabelFunctionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelFunctionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelFunctionID.setForeground(Color.gray);
		jLabelFunctionID.setFocusable(false);
		jLabelSessionID.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelSessionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelSessionID.setForeground(Color.gray);
		jLabelSessionID.setFocusable(false);
		jPanelButtons.setBorder(null);
		jPanelButtons.setLayout(null);
		jPanelButtons.setFocusable(false);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
		gridLayoutInfo.setVgap(4);
		jPanelInfo.setLayout(gridLayoutInfo);
		jPanelInfo.add(jLabelSessionID);
		jPanelInfo.add(jLabelFunctionID);
		jPanelInfo.setFocusable(false);
		jPanelMain.add(jPanelTop, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelTop.add(jPanelKeyFields, BorderLayout.CENTER);
		jPanelTop.add(jScrollPaneMessages, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		jButtonCancel.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jButtonCancel.setText(res.getString("Cancel"));
		jButtonCancel.setBounds(new Rectangle(5, 2, 80, 32));
		jButtonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				keyMap_.clear();
				setVisible(false);
			}
		});
		jButtonOK.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jButtonOK.setText("OK");
		jButtonOK.setBounds(new Rectangle(270, 2, 80, 32));
		jButtonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean anyOfFieldsAreNull = false;
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).getInternalValue().equals(fieldList.get(i).getNullValue())) {
						anyOfFieldsAreNull = true;
						break;
					} else {
						keyMap_.put(fieldList.get(i).getFieldID(), fieldList.get(i).getInternalValue());
					}
				}
				if (anyOfFieldsAreNull) {
					jTextAreaMessages.setText(res.getString("FunctionError23"));
				} else {
					setVisible(false);
				}
			}
		});
		jPanelButtons.add(jButtonCancel);
		jPanelButtons.add(jButtonOK);
		this.getRootPane().setDefaultButton(jButtonOK);
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
		this.setSize(new Dimension(scrSize.width, scrSize.height));
		//
		StringTokenizer workTokenizer;
		String tableAlias, tableID, fieldID;
		org.w3c.dom.Element element;
		int posX = 0;
		int posY = 0;
		int biggestWidth = 0;
		int biggestHeight = 0;
		Dimension dim = new Dimension();
		Dimension dimOfPriviousField = new Dimension();
		boolean topField = true;
		XF210_Field field;
		//
		this.setTitle(dialog_.getTitle());
		jLabelSessionID.setText(dialog_.getSession().getSessionID());
		jLabelFunctionID.setText(dialog_.getFunctionInfo());
		FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));
		//
		NodeList fieldElementList = dialog_.getFunctionElement().getElementsByTagName("Field");
		SortableDomElementListModel sortingList1 = XFUtility.getSortedListModel(fieldElementList, "Order");
		for (int i = 0; i < sortingList1.getSize(); i++) {
			element = (org.w3c.dom.Element)sortingList1.getElementAt(i);
			workTokenizer = new StringTokenizer(element.getAttribute("DataSource"), "." );
			tableAlias = workTokenizer.nextToken();
			tableID = dialog_.getTableIDOfTableAlias(tableAlias);
			fieldID =workTokenizer.nextToken();
			//
			if (tableID.equals(dialog_.getPrimaryTableID())) {
				ArrayList<String> keyFieldList = dialog_.getKeyFieldList();
				for (int j = 0; j < keyFieldList.size(); j++) {
					if (keyFieldList.get(j).equals(fieldID)) {
						field = new XF210_Field((org.w3c.dom.Element)sortingList1.getElementAt(i), dialog_);
						field.setEditable(true);
						fieldList.add(field);
						//
						if (topField) {
							posX = 0;
							posY = this.FIELD_VERTICAL_MARGIN + 8;
							topField = false;
						} else {
							posX = 0;
							posY = posY + dimOfPriviousField.height+ field.getPositionMargin() + this.FIELD_VERTICAL_MARGIN;
						}
						dim = field.getPreferredSize();
						field.setBounds(posX, posY, dim.width, dim.height);
						jPanelKeyFields.add(field);
						//
						if (posX + dim.width > biggestWidth) {
							biggestWidth = posX + dim.width;
						}
						if (posY + dim.height > biggestHeight) {
							biggestHeight = posY + dim.height;
						}
						dimOfPriviousField = new Dimension(dim.width, dim.height);
					}
				}
			}
		}
		//
		int width = 450;
		if (biggestWidth > 430) {
			width = biggestWidth + 20;
		}
		int height = biggestHeight + 117;
		this.setPreferredSize(new Dimension(width, height));
		posX = (scrSize.width - width) / 2;
		posY = (scrSize.height - height) / 2;
		this.setLocation(posX, posY);
		this.pack();
	}
	
	public HashMap<String, Object> requestKeyValues(String message) {
		keyMap_.clear();
		if (message.equals("")) {
			jTextAreaMessages.setText(res.getString("FunctionMessage29"));
		} else {
			jTextAreaMessages.setText(message);
		}
		this.setVisible(true);
		//
		return keyMap_;
	}
}

class XF210_FunctionButton_actionAdapter implements java.awt.event.ActionListener {
	XF210 adaptee;
	XF210_FunctionButton_actionAdapter(XF210 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jFunctionButton_actionPerformed(e);
	}
}

class XF210_Component_keyAdapter extends java.awt.event.KeyAdapter {
	XF210 adaptee;
	XF210_Component_keyAdapter(XF210 adaptee) {
		this.adaptee = adaptee;
	}
	public void keyPressed(KeyEvent e) {
		adaptee.component_keyPressed(e);
	}
}
