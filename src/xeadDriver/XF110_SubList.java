package xeadDriver;

/*
 * Copyright (c) 2015 WATANABE kozo <qyf05466@nifty.com>,
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
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.*;
import javax.swing.*;

import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.w3c.dom.*;

public class XF110_SubList extends JDialog implements XFScriptable {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element functionElement_ = null;
	private Session session_ = null;
	private JPanel jPanelMain = new JPanel();
	private JPanel jPanelCenter = new JPanel();
	private JPanel jPanelBatchFields = new JPanel();
	private JScrollPane jScrollPaneBatchFields = new JScrollPane();
	private XF110_SubListBatchTable batchTable;
	private XF110_SubListBatchField firstEditableBatchField = null;
	private ArrayList<XF110_SubListBatchField> batchFieldList = new ArrayList<XF110_SubListBatchField>();
	private ArrayList<XF110_SubListBatchReferTable> batchReferTableList = new ArrayList<XF110_SubListBatchReferTable>();
	private NodeList batchReferElementList;
	private String batchFunctionID = "";
	private XF110_SubListDetailTable detailTable;
	private ArrayList<XF110_SubListDetailColumn> detailColumnList = new ArrayList<XF110_SubListDetailColumn>();
	private ArrayList<XF110_SubListDetailReferTable> detailReferTableList = new ArrayList<XF110_SubListDetailReferTable>();
	private ArrayList<String> batchWithKeyList = new ArrayList<String>();
	private ArrayList<String> batchKeyList = new ArrayList<String>();
	private TableModelEditableList tableModelMain = null;
	private JTable jTableMain = new JTable();
	private TableHeadersRenderer headersRenderer;
	private TableCellsRenderer cellsRenderer;
	private TableCellsEditor cellsEditor;
	private boolean isHeaderResizing = false;
	private String initialMsg = "";
	private NodeList detailReferElementList = null;
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private JPanel[] jPanelButtonArray = new JPanel[7];
	private Action checkAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			messageList.clear();
			checkErrorsToUpdate(true);
			setMessagesOnPanel();
		}
	};
	private Action tabAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			cellsEditor.transferFocusOfCell(-1, -1, true);
		}
	};
	private Action shiftTabAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			cellsEditor.transferFocusOfCell(-1, -1, false);
		}
	};
	private Action arrowUpAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			cellsEditor.requestFocusOnVerticalCell("UP");
		}
	};
	private Action arrowDownAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			cellsEditor.requestFocusOnVerticalCell("DOWN");
		}
	};
	private String buttonUpdateCaption = "";
	private JPanel jPanelInfo = new JPanel();
	private GridLayout gridLayoutButtons = new GridLayout();
	private GridLayout gridLayoutInfo = new GridLayout();
	private ArrayList<String> messageList = new ArrayList<String>();
	private JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JProgressBar jProgressBar = new JProgressBar();
	private JSplitPane jSplitPaneMain = new JSplitPane();
	private JScrollPane jScrollPaneTable = new JScrollPane();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private JButton[] jButtonArray = new JButton[7];
	private Color selectionColorWithFocus = new Color(49,106,197);
	private Color selectionColorWithoutFocus = new Color(213,213,213);
	private SortableDomElementListModel sortingList1;
	private SortableDomElementListModel sortingList2;
	private Action helpAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			session_.browseHelp();
		}
	};
	private Action[] actionButtonArray = new Action[7];
	private String[] actionDefinitionArray = new String[7];
	private ScriptEngine scriptEngine;
	private Bindings scriptBindings;
	private String scriptNameRunning = "";
	private boolean readyToShowDialog;
	private boolean isInvalid = false;
	private XF110 dialog_;
	private String reply_ = "";
	private KeyStroke keyStrokeToUpdate = null;
	private JCheckBox jCheckBoxToExecuteBatchFunction = new JCheckBox();
	private ReferChecker referChecker = null;
	private Thread threadToSetupReferChecker = null;

	public XF110_SubList(XF110 dialog) {
		super(dialog, "", true);
		dialog_ = dialog;
		try {
			initComponentsAndVariants();
			setupFieldsAndColumns();
		} catch(Exception e) {
			e.printStackTrace(dialog.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	void initComponentsAndVariants() throws Exception {
		jPanelMain.setLayout(new BorderLayout());
		jSplitPaneMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPaneMain.add(jPanelCenter, JSplitPane.TOP);
		jSplitPaneMain.add(jScrollPaneMessages, JSplitPane.BOTTOM);
		jSplitPaneMain.setFocusable(false);
		jScrollPaneBatchFields.getViewport().add(jPanelBatchFields, null);
		jScrollPaneBatchFields.setBorder(null);
		jScrollPaneBatchFields.setFocusable(false);
		jPanelBatchFields.setLayout(null);
		jPanelBatchFields.setFocusable(false);
		jCheckBoxToExecuteBatchFunction.setFocusable(true);

		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jTextAreaMessages.setWrapStyleWord(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jPanelCenter.setLayout(new BorderLayout());
		jPanelCenter.setBorder(null);
		jPanelCenter.add(jScrollPaneTable, BorderLayout.CENTER);

		jTableMain.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jTableMain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTableMain.setRowSelectionAllowed(false);
		jTableMain.setFocusable(false);
		JTableHeader header = new JTableHeader(jTableMain.getColumnModel()) {
			private static final long serialVersionUID = 1L;
			public String getToolTipText(MouseEvent e) {
				return headersRenderer.getToolTipText(e);
			}
		};
		header.setResizingAllowed(false);
		header.setReorderingAllowed(false);
		header.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (headersRenderer.hasMouseOnColumnBorder(e.getX())) {
					isHeaderResizing = true;
					headersRenderer.setSizingHeader(e.getX());
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (isHeaderResizing) {
					headersRenderer.setNewBoundsToHeaders(e.getX());
					TableColumn column = jTableMain.getColumnModel().getColumn(0);
					column.setPreferredWidth(headersRenderer.getWidth());
					cellsRenderer.setupCellBounds();
					jScrollPaneTable.updateUI();
				}
				isHeaderResizing = false;
			}
			public void mouseExited(MouseEvent e) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		header.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				if (headersRenderer.hasMouseOnColumnBorder(e.getX())) {
					setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				} else {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}
			public void mouseDragged(MouseEvent e) {
				Graphics2D g2 = (Graphics2D)jScrollPaneTable.getGraphics();
				g2.setColor(Color.gray.darker());
				int pointX;
				if (e.getX() < 30) {
					pointX = 30;
				} else {
					pointX = e.getX()-1;
				}
				g2.fillRect(pointX,0,3,jTableMain.getHeight()+headersRenderer.getHeight());
				jScrollPaneTable.updateUI();
			}
		});
		jTableMain.setTableHeader(header);
		jScrollPaneTable.getViewport().add(jTableMain, null);
		jScrollPaneTable.addMouseListener(new XF110_SubListjScrollPaneTable_mouseAdapter(this));
		jScrollPaneTable.setFocusable(true);
		jScrollPaneTable.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				if (jTableMain.getRowCount() > 0) {
					jTableMain.editCellAt(0, 0);
					cellsEditor.transferFocusOfCell(0, 0, true);
				}
			}
		});

		jPanelBottom.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setLayout(new BorderLayout());
		jPanelBottom.setBorder(null);
		jLabelFunctionID.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
		jLabelFunctionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelFunctionID.setForeground(Color.gray);
		jLabelFunctionID.setFocusable(false);
		jLabelSessionID.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
		jLabelSessionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelSessionID.setForeground(Color.gray);
		jLabelSessionID.setFocusable(false);
		jPanelButtons.setBorder(null);
		jPanelButtons.setLayout(gridLayoutButtons);
		jPanelButtons.setFocusable(false);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
		jPanelInfo.setLayout(gridLayoutInfo);
		jPanelInfo.add(jLabelSessionID);
		jPanelInfo.add(jLabelFunctionID);
		jPanelInfo.setFocusable(false);
		jProgressBar.setStringPainted(true);
		jProgressBar.setString(XFUtility.RESOURCE.getString("ChrossCheck"));
		gridLayoutButtons.setColumns(7);
		gridLayoutButtons.setRows(1);

		for (int i = 0; i < 7; i++) {
			jButtonArray[i] = new JButton();
			jButtonArray[i].setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
			jButtonArray[i].setFocusable(false);
			jButtonArray[i].addActionListener(new XF110_SubListFunctionButton_actionAdapter(this));
			jPanelButtonArray[i] = new JPanel();
			jPanelButtonArray[i].setLayout(new BorderLayout());
			jPanelButtonArray[i].add(jButtonArray[i], BorderLayout.CENTER);
			actionButtonArray[i] = new ButtonAction(jButtonArray[i]);
			jPanelButtons.add(jPanelButtonArray[i]);
		}

		jPanelMain.add(jSplitPaneMain, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
	}

	void setupFieldsAndColumns() throws Exception {
		String workStr, workAlias, workTableID, workFieldID;
		StringTokenizer workTokenizer, workTokenizer2;
		org.w3c.dom.Element workElement;

		///////////////////////////
		// Initializing variants //
		///////////////////////////
		functionElement_ = dialog_.getFunctionElement();
		session_ = dialog_.getSession();

		//////////////////////////////
		// Set panel configurations //
		//////////////////////////////
		jLabelSessionID.setText(session_.getSessionID());
		jLabelFunctionID.setText(dialog_.jLabelFunctionID.getText());
		FontMetrics metrics = jLabelFunctionID.getFontMetrics(jLabelFunctionID.getFont());
		jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));
		this.setTitle(functionElement_.getAttribute("Name"));
		initialMsg = functionElement_.getAttribute("InitialMsg");

		//////////////////////////////////////
		// Setup information of Batch Table //
		//////////////////////////////////////
		if (functionElement_.getAttribute("BatchTable").equals("")) {
			batchTable = null;
		} else {
			setupBatchTableAndFieldsConfiguration();
		}
		this.pack();

		///////////////////////////////////////
		// Setup information of detail table //
		//////////////////////////////////////
		detailTable = new XF110_SubListDetailTable(functionElement_, this);
		detailReferTableList.clear();
		detailReferElementList = detailTable.getTableElement().getElementsByTagName("Refer");
		sortingList2 = XFUtility.getSortedListModel(detailReferElementList, "Order");
		for (int j = 0; j < sortingList2.getSize(); j++) {
			detailReferTableList.add(new XF110_SubListDetailReferTable((org.w3c.dom.Element)sortingList2.getElementAt(j), this));
		}

		///////////////////////
		// Setup column list //
		///////////////////////
		detailColumnList.clear();
		int columnIndex = 0;
		NodeList columnFieldList = functionElement_.getElementsByTagName("Column");
		sortingList2 = XFUtility.getSortedListModel(columnFieldList, "Order");
		for (int j = 0; j < sortingList2.getSize(); j++) {
			detailColumnList.add(new XF110_SubListDetailColumn((org.w3c.dom.Element)sortingList2.getElementAt(j), this));
			if (detailColumnList.get(j).isVisibleOnPanel()) {
				columnIndex++;
				detailColumnList.get(j).setColumnIndex(columnIndex);
			}
		}

		///////////////////////////////////////////////
		// Setup table-model and renderer and editor //
		///////////////////////////////////////////////
		headersRenderer = new TableHeadersRenderer(); 
		cellsRenderer = new TableCellsRenderer(headersRenderer); 
		cellsEditor = new TableCellsEditor(headersRenderer); 
		jTableMain.setRowHeight(headersRenderer.getHeight());
		tableModelMain = new TableModelEditableList();
		jTableMain.setModel(tableModelMain);
		tableModelMain.addColumn(""); //column index:0 //
		TableColumn column = jTableMain.getColumnModel().getColumn(0);
		column.setHeaderRenderer(headersRenderer);
		column.setCellRenderer(cellsRenderer);
		column.setCellEditor(cellsEditor);
		column.setPreferredWidth(headersRenderer.getWidth());

		//////////////////////////////////////////////////
		// Add detail table key fields as HIDDEN column //
		//////////////////////////////////////////////////
		for (int j = 0; j < detailTable.getKeyFieldIDList().size(); j++) {
			if (!containsDetailField(detailTable.getTableID(), "", detailTable.getKeyFieldIDList().get(j))) {
				detailColumnList.add(new XF110_SubListDetailColumn(detailTable.getTableID(), "", detailTable.getKeyFieldIDList().get(j), this));
			}
		}

		////////////////////////////////////////////////
		// Add Batch with Key fields as HIDDEN column //
		////////////////////////////////////////////////
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("BatchWithKeyFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			workStr = workTokenizer.nextToken();
			batchWithKeyList.add(workStr);
			workTokenizer2 = new StringTokenizer(workStr, "." );
			workAlias = workTokenizer2.nextToken();
			workTableID = getTableIDOfTableAlias(workAlias);
			workFieldID = workTokenizer2.nextToken();
			if (!containsDetailField(workTableID, workAlias, workFieldID)) {
				detailColumnList.add(new XF110_SubListDetailColumn(workTableID, workAlias, workFieldID, this));
			}
		}

		////////////////////////////////////////////////////////////
		// Add unique key fields of detail table as HIDDEN column //
		////////////////////////////////////////////////////////////
		for (int i = 0; i < detailTable.getUniqueKeyList().size(); i++) {
			workTokenizer = new StringTokenizer(detailTable.getUniqueKeyList().get(i), ";" );
			while (workTokenizer.hasMoreTokens()) {
				workFieldID = workTokenizer.nextToken();
				if (!containsDetailField(detailTable.getTableID(), "", workFieldID)) {
					detailColumnList.add(new XF110_SubListDetailColumn(detailTable.getTableID(), "", workFieldID, this));
				}
			}
		}

		/////////////////////////////////////////////////////////////
		// Analyze fields in script and add them as HIDDEN columns //
		/////////////////////////////////////////////////////////////
		for (int j = 0; j < detailTable.getScriptList().size(); j++) {
			if	(detailTable.getScriptList().get(j).isToBeRunAtEvent("BR", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("AR", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("BU", "")
					|| detailTable.getScriptList().get(j).isToBeRunAtEvent("AU", "")) {
				for (int k = 0; k < detailTable.getScriptList().get(j).getFieldList().size(); k++) {
					workTokenizer = new StringTokenizer(detailTable.getScriptList().get(j).getFieldList().get(k), "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!containsDetailField(workTableID, workAlias, workFieldID)) {
						workElement = session_.getFieldElement(workTableID, workFieldID);
						if (workElement == null) {
							isInvalid = true;
							String msg = XFUtility.RESOURCE.getString("FunctionError1") + detailTable.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + detailTable.getScriptList().get(j).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
							JOptionPane.showMessageDialog(this, msg);
							throw new Exception(msg);
						} else {
							if (detailTable.isValidDataSource(workTableID, workAlias, workFieldID)) {
								detailColumnList.add(new XF110_SubListDetailColumn(workTableID, workAlias, workFieldID, this));
							}
						}
					}
				}
			}
		}

		////////////////////////////////////////////////////////////
		// Add BYTEA-type-field for BYTEA field as HIDDEN columns //
		////////////////////////////////////////////////////////////
		for (int j = 0; j < detailColumnList.size(); j++) {
			if (detailColumnList.get(j).getBasicType().equals("BYTEA") && !detailColumnList.get(j).getByteaTypeFieldID().equals("")) {
				if (!containsDetailField(detailTable.getTableID(), "", detailColumnList.get(j).getByteaTypeFieldID())) {
					detailColumnList.add(new XF110_SubListDetailColumn(detailTable.getTableID(), "", detailColumnList.get(j).getByteaTypeFieldID(), this));
				}
			}
		}

		////////////////////////////////////////
		// Analyze detail refer tables and    //
		// add their fields as HIDDEN columns //
		////////////////////////////////////////
		for (int j = detailReferTableList.size()-1; j > -1; j--) {
			for (int k = 0; k < detailReferTableList.get(j).getFieldIDList().size(); k++) {
				if (containsDetailField(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k))) {
					detailReferTableList.get(j).setToBeExecuted(true);
					break;
				}
			}
			if (!detailReferTableList.get(j).isToBeExecuted()) {
				for (int k = 0; k < detailReferTableList.get(j).getWithKeyFieldIDList().size(); k++) {
					workTokenizer = new StringTokenizer(detailReferTableList.get(j).getWithKeyFieldIDList().get(k), "." );
					workAlias = workTokenizer.nextToken();
					workFieldID = workTokenizer.nextToken();
					if (workAlias.equals(detailTable.getTableID()) && containsDetailField(detailTable.getTableID(), "", workFieldID)) {
						detailReferTableList.get(j).setToBeExecuted(true);
						break;
					}
				}
			}
			if (detailReferTableList.get(j).isToBeExecuted()) {
				for (int k = 0; k < detailReferTableList.get(j).getFieldIDList().size(); k++) {
					if (!containsDetailField(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k))) {
						detailColumnList.add(new XF110_SubListDetailColumn(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k), this));
					}
				}
				for (int k = 0; k < detailReferTableList.get(j).getWithKeyFieldIDList().size(); k++) {
					workTokenizer = new StringTokenizer(detailReferTableList.get(j).getWithKeyFieldIDList().get(k), "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!containsDetailField(workTableID, workAlias, workFieldID)) {
						detailColumnList.add(new XF110_SubListDetailColumn(workTableID, workAlias, workFieldID, this));
					}
				}
			}
		}

		/////////////////////////////////////////////
		// Initializing script engine and bindings //
		/////////////////////////////////////////////
		scriptEngine = dialog_.getScriptEngine();
		scriptBindings = scriptEngine.createBindings();
		scriptBindings.put("instance", (XFScriptable)this);
		for (int i = 0; i < batchFieldList.size(); i++) {
			scriptBindings.put(batchFieldList.get(i).getFieldIDInScript(), batchFieldList.get(i));
		}
		for (int i = 0; i < detailColumnList.size(); i++) {
			if (!scriptBindings.containsKey(detailColumnList.get(i).getFieldIDInScript())) {
				scriptBindings.put(detailColumnList.get(i).getFieldIDInScript(), detailColumnList.get(i));
			}
		}
	}
	
	private void setupBatchTableAndFieldsConfiguration() throws Exception {
		String workStr, workAlias, workTableID, workFieldID;
		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;
		XF110_SubListBatchField batchField;

		///////////////////////
		// Setup Batch Table //
		///////////////////////
		batchTable = new XF110_SubListBatchTable(functionElement_, this);
		batchReferTableList.clear();
		batchReferElementList = batchTable.getTableElement().getElementsByTagName("Refer");
		sortingList1 = XFUtility.getSortedListModel(batchReferElementList, "Order");
		for (int i = 0; i < sortingList1.getSize(); i++) {
			batchReferTableList.add(new XF110_SubListBatchReferTable((org.w3c.dom.Element)sortingList1.getElementAt(i), this));
		}

		//////////////////////////////////////////
		// Initialize variants for Batch Fields //
		//////////////////////////////////////////
		batchFieldList.clear();
		firstEditableBatchField = null;
		Dimension dimOfPriviousField = new Dimension(0,0);
		Dimension dim = new Dimension(0,0);
		int posX = 0;
		int posY = 0;
		int biggestWidth = 300;
		int biggestHeight = 30;
		boolean firstVisibleField = true;
		boolean hasAnyBatchFields = false;
		XFEditableField zipField = null;

		////////////////////////
		// Setup Batch Fields //
		////////////////////////
		NodeList batchFieldElementList = functionElement_.getElementsByTagName("BatchField");
		sortingList1 = XFUtility.getSortedListModel(batchFieldElementList, "Order");
		for (int i = 0; i < sortingList1.getSize(); i++) {
			batchFieldList.add(new XF110_SubListBatchField((org.w3c.dom.Element)sortingList1.getElementAt(i), this));
			if (batchFieldList.get(i).getTypeOptionList().contains("ZIPNO")) {
				zipField = batchFieldList.get(i).getComponent();
			}
			if (batchFieldList.get(i).getTypeOptionList().contains("ZIPADRS")) {
				batchFieldList.get(i).setRefferComponent(zipField);
			}
			if (firstVisibleField) {
				posX = 0;
				posY = XFUtility.FIELD_VERTICAL_MARGIN + 3;
				firstVisibleField = false;
				hasAnyBatchFields = true;
			} else {
				if (batchFieldList.get(i).isHorizontal()) {
					posX = posX + dimOfPriviousField.width + batchFieldList.get(i).getPositionMargin() + XFUtility.FIELD_HORIZONTAL_MARGIN;
				} else {
					posX = 0;
					posY = posY + dimOfPriviousField.height+ batchFieldList.get(i).getPositionMargin() + XFUtility.FIELD_VERTICAL_MARGIN;
				}
			}
			dim = batchFieldList.get(i).getPreferredSize();
			batchFieldList.get(i).setBounds(posX, posY, dim.width, dim.height);
			jPanelBatchFields.add(batchFieldList.get(i));

			if (posX + dim.width > biggestWidth) {
				biggestWidth = posX + dim.width;
			}
			if (posY + dim.height > biggestHeight) {
				biggestHeight = posY + dim.height;
			}

			if (batchFieldList.get(i).isHorizontal()) {
				dimOfPriviousField = new Dimension(dim.width, XFUtility.FIELD_UNIT_HEIGHT);
			} else {
				dimOfPriviousField = new Dimension(dim.width, dim.height);
			}
		}

		///////////////////////////////////////////////
		// Setup panel components for Batch Function //
		///////////////////////////////////////////////
		batchFunctionID = "";
		jCheckBoxToExecuteBatchFunction.setSelected(false);
		if (!functionElement_.getAttribute("BatchRecordFunction").equals("")) {
			batchFunctionID = functionElement_.getAttribute("BatchRecordFunction");
			jCheckBoxToExecuteBatchFunction.setSelected(true);
			if (!functionElement_.getAttribute("BatchRecordFunctionMsg").equals("")) {
				jCheckBoxToExecuteBatchFunction.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
				jCheckBoxToExecuteBatchFunction.setText(functionElement_.getAttribute("BatchRecordFunctionMsg"));
				FontMetrics metrics = jCheckBoxToExecuteBatchFunction.getFontMetrics(jCheckBoxToExecuteBatchFunction.getFont());
				if (firstVisibleField) {
					posX = 0;
					posY = XFUtility.FIELD_VERTICAL_MARGIN + 3;
					firstVisibleField = false;
					hasAnyBatchFields = true;
				} else {
					posX = posX + dimOfPriviousField.width + 50;
				}
				jCheckBoxToExecuteBatchFunction.setBounds(posX, posY + 1, metrics.stringWidth(jCheckBoxToExecuteBatchFunction.getText()) + 35, 20);
				jPanelBatchFields.add(jCheckBoxToExecuteBatchFunction);
				//dim = jCheckBoxToExecuteBatchFunction.getPreferredSize();
				dim = new Dimension(jCheckBoxToExecuteBatchFunction.getBounds().width, jCheckBoxToExecuteBatchFunction.getBounds().height);
				//JOptionPane.showMessageDialog(null, (posX + dim.width)+", "+ biggestWidth);
				if (posX + dim.width > biggestWidth) {
					biggestWidth = posX + dim.width;
				}
				if (posY + dim.height > biggestHeight) {
					biggestHeight = posY + dim.height;
				}
			}
		}

		///////////////////////////////////////////
		// Add batch table keys as HIDDEN fields //
		///////////////////////////////////////////
		for (int i = 0; i < batchTable.getKeyFieldIDList().size(); i++) {
			if (!containsBatchField(batchTable.getTableID(), "", batchTable.getKeyFieldIDList().get(i))) {
				batchFieldList.add(new XF110_SubListBatchField(batchTable.getTableID(), "", batchTable.getKeyFieldIDList().get(i), this));
			}
		}

		///////////////////////////////////////////////////////////
		// Add unique key fields of batch table as HIDDEN column //
		///////////////////////////////////////////////////////////
		for (int i = 0; i < batchTable.getUniqueKeyList().size(); i++) {
			workTokenizer = new StringTokenizer(batchTable.getUniqueKeyList().get(i), ";" );
			while (workTokenizer.hasMoreTokens()) {
				workFieldID = workTokenizer.nextToken();
				if (!containsBatchField(batchTable.getTableID(), "", workFieldID)) {
					batchFieldList.add(new XF110_SubListBatchField(batchTable.getTableID(), "", workFieldID, this));
				}
			}
		}
		
		/////////////////////////////////////////////////////////////
		// Analyze fields in script and add them as HIDDEN columns //
		/////////////////////////////////////////////////////////////
		for (int i = 0; i < batchTable.getScriptList().size(); i++) {
			if	(batchTable.getScriptList().get(i).isToBeRunAtEvent("BC", "")
					|| batchTable.getScriptList().get(i).isToBeRunAtEvent("AC", "")) {
				for (int j = 0; j < batchTable.getScriptList().get(i).getFieldList().size(); j++) {
					workTokenizer = new StringTokenizer(batchTable.getScriptList().get(i).getFieldList().get(j), "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!containsBatchField(workTableID, workAlias, workFieldID)) {
						workElement = session_.getFieldElement(workTableID, workFieldID);
						if (workElement == null) {
							isInvalid = true;
							String msg = XFUtility.RESOURCE.getString("FunctionError1") + batchTable.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + batchTable.getScriptList().get(i).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
							JOptionPane.showMessageDialog(this, msg);
							throw new Exception(msg);
						} else {
							batchFieldList.add(new XF110_SubListBatchField(workTableID, workAlias, workFieldID, this));
						}
					}
				}
			}
		}

		/////////////////////////////////////////////////////////////////
		// Add BYTEA-type-field for BYTEA field as HIDDEN batch fields //
		/////////////////////////////////////////////////////////////////
		for (int i = 0; i < batchFieldList.size(); i++) {
			if (batchFieldList.get(i).getBasicType().equals("BYTEA") && !batchFieldList.get(i).getByteaTypeFieldID().equals("")) {
				if (!containsBatchField(batchTable.getTableID(), "", batchFieldList.get(i).getByteaTypeFieldID())) {
					batchFieldList.add(new XF110_SubListBatchField(batchTable.getTableID(), "", batchFieldList.get(i).getByteaTypeFieldID(), this));
				}
			}
		}
		
		////////////////////////////////////////////////////////////////////////////
		// Analyze batch refer tables and add their fields as HIDDEN batch fields //
		////////////////////////////////////////////////////////////////////////////
		for (int i = batchReferTableList.size()-1; i > -1; i--) {
			for (int j = 0; j < batchReferTableList.get(i).getFieldIDList().size(); j++) {
				if (containsBatchField(batchReferTableList.get(i).getTableID(), batchReferTableList.get(i).getTableAlias(), batchReferTableList.get(i).getFieldIDList().get(j))) {
					batchReferTableList.get(i).setToBeExecuted(true);
					break;
				}
			}
			if (batchReferTableList.get(i).isToBeExecuted()) {
				for (int j = 0; j < batchReferTableList.get(i).getFieldIDList().size(); j++) {
					if (!containsBatchField(batchReferTableList.get(i).getTableID(), batchReferTableList.get(i).getTableAlias(), batchReferTableList.get(i).getFieldIDList().get(j))) {
						batchFieldList.add(new XF110_SubListBatchField(batchReferTableList.get(i).getTableID(), batchReferTableList.get(i).getTableAlias(), batchReferTableList.get(i).getFieldIDList().get(j), this));
					}
				}
				for (int j = 0; j < batchReferTableList.get(i).getWithKeyFieldIDList().size(); j++) {
					workTokenizer = new StringTokenizer(batchReferTableList.get(i).getWithKeyFieldIDList().get(j), "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!containsBatchField(workTableID, workAlias, workFieldID)) {
						batchFieldList.add(new XF110_SubListBatchField(workTableID, workAlias, workFieldID, this));
					}
				}
			}
		}
		
		///////////////////////////////////////////////////////////////////////////////////
		// Add Batch Key fields as HIDDEN fields if they are not on the batch field list //
		// And set them as edit-controlled if they are already on the list               //
		///////////////////////////////////////////////////////////////////////////////////
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("BatchKeyFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			workStr = workTokenizer.nextToken();
			batchKeyList.add(workStr);
			batchField = getBatchFieldObjectByID(batchTable.getTableID(), batchTable.getTableID(), workStr); 
			if (batchField == null) {
				batchFieldList.add(new XF110_SubListBatchField(batchTable.getTableID(), batchTable.getTableID(), workStr, this));
			} else {
				batchField.setEditable(false);
			}
		}

		///////////////////////////////////
		// Setup panels for Batch fields //
		///////////////////////////////////
		if (hasAnyBatchFields) {
			jPanelBatchFields.setPreferredSize(new Dimension(biggestWidth, biggestHeight + 5));
			jPanelCenter.add(jPanelBatchFields, BorderLayout.NORTH);
		}
	}

	public String request() {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			/////////////////////////////////
			// Initializing basic variants //
			/////////////////////////////////
			reply_ = "";
			readyToShowDialog = true;
	        threadToSetupReferChecker = null;

	        ///////////////////////////////////////////////////
			// Select detail record and setup rows of JTable //
			///////////////////////////////////////////////////
			selectDetailRecordsAndSetupTableRows();

			/////////////////////////////////////////////////////////////
			// Clear key map list and setup field value of Batch Table //
			/////////////////////////////////////////////////////////////
			if (batchTable != null) {
				batchTable.getKeyMapList().clear();
			}
			if (batchFieldList.size() > 0) {
				setBatchFieldValues();
			}
			
			////////////////////////
			// Setup referChecker //
			////////////////////////
			XF110_SubListReferCheckerConstructor constructor = new XF110_SubListReferCheckerConstructor(this);
	        threadToSetupReferChecker = new Thread(constructor);
	        threadToSetupReferChecker.start();

			////////////////////////////////
			// Setup panel configurations //
			////////////////////////////////
			if (batchFieldList.size() > 0 && (jPanelBatchFields.getPreferredSize().width + 40) > dialog_.getPreferredSize().width) {
				this.setPreferredSize(new Dimension(jPanelBatchFields.getPreferredSize().width + 40, dialog_.getPreferredSize().height));
				int posX = ((session_.getMenuRectangle().width - (jPanelBatchFields.getPreferredSize().width + 40)) / 2) + session_.getMenuRectangle().x;
				this.setLocation(posX, dialog_.getLocation().y);
			} else {
				this.setPreferredSize(new Dimension(dialog_.getPreferredSize().width, dialog_.getPreferredSize().height));
				this.setLocation(dialog_.getLocation().x, dialog_.getLocation().y);
			}
			messageList.clear();
			if (initialMsg.equals("")) {
				jTextAreaMessages.setText(XFUtility.RESOURCE.getString("FunctionMessage7") + buttonUpdateCaption + XFUtility.RESOURCE.getString("FunctionMessage8"));
			} else {
				jTextAreaMessages.setText(initialMsg);
			}
			setupFunctionKeysAndButtons();
			jSplitPaneMain.setDividerLocation(this.getPreferredSize().height - 150);
			jPanelBottom.remove(jProgressBar);
			jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
			this.pack();

			//////////////////////////////////////////
			// Set focus on top focusable component //
			//////////////////////////////////////////
			if (firstEditableBatchField == null) {
				jScrollPaneTable.setFocusable(false);
				if (jTableMain.getRowCount() > 0) {
					//jTableMain.editCellAt(0, 0);
					cellsEditor.transferFocusOfCell(0, 0, true);
				}
			} else {
				jScrollPaneTable.setFocusable(true);
        		firstEditableBatchField.requestFocus();
        	}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5"));
			e.printStackTrace(dialog_.getExceptionStream());
			setErrorAndCloseFunction();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		////////////////
		// Show Panel //
		////////////////
		if (readyToShowDialog) {
			this.setVisible(true);
		}

		return reply_;
	}

	void setBatchFieldValues() {
		int compareResult;
		XF110_SubListDetailRowNumber rowNumberPrev = null;
		XF110_SubListDetailRowNumber rowNumberCurr = null;
		XF110_SubListBatchField batchField;
		ArrayList<Boolean> compareResultList = new ArrayList<Boolean>();

		for (int i = 0; i < batchWithKeyList.size(); i++) {
			compareResultList.add(true);
		}

		for (int i = 0; i < jTableMain.getRowCount(); i++) {
			rowNumberCurr = (XF110_SubListDetailRowNumber)tableModelMain.getValueAt(i,0);
			if (rowNumberPrev != null) {
				for (int j = 0; j < batchWithKeyList.size(); j++) {
					compareResult = rowNumberCurr.getColumnValueMap().get(batchWithKeyList.get(j)).toString().compareTo(rowNumberPrev.getColumnValueMap().get(batchWithKeyList.get(j)).toString());
					if (compareResult != 0) {
						compareResultList.set(j, false);
					}
				}
			}
			rowNumberPrev = rowNumberCurr;
		}

		for (int i = 0; i < compareResultList.size(); i++) {
			if (compareResultList.get(i)) {
				batchField = getBatchFieldObjectByID(batchTable.getTableID(), "", batchKeyList.get(i));
				if (batchField != null && rowNumberCurr != null) {
					batchField.setValue(rowNumberCurr.getColumnValueMap().get(batchWithKeyList.get(i)));
				}
			}
		}

		////////////////////////////////////////////////////////////////////////////////////////
		// These steps are required to set value to field according to related ComboBox-field //
		////////////////////////////////////////////////////////////////////////////////////////
		for (int i = 0; i < batchFieldList.size(); i++) {
			batchFieldList.get(i).setValue(batchFieldList.get(i).getInternalValue());
		}

		fetchBatchReferRecords(false);
	}
	
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			closeFunction("EXIT");
		}
	}
	
	boolean isInvalid() {
		return isInvalid;
	}
	
	public boolean isAvailable() {
		return dialog_.isAvailable();
	}

	void setErrorAndCloseFunction() {
		reply_ = "ERROR";
		if (this.isVisible()) {
			this.setVisible(false);
		} else {
			readyToShowDialog = false;
		}
	}

	void closeFunction(String code) {
		try {
			if (threadToSetupReferChecker != null) {
				threadToSetupReferChecker.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		reply_ = code;
		this.setVisible(false);
	}

	public HashMap<String, Object> getParmMap() {
		return dialog_.getParmMap();
	}
	
	public void setProcessLog(String text) {
		dialog_.setProcessLog(text);
	}

	public XFTableOperator createTableOperator(String oparation, String tableID) {
		return dialog_.createTableOperator(oparation, tableID);
	}

	public XFTableOperator createTableOperator(String sqlText) {
		return dialog_.createTableOperator(sqlText);
	}

	public XFTableOperator getReferOperator(String sqlText) {
		return dialog_.getReferOperator(sqlText);
	}

	public HashMap<String, Object> getReturnMap() {
		return dialog_.getReturnMap();
	}

	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(this, message);
		}
		reply_ = "EXIT";
		if (this.isVisible()) {
			this.setVisible(false);
		}
	}
	
	public void cancelWithScriptException(ScriptException e, String scriptName) {
		JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError7") + scriptName + XFUtility.RESOURCE.getString("FunctionError8"));
		dialog_.setExceptionHeader("'" + scriptName + "' Script error\n");
		e.printStackTrace(dialog_.getExceptionStream());
		this.rollback();
		setErrorAndCloseFunction();
	}
	
	public void cancelWithException(Exception e) {
		JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5") + "\n" + e.getMessage());
		e.printStackTrace(dialog_.getExceptionStream());
		this.rollback();
		setErrorAndCloseFunction();
	}

	public void callFunction(String functionID) {
		try {
			dialog_.setReturnMap(session_.executeFunction(functionID, dialog_.getParmMap()));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			dialog_.setExceptionHeader(e.getMessage());
			setErrorAndCloseFunction();
		}
	}

	public void setReferChecker(ReferChecker checker) {
		referChecker = checker;
	}
	
	public void startProgress(String text, int maxValue) {
		jProgressBar.setMaximum(maxValue);
		jProgressBar.setValue(0);
		jPanelBottom.remove(jPanelInfo);
		jProgressBar.setString(text);
		jProgressBar.setPreferredSize(jPanelInfo.getPreferredSize());
		jPanelBottom.add(jProgressBar, BorderLayout.EAST);
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
		jPanelBottom.repaint();
	}

	public void commit() {
		dialog_.commit();
	}
	
	public void rollback() {
		dialog_.rollback();
	}
	
	int fetchBatchReferRecords(boolean toBeChecked) {
		int countOfErrors = 0;
		boolean recordNotFound;
		XFTableOperator operator;

		try {
			countOfErrors = countOfErrors + batchTable.runScript("BC", "BR()"); /* Script to be run BEFORE CREATE */
			for (int i = 0; i < batchReferTableList.size(); i++) {
				if (batchReferTableList.get(i).isToBeExecuted()) {

					countOfErrors = countOfErrors + batchTable.runScript("BC", "BR(" + batchReferTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE READ */

					if (!batchReferTableList.get(i).isKeyNullable() || !batchReferTableList.get(i).isKeyNull()) {

						recordNotFound = true;

						for (int j = 0; j < batchFieldList.size(); j++) {
							if (batchFieldList.get(j).getTableAlias().equals(batchReferTableList.get(i).getTableAlias())
									&& !batchFieldList.get(j).isPromptListField()) {
								batchFieldList.get(j).setValue(batchFieldList.get(j).getNullValue());
							}
						}

						operator = createTableOperator(batchReferTableList.get(i).getSelectSQL(false));
						while (operator.next()) {
							if (batchReferTableList.get(i).isRecordToBeSelected(operator)) {
								recordNotFound = false;
								for (int j = 0; j < batchFieldList.size(); j++) {
									if (batchFieldList.get(j).getTableAlias().equals(batchReferTableList.get(i).getTableAlias())) {
										batchFieldList.get(j).setValueOfResultSet(operator);
									}
								}
								countOfErrors = countOfErrors + batchTable.runScript("BC", "AR(" + batchReferTableList.get(i).getTableAlias() + ")"); /* Script to be run AFTER READ */
							}
						}

						if (recordNotFound && toBeChecked && !batchReferTableList.get(i).isOptional()) {
							countOfErrors++;
							batchReferTableList.get(i).setErrorOnRelatedFields();
						}
					}
				}
			}

			////////////////////////////////////////////////////////////////
			// Run Script for BeforeCreate and AfterRead-all-refer-tables //
			////////////////////////////////////////////////////////////////
			countOfErrors = countOfErrors + batchTable.runScript("BC", "AR()");

		} catch(ScriptException e) {
			this.cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			this.cancelWithException(e);
		}
		return countOfErrors;
	}
	
	int fetchDetailReferRecords(String event, boolean toBeChecked, String specificReferTable, HashMap<String, Object> columnValueMap, HashMap<String, Object> columnOldValueMap) {
		int countOfErrors = 0;
		boolean recordNotFound;
		XFTableOperator operator;
		String sql;

		try {
			countOfErrors = countOfErrors + detailTable.runScript(event, "BR()", columnValueMap, columnOldValueMap); /* Script to be run AFTER READ primary table */

			for (int i = 0; i < detailReferTableList.size(); i++) {
				if (specificReferTable.equals("")
						|| specificReferTable.equals(detailReferTableList.get(i).getTableAlias())) {

					if (detailReferTableList.get(i).isToBeExecuted()) {

						countOfErrors = countOfErrors + detailTable.runScript(event, "BR(" + detailReferTableList.get(i).getTableAlias() + ")", columnValueMap, columnOldValueMap); /* Script to be run BEFORE READ */

						if (!detailReferTableList.get(i).isKeyNullable() || !detailReferTableList.get(i).isKeyNull()) {

							recordNotFound = true;

							sql = detailReferTableList.get(i).getSelectSQL(false);
							if (!sql.equals("")) {
								operator = createTableOperator(sql);
								while (operator.next()) {

									if (detailReferTableList.get(i).isRecordToBeSelected(operator)) {

										recordNotFound = false;

										for (int j = 0; j < detailColumnList.size(); j++) {
											if (detailColumnList.get(j).getTableAlias().equals(detailReferTableList.get(i).getTableAlias())) {
												detailColumnList.get(j).setValueOfResultSet(operator);
												columnValueMap.put(detailColumnList.get(j).getDataSourceName(), detailColumnList.get(j).getInternalValue());
											}
										}

										countOfErrors = countOfErrors + detailTable.runScript(event, "AR(" + detailReferTableList.get(i).getTableAlias() + ")", columnValueMap, columnOldValueMap); /* Script to be run AFTER READ */
									}
								}
							}

							if (recordNotFound && toBeChecked && !detailReferTableList.get(i).isOptional()) {
								countOfErrors++;
								detailReferTableList.get(i).setErrorOnRelatedFields();
							}
						}
					}
				}
			}

			///////////////////////////////////////////////
			// Run Script for AfterRead-all-refer-tables //
			///////////////////////////////////////////////
			countOfErrors = countOfErrors + detailTable.runScript(event, "AR()", columnValueMap, columnOldValueMap); /* Script to be run AFTER READ */

		} catch(ScriptException e) {
			this.cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			this.cancelWithException(e);
		}
		return countOfErrors;
	}

	void setupFunctionKeysAndButtons() {
		InputMap inputMapBatchFields  = jPanelBatchFields.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMapBatchFields = jPanelBatchFields.getActionMap();
		InputMap inputMapTableMain  = jTableMain.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMapTableMain = jTableMain.getActionMap();

		inputMapBatchFields.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHECK");
		actionMapBatchFields.put("CHECK", checkAction);
		inputMapBatchFields.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMapBatchFields.put("HELP", helpAction);
		actionMapBatchFields.put(inputMapBatchFields.get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)), tabAction);
		actionMapBatchFields.put(inputMapBatchFields.get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK)), shiftTabAction);
		actionMapBatchFields.put(inputMapBatchFields.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)), arrowUpAction);
		actionMapBatchFields.put(inputMapBatchFields.get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)), arrowDownAction);

		inputMapTableMain.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHECK");
		actionMapTableMain.put("CHECK", checkAction);
		inputMapTableMain.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMapTableMain.put("HELP", helpAction);
		actionMapTableMain.put(inputMapTableMain.get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)), tabAction);
		actionMapTableMain.put(inputMapTableMain.get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, Event.SHIFT_MASK)), shiftTabAction);
		actionMapTableMain.put(inputMapTableMain.get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)), arrowUpAction);
		actionMapTableMain.put(inputMapTableMain.get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)), arrowDownAction);

		for (int i = 0; i < 7; i++) {
			jButtonArray[i].setText("");
			jButtonArray[i].setVisible(false);
			actionDefinitionArray[i] = "";
		}
		int workIndex;
		org.w3c.dom.Element element;
		NodeList buttonList = functionElement_.getElementsByTagName("Button");
		for (int i = 0; i < buttonList.getLength(); i++) {
			element = (org.w3c.dom.Element)buttonList.item(i);
			if (element.getAttribute("Action").equals("EXIT")
					|| element.getAttribute("Action").equals("PREV")
					|| element.getAttribute("Action").equals("UPDATE")
					|| element.getAttribute("Action").equals("OUTPUT")) {

				workIndex = Integer.parseInt(element.getAttribute("Position"));
				actionDefinitionArray[workIndex] = element.getAttribute("Action");
				XFUtility.setCaptionToButton(jButtonArray[workIndex], element, "", this.getPreferredSize().width / 8);
				jButtonArray[workIndex].setVisible(true);

				inputMapTableMain.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
				actionMapTableMain.put("actionButton" + workIndex, actionButtonArray[workIndex]);

				inputMapBatchFields.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
				actionMapBatchFields.put("actionButton" + workIndex, actionButtonArray[workIndex]);

				if (element.getAttribute("Action").equals("UPDATE")) {
					buttonUpdateCaption = element.getAttribute("Caption");
					keyStrokeToUpdate = XFUtility.getKeyStroke(element.getAttribute("Number"));
				}
			}
		}
	}
	
	int getKeyCodeToUpdate() {
		return keyStrokeToUpdate.getKeyCode();
	}

	void selectDetailRecordsAndSetupTableRows() {
		HashMap<String, Object> keyValueMap;
		HashMap<String, Object> columnValueMap;
		HashMap<String, Object> columnOldValueMap;
		HashMap<String, String[]> columnValueListMap;
		HashMap<String, Boolean> columnEditableMap;
		XFTableOperator operator;

		try {
			int rowCount = tableModelMain.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				tableModelMain.removeRow(0);
			}
			cellsEditor.init();

			int countOfRows = 0;
			XF110_RowNumber rowNumber;
			for (int p = 0; p < dialog_.tableModelMain.getRowCount(); p++) {
				rowNumber = (XF110_RowNumber)dialog_.tableModelMain.getValueAt(p, 0);
				if (rowNumber.isSelected()) {
					operator = createTableOperator(detailTable.getSQLToSelect(rowNumber));
					if (operator.next()) {

						for (int i = 0; i < detailColumnList.size(); i++) {
							detailColumnList.get(i).initValue();
						}
						//detailTable.runScript("BR", "", null, null); /* Detail Table Script to be run BEFORE READ */
						for (int i = 0; i < detailColumnList.size(); i++) {
							if (detailColumnList.get(i).getTableID().equals(detailTable.getTableID())) {
								detailColumnList.get(i).setValueOfResultSet(operator);
							}
						}

						columnValueMap = new HashMap<String, Object>();
						columnOldValueMap = new HashMap<String, Object>();
						keyValueMap = new HashMap<String, Object>();
						for (int i = 0; i < detailColumnList.size(); i++) {
							columnValueMap.put(detailColumnList.get(i).getDataSourceName(), detailColumnList.get(i).getInternalValue());
							columnOldValueMap.put(detailColumnList.get(i).getDataSourceName(), detailColumnList.get(i).getOldValue());
							if (detailColumnList.get(i).isKey()) {
								keyValueMap.put(detailColumnList.get(i).getFieldID(), detailColumnList.get(i).getInternalValue());
							}
						}
						if (!detailTable.getUpdateCounterID().equals("")) {
							columnValueMap.put(detailTable.getUpdateCounterID(), Long.parseLong(operator.getValueOf(detailTable.getUpdateCounterID()).toString()));
						}

						detailTable.runScript("BU", "BR()", columnValueMap, null); /* Detail Table Script to be run BEFORE UPDATE */

						fetchDetailReferRecords("AR", false, "", columnValueMap, null);

						detailTable.runScript("BU", "AR()", columnValueMap, null); /* Detail Table Script to be run BEFORE UPDATE */

						columnEditableMap = new HashMap<String, Boolean>();
						columnValueListMap = new HashMap<String, String[]>();
						for (int i = 0; i < detailColumnList.size(); i++) {
							columnEditableMap.put(detailColumnList.get(i).getDataSourceName(), detailColumnList.get(i).isEditable());
							columnValueListMap.put(detailColumnList.get(i).getDataSourceName(), detailColumnList.get(i).getValueList());
							if (detailColumnList.get(i).getBasicType().equals("BYTEA")) {
								detailColumnList.get(i).setupByteaTypeField(detailColumnList);
							}
						}
						
						Object[] cell = new Object[1];
						cell[0] = new XF110_SubListDetailRowNumber(countOfRows + 1, keyValueMap, columnValueMap, columnOldValueMap, columnEditableMap, columnValueListMap, this);
						tableModelMain.addRow(cell);

						countOfRows++;
					} else {
						String errorMessage = XFUtility.RESOURCE.getString("FunctionError19");
						JOptionPane.showMessageDialog(this, errorMessage);
						dialog_.setExceptionHeader(errorMessage);
						setErrorAndCloseFunction();
					}
				}
			}
		} catch(ScriptException e) {
			this.cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5") + "\n" + e.getMessage());
			e.printStackTrace(dialog_.getExceptionStream());
			setErrorAndCloseFunction();
		}
	}

	void checkErrorsToUpdate(boolean checkOnly) {
		XF110_SubListDetailRowNumber tableRowNumber, previousRowNumber = null;
		XFTableOperator operator;
		int recordCount;
		HashMap<String, Object> keyMap;

		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			
			/////////////////////////////////////
			// Complete editing cell component //
			/////////////////////////////////////
			cellsEditor.stopCellEditing();

			///////////////////////////////////////////
			// Validate values of the detail records //
			///////////////////////////////////////////
			int countOfErrors = 0;
			for (int i = 0; i < jTableMain.getRowCount(); i++) {
				tableRowNumber = (XF110_SubListDetailRowNumber)tableModelMain.getValueAt(i, 0);
				tableRowNumber.setValuesToDetailColumns();
				countOfErrors = countOfErrors + tableRowNumber.countErrors(messageList);
			}

			/////////////////////////////////////////
			// Validate values for the batch table //
			/////////////////////////////////////////
			if (batchTable != null) {
				for (int i = 0; i < batchFieldList.size(); i++) {
					batchFieldList.get(i).setError(false);
				}
				countOfErrors = countOfErrors + fetchBatchReferRecords(true);
				for (int i = 0; i < batchFieldList.size(); i++) {
					if (batchFieldList.get(i).isNullError()) {
						countOfErrors++;
					}
					if (batchFieldList.get(i).getBasicType().equals("BYTEA")) {
						batchFieldList.get(i).setupByteaTypeField(batchFieldList);
					}
				}
			}

			if (countOfErrors == 0) {
				if (this.hasNoErrorWithKey(checkOnly)) {
					if (checkOnly) {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage9"));
					} else {

						//////////////////////////////////////////////
						// Resort order of detail records to update //
						//////////////////////////////////////////////
						ArrayList<XF110_SubListDetailRowNumber> rowNumberList = new ArrayList<XF110_SubListDetailRowNumber>();
						for (int i = 0; i < jTableMain.getRowCount(); i++) {
							rowNumberList.add((XF110_SubListDetailRowNumber)tableModelMain.getValueAt(i,0));
						}
						XF110_SubListDetailRowNumber[] rowNumberArray = rowNumberList.toArray(new XF110_SubListDetailRowNumber[0]);
						if (batchWithKeyList.size() > 0) {
							//Arrays.sort(rowNumberArray, new RowNumberComparator());
							Arrays.sort(rowNumberArray);
						}
						for (int i = 0; i < rowNumberArray.length; i++) {

							tableRowNumber = rowNumberArray[i];
							tableRowNumber.setValuesToDetailColumns();

							///////////////////////////////////////////////////
							// Insert record of the batch table at key-break //
							///////////////////////////////////////////////////
							if (batchTable != null && isBatchKeyBreak(previousRowNumber, tableRowNumber)) {
								operator = createTableOperator(batchTable.getSQLToInsert(tableRowNumber));
								recordCount = operator.execute();
								if (recordCount == 1) {
									//batchTable.runScript("AC", "");
								} else {
									String errorMessage = XFUtility.RESOURCE.getString("FunctionError20");
									JOptionPane.showMessageDialog(jPanelMain, errorMessage);
									dialog_.setExceptionHeader(errorMessage);
									this.rollback();
									setErrorAndCloseFunction();
								}
							}
							previousRowNumber = tableRowNumber;

							///////////////////////////////////////
							// Update record of the detail table //
							///////////////////////////////////////
							operator = createTableOperator(detailTable.getSQLToUpdate(tableRowNumber));
							recordCount = operator.execute();
							if (recordCount == 1) {
								detailTable.runScript("AU", "", tableRowNumber.getColumnValueMap(), tableRowNumber.getColumnOldValueMap());
							} else {
								String errorMessage = XFUtility.RESOURCE.getString("FunctionError19");
								JOptionPane.showMessageDialog(jPanelMain, errorMessage);
								dialog_.setExceptionHeader(errorMessage);
								this.rollback();
								setErrorAndCloseFunction();
							}
						}
						
						/////////////////////////////////////////////////
						// Execute batch-table script for AFTER-CREATE //
						/////////////////////////////////////////////////
						if (batchTable != null) {
							for (int i = 0; i < batchTable.getKeyMapList().size(); i++) {
								keyMap = batchTable.getKeyMapList().get(i).getHashMap();
								for (int j = 0; j < batchFieldList.size(); j++) {
									if (keyMap.containsKey(batchFieldList.get(j).getFieldID())) {
										batchFieldList.get(j).setValue(keyMap.get(batchFieldList.get(j).getFieldID()));
									}
								}
								batchTable.runScript("AC", "");
							}
						}

						///////////////////////////////////////////////////////////
						// Execute the function for batch-table records inserted //
						///////////////////////////////////////////////////////////
						try {
							if (batchTable != null && !batchFunctionID.equals("") && jCheckBoxToExecuteBatchFunction.isSelected()) {
								for (int i = 0; i < batchTable.getKeyMapList().size(); i++) {
									session_.executeFunction(batchFunctionID, batchTable.getKeyMapList().get(i).getHashMap());
								}
							}
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError5") + "\n" + e.getMessage());
							e.printStackTrace(dialog_.getExceptionStream());
							this.rollback();
							setErrorAndCloseFunction();
						}

						closeFunction("UPDATE");
					}
				}
			}

			if (dialog_.isToBeCanceled) {
				this.rollback();
			} else {
				this.commit();
			}
		} catch(ScriptException e) {
			this.cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			this.cancelWithException(e);
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	boolean hasNoErrorWithKey(boolean isCheckOnly) {
		boolean hasNoError = true;
		ArrayList<String> uniqueKeyList = new ArrayList<String>();
		ArrayList<String> keyFieldList = new ArrayList<String>();
		StringTokenizer workTokenizer;
		XFTableOperator operator;

		try {
			if (batchTable != null) {
				uniqueKeyList = batchTable.getUniqueKeyList();
				for (int i = 0; i < uniqueKeyList.size(); i++) {
					keyFieldList.clear();
					workTokenizer = new StringTokenizer(uniqueKeyList.get(i), ";" );
					while (workTokenizer.hasMoreTokens()) {
						keyFieldList.add(workTokenizer.nextToken());
					}
					operator = createTableOperator(batchTable.getSQLToCheckSKDuplication(keyFieldList));
					if (operator.next()) {
						hasNoError = false;
						messageList.add(XFUtility.RESOURCE.getString("FunctionError22"));
						for (int j = 0; j < batchFieldList.size(); j++) {
							if (keyFieldList.contains(batchFieldList.get(j).getFieldID())) {
								batchFieldList.get(j).setError(true);
							}
						}
					}
				}
			}

			if (!isCheckOnly) {
				threadToSetupReferChecker.join();
			}

			uniqueKeyList = detailTable.getUniqueKeyList();
			int rowSequence;
			for (int j = 0; j < jTableMain.getRowCount(); j++) {
				XF110_SubListDetailRowNumber tableRowNumber = (XF110_SubListDetailRowNumber)tableModelMain.getValueAt(j, 0);
				rowSequence = tableRowNumber.getRowIndex() + 1;

				if (detailTable.hasPrimaryKeyValueAltered(tableRowNumber)) {
					hasNoError = false;
					messageList.add(XFUtility.RESOURCE.getString("FunctionError26") + rowSequence + XFUtility.RESOURCE.getString("FunctionError27"));
				} else {
					for (int i = 0; i < uniqueKeyList.size(); i++) {
						keyFieldList.clear();
						workTokenizer = new StringTokenizer(uniqueKeyList.get(i), ";" );
						while (workTokenizer.hasMoreTokens()) {
							keyFieldList.add(workTokenizer.nextToken());
						}
						operator = createTableOperator(detailTable.getSQLToCheckSKDuplication(tableRowNumber, keyFieldList));
						if (operator.next()) {
							hasNoError = false;
							messageList.add(XFUtility.RESOURCE.getString("FunctionError28") + rowSequence + XFUtility.RESOURCE.getString("FunctionError29"));
						}
						if (!hasNoError) {
							break;
						}
					}
					if (hasNoError && !isCheckOnly && referChecker != null) {
						ArrayList<String> errorMsgList = referChecker.getOperationErrors("UPDATE", tableRowNumber.getColumnValueMapWithFieldID(), tableRowNumber.getColumnOldValueMapWithFieldID(), rowSequence);
						for (int i = 0; i < errorMsgList.size(); i++) {
							hasNoError = false;
							messageList.add(errorMsgList.get(i));
						}
					}
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5") + "\n" + e.getMessage());
			e.printStackTrace(dialog_.getExceptionStream());
			setErrorAndCloseFunction();
		}
		return hasNoError;
	}
	
	boolean isBatchKeyBreak(XF110_SubListDetailRowNumber rowNumber1, XF110_SubListDetailRowNumber rowNumber2) {
		boolean isBreak = false;
		int compareResult;
		if (batchTable != null) {
			if (rowNumber1 == null) {
				isBreak = true;
			} else {
				if (batchWithKeyList.size() > 0) {
					for (int i = 0; i < batchWithKeyList.size(); i++) {
						compareResult = rowNumber1.getColumnValueMap().get(batchWithKeyList.get(i)).toString().compareTo(rowNumber2.getColumnValueMap().get(batchWithKeyList.get(i)).toString());
						if (compareResult != 0) {
							isBreak = true;
							break;
						}
					}
				}
			}
		}
		return isBreak;	
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

	public String getReturnMessage() {
		StringBuffer buf = new StringBuffer();
		if (reply_.equals("UPDATE")) {
			buf.append(XFUtility.RESOURCE.getString("Asterisk"));
			buf.append(jTableMain.getRowCount());
			if (batchTable == null) {
				buf.append(XFUtility.RESOURCE.getString("FunctionMessage40"));
				buf.append(detailTable.getTableElement().getAttribute("Name"));
				buf.append(XFUtility.RESOURCE.getString("FunctionMessage41"));
			} else {
				buf.append(XFUtility.RESOURCE.getString("FunctionMessage40"));
				buf.append(detailTable.getTableElement().getAttribute("Name"));
				buf.append(XFUtility.RESOURCE.getString("FunctionMessage42"));
				if (batchTable.getKeyMapList().size() > 1) {
					buf.append(batchTable.getKeyMapList().size());
					buf.append(XFUtility.RESOURCE.getString("FunctionMessage40"));
					buf.append(batchTable.getTableElement().getAttribute("Name"));
					buf.append(XFUtility.RESOURCE.getString("FunctionMessage43"));
				} else {
					buf.append(batchTable.getTableElement().getAttribute("Name"));
					buf.append(XFUtility.RESOURCE.getString("FunctionMessage43"));
				}
			}
		}
		return buf.toString();
	}
	
	void doButtonAction(String action) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			messageList.clear();

			if (action.equals("EXIT")) {
				closeFunction("EXIT");
			}
			if (action.equals("PREV")) {
				returnToMainList();
			}
			if (action.equals("UPDATE")) {
				checkErrorsToUpdate(false);
			}
			if (action.equals("OUTPUT")) {
				session_.browseFile(getExcellBookURI());
			}
			
			setMessagesOnPanel();

		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	void returnToMainList() {
		try {
			threadToSetupReferChecker.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		reply_ = "PREV";
		this.setVisible(false);
	}

	class TableModelEditableList extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		public boolean isCellEditable(int row, int col) {
			return true;
		}
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int col){
			return XF110_SubListDetailRowNumber.class;
		}
	}

	class TableHeadersRenderer extends JPanel implements TableCellRenderer {   
		private static final long serialVersionUID = 1L;
		private JLabel numberLabel = new JLabel("No.");
		private JPanel centerPanel = new JPanel();
		private ArrayList<JLabel> headerList = new ArrayList<JLabel>();
		private int totalWidthOfCenterPanel = 0;
		private int totalHeight = 0;
		private Component sizingHeader = null;

		public TableHeadersRenderer() {
			arrangeColumnsPosition(true);
			centerPanel.setLayout(null);
			numberLabel.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
			numberLabel.setBorder(new HeaderBorder());
			numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
			this.setLayout(new BorderLayout());
			this.add(numberLabel, BorderLayout.WEST);
			this.add(centerPanel, BorderLayout.CENTER);
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {  
			return this;
		}
		
		public int getWidth() {
			return this.getPreferredSize().width;
		}
		
		public int getSequenceWidth() {
			return numberLabel.getPreferredSize().width;
		}
		
		public String getSequenceLabel() {
			return numberLabel.getText();
		}
		
		public int getHeight() {
			return this.getPreferredSize().height;
		}
		
		public ArrayList<JLabel> getColumnHeaderList() {
			return headerList;
		}
		
		public boolean hasMouseOnColumnBorder(int headersPosX) {
			boolean result = false;
			double posX = headersPosX - numberLabel.getBounds().getWidth();
			if (posX >= -3 && posX <= 0) {
				result = true;
			} else {
				for (int i = 0; i < headerList.size(); i++) {
					if (posX >= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width - 3)
							&& posX <= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width)) {
						result = true;
						break;
					}
				}
			}
			return result;
		}
		
		public void setSizingHeader(int headersPosX) {
			double posX = headersPosX - numberLabel.getBounds().getWidth();
			sizingHeader = numberLabel;
			for (int i = 0; i < headerList.size(); i++) {
				if (posX >= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width - 3)
						&& posX <= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width)) {
					sizingHeader = headerList.get(i);
					break;
				}
			}
		}
		
		public void setNewBoundsToHeaders(int posXOnHeaders) {
			if (sizingHeader == numberLabel) {
				numberLabel.setPreferredSize(new Dimension(posXOnHeaders, totalHeight));
				this.setPreferredSize(new Dimension(totalWidthOfCenterPanel + posXOnHeaders, totalHeight));
			} else {
				int posX = posXOnHeaders - numberLabel.getBounds().width;
				int widthAdjusted = 0;
				for (int i = 0; i < headerList.size(); i++) {
					if (sizingHeader == headerList.get(i)) {
						int newWidth = posX - headerList.get(i).getBounds().x;
						if (newWidth > 0) {
							detailColumnList.get(i).setWidth(newWidth);
							widthAdjusted = newWidth - headerList.get(i).getBounds().width;
						}
						break;
					}
				}
				if (widthAdjusted != 0) {
					arrangeColumnsPosition(false);
				}
			}
		}
		
		public void arrangeColumnsPosition(boolean isWithDefaultSequenceWidth) {
			int fromX = 0;
			int fromY = 0;
			int width, height, wrkInt1, wrkInt2;
			JLabel header;
			totalWidthOfCenterPanel = 0;
			centerPanel.removeAll();
			headerList.clear();
			for (int i = 0; i < detailColumnList.size(); i++) {
				if (detailColumnList.get(i).isVisibleOnPanel()) {
					header = new JLabel();
					header.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
					if (detailColumnList.get(i).getValueType().equals("IMAGE")
							|| detailColumnList.get(i).getValueType().equals("FLAG")) {
						header.setHorizontalAlignment(SwingConstants.CENTER);
					} else {
						if (detailColumnList.get(i).getBasicType().equals("INTEGER")
								|| detailColumnList.get(i).getBasicType().equals("FLOAT")) {
							header.setHorizontalAlignment(SwingConstants.RIGHT);
						} else {
							header.setHorizontalAlignment(SwingConstants.LEFT);
						}
					}
					header.setText(detailColumnList.get(i).getCaption());
					header.setOpaque(true);

					width = detailColumnList.get(i).getWidth();
					height = XFUtility.ROW_UNIT_HEIGHT_EDITABLE * detailColumnList.get(i).getRows();
					if (i > 0) {
						fromX = headerList.get(i-1).getBounds().x + headerList.get(i-1).getBounds().width;
						fromY = headerList.get(i-1).getBounds().y + headerList.get(i-1).getBounds().height;
						for (int j = i-1; j >= 0; j--) {
							if (detailColumnList.get(i).getLayout().equals("VERTICAL")) {
								wrkInt1 = headerList.get(j).getBounds().y + headerList.get(j).getBounds().height;
								if (wrkInt1 <= fromY) {
									fromX = headerList.get(j).getBounds().x;
								} else {
									break;
								}
							} else {
								wrkInt1 = headerList.get(j).getBounds().x + headerList.get(j).getBounds().width;
								if (wrkInt1 <= fromX) {
									fromY = headerList.get(j).getBounds().y;
								} else {
									break;
								}
							}
						}
						for (int j = i-1; j >= 0; j--) {
							wrkInt1 = headerList.get(j).getBounds().x + headerList.get(j).getBounds().width;
							wrkInt2 = fromX + width;
							if (wrkInt2 < wrkInt1 && wrkInt2+2 > wrkInt1) {
								width = wrkInt1 - fromX;
							}
						}
					}

					header.setBounds(new Rectangle(fromX, fromY, width, height));
					header.setBorder(new HeaderBorder());
					headerList.add(header);
					centerPanel.add(header);

					if (fromX + width > totalWidthOfCenterPanel) {
						totalWidthOfCenterPanel = fromX + width;
					}
					if (fromY + height > totalHeight) {
						totalHeight = fromY + height;
					}
				}
			}
			if (isWithDefaultSequenceWidth) {
				numberLabel.setPreferredSize(new Dimension(XFUtility.SEQUENCE_WIDTH, totalHeight));
			}
			centerPanel.setPreferredSize(new Dimension(totalWidthOfCenterPanel, totalHeight));
			this.setPreferredSize(new Dimension(totalWidthOfCenterPanel + numberLabel.getPreferredSize().width, totalHeight));
		}

		public String getToolTipText(MouseEvent e) {
			String text = "";
			XF110_SubListDetailColumn column;
			if (e.getPoint().x > numberLabel.getPreferredSize().width) {
				Component compo = centerPanel.getComponentAt(e.getPoint().x-numberLabel.getPreferredSize().width, e.getPoint().y);
				if (compo != null) {
					for (int i = 0; i < headerList.size(); i++) {
						if (compo.equals(headerList.get(i))) {
							column = detailColumnList.get(i);
							if (column.getDecimalSize() > 0) {
								text = "<html>" + column.getFieldName() + " " + column.getDataSourceName() + " (" + column.getDataSize() + "," + column.getDecimalSize() + ")<br>" + column.getFieldRemarks();
							} else {
								text = "<html>" + column.getFieldName() + " " + column.getDataSourceName() + " (" + column.getDataSize() + ")<br>" + column.getFieldRemarks();
							}
							break;
						}
					}
				}
			} else {
				text = numberLabel.getText();
			}
			return text;
		}
	}  

	public class TableCellsRenderer extends JPanel implements TableCellRenderer {
		private static final long serialVersionUID = 1L;
		private JLabel numberCell = new JLabel("");
		private JPanel multiLinesPanel = new JPanel();
		private ArrayList<JLabel> cellList = new ArrayList<JLabel>();
		private TableHeadersRenderer headersRenderer_;

		public TableCellsRenderer(TableHeadersRenderer headersRenderer) {
			headersRenderer_ = headersRenderer;
			numberCell.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
			numberCell.setBorder(new CellBorder());
			numberCell.setHorizontalAlignment(SwingConstants.CENTER);
			multiLinesPanel.setLayout(null);
			multiLinesPanel.setOpaque(false);
			setupCellBounds();
			this.setLayout(new BorderLayout());
			this.add(numberCell, BorderLayout.WEST);
			this.add(multiLinesPanel, BorderLayout.CENTER);
		}   
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				numberCell.setForeground(table.getSelectionForeground());
			} else {
				if (row%2==0) {
					setBackground(SystemColor.text);
				} else {
					setBackground(XFUtility.ODD_ROW_COLOR);
				}
				numberCell.setForeground(table.getForeground());
			}
			setFocusable(false);

			XF110_SubListDetailRowNumber rowObject = (XF110_SubListDetailRowNumber)value;
			rowObject.setValuesToDetailColumns();
			numberCell.setText(rowObject.getRowNumberString());
			for (int i = 0; i < cellList.size(); i++) {
				cellList.get(i).setEnabled(detailColumnList.get(i).isEnabled());
				cellList.get(i).setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
				if (detailColumnList.get(i).getValueType().equals("IMAGE")
							|| detailColumnList.get(i).getValueType().equals("FLAG")) {
					cellList.get(i).setIcon((Icon)detailColumnList.get(i).getExternalValue());
				} else {
					cellList.get(i).setText((String)detailColumnList.get(i).getExternalValue());
					if (isSelected) {
						cellList.get(i).setForeground(table.getSelectionForeground());
					} else {
						if (detailColumnList.get(i).getColor().equals(Color.black)) {
							cellList.get(i).setForeground(table.getForeground());
						} else {
							cellList.get(i).setForeground(Color.getColor(detailColumnList.get(i).getColor()));
						}
					}
				}
				if (rowObject.getErrorCellIndexList().contains(i)) {
					cellList.get(i).setBackground(XFUtility.ERROR_COLOR);
				} else {
					if (row%2==0) {
						cellList.get(i).setBackground(SystemColor.text);
					} else {
						cellList.get(i).setBackground(XFUtility.ODD_ROW_COLOR);
					}
				}
			}
			return this;
		}

		private void setupCellBounds() {
			JLabel cell;
			Rectangle rec;
			cellList.clear();
			multiLinesPanel.removeAll();
			for (int i = 0; i < headersRenderer_.getColumnHeaderList().size(); i++) {
				cell = new JLabel();
				cell.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
				cell.setHorizontalAlignment(headersRenderer_.getColumnHeaderList().get(i).getHorizontalAlignment());
				rec = headersRenderer_.getColumnHeaderList().get(i).getBounds();
				cell.setBounds(rec.x, rec.y, rec.width, rec.height);
				cell.setBorder(new HeaderBorder());
				cell.setOpaque(true);
				cellList.add(cell);
				multiLinesPanel.add(cell);
			}
			int totalWidth = headersRenderer_.getWidth() - headersRenderer_.getSequenceWidth();
			int totalHeight = headersRenderer_.getHeight();
			multiLinesPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
			numberCell.setPreferredSize(new Dimension(headersRenderer_.getSequenceWidth(), totalHeight));
			this.setPreferredSize(new Dimension(totalWidth + headersRenderer_.getSequenceWidth(), totalHeight));
		}

		public void updateCellWidths() {
			for (int i = 0; i < headersRenderer_.getColumnHeaderList().size(); i++) {
				cellList.get(i).setBounds(headersRenderer_.getColumnHeaderList().get(i).getBounds());
			}
			int totalWidth = headersRenderer_.getWidth() - headersRenderer_.getSequenceWidth();
			int totalHeight = headersRenderer_.getHeight();
			multiLinesPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
			numberCell.setPreferredSize(new Dimension(headersRenderer_.getSequenceWidth(), totalHeight));
			this.setPreferredSize(new Dimension(totalWidth + headersRenderer_.getSequenceWidth(), totalHeight));
		}
	}
	
	public class TableCellsEditor extends AbstractCellEditor implements TableCellEditor {
		private static final long serialVersionUID = 1L;
	    private JPanel jPanel = new JPanel();
		private JLabel numberCell = new JLabel("");
		private JPanel multiLinesPanel = new JPanel();
		private ArrayList<XFTableColumnEditor> cellList = new ArrayList<XFTableColumnEditor>();
		private TableHeadersRenderer headersRenderer_;
		private int currentActiveRowIndex = -1;
		private int currentActiveCellIndex = -1;
		private XF110_SubListDetailRowNumber activeRowObject = null;

		public TableCellsEditor(TableHeadersRenderer headersRenderer) {
			headersRenderer_ = headersRenderer;
			numberCell.setBorder(new CellBorder());
			numberCell.setHorizontalAlignment(SwingConstants.CENTER);
			numberCell.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
			numberCell.setOpaque(true);
			multiLinesPanel.setLayout(null);
			multiLinesPanel.setOpaque(false);

			XFTableColumnEditor cell;
			cellList.clear();
			multiLinesPanel.removeAll();
			for (int i = 0; i < headersRenderer_.getColumnHeaderList().size(); i++) {
				cell = detailColumnList.get(i).getColumnEditor();
				cell.setEnabled(detailColumnList.get(i).isEnabled());
				cell.setHorizontalAlignment(headersRenderer_.getColumnHeaderList().get(i).getHorizontalAlignment());
				//cell.setBounds(headersRenderer_.getColumnHeaderList().get(i).getBounds());
				cell.setBorder(new HeaderBorder());
				cellList.add(cell);
				multiLinesPanel.add((Component)cell);
			}

			int totalWidth = headersRenderer_.getWidth() - headersRenderer_.getSequenceWidth();
			int totalHeight = headersRenderer_.getHeight();
			multiLinesPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
			numberCell.setPreferredSize(new Dimension(headersRenderer_.getSequenceWidth(), totalHeight));
			jPanel.setPreferredSize(new Dimension(totalWidth + headersRenderer_.getSequenceWidth(), totalHeight));

			jPanel.setLayout(new BorderLayout());
			jPanel.add(numberCell, BorderLayout.WEST);
			jPanel.add(multiLinesPanel, BorderLayout.CENTER);
		}

		public void updateCellWidths() {
			for (int i = 0; i < headersRenderer_.getColumnHeaderList().size(); i++) {
				cellList.get(i).setBounds(headersRenderer_.getColumnHeaderList().get(i).getBounds());
			}
			int totalWidth = headersRenderer_.getWidth() - headersRenderer_.getSequenceWidth();
			int totalHeight = headersRenderer_.getHeight();
			multiLinesPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
			numberCell.setPreferredSize(new Dimension(headersRenderer_.getSequenceWidth(), totalHeight));
		}
		
	    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int vColIndex) {
			currentActiveRowIndex = row;

			activeRowObject = (XF110_SubListDetailRowNumber)value;
			activeRowObject.setValuesToDetailColumns();

			numberCell.setText(activeRowObject.getRowNumberString());
			numberCell.setBackground(table.getSelectionBackground());
			numberCell.setForeground(table.getSelectionForeground());

			for (int i = 0; i < cellList.size(); i++) {
				cellList.get(i).setBounds(headersRenderer_.getColumnHeaderList().get(i).getBounds());
				cellList.get(i).setEditable(detailColumnList.get(i).isEditable());
				cellList.get(i).setFocusable(detailColumnList.get(i).isEditable());
				if (activeRowObject.getErrorCellIndexList().contains(i)) {
					cellList.get(i).setColorOfError();
				} else {
					cellList.get(i).setColorOfNormal(row);
				}
    			if (cellList.get(i) instanceof XF110_SubListCellEditorWithTextField) {
    				((XF110_SubListCellEditorWithTextField)cellList.get(i)).setValueList(detailColumnList.get(i).getValueList());
    			}
    			if (cellList.get(i) instanceof XF110_SubListCellEditorWithComboBox) {
    				((XF110_SubListCellEditorWithComboBox)cellList.get(i)).setupRecordList();
    			}
				if (!detailColumnList.get(i).getByteaTypeFieldID().equals("")) {
					for (int j = 0; j < detailColumnList.size(); j++) {
						if (detailColumnList.get(j).getFieldID().equals(detailColumnList.get(i).getByteaTypeFieldID())) {
							((XF110_SubListCellEditorWithByteaColumn)cellList.get(i)).setTypeColumn((XFColumnScriptable)detailColumnList.get(j));
							break;
						}
					}
				}
				cellList.get(i).setValue(detailColumnList.get(i).getInternalValue());
			}

			return jPanel;
	    }

	    public void transferFocusOfCell(int fromRowIndex, int fromColumnIndex, boolean isForward) {
	    	boolean cellSelected = false;
	    	int column, row;

			if (fromRowIndex > -1) {
				row = fromRowIndex;
			} else {
				row = currentActiveRowIndex;
			}

			if (fromColumnIndex > -1) {
				column = fromColumnIndex;
			} else {
				if (isForward) {
					column = currentActiveCellIndex + 1;
				} else {
					column = currentActiveCellIndex - 1;
				}
			}

			for (;;) {
				if (isForward) {
					if (column >= cellList.size()) {
						column = 0;
						row++;
					}
					for (int i = row; i < jTableMain.getRowCount(); i++) {
						jTableMain.editCellAt(i, 0);
						for (int j = column; j < cellList.size(); j++) {
							if (cellList.get(j).isEditable()) {
								//jTableMain.editCellAt(i, 0);
								cellList.get(j).requestFocus();
								currentActiveRowIndex = i;
								currentActiveCellIndex = j;
								cellSelected = true;
								break;
							}
						}
						if (cellSelected) {
							break;
						} else {
							column = 0;
						}
					}
					if (cellSelected) {
						break;
					} else {
						if (firstEditableBatchField == null) {
							column = 0;
							row = 0;
							if (row == fromRowIndex && column == fromColumnIndex) {
								break;
							}
						} else {
							stopCellEditing();
							jScrollPaneTable.transferFocus();
							break;
						}
					}
				} else {
					if (column < 0) {
						column = cellList.size() - 1;
						row--;
					}
					for (int i = row; i >= 0; i--) {
						jTableMain.editCellAt(i, 0);
						for (int j = column; j >= 0; j--) {
							if (cellList.get(j).isEditable()) {
								//jTableMain.editCellAt(i, 0);
								cellList.get(j).requestFocus();
								currentActiveRowIndex = i;
								currentActiveCellIndex = j;
								cellSelected = true;
								break;
							}
						}
						if (cellSelected) {
							break;
						} else {
							column = cellList.size() - 1;
						}
					}
					if (cellSelected) {
						break;
					} else {
						if (firstEditableBatchField == null) {
							column = cellList.size() - 1;
							row = jTableMain.getRowCount() - 1;
							if (row == fromRowIndex && column == fromColumnIndex) {
								break;
							}
						} else {
							stopCellEditing();
							jScrollPaneTable.transferFocusBackward();
							break;
						}
					}
				}
			}
		}
	    
	    public void requestFocusOnVerticalCell(String direction) {
	    	if (direction.equals("UP") && currentActiveRowIndex >= 1) {
	    		transferFocusOfCell(currentActiveRowIndex-1, currentActiveCellIndex, false);
	    	}
	    	if (direction.equals("DOWN") && currentActiveRowIndex < jTableMain.getRowCount()-1) {
		    	transferFocusOfCell(currentActiveRowIndex+1, currentActiveCellIndex, true);
		    }
	    }
	    
	    public void requestFocusOnCellAt(int row, int column) {
	    	if (row >= 0 && row < jTableMain.getRowCount()) {
	    		jTableMain.editCellAt(row, 0);
	    		if (column >= 0
	    				&& column < cellList.size()
	    				&& cellList.get(column).isEditable()) {
	    			cellList.get(column).requestFocus();
	    			currentActiveRowIndex = row;
	    			currentActiveCellIndex = column;
	    		}
	    	}
	    }

	    public XF110_SubListDetailRowNumber getActiveRowObject() {
			return activeRowObject;
		}
	    
	    public void updateRowObject() {
	    	Object value;
	    	if (activeRowObject != null) {
	    		for (int i = 0; i < cellList.size(); i++) {
	    			value = cellList.get(i).getInternalValue();
	    			if (value instanceof XFHashMap) {
	    				XFHashMap keyValues = (XFHashMap)value;
	    				for (int j = 0; j < keyValues.size(); j++) {
	    					activeRowObject.getColumnValueMap().put(
	    							keyValues.getKeyIDByIndex(j), keyValues.getValueByIndex(j));
	    				}
	    			} else {
	    				activeRowObject.getColumnValueMap().put(
	    						detailColumnList.get(i).getDataSourceName(), value);
	    			}
	    		}
	    	}
	    }
	    
	    public void updateActiveColumnIndex() {
			for (int i = 0; i < cellList.size(); i++) {
				if (cellList.get(i).hasFocus()) {
					currentActiveCellIndex = i;
				}
			}
	    }
	    
	    public void init() {
			cancelCellEditing();
			currentActiveRowIndex = -1;
			currentActiveCellIndex = -1;
	    }
	    
	    public ArrayList<XFTableColumnEditor> getCellList() {
	    	return cellList;
	    }

	    ////////////////////////////////////////////////////////////
	    // This method is called when editing is completed.       //
	    // It must return the new value to be stored in the cell. //
	    ////////////////////////////////////////////////////////////
	    public Object getCellEditorValue() {
	    	updateRowObject();
			return activeRowObject;
	    }
	}

