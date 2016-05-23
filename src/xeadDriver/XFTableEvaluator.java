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

import java.awt.Color;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.w3c.dom.NodeList;

////////////////////////////////////////////////////////////////////
// This is a public class that can be used in Table-Script.       //
// Note that public classes must be defined in its own java file. //
////////////////////////////////////////////////////////////////////
public class XFTableEvaluator {
    private Session session_ = null;
    private XFScriptable instance_ = null;
    private String tableID_ = "";
    private String tableName = "";
    private String moduleID = "";
    private String dbName = "";
    private String orderBy = "";
	private String fixedWhere = "";
	private ArrayList<XFTableEvaluator_ReferTable> referTableList = new ArrayList<XFTableEvaluator_ReferTable>();
	private ArrayList<XFTableEvaluator_Field> fieldList = new ArrayList<XFTableEvaluator_Field>();
	private ArrayList<String> keyFieldList = new ArrayList<String>();
    private ArrayList<String> withKeyList_ = new ArrayList<String>();
	private ArrayList<String> uniqueKeyList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private StringTokenizer workTokenizer;
	private String updateCounterID = "";
	private int updateCounterValue = 0;
	private String detailRowNoID = "";
	private ScriptEngine scriptEngine;
	private Bindings scriptBindings;
	private String scriptNameRunning = "";
	private XFTableOperator tableOperatorToSelect = null;
	private XFTableOperator tableOperator = null;
	private SortableDomElementListModel sortingList;

