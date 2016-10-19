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

import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.w3c.dom.*;

public class XF300 extends JDialog implements XFExecutable, XFScriptable {
	private static final long serialVersionUID = 1L;
	private final int FIELD_HORIZONTAL_MARGIN = 1;
	private final int FIELD_VERTICAL_MARGIN = 5;

	private org.w3c.dom.Element functionElement_ = null;
	private Session session_ = null;
	private boolean instanceIsAvailable = true;
	private boolean isClosing = false;
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
	private String headerFunctionParm = "";
	private JSplitPane jSplitPaneTop = new JSplitPane();
	private JPanel jPanelTop = new JPanel();
	private JPanel jPanelCenter = new JPanel();
	private JButton jButtonList = new JButton();
	private JPanel jPanelFilters[] = new JPanel[10];
	private JScrollPane jScrollPaneFilters[] = new JScrollPane[10];
	private XF300_Filter firstEditableFilter[] = new XF300_Filter[10];
	private int rowsOfDisplayedFilters[] = new int[10];
	@SuppressWarnings("unchecked")
	private ArrayList<XF300_Filter> filterListArray[] = new ArrayList[10];
	@SuppressWarnings("unchecked")
	private ArrayList<XF300_DetailColumn>[] detailColumnListArray = new ArrayList[10];
	@SuppressWarnings("unchecked")
	private ArrayList<XF300_DetailReferTable>[] detailReferTableListArray = new ArrayList[10];
	@SuppressWarnings("unchecked")
	private ArrayList<WorkingRow>[] workingRowListArray = new ArrayList[10];
	@SuppressWarnings("unchecked")
	private HashMap<String, CompiledScript>[] compiledScriptMapArray = new HashMap[10];
	private XF300_DetailTable[] detailTableArray = new XF300_DetailTable[10];
	private String[] detailFunctionIDArray = new String[10];
	private String[] detailParmTypeArray = new String[10];
	private String[] detailParmAdditionalArray = new String[10];
	private String[] detailInitialListingArray = new String[10];
	private TableModelReadOnly[] tableModelMainArray = new TableModelReadOnly[10];
	private JTable[] jTableMainArray = new JTable[10];
	private String[] initialMsgArray = new String[10];
	private String[] listingResultMsgArray = new String[10];
	private NodeList[] detailReferElementList = new NodeList[10];
	private Bindings[] detailScriptBindingsArray = new Bindings[10];
	private TableHeadersRenderer[] headersRenderer = new TableHeadersRenderer[10];
	private TableCellsRenderer[] cellsRenderer = new TableCellsRenderer[10];
	private boolean isListingInNormalOrder[] = new boolean[10];
	private boolean isHeaderResizing = false;
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
	private JProgressBar jProgressBar = new JProgressBar();
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
	private JSplitPane jSplitPaneMain = null;
	private JSplitPane jSplitPaneCenter = null;
	private JScrollPane jScrollPaneTable = new JScrollPane();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private JButton[] jButtonArray = new JButton[7];
	private ArrayList<String> disabledButtonNumberList = new ArrayList<String>(); 
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
	private Action escapeAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		public void actionPerformed(ActionEvent e) {
			returnToMenu();
		}
	};
	private Action[] actionButtonArray = new Action[7];
	private String[] actionDefinitionArray = new String[7];
	private String actionF6 = "";
	private String actionF8 = "";
	private ScriptEngine scriptEngine;
	private Bindings headerScriptBindings;
	private String scriptNameRunning = "";
	private ByteArrayOutputStream exceptionLog;
	private PrintStream exceptionStream;
	private String exceptionHeader = "";
	private int evaluatingScriptTabIndex = -1;
	private int biggestWidth = 300;
	private int biggestHeight = 30;
	private HashMap<String, Object> variantMap = new HashMap<String, Object>();
	
	public XF300(Session session, int instanceArrayIndex) {
		super(session, "", true);
		session_ = session;
		instanceArrayIndex_ = instanceArrayIndex;
		initComponentsAndVariants();
	}

	void initComponentsAndVariants() {
		jPanelMain.setLayout(new BorderLayout());
		jScrollPaneHeaderFields.getViewport().add(jPanelHeaderFields, null);
		jScrollPaneHeaderFields.setBorder(null);
		jPanelHeaderFields.setLayout(null);
		jPanelHeaderFields.setFocusable(false);
		jPanelCenter.setLayout(new BorderLayout());
		jPanelCenter.setBorder(null);
		jPanelTop.setPreferredSize(new Dimension(600, 45));
		jPanelTop.setLayout(new BorderLayout());
		jButtonList.setPreferredSize(new Dimension(100, 100));
		jButtonList.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jButtonList.setText(XFUtility.RESOURCE.getString("Search"));
		jButtonList.addActionListener(new XF300_jButtonList_actionAdapter(this));
		jButtonList.addKeyListener(new XF300_keyAdapter(this));
		jPanelTop.setBorder(BorderFactory.createEtchedBorder());
		jSplitPaneTop.setOrientation(JSplitPane.VERTICAL_SPLIT);

		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jTextAreaMessages.setWrapStyleWord(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jTabbedPane.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTabbedPane.addKeyListener(new XF300_jTabbedPane_keyAdapter(this));
		jTabbedPane.addChangeListener(new XF300_jTabbedPane_changeAdapter(this));

		for (int i = 0; i < 10; i++) {
			detailTableArray[i] = null;
			detailReferElementList[i] = null;
			filterListArray[i] = new ArrayList<XF300_Filter>();
			workingRowListArray[i] = new ArrayList<WorkingRow>();
			detailColumnListArray[i] = new ArrayList<XF300_DetailColumn>();
			detailReferTableListArray[i] = new ArrayList<XF300_DetailReferTable>();
			jPanelFilters[i] = new JPanel();
			jPanelFilters[i].setLayout(null);
			jScrollPaneFilters[i] = new JScrollPane();
			jScrollPaneFilters[i].getViewport().add(jPanelFilters[i]);
			jScrollPaneFilters[i].setBorder(null);
			jTableMainArray[i] = new JTable();
			jTableMainArray[i].setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
			jTableMainArray[i].setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			jTableMainArray[i].setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTableMainArray[i].setSelectionBackground(selectionColorWithoutFocus);
			jTableMainArray[i].setSelectionForeground(Color.black);
			jTableMainArray[i].addKeyListener(new XF300_jTableMain_keyAdapter(this));
			jTableMainArray[i].addMouseListener(new XF300_jTableMain_mouseAdapter(this));
			jTableMainArray[i].addFocusListener(new XF300_jTableMain_focusAdapter(this));
			JTableHeader header = new JTableHeader(jTableMainArray[i].getColumnModel()) {
				private static final long serialVersionUID = 1L;
				public String getToolTipText(MouseEvent e) {
					return headersRenderer[jTabbedPane.getSelectedIndex()].getToolTipText(e);
				}
			};
			header.setResizingAllowed(false);
			header.setReorderingAllowed(false);
			header.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (headersRenderer[jTabbedPane.getSelectedIndex()].hasMouseOnColumnBorder(e.getX(), e.getY())) {
						isHeaderResizing = true;
						headersRenderer[jTabbedPane.getSelectedIndex()].setSizingHeader(e.getX());
					} else {
						headersRenderer[jTabbedPane.getSelectedIndex()].resortRowsByColumnAt(e.getX(), e.getY());
					}
				}
				public void mouseReleased(MouseEvent e) {
					if (isHeaderResizing) {
						headersRenderer[jTabbedPane.getSelectedIndex()].setNewBoundsToHeaders(e.getX());
						TableColumn column = jTableMainArray[jTabbedPane.getSelectedIndex()].getColumnModel().getColumn(0);
						column.setPreferredWidth(headersRenderer[jTabbedPane.getSelectedIndex()].getWidth());
						cellsRenderer[jTabbedPane.getSelectedIndex()].setupCellBounds();
						jScrollPaneTable.updateUI();
					}
					isHeaderResizing = false;
				}
				public void mouseExited(MouseEvent e) {
					setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					headersRenderer[jTabbedPane.getSelectedIndex()].resetUnderlineOfColumns();
				}
			});
			header.addMouseMotionListener(new MouseMotionAdapter() {
				public void mouseMoved(MouseEvent e) {
					if (headersRenderer[jTabbedPane.getSelectedIndex()].hasMouseOnColumnBorder(e.getX(), e.getY())) {
						setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
					} else {
						setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
					headersRenderer[jTabbedPane.getSelectedIndex()].setUnderlineOnColumnAt(e.getX(), e.getY());
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
					g2.fillRect(pointX,0,3,jTableMainArray[jTabbedPane.getSelectedIndex()].getHeight()+headersRenderer[jTabbedPane.getSelectedIndex()].getHeight());
					jScrollPaneTable.updateUI();
				}
			});
			jTableMainArray[i].setTableHeader(header);
		}
		jScrollPaneTable.getViewport().add(jTableMainArray[0], null);
		jScrollPaneTable.addMouseListener(new XF300_jScrollPaneTable_mouseAdapter(this));

		jPanelBottom.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setLayout(new BorderLayout());
		jPanelBottom.setBorder(null);
		jLabelFunctionID.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE-2));
		jLabelFunctionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelFunctionID.setForeground(Color.gray);
		jLabelFunctionID.setFocusable(false);
		jLabelSessionID.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE-2));
		jLabelSessionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelSessionID.setForeground(Color.gray);
		jLabelSessionID.setFocusable(false);
		jProgressBar.setStringPainted(true);
		jPanelButtons.setBorder(null);
		jPanelButtons.setLayout(gridLayoutButtons);
		jPanelButtons.setFocusable(false);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
		jPanelInfo.setLayout(gridLayoutInfo);
		jPanelInfo.add(jLabelSessionID);
		jPanelInfo.add(jLabelFunctionID);
		jPanelInfo.setFocusable(false);
		gridLayoutButtons.setColumns(7);
		gridLayoutButtons.setRows(1);
		for (int i = 0; i < 7; i++) {
			jButtonArray[i] = new JButton();
			jButtonArray[i].setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
			jButtonArray[i].setFocusable(false);
			jButtonArray[i].addActionListener(new XF300_FunctionButton_actionAdapter(this));
			jPanelButtonArray[i] = new JPanel();
			jPanelButtonArray[i].setLayout(new BorderLayout());
			jPanelButtonArray[i].add(jButtonArray[i], BorderLayout.CENTER);
			actionButtonArray[i] = new ButtonAction(jButtonArray[i]);
			jPanelButtons.add(jPanelButtonArray[i]);
		}
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);

		jScrollPaneTreeView.getViewport().add(jTree);
		jTree.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTree.setRowHeight(XFUtility.ROW_UNIT_HEIGHT);
		jTree.addKeyListener(new XF300_jTree_keyAdapter(this));
		jTree.addMouseListener(new XF300_jTree_mouseAdapter(this));
	    jTree.addTreeWillExpandListener(new XF300_jTree_treeWillExpandAdapter(this));
		jTree.setCellRenderer((TreeCellRenderer)treeRenderer);
	    jTree.setModel(treeModel);
		jSplitPaneTreeView.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		jSplitPaneTreeView.setFocusable(false);
		jLabelTree.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jLabelTree.setPreferredSize(new Dimension(100, 20));
		jPanelTreeView.setLayout(new BorderLayout());
		jPanelTreeView.add(jLabelTree, BorderLayout.NORTH);
		jPanelTreeView.add(jScrollPaneTreeView, BorderLayout.CENTER);
		jMenuItemTreeNodeExplosion.setText(XFUtility.RESOURCE.getString("ExplosionAsRoot"));
		jMenuItemTreeNodeExplosion.addActionListener(new XF300_jMenuItemTreeNodeExplosion_actionAdapter(this));
		jMenuItemTreeNodeImplosion.setText(XFUtility.RESOURCE.getString("ImplosionAsRoot"));
		jMenuItemTreeNodeImplosion.addActionListener(new XF300_jMenuItemTreeNodeImplosion_actionAdapter(this));
		jPopupMenuTreeNode.add(jMenuItemTreeNodeExplosion);
		jPopupMenuTreeNode.add(jMenuItemTreeNodeImplosion);

		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);
		this.addWindowFocusListener(new XF300_WindowAdapter(this));
		this.addKeyListener(new XF300_keyAdapter(this));
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

			///////////////////////////
			// Initializing variants //
			///////////////////////////
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
			Arrays.fill(isListingInNormalOrder, true);
			
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
			for (int i = 0; i < detailTabSortingList.getSize(); i++) {
				for (int j = 0; j < filterListArray[i].size(); j++) {
					filterListArray[i].get(j).setValue(filterListArray[i].get(j).getDefaultValue());
					if (!filterListArray[i].get(j).isValidatedWithParmMapValue(parmMap_)) {
						JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError47") + filterListArray[i].get(j).getCaption() + XFUtility.RESOURCE.getString("FunctionError48"));
						isClosing = true;
						break;
					}
				}
			}

			//////////////////////////////////////
			// Set size of components on screen //
			//////////////////////////////////////
			Rectangle screenRect = session_.getMenuRectangle();
			if (functionElement_.getAttribute("Size").equals("")) {
				this.setPreferredSize(new Dimension(screenRect.width, screenRect.height));
				this.setLocation(screenRect.x, screenRect.y);
			} else {
				if (functionElement_.getAttribute("Size").equals("AUTO")) {
					int strViewWidth = 0;
					int posX = 0;
					int posY = 0;
					if (!functionElement_.getAttribute("StructureTable").equals("")) {
						strViewWidth = Integer.parseInt(functionElement_.getAttribute("StructureViewWidth"));
					}
					int workWidth = biggestWidth + 50 + strViewWidth;
					if (workWidth < 1000) {
						workWidth = 1000;
					}
					if (workWidth > screenRect.width) {
						workWidth = screenRect.width;
						posX = screenRect.x;
					} else {
						posX = ((screenRect.width - workWidth) / 2) + screenRect.x;
						if (posX > 100) {
							posX = posX - 10;
						}
					}
					int workHeight = biggestHeight + 500;
					if (workHeight > (screenRect.height - 60)) {
						workHeight = screenRect.height - 60;
						posY = screenRect.y + 30;
					} else {
						posY = ((screenRect.height - workHeight) / 2) + screenRect.y;
					}
					this.setPreferredSize(new Dimension(workWidth, workHeight));
					this.setLocation(posX, posY);
				} else {
					StringTokenizer workTokenizer = new StringTokenizer(functionElement_.getAttribute("Size"), ";" );
					int width = Integer.parseInt(workTokenizer.nextToken());
					int height = Integer.parseInt(workTokenizer.nextToken());
					this.setPreferredSize(new Dimension(width, height));
					int posX = ((screenRect.width - width) / 2) + screenRect.x;
					int posY = ((screenRect.height - height) / 2) + screenRect.y;
					this.setLocation(posX, posY);
				}
			}
			if (detailFunctionIDArray[0].equals("") && jTableMainArray[0].isFocusable()) {
				returnMap_.put("RETURN_CODE", "01");
			}
			jPanelHeaderFields.setPreferredSize(new Dimension(biggestWidth, biggestHeight));
			jSplitPaneCenter.setDividerLocation(biggestHeight + this.FIELD_VERTICAL_MARGIN + 13);
			jSplitPaneCenter.updateUI();
			jSplitPaneMain.setDividerLocation(this.getPreferredSize().height - 150);
			jSplitPaneMain.updateUI();
			jPanelMain.updateUI();
			this.pack();

			/////////////////////////
			// Fetch Header Record //
			/////////////////////////
			fetchHeaderRecord(false);
			if (!isClosing) {

				/////////////////////////////////////////////////
				// Select Detail Tab Records and Setup JTables //
				/////////////////////////////////////////////////
				for (int i = 0; i < detailTabSortingList.getSize(); i++) {
					listingResultMsgArray[i] = "";
					if (jTabbedPane.isEnabledAt(i)
							&& (filterListArray[i].size() == 0
							|| firstEditableFilter[i] == null
							|| detailInitialListingArray[i].equals("T"))) {
						if (i==0) {
							selectDetailRecordsAndSetupTableRows(i, true);
						} else {
							clearTableRows(i);
						}
					} else {
						clearTableRows(i);
					}
				}

				//////////////////////////////////////////////
				// Set first tab to be selected and focused //
				//////////////////////////////////////////////
				if (jTabbedPane.getTabCount() == 0) {
					jPanelBottom.add(jPanelDummy, BorderLayout.CENTER);
					jTextAreaMessages.setText(XFUtility.RESOURCE.getString("FunctionMessage30"));
				} else {
					jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
					for (int i = 0; i < detailTabSortingList.getSize(); i++) {
						if (jTabbedPane.isEnabledAt(i)) {
							jTabbedPane.setSelectedIndex(i);
							break;
						}
					}
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
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		return returnMap_;
	}

	public void setFunctionSpecifications(org.w3c.dom.Element functionElement) throws Exception {
		org.w3c.dom.Element workElement;
		String workStr, workAlias, workTableID, workFieldID;
		StringTokenizer workTokenizer;
		org.w3c.dom.Element fieldElement;

		///////////////////////////////
		// Initialize basic variants //
		///////////////////////////////
		isClosing = false;
		functionElement_ = functionElement;
		keyInputDialog = null;

		////////////////////////////////////////////////
		// Setup SplitPane / These steps are required //
		// as its divider location can't be altered.  //
		////////////////////////////////////////////////
		jSplitPaneCenter = new JSplitPane();
		jSplitPaneCenter.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPaneCenter.add(jScrollPaneHeaderFields, JSplitPane.TOP);
		jSplitPaneCenter.add(jTabbedPane, JSplitPane.BOTTOM);
		jSplitPaneCenter.setFocusable(false);
		jSplitPaneMain = new JSplitPane();
		jSplitPaneMain.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPaneMain.add(jSplitPaneCenter, JSplitPane.TOP);
		jSplitPaneMain.add(jScrollPaneMessages, JSplitPane.BOTTOM);
		jSplitPaneMain.setFocusable(false);

		//////////////////////////////
		// Set panel configurations //
		//////////////////////////////
		jLabelSessionID.setText(session_.getSessionID());
		if (instanceArrayIndex_ >= 0) {
			jLabelFunctionID.setText("300" + "-" + instanceArrayIndex_ + "-" + functionElement_.getAttribute("ID"));
		} else {
			jLabelFunctionID.setText("300" + "-" + functionElement_.getAttribute("ID"));
		}
		FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
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
			jSplitPaneTreeView.updateUI();
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
		headerFunctionParm = functionElement_.getAttribute("HeaderParmAdditional");

		/////////////////////////////////////////////////
		// Setup Header Fields and Fetch Header Record //
		/////////////////////////////////////////////////
		jPanelHeaderFields.removeAll();
		headerFieldList.clear();
		Dimension dimOfPriviousField = new Dimension(0,0);
		Dimension dim = new Dimension(0,0);
		int posX = 0;
		int posY = 0;
		biggestWidth = 300;
		biggestHeight = 30;
		boolean firstVisibleField = true;
		NodeList headerFieldElementList = functionElement_.getElementsByTagName("Field");
		workSortingList = XFUtility.getSortedListModel(headerFieldElementList, "Order");
		for (int i = 0; i < workSortingList.getSize(); i++) {
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
				if (posX + dim.width > biggestWidth) {
					biggestWidth = posX + dim.width;
				}
				if (posY + dim.height > biggestHeight) {
					biggestHeight = posY + dim.height;
				}
				if (headerFieldList.get(i).isHorizontal()) {
					dimOfPriviousField = new Dimension(dim.width, XFUtility.FIELD_UNIT_HEIGHT);
				} else {
					dimOfPriviousField = new Dimension(dim.width, dim.height);
				}
			}
		}

		/////////////////////////////////////////////
		// Add primary table keys as HIDDEN fields //
		/////////////////////////////////////////////
		for (int i = 0; i < headerTable_.getKeyFieldIDList().size(); i++) {
			if (!containsHeaderField(headerTable_.getTableID(), "", headerTable_.getKeyFieldIDList().get(i))) {
				headerFieldList.add(new XF300_HeaderField(headerTable_.getTableID(), "", headerTable_.getKeyFieldIDList().get(i), this));
			}
		}

		/////////////////////////////////////////////////////////////
		// Analyze fields in script and add them as HIDDEN columns //
		/////////////////////////////////////////////////////////////
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
							String msg = XFUtility.RESOURCE.getString("FunctionError1") + headerTable_.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + headerTable_.getScriptList().get(i).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
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

		//////////////////////////////////////////////////////////////////
		// Add BYTEA-type-field for BYTEA field as HIDDEN header fields //
		//////////////////////////////////////////////////////////////////
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (headerFieldList.get(i).getBasicType().equals("BYTEA") && !headerFieldList.get(i).getByteaTypeFieldID().equals("")) {
				if (!containsHeaderField(headerTable_.getTableID(), "", headerFieldList.get(i).getByteaTypeFieldID())) {
					headerFieldList.add(new XF300_HeaderField(headerTable_.getTableID(), "", headerFieldList.get(i).getByteaTypeFieldID(), this));
				}
			}
		}

		//////////////////////////////////////////////////////////////////////////////
		// Analyze header refer tables and add their fields as HIDDEN header fields //
		//////////////////////////////////////////////////////////////////////////////
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

		////////////////////////////////////////////////////////
		// Add detail fields on header table as HIDDEN fields //
		////////////////////////////////////////////////////////
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

		jPanelHeaderFields.setPreferredSize(new Dimension(biggestWidth, biggestHeight));
		jSplitPaneCenter.setDividerLocation(biggestHeight + this.FIELD_VERTICAL_MARGIN + 13);
		jSplitPaneMain.setDividerLocation(this.getPreferredSize().height - 125);
		this.pack();

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
			if (firstTab) {
				jTabbedPane.addTab(workElement.getAttribute("Caption"), null, jPanelCenter);
				firstTab = false;
			} else {
				jTabbedPane.addTab(workElement.getAttribute("Caption"), null, null);
			}
			detailTableArray[i] = new XF300_DetailTable(workElement, i, this);
			detailReferTableListArray[i].clear();
			detailReferElementList[i] = detailTableArray[i].getTableElement().getElementsByTagName("Refer");
			workSortingList = XFUtility.getSortedListModel(detailReferElementList[i], "Order");
			for (int j = 0; j < workSortingList.getSize(); j++) {
				detailReferTableListArray[i].add(new XF300_DetailReferTable((org.w3c.dom.Element)workSortingList.getElementAt(j), i, this));
			}
			initialMsgArray[i] = workElement.getAttribute("InitialMsg");
			compiledScriptMapArray[i] = new HashMap<String, CompiledScript>();

			////////////////////////////////////////////
			// Add header table keys as HIDDEN fields //
			////////////////////////////////////////////
			for (int j = 0; j < detailTableArray[i].getHeaderKeyFieldIDList().size(); j++) {
				if (!containsHeaderField(headerTable_.getTableID(), "", detailTableArray[i].getHeaderKeyFieldIDList().get(j))) {
					headerFieldList.add(new XF300_HeaderField(headerTable_.getTableID(), "", detailTableArray[i].getHeaderKeyFieldIDList().get(j), this));
				}
			}

			/////////////////////////
			// Setup Filter fields //
			/////////////////////////
			XF300_Filter filter;
			int filtersWidth = 0;
			posX = 0;
			posY = 8;
			int wrkInt = 0;
			rowsOfDisplayedFilters[i] = 0;
			dimOfPriviousField = new Dimension(0,0);
			filterListArray[i].clear();
			jPanelFilters[i].removeAll();
			firstEditableFilter[i] = null;
			NodeList filterFieldList = workElement.getElementsByTagName("Filter");
			workSortingList = XFUtility.getSortedListModel(filterFieldList, "Order");
			for (int j = 0; j < workSortingList.getSize(); j++) {
				filter = new XF300_Filter((org.w3c.dom.Element)workSortingList.getElementAt(j), this, i);
				filterListArray[i].add(filter);
				if (!filter.isHidden()) {
					jPanelFilters[i].add(filter);
					if (filter.isEditable() && firstEditableFilter[i] == null) {
						firstEditableFilter[i] = filter;
					}
					if (wrkInt == 0) {
						rowsOfDisplayedFilters[i]++;
					} else {
						if (filter.isVerticalPosition()) {
							posX = 0;
							posY = posY + dimOfPriviousField.height + filter.getVerticalMargin();
							rowsOfDisplayedFilters[i]++;
						} else {
							posX = posX + dimOfPriviousField.width;
						}
					}

					dim = filter.getPreferredSize();
					filter.setBounds(posX, posY, dim.width, dim.height);
					if (posX + dim.width > filtersWidth) {
						filtersWidth = posX + dim.width;
					}
					dimOfPriviousField = new Dimension(dim.width, dim.height);

					wrkInt++;
				}
			}
			jPanelFilters[i].setPreferredSize(new Dimension(filtersWidth, posY + dimOfPriviousField.height));

			///////////////////////////////////////////
			// Setup components for JTable of Detail //
			///////////////////////////////////////////
			tableModelMainArray[i] = new TableModelReadOnly();
			jTableMainArray[i].setModel(tableModelMainArray[i]);
			detailColumnListArray[i].clear();

			///////////////////////////////
			// Add Column on table model //
			///////////////////////////////
			int columnIndex = 0;
			columnFieldList = workElement.getElementsByTagName("Column");
			workSortingList = XFUtility.getSortedListModel(columnFieldList, "Order");
			for (int j = 0; j < workSortingList.getSize(); j++) {
				detailColumnListArray[i].add(new XF300_DetailColumn(workElement.getAttribute("Table"), (org.w3c.dom.Element)workSortingList.getElementAt(j), this, i));
				if (detailColumnListArray[i].get(j).isVisibleOnPanel()) {
					columnIndex++;
					detailColumnListArray[i].get(j).setColumnIndex(columnIndex);
				}
			}
			headersRenderer[i] = new TableHeadersRenderer(i, this); 
			cellsRenderer[i] = new TableCellsRenderer(headersRenderer[i]); 
			jTableMainArray[i].setRowHeight(headersRenderer[i].getHeight());
			tableModelMainArray[i].addColumn(""); //column index:0 //
			TableColumn column = jTableMainArray[i].getColumnModel().getColumn(0);
			column.setHeaderRenderer(headersRenderer[i]);
			column.setCellRenderer(cellsRenderer[i]);
			column.setPreferredWidth(headersRenderer[i].getWidth());

			//////////////////////////////////////////////////
			// Add detail table key fields as HIDDEN column //
			//////////////////////////////////////////////////
			for (int j = 0; j < detailTableArray[i].getKeyFieldIDList().size(); j++) {
				if (!containsDetailField(i, detailTableArray[i].getTableID(), "", detailTableArray[i].getKeyFieldIDList().get(j))) {
					detailColumnListArray[i].add(new XF300_DetailColumn(detailTableArray[i].getTableID(), detailTableArray[i].getTableID(), "", detailTableArray[i].getKeyFieldIDList().get(j), this, i));
				}
			}

			/////////////////////////////////////////
			// Add OrderBy fields as HIDDEN column //
			/////////////////////////////////////////
			ArrayList<String> orderByFieldList = detailTableArray[i].getOrderByFieldIDList(true);
			for (int j = 0; j < orderByFieldList.size(); j++) {
				workStr = orderByFieldList.get(j).replace("(D)", "");
				workStr = workStr.replace("(A)", "");
				workTokenizer = new StringTokenizer(workStr, "." );
				workAlias = workTokenizer.nextToken();
				workTableID = getTableIDOfTableAlias(workAlias, i);
				workFieldID = workTokenizer.nextToken();
				if (!containsDetailField(i, workTableID, workAlias, workFieldID)) {
					detailColumnListArray[i].add(new XF300_DetailColumn(detailTableArray[i].getTableID(), workTableID, workAlias, workFieldID, this, i));
				}
			}

			/////////////////////////////////////////
			// Add filter fields as HIDDEN columns //
			/////////////////////////////////////////
			for (int j = 0; j < filterListArray[i].size(); j++) {
				if (!containsDetailField(i, filterListArray[i].get(j).getTableID(), filterListArray[i].get(j).getTableAlias(), filterListArray[i].get(j).getFieldID())) {
					detailColumnListArray[i].add(new XF300_DetailColumn(detailTableArray[i].getTableID(), filterListArray[i].get(j).getTableID(), filterListArray[i].get(j).getTableAlias(), filterListArray[i].get(j).getFieldID(), this, i));
				}
			}

			/////////////////////////////////////////////////////////////
			// Analyze fields in script and add them as HIDDEN columns //
			/////////////////////////////////////////////////////////////
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
								String msg = XFUtility.RESOURCE.getString("FunctionError1") + detailTableArray[i].getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + detailTableArray[i].getScriptList().get(j).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
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

			////////////////////////////////////////////////////////////
			// Add BYTEA-type-field for BYTEA field as HIDDEN columns //
			////////////////////////////////////////////////////////////
			for (int j = 0; j < detailColumnListArray[i].size(); j++) {
				if (detailColumnListArray[i].get(j).getBasicType().equals("BYTEA") && !detailColumnListArray[i].get(j).getByteaTypeFieldID().equals("")) {
					if (!containsDetailField(i, detailTableArray[i].getTableID(), "", detailColumnListArray[i].get(j).getByteaTypeFieldID())) {
						detailColumnListArray[i].add(new XF300_DetailColumn(detailTableArray[i].getTableID(), detailTableArray[i].getTableID(), "", detailColumnListArray[i].get(j).getByteaTypeFieldID(), this, i));
					}
				}
			}

			//////////////////////////////////////////////////////////////////////////////
			// Analyze detail refer tables and add their fields as HIDDEN column fields //
			//////////////////////////////////////////////////////////////////////////////
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
			detailParmAdditionalArray[i] = workElement.getAttribute("ParmAdditional");
			detailInitialListingArray[i] = workElement.getAttribute("InitialListing");
			detailFunctionIDArray[i] = workElement.getAttribute("DetailFunction");
			if (detailFunctionIDArray[i].equals("NONE")) {
				jTableMainArray[i].setRowSelectionAllowed(false);
			} else {
				jTableMainArray[i].setRowSelectionAllowed(true);
			}
		}
		

		//////////////////////////////////////
		// Setup Script Engine and Bindings //
		//////////////////////////////////////
		scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
		headerScriptBindings = scriptEngine.createBindings();
		headerScriptBindings.put("instance", (XFScriptable)this);
		for (int i = 0; i < headerFieldList.size(); i++) {
			headerScriptBindings.put(headerFieldList.get(i).getDataSourceID(), headerFieldList.get(i));
		}
		for (int i = 0; i < detailTabSortingList.getSize(); i++) {
			detailScriptBindingsArray[i] = scriptEngine.createBindings();
			detailScriptBindingsArray[i].putAll(headerScriptBindings);
			for (int j = 0; j < detailColumnListArray[i].size(); j++) {
				detailScriptBindingsArray[i].put(detailColumnListArray[i].get(j).getDataSourceID(), detailColumnListArray[i].get(j));
			}
		}

		tablesReadyToUse = true;
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

	public void setErrorAndCloseFunction() {
		returnMap_.put("RETURN_CODE", "99");
		this.rollback();
		closeFunction();
	}

	void returnToMenu() {
		returnMap_.put("RETURN_TO", "MENU");
		closeFunction();
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
		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), processLog.toString(), errorLog);
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		this.setVisible(false);
	}
	
	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(null, message);
		}
		returnMap_.put("RETURN_CODE", "01");
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
						isClosing = true;
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
						if (headerFieldList.get(i).getTableAlias().equals(headerTable_.getTableID())) {
							headerFieldList.get(i).setValueOfResultSet(headerTableOp);
						}
					}
					headerTable_.runScript("AR", "BR()"); /* Script to be run AFETR READ primary table*/
				} else {
					if (keyInputRequired) {
						inputDialogMessage = XFUtility.RESOURCE.getString("FunctionError37");
					} else {
						if (!cancelWithoutMessage) {
							JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError38"));
						}
						isClosing = true;
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
					if (referTableOp.next()) {
						for (int j = 0; j < headerFieldList.size(); j++) {
							if (headerFieldList.get(j).getTableAlias().equals(headerReferTableList.get(i).getTableAlias())) {
								headerFieldList.get(j).setValueOfResultSet(referTableOp);
							}
						}
						headerTable_.runScript("AR", "AR(" + headerReferTableList.get(i).getTableAlias() + ")"); /* Script to be run AFTER READ */
					}
				}
			}

			headerTable_.runScript("AR", "AR()"); /* Script to be run AFTER READ */

			for (int i = 0; i < headerFieldList.size(); i++) {
				if (headerFieldList.get(i).getBasicType().equals("BYTEA")) {
					headerFieldList.get(i).setupByteaTypeField(headerFieldList);
				}
			}

			////////////////////////////
			// Set Detail Tab Enabled //
			////////////////////////////
			for (int i = 0; i < detailTabSortingList.getSize(); i++) {
				jTabbedPane.setEnabledAt(i, true);
			}
			if (parmMap_.get("DISABLED_TAB_LIST") != null) {
				String wrkStr = parmMap_.get("DISABLED_TAB_LIST").toString();
				StringTokenizer tokenizer = new StringTokenizer(wrkStr, ",");
				while (tokenizer.hasMoreTokens()) {
					wrkStr = tokenizer.nextToken();
					jTabbedPane.setEnabledAt(Integer.parseInt(wrkStr), false);
				}
				parmMap_.remove("DISABLED_TAB_LIST");
			}

			/////////////////////////////////
			// Set Function Button Enabled //
			/////////////////////////////////
			disabledButtonNumberList.clear();
			if (parmMap_.get("DISABLED_BUTTON_LIST") != null) {
				String wrkStr = parmMap_.get("DISABLED_BUTTON_LIST").toString();
				StringTokenizer tokenizer = new StringTokenizer(wrkStr, ",");
				while (tokenizer.hasMoreTokens()) {
					wrkStr = tokenizer.nextToken();
					disabledButtonNumberList.add(wrkStr);
				}
				parmMap_.remove("DISABLED_BUTTON_LIST");
			}

		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
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
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != InputEvent.BUTTON1_MASK) {
			int row = jTree.getRowForLocation(e.getX(),e.getY());
			if (row > -1) {
				Component com = (Component)e.getSource();
				jPopupMenuTreeNode.show(com, e.getX() , e.getY());
			}
		}
	}
	
	void setupHeaderDetailsForTreeNodeSelected() {
		XF300_TreeNode node;
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			TreePath tp = jTree.getSelectionPath();
			node = (XF300_TreeNode)tp.getLastPathComponent();
			if (node != currentTreeNode) {
				currentTreeNode = node;
				parmMap_ = currentTreeNode.getKeyMap();

				/////////////////////////
				// Fetch Header Record //
				/////////////////////////
				fetchHeaderRecord(true);

				/////////////////////////////////////////////
				// Select Detail records and setup JTables //
				/////////////////////////////////////////////
				for (int i = 0; i < detailTabSortingList.getSize(); i++) {
					listingResultMsgArray[i] = "";
					if (filterListArray[i].size() == 0
							|| firstEditableFilter[i] == null
							|| detailInitialListingArray[i].equals("T")) {
						selectDetailRecordsAndSetupTableRows(i, false);
					} else {
						clearTableRows(i);
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
		InputMap inputMap  = jPanelMain.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		inputMap.clear();
		ActionMap actionMap = jPanelMain.getActionMap();
		actionMap.clear();
		for (int i = 0; i < 7; i++) {
			jButtonArray[i].setText("");
			jButtonArray[i].setVisible(false);
			actionDefinitionArray[i] = "";
		}
		actionF6 = "";
		actionF8 = "";

		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "HELP");
		actionMap.put("HELP", helpAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_T,ActionEvent.CTRL_MASK), "FOCUS_TAB");
		actionMap.put("FOCUS_TAB", focusTabAction);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
		actionMap.put("ESCAPE", escapeAction);

		int workIndex;
		org.w3c.dom.Element element;
		NodeList buttonList = detailTableArray[tabIndex].getDetailTableElement().getElementsByTagName("Button");
		for (int i = 0; i < buttonList.getLength(); i++) {
			element = (org.w3c.dom.Element)buttonList.item(i);
			workIndex = Integer.parseInt(element.getAttribute("Position"));
			actionDefinitionArray[workIndex] = element.getAttribute("Action");
			XFUtility.setCaptionToButton(jButtonArray[workIndex], element, "", this.getPreferredSize().width / 8, session_);
			jButtonArray[workIndex].setVisible(true);
			if (element.getAttribute("Action").equals("ADD")) {
				String functionName = session_.getFunctionName(detailFunctionIDArray[tabIndex]);
				if (!functionName.equals("")) {
					jButtonArray[workIndex].setToolTipText(detailFunctionIDArray[tabIndex] + " " + functionName);
				}
			}
			if (element.getAttribute("Action").equals("HEADER")) {
				String functionName = session_.getFunctionName(headerFunctionID);
				if (!functionName.equals("")) {
					jButtonArray[workIndex].setToolTipText(headerFunctionID + " " + functionName);
				}
			}
			jButtonArray[workIndex].setEnabled(true);
			if (disabledButtonNumberList.contains(element.getAttribute("Number"))) {
				jButtonArray[workIndex].setEnabled(false);
			}
			inputMap.put(XFUtility.getKeyStroke(element.getAttribute("Number")), "actionButton" + workIndex);
			actionMap.put("actionButton" + workIndex, actionButtonArray[workIndex]);
			if (element.getAttribute("Number").equals("6")) {
				actionF6 = element.getAttribute("Position");
			}
			if (element.getAttribute("Number").equals("8")) {
				actionF8 = element.getAttribute("Position");
			}
		}
	}
	
	void clearTableRows(int index) {
		int rowCount = tableModelMainArray[index].getRowCount();
		for (int i = 0; i < rowCount; i++) {
			tableModelMainArray[index].removeRow(0);
		}
		workingRowListArray[index].clear();
		headersRenderer[index].clearSortingColumn();
		jScrollPaneTable.updateUI();
	}

	void selectDetailRecordsAndSetupTableRows(int index, boolean isToOverrideMessage) {
		HashMap<String, Object> keyMap;
		HashMap<String, Object> columnMap;
		ArrayList<String> orderByFieldList;
		ArrayList<String> orderByFieldTypeList;
		ArrayList<Object> orderByValueList;
		ArrayList<TableCellReadOnly> cellObjectList;
		String workStr, sql;
		boolean readyToValidate;
		boolean toBeSelected;
		XFTableOperator detailTableOp, referTableOp;
 
		try {
			clearTableRows(index);
			referOperatorList.clear();

			detailTableArray[index].runScript(index, "BR", ""); /* Script to be run BEFORE READ */

			int countOfRows = 0;
			detailTableOp = createTableOperator(detailTableArray[index].getSelectSQL());
			while (detailTableOp.next()) {

				for (int i = 0; i < detailColumnListArray[index].size(); i++) {
					detailColumnListArray[index].get(i).setReadyToValidate(false);
					detailColumnListArray[index].get(i).initialize();
				}

				for (int i = 0; i < detailColumnListArray[index].size(); i++) {
					if (detailColumnListArray[index].get(i).getTableAlias().equals(detailTableArray[index].getTableID())) {
						readyToValidate = detailColumnListArray[index].get(i).setValueOfResultSet(detailTableOp);
						detailColumnListArray[index].get(i).setReadyToValidate(readyToValidate);
					}
				}

				detailTableArray[index].runScript(index, "AR", "BR()"); /* Script to be run AFTER READ primary table */

				if (isTheRowToBeSelected(index)) {
					toBeSelected = true;
					for (int i = 0; i < detailReferTableListArray[index].size(); i++) {
						if (detailReferTableListArray[index].get(i).isToBeExecuted()) {

							detailTableArray[index].runScript(index, "AR", "BR(" + detailReferTableListArray[index].get(i).getTableAlias() + ")"); /* Script to be run BEFORE READ */

							sql = detailReferTableListArray[index].get(i).getSelectSQL();
							if (!sql.equals("")) {
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
								if (referTableOp.next()) {
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
							if (!toBeSelected) {
								break;
							}
						}
					}

					detailTableArray[index].runScript(index, "AR", "AR()"); /* Script to be run AFTER READ */

					if (toBeSelected) {
						for (int i = 0; i < detailColumnListArray[index].size(); i++) {
							detailColumnListArray[index].get(i).setReadyToValidate(true);
							if (detailColumnListArray[index].get(i).getBasicType().equals("BYTEA")) {
								detailColumnListArray[index].get(i).setByteaType(detailColumnListArray[index]);
							}
						}
						if (isTheRowToBeSelected(index)) {
							keyMap = new HashMap<String, Object>();
							for (int i = 0; i < detailTableArray[index].getKeyFieldIDList().size(); i++) {
								keyMap.put(detailTableArray[index].getKeyFieldIDList().get(i), detailTableOp.getValueOf(detailTableArray[index].getKeyFieldIDList().get(i)));
							}

							columnMap = new HashMap<String, Object>();
							cellObjectList = new ArrayList<TableCellReadOnly>();
							for (int i = 0; i < detailColumnListArray[index].size(); i++) {
								columnMap.put(detailColumnListArray[index].get(i).getDataSourceName(), detailColumnListArray[index].get(i).getInternalValue());
								cellObjectList.add(detailColumnListArray[index].get(i).getCellObject());
							}

							if (detailTableArray[index].hasOrderByAsItsOwnFields()) {
								Object[] cell = new Object[1];
								cell[0] = new XF300_DetailRowNumber(countOfRows + 1, keyMap, columnMap, cellObjectList);
								tableModelMainArray[index].addRow(cell);
							} else {
								orderByFieldList = detailTableArray[index].getOrderByFieldIDList(isListingInNormalOrder[index]);
								orderByFieldTypeList = new ArrayList<String>();
								orderByValueList = new ArrayList<Object>();
								for (int i = 0; i < orderByFieldList.size(); i++) {
									workStr = orderByFieldList.get(i).replace("(D)", "");
									workStr = workStr.replace("(A)", "");
									for (int j = 0; j < detailColumnListArray[index].size(); j++) {
										if (detailColumnListArray[index].get(j).getDataSourceName().equals(workStr)) {
											orderByFieldTypeList.add(detailColumnListArray[index].get(j).getBasicType());
											orderByValueList.add(detailColumnListArray[index].get(j).getExternalValue());
											break;
										}
									}
								}
								workingRowListArray[index].add(new WorkingRow(index, cellObjectList, keyMap, columnMap, orderByValueList, orderByFieldTypeList));
							}
							countOfRows++;
						}
					}
				}
			}

			headerTable_.runScript("AR", "AS()"); /* Script to be run AFTER READ and AFTER SUMMARY */

			if (!detailTableArray[index].hasOrderByAsItsOwnFields()) {
				WorkingRow[] workingRowArray = workingRowListArray[index].toArray(new WorkingRow[0]);
				Arrays.sort(workingRowArray);
				for (int i = 0; i < workingRowArray.length; i++) {
					Object[] cell = new Object[1];
					cell[0] = new XF300_DetailRowNumber(i + 1, workingRowArray[i].getKeyMap(), workingRowArray[i].getColumnMap(), workingRowArray[i].getCellObjectList());
					tableModelMainArray[index].addRow(cell);
				}
			}

			if (isToOverrideMessage) {
				messageList.clear();
				if (countOfRows > 0) {
					jTableMainArray[index].setRowSelectionInterval(0, 0);
					jTableMainArray[index].requestFocus();
					if (detailFunctionIDArray[index].equals("NONE")) {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage57"));
					} else {
						if (detailFunctionIDArray[index].equals("")) {
							messageList.add(XFUtility.RESOURCE.getString("FunctionMessage2"));
						} else {
							messageList.add(XFUtility.RESOURCE.getString("FunctionMessage3"));
						}
					}
				} else {
					if (filterListArray[index].size() > 0 && firstEditableFilter[index] != null) {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage4"));
					} else {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage31"));
					}
				}
				listingResultMsgArray[index] = messageList.get(0);
				setMessagesOnPanel();
			}

		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		}
	}
	
	public void resortTableRowsByColumnIndex(int tabIndex, int col, boolean isAscending) {
		String workStr;
		ArrayList<Object> orderByValueList;
		ArrayList<String> orderByFieldTypeList;
		ArrayList<String> orderByFieldList = detailTableArray[tabIndex].getOrderByFieldIDList(isListingInNormalOrder[tabIndex]);
		workStr = detailColumnListArray[tabIndex].get(col).getDataSourceName();
		if (!isAscending) {
			workStr = workStr + "(D)";
		}
		orderByFieldList.add(0, workStr);
		
		workingRowListArray[tabIndex].clear();
		int rowCount = tableModelMainArray[tabIndex].getRowCount();
		for (int i = 0; i < rowCount; i++) {
			orderByValueList =new ArrayList<Object>();
			orderByFieldTypeList =new ArrayList<String>();
			XF300_DetailRowNumber cell = (XF300_DetailRowNumber)tableModelMainArray[tabIndex].getValueAt(i, 0);
			for (int j = 0; j < orderByFieldList.size(); j++) {
				workStr = orderByFieldList.get(j).replace("(D)", "");
				workStr = workStr.replace("(A)", "");
				for (int k = 0; k < detailColumnListArray[tabIndex].size(); k++) {
					if (detailColumnListArray[tabIndex].get(k).getDataSourceName().equals(workStr)) {
						TableCellReadOnly cellObject = cell.getCellObjectList().get(k);
						orderByValueList.add(cellObject.getInternalValue());
						orderByFieldTypeList.add(detailColumnListArray[tabIndex].get(k).getBasicType());
						break;
					}
				}
			}
			workingRowListArray[tabIndex].add(new WorkingRow(tabIndex, cell.getCellObjectList(), cell.getKeyMap(), cell.getColumnMap(), orderByValueList, orderByFieldTypeList));
		}

		for (int i = 0; i < rowCount; i++) {
			tableModelMainArray[tabIndex].removeRow(0);
		}

		WorkingRow[] workingRowArray = workingRowListArray[tabIndex].toArray(new WorkingRow[0]);
		Arrays.sort(workingRowArray);
		for (int i = 0; i < workingRowArray.length; i++) {
			Object[] cell = new Object[1];
			cell[0] = new XF300_DetailRowNumber(i + 1, workingRowArray[i].getKeyMap(), workingRowArray[i].getColumnMap(), workingRowArray[i].getCellObjectList());
			tableModelMainArray[tabIndex].addRow(cell);
		}

		orderByFieldList.remove(0);
	}

	void setupTreeNodes() {
		XFTableOperator headerTableOp, structureTableOp;
		XF300_TreeNode rootNode, childNode;
		String sql;
		currentTreeNode = null;
		try {
			////////////////////////////////
			// Set Root Node to TreeModel //
			////////////////////////////////
			structureTable_ = new XF300_StructureTable(functionElement_, this);
			sql = structureTable_.getSQLForNodeInfo(parmMap_);
			headerTableOp = createTableOperator(sql);
			if (headerTableOp.next()) {
				rootNode = structureTable_.createTreeNode(headerTableOp);
				treeModel = new DefaultTreeModel(rootNode);
				currentTreeNode = rootNode;

				/////////////////////
				// Add Child Nodes //
				/////////////////////
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

		    jTree.setModel(treeModel);
		    jTree.expandRow(0);
		    jTree.setSelectionRow(0);

			if (isForExplosion) {
				jLabelTree.setText(" " + structureTable_.getName());
			} else {
				jLabelTree.setText(" " + structureTable_.getName() + " - " + XFUtility.RESOURCE.getString("Implosion"));
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
			messageList.clear();

			if (action.equals("EXIT")) {
				closeFunction();
			}

			if (action.equals("HEADER")) {
				try {
					HashMap<String, Object> workMap = new HashMap<String, Object>();
					workMap.putAll(parmMap_);
					workMap.remove("INSTANCE_MODE");
					StringTokenizer workTokenizer1, workTokenizer2;
					String parmName, parmValue;
					workTokenizer1 = new StringTokenizer(headerFunctionParm, ";" );
					while (workTokenizer1.hasMoreTokens()) {
						workTokenizer2 = new StringTokenizer(workTokenizer1.nextToken(), ":" );
						if (workTokenizer2.countTokens() == 2) {
							parmName = workTokenizer2.nextToken();
							parmValue = workTokenizer2.nextToken();
							workMap.put(parmName, parmValue);
						}
					}
//					returnMap = session_.executeFunction(headerFunctionID, parmMap_);
					returnMap = session_.executeFunction(headerFunctionID, workMap);
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
								if (filterListArray[i].size() == 0 || firstEditableFilter[i] == null) {
									selectDetailRecordsAndSetupTableRows(i, false);
								}
							}
							if (!functionElement_.getAttribute("StructureTable").equals("")) {
								setupTreeNodes();
							}
							returnMap_.put("RETURN_CODE", returnMap.get("RETURN_CODE"));
						}
					}
					if (returnMap.get("RETURN_TO") != null && returnMap.get("RETURN_TO").equals("MENU")) {
						returnToMenu();
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
					messageList.add(XFUtility.RESOURCE.getString("FunctionError15"));
				}
			}

			if (action.equals("ADD")) {
				try {
					returnMap = session_.executeFunction(detailFunctionIDArray[jTabbedPane.getSelectedIndex()], parmMap_);
					if (returnMap.get("RETURN_MESSAGE") == null) {
						messageList.add(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
					} else {
						messageList.add(returnMap.get("RETURN_MESSAGE").toString());
					}
					if (returnMap.get("RETURN_CODE").equals("10")) {
						if (filterListArray[jTabbedPane.getSelectedIndex()].size() == 0 || firstEditableFilter[jTabbedPane.getSelectedIndex()] == null) {
							selectDetailRecordsAndSetupTableRows(jTabbedPane.getSelectedIndex(), false);
						}
						returnMap_.put("RETURN_CODE", returnMap.get("RETURN_CODE"));
					}
					if (returnMap.get("RETURN_TO") != null && returnMap.get("RETURN_TO").equals("MENU")) {
						returnToMenu();
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
					messageList.add(XFUtility.RESOURCE.getString("FunctionError15"));
				}
			}

			if (action.equals("OUTPUT")) {
				session_.browseFile(getExcellBookURI());
			}

			pos1 = action.indexOf("CALL(", 0);
			if (pos1 >= 0) {
				pos2 = action.indexOf(")", pos1);
				String functionAndParms = action.substring(pos1+5, pos2);
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
//					returnMap = session_.executeFunction(functionID, parmMap_);
					returnMap = session_.executeFunction(functionID, workMap);
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
								if (filterListArray[i].size() == 0
										|| firstEditableFilter[i] == null
										|| detailInitialListingArray[i].equals("T")) {
									selectDetailRecordsAndSetupTableRows(i, false);
								}
							}
							returnMap_.put("RETURN_CODE", returnMap.get("RETURN_CODE"));
						}
					}
					if (returnMap.get("RETURN_TO") != null && returnMap.get("RETURN_TO").equals("MENU")) {
						returnToMenu();
					}
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
					messageList.add(XFUtility.RESOURCE.getString("FunctionError15"));
				}
			}

			setMessagesOnPanel();

		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	class TableHeadersRenderer extends JPanel implements TableCellRenderer {   
		private static final long serialVersionUID = 1L;
		private JLabel numberLabel = new JLabel("No.");
		private JPanel centerPanel = new JPanel();
		private ArrayList<JLabel> headerList = new ArrayList<JLabel>();
		private int totalWidthOfCenterPanel = 0;
		private int totalHeight = 0;
		private ArrayList<XF300_DetailColumn> columnList_;
		private Component sizingHeader = null;
		private JLabel sortingColumn = null;
		private boolean isAscendingColumnSorting = true;
		private XF300 dialog_;
		private int tabIndex_;

		public TableHeadersRenderer(int tabIndex, XF300 dialog) {
			tabIndex_ = tabIndex;
			dialog_ = dialog;
			columnList_ = dialog_.getDetailColumnList(tabIndex_);
			arrangeColumnsPosition(true);
			centerPanel.setLayout(null);
			numberLabel.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
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
		
		public boolean hasMouseOnColumnBorder(int headerPosX, int headerPosY) {
			boolean result = false;
			double posX = headerPosX - numberLabel.getBounds().getWidth();
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
			double posX = headerPosX - numberLabel.getBounds().getWidth();
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
					dialog_.resortTableRowsByColumnIndex(tabIndex_, i, isAscendingColumnSorting);
				}
			}
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
							columnList_.get(i).setWidth(newWidth);
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
			for (int i = 0; i < columnList_.size(); i++) {
				if (columnList_.get(i).isVisibleOnPanel()) {
					header = new JLabel();
					header.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
					if (columnList_.get(i).getValueType().equals("IMAGE")
							|| columnList_.get(i).getValueType().equals("FLAG")) {
						header.setHorizontalAlignment(SwingConstants.CENTER);
					} else {
						if (columnList_.get(i).getBasicType().equals("INTEGER")
								|| columnList_.get(i).getBasicType().equals("FLOAT")) {
							header.setHorizontalAlignment(SwingConstants.RIGHT);
						} else {
							header.setHorizontalAlignment(SwingConstants.LEFT);
						}
					}
					header.setText(columnList_.get(i).getCaption());
					header.setOpaque(true);

					width = columnList_.get(i).getWidth();
					height = XFUtility.ROW_UNIT_HEIGHT * columnList_.get(i).getRows();
					if (i > 0) {
						fromX = headerList.get(i-1).getBounds().x + headerList.get(i-1).getBounds().width;
						fromY = headerList.get(i-1).getBounds().y + headerList.get(i-1).getBounds().height;
						for (int j = i-1; j >= 0; j--) {
							if (columnList_.get(i).getLayout().equals("VERTICAL")) {
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
			if (e.getPoint().x > numberLabel.getPreferredSize().width) {
				Component compo = centerPanel.getComponentAt(e.getPoint().x-numberLabel.getPreferredSize().width, e.getPoint().y);
				if (compo != null) {
					for (int i = 0; i < headerList.size(); i++) {
						if (compo.equals(headerList.get(i))) {
							if (columnList_.get(i).getDecimalSize() > 0) {
								text = "<html>" + columnList_.get(i).getName() + " " + columnList_.get(i).getDataSourceName() + " (" + columnList_.get(i).getDataSize() + "," + columnList_.get(i).getDecimalSize() + ")<br>" + columnList_.get(i).getFieldRemarks();
							} else {
								text = "<html>" + columnList_.get(i).getName() + " " + columnList_.get(i).getDataSourceName() + " (" + columnList_.get(i).getDataSize() + ")<br>" + columnList_.get(i).getFieldRemarks();
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
			XF300_DetailRowNumber rowObject = (XF300_DetailRowNumber)value;
			numberCell.setText(rowObject.getRowNumberString());
			ArrayList<XF300_DetailColumn> columnList = detailColumnListArray[jTabbedPane.getSelectedIndex()];
			for (int i = 0; i < cellList.size(); i++) {
				cellList.get(i).setEnabled(columnList.get(i).isEnabled());
				if (columnList.get(i).getValueType().equals("IMAGE")
						|| columnList.get(i).getValueType().equals("FLAG")) {
					cellList.get(i).setIcon((Icon)rowObject.getCellObjectList().get(i).getExternalValue());
				} else {
					cellList.get(i).setText((String)rowObject.getCellObjectList().get(i).getExternalValue());
					if (isSelected) {
						if (rowObject.getCellObjectList().get(i).getColor().equals(Color.black)) {
							cellList.get(i).setForeground(table.getSelectionForeground());
						} else {
							if (rowObject.getCellObjectList().get(i).getColor().equals(Color.blue)) {
								cellList.get(i).setForeground(Color.cyan);
							} else {
								cellList.get(i).setForeground(rowObject.getCellObjectList().get(i).getColor());
							}
						}
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
				cell.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
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
			this.setPreferredSize(new Dimension(totalWidth + headersRenderer_.getSequenceWidth(), totalHeight));
		}
	} 

	 class WorkingRow extends Object implements Comparable {
		private ArrayList<TableCellReadOnly> cellObjectList_ = null;
		private ArrayList<Object> orderByValueList_ = new ArrayList<Object>();
		private ArrayList<String> orderByFieldTypeList_ = null;
		private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
		private HashMap<String, Object> columnMap_ = new HashMap<String, Object>();
		private int index_;
		public WorkingRow(int index, ArrayList<TableCellReadOnly> cellObjectList, HashMap<String, Object> keyMap, HashMap<String, Object> columnMap, ArrayList<Object> orderByValueList, ArrayList<String> orderByFieldTypeList) {
			index_ = index;
			cellObjectList_ = cellObjectList;
			keyMap_ = keyMap;
			columnMap_ = columnMap;
			orderByValueList_ = orderByValueList;
			orderByFieldTypeList_ = orderByFieldTypeList;
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
			ArrayList<String> orderByFieldList = detailTableArray[index_].getOrderByFieldIDList(isListingInNormalOrder[index_]);
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
		 TableCellReadOnly cellObject = null;
		 XF300_DetailRowNumber rowObject;
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
		 styleDataInteger.setFont(fontDefault);
		 styleDataInteger.setDataFormat(format.getFormat("#,##0"));

		 int currentRowNumber = -1;
		 int mergeRowNumberFrom = -1;

		 try {
			 xlsFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".xlsx");
			 xlsFileName = xlsFile.getPath();
			 fileOutputStream = new FileOutputStream(xlsFileName);

			 ////////////////////////
			 // Header field lines //
			 ////////////////////////
			 for (int i = 0; i < headerFieldList.size(); i++) {
				 if (headerFieldList.get(i).isVisibleOnPanel()) {
					 for (int j = 0; j < headerFieldList.get(i).getRows(); j++) {
						 currentRowNumber++;
						 XSSFRow rowData = workSheet.createRow(currentRowNumber);

						 /////////////////////////////////
						 // Cell for header field label //
						 /////////////////////////////////
						 XSSFCell cellHeader = rowData.createCell(0);
						 cellHeader.setCellStyle(styleHeaderLabel);
						 if (j==0) {
							 mergeRowNumberFrom = currentRowNumber;
							 if (!headerFieldList.get(i).getFieldOptionList().contains("NO_CAPTION")) {
								 cellHeader.setCellValue(new XSSFRichTextString(headerFieldList.get(i).getCaption()));
							 }
						 }
						 rowData.createCell(1).setCellStyle(styleHeaderLabel);

						 ////////////////////////////////
						 // Cell for header field data //
						 ////////////////////////////////
						 setupCellAttributesForHeaderField(rowData, workBook, workSheet, headerFieldList.get(i), currentRowNumber, j);
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

			 ////////////////////////////////
			 // Detail column heading line //
			 ////////////////////////////////
			 currentRowNumber++;
			 XSSFRow rowCaption = workSheet.createRow(currentRowNumber);
			 XSSFCell cell = rowCaption.createCell(0);
			 cell.setCellStyle(styleDetailNumberLabel);
			 workSheet.setColumnWidth(0, headersRenderer[jTabbedPane.getSelectedIndex()].getSequenceWidth() * 40);
			 wrkStr = XFUtility.getCaptionForCell(headersRenderer[jTabbedPane.getSelectedIndex()].getSequenceLabel());
			 cell.setCellValue(new XSSFRichTextString(wrkStr));
			 ArrayList<XF300_DetailColumn> columnList = detailColumnListArray[jTabbedPane.getSelectedIndex()];
			 for (int j = 0; j < columnList.size(); j++) {
				 if (columnList.get(j).isVisibleOnPanel()) {
					 cell = rowCaption.createCell(j+1);
					 if (columnList.get(j).getBasicType().equals("INTEGER")
							 || columnList.get(j).getBasicType().equals("FLOAT")) {
						 if (columnList.get(j).getTypeOptionList().contains("MSEQ") || columnList.get(j).getTypeOptionList().contains("FYEAR")) {
							 cell.setCellStyle(styleDetailLabel);
						 } else {
							 cell.setCellStyle(styleDetailNumberLabel);
						 }
					 } else {
						 cell.setCellStyle(styleDetailLabel);
					 }
					 Rectangle rect = headersRenderer[jTabbedPane.getSelectedIndex()].getColumnHeaderList().get(j).getBounds();
					 workSheet.setColumnWidth(j+1, rect.width * 30);
					 wrkStr = XFUtility.getCaptionForCell(headersRenderer[jTabbedPane.getSelectedIndex()].getColumnHeaderList().get(j).getText());
					 wrkStr = wrkStr.replaceAll("<html>" , "");
					 wrkStr = wrkStr.replaceAll("<u>" , "");
					 cell.setCellValue(new XSSFRichTextString(wrkStr));
				 } else {
					 break;
				 }
			 }

			 //////////////////////////////
			 // Detail column data lines //
			 //////////////////////////////
			 for (int i = 0; i < tableModelMainArray[jTabbedPane.getSelectedIndex()].getRowCount(); i++) {
				 currentRowNumber++;
				 XSSFRow rowData = workSheet.createRow(currentRowNumber);
				 cell = rowData.createCell(0); //Column of Sequence Number
				 cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
				 cell.setCellStyle(styleDataInteger);
				 cell.setCellValue(i + 1);
				 rowObject = (XF300_DetailRowNumber)tableModelMainArray[jTabbedPane.getSelectedIndex()].getValueAt(i,0);
				 for (int j = 0; j < detailColumnListArray[jTabbedPane.getSelectedIndex()].size(); j++) {
					 if (detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).isVisibleOnPanel()) {
						 cellObject = (TableCellReadOnly)rowObject.getCellObjectList().get(j);
						 setupCellAttributesForDetailColumn(rowData.createCell(j+1), workBook, detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).getBasicType(), detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).getTypeOptionList(), cellObject, detailColumnListArray[jTabbedPane.getSelectedIndex()].get(j).getDecimalSize());
						 if (cellObject.getValueType().equals("IMAGE") && !cellObject.getInternalValue().equals("")) {
							 imageFileName = session_.getImageFileFolder() + cellObject.getInternalValue();
							 XFUtility.setupImageCell(workBook, workSheet, currentRowNumber, currentRowNumber+1, j+1, j+2, imageFileName);
						 }
					 }
				 }
			 }
			 workBook.write(fileOutputStream);
			 messageList.add(XFUtility.RESOURCE.getString("XLSComment1"));

		 } catch (Exception e) {
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

	 private void setupCellAttributesForHeaderField(XSSFRow rowData, XSSFWorkbook workBook, XSSFSheet workSheet, XF300_HeaderField object, int currentRowNumber, int rowIndexInCell) {
		 String wrk;

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

		 String basicType = object.getBasicType();
		 if (basicType.equals("INTEGER")) {
			 if (object.getTypeOptionList().contains("MSEQ") || object.getTypeOptionList().contains("FYEAR")) {
				 cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
				 cellValue.setCellStyle(style);
				 cellValue.setCellValue(new XSSFRichTextString((String)object.getExternalValue()));
			 } else {
				 style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
				 style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
				 wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
				 if (wrk.equals("")) {
					 cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
					 cellValue.setCellStyle(style);
					 cellValue.setCellValue(new XSSFRichTextString(object.getExternalValue().toString()));
				 } else {
					 if (!object.getTypeOptionList().contains("NO_EDIT")
							 && !object.getTypeOptionList().contains("ZERO_SUPPRESS")) {
						 style.setDataFormat(format.getFormat("#,##0"));
					 }
					 cellValue.setCellStyle(style);
					 if (rowIndexInCell==0) {
						 if (wrk.equals("") || object.getTypeOptionList().contains("NO_EDIT")) {
							 cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
							 cellValue.setCellValue(new XSSFRichTextString(wrk));
						 } else {
							 cellValue.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
							 cellValue.setCellValue(Double.parseDouble(wrk));
						 }
					 }
				 }
			 }
		 } else {
			 if (basicType.equals("FLOAT")) {
				 style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
				 style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
				 wrk = XFUtility.getStringNumber(object.getExternalValue().toString());
				 if (wrk.equals("")) {
					 cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
					 cellValue.setCellStyle(style);
					 if (rowIndexInCell==0) {
						 cellValue.setCellValue(new XSSFRichTextString(""));
					 }
				 } else {
					 if (!object.getTypeOptionList().contains("NO_EDIT")
							 && !object.getTypeOptionList().contains("ZERO_SUPPRESS")) {
						 style.setDataFormat(XFUtility.getFloatFormat(workBook, object.getDecimalSize()));
					 }
					 cellValue.setCellStyle(style);
					 if (rowIndexInCell==0) {
						 if (wrk.equals("") || object.getTypeOptionList().contains("NO_EDIT")) {
							 cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
							 cellValue.setCellValue(new XSSFRichTextString(wrk));
						 } else {
							 cellValue.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
							 cellValue.setCellValue(Double.parseDouble(wrk));
						 }
					 }
				 }
			 } else {
				 cellValue.setCellType(XSSFCell.CELL_TYPE_STRING);
				 cellValue.setCellStyle(style);
				 if (rowIndexInCell==0) {
					 if (basicType.equals("STRING")) {
						 if (object.isImage()) {
							 style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
							 style.setVerticalAlignment(XSSFCellStyle.VERTICAL_BOTTOM);
							 style.setDataFormat(format.getFormat("text"));
							 cellValue.setCellStyle(style);
							 cellValue.setCellValue(new XSSFRichTextString((String)object.getInternalValue()));
							 try {
								 //XFUtility.setupImageCellForField(workBook, workSheet, 2, currentRowNumber, 4, object.getRows(), (String)object.getExternalValue(), patriarch);
								 XFUtility.setupImageCell(workBook, workSheet, currentRowNumber, currentRowNumber + object.getRows(), 2, 7,(String)object.getExternalValue());
							 } catch(Exception e) {
								 e.printStackTrace(exceptionStream);
							 }
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

	 private void setupCellAttributesForDetailColumn(XSSFCell cell, XSSFWorkbook workBook, String basicType, ArrayList<String> typeOptionList, TableCellReadOnly object, int decimalSize) {
		 String wrk;

		 XSSFFont font = workBook.createFont();
		 font.setFontHeightInPoints((short)11);
		 font.setFontName(session_.systemFont);
		 if (object.getColor() != Color.BLACK) {
			 font.setColor(new XSSFColor(object.getColor()));
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

		 Object value = object.getExternalValue();
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
				 style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
				 style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
				 if (value == null) {
					 wrk = "";
				 } else {
					 wrk = XFUtility.getStringNumber(value.toString());
				 }
				 if (wrk.equals("") || typeOptionList.contains("NO_EDIT")) {
					 cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					 cell.setCellStyle(style);
					 cell.setCellValue(new XSSFRichTextString(value.toString()));
				 } else {
					 cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
					 if (!typeOptionList.contains("NO_EDIT")
							 && !typeOptionList.contains("ZERO_SUPPRESS")) {
						 style.setDataFormat(format.getFormat("#,##0"));
					 }
					 cell.setCellStyle(style);
					 cell.setCellValue(Double.parseDouble(wrk));
				 }
			 }
		 } else {
			 if (basicType.equals("FLOAT")) {
				 style.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
				 style.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);
				 if (value == null) {
					 wrk = "";
				 } else {
					 wrk = XFUtility.getStringNumber(value.toString());
				 }
				 if (wrk.equals("") || typeOptionList.contains("NO_EDIT")) {
					 cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					 cell.setCellStyle(style);
					 cell.setCellValue(new XSSFRichTextString(wrk));
				 } else {
					 cell.setCellType(XSSFCell.CELL_TYPE_NUMERIC);
					 if (!typeOptionList.contains("NO_EDIT")
							 && !typeOptionList.contains("ZERO_SUPPRESS")) {
						 style.setDataFormat(XFUtility.getFloatFormat(workBook, decimalSize));
					 }
					 cell.setCellStyle(style);
					 cell.setCellValue(Double.parseDouble(wrk));
				 }
			 } else {
				 cell.setCellType(XSSFCell.CELL_TYPE_STRING);
				 cell.setCellStyle(style);
				 if (value == null || object.getValueType().equals("IMAGE")) {
					 wrk = "";
				 } else {
					 if (object.getValueType().equals("FLAG")) {
						 wrk = object.getInternalValue().toString();
					 } else {
						 wrk = value.toString();
					 }
				 }
				 cell.setCellValue(new XSSFRichTextString(wrk));
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
		//////////////////////////////////////////////
		// Steps to control focus from tab to table //
		//////////////////////////////////////////////
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			jTableMainArray[jTabbedPane.getSelectedIndex()].requestFocus();
			if (jTableMainArray[jTabbedPane.getSelectedIndex()].getRowCount() > 0) {
				jTableMainArray[jTabbedPane.getSelectedIndex()].setRowSelectionInterval(0, 0);
			}
		}

		//////////////////////////////////////
		// Steps to control listing request //
		//////////////////////////////////////
		if (e.getKeyCode() == KeyEvent.VK_L) {
			if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
				if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
					isListingInNormalOrder[jTabbedPane.getSelectedIndex()] = false;
				} else {
					isListingInNormalOrder[jTabbedPane.getSelectedIndex()] = true;
				}
				jButtonList.doClick();
			}
		}
		
		///////////////////////////////////////////
		// Steps to hack F6 and F8 of jSplitPane //
		///////////////////////////////////////////
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
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			int index = jTabbedPane.getSelectedIndex();
			jPanelCenter.removeAll();
			jPanelTop.removeAll();
			jPanelTop.setVisible(false);
			if (filterListArray[index].size() > 0) {
				jPanelTop.setVisible(true);
				jPanelTop.add(jScrollPaneFilters[index], BorderLayout.CENTER);
				if (firstEditableFilter[index] != null) {
					jPanelTop.add(jButtonList, BorderLayout.EAST);
					firstEditableFilter[index].requestFocus();
				}
				jSplitPaneTop.add(jPanelTop, JSplitPane.TOP);
				jSplitPaneTop.add(jScrollPaneTable, JSplitPane.BOTTOM);
				jSplitPaneTop.setDividerLocation(30 * rowsOfDisplayedFilters[index] + 16);
				jSplitPaneTop.updateUI();
				jPanelCenter.add(jSplitPaneTop, BorderLayout.CENTER);
			} else {
				jPanelCenter.add(jScrollPaneTable, BorderLayout.CENTER);
			}
			jPanelCenter.updateUI();
			jScrollPaneTable.getViewport().add(jTableMainArray[index], null);
			messageList.clear();
			if (jTableMainArray[index].getRowCount() > 0) {
				if (initialMsgArray[index].equals("")) {
					if (detailFunctionIDArray[index].equals("NONE")) {
						messageList.add(XFUtility.RESOURCE.getString("FunctionMessage57"));
					} else {
						if (detailFunctionIDArray[index].equals("")) {
							messageList.add(XFUtility.RESOURCE.getString("FunctionMessage2"));
						} else {
							messageList.add(XFUtility.RESOURCE.getString("FunctionMessage3"));
						}
					}
				} else {
					messageList.add(initialMsgArray[index]);
				}
			} else {
				if (index == 0) {
					if (initialMsgArray[index].equals("")) {
						if (listingResultMsgArray[index].equals("")) {
							if (filterListArray[index].size() > 0 && firstEditableFilter[index] != null) {
								StringBuffer buf = new StringBuffer();
								buf.append(XFUtility.RESOURCE.getString("FunctionMessage1"));
								buf.append(detailTableArray[index].getOrderByDescription());
								buf.append(XFUtility.RESOURCE.getString("FunctionMessage56"));
								messageList.add(buf.toString());
							} else {
								messageList.add(XFUtility.RESOURCE.getString("FunctionMessage31"));
							}
						} else {
							messageList.add(listingResultMsgArray[index]);
						}
					} else {
						messageList.add(initialMsgArray[index]);
					}
				} else {
					if (detailInitialListingArray[index].equals("T")) {
						selectDetailRecordsAndSetupTableRows(index, true);
					} else {
						if (initialMsgArray[index].equals("")) {
							StringBuffer buf = new StringBuffer();
							buf.append(XFUtility.RESOURCE.getString("FunctionMessage1"));
							buf.append(detailTableArray[index].getOrderByDescription());
							buf.append(XFUtility.RESOURCE.getString("FunctionMessage56"));
							messageList.add(buf.toString());
						} else {
							messageList.add(initialMsgArray[index]);
						}
					}
				}
			}
			setMessagesOnPanel();
			setupFunctionKeysAndButtonsForTabIndex(index);

			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
	
	void dialog_keyPressed(KeyEvent e) {
		//////////////////////////////////////
		// Steps to control listing request //
		//////////////////////////////////////
		if (e.getKeyCode() == KeyEvent.VK_L) {
			if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
				if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
					isListingInNormalOrder[jTabbedPane.getSelectedIndex()] = false;
				} else {
					isListingInNormalOrder[jTabbedPane.getSelectedIndex()] = true;
				}
				jButtonList.doClick();
			}
		}
	}

	void jTableMain_mouseClicked(MouseEvent e) {
		if (e.getClickCount() >= 2 && tableModelMainArray[jTabbedPane.getSelectedIndex()].getRowCount() > 0) {
			if (!detailFunctionIDArray[jTabbedPane.getSelectedIndex()].equals("NONE")) {
				processRow(false);
			}
		}
	}

	void jTableMain_keyPressed(KeyEvent e) {
		//////////////////////////////////////////////
		// Steps to control focus from table to tab //
		//////////////////////////////////////////////
		if (e.getKeyCode() == KeyEvent.VK_UP && jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow() == 0) {
			jTabbedPane.requestFocus();
		}

		//////////////////////////////////////////////
		// Steps to control selected-row processing //
		//////////////////////////////////////////////
		if (e.getKeyCode() == KeyEvent.VK_ENTER && tableModelMainArray[jTabbedPane.getSelectedIndex()].getRowCount() > 0) {
			processRow(true);
		}

		//////////////////////////////////////
		// Steps to control listing request //
		//////////////////////////////////////
		if (e.getKeyCode() == KeyEvent.VK_L) {
			if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0) {
				if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
					isListingInNormalOrder[jTabbedPane.getSelectedIndex()] = false;
				} else {
					isListingInNormalOrder[jTabbedPane.getSelectedIndex()] = true;
				}
				jButtonList.doClick();
			}
		}

		//////////////////////////////////////////
		// Steps to hack F6 and F8 of component //
		//////////////////////////////////////////
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

				int rowNumber = jTableMainArray[jTabbedPane.getSelectedIndex()].convertRowIndexToModel(jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow());
				if (rowNumber > -1) {
					XF300_DetailRowNumber detailRowNumber = (XF300_DetailRowNumber)tableModelMainArray[jTabbedPane.getSelectedIndex()].getValueAt(rowNumber,0);
					HashMap<String, Object> keyMap = detailRowNumber.getKeyMap();
					if (detailFunctionIDArray[jTabbedPane.getSelectedIndex()].equals("")) {
						if (detailParmTypeArray[jTabbedPane.getSelectedIndex()].equals("COLUMNS")) {
							keyMap = detailRowNumber.getColumnMap();
						}
						returnMap_.putAll(keyMap);
						returnMap_.put("RETURN_CODE", "00");
						closeFunction();
					} else {
						if (detailParmTypeArray[jTabbedPane.getSelectedIndex()].equals("COLUMNS")) {
							keyMap = detailRowNumber.getColumnMap();
						} else {
							if (!detailTableArray[jTabbedPane.getSelectedIndex()].getTableID().equals(headerTable_.getTableID())) {
								keyMap.putAll(parmMap_);
							}
						}
						if (!detailParmAdditionalArray[jTabbedPane.getSelectedIndex()].equals("")) {
							StringTokenizer workTokenizer1 = new StringTokenizer(detailParmAdditionalArray[jTabbedPane.getSelectedIndex()], ";" );
							while (workTokenizer1.hasMoreTokens()) {
								StringTokenizer workTokenizer2 = new StringTokenizer(workTokenizer1.nextToken(), ":" );
								if (workTokenizer2.countTokens() == 2) {
									String parmName = workTokenizer2.nextToken();
									String parmValue = workTokenizer2.nextToken();
									keyMap.put(parmName, parmValue);
								}
							}
						}
						try {
							HashMap<String, Object> returnMap = session_.executeFunction(detailFunctionIDArray[jTabbedPane.getSelectedIndex()], keyMap);
							if (returnMap.get("RETURN_CODE").equals("10")
									|| returnMap.get("RETURN_CODE").equals("20")
									|| returnMap.get("RETURN_CODE").equals("30")) {
								fetchHeaderRecord(true);
								for (int i = 0; i < detailTabSortingList.getSize(); i++) {
									selectDetailRecordsAndSetupTableRows(i, false);
								}
								messageList.clear();
							}
							if (returnMap.get("RETURN_MESSAGE") == null) {
								messageList.add(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
							} else {
								messageList.add(returnMap.get("RETURN_MESSAGE").toString());
							}
							if (returnMap.get("RETURN_TO") != null && returnMap.get("RETURN_TO").equals("MENU")) {
								returnToMenu();
							}
						} catch (Exception e) {
							messageList.add(XFUtility.RESOURCE.getString("FunctionError15") + " " + detailFunctionIDArray[jTabbedPane.getSelectedIndex()]);
						}
					}
				}
			} finally {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
		if (enterKeyPressed && jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow() >= 0) {
			if (jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow() == 0) {
				jTableMainArray[jTabbedPane.getSelectedIndex()].setRowSelectionInterval(jTableMainArray[jTabbedPane.getSelectedIndex()].getRowCount() - 1, jTableMainArray[jTabbedPane.getSelectedIndex()].getRowCount() - 1);
			} else {
				jTableMainArray[jTabbedPane.getSelectedIndex()].setRowSelectionInterval(jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow() - 1, jTableMainArray[jTabbedPane.getSelectedIndex()].getSelectedRow() - 1);
			}
		}
		setMessagesOnPanel();
	}

	void setMessagesOnPanel() {
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
	}

	public String getFunctionID() {
		if (functionElement_ == null) {
			return "";
		} else {
			return functionElement_.getAttribute("ID");
		}
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
	
	public Object getFieldObjectByID(String tableID, String fieldID) {
		String id = tableID + "_" + fieldID;
		if (evaluatingScriptTabIndex < 0) {
			if (headerScriptBindings.containsKey(id)) {
				return headerScriptBindings.get(id);
			} else {
				JOptionPane.showMessageDialog(null, "Field object " + id + " is not found.");
				return null;
			}
		} else {
			if (detailScriptBindingsArray[evaluatingScriptTabIndex].containsKey(id)) {
				return detailScriptBindingsArray[evaluatingScriptTabIndex].get(id);
			} else {
				JOptionPane.showMessageDialog(null, "Field object " + id + " is not found.");
				return null;
			}
		}
	}

	public void evalHeaderTableScript(String scriptName, String scriptText) throws ScriptException {
		if (!scriptText.equals("")) {
			scriptNameRunning = scriptName;
			StringBuffer bf = new StringBuffer();
			bf.append(scriptText);
			bf.append(session_.getScriptFunctions());
			scriptEngine.eval(bf.toString(), headerScriptBindings);
		}
	}

	public void evalDetailTableScript(String scriptName, String scriptText, int index) throws ScriptException {
		if (!scriptText.equals("")) {
			scriptNameRunning = scriptName;
			if (compiledScriptMapArray[index].containsKey(scriptNameRunning)) {
				compiledScriptMapArray[index].get(scriptNameRunning).eval(detailScriptBindingsArray[index]);
			} else {
				StringBuffer bf = new StringBuffer();
				bf.append(scriptText);
				bf.append(session_.getScriptFunctions());
				Compilable compiler = (Compilable)session_.getScriptEngineManager().getEngineByName("js");
				CompiledScript script = compiler.compile(bf.toString());
				script.eval(detailScriptBindingsArray[index]);
				compiledScriptMapArray[index].put(scriptNameRunning, script);
			}
		
		}
	}
	
	public void setEvaluatingScriptTabIndex(int index) {
		evaluatingScriptTabIndex = index;
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

	public boolean isListingInNormalOrder(int tabIndex) {
		return isListingInNormalOrder[tabIndex];
	}

	public void setListingInNormalOrder(int tabIndex, boolean isInNormalOrder) {
		isListingInNormalOrder[tabIndex] = isInNormalOrder;
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
		for (int j = 0; j < headerReferElementList.getLength(); j++) {
			workElement = (org.w3c.dom.Element)headerReferElementList.item(j);
			if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
				tableID = workElement.getAttribute("ToTable");
				break;
			}
		}
		if (index > -1) {
			for (int j = 0; j < detailReferElementList[index].getLength(); j++) {
				workElement = (org.w3c.dom.Element)detailReferElementList[index].item(j);
				if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
					tableID = workElement.getAttribute("ToTable");
					break;
				}
			}
		}
		return tableID;
	}
	
	public int getSelectedTabIndex() {
		return jTabbedPane.getSelectedIndex();
	}

	boolean isTheRowToBeSelected(int index) {
		boolean isToBeSelected = true;
		boolean isToBeSelectedInFilterGroup = false;
		String filterGroupID = "";
		for (int i = 0; i < filterListArray[index].size(); i++) {
			if (!filterListArray[index].get(i).getStringValue().equals("")) {
				if (!filterGroupID.equals(filterListArray[index].get(i).getFilterGroupID())) {
					if (!filterGroupID.equals("") && isToBeSelected) {
						isToBeSelected = isToBeSelectedInFilterGroup;
					}
					filterGroupID = filterListArray[index].get(i).getFilterGroupID();
					isToBeSelectedInFilterGroup = false;
				}
				if (filterListArray[index].get(i).isValidated()) {
					isToBeSelectedInFilterGroup = true;
				}
			}
		}
		if (!filterGroupID.equals("") && isToBeSelected) {
			isToBeSelected = isToBeSelectedInFilterGroup;
		}
		return isToBeSelected;
	}
}

class XF300_HeaderField extends JPanel implements XFFieldScriptable {
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
	private String byteaTypeFieldID = "";
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
	private boolean isEnabled = true;
	private boolean isKey = false;
	private boolean isFieldOnPrimaryTable = false;
	private boolean isVisibleOnPanel = true;
	private boolean isHorizontal = false;
	private boolean isVirtualField = false;
	private boolean isImage = false;
	private int positionMargin = 0;
	private Color foreground = Color.black;
	private XF300 dialog_;

	public XF300_HeaderField(org.w3c.dom.Element functionFieldElement, XF300 dialog){
		super();
		String wrkStr;
		dialog_ = dialog;
		functionFieldElement_ = functionFieldElement;
		fieldOptions = functionFieldElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);

		StringTokenizer workTokenizer = new StringTokenizer(functionFieldElement_.getAttribute("DataSource"), "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog.getTableIDOfTableAlias(tableAlias_, -1);
		fieldID_ =workTokenizer.nextToken();

		if (tableID_.equals(dialog_.getHeaderTable().getTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnPrimaryTable = true;
			ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
			for (int i = 0; i < keyFieldList.size(); i++) {
				if (keyFieldList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
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
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();

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

		if (dataType.equals("VARCHAR") || dataType.equals("LONG VARCHAR")) {
			xFTextArea = new XFTextArea("", fieldOptions, dialog_.getSession().systemFont);
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
					xFUrlField = new XFUrlField(dataSize, fieldOptions, dialog_.getSession().systemFont);
					xFUrlField.setLocation(5, 0);
					xFUrlField.setEditable(false);
					component = xFUrlField;
				} else {
					if (dataTypeOptionList.contains("IMAGE")) {
						xFImageField = new XFImageField(fieldOptions, dataSize, dialog_.getSession().getImageFileFolder(), dialog_.getSession().systemFont);
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
							xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions, dialog_.getSession().systemFont);
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
										if (this.getBasicType().equals("BYTEA")) {
											component = new XFByteaField(byteaTypeFieldID, fieldOptions, dialog_.getSession());
											component.setLocation(5, 0);
										} else {
											xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions, dialog_.getSession().systemFont);
											xFTextField.setLocation(5, 0);
											xFTextField.setEditable(false);
											wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
											if (!wrkStr.equals("")) {
												String wrk;
												int fieldWidth = 0;
												FontMetrics metrics = xFTextField.getFontMetrics(xFTextField.getFont());
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
			FontMetrics metrics = jLabelFieldComment.getFontMetrics(jLabelFieldComment.getFont());
			this.setPreferredSize(new Dimension(this.getPreferredSize().width + metrics.stringWidth(wrkStr) + 5, this.getPreferredSize().height));
			this.add(jLabelFieldComment, BorderLayout.EAST);
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

	public XF300_HeaderField(String tableID, String tableAlias, String fieldID, XF300 dialog){
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

		if (tableID_.equals(dialog_.getHeaderTable().getTableID()) && tableID_.equals(tableAlias_)) {
			isFieldOnPrimaryTable = true;
			ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
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
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();

		xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions, dialog_.getSession().systemFont);
		xFTextField.setLocation(5, 0);
		xFTextField.setEditable(false);
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			String wrk;
			int fieldWidth = 50;
			FontMetrics metrics = xFTextField.getFontMetrics(xFTextField.getFont());
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

		component = xFTextField;

		fieldRows = component.getRows();
		jPanelField.setLayout(null);
		jPanelField.setPreferredSize(new Dimension(component.getWidth() + 5, component.getHeight()));
		jPanelField.add((JComponent)component);

		this.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(component.getWidth() + 160, component.getHeight()));
		this.add(jPanelField, BorderLayout.CENTER);

		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
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

	public String getByteaTypeFieldID(){
		return byteaTypeFieldID;
	}

	public String getDataSourceName(){
		return tableAlias_ + "." + fieldID_;
	}

	public String getDataSourceID(){
		return tableAlias_ + "_" + fieldID_;
	}

	public void setValueList(String[] valueList) {
	}

	public String[] getValueList() {
		return new String[0];
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
			if (!this.isVirtualField) {
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
						if (this.getBasicType().equals("BYTEA")) {
							component.setValue(operator.getValueOf(this.getFieldID()));
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
		String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			returnObj = keyValue;
		} else {
			if (this.getBasicType().equals("BYTEA")) {
				returnObj = component.getInternalValue();
			} else {
				returnObj = (String)component.getInternalValue();
			}
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
		}
		return returnObj;
	}

	public void setOldValue(Object object){
	}

	public Object getOldValue() {
		return getInternalValue();
	}
	
	public void setupByteaTypeField(ArrayList<XF300_HeaderField> fieldList) {
		if (isVisibleOnPanel) {
			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).getFieldID().equals(byteaTypeFieldID)) {
					((XFByteaField)component).setTypeField((XFFieldScriptable)fieldList.get(i));
					break;
				}
			}
		}
	}
	
	public boolean isValueChanged() {
		return !this.getValue().equals(this.getOldValue());
	}

	public void setEditable(boolean editable){
		component.setEditable(editable);
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled){
		this.isEnabled = isEnabled;
		jLabelField.setEnabled(isEnabled);
		component.setEnabled(isEnabled);
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

	public Color getForeground() {
		return foreground;
	}
}

class XF300_DetailRowNumber extends Object {
	private int number_;
	private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
	private HashMap<String, Object> columnMap_ = new HashMap<String, Object>();
	private ArrayList<TableCellReadOnly> cellObjectList_;
	public XF300_DetailRowNumber(int num, HashMap<String, Object> keyMap, HashMap<String, Object> columnMap, ArrayList<TableCellReadOnly> cellObjectList) {
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
}

class XF300_DetailColumn implements XFFieldScriptable {
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
	private String byteaTypeFieldID = "";
	private int fieldWidth = 50;
	private int columnIndex = -1;
	private boolean isEnabled = true;
	private boolean isVisibleOnPanel = true;
	private boolean isVirtualField = false;
	private boolean isReadyToValidate = false;
	private String valueType = "STRING";
	private String flagTrue = "";
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private Object value_ = null;
	private Color foreground = Color.black;
	private int fieldRows = 1;
	private String fieldLayout = "HORIZONTAL";

	public XF300_DetailColumn(String detailTableID, org.w3c.dom.Element functionColumnElement, XF300 dialog, int index){
		super();
		String wrkStr;
		functionColumnElement_ = functionColumnElement;
		dialog_ = dialog;
		fieldOptions = functionColumnElement_.getAttribute("FieldOptions");

		StringTokenizer workTokenizer = new StringTokenizer(functionColumnElement_.getAttribute("DataSource"), "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog.getTableIDOfTableAlias(tableAlias_, index);
		fieldID_ =workTokenizer.nextToken();

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

	public XF300_DetailColumn(String detailTableID, String tableID, String tableAlias, String fieldID, XF300 dialog, int index){
		super();
		functionColumnElement_ = null;
		dialog_ = dialog;
		fieldOptions = "";
		isVisibleOnPanel = false;

		tableID_ = tableID;
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}
		fieldID_ = fieldID;

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

	public String getByteaTypeFieldID(){
		return byteaTypeFieldID;
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

	public String getDataSourceID(){
		return tableAlias_ + "_" + fieldID_;
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

	public String getCaption(){
		return fieldCaption;
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

	public boolean setValueOfResultSet(XFTableOperator operator) {
		String basicType = this.getBasicType();
		boolean isFoundInResultSet = false;
		this.setColor("");

		try {
			if (!this.isVirtualField) {
				if (basicType.equals("BYTEA")) {
					isFoundInResultSet = true; //BYTEA field is not contained intentionally in result set //
					value_ = new XFByteArray(null);
				} else {
					Object value = operator.getValueOf(this.getFieldID()); 
					isFoundInResultSet = true;
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
	
	public void setByteaType(ArrayList<XF300_DetailColumn> columnList) {
		if (isVisibleOnPanel) {
			for (int i = 0; i < columnList.size(); i++) {
				if (columnList.get(i).getFieldID().equals(byteaTypeFieldID)) {
					((XFByteArray)value_).setType(columnList.get(i).getValue().toString());
					break;
				}
			}
		}
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

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
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

	public Color getForeground() {
		return foreground;
	}
}


class XF300_Filter extends JPanel {
	private static final long serialVersionUID = 1L;
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
	private XFInputAssistField xFInputAssistField = null;
	private XFCheckBox xFCheckBox = null;
	private XFDateField xFDateField = null;
	private XFYMonthBox xFYMonthBox = null;
	private XFMSeqBox xFMSeqBox = null;
	private XFFYearBox xFFYearBox = null;
	private JComboBox jComboBox = null;
	private XF300_PromptCallField xFPromptCall = null;
	private ArrayList<String> keyValueList = new ArrayList<String>();
	private JComponent component = null;
	private boolean isVertical = false;
	private int verticalMargin = 5;
	private int horizontalMargin = 30;
	private boolean isReflect = false;
	private boolean isEditable_ = true;
	private boolean isHidden = false;
	private int index_ = 0;
	private String filterGroupID = "";

	public XF300_Filter(org.w3c.dom.Element fieldElement, XF300 dialog, int index){
		super();
		String wrkStr;
		fieldElement_ = fieldElement;
		dialog_ = dialog;
		index_ = index;

		fieldOptions = fieldElement_.getAttribute("FieldOptions");
		fieldOptionList = XFUtility.getOptionList(fieldOptions);
		StringTokenizer workTokenizer1 = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer1.nextToken();
		tableID = dialog_.getTableIDOfTableAlias(tableAlias, index);
		fieldID =workTokenizer1.nextToken();

		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID + "." + fieldID + XFUtility.RESOURCE.getString("FunctionError11"));
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
		if (operandType.equals("REFLECT")) {
			isReflect = true;
		}

		filterGroupID = tableAlias + "." + fieldID + ":" + operandType;

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = XFUtility.getCaptionValue(wrkStr, dialog_.getSession());
		}
		jLabelField = new JLabel(fieldCaption);
		jLabelField.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelField.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		int width = XFUtility.adjustFontSizeToGetPreferredWidthOfLabel(jLabelField, XFUtility.DEFAULT_LABEL_WIDTH);
		if (isVertical || dialog_.getFilterList(index_).size() == 0) {
			jLabelField.setPreferredSize(new Dimension(XFUtility.DEFAULT_LABEL_WIDTH, XFUtility.FIELD_UNIT_HEIGHT));
		} else {
			jLabelField.setPreferredSize(new Dimension(width + horizontalMargin, XFUtility.FIELD_UNIT_HEIGHT));
		}

		jPanelField.setLayout(null);
		this.setLayout(new BorderLayout());
		this.add(jLabelField, BorderLayout.WEST);
		this.add(jPanelField, BorderLayout.CENTER);

		XFTableOperator operator;

		////////////////////////////////////////////////////////////////////////////////
		// Steps to check BOOLEAN should be here because the field can be specified   //
		// as PROMPT_LIST1/2. This happens because BOOLEAN is placed recently         //
		////////////////////////////////////////////////////////////////////////////////
		if (!XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN").equals("")) {
			componentType = "BOOLEAN";
			xFCheckBox = new XFCheckBox(dataTypeOptions);
			xFCheckBox.addKeyListener(new XF300_keyAdapter(dialog));
			xFCheckBox.setLocation(5, 0);
			xFCheckBox.setEditable(true);
			component = xFCheckBox;
		} else {
			if (fieldOptionList.contains("PROMPT_LIST0")) {
				componentType = "ASSISTFIELD";
				xFInputAssistField = new XFInputAssistField(tableID, fieldID, dataSize, dataTypeOptions, dialog_.getSession());
				xFInputAssistField.addKeyListener(new XF300_keyAdapter(dialog));
				xFInputAssistField.setLocation(5, 0);
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
						jComboBox.addKeyListener(new XF300_keyAdapter(dialog));
						component = jComboBox;
						int fieldWidth = 20;

						if (fieldOptionList.contains("PROMPT_LIST1")) {
							keyValueList.add("");
							jComboBox.addItem("");
						}

						try {
							operator = dialog_.createTableOperator("Select", dialog_.getSession().getTableNameOfUserVariants());
							operator.addKeyValue("IDUSERKUBUN", wrkStr);
							operator.setOrderBy("SQLIST");
							while (operator.next()) {
								wrkKey = operator.getValueOf("KBUSERKUBUN").toString().trim();
								keyValueList.add(wrkKey);
								wrkText = operator.getValueOf("TXUSERKUBUN").toString().trim();
								jComboBox.addItem(wrkText);
								if (metrics2.stringWidth(wrkText) > fieldWidth) {
									fieldWidth = metrics2.stringWidth(wrkText);
								}
							}

							jComboBox.setBounds(new Rectangle(5, 0, fieldWidth + 30, 24));
							jComboBox.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
							jComboBox.setSelectedIndex(0);
						} catch(Exception e) {
							e.printStackTrace(dialog_.getExceptionStream());
							dialog_.setErrorAndCloseFunction();
						}

					} else {
						wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "VALUES");
						if (!wrkStr.equals("")) {
							componentType = "VALUES_LIST";
							jComboBox = new JComboBox();
							jComboBox.addKeyListener(new XF300_keyAdapter(dialog));
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

							jComboBox.setBounds(new Rectangle(5, 0, fieldWidth + 30, 24));
							jComboBox.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
							jComboBox.setSelectedIndex(0);

						} else {
							componentType = "RECORDS_LIST";
							jComboBox = new JComboBox();
							jComboBox.addKeyListener(new XF300_keyAdapter(dialog));
							component = jComboBox;
							int fieldWidth = 20;

							ArrayList<XF300_DetailReferTable> referTableList = dialog_.getDetailReferTableList(index);
							for (int i = 0; i < referTableList.size(); i++) {
								if (referTableList.get(i).getTableID().equals(tableID)) {
									if (referTableList.get(i).getTableAlias().equals("") || referTableList.get(i).getTableAlias().equals(tableAlias)) {
										if (fieldOptionList.contains("PROMPT_LIST1")) {
											jComboBox.addItem("");
										}

										try {
											operator = dialog_.createTableOperator("Select", tableID);
											operator.setSelectFields(fieldID);
											operator.setOrderBy(fieldID);
											while (operator.next()) {
												wrkKey = operator.getValueOf(fieldID).toString().trim();
												jComboBox.addItem(wrkKey);
												if (metrics2.stringWidth(wrkKey) > fieldWidth) {
													fieldWidth = metrics2.stringWidth(wrkKey);
												}
											}

											jComboBox.setBounds(new Rectangle(5, 0, fieldWidth + 30, 24));
											jComboBox.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
											jComboBox.setSelectedIndex(0);
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
						xFPromptCall.addKeyListener(new XF300_keyAdapter(dialog));
						xFPromptCall.setLocation(5, 0);
						component = xFPromptCall;
						if (component.getBounds().width < 70) {
							component.setBounds(new Rectangle(component.getBounds().x, component.getBounds().y, 70, component.getBounds().height));
						}
					} else {
						if (dataType.equals("DATE")) {
							componentType = "DATE";
							xFDateField = new XFDateField(dialog_.getSession());
							xFDateField.addKeyListener(new XF300_keyAdapter(dialog));
							xFDateField.setLocation(5, 0);
							xFDateField.setEditable(true);
							component = xFDateField;
						} else {
							if (dataTypeOptionList.contains("YMONTH")) {
								componentType = "YMONTH";
								xFYMonthBox = new XFYMonthBox(dialog_.getSession());
								xFYMonthBox.addKeyListener(new XF300_keyAdapter(dialog));
								xFYMonthBox.setLocation(5, 0);
								xFYMonthBox.setEditable(true);
								component = xFYMonthBox;
							} else {
								if (dataTypeOptionList.contains("MSEQ")) {
									componentType = "MSEQ";
									xFMSeqBox = new XFMSeqBox(dialog_.getSession());
									xFMSeqBox.addKeyListener(new XF300_keyAdapter(dialog));
									xFMSeqBox.setLocation(5, 0);
									xFMSeqBox.setEditable(true);
									component = xFMSeqBox;
								} else {
									if (dataTypeOptionList.contains("FYEAR")) {
										componentType = "FYEAR";
										xFFYearBox = new XFFYearBox(dialog_.getSession());
										xFFYearBox.addKeyListener(new XF300_keyAdapter(dialog));
										xFFYearBox.setLocation(5, 0);
										xFFYearBox.setEditable(true);
										component = xFFYearBox;
									} else {
										componentType = "TEXTFIELD";
										xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, fieldOptions, dialog_.getSession().systemFont);
										xFTextField.addKeyListener(new XF300_keyAdapter(dialog));
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
		this.setPreferredSize(new Dimension(jLabelField.getPreferredSize().width + component.getBounds().width + 5, component.getBounds().height));
	}

	Object getDefaultValue() {
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

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
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

	public String getCaption(){
		return fieldCaption;
	}

	public String getCaptionAndValue(){
		String value = fieldCaption + ":";
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
				xFDateField.setEditable(false);
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
			if (componentType.equals("PROMPT_CALL")) {
				xFPromptCall.setValue(mapValue.toString());
			}
		}
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
			if (mapValue.toString().equals("")) {
				isValidated = false;
			}
		} else {
			if (!this.isEditable_) {
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
			jComboBox.setSelectedIndex(keyValueList.indexOf(value.toString()));
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
					if (Double.parseDouble(wrkStr.trim()) != 0 || !fieldOptionList.contains("IGNORE_IF_ZERO")) {
						value = wrkStr;
					}
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
			value = (String)keyValueList.get(jComboBox.getSelectedIndex());
		}
		if (componentType.equals("VALUES_LIST")) {
			value = (String)jComboBox.getSelectedItem();
		}
		if (componentType.equals("PROMPT_CALL")) {
			wrkStr = (String)xFPromptCall.getInternalValue();
			if (!wrkStr.equals("")) {
				if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
					if (Double.parseDouble(wrkStr.trim()) != 0 || !fieldOptionList.contains("IGNORE_IF_ZERO")) {
						value = wrkStr;
					}
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
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
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
			jComboBox.setEditable(isEditable_);
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

	public boolean isReflect(){
		return isReflect;
	}
	
	public boolean isValidated(){
		boolean validated = false;
		XF300_DetailColumn columnField = dialog_.getDetailColumnObjectByID(index_, this.getTableID(), this.getTableAlias(), this.getFieldID());

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

			if (columnField.isReadyToValidate()) {
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
						if ((Double.parseDouble(stringFilterValue) == 0) && fieldOptionList.contains("IGNORE_IF_ZERO")) {
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
									if (stringResultValue.toUpperCase().equals(stringFilterValue.toUpperCase())) {
										validated = true;
									}
								}
								if (operandType.equals("SCAN")) {
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
								StringTokenizer workTokenizer = new StringTokenizer(stringFilterValue, ";" );
								while (workTokenizer.hasMoreTokens()) {
									if (operandType.equals("EQ")) {
										if (stringResultValue.equals(workTokenizer.nextToken())) {
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

		return validated;
	}
	
	public String getStringValue(){
		String value = "";
		if (componentType.equals("TEXTFIELD") || componentType.equals("ASSISTFIELD")) {
			if (componentType.equals("TEXTFIELD")) {
				value = xFTextField.getInternalValue().toString().trim();
			}
			if (componentType.equals("ASSISTFIELD")) {
				value = xFInputAssistField.getInternalValue().toString().trim();
			}
			if (this.getBasicType().equals("INTEGER") || this.getBasicType().equals("FLOAT")) {
				try {
					if (Double.parseDouble(value) == 0 && fieldOptionList.contains("IGNORE_IF_ZERO")) {
						value = "";
					}
				} catch (NumberFormatException e) {
					value = "";
				}
			}
		}
		if (componentType.equals("BOOLEAN")) {
			value = (String)xFCheckBox.getInternalValue();
		}
		if (componentType.equals("DATE")) {
			if (xFDateField.getInternalValue() == null) {
				value = "";
			} else {
				value = xFDateField.getInternalValue().toString();
			}
		}
		if (componentType.equals("YMONTH")) {
			if (xFYMonthBox.getInternalValue() == null) {
				value = "";
			} else {
				value = xFYMonthBox.getInternalValue().toString();
			}
		}
		if (componentType.equals("MSEQ")) {
			if (xFMSeqBox.getInternalValue() == null) {
				value = "";
			} else {
				value = xFMSeqBox.getInternalValue().toString();
			}
		}
		if (componentType.equals("FYEAR")) {
			if (xFFYearBox.getInternalValue() == null) {
				value = "";
			} else {
				value = xFFYearBox.getInternalValue().toString();
			}
		}
		if (componentType.equals("KUBUN_LIST")) {
			value = keyValueList.get(jComboBox.getSelectedIndex());
		}
		if (componentType.equals("RECORDS_LIST")
				|| componentType.equals("VALUES_LIST")) {
			value = (String)jComboBox.getSelectedItem();
		}
		if (componentType.equals("PROMPT_CALL")) {
			value = xFPromptCall.getInternalValue().toString();
		}
		return value;
	}
}

class XF300_PromptCallField extends JPanel implements XFEditableField {
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
    private XF300 dialog_;
    private org.w3c.dom.Element fieldElement_;
    private String functionID_ = "";
    private ArrayList<XF300_DetailReferTable> referTableList_;
    private String oldValue = "";
    private String listValue = "";
    private ArrayList<String> fieldsToPutList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToPutToList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetList_ = new ArrayList<String>();
    private ArrayList<String> fieldsToGetToList_ = new ArrayList<String>();
	private ArrayList<String> kubunValueList = null;
	private ArrayList<String> kubunTextList = null;
    private int index_;

	public XF300_PromptCallField(org.w3c.dom.Element fieldElement, String functionID, XF300 dialog, int index){
		super();
		fieldElement_ = fieldElement;
		functionID_ = functionID;
		dialog_ = dialog;
		index_ = index;

		String fieldOptions = fieldElement_.getAttribute("FieldOptions");
		StringTokenizer workTokenizer = new StringTokenizer(fieldElement_.getAttribute("DataSource"), "." );
		tableAlias = workTokenizer.nextToken();
		fieldID =workTokenizer.nextToken();
		tableID = tableAlias;
		referTableList_ = dialog_.getDetailReferTableList(index);
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
		xFTextField.addKeyListener(new XF300_keyAdapter(dialog));

		String wrkStr = XFUtility.getOptionValueWithKeyword(workElement.getAttribute("TypeOptions"), "KUBUN");
		if (!wrkStr.equals("")) {
			FontMetrics metrics = xFTextField.getFontMetrics(xFTextField.getFont());
			String wrk = "";
			int fieldWidth = 50;
			try {
				kubunValueList = new ArrayList<String>();
				kubunTextList = new ArrayList<String>();
				String userVariantsTableID = dialog_.getSession().getTableNameOfUserVariants();
				String sql = "select * from " + userVariantsTableID + " where IDUSERKUBUN = '" + wrkStr + "' order by SQLIST";
				XFTableOperator operator = dialog_.createTableOperator(sql);
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

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XF300.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object value;
				XF300_Filter filter;
				HashMap<String, Object> fieldValuesMap = new HashMap<String, Object>();
				for (int i = 0; i < fieldsToPutList_.size(); i++) {
					filter = dialog_.getFilterObjectByName(index_, fieldsToPutList_.get(i));
					if (filter != null) {
						fieldValuesMap.put(fieldsToPutToList_.get(i), filter.getValue());
					}
				}
				try {
					HashMap<String, Object> returnMap = dialog_.getSession().executeFunction(functionID_, fieldValuesMap);
					if (returnMap.get("RETURN_TO") != null && returnMap.get("RETURN_TO").equals("MENU")) {
						dialog_.returnToMenu();
					}
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
							}						}
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
		jButton.addKeyListener(new XF300_keyAdapter(dialog));

		this.setSize(new Dimension(xFTextField.getWidth() + 27, XFUtility.FIELD_UNIT_HEIGHT));
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
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID = "";
	private String fixedWhere = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF300 dialog_;
	private StringTokenizer workTokenizer;
	private String dbName = "";

	public XF300_HeaderTable(org.w3c.dom.Element functionElement, XF300 dialog){
		super();
		functionElement_ = functionElement;
		dialog_ = dialog;

		tableID = functionElement_.getAttribute("HeaderTable");
		tableElement = dialog_.getSession().getTableElement(tableID);

		if (tableElement.getAttribute("DB").equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(tableElement.getAttribute("DB"));
		}

		String workString;
		org.w3c.dom.Element workElement;

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

		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID, element, dialog_.getSession().getTableNodeList()));
		}
	}

	public String getSelectSQL(){
		int count;
		StringBuffer buf = new StringBuffer();
		
		///////////////////////////
		// Select-Fields section //
		///////////////////////////
		buf.append("select ");
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

		////////////////////////////
		// From and Where section //
		////////////////////////////
		buf.append(" from ");
		buf.append(tableID);
		fixedWhere = XFUtility.getFixedWhereValue(functionElement_.getAttribute("FixedWhere"), dialog_.getSession());
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
				if (dialog_.getParmMap().containsKey(dialog_.getHeaderFieldList().get(i).getFieldID())) {
					buf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(i).getBasicType(),
							dialog_.getParmMap().get(dialog_.getHeaderFieldList().get(i).getFieldID()), dbName)) ;
				} else {
					buf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(i).getBasicType(),
							dialog_.getParmMap().get(dialog_.getHeaderFieldList().get(i).getDataSourceName()), dbName)) ;
				}
			}
		}
		if (!fixedWhere.equals("")) {
			buf.append(" and (");
			buf.append(fixedWhere);
			buf.append(") ");
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
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF300_HeaderReferTable referTable;
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
		return isValid;
	}

	public void runScript(String event1, String event2) throws ScriptException {
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		dialog_.setEvaluatingScriptTabIndex(-1);
		for (int i = 0; i < validScriptList.size(); i++) {
			dialog_.evalHeaderTableScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
		}
	}
}

class XF300_HeaderReferTable extends Object {
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF300 dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private String dbName = "";

	public XF300_HeaderReferTable(org.w3c.dom.Element referElement, XF300 dialog){
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
		org.w3c.dom.Element workElement;
		StringBuffer buf = new StringBuffer();

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
		if (count == 0) {
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				if (count > 0) {
					buf.append(",");
				}
				count++;
				buf.append(toKeyFieldIDList.get(i));
			}
		}
		
		////////////////////////////
		// From and Where section //
		////////////////////////////
		buf.append(" from ");
		buf.append(tableID);
		buf.append(" where ");
		count = 0;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (count > 0) {
				buf.append(" and ");
			}
			buf.append(toKeyFieldIDList.get(i));
			buf.append("=");
			for (int j = 0; j < dialog_.getHeaderFieldList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getHeaderFieldList().get(j).getTableAlias() + "." + dialog_.getHeaderFieldList().get(j).getFieldID())) {
					buf.append(XFUtility.getTableOperationValue(dialog_.getHeaderFieldList().get(j).getBasicType(),
							dialog_.getHeaderFieldList().get(j).getInternalValue(), dbName)) ;
					break;
				}
			}
			count++;
		}

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
}

class XF300_StructureTable extends Object {
	private org.w3c.dom.Element functionElement_ = null;
	private org.w3c.dom.Element tableElement_ = null;
	private String tableID = "";
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
	private String dbName = "";

	public XF300_StructureTable(org.w3c.dom.Element functionElement, XF300 dialog){
		super();
		String imageFileFolder, wrkStr;
		ImageIcon icon;
		StringTokenizer workTokenizer1, workTokenizer2;
		org.w3c.dom.Element workElement;

		functionElement_ = functionElement;
		dialog_ = dialog;

		tableID = functionElement_.getAttribute("StructureTable");
		tableElement_ = dialog_.getSession().getTableElement(tableID);

		if (tableElement_.getAttribute("DB").equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(tableElement_.getAttribute("DB"));
		}

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

		imageFileFolder = dialog_.getSession().getImageFileFolder();
		if (!functionElement_.getAttribute("StructureNodeDefaultIcon").equals("")) {
			defaultIcon = new ImageIcon(imageFileFolder + functionElement_.getAttribute("StructureNodeDefaultIcon"));
		}
		iconControlFieldID = functionElement_.getAttribute("StructureNodeIconsFieldID");;
		if (!iconControlFieldID.equals("")) {
			workElement = dialog_.getSession().getFieldElement(dialog_.getHeaderTable().getTableID(), iconControlFieldID);
			basicTypeOfIconControlField = XFUtility.getBasicTypeOf(workElement.getAttribute("Type"));
		}

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
		if (functionElement_.getAttribute("StructureViewTitle").equals("")) {
			return tableElement_.getAttribute("Name");
		} else {
			return functionElement_.getAttribute("StructureViewTitle");
		}
	}
	
	public String getSelectSQL(HashMap<String, Object> parmMap){
		int count;
		StringBuffer buf = new StringBuffer();
		XF300_HeaderField headerField;
		
		///////////////////////////////
		// Select-Field-From section //
		///////////////////////////////
		buf.append("select * from ");
		buf.append(tableID);
		
		///////////////////
		// Where section //
		///////////////////
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
			buf.append(XFUtility.getTableOperationValue(headerField.getBasicType(),
					parmMap.get(dialog_.getHeaderTable().getKeyFieldIDList().get(i)), dbName)) ;
		}
		
		//////////////////////
		// Order-by section //
		//////////////////////
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

		return buf.toString();
	}
	
	public String getSQLForNodeInfo(HashMap<String, Object> keyValueMap){
		int count;
		StringBuffer buf = new StringBuffer();
		XF300_HeaderField headerField;

		///////////////////////////////
		// Select-Field-From section //
		///////////////////////////////
		buf.append("select * from ");
		buf.append(dialog_.getHeaderTable().getTableID());
		
		///////////////////
		// Where section //
		///////////////////
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
			buf.append(XFUtility.getTableOperationValue(headerField.getBasicType(),
					keyValueMap.get(dialog_.getHeaderTable().getKeyFieldIDList().get(i)), dbName)) ;
		}

		return buf.toString();
	}
	
	public String getSQLForNodeInfo(XFTableOperator structreTableOperator) throws Exception {
		int count;
		StringBuffer buf = new StringBuffer();
		XF300_HeaderField headerField;

		///////////////////////////////
		// Select-Field-From section //
		///////////////////////////////
		buf.append("select * from ");
		buf.append(dialog_.getHeaderTable().getTableID());
		
		///////////////////
		// Where section //
		///////////////////
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
			if (dialog_.isForExplosion()) {
				buf.append(XFUtility.getTableOperationValue(headerField.getBasicType(),
						structreTableOperator.getValueOf(childKeyFieldIDList.get(i)), dbName)) ;
			} else {
				buf.append(XFUtility.getTableOperationValue(headerField.getBasicType(),
						structreTableOperator.getValueOf(upperKeyFieldIDList.get(i)), dbName)) ;
			}
		}

		return buf.toString();
	}
	
	public XF300_TreeNode createTreeNode(XFTableOperator headerTableOperator) throws Exception {
		HashMap<String, Object> keyValueMap = new HashMap<String, Object>();
		for (int i = 0; i < dialog_.getHeaderTable().getKeyFieldIDList().size(); i++) {
			keyValueMap.put(dialog_.getHeaderTable().getKeyFieldIDList().get(i), headerTableOperator.getValueOf(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
		}

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

		Icon icon = defaultIcon;
		if (!iconControlFieldID.equals("")) {
			Object value = headerTableOperator.getValueOf(iconControlFieldID);
			if (XFUtility.isLiteralRequiredBasicType(basicTypeOfIconControlField)) {
				icon = nodeIconMap.get(value.toString().trim());
			} else {
				int intValue = Integer.parseInt(value.toString());
				icon = nodeIconMap.get(intValue);
			}
			if (icon == null) {
				icon = defaultIcon;
			}
		}

		return new XF300_TreeNode(keyValueMap, sb.toString(), icon, dialog_);
	}
	
	public XF300_TreeNode createTreeNode(XFTableOperator headerTableOperator, XFTableOperator structureTableOperator) throws Exception {
		HashMap<String, Object> keyValueMap = new HashMap<String, Object>();
		for (int i = 0; i < dialog_.getHeaderTable().getKeyFieldIDList().size(); i++) {
			keyValueMap.put(dialog_.getHeaderTable().getKeyFieldIDList().get(i), headerTableOperator.getValueOf(dialog_.getHeaderTable().getKeyFieldIDList().get(i)));
		}

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

		Icon icon = defaultIcon;
		if (!iconControlFieldID.equals("")) {
			Object value = headerTableOperator.getValueOf(iconControlFieldID);
			if (XFUtility.isLiteralRequiredBasicType(basicTypeOfIconControlField)) {
				icon = nodeIconMap.get(value.toString().trim());
			} else {
				int intValue = Integer.parseInt(value.toString());
				icon = nodeIconMap.get(intValue);
			}
			if (icon == null) {
				icon = defaultIcon;
			}
		}

		return new XF300_TreeNode(keyValueMap, sb.toString(), icon, dialog_);
	}
}

class XF300_DetailTable extends Object {
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element detailTableElement_ = null;
	private String tableID = "";
	private String fixedWhere = "";
	private ArrayList<String> headerKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF300 dialog_;
	private int tabIndex_;
	private StringTokenizer workTokenizer;
	private boolean hasOrderByAsItsOwnFields = true;
	private String dbName = "";

	public XF300_DetailTable(org.w3c.dom.Element detailTableElement, int tabIndex, XF300 dialog){
		super();
		detailTableElement_ = detailTableElement;
		tabIndex_ = tabIndex;
		dialog_ = dialog;

		tableID = detailTableElement.getAttribute("Table");
		tableElement = dialog_.getSession().getTableElement(tableID);

		if (tableElement.getAttribute("DB").equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(tableElement.getAttribute("DB"));
		}

		int pos1;
		String wrkStr1, wrkStr2;
		org.w3c.dom.Element workElement;

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

		workTokenizer = new StringTokenizer(detailTableElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			wrkStr1 = workTokenizer.nextToken();
			pos1 = wrkStr1.indexOf(".");
			if (pos1 > -1) { 
				wrkStr2 = wrkStr1.substring(0, pos1);
				if (!wrkStr2.equals(this.tableID)) {
					hasOrderByAsItsOwnFields = false;
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
	
	public String getSelectSQL(){
		int count;
		StringBuffer buf = new StringBuffer();
		XF300_HeaderField headerField;
		ArrayList<String> fieldIDList = new ArrayList<String>();
		
		////////////////////////////////
		// Select-Fields-FROM section //
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
		for (int i = 0; i < dialog_.getDetailColumnList(tabIndex_).size(); i++) {
			if (dialog_.getDetailColumnList(tabIndex_).get(i).getTableID().equals(tableID)
					&& !dialog_.getDetailColumnList(tabIndex_).get(i).isVirtualField()
					&& !dialog_.getDetailColumnList(tabIndex_).get(i).getBasicType().equals("BYTEA")) {
				if (!fieldIDList.contains(dialog_.getDetailColumnList(tabIndex_).get(i).getFieldID())) {
					count++;
					if (count > 0) {
						buf.append(", ");
					}
					buf.append(dialog_.getDetailColumnList(tabIndex_).get(i).getFieldID());
					fieldIDList.add(dialog_.getDetailColumnList(tabIndex_).get(i).getFieldID());
				}
			}
		}
		buf.append(" from ");
		buf.append(tableID);
		
		///////////////////
		// Where section //
		///////////////////
		fixedWhere = XFUtility.getFixedWhereValue(detailTableElement_.getAttribute("FixedWhere"), dialog_.getSession());
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
			buf.append(XFUtility.getTableOperationValue(headerField.getBasicType(), headerField.getInternalValue(), dbName)) ;
		}
		if (!fixedWhere.equals("")) {
			buf.append(" and (");
			buf.append(fixedWhere);
			buf.append(") ");
		}
		
		//////////////////////
		// Order-by section //
		//////////////////////
		if (this.hasOrderByAsItsOwnFields) {
			ArrayList<String> orderByFieldList = getOrderByFieldIDList(dialog_.isListingInNormalOrder(tabIndex_));
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
					if (!dialog_.isListingInNormalOrder(tabIndex_)) {
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
					workTableID = dialog_.getTableIDOfTableAlias(workAlias, tabIndex_);
					workFieldID = workStr.substring(pos0+1, pos1);
					workElement = dialog_.getSession().getFieldElement(workTableID, workFieldID);
					buf.append(workElement.getAttribute("Name"));
					buf.append(XFUtility.RESOURCE.getString("Descend"));
				} else {
					workAlias = workStr.substring(0, pos0);
					workTableID = dialog_.getTableIDOfTableAlias(workAlias, tabIndex_);
					workFieldID = workStr.substring(pos0+1, workStr.length());
					workElement = dialog_.getSession().getFieldElement(workTableID, workFieldID);
					buf.append(workElement.getAttribute("Name"));
				}
			}
		} else {
			int numberOfHeaderTableKeys = dialog_.getHeaderTable().getKeyFieldIDList().size();
			for (int i = numberOfHeaderTableKeys; i < keyFieldIDList.size(); i++) {
				if (i > numberOfHeaderTableKeys) {
					buf.append(">");
				}
				workElement = dialog_.getSession().getFieldElement(tableID, keyFieldIDList.get(i));
				buf.append(workElement.getAttribute("Name"));
			}
		}

		return buf.toString();
	}
	
	public boolean hasOrderByAsItsOwnFields(){
		return hasOrderByAsItsOwnFields;
	}
	
	public boolean isValidDataSource(int index, String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF300_DetailReferTable referTable;
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

		return isValid;
	}

	public void runScript(int index, String event1, String event2) throws ScriptException {
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		dialog_.setEvaluatingScriptTabIndex(tabIndex_);
		for (int i = 0; i < validScriptList.size(); i++) {
			dialog_.evalDetailTableScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText(), index);
		}
	}
}

class XF300_DetailReferTable extends Object {
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private int tabIndex_;
	private XF300 dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private String dbName = "";

	public XF300_DetailReferTable(org.w3c.dom.Element referElement, int tabIndex, XF300 dialog){
		super();
		referElement_ = referElement;
		tabIndex_ = tabIndex;
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
		boolean validWhereKeys = false;
		
		////////////////////////////////
		// Select-Fields-From section //
		////////////////////////////////
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
			for (int j = 0; j < dialog_.getDetailColumnList(tabIndex_).size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList(tabIndex_).get(j).getDataSourceName())) {
					buf.append(XFUtility.getTableOperationValue(dialog_.getDetailColumnList(tabIndex_).get(j).getBasicType(),
							dialog_.getDetailColumnList(tabIndex_).get(j).getInternalValue(), dbName)) ;
					if (!dialog_.getDetailColumnList(tabIndex_).get(j).getInternalValue().equals("")) {
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
}

class XF300_KeyInputDialog extends JDialog {
	private static final long serialVersionUID = 1L;
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
		jTextAreaMessages.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jScrollPaneMessages.setPreferredSize(new Dimension(10, 50));
		jPanelBottom.setPreferredSize(new Dimension(10, 50));
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
		jPanelButtons.setLayout(null);
		jPanelButtons.setFocusable(false);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
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
		jButtonCancel.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
		jButtonCancel.setText(XFUtility.RESOURCE.getString("Cancel"));
		jButtonCancel.setBounds(new Rectangle(20, 9, 100, 32));
		jButtonCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				keyMap_.clear();
				setVisible(false);
			}
		});
		jButtonOK.setFont(new java.awt.Font(dialog_.getSession().systemFont, 0, XFUtility.FONT_SIZE-2));
		jButtonOK.setText("OK");
		jButtonOK.setBounds(new Rectangle(200, 9, 100, 32));
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
					jTextAreaMessages.setText(XFUtility.RESOURCE.getString("FunctionError23"));
				} else {
					setVisible(false);
				}
			}
		});
		jPanelButtons.add(jButtonCancel);
		jPanelButtons.add(jButtonOK);
		this.getRootPane().setDefaultButton(jButtonOK);
		this.getContentPane().add(jPanelMain, BorderLayout.CENTER);

		org.w3c.dom.Element element;
		int posX = 0;
		int posY = 0;
		int biggestWidth = 0;
		int biggestHeight = 0;
		Dimension dim = new Dimension();
		Dimension dimOfPriviousField = new Dimension();
		boolean topField = true;
		XF300_HeaderField field;

		this.setTitle(dialog_.getTitle());
		jLabelSessionID.setText(dialog_.getSession().getSessionID());
		jLabelFunctionID.setText(dialog_.getFunctionInfo());
		FontMetrics metrics = jLabelFunctionID.getFontMetrics(jLabelFunctionID.getFont());
		jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));

		ArrayList<String> keyFieldList = dialog_.getHeaderTable().getKeyFieldIDList();
		for (int i = 0; i < keyFieldList.size(); i++) {
			element = dialog_.getSession().getDomDocument().createElement("Field");
			element.setAttribute("DataSource", dialog_.getHeaderTable().getTableID()+"."+keyFieldList.get(i));
			field = new XF300_HeaderField(element, dialog_);
			field.setEditable(true);
			fieldList.add(field);

			if (topField) {
				posX = 0;
				posY = XFUtility.FIELD_VERTICAL_MARGIN + 8;
				topField = false;
			} else {
				posX = 0;
				posY = posY + dimOfPriviousField.height+ field.getPositionMargin() + XFUtility.FIELD_VERTICAL_MARGIN;
			}
			dim = field.getPreferredSize();
			field.setBounds(posX, posY, dim.width, dim.height);
			jPanelKeyFields.add(field);

			if (posX + dim.width > biggestWidth) {
				biggestWidth = posX + dim.width;
			}
			if (posY + dim.height > biggestHeight) {
				biggestHeight = posY + dim.height;
			}
			dimOfPriviousField = new Dimension(dim.width, dim.height);
		}

		int width = 500;
		if (biggestWidth > 480) {
			width = biggestWidth + 20;
		}
		int height = biggestHeight + 170;
		this.setPreferredSize(new Dimension(width, height));
		this.pack();
	}
	
	public HashMap<String, Object> requestKeyValues(String message) {
		keyMap_.clear();
		if (message.equals("")) {
			jTextAreaMessages.setText(XFUtility.RESOURCE.getString("FunctionMessage29"));
		} else {
			jTextAreaMessages.setText(message);
		}
		Rectangle screenRect = dialog_.getSession().getMenuRectangle();
		int posX = (screenRect.width - (int)this.getPreferredSize().getWidth()) / 2 + screenRect.x;
		int posY = (screenRect.height - (int)this.getPreferredSize().getHeight()) / 2 + screenRect.y;
		this.setLocation(posX, posY);
		this.setVisible(true);
		return keyMap_;
	}

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			keyMap_.clear();
			setVisible(false);
			//dialog_.closeFunction();
		}
		super.processWindowEvent(e);
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
		if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
			adaptee.setListingInNormalOrder(adaptee.getSelectedTabIndex(), false);
		} else {
			adaptee.setListingInNormalOrder(adaptee.getSelectedTabIndex(), true);
		}
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