//	class WorkingRow extends Object {
//		private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
//		private HashMap<String, Object> columnValueMap_ = new HashMap<String, Object>();
//		private HashMap<String, Object> columnOldValueMap_ = new HashMap<String, Object>();
//		private ArrayList<Object> orderByValueList_ = new ArrayList<Object>();
//		public WorkingRow(HashMap<String, Object> keyMap, HashMap<String, Object> columnValueMap, HashMap<String, Object> columnOldValueMap, ArrayList<Object> orderByValueList) {
//			columnValueMap_ = columnValueMap;
//			columnOldValueMap_ = columnOldValueMap;
//			keyMap_ = keyMap;
//			orderByValueList_ = orderByValueList;
//		}
//		public HashMap<String, Object> getKeyMap() {
//			return keyMap_;
//		}
//		public HashMap<String, Object> getColumnValueMap() {
//			return columnValueMap_;
//		}
//		public HashMap<String, Object> getColumnOldValueMap() {
//			return columnOldValueMap_;
//		}
//		public ArrayList<Object> getOrderByValueList() {
//			return orderByValueList_;
//		}
//	}
//
//	class WorkingRowComparator implements java.util.Comparator<WorkingRow>{
//		public int compare(WorkingRow row1, WorkingRow row2){
//			int compareResult = 0;
//			for (int i = 0; i < row1.getOrderByValueList().size(); i++) {
//				compareResult = row1.getOrderByValueList().get(i).toString().compareTo(row2.getOrderByValueList().get(i).toString());
//				if (detailTable.getOrderByFieldIDList().get(i).contains("(D)")) {
//					compareResult = compareResult * -1;
//				}
//				if (compareResult != 0) {
//					break;
//				}
//			}
//			return compareResult;
//		}
//	}
//
//	class RowNumberComparator implements java.util.Comparator<XF110_SubListDetailRowNumber>{
//		public int compare(XF110_SubListDetailRowNumber rowNumber1, XF110_SubListDetailRowNumber rowNumber2){
//			int compareResult = 0;
//			for (int i = 0; i < batchWithKeyList.size(); i++) {
//				compareResult = rowNumber1.getColumnValueMap().get(batchWithKeyList.get(i)).toString().compareTo(rowNumber2.getColumnValueMap().get(batchWithKeyList.get(i)).toString());
//				if (compareResult != 0) {
//					break;
//				}
//			}
//			return compareResult;
//		}
//	}
//
//	private URI getExcellBookURI() {
//		File xlsFile = null;
//		String xlsFileName = "";
//		FileOutputStream fileOutputStream = null;
//		HSSFFont font = null;
//		XF110_SubListDetailRowNumber rowObject;
//		String imageFileName = "";
//		String wrkStr;
//
//		HSSFWorkbook workBook = new HSSFWorkbook();
//		wrkStr = functionElement_.getAttribute("Name").replace("/", "_").replace("^", "_");
//		HSSFSheet workSheet = workBook.createSheet(wrkStr);
//		workSheet.setDefaultRowHeight( (short) 300);
//		HSSFFooter workSheetFooter = workSheet.getFooter();
//		workSheetFooter.setRight(functionElement_.getAttribute("Name") + "  Page " + HSSFFooter.page() + " / " + HSSFFooter.numPages() );
//		patriarch = workSheet.createDrawingPatriarch();
//
//		HSSFFont fontHeader = workBook.createFont();
//		fontHeader = workBook.createFont();
//		fontHeader.setFontName(XFUtility.RESOURCE.getString("XLSFontHDR"));
//		fontHeader.setFontHeightInPoints((short)11);
//
//		HSSFFont fontDataBlack = workBook.createFont();
//		fontDataBlack.setFontName(XFUtility.RESOURCE.getString("XLSFontDTL"));
//		fontDataBlack.setFontHeightInPoints((short)11);
//		HSSFFont fontDataRed = workBook.createFont();
//		fontDataRed.setFontName(XFUtility.RESOURCE.getString("XLSFontDTL"));
//		fontDataRed.setFontHeightInPoints((short)11);
//		fontDataRed.setColor(HSSFColor.RED.index);
//		HSSFFont fontDataBlue = workBook.createFont();
//		fontDataBlue.setFontName(XFUtility.RESOURCE.getString("XLSFontDTL"));
//		fontDataBlue.setFontHeightInPoints((short)11);
//		fontDataBlue.setColor(HSSFColor.BLUE.index);
//		HSSFFont fontDataGreen = workBook.createFont();
//		fontDataGreen.setFontName(XFUtility.RESOURCE.getString("XLSFontDTL"));
//		fontDataGreen.setFontHeightInPoints((short)11);
//		fontDataGreen.setColor(HSSFColor.GREEN.index);
//		HSSFFont fontDataOrange = workBook.createFont();
//		fontDataOrange.setFontName(XFUtility.RESOURCE.getString("XLSFontDTL"));
//		fontDataOrange.setFontHeightInPoints((short)11);
//		fontDataOrange.setColor(HSSFColor.ORANGE.index);
//
//		HSSFCellStyle styleHeaderLabel = workBook.createCellStyle();
//		styleHeaderLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//		styleHeaderLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//		styleHeaderLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
//		styleHeaderLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
//		styleHeaderLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
//		styleHeaderLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//		styleHeaderLabel.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
//		styleHeaderLabel.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
//		styleHeaderLabel.setFont(fontHeader);
//
//		HSSFCellStyle styleDetailLabel = workBook.createCellStyle();
//		styleDetailLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//		styleDetailLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//		styleDetailLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
//		styleDetailLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
//		styleDetailLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
//		styleDetailLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//		styleDetailLabel.setAlignment(HSSFCellStyle.ALIGN_LEFT);
//		styleDetailLabel.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
//		styleDetailLabel.setFont(fontHeader);
//		styleDetailLabel.setWrapText(true);
//
//		HSSFCellStyle styleDetailNumberLabel = workBook.createCellStyle();
//		styleDetailNumberLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//		styleDetailNumberLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//		styleDetailNumberLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
//		styleDetailNumberLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
//		styleDetailNumberLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
//		styleDetailNumberLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
//		styleDetailNumberLabel.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
//		styleDetailNumberLabel.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
//		styleDetailNumberLabel.setFont(fontHeader);
//
//		HSSFCellStyle styleDataInteger = workBook.createCellStyle();
//		styleDataInteger.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//		styleDataInteger.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//		styleDataInteger.setBorderRight(HSSFCellStyle.BORDER_THIN);
//		styleDataInteger.setBorderTop(HSSFCellStyle.BORDER_THIN);
//		styleDataInteger.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
//		styleDataInteger.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
//		styleDataInteger.setFont(fontDataBlack);
//		styleDataInteger.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
//
//		int currentRowNumber = -1;
//		int mergeRowNumberFrom = -1;
//
//		try {
//			xlsFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".xls");
//			xlsFileName = xlsFile.getPath();
//			fileOutputStream = new FileOutputStream(xlsFileName);
//			int columnIndex;
//			
//			////////////////////////
//			// Header field lines //
//			////////////////////////
//			for (int i = 0; i < batchFieldList.size() && i < 24; i++) {
//				if (batchFieldList.get(i).isVisibleOnPanel()) {
//					for (int j = 0; j < batchFieldList.get(i).getRows(); j++) {
//
//						currentRowNumber++;
//						HSSFRow rowData = workSheet.createRow(currentRowNumber);
//
//						/////////////////////////////////
//						// Cell for header field label //
//						/////////////////////////////////
//						HSSFCell cellHeader = rowData.createCell(0);
//						cellHeader.setCellStyle(styleHeaderLabel);
//						if (j==0) {
//							mergeRowNumberFrom = currentRowNumber;
//							if (!batchFieldList.get(i).getFieldOptionList().contains("NO_CAPTION")) {
//								cellHeader.setCellValue(new HSSFRichTextString(batchFieldList.get(i).getCaption()));
//							}
//						}
//						rowData.createCell(1).setCellStyle(styleHeaderLabel);
//
//						////////////////////////////////
//						// Cell for header field data //
//						////////////////////////////////
//						if (batchFieldList.get(i).getColor().equals("black")) {
//							font = fontDataBlack;
//						}
//						if (batchFieldList.get(i).getColor().equals("red")) {
//							font = fontDataRed;
//						}
//						if (batchFieldList.get(i).getColor().equals("blue")) {
//							font = fontDataBlue;
//						}
//						if (batchFieldList.get(i).getColor().equals("green")) {
//							font = fontDataGreen;
//						}
//						if (batchFieldList.get(i).getColor().equals("orange")) {
//							font = fontDataOrange;
//						}
//						setupCellAttributesForHeaderField(rowData, workBook, workSheet, batchFieldList.get(i), currentRowNumber, j, font);
//					}
//					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 0, 1));
//					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 2, 6));
//				}
//			}
//			
//			////////////
//			// Spacer //
//			////////////
//			currentRowNumber++;
//			workSheet.createRow(currentRowNumber);
//
//			//////////////////////////////////////////
//			// Column heading line for detail items //
//			//////////////////////////////////////////
//			currentRowNumber++;
//			HSSFRow rowCaption = workSheet.createRow(currentRowNumber);
//			HSSFCell cell = rowCaption.createCell(0);
//			cell.setCellStyle(styleDetailNumberLabel);
//			workSheet.setColumnWidth(0, headersRenderer.getSequenceWidth() * 40);
//			wrkStr = XFUtility.getCaptionForCell(headersRenderer.getSequenceLabel());
//			cell.setCellValue(new HSSFRichTextString(wrkStr));
//			for (int j = 0; j < detailColumnList.size(); j++) {
//				if (detailColumnList.get(j).isVisibleOnPanel()) {
//					cell = rowCaption.createCell(j+1);
//					if (detailColumnList.get(j).getBasicType().equals("INTEGER")
//							|| detailColumnList.get(j).getBasicType().equals("FLOAT")) {
//						if (detailColumnList.get(j).getTypeOptionList().contains("MSEQ") || detailColumnList.get(j).getTypeOptionList().contains("FYEAR")) {
//							cell.setCellStyle(styleDetailLabel);
//						} else {
//							cell.setCellStyle(styleDetailNumberLabel);
//						}
//					} else {
//						cell.setCellStyle(styleDetailLabel);
//					}
//					Rectangle rect = headersRenderer.getColumnHeaderList().get(j).getBounds();
//					workSheet.setColumnWidth(j+1, rect.width * 40);
//					wrkStr = XFUtility.getCaptionForCell(headersRenderer.getColumnHeaderList().get(j).getText());
//					wrkStr = wrkStr.replaceAll("<html>" , "");
//					wrkStr = wrkStr.replaceAll("<u>" , "");
//					cell.setCellValue(new HSSFRichTextString(wrkStr));
//				} else {
//					break;
//				}
//			}
//
//			for (int i = 0; i < tableModelMain.getRowCount(); i++) {
//				currentRowNumber++;
//				HSSFRow rowData = workSheet.createRow(currentRowNumber);
//
//				cell = rowData.createCell(0); //Column of Sequence Number
//				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
//				cell.setCellStyle(styleDataInteger);
//				cell.setCellValue(i + 1);
//
//				rowObject = (XF110_SubListDetailRowNumber)tableModelMain.getValueAt(i,0);
//				rowObject.setValuesToDetailColumns();
//				columnIndex = 0;
//				for (int j = 0; j < detailColumnList.size(); j++) {
//					if (detailColumnList.get(j).isVisibleOnPanel()) {
//						columnIndex++;
//						font = fontDataBlack;
//						if (detailColumnList.get(j).getColor().equals(Color.red)) {
//							font = fontDataRed;
//						}
//						if (detailColumnList.get(j).getColor().equals(Color.blue)) {
//							font = fontDataBlue;
//						}
//						if (detailColumnList.get(j).getColor().equals(Color.green)) {
//							font = fontDataGreen;
//						}
//						if (detailColumnList.get(j).getColor().equals(Color.orange)) {
//							font = fontDataOrange;
//						}
//						setupCellAttributesForDetailColumn(rowData.createCell(columnIndex), workBook, detailColumnList.get(j), font);
//						if (detailColumnList.get(j).getValueType().equals("IMAGE") && !detailColumnList.get(j).getInternalValue().equals("")) {
//							imageFileName = session_.getImageFileFolder() + detailColumnList.get(j).getInternalValue();
//							XFUtility.setupImageCellForDetailColumn(workBook, workSheet, currentRowNumber, columnIndex, imageFileName, patriarch);
//						}
//					}
//				}
//			}
//
//			workBook.write(fileOutputStream);
//			messageList.add(XFUtility.RESOURCE.getString("XLSComment1"));
//
//		} catch (Exception e) {
//			messageList.add(XFUtility.RESOURCE.getString("XLSErrorMessage"));
//			e.printStackTrace(dialog_.getExceptionStream());
//		} finally {
//			try {
//				fileOutputStream.close();
//			} catch (IOException e) {
//				e.printStackTrace(dialog_.getExceptionStream());
//			}
//		}
//		return xlsFile.toURI();
//	}
//
//	private void setupCellAttributesForHeaderField(HSSFRow rowData, HSSFWorkbook workBook, HSSFSheet workSheet, XF110_SubListBatchField object, int currentRowNumber, int rowIndexInCell, HSSFFont font) {
//		String wrk;
//
//		HSSFCellStyle style = workBook.createCellStyle();
//		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
//		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
//		style.setFont(font);
//		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
//		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
//		style.setWrapText(true);
//		style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
//
//		HSSFCell cellValue = rowData.createCell(2);
//
//		String basicType = object.getBasicType();
//		if (basicType.equals("INTEGER")) {
//			if (object.getTypeOptionList().contains("MSEQ") || object.getTypeOptionList().contains("FYEAR")) {
//				cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
//				cellValue.setCellStyle(style);
//				cellValue.setCellValue(new HSSFRichTextString((String)object.getExternalValue()));
//			} else {
//				wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
//				if (wrk.equals("") || object.getTypeOptionList().contains("NO_EDIT")) {
//					cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
//					cellValue.setCellStyle(style);
//					if (rowIndexInCell==0) {
//						cellValue.setCellValue(new HSSFRichTextString(""));
//					}
//				} else {
//					cellValue.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
//					style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
//					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
//					if (!object.getTypeOptionList().contains("NO_EDIT")
//						&& !object.getTypeOptionList().contains("ZERO_SUPPRESS")) {
//						style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
//					}
//					cellValue.setCellStyle(style);
//					if (rowIndexInCell==0) {
//						cellValue.setCellValue(Double.parseDouble(wrk));
//					}
//				}
//			}
//		} else {
//			if (basicType.equals("FLOAT")) {
//				wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
//				if (wrk.equals("") || object.getTypeOptionList().contains("NO_EDIT")) {
//					cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
//					cellValue.setCellStyle(style);
//					if (rowIndexInCell==0) {
//						cellValue.setCellValue(new HSSFRichTextString(""));
//					}
//				} else {
//					cellValue.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
//					style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
//					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
//					if (!object.getTypeOptionList().contains("NO_EDIT")
//							&& !object.getTypeOptionList().contains("ZERO_SUPPRESS")) {
//						style.setDataFormat(XFUtility.getFloatFormat(workBook, object.getDecimalSize()));
//					}
//					cellValue.setCellStyle(style);
//					if (rowIndexInCell==0) {
//						cellValue.setCellValue(Double.parseDouble(wrk));
//					}
//				}
//			} else {
//				cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
//				cellValue.setCellStyle(style);
//				if (rowIndexInCell==0) {
//					if (basicType.equals("STRING")) {
//						if (object.isImage()) {
//							style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
//							style.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);
//							style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
//							HSSFPatriarch patriarch = workSheet.createDrawingPatriarch();
//							HSSFClientAnchor anchor = null;
//							cellValue.setCellStyle(style);
//							cellValue.setCellValue(new HSSFRichTextString((String)object.getInternalValue()));
//							int imageType = -1;
//							String fileName = (String)object.getExternalValue();
//							File imageFile = new File(fileName);
//							if (imageFile.exists()) {
//								boolean isValidFileType = false;
//								if (fileName.contains(".png") || fileName.contains(".PNG")) {
//									imageType = HSSFWorkbook.PICTURE_TYPE_PNG;
//									isValidFileType = true;
//								}
//								if (fileName.contains(".jpg") || fileName.contains(".JPG") || fileName.contains(".jpeg") || fileName.contains(".JPEG")) {
//									imageType = HSSFWorkbook.PICTURE_TYPE_JPEG;
//									isValidFileType = true;
//								}
//								if (isValidFileType) {
//									int pictureIndex;
//									FileInputStream fis = null;
//									ByteArrayOutputStream bos = null;
//									try {
//										////////////////////////////////////////////////////////////////////////////////////
//										// Read in the image file and copy the image bytes into the ByteArrayOutputStream //
//										////////////////////////////////////////////////////////////////////////////////////
//										fis = new FileInputStream(imageFile);
//										bos = new ByteArrayOutputStream( );
//										int c;
//										while ((c = fis.read()) != -1) {
//											bos.write(c);
//										}
//										/////////////////////////////////////////
//										// Add the image bytes to the workbook //
//										/////////////////////////////////////////
//										pictureIndex = workBook.addPicture( bos.toByteArray(), imageType);
//										anchor = new HSSFClientAnchor(0,0,0,0,(short)2,currentRowNumber,(short)6,currentRowNumber + object.getRows());
//										anchor.setAnchorType(0);
//										anchor.setDx1(30);
//										anchor.setDy1(30);
//										anchor.setDx2(-30);
//										anchor.setDy2(-250);
//										patriarch.createPicture(anchor, pictureIndex);
//									} catch(Exception ex) {
//										ex.printStackTrace(dialog_.getExceptionStream());
//									} finally {
//										try {
//											if (fis != null) {
//												fis.close();
//											}
//											if (bos != null) {
//												bos.close();
//											}
//										} catch (IOException ex) {
//											ex.printStackTrace(dialog_.getExceptionStream());
//										}
//									}
//								}
//							}
//						} else {
//							cellValue.setCellStyle(style);
//							cellValue.setCellValue(new HSSFRichTextString((String)object.getExternalValue()));
//						}
//					} else {
//						if (basicType.equals("DATE")) {
//							java.util.Date utilDate = XFUtility.convertDateFromStringToUtil((String)object.getInternalValue());
//							String text = XFUtility.getUserExpressionOfUtilDate(utilDate, session_.getDateFormat(), false);
//							cellValue.setCellValue(new HSSFRichTextString(text));
//						}
//						if (object.getBasicType().equals("DATETIME") || object.getBasicType().equals("TIME")) {
//							cellValue.setCellValue(new HSSFRichTextString(object.getInternalValue().toString()));
//						}
//					}
//				}
//			}
//		}
//		rowData.createCell(3).setCellStyle(style);
//		rowData.createCell(4).setCellStyle(style);
//		rowData.createCell(5).setCellStyle(style);
//		rowData.createCell(6).setCellStyle(style);
//	}
//
//	private void setupCellAttributesForDetailColumn(HSSFCell cell, HSSFWorkbook workBook, XF110_SubListDetailColumn column, HSSFFont font) {
//		String wrk;
//
//		HSSFCellStyle style = workBook.createCellStyle();
//		style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
//		style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
//		style.setBorderRight(HSSFCellStyle.BORDER_THIN);
//		style.setBorderTop(HSSFCellStyle.BORDER_THIN);
//		style.setFont(font);
//		style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
//		style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
//		style.setWrapText(true);
//		style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
//
//		Object value = column.getExternalValue();
//		if (column.getBasicType().equals("INTEGER")) {
//			if (column.getTypeOptionList().contains("MSEQ") || column.getTypeOptionList().contains("FYEAR")) {
//				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
//				cell.setCellValue(new HSSFRichTextString(value.toString()));
//				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
//				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
//				style.setWrapText(true);
//				style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
//				cell.setCellStyle(style);
//			} else {
//				if (value == null) {
//					wrk = "";
//				} else {
//					wrk = XFUtility.getStringNumber(value.toString());
//				}
//				if (wrk.equals("") || column.getTypeOptionList().contains("NO_EDIT")) {
//					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
//					cell.setCellStyle(style);
//					cell.setCellValue(new HSSFRichTextString(wrk));
//				} else {
//					cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
//					style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
//					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
//					if (!column.getTypeOptionList().contains("NO_EDIT")
//							&& !column.getTypeOptionList().contains("ZERO_SUPPRESS")) {
//						style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
//					}
//					cell.setCellStyle(style);
//					cell.setCellValue(Double.parseDouble(wrk));
//				}
//			}
//		} else {
//			if (column.getBasicType().equals("FLOAT")) {
//				if (value == null) {
//					wrk = "";
//				} else {
//					wrk = XFUtility.getStringNumber(value.toString());
//				}
//				if (wrk.equals("") || column.getTypeOptionList().contains("NO_EDIT")) {
//					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
//					cell.setCellStyle(style);
//					cell.setCellValue(new HSSFRichTextString(wrk));
//				} else {
//					cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
//					style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
//					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
//					if (!column.getTypeOptionList().contains("NO_EDIT")
//							&& !column.getTypeOptionList().contains("ZERO_SUPPRESS")) {
//						style.setDataFormat(XFUtility.getFloatFormat(workBook, column.getDecimalSize()));
//					}
//					cell.setCellStyle(style);
//					cell.setCellValue(Double.parseDouble(wrk));
//				}
//			} else {
//				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
//				cell.setCellStyle(style);
//				if (value == null || column.getValueType().equals("IMAGE")) {
//					wrk = "";
//				} else {
//					if (column.getValueType().equals("FLAG")) {
//						wrk = column.getInternalValue().toString();
//					} else {
//						wrk = value.toString();
//					}
//				}
//				cell.setCellValue(new HSSFRichTextString(wrk));
//			}
//		}
//	}
	private URI getExcellBookURI() {
		File xlsFile = null;
		String xlsFileName = "";
		FileOutputStream fileOutputStream = null;
		XF110_SubListDetailRowNumber rowObject;
		String imageFileName = "";
		String wrkStr;

		XSSFWorkbook workBook = new XSSFWorkbook();
		wrkStr = functionElement_.getAttribute("Name").replace("/", "_").replace("^", "_");
		XSSFSheet workSheet = workBook.createSheet(wrkStr);
		workSheet.setDefaultRowHeight( (short) 300);
		Footer workSheetFooter = workSheet.getFooter();
		workSheetFooter.setRight(functionElement_.getAttribute("Name") + "  Page &P / &N");
		XSSFDataFormat format = workBook.createDataFormat();

		XSSFFont fontDefault = workBook.createFont();
		fontDefault = workBook.createFont();
		fontDefault.setFontName(session_.systemFont);
		fontDefault.setFontHeightInPoints((short)11);

		XSSFCellStyle styleHeaderLabel = workBook.createCellStyle();
		styleHeaderLabel.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderRight(XSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderTop(XSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
		styleHeaderLabel.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		styleHeaderLabel.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
		styleHeaderLabel.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		styleHeaderLabel.setFont(fontDefault);

		XSSFCellStyle styleDetailLabel = workBook.createCellStyle();
		styleDetailLabel.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderRight(XSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderTop(XSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
		styleDetailLabel.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		styleDetailLabel.setAlignment(XSSFCellStyle.ALIGN_LEFT);
		styleDetailLabel.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		styleDetailLabel.setFont(fontDefault);
		styleDetailLabel.setWrapText(true);

		XSSFCellStyle styleDetailNumberLabel = workBook.createCellStyle();
		styleDetailNumberLabel.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderRight(XSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderTop(XSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
		styleDetailNumberLabel.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		styleDetailNumberLabel.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
		styleDetailNumberLabel.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		styleDetailNumberLabel.setFont(fontDefault);

		XSSFCellStyle styleDataInteger = workBook.createCellStyle();
		styleDataInteger.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderRight(XSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderTop(XSSFCellStyle.BORDER_THIN);
		styleDataInteger.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
		styleDataInteger.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
		styleDataInteger.setDataFormat(format.getFormat("#,##0"));

		XSSFCellStyle styleRowNumber = workBook.createCellStyle();
		styleRowNumber.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		styleRowNumber.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		styleRowNumber.setBorderRight(XSSFCellStyle.BORDER_THIN);
		styleRowNumber.setBorderTop(XSSFCellStyle.BORDER_THIN);
		styleRowNumber.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
		styleRowNumber.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
		styleRowNumber.setDataFormat(format.getFormat("###0"));
		styleRowNumber.setFont(fontDefault);

		int currentRowNumber = -1;
		int mergeRowNumberFrom = -1;

		try {
			xlsFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".xlsx");
			xlsFileName = xlsFile.getPath();
			fileOutputStream = new FileOutputStream(xlsFileName);
			int columnIndex;
			
			////////////////////////
			// Header field lines //
			////////////////////////
			for (int i = 0; i < batchFieldList.size() && i < 24; i++) {
				if (batchFieldList.get(i).isVisibleOnPanel()) {
					for (int j = 0; j < batchFieldList.get(i).getRows(); j++) {

						currentRowNumber++;
						XSSFRow rowData = workSheet.createRow(currentRowNumber);

						//////////////////////////////////
						// Cells for header field label //
						//////////////////////////////////
						XSSFCell cellHeader = rowData.createCell(0);
						cellHeader.setCellStyle(styleHeaderLabel);
						if (j==0) {
							mergeRowNumberFrom = currentRowNumber;
							if (!batchFieldList.get(i).getFieldOptionList().contains("NO_CAPTION")) {
								cellHeader.setCellValue(new XSSFRichTextString(batchFieldList.get(i).getCaption()));
							}
						}
						rowData.createCell(1).setCellStyle(styleHeaderLabel);

						/////////////////////////////////
						// Cells for header field data //
						/////////////////////////////////
						setupCellAttributesForHeaderField(rowData, workBook, workSheet, batchFieldList.get(i), currentRowNumber, j);
					}
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 0, 1));
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 2, 6));
				}
			}
			
			////////////
			// Spacer //
			////////////
			currentRowNumber++;
			workSheet.createRow(currentRowNumber);

			//////////////////////////////////////////
			// Column heading line for detail items //
			//////////////////////////////////////////
			currentRowNumber++;
			XSSFRow rowCaption = workSheet.createRow(currentRowNumber);
			XSSFCell cell = rowCaption.createCell(0);
			cell.setCellStyle(styleDetailNumberLabel);
			workSheet.setColumnWidth(0, headersRenderer.getSequenceWidth() * 40);
			wrkStr = XFUtility.getCaptionForCell(headersRenderer.getSequenceLabel());
			cell.setCellValue(new XSSFRichTextString(wrkStr));
			for (int j = 0; j < detailColumnList.size(); j++) {
				if (detailColumnList.get(j).isVisibleOnPanel()) {
					cell = rowCaption.createCell(j+1);
					if (detailColumnList.get(j).getBasicType().equals("INTEGER")
							|| detailColumnList.get(j).getBasicType().equals("FLOAT")) {
						if (detailColumnList.get(j).getTypeOptionList().contains("MSEQ") || detailColumnList.get(j).getTypeOptionList().contains("FYEAR")) {
							cell.setCellStyle(styleDetailLabel);
						} else {
							cell.setCellStyle(styleDetailNumberLabel);
						}
					} else {
						cell.setCellStyle(styleDetailLabel);
					}
					Rectangle rect = headersRenderer.getColumnHeaderList().get(j).getBounds();
					workSheet.setColumnWidth(j+1, rect.width * 40);
					wrkStr = XFUtility.getCaptionForCell(headersRenderer.getColumnHeaderList().get(j).getText());
					wrkStr = wrkStr.replaceAll("<html>" , "");
					wrkStr = wrkStr.replaceAll("<u>" , "");
					cell.setCellValue(new XSSFRichTextString(wrkStr));
				} else {
					break;
				}
			}

			for (int i = 0; i < tableModelMain.getRowCount(); i++) {
				currentRowNumber++;
				XSSFRow rowData = workSheet.createRow(currentRowNumber);

				cell = rowData.createCell(0); //Column of Sequence Number
				cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellStyle(styleDataInteger);
				cell.setCellValue(i + 1);

				rowObject = (XF110_SubListDetailRowNumber)tableModelMain.getValueAt(i,0);
				rowObject.setValuesToDetailColumns();
				columnIndex = 0;
				for (int j = 0; j < detailColumnList.size(); j++) {
					if (detailColumnList.get(j).isVisibleOnPanel()) {
						columnIndex++;
						setupCellAttributesForDetailColumn(rowData.createCell(columnIndex), workBook, detailColumnList.get(j));
						if (detailColumnList.get(j).getValueType().equals("IMAGE") && !detailColumnList.get(j).getInternalValue().equals("")) {
							imageFileName = session_.getImageFileFolder() + detailColumnList.get(j).getInternalValue();
							XFUtility.setupImageCell(workBook, workSheet, currentRowNumber, currentRowNumber+1, columnIndex, columnIndex+1, imageFileName);
						}
					}
				}
			}

			workBook.write(fileOutputStream);
			messageList.add(XFUtility.RESOURCE.getString("XLSComment1"));

		} catch (Exception e) {
			messageList.add(XFUtility.RESOURCE.getString("XLSErrorMessage"));
			e.printStackTrace(dialog_.getExceptionStream());
		} finally {
			try {
				fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace(dialog_.getExceptionStream());
			}
		}
		return xlsFile.toURI();
	}

	private void setupCellAttributesForHeaderField(XSSFRow rowData, XSSFWorkbook workBook, XSSFSheet workSheet, XF110_SubListBatchField object, int currentRowNumber, int rowIndexInCell) {
		String wrk;

		XSSFFont font = workBook.createFont();
		font.setFontHeightInPoints((short)11);
		font.setFontName(session_.systemFont);
		if (object.getForeground() != Color.BLACK) {
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

		String basicType = object.getBasicType();
		if (basicType.equals("INTEGER")) {
			if (object.getTypeOptionList().contains("MSEQ") || object.getTypeOptionList().contains("FYEAR")) {
				cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
				cellValue.setCellStyle(style);
				cellValue.setCellValue(new XSSFRichTextString((String)object.getExternalValue()));
			} else {
				wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
				if (wrk.equals("") || object.getTypeOptionList().contains("NO_EDIT")) {
					cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(new XSSFRichTextString(""));
					}
				} else {
					cellValue.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
					style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
					style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
					if (!object.getTypeOptionList().contains("NO_EDIT")
						&& !object.getTypeOptionList().contains("ZERO_SUPPRESS")) {
						style.setDataFormat(format.getFormat("#,##0"));
					}
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(Double.parseDouble(wrk));
					}
				}
			}
		} else {
			if (basicType.equals("FLOAT")) {
				wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
				if (wrk.equals("") || object.getTypeOptionList().contains("NO_EDIT")) {
					cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(new XSSFRichTextString(""));
					}
				} else {
					cellValue.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
					style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
					style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
					if (!object.getTypeOptionList().contains("NO_EDIT")
							&& !object.getTypeOptionList().contains("ZERO_SUPPRESS")) {
						style.setDataFormat(XFUtility.getFloatFormat(workBook, object.getDecimalSize()));
					}
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(Double.parseDouble(wrk));
					}
				}
			} else {
				cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
				cellValue.setCellStyle(style);
				if (rowIndexInCell==0) {
					if (basicType.equals("STRING")) {
						if (object.isImage()) {
							XFUtility.setupImageCell(workBook, workSheet, currentRowNumber, currentRowNumber + object.getRows(), 2, 6, (String)object.getExternalValue());
						} else {
							cellValue.setCellStyle(style);
							cellValue.setCellValue(new XSSFRichTextString((String)object.getExternalValue()));
						}
					} else {
						if (basicType.equals("DATE")) {
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
		}
		rowData.createCell(3).setCellStyle(style);
		rowData.createCell(4).setCellStyle(style);
		rowData.createCell(5).setCellStyle(style);
		rowData.createCell(6).setCellStyle(style);
	}

	private void setupCellAttributesForDetailColumn(XSSFCell cell, XSSFWorkbook workBook, XF110_SubListDetailColumn column) {
		String wrk;

		XSSFFont font = workBook.createFont();
		font.setFontHeightInPoints((short)11);
		font.setFontName(session_.systemFont);
		if (column.getForeground() != Color.BLACK) {
			font.setColor(new XSSFColor(column.getForeground()));
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

		Object value = column.getExternalValue();
		if (column.getBasicType().equals("INTEGER")) {
			if (column.getTypeOptionList().contains("MSEQ") || column.getTypeOptionList().contains("FYEAR")) {
				cell.setCellType(XSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(new XSSFRichTextString(value.toString()));
				style.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
				style.setWrapText(true);
				style.setDataFormat(format.getFormat("text"));
				cell.setCellStyle(style);
			} else {
				if (value == null) {
					wrk = "";
				} else {
					wrk = XFUtility.getStringNumber(value.toString());
				}
				if (wrk.equals("") || column.getTypeOptionList().contains("NO_EDIT")) {
					cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					cell.setCellStyle(style);
					cell.setCellValue(new XSSFRichTextString(wrk));
				} else {
					cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
					style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
					style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
					if (!column.getTypeOptionList().contains("NO_EDIT")
							&& !column.getTypeOptionList().contains("ZERO_SUPPRESS")) {
						style.setDataFormat(format.getFormat("#,##0"));
					}
					cell.setCellStyle(style);
					cell.setCellValue(Double.parseDouble(wrk));
				}
			}
		} else {
			if (column.getBasicType().equals("FLOAT")) {
				if (value == null) {
					wrk = "";
				} else {
					wrk = XFUtility.getStringNumber(value.toString());
				}
				if (wrk.equals("") || column.getTypeOptionList().contains("NO_EDIT")) {
					cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					cell.setCellStyle(style);
					cell.setCellValue(new XSSFRichTextString(wrk));
				} else {
					cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
					style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
					style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
					if (!column.getTypeOptionList().contains("NO_EDIT")
							&& !column.getTypeOptionList().contains("ZERO_SUPPRESS")) {
						style.setDataFormat(XFUtility.getFloatFormat(workBook, column.getDecimalSize()));
					}
					cell.setCellStyle(style);
					cell.setCellValue(Double.parseDouble(wrk));
				}
			} else {
				cell.setCellType(XSSFCell.CELL_TYPE_STRING);
				cell.setCellStyle(style);
				if (value == null || column.getValueType().equals("IMAGE")) {
					wrk = "";
				} else {
					if (column.getValueType().equals("FLAG")) {
						wrk = column.getInternalValue().toString();
					} else {
						wrk = value.toString();
					}
				}
				cell.setCellValue(new XSSFRichTextString(wrk));
			}
		}
	}
	
	void jTableMain_focusGained(FocusEvent e) {
		if (jTableMain.getRowCount() > 0) {
			jTableMain.setSelectionBackground(selectionColorWithFocus);
			jTableMain.setSelectionForeground(Color.white);
		}
	}

	void jTableMain_focusLost(FocusEvent e) {
		jTableMain.setSelectionBackground(selectionColorWithoutFocus);
		jTableMain.setSelectionForeground(Color.black);
	}

	void jScrollPaneTable_mousePressed(MouseEvent e) {
		jTableMain.requestFocus();
	}

	void setMessagesOnPanel() {
		XF110_SubListDetailRowNumber rowObject;
		int workRow = 0;
		boolean topErrorFieldNotFound = true;
		for (int i = 0; i < batchFieldList.size(); i++) {
			if (batchFieldList.get(i).isError()) {
				if (topErrorFieldNotFound) {
					batchFieldList.get(i).requestFocus();
					topErrorFieldNotFound = false;
				}
				if (batchFieldList.get(i).isVisibleOnPanel()) {
					messageList.add(workRow, batchFieldList.get(i).getCaption() + XFUtility.RESOURCE.getString("Colon") + batchFieldList.get(i).getError());
				} else {
					messageList.add(workRow, batchFieldList.get(i).getError());
				}
				workRow++;
			}
		}
		if (topErrorFieldNotFound) {
			for (int i = 0; i < jTableMain.getRowCount(); i++) {
				rowObject = (XF110_SubListDetailRowNumber)tableModelMain.getValueAt(i, 0);
				if (rowObject.getFirstErrorCellIndex() > -1) {
					cellsEditor.requestFocusOnCellAt(i, rowObject.getFirstErrorCellIndex());
					break;
				}
			}
		}

		jTextAreaMessages.setText("");
		StringBuffer sb = new StringBuffer("");
		int countErrors = 0;
		for (int i = 0; i < messageList.size(); i++) {
			if (countErrors > 0) {
				sb.append("\n");
			}
			countErrors++;
			if (messageList.size() > 1) {
				sb.append("(" + Integer.toString(countErrors) + "/"+ Integer.toString(messageList.size()) + ") " + messageList.get(i));
			} else {
				sb.append(messageList.get(i));
			}
		}
		jTextAreaMessages.setText(sb.toString());

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

	public String getFunctionID() {
		return functionElement_.getAttribute("ID");
	}

	public String getPrimaryTableID() {
		return dialog_.getPrimaryTableID();
	}

	public StringBuffer getProcessLog() {
		return dialog_.getProcessLog();
	}

	public Object getVariant(String variantID) {
		return dialog_.getVariant(variantID);
	}

	public void setVariant(String variantID, Object value) {
		dialog_.setVariant(variantID, value);
	}

	public String getScriptNameRunning() {
		return scriptNameRunning;
	}

	public org.w3c.dom.Element getFunctionElement() {
		return functionElement_;
	}

	public ArrayList<String> getKeyFieldList() {
		return detailTable.getKeyFieldIDList();
	}

	public ArrayList<String> getBatchTableKeyFieldList() {
		return batchTable.getKeyFieldIDList();
	}

	public Session getSession() {
		return session_;
	}
	
	public String getFunctionInfo() {
		return jLabelFunctionID.getText();
	}

	public PrintStream getExceptionStream() {
		return dialog_.getExceptionStream();
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
	
	public XF110_SubListBatchTable getBatchTable() {
		return batchTable;
	}
	
	public XF110_SubListBatchField getFirstEditableBatchField() {
		return firstEditableBatchField;
	}
	
	public void setFirstEditableBatchField(XF110_SubListBatchField field) {
		firstEditableBatchField = field;
	}
	
	public ArrayList<XF110_SubListBatchReferTable> getHeaderReferTableList() {
		return batchReferTableList;
	}

	public ArrayList<XF110_SubListBatchField> getBatchFieldList() {
		return batchFieldList;
	}

	public ArrayList<String> getBatchWithKeyList() {
		return batchWithKeyList;
	}
	
	public JCheckBox getCheckBoxToExecuteBatchFunction() {
		return jCheckBoxToExecuteBatchFunction;
	}

	public ArrayList<String> getBatchKeyList() {
		return batchKeyList;
	}

	public XF110_SubListBatchField getBatchFieldObjectByID(String tableID, String tableAlias, String fieldID) {
		XF110_SubListBatchField batchField = null;
		for (int i = 0; i < batchFieldList.size(); i++) {
			if (tableID.equals("")) {
				if (batchFieldList.get(i).getTableAlias().equals(tableAlias) && batchFieldList.get(i).getFieldID().equals(fieldID)) {
					batchField = batchFieldList.get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (batchFieldList.get(i).getTableID().equals(tableID) && batchFieldList.get(i).getFieldID().equals(fieldID)) {
					batchField = batchFieldList.get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (batchFieldList.get(i).getTableID().equals(tableID) && batchFieldList.get(i).getTableAlias().equals(tableAlias) && batchFieldList.get(i).getFieldID().equals(fieldID)) {
					batchField = batchFieldList.get(i);
					break;
				}
			}
		}
		return batchField;
	}

	public boolean containsBatchField(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < batchFieldList.size(); i++) {
			if (tableID.equals("")) {
				if (batchFieldList.get(i).getTableAlias().equals(tableAlias)
						&& batchFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (batchFieldList.get(i).getTableID().equals(tableID)
						&& batchFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (batchFieldList.get(i).getTableID().equals(tableID)
						&& batchFieldList.get(i).getTableAlias().equals(tableAlias)
						&& batchFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public boolean containsDetailField(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < detailColumnList.size(); i++) {
			if (tableID.equals("")) {
				if (detailColumnList.get(i).getTableAlias().equals(tableAlias)
						&& detailColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID)
						&& detailColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID)
						&& detailColumnList.get(i).getTableAlias().equals(tableAlias)
						&& detailColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public XF110_SubListDetailTable getDetailTable() {
		return detailTable;
	}

	public TableModelEditableList getTableModel() {
		return tableModelMain;
	}

	public JTable getJTableMain() {
		return jTableMain;
	}
	
	public TableCellsEditor getCellsEditor() {
		return cellsEditor;
	}

	public ArrayList<String> getMessageList() {
		return messageList;
	}

	public ArrayList<XF110_SubListDetailColumn> getDetailColumnList() {
		return detailColumnList;
	}

	public ArrayList<XF110_SubListDetailReferTable> getDetailReferTableList() {
		return detailReferTableList;
	}

	public ArrayList<XF110_SubListBatchReferTable> getBatchReferTableList() {
		return batchReferTableList;
	}

	public XF110_SubListDetailColumn getDetailColumnObjectByID(String tableID, String tableAlias, String fieldID) {
		XF110_SubListDetailColumn detailColumnField = null;
		for (int i = 0; i < detailColumnList.size(); i++) {
			if (tableID.equals("")) {
				if (detailColumnList.get(i).getTableAlias().equals(tableAlias) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID) && detailColumnList.get(i).getTableAlias().equals(tableAlias) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
		}
		return detailColumnField;
	}

	public String getTableIDOfTableAlias(String tableAlias) {
		String tableID = tableAlias;
		org.w3c.dom.Element workElement;
		if (batchReferElementList != null) {
			for (int j = 0; j < batchReferElementList.getLength(); j++) {
				workElement = (org.w3c.dom.Element)batchReferElementList.item(j);
				if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
					tableID = workElement.getAttribute("ToTable");
					break;
				}
			}
		}
		if (detailReferElementList != null) {
			for (int j = 0; j < detailReferElementList.getLength(); j++) {
				workElement = (org.w3c.dom.Element)detailReferElementList.item(j);
				if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
					tableID = workElement.getAttribute("ToTable");
					break;
				}
			}
		}
		return tableID;
	}

	public Object getValueOfBatchFieldByName(String dataSourceName) {
		Object obj = null;
		for (int i = 0; i < batchFieldList.size(); i++) {
			if (batchFieldList.get(i).getDataSourceName().equals(dataSourceName)) {
				obj = batchFieldList.get(i).getInternalValue();
				break;
			}
		}
		return obj;
	}
}

class XF110_SubListBatchField extends XFFieldScriptable {
	private static final long serialVersionUID = 1L;
	org.w3c.dom.Element functionFieldElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private String tableID_ = "";
	private String tableAlias_ = "";
	private String fieldID_ = "";
	private String fieldName = "";
	private String fieldRemarks = "";
	private String dataType = "";
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private String fieldCaption = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private String fieldOptions = "";
	private ArrayList<String> fieldOptionList;
	private String byteaTypeFieldID = "";
	private int fieldRows = 1;
	private JPanel jPanelField = new JPanel();
	private JLabel jLabelField = new JLabel();
	private JPanel jPanelFieldComment = null;
	private JLabel jLabelFieldComment = new JLabel();
	private XFEditableField component = null;
	private XFEditableField refferComponent = null;
	private JButton jButtonToRefferZipNo = null;
	private boolean isKey = false;
	private boolean isNullable = true;
	private boolean isFieldOnBatchTable = false;
	private boolean isVisibleOnPanel = true;
	private boolean isEnabled = true;
	private boolean isEditable = true;
	private boolean isHorizontal = false;
	private boolean isVirtualField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private boolean isImage = false;
	private boolean isError = false;
	private String autoNumberKey = "";
	private String errorMessage = "";
	private int positionMargin = 0;
	private Color foreground = Color.black;
	private XF110_SubList dialog_;

	public XF110_SubListBatchField(org.w3c.dom.Element functionFieldElement, XF110_SubList dialog){
		super();
		String wrkStr;
		dialog_ = dialog;
		functionFieldElement_ = functionFieldElement;
		fieldOptions = functionFieldElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);

		StringTokenizer workTokenizer = new StringTokenizer(functionFieldElement_.getAttribute("DataSource"), "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog.getTableIDOfTableAlias(tableAlias_);
		fieldID_ =workTokenizer.nextToken();

		if (tableID_.equals(dialog_.getBatchTable().getTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnBatchTable = true;
			ArrayList<String> keyFieldList = dialog_.getBatchTable().getKeyFieldIDList();
			for (int i = 0; i < keyFieldList.size(); i++) {
				if (keyFieldList.get(i).equals(fieldID_)) {
					isKey = true;
					isEditable = false;
					break;
				}
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

		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		fieldName = workElement.getAttribute("Name");
		fieldRemarks = XFUtility.substringLinesWithTokenOfEOL(workElement.getAttribute("Remarks"), "<br>");
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
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			autoNumberKey = wrkStr;
		}
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

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

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = XFUtility.getCaptionValue(wrkStr, dialog_.getSession());
		}
		jLabelField.setText(fieldCaption);
		jLabelField.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelField.setVerticalAlignment(SwingConstants.TOP);
		jLabelField.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		if (fieldOptionList.contains("CAPTION_LENGTH_VARIABLE")) {
			FontMetrics metrics = jLabelField.getFontMetrics(jLabelField.getFont());
			jLabelField.setPreferredSize(new Dimension(metrics.stringWidth(fieldCaption), XFUtility.FIELD_UNIT_HEIGHT));
		} else {
			jLabelField.setPreferredSize(new Dimension(XFUtility.DEFAULT_LABEL_WIDTH, XFUtility.FIELD_UNIT_HEIGHT));
			XFUtility.adjustFontSizeToGetPreferredWidthOfLabel(jLabelField, XFUtility.DEFAULT_LABEL_WIDTH);
		}

		if (fieldOptionList.contains("PROMPT_LIST")) {
			isEditable = true;
			XF110_SubListBatchReferTable referTable = null;
			ArrayList<XF110_SubListBatchReferTable> referTableList = dialog_.getHeaderReferTableList();
			for (int i = 0; i < referTableList.size(); i++) {
				if (referTableList.get(i).getTableID().equals(tableID_)) {
					if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias_)) {
						referTable = referTableList.get(i);
						break;
					}
				}
			}
			component = new XF110_SubListBatchComboBox(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, referTable, isNullable);
			component.setLocation(5, 0);
		} else {
			wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
			if (!wrkStr.equals("")) {
				isEditable = true;
				component = new XF110_SubListBatchPromptCall(functionFieldElement_, wrkStr, dialog_);
				component.setLocation(5, 0);
			} else {
				if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN").equals("") || !XFUtility.getOptionValueWithKeyword(dataTypeOptions, "VALUES").equals("")) {
					if (this.isFieldOnBatchTable) {
						component = new XF110_SubListBatchComboBox(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, null, isNullable);
					} else {
						component = new XF110_SubListBatchCodeText(functionFieldElement_.getAttribute("DataSource"), dataTypeOptions, dialog_);
					}
					component.setLocation(5, 0);
				} else {
					if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN").equals("")) {
						component = new XFCheckBox(dataTypeOptions);
						component.setLocation(5, 0);
					} else {
						if (dataType.equals("VARCHAR") || dataType.equals("LONG VARCHAR")) {
							component = new XFTextArea(dataTypeOptions, fieldOptions, dialog_.getSession().systemFont);
							component.setLocation(5, 0);
							component.setEditable(false);
						} else {
							if (dataTypeOptionList.contains("URL")) {
								component = new XFUrlField(dataSize, fieldOptions, dialog_.getSession().systemFont);
								component.setLocation(5, 0);
								component.setEditable(false);
							} else {
								if (dataTypeOptionList.contains("IMAGE")) {
									component = new XFImageField(fieldOptions, dataSize, dialog_.getSession().getImageFileFolder(), dialog_.getSession().systemFont);
									component.setLocation(5, 0);
									isImage = true;
								} else {
									if (dataType.equals("DATE")) {
										component = new XFDateField(dialog_.getSession());
										component.setLocation(5, 0);
										component.setEditable(false);
									} else {
										if (dataTypeOptionList.contains("YMONTH")) {
											component = new XFYMonthBox(dialog_.getSession());
											component.setLocation(5, 0);
											component.setEditable(false);
										} else {
											if (dataTypeOptionList.contains("MSEQ")) {
												component = new XFMSeqBox(dialog_.getSession());
												component.setLocation(5, 0);
												component.setEditable(false);
											} else {
												if (dataTypeOptionList.contains("FYEAR")) {
													component = new XFFYearBox(dialog_.getSession());
													component.setLocation(5, 0);
													component.setEditable(false);
												} else {
													if (this.getBasicType().equals("BYTEA")) {
														component = new XFByteaField(byteaTypeFieldID, fieldOptions, dialog_.getSession());
														component.setLocation(5, 0);
													} else {
														component = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions, dialog_.getSession().systemFont);
														component.setLocation(5, 0);
														component.setEditable(false);
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
		if (isEditable) {	
			component.setEditable(true);
			if (dialog_.getFirstEditableBatchField() == null) {
				dialog_.setFirstEditableBatchField(this);
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

		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		if (fieldOptionList.contains("NO_CAPTION")) {
			this.setPreferredSize(new Dimension(component.getWidth() + 10, component.getHeight()));
		} else {
			this.setPreferredSize(new Dimension(component.getWidth() + jLabelField.getPreferredSize().width + 10, component.getHeight()));
			this.add(jLabelField, BorderLayout.WEST);
		}
		this.add(jPanelField, BorderLayout.CENTER);

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "COMMENT");
		if (!wrkStr.equals("")) {
			jLabelFieldComment.setText(wrkStr);
			jLabelFieldComment.setForeground(Color.blue);
			jLabelFieldComment.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
			//jLabelFieldComment.setVerticalAlignment(SwingConstants.TOP);
			FontMetrics metrics = jLabelFieldComment.getFontMetrics(jLabelFieldComment.getFont());
			this.setPreferredSize(new Dimension(this.getPreferredSize().width + metrics.stringWidth(wrkStr) + 6, this.getPreferredSize().height));
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

		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}

		if (decimalSize > 0) {
			wrkStr = "<html>" + fieldName + " " + tableAlias_ + "." + fieldID_ + " (" + dataSize + "," + decimalSize + ")<br>" + fieldRemarks;
		} else {
			wrkStr = "<html>" + fieldName + " " + tableAlias_ + "." + fieldID_ + " (" + dataSize + ")<br>" + fieldRemarks;
		}
		this.setToolTipText(wrkStr);
		component.setToolTipText(wrkStr);
	}

	public XF110_SubListBatchField(String tableID, String tableAlias, String fieldID, XF110_SubList dialog){
		super();
		String wrkStr;
		dialog_ = dialog;
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

		if (tableID_.equals(dialog_.getBatchTable().getTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnBatchTable = true;
			ArrayList<String> keyFieldList = dialog_.getBatchTable().getKeyFieldIDList();
			for (int i = 0; i < keyFieldList.size(); i++) {
				if (keyFieldList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
		}

		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
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
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			autoNumberKey = wrkStr;
		}
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

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

		component = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions, dialog_.getSession().systemFont);
		component.setLocation(5, 0);
		component.setEditable(false);

		fieldRows = component.getRows();
		jPanelField.setLayout(null);
		jPanelField.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		jPanelField.add((JComponent)component);

		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(component.getWidth() + 130, component.getHeight()));
		this.add(jPanelField, BorderLayout.CENTER);

		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
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

	public boolean isAutoNumberField() {
		return !autoNumberKey.equals("") && isFieldOnBatchTable;
	}

	public boolean isNull(){
		boolean isNull = false;
		String basicType = this.getBasicType();
		if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
			String value = (String)component.getInternalValue();
			if (value == null || value.equals("") || value.equals("0") || value.equals("0.0")) {
				isNull = true;
			}
		}
		if (basicType.equals("STRING") || basicType.equals("DATETIME") || basicType.equals("TIME")) {
			String value = (String)component.getInternalValue();
			if (value == null || value.equals("")) {
				isNull = true;
			}
		}
		if (basicType.equals("DATE")) {
			Object value = (java.sql.Date)component.getInternalValue();
			if (value == null || value.equals("")) {
				isNull = true;
			}
		}
		return isNull;
	}

	public String getTableID(){
		return tableID_;
	}

	public String getTableAlias(){
		return tableAlias_;
	}

	public String getFieldID(){
		return fieldID_;
	}

	public String getByteaTypeFieldID(){
		return byteaTypeFieldID;
	}

	public String getDataSourceName(){
		return tableAlias_ + "." + fieldID_;
	}

	public int getDecimalSize(){
		return decimalSize;
	}

	public String getCaption(){
		return fieldCaption;
	}

	public boolean isVisibleOnPanel(){
		return isVisibleOnPanel;
	}

	public boolean isHorizontal(){
		return isHorizontal;
	}

	public int getPositionMargin(){
		return positionMargin;
	}

	public boolean isNullable(){
		return isNullable;
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

	public boolean isFieldOnBatchTable(){
		return isFieldOnBatchTable;
	}

	public boolean isKey(){
		return isKey;
	}

	public boolean isNullError(){
		String basicType = this.getBasicType();
		boolean isNullError = false;
		if (this.isVisibleOnPanel && this.isFieldOnBatchTable) {
			if (basicType.equals("INTEGER")) {
				if (!this.isNullable) {
					long value = Long.parseLong((String)component.getInternalValue());
					if (value == 0) {
						isNullError = true;	
					}
				}
			}
			if (basicType.equals("FLOAT")) {
				if (!this.isNullable) {
					double value = Double.parseDouble((String)component.getInternalValue());
					if (value == 0) {
						isNullError = true;
					}
				}
			}
			if (basicType.equals("DATE")) {
				if (!this.isNullable) {
					String strDate = (String)component.getInternalValue();
					if (strDate == null || strDate.equals("")) {
						isNullError = true;
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
			if (isNullError) {
				this.setError(XFUtility.RESOURCE.getString("FunctionError16"));
			}
		}
		return isNullError;
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

	public boolean isError() {
		return isError;
	}

	public void setValueOfResultSet(XFTableOperator operator){
		try {
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					component.setValue(XFUtility.calculateExpireValue(this.getTableElement(), operator, dialog_.getSession(), dialog_.getProcessLog()));
				}
			} else {
				Object value = operator.getValueOf(this.getFieldID()); 
				if (this.getBasicType().equals("INTEGER")) {
					if (value == null || value.equals("")) {
						component.setValue("");
					} else {
						String wrkStr = value.toString();
						int pos = wrkStr.indexOf(".");
						if (pos >= 0) {
							wrkStr = wrkStr.substring(0, pos);
						}
						component.setValue(Long.parseLong(wrkStr));
					}
				} else {
					if (this.getBasicType().equals("FLOAT")) {
						if (value == null || value.equals("")) {
							component.setValue("");
						} else {
							component.setValue(Double.parseDouble(value.toString()));
						}
					} else {
//						if (value == null) {
//							component.setValue("");
//						} else {
//							component.setValue(value.toString().trim());
//						}
						if (this.getBasicType().equals("STRING") || this.getBasicType().equals("TIME") || this.getBasicType().equals("DATETIME")) {
							if (value == null) {
								component.setValue("");
							} else {
								component.setValue(value.toString().trim());
							}
						} else {
							if (this.getBasicType().equals("DATE") || this.getBasicType().equals("BYTEA")) {
								component.setValue(value);
							} else {
								if (value == null) {
									component.setValue("");
								} else {
									component.setValue(value.toString().trim());
								}
							}
						}
					}
				}
			}
			component.setOldValue(component.getInternalValue());
		} catch (Exception e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public String getAutoNumber() {
		String value = "";
		if (!autoNumberKey.equals("") && isFieldOnBatchTable) {
			value = dialog_.getSession().getNextNumber(autoNumberKey);
		}
		return value;
	}

	public void setValue(Object object){
		XFUtility.setValueToEditableField(this.getBasicType(), object, component);
	}
	
	public void setupByteaTypeField(ArrayList<XF110_SubListBatchField> fieldList) {
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getFieldID().equals(byteaTypeFieldID)) {
				((XFByteaField)component).setTypeField((XFFieldScriptable)fieldList.get(i));
				break;
			}
		}
	}
	
	public boolean isControledByFieldOtherThan(XFEditableField component) {
		boolean result = false;
		for (int i = 0; i < dialog_.getBatchFieldList().size(); i++) {
			if (dialog_.getBatchFieldList().get(i).getComponent() == component) {
				break;
			} else {
				if (dialog_.getBatchFieldList().get(i).getComponent() instanceof XF110_SubListBatchComboBox) {
					XF110_SubListBatchComboBox comboBox = (XF110_SubListBatchComboBox)dialog_.getBatchFieldList().get(i).getComponent();
					if (comboBox.getKeyFieldList().contains(tableAlias_+"."+fieldID_)) {
						result = true;
						break;
					}
				}
				if (dialog_.getBatchFieldList().get(i).getComponent() instanceof XF110_SubListBatchPromptCall) {
					XF110_SubListBatchPromptCall promptField = (XF110_SubListBatchPromptCall)dialog_.getBatchFieldList().get(i).getComponent();
					if (promptField.getControlFieldList().contains(tableAlias_+"."+fieldID_)) {
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	public Object getInternalValue(){
		Object returnObj = null;
		if (this.getBasicType().equals("BYTEA")) {
			returnObj = component.getInternalValue();
		} else {
			returnObj = (String)component.getInternalValue();
		}
		return returnObj;
	}

	public Object getExternalValue(){
		Object returnObj = (String)component.getExternalValue();
		return returnObj;
	}

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public Object getValue() {
		Object returnObj = null;
		if (this.getBasicType().equals("INTEGER")) {
			returnObj = Long.parseLong((String)component.getInternalValue());
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				returnObj = Double.parseDouble((String)component.getInternalValue());
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

	public void setOldValue(Object object){
	}

	public Object getOldValue() {
		return getValue();
	}
	
	public boolean isValueChanged() {
		return !this.getValue().equals(this.getOldValue());
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
		component.setEnabled(isEnabled);
		jLabelField.setEnabled(isEnabled);
	}

	public boolean isEditable() {
		return isEditable;
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

	public boolean isComponentFocusable() {
		return component.isEditable();
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
		component.setEditable(isEditable);
		if (jButtonToRefferZipNo != null) {
			if (isEditable) {
				jPanelFieldComment.add(jButtonToRefferZipNo, BorderLayout.WEST);
				jPanelFieldComment.repaint();
			} else {
				jPanelFieldComment.remove(jButtonToRefferZipNo);
				jPanelFieldComment.repaint();
			}
		}
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

	public Color getForeground() {
		return foreground;
	}

	public String getFieldIDInScript(){
		return tableAlias_ + "_" + fieldID_;
	}

	public void requestFocus() {
		component.requestFocus();
	}
}

class XF110_SubListCellEditorWithTextField extends XFTextField implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private XF110_SubList dialog_ = null;

	public XF110_SubListCellEditorWithTextField(XF110_SubListDetailColumn detailColumn, XF110_SubList dialog) {
		super(detailColumn.getBasicType(), detailColumn.getDataSize(), detailColumn.getDecimalSize(), detailColumn.getDataTypeOptions(), detailColumn.getFieldOptions(), dialog.getSession().systemFont);
		dialog_ = dialog;
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setEditable(true);
		if (detailColumn.getDataTypeOptions().contains("KANJI")) {
			this.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
		}
		if (detailColumn.getBasicType().equals("INTEGER") || detailColumn.getBasicType().equals("FLOAT")) {
			this.setHorizontalAlignment(SwingConstants.RIGHT);
		} else {
			this.setHorizontalAlignment(SwingConstants.LEFT);
		}
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
			}
		});
	}
	
	public void setColorOfError() {
		this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				this.setBackground(SystemColor.text);
			} else {
				this.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}            

class XF110_SubListCellEditorWithCheckBox extends XFCheckBox implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private XF110_SubList dialog_ = null;

	public XF110_SubListCellEditorWithCheckBox(String dataTypeOptions, XF110_SubList dialog) {
		super(dataTypeOptions);
		dialog_ = dialog;
		this.setOpaque(true);
		this.setBorderPainted(true);
		this.setHorizontalAlignment(SwingConstants.CENTER);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
			}
		});
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog_.getCellsEditor().updateRowObject();
			}
		});
	}

	public boolean isCellEditable(EventObject event) {
		return true;
	} 

	public Object getInternalValue() {
		return super.getInternalValue();
	}
	
	public void setValue(Object obj) {
		super.setValue(obj);
	}
	
	public void setColorOfError() {
		this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				this.setBackground(SystemColor.text);
			} else {
				this.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}            

class XF110_SubListCellEditorWithDateField extends XFDateField implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private XF110_SubList dialog_ = null;

	public XF110_SubListCellEditorWithDateField(XF110_SubList dialog) {
		super(dialog.getSession());
		dialog_ = dialog;
		this.setInternalBorder(null);
		this.setOpaque(false);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
			}
		});
	}

	public void setHorizontalAlignment(int alignment) {
	}
	
	public void setColorOfError() {
		this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				this.setBackground(SystemColor.text);
			} else {
				this.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}

class XF110_SubListCellEditorWithByteaColumn extends XFByteaColumn implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private XF110_SubList dialog_ = null;

	public XF110_SubListCellEditorWithByteaColumn(String typeFieldID, String fieldOptions, XF110_SubList dialog) {
		super(typeFieldID, fieldOptions, dialog.getSession());
		dialog_ = dialog;
		this.setFocusable(true);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
			}
		});
	}

	public void setHorizontalAlignment(int alignment) {
	}
	
	public void setColorOfError() {
		this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				this.setBackground(SystemColor.text);
			} else {
				this.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}            

class XF110_SubListCellEditorWithLongTextEditor extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JTextField jTextField;
	private JButton jButton = new JButton();
	private String fieldCaption_ = "";
	private ArrayList<String> dataTypeOptionList_ = null;
	private String textData = "";
    private XF110_SubList dialog_;
    private boolean isEditable_ = true;

	public XF110_SubListCellEditorWithLongTextEditor(String fieldCaption, ArrayList<String> dataTypeOptionList, XF110_SubList dialog){
		super();
		fieldCaption_ = fieldCaption;
		dataTypeOptionList_ = dataTypeOptionList;
		dialog_ = dialog;

		jTextField = new JTextField();
		jTextField.setOpaque(true);
		jTextField.setBorder(null);
		jTextField.setEditable(false);
		jTextField.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XF310.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textData = dialog_.getSession().getLongTextEditor().request(dialog_.getTitle(), fieldCaption_, dataTypeOptionList_, textData, isEditable_);
				jTextField.setText(textData);
			}
		});
		jButton.addKeyListener(new java.awt.event.KeyAdapter() {
			public void keyPressed(KeyEvent e)  {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jButton.doClick();
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					e.setSource(dialog_.getJTableMain());
					dialog_.getJTableMain().dispatchEvent(e);
				}
			}
		});

		this.setLayout(new BorderLayout());
		this.setFocusable(true);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jButton.requestFocus();
			}
		});
		this.add(jTextField, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
	}

	public void setHorizontalAlignment(int alignment) {
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
	}
	
	public boolean isEditable() {
		return isEditable_;
	}

	public Object getInternalValue() {
		return textData;
	}

	public Object getExternalValue() {
		return textData;
	}
	
	public void setValue(Object obj) {
		if (obj == null) {
			textData = "";
			jTextField.setText("");
		} else {
			textData = obj.toString();
			jTextField.setText(textData);
		}
	}
	
	public void setColorOfError() {
		jTextField.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (isEditable_) {
			if (row%2==0) {
				jTextField.setBackground(SystemColor.text);
			} else {
				jTextField.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			jTextField.setBackground(SystemColor.control);
		}
	}
}            

class XF110_SubListCellEditorWithYMonthBox extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JComboBox jComboBoxYear = new JComboBox();
	private JComboBox jComboBoxMonth = new JComboBox();
	private JLabel jLabel = new JLabel();
	private boolean isEditable_ = true;
	private XF110_SubList dialog_ = null;

	public XF110_SubListCellEditorWithYMonthBox(XF110_SubList dialog) {
		super();
		dialog_ = dialog;
		GregorianCalendar calendar = new GregorianCalendar();
		int currentYear = calendar.get(Calendar.YEAR);
		int minimumYear = currentYear - 10;
		int maximumYear = currentYear + 10;

		jComboBoxYear.setEditable(false);
		jComboBoxYear.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
		jComboBoxYear.setBounds(new Rectangle(0, 0, 70, XFUtility.ROW_UNIT_HEIGHT_EDITABLE));
		jComboBoxYear.addItem("");
		for (int i = minimumYear; i <= maximumYear; i++) {
			jComboBoxYear.addItem(String.valueOf(i));
		}
		jComboBoxYear.addItem("9999");
		jComboBoxYear.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				dialog_.getCellsEditor().updateRowObject();
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			}
		});
		jComboBoxMonth.setEditable(false);
		jComboBoxMonth.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
		jComboBoxMonth.setBounds(new Rectangle(70, 0, 50, XFUtility.ROW_UNIT_HEIGHT_EDITABLE));
		jComboBoxMonth.addItem("");
		jComboBoxMonth.addItem("01");
		jComboBoxMonth.addItem("02");
		jComboBoxMonth.addItem("03");
		jComboBoxMonth.addItem("04");
		jComboBoxMonth.addItem("05");
		jComboBoxMonth.addItem("06");
		jComboBoxMonth.addItem("07");
		jComboBoxMonth.addItem("08");
		jComboBoxMonth.addItem("09");
		jComboBoxMonth.addItem("10");
		jComboBoxMonth.addItem("11");
		jComboBoxMonth.addItem("12");
		jComboBoxMonth.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				dialog_.getCellsEditor().updateRowObject();
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			}
		});
		jLabel.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jLabel.setBackground(SystemColor.control);
		jLabel.setBounds(new Rectangle(0, 0, 120, XFUtility.ROW_UNIT_HEIGHT_EDITABLE));

		this.setSize(new Dimension(120, XFUtility.ROW_UNIT_HEIGHT_EDITABLE));
		this.setBorder(BorderFactory.createEmptyBorder());
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jComboBoxYear.setBackground(Color.cyan);
				jComboBoxMonth.setBackground(Color.cyan);
			}
			public void focusLost(FocusEvent e) {
				jComboBoxYear.setBackground(SystemColor.text);
				jComboBoxMonth.setBackground(SystemColor.text);
			}
		});
		this.setLayout(null);
		this.add(jComboBoxYear);
		this.add(jComboBoxMonth);
	}
	
	public void setHorizontalAlignment(int alignment) {
		jLabel.setHorizontalAlignment(alignment);
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		this.removeAll();
		if (isEditable) {
			this.add(jComboBoxYear);
			this.add(jComboBoxMonth);
		} else {
			//jLabel.setText(this.getExternalValue().toString());
			this.add(jLabel);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}

	public Object getInternalValue() {
		String year = (String)jComboBoxYear.getItemAt(jComboBoxYear.getSelectedIndex());
		String month = (String)jComboBoxMonth.getItemAt(jComboBoxMonth.getSelectedIndex());
		return year + month;
	}

	public Object getExternalValue() {
		return this.getInternalValue();
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		if (value != null) {
			value = value.trim();
		}
		if (value == null || value.equals("")) {
			jComboBoxYear.setSelectedIndex(0);
			jComboBoxMonth.setSelectedIndex(0);
		} else {
			if (value.length() == 6) {
				String yearValue = value.substring(0, 4);
				String monthValue = value.substring(4, 6);
				for (int i = 0; i < jComboBoxYear.getItemCount(); i++) {
					if (jComboBoxYear.getItemAt(i).equals(yearValue)) {
						jComboBoxYear.setSelectedIndex(i);
						break;
					}
				}
				for (int i = 0; i < jComboBoxMonth.getItemCount(); i++) {
					if (jComboBoxMonth.getItemAt(i).equals(monthValue)) {
						jComboBoxMonth.setSelectedIndex(i);
						break;
					}
				}
				jLabel.setText(XFUtility.getUserExpressionOfYearMonth(value, dialog_.getSession().getDateFormat()));
			}
		}
	}

	public void setBackground(Color color) {
		if (jComboBoxYear != null && jComboBoxMonth != null) {
			jComboBoxYear.setBackground(color);
			jComboBoxMonth.setBackground(color);
		}
	}
	
	public void setColorOfError() {
		jComboBoxYear.setBackground(XFUtility.ERROR_COLOR);
		jComboBoxMonth.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				jComboBoxYear.setBackground(SystemColor.text);
				jComboBoxMonth.setBackground(SystemColor.text);
			} else {
				jComboBoxYear.setBackground(XFUtility.ODD_ROW_COLOR);
				jComboBoxMonth.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			jComboBoxYear.setBackground(SystemColor.control);
			jComboBoxMonth.setBackground(SystemColor.control);
		}
	}
}

class XF110_SubListCellEditorWithImageField extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JLabel jLabel = new JLabel();
	private JButton jButton = new JButton();
	private XF110_SubList dialog_ = null;
	private String imageFileName_ = "";
	private boolean isEditable_ = false;

	public XF110_SubListCellEditorWithImageField(XF110_SubList dialog) {
		super();
		dialog_ = dialog;
		jLabel.setOpaque(true);
		jLabel.setFocusable(false);
		jLabel.setHorizontalAlignment(SwingConstants.CENTER);
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String answer = JOptionPane.showInputDialog(null, XFUtility.RESOURCE.getString("InputImageFileName"), imageFileName_);
				if (answer != null) {
					imageFileName_ = answer;
					dialog_.getCellsEditor().updateRowObject();
					jButton.setToolTipText(imageFileName_);
				}
			}
		});
		this.setLayout(new BorderLayout());
		this.add(jLabel, BorderLayout.CENTER);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jButton.requestFocus();
			}
		});
	}

	public void setHorizontalAlignment(int alignment) {
	}
	
	public Object getInternalValue() {
		return imageFileName_;
	}
	
	public Object getExternalValue() {
		return jLabel.getIcon();
	}
	
	public void setValue(Object obj) {
		imageFileName_ = obj.toString();
		String fileName = dialog_.getSession().getImageFileFolder() + imageFileName_;
		jLabel.setIcon(XFUtility.createSmallIcon(fileName, this.getWidth(), this.getHeight()));
		jLabel.setToolTipText(imageFileName_);
		jButton.setIcon(XFUtility.createSmallIcon(fileName, (int)Math.round(this.getWidth()* 0.8), (int)Math.round(this.getHeight()* 0.8)));
		jButton.setToolTipText(imageFileName_);
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		this.removeAll();
		if (isEditable) {
			this.add(jButton, BorderLayout.CENTER);
		} else {
			this.add(jLabel, BorderLayout.CENTER);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}
	
	public void setColorOfError() {
		jButton.setForeground(Color.red);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			jButton.setForeground(Color.black);
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}            

class XF110_SubListCellEditorWithFYearBox extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JComboBox jComboBoxYear = new JComboBox();
	private JLabel jLabel = new JLabel();
	private boolean isEditable_ = true;
	private XF110_SubList dialog_ = null;

	public XF110_SubListCellEditorWithFYearBox(XF110_SubList dialog) {
		super();
		dialog_ = dialog;
		GregorianCalendar calendar = new GregorianCalendar();
		int currentYear = calendar.get(Calendar.YEAR);
		int minimumYear = currentYear - 10;
		int maximumYear = currentYear + 10;

		jComboBoxYear.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
		jComboBoxYear.addItem("");
		for (int i = minimumYear; i <= maximumYear; i++) {
			jComboBoxYear.addItem(String.valueOf(i));
		}
		jComboBoxYear.addItem("9999");
		jComboBoxYear.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				dialog_.getCellsEditor().updateRowObject();
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			}
		});
		jLabel.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jLabel.setBackground(SystemColor.control);

		this.setSize(new Dimension(70, XFUtility.ROW_UNIT_HEIGHT_EDITABLE));
		this.setBorder(BorderFactory.createEmptyBorder());
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jComboBoxYear.requestFocus();
			}
		});
		this.setLayout(new BorderLayout());
		this.add(jComboBoxYear, BorderLayout.CENTER);
	}
	
	public void setHorizontalAlignment(int alignment) {
		jLabel.setHorizontalAlignment(alignment);
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		this.removeAll();
		if (isEditable) {
			this.add(jComboBoxYear, BorderLayout.CENTER);
		} else {
			//jLabel.setText(this.getExternalValue().toString());
			this.add(jLabel, BorderLayout.CENTER);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}

	public Object getInternalValue() {
		String year = (String)jComboBoxYear.getItemAt(jComboBoxYear.getSelectedIndex());
		return year;
	}

	public Object getExternalValue() {
		return this.getInternalValue();
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		if (value != null) {
			value = value.trim();
		}
		if (value == null || value.equals("")) {
			jComboBoxYear.setSelectedIndex(0);
		} else {
			if (value.length() == 4) {
				for (int i = 0; i < jComboBoxYear.getItemCount(); i++) {
					if (jComboBoxYear.getItemAt(i).equals(value)) {
						jComboBoxYear.setSelectedIndex(i);
						break;
					}
				}
			}
		}
		jLabel.setText(this.getExternalValue().toString());
	}

	public void setBackground(Color color) {
		if (jComboBoxYear != null) {
			jComboBoxYear.setBackground(color);
		}
	}
	
	public void setColorOfError() {
		this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				this.setBackground(SystemColor.text);
			} else {
				this.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}

class XF110_SubListCellEditorWithMSeqBox extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JComboBox jComboBoxMSeq = new JComboBox();
	private ArrayList<Integer> listMSeq = new ArrayList<Integer>();
	private JLabel jLabel = new JLabel();
	private boolean isEditable_ = true;
	private XF110_SubList dialog_ = null;
    private String language = "";
    private String[] monthArrayEn = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov"};
    private String[] monthArrayJp = {"‚PŒŽ“x","‚QŒŽ“x","‚RŒŽ“x","‚SŒŽ“x","‚TŒŽ“x","‚UŒŽ“x","‚VŒŽ“x","‚WŒŽ“x","‚XŒŽ“x","10ŒŽ“x","11ŒŽ“x","12ŒŽ“x","‚PŒŽ“x","‚QŒŽ“x","‚RŒŽ“x","‚SŒŽ“x","‚TŒŽ“x","‚UŒŽ“x","‚VŒŽ“x","‚WŒŽ“x","‚XŒŽ“x","10ŒŽ“x","11ŒŽ“x"};
    private int startMonth = 1;

	public XF110_SubListCellEditorWithMSeqBox(XF110_SubList dialog) {
		super();
		dialog_ = dialog;
		language = dialog_.getSession().getDateFormat().substring(0, 2);
		startMonth = dialog_.getSession().getSystemVariantInteger("FIRST_MONTH");

		jComboBoxMSeq.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
		jComboBoxMSeq.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				dialog_.getCellsEditor().updateRowObject();
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
			}
		});
		listMSeq.add(0);
		listMSeq.add(1);
		listMSeq.add(2);
		listMSeq.add(3);
		listMSeq.add(4);
		listMSeq.add(5);
		listMSeq.add(6);
		listMSeq.add(7);
		listMSeq.add(8);
		listMSeq.add(9);
		listMSeq.add(10);
		listMSeq.add(11);
		listMSeq.add(12);
		if (language.equals("en")) {
			jComboBoxMSeq.setBounds(new Rectangle(0, 0, 70, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(70, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMSeq.addItem("");
			for (int i = startMonth -1; i < startMonth + 11; i++) {
				jComboBoxMSeq.addItem(monthArrayEn[i]);
			}
		}

		if (language.equals("jp")) {
			jComboBoxMSeq.setBounds(new Rectangle(0, 0, 80, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(80, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMSeq.addItem("");
			for (int i = startMonth -1; i < startMonth + 11; i++) {
				jComboBoxMSeq.addItem(monthArrayJp[i]);
			}
		}
		jLabel.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jLabel.setBackground(SystemColor.control);

		this.setBorder(BorderFactory.createEmptyBorder());
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jComboBoxMSeq.requestFocus();
			}
		});
		this.setLayout(new BorderLayout());
		this.add(jComboBoxMSeq, BorderLayout.CENTER);
	}
	
	public void setHorizontalAlignment(int alignment) {
		jLabel.setHorizontalAlignment(alignment);
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		this.removeAll();
		if (isEditable) {
			this.add(jComboBoxMSeq, BorderLayout.CENTER);
		} else {
			jLabel.setText(this.getExternalValue().toString());
			this.add(jLabel, BorderLayout.CENTER);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}

	public Object getInternalValue() {
		return listMSeq.get(jComboBoxMSeq.getSelectedIndex());
	}

	public Object getExternalValue() {
		return (String)jComboBoxMSeq.getItemAt(jComboBoxMSeq.getSelectedIndex());
	}
	
	public void setValue(Object obj) {
		int value = 0;
		try {
			value = Integer.parseInt(obj.toString());
		} catch (NumberFormatException e) {
		}
		for (int i = 0; i < listMSeq.size(); i++) {
			if (listMSeq.get(i) == value) {
				jComboBoxMSeq.setSelectedIndex(i);
				break;
			}
		}
	}

	public void setBackground(Color color) {
		if (jComboBoxMSeq != null) {
			jComboBoxMSeq.setBackground(color);
		}
	}
	
	public void setColorOfError() {
		this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (this.isEditable()) {
			if (row%2==0) {
				this.setBackground(SystemColor.text);
			} else {
				this.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			this.setBackground(SystemColor.control);
		}
	}
}

class XF110_SubListCellEditorWithComboBox extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private String dataTypeOptions_ = "";
	private String tableAlias = "";
	private String fieldID = "";
	private String listType = "";
	private JComboBox jComboBox = new JComboBox();
	private JLabel jLabel = new JLabel();
	private boolean isEditable_ = true;
	private ArrayList<String> kubunKeyValueList = new ArrayList<String>();
	private ArrayList<XFHashMap> tableKeyValuesList = new ArrayList<XFHashMap>();
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private XF110_SubListDetailReferTable referTable_ = null;
	private XF110_SubList dialog_;
	private int indexOfColumn = -1;

	public XF110_SubListCellEditorWithComboBox(String dataSourceName, String dataTypeOptions, XF110_SubList dialog, XF110_SubListDetailReferTable referTable, boolean isNullable) {
		super();
		StringTokenizer workTokenizer;
		String wrk = "";
		String strWrk;

		dataTypeOptions_ = dataTypeOptions;
		workTokenizer = new StringTokenizer(dataSourceName, "." );
		tableAlias = workTokenizer.nextToken();
		referTable_ = referTable;
		fieldID =workTokenizer.nextToken();
		dialog_ = dialog;

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
			}
		} else {
			strWrk = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "KUBUN");
			if (!strWrk.equals("")) {
				listType = "KUBUN_LIST";
				if (isNullable) {
					kubunKeyValueList.add("");
					jComboBox.addItem("");
				}
				try {
					XFTableOperator operator = dialog_.createTableOperator("Select", dialog_.getSession().getTableNameOfUserVariants());
					operator.addKeyValue("IDUSERKUBUN", strWrk);
					operator.setOrderBy("SQLIST");
					while (operator.next()) {
						kubunKeyValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
						jComboBox.addItem(operator.getValueOf("TXUSERKUBUN").toString().trim());
					}
					if (jComboBox.getItemCount() == 0) {
						JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError24") + dataSourceName + XFUtility.RESOURCE.getString("FunctionError25"));
					}
				} catch (Exception e) {
					e.printStackTrace(dialog_.getExceptionStream());
					dialog_.setErrorAndCloseFunction();
				}
			} else {
				if (referTable_ != null) {
					listType = "RECORDS_LIST";
					keyFieldList = referTable_.getKeyFieldIDList();
				}
			}
		}

		indexOfColumn = dialog_.getDetailColumnList().size();
		jComboBox.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
		jComboBox.setFocusable(false);
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
		jComboBox.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent arg0) {
			}
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				if (referTable_ != null && listType.equals("RECORDS_LIST")) {
					XFHashMap keyValueMap = tableKeyValuesList.get(jComboBox.getSelectedIndex());
					for (int i = 0; i < keyValueMap.size(); i++) {
						dialog_.getCellsEditor().getActiveRowObject().getColumnValueMap().put(keyValueMap.getKeyIDByIndex(i), keyValueMap.getValueByIndex(i));
					}
					dialog_.getCellsEditor().getActiveRowObject().getColumnValueMap().put(tableAlias+"."+fieldID, jComboBox.getItemAt(jComboBox.getSelectedIndex()));
					XF110_SubListCellEditorWithComboBox comboBoxEditor;
					for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
						if (i > indexOfColumn) {
							if (dialog_.getDetailColumnList().get(i).getColumnEditor() instanceof XF110_SubListCellEditorWithComboBox) {
								comboBoxEditor = (XF110_SubListCellEditorWithComboBox)dialog_.getDetailColumnList().get(i).getColumnEditor();
								comboBoxEditor.setupRecordList();
							}
						}
					}
					//dialog_.getCellsEditor().updateRowObject();
				}
			}
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				setupRecordList();
			}
		});
		jLabel.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jLabel.setBackground(SystemColor.control);

