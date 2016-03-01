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

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class XF000 extends JDialog implements XFExecutable, XFScriptable {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element functionElement_ = null;
	private HashMap<String, Object> parmMap_ = null;
	private HashMap<String, Object> returnMap_ = new HashMap<String, Object>();
	private Session session_ = null;
	private boolean instanceIsAvailable_ = true;
	private int programSequence;
	private int instanceArrayIndex_ = -1;
	private StringBuffer processLog = new StringBuffer();
	private ScriptEngine scriptEngine;
	private Bindings engineScriptBindings;
	private ByteArrayOutputStream exceptionLog;
	private PrintStream exceptionStream;
	private String exceptionHeader = "";

	private JPanel jPanelMain = new JPanel();
	private Dimension scrSize;
	private JPanel jPanelTimer = new JPanel();
	private JPanel jPanelTop = new JPanel();
	private JPanel jPanelBottom = new JPanel();
	private JPanel jPanelButtons = new JPanel();
	private JPanel jPanelInfo = new JPanel();
	private JLabel jLabelTime = new JLabel();
	private JTextField jTextFieldTime = new JTextField();
	private ArrayList<String> timeList = new ArrayList<String>(); 
	private String timeRepeatFrom = "";
	private String timeRepeatThru = "";
	private int intervalMilliSeconds = 0;
	private JLabel jLabelCondition = new JLabel();
	private JCheckBox jCheckBoxRepeat = new JCheckBox();
	private JCheckBox jCheckBoxRunOffDay = new JCheckBox();
	private JCheckBox jCheckBoxRunNow = new JCheckBox();
	private Calendar calendar;
	private SimpleDateFormat formatter = new SimpleDateFormat("dd-HH:mm:ss");
	private Timer timer;
	private TimerTaskScript task = null;
	private boolean alreadyRun = false;
	private boolean errorHasOccured = false;
	private boolean firstTime = true;
	private GridLayout gridLayoutInfo = new GridLayout();
	private JLabel jLabelFunctionID = new JLabel();
	private JLabel jLabelSessionID = new JLabel();
	private JProgressBar jProgressBar = new JProgressBar();
	private JScrollPane jScrollPaneMessages = new JScrollPane();
	private JTextArea jTextAreaMessages = new JTextArea();
	private JButton jButtonExit = new JButton();
	private JButton jButtonStart = new JButton();
	private JButton jButtonStop = new JButton();
	private JButton jButtonIconify = new JButton();
	private final int FONT_SIZE = 14;
	private HashMap<String, Object> variantMap = new HashMap<String, Object>();
	
	public XF000(Session session, int instanceArrayIndex) {
		super(session, "", true);
		session_ = session;
		instanceArrayIndex_ = instanceArrayIndex;
		initConsole();
	}

	void initConsole() {
		scrSize = Toolkit.getDefaultToolkit().getScreenSize();
		jPanelMain.setLayout(new BorderLayout());
		jPanelTop.setLayout(new BorderLayout());
		jPanelTimer.setLayout(null);
		jPanelTimer.setFocusable(false);
		jPanelTimer.setBorder(BorderFactory.createEtchedBorder());
		jPanelTimer.setPreferredSize(new Dimension(10, 75));
		jLabelTime.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jLabelTime.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelTime.setHorizontalTextPosition(SwingConstants.LEADING);
		jLabelTime.setText(XFUtility.RESOURCE.getString("TimerTime"));
		jLabelTime.setBounds(new Rectangle(10, 42, 100, 20));
		jTextFieldTime.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTextFieldTime.setBounds(new Rectangle(115, 40, 540, 25));
		jLabelCondition.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jLabelCondition.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelCondition.setHorizontalTextPosition(SwingConstants.LEADING);
		jLabelCondition.setText(XFUtility.RESOURCE.getString("TimerCondition"));
		jLabelCondition.setBounds(new Rectangle(10, 11, 100, 20));
		jCheckBoxRepeat.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jCheckBoxRepeat.setText(XFUtility.RESOURCE.getString("TimerRepeat"));
		jCheckBoxRepeat.setBounds(new Rectangle(295, 11, 180, 20));
		jCheckBoxRunOffDay.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jCheckBoxRunOffDay.setText(XFUtility.RESOURCE.getString("TimerRunOffDay"));
		jCheckBoxRunOffDay.setBounds(new Rectangle(485, 11, 180, 20));
		jCheckBoxRunNow.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jCheckBoxRunNow.setText(XFUtility.RESOURCE.getString("TimerRunNow"));
		jCheckBoxRunNow.setBounds(new Rectangle(115, 11, 180, 20));
		jCheckBoxRunNow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setRunNow();
			}
		});
		jPanelTimer.add(jLabelTime);
		jPanelTimer.add(jTextFieldTime);
		jPanelTimer.add(jLabelCondition);
		jPanelTimer.add(jCheckBoxRepeat);
		jPanelTimer.add(jCheckBoxRunOffDay);
		jPanelTimer.add(jCheckBoxRunNow);
		jTextAreaMessages.setEditable(false);
		jTextAreaMessages.setBorder(BorderFactory.createEtchedBorder());
		jTextAreaMessages.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTextAreaMessages.setFocusable(false);
		jTextAreaMessages.setLineWrap(true);
		jTextAreaMessages.setWrapStyleWord(true);
		jTextAreaMessages.setOpaque(false);
		DefaultCaret caret = (DefaultCaret)jTextAreaMessages.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		jScrollPaneMessages.getViewport().add(jTextAreaMessages, null);
		jPanelBottom.setPreferredSize(new Dimension(10, 35));
		jPanelBottom.setLayout(new BorderLayout());
		jPanelBottom.setBorder(null);
		jLabelFunctionID.setFont(new java.awt.Font(session_.systemFont, 0, FONT_SIZE-2));
		jLabelFunctionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelFunctionID.setForeground(Color.gray);
		jLabelFunctionID.setFocusable(false);
		jLabelSessionID.setFont(new java.awt.Font(session_.systemFont, 0, FONT_SIZE-2));
		jLabelSessionID.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelSessionID.setForeground(Color.gray);
		jLabelSessionID.setFocusable(false);
		jPanelButtons.setBorder(null);
		jPanelButtons.setLayout(null);
		jPanelButtons.setFocusable(false);
		gridLayoutInfo.setColumns(1);
		gridLayoutInfo.setRows(2);
		gridLayoutInfo.setVgap(0);
		jPanelInfo.setLayout(gridLayoutInfo);
		jPanelInfo.add(jLabelSessionID);
		jPanelInfo.add(jLabelFunctionID);
		jPanelInfo.setFocusable(false);
		jProgressBar.setStringPainted(true);
		jPanelMain.add(jPanelTop, BorderLayout.CENTER);
		jPanelMain.add(jPanelBottom, BorderLayout.SOUTH);
		jPanelTop.add(jPanelTimer, BorderLayout.NORTH);
		jPanelTop.add(jScrollPaneMessages, BorderLayout.CENTER);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		jPanelBottom.add(jPanelButtons, BorderLayout.CENTER);
		jButtonExit.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jButtonExit.setText(XFUtility.RESOURCE.getString("ExitButton"));
		jButtonExit.setBounds(new Rectangle(5, 2, 120, 32));
		jButtonExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (timer != null) {
					stopTimer();
				}
				closeFunction();
			}
		});
		jButtonStart.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jButtonStart.setText(XFUtility.RESOURCE.getString("TimerStart"));
		jButtonStart.setBounds(new Rectangle(160, 2, 120, 32));
		jButtonStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startTimer();
			}
		});
		jButtonStop.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jButtonStop.setText(XFUtility.RESOURCE.getString("TimerStop"));
		jButtonStop.setBounds(new Rectangle(315, 2, 120, 32));
		jButtonStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (timer != null) {
					stopTimer();
				}
			}
		});
		jButtonIconify.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jButtonIconify.setText(XFUtility.RESOURCE.getString("Iconify"));
		jButtonIconify.setBounds(new Rectangle(470, 2, 120, 32));
		jButtonIconify.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				session_.setExtendedState(JFrame.ICONIFIED | session_.getExtendedState());
			}
		});
		jPanelButtons.add(jButtonExit);
		jPanelButtons.add(jButtonStart);
		jPanelButtons.add(jButtonStop);
		jPanelButtons.add(jButtonIconify);
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
			instanceIsAvailable_ = false;
			errorHasOccured = false;
			exceptionLog = new ByteArrayOutputStream();
			exceptionStream = new PrintStream(exceptionLog);
			exceptionHeader = "";
			processLog.delete(0, processLog.length());
			variantMap.clear();
			scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
			engineScriptBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
			timer = null;
			
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
			
			////////////////////////////////////
			// Run Script or Show Timer Panel //
			////////////////////////////////////
			if (functionElement_.getAttribute("TimerOption").equals("")) {
				runScript();
				closeFunction();
			} else {
				jLabelSessionID.setText(session_.getSessionID());
				if (instanceArrayIndex_ >= 0) {
					jLabelFunctionID.setText("000" + "-" + instanceArrayIndex_ + "-" + functionElement_.getAttribute("ID"));
				} else {
					jLabelFunctionID.setText("000" + "-" + functionElement_.getAttribute("ID"));
				}
				FontMetrics metrics = jLabelFunctionID.getFontMetrics(new java.awt.Font("Dialog", 0, FONT_SIZE));
				jPanelInfo.setPreferredSize(new Dimension(metrics.stringWidth(jLabelFunctionID.getText()), 35));

				if (functionElement_.getAttribute("TimerOption").contains(":")) {
					setupConsoleWithTimer();
				} else {
					if (functionElement_.getAttribute("TimerOption").equals("CONSOLE")) {
						setupConsoleWithoutTimer();
					}
				}

				this.setVisible(true);
			}

		} catch(ScriptException e) {
			cancelWithScriptException(e, "");
		} catch (Exception e) {
			cancelWithException(e);
		} finally {
			instanceIsAvailable_ = true;
		}

		return returnMap_;
	}

	public void setFunctionSpecifications(org.w3c.dom.Element functionElement) throws Exception {
		functionElement_ = functionElement;
		if (!functionElement_.getAttribute("TimerOption").equals("")) {
			this.setTitle(functionElement_.getAttribute("Name"));
			this.setSize(new Dimension(scrSize.width, scrSize.height));
			int width = 700;
			int height = 450;
			this.setPreferredSize(new Dimension(width, height));
			int posX = (scrSize.width - width) / 2;
			int posY = (scrSize.height - height) / 2;
			this.setLocation(posX, posY);
			this.pack();
		}
	}
	
	void setupConsoleWithTimer() {
		jLabelTime.setEnabled(true);
		jTextFieldTime.setEnabled(true);
		jTextFieldTime.setText(functionElement_.getAttribute("TimerOption"));

		jCheckBoxRunNow.setSelected(false);
		jCheckBoxRunNow.setEnabled(true);
		jCheckBoxRepeat.setSelected(true);
		jCheckBoxRepeat.setEnabled(true);
		jCheckBoxRunOffDay.setSelected(false);
		jCheckBoxRunOffDay.setEnabled(true);

		if (functionElement_.getAttribute("TimerMessage").equals("")) {
			jTextAreaMessages.setText("> " + XFUtility.RESOURCE.getString("FunctionMessage44") + "\n");
		} else {
			jTextAreaMessages.setText(functionElement_.getAttribute("TimerMessage") + "\n");
		}

		jButtonStart.setEnabled(true);
		jButtonStop.setEnabled(false);
		jButtonIconify.setEnabled(false);
		jPanelBottom.remove(jProgressBar);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		this.getRootPane().setDefaultButton(jButtonStart);
	}
	
	void setupConsoleWithoutTimer() {
		firstTime = true;

		jLabelCondition.setEnabled(false);
		jLabelTime.setEnabled(false);
		jTextFieldTime.setEnabled(false);
		jTextFieldTime.setText("");

		jCheckBoxRunNow.setSelected(true);
		jCheckBoxRunNow.setEnabled(false);
		jCheckBoxRepeat.setSelected(false);
		jCheckBoxRepeat.setEnabled(false);
		jCheckBoxRunOffDay.setSelected(false);
		jCheckBoxRunOffDay.setEnabled(false);

		jTextAreaMessages.setText("");

		jButtonStart.setEnabled(false);
		jButtonStop.setEnabled(false);
		jButtonIconify.setEnabled(false);
		jPanelBottom.remove(jProgressBar);
		jPanelBottom.add(jPanelInfo, BorderLayout.EAST);
		this.getRootPane().setDefaultButton(jButtonExit);
	}

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			if (timer != null) {
				stopTimer();
			}
			closeFunction();
		}
		if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
			if (firstTime && functionElement_.getAttribute("TimerOption").equals("CONSOLE")) {
				runNow();
				firstTime = false;
			}
		}
	}

	public boolean isAvailable() {
		return instanceIsAvailable_;
	}

	public void callFunction(String functionID) {
		if (this.isVisible()) {
			jTextAreaMessages.setText(getNewMessage(session_.getFunctionName(functionID) + "(" + functionID + ")", XFUtility.RESOURCE.getString("FunctionMessage38")));
			jScrollPaneMessages.paintImmediately(0,0,jScrollPaneMessages.getWidth(),jScrollPaneMessages.getHeight());
		}

		try {
			returnMap_ = session_.executeFunction(functionID, parmMap_);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			if (this.isVisible()) {
				jTextAreaMessages.setText(getNewMessage(e.getMessage(), ""));
			}
			exceptionHeader = e.getMessage();
			setErrorAndCloseFunction();
		}

		if (this.isVisible() && returnMap_.get("RETURN_MESSAGE") != null
				&& !returnMap_.get("RETURN_MESSAGE").equals("")) {
			jTextAreaMessages.setText(getNewMessage(returnMap_.get("RETURN_MESSAGE").toString(), ""));
			jScrollPaneMessages.paintImmediately(0,0,jScrollPaneMessages.getWidth(),jScrollPaneMessages.getHeight());
		}
		if (returnMap_.get("RETURN_CODE").equals("99")) {
			errorHasOccured = true;
		}
	}
	
	public void commit() {
		session_.commit(true, processLog);
	}
	
	public void rollback() {
		session_.commit(false, processLog);
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
	
	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(null, message);
		}
		errorHasOccured = true;
	}
	
	public void cancelWithScriptException(ScriptException e, String scriptName) {
		if (scriptName.equals("")) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError12"));
			exceptionHeader = "'Script error in the function'\n";
		} else {
			JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError7") + scriptName + XFUtility.RESOURCE.getString("FunctionError8"));
			exceptionHeader = "'" + scriptName + "' Script error\n";
		}
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

	public void setErrorAndCloseFunction() {
		errorHasOccured = true;
		closeFunction();
	}

	void closeFunction() {
		/////////////////////////////////////////////////////////
		// Function type XF000 does not commit expressly. This //
		// means programmer is responsible to code committing. //
		/////////////////////////////////////////////////////////

		if (errorHasOccured) {
			returnMap_.put("RETURN_CODE", "99");
			this.rollback();
		}

		if (returnMap_.get("RETURN_MESSAGE") != null && !returnMap_.get("RETURN_MESSAGE").equals("")) {
			setProcessLog(returnMap_.get("RETURN_MESSAGE").toString());
		}
		instanceIsAvailable_ = true;

		String errorLog = "";
		if (exceptionLog.size() > 0 || !exceptionHeader.equals("")) {
			errorLog = exceptionHeader + exceptionLog.toString();
		}
		String wrkStr = processLog.toString();
		if (!functionElement_.getAttribute("TimerOption").equals("")) {
			wrkStr = wrkStr + "\n\n<Console Log>\n" + jTextAreaMessages.getText();
		}
		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), wrkStr, errorLog);

		if (this.isVisible()) {
			this.setVisible(false);
		}
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

	public Session getSession() {
		return session_;
	}

	public PrintStream getExceptionStream() {
		return exceptionStream;
	}

	public Object getFieldObjectByID(String tableID, String fieldID) {
		return null;
	}

	public void runScript() throws ScriptException, Exception {
		engineScriptBindings.clear();
		engineScriptBindings.put("instance", (XFScriptable)this);
		String scriptText = XFUtility.substringLinesWithTokenOfEOL(functionElement_.getAttribute("Script"), "\n");
		if (!scriptText.equals("")) {

			StringBuffer bf = new StringBuffer();
			bf.append(scriptText);
			bf.append(session_.getScriptFunctions());
			scriptEngine.eval(bf.toString());

			if (returnMap_.get("RETURN_CODE").equals("99")) {
				errorHasOccured = true;
				if (this.isVisible()) {
					if (returnMap_.get("RETURN_MESSAGE") != null && !returnMap_.get("RETURN_MESSAGE").equals("")) {
						jTextAreaMessages.setText(getNewMessage(returnMap_.get("RETURN_MESSAGE").toString(), ""));
						returnMap_.remove("RETURN_MESSAGE");
					} else {
						jTextAreaMessages.setText(getNewMessage(XFUtility.RESOURCE.getString("FunctionMessage51"), "") + "\n");
					}
				}
			} else {
				if (this.isVisible()) {
					if (returnMap_.get("RETURN_MESSAGE") != null && !returnMap_.get("RETURN_MESSAGE").equals("")) {
						jTextAreaMessages.setText(getNewMessage(returnMap_.get("RETURN_MESSAGE").toString(), ""));
						returnMap_.remove("RETURN_MESSAGE");
					} else {
						jTextAreaMessages.setText(getNewMessage(XFUtility.RESOURCE.getString("FunctionMessage48"), "") + "\n");
					}
				}
			}
		}
	}

	private void runNow() {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));
			jTextAreaMessages.setText(getNewMessage(XFUtility.RESOURCE.getString("FunctionMessage47"), ""));
			runScript();
			jButtonStart.setEnabled(false);
		} catch(ScriptException e) {
			cancelWithScriptException(e, "");
		} catch (Exception e) {
			cancelWithException(e);
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	private void startTimer() {
		if (jCheckBoxRunNow.isSelected()) {
			runNow();
		} else {
			if (isValidTimeValue()) {
				jLabelCondition.setEnabled(false);
				jLabelTime.setEnabled(false);
				jTextFieldTime.setEnabled(false);
				jCheckBoxRunNow.setEnabled(false);
				jCheckBoxRepeat.setEnabled(false);
				jCheckBoxRunOffDay.setEnabled(false);
				jButtonStart.setEnabled(false);
				jButtonStop.setEnabled(true);
				jButtonIconify.setEnabled(true);
				this.getRootPane().setDefaultButton(jButtonStop);
				Calendar date = getNextDateTimeToRun();
				if (date != null) {
					alreadyRun = false;
					timer = new Timer(true);
					task = new TimerTaskScript();
					timer.schedule(task, date.getTime());
					jTextAreaMessages.setText(getNewMessage(formatter.format(date.getTime()), XFUtility.RESOURCE.getString("FunctionMessage45") + "\n"));
				}
			} else {
				JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("TimerError"));
			}
		}
	}
	
	boolean isValidTimeValue() {
		boolean isValid = false;
		int pos1, pos2, wrkInt1, wrkInt2;
		int previousHour = 0;
		int previousMin = 0;
		String wrkStr, hour, min, sec;
		String hourFrom = "";
		String hourThru = "";
		String minFrom = "";
		String minThru = "";
		StringTokenizer workTokenizer1, workTokenizer2;
		
		timeList.clear();
		timeRepeatFrom = "";
		timeRepeatThru = "";
		intervalMilliSeconds = 0;
		if (jTextFieldTime.getText().contains("LIST(") || jTextFieldTime.getText().contains("REPEAT(")) {
			isValid = true;
			pos1 = jTextFieldTime.getText().indexOf("LIST(");
			if (pos1 >= 0) {
				pos2 = jTextFieldTime.getText().indexOf(")", pos1);
				if (pos2 >= 0) {
					wrkStr = jTextFieldTime.getText().substring(pos1+5, pos2).replace(" " , "");
					workTokenizer1 = new StringTokenizer(wrkStr, "," );
					if (workTokenizer1.countTokens() > 0) {
						while (workTokenizer1.hasMoreTokens()) {
							wrkStr = workTokenizer1.nextToken();
							workTokenizer2 = new StringTokenizer(wrkStr, ":" );
							if (workTokenizer2.countTokens() == 2) {
								try {
									hour = workTokenizer2.nextToken();
									min = workTokenizer2.nextToken();
									wrkInt1 = Integer.parseInt(hour);
									wrkInt2 = Integer.parseInt(min);
									if (wrkInt1 >= 0 && wrkInt1 <= 23
											&& wrkInt2 >= 0 && wrkInt2 <= 59) {
										if ((wrkInt1 > previousHour) 
												|| (wrkInt1 == previousHour && wrkInt2 >= previousMin)) {
											timeList.add(wrkStr);
											previousHour = wrkInt1;
											previousMin = wrkInt2;
										} else {
											isValid = false;
											break;
										}
									} else {
										isValid = false;
										break;
									}
								} catch (NumberFormatException e) {
									isValid = false;
									break;
								}
							} else {
								isValid = false;
								break;
							}
						}
					} else {
						isValid = false;
					}
				}
			}
			if (isValid) {
				pos1 = jTextFieldTime.getText().indexOf("REPEAT(");
				if (pos1 >= 0) {
					pos2 = jTextFieldTime.getText().indexOf(")", pos1);
					if (pos2 >= 0) {
						wrkStr = jTextFieldTime.getText().substring(pos1+7, pos2).replace(" " , "");
						workTokenizer1 = new StringTokenizer(wrkStr, "," );
						if (workTokenizer1.countTokens() == 3) {
							timeRepeatFrom = workTokenizer1.nextToken();
							timeRepeatThru = workTokenizer1.nextToken();
							sec = workTokenizer1.nextToken();

							workTokenizer2 = new StringTokenizer(timeRepeatFrom, ":" );
							if (workTokenizer2.countTokens() == 2) {
								try {
									hourFrom = workTokenizer2.nextToken();
									minFrom = workTokenizer2.nextToken();
									wrkInt1 = Integer.parseInt(hourFrom);
									wrkInt2 = Integer.parseInt(minFrom);
									if (wrkInt1 >= 0 && wrkInt1 <= 23
											&& wrkInt2 >= 0 && wrkInt2 <= 59) {
									} else {
										isValid = false;
									}
								} catch (NumberFormatException e) {
									isValid = false;
								}
							} else {
								isValid = false;
							}

							workTokenizer2 = new StringTokenizer(timeRepeatThru, ":" );
							if (workTokenizer2.countTokens() == 2) {
								try {
									hourThru = workTokenizer2.nextToken();
									minThru = workTokenizer2.nextToken();
									wrkInt1 = Integer.parseInt(hourThru);
									wrkInt2 = Integer.parseInt(minThru);
									if (wrkInt1 >= 0 && wrkInt1 <= 23
											&& wrkInt2 >= 0 && wrkInt2 <= 59) {
									} else {
										isValid = false;
									}
								} catch (NumberFormatException e) {
									isValid = false;
								}
							} else {
								isValid = false;
							}
							
							if (isValid) {
								int diff = timeRepeatFrom.compareTo(timeRepeatThru);
								if (diff >= 0) {
									isValid = false;
								} else {
									try {
										float wrkFloat = Float.parseFloat(sec) * 1000f;
										intervalMilliSeconds = (int)wrkFloat;
										if (intervalMilliSeconds > 0 && intervalMilliSeconds < 86400000) {
										} else {
											isValid = false;
										}
									} catch (NumberFormatException e) {
										isValid = false;
									}
								}
							}

						} else {
							isValid = false;
						}
					} else {
						isValid = false;
					}
				}
			}
		} else {
			if (!jTextFieldTime.getText().equals("")) {
				workTokenizer1 = new StringTokenizer(jTextFieldTime.getText(), ":" );
				if (workTokenizer1.countTokens() == 2) {
					isValid = true;
					try {
						hour = workTokenizer1.nextToken();
						min = workTokenizer1.nextToken();
						wrkInt1 = Integer.parseInt(hour);
						wrkInt2 = Integer.parseInt(min);
						if (wrkInt1 >= 0 && wrkInt1 <= 23
								&& wrkInt2 >= 0 && wrkInt2 <= 59) {
							timeList.add(jTextFieldTime.getText());
						} else {
							isValid = false;
						}
					} catch (NumberFormatException e) {
						isValid = false;
					}
				}
			}
		}
		return isValid;
	}

	private void stopTimer() {
		jLabelCondition.setEnabled(true);
		jLabelTime.setEnabled(true);
		jTextFieldTime.setEnabled(true);
		jCheckBoxRunNow.setEnabled(true);
		jCheckBoxRepeat.setEnabled(true);
		jCheckBoxRunOffDay.setEnabled(true);
		jButtonStart.setEnabled(true);
		jButtonStop.setEnabled(false);
		jButtonIconify.setEnabled(false);
		this.getRootPane().setDefaultButton(jButtonStart);
		if (timer != null) {
			timer.cancel();
			task = null;
		}
		jTextAreaMessages.setText(getNewMessage(XFUtility.RESOURCE.getString("FunctionMessage46"), ""));
	}
	
	private Calendar getNextDateTimeToRun() {
		int diff;
		String wrkStr;
		Calendar date = null;
		java.util.Date wrkDate;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		if (jCheckBoxRepeat.isSelected() || !alreadyRun) {
			Calendar currentDateTime = Calendar.getInstance();
			Calendar targetDateTime = getTargetDateTime(currentDateTime);
			diff = targetDateTime.compareTo(currentDateTime);
			if (diff <= 0) {
				targetDateTime.add(Calendar.DAY_OF_MONTH, 1);
			}
			if (!jCheckBoxRunOffDay.isSelected()) {
				wrkDate = targetDateTime.getTime();
				wrkStr = df.format(wrkDate);
				while (session_.isOffDate(wrkStr)) {
					targetDateTime.add(Calendar.DAY_OF_MONTH, 1);
					wrkDate = targetDateTime.getTime();
					wrkStr = df.format(wrkDate);
				}
			}
			date = targetDateTime;
			alreadyRun = true;
		}
		return date;
	}
	
	Calendar getTargetDateTime(Calendar currentDateTime) {
		int hour1 = 0;
		int min1= 0;
		int hour2 =0;
		int min2 = 0;
		int diff, hour, min;
		StringTokenizer workTokenizer;
		Calendar targetDateTime = null;
		Calendar dateTime1 = null;
		Calendar dateTime2 = null;
		Calendar dateTime3 = null;

		if (timeList.size() > 0) {
			dateTime1 = Calendar.getInstance();
			for (int i=0; i<timeList.size(); i++) {
				workTokenizer = new StringTokenizer(timeList.get(i), ":" );
				hour = Integer.parseInt(workTokenizer.nextToken());
				min = Integer.parseInt(workTokenizer.nextToken());
				dateTime1.set(Calendar.HOUR_OF_DAY, hour);
				dateTime1.set(Calendar.MINUTE, min);
				dateTime1.set(Calendar.SECOND, 0);
				dateTime1.set(Calendar.MILLISECOND, 0);
				diff = dateTime1.compareTo(currentDateTime);
				if (diff >= 0) {
					break;
				}
			}
			diff = dateTime1.compareTo(currentDateTime);
			if (diff < 0) {
				dateTime1 = null;
			}
		}

		if (!timeRepeatFrom.equals("")) {
			dateTime2 = Calendar.getInstance();
			workTokenizer = new StringTokenizer(timeRepeatFrom, ":" );
			hour = Integer.parseInt(workTokenizer.nextToken());
			min = Integer.parseInt(workTokenizer.nextToken());
			dateTime2.set(Calendar.HOUR_OF_DAY, hour);
			dateTime2.set(Calendar.MINUTE, min);
			dateTime2.set(Calendar.SECOND, 0);
			dateTime2.set(Calendar.MILLISECOND, 0);

			dateTime3 = Calendar.getInstance();
			workTokenizer = new StringTokenizer(timeRepeatThru, ":" );
			hour = Integer.parseInt(workTokenizer.nextToken());
			min = Integer.parseInt(workTokenizer.nextToken());
			dateTime3.set(Calendar.HOUR_OF_DAY, hour);
			dateTime3.set(Calendar.MINUTE, min);
			dateTime3.set(Calendar.SECOND, 0);
			dateTime3.set(Calendar.MILLISECOND, 0);

			diff = dateTime2.compareTo(currentDateTime);
			if (diff <  0) {
				int wrkInt = 0;
				for (;;) {
					wrkInt = wrkInt + intervalMilliSeconds;
					dateTime2.set(Calendar.MILLISECOND, wrkInt);
					diff = dateTime3.compareTo(dateTime2);
					if (diff < 0) {
						dateTime2 = null;
						break;
					} else {
						diff = dateTime2.compareTo(currentDateTime);
						if (diff >= 0) {
							break;
						}
					}
				}
			}
		}
		
		if (dateTime1 != null && dateTime2 != null) {
			diff = dateTime1.compareTo(dateTime2);
			if (diff < 0) {
				targetDateTime = dateTime1;
			} else {
				targetDateTime = dateTime2;
			}
		} else {
			if (dateTime1 != null) {
				targetDateTime = dateTime1;
			} else {
				targetDateTime = dateTime2;
			}
		}
		
		if (targetDateTime == null) {
			targetDateTime = Calendar.getInstance();
			if (timeList.size() > 0) {
				workTokenizer = new StringTokenizer(timeList.get(0), ":" );
				hour1 = Integer.parseInt(workTokenizer.nextToken());
				min1 = Integer.parseInt(workTokenizer.nextToken());
			}
			if (!timeRepeatFrom.equals("")) {
				workTokenizer = new StringTokenizer(timeRepeatFrom, ":" );
				hour2 = Integer.parseInt(workTokenizer.nextToken());
				min2 = Integer.parseInt(workTokenizer.nextToken());
			}
			if (timeList.size() > 0 && timeRepeatFrom.equals("")) {
				targetDateTime.set(Calendar.HOUR_OF_DAY, hour1);
				targetDateTime.set(Calendar.MINUTE, min1);
			}
			if (timeList.size() == 0 && !timeRepeatFrom.equals("")) {
				targetDateTime.set(Calendar.HOUR_OF_DAY, hour2);
				targetDateTime.set(Calendar.MINUTE, min2);
			}
			if (timeList.size() > 0 && !timeRepeatFrom.equals("")) {
				if (hour1 < hour2) {
					targetDateTime.set(Calendar.HOUR_OF_DAY, hour1);
					targetDateTime.set(Calendar.MINUTE, min1);
				} else {
					if (hour1 == hour2) {
						if (min1 <= min2) {
							targetDateTime.set(Calendar.HOUR_OF_DAY, hour1);
							targetDateTime.set(Calendar.MINUTE, min1);
						} else {
							targetDateTime.set(Calendar.HOUR_OF_DAY, hour2);
							targetDateTime.set(Calendar.MINUTE, min2);
						}
					} else {
						targetDateTime.set(Calendar.HOUR_OF_DAY, hour2);
						targetDateTime.set(Calendar.MINUTE, min2);
					}
				}
			}
			targetDateTime.set(Calendar.SECOND, 0);
			targetDateTime.set(Calendar.MILLISECOND, 0);
		}

		return targetDateTime;
	}
	
	class TimerTaskScript extends TimerTask {
		public void run() {
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				jTextAreaMessages.setText(getNewMessage(XFUtility.RESOURCE.getString("FunctionMessage47"), ""));
				runScript();
				jButtonStart.setEnabled(false);
//				if (errorHasOccured) {
//					jTextAreaMessages.setText(getNewMessage(XFUtility.RESOURCE.getString("FunctionMessage51"), "") + "\n");
//				} else {
//					jTextAreaMessages.setText(getNewMessage(XFUtility.RESOURCE.getString("FunctionMessage48"), "") + "\n");
//				}
				if (jCheckBoxRepeat.isSelected()) {
					startTimer();
				} else {
					stopTimer();
				}
			} catch(ScriptException e) {
				cancelWithScriptException(e, "");
			} catch (Exception e) {
				cancelWithException(e);
			} finally {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}
	
	private void setRunNow() {
		if (jCheckBoxRunNow.isSelected()) {
			jLabelTime.setEnabled(false);
			jTextFieldTime.setEnabled(false);
			jCheckBoxRepeat.setEnabled(false);
			jCheckBoxRunOffDay.setEnabled(false);
		} else {
			jLabelTime.setEnabled(true);
			jTextFieldTime.setEnabled(true);
			jCheckBoxRepeat.setEnabled(true);
			jCheckBoxRunOffDay.setEnabled(true);
		}
	}
	
	private String getNewMessage(String newMessage1, String newMessage2) {
		StringBuffer bf = new StringBuffer();
		if (!jTextAreaMessages.getText().equals("")) {
			bf.append(jTextAreaMessages.getText());
			bf.append("\n");
		}
		bf.append("> ");
		bf.append(newMessage1);
		if (!newMessage2.equals("")) {
			bf.append(" ");
			bf.append(newMessage2);
		}
		calendar = Calendar.getInstance();
		bf.append("(");
		bf.append(formatter.format(calendar.getTime()));
		bf.append(")");
		return bf.toString();
	}
}