	public XFTableEvaluator(XFScriptable instance, String tableID) {
		super();
		instance_ = instance;
    	tableID_ = tableID;

		session_ = instance_.getSession();
		org.w3c.dom.Element tableElement = session_.getTableElement(tableID);
		tableName = tableElement.getAttribute("Name");
		if (tableElement.getAttribute("DB").equals("")) {
			dbName = session_.getDatabaseName();
		} else {
			dbName = session_.getSubDBName(tableElement.getAttribute("DB"));
		}
    	moduleID = tableID;
		if (!tableElement.getAttribute("ModuleID").equals("")) {
	    	moduleID = tableElement.getAttribute("ModuleID");
		}

		XFTableEvaluator_Field workField;
		org.w3c.dom.Element workElement;

		String wrkStr1;
		NodeList nodeList = tableElement.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("PK")) {
				workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
				while (workTokenizer.hasMoreTokens()) {
					wrkStr1 = workTokenizer.nextToken();
					if (!wrkStr1.equals("")) {
						keyFieldList.add(wrkStr1);
						workElement = session_.getFieldElement(tableID, wrkStr1);
						if (XFUtility.getBasicTypeOf(workElement.getAttribute("Type")).equals("INTEGER")
								&& tableElement.getAttribute("DetailRowNumberAuto").equals("T")) {
							detailRowNoID = wrkStr1;
						}
					}
				}
			}
			if (workElement.getAttribute("Type").equals("SK")) {
				uniqueKeyList.add(workElement.getAttribute("Fields"));
			}
		}

		NodeList tableFieldList = tableElement.getElementsByTagName("Field");
		sortingList = XFUtility.getSortedListModel(tableFieldList, "Order");
		for (int i = 0; i < sortingList.getSize(); i++) {
			workElement = (org.w3c.dom.Element)sortingList.getElementAt(i);
			workField = new XFTableEvaluator_Field(tableID, "", workElement.getAttribute("ID"), this);
			fieldList.add(workField);
		}

		XFTableEvaluator_ReferTable referTable;
		NodeList referNodeList = tableElement.getElementsByTagName("Refer");
		sortingList = XFUtility.getSortedListModel(referNodeList, "Order");
		for (int i = 0; i < sortingList.getSize(); i++) {
			org.w3c.dom.Element element = (org.w3c.dom.Element)sortingList.getElementAt(i);
			referTable = new XFTableEvaluator_ReferTable(element, this);
			referTableList.add(referTable);
			for (int j = 0; j < referTable.getFieldList().size(); j++) {
				fieldList.add(referTable.getFieldList().get(j));
			}
		}

		updateCounterID = tableElement.getAttribute("UpdateCounter");
		if (updateCounterID.equals("")) {
			updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
		} else {
			if (updateCounterID.toUpperCase().equals("*NONE")) {
				updateCounterID = "";
			}
		}

		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID, element, session_.getTableNodeList()));
		}

		//////////////////////////////////////
		// Setup Script Engine and Bindings //
		//////////////////////////////////////
		scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
		scriptBindings = scriptEngine.createBindings();
		scriptBindings.put("instance", instance_);
		for (int i = 0; i < fieldList.size(); i++) {
			scriptBindings.put(fieldList.get(i).getDataSourceID(), fieldList.get(i));
		}
	}

	public Session getSession() {
		return session_;
	}
	
	public void evalScript(String scriptName, String scriptText) throws ScriptException {
		if (!scriptText.equals("")) {
			scriptNameRunning = scriptName;
			StringBuffer bf = new StringBuffer();
			bf.append(scriptText);
			bf.append(session_.getScriptFunctions());
			scriptEngine.eval(bf.toString(), scriptBindings);
		}
	}

	public String getTableID() {
		return tableID_;
	}

	public String getTableName() {
		return tableName;
	}

	public String getDetailRowNoID() {
		return detailRowNoID;
	}

	public void setOrderBy(String text) {
		orderBy = text;
	}

	public void clear() {
		orderBy = "";
		tableOperatorToSelect = null;
		for (int i = 0; i < fieldList.size(); i++) {
			fieldList.get(i).setValue(fieldList.get(i).getNullValue());
		}
	}
	   
    public void addKeyValue(String fieldID, Object value) {
    	String fieldID_ = fieldID;
    	String operand_ = " = ";
    	if (fieldID.contains("!=")) {
    		fieldID_ = fieldID.replace("!=", "");
    		operand_ = " != ";
    	} else {
    		if (fieldID.contains("<=")) {
    			fieldID_ = fieldID.replace("<=", "");
    			operand_ = " <= ";
    		} else {
    			if (fieldID.contains(">=")) {
    				fieldID_ = fieldID.replace(">=", "");
    				operand_ = " >= ";
    			} else {
    				if (fieldID.contains("=")) {
    					fieldID_ = fieldID.replace("=", "");
    					operand_ = " = ";
    				}
    				if (fieldID.contains("<")) {
    					fieldID_ = fieldID.replace("<", "");
    					operand_ = " < ";
    				}
    				if (fieldID.contains(">")) {
    					fieldID_ = fieldID.replace(">", "");
    					operand_ = " > ";
    				}
    			}
    		}
    	}
		fieldID_ = fieldID_.trim();

    	if (withKeyList_.size() > 0) {
        	withKeyList_.add(" and ");
    	}
		if (value.toString().trim().startsWith("'") && value.toString().trim().endsWith("'")) {
	    	withKeyList_.add(fieldID_ + operand_ + value);
		} else {
			org.w3c.dom.Element workElement = session_.getFieldElement(tableID_, fieldID_);
    		if (workElement == null) { //UPDCOUNTER//
				withKeyList_.add(fieldID_ + operand_ + value);
    		} else {
    			if ((workElement.getAttribute("Type").contains("DATE") || workElement.getAttribute("Type").contains("TIME"))
    					&& operand_.equals(" = ") && value.toString().trim().equals("")) {
    				withKeyList_.add(fieldID_ + " is NULL");
    			} else {
    				String basicType = XFUtility.getBasicTypeOf(workElement.getAttribute("Type"));
    				if (XFUtility.isLiteralRequiredBasicType(basicType)) {
    					int length = Integer.parseInt(workElement.getAttribute("Size"));
    					withKeyList_.add(fieldID_ + operand_ + getLiteraledStringValue(value.toString(), length));
    				} else {
    					withKeyList_.add(fieldID_ + operand_ + value);
    				}
    			}
    		}
		}

		tableOperatorToSelect = null;
    }
    
    private String getLiteraledStringValue(String value, int length) {
		StringBuffer bf = new StringBuffer();
    	bf.append("'");
    	if (value.length() > length) {
	    	bf.append(value.substring(0, length));
    	} else {
	    	bf.append(value);
    	    for (int i = 0; i < length - value.length(); i++) {
    	    	bf.append(" ");
    	    }
    	}
    	bf.append("'");
    	return bf.toString();
    }

    public void addKeyValue(String prefix, String fieldID, Object value, String postfix) {
    	String fieldID_ = fieldID;
    	String operand_ = " = ";
    	if (fieldID.contains("!=")) {
    		fieldID_ = fieldID.replace("!=", "");
    		operand_ = " != ";
    	} else {
    		if (fieldID.contains("<=")) {
    			fieldID_ = fieldID.replace("<=", "");
    			operand_ = " <= ";
    		} else {
    			if (fieldID.contains(">=")) {
    				fieldID_ = fieldID.replace(">=", "");
    				operand_ = " >= ";
    			} else {
    				if (fieldID.contains("=")) {
    					fieldID_ = fieldID.replace("=", "");
    					operand_ = " = ";
    				}
    				if (fieldID.contains("<")) {
    					fieldID_ = fieldID.replace("<", "");
    					operand_ = " < ";
    				}
    				if (fieldID.contains(">")) {
    					fieldID_ = fieldID.replace(">", "");
    					operand_ = " > ";
    				}
    			}
    		}
    	}
		fieldID_ = fieldID_.trim();

		if (value.toString().trim().startsWith("'") && value.toString().trim().endsWith("'")) {
	    	withKeyList_.add(prefix + " " + fieldID_ + operand_ + value + " " + postfix);
		} else {
			org.w3c.dom.Element workElement = session_.getFieldElement(tableID_, fieldID_);
    		if (workElement == null) { //UPDCOUNTER//
				withKeyList_.add(prefix + " " + fieldID_ + operand_ + value + " " + postfix);
    		} else {
    			if (workElement.getAttribute("Type").contains("DATE") || workElement.getAttribute("Type").contains("TIME")) {
    				if (value.toString().trim().equals("")) {
    					if (operand_.equals(" = ")) {
    						withKeyList_.add(prefix + " " + fieldID_ + " is NULL " + postfix);
    					}
    					if (operand_.equals(" != ")) {
    						withKeyList_.add(prefix + " " + fieldID_ + " is not NULL " + postfix);
    					}
    				} else {
    					withKeyList_.add(prefix + " " + fieldID_ + operand_ + "'" + value + "' " + postfix);
    				}
    			} else {
    				String basicType = XFUtility.getBasicTypeOf(workElement.getAttribute("Type"));
//					withKeyList_.add(prefix + " " + fieldID_ + operand_ + XFUtility.getTableOperationValue(basicType, value, dbName) + " " + postfix);
    				if (XFUtility.isLiteralRequiredBasicType(basicType)) {
    					int length = Integer.parseInt(workElement.getAttribute("Size"));
    					withKeyList_.add(prefix + " " + fieldID_ + operand_ + getLiteraledStringValue(value.toString(), length) + " " + postfix);
    				} else {
    					withKeyList_.add(prefix + " " + fieldID_ + operand_ + XFUtility.getTableOperationValue(basicType, value, dbName) + " " + postfix);
    				}
    			}
    		}
		}

		tableOperatorToSelect = null;
    }
    
    public void addValue(String dataSourceID, Object value) {
    	setValueOf(dataSourceID, value);
    }
    
    public void setValueOf(String dataSourceID, Object value) {
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getDataSourceID().equals(dataSourceID)) {
				fieldList.get(i).setValue(value);
				break;
			}
		}
    }
    
    public Object getValueOf(String dataSourceID) {
    	Object fieldValue = null;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getDataSourceID().equals(dataSourceID)) {
				fieldValue = fieldList.get(i).getValue();
				break;
			}
		}
    	return fieldValue;
    }
    
    public XFTableEvaluator_Field getFieldOf(String dataSourceID) {
    	XFTableEvaluator_Field fieldObject = null;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getDataSourceID().equals(dataSourceID)) {
				fieldObject = fieldList.get(i);
				break;
			}
		}
    	return fieldObject;
    }
    
    public int getFieldCount() {
    	return fieldList.size();
    }

    public XFScriptable getInstance() {
    	return instance_;
    }
    public XFTableEvaluator_Field getFieldAt(int index) {
    	XFTableEvaluator_Field fieldObject = null;
    	if (index >= 0 && index < fieldList.size()) {
			fieldObject = fieldList.get(index);
		}
    	return fieldObject;
    }

	public PrintStream getExceptionStream() {
		return instance_.getExceptionStream();
	}

	public void setErrorAndCloseFunction() {
		instance_.setErrorAndCloseFunction();
	}

	public ArrayList<XFTableEvaluator_Field> getFieldList() {
		return fieldList;
	}

	public ArrayList<String> getKeyFieldList() {
		return keyFieldList;
	}

	public ArrayList<XFTableEvaluator_ReferTable> getReferTableList() {
		return referTableList;
	}

	public int select() {
		int countOfRows = 0;
		try {
			tableOperatorToSelect = instance_.createTableOperator(getSQLToSelect());
			for (int i = 0; i < fieldList.size(); i++) {
				fieldList.get(i).setValue(fieldList.get(i).getNullValue());
			}
			runScript("BR", ""); /* Script to be run BEFORE READ */
			countOfRows = tableOperatorToSelect.execute();
		} catch(ScriptException e) {
			instance_.cancelWithScriptException(e, scriptNameRunning);
		} catch(Exception e) {
			instance_.cancelWithException(e);
		}
		return countOfRows;
	}

	public boolean next() {
		boolean hasFound = false;
		String sql;
		XFTableOperator referTableOperator;

		try {
			if (tableOperatorToSelect == null) {
				select();
			}

			hasFound = tableOperatorToSelect.next();
			if (hasFound) {
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).isFieldOnPrimaryTable()
							&& tableOperatorToSelect.hasValueOf(fieldList.get(i).getTableID(), fieldList.get(i).getFieldID())) {
						fieldList.get(i).setValueOfResultSet(tableOperatorToSelect);
					}
				}
				if (!updateCounterID.equals("")) {
					String wrkStr = tableOperatorToSelect.getValueOf(updateCounterID).toString();
					updateCounterValue = Integer.parseInt(wrkStr);
				}

				runScript("AR", "BR()"); /* Script to be run AFTER READ primary table */
				for (int i = 0; i < referTableList.size(); i++) {
					runScript("AR", "BR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE READ */
					sql = referTableList.get(i).getSelectSQL();
					if (!sql.equals("")) {
						referTableOperator = instance_.createTableOperator(sql);
						if (referTableOperator.next()) {
							for (int j = 0; j < fieldList.size(); j++) {
								if (fieldList.get(j).getTableID().equals(referTableList.get(i).getTableID())
										&& fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
									if (referTableOperator.hasValueOf(fieldList.get(j).getTableID(), fieldList.get(j).getFieldID())) {
										fieldList.get(j).setValueOfResultSet(referTableOperator);
									}
								}
							}
							runScript("AR", "AR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run AFTER READ */
						}
					}
				}
				runScript("AR", "AR()"); /* Script to be run AFTER READ */

				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).isFieldOnPrimaryTable()) {
						fieldList.get(i).setOldValue(fieldList.get(i).getValue());
					}
				}
			}
		} catch(Exception e) {
			instance_.cancelWithException(e);
		}
		return hasFound;
	}

	public int insert() {
		return insert(false);
	}

	public int insert(boolean isCheckOnly) {
		int countOfProcessed = 0;
		int countOfErrors = 0;
		try {
			for (int i = 0; i < fieldList.size(); i++) {
				fieldList.get(i).setError(false);
			}

			runScript("BC", "BR()"); /* Script to be run BEFORE CREATE */
			for (int i = 0; i < referTableList.size(); i++) {
				if (!referTableList.get(i).isKeyNullable() || !referTableList.get(i).isKeyNull()) {
					runScript("BC", "BR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE CREATE */
					tableOperator = instance_.createTableOperator(referTableList.get(i).getSelectSQL());
					if (tableOperator.next()) {
						for (int j = 0; j < fieldList.size(); j++) {
							if (fieldList.get(j).getTableID().equals(referTableList.get(i).getTableID())
									&& fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
								fieldList.get(j).setValueOfResultSet(tableOperator);
							}
						}
					} else {
						if (!referTableList.get(i).isOptional()) {
							referTableList.get(i).setErrorOnRelatedFields();
						}
					}
					runScript("BC", "AR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE CREATE */
				}
			}
			runScript("BC", "AR()"); /* Script to be run BEFORE CREATE */
			
			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).isError()) {
					countOfErrors++;
				}
			}
			if (countOfErrors == 0) {
				if (hasNoErrorWithKey("INSERT") && !isCheckOnly) {
					for (int i = 0; i < fieldList.size(); i++) {
						if (fieldList.get(i).isAutoNumberField()) {
							fieldList.get(i).setValue(fieldList.get(i).getAutoNumber());
						}
						if (fieldList.get(i).isAutoDetailRowNumber()) {
							int lastNumber = 0;
							tableOperator = instance_.createTableOperator(getSQLToGetLastDetailRowNumberValue());
							if (tableOperator.next()) {
								lastNumber = Integer.parseInt(tableOperator.getValueOf(detailRowNoID).toString());
							}
							fieldList.get(i).setValue(lastNumber + 1);
						}
					}
					tableOperator = instance_.createTableOperator(getSQLToInsert());
					countOfProcessed = tableOperator.execute();
					if (countOfProcessed == 1) {
						runScript("AC", ""); /* Script to be run AFTER CREATE */
					}
				}
			}
		} catch(ScriptException e) {
			instance_.cancelWithScriptException(e, scriptNameRunning);
		} catch(Exception e) {
			instance_.cancelWithException(e);
		}
		return countOfProcessed;
	}

	public int update() {
		return update(false);
	}

	public int update(boolean isCheckOnly) {
		int countOfProcessed = 0;

		try {
			for (int i = 0; i < fieldList.size(); i++) {
				fieldList.get(i).setError(false);
			}

			runScript("BU", "BR()"); /* Script to be run BEFORE UPDATE */
			for (int i = 0; i < referTableList.size(); i++) {
				if (!referTableList.get(i).isKeyNullable() || !referTableList.get(i).isKeyNull()) {
					runScript("BU", "BR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE UPDATE */
					tableOperator = instance_.createTableOperator(referTableList.get(i).getSelectSQL());
					if (tableOperator.next()) {
						for (int j = 0; j < fieldList.size(); j++) {
							if (fieldList.get(j).getTableID().equals(referTableList.get(i).getTableID())
									&& fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
								fieldList.get(j).setValueOfResultSet(tableOperator);
							}
						}
					} else {
						if (!referTableList.get(i).isOptional()) {
							referTableList.get(i).setErrorOnRelatedFields();
						}
					}
					runScript("BU", "AR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE UPDATE */
				}
			}
			runScript("BU", "AR()"); /* Script to be run BEFORE UPDATE */

			int countOfErrors = 0;
			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).isFieldOnPrimaryTable()) {
					fieldList.get(i).checkError();
					if (fieldList.get(i).isError()) {
						countOfErrors++;
					}
				}
			}
			if (countOfErrors == 0) {
				if (hasNoErrorWithKey("UPDATE") && !isCheckOnly) {
					tableOperator = instance_.createTableOperator(getSQLToUpdate());
					countOfProcessed = tableOperator.execute();
					if (countOfProcessed == 1) {
						runScript("AU", ""); /* Script to be run AFTER UPDATE */
					}
				}
			}
		} catch(ScriptException e) {
			instance_.cancelWithScriptException(e, scriptNameRunning);
		} catch(Exception e) {
			instance_.cancelWithException(e);
		}
		return countOfProcessed;
	}

	public int delete() {
		return delete(false);
	}

	public int delete(boolean isCheckOnly) {
		int countOfProcessed = 0;
		int countOfErrors = 0;
		try {
			for (int i = 0; i < fieldList.size(); i++) {
				fieldList.get(i).setError(false);
			}

			runScript("BD", "BR()"); /* Script to be run BEFORE DELETE */
			for (int i = 0; i < referTableList.size(); i++) {
				if (!referTableList.get(i).isKeyNullable() || !referTableList.get(i).isKeyNull()) {
					runScript("BD", "BR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE DELETE */
					tableOperator = instance_.createTableOperator(referTableList.get(i).getSelectSQL());
					if (tableOperator.next()) {
						for (int j = 0; j < fieldList.size(); j++) {
							if (fieldList.get(j).getTableID().equals(referTableList.get(i).getTableID())
									&& fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
								fieldList.get(j).setValueOfResultSet(tableOperator);
							}
						}
					} else {
						if (!referTableList.get(i).isOptional()) {
							referTableList.get(i).setErrorOnRelatedFields();
						}
					}
					runScript("BD", "AR(" + referTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE DELETE */
				}
			}
			runScript("BD", "AR()"); /* Script to be run BEFORE DELETE */
			
			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).isError()) {
					countOfErrors++;
				}
			}
			if (countOfErrors == 0 && isCheckOnly) {
				tableOperator = instance_.createTableOperator(getSQLToDelete());
				countOfProcessed = tableOperator.execute();
				if (countOfProcessed == 1) {
					runScript("AD", ""); /* Script to be run AFTER DELETE */
				}
			}
		} catch(ScriptException e) {
			instance_.cancelWithScriptException(e, scriptNameRunning);
		} catch(Exception e) {
			instance_.cancelWithException(e);
		}
		return countOfProcessed;
	}
	
	private boolean hasNoErrorWithKey(String operation) {
		boolean hasNoError = true;
		ArrayList<String> keyFieldList = new ArrayList<String>();
		String sql = "";

		try {
			if (operation.equals("INSERT")) {
				boolean hasAutoNumberKey = false;
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).isAutoNumberField()) {
						hasAutoNumberKey = true;
						break;
					}
					if (fieldList.get(i).isAutoDetailRowNumber()) {
						hasAutoNumberKey = true;
						break;
					}
				}
				if (!hasAutoNumberKey) {
					tableOperator = instance_.createTableOperator(getSQLToCheckPKDuplication());
					if (tableOperator.next()) {
						hasNoError = false;
						for (int i = 0; i < fieldList.size(); i++) {
							if (fieldList.get(i).isKey()) {
								fieldList.get(i).setError(XFUtility.RESOURCE.getString("FunctionError31"));
							}
						}
					}
				}
			}

			if (operation.equals("UPDATE")) {
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).isKey()) {
						if (!fieldList.get(i).isError() && fieldList.get(i).isValueChanged()) {
							fieldList.get(i).setError(XFUtility.RESOURCE.getString("FunctionError32"));
							break;
						}
						if (!fieldList.get(i).isError() && fieldList.get(i).isNull()) {
							fieldList.get(i).setError(XFUtility.RESOURCE.getString("FunctionError54"));
							break;
						}
					}
				}
			}

			if (hasNoError) {
				StringTokenizer workTokenizer;
				for (int i = 0; i < uniqueKeyList.size(); i++) {
					keyFieldList.clear();
					workTokenizer = new StringTokenizer(uniqueKeyList.get(i), ";" );
					while (workTokenizer.hasMoreTokens()) {
						keyFieldList.add(workTokenizer.nextToken());
					}
					if (operation.equals("UPDATE")) {
						sql = getSQLToCheckSKDuplication(keyFieldList, true);
					} else {
						sql = getSQLToCheckSKDuplication(keyFieldList, false);
					}
					if (!sql.equals("")) {
						tableOperator = instance_.createTableOperator(sql);
						if (tableOperator.next()) {
							hasNoError = false;
							for (int j = 0; j < fieldList.size(); j++) {
								if (keyFieldList.contains(fieldList.get(j).getFieldID())) {
									fieldList.get(j).setError(XFUtility.RESOURCE.getString("FunctionError22"));
								}
							}
						}
					}
				}
			}
		} catch(Exception e) {
			instance_.cancelWithException(e);
		}
		return hasNoError;
	}

	public String getSQLToSelect(){
		StringBuffer buf = new StringBuffer();

		buf.append("select ");
		boolean firstField = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isFieldOnPrimaryTable()
			&& !fieldList.get(i).isVirtualField()) {
				if (!firstField) {
					buf.append(",");
				}
				buf.append(fieldList.get(i).getFieldID());
				firstField = false;
			}
		}
		if (!updateCounterID.equals("")) {
			buf.append(",");
			buf.append(updateCounterID);
		}
		buf.append(" from ");
		buf.append(moduleID);

		if (!orderBy.equals("")) {
			buf.append(" order by ");
			buf.append(orderBy);
		}

		int countOfWhereKey = 0;
		for (int i = 0; i < withKeyList_.size(); i++) {
			if (countOfWhereKey == 0) {
				buf.append(" where ");
			}
			buf.append(withKeyList_.get(i));
			countOfWhereKey++;
		}
		if (!fixedWhere.equals("")) {
			if (countOfWhereKey == 0) {
				buf.append(" where (");
			} else {
				buf.append(" and (");
			}
			buf.append(fixedWhere);
			buf.append(") ");
			countOfWhereKey++;
		}

		return buf.toString();
	}
	
	public String getSQLToGetLastDetailRowNumberValue(){
		StringBuffer buf = new StringBuffer();
		if (!detailRowNoID.equals("")) {
			buf.append("select ");
			buf.append(detailRowNoID);
			buf.append(" from ");
			buf.append(moduleID);
			buf.append(" where ") ;
			int orderOfFieldInKey = 0;
			for (int i = 0; i < fieldList.size(); i++) {
				if (fieldList.get(i).isKey()
						&& !fieldList.get(i).getFieldID().equals(detailRowNoID)) {
					if (orderOfFieldInKey > 0) {
						buf.append(" and ") ;
					}
					buf.append(fieldList.get(i).getFieldID()) ;
					buf.append("=") ;
					buf.append(XFUtility.getTableOperationValue(fieldList.get(i).getBasicType(), fieldList.get(i).getValue(), dbName));
					orderOfFieldInKey++;
				}
			}
			buf.append(" order by ");
			buf.append(detailRowNoID);
			buf.append(" desc");
		}
		return buf.toString();
	}

	String getSQLToInsert() {
		StringBuffer statementBuf = new StringBuffer();

		statementBuf.append("insert into ");
		statementBuf.append(moduleID);

		statementBuf.append(" (");
		boolean firstField = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isFieldOnPrimaryTable()
			&& !fieldList.get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ") ;
				}
				statementBuf.append(fieldList.get(i).getFieldID()) ;
				firstField = false;
			}
		}
		statementBuf.append(") values(") ;
		firstField = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isFieldOnPrimaryTable()
			&& !fieldList.get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ") ;
				}
				statementBuf.append(XFUtility.getTableOperationValue(fieldList.get(i).getBasicType(), fieldList.get(i).getValue(), dbName)) ;
				firstField = false;
			}
		}
		statementBuf.append(")") ;

		return statementBuf.toString();
	}

	String getSQLToCheckPKDuplication() {
		StringBuffer statementBuf = new StringBuffer();

		statementBuf.append("select ");
		boolean firstField = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isFieldOnPrimaryTable()
					&& !fieldList.get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(",");
				}
				statementBuf.append(fieldList.get(i).getFieldID());
				firstField = false;
			}
		}
		statementBuf.append(" from ");
		statementBuf.append(moduleID);

		statementBuf.append(" where ") ;
		firstField = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isKey()) {
				if (!firstField) {
					statementBuf.append(" and ") ;
				}
				statementBuf.append(fieldList.get(i).getFieldID()) ;
				statementBuf.append("=") ;
				statementBuf.append(XFUtility.getTableOperationValue(fieldList.get(i).getBasicType(), fieldList.get(i).getValue(), dbName)) ;
				firstField = false;
			}
		}

		return statementBuf.toString();
	}

	public ArrayList<String> getUniqueKeyList() {
		return uniqueKeyList;
	}
	
	public String getSQLToCheckSKDuplication(ArrayList<String> keyFieldList, boolean isToUpdate) {
		StringBuffer statementBuf = new StringBuffer();

		statementBuf.append("select ");
		boolean firstField = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isFieldOnPrimaryTable()
					&& !fieldList.get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(",");
				}
				statementBuf.append(fieldList.get(i).getFieldID());
				firstField = false;
			}
		}
		statementBuf.append(" from ");
		statementBuf.append(moduleID);

		statementBuf.append(" where ") ;
		firstField = true;
		for (int j = 0; j < fieldList.size(); j++) {
			if (fieldList.get(j).isFieldOnPrimaryTable()) {
				for (int p = 0; p < keyFieldList.size(); p++) {
					if (fieldList.get(j).getFieldID().equals(keyFieldList.get(p))) {
						if (!isToUpdate && fieldList.get(j).isAutoNumberField()) {
							return "";
						} else {
							if (!firstField) {
								statementBuf.append(" and ") ;
							}
							statementBuf.append(fieldList.get(j).getFieldID()) ;
							statementBuf.append("=") ;
							statementBuf.append(XFUtility.getTableOperationValue(fieldList.get(j).getBasicType(), fieldList.get(j).getValue(), dbName)) ;
							firstField = false;
						}
					}
				}
			}
		}

		if (isToUpdate) {
			firstField = true;
			for (int j = 0; j < fieldList.size(); j++) {
				if (fieldList.get(j).isFieldOnPrimaryTable()) {
					if (fieldList.get(j).isKey()) {
						if (firstField) {
							statementBuf.append(" and (") ;
						} else {
							statementBuf.append(" or ") ;
						}
						statementBuf.append(fieldList.get(j).getFieldID()) ;
						statementBuf.append("!=") ;
						statementBuf.append(XFUtility.getTableOperationValue(fieldList.get(j).getBasicType(), fieldList.get(j).getValue(), dbName)) ;
						firstField = false;
					}
				}
			}
			statementBuf.append(")") ;
		}

		return statementBuf.toString();
	}

	String getSQLToUpdate() {
		StringBuffer statementBuf = new StringBuffer();

		statementBuf.append("update ");
		statementBuf.append(moduleID);
		statementBuf.append(" set ");
		boolean firstField = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isFieldOnPrimaryTable()
			&& !fieldList.get(i).isKey()
			&& !fieldList.get(i).isVirtualField()) {
				if (!firstField) {
					statementBuf.append(", ") ;
				}
				statementBuf.append(fieldList.get(i).getFieldID()) ;
				statementBuf.append("=") ;
				statementBuf.append(XFUtility.getTableOperationValue(fieldList.get(i).getBasicType(), fieldList.get(i).getValue(), dbName)) ;
				firstField = false;
			}
		}
		if (!updateCounterID.equals("")) {
			statementBuf.append(", ") ;
			statementBuf.append(updateCounterID) ;
			statementBuf.append("=") ;
			statementBuf.append(updateCounterValue + 1) ;
		}

		//////////////////////////////////////////////////////////////////////
		// Note that update is done with PK not with keys set by addKeyOf() //
		//////////////////////////////////////////////////////////////////////
		statementBuf.append(" where ") ;
		firstField = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isFieldOnPrimaryTable() && fieldList.get(i).isKey()) {
				if (!firstField) {
					statementBuf.append(" and ") ;
				}
				statementBuf.append(fieldList.get(i).getFieldID()) ;
				statementBuf.append("=") ;
				statementBuf.append(XFUtility.getTableOperationValue(fieldList.get(i).getBasicType(), fieldList.get(i).getValue(), dbName)) ;
				firstField = false;
			}
		}
		if (!updateCounterID.equals("")) {
			statementBuf.append(" and ") ;
			statementBuf.append(updateCounterID) ;
			statementBuf.append("=") ;
			statementBuf.append(updateCounterValue) ;
		}

		return statementBuf.toString();
	}

	String getSQLToDelete() {
		StringBuffer statementBuf = new StringBuffer();

		statementBuf.append("delete from ");
		statementBuf.append(moduleID);

		//////////////////////////////////////////////////////////////////////
		// Note that update is done with PK not with keys set by addKeyOf() //
		//////////////////////////////////////////////////////////////////////
		statementBuf.append(" where ") ;
		boolean firstField = true;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).isFieldOnPrimaryTable() && fieldList.get(i).isKey()) {
				if (!firstField) {
					statementBuf.append(" and ") ;
				}
				statementBuf.append(fieldList.get(i).getFieldID()) ;
				statementBuf.append("=") ;
				statementBuf.append(XFUtility.getTableOperationValue(fieldList.get(i).getBasicType(), fieldList.get(i).getValue(), dbName)) ;
				firstField = false;
			}
		}
		if (!updateCounterID.equals("")) {
			statementBuf.append(" and ") ;
			statementBuf.append(updateCounterID) ;
			statementBuf.append("=") ;
			statementBuf.append(updateCounterValue) ;
		}

		return statementBuf.toString();
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
		if (validScriptList.size() > 0) {
			for (int i = 0; i < validScriptList.size(); i++) {
				evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
			}
		}
	}
}