//		this.addFocusListener(new java.awt.event.FocusAdapter() {
//			public void focusGained(FocusEvent e) {
//				dialog_.getCellsEditor().updateActiveColumnIndex();
//			}
//		});
		this.setLayout(new BorderLayout());
		this.add(jComboBox, BorderLayout.CENTER);
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		this.removeAll();
		if (isEditable) {
			this.add(jComboBox, BorderLayout.CENTER);
		} else {
			this.add(jLabel, BorderLayout.CENTER);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}
	
	public void setHorizontalAlignment(int alignment) {
		jLabel.setHorizontalAlignment(alignment);
	}

	public void setupRecordList() {
		if (referTable_ != null && listType.equals("RECORDS_LIST")) {
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));

				dialog_.getCellsEditor().getActiveRowObject().setValuesToDetailColumns();

				String selectedItemValue = "";
				if (jComboBox.getSelectedIndex() >= 0) {
					selectedItemValue = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
				}

				tableKeyValuesList.clear();
				jComboBox.removeAllItems();

				boolean blankItemRequired = false;
				XFHashMap blankKeyValues = new XFHashMap();
				for (int i = 0; i < referTable_.getWithKeyFieldIDList().size(); i++) {
					for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
						if (referTable_.getWithKeyFieldIDList().get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
							if (dialog_.getDetailColumnList().get(j).isNullable()) {
								blankItemRequired = true;
								if (dialog_.getDetailColumnList().get(j).isVisibleOnPanel()) {
									blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getDetailColumnList().get(j).getValue());
								} else {
									blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getDetailColumnList().get(j).getNullValue());
								}
							} else {
								blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getDetailColumnList().get(j).getValue());
							}
						}
					}
				}
				if (blankItemRequired) {
					tableKeyValuesList.add(blankKeyValues);
					jComboBox.addItem("");
				}

				String wrk;
				XFHashMap keyValues;
				XFTableOperator operator = dialog_.createTableOperator(referTable_.getSelectSQL(true));
				while (operator.next()) {
					keyValues = new XFHashMap();
					for (int i = 0; i < keyFieldList.size(); i++) {
						keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), operator.getValueOf(keyFieldList.get(i)));
					}
					wrk = operator.getValueOf(fieldID).toString().trim();
					keyValues.addValue(tableAlias + "." + fieldID, wrk);
					tableKeyValuesList.add(keyValues);
					jComboBox.addItem(wrk);
				}

				jComboBox.setSelectedItem(selectedItemValue);
				this.updateUI();

			} catch (Exception e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			} finally {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	public Object getInternalValue() {
		Object value = "";
		if (listType.equals("VALUES_LIST")) {
			value = jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
		}
		if (listType.equals("KUBUN_LIST")) {
			value = kubunKeyValueList.get(jComboBox.getSelectedIndex());
		}
		if (listType.equals("RECORDS_LIST")) {
			if (tableKeyValuesList.size() > 0) {
				value = tableKeyValuesList.get(jComboBox.getSelectedIndex());
			}
		}
		return value;
	}

	public Object getExternalValue() {
		return jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
	}
	
	public void setValue(Object obj) {
		String value = (String)obj;
		value = value.trim();
		if (jComboBox.getItemCount() > 0) {
			jComboBox.setSelectedIndex(0);
		}
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
			if (jComboBox.getItemCount() == 0) {
				setupRecordList();
			}
			if (value == null || value.equals("")) {
			} else {
				for (int i = 0; i < jComboBox.getItemCount(); i++) {
					if (jComboBox.getItemAt(i).toString().equals(value)) {
						jComboBox.setSelectedIndex(i);
						break;
					}
				}
			}
		}
		if (jComboBox.getSelectedIndex() >= 0) {
			jLabel.setText(this.getExternalValue().toString());
		}
	}
	
	public void setColorOfError() {
		jComboBox.setBackground(XFUtility.ERROR_COLOR);
		//this.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (row%2==0) {
			jComboBox.setBackground(SystemColor.text);
		} else {
			jComboBox.setBackground(XFUtility.ODD_ROW_COLOR);
		}
	}
}

