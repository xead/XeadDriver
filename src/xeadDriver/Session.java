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
import java.awt.image.BufferedImage;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
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
import javax.imageio.ImageIO;
import javax.mail.*;
import javax.mail.internet.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class Session extends JFrame {
	private static final long serialVersionUID = 1L;
	private String systemName = "";
	private String systemVersion = "";
	private String sessionID = "";
	private String sessionStatus = "";
	private String processorVersion = "";
	private boolean noErrorsOccured = true;
	private boolean skipPreload = false;
	private boolean isClientSession = true;
	private String databaseName = "";
	private String databaseUser = "";
	private String databasePassword = "";
	private String appServerName = "";
	private String fileName = "";
	private String userID = "";
	private String userName = "";
	private String userEmployeeNo = "";
	private String userEmailAddress = "";
	private String digestAlgorithmForUser = "MD5";
	private int countOfExpandForUser = 1;
	private boolean isValueSaltedForUser = false;
	private String userMenus = "";
	private String userTable = "";
	private String userFilterValueTable = "";
	private String variantsTable = "";
	private String userVariantsTable = "";
	private String sessionTable = "";
	private String sessionDetailTable = "";
	private String numberingTable = "";
	private String calendarTable = "";
	public String taxTable = "";
	private String exchangeRateAnnualTable = "";
	private String exchangeRateMonthlyTable = "";
	private String menuIDUsing = "";
	private String imageFileFolder = "";
	private File outputFolder = null;
	private String welcomePageURL = "";
	private String dateFormat = "";
	public String systemFont = "";
	private int sqProgram = 0;
	private String currentFolder = "";
	private String loginScript = "";
	private String scriptFunctions = "";
	private String smtpHost = "";
	private String smtpPort = "";
	private String smtpUser = "";
	private String smtpPassword = "";
	private String smtpAdminEmail = "";

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
	private ArrayList<String> loadingFunctionIDList = new ArrayList<String>();
	private XFCalendar xfCalendar = null;
	public JFileChooser jFileChooser = new JFileChooser();

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
	private DialogLogin loginDialog = null;
	private DialogModifyPassword modifyPasswordDialog = null;
	private DialogCheckRead checkReadDialog = null;
	private FunctionLauncher functionLauncher = null;
	private SortableDomElementListModel sortingList;
	private NodeList functionList = null;
	private NodeList tableList = null;
	private ArrayList<String> offDateList = new ArrayList<String>();
	private HashMap<String, BaseFont> baseFontMap = new HashMap<String, BaseFont>();
	private HashMap<String, String> attributeMap = new HashMap<String, String>();
	private ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
	private Bindings globalScriptBindings = null;
	private ScriptEngine scriptEngine = null;
	private static final String ZIP_URL = "http://zip.cgis.biz/xml/zip.php?";
	private DOMParser domParser = new DOMParser();
	private org.w3c.dom.Document responseDoc = null;
	private HttpGet httpGet = new HttpGet();
	private ArrayList<XFExecutable> preloadedFunctionList = new ArrayList<XFExecutable>();
	private Application application = null;
	private XFOptionDialog optionDialog = null;
	private XFInputDialog inputDialog = null;
	private XFCheckListDialog checkListDialog = null;
	private XFLongTextEditor xfLongTextEditor = null;

	//////////////////////////////
	// Construct Online Session //
	//////////////////////////////
	public Session(String[] args, Application app) {
		String loginUser = "";
		String loginPassword = "";
		application = app;

		////////////////////////
		// Java Version Check //
		////////////////////////
		String version = System.getProperty("java.version");
		if (!version.startsWith("1.7.") && !version.startsWith("1.8.")) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("JavaVersionError1") + version + XFUtility.RESOURCE.getString("JavaVersionError2"));
			System.exit(0);
		}

		///////////////////////////////////////////
		// Check the parameters to setup session //
		///////////////////////////////////////////
		try {
			if (args.length >= 1) {
				fileName =  args[0];
			}
			if (args.length >= 2) {
				if (args[1].equals("SKIP_PRELOAD")) {
					skipPreload = true;
				} else {
					loginUser =  args[1];
				}
			}
			if (args.length >= 3) {
				if (args[2].equals("SKIP_PRELOAD")) {
					skipPreload = true;
				} else {
					loginPassword =  args[2];
				}
			}
			if (args.length >= 4) {
				if (args[3].equals("SKIP_PRELOAD")) {
					skipPreload = true;
				}	
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
				if (parseSystemDefinition(fileName)) {
					if (setupSessionVariants()) {

						application.setTextOnSplash(XFUtility.RESOURCE.getString("SplashMessage6"));
						loginDialog = new DialogLogin(this, loginUser, loginPassword);
						if (loginDialog.userIsValidated(true)) {

							userID = loginDialog.getUserID();
							userName = loginDialog.getUserName();
							userEmployeeNo = loginDialog.getUserEmployeeNo();
							userEmailAddress = loginDialog.getUserEmailAddress();
							userMenus = loginDialog.getUserMenus();
							processorVersion = "D" + DialogAbout.VERSION;

							writeLogAndStartSession();
							setupMenusAndComponents();

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
					} else {
						System.exit(0);
					}
				} else {
					System.exit(0);
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

	///////////////////////////////////
	// Construct WEB-Service Session //
	///////////////////////////////////
	public Session(String fileName, String user, String password, String xeadServerVersion) throws Exception {
		try {
			isClientSession = false;

			if (parseSystemDefinition(fileName)) {
				if (setupSessionVariants()) {

					loginDialog = new DialogLogin(this, user, password);
					if (loginDialog.userIsValidated(false)) {

						userID = loginDialog.getUserID();
						userName = loginDialog.getUserName();
						userEmployeeNo = loginDialog.getUserEmployeeNo();
						userEmailAddress = loginDialog.getUserEmailAddress();
						userMenus = "";
						menuIDUsing = "**";
						processorVersion = "S" + xeadServerVersion;

						writeLogAndStartSession();

					} else {
						throw new Exception("User or password is invalid.");
					}
				} else {
					throw new Exception("System definition file is invalid to setup session.");
				}
			} else {
				throw new Exception("Failed to parse the system definition file.");
			}

		} catch(Exception e) {
			noErrorsOccured = false;
			closeSession(false);
			System.exit(0);
			throw e;
		}
	}

	////////////////////////////////////////////////////////////////
	// Parse XML formatted data into DOM with file name requested //
	////////////////////////////////////////////////////////////////
	private boolean parseSystemDefinition(String fileName) throws Exception {
		if (application != null) {
			application.setTextOnSplash(XFUtility.RESOURCE.getString("SplashMessage1"));
			application.setProgressMax(5);
			application.setProgressValue(1);
		}

		if (fileName.startsWith("http:")
				|| fileName.startsWith("https:")
				|| fileName.startsWith("file:")) {
			try {
				URL url = new URL(fileName);
				URLConnection connection = url.openConnection();
				InputStream inputStream = connection.getInputStream();
				domParser.parse(new InputSource(inputStream));
				domDocument = domParser.getDocument();
				return true;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("SessionError2") + fileName + XFUtility.RESOURCE.getString("SessionError3") + "\n" + e.getMessage());
				return false;
			}
		} else {
			File xeafFile = new File(fileName);
			if (xeafFile.exists()) {
				currentFolder = xeafFile.getParent();
				try {
					domParser.parse(new InputSource(new FileInputStream(fileName)));
					domDocument = domParser.getDocument();
					return true;
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("SessionError2") + fileName + XFUtility.RESOURCE.getString("SessionError3") + "\n" + e.getMessage());
					return false;
				}
			} else {
				JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("SessionError21") + fileName + XFUtility.RESOURCE.getString("SessionError22"));
				return false;
			}
		}
	}

	///////////////////////////////////////////////////////////
	// Setup session variants according to system definition //
	///////////////////////////////////////////////////////////
	private boolean setupSessionVariants() throws Exception {
		if (application != null) {
			application.setProgressValue(2);
		}

		NodeList nodeList = domDocument.getElementsByTagName("System");
		org.w3c.dom.Element element = (org.w3c.dom.Element)nodeList.item(0);
		systemName = element.getAttribute("Name");
		systemVersion = element.getAttribute("Version");
		welcomePageURL = element.getAttribute("WelcomePageURL");
		dateFormat = element.getAttribute("DateFormat");
		systemFont = element.getAttribute("SystemFont");
		if (systemFont.equals("")) {
			systemFont = "SanSerif";
		}
		calendar.setLenient(false);

		/////////////////
		// Hash Format //
		/////////////////
		String hashFormat = element.getAttribute("HashFormat");
		if (!hashFormat.equals("")) {
			try {
				StringTokenizer workTokenizer = new StringTokenizer(hashFormat, ";" );
				digestAlgorithmForUser = workTokenizer.nextToken(); //Default:MD5
				if (!digestAlgorithmForUser.equals("MD5")) {
					new DigestAdapter(digestAlgorithmForUser);
				}
				if (workTokenizer.hasMoreTokens()) {
					countOfExpandForUser = Integer.parseInt(workTokenizer.nextToken()); //Default:1
				}
				if (workTokenizer.hasMoreTokens()) {
					isValueSaltedForUser = Boolean.parseBoolean(workTokenizer.nextToken()); //Default:false
				}
			} catch (Exception e) {}
		}
		
		/////////////
		// Dialogs //
		/////////////
		optionDialog = new XFOptionDialog(this);
		inputDialog = new XFInputDialog(this);
		checkListDialog = new XFCheckListDialog(this);
		xfLongTextEditor = new XFLongTextEditor(this);
		if (application != null) {
			application.setProgressValue(3);
		}
		
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
		if (application != null) {
			application.setProgressValue(4);
		}

		///////////////////////////
		// System control tables //
		///////////////////////////
		userTable = element.getAttribute("UserTable");
		userFilterValueTable = element.getAttribute("UserFilterValueTable");
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
		databaseName = databaseName.replace("<CURRENT>", currentFolder);
		databaseUser = element.getAttribute("DatabaseUser");
		databasePassword = element.getAttribute("DatabasePassword");
		org.w3c.dom.Element subDBElement;
		NodeList subDBList = domDocument.getElementsByTagName("SubDB");
		for (int i = 0; i < subDBList.getLength(); i++) {
			subDBElement = (org.w3c.dom.Element)subDBList.item(i);
			subDBIDList.add(subDBElement.getAttribute("ID"));
			subDBUserList.add(subDBElement.getAttribute("User"));
			subDBPasswordList.add(subDBElement.getAttribute("Password"));
			wrkStr = subDBElement.getAttribute("Name");
			wrkStr = wrkStr.replace("<CURRENT>", currentFolder);
			subDBNameList.add(wrkStr);
		}


		///////////////////
		// DB-Method URL //
		///////////////////
		if (!element.getAttribute("AppServerName").equals("")) {
			appServerName = element.getAttribute("AppServerName");
		}
		if (appServerName.equals("")) {
			boolean isOkay = setupConnectionToDatabase(true);
			if (!isOkay) {
				return false;
			}
		}

		/////////////////////////
		// SMTP Configurations //
		/////////////////////////
		smtpHost = element.getAttribute("SmtpHost");
		smtpPort = element.getAttribute("SmtpPort");
		smtpUser = element.getAttribute("SmtpUser");
		smtpPassword = element.getAttribute("SmtpPassword");
		smtpAdminEmail = element.getAttribute("SmtpAdminEmail");
		if (application != null) {
			application.setProgressValue(5);
		}

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

		////////////////////////////////////////
		// Return if variants setup succeeded //
		////////////////////////////////////////
		return true;
	}

	public boolean setupConnectionToDatabase(boolean isToStartSession) {
		String dbName = "";
		try {
			///////////////////////////////////////////////////////////////////////////////
			// Setup committing connections.                                             //
			// Note that default isolation level of JavaDB is TRANSACTION_READ_COMMITTED //
			///////////////////////////////////////////////////////////////////////////////
			dbName = databaseName;
			XFUtility.loadDriverClass(databaseName);
			connectionManualCommit = DriverManager.getConnection(databaseName, databaseUser, databasePassword);
			connectionManualCommit.setAutoCommit(false);
			connectionAutoCommit = DriverManager.getConnection(databaseName, databaseUser, databasePassword);
			connectionAutoCommit.setAutoCommit(true);

			////////////////////////////////////////////////////////
			// Setup read-only connections for Sub-DB definitions //
			////////////////////////////////////////////////////////
			Connection subDBConnection = null;
			subDBConnectionList.clear();
			for (int i = 0; i < subDBIDList.size(); i++) {
				dbName = subDBNameList.get(i);
				XFUtility.loadDriverClass(subDBNameList.get(i));
				subDBConnection = DriverManager.getConnection(subDBNameList.get(i), subDBUserList.get(i), subDBPasswordList.get(i));
				subDBConnection.setReadOnly(true);
				subDBConnectionList.add(subDBConnection);
			}
			return true;

		} catch (Exception e) {
			if (isToStartSession) {
//				if (e.getMessage() != null && e.getMessage().contains("java.net.ConnectException") && databaseName.contains("derby:")) {
//					JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("SessionError4") + systemName + XFUtility.RESOURCE.getString("SessionError5"));
//				} else {
					JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("SessionError6") + dbName + XFUtility.RESOURCE.getString("SessionError7") + "Message:" + e.getMessage());
//				}
			}
			return false;
		}
	}

	
	private void writeLogAndStartSession() throws ScriptException, Exception {
		if (application != null) {
			application.setTextOnSplash(XFUtility.RESOURCE.getString("SplashMessage5"));
			application.setProgressMax(5);
			application.setProgressValue(1);
		}

		/////////////////////////////////
		// Setup session no and status //
		/////////////////////////////////
		sessionID = this.getNextNumber("NRSESSION");
		sessionStatus = "ACT";
		if (application != null) {
			application.setProgressValue(2);
		}

		//////////////////////////////////////////
		// insert a new record to session table //
		//////////////////////////////////////////
		String sql = "insert into " + sessionTable
		+ " (NRSESSION, IDUSER, DTLOGIN, TXIPADDRESS, VLVERSION, KBSESSIONSTATUS) values ("
		+ "'" + sessionID + "'," + "'" + userID + "'," + "CURRENT_TIMESTAMP,"
		+ "'" + getIpAddress() + "','" + processorVersion + "','" + sessionStatus + "')";
		XFTableOperator operator = new XFTableOperator(this, null, sql, true);
		operator.execute();
		if (application != null) {
			application.setProgressValue(3);
		}

		//////////////////////////////////////
		// setup off date list for calendar //
		//////////////////////////////////////
		operator = new XFTableOperator(this, null, "select * from " + calendarTable, true);
		while (operator.next()) {
			offDateList.add(operator.getValueOf("KBCALENDAR").toString() + ";" +operator.getValueOf("DTOFF").toString());
		}
		if (application != null) {
			application.setProgressValue(4);
		}

		/////////////////////////////
		// setup function launcher //
		/////////////////////////////
		functionLauncher = new FunctionLauncher(this);
		if (application != null) {
			application.setProgressValue(5);
		}

		////////////////////////////////////////////////////
		// setup global bindings and execute login-script //
		////////////////////////////////////////////////////
		globalScriptBindings = scriptEngineManager.getBindings();
		globalScriptBindings.put("session", this);
		scriptEngine = scriptEngineManager.getEngineByName("js");
		if (!loginScript.equals("")) {
			scriptEngine.eval(loginScript);
		}
	}

	private void setupMenusAndComponents() throws Exception {
		if (application != null) {
			application.setTextOnSplash(XFUtility.RESOURCE.getString("SplashMessage2"));
			application.setProgressMax(5);
			application.setProgressValue(0);
		}

		jTabbedPaneMenu.setFont(new java.awt.Font(systemFont, 0, XFUtility.FONT_SIZE+2));
		jTabbedPaneMenu.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		jTabbedPaneMenu.addKeyListener(new Session_keyAdapter(this));
		jTabbedPaneMenu.requestFocus();
		jTabbedPaneMenu.addChangeListener(new Session_jTabbedPaneMenu_changeAdapter(this));
		jLabelUser.setFont(new java.awt.Font(systemFont, 0, XFUtility.FONT_SIZE));
		jLabelSession.setFont(new java.awt.Font(systemFont, 0, XFUtility.FONT_SIZE));

		screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		jEditorPaneNews.setBorder(BorderFactory.createEtchedBorder());
		jEditorPaneNews.setFont(new java.awt.Font(systemFont, 0, XFUtility.FONT_SIZE));
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
				final URLConnection connection = new URL(welcomePageURL).openConnection();
			    connection.connect();
				isValidURL = true;
			} catch (Exception ex) {
			}
		}
		if (isValidURL) {
			jEditorPaneNews.setPage(welcomePageURL);
		} else{
			String defaultImageFileName = "";
			if (currentFolder.equals("")) {
				defaultImageFileName = "WelcomePageDefaultImage.jpg";
			} else {
				defaultImageFileName = currentFolder + File.separator + "WelcomePageDefaultImage.jpg";
			}
			File imageFile = new File(defaultImageFileName);
			if (imageFile.exists()) {
				BufferedImage image = ImageIO.read(imageFile);
				if (image.getWidth() > Math.round(screenSize.width * 0.8)) {
					imageIcon = new ImageIcon(image);
					JLabel labelImage = new JLabel("", imageIcon, JLabel.CENTER);
					jScrollPaneNews.getViewport().add(labelImage);
				} else {
					BufferedImage normalLightImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
					Color c;
					int rgb, a, r, g, b;
					for(int y=0; y < image.getHeight(); y++) {
						for(int x=0; x < image.getWidth(); x++) {
							rgb = image.getRGB(x, y);
							c = new Color(rgb, true);
							a = c.getAlpha();
							r = (c.getRed()   + 255) / 2;
							g = (c.getGreen()   + 255) / 2;
							b = (c.getBlue()   + 255) / 2;
							normalLightImage.setRGB(x, y, new Color(r, g, b, a).getRGB());
						}
					}
					int adjustedWidth = Math.round(screenSize.width * 0.98f);
					int adjustedHeight = Math.round(image.getHeight() * adjustedWidth / image.getWidth());
					BufferedImage extendedLightImage = new BufferedImage(adjustedWidth, adjustedHeight, BufferedImage.TYPE_INT_ARGB);
					extendedLightImage.getGraphics().drawImage(normalLightImage, 0, 0, adjustedWidth, adjustedHeight, this);
					extendedLightImage.getGraphics().drawImage(image, (adjustedWidth-image.getWidth())/2, 0, image.getWidth(), image.getHeight(), this);
					imageIcon = new ImageIcon(extendedLightImage);
					JLabel jLabelImage = new JLabel("", imageIcon, JLabel.CENTER);
					jScrollPaneNews.getViewport().add(jLabelImage);
				}
			} else {
				if (welcomePageURL.equals("")) {
					jEditorPaneNews.setText(XFUtility.RESOURCE.getString("SessionError10"));
				} else {
					jEditorPaneNews.setText(XFUtility.RESOURCE.getString("SessionError11") + welcomePageURL + XFUtility.RESOURCE.getString("SessionError12"));
				}
			}
		}
		if (application != null) {
			application.setProgressValue(1);
		}

		jScrollPaneMenu.getViewport().add(jPanelMenu, null);
		jPanelMenuTopMargin.setPreferredSize(new Dimension(20, 20));
		jPanelMenuTopMargin.setOpaque(false);
		jPanelMenuLeftMargin.setPreferredSize(new Dimension(80, 80));
		jPanelMenuLeftMargin.setOpaque(false);
		jPanelMenuRightMargin.setPreferredSize(new Dimension(80, 80));
		jPanelMenuRightMargin.setOpaque(false);
		jPanelMenuBottomMargin.setPreferredSize(new Dimension(20, 20));
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
		gridLayoutMenuButtons.setVgap(20);
		for (int i = 0; i < 20; i++) {
			jButtonMenuOptionArray[i] = new JButton();
			jButtonMenuOptionArray[i].setFont(new java.awt.Font(systemFont, 0, XFUtility.FONT_SIZE+2));
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
		if (application != null) {
			application.setProgressValue(2);
		}

		jPanelTop.setPreferredSize(new Dimension(10, 30));
		jPanelTop.setBorder(BorderFactory.createLoweredBevelBorder());
		jPanelTop.setLayout(new BorderLayout());
		jPanelTop.add(jLabelUser, BorderLayout.WEST);
		jPanelTop.add(jLabelSession, BorderLayout.EAST);
		jScrollPaneMessages.setPreferredSize(new Dimension(10, 40));
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font(systemFont, 0, XFUtility.FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jSplitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane2.add(jScrollPaneNews, JSplitPane.TOP);
		jSplitPane2.add(jTabbedPaneMenu, JSplitPane.BOTTOM);
		jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		jSplitPane1.add(jSplitPane2, JSplitPane.TOP);
		jSplitPane1.add(jScrollPaneMessages, JSplitPane.BOTTOM);

		this.enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		this.setTitle(systemName + " " + systemVersion);
		imageTitle = Toolkit.getDefaultToolkit().createImage(xeadDriver.Session.class.getResource("title32.png"));
		this.setIconImage(imageTitle);
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
				jSplitPane2.setDividerLocation(getHeight() / 5);
				jSplitPane1.setDividerLocation(getHeight() - (getHeight() / 7));
			}
		});
		this.getContentPane().setFocusable(false);
		this.getContentPane().add(jPanelTop, BorderLayout.NORTH);
		this.getContentPane().add(jSplitPane1, BorderLayout.CENTER);
		this.pack();
		this.validate();
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
		if (application != null) {
			application.setProgressValue(3);
		}

		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 20; j++) {
				menuOptionArray[i][j] = null;
			}
		}

		modifyPasswordDialog = new DialogModifyPassword(this);
		checkReadDialog = new DialogCheckRead(this);
		if (application != null) {
			application.setProgressValue(4);
		}

		///////////////////////////////////////////////
		// Setup elements on menu and show first tab //
		///////////////////////////////////////////////
		jLabelUser.setText("User " + userName);
		if (userMenus.equals("ALL")) {
			jLabelSession.setText("<html><u><font color='blue'>Session " + sessionID);
			jLabelSession.addMouseListener(new Session_jLabelSession_mouseAdapter(this));
			buildMenuWithID("");
		} else {
			jLabelSession.setText("Session " + sessionID);
			StringTokenizer workTokenizer = new StringTokenizer(userMenus, "," );
			while (workTokenizer.hasMoreTokens()) {
				buildMenuWithID(workTokenizer.nextToken());
			}
		}
		setupOptionsOfMenuWithTabNo(0);
		if (application != null) {
			application.setProgressValue(5);
		}

		//////////////////////////////
		// setup calendar component //
		//////////////////////////////
		xfCalendar = new XFCalendar(this);

		////////////////////////////////////////////////////
		// Construct Functions to be loaded at logging-in //
		////////////////////////////////////////////////////
		org.w3c.dom.Element element;
		int wrkCount = 0;
		XFExecutable module = null;
		String functionType;
		if (loadingFunctionIDList.size() > 0) {
			wrkCount = 0;
			if (application != null) {
				application.setProgressMax(loadingFunctionIDList.size());
				application.setProgressValue(wrkCount);
				application.setTextOnSplash(XFUtility.RESOURCE.getString("SplashMessage4"));
			}
			for (int i = 0; i < functionList.getLength(); i++) {
				element = (org.w3c.dom.Element)functionList.item(i);
				if (loadingFunctionIDList.contains(element.getAttribute("ID"))) {
					wrkCount++;
					if (application != null) {
						application.setProgressValue(wrkCount);
						application.repaintProgress();
					}

					functionType = element.getAttribute("Type");
					if (functionType.equals("XF000")) {
						module = new XF000(this, -1);
					}
					if (functionType.equals("XF100")) {
						module = new XF100(this, -1);
					}
					if (functionType.equals("XF110")) {
						module = new XF110(this, -1);
					}
					if (functionType.equals("XF200")) {
						module = new XF200(this, -1);
					}
					if (functionType.equals("XF290")) {
						module = new XF290(this, -1);
					}
					if (functionType.equals("XF300")) {
						module = new XF300(this, -1);
					}
					if (functionType.equals("XF310")) {
						module = new XF310(this, -1);
					}
					if (functionType.equals("XF390")) {
						module = new XF390(this, -1);
					}
					module.setFunctionSpecifications(element);
					preloadedFunctionList.add(module);
				}
			}
		}
	}

	public void setMessage(String text) {
		jTextAreaMessages.setText(text);
		jTextAreaMessages.setForeground(Color.blue);
		jTextAreaMessages.paintImmediately(0, 0, jTextAreaMessages.getWidth(), jTextAreaMessages.getHeight());
		jTextAreaMessages.setForeground(Color.black);
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

		org.w3c.dom.Element ipAddressField = getFieldElement(sessionTable, "TXIPADDRESS");
		int ipAddressSize = Integer.parseInt(ipAddressField.getAttribute("Size"));
		if (value.length() > ipAddressSize) {
			value = value.substring(0, ipAddressSize);
		}

		return value;
	}

	void jLabelSession_mouseClicked(MouseEvent e) {
		try {
			HashMap<String, Object> parmMap = new HashMap<String, Object>();
			parmMap.put("NRSESSION", sessionID);
			functionLauncher.execute("ZF051", parmMap);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "Unable to call the function ZF051.");
		}
	}

	void jLabelSession_mouseEntered(MouseEvent e) {
		setCursor(editorKit.getLinkCursor());
	}

	void jLabelSession_mouseExited(MouseEvent e) {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public ScriptEngineManager getScriptEngineManager() {
		return scriptEngineManager;
	}

	public BaseFont getBaseFontWithID(String id) {
		return baseFontMap.get(id);
	}

	public XFExecutable getPreloadedFunction(String id) {
		XFExecutable preloadedFunction = null;
		for (int i = 0; i < preloadedFunctionList.size(); i++) {
			if (preloadedFunctionList.get(i).getFunctionID().equals(id)) {
				preloadedFunction = preloadedFunctionList.get(i);
			}
		}
		return preloadedFunction;
	}

	public java.util.Date getDateOnCalendar(java.util.Date date, String kbCalendar, Point position) {
		return xfCalendar.getDateOnCalendar(date, kbCalendar, position);
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

		NodeList menuList = domDocument.getElementsByTagName("Menu");
		sortingList = XFUtility.getSortedListModel(menuList, "ID");
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
				if (!skipPreload) {
					tokenizer = new StringTokenizer(menuElement.getAttribute("FunctionsToBeLoaded"), ";" );
					while (tokenizer.hasMoreTokens()) {
						wrkStr = tokenizer.nextToken();
						if (!loadingFunctionIDList.contains(wrkStr)) {
							loadingFunctionIDList.add(wrkStr);
						}
					}
				}

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

	public String formatTime(int timeFrom) {
		if (timeFrom > 9999) {
			JOptionPane.showMessageDialog(null, "Invalid time value.");
			return null;
		} else {
			return formatTime(Integer.toString(timeFrom));
		}
	}

	public String formatTime(String timeFrom) {
		if (timeFrom.contains(":")) {
			return timeFrom;
		} else {
			if (timeFrom.length() > 4) {
				JOptionPane.showMessageDialog(null, "Invalid time value.");
				return null;
			} else {
				String wrkStr = "00:00";
				if (timeFrom.length() == 1) {
					wrkStr = "00:" + "0"+ timeFrom;
				}
				if (timeFrom.length() == 2) {
					wrkStr = "00:" + timeFrom;
				}
				if (timeFrom.length() == 3) {
					wrkStr = "0" + timeFrom.substring(0, 1) + ":" + timeFrom.substring(1, 3);
				}
				if (timeFrom.length() == 4) {
					wrkStr = timeFrom.substring(0, 2) + ":" + timeFrom.substring(2, 4);
				}
				return wrkStr;
			}
		}
	}

	public String getOffsetTime(int timeFrom, int minutes) {
		if (timeFrom > 9999) {
			JOptionPane.showMessageDialog(null, "Invalid time format.");
			return null;
		} else {
			String strTimeFrom = Integer.toString(timeFrom);
			if (strTimeFrom.length() == 1) {
				strTimeFrom = "00:" + "0"+ strTimeFrom;
			}
			if (strTimeFrom.length() == 2) {
				strTimeFrom = "00:" + strTimeFrom;
			}
			if (strTimeFrom.length() == 3) {
				strTimeFrom = "0" + strTimeFrom.substring(0, 1) + ":" + strTimeFrom.substring(1, 3);
			}
			if (strTimeFrom.length() == 4) {
				strTimeFrom = strTimeFrom.substring(0, 2) + ":" + strTimeFrom.substring(2, 4);
			}
			return getOffsetTime(strTimeFrom, minutes);
		}
	}

	private String getOffsetTime(String hhmmFrom, int minutes) {
		if (!hhmmFrom.contains(":")) {
			if (hhmmFrom.length() == 1) {
				hhmmFrom = "00:" + "0"+ hhmmFrom;
			}
			if (hhmmFrom.length() == 2) {
				hhmmFrom = "00:" + hhmmFrom;
			}
			if (hhmmFrom.length() == 3) {
				hhmmFrom = "0" + hhmmFrom.substring(0, 1) + ":" + hhmmFrom.substring(1, 3);
			}
			if (hhmmFrom.length() == 4) {
				hhmmFrom = hhmmFrom.substring(0, 2) + ":" + hhmmFrom.substring(2, 4);
			}
		}
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

	public String getOffsetDateTime(String dateFrom, int timeFrom, int minutes) {
		return getOffsetDateTime(dateFrom, timeFrom, minutes, 0, "00");
	}

	public String getOffsetDateTime(String dateFrom, int timeFrom, int minutes, int countType) {
		return getOffsetDateTime(dateFrom, timeFrom, minutes, countType, "00");
	}
	
	public String getOffsetDateTime(String dateFrom, int timeFrom, int minutes, int countType, String kbCalendar) {
		if (timeFrom > 9999) {
			JOptionPane.showMessageDialog(null, "Invalid time format.");
			return null;
		} else {
			String strTimeFrom = Integer.toString(timeFrom);
			if (strTimeFrom.length() == 1) {
				strTimeFrom = "00:" + "0"+ strTimeFrom;
			}
			if (strTimeFrom.length() == 2) {
				strTimeFrom = "00:" + strTimeFrom;
			}
			if (strTimeFrom.length() == 3) {
				strTimeFrom = "0" + strTimeFrom.substring(0, 1) + ":" + strTimeFrom.substring(1, 3);
			}
			if (strTimeFrom.length() == 4) {
				strTimeFrom = strTimeFrom.substring(0, 2) + ":" + strTimeFrom.substring(2, 4);
			}
			return getOffsetDateTime(dateFrom, strTimeFrom, minutes, countType, kbCalendar);
		}
	}

	public String getOffsetDateTime(String dateFrom, String timeFrom, int minutes, int countType) {
		return getOffsetDateTime(dateFrom, timeFrom, minutes, countType, "00");
	}

	public String getOffsetDateTime(String dateFrom, String timeFrom, int minutes) {
		return getOffsetDateTime(dateFrom, timeFrom, minutes, 0, "00");
	}

	public String getOffsetDateTime(String dateFrom, String timeFrom, int minutes, int countType, String kbCalendar) {
		if (!timeFrom.contains(":")) {
			if (timeFrom.length() == 1) {
				timeFrom = "00:" + "0"+ timeFrom;
			}
			if (timeFrom.length() == 2) {
				timeFrom = "00:" + timeFrom;
			}
			if (timeFrom.length() == 3) {
				timeFrom = "0" + timeFrom.substring(0, 1) + ":" + timeFrom.substring(1, 3);
			}
			if (timeFrom.length() == 4) {
				timeFrom = timeFrom.substring(0, 2) + ":" + timeFrom.substring(2, 4);
			}
		}
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
		return getOffsetDate(dateFrom, days, countType, "00");
	}

	public String getOffsetDate(String dateFrom, int days, int countType, String kbCalendar) {
		String offsetDate = "";
		Date workDate;
		SimpleDateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");

		dateFrom = dateFrom.replaceAll("-", "").replaceAll("/", "").trim();

		int y = Integer.parseInt(dateFrom.substring(0,4));
		int m = Integer.parseInt(dateFrom.substring(4,6));
		int d = Integer.parseInt(dateFrom.substring(6,8));
		Calendar cal = Calendar.getInstance();
		cal.set(y, m-1, d);

		if (countType == 0) {
			cal.add(Calendar.DATE, days);
		}
		if (countType == 1) {
			if (days >= 0) {
				for (int i = 0; i < days; i++) {
					cal.add(Calendar.DATE, 1);
					workDate = cal.getTime();
					if (offDateList.contains(kbCalendar + ";" + dfm.format(workDate))) {
						days++;
					}
				}
			} else {
				days = days * -1;
				for (int i = 0; i < days; i++) {
					cal.add(Calendar.DATE, -1);
					workDate = cal.getTime();
					if (offDateList.contains(kbCalendar + ";" + dfm.format(workDate))) {
						days++;
					}
				}
			}
		}

		workDate = cal.getTime();
		offsetDate = dfm.format(workDate);

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
		return getDaysBetweenDates(strDateFrom, strDateThru, countType, "00");
	}

	public int getDaysBetweenDates(String strDateFrom, String strDateThru, int countType, String kbCalendar) {
		int days = 0;
		int y, m, d;
		Date dateFrom, dateThru;
		SimpleDateFormat dfm = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();

		strDateFrom = strDateFrom.replaceAll("-", "").replaceAll("/", "").trim();
		y = Integer.parseInt(strDateFrom.substring(0,4));
		m = Integer.parseInt(strDateFrom.substring(4,6));
		d = Integer.parseInt(strDateFrom.substring(6,8));
		cal.set(y, m-1, d, 0, 0, 0);
		dateFrom = cal.getTime();

		strDateThru = strDateThru.replaceAll("-", "").replaceAll("/", "").trim();
		y = Integer.parseInt(strDateThru.substring(0,4));
		m = Integer.parseInt(strDateThru.substring(4,6));
		d = Integer.parseInt(strDateThru.substring(6,8));
		cal.set(y, m-1, d, 0, 0, 0);
		dateThru = cal.getTime();

		if (countType == 0) {
			long diff = dateThru.getTime() - dateFrom.getTime();
			days = (int)(diff / 86400000); 
		}
		if (countType == 1) {
			if (dateThru.getTime() == dateFrom.getTime()) {
				days = 0;
			}
			if (dateThru.getTime() > dateFrom.getTime()) {
				y = Integer.parseInt(strDateFrom.substring(0,4));
				m = Integer.parseInt(strDateFrom.substring(4,6));
				d = Integer.parseInt(strDateFrom.substring(6,8));
				cal.set(y, m-1, d, 0, 0, 0);
				long timeThru = dateThru.getTime();
				long timeWork = dateFrom.getTime();
				while (timeThru > timeWork) {
					cal.add(Calendar.DATE, 1);
					if (!offDateList.contains(kbCalendar + ";" + dfm.format(cal.getTime()))) {
						days++;
					}
					timeWork = cal.getTime().getTime();
				}
			}
			if (dateThru.getTime() < dateFrom.getTime()) {
				y = Integer.parseInt(strDateThru.substring(0,4));
				m = Integer.parseInt(strDateThru.substring(4,6));
				d = Integer.parseInt(strDateThru.substring(6,8));
				cal.set(y, m-1, d, 0, 0, 0);
				long timeWork = dateThru.getTime();
				long timeFrom = dateFrom.getTime();
				while (timeFrom > timeWork) {
					cal.add(Calendar.DATE, 1);
					if (!offDateList.contains(kbCalendar + ";" + dfm.format(cal.getTime()))) {
						days++;
					}
					timeWork = cal.getTime().getTime();
				}
				days = days * -1;
			}
		}

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
		return isOffDate(date, "00");
	}

	public boolean isOffDate(String date, String kbCalendar) {
		return offDateList.contains(kbCalendar + ";" + date);
	}

	public boolean isClientSession() {
		return isClientSession;
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

	public Object getMinutesBetweenTimes(int timeFrom, int timeThru) {
		if (timeFrom > 9999 || timeThru > 9999) {
			JOptionPane.showMessageDialog(null, "Invalid time format.");
			return null;
		} else {
			String strTimeFrom = Integer.toString(timeFrom);
			if (strTimeFrom.length() == 1) {
				strTimeFrom = "00:" + "0"+ strTimeFrom;
			}
			if (strTimeFrom.length() == 2) {
				strTimeFrom = "00:" + strTimeFrom;
			}
			if (strTimeFrom.length() == 3) {
				strTimeFrom = "0" + strTimeFrom.substring(0, 1) + ":" + strTimeFrom.substring(1, 3);
			}
			if (strTimeFrom.length() == 4) {
				strTimeFrom = strTimeFrom.substring(0, 2) + ":" + strTimeFrom.substring(2, 4);
			}
			String strTimeThru = Integer.toString(timeThru);
			if (strTimeThru.length() == 1) {
				strTimeThru = "00:" + "0"+ strTimeThru;
			}
			if (strTimeThru.length() == 2) {
				strTimeThru = "00:" + strTimeThru;
			}
			if (strTimeThru.length() == 3) {
				strTimeThru = "0" + strTimeThru.substring(0, 1) + ":" + strTimeThru.substring(1, 3);
			}
			if (strTimeThru.length() == 4) {
				strTimeThru = strTimeThru.substring(0, 2) + ":" + strTimeThru.substring(2, 4);
			}
			return getMinutesBetweenTimes(strTimeFrom, strTimeThru);
		}
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
		DecimalFormat decimalFormat = new DecimalFormat();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < digit; i++) {
			buf.append("0");
		}
		decimalFormat.applyPattern(buf.toString());
		String outputdata = decimalFormat.format(number);
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
			JOptionPane.showMessageDialog(null, "Accessing to the system variant table failed.\n" + e.getMessage());
		}
		return strValue;
	}

	public HashMap getFilterValueMap(String functionID) {
		HashMap<String, String> valueMap = null;
		if (!userFilterValueTable.equals("")) {
			try {
				valueMap = new HashMap<String, String>();
				String sql = "select * from " + userFilterValueTable
						+ " where IDUSER ='" + this.getUserID()
						+ "' AND IDFUNCTION = '" + functionID + "'";
				XFTableOperator operator = new XFTableOperator(this, null, sql, true);
				while (operator.next()) {
					valueMap.put(operator.getValueOf("IDFILTER").toString(), operator.getValueOf("TXVALUE").toString());
				}
			} catch (Exception e) {
				valueMap = null;
			}
		}
		return valueMap;
	}

	public void setFilterValueMap(String functionID, String filterID, String value) {
		if (!userFilterValueTable.equals("")) {
			try {
				String sql = "select * from " + userFilterValueTable
						+ " where IDUSER ='" + this.getUserID()
						+ "' AND IDFUNCTION = '" + functionID
						+ "' AND IDFILTER = '" + filterID + "'";
				XFTableOperator operator = new XFTableOperator(this, null, sql, true);
				if (operator.next()) {
					sql = "update " + userFilterValueTable
							+ " set TXVALUE = '" + value
							+ "' where IDUSER ='" + this.getUserID()
							+ "' AND IDFUNCTION = '" + functionID
							+ "' AND IDFILTER = '" + filterID + "'";
				} else {
					sql = "insert into " + userFilterValueTable
							+ " (IDUSER, IDFUNCTION, IDFILTER, TXVALUE) values ("
							+ "'" + this.getUserID() + "', '" + functionID
							+ "', '" + filterID + "', '" + value + "')";
				}
				operator = new XFTableOperator(this, null, sql, true);
				operator.execute();
			} catch (Exception e) {}
		}
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
			JOptionPane.showMessageDialog(null, "Accessing to the system variant table failed.\n" + e.getMessage());
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
			JOptionPane.showMessageDialog(null, "Accessing to the system variant table failed.\n" + e.getMessage());
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
			JOptionPane.showMessageDialog(null, "Accessing to the system variant table failed.\n" + e.getMessage());
		}
	}

	public float getAnnualExchangeRate(String currencyCode, int fYear, String type) {
		float rateReturn = 0;
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
					JOptionPane.showMessageDialog(null, "Annual exchange rate not found for '" + currencyCode + "'," + fYear + "."+ "\nSQL: " + sql);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return rateReturn;
	}

	public float getMonthlyExchangeRate(String currencyCode, String date, String type) {
		return getMonthlyExchangeRate(currencyCode, getFYearOfDate(date), getMSeqOfDate(date), type);
	}

	public float getMonthlyExchangeRate(String currencyCode, int fYear, int mSeq, String type) {
		float rateReturn = 0;
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
		return rateReturn;
	}

	public int getTaxAmount(String date, int amount) {
		int fromDate = 0;
		int taxAmount = 0;
		if (date != null && !date.equals("")) {
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
			parmDate = parmDate.replaceAll("-", "").replaceAll("/", "").trim();
			parmDate = parmDate.replaceAll("/", "");
			month = Integer.parseInt(parmDate.substring(4,6));
			date = Integer.parseInt(parmDate.substring(6,8));

			boolean isWithinMonth = false;
			int startMonth = 1;
			int lastDay = 31;
			int value1 = getSystemVariantInteger("FIRST_MONTH");
			if (value1 != 0) {
				startMonth = value1;
			}
			int value2 = getSystemVariantInteger("LAST_DAY");
			if(value2 != 0) {
				lastDay = value2;
			}

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
			parmDate = parmDate.replaceAll("-", "").replaceAll("/", "").trim();
			fYear = Integer.parseInt(parmDate.substring(0,4));
			month = Integer.parseInt(parmDate.substring(4,6));
			date = Integer.parseInt(parmDate.substring(6,8));

			boolean isWithinMonth = false;
			int startMonth = 1;
			int lastDay = 31;
			int value1 = getSystemVariantInteger("FIRST_MONTH");
			if (value1 != 0) {
				startMonth = value1;
			}
			int value2 = getSystemVariantInteger("LAST_DAY");
			if(value2 != 0) {
				lastDay = value2;
			}

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
		return resultYear + resultMonth;
	}

	public void closeSession(boolean isToWriteLogAndClose) {

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
			logout(e);
		} else {
			super.processWindowEvent(e);
		}
	}
	
	void logout(WindowEvent e) {
		Object[] bts = {XFUtility.RESOURCE.getString("Yes"), XFUtility.RESOURCE.getString("No")};
		int rtn = JOptionPane.showOptionDialog(this, XFUtility.RESOURCE.getString("FunctionMessage55"),
				systemName, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
		if (rtn == 0) {
			if (e != null) {
				super.processWindowEvent(e);
			}
			this.closeSession(true);
			this.setVisible(false);
			System.exit(0);
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
			Component com = (Component)e.getSource();
			for (int i = 0; i < 20; i++) {
				if (com.equals(jButtonMenuOptionArray[i])) {
					if (menuOptionArray[jTabbedPaneMenu.getSelectedIndex()][i].isLogoutOption) {
						logout(null);
					} else {
						HashMap<String, Object> returnMap = menuOptionArray[jTabbedPaneMenu.getSelectedIndex()][i].call();
						if (returnMap != null && returnMap.get("RETURN_CODE") != null) {
							if (returnMap.get("RETURN_MESSAGE") == null) {
								jTextAreaMessages.setText(XFUtility.getMessageOfReturnCode(returnMap.get("RETURN_CODE").toString()));
							} else {
								jTextAreaMessages.setText(returnMap.get("RETURN_MESSAGE").toString());
							}
						}
						if (returnMap.get("RETURN_TO") != null && returnMap.get("RETURN_TO").equals("MENU")) {
							jTextAreaMessages.setText(XFUtility.RESOURCE.getString("SessionMessage3"));
						}
					}
					break;
				}
			}
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public int writeLogOfFunctionStarted(String functionID, String functionName) {
		sqProgram++;
		try {
			if (functionName.length() > 30) {
				functionName = functionName.substring(0, 27) + "...";
			}
			String sql = "insert into " + sessionDetailTable
							+ " (NRSESSION, SQPROGRAM, IDMENU, IDPROGRAM, TXPROGRAM, DTSTART, KBPROGRAMSTATUS) values ("
							+ "'" + this.getSessionID() + "'," + sqProgram + "," + "'" + this.getMenuID() + "'," + "'" + functionID + "'," + "'" + functionName + "'," + "CURRENT_TIMESTAMP,'')";
			XFTableOperator operator = new XFTableOperator(this, null, sql, true);
			operator.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sqProgram;
	}

	public void writeLogOfFunctionClosed(int sqProgramOfFunction, String programStatus, String tableOperationLog, String errorLog) {
		String logString = "";
		StringBuffer bf = new StringBuffer();

		if (tableOperationLog.length() > 3000) {
			tableOperationLog = "... " + tableOperationLog.substring(tableOperationLog.length()-3000, tableOperationLog.length());
		}
		bf.append(tableOperationLog.replace("'", "\""));
		if (!errorLog.equals("")) {
			bf.append("\n<ERROR LOG>\n");
			bf.append(errorLog.replace("'", "\""));
		}
		logString = bf.toString();
		if (logString.length() > 3800) {
			logString = logString.substring(0, 3800) + " ...";
		}

		////////////////////////////////////////////////////////////////////////////
		// Note that value of MS Access Date field is expressed like #2000-01-01# //
		////////////////////////////////////////////////////////////////////////////
		if (databaseName.contains("ucanaccess:")) {
			if (logString.contains("#")) {
				logString = logString.replaceAll("#", "\"");
			}
		}

		if (programStatus.equals("99")) {
			noErrorsOccured = false;
		}

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
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			logout(null);
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

		if (direction.equals("DOWN")) {
			while (i != 9 && i != 19) {
				i = i + 1;
				if (jButtonMenuOptionArray[i].isVisible()) {
					jButtonMenuOptionArray[i].requestFocus();
					break;
				}
			}
		}

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
		private XFExecutable lastPanelFunction = null;

		public FunctionLauncher(Session session) {
			session_ = session;
			xF000[0] = new XF000(session, 0);
			if (session_.isClientSession) {
				xF100[0] = new XF100(session, 0);
				xF110[0] = new XF110(session, 0);
				xF200[0] = new XF200(session, 0);
				xF290[0] = new XF290(session, 0);
				xF300[0] = new XF300(session, 0);
				xF310[0] = new XF310(session, 0);
				xF390[0] = new XF390(session, 0);
			}
		}

		public HashMap<String, Object> execute(String functionID, HashMap<String, Object> parmMap) throws Exception {
			HashMap<String, Object> returnMap = new HashMap<String, Object>();
			boolean isInvalidFunctionID = true;
			XFExecutable workFunction = lastPanelFunction;

			XFExecutable preloadedFunction = session_.getPreloadedFunction(functionID); 
			if (preloadedFunction == null) {
				org.w3c.dom.Element functionElement = null;
				for (int i = 0; i < functionList.getLength(); i++) {
					functionElement = (org.w3c.dom.Element)functionList.item(i);
					if (functionElement.getAttribute("ID").equals(functionID)) {
						returnMap = this.execute(functionElement, parmMap);
						isInvalidFunctionID = false;
						break;
					}
				}
				if (isInvalidFunctionID) {
					if (functionID.equals("")) {
						throw new Exception("Calling canceled as the function for the action is not specified.");
					} else {
						throw new Exception("Calling canceled as its function id '" + functionID + "' is invalid.");
					}
				}
			} else {
				lastPanelFunction = preloadedFunction;
				returnMap = preloadedFunction.execute(parmMap);
			}

			lastPanelFunction = workFunction;

			return returnMap;
		}

		public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parmMap) {
			HashMap<String, Object> returnMap = new HashMap<String, Object>();
			int countOfRuccursiveCalls = 0;

			if (functionElement == null) {
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError15"));
			} else {
				String functionID = functionElement.getAttribute("ID");
				for (int i = 0; i < 10; i++) {
					if (functionElement.getAttribute("Type").equals("XF000")) {
						if (xF000[i] == null) {
							xF000[i] = new XF000(session_, i);
							if (!functionElement.getAttribute("TimerOption").equals("")) {
								lastPanelFunction = xF000[i];
							}
							returnMap = xF000[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF000[i].isAvailable()) {
								if (!functionElement.getAttribute("TimerOption").equals("")) {
									lastPanelFunction = xF000[i];
								}
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
							lastPanelFunction = xF100[i];
							returnMap = xF100[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF100[i].isAvailable()) {
								lastPanelFunction = xF100[i];
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
							lastPanelFunction = xF110[i];
							returnMap = xF110[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF110[i].isAvailable()) {
								lastPanelFunction = xF110[i];
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
							lastPanelFunction = xF200[i];
							returnMap = xF200[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF200[i].isAvailable()) {
								lastPanelFunction = xF200[i];
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
							lastPanelFunction = xF300[i];
							returnMap = xF300[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF300[i].isAvailable()) {
								lastPanelFunction = xF300[i];
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
							lastPanelFunction = xF310[i];
							returnMap = xF310[i].execute(functionElement, parmMap);
							break;
						} else {
							if (xF310[i].isAvailable()) {
								lastPanelFunction = xF310[i];
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
			return returnMap;
		}

		public void startProgress(String text, int max) {
			if (lastPanelFunction != null) {
				lastPanelFunction.startProgress(text, max);
			}
		}
		public void incrementProgress() {
			if (lastPanelFunction != null) {
				lastPanelFunction.incrementProgress();
			}
		}
		public void endProgress() {
			if (lastPanelFunction != null) {
				lastPanelFunction.endProgress();
			}
		}
	}

	public void startProgress(String text, int max) {
		functionLauncher.startProgress(text, max);
	}
	public void incrementProgress() {
		functionLauncher.incrementProgress();
	}
	public void endProgress() {
		functionLauncher.endProgress();
	}

	class MenuOption extends Object {
		private org.w3c.dom.Element functionElement_ = null;
		private String optionName_;
		private boolean isLogoutOption = false;

		public MenuOption(org.w3c.dom.Element functionElement, String optionName) {
			functionElement_ = functionElement;
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

		public String getMenuOptionName() {
			return optionName_;
		}

		public boolean isLogoutOption() {
			return isLogoutOption;
		}

		public HashMap<String, Object> call() {
			if (isLogoutOption) {
				return null;
			} else {
				try {
					return functionLauncher.execute(functionElement_.getAttribute("ID"), new HashMap<String, Object>());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e.getMessage());
					return null;
				}
			}
		}
	}

	public String getAppServerName() {
		return appServerName;
	}

	public String getSubDBName(String id) {
		return subDBNameList.get(subDBIDList.indexOf(id));
	}

	public int getSubDBListSize() {
		return subDBNameList.size();
	}

	public String getSubDBName(int index) {
		return subDBNameList.get(index);
	}

	public String getSubDBUser(int index) {
		return subDBUserList.get(index);
	}

	public String getSubDBPassword(int index) {
		return subDBPasswordList.get(index);
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

	public Object requestWebService(String uri) {
		return requestWebService(uri, "UTF-8");
	}
	public Object requestWebService(String uri, String encoding) {
		Object response = null;
		HttpResponse httpResponse = null;
		InputStream inputStream = null;
		HttpClient httpClient = new DefaultHttpClient();
		try {
			httpGet.setURI(new URI(uri));
			httpResponse = httpClient.execute(httpGet);
			response = EntityUtils.toString(httpResponse.getEntity(), encoding);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionMessage53") + "\n" + ex.getMessage());
		} finally {
			httpClient.getConnectionManager().shutdown();
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch(Exception e) {}
		}
		return response;
	}
	public XFHttpRequest createWebServiceRequest(String uri) {
		return new XFHttpRequest(uri, "UTF-8");
	}
	public XFHttpRequest createWebServiceRequest(String uri, String encoding) {
		return new XFHttpRequest(uri, encoding);
	}

	public Document parseStringToGetXmlDocument(String data) throws Exception {
		return parseStringToGetXmlDocument(data, "UTF-8");
	}
	public Document parseStringToGetXmlDocument(String data, String encoding) throws Exception {
		domParser.parse(new InputSource(new ByteArrayInputStream(data.getBytes(encoding))));
		return domParser.getDocument();
	}
	public Document createXmlDocument() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    DocumentBuilder db = dbf.newDocumentBuilder();
	    return db.newDocument();
	}
	public Document createXmlDocument(String name) throws Exception {
	    Document document = createXmlDocument();
	    org.w3c.dom.Element element = document.createElement(name);
	    document.appendChild(element);
	    return document;
	}
	public org.w3c.dom.Element createXmlNode(Document document, String name) throws Exception {
		return document.createElement(name);
	}
	public org.w3c.dom.Element getXmlNode(Document document, String name) throws Exception {
		NodeList elementList = document.getElementsByTagName(name);
		return (org.w3c.dom.Element)elementList.item(0);
	}
	public org.w3c.dom.Element getXmlNode(org.w3c.dom.Element element, String name) throws Exception {
		NodeList elementList = element.getElementsByTagName(name);
		return (org.w3c.dom.Element)elementList.item(0);
	}
	public ArrayList<org.w3c.dom.Element> getXmlNodeList(Document document, String name) throws Exception {
		ArrayList<org.w3c.dom.Element> elementList = new ArrayList<org.w3c.dom.Element>();
		NodeList nodeList = document.getElementsByTagName(name);
		for (int i = 0; i < nodeList.getLength(); i++) {
			elementList.add((org.w3c.dom.Element)nodeList.item(i));
		}
		return elementList;
	}
	public ArrayList<org.w3c.dom.Element> getXmlNodeList(org.w3c.dom.Element element, String name) throws Exception {
		ArrayList<org.w3c.dom.Element> elementList = new ArrayList<org.w3c.dom.Element>();
		NodeList nodeList = element.getElementsByTagName(name);
		for (int i = 0; i < nodeList.getLength(); i++) {
			elementList.add((org.w3c.dom.Element)nodeList.item(i));
		}
		return elementList;
	}
	public String getXmlNodeContent(org.w3c.dom.Element element, String name) throws Exception {
		org.w3c.dom.Element workElement = getXmlNode(element, name);
		return workElement.getTextContent();
	}

	public JSONObject createJsonObject(String text) throws Exception {
		return new JSONObject(text);
	}
	public JSONObject createJsonObject() throws Exception {
		return new JSONObject();
	}
	public JSONArray createJsonArray(String text) throws Exception {
		return new JSONArray(text);
	}
	public JSONObject getJsonObject(JSONObject object, String name) throws Exception {
		return object.getJSONObject(name);
	}
	public JSONArray getJsonArray(JSONObject object, String name) throws Exception {
		return object.getJSONArray(name);
	}
	public JSONObject getJsonObject(JSONArray array, int index) throws Exception {
		return array.getJSONObject(index);
	}

	public byte[] decodeBase64StringToByteArray(Object value) {
		if (value instanceof String) {
			return Base64.decodeBase64((String)value);
		} else {
			return null;
		}
	}
	public String encodeByteArrayToBase64String(Object value) {
		if (value instanceof byte[]) {
			return Base64.encodeBase64String((byte[])value);
		} else {
			return "";
		}
	}
	
	public String getDigestedValueForUser(String user, String value) {
		if (isValueSaltedForUser) {
			return getDigestedValue(value, digestAlgorithmForUser, countOfExpandForUser, user);
		} else {
			return getDigestedValue(value, digestAlgorithmForUser, countOfExpandForUser, "");
		}
	}
	public String getDigestedValueForUser(String value) {
		if (isValueSaltedForUser) {
			return getDigestedValue(value, digestAlgorithmForUser, countOfExpandForUser, userID);
		} else {
			return getDigestedValue(value, digestAlgorithmForUser, countOfExpandForUser, "");
		}
	}
	public String getDigestedValue(String value, String algorithm) {
		return getDigestedValue(value, algorithm, 1, "");
	}
	public String getDigestedValue(String value, String algorithm, int expand, String salt) {
		String digestedValue = "";
		int count = expand - 1;
		try {
			DigestAdapter adapter = new DigestAdapter(algorithm);
			digestedValue = adapter.digest(value + salt);
			for (int i=0;i<count;i++) {
				digestedValue = adapter.digest(digestedValue + salt);
			}
		} catch (NoSuchAlgorithmException e) {
			return digestedValue;
		}
		return digestedValue;
	}

	public String getAddressFromZipNo(String zipNo) {
		String value = "";
		String zipNo_ = zipNo.replace("-", "");
		HttpResponse response = null;
		InputStream inputStream = null;
		HttpClient httpClient = new DefaultHttpClient();
		try {
			httpGet.setURI(new URI(ZIP_URL + "zn=" + zipNo_));
			response = httpClient.execute(httpGet);  
			if (response.getStatusLine().getStatusCode() < 400){
				inputStream = response.getEntity().getContent();
				domParser.parse(new InputSource(inputStream));
				responseDoc = domParser.getDocument();
				org.w3c.dom.Element rootNode = (org.w3c.dom.Element)responseDoc.getElementsByTagName("ZIP_result").item(0);
				if (rootNode.getElementsByTagName("value").getLength() == 0) {
					JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionMessage54") + "\n" + zipNo);
				} else {
					org.w3c.dom.Element stateNode = (org.w3c.dom.Element)rootNode.getElementsByTagName("value").item(4);
					org.w3c.dom.Element cityNode = (org.w3c.dom.Element)rootNode.getElementsByTagName("value").item(5);
					org.w3c.dom.Element addressNode = (org.w3c.dom.Element)rootNode.getElementsByTagName("value").item(6);
					org.w3c.dom.Element companyNode = (org.w3c.dom.Element)rootNode.getElementsByTagName("value").item(7);
					String state = stateNode.getAttribute("state");
					String city = cityNode.getAttribute("city");
					String address = addressNode.getAttribute("address");
					if (address.equals("none")) {
						address = "";
					}
					String company = companyNode.getAttribute("company");
					if (company.equals("none")) {
						company = "";
					}
					value = state + city + address + company;
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

	public String executeProgram(String pgmName) {
		String message = "";
		try {
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec(pgmName);
			if (p != null) {
				int count = 0;
				String result = "";
				StringBuffer buf = new StringBuffer();
				InputStream is = p.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				while ((result = br.readLine()) != null) {
					if (count > 0) {
						buf.append("\n");
					}
					buf.append(result);
					count++;
				}
				message = buf.toString();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, ex.getMessage());
		}
		return message;
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
		if (extension.equals(".pdf")) {
			header = "PDF_" + header;
		}
		if (extension.equals(".xls")) {
			header = "XLS_" + header;
		}
		if (extension.equals(".log")) {
			header = "LOG_" + header;
		}
		File tempFile = File.createTempFile(header + functionID + "_", extension, outputFolder);
		if (outputFolder == null) {
			tempFile.deleteOnExit();
		}
		return tempFile;
	}

	public boolean existsFile(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}

	public boolean deleteFile(String fileName) {
			File file = new File(fileName);
			return file.delete();
	}

	public boolean renameFile(String currentName, String newName) {
			File currentFile = new File(currentName);
			File newFile = new File(newName);
			return currentFile.renameTo(newFile);
	}

	public XFTextFileOperator createTextFileOperator(String operation, String fileName, String separator) {
		return createTextFileOperator(operation, fileName, separator, "");
	}

	public XFTextFileOperator createTextFileOperator(String operation, String fileName, String separator, String charset) {
		File file = new File(fileName);
		if (operation.equals("Read") && !file.exists()) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("SessionError21") + fileName + XFUtility.RESOURCE.getString("SessionError22"));
			return null;
		} else {
			return new XFTextFileOperator(operation, fileName, separator, charset);
		}
	}
	
	public XFTableOperator createTableOperator(String oparation, String tableID) {
		XFTableOperator operator = null;
		try {
			operator = new XFTableOperator(this, null, oparation, tableID, true);
		} catch (Exception e) {
		}
		return operator;
	}

	public XFTableOperator createTableOperator(String sqlText) {
		return new XFTableOperator(this, null, sqlText, true);
	}

	org.w3c.dom.Document getDomDocument() {
		return domDocument;
	}

	public void sendMail(String addressFrom, String addressTo, String addressCc, String subject, String message) {
		sendMail(addressFrom, addressTo, addressCc, subject, message, "", "", "");
	}
	
	public void sendMail(String addressFrom, String addressTo, String addressCc, String subject, String message,
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
			mailObj.setFrom(new InternetAddress(addressFrom));
			mailObj.setSentDate(new Date());

			StringTokenizer workTokenizer = new StringTokenizer(addressTo.replaceAll(" ",""), "," );
			InternetAddress[] toList = new InternetAddress[workTokenizer.countTokens()];
			int i = 0;
			while (workTokenizer.hasMoreTokens()) {
				toList[i] = new InternetAddress(workTokenizer.nextToken());
				i++;
			}
			mailObj.setRecipients(Message.RecipientType.TO, toList);

			if (!addressCc.equals("")) {
				workTokenizer = new StringTokenizer(addressCc.replaceAll(" ",""), "," );
				InternetAddress[] ccList = new InternetAddress[workTokenizer.countTokens()];
				i = 0;
				while (workTokenizer.hasMoreTokens()) {
					ccList[i] = new InternetAddress(workTokenizer.nextToken());
					i++;
				}
				mailObj.setRecipients(Message.RecipientType.CC, ccList);
			}

			if (charset.equals("")) {
				mailObj.setSubject(subject);
			} else {
				mailObj.setSubject(subject, charset);
			}

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
		return functionLauncher.execute(functionID, parmMap);
	}

	Desktop getDesktop() {
		return desktop;
	}

	public DialogCheckRead getDialogCheckRead() {
		return checkReadDialog;
	}

	String getSystemName() {
		return systemName;
	}

	String getVersion() {
		return systemVersion;
	}

	public String getAdminEmail() {
		return smtpAdminEmail;
	}

	String getTableNameOfUser() {
		return userTable;
	}

	String getTableNameOfUserFilterValue() {
		return userFilterValueTable;
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
		int x = this.getX();
		if (x < 0) {
			x = 0;
		}
		int y = this.getY();
		if (y < 0) {
			y = 0;
		}
		int w = this.getWidth();
		if (w > screenSize.width) {
			w = screenSize.width;
		}
		int h = this.getHeight();
		if (h > screenSize.height) {
			h = screenSize.height;
		}
		return new Rectangle(x, y, w, h);
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

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void compressTable(String tableID) throws Exception {
		StringBuffer statementBuf;
		org.w3c.dom.Element element;
		Statement statement = null;
		HttpPost httpPost = null;
		String msg = "";

		if (databaseName.contains("jdbc:derby:")) {
			try {
				if (appServerName.equals("")) {
					statement = connectionManualCommit.createStatement();
				}
				for (int i = 0; i < tableList.getLength(); i++) {
					element = (org.w3c.dom.Element)tableList.item(i);
					if (element.getAttribute("ID").startsWith(tableID) || tableID.equals("")) {
						statementBuf = new StringBuffer();
						statementBuf.append("CALL SYSCS_UTIL.SYSCS_COMPRESS_TABLE('");
						statementBuf.append(databaseUser);
						statementBuf.append("', '") ;
						statementBuf.append(element.getAttribute("ID"));
						statementBuf.append("', 1)") ;
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

	public String getFileName() {
		return fileName;
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

	public String getSystemProperty(String id) {
		return System.getProperty(id);
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
		for (int k = 0; k < functionList.getLength(); k++) {
			workElement = (org.w3c.dom.Element)functionList.item(k);
			if (workElement.getAttribute("ID").equals(functionID)) {
				functionName = workElement.getAttribute("Name");
				break;
			}
		}
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

	public String getFieldName(String tableID, String fieldID) {
		String name = "";
		org.w3c.dom.Element element = getFieldElement(tableID, fieldID);
		if (element != null) {
			name = element.getAttribute("Name");
		}
		return name;
	}

	public String getFieldType(String tableID, String fieldID) {
		String name = "";
		org.w3c.dom.Element element = getFieldElement(tableID, fieldID);
		if (element != null) {
			name = element.getAttribute("Type");
		}
		return name;
	}

	public String getFieldSize(String tableID, String fieldID) {
		String name = "";
		org.w3c.dom.Element element = getFieldElement(tableID, fieldID);
		if (element != null) {
			name = element.getAttribute("Size");
		}
		return name;
	}

	public String getFieldDecimal(String tableID, String fieldID) {
		String name = "";
		org.w3c.dom.Element element = getFieldElement(tableID, fieldID);
		if (element != null) {
			name = element.getAttribute("Decimal");
		}
		return name;
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

class Session_jLabelSession_mouseAdapter extends java.awt.event.MouseAdapter {
	Session adaptee;
	Session_jLabelSession_mouseAdapter(Session adaptee) {
		this.adaptee = adaptee;
	}
	public void mouseClicked(MouseEvent e) {
		adaptee.jLabelSession_mouseClicked(e);
	}
	public void mouseEntered(MouseEvent e) {
		adaptee.jLabelSession_mouseEntered(e);
	}
	public void mouseExited(MouseEvent e) {
		adaptee.jLabelSession_mouseExited(e);
	}
}