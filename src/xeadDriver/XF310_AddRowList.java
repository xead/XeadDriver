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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
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
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.w3c.dom.NodeList;

class XF310_AddRowList extends JDialog implements XFScriptable {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private JPanel jPanelMain = new JPanel();
	private JScrollPane jScrollPaneTable = new JScrollPane();
	private TableModelMain tableModelMain;
	private JTable jTableMain = new JTable();
	private DefaultTableCellRenderer rendererTableHeader = null;
	private XF310_AddRowListTable addRowListTable;
	private ArrayList<XF310_AddRowListColumn> addRowListColumnList = new ArrayList<XF310_AddRowListColumn>();
	private ArrayList<XF310_AddRowListReferTable> addRowListReferTableList = new ArrayList<XF310_AddRowListReferTable>();
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
	private Bindings engineScriptBindings;
	private int result;
	private final int FONT_SIZE = 14;
	private final int ROW_HEIGHT = 18;
	private boolean isInvalid = false;
	private boolean isWithoutButtonToAddBlank;
	private boolean isWithoutButtonToCallFunction;
	private boolean isFirstTimeToSelect = true;
	private ArrayList<XF310_AddRowListNumber> addRowListNumberReturnList = new ArrayList<XF310_AddRowListNumber>();
	private Action helpAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			dialog_.getSession().browseHelp();
		}
	};
	private ResultSet resultOfDetailTable = null;
	private Statement statementForDetailTable = null;
	private ResultSet resultOfReferTable = null;
	private Statement statementForReferTable = null;
	
	public XF310_AddRowList(XF310 dialog) throws Exception {
		super(dialog, "", true);
		//
		dialog_ = dialog;
		//
		jPanelMain.setLayout(new BorderLayout());
		jPanelTop.setLayout(new BorderLayout());
		jTableMain.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTableMain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTableMain.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableMain.setRowHeight(ROW_HEIGHT);
		jTableMain.setRowSelectionAllowed(true);
		jTableMain.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !addSelectedActionName.equals("")) {
					int row = jTableMain.getSelectedRow();
					if (row > -1) {
						boolean checked = (Boolean)tableModelMain.getValueAt(row, 1);
						tableModelMain.setValueAt(!checked, row, 1);
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ENTER && tableModelMain.getRowCount() > 0 && addSelectedActionName.equals("")) {
					processRow();
				}
			}
		});
		jTableMain.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2 && tableModelMain.getRowCount() > 0) {
					processRow();
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
		jTableMain.setAutoCreateRowSorter(true);
		JTableHeader header = new JTableHeader(jTableMain.getColumnModel()) {
			private static final long serialVersionUID = 1L;
			public String getToolTipText(MouseEvent e) {
				String text = "";
				int c = columnAtPoint(e.getPoint());
				for (int i = 0; i < addRowListColumnList.size(); i++) {
					if (addRowListColumnList.get(i).isVisibleOnPanel()) {
						if (addRowListColumnList.get(i).getColumnIndex() == c) {
							if (addRowListColumnList.get(i).getDecimalSize() > 0) {
								text = "<html>" + addRowListColumnList.get(i).getFieldName() + " " + addRowListColumnList.get(i).getDataSourceName() + " (" + addRowListColumnList.get(i).getDataSize() + "," + addRowListColumnList.get(i).getDecimalSize() + ")<br>" + addRowListColumnList.get(i).getFieldRemarks();
							} else {
								text = "<html>" + addRowListColumnList.get(i).getFieldName() + " " + addRowListColumnList.get(i).getDataSourceName() + " (" + addRowListColumnList.get(i).getDataSize() + ")<br>" + addRowListColumnList.get(i).getFieldRemarks();
							}
							break;
						}
					}
				}
				return text;
			}
		};
		jTableMain.setTableHeader(header);
		jTableMain.getTableHeader().setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		rendererTableHeader = (DefaultTableCellRenderer)jTableMain.getTableHeader().getDefaultRenderer();
		rendererTableHeader.setHorizontalAlignment(SwingConstants.LEFT);
		jScrollPaneTable.getViewport().add(jTableMain, null);
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jTextAreaMessages.setWrapStyleWord(true);
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
		jPanelButtons.setLayout(gridLayoutButtons);
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
		jPanelTop.add(jScrollPaneTable, BorderLayout.CENTER);
		jPanelTop.add(jScrollPaneMessages, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		gridLayoutButtons.setColumns(6);
		gridLayoutButtons.setRows(1);
		gridLayoutButtons.setHgap(2);
		//
		for (int i = 0; i < 6; i++) {
			jButtonArray[i] = new JButton();
			jButtonArray[i].setBounds(new Rectangle(0, 0, 90, 30));
			jButtonArray[i].setFocusable(false);
			jButtonArray[i].addActionListener(new XF310_AddRowListFunctionButton_actionAdapter(this));
			jPanelButtonArray[i] = new JPanel();
			jPanelButtonArray[i].setLayout(new BorderLayout());
			jPanelButtonArray[i].add(jButtonArray[i], BorderLayout.CENTER);
			actionButtonArray[i] = new ButtonAction(jButtonArray[i]);
			jPanelButtons.add(jPanelButtonArray[i]);
		}
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
		setupFunctionKeysAndButtons();
		//
		//////////////////////////////////////
		// Setup Script Engine and Bindings //
		//////////////////////////////////////
		scriptEngine = dialog_.getScriptEngine();
		engineScriptBindings = scriptEngine.createBindings();
		engineScriptBindings.put("instance", (XFScriptable)this);
		//
		StringTokenizer workTokenizer;
		int posX = 0;
		int posY = 0;
		String workStr, workAlias, workTableID, workFieldID;
		//
		this.setTitle(dialog_.getTitle() + " - " + dialog_.getAddRowListTitle());
		jLabelSessionID.setText(dialog_.getSession().getSessionID());
		jLabelFunctionID.setText(dialog_.getFunctionInfo());
		FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));
		//
		addRowListTable = new XF310_AddRowListTable(dialog_.getFunctionElement(), this);
		tableModelMain = new TableModelMain();
		jTableMain.setModel(tableModelMain);
		addRowListColumnList.clear();
		addRowListReferTableList.clear();
		//
		// Setup List of add-row-list refer tables //
		addRowListReferTableList.clear();
		NodeList addRowListReferElementList = addRowListTable.getTableElement().getElementsByTagName("Refer");
		SortableDomElementListModel sortingList = XFUtility.getSortedListModel(addRowListReferElementList, "Order");
		for (int j = 0; j < sortingList.getSize(); j++) {
			addRowListReferTableList.add(new XF310_AddRowListReferTable((org.w3c.dom.Element)sortingList.getElementAt(j), this));
		}
		//
		// Setup add-row-list columns //
		tableModelMain.addColumn("NO."); //column index:0 //
		int columnIndex = 0;
		if (!addSelectedActionName.equals("")) {
			tableModelMain.addColumn(""); //column index:1 //
			columnIndex = 1;
		}
		NodeList columnFieldList = dialog_.getFunctionElement().getElementsByTagName("AddRowListColumn");
		sortingList = XFUtility.getSortedListModel(columnFieldList, "Order");
		for (int j = 0; j < sortingList.getSize(); j++) {
			addRowListColumnList.add(new XF310_AddRowListColumn((org.w3c.dom.Element)sortingList.getElementAt(j), this));
			if (addRowListColumnList.get(j).isVisibleOnPanel()) {
				columnIndex++;
				addRowListColumnList.get(j).setColumnIndex(columnIndex);
				tableModelMain.addColumn(addRowListColumnList.get(j).getCaption());
			}
		}
		//
		// Set attributes to each column //
		TableColumn column = jTableMain.getColumnModel().getColumn(0);
		column.setPreferredWidth(38);
		if (!addSelectedActionName.equals("")) {
			column.setCellRenderer(new RowNoRenderer());
			column = jTableMain.getColumnModel().getColumn(1);
			column.setPreferredWidth(22);
			column.setCellRenderer(new CheckBoxRenderer());
			column.setCellEditor(new DefaultCellEditor(new JCheckBox()));
			column.setHeaderRenderer(new CheckBoxHeaderRenderer(new CheckBoxHeaderListener()));
		}
		for (int j = 0; j < addRowListColumnList.size(); j++) {
			if (addRowListColumnList.get(j).isVisibleOnPanel()) {
				column = jTableMain.getColumnModel().getColumn(addRowListColumnList.get(j).getColumnIndex());
				column.setPreferredWidth(addRowListColumnList.get(j).getWidth());
				//column.setCellRenderer(new DefaultRenderer());
				column.setCellRenderer(addRowListColumnList.get(j).getCellRenderer());
				column.setHeaderRenderer(addRowListColumnList.get(j).getHeaderRenderer());
			}
		}
		//
		// Add key fields as HIDDEN column if they are not on add-row-list column list //
		for (int j = 0; j < addRowListTable.getKeyFieldIDList().size(); j++) {
			if (!containsAddRowListField(addRowListTable.getTableID(), "", addRowListTable.getKeyFieldIDList().get(j))) {
				addRowListColumnList.add(new XF310_AddRowListColumn(addRowListTable.getTableID(), "", addRowListTable.getKeyFieldIDList().get(j), this));
			}
		}
		//
		// Add with-fields as HIDDEN column if they are not on add-row-list column list //
		for (int j = 0; j < addRowListTable.getWithFieldIDList().size(); j++) {
			if (!containsAddRowListField(addRowListTable.getTableID(), "", addRowListTable.getWithFieldIDList().get(j))) {
				addRowListColumnList.add(new XF310_AddRowListColumn(addRowListTable.getTableID(), "", addRowListTable.getWithFieldIDList().get(j), this));
			}
		}
		//
		// Add return fields as HIDDEN column if they are not on add-row-list column list //
		for (int j = 0; j < addRowListTable.getReturnDataSourceList().size(); j++) {
			workTokenizer = new StringTokenizer(addRowListTable.getReturnDataSourceList().get(j), "." );
			workAlias = workTokenizer.nextToken();
			workTableID = dialog_.getTableIDOfTableAlias(workAlias);
			workFieldID = workTokenizer.nextToken();
			if (!containsAddRowListField(workTableID, workAlias, workFieldID)) {
				addRowListColumnList.add(new XF310_AddRowListColumn(workTableID, workAlias, workFieldID, this));
			}
		}
		//
		// Add OrderBy fields as HIDDEN column if they are not on add-row-list column list //
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
		//
		// Analyze fields in script and add them as HIDDEN columns if they are not on add-row-list column list //
		org.w3c.dom.Element workElement;
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
							String msg = res.getString("FunctionError1") + addRowListTable.getTableID() + res.getString("FunctionError2") + addRowListTable.getScriptList().get(j).getName() + res.getString("FunctionError3") + workAlias + "_" + workFieldID + res.getString("FunctionError4");
							JOptionPane.showMessageDialog(this, msg);
							throw new Exception();
						} else {
							addRowListColumnList.add(new XF310_AddRowListColumn(workTableID, workAlias, workFieldID, this));
						}
					}
				}
			}
		}
		//
		// Analyze detail refer tables and add their fields as HIDDEN columns if necessary //
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
		//
		// Add common used field (except for keys) to return field list of add-row-list table //
		addRowListTable.addCommonUsedColumnToReturnList();
		//
        Rectangle screenRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int width = screenRect.width - 150;
		int height = screenRect.height - 150;
		this.setPreferredSize(new Dimension(width, height));
		posX = (screenRect.width - width) / 2;
		posY = (screenRect.height - height) / 2;
		this.setLocation(posX, posY);
		this.pack();
	}
	
	//public int requestSelection() throws Exception, SQLException, ScriptException {
	public int requestSelection() {
		try {
		//
		if (isFirstTimeToSelect) {
			selectDetailRecordsAndSetupTableRows();
			isFirstTimeToSelect = false;
		}
		//
		result = 0;
		addRowListNumberReturnList = new ArrayList<XF310_AddRowListNumber>();
		//
		if (tableModelMain.getRowCount() == 0) {
			jTextAreaMessages.setText(res.getString("FunctionMessage49") + addRowListTable.getTableElement().getAttribute("Name") + res.getString("FunctionMessage50"));
		} else {
			if (dialog_.getFunctionElement().getAttribute("AddRowListInitialMsg").equals("")) {
				if (addSelectedActionName.equals("")) {
					jTextAreaMessages.setText(res.getString("FunctionMessage32"));
				} else {
					jTextAreaMessages.setText(res.getString("FunctionMessage5") + addSelectedActionName + res.getString("FunctionMessage6"));
				}
			} else {
				jTextAreaMessages.setText(dialog_.getFunctionElement().getAttribute("AddRowListInitialMsg"));
			}
		}
		//
		if (!addSelectedActionName.equals("")) {
			for (int i = 0; i < tableModelMain.getRowCount(); i++) {
				tableModelMain.setValueAt(false, i, 1);
			}
		}
		jTableMain.requestFocus();
		//
		if (tableModelMain.getRowCount() > 0) {
			jTableMain.setRowSelectionInterval(0, 0);
		}
		//
		this.setVisible(true);
		//
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (resultOfDetailTable != null) {
					resultOfDetailTable.close();
				}
				if (resultOfReferTable != null) {
					resultOfReferTable.close();
				}
				if (statementForDetailTable != null) {
					statementForDetailTable.close();
				}
				if (statementForReferTable != null) {
					statementForReferTable.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//
		return result;
	}
	
	void selectDetailRecordsAndSetupTableRows() throws Exception, SQLException, ScriptException {
		int columnIndex;
		String workStr;
		HashMap<String, Object> columnValueMap, returnFieldMap;
		statementForDetailTable = dialog_.getSession().getConnection().createStatement();
		//
		int rowCount = tableModelMain.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			tableModelMain.removeRow(0);
		}
		//
		ArrayList<Object> orderByValueList = new ArrayList<Object>();
		ArrayList<WorkingRow> tableRowList = new ArrayList<WorkingRow>();
		int countOfRows = 0;
		int workIndex;
		//
		String sql = addRowListTable.getSQLToSelect();
		dialog_.setProcessLog(sql);
		resultOfDetailTable = statementForDetailTable.executeQuery(sql);
		while (resultOfDetailTable.next()) {
			if (addRowListTable.isRecordToBeSelected(resultOfDetailTable)) {
				//
				addRowListTable.runScript("BR", "", null); /* Script to be run BEFORE READ */
				//
				for (int i = 0; i < addRowListColumnList.size(); i++) {
					addRowListColumnList.get(i).initialize();
					if (addRowListColumnList.get(i).getTableID().equals(addRowListTable.getTableID())) {
						addRowListColumnList.get(i).setValueOfResultSet(resultOfDetailTable);
					}
				}
				//
				columnValueMap = new HashMap<String, Object>();
				for (int i = 0; i < addRowListColumnList.size(); i++) {
					columnValueMap.put(addRowListColumnList.get(i).getDataSourceName(), addRowListColumnList.get(i).getInternalValue());
				}
				//
				fetchAddRowListReferRecords("AR", "", columnValueMap);
				//
				returnFieldMap = new HashMap<String, Object>();
				for (int i = 0; i < addRowListColumnList.size(); i++) {
					workIndex = addRowListTable.getReturnDataSourceList().indexOf(addRowListColumnList.get(i).getDataSourceName());
					if (workIndex > -1) {
						returnFieldMap.put(addRowListTable.getReturnToDetailDataSourceList().get(workIndex), addRowListColumnList.get(i).getInternalValue());
					}
				}
				//
				if (addRowListTable.isOrderByInSelectSQL()) {
					Object[] columnCell = new Object[addRowListColumnList.size() + 2];
					columnCell[0] = new XF310_AddRowListNumber(countOfRows + 1, columnValueMap, returnFieldMap);
					if (!addSelectedActionName.equals("")) {
						columnCell[1] = false;
					}
					for (int i = 0; i < addRowListColumnList.size(); i++) {
						if (addRowListColumnList.get(i).isVisibleOnPanel()) {
							columnIndex = addRowListColumnList.get(i).getColumnIndex();
							columnCell[columnIndex] = new XF310_AddRowListCell(addRowListColumnList.get(i), (XF310_AddRowListNumber)columnCell[0]);
						}
					}
					tableModelMain.addRow(columnCell);
				} else {
					orderByValueList = new ArrayList<Object>();
					for (int i = 0; i < addRowListTable.getOrderByFieldIDList().size(); i++) {
						workStr = addRowListTable.getOrderByFieldIDList().get(i).replace("(D)", "");
						workStr = workStr.replace("(A)", "");
						for (int j = 0; j < addRowListColumnList.size(); j++) {
							if (addRowListColumnList.get(j).getDataSourceName().equals(workStr)) {
								orderByValueList.add(addRowListColumnList.get(j).getExternalValue());
								break;
							}
						}
					}
					tableRowList.add(new WorkingRow(columnValueMap, returnFieldMap, orderByValueList));
				}
				//
				countOfRows++;
			}
		}
		//
		if (!addRowListTable.isOrderByInSelectSQL()) {
			WorkingRow[] workingRowArray = tableRowList.toArray(new WorkingRow[0]);
			Arrays.sort(workingRowArray, new WorkingRowComparator());
			for (int i = 0; i < workingRowArray.length; i++) {
				Object[] Cell = new Object[addRowListColumnList.size() + 2];
				Cell[0] = new XF310_AddRowListNumber(i + 1, workingRowArray[i].getColumnMap(), workingRowArray[i].getReturnFieldMap());
				if (!addSelectedActionName.equals("")) {
					Cell[1] = false;
				}
				for (int j = 0; j < addRowListColumnList.size(); j++) {
					if (addRowListColumnList.get(j).isVisibleOnPanel()) {
						columnIndex = addRowListColumnList.get(j).getColumnIndex(); 
						Cell[columnIndex] = new XF310_AddRowListCell(addRowListColumnList.get(j), (XF310_AddRowListNumber)Cell[0]);
					}
				}
				tableModelMain.addRow(Cell);
			}
		}
	}

	boolean isInvalid() {
		return isInvalid;
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

	public HashMap<String, Object> getParmMap() {
		return dialog_.getParmMap();
	}
	
	public void setProcessLog(String text) {
		dialog_.setProcessLog(text);
	}

	public HashMap<String, Object> getReturnMap() {
		return dialog_.getReturnMap();
	}

	public void callFunction(String functionID) {
		try {
			dialog_.setReturnMap(XFUtility.callFunction(dialog_.getSession(), functionID, dialog_.getParmMap()));
		} catch (Exception e) {
			String message = res.getString("FunctionError9") + functionID + res.getString("FunctionError10");
			JOptionPane.showMessageDialog(null,message);
			dialog_.setExceptionHeader(message);
			setErrorAndCloseFunction();
		}
	}

	void setupFunctionKeysAndButtons() {
		//
		InputMap inputMap  = jPanelMain.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.clear();
		ActionMap actionMap = jPanelMain.getActionMap();
		actionMap.clear();
		//
		for (int i = 0; i < 6; i++) {
			jButtonArray[i].setText("");
			jButtonArray[i].setVisible(false);
			actionDefinitionArray[i] = "";
		}
		//
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMap.put("HELP", helpAction);
		//
		isWithoutButtonToAddBlank = true;
		isWithoutButtonToCallFunction = true;
		//
		int workIndex;
		org.w3c.dom.Element element;
		NodeList buttonList = dialog_.getFunctionElement().getElementsByTagName("AddRowListButton");
		for (int i = 0; i < buttonList.getLength(); i++) {
			element = (org.w3c.dom.Element)buttonList.item(i);
			//
			workIndex = Integer.parseInt(element.getAttribute("Position"));
			actionDefinitionArray[workIndex] = element.getAttribute("Action");
			XFUtility.setCaptionToButton(jButtonArray[workIndex], element, "");
			jButtonArray[workIndex].setVisible(true);
			inputMap.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
			actionMap.put("actionButton" + workIndex, actionButtonArray[workIndex]);
			//
			if (element.getAttribute("Action").equals("ADD_SELECTED")) {
				addSelectedActionName = element.getAttribute("Caption");
			}
			//
			if (element.getAttribute("Action").equals("ADD_BLANK")) {
				isWithoutButtonToAddBlank = false;
			}
			//
			if (element.getAttribute("Action").contains("CALL(")) {
				isWithoutButtonToCallFunction = false;
			}
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
		//
		if (tableModelMain.getRowCount() == 1 && isWithoutButtonToAddBlank && isWithoutButtonToCallFunction) {
			defaultRow = new ArrayList<XF310_AddRowListNumber>();
			XF310_AddRowListNumber rowNumber = (XF310_AddRowListNumber)tableModelMain.getValueAt(0, 0);
			defaultRow.add(rowNumber);
		}
		//
		return defaultRow;
	}

	class CheckBoxHeaderListener implements ItemListener {   
		private CheckBoxHeaderRenderer rendererComponent_ = null;   
		public void setRenderer(CheckBoxHeaderRenderer rendererComponent) {
			rendererComponent_ = rendererComponent;
		}
		public void itemStateChanged(ItemEvent e) {
			if (rendererComponent_ != null) {
				if (rendererComponent_.isSelected()) {
					for (int i = 0; i < tableModelMain.getRowCount(); i++) {
						tableModelMain.setValueAt(true, i, 1);
					}
				} else {
					for (int i = 0; i < tableModelMain.getRowCount(); i++) {
						tableModelMain.setValueAt(false, i, 1);
					}
				}
			}
		}   
	}   
	
	class CheckBoxHeaderRenderer extends JCheckBox implements TableCellRenderer, MouseListener {   
		private static final long serialVersionUID = 1L;
		protected CheckBoxHeaderRenderer rendererComponent;   
		protected int column;   
		protected boolean mousePressed = false;   
		public CheckBoxHeaderRenderer(CheckBoxHeaderListener itemListener) {   
			rendererComponent = this;   
			rendererComponent.addItemListener(itemListener);   
			itemListener.setRenderer(this);
		}   
		public Component getTableCellRendererComponent(	JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {  
			if (table != null) {   
				JTableHeader header = table.getTableHeader();   
				if (header != null) {   
					header.addMouseListener(rendererComponent);   
				}   
			}   
			setColumn(column);   
			rendererComponent.setText("");   
			rendererComponent.setBackground(new Color(219,219,219));   
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));   
			return rendererComponent;   
		}   
		protected void setColumn(int column) {   
			this.column = column;   
		}   
		public int getColumn() {   
			return column;   
		}   
		protected void handleClickEvent(MouseEvent e) {   
			if (mousePressed) {   
				mousePressed=false;   
				JTableHeader header = (JTableHeader)(e.getSource());   
				JTable tableView = header.getTable();   
				TableColumnModel columnModel = tableView.getColumnModel();   
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());   
				int column = tableView.convertColumnIndexToModel(viewColumn);   
				if (viewColumn == this.column && e.getClickCount() == 1 && column != -1) {   
					doClick();   
				}   
			}   
		}   
		public void mouseClicked(MouseEvent e) {   
			handleClickEvent(e);   
			((JTableHeader)e.getSource()).repaint();   
		}   
		public void mousePressed(MouseEvent e) {   
			mousePressed = true;   
		}   
		public void mouseReleased(MouseEvent e) {   
		}   
		public void mouseEntered(MouseEvent e) {   
		}   
		public void mouseExited(MouseEvent e) {   
		}   
	}  

	public class CheckBoxRenderer extends JCheckBox implements TableCellRenderer {
		private static final long serialVersionUID = 1L;
		private Color oddRowColor = new Color(240, 240, 255);
		public CheckBoxRenderer() {
			super();
		}
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int	row, int column) {
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				if (row%2==0) {
					setBackground(table.getBackground());
				} else {
					setBackground(oddRowColor);
				}
				setForeground(table.getForeground());
			}
			setFocusable(false);
			boolean isChecked = ((Boolean) value).booleanValue();
			setSelected(isChecked);
			return this;
		} 
	} 
	
	private void fetchAddRowListReferRecords(String event, String specificReferTable, HashMap<String, Object> columnValueMap) throws SQLException, ScriptException {
		String sql;
		statementForReferTable = dialog_.getSession().getConnection().createStatement();
		//
		addRowListTable.runScript(event, "BR()", columnValueMap); /* Script to be run AFTER READ primary table BEFORE READ any join tables */
		//
		for (int i = 0; i < addRowListReferTableList.size(); i++) {
			if (specificReferTable.equals("") || specificReferTable.equals(addRowListReferTableList.get(i).getTableAlias())) {
				//
				if (addRowListReferTableList.get(i).isToBeExecuted()) {
					//
					addRowListTable.runScript(event, "BR(" + addRowListReferTableList.get(i).getTableAlias() + ")", columnValueMap); /* Script to be run BEFORE READ */
					//
					if (!addRowListReferTableList.get(i).isKeyNull()) {
						//
						sql = addRowListReferTableList.get(i).getSelectSQL();
						if (!sql.equals("")) {
							dialog_.setProcessLog(sql);
							resultOfReferTable = statementForReferTable.executeQuery(sql);
							while (resultOfReferTable.next()) {
								//
								if (addRowListReferTableList.get(i).isRecordToBeSelected(resultOfReferTable)) {
									//
									for (int j = 0; j < addRowListColumnList.size(); j++) {
										if (addRowListColumnList.get(j).getTableAlias().equals(addRowListReferTableList.get(i).getTableAlias())) {
											addRowListColumnList.get(j).setValueOfResultSet(resultOfReferTable);
											columnValueMap.put(addRowListColumnList.get(j).getDataSourceName(), addRowListColumnList.get(j).getInternalValue());
										}
									}
									//
									addRowListTable.runScript(event, "AR(" + addRowListReferTableList.get(i).getTableAlias() + ")", columnValueMap); /* Script to be run AFTER READ */
									//
								}
							}
						}
					}
				}
			}
		}
		//
		addRowListTable.runScript(event, "AR()", columnValueMap); /* Script to be run AFTER READ all join tables */
	}

	class DefaultRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private Color oddRowColor = new Color(240, 240, 255);
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
			XF310_AddRowListCell cell = (XF310_AddRowListCell)value;
			//
			setText((String)cell.getExternalValue());
			setFont(new java.awt.Font("SansSerif", 0, 14));
			//
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				if (row%2==0) {
					setBackground(table.getBackground());
				} else {
					setBackground(oddRowColor);
				}
				setForeground(cell.getForeground());
			}
			//
			setFocusable(false);
			//
			if (cell.getDetailColumn().getBasicType().equals("INTEGER") || cell.getDetailColumn().getBasicType().equals("FLOAT")) {
				setHorizontalAlignment(SwingConstants.RIGHT);
			}
			//
			validate();
			//
			return this;
		}
	}

	class RowNoRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private Color oddRowColor = new Color(240, 240, 255);
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
			XF310_AddRowListNumber rowNumber = (XF310_AddRowListNumber)value;
			//
			setText(rowNumber.toString());
			setFont(new java.awt.Font("SansSerif", 0, 14));
			//
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				if (row%2==0) {
					setBackground(table.getBackground());
				} else {
					setBackground(oddRowColor);
				}
				setForeground(table.getForeground());
			}
			//
			setFocusable(false);
			setHorizontalAlignment(SwingConstants.CENTER);
			//
			validate();
			return this;
		}
	}

	public boolean containsAddRowListField(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < addRowListColumnList.size(); i++) {
			if (tableID.equals("")) {
				if (addRowListColumnList.get(i).getTableAlias().equals(tableAlias)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (addRowListColumnList.get(i).getTableID().equals(tableID) && addRowListColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (addRowListColumnList.get(i).getTableID().equals(tableID) && addRowListColumnList.get(i).getTableAlias().equals(tableAlias) && addRowListColumnList.get(i).getFieldID().equals(fieldID)) {
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

	public Bindings getEngineScriptBindings() {
		return engineScriptBindings;
	}
	
	public void evalScript(String name, String text) throws ScriptException {
		dialog_.evalScript(name, text, engineScriptBindings);
	}
	
	public String getTableIDOfTableAlias(String alias) {
		return dialog_.getTableIDOfTableAlias(alias);
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
			//
			jTextAreaMessages.setText("");
			//
			if (action.equals("EXIT")) {
				doExitAction();
			}
			//
			if (action.equals("ADD_SELECTED")) {
				XF310_AddRowListNumber rowNumber;
				boolean checked;
				for (int i = 0; i < tableModelMain.getRowCount(); i++) {
					checked = (Boolean)tableModelMain.getValueAt(i, 1);
					if (checked) {
						rowNumber = (XF310_AddRowListNumber)tableModelMain.getValueAt(i, 0);
						addRowListNumberReturnList.add(rowNumber);
					}
				}
				result = 1;
				this.setVisible(false);
			}
			//
			if (action.equals("ADD_BLANK")) {
				addRowListNumberReturnList.clear();
				result = 2;
				this.setVisible(false);
			}
			//
			if (action.contains("CALL")) {
				doCallAction(action);
				selectDetailRecordsAndSetupTableRows();
			}
			//
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (resultOfDetailTable != null) {
					resultOfDetailTable.close();
				}
				if (resultOfReferTable != null) {
					resultOfReferTable.close();
				}
				if (statementForDetailTable != null) {
					statementForDetailTable.close();
				}
				if (statementForReferTable != null) {
					statementForReferTable.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			//
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
			//
			org.w3c.dom.Element workElement1, elementOfFunction = null;
			NodeList functionList = dialog_.getSession().getFunctionList();
			for (int k = 0; k < functionList.getLength(); k++) {
				workElement1 = (org.w3c.dom.Element)functionList.item(k);
				if (workElement1.getAttribute("ID").equals(functionID)) {
					elementOfFunction = workElement1;
					break;
				}
			}
			//
			if (elementOfFunction == null) {
				jTextAreaMessages.setText("");
				jTextAreaMessages.setText(res.getString("FunctionError17") + functionID + res.getString("FunctionError18"));
			} else {
				jTextAreaMessages.setText("");
				HashMap<String, Object> returnMap = dialog_.getSession().getFunction().execute(elementOfFunction, addRowListTable.getKeyMap());
				if (returnMap.get("RETURN_MESSAGE") == null) {
					jTextAreaMessages.setText(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
				} else {
					jTextAreaMessages.setText(returnMap.get("RETURN_MESSAGE").toString());
				}
			}
		}
	}
	
	class TableModelMain extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		public boolean isCellEditable(int row, int col) {
			return col == 1;
		}
	}

	class WorkingRow extends Object {
		private HashMap<String, Object> columnMap_ = new HashMap<String, Object>();
		private HashMap<String, Object> returnFieldMap_ = new HashMap<String, Object>();
		private ArrayList<Object> orderByValueList_ = new ArrayList<Object>();
		//
		public WorkingRow(HashMap<String, Object> columnMap, HashMap<String, Object> returnFieldMap, ArrayList<Object> orderByValueList) {
			columnMap_ = columnMap;
			returnFieldMap_ = returnFieldMap;
			orderByValueList_ = orderByValueList;
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
	}

	class WorkingRowComparator implements java.util.Comparator<WorkingRow>{
		public int compare(WorkingRow row1, WorkingRow row2){
			int compareResult = 0;
			for (int i = 0; i < row1.getOrderByValueList().size(); i++) {
				compareResult = row1.getOrderByValueList().get(i).toString().compareTo(row2.getOrderByValueList().get(i).toString());
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
}

class XF310_AddRowListTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID_ = "";
	private String activeWhere = "";
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
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private String rangeKeyFieldSearch = "";
	private Object valueKey = null;
	private Object valueFrom = null;
	private Object valueThru = null;
	private ArrayList<Object> upperKeyGroupValueList = new ArrayList<Object>();

	public XF310_AddRowListTable(org.w3c.dom.Element functionElement, XF310_AddRowList dialog){
		super();
		//
		functionElement_ = functionElement;
		dialog_ = dialog;
		//
		tableID_ = functionElement_.getAttribute("AddRowListTable");
		tableElement = dialog_.getSession().getTableElement(tableID_);
		activeWhere = tableElement.getAttribute("ActiveWhere");
		//
		int pos1;
		String wrkStr1, wrkStr2;
		org.w3c.dom.Element workElement;
		//
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
		//
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("AddRowListWithFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withFieldIDList.add(workTokenizer.nextToken());
		}
		//
		rangeKeyType = 0;
		String wrkStr = tableElement.getAttribute("RangeKey");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			rangeKeyFieldValid =workTokenizer.nextToken();
			rangeKeyFieldExpire =workTokenizer.nextToken();
			workElement = dialog_.getSession().getFieldElement(tableID_, rangeKeyFieldExpire);
			if (withFieldIDList.contains(rangeKeyFieldValid)) {
				if (XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
					rangeKeyType = 1;
				} else {
					rangeKeyType = 2;
				}
			}
		}
		//
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("AddRowListWithHeaderFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withHeaderDataSourceList.add(workTokenizer.nextToken());
		}
		//
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("AddRowListReturnDataSources"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			returnDataSourceList.add(workTokenizer.nextToken());
		}
		//
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("AddRowListReturnToDetailDataSources"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			returnToDetailDataSourceList.add(workTokenizer.nextToken());
		}
		//
		additionalWhere = functionElement_.getAttribute("AddRowListWhere");
		//
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("AddRowListOrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			wrkStr1 = workTokenizer.nextToken();
			//
			pos1 = wrkStr1.indexOf(".");
			if (pos1 > -1) { 
				wrkStr2 = wrkStr1.substring(0, pos1);
				if (!wrkStr2.equals(tableID_)) {
					isOrderByInSelectSQL = false;
				}
			}
			//
			orderByFieldIDList.add(wrkStr1);
		}
		if (rangeKeyType != 0 && orderByFieldIDList.size() > 0) {
			isOrderByInSelectSQL = false;
		}
		//
		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID_, element));
		}
	}
	
	public String getSQLToSelect(){
		int count;
		XF310_HeaderField headerField;
		StringBuffer buf = new StringBuffer();
		String workAlias, workTableID, workFieldID;
		org.w3c.dom.Element workElement;
		//
		buf.append("select ");
		//
		count = -1;
		for (int i = 0; i < keyFieldIDList.size(); i++) {
			count++;
			if (count > 0) {
				buf.append(",");
			}
			buf.append(keyFieldIDList.get(i));
		}
		for (int i = 0; i < withFieldIDList.size(); i++) {
			count++;
			if (count > 0) {
				buf.append(",");
			}
			buf.append(withFieldIDList.get(i));
		}
		for (int i = 0; i < dialog_.getAddRowListColumnList().size(); i++) {
			if (dialog_.getAddRowListColumnList().get(i).getTableID().equals(tableID_) && !dialog_.getAddRowListColumnList().get(i).isVirtualField()) {
				count++;
				if (count > 0) {
					buf.append(",");
				}
				buf.append(dialog_.getAddRowListColumnList().get(i).getFieldID());
			}
		}
		if (rangeKeyType != 0) {
			workElement = dialog_.getSession().getFieldElement(tableID_, rangeKeyFieldExpire);
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				count++;
				if (count > 0) {
					buf.append(",");
				}
				buf.append(rangeKeyFieldExpire);
			}
		}
		//
		buf.append(" from ");
		buf.append(tableID_);
		//
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < withFieldIDList.size(); i++) {
			if (withFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				rangeKeyFieldSearch = withHeaderDataSourceList.get(i);
			} else {
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
				if (XFUtility.isLiteralRequiredBasicType(headerField.getBasicType())) {
					buf.append("'");
					buf.append(headerField.getInternalValue());
					buf.append("'");
				} else {
					buf.append(headerField.getInternalValue());
				}
			}
		}
		//
		if (!activeWhere.equals("")) {
			buf.append(" and (");
			buf.append(activeWhere);
			buf.append(") ");
		}
		//
		if (!additionalWhere.equals("")) {
			buf.append(" and (");
			buf.append(additionalWhere);
			buf.append(") ");
		}
		//
		int pos0,pos1;
		count = -1;
		if (rangeKeyType == 0) {
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
		} else {
			buf.append(" order by ");
			for (int i = 0; i < keyFieldIDList.size(); i++) {
				count++;
				if (count > 0) {
					buf.append(",");
				}
				buf.append(keyFieldIDList.get(i));
				if (keyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
					buf.append(" DESC ");
				}
			}
		}
		//
		if (rangeKeyType != 0) {
			workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
			workAlias = workTokenizer.nextToken();
			workFieldID = workTokenizer.nextToken();
			valueKey = dialog_.getHeaderFieldObjectByID("", workAlias, workFieldID).getInternalValue();
			upperKeyGroupValueList.clear();
			for (int i = 0; i < keyFieldIDList.size(); i++) {
				upperKeyGroupValueList.add(i, null);
			}
		}
		//
		return buf.toString();
	}
	
	public boolean isRecordToBeSelected(ResultSet result){
		boolean returnValue = false;
		int comp1, comp2;
		//
		if (rangeKeyType == 0) {
			returnValue = true;
		}
		//
		//virtual selection//
		if (rangeKeyType == 1) {
			try {
				// Note that result set is ordered by rangeKeyFieldValue DESC //
				if (isNewUpperKeyGroup(result)) {
					valueFrom = result.getObject(rangeKeyFieldValid);
					comp1 = valueKey.toString().compareTo(valueFrom.toString());
					if (comp1 >= 0) {
						returnValue = true;
					}
				} else {
					valueFrom = result.getObject(rangeKeyFieldValid);
					comp1 = valueKey.toString().compareTo(valueFrom.toString());
					comp2 = valueKey.toString().compareTo(valueThru.toString());
					if (comp1 >= 0 && comp2 < 0) {
						returnValue = true;
					}
				}
				valueThru = valueFrom;
			} catch (SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
		//
		//physical selection//
		if (rangeKeyType == 2) {
			try {
				valueFrom = result.getObject(rangeKeyFieldValid);
				valueThru = result.getObject(rangeKeyFieldExpire);
				if (valueThru == null) {
					comp1 = valueKey.toString().compareTo(valueFrom.toString());
					if (comp1 >= 0) {
						returnValue = true;
					}
				} else {
					comp1 = valueKey.toString().compareTo(valueFrom.toString());
					comp2 = valueKey.toString().compareTo(valueThru.toString());
					if (comp1 >= 0 && comp2 < 0) {
						returnValue = true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
		//
		return returnValue;
	}

	boolean isNewUpperKeyGroup(ResultSet result){
		boolean returnValue = false;
		//
		try {
			for (int i = 0; i < keyFieldIDList.size(); i++) {
				if (!keyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
					if (!upperKeyGroupValueList.get(i).equals(result.getObject(keyFieldIDList.get(i)))) {
						returnValue = true;
						break;
					}
				}
				upperKeyGroupValueList.add(i, result.getObject(keyFieldIDList.get(i)));
			}
		} catch (SQLException e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
		//
		return returnValue;
	}
	
	public HashMap<String, Object> getKeyMap() {
		HashMap<String, Object> keyMap = new HashMap<String, Object>();
		String workAlias, workTableID, workFieldID;
		XF310_HeaderField headerField;
		//
		for (int i = 0; i < withFieldIDList.size(); i++) {
			workTokenizer = new StringTokenizer(withHeaderDataSourceList.get(i), "." );
			workAlias = workTokenizer.nextToken();
			workTableID = dialog_.getTableIDOfTableAlias(workAlias);
			workFieldID = workTokenizer.nextToken();
			headerField = dialog_.getHeaderFieldObjectByID(workTableID, workAlias, workFieldID);
			//
			keyMap.put(withFieldIDList.get(i), headerField.getInternalValue()) ;
		}
		//
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
		//
		return isValid;
	}

	public void runScript(String event1, String event2, HashMap<String, Object> columnValueMap) throws ScriptException  {
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
		for (int i = 0; i < validScriptList.size(); i++) {
			dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
		}
		//
		if (columnValueMap != null) {
			for (int i = 0; i < dialog_.getAddRowListColumnList().size(); i++) {
				columnValueMap.put(dialog_.getAddRowListColumnList().get(i).getDataSourceName(), dialog_.getAddRowListColumnList().get(i).getInternalValue());
			}
		}
	}
}

class XF310_AddRowListColumn extends Object implements XFScriptableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
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
	private int fieldWidth = 50;
	private int columnIndex = -1;
	private boolean isVisibleOnPanel = true;
	private boolean isVirtualField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private DecimalFormat integerFormat = new DecimalFormat("#,##0");
	private DecimalFormat floatFormat0 = new DecimalFormat("#,##0");
	private DecimalFormat floatFormat1 = new DecimalFormat("#,##0.0");
	private DecimalFormat floatFormat2 = new DecimalFormat("#,##0.00");
	private DecimalFormat floatFormat3 = new DecimalFormat("#,##0.000");
	private DecimalFormat floatFormat4 = new DecimalFormat("#,##0.0000");
	private DecimalFormat floatFormat5 = new DecimalFormat("#,##0.00000");
	private DecimalFormat floatFormat6 = new DecimalFormat("#,##0.000000");
	private Object value_ = null;
	private Color foreground = Color.black;

	public XF310_AddRowListColumn(org.w3c.dom.Element functionColumnElement, XF310_AddRowList dialog){
		super();
		//
		String wrkStr;
		functionColumnElement_ = functionColumnElement;
		dialog_ = dialog;
		fieldOptions = functionColumnElement_.getAttribute("FieldOptions");
		//
		StringTokenizer workTokenizer = new StringTokenizer(functionColumnElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		tableID = dialog.getTableIDOfTableAlias(tableAlias);
		fieldID =workTokenizer.nextToken();
		//
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID + "." + fieldID + res.getString("FunctionError11"));
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
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = wrkStr;
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		//if (dataSize > 50) {
		//	dataSize = 50;
		//}
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		//
		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID)) {
				isRangeKeyFieldExpire = true;
			}
		}
		//
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		JLabel jLabel = new JLabel();
		FontMetrics metrics = jLabel.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		int captionWidth = metrics.stringWidth(fieldCaption) + 18;
		//
		String basicType = this.getBasicType();
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			String wrk = "";
			Statement statement = null;
			ResultSet result = null;
			try {
				statement = dialog_.getSession().getConnection().createStatement();
				result = statement.executeQuery("select * from " + dialog_.getSession().getTableNameOfUserVariants() + " where IDUSERKUBUN = '" + wrkStr + "'");
				while (result.next()) {
					//
					kubunValueList.add(result.getString("KBUSERKUBUN").trim());
					wrk = result.getString("TXUSERKUBUN").trim();
					if (metrics.stringWidth(wrk) + 10 > fieldWidth) {
						fieldWidth = metrics.stringWidth(wrk) + 10;
					}
					kubunTextList.add(wrk);
				}
			} catch (SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			} finally {
				try {
					if (result != null) {
						result.close();
					}
					if (statement != null) {
						statement.close();
					}
				} catch (SQLException e) {
					e.printStackTrace(dialog_.getExceptionStream());
				}
			}
		} else {
			if (dataTypeOptionList.contains("KANJI")) {
				fieldWidth = dataSize * 14 + 5;
			} else {
				if (dataTypeOptionList.contains("FYEAR")) {
					fieldWidth = 85;
				} else {
					if (dataTypeOptionList.contains("YMONTH")) {
						fieldWidth = 85;
					} else {
						if (dataTypeOptionList.contains("MSEQ")) {
							fieldWidth = 50;
						} else {
							if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
								fieldWidth = XFUtility.getLengthOfEdittedNumericValue(dataSize, decimalSize, dataTypeOptionList.contains("ACCEPT_MINUS")) * 7 + 21;
							} else {
								if (basicType.equals("DATE")) {
									fieldWidth = XFUtility.getWidthOfDateValue(dialog_.getSession().getDateFormat());
								} else {
									fieldWidth = dataSize * 7 + 12;
								}
							}
						}
					}
				}
			}
		}
		//
		if (fieldWidth > 320) {
			fieldWidth = 320;
		}
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}
		//
		if (captionWidth > fieldWidth) {
			fieldWidth = captionWidth;
		}
		//
		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
	}

	public XF310_AddRowListColumn(String tableID, String tableAlias, String fieldID, XF310_AddRowList dialog){
		super();
		//
		functionColumnElement_ = null;
		dialog_ = dialog;
		fieldOptions = "";
		//
		isVisibleOnPanel = false;
		//
		this.tableID = tableID;
		if (tableAlias.equals("")) {
			this.tableAlias = tableID;
		} else {
			this.tableAlias = tableAlias;
		}
		this.fieldID = fieldID;
		//
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(this.tableID, this.fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, this.tableID + "." + this.fieldID + res.getString("FunctionError11"));
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
		//
		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			StringTokenizer workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID)) {
				isRangeKeyFieldExpire = true;
			}
		}
		//
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
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

	public String getTableID(){
		return tableID;
	}

	public String getFieldID(){
		return fieldID;
	}

	public String getFieldName(){
		return fieldName;
	}

	public String getFieldRemarks(){
		return fieldRemarks;
	}

	public String getFieldIDInScript(){
		return tableAlias + "_" + fieldID;
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
		//
		if (basicType.equals("INTEGER")) {
			if (value_ == null || value_.toString().equals("")) {
				value = "";
			} else {
				String wrkStr = value_.toString();
				int pos = wrkStr.indexOf(".");
				if (pos >= 0) {
					wrkStr = wrkStr.substring(0, pos);
				}
				value = integerFormat.format(Integer.parseInt(wrkStr));
			}
		} else {
			if (basicType.equals("FLOAT")) {
				if (value_ == null || value_.toString().equals("")) {
					value = "";
				} else {
					double doubleWrk = Double.parseDouble(value_.toString());
					if (decimalSize == 0) {
						value = floatFormat0.format(doubleWrk);
					}
					if (decimalSize == 1) {
						value = floatFormat1.format(doubleWrk);
					}
					if (decimalSize == 2) {
						value = floatFormat2.format(doubleWrk);
					}
					if (decimalSize == 3) {
						value = floatFormat3.format(doubleWrk);
					}
					if (decimalSize == 4) {
						value = floatFormat4.format(doubleWrk);
					}
					if (decimalSize == 5) {
						value = floatFormat5.format(doubleWrk);
					}
					if (decimalSize == 6) {
						value = floatFormat6.format(doubleWrk);
					}
				}
			} else {
				if (basicType.equals("DATE")) {
					if (value_ == null || value_.equals("")) {
						value = "";
					} else {
						value = XFUtility.getUserExpressionOfUtilDate(XFUtility.convertDateFromSqlToUtil(java.sql.Date.valueOf(value_.toString())), dialog_.getSession().getDateFormat(), false);
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
							if (value_ != null) {
								value = value_.toString().trim();
								//
								if (dataTypeOptionList.contains("YMONTH") || dataTypeOptionList.contains("FYEAR")) {
									String wrkStr = (String)value.toString();
									if (!wrkStr.equals("")) {
										value = XFUtility.getUserExpressionOfYearMonth(wrkStr, dialog_.getSession().getDateFormat());
									}
								}
								if (dataTypeOptionList.contains("MSEQ")) {
									String wrkStr = (String)value.toString();
									if (!wrkStr.equals("")) {
										value = XFUtility.getUserExpressionOfMSeq(Integer.parseInt(wrkStr), dialog_.getSession());
									}
								}
							}
						}
					}
				}
			}
		}
		//
		return value;
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

	public int getColumnIndex(){
		return columnIndex;
	}

	public void setColumnIndex(int index){
		columnIndex = index;
	}

	public XF310_AddRowListCellRenderer getCellRenderer(){
		XF310_AddRowListCellRenderer renderer = new XF310_AddRowListCellRenderer();
		if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
			if (this.getTypeOptionList().contains("MSEQ") || this.getTypeOptionList().contains("FYEAR")) {
				renderer.setHorizontalAlignment(SwingConstants.LEFT);
			} else {
				renderer.setHorizontalAlignment(SwingConstants.RIGHT);
			}
		} else {
			renderer.setHorizontalAlignment(SwingConstants.LEFT);
		}
		return renderer;
	}

	public HorizontalAlignmentHeaderRenderer getHeaderRenderer(){
		HorizontalAlignmentHeaderRenderer renderer = null;
		if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
			if (this.getTypeOptionList().contains("MSEQ") || this.getTypeOptionList().contains("FYEAR")) {
				renderer = new HorizontalAlignmentHeaderRenderer(SwingConstants.LEFT);
			} else {
				renderer = new HorizontalAlignmentHeaderRenderer(SwingConstants.RIGHT);
			}
		} else {
			renderer = new HorizontalAlignmentHeaderRenderer(SwingConstants.LEFT);
		}
		return renderer;
	}

	public void setValueOfResultSet(ResultSet result) throws SQLException{
		String basicType = this.getBasicType();
		this.setColor("");
		//
		if (this.isVirtualField) {
			if (this.isRangeKeyFieldExpire()) {
				value_ = XFUtility.calculateExpireValue(this.getTableElement(), result, dialog_.getSession());
			}
		} else {
			//
			Object value = result.getObject(this.getFieldID()); 
			//
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
						String stringValue = result.getString(this.getFieldID());
						if (stringValue == null) {
							value_ = "";
						} else {
							value_ = stringValue.trim();
						}
					}
				}
			}
		}
	}

	public void initialize() {
		value_ = XFUtility.getNullValueOfBasicType(this.getBasicType());
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

class XF310_AddRowListCell extends Object {
	private static final long serialVersionUID = 1L;
	private XF310_AddRowListColumn column_ = null;
	private XF310_AddRowListNumber rowNumber_ = null;
	//
	public XF310_AddRowListCell(XF310_AddRowListColumn column, XF310_AddRowListNumber rowNumber) {
		column_ = column;
		rowNumber_ = rowNumber;
	}
	public Object getInternalValue() {
		column_.setValue(rowNumber_.getColumnMap().get(column_.getDataSourceName()));
		return column_.getInternalValue();
	}
	public Object getExternalValue() {
		column_.setValue(rowNumber_.getColumnMap().get(column_.getDataSourceName()));
		return column_.getExternalValue();
	}
	public void setInternalValue(Object value) {
		rowNumber_.getColumnMap().put(column_.getDataSourceName(), value);
	}
	public XF310_AddRowListColumn getDetailColumn() {
		return column_;
	}
	public Color getForeground() {
		return column_.getForeground();
	}
}

class XF310_AddRowListCellRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		XF310_AddRowListCell cell = (XF310_AddRowListCell)value;
		//
		setText((String)cell.getExternalValue());
		setFont(new java.awt.Font("Dialog", 0, 14));
		//
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			if (row%2==0) {
				setBackground(table.getBackground());
			} else {
				setBackground(XFUtility.ODD_ROW_COLOR);
			}
			setForeground(table.getForeground());
		}
		//
		validate();
		return this;
	}
}

