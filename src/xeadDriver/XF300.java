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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.table.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.text.DecimalFormat;
import org.w3c.dom.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class XF300 extends JDialog implements XFExecutable, XFScriptable {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element functionElement_ = null;
	private Session session_ = null;
	private boolean instanceIsAvailable = true;
	private boolean isToBeCanceled = false;
	private int instanceArrayIndex_ = -1;
	private int programSequence;
	private StringBuffer processLog = new StringBuffer();
	private XF300_KeyInputDialog keyInputDialog = null;
	private JPanel jPanelMain = new JPanel();
	private JTabbedPane jTabbedPane = new JTabbedPane();
	private JPanel jPanelHeaderFields = new JPanel();
	private JScrollPane jScrollPaneHeaderFields = new JScrollPane();
	private XF300_HeaderTable headerTable_;
	private HashMap<String, Object> parmMap_ = null;
	private HashMap<String, Object> returnMap_ = new HashMap<String, Object>();
	private ArrayList<XF300_HeaderField> headerFieldList = new ArrayList<XF300_HeaderField>();
	private ArrayList<XF300_HeaderReferTable> headerReferTableList = new ArrayList<XF300_HeaderReferTable>();
	private ArrayList<XFTableOperator> referOperatorList = new ArrayList<XFTableOperator>();
	private NodeList headerReferElementList;
	private XF300_StructureTable structureTable_ = null;
	private String headerFunctionID = "";
	private JPanel jPanelCenter = new JPanel();
	private JPanel jPanelTop = new JPanel();
	private JPanel jPanelTopEast = new JPanel();
	private JPanel jPanelTopCenter = new JPanel();
	private JPanel jPanelTopNorthMargin = new JPanel();
	private JPanel jPanelTopSouthMargin = new JPanel();
	private JPanel jPanelTopEastMargin = new JPanel();
	private JPanel jPanelTopWestMargin = new JPanel();
	private JButton jButtonList = new JButton();
	private JPanel jPanelFilter[] = new JPanel[10];
	private double filterWidth[] = new double[10];
	private boolean anyFilterIsEditable[] = new boolean[10];
	@SuppressWarnings("unchecked")
	private ArrayList<XF300_Filter> filterListArray[] = new ArrayList[10];
	@SuppressWarnings("unchecked")
	private ArrayList<XF300_DetailColumn>[] detailColumnListArray = new ArrayList[10];
	@SuppressWarnings("unchecked")
	private ArrayList<XF300_DetailReferTable>[] detailReferTableListArray = new ArrayList[10];
	@SuppressWarnings("unchecked")
	private ArrayList<WorkingRow>[] workingRowListArray = new ArrayList[10];
	private XF300_DetailTable[] detailTableArray = new XF300_DetailTable[10];
	private String[] detailFunctionIDArray = new String[10];
	private String[] detailParmTypeArray = new String[10];
	private TableModelReadOnly[] tableModelMainArray = new TableModelReadOnly[10];
	private JTable[] jTableMainArray = new JTable[10];
	private String[] initialMsgArray = new String[10];
	private NodeList[] detailReferElementList = new NodeList[10];
	private Bindings[] detailScriptBindingsArray = new Bindings[10];
	private DefaultTableCellRenderer rendererTableHeader = null;
	private DefaultTableCellRenderer rendererAlignmentCenter = new DefaultTableCellRenderer();
	private DefaultTableCellRenderer rendererAlignmentRight = new DefaultTableCellRenderer();
	private DefaultTableCellRenderer rendererAlignmentLeft = new DefaultTableCellRenderer();
	private boolean tablesReadyToUse;
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private JPanel[] jPanelButtonArray = new JPanel[7];
	private JPanel jPanelDummy = new JPanel();
	private JPanel jPanelInfo = new JPanel();
	private GridLayout gridLayoutButtons = new GridLayout();
	private GridLayout gridLayoutInfo = new GridLayout();
	private ArrayList<String> messageList = new ArrayList<String>();
	private JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JSplitPane jSplitPaneTreeView = new JSplitPane();
	private JScrollPane jScrollPaneTreeView = new JScrollPane();
	private XF300_TreeRenderer treeRenderer = new XF300_TreeRenderer();
	private JPanel jPanelTreeView = new JPanel();
	private JTree jTree = new JTree();
	private DefaultTreeModel treeModel;
	private XF300_TreeNode currentTreeNode = null;
	private JPopupMenu jPopupMenuTreeNode = new JPopupMenu();
	private JMenuItem jMenuItemTreeNodeExplosion = new JMenuItem();
	private JMenuItem jMenuItemTreeNodeImplosion = new JMenuItem();
	private JLabel jLabelTree = new JLabel();
	private boolean isForExplosion = true;
	private JSplitPane jSplitPaneMain = new JSplitPane();
	private JSplitPane jSplitPaneCenter = new JSplitPane();
	private JScrollPane jScrollPaneTable = new JScrollPane();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private JButton[] jButtonArray = new JButton[7];
	private Color selectionColorWithFocus = new Color(49,106,197);
	private Color selectionColorWithoutFocus = new Color(213,213,213);
	private SortableDomElementListModel detailTabSortingList;
	private SortableDomElementListModel workSortingList;
	private Action helpAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			session_.browseHelp();
		}
	};
	private Action focusTabAction = new AbstractAction(){
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e){
			if (jTabbedPane.getTabCount() > 0) {
				jTabbedPane.requestFocus();
			}
		}
	};
	private Action[] actionButtonArray = new Action[7];
	private String[] actionDefinitionArray = new String[7];
	private String actionF6 = "";
	private String actionF8 = "";
	private ScriptEngine scriptEngine;
	private Bindings headerScriptBindings;
	private String scriptNameRunning = "";
	private final int FIELD_HORIZONTAL_MARGIN = 1;
	private final int FIELD_VERTICAL_MARGIN = 5;
	private final int FONT_SIZE = 14;
	//private final int ROW_HEIGHT = 18;
	private ByteArrayOutputStream exceptionLog;
	private PrintStream exceptionStream;
	private String exceptionHeader = "";
	private String lastFunctionID = "";
	private HSSFPatriarch patriarch = null;
	
	public XF300(Session session, int instanceArrayIndex) {
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
		jSplitPaneMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPaneMain.add(jSplitPaneCenter, JSplitPane.TOP);
		jSplitPaneMain.add(jScrollPaneMessages, JSplitPane.BOTTOM);
		jSplitPaneMain.setFocusable(false);
		//
		jScrollPaneHeaderFields.getViewport().add(jPanelHeaderFields, null);
		jScrollPaneHeaderFields.setBorder(null);
		jPanelHeaderFields.setLayout(null);
		jPanelHeaderFields.setFocusable(false);
		jPanelCenter.setLayout(new BorderLayout());
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
		jButtonList.addActionListener(new XF300_jButtonList_actionAdapter(this));
		jButtonList.addKeyListener(new XF300_keyAdapter(this));
		jPanelTopCenter.setLayout(new BorderLayout());
		jPanelTop.setBorder(BorderFactory.createEtchedBorder());
		jPanelTop.add(jPanelTopCenter, BorderLayout.CENTER);
		jPanelTop.add(jPanelTopNorthMargin, BorderLayout.NORTH);
		jPanelTop.add(jPanelTopSouthMargin, BorderLayout.SOUTH);
		jPanelTop.add(jPanelTopEast, BorderLayout.EAST);
		jPanelCenter.add(jPanelTop, BorderLayout.NORTH);
		jPanelCenter.add(jScrollPaneTable, BorderLayout.CENTER);
		//
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jTextAreaMessages.setWrapStyleWord(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jSplitPaneCenter.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPaneCenter.add(jScrollPaneHeaderFields, JSplitPane.TOP);
		jSplitPaneCenter.add(jTabbedPane, JSplitPane.BOTTOM);
		jSplitPaneCenter.setFocusable(false);
		jTabbedPane.setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
		jTabbedPane.addKeyListener(new XF300_jTabbedPane_keyAdapter(this));
		jTabbedPane.addChangeListener(new XF300_jTabbedPane_changeAdapter(this));
		//
		rendererAlignmentCenter.setHorizontalAlignment(SwingConstants.CENTER);
		rendererAlignmentRight.setHorizontalAlignment(SwingConstants.RIGHT);
		rendererAlignmentLeft.setHorizontalAlignment(SwingConstants.LEFT);
		for (int i = 0; i < 10; i++) {
			detailTableArray[i] = null;
			detailReferElementList[i] = null;
			filterListArray[i] = new ArrayList<XF300_Filter>();
			workingRowListArray[i] = new ArrayList<WorkingRow>();
			detailColumnListArray[i] = new ArrayList<XF300_DetailColumn>();
			detailReferTableListArray[i] = new ArrayList<XF300_DetailReferTable>();
			jPanelFilter[i] = new JPanel();
			jTableMainArray[i] = new JTable();
			jTableMainArray[i].setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
			jTableMainArray[i].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			//jTableMainArray[i].setRowHeight(ROW_HEIGHT);
			jTableMainArray[i].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTableMainArray[i].setRowSelectionAllowed(true);
			jTableMainArray[i].setSelectionBackground(selectionColorWithoutFocus);
			jTableMainArray[i].setSelectionForeground(Color.black);
			jTableMainArray[i].addKeyListener(new XF300_jTableMain_keyAdapter(this));
			jTableMainArray[i].addMouseListener(new XF300_jTableMain_mouseAdapter(this));
			jTableMainArray[i].addFocusListener(new XF300_jTableMain_focusAdapter(this));
			WorkingTableHeader header = new WorkingTableHeader(jTableMainArray[i].getColumnModel());
			jTableMainArray[i].setTableHeader(header);
			jTableMainArray[i].getTableHeader().setFont(new java.awt.Font("SansSerif", 0, FONT_SIZE));
			rendererTableHeader = (DefaultTableCellRenderer)jTableMainArray[i].getTableHeader().getDefaultRenderer();
			rendererTableHeader.setHorizontalAlignment(SwingConstants.LEFT);
		}
		jScrollPaneTable.getViewport().add(jTableMainArray[0], null);
		jScrollPaneTable.addMouseListener(new XF300_jScrollPaneTable_mouseAdapter(this));
		//
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
		gridLayoutButtons.setColumns(7);
		gridLayoutButtons.setRows(1);
		gridLayoutButtons.setHgap(2);
		//
		for (int i = 0; i < 7; i++) {
			jButtonArray[i] = new JButton();
			jButtonArray[i].setBounds(new Rectangle(0, 0, 90, 30));
			jButtonArray[i].setFocusable(false);
			jButtonArray[i].addActionListener(new XF300_FunctionButton_actionAdapter(this));
			jPanelButtonArray[i] = new JPanel();
			jPanelButtonArray[i].setLayout(new BorderLayout());
			jPanelButtonArray[i].add(jButtonArray[i], BorderLayout.CENTER);
			actionButtonArray[i] = new ButtonAction(jButtonArray[i]);
			jPanelButtons.add(jPanelButtonArray[i]);
		}
		//
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		//jPanelMain.add(jSplitPaneMain, BorderLayout.CENTER);
		//jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		//
		jScrollPaneTreeView.getViewport().add(jTree);
		jTree.setFont(new java.awt.Font("Dialog", 0, 12));
		jTree.addKeyListener(new XF300_jTree_keyAdapter(this));
		jTree.addMouseListener(new XF300_jTree_mouseAdapter(this));
	    jTree.addTreeWillExpandListener(new XF300_jTree_treeWillExpandAdapter(this));
		jTree.setCellRenderer((TreeCellRenderer)treeRenderer);
	    jTree.setModel(treeModel);
		jSplitPaneTreeView.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		jSplitPaneTreeView.setFocusable(false);
		jLabelTree.setFont(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jLabelTree.setPreferredSize(new Dimension(100, 20));
		jPanelTreeView.setLayout(new BorderLayout());
		jPanelTreeView.add(jLabelTree, BorderLayout.NORTH);
		jPanelTreeView.add(jScrollPaneTreeView, BorderLayout.CENTER);
		jMenuItemTreeNodeExplosion.setText(res.getString("ExplosionAsRoot"));
		jMenuItemTreeNodeExplosion.addActionListener(new XF300_jMenuItemTreeNodeExplosion_actionAdapter(this));
		jMenuItemTreeNodeImplosion.setText(res.getString("ImplosionAsRoot"));
		jMenuItemTreeNodeImplosion.addActionListener(new XF300_jMenuItemTreeNodeImplosion_actionAdapter(this));
		jPopupMenuTreeNode.add(jMenuItemTreeNodeExplosion);
		jPopupMenuTreeNode.add(jMenuItemTreeNodeImplosion);
		//
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
		this.addWindowFocusListener(new XF300_WindowAdapter(this));
		this.addKeyListener(new XF300_keyAdapter(this));
	}

	public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parmMap) {
		org.w3c.dom.Element workElement;
		String workStr, workAlias, workTableID, workFieldID;
		StringTokenizer workTokenizer;
		org.w3c.dom.Element fieldElement;

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

			///////////////////////////
			// Initializing variants //
			///////////////////////////
			instanceIsAvailable = false;
			isToBeCanceled = false;
			exceptionLog = new ByteArrayOutputStream();
			exceptionStream = new PrintStream(exceptionLog);
			exceptionHeader = "";
			processLog.delete(0, processLog.length());
			messageList.clear();
			functionElement_ = functionElement;
			programSequence = session_.writeLogOfFunctionStarted(functionElement_.getAttribute("ID"), functionElement_.getAttribute("Name"));
			keyInputDialog = null;

			////////////////////
			// Set panel size //
			////////////////////
			Rectangle screenRect = session_.getMenuRectangle();
			if (functionElement_.getAttribute("Size").equals("")) {
				this.setPreferredSize(new Dimension(screenRect.width, screenRect.height));
				this.setLocation(screenRect.x, screenRect.y);
			} else {
				workTokenizer = new StringTokenizer(functionElement_.getAttribute("Size"), ";" );
				int width = Integer.parseInt(workTokenizer.nextToken());
				int height = Integer.parseInt(workTokenizer.nextToken());
				this.setPreferredSize(new Dimension(width, height));
				int posX = ((screenRect.width - width) / 2) + screenRect.x;
				int posY = ((screenRect.height - height) / 2) + screenRect.y;
				this.setLocation(posX, posY);
			}
			jPanelMain.updateUI();
			this.pack();
		
			if (!functionElement_.getAttribute("ID").equals(lastFunctionID)) {
				lastFunctionID = functionElement_.getAttribute("ID");

				//////////////////////////////////////
				// Setup Script Engine and Bindings //
				//////////////////////////////////////
				scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
				headerScriptBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
				headerScriptBindings.clear();
				headerScriptBindings.put("instance", (XFScriptable)this);

				//////////////////////////////
				// Set panel configurations //
				//////////////////////////////
				jLabelSessionID.setText(session_.getSessionID());
				jLabelFunctionID.setText("300" + "-" + instanceArrayIndex_ + "-" + functionElement_.getAttribute("ID"));
				FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font("Dialog", 0, FONT_SIZE));
				jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));
				this.setTitle(functionElement_.getAttribute("Name"));
				jPanelMain.removeAll();
				if (functionElement_.getAttribute("StructureTable").equals("")) {
					jPanelMain.add(jSplitPaneMain, BorderLayout.CENTER);
					jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
				} else {
					jSplitPaneTreeView.add(jPanelTreeView, JSplitPane.LEFT);
					jSplitPaneTreeView.add(jSplitPaneMain, JSplitPane.RIGHT);
					jSplitPaneTreeView.setDividerLocation(Integer.parseInt(functionElement_.getAttribute("StructureViewWidth")));
					jPanelMain.add(jSplitPaneTreeView, BorderLayout.CENTER);
					jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
				}
				jPanelMain.updateUI();
				this.pack();

				///////////////////////////////////////
				// Setup information of Header Table //
				///////////////////////////////////////
				headerTable_ = new XF300_HeaderTable(functionElement_, this);
				headerReferTableList.clear();
				headerReferElementList = headerTable_.getTableElement().getElementsByTagName("Refer");
				workSortingList = XFUtility.getSortedListModel(headerReferElementList, "Order");
				for (int i = 0; i < workSortingList.getSize(); i++) {
					headerReferTableList.add(new XF300_HeaderReferTable((org.w3c.dom.Element)workSortingList.getElementAt(i), this));
				}
				headerFunctionID = functionElement_.getAttribute("HeaderFunction");

				/////////////////////////////////////////////////
				// Setup Header Fields and Fetch Header Record //
				/////////////////////////////////////////////////
				jPanelHeaderFields.removeAll();
				headerFieldList.clear();
				Dimension dimOfPriviousField = new Dimension(0,0);
				Dimension dim = new Dimension(0,0);
				int posX = 0;
				int posY = 0;
				int biggestWidth = 300;
				int biggestHeight = 30;
				boolean firstVisibleField = true;
				//
				NodeList headerFieldElementList = functionElement_.getElementsByTagName("Field");
				workSortingList = XFUtility.getSortedListModel(headerFieldElementList, "Order");
				for (int i = 0; i < workSortingList.getSize(); i++) {
					//
					headerFieldList.add(new XF300_HeaderField((org.w3c.dom.Element)workSortingList.getElementAt(i), this));
					if (headerFieldList.get(i).isVisibleOnPanel()) {
						if (firstVisibleField) {
							posX = 0;
							posY = this.FIELD_VERTICAL_MARGIN + 3;
							firstVisibleField = false;
						} else {
							if (headerFieldList.get(i).isHorizontal()) {
								posX = posX + dimOfPriviousField.width + headerFieldList.get(i).getPositionMargin() + this.FIELD_HORIZONTAL_MARGIN;
							} else {
								posX = 0;
								posY = posY + dimOfPriviousField.height+ headerFieldList.get(i).getPositionMargin() + this.FIELD_VERTICAL_MARGIN;
							}
						}
						dim = headerFieldList.get(i).getPreferredSize();
						headerFieldList.get(i).setBounds(posX, posY, dim.width, dim.height);
						jPanelHeaderFields.add(headerFieldList.get(i));
						//
						if (posX + dim.width > biggestWidth) {
							biggestWidth = posX + dim.width;
						}
						//
						if (posY + dim.height > biggestHeight) {
							biggestHeight = posY + dim.height;
						}
						//
						if (headerFieldList.get(i).isHorizontal()) {
							dimOfPriviousField = new Dimension(dim.width, XFUtility.FIELD_UNIT_HEIGHT);
						} else {
							dimOfPriviousField = new Dimension(dim.width, dim.height);
						}
					}
				}
				//
				// Add primary table keys as HIDDEN fields if they are not on the header field list //
				for (int i = 0; i < headerTable_.getKeyFieldIDList().size(); i++) {
					if (!containsHeaderField(headerTable_.getTableID(), "", headerTable_.getKeyFieldIDList().get(i))) {
						headerFieldList.add(new XF300_HeaderField(headerTable_.getTableID(), "", headerTable_.getKeyFieldIDList().get(i), this));
					}
				}
				//
				// Analyze fields in script and add them as HIDDEN columns if necessary //
				for (int i = 0; i < headerTable_.getScriptList().size(); i++) {
					if	(headerTable_.getScriptList().get(i).isToBeRunAtEvent("BR", "")
							|| headerTable_.getScriptList().get(i).isToBeRunAtEvent("AR", "")) {
						for (int j = 0; j < headerTable_.getScriptList().get(i).getFieldList().size(); j++) {
							workTokenizer = new StringTokenizer(headerTable_.getScriptList().get(i).getFieldList().get(j), "." );
							workAlias = workTokenizer.nextToken();
							workTableID = getTableIDOfTableAlias(workAlias, -1);
							workFieldID = workTokenizer.nextToken();
							if (!containsHeaderField(workTableID, workAlias, workFieldID)) {
								fieldElement = session_.getFieldElement(workTableID, workFieldID);
								if (fieldElement == null) {
									String msg = res.getString("FunctionError1") + headerTable_.getTableID() + res.getString("FunctionError2") + headerTable_.getScriptList().get(i).getName() + res.getString("FunctionError3") + workAlias + "_" + workFieldID + res.getString("FunctionError4");
									JOptionPane.showMessageDialog(null, msg);
									throw new Exception(msg);
								} else {
									if (headerTable_.isValidDataSource(workTableID, workAlias, workFieldID)) {
										headerFieldList.add(new XF300_HeaderField(workTableID, workAlias, workFieldID, this));
									}
								}
							}
						}
					}
				}
				//
				// Analyze header refer tables and add their fields as HIDDEN header fields if necessary //
				for (int i = headerReferTableList.size()-1; i > -1; i--) {
					for (int j = 0; j < headerReferTableList.get(i).getFieldIDList().size(); j++) {
						if (containsHeaderField(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j))) {
							headerReferTableList.get(i).setToBeExecuted(true);
							break;
						}
					}
					if (headerReferTableList.get(i).isToBeExecuted()) {
						for (int j = 0; j < headerReferTableList.get(i).getFieldIDList().size(); j++) {
							if (!containsHeaderField(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j))) {
								headerFieldList.add(new XF300_HeaderField(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j), this));
							}
						}
						for (int j = 0; j < headerReferTableList.get(i).getWithKeyFieldIDList().size(); j++) {
							workTokenizer = new StringTokenizer(headerReferTableList.get(i).getWithKeyFieldIDList().get(j), "." );
							workAlias = workTokenizer.nextToken();
							workTableID = getTableIDOfTableAlias(workAlias, -1);
							workFieldID = workTokenizer.nextToken();
							if (!containsHeaderField(workTableID, workAlias, workFieldID)) {
								headerFieldList.add(new XF300_HeaderField(workTableID, workAlias, workFieldID, this));
							}
						}
					}
				}
				//
				// Add detail fields on header table as HIDDEN fields if they are not on the header fields list //
				NodeList columnFieldList = functionElement_.getElementsByTagName("Column");
				for (int i = 0; i < columnFieldList.getLength(); i++) {
					workElement = (org.w3c.dom.Element)columnFieldList.item(i);
					workStr = workElement.getAttribute("DataSource");
					if (workStr.substring(0, workStr.indexOf(".")).equals(headerTable_.getTableID())) {
						if (!containsHeaderField(headerTable_.getTableID(), "", workStr.substring(workStr.indexOf(".") + 1, workStr.length()))) {
							headerFieldList.add(new XF300_HeaderField(headerTable_.getTableID(), "", workStr.substring(workStr.indexOf(".") + 1, workStr.length()), this));
						}
					}
				}
				//
				jSplitPaneMain.setDividerLocation(this.getPreferredSize().height - 118);
				this.pack();
				jPanelHeaderFields.setPreferredSize(new Dimension(biggestWidth, biggestHeight));
				jSplitPaneCenter.setDividerLocation(biggestHeight + this.FIELD_VERTICAL_MARGIN + 13);

				///////////////////////////////////////
				// Setup information of detail table //
				///////////////////////////////////////
				tablesReadyToUse = false;
				jPanelBottom.remove(jPanelButtons);
				jPanelBottom.remove(jPanelDummy);
				int tabCount = jTabbedPane.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					jTabbedPane.removeTabAt(0);
				}
				boolean firstTab = true;
				NodeList detailElementList = functionElement_.getElementsByTagName("Detail");
				detailTabSortingList = XFUtility.getSortedListModel(detailElementList, "Order");
				for (int i = 0; i < detailTabSortingList.getSize(); i++) {
					workElement = (org.w3c.dom.Element)detailTabSortingList.getElementAt(i);
					//
					if (firstTab) {
						jTabbedPane.addTab(workElement.getAttribute("Caption"), null, jPanelCenter);
						firstTab = false;
					} else {
						jTabbedPane.addTab(workElement.getAttribute("Caption"), null, null);
					}
					//
					detailTableArray[i] = new XF300_DetailTable(workElement, i, this);
					//
					detailReferTableListArray[i].clear();
					detailReferElementList[i] = detailTableArray[i].getTableElement().getElementsByTagName("Refer");
					workSortingList = XFUtility.getSortedListModel(detailReferElementList[i], "Order");
					for (int j = 0; j < workSortingList.getSize(); j++) {
						detailReferTableListArray[i].add(new XF300_DetailReferTable((org.w3c.dom.Element)workSortingList.getElementAt(j), i, this));
					}
					//
					initialMsgArray[i] = workElement.getAttribute("InitialMsg");
					//
					// Add header table keys as HIDDEN fields if they are not on the header field list //
					for (int j = 0; j < detailTableArray[i].getHeaderKeyFieldIDList().size(); j++) {
						if (!containsHeaderField(headerTable_.getTableID(), "", detailTableArray[i].getHeaderKeyFieldIDList().get(j))) {
							headerFieldList.add(new XF300_HeaderField(headerTable_.getTableID(), "", detailTableArray[i].getHeaderKeyFieldIDList().get(j), this));
						}
					}
					//
					detailScriptBindingsArray[i] = scriptEngine.createBindings();
					detailScriptBindingsArray[i].putAll(headerScriptBindings);

					///////////////////////////
					// Setup Filter fields ////
					///////////////////////////
					filterListArray[i].clear();
					jPanelFilter[i].removeAll();
					anyFilterIsEditable[i] = false;
					GridLayout gridLayoutFilter = new GridLayout();
					NodeList FilterFieldList = workElement.getElementsByTagName("Filter");
					workSortingList = XFUtility.getSortedListModel(FilterFieldList, "Order");
					if (workSortingList.getSize() <= 2) {
						gridLayoutFilter.setColumns(2);
						gridLayoutFilter.setRows(1);
						filterWidth[i] = (this.getPreferredSize().getWidth() - 90) / 2;
					}
					if (workSortingList.getSize() == 3) {
						gridLayoutFilter.setColumns(3);
						gridLayoutFilter.setRows(1);
						filterWidth[i] = (this.getPreferredSize().getWidth() - 90) / 3;
					}
					if (workSortingList.getSize() == 4) {
						gridLayoutFilter.setColumns(4);
						gridLayoutFilter.setRows(1);
						filterWidth[i] = (this.getPreferredSize().getWidth() - 90) / 4;
					}
					jPanelFilter[i].setLayout(gridLayoutFilter);
					for (int j = 0; j < workSortingList.getSize() && j < 4; j++) {
						filterListArray[i].add(new XF300_Filter((org.w3c.dom.Element)workSortingList.getElementAt(j), this, i));
						if (!filterListArray[i].get(j).isValidatedWithParmMapValue(parmMap_)) {
							JOptionPane.showMessageDialog(this, res.getString("FunctionError47") + filterListArray[i].get(j).getCaption() + res.getString("FunctionError48"));
							isToBeCanceled = true;
							break;
						}
						if (filterListArray[i].get(j).isEditable()) {
							anyFilterIsEditable[i] = true;
						}
						jPanelFilter[i].add(filterListArray[i].get(j));
					}

					///////////////////////////////////////////
					// Setup components for JTable of Detail //
					///////////////////////////////////////////
					tableModelMainArray[i] = new TableModelReadOnly();
					jTableMainArray[i].setModel(tableModelMainArray[i]);
					jTableMainArray[i].setRowHeight(XFUtility.ROW_HEIGHT_WITHOUT_IMAGE);
					TableColumn column = null;
					detailColumnListArray[i].clear();
					//
					// Add Column on table model //
					tableModelMainArray[i].addColumn("NO."); //column index:0 //
					int columnIndex = 0;
					columnFieldList = workElement.getElementsByTagName("Column");
					workSortingList = XFUtility.getSortedListModel(columnFieldList, "Order");
					for (int j = 0; j < workSortingList.getSize(); j++) {
						detailColumnListArray[i].add(new XF300_DetailColumn(workElement.getAttribute("Table"), (org.w3c.dom.Element)workSortingList.getElementAt(j), this, i));
						if (detailColumnListArray[i].get(j).isVisibleOnPanel()) {
							columnIndex++;
							detailColumnListArray[i].get(j).setColumnIndex(columnIndex);
							tableModelMainArray[i].addColumn(detailColumnListArray[i].get(j).getCaption());
						}
						if (detailColumnListArray[i].get(j).isImage()) {
							jTableMainArray[i].setRowHeight(XFUtility.ROW_HEIGHT_WITH_IMAGE);
						}
					}
					//
					// Set attributes to each column //
					column = jTableMainArray[i].getColumnModel().getColumn(0);
					column.setPreferredWidth(38);
					column.setCellRenderer(new XF300_DetailRowNumberRenderer());
					for (int j = 0; j < workSortingList.getSize(); j++) {
						if (detailColumnListArray[i].get(j).isVisibleOnPanel()) {
							column = jTableMainArray[i].getColumnModel().getColumn(detailColumnListArray[i].get(j).getColumnIndex());
							column.setPreferredWidth(detailColumnListArray[i].get(j).getWidth());
							column.setCellRenderer(detailColumnListArray[i].get(j).getCellRenderer());
							column.setHeaderRenderer(detailColumnListArray[i].get(j).getHeaderRenderer());
						}
					}
					//
					// Add detail table key fields as HIDDEN column if they are not on detail field list //
					for (int j = 0; j < detailTableArray[i].getKeyFieldIDList().size(); j++) {
						if (!containsDetailField(i, detailTableArray[i].getTableID(), "", detailTableArray[i].getKeyFieldIDList().get(j))) {
							detailColumnListArray[i].add(new XF300_DetailColumn(detailTableArray[i].getTableID(), detailTableArray[i].getTableID(), "", detailTableArray[i].getKeyFieldIDList().get(j), this, i));
						}
					}
					//
					// Add OrderBy fields as HIDDEN column if they are not on detail field list //
					for (int j = 0; j < detailTableArray[i].getOrderByFieldIDList().size(); j++) {
						workStr = detailTableArray[i].getOrderByFieldIDList().get(j).replace("(D)", "");
						workStr = workStr.replace("(A)", "");
						workTokenizer = new StringTokenizer(workStr, "." );
						workAlias = workTokenizer.nextToken();
						workTableID = getTableIDOfTableAlias(workAlias, i);
						workFieldID = workTokenizer.nextToken();
						if (!containsDetailField(i, workTableID, workAlias, workFieldID)) {
							detailColumnListArray[i].add(new XF300_DetailColumn(detailTableArray[i].getTableID(), workTableID, workAlias, workFieldID, this, i));
						}
					}
					//
					// Add filter fields as HIDDEN columns if they are not on the column list //
					for (int j = 0; j < filterListArray[i].size(); j++) {
						if (!containsDetailField(i, filterListArray[i].get(j).getTableID(), filterListArray[i].get(j).getTableAlias(), filterListArray[i].get(j).getFieldID())) {
							detailColumnListArray[i].add(new XF300_DetailColumn(detailTableArray[i].getTableID(), filterListArray[i].get(j).getTableID(), filterListArray[i].get(j).getTableAlias(), filterListArray[i].get(j).getFieldID(), this, i));
						}
					}
					//
					// Analyze fields in script and add them as HIDDEN columns if necessary //
					for (int j = 0; j < detailTableArray[i].getScriptList().size(); j++) {
						if	(detailTableArray[i].getScriptList().get(j).isToBeRunAtEvent("BR", "")
								|| detailTableArray[i].getScriptList().get(j).isToBeRunAtEvent("AR", "")) {
							for (int k = 0; k < detailTableArray[i].getScriptList().get(j).getFieldList().size(); k++) {
								workTokenizer = new StringTokenizer(detailTableArray[i].getScriptList().get(j).getFieldList().get(k), "." );
								workAlias = workTokenizer.nextToken();
								workTableID = getTableIDOfTableAlias(workAlias, i);
								workFieldID = workTokenizer.nextToken();
								if (!containsDetailField(i, workTableID, workAlias, workFieldID)) {
									fieldElement = session_.getFieldElement(workTableID, workFieldID);
									if (fieldElement == null) {
										String msg = res.getString("FunctionError1") + detailTableArray[i].getTableID() + res.getString("FunctionError2") + detailTableArray[i].getScriptList().get(j).getName() + res.getString("FunctionError3") + workAlias + "_" + workFieldID + res.getString("FunctionError4");
										JOptionPane.showMessageDialog(null, msg);
										throw new Exception(msg);
									} else {
										if (detailTableArray[i].isValidDataSource(i, workTableID, workAlias, workFieldID)) {
											detailColumnListArray[i].add(new XF300_DetailColumn(detailTableArray[i].getTableID(), workTableID, workAlias, workFieldID, this, i));
										}
									}
								}
							}
						}
					}
					//
					// Analyze detail refer tables and add their fields as HIDDEN column fields if necessary //
					for (int j = detailReferTableListArray[i].size()-1; j > -1; j--) {
						for (int k = 0; k < detailReferTableListArray[i].get(j).getFieldIDList().size(); k++) {
							if (containsDetailField(i, detailReferTableListArray[i].get(j).getTableID(), detailReferTableListArray[i].get(j).getTableAlias(), detailReferTableListArray[i].get(j).getFieldIDList().get(k))) {
								detailReferTableListArray[i].get(j).setToBeExecuted(true);
								break;
							}
						}
						if (detailReferTableListArray[i].get(j).isToBeExecuted()) {
							for (int k = 0; k < detailReferTableListArray[i].get(j).getFieldIDList().size(); k++) {
								if (!containsDetailField(i, detailReferTableListArray[i].get(j).getTableID(), detailReferTableListArray[i].get(j).getTableAlias(), detailReferTableListArray[i].get(j).getFieldIDList().get(k))) {
									detailColumnListArray[i].add(new XF300_DetailColumn(detailTableArray[i].getTableID(), detailReferTableListArray[i].get(j).getTableID(), detailReferTableListArray[i].get(j).getTableAlias(), detailReferTableListArray[i].get(j).getFieldIDList().get(k), this, i));
								}
							}
							for (int k = 0; k < detailReferTableListArray[i].get(j).getWithKeyFieldIDList().size(); k++) {
								workTokenizer = new StringTokenizer(detailReferTableListArray[i].get(j).getWithKeyFieldIDList().get(k), "." );
								workAlias = workTokenizer.nextToken();
								workTableID = getTableIDOfTableAlias(workAlias, i);
								workFieldID = workTokenizer.nextToken();
								if (!containsDetailField(i, workTableID, workAlias, workFieldID)) {
									detailColumnListArray[i].add(new XF300_DetailColumn(detailTableArray[i].getTableID(), workTableID, workAlias, workFieldID, this, i));
								}
							}
						}
					}

					/////////////////////////////////////////////////
					// Setup information of detail function called //
					/////////////////////////////////////////////////
					detailParmTypeArray[i] = workElement.getAttribute("ParmType");
					detailFunctionIDArray[i] = workElement.getAttribute("DetailFunction");
				}
				tablesReadyToUse = true;
			}

			/////////////////////////
			// Fetch Header Record //
			/////////////////////////
			fetchHeaderRecord(false);
			if (!isToBeCanceled) {

				/////////////////////////////////////////////
				// Select Detail records and setup JTables //
				/////////////////////////////////////////////
				for (int i = 0; i < detailTabSortingList.getSize(); i++) {
					if (filterListArray[i].size() == 0 || !anyFilterIsEditable[i]) {
						selectDetailRecordsAndSetupTableRows(i, true);
					}
				}

				//////////////////////////////////////////////
				// Set first tab to be selected and focused //
				//////////////////////////////////////////////
				if (jTabbedPane.getTabCount() == 0) {
					jPanelBottom.add(jPanelDummy, BorderLayout.CENTER);
					jTextAreaMessages.setText(res.getString("FunctionMessage30"));
				} else {
					jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
					jTabbedPane.setSelectedIndex(0);
					jTabbedPane_stateChanged(null);
					jTabbedPane.requestFocus();
				}
				if (parmMap_.containsKey("INITIAL_MESSAGE")) {
					jTextAreaMessages.setText((String)parmMap_.get("INITIAL_MESSAGE"));
					parmMap_.remove("INITIAL_MESSAGE");
				}

				///////////////////////////////
				// Setup Structure Tree View //
				///////////////////////////////
				if (!functionElement_.getAttribute("StructureTable").equals("")) {
					isForExplosion = true;
					setupTreeNodes();
				}

				////////////////
				// Show Panel //
				////////////////
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				this.setVisible(true);
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError5"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
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

	void windowGainedFocus(WindowEvent e) {
		if (jTabbedPane.getTabCount() > 0) {
			if (jTableMainArray[jTabbedPane.getSelectedIndex()].getRowCount() >= 1) {
				jTableMainArray[jTabbedPane.getSelectedIndex()].requestFocus();
			} else {
				jTabbedPane.requestFocus();
			}
		}
	}

	void setErrorAndCloseFunction() {
		returnMap_.put("RETURN_CODE", "99");
		this.rollback();
		closeFunction();
	}

	void closeFunction() {
		if (!returnMap_.get("RETURN_CODE").equals("99")) {
			this.commit();
		}
		//
		instanceIsAvailable = true;
		//
//		String wrkStr;
//		if (exceptionLog.size() > 0 || !exceptionHeader.equals("")) {
//			wrkStr = processLog.toString() + "\nERROR LOG:\n" + exceptionHeader + exceptionLog.toString();
//		} else {
//			wrkStr = processLog.toString();
//		}
//		wrkStr = wrkStr.replace("'", "\"");
//		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), wrkStr);
		String errorLog = "";
		if (exceptionLog.size() > 0 || !exceptionHeader.equals("")) {
			errorLog = exceptionHeader + exceptionLog.toString();
		}
		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), processLog.toString(), errorLog);
		//
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		//
		this.setVisible(false);
	}
	
	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(null, message);
		}
		returnMap_.put("RETURN_CODE", "01");
		isToBeCanceled = true;
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
	
	public void startProgress(int maxValue) {
	}
	
	public void incrementProgress() {
	}

	public void commit() {
		session_.commit(true, processLog);
	}
	
	public void rollback() {
		session_.commit(false, processLog);
	}

	void fetchHeaderRecord(boolean cancelWithoutMessage) {
		XFTableOperator headerTableOp, referTableOp;
		boolean recordNotFound = true;
		String inputDialogMessage = "";
		boolean keyInputRequired = false;

		try {
			for (int i = 0; i < headerFieldList.size(); i++) {
				if (headerFieldList.get(i).isKey()) {
					if (parmMap_.containsKey(headerFieldList.get(i).getFieldID())) {
						if (parmMap_.get(headerFieldList.get(i).getFieldID()).equals(headerFieldList.get(i).getNullValue())) {
							keyInputRequired = true;
						}
					} else {
						if (parmMap_.containsKey(headerFieldList.get(i).getDataSourceName())) {
							if (parmMap_.get(headerFieldList.get(i).getDataSourceName()).equals(headerFieldList.get(i).getNullValue())) {
								keyInputRequired = true;
							}
						} else {
							keyInputRequired = true;
						}
					}
				} else {
					headerFieldList.get(i).setValue(headerFieldList.get(i).getNullValue());
					headerFieldList.get(i).setColor("black");
				}
			}

			while (recordNotFound) {
				if (keyInputRequired) {
					if (keyInputDialog == null) {
						keyInputDialog = new XF300_KeyInputDialog(this);
					}
					parmMap_ = keyInputDialog.requestKeyValues(inputDialogMessage);
					if (parmMap_.size() == 0) {
						isToBeCanceled = true;
						returnMap_.put("RETURN_CODE", "01");
						closeFunction();
						return;
					}
					returnMap_.putAll(parmMap_);
				}

				headerTable_.runScript("BR", ""); /* Script to be run BEFORE READ */

				headerTableOp = createTableOperator(headerTable_.getSelectSQL());
				if (headerTableOp.next()) {
					recordNotFound = false;
					for (int i = 0; i < headerFieldList.size(); i++) {
						if (headerFieldList.get(i).getTableID().equals(headerTable_.getTableID())) {
							headerFieldList.get(i).setValueOfResultSet(headerTableOp);
						}
					}
					headerTable_.runScript("AR", "BR()"); /* Script to be run AFETR READ primary table*/
				} else {
					if (keyInputRequired) {
						inputDialogMessage = res.getString("FunctionError37");
					} else {
						if (!cancelWithoutMessage) {
							JOptionPane.showMessageDialog(this, res.getString("FunctionError38"));
						}
						isToBeCanceled = true;
						returnMap_.put("RETURN_CODE", "01");
						closeFunction();
						return;
					}
				}
			}

			for (int i = 0; i < headerReferTableList.size(); i++) {
				if (headerReferTableList.get(i).isToBeExecuted()) {
					headerTable_.runScript("AR", "BR(" + headerReferTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE READ */
					referTableOp = createTableOperator(headerReferTableList.get(i).getSelectSQL());
					while (referTableOp.next()) {
						if (headerReferTableList.get(i).isRecordToBeSelected(referTableOp)) {
							for (int j = 0; j < headerFieldList.size(); j++) {
								if (headerFieldList.get(j).getTableAlias().equals(headerReferTableList.get(i).getTableAlias())) {
									headerFieldList.get(j).setValueOfResultSet(referTableOp);
								}
							}
							headerTable_.runScript("AR", "AR(" + headerReferTableList.get(i).getTableAlias() + ")"); /* Script to be run AFTER READ */
						}
					}
				}
			}

			headerTable_.runScript("AR", "AR()"); /* Script to be run AFTER READ */

		} catch(ScriptException e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError5") + "\n" + e.getMessage());
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
	}
	
	void jTree_keyReleased(KeyEvent e) {
		setupHeaderDetailsForTreeNodeSelected();
	}

	void jTree_mousePressed(MouseEvent e) {
		int row = jTree.getRowForLocation(e.getX(),e.getY());
		if (row > -1) {
			TreePath tp = jTree.getPathForRow(row);
			jTree.setSelectionPath(tp);
		}
	}

	void jTree_mouseReleased(MouseEvent e) {
		setupHeaderDetailsForTreeNodeSelected();
		//
		//Right Button Pressed//
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK) {
			int row = jTree.getRowForLocation(e.getX(),e.getY());
			if (row > -1) {
				Component com = (Component)e.getSource();
				jPopupMenuTreeNode.show(com, e.getX() , e.getY());
			}
		}
	}
	
	void setupHeaderDetailsForTreeNodeSelected() {
		int rowCount;
		XF300_TreeNode node;
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			TreePath tp = jTree.getSelectionPath();
			node = (XF300_TreeNode)tp.getLastPathComponent();
			if (node != currentTreeNode) {
				currentTreeNode = node;
				parmMap_ = currentTreeNode.getKeyMap();
				fetchHeaderRecord(true);
				for (int i = 0; i < jTabbedPane.getTabCount(); i++) {
					if (filterListArray[i].size() == 0 || !anyFilterIsEditable[i]) {
						selectDetailRecordsAndSetupTableRows(i, false);
					} else {
						rowCount = tableModelMainArray[i].getRowCount();
						for (int j = 0; j < rowCount; j++) {
							tableModelMainArray[i].removeRow(0);
						}
					}
				}
			}
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	void jTree_treeWillExpand(TreeExpansionEvent e) {
		XF300_TreeNode node, firstChildNode;
		TreePath tp = e.getPath();
		node = (XF300_TreeNode)tp.getLastPathComponent();
		firstChildNode = (XF300_TreeNode)node.getChildAt(0);
		if (firstChildNode != null) {
			if (firstChildNode.toString().equals("Dummy")) {
				node.removeAllChildren();
				node.setupChildNode();
			}
		}
	}

	void setupFunctionKeysAndButtonsForTabIndex(int tabIndex) {
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
		actionF6 = "";
		actionF8 = "";
		//
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMap.put("HELP", helpAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T,ActionEvent.CTRL_MASK), "FOCUS_TAB");
		actionMap.put("FOCUS_TAB", focusTabAction);
		//
		int workIndex;
		org.w3c.dom.Element element;
		NodeList buttonList = detailTableArray[tabIndex].getDetailTableElement().getElementsByTagName("Button");
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
			if (element.getAttribute("Number").equals("6")) {
				actionF6 = element.getAttribute("Position");
			}
			if (element.getAttribute("Number").equals("8")) {
				actionF8 = element.getAttribute("Position");
			}
		}
	}

	void selectDetailRecordsAndSetupTableRows(int index, boolean isToOverrideMessage) {
		HashMap<String, Object> keyMap;
		HashMap<String, Object> columnMap;
		ArrayList<Object> columnValueList;
		ArrayList<Object> orderByValueList;
		String workStr, sql;
		boolean readyToValidate;
		boolean toBeSelected;
		XFTableOperator detailTableOp, referTableOp;
		// 
		try {
			//
			int rowCount = tableModelMainArray[index].getRowCount();
			for (int i = 0; i < rowCount; i++) {
				tableModelMainArray[index].removeRow(0);
			}
			workingRowListArray[index].clear();
			referOperatorList.clear();
			//
			int countOfRows = 0;
			detailTableOp = createTableOperator(detailTableArray[index].getSelectSQL());
			while (detailTableOp.next()) {
				//
				for (int i = 0; i < detailColumnListArray[index].size(); i++) {
					detailColumnListArray[index].get(i).setReadyToValidate(false);
					detailColumnListArray[index].get(i).initialize();
				}
				//
				detailTableArray[index].runScript(index, "BR", ""); /* Script to be run BEFORE READ */
				//
				for (int i = 0; i < detailColumnListArray[index].size(); i++) {
					if (detailColumnListArray[index].get(i).getTableID().equals(detailTableArray[index].getTableID())) {
						readyToValidate = detailColumnListArray[index].get(i).setValueOfResultSet(detailTableOp);
						detailColumnListArray[index].get(i).setReadyToValidate(readyToValidate);
					}
				}
				//
				detailTableArray[index].runScript(index, "AR", "BR()"); /* Script to be run AFTER READ primary table */
				//
				if (isTheRowToBeSelected(index)) {
					//
					toBeSelected = true;
					//
					for (int i = 0; i < detailReferTableListArray[index].size(); i++) {
						//
						if (detailReferTableListArray[index].get(i).isToBeExecuted()) {
							//
							detailTableArray[index].runScript(index, "AR", "BR(" + detailReferTableListArray[index].get(i).getTableAlias() + ")"); /* Script to be run BEFORE READ */
							//
							sql = detailReferTableListArray[index].get(i).getSelectSQL();
							if (!sql.equals("")) {
								//referTableOp = createTableOperator(sql);
								referTableOp = null;
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
								while (referTableOp.next()) {
									if (detailReferTableListArray[index].get(i).isRecordToBeSelected(index, referTableOp)) {
										for (int j = 0; j < detailColumnListArray[index].size(); j++) {
											if (detailColumnListArray[index].get(j).getTableAlias().equals(detailReferTableListArray[index].get(i).getTableAlias())) {
												detailColumnListArray[index].get(j).setValueOfResultSet(referTableOp);
											}
										}
										detailTableArray[index].runScript(index, "AR", "AR(" + detailReferTableListArray[index].get(i).getTableAlias() + ")"); /* Script to be run AFTER READ */
										if (!isTheRowToBeSelected(index)) {
											toBeSelected = false;
											break;
										}
									}
								}
							}
							if (!toBeSelected) {
								break;
							}
						}
					}
					//
					detailTableArray[index].runScript(index, "AR", "AR()"); /* Script to be run AFTER READ */
					//
					if (toBeSelected) {
						//
						for (int i = 0; i < detailColumnListArray[index].size(); i++) {
							detailColumnListArray[index].get(i).setReadyToValidate(true);
						}
						//
						if (isTheRowToBeSelected(index)) {
							//
							keyMap = new HashMap<String, Object>();
							for (int i = 0; i < detailTableArray[index].getKeyFieldIDList().size(); i++) {
								keyMap.put(detailTableArray[index].getKeyFieldIDList().get(i), detailTableOp.getValueOf(detailTableArray[index].getKeyFieldIDList().get(i)));
							}
							//
							columnMap = new HashMap<String, Object>();
							for (int i = 0; i < detailColumnListArray[index].size(); i++) {
								columnMap.put(detailColumnListArray[index].get(i).getDataSourceName(), detailColumnListArray[index].get(i).getInternalValue());
							}
							//
							if (detailTableArray[index].hasOrderByAsItsOwnFields()) {
								Object[] Cell = new Object[detailColumnListArray[index].size() + 1];
								Cell[0] = new XF300_DetailRowNumber(countOfRows + 1, keyMap, columnMap);
								for (int i = 0; i < detailColumnListArray[index].size(); i++) {
									if (detailColumnListArray[index].get(i).isVisibleOnPanel()) {
										Cell[detailColumnListArray[index].get(i).getColumnIndex()] = detailColumnListArray[index].get(i).getCellObject();
									}
								}
								tableModelMainArray[index].addRow(Cell);
							} else {
								columnValueList = new ArrayList<Object>();
								for (int i = 0; i < detailColumnListArray[index].size(); i++) {
									if (detailColumnListArray[index].get(i).isVisibleOnPanel()) {
										columnValueList.add(detailColumnListArray[index].get(i).getCellObject());
									}
								}
								orderByValueList = new ArrayList<Object>();
								for (int i = 0; i < detailTableArray[index].getOrderByFieldIDList().size(); i++) {
									workStr = detailTableArray[index].getOrderByFieldIDList().get(i).replace("(D)", "");
									workStr = workStr.replace("(A)", "");
									for (int j = 0; j < detailColumnListArray[index].size(); j++) {
										if (detailColumnListArray[index].get(j).getDataSourceName().equals(workStr)) {
											orderByValueList.add(detailColumnListArray[index].get(j).getExternalValue());
											break;
										}
									}
								}
								workingRowListArray[index].add(new WorkingRow(index, columnValueList, keyMap, columnMap, orderByValueList));
							}
							//
							countOfRows++;
						}
					}
				}
			}
			//
			headerTable_.runScript("AR", "AS()"); /* Script to be run AFTER READ and AFTER SUMMARY */
			//
			if (!detailTableArray[index].hasOrderByAsItsOwnFields()) {
				WorkingRow[] workingRowArray = workingRowListArray[index].toArray(new WorkingRow[0]);
				Arrays.sort(workingRowArray, new WorkingRowComparator());
				for (int i = 0; i < workingRowArray.length; i++) {
					Object[] Cell = new Object[workingRowArray[i].getColumnValueList().size() + 1];
					Cell[0] = new XF300_DetailRowNumber(i + 1, workingRowArray[i].getKeyMap(), workingRowArray[i].getColumnMap());
					for (int j = 0; j < workingRowArray[i].getColumnValueList().size(); j++) {
						Cell[j+1] = workingRowArray[i].getColumnValueList().get(j);
					}
					tableModelMainArray[index].addRow(Cell);
				}
			}
			//
			if (isToOverrideMessage) {
				//
				messageList.clear();
				//
				if (countOfRows > 0) {
					jTableMainArray[index].setRowSelectionInterval(0, 0);
					jTableMainArray[index].requestFocus();
					//
					if (detailFunctionIDArray[index].equals("")) {
						messageList.add(res.getString("FunctionMessage2"));
					} else {
						messageList.add(res.getString("FunctionMessage3"));
					}
				} else {
					messageList.add(res.getString("FunctionMessage4"));
				}
				//
				setMessagesOnPanel();
			}
			//
		} catch(ScriptException e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, res.getString("FunctionError5") + "\n" + e.getMessage());
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
	}

	void setupTreeNodes() {
		XFTableOperator headerTableOp, structureTableOp;
		XF300_TreeNode rootNode, childNode;
		String sql;
		currentTreeNode = null;
		try {
			//
			// Set Root Node to TreeModel //
			structureTable_ = new XF300_StructureTable(functionElement_, this);
			sql = structureTable_.getSQLForNodeInfo(parmMap_);
			headerTableOp = createTableOperator(sql);
			if (headerTableOp.next()) {
				rootNode = structureTable_.createTreeNode(headerTableOp);
				treeModel = new DefaultTreeModel(rootNode);
				currentTreeNode = rootNode;
				//
				// Add Child Nodes //
				sql = structureTable_.getSelectSQL(parmMap_);
				structureTableOp = createTableOperator(sql);
				while (structureTableOp.next()) {
					sql = structureTable_.getSQLForNodeInfo(structureTableOp);
					headerTableOp = createTableOperator(sql);
					if (headerTableOp.next()) {
						childNode = structureTable_.createTreeNode(headerTableOp, structureTableOp);
						childNode.add(new XF300_TreeNode(null, "Dummy", null, this));
						rootNode.add(childNode);
					}
				}
			}
			//
		    jTree.setModel(treeModel);
		    jTree.expandRow(0);
		    jTree.setSelectionRow(0);
			//
			if (isForExplosion) {
				jLabelTree.setText(" " + structureTable_.getName());
			} else {
				jLabelTree.setText(" " + structureTable_.getName() + " - " + res.getString("Implosion"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void jMenuItemTreeNodeExplosion_actionPerformed(ActionEvent e) {
		isForExplosion = true;
		setupTreeNodes();
	}

	void jMenuItemTreeNodeImplosion_actionPerformed(ActionEvent e) {
		isForExplosion = false;
		setupTreeNodes();
	}
	
	boolean isForExplosion() {
		return isForExplosion;
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
		int pos1, pos2;
		HashMap<String, Object> returnMap;
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			//
			messageList.clear();
			//
			if (action.equals("EXIT")) {
				closeFunction();
			}
			//
			if (action.equals("HEADER")) {
				try {
					returnMap = session_.executeFunction(headerFunctionID, parmMap_);
					if (returnMap.get("RETURN_CODE").equals("30")) {
						returnMap_.put("RETURN_CODE", "30");
						closeFunction();
					} else {
						if (returnMap.get("RETURN_MESSAGE") == null) {
							messageList.add(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
						} else {
							messageList.add(returnMap.get("RETURN_MESSAGE").toString());
						}
						if (returnMap.get("RETURN_CODE").equals("20")) {
							fetchHeaderRecord(true);
							for (int i = 0; i < jTabbedPane.getTabCount(); i++) {
								if (filterListArray[i].size() == 0 || !anyFilterIsEditable[i]) {
									selectDetailRecordsAndSetupTableRows(i, false);
								}
							}
							returnMap_.put("RETURN_CODE", returnMap.get("RETURN_CODE"));
						}
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
					messageList.add(res.getString("FunctionError15"));
				}
			}
			//
			if (action.equals("ADD")) {
				try {
					returnMap = session_.executeFunction(detailFunctionIDArray[jTabbedPane.getSelectedIndex()], parmMap_);
					if (returnMap.get("RETURN_MESSAGE") == null) {
						messageList.add(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
					} else {
						messageList.add(returnMap.get("RETURN_MESSAGE").toString());
					}
					if (returnMap.get("RETURN_CODE").equals("10")) {
						if (filterListArray[jTabbedPane.getSelectedIndex()].size() == 0 || !anyFilterIsEditable[jTabbedPane.getSelectedIndex()]) {
							selectDetailRecordsAndSetupTableRows(jTabbedPane.getSelectedIndex(), false);
						}
						returnMap_.put("RETURN_CODE", returnMap.get("RETURN_CODE"));
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
					messageList.add(res.getString("FunctionError15"));
				}
			}
			//
			if (action.equals("OUTPUT")) {
				session_.browseFile(getExcellBookURI());
			}
			//
			pos1 = action.indexOf("CALL(", 0);
			if (pos1 >= 0) {
				pos2 = action.indexOf(")", pos1);
				String functionID = action.substring(pos1+5, pos2);
				try {
					returnMap = session_.executeFunction(functionID, parmMap_);
					if (returnMap.get("RETURN_CODE").equals("30")) {
						returnMap_.put("RETURN_CODE", "30");
						closeFunction();
					} else {
						if (returnMap.get("RETURN_MESSAGE") == null) {
							messageList.add(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
						} else {
							messageList.add(returnMap.get("RETURN_MESSAGE").toString());
						}
						if (returnMap.get("RETURN_CODE").equals("10")
								|| returnMap.get("RETURN_CODE").equals("20")) {
							fetchHeaderRecord(true);
							for (int i = 0; i < jTabbedPane.getTabCount(); i++) {
								if (filterListArray[i].size() == 0 || !anyFilterIsEditable[i]) {
									selectDetailRecordsAndSetupTableRows(i, false);
								}
							}
							returnMap_.put("RETURN_CODE", returnMap.get("RETURN_CODE"));
						}
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
					messageList.add(res.getString("FunctionError15"));
				}
			}
			//
			setMessagesOnPanel();
			//
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	 class WorkingRow extends Object {
		private ArrayList<Object> columnValueList_ = new ArrayList<Object>();
		private ArrayList<Object> orderByValueList_ = new ArrayList<Object>();
		private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
		private HashMap<String, Object> columnMap_ = new HashMap<String, Object>();
		private int index_;
		//
		public WorkingRow(int index, ArrayList<Object> columnValueList, HashMap<String, Object> keyMap, HashMap<String, Object> columnMap, ArrayList<Object> orderByValueList) {
			columnValueList_ = columnValueList;
			keyMap_ = keyMap;
			columnMap_ = columnMap;
			orderByValueList_ = orderByValueList;
			index_ = index;
		}
		public HashMap<String, Object> getKeyMap() {
			return keyMap_;
		}
		public HashMap<String, Object> getColumnMap() {
			return columnMap_;
		}
		public ArrayList<Object> getColumnValueList() {
			return columnValueList_;
		}
		public ArrayList<Object> getOrderByValueList() {
			return orderByValueList_;
		}
		public int getIndex() {
			return index_;
		}
	}

	class WorkingTableHeader extends JTableHeader {
		private static final long serialVersionUID = 1L;
		public WorkingTableHeader(TableColumnModel model) {
			super(model);
		}
		public String getToolTipText(MouseEvent e) {
			String text = "";
			int tabIndex = jTabbedPane.getSelectedIndex();
			int c = columnAtPoint(e.getPoint());
			for (int i = 0; i < detailColumnListArray[tabIndex].size(); i++) {
				if (detailColumnListArray[tabIndex].get(i).isVisibleOnPanel()) {
					if (detailColumnListArray[tabIndex].get(i).getColumnIndex() == c) {
						if (detailColumnListArray[tabIndex].get(i).getDecimalSize() > 0) {
							text = "<html>" + detailColumnListArray[tabIndex].get(i).getFieldName() + " " + detailColumnListArray[tabIndex].get(i).getDataSourceName() + " (" + detailColumnListArray[tabIndex].get(i).getDataSize() + "," + detailColumnListArray[tabIndex].get(i).getDecimalSize() + ")<br>" + detailColumnListArray[tabIndex].get(i).getFieldRemarks();
						} else {
							text = "<html>" + detailColumnListArray[tabIndex].get(i).getFieldName() + " " + detailColumnListArray[tabIndex].get(i).getDataSourceName() + " (" + detailColumnListArray[tabIndex].get(i).getDataSize() + ")<br>" + detailColumnListArray[tabIndex].get(i).getFieldRemarks();
						}
						break;
					}
				}
			}
			return text;
		}
	}
		
	 class WorkingRowComparator implements java.util.Comparator<WorkingRow>{
		 public int compare(WorkingRow row1, WorkingRow row2){
			 int compareResult = 0;
			 for (int i = 0; i < row1.getOrderByValueList().size(); i++) {
				 compareResult = row1.getOrderByValueList().get(i).toString().compareTo(row2.getOrderByValueList().get(i).toString());
				 if (detailTableArray[row1.getIndex()].getOrderByFieldIDList().get(i).contains("(D)")) {
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
		HSSFFont font = null;
		TableCellReadOnly cellObject = null;
		String imageFileName = "";
		//
		HSSFWorkbook workBook = new HSSFWorkbook();
		String wrkStr = functionElement_.getAttribute("Name").replace("/", "_").replace("^", "_");
		HSSFSheet workSheet = workBook.createSheet(wrkStr);
		workSheet.setDefaultRowHeight( (short) 300);
		HSSFFooter workSheetFooter = workSheet.getFooter();
		workSheetFooter.setRight(functionElement_.getAttribute("Name") + "  Page " + HSSFFooter.page() + " / " + HSSFFooter.numPages() );
		patriarch = workSheet.createDrawingPatriarch();
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
		HSSFCellStyle styleHeaderLabel = workBook.createCellStyle();
		styleHeaderLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleHeaderLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		styleHeaderLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleHeaderLabel.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		styleHeaderLabel.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		styleHeaderLabel.setFont(fontHeader);
		//
		HSSFCellStyle styleDetailLabel = workBook.createCellStyle();
		styleDetailLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleDetailLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		styleDetailLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleDetailLabel.setAlignment(HSSFCellStyle.ALIGN_LEFT);
		styleDetailLabel.setFont(fontHeader);
		//
		HSSFCellStyle styleDetailNumberLabel = workBook.createCellStyle();
		styleDetailNumberLabel.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleDetailNumberLabel.setFillForegroundColor(HSSFColor.GREY_25_PERCENT.index);
		styleDetailNumberLabel.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styleDetailNumberLabel.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		styleDetailNumberLabel.setFont(fontHeader);
		//
		HSSFCellStyle styleDataInteger = workBook.createCellStyle();
		styleDataInteger.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderRight(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setBorderTop(HSSFCellStyle.BORDER_THIN);
		styleDataInteger.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
		styleDataInteger.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
		styleDataInteger.setFont(fontDataBlack);
		styleDataInteger.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
		//
		int currentRowNumber = -1;
		int mergeRowNumberFrom = -1;
		//
		try {
			xlsFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".xls");
			xlsFileName = xlsFile.getPath();
			fileOutputStream = new FileOutputStream(xlsFileName);
			int columnIndex;
			//
			// Header Lines //
			for (int i = 0; i < headerFieldList.size(); i++) {
				if (headerFieldList.get(i).isVisibleOnPanel()) {
					for (int j = 0; j < headerFieldList.get(i).getRows(); j++) {
						//
						currentRowNumber++;
						HSSFRow rowData = workSheet.createRow(currentRowNumber);
						//
						// Cells for header field label //
						HSSFCell cellHeader = rowData.createCell(0);
						cellHeader.setCellStyle(styleHeaderLabel);
						if (j==0) {
							mergeRowNumberFrom = currentRowNumber;
							if (!headerFieldList.get(i).getFieldOptionList().contains("NO_CAPTION")) {
								cellHeader.setCellValue(new HSSFRichTextString(headerFieldList.get(i).getCaption()));
							}
						}
						rowData.createCell(1).setCellStyle(styleHeaderLabel);
						//
						// Cells for header field data //
						if (headerFieldList.get(i).getColor().equals("black")) {
							font = fontDataBlack;
						}
						if (headerFieldList.get(i).getColor().equals("red")) {
							font = fontDataRed;
						}
						if (headerFieldList.get(i).getColor().equals("blue")) {
							font = fontDataBlue;
						}
						if (headerFieldList.get(i).getColor().equals("green")) {
							font = fontDataGreen;
						}
						if (headerFieldList.get(i).getColor().equals("orange")) {
							font = fontDataOrange;
						}
						setupCellAttributesForHeaderField(rowData, workBook, workSheet, headerFieldList.get(i), currentRowNumber, j, font);
					}
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 0, 1));
					workSheet.addMergedRegion(new CellRangeAddress(mergeRowNumberFrom, currentRowNumber, 2, 6));
				}
			}
			//
			// Spacer //
			currentRowNumber++;
			workSheet.createRow(currentRowNumber);
			//
			// Detail Lines //
			currentRowNumber++;
			HSSFRow rowCaption = workSheet.createRow(currentRowNumber);
			for (int i = 0; i < tableModelMainArray[jTabbedPane.getSelectedIndex()].getColumnCount(); i++) {
				HSSFCell cell = rowCaption.createCell(i);
				if (i == 0) {
					cell.setCellStyle(styleDetailNumberLabel);
				} else {
					for (int j = 0; j < detailColumnListArray[jTabbedPane.getSelectedIndex()].size(); j++) {
						if (detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).getColumnIndex() == i) {
							if (detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).getBasicType().equals("INTEGER")
									|| detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).getBasicType().equals("FLOAT")) {
								if (detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).getTypeOptionList().contains("MSEQ")
										|| detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).getTypeOptionList().contains("FYEAR")) {
									cell.setCellStyle(styleDetailLabel);
								} else {
									cell.setCellStyle(styleDetailNumberLabel);
								}
							} else {
								cell.setCellStyle(styleDetailLabel);
							}
							break;
						}
					}
				}
				Rectangle rect = jTableMainArray[jTabbedPane.getSelectedIndex()].getCellRect(0, i, true);
				workSheet.setColumnWidth(i, rect.width * 40);
				cell.setCellValue(new HSSFRichTextString(tableModelMainArray[jTabbedPane.getSelectedIndex()].getColumnName(i)));
			}
			//
			for (int i = 0; i < tableModelMainArray[jTabbedPane.getSelectedIndex()].getRowCount(); i++) {
				currentRowNumber++;
				HSSFRow rowData = workSheet.createRow(currentRowNumber);
				//
				HSSFCell cell = rowData.createCell(0); //Column of Sequence Number
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
				cell.setCellStyle(styleDataInteger);
				cell.setCellValue(i + 1);
				//
				columnIndex = 0;
				for (int j = 0; j < detailColumnListArray[jTabbedPane.getSelectedIndex()].size(); j++) {
					if (detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).isVisibleOnPanel()) {
						columnIndex++;
						cellObject = (TableCellReadOnly)tableModelMainArray[jTabbedPane.getSelectedIndex()].getValueAt(i,columnIndex);
						if (cellObject.getColor().equals(Color.black)) {
							font = fontDataBlack;
						}
						if (cellObject.getColor().equals(Color.red)) {
							font = fontDataRed;
						}
						if (cellObject.getColor().equals(Color.blue)) {
							font = fontDataBlue;
						}
						if (cellObject.getColor().equals(Color.green)) {
							font = fontDataGreen;
						}
						if (cellObject.getColor().equals(Color.orange)) {
							font = fontDataOrange;
						}
						setupCellAttributesForDetailColumn(rowData.createCell(columnIndex), workBook, detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).getBasicType(), detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).getTypeOptionList(), cellObject, font, detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).getDecimalSize());
						if (cellObject.isImage() && !cellObject.getInternalValue().equals("")) {
							imageFileName = session_.getImageFileFolder() + cellObject.getInternalValue();
							XFUtility.setupImageCellForDetailColumn(workBook, workSheet, currentRowNumber, columnIndex, imageFileName, patriarch);
						}
					}
				}
			}
			//
			workBook.write(fileOutputStream);
			//
			messageList.add(res.getString("XLSComment1"));
			//
		} catch (Exception e) {
			messageList.add(res.getString("XLSErrorMessage"));
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

	private void setupCellAttributesForHeaderField(HSSFRow rowData, HSSFWorkbook workBook, HSSFSheet workSheet, XF300_HeaderField object, int currentRowNumber, int rowIndexInCell, HSSFFont font) {
		String wrk;
		//
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
		//
		HSSFCell cellValue = rowData.createCell(2);
		//
		String basicType = object.getBasicType();
		if (basicType.equals("INTEGER")) {
			if (object.getTypeOptionList().contains("MSEQ") || object.getTypeOptionList().contains("FYEAR")) {
				cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
				cellValue.setCellStyle(style);
				cellValue.setCellValue(new HSSFRichTextString((String)object.getExternalValue()));
			} else {
				wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
				if (wrk.equals("")) {
					cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
					cellValue.setCellStyle(style);
					cellValue.setCellValue(new HSSFRichTextString(object.getExternalValue().toString()));
				} else {
					cellValue.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
					style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(Double.parseDouble(wrk));
					}
				}
			}
		} else {
			if (basicType.equals("FLOAT")) {
				wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
				if (wrk.equals("")) {
					cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(new HSSFRichTextString(""));
					}
				} else {
					cellValue.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
					style.setDataFormat(XFUtility.getFloatFormat(workBook, object.getDecimalSize()));
					cellValue.setCellStyle(style);
					if (rowIndexInCell==0) {
						cellValue.setCellValue(Double.parseDouble(wrk));
					}
				}
			} else {
				cellValue.setCellType(HSSFCell.CELL_TYPE_STRING);
				cellValue.setCellStyle(style);
				if (rowIndexInCell==0) {
					if (basicType.equals("STRING")) {
						if (object.isImage()) {
							style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
							style.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);
							style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
							cellValue.setCellStyle(style);
							cellValue.setCellValue(new HSSFRichTextString((String)object.getInternalValue()));
							try {
								XFUtility.setupImageCellForField(workBook, workSheet, 2, currentRowNumber, 4, object.getRows(), (String)object.getExternalValue(), patriarch);
							} catch(Exception e) {
								e.printStackTrace(exceptionStream);
							}
//							style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
//							style.setVerticalAlignment(HSSFCellStyle.VERTICAL_BOTTOM);
//							style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
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
//										// read in the image file and copy the image bytes into the ByteArrayOutputStream//
//										fis = new FileInputStream(imageFile);
//										bos = new ByteArrayOutputStream();
//										int c;
//										while ((c = fis.read()) != -1) {
//											bos.write(c);
//										}
//										// add the image bytes to the workbook //
//										pictureIndex = workBook.addPicture( bos.toByteArray(), imageType);
//										anchor = new HSSFClientAnchor(0,0,0,0,
//												(short)2, currentRowNumber, (short)6, currentRowNumber + object.getRows());
//										anchor.setAnchorType(0);
//										anchor.setDx1(30);
//										anchor.setDy1(30);
//										anchor.setDx2(-30);
//										anchor.setDy2(-250);
//										patriarch.createPicture(anchor, pictureIndex);
//									} catch(Exception e) {
//										e.printStackTrace(exceptionStream);
//									} finally {
//										try {
//											if (fis != null) {
//												fis.close();
//											}
//											if (bos != null) {
//												bos.close();
//											}
//										} catch (IOException e) {
//											e.printStackTrace(exceptionStream);
//										}
//									}
//								}
//							}
						} else {
							cellValue.setCellStyle(style);
							cellValue.setCellValue(new HSSFRichTextString((String)object.getExternalValue()));
						}
					} else {
						if (basicType.equals("DATE")) {
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
		}
		rowData.createCell(3).setCellStyle(style);
		rowData.createCell(4).setCellStyle(style);
		rowData.createCell(5).setCellStyle(style);
		rowData.createCell(6).setCellStyle(style);
	}

	private void setupCellAttributesForDetailColumn(HSSFCell cell, HSSFWorkbook workBook, String basicType, ArrayList<String> typeOptionList, TableCellReadOnly object, HSSFFont font, int decimalSize) {
		String wrk;
		//
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
		//
		Object value = object.getExternalValue();
		if (basicType.equals("INTEGER")) {
			if (typeOptionList.contains("MSEQ") || typeOptionList.contains("FYEAR")) {
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellValue(new HSSFRichTextString(object.getExternalValue().toString()));
				style.setAlignment(HSSFCellStyle.ALIGN_LEFT);
				style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
				style.setWrapText(true);
				style.setDataFormat(HSSFDataFormat.getBuiltinFormat("text"));
				cell.setCellStyle(style);
			} else {
				if (value == null) {
					wrk = "";
				} else {
					wrk = XFUtility.getStringNumber(value.toString());
				}
				if (wrk.equals("")) {
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					cell.setCellStyle(style);
					cell.setCellValue(new HSSFRichTextString(value.toString()));
				} else {
					cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
					style.setDataFormat(HSSFDataFormat.getBuiltinFormat("#,##0"));
					cell.setCellStyle(style);
					cell.setCellValue(Double.parseDouble(wrk));
				}
			}
		} else {
			if (basicType.equals("FLOAT")) {
				if (value == null) {
					wrk = "";
				} else {
					wrk = XFUtility.getStringNumber(value.toString());
				}
				if (wrk.equals("")) {
					cell.setCellType(HSSFCell.CELL_TYPE_STRING);
					cell.setCellStyle(style);
					cell.setCellValue(new HSSFRichTextString(wrk));
				} else {
					cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
					style.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
					style.setVerticalAlignment(HSSFCellStyle.VERTICAL_TOP);
					style.setDataFormat(XFUtility.getFloatFormat(workBook, decimalSize));
					cell.setCellStyle(style);
					cell.setCellValue(Double.parseDouble(wrk));
				}
			} else {
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
				cell.setCellStyle(style);
				if (value == null || object.isImage()) {
					wrk = "";
				} else {
					wrk = value.toString();
				}
				cell.setCellValue(new HSSFRichTextString(wrk));
			}
		}
	}
	
	void jTableMain_focusGained(FocusEvent e) {
		if (jTableMainArray[jTabbedPane.getSelectedIndex()].getRowCount() > 0) {
			jTableMainArray[jTabbedPane.getSelectedIndex()].setSelectionBackground(selectionColorWithFocus);
			jTableMainArray[jTabbedPane.getSelectedIndex()].setSelectionForeground(Color.white);
		} else {
			jTabbedPane.requestFocus();
		}
	}

	void jTableMain_focusLost(FocusEvent e) {
		for (int i = 0; i < detailTabSortingList.getSize(); i++) {
			jTableMainArray[i].setSelectionBackground(selectionColorWithoutFocus);
			jTableMainArray[i].setSelectionForeground(Color.black);
		}
	}

	void jScrollPaneTable_mousePressed(MouseEvent e) {
		jTableMainArray[jTabbedPane.getSelectedIndex()].requestFocus();
	}

	void jTabbedPane_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			jTableMainArray[jTabbedPane.getSelectedIndex()].requestFocus();
			if (jTableMainArray[jTabbedPane.getSelectedIndex()].getRowCount() > 0) {
				jTableMainArray[jTabbedPane.getSelectedIndex()].setRowSelectionInterval(0, 0);
			}
		}
		//
		// Steps to hack F6 and F8 of jSplitPane //
		if (e.getKeyCode() == KeyEvent.VK_F6) {
			if (!actionF6.equals("")) {
				int workIndex = Integer.parseInt(actionF6);
				jButtonArray[workIndex].doClick();
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_F8) {
			if (!actionF8.equals("")) {
				int workIndex = Integer.parseInt(actionF8);
				jButtonArray[workIndex].doClick();
			}
		}
	}

	void jTabbedPane_stateChanged(ChangeEvent e) {
		if (tablesReadyToUse) {
			//
			int index = jTabbedPane.getSelectedIndex();
			//
			jPanelTopCenter.removeAll();
			jPanelTop.setVisible(false);
			jPanelTopEast.setVisible(false);
			if (filterListArray[index].size() > 0) {
				jPanelTop.setVisible(true);
				jPanelTopCenter.add(jPanelFilter[index], BorderLayout.CENTER);
				//filterListArray[index].get(0).requestFocus();
				jPanelTopEast.setVisible(anyFilterIsEditable[index]);
				//
				if (anyFilterIsEditable[index]) {
					jPanelTopEast.setVisible(true);
				}
			}
			//
			jScrollPaneTable.getViewport().add(jTableMainArray[index], null);
			//
			messageList.clear();
			if (jTableMainArray[index].getRowCount() > 0) {
				if (initialMsgArray[index].equals("")) {
					messageList.add(res.getString("FunctionMessage3"));
				} else {
					messageList.add(initialMsgArray[index]);
				}
			} else {
				if (initialMsgArray[index].equals("")) {
					if (filterListArray[index].size() > 0 && anyFilterIsEditable[index]) {
						messageList.add(res.getString("FunctionMessage1"));
					} else {
						messageList.add(res.getString("FunctionMessage31"));
					}
				} else {
					messageList.add(initialMsgArray[index]);
				}
			}
			setMessagesOnPanel();
			//
			setupFunctionKeysAndButtonsForTabIndex(index);
		}
	}
	
	void dialog_keyPressed(KeyEvent e) {
	}

	void jTableMain_mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2 && tableModelMainArray[jTabbedPane.getSelectedIndex()].getRowCount() > 0) {
			processRow(false);
		}
	}

	void jTableMain_keyPressed(KeyEvent e) {
		//
		if (e.getKeyCode() == KeyEvent.VK_UP && jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow() == 0) {
			jTabbedPane.requestFocus();
		}
		//
		if (e.getKeyCode() == KeyEvent.VK_ENTER && tableModelMainArray[jTabbedPane.getSelectedIndex()].getRowCount() > 0) {
			processRow(true);
		}
		//
		// Steps to hack F6 and F8 of component //
		if (e.getKeyCode() == KeyEvent.VK_F6) {
			if (!actionF6.equals("")) {
				int workIndex = Integer.parseInt(actionF6);
				jButtonArray[workIndex].doClick();
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_F8 && !actionF8.equals("")) {
			if (!actionF8.equals("")) {
				int workIndex = Integer.parseInt(actionF8);
				jButtonArray[workIndex].doClick();
			}
		}
	}
	
	void processRow(boolean enterKeyPressed) {
		messageList.clear();
		if (jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow() > -1) {
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				//
				int rowNumber = jTableMainArray[jTabbedPane.getSelectedIndex()].convertRowIndexToModel(jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow());
				if (rowNumber > -1) {
					XF300_DetailRowNumber detailRowNumber = (XF300_DetailRowNumber)tableModelMainArray[jTabbedPane.getSelectedIndex()].getValueAt(rowNumber,0);
					HashMap<String, Object> keyMap = detailRowNumber.getKeyMap();
					//
					if (detailFunctionIDArray[jTabbedPane.getSelectedIndex()].equals("")) {
						if (detailParmTypeArray[jTabbedPane.getSelectedIndex()].equals("COLUMNS")) {
							keyMap = detailRowNumber.getColumnMap();
						}
						returnMap_.putAll(keyMap);
						closeFunction();
					} else {
						if (detailFunctionIDArray[jTabbedPane.getSelectedIndex()].equals("NONE")) {
							messageList.add(res.getString("FunctionMessage52"));
						} else {	
							keyMap.putAll(parmMap_);
							try {
								HashMap<String, Object> returnMap = session_.executeFunction(detailFunctionIDArray[jTabbedPane.getSelectedIndex()], keyMap);
								if (returnMap.get("RETURN_MESSAGE") == null) {
									messageList.add(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
								} else {
									messageList.add(returnMap.get("RETURN_MESSAGE").toString());
								}
								if (returnMap.get("RETURN_CODE").equals("20") || returnMap.get("RETURN_CODE").equals("30")) {
									fetchHeaderRecord(true);
									selectDetailRecordsAndSetupTableRows(jTabbedPane.getSelectedIndex(), false);
								}
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, e.getMessage());
								messageList.add(res.getString("FunctionError15"));
							}
						}
					}
				}
			} finally {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
		//
		if (enterKeyPressed && jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow() >= 0) {
			if (jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow() == 0) {
				jTableMainArray[jTabbedPane.getSelectedIndex()].setRowSelectionInterval(jTableMainArray[jTabbedPane.getSelectedIndex()].getRowCount() - 1, jTableMainArray[jTabbedPane.getSelectedIndex()].getRowCount() - 1);
			} else {
				jTableMainArray[jTabbedPane.getSelectedIndex()].setRowSelectionInterval(jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow() - 1, jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow() - 1);
			}
		}
		//
		setMessagesOnPanel();
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

	public String getScriptNameRunning() {
		return scriptNameRunning;
	}

	public org.w3c.dom.Element getFunctionElement() {
		return functionElement_;
	}

	public String getFunctionInfo() {
		return jLabelFunctionID.getText();
	}

	public Session getSession() {
		return session_;
	}

	public PrintStream getExceptionStream() {
		return exceptionStream;
	}

	public Bindings getHeaderScriptBindings() {
		return 	headerScriptBindings;
	}

	public void evalHeaderTableScript(String scriptName, String scriptText) throws ScriptException {
		if (!scriptText.equals("")) {
			scriptNameRunning = scriptName;
			scriptEngine.eval(scriptText + session_.getScriptFunctions());
		}
	}

	public void evalDetailTableScript(String scriptName, String scriptText, int index) throws ScriptException {
		if (!scriptText.equals("")) {
			scriptNameRunning = scriptName;
			scriptEngine.eval(scriptText + session_.getScriptFunctions(), detailScriptBindingsArray[index]);
		}
	}

	public HashMap<String, Object> getParmMap() {
		return parmMap_;
	}
	
	public void setProcessLog(String text) {
		XFUtility.appendLog(text, processLog);
	}
	
	public StringBuffer getProcessLog() {
		return processLog;
	}

	public XFTableOperator createTableOperator(String oparation, String tableID) {
		return new XFTableOperator(session_, processLog, oparation, tableID);
	}

	public XFTableOperator createTableOperator(String sqlText) {
		return new XFTableOperator(session_, processLog, sqlText);
	}

	public HashMap<String, Object> getReturnMap() {
		return returnMap_;
	}
	
	public XF300_HeaderTable getHeaderTable() {
		return headerTable_;
	}
	
	public ArrayList<XF300_HeaderReferTable> getHeaderReferTableList() {
		return headerReferTableList;
	}

	public ArrayList<XF300_HeaderField> getHeaderFieldList() {
		return headerFieldList;
	}

	public XF300_HeaderField getHeaderFieldObjectByID(String tableID, String tableAlias, String fieldID) {
		XF300_HeaderField headerField = null;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (tableID.equals("")) {
				if (headerFieldList.get(i).getTableAlias().equals(tableAlias) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					headerField = headerFieldList.get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					headerField = headerFieldList.get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getTableAlias().equals(tableAlias) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					headerField = headerFieldList.get(i);
					break;
				}
			}
		}
		return headerField;
	}

	public boolean containsHeaderField(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (tableID.equals("")) {
				if (headerFieldList.get(i).getTableAlias().equals(tableAlias)
						&& headerFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID)
						&& headerFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID)
						&& headerFieldList.get(i).getTableAlias().equals(tableAlias)
						&& headerFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public boolean containsDetailField(int tabIndex, String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < detailColumnListArray[tabIndex].size(); i++) {
			if (tableID.equals("")) {
				if (detailColumnListArray[tabIndex].get(i).getTableAlias().equals(tableAlias)
						&& detailColumnListArray[tabIndex].get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (detailColumnListArray[tabIndex].get(i).getTableID().equals(tableID)
						&& detailColumnListArray[tabIndex].get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (detailColumnListArray[tabIndex].get(i).getTableID().equals(tableID)
						&& detailColumnListArray[tabIndex].get(i).getTableAlias().equals(tableAlias)
						&& detailColumnListArray[tabIndex].get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public XF300_DetailTable getDetailTable(int tabIndex) {
		return detailTableArray[tabIndex];
	}

	public ArrayList<XF300_DetailColumn> getDetailColumnList(int tabIndex) {
		return detailColumnListArray[tabIndex];
	}

	public ArrayList<XF300_DetailReferTable> getDetailReferTableList(int tabIndex) {
		return detailReferTableListArray[tabIndex];
	}
	
	public XF300_StructureTable getStructureTable() {
		return structureTable_;
	}

	public Bindings getDetailScriptBindings(int tabIndex) {
		return detailScriptBindingsArray[tabIndex];
	}

	public XF300_Filter getFilterObjectByName(int index, String dataSourceName) {
		XF300_Filter filter = null;
		for (int i = 0; i < filterListArray[index].size(); i++) {
			if (filterListArray[index].get(i).getDataSourceName().equals(dataSourceName)) {
				filter = filterListArray[index].get(i);
				break;
			}
		}
		return filter;
	}
	
	public ArrayList<XF300_Filter> getFilterList(int tabIndex) {
		return filterListArray[tabIndex];
	}
	
	public double getFilterWidth(int tabIndex) {
		return filterWidth[tabIndex];
	}

	public XF300_DetailColumn getDetailColumnObjectByID(int tabIndex, String tableID, String tableAlias, String fieldID) {
		XF300_DetailColumn detailColumnField = null;
		for (int i = 0; i < detailColumnListArray[tabIndex].size(); i++) {
			if (tableID.equals("")) {
				if (detailColumnListArray[tabIndex].get(i).getTableAlias().equals(tableAlias) && detailColumnListArray[tabIndex].get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnListArray[tabIndex].get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (detailColumnListArray[tabIndex].get(i).getTableID().equals(tableID) && detailColumnListArray[tabIndex].get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnListArray[tabIndex].get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (detailColumnListArray[tabIndex].get(i).getTableID().equals(tableID) && detailColumnListArray[tabIndex].get(i).getTableAlias().equals(tableAlias) && detailColumnListArray[tabIndex].get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnListArray[tabIndex].get(i);
					break;
				}
			}
		}
		return detailColumnField;
	}

	public String getTableIDOfTableAlias(String tableAlias, int index) {
		String tableID = tableAlias;
		org.w3c.dom.Element workElement;
		//
		for (int j = 0; j < headerReferElementList.getLength(); j++) {
			workElement = (org.w3c.dom.Element)headerReferElementList.item(j);
			if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
				tableID = workElement.getAttribute("ToTable");
				break;
			}
		}
		//
		if (index > -1) {
			for (int j = 0; j < detailReferElementList[index].getLength(); j++) {
				workElement = (org.w3c.dom.Element)detailReferElementList[index].item(j);
				if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
					tableID = workElement.getAttribute("ToTable");
					break;
				}
			}
		}
		//
		return tableID;
	}
	
	public int getSelectedTabIndex() {
		return jTabbedPane.getSelectedIndex();
	}

	boolean isTheRowToBeSelected(int index) {
		boolean toBeSelected = true;
		//
		for (int i = 0; i < filterListArray[index].size(); i++) {
			toBeSelected = filterListArray[index].get(i).isValidated();
			if (!toBeSelected) {
				break;
			}
		}
		//
		return toBeSelected;
	}
}

class XF300_HeaderField extends JPanel implements XFScriptableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
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
	private int fieldRows = 1;
	private JPanel jPanelField = new JPanel();
	private JLabel jLabelField = new JLabel();
	private JLabel jLabelFieldComment = new JLabel();
	private XFTextField xFTextField = null;
	private XFCheckBox xFCheckBox = null;
	private XFDateField xFDateField = null;
	private XFTextArea xFTextArea = null;
	private XFYMonthBox xFYMonthBox = null;
	private XFMSeqBox xFMSeqBox = null;
	private XFFYearBox xFFYearBox = null;
	private XFUrlField xFUrlField = null;
	private XFImageField xFImageField = null;
	private XFEditableField component = null;
	private ArrayList<String> keyValueList = new ArrayList<String>();
	private String keyValue = "";
	private ArrayList<String> textValueList = new ArrayList<String>();
	private boolean isKey = false;
	private boolean isFieldOnPrimaryTable = false;
	private boolean isVisibleOnPanel = true;
	private boolean isHorizontal = false;
	private boolean isVirtualField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private boolean isImage = false;
	private int positionMargin = 0;
	private Color foreground = Color.black;
	private XF300 dialog_;

	public XF300_HeaderField(org.w3c.dom.Element functionFieldElement, XF300 dialog){
		super();
		//
		String wrkStr;
		dialog_ = dialog;
		functionFieldElement_ = functionFieldElement;
		fieldOptions = functionFieldElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);
		//
		StringTokenizer workTokenizer = new StringTokenizer(functionFieldElement_.getAttribute("DataSource"), "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog.getTableIDOfTableAlias(tableAlias_, -1);
		fieldID_ =workTokenizer.nextToken();
		//
		if (tableID_.equals(dialog_.getHeaderTable().getTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnPrimaryTable = true;
			//
			ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
			for (int i = 0; i < keyFieldList.size(); i++) {
				if (keyFieldList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
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
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + res.getString("FunctionError11"));
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
		jLabelField.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelField.setVerticalAlignment(SwingConstants.TOP);
		jLabelField.setFont(new java.awt.Font("Dialog", 0, 14));
		FontMetrics metrics = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		if (fieldOptionList.contains("CAPTION_LENGTH_VARIABLE")) {
			jLabelField.setPreferredSize(new Dimension(metrics.stringWidth(fieldCaption), XFUtility.FIELD_UNIT_HEIGHT));
		} else {
			jLabelField.setPreferredSize(new Dimension(120, XFUtility.FIELD_UNIT_HEIGHT));
			if (metrics.stringWidth(fieldCaption) > 120) {
				jLabelField.setFont(new java.awt.Font("Dialog", 0, 12));
				metrics = jLabelField.getFontMetrics(new java.awt.Font("Dialog", 0, 12));
				if (metrics.stringWidth(fieldCaption) > 120) {
					jLabelField.setFont(new java.awt.Font("Dialog", 0, 10));
				}
			}
		}
		//
		if (dataType.equals("VARCHAR") || dataType.equals("LONG VARCHAR")) {
			xFTextArea = new XFTextArea(dataSize, "", fieldOptions);
			xFTextArea.setLocation(5, 0);
			xFTextArea.setEditable(false);
			component = xFTextArea;
		} else {
			if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN").equals("")) {
				xFCheckBox = new XFCheckBox(dataTypeOptions);
				xFCheckBox.setLocation(5, 0);
				xFCheckBox.setEditable(false);
				component = xFCheckBox;
			} else {
				if (dataTypeOptionList.contains("URL")) {
					xFUrlField = new XFUrlField(dataSize);
					xFUrlField.setLocation(5, 0);
					xFUrlField.setEditable(false);
					component = xFUrlField;
				} else {
					if (dataTypeOptionList.contains("IMAGE")) {
						xFImageField = new XFImageField(fieldOptions, dataSize, dialog_.getSession().getImageFileFolder());
						xFImageField.setLocation(5, 0);
						component = xFImageField;
						isImage = true;
					} else {
						if (dataType.equals("DATE")) {
							xFDateField = new XFDateField(dialog_.getSession());
							xFDateField.setLocation(5, 0);
							xFDateField.setEditable(false);
							component = xFDateField;
						} else {
							xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions);
							xFTextField.setLocation(5, 0);
							xFTextField.setEditable(false);
							component = xFTextField;
							if (dataTypeOptionList.contains("YMONTH")) {
								xFYMonthBox = new XFYMonthBox(dialog_.getSession());
								xFYMonthBox.setLocation(5, 0);
								xFYMonthBox.setEditable(false);
								component = xFYMonthBox;
							} else {
								if (dataTypeOptionList.contains("MSEQ")) {
									xFMSeqBox = new XFMSeqBox(dialog_.getSession());
									xFMSeqBox.setLocation(5, 0);
									xFMSeqBox.setEditable(false);
									component = xFMSeqBox;
								} else {
									if (dataTypeOptionList.contains("FYEAR")) {
										xFFYearBox = new XFFYearBox(dialog_.getSession());
										xFFYearBox.setLocation(5, 0);
										xFFYearBox.setEditable(false);
										component = xFFYearBox;
									} else {
										xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions);
										xFTextField.setLocation(5, 0);
										xFTextField.setEditable(false);
										//
										wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
										if (!wrkStr.equals("")) {
											String wrk;
											int fieldWidth = 0;
											JLabel jLabel = new JLabel();
											metrics = jLabel.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
											try {
												XFTableOperator operator = dialog_.createTableOperator("Select", dialog_.getSession().getTableNameOfUserVariants());
												operator.addKeyValue("IDUSERKUBUN", wrkStr);
												operator.setOrderBy("SQLIST");
												while (operator.next()) {
													keyValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
													wrk = operator.getValueOf("TXUSERKUBUN").toString().trim();
													textValueList.add(wrk);
													if (metrics.stringWidth(wrk) > fieldWidth) {
														fieldWidth = metrics.stringWidth(wrk);
													}
												}
												xFTextField.setSize(fieldWidth + 10, xFTextField.getHeight());
											} catch (Exception e) {
												e.printStackTrace(dialog_.getExceptionStream());
												dialog_.setErrorAndCloseFunction();
											}
										}
										//
										component = xFTextField;
									}
								}
							}
						}
					}
				}
			}
		}
		fieldRows = component.getRows();
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "WIDTH");
		if (!wrkStr.equals("")) {
			component.setWidth(Integer.parseInt(wrkStr));
		}
		//
		jPanelField.setLayout(null);
		jPanelField.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		jPanelField.add((JComponent)component);
		//
		this.setOpaque(false);
		this.setLayout(new BorderLayout());
		if (fieldOptionList.contains("NO_CAPTION")) {
			this.setPreferredSize(new Dimension(component.getWidth() + 10, component.getHeight()));
		} else {
			this.setPreferredSize(new Dimension(component.getWidth() + jLabelField.getPreferredSize().width + 10, component.getHeight()));
			this.add(jLabelField, BorderLayout.WEST);
		}
		this.add(jPanelField, BorderLayout.CENTER);
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "COMMENT");
		if (!wrkStr.equals("")) {
			jLabelFieldComment.setText(" " + wrkStr);
			jLabelFieldComment.setForeground(Color.blue);
			jLabelFieldComment.setFont(new java.awt.Font("Dialog", 0, 12));
			jLabelFieldComment.setVerticalAlignment(SwingConstants.TOP);
			metrics = jLabelFieldComment.getFontMetrics(new java.awt.Font("Dialog", 0, 12));
			this.setPreferredSize(new Dimension(this.getPreferredSize().width + metrics.stringWidth(wrkStr) + 5, this.getPreferredSize().height));
			this.add(jLabelFieldComment, BorderLayout.EAST);
		}
		//
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		if (decimalSize > 0) {
			wrkStr = "<html>" + fieldName + " " + tableAlias_ + "." + fieldID_ + " (" + dataSize + "," + decimalSize + ")<br>" + fieldRemarks;
		} else {
			wrkStr = "<html>" + fieldName + " " + tableAlias_ + "." + fieldID_ + " (" + dataSize + ")<br>" + fieldRemarks;
		}
		this.setToolTipText(wrkStr);
		component.setToolTipText(wrkStr);
		//
		if (!dialog_.getHeaderScriptBindings().containsKey(this.getFieldIDInScript())) {
			dialog_.getHeaderScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
		}
	}

	public XF300_HeaderField(String tableID, String tableAlias, String fieldID, XF300 dialog){
		super();
		//
		String wrkStr;
		dialog_ = dialog;
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
		//
		if (tableID_.equals(dialog_.getHeaderTable().getTableID()) && tableID_.equals(tableAlias_)) {
			isFieldOnPrimaryTable = true;
			//
			ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
			for (int i = 0; i < keyFieldList.size(); i++) {
				if (keyFieldList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
		}
		//
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
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
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
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
		xFTextField.setEditable(false);
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			//
			String wrk;
			int fieldWidth = 50;
			JLabel jLabel = new JLabel();
			FontMetrics metrics = jLabel.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
			//
			try {
				XFTableOperator operator = dialog_.createTableOperator("Select", dialog_.getSession().getTableNameOfUserVariants());
				operator.addKeyValue("IDUSERKUBUN", wrkStr);
				operator.setOrderBy("SQLIST");
				while (operator.next()) {
					keyValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
					wrk = operator.getValueOf("TXUSERKUBUN").toString().trim();
					textValueList.add(wrk);
					if (metrics.stringWidth(wrk) > fieldWidth) {
						fieldWidth = metrics.stringWidth(wrk);
					}
				}
				xFTextField.setSize(fieldWidth, xFTextField.getHeight());
			} catch (Exception e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}
		//
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
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		if (!dialog_.getHeaderScriptBindings().containsKey(this.getFieldIDInScript())) {
			dialog_.getHeaderScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
		}
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

	public String getDataSourceName(){
		return tableAlias_ + "." + fieldID_;
	}

	public int getDecimalSize(){
		return decimalSize;
	}

	public String getCaption(){
		return jLabelField.getText();
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

	public boolean isFieldOnPrimaryTable(){
		return isFieldOnPrimaryTable;
	}

	public boolean isKey(){
		return isKey;
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
						if (value == null) {
							component.setValue("");
						} else {
							String stringValue = value.toString().trim();
							String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
							if (!wrkStr.equals("")) {
								for (int i = 0; i < keyValueList.size(); i++) {
									if (keyValueList.get(i).equals(stringValue)) {
										keyValue = stringValue;
										component.setValue(textValueList.get(i));
										break;
									}
								}

							} else {
								component.setValue(stringValue);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public void setValue(Object object){
		String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (wrkStr.equals("")) {
			XFUtility.setValueToEditableField(this.getBasicType(), object, component);
		} else {
			for (int i = 0; i < keyValueList.size(); i++) {
				if (keyValueList.get(i).equals(object)) {
					keyValue = (String)object;
					component.setValue(textValueList.get(i));
					break;
				}
			}
		}
	}

	public Object getInternalValue(){
		Object returnObj = null;
		//
		String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			returnObj = keyValue;
		} else {
			returnObj = (String)component.getInternalValue();
			//returnObj = component.getInternalValue();
		}
		//
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
		//
		String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			returnObj = keyValue;
		} else {
			if (this.getBasicType().equals("INTEGER")) {
				returnObj = Long.parseLong((String)component.getInternalValue());
			} else {
				if (this.getBasicType().equals("FLOAT")) {
					returnObj = Double.parseDouble((String)component.getInternalValue());
				} else {
					if (component.getInternalValue() == null) {
						returnObj = "";
					} else {
						returnObj = (String)component.getInternalValue();
					}
				}
			}
		}
		//
		return returnObj;
	}

	public void setOldValue(Object object){
	}

	public Object getOldValue() {
		return getInternalValue();
	}
	
	public boolean isValueChanged() {
		return !this.getValue().equals(this.getOldValue());
	}

	public void setEditable(boolean editable){
		component.setEditable(editable);
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
		component.setForeground(foreground);
	}

	public String getColor() {
		return XFUtility.convertColorToString(foreground);
	}

	public String getFieldIDInScript(){
		return tableAlias_ + "_" + fieldID_;
	}
}

class XF300_DetailRowNumberRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = 1L;
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		XF300_DetailRowNumber cell = (XF300_DetailRowNumber)value;
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

class XF300_DetailRowNumber extends Object {
	private int number_;
	private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
	private HashMap<String, Object> columnMap_ = new HashMap<String, Object>();
	private DecimalFormat integerFormat = new DecimalFormat("0000");
	//
	public XF300_DetailRowNumber(int num, HashMap<String, Object> keyMap, HashMap<String, Object> columnMap) {
		number_ = num;
		keyMap_ = keyMap;
		columnMap_ = columnMap;
	}
	public String toString() {
		return integerFormat.format(number_);
	}
	public HashMap<String, Object> getKeyMap() {
		return keyMap_;
	}
	public HashMap<String, Object> getColumnMap() {
		return columnMap_;
	}
}

class XF300_DetailColumn extends Object implements XFScriptableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element functionColumnElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF300 dialog_ = null;
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
	private String fieldOptions = "";
	private String fieldCaption = "";
	private int fieldWidth = 50;
	private int columnIndex = -1;
	private boolean isVisibleOnPanel = true;
	private boolean isVirtualField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private boolean isReadyToValidate = false;
	private boolean isImage = false;
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

	public XF300_DetailColumn(String detailTableID, org.w3c.dom.Element functionColumnElement, XF300 dialog, int index){
		super();
		//
		String wrkStr;
		functionColumnElement_ = functionColumnElement;
		dialog_ = dialog;
		fieldOptions = functionColumnElement_.getAttribute("FieldOptions");
		//
		StringTokenizer workTokenizer = new StringTokenizer(functionColumnElement_.getAttribute("DataSource"), "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog.getTableIDOfTableAlias(tableAlias_, index);
		fieldID_ =workTokenizer.nextToken();
		//
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + res.getString("FunctionError11"));
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
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
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
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		JLabel jLabel = new JLabel();
		FontMetrics metrics = jLabel.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		int captionWidth = metrics.stringWidth(fieldCaption) + 18;
		//
		String basicType = this.getBasicType();
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			try {
				XFTableOperator operator = dialog_.createTableOperator("Select", dialog_.getSession().getTableNameOfUserVariants());
				operator.addKeyValue("IDUSERKUBUN", wrkStr);
				operator.setOrderBy("SQLIST");
				while (operator.next()) {
					kubunValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
					wrkStr = operator.getValueOf("TXUSERKUBUN").toString().trim();
					if (metrics.stringWidth(wrkStr) + 10 > fieldWidth) {
						fieldWidth = metrics.stringWidth(wrkStr) + 10;
					}
					kubunTextList.add(wrkStr);
				}
			} catch (Exception e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
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
									if (dataTypeOptionList.contains("IMAGE")) {
										isImage = true;
										fieldWidth = 60;
									} else {
										fieldWidth = dataSize * 7 + 15;
									}
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
		dialog_.getDetailScriptBindings(index).put(this.getFieldIDInScript(), (XFScriptableField)this);
	}

	public XF300_DetailColumn(String detailTableID, String tableID, String tableAlias, String fieldID, XF300 dialog, int index){
		super();
		//
		functionColumnElement_ = null;
		dialog_ = dialog;
		fieldOptions = "";
		//
		isVisibleOnPanel = false;
		//
		tableID_ = tableID;
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}
		fieldID_ = fieldID;
		//
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + res.getString("FunctionError11"));
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
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		dialog_.getDetailScriptBindings(index).put(this.getFieldIDInScript(), (XFScriptableField)this);
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

	public boolean isReadyToValidate(){
		return isReadyToValidate;
	}

	public void setReadyToValidate(boolean isReady) {
		isReadyToValidate = isReady;
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

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
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
		String basicType = this.getBasicType();
		//
		if (basicType.equals("INTEGER") && !dataTypeOptionList.contains("MSEQ") && !dataTypeOptionList.contains("FYEAR")) {
			if (value_ == null || value_.toString().equals("")) {
				value = "";
			} else {
				String wrkStr = value_.toString();
				int pos = wrkStr.indexOf(".");
				if (pos >= 0) {
					wrkStr = wrkStr.substring(0, pos);
				}
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
						if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN").equals("")) {
							String wrkStr = value_.toString().trim();
							for (int i = 0; i < kubunValueList.size(); i++) {
								if (kubunValueList.get(i).equals(wrkStr)) {
									value = kubunTextList.get(i);
									break;
								}
							}
						} else {
							if (this.isImage()) {
								value = XFUtility.createSmallIcon(dialog_.getSession().getImageFileFolder() + value_.toString().trim());
							} else {
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
						}
					}
				}
			}
		}
		//
		return value;
	}
	
	public boolean isImage() {
		return isImage;
	}

	public TableCellReadOnly getCellObject() {
		return new TableCellReadOnly(this.getInternalValue(), this.getExternalValue(), this.getForeground(), isImage);
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

	public boolean setValueOfResultSet(XFTableOperator operator) {
		String basicType = this.getBasicType();
		boolean isFoundInResultSet = false;
		this.setColor("");
		//
		try {
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					value_ = XFUtility.calculateExpireValue(this.getTableElement(), operator, dialog_.getSession(), dialog_.getProcessLog());
				}
			} else {
				//
				Object value = operator.getValueOf(this.getFieldID()); 
				isFoundInResultSet = true;
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
		} catch (Exception e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
		return isFoundInResultSet;
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


class XF300_Filter extends JPanel {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private org.w3c.dom.Element fieldElement_ = null;
	private XF300 dialog_ = null;
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
	private JComboBox jComboBox = null;
	private XF300_PromptCallField xFPromptCall = null;
	private ArrayList<String> keyValueList = new ArrayList<String>();
	private JComponent component = null;
	private boolean isReflect = false;
	private boolean isEditable_ = true;
	private int index_ = 0;
	//
	public XF300_Filter(org.w3c.dom.Element fieldElement, XF300 dialog, int index){
		super();
		//
		String wrkStr;
		fieldElement_ = fieldElement;
		dialog_ = dialog;
		index_ = index;
		//
		fieldOptions = fieldElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);
		StringTokenizer workTokenizer1 = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer1.nextToken();
		tableID = dialog_.getTableIDOfTableAlias(tableAlias, index);
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
		int fieldWidthMax = (int)dialog_.getFilterWidth(index_) - 110;
		//
		XFTableOperator operator;
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
					try {
						operator = dialog_.createTableOperator("Select", dialog_.getSession().getTableNameOfUserVariants());
						operator.addKeyValue("IDUSERKUBUN", wrkStr);
						operator.setOrderBy("SQLIST");
						while (operator.next()) {
							valueIndex++;
							wrkKey = operator.getValueOf("KBUSERKUBUN").toString().trim();
							keyValueList.add(wrkKey);
							if (wrkKey.equals(defaultValue)) {
								selectIndex = valueIndex;
							}
							wrkText = operator.getValueOf("TXUSERKUBUN").toString().trim();
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
					} catch(Exception e) {
						e.printStackTrace(dialog_.getExceptionStream());
						dialog_.setErrorAndCloseFunction();
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
						//
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
						ArrayList<XF300_DetailReferTable> referTableList = dialog_.getDetailReferTableList(index);
						for (int i = 0; i < referTableList.size(); i++) {
							if (referTableList.get(i).getTableID().equals(tableID)) {
								if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias)) {
									//
									if (fieldOptionList.contains("PROMPT_LIST1")) {
										valueIndex++;
										jComboBox.addItem("");
									}
									//
									try {
										StringBuffer buf = new StringBuffer();
										buf.append("select ");
										buf.append(fieldID);
										buf.append(" from ");
										buf.append(tableID);
										buf.append(" order by ");
										buf.append(fieldID);
										operator = dialog_.createTableOperator(buf.toString());
										while (operator.next()) {
											valueIndex++;
											wrkKey = operator.getValueOf(fieldID).toString().trim();
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
									} catch(Exception e) {
										e.printStackTrace(dialog_.getExceptionStream());
										dialog_.setErrorAndCloseFunction();
									}
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
					xFPromptCall = new XF300_PromptCallField(fieldElement, wrkStr, dialog_, index_);
					xFPromptCall.setLocation(5, 0);
					xFPromptCall.setValue(getDefaultValue());
					component = xFPromptCall;
				} else {
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
		component.addKeyListener(new XF300_keyAdapter(dialog));
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

	public void setParmMapValue(HashMap<String, Object> parmMap){
		String mapKey = this.getTableID() + "." + this.getFieldID();
		//
		if (parmMap != null && parmMap.containsKey(mapKey)) {
			//
			Object mapValue = parmMap.get(mapKey);
			//
			if (componentType.equals("TEXTFIELD")) {
				xFTextField.setText(mapValue.toString().trim());
			}
			//
			if (componentType.equals("DATE")) {
				xFDateField.setValue(mapValue.toString());
				xFDateField.setEditable(false);
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
			if (componentType.equals("PROMPT_CALL")) {
				xFPromptCall.setValue(mapValue.toString());
			}
		}
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
				xFTextField.setText(mapValue.toString().trim());
			}
			//
			if (componentType.equals("DATE")) {
				xFDateField.setValue(mapValue.toString());
				//xFDateField.setEditable(false);
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
		XF300_DetailColumn columnField = dialog_.getDetailColumnObjectByID(index_, this.getTableID(), this.getTableAlias(), this.getFieldID());
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
			if (componentType.equals("PROMPT_CALL")) {
				columnField.setValue(xFPromptCall.getInternalValue());
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

class XF300_PromptCallField extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private String tableID = "";
	private String tableAlias = "";
	private String fieldID = "";
	private int rows_ = 1;
	private XFTextField xFTextField;
	private JButton jButton = new JButton();
	private boolean isEditable = true;
    private XF300 dialog_;
    private org.w3c.dom.Element fieldElement_;
    private String functionID_ = "";
    private ArrayList<XF300_DetailReferTable> referTableList_;
    private String oldValue = "";
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();
    private int index_;

	public XF300_PromptCallField(org.w3c.dom.Element fieldElement, String functionID, XF300 dialog, int index){
		//
		super();
		//
		fieldElement_ = fieldElement;
		functionID_ = functionID;
		dialog_ = dialog;
		index_ = index;
		//
		String fieldOptions = fieldElement_.getAttribute("FieldOptions");
		StringTokenizer workTokenizer = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		fieldID =workTokenizer.nextToken();
		//
		tableID = tableAlias;
		referTableList_ = dialog_.getDetailReferTableList(index);
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
				HashMap<String, Object> fieldValuesMap = new HashMap<String, Object>();
				for (int i = 0; i < fieldsToPutList_.size(); i++) {
					value = dialog_.getFilterObjectByName(index_, fieldsToPutList_.get(i));
					if (value != null) {
						fieldValuesMap.put(fieldsToPutToList_.get(i), value);
					}
				}
				try {
					HashMap<String, Object> returnMap = dialog_.getSession().executeFunction(functionID_, fieldValuesMap);
					if (!returnMap.get("RETURN_CODE").equals("99")) {
						HashMap<String, Object> fieldsToGetMap = new HashMap<String, Object>();
						for (int i = 0; i < fieldsToGetList_.size(); i++) {
							value = returnMap.get(fieldsToGetList_.get(i));
							if (value != null) {
								fieldsToGetMap.put(fieldsToGetToList_.get(i), value);
							}
						}
						for (int i = 0; i < dialog_.getFilterList(index_).size(); i++) {
							value = fieldsToGetMap.get(dialog_.getFilterList(index_).get(i).getDataSourceName());
							if (value != null && dialog_.getFilterList(index_).get(i).isEditable()) {
								dialog_.getFilterList(index_).get(i).setValue(value);
							}
						}
						if (!xFTextField.getText().equals("")) {
							xFTextField.setFocusable(true);
						}
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(null, ex.getMessage());
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
		int fieldWidth = (int)dialog_.getFilterWidth(index_) - 100 - 27;
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


class XF300_HeaderTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID = "";
	private String activeWhere = "";
	private String fixedWhere = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF300 dialog_;
	private StringTokenizer workTokenizer;

	public XF300_HeaderTable(org.w3c.dom.Element functionElement, XF300 dialog){
		super();
		//
		functionElement_ = functionElement;
		dialog_ = dialog;
		//
		tableID = functionElement_.getAttribute("HeaderTable");
		tableElement = dialog_.getSession().getTableElement(tableID);
		activeWhere = tableElement.getAttribute("ActiveWhere");
		fixedWhere = functionElement_.getAttribute("FixedWhere");
		//
		String workString;
		org.w3c.dom.Element workElement;
		//
		if (functionElement_.getAttribute("HeaderKeyFields").equals("")) {
			NodeList nodeList = tableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer.hasMoreTokens()) {
						workString = workTokenizer.nextToken();
						keyFieldIDList.add(workString);
					}
					break;
				}
			}
		} else {
			workTokenizer = new StringTokenizer(functionElement_.getAttribute("HeaderKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				keyFieldIDList.add(workTokenizer.nextToken());
			}
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

	public String getSelectSQL(){
		int count;
		StringBuffer buf = new StringBuffer();
		//
		buf.append("select ");
		//
		count = -1;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isFieldOnPrimaryTable() && !dialog_.getHeaderFieldList().get(i).isVirtualField()) {
				count++;
				if (count > 0) {
					buf.append(",");
				}
				buf.append(dialog_.getHeaderFieldList().get(i).getFieldID());
			}
		}
		//
		buf.append(" from ");
		buf.append(tableID);
		//
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < dialog_.getHeaderFieldList().size(); i++) {
			if (dialog_.getHeaderFieldList().get(i).isKey()) {
				count++;
				if (count > 0) {
					buf.append(" and ") ;
				}
				buf.append(dialog_.getHeaderFieldList().get(i).getFieldID()) ;
				buf.append("=") ;
				if (XFUtility.isLiteralRequiredBasicType(dialog_.getHeaderFieldList().get(i).getBasicType())) {
					buf.append("'") ;
					if (dialog_.getParmMap().containsKey(dialog_.getHeaderFieldList().get(i).getFieldID())) {
						buf.append(dialog_.getParmMap().get(dialog_.getHeaderFieldList().get(i).getFieldID()));
					} else {
						buf.append(dialog_.getParmMap().get(dialog_.getHeaderFieldList().get(i).getDataSourceName()));
					}
					buf.append("'") ;
				} else {
					if (dialog_.getParmMap().containsKey(dialog_.getHeaderFieldList().get(i).getFieldID())) {
						buf.append(dialog_.getParmMap().get(dialog_.getHeaderFieldList().get(i).getFieldID()));
					} else {
						buf.append(dialog_.getParmMap().get(dialog_.getHeaderFieldList().get(i).getDataSourceName()));
					}
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
		if (!fixedWhere.equals("")) {
			buf.append(" and (");
			buf.append(fixedWhere);
			buf.append(") ");
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
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF300_HeaderReferTable referTable;
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
			for (int i = 0; i < dialog_.getHeaderReferTableList().size(); i++) {
				referTable = dialog_.getHeaderReferTableList().get(i);
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

	public void runScript(String event1, String event2) throws ScriptException {
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
			dialog_.evalHeaderTableScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
		}
	}
}

class XF300_HeaderReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF300 dialog_ = null;
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
	public XF300_HeaderReferTable(org.w3c.dom.Element referElement, XF300 dialog){
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

	public String getSelectSQL(){
		int count;
		org.w3c.dom.Element workElement;
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
				for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
					if (withKeyFieldIDList.get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
						if (XFUtility.isLiteralRequiredBasicType(dialog_.getHeaderFieldList().get(j).getBasicType())) {
							buf.append("'") ;
							buf.append(dialog_.getHeaderFieldList().get(j).getInternalValue());
							buf.append("'") ;
						} else {
							buf.append(dialog_.getHeaderFieldList().get(j).getInternalValue());
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
		return  fieldIDList;
	}
	
	public void setToBeExecuted(boolean executed){
		isToBeExecuted = executed;
	}
	
	public boolean isToBeExecuted(){
		return isToBeExecuted;
	}

	public boolean isRecordToBeSelected(XFTableOperator operator) throws Exception {
		boolean returnValue = false;
		//
		if (rangeKeyType == 0) {
			returnValue = true;
		}
		//
		if (rangeKeyType == 1) {
			if (!rangeValidated) { 
				// Note that result set is ordered by rangeKeyFieldValue DESC //
				StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
				String workTableAlias = workTokenizer.nextToken();
				String workFieldID = workTokenizer.nextToken();
				Object valueKey = dialog_.getHeaderFieldObjectByID("", workTableAlias, workFieldID).getInternalValue();
				Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
					rangeValidated = true;
				}
			}
		}
		//
		if (rangeKeyType == 2) {
			StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
			String workTableAlias = workTokenizer.nextToken();
			String workFieldID = workTokenizer.nextToken();
			Object valueKey = dialog_.getHeaderFieldObjectByID("", workTableAlias, workFieldID).getInternalValue();
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
		//
		return returnValue;
	}
}

class XF300_StructureTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element functionElement_ = null;
	private org.w3c.dom.Element tableElement_ = null;
	private String tableID = "";
	private String activeWhere = "";
	private ArrayList<String> upperKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> childKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private ArrayList<String> rootTextFieldIDList = new ArrayList<String>();
	private ArrayList<String> nodeTextFieldIDList = new ArrayList<String>();
	private HashMap<Object, Icon> nodeIconMap = new HashMap<Object, Icon>();
	private ImageIcon defaultIcon = null;
	private String iconControlFieldID;
	private String basicTypeOfIconControlField = "";
	private XF300 dialog_;

	public XF300_StructureTable(org.w3c.dom.Element functionElement, XF300 dialog){
		super();
		String imageFileFolder, wrkStr;
		ImageIcon icon;
		StringTokenizer workTokenizer1, workTokenizer2;
		org.w3c.dom.Element workElement;
		//
		functionElement_ = functionElement;
		dialog_ = dialog;
		//
		tableID = functionElement_.getAttribute("StructureTable");
		tableElement_ = dialog_.getSession().getTableElement(tableID);
		activeWhere = tableElement_.getAttribute("ActiveWhere");
		workTokenizer1 = new StringTokenizer(functionElement_.getAttribute("StructureUpperKeys"), ";" );
		while (workTokenizer1.hasMoreTokens()) {
			upperKeyFieldIDList.add(workTokenizer1.nextToken());
		}
		workTokenizer1 = new StringTokenizer(functionElement_.getAttribute("StructureChildKeys"), ";" );
		while (workTokenizer1.hasMoreTokens()) {
			childKeyFieldIDList.add(workTokenizer1.nextToken());
		}
		workTokenizer1 = new StringTokenizer(functionElement_.getAttribute("StructureOrderBy"), ";" );
		while (workTokenizer1.hasMoreTokens()) {
			orderByFieldIDList.add(workTokenizer1.nextToken());
		}
		workTokenizer1 = new StringTokenizer(functionElement_.getAttribute("StructureRootText"), ";" );
		while (workTokenizer1.hasMoreTokens()) {
			rootTextFieldIDList.add(workTokenizer1.nextToken());
		}
		workTokenizer1 = new StringTokenizer(functionElement_.getAttribute("StructureNodeText"), ";" );
		while (workTokenizer1.hasMoreTokens()) {
			nodeTextFieldIDList.add(workTokenizer1.nextToken());
		}
		//
		imageFileFolder = dialog_.getSession().getImageFileFolder();
		if (!functionElement_.getAttribute("StructureNodeDefaultIcon").equals("")) {
			defaultIcon = new ImageIcon(imageFileFolder + functionElement_.getAttribute("StructureNodeDefaultIcon"));
		}
		iconControlFieldID = functionElement_.getAttribute("StructureNodeIconsFieldID");;
		if (!iconControlFieldID.equals("")) {
			workElement = dialog_.getSession().getFieldElement(dialog_.getHeaderTable().getTableID(), iconControlFieldID);
			basicTypeOfIconControlField = XFUtility.getBasicTypeOf(workElement.getAttribute("Type"));
		}
		//
		workTokenizer1 = new StringTokenizer(functionElement_.getAttribute("StructureNodeIconsFieldValues"), ";" );
		workTokenizer2 = new StringTokenizer(functionElement_.getAttribute("StructureNodeIcons"), ";" );
		while (workTokenizer1.hasMoreTokens()) {
			icon = new ImageIcon(imageFileFolder + workTokenizer2.nextToken());
			wrkStr = workTokenizer1.nextToken();
			wrkStr = wrkStr.replaceAll("'", "");
			if (XFUtility.isLiteralRequiredBasicType(basicTypeOfIconControlField)) {
				nodeIconMap.put(wrkStr, icon);
			} else {
				nodeIconMap.put(Integer.parseInt(wrkStr), icon);
			}
		}
	}
	
	public String getName() {
		return tableElement_.getAttribute("Name");
	}
	
	public String getSelectSQL(HashMap<String, Object> parmMap){
		int count;
		StringBuffer buf = new StringBuffer();
		XF300_HeaderField headerField;
		//
		buf.append("select * from ");
		buf.append(tableID);
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < dialog_.getHeaderTable().getKeyFieldIDList().size(); i++) {
			count++;
			if (count > 0) {
				buf.append(" and ") ;
			}
			if (dialog_.isForExplosion()) {
				buf.append(upperKeyFieldIDList.get(i)) ;
			} else {
				buf.append(childKeyFieldIDList.get(i)) ;
			}
			buf.append("=") ;
			headerField = dialog_.getHeaderFieldObjectByID(dialog_.getHeaderTable().getTableID(), "", dialog_.getHeaderTable().getKeyFieldIDList().get(i));
			if (XFUtility.isLiteralRequiredBasicType(headerField.getBasicType())) {
				buf.append("'");
				buf.append(parmMap.get(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
				buf.append("'");
			} else {
				buf.append(parmMap.get(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
			}
		}
		//
		if (orderByFieldIDList.size() > 0) {
			int pos;
			buf.append(" order by ");
			for (int i = 0; i < orderByFieldIDList.size(); i++) {
				if (i > 0) {
					buf.append(",");
				}
				pos = orderByFieldIDList.get(i).indexOf("(A)");
				if (pos >= 0) {
					buf.append(orderByFieldIDList.get(i).substring(0, pos));
				} else {
					pos = orderByFieldIDList.get(i).indexOf("(D)");
					if (pos >= 0) {
						buf.append(orderByFieldIDList.get(i).substring(0, pos));
						buf.append(" DESC ");
					} else {
						buf.append(orderByFieldIDList.get(i).substring(0, orderByFieldIDList.get(i).length()));
					}
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
		return buf.toString();
	}
	
	public String getSQLForNodeInfo(HashMap<String, Object> keyValueMap){
		int count;
		StringBuffer buf = new StringBuffer();
		XF300_HeaderField headerField;
		//
		buf.append("select * from ");
		buf.append(dialog_.getHeaderTable().getTableID());
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < dialog_.getHeaderTable().getKeyFieldIDList().size(); i++) {
			count++;
			if (count > 0) {
				buf.append(" and ") ;
			}
			buf.append(dialog_.getHeaderTable().getKeyFieldIDList().get(i)) ;
			buf.append("=") ;
			headerField = dialog_.getHeaderFieldObjectByID(dialog_.getHeaderTable().getTableID(), "", dialog_.getHeaderTable().getKeyFieldIDList().get(i));
			if (XFUtility.isLiteralRequiredBasicType(headerField.getBasicType())) {
				buf.append("'");
				buf.append(keyValueMap.get(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
				buf.append("'");
			} else {
				buf.append(keyValueMap.get(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
			}
		}
		//
		return buf.toString();
	}
	
	public String getSQLForNodeInfo(XFTableOperator structreTableOperator) throws Exception {
		int count;
		StringBuffer buf = new StringBuffer();
		XF300_HeaderField headerField;
		//
		buf.append("select * from ");
		buf.append(dialog_.getHeaderTable().getTableID());
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < dialog_.getHeaderTable().getKeyFieldIDList().size(); i++) {
			count++;
			if (count > 0) {
				buf.append(" and ") ;
			}
			buf.append(dialog_.getHeaderTable().getKeyFieldIDList().get(i)) ;
			buf.append("=") ;
			headerField = dialog_.getHeaderFieldObjectByID(dialog_.getHeaderTable().getTableID(), "", dialog_.getHeaderTable().getKeyFieldIDList().get(i));
			if (XFUtility.isLiteralRequiredBasicType(headerField.getBasicType())) {
				buf.append("'");
				if (dialog_.isForExplosion()) {
					buf.append(structreTableOperator.getValueOf(childKeyFieldIDList.get(i)));
				} else {
					buf.append(structreTableOperator.getValueOf(upperKeyFieldIDList.get(i)));
				}
				buf.append("'");
			} else {
				if (dialog_.isForExplosion()) {
					buf.append(structreTableOperator.getValueOf(childKeyFieldIDList.get(i)));
				} else {
					buf.append(structreTableOperator.getValueOf(upperKeyFieldIDList.get(i)));
				}
			}
		}
		//
		return buf.toString();
	}
	
	public XF300_TreeNode createTreeNode(XFTableOperator headerTableOperator) throws Exception {
		HashMap<String, Object> keyValueMap = new HashMap<String, Object>();
		for (int i = 0; i < dialog_.getHeaderTable().getKeyFieldIDList().size(); i++) {
			keyValueMap.put(dialog_.getHeaderTable().getKeyFieldIDList().get(i), headerTableOperator.getValueOf(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
		}
		//
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < rootTextFieldIDList.size(); i++) {
			if (rootTextFieldIDList.get(i).contains("'")) {
				sb.append(rootTextFieldIDList.get(i).replaceAll("'", ""));
			} else {
				if (rootTextFieldIDList.get(i).contains(".")) {
					sb.append("???");
					break;
				} else {
					if (headerTableOperator.hasValueOf(dialog_.getHeaderTable().getTableID(), rootTextFieldIDList.get(i))) {
						sb.append(headerTableOperator.getValueOf(rootTextFieldIDList.get(i)).toString().trim());
					}
				}
			}
		}
		//
		Icon icon = null;
		Object value = headerTableOperator.getValueOf(iconControlFieldID);
		if (XFUtility.isLiteralRequiredBasicType(basicTypeOfIconControlField)) {
			icon = nodeIconMap.get(value.toString());
		} else {
			int intValue = Integer.parseInt(value.toString());
			icon = nodeIconMap.get(intValue);
		}
		if (icon == null) {
			icon = defaultIcon;
		}
		//
		return new XF300_TreeNode(keyValueMap, sb.toString(), icon, dialog_);
	}
	
	public XF300_TreeNode createTreeNode(XFTableOperator headerTableOperator, XFTableOperator structureTableOperator) throws Exception {
		HashMap<String, Object> keyValueMap = new HashMap<String, Object>();
		for (int i = 0; i < dialog_.getHeaderTable().getKeyFieldIDList().size(); i++) {
			keyValueMap.put(dialog_.getHeaderTable().getKeyFieldIDList().get(i), headerTableOperator.getValueOf(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
		}
		//
		StringBuffer sb = new StringBuffer();
		String wrkTableID, fieldID;
		for (int i = 0; i < nodeTextFieldIDList.size(); i++) {
			if (nodeTextFieldIDList.get(i).contains("'")) {
				sb.append(nodeTextFieldIDList.get(i).replaceAll("'", ""));
			} else {
				if (nodeTextFieldIDList.get(i).contains(".")) {
					wrkTableID = nodeTextFieldIDList.get(i).substring(0, nodeTextFieldIDList.get(i).indexOf("."));
					fieldID = nodeTextFieldIDList.get(i).substring(nodeTextFieldIDList.get(i).indexOf(".")+1, nodeTextFieldIDList.get(i).length());
					if (wrkTableID.equals(dialog_.getHeaderTable().getTableID())) {
						sb.append(headerTableOperator.getValueOf(fieldID).toString().trim());
					} else {
						if (wrkTableID.equals(tableID)) {
							sb.append(structureTableOperator.getValueOf(fieldID).toString().trim());
						} else {
							sb.append("???");
						}
					}
				} else {
					sb.append("???");
				}
			}
		}
		//
		Icon icon = null;
		Object value = headerTableOperator.getValueOf(iconControlFieldID);
		if (XFUtility.isLiteralRequiredBasicType(basicTypeOfIconControlField)) {
			icon = nodeIconMap.get(value.toString());
		} else {
			int intValue = Integer.parseInt(value.toString());
			icon = nodeIconMap.get(intValue);
		}
		if (icon == null) {
			icon = defaultIcon;
		}
		//
		return new XF300_TreeNode(keyValueMap, sb.toString(), icon, dialog_);
	}
}

class XF300_DetailTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element detailTableElement_ = null;
	private String tableID = "";
	private String activeWhere = "";
	private String fixedWhere = "";
	private ArrayList<String> headerKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF300 dialog_;
	private int tabIndex_;
	private StringTokenizer workTokenizer;
	private boolean hasOrderByAsItsOwnFields = true;

	public XF300_DetailTable(org.w3c.dom.Element detailTableElement, int tabIndex, XF300 dialog){
		super();
		//
		detailTableElement_ = detailTableElement;
		tabIndex_ = tabIndex;
		dialog_ = dialog;
		//
		tableID = detailTableElement.getAttribute("Table");
		tableElement = dialog_.getSession().getTableElement(tableID);
		activeWhere = tableElement.getAttribute("ActiveWhere");
		fixedWhere = detailTableElement.getAttribute("FixedWhere");
		//
		int pos1;
		String wrkStr1, wrkStr2;
		org.w3c.dom.Element workElement;
		//
		if (detailTableElement_.getAttribute("HeaderKeyFields").equals("")) {
			org.w3c.dom.Element headerTableElement = dialog_.getSession().getTableElement(dialog_.getHeaderTable().getTableID());
			NodeList nodeList = headerTableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer.hasMoreTokens()) {
						wrkStr1 = workTokenizer.nextToken();
						headerKeyFieldIDList.add(wrkStr1);
					}
					break;
				}
			}
		} else {
			workTokenizer = new StringTokenizer(detailTableElement_.getAttribute("HeaderKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				headerKeyFieldIDList.add(workTokenizer.nextToken());
			}
		}
		//
		if (detailTableElement_.getAttribute("KeyFields").equals("")) {
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
			workTokenizer = new StringTokenizer(detailTableElement_.getAttribute("KeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				keyFieldIDList.add(workTokenizer.nextToken());
			}
		}
		//
		workTokenizer = new StringTokenizer(detailTableElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			wrkStr1 = workTokenizer.nextToken();
			//
			pos1 = wrkStr1.indexOf(".");
			if (pos1 > -1) { 
				wrkStr2 = wrkStr1.substring(0, pos1);
				if (!wrkStr2.equals(this.tableID)) {
					hasOrderByAsItsOwnFields = false;
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
	
	public String getSelectSQL(){
		int count;
		StringBuffer buf = new StringBuffer();
		XF300_HeaderField headerField;
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
		for (int i = 0; i < dialog_.getDetailColumnList(tabIndex_).size(); i++) {
			if (dialog_.getDetailColumnList(tabIndex_).get(i).getTableID().equals(tableID) && !dialog_.getDetailColumnList(tabIndex_).get(i).isVirtualField()) {
				count++;
				if (count > 0) {
					buf.append(",");
				}
				buf.append(dialog_.getDetailColumnList(tabIndex_).get(i).getFieldID());
			}
		}
		//
		buf.append(" from ");
		buf.append(tableID);
		//
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < headerKeyFieldIDList.size(); i++) {
			count++;
			if (count > 0) {
				buf.append(" and ") ;
			}
			buf.append(keyFieldIDList.get(i)) ;
			buf.append("=") ;
			headerField = dialog_.getHeaderFieldObjectByID(dialog_.getHeaderTable().getTableID(), "", headerKeyFieldIDList.get(i));
			if (XFUtility.isLiteralRequiredBasicType(headerField.getBasicType())) {
				buf.append("'");
				buf.append(headerField.getInternalValue());
				buf.append("'");
			} else {
				buf.append(headerField.getInternalValue());
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
		if (this.hasOrderByAsItsOwnFields) {
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
	
	public ArrayList<String> getHeaderKeyFieldIDList(){
		return headerKeyFieldIDList;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return keyFieldIDList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}

	public org.w3c.dom.Element getDetailTableElement() {
		return detailTableElement_;
	}

	public org.w3c.dom.Element getTableElement() {
		return tableElement;
	}
	
	public ArrayList<String> getOrderByFieldIDList(){
		return orderByFieldIDList;
	}
	
	public boolean hasOrderByAsItsOwnFields(){
		return hasOrderByAsItsOwnFields;
	}
	
	public boolean isValidDataSource(int index, String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF300_DetailReferTable referTable;
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
			for (int i = 0; i < dialog_.getDetailReferTableList(index).size(); i++) {
				referTable = dialog_.getDetailReferTableList(index).get(i);
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

	public void runScript(int index, String event1, String event2) throws ScriptException {
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
			dialog_.evalDetailTableScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText(), index);
		}
	}
}

class XF300_DetailReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private int tabIndex_;
	private XF300 dialog_ = null;
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

	public XF300_DetailReferTable(org.w3c.dom.Element referElement, int tabIndex, XF300 dialog){
		super();
		//
		referElement_ = referElement;
		tabIndex_ = tabIndex;
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
				for (int j = 0; j < dialog_.getDetailColumnList(tabIndex_).size(); j++) {
					if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList(tabIndex_).get(j).getDataSourceName())) {
						//if (dialog_.getDetailColumnList(tabIndex_).get(j).isLiteralRequired()) {
						if (XFUtility.isLiteralRequiredBasicType(dialog_.getDetailColumnList(tabIndex_).get(j).getBasicType())) {
							buf.append("'");
							buf.append(dialog_.getDetailColumnList(tabIndex_).get(j).getInternalValue());
							buf.append("'");
							if (!dialog_.getDetailColumnList(tabIndex_).get(j).getInternalValue().equals("")) {
								validWhereKeys = true;
							}
						} else {
							buf.append(dialog_.getDetailColumnList(tabIndex_).get(j).getInternalValue());
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

	public boolean isRecordToBeSelected(int index, XFTableOperator operator) throws Exception {
		boolean returnValue = false;
		//
		if (rangeKeyType == 0) {
			returnValue = true;
		}
		//
		if (rangeKeyType == 1) {
			// Note that result set is ordered by rangeKeyFieldValue DESC //
			if (!rangeValidated) { 
				StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
				String workTableAlias = workTokenizer.nextToken();
				String workFieldID = workTokenizer.nextToken();
				Object valueKey = dialog_.getDetailColumnObjectByID(index, "", workTableAlias, workFieldID).getInternalValue();
				Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
					rangeValidated = true;
				}
			}
		}
		//
		if (rangeKeyType == 2) {
			StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
			String workTableAlias = workTokenizer.nextToken();
			String workFieldID = workTokenizer.nextToken();
			Object valueKey = dialog_.getDetailColumnObjectByID(index, "", workTableAlias, workFieldID).getInternalValue();
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
		//
		return returnValue;
	}
}

class XF300_KeyInputDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private JPanel jPanelMain = new JPanel();
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
	private XF300 dialog_;
	private final int FIELD_VERTICAL_MARGIN = 5;
	private final int FONT_SIZE = 14;
	private ArrayList<XF300_HeaderField> fieldList = new ArrayList<XF300_HeaderField>();
	private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
	
	public XF300_KeyInputDialog(XF300 dialog) {
		super(dialog, "", true);
		dialog_ = dialog;
		initComponentsAndVariants();
	}

	void initComponentsAndVariants() {
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
		jButtonOK.setBounds(new Rectangle(200, 2, 80, 32));
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
		XF300_HeaderField field;
		//
		this.setTitle(dialog_.getTitle());
		jLabelSessionID.setText(dialog_.getSession().getSessionID());
		jLabelFunctionID.setText(dialog_.getFunctionInfo());
		FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font("Dialog", 0, FONT_SIZE));
		jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));
		//
		NodeList headerFieldElementList = dialog_.getFunctionElement().getElementsByTagName("Field");
		SortableDomElementListModel sortingList1 = XFUtility.getSortedListModel(headerFieldElementList, "Order");
		for (int i = 0; i < sortingList1.getSize(); i++) {
			element = (org.w3c.dom.Element)sortingList1.getElementAt(i);
			workTokenizer = new StringTokenizer(element.getAttribute("DataSource"), "." );
			tableAlias = workTokenizer.nextToken();
			tableID = dialog_.getTableIDOfTableAlias(tableAlias, -1);
			fieldID =workTokenizer.nextToken();
			//
			if (tableID.equals(dialog_.getHeaderTable().getTableID())) {
				ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
				for (int j = 0; j < keyFieldList.size(); j++) {
					if (keyFieldList.get(j).equals(fieldID)) {
						field = new XF300_HeaderField((org.w3c.dom.Element)sortingList1.getElementAt(i), dialog_);
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
		this.pack();
	}
	
	public HashMap<String, Object> requestKeyValues(String message) {
		keyMap_.clear();
		//
		if (message.equals("")) {
			jTextAreaMessages.setText(res.getString("FunctionMessage29"));
		} else {
			jTextAreaMessages.setText(message);
		}
		//
		Rectangle screenRect = dialog_.getSession().getMenuRectangle();
		int posX = (screenRect.width - (int)this.getPreferredSize().getWidth()) / 2 + screenRect.x;
		int posY = (screenRect.height - (int)this.getPreferredSize().getHeight()) / 2 + screenRect.y;
		this.setLocation(posX, posY);
		//
		this.setVisible(true);
		//
		return keyMap_;
	}
}

class XF300_TreeNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 1L;
	private String nodeText_;
	private Icon nodeIcon_;
	private HashMap<String, Object> keyMap_ = null;
	private XF300 frame_;
	public XF300_TreeNode(HashMap<String, Object> keyMap, String nodeText, Icon nodeIcon, XF300 frame) {
		super();
		keyMap_ = keyMap;
		nodeText_ = nodeText;
		nodeIcon_ = nodeIcon;
		frame_ = frame;
	}
	public Icon getIcon() {
		return nodeIcon_;
	}
	public String toString() {
		return nodeText_;
	}
	public HashMap<String, Object> getKeyMap() {
		return keyMap_;
	}
	public void setupChildNode() {
		XF300_TreeNode childNode;
		XFTableOperator structureTableOp, headerTableOp; 
		String sql = frame_.getStructureTable().getSelectSQL(keyMap_);
		structureTableOp = frame_.createTableOperator(sql);
		try {
			while (structureTableOp.next()) {
				sql = frame_.getStructureTable().getSQLForNodeInfo(structureTableOp);
				headerTableOp = frame_.createTableOperator(sql);
				if (headerTableOp.next()) {
					childNode = frame_.getStructureTable().createTreeNode(headerTableOp, structureTableOp);
					childNode.add(new XF300_TreeNode(null, "Dummy", null, frame_));
					this.add(childNode);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class XF300_TreeRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 1L;
	public Component getTreeCellRendererComponent(JTree tree, Object treeNode, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		Component renderer = super.getTreeCellRendererComponent(tree,treeNode,selected,expanded,leaf,row,hasFocus);
		if (hasFocus) {
			this.setBackgroundSelectionColor(XFUtility.SELECTED_ACTIVE_COLOR);
		} else {
			this.setBackgroundSelectionColor(Color.LIGHT_GRAY);
			this.setForeground(Color.black);
		}
		this.setText(((XF300_TreeNode)treeNode).toString());
		this.setIcon(((XF300_TreeNode)treeNode).getIcon());
		return renderer;
	}
}


class XF300_jTabbedPane_keyAdapter extends java.awt.event.KeyAdapter {
	XF300 adaptee;
	XF300_jTabbedPane_keyAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void keyPressed(KeyEvent e) {
		adaptee.jTabbedPane_keyPressed(e);
	}
}

class XF300_jTabbedPane_changeAdapter  implements ChangeListener {
	XF300 adaptee;
	XF300_jTabbedPane_changeAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void stateChanged(ChangeEvent e) {
		adaptee.jTabbedPane_stateChanged(e);
	}
}

class XF300_jTableMain_keyAdapter extends java.awt.event.KeyAdapter {
	XF300 adaptee;
	XF300_jTableMain_keyAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void keyPressed(KeyEvent e) {
		adaptee.jTableMain_keyPressed(e);
	}
}

class XF300_jTableMain_focusAdapter extends java.awt.event.FocusAdapter {
	XF300 adaptee;
	XF300_jTableMain_focusAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void focusGained(FocusEvent e) {
		adaptee.jTableMain_focusGained(e);
	}
	public void focusLost(FocusEvent e) {
		adaptee.jTableMain_focusLost(e);
	}
}

class XF300_jTableMain_mouseAdapter extends java.awt.event.MouseAdapter {
	XF300 adaptee;
	XF300_jTableMain_mouseAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void mouseClicked(MouseEvent e) {
		adaptee.jTableMain_mouseClicked(e);
	}
}

class XF300_jScrollPaneTable_mouseAdapter extends java.awt.event.MouseAdapter {
	XF300 adaptee;
	XF300_jScrollPaneTable_mouseAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void mousePressed(MouseEvent e) {
		adaptee.jScrollPaneTable_mousePressed(e);
	}
}

class XF300_FunctionButton_actionAdapter implements java.awt.event.ActionListener {
	XF300 adaptee;
	XF300_FunctionButton_actionAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jFunctionButton_actionPerformed(e);
	}
}

class XF300_WindowAdapter extends java.awt.event.WindowAdapter {
	XF300 adaptee;
	XF300_WindowAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void windowGainedFocus(WindowEvent e) {
		adaptee.windowGainedFocus(e);
	}
}

class XF300_keyAdapter extends java.awt.event.KeyAdapter {
	XF300 adaptee;
	XF300_keyAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void keyPressed(KeyEvent e) {
		adaptee.dialog_keyPressed(e);
	}
}

class XF300_jButtonList_actionAdapter implements java.awt.event.ActionListener {
	XF300 adaptee;
	XF300_jButtonList_actionAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.selectDetailRecordsAndSetupTableRows(adaptee.getSelectedTabIndex(), true);
	}
}

class XF300_jTree_mouseAdapter extends java.awt.event.MouseAdapter {
	XF300 adaptee;
	XF300_jTree_mouseAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void mousePressed(MouseEvent e) {
		adaptee.jTree_mousePressed(e);
	}
	public void mouseReleased(MouseEvent e) {
		adaptee.jTree_mouseReleased(e);
	}
}

class XF300_jTree_treeWillExpandAdapter implements javax.swing.event.TreeWillExpandListener {
	XF300 adaptee;
	XF300_jTree_treeWillExpandAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void treeWillCollapse(TreeExpansionEvent e) {
	}
	public void treeWillExpand(TreeExpansionEvent e) {
		adaptee.jTree_treeWillExpand(e);
	}
}

class XF300_jTree_keyAdapter extends java.awt.event.KeyAdapter {
	XF300 adaptee;
	XF300_jTree_keyAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void keyReleased(KeyEvent e) {
		adaptee.jTree_keyReleased(e);
	}
}

class XF300_jMenuItemTreeNodeExplosion_actionAdapter implements ActionListener {
	XF300 adaptee;
	XF300_jMenuItemTreeNodeExplosion_actionAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jMenuItemTreeNodeExplosion_actionPerformed(e);
	}
}

class XF300_jMenuItemTreeNodeImplosion_actionAdapter implements ActionListener {
	XF300 adaptee;
	XF300_jMenuItemTreeNodeImplosion_actionAdapter(XF300 adaptee) {
		this.adaptee = adaptee;
	}
	public void actionPerformed(ActionEvent e) {
		adaptee.jMenuItemTreeNodeImplosion_actionPerformed(e);
	}
}
