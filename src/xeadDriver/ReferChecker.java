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

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.w3c.dom.*;

////////////////////////////////////////////////////////////////////
// ReferChecker is the class to check possible inconsistency when //
// the target record is processed(updated/inserted/deleted).      //
// Target Table  : The table which record is to be processed      //
// Subject Table : The table which has joins to the target table  //
////////////////////////////////////////////////////////////////////
public class ReferChecker extends Object {
	private static final long serialVersionUID = 1L;
	private Session session_;
	private org.w3c.dom.Element targetTableElement_;
	private String targetTableID_;
	private HashMap<String, Object> columnValueMap_;
	private HashMap<String, Object> columnOldValueMap_;
	private ArrayList<XFTableOperator> operatorList = new ArrayList<XFTableOperator>();
	private ArrayList<ReferChecker_SubjectTable> subjectTableList_ = new ArrayList<ReferChecker_SubjectTable>();
	private ArrayList<String> primaryKeyFieldIDList_ = new ArrayList<String>();
	private int rangeKeyType_;
	private String rangeKeyFieldValid_ = "";
	private String rangeKeyFieldExpire_ = "";
	private XFScriptable function_;
	private String scriptNameRunning_ = "";

	public ReferChecker(Session session, String targetTableID, XFScriptable function) {
		super();
		//
		session_ = session;
		targetTableID_ = targetTableID;
		targetTableElement_ = session_.getTableElement(targetTableID);
		function_ = function;
		StringTokenizer workTokenizer;
		//
		String wrkStr = targetTableElement_.getAttribute("RangeKey");
		if (wrkStr.equals("")) {
			rangeKeyType_ = 0; //0:Non-Range-refer//
		} else {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			rangeKeyFieldValid_ =workTokenizer.nextToken();
			rangeKeyFieldExpire_ =workTokenizer.nextToken();
			org.w3c.dom.Element workElement = session_.getFieldElement(targetTableID_, rangeKeyFieldExpire_);
			if (workElement.getAttribute("TypeOptions").contains("VIRTUAL")) {
				rangeKeyType_ = 1; //1:Range-refer with virtual expire field//
			} else {
				rangeKeyType_ = 2; //2:Range-refer with physical expire field//
			}
		}
		//
		NodeList keyList = targetTableElement_.getElementsByTagName("Key");
		org.w3c.dom.Element keyElement;
		for (int i = 0; i < keyList.getLength(); i++) {
			keyElement = (org.w3c.dom.Element)keyList.item(i);
			if (keyElement.getAttribute("Type").equals("PK")) {
				workTokenizer = new StringTokenizer(keyElement.getAttribute("Fields"), ";" );
				while (workTokenizer.hasMoreTokens()) {
					primaryKeyFieldIDList_.add(workTokenizer.nextToken());
				}
			}
		}
		//
		NodeList tableList, referList;
		org.w3c.dom.Element tableElement, referElement;
		tableList = session.getTableNodeList();
		int countOfSubjectTables = 0;
		int countWork = 0;
		int progressRate = 0;
		for (int i = 0; i < tableList.getLength(); i++) {
			tableElement = (org.w3c.dom.Element)tableList.item(i);
			referList = tableElement.getElementsByTagName("Refer");
			for (int j = 0; j < referList.getLength(); j++) {
				referElement = (org.w3c.dom.Element)referList.item(j);
				if (referElement.getAttribute("ToTable").equals(targetTableID_)
						&& !referElement.getAttribute("Optional").equals("T")
						&& XFUtility.isStaticRefer(tableElement.getAttribute("ID"), referElement)) {
					countOfSubjectTables++;
				}
			}
		}
		//tableList = session.getDomDocument().getElementsByTagName("Table");
		for (int i = 0; i < tableList.getLength(); i++) {
			tableElement = (org.w3c.dom.Element)tableList.item(i);
			referList = tableElement.getElementsByTagName("Refer");
			for (int j = 0; j < referList.getLength(); j++) {
				referElement = (org.w3c.dom.Element)referList.item(j);
				if (referElement.getAttribute("ToTable").equals(targetTableID_)
						&& !referElement.getAttribute("Optional").equals("T")
						&& XFUtility.isStaticRefer(tableElement.getAttribute("ID"), referElement)) {
					subjectTableList_.add(new ReferChecker_SubjectTable(tableElement, referElement, this));
					countWork++;
					progressRate = Math.round(countWork * 100 / countOfSubjectTables);
					session.getApplication().setTextOnSplash(XFUtility.RESOURCE.getString("SplashMessage3") + " " + targetTableID_ + " (" + progressRate + "%)");
				}
			}
		}
	}
	
	public String getTargetTableID() {
		return targetTableID_;
	}
	
	public ArrayList<String> getPrimaryKeyFieldIDList() {
		return primaryKeyFieldIDList_;
	}
	
	public Session getSession() {
		return session_;
	}
	
	public void setFunction(XFScriptable function) {
		function_ = function;
	}
	
	public XFScriptable getFunction() {
		return function_;
	}
	
	public int getRangeKeyType() {
		return rangeKeyType_;
	}
	
	public HashMap<String, Object> getColumnValueMap() {
		return columnValueMap_;
	}
	
	public HashMap<String, Object> getColumnOldValueMap() {
		return columnOldValueMap_;
	}
	
	public String getRangeKeyFieldValid() {
		return rangeKeyFieldValid_;
	}
	
	public String getRangeKeyFieldExpire() {
		return rangeKeyFieldExpire_;
	}
	
	public ArrayList<String> getOperationErrors(String operation, HashMap<String, Object> columnValueMap,  HashMap<String, Object> columnOldValueMap) {
		return getOperationErrors(operation, columnValueMap, columnOldValueMap, 0, "");
	}

	public ArrayList<String> getOperationErrors(String operation, HashMap<String, Object> columnValueMap,  HashMap<String, Object> columnOldValueMap, String checkSkippingTableID) {
		return getOperationErrors(operation, columnValueMap, columnOldValueMap, 0, checkSkippingTableID);
	}