class XF110_SubListCellEditorWithPromptCall extends JPanel implements XFTableColumnEditor {
	private static final long serialVersionUID = 1L;
	private JTextField jTextField;
	private JButton jButton = new JButton();
    private XF110_SubList dialog_;
    private String functionID_;
    private org.w3c.dom.Element fieldElement_;
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private String dataType = "";
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
    private ArrayList<XF110_SubListDetailReferTable> referTableList_;
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();

	public XF110_SubListCellEditorWithPromptCall(org.w3c.dom.Element fieldElement, String functionID, XF110_SubList dialog){
		super();
		fieldElement_ = fieldElement;
		functionID_ = functionID;
		dialog_ = dialog;

		jTextField = new JTextField();
		jTextField.setOpaque(true);
		jTextField.setBorder(null);
		jTextField.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));

		StringTokenizer workTokenizer = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		fieldID =workTokenizer.nextToken();
		tableID = tableAlias;
		referTableList_ = dialog_.getDetailReferTableList();
		for (int i = 0; i < referTableList_.size(); i++) {
			if (referTableList_.get(i).getTableAlias().equals(tableAlias)) {
				tableID = referTableList_.get(i).getTableID();
				break;
			}
		}
		if (!tableAlias.equals(dialog_.getDetailTable().getTableID())) {
			jTextField.setEditable(false);
		}

		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID + "." + fieldID + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);

		String fieldOptions = fieldElement_.getAttribute("FieldOptions");
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

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XF110.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value;
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));
					HashMap<String, Object> parmValueMap = new HashMap<String, Object>();
					HashMap<String, Object> columnValueMap = dialog_.getCellsEditor().getActiveRowObject().getColumnValueMap();
					for (int i = 0; i < fieldsToPutList_.size(); i++) {
						value = columnValueMap.get(fieldsToPutList_.get(i));
						if (value == null) {
							JOptionPane.showMessageDialog(null, "Unable to send the value of field " + fieldsToPutList_.get(i));
						} else {
							parmValueMap.put(fieldsToPutToList_.get(i), value);
						}
					}
					HashMap<String, Object> returnMap = dialog_.getSession().executeFunction(functionID_, parmValueMap);
					//if (!returnMap.get("RETURN_CODE").equals("99")) {
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
						String dataSourceName;
						for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
							dataSourceName = dialog_.getDetailColumnList().get(i).getDataSourceName();
							if (fieldsToGetMap.containsKey(dataSourceName) && columnValueMap.containsKey(dataSourceName)) {
								columnValueMap.put(dataSourceName, fieldsToGetMap.get(dataSourceName));
							}
						}
						for (int i = 0; i < dialog_.getCellsEditor().getCellList().size(); i++) {
							dataSourceName = dialog_.getDetailColumnList().get(i).getDataSourceName();
							if (fieldsToGetMap.containsKey(dataSourceName)) {
								dialog_.getCellsEditor().getCellList().get(i).setValue(fieldsToGetMap.get(dataSourceName));
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
				if (e.getKeyCode() == KeyEvent.VK_TAB) {
					e.setSource(dialog_.getJTableMain());
					dialog_.getJTableMain().dispatchEvent(e);
				}
			}
		});

		this.setLayout(new BorderLayout());
		this.setFocusable(true);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				dialog_.getCellsEditor().updateActiveColumnIndex();
				jButton.requestFocus();
			}
		});
		this.add(jTextField, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
	}
	
	public void setHorizontalAlignment(int alignment) {
	}
	
	public void setEditable(boolean isEditable) {
		jButton.setEnabled(isEditable);
	}
	
	public boolean isEditable() {
		return jButton.isEnabled();
	}

	public Object getInternalValue() {
		String text = "";
		String basicType = XFUtility.getBasicTypeOf(dataType);
		if (basicType.equals("INTEGER")
				|| basicType.equals("FLOAT")
				|| dataTypeOptionList.contains("DIAL")
				|| dataTypeOptionList.contains("ZIPNO")) {
			text = XFUtility.getStringNumber(jTextField.getText());
		} else {
			text = jTextField.getText();
		}
		return text;
	}

	public Object getExternalValue() {
		return jTextField.getText();
	}
	
	public void setValue(Object obj) {
		if (obj == null) {
			jTextField.setText("");
		} else {
			jTextField.setText(obj.toString());
		}
	}
	
	public void setColorOfError() {
		jTextField.setBackground(XFUtility.ERROR_COLOR);
	}
	
	public void setColorOfNormal(int row) {
		if (jButton.isEnabled()) {
			if (row%2==0) {
				jTextField.setBackground(SystemColor.text);
			} else {
				jTextField.setBackground(XFUtility.ODD_ROW_COLOR);
			}
		} else {
			jTextField.setBackground(SystemColor.control);
		}
	}
}
	
