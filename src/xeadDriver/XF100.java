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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.table.*;
import javax.swing.*;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.xssf.usermodel.*;
import org.w3c.dom.*;

public class XF100 extends JDialog implements XFExecutable, XFScriptable {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element functionElement_ = null;
	private Session session_ = null;
	private HashMap<String, Object> parmMap_ = null;
	private HashMap<String, Object> returnMap_ = new HashMap<String, Object>();
	private boolean instanceIsAvailable = true;
	private int instanceArrayIndex_ = -1;
	private int programSequence;
	private StringBuffer processLog = new StringBuffer();
	private NodeList referElementList;
	private JPanel jPanelMain = new JPanel();
	private JSplitPane jSplitPaneTop = new JSplitPane();
	private JPanel jPanelTop = new JPanel();
	private JPanel jPanelFilters = new JPanel();
	private JScrollPane jScrollPaneFilters = new JScrollPane();
	private JButton jButtonList = new JButton();
	private JButton buttonReturnRows = new JButton();
	private String functionKeyToSelectRows = "";
	private XF100_PrimaryTable primaryTable_;
	private ArrayList<XFTableOperator> referOperatorList = new ArrayList<XFTableOperator>();
	private ArrayList<XF100_Filter> filterList = new ArrayList<XF100_Filter>();
	private ArrayList<XF100_Column> columnList = new ArrayList<XF100_Column>();
	private ArrayList<XF100_ReferTable> referTableList = new ArrayList<XF100_ReferTable>();
	private ArrayList<WorkingRow> workingRowList = new ArrayList<WorkingRow>();
	private SortableDomElementListModel sortingList;
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
	private JSplitPane jSplitPaneCenter = new JSplitPane();
	private JScrollPane jScrollPaneTable = new JScrollPane();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private TableModelReadOnly tableModelMain;
	private JTable jTableMain = new JTable();
	private TableHeadersRenderer headersRenderer;
	private TableCellsRenderer cellsRenderer;
	private boolean isHeaderResizing = false;
	private int countOfRows = 0;
	private JTextArea jTextAreaMessages = new JTextArea();
	private JButton[] jButtonArray = new JButton[7];
	private String detailFunctionID = "";
	private String initialMsg = "";
	private Color selectionColorWithFocus = new Color(49,106,197);
	private Color selectionColorWithoutFocus = new Color(213,213,213);
	private Action helpAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			session_.browseHelp();
		}
	};
	private Action escapeAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			returnTo("MENU");
		}
	};
	private Action[] actionButtonArray = new Action[7];
	private String[] actionDefinitionArray = new String[7];
	private int buttonIndexForF6, buttonIndexForF8;
	private ScriptEngine scriptEngine;
	public Bindings scriptBindings;
	private String scriptNameRunning = "";
	private int initialReadCount = 0;
	private ByteArrayOutputStream exceptionLog;
	private PrintStream exceptionStream;
	private String exceptionHeader = "";
	private boolean isListingInNormalOrder;
	private HashMap<String, CompiledScript> compiledScriptMap = new HashMap<String, CompiledScript>();
	private int headersWidth = 0;
	private int filtersWidth = 0;
	private boolean isClosing = false;
	private XF100_Filter firstEditableFilter = null;
	private HashMap<String, Object> variantMap = new HashMap<String, Object>();
	private JPopupMenu jPopupMenu = new JPopupMenu();
	private JMenuItem jMenuItemToSelect = new JMenuItem();
	private JMenuItem jMenuItemToCallToAdd = new JMenuItem();
	private JMenuItem jMenuItemToOutput = new JMenuItem();
	private String actionToCallToAdd = "";

	public XF100(Session session, int instanceArrayIndex) {
		super(session, "", true);
		session_ = session;
		instanceArrayIndex_ = instanceArrayIndex;
		initComponentsAndVariants();
	}

	void initComponentsAndVariants() {
		jPanelMain.setLayout(new BorderLayout());
		jPanelTop.setPreferredSize(new Dimension(1000, 48));
		jPanelTop.setLayout(new BorderLayout());
		jButtonList.setPreferredSize(new Dimension(100, 100));
		jButtonList.setLayout(new BorderLayout());
		jButtonList.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jButtonList.setText(XFUtility.RESOURCE.getString("Search"));
		jButtonList.addActionListener(new XF100_jButtonList_actionAdapter(this));
		jButtonList.addKeyListener(new XF100_Component_keyAdapter(this));
		jPanelTop.setBorder(BorderFactory.createEtchedBorder());
		jPanelTop.add(jScrollPaneFilters, BorderLayout.CENTER);

		jPanelFilters.setLayout(null);
		jScrollPaneFilters.getViewport().add(jPanelFilters);
		jScrollPaneFilters.setBorder(null);
		jSplitPaneTop.setOrientation(JSplitPane.VERTICAL_SPLIT);

		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jTextAreaMessages.setWrapStyleWord(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jSplitPaneCenter.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPaneCenter.add(jScrollPaneMessages, JSplitPane.BOTTOM);

		jTableMain.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTableMain.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jTableMain.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jTableMain.setRowSelectionAllowed(true);
		jTableMain.addKeyListener(new XF100_jTableMain_keyAdapter(this));
		jTableMain.addKeyListener(new XF100_Component_keyAdapter(this));
		jTableMain.addMouseListener(new XF100_jTableMain_mouseAdapter(this));
		jTableMain.addMouseMotionListener(new XF100_jTableMain_mouseMotionAdapter(this));
		jTableMain.addFocusListener(new XF100_jTableMain_focusAdapter(this));
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
			public void mouseClicked(MouseEvent e) {
				headersRenderer.checkSelection(e);
				jScrollPaneTable.updateUI();
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
		jScrollPaneTable.addMouseListener(new XF100_jScrollPaneTable_mouseAdapter(this));
		jMenuItemToSelect.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jMenuItemToSelect.setText(XFUtility.RESOURCE.getString("Select"));
		jMenuItemToSelect.addActionListener(new XF100_jMenuItemToCall_actionAdapter(this));
		jPopupMenu.add(jMenuItemToSelect);
		jMenuItemToCallToAdd.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jMenuItemToCallToAdd.addActionListener(new XF100_jMenuItemToCallToAdd_actionAdapter(this));
		jMenuItemToOutput.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jMenuItemToOutput.addActionListener(new XF100_jMenuItemToOutput_actionAdapter(this));

		jPanelBottom.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setLayout(new BorderLayout());
		jPanelBottom.setBorder(null);
		jLabelFunctionID.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE-2));
		jLabelFunctionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelFunctionID.setForeground(Color.gray);
		jLabelSessionID.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE-2));
		jLabelSessionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelSessionID.setForeground(Color.gray);
		jProgressBar.setStringPainted(true);
		jPanelButtons.setBorder(null);
		jPanelButtons.setLayout(gridLayoutButtons);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
		jPanelInfo.setLayout(gridLayoutInfo);
		jPanelInfo.add(jLabelSessionID);
		jPanelInfo.add(jLabelFunctionID);
		gridLayoutButtons.setColumns(7);
		gridLayoutButtons.setRows(1);

		for (int i = 0; i < 7; i++) {
			jButtonArray[i] = new JButton();
			jButtonArray[i].setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
			jButtonArray[i].setFocusable(false);
			jButtonArray[i].addActionListener(new XF100_FunctionButton_actionAdapter(this));
			jPanelButtonArray[i] = new JPanel();
			jPanelButtonArray[i].setLayout(new BorderLayout());
			jPanelButtonArray[i].add(jButtonArray[i], BorderLayout.CENTER);
			actionButtonArray[i] = new ButtonAction(jButtonArray[i]);
			jPanelButtons.add(jPanelButtonArray[i]);
		}

		jPanelMain.add(jSplitPaneCenter, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
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
			returnMap_.put("RETURN_CODE", "00");

			/////////////////////////
			// Initialize variants //
			/////////////////////////
			instanceIsAvailable = false;
			isClosing = false;
			exceptionLog = new ByteArrayOutputStream();
			exceptionStream = new PrintStream(exceptionLog);
			exceptionHeader = "";
			processLog.delete(0, processLog.length());
			variantMap.clear();
			messageList.clear();
			jPanelBottom.remove(jProgressBar);
			jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
			isListingInNormalOrder = true;
			
			///////////////////////////////////////////
			// Setup specifications for the function //
			///////////////////////////////////////////
			if (functionElement != null
					&& (functionElement_ == null || !functionElement_.getAttribute("ID").equals(functionElement.getAttribute("ID")))) {
				setFunctionSpecifications(functionElement);
			}

			/////////////////////////////////
			// Write log to start function //
			/////////////////////////////////
			programSequence = session_.writeLogOfFunctionStarted(functionElement_.getAttribute("ID"), functionElement_.getAttribute("Name"));

			/////////////////////////////////////
			// Validate parameters with filter //
			/////////////////////////////////////
			for (int i = 0; i < filterList.size(); i++) {
				filterList.get(i).setValue(filterList.get(i).getDefaultValue());
				if (!filterList.get(i).isValidatedWithParmMapValue(parmMap_)) {
					JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError47") + filterList.get(i).getCaption() + XFUtility.RESOURCE.getString("FunctionError48"));
					isClosing = true;
					break;
				}
			}

			//////////////////////////
			// Restore filter value //
			//////////////////////////
			HashMap<String, String> valueMap = session_.getFilterValueMap(this.getFunctionID());
			if (valueMap != null) {
				for (int i = 0; i < filterList.size(); i++) {
					if (filterList.get(i).isEditable() && filterList.get(i).getBasicType().equals("STRING")) {
						if (filterList.get(i).getValue().equals("")) {
							if (valueMap.containsKey(filterList.get(i).getDataSourceName())) {
								filterList.get(i).setValue(valueMap.get(filterList.get(i).getDataSourceName()));
							}
						}
					}
				}
			}

			////////////////////////////////
			// Setup Panel Configurations //
			////////////////////////////////
			int posX, posY;
	        Rectangle screenRect = session_.getMenuRectangle();
			if (functionElement_.getAttribute("Size").equals("")) {
				this.setPreferredSize(new Dimension(screenRect.width, screenRect.height));
				this.setLocation(screenRect.x, screenRect.y);
			} else {
				if (functionElement_.getAttribute("Size").equals("AUTO")) {
					headersWidth = headersWidth + 70;
					filtersWidth = filtersWidth + 140;
					int maxWidth = 0;
					if (headersWidth >= filtersWidth) {
						maxWidth = headersWidth;
					} else {
						maxWidth = filtersWidth;
					}
					
					if (maxWidth > screenRect.width) {
						maxWidth = screenRect.width;
					}
					int height = screenRect.height * maxWidth / screenRect.width;
					this.setPreferredSize(new Dimension(maxWidth, height));
					posX = ((screenRect.width - maxWidth) / 2) + screenRect.x;
					posY = ((screenRect.height - height) / 2) + screenRect.y;
					this.setLocation(posX, posY);
				} else {
					StringTokenizer workTokenizer = new StringTokenizer(functionElement_.getAttribute("Size"), ";" );
					int width = Integer.parseInt(workTokenizer.nextToken());
					int height = Integer.parseInt(workTokenizer.nextToken());
					this.setPreferredSize(new Dimension(width, height));
					posX = ((screenRect.width - width) / 2) + screenRect.x;
					posY = ((screenRect.height - height) / 2) + screenRect.y;
					this.setLocation(posX, posY);
				}
			}
			setupFunctionKeysAndButtons();
			buttonReturnRows.setEnabled(false);
			if (detailFunctionID.equals("") && jTableMain.isFocusable()) {
				returnMap_.put("RETURN_CODE", "01");
			}
			jSplitPaneCenter.setDividerLocation(this.getPreferredSize().height - 151);
			jSplitPaneCenter.updateUI();
			this.pack();

		} catch(Exception e) {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		////////////////////////////////
		// Set message and show Panel //
		////////////////////////////////
		if (!returnMap_.get("RETURN_CODE").equals("99") && !isClosing) {
			int rowCount = tableModelMain.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				tableModelMain.removeRow(0);
			}
			if (firstEditableFilter == null) {
				if (!initialMsg.equals("")) {
					session_.setMessage(initialMsg);
				}
				selectRowsAndList();
				if (!initialMsg.equals("")) {
					session_.setMessage("");
				}
			} else {
				if (functionElement_.getAttribute("InitialListing").equals("T")) {
					if (!initialMsg.equals("")) {
						session_.setMessage(initialMsg);
					}
					selectRowsAndList();
					if (!initialMsg.equals("")) {
						session_.setMessage("");
					}
				} else {
					if (initialMsg.equals("")) {
						StringBuffer buf = new StringBuffer();
						buf.append(XFUtility.RESOURCE.getString("FunctionMessage1"));
						buf.append(primaryTable_.getOrderByDescription());
						buf.append(XFUtility.RESOURCE.getString("FunctionMessage56"));
						jTextAreaMessages.setText(buf.toString());
					} else {
						jTextAreaMessages.setText(initialMsg);
					}
				}
			}
			if (!isClosing) {
				session_.setMessageComponent(jScrollPaneMessages);
				this.setVisible(true);
			}
//			if (firstEditableFilter != null) {
//				if (initialMsg.equals("")) {
//					StringBuffer buf = new StringBuffer();
//					buf.append(XFUtility.RESOURCE.getString("FunctionMessage1"));
//					buf.append(primaryTable_.getOrderByDescription());
//					buf.append(XFUtility.RESOURCE.getString("FunctionMessage56"));
//					jTextAreaMessages.setText(buf.toString());
//				} else {
//					jTextAreaMessages.setText(initialMsg);
//				}
//				if (functionElement_.getAttribute("InitialListing").equals("T")) {
//					if (!initialMsg.equals("")) {
//						session_.setMessage(initialMsg);
//					}
//					selectRowsAndList();
//					if (!initialMsg.equals("")) {
//						session_.setMessage("");
//					}
//				} else {
//					int rowCount = tableModelMain.getRowCount();
//					for (int i = 0; i < rowCount; i++) {
//						tableModelMain.removeRow(0);
//					}
//				}
//			} else {
//				if (!initialMsg.equals("")) {
//					session_.setMessage(initialMsg);
//				}
//				selectRowsAndList();
//				if (!initialMsg.equals("")) {
//					session_.setMessage("");
//				}
//			}
		}

		return returnMap_;
	}

	public void setFunctionSpecifications(org.w3c.dom.Element functionElement) throws Exception {
		String workAlias, workTableID, workFieldID, workStr;
		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;
		XF100_Filter filter;

		/////////////////////////////////////////////
		// Set specifications to the inner variant //
		/////////////////////////////////////////////
		functionElement_ = functionElement;
		
		//////////////////////////////////////////////
		// Setup the primary table and refer tables //
		//////////////////////////////////////////////
		compiledScriptMap.clear();
		primaryTable_ = new XF100_PrimaryTable(functionElement_, this);
		referTableList.clear();
		referElementList = primaryTable_.getTableElement().getElementsByTagName("Refer");
		sortingList = XFUtility.getSortedListModel(referElementList, "Order");
		for (int i = 0; i < sortingList.getSize(); i++) {
			referTableList.add(new XF100_ReferTable((org.w3c.dom.Element)sortingList.getElementAt(i), this));
		}
		if (functionElement_.getAttribute("InitialReadCount").equals("")) {
			initialReadCount = 0;
		} else {
			initialReadCount = Integer.parseInt(functionElement_.getAttribute("InitialReadCount"));
		}
		initialMsg = functionElement_.getAttribute("InitialMsg");

		//////////////////////////////
		// Setup JTable and Columns //
		//////////////////////////////
		tableModelMain = new TableModelReadOnly();
		jTableMain.setModel(tableModelMain);
		detailFunctionID = functionElement_.getAttribute("DetailFunction");
		if (detailFunctionID.equals("NONE")) {
			jTableMain.setRowSelectionAllowed(false);
		} else {
			jTableMain.setRowSelectionAllowed(true);
		}
		columnList.clear();
		int columnIndex = 0;
		NodeList columnElementList = functionElement_.getElementsByTagName("Column");
		sortingList = XFUtility.getSortedListModel(columnElementList, "Order");
		for (int i = 0; i < columnElementList.getLength(); i++) {
			columnList.add(new XF100_Column((org.w3c.dom.Element)sortingList.getElementAt(i), this));
			if (columnList.get(i).isVisibleOnPanel()) {
				columnIndex++;
				columnList.get(i).setColumnIndex(columnIndex);
			}
		}
		boolean isWithCheckBox = false;
		if (detailFunctionID.equals("")) {
			NodeList buttonList = functionElement_.getElementsByTagName("Button");
			for (int i = 0; i < buttonList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)buttonList.item(i);
				if (workElement.getAttribute("Action").equals("RETURN_ROWS")) {
					isWithCheckBox = true;
					break;
				}
			}
		}
		headersRenderer = new TableHeadersRenderer(this, isWithCheckBox);
		cellsRenderer = new TableCellsRenderer(headersRenderer);
		jTableMain.setRowHeight(headersRenderer.getHeight());
		tableModelMain.addColumn(""); //column index:0 //
		TableColumn column = jTableMain.getColumnModel().getColumn(0);
		column.setHeaderRenderer(headersRenderer);
		column.setCellRenderer(cellsRenderer);
		column.setPreferredWidth(headersRenderer.getWidth());
		headersWidth = headersRenderer.getWidth();

		////////////////////////////////////////////////////
		// Add primary table key fields as HIDDEN columns //
		////////////////////////////////////////////////////
		for (int i = 0; i < primaryTable_.getKeyFieldIDList().size(); i++) {
			if (!existsInColumnList(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldIDList().get(i))) {
				columnList.add(new XF100_Column(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldIDList().get(i), this));
			}
		}

		//////////////////////////////////////////
		// Add OrderBy fields as HIDDEN columns //
		//////////////////////////////////////////
		ArrayList<String> orderByFieldList = primaryTable_.getOrderByFieldIDList(true);
		for (int i = 0; i < orderByFieldList.size(); i++) {
			workStr = orderByFieldList.get(i).replace("(D)", "");
			workStr = workStr.replace("(A)", "");
			workTokenizer = new StringTokenizer(workStr, "." );
			workAlias = workTokenizer.nextToken();
			workTableID = getTableIDOfTableAlias(workAlias);
			workFieldID = workTokenizer.nextToken();
			if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
				columnList.add(new XF100_Column(workTableID, workAlias, workFieldID, this));
			}
		}

		/////////////////////////////////////////////////////////////
		// Analyze fields in script and add them as HIDDEN columns //
		/////////////////////////////////////////////////////////////
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
							String msg = XFUtility.RESOURCE.getString("FunctionError1") + primaryTable_.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + primaryTable_.getScriptList().get(i).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
							JOptionPane.showMessageDialog(this, msg);
							throw new Exception(msg);
						} else {
							if (primaryTable_.isValidDataSource(workTableID, workAlias, workFieldID)) {
								columnList.add(new XF100_Column(workTableID, workAlias, workFieldID, this));
							}
						}
					}
				}
			}
		}

		////////////////////////////////////////////////////////////
		// Add BYTEA-type-field for BYTEA field as HIDDEN columns //
		////////////////////////////////////////////////////////////
		for (int i = 0; i < columnList.size(); i++) {
			if (columnList.get(i).getBasicType().equals("BYTEA") && !columnList.get(i).getByteaTypeFieldID().equals("")) {
				if (!existsInColumnList(primaryTable_.getTableID(), "", columnList.get(i).getByteaTypeFieldID())) {
					columnList.add(new XF100_Column(primaryTable_.getTableID(), "", columnList.get(i).getByteaTypeFieldID(), this));
				}
			}
		}

		/////////////////////////
		// Setup Filter fields //
		/////////////////////////
		filtersWidth = 0;
		int posX = 0;
		int posY = 8;
		Dimension dimOfPriviousField = new Dimension(0,0);
		Dimension dim;
		filterList.clear();
		jPanelTop.removeAll();
		jPanelFilters.removeAll();
		NodeList filterFieldList = functionElement_.getElementsByTagName("Filter");
		sortingList = XFUtility.getSortedListModel(filterFieldList, "Order");
		for (int i = 0; i < sortingList.getSize(); i++) {
			filter = new XF100_Filter((org.w3c.dom.Element)sortingList.getElementAt(i), this);
			filterList.add(filter);
			if (!filter.isHidden()) {
				jPanelFilters.add(filter);
				if (filter.isEditable() && firstEditableFilter == null) {
					firstEditableFilter = filter;
				}
				if (filter.isVerticalPosition()) {
					posX = 0;
					posY = posY + dimOfPriviousField.height + filter.getVerticalMargin();
				} else {
					posX = posX + dimOfPriviousField.width;
				}

				dim = filter.getPreferredSize();
				filter.setBounds(posX, posY, dim.width, dim.height);
				if (posX + dim.width > filtersWidth) {
					filtersWidth = posX + dim.width;
				}
				dimOfPriviousField = new Dimension(dim.width, dim.height);
			}
		}
		if (filterList.size() >= 1) {
			/////////////////////////////////////////
			// Add filter fields as HIDDEN columns //
			// if they are not on the column list  //
			/////////////////////////////////////////
			int countOfVisibleFilters = 0;
			for (int i = 0; i < filterList.size(); i++) {
				if (!filterList.get(i).isHidden()) {
					countOfVisibleFilters++;
				}
				if (!existsInColumnList(filterList.get(i).getTableID(), filterList.get(i).getTableAlias(), filterList.get(i).getFieldID())) {
					columnList.add(new XF100_Column(filterList.get(i).getTableID(), filterList.get(i).getTableAlias(), filterList.get(i).getFieldID(), this));
				}
			}
			////////////////////////////////////////////////
			// Put the panel with filters on jPanelCenter //
			////////////////////////////////////////////////
			if (countOfVisibleFilters > 0) {
				jPanelFilters.setPreferredSize(new Dimension(filtersWidth, posY + dimOfPriviousField.height));
				jPanelTop.add(jScrollPaneFilters, BorderLayout.CENTER);
				if (firstEditableFilter != null) {
					jPanelTop.add(jButtonList, BorderLayout.EAST);
					firstEditableFilter.requestFocus();
				}
				jSplitPaneTop.add(jPanelTop, JSplitPane.TOP);
				jSplitPaneTop.add(jScrollPaneTable, JSplitPane.BOTTOM);
				jSplitPaneTop.setDividerLocation(jPanelFilters.getPreferredSize().height + 14);
				jSplitPaneTop.updateUI();
				jSplitPaneCenter.add(jSplitPaneTop, JSplitPane.TOP);
			} else {
				jSplitPaneCenter.add(jScrollPaneTable, JSplitPane.TOP);
			}
		} else {
			jSplitPaneCenter.add(jScrollPaneTable, JSplitPane.TOP);
		}

		////////////////////////////////
		// Setup Panel Configurations //
		////////////////////////////////
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
			jLabelFunctionID.setText("100" + "-" + instanceArrayIndex_ + "-" + functionElement_.getAttribute("ID"));
		} else {
			jLabelFunctionID.setText("100" + "-" + functionElement_.getAttribute("ID"));
		}
		FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));
		this.setTitle(functionElement_.getAttribute("Name"));

		/////////////////////////////////////////////////////////////////
		// Analyze refer tables and add their fields as HIDDEN columns //
		/////////////////////////////////////////////////////////////////
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
						columnList.add(new XF100_Column(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j), this));
					}
				}
				for (int j = 0; j < referTableList.get(i).getWithKeyFieldIDList().size(); j++) {
					workTokenizer = new StringTokenizer(referTableList.get(i).getWithKeyFieldIDList().get(j), "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
						columnList.add(new XF100_Column(workTableID, workAlias, workFieldID, this));
					}
				}
			}
		}
		//////////////////////////////////////
		// Setup Script Engine and Bindings //
		//////////////////////////////////////
		scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
		scriptBindings = scriptEngine.createBindings();
		scriptBindings.put("instance", (XFScriptable)this);
		for (int i = 0; i < columnList.size(); i++) {
			scriptBindings.put(columnList.get(i).getDataSourceID(), columnList.get(i));
		}
	}

	public boolean isAvailable() {
		return instanceIsAvailable;
	}

	public boolean isListingInNormalOrder() {
		return isListingInNormalOrder;
	}

	public void setListingInNormalOrder(boolean isInNormalOrder) {
		isListingInNormalOrder = isInNormalOrder;
	}

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			closeFunction();
		}
	}

	public void setErrorAndCloseFunction() {
		returnMap_.put("RETURN_CODE", "99");
		this.rollback();
		closeFunction();
	}

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
		if (!returnMap_.get("RETURN_CODE").equals("99")) {
			this.commit();
		}
		instanceIsAvailable = true;
		String errorLog = "";
		if (exceptionLog.size() > 0 || !exceptionHeader.equals("")) {
			errorLog = exceptionHeader + exceptionLog.toString();
		}
		session_.removeMessageComponent(jScrollPaneMessages);
		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), processLog.toString(), errorLog);
		isClosing = true;
		this.setVisible(false);
	}
	
	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(this, message);
		}
		returnMap_.put("RETURN_CODE", "01");
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
	
	public void commit() {
		session_.commit(true, processLog);
	}
	
	public void rollback() {
		session_.commit(false, processLog);
	}

	void setupFunctionKeysAndButtons() {
		InputMap inputMap  = jPanelMain.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.clear();
		ActionMap actionMap = jPanelMain.getActionMap();
		actionMap.clear();

		for (int i = 0; i < 7; i++) {
			jButtonArray[i].setText("");
			jButtonArray[i].setVisible(false);
			actionDefinitionArray[i] = "";
		}

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMap.put("HELP", helpAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
		actionMap.put("ESCAPE", escapeAction);

		buttonIndexForF6 = -1;
		buttonIndexForF8 = -1;
		int workIndex;
		org.w3c.dom.Element element;
		NodeList buttonList = functionElement_.getElementsByTagName("Button");
		for (int i = 0; i < buttonList.getLength(); i++) {
			element = (org.w3c.dom.Element)buttonList.item(i);

			workIndex = Integer.parseInt(element.getAttribute("Position"));
			actionDefinitionArray[workIndex] = element.getAttribute("Action");
			XFUtility.setCaptionToButton(jButtonArray[workIndex], element, "", this.getPreferredSize().width / 8, session_);
			jButtonArray[workIndex].setVisible(true);
			if (element.getAttribute("Action").equals("ADD")) {
				String functionName = session_.getFunctionName(detailFunctionID);
				if (!functionName.equals("")) {
					jButtonArray[workIndex].setToolTipText(detailFunctionID + " " + functionName);
				}
			}
			if (element.getAttribute("Action").equals("ADD")
					|| element.getAttribute("Caption").equals(XFUtility.RESOURCE.getString("AddRow"))) {
				jMenuItemToCallToAdd.setText(element.getAttribute("Caption"));
				jPopupMenu.add(jMenuItemToCallToAdd);
				actionToCallToAdd = element.getAttribute("Action");
			}
			if (element.getAttribute("Action").equals("OUTPUT")) {
				jMenuItemToOutput.setText(element.getAttribute("Caption"));
				jPopupMenu.add(jMenuItemToOutput);
			}
			if (element.getAttribute("Action").equals("RETURN_ROWS")) {
				buttonReturnRows = jButtonArray[workIndex];
				buttonReturnRows.setEnabled(false);
				functionKeyToSelectRows = "F" + element.getAttribute("Number");
			}
			inputMap.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
			actionMap.put("actionButton" + workIndex, actionButtonArray[workIndex]);

			if (element.getAttribute("Number").equals("6")) {
				buttonIndexForF6 = workIndex;
			}
			if (element.getAttribute("Number").equals("8")) {
				buttonIndexForF8 = workIndex;
			}
		}
	}

	void selectRowsAndList() {
		HashMap<String, Object> keyMap = null;
		HashMap<String, Object> columnMap = null;
		ArrayList<TableCellReadOnly> cellObjectList= null;
		ArrayList<String> orderByFieldList;
		ArrayList<String> orderByFieldTypeList;
		ArrayList<Object> orderByValueList;
		XFTableOperator primaryTableOp, referTableOp;
		String wrkStr, sql;
		boolean readyToEvaluate;
		int rowIndexNumber = 0;

		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			int rowCount = tableModelMain.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				tableModelMain.removeRow(0);
			}
			headersRenderer.clearSortingColumn();
			jScrollPaneTable.updateUI();

			boolean toBeSelected = true;
			boolean dialogCheckReadRequested = false;
			countOfRows = 0;
			int countOfBlockUnit = initialReadCount;
			int blockRows = 0;
			int originalFromRow = 0;
			int fromRow = originalFromRow;
			workingRowList.clear();
			referOperatorList.clear();

//			primaryTable_.runScript("BR", ""); /* Script to be run BEFORE READ */

			if (countOfBlockUnit == 0) {
				primaryTableOp = createTableOperator(primaryTable_.getSelectSQL());
			} else {
				primaryTableOp = createTableOperator(primaryTable_.getSelectSQL(fromRow, fromRow+countOfBlockUnit-1));
			}
			while (primaryTableOp.next()) {

				for (int i = 0; i < columnList.size(); i++) {
					columnList.get(i).setReadyToEvaluate(false);
					columnList.get(i).initialize();
				}

				primaryTable_.runScript("BR", ""); /* Script to be run BEFORE READ */

				for (int i = 0; i < columnList.size(); i++) {
					if (columnList.get(i).getTableAlias().equals(primaryTable_.getTableID())) {
						readyToEvaluate = columnList.get(i).setValueOfResultSet(primaryTableOp);
						columnList.get(i).setReadyToEvaluate(readyToEvaluate);
					}
				}

				primaryTable_.runScript("AR", "BR()"); /* Script to be run AFTER READ primary table */

				if (isTheRowToBeSelected()) {
					toBeSelected = true;

					for (int i = 0; i < referTableList.size(); i++) {
						if (referTableList.get(i).isToBeExecuted()) {
							primaryTable_.runScript("AR", "BR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE READ */
							sql = referTableList.get(i).getSelectSQL();
							if (!sql.equals("")) {
								referTableOp = getReferOperator(sql);
								if (referTableOp.next()) {
									for (int j = 0; j < columnList.size(); j++) {
										if (columnList.get(j).getTableID().equals(referTableList.get(i).getTableID()) && columnList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
											if (referTableOp.hasValueOf(columnList.get(j).getTableID(), columnList.get(j).getFieldID())) {
												columnList.get(j).setReadyToEvaluate(true);
												columnList.get(j).setValueOfResultSet(referTableOp);
											}
										}
									}
									primaryTable_.runScript("AR", "AR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run AFTER READ */
									if (!isTheRowToBeSelected()) {
										toBeSelected = false;
										break;
									}
								}
							}
							if (!toBeSelected) {
								break;
							}
						}
					}

					primaryTable_.runScript("AR", "AR()"); /* Script to be run AFTER READ */

					if (toBeSelected) {
						for (int i = 0; i < columnList.size(); i++) {
							columnList.get(i).setReadyToEvaluate(true);
							if (columnList.get(i).getBasicType().equals("BYTEA")) {
								columnList.get(i).setByteaType(columnList);
							}
						}
						if (isTheRowToBeSelected()) {
							rowIndexNumber++;

							keyMap = new HashMap<String, Object>();
							for (int i = 0; i < primaryTable_.getKeyFieldIDList().size(); i++) {
								keyMap.put(primaryTable_.getKeyFieldIDList().get(i), primaryTableOp.getValueOf(primaryTable_.getKeyFieldIDList().get(i)));
							}

							columnMap = new HashMap<String, Object>();
							cellObjectList = new ArrayList<TableCellReadOnly>();
							for (int i = 0; i < columnList.size(); i++) {
								columnMap.put(columnList.get(i).getDataSourceName(), columnList.get(i).getInternalValue());
								cellObjectList.add(columnList.get(i).getCellObject());
							}

							if (primaryTable_.hasOrderByAsItsOwnFields()) {
								Object[] cell = new Object[1];
								cell[0] = new XF100_RowNumber(rowIndexNumber, keyMap, columnMap, cellObjectList);
								tableModelMain.addRow(cell);
							} else {
								orderByFieldList = primaryTable_.getOrderByFieldIDList(isListingInNormalOrder);
								orderByFieldTypeList = new ArrayList<String>();
								orderByValueList = new ArrayList<Object>();
								for (int i = 0; i < orderByFieldList.size(); i++) {
									wrkStr = orderByFieldList.get(i).replace("(D)", "");
									wrkStr = wrkStr.replace("(A)", "");
									for (int j = 0; j < columnList.size(); j++) {
										if (columnList.get(j).getDataSourceName().equals(wrkStr)) {
											orderByFieldTypeList.add(columnList.get(j).getBasicType());
											orderByValueList.add(columnList.get(j).getExternalValue());
											break;
										}
									}
								}
								workingRowList.add(new WorkingRow(cellObjectList, keyMap, columnMap, orderByValueList, orderByFieldTypeList));
							}
						}
					}
					
					countOfRows++;
					blockRows++;
					if (countOfBlockUnit > 0 && blockRows == countOfBlockUnit) {
						primaryTableOp = null; //clear heap//
						dialogCheckReadRequested = true;
						if (countOfRows > 0) {
							jTableMain.scrollRectToVisible(jTableMain.getCellRect(countOfRows-1, 0, true));
						}
						//////////////////////////////////////
						// row number of the first row is 0 //
						//////////////////////////////////////
						boolean repliedOK = session_.getDialogCheckRead().request(originalFromRow, rowIndexNumber, fromRow+countOfBlockUnit, countOfBlockUnit);
						if (repliedOK) {
							blockRows = 0;
							fromRow = session_.getDialogCheckRead().getNextRow();
							countOfBlockUnit = session_.getDialogCheckRead().getCountUnit();
							if (fromRow >= 0 && countOfBlockUnit >= 1) {
								if (session_.getDialogCheckRead().isRestarting()) {
									originalFromRow = session_.getDialogCheckRead().getNextRow();
									rowCount = tableModelMain.getRowCount();
									for (int i = 0; i < rowCount; i++) {
										tableModelMain.removeRow(0);
									}
									countOfRows = 0;
									workingRowList.clear();
								}
								primaryTableOp = createTableOperator(primaryTable_.getSelectSQL(fromRow, fromRow+countOfBlockUnit-1));
							} else {
								break;
							}
						} else {
							break;
						}
					}
					
				}
			}
			primaryTableOp = null; //clear heap//

			if (!primaryTable_.hasOrderByAsItsOwnFields()) {
				WorkingRow[] workingRowArray = workingRowList.toArray(new WorkingRow[0]);
				Arrays.sort(workingRowArray);
				for (int i = 0; i < workingRowArray.length; i++) {
					Object[] cell = new Object[1];
					cell[0] = new XF100_RowNumber(i + 1, workingRowArray[i].getKeyMap(), workingRowArray[i].getColumnMap(), workingRowArray[i].getCellObjectList());
					tableModelMain.addRow(cell);
				}
			}

			jTableMain.requestFocus();
			messageList.clear();
			if (jTableMain.getRowCount() > 0) {
				if (dialogCheckReadRequested) {
					jTableMain.scrollRectToVisible(jTableMain.getCellRect(tableModelMain.getRowCount()-1, 0, true));
				} else {
					jTableMain.scrollRectToVisible(jTableMain.getCellRect(0, 0, true));
					jTableMain.setRowSelectionInterval(0, 0);
				}
//				if (parmMap_.containsKey("INITIAL_MESSAGE")) {
//					messageList.add((String)parmMap_.get("INITIAL_MESSAGE"));
//					parmMap_.remove("INITIAL_MESSAGE");
//					returnMap_.remove("INITIAL_MESSAGE");
//				}
				if (!initialMsg.equals("")) {
					messageList.add(initialMsg);
				}
				wrkStr = "";
				if (originalFromRow > 0) {
					wrkStr = XFUtility.RESOURCE.getString("DialogCheckReadMessage1")
							+ (originalFromRow+1) + XFUtility.RESOURCE.getString("DialogCheckReadMessage2")
							+ (fromRow+blockRows) + XFUtility.RESOURCE.getString("DialogCheckReadMessage3")
							+ countOfRows + XFUtility.RESOURCE.getString("DialogCheckReadMessage4");
				}
				if (detailFunctionID.equals("NONE")) {
					messageList.add(XFUtility.RESOURCE.getString("FunctionMessage57") + wrkStr);
				} else {
					if (detailFunctionID.equals("")) {
						if (headersRenderer.isWithCheckBox_) {
							messageList.add(XFUtility.RESOURCE.getString("FunctionMessage5") + functionKeyToSelectRows + XFUtility.RESOURCE.getString("FunctionMessage6") + wrkStr);
						} else {
							messageList.add(XFUtility.RESOURCE.getString("FunctionMessage2") + wrkStr);
						}
					} else {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage3") + wrkStr);
					}
				}
				if (parmMap_.containsKey("INITIAL_MESSAGE")) {
					messageList.clear();
					messageList.add((String)parmMap_.get("INITIAL_MESSAGE"));
					parmMap_.remove("INITIAL_MESSAGE");
					returnMap_.remove("INITIAL_MESSAGE");
				}
			} else {
				messageList.add(XFUtility.RESOURCE.getString("FunctionMessage4"));
			}
			setMessagesOnPanel();

			if (countOfRows == 1 && functionElement_.getAttribute("ProcessRowIfOnlyOne").equals("T")) {
				jTableMain.setRowSelectionInterval(0, 0);
				processRow(false);
			}

		} catch(ScriptException e) {
			e.printStackTrace();
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch(Exception e) {
			e.printStackTrace();
			cancelWithException(e);
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public void resortTableRowsByColumnIndex(int col, boolean isAscending) {
		String workStr;
		ArrayList<Object> orderByValueList;
		ArrayList<String> orderByFieldTypeList;
		ArrayList<String> orderByFieldList = primaryTable_.getOrderByFieldIDList(isListingInNormalOrder);
		workStr = columnList.get(col).getDataSourceName();
		if (!isAscending) {
			workStr = workStr + "(D)";
		}
		orderByFieldList.add(0, workStr);
		
		workingRowList.clear();
		int rowCount = tableModelMain.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			orderByValueList =new ArrayList<Object>();
			orderByFieldTypeList =new ArrayList<String>();
			XF100_RowNumber cell = (XF100_RowNumber)tableModelMain.getValueAt(i, 0);
			for (int j = 0; j < orderByFieldList.size(); j++) {
				workStr = orderByFieldList.get(j).replace("(D)", "");
				workStr = workStr.replace("(A)", "");
				for (int k = 0; k < columnList.size(); k++) {
					if (columnList.get(k).getDataSourceName().equals(workStr)) {
						TableCellReadOnly cellObject = cell.getCellObjectList().get(k);
						orderByValueList.add(cellObject.getInternalValue());
						orderByFieldTypeList.add(columnList.get(k).getBasicType());
						break;
					}
				}
			}
			workingRowList.add(new WorkingRow(cell.getCellObjectList(), cell.getKeyMap(), cell.getColumnMap(), orderByValueList, orderByFieldTypeList));
		}

		for (int i = 0; i < rowCount; i++) {
			tableModelMain.removeRow(0);
		}

		WorkingRow[] workingRowArray = workingRowList.toArray(new WorkingRow[0]);
		Arrays.sort(workingRowArray);
		for (int i = 0; i < workingRowArray.length; i++) {
			Object[] cell = new Object[1];
			cell[0] = new XF100_RowNumber(i + 1, workingRowArray[i].getKeyMap(), workingRowArray[i].getColumnMap(), workingRowArray[i].getCellObjectList());
			tableModelMain.addRow(cell);
		}

		orderByFieldList.remove(0);
	}

	public void saveFilterValues() {
		if (!session_.getTableNameOfUserFilterValue().equals("")) {
			for (int i = 0; i < filterList.size(); i++) {
				if (filterList.get(i).isEditable()
						&& filterList.get(i).getBasicType().equals("STRING")
						&& filterList.get(i).getDefaultValue().equals("")) {
					session_.setFilterValue(getFunctionID(), filterList.get(i).getDataSourceName(), filterList.get(i).getValue().toString());
				}
			}
		}
	}
	
	public XFTableOperator getReferOperator(String sql) {
		XFTableOperator referTableOp = null;
		for (int k = 0; k < referOperatorList.size(); k++) {
			if (referOperatorList.get(k).getSqlText().equals(sql)) {
				referTableOp = referOperatorList.get(k);
				referTableOp.resetCursor();
			}
		}
		if (referTableOp == null ) {
			referTableOp = createTableOperator(sql);
			referOperatorList.add(referTableOp);
		}
		return referTableOp;
	}
	
	void jMenuItemToCall_actionPerformed(ActionEvent e) {
		processRow(false);
	}
	
	void jMenuItemToCallToAdd_actionPerformed(ActionEvent e) {
		doButtonAction(actionToCallToAdd);
	}
	
	void jMenuItemToOutput_actionPerformed(ActionEvent e) {
		doButtonAction("OUTPUT");
	}
	
	void jFunctionButton_actionPerformed(ActionEvent e) {
		Component com = (Component)e.getSource();
		for (int i = 0; i < 7; i++) {
			if (com.equals(jButtonArray[i])) {
				doButtonAction(actionDefinitionArray[i]);
				break;
			}
		}
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
		int pos1, pos2;
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			messageList.clear();

			if (action.equals("EXIT")) {
				closeFunction();
			}

			if (action.equals("ADD")) {
				try {
					HashMap<String, Object> returnMap = session_.executeFunction(detailFunctionID, null);
					if (returnMap.get("RETURN_CODE").equals("10")
							|| returnMap.get("RETURN_CODE").equals("20")
							|| returnMap.get("RETURN_CODE").equals("30")) {
						selectRowsAndList();
						messageList.clear();
					}
					if (returnMap.get("RETURN_MESSAGE") == null) {
						messageList.add(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
					} else {
						messageList.add(returnMap.get("RETURN_MESSAGE").toString());
					}
					if (returnMap.get("RETURN_TO") != null) {
						returnTo(returnMap.get("RETURN_TO").toString());
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
					exceptionHeader = e.getMessage();
					setErrorAndCloseFunction();
				}
			}

			if (action.equals("OUTPUT")) {
				session_.browseFile(getExcellBookURI());
			}

			if (action.contains("CALL")) {
				pos1 = action.indexOf("CALL(", 0);
				if (pos1 >= 0) {
					pos2 = action.indexOf(")", 0);
					String callAction = action.substring(pos1+5, pos2);
					if (!callAction.equals("")) {
						try {
							HashMap<String, Object> workMap = new HashMap<String, Object>();
							StringTokenizer workTokenizer1, workTokenizer2, workTokenizer3;
							String parmName, parmValue;
							workTokenizer1 = new StringTokenizer(callAction, "," );
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
							//HashMap<String, Object> returnMap = session_.executeFunction(functionID, parmMap_);
							HashMap<String, Object> returnMap = session_.executeFunction(functionID, workMap);
							if (returnMap.get("RETURN_CODE").equals("10")
									|| returnMap.get("RETURN_CODE").equals("20")
									|| returnMap.get("RETURN_CODE").equals("30")) {
								selectRowsAndList();
								messageList.clear();
							}
							if (returnMap.get("RETURN_MESSAGE") == null) {
								messageList.add(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
							} else {
								messageList.add(returnMap.get("RETURN_MESSAGE").toString());
							}
							if (returnMap.get("RETURN_TO") != null) {
								returnTo(returnMap.get("RETURN_TO").toString());
							}
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, e.getMessage());
							exceptionHeader = e.getMessage();
							setErrorAndCloseFunction();
						}
					}
				}
			}

			if (action.equals("NULL")) {
				if (functionElement_.getAttribute("ParmType").equals("COLUMNS")) {
					for (int i = 0; i < columnList.size(); i++) {
						returnMap_.put(columnList.get(i).getDataSourceName(), XFUtility.getNullValueOfBasicType(columnList.get(i).getBasicType()));
					}
				} else {
					for (int i = 0; i < columnList.size(); i++) {
						if (columnList.get(i).getTableID().equals(primaryTable_.getTableID())) {
							for (int j = 0; j < primaryTable_.getKeyFieldIDList().size(); j++) {
								if (columnList.get(i).getFieldID().equals(primaryTable_.getKeyFieldIDList().get(j))) {
									returnMap_.put(primaryTable_.getKeyFieldIDList().get(j), XFUtility.getNullValueOfBasicType(columnList.get(i).getBasicType()));
								}
							}
						}
					}
				}
				closeFunction();
			}

			if (action.equals("RETURN_ROWS")) {
				for (int i = 0; i < columnList.size(); i++) {
					returnMap_.put(columnList.get(i).getDataSourceName(), "");
				}
				for (int k = 0; k < tableModelMain.getRowCount(); k++) {
					XF100_RowNumber rowObject = (XF100_RowNumber)tableModelMain.getValueAt(k, 0);
					if (rowObject.isSelected()) {
						HashMap<String, Object> workMap = rowObject.getColumnMap();
						for (int i = 0; i < columnList.size(); i++) {
							Object value = returnMap_.get(columnList.get(i).getDataSourceName());
							if (!value.equals("")) {
								value = value + "<ListSeparator>";
							}
							value = value + workMap.get(columnList.get(i).getDataSourceName()).toString();
							returnMap_.put(columnList.get(i).getDataSourceName(), value);
						}
					}
				}
				returnMap_.put("RETURN_CODE", "10");
				if (returnMap_.get("RETURN_TO") != null) {
					returnTo(returnMap_.get("RETURN_TO").toString());
				}
			}

			setMessagesOnPanel();

		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public XF100_Column getColumnObjectByID(String tableID, String tableAlias, String fieldID) {
		XF100_Column columnField = null;
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
				if (columnList.get(i).getTableAlias().equals(tableAlias)
						&& columnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (columnList.get(i).getTableID().equals(tableID)
						&& columnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (columnList.get(i).getTableID().equals(tableID)
						&& columnList.get(i).getTableAlias().equals(tableAlias)
						&& columnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public XF100_Filter getFilterObjectByName(String dataSourceName) {
		XF100_Filter filter = null;
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

	boolean isTheRowToBeSelected() {
		boolean isToBeSelected = true;
		boolean isToBeSelectedInFilterGroup = false;
		String filterGroupID = "";
		for (int i = 0; i < filterList.size(); i++) {
			if (!filterList.get(i).isPrimaryWhere()
					&& filterList.get(i).isValueSpecified()) {
				if (!filterGroupID.equals(filterList.get(i).getFilterGroupID())) {
					if (!filterGroupID.equals("") && isToBeSelected) {
						isToBeSelected = isToBeSelectedInFilterGroup;
					}
					filterGroupID = filterList.get(i).getFilterGroupID();
					isToBeSelectedInFilterGroup = false;
				}
				if (filterList.get(i).isValidated()) {
					isToBeSelectedInFilterGroup = true;
				}
			}
		}
		if (!filterGroupID.equals("") && isToBeSelected) {
			isToBeSelected = isToBeSelectedInFilterGroup;
		}
		return isToBeSelected;
	}
	
	 class WorkingRow extends Object implements Comparable {
		private ArrayList<TableCellReadOnly> cellObjectList_ = null;
		private ArrayList<Object> orderByValueList_ = null;
		private ArrayList<String> orderByFieldTypeList_ = null;
		private HashMap<String, Object> keyMap_ = null;
		private HashMap<String, Object> columnMap_ = null;
		public WorkingRow(ArrayList<TableCellReadOnly> cellObjectList, HashMap<String, Object> keyMap, HashMap<String, Object> columnMap, ArrayList<Object> orderByValueList, ArrayList<String> orderByFieldTypeList) {
			cellObjectList_ = cellObjectList;
			keyMap_ = keyMap;
			columnMap_ = columnMap;
			orderByValueList_ = orderByValueList;
			orderByFieldTypeList_ = orderByFieldTypeList;
		}
		public ArrayList<TableCellReadOnly> getCellObjectList() {
			return cellObjectList_;
		}
		public HashMap<String, Object> getKeyMap() {
			return keyMap_;
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
            ArrayList<String> orderByFieldList = primaryTable_.getOrderByFieldIDList(isListingInNormalOrder);
            for (int i = 0; i < this.getOrderByValueList().size(); i++) {
            	if (this.getOrderByFieldTypeList().get(i).equals("INTEGER")
            			|| this.getOrderByFieldTypeList().get(i).equals("FLOAT")) {
//            		wrkStr = XFUtility.getStringNumber(this.getOrderByValueList().get(i).toString());
//            		doubleNumber1 = Double.parseDouble(wrkStr);
//            		wrkStr = XFUtility.getStringNumber(otherRow.getOrderByValueList().get(i).toString());
//            		doubleNumber2 = Double.parseDouble(wrkStr);
					if (this.getOrderByValueList().get(i).toString().equals("")) {
						doubleNumber1 = 0;
					} else {
						wrkStr = XFUtility.getStringNumber(this.getOrderByValueList().get(i).toString());
						doubleNumber1 = Double.parseDouble(wrkStr);
					}
					if (otherRow.getOrderByValueList().get(i).toString().equals("")) {
						doubleNumber2 = 0;
					} else {
						wrkStr = XFUtility.getStringNumber(otherRow.getOrderByValueList().get(i).toString());
						doubleNumber2 = Double.parseDouble(wrkStr);
					}
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
            	if (orderByFieldList.get(i).contains("(D)")) {
            		compareResult = compareResult * -1;
            	}
            	if (compareResult != 0) {
            		break;
            	}
            }
            return compareResult;
        }
	}

	 private URI getExcellBookURI() {
		File xlsFile = null;
		String xlsFileName = "";
		FileOutputStream fileOutputStream = null;
		String imageFileName = "";
		String wrkStr;
		int currentRowNumber = -1;

		XSSFWorkbook workBook = new XSSFWorkbook();
		wrkStr = functionElement_.getAttribute("Name").replace("/", "_").replace("Å^", "_");
		XSSFSheet workSheet = workBook.createSheet(wrkStr);
		workSheet.setDefaultRowHeight( (short) 300);
		Footer workSheetFooter = workSheet.getFooter();
		workSheetFooter.setRight(functionElement_.getAttribute("Name") + "  Page &P / &N");
		XSSFDataFormat format = workBook.createDataFormat();

		XSSFFont fontDefault = workBook.createFont();
		fontDefault.setFontName(session_.systemFont);
		fontDefault.setFontHeightInPoints((short)11);

		XSSFCellStyle styleHeader = workBook.createCellStyle();
		styleHeader.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		styleHeader.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		styleHeader.setBorderRight(XSSFCellStyle.BORDER_THIN);
		styleHeader.setBorderTop(XSSFCellStyle.BORDER_THIN);
		styleHeader.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
		styleHeader.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		styleHeader.setAlignment(XSSFCellStyle.ALIGN_LEFT);
		styleHeader.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		styleHeader.setFont(fontDefault);
		styleHeader.setWrapText(true);

		XSSFCellStyle styleHeaderNumber = workBook.createCellStyle();
		styleHeaderNumber.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		styleHeaderNumber.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		styleHeaderNumber.setBorderRight(XSSFCellStyle.BORDER_THIN);
		styleHeaderNumber.setBorderTop(XSSFCellStyle.BORDER_THIN);
		styleHeaderNumber.setFillForegroundColor(new XSSFColor(Color.LIGHT_GRAY));
		styleHeaderNumber.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
		styleHeaderNumber.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
		styleHeaderNumber.setVerticalAlignment(XSSFCellStyle.VERTICAL_CENTER);
		styleHeaderNumber.setFont(fontDefault);

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

		try {
			xlsFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".xlsx");
			xlsFileName = xlsFile.getPath();
			fileOutputStream = new FileOutputStream(xlsFileName);
			TableCellReadOnly cellObject = null;
			XF100_RowNumber rowObject;

			///////////////////////////
			// Setup column headings //
			///////////////////////////
			currentRowNumber++;
			XSSFRow rowCaption = workSheet.createRow(currentRowNumber);
			XSSFCell cell = rowCaption.createCell(0);
			cell.setCellStyle(styleHeaderNumber);
			workSheet.setColumnWidth(0, XFUtility.SEQUENCE_WIDTH * 40);
			wrkStr = XFUtility.getCaptionForCell(headersRenderer.getSequenceLabel());
			cell.setCellValue(new XSSFRichTextString(wrkStr));
			for (int j = 0; j < columnList.size(); j++) {
				if (columnList.get(j).isVisibleOnPanel()) {
					cell = rowCaption.createCell(j+1);
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
					Rectangle rect = headersRenderer.getColumnHeaderList().get(j).getBounds();
					workSheet.setColumnWidth(j+1, rect.width * 30);
					wrkStr = XFUtility.getCaptionForCell(headersRenderer.getColumnHeaderList().get(j).getText());
					wrkStr = wrkStr.replaceAll("<html>" , "");
					wrkStr = wrkStr.replaceAll("<u>" , "");
					cell.setCellValue(new XSSFRichTextString(wrkStr));
				} else {
					break;
				}
			}

			///////////////////////
			// create data cells //
			///////////////////////
			for (int i = 0; i < tableModelMain.getRowCount(); i++) {
				currentRowNumber++;
				XSSFRow rowData = workSheet.createRow(currentRowNumber);

				cell = rowData.createCell(0); //Column of Row Number
				cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellStyle(styleRowNumber);
				cell.setCellValue(i + 1);

				rowObject = (XF100_RowNumber)tableModelMain.getValueAt(i,0);
				for (int j = 0; j < columnList.size(); j++) {
					if (columnList.get(j).isVisibleOnPanel()) {
						cellObject = (TableCellReadOnly)rowObject.getCellObjectList().get(j);
						setupCellAttributes(rowData.createCell(j+1), workBook, columnList.get(j).getBasicType(), columnList.get(j).getTypeOptionList(), cellObject, columnList.get(j).getDecimalSize());
						if (cellObject.getValueType().equals("IMAGE") && !cellObject.getInternalValue().equals("")) {
							imageFileName = session_.getImageFileFolder() + cellObject.getInternalValue();
							//XFUtility.setupImageCellForColumn(workBook, workSheet, currentRowNumber, j+1, imageFileName);
							XFUtility.setupImageCell(workBook, workSheet, currentRowNumber, currentRowNumber+1, j+1, j+2, imageFileName);
						}
					}
				}
			}

			///////////////////////////////
			// Setup comments on filters //
			///////////////////////////////
			currentRowNumber++;
			XSSFRow rowRemarks = workSheet.createRow(currentRowNumber);
			cell = rowRemarks.createCell(0);
			CellStyle style = workBook.createCellStyle();
			style.setFont(fontDefault);
			cell.setCellStyle(style);
			StringBuffer buf = new StringBuffer();
			buf.append(XFUtility.RESOURCE.getString("XLSComment2"));
			for (int j = 0; j < filterList.size(); j++) {
				if (j>0) {
					buf.append("ÅA");
				}
				buf.append(filterList.get(j).getCaptionAndValue());
			}
			cell.setCellValue(new XSSFRichTextString(buf.toString()));

			workBook.write(fileOutputStream);
			messageList.add(XFUtility.RESOURCE.getString("XLSComment1"));

		} catch(Exception e) {
			messageList.add(XFUtility.RESOURCE.getString("XLSErrorMessage"));
			e.printStackTrace(exceptionStream);
		} finally {
			try {
				fileOutputStream.close();
			} catch(Exception e) {
				e.printStackTrace(exceptionStream);
			}
		}
		return xlsFile.toURI();
	}

	private void setupCellAttributes(XSSFCell cell, XSSFWorkbook workbook, String basicType, ArrayList<String> typeOptionList, TableCellReadOnly object, int decimalSize) {
		String wrk;

		XSSFFont font = workbook.createFont();
		font.setFontHeightInPoints((short)11);
		font.setFontName(session_.systemFont);
		if (object.getColor() != Color.BLACK) {
			font.setColor(new XSSFColor(object.getColor()));
		}
		XSSFDataFormat format = workbook.createDataFormat();
		CellStyle style = workbook.createCellStyle();
		style.setFont(font);
		style.setBorderBottom(XSSFCellStyle.BORDER_THIN);
		style.setBorderLeft(XSSFCellStyle.BORDER_THIN);
		style.setBorderRight(XSSFCellStyle.BORDER_THIN);
		style.setBorderTop(XSSFCellStyle.BORDER_THIN);

		if (basicType.equals("INTEGER")) {
			if (typeOptionList.contains("MSEQ") || typeOptionList.contains("FYEAR")) {
				cell.setCellType(XSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(new XSSFRichTextString(object.getExternalValue().toString()));
				style.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
				style.setWrapText(true);
				style.setDataFormat(format.getFormat("text"));
				cell.setCellStyle(style);
			} else {
				if (object.getExternalValue() == null) {
					wrk = "";
				} else {
					wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
				}
				if (wrk.equals("") || typeOptionList.contains("NO_EDIT")) {
					cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					cell.setCellValue(new XSSFRichTextString(wrk));
				} else {
					cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue(Double.parseDouble(wrk));
				}
				style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
				if (!typeOptionList.contains("NO_EDIT")
						&& !typeOptionList.contains("ZERO_SUPPRESS")) {
					style.setDataFormat(format.getFormat("#,##0"));
				}
				cell.setCellStyle(style);
			}
		} else {
			if (basicType.equals("FLOAT")) {
				if (object.getExternalValue() == null) {
					wrk = "";
				} else {
					wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
				}
				if (wrk.equals("") || typeOptionList.contains("NO_EDIT")) {
					cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					cell.setCellValue(new XSSFRichTextString(wrk));
				} else {
					cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
					cell.setCellValue(Double.parseDouble(wrk));
				}
				style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
				style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
				if (!typeOptionList.contains("NO_EDIT")
						&& !typeOptionList.contains("ZERO_SUPPRESS")) {
					style.setDataFormat(XFUtility.getFloatFormat(workbook, decimalSize));
				}
				cell.setCellStyle(style);
			} else {
				cell.setCellType(XSSFCell.CELL_TYPE_STRING);
				if (object.getExternalValue() == null || object.getValueType().equals("IMAGE")) {
					wrk = "";
				} else {
					if (object.getValueType().equals("FLAG")) {
						wrk = object.getInternalValue().toString();
					} else {
						wrk = object.getExternalValue().toString();
					}
				}
				cell.setCellValue(new XSSFRichTextString(wrk));
				style.setAlignment(XSSFCellStyle.ALIGN_LEFT);
				style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
				style.setWrapText(true);
				style.setDataFormat(format.getFormat("text"));
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

	void jScrollPaneTable_mouseClicked(MouseEvent e) {
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK) {
			int selectedRow = jTableMain.getSelectedRow();
			if (selectedRow == -1) {
				jMenuItemToSelect.setEnabled(false);
			} else {
				jMenuItemToSelect.setEnabled(true);
			}
			jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	void component_keyPressed(KeyEvent e) {
		//////////////////////////////////////
		// Steps to control listing request //
		//////////////////////////////////////
		if (e.getKeyCode() == KeyEvent.VK_L) {
			if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
				if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
					isListingInNormalOrder = false;
				} else {
					isListingInNormalOrder = true;
				}
				selectRowsAndList();
				saveFilterValues();
			}
		}

		///////////////////////////////////////////////
		// Steps to override F6 and F8 of JSplitPane //
		///////////////////////////////////////////////
		if (e.getKeyCode() == KeyEvent.VK_F6 && buttonIndexForF6 != -1) {
			jButtonArray[buttonIndexForF6].doClick();
		}
		if (e.getKeyCode() == KeyEvent.VK_F8 && buttonIndexForF8 != -1) {
			jButtonArray[buttonIndexForF8].doClick();
		}
	}

	void jTableMain_mouseClicked(MouseEvent e) {
		if (headersRenderer.isLinkedColumn(e.getPoint().x)) {
			callLinkedFunction(jTableMain.rowAtPoint(e.getPoint()), headersRenderer.getColumnIndex(e.getPoint().x));
		} else {
			if (headersRenderer.isWithCheckBox_) {
				processRow(false);
			} else {
				if (e.getClickCount() >= 2 && tableModelMain.getRowCount() > 0) {
					if (!detailFunctionID.equals("NONE")) {
						processRow(false);
					}
				} else {
					if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK) {
						int selectedRow = jTableMain.rowAtPoint(e.getPoint());
						jTableMain.setRowSelectionInterval(selectedRow, selectedRow);
						jMenuItemToSelect.setEnabled(true);
						jPopupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}
		}
	}

	void jTableMain_mouseMoved(MouseEvent e) {
		if (headersRenderer.isLinkedColumn(e.getPoint().x)) {
			setCursor(session_.editorKit.getLinkCursor());
		} else {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	void jTableMain_mouseExited(MouseEvent e) {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	void jTableMain_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP && jTableMain.getSelectedRow() == 0) {
			if (filterList.size() > 0) {
				filterList.get(0).requestFocus();
			} else {
				jButtonList.requestFocus();
			}
		}

		if (e.getKeyCode() == KeyEvent.VK_TAB) {
			if (filterList.size() > 0) {
				filterList.get(0).requestFocus();
			} else {
				jButtonList.requestFocus();
			}
		}

		if (e.getKeyCode() == KeyEvent.VK_ENTER && tableModelMain.getRowCount() > 0) {
			processRow(true);
		}
	
		///////////////////////////////////////////////
		// Steps to override F6 and F8 of JSplitPane //
		///////////////////////////////////////////////
		if (e.getKeyCode() == KeyEvent.VK_F6 && buttonIndexForF6 != -1) {
			jButtonArray[buttonIndexForF6].doClick();
		}
		if (e.getKeyCode() == KeyEvent.VK_F8 && buttonIndexForF8 != -1) {
			jButtonArray[buttonIndexForF8].doClick();
		}
	}

	void callLinkedFunction(int rowIndex, int columnIndex) {
		String linkedText = ""; Object value;
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			XF100_RowNumber rowObject = (XF100_RowNumber)tableModelMain.getValueAt(rowIndex, 0);
			String valueType = rowObject.getCellObjectList().get(columnIndex).getValueType();

			if (valueType.equals("URL")) {
				linkedText = rowObject.getCellObjectList().get(columnIndex).getInternalValue().toString();
				linkedText = linkedText.replaceAll("\\\\", "/");
				if (!linkedText.startsWith("http") && !linkedText.startsWith("mail")) {
					linkedText = linkedText.replace("file://", "");
					linkedText = "file://" + linkedText;
				}
				session_.browseFile(new URI(linkedText));
			}

			if (valueType.equals("LINKED")) {
				HashMap<String, Object> fieldValuesMap = new HashMap<String, Object>();
				XF100_Column column = getColumnList().get(columnIndex);
				for (int i = 0; i < column.getFieldsToPutList().size(); i++) {
					value = rowObject.getColumnMap().get(column.getFieldsToPutList().get(i));
					if (value == null) {
						JOptionPane.showMessageDialog(null, "Unable to send the value of field " + column.getFieldsToPutList().get(i));
					} else {
						//fieldValuesMap.put(column.getFieldsToPutToList().get(i), value);
						String dataSourceID = column.getFieldsToPutToList().get(i);
						fieldValuesMap.put(dataSourceID, value);
						int index = dataSourceID.indexOf(".");
						if (index > -1) {
							String fieldID = dataSourceID.substring(index, dataSourceID.length());
							fieldValuesMap.put(fieldID, value);
						}
					}
				}

				linkedText = column.getLinkedFunctionID();
				HashMap<String, Object> returnMap = session_.executeFunction(linkedText, fieldValuesMap);
				if (returnMap.get("RETURN_TO") != null) {
					returnTo(returnMap.get("RETURN_TO").toString());
				}
			}

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Unable to call the linked Function.\n" + linkedText);
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	void processRow(boolean enterKeyPressed) {
		messageList.clear();
		if (detailFunctionID.equals("")) {
			if (headersRenderer.isWithCheckBox_) {
				if (jTableMain.getSelectedRow() > -1) {
					XF100_RowNumber rowObject = (XF100_RowNumber)tableModelMain.getValueAt(jTableMain.getSelectedRow(), 0);
					rowObject.setSelected(!rowObject.isSelected());
					jTableMain.repaint();
					buttonReturnRows.setEnabled(false);
					for (int i = 0; i < tableModelMain.getRowCount(); i++) {
						rowObject = (XF100_RowNumber)tableModelMain.getValueAt(i, 0);
						if (rowObject.isSelected()) {
							buttonReturnRows.setEnabled(true);
							break;
						}
					}
				}

			} else {
				int rowNumber = jTableMain.convertRowIndexToModel(jTableMain.getSelectedRow());
				if (rowNumber > -1) {
					XF100_RowNumber tableRowNumber = (XF100_RowNumber)tableModelMain.getValueAt(rowNumber,0);
					HashMap<String, Object> workMap = tableRowNumber.getKeyMap();
					if (functionElement_.getAttribute("ParmType").equals("COLUMNS")) {
						workMap = tableRowNumber.getColumnMap();
					}
					returnMap_.putAll(workMap);
					returnMap_.put("RETURN_CODE", "00");
					closeFunction();
				}
			}
		} else {
			if (jTableMain.getSelectedRow() > -1) {
				try {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));

					int rowNumber = jTableMain.convertRowIndexToModel(jTableMain.getSelectedRow());
					if (rowNumber > -1) {
						XF100_RowNumber tableRowNumber = (XF100_RowNumber)tableModelMain.getValueAt(rowNumber,0);
						HashMap<String, Object> workMap = tableRowNumber.getKeyMap();
						if (functionElement_.getAttribute("ParmType").equals("COLUMNS")) {
							workMap = tableRowNumber.getColumnMap();
						}
						if (!functionElement_.getAttribute("ParmAdditional").equals("")) {
							StringTokenizer workTokenizer1 = new StringTokenizer(functionElement_.getAttribute("ParmAdditional"), ";" );
							while (workTokenizer1.hasMoreTokens()) {
								StringTokenizer workTokenizer2 = new StringTokenizer(workTokenizer1.nextToken(), ":" );
								if (workTokenizer2.countTokens() == 2) {
									String parmName = workTokenizer2.nextToken();
									String parmValue = workTokenizer2.nextToken();
									workMap.put(parmName, parmValue);
								}
							}
						}
						workMap.putAll(parmMap_);

						try {
							HashMap<String, Object> returnMap = session_.executeFunction(detailFunctionID, workMap);
							if (returnMap.get("RETURN_CODE").equals("10")
									|| returnMap.get("RETURN_CODE").equals("20")
									|| returnMap.get("RETURN_CODE").equals("30")) {
								selectRowsAndList();
								messageList.clear();
							}
							if (returnMap.get("RETURN_MESSAGE") == null) {
								messageList.add(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
							} else {
								messageList.add(returnMap.get("RETURN_MESSAGE").toString());
							}
							if (returnMap.get("RETURN_TO") != null) {
								returnMap_.putAll(returnMap);
								returnTo(returnMap.get("RETURN_TO").toString());
							}
						} catch (Exception e) {
							messageList.add(XFUtility.RESOURCE.getString("FunctionError15") + " " + detailFunctionID);
						}
					}
				} finally {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				}
			}

			if (enterKeyPressed && jTableMain.getSelectedRow() >= 0) {
				if (jTableMain.getSelectedRow() == 0) {
					jTableMain.setRowSelectionInterval(jTableMain.getRowCount() - 1, jTableMain.getRowCount() - 1);
				} else {
					jTableMain.setRowSelectionInterval(jTableMain.getSelectedRow() - 1, jTableMain.getSelectedRow() - 1);
				}
			}

			setMessagesOnPanel();
		}
	}

	void setMessagesOnPanel() {
		jTextAreaMessages.setText("");
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
		jTextAreaMessages.setText(sb.toString());
		jTextAreaMessages.setCaretPosition(0);
	}

	public String getFunctionID() {
		return functionElement_.getAttribute("ID");
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
		return new XFTableOperator(session_, processLog, sqlText);
	}

	public XFTableEvaluator createTableEvaluator(String tableID) {
		return new XFTableEvaluator(this, tableID);
	}

	public HashMap<String, Object> getReturnMap() {
		return returnMap_;
	}

	public String getScriptNameRunning() {
		return scriptNameRunning;
	}

	public Session getSession() {
		return session_;
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
			if (compiledScriptMap.containsKey(scriptNameRunning)) {
				compiledScriptMap.get(scriptNameRunning).eval(scriptBindings);
			} else {
				StringBuffer bf = new StringBuffer();
				bf.append(scriptText);
				bf.append(session_.getScriptFunctions());
				Compilable compiler = (Compilable)session_.getScriptEngineManager().getEngineByName("js");
				CompiledScript script = compiler.compile(bf.toString());
				script.eval(scriptBindings);
				compiledScriptMap.put(scriptNameRunning, script);
			}
		}
	}

	public void executeScript(String scriptText) {
		try {
			evalScript("Internal Script", scriptText);
		} catch (Exception e) {}
	}

	public ArrayList<XF100_Filter> getFilterList() {
		return filterList;
	}
	
	public ArrayList<XF100_Column> getColumnList() {
		return columnList;
	}

	public boolean scriptIsToBeExecuted(String script) {
		boolean result = true;
		for (int i = 0; i < columnList.size(); i++) {
			if (!script.contains(columnList.get(i).getDataSourceID())) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	public ArrayList<XF100_ReferTable> getReferTableList() {
		return referTableList;
	}

	public String getPrimaryTableID() {
		return primaryTable_.getTableID();
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

	class TableHeadersRenderer extends JPanel implements TableCellRenderer {   
		private static final long serialVersionUID = 1L;
		private JPanel westPanel = new JPanel();
		private JLabel numberLabel = new JLabel("No.");
		private JPanel checkBoxPanel = new JPanel();
		private JCheckBox checkBox = new JCheckBox();
		private JPanel centerPanel = new JPanel();
		private ArrayList<JLabel> headerList = new ArrayList<JLabel>();
		private ArrayList<String> dataTypeList = new ArrayList<String>();
		private int totalWidthOfCenterPanel = 0;
		private int totalHeight = 0;
		private Component sizingHeader = null;
		private JLabel sortingColumn = null;
		private boolean isAscendingColumnSorting = true;
		private XF100 dialog_;
		private boolean isWithCheckBox_ = false;

		public TableHeadersRenderer(XF100 dialog, boolean isWithCheckBox) {
			dialog_ = dialog;
			isWithCheckBox_ = isWithCheckBox;
			numberLabel.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
			numberLabel.setBorder(new HeaderBorder());
			numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
			numberLabel.setOpaque(true);
			centerPanel.setLayout(null);

			if (isWithCheckBox_) {
				GridLayout layout = new GridLayout();
				layout.setColumns(2);
				layout.setRows(1);
				westPanel.setLayout(layout);
				westPanel.setPreferredSize(new Dimension(XFUtility.SEQUENCE_WIDTH*2, 10));
				arrangeColumnsPosition(true);
				checkBox.setHorizontalAlignment(SwingConstants.CENTER);
				checkBoxPanel.setBorder(new HeaderBorder());
				checkBoxPanel.setLayout(new BorderLayout());
				checkBoxPanel.add(checkBox, BorderLayout.CENTER);
				westPanel.add(numberLabel);
				westPanel.add(checkBoxPanel);
				this.setLayout(new BorderLayout());
				this.add(westPanel, BorderLayout.WEST);
				this.add(centerPanel, BorderLayout.CENTER);

			} else {
				arrangeColumnsPosition(true);
				this.setLayout(new BorderLayout());
				this.add(numberLabel, BorderLayout.WEST);
				this.add(centerPanel, BorderLayout.CENTER);
			}
		}

		public boolean isWithCheckBox() {
			return isWithCheckBox_;
		}
		
		public int getWestPanelWidth() {
			return westPanel.getPreferredSize().width;
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
		
		public void setSelected(boolean isChecked) {
			checkBox.setSelected(isChecked);
		}
		
		public boolean hasMouseOnColumnBorder(int headerPosX, int headerPosY) {
			boolean result = false;
			double posX;
			if (isWithCheckBox_) {
				posX = headerPosX - westPanel.getBounds().getWidth();
			} else {
				posX = headerPosX - numberLabel.getBounds().getWidth();
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
			if (isWithCheckBox_) {
				posX = headerPosX - numberLabel.getBounds().getWidth() - checkBox.getBounds().getWidth();
			} else {
				posX = headerPosX - numberLabel.getBounds().getWidth();
			}
			for (int i = 0; i < headerList.size(); i++) {
				headerList.get(i).setText(headerList.get(i).getText().replace("<html><u>", ""));
				if (posX >= headerList.get(i).getBounds().x
				&& posX < (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width)
				&& headerPosY >= headerList.get(i).getBounds().y
				&& headerPosY < (headerList.get(i).getBounds().y + headerList.get(i).getBounds().height)) {
					headerList.get(i).setText("<html><u>"+headerList.get(i).getText());
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
			double posX;
			if (isWithCheckBox_) {
				posX = headerPosX - numberLabel.getBounds().getWidth() - checkBox.getBounds().getWidth();
			} else {
				posX = headerPosX - numberLabel.getBounds().getWidth();
			}
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
			double posX;
			if (isWithCheckBox_) {
				posX = headersPosX - westPanel.getBounds().getWidth();
				sizingHeader = westPanel;
			} else {
				posX = headersPosX - numberLabel.getBounds().getWidth();
				sizingHeader = numberLabel;
			}
			for (int i = 0; i < headerList.size(); i++) {
				if (posX >= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width - 3)
						&& posX <= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width)) {
					sizingHeader = headerList.get(i);
					break;
				}
			}
		}
		
		public void setNewBoundsToHeaders(int posXOnHeaders) {
			if (isWithCheckBox_) {
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
								columnList.get(i).setWidth(newWidth);
								widthAdjusted = newWidth - headerList.get(i).getBounds().width;
							}
							break;
						}
					}
					if (widthAdjusted != 0) {
						arrangeColumnsPosition(false);
					}
				}
				
			} else {
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
								columnList.get(i).setWidth(newWidth);
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
		}
		
		public void arrangeColumnsPosition(boolean isWithDefaultSequenceWidth) {
			int fromX = 0;
			int fromY = 0;
			int width, height, wrkInt1, wrkInt2;
			JLabel header;
			totalWidthOfCenterPanel = 0;
			centerPanel.removeAll();
			headerList.clear();
			for (int i = 0; i < columnList.size(); i++) {
				if (columnList.get(i).isVisibleOnPanel()) {
					header = new JLabel();
					header.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
					if (columnList.get(i).getValueType().equals("IMAGE")
							|| columnList.get(i).getValueType().equals("FLAG")) {
						header.setHorizontalAlignment(SwingConstants.CENTER);
					} else {
						if (columnList.get(i).getBasicType().equals("INTEGER")
								|| columnList.get(i).getBasicType().equals("FLOAT")) {
							header.setHorizontalAlignment(SwingConstants.RIGHT);
						} else {
							header.setHorizontalAlignment(SwingConstants.LEFT);
						}
					}
					header.setText(columnList.get(i).getCaption());
					header.setOpaque(true);

					width = columnList.get(i).getWidth();
					height = XFUtility.ROW_UNIT_HEIGHT * columnList.get(i).getRows();
					if (i > 0) {
						fromX = headerList.get(i-1).getBounds().x + headerList.get(i-1).getBounds().width;
						fromY = headerList.get(i-1).getBounds().y + headerList.get(i-1).getBounds().height;
						for (int j = i-1; j >= 0; j--) {
							if (columnList.get(i).getLayout().equals("VERTICAL")) {
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
					dataTypeList.add(columnList.get(i).getValueType());

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
			if (isWithCheckBox_) {
				this.setPreferredSize(new Dimension(totalWidthOfCenterPanel + westPanel.getPreferredSize().width, totalHeight));
			} else {
				this.setPreferredSize(new Dimension(totalWidthOfCenterPanel + numberLabel.getPreferredSize().width, totalHeight));
			}
		}

		public int getColumnIndex(int posX) {
			int index = -1;
			int posXOnCenterPanel = 0;
			if (isWithCheckBox_) {
				posXOnCenterPanel = posX - westPanel.getPreferredSize().width;
			} else {
				posXOnCenterPanel = posX - numberLabel.getPreferredSize().width;
			}
			for (int i = 0; i < headerList.size(); i++) {
				if (posXOnCenterPanel >= headerList.get(i).getBounds().x
						&& posXOnCenterPanel <= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width)) {
					index = i;
					break;
				}
			}
			return index;
		}

		public boolean isLinkedColumn(int posX) {
			boolean isLinkedColumn = false;
			int posXOnCenterPanel = 0;
			if (isWithCheckBox_) {
				posXOnCenterPanel = posX - westPanel.getPreferredSize().width;
			} else {
				posXOnCenterPanel = posX - numberLabel.getPreferredSize().width;
			}
			for (int i = 0; i < headerList.size(); i++) {
				if (posXOnCenterPanel >= headerList.get(i).getBounds().x
						&& posXOnCenterPanel <= (headerList.get(i).getBounds().x + headerList.get(i).getBounds().width)) {
					if (dataTypeList.get(i).equals("URL") || dataTypeList.get(i).equals("LINKED")) {
						isLinkedColumn = true;
					}
					break;
				}
			}
			return isLinkedColumn;
		}
		
		public String getToolTipText(MouseEvent e) {
			String text = "";
			if (e.getPoint().x > numberLabel.getPreferredSize().width) {
				Component compo = centerPanel.getComponentAt(e.getPoint().x-numberLabel.getPreferredSize().width, e.getPoint().y);
				if (compo != null) {
					for (int i = 0; i < headerList.size(); i++) {
						if (compo.equals(headerList.get(i))) {
							if (columnList.get(i).getDecimalSize() > 0) {
								text = "<html>" + columnList.get(i).getFieldName() + " " + columnList.get(i).getDataSourceName() + " (" + columnList.get(i).getDataSize() + "," + columnList.get(i).getDecimalSize() + ")<br>" + columnList.get(i).getFieldRemarks();
							} else {
								text = "<html>" + columnList.get(i).getFieldName() + " " + columnList.get(i).getDataSourceName() + " (" + columnList.get(i).getDataSize() + ")<br>" + columnList.get(i).getFieldRemarks();
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

		public void checkSelection(MouseEvent e) {
			XF100_RowNumber rowObject;
			if (isWithCheckBox_ && e.getX() > numberLabel.getWidth() && e.getX() <= westPanel.getWidth()) {
				buttonReturnRows.setEnabled(false);
				checkBox.setSelected(!checkBox.isSelected());
				for (int i = 0; i < tableModelMain.getRowCount(); i++) {
					rowObject = (XF100_RowNumber)tableModelMain.getValueAt(i, 0);
					rowObject.setSelected(checkBox.isSelected());
					if (rowObject.isSelected()) {
						buttonReturnRows.setEnabled(true);
					}
				}
			}
		}
	}  

	public class TableCellsRenderer extends JPanel implements TableCellRenderer {
		private static final long serialVersionUID = 1L;
		private JPanel westPanel = new JPanel();
		private JLabel numberCell = new JLabel("");
		private JPanel checkBoxPanel = new JPanel();
		private JCheckBox checkBox = new JCheckBox();
		private JPanel centerPanel = new JPanel();
		private ArrayList<JLabel> cellList = new ArrayList<JLabel>();
		private TableHeadersRenderer headersRenderer_;

		public TableCellsRenderer(TableHeadersRenderer headersRenderer) {
			headersRenderer_ = headersRenderer;

			if (headersRenderer_.isWithCheckBox_) {
				GridLayout layout = new GridLayout();
				layout.setColumns(2);
				layout.setRows(1);
				westPanel.setLayout(layout);
				numberCell.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
				numberCell.setBorder(new CellBorder());
				numberCell.setHorizontalAlignment(SwingConstants.CENTER);
				numberCell.setOpaque(true);
				checkBox.setHorizontalAlignment(SwingConstants.CENTER);
				checkBoxPanel.setBorder(new CellBorder());
				checkBoxPanel.setLayout(new BorderLayout());
				checkBoxPanel.add(checkBox, BorderLayout.CENTER);
				westPanel.add(numberCell);
				westPanel.add(checkBoxPanel);

				centerPanel.setLayout(null);
				centerPanel.setOpaque(false);
				setupCellBounds();
				this.setLayout(new BorderLayout());
				this.add(westPanel, BorderLayout.WEST);
				this.add(centerPanel, BorderLayout.CENTER);

			} else {
				numberCell.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
				numberCell.setBorder(new CellBorder());
				numberCell.setHorizontalAlignment(SwingConstants.CENTER);
				centerPanel.setLayout(null);
				centerPanel.setOpaque(false);
				setupCellBounds();
				this.setLayout(new BorderLayout());
				this.add(numberCell, BorderLayout.WEST);
				this.add(centerPanel, BorderLayout.CENTER);
			}
		}
		
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				numberCell.setBackground(table.getSelectionBackground());
				checkBox.setBackground(table.getSelectionBackground());
				numberCell.setForeground(table.getSelectionForeground());
			} else {
				if (row%2==0) {
					setBackground(SystemColor.text);
					numberCell.setBackground(SystemColor.text);
					checkBox.setBackground(SystemColor.text);
				} else {
					setBackground(XFUtility.ODD_ROW_COLOR);
					numberCell.setBackground(XFUtility.ODD_ROW_COLOR);
					checkBox.setBackground(XFUtility.ODD_ROW_COLOR);
				}
				numberCell.setForeground(table.getForeground());
			}
			setFocusable(false);

			XF100_RowNumber rowObject = (XF100_RowNumber)value;
			numberCell.setText(rowObject.getRowNumberString());
			if (headersRenderer_.isWithCheckBox_) {
				checkBox.setSelected(rowObject.isSelected());
			}
			for (int i = 0; i < cellList.size(); i++) {
				cellList.get(i).setEnabled(columnList.get(i).isEnabled());
				if (columnList.get(i).getValueType().equals("IMAGE")
						|| columnList.get(i).getValueType().equals("FLAG")) {
					cellList.get(i).setIcon((Icon)rowObject.getCellObjectList().get(i).getExternalValue());
				} else {
					cellList.get(i).setText((String)rowObject.getCellObjectList().get(i).getExternalValue());
					if (isSelected) {
						if (rowObject.getCellObjectList().get(i).getValueType().equals("URL") || rowObject.getCellObjectList().get(i).getValueType().equals("LINKED")) {
							cellList.get(i).setText("<html><u><font color='#00ffff'>"+rowObject.getCellObjectList().get(i).getExternalValue());
						} else {
							if (rowObject.getCellObjectList().get(i).getColor().equals(Color.black)) {
								cellList.get(i).setForeground(table.getSelectionForeground());
							} else {
								if (rowObject.getCellObjectList().get(i).getColor().equals(Color.blue)) {
									cellList.get(i).setForeground(Color.cyan);
								} else {
									cellList.get(i).setForeground(rowObject.getCellObjectList().get(i).getColor());
								}
							}
						}
					} else {
						if (rowObject.getCellObjectList().get(i).getValueType().equals("URL") || rowObject.getCellObjectList().get(i).getValueType().equals("LINKED")) {
							cellList.get(i).setText("<html><u><font color='blue'>"+rowObject.getCellObjectList().get(i).getExternalValue());
						} else {
							if (rowObject.getCellObjectList().get(i).getColor().equals(Color.black)) {
								cellList.get(i).setForeground(table.getForeground());
							} else {
								cellList.get(i).setForeground(rowObject.getCellObjectList().get(i).getColor());
							}
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
			centerPanel.removeAll();
			for (int i = 0; i < headersRenderer_.getColumnHeaderList().size(); i++) {
				cell = new JLabel();
				cell.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
				cell.setHorizontalAlignment(headersRenderer_.getColumnHeaderList().get(i).getHorizontalAlignment());
				rec = headersRenderer_.getColumnHeaderList().get(i).getBounds();
				cell.setBounds(rec.x, rec.y, rec.width, rec.height);
				cell.setBorder(new HeaderBorder());
				cellList.add(cell);
				centerPanel.add(cell);
			}
			if (headersRenderer_.isWithCheckBox_) {
				int totalWidth = headersRenderer_.getWidth() - headersRenderer_.getWestPanelWidth();
				int totalHeight = headersRenderer_.getHeight();
				centerPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
				westPanel.setPreferredSize(new Dimension(headersRenderer_.getWestPanelWidth(), totalHeight));
				this.setPreferredSize(new Dimension(totalWidth + headersRenderer_.getWestPanelWidth(), totalHeight));
			} else {
				int totalWidth = headersRenderer_.getWidth() - headersRenderer_.getSequenceWidth();
				int totalHeight = headersRenderer_.getHeight();
				centerPanel.setPreferredSize(new Dimension(totalWidth, totalHeight));
				numberCell.setPreferredSize(new Dimension(headersRenderer_.getSequenceWidth(), totalHeight));
				this.setPreferredSize(new Dimension(totalWidth + headersRenderer_.getSequenceWidth(), totalHeight));
			}
		}
	} 
}

class XF100_Filter extends JPanel {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element fieldElement_ = null;
	private XF100 dialog_ = null;
	private String dataType = "";
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private String basicType = "";
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private String fieldName = "";
	private String fieldRemarks = "";
	private String fieldCaption = "";
	private String fieldOptions = "";
	private ArrayList<String> fieldOptionList;
	private String componentType = "";
	private String operand = "";
	private String operandType = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private JPanel jPanelField = new JPanel();
	private JPanel jPanelCaption = new JPanel();
	private JLabel jLabelField = new JLabel();
	private JButton jButtonOperand = new JButton();
	private JPopupMenu jPopupMenu = new JPopupMenu();
	private XFTextField xFTextField = null;
	private XFInputAssistField xFInputAssistField = null;
	private XFCheckBox xFCheckBox = null;
	private XFDateField xFDateField = null;
	private XFYMonthBox xFYMonthBox = null;
	private XFMSeqBox xFMSeqBox = null;
	private XFFYearBox xFFYearBox = null;
	private JComboBox jComboBox = null;
	private XF100_PromptCallField xFPromptCall = null;
	private ArrayList<String> keyValueList = new ArrayList<String>();
	private JComponent component = null;
	private boolean isVertical = false;
	private int verticalMargin = 8;
	private int horizontalMargin = 50;
	private boolean isReflect = false;
	private boolean isEditable_ = true;
	private boolean isHidden = false;
	private boolean isVirtualField = false;
	private boolean isPrimaryWhere = false;
	private String filterGroupID = "";
	private JRadioButtonMenuItem itemEQ = new JRadioButtonMenuItem(" = ");
	private JRadioButtonMenuItem itemNE = new JRadioButtonMenuItem(" != ");
	private JRadioButtonMenuItem itemGE = new JRadioButtonMenuItem(" >= ");
	private JRadioButtonMenuItem itemGT = new JRadioButtonMenuItem(" > ");
	private JRadioButtonMenuItem itemLE = new JRadioButtonMenuItem(" <= ");
	private JRadioButtonMenuItem itemLT = new JRadioButtonMenuItem(" < ");
	private JRadioButtonMenuItem itemGeneric = new JRadioButtonMenuItem(" ?* ");
	private JRadioButtonMenuItem itemScan = new JRadioButtonMenuItem(" *?* ");
	private ButtonGroup itemGroup = new ButtonGroup();
	private ImageIcon buttonIcon = null;

	public XF100_Filter(org.w3c.dom.Element fieldElement, XF100 dialog) throws Exception {
		super();
		String wrkStr;
		fieldElement_ = fieldElement;
		dialog_ = dialog;

		fieldOptions = fieldElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);
		StringTokenizer workTokenizer1 = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer1.nextToken();
		tableID = dialog_.getTableIDOfTableAlias(tableAlias);
		fieldID =workTokenizer1.nextToken();

		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID + "." + fieldID + XFUtility.RESOURCE.getString("FunctionError11"));
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
		if (dataSize > 50) {
			dataSize = 50;
		}
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}

		if (fieldOptionList.contains("VERTICAL")) {
			isVertical = true;
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "VERTICAL");
		if (!wrkStr.equals("")) {
			isVertical = true;
			verticalMargin = Integer.parseInt(wrkStr);
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "HORIZONTAL");
		if (!wrkStr.equals("")) {
			horizontalMargin = Integer.parseInt(wrkStr) + 5;
		}

		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		} else {
			if (tableID.equals(dialog_.getPrimaryTableID())) {
				isPrimaryWhere = true;
			}
		}
		
		if (fieldOptionList.contains("REFLECT")) {
			operandType = "REFLECT";
			operand = "";
			isReflect = true;
			buttonIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("filterReflect.png"));
		 	jButtonOperand.setIcon(buttonIcon);
		} else {
			if (fieldOptionList.contains("PROMPT_LIST1") || fieldOptionList.contains("PROMPT_LIST2")) {
				jPopupMenu.add(itemEQ);
				jPopupMenu.add(itemNE);
				itemGroup.add(itemEQ);
				itemGroup.add(itemNE);
			} else {
				basicType = XFUtility.getBasicTypeOf(dataType);
				if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
					jPopupMenu.add(itemEQ);
					jPopupMenu.add(itemNE);
					jPopupMenu.add(itemGE);
					jPopupMenu.add(itemGT);
					jPopupMenu.add(itemLE);
					jPopupMenu.add(itemLT);
					itemGroup.add(itemEQ);
					itemGroup.add(itemNE);
					itemGroup.add(itemGE);
					itemGroup.add(itemGT);
					itemGroup.add(itemLE);
					itemGroup.add(itemLT);
				}
				if (basicType.equals("STRING") || basicType.equals("CLOB")) {
					jPopupMenu.add(itemEQ);
					jPopupMenu.add(itemNE);
					jPopupMenu.add(itemScan);
					jPopupMenu.add(itemGeneric);
					itemGroup.add(itemEQ);
					itemGroup.add(itemNE);
					itemGroup.add(itemScan);
					itemGroup.add(itemGeneric);
				}
				if (basicType.equals("BINARY") || basicType.equals("BYTEA")) {
					jPopupMenu.add(itemEQ);
					jPopupMenu.add(itemNE);
					itemGroup.add(itemEQ);
					itemGroup.add(itemNE);
				}
				if (basicType.equals("DATE")) {
					jPopupMenu.add(itemEQ);
					jPopupMenu.add(itemNE);
					jPopupMenu.add(itemGE);
					jPopupMenu.add(itemGT);
					jPopupMenu.add(itemLE);
					jPopupMenu.add(itemLT);
					itemGroup.add(itemEQ);
					itemGroup.add(itemNE);
					itemGroup.add(itemGE);
					itemGroup.add(itemGT);
					itemGroup.add(itemLE);
					itemGroup.add(itemLT);
				}
				if (basicType.equals("TIME") || basicType.equals("DATETIME")) {
					jPopupMenu.add(itemEQ);
					jPopupMenu.add(itemNE);
					jPopupMenu.add(itemGE);
					jPopupMenu.add(itemGT);
					jPopupMenu.add(itemLE);
					jPopupMenu.add(itemLT);
					jPopupMenu.add(itemGeneric);
					itemGroup.add(itemEQ);
					itemGroup.add(itemNE);
					itemGroup.add(itemGE);
					itemGroup.add(itemGT);
					itemGroup.add(itemLE);
					itemGroup.add(itemLT);
					itemGroup.add(itemGeneric);
				}
			}
			itemEQ.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					operandType = "EQ";
					operand = " = ";
					buttonIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("filterEQ.png"));
				 	jButtonOperand.setIcon(buttonIcon);
				}
			});
			itemNE.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					operandType = "NE";
					operand = " != ";
					buttonIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("filterNE.png"));
				 	jButtonOperand.setIcon(buttonIcon);
				}
			});
			itemGE.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					operandType = "GE";
					operand = " >= ";
					buttonIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("filterGE.png"));
				 	jButtonOperand.setIcon(buttonIcon);
				}
			});
			itemGT.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					operandType = "GT";
					operand = " > ";
					buttonIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("filterGT.png"));
				 	jButtonOperand.setIcon(buttonIcon);
				}
			});
			itemLE.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					operandType = "LE";
					operand = " <= ";
					buttonIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("filterLE.png"));
				 	jButtonOperand.setIcon(buttonIcon);
				}
			});
			itemLT.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					operandType = "LT";
					operand = " < ";
					buttonIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("filterLT.png"));
				 	jButtonOperand.setIcon(buttonIcon);
				}
			});
			itemScan.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					operandType = "SCAN";
					operand = " LIKE ";
					buttonIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("filterScan.png"));
				 	jButtonOperand.setIcon(buttonIcon);
				}
			});
			itemGeneric.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					operandType = "GENERIC";
					operand = " LIKE ";
					buttonIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("filterGeneric.png"));
				 	jButtonOperand.setIcon(buttonIcon);
				}
			});

			itemEQ.setSelected(true);
			itemEQ.doClick();
			if (fieldOptionList.contains("GE")) {
				itemGE.setSelected(true);
				itemGE.doClick();
			}
			if (fieldOptionList.contains("GT")) {
				itemGT.setSelected(true);
				itemGT.doClick();
			}
			if (fieldOptionList.contains("LE")) {
				itemLE.setSelected(true);
				itemLE.doClick();
			}
			if (fieldOptionList.contains("LT")) {
				itemLT.setSelected(true);
				itemLT.doClick();
			}
			if (fieldOptionList.contains("SCAN")) {
				itemScan.setSelected(true);
				itemScan.doClick();
			}
			if (fieldOptionList.contains("GENERIC")) {
				itemGeneric.setSelected(true);
				itemGeneric.doClick();
			}
		}

		filterGroupID = tableAlias + "." + fieldID + ":" + operand;

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = XFUtility.getCaptionValue(wrkStr, dialog_.getSession());
			fieldCaption = fieldCaption.replace("SCAN","");
			fieldCaption = fieldCaption.replace("ÅÜ","");
			fieldCaption = fieldCaption.replace("è„à åÖ","");
		}
		jLabelField = new JLabel(fieldCaption);
		jLabelField.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelField.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		int width = XFUtility.adjustFontSizeToGetPreferredWidthOfLabel(jLabelField, XFUtility.DEFAULT_LABEL_WIDTH - 20);