	public ArrayList<String> getOperationErrors(String operation, HashMap<String, Object> columnValueMap,  HashMap<String, Object> columnOldValueMap, int rowNumber) {
		return getOperationErrors(operation, columnValueMap, columnOldValueMap, rowNumber, "");
	}
	
	public ArrayList<String> getOperationErrors(String operation, HashMap<String, Object> columnValueMap,  HashMap<String, Object> columnOldValueMap, int rowNumber, String checkSkippingTableID) {
		ArrayList<String> msgList  = new ArrayList<String>();
		columnValueMap_ = columnValueMap; //mapping key is Field ID//
		columnOldValueMap_ = columnOldValueMap; //mapping key is Field ID//
		StringBuffer bf = new StringBuffer();
		String msgHeader;
		operatorList.clear();
		//
		//function_.startProgress(XFUtility.RESOURCE.getString("ChrossCheck"), subjectTableList_.size());
		session_.startProgress(XFUtility.RESOURCE.getString("ChrossCheck"), subjectTableList_.size());
		//
		if (rowNumber > 0) {
			bf.append(XFUtility.RESOURCE.getString("ReferCheckerMessage0"));
			bf.append(XFUtility.RESOURCE.getString("ReferCheckerMessage1"));
			bf.append(rowNumber);
			bf.append(XFUtility.RESOURCE.getString("ReferCheckerMessage2"));
		} else {
			bf.append(XFUtility.RESOURCE.getString("ReferCheckerMessage0"));
		}
		msgHeader = bf.toString();
		//
		try {
			for (int i = 0; i < subjectTableList_.size(); i++) {
				if (!subjectTableList_.get(i).getID().equals(checkSkippingTableID)) {
					if (operation.toUpperCase().equals("INSERT")) {
						if (rangeKeyType_ == 1 || rangeKeyType_ == 2) {
							if (subjectTableList_.get(i).hasErrorToInsert()) {
								msgList.add(msgHeader + XFUtility.RESOURCE.getString("ReferCheckerMessage3") + subjectTableList_.get(i).getName() + XFUtility.RESOURCE.getString("ReferCheckerMessage4"));
							}
						}
					}
					if (operation.toUpperCase().equals("UPDATE")) {
						if (subjectTableList_.get(i).hasErrorToUpdate()) {
							msgList.add(msgHeader + XFUtility.RESOURCE.getString("ReferCheckerMessage5") + subjectTableList_.get(i).getName() + XFUtility.RESOURCE.getString("ReferCheckerMessage6"));
						}
					}
					if (operation.toUpperCase().equals("DELETE")) {
						if (subjectTableList_.get(i).hasErrorToDelete()) {
							msgList.add(msgHeader + XFUtility.RESOURCE.getString("ReferCheckerMessage7") + subjectTableList_.get(i).getName() + XFUtility.RESOURCE.getString("ReferCheckerMessage8"));
						}
					}
				}
				//function_.incrementProgress();
				session_.incrementProgress();
			}
		} catch(ScriptException e) {
			function_.cancelWithScriptException(e, scriptNameRunning_);
		} catch (Exception e) {
			function_.cancelWithException(e);
		}
		//
		return msgList;
	}

	public XFTableOperator createTableOperator(String sqlText, boolean isUseCash) {
		XFTableOperator operator = null;
		//
		if (isUseCash) {
			for (int i = 0; i < operatorList.size(); i++) {
				if (operatorList.get(i).getSqlText().equals(sqlText)) {
					operator = operatorList.get(i);
					operator.resetCursor();
					break;
				}
			}
			if (operator == null ) {
				operator = new XFTableOperator(session_, function_.getProcessLog(), sqlText);
				operatorList.add(operator);
			}
		} else {
			operator = new XFTableOperator(session_, function_.getProcessLog(), sqlText);
		}
		//
		return operator;
	}
	
	public void setScriptNameRunning(String name) {
		scriptNameRunning_ = name;
	}
}

