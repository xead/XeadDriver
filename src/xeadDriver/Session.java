package xeadDriver;

/*
 * Copyright (c) 2013 WATANABE kozo <qyf05466@nifty.com>,
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
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.security.*;
import java.net.URI;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.xerces.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import com.lowagie.text.pdf.BaseFont;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

public class Session extends JFrame {
	private static final long serialVersionUID = 1L;
	private String systemName = "";
	private String version = "";
	private String sessionID = "";
	private String sessionStatus = "";
	private boolean noErrorsOccured = true;
	private String databaseName = "";
	private String databaseUser = "";
	private String databasePassword = "";
	private String appServerName = "";
	private String userID = "";
	private String userName = "";
	private String userEmployeeNo = "";
	private String userEmailAddress = "";
	private String userMenus = "";
	private String userTable = "";
	private String variantsTable = "";
	private String userVariantsTable = "";
	private String sessionTable = "";
	private String sessionDetailTable = "";
	private String numberingTable = "";
	private String calendarTable = "";
	private String taxTable = "";
	private String exchangeRateAnnualTable = "";
	private String exchangeRateMonthlyTable = "";
	private String menuIDUsing = "";
	private String imageFileFolder = "";
	private File outputFolder = null;
	private String welcomePageURL = "";
	private String dateFormat = "";
	private int sqProgram = 0;
	private String currentFolder = "";
	private String loginScript = "";
	private String scriptFunctions = "";
	private String smtpHost = "";
	private String smtpPort = "";
	private String smtpUser = "";
	private String smtpPassword = "";

	private Image imageTitle;
	private Dimension screenSize = new Dimension(0,0);
	private Connection connectionManualCommit = null;
	private Connection connectionAutoCommit = null;
	private ArrayList<String> subDBIDList = new ArrayList<String>();
	private ArrayList<String> subDBNameList = new ArrayList<String>();
	private ArrayList<String> subDBUserList = new ArrayList<String>();
	private ArrayList<String> subDBPasswordList = new ArrayList<String>();
	private ArrayList<Connection> subDBConnectionList = new ArrayList<Connection>();
	private String[] menuIDArray = new String[20];
	private String[] menuCaptionArray = new String[20];
	private String[] helpURLArray = new String[20];
	private MenuOption[][] menuOptionArray = new MenuOption[20][20];
	private JButton[] jButtonMenuOptionArray = new JButton[20];
	private ArrayList<String> loadingChekerIDList = new ArrayList<String>();

	private JLabel jLabelUser = new JLabel();
	private JLabel jLabelSession = new JLabel();
	private GridLayout gridLayoutMenuButtons = new GridLayout();
	private JPanel jPanelTop = new JPanel();
	private JPanel jPanelMenu = new JPanel();
	private JPanel jPanelMenuCenter = new JPanel();
	private JPanel jPanelMenuTopMargin = new JPanel();
	private JPanel jPanelMenuLeftMargin = new JPanel();
	private JPanel jPanelMenuRightMargin = new JPanel();
	private JPanel jPanelMenuBottomMargin = new JPanel();
	private JSplitPane jSplitPane1 = new JSplitPane();
	private JSplitPane jSplitPane2 = new JSplitPane();
	private JScrollPane jScrollPaneNews = new JScrollPane();
	private JScrollPane jScrollPaneMenu = new JScrollPane();
	private JTabbedPane jTabbedPaneMenu = new JTabbedPane();
	private JEditorPane jEditorPaneNews = new JEditorPane();
	private HTMLEditorKit editorKit = new HTMLEditorKit();
	private ImageIcon imageIcon = null;
	private JTextArea jTextAreaMessages = new JTextArea();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private Calendar calendar = GregorianCalendar.getInstance();
	private org.w3c.dom.Document domDocument;
	private Desktop desktop = Desktop.getDesktop();
	private DigestAdapter digestAdapter = null;
	private DialogLogin loginDialog = null;
	private DialogModifyPassword modifyPasswordDialog = null;
	private FunctionLauncher functionLauncher = new FunctionLauncher(this);
	private SortableDomElementListModel sortingList;
	private NodeList functionList = null;
	private NodeList tableList = null;
	private ArrayList<String> offDateList = new ArrayList<String>();
	private HashMap<String, BaseFont> baseFontMap = new HashMap<String, BaseFont>();
	private HashMap<String, String> attributeMap = new HashMap<String, String>();
	private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
	private Bindings globalScriptBindings = null;
	private ScriptEngine scriptEngine = null;
    private static final String ZIP_URL = "http://api.postalcode.jp/v1/zipsearch?";
	private DOMParser responseDocParser = new DOMParser();
	private org.w3c.dom.Document responseDoc = null;
	private HttpGet httpGet = new HttpGet();
	private ArrayList<ReferChecker> referCheckerList = new ArrayList<ReferChecker>();
	private Application application;
	private XFOptionDialog optionDialog = new XFOptionDialog(this);
	private XFInputDialog inputDialog = new XFInputDialog(this);
	private XFCheckListDialog checkListDialog = new XFCheckListDialog();
	private XFLongTextEditor xfLongTextEditor = new XFLongTextEditor(this);

	public Session(String[] args, Application app) {
		String fileName = "";
		String loginUser = "";
		String loginPassword = "";
		application = app;

		try {
			if (args.length >= 1) {
				fileName =  args[0];
			}
			if (args.length >= 2) {
				loginUser =  args[1];
			}
			if (args.length >= 3) {
				loginPassword =  args[2];
			}

			if (fileName.equals("")) {
				EventQueue.invokeLater(new Runnable() {
					@Override public void run() {
						application.hideSplash();
					}
				});
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError1"));
				System.exit(0);
			} else {
				loginDialog = setupVariantsToGetLoginDialog(fileName, loginUser, loginPassword);
				if (loginDialog == null) {
					System.exit(0);
				} else {
					if (loginDialog.userIsValidated()) {

						userID = loginDialog.getUserID();
						userName = loginDialog.getUserName();
						userEmployeeNo = loginDialog.getUserEmployeeNo();
						userEmailAddress = loginDialog.getUserEmailAddress();
						userMenus = loginDialog.getUserMenus();

						setupSessionAndMenus();

						EventQueue.invokeLater(new Runnable() {
							@Override public void run() {
								application.hideSplash();
							}
						});
						this.setVisible(true);
					}else {
						closeSession(false);
						System.exit(0);
					}
				}
			}
		} catch(ScriptException e) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError0") + "\n" + e.getMessage());
			noErrorsOccured = false;
			closeSession(false);
			System.exit(0);
		} catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("LogInError3") + "\n" + e.getMessage());
			noErrorsOccured = false;
			closeSession(false);
			System.exit(0);
		}
	}
	
	private DialogLogin setupVariantsToGetLoginDialog(String fileName, String user, String password) throws Exception {
		////////////////////////////////////////////////////////////////
		// Parse XML formatted data into DOM with file name requested //
		////////////////////////////////////////////////////////////////
		application.setTextOnSplash(XFUtility.RESOURCE.getString("SplashMessage1"));
		if (fileName.startsWith("http:")
				|| fileName.startsWith("https:")
				|| fileName.startsWith("file:")) {
        	try {
        		URL url = new URL(fileName);
        		URLConnection connection = url.openConnection();
        		InputStream inputStream = connection.getInputStream();
        		DOMParser parser = new DOMParser();
        		parser.parse(new InputSource(inputStream));
        		domDocument = parser.getDocument();
        	} catch (Exception e) {
        		JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("SessionError2") + fileName + XFUtility.RESOURCE.getString("SessionError3") + "\n" + e.getMessage());
        		return null;
        	}
		} else {
			File xeafFile = new File(fileName);
	        if (xeafFile.exists()) {
	        	currentFolder = xeafFile.getParent();
	        	try {
					DOMParser parser = new DOMParser();
					parser.parse(new InputSource(new FileInputStream(fileName)));
					domDocument = parser.getDocument();
	        	} catch (Exception e) {
	        		JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("SessionError2") + fileName + XFUtility.RESOURCE.getString("SessionError3") + "\n" + e.getMessage());
	        		return null;
	        	}
	        } else {
	    		JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("SessionError21") + fileName + XFUtility.RESOURCE.getString("SessionError22"));
	    		return null;
	        }
		}

		////////////////////////////////////////////////////////////
		// Extract various elements of system definition from DOM //
		////////////////////////////////////////////////////////////
		NodeList nodeList = domDocument.getElementsByTagName("System");
		org.w3c.dom.Element element = (org.w3c.dom.Element)nodeList.item(0);
		systemName = element.getAttribute("Name");
		version = element.getAttribute("Version");
		welcomePageURL = element.getAttribute("WelcomePageURL");
		dateFormat = element.getAttribute("DateFormat");
		calendar.setLenient(false);

		////////////////////
		// System Folders //
		////////////////////
		String wrkStr = element.getAttribute("ImageFileFolder"); 
		if (wrkStr.equals("")) {
			imageFileFolder = currentFolder + File.separator;
		} else {
			if (wrkStr.contains("<CURRENT>")) {
				imageFileFolder = wrkStr.replace("<CURRENT>", currentFolder) + File.separator;
			} else {
				imageFileFolder = wrkStr + File.separator;
			}
		}
		wrkStr = element.getAttribute("OutputFolder"); 
		if (wrkStr.equals("")) {
			outputFolder = null;
		} else {
			if (wrkStr.contains("<CURRENT>")) {
				wrkStr = wrkStr.replace("<CURRENT>", currentFolder);
			}
			outputFolder = new File(wrkStr);
			if (!outputFolder.exists()) {
				outputFolder = null;
			}
		}
		
		///////////////////////////
		// System control tables //
		///////////////////////////
		userTable = element.getAttribute("UserTable");
		variantsTable = element.getAttribute("VariantsTable");
		userVariantsTable = element.getAttribute("UserVariantsTable");
		numberingTable = element.getAttribute("NumberingTable");
		sessionTable = element.getAttribute("SessionTable");
		sessionDetailTable = element.getAttribute("SessionDetailTable");
		taxTable = element.getAttribute("TaxTable");
		calendarTable = element.getAttribute("CalendarTable");
		exchangeRateAnnualTable = element.getAttribute("ExchangeRateAnnualTable");
		exchangeRateMonthlyTable = element.getAttribute("ExchangeRateMonthlyTable");

		////////////////////
		// System Scripts //
		////////////////////
		loginScript = XFUtility.substringLinesWithTokenOfEOL(element.getAttribute("LoginScript"), "\n");
		scriptFunctions = XFUtility.substringLinesWithTokenOfEOL(element.getAttribute("ScriptFunctions"), "\n");

		////////////////////////////////
		// Function List / Table List //
		////////////////////////////////
		functionList = domDocument.getElementsByTagName("Function");
		tableList = domDocument.getElementsByTagName("Table");

		//////////////////////////
		// Main / Sub Databases //
		//////////////////////////
		databaseName = element.getAttribute("DatabaseName");
		if (databaseName.contains("<CURRENT>")) {
			databaseName = databaseName.replace("<CURRENT>", currentFolder);
		}
		databaseUser = element.getAttribute("DatabaseUser");
		databasePassword = element.getAttribute("DatabasePassword");
		org.w3c.dom.Element subDBElement;
		Connection subDBConnection;
		NodeList subDBList = domDocument.getElementsByTagName("SubDB");
		for (int i = 0; i < subDBList.getLength(); i++) {
			subDBElement = (org.w3c.dom.Element)subDBList.item(i);
			subDBIDList.add(subDBElement.getAttribute("ID"));
			subDBUserList.add(subDBElement.getAttribute("User"));
			subDBPasswordList.add(subDBElement.getAttribute("Password"));
			wrkStr = subDBElement.getAttribute("Name");
			if (wrkStr.contains("<CURRENT>")) {
				wrkStr = wrkStr.replace("<CURRENT>", currentFolder);
			}
			subDBNameList.add(wrkStr);
		}
		
		///////////////////
		// DB-Method URL //
		///////////////////
		if (!element.getAttribute("AppServerName").equals("")) {
			appServerName = element.getAttribute("AppServerName");
		}
		if (appServerName.equals("")) {
			try {
				///////////////////////////////////////////////////////////////////////////////
				// Setup committing connections.                                             //
				// Note that default isolation level of JavaDB is TRANSACTION_READ_COMMITTED //
				///////////////////////////////////////////////////////////////////////////////
				connectionManualCommit = DriverManager.getConnection(databaseName, databaseUser, databasePassword);
				connectionManualCommit.setAutoCommit(false);
				connectionAutoCommit = DriverManager.getConnection(databaseName, databaseUser, databasePassword);
				connectionAutoCommit.setAutoCommit(true);

				////////////////////////////////////////////////////////
				// Setup read-only connections for Sub-DB definitions //
				////////////////////////////////////////////////////////
				for (int i = 0; i < subDBIDList.size(); i++) {
					subDBConnection = DriverManager.getConnection(subDBNameList.get(i), subDBUserList.get(i), subDBPasswordList.get(i));
					subDBConnection.setReadOnly(true);
					subDBConnectionList.add(subDBConnection);
				}
			} catch (Exception e) {
				if (e.getMessage().contains("java.net.ConnectException") && databaseName.contains("jdbc:derby://")) {
					JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("SessionError4") + systemName + XFUtility.RESOURCE.getString("SessionError5"));
				} else {
					JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("SessionError6") + databaseName + XFUtility.RESOURCE.getString("SessionError7") + e.getMessage());
				}
				return null;
			}
		}

		/////////////////////////
		// SMTP Configurations //
		/////////////////////////
		smtpHost = element.getAttribute("SmtpHost");
		smtpPort = element.getAttribute("SmtpPort");
		smtpUser = element.getAttribute("SmtpUser");
		smtpPassword = element.getAttribute("SmtpPassword");

		////////////////////
		// PDF print font //
		////////////////////
		org.w3c.dom.Element fontElement;
		BaseFont baseFont;
		NodeList printFontList = domDocument.getElementsByTagName("PrintFont");
		for (int i = 0; i < printFontList.getLength(); i++) {
			fontElement = (org.w3c.dom.Element)printFontList.item(i);
			wrkStr = fontElement.getAttribute("ID");
			try {
				baseFont = BaseFont.createFont(fontElement.getAttribute("PDFFontName"), fontElement.getAttribute("PDFEncoding"), false);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError8") + fontElement.getAttribute("FontName") + XFUtility.RESOURCE.getString("SessionError9"));
				baseFont = BaseFont.createFont("Times-Roman", "Cp1252", false);
			}
			baseFontMap.put(wrkStr, baseFont);
		}

		jTabbedPaneMenu.setFont(new java.awt.Font("SansSerif", 0, 14));
		jTabbedPaneMenu.addKeyListener(new Session_keyAdapter(this));
		jTabbedPaneMenu.requestFocus();
		jTabbedPaneMenu.addChangeListener(new Session_jTabbedPaneMenu_changeAdapter(this));
		jLabelUser.setFont(new java.awt.Font("SansSerif", 0, 14));
		jLabelSession.setFont(new java.awt.Font("SansSerif", 0, 14));

		jEditorPaneNews.setBorder(BorderFactory.createEtchedBorder());
		jEditorPaneNews.setEditable(false);
		jEditorPaneNews.setContentType("text/html");
		jEditorPaneNews.addHyperlinkListener(new Session_jEditorPane_actionAdapter(this));
		jEditorPaneNews.setFocusable(false);
		editorKit.install(jEditorPaneNews);
		jEditorPaneNews.setEditorKit(editorKit);
		jScrollPaneNews.getViewport().add(jEditorPaneNews);
		boolean isValidURL = false;
		if (!welcomePageURL.equals("")) {
			try {
				jEditorPaneNews.setPage(welcomePageURL);
				isValidURL = true;
			} catch (Exception ex) {
			}
		}
		if (!isValidURL) {
			String defaultImageFileName = "";
			if (currentFolder.equals("")) {
				defaultImageFileName = "WelcomePageDefaultImage.jpg";
			} else {
				defaultImageFileName = currentFolder + File.separator + "WelcomePageDefaultImage.jpg";
			}
			File imageFile = new File(defaultImageFileName);
			if (imageFile.exists()) {
				imageIcon = new ImageIcon(defaultImageFileName);
				JLabel labelImage = new JLabel("", imageIcon, JLabel.CENTER);
				jScrollPaneNews.getViewport().add(labelImage);
			} else {
				if (welcomePageURL.equals("")) {
					jEditorPaneNews.setText(XFUtility.RESOURCE.getString("SessionError10"));
				} else {
					jEditorPaneNews.setText(XFUtility.RESOURCE.getString("SessionError11") + welcomePageURL + XFUtility.RESOURCE.getString("SessionError12"));
				}
			}
		}

		jScrollPaneMenu.getViewport().add(jPanelMenu, null);
		jPanelMenuTopMargin.setPreferredSize(new Dimension(15, 15));
		jPanelMenuTopMargin.setOpaque(false);
		jPanelMenuLeftMargin.setPreferredSize(new Dimension(40, 40));
		jPanelMenuLeftMargin.setOpaque(false);
		jPanelMenuRightMargin.setPreferredSize(new Dimension(40, 40));
		jPanelMenuRightMargin.setOpaque(false);
		jPanelMenuBottomMargin.setPreferredSize(new Dimension(10, 10));
		jPanelMenuBottomMargin.setOpaque(false);
		jPanelMenuCenter.setOpaque(false);
		jPanelMenu.setLayout(new BorderLayout());
		jPanelMenu.add(jPanelMenuTopMargin, BorderLayout.NORTH);
		jPanelMenu.add(jPanelMenuLeftMargin, BorderLayout.WEST);
		jPanelMenu.add(jPanelMenuRightMargin, BorderLayout.EAST);
		jPanelMenu.add(jPanelMenuBottomMargin, BorderLayout.SOUTH);
		jPanelMenu.add(jPanelMenuCenter, BorderLayout.CENTER);
		jPanelMenuCenter.setLayout(gridLayoutMenuButtons);
		gridLayoutMenuButtons.setColumns(2);
		gridLayoutMenuButtons.setRows(10);
		gridLayoutMenuButtons.setHgap(80);
		gridLayoutMenuButtons.setVgap(15);
		for (int i = 0; i < 20; i++) {
			jButtonMenuOptionArray[i] = new JButton();
			jButtonMenuOptionArray[i].setFont(new java.awt.Font("SansSerif", 0, 16));
			jButtonMenuOptionArray[i].addActionListener(new Session_jButton_actionAdapter(this));
			jButtonMenuOptionArray[i].addKeyListener(new Session_keyAdapter(this));
		}
		jPanelMenuCenter.add(jButtonMenuOptionArray[0]);
		jPanelMenuCenter.add(jButtonMenuOptionArray[10]);
		jPanelMenuCenter.add(jButtonMenuOptionArray[1]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[11]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[2]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[12]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[3]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[13]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[4]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[14]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[5]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[15]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[6]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[16]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[7]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[17]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[8]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[18]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[9]);
	    jPanelMenuCenter.add(jButtonMenuOptionArray[19]);

		jPanelTop.setPreferredSize(new Dimension(10, 30));
		jPanelTop.setBorder(BorderFactory.createLoweredBevelBorder());
	    jPanelTop.setLayout(new BorderLayout());
	    jPanelTop.add(jLabelUser, BorderLayout.WEST);
	    jPanelTop.add(jLabelSession, BorderLayout.EAST);
	    jScrollPaneMessages.setPreferredSize(new Dimension(10, 40));
	    jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
	    jTextAreaMessages.setFont(new java.awt.Font("SansSerif", 0, 14));
		//jTextAreaMessages.setText(XFUtility.RESOURCE.getString("SessionMessage"));
		jTextAreaMessages.setFocusable(false);
	    jTextAreaMessages.setLineWrap(true);
	    jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
	    jSplitPane2.add(jScrollPaneNews, JSplitPane.TOP);
	    jSplitPane2.add(jTabbedPaneMenu, JSplitPane.BOTTOM);
	    jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
	    jSplitPane1.add(jSplitPane2, JSplitPane.TOP);
	    jSplitPane1.add(jScrollPaneMessages, JSplitPane.BOTTOM);

		this.enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		this.setTitle(systemName + " " + version);
	 	imageTitle = Toolkit.getDefaultToolkit().createImage(xeadDriver.Session.class.getResource("title.png"));
		this.setIconImage(imageTitle);
		screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int shorterWidth1 = Math.round(screenSize.width * (float)0.9);
		int shorterHeight1 = Math.round(shorterWidth1 / 3 * 2);
		int shorterHeight2 = Math.round(screenSize.height * (float)0.9);
		int shorterWidth2 = Math.round(shorterHeight2 / 2 * 3);
		if (shorterWidth2 <= shorterWidth1) {
			this.setPreferredSize(new Dimension(shorterWidth2, shorterHeight2));
			this.setLocation((screenSize.width - shorterWidth2) / 2, (screenSize.height - shorterHeight2) / 2);
		} else {
			this.setPreferredSize(new Dimension(shorterWidth1, shorterHeight1));
			this.setLocation((screenSize.width - shorterWidth1) / 2, (screenSize.height - shorterHeight1) / 2);
		}
		this.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e) {
				jSplitPane2.setDividerLocation(getHeight() / 4);
				jSplitPane1.setDividerLocation(getHeight() - (getHeight() / 7));
			}
        });
	    this.getContentPane().setFocusable(false);
		this.getContentPane().add(jPanelTop, BorderLayout.NORTH);
		this.getContentPane().add(jSplitPane1, BorderLayout.CENTER);
		this.pack();
		this.validate();
		if (screenSize.height < 1000) {
			this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}

	    for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 20; j++) {
		    	menuOptionArray[i][j] = null;
			}
	    }

        digestAdapter = new DigestAdapter("MD5");
	    modifyPasswordDialog = new DialogModifyPassword(this);

	    /////////////////////////
	    // Return Login Dialog //
	    /////////////////////////
		return new DialogLogin(this, user, password);
	}
	
	private void setupSessionAndMenus() throws ScriptException, Exception {
		/////////////////////////////////
		// Setup session no and status //
		/////////////////////////////////
		sessionID = this.getNextNumber("NRSESSION");
		sessionStatus = "ACT";

		//////////////////////////////////////////
		// insert a new record to session table //
		//////////////////////////////////////////
		String sql = "insert into " + sessionTable
		+ " (NRSESSION, IDUSER, DTLOGIN, TXIPADDRESS, KBSESSIONSTATUS) values ("
		+ "'" + sessionID + "'," + "'" + userID + "'," + "CURRENT_TIMESTAMP,"
		+ "'" + getIpAddress() + "','" + sessionStatus + "')";
		XFTableOperator operator = new XFTableOperator(this, null, sql, true);
		operator.execute();

		//////////////////////////////////////
		// setup off date list for calendar //
		//////////////////////////////////////
		operator = new XFTableOperator(this, null, "select * from " + calendarTable, true);
		while (operator.next()) {
			offDateList.add(operator.getValueOf("DTOFF").toString());
		}

		///////////////////////////////////////////////
		// Setup elements on menu and show first tab //
		///////////////////////////////////////////////
		jLabelUser.setText("User " + userName);
		jLabelSession.setText("Session " + sessionID);
		NodeList menuList = domDocument.getElementsByTagName("Menu");
		sortingList = XFUtility.getSortedListModel(menuList, "ID");
		if (userMenus.equals("ALL")) {
			buildMenuWithID("");
		} else {
			StringTokenizer workTokenizer = new StringTokenizer(userMenus, "," );
			while (workTokenizer.hasMoreTokens()) {
				buildMenuWithID(workTokenizer.nextToken());
			}
		}
		setupOptionsOfMenuWithTabNo(0);

		////////////////////////////////////////////////////
		// setup global bindings and execute login-script //
		////////////////////////////////////////////////////
		globalScriptBindings = scriptEngineManager.getBindings();
		globalScriptBindings.put("session", this);
		scriptEngine = scriptEngineManager.getEngineByName("js");
		if (!loginScript.equals("")) {
			scriptEngine.eval(loginScript);
		}
		
		/////////////////////////////////////////////////////////
		// Construct Cross-Checkers to be loaded at logging-in //
		/////////////////////////////////////////////////////////
		org.w3c.dom.Element element;
		for (int i = 0; i < tableList.getLength(); i++) {
			element = (org.w3c.dom.Element)tableList.item(i);
			if (loadingChekerIDList.contains(element.getAttribute("ID"))) {
				ReferChecker checker = new ReferChecker(this, element.getAttribute("ID"), null);
				referCheckerList.add(checker);
			}
		}
	}
	
	private String getIpAddress() {
		String value = "N/A";
		HttpPost httpPost = null;
		HttpClient httpClient = null;

		try {
			InetAddress ip = InetAddress.getLocalHost();
			value = ip.getHostAddress();

			if (!getAppServerName().equals("")) {
				httpPost = new HttpPost(getAppServerName());
				List<NameValuePair> objValuePairs = new ArrayList<NameValuePair>(1);
				objValuePairs.add(new BasicNameValuePair("METHOD", "IP"));
				httpPost.setEntity(new UrlEncodedFormEntity(objValuePairs, "UTF-8"));  

				try {
					httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(httpPost);
					HttpEntity responseEntity = response.getEntity();
					if (responseEntity != null) {
						value = EntityUtils.toString(responseEntity) + " - " + ip.getHostAddress();
					}
				} catch(Exception e) {
				} finally {
					if (httpClient != null) {
						httpClient.getConnectionManager().shutdown();
					}
				}
			}
		} catch(Exception e) {
		} finally {
			if (httpPost != null) {
				httpPost.abort();
			}
		}

		return value;
	}

	public ScriptEngineManager getScriptEngineManager() {
		return scriptEngineManager;
	}
	
	public BaseFont getBaseFontWithID(String id) {
		return baseFontMap.get(id);
	}
	
	private void buildMenuWithID(String id) {
		int optionNo = 0;
		NodeList optionList = null;
		org.w3c.dom.Element menuElement = null;
		org.w3c.dom.Element optionElement = null;
		org.w3c.dom.Element workElement = null;
		int menuIndex;
		StringTokenizer tokenizer;
		String wrkStr;
		//
		for (int i = 0; i < sortingList.getSize(); i++) {
			menuElement = (org.w3c.dom.Element)sortingList.getElementAt(i);
			if (menuElement.getAttribute("ID").equals(id) || id.equals("")) {
				if (jTabbedPaneMenu.getTabCount() == 0) {
					jTabbedPaneMenu.addTab(menuElement.getAttribute("Name"), null, jScrollPaneMenu);
				} else {
					jTabbedPaneMenu.addTab(menuElement.getAttribute("Name"), null, null);
				}
				menuIndex = jTabbedPaneMenu.getTabCount() - 1;
				menuIDArray[menuIndex] = menuElement.getAttribute("ID");
				menuCaptionArray[menuIndex] = menuElement.getAttribute("Name");
				helpURLArray[menuIndex] = menuElement.getAttribute("HelpURL");
				tokenizer = new StringTokenizer(menuElement.getAttribute("CrossCheckersToBeLoaded"), ";" );
				while (tokenizer.hasMoreTokens()) {
					wrkStr = tokenizer.nextToken();
					if (!loadingChekerIDList.contains(wrkStr)) {
						loadingChekerIDList.add(wrkStr);
					}
				}
				//
				optionList = menuElement.getElementsByTagName("Option");
				for (int j = 0; j < optionList.getLength(); j++) {
					optionElement = (org.w3c.dom.Element)optionList.item(j);
					optionNo = Integer.parseInt(optionElement.getAttribute("Index"));
					org.w3c.dom.Element functionElement = null;
					for (int k = 0; k < functionList.getLength(); k++) {
						workElement = (org.w3c.dom.Element)functionList.item(k);
						if (workElement.getAttribute("ID").equals(optionElement.getAttribute("FunctionID"))) {
							functionElement = workElement;
							break;
						}
					}
					menuOptionArray[menuIndex][optionNo] = new MenuOption(functionElement, optionElement.getAttribute("OptionName"));
				}
				menuOptionArray[menuIndex][19] = new MenuOption(null, "LOGOUT");
				//
				if (!id.equals("")) {
					break;
				}
			}
		}
		
	}
	
	public Application getApplication() {
		return application;
	}
	
	public XFOptionDialog getOptionDialog() {
		return optionDialog;
	}
	
	public XFInputDialog getInputDialog() {
		if (inputDialog.isVisible()) {
			return new XFInputDialog(this);
		} else {
			inputDialog.clear();
			return inputDialog;
		}
	}
	
	public XFCheckListDialog getCheckListDialog() {
		return checkListDialog;
	}
	
	public XFLongTextEditor getLongTextEditor() {
		return xfLongTextEditor;
	}
	
	public String getCurrentMenuID() {
		return menuIDArray[jTabbedPaneMenu.getSelectedIndex()];
	}

	public void browseHelp() {
		if (helpURLArray[jTabbedPaneMenu.getSelectedIndex()].equals("")) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError16") + menuCaptionArray[jTabbedPaneMenu.getSelectedIndex()] + XFUtility.RESOURCE.getString("SessionError17"));
		} else {
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				desktop.browse(new URI(helpURLArray[jTabbedPaneMenu.getSelectedIndex()]));
			} catch (URISyntaxException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError18") + menuCaptionArray[jTabbedPaneMenu.getSelectedIndex()] + XFUtility.RESOURCE.getString("SessionError19") + ex.getMessage());
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError18") + menuCaptionArray[jTabbedPaneMenu.getSelectedIndex()] + XFUtility.RESOURCE.getString("SessionError19") + ex.getMessage());
			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError18") + menuCaptionArray[jTabbedPaneMenu.getSelectedIndex()] + XFUtility.RESOURCE.getString("SessionError19") + ex.getMessage());
			} finally {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
	
	void setupOptionsOfMenuWithTabNo(int tabNumber) {
		if (tabNumber > -1) {
			menuIDUsing = menuIDArray[tabNumber];
			jTabbedPaneMenu.requestFocus();
			for (int i = 0; i < 20; i++) {
				if (menuOptionArray[tabNumber][i] == null) {
					jButtonMenuOptionArray[i].setVisible(false);
				} else {
					jButtonMenuOptionArray[i].setText(menuOptionArray[tabNumber][i].getMenuOptionName());
					jButtonMenuOptionArray[i].setVisible(true);
				}
			}
			if (helpURLArray[tabNumber] != null) {
				if (helpURLArray[tabNumber].equals("")) {
					jTextAreaMessages.setText(XFUtility.RESOURCE.getString("SessionMessage1"));
				} else {
					jTextAreaMessages.setText(XFUtility.RESOURCE.getString("SessionMessage2"));
				}
			}
		}
	}

	public String getTimeStamp() {
		return XFUtility.getUserExpressionOfUtilDate(null, "", true);
	}

	public String getToday() {
		return getToday("");
	}

	public String getToday(String dateFormat) {
		return XFUtility.getUserExpressionOfUtilDate(null, dateFormat, false);
	}

	public String getTodayTime(String dateFormat) {
		return XFUtility.getUserExpressionOfUtilDate(null, dateFormat, true);
	}

	public String getThisMonth() {
		SimpleDateFormat dfm = new SimpleDateFormat("yyyyMM");
		Calendar cal = Calendar.getInstance();
		return dfm.format(cal.getTime());
	}
	
	public String getErrorOfAccountDate(String dateValue) {
		String message = "";
		try {
			int yyyyMSeq = getSystemVariantInteger("ALLOWED_FISCAL_MONTH");
			if (yyyyMSeq > 200000 && yyyyMSeq < 210000) {
				String date = dateValue.replace("/", "");
				date = date.replace("-", "");
				if (date.length() >= 8) {
					date = date.substring(0, 8);
					int yyyy = getFYearOfDate(date);
					int mSeq = getMSeqOfDate(dateValue);
					int yyyyMSeqTarget = yyyy * 100 + mSeq;
					if (yyyyMSeqTarget < yyyyMSeq) {
						message = XFUtility.RESOURCE.getString("FunctionError49");
					}
				}
			}
		} catch (NumberFormatException e) {
		} 
		return message;
	}
	
	private String getOffsetTime(String hhmmFrom, int minutes) {
        double days = 0;
        double hh = Double.parseDouble(hhmmFrom.substring(0,2));
        double mm = Double.parseDouble(hhmmFrom.substring(3,5)) + minutes;

        if (mm >= 60) {
        	hh = hh + Math.ceil(mm / 60);
        	mm = mm % 60;
        }
        if (mm <= -60) {
        	hh = hh + Math.ceil(mm / 60);
        	mm = (mm % 60) * -1;
        } else {
        	if (mm < 0) {
        		hh = hh + - 1;
        		mm = mm + 60;
        	}
        }
        
        if (hh >= 24) {
        	days = Math.ceil(hh / 24);
        	hh = hh % 24;
        }
        if (hh <= -24) {
        	days = Math.ceil(hh / 24);
        	hh = (hh % 24) * -1;
        } else {
        	if (hh < 0) {
        		days = days - 1;
        		hh = hh + 24;
        	}
        }
        
        String strDays = Double.toString(days).replace(".0", "");
        String strHH = Double.toString(hh).replace(".0", "");
        if (hh < 10) {
        	strHH = "0" + strHH;
        }
        String strMM = Double.toString(mm).replace(".0", "");
        if (mm < 10) {
        	strMM = "0" + strMM;
        }
        return strDays + ":" + strHH + ":" + strMM;//-days:hh:mm//
	}

	public String getOffsetDateTime(String dateFrom, String timeFrom, int minutes, int countType) {
		String dateTime = "";
		String daysHhMm = getOffsetTime(timeFrom, minutes); //timeFrom format is hh:mm//
		StringTokenizer workTokenizer = new StringTokenizer(daysHhMm, ":" );//-days:hh:mm//
        String date;
		String hh;
		String mm;
		try {
			int days = Integer.parseInt(workTokenizer.nextToken());
			date = getOffsetDate(dateFrom, days, countType);
			hh = workTokenizer.nextToken();
			mm = workTokenizer.nextToken();
			dateTime = date + " " + hh + ":" + mm;
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Failed to get offset date-time.\n" + e.getMessage());
		}
		return dateTime; //yyyy-mm-dd hh:mm//
	}

	public String getOffsetDate(String dateFrom, int days, int countType) {
		String offsetDate = "";
		Date workDate;
		SimpleDateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
		//
		dateFrom = dateFrom.replaceAll("-", "").trim();
		//
        int y = Integer.parseInt(dateFrom.substring(0,4));
        int m = Integer.parseInt(dateFrom.substring(4,6));
        int d = Integer.parseInt(dateFrom.substring(6,8));
		Calendar cal = Calendar.getInstance();
		cal.set(y, m-1, d);
		//
		if (countType == 0) {
			cal.add(Calendar.DATE, days);
		}
		//
		if (countType == 1) {
			for (int i = 0; i < days; i++) {
				cal.add(Calendar.DATE, 1);
				workDate = cal.getTime();
				if (offDateList.contains(dfm.format(workDate))) {
					days++;
				}
			}
		}
		//
		workDate = cal.getTime();
		offsetDate = dfm.format(workDate);
		//
		return offsetDate;
	}

	public String getOffsetYearMonth(String yearMonthFrom, int months) {
		String offsetYearMonth = "";
        try {
			int year = Integer.parseInt(yearMonthFrom.substring(0,4));
			int month = Integer.parseInt(yearMonthFrom.substring(4,6));
			if (months > 0) {
				for (int i = 0; i < months; i++) {
					month++;
					if (month > 12) {
						month = 1;
						year++;
					}
				}
			} else {
				for (int i = 0; i > months; i--) {
					month--;
					if (month < 1) {
						month = 12;
						year--;
					}
				}
			}
			if (month >= 10) {
				offsetYearMonth = Integer.toString(year) + Integer.toString(month);
			} else {
				offsetYearMonth = Integer.toString(year) + "0" + Integer.toString(month);
			}
		} catch (NumberFormatException e) {
		}
		return offsetYearMonth;
	}

	public int getDaysBetweenDates(String strDateFrom, String strDateThru, int countType) {
		int days = 0;
		int y, m, d;
		Date dateFrom, dateThru;
		SimpleDateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		//
		strDateFrom = strDateFrom.replaceAll("-", "").trim();
        y = Integer.parseInt(strDateFrom.substring(0,4));
        m = Integer.parseInt(strDateFrom.substring(4,6));
        d = Integer.parseInt(strDateFrom.substring(6,8));
		cal.set(y, m-1, d, 0, 0, 0);
		dateFrom = cal.getTime();
		//
		strDateThru = strDateThru.replaceAll("-", "").trim();
        y = Integer.parseInt(strDateThru.substring(0,4));
        m = Integer.parseInt(strDateThru.substring(4,6));
        d = Integer.parseInt(strDateThru.substring(6,8));
		cal.set(y, m-1, d, 0, 0, 0);
		dateThru = cal.getTime();
		//
		if (countType == 0) {
			long diff = dateThru.getTime() - dateFrom.getTime();
			days = (int)(diff / 86400000); 
		}
		//
		if (countType == 1) {
			//
			if (dateThru.getTime() == dateFrom.getTime()) {
				days = 0;
			}
			//
			if (dateThru.getTime() > dateFrom.getTime()) {
		        y = Integer.parseInt(strDateFrom.substring(0,4));
		        m = Integer.parseInt(strDateFrom.substring(4,6));
		        d = Integer.parseInt(strDateFrom.substring(6,8));
				cal.set(y, m-1, d, 0, 0, 0);
				long timeThru = dateThru.getTime();
				long timeWork = dateFrom.getTime();
				while (timeThru > timeWork) {
					cal.add(Calendar.DATE, 1);
					if (!offDateList.contains(dfm.format(cal.getTime()))) {
						days++;
					}
					timeWork = cal.getTime().getTime();
				}
			}
			//
			if (dateThru.getTime() < dateFrom.getTime()) {
		        y = Integer.parseInt(strDateThru.substring(0,4));
		        m = Integer.parseInt(strDateThru.substring(4,6));
		        d = Integer.parseInt(strDateThru.substring(6,8));
				cal.set(y, m-1, d, 0, 0, 0);
				long timeWork = dateThru.getTime();
				long timeFrom = dateFrom.getTime();
				while (timeFrom > timeWork) {
					cal.add(Calendar.DATE, 1);
					if (!offDateList.contains(dfm.format(cal.getTime()))) {
						days++;
					}
					timeWork = cal.getTime().getTime();
				}
				days = days * -1;
			}
		}
		//
		return days;
	}

	public boolean isValidDate(String date) {
		boolean result = true;
		String argDate = date.replaceAll("[^0-9]","").trim();
		try {
	        int y = Integer.parseInt(argDate.substring(0,4));
	        int m = Integer.parseInt(argDate.substring(4,6));
	        int d = Integer.parseInt(argDate.substring(6,8));
			calendar.set(y, m-1, d);
			calendar.getTime();
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	public boolean isValidDateFormat(String date, String separator) {
		boolean result = false;
		if (isValidDate(date)
				&& date.length() == 10
				&& date.substring(4, 5).equals(separator)
				&& date.substring(7, 8).equals(separator)) {
			result = true;
		}
		return result;
	}

	public boolean isOffDate(String date) {
		return offDateList.contains(date);
	}

	public boolean isValidTime(String time, String format) {
		boolean result = false;
		try {
			if (format.toUpperCase().equals("HH:MM")) {
				if (time.length() == 5) {
					if (time.substring(2, 3).equals(":")) {
						int hour = Integer.parseInt(time.substring(0,2));
						int min = Integer.parseInt(time.substring(3,5));
						if (hour >= 0 && hour <= 24
								&& min >= 0 && min <= 60) {
							result = true;
						}
					}
				}
			}
			if (format.toUpperCase().equals("HH:MM:SS")) {
				if (time.length() >= 7 && time.length() <= 12) {
					if (time.substring(2, 3).equals(":")
							&& time.substring(5, 6).equals(":")) {
						int hour = Integer.parseInt(time.substring(0,2));
						int min = Integer.parseInt(time.substring(3,5));
						float sec = Float.parseFloat(time.substring(6,time.length()));
						if (hour >= 0 && hour <= 24
								&& min >= 0 && min < 60
								&& sec >= 0 && sec < 60) {
							result = true;
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return result;
	}

	public Object getMinutesBetweenTimes(String timeFrom, String timeThru) {
		int minDiff = 0;
		try {
			if (timeFrom.substring(2, 3).equals(":") && timeThru.substring(2, 3).equals(":")) {
				int hourFrom = Integer.parseInt(timeFrom.substring(0,2));
				int minFrom = Integer.parseInt(timeFrom.substring(3,5));
				int hourThru = Integer.parseInt(timeThru.substring(0,2));
				int minThru = Integer.parseInt(timeThru.substring(3,5));
				minDiff = ((hourThru - hourFrom) * 60) + (minThru - minFrom);
			} else {
				JOptionPane.showMessageDialog(null, "Invalid time format.");
				return null;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Invalid time format.\n" + e.getMessage());
			return null;
		}
		return minDiff;
	}

	public String getNextNumber(String numberID) {
		String nextNumber = "";
		/////////////////////////////////////////////////////////////
		// Getting next number and updating number with counted-up //
		/////////////////////////////////////////////////////////////
		try {
			String sql = "select * from " + numberingTable + " where IDNUMBER = '" + numberID + "'";
			XFTableOperator operator = new XFTableOperator(this, null, sql, true);
			if (operator.next()) {
				nextNumber = getFormattedNextNumber(
						Integer.parseInt(operator.getValueOf("NRCURRENT").toString()),
						Integer.parseInt(operator.getValueOf("NRNUMDIGIT").toString()),
						operator.getValueOf("TXPREFIX").toString().trim(),
						operator.getValueOf("FGWITHCD").toString());
				int number = countUpNumber(
						Integer.parseInt(operator.getValueOf("NRCURRENT").toString()),
						Integer.parseInt(operator.getValueOf("NRNUMDIGIT").toString()));
				sql = "update " + numberingTable + 
				" set NRCURRENT = " + number +
				", UPDCOUNTER = " + (Integer.parseInt(operator.getValueOf("UPDCOUNTER").toString()) + 1) +
				" where IDNUMBER = '" + numberID + "'" +
				" and UPDCOUNTER = " + Integer.parseInt(operator.getValueOf("UPDCOUNTER").toString());
				operator = new XFTableOperator(this, null, sql, true);
				operator.execute();
			} else {
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError13") + numberID + XFUtility.RESOURCE.getString("SessionError14"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return nextNumber;
	}

	public void setNextNumber(String numberID, int nextNumber) {
		try {
			String sql = "select * from " + numberingTable + " where IDNUMBER = '" + numberID + "'";
			XFTableOperator operator = new XFTableOperator(this, null, sql, true);
			if (operator.next()) {
				sql = "update " + numberingTable + 
				" set NRCURRENT = " + nextNumber +
				", UPDCOUNTER = " + (Integer.parseInt(operator.getValueOf("UPDCOUNTER").toString()) + 1) +
				" where IDNUMBER = '" + numberID + "'" +
				" and UPDCOUNTER = " + Integer.parseInt(operator.getValueOf("UPDCOUNTER").toString());
				operator = new XFTableOperator(this, null, sql, true);
				operator.execute();
			} else {
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError13") + numberID + XFUtility.RESOURCE.getString("SessionError14"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getFormattedNextNumber(int number, int digit, String prefix, String withCD) {
		String wrkStr;
		String nextNumber = "";
		int wrkInt, wrkSum;
		//
		DecimalFormat decimalFormat = new DecimalFormat();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < digit; i++) {
			buf.append("0");
		}
		decimalFormat.applyPattern(buf.toString());
		String outputdata = decimalFormat.format(number);
		//
		if (withCD.equals("T")) {
			wrkSum = 0;
			for (int i = 0; i < outputdata.length(); i++) {
				wrkStr = outputdata.substring(i, i+1);
				wrkInt = Integer.parseInt(wrkStr);
				if ((wrkInt % 2) == 1) {
					wrkInt = wrkInt * 3;
				}
				wrkSum = wrkSum + wrkInt;
			}
			wrkInt = wrkSum % 10;
			wrkInt = 10 - wrkInt; //Check Digit with Modulus 10//
			nextNumber = prefix + outputdata + wrkInt;
		} else {
			nextNumber = prefix + outputdata;
		}
		//
		return nextNumber;
	}
		
	private int countUpNumber(int number, int digit) {
		number++;
		if (digit == 1 && number == 10) {
			number = 1;
		}
		if (digit == 2 && number == 100) {
			number = 1;
		}
		if (digit == 3 && number == 1000) {
			number = 1;
		}
		if (digit == 4 && number == 10000) {
			number = 1;
		}
		if (digit == 5 && number == 100000) {
			number = 1;
		}
		if (digit == 6 && number == 1000000) {
			number = 1;
		}
		if (digit == 7 && number == 10000000) {
			number = 1;
		}
		if (digit == 8 && number == 100000000) {
			number = 1;
		}
		if (digit == 9 && number == 1000000000) {
			number = 1;
		}
		return number;
	}
	
	public String getSystemVariantString(String itemID) {
		String strValue = "";
		String sql = "";
		try {
			sql = "select * from " + variantsTable + " where IDVARIANT = '" + itemID + "'";
			XFTableOperator operator = new XFTableOperator(this, null, sql, true);
			if (operator.next()) {
				strValue = operator.getValueOf("TXVALUE").toString().trim();
				if (strValue.contains("<CURRENT>")) {
					strValue = strValue.replace("<CURRENT>", currentFolder);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return strValue;
	}
	
	public int getSystemVariantInteger(String itemID) {
		String strValue = "";
		int intValue = 0;
		try {
			String sql = "select * from " + variantsTable + " where IDVARIANT = '" + itemID + "'";
			XFTableOperator operator = new XFTableOperator(this, null, sql, true);
			if (operator.next()) {
				strValue = operator.getValueOf("TXVALUE").toString().trim();
				if (operator.getValueOf("TXTYPE").toString().trim().equals("NUMBER")) {
					intValue = Integer.parseInt(strValue);
				} else {
					JOptionPane.showMessageDialog(null, "Value of system Variant with ID '" + itemID + "' is not number.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return intValue;
	}
	
	public float getSystemVariantFloat(String itemID) {
		String strValue = "";
		float floatValue = (float)0.0;
		try {
			String sql = "select * from " + variantsTable + " where IDVARIANT = '" + itemID + "'";
			XFTableOperator operator = new XFTableOperator(this, null, sql, true);
			if (operator.next()) {
				strValue = operator.getValueOf("TXVALUE").toString().trim();
				if (operator.getValueOf("TXTYPE").toString().trim().equals("NUMBER")) {
					floatValue = Float.parseFloat(strValue);
				} else {
					JOptionPane.showMessageDialog(null, "Value of system Variant with ID '" + itemID + "' is not number.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return floatValue;
	}

	public void setSystemVariant(String itemID, String value) {
		try {
			String sql = "select * from " + variantsTable + " where IDVARIANT = '" + itemID + "'";
			XFTableOperator operator = new XFTableOperator(this, null, sql, true);
			if (operator.next()) {
				sql = "update " + variantsTable + 
				" set TXVALUE = '" + value + "'" +
				", UPDCOUNTER = " + (Integer.parseInt(operator.getValueOf("UPDCOUNTER").toString()) + 1) +
				" where IDVARIANT = '" + itemID + "'" +
				" and UPDCOUNTER = " + Integer.parseInt(operator.getValueOf("UPDCOUNTER").toString());
				operator = new XFTableOperator(this, null, sql, true);
				operator.execute();
			} else {
				sql = "insert into " + variantsTable
				+ " (IDVARIANT, TXNAME, TXTYPE, TXVALUE) values ("
				+ "'" + itemID + "', '" + itemID.substring(0, 10) + "', 'STRING', '" + value + "')";
				operator = new XFTableOperator(this, null, sql, true);
				operator.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public float getAnnualExchangeRate(String currencyCode, int fYear, String type) {
		float rateReturn = 0;
		//
		if (currencyCode.equals(getSystemVariantString("SYSTEM_CURRENCY"))) {
			rateReturn = 1;
		} else {
			try {
				String sql = "select * from " + exchangeRateAnnualTable
					+ " where KBCURRENCY = '" + currencyCode
					+ "' and DTNEND = " + fYear;
				XFTableOperator operator = new XFTableOperator(this, null, sql, true);
				if (operator.next()) {
					rateReturn = Float.parseFloat(operator.getValueOf("VLRATEM").toString());
					if (type.equals("TTB")) {
						rateReturn = Float.parseFloat(operator.getValueOf("VLRATEB").toString());
					}
					if (type.equals("TTS")) {
						rateReturn = Float.parseFloat(operator.getValueOf("VLRATES").toString());
					}
				} else {
					JOptionPane.showMessageDialog(null, "Annual exchange rate not found for '" + currencyCode + "'," + fYear + ".");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//
		return rateReturn;
	}
	
	public float getMonthlyExchangeRate(String currencyCode, int fYear, int mSeq, String type) {
		float rateReturn = 0;
		//
		if (currencyCode.equals(getSystemVariantString("SYSTEM_CURRENCY"))) {
			rateReturn = 1;
		} else {
			try {
				String sql = "select * from " + exchangeRateMonthlyTable
					+ " where KBCURRENCY = '" + currencyCode
					+ "' and DTNEND = " + fYear
					+ " and DTMSEQ = " + mSeq;
				XFTableOperator operator = new XFTableOperator(this, null, sql, true);
				if (operator.next()) {
					rateReturn = Float.parseFloat(operator.getValueOf("VLRATEM").toString());
					if (type.equals("TTB")) {
						rateReturn = Float.parseFloat(operator.getValueOf("VLRATEB").toString());
					}
					if (type.equals("TTS")) {
						rateReturn = Float.parseFloat(operator.getValueOf("VLRATES").toString());
					}
				}
				if (rateReturn == 0) {
					rateReturn = getAnnualExchangeRate(currencyCode, fYear, type);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//
		return rateReturn;
	}

	public int getTaxAmount(String date, int amount) {
		int fromDate = 0;
		int taxAmount = 0;
		if (!date.equals("") && date != null) {
			int targetDate = Integer.parseInt(date.replaceAll("-", "").replaceAll("/", ""));
			float rate = 0;
			try {
				String sql = "select * from " + taxTable + " order by DTSTART DESC";
				XFTableOperator operator = new XFTableOperator(this, null, sql, true);
				while (operator.next()) {
					fromDate = Integer.parseInt(operator.getValueOf("DTSTART").toString().replaceAll("-", ""));
					if (targetDate >= fromDate) {
						rate = Float.parseFloat(operator.getValueOf("VLTAXRATE").toString());
						break;
					}
				}
				taxAmount = (int)Math.floor(amount * rate);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return taxAmount;
	}

	public int getMSeqOfDate(String parmDate) {
		int mSeq = 0;
		if (!parmDate.equals("")) {
			int month, date;
			parmDate = parmDate.replaceAll("-", "").trim();
			parmDate = parmDate.replaceAll("/", "");
	        month = Integer.parseInt(parmDate.substring(4,6));
	        date = Integer.parseInt(parmDate.substring(6,8));
			//
			boolean isWithinMonth = false;
			int startMonth = 1;
			int lastDay = 31;
			//
			startMonth = getSystemVariantInteger("FIRST_MONTH");
			lastDay = getSystemVariantInteger("LAST_DAY");
			//
			if (lastDay >= 31) {
				if ((month == 1 && date <= 31) 
						|| (month == 2 && date <= 29)
						|| (month == 3 && date <= 31)
						|| (month == 4 && date <= 30)
						|| (month == 5 && date <= 31)
						|| (month == 6 && date <= 30)
						|| (month == 7 && date <= 31)
						|| (month == 8 && date <= 31)
						|| (month == 9 && date <= 30)
						|| (month == 10 && date <= 31)
						|| (month == 11 && date <= 30)
						|| (month == 12 && date <= 31)) {
					isWithinMonth = true;
				}
			}
			if (lastDay == 30) {
				if ((month == 1 && date <= 30) 
						|| (month == 2 && date <= 29)
						|| (month == 3 && date <= 30)
						|| (month == 4 && date <= 30)
						|| (month == 5 && date <= 30)
						|| (month == 6 && date <= 30)
						|| (month == 7 && date <= 30)
						|| (month == 8 && date <= 30)
						|| (month == 9 && date <= 30)
						|| (month == 10 && date <= 30)
						|| (month == 11 && date <= 30)
						|| (month == 12 && date <= 30)) {
					isWithinMonth = true;
				}
			}
			if (lastDay <= 29 && date <= lastDay) {
					isWithinMonth = true;
			}
			//
			if (isWithinMonth) {
				mSeq = month - startMonth + 1;
			} else {
				mSeq = month - startMonth + 2;
			}
			if (mSeq <= 0) {
				mSeq = mSeq + 12;
			}
		}
		return mSeq;
	}

	public int getFYearOfDate(String parmDate) {
		int fYear = 0;
		int mSeq = 0;
		if (!parmDate.equals("") && parmDate != null) {
			int month, date;
			parmDate = parmDate.replaceAll("-", "").trim();
			parmDate = parmDate.replaceAll("/", "");
	        fYear = Integer.parseInt(parmDate.substring(0,4));
	        month = Integer.parseInt(parmDate.substring(4,6));
	        date = Integer.parseInt(parmDate.substring(6,8));
			//
			boolean isWithinMonth = false;
			int startMonth = 1;
			int lastDay = 31;
			//
			startMonth = getSystemVariantInteger("FIRST_MONTH");
			lastDay = (Integer)getSystemVariantInteger("LAST_DAY");
			//
			if (lastDay >= 31) {
				if ((month == 1 && date <= 31) 
						|| (month == 2 && date <= 29)
						|| (month == 3 && date <= 31)
						|| (month == 4 && date <= 30)
						|| (month == 5 && date <= 31)
						|| (month == 6 && date <= 30)
						|| (month == 7 && date <= 31)
						|| (month == 8 && date <= 31)
						|| (month == 9 && date <= 30)
						|| (month == 10 && date <= 31)
						|| (month == 11 && date <= 30)
						|| (month == 12 && date <= 31)) {
					isWithinMonth = true;
				}
			}
			if (lastDay == 30) {
				if ((month == 1 && date <= 30) 
						|| (month == 2 && date <= 29)
						|| (month == 3 && date <= 30)
						|| (month == 4 && date <= 30)
						|| (month == 5 && date <= 30)
						|| (month == 6 && date <= 30)
						|| (month == 7 && date <= 30)
						|| (month == 8 && date <= 30)
						|| (month == 9 && date <= 30)
						|| (month == 10 && date <= 30)
						|| (month == 11 && date <= 30)
						|| (month == 12 && date <= 30)) {
					isWithinMonth = true;
				}
			}
			if (lastDay <= 29 && date <= lastDay) {
					isWithinMonth = true;
			}
			//
			if (isWithinMonth) {
				mSeq = month - startMonth + 1;
			} else {
				mSeq = month - startMonth + 2;
			}
			if (mSeq <= 0) {
				fYear = fYear - 1;
			}
		}
		return fYear;
	}
	
	public String getYearMonthOfFYearMSeq(String fYearMSeq) {
		String resultYear = "";
		String resultMonth = "";
		int workInt;
		int startMonth = getSystemVariantInteger("FIRST_MONTH");
		int fYear = Integer.parseInt(fYearMSeq.substring(0, 4)); 
		int mSeq = Integer.parseInt(fYearMSeq.substring(4, 6)); 
		//
		workInt = startMonth + mSeq - 1;
		if (workInt > 12) {
			workInt = workInt - 12;
			resultMonth = Integer.toString(workInt);
			workInt = fYear + 1;
			resultYear = Integer.toString(workInt);
		} else {
			resultMonth = Integer.toString(workInt);
			workInt = fYear;
			resultYear = Integer.toString(workInt);
		}
		//
		return resultYear + resultMonth;
	}

	void closeSession(boolean isToWriteLogAndClose) {

		///////////////////////
		// Write session log //
		///////////////////////
		if (isToWriteLogAndClose) {
			if (noErrorsOccured) {
				sessionStatus = "END";
			} else {
				sessionStatus = "ERR";
			}
			try {
				String sql = "update " + sessionTable
				+ " set DTLOGOUT=CURRENT_TIMESTAMP, KBSESSIONSTATUS='"
				+ sessionStatus + "' where " + "NRSESSION='" + sessionID + "'";
				XFTableOperator operator = new XFTableOperator(this, null, sql, true);
				operator.execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		////////////////////////////////
		// Commit pending transaction //
		////////////////////////////////
		this.commit(true, null);

		///////////////////////////////////////////
		// Execute login-script to close session //
		///////////////////////////////////////////
		if (scriptEngine != null && !loginScript.equals("")) {
			try {
				scriptEngine.eval(loginScript);
			} catch (ScriptException e) {
				e.printStackTrace();
			}
		}

		///////////////////////////////
		// Close local DB connection //
		///////////////////////////////
		if (appServerName.equals("")) {
			try {
				connectionManualCommit.close();
				connectionAutoCommit.close();
				if (databaseName.contains("jdbc:derby")) {
					DriverManager.getConnection("jdbc:derby:;shutdown=true");
				}
			} catch (SQLException e) {
				if (databaseName.contains("jdbc:derby")
						&& e.getSQLState() != null
						&& !e.getSQLState().equals("XJ015")) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			Object[] bts = {XFUtility.RESOURCE.getString("LogOut"), XFUtility.RESOURCE.getString("Cancel")};
			int rtn = JOptionPane.showOptionDialog(this, XFUtility.RESOURCE.getString("FunctionMessage55"),
					systemName, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
			if (rtn == 0) {
				super.processWindowEvent(e);
				this.closeSession(true);
				this.setVisible(false);
				System.exit(0);
			}
		} else {
			super.processWindowEvent(e);
		}
	}

	void jEditorPane_hyperlinkUpdate(HyperlinkEvent e) {
		if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				desktop.browse(e.getURL().toURI());
			} catch (URISyntaxException ex) {
				ex.printStackTrace();
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	void jButtonMenu_actionPerformed(ActionEvent e) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			//
			Component com = (Component)e.getSource();
			for (int i = 0; i < 20; i++) {
				if (com.equals(jButtonMenuOptionArray[i])) {
					if (menuOptionArray[jTabbedPaneMenu.getSelectedIndex()][i].isLogoutOption) {
						Object[] bts = {XFUtility.RESOURCE.getString("LogOut"), XFUtility.RESOURCE.getString("Cancel")};
						int rtn = JOptionPane.showOptionDialog(this, XFUtility.RESOURCE.getString("FunctionMessage55"),
								systemName, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
						if (rtn == 0) {
							this.closeSession(true);
							this.setVisible(false);
							System.exit(0);
						}
					} else {
						HashMap<String, Object> returnMap = menuOptionArray[jTabbedPaneMenu.getSelectedIndex()][i].call();
						if (returnMap != null && returnMap.get("RETURN_CODE") != null) {
							if (returnMap.get("RETURN_MESSAGE") == null) {
								jTextAreaMessages.setText(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
							} else {
								jTextAreaMessages.setText(returnMap.get("RETURN_MESSAGE").toString());
							}
						}
					}
					break;
				}
			}
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	int writeLogOfFunctionStarted(String functionID, String functionName) {
		sqProgram++;
		//
		try {
			String sql = "insert into " + sessionDetailTable
				+ " (NRSESSION, SQPROGRAM, IDMENU, IDPROGRAM, TXPROGRAM, DTSTART, KBPROGRAMSTATUS) values ("
				+ "'" + this.getSessionID() + "'," + sqProgram + "," + "'" + this.getMenuID() + "'," + "'" + functionID + "'," + "'" + functionName + "'," + "CURRENT_TIMESTAMP,'')";
			XFTableOperator operator = new XFTableOperator(this, null, sql, true);
			operator.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//
		return sqProgram;
	}

	void writeLogOfFunctionClosed(int sqProgramOfFunction, String programStatus, String tableOperationLog, String errorLog) {
		String logString = "";
		StringBuffer bf = new StringBuffer();
		if (errorLog.equals("")) {
			bf.append(tableOperationLog.replace("'", "\""));
			logString = bf.toString();
		} else {
//			int totalLength = tableOperationLog.length() + errorLog.length();
//			if (totalLength > 20000) {
//				String errorLogFileName = "";
//				try {
//					File logFile = createTempFile(sessionID, ".log");
//					errorLogFileName = logFile.getPath();
//					FileWriter fileWriter = new FileWriter(errorLogFileName);
//					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
//					bufferedWriter.write(errorLog);
//					bufferedWriter.flush();
//					bufferedWriter.close();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				bf.append(tableOperationLog.replace("'", "\""));
//				bf.append("\n...Error log follows in the file ");
//				bf.append(errorLogFileName);
//				bf.append(".");
//				logString = bf.toString();
//			} else {
//				bf.append(tableOperationLog.replace("'", "\""));
//				bf.append("\n");
//				bf.append(errorLog.replace("'", "\""));
//				logString = bf.toString();
//			}
			int totalLength = tableOperationLog.length() + errorLog.length();
			if (totalLength > 20000) {
				StringBuffer bf2 = new StringBuffer();
				int wrkInt = 20000 - tableOperationLog.length();
				if (wrkInt > 100) {
					bf2.append(errorLog.substring(0, wrkInt));
					bf2.append(" ...");
					errorLog = bf2.toString();
				} else {
					bf2.append(tableOperationLog.substring(0, 20000));
					bf2.append(" ... Error log was disposed as it is too long.");
					tableOperationLog = bf2.toString();
				}
			}
			bf.append(tableOperationLog.replace("'", "\""));
			bf.append("\n");
			bf.append(errorLog.replace("'", "\""));
			logString = bf.toString();
		}
		//
		if (programStatus.equals("99")) {
			noErrorsOccured = false;
		}
		//
		try {
			String sql = "update " + sessionDetailTable
				+ " set DTEND=CURRENT_TIMESTAMP, KBPROGRAMSTATUS='"
				+ programStatus + "', TXERRORLOG='"
				+ logString + "' where " + "NRSESSION='"
				+ this.getSessionID() + "' and "
				+ "SQPROGRAM=" + sqProgramOfFunction;
			XFTableOperator operator = new XFTableOperator(this, null, sql, true);
			operator.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void menu_keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_F1) {
			browseHelp();
		}
		if (((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
		  && e.getKeyCode() == KeyEvent.VK_T) {
			jTabbedPaneMenu.requestFocus();
		}
		if (e.getKeyCode() == KeyEvent.VK_F12) {
			boolean modified = modifyPasswordDialog.passwordModified();
			if (modified) {
				jTextAreaMessages.setText(XFUtility.RESOURCE.getString("PasswordModified"));
			}
		}
		//
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			Component com = getFocusOwner();
			for (int i = 0; i < 20; i++) {
				if (com.equals(jButtonMenuOptionArray[i])) {
					setFocusOnNextVisibleButton(i, "RIGHT");
					break;
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			Component com = getFocusOwner();
			for (int i = 0; i < 20; i++) {
				if (com.equals(jButtonMenuOptionArray[i])) {
					setFocusOnNextVisibleButton(i, "LEFT");
					break;
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			Component com = getFocusOwner();
			if (com.equals(jTabbedPaneMenu)) {
				for (int i = 0; i < 20; i++) {
					if (jButtonMenuOptionArray[i].isVisible()) {
						jButtonMenuOptionArray[i].requestFocus();
						break;
					}
				}
			} else {
				for (int i = 0; i < 20; i++) {
					if (com.equals(jButtonMenuOptionArray[i])) {
						setFocusOnNextVisibleButton(i, "DOWN");
						break;
					}
				}
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			Component com = getFocusOwner();
			if (com.equals(jButtonMenuOptionArray[10])) {
				jTabbedPaneMenu.requestFocus();
			} else {
				for (int i = 0; i < 20; i++) {
					if (com.equals(jButtonMenuOptionArray[i])) {
						setFocusOnNextVisibleButton(i, "UP");
						break;
					}
				}
			}
		}
	}
	
	void setFocusOnNextVisibleButton(int index, String direction) {
		int i= index;
		boolean anyFound = false;
		//
		if (direction.equals("RIGHT")) {
			if (i >= 0 && i <= 9) {
				i = i + 10;
				if (jButtonMenuOptionArray[i].isVisible()) {
					jButtonMenuOptionArray[i].requestFocus();
				} else {
					while (i != 19) {
						i = i + 1;
						if (jButtonMenuOptionArray[i].isVisible()) {
							jButtonMenuOptionArray[i].requestFocus();
							anyFound = true;
							break;
						}
					}
					if (!anyFound) {
						i = index + 10;
						while (i >= 9) {
							i = i - 1;
							if (jButtonMenuOptionArray[i].isVisible()) {
								jButtonMenuOptionArray[i].requestFocus();
								break;
							}
						}
					}
				}
			}
		}
		//
		if (direction.equals("LEFT")) {
			if (i >= 10 && i <= 19) {
				i = i - 10;
				if (jButtonMenuOptionArray[i].isVisible()) {
					jButtonMenuOptionArray[i].requestFocus();
				} else {
					while (i != 0) {
						i = i - 1;
						if (jButtonMenuOptionArray[i].isVisible()) {
							jButtonMenuOptionArray[i].requestFocus();
							anyFound = true;
							break;
						}
					}
					if (!anyFound) {
						i = index - 10;
						while (i <= 9) {
							i = i + 1;
							if (jButtonMenuOptionArray[i].isVisible()) {
								jButtonMenuOptionArray[i].requestFocus();
								break;
							}
						}
					}
				}
			}
		}
		//
		if (direction.equals("DOWN")) {
			while (i != 9 && i != 19) {
				i = i + 1;
				if (jButtonMenuOptionArray[i].isVisible()) {
					jButtonMenuOptionArray[i].requestFocus();
					break;
				}
			}
		}
		//
		if (direction.equals("UP")) {
			while (i != 0 && i != 10) {
				i = i - 1;
				if (jButtonMenuOptionArray[i].isVisible()) {
					jButtonMenuOptionArray[i].requestFocus();
					anyFound = true;
					break;
				} else {
					if (index > 10 && i ==10) {
						i = i - 1;
						if (jButtonMenuOptionArray[i].isVisible()) {
							jButtonMenuOptionArray[i].requestFocus();
							anyFound = true;
							break;
						}
					}

				}
			}
			if (!anyFound) {
				jTabbedPaneMenu.requestFocus();
			}
		}
	}

	void jTabbedPaneMenu_stateChanged(ChangeEvent e) {
		setupOptionsOfMenuWithTabNo(jTabbedPaneMenu.getSelectedIndex());
	}

	class FunctionLauncher extends Object {
		private XF000[] xF000 = new XF000[10];
		private XF100[] xF100 = new XF100[10];
		private XF110[] xF110 = new XF110[10];
		private XF200[] xF200 = new XF200[10];
		private XF290[] xF290 = new XF290[10];
		private XF300[] xF300 = new XF300[10];
		private XF310[] xF310 = new XF310[10];
		private XF390[] xF390 = new XF390[10];
		private Session session_ = null;
		//
		public FunctionLauncher(Session session) {
			xF000[0] = new XF000(session, 0);
			xF100[0] = new XF100(session, 0);
			xF110[0] = new XF110(session, 0);
			xF200[0] = new XF200(session, 0);
			xF290[0] = new XF290(session, 0);
			xF300[0] = new XF300(session, 0);
			xF310[0] = new XF310(session, 0);
			xF390[0] = new XF390(session, 0);
			session_ = session;
		}
		//
		public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parmMap) {
			HashMap<String, Object> returnMap = new HashMap<String, Object>();
			int countOfRuccursiveCalls = 0;
			//
			if (functionElement == null) {
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError15"));
			} else {
				String functionID = functionElement.getAttribute("ID");
				for (int i = 0; i < 10; i++) {
					if (functionElement.getAttribute("Type").equals("XF000")) {
						if (xF000[i] == null) {
							xF000[i] = new XF000(session_, i);
							returnMap = xF000[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF000[i].isAvailable()) {
								returnMap = xF000[i].execute(functionElement, parmMap);
								break;
							} else {
								if (xF000[i].getFunctionID().equals(functionID)) {
									if (countOfRuccursiveCalls >= 1) {
										JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError20"));
										returnMap.put("RETURN_CODE", "01");
										break;
									} else {
										countOfRuccursiveCalls++;
									}
								}
							}
						}
					}
					if (functionElement.getAttribute("Type").equals("XF100")) {
						if (xF100[i] == null) {
							xF100[i] = new XF100(session_, i);
							returnMap = xF100[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF100[i].isAvailable()) {
								returnMap = xF100[i].execute(functionElement, parmMap);
								break;
							} else {
								if (xF100[i].getFunctionID().equals(functionID)) {
									if (countOfRuccursiveCalls >= 1) {
										JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError20"));
										returnMap.put("RETURN_CODE", "01");
										break;
									} else {
										countOfRuccursiveCalls++;
									}
								}
							}
						}
					}
					if (functionElement.getAttribute("Type").equals("XF110")) {
						if (xF110[i] == null) {
							xF110[i] = new XF110(session_, i);
							returnMap = xF110[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF110[i].isAvailable()) {
								returnMap = xF110[i].execute(functionElement, parmMap);
								break;
							} else {
								if (xF110[i].getFunctionID().equals(functionID)) {
									if (countOfRuccursiveCalls >= 1) {
										JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError20"));
										returnMap.put("RETURN_CODE", "01");
										break;
									} else {
										countOfRuccursiveCalls++;
									}
								}
							}
						}
					}
					if (functionElement.getAttribute("Type").equals("XF200")) {
						if (xF200[i] == null) {
							xF200[i] = new XF200(session_, i);
							returnMap = xF200[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF200[i].isAvailable()) {
								returnMap = xF200[i].execute(functionElement, parmMap);
								break;
							} else {
								if (xF200[i].getFunctionID().equals(functionID)) {
									if (countOfRuccursiveCalls >= 1) {
										JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError20"));
										returnMap.put("RETURN_CODE", "01");
										break;
									} else {
										countOfRuccursiveCalls++;
									}
								}
							}
						}
					}
					if (functionElement.getAttribute("Type").equals("XF290")) {
						if (xF290[i] == null) {
							xF290[i] = new XF290(session_, i);
							returnMap = xF290[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF290[i].isAvailable()) {
								returnMap = xF290[i].execute(functionElement, parmMap);
								break;
							} else {
								if (xF290[i].getFunctionID().equals(functionID)) {
									if (countOfRuccursiveCalls >= 1) {
										JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError20"));
										returnMap.put("RETURN_CODE", "01");
										break;
									} else {
										countOfRuccursiveCalls++;
									}
								}
							}
						}
					}
					if (functionElement.getAttribute("Type").equals("XF300")) {
						if (xF300[i] == null) {
							xF300[i] = new XF300(session_, i);
							returnMap = xF300[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF300[i].isAvailable()) {
								returnMap = xF300[i].execute(functionElement, parmMap);
								break;
							} else {
								if (xF300[i].getFunctionID().equals(functionID)) {
									if (countOfRuccursiveCalls >= 1) {
										JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError20"));
										returnMap.put("RETURN_CODE", "01");
										break;
									} else {
										countOfRuccursiveCalls++;
									}
								}
							}
						}
					}
					if (functionElement.getAttribute("Type").equals("XF310")) {
						if (xF310[i] == null) {
							xF310[i] = new XF310(session_, i);
							returnMap = xF310[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF310[i].isAvailable()) {
								returnMap = xF310[i].execute(functionElement, parmMap);
								break;
							} else {
								if (xF310[i].getFunctionID().equals(functionID)) {
									if (countOfRuccursiveCalls >= 1) {
										JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError20"));
										returnMap.put("RETURN_CODE", "01");
										break;
									} else {
										countOfRuccursiveCalls++;
									}
								}
							}
						}
					}
					if (functionElement.getAttribute("Type").equals("XF390")) {
						if (xF390[i] == null) {
							xF390[i] = new XF390(session_, i);
							returnMap = xF390[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF390[i].isAvailable()) {
								returnMap = xF390[i].execute(functionElement, parmMap);
								break;
							} else {
								if (xF390[i].getFunctionID().equals(functionID)) {
									if (countOfRuccursiveCalls >= 1) {
										JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError20"));
										returnMap.put("RETURN_CODE", "01");
										break;
									} else {
										countOfRuccursiveCalls++;
									}
								}
							}
						}
					}
				}
			}
			//
			return returnMap;
		}
	}

	class MenuOption extends Object {
		private org.w3c.dom.Element functionElement_ = null;
		private String optionName_;
		private boolean isLogoutOption = false;
		//
		public MenuOption(org.w3c.dom.Element functionElement, String optionName) {
			//
			functionElement_ = functionElement;
			//
			if (optionName.equals("")) {
				if (functionElement_ != null) {
					optionName_ = functionElement_.getAttribute("Name");
				}
			} else {
				if (optionName.equals("LOGOUT")) {
					optionName_ = XFUtility.RESOURCE.getString("LogOut");
					isLogoutOption = true;
				} else {
					optionName_ = optionName;
				}
			}
		}
		//
		public String getMenuOptionName() {
			return optionName_;
		}
		//
		public boolean isLogoutOption() {
			return isLogoutOption;
		}
		//
		public HashMap<String, Object> call() {
			if (isLogoutOption) {
				return null;
			} else {
				return functionLauncher.execute(functionElement_, new HashMap<String, Object>());
			}
		}
	}
	
	public String getAppServerName() {
		return appServerName;
	}
	
	public String getSubDBName(String id) {
		return subDBNameList.get(subDBIDList.indexOf(id));
	}
	
	public Connection getConnectionManualCommit() {
		return connectionManualCommit;
	}
	
	public Connection getConnectionAutoCommit() {
		return connectionAutoCommit;
	}

	public Connection getConnectionReadOnly(String id) {
		return subDBConnectionList.get(subDBIDList.indexOf(id));
	}
	
	public void commit() {
		this.commit(true, null);
	}
	
	public void commit(boolean isCommit, StringBuffer logBuf) {
		if (appServerName.equals("")) {
			try {
				if (isCommit) {
					connectionManualCommit.commit();
					if (logBuf != null) {
						XFUtility.appendLog("Local-commit succeeded.", logBuf);
					}
				} else {
					connectionManualCommit.rollback();
					if (logBuf != null) {
						XFUtility.appendLog("Local-rollback succeeded.", logBuf);
					}
				}
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage());
				if (logBuf != null) {
					XFUtility.appendLog(e.getMessage(), logBuf);
				}
			}
		} else {
			HttpClient httpClient = new DefaultHttpClient();
	        HttpPost httpPost = null;
			try {
				httpPost = new HttpPost(appServerName);
				List<NameValuePair> objValuePairs = new ArrayList<NameValuePair>(2);
				objValuePairs.add(new BasicNameValuePair("SESSION", sessionID));
				if (isCommit) {
					objValuePairs.add(new BasicNameValuePair("METHOD", "COMMIT"));
				} else {
					objValuePairs.add(new BasicNameValuePair("METHOD", "ROLLBACK"));
				}
				httpPost.setEntity(new UrlEncodedFormEntity(objValuePairs, "UTF-8"));  
				//
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity responseEntity = response.getEntity();
				if (logBuf != null) {
					if (responseEntity == null) {
						XFUtility.appendLog("Response is NULL.", logBuf);
					} else {
						XFUtility.appendLog(EntityUtils.toString(responseEntity), logBuf);
					}
				}
			} catch (Exception e) {
				String msg = "";
				if (isCommit) {
					msg = "Connection failed to commit with the servlet '" + appServerName + "'";
				} else {
					msg = "Connection failed to rollback with the servlet '" + appServerName + "'";
				}
				JOptionPane.showMessageDialog(null, msg);
				if (logBuf != null) {
					XFUtility.appendLog(e.getMessage(), logBuf);
				}
			} finally {
				httpClient.getConnectionManager().shutdown();
				if (httpPost != null) {
					httpPost.abort();
				}
			}
		}
	}
	
	//public HttpClient getHttpClient() {
	//	return httpClient;
	//}
	
	public ReferChecker createReferChecker(String tableID, XFScriptable function) {
		ReferChecker checker = null;
		for (int i = 0; i < referCheckerList.size(); i++) {
			//if (referCheckerList.get(i).getTargetTableID().equals(tableID)
			//		&& referCheckerList.get(i).getFunction().getFunctionID().equals(function.getFunctionID())) {
			if (referCheckerList.get(i).getTargetTableID().equals(tableID)) {
				checker = referCheckerList.get(i);
				checker.setFunction(function);
				break;
			}
		}
		if (checker == null) {
			checker = new ReferChecker(this, tableID, function);
			if (function != null && !function.isAvailable()) {
				referCheckerList.add(checker);
			}
		}
		return checker;
	}
	
	public String getAddressFromZipNo(String zipNo) {
		String value = "";
        HttpResponse response = null;
    	InputStream inputStream = null;
		HttpClient httpClient = new DefaultHttpClient();
		try {
			httpGet.setURI(new URI(ZIP_URL + "zipcode=" + zipNo + "&format=xml"));
	        response = httpClient.execute(httpGet);  
	        if (response.getStatusLine().getStatusCode() < 400){
	        	inputStream = response.getEntity().getContent();
	        	responseDocParser.parse(new InputSource(inputStream));
    			responseDoc = responseDocParser.getDocument();
    			org.w3c.dom.Element rootNode = (org.w3c.dom.Element)responseDoc.getElementsByTagName("groovewebservice").item(0);
    			if (rootNode.getElementsByTagName("address").getLength() == 0) {
    				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionMessage54") + "\n" + zipNo);
    			} else {
    				org.w3c.dom.Element addressNode = (org.w3c.dom.Element)rootNode.getElementsByTagName("address").item(0);
    				org.w3c.dom.Element prefectureNode = (org.w3c.dom.Element)addressNode.getElementsByTagName("prefecture").item(0);
    				org.w3c.dom.Element cityNode = (org.w3c.dom.Element)addressNode.getElementsByTagName("city").item(0);
    				org.w3c.dom.Element townNode = (org.w3c.dom.Element)addressNode.getElementsByTagName("town").item(0);
    				value = prefectureNode.getTextContent() + cityNode.getTextContent() + townNode.getTextContent();
    			}
	        }  
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionMessage53") + "\n" + ex.getMessage());
		} finally {
			httpClient.getConnectionManager().shutdown();
			try {
				if (inputStream != null) {
					inputStream.close();
				}
            } catch(IOException e) {}
		}
		return value;
	}
	
	public String getDateFormat() {
		return dateFormat;
	}
	
	public void executeProgram(String pgmName) {
		try {
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec(pgmName);
			if (p != null) {
				String result = "";
				StringBuffer buf = new StringBuffer();
				buf.append(pgmName);
				buf.append(" was executed.\n");
				InputStream is = p.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				while ((result = br.readLine()) != null) {
					buf.append(result);
					buf.append("\n");
				}
				JOptionPane.showMessageDialog(null, buf.toString());
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage());
		}	
	}
	
	public void browseFile(String fileName) {
		File file = new File(fileName);
		browseFile(file.toURI());
	}
	
	public void browseFile(URI uri) {
		try {
			desktop.browse(uri);
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage());
		}	
	}
	
	public void editFile(String fileName) {
		try {
			File file = new File(fileName);
			desktop.edit(file);
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage());
		}	
	}
	
	public String getImageFileFolder() {
		return imageFileFolder;
	}
	
	public File createTempFile(String functionID, String extension) throws IOException {
		String header = "XeadDriver_";
		//
		if (extension.equals(".pdf")) {
			header = "PDF_" + header;
		}
		if (extension.equals(".xls")) {
			header = "XLS_" + header;
		}
		if (extension.equals(".log")) {
			header = "LOG_" + header;
		}
		//
		File tempFile = File.createTempFile(header + functionID + "_", extension, outputFolder);
		//
		if (outputFolder == null) {
			tempFile.deleteOnExit();
		}
		//
		return tempFile;
	}

	public XFTableOperator createTableOperator(String oparation, String tableID) {
		XFTableOperator operator = null;
		try {
			operator = new XFTableOperator(this, null, oparation, tableID);
		} catch (Exception e) {
		}
		return operator;
	}

	public XFTableOperator createTableOperator(String sqlText) {
		return new XFTableOperator(this, null, sqlText);
	}

	//DatabaseMetaData getDatabaseMetaData() {
	//	return databaseMetaData;
	//}

	//DatabaseMetaData getDatabaseMetaData(String id) {
	//	DatabaseMetaData metadata;
	//	if (id.equals("")) {
	//		metadata = databaseMetaData;
	//	} else {
	//		metadata = subDBDatabaseMetaDataList.get(subDBIDList.indexOf(id));
	//	}
	//	return metadata;
	//}

	org.w3c.dom.Document getDomDocument() {
		return domDocument;
	}

	public void sendMail(String addressFrom, String addressTo, String addressCc, 
			String subject, String message,
			String fileName, String attachedName, String charset) {
		try{
			Properties props = new Properties();
			props.put("mail.smtp.host", smtpHost);
			props.put("mail.host", smtpHost);
			props.put("mail.from", addressFrom);
			if (!smtpPassword.equals("")) {
				props.setProperty("mail.smtp.auth", "true");
			}
			if (!smtpPort.equals("")) {
				props.put("mail.smtp.port", smtpPort);
			}
			javax.mail.Session mailSession = javax.mail.Session.getDefaultInstance(props, null);
			MimeMessage mailObj = new MimeMessage(mailSession);
			InternetAddress[] toList = new InternetAddress[1];
			toList[0] = new InternetAddress(addressTo);
			mailObj.setRecipients(Message.RecipientType.TO, toList);
			InternetAddress[] ccList = new InternetAddress[1];
			ccList[0] = new InternetAddress(addressCc);
			mailObj.setRecipients(Message.RecipientType.CC, ccList);
			mailObj.setFrom(new InternetAddress(addressFrom));
			mailObj.setSentDate(new Date());
			if (charset.equals("")) {
				mailObj.setSubject(subject);
			} else {
				mailObj.setSubject(subject, charset);
			}
			//
			MimeBodyPart bodyMessage = new MimeBodyPart();
			MimeBodyPart bodyAttachedFile = new MimeBodyPart();
			if (charset.equals("")) {
				bodyMessage.setText(message);
			} else {
				bodyMessage.setText(message, charset);
			}
			if (!fileName.equals("")) {
				FileDataSource fds = new FileDataSource(fileName);
				bodyAttachedFile.setDataHandler(new DataHandler(fds));
				if (!attachedName.equals("")) {
					if (charset.equals("")) {
						bodyAttachedFile.setFileName(MimeUtility.encodeWord(attachedName));
					} else {
						bodyAttachedFile.setFileName(MimeUtility.encodeWord(attachedName, charset, "B"));
					}
				}
			}
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(bodyMessage);
			if (!fileName.equals("")) {
				multipart.addBodyPart(bodyAttachedFile);
			}
			mailObj.setContent(multipart);
			//
			if (smtpPassword.equals("")) {
				Transport.send(mailObj);
			} else {
				Transport tp = mailSession.getTransport("smtp");
				tp.connect(smtpHost, smtpUser, smtpPassword);
				tp.sendMessage(mailObj, toList);
			}
        }catch(Exception e){
			JOptionPane.showMessageDialog(null, "Sending mail with subject '" + subject + "' failed.\n\n" + e.getMessage());
		}
	}
	
	public HashMap<String, Object> executeFunction(String functionID, HashMap<String, Object> parmMap) throws Exception {
		HashMap<String, Object> returnMap = null;
		org.w3c.dom.Element element, elementOfFunction = null;
		//
		for (int k = 0; k < functionList.getLength(); k++) {
			element = (org.w3c.dom.Element)functionList.item(k);
			if (element.getAttribute("ID").equals(functionID)) {
				elementOfFunction = element;
				break;
			}
		}
		//
		if (elementOfFunction == null) {
			throw new Exception(XFUtility.RESOURCE.getString("FunctionError9") + functionID + XFUtility.RESOURCE.getString("FunctionError10"));
		} else {
			returnMap = functionLauncher.execute(elementOfFunction, parmMap);
		}
		//
		return returnMap;
	}

	Desktop getDesktop() {
		return desktop;
	}

	DigestAdapter getDigestAdapter() {
		return digestAdapter;
	}
	
	String getSystemName() {
		return systemName;
	}
	
	String getVersion() {
		return version;
	}

	String getTableNameOfUser() {
		return userTable;
	}

	String getTableNameOfVariants() {
		return variantsTable;
	}

	String getTableNameOfUserVariants() {
		return userVariantsTable;
	}

	String getTableNameOfNumbering() {
		return numberingTable;
	}

	String getTableNameOfCalendar() {
		return calendarTable;
	}

	String getMenuID() {
		return menuIDUsing;
	}

	Rectangle getMenuRectangle() {
		return new Rectangle(this.getLocation().x, this.getLocation().y, this.getWidth(), this.getHeight());
	}

	int getNextSQPROGRAM() {
		sqProgram++;
		return sqProgram;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public String getDatabaseUser() {
		return databaseUser;
	}
	
	public void compressTable(String tableID) throws Exception {
		StringBuffer statementBuf;
		org.w3c.dom.Element element;
		Statement statement = null;
		HttpPost httpPost = null;
		String msg = "";
		//
		if (databaseName.contains("jdbc:derby:")) {
			try {
				if (appServerName.equals("")) {
					statement = connectionManualCommit.createStatement();
				}
				//
				for (int i = 0; i < tableList.getLength(); i++) {
					//
					element = (org.w3c.dom.Element)tableList.item(i);
					if (element.getAttribute("ID").startsWith(tableID) || tableID.equals("")) {
						//
						statementBuf = new StringBuffer();
						statementBuf.append("CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE('");
						statementBuf.append(databaseUser);
						statementBuf.append("', '") ;
						statementBuf.append(element.getAttribute("ID"));
						statementBuf.append("', 1)") ;
						//
						//////////////////////////////////////
						// Execute procedure by auto-commit //
						//////////////////////////////////////
						if (appServerName.equals("")) {
							statement.executeUpdate(statementBuf.toString());
						} else {
							HttpClient httpClient = new DefaultHttpClient();
							try {
								httpPost = new HttpPost(appServerName);
								List<NameValuePair> objValuePairs = new ArrayList<NameValuePair>(1);
								objValuePairs.add(new BasicNameValuePair("METHOD", statementBuf.toString()));
								httpPost.setEntity(new UrlEncodedFormEntity(objValuePairs, "UTF-8"));  
								HttpResponse response = httpClient.execute(httpPost);
								HttpEntity responseEntity = response.getEntity();
								if (responseEntity == null) {
									msg = "Compressing table " + element.getAttribute("ID") + " failed.";
									JOptionPane.showMessageDialog(null, msg);
									throw new Exception(msg);
								}
							} finally {
								httpClient.getConnectionManager().shutdown();
								if (httpPost != null) {
									httpPost.abort();
								}
							}
						}
					}
				}
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Compressing table " + tableID + " failed.\n" + e.getMessage());
				throw new Exception(e.getMessage());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Compressing table " + tableID + " failed.\n" + e.getMessage());
				throw new Exception(e.getMessage());
			} finally {
				if (appServerName.equals("")) {
					try {
						if (statement != null) {
							statement.close();
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public String getSessionID() {
		return sessionID;
	}

	public String getStatus() {
		return sessionStatus;
	}

	public String getUserID() {
		return userID;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserEmployeeNo() {
		return userEmployeeNo;
	}

	public String getUserEmailAddress() {
		return userEmailAddress;
	}
	
	public String getAttribute(String id) {
		return attributeMap.get(id);
	}
	
	public void setAttribute(String id, String value) {
		attributeMap.put(id.trim(), value.trim());
	}

	public NodeList getFunctionList() {
		return functionList;
	}

	public String getScriptFunctions() {
		return scriptFunctions;
	}

	public String getFunctionName(String functionID) {
		org.w3c.dom.Element workElement;
		String functionName = "";
		//
		for (int k = 0; k < functionList.getLength(); k++) {
			workElement = (org.w3c.dom.Element)functionList.item(k);
			if (workElement.getAttribute("ID").equals(functionID)) {
				functionName = workElement.getAttribute("Name");
				break;
			}
		}
		//
		return functionName;
	}
	
	org.w3c.dom.Element getTablePKElement(String tableID) {
		org.w3c.dom.Element element1, element2;
		org.w3c.dom.Element element3 = null;
		NodeList nodeList;
		for (int i = 0; i < tableList.getLength(); i++) {
			element1 = (org.w3c.dom.Element)tableList.item(i);
			if (element1.getAttribute("ID").equals(tableID)) {
				nodeList = element1.getElementsByTagName("Key");
				for (int j = 0; j < nodeList.getLength(); j++) {
					element2 = (org.w3c.dom.Element)nodeList.item(j);
					if (element2.getAttribute("Type").equals("PK")) {
						element3 = element2;
						break;
					}
				}
				break;
			}
		}
		return element3;
	}
	
	org.w3c.dom.Element getTableElement(String tableID) {
		org.w3c.dom.Element element1;
		org.w3c.dom.Element element2 = null;
		for (int i = 0; i < tableList.getLength(); i++) {
			element1 = (org.w3c.dom.Element)tableList.item(i);
			if (element1.getAttribute("ID").equals(tableID)) {
				element2 = element1;
				break;
			}
		}
		return element2;
	}
	
	public String getTableName(String tableID) {
		String tableName = "";
		org.w3c.dom.Element element1;
		org.w3c.dom.Element element2 = null;
		for (int i = 0; i < tableList.getLength(); i++) {
			element1 = (org.w3c.dom.Element)tableList.item(i);
			if (element1.getAttribute("ID").equals(tableID)) {
				element2 = element1;
				tableName = element2.getAttribute("Name");
				break;
			}
		}
		return tableName;
	}
	
	public NodeList getTableNodeList() {
		return tableList;
	}
	
	public org.w3c.dom.Element getFieldElement(String tableID, String fieldID) {
		org.w3c.dom.Element element1, element2;
		org.w3c.dom.Element element3 = null;
		NodeList nodeList;
		for (int i = 0; i < tableList.getLength(); i++) {
			element1 = (org.w3c.dom.Element)tableList.item(i);
			if (element1.getAttribute("ID").equals(tableID)) {
				nodeList = element1.getElementsByTagName("Field");
				for (int j = 0; j < nodeList.getLength(); j++) {
					element2 = (org.w3c.dom.Element)nodeList.item(j);
					if (element2.getAttribute("ID").equals(fieldID)) {
						element3 = element2;
						break;
					}
				}
				break;
			}
		}
		return element3;
	}
}

class DigestAdapter {
    private MessageDigest digest_;

    public DigestAdapter(String algorithm) throws NoSuchAlgorithmException {
        digest_ = MessageDigest.getInstance(algorithm);
    }

    public synchronized String digest(String str) {
        return toHexString(digestArray(str));
    }

    public synchronized byte[] digestArray(String str) {
        byte[] hash = digest_.digest(str.getBytes());
        digest_.reset();
        return hash;
    }

    private String toHexString(byte[] arr) {
        StringBuffer buff = new StringBuffer(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String b = Integer.toHexString(arr[i] & 0xff);
            if (b.length() == 1) {
                buff.append("0");
            }
            buff.append(b);
        }
        return buff.toString();
    }
}

class Session_jButton_actionAdapter implements java.awt.event.ActionListener {
	  Session adaptee;
	  Session_jButton_actionAdapter(Session adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void actionPerformed(ActionEvent e) {
	    adaptee.jButtonMenu_actionPerformed(e);
	  }
}

class Session_jEditorPane_actionAdapter implements javax.swing.event.HyperlinkListener {
	  Session adaptee;
	  Session_jEditorPane_actionAdapter(Session adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void hyperlinkUpdate(HyperlinkEvent e) {
	    adaptee.jEditorPane_hyperlinkUpdate(e);
	  }
}

class Session_keyAdapter extends java.awt.event.KeyAdapter {
	  Session adaptee;
	  Session_keyAdapter(Session adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void keyPressed(KeyEvent e) {
	    adaptee.menu_keyPressed(e);
	  }
}

class Session_jTabbedPaneMenu_changeAdapter  implements ChangeListener {
	Session adaptee;
	Session_jTabbedPaneMenu_changeAdapter(Session adaptee) {
		this.adaptee = adaptee;
	}
	public void stateChanged(ChangeEvent e) {
		adaptee.jTabbedPaneMenu_stateChanged(e);
	}
}