class XFTableEvaluator_ReferTable extends Object {
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private String tableID = "";
	private String moduleID = "";
	private String tableAlias = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<XFTableEvaluator_Field> fieldList = new ArrayList<XFTableEvaluator_Field>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private boolean isOptional = false;
	private String dbName = "";
	private XFTableEvaluator evaluator_;

	public XFTableEvaluator_ReferTable(org.w3c.dom.Element referElement, XFTableEvaluator evaluator){
		super();

		referElement_ = referElement;
		evaluator_ = evaluator;

		tableID = referElement_.getAttribute("ToTable");
		tableElement = evaluator_.getSession().getTableElement(tableID);
    	moduleID = tableID;
		if (!tableElement.getAttribute("ModuleID").equals("")) {
	    	moduleID = tableElement.getAttribute("ModuleID");
		}

		String dbID = tableElement.getAttribute("DB");
		if (dbID.equals("")) {
			dbName = evaluator_.getSession().getDatabaseName();
		} else {
			dbName = evaluator_.getSession().getSubDBName(dbID);
		}
		StringTokenizer workTokenizer;
		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}

		String workFieldID;
		workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			workFieldID = workTokenizer.nextToken();
			fieldIDList.add(workFieldID);
			fieldList.add(new XFTableEvaluator_Field(tableID, tableAlias, workFieldID, evaluator_));
		}

		if (referElement_.getAttribute("ToKeyFields").equals("")) {
			org.w3c.dom.Element workElement = evaluator_.getSession().getTablePKElement(tableID);
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

		if (referElement_.getAttribute("Optional").equals("T")) {
			isOptional = true;
		}
	}

	public String getSelectSQL(){
		org.w3c.dom.Element workElement;
		StringBuffer buf = new StringBuffer();

		buf.append("select ");
		int count = 0;
		for (int i = 0; i < fieldIDList.size(); i++) {
			workElement = evaluator_.getSession().getFieldElement(tableID, fieldIDList.get(i));
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				if (count > 0) {
					buf.append(",");
				}
				count++;
				buf.append(fieldIDList.get(i));
			}
		}
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (count > 0) {
				buf.append(",");
			}
			count++;
			buf.append(toKeyFieldIDList.get(i));
		}

		buf.append(" from ");
		buf.append(moduleID);

		String dataSourceName;
		count = 0;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (count == 0) {
				buf.append(" where ");
			} else {
				buf.append(" and ");
			}
			buf.append(toKeyFieldIDList.get(i));
			buf.append("=");
			for (int j = 0; j < evaluator_.getFieldList().size(); j++) {
				dataSourceName = evaluator_.getFieldList().get(j).getTableAlias() + "." + evaluator_.getFieldList().get(j).getFieldID();
				if (withKeyFieldIDList.get(i).equals(dataSourceName)) {
					buf.append(XFUtility.getTableOperationValue(evaluator_.getFieldList().get(j).getBasicType(), evaluator_.getFieldList().get(j).getValue(), dbName)) ;
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

	public boolean isOptional() {
		return isOptional;
	}

	public boolean isKeyNullable() {
		boolean isKeyNullable = false;
		String dataSourceName;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < evaluator_.getFieldList().size(); j++) {
				dataSourceName = evaluator_.getFieldList().get(j).getTableAlias() + "." + evaluator_.getFieldList().get(j).getFieldID();
				if (withKeyFieldIDList.get(i).equals(dataSourceName)) {
					if (evaluator_.getFieldList().get(j).isNullable()) {
						isKeyNullable = true;
						break;
					}
				}
			}
		}
		return isKeyNullable;
	}

	public boolean isKeyNull() {
		boolean isKeyNull = false;
		String dataSourceName;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < evaluator_.getFieldList().size(); j++) {
				dataSourceName = evaluator_.getFieldList().get(j).getTableAlias() + "." + evaluator_.getFieldList().get(j).getFieldID();
				if (withKeyFieldIDList.get(i).equals(dataSourceName)) {
					if (evaluator_.getFieldList().get(j).isNull()) {
						isKeyNull = true;
						break;
					}
				}
			}
		}
		return isKeyNull;
	}

	public void setErrorOnRelatedFields() {
		String dataSourceName;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			for (int j = 0; j < evaluator_.getFieldList().size(); j++) {
				dataSourceName = evaluator_.getFieldList().get(j).getTableAlias() + "." + evaluator_.getFieldList().get(j).getFieldID();
				if (withKeyFieldIDList.get(i).equals(dataSourceName) && !evaluator_.getFieldList().get(j).isError()) {
					evaluator_.getFieldList().get(j).setError(XFUtility.RESOURCE.getString("FunctionError53") + tableElement.getAttribute("Name") + XFUtility.RESOURCE.getString("FunctionError45"));
					break;
				}
			}
		}
	}

	public ArrayList<String> getKeyFieldIDList(){
		return toKeyFieldIDList;
	}

	public ArrayList<String> getFieldIDList(){
		return fieldIDList;
	}

	public ArrayList<XFTableEvaluator_Field> getFieldList(){
		return fieldList;
	}

	public ArrayList<String> getWithKeyFieldIDList(){
		return withKeyFieldIDList;
	}
}