class ReferChecker_SubjectTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element subjectTableElement_;
	private org.w3c.dom.Element referElement_;
	private String subjectTableID;
	private String subjectTableActiveWhere = "";
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldTableAliasList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<ReferChecker_Field> fieldList = new ArrayList<ReferChecker_Field>();
	private ReferChecker targetTableChecker_;
	private ArrayList<ReferChecker_ReferTable> referTableList = new ArrayList<ReferChecker_ReferTable>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private ArrayList<String> referFieldIDList = new ArrayList<String>();
	private NodeList referNodeList;
	private ScriptEngine scriptEngine_;
	private Bindings scriptBindings_ = null;
	//private String scriptNameRunning_ = "";

	public ReferChecker_SubjectTable(org.w3c.dom.Element subjectTableElement, org.w3c.dom.Element referElement, ReferChecker targetTableChecker) {
		super();

		subjectTableElement_ = subjectTableElement; // table-definition referring to the target table //
		referElement_ = referElement; // refer-definition to the target table //
		targetTableChecker_ = targetTableChecker;

		subjectTableID = subjectTableElement_.getAttribute("ID");
		subjectTableActiveWhere = subjectTableElement.getAttribute("ActiveWhere");

		scriptEngine_ = targetTableChecker_.getSession().getScriptEngineManager().getEngineByName("js");
		scriptBindings_ = scriptEngine_.createBindings();
		scriptBindings_.put("instance", targetTableChecker_.getFunction());

		org.w3c.dom.Element workElement;
		StringTokenizer workTokenizer1, workTokenizer2;
		String wrkStr, wrkFieldID, wrkTableAlias, wrkTableID;

		NodeList nodeList = subjectTableElement_.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("PK")) {
				workTokenizer1 = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
				while (workTokenizer1.hasMoreTokens()) {
					wrkStr = workTokenizer1.nextToken();
					keyFieldIDList.add(wrkStr);
				}
				break;
			}
		}
		//
		wrkStr = referElement_.getAttribute("ToKeyFields");
		if (wrkStr.equals("")) {
			for (int j = 0; j < targetTableChecker_.getPrimaryKeyFieldIDList().size(); j++) {
				wrkStr = targetTableChecker_.getPrimaryKeyFieldIDList().get(j);
				toKeyFieldIDList.add(wrkStr);
			}
		} else {
			workTokenizer1 = new StringTokenizer(wrkStr, ";" );
			while (workTokenizer1.hasMoreTokens()) {
				wrkStr = workTokenizer1.nextToken();
				toKeyFieldIDList.add(wrkStr);
			}
		}
		//
		workTokenizer1 = new StringTokenizer(referElement_.getAttribute("WithKeyFields"), ";" );
		while (workTokenizer1.hasMoreTokens()) {
			wrkStr = workTokenizer1.nextToken();
			workTokenizer2 = new StringTokenizer(wrkStr, "." );
			withKeyFieldTableAliasList.add(workTokenizer2.nextToken());
			withKeyFieldIDList.add(workTokenizer2.nextToken());
		}
		workTokenizer1 = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer1.hasMoreTokens()) {
			wrkStr = workTokenizer1.nextToken();
			referFieldIDList.add(wrkStr);
		}
		//
		NodeList fieldNodeList = subjectTableElement_.getElementsByTagName("Field");
		for (int i = 0; i < fieldNodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)fieldNodeList.item(i);
			fieldList.add(new ReferChecker_Field(subjectTableID, "", workElement.getAttribute("ID"), this));
		}
		//
		referNodeList = subjectTableElement_.getElementsByTagName("Refer");
		SortableDomElementListModel sortableList = XFUtility.getSortedListModel(referNodeList, "Order");
		for (int i = 0; i < sortableList.getSize(); i++) {
			workElement = (org.w3c.dom.Element)sortableList.getElementAt(i);
			referTableList.add(new ReferChecker_ReferTable(workElement, this));
			//
			wrkTableID = workElement.getAttribute("ToTable");
			wrkTableAlias = workElement.getAttribute("TableAlias");
			if (wrkTableAlias.equals("")) {
				wrkTableAlias = wrkTableID;
			}
			workTokenizer1 = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
			while (workTokenizer1.hasMoreTokens()) {
				wrkFieldID = workTokenizer1.nextToken();
				fieldList.add(new ReferChecker_Field(wrkTableID, wrkTableAlias, wrkFieldID, this));
			}
		}
		//
		NodeList workList = subjectTableElement_.getElementsByTagName("Script");
		sortableList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortableList.size(); i++) {
			workElement = (org.w3c.dom.Element)sortableList.getElementAt(i);
        	scriptList.add(new XFScript(subjectTableID, workElement, targetTableChecker_.getSession().getTableNodeList()));
		}
		//
		// Analyze fields in scripts and add them as HIDDEN columns if necessary //
		for (int i = 0; i < scriptList.size(); i++) {
			if	(scriptList.get(i).isToBeRunAtEvent("BR", "") || scriptList.get(i).isToBeRunAtEvent("BU", "")) {
				for (int j = 0; j < scriptList.get(i).getFieldList().size(); j++) {
					workTokenizer1 = new StringTokenizer(scriptList.get(i).getFieldList().get(j), "." );
					wrkTableAlias = workTokenizer1.nextToken();
					wrkTableID = getTableIDOfTableAlias(wrkTableAlias);
					wrkFieldID = workTokenizer1.nextToken();
					if (!containsField(wrkTableID, wrkTableAlias, wrkFieldID)) {
						workElement = targetTableChecker_.getSession().getFieldElement(wrkTableID, wrkFieldID);
						if (workElement == null) {
							String msg = XFUtility.RESOURCE.getString("FunctionError1")
								+ subjectTableID + XFUtility.RESOURCE.getString("FunctionError2")
								+ scriptList.get(i).getName() + XFUtility.RESOURCE.getString("FunctionError3")
								+ wrkTableID + "_" + wrkFieldID + XFUtility.RESOURCE.getString("FunctionError4");
							JOptionPane.showMessageDialog(null, msg);
						} else {
							fieldList.add(new ReferChecker_Field(wrkTableID, wrkTableID, wrkFieldID, this));
						}
					}
				}
			}
		}
	}
	

	public boolean containsField(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < fieldList.size(); i++) {
			if (tableID.equals("")) {
				if (fieldList.get(i).getTableAlias().equals(tableAlias)
						&& fieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (fieldList.get(i).getTableID().equals(tableID)
						&& fieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (fieldList.get(i).getTableID().equals(tableID)
						&& fieldList.get(i).getTableAlias().equals(tableAlias)
						&& fieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}
	
	public String getID() {
		return subjectTableID;
	}
	
	public String getName() {
		return subjectTableElement_.getAttribute("Name");
	}
	
	public NodeList getReferNodeList() {
		return referNodeList;
	}
	
	public ReferChecker getReferChecker() {
		return targetTableChecker_;
	}
	
	public Bindings getScriptBindings() {
		return scriptBindings_;
	}
	
	public boolean hasErrorToInsert() throws ScriptException, Exception {
		boolean isFound, hasError = false;
		XFTableOperator operatorSubjectTable, operatorReferTable;
		int countOfErrors;
		//
		String sql = this.getSelectSQL();
		if (!sql.equals("")) {
			operatorSubjectTable = this.getReferChecker().createTableOperator(sql, true);
			while (operatorSubjectTable.next()) {
				//
				countOfErrors = 0;
				for (int i = 0; i < fieldList.size(); i++) {
					fieldList.get(i).initialize();
				}
				//
				countOfErrors = countOfErrors + this.runScript("BR", "");
				//
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).getTableID().equals(subjectTableID)) {
						if (operatorSubjectTable.hasValueOf(fieldList.get(i).getTableID(), fieldList.get(i).getFieldID())) {
							fieldList.get(i).setValueOfResultSet(operatorSubjectTable);
						}
					}
				}
				//
				countOfErrors = countOfErrors + this.runScript("BU", "BR()");
				//
				for (int i = 0; i < referTableList.size(); i++) {
					if (referTableList.get(i).isToBeChecked()) {
						//
						countOfErrors = countOfErrors + this.runScript("BU", "BR(" + referTableList.get(i).getTableAlias() + ")");
						//
						isFound = false;
						sql = referTableList.get(i).getSelectSQL();
						operatorReferTable = this.getReferChecker().createTableOperator(sql, true);
						while (operatorReferTable.next()) {
							if (referTableList.get(i).getTableAlias().equals(targetTableChecker_.getTargetTableID())) {
								if (referTableList.get(i).newRecordIsToBeSelectedHere(operatorReferTable)) {
									for (int j = 0; j < fieldList.size(); j++) {
										if (fieldList.get(j).getTableID().equals(referTableList.get(i).getTableID())) {
											fieldList.get(j).setValueOfColumnValueMap();
										}
									}
									isFound = true;
									break;
								}
							}
							if (referTableList.get(i).isRecordToBeSelected(operatorReferTable, false)) {
								for (int j = 0; j < fieldList.size(); j++) {
									if (fieldList.get(j).getTableID().equals(referTableList.get(i).getTableID())) {
										fieldList.get(j).setValueOfResultSet(operatorReferTable);
									}
								}
								isFound = true;
								break;
							}
						}
						if (!isFound) {
							countOfErrors++;
							break;
						}
						countOfErrors = countOfErrors + this.runScript("BU", "AR(" + referTableList.get(i).getTableAlias() + ")");
					}
				}
				countOfErrors = countOfErrors + this.runScript("BU", "AR()");
				//
				if (countOfErrors > 0) {
					hasError = true;
					setRecordInformationInSessionLog();
				}
			}
		}
		return hasError;
	}
	
	public boolean hasErrorToUpdate() throws ScriptException, Exception {
		boolean isFound, hasError = false;
		XFTableOperator operatorSubjectTable, operatorReferTable;
		int countOfErrors;
		//
		if (hasReferFieldWhichValuesAreAltered()) {
			String sql = this.getSelectSQL();
			if (!sql.equals("")) {
				operatorSubjectTable = this.getReferChecker().createTableOperator(sql, true);
				while (operatorSubjectTable.next()) {
					//
					countOfErrors = 0;
					for (int i = 0; i < fieldList.size(); i++) {
						fieldList.get(i).initialize();
					}
					//
					countOfErrors = countOfErrors + this.runScript("BR", "");
					//
					for (int i = 0; i < fieldList.size(); i++) {
						if (fieldList.get(i).getTableID().equals(subjectTableID)) {
							if (operatorSubjectTable.hasValueOf(fieldList.get(i).getTableID(), fieldList.get(i).getFieldID())) {
								fieldList.get(i).setValueOfResultSet(operatorSubjectTable);
							}
						}
					}
					//
					countOfErrors = countOfErrors + this.runScript("BU", "BR()");
					//
					for (int i = 0; i < referTableList.size(); i++) {
						if (referTableList.get(i).isToBeChecked()) {
							//
							countOfErrors = countOfErrors + this.runScript("BU", "BR(" + referTableList.get(i).getTableAlias() + ")");
							//
							isFound = false;
							sql = referTableList.get(i).getSelectSQL();
							operatorReferTable = this.getReferChecker().createTableOperator(sql, true);
							while (operatorReferTable.next()) {
								if (referTableList.get(i).getTableAlias().equals(targetTableChecker_.getTargetTableID())) {
									if (referTableList.get(i).isRecordToBeSelected(operatorReferTable, true)) {
										for (int j = 0; j < fieldList.size(); j++) {
											if (fieldList.get(j).getTableID().equals(referTableList.get(i).getTableID())) {
												fieldList.get(j).setValueOfColumnValueMap();
											}
										}
										isFound = true;
										break;
									}
								} else {
									if (referTableList.get(i).isRecordToBeSelected(operatorReferTable, false)) {
										for (int j = 0; j < fieldList.size(); j++) {
											if (fieldList.get(j).getTableID().equals(referTableList.get(i).getTableID())) {
												fieldList.get(j).setValueOfResultSet(operatorReferTable);
											}
										}
										isFound = true;
										break;
									}
								}
							}
							if (!isFound) {
								countOfErrors++;
								break;
							}
							countOfErrors = countOfErrors + this.runScript("BU", "AR(" + referTableList.get(i).getTableAlias() + ")");
						}
					}
					countOfErrors = countOfErrors + this.runScript("BU", "AR()");
					//
					if (countOfErrors > 0) {
						hasError = true;
						setRecordInformationInSessionLog();
					}
				}
			}
		}
		//
		return hasError;
	}
	
	public boolean hasErrorToDelete() throws ScriptException, Exception {
		boolean isFound, hasError = false;
		XFTableOperator operatorSubjectTable, operatorReferTable;
		int countOfErrors;
		//
		String sql = this.getSelectSQL();
		if (!sql.equals("")) {
			operatorSubjectTable = this.getReferChecker().createTableOperator(sql, true);
			while (operatorSubjectTable.next()) {
				//
				countOfErrors = 0;
				for (int i = 0; i < fieldList.size(); i++) {
					fieldList.get(i).initialize();
				}
				//
				countOfErrors = countOfErrors + this.runScript("BR", "");
				//
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).getTableID().equals(subjectTableID)) {
						if (operatorSubjectTable.hasValueOf(fieldList.get(i).getTableID(), fieldList.get(i).getFieldID())) {
							fieldList.get(i).setValueOfResultSet(operatorSubjectTable);
						}
					}
				}
				//
				countOfErrors = countOfErrors + this.runScript("BU", "BR()");
				//
				for (int i = 0; i < referTableList.size(); i++) {
					if (referTableList.get(i).isToBeChecked()) {
						//
						countOfErrors = countOfErrors + this.runScript("BU", "BR(" + referTableList.get(i).getTableAlias() + ")");
						//
						isFound = false;
						sql = referTableList.get(i).getSelectSQL();
						//operatorReferTable = new XFTableOperator(targetTableChecker_.getSession(), targetTableChecker_.getFunction().getProcessLog(), sql, false);
						operatorReferTable = this.getReferChecker().createTableOperator(sql, true);
						while (operatorReferTable.next()) {
							if (referTableList.get(i).isRecordToBeSelected(operatorReferTable, false)) {
								for (int j = 0; j < fieldList.size(); j++) {
									if (fieldList.get(j).getTableID().equals(referTableList.get(i).getTableID())) {
										fieldList.get(j).setValueOfResultSet(operatorReferTable);
									}
								}
								isFound = true;
								break;
							}
						}
						if (referTableList.get(i).getTableID().equals(targetTableChecker_.getTargetTableID())
								&& isFound && isRecordReferringToTargetRecord()) {
							countOfErrors++;
							break;
						}
						countOfErrors = countOfErrors + this.runScript("BU", "AR(" + referTableList.get(i).getTableAlias() + ")");
					}
				}
				countOfErrors = countOfErrors + this.runScript("BU", "AR()"); /* Script to be run AFTER READ subject record AR() */
				//
				if (countOfErrors > 0) {
					hasError = true;
					setRecordInformationInSessionLog();
				}
			}
		}
		//
		return hasError;
	}

	private boolean hasReferFieldWhichValuesAreAltered() {
		boolean hasAltered = false;
		Object valueNew, valueOld;
		if (targetTableChecker_.getColumnValueMap() != null
				&& targetTableChecker_.getColumnOldValueMap() != null) {
			for (int i = 0; i < referFieldIDList.size(); i++) {
				valueNew = targetTableChecker_.getColumnValueMap().get(referFieldIDList.get(i));
				valueOld = targetTableChecker_.getColumnOldValueMap().get(referFieldIDList.get(i));
				if (valueNew == null && valueOld != null
						|| valueNew != null && valueOld == null) {
					hasAltered = true;
					break;
				} else {
					if (valueNew != null && valueOld != null) {
						if (!valueNew.equals(valueOld)) {
							hasAltered = true;
							break;
						}
					}
				}
			}
		}
		return hasAltered;
	}
	
	private void setRecordInformationInSessionLog() {
		StringBuffer keyValueStringBuffer = new StringBuffer();
		keyValueStringBuffer.append(XFUtility.RESOURCE.getString("ReferCheckerMessage9"));
		keyValueStringBuffer.append(subjectTableID);
		keyValueStringBuffer.append(" Key:");
		int wrkInt = 0;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getTableID().equals(subjectTableID)) {
				if (keyFieldIDList.contains(fieldList.get(i).getFieldID())) {
					if (wrkInt > 0) {
						keyValueStringBuffer.append(",");
					}
					keyValueStringBuffer.append(fieldList.get(i).getValue());
					wrkInt++;
				}
			}
		}
		keyValueStringBuffer.append(XFUtility.RESOURCE.getString("ReferCheckerMessage10"));
		keyValueStringBuffer.append("\n");
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isError()) {
				keyValueStringBuffer.append(fieldList.get(i).getFieldID() + ";" + fieldList.get(i).getError());
				keyValueStringBuffer.append("\n");
			}
		}
		targetTableChecker_.getFunction().getProcessLog().append(keyValueStringBuffer.toString());
	}
	
	public String getTableIDOfTableAlias(String alias) {
		String returnValue = alias;
		org.w3c.dom.Element element;
		for (int i = 0; i < referNodeList.getLength(); i++) {
			element = (org.w3c.dom.Element)referNodeList.item(i);
			if (element.getAttribute("TableAlias").equals(alias) || (element.getAttribute("TableAlias").equals("") && element.getAttribute("ToTable").equals(alias))) {
				returnValue = element.getAttribute("ToTable");
				break;
			}
		}
		return returnValue;
	}

	public String getSelectSQL() {
		ReferChecker_Field field;
		StringBuffer buf = new StringBuffer();
		int count;
		Object value;
		//
		buf.append("select * from ");
		buf.append(subjectTableID);
		//
		count = -1;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			field = null;
			for (int j = 0; j < fieldList.size(); j++) {
				if (fieldList.get(j).getDataSourceName().equals(subjectTableID + "." + withKeyFieldIDList.get(i))) {
					field = fieldList.get(j);
					break;
				}
			}
			//
			if (field != null && !field.isVirtualField()) {
				count++;
				if (count == 0) {
					buf.append(" where ") ;
				}
				if (count > 0) {
					buf.append(" and ") ;
				}
				buf.append(withKeyFieldIDList.get(i)) ;
				if (toKeyFieldIDList.get(i).equals(targetTableChecker_.getRangeKeyFieldValid())) {
					buf.append(">=") ;
				} else {
					buf.append("=") ;
				}
				//
				value = targetTableChecker_.getColumnValueMap().get(toKeyFieldIDList.get(i));
				if (XFUtility.isLiteralRequiredBasicType(field.getBasicType())) {
					buf.append("'");
					buf.append(value.toString());
					buf.append("'");
				} else {
					buf.append(value.toString());
				}
			}
		}
		//
		if (!subjectTableActiveWhere.equals("")) {
			buf.append(" and ");
			buf.append(subjectTableActiveWhere);
		}
		//
		if (count == -1) {
			return "";
		} else {
			return buf.toString();
		}
	}
	
	public Object getValueWithDataSourceName(String dataSourceName) {
		Object value = null;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getDataSourceName().equals(dataSourceName)) {
				value = fieldList.get(i).getValue();
			}
		}
		return value;
	}

	public boolean isRecordReferringToTargetRecord() {
		boolean result = true;
		int comp1,comp2;
		Object subjectValueKey, targetValueKey, targetValueKeyFrom, targetValueKeyThru;
		//
		if (targetTableChecker_.getRangeKeyType() == 0) {
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				subjectValueKey = getValueWithDataSourceName(withKeyFieldTableAliasList.get(i) + "." + withKeyFieldIDList.get(i));
				targetValueKey = targetTableChecker_.getColumnValueMap().get(toKeyFieldIDList.get(i));
//				if (!subjectValueKey.equals(targetValueKey)) {
//					result = false;
//					break;
//				}
				comp1 = subjectValueKey.toString().compareTo(targetValueKey.toString());
				if (comp1 != 0) {
					result = false;
					break;
				}
			}
		}
		//
		if (targetTableChecker_.getRangeKeyType() == 1) {
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				subjectValueKey = getValueWithDataSourceName(withKeyFieldTableAliasList.get(i) + "." + withKeyFieldIDList.get(i));
				if (toKeyFieldIDList.get(i).equals(targetTableChecker_.getRangeKeyFieldValid())) {
					targetValueKeyFrom = targetTableChecker_.getColumnValueMap().get(targetTableChecker_.getRangeKeyFieldValid());
					comp1 = subjectValueKey.toString().compareTo(targetValueKeyFrom.toString());
					if (comp1 < 0) {
						result = false;
						break;
					}
				} else {
					targetValueKey = targetTableChecker_.getColumnValueMap().get(toKeyFieldIDList.get(i));
//					if (!subjectValueKey.equals(targetValueKey)) {
//						result = false;
//						break;
//					}
					comp1 = subjectValueKey.toString().compareTo(targetValueKey.toString());
					if (comp1 != 0) {
						result = false;
						break;
					}
				}
			}
		}
		//
		if (targetTableChecker_.getRangeKeyType() == 2) {
			for (int i = 0; i < toKeyFieldIDList.size(); i++) {
				subjectValueKey = getValueWithDataSourceName(withKeyFieldTableAliasList.get(i) + "." + withKeyFieldIDList.get(i));
				if (toKeyFieldIDList.get(i).equals(targetTableChecker_.getRangeKeyFieldValid())) {
					targetValueKeyFrom = targetTableChecker_.getColumnValueMap().get(targetTableChecker_.getRangeKeyFieldValid());
					targetValueKeyThru = targetTableChecker_.getColumnValueMap().get(targetTableChecker_.getRangeKeyFieldExpire());
					if (targetValueKeyThru == null) {
						comp1 = subjectValueKey.toString().compareTo(targetValueKeyFrom.toString());
						if (comp1 < 0) {
							result = false;
							break;
						}
					} else {
						comp1 = subjectValueKey.toString().compareTo(targetValueKeyFrom.toString());
						comp2 = subjectValueKey.toString().compareTo(targetValueKeyThru.toString());
						if (comp1 < 0 || comp2 >= 0) {
							result = false;
							break;
						}
					}
				} else {
					targetValueKey = targetTableChecker_.getColumnValueMap().get(toKeyFieldIDList.get(i));
//					if (!subjectValueKey.equals(targetValueKey)) {
//						result = false;
//						break;
//					}
					comp1 = subjectValueKey.toString().compareTo(targetValueKey.toString());
					if (comp1 != 0) {
						result = false;
						break;
					}
				}
			}
		}
		return result;
	}

	public int runScript(String event1, String event2) throws ScriptException, Exception  {
		int countOfErrors = 0;
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		StringBuffer bf;
		//
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		//
		if (validScriptList.size() > 0) {
			for (int i = 0; i < validScriptList.size(); i++) {
				//scriptNameRunning_ = validScriptList.get(i).getName();
				targetTableChecker_.setScriptNameRunning(validScriptList.get(i).getName());
				bf = new StringBuffer();
				bf.append(validScriptList.get(i).getScriptText());
				bf.append(targetTableChecker_.getSession().getScriptFunctions());
				scriptEngine_.eval(bf.toString(), scriptBindings_);
			}
			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).isError()) {
					countOfErrors++;
				}
			}
		}
		//
		return countOfErrors;
	}
}