//		if (isVertical || dialog_.getFilterList().size() == 0) {
//			jLabelField.setPreferredSize(new Dimension(XFUtility.DEFAULT_LABEL_WIDTH, XFUtility.FIELD_UNIT_HEIGHT));
//		} else {
//			jLabelField.setPreferredSize(new Dimension(width + horizontalMargin, XFUtility.FIELD_UNIT_HEIGHT));
//		}
		if (isVertical || dialog_.getFilterList().size() == 0) {
			jPanelCaption.setPreferredSize(new Dimension(XFUtility.DEFAULT_LABEL_WIDTH, XFUtility.FIELD_UNIT_HEIGHT));
		} else {
			jPanelCaption.setPreferredSize(new Dimension(width + horizontalMargin, XFUtility.FIELD_UNIT_HEIGHT));
		}
		jButtonOperand.setPreferredSize(new Dimension(20, XFUtility.FIELD_UNIT_HEIGHT));
		jButtonOperand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component com = (Component)e.getSource();
				jPopupMenu.show(com, 10, 10);
			}
		});
		jButtonOperand.setFocusable(false);
//		jButtonOperand.addKeyListener(new XF100_Component_keyAdapter(dialog));
//		jButtonOperand.addKeyListener(new KeyAdapter() {
//			public void keyPressed(KeyEvent e) {
//			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
//					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//						jButton.doClick();
//					}
//				}
//			} 
//		});
		jPanelCaption.setLayout(new BorderLayout());
		jPanelCaption.add(jLabelField, BorderLayout.CENTER);
		jPanelCaption.add(jButtonOperand, BorderLayout.EAST);

		jPanelField.setLayout(null);
		this.setLayout(new BorderLayout());
		this.add(jPanelCaption, BorderLayout.WEST);
		this.add(jPanelField, BorderLayout.CENTER);

		////////////////////////////////////////////////////////////////////////////////
		// Steps to check BOOLEAN should be here because the field can be specified   //
		// as PROMPT_LIST1/2. This happens because BOOLEAN is placed recently         //
		////////////////////////////////////////////////////////////////////////////////
		if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN").equals("")) {
			componentType = "BOOLEAN";
			xFCheckBox = new XFCheckBox(dataTypeOptions);
			xFCheckBox.addKeyListener(new XF100_Component_keyAdapter(dialog));
			xFCheckBox.setLocation(0, 0);
			xFCheckBox.setEditable(true);
			component = xFCheckBox;
		} else {
			if (fieldOptionList.contains("PROMPT_LIST0")) {
				componentType = "ASSISTFIELD";
				xFInputAssistField = new XFInputAssistField(tableID, fieldID, dataSize, dataTypeOptions, dialog_.getSession());
				xFInputAssistField.addKeyListener(new XF100_Component_keyAdapter(dialog));
				xFInputAssistField.setLocation(0, 0);
				component = xFInputAssistField;
			} else {
				////////////////////////////////////////////////////////////////////////////////
				// PROMPT_LIST1 is the list with blank row, PROMPT_LIST2 is without blank row //
				////////////////////////////////////////////////////////////////////////////////
				if (fieldOptionList.contains("PROMPT_LIST1") || fieldOptionList.contains("PROMPT_LIST2")) {
					FontMetrics metrics2 = jLabelField.getFontMetrics(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
					String wrkText, wrkKey;
					wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
					if (!wrkStr.equals("")) {
						componentType = "KUBUN_LIST";
						jComboBox = new JComboBox();
						jComboBox.addKeyListener(new XF100_Component_keyAdapter(dialog));
						component = jComboBox;
						int fieldWidth = 20;
						if (fieldOptionList.contains("PROMPT_LIST1")) {
							keyValueList.add("");
							jComboBox.addItem("");
						}
						String userVariantsTableID = dialog_.getSession().getTableNameOfUserVariants();
						String sql = "select * from " + userVariantsTableID + " where IDUSERKUBUN = '" + wrkStr + "' order by SQLIST";
						XFTableOperator operator = dialog_.getReferOperator(sql);
						while (operator.next()) {
							wrkKey = operator.getValueOf("KBUSERKUBUN").toString().trim();
							keyValueList.add(wrkKey);
							wrkText = operator.getValueOf("TXUSERKUBUN").toString().trim();
							jComboBox.addItem(wrkText);
							if (metrics2.stringWidth(wrkText) > fieldWidth) {
								fieldWidth = metrics2.stringWidth(wrkText);
							}
						}
						jComboBox.setBounds(new Rectangle(0, 0, fieldWidth + 30, 24));
						jComboBox.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
						jComboBox.setSelectedIndex(0);
						jComboBox.setEditable(false);

					} else {
						wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "VALUES");
						if (!wrkStr.equals("")) {
							componentType = "VALUES_LIST";
							jComboBox = new JComboBox();
							jComboBox.addKeyListener(new XF100_Component_keyAdapter(dialog));
							component = jComboBox;
							int fieldWidth = 20;
							if (fieldOptionList.contains("PROMPT_LIST1")) {
								jComboBox.addItem("");
							}
							StringTokenizer workTokenizer = new StringTokenizer(wrkStr, ";" );
							while (workTokenizer.hasMoreTokens()) {
								wrkKey = workTokenizer.nextToken();
								jComboBox.addItem(wrkKey);
								if (metrics2.stringWidth(wrkKey) > fieldWidth) {
									fieldWidth = metrics2.stringWidth(wrkKey);
								}
							}
							jComboBox.setBounds(new Rectangle(0, 0, fieldWidth + 30, 24));
							jComboBox.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
							jComboBox.setSelectedIndex(0);
							jComboBox.setEditable(false);

						} else {
							componentType = "RECORDS_LIST";
							jComboBox = new JComboBox();
							jComboBox.addKeyListener(new XF100_Component_keyAdapter(dialog));
							component = jComboBox;
							int fieldWidth = 20;
							ArrayList<XF100_ReferTable> referTableList = dialog_.getReferTableList();
							for (int i = 0; i < referTableList.size(); i++) {
								if (referTableList.get(i).getTableID().equals(tableID)) {
									if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias)) {
										if (fieldOptionList.contains("PROMPT_LIST1")) {
											jComboBox.addItem("");
										}
										XFTableOperator operator = dialog_.createTableOperator("Select", tableID);
										operator.setSelectFields(fieldID);
										operator.setOrderBy(fieldID);
										while (operator.next()) {
											wrkKey = operator.getValueOf(fieldID).toString().trim();
											jComboBox.addItem(wrkKey);
											if (metrics2.stringWidth(wrkKey) > fieldWidth) {
												fieldWidth = metrics2.stringWidth(wrkKey);
											}
										}
										jComboBox.setBounds(new Rectangle(0, 0, fieldWidth + 30, 24));
										jComboBox.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
										jComboBox.setSelectedIndex(0);
										jComboBox.setEditable(false);
										break;
									}
								}
							}
						}
					}

				} else {
					wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL");
					if (!wrkStr.equals("")) {
						componentType = "PROMPT_CALL";
						xFPromptCall = new XF100_PromptCallField(fieldElement, wrkStr, dialog_);
						xFPromptCall.addKeyListener(new XF100_Component_keyAdapter(dialog));
						xFPromptCall.setLocation(0, 0);
						component = xFPromptCall;
						if (component.getBounds().width < 70) {
							component.setBounds(new Rectangle(component.getBounds().x, component.getBounds().y, 70, component.getBounds().height));
						}
					} else {
						if (dataType.equals("DATE")) {
							componentType = "DATE";
							xFDateField = new XFDateField(dialog_.getSession());
							xFDateField.addKeyListener(new XF100_Component_keyAdapter(dialog));
							xFDateField.setLocation(0, 0);
							xFDateField.setEditable(true);
							component = xFDateField;
						} else {
							if (dataTypeOptionList.contains("YMONTH")) {
								componentType = "YMONTH";
								xFYMonthBox = new XFYMonthBox(dialog_.getSession());
								xFYMonthBox.addKeyListener(new XF100_Component_keyAdapter(dialog));
								xFYMonthBox.setLocation(0, 0);
								xFYMonthBox.setEditable(true);
								component = xFYMonthBox;
							} else {
								if (dataTypeOptionList.contains("MSEQ")) {
									componentType = "MSEQ";
									xFMSeqBox = new XFMSeqBox(dialog_.getSession());
									xFMSeqBox.addKeyListener(new XF100_Component_keyAdapter(dialog));
									xFMSeqBox.setLocation(0, 0);
									xFMSeqBox.setEditable(true);
									component = xFMSeqBox;
								} else {
									if (dataTypeOptionList.contains("FYEAR")) {
										componentType = "FYEAR";
										xFFYearBox = new XFFYearBox(dialog_.getSession());
										xFFYearBox.addKeyListener(new XF100_Component_keyAdapter(dialog));
										xFFYearBox.setLocation(0, 0);
										xFFYearBox.setEditable(true);
										component = xFFYearBox;
									} else {
										componentType = "TEXTFIELD";
										xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions, dialog_.getSession().systemFont, true);
										xFTextField.addKeyListener(new XF100_Component_keyAdapter(dialog));
										xFTextField.setLocation(0, 0);
										component = xFTextField;
									}
								}
							}
						}
					}
				}
			}
		}

		if (fieldOptionList.contains("NON_EDITABLE")) {
			this.setEditable(false);
		} else {
			this.setEditable(true);
		}
		if (fieldOptionList.contains("HIDDEN")) {
			isHidden = true;
		}

		if (decimalSize > 0) {
			wrkStr = "<html>" + fieldName + " " + tableAlias + "." + fieldID + " (" + dataSize + "," + decimalSize + ")<br>" + fieldRemarks;
		} else {
			wrkStr = "<html>" + fieldName + " " + tableAlias + "." + fieldID + " (" + dataSize + ")<br>" + fieldRemarks;
		}
		this.setToolTipText(wrkStr);
		component.setToolTipText(wrkStr);
		jPanelField.add(component);

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "WIDTH");
		if (wrkStr.equals("")) {
			if (component.getBounds().width > 250) {
				component.setBounds(new Rectangle(component.getBounds().x, component.getBounds().y, 250, component.getBounds().height));
			}
		} else {
			component.setBounds(new Rectangle(component.getBounds().x, component.getBounds().y, Integer.parseInt(wrkStr), component.getBounds().height));
		}
		//this.setPreferredSize(new Dimension(jLabelField.getPreferredSize().width + component.getBounds().width + 5, component.getBounds().height));
		this.setPreferredSize(new Dimension(jPanelCaption.getPreferredSize().width + component.getBounds().width, component.getBounds().height));
	}

	public Object getDefaultValue() {
		Object value = "";
		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "VALUE");
		if (wrkStr.equals("")) {
			if (componentType.equals("BOOLEAN")) {
				value = xFCheckBox.getFalseValue();
			}
		} else {
			value = XFUtility.getDefaultValueOfFilterField(wrkStr, dialog_.getSession());
			if (value == null) {
				JOptionPane.showMessageDialog(this, tableID + "." + fieldID + XFUtility.RESOURCE.getString("FunctionError46") + "\n" + wrkStr);
			}
		}
		return value;
	}
	
	public boolean isHidden() {
		return isHidden;
	}

	public boolean isVerticalPosition(){
		return isVertical;
	}

	public int getVerticalMargin(){
		return verticalMargin;
	}

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}

	public String getCaption(){
		return fieldCaption;
	}

	public String getCaptionAndValue(){
		String value = fieldCaption + operand;
		String wrk = "";
		if (componentType.equals("TEXTFIELD")) {
			wrk = (String)xFTextField.getExternalValue();
		}
		if (componentType.equals("ASSISTFIELD")) {
			wrk = (String)xFInputAssistField.getExternalValue();
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
		if (wrk.equals("")) {
			value = value + "* ";
		} else {
			value = value + wrk + " ";
		}
		return value;
	}

	public String getDataType(){
		return dataType;
	}

	public String getComponentType(){
		return componentType;
	}

	public Object getComponent(){
		return component;
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
		if (parmMap != null && parmMap.containsKey(mapKey)) {
			Object mapValue = parmMap.get(mapKey);
			if (componentType.equals("TEXTFIELD")) {
				xFTextField.setText(mapValue.toString().trim());
			}
			if (componentType.equals("ASSISTFIELD")) {
				xFInputAssistField.setValue(mapValue.toString().trim());
			}
			if (componentType.equals("DATE")) {
				xFDateField.setValue(mapValue.toString());
			}
			if (componentType.equals("YMONTH")) {
				xFYMonthBox.setValue(mapValue.toString());
			}
			if (componentType.equals("MSEQ")) {
				xFMSeqBox.setValue(mapValue.toString());
			}
			if (componentType.equals("FYEAR")) {
				xFFYearBox.setValue(mapValue.toString());
			}
			if (componentType.equals("KUBUN_LIST")) {
				jComboBox.setSelectedIndex(keyValueList.indexOf(mapValue.toString()));
			}
			if (componentType.equals("VALUES_LIST") || componentType.equals("RECORDS_LIST")) {
				for (int i = 0; i < jComboBox.getItemCount(); i++) {
					if (jComboBox.getItemAt(i).equals(mapValue.toString())) {
							jComboBox.setSelectedIndex(i);
							break;
					}
				}
			}
			if (componentType.equals("BOOLEAN")) {
				xFCheckBox.setValue(keyValueList.indexOf(mapValue.toString()));
			}
			if (componentType.equals("PROMPT_CALL")) {
				xFPromptCall.setValue(mapValue.toString());
			}
			if (!this.isEditable_ && mapValue.toString().equals("")) {
				isValidated = false;
			}
		} else {
			if (!this.isEditable_ && !this.isValueSpecified()) {
				isValidated = false;
			}
		}
		return isValidated;
	}

	public void setValue(Object value){
		if (componentType.equals("TEXTFIELD")) {
			xFTextField.setText(value.toString().trim());
		}
		if (componentType.equals("ASSISTFIELD")) {
			xFInputAssistField.setValue(value.toString().trim());
		}
		if (componentType.equals("BOOLEAN")) {
			xFCheckBox.setValue(value.toString().trim());
		}
		if (componentType.equals("DATE")) {
			xFDateField.setValue(value.toString());
		}
		if (componentType.equals("YMONTH")) {
			xFYMonthBox.setValue(value.toString());
		}
		if (componentType.equals("MSEQ")) {
			xFMSeqBox.setValue(value.toString());
		}
		if (componentType.equals("FYEAR")) {
			xFFYearBox.setValue(value.toString());
		}
		if (componentType.equals("KUBUN_LIST")) {
			if (keyValueList.indexOf(value.toString()) != -1) {
				jComboBox.setSelectedIndex(keyValueList.indexOf(value.toString()));
			}
		}
		if (componentType.equals("VALUES_LIST") || componentType.equals("RECORDS_LIST")) {
			for (int i = 0; i < jComboBox.getItemCount(); i++) {
				if (jComboBox.getItemAt(i).equals(value.toString())) {
					jComboBox.setSelectedIndex(i);
					break;
				}
			}
		}
		if (componentType.equals("PROMPT_CALL")) {
			xFPromptCall.setValue(value.toString());
		}
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
		jButtonOperand.setEnabled(isEditable_);
		if (componentType.equals("TEXTFIELD")) {
			xFTextField.setEditable(isEditable_);
		}
		if (componentType.equals("ASSISTFIELD")) {
			xFInputAssistField.setEditable(isEditable_);
		}
		if (componentType.equals("BOOLEAN")) {
			xFCheckBox.setEditable(isEditable_);
		}
		if (componentType.equals("DATE")) {
			xFDateField.setEditable(isEditable_);
		}
		if (componentType.equals("YMONTH")) {
			xFYMonthBox.setEditable(isEditable_);
		}
		if (componentType.equals("MSEQ")) {
			xFMSeqBox.setEditable(isEditable_);
		}
		if (componentType.equals("FYEAR")) {
			xFFYearBox.setEditable(isEditable_);
		}
		if (componentType.equals("KUBUN_LIST") || componentType.equals("VALUES_LIST") || componentType.equals("RECORDS_LIST")) {
			jComboBox.setEnabled(isEditable_);
		}
		if (componentType.equals("PROMPT_CALL")) {
			xFPromptCall.setEditable(isEditable_);
		}
	}
	
	public boolean isEditable() {
		return isEditable_;
	}
	
	public String getFilterGroupID() {
		return filterGroupID;
	}
	
	public boolean isVirtualField() {
		return isVirtualField;
	}

	public boolean isReflect(){
		return isReflect;
	}
	
	public String getOperand() {
		return operand;
	}
	
	public boolean isPrimaryWhere() {
		return isPrimaryWhere;
	}
	
	public boolean isValidated(){
		boolean validated = false;
		if (isPrimaryWhere) {
			validated = true;
		} else {
			XF100_Column columnField = dialog_.getColumnObjectByID(this.getTableID(), this.getTableAlias(), this.getFieldID());
			if (this.isReflect) {
				if (componentType.equals("TEXTFIELD")) {
					columnField.setValue((String)xFTextField.getInternalValue());
				}
				if (componentType.equals("ASSISTFIELD")) {
					columnField.setValue((String)xFInputAssistField.getInternalValue());
				}
				if (componentType.equals("KUBUN_LIST")) {
					columnField.setValue((String)keyValueList.get(jComboBox.getSelectedIndex()));
				}
				if (componentType.equals("RECORDS_LIST")
						|| componentType.equals("VALUES_LIST")) {
					columnField.setValue((String)jComboBox.getSelectedItem());
				}
				if (componentType.equals("BOOLEAN")) {
					columnField.setValue((String)xFCheckBox.getInternalValue());
				}
				if (componentType.equals("DATE")) {
					columnField.setValue(xFDateField.getInternalValue());
				}
				if (componentType.equals("YMONTH")) {
					columnField.setValue(xFYMonthBox.getInternalValue());
				}
				if (componentType.equals("MSEQ")) {
					columnField.setValue(xFMSeqBox.getInternalValue());
				}
				if (componentType.equals("FYEAR")) {
					columnField.setValue(xFFYearBox.getInternalValue());
				}
				if (componentType.equals("PROMPT_CALL")) {
					columnField.setValue(xFPromptCall.getInternalValue());
				}
				validated = true;

			} else {
				String stringResultValue = "";
				String stringFilterValue = "";
				double doubleResultValue = 0;
				double doubleFilterValue = 0;

				if (columnField.isReadyToEvaluate()) {
					if (componentType.equals("TEXTFIELD") || componentType.equals("ASSISTFIELD")) {
						if (columnField.getInternalValue() != null) {
							stringResultValue = columnField.getInternalValue().toString().trim();
						} 
						if (componentType.equals("TEXTFIELD")) {
							stringFilterValue = (String)xFTextField.getInternalValue();
						}
						if (componentType.equals("ASSISTFIELD")) {
							stringFilterValue = (String)xFInputAssistField.getInternalValue();
						}
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
								if (operandType.equals("NE")) {
									if (doubleResultValue != doubleFilterValue) {
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
									if (operandType.equals("NE")) {
										if (doubleResultValue != doubleFilterValue) {
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
										if (stringFilterValue.equals("\'\'") || stringFilterValue.equals("\"\"") || stringFilterValue.equals("\'") || stringFilterValue.equals("\"")) {
											stringFilterValue = "";
										}
										if (stringResultValue.toUpperCase().equals(stringFilterValue.toUpperCase())) {
											validated = true;
										}
									}
									if (operandType.equals("NE")) {
										if (stringFilterValue.equals("\'\'") || stringFilterValue.equals("\"\"") || stringFilterValue.equals("\'") || stringFilterValue.equals("\"")) {
											stringFilterValue = "";
										}
										if (!stringResultValue.toUpperCase().equals(stringFilterValue.toUpperCase())) {
											validated = true;
										}
									}
									if (operandType.equals("SCAN")) {
										if (stringFilterValue.equals("\'\'") || stringFilterValue.equals("\"\"") || stringFilterValue.equals("\'") || stringFilterValue.equals("\"")) {
											stringFilterValue = "";
										}
										if (stringResultValue.toUpperCase().contains(stringFilterValue.toUpperCase())) {
											validated = true;
										}
									}
									if (operandType.equals("GENERIC")) {
										int lengthResultValue = stringResultValue.length();
										int lengthFieldValue = stringFilterValue.length();
										if (lengthResultValue >= lengthFieldValue) {
											String wrk = stringResultValue.substring(0, lengthFieldValue);
											if (wrk.toUpperCase().equals(stringFilterValue.toUpperCase())) {
												validated = true;
											}
										}
									}
								}
							}
						}
					}

					if (componentType.equals("KUBUN_LIST")) {
						String fieldValue = (String)keyValueList.get(jComboBox.getSelectedIndex());
						if (fieldValue.equals("")) {
							validated = true;
						} else {
							String resultValue = (String)columnField.getInternalValue();
							if (resultValue.equals(fieldValue)) {
								validated = true;
							}
						}
					}

					if (componentType.equals("RECORDS_LIST")) {
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

					if (componentType.equals("BOOLEAN")) {
						String fieldValue = (String)xFCheckBox.getInternalValue();
						String resultValue = (String)columnField.getInternalValue();
						if (resultValue.equals(fieldValue)) {
							validated = true;
						}
					}

					if (componentType.equals("DATE")) {
						stringFilterValue = (String)xFDateField.getInternalValue();
						if (stringFilterValue == null) {
							validated = true;
						} else {
							if (columnField.getInternalValue() != null) {
								stringResultValue = columnField.getInternalValue().toString().trim();
								stringResultValue = XFUtility.getStringNumber(stringResultValue).replace("-", "");
							} 
							if (!stringResultValue.equals("")) {
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
								if (operandType.equals("NE")) {
									if (doubleResultValue != doubleFilterValue) {
										validated = true;
									}
								}
							}
						}
					}

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
							if (operandType.equals("NE")) {
								if (doubleResultValue != doubleFilterValue) {
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
							if (operandType.equals("NE")) {
								if (doubleResultValue != doubleFilterValue) {
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

					if (componentType.equals("FYEAR")) {
						stringFilterValue = (String)xFFYearBox.getInternalValue();
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
							if (operandType.equals("NE")) {
								if (doubleResultValue != doubleFilterValue) {
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

					if (componentType.equals("PROMPT_CALL")) {
						if (columnField.getInternalValue() != null) {
							stringResultValue = columnField.getInternalValue().toString().trim();
						} 
						stringFilterValue = (String)xFPromptCall.getInternalValue();
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
								if (operandType.equals("NE")) {
									if (doubleResultValue != doubleFilterValue) {
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
									if (operandType.equals("NE")) {
										if (doubleResultValue != doubleFilterValue) {
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
									StringTokenizer workTokenizer = new StringTokenizer(stringFilterValue, ";" );
									while (workTokenizer.hasMoreTokens()) {
										if (operandType.equals("EQ")) {
											if (stringResultValue.equals(workTokenizer.nextToken())) {
												validated = true;
											}
										}
										if (operandType.equals("NE")) {
											if (!stringResultValue.equals(workTokenizer.nextToken())) {
												validated = true;
											}
										}
										if (operandType.equals("SCAN")) {
											if (stringResultValue.contains(workTokenizer.nextToken())) {
												validated = true;
											}
										}
										if (operandType.equals("GENERIC")) {
											String wrkStr = workTokenizer.nextToken();
											int lengthResultValue = stringResultValue.length();
											int lengthFieldValue = wrkStr.length();
											if (lengthResultValue >= lengthFieldValue) {
												String wrk = stringResultValue.substring(0, lengthFieldValue);
												if (wrk.equals(wrkStr)) {
													validated = true;
												}
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
		}
		return validated;
	}
	
	public String getSQLWhereValue(){
		String value = "";
		String wrkStr = "";
		if (!operand.equals("") && !isVirtualField) {

			if (componentType.equals("TEXTFIELD") || componentType.equals("ASSISTFIELD")) {
				if (componentType.equals("TEXTFIELD")) {
					wrkStr = (String)xFTextField.getInternalValue();
				}
				if (componentType.equals("ASSISTFIELD")) {
					wrkStr = (String)xFInputAssistField.getInternalValue();
				}
				if (!wrkStr.equals("")) {
					if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
						value = fieldID + operand + wrkStr;
					} else {
						if (this.getBasicType().equals("DATE") || this.getBasicType().equals("TIME")) {
							wrkStr = wrkStr.replace("-", "");
							wrkStr = wrkStr.replace("/", "");
							value = fieldID + operand + wrkStr;
						} else {
							if (this.getBasicType().equals("DATETIME")) {
								String whereValue = XFUtility.getWhereValueOfDateTimeSegment(operandType, wrkStr);
								if (whereValue.equals("")) {
									JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionMessage62") + wrkStr + XFUtility.RESOURCE.getString("FunctionMessage63"));
								} else {
									value = fieldID + whereValue;
								}
							} else {
								if (wrkStr.equals("\'\'") || wrkStr.equals("\"\"") || wrkStr.equals("\'") || wrkStr.equals("\"") ||
										wrkStr.equals("ÅfÅf") || wrkStr.equals("ÅhÅh") || wrkStr.equals("Åf") || wrkStr.equals("Åh")) {
									wrkStr = "";
								}
								if (operandType.equals("SCAN")) {
									value = "UPPER(" + fieldID + ") LIKE UPPER('%" + wrkStr + "%')";
								} else {
									if (operandType.equals("GENERIC")) {
										value = "UPPER(" + fieldID + ") LIKE UPPER('" + wrkStr + "%')";
									} else {
										value = fieldID + operand + "'" + wrkStr + "'";
										if (wrkStr.equals("") && operandType.equals("EQ")) {
											value = fieldID + " = '' or " + fieldID + " is null";
										}
									}
								}
							}
						}
					}
				}
			}
			if (componentType.equals("KUBUN_LIST")) {
				wrkStr = (String)keyValueList.get(jComboBox.getSelectedIndex());
				if (!wrkStr.equals("")) {
					value = fieldID + operand + "'" + wrkStr + "'";
				}
			}
			if (componentType.equals("VALUES_LIST")) {
				wrkStr = (String)jComboBox.getSelectedItem();
				if (!wrkStr.equals("")) {
					value = fieldID + operand + "'" + wrkStr + "'";
				}
			}
			if (componentType.equals("PROMPT_CALL")) {
				wrkStr = (String)xFPromptCall.getInternalValue();
				if (!wrkStr.equals("")) {
					if (wrkStr.contains(";")) {
						StringBuffer bf = new StringBuffer();
						StringTokenizer workTokenizer = new StringTokenizer(wrkStr, ";" );
						while (workTokenizer.hasMoreTokens()) {
							if (!bf.toString().equals("")) {
								bf.append(" or ");
							}
							bf.append(fieldID + operand + "'" + workTokenizer.nextToken() + "'");
						}
						value = bf.toString();
					} else {
						if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
							value = fieldID + operand + wrkStr;
						} else {
							if (operand.equals(" LIKE ")) {
								value = fieldID + operand + "'%" + wrkStr + "%'";
							} else {
								value = fieldID + operand + "'" + wrkStr + "'";
							}
						}
					}
				}
			}
			if (componentType.equals("BOOLEAN")) {
				wrkStr = (String)xFCheckBox.getInternalValue();
				if (!wrkStr.equals("")) {
					value = fieldID + operand + "'" + wrkStr + "'";
				}
			}
			if (componentType.equals("DATE")) {
				wrkStr = (String)xFDateField.getInternalValue();
				if (wrkStr != null && !wrkStr.equals("")) {
					value = fieldID + operand + "'" + wrkStr + "'";
				}
			}
			if (componentType.equals("YMONTH")) {
				wrkStr = (String)xFYMonthBox.getInternalValue();
				if (!wrkStr.equals("")) {
					value = fieldID + operand + "'" + wrkStr + "'";
				}
			}
			if (componentType.equals("MSEQ")) {
				wrkStr = (String)xFMSeqBox.getInternalValue();
				if (!wrkStr.equals("0")) {
					value = fieldID + operand + wrkStr;
				}
			}
			if (componentType.equals("FYEAR")) {
				wrkStr = (String)xFFYearBox.getInternalValue();
				if (!wrkStr.equals("")) {
					value = fieldID + operand + wrkStr;
				}
			}
		}
		return value;
	}
	
	public Object getValue(){
		Object value = "";
		String wrkStr = "";
		if (componentType.equals("TEXTFIELD") || componentType.equals("ASSISTFIELD")) {
			if (componentType.equals("TEXTFIELD")) {
				wrkStr = (String)xFTextField.getInternalValue();
			}
			if (componentType.equals("ASSISTFIELD")) {
				wrkStr = (String)xFInputAssistField.getInternalValue();
			}
			if (!wrkStr.equals("")) {
				if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
					//if (Double.parseDouble(wrkStr.trim()) != 0 || !fieldOptionList.contains("IGNORE_IF_ZERO")) {
						value = wrkStr;
					//}
				} else {
					if (this.getBasicType().equals("DATE") || this.getBasicType().equals("TIME") || this.getBasicType().equals("DATETIME")) {
						wrkStr = wrkStr.replace("-", "");
						wrkStr = wrkStr.replace("/", "");
						value = wrkStr;
					} else {
						value = wrkStr;
					}
				}
			}
		}
		if (componentType.equals("KUBUN_LIST")) {
			//if (jComboBox.getSelectedIndex() == -1) {
			//	jComboBox.setSelectedIndex(0);
			//	value = (String)keyValueList.get(0);
			//} else {
				value = (String)keyValueList.get(jComboBox.getSelectedIndex());
			//}
		}
		if (componentType.equals("VALUES_LIST")) {
			value = (String)jComboBox.getSelectedItem();
		}
		if (componentType.equals("PROMPT_CALL")) {
			wrkStr = (String)xFPromptCall.getInternalValue();
			if (!wrkStr.equals("")) {
				if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
					//if (Double.parseDouble(wrkStr.trim()) != 0 || !fieldOptionList.contains("IGNORE_IF_ZERO")) {
						value = wrkStr;
					//}
				} else {
					value = wrkStr;
				}
			}
		}
		if (componentType.equals("BOOLEAN")) {
			value = (String)xFCheckBox.getInternalValue();
		}
		if (componentType.equals("DATE")) {
			wrkStr = (String)xFDateField.getInternalValue();
			if (wrkStr != null && !wrkStr.equals("")) {
				value = wrkStr;
			}
		}
		if (componentType.equals("YMONTH")) {
			value = (String)xFYMonthBox.getInternalValue();
		}
		if (componentType.equals("MSEQ")) {
			wrkStr = (String)xFMSeqBox.getInternalValue();
			if (!wrkStr.equals("0")) {
				value = wrkStr;
			}
		}
		if (componentType.equals("FYEAR")) {
			value = (String)xFFYearBox.getInternalValue();
		}
		return value;
	}
	
	public boolean isValueSpecified() {
		boolean result = false;
		String wrkStr = "";
		if (componentType.equals("TEXTFIELD") || componentType.equals("ASSISTFIELD")) {
			if (componentType.equals("TEXTFIELD")) {
				wrkStr = (String)xFTextField.getInternalValue();
			}
			if (componentType.equals("ASSISTFIELD")) {
				wrkStr = (String)xFInputAssistField.getInternalValue();
			}
			if (!wrkStr.equals("")) {
				if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
					//if (Double.parseDouble(wrkStr.trim()) != 0 || !fieldOptionList.contains("IGNORE_IF_ZERO")) {
						result = true;
					//}
				} else {
					result = true;
				}
			}
		}
		if (componentType.equals("KUBUN_LIST")) {
			if (jComboBox.getSelectedIndex() >= 0) {
				wrkStr = (String)keyValueList.get(jComboBox.getSelectedIndex());
				if (!wrkStr.equals("")) {
					result = true;
				}
			}
		}
		if (componentType.equals("VALUES_LIST") || componentType.equals("RECORDS_LIST")) {
			if (jComboBox.getSelectedIndex() >= 0) {
				wrkStr = (String)jComboBox.getSelectedItem();
				if (!wrkStr.equals("")) {
					result = true;
				}
			}
		}
		if (componentType.equals("PROMPT_CALL")) {
			wrkStr = (String)xFPromptCall.getInternalValue();
			if (!wrkStr.equals("")) {
				if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
					//if (Double.parseDouble(wrkStr.trim()) != 0 || !fieldOptionList.contains("IGNORE_IF_ZERO")) {
						result = true;
					//}
				} else {
					result = true;
				}
			}
		}
		if (componentType.equals("BOOLEAN")) {
			wrkStr = (String)xFCheckBox.getInternalValue();
			if (!wrkStr.equals("")) {
				result = true;
			}
		}
		if (componentType.equals("DATE")) {
			wrkStr = (String)xFDateField.getInternalValue();
			if (wrkStr != null && !wrkStr.equals("")) {
				result = true;
			}
		}
		if (componentType.equals("YMONTH")) {
			wrkStr = (String)xFYMonthBox.getInternalValue();
			if (!wrkStr.equals("")) {
				result = true;
			}
		}
		if (componentType.equals("MSEQ")) {
			wrkStr = (String)xFMSeqBox.getInternalValue();
			if (!wrkStr.equals("0")) {
				result = true;
			}
		}
		if (componentType.equals("FYEAR")) {
			wrkStr = (String)xFFYearBox.getInternalValue();
			if (!wrkStr.equals("")) {
				result = true;
			}
		}
		return result;
	}
}

class XF100_PromptCallField extends JPanel implements XFEditableField {
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
    private XF100 dialog_;
    private org.w3c.dom.Element fieldElement_;
    private String functionID_ = "";
    private ArrayList<XF100_ReferTable> referTableList_;
    private String oldValue = "";
    private String listValue = "";
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();
	private ArrayList<String> kubunValueList = null;
	private ArrayList<String> kubunTextList = null;

	public XF100_PromptCallField(org.w3c.dom.Element fieldElement, String functionID, XF100 dialog){
		super();
		fieldElement_ = fieldElement;
		functionID_ = functionID;
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

		String wrkStr = XFUtility.getOptionValueWithKeyword(workElement.getAttribute("TypeOptions"), "KUBUN");
		if (!wrkStr.equals("")) {
			JLabel jLabel = new JLabel();
			FontMetrics metrics = jLabel.getFontMetrics(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
			String wrk = "";
			int fieldWidth = 50;
			try {

				kubunValueList = new ArrayList<String>();
				kubunTextList = new ArrayList<String>();
				String userVariantsTableID = dialog_.getSession().getTableNameOfUserVariants();
				String sql = "select * from " + userVariantsTableID + " where IDUSERKUBUN = '" + wrkStr + "' order by SQLIST";
				XFTableOperator operator = dialog_.getReferOperator(sql);
				while (operator.next()) {
					kubunValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
					wrk = operator.getValueOf("TXUSERKUBUN").toString().trim();
					if (metrics.stringWidth(wrk) + 10 > fieldWidth) {
						fieldWidth = metrics.stringWidth(wrk) + 10;
					}
					kubunTextList.add(wrk);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			xFTextField.setWidth(fieldWidth);
			xFTextField.setEditable(false);
		}

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "PROMPT_CALL_TO_PUT");
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

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XF100.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value;
				XF100_Filter filter;
				HashMap<String, Object> fieldValuesMap = new HashMap<String, Object>();
				for (int i = 0; i < fieldsToPutList_.size(); i++) {
					filter = dialog_.getFilterObjectByName(fieldsToPutList_.get(i));
					if (filter != null) {
						fieldValuesMap.put(fieldsToPutToList_.get(i), filter.getValue());
					}
				}
				fieldValuesMap.put("RETURN_TO", dialog_.getFunctionID());
				try {
					HashMap<String, Object> returnMap = dialog_.getSession().executeFunction(functionID_, fieldValuesMap);
					if (returnMap.get("RETURN_TO") != null) {
						dialog_.returnTo(returnMap.get("RETURN_TO").toString());
					}
					if (!returnMap.get("RETURN_CODE").equals("99")) {
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
						if (xFTextField.getText().equals("")) {
							listValue = "";
						} else {
							xFTextField.setFocusable(true);
							if (xFTextField.getText().contains(";")) {
								listValue = xFTextField.getText();
								xFTextField.setText("*LIST");
							} else {
								if (kubunValueList == null) {
									listValue = "";
								} else {
									listValue = xFTextField.getText();
									xFTextField.setText(kubunTextList.get(kubunValueList.indexOf(listValue)));
								}
							}
						}
					}
				} catch (Exception ex) {
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

	public void setEditable(boolean editable) {
		jButton.setEnabled(editable);
		xFTextField.setEditable(editable);
		isEditable = editable;
	}
	
	public void addKeyListener(KeyListener listener) {
		xFTextField.addKeyListener(listener);
		jButton.addKeyListener(listener);
	}

	public void requestFocus() {
		xFTextField.requestFocus();
	}

	public Object getInternalValue() {
		String text = "";
		if (xFTextField.getText().equals("*LIST") || kubunValueList != null) {
			text = listValue;
		} else {
			String basicType = XFUtility.getBasicTypeOf(dataType);
			if (basicType.equals("INTEGER")
					|| basicType.equals("FLOAT")
					|| dataTypeOptionList.contains("DIAL")
					|| dataTypeOptionList.contains("ZIPNO")) {
				text = XFUtility.getStringNumber(xFTextField.getText());
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
			listValue = "";
			xFTextField.setText("");
		} else {
			listValue = obj.toString();
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

class XF100_RowNumber extends Object {
	private int number_;
	private HashMap<String, Object> keyMap_;
	private HashMap<String, Object> columnMap_;
	private ArrayList<TableCellReadOnly> cellObjectList_;
	private boolean isSelected_ = false;
	public XF100_RowNumber(int num, HashMap<String, Object> keyMap, HashMap<String, Object> columnMap, ArrayList<TableCellReadOnly> cellObjectList) {
		number_ = num;
		keyMap_ = keyMap;
		columnMap_ = columnMap;
		cellObjectList_ = cellObjectList;
	}
	public HashMap<String, Object> getKeyMap() {
		return keyMap_;
	}
	public HashMap<String, Object> getColumnMap() {
		return columnMap_;
	}
	public ArrayList<TableCellReadOnly> getCellObjectList() {
		return cellObjectList_;
	}
	public String getRowNumberString() {
		return Integer.toString(number_);
	}
	public void setSelected(boolean isSelected) {
		isSelected_ = isSelected;
	}
	public boolean isSelected() {
		return isSelected_;
	}
}

class XF100_Column implements XFFieldScriptable {
	private org.w3c.dom.Element functionColumnElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF100 dialog_ = null;
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
	private String byteaTypeFieldID = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private int fieldWidth = 50;
	private int columnIndex = -1;
	private boolean isEnabled = true;
	private boolean isVisibleOnPanel = true;
	private boolean isVirtualField = false;
	private boolean isReadyToEvaluate = false;
	private String valueType = "STRING";
    private String linkedFunctionID_;
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
	private String flagTrue = "";
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private Object value_ = null;
	private Color foreground = Color.black;
	private int fieldRows = 1;
	private String fieldLayout = "HORIZONTAL";

	public XF100_Column(org.w3c.dom.Element functionColumnElement, XF100 dialog) throws Exception {
		super();
		String wrkStr;
		functionColumnElement_ = functionColumnElement;
		dialog_ = dialog;
		fieldOptions = functionColumnElement_.getAttribute("FieldOptions");

		StringTokenizer workTokenizer = new StringTokenizer(functionColumnElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		tableID = dialog_.getTableIDOfTableAlias(tableAlias);
		fieldID =workTokenizer.nextToken();

		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID + "." + fieldID + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		fieldName = workElement.getAttribute("Name");
		fieldRemarks = XFUtility.getLayoutedString(workElement.getAttribute("Remarks"), "<br>", dialog_.getSession().systemFont);;
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (dataSize > 50) {
			dataSize = 50;
		}
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();

		JLabel jLabel = new JLabel();
		FontMetrics metrics = jLabel.getFontMetrics(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
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
		int captionWidth = metrics.stringWidth(fieldCaption) + 18;

		ArrayList<String> fieldOptionList = XFUtility.getOptionList(fieldOptions);
		if (fieldOptionList.contains("VERTICAL")) {
			fieldLayout = "VERTICAL";
		} else {
			if (fieldOptionList.contains("HIDDEN")) {
				isVisibleOnPanel = false;
			}
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
				String userVariantsTableID = dialog_.getSession().getTableNameOfUserVariants();
				String sql = "select * from " + userVariantsTableID + " where IDUSERKUBUN = '" + wrkStr + "' order by SQLIST";
				XFTableOperator operator = dialog_.getReferOperator(sql);
				while (operator.next()) {
					kubunValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
					wrk = operator.getValueOf("TXUSERKUBUN").toString().trim();
					if (metrics.stringWidth(wrk) + 10 > fieldWidth) {
						fieldWidth = metrics.stringWidth(wrk) + 10;
					}
					kubunTextList.add(wrk);
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
								if (dataTypeOptionList.contains("URL")) {
									valueType = "URL";
									fieldWidth = dataSize * (XFUtility.FONT_SIZE/2 + 2) + 15;
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
		}

		linkedFunctionID_ = XFUtility.getOptionValueWithKeyword(fieldOptions, "LINKED_CALL");
		if (!linkedFunctionID_.equals("")) {
			valueType = "LINKED";
			wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "LINKED_CALL_TO_PUT");
			if (!wrkStr.equals("")) {
				workTokenizer = new StringTokenizer(wrkStr, ";" );
				while (workTokenizer.hasMoreTokens()) {
					fieldsToPutList_.add(workTokenizer.nextToken());
				}
			}
			wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "LINKED_CALL_TO_PUT_TO");
			if (!wrkStr.equals("")) {
				workTokenizer = new StringTokenizer(wrkStr, ";" );
				while (workTokenizer.hasMoreTokens()) {
					fieldsToPutToList_.add(workTokenizer.nextToken());
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

	public XF100_Column(String tableID, String tableAlias, String fieldID, XF100 dialog) throws Exception {
		super();
		String wrkStr;
		functionColumnElement_ = null;

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

		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID + "." + fieldID + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();

		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			String userVariantsTableID = dialog_.getSession().getTableNameOfUserVariants();
			String sql = "select * from " + userVariantsTableID + " where IDUSERKUBUN = '" + wrkStr + "' order by SQLIST";
			XFTableOperator operator = dialog_.getReferOperator(sql);
			while (operator.next()) {
				kubunValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
				kubunTextList.add(operator.getValueOf("TXUSERKUBUN").toString().trim());
			}
		}

		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}

		isVisibleOnPanel = false;
	}

	public boolean isReadyToEvaluate(){
		return isReadyToEvaluate;
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
		if (basicType.equals("INTEGER")
				&& !dataTypeOptionList.contains("MSEQ")
				&& !dataTypeOptionList.contains("FYEAR")) {
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
						wrkStr = value_.toString().substring(0, 10);
						value = XFUtility.getUserExpressionOfUtilDate(XFUtility.convertDateFromSqlToUtil(java.sql.Date.valueOf(wrkStr)), dialog_.getSession().getDateFormat(), false);
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
							if (dataTypeOptionList.contains("YMONTH") || dataTypeOptionList.contains("FYEAR")) {
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
									if (valueType.equals("STRING") || valueType.equals("URL") || valueType.equals("LINKED")) {
										if (value_ == null) {
											value = "";
										} else {
											value = value_.toString().trim();
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
			}
		}
		return value;
	}

	public String getValueType() {
		return valueType;
	}

	public String getLinkedFunctionID() {
		return linkedFunctionID_;
	}

	public ArrayList<String> getFieldsToPutList() {
		return fieldsToPutList_;
	}

	public ArrayList<String> getFieldsToPutToList() {
		return fieldsToPutToList_;
	}

	public TableCellReadOnly getCellObject() {
		return new TableCellReadOnly(this.getInternalValue(), this.getExternalValue(), this.getForeground(), valueType);
	}

	public String getCaption(){
		return fieldCaption;
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

	public void setReadyToEvaluate(boolean isReady) {
		isReadyToEvaluate = isReady;
	}

	public boolean setValueOfResultSet(XFTableOperator operator) {
		Object value = null;
		boolean isFoundInResultSet = false;
		String basicType = this.getBasicType();
		this.setColor("");

		try {
			if (!this.isVirtualField) {
				if (basicType.equals("BYTEA")) {
					isFoundInResultSet = true; //BYTEA field is not contained intentionally in result set //
					value_ = new XFByteArray(null);
				} else {
					value = operator.getValueOf(this.getFieldID());
					isFoundInResultSet = true;
					if (basicType.equals("INTEGER")) {
						if (value == null || value.equals("")) {
							value_ = "0";
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
								value_ = "0.0";
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
		} catch(Exception e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
		return isFoundInResultSet;
	}

	public void initialize() {
		value_ = XFUtility.getNullValueOfBasicType(this.getBasicType());
	}
	
	public void setByteaType(ArrayList<XF100_Column> columnList) {
		if (isVisibleOnPanel) {
			for (int i = 0; i < columnList.size(); i++) {
				if (columnList.get(i).getFieldID().equals(byteaTypeFieldID)) {
					((XFByteArray)value_).setType(columnList.get(i).getValue().toString());
					break;
				}
			}
		}
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

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
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

	public void setWarning(String message) {
	}

	public String getWarning() {
		return "";
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

class XF100_PrimaryTable extends Object {
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID = "";
	private String fixedWhere = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF100 dialog_;
	private StringTokenizer workTokenizer;
	private boolean hasOrderByAsItsOwnPhysicalFields = true;
	private String databaseName = "";

	public XF100_PrimaryTable(org.w3c.dom.Element functionElement, XF100 dialog){
		super();
		functionElement_ = functionElement;
		dialog_ = dialog;

		tableID = functionElement.getAttribute("PrimaryTable");
		tableElement = dialog_.getSession().getTableElement(tableID);

		int pos1, pos2;
		String wrkStr1, wrkStr2, wrkStr3;
		org.w3c.dom.Element workElement;

		NodeList nodeList = tableElement.getElementsByTagName("Field");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			fieldIDList.add(workElement.getAttribute("ID"));
		}

		if (tableElement.getAttribute("DB").equals("")) {
			databaseName = dialog_.getSession().getDatabaseName();
		} else {
			databaseName = dialog_.getSession().getSubDBName(tableElement.getAttribute("DB"));
		}

		if (functionElement_.getAttribute("KeyFields").equals("")) {
			nodeList = tableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer.hasMoreTokens()) {
						keyFieldIDList.add(workTokenizer.nextToken());
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

		workTokenizer = new StringTokenizer(functionElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			wrkStr1 = workTokenizer.nextToken();
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
			orderByFieldIDList.add(wrkStr1);
		}

		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID, element, dialog_.getSession().getTableNodeList()));
		}
	}

	public String getSelectSQL(int fromRow, int thruRow){
		StringBuffer buf = new StringBuffer();

		if (databaseName.contains("postgresql:") || databaseName.contains("mysql:")) {

			/////////////////////////////////////////////////////
			// SELECT fields FROM table WHERE ... ORDER BY ... //
			//   LIMIT (thruRow-fromRow+1) OFFSET fromRow      //
			/////////////////////////////////////////////////////
			buf.append(getSelectSQL());
			buf.append(" limit ");
			buf.append(thruRow - fromRow + 1);
			buf.append(" offset ");
			buf.append(fromRow);

		} else {

			if (databaseName.contains("derby:")) {

				////////////////////////////////////////////////////////////////////
				// SELECT fields FROM table WHERE ... ORDER BY ...                //
				//   OFFSET fromRow ROWS FETCH NEXT (thruRow-fromRow+1) ROWS ONLY //
				////////////////////////////////////////////////////////////////////
				buf.append(getSelectSQL());
				buf.append(" offset ");
				buf.append(fromRow);
				buf.append(" rows fetch next ");
				buf.append(thruRow - fromRow + 1);
				buf.append(" rows only");

			} else {

				////////////////////////////////////////////////////////////////////////////
				// SELECT * FROM (SELECT fields, row_number() OVER (ORDER BY ...) rNum    //
				//	 FROM table WHERE ... )t WHERE t.rNum >= fromRow AND t.rNum < thruRow //
				////////////////////////////////////////////////////////////////////////////

				///////////////////////////
				// Fields to be selected //
				///////////////////////////
				int count = 0;
				buf.append("select * from (select ");
				for (int i = 0; i < dialog_.getColumnList().size(); i++) {
					if (dialog_.getColumnList().get(i).getTableID().equals(tableID)
							&& !dialog_.getColumnList().get(i).isVirtualField()
							&& !dialog_.getColumnList().get(i).getBasicType().equals("BYTEA")) {
						if (count > 0) {
							buf.append(",");
						}
						count++;
						buf.append(dialog_.getColumnList().get(i).getFieldID());
					}
				}

				buf.append(", row_number() over ");

				/////////////////////
				// Order-by fields //
				/////////////////////
				if (this.hasOrderByAsItsOwnPhysicalFields) {
					ArrayList<String> orderByFieldList = getOrderByFieldIDList(dialog_.isListingInNormalOrder());
					if (orderByFieldList.size() > 0) {
						int pos0,pos1;
						buf.append("(order by ");
						for (int i = 0; i < orderByFieldList.size(); i++) {
							if (i > 0) {
								buf.append(",");
							}
							pos0 = orderByFieldList.get(i).indexOf(".");
							pos1 = orderByFieldList.get(i).indexOf("(A)");
							if (pos1 >= 0) {
								buf.append(orderByFieldList.get(i).substring(pos0+1, pos1));
							} else {
								pos1 = orderByFieldList.get(i).indexOf("(D)");
								if (pos1 >= 0) {
									buf.append(orderByFieldList.get(i).substring(pos0+1, pos1));
									buf.append(" DESC ");
								} else {
									buf.append(orderByFieldList.get(i).substring(pos0+1, orderByFieldList.get(i).length()));
								}
							}
						}
						buf.append(") rNum");
					} else {
						buf.append("(order by ");
						for (int i = 0; i < keyFieldIDList.size(); i++) {
							if (i > 0) {
								buf.append(",");
							}
							buf.append(keyFieldIDList.get(i));
							if (!dialog_.isListingInNormalOrder()) {
								buf.append(" DESC ");
							}
						}
						buf.append(") rNum");
					}
				}

				//////////////
				// Table ID //
				//////////////
				buf.append(" from ");
				buf.append(tableID);

				///////////////////
				// where section //
				///////////////////
				count = 0;
				String filterGroupID = "";
				fixedWhere = XFUtility.getFixedWhereValue(functionElement_.getAttribute("FixedWhere"), dialog_.getSession());
				if (!fixedWhere.equals("")) {
					buf.append(" where ((");
					buf.append(fixedWhere);
					//buf.append(")");
					count++;
				}
				for (int i = 0; i < dialog_.getFilterList().size(); i++) {
					if (dialog_.getFilterList().get(i).isPrimaryWhere() && !dialog_.getFilterList().get(i).getSQLWhereValue().equals("")) {
						if (filterGroupID.equals(dialog_.getFilterList().get(i).getFilterGroupID())
								&& !filterGroupID.equals("")) {
							buf.append(" or ");
						} else {
							if (count > 0) {
								buf.append(") and (");
							} else {
								buf.append(" where ((");
							}
							filterGroupID = dialog_.getFilterList().get(i).getFilterGroupID();
						}
						count++;
						buf.append(dialog_.getFilterList().get(i).getSQLWhereValue());
					}
				}
				if (count > 0) {
					buf.append("))");
				}

				buf.append(" ) t where t.rNum >= ");
				buf.append(fromRow);
				buf.append(" and t.rNum < ");
				buf.append(thruRow);

			}
		}
		return buf.toString();
	}

	public String getSelectSQL(){
		StringBuffer buf = new StringBuffer();

		///////////////////////////
		// Fields to be selected //
		///////////////////////////
		int count = 0;
		buf.append("select ");
		for (int i = 0; i < dialog_.getColumnList().size(); i++) {
			if (dialog_.getColumnList().get(i).getTableID().equals(tableID)
					&& !dialog_.getColumnList().get(i).isVirtualField()
					&& !dialog_.getColumnList().get(i).getBasicType().equals("BYTEA")) {
				if (count > 0) {
					buf.append(",");
				}
				count++;
				buf.append(dialog_.getColumnList().get(i).getFieldID());
			}
		}

		//////////////
		// Table ID //
		//////////////
		buf.append(" from ");
		buf.append(tableID);

		///////////////////
		// where section //
		///////////////////
		count = 0;
		String filterGroupID = "";
		fixedWhere = XFUtility.getFixedWhereValue(functionElement_.getAttribute("FixedWhere"), dialog_.getSession());
		if (!fixedWhere.equals("")) {
			buf.append(" where ((");
			buf.append(fixedWhere);
			count++;
		}
		for (int i = 0; i < dialog_.getFilterList().size(); i++) {
			if (dialog_.getFilterList().get(i).isPrimaryWhere() && !dialog_.getFilterList().get(i).getSQLWhereValue().equals("")) {
				if (filterGroupID.equals(dialog_.getFilterList().get(i).getFilterGroupID())
						&& !filterGroupID.equals("")) {
					buf.append(" or ");
				} else {
					if (count > 0) {
						buf.append(") and (");
					} else {
						buf.append(" where ((");
					}
					filterGroupID = dialog_.getFilterList().get(i).getFilterGroupID();
				}
				count++;
				buf.append(dialog_.getFilterList().get(i).getSQLWhereValue());
			}
		}
		if (count > 0) {
			buf.append("))");
		}

		//////////////////////
		// Order-by section //
		//////////////////////
		if (this.hasOrderByAsItsOwnPhysicalFields) {
			ArrayList<String> orderByFieldList = getOrderByFieldIDList(dialog_.isListingInNormalOrder());
			if (orderByFieldList.size() > 0) {
				int pos0,pos1;
				buf.append(" order by ");
				for (int i = 0; i < orderByFieldList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					pos0 = orderByFieldList.get(i).indexOf(".");
					pos1 = orderByFieldList.get(i).indexOf("(A)");
					if (pos1 >= 0) {
						buf.append(orderByFieldList.get(i).substring(pos0+1, pos1));
					} else {
						pos1 = orderByFieldList.get(i).indexOf("(D)");
						if (pos1 >= 0) {
							buf.append(orderByFieldList.get(i).substring(pos0+1, pos1));
							buf.append(" DESC ");
						} else {
							buf.append(orderByFieldList.get(i).substring(pos0+1, orderByFieldList.get(i).length()));
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
					if (!dialog_.isListingInNormalOrder()) {
						buf.append(" DESC ");
					}
				}
			}
		}

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
	
	public ArrayList<String> getOrderByFieldIDList(boolean isInNormalOrder){
		if (isInNormalOrder) {
			return orderByFieldIDList;
		} else {
			String workStr;
			ArrayList<String> listInReverseOrder = new ArrayList<String>();
			for (int i = 0; i < orderByFieldIDList.size(); i++) {
				workStr = orderByFieldIDList.get(i);
				if (workStr.contains("(D)")) {
					workStr = workStr.replace("(D)", "(A)");
				} else {
					if (workStr.contains("(A)")) {
						workStr = workStr.replace("(A)", "(D)");
					} else {
						workStr = workStr + "(D)";
					}
				}
				listInReverseOrder.add(workStr);
			}
			return listInReverseOrder;
		}
	}
	
	public String getOrderByDescription() {
		StringBuffer buf = new StringBuffer();
		int pos0,pos1;
		String workAlias, workTableID, workFieldID, workStr;
		org.w3c.dom.Element workElement;
		
		if (orderByFieldIDList.size() > 0) {
			for (int i = 0; i < orderByFieldIDList.size(); i++) {
				if (i > 0) {
					buf.append(">");
				}
				workStr = orderByFieldIDList.get(i).replace("(A)", "");
				pos0 = workStr.indexOf(".");
				pos1 = workStr.indexOf("(D)");
				if (pos1 >= 0) {
					workAlias = workStr.substring(0, pos0);
					workTableID = dialog_.getTableIDOfTableAlias(workAlias);
					workFieldID = workStr.substring(pos0+1, pos1);
					workElement = dialog_.getSession().getFieldElement(workTableID, workFieldID);
					buf.append(workElement.getAttribute("Name"));
					buf.append(XFUtility.RESOURCE.getString("Descend"));
				} else {
					workAlias = workStr.substring(0, pos0);
					workTableID = dialog_.getTableIDOfTableAlias(workAlias);
					workFieldID = workStr.substring(pos0+1, workStr.length());
					workElement = dialog_.getSession().getFieldElement(workTableID, workFieldID);
					buf.append(workElement.getAttribute("Name"));
				}
			}
		} else {
			for (int i = 0; i < keyFieldIDList.size(); i++) {
				if (i > 0) {
					buf.append(">");
				}
				workElement = dialog_.getSession().getFieldElement(tableID, keyFieldIDList.get(i));
				buf.append(workElement.getAttribute("Name"));
			}
		}

		return buf.toString();
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}
	
	public boolean hasOrderByAsItsOwnFields(){
		return hasOrderByAsItsOwnPhysicalFields;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF100_ReferTable referTable;
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

	public void runScript(String event1, String event2) throws ScriptException {
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		String tableAlias = "";
		int pos = event2.indexOf("(");
		if (pos > -1 && event2.length() > 1) {
			tableAlias = event2.substring(pos + 1, event2.length() - 1);
		}
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
			for (int i = 0; i < dialog_.getColumnList().size(); i++) {
				if (event1.contains("AR") && !tableAlias.equals(this.tableID) && dialog_.getColumnList().get(i).getTableAlias().equals(tableAlias)) {
					dialog_.getColumnList().get(i).setReadyToEvaluate(true);
				}
			}
		}
	}
}

class XF100_ReferTable extends Object {
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private String tableID = "";
	private String tableAlias = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private XF100 dialog_;
	private boolean isToBeExecuted = false;
	private String dbName = "";
	
	public XF100_ReferTable(org.w3c.dom.Element referElement, XF100 dialog){
		super();
		referElement_ = referElement;
		
		tableID = referElement_.getAttribute("ToTable");
		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}
		dialog_ = dialog;
		
		tableElement = dialog_.getSession().getTableElement(tableID);

		String dbID = tableElement.getAttribute("DB");
		if (dbID.equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(dbID);
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
	}

	public String getSelectSQL(){
		int count;
		StringBuffer buf = new StringBuffer();
		boolean validWhereKeys = false;

		///////////////////////////
		// Fields to be selected //
		///////////////////////////
		buf.append("select ");
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

		//////////////
		// Table ID //
		//////////////
		buf.append(" from ");
		buf.append(tableID);

		///////////////////
		// Where section //
		///////////////////
		buf.append(" where ");
		XF100_Column column;
		count = 0;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
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
				JOptionPane.showMessageDialog(null, withKeyFieldIDList.get(i) + XFUtility.RESOURCE.getString("FunctionError11"));
			}
			buf.append(XFUtility.getTableOperationValue(column.getBasicType(), column.getInternalValue(), dbName));
			if (!column.getInternalValue().equals("")) {
				validWhereKeys = true;
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
}

class XF100_Component_keyAdapter extends java.awt.event.KeyAdapter {
	XF100 adaptee;
	XF100_Component_keyAdapter(XF100 adaptee) {
		this.adaptee = adaptee;
	}
	public void keyPressed(KeyEvent e) {
		adaptee.component_keyPressed(e);
	}
}

class XF100_jTableMain_keyAdapter extends java.awt.event.KeyAdapter {
	XF100 adaptee;
	XF100_jTableMain_keyAdapter(XF100 adaptee) {
		this.adaptee = adaptee;
	}
	public void keyPressed(KeyEvent e) {
		adaptee.jTableMain_keyPressed(e);
	}
}

class XF100_jTableMain_mouseAdapter extends java.awt.event.MouseAdapter {
	XF100 adaptee;
	XF100_jTableMain_mouseAdapter(XF100 adaptee) {
		this.adaptee = adaptee;
	}
	public void mouseClicked(MouseEvent e) {
		adaptee.jTableMain_mouseClicked(e);
	}
	public void mouseExited(MouseEvent e) {
		adaptee.jTableMain_mouseExited(e);
	}
}

class XF100_jTableMain_mouseMotionAdapter extends java.awt.event.MouseMotionAdapter {
	XF100 adaptee;
	XF100_jTableMain_mouseMotionAdapter(XF100 adaptee) {
		this.adaptee = adaptee;
	}
	public void mouseMoved(MouseEvent e) {
		adaptee.jTableMain_mouseMoved(e);
	}
}

class XF100_jTableMain_focusAdapter extends java.awt.event.FocusAdapter {
	XF100 adaptee;
	XF100_jTableMain_focusAdapter(XF100 adaptee) {
		this.adaptee = adaptee;
	}
	public void focusGained(FocusEvent e) {
		adaptee.jTableMain_focusGained(e);
	}
	public void focusLost(FocusEvent e) {
		adaptee.jTableMain_focusLost(e);
	}
}

class XF100_jScrollPaneTable_mouseAdapter extends java.awt.event.MouseAdapter {
	XF100 adaptee;
	XF100_jScrollPaneTable_mouseAdapter(XF100 adaptee) {
		this.adaptee = adaptee;
	}
	public void mousePressed(MouseEvent e) {
		adaptee.jScrollPaneTable_mousePressed(e);
	}
	public void mouseClicked(MouseEvent e) {
		adaptee.jScrollPaneTable_mouseClicked(e);
	}
}

class XF100_jButtonList_actionAdapter implements java.awt.event.ActionListener {
	XF100 adaptee;
	XF100_jButtonList_actionAdapter(XF100 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
			adaptee.setListingInNormalOrder(false);
		} else {
			adaptee.setListingInNormalOrder(true);
		}
		adaptee.selectRowsAndList();
		adaptee.saveFilterValues();
	}
}

class XF100_FunctionButton_actionAdapter implements java.awt.event.ActionListener {
	XF100 adaptee;
	XF100_FunctionButton_actionAdapter(XF100 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jFunctionButton_actionPerformed(e);
	}
}

class XF100_jMenuItemToCall_actionAdapter implements java.awt.event.ActionListener {
	XF100 adaptee;
	XF100_jMenuItemToCall_actionAdapter(XF100 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jMenuItemToCall_actionPerformed(e);
	}
}

class XF100_jMenuItemToCallToAdd_actionAdapter implements java.awt.event.ActionListener {
	XF100 adaptee;
	XF100_jMenuItemToCallToAdd_actionAdapter(XF100 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jMenuItemToCallToAdd_actionPerformed(e);
	}
}

class XF100_jMenuItemToOutput_actionAdapter implements java.awt.event.ActionListener {
	XF100 adaptee;
	XF100_jMenuItemToOutput_actionAdapter(XF100 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jMenuItemToOutput_actionPerformed(e);
	}
}
