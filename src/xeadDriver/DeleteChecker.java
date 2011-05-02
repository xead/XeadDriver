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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import org.w3c.dom.*;

public class DeleteChecker extends Object {
	private static final long serialVersionUID = 1L;
	private static ResourceBundle res = ResourceBundle.getBundle("xeadDriver.Res");
	private Session session_;
	private org.w3c.dom.Element subjectTableElement_;
	private String subjectTableID;
	private HashMap<String, Object> keyValueMap_;
	private ArrayList<DeleteChecker_BaseTable> baseTableList = new ArrayList<DeleteChecker_BaseTable>();
	private ArrayList<String> primaryKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> secondaryKeyFieldIDList = new ArrayList<String>();
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private ScriptEngine scriptEngine_;
	
	///////////////////////////////////////////////////////////////////////
	// Subject Table : The table which record is to be deleted           //
	// Base Table : The table which has a reference to the subject table //
	///////////////////////////////////////////////////////////////////////
	
	public DeleteChecker(Session session, org.w3c.dom.Element subjectTableElement, HashMap<String, Object> keyValueMap, ScriptEngine scriptEngine) {
		super();
		//
		session_ = session;
		subjectTableElement_ = subjectTableElement;
		keyValueMap_ = keyValueMap;
		scriptEngine_ = scriptEngine;
		//
		subjectTableID = subjectTableElement_.getAttribute("ID");
		//
		StringTokenizer workTokenizer;
		String wrkStr = subjectTableElement.getAttribute("RangeKey");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			rangeKeyFieldValid =workTokenizer.nextToken();
			rangeKeyFieldExpire =workTokenizer.nextToken();
			org.w3c.dom.Element workElement = session_.getFieldElement(subjectTableID, rangeKeyFieldExpire);
			if (workElement.getAttribute("TypeOptions").contains("VIRTUAL")) {
				rangeKeyType = 1;
			} else {
				rangeKeyType = 2;
			}
		}
		//
		NodeList keyList = subjectTableElement_.getElementsByTagName("Key");
		org.w3c.dom.Element keyElement;
		for (int i = 0; i < keyList.getLength(); i++) {
			keyElement = (org.w3c.dom.Element)keyList.item(i);
			if (keyElement.getAttribute("Type").equals("PK")) {
				workTokenizer = new StringTokenizer(keyElement.getAttribute("Fields"), ";" );
				while (workTokenizer.hasMoreTokens()) {
					primaryKeyFieldIDList.add(workTokenizer.nextToken());
				}
			}
			if (keyElement.getAttribute("Type").equals("SK")) {
				workTokenizer = new StringTokenizer(keyElement.getAttribute("Fields"), ";" );
				while (workTokenizer.hasMoreTokens()) {
					secondaryKeyFieldIDList.add(workTokenizer.nextToken());
				}
			}
		}
		//
		keyValueMap_.putAll(getOtherKeyFieldValueMap());
		//
		NodeList tableList, referList;
		org.w3c.dom.Element tableElement, referElement;
		SortableDomElementListModel sortableList;
		DeleteChecker_BaseTable baseTable;
		//
		boolean found;
		tableList = session.getDomDocument().getElementsByTagName("Table");
		for (int i = 0; i < tableList.getLength(); i++) {
			tableElement = (org.w3c.dom.Element)tableList.item(i);
			referList = tableElement.getElementsByTagName("Refer");
			sortableList = XFUtility.getSortedListModel(referList, "Order");
			for (int j = 0; j < sortableList.getSize(); j++) {
				referElement = (org.w3c.dom.Element)sortableList.elementAt(j);
				if (referElement.getAttribute("ToTable").equals(subjectTableID)) {
					baseTable = new DeleteChecker_BaseTable(tableElement, referElement, this);
					found = false;
					for (int k = 0; k < baseTableList.size(); k++) {
						if (baseTableList.get(k).getWithKeyFields().equals(baseTable.getWithKeyFields())) {
							found = true;
							if (!baseTableList.get(k).isStaticRefer() && baseTable.isStaticRefer()) {
								baseTableList.set(k, baseTable);
							}
						}
					}
					if (!found) {
						baseTableList.add(baseTable);
					}
				}
			}
		}
	}
	
	public String getSubjectTableID() {
		return subjectTableID;
	}
	
	public ArrayList<String> getPrimaryKeyFieldIDList() {
		return primaryKeyFieldIDList;
	}
	
	public Session getSession() {
		return session_;
	}
	
	public ScriptEngine getScriptEngine() {
		return scriptEngine_;
	}
	
	public org.w3c.dom.Element getSubjectTableElement() {
		return subjectTableElement_;
	}
	
	public int getRangeKeyType() {
		return rangeKeyType;
	}
	
	public HashMap<String, Object> getKeyValueMap() {
		return keyValueMap_;
	}
	
	public String getRangeKeyFieldValid() {
		return rangeKeyFieldValid;
	}
	
	public String getRangeKeyFieldExpire() {
		return rangeKeyFieldExpire;
	}
	
	public ArrayList<String> getDeleteErrors() {
		ArrayList<String> msgList  = new ArrayList<String>();
		//
		for (int i = 0; i < baseTableList.size(); i++) {
			if (baseTableList.get(i).hasReferringRecord()) {
				msgList.add(res.getString("DeleteCheckerMessage1") + baseTableList.get(i).getName() + res.getString("DeleteCheckerMessage2"));
			}
		}
		//
		return msgList;
	}

	private HashMap<String, Object> getOtherKeyFieldValueMap() {
		HashMap<String, Object> fieldValueMap = new HashMap<String, Object>();
		StringBuffer buf = new StringBuffer();
		org.w3c.dom.Element element;
		String wrkStr;
		int count;
		//
		if (secondaryKeyFieldIDList.size() > 0 || rangeKeyType == 2) {
			//
			buf.append("select ");
			//
			count = -1;
			for (int i = 0; i < secondaryKeyFieldIDList.size(); i++) {
				count++;
				if (count > 0) {
					buf.append(",") ;
				}
				buf.append(secondaryKeyFieldIDList.get(i)) ;
			}
			if (rangeKeyType == 2) {
				count++;
				if (count > 0) {
					buf.append(",") ;
				}
				buf.append(rangeKeyFieldExpire) ;
			}
			//
			buf.append(" from ");
			buf.append(subjectTableID);
			//
			buf.append(" where ") ;
			//
			count = -1;
			for (int i = 0; i < primaryKeyFieldIDList.size(); i++) {
				count++;
				if (count > 0) {
					buf.append(" and ") ;
				}
				buf.append(primaryKeyFieldIDList.get(i));
				buf.append("=");
				element = this.getSession().getFieldElement(subjectTableID, primaryKeyFieldIDList.get(i));
				wrkStr = element.getAttribute("Type");
				if (wrkStr.equals("CHAR")
						|| wrkStr.equals("VARCHAR")
						|| wrkStr.equals("LONG VARCHAR")
						|| wrkStr.equals("DATE")
						|| wrkStr.equals("TIME")
						|| wrkStr.equals("TIMESTAMP")) {
					buf.append("'");
					buf.append(keyValueMap_.get(primaryKeyFieldIDList.get(i)));
					buf.append("'");
				} else {
					buf.append(keyValueMap_.get(primaryKeyFieldIDList.get(i)));
				}
			}
			//
			String activeWhere = subjectTableElement_.getAttribute("ActiveWhere");
			if (!activeWhere.equals("")) {
				buf.append(" and ");
				buf.append(activeWhere);
			}
			//
			try {
				Statement statement = this.getSession().getConnection().createStatement();
				ResultSet resultSet = statement.executeQuery(buf.toString());
				if (resultSet.next()) {
					for (int i = 0; i < secondaryKeyFieldIDList.size(); i++) {
						fieldValueMap.put(secondaryKeyFieldIDList.get(i), resultSet.getObject(secondaryKeyFieldIDList.get(i)));
					}
					if (rangeKeyType == 2) {
						fieldValueMap.put(rangeKeyFieldExpire, resultSet.getObject(rangeKeyFieldExpire));
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//
		if (rangeKeyType == 1) {
			//
			buf.append("select ");
			buf.append(rangeKeyFieldValid) ;
			buf.append(" from ");
			buf.append(subjectTableID);
			//
			buf.append(" where ") ;
			count = -1;
			for (int i = 0; i < primaryKeyFieldIDList.size(); i++) {
				count++;
				if (count > 0) {
					buf.append(" and ") ;
				}
				buf.append(primaryKeyFieldIDList.get(i));
				if (primaryKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
					buf.append(">");
				} else {
					buf.append("=");
				}
				element = this.getSession().getFieldElement(subjectTableID, primaryKeyFieldIDList.get(i));
				wrkStr = element.getAttribute("Type");
				if (wrkStr.equals("CHAR")
						|| wrkStr.equals("VARCHAR")
						|| wrkStr.equals("LONG VARCHAR")
						|| wrkStr.equals("DATE")
						|| wrkStr.equals("TIME")
						|| wrkStr.equals("TIMESTAMP")) {
					buf.append("'");
					buf.append(keyValueMap_.get(primaryKeyFieldIDList.get(i)));
					buf.append("'");
				} else {
					buf.append(keyValueMap_.get(primaryKeyFieldIDList.get(i)));
				}
			}
			String activeWhere = subjectTableElement_.getAttribute("ActiveWhere");
			if (!activeWhere.equals("")) {
				buf.append(" and ");
				buf.append(activeWhere);
			}
			//
			buf.append(" order by ");
			buf.append(rangeKeyFieldValid);
			//
			try {
				Statement statement = this.getSession().getConnection().createStatement();
				ResultSet resultSet = statement.executeQuery(buf.toString());
				if (resultSet.next()) {
					fieldValueMap.put(rangeKeyFieldExpire, resultSet.getObject(rangeKeyFieldValid));
				} else {
					fieldValueMap.put(rangeKeyFieldExpire, null);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//
		return fieldValueMap;
	}
}

class DeleteChecker_BaseTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element baseTableElement_;
	private org.w3c.dom.Element elementOfReferToSubjectTable_;
	private String baseTableID;
	private String baseTableActiveWhere = "";
	private ArrayList<String> fieldTableIDList = new ArrayList<String>();
	private ArrayList<String> fieldTableAliasList = new ArrayList<String>();
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private HashMap<String, Object> dataSourceValueMap = new HashMap<String, Object>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldTableAliasList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<Boolean> withKeyFieldIsLiteralRequiredList = new ArrayList<Boolean>();
	private ArrayList<Object> keyFieldValueList = new ArrayList<Object>();
	private DeleteChecker deleteChecker_;
	private ArrayList<DeleteChecker_ReferTable> referTableList = new ArrayList<DeleteChecker_ReferTable>();
	private ArrayList<DeleteChecker_BaseTableScript> scriptList = new ArrayList<DeleteChecker_BaseTableScript>();
	private NodeList referNodeList;
	private boolean isStaticRefer = true;
	private String withKeyFields = "";
	private String rangeKeyDataSource = "";

	public DeleteChecker_BaseTable(org.w3c.dom.Element baseTableElement, org.w3c.dom.Element elementOfReferToSubjectTable, DeleteChecker deleteChecker) {
		super();
		//
		baseTableElement_ = baseTableElement;
		elementOfReferToSubjectTable_ = elementOfReferToSubjectTable;
		deleteChecker_ = deleteChecker;
		//
		baseTableID = baseTableElement_.getAttribute("ID");
		baseTableActiveWhere = baseTableElement.getAttribute("ActiveWhere");
		//
		org.w3c.dom.Element referElement, fieldElement;
		StringTokenizer workTokenizer1;
		StringTokenizer workTokenizer2;
		String wrkStr, wrkFieldID, wrkTableAlias, wrkTableID;
		int index;
		//
		referNodeList = baseTableElement_.getElementsByTagName("Refer");
		SortableDomElementListModel sortableList = XFUtility.getSortedListModel(referNodeList, "Order");
		for (int i = 0; i < sortableList.getSize(); i++) {
			//
			referElement = (org.w3c.dom.Element)sortableList.getElementAt(i);
			//
			if (referElement.getAttribute("ToTable").equals(elementOfReferToSubjectTable_.getAttribute("ToTable")) &&
					referElement.getAttribute("TableAlias").equals(elementOfReferToSubjectTable_.getAttribute("TableAlias"))) {
				//
				int indexOfRangeKeyFieldValid = -1;
				wrkStr = referElement.getAttribute("ToKeyFields");
				if (wrkStr.equals("")) {
					for (int j = 0; j < deleteChecker.getPrimaryKeyFieldIDList().size(); j++) {
						wrkStr = deleteChecker.getPrimaryKeyFieldIDList().get(j);
						toKeyFieldIDList.add(wrkStr);
						keyFieldValueList.add(deleteChecker.getKeyValueMap().get(wrkStr));
						if (wrkStr.equals(deleteChecker_.getRangeKeyFieldValid())) {
							indexOfRangeKeyFieldValid = j;
						}
					}
				} else {
					workTokenizer1 = new StringTokenizer(wrkStr, ";" );
					index = 0;
					while (workTokenizer1.hasMoreTokens()) {
						wrkStr = workTokenizer1.nextToken();
						toKeyFieldIDList.add(wrkStr);
						keyFieldValueList.add(deleteChecker.getKeyValueMap().get(wrkStr));
						if (wrkStr.equals(deleteChecker_.getRangeKeyFieldValid())) {
							indexOfRangeKeyFieldValid = index;
						}
						index++;
					}
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
						rangeKeyDataSource = wrkStr;
					}
					index++;
				}
				//
				for (int j = 0; j < withKeyFieldIDList.size(); j++) {
					wrkTableID = withKeyFieldTableAliasList.get(j);
					wrkTableID = XFUtility.getTableIDOfTableAlias(wrkTableID, referNodeList, null);
					fieldElement = deleteChecker_.getSession().getFieldElement(wrkTableID, withKeyFieldIDList.get(j));
					wrkStr = fieldElement.getAttribute("Type");
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
				//
				withKeyFields = referElement.getAttribute("WithKeyFields");
				workTokenizer1 = new StringTokenizer(withKeyFields, ";" );
				while (workTokenizer1.hasMoreTokens()) {
					workTokenizer2 = new StringTokenizer(workTokenizer1.nextToken(), "." );
					wrkStr = workTokenizer2.nextToken();
					if (!wrkStr.equals(baseTableID)) {
						isStaticRefer = false;
					}
					fieldTableAliasList.add(wrkStr);
					fieldTableIDList.add(getTableIDOfTableAlias(wrkStr));
					fieldIDList.add(workTokenizer2.nextToken());
				}
				//
				break;
				//
			} else {
				//
				wrkTableID = referElement.getAttribute("ToTable");
				wrkTableAlias = referElement.getAttribute("TableAlias");
				if (wrkTableAlias.equals("")) {
					wrkTableAlias = wrkTableID;
				}
				//
				workTokenizer1 = new StringTokenizer(referElement.getAttribute("Fields"), ";" );
				while (workTokenizer1.hasMoreTokens()) {
					fieldTableAliasList.add(wrkTableAlias);
					fieldTableIDList.add(getTableIDOfTableAlias(wrkTableID));
					wrkFieldID = workTokenizer1.nextToken();
					fieldIDList.add(wrkFieldID);
				}
				//
				workTokenizer1 = new StringTokenizer(referElement.getAttribute("WithKeyFields"), ";" );
				while (workTokenizer1.hasMoreTokens()) {
					workTokenizer2 = new StringTokenizer(workTokenizer1.nextToken(), "." );
					wrkStr = workTokenizer2.nextToken();
					fieldTableAliasList.add(wrkStr);
					fieldTableIDList.add(getTableIDOfTableAlias(wrkStr));
					fieldIDList.add(workTokenizer2.nextToken());
				}
				//
				referTableList.add(new DeleteChecker_ReferTable(referElement, this));
			}
		}
		//
		org.w3c.dom.Element scriptElement;
		DeleteChecker_BaseTableScript script;
		//
		NodeList workList = baseTableElement_.getElementsByTagName("Script");
		sortableList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortableList.size(); i++) {
	        scriptElement = (org.w3c.dom.Element)sortableList.getElementAt(i);
	        if (scriptElement.getAttribute("Event").equals("BR") || 
	          scriptElement.getAttribute("Event").equals("AR") ||
	          scriptElement.getAttribute("Event").contains("BR(") ||
	          scriptElement.getAttribute("Event").contains("AR(") ) {
	        	//
	        	script = new DeleteChecker_BaseTableScript(scriptElement);
	    		for (int j = 0; j < script.getDataSourceList().size(); j++) {
					workTokenizer1 = new StringTokenizer(script.getDataSourceList().get(j), "." );
					wrkStr = workTokenizer1.nextToken();
					fieldTableAliasList.add(wrkStr);
					fieldTableIDList.add(getTableIDOfTableAlias(wrkStr));
					fieldIDList.add(workTokenizer1.nextToken());
				}
	    		//
	        	scriptList.add(script);
	        }
		}
	}
	
	public String getName() {
		return baseTableElement_.getAttribute("Name");
	}

	public boolean isStaticRefer() {
		return isStaticRefer;
	}
	
	public String getWithKeyFields() {
		return withKeyFields;
	}
	
	public DeleteChecker getDeleteChecker() {
		return deleteChecker_;
	}
	
	public boolean hasReferringRecord() {
		boolean result = false;
		//
		try {
			String baseRecordSQL, referRecordSQL;
			ResultSet baseRecordSet, referRecordSet;
			//
			Statement statementBase = deleteChecker_.getSession().getConnection().createStatement();
			Statement statementRefer = deleteChecker_.getSession().getConnection().createStatement();
			//
			this.runScript("BR", ""); /* Script to be run BEFORE READ */
			//
			baseRecordSQL = getSelectSQL();
			baseRecordSet = statementBase.executeQuery(baseRecordSQL);
			while (baseRecordSet.next()) {
				dataSourceValueMap.clear();
				//
				setupDataSourceValueMapWithResultSet(baseTableID, baseRecordSet);
				//
				for (int i = 0; i < referTableList.size(); i++) {
					//
					this.runScript("BR", referTableList.get(i).getTableAlias()); /* Script to be run BEFORE READ */
					//
					referRecordSQL = referTableList.get(i).getSelectSQL(dataSourceValueMap);
					referRecordSet = statementRefer.executeQuery(referRecordSQL);
					while (referRecordSet.next()) {
						if (referTableList.get(i).isTargetRecord(referRecordSet, dataSourceValueMap)) {
							//
							setupDataSourceValueMapWithResultSet(referTableList.get(i).getTableID(), referRecordSet);
							//
							this.runScript("AR", referTableList.get(i).getTableAlias()); /* Script to be run AFTER READ */
							//
							break;
						}
					}
					//
					referRecordSet.close();
				}
				//
				if (isKeyValueEquivalantRecord(dataSourceValueMap)) {
					result = true;
					break;
				}
			}
			//
			baseRecordSet.close();
			//
		} catch (SQLException e1) {
			e1.printStackTrace();
		} catch(ScriptException e2) {
			e2.printStackTrace();
		}
		//
		return result;
	}
	
	public String getTableIDOfTableAlias(String alias) {
		String returnValue = "";
		org.w3c.dom.Element element;
		if (alias.equals(baseTableID)) {
			returnValue = alias;
		} else {
			for (int i = 0; i < referNodeList.getLength(); i++) {
				element = (org.w3c.dom.Element)referNodeList.item(i);
				if (element.getAttribute("TableAlias").equals(alias) || (element.getAttribute("TableAlias").equals("") && element.getAttribute("ToTable").equals(alias))) {
					returnValue = element.getAttribute("ToTable");
					break;
				}
			}
		}
		return returnValue;
	}
	
	public void setupDataSourceValueMapWithResultSet(String tableID, ResultSet resultSet) {
		Object workObject;
		org.w3c.dom.Element element;
		//
		try {
			for (int i = 0; i < fieldIDList.size(); i++) {
				if (tableID.equals(fieldTableIDList.get(i))) {
					element = deleteChecker_.getSession().getFieldElement(fieldTableIDList.get(i), fieldIDList.get(i));
					if (!element.getAttribute("TypeOptions").contains("VIRTUAL")) {
						workObject = resultSet.getObject(fieldIDList.get(i));
						dataSourceValueMap.put(fieldTableAliasList.get(i) + "." + fieldIDList.get(i), workObject);
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public String getSelectSQL() {
		StringBuffer buf = new StringBuffer();
		org.w3c.dom.Element element;
		int count;
		//
		buf.append("select ");
		count = -1;
		for (int i = 0; i < fieldIDList.size(); i++) {
			if (fieldTableAliasList.get(i).equals(baseTableID)) {
				element = deleteChecker_.getSession().getFieldElement(fieldTableIDList.get(i), fieldIDList.get(i));
				if (!element.getAttribute("TypeOptions").contains("VIRTUAL")) {
					count++;
					if (count > 0) {
						buf.append(",") ;
					}
					buf.append(fieldIDList.get(i)) ;
				}
			}
		}
		buf.append(" from ");
		buf.append(baseTableID);
		//
		count = -1;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			if (withKeyFieldTableAliasList.get(i).equals(baseTableID)) {
				count++;
				if (count == 0) {
					buf.append(" where ") ;
				}
				if (count > 0) {
					buf.append(" and ") ;
				}
				buf.append(withKeyFieldIDList.get(i)) ;
				if (toKeyFieldIDList.get(i).equals(deleteChecker_.getRangeKeyFieldValid())) {
					buf.append(">=") ;
				} else {
					buf.append("=") ;
				}
				if (withKeyFieldIsLiteralRequiredList.get(i)) {
					buf.append("'");
					buf.append(keyFieldValueList.get(i));
					buf.append("'");
				} else {
					buf.append(keyFieldValueList.get(i));
				}
			}
		}
		//
		if (!baseTableActiveWhere.equals("")) {
			buf.append(" and ");
			buf.append(baseTableActiveWhere);
		}
		//
		return buf.toString();
	}
	
	public boolean isKeyValueEquivalantRecord(HashMap<String, Object> valueDataSourceMap) {
		boolean result = true;
		int comp1, comp2;
		Object valueKey, valueFrom, valueThru;
		//
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (rangeKeyDataSource.equals(withKeyFieldTableAliasList.get(i) + "." + withKeyFieldIDList.get(i))) {
				//
				valueKey = valueDataSourceMap.get(rangeKeyDataSource);
				valueFrom = deleteChecker_.getKeyValueMap().get(deleteChecker_.getRangeKeyFieldValid());
				valueThru = deleteChecker_.getKeyValueMap().get(deleteChecker_.getRangeKeyFieldExpire());
				//
				comp1 = valueKey.toString().compareTo(valueFrom.toString());
				//
				if (valueThru == null) {
					comp2 = -1;
				} else {
					comp2 = valueKey.toString().compareTo(valueThru.toString());
				}
				//
				if (comp1 < 0 || comp2 >= 0) {
					result = false;
					break;
				}
			} else {
				valueKey = valueDataSourceMap.get(withKeyFieldTableAliasList.get(i) + "." + withKeyFieldIDList.get(i));
				if (valueKey == null || !valueKey.equals(keyFieldValueList.get(i))) {
					result = false;
					break;
				}
			}
		}
		//
		return result;
	}

	public void runScript(String event, String tableAlias) throws ScriptException  {
		String scriptText = "";
		String wrkStr;
		StringTokenizer workTokenizer1;
		DeleteChecker_BaseTableScript script;
		ArrayList<DeleteChecker_BaseTableScript> validScriptList = new ArrayList<DeleteChecker_BaseTableScript>();
		//
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtTheEvent(event, tableAlias)) {
				validScriptList.add(script);
			}
		}
		//
		if (validScriptList.size() > 0) {
			//
			Bindings bindings = deleteChecker_.getScriptEngine().createBindings();
			//
			for (int i = 0; i < validScriptList.size(); i++) {
				script = validScriptList.get(i);
				for (int j = 0; j < script.getDataSourceList().size(); j++) {
					workTokenizer1 = new StringTokenizer(script.getDataSourceList().get(j), "." );
					wrkStr = workTokenizer1.nextToken() + "_" + workTokenizer1.nextToken() + "_Value";
					bindings.put(wrkStr, dataSourceValueMap.get(script.getDataSourceList().get(j)));
				}
			}
			//
			for (int i = 0; i < validScriptList.size(); i++) {
				script = validScriptList.get(i);
				scriptText = script.getScriptText();
				if (!scriptText.equals("")) {
					deleteChecker_.getScriptEngine().eval(scriptText);
				}
			}
			//
			for (int i = 0; i < validScriptList.size(); i++) {
				script = validScriptList.get(i);
				for (int j = 0; j < script.getDataSourceList().size(); j++) {
					workTokenizer1 = new StringTokenizer(script.getDataSourceList().get(j), "." );
					wrkStr = workTokenizer1.nextToken() + "_" + workTokenizer1.nextToken() + "_Value";
					dataSourceValueMap.put(script.getDataSourceList().get(j), bindings.get(wrkStr));
				}
			}
		}
	}
}

class DeleteChecker_BaseTableScript extends Object {
	private static final long serialVersionUID = 1L;
	private ArrayList<String> updateFieldList = new ArrayList<String>();
	private ArrayList<String> dataSourceList = new ArrayList<String>();
	private String scriptText_ = "";
	private String event_ = "";
	private StringTokenizer workTokenizer;
	String[] sectionDigit = {"(", ")", "{", "}", "+", "-", "/", "*", "=", "<", ">", ";", "|", "&", "\n", "\t", " "};
	
	public DeleteChecker_BaseTableScript(org.w3c.dom.Element scriptElement) {
		super();
		event_ = scriptElement.getAttribute("Event");
		scriptText_ = XFUtility.substringLinesWithTokenOfEOL(scriptElement.getAttribute("Text"), "\n");
		//
		int wrkInt;
		int posWrk;
		int pos = 0;
		while (pos < scriptText_.length()) {
			posWrk = scriptText_.indexOf("_Value = ", pos);
			if (posWrk == -1) {
				pos = scriptText_.length();
			}
			if (posWrk != -1) {
				wrkInt = posWrk - 1;
				while (wrkInt > -1) {
					wrkInt--;
					if (wrkInt == -1 || isFirstDigitOfField(scriptText_.substring(wrkInt, wrkInt+1))) {
						workTokenizer = new StringTokenizer(scriptText_.substring(wrkInt + 1, posWrk + 6), "_" );
						updateFieldList.add(workTokenizer.nextToken() + "." + workTokenizer.nextToken());
						wrkInt = -1;
						pos = posWrk + 6;
					}
				}
			}
		}
		//
		pos = 0;
		while (pos < scriptText_.length()) {
			posWrk = scriptText_.indexOf("_Value", pos);
			if (posWrk == -1) {
				pos = scriptText_.length();
			}
			if (posWrk != -1) {
				wrkInt = posWrk - 1;
				while (wrkInt > -1) {
					wrkInt--;
					if (wrkInt == -1 || isFirstDigitOfField(scriptText_.substring(wrkInt, wrkInt+1))) {
						workTokenizer = new StringTokenizer(scriptText_.substring(wrkInt + 1, posWrk + 6), "_" );
						dataSourceList.add(workTokenizer.nextToken() + "." + workTokenizer.nextToken());
						wrkInt = -1;
						pos = posWrk + 6;
					}
				}
			}
		}
	}
	
	private boolean isFirstDigitOfField(String digit) {
		boolean result = false;
		for (int i = 0; i < sectionDigit.length; i++) {
			if (digit.equals(sectionDigit[i])) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	String getEvent() {
		return event_;
	}
	
	String getScriptText() {
		return scriptText_;
	}
	
	boolean isToBeRunAtTheEvent(String event, String tableAlias) {
		boolean result = false;
		//
		workTokenizer = new StringTokenizer(event_, ";" );
		while (workTokenizer.hasMoreTokens()) {
			if (tableAlias.equals("")) {
				if (workTokenizer.nextToken().equals(event)) {
					result = true;
					break;
				}
			} else {
				if (workTokenizer.nextToken().contains(event + "(" + tableAlias + ")")) {
					result = true;
					break;
				}
			}
		}
		//
		return result;
	}
	
	ArrayList<String> getDataSourceList() {
		return dataSourceList;
	}
}

class DeleteChecker_ReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private DeleteChecker_BaseTable baseTable_;
	private DeleteChecker deleteChecker;
	private org.w3c.dom.Element referElement_;
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldTableAliasList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<Boolean> withKeyFieldIsLiteralRequiredList = new ArrayList<Boolean>();
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private boolean rangeValidated;
	private String tableID;
	private String tableAlias;
	private String rangeKeyDataSource;

	public DeleteChecker_ReferTable(org.w3c.dom.Element referElement, DeleteChecker_BaseTable baseTable) {
		super();
		//
		referElement_ = referElement;
		baseTable_ = baseTable;
		deleteChecker = baseTable_.getDeleteChecker();
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
		wrkStr = referElement.getAttribute("ToKeyFields");
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
				rangeKeyDataSource = wrkStr;
			}
			index++;
		}
		//
		for (int j = 0; j < withKeyFieldIDList.size(); j++) {
			workElement = deleteChecker.getSession().getFieldElement(withKeyFieldTableAliasList.get(j), withKeyFieldIDList.get(j));
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
	
	public String getSelectSQL(HashMap<String, Object> dataSourceValueMap) {
		StringBuffer buf = new StringBuffer();
		StringTokenizer workTokenizer1;
		org.w3c.dom.Element element;
		String wrkStr;
		int count;
		//
		buf.append("select ");
		workTokenizer1 = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		count = -1;
		while (workTokenizer1.hasMoreTokens()) {
			wrkStr = workTokenizer1.nextToken();
			element = deleteChecker.getSession().getFieldElement(tableID, wrkStr);
			if (!element.getAttribute("TypeOptions").contains("VIRTUAL")) {
				count++;
				if (count > 0) {
					buf.append(",") ;
				}
				buf.append(wrkStr) ;
			}
		}
		if (!rangeKeyFieldValid.equals("")) {
			count++;
			if (count > 0) {
				buf.append(",") ;
			}
			buf.append(rangeKeyFieldValid) ;
			
		}
		//
		buf.append(" from ");
		buf.append(tableID);
		//
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			count++;
			if (count > 0) {
				buf.append(" and ") ;
			}
			buf.append(toKeyFieldIDList.get(i)) ;
			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				buf.append(">=") ;
			} else {
				buf.append("=") ;
			}
			if (withKeyFieldIsLiteralRequiredList.get(i)) {
				buf.append("'");
				buf.append(dataSourceValueMap.get(withKeyFieldTableAliasList.get(i) + "." + withKeyFieldIDList.get(i)));
				buf.append("'");
			} else {
				buf.append(dataSourceValueMap.get(withKeyFieldTableAliasList.get(i) + "." + withKeyFieldIDList.get(i)));
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
		//
		return buf.toString();
	}
	
	public boolean isTargetRecord(ResultSet resultSet, HashMap<String, Object> dataSourceValueMap) {
		boolean result = true;
		//
		if (rangeKeyType == 0) {
			result = true;
		}
		//
		if (rangeKeyType == 1) {
			try {
				if (!rangeValidated) { 
					//
					// Note that result set is ordered by rangeKeyFieldValue DESC //
					//
					Object valueKey = dataSourceValueMap.get(rangeKeyDataSource);
					Object valueFrom = resultSet.getObject(rangeKeyFieldValid);
					int comp1 = valueKey.toString().compareTo(valueFrom.toString());
					if (comp1 >= 0) {
						result = true;
						rangeValidated = true;
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//
		if (rangeKeyType == 2) {
			try {
				Object valueKey = dataSourceValueMap.get(rangeKeyDataSource);
				Object valueFrom = resultSet.getObject(rangeKeyFieldValid);
				Object valueThru = resultSet.getObject(rangeKeyFieldExpire);
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				int comp2 = valueKey.toString().compareTo(valueThru.toString());
				if (comp1 >= 0 && comp2 < 0) {
					result = true;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		//
		return result;
	}
}