class ReferChecker_ReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private ReferChecker_SubjectTable subjectTable_;
	private ReferChecker deleteChecker;
	private org.w3c.dom.Element referElement_;
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldTableAliasList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<Boolean> withKeyFieldIsLiteralRequiredList = new ArrayList<Boolean>();
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private String rangeKeyFieldSearch = "";
	private boolean rangeValidated;
	private boolean notSelectedYet;
	private String tableID;
	private String tableAlias;

	public ReferChecker_ReferTable(org.w3c.dom.Element referElement, ReferChecker_SubjectTable subjectTable) {
		super();
		//
		referElement_ = referElement;
		subjectTable_ = subjectTable;
		deleteChecker = subjectTable_.getReferChecker();
		//
		tableID = referElement_.getAttribute("ToTable");
		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}
		//
		StringTokenizer workTokenizer1, workTokenizer2;
		String wrkStr;
		org.w3c.dom.Element workElement;
		int index;
		//
		org.w3c.dom.Element tableElement = deleteChecker.getSession().getTableElement(tableID);
		wrkStr = tableElement.getAttribute("RangeKey");
		if (!wrkStr.equals("")) {
			workTokenizer1 = new StringTokenizer(wrkStr, ";" );
			rangeKeyFieldValid =workTokenizer1.nextToken();
			rangeKeyFieldExpire =workTokenizer1.nextToken();
			workElement = deleteChecker.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (workElement.getAttribute("TypeOptions").contains("VIRTUAL")) {
				rangeKeyType = 1;
			} else {
				rangeKeyType = 2;
			}
		}
		//
		int indexOfRangeKeyFieldValid = -1;
		wrkStr = referElement_.getAttribute("ToKeyFields");
		if (wrkStr.equals("")) {
			workElement = deleteChecker.getSession().getTablePKElement(referElement_.getAttribute("ToTable"));
			wrkStr = workElement.getAttribute("Fields");
		}
		workTokenizer1 = new StringTokenizer(wrkStr, ";" );
		index = 0;
		while (workTokenizer1.hasMoreTokens()) {
			wrkStr = workTokenizer1.nextToken();
			toKeyFieldIDList.add(wrkStr);
			if (wrkStr.equals(rangeKeyFieldValid)) {
				indexOfRangeKeyFieldValid = index;
			}
			index++;
		}
		//
		index = 0;
		workTokenizer1 = new StringTokenizer(referElement.getAttribute("WithKeyFields"), ";" );
		while (workTokenizer1.hasMoreTokens()) {
			wrkStr = workTokenizer1.nextToken();
			workTokenizer2 = new StringTokenizer(wrkStr, "." );
			withKeyFieldTableAliasList.add(workTokenizer2.nextToken());
			withKeyFieldIDList.add(workTokenizer2.nextToken());
			if (indexOfRangeKeyFieldValid == index) {
				rangeKeyFieldSearch = wrkStr;
			}
			index++;
		}
		//
		for (int j = 0; j < withKeyFieldIDList.size(); j++) {
			wrkStr = withKeyFieldTableAliasList.get(j);
			wrkStr = XFUtility.getTableIDOfTableAlias(wrkStr, subjectTable.getReferNodeList(), null);
			workElement = deleteChecker.getSession().getFieldElement(wrkStr, withKeyFieldIDList.get(j));
			wrkStr = workElement.getAttribute("Type");
			if (wrkStr.equals("CHAR")
					|| wrkStr.equals("VARCHAR")
					|| wrkStr.equals("LONG VARCHAR")
					|| wrkStr.equals("DATE")
					|| wrkStr.equals("TIME")
					|| wrkStr.equals("TIMESTAMP")) {
				withKeyFieldIsLiteralRequiredList.add(true);
			} else {
				withKeyFieldIsLiteralRequiredList.add(false);
			}
		}
	}
	
	public String getTableID() {
		return tableID;
	}
	
	public String getTableAlias() {
		return tableAlias;
	}
	
	public String getSelectSQL() {
		StringBuffer buf = new StringBuffer();
		int count;
		//
		buf.append("select * from ");
		buf.append(tableID);
		//
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (!toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				count++;
				if (count > 0) {
					buf.append(" and ") ;
				}
				buf.append(toKeyFieldIDList.get(i)) ;
				buf.append("=") ;
				if (withKeyFieldIsLiteralRequiredList.get(i)) {
					buf.append("'");
					buf.append(subjectTable_.getValueWithDataSourceName(withKeyFieldTableAliasList.get(i) + "." + withKeyFieldIDList.get(i)));
					buf.append("'");
				} else {
					buf.append(subjectTable_.getValueWithDataSourceName(withKeyFieldTableAliasList.get(i) + "." + withKeyFieldIDList.get(i)));
				}
			}
		}
		//
		String activeWhere = deleteChecker.getSession().getTableElement(referElement_.getAttribute("ToTable")).getAttribute("ActiveWhere");
		if (!activeWhere.equals("")) {
			buf.append(" and ");
			buf.append(activeWhere);
		}
		//
		if (this.rangeKeyType != 0) {
			buf.append(" order by ");
			buf.append(rangeKeyFieldValid);
			buf.append(" DESC ");
		}
		//
		rangeValidated = false;
		notSelectedYet = true;
		//
		return buf.toString();
	}
	
	public boolean isToBeChecked() {
		boolean isToBeChecked = true;
		String wrkStr;
		int wrkInt;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (withKeyFieldIsLiteralRequiredList.get(i)) {
				wrkStr = subjectTable_.getValueWithDataSourceName(withKeyFieldTableAliasList.get(i) + "." + withKeyFieldIDList.get(i)).toString();
				if (wrkStr.trim().equals("")) {
					isToBeChecked = false;
					break;
				}
			} else {
				wrkStr = subjectTable_.getValueWithDataSourceName(withKeyFieldTableAliasList.get(i) + "." + withKeyFieldIDList.get(i)).toString();
				if (wrkStr.trim().equals("")) {
					isToBeChecked = false;
					break;
				} else {
					wrkInt = Integer.parseInt(wrkStr);
					if (wrkInt == 0) {
						isToBeChecked = false;
						break;
					}
				}
			}
		}
		return isToBeChecked;
	}
	
	public boolean isRecordToBeSelected(XFTableOperator operator, boolean isOverridenByNewValue) throws Exception {
		boolean returnValue = false;
		Object subjectKey = null;
		Object targetKeyFrom = null;
		Object targetKeyThru = null;
		//
		if (rangeKeyType == 0) {
			returnValue = true;
		}
		//
		if (rangeKeyType == 1) {
			////////////////////////////////////////////////////////////////
			// Note that result set is ordered by rangeKeyFieldValue DESC //
			////////////////////////////////////////////////////////////////
			if (!rangeValidated) { 
				subjectKey = subjectTable_.getValueWithDataSourceName(rangeKeyFieldSearch);
				if (isOverridenByNewValue) {
					targetKeyFrom = subjectTable_.getReferChecker().getColumnValueMap().get(rangeKeyFieldValid);
				} else {
					targetKeyFrom = operator.getValueOf(rangeKeyFieldValid);
				}
				int comp1 = subjectKey.toString().compareTo(targetKeyFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
					rangeValidated = true;
				}
			}
		}
		//
		if (rangeKeyType == 2) {
			subjectKey = subjectTable_.getValueWithDataSourceName(rangeKeyFieldSearch);
			if (isOverridenByNewValue) {
				targetKeyFrom = subjectTable_.getReferChecker().getColumnValueMap().get(rangeKeyFieldValid);
				targetKeyThru = subjectTable_.getReferChecker().getColumnValueMap().get(rangeKeyFieldExpire);
			} else {
				targetKeyFrom = operator.getValueOf(rangeKeyFieldValid);
				targetKeyThru = operator.getValueOf(rangeKeyFieldExpire);
			}
			if (targetKeyThru == null) {
				int comp1 = subjectKey.toString().compareTo(targetKeyFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
				}
			} else {
				int comp1 = subjectKey.toString().compareTo(targetKeyFrom.toString());
				int comp2 = subjectKey.toString().compareTo(targetKeyThru.toString());
				if (comp1 >= 0 && comp2 < 0) {
					returnValue = true;
				}
			}
		}
		//
		return returnValue;
	}
	
	public boolean newRecordIsToBeSelectedHere(XFTableOperator operator) throws Exception {
		boolean returnValue = false;
		Object subjectRecordKey = null;
		Object existingTargetRecordKey = null;
		Object newTargetRecordKey = null;
		//
		if (rangeKeyType == 0) {
			returnValue = false;
		}
		//
		if (rangeKeyType == 1) {
			////////////////////////////////////////////////////////////////
			// Note that result set is ordered by rangeKeyFieldValue DESC //
			////////////////////////////////////////////////////////////////
			if (!rangeValidated && notSelectedYet) { 
				subjectRecordKey = subjectTable_.getValueWithDataSourceName(rangeKeyFieldSearch);
				existingTargetRecordKey = operator.getValueOf(rangeKeyFieldValid);
				newTargetRecordKey = subjectTable_.getReferChecker().getColumnValueMap().get(rangeKeyFieldValid);
				int comp1 = newTargetRecordKey.toString().compareTo(existingTargetRecordKey.toString());
				if (comp1 < 0) {
					comp1 = subjectRecordKey.toString().compareTo(newTargetRecordKey.toString());
					if (comp1 >= 0) {
						returnValue = true;
						rangeValidated = true;
					}
				}
				notSelectedYet = false;
			}
		}
		//
		if (rangeKeyType == 2) {
			if (notSelectedYet) { 
				existingTargetRecordKey = operator.getValueOf(rangeKeyFieldValid);
				newTargetRecordKey = subjectTable_.getReferChecker().getColumnValueMap().get(rangeKeyFieldValid);
				int comp1 = newTargetRecordKey.toString().compareTo(existingTargetRecordKey.toString());
				if (comp1 < 0) {
					comp1 = subjectRecordKey.toString().compareTo(newTargetRecordKey.toString());
					if (comp1 >= 0) {
						returnValue = true;
					}
				}
				notSelectedYet = false;
			}
		}
		//
		return returnValue;
	}
}