class XFTableEvaluator_Field extends Object implements XFFieldScriptable {
	private String tableID_ = "";
	private String tableAlias_ = "";
	private String fieldID_ = "";
	private String fieldName = "";
	private String dataType = "";
	private int dataSize = 5;
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private String autoNumberKey = "";
	private String errorMessage = "";
	private Object value_ = null;
	private Object oldValue_ = null;
	private boolean isNullable = true;
	private boolean isKey = false;
	private boolean isNoUpdate = false;
	private boolean isFieldOnPrimaryTable = false;
	private boolean isError = false;
	private boolean isVirtualField = false;
	private boolean isEditable = true;
	private boolean isAutoDetailRowNumber = false;
	private Color foreground = Color.black;
	private XFTableEvaluator evaluator_;

	public XFTableEvaluator_Field(String tableID, String tableAlias, String fieldID, XFTableEvaluator evaluator){
		super();

		tableID_ = tableID;
		tableAlias_ = tableAlias;
		if (tableAlias_.equals("")) {
			tableAlias_ = tableID;
		}
		fieldID_ = fieldID;
		evaluator_ = evaluator;

		org.w3c.dom.Element fieldElement = evaluator_.getSession().getFieldElement(tableID_, fieldID_);
		dataType = fieldElement.getAttribute("Type");
		fieldName = fieldElement.getAttribute("Name");
		dataSize = Integer.parseInt(fieldElement.getAttribute("Size"));
		dataTypeOptions = fieldElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (fieldElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}
		if (fieldElement.getAttribute("NoUpdate").equals("T")) {
			isNoUpdate = true;
		}
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}