class XF110_SubListDetailRowNumber extends Object implements Comparable {
	private int number_;
	private HashMap<String, Object> keyValueMap_;
	private HashMap<String, Object> columnValueMapWithDSName_;
	private HashMap<String, Object> columnOldValueMapWithDSName_;
	private HashMap<String, Boolean> columnEditableMapWithDSName_;
	private HashMap<String, String[]> columnValueListMapWithDSName_;
	private ArrayList<Integer> errorCellIndexList = new ArrayList<Integer>();
	private XF110_SubList dialog_ = null;

	public XF110_SubListDetailRowNumber(int num, HashMap<String, Object> keyMap, HashMap<String, Object> columnValueMap, HashMap<String, Object> columnOldValueMap, HashMap<String, Boolean> columnEditableMap, HashMap<String, String[]> columnValueListMap, XF110_SubList dialog) {
		number_ = num;
		keyValueMap_ = keyMap;
		columnValueMapWithDSName_ = columnValueMap;
		columnOldValueMapWithDSName_ = columnOldValueMap;
		columnEditableMapWithDSName_ = columnEditableMap;
		columnValueListMapWithDSName_ = columnValueListMap;
		dialog_ = dialog;
	}

	public int compareTo(Object other) {
		XF110_SubListDetailRowNumber otherRowNumber = (XF110_SubListDetailRowNumber)other;
		int compareResult = 0;
		for (int i = 0; i < dialog_.getBatchWithKeyList().size(); i++) {
			compareResult = this.getColumnValueMap().get(dialog_.getBatchWithKeyList().get(i)).toString().compareTo(otherRowNumber.getColumnValueMap().get(dialog_.getBatchWithKeyList().get(i)).toString());
			if (compareResult != 0) {
				break;
			}
		}
		return compareResult;
	}
		