class ReferChecker_Field extends XFColumnScriptable {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private ReferChecker_SubjectTable subjectTable_ = null;
	private String tableAlias = "";
	private String tableID = "";
	private String dataType = "";
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private String fieldID = "";
	private boolean isVirtualField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private boolean isError = false;
	private String errorMessage = "";
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private Object value_ = null;
	private Color foreground = Color.black;

	public ReferChecker_Field(String tableID, String tableAlias, String fieldID, ReferChecker_SubjectTable subjectTable) {
		super();
		//
		String wrkStr;
		this.tableAlias = "";
		this.tableID = tableID;
		if (tableAlias.equals("")) {
			this.tableAlias = tableID;
		} else {
			this.tableAlias = tableAlias;
		}
		this.fieldID = fieldID;
		subjectTable_ = subjectTable;
		//
		org.w3c.dom.Element workElement = subjectTable_.getReferChecker().getSession().getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID + "." + fieldID + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
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
			try {
				String sql = "select * from " + subjectTable_.getReferChecker().getSession().getTableNameOfUserVariants() + " where IDUSERKUBUN = '" + wrkStr + "' order by SQLIST";
				XFTableOperator operator = subjectTable_.getReferChecker().createTableOperator(sql, true);
				while (operator.next()) {
					kubunValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
					kubunTextList.add(operator.getValueOf("TXUSERKUBUN").toString().trim());
				}
			} catch(Exception e) {
			}
		}
		//
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		if (!subjectTable_.getScriptBindings().containsKey(this.getFieldIDInScript())) {
			subjectTable_.getScriptBindings().put(this.getFieldIDInScript(), this);
		}
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

