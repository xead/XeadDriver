//
///*
// * Copyright (c) 2011 WATANABE kozo <qyf05466@nifty.com>,
// * All rights reserved.
// *
// * This file is part of XEAD Driver.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// *     * Redistributions of source code must retain the above copyright
// *       notice, this list of conditions and the following disclaimer.
// *     * Redistributions in binary form must reproduce the above copyright
// *       notice, this list of conditions and the following disclaimer in the
// *       documentation and/or other materials provided with the distribution.
// *     * Neither the name of the XEAD Project nor the names of its contributors
// *       may be used to endorse or promote products derived from this software
// *       without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// * POSSIBILITY OF SUCH DAMAGE.
// */
//
//import javax.script.Bindings;
//import javax.script.ScriptContext;
//import javax.script.ScriptEngine;
//import javax.script.ScriptException;
//import javax.swing.*;
//import java.io.ByteArrayOutputStream;
//import java.io.PrintStream;
//import java.sql.Connection;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.ResourceBundle;
//import java.util.StringTokenizer;
//import org.w3c.dom.*;
//
//public class XF010 extends Object implements XFExecutable, XFScriptable {
//	private static final long serialVersionUID = 1L;
//	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
//	private org.w3c.dom.Element functionElement_ = null;
//	private HashMap<String, Object> parmMap_ = null;
//	private HashMap<String, Object> returnMap_ = new HashMap<String, Object>();
//	private XF010_PrimaryTable primaryTable_ = null;
//	private Session session_ = null;
//	private boolean instanceIsAvailable_ = true;
//	private int programSequence;
//	private StringBuffer processLog = new StringBuffer();
//	private ArrayList<XF010_ReferTable> referTableList = new ArrayList<XF010_ReferTable>();
//	private ArrayList<XF010_Field> fieldList = new ArrayList<XF010_Field>();
//	private Connection connection = null;
//	private ScriptEngine scriptEngine;
//	private Bindings engineScriptBindings;
//	private String scriptNameRunning = "";
//	private ByteArrayOutputStream exceptionLog;
//	private PrintStream exceptionStream;
//	private String exceptionHeader = "";
//
//	public XF010(Session session, int instanceArrayIndex) {
//		super();
//		try {
//			session_ = session;
//			//
//		} catch(Exception e) {
//			e.printStackTrace(exceptionStream);
//		}
//	}
//
//	public boolean isAvailable() {
//		return instanceIsAvailable_;
//	}
//
//	public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parmMap) {
//		SortableDomElementListModel sortedList;
//		String workAlias, workTableID, workFieldID;
//		StringTokenizer workTokenizer;
//		org.w3c.dom.Element workElement;
//
//		try {
//
//			///////////////////
//			// Process parms //
//			///////////////////
//			parmMap_ = parmMap;
//			if (parmMap_ == null) {
//				parmMap_ = new HashMap<String, Object>();
//			}
//			returnMap_.clear();
//			returnMap_.putAll(parmMap_);
//			returnMap_.put("RETURN_CODE", "00");
//
//			/////////////////////////
//			// Initialize variants //
//			/////////////////////////
//			instanceIsAvailable_ = false;
//			exceptionLog = new ByteArrayOutputStream();
//			exceptionStream = new PrintStream(exceptionLog);
//			exceptionHeader = "";
//			functionElement_ = functionElement;
//			processLog.delete(0, processLog.length());
//			connection = session_.getConnection();
//			programSequence = session_.writeLogOfFunctionStarted(functionElement_.getAttribute("ID"), functionElement_.getAttribute("Name"));
//
//			//////////////////////////////////////
//			// Setup Script Engine and Bindings //
//			//////////////////////////////////////
//			scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
//			engineScriptBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
//			engineScriptBindings.clear();
//			engineScriptBindings.put("instance", (XFScriptable)this);
//			//engineScriptBindings.put("session", (XFScriptableSession)session_);
//			
//			//////////////////////////////////////////////
//			// Setup the primary table and refer tables //
//			//////////////////////////////////////////////
//			primaryTable_ = new XF010_PrimaryTable(functionElement_, this);
//			referTableList.clear();
//			NodeList referNodeList = primaryTable_.getTableElement().getElementsByTagName("Refer");
//			sortedList = XFUtility.getSortedListModel(referNodeList, "Order");
//			for (int i = 0; i < sortedList.getSize(); i++) {
//				org.w3c.dom.Element element = (org.w3c.dom.Element)sortedList.getElementAt(i);
//				referTableList.add(new XF010_ReferTable(element, this));
//			}
//
//			/////////////////////////
//			// Setup column fields //
//			/////////////////////////
//			fieldList.clear();
//			NodeList functionFieldList = functionElement_.getElementsByTagName("Field");
//			SortableDomElementListModel sortableList = XFUtility.getSortedListModel(functionFieldList, "DataSource");
//			for (int i = 0; i < sortableList.getSize(); i++) {
//				fieldList.add(new XF010_Field((org.w3c.dom.Element)sortableList.getElementAt(i), this));
//			}
//			//
//			// Add primary table key fields as HIDDEN column if they are not on the column list //
//			for (int i = 0; i < primaryTable_.getKeyFieldList().size(); i++) {
//				if (!existsInColumnList(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldList().get(i))) {
//					fieldList.add(new XF010_Field(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldList().get(i), this));
//				}
//			}
//			//
//			// Analyze fields in script and add them as HIDDEN columns if necessary //
//			for (int i = 0; i < primaryTable_.getScriptList().size(); i++) {
//				if	(primaryTable_.getScriptList().get(i).isToBeRunAtEvent("BR", "")
//				  || primaryTable_.getScriptList().get(i).isToBeRunAtEvent("AR", "")) {
//					for (int j = 0; j < primaryTable_.getScriptList().get(i).getFieldList().size(); j++) {
//						workTokenizer = new StringTokenizer(primaryTable_.getScriptList().get(i).getFieldList().get(j), "." );
//						workAlias = workTokenizer.nextToken();
//						workTableID = getTableIDOfTableAlias(workAlias);
//						workFieldID = workTokenizer.nextToken();
//						if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
//							workElement = session_.getFieldElement(workTableID, workFieldID);
//							if (workElement == null) {
//								String msg = res.getString("FunctionError1") + primaryTable_.getTableID() + res.getString("FunctionError2") + primaryTable_.getScriptList().get(i).getName() + res.getString("FunctionError3") + workAlias + "_" + workFieldID + res.getString("FunctionError4");
//								JOptionPane.showMessageDialog(null, msg);
//								throw new Exception(msg);
//							} else {
//								if (primaryTable_.isValidDataSource(workTableID, workAlias, workFieldID)) {
//									fieldList.add(new XF010_Field(workTableID, workAlias, workFieldID, this));
//								}
//							}
//						}
//					}
//				}
//			}
//			//
//			// Analyze refer tables and add their fields as HIDDEN columns if necessary //
//			for (int i = referTableList.size()-1; i > -1; i--) {
//				for (int j = 0; j < referTableList.get(i).getFieldIDList().size(); j++) {
//					if (existsInColumnList(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j))) {
//						referTableList.get(i).setToBeExecuted(true);
//						break;
//					}
//				}
//				if (referTableList.get(i).isToBeExecuted()) {
//					for (int j = 0; j < referTableList.get(i).getFieldIDList().size(); j++) {
//						if (!existsInColumnList(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j))) {
//							fieldList.add(new XF010_Field(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j), this));
//						}
//					}
//					for (int j = 0; j < referTableList.get(i).getWithKeyFieldIDList().size(); j++) {
//						workTokenizer = new StringTokenizer(referTableList.get(i).getWithKeyFieldIDList().get(j), "." );
//						workAlias = workTokenizer.nextToken();
//						workTableID = getTableIDOfTableAlias(workAlias);
//						workFieldID = workTokenizer.nextToken();
//						if (!existsInColumnList(workTableID, workAlias, workFieldID)) {
//							fieldList.add(new XF010_Field(workTableID, workAlias, workFieldID, this));
//						}
//					}
//				}
//			}
//
//			//////////////////////////////////////////////////
//			// Fetch the record and set values on the panel //
//			//////////////////////////////////////////////////
//			fetchTableRecord();
//
//			///////////////////////////////
//			// Run function's own script //
//			///////////////////////////////
//			runFunctionScript();
//
//			///////////
//			// Close //
//			///////////
//			closeFunction();
//
//		} catch (Exception e) {
//			JOptionPane.showMessageDialog(null, res.getString("FunctionError5"));
//			e.printStackTrace(exceptionStream);
//			setErrorAndCloseFunction();
//		}
//
//		///////////////////////////////
//		// Release instance and exit //
//		///////////////////////////////
//		instanceIsAvailable_ = true;
//		return returnMap_;
//	}
//
//	void setErrorAndCloseFunction() {
//		returnMap_.put("RETURN_CODE", "99");
//		closeFunction();
//	}
//
//	void closeFunction() {
//		instanceIsAvailable_ = true;
//		//
//		String wrkStr;
//		if (exceptionLog.size() > 0 || !exceptionHeader.equals("")) {
//			wrkStr = processLog.toString() + "\nERROR LOG:\n" + exceptionHeader + exceptionLog.toString();
//		} else {
//			wrkStr = processLog.toString();
//		}
//		wrkStr = wrkStr.replace("'", "\"");
//		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), wrkStr);
//	}
//
//	void fetchTableRecord() {
//		try {
//			//
//			for (int i = 0; i < fieldList.size(); i++) {
//				if (parmMap_.containsKey(fieldList.get(i).getFieldID())) {
//					fieldList.get(i).setValue(parmMap_.get(fieldList.get(i).getFieldID()));
//				}
//			}
//			//
//			primaryTable_.runScript("BR", "BR()");
//			//
//			Statement statementForPrimaryTable = connection.createStatement();
//			String sql = primaryTable_.getSQLToSelect();
//			XFUtility.appendLog(sql, processLog);
//			ResultSet resultOfPrimaryTable = statementForPrimaryTable.executeQuery(sql);
//			if (resultOfPrimaryTable.next()) {
//				//
//				for (int i = 0; i < fieldList.size(); i++) {
//					if (fieldList.get(i).getTableID().equals(primaryTable_.getTableID())) {
//						fieldList.get(i).setValueOfResultSet(resultOfPrimaryTable);
//					}
//				}
//				//
//				primaryTable_.runScript("AR", "BR()");
//				//
//				fetchReferTableRecords("AR", false, "");
//				//
//				primaryTable_.runScript("AR", "AR()");
//				//
//				resultOfPrimaryTable.close();
//			}
//			//
//		} catch (SQLException e) {
//			JOptionPane.showMessageDialog(null, res.getString("FunctionError6"));
//			e.printStackTrace(exceptionStream);
//			setErrorAndCloseFunction();
//		} catch (ScriptException e) {
//			JOptionPane.showMessageDialog(null, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
//			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
//			e.printStackTrace(exceptionStream);
//			setErrorAndCloseFunction();
//		}
//	}
//
//	protected void fetchReferTableRecords(String event, boolean toBeChecked, String specificReferTable) {
//		ResultSet resultOfReferTable = null;
//		String sql = "";
//		//
//		try {
//			//
//			Statement statementForReferTable = connection.createStatement();
//			//
//			for (int i = 0; i < referTableList.size(); i++) {
//				if (specificReferTable.equals("") || specificReferTable.equals(referTableList.get(i).getTableAlias())) {
//					//
//					if (referTableList.get(i).isToBeExecuted()) {
//						//
//						primaryTable_.runScript(event, "BR(" + referTableList.get(i).getTableAlias() + ")");
//						//
//						for (int j = 0; j < fieldList.size(); j++) {
//							if (fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
//								fieldList.get(j).setValue(fieldList.get(j).getNullValue());
//							}
//						}
//						//
//						if (!referTableList.get(i).isKeyNullable() || !referTableList.get(i).isKeyNull()) {
//							//
//							sql = referTableList.get(i).getSelectSQL(false);
//							XFUtility.appendLog(sql, processLog);
//							resultOfReferTable = statementForReferTable.executeQuery(sql);
//							while (resultOfReferTable.next()) {
//								//
//								if (referTableList.get(i).isRecordToBeSelected(resultOfReferTable)) {
//									//
//									for (int j = 0; j < fieldList.size(); j++) {
//										if (fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
//											fieldList.get(j).setValueOfResultSet(resultOfReferTable);
//										}
//									}
//									//
//									primaryTable_.runScript(event, "AR(" + referTableList.get(i).getTableAlias() + ")");
//								}
//							}
//							//
//							resultOfReferTable.close();
//						}
//					}
//				}
//			}
//			//
//		} catch (SQLException e) {
//			JOptionPane.showMessageDialog(null, res.getString("FunctionError6"));
//			e.printStackTrace(exceptionStream);
//			setErrorAndCloseFunction();
//		} catch(ScriptException e) {
//			JOptionPane.showMessageDialog(null, res.getString("FunctionError7") + this.getScriptNameRunning() + res.getString("FunctionError8"));
//			exceptionHeader = "'" + this.getScriptNameRunning() + "' Script error\n";
//			e.printStackTrace(exceptionStream);
//			setErrorAndCloseFunction();
//		} catch(Exception e) {
//			JOptionPane.showMessageDialog(null, "An error occurred in Routine 'fetchReferTableRecords'" + e.getMessage());
//			e.printStackTrace(exceptionStream);
//			setErrorAndCloseFunction();
//		}
//	}
//
//	public String getScriptNameRunning() {
//		return scriptNameRunning;
//	}
//
//	public void runFunctionScript() throws ScriptException {
//		evalScript("Function's own script", XFUtility.substringLinesWithTokenOfEOL(functionElement_.getAttribute("Script"), "\n"));
//	}
//
//	public void callFunction(String functionID) {
//		try {
//			returnMap_ = XFUtility.callFunction(session_, functionID, parmMap_);
//		} catch (Exception e) {
//			String message = res.getString("FunctionError9") + functionID + res.getString("FunctionError10");
//			JOptionPane.showMessageDialog(null,message);
//			exceptionHeader = message;
//			setErrorAndCloseFunction();
//		}
//	}
//	
//	public void cancelWithMessage(String message) {
//		if (!message.equals("")) {
//			JOptionPane.showMessageDialog(null, message);
//		}
//		if (returnMap_.get("RETURN_CODE").equals("00")) {
//			returnMap_.put("RETURN_CODE", "01");
//		}
//	}
//
//	public String getFunctionID() {
//		return functionElement_.getAttribute("ID");
//	}
//
//	public HashMap<String, Object> getParmMap() {
//		return parmMap_;
//	}
//	
//	public void setProcessLog(String text) {
//		XFUtility.appendLog(text, processLog);
//	}
//
//	public HashMap<String, Object> getReturnMap() {
//		return returnMap_;
//	}
//
//	//public StringBuffer getProcessLog() {
//	//	return processLog;
//	//}
//
//	public Session getSession() {
//		return session_;
//	}
//
//	public PrintStream getExceptionStream() {
//		return exceptionStream;
//	}
//
//	public Bindings getEngineScriptBindings() {
//		return 	engineScriptBindings;
//	}
//	
//	public void evalScript(String scriptName, String scriptText) throws ScriptException {
//		if (!scriptText.equals("")) {
//			scriptNameRunning = scriptName;
//			scriptEngine.eval(scriptText);
//		}
//	}
//
//	public String getPrimaryTableID() {
//		return primaryTable_.getTableID();
//	}
//
//	public ArrayList<XF010_Field> getFieldList() {
//		return fieldList;
//	}
//
//	public XF010_Field getFieldObjectByID(String tableID, String tableAlias, String fieldID) {
//		XF010_Field field = null;
//		for (int i = 0; i < fieldList.size(); i++) {
//			if (tableID.equals("")) {
//				if (fieldList.get(i).getTableAlias().equals(tableAlias) && fieldList.get(i).getFieldID().equals(fieldID)) {
//					field = fieldList.get(i);
//					break;
//				}
//			}
//			if (tableAlias.equals("")) {
//				if (fieldList.get(i).getTableID().equals(tableID) && fieldList.get(i).getFieldID().equals(fieldID)) {
//					field = fieldList.get(i);
//					break;
//				}
//			}
//			if (!tableID.equals("") && !tableAlias.equals("")) {
//				if (fieldList.get(i).getTableID().equals(tableID) && fieldList.get(i).getTableAlias().equals(tableAlias) && fieldList.get(i).getFieldID().equals(fieldID)) {
//					field = fieldList.get(i);
//					break;
//				}
//			}
//		}
//		return field;
//	}
//
//	public ArrayList<String> getKeyFieldList() {
//		return primaryTable_.getKeyFieldList();
//	}
//
//	public ArrayList<XF010_ReferTable> getReferTableList() {
//		return referTableList;
//	}
//
//	public boolean existsInColumnList(String tableID, String tableAlias, String fieldID) {
//		boolean result = false;
//		for (int i = 0; i < fieldList.size(); i++) {
//			if (tableID.equals("")) {
//				if (fieldList.get(i).getTableAlias().equals(tableAlias)) {
//					result = true;
//				}
//			}
//			if (tableAlias.equals("")) {
//				if (fieldList.get(i).getTableID().equals(tableID) && fieldList.get(i).getFieldID().equals(fieldID)) {
//					result = true;
//				}
//			}
//			if (!tableID.equals("") && !tableAlias.equals("")) {
//				if (fieldList.get(i).getTableID().equals(tableID) && fieldList.get(i).getTableAlias().equals(tableAlias) && fieldList.get(i).getFieldID().equals(fieldID)) {
//					result = true;
//				}
//			}
//		}
//		return result;
//	}
//
//	public String getTableIDOfTableAlias(String tableAlias) {
//		String tableID = tableAlias;
//		XF010_ReferTable referTable;
//		for (int j = 0; j < referTableList.size(); j++) {
//			referTable = referTableList.get(j);
//			referTable.getTableAlias();
//			if (referTable.getTableAlias().equals(tableAlias)) {
//				tableID = referTable.getTableID();
//				break;
//			}
//		}
//		return tableID;
//	}
//
//	public Object getValueOfFieldByName(String dataSourceName) {
//		Object obj = null;
//		for (int i = 0; i < fieldList.size(); i++) {
//			if (fieldList.get(i).getDataSourceName().equals(dataSourceName)) {
//				obj = fieldList.get(i).getValue();
//				break;
//			}
//		}
//		return obj;
//	}
//
//	public HashMap<String, Object> getPrimaryFieldValueMap() {
//		HashMap<String, Object> map = new HashMap<String, Object>();
//		for (int i = 0; i < fieldList.size(); i++) {
//			if (fieldList.get(i).isFieldOnPrimaryTable()) {
//				map.put(fieldList.get(i).getDataSourceName(), fieldList.get(i).getValue());
//			}
//		}
//		return map;
//	}
//}
//
//
//class XF010_Field extends Object implements XFScriptableField {
//	private static final long serialVersionUID = 1L;
//	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
//	org.w3c.dom.Element functionFieldElement_ = null;
//	org.w3c.dom.Element tableElement = null;
//	private String dataSourceName = "";
//	private String tableID_ = "";
//	private String tableAlias_ = "";
//	private String fieldID_ = "";
//	private String dataType = "";
//	//private int dataSize = 5;
//	private String dataTypeOptions = "";
//	private ArrayList<String> dataTypeOptionList;
//	private boolean isNullable = true;
//	private boolean isKey = false;
//	private boolean isFieldOnPrimaryTable = false;
//	private boolean isVirtualField = false;
//	private boolean isRangeKeyFieldValid = false;
//	private boolean isRangeKeyFieldExpire = false;
//	private Object value_ = null;
//	private XF010 dialog_;
//
//	public XF010_Field(org.w3c.dom.Element functionFieldElement, XF010 dialog){
//		super();
//		//
//		dialog_ = dialog;
//		functionFieldElement_ = functionFieldElement;
//		//
//		dataSourceName = functionFieldElement_.getAttribute("DataSource");
//		StringTokenizer workTokenizer = new StringTokenizer(dataSourceName, "." );
//		tableAlias_ = workTokenizer.nextToken();
//		tableID_ = dialog_.getTableIDOfTableAlias(tableAlias_);
//		fieldID_ =workTokenizer.nextToken();
//		//
//		if (tableID_.equals(dialog_.getPrimaryTableID()) && tableAlias_.equals(tableID_)) {
//			isFieldOnPrimaryTable = true;
//			//
//			ArrayList<String> keyNameList = dialog_.getKeyFieldList();
//			for (int i = 0; i < keyNameList.size(); i++) {
//				if (keyNameList.get(i).equals(fieldID_)) {
//					isKey = true;
//					break;
//				}
//			}
//		}
//		//
//		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID_, fieldID_);
//		if (workElement == null) {
//			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + res.getString("FunctionError11"));
//		}
//		dataType = workElement.getAttribute("Type");
//		dataTypeOptions = workElement.getAttribute("TypeOptions");
//		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
//		//dataSize = Integer.parseInt(workElement.getAttribute("Size"));
//		//if (dataSize > 50) {
//		//	dataSize = 50;
//		//}
//		if (workElement.getAttribute("Nullable").equals("F")) {
//			isNullable = false;
//		}
//		if (dataTypeOptionList.contains("VIRTUAL")) {
//			isVirtualField = true;
//		}
//		//
//		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
//		if (!tableElement.getAttribute("RangeKey").equals("")) {
//			workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
//			if (workTokenizer.nextToken().equals(fieldID_)) {
//				isRangeKeyFieldValid = true;
//			}
//			if (workTokenizer.nextToken().equals(fieldID_)) {
//				isRangeKeyFieldExpire = true;
//			}
//		}
//		//
//		value_ = this.getNullValue();
//		//
//		if (tableID_.equals(dialog_.getPrimaryTableID())) {
//			Object value = dialog_.getParmMap().get(fieldID_);
//			if (value != null) {
//				value_ = value;
//			}
//		}
//		//
//		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
//	}
//
//	public XF010_Field(String tableID, String tableAlias, String fieldID, XF010 dialog){
//		super();
//		//
//		functionFieldElement_ = null;
//		tableID_ = tableID;
//		fieldID_ = fieldID;
//		dialog_ = dialog;
//		if (tableAlias.equals("")) {
//			tableAlias_ = tableID;
//		} else {
//			tableAlias_ = tableAlias;
//		}
//		dataSourceName = tableAlias_ + "." + fieldID_;
//		//
//		if (tableID_.equals(dialog_.getPrimaryTableID()) && tableAlias_.equals(tableID_)) {
//			isFieldOnPrimaryTable = true;
//			//
//			ArrayList<String> keyNameList = dialog_.getKeyFieldList();
//			for (int i = 0; i < keyNameList.size(); i++) {
//				if (keyNameList.get(i).equals(fieldID_)) {
//					isKey = true;
//					break;
//				}
//			}
//		}
//		//
//		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID_, fieldID_);
//		if (workElement == null) {
//			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + res.getString("FunctionError11"));
//		}
//		dataType = workElement.getAttribute("Type");
//		dataTypeOptions = workElement.getAttribute("TypeOptions");
//		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
//		//dataSize = Integer.parseInt(workElement.getAttribute("Size"));
//		//if (dataSize > 50) {
//		//	dataSize = 50;
//		//}
//		if (workElement.getAttribute("Nullable").equals("F")) {
//			isNullable = false;
//		}
//		if (dataTypeOptionList.contains("VIRTUAL")) {
//			isVirtualField = true;
//		}
//		//
//		value_ = this.getNullValue();
//		//
//		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
//		if (!tableElement.getAttribute("RangeKey").equals("")) {
//			StringTokenizer workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
//			if (workTokenizer.nextToken().equals(fieldID_)) {
//				isRangeKeyFieldValid = true;
//			}
//			if (workTokenizer.nextToken().equals(fieldID_)) {
//				isRangeKeyFieldExpire = true;
//			}
//		}
//		//
//		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), (XFScriptableField)this);
//	}
//
//	public String getDataSourceName(){
//		return dataSourceName;
//	}
//
//	public String getFieldID(){
//		return fieldID_;
//	}
//
//	public String getFieldIDInScript(){
//		return tableAlias_ + "_" + fieldID_;
//	}
//
//	public String getTableAlias(){
//		return tableAlias_;
//	}
//
//	public String getTableID(){
//		return tableID_;
//	}
//
//	public boolean isNullable(){
//		return isNullable;
//	}
//
//	public boolean isVirtualField(){
//		return isVirtualField;
//	}
//
//	public boolean isRangeKeyFieldValid(){
//		return isRangeKeyFieldValid;
//	}
//
//	public boolean isRangeKeyFieldExpire(){
//		return isRangeKeyFieldExpire;
//	}
//
//	public org.w3c.dom.Element getTableElement(){
//		return tableElement;
//	}
//
//	public String getBasicType(){
//		return XFUtility.getBasicTypeOf(dataType);
//	}
//
//	public boolean isFieldOnPrimaryTable(){
//		return isFieldOnPrimaryTable;
//	}
//
//	public boolean isKey(){
//		return isKey;
//	}
//
//	public Object getTableOperationValue(){
//		Object returnValue = null;
//		//
//		String basicType = this.getBasicType();
//		if (basicType.equals("INTEGER")) {
//			returnValue = this.getValue();
//		}
//		if (basicType.equals("FLOAT")) {
//			returnValue = this.getValue();
//		}
//		if (basicType.equals("STRING")) {
//			returnValue = "'" + (String)this.getValue() + "'";
//		}
//		if (basicType.equals("DATE")) {
//			String strDate = (String)this.getValue();
//			if (strDate == null ) {
//				returnValue = "NULL";
//			} else {
//				returnValue = "'" + strDate + "'";
//			}
//		}
//		if (basicType.equals("DATETIME")) {
//			String timeDate = (String)this.getValue();
//			if (timeDate == null || timeDate.equals("")) {
//				returnValue = "NULL";
//			} else {
//				if (timeDate.equals("CURRENT_TIMESTAMP")) {
//					returnValue = timeDate;
//				} else {
//					timeDate = timeDate.replace("/", "-");
//					returnValue = "'" + timeDate + "'";
//				}
//			}
//		}
//		return returnValue;
//	}
//
//	public void setValueOfResultSet(ResultSet result){
//		try {
//			if (this.isVirtualField) {
//				if (this.isRangeKeyFieldExpire()) {
//					value_ = XFUtility.calculateExpireValue(this.getTableElement(), result, dialog_.getSession());
//				}
//			} else {
//				String basicType = this.getBasicType();
//				//
//				if (basicType.equals("INTEGER")) {
//					value_ = result.getInt(this.getFieldID());
//				}
//				//
//				if (basicType.equals("FLOAT")) {
//					value_ = result.getFloat(this.getFieldID());
//				}
//				//
//				if (basicType.equals("STRING")) {
//					//value_ = result.getString(this.getFieldID());
//					String strWrk = result.getString(this.getFieldID());
//					if (strWrk == null) {
//						value_ = "";
//					} else {
//						value_ = strWrk.trim();
//					}
//				}
//				//
//				if (basicType.equals("TIME")) {
//					value_ = result.getTime(this.getFieldID());
//				}
//				//
//				if (basicType.equals("DATETIME")) {
//					value_ = result.getTimestamp(this.getFieldID());
//				}
//				//
//				if (basicType.equals("DATE")) {
//					value_ = result.getDate(this.getFieldID());
//				}
//			}
//			if (value_ == null) {
//				value_ = this.getNullValue();
//			}
//		} catch (SQLException e) {
//			e.printStackTrace(dialog_.getExceptionStream());
//			dialog_.setErrorAndCloseFunction();
//		}
//	}
//
//	public void setValue(Object object){
//		value_ = XFUtility.getValueAccordingToBasicType(this.getBasicType(), object);
//	}
//
//	public Object getNullValue(){
//		return XFUtility.getNullValueOfBasicType(this.getBasicType());
//	}
//
//	public boolean isNull(){
//		boolean isNull = false;
//		String basicType = this.getBasicType();
//		//
//		if (basicType.equals("INTEGER")) {
//			if (value_ == null || value_.equals(0)) {
//				isNull = true;
//			}
//		}
//		//
//		if (basicType.equals("FLOAT")) {
//			if (value_ == null || value_.equals(0.0)) {
//				isNull = true;
//			}
//		}
//		//
//		if (basicType.equals("STRING") || basicType.equals("DATETIME") || basicType.equals("TIME")) {
//			if (value_ == null || value_.equals("")) {
//				isNull = true;
//			}
//		}
//		//
//		if (basicType.equals("DATE")) {
//			if (value_ == null || value_.equals("")) {
//				isNull = true;
//			}
//		}
//		//
//		return isNull;
//	}
//
//	public Object getValue(){
//		return value_;
//	}
//
//	public Object getOldValue() {
//		return value_;
//	}
//	
//	public boolean isValueChanged() {
//		return !this.getValue().equals(this.getOldValue());
//	}
//
//	public void setOldValue(Object object){
//	}
//
//	public boolean isEditable() {
//		return false;
//	}
//
//	public void setEditable(boolean isEditable) {
//	}
//
//	public void setError(String message) {
//	}
//
//	public String getError() {
//		return "";
//	}
//
//	public void setColor(String color) {
//	}
//
//	public String getColor() {
//		return "";
//	}
//}
//
//class XF010_PrimaryTable extends Object {
//	private static final long serialVersionUID = 1L;
//	private org.w3c.dom.Element tableElement = null;
//	private org.w3c.dom.Element functionElement_ = null;
//	private String tableID = "";
//	private String activeWhere = "";
//	private String fixedWhere = "";
//	private ArrayList<String> keyFieldList = new ArrayList<String>();
//	private ArrayList<String> orderByFieldList = new ArrayList<String>();
//	private ArrayList<String> uniqueKeyList = new ArrayList<String>();
//	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
//	private XF010 dialog_;
//	private StringTokenizer workTokenizer;
//
//	public XF010_PrimaryTable(org.w3c.dom.Element functionElement, XF010 dialog){
//		super();
//		//
//		functionElement_ = functionElement;
//		dialog_ = dialog;
//		//
//		tableID = functionElement_.getAttribute("PrimaryTable");
//		tableElement = dialog_.getSession().getTableElement(tableID);
//		activeWhere = tableElement.getAttribute("ActiveWhere");
//		fixedWhere = functionElement_.getAttribute("FixedWhere");
//		//
//		String workString;
//		org.w3c.dom.Element workElement;
//		//
//		if (functionElement_.getAttribute("KeyFields").equals("")) {
//			NodeList nodeList = tableElement.getElementsByTagName("Key");
//			for (int i = 0; i < nodeList.getLength(); i++) {
//				workElement = (org.w3c.dom.Element)nodeList.item(i);
//				if (workElement.getAttribute("Type").equals("PK")) {
//					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
//					while (workTokenizer.hasMoreTokens()) {
//						workString = workTokenizer.nextToken();
//						keyFieldList.add(workString);
//					}
//				}
//			}
//		} else {
//			workTokenizer = new StringTokenizer(functionElement_.getAttribute("KeyFields"), ";" );
//			while (workTokenizer.hasMoreTokens()) {
//				keyFieldList.add(workTokenizer.nextToken());
//			}
//		}
//		//
//		NodeList nodeList = tableElement.getElementsByTagName("Key");
//		for (int i = 0; i < nodeList.getLength(); i++) {
//			workElement = (org.w3c.dom.Element)nodeList.item(i);
//			if (workElement.getAttribute("Type").equals("SK")) {
//				uniqueKeyList.add(workElement.getAttribute("Fields"));
//			}
//		}
//		//
//		workTokenizer = new StringTokenizer(functionElement_.getAttribute("OrderBy"), ";" );
//		while (workTokenizer.hasMoreTokens()) {
//			orderByFieldList.add(workTokenizer.nextToken());
//		}
//		//
//		org.w3c.dom.Element element;
//		NodeList workList = tableElement.getElementsByTagName("Script");
//		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
//		for (int i = 0; i < sortList.size(); i++) {
//	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
//	        scriptList.add(new XFScript(tableID, element));
//		}
//	}
//	
//	public String getSQLToSelect(){
//		StringBuffer buf = new StringBuffer();
//		//
//		buf.append("select ");
//		//
//		boolean firstField = true;
//		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
//			if (dialog_.getFieldList().get(i).isFieldOnPrimaryTable()
//			&& !dialog_.getFieldList().get(i).isVirtualField()) {
//				if (!firstField) {
//					buf.append(",");
//				}
//				buf.append(dialog_.getFieldList().get(i).getFieldID());
//				firstField = false;
//			}
//		}
//		//
//		buf.append(" from ");
//		buf.append(tableID);
//		//
//		if (orderByFieldList.size() > 0) {
//			buf.append(" order by ");
//			for (int i = 0; i < orderByFieldList.size(); i++) {
//				if (i > 0) {
//					buf.append(",");
//				}
//				buf.append(orderByFieldList.get(i));
//			}
//		}
//		//
//		buf.append(" where ") ;
//		//
//		int orderOfFieldInKey = 0;
//		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
//			if (dialog_.getFieldList().get(i).isKey()) {
//				if (orderOfFieldInKey > 0) {
//					buf.append(" and ") ;
//				}
//				buf.append(dialog_.getFieldList().get(i).getFieldID()) ;
//				buf.append("=") ;
//				if (XFUtility.isLiteralRequiredBasicType(dialog_.getFieldList().get(i).getBasicType())) {
//					buf.append("'") ;
//					buf.append(dialog_.getFieldList().get(i).getValue()) ;
//					buf.append("'") ;
//				} else {
//					buf.append(dialog_.getFieldList().get(i).getValue()) ;
//				}
//				orderOfFieldInKey++;
//			}
//		}
//		//
//		if (!activeWhere.equals("")) {
//			buf.append(" and (");
//			buf.append(activeWhere);
//			buf.append(") ");
//		}
//		//
//		if (!fixedWhere.equals("")) {
//			buf.append(" and (");
//			buf.append(fixedWhere);
//			buf.append(") ");
//		}
//		//
//		return buf.toString();
//	}
//	
//	public String getTableID(){
//		return tableID;
//	}
//	
//	public org.w3c.dom.Element getTableElement(){
//		return tableElement;
//	}
//	
//	public ArrayList<String> getKeyFieldList(){
//		return keyFieldList;
//	}
//	
//	public ArrayList<XFScript> getScriptList(){
//		return scriptList;
//	}
//	
//	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
//		boolean isValid = false;
//		XF010_ReferTable referTable;
//		org.w3c.dom.Element workElement;
//		//
//		if (this.getTableID().equals(tableID) && this.getTableID().equals(tableAlias)) {
//			NodeList nodeList = tableElement.getElementsByTagName("Field");
//			for (int i = 0; i < nodeList.getLength(); i++) {
//				workElement = (org.w3c.dom.Element)nodeList.item(i);
//				if (workElement.getAttribute("ID").equals(fieldID)) {
//					isValid = true;
//					break;
//				}
//			}
//		} else {
//			for (int i = 0; i < dialog_.getReferTableList().size(); i++) {
//				referTable = dialog_.getReferTableList().get(i);
//				if (referTable.getTableID().equals(tableID) && referTable.getTableAlias().equals(tableAlias)) {
//					for (int j = 0; j < referTable.getFieldIDList().size(); j++) {
//						if (referTable.getFieldIDList().get(j).equals(fieldID)) {
//							isValid = true;
//							break;
//						}
//					}
//				}
//				if (isValid) {
//					break;
//				}
//			}
//		}
//		//
//		return isValid;
//	}
//
//	public int runScript(String event1, String event2) throws ScriptException {
//		int countOfErrors = 0;
//		XFScript script;
//		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
//		//
//		for (int i = 0; i < scriptList.size(); i++) {
//			script = scriptList.get(i);
//			if (script.isToBeRunAtEvent(event1, event2)) {
//				validScriptList.add(script);
//			}
//		}
//		//
//		if (validScriptList.size() > 0) {
//			for (int i = 0; i < validScriptList.size(); i++) {
//				dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
//			}
//		}
//		//
//		return countOfErrors;
//	}
//}
//
//class XF010_ReferTable extends Object {
//	private static final long serialVersionUID = 1L;
//	private org.w3c.dom.Element referElement_ = null;
//	private org.w3c.dom.Element tableElement = null;
//	private XF010 dialog_ = null;
//	private String tableID = "";
//	private String tableAlias = "";
//	private String activeWhere = "";
//	private ArrayList<String> fieldIDList = new ArrayList<String>();
//	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
//	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
//	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
//	private boolean isToBeExecuted = false;
//	private int rangeKeyType = 0;
//	private String rangeKeyFieldValid = "";
//	private String rangeKeyFieldExpire = "";
//	private String rangeKeyFieldSearch = "";
//	private boolean rangeValidated;
//
//	public XF010_ReferTable(org.w3c.dom.Element referElement, XF010 dialog){
//		super();
//		//
//		referElement_ = referElement;
//		dialog_ = dialog;
//		//
//		tableID = referElement_.getAttribute("ToTable");
//		tableElement = dialog_.getSession().getTableElement(tableID);
//		//
//		StringTokenizer workTokenizer;
//		String wrkStr = tableElement.getAttribute("RangeKey");
//		if (!wrkStr.equals("")) {
//			workTokenizer = new StringTokenizer(wrkStr, ";" );
//			rangeKeyFieldValid =workTokenizer.nextToken();
//			rangeKeyFieldExpire =workTokenizer.nextToken();
//			org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
//			if (XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
//				rangeKeyType = 1;
//			} else {
//				rangeKeyType = 2;
//			}
//		}
//		//
//		activeWhere = tableElement.getAttribute("ActiveWhere");
//		//
//		tableAlias = referElement_.getAttribute("TableAlias");
//		if (tableAlias.equals("")) {
//			tableAlias = tableID;
//		}
//		//
//		workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
//		while (workTokenizer.hasMoreTokens()) {
//			fieldIDList.add(workTokenizer.nextToken());
//		}
//		//
//		if (referElement_.getAttribute("ToKeyFields").equals("")) {
//			org.w3c.dom.Element workElement = dialog_.getSession().getTablePKElement(tableID);
//			workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
//			while (workTokenizer.hasMoreTokens()) {
//				toKeyFieldIDList.add(workTokenizer.nextToken());
//			}
//		} else {
//			workTokenizer = new StringTokenizer(referElement_.getAttribute("ToKeyFields"), ";" );
//			while (workTokenizer.hasMoreTokens()) {
//				toKeyFieldIDList.add(workTokenizer.nextToken());
//			}
//		}
//		//
//		workTokenizer = new StringTokenizer(referElement_.getAttribute("WithKeyFields"), ";" );
//		while (workTokenizer.hasMoreTokens()) {
//			withKeyFieldIDList.add(workTokenizer.nextToken());
//		}
//		//
//		workTokenizer = new StringTokenizer(referElement_.getAttribute("OrderBy"), ";" );
//		while (workTokenizer.hasMoreTokens()) {
//			orderByFieldIDList.add(workTokenizer.nextToken());
//		}
//	}
//
//	public String getSelectSQL(boolean isToGetRecordsForComboBox){
//		org.w3c.dom.Element workElement;
//		int count;
//		StringBuffer buf = new StringBuffer();
//		//
//		buf.append("select ");
//		//
//		count = 0;
//		for (int i = 0; i < fieldIDList.size(); i++) {
//			workElement = dialog_.getSession().getFieldElement(tableID, fieldIDList.get(i));
//			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
//				if (count > 0) {
//					buf.append(",");
//				}
//				count++;
//				buf.append(fieldIDList.get(i));
//			}
//		}
//		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
//			if (count > 0) {
//				buf.append(",");
//			}
//			count++;
//			buf.append(toKeyFieldIDList.get(i));
//		}
//		if (!rangeKeyFieldValid.equals("")) {
//			if (count > 0) {
//				buf.append(",");
//			}
//			buf.append(rangeKeyFieldValid);
//			//
//			workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
//			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
//				buf.append(",");
//				buf.append(rangeKeyFieldExpire);
//			}
//		}
//		//
//		buf.append(" from ");
//		buf.append(tableID);
//		//
//		StringTokenizer workTokenizer;
//		String keyFieldID, keyFieldTableID;
//		count = 0;
//		boolean isToBeWithValue;
//		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
//			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
//				rangeKeyFieldSearch = withKeyFieldIDList.get(i);
//			} else {
//				if (isToGetRecordsForComboBox) {
//					//
//					// If the field is PK of the primary key or one on other join tables, value of the field is put into WHERE criteria to select records //
//					for (int j = 0; j < dialog_.getFieldList().size(); j++) {
//						if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getDataSourceName())) {
//							isToBeWithValue = false;
//							//
//							workTokenizer = new StringTokenizer(withKeyFieldIDList.get(i), "." );
//							keyFieldTableID = workTokenizer.nextToken();
//							keyFieldID = workTokenizer.nextToken();
//							if (keyFieldTableID.equals(dialog_.getPrimaryTableID())) {
//								for (int k = 0; k < dialog_.getKeyFieldList().size(); k++) {
//									if (keyFieldID.equals(dialog_.getKeyFieldList().get(k))) {
//										isToBeWithValue = true;
//									}
//								}
//							} else {
//								if (!keyFieldTableID.equals(this.tableAlias)) {
//									isToBeWithValue = true;
//								}
//							}
//							//
//							if (isToBeWithValue) {
//								if (count == 0) {
//									buf.append(" where ");
//								} else {
//									buf.append(" and ");
//								}
//								buf.append(toKeyFieldIDList.get(i));
//								buf.append("=");
//								buf.append(dialog_.getFieldList().get(j).getTableOperationValue());
//								count++;
//								break;
//							}
//						}
//					}
//				} else {
//					if (count == 0) {
//						buf.append(" where ");
//					} else {
//						buf.append(" and ");
//					}
//					buf.append(toKeyFieldIDList.get(i));
//					buf.append("=");
//					for (int j = 0; j < dialog_.getFieldList().size(); j++) {
//						if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getDataSourceName())) {
//							buf.append(dialog_.getFieldList().get(j).getTableOperationValue());
//							break;
//						}
//					}
//					count++;
//				}
//			}
//		}
//		//
//		if (!activeWhere.equals("")) {
//			if (count == 0) {
//				buf.append(" where ");
//			} else {
//				buf.append(" and ");
//			}
//			buf.append(activeWhere);
//		}
//		//
//		if (this.rangeKeyType != 0) {
//			buf.append(" order by ");
//			buf.append(rangeKeyFieldValid);
//			buf.append(" DESC ");
//		} else {
//			if (orderByFieldIDList.size() > 0) {
//				int pos0,pos1;
//				buf.append(" order by ");
//				for (int i = 0; i < orderByFieldIDList.size(); i++) {
//					if (i > 0) {
//						buf.append(",");
//					}
//					pos0 = orderByFieldIDList.get(i).indexOf(".");
//					pos1 = orderByFieldIDList.get(i).indexOf("(A)");
//					if (pos1 >= 0) {
//						buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
//					} else {
//						pos1 = orderByFieldIDList.get(i).indexOf("(D)");
//						if (pos1 >= 0) {
//							buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
//							buf.append(" DESC ");
//						} else {
//							buf.append(orderByFieldIDList.get(i).substring(pos0+1, orderByFieldIDList.get(i).length()));
//						}
//					}
//				}
//			}
//		}
//		//
//		rangeValidated = false;
//		//
//		return buf.toString();
//	}
//
//	public String getTableID(){
//		return tableID;
//	}
//
//	public String getTableAlias(){
//		return tableAlias;
//	}
//
//	public boolean isKeyNullable() {
//		boolean isKeyNullable = false;
//		//
//		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
//			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
//				if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getTableAlias() + "." + dialog_.getFieldList().get(j).getFieldID())) {
//					if (dialog_.getFieldList().get(j).isNullable()) {
//						isKeyNullable = true;
//						break;
//					}
//				}
//			}
//		}
//		//
//		return isKeyNullable;
//	}
//
//	public boolean isKeyNull() {
//		boolean isKeyNull = false;
//		//
//		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
//			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
//				if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getTableAlias() + "." + dialog_.getFieldList().get(j).getFieldID())) {
//					if (dialog_.getFieldList().get(j).isNull()) {
//						isKeyNull = true;
//						break;
//					}
//				}
//			}
//		}
//		//
//		return isKeyNull;
//	}
//
//	public ArrayList<String> getKeyFieldIDList(){
//		return toKeyFieldIDList;
//	}
//
//	public ArrayList<String> getFieldIDList(){
//		return fieldIDList;
//	}
//
//	public ArrayList<String> getWithKeyFieldIDList(){
//		return withKeyFieldIDList;
//	}
//
//	public void setKeyFieldValues(XFHashMap keyValues){
//		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
//			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
//				if (dialog_.getFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))) {
//					dialog_.getFieldList().get(j).setValue(keyValues.getValue(withKeyFieldIDList.get(i)));
//					break;
//				}
//			}
//		}
//	}
//	
//	public void setToBeExecuted(boolean executed){
//		isToBeExecuted = executed;
//	}
//	
//	public boolean isToBeExecuted(){
//		return isToBeExecuted;
//	}
//	
//	public boolean isRecordToBeSelected(ResultSet result){
//		boolean returnValue = false;
//		//
//		if (rangeKeyType == 0) {
//			returnValue = true;
//		}
//		//
//		if (rangeKeyType == 1) {
//			try {
//				if (!rangeValidated) { 
//					// Note that result set is ordered by rangeKeyFieldValue DESC //
//					StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
//					String workTableAlias = workTokenizer.nextToken();
//					String workFieldID = workTokenizer.nextToken();
//					Object valueKey = dialog_.getFieldObjectByID("", workTableAlias, workFieldID).getValue();
//					Object valueFrom = result.getObject(rangeKeyFieldValid);
//					int comp1 = valueKey.toString().compareTo(valueFrom.toString());
//					if (comp1 >= 0) {
//						returnValue = true;
//						rangeValidated = true;
//					}
//				}
//			} catch (SQLException e) {
//				e.printStackTrace(dialog_.getExceptionStream());
//				dialog_.setErrorAndCloseFunction();
//			}
//		}
//		//
//		if (rangeKeyType == 2) {
//			try {
//				StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
//				String workTableAlias = workTokenizer.nextToken();
//				String workFieldID = workTokenizer.nextToken();
//				Object valueKey = dialog_.getFieldObjectByID("", workTableAlias, workFieldID).getValue();
//				Object valueFrom = result.getObject(rangeKeyFieldValid);
//				Object valueThru = result.getObject(rangeKeyFieldExpire);
//				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
//				int comp2 = valueKey.toString().compareTo(valueThru.toString());
//				if (comp1 >= 0 && comp2 < 0) {
//					returnValue = true;
//				}
//			} catch (SQLException e) {
//				e.printStackTrace(dialog_.getExceptionStream());
//				dialog_.setErrorAndCloseFunction();
//			}
//		}
//		//
//		return returnValue;
//	}
//}