		if (tableID_.equals(evaluator_.getTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnPrimaryTable = true;
			isEditable = false;
			ArrayList<String> keyFieldIDList = evaluator_.getKeyFieldList();
			for (int i = 0; i < keyFieldIDList.size(); i++) {
				if (keyFieldIDList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
			if (fieldID_.equals(evaluator_.getDetailRowNoID())) {
				isAutoDetailRowNumber = true;
			}
		} else {
			isEditable = true;
		}

		String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			autoNumberKey = wrkStr;
		}

		setValue(getNullValue());
		setOldValue(getNullValue());
	}

	public String getDataSourceID(){
		return tableAlias_ + "_" + fieldID_;
	}

	public String getFieldID(){
		return fieldID_;
	}

	public String getName(){
		return fieldName;
	}

	public String getTableAlias(){
		return tableAlias_;
	}

	public String getTableID(){
		return tableID_;
	}

	public boolean isAutoDetailRowNumber(){
		return isAutoDetailRowNumber;
	}

	public boolean isNullable(){
		return isNullable;
	}

	public boolean isVirtualField(){
		return isVirtualField;
	}

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}
	
	public ArrayList<String> getTypeOptionList() {
		return dataTypeOptionList;
	}

	public boolean isFieldOnPrimaryTable(){
		return isFieldOnPrimaryTable;
	}