	public HashMap<String, Object> getKeyValueMap() {
		return keyValueMap_;
	}

	public HashMap<String, Object> getColumnValueMap() {
		return columnValueMapWithDSName_;
	}

	public HashMap<String, Object> getColumnValueMapWithFieldID() {
		HashMap<String, Object> valueMapWithFieldID = new HashMap<String, Object>();
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).getTableAlias().equals(dialog_.getPrimaryTableID())) {
				if (columnValueMapWithDSName_.containsKey(dialog_.getDetailColumnList().get(i).getDataSourceName())) {
					valueMapWithFieldID.put(dialog_.getDetailColumnList().get(i).getFieldID(), columnValueMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
				}
			}
		}
		return valueMapWithFieldID;
	}

	public HashMap<String, Object> getColumnOldValueMap() {
		return columnOldValueMapWithDSName_;
	}

	public HashMap<String, Object> getColumnOldValueMapWithFieldID() {
		HashMap<String, Object> oldValueMapWithFieldID = new HashMap<String, Object>();
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).getTableAlias().equals(dialog_.getPrimaryTableID())) {
				if (columnOldValueMapWithDSName_.containsKey(dialog_.getDetailColumnList().get(i).getDataSourceName())) {
					oldValueMapWithFieldID.put(dialog_.getDetailColumnList().get(i).getFieldID(), columnOldValueMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
				}
			}
		}
		return oldValueMapWithFieldID;
	}
	
	public HashMap<String, String[]> getColumnValueListMap() {
		return columnValueListMapWithDSName_;
	}
	
	public HashMap<String, String[]> getColumnValueListMapWithFieldID() {
		HashMap<String, String[]> valueListMapWithFieldID = new HashMap<String, String[]>();
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).getTableAlias().equals(dialog_.getDetailTable().getTableID())) {
				if (columnValueListMapWithDSName_.containsKey(dialog_.getDetailColumnList().get(i).getDataSourceName())) {
					valueListMapWithFieldID.put(dialog_.getDetailColumnList().get(i).getFieldID(), columnValueListMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
				}
			}
		}
		return valueListMapWithFieldID;
	}
	
	public void setValuesToDetailColumns() {
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			dialog_.getDetailColumnList().get(i).setValue(columnValueMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
			dialog_.getDetailColumnList().get(i).setOldValue(columnOldValueMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
			dialog_.getDetailColumnList().get(i).setEditable(columnEditableMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
			dialog_.getDetailColumnList().get(i).setValueList(columnValueListMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()));
		}
	}

	public int countErrors(ArrayList<String> messageList) {
		int countOfErrors = 0;

		////////////////////////
		// Reset Error Status //
		////////////////////////
		resetErrors();
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			dialog_.getDetailColumnList().get(i).setEditable(true);
			dialog_.getDetailColumnList().get(i).setError(false);
			if (!dialog_.getDetailColumnList().get(i).isFieldOnDetailTable()) {
				dialog_.getDetailColumnList().get(i).setValue(dialog_.getDetailColumnList().get(i).getNullValue());
			}
		}

		countOfErrors = dialog_.fetchDetailReferRecords("BU", true, "", columnValueMapWithDSName_, columnOldValueMapWithDSName_);

		////////////////////////////
		// Check Null-Constraints //
		////////////////////////////
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			columnValueMapWithDSName_.put(dialog_.getDetailColumnList().get(i).getDataSourceName(), dialog_.getDetailColumnList().get(i).getInternalValue());
			if (dialog_.getDetailColumnList().get(i).getTableAlias().equals(dialog_.getDetailTable().getTableID())
					&& dialog_.getDetailColumnList().get(i).isVisibleOnPanel()
					&& dialog_.getDetailColumnList().get(i).isEditable()) {
				if (dialog_.getDetailColumnList().get(i).isNullError(columnValueMapWithDSName_.get(dialog_.getDetailColumnList().get(i).getDataSourceName()))) {
					countOfErrors++;
				}
			}
			columnValueListMapWithDSName_.put(dialog_.getDetailColumnList().get(i).getDataSourceName(), dialog_.getDetailColumnList().get(i).getValueList());
		}

		/////////////////////////
		// Set Errors on Cells //
		/////////////////////////
		int rowNumber;
		String message = "";
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isError()) {
				if (dialog_.getDetailColumnList().get(i).isVisibleOnPanel() && dialog_.getDetailColumnList().get(i).isEditable()) {
					setErrorOnCellAt(i);
				}
				rowNumber = this.getRowIndex() + 1;
				if (dialog_.getDetailColumnList().get(i).isVisibleOnPanel()) {
					messageList.add(dialog_.getDetailColumnList().get(i).getCaption() + XFUtility.RESOURCE.getString("LineNumber1") + rowNumber + XFUtility.RESOURCE.getString("LineNumber2") + dialog_.getDetailColumnList().get(i).getError());
				} else {
					message = XFUtility.RESOURCE.getString("LineNumber1") + rowNumber + XFUtility.RESOURCE.getString("LineNumber2") + dialog_.getDetailColumnList().get(i).getError();
					if (!messageList.contains(message)) {
						messageList.add(message);
					}
				}
			}
		}

		return countOfErrors;
	}

	public void resetErrors() {
		errorCellIndexList.clear();
	}
	
	public void setErrorOnCellAt(int index) {
		errorCellIndexList.add(index);
	}
	
	public int getFirstErrorCellIndex() {
		if (errorCellIndexList.size() > 0) {
			return errorCellIndexList.get(0);
		} else {
			return -1;
		}
	}
	
	public ArrayList<Integer> getErrorCellIndexList() {
		return errorCellIndexList;
	}
	
	public String getRowNumberString() {
		return Integer.toString(number_);
	}

	public int getRowIndex() {
		int index = -1;
		XF110_SubListDetailRowNumber rowNumber = null;
		for (int i = 0; i < dialog_.getTableModel().getRowCount(); i++) {
			rowNumber = (XF110_SubListDetailRowNumber)dialog_.getTableModel().getValueAt(i, 0);
			if (rowNumber.equals(this)) {
				index = i;
			}
		}
		return index;
	}

	public void setNumber(int num) {
		number_ = num;
	}
}

class XF110_SubListDetailColumn extends XFColumnScriptable {
	private org.w3c.dom.Element functionColumnElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF110_SubList dialog_ = null;
	private String tableID_ = "";
	private String tableAlias_ = "";
	private String fieldID_ = "";
	private String fieldName = "";
	private String fieldRemarks = "";
	private String dataType = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private ArrayList<String> fieldOptionList;
	private String fieldOptions = "";
	private String fieldCaption = "";
	private String byteaTypeFieldID = "";
	private int fieldWidth = 50;
	private int columnIndex = -1;
	private boolean isKey = false;
	private boolean isNullable = true;
	private boolean isFieldOnDetailTable = false;
	private boolean isVisibleOnPanel = true;
	private boolean isVirtualField = false;
	private boolean isEnabled = true;
	private boolean isEditable = true;
	private boolean isNonEditableField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private String valueType = "STRING";
	private String flagTrue = "";
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private ArrayList<String> additionalHiddenFieldList = new ArrayList<String>();
	private Object value_ = null;
	private Object oldValue_ = null;
	private String[] valueList_ = null;
	private Color foreground = Color.black;
	private XFTableColumnEditor editor = null;
	private boolean isError = false;
	private String errorMessage = "";
	private int fieldRows = 1;
	private String fieldLayout = "HORIZONTAL";

	public XF110_SubListDetailColumn(org.w3c.dom.Element functionColumnElement, XF110_SubList dialog){
		super();
		String wrkStr;
		functionColumnElement_ = functionColumnElement;
		dialog_ = dialog;
		fieldOptions = functionColumnElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);

