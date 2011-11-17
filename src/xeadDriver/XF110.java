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

import javax.swing.table.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.text.DecimalFormat;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.w3c.dom.*;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class XF110 extends JDialog implements XFExecutable, XFScriptable {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element functionElement_ = null;
	private Session session_ = null;
	private HashMap<String, Object> parmMap_ = null;
	private HashMap<String, Object> returnMap_ = new HashMap<String, Object>();
	private Connection connection = null;
	private boolean instanceIsAvailable = true;
	public boolean isToBeCanceled = false;
	private int instanceArrayIndex_ = -1;
	private int programSequence;
	private StringBuffer processLog = new StringBuffer();
	private NodeList referElementList;
	private JPanel jPanelMain = new JPanel();
	private JPanel jPanelCenter = new JPanel();
	private JPanel jPanelTop = new JPanel();
	private JPanel jPanelTopEast = new JPanel();
	private JPanel jPanelTopCenter = new JPanel();
	private JPanel jPanelTopNorthMargin = new JPanel();
	private JPanel jPanelTopSouthMargin = new JPanel();
	private JPanel jPanelTopEastMargin = new JPanel();
	private JPanel jPanelTopWestMargin = new JPanel();
	private JPanel jPanelFilter = new JPanel();
	private XF110_PrimaryTable primaryTable_;
	private ArrayList<XF110_Filter> filterList = new ArrayList<XF110_Filter>();
	private ArrayList<XF110_Column> columnList = new ArrayList<XF110_Column>();
	private ArrayList<XF110_ReferTable> referTableList = new ArrayList<XF110_ReferTable>();
	private ArrayList<WorkingRow> workingRowList = new ArrayList<WorkingRow>();
	private SortableDomElementListModel sortingList;
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private JPanel[] jPanelButtonArray = new JPanel[7];
	private JPanel jPanelInfo = new JPanel();
	private GridLayout gridLayoutButtons = new GridLayout();
	private GridLayout gridLayoutInfo = new GridLayout();
	private GridLayout gridLayoutFilter = new GridLayout();
	private ArrayList<String> messageList = new ArrayList<String>();
	JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JSplitPane jSplitPaneCenter = new JSplitPane();
	private JScrollPane jScrollPaneTable = new JScrollPane();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	TableModelMain tableModelMain;
	private JTable jTableMain = new JTable();
	private DefaultTableCellRenderer rendererTableHeader = null;
	private DefaultTableCellRenderer rendererAlignmentCenter = new DefaultTableCellRenderer();
	private DefaultTableCellRenderer rendererAlignmentRight = new DefaultTableCellRenderer();
	private DefaultTableCellRenderer rendererAlignmentLeft = new DefaultTableCellRenderer();
	private int countOfRows = 0;
	private JTextArea jTextAreaMessages = new JTextArea();
	private JButton jButtonList = new JButton();
	private JButton[] jButtonArray = new JButton[7];
	private JButton buttonNext = null;
	private String buttonNextCaption = "";
	private Color selectionColorWithFocus = new Color(49,106,197);
	private Color selectionColorWithoutFocus = new Color(213,213,213);
	private Action helpAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			session_.browseHelp();
		}
	};
	private Action[] actionButtonArray = new Action[7];
	private String[] actionDefinitionArray = new String[7];
	private int buttonIndexForF6, buttonIndexForF8;
	private ScriptEngine scriptEngine;
	public Bindings engineScriptBindings;
	private String scriptNameRunning = "";
	private final int FONT_SIZE = 14;
	private final int ROW_HEIGHT = 18;
	private int initialReadCount = 0;
	private double filterWidth = 0;
	private ByteArrayOutputStream exceptionLog;
	private PrintStream exceptionStream;
	private String exceptionHeader = "";
	private XF110_SubList subList;
	private boolean anyFilterIsEditable = false;

	public XF110(Session session, int instanceArrayIndex) {
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
		jPanelMain.setLayout(new BorderLayout());
		jPanelCenter.setLayout(new BorderLayout());
		//
		jPanelTop.setPreferredSize(new Dimension(600, 45));
		jPanelTop.setLayout(new BorderLayout());
		jPanelTopNorthMargin.setPreferredSize(new Dimension(600, 8));
		jPanelTopSouthMargin.setPreferredSize(new Dimension(600, 8));
		jPanelTopEastMargin.setPreferredSize(new Dimension(8, 20));
		jPanelTopWestMargin.setPreferredSize(new Dimension(8, 20));
		jPanelTopEast.setPreferredSize(new Dimension(80, 80));
		jPanelTopEast.setLayout(new BorderLayout());
		jPanelTopEast.add(jButtonList, BorderLayout.CENTER);
		jPanelTopEast.add(jPanelTopEastMargin, BorderLayout.EAST);
		jPanelTopEast.add(jPanelTopWestMargin, BorderLayout.WEST);
		jButtonList.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jButtonList.setText(res.getString("Search"));
		jButtonList.addActionListener(new XF110_jButtonList_actionAdapter(this));
		jButtonList.addKeyListener(new XF110_Component_keyAdapter(this));
		jPanelTopCenter.setLayout(new BorderLayout());
		jPanelTopCenter.add(jPanelFilter, BorderLayout.CENTER);
		jPanelTop.setBorder(BorderFactory.createEtchedBorder());
		jPanelTop.add(jPanelTopCenter, BorderLayout.CENTER);
		jPanelTop.add(jPanelTopNorthMargin, BorderLayout.NORTH);
		jPanelTop.add(jPanelTopSouthMargin, BorderLayout.SOUTH);
		//
		jPanelFilter.setLayout(gridLayoutFilter);
		gridLayoutFilter.setHgap(5);
		gridLayoutFilter.setVgap(5);
		//
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jTextAreaMessages.setWrapStyleWord(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jSplitPaneCenter.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPaneCenter.add(jPanelCenter, JSplitPane.TOP);
		jSplitPaneCenter.add(jScrollPaneMessages, JSplitPane.BOTTOM);
		//
		jPanelCenter.add(jPanelTop, BorderLayout.NORTH);
		jPanelCenter.add(jScrollPaneTable, BorderLayout.CENTER);
		//
		jTableMain.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTableMain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTableMain.setRowHeight(ROW_HEIGHT);
		jTableMain.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableMain.setRowSelectionAllowed(true);
		jTableMain.addKeyListener(new XF110_jTableMain_keyAdapter(this));
		jTableMain.addMouseListener(new XF110_jTableMain_mouseAdapter(this));
		jTableMain.addFocusListener(new XF110_jTableMain_focusAdapter(this));
		//jTableMain.setAutoCreateRowSorter(true);
		rendererAlignmentCenter.setHorizontalAlignment(SwingConstants.CENTER);
		rendererAlignmentRight.setHorizontalAlignment(SwingConstants.RIGHT);
		rendererAlignmentLeft.setHorizontalAlignment(SwingConstants.LEFT);
		JTableHeader header = new JTableHeader(jTableMain.getColumnModel()) {
			private static final long serialVersionUID = 1L;
			public String getToolTipText(MouseEvent e) {
				String text = "";
				int c = columnAtPoint(e.getPoint());
				for (int i = 0; i < columnList.size(); i++) {
					if (columnList.get(i).isVisibleOnPanel()) {
						if (columnList.get(i).getColumnIndex() == c) {
							if (columnList.get(i).getDecimalSize() > 0) {
								text = "<html>" + columnList.get(i).getFieldName() + " " + columnList.get(i).getDataSourceName() + " (" + columnList.get(i).getDataSize() + "," + columnList.get(i).getDecimalSize() + ")<br>" + columnList.get(i).getFieldRemarks();
							} else {
								text = "<html>" + columnList.get(i).getFieldName() + " " + columnList.get(i).getDataSourceName() + " (" + columnList.get(i).getDataSize() + ")<br>" + columnList.get(i).getFieldRemarks();
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
		jScrollPaneTable.addMouseListener(new XF110_jScrollPaneTable_mouseAdapter(this));
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
			jButtonArray[i].addActionListener(new XF110_FunctionButton_actionAdapter(this));
			jPanelButtonArray[i] = new JPanel();
			jPanelButtonArray[i].setLayout(new BorderLayout());
			jPanelButtonArray[i].add(jButtonArray[i], BorderLayout.CENTER);
			actionButtonArray[i] = new ButtonAction(jButtonArray[i]);
			jPanelButtons.add(jPanelButtonArray[i]);
		}
		//
		jPanelMain.add(jSplitPaneCenter, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
	}

	public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parmMap) {
		String workAlias, workTableID, workFieldID, workStr;
		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;

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
			returnMap_.put("RETURN_CODE", "00");

			/////////////////////////
			// Initialize variants //
			/////////////////////////
			instanceIsAvailable = false;
			exceptionLog = new ByteArrayOutputStream();
			exceptionStream = new PrintStream(exceptionLog);
			exceptionHeader = "";
			processLog.delete(0, processLog.length());
			messageList.clear();
			functionElement_ = functionElement;
			if (functionElement_.getAttribute("InitialReadCount").equals("")) {
				initialReadCount = 0;
			} else {
				initialReadCount = Integer.parseInt(functionElement_.getAttribute("InitialReadCount"));
			}
			connection = session_.getConnection();
			programSequence = session_.writeLogOfFunctionStarted(functionElement_.getAttribute("ID"), functionElement_.getAttribute("Name"));

			//////////////////////////////////////
			// Setup Script Engine and Bindings //
			//////////////////////////////////////
			scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
			engineScriptBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
			engineScriptBindings.clear();
			engineScriptBindings.put("instance", (XFScriptable)this);
			
			//////////////////////////////
			// Set panel configurations //
			//////////////////////////////
			jLabelSessionID.setText(session_.getSessionID());
			jLabelFunctionID.setText("110" + "-" + instanceArrayIndex_ + "-" + functionElement_.getAttribute("ID"));
			FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font("Dialog", 0, FONT_SIZE));
			jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));
			this.setTitle(functionElement_.getAttribute("Name"));
	        Rectangle screenRect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			if (functionElement_.getAttribute("Size").equals("")) {
				this.setPreferredSize(new Dimension(screenRect.width, screenRect.height));
				this.setLocation(screenRect.x, screenRect.y);
			} else {
				workTokenizer = new StringTokenizer(functionElement_.getAttribute("Size"), ";" );
				int width = Integer.parseInt(workTokenizer.nextToken());
				int height = Integer.parseInt(workTokenizer.nextToken());
				this.setPreferredSize(new Dimension(width, height));
				int posX = (screenRect.width - width) / 2;
				int posY = (screenRect.height - height) / 2;
				this.setLocation(posX, posY);
			}
			this.pack();
			jSplitPaneCenter.setDividerLocation(this.getPreferredSize().height - 118);

			//////////////////////////////////////////////
			// Setup the primary table and refer tables //
			//////////////////////////////////////////////
			primaryTable_ = new XF110_PrimaryTable(functionElement_, this);
			referTableList.clear();
			referElementList = primaryTable_.getTableElement().getElementsByTagName("Refer");
			sortingList = XFUtility.getSortedListModel(referElementList, "Order");
			for (int i = 0; i < sortingList.getSize(); i++) {
				referTableList.add(new XF110_ReferTable((org.w3c.dom.Element)sortingList.getElementAt(i), this));
			}

			///////////////////////////
			// Setup Filter fields ////
			///////////////////////////
			filterList.clear();
			jPanelCenter.remove(jPanelTop);
			jPanelTop.remove(jPanelTopEast);
			jPanelFilter.removeAll();
			anyFilterIsEditable = false;
			NodeList FilterFieldList = functionElement_.getElementsByTagName("Filter");
			sortingList = XFUtility.getSortedListModel(FilterFieldList, "Order");
			if (sortingList.getSize() <= 2) {
				jPanelTop.setPreferredSize(new Dimension(600, 45));
				gridLayoutFilter.setColumns(2);
				gridLayoutFilter.setRows(1);
				filterWidth = (this.getPreferredSize().getWidth() - 90) / 2;
			}
			if (sortingList.getSize() == 3) {
				jPanelTop.setPreferredSize(new Dimension(600, 45));
				gridLayoutFilter.setColumns(3);
				gridLayoutFilter.setRows(1);
				filterWidth = (this.getPreferredSize().getWidth() - 90) / 3;
			}
			if (sortingList.getSize() == 4) {
				jPanelTop.setPreferredSize(new Dimension(600, 45));
				gridLayoutFilter.setColumns(4);
				gridLayoutFilter.setRows(1);
				filterWidth = (this.getPreferredSize().getWidth() - 90) / 4;
			}
			if (sortingList.getSize() >= 5 && sortingList.getSize() <= 6) {
				jPanelTop.setPreferredSize(new Dimension(600, 75));
				gridLayoutFilter.setColumns(3);
				gridLayoutFilter.setRows(2);
				filterWidth = (this.getPreferredSize().getWidth() - 90) / 3;
			}
			if (sortingList.getSize() >= 7) {
				jPanelTop.setPreferredSize(new Dimension(600, 75));
				gridLayoutFilter.setColumns(4);
				gridLayoutFilter.setRows(2);
				filterWidth = (this.getPreferredSize().getWidth() - 90) / 4;
			}
			for (int i = 0; i < sortingList.getSize() && i < 8; i++) {
				filterList.add(new XF110_Filter((org.w3c.dom.Element)sortingList.getElementAt(i), this));
				//filterList.get(i).setParmMapValue(parmMap_);
				if (!filterList.get(i).isValidatedWithParmMapValue(parmMap_)) {
					JOptionPane.showMessageDialog(this, res.getString("FunctionError47") + filterList.get(i).getCaption() + res.getString("FunctionError48"));
					isToBeCanceled = true;
					break;
				}
				if (filterList.get(i).isEditable()) {
					anyFilterIsEditable = true;
				}
				jPanelFilter.add(filterList.get(i));
			}
			if (anyFilterIsEditable) {
				jPanelTop.add(jPanelTopEast, BorderLayout.EAST);
			}
			if (filterList.size() >= 1) {
				jPanelCenter.add(jPanelTop, BorderLayout.NORTH);
				filterList.get(0).requestFocus();
			}

			//////////////////////////////
			// Setup JTable and Columns //
			//////////////////////////////
			tableModelMain = new TableModelMain();
			jTableMain.setModel(tableModelMain);
			TableColumn column = null;
			columnList.clear();
			tableModelMain.addColumn("NO."); //column index:0 //
			tableModelMain.addColumn(""); //column index:1 //
			int columnIndex = 1;
			NodeList columnElementList = functionElement_.getElementsByTagName("Column");
			sortingList = XFUtility.getSortedListModel(columnElementList, "Order");
			for (int i = 0; i < columnElementList.getLength(); i++) {
				columnList.add(new XF110_Column((org.w3c.dom.Element)sortingList.getElementAt(i), this));
				if (columnList.get(i).isVisibleOnPanel()) {
					columnIndex++;
					columnList.get(i).setColumnIndex(columnIndex);
					tableModelMain.addColumn(columnList.get(i).getCaption());
				}
			}
			//
			column = jTableMain.getColumnModel().getColumn(0);
			column.setPreferredWidth(38);
			column.setCellRenderer(new XF110_RowNumberRenderer());
			column = jTableMain.getColumnModel().getColumn(1);
			column.setPreferredWidth(22);
			column.setCellRenderer(new CheckBoxRenderer());
			column.setCellEditor(new DefaultCellEditor(new JCheckBox()));
			column.setHeaderRenderer(new CheckBoxHeaderRenderer(new CheckBoxHeaderListener()));
			for (int i = 0; i < columnList.size(); i++) {
				if (columnList.get(i).isVisibleOnPanel()) {
					column = jTableMain.getColumnModel().getColumn(columnList.get(i).getColumnIndex());
					column.setPreferredWidth(columnList.get(i).getWidth());
					column.setCellRenderer(columnList.get(i).getCellRenderer());
					column.setHeaderRenderer(columnList.get(i).getHeaderRenderer());
				}
			}
			//
			// Add primary table key fields as HIDDEN columns if they are not on the column list //
			for (int i = 0; i < primaryTable_.getKeyFieldIDList().size(); i++) {
				if (!existsInColumnList(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldIDList().get(i))) {
					columnList.add(new XF110_Column(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldIDList().get(i), this));
				}
			}
			//
			// Add OrderBy fields as HIDDEN columns if they are not on the column list //
			for (int i = 0; i < primaryTable_.getOrderByFieldIDList().size(); i++) {
				workStr = primaryTable_.getOrderByFieldIDList().get(i).replace("(D)", "");
				workStr = workStr.replace("(A)", "");
				workTokenizer = new StringTokenizer(workStr, "." );
				workAlias = workTokenizer.nextToken();
				workTableID = getTableIDOfTableAlias(workAlias);
				workFieldID = workTokenizer.nextToken();
				if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
					columnList.add(new XF110_Column(workTableID, workAlias, workFieldID, this));
				}
			}
			//
			// Add filter fields as HIDDEN columns if they are not on the column list //
			for (int i = 0; i < filterList.size(); i++) {
				if (!existsInColumnList(filterList.get(i).getTableID(), filterList.get(i).getTableAlias(), filterList.get(i).getFieldID())) {
					columnList.add(new XF110_Column(filterList.get(i).getTableID(), filterList.get(i).getTableAlias(), filterList.get(i).getFieldID(), this));
				}
			}
			//
			// Analyze fields in script and add them as HIDDEN columns if necessary //
			for (int i = 0; i < primaryTable_.getScriptList().size(); i++) {
				if	(primaryTable_.getScriptList().get(i).isToBeRunAtEvent("BR", "")
						|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("AR", "")) {
					for (int j = 0; j < primaryTable_.getScriptList().get(i).getFieldList().size(); j++) {
						workTokenizer = new StringTokenizer(primaryTable_.getScriptList().get(i).getFieldList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workTableID = getTableIDOfTableAlias(workAlias);
						workFieldID = workTokenizer.nextToken();
						if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
							workElement = session_.getFieldElement(workTableID, workFieldID);
							if (workElement == null) {
								String msg = res.getString("FunctionError1") + primaryTable_.getTableID() + res.getString("FunctionError2") + primaryTable_.getScriptList().get(i).getName() + res.getString("FunctionError3") + workAlias + "_" + workFieldID + res.getString("FunctionError4");
								JOptionPane.showMessageDialog(this, msg);
								throw new Exception(msg);
							} else {
								if (primaryTable_.isValidDataSource(workTableID, workAlias, workFieldID)) {
									columnList.add(new XF110_Column(workTableID, workAlias, workFieldID, this));
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
				if (referTableList.get(i).isToBeExecuted()) {
					for (int j = 0; j < referTableList.get(i).getFieldIDList().size(); j++) {
						if (!existsInColumnList(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j))) {
							columnList.add(new XF110_Column(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j), this));
						}
					}
					for (int j = 0; j < referTableList.get(i).getWithKeyFieldIDList().size(); j++) {
						workTokenizer = new StringTokenizer(referTableList.get(i).getWithKeyFieldIDList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workTableID = getTableIDOfTableAlias(workAlias);
						workFieldID = workTokenizer.nextToken();
						if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
							columnList.add(new XF110_Column(workTableID, workAlias, workFieldID, this));
						}
					}
				}
			}

			//////////////////////////////////////////////
			// Setup function-keys and function-buttons //
			//////////////////////////////////////////////
			setupFunctionKeysAndButtons();

			//////////////////////////////
			// Construct SubList Dialog //
			//////////////////////////////
			subList = new XF110_SubList(this);
			if (subList.isInvalid()) {
				throw new Exception();
			} else {
				subList.getCheckBoxToExecuteBatchFunction().setSelected(true);
			}

		} catch(Exception e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError5"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		////////////////////////////////////////
		// Set message and rows to show Panel //
		////////////////////////////////////////
		if (anyFilterIsEditable) {
			if (parmMap_.containsKey("INITIAL_MESSAGE")) {
				jTextAreaMessages.setText((String)parmMap_.get("INITIAL_MESSAGE"));
				parmMap_.remove("INITIAL_MESSAGE");
			} else {
				jTextAreaMessages.setText(res.getString("FunctionMessage1"));
			}
		} else {
			selectRowsAndList();
		}
		if (!returnMap_.get("RETURN_CODE").equals("99") && !isToBeCanceled) {
			this.setVisible(true);
		}

		///////////////////////////////
		// Release instance and exit //
		///////////////////////////////
		return returnMap_;
	}

	public boolean isAvailable() {
		return instanceIsAvailable;
	}

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			closeFunction();
		}
	}

	void setErrorAndCloseFunction() {
		returnMap_.put("RETURN_CODE", "99");
		closeFunction();
	}

	void closeFunction() {
		instanceIsAvailable = true;
		String wrkStr;
		if (exceptionLog.size() > 0 || !exceptionHeader.equals("")) {
			wrkStr = processLog.toString() + "\nERROR LOG:\n" + exceptionHeader + exceptionLog.toString();
		} else {
			wrkStr = processLog.toString();
		}
		wrkStr = wrkStr.replace("'", "\"");
		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), wrkStr );
		this.setVisible(false);
	}
	
	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(this, message);
		}
		returnMap_.put("RETURN_CODE", "01");
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
			if (element.getAttribute("Action").equals("EXIT")
					|| element.getAttribute("Action").equals("NEXT")
					|| element.getAttribute("Action").equals("OUTPUT")) {
				//
				workIndex = Integer.parseInt(element.getAttribute("Position"));
				actionDefinitionArray[workIndex] = element.getAttribute("Action");
				XFUtility.setCaptionToButton(jButtonArray[workIndex], element, "");
				jButtonArray[workIndex].setVisible(true);
				inputMap.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
				actionMap.put("actionButton" + workIndex, actionButtonArray[workIndex]);
				//
				if (element.getAttribute("Number").equals("6")) {
					buttonIndexForF6 = workIndex;
				}
				if (element.getAttribute("Number").equals("8")) {
					buttonIndexForF8 = workIndex;
				}
				if (element.getAttribute("Action").equals("NEXT")) {
					buttonNext = jButtonArray[workIndex];
					buttonNext.setEnabled(false);
					buttonNextCaption = element.getAttribute("Caption");
				}
			}
		}
	}

	void selectRowsAndList() {
		HashMap<String, Object> keyMap;
		ArrayList<Object> columnValueList;
		ArrayList<Object> orderByValueList;
		String workStr;
		Statement statementForPrimaryTable = null;
		Statement statementForReferTable = null;
		ResultSet resultOfPrimaryTable = null;
		ResultSet resultOfReferTable = null;
		//
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			//
			int rowCount = tableModelMain.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				tableModelMain.removeRow(0);
			}
			//
			statementForPrimaryTable = connection.createStatement();
			statementForReferTable = connection.createStatement();
			boolean toBeSelected = true;
			boolean readyToValidate;
			countOfRows = 0;
			int blockRows = 0;
			workingRowList.clear();
			//
			String sql = primaryTable_.getSelectSQL();
			XFUtility.appendLog(sql, processLog);
			resultOfPrimaryTable = statementForPrimaryTable.executeQuery(sql);
			while (resultOfPrimaryTable.next()) {
				//
				for (int i = 0; i < columnList.size(); i++) {
					columnList.get(i).setReadyToValidate(false);
					columnList.get(i).initialize();
				}
				//
				primaryTable_.runScript("BR", ""); /* Script to be run BEFORE READ */
				//
				for (int i = 0; i < columnList.size(); i++) {
					if (columnList.get(i).getTableID().equals(primaryTable_.getTableID())) {
						readyToValidate = columnList.get(i).setValueOfResultSet(resultOfPrimaryTable);
						columnList.get(i).setReadyToValidate(readyToValidate);
					}
				}
				//
				primaryTable_.runScript("AR", "BR()"); /* Script to be run AFTER READ primary table */
				//
				if (checkIfThisRowIsToBeSelected()) {
					//
					toBeSelected = true;
					for (int i = 0; i < referTableList.size(); i++) {
						//
						if (referTableList.get(i).isToBeExecuted()) {
							//
							primaryTable_.runScript("AR", "BR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE READ */
							//
							sql = referTableList.get(i).getSelectSQL();
							if (!sql.equals("")) {
								XFUtility.appendLog(sql, processLog);
								resultOfReferTable = statementForReferTable.executeQuery(sql);
								while (resultOfReferTable.next()) {
									//
									if (referTableList.get(i).isRecordToBeSelected(resultOfReferTable)) {
										//
										for (int j = 0; j < columnList.size(); j++) {
											if (columnList.get(j).getTableID().equals(referTableList.get(i).getTableID()) && columnList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
												readyToValidate = columnList.get(j).setValueOfResultSet(resultOfReferTable);
												columnList.get(j).setReadyToValidate(readyToValidate);
											}
										}
										//
										primaryTable_.runScript("AR", "AR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run AFTER READ */
										//
										if (!checkIfThisRowIsToBeSelected()) {
											toBeSelected = false;
											break;
										}
									}
								}
							}
							//
							if (!toBeSelected) {
								break;
							}
						}
					}
					//
					primaryTable_.runScript("AR", "AR()"); /* Script to be run AFTER READ */
					//
					if (toBeSelected) {
						//
						for (int i = 0; i < columnList.size(); i++) {
							columnList.get(i).setReadyToValidate(true);
						}
						//
						if (checkIfThisRowIsToBeSelected()) {
							//
							keyMap = new HashMap<String, Object>();
							for (int i = 0; i < primaryTable_.getKeyFieldIDList().size(); i++) {
								keyMap.put(primaryTable_.getKeyFieldIDList().get(i), resultOfPrimaryTable.getObject(primaryTable_.getKeyFieldIDList().get(i)));
							}
							//
							if (primaryTable_.hasOrderByAsItsOwnFields()) {
								Object[] Cell = new Object[columnList.size() + 1];
								Cell[0] = new XF110_RowNumber(countOfRows + 1, keyMap, resultOfPrimaryTable.getInt(primaryTable_.getUpdateCounterID()));
								Cell[1] = false;
								for (int i = 0; i < columnList.size(); i++) {
									if (columnList.get(i).isVisibleOnPanel()) {
										Cell[columnList.get(i).getColumnIndex()] = columnList.get(i).getCellObject();
									}
								}
								tableModelMain.addRow(Cell);
							} else {
								columnValueList = new ArrayList<Object>();
								for (int i = 0; i < columnList.size(); i++) {
									if (columnList.get(i).isVisibleOnPanel()) {
										columnValueList.add(columnList.get(i).getCellObject());
									}
								}
								orderByValueList = new ArrayList<Object>();
								for (int i = 0; i < primaryTable_.getOrderByFieldIDList().size(); i++) {
									workStr = primaryTable_.getOrderByFieldIDList().get(i).replace("(D)", "");
									workStr = workStr.replace("(A)", "");
									for (int j = 0; j < columnList.size(); j++) {
										if (columnList.get(j).getDataSourceName().equals(workStr)) {
											orderByValueList.add(columnList.get(j).getExternalValue());
											break;
										}
									}
								}
								workingRowList.add(new WorkingRow(columnValueList, keyMap, resultOfPrimaryTable.getInt(primaryTable_.getUpdateCounterID()), orderByValueList));
							}
							//
							countOfRows++;
							blockRows++;
							if (initialReadCount > 0 && blockRows == initialReadCount) {
								Object[] bts = {res.getString("ReadEnd"), res.getString("ReadMore")};
								int reply = JOptionPane.showOptionDialog(jPanelMain, countOfRows + res.getString("ReadCountMessage"), res.getString("ReadCountCheck"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
								if (reply == 0) {
									break;
								} else {
									blockRows = 0;
								}
							}
						}
					}
				}
			}
			//
			if (!primaryTable_.hasOrderByAsItsOwnFields()) {
				WorkingRow[] workingRowArray = workingRowList.toArray(new WorkingRow[0]);
				Arrays.sort(workingRowArray, new WorkingRowComparator());
				for (int i = 0; i < workingRowArray.length; i++) {
					Object[] Cell = new Object[workingRowArray[i].getColumnValueList().size() + 2];
					Cell[0] = new XF110_RowNumber(i + 1, workingRowArray[i].getKeyMap(), workingRowArray[i].getUpdateCounter());
					Cell[1] = false;
					for (int j = 0; j < workingRowArray[i].getColumnValueList().size(); j++) {
						//Cell[j+1] = workingRowArray[i].getColumnValueList().get(j);
						Cell[j+2] = workingRowArray[i].getColumnValueList().get(j);
					}
					tableModelMain.addRow(Cell);
				}
			}
			//
			jTableMain.requestFocus();
			//
			messageList.clear();
			if (countOfRows > 0) {
				jTableMain.setRowSelectionInterval(0, 0);
				messageList.add(res.getString("FunctionMessage5") + buttonNextCaption + res.getString("FunctionMessage6"));
			} else {
				messageList.add(res.getString("FunctionMessage4"));
			}
			setMessagesOnPanel();
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
		} finally {
			try {
				if (resultOfPrimaryTable != null) {
					resultOfPrimaryTable.close();
				}
				if (resultOfReferTable != null) {
					resultOfReferTable.close();
				}
				if (statementForPrimaryTable != null) {
					statementForPrimaryTable.close();
				}
				if (statementForReferTable != null) {
					statementForReferTable.close();
				}
			} catch (SQLException ex) {
				ex.printStackTrace(exceptionStream);
			}
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	void jFunctionButton_actionPerformed(ActionEvent e) {
		Component com = (Component)e.getSource();
		//
		for (int i = 0; i < 7; i++) {
			if (com.equals(jButtonArray[i])) {
				doButtonAction(actionDefinitionArray[i]);
				break;
			}
		}
		//
		if (jTableMain.getSelectedRow() > -1) {
			jTableMain.requestFocus();
		} else {
			if (filterList.size() > 0) {
				filterList.get(0).requestFocus();
			} else {
				jButtonList.requestFocus();
			}
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
			if (action.equals("NEXT")) {
				String reply = subList.request();
				if (reply.equals("ERROR")) {
					setErrorAndCloseFunction();
				}
				if (reply.equals("EXIT")) {
					closeFunction();
				}
				if (reply.equals("UPDATE")) {
					returnMap_.put("RETURN_CODE", "20");
					returnMap_.put("RETURN_MESSAGE", subList.getReturnMessage());
					closeFunction();
				}
			}
			//
			if (action.equals("OUTPUT")) {
				session_.browseFile(getExcellBookURI());
			}
			//
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public XF110_Column getColumnObjectByID(String tableID, String tableAlias, String fieldID) {
		XF110_Column columnField = null;
		for (int i = 0; i < columnList.size(); i++) {
			if (tableID.equals("")) {
				if (columnList.get(i).getTableAlias().equals(tableAlias) && columnList.get(i).getFieldID().equals(fieldID)) {
					columnField = columnList.get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (columnList.get(i).getTableID().equals(tableID) && columnList.get(i).getFieldID().equals(fieldID)) {
					columnField = columnList.get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (columnList.get(i).getTableID().equals(tableID) && columnList.get(i).getTableAlias().equals(tableAlias) && columnList.get(i).getFieldID().equals(fieldID)) {
					columnField = columnList.get(i);
					break;
				}
			}
		}
		return columnField;
	}

	public boolean existsInColumnList(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < columnList.size(); i++) {
			if (tableID.equals("")) {
				if (columnList.get(i).getTableAlias().equals(tableAlias)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (columnList.get(i).getTableID().equals(tableID) && columnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (columnList.get(i).getTableID().equals(tableID) && columnList.get(i).getTableAlias().equals(tableAlias) && columnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public XF110_Filter getFilterObjectByName(String dataSourceName) {
		XF110_Filter filter = null;
		for (int i = 0; i < filterList.size(); i++) {
			if (filterList.get(i).getDataSourceName().equals(dataSourceName)) {
				filter = filterList.get(i);
				break;
			}
		}
		return filter;
	}
	
	public String getTableIDOfTableAlias(String tableAlias) {
		String tableID = tableAlias;
		org.w3c.dom.Element workElement;
		for (int j = 0; j < referElementList.getLength(); j++) {
			workElement = (org.w3c.dom.Element)referElementList.item(j);
			if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
				tableID = workElement.getAttribute("ToTable");
				break;
			}
		}
		return tableID;
	}

	boolean checkIfThisRowIsToBeSelected() {
		boolean toBeSelected = true;
		//
		for (int i = 0; i < filterList.size(); i++) {
			toBeSelected = filterList.get(i).isValidated();
			if (!toBeSelected) {
				break;
			}
		}
		//
		return toBeSelected;
	}
	
	class TableModelMain extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		public boolean isCellEditable(int row, int col) {
			return col == 1;
		}
	}

	class CheckBoxHeaderListener implements ItemListener {
		private CheckBoxHeaderRenderer rendererComponent_ = null;   
		public void setRenderer(CheckBoxHeaderRenderer rendererComponent) {
			rendererComponent_ = rendererComponent;
		}
		public void itemStateChanged(ItemEvent e) {   
			if (rendererComponent_ != null) {
				buttonNext.setEnabled(false);
				if (rendererComponent_.isSelected()) {
					for (int i = 0; i < tableModelMain.getRowCount(); i++) {
						tableModelMain.setValueAt(true, i, 1);
						buttonNext.setEnabled(true);
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
					setBackground(XFUtility.ODD_ROW_COLOR);
				}
				setForeground(table.getForeground());
			}
			setFocusable(false);
			boolean isChecked = ((Boolean) value).booleanValue();
			setSelected(isChecked);
			//
			if (isChecked) {
				buttonNext.setEnabled(true);
			} else {
				buttonNext.setEnabled(false);
				for (int i = 0; i < tableModelMain.getRowCount(); i++) {
					if ((Boolean)tableModelMain.getValueAt(i, 1)) {
						buttonNext.setEnabled(true);
					}
				}
			}
			//
			return this;
		} 
	} 
	
	 class WorkingRow extends Object {
		private ArrayList<Object> columnValueList_ = new ArrayList<Object>();
		private ArrayList<Object> orderByValueList_ = new ArrayList<Object>();
		private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
		private int updateCounter_ = 0;
		//
		public WorkingRow(ArrayList<Object> columnValueList, HashMap<String, Object> keyMap, int updateCounter, ArrayList<Object> orderByValueList) {
			columnValueList_ = columnValueList;
			keyMap_ = keyMap;
			updateCounter_ = updateCounter;
			orderByValueList_ = orderByValueList;
		}
		public HashMap<String, Object> getKeyMap() {
			return keyMap_;
		}
		public ArrayList<Object> getColumnValueList() {
			return columnValueList_;
		}
		public ArrayList<Object> getOrderByValueList() {
			return orderByValueList_;
		}
		public int getUpdateCounter() {
			return updateCounter_;
		}
	}
		
	 class WorkingRowComparator implements java.util.Comparator<WorkingRow>{
		 public int compare(WorkingRow row1, WorkingRow row2){
			 int compareResult = 0;
			 for (int i = 0; i < row1.getOrderByValueList().size(); i++) {
				compareResult = row1.getOrderByValueList().get(i).toString().compareTo(row2.getOrderByValueList().get(i).toString());
				if (primaryTable_.getOrderByFieldIDList().get(i).contains("(D)")) {
					compareResult = compareResult * -1;
				}
				if (compareResult != 0) {
					break;
				}
			 }
			 return compareResult;
		 }
	 }

	 URI getExcellBookURI() {
		 File xlsFile = null;
		 String xlsFileName = "";
		 FileOutputStream fileOutputStream = null;
		 //
		 HSSFWorkbook workBook = new HSSFWorkbook();
		 String wrkStr = functionElement_.getAttribute("Name").replace("/", "_").replace("Å^", "_");
		 HSSFSheet workSheet = workBook.createSheet(wrkStr);
		 workSheet.setDefaultRowHeight( (short) 300);
		 HSSFFooter workSheetFooter = workSheet.getFooter();
		 workSheetFooter.setRight(functionElement_.getAttribute("Name") + "  Page " + HSSFFooter.page() + " / " + HSSFFooter.numPages() );
		 //
		 HSSFFont fontHeader = workBook.createFont();
		 fontHeader = workBook.createFont();
		 fontHeader.setFontName(res.getString("XLSFontHDR"));
		 fontHeader.setFontHeightInPoints((short)11);
		 //
		 HSSFFont fontDetailBlack = workBook.createFont();
		 fontDetailBlack.setFontName(res.getString("XLSFontDTL"));
		 fontDetailBlack.setFontHeightInPoints((short)11);
		 HSSFFont fontDetailRed = workBook.createFont();
		 fontDetailRed.setFontName(res.getString("XLSFontDTL"));
		 fontDetailRed.setFontHeightInPoints((short)11);
		 fontDetailRed.setColor(HSSFColor.RED.index);
		 HSSFFont fontDetailBlue = workBook.createFont();
		 fontDetailBlue.setFontName(res.getString("XLSFontDTL"));
		 fontDetailBlue.setFontHeightInPoints((short)11);
		 fontDetailBlue.setColor(HSSFColor.BLUE.index);
		 HSSFFont fontDetailGreen = workBook.createFont();
		 fontDetailGreen.setFontName(res.getString("XLSFontDTL"));
		 fontDetailGreen.setFontHeightInPoints((short)11);
		 fontDetailGreen.setColor(HSSFColor.GREEN.index);
		 HSSFFont fontDetailOrange = workBook.createFont();
		 fontDetailOrange.setFontName(res.getString("XLSFontDTL"));
		 fontDetailOrange.setFontHeightInPoints((short)11);
		 fontDetailOrange.setColor(HSSFColor.ORANGE.index);
		 //
		 HSSFCellStyle styleHeader = workBook.createCellStyle();
		 styleHeader.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		 styleHeader.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		 styleHeader.setBorderRight(HSSFCellStyle.BORDER_THIN);
		 styleHeader.setBorderTop(HSSFCellStyle.BORDER_THIN);
		 styleHeader.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		 styleHeader.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		 styleHeader.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		 styleHeader.setFont(fontHeader);
		 //
		 HSSFCellStyle styleHeaderNumber = workBook.createCellStyle();
		 styleHeaderNumber.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		 styleHeaderNumber.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		 styleHeaderNumber.setBorderRight(HSSFCellStyle.BORDER_THIN);
		 styleHeaderNumber.setBorderTop(HSSFCellStyle.BORDER_THIN);
		 styleHeaderNumber.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		 styleHeaderNumber.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		 styleHeaderNumber.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		 styleHeaderNumber.setFont(fontHeader);
		 //
		 HSSFCellStyle styleHeaderSelect = workBook.createCellStyle();
		 styleHeaderSelect.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		 styleHeaderSelect.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		 styleHeaderSelect.setBorderRight(HSSFCellStyle.BORDER_THIN);
		 styleHeaderSelect.setBorderTop(HSSFCellStyle.BORDER_THIN);
		 styleHeaderSelect.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		 styleHeaderSelect.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		 styleHeaderSelect.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		 styleHeaderSelect.setFont(fontHeader);
		 //
		 HSSFCellStyle styleDataInteger = workBook.createCellStyle();
		 styleDataInteger.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		 styleDataInteger.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		 styleDataInteger.setBorderRight(HSSFCellStyle.BORDER_THIN);
		 styleDataInteger.setBorderTop(HSSFCellStyle.BORDER_THIN);
		 styleDataInteger.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		 styleDataInteger.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		 styleDataInteger.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
		 //
		 HSSFCellStyle styleDataString = workBook.createCellStyle();
		 styleDataString.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		 styleDataString.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		 styleDataString.setBorderRight(HSSFCellStyle.BORDER_THIN);
		 styleDataString.setBorderTop(HSSFCellStyle.BORDER_THIN);
		 styleDataString.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		 styleDataString.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		 //
		 int currentRowNumber = -1;
		 //
		 try {
			 xlsFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".xls");
			 xlsFileName = xlsFile.getPath();
			 fileOutputStream = new FileOutputStream(xlsFileName);
			 TableCellReadOnly cellObject = null;
			 HSSFFont font = null;
			 int columnIndex;
			 //
			 currentRowNumber++;
			 HSSFRow rowCaption = workSheet.createRow(currentRowNumber);
			 for (int i = 0; i < tableModelMain.getColumnCount(); i++) {
				 HSSFCell cell = rowCaption.createCell(i);
				 if (i == 0) {
					 cell.setCellStyle(styleHeaderNumber);
				 } else {
					 if (i == 1) {
						 cell.setCellStyle(styleHeaderSelect);
					 } else {
						 for (int j = 0; j < columnList.size(); j++) {
							 if (columnList.get(j).getColumnIndex() == i) {
								 if (columnList.get(j).getBasicType().equals("INTEGER")
										 || columnList.get(j).getBasicType().equals("FLOAT")) {
									if (columnList.get(j).getTypeOptionList().contains("MSEQ") || columnList.get(j).getTypeOptionList().contains("FYEAR")) {
										cell.setCellStyle(styleHeader);
									} else {
										cell.setCellStyle(styleHeaderNumber);
									}
								 } else {
									 cell.setCellStyle(styleHeader);
								 }
								 break;
							 }
						 }
					 }
				 }
				 Rectangle rect = jTableMain.getCellRect(0, i, true);
				 workSheet.setColumnWidth(i, rect.width * 40);
				 if (i == 1) {
					 cell.setCellValue(new HSSFRichTextString(res.getString("Sel")));
				 } else {
					 cell.setCellValue(new HSSFRichTextString(tableModelMain.getColumnName(i)));
				 }
			 }
			 //
			 for (int i = 0; i < tableModelMain.getRowCount(); i++) {
				 currentRowNumber++;
				 HSSFRow rowData = workSheet.createRow(currentRowNumber);
				 //
				 HSSFCell cell = rowData.createCell(0); //Column of Sequence Number
				 cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				 cell.setCellStyle(styleDataInteger);
				 cell.setCellValue(i + 1);
				 //
				 cell = rowData.createCell(1); //Column of Selection
				 cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				 cell.setCellStyle(styleDataString);
				 if ((Boolean)tableModelMain.getValueAt(i, 1)) {
					 cell.setCellValue(new HSSFRichTextString("v"));
				 } else {
					 cell.setCellValue(new HSSFRichTextString(""));
				 }
				 //
				 columnIndex = 1;
				 for (int j = 0; j < columnList.size(); j++) {
					 if (columnList.get(j).isVisibleOnPanel()) {
						 columnIndex++;
						 cellObject = (TableCellReadOnly)tableModelMain.getValueAt(i,columnIndex);
						 if (cellObject.getColor().equals(Color.black)) {
							 font = fontDetailBlack;
						 }
						 if (cellObject.getColor().equals(Color.red)) {
							 font = fontDetailRed;
						 }
						 if (cellObject.getColor().equals(Color.blue)) {
							 font = fontDetailBlue;
						 }
						 if (cellObject.getColor().equals(Color.green)) {
							 font = fontDetailGreen;
						 }
						 if (cellObject.getColor().equals(Color.orange)) {
							 font = fontDetailOrange;
						 }
						 setupCellAttributes(rowData.createCell(columnIndex), workBook, columnList.get(j).getBasicType(), columnList.get(j).getTypeOptionList(), (TableCellReadOnly)tableModelMain.getValueAt(i,columnIndex), font, columnList.get(j).getDecimalSize());
					 }
				 }
			 }
			 //
			 currentRowNumber++;
			 HSSFRow rowRemarks = workSheet.createRow(currentRowNumber);
			 HSSFCell cell = rowRemarks.createCell(0);
			 StringBuffer buf = new StringBuffer();
			 buf.append(res.getString("XLSComment2"));
			 for (int j = 0; j < filterList.size(); j++) {
				 if (j>0) {
					 buf.append("ÅA");
				 }
				 buf.append(filterList.get(j).getCaptionAndValue());
			 }
			 cell.setCellValue(new HSSFRichTextString(buf.toString()));
			 //
			 workBook.write(fileOutputStream);
			 //
			 messageList.add(res.getString("XLSComment1"));
			 //
		 } catch(Exception e) {
			 messageList.add(res.getString("XLSErrorMessage"));
			 e.printStackTrace(exceptionStream);
		 } finally {
			 try {
				 fileOutputStream.close();
			 } catch(Exception e) {
				 e.printStackTrace(exceptionStream);
			 }
		 }
		 //
		 return xlsFile.toURI();
	 }

	 private void setupCellAttributes(HSSFCell cell, HSSFWorkbook workbook, String basicType, ArrayList<String> typeOptionList, TableCellReadOnly object, HSSFFont font, int decimalSize) {
		 String wrk;
		 //
		 Color color = object.getColor();
		 if (color.equals(Color.red)) {
			 font.setColor(HSSFColor.RED.index);
		 }
		 if (color.equals(Color.blue)) {
			 font.setColor(HSSFColor.BLUE.index);
		 }
		 if (color.equals(Color.green)) {
			 font.setColor(HSSFColor.GREEN.index);
		 }
		 if (color.equals(Color.orange)) {
			 font.setColor(HSSFColor.ORANGE.index);
		 }
		 //
		 HSSFCellStyle style = workbook.createCellStyle();
		 style.setFont(font);
		 style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		 style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		 style.setBorderRight(HSSFCellStyle.BORDER_THIN);
		 style.setBorderTop(HSSFCellStyle.BORDER_THIN);
		 //
		 if (basicType.equals("INTEGER")) {
			if (typeOptionList.contains("MSEQ") || typeOptionList.contains("FYEAR")) {
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(new HSSFRichTextString(object.getValue().toString()));
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
				style.setWrapText(true);
				style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
				cell.setCellStyle(style);
			} else {
				if (object.getValue() == null) {
					wrk = "";
				} else {
					wrk = XFUtility.getStringNumber(object.getValue().toString());
				}
				if (wrk.equals("")) {
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					cell.setCellValue(new HSSFRichTextString(wrk));
				} else {
					cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue(Double.parseDouble(wrk));
				}
				//
				style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
				style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
				cell.setCellStyle(style);
			}
		 } else {
			 if (basicType.equals("FLOAT")) {
				 if (object.getValue() == null) {
					 wrk = "";
				 } else {
					 wrk = XFUtility.getStringNumber(object.getValue().toString());
				 }
				 if (wrk.equals("")) {
					 cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					 cell.setCellValue(new HSSFRichTextString(wrk));
				 } else {
					 cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					 cell.setCellValue(Double.parseDouble(wrk));
				 }
				 //
				 style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
				 style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
				 style.setDataFormat(XFUtility.getFloatFormat(workbook, decimalSize));
				 cell.setCellStyle(style);
			 } else {
				 cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				 if (object.getValue() == null) {
					 wrk = "";
				 } else {
					 wrk = object.getValue().toString();
				 }
				 cell.setCellValue(new HSSFRichTextString(wrk));
				 //
				 style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				 style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
				 style.setWrapText(true);
				 style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
				 cell.setCellStyle(style);
			 }
		 }
	 }
	
	void jTableMain_focusGained(FocusEvent e) {
		jTableMain.setSelectionBackground(selectionColorWithFocus);
		jTableMain.setSelectionForeground(Color.white);
	}

	void jTableMain_focusLost(FocusEvent e) {
		jTableMain.setSelectionBackground(selectionColorWithoutFocus);
		jTableMain.setSelectionForeground(Color.black);
	}

	void jScrollPaneTable_mousePressed(MouseEvent e) {
		jTableMain.requestFocus();
	}

	void component_keyPressed(KeyEvent e) {
		//
		// Steps to override F6 and F8 of JSplitPane//
		if (e.getKeyCode() == KeyEvent.VK_F6 && buttonIndexForF6 != -1) {
			jButtonArray[buttonIndexForF6].doClick();
		}
		if (e.getKeyCode() == KeyEvent.VK_F8 && buttonIndexForF8 != -1) {
			jButtonArray[buttonIndexForF8].doClick();
		}
	}

	void jTableMain_mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2 && tableModelMain.getRowCount() > 0) {
			processRow();
		}
	}

	void jTableMain_keyPressed(KeyEvent e) {
		//
		if (e.getKeyCode() == KeyEvent.VK_UP && jTableMain.getSelectedRow() == 0) {
			if (filterList.size() > 0) {
				filterList.get(0).requestFocus();
			} else {
				jButtonList.requestFocus();
			}
		}
		//
		if (e.getKeyCode() == KeyEvent.VK_TAB) {
			if (filterList.size() > 0) {
				filterList.get(0).requestFocus();
			} else {
				jButtonList.requestFocus();
			}
		}
		//
		if (e.getKeyCode() == KeyEvent.VK_ENTER && tableModelMain.getRowCount() > 0) {
			processRow();
		}
		//
		// Steps to override F6 and F8 of JSplitPane//
		if (e.getKeyCode() == KeyEvent.VK_F6 && buttonIndexForF6 != -1) {
			jButtonArray[buttonIndexForF6].doClick();
		}
		if (e.getKeyCode() == KeyEvent.VK_F8 && buttonIndexForF8 != -1) {
			jButtonArray[buttonIndexForF8].doClick();
		}
	}
	
	void processRow() {
		if ((Boolean)tableModelMain.getValueAt(jTableMain.getSelectedRow(), 1)) {
			tableModelMain.setValueAt(false, jTableMain.getSelectedRow(), 1);
		} else {
			tableModelMain.setValueAt(true, jTableMain.getSelectedRow(), 1);
		}
	}

	void setMessagesOnPanel() {
		//
		jTextAreaMessages.setText("");
		//
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < messageList.size(); i++) {
			if (i > 0) {
				sb.append("\n");
			}
			if (messageList.get(i).equals("")) {
				return;
			} else {
				sb.append(messageList.get(i));
			}
		}
		//
		jTextAreaMessages.setText(sb.toString());
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

	public XFTableOperator createTableOperator(String oparation, String tableID) {
		return new XFTableOperator(session_, processLog, oparation, tableID);
	}

	public HashMap<String, Object> getReturnMap() {
		return returnMap_;
	}

	public void setReturnMap(HashMap<String, Object> map) {
		returnMap_ = map;
	}

	public String getScriptNameRunning() {
		return scriptNameRunning;
	}

	public org.w3c.dom.Element getFunctionElement() {
		return functionElement_;
	}

	public Session getSession() {
		return session_;
	}
	
	public ScriptEngine getScriptEngine() {
		return scriptEngine;
	}

	public PrintStream getExceptionStream() {
		return exceptionStream;
	}

	public void setExceptionHeader(String value) {
		exceptionHeader = value;
	}

	//public StringBuffer getProcessLog() {
	//	return processLog;
	//}

	public Bindings getEngineScriptBindings() {
		return 	engineScriptBindings;
	}

	public void evalScript(String scriptName, String scriptText) throws ScriptException {
		if (!scriptText.equals("")) {
			scriptNameRunning = scriptName;
			scriptEngine.eval(scriptText + session_.getScriptFunctions());
		}
	}
	
	public ArrayList<XF110_Filter> getFilterList() {
		return filterList;
	}
	
	public double getFilterWidth() {
		return filterWidth;
	}
	
	public ArrayList<XF110_Column> getColumnList() {
		return columnList;
	}

	public boolean scriptIsToBeExecuted(String script) {
		boolean result = true;
		for (int i = 0; i < columnList.size(); i++) {
			if (!script.contains(columnList.get(i).getFieldIDInScript())) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	public ArrayList<XF110_ReferTable> getReferTableList() {
		return referTableList;
	}

	public String getPrimaryTableID() {
		return primaryTable_.getTableID();
	}
}

class XF110_Filter extends JPanel {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element fieldElement_ = null;
	private XF110 dialog_ = null;
	private String dataType = "";
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private String fieldName = "";
	private String fieldRemarks = "";
	private String fieldCaption = "";
	private String fieldOptions = "";
	private ArrayList<String> fieldOptionList;
	private String componentType = "";
	private String operandType = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private JPanel jPanelField = new JPanel();
	private JLabel jLabelField = new JLabel();
	private XFTextField xFTextField = null;
	private XFCheckBox xFCheckBox = null;
	private XFDateField xFDateField = null;
	private XFYMonthBox xFYMonthBox = null;
	private XFMSeqBox xFMSeqBox = null;
	private XFFYearBox xFFYearBox = null;
	private XF110_PromptCallField xFPromptCall = null;
	private JComboBox jComboBox = null;
	private ArrayList<String> keyValueList = new ArrayList<String>();
	private JComponent component = null;
	private boolean isReflect = false;
	private boolean isEditable_ = true;

	public XF110_Filter(org.w3c.dom.Element fieldElement, XF110 dialog){
		super();
		//
		String wrkStr;
		fieldElement_ = fieldElement;
		dialog_ = dialog;
		//
		fieldOptions = fieldElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);
		StringTokenizer workTokenizer1 = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer1.nextToken();
		tableID = dialog_.getTableIDOfTableAlias(tableAlias);
		fieldID =workTokenizer1.nextToken();
		//
		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID + "." + fieldID + res.getString("FunctionError11"));
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
		if (dataSize > 50) {
			dataSize = 50;
		}
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		//
		operandType = "EQ";
		if (fieldOptionList.contains("GE")) {
			operandType = "GE";
		}
		if (fieldOptionList.contains("GT")) {
			operandType = "GT";
		}
		if (fieldOptionList.contains("LE")) {
			operandType = "LE";
		}
		if (fieldOptionList.contains("LT")) {
			operandType = "LT";
		}
		if (fieldOptionList.contains("SCAN")) {
			operandType = "SCAN";
		}
		if (fieldOptionList.contains("GENERIC")) {
			operandType = "GENERIC";
		}
		if (fieldOptionList.contains("REFLECT")) {
			operandType = "REFLECT";
		}
		//
		if (operandType.equals("REFLECT")) {
			isReflect = true;
		}
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = wrkStr;
		}
		jLabelField = new JLabel(fieldCaption);
		jLabelField.setPreferredSize(new Dimension(90, 20));
		jLabelField.setHorizontalAlignment(SwingConstants.RIGHT);
		FontMetrics metrics1 = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		if (metrics1.stringWidth(fieldCaption) > 90) {
			jLabelField.setFont(new java.awt.Font("Dialog", 0, 12));
			metrics1 = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 12));
			if (metrics1.stringWidth(fieldCaption) > 90) {
				jLabelField.setFont(new java.awt.Font("Dialog", 0, 10));
			} else {
				jLabelField.setFont(new java.awt.Font("Dialog", 0, 12));
			}
		} else {
			jLabelField.setFont(new java.awt.Font("Dialog", 0, 14));
		}
		//
		jPanelField.setLayout(null);
		this.setLayout(new BorderLayout());
		this.add(jLabelField, BorderLayout.WEST);
		this.add(jPanelField, BorderLayout.CENTER);
		//
		int fieldWidthMax = (int)dialog_.getFilterWidth() - 100;
		//
//		componentType = "TEXTFIELD";
//		xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions);
//		if (xFTextField.getWidth() > fieldWidthMax) {
//			xFTextField.setBounds(new Rectangle(5, 0, fieldWidthMax, xFTextField.getHeight()));
//		} else {
//			xFTextField.setLocation(5, 0);
//		}
//		xFTextField.setValue(getDefaultValue());
//		component = xFTextField;
		//
		////////////////////////////////////////////////////////////////////////////////
		// Steps to check BOOLEAN should be here because the field can be specified   //
		// as PROMPT_LIST1/2. This happens because BOOLEAN is placed recently         //
		////////////////////////////////////////////////////////////////////////////////
		if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN").equals("")) {
			componentType = "BOOLEAN";
			xFCheckBox = new XFCheckBox(dataTypeOptions);
			xFCheckBox.setLocation(5, 0);
			xFCheckBox.setEditable(true);
			xFCheckBox.setValue(getDefaultValue());
			component = xFCheckBox;
		} else {
			//
			////////////////////////////////////////////////////////////////////////////////
			// PROMPT_LIST1 is the list with blank row, PROMPT_LIST2 is without blank row //
			////////////////////////////////////////////////////////////////////////////////
			if (fieldOptionList.contains("PROMPT_LIST1") || fieldOptionList.contains("PROMPT_LIST2")) {
				//
				FontMetrics metrics2 = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 12));
				int valueIndex = -1;
				int selectIndex = 0;
				String wrkText, wrkKey;
				//
				wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
				if (!wrkStr.equals("")) {
					//
					componentType = "KUBUN_LIST";
					Object defaultValue = getDefaultValue();
					jComboBox = new JComboBox();
					component = jComboBox;
					//
					fieldWidthMax = fieldWidthMax - 28;
					int fieldWidth = 20;
					//
					if (fieldOptionList.contains("PROMPT_LIST1")) {
						valueIndex++;
						keyValueList.add("");
						jComboBox.addItem("");
					}
					//
					Statement statement = null;
					ResultSet result = null;
					try {
						statement = dialog_.getSession().getConnection().createStatement();
						result = statement.executeQuery("select * from " + dialog_.getSession().getTableNameOfUserVariants() + " where IDUSERKUBUN = '" + wrkStr + "' order by SQLIST");
						while (result.next()) {
							valueIndex++;
							//
							wrkKey = result.getString("KBUSERKUBUN").trim();
							keyValueList.add(wrkKey);
							if (wrkKey.equals(defaultValue)) {
								selectIndex = valueIndex;
							}
							wrkText = result.getString("TXUSERKUBUN").trim();
							jComboBox.addItem(wrkText);
							if (metrics2.stringWidth(wrkText) > fieldWidth) {
								fieldWidth = metrics2.stringWidth(wrkText);
							}
						}
						//
						if (fieldWidth > fieldWidthMax) {
							fieldWidth = fieldWidthMax;
						}
						jComboBox.setBounds(new Rectangle(5, 0, fieldWidth + 28, 24));
						jComboBox.setFont(new java.awt.Font("Dialog", 0, 12));
						jComboBox.setSelectedIndex(selectIndex);
					} catch(SQLException e) {
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
						} catch (SQLException ex) {
							ex.printStackTrace(dialog_.getExceptionStream());
						}
					}
					//
				} else {
					//
					wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "VALUES");
					if (!wrkStr.equals("")) {
						//
						componentType = "VALUES_LIST";
						Object defaultValue = getDefaultValue();
						jComboBox = new JComboBox();
						component = jComboBox;
						//
						fieldWidthMax = fieldWidthMax - 28;
						int fieldWidth = 20;
						//
						if (fieldOptionList.contains("PROMPT_LIST1")) {
							valueIndex++;
							jComboBox.addItem("");
						}
						//
						StringTokenizer workTokenizer = new StringTokenizer(wrkStr, ";" );
						while (workTokenizer.hasMoreTokens()) {
							valueIndex++;
							wrkKey = workTokenizer.nextToken();
							jComboBox.addItem(wrkKey);
							if (wrkKey.equals(defaultValue)) {
								selectIndex = valueIndex;
							}
							if (metrics2.stringWidth(wrkKey) > fieldWidth) {
								fieldWidth = metrics2.stringWidth(wrkKey);
							}
						}
						if (fieldWidth > fieldWidthMax) {
							fieldWidth = fieldWidthMax;
						}
						jComboBox.setBounds(new Rectangle(5, 0, fieldWidth + 28, 24));
						jComboBox.setFont(new java.awt.Font("Dialog", 0, 12));
						jComboBox.setSelectedIndex(selectIndex);
						//
					} else {
						//
						componentType = "RECORDS_LIST";
						Object defaultValue = getDefaultValue();
						jComboBox = new JComboBox();
						component = jComboBox;
						//
						fieldWidthMax = fieldWidthMax - 28;
						int fieldWidth = 20;
						//
						ArrayList<XF110_ReferTable> referTableList = dialog_.getReferTableList();
						for (int i = 0; i < referTableList.size(); i++) {
							if (referTableList.get(i).getTableID().equals(tableID)) {
								if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias)) {
									//
									if (fieldOptionList.contains("PROMPT_LIST1")) {
										valueIndex++;
										jComboBox.addItem("");
									}
									//
									Statement statement = null;
									ResultSet result = null;
									try {
										statement = dialog_.getSession().getConnection().createStatement();
										result = statement.executeQuery(referTableList.get(i).getSelectSQLToList(fieldID));
										while (result.next()) {
											valueIndex++;
											wrkKey = result.getString(fieldID).trim();
											jComboBox.addItem(wrkKey);
											if (wrkKey.equals(defaultValue)) {
												selectIndex = valueIndex;
											}
											if (metrics2.stringWidth(wrkKey) > fieldWidth) {
												fieldWidth = metrics2.stringWidth(wrkKey);
											}
										}
										//
										if (fieldWidth > fieldWidthMax) {
											fieldWidth = fieldWidthMax;
										}
										jComboBox.setBounds(new Rectangle(5, 0, fieldWidth + 28, 24));
										jComboBox.setFont(new java.awt.Font("Dialog", 0, 12));
										jComboBox.setSelectedIndex(selectIndex);
										//
									} catch(SQLException e) {
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
										} catch (SQLException ex) {
											ex.printStackTrace(dialog_.getExceptionStream());
										}
									}
									break;
								}
							}
						}
					}
				}
				//
			} else {
				wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
				if (!wrkStr.equals("")) {
					componentType = "PROMPT_CALL";
					org.w3c.dom.Element promptFunctionElement = null;
					NodeList functionList = dialog_.getSession().getFunctionList();
					for (int k = 0; k < functionList.getLength(); k++) {
						workElement = (org.w3c.dom.Element)functionList.item(k);
						if (workElement.getAttribute("ID").equals(wrkStr)) {
							promptFunctionElement = workElement;
							break;
						}
					}
					xFPromptCall = new XF110_PromptCallField(fieldElement, promptFunctionElement, dialog_);
					xFPromptCall.setLocation(5, 0);
					xFPromptCall.setValue(getDefaultValue());
					component = xFPromptCall;
				} else {
					//
					if (dataType.equals("DATE")) {
						componentType = "DATE";
						xFDateField = new XFDateField(dialog_.getSession());
						xFDateField.setLocation(5, 0);
						xFDateField.setEditable(true);
						xFDateField.setValue(getDefaultValue());
						component = xFDateField;
					} else {
						if (dataTypeOptionList.contains("YMONTH")) {
							componentType = "YMONTH";
							xFYMonthBox = new XFYMonthBox(dialog_.getSession());
							xFYMonthBox.setLocation(5, 0);
							xFYMonthBox.setEditable(true);
							xFYMonthBox.setValue(getDefaultValue());
							component = xFYMonthBox;
						} else {
							if (dataTypeOptionList.contains("MSEQ")) {
								componentType = "MSEQ";
								xFMSeqBox = new XFMSeqBox(dialog_.getSession());
								xFMSeqBox.setLocation(5, 0);
								xFMSeqBox.setEditable(true);
								xFMSeqBox.setValue(getDefaultValue());
								component = xFMSeqBox;
							} else {
								if (dataTypeOptionList.contains("FYEAR")) {
									componentType = "FYEAR";
									xFFYearBox = new XFFYearBox(dialog_.getSession());
									xFFYearBox.setLocation(5, 0);
									xFFYearBox.setEditable(true);
									xFFYearBox.setValue(getDefaultValue());
									component = xFFYearBox;
								} else {
									componentType = "TEXTFIELD";
									xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions);
									if (xFTextField.getWidth() > fieldWidthMax) {
										xFTextField.setBounds(new Rectangle(5, 0, fieldWidthMax, xFTextField.getHeight()));
									} else {
										xFTextField.setLocation(5, 0);
									}
									xFTextField.setValue(getDefaultValue());
									component = xFTextField;
								}
							}
						}
					}
				}
			}
		}
		//
		if (fieldOptionList.contains("NON_EDITABLE")) {
			this.setEditable(false);
		}
		//
		if (decimalSize > 0) {
			wrkStr = "<html>" + fieldName + " " + tableAlias + "." + fieldID + " (" + dataSize + "," + decimalSize + ")<br>" + fieldRemarks;
		} else {
			wrkStr = "<html>" + fieldName + " " + tableAlias + "." + fieldID + " (" + dataSize + ")<br>" + fieldRemarks;
		}
		this.setToolTipText(wrkStr);
		component.setToolTipText(wrkStr);
		//
		component.addKeyListener(new XF110_Component_keyAdapter(dialog));
		jPanelField.add(component);
	}

	Object getDefaultValue() {
		Object value = "";
		//
		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "VALUE");
		if (wrkStr.equals("")) {
			if (componentType.equals("BOOLEAN")) {
				value = xFCheckBox.getFalseValue();
			}
		} else {
			value = XFUtility.getDefaultValueOfFilterField(wrkStr, dialog_.getSession());
			if (value == null) {
				JOptionPane.showMessageDialog(this, tableID + "." + fieldID + res.getString("FunctionError46") + "\n" + wrkStr);
			}
		}
		//
		return value;
	}

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}

	public String getCaption(){
		return fieldCaption;
	}

	public String getCaptionAndValue(){
		String value = fieldCaption + ":";
		String wrk = "";
		//
		if (componentType.equals("TEXTFIELD")) {
			wrk = (String)xFTextField.getExternalValue();
		}
		if (componentType.equals("KUBUN_LIST") || componentType.equals("VALUES_LIST") || componentType.equals("RECORDS_LIST")) {
			wrk = jComboBox.getSelectedItem().toString();
		}
		if (componentType.equals("BOOLEAN")) {
			wrk = (String)xFCheckBox.getExternalValue();
		}
		if (componentType.equals("YMONTH")) {
			wrk = (String)xFYMonthBox.getExternalValue();
		}
		if (componentType.equals("MSEQ")) {
			wrk = (String)xFMSeqBox.getExternalValue();
		}
		if (componentType.equals("FYEAR")) {
			wrk = (String)xFFYearBox.getExternalValue();
		}
		if (componentType.equals("DATE")) {
			wrk = (String)xFDateField.getExternalValue();
		}
		if (componentType.equals("PROMPT_CALL")) {
			wrk = (String)xFPromptCall.getExternalValue();
		}
		//
		if (wrk.equals("")) {
			value = value + "* ";
		} else {
			value = value + wrk + " ";
		}
		//
		return value;
	}

	public String getDataType(){
		return dataType;
	}
	
	public void requestFocus(){
		super.requestFocus();
		component.requestFocus();
	}
	
	public String getFieldID(){
		return fieldID;
	}
	
	public String getTableAlias(){
		return tableAlias;
	}
	
	public String getTableID(){
		return tableID;
	}
	
	public String getDataSourceName(){
		return tableAlias + "." + fieldID;
	}

	public boolean isValidatedWithParmMapValue(HashMap<String, Object> parmMap){
		boolean isValidated = true;
		String mapKey = this.getTableID() + "." + this.getFieldID();
		//
		if (parmMap != null && parmMap.containsKey(mapKey)) {
			//
			Object mapValue = parmMap.get(mapKey);
			//
			if (componentType.equals("TEXTFIELD")) {
				xFTextField.setText(mapValue.toString());
			}
			//
			if (componentType.equals("DATE")) {
				xFDateField.setValue(mapValue.toString());
			}
			//
			if (componentType.equals("YMONTH")) {
				xFYMonthBox.setValue(mapValue.toString());
			}
			//
			if (componentType.equals("MSEQ")) {
				xFMSeqBox.setValue(mapValue.toString());
			}
			//
			if (componentType.equals("FYEAR")) {
				xFFYearBox.setValue(mapValue.toString());
			}
			//
			if (componentType.equals("KUBUN_LIST")) {
				jComboBox.setSelectedIndex(keyValueList.indexOf(mapValue.toString()));
			}
			//
			if (componentType.equals("VALUES_LIST") || componentType.equals("RECORDS_LIST")) {
				for (int i = 0; i < jComboBox.getItemCount(); i++) {
					if (jComboBox.getItemAt(i).equals(mapValue.toString())) {
							jComboBox.setSelectedIndex(i);
							break;
					}
				}
			}
			//
			if (componentType.equals("BOOLEAN")) {
				xFCheckBox.setValue(keyValueList.indexOf(mapValue.toString()));
			}
			//
			if (componentType.equals("PROMPT_CALL")) {
				xFPromptCall.setValue(mapValue.toString());
			}
			//
			if (mapValue.toString().equals("")) {
				isValidated = false;
			}
			//
		} else {
			if (!this.isEditable_) {
				isValidated = false;
			}
		}
		//
		return isValidated;
	}

	public void setValue(Object value){
		//
		if (componentType.equals("TEXTFIELD")) {
			xFTextField.setText(value.toString().trim());
		}
		//
		if (componentType.equals("BOOLEAN")) {
			xFCheckBox.setValue(value.toString().trim());
		}
		//
		if (componentType.equals("DATE")) {
			xFDateField.setValue(value.toString());
			xFDateField.setEditable(false);
		}
		//
		if (componentType.equals("YMONTH")) {
			xFYMonthBox.setValue(value.toString());
		}
		//
		if (componentType.equals("MSEQ")) {
			xFMSeqBox.setValue(value.toString());
		}
		//
		if (componentType.equals("FYEAR")) {
			xFFYearBox.setValue(value.toString());
		}
		//
		if (componentType.equals("KUBUN_LIST")) {
			jComboBox.setSelectedIndex(keyValueList.indexOf(value.toString()));
		}
		//
		if (componentType.equals("VALUES_LIST") || componentType.equals("RECORDS_LIST")) {
			for (int i = 0; i < jComboBox.getItemCount(); i++) {
				if (jComboBox.getItemAt(i).equals(value.toString())) {
					jComboBox.setSelectedIndex(i);
					break;
				}
			}
		}
		//
		if (componentType.equals("PROMPT_CALL")) {
			xFPromptCall.setValue(value.toString());
		}
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		//
		if (componentType.equals("TEXTFIELD")) {
			xFTextField.setEditable(isEditable_);
		}
		//
		if (componentType.equals("BOOLEAN")) {
			xFCheckBox.setEditable(isEditable_);
		}
		//
		if (componentType.equals("DATE")) {
			xFDateField.setEditable(isEditable_);
		}
		//
		if (componentType.equals("YMONTH")) {
			xFYMonthBox.setEditable(isEditable_);
		}
		//
		if (componentType.equals("MSEQ")) {
			xFMSeqBox.setEditable(isEditable_);
		}
		//
		if (componentType.equals("FYEAR")) {
			xFFYearBox.setEditable(isEditable_);
		}
		//
		if (componentType.equals("KUBUN_LIST") || componentType.equals("VALUES_LIST") || componentType.equals("RECORDS_LIST")) {
			jComboBox.setEditable(isEditable_);
		}
		//
		if (componentType.equals("PROMPT_CALL")) {
			xFPromptCall.setEditable(isEditable_);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}

	public boolean isReflect(){
		return isReflect;
	}
	
	public boolean isValidated(){
		boolean validated = false;
		//
		XF110_Column columnField = dialog_.getColumnObjectByID(this.getTableID(), this.getTableAlias(), this.getFieldID());
		//
		if (this.isReflect) {
			//
			if (componentType.equals("TEXTFIELD")) {
				columnField.setValue((String)xFTextField.getInternalValue());
			}
			//
			if (componentType.equals("KUBUN_LIST") || componentType.equals("RECORDS_LIST") || componentType.equals("VALUES_LIST")) {
				columnField.setValue((String)jComboBox.getSelectedItem());
			}
			//
			if (componentType.equals("BOOLEAN")) {
				columnField.setValue((String)xFCheckBox.getInternalValue());
			}
			//
			if (componentType.equals("DATE")) {
				columnField.setValue(xFDateField.getInternalValue());
			}
			//
			if (componentType.equals("YMONTH")) {
				columnField.setValue(xFYMonthBox.getInternalValue());
			}
			//
			if (componentType.equals("MSEQ")) {
				columnField.setValue(xFMSeqBox.getInternalValue());
			}
			//
			if (componentType.equals("FYEAR")) {
				columnField.setValue(xFFYearBox.getInternalValue());
			}
			//
			validated = true;
			//
		} else {
			//
			String stringResultValue = "";
			String stringFilterValue = "";
			double doubleResultValue = 0;
			double doubleFilterValue = 0;
			//
			if (columnField.isReadyToValidate()) {
				if (componentType.equals("TEXTFIELD")) {
					if (columnField.getInternalValue() != null) {
						stringResultValue = columnField.getInternalValue().toString().trim();
					} 
					stringFilterValue = (String)xFTextField.getInternalValue();
					//
					if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
						if (stringFilterValue.equals("")) {
							validated = true;
						} else {
							doubleResultValue = Double.parseDouble(stringResultValue);
							String wrk = XFUtility.getStringNumber(stringFilterValue);
							if (!wrk.equals("")) {
								doubleFilterValue = Double.parseDouble(wrk);
							}
							if (operandType.equals("EQ")) {
								if (doubleResultValue == doubleFilterValue) {
									validated = true;
								}
							}
							if (operandType.equals("GE")) {
								if (doubleResultValue >= doubleFilterValue) {
									validated = true;
								}
							}
							if (operandType.equals("GT")) {
								if (doubleResultValue > doubleFilterValue) {
									validated = true;
								}
							}
							if (operandType.equals("LE")) {
								if (doubleResultValue <= doubleFilterValue) {
									validated = true;
								}
							}
							if (operandType.equals("LT")) {
								if (doubleResultValue < doubleFilterValue) {
									validated = true;
								}
							}
						}
					} else {
						if (this.getBasicType().equals("DATE") || this.getBasicType().equals("TIME") || this.getBasicType().equals("DATETIME")) {
							if (stringFilterValue.equals("")) {
								validated = true;
							} else {
								stringResultValue = XFUtility.getStringNumber(stringResultValue).replace("-", "");
								doubleResultValue = Double.parseDouble(stringResultValue);
								stringFilterValue = XFUtility.getStringNumber(stringFilterValue).replace("-", "");
								if (!stringFilterValue.equals("")) {
									doubleFilterValue = Double.parseDouble(stringFilterValue);
								}
								if (operandType.equals("EQ")) {
									if (doubleResultValue == doubleFilterValue) {
										validated = true;
									}
								}
								if (operandType.equals("GE") && !operandType.equals("GENERIC")) {
									if (doubleResultValue >= doubleFilterValue) {
										validated = true;
									}
								}
								if (operandType.equals("GT")) {
									if (doubleResultValue > doubleFilterValue) {
										validated = true;
									}
								}
								if (operandType.equals("LE")) {
									if (doubleResultValue <= doubleFilterValue) {
										validated = true;
									}
								}
								if (operandType.equals("LT")) {
									if (doubleResultValue < doubleFilterValue) {
										validated = true;
									}
								}
								if (operandType.equals("GENERIC")) {
									int lengthResultValue = stringResultValue.length();
									int lengthFieldValue = stringFilterValue.length();
									if (lengthResultValue >= lengthFieldValue) {
										String wrk = stringResultValue.substring(0, lengthFieldValue);
										if (wrk.equals(stringFilterValue)) {
											validated = true;
										}
									}
								}
							}
						} else {
							if (stringFilterValue.equals("")) {
								validated = true;
							} else {
								if (operandType.equals("EQ")) {
									if (stringResultValue.equals(stringFilterValue)) {
										validated = true;
									}
								}
								if (operandType.equals("SCAN")) {
									if (stringResultValue.contains(stringFilterValue)) {
										validated = true;
									}
								}
								if (operandType.equals("GENERIC")) {
									int lengthResultValue = stringResultValue.length();
									int lengthFieldValue = stringFilterValue.length();
									if (lengthResultValue >= lengthFieldValue) {
										String wrk = stringResultValue.substring(0, lengthFieldValue);
										if (wrk.equals(stringFilterValue)) {
											validated = true;
										}
									}
								}
							}
						}
					}
				}
				//
				if (componentType.equals("KUBUN_LIST") || componentType.equals("RECORDS_LIST")) {
					String fieldValue = (String)jComboBox.getSelectedItem();
					if (fieldValue.equals("")) {
						validated = true;
					} else {
						String resultValue = (String)columnField.getExternalValue();
						if (resultValue.equals(fieldValue)) {
							validated = true;
						} else {
							if (fieldValue.equals("NULL") && resultValue.equals("")) {
								validated = true;
							}
							if (fieldValue.equals("!NULL") && !resultValue.equals("")) {
								validated = true;
							}
						}
					}
				}
				//
				if (componentType.equals("VALUES_LIST")) {
					String fieldValue = (String)jComboBox.getSelectedItem();
					if (fieldValue.equals("")) {
						validated = true;
					} else {
						String resultValue = (String)columnField.getInternalValue();
						if (resultValue.equals(fieldValue)) {
							validated = true;
						}
					}
				}
				//
				if (componentType.equals("BOOLEAN")) {
					String fieldValue = (String)xFCheckBox.getInternalValue();
					String resultValue = (String)columnField.getInternalValue();
					if (resultValue.equals(fieldValue)) {
						validated = true;
					}
				}
				//
				if (componentType.equals("DATE")) {
					stringFilterValue = (String)xFDateField.getInternalValue();
					if (stringFilterValue == null) {
						validated = true;
					} else {
						if (columnField.getInternalValue() != null) {
							stringResultValue = columnField.getInternalValue().toString().trim();
							stringResultValue = XFUtility.getStringNumber(stringResultValue).replace("-", "");
						} 
						doubleResultValue = Double.parseDouble(stringResultValue);
						stringFilterValue = XFUtility.getStringNumber(stringFilterValue).replace("-", "");
						if (!stringFilterValue.equals("")) {
							doubleFilterValue = Double.parseDouble(stringFilterValue);
						}
						if (operandType.equals("EQ")) {
							if (doubleResultValue == doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("GE")) {
							if (doubleResultValue >= doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("GT")) {
							if (doubleResultValue > doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("LE")) {
							if (doubleResultValue <= doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("LT")) {
							if (doubleResultValue < doubleFilterValue) {
								validated = true;
							}
						}

					}
				}
				//
				if (componentType.equals("YMONTH")) {
					stringFilterValue = (String)xFYMonthBox.getInternalValue();
					if (stringFilterValue.equals("")) {
						validated = true;
					} else {
						if (columnField.getInternalValue() != null) {
							stringResultValue = columnField.getInternalValue().toString().trim();
							stringResultValue = XFUtility.getStringNumber(stringResultValue).replace("/", "");
						} 
						doubleResultValue = Double.parseDouble(stringResultValue);
						if (!stringFilterValue.equals("")) {
							doubleFilterValue = Double.parseDouble(stringFilterValue);
						}
						if (operandType.equals("EQ")) {
							if (doubleResultValue == doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("GE") && !operandType.equals("GENERIC")) {
							if (doubleResultValue >= doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("GT")) {
							if (doubleResultValue > doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("LE")) {
							if (doubleResultValue <= doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("LT")) {
							if (doubleResultValue < doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("GENERIC")) {
							int lengthResultValue = stringResultValue.length();
							int lengthFieldValue = stringFilterValue.length();
							if (lengthResultValue >= lengthFieldValue) {
								String wrk = stringResultValue.substring(0, lengthFieldValue);
								if (wrk.equals(stringFilterValue)) {
									validated = true;
								}
							}
						}

					}
				}
				//
				if (componentType.equals("MSEQ")) {
					stringFilterValue = (String)xFMSeqBox.getInternalValue();
					if (stringFilterValue.equals("0")) {
						validated = true;
					} else {
						if (columnField.getInternalValue() != null) {
							stringResultValue = columnField.getInternalValue().toString().trim();
							stringResultValue = XFUtility.getStringNumber(stringResultValue).replace("/", "");
						} 
						doubleResultValue = Double.parseDouble(stringResultValue);
						if (!stringFilterValue.equals("")) {
							doubleFilterValue = Double.parseDouble(stringFilterValue);
						}
						if (operandType.equals("EQ")) {
							if (doubleResultValue == doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("GE") && !operandType.equals("GENERIC")) {
							if (doubleResultValue >= doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("GT")) {
							if (doubleResultValue > doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("LE")) {
							if (doubleResultValue <= doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("LT")) {
							if (doubleResultValue < doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("GENERIC")) {
							int lengthResultValue = stringResultValue.length();
							int lengthFieldValue = stringFilterValue.length();
							if (lengthResultValue >= lengthFieldValue) {
								String wrk = stringResultValue.substring(0, lengthFieldValue);
								if (wrk.equals(stringFilterValue)) {
									validated = true;
								}
							}
						}

					}
				}
				//
				if (componentType.equals("FYEAR")) {
					stringFilterValue = (String)xFFYearBox.getInternalValue();
					if (stringFilterValue.equals("0")) {
						validated = true;
					} else {
						if (columnField.getInternalValue() != null) {
							stringResultValue = columnField.getInternalValue().toString().trim();
							stringResultValue = XFUtility.getStringNumber(stringResultValue).replace("/", "");
						} 
						doubleResultValue = Double.parseDouble(stringResultValue);
						if (!stringFilterValue.equals("")) {
							doubleFilterValue = Double.parseDouble(stringFilterValue);
						}
						if (operandType.equals("EQ")) {
							if (doubleResultValue == doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("GE") && !operandType.equals("GENERIC")) {
							if (doubleResultValue >= doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("GT")) {
							if (doubleResultValue > doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("LE")) {
							if (doubleResultValue <= doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("LT")) {
							if (doubleResultValue < doubleFilterValue) {
								validated = true;
							}
						}
						if (operandType.equals("GENERIC")) {
							int lengthResultValue = stringResultValue.length();
							int lengthFieldValue = stringFilterValue.length();
							if (lengthResultValue >= lengthFieldValue) {
								String wrk = stringResultValue.substring(0, lengthFieldValue);
								if (wrk.equals(stringFilterValue)) {
									validated = true;
								}
							}
						}

					}
				}
				//
				if (componentType.equals("PROMPT_CALL")) {
					if (columnField.getInternalValue() != null) {
						stringResultValue = columnField.getInternalValue().toString().trim();
					} 
					stringFilterValue = (String)xFPromptCall.getInternalValue();
					//
					if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
						if (stringFilterValue.equals("")) {
							validated = true;
						} else {
							doubleResultValue = Double.parseDouble(stringResultValue);
							String wrk = XFUtility.getStringNumber(stringFilterValue);
							if (!wrk.equals("")) {
								doubleFilterValue = Double.parseDouble(wrk);
							}
							if (operandType.equals("EQ")) {
								if (doubleResultValue == doubleFilterValue) {
									validated = true;
								}
							}
							if (operandType.equals("GE")) {
								if (doubleResultValue >= doubleFilterValue) {
									validated = true;
								}
							}
							if (operandType.equals("GT")) {
								if (doubleResultValue > doubleFilterValue) {
									validated = true;
								}
							}
							if (operandType.equals("LE")) {
								if (doubleResultValue <= doubleFilterValue) {
									validated = true;
								}
							}
							if (operandType.equals("LT")) {
								if (doubleResultValue < doubleFilterValue) {
									validated = true;
								}
							}
						}
					} else {
						if (this.getBasicType().equals("DATE") || this.getBasicType().equals("TIME") || this.getBasicType().equals("DATETIME")) {
							if (stringFilterValue.equals("")) {
								validated = true;
							} else {
								stringResultValue = XFUtility.getStringNumber(stringResultValue).replace("-", "");
								doubleResultValue = Double.parseDouble(stringResultValue);
								stringFilterValue = XFUtility.getStringNumber(stringFilterValue).replace("-", "");
								if (!stringFilterValue.equals("")) {
									doubleFilterValue = Double.parseDouble(stringFilterValue);
								}
								if (operandType.equals("EQ")) {
									if (doubleResultValue == doubleFilterValue) {
										validated = true;
									}
								}
								if (operandType.equals("GE") && !operandType.equals("GENERIC")) {
									if (doubleResultValue >= doubleFilterValue) {
										validated = true;
									}
								}
								if (operandType.equals("GT")) {
									if (doubleResultValue > doubleFilterValue) {
										validated = true;
									}
								}
								if (operandType.equals("LE")) {
									if (doubleResultValue <= doubleFilterValue) {
										validated = true;
									}
								}
								if (operandType.equals("LT")) {
									if (doubleResultValue < doubleFilterValue) {
										validated = true;
									}
								}
								if (operandType.equals("GENERIC")) {
									int lengthResultValue = stringResultValue.length();
									int lengthFieldValue = stringFilterValue.length();
									if (lengthResultValue >= lengthFieldValue) {
										String wrk = stringResultValue.substring(0, lengthFieldValue);
										if (wrk.equals(stringFilterValue)) {
											validated = true;
										}
									}
								}
							}
						} else {
							if (stringFilterValue.equals("")) {
								validated = true;
							} else {
								if (operandType.equals("EQ")) {
									if (stringResultValue.equals(stringFilterValue)) {
										validated = true;
									}
								}
								if (operandType.equals("SCAN")) {
									if (stringResultValue.contains(stringFilterValue)) {
										validated = true;
									}
								}
								if (operandType.equals("GENERIC")) {
									int lengthResultValue = stringResultValue.length();
									int lengthFieldValue = stringFilterValue.length();
									if (lengthResultValue >= lengthFieldValue) {
										String wrk = stringResultValue.substring(0, lengthFieldValue);
										if (wrk.equals(stringFilterValue)) {
											validated = true;
										}
									}
								}
							}
						}
					}
				}
			} else {
				validated = true;
			}
		}
		//
		return validated;
	}
}

class XF110_PromptCallField extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private int rows_ = 1;
	private XFTextField xFTextField;
	private JButton jButton = new JButton();
	private boolean isEditable = true;
    private XF110 dialog_;
    private org.w3c.dom.Element fieldElement_;
    private org.w3c.dom.Element functionElement_;
    private ArrayList<XF110_ReferTable> referTableList_;
    private String oldValue = "";
    private Color normalColor;
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();

	public XF110_PromptCallField(org.w3c.dom.Element fieldElement, org.w3c.dom.Element functionElement, XF110 dialog){
		//
		super();
		//
		fieldElement_ = fieldElement;
		functionElement_ = functionElement;
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
		xFTextField = new XFTextField(XFUtility.getBasicTypeOf(dataType), dataSize, decimalSize, dataTypeOptions, fieldOptions);
		xFTextField.setLocation(5, 0);
		xFTextField.setEditable(false);
		xFTextField.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent event) {
				xFTextField.selectAll();
			} 
			public void focusLost(FocusEvent event) {
				xFTextField.select(0, 0);
			} 
		});
		xFTextField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (isEditable && event.getKeyCode() == KeyEvent.VK_DELETE) {
					xFTextField.setText("");
					xFTextField.setFocusable(false);
				}
			} 
		});
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
		jButton.setText("...");
		jButton.setFont(new java.awt.Font("Dialog", 0, 11));
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value;
				//
				HashMap<String, Object> fieldValuesMap = new HashMap<String, Object>();
				for (int i = 0; i < fieldsToPutList_.size(); i++) {
					value = dialog_.getFilterObjectByName(fieldsToPutList_.get(i));
					if (value != null) {
						fieldValuesMap.put(fieldsToPutToList_.get(i), value);
					}
				}
				//
				HashMap<String, Object> returnMap = dialog_.getSession().getFunction().execute(functionElement_, fieldValuesMap);
				if (!returnMap.get("RETURN_CODE").equals("99")) {
					//
					HashMap<String, Object> fieldsToGetMap = new HashMap<String, Object>();
					for (int i = 0; i < fieldsToGetList_.size(); i++) {
						value = returnMap.get(fieldsToGetList_.get(i));
						if (value != null) {
							fieldsToGetMap.put(fieldsToGetToList_.get(i), value);
						}
					}
					for (int i = 0; i < dialog_.getFilterList().size(); i++) {
						value = fieldsToGetMap.get(dialog_.getFilterList().get(i).getDataSourceName());
						if (value != null && dialog_.getFilterList().get(i).isEditable()) {
							dialog_.getFilterList().get(i).setValue(value);
						}
					}
					//
					if (!xFTextField.getText().equals("")) {
						xFTextField.setFocusable(true);
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
		int fieldWidth = (int)dialog_.getFilterWidth() - 100 - 27;
		if (fieldWidth < xFTextField.getWidth()) {
			this.setSize(new Dimension(fieldWidth + 27, XFUtility.FIELD_UNIT_HEIGHT));
		} else {
			this.setSize(new Dimension(xFTextField.getWidth() + 27, XFUtility.FIELD_UNIT_HEIGHT));
		}
		this.setLayout(new BorderLayout());
		this.add(xFTextField, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
	}

	public void setEditable(boolean editable) {
		jButton.setEnabled(editable);
		isEditable = editable;
	}

	public void requestFocus() {
		xFTextField.requestFocus();
	}

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
	
	public void setWidth(int width) {
		xFTextField.setWidth(width - 27);
	}

	public void setBackground(Color color) {
		if (xFTextField != null) {
			if (color.equals(XFUtility.ACTIVE_COLOR)) {
				xFTextField.setBackground(normalColor);
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
}

//class XF110_Cell extends Object {
//	private static final long serialVersionUID = 1L;
//	private Object value_ = null;
//	private Color color_ = null;
//	public XF110_Cell(Object value, Color color) {
//		value_ = value;
//		color_ = color;
//	}
//	public Object getValue() {
//		return value_;
//	}
//	public Color getColor() {
//		return color_;
//	}
//}

class XF110_RowNumber extends Object {
	private int number_;
	private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
	private int updateCounter_ = 0;
	private DecimalFormat integerFormat = new DecimalFormat("0000");
	//
	public XF110_RowNumber(int num, HashMap<String, Object> keyMap, int updateCounter) {
		number_ = num;
		keyMap_ = keyMap;
		updateCounter_ = updateCounter;
	}
	public String toString() {
		return integerFormat.format(number_);
	}
	public HashMap<String, Object> getKeyMap() {
		return keyMap_;
	}
	public int getUpdateCounter() {
		return updateCounter_;
	}
}

class XF110_Column extends Object implements XFScriptableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element functionColumnElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF110 dialog_ = null;
	private String tableAlias = "";
	private String tableID = "";
	private String dataType = "";
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private String fieldID = "";
	private String fieldName = "";
	private String fieldRemarks = "";
	private String fieldCaption = "";
	private String fieldOptions = "";
	private ArrayList<String> fieldOptionList;
	private int dataSize = 5;
	private int decimalSize = 0;
	private int fieldWidth = 50;
	private int columnIndex = -1;
	private boolean isVisibleOnPanel = true;
	private boolean isVirtualField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private boolean isReadyToValidate = false;
	private DecimalFormat integerFormat = new DecimalFormat("#,##0");
	private DecimalFormat floatFormat0 = new DecimalFormat("#,##0");
	private DecimalFormat floatFormat1 = new DecimalFormat("#,##0.0");
	private DecimalFormat floatFormat2 = new DecimalFormat("#,##0.00");
	private DecimalFormat floatFormat3 = new DecimalFormat("#,##0.000");
	private DecimalFormat floatFormat4 = new DecimalFormat("#,##0.0000");
	private DecimalFormat floatFormat5 = new DecimalFormat("#,##0.00000");
	private DecimalFormat floatFormat6 = new DecimalFormat("#,##0.000000");
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private Object value_ = null;
	private Color foreground = Color.black;

	public XF110_Column(org.w3c.dom.Element functionColumnElement, XF110 dialog){
		super();
		//
		String wrkStr;
		functionColumnElement_ = functionColumnElement;
		dialog_ = dialog;
		fieldOptions = functionColumnElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);
		if (fieldOptionList.contains("LIST2")) {
			isVisibleOnPanel = false;
		}
		//
		StringTokenizer workTokenizer = new StringTokenizer(functionColumnElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		tableID = dialog_.getTableIDOfTableAlias(tableAlias);
		fieldID =workTokenizer.nextToken();
		//
		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
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
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		//if (dataSize > 50) {
		//	dataSize = 50;
		//}
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
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
		JLabel jLabel = new JLabel();
		FontMetrics metrics = jLabel.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = wrkStr;
		}
		int captionWidth = metrics.stringWidth(fieldCaption) + 18;
		//
		String basicType = this.getBasicType();
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			Statement statement = null;
			ResultSet result = null;
			try {
				String wrk = "";
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
			} catch(SQLException e) {
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
				} catch (SQLException ex) {
					ex.printStackTrace(dialog_.getExceptionStream());
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

	public XF110_Column(String tableID, String tableAlias, String fieldID, XF110 dialog){
		super();
		//
		String wrkStr;
		functionColumnElement_ = null;
		//
		dialog_ = dialog;
		fieldOptions = "";
		this.tableAlias = "";
		this.tableID = tableID;
		if (tableAlias.equals("")) {
			this.tableAlias = tableID;
		} else {
			this.tableAlias = tableAlias;
		}
		this.fieldID = fieldID;
		//
		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID + "." + fieldID + res.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
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
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			Statement statement = null;
			ResultSet result = null;
			try {
				String wrk = "";
				statement = dialog_.getSession().getConnection().createStatement();
				result = statement.executeQuery("select * from " + dialog_.getSession().getTableNameOfUserVariants() + " where IDUSERKUBUN = '" + wrkStr + "'");
				while (result.next()) {
					kubunValueList.add(result.getString("KBUSERKUBUN").trim());
					wrk = result.getString("TXUSERKUBUN").trim();
					kubunTextList.add(wrk);
				}
			} catch(SQLException e) {
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
				} catch (SQLException ex) {
					ex.printStackTrace(dialog_.getExceptionStream());
				}
			}
		}
		//
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		//if (dataSize > 50) {
		//	dataSize = 50;
		//}
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		isVisibleOnPanel = false;
		//
		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
	}

	public boolean isReadyToValidate(){
		return isReadyToValidate;
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

	public String getDataSourceName(){
		return tableAlias + "." + fieldID;
	}

	public String getTableAlias(){
		return tableAlias;
	}

	public int getDataSize(){
		return dataSize;
	}

	public int getDecimalSize(){
		return decimalSize;
	}

	public Object getInternalValue(){
		return value_;
	}

	public Object getExternalValue(){
		Object value = null;
		String wrkStr;
		String basicType = this.getBasicType();
		if (basicType.equals("INTEGER") && !dataTypeOptionList.contains("MSEQ") && !dataTypeOptionList.contains("FYEAR")) {
			if (value_ == null || value_.toString().equals("")) {
				value = "";
			} else {
				wrkStr = value_.toString();
				int pos = wrkStr.indexOf(".");
				if (pos >= 0) {
					wrkStr = wrkStr.substring(0, pos);
				}
				//value = integerFormat.format(Integer.parseInt(wrkStr));
				value = integerFormat.format(Long.parseLong(wrkStr));
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
						wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
						if (!wrkStr.equals("")) {
							if (value_ == null) {
								value = "";
							} else {
								wrkStr = value_.toString().trim();
								for (int i = 0; i < kubunValueList.size(); i++) {
									if (kubunValueList.get(i).equals(wrkStr)) {
										value = kubunTextList.get(i);
										break;
									}
								}
							}
						} else {
							if (dataTypeOptionList.contains("YMONTH")) {
								if (value_ == null) {
									value = "";
								} else {
									wrkStr = value_.toString().trim();
									if (wrkStr.equals("")) {
										value = "";
									} else {
										value = XFUtility.getUserExpressionOfYearMonth(wrkStr, dialog_.getSession().getDateFormat());
									}
								}
							} else {
								if (dataTypeOptionList.contains("MSEQ")) {
									if (value_ == null) {
										value = "";
									} else {
										wrkStr = value_.toString().trim();
										value = XFUtility.getUserExpressionOfMSeq(Integer.parseInt(wrkStr), dialog_.getSession());
									}
								} else {
									if (dataTypeOptionList.contains("FYEAR")) {
										if (value_ == null) {
											value = "";
										} else {
											wrkStr = value_.toString().trim();
											value = XFUtility.getUserExpressionOfYearMonth(wrkStr, dialog_.getSession().getDateFormat());
										}
									} else {
										if (value_ == null) {
											value = "";
										} else {
											value = value_.toString().trim();
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return value;
	}

	public TableCellReadOnly getCellObject() {
		return new TableCellReadOnly(this.getExternalValue(), this.getForeground());
	}

	public TableCellRenderer getCellRenderer(){
		TableCellRenderer renderer = null;
		//
		String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN");
		if (wrkStr.equals("")) {
			renderer = new TableCellRendererReadOnly();
			if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
				if (this.getTypeOptionList().contains("MSEQ") || this.getTypeOptionList().contains("FYEAR")) {
					((TableCellRendererReadOnly)renderer).setHorizontalAlignment(SwingConstants.LEFT);
				} else {
					((TableCellRendererReadOnly)renderer).setHorizontalAlignment(SwingConstants.RIGHT);
				}
			} else {
				((TableCellRendererReadOnly)renderer).setHorizontalAlignment(SwingConstants.LEFT);
			}
		} else {
			renderer = new TableCellRendererWithCheckBox(dataTypeOptions);
			((TableCellRendererWithCheckBox)renderer).setHorizontalAlignment(SwingConstants.CENTER);
			((TableCellRendererWithCheckBox)renderer).setEditable(false);
		}
		//
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

	public String getCaption(){
		return fieldCaption;
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

	public void setReadyToValidate(boolean isReady) {
		isReadyToValidate = isReady;
	}

	public boolean setValueOfResultSet(ResultSet result) {
		Object value = null;
		boolean isFoundInResultSet = false;
		String basicType = this.getBasicType();
		this.setColor("");
		//
		try {
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					value_ = XFUtility.calculateExpireValue(this.getTableElement(), result, dialog_.getSession());
				}
			} else {
				value = result.getObject(this.getFieldID());
				isFoundInResultSet = true;
				//
				//if (this.getTableAlias().equals(dialog_.getPrimaryTableID())) {
				//	value_ = value;
				//} else {
					if (basicType.equals("INTEGER")) {
						if (value == null || value.equals("")) {
							value_ = "";
						} else {
							String wrkStr = value.toString();
							int pos = wrkStr.indexOf(".");
							if (pos >= 0) {
								wrkStr = wrkStr.substring(0, pos);
							}
							//value_ = Integer.parseInt(wrkStr);
							value_ = Long.parseLong(wrkStr);
						}
					} else {
						if (basicType.equals("FLOAT")) {
							if (value == null || value.equals("")) {
								value_ = "";
							} else {
								//value_ = Float.parseFloat(value.toString());
								value_ = Double.parseDouble(value.toString());
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
				//}
			}
		} catch(SQLException e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
		//
		return isFoundInResultSet;
	}

	public void initialize() {
		value_ = XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public void setValue(Object value) {
		value_ = XFUtility.getValueAccordingToBasicType(this.getBasicType(), value);
	}

	public Object getValue() {
		return getInternalValue();
	}

	public void setOldValue(Object value) {
	}

	public Object getOldValue() {
		return getInternalValue();
	}
	
	public boolean isValueChanged() {
		return !this.getValue().equals(this.getOldValue());
	}

	public boolean isEditable() {
		return false;
	}

	public void setEditable(boolean isEditable) {
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

//class XF110_CellRenderer extends DefaultTableCellRenderer {
//	private static final long serialVersionUID = 1L;
//	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
//		XF110_Cell cell = (XF110_Cell)value;
//		//
//		setText((String)cell.getValue());
//		setFont(new java.awt.Font("SansSerif", 0, 14));
//		//
//		if (isSelected) {
//			setBackground(table.getSelectionBackground());
//			setForeground(table.getSelectionForeground());
//		} else {
//			if (row%2==0) {
//				setBackground(table.getBackground());
//			} else {
//				setBackground(XFUtility.ODD_ROW_COLOR);
//			}
//			setForeground(table.getForeground());
//		}
//		//
//		if (!cell.getColor().equals(table.getForeground())) {
//			setForeground(cell.getColor());
//		}
//		//
//		validate();
//		return this;
//	}
//}

class XF110_RowNumberRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		XF110_RowNumber cell = (XF110_RowNumber)value;
		//
		setText(cell.toString());
		setFont(new java.awt.Font("SansSerif", 0, 14));
		setHorizontalAlignment(SwingConstants.RIGHT);
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

class XF110_PrimaryTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID = "";
	private String activeWhere = "";
	private String fixedWhere = "";
	private String updateCounterID = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF110 dialog_;
	private StringTokenizer workTokenizer;
	private boolean hasOrderByAsItsOwnPhysicalFields = true;

	public XF110_PrimaryTable(org.w3c.dom.Element functionElement, XF110 dialog){
		super();
		//
		functionElement_ = functionElement;
		dialog_ = dialog;
		//
		tableID = functionElement.getAttribute("PrimaryTable");
		tableElement = dialog_.getSession().getTableElement(tableID);
		activeWhere = tableElement.getAttribute("ActiveWhere");
		updateCounterID = tableElement.getAttribute("UpdateCounter");
		if (updateCounterID.equals("")) {
			updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
		}
		//
		int pos1, pos2;
		String wrkStr1, wrkStr2, wrkStr3;
		org.w3c.dom.Element workElement;
		//
		if (functionElement_.getAttribute("KeyFields").equals("")) {
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
		} else {
			workTokenizer = new StringTokenizer(functionElement_.getAttribute("KeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				keyFieldIDList.add(workTokenizer.nextToken());
			}
		}
		//
		if (!functionElement_.getAttribute("FixedWhere").equals("")) {
			fixedWhere = functionElement_.getAttribute("FixedWhere");
		}
		//
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			wrkStr1 = workTokenizer.nextToken();
			//
			pos1 = wrkStr1.indexOf(".");
			if (pos1 > -1) { 
				wrkStr2 = wrkStr1.substring(0, pos1);
				if (wrkStr2.equals(this.tableID)) {
					pos2 = wrkStr1.indexOf("(", pos1);
					if (pos2 > -1) {
						wrkStr3 = wrkStr1.substring(pos1+1, pos2);
					} else {
						wrkStr3 = wrkStr1.substring(pos1+1);
					}
					workElement = dialog_.getSession().getFieldElement(wrkStr2, wrkStr3);
					if (XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
						hasOrderByAsItsOwnPhysicalFields = false;
					}
				} else {
					hasOrderByAsItsOwnPhysicalFields = false;
				}
			}
			//
			orderByFieldIDList.add(wrkStr1);
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
	
	public String getUpdateCounterID(){
		return updateCounterID;
	}

	public String getSelectSQL(){
		int count;
		StringBuffer buf = new StringBuffer();
		//
		buf.append("select ");
		//
		count = 0;
		for (int i = 0; i < dialog_.getColumnList().size(); i++) {
			if (dialog_.getColumnList().get(i).getTableID().equals(tableID)
			&& !dialog_.getColumnList().get(i).isVirtualField()) {
				if (count > 0) {
					buf.append(",");
				}
				count++;
				buf.append(dialog_.getColumnList().get(i).getFieldID());
			}
		}
		if (count > 0) {
			buf.append(",");
		}
		buf.append(updateCounterID);
		//
		buf.append(" from ");
		buf.append(tableID);
		//
		if (activeWhere.equals("")) {
			if (!fixedWhere.equals("")) {
				buf.append(" where (");
				buf.append(fixedWhere);
				buf.append(") ");
			}
		} else {
			buf.append(" where (");
			buf.append(activeWhere);
			buf.append(") ");
			if (!fixedWhere.equals("")) {
				buf.append(" and (");
				buf.append(fixedWhere);
				buf.append(") ");
			}
		}
		//
		if (this.hasOrderByAsItsOwnPhysicalFields) {
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
			} else {
				buf.append(" order by ");
				for (int i = 0; i < keyFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					buf.append(keyFieldIDList.get(i));
				}
			}
		}
		//
		return buf.toString();
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
	
	public ArrayList<String> getOrderByFieldIDList(){
		return orderByFieldIDList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}
	
	public boolean hasOrderByAsItsOwnFields(){
		return hasOrderByAsItsOwnPhysicalFields;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF110_ReferTable referTable;
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

	public void runScript(String event1, String event2) throws ScriptException  {
		//String scriptText = "";
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		String tableAlias = "";
		//
		int pos = event2.indexOf("(");
		if (pos > -1 && event2.length() > 1) {
			tableAlias = event2.substring(pos + 1, event2.length() - 1);
		}
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
			for (int i = 0; i < dialog_.getColumnList().size(); i++) {
				if (event1.contains("AR") && !tableAlias.equals(this.tableID) && dialog_.getColumnList().get(i).getTableAlias().equals(tableAlias)) {
					dialog_.getColumnList().get(i).setReadyToValidate(true);
				}
			}
		}
	}
}

class XF110_ReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private String tableID = "";
	private String tableAlias = "";
	private String activeWhere = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private XF110 dialog_;
	private boolean isToBeExecuted = false;
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private String rangeKeyFieldSearch = "";
	private boolean rangeValidated;
	
	public XF110_ReferTable(org.w3c.dom.Element referElement, XF110 dialog){
		super();
		//
		referElement_ = referElement;
		//
		tableID = referElement_.getAttribute("ToTable");
		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}
		//
		dialog_ = dialog;
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
	}

	public String getSelectSQL(){
		int count;
		StringBuffer buf = new StringBuffer();
		boolean validWhereKeys = false;
		//
		buf.append("select ");
		//
		org.w3c.dom.Element workElement;
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
		XF110_Column column;
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
				StringTokenizer workTokenizer = new StringTokenizer(withKeyFieldIDList.get(i), "." );
				String workTableAlias = workTokenizer.nextToken();
				String workFieldID = workTokenizer.nextToken();
				column = dialog_.getColumnObjectByID("", workTableAlias, workFieldID);
				if (column == null) {
					JOptionPane.showMessageDialog(null, withKeyFieldIDList.get(i) + res.getString("FunctionError11"));
				}
				if (XFUtility.isLiteralRequiredBasicType(column.getBasicType())) {
					buf.append("'");
					buf.append(column.getInternalValue());
					buf.append("'");
					if (!column.getInternalValue().equals("")) {
						validWhereKeys = true;
					}
				} else {
					buf.append(column.getInternalValue());
					validWhereKeys = true;
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

	public String getSelectSQLToList(String fieldID){
		StringBuffer buf = new StringBuffer();
		//
		buf.append("select ");
		buf.append(fieldID);
		buf.append(" from ");
		buf.append(tableID);
		buf.append(" order by ");
		buf.append(fieldID);
		//
		return buf.toString();
	}

	public String getTableID(){
		return tableID;
	}
	
	public String getTableAlias(){
		return tableAlias;
	}
	
	public ArrayList<String> getWithKeyFieldIDList(){
		return withKeyFieldIDList;
	}
	
	public ArrayList<String> getFieldIDList(){
		return fieldIDList;
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
				// Note that result set is ordered by rangeKeyFieldValue DESC //
				if (!rangeValidated) { 
					StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
					String workTableAlias = workTokenizer.nextToken();
					String workFieldID = workTokenizer.nextToken();
					Object valueKey = dialog_.getColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
					Object valueFrom = result.getObject(rangeKeyFieldValid);
					int comp1 = valueKey.toString().compareTo(valueFrom.toString());
					if (comp1 >= 0) {
						returnValue = true;
						rangeValidated = true;
					}
				}
			} catch(SQLException e) {
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
				Object valueKey = dialog_.getColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
				Object valueFrom = result.getObject(rangeKeyFieldValid);
				Object valueThru = result.getObject(rangeKeyFieldExpire);
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				int comp2 = valueKey.toString().compareTo(valueThru.toString());
				if (comp1 >= 0 && comp2 < 0) {
					returnValue = true;
				}
			} catch(SQLException e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
		//
		return returnValue;
	}
}

class XF110_Component_keyAdapter extends java.awt.event.KeyAdapter {
	XF110 adaptee;
	XF110_Component_keyAdapter(XF110 adaptee) {
		this.adaptee = adaptee;
	}
	public void keyPressed(KeyEvent e) {
		adaptee.component_keyPressed(e);
	}
}

class XF110_jTableMain_keyAdapter extends java.awt.event.KeyAdapter {
	XF110 adaptee;
	XF110_jTableMain_keyAdapter(XF110 adaptee) {
		this.adaptee = adaptee;
	}
	public void keyPressed(KeyEvent e) {
		adaptee.jTableMain_keyPressed(e);
	}
}

class XF110_jTableMain_mouseAdapter extends java.awt.event.MouseAdapter {
	XF110 adaptee;
	XF110_jTableMain_mouseAdapter(XF110 adaptee) {
		this.adaptee = adaptee;
	}
	public void mouseClicked(MouseEvent e) {
		adaptee.jTableMain_mouseClicked(e);
	}
}

class XF110_jTableMain_focusAdapter extends java.awt.event.FocusAdapter {
	XF110 adaptee;
	XF110_jTableMain_focusAdapter(XF110 adaptee) {
		this.adaptee = adaptee;
	}
	public void focusGained(FocusEvent e) {
		adaptee.jTableMain_focusGained(e);
	}
	public void focusLost(FocusEvent e) {
		adaptee.jTableMain_focusLost(e);
	}
}

class XF110_jScrollPaneTable_mouseAdapter extends java.awt.event.MouseAdapter {
	XF110 adaptee;
	XF110_jScrollPaneTable_mouseAdapter(XF110 adaptee) {
		this.adaptee = adaptee;
	}
	public void mousePressed(MouseEvent e) {
		adaptee.jScrollPaneTable_mousePressed(e);
	}
}

class XF110_jButtonList_actionAdapter implements java.awt.event.ActionListener {
	XF110 adaptee;
	XF110_jButtonList_actionAdapter(XF110 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.selectRowsAndList();
	}
}

class XF110_FunctionButton_actionAdapter implements java.awt.event.ActionListener {
	XF110 adaptee;
	XF110_FunctionButton_actionAdapter(XF110 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jFunctionButton_actionPerformed(e);
	}
}
