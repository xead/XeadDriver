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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.w3c.dom.NodeList;

class XF310_AddRowList extends JDialog implements XFScriptable {
	private static final long serialVersionUID = 1L;
	private JPanel jPanelMain = new JPanel();
	private JScrollPane jScrollPaneTable = new JScrollPane();
	private TableModelMain tableModelMain;
	private JTable jTableMain = new JTable();
	private TableHeadersRenderer headersRenderer;
	private TableCellsRenderer cellsRenderer;
	private boolean isHeaderResizing = false;
	private XF310_AddRowListTable addRowListTable;
	private ArrayList<XF310_AddRowListColumn> addRowListColumnList = new ArrayList<XF310_AddRowListColumn>();
	private ArrayList<XF310_AddRowListReferTable> addRowListReferTableList = new ArrayList<XF310_AddRowListReferTable>();
	private ArrayList<XFTableOperator> referOperatorList = new ArrayList<XFTableOperator>();
	private JPanel jPanelTop = new JPanel();
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private JPanel jPanelInfo = new JPanel();
	private GridLayout gridLayoutInfo = new GridLayout();
	private JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private GridLayout gridLayoutButtons = new GridLayout();
	private JPanel[] jPanelButtonArray = new JPanel[6];
	private JButton[] jButtonArray = new JButton[6];
	private Action[] actionButtonArray = new Action[6];
	private String[] actionDefinitionArray = new String[6];
	private String addSelectedActionName = "";
	private XF310 dialog_;
	private ScriptEngine scriptEngine;
	private Bindings scriptBindings;
	private int result;
	private boolean isInvalid = false;
	private boolean isWithoutButtonToAddBlank;
	private boolean isWithoutButtonToCallFunction;
	private String lastSqlText = "";
	private ArrayList<XF310_AddRowListNumber> addRowListNumberReturnList = null;
	private Action helpAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			dialog_.getSession().browseHelp();
		}
	};
	private Action escapeAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			setVisible(false);
			dialog_.returnToMenu();
		}
	};
	
	public XF310_AddRowList(XF310 dialog) {
		super(dialog, "", true);
		org.w3c.dom.Element workElement;

		dialog_ = dialog;
		jPanelMain.setLayout(new BorderLayout());
		jPanelTop.setLayout(new BorderLayout());
		jTableMain.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jTableMain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTableMain.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableMain.setRowSelectionAllowed(true);
		jTableMain.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_ENTER
						|| e.getKeyCode() == KeyEvent.VK_SPACE)
						&& !addSelectedActionName.equals("")
						&& jTableMain.getSelectedRow() > -1) {
					XF310_AddRowListNumber rowNumber = (XF310_AddRowListNumber)tableModelMain.getValueAt(jTableMain.getSelectedRow(), 0);
					rowNumber.setSelected(!rowNumber.isSelected());
					jTableMain.updateUI();
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER && tableModelMain.getRowCount() > 0
						&& addSelectedActionName.equals("")) {
					processRow();
				}
			}
		});
		jTableMain.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2 && tableModelMain.getRowCount() > 0) {
					processRow();
				}
				if (e.getClickCount() == 1 && jTableMain.getSelectedRow() > -1) {
					XF310_AddRowListNumber rowNumber = (XF310_AddRowListNumber)tableModelMain.getValueAt(jTableMain.getSelectedRow(), 0);
					rowNumber.setSelected(!rowNumber.isSelected());
					jTableMain.updateUI();
				}
			}
		});
		jTableMain.addFocusListener(new FocusAdapter() {
			private Color selectionColorWithFocus = new Color(49,106,197);
			private Color selectionColorWithoutFocus = new Color(213,213,213);
			public void focusGained(FocusEvent e) {
				jTableMain.setSelectionBackground(selectionColorWithFocus);
				jTableMain.setSelectionForeground(Color.white);
			}
			public void focusLost(FocusEvent e) {
				jTableMain.setSelectionBackground(selectionColorWithoutFocus);
				jTableMain.setSelectionForeground(Color.black);
			}
		});
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
				if (headersRenderer.hasMouseOnColumnBorder(e.getX(), e.getY())) {
					isHeaderResizing = true;
					headersRenderer.setSizingHeader(e.getX());
				} else {
					headersRenderer.resortRowsByColumnAt(e.getX(), e.getY());
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
				headersRenderer.resetUnderlineOfColumns();
			}
		});
		header.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				if (headersRenderer.hasMouseOnColumnBorder(e.getX(), e.getY())) {
					setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
				} else {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
				headersRenderer.setUnderlineOnColumnAt(e.getX(), e.getY());
				jScrollPaneTable.updateUI();
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
				g2.fillRect(pointX,0,3,jTableMain.getHeight() + headersRenderer.getHeight());
				jScrollPaneTable.updateUI();
			}
		});
		jTableMain.setTableHeader(header);
		jScrollPaneTable.getViewport().add(jTableMain, null);
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jTextAreaMessages.setWrapStyleWord(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jScrollPaneMessages.setPreferredSize(new Dimension(10, 50));
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
		jPanelMain.add(jPanelTop, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelTop.add(jScrollPaneTable, BorderLayout.CENTER);
		jPanelTop.add(jScrollPaneMessages, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		gridLayoutButtons.setColumns(6);
		gridLayoutButtons.setRows(1);
		for (int i = 0; i < 6; i++) {
			jButtonArray[i] = new JButton();
			jButtonArray[i].setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
			jButtonArray[i].setFocusable(false);
			jButtonArray[i].addActionListener(new XF310_AddRowListFunctionButton_actionAdapter(this));
			jPanelButtonArray[i] = new JPanel();
			jPanelButtonArray[i].setLayout(new BorderLayout());
			jPanelButtonArray[i].add(jButtonArray[i], BorderLayout.CENTER);
			actionButtonArray[i] = new ButtonAction(jButtonArray[i]);
			jPanelButtons.add(jPanelButtonArray[i]);
		}
		NodeList buttonList = dialog_.getFunctionElement().getElementsByTagName("AddRowListButton");
		for (int i = 0; i < buttonList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)buttonList.item(i);
			if (workElement.getAttribute("Action").equals("ADD_SELECTED")) {
				addSelectedActionName = workElement.getAttribute("Caption");
			}
			if (workElement.getAttribute("Action").equals("ADD_BLANK")) {
				isWithoutButtonToAddBlank = false;
			}
			if (workElement.getAttribute("Action").contains("CALL(")) {
				isWithoutButtonToCallFunction = false;
			}
		}
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);

		StringTokenizer workTokenizer;
		int posX = 0;
		int posY = 0;
		String workStr, workAlias, workTableID, workFieldID;

		this.setTitle(dialog_.getTitle() + " - " + dialog_.getAddRowListTitle());
		jLabelSessionID.setText(dialog_.getSession().getSessionID());
		jLabelFunctionID.setText(dialog_.getFunctionInfo());
		FontMetrics metrics = jLabelFunctionID.getFontMetrics(jLabelFunctionID.getFont());
		jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));

		addRowListTable = new XF310_AddRowListTable(dialog_.getFunctionElement(), this);
		tableModelMain = new TableModelMain();
		jTableMain.setModel(tableModelMain);
		addRowListColumnList.clear();
		addRowListReferTableList.clear();

		addRowListReferTableList.clear();
		NodeList addRowListReferElementList = addRowListTable.getTableElement().getElementsByTagName("Refer");
		SortableDomElementListModel sortingList = XFUtility.getSortedListModel(addRowListReferElementList, "Order");
		for (int j = 0; j < sortingList.getSize(); j++) {
			addRowListReferTableList.add(new XF310_AddRowListReferTable((org.w3c.dom.Element)sortingList.getElementAt(j), this));
		}

		////////////////////////////////
		// Setup add-row-list columns //
		////////////////////////////////
		int columnIndex = 0;
		NodeList columnFieldList = dialog_.getFunctionElement().getElementsByTagName("AddRowListColumn");
		sortingList = XFUtility.getSortedListModel(columnFieldList, "Order");
		for (int j = 0; j < sortingList.getSize(); j++) {
			addRowListColumnList.add(new XF310_AddRowListColumn((org.w3c.dom.Element)sortingList.getElementAt(j), this));
			if (addRowListColumnList.get(j).isVisibleOnPanel()) {
				columnIndex++;
				addRowListColumnList.get(j).setColumnIndex(columnIndex);
			}
		}
		if (columnIndex == 0) {
			isInvalid = true;
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError52"));
			return;
		}
		headersRenderer = new TableHeadersRenderer(this); 
		cellsRenderer = new TableCellsRenderer(headersRenderer); 
		jTableMain.setRowHeight(headersRenderer.getHeight());
		tableModelMain.addColumn(""); //column index:0 //
		TableColumn column = jTableMain.getColumnModel().getColumn(0);
		column.setHeaderRenderer(headersRenderer);
		column.setCellRenderer(cellsRenderer);
		column.setPreferredWidth(headersRenderer.getWidth());
		int width = headersRenderer.getWidth();
		
		/////////////////////////////////////
		// Add key fields as HIDDEN column //
		/////////////////////////////////////
		for (int j = 0; j < addRowListTable.getKeyFieldIDList().size(); j++) {
			if (!containsAddRowListField(addRowListTable.getTableID(), "", addRowListTable.getKeyFieldIDList().get(j))) {
				addRowListColumnList.add(new XF310_AddRowListColumn(addRowListTable.getTableID(), "", addRowListTable.getKeyFieldIDList().get(j), this));
			}
		}
		
		//////////////////////////////////////
		// Add with-fields as HIDDEN column //
		//////////////////////////////////////
		for (int j = 0; j < addRowListTable.getWithFieldIDList().size(); j++) {
			if (!containsAddRowListField(addRowListTable.getTableID(), "", addRowListTable.getWithFieldIDList().get(j))) {
				addRowListColumnList.add(new XF310_AddRowListColumn(addRowListTable.getTableID(), "", addRowListTable.getWithFieldIDList().get(j), this));
			}
		}
		
		////////////////////////////////////////
		// Add return fields as HIDDEN column //
		////////////////////////////////////////
		for (int j = 0; j < addRowListTable.getReturnDataSourceList().size(); j++) {
			workTokenizer = new StringTokenizer(addRowListTable.getReturnDataSourceList().get(j), "." );
			workAlias = workTokenizer.nextToken();
			workTableID = dialog_.getTableIDOfTableAlias(workAlias);
			workFieldID = workTokenizer.nextToken();
			if (!containsAddRowListField(workTableID, workAlias, workFieldID)) {
				addRowListColumnList.add(new XF310_AddRowListColumn(workTableID, workAlias, workFieldID, this));
			}
		}
		
		/////////////////////////////////////////
		// Add OrderBy fields as HIDDEN column //
		/////////////////////////////////////////
		for (int j = 0; j < addRowListTable.getOrderByFieldIDList().size(); j++) {
			workStr = addRowListTable.getOrderByFieldIDList().get(j).replace("(D)", "");
			workStr = workStr.replace("(A)", "");
			workTokenizer = new StringTokenizer(workStr, "." );
			workAlias = workTokenizer.nextToken();
			workTableID = dialog_.getTableIDOfTableAlias(workAlias);
			workFieldID = workTokenizer.nextToken();
			if (!containsAddRowListField(workTableID, workAlias, workFieldID)) {
				addRowListColumnList.add(new XF310_AddRowListColumn(workTableID, workAlias, workFieldID, this));
			}
		}
		
		/////////////////////////////////////////////////////////////
		// Analyze fields in script and add them as HIDDEN columns //
		/////////////////////////////////////////////////////////////
		for (int j = 0; j < addRowListTable.getScriptList().size(); j++) {
			if	(addRowListTable.getScriptList().get(j).isToBeRunAtEvent("BR", "")
				|| addRowListTable.getScriptList().get(j).isToBeRunAtEvent("AR", "")) {
				for (int k = 0; k < addRowListTable.getScriptList().get(j).getFieldList().size(); k++) {
					workTokenizer = new StringTokenizer(addRowListTable.getScriptList().get(j).getFieldList().get(k), "." );
					workAlias = workTokenizer.nextToken();
					workTableID = dialog_.getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!containsAddRowListField(workTableID, workAlias, workFieldID) && addRowListTable.isValidDataSource(workTableID, workAlias, workFieldID)) {
						workElement = dialog_.getSession().getFieldElement(workTableID, workFieldID);
						if (workElement == null) {
							isInvalid = true;
							String msg = XFUtility.RESOURCE.getString("FunctionError1") + addRowListTable.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + addRowListTable.getScriptList().get(j).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
							JOptionPane.showMessageDialog(this, msg);
							return;
						} else {
							addRowListColumnList.add(new XF310_AddRowListColumn(workTableID, workAlias, workFieldID, this));
						}
					}
				}
			}
		}

		////////////////////////////////////////////////////////////
		// Add BYTEA-type-field for BYTEA field as HIDDEN columns //
		////////////////////////////////////////////////////////////
		for (int j = 0; j < addRowListColumnList.size(); j++) {
			if (addRowListColumnList.get(j).getBasicType().equals("BYTEA") && !addRowListColumnList.get(j).getByteaTypeFieldID().equals("")) {
				if (!containsAddRowListField(addRowListTable.getTableID(), "", addRowListColumnList.get(j).getByteaTypeFieldID())) {
					addRowListColumnList.add(new XF310_AddRowListColumn(addRowListTable.getTableID(), "", addRowListColumnList.get(j).getByteaTypeFieldID(), this));
				}
			}
		}
		
		////////////////////////////////////////////////////////////////////////
		// Analyze detail refer tables and add their fields as HIDDEN columns //
		////////////////////////////////////////////////////////////////////////
		for (int j = addRowListReferTableList.size()-1; j > -1; j--) {
			for (int k = 0; k < addRowListReferTableList.get(j).getFieldIDList().size(); k++) {
				if (containsAddRowListField(addRowListReferTableList.get(j).getTableID(), addRowListReferTableList.get(j).getTableAlias(), addRowListReferTableList.get(j).getFieldIDList().get(k))) {
					addRowListReferTableList.get(j).setToBeExecuted(true);
					break;
				}
			}
			if (addRowListReferTableList.get(j).isToBeExecuted()) {
				for (int k = 0; k < addRowListReferTableList.get(j).getFieldIDList().size(); k++) {
					if (!containsAddRowListField(addRowListReferTableList.get(j).getTableID(), addRowListReferTableList.get(j).getTableAlias(), addRowListReferTableList.get(j).getFieldIDList().get(k))) {
						addRowListColumnList.add(new XF310_AddRowListColumn(addRowListReferTableList.get(j).getTableID(), addRowListReferTableList.get(j).getTableAlias(), addRowListReferTableList.get(j).getFieldIDList().get(k), this));
					}
				}
				for (int k = 0; k < addRowListReferTableList.get(j).getWithKeyFieldIDList().size(); k++) {
					workTokenizer = new StringTokenizer(addRowListReferTableList.get(j).getWithKeyFieldIDList().get(k), "." );
					workAlias = workTokenizer.nextToken();
					workTableID = dialog_.getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!containsAddRowListField(workTableID, workAlias, workFieldID)) {
						addRowListColumnList.add(new XF310_AddRowListColumn(workTableID, workAlias, workFieldID, this));
					}
				}
			}
		}
		
		//////////////////////////////////////////////////////////////////
		// Add common used field (except for keys) to return field list //
		//////////////////////////////////////////////////////////////////
		addRowListTable.addCommonUsedColumnToReturnList();

		//////////////////////////////////////
		// Setup script engine and bindings //
		//////////////////////////////////////
		scriptEngine = dialog_.getScriptEngine();
		scriptBindings = scriptEngine.createBindings();
		scriptBindings.put("instance", (XFScriptable)this);
		for (int i = 0; i < addRowListColumnList.size(); i++) {
			scriptBindings.put(addRowListColumnList.get(i).getDataSourceID(), addRowListColumnList.get(i));
		}

        Rectangle screenRect = dialog_.getSession().getMenuRectangle();
		width = width + 50;
		if (width < 800) {
			width = 800;
		}
		if (width > screenRect.width) {
			width = screenRect.width;
		}
		int height = screenRect.height * width / screenRect.width;
		this.setPreferredSize(new Dimension(width, height));
		posX = ((screenRect.width - width) / 2) + screenRect.x;
		posY = ((screenRect.height - height) / 2) + screenRect.y;
		this.setLocation(posX, posY);
		this.pack();
		setupFunctionKeysAndButtons();
	}
	
	public int requestSelection() {
		try {
			selectDetailRecordsAndSetupTableRows();

			result = 0;
			addRowListNumberReturnList = new ArrayList<XF310_AddRowListNumber>();

			if (tableModelMain.getRowCount() == 0) {
				jTextAreaMessages.setText(XFUtility.RESOURCE.getString("FunctionMessage49") + addRowListTable.getTableElement().getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionMessage50"));
			} else {
				if (dialog_.getFunctionElement().getAttribute("AddRowListInitialMsg").equals("")) {
					if (addSelectedActionName.equals("")) {
						jTextAreaMessages.setText(XFUtility.RESOURCE.getString("FunctionMessage32"));
					} else {
						jTextAreaMessages.setText(XFUtility.RESOURCE.getString("FunctionMessage5") + addSelectedActionName + XFUtility.RESOURCE.getString("FunctionMessage6"));
					}
				} else {
					jTextAreaMessages.setText(dialog_.getFunctionElement().getAttribute("AddRowListInitialMsg"));
				}
			}

			if (!addSelectedActionName.equals("")) {
				for (int i = 0; i < tableModelMain.getRowCount(); i++) {
					XF310_AddRowListNumber rowNumber = (XF310_AddRowListNumber)tableModelMain.getValueAt(i, 0);
					rowNumber.setSelected(false);
				}
			}
			jTableMain.requestFocus();
			if (tableModelMain.getRowCount() > 0) {
				jTableMain.setRowSelectionInterval(0, 0);
			}
			this.setVisible(true);

		} catch (ScriptException e) {
			cancelWithScriptException(e, dialog_.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		}
		return result;
	}
	
	void selectDetailRecordsAndSetupTableRows() throws ScriptException, Exception {
		String workStr;
		HashMap<String, Object> columnValueMap, returnFieldMap;
		ArrayList<TableCellReadOnly> cellObjectList = null;
		ArrayList<Object> orderByValueList;
		ArrayList<String> orderByFieldTypeList;
		ArrayList<WorkingRow> tableRowList = new ArrayList<WorkingRow>();
		int countOfRows = 0;
		int workIndex;

		String sql = addRowListTable.getSQLToSelect();
		if (!sql.equals(lastSqlText)) {

			int rowCount = tableModelMain.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				tableModelMain.removeRow(0);
			}
			referOperatorList.clear();

			addRowListTable.runScript("BR", "", null); /* Script to be run BEFORE READ */

			XFTableOperator operator = dialog_.createTableOperator(sql);
			while (operator.next()) {
				for (int i = 0; i < addRowListColumnList.size(); i++) {
					addRowListColumnList.get(i).initialize();
					if (addRowListColumnList.get(i).getTableID().equals(addRowListTable.getTableID())
							&& !addRowListColumnList.get(i).isVirtualField()) {
						addRowListColumnList.get(i).setValueOfResultSet(operator);
					}
				}

				columnValueMap = new HashMap<String, Object>();
				for (int i = 0; i < addRowListColumnList.size(); i++) {
					columnValueMap.put(addRowListColumnList.get(i).getDataSourceName(), addRowListColumnList.get(i).getInternalValue());
				}

				fetchAddRowListReferRecords("AR", "", columnValueMap);

				returnFieldMap = new HashMap<String, Object>();
				cellObjectList = new ArrayList<TableCellReadOnly>();
				for (int i = 0; i < addRowListColumnList.size(); i++) {
					workIndex = addRowListTable.getReturnDataSourceList().indexOf(addRowListColumnList.get(i).getDataSourceName());
					if (workIndex > -1) {
						returnFieldMap.put(addRowListTable.getReturnToDetailDataSourceList().get(workIndex), addRowListColumnList.get(i).getInternalValue());
					}
					cellObjectList.add(addRowListColumnList.get(i).getCellObject());
					if (addRowListColumnList.get(i).getBasicType().equals("BYTEA")) {
						addRowListColumnList.get(i).setByteaType(addRowListColumnList);
					}
				}

				if (addRowListTable.isOrderByInSelectSQL()) {
					Object[] cell = new Object[1];
					cell[0] = new XF310_AddRowListNumber(countOfRows + 1, columnValueMap, returnFieldMap, cellObjectList);
					tableModelMain.addRow(cell);
				} else {
					orderByFieldTypeList = new ArrayList<String>();
					orderByValueList = new ArrayList<Object>();
					for (int i = 0; i < addRowListTable.getOrderByFieldIDList().size(); i++) {
						workStr = addRowListTable.getOrderByFieldIDList().get(i).replace("(D)", "");
						workStr = workStr.replace("(A)", "");
						for (int j = 0; j < addRowListColumnList.size(); j++) {
							if (addRowListColumnList.get(j).getDataSourceName().equals(workStr)) {
								orderByFieldTypeList.add(addRowListColumnList.get(j).getBasicType());
								orderByValueList.add(addRowListColumnList.get(j).getExternalValue());
								break;
							}
						}
					}
					tableRowList.add(new WorkingRow(cellObjectList, columnValueMap, returnFieldMap, orderByValueList, orderByFieldTypeList));
				}
				countOfRows++;
			}

			if (!addRowListTable.isOrderByInSelectSQL()) {
				WorkingRow[] workingRowArray = tableRowList.toArray(new WorkingRow[0]);
				Arrays.sort(workingRowArray);
				for (int i = 0; i < workingRowArray.length; i++) {
					Object[] cell = new Object[1];
					cell[0] = new XF310_AddRowListNumber(i + 1, workingRowArray[i].getColumnMap(), workingRowArray[i].getReturnFieldMap(), workingRowArray[i].getCellObjectList());
					tableModelMain.addRow(cell);
				}
			}

			lastSqlText = sql;
		}
	}
	
	public void resortTableRowsByColumnIndex(int col, boolean isAscending) {
		String workStr;
		ArrayList<Object> orderByValueList;
		ArrayList<String> orderByFieldTypeList;
		ArrayList<String> orderByFieldList = addRowListTable.getOrderByFieldIDList();
		ArrayList<WorkingRow> tableRowList = new ArrayList<WorkingRow>();
		workStr = addRowListColumnList.get(col).getDataSourceName();
		if (!isAscending) {
			workStr = workStr + "(D)";
		}
		orderByFieldList.add(0, workStr);
		
		int rowCount = tableModelMain.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			orderByValueList =new ArrayList<Object>();
			orderByFieldTypeList =new ArrayList<String>();
			XF310_AddRowListNumber cell = (XF310_AddRowListNumber)tableModelMain.getValueAt(i, 0);
			for (int j = 0; j < orderByFieldList.size(); j++) {
				workStr = orderByFieldList.get(j).replace("(D)", "");
				workStr = workStr.replace("(A)", "");
				for (int k = 0; k < addRowListColumnList.size(); k++) {
					if (addRowListColumnList.get(k).getDataSourceName().equals(workStr)) {
						TableCellReadOnly cellObject = cell.getCellObjectList().get(k);
						orderByValueList.add(cellObject.getInternalValue());
						orderByFieldTypeList.add(addRowListColumnList.get(k).getBasicType());
						break;
					}
				}
			}
			tableRowList.add(new WorkingRow(cell.getCellObjectList(), cell.getColumnMap(), cell.getReturnFieldMap(), orderByValueList, orderByFieldTypeList));
		}

		for (int i = 0; i < rowCount; i++) {
			tableModelMain.removeRow(0);
		}

		WorkingRow[] workingRowArray = tableRowList.toArray(new WorkingRow[0]);
		Arrays.sort(workingRowArray);
		for (int i = 0; i < workingRowArray.length; i++) {
			Object[] cell = new Object[1];
			cell[0] = new XF310_AddRowListNumber(i + 1, workingRowArray[i].getColumnMap(), workingRowArray[i].getReturnFieldMap(), workingRowArray[i].getCellObjectList());
			tableModelMain.addRow(cell);
		}

		orderByFieldList.remove(0);
	}

	boolean isInvalid() {
		return isInvalid;
	}
	
	public boolean isAvailable() {
		return dialog_.isAvailable();
	}
	
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			doExitAction();
		}
	}

	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(this, message);
		}
		result = 3;
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

	public HashMap<String, Object> getParmMap() {
		return dialog_.getParmMap();
	}
	
	public StringBuffer getProcessLog() {
		return dialog_.getProcessLog();
	}
	
	public void setProcessLog(String text) {
		dialog_.setProcessLog(text);
	}

	public Object getVariant(String variantID) {
		return dialog_.getVariant(variantID);
	}

	public void setVariant(String variantID, Object value) {
		dialog_.setVariant(variantID, value);
	}

	public XFTableOperator createTableOperator(String oparation, String tableID) {
		return dialog_.createTableOperator(oparation, tableID);
	}

	public XFTableOperator createTableOperator(String sqlText) {
		return dialog_.createTableOperator(sqlText);
	}

	public XFTableEvaluator createTableEvaluator(String tableID) {
		return new XFTableEvaluator(this, tableID);
	}

	public HashMap<String, Object> getReturnMap() {
		return dialog_.getReturnMap();
	}

	public void callFunction(String functionID) {
		try {
			dialog_.setReturnMap(dialog_.getSession().executeFunction(functionID, dialog_.getParmMap()));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			dialog_.setExceptionHeader(e.getMessage());
			setErrorAndCloseFunction();
		}
	}
	
	public void startProgress(String text, int maxValue) {
	}
	
	public void incrementProgress() {
	}
	
	public void endProgress() {
	}
	
	public void commit() {
		dialog_.commit();
	}
	
	public void rollback() {
		dialog_.rollback();
	}

	void setupFunctionKeysAndButtons() {
		InputMap inputMap  = jPanelMain.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.clear();
		ActionMap actionMap = jPanelMain.getActionMap();
		actionMap.clear();
		for (int i = 0; i < 6; i++) {
			jButtonArray[i].setText("");
			jButtonArray[i].setVisible(false);
			actionDefinitionArray[i] = "";
		}
		isWithoutButtonToAddBlank = true;
		isWithoutButtonToCallFunction = true;

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMap.put("HELP", helpAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
		actionMap.put("ESCAPE", escapeAction);

		int workIndex;
		org.w3c.dom.Element element;
		NodeList buttonList = dialog_.getFunctionElement().getElementsByTagName("AddRowListButton");
		for (int i = 0; i < buttonList.getLength(); i++) {
			element = (org.w3c.dom.Element)buttonList.item(i);
			workIndex = Integer.parseInt(element.getAttribute("Position"));
			actionDefinitionArray[workIndex] = element.getAttribute("Action");
			XFUtility.setCaptionToButton(jButtonArray[workIndex], element, "", this.getPreferredSize().width / 7);
			jButtonArray[workIndex].setVisible(true);
			inputMap.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
			actionMap.put("actionButton" + workIndex, actionButtonArray[workIndex]);
		}
	}
	
	private void processRow() {
		XF310_AddRowListNumber rowNumber = (XF310_AddRowListNumber)tableModelMain.getValueAt(jTableMain.getSelectedRow(), 0);
		addRowListNumberReturnList.add(rowNumber);
		result = 1;
		this.setVisible(false);
	}
	
	public ArrayList<XF310_AddRowListNumber> getSelectionList() {
		return addRowListNumberReturnList;
	}
	
	public ArrayList<XF310_AddRowListNumber> getDefaultRow() {
		ArrayList<XF310_AddRowListNumber> defaultRow = null;
		if (tableModelMain.getRowCount() == 1 && isWithoutButtonToAddBlank && isWithoutButtonToCallFunction) {
			defaultRow = new ArrayList<XF310_AddRowListNumber>();
			XF310_AddRowListNumber rowNumber = (XF310_AddRowListNumber)tableModelMain.getValueAt(0, 0);
			defaultRow.add(rowNumber);
		}
		return defaultRow;
	}
	
	private void fetchAddRowListReferRecords(String event, String specificReferTable, HashMap<String, Object> columnValueMap) throws ScriptException, Exception {
		String sql;
		XFTableOperator operator;

		addRowListTable.runScript(event, "BR()", columnValueMap); /* Script to be run AFTER READ primary table BEFORE READ any join tables */

		for (int i = 0; i < addRowListReferTableList.size(); i++) {
			if (specificReferTable.equals("") || specificReferTable.equals(addRowListReferTableList.get(i).getTableAlias())) {
				if (addRowListReferTableList.get(i).isToBeExecuted()) {

					addRowListTable.runScript(event, "BR(" + addRowListReferTableList.get(i).getTableAlias() + ")", columnValueMap); /* Script to be run BEFORE READ */

					if (!addRowListReferTableList.get(i).isKeyNull()) {

						sql = addRowListReferTableList.get(i).getSelectSQL();
						if (!sql.equals("")) {
							operator = null;
							for (int k = 0; k < referOperatorList.size(); k++) {
								if (referOperatorList.get(k).getSqlText().equals(sql)) {
									operator = referOperatorList.get(k);
									operator.resetCursor();
								}
							}
							if (operator == null ) {
								operator = createTableOperator(sql);
								referOperatorList.add(operator);
							}
							if (operator.next()) {
								for (int j = 0; j < addRowListColumnList.size(); j++) {
									if (addRowListColumnList.get(j).getTableAlias().equals(addRowListReferTableList.get(i).getTableAlias())) {
										addRowListColumnList.get(j).setValueOfResultSet(operator);
										columnValueMap.put(addRowListColumnList.get(j).getDataSourceName(), addRowListColumnList.get(j).getInternalValue());
									}
								}
								addRowListTable.runScript(event, "AR(" + addRowListReferTableList.get(i).getTableAlias() + ")", columnValueMap); /* Script to be run AFTER READ */
							}
						}
					}
				}
			}
		}

		addRowListTable.runScript(event, "AR()", columnValueMap); /* Script to be run AFTER READ all join tables */
	}

	public boolean containsAddRowListField(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < addRowListColumnList.size(); i++) {
			if (tableID.equals("")) {
				if (addRowListColumnList.get(i).getTableAlias().equals(tableAlias)
						&& addRowListColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (addRowListColumnList.get(i).getTableID().equals(tableID)
						&& addRowListColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (addRowListColumnList.get(i).getTableID().equals(tableID)
						&& addRowListColumnList.get(i).getTableAlias().equals(tableAlias)
						&& addRowListColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public Session getSession() {
		return dialog_.getSession();
	}
	
	public PrintStream getExceptionStream() {
		return dialog_.getExceptionStream();
	}
	
	public void setErrorAndCloseFunction() {
		dialog_.setErrorAndCloseFunction();
	}
	
	public ArrayList<XF310_DetailColumn> getDetailColumnList() {
		return dialog_.getDetailColumnList();
	}
	
	public String getFunctionID() {
		return dialog_.getFunctionID();
	}

	public Bindings getScriptBindings() {
		return scriptBindings;
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
	
	public void evalScript(String name, String text) throws ScriptException {
		dialog_.evalScript(name, text, scriptBindings);
	}
	
	public String getTableIDOfTableAlias(String tableAlias) {
		String tableID = tableAlias;
		for (int j = 0; j < addRowListReferTableList.size(); j++) {
			if (addRowListReferTableList.get(j).getTableAlias().equals(tableAlias)) {
				tableID = addRowListReferTableList.get(j).getTableID();
				break;
			}
		}
		return tableID;
	}
	
	public XF310_HeaderField getHeaderFieldObjectByID(String tableID, String alias, String fieldID) {
		return dialog_.getHeaderFieldObjectByID(tableID, alias, fieldID);
	}
	
	public ArrayList<XF310_AddRowListColumn> getAddRowListColumnList() {
		return addRowListColumnList;
	}

	public ArrayList<XF310_AddRowListReferTable> getAddRowListReferTableList() {
		return addRowListReferTableList;
	}
	
	public XF310_AddRowListTable getAddRowListTable() {
		return addRowListTable;
	}

	public XF310_AddRowListColumn getAddRowListColumnObjectByID(String tableID, String tableAlias, String fieldID) {
		XF310_AddRowListColumn addRowListColumnField = null;
		for (int i = 0; i < addRowListColumnList.size(); i++) {
			if (tableID.equals("")) {
				if (addRowListColumnList.get(i).getTableAlias().equals(tableAlias) && addRowListColumnList.get(i).getFieldID().equals(fieldID)) {
					addRowListColumnField = addRowListColumnList.get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (addRowListColumnList.get(i).getTableID().equals(tableID) && addRowListColumnList.get(i).getFieldID().equals(fieldID)) {
					addRowListColumnField = addRowListColumnList.get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (addRowListColumnList.get(i).getTableID().equals(tableID) && addRowListColumnList.get(i).getTableAlias().equals(tableAlias) && addRowListColumnList.get(i).getFieldID().equals(fieldID)) {
					addRowListColumnField = addRowListColumnList.get(i);
					break;
				}
			}
		}
		return addRowListColumnField;
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

	void doButtonAction(String action) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			jTextAreaMessages.setText("");

			if (action.equals("EXIT")) {
				doExitAction();
			}
			if (action.equals("ADD_SELECTED")) {
				XF310_AddRowListNumber rowNumber;
				for (int i = 0; i < tableModelMain.getRowCount(); i++) {
					rowNumber = (XF310_AddRowListNumber)tableModelMain.getValueAt(i, 0);
					if (rowNumber.isSelected()) {
						addRowListNumberReturnList.add(rowNumber);
					}
				}
				result = 1;
				this.setVisible(false);
			}
			if (action.equals("ADD_BLANK")) {
				addRowListNumberReturnList.clear();
				result = 2;
				this.setVisible(false);
			}
			if (action.contains("CALL")) {
				doCallAction(action);
				selectDetailRecordsAndSetupTableRows();
			}
		} catch (ScriptException e) {
			cancelWithScriptException(e, dialog_.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	void doExitAction() {
		addRowListNumberReturnList.clear();
		result = 0;
		this.setVisible(false);
	}
	
	void doCallAction(String action) {
		int pos1 = action.indexOf("CALL(", 0);
		if (pos1 >= 0) {
			int pos2 = action.indexOf(")", pos1);
			String functionID = action.substring(pos1+5, pos2);
			jTextAreaMessages.setText("");
			try {
				HashMap<String, Object> returnMap = dialog_.getSession().executeFunction(functionID, addRowListTable.getKeyMap());
				if (returnMap.get("RETURN_MESSAGE") == null) {
					jTextAreaMessages.setText(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
				} else {
					jTextAreaMessages.setText(returnMap.get("RETURN_MESSAGE").toString());
				}
				if (returnMap.get("RETURN_TO") != null && returnMap.get("RETURN_TO").equals("MENU")) {
					this.setVisible(false);
					dialog_.returnToMenu();
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
			}
		}
	}
	
	class TableModelMain extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		public boolean isCellEditable(int row, int col) {
			return col == 1;
		}
	}

	class WorkingRow extends Object implements Comparable {
		private HashMap<String, Object> columnMap_;
		private HashMap<String, Object> returnFieldMap_;
		private ArrayList<Object> orderByValueList_;
		private ArrayList<String> orderByFieldTypeList_ = null;
		private ArrayList<TableCellReadOnly> cellObjectList_;
		public WorkingRow(ArrayList<TableCellReadOnly> cellObjectList, HashMap<String, Object> columnMap, HashMap<String, Object> returnFieldMap, ArrayList<Object> orderByValueList, ArrayList<String> orderByFieldTypeList) {
			cellObjectList_ = cellObjectList;
			columnMap_ = columnMap;
			returnFieldMap_ = returnFieldMap;
			orderByValueList_ = orderByValueList;
			orderByFieldTypeList_ = orderByFieldTypeList;
		}
		public ArrayList<TableCellReadOnly> getCellObjectList() {
			return cellObjectList_;
		}
		public HashMap<String, Object> getReturnFieldMap() {
			return returnFieldMap_;
		}
		public HashMap<String, Object> getColumnMap() {
			return columnMap_;
		}
		public ArrayList<Object> getOrderByValueList() {
			return orderByValueList_;
		}
		public ArrayList<String> getOrderByFieldTypeList() {
			return orderByFieldTypeList_;
		}
		public int compareTo(Object other) {
			WorkingRow otherRow = (WorkingRow)other;
			int compareResult = 0;
			double doubleNumber1, doubleNumber2;
			String wrkStr;
			for (int i = 0; i < this.getOrderByValueList().size(); i++) {
				if (this.getOrderByFieldTypeList().get(i).equals("INTEGER")
						|| this.getOrderByFieldTypeList().get(i).equals("FLOAT")) {
					wrkStr = XFUtility.getStringNumber(this.getOrderByValueList().get(i).toString());
					doubleNumber1 = Double.parseDouble(wrkStr);
					wrkStr = XFUtility.getStringNumber(otherRow.getOrderByValueList().get(i).toString());
					doubleNumber2 = Double.parseDouble(wrkStr);
					compareResult = 0;
					if (doubleNumber1 > doubleNumber2) {
						compareResult = 1;
					}
					if (doubleNumber1 < doubleNumber2) {
						compareResult = -1;
					}
				} else {
					compareResult = this.getOrderByValueList().get(i).toString().compareTo(otherRow.getOrderByValueList().get(i).toString());
				}
				if (addRowListTable.getOrderByFieldIDList().get(i).contains("(D)")) {
					compareResult = compareResult * -1;
				}
				if (compareResult != 0) {
					break;
				}
			}
			return compareResult;
        }
	}

	class TableHeadersRenderer extends JPanel implements TableCellRenderer {   
		private static final long serialVersionUID = 1L;
		private JLabel numberLabel = new JLabel("No.");
		private JLabel checkBoxLabel = null;
		private JPanel westPanel = null;
		private JPanel centerPanel = new JPanel();
		private ArrayList<JLabel> headerList = new ArrayList<JLabel>();
		private int totalWidthOfCenterPanel = 0;
		private int totalHeight = 0;
		private Component sizingHeader = null;
		private JLabel sortingColumn = null;
		private boolean isAscendingColumnSorting = true;
		private XF310_AddRowList dialog_;

		public TableHeadersRenderer(XF310_AddRowList dialog) {
			dialog_ = dialog;
			arrangeColumnsPosition(true);
			centerPanel.setLayout(null);
			this.setLayout(new BorderLayout());
			numberLabel.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
			numberLabel.setBorder(new HeaderBorder());
			numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
			if (addSelectedActionName.equals("")) {
				this.add(numberLabel, BorderLayout.WEST);
				this.setPreferredSize(new Dimension(totalWidthOfCenterPanel + numberLabel.getPreferredSize().width, totalHeight));
			} else {
				westPanel = new JPanel();
				westPanel.setLayout(new GridLayout(1,2));
				checkBoxLabel = new JLabel(XFUtility.RESOURCE.getString("Sel"));
				checkBoxLabel.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
				checkBoxLabel.setBorder(new HeaderBorder());
				checkBoxLabel.setHorizontalAlignment(SwingConstants.CENTER);
				westPanel.add(numberLabel);
				westPanel.add(checkBoxLabel);
				this.add(westPanel, BorderLayout.WEST);
				this.setPreferredSize(new Dimension(totalWidthOfCenterPanel + westPanel.getPreferredSize().width, totalHeight));
			}
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
		
		public boolean hasMouseOnColumnBorder(int headerPosX, int headerPosY) {
			boolean result = false;
			double posX;
			if (westPanel == null) {
				posX = headerPosX - numberLabel.getBounds().getWidth();
			} else {
				posX = headerPosX - westPanel.getBounds().getWidth();
			}
			if (posX >= -3 && posX <= 0) {
				result = true;
			} else {
				for (int i = 0; i < headerList.size(); i++) {
					if (posX >= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width - 3)
						&& posX <= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width)
						&& headerPosY >= headerList.get(i).getBounds().y
						&& headerPosY <= (headerList.get(i).getBounds().y + headerList.get(i).getBounds().height)) {
						result = true;
						break;
					}
				}
			}
			return result;
		}
		
		public void setUnderlineOnColumnAt(int headerPosX, int headerPosY) {
			double posX;
			if (westPanel == null) {
				posX = headerPosX - numberLabel.getBounds().getWidth();
			} else {
				posX = headerPosX - westPanel.getBounds().getWidth();
			}
			for (int i = 0; i < headerList.size(); i++) {
				if (posX >= headerList.get(i).getBounds().x
				&& posX < (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width)
				&& headerPosY >= headerList.get(i).getBounds().y
				&& headerPosY < (headerList.get(i).getBounds().y + headerList.get(i).getBounds().height)) {
					headerList.get(i).setText("<html><u>"+headerList.get(i).getText());
				} else {
					if (!headerList.get(i).equals(sortingColumn)) {
						headerList.get(i).setText(headerList.get(i).getText().replace("<html><u>", ""));
					}
				}
			}
		}
		
		public void resetUnderlineOfColumns() {
			for (int i = 0; i < headerList.size(); i++) {
				if (!headerList.get(i).equals(sortingColumn)) {
					headerList.get(i).setText(headerList.get(i).getText().replace("<html><u>", ""));
				}
			}
		}
		
		public void clearSortingColumn() {
			sortingColumn = null;
			resetUnderlineOfColumns();
		}

		public void resortRowsByColumnAt(int headerPosX, int headerPosY) {
			double posX = headerPosX - numberLabel.getBounds().getWidth();
			for (int i = 0; i < headerList.size(); i++) {
				if (posX >= headerList.get(i).getBounds().x
						&& posX < (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width - 3)
						&& headerPosY >= headerList.get(i).getBounds().y
						&& headerPosY < (headerList.get(i).getBounds().y + headerList.get(i).getBounds().height)) {
					if (headerList.get(i).equals(sortingColumn)) {
						isAscendingColumnSorting = !isAscendingColumnSorting;
					} else {
						isAscendingColumnSorting = true;
						sortingColumn = headerList.get(i);
					}
					dialog_.resortTableRowsByColumnIndex(i, isAscendingColumnSorting);
				}
			}
		}
		
		public void setSizingHeader(int headersPosX) {
			double posX = headersPosX - westPanel.getBounds().getWidth();
			sizingHeader = westPanel;
			for (int i = 0; i < headerList.size(); i++) {
				if (posX >= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width - 3)
						&& posX <= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width)) {
					sizingHeader = headerList.get(i);
					break;
				}
			}
		}
		
		public void setNewBoundsToHeaders(int posXOnHeaders) {
			if (sizingHeader == westPanel) {
				westPanel.setPreferredSize(new Dimension(posXOnHeaders, totalHeight));
				this.setPreferredSize(new Dimension(totalWidthOfCenterPanel + posXOnHeaders, totalHeight));
			} else {
				int posX = posXOnHeaders - westPanel.getBounds().width;
				int widthAdjusted = 0;
				for (int i = 0; i < headerList.size(); i++) {
					if (sizingHeader == headerList.get(i)) {
						int newWidth = posX - headerList.get(i).getBounds().x;
						if (newWidth > 0) {
							addRowListColumnList.get(i).setWidth(newWidth);
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
			for (int i = 0; i < addRowListColumnList.size(); i++) {
				if (addRowListColumnList.get(i).isVisibleOnPanel()) {
					header = new JLabel();
					header.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
					if (addRowListColumnList.get(i).getValueType().equals("IMAGE")
							|| addRowListColumnList.get(i).getValueType().equals("FLAG")) {
						header.setHorizontalAlignment(SwingConstants.CENTER);
					} else {
						if (addRowListColumnList.get(i).getBasicType().equals("INTEGER")
								|| addRowListColumnList.get(i).getBasicType().equals("FLOAT")) {
							header.setHorizontalAlignment(SwingConstants.RIGHT);
						} else {
							header.setHorizontalAlignment(SwingConstants.LEFT);
						}
					}
					header.setText(addRowListColumnList.get(i).getCaption());
					header.setOpaque(true);

					width = addRowListColumnList.get(i).getWidth();
					height = XFUtility.ROW_UNIT_HEIGHT * addRowListColumnList.get(i).getRows();
					if (i > 0) {
						fromX = headerList.get(i-1).getBounds().x + headerList.get(i-1).getBounds().width;
						fromY = headerList.get(i-1).getBounds().y + headerList.get(i-1).getBounds().height;
						for (int j = i-1; j >= 0; j--) {
							if (addRowListColumnList.get(i).getLayout().equals("VERTICAL")) {
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
				if (westPanel == null) {
					numberLabel.setPreferredSize(new Dimension(XFUtility.SEQUENCE_WIDTH, totalHeight));
				} else {
					westPanel.setPreferredSize(new Dimension(XFUtility.SEQUENCE_WIDTH*2, totalHeight));
				}
			}
			centerPanel.setPreferredSize(new Dimension(totalWidthOfCenterPanel, totalHeight));
			if (westPanel == null) {
				this.setPreferredSize(new Dimension(totalWidthOfCenterPanel + numberLabel.getPreferredSize().width, totalHeight));
			} else {
				this.setPreferredSize(new Dimension(totalWidthOfCenterPanel + westPanel.getPreferredSize().width, totalHeight));
			}
		}

		public String getToolTipText(MouseEvent e) {
			String text = "";
			XF310_AddRowListColumn column;
			if (e.getPoint().x > numberLabel.getPreferredSize().width) {
				Component compo = centerPanel.getComponentAt(e.getPoint().x-numberLabel.getPreferredSize().width, e.getPoint().y);
				if (compo != null) {
					for (int i = 0; i < headerList.size(); i++) {
						if (compo.equals(headerList.get(i))) {
							column = addRowListColumnList.get(i);
							if (column.getDecimalSize() > 0) {
								text = "<html>" + column.getName() + " " + column.getDataSourceName() + " (" + column.getDataSize() + "," + column.getDecimalSize() + ")<br>" + column.getFieldRemarks();
							} else {
								text = "<html>" + column.getName() + " " + column.getDataSourceName() + " (" + column.getDataSize() + ")<br>" + column.getFieldRemarks();
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
		private JCheckBox checkBoxCell = null;
		private JPanel westPanel = null;
		private JPanel multiLinesPanel = new JPanel();
		private ArrayList<JLabel> cellList = new ArrayList<JLabel>();
		private TableHeadersRenderer headersRenderer_;

		public TableCellsRenderer(TableHeadersRenderer headersRenderer) {
			headersRenderer_ = headersRenderer;
			numberCell.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
			numberCell.setBorder(new CellBorder());
			numberCell.setHorizontalAlignment(SwingConstants.CENTER);
			multiLinesPanel.setLayout(null);
			multiLinesPanel.setOpaque(false);
			setupCellBounds();
			this.setLayout(new BorderLayout());
			if (addSelectedActionName.equals("")) {
				this.add(numberCell, BorderLayout.WEST);
			} else {
				westPanel = new JPanel();
				westPanel.setLayout(new GridLayout(1,2));
				westPanel.setBorder(new CellBorder());
				westPanel.setOpaque(false);
				checkBoxCell = new JCheckBox();
				checkBoxCell.setBorder(BorderFactory.createEtchedBorder());
				checkBoxCell.setHorizontalAlignment(SwingConstants.CENTER);
				checkBoxCell.setOpaque(false);
				westPanel.add(numberCell);
				westPanel.add(checkBoxCell);
				this.add(westPanel, BorderLayout.WEST);
			}

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

			XF310_AddRowListNumber rowObject = (XF310_AddRowListNumber)value;
			numberCell.setText(rowObject.getRowNumberString());
			if (checkBoxCell != null) {
				checkBoxCell.setSelected(rowObject.isSelected());
			}
			for (int i = 0; i < cellList.size(); i++) {
				cellList.get(i).setEnabled(addRowListColumnList.get(i).isEnabled());
				if (addRowListColumnList.get(i).getValueType().equals("IMAGE")
						|| addRowListColumnList.get(i).getValueType().equals("FLAG")) {
					cellList.get(i).setIcon((Icon)rowObject.getCellObjectList().get(i).getExternalValue());
				} else {
					cellList.get(i).setText((String)rowObject.getCellObjectList().get(i).getExternalValue());
					if (isSelected) {
						cellList.get(i).setForeground(table.getSelectionForeground());
					} else {
						if (rowObject.getCellObjectList().get(i).getColor().equals(Color.black)) {
							cellList.get(i).setForeground(table.getForeground());
						} else {
							cellList.get(i).setForeground(rowObject.getCellObjectList().get(i).getColor());
						}
					}
				}
			}
			return this;
		}
		
		public void setupCellBounds() {
			JLabel cell;
			Rectangle rec;
			cellList.clear();
			multiLinesPanel.removeAll();

			for (int i = 0; i < headersRenderer_.getColumnHeaderList().size(); i++) {
				cell = new JLabel();
				cell.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
				cell.setHorizontalAlignment(headersRenderer_.getColumnHeaderList().get(i).getHorizontalAlignment());
				rec = headersRenderer_.getColumnHeaderList().get(i).getBounds();
				cell.setBounds(rec.x, rec.y, rec.width, rec.height);
				cell.setBorder(new HeaderBorder());
				cellList.add(cell);
				multiLinesPanel.add(cell);
			}

			int totalWidth = headersRenderer_.getWidth() - headersRenderer_.getSequenceWidth();
			int totalHeight = headersRenderer_.getHeight();
			multiLinesPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
			numberCell.setPreferredSize(new Dimension(headersRenderer_.getSequenceWidth(), totalHeight));
			if (westPanel == null) {
				this.setPreferredSize(new Dimension(totalWidth + headersRenderer_.getSequenceWidth(), totalHeight));
			} else {
				westPanel.setPreferredSize(new Dimension(headersRenderer_.getSequenceWidth() * 2, totalHeight));
				this.setPreferredSize(new Dimension(totalWidth + (headersRenderer_.getSequenceWidth() * 2), totalHeight));
			}
		}
	} 
}

class XF310_AddRowListTable extends Object {
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID_ = "";
	private String additionalWhere = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withFieldIDList = new ArrayList<String>();
	private ArrayList<String> withHeaderDataSourceList = new ArrayList<String>();
	private ArrayList<String> returnDataSourceList = new ArrayList<String>();
	private ArrayList<String> returnToDetailDataSourceList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF310_AddRowList dialog_;
	private StringTokenizer workTokenizer;
	private boolean isOrderByInSelectSQL = true;
	private ArrayList<Object> upperKeyGroupValueList = new ArrayList<Object>();
	private String dbName = "";

	public XF310_AddRowListTable(org.w3c.dom.Element functionElement, XF310_AddRowList dialog){
		super();
		functionElement_ = functionElement;
		dialog_ = dialog;

		tableID_ = functionElement_.getAttribute("AddRowListTable");
		tableElement = dialog_.getSession().getTableElement(tableID_);

		if (tableElement.getAttribute("DB").equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(tableElement.getAttribute("DB"));
		}

		int pos1;
		String wrkStr1, wrkStr2;
		org.w3c.dom.Element workElement;

		NodeList nodeList = tableElement.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("PK")) {
				workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
				while (workTokenizer.hasMoreTokens()) {
					wrkStr1 = workTokenizer.nextToken();
					keyFieldIDList.add(wrkStr1);
				}
				break;
			}
		}

		workTokenizer = new StringTokenizer(functionElement_.getAttribute("AddRowListWithFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withFieldIDList.add(workTokenizer.nextToken());
		}

		workTokenizer = new StringTokenizer(functionElement_.getAttribute("AddRowListWithHeaderFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withHeaderDataSourceList.add(workTokenizer.nextToken());
		}

		workTokenizer = new StringTokenizer(functionElement_.getAttribute("AddRowListReturnDataSources"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			returnDataSourceList.add(workTokenizer.nextToken());
		}

		workTokenizer = new StringTokenizer(functionElement_.getAttribute("AddRowListReturnToDetailDataSources"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			returnToDetailDataSourceList.add(workTokenizer.nextToken());
		}

		workTokenizer = new StringTokenizer(functionElement_.getAttribute("AddRowListOrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			wrkStr1 = workTokenizer.nextToken();
			pos1 = wrkStr1.indexOf(".");
			if (pos1 > -1) { 
				wrkStr2 = wrkStr1.substring(0, pos1);
				if (!wrkStr2.equals(tableID_)) {
					isOrderByInSelectSQL = false;
				}
			}
			orderByFieldIDList.add(wrkStr1);
		}

		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID_, element, dialog_.getSession().getTableNodeList()));
		}
	}
	
	public String getSQLToSelect(){
		int count;
		XF310_HeaderField headerField;
		StringBuffer buf = new StringBuffer();
		String workAlias, workTableID, workFieldID;
		ArrayList<String> fieldIDList = new ArrayList<String>();
		
		////////////////////////////////
		// Select-Fields-From section //
		////////////////////////////////
		buf.append("select ");
		count = -1;
		for (int i = 0; i < keyFieldIDList.size(); i++) {
			count++;
			if (count > 0) {
				buf.append(", ");
			}
			buf.append(keyFieldIDList.get(i));
			fieldIDList.add(keyFieldIDList.get(i));
		}
		for (int i = 0; i < withFieldIDList.size(); i++) {
			if (!fieldIDList.contains(withFieldIDList.get(i))) {
				count++;
				if (count > 0) {
					buf.append(", ");
				}
				buf.append(withFieldIDList.get(i));
				fieldIDList.add(withFieldIDList.get(i));
			}
		}
		for (int i = 0; i < dialog_.getAddRowListColumnList().size(); i++) {
			if (dialog_.getAddRowListColumnList().get(i).getTableID().equals(tableID_)
					&& !dialog_.getAddRowListColumnList().get(i).isVirtualField()
					&& !dialog_.getAddRowListColumnList().get(i).getBasicType().equals("BYTEA")) {
				if (!fieldIDList.contains(dialog_.getAddRowListColumnList().get(i).getFieldID())) {
					count++;
					if (count > 0) {
						buf.append(", ");
					}
					buf.append(dialog_.getAddRowListColumnList().get(i).getFieldID());
					fieldIDList.add(dialog_.getAddRowListColumnList().get(i).getFieldID());
				}
			}
		}
		buf.append(" from ");
		buf.append(tableID_);

		///////////////////
		// Where section //
		///////////////////
		additionalWhere = XFUtility.getFixedWhereValue(functionElement_.getAttribute("AddRowListWhere"), dialog_.getSession());
		if (withFieldIDList.size() > 0) {
			buf.append(" where ") ;
			count = -1;
			for (int i = 0; i < withFieldIDList.size(); i++) {
				count++;
				if (count > 0) {
					buf.append(" and ") ;
				}
				buf.append(withFieldIDList.get(i)) ;
				buf.append("=") ;
				workTokenizer = new StringTokenizer(withHeaderDataSourceList.get(i), "." );
				workAlias = workTokenizer.nextToken();
				workTableID = dialog_.getTableIDOfTableAlias(workAlias);
				workFieldID = workTokenizer.nextToken();
				headerField = dialog_.getHeaderFieldObjectByID(workTableID, workAlias, workFieldID);
				buf.append(XFUtility.getTableOperationValue(headerField.getBasicType(), headerField.getInternalValue(), dbName)) ;
			}
		}
		if (!additionalWhere.equals("")) {
			if (withFieldIDList.size() > 0) {
				buf.append(" and (");
				buf.append(additionalWhere);
				buf.append(") ");
			} else {
				buf.append(" where ") ;
				buf.append(additionalWhere);
			}
		}
		
		//////////////////////
		// Order-by section //
		//////////////////////
		int pos0,pos1;
		count = -1;
		if (isOrderByInSelectSQL) {
			if (orderByFieldIDList.size() > 0) {
				for (int i = 0; i < orderByFieldIDList.size(); i++) {
					count++;
					if (count <= 0) {
						buf.append(" order by ");
					} else {
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
			} else {
				for (int i = 0; i < keyFieldIDList.size(); i++) {
					count++;
					if (count <= 0) {
						buf.append(" order by ");
					} else {
						buf.append(",");
					}
					buf.append(keyFieldIDList.get(i));
				}
			}
		}

		return buf.toString();
	}

	boolean isNewUpperKeyGroup(XFTableOperator operator) throws Exception {
		boolean returnValue = false;
		for (int i = 0; i < keyFieldIDList.size(); i++) {
			if (!upperKeyGroupValueList.get(i).equals(operator.getValueOf(keyFieldIDList.get(i)))) {
				returnValue = true;
				break;
			}
			upperKeyGroupValueList.add(i, operator.getValueOf(keyFieldIDList.get(i)));
		}
		return returnValue;
	}

	public HashMap<String, Object> getKeyMap() {
		HashMap<String, Object> keyMap = new HashMap<String, Object>();
		String workAlias, workTableID, workFieldID;
		XF310_HeaderField headerField;
		for (int i = 0; i < withFieldIDList.size(); i++) {
			workTokenizer = new StringTokenizer(withHeaderDataSourceList.get(i), "." );
			workAlias = workTokenizer.nextToken();
			workTableID = dialog_.getTableIDOfTableAlias(workAlias);
			workFieldID = workTokenizer.nextToken();
			headerField = dialog_.getHeaderFieldObjectByID(workTableID, workAlias, workFieldID);
			keyMap.put(withFieldIDList.get(i), headerField.getInternalValue()) ;
		}
		return keyMap;
	}
	
	public String getTableID(){
		return tableID_;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return keyFieldIDList;
	}
	
	public ArrayList<String> getWithFieldIDList(){
		return withFieldIDList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}
	
	public org.w3c.dom.Element getTableElement() {
		return tableElement;
	}
	
	public ArrayList<String> getReturnDataSourceList(){
		return returnDataSourceList;
	}
	
	public ArrayList<String> getReturnToDetailDataSourceList(){
		return returnToDetailDataSourceList;
	}
	
	public void addCommonUsedColumnToReturnList() {
		if (functionElement_.getAttribute("AddRowListReturnDataSources").equals("")) {
			for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
				if (!dialog_.getDetailColumnList().get(i).isKey()) {
					for (int j = 0; j < dialog_.getAddRowListColumnList().size(); j++) {
						if (dialog_.getDetailColumnList().get(i).getDataSourceName().equals(dialog_.getAddRowListColumnList().get(j).getDataSourceName())) {
							returnDataSourceList.add(dialog_.getDetailColumnList().get(i).getDataSourceName());
							returnToDetailDataSourceList.add(dialog_.getDetailColumnList().get(i).getDataSourceName());
						}
					}
				}
			}
		}
	}
	
	public ArrayList<String> getOrderByFieldIDList(){
		return orderByFieldIDList;
	}
	
	public boolean isOrderByInSelectSQL(){
		return isOrderByInSelectSQL;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF310_AddRowListReferTable referTable;
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
			for (int i = 0; i < dialog_.getAddRowListReferTableList().size(); i++) {
				referTable = dialog_.getAddRowListReferTableList().get(i);
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

	public void runScript(String event1, String event2, HashMap<String, Object> columnValueMap) throws ScriptException {
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		for (int i = 0; i < validScriptList.size(); i++) {
			dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
		}
		if (columnValueMap != null) {
			for (int i = 0; i < dialog_.getAddRowListColumnList().size(); i++) {
				columnValueMap.put(dialog_.getAddRowListColumnList().get(i).getDataSourceName(), dialog_.getAddRowListColumnList().get(i).getInternalValue());
			}
		}
	}
}

class XF310_AddRowListColumn implements XFFieldScriptable {
	private org.w3c.dom.Element functionColumnElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF310_AddRowList dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private String fieldName = "";
	private String fieldRemarks = "";
	private String dataType = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private String fieldOptions = "";
	private String fieldCaption = "";
	private String byteaTypeFieldID = "";
	private int fieldWidth = 50;
	private int columnIndex = -1;
	private boolean isEnabled = true;
	private boolean isVisibleOnPanel = true;
	private boolean isVirtualField = false;
	private String valueType = "STRING";
	private String flagTrue = "";
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private Object value_ = null;
	private Color foreground = Color.black;
	private int fieldRows = 1;
	private String fieldLayout = "HORIZONTAL";

	public XF310_AddRowListColumn(org.w3c.dom.Element functionColumnElement, XF310_AddRowList dialog){
		super();
		String wrkStr;
		functionColumnElement_ = functionColumnElement;
		dialog_ = dialog;
		fieldOptions = functionColumnElement_.getAttribute("FieldOptions");

		StringTokenizer workTokenizer = new StringTokenizer(functionColumnElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		tableID = dialog.getTableIDOfTableAlias(tableAlias);
		fieldID =workTokenizer.nextToken();

		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID + "." + fieldID + XFUtility.RESOURCE.getString("FunctionError11"));
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
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();

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

		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";");
			if (workTokenizer.countTokens() >= 1) {
				flagTrue = workTokenizer.nextToken();
			}
			valueType = "FLAG";
			fieldWidth = 25;
		} else {
			String basicType = this.getBasicType();
			wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
			if (!wrkStr.equals("")) {
				String wrk = "";
				try {
					XFTableOperator operator = dialog_.createTableOperator("Select", dialog_.getSession().getTableNameOfUserVariants());
					operator.addKeyValue("IDUSERKUBUN", wrkStr);
					operator.setOrderBy("SQLIST");
					while (operator.next()) {
						kubunValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
						wrk = operator.getValueOf("TXUSERKUBUN").toString().trim();
						if (metrics.stringWidth(wrk) + 10 > fieldWidth) {
							fieldWidth = metrics.stringWidth(wrk) + 10;
						}
						kubunTextList.add(wrk);
					}
				} catch (Exception e) {
					e.printStackTrace(dialog_.getExceptionStream());
					dialog_.setErrorAndCloseFunction();
				}
			} else {
				if (dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS")) {
					fieldWidth = dataSize * XFUtility.FONT_SIZE + 5;
				} else {
					if (dataTypeOptionList.contains("FYEAR")) {
						fieldWidth = 100;
					} else {
						if (dataTypeOptionList.contains("YMONTH")) {
							fieldWidth = 120;
						} else {
							if (dataTypeOptionList.contains("MSEQ")) {
								fieldWidth = 80;
							} else {
								if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
									fieldWidth = XFUtility.getLengthOfEdittedNumericValue(dataSize, decimalSize, dataTypeOptionList) * (XFUtility.FONT_SIZE/2 + 2) + 15;
								} else {
									if (basicType.equals("DATE")) {
										fieldWidth = XFUtility.getWidthOfDateValue(dialog_.getSession().getDateFormat(), dialog_.getSession().systemFont, XFUtility.FONT_SIZE);
									} else {
										if (basicType.equals("BYTEA")) {
											valueType = "BYTEA";
											fieldWidth = 100;
										} else {
											if (dataTypeOptionList.contains("IMAGE")) {
												valueType = "IMAGE";
												fieldWidth = 60;
												fieldRows = 2;
											} else {
												fieldWidth = dataSize * (XFUtility.FONT_SIZE/2 + 2) + 15;
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
	}

	public XF310_AddRowListColumn(String tableID, String tableAlias, String fieldID, XF310_AddRowList dialog){
		super();
		functionColumnElement_ = null;
		dialog_ = dialog;
		fieldOptions = "";
		isVisibleOnPanel = false;

		this.tableID = tableID;
		if (tableAlias.equals("")) {
			this.tableAlias = tableID;
		} else {
			this.tableAlias = tableAlias;
		}
		this.fieldID = fieldID;

		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(this.tableID, this.fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, this.tableID + "." + this.fieldID + XFUtility.RESOURCE.getString("FunctionError11"));
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
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();

		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
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

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}

	public String getByteaTypeFieldID(){
		return byteaTypeFieldID;
	}

	public String getTableID(){
		return tableID;
	}

	public String getFieldID(){
		return fieldID;
	}

	public String getDataSourceID(){
		return tableAlias + "_" + fieldID;
	}

	public String getName(){
		return fieldName;
	}

	public void setValueList(String[] valueList) {
	}

	public String[] getValueList() {
		return new String[0];
	}

	public String getFieldRemarks(){
		return fieldRemarks;
	}

	public String getTableAlias(){
		return tableAlias;
	}

	public String getDataSourceName(){
		return tableAlias + "." + fieldID;
	}

	public Object getInternalValue(){
		return value_;
	}

	public Object getExternalValue(){
		Object value = null;
		String basicType = this.getBasicType();
		if (basicType.equals("INTEGER")) {
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
							String wrkStr = value_.toString().trim();
							for (int i = 0; i < kubunValueList.size(); i++) {
								if (kubunValueList.get(i).equals(wrkStr)) {
									value = kubunTextList.get(i);
									break;
								}
							}
						} else {
							if (valueType.equals("STRING")) {
								if (value_ != null) {
									value = value_.toString().trim();
									if (dataTypeOptionList.contains("YMONTH") || dataTypeOptionList.contains("FYEAR")) {
										String wrkStr = value.toString();
										if (!wrkStr.equals("")) {
											value = XFUtility.getUserExpressionOfYearMonth(wrkStr, dialog_.getSession().getDateFormat());
										}
									}
									if (dataTypeOptionList.contains("MSEQ")) {
										String wrkStr = value.toString();
										if (!wrkStr.equals("")) {
											value = XFUtility.getUserExpressionOfMSeq(Integer.parseInt(wrkStr), dialog_.getSession());
										}
									}
								}
							}
							if (valueType.equals("IMAGE")) {
								String fileName = dialog_.getSession().getImageFileFolder() + value_.toString().trim();
								int iconHeight = fieldRows * XFUtility.ROW_UNIT_HEIGHT;
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

	public TableCellReadOnly getCellObject() {
		return new TableCellReadOnly(this.getInternalValue(), this.getExternalValue(), this.getForeground(), valueType);
	}

	public boolean isNull(){
		return XFUtility.isNullValue(this.getBasicType(), value_);
	}

	public String getDataTypeOptions() {
		return dataTypeOptions;
	}
	
	public ArrayList<String> getTypeOptionList() {
		return dataTypeOptionList;
	}

	public String getFieldOptions() {
		return fieldOptions;
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

	public void setValueOfResultSet(XFTableOperator operator) throws Exception {
		String basicType = this.getBasicType();
		this.setColor("");
		if (!this.isVirtualField) {
			if (basicType.equals("BYTEA")) {
				value_ = new XFByteArray(null);
			} else {
				Object value = operator.getValueOf(fieldID); 
				if (basicType.equals("INTEGER")) {
					if (value == null || value.equals("")) {
						value_ = "";
					} else {
						String wrkStr = value.toString();
						int pos = wrkStr.indexOf(".");
						if (pos >= 0) {
							wrkStr = wrkStr.substring(0, pos);
						}
						value_ = Integer.parseInt(wrkStr);
					}
				} else {
					if (basicType.equals("FLOAT")) {
						if (value == null || value.equals("")) {
							value_ = "";
						} else {
							value_ = Float.parseFloat(value.toString());
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
	}

	public void initialize() {
		value_ = XFUtility.getNullValueOfBasicType(this.getBasicType());
	}
	
	public void setByteaType(ArrayList<XF310_AddRowListColumn> columnList) {
		if (isVisibleOnPanel) {
			for (int i = 0; i < columnList.size(); i++) {
				if (columnList.get(i).getFieldID().equals(byteaTypeFieldID)) {
					((XFByteArray)value_).setType(columnList.get(i).getValue().toString());
					break;
				}
			}
		}
	}

	public void setValue(Object value){
		value_ = XFUtility.getValueAccordingToBasicType(this.getBasicType(), value);
	}

	public Object getValue() {
		return getInternalValue();
	}

	public void setOldValue(Object value){
	}

	public Object getOldValue() {
		return getInternalValue();
	}
	
	public boolean isValueChanged() {
		return !this.getValue().equals(this.getOldValue());
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public void setEditable(boolean editable){
	}

	public boolean isEditable() {
		return false;
	}

	public void setError(String message) {
	}

	public String getError() {
		return "";
	}

	public void setColor(String color) {
		foreground = XFUtility.convertStringToColor(color);
	}

	public String getColor() {
		return XFUtility.convertColorToString(foreground);
	}

	Color getForeground() {
		return foreground;
	}
}

class XF310_AddRowListReferTable extends Object {
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF310_AddRowList dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private String dbName = "";

	public XF310_AddRowListReferTable(org.w3c.dom.Element referElement, XF310_AddRowList dialog){
		super();
		referElement_ = referElement;
		dialog_ = dialog;

		tableID = referElement_.getAttribute("ToTable");
		tableElement = dialog_.getSession().getTableElement(tableID);

		if (tableElement.getAttribute("DB").equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(tableElement.getAttribute("DB"));
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
	}

	public String getSelectSQL(){
		int count;
		StringBuffer buf = new StringBuffer();
		org.w3c.dom.Element workElement;
		boolean validWhereKeys = false;
		
		////////////////////////////////
		// Select-Fields-From section //
		////////////////////////////////
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
		if (count == 0) {
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				if (count > 0) {
					buf.append(",");
				}
				count++;
				buf.append(toKeyFieldIDList.get(i));
			}
		}
		buf.append(" from ");
		buf.append(tableID);
		
		///////////////////
		// Where section //
		///////////////////
		buf.append(" where ");
		count = 0;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (count > 0) {
				buf.append(" and ");
			}
			buf.append(toKeyFieldIDList.get(i));
			buf.append("=");
			for (int j = 0; j < dialog_.getAddRowListColumnList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getAddRowListColumnList().get(j).getDataSourceName())) {
					buf.append(XFUtility.getTableOperationValue(dialog_.getAddRowListColumnList().get(j).getBasicType(),
							dialog_.getAddRowListColumnList().get(j).getInternalValue(), dbName)) ;
					if (!dialog_.getAddRowListColumnList().get(j).getInternalValue().equals("")) {
						validWhereKeys = true;
					}
					break;
				}
			}
			count++;
		}

		if (validWhereKeys) {
			return buf.toString();
		} else {
			return "";
		}
	}

	public String getTableID(){
		return tableID;
	}

	public String getTableAlias(){
		return tableAlias;
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

	public boolean isKeyNull() {
		boolean isKeyNull = false;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getAddRowListColumnList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getAddRowListColumnList().get(j).getTableAlias() + "." + dialog_.getAddRowListColumnList().get(j).getFieldID())) {
					if (dialog_.getAddRowListColumnList().get(j).isNull()) {
						isKeyNull = true;
						break;
					}
				}
			}
		}
		return isKeyNull;
	}

	public void setKeyFieldValues(XFHashMap keyValues){
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getAddRowListColumnList().size(); j++) {
				if (dialog_.getAddRowListColumnList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))) {
					dialog_.getAddRowListColumnList().get(j).setValue(keyValues.getValue(withKeyFieldIDList.get(i)));
					break;
				}
			}
		}
	}
}

class XF310_AddRowListNumber extends Object {
	private int number_;
	private HashMap<String, Object> columnMap_ = new HashMap<String, Object>();
	private HashMap<String, Object> returnFieldMap_ = new HashMap<String, Object>();
	private ArrayList<TableCellReadOnly> cellObjectList_;
	private boolean isSelected = false;
	public XF310_AddRowListNumber(int num, HashMap<String, Object> columnMap, HashMap<String, Object> returnFieldMap, ArrayList<TableCellReadOnly> cellObjectList) {
		number_ = num;
		columnMap_ = columnMap;
		returnFieldMap_ = returnFieldMap;
		cellObjectList_ = cellObjectList;
	}
	public HashMap<String, Object> getColumnMap() {
		return columnMap_;
	}
	public HashMap<String, Object> getReturnFieldMap() {
		return returnFieldMap_;
	}
	public void setNumber(int num) {
		number_ = num;
	}
	public ArrayList<TableCellReadOnly> getCellObjectList() {
		return cellObjectList_;
	}
	public String getRowNumberString() {
		return Integer.toString(number_);
	}
	public void setSelected(boolean selected) {
		isSelected = selected;
	}
	public boolean isSelected() {
		return isSelected;
	}
}

class XF310_AddRowListFunctionButton_actionAdapter implements java.awt.event.ActionListener {
	XF310_AddRowList adaptee;
	XF310_AddRowListFunctionButton_actionAdapter(XF310_AddRowList adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jFunctionButton_actionPerformed(e);
	}
}