		StringTokenizer workTokenizer = new StringTokenizer(functionColumnElement_.getAttribute("DataSource"), "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog.getTableIDOfTableAlias(tableAlias_);
		fieldID_ =workTokenizer.nextToken();

		isVisibleOnPanel = true;
		if (tableID_.equals(dialog_.getDetailTable().getTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnDetailTable = true;
			ArrayList<String> keyNameList = dialog_.getDetailTable().getKeyFieldIDList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					isNonEditableField = true;
					break;
				}
			}
		} else {
			if (!fieldOptionList.contains("PROMPT_LIST")) {
				isNonEditableField = true;
			}
		}

		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		fieldName = workElement.getAttribute("Name");
		fieldRemarks = XFUtility.substringLinesWithTokenOfEOL(workElement.getAttribute("Remarks"), "<br>");
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (wrkStr.equals("")) {
			if (workElement.getAttribute("ColumnName").equals("")) {
				if (workElement.getAttribute("Name").equals("")) {
					fieldCaption = workElement.getAttribute("ID");
				} else {
					fieldCaption = fieldName;
				}
			} else {
				fieldCaption = workElement.getAttribute("ColumnName");
			}
		} else {
			fieldCaption = XFUtility.getCaptionValue(wrkStr, dialog_.getSession());
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			if (!fieldOptionList.contains("PROMPT_LIST")) {
				isNullable = false;
			}
		}
		if (workElement.getAttribute("NoUpdate").equals("T")) {
			isNonEditableField = true;
		}
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

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

		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}

		JLabel jLabel = new JLabel();
		FontMetrics metrics = jLabel.getFontMetrics(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		int captionWidth = metrics.stringWidth(fieldCaption) + 18;

		ArrayList<String> fieldOptionList = XFUtility.getOptionList(fieldOptions);
		if (fieldOptionList.contains("VERTICAL")) {
			fieldLayout = "VERTICAL";
		}

		String basicType = this.getBasicType();

		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";");
			if (workTokenizer.countTokens() >= 1) {
				flagTrue = workTokenizer.nextToken();
			}
			valueType = "FLAG";
			fieldWidth = 25;
			editor = new XF110_SubListCellEditorWithCheckBox(dataTypeOptions, dialog_);
		} else {
			if (fieldOptionList.contains("PROMPT_LIST")) {
				XF110_SubListDetailReferTable referTable = null;
				ArrayList<XF110_SubListDetailReferTable> referTableList = dialog_.getDetailReferTableList();
				for (int i = 0; i < referTableList.size(); i++) {
					if (referTableList.get(i).getTableID().equals(tableID_)) {
						if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias_)) {
							referTable = referTableList.get(i);
							break;
						}
					}
				}
				if (dataTypeOptionList.contains("KANJI")) {
					fieldWidth = dataSize * XFUtility.FONT_SIZE + 5;
				} else {
					fieldWidth = dataSize * (XFUtility.FONT_SIZE/2 + 2) + 15;
				}
				editor = new XF110_SubListCellEditorWithComboBox(functionColumnElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, referTable, isNullable);
			} else {
				wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
				if (!wrkStr.equals("")) {
					if (dataTypeOptionList.contains("KANJI")) {
						fieldWidth = dataSize * XFUtility.FONT_SIZE + 5;
					} else {
						fieldWidth = dataSize * (XFUtility.FONT_SIZE/2 + 2) + 15;
					}
					editor = new XF110_SubListCellEditorWithPromptCall(functionColumnElement_, wrkStr, dialog_);
					wrkStr = XFUtility.getOptionValueWithKeyword(wrkStr, "PROMPT_CALL_TO_GET_TO");
					if (!wrkStr.equals("")) {
						workTokenizer = new StringTokenizer(wrkStr, ";" );
						while (workTokenizer.hasMoreTokens()) {
							additionalHiddenFieldList.add(workTokenizer.nextToken());
						}
					}
				} else {
					wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
					if (!wrkStr.equals("")) {
						try {
							String wrk = "";
							String userVariantsTableID = dialog_.getSession().getTableNameOfUserVariants();
							String sql = "select * from " + userVariantsTableID + " where IDUSERKUBUN = '" + wrkStr + "' order by SQLIST";
							XFTableOperator operator = dialog_.getReferOperator(sql);
							while (operator.next()) {
								kubunValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
								wrk = operator.getValueOf("TXUSERKUBUN").toString().trim();
								if (metrics.stringWidth(wrk) + 20 > fieldWidth) {
									fieldWidth = metrics.stringWidth(wrk) + 20;
								}
								kubunTextList.add(wrk);
							}
							editor = new XF110_SubListCellEditorWithComboBox(functionColumnElement_.getAttribute("DataSource"), dataTypeOptions, dialog_, null, isNullable);
						} catch (Exception e) {
							e.printStackTrace(dialog_.getExceptionStream());
							dialog_.setErrorAndCloseFunction();
						}
					} else {
						if ((dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS"))
								&& !dataType.equals("VARCHAR") && !dataType.equals("LONG VARCHAR")) {
							fieldWidth = dataSize * XFUtility.FONT_SIZE + 5;
							editor = new XF110_SubListCellEditorWithTextField(this, dialog_);
						} else {
							if (dataTypeOptionList.contains("YMONTH")) {
								fieldWidth = 100;
								editor = new XF110_SubListCellEditorWithYMonthBox(dialog_);
							} else {
								if (dataTypeOptionList.contains("MSEQ")) {
									fieldWidth = 80;
									editor = new XF110_SubListCellEditorWithMSeqBox(dialog_);
								} else {
									if (dataTypeOptionList.contains("FYEAR")) {
										fieldWidth = 100;
										editor = new XF110_SubListCellEditorWithFYearBox(dialog_);
									} else {
										if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
											fieldWidth = XFUtility.getLengthOfEdittedNumericValue(dataSize, decimalSize, dataTypeOptionList) * (XFUtility.FONT_SIZE/2 + 2) + 15;
											editor = new XF110_SubListCellEditorWithTextField(this, dialog_);
										} else {
											if (basicType.equals("DATE")) {
												fieldWidth = XFUtility.getWidthOfDateValue(dialog_.getSession().getDateFormat(), dialog_.getSession().systemFont, XFUtility.FONT_SIZE);
												editor = new XF110_SubListCellEditorWithDateField(dialog_);
											} else {
												if (dataTypeOptionList.contains("IMAGE")) {
													valueType = "IMAGE";
													fieldWidth = 60;
													fieldRows = 2;
													editor = new XF110_SubListCellEditorWithImageField(dialog_);
												} else {
													if (dataType.equals("VARCHAR") || dataType.equals("LONG VARCHAR")) {
														fieldWidth = 100;
														editor = new XF110_SubListCellEditorWithLongTextEditor(fieldCaption, dataTypeOptionList, dialog_);
													} else {
														if (this.getBasicType().equals("BYTEA")) {
															valueType = "BYTEA";
															fieldWidth = 100;
															editor = new XF110_SubListCellEditorWithByteaColumn(byteaTypeFieldID, fieldOptions, dialog_);
														} else {
															fieldWidth = dataSize * (XFUtility.FONT_SIZE/2 + 2) + 15;
															editor = new XF110_SubListCellEditorWithTextField(this, dialog_);
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

		isEditable = !isNonEditableField;

		if (fieldWidth > 400) {
			fieldWidth = 400;
		}
		if (captionWidth > fieldWidth) {
			fieldWidth = captionWidth;
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "ROWS");
		if (!wrkStr.equals("")) {
			fieldRows = Integer.parseInt(wrkStr);
		}

		////////////////////////////////////////////////
		// No need to put this to script bindings     //
		// if the field is already put as Batch field //
		////////////////////////////////////////////////
		//if (!dialog_.getEngineScriptBindings().containsKey(this.getFieldIDInScript())) {
		//	dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), this);
		//}
	}

	public XF110_SubListDetailColumn(String tableID, String tableAlias, String fieldID, XF110_SubList dialog){
		super();
		functionColumnElement_ = null;
		dialog_ = dialog;
		fieldOptions = "";
		fieldOptionList = new ArrayList<String>();
		isVisibleOnPanel = false;

		tableID_ = tableID;
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}
		fieldID_ = fieldID;

		if (tableID_.equals(dialog_.getDetailTable().getTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnDetailTable = true;
			ArrayList<String> keyNameList = dialog_.getDetailTable().getKeyFieldIDList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					isNonEditableField = true;
					break;
				}
			}
		} else {
			isNonEditableField = true;
		}

		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
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
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}
		if (workElement.getAttribute("NoUpdate").equals("T")) {
			isNonEditableField = true;
		}
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

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

		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}

		isEditable = !isNonEditableField;

		////////////////////////////////////////////////
		// No need to put this to script bindings     //
		// if the field is already put as Batch field //
		////////////////////////////////////////////////
		//if (!dialog_.getEngineScriptBindings().containsKey(this.getFieldIDInScript())) {
		//	dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), this);
		//}
	}
	
	public boolean isNullError(Object object){
		String basicType = this.getBasicType();
		String strWrk;
		boolean isNullError = false;
		if (basicType.equals("INTEGER")) {
			if (!this.isNullable) {
				long value = Long.parseLong(object.toString());
				if (value == 0) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("FLOAT")) {
			if (!this.isNullable) {
				double value = Double.parseDouble(object.toString());
				if (value == 0) {
					isNullError = true;
				}
			}
		}
		if (basicType.equals("DATE")) {
			if ((object == null || object.equals("")) && !this.isNullable) {
				isNullError = true;
			}
		}
		if (basicType.equals("BYTEA")) {
			if (!this.isNullable) {
				if (object == null || ((XFByteArray)object).getInternalValue() == null) {
					isError = true;
				}
			}
		}
		if (basicType.equals("STRING")) {
			strWrk = "";
			if (object != null) {
				strWrk = object.toString();
			}
			if (strWrk.equals("") && !this.isNullable) {
				isNullError = true;
			}
			if (dataTypeOptionList.contains("YMONTH") && strWrk.length() > 0 && strWrk.length() < 6) {
				isNullError = true;
			}
			if (dataTypeOptionList.contains("FYEAR") && strWrk.length() > 0 && strWrk.length() < 4) {
				isNullError = true;
			}
		}
		if (isNullError) {
			this.setError(XFUtility.RESOURCE.getString("FunctionError16"));
		}
		return isNullError;
	}

	public void setError(boolean error) {
		if (error) {
			isError = true;
		} else {
			isError = false;
			this.errorMessage = "";
		}
	}

	public String getError(){
		return errorMessage;
	}

	public void setError(String message){
		if (!message.equals("") && this.errorMessage.equals("")) {
			setError(true);
			this.errorMessage = message;
		}
	}

	public boolean isError() {
		return isError;
	}

	public String getByteaTypeFieldID(){
		return byteaTypeFieldID;
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

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}
	
	public ArrayList<String> getTypeOptionList() {
		return dataTypeOptionList;
	}

	public String getTableID(){
		return tableID_;
	}

	public String getFieldID(){
		return fieldID_;
	}

	public String getFieldName(){
		return fieldName;
	}

	public String getFieldRemarks(){
		return fieldRemarks;
	}

	public String getFieldIDInScript(){
		return tableAlias_ + "_" + fieldID_;
	}

	public String getTableAlias(){
		return tableAlias_;
	}

	public String getDataSourceName(){
		return tableAlias_ + "." + fieldID_;
	}

	public boolean isKey(){
		return isKey;
	}

	public boolean isFieldOnDetailTable(){
		return isFieldOnDetailTable;
	}

	public Object getInternalValue(){
		return value_;
	}

	public void setOldValue(Object value){
		oldValue_ = XFUtility.getValueAccordingToBasicType(this.getBasicType(), value);
	}

	public Object getOldValue(){
		return oldValue_;
	}
	
	public boolean isValueChanged() {
		if (valueType.equals("BYTEA")) {
			return !((XFByteArray)value_).getInternalValue().equals(((XFByteArray)oldValue_).getInternalValue());
		} else {
			return !this.getValue().equals(this.getOldValue());
		}
	}

	public Object getExternalValue(){
		Object value = null;
		String basicType = this.getBasicType();
		if (basicType.equals("INTEGER") && !dataTypeOptionList.contains("MSEQ") && !dataTypeOptionList.contains("FYEAR")) {
			if (value_ == null || value_.toString().equals("")) {
				value = "";
			} else {
				value = XFUtility.getFormattedIntegerValue(value_.toString(), dataTypeOptionList, dataSize);
			}
		} else {
			if (basicType.equals("FLOAT")) {
				if (value_ == null || value_.toString().equals("")) {
					value = "";
				} else {
					value = XFUtility.getFormattedFloatValue(value_.toString(), decimalSize);
				}
			} else {
				if (basicType.equals("DATE")) {
					if (value_ == null || value_.equals("")) {
						value = "";
					} else {
						String wrkStr = value_.toString().substring(0, 10);
						value = XFUtility.getUserExpressionOfUtilDate(XFUtility.convertDateFromSqlToUtil(java.sql.Date.valueOf(wrkStr)), dialog_.getSession().getDateFormat(), false);
					}
				} else {
					if (basicType.equals("DATETIME")) {
						if (value_ != null) {
							value = value_.toString().replace("-", "/");
						}
					} else {
						if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN").equals("")) {
							if (value_ == null || value_.toString().trim().equals("")) {
								value = "";
							} else {
								String wrkStr = value_.toString().trim();
								for (int i = 0; i < kubunValueList.size(); i++) {
									if (kubunValueList.get(i).equals(wrkStr)) {
										value = kubunTextList.get(i);
										break;
									}
								}
							}
						} else {
							if (valueType.equals("STRING")) {
								if (value_ == null) {
									value = "";
								} else {
									value = value_.toString().trim();
									if (dataTypeOptionList.contains("YMONTH") || dataTypeOptionList.contains("FYEAR")) {
										String wrkStr = value.toString();
										if (!wrkStr.equals("")) {
											value = XFUtility.getUserExpressionOfYearMonth(wrkStr, dialog_.getSession().getDateFormat());
										}
									}
									if (dataTypeOptionList.contains("MSEQ")) {
										value = XFUtility.getUserExpressionOfMSeq(Integer.parseInt(value.toString()), dialog_.getSession());
									}
								}
							}
							if (valueType.equals("IMAGE")) {
								String fileName = dialog_.getSession().getImageFileFolder() + value_.toString().trim();
								int iconHeight = fieldRows * XFUtility.ROW_UNIT_HEIGHT_EDITABLE;
								value = XFUtility.createSmallIcon(fileName, fieldWidth, iconHeight);
							}
							if (valueType.equals("FLAG")) {
								if (value_.toString().trim().equals(flagTrue)) {
									value = XFUtility.ICON_CHECK_1D;
								} else {
									value = XFUtility.ICON_CHECK_0D;
								}
							}
							if (valueType.equals("BYTEA")) {
								value = ((XFByteArray)value_).getExternalValue();
							}
						}
					}
				}
			}
		}
		return value;
	}
	
	public String getValueType() {
		return valueType;
	}

	public boolean isNull(){
		return XFUtility.isNullValue(this.getBasicType(), value_);
	}

	public boolean isNullable() {
		return isNullable;
	}

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public String getDataTypeOptions() {
		return dataTypeOptions;
	}

	public String getFieldOptions() {
		return fieldOptions;
	}

	public XFTableColumnEditor getColumnEditor(){
		return editor;
	}

	public String getCaption(){
		return fieldCaption;
	}

	public int getDataSize(){
		return dataSize;
	}

	public int getDecimalSize(){
		return decimalSize;
	}

	public int getWidth(){
		return fieldWidth;
	}

	public void setWidth(int width){
		fieldWidth = width;
	}

	public int getRows(){
		return fieldRows;
	}

	public String getLayout(){
		return fieldLayout;
	}

	public int getColumnIndex(){
		return columnIndex;
	}

	public void setColumnIndex(int index){
		columnIndex = index;
	}

	public void setValueOfResultSet(XFTableOperator operator) {
		String basicType = this.getBasicType();
		this.setColor("");

		try {
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					value_ = XFUtility.calculateExpireValue(this.getTableElement(), operator, dialog_.getSession(), dialog_.getProcessLog());
				}
			} else {
				Object value = operator.getValueOf(this.getFieldID()); 
				if (basicType.equals("BYTEA")) {
					value_ = new XFByteArray(value);
				} else {
					if (basicType.equals("INTEGER")) {
						if (value == null || value.equals("")) {
							value_ = "";
						} else {
							String wrkStr = value.toString();
							int pos = wrkStr.indexOf(".");
							if (pos >= 0) {
								wrkStr = wrkStr.substring(0, pos);
							}
							value_ = Long.parseLong(wrkStr);
						}
					} else {
						if (basicType.equals("FLOAT")) {
							if (value == null || value.equals("")) {
								value_ = "";
							} else {
								value_ = Double.parseDouble(value.toString());
							}
						} else {
							if (value == null) {
								value_ = "";
							} else {
								value_ = value.toString().trim();
							}
						}
					}
				}
			}

			oldValue_ = value_;

		} catch (Exception e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public void initValue() {
		value_ = this.getNullValue();
		valueList_ = null;
		if (!isNonEditableField) {
			isEditable = true;
		}
	}
	
	public void setupByteaTypeField(ArrayList<XF110_SubListDetailColumn> columnList) {
		if (!byteaTypeFieldID.equals("")) {
			for (int i = 0; i < columnList.size(); i++) {
				if (columnList.get(i).getFieldID().equals(byteaTypeFieldID)) {
					((XFByteArray)value_).setTypeColumn((XFColumnScriptable)columnList.get(i));
					break;
				}
			}
		}
	}
	
	public void setValueList(String[] valueList) {
		valueList_ = valueList;
	}
	
	public String[] getValueList() {
		return valueList_;
	}

	public void setValue(Object value){
		if (this.getBasicType().equals("BYTEA")) {
			value_ = value;
		} else {
			value_ = XFUtility.getValueAccordingToBasicType(this.getBasicType(), value);
		}
	}

	public Object getValue() {
		return getInternalValue();
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public boolean isNonEditableField() {
		return isNonEditableField;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	public void setColor(String color) {
		foreground = XFUtility.convertStringToColor(color);
	}

	public String getColor() {
		return XFUtility.convertColorToString(foreground);
	}

	public Color getForeground() {
		return foreground;
	}
}

class XF110_SubListBatchTable extends Object {
	private org.w3c.dom.Element tableElement = null;
	private String tableID = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private ArrayList<XFHashMap> keyMapList = new ArrayList<XFHashMap>();
	private ArrayList<String> uniqueKeyList = new ArrayList<String>();
	private XF110_SubList dialog_;
	private StringTokenizer workTokenizer;
	private String dbName = "";

	public XF110_SubListBatchTable(org.w3c.dom.Element functionElement, XF110_SubList dialog){
		super();
		dialog_ = dialog;
		tableID = functionElement.getAttribute("BatchTable");
		tableElement = dialog_.getSession().getTableElement(tableID);

		String dbID = tableElement.getAttribute("DB");
		if (dbID.equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(dbID);
		}

		String workString;
		org.w3c.dom.Element workElement;
		NodeList nodeList = tableElement.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("PK")) {
				workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
				while (workTokenizer.hasMoreTokens()) {
					workString = workTokenizer.nextToken();
					keyFieldIDList.add(workString);
				}
			}
			if (workElement.getAttribute("Type").equals("SK")) {
				uniqueKeyList.add(workElement.getAttribute("Fields"));
			}
		}

		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID, element, dialog_.getSession().getTableNodeList()));
		}
	}
	
	public String getSQLToInsert(XF110_SubListDetailRowNumber rowNumber){
		StringBuffer statementBuf = new StringBuffer();
		int index;

		//////////////////////////
		// Insert-Table section //
		//////////////////////////
		statementBuf.append("insert into ");
		statementBuf.append(tableID);
		statementBuf.append(" (");

		//////////////////////////////////////////////
		// Setting values to each batch table field //
		//////////////////////////////////////////////
		for (int i = 0; i < dialog_.getBatchFieldList().size(); i++) {
			if (dialog_.getBatchFieldList().get(i).isFieldOnBatchTable()) {
				if (dialog_.getBatchFieldList().get(i).isAutoNumberField()) {
					dialog_.getBatchFieldList().get(i).setValue(dialog_.getBatchFieldList().get(i).getAutoNumber());
				} else {
					index = dialog_.getBatchKeyList().indexOf(dialog_.getBatchFieldList().get(i).getFieldID());
					if (index > -1) {
						dialog_.getBatchFieldList().get(i).setValue(rowNumber.getColumnValueMap().get(dialog_.getBatchWithKeyList().get(index)));
					}
				}
			}
		}

		///////////////////////////////
		// Fields and Values section //
		///////////////////////////////
		boolean firstField = true;
		for (int i = 0; i < dialog_.getBatchFieldList().size(); i++) {
			if (dialog_.getBatchFieldList().get(i).isFieldOnBatchTable()
			&& !dialog_.getBatchFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ") ;
				}
				statementBuf.append(dialog_.getBatchFieldList().get(i).getFieldID()) ;
				firstField = false;
			}
		}
		statementBuf.append(") values(") ;
		XFHashMap keyMap = new XFHashMap();
		firstField = true;
		for (int i = 0; i < dialog_.getBatchFieldList().size(); i++) {
			if (dialog_.getBatchFieldList().get(i).isFieldOnBatchTable()
					&& !dialog_.getBatchFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ") ;
				}
				statementBuf.append(XFUtility.getTableOperationValue(dialog_.getBatchFieldList().get(i).getBasicType(), dialog_.getBatchFieldList().get(i).getInternalValue(), dbName)) ;
				if (dialog_.getBatchFieldList().get(i).isKey()) {
					keyMap.addValue(dialog_.getBatchFieldList().get(i).getFieldID(), dialog_.getBatchFieldList().get(i).getInternalValue());
				}
				firstField = false;
			}
		}
		keyMapList.add(keyMap);
		statementBuf.append(")") ;
		
		return statementBuf.toString();
	}

	public ArrayList<String> getUniqueKeyList() {
		return uniqueKeyList;
	}
	
	public String getSQLToCheckSKDuplication(ArrayList<String> keyFieldList) {
		StringBuffer statementBuf = new StringBuffer();
		
		///////////////////////////
		// Select-Fields section //
		///////////////////////////
		statementBuf.append("select ");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getBatchFieldList().size(); i++) {
			if (dialog_.getBatchFieldList().get(i).isFieldOnBatchTable()
					&& !dialog_.getBatchFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(",");
				}
				statementBuf.append(dialog_.getBatchFieldList().get(i).getFieldID());
				firstField = false;
			}
		}

		////////////////////////////
		// From and Where section //
		////////////////////////////
		statementBuf.append(" from ");
		statementBuf.append(tableID);
		statementBuf.append(" where ") ;
		firstField = true;
		for (int j = 0; j < dialog_.getBatchFieldList().size(); j++) {
			if (dialog_.getBatchFieldList().get(j).isFieldOnBatchTable()) {
				for (int p = 0; p < keyFieldList.size(); p++) {
					if (dialog_.getBatchFieldList().get(j).getFieldID().equals(keyFieldList.get(p))) {
						if (!firstField) {
							statementBuf.append(" and ") ;
						}
						statementBuf.append(dialog_.getBatchFieldList().get(j).getFieldID()) ;
						statementBuf.append("=") ;
						statementBuf.append(XFUtility.getTableOperationValue(dialog_.getBatchFieldList().get(j).getBasicType(), dialog_.getBatchFieldList().get(j).getInternalValue(), dbName)) ;
						firstField = false;
					}
				}
			}
		}

		return statementBuf.toString();
	}

	public String getTableID(){
		return tableID;
	}

	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return keyFieldIDList;
	}
	
	public ArrayList<XFHashMap> getKeyMapList(){
		return keyMapList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF110_SubListBatchReferTable referTable1;
		XF110_SubListDetailReferTable referTable2;
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
			for (int i = 0; i < dialog_.getBatchReferTableList().size(); i++) {
				referTable1 = dialog_.getBatchReferTableList().get(i);
				if (referTable1.getTableID().equals(tableID) && referTable1.getTableAlias().equals(tableAlias)) {
					for (int j = 0; j < referTable1.getFieldIDList().size(); j++) {
						if (referTable1.getFieldIDList().get(j).equals(fieldID)) {
							isValid = true;
							break;
						}
					}
				}
				if (isValid) {
					break;
				}
			}
			if (!isValid) {
				if (dialog_.getDetailTable().getTableID().equals(tableID) && dialog_.getDetailTable().getTableID().equals(tableAlias)) {
					NodeList nodeList = dialog_.getDetailTable().getTableElement().getElementsByTagName("Field");
					for (int i = 0; i < nodeList.getLength(); i++) {
						workElement = (org.w3c.dom.Element)nodeList.item(i);
						if (workElement.getAttribute("ID").equals(fieldID)) {
							isValid = true;
							break;
						}
					}
				} else {
					for (int i = 0; i < dialog_.getDetailReferTableList().size(); i++) {
						referTable2 = dialog_.getDetailReferTableList().get(i);
						if (referTable2.getTableID().equals(tableID) && referTable2.getTableAlias().equals(tableAlias)) {
							for (int j = 0; j < referTable2.getFieldIDList().size(); j++) {
								if (referTable2.getFieldIDList().get(j).equals(fieldID)) {
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
			for (int i = 0; i < dialog_.getBatchFieldList().size(); i++) {
				if (dialog_.getBatchFieldList().get(i).isError()) {
					countOfErrors++;
				}
			}
		}
		return countOfErrors;
	}
}

class XF110_SubListBatchReferTable extends Object {
	//private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF110_SubList dialog_ = null;
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
	private String dbName = "";

	public XF110_SubListBatchReferTable(org.w3c.dom.Element referElement, XF110_SubList dialog){
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

		activeWhere = tableElement.getAttribute("ActiveWhere");

		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}

		workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			fieldIDList.add(workTokenizer.nextToken());
		}

		if (referElement_.getAttribute("ToKeyFields").equals("")) {
			org.w3c.dom.Element workElement = dialog.getSession().getTablePKElement(tableID);
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

		workTokenizer = new StringTokenizer(referElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldIDList.add(workTokenizer.nextToken());
		}

		if (referElement_.getAttribute("Optional").equals("T")) {
			isOptional = true;
		}
	}

	public String getSelectSQL(boolean isToGetRecordsForComboBox){
		int count;
		StringBuffer buf = new StringBuffer();

		///////////////////////////
		// Select-Fields section //
		///////////////////////////
		buf.append("select ");
		org.w3c.dom.Element workElement;
		count = 0;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			workElement = dialog_.getSession().getFieldElement(tableID, toKeyFieldIDList.get(i));
			if (count > 0) {
				buf.append(",");
			}
			count++;
			buf.append(toKeyFieldIDList.get(i));
		}
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
		if (!rangeKeyFieldValid.equals("")) {
			if (count > 0) {
				buf.append(",");
			}
			buf.append(rangeKeyFieldValid);
			workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				buf.append(",");
				buf.append(rangeKeyFieldExpire);
			}
		}
		
		////////////////////////////
		// From and Where section //
		////////////////////////////
		buf.append(" from ");
		buf.append(tableID);
		StringTokenizer workTokenizer;
		String keyFieldID, keyFieldTableID;
		count = 0;
		boolean isToBeWithValue;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				rangeKeyFieldSearch = withKeyFieldIDList.get(i);
			} else {
				if (isToGetRecordsForComboBox) {
					/////////////////////////////////////////////////////////////////
					// Value of the field which has either of these conditions     //
					// should be within WHERE to SELECT records:                   //
					// 1. The with-key-field is not edit-able                       //
					// 2. The with-key-field is part of PK of the batch table      //
					// 3. The with-key-field is on the batch table and consists    //
					//    of upper part of with-key-fields                         //
					// 4. The with-key-field is part of PK of the other join table //
					/////////////////////////////////////////////////////////////////
					for (int j = 0; j < dialog_.getBatchFieldList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getBatchFieldList().get(j).getDataSourceName())) {
							isToBeWithValue = false;
							if (dialog_.getBatchFieldList().get(j).isVisibleOnPanel() && !dialog_.getBatchFieldList().get(j).isEditable()) {
								isToBeWithValue = true;
							} else {
								workTokenizer = new StringTokenizer(withKeyFieldIDList.get(i), "." );
								keyFieldTableID = workTokenizer.nextToken();
								keyFieldID = workTokenizer.nextToken();
								if (keyFieldTableID.equals(dialog_.getBatchTable().getTableID())) {
									if (withKeyFieldIDList.size() > 1 && i < (withKeyFieldIDList.size() - 1)) {
										isToBeWithValue = true;
									} else {
										for (int k = 0; k < dialog_.getBatchTableKeyFieldList().size(); k++) {
											if (keyFieldID.equals(dialog_.getBatchTableKeyFieldList().get(k))) {
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
								buf.append(XFUtility.getTableOperationValue(dialog_.getBatchFieldList().get(j).getBasicType(), dialog_.getBatchFieldList().get(j).getInternalValue(), dbName)) ;
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
					for (int j = 0; j < dialog_.getBatchFieldList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getBatchFieldList().get(j).getTableAlias() + "." + dialog_.getBatchFieldList().get(j).getFieldID())) {
//							if (XFUtility.isLiteralRequiredBasicType(dialog_.getBatchFieldList().get(j).getBasicType())) {
//								buf.append("'") ;
//								buf.append(dialog_.getBatchFieldList().get(j).getInternalValue());
//								buf.append("'") ;
//							} else {
//								buf.append(dialog_.getBatchFieldList().get(j).getInternalValue());
//							}
							buf.append(XFUtility.getTableOperationValue(dialog_.getBatchFieldList().get(j).getBasicType(), dialog_.getBatchFieldList().get(j).getInternalValue(), dbName)) ;
							break;
						}
					}
					count++;
				}
			}
		}
		if (!activeWhere.equals("")) {
			buf.append(" and ");
			buf.append(activeWhere);
		}
		
		//////////////////////
		// Order-by section //
		//////////////////////
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
		rangeValidated = false;

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

	public ArrayList<String> getKeyFieldIDList(){
		return toKeyFieldIDList;
	}
	
	public ArrayList<String> getWithKeyFieldIDList(){
		return withKeyFieldIDList;
	}

	public ArrayList<String> getFieldIDList(){
		return  fieldIDList;
	}
	
	public void setToBeExecuted(boolean executed){
		isToBeExecuted = executed;
	}
	
	public boolean isToBeExecuted(){
		return isToBeExecuted;
	}

	public boolean isKeyNullable() {
		boolean isKeyNullable = false;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getBatchFieldList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getBatchFieldList().get(j).getTableAlias() + "." + dialog_.getBatchFieldList().get(j).getFieldID())) {
					if (dialog_.getBatchFieldList().get(j).isNullable()) {
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
			for (int j = 0; j < dialog_.getBatchFieldList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getBatchFieldList().get(j).getTableAlias() + "." + dialog_.getBatchFieldList().get(j).getFieldID())) {
					if (dialog_.getBatchFieldList().get(j).isNull()) {
						isKeyNull = true;
						break;
					}
				}
			}
		}
		return isKeyNull;
	}

	public boolean isRecordToBeSelected(XFTableOperator operator) throws Exception {
		boolean returnValue = false;

		if (rangeKeyType == 0) {
			returnValue = true;
		}

		////////////////////
		// VIRTUAL SEARCH //
		////////////////////
		if (rangeKeyType == 1) {
			if (!rangeValidated) { 
				// Note that result set is ordered by rangeKeyFieldValue DESC //
				Object valueKey = dialog_.getValueOfBatchFieldByName(rangeKeyFieldSearch);
				Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
					rangeValidated = true;
				}
			}
		}

		/////////////////////
		// PHYSICAL SEARCH //
		/////////////////////
		if (rangeKeyType == 2) {
			Object valueKey = dialog_.getValueOfBatchFieldByName(rangeKeyFieldSearch);
			Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
			Object valueThru = operator.getValueOf(rangeKeyFieldExpire);
			if (valueThru == null) {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
				}
			} else {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				int comp2 = valueKey.toString().compareTo(valueThru.toString());
				if (comp1 >= 0 && comp2 < 0) {
					returnValue = true;
				}
			}
		}

		return returnValue;
	}

	public void setErrorOnRelatedFields() {
		boolean noneOfKeyFieldsWereSetError = true;

		//////////////////////////////////////////////////
		// Set error on the visible edit-able key field //
		//////////////////////////////////////////////////
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getBatchFieldList().size(); j++) {
				if (dialog_.getBatchFieldList().get(j).isVisibleOnPanel()
						&& dialog_.getBatchFieldList().get(j).isEditable()
						&& dialog_.getBatchFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
						&& !dialog_.getBatchFieldList().get(j).isError()) {
					dialog_.getBatchFieldList().get(j).setError(XFUtility.RESOURCE.getString("FunctionError53") + tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
					noneOfKeyFieldsWereSetError = false;
					break;
				}
			}
		}

		if (noneOfKeyFieldsWereSetError) {
			////////////////////////////////////////////////////////
			// Set error on the visible edit-able attribute field //
			////////////////////////////////////////////////////////
			for (int i = 0; i < fieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getBatchFieldList().size(); j++) {
					if (dialog_.getBatchFieldList().get(j).isVisibleOnPanel()
							&& dialog_.getBatchFieldList().get(j).isEditable()
							&& dialog_.getBatchFieldList().get(j).getFieldID().equals(fieldIDList.get(i))
							&& dialog_.getBatchFieldList().get(j).getTableAlias().equals(this.tableAlias)
							&& !dialog_.getBatchFieldList().get(j).isError()) {
						dialog_.getBatchFieldList().get(j).setError(XFUtility.RESOURCE.getString("FunctionError53") + tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
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
				for (int j = 0; j < dialog_.getBatchFieldList().size(); j++) {
					if (dialog_.getBatchFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
							&& !dialog_.getBatchFieldList().get(j).isError()) {
						dialog_.getBatchFieldList().get(j).setError(XFUtility.RESOURCE.getString("FunctionError53") + tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
						break;
					}
				}
			}
		}
	}

	public void setKeyFieldValues(XFHashMap keyValues){
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getBatchFieldList().size(); j++) {
				if (dialog_.getBatchFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))) {
					dialog_.getBatchFieldList().get(j).setValue(keyValues.getValue(withKeyFieldIDList.get(i)));
					break;
				}
			}
		}
	}
}