	public boolean isKey(){
		return isKey;
	}

	public void setError(boolean error) {
		isError = error;
	}

	public void setEditable(boolean editable){
		this.isEditable = editable;
	}

	public boolean isNoUpdate() {
		return this.isNoUpdate;
	}

	public boolean isEditable() {
		return this.isEditable;
	}

	public void setValueOfResultSet(XFTableOperator operator){
		try {
			if (!isVirtualField) {
				value_ = operator.getValueOf(this.getFieldID());
				String basicType = this.getBasicType();
				if (basicType.equals("INTEGER")) {
					value_ = Integer.parseInt(operator.getValueOf(this.getFieldID()).toString());
				}
				if (basicType.equals("FLOAT")) {
					value_ = Float.parseFloat(operator.getValueOf(this.getFieldID()).toString());
				}
				if (basicType.equals("STRING") || basicType.equals("TIME") || basicType.equals("DATETIME") || basicType.equals("DATE") || basicType.equals("BYTEA")) {
					value_ = operator.getValueOf(this.getFieldID()).toString();
				}
			}
		} catch(Exception e) {
			e.printStackTrace(evaluator_.getExceptionStream());
			evaluator_.setErrorAndCloseFunction();
		}
	}

	public void setValue(Object object){
		value_ = object;
	}

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public boolean isNull(){
		return XFUtility.isNullValue(this.getBasicType(), value_);
	}