class XF310_AddRowListReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF310_AddRowList dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private String activeWhere = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private String rangeKeyFieldSearch = "";
	private boolean rangeValidated;
	//
	public XF310_AddRowListReferTable(org.w3c.dom.Element referElement, XF310_AddRowList dialog){
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
	}
	//
	public String getSelectSQL(){
		int count;
		StringBuffer buf = new StringBuffer();
		org.w3c.dom.Element workElement;
		boolean validWhereKeys = false;
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
		if (count == 0) {
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				if (count > 0) {
					buf.append(",");
				}
				count++;
				buf.append(toKeyFieldIDList.get(i));
			}
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
		buf.append(" where ");
		//
		count = 0;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				rangeKeyFieldSearch = withKeyFieldIDList.get(i);
			} else {
				if (count > 0) {
					buf.append(" and ");
				}
				buf.append(toKeyFieldIDList.get(i));
				buf.append("=");
				for (int j = 0; j < dialog_.getAddRowListColumnList().size(); j++) {
					if (withKeyFieldIDList.get(i).equals(dialog_.getAddRowListColumnList().get(j).getDataSourceName())) {
						if (XFUtility.isLiteralRequiredBasicType(dialog_.getAddRowListColumnList().get(j).getBasicType())) {
							buf.append("'");
							buf.append(dialog_.getAddRowListColumnList().get(j).getInternalValue());
							buf.append("'");
							if (!dialog_.getAddRowListColumnList().get(j).getInternalValue().equals("")) {
								validWhereKeys = true;
							}
						} else {
							buf.append(dialog_.getAddRowListColumnList().get(j).getInternalValue());
							validWhereKeys = true;
						}
						break;
					}
				}
				count++;
			}
		}
		//
		if (!activeWhere.equals("")) {
			buf.append(" and ");
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
		//
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
		//
		return isKeyNull;
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
				// Note that result set is ordered by rangeKeyFieldValue DESC //
				if (!rangeValidated) { 
					StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
					String workTableAlias = workTokenizer.nextToken();
					String workFieldID = workTokenizer.nextToken();
					Object valueKey = dialog_.getAddRowListColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
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
				StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
				String workTableAlias = workTokenizer.nextToken();
				String workFieldID = workTokenizer.nextToken();
				Object valueKey = dialog_.getAddRowListColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
				Object valueFrom = result.getObject(rangeKeyFieldValid);
				Object valueThru = result.getObject(rangeKeyFieldExpire);
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
	private DecimalFormat integerFormat = new DecimalFormat("0000");
	//
	public XF310_AddRowListNumber(int num, HashMap<String, Object> columnMap, HashMap<String, Object> returnFieldMap) {
		number_ = num;
		columnMap_ = columnMap;
		returnFieldMap_ = returnFieldMap;
	}
	public String toString() {
		return integerFormat.format(number_);
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