class XF110_SubListDetailTable extends Object {
	//private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID_ = "";
	private String activeWhere = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private ArrayList<String> batchTableWithKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> batchTableToKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> uniqueKeyList = new ArrayList<String>();
	private XF110_SubList dialog_;
	private StringTokenizer workTokenizer1, workTokenizer2;
	private String updateCounterID = "";
	private String dbName = "";

	public XF110_SubListDetailTable(org.w3c.dom.Element functionElement, XF110_SubList dialog){
		super();
		functionElement_ = functionElement;
		dialog_ = dialog;

		tableID_ = functionElement_.getAttribute("PrimaryTable");
		tableElement = dialog_.getSession().getTableElement(tableID_);

		String dbID = tableElement.getAttribute("DB");
		if (dbID.equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(dbID);
		}

		activeWhere = tableElement.getAttribute("ActiveWhere");
		updateCounterID = tableElement.getAttribute("UpdateCounter");
		if (updateCounterID.equals("")) {
			updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
		} else {
			if (updateCounterID.toUpperCase().equals("*NONE")) {
				updateCounterID = "";
			}
		}

		int pos1;
		String wrkStr, wrkTableID, wrkFieldID;
		org.w3c.dom.Element workElement;

		if (functionElement_.getAttribute("KeyFields").equals("")) {
			NodeList nodeList = tableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer1 = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer1.hasMoreTokens()) {
						wrkStr = workTokenizer1.nextToken();
						keyFieldIDList.add(wrkStr);
					}
					break;
				}
			}
		} else {
			workTokenizer1 = new StringTokenizer(functionElement_.getAttribute("KeyFields"), ";" );
			while (workTokenizer1.hasMoreTokens()) {
				keyFieldIDList.add(workTokenizer1.nextToken());
			}
		}

		NodeList nodeList = tableElement.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("SK")) {
				uniqueKeyList.add(workElement.getAttribute("Fields"));
			}
		}

		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID_, element, dialog_.getSession().getTableNodeList()));
		}

		if (dialog_.getBatchTable() != null) {
			workList = tableElement.getElementsByTagName("Refer");
			for (int i = 0; i < workList.getLength(); i++) {
				element = (org.w3c.dom.Element)workList.item(i);
				if (element.getAttribute("ToTable").equals(dialog_.getBatchTable().getTableID())) {
					workTokenizer1 = new StringTokenizer(element.getAttribute("WithKeyFields"), ";" );
					workTokenizer2 = new StringTokenizer(element.getAttribute("ToKeyFields"), ";" );
					while (workTokenizer1.hasMoreTokens()) {
						wrkStr = workTokenizer1.nextToken();
						pos1 = wrkStr.indexOf(".");
						if (pos1 > -1) { 
							wrkTableID = wrkStr.substring(0, pos1);
							wrkFieldID = wrkStr.substring(pos1+1, wrkStr.length());
							if (wrkTableID.equals(tableID_)) {
								batchTableWithKeyFieldIDList.add(wrkFieldID);
								batchTableToKeyFieldIDList.add(workTokenizer2.nextToken());
							}
						}
					}
				}
			}
		}
	}

	public String getSQLToSelect(XF110_RowNumber rowNumber){
		int count;
		StringBuffer buf = new StringBuffer();

		///////////////////////////
		// Select-Fields section //
		///////////////////////////
		buf.append("select ");
		count = -1;
		for (int i = 0; i < keyFieldIDList.size(); i++) {
			count++;
			if (count > 0) {
				buf.append(",");
			}
			buf.append(keyFieldIDList.get(i));
		}
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).getTableID().equals(tableID_)
					&& !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				count++;
				if (count > 0) {
					buf.append(",");
				}
				buf.append(dialog_.getDetailColumnList().get(i).getFieldID());
			}
		}
		if (!updateCounterID.equals("")) {
			if (count > 0) {
				buf.append(",");
			}
			buf.append(updateCounterID);
		}

		////////////////////////////
		// From and Where section //
		////////////////////////////
		buf.append(" from ");
		buf.append(tableID_);
		buf.append(" where ") ;
		boolean firstKey = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			for (int j = 0; j < keyFieldIDList.size(); j++) {
				if (dialog_.getDetailColumnList().get(i).getTableID().equals(tableID_) && dialog_.getDetailColumnList().get(i).getFieldID().equals(keyFieldIDList.get(j))) {
					if (!firstKey) {
						buf.append(" and ") ;
					}
					buf.append(keyFieldIDList.get(j)) ;
					buf.append("=") ;
					buf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(i).getBasicType(), rowNumber.getKeyMap().get(keyFieldIDList.get(j)), dbName));
					firstKey = false;
				}
			}
		}
		if (!updateCounterID.equals("")) {
			if (count > 0) {
				buf.append(" and ");
			}
			buf.append(updateCounterID);
			buf.append("=");
			buf.append(rowNumber.getUpdateCounter());
		}
		if (!activeWhere.equals("")) {
			buf.append(" and ");
			buf.append(activeWhere);
		}

		return buf.toString();
	}

	String getSQLToUpdate(XF110_SubListDetailRowNumber rowNumber) {
		StringBuffer statementBuf = new StringBuffer();

		////////////////////////
		// Update-Set section //
		////////////////////////
		statementBuf.append("update ");
		statementBuf.append(tableID_);
		statementBuf.append(" set ");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isFieldOnDetailTable()
			&& !dialog_.getDetailColumnList().get(i).isKey()
			&& !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				if (!batchTableWithKeyFieldIDList.contains(dialog_.getDetailColumnList().get(i).getFieldID())) {
					if (!firstField) {
						statementBuf.append(", ");
					}
					statementBuf.append(dialog_.getDetailColumnList().get(i).getFieldID());
					statementBuf.append("=");
					statementBuf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(i).getBasicType(), rowNumber.getColumnValueMap().get(dialog_.getDetailColumnList().get(i).getDataSourceName()), dbName));
					firstField = false;
				}
			}
		}
		for (int i = 0; i < batchTableWithKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getBatchFieldList().size(); j++) {
				if (dialog_.getBatchFieldList().get(j).getFieldID().equals(batchTableToKeyFieldIDList.get(i))
					&& dialog_.getBatchFieldList().get(j).isFieldOnBatchTable()) {
					if (!firstField) {
						statementBuf.append(", ");
					}
					statementBuf.append(batchTableWithKeyFieldIDList.get(i));
					statementBuf.append("=");
					statementBuf.append(XFUtility.getTableOperationValue(dialog_.getBatchFieldList().get(j).getBasicType(), dialog_.getBatchFieldList().get(j).getInternalValue(), dbName));
					firstField = false;
					for (int k = 0; k < dialog_.getDetailColumnList().size(); k++) {
						if (dialog_.getDetailColumnList().get(k).getFieldID().equals(batchTableToKeyFieldIDList.get(i))) {
							dialog_.getDetailColumnList().get(k).setValue(dialog_.getBatchFieldList().get(j).getInternalValue());
							rowNumber.getColumnValueMap().put(dialog_.getDetailColumnList().get(k).getDataSourceName(), dialog_.getBatchFieldList().get(j).getInternalValue());
							break;
						}
					}
					break;
				}
			}
		}
		if (!updateCounterID.equals("")) {
			if (!firstField) {
				statementBuf.append(", ");
			}
			statementBuf.append(updateCounterID);
			statementBuf.append("=");
			statementBuf.append((Long)rowNumber.getColumnValueMap().get(updateCounterID) + 1);
		}
		
		///////////////////
		// Where section //
		///////////////////
		statementBuf.append(" where ") ;
		boolean firstKey = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			for (int j = 0; j < keyFieldIDList.size(); j++) {
				if (dialog_.getDetailColumnList().get(i).getTableID().equals(tableID_) && 
						dialog_.getDetailColumnList().get(i).getFieldID().equals(keyFieldIDList.get(j))) {
					if (!firstKey) {
						statementBuf.append(" and ") ;
					}
					statementBuf.append(keyFieldIDList.get(j)) ;
					statementBuf.append("=") ;
					statementBuf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(i).getBasicType(), rowNumber.getKeyValueMap().get(keyFieldIDList.get(j)), dbName));
					firstKey = false;
				}
			}
		}
		if (!updateCounterID.equals("")) {
			statementBuf.append(" and ") ;
			statementBuf.append(updateCounterID) ;
			statementBuf.append("=") ;
			statementBuf.append(rowNumber.getColumnValueMap().get(updateCounterID));
		}

		return statementBuf.toString();
	}

	public ArrayList<String> getUniqueKeyList() {
		return uniqueKeyList;
	}
	
	public String getSQLToCheckSKDuplication(XF110_SubListDetailRowNumber rowNumber, ArrayList<String> keyFieldList) {
		StringBuffer statementBuf = new StringBuffer();

		///////////////////////////
		// Select-Fields section //
		///////////////////////////
		statementBuf.append("select ");
		boolean firstField = true;
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).isFieldOnDetailTable()
					&& !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(",");
				}
				statementBuf.append(dialog_.getDetailColumnList().get(i).getFieldID());
				firstField = false;
			}
		}

		////////////////////////////
		// From and Where section //
		////////////////////////////
		statementBuf.append(" from ");
		statementBuf.append(tableID_);
		statementBuf.append(" where ") ;
		firstField = true;
		for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
			if (dialog_.getDetailColumnList().get(j).isFieldOnDetailTable()) {
				for (int p = 0; p < keyFieldList.size(); p++) {
					if (dialog_.getDetailColumnList().get(j).getFieldID().equals(keyFieldList.get(p))) {
						if (!firstField) {
							statementBuf.append(" and ") ;
						}
						statementBuf.append(dialog_.getDetailColumnList().get(j).getFieldID()) ;
						statementBuf.append("=") ;
						statementBuf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(j).getBasicType(), rowNumber.getColumnValueMap().get(dialog_.getDetailColumnList().get(j).getDataSourceName()), dbName));
						firstField = false;
					}
				}
			}
		}
		firstField = true;
		for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
			if (dialog_.getDetailColumnList().get(j).isFieldOnDetailTable()) {
				if (dialog_.getDetailColumnList().get(j).isKey()) {
					if (firstField) {
						statementBuf.append(" and (") ;
					} else {
						statementBuf.append(" or ") ;
					}
					statementBuf.append(dialog_.getDetailColumnList().get(j).getFieldID()) ;
					statementBuf.append("!=") ;
					statementBuf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(j).getBasicType(), rowNumber.getKeyValueMap().get(dialog_.getDetailColumnList().get(j).getFieldID()), dbName));
					firstField = false;
				}
			}
		}
		statementBuf.append(")") ;

		return statementBuf.toString();
	}
	
	boolean hasPrimaryKeyValueAltered(XF110_SubListDetailRowNumber rowNumber) {
		boolean altered = false;
		for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
			if (dialog_.getDetailColumnList().get(j).isKey()) {
				if (!rowNumber.getColumnValueMap().get(dialog_.getDetailColumnList().get(j).getDataSourceName()).equals(rowNumber.getColumnOldValueMap().get(dialog_.getDetailColumnList().get(j).getDataSourceName()))) {
					altered = true;
					break;
				}
			}
		}
		return altered;
	}

	public String getTableID(){
		return tableID_;
	}
	
	public String getUpdateCounterID(){
		return updateCounterID;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return keyFieldIDList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}

	public org.w3c.dom.Element getTableElement() {
		return tableElement;
	}
	
	public ArrayList<String> getOrderByFieldIDList(){
		return orderByFieldIDList;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF110_SubListDetailReferTable referTable;
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
			for (int i = 0; i < dialog_.getDetailReferTableList().size(); i++) {
				referTable = dialog_.getDetailReferTableList().get(i);
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

	public int runScript(String event1, String event2, HashMap<String, Object> columnValueMap, HashMap<String, Object> columnOldValueMap) throws ScriptException {
		int countOfErrors = 0;
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		try {
			if (validScriptList.size() > 0) {
				for (int i = 0; i < validScriptList.size(); i++) {
					dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
				}
				for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
					columnValueMap.put(dialog_.getDetailColumnList().get(i).getDataSourceName(), dialog_.getDetailColumnList().get(i).getInternalValue());
					if (columnOldValueMap != null) {
						columnOldValueMap.put(dialog_.getDetailColumnList().get(i).getDataSourceName(), dialog_.getDetailColumnList().get(i).getOldValue());
					}
					if (dialog_.getDetailColumnList().get(i).isError()) {
						countOfErrors++;
					}
				}
			}
		} catch (Exception e) {
			throw new ScriptException("Failed to setup variants in script. " + e.getMessage());
		}
		return countOfErrors;
	}
}

class XF110_SubListDetailReferTable extends Object {
	//private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF110_SubList dialog_ = null;
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
	private String dbName = "";

	public XF110_SubListDetailReferTable(org.w3c.dom.Element referElement, XF110_SubList dialog){
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

		activeWhere = tableElement.getAttribute("ActiveWhere");

		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}

		workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			fieldIDList.add(workTokenizer.nextToken());
		}

		if (referElement_.getAttribute("ToKeyFields").equals("")) {
			org.w3c.dom.Element workElement = dialog.getSession().getTablePKElement(tableID);
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

		workTokenizer = new StringTokenizer(referElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldIDList.add(workTokenizer.nextToken());
		}

		if (referElement_.getAttribute("Optional").equals("T")) {
			isOptional = true;
		}
	}

	public String getSelectSQL(boolean isToGetRecordsForComboBox){
		int count;
		org.w3c.dom.Element workElement;
		StringBuffer buf = new StringBuffer();
		boolean validWhereKeys = false;
		
		///////////////////////////
		// Select-Fields section //
		///////////////////////////
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

		////////////////////////////
		// From and Where section //
		////////////////////////////
		buf.append(" from ");
		buf.append(tableID);
		StringTokenizer workTokenizer;
		String keyFieldID, keyFieldTableID;
		count = 0;
		boolean isToBeWithValue;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				rangeKeyFieldSearch = withKeyFieldIDList.get(i);
			} else {
				if (isToGetRecordsForComboBox) {
					/////////////////////////////////////////////////////////////////
					// Value of the field which has either of these conditions     //
					// should be within WHERE to SELECT records:                   //
					// 1. The with-key-field is not edit-able                      //
					// 2. The with-key-field is part of PK of the primary table    //
					// 3. The with-key-field is on the primary table and           //
					//    consists of upper part of with-key-fields                //
					// 4. The with-key-field is part of PK of the other join table //
					/////////////////////////////////////////////////////////////////
					for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getDataSourceName())) {
							isToBeWithValue = false;
							if (!dialog_.getDetailColumnList().get(j).isEditable()) {
								isToBeWithValue = true;
							} else {
								workTokenizer = new StringTokenizer(withKeyFieldIDList.get(i), "." );
								keyFieldTableID = workTokenizer.nextToken();
								keyFieldID = workTokenizer.nextToken();
								if (keyFieldTableID.equals(dialog_.getDetailTable().getTableID())) {
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
								buf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(j).getBasicType(), dialog_.getDetailColumnList().get(j).getInternalValue(), dbName));
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
					for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
						if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getDataSourceName())) {
//							if (XFUtility.isLiteralRequiredBasicType(dialog_.getDetailColumnList().get(j).getBasicType())) {
//								buf.append("'");
//								buf.append(dialog_.getDetailColumnList().get(j).getInternalValue());
//								buf.append("'");
//								if (!dialog_.getDetailColumnList().get(j).getInternalValue().equals("")) {
//									validWhereKeys = true;
//								}
//							} else {
//								buf.append(dialog_.getDetailColumnList().get(j).getInternalValue());
//								validWhereKeys = true;
//							}
							buf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList().get(j).getBasicType(), dialog_.getDetailColumnList().get(j).getInternalValue(), dbName));
							if (!dialog_.getDetailColumnList().get(j).getInternalValue().equals("")) {
								validWhereKeys = true;
							}
							break;
						}
					}
					count++;
				}
			}
		}
		if (!activeWhere.equals("")) {
			buf.append(" and ");
			buf.append(activeWhere);
		}

		//////////////////////
		// Order-by section //
		//////////////////////
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

		rangeValidated = false;

		if (isToGetRecordsForComboBox) {
			return buf.toString();
		} else {	
			if (validWhereKeys) {
				return buf.toString();
			} else {
				return "";
			}
		}
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
	
	public ArrayList<String> getKeyFieldIDList(){
		return toKeyFieldIDList;
	}
	
	public ArrayList<String> getWithKeyFieldIDList(){
		return withKeyFieldIDList;
	}

	public ArrayList<String> getFieldIDList(){
		return  fieldIDList;
	}
	
	public void setToBeExecuted(boolean executed){
		isToBeExecuted = executed;
	}
	
	public boolean isToBeExecuted(){
		return isToBeExecuted;
	}

	public boolean isKeyNullable() {
		boolean isKeyNullable = false;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
					if (dialog_.getDetailColumnList().get(j).isNullable()) {
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
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getTableAlias() + "." + dialog_.getDetailColumnList().get(j).getFieldID())) {
					if (dialog_.getDetailColumnList().get(j).isNull()) {
						isKeyNull = true;
						break;
					}
				}
			}
		}
		return isKeyNull;
	}

	public boolean isRecordToBeSelected(XFTableOperator operator) throws Exception{
		boolean returnValue = false;

		if (rangeKeyType == 0) {
			returnValue = true;
		}

		if (rangeKeyType == 1) {
			////////////////////////////////////////////////////////////////
			// Note that result set is ordered by rangeKeyFieldValue DESC //
			////////////////////////////////////////////////////////////////
			if (!rangeValidated) { 
				StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
				String workTableAlias = workTokenizer.nextToken();
				String workFieldID = workTokenizer.nextToken();
				Object valueKey = dialog_.getDetailColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
				Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
					rangeValidated = true;
				}
			}
		}

		if (rangeKeyType == 2) {
			StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
			String workTableAlias = workTokenizer.nextToken();
			String workFieldID = workTokenizer.nextToken();
			Object valueKey = dialog_.getDetailColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
			Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
			Object valueThru = operator.getValueOf(rangeKeyFieldExpire);
			if (valueThru == null) {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
				}
			} else {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				int comp2 = valueKey.toString().compareTo(valueThru.toString());
				if (comp1 >= 0 && comp2 < 0) {
					returnValue = true;
				}
			}
		}

		return returnValue;
	}

	public void setKeyFieldValues(XFHashMap keyValues){
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (dialog_.getDetailColumnList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))) {
					dialog_.getDetailColumnList().get(j).setValue(keyValues.getValue(toKeyFieldIDList.get(i)));
					break;
				}
			}
		}
	}

	public void setErrorOnRelatedFields() {
		boolean noneOfKeyFieldsWereSetError = true;

		//////////////////////////////////////////////////
		// Set error on the visible edit-able key field //
		//////////////////////////////////////////////////
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (dialog_.getDetailColumnList().get(j).isVisibleOnPanel()
						&& dialog_.getDetailColumnList().get(j).isEditable()
						&& dialog_.getDetailColumnList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
						&& !dialog_.getDetailColumnList().get(j).isError()) {
					dialog_.getDetailColumnList().get(j).setError(XFUtility.RESOURCE.getString("FunctionError53") + tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
					noneOfKeyFieldsWereSetError = false;
					break;
				}
			}
		}

		if (noneOfKeyFieldsWereSetError) {
			////////////////////////////////////////////////////////
			// Set error on the visible edit-able attribute field //
			////////////////////////////////////////////////////////
			for (int i = 0; i < fieldIDList.size(); i++) {
				for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
					if (dialog_.getDetailColumnList().get(j).isVisibleOnPanel()
							&& !dialog_.getDetailColumnList().get(j).isNonEditableField()
							&& dialog_.getDetailColumnList().get(j).getFieldID().equals(fieldIDList.get(i))
							&& dialog_.getDetailColumnList().get(j).getTableAlias().equals(this.tableAlias)
							&& !dialog_.getDetailColumnList().get(j).isError()) {
						dialog_.getDetailColumnList().get(j).setError(XFUtility.RESOURCE.getString("FunctionError53") + tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
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
				for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
					if (dialog_.getDetailColumnList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))
							&& !dialog_.getDetailColumnList().get(j).isError()) {
						dialog_.getDetailColumnList().get(j).setError(XFUtility.RESOURCE.getString("FunctionError53") + tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
						break;
					}
				}
			}
		}
	}
}

class XF110_SubListBatchComboBox extends JPanel implements XFEditableField {
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
	private XF110_SubListBatchReferTable referTable_ = null;
	private XF110_SubList dialog_;
	private String oldValue = "";
	private int indexOfField = -1;
	
	public XF110_SubListBatchComboBox(String dataSourceName, String dataTypeOptions, XF110_SubList dialog, XF110_SubListBatchReferTable chainTable, boolean isNullable){
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

		indexOfField = dialog_.getBatchFieldList().size();
		jTextField.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jTextField.setEditable(false);
		jTextField.setFocusable(false);
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
				if (referTable_ != null && isEditable && jComboBox.getSelectedIndex() >= 0) {
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
					XF110_SubListBatchComboBox comboBoxField;
					for (int i = 0; i < dialog_.getBatchFieldList().size(); i++) {
						if (i > indexOfField) {
							if (dialog_.getBatchFieldList().get(i).getComponent() instanceof XF110_SubListBatchComboBox) {
								comboBoxField = (XF110_SubListBatchComboBox)dialog_.getBatchFieldList().get(i).getComponent();
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
					String userVariantsTableID = dialog_.getSession().getTableNameOfUserVariants();
					String sql = "select * from " + userVariantsTableID + " where IDUSERKUBUN = '" + strWrk + "' order by SQLIST";
					XFTableOperator operator = dialog_.getReferOperator(sql);
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
				} catch (Exception e) {
					e.printStackTrace(dialog_.getExceptionStream());
					dialog_.setErrorAndCloseFunction();
				}
			} else {
				if (referTable_ != null) {
					listType = "RECORDS_LIST";
					keyFieldList = referTable_.getKeyFieldIDList();
					workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
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
					setupRecordList();
				}
			}
		}

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

			boolean blankItemRequired = false;
			XFHashMap blankKeyValues = new XFHashMap();
			for (int i = 0; i < referTable_.getWithKeyFieldIDList().size(); i++) {
				for (int j = 0; j < dialog_.getBatchFieldList().size(); j++) {
					if (referTable_.getWithKeyFieldIDList().get(i).equals(dialog_.getBatchFieldList().get(j).getTableAlias() + "." + dialog_.getBatchFieldList().get(j).getFieldID())) {
						if (dialog_.getBatchFieldList().get(j).isNullable()) {
							blankItemRequired = true;
							if (dialog_.getBatchFieldList().get(j).isVisibleOnPanel()
								|| dialog_.getBatchFieldList().get(j).isControledByFieldOtherThan(this)) {
								blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getBatchFieldList().get(j).getValue());
							} else {
								blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getBatchFieldList().get(j).getNullValue());
							}
						} else {
							blankKeyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), dialog_.getBatchFieldList().get(j).getValue());
						}
					}
				}
			}
			if (blankItemRequired) {
				tableKeyValuesList.add(blankKeyValues);
				jComboBox.addItem("");
			}

			try {
				String wrk = "";
				XFHashMap keyValues;
				XFTableOperator operator = dialog_.createTableOperator(referTable_.getSelectSQL(true));
				while (operator.next()) {
					if (referTable_.isRecordToBeSelected(operator)) {
						keyValues = new XFHashMap();
						for (int i = 0; i < keyFieldList.size(); i++) {
							keyValues.addValue(referTable_.getWithKeyFieldIDList().get(i), operator.getValueOf(keyFieldList.get(i)));
						}
						tableKeyValuesList.add(keyValues);
						wrk = operator.getValueOf(fieldID).toString().trim();
						jComboBox.addItem(wrk);
					}
				}
			} catch (Exception e) {
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
	
	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getExternalValue() {
		return jComboBox.getItemAt(jComboBox.getSelectedIndex()).toString();
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
	
	public void setValue(Object obj) {
		String value = (String)obj;
		value = value.trim();
//		if (jComboBox.getItemCount() > 0) {
//			jComboBox.setSelectedIndex(0);
//		}
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
							if (i != jComboBox.getSelectedIndex()) {
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
	
	public void setWidth(int width) {
		//jComboBox.setSize(width, jComboBox.getHeight());
		//jTextField.setSize(width - 17, jTextField.getHeight());
		this.setSize(new Dimension(width, XFUtility.FIELD_UNIT_HEIGHT));
	}

	public void setBackground(Color color) {
		if (jComboBox != null) {
			jComboBox.setBackground(color);
		}
	}

	public int getRows() {
		return rows_;
	}
}

class XF110_SubListBatchCodeText extends JTextField implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private String dataTypeOptions_ = "";
	private int fieldWidth = 0;
	private ArrayList<String> codeValueList = new ArrayList<String>();
	private ArrayList<String> textValueList = new ArrayList<String>();
	private XF110_SubList dialog_;
	
	public XF110_SubListBatchCodeText(String dataSourceName, String dataTypeOptions, XF110_SubList dialog){
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
					XFTableOperator operator = dialog_.createTableOperator(sql);
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

class XF110_SubListBatchPromptCall extends JPanel implements XFEditableField {
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
    private XF110_SubList dialog_;
    private String functionID_;
    private org.w3c.dom.Element fieldElement_;
    private ArrayList<XF110_SubListBatchReferTable> referTableList_;
    private String oldValue = "";
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();

	public XF110_SubListBatchPromptCall(org.w3c.dom.Element fieldElement, String functionID, XF110_SubList dialog){
		super();
		fieldElement_ = fieldElement;
		functionID_ = functionID;
		dialog_ = dialog;

		String fieldOptions = fieldElement_.getAttribute("FieldOptions");
		StringTokenizer workTokenizer = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		fieldID =workTokenizer.nextToken();
		tableID = tableAlias;
		referTableList_ = dialog_.getHeaderReferTableList();
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
		xFTextField.setEditable(tableID.equals(dialog_.getBatchTable().getTableID()));
		xFTextField.setFocusable(tableID.equals(dialog_.getBatchTable().getTableID()));
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

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XF110_SubList.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value;
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));

					HashMap<String, Object> fieldValuesMap = new HashMap<String, Object>();
					for (int i = 0; i < fieldsToPutList_.size(); i++) {
						value = dialog_.getValueOfBatchFieldByName(fieldsToPutList_.get(i));
						if (value == null) {
							JOptionPane.showMessageDialog(null, "Unable to send the value of field " + fieldsToPutList_.get(i));
						} else {
							fieldValuesMap.put(fieldsToPutToList_.get(i), value);
						}
					}

					HashMap<String, Object> returnMap = dialog_.getSession().executeFunction(functionID_, fieldValuesMap);
					//if (!returnMap.get("RETURN_CODE").equals("99")) {
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
						for (int i = 0; i < dialog_.getBatchFieldList().size(); i++) {
							value = fieldsToGetMap.get(dialog_.getBatchFieldList().get(i).getDataSourceName());
							if (value != null) {
								dialog_.getBatchFieldList().get(i).setValue(value);
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
		this.add(jButton, BorderLayout.EAST);
	}
	
	public ArrayList<String> getControlFieldList() {
		return fieldsToGetToList_;
	}

	public void setEditable(boolean editable) {
		isEditable = editable;
		jButton.setEnabled(isEditable);
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
			text = xFTextField.getText();
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
			xFTextField.setText(obj.toString());
		}
	}
	
	public void setWidth(int width) {
		xFTextField.setWidth(width - 27);
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
		return jButton.isFocusable();
	}

	public boolean isFocusable() {
		return false;
	}
}

class XF110_SubListjTableMain_focusAdapter extends java.awt.event.FocusAdapter {
	XF110_SubList adaptee;
	XF110_SubListjTableMain_focusAdapter(XF110_SubList adaptee) {
		this.adaptee = adaptee;
	}
	public void focusGained(FocusEvent e) {
		adaptee.jTableMain_focusGained(e);
	}
	public void focusLost(FocusEvent e) {
		adaptee.jTableMain_focusLost(e);
	}
}

class XF110_SubListjScrollPaneTable_mouseAdapter extends java.awt.event.MouseAdapter {
	XF110_SubList adaptee;
	XF110_SubListjScrollPaneTable_mouseAdapter(XF110_SubList adaptee) {
		this.adaptee = adaptee;
	}
	public void mousePressed(MouseEvent e) {
		adaptee.jScrollPaneTable_mousePressed(e);
	}
}

class XF110_SubListFunctionButton_actionAdapter implements java.awt.event.ActionListener {
	XF110_SubList adaptee;
	XF110_SubListFunctionButton_actionAdapter(XF110_SubList adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jFunctionButton_actionPerformed(e);
	}
}

class XF110_SubListReferCheckerConstructor implements Runnable {
	XF110_SubList adaptee;
	XF110_SubListReferCheckerConstructor(XF110_SubList adaptee) {
		this.adaptee = adaptee;
	}
	public void run() {
		ReferChecker checker = adaptee.getSession().createReferChecker(adaptee.getPrimaryTableID(), adaptee);
		adaptee.setReferChecker(checker);
	}
}