	public void setOldValue(Object object){
		oldValue_ = object;
	}

	public Object getOldValue(){
		return oldValue_;
	}
	
	public void checkError() {
		if (!this.isNullable() && this.getNullValue().equals(value_)) {
			this.setError(XFUtility.RESOURCE.getString("FunctionError16"));
		}
		if (this.isNoUpdate() && this.isValueChanged()) {
			this.setError(XFUtility.RESOURCE.getString("FunctionError51"));
		}
		if (this.isTooLong()) {
			this.setError(XFUtility.RESOURCE.getString("FunctionError55"));
		}
		if (!this.isError) {
			StringTokenizer tokenizer;

			/////////////////
			// KUBUN field //
			/////////////////
			String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
			if (!wrkStr.equals("")) {
				if (!isNullable || !value_.toString().equals("")) {
					boolean isMatched = false;
					String sql = "select * from " + evaluator_.getSession().getTableNameOfUserVariants() + " where IDUSERKUBUN = '" + wrkStr + "' order by SQLIST";
					XFTableOperator operator = evaluator_.getInstance().createTableOperator(sql);
					try {
						while (operator.next()) {
							if (operator.getValueOf("KBUSERKUBUN").toString().trim().equals(value_)) {
								isMatched = true;
								break;
							}
						}
					} catch (Exception e) {}
					if (!isMatched) {
						this.setError(XFUtility.RESOURCE.getString("FunctionError25"));
					}
				}
			}

			//////////////////
			// VALUES field //
			//////////////////
			wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "VALUES");
			if (!wrkStr.equals("")) {
				if (!isNullable || !value_.toString().equals("")) {
					boolean isMatched = false;
					tokenizer = new StringTokenizer(wrkStr, ";");
					while (tokenizer.hasMoreTokens()) {
						if (tokenizer.nextToken().equals(value_)) {
							isMatched = true;
							break;
						}
					}
					if (!isMatched) {
						this.setError(XFUtility.RESOURCE.getString("FunctionError23"));
					}
				}
			}

			///////////////////
			// BOOLEAN field //
			///////////////////
			wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "BOOLEAN");
			if (!wrkStr.equals("")) {
				if (!isNullable || !value_.toString().equals("")) {
					boolean isMatched = false;
					tokenizer = new StringTokenizer(wrkStr, ";");
					while (tokenizer.hasMoreTokens()) {
						if (tokenizer.nextToken().equals(value_)) {
							isMatched = true;
							break;
						}
					}
					if (!isMatched) {
						this.setError(XFUtility.RESOURCE.getString("FunctionError23"));
					}
				}
			}

			if (dataType.equals("DATE")) {
				if (!isNullable || !value_.toString().equals("")) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					try {
						format.parse(value_.toString());
					} catch (ParseException e) {
						this.setError(XFUtility.RESOURCE.getString("FunctionError23"));
					}
				}
			}

			if (dataTypeOptionList.contains("YMONTH")) {
				if (!isNullable || !value_.toString().equals("")) {
					GregorianCalendar calendar = new GregorianCalendar();
					int currentYear = calendar.get(Calendar.YEAR);
					int minimumYear = currentYear - 30;
					int maximumYear = currentYear + 10;
					String value = value_.toString();
					if (value.length() == 6) {
						try {
							int yearValue = Integer.parseInt(value.substring(0, 4));
							int monthValue = Integer.parseInt(value.substring(4, 6));
							if (yearValue < minimumYear || yearValue > maximumYear
									|| monthValue < 1 || monthValue > 12) {
								this.setError(XFUtility.RESOURCE.getString("FunctionError23"));
							}
						} catch (NumberFormatException e) {
							this.setError(XFUtility.RESOURCE.getString("FunctionError23"));
						}
					} else {
						this.setError(XFUtility.RESOURCE.getString("FunctionError23"));
					}
				}
			}

			if (dataTypeOptionList.contains("MSEQ")) {
				if (!isNullable || !value_.toString().equals("")) {
					try {
						int monthValue = Integer.parseInt(value_.toString());
						if (monthValue < 1 || monthValue > 12) {
							this.setError(XFUtility.RESOURCE.getString("FunctionError23"));
						}
					} catch (NumberFormatException e) {
						this.setError(XFUtility.RESOURCE.getString("FunctionError23"));
					}
				}
			}

			if (dataTypeOptionList.contains("FYEAR")) {
				if (!isNullable || !value_.toString().equals("")) {
					GregorianCalendar calendar = new GregorianCalendar();
					int currentYear = calendar.get(Calendar.YEAR);
					int minimumYear = currentYear - 30;
					int maximumYear = currentYear + 10;
					String value = value_.toString();
					if (value.length() == 4) {
						try {
							int yearValue = Integer.parseInt(value);
							if (yearValue < minimumYear || yearValue > maximumYear) {
								this.setError(XFUtility.RESOURCE.getString("FunctionError23"));
							}
						} catch (NumberFormatException e) {
							this.setError(XFUtility.RESOURCE.getString("FunctionError23"));
						}
					} else {
						this.setError(XFUtility.RESOURCE.getString("FunctionError23"));
					}
				}
			}
		}
	}

	public boolean isTooLong() {
		boolean isError = false;
		if (dataType.equals("CHAR") &&  value_ != null) {
			String wrkStr = value_.toString();
			if (wrkStr.length() > this.dataSize) {
				isError = true;
			}
		}
		return isError;
	}

	public boolean isValueChanged() {
		return !this.getValue().equals(this.getOldValue());
	}

	public String getAutoNumber() {
		String value = "";
		if (!autoNumberKey.equals("") && isFieldOnPrimaryTable) {
			value = evaluator_.getSession().getNextNumber(autoNumberKey);
		}
		return value;
	}

	public boolean isAutoNumberField() {
		return !autoNumberKey.equals("") && isFieldOnPrimaryTable;
	}

	public boolean isError() {
		return isError;
	}

	public Object getValue() {
		return value_;
	}

	public void setError(String message) {
		if (!message.equals("")) {
			setError(true);
			if (this.errorMessage.equals("")) {
				this.errorMessage = message;
			} else {
				this.errorMessage = this.errorMessage + " " +message;
			}
		}
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

	public boolean isEnabled() {
		return true;
	}

	public void setEnabled(boolean enabled) {
	}

	public void setValueList(String[] valueList) {
	}

	public String[] getValueList() {
		return new String[0];
	}
}