	public String getFieldIDInScript(){
		return tableAlias + "_" + fieldID;
	}

	public String getDataSourceName(){
		return tableAlias + "." + fieldID;
	}

	public String getTableAlias(){
		return tableAlias;
	}

	public Object getInternalValue(){
		return value_;
	}

	public boolean setValueOfResultSet(XFTableOperator operator) {
		Object value = null;
		boolean isFoundInResultSet = false;
		String basicType = this.getBasicType();
		this.setColor("");
		//
		try {
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					value_ = XFUtility.calculateExpireValue(this.getTableElement(), operator, subjectTable_.getReferChecker().getSession(), null);
				}
			} else {
				value = operator.getValueOf(this.getFieldID());
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
		} catch(Exception e) {
		}
		//
		return isFoundInResultSet;
	}

	public void setValueOfColumnValueMap() {
		String basicType = this.getBasicType();
		Object value = subjectTable_.getReferChecker().getColumnValueMap().get(fieldID);
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

	public void initialize() {
		value_ = XFUtility.getNullValueOfBasicType(this.getBasicType());
		isError = false;
		errorMessage = "";
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

	public boolean isError() {
		return isError;
	}

	public void setError(String message) {
		if (!message.equals("")) {
			isError = true;
			if (errorMessage.equals("")) {
				errorMessage = message;
			}
		}
	}

	public void setError(boolean error) {
		isError = error;
	}

	public String getError() {
		return errorMessage;
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