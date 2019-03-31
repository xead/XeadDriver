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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import xeadServer.Relation;

////////////////////////////////////////////////////////////////////
// This is a public class that can be used in Table-Script.       //
// Note that public classes must be defined in its own java file. //
////////////////////////////////////////////////////////////////////
public class XFTableOperator {
    private Session session_ = null;
    private Relation relation_ = null;
    private String operation_ = "";
    private String tableID_ = "";
    private String moduleID = "";
    private String dbID = "";
    private String dbName = "";
    private String selectFields_ = "";
    private String orderBy_ = "";
    private String additionalWhere_ = "";
    private String sqlText_ = "";
    private ArrayList<String> fieldIDList_ = new ArrayList<String>();
    //private ArrayList<String> physicalIDList_ = new ArrayList<String>();
    private ArrayList<Object> fieldValueList_ = new ArrayList<Object>();
    private ArrayList<String> withKeyList_ = new ArrayList<String>();
    private StringBuffer logBuf_ = null;
    private boolean hasFoundRecord_ = false;
    private boolean isAutoCommit_ = false;

	public XFTableOperator(Session session, StringBuffer logBuf, String operation, String tableID) throws Exception {
		this(session, logBuf, operation, tableID, false);
	}
	
	public XFTableOperator(Session session, StringBuffer logBuf, String operation, String tableID, boolean isAutoCommit) throws Exception {
		super();
		session_ = session;
		logBuf_ = logBuf;
        operation_ = operation;
    	tableID_ = tableID;
    	isAutoCommit_ = isAutoCommit;

    	org.w3c.dom.Element tableElement = session_.getTableElement(tableID_);
    	if (tableElement == null) {
			throw new Exception("'" + tableID_ + "' is invalid to get Table-Operator.");
    	} else {
    		moduleID = tableElement.getAttribute("ModuleID");
    		if (moduleID.equals("")) {
    			moduleID = tableID_;
    		}
    		dbID = tableElement.getAttribute("DB");
    		if (dbID.equals("")) {
    			dbName = session_.getDatabaseName();
    		} else {
    			dbName = session_.getSubDBName(dbID);
    		}
    	}
	}

	public XFTableOperator(Session session, StringBuffer logBuf, String sqlText) {
		this(session, logBuf, sqlText, false);
	}
	
	public XFTableOperator(Session session, StringBuffer logBuf, String sqlText, boolean isAutoCommit) {
		super();
		session_ = session;
		logBuf_ = logBuf;
		sqlText_ = sqlText;

		int pos1, pos2;
		if (sqlText_.toUpperCase().startsWith("SELECT ")) {
			if (sqlText_.toUpperCase().startsWith("SELECT COUNT(*)")) {
				operation_ = "COUNT";
			} else {
				operation_ = "SELECT";
			}
			pos1 = sqlText_.toUpperCase().indexOf(" FROM ") + 6;
			pos2 = sqlText_.indexOf(" ", pos1);
			if (pos2 < 0) {
				pos2 = sqlText_.length();
			}
			tableID_ = sqlText_.substring(pos1, pos2);
			if (tableID_.startsWith("(")) {
				pos1 = sqlText_.toUpperCase().indexOf(" FROM ", pos1+1) + 6;
				pos2 = sqlText_.indexOf(" ", pos1);
				if (pos2 < 0) {
					pos2 = sqlText_.length();
				}
				tableID_ = sqlText_.substring(pos1, pos2);
			}
		}
		if (sqlText_.toUpperCase().startsWith("INSERT ")) {
			operation_ = "INSERT";
			pos1 = sqlText_.toUpperCase().indexOf("INSERT INTO ") + 12;
			pos2 = sqlText_.indexOf(" ", pos1);
			if (pos2 < 0) {
				pos2 = sqlText_.length();
			}
			tableID_ = sqlText_.substring(pos1, pos2);
		}
		if (sqlText_.toUpperCase().startsWith("UPDATE ")) {
			operation_ = "UPDATE";
			pos1 = sqlText_.toUpperCase().indexOf("UPDATE ") + 7;
			pos2 = sqlText_.indexOf(" ", pos1);
			if (pos2 < 0) {
				pos2 = sqlText_.length();
			}
			tableID_ = sqlText_.substring(pos1, pos2);
		}
		if (sqlText_.toUpperCase().startsWith("DELETE ")) {
			operation_ = "DELETE";
			pos1 = sqlText_.toUpperCase().indexOf("DELETE FROM ") + 12;
			pos2 = sqlText_.indexOf(" ", pos1);
			if (pos2 < 0) {
				pos2 = sqlText_.length();
			}
			tableID_ = sqlText_.substring(pos1, pos2);
		}
    	isAutoCommit_ = isAutoCommit;

    	org.w3c.dom.Element tableElement = session_.getTableElement(tableID_);
    	moduleID = tableElement.getAttribute("ModuleID");
		if (moduleID.equals("")) {
			moduleID = tableID_;
		}
		dbID = tableElement.getAttribute("DB");
		if (dbID.equals("")) {
			dbName = session_.getDatabaseName();
		} else {
			dbName = session_.getSubDBName(dbID);
		}

		if (dbName.contains("jdbc:mysql:")) {
			sqlText_ = sqlText_.replaceAll("\\\\", "\\\\\\\\");
		}
	}

    public void setSelectFields(String fields) {
		sqlText_ = "";
    	selectFields_ = fields;
    }

    public void setDistinctFields(String fields) {
		sqlText_ = "";
    	selectFields_ = "distinct " + fields;
    }

    public void setValueOf(String fieldID, Object value) {
    	addValue(fieldID, value);
    }

    public void addValue(String fieldID, Object value) {
		sqlText_ = "";
    	int index = fieldIDList_.indexOf(fieldID);
    	if (index > -1) {
    		fieldIDList_.remove(index);
    		fieldValueList_.remove(index);
    	}

		org.w3c.dom.Element workElement = session_.getFieldElement(tableID_, fieldID);
		if (workElement == null || workElement.getAttribute("PhysicalID").equals("")) {
	    	fieldIDList_.add(fieldID);
		} else {
	    	fieldIDList_.add(workElement.getAttribute("PhysicalID"));
		}
    	if (value.toString().trim().startsWith("'") && value.toString().trim().endsWith("'")) {
    		if (value.toString().contains("''")) {
        		fieldValueList_.add(value.toString());
    		} else {
    			String strValue = value.toString().substring(1, value.toString().length());
    			strValue = strValue.substring(0, strValue.length()-1);
    			strValue = strValue.replaceAll("'","''");
    			fieldValueList_.add("'" + strValue + "'");
    		}
    	} else {
    		if (workElement == null) { //UPDCOUNTER //
				fieldValueList_.add(value);
    		} else {
    			if ((workElement.getAttribute("Type").contains("DATE") || workElement.getAttribute("Type").contains("TIME"))
    					&& value.toString().trim().equals("")) {
    				fieldValueList_.add("NULL");
    			} else {
    				String basicType = XFUtility.getBasicTypeOf(workElement.getAttribute("Type"));
    				if (basicType.equals("DATETIME") && value.equals("CURRENT_TIMESTAMP")) {
    					fieldValueList_.add(value);
    				} else {
    					fieldValueList_.add(XFUtility.getTableOperationValue(basicType, value, dbName));
    				}
    			}
    		}
    	}
    }
    
    public void addKeyValue(String fieldID, Object value) {
		sqlText_ = "";
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
		org.w3c.dom.Element workElement = session_.getFieldElement(tableID_, fieldID_);
		if (workElement != null && !workElement.getAttribute("PhysicalID").equals("")) {
	    	fieldID_ = workElement.getAttribute("PhysicalID");
		}

		if (withKeyList_.size() > 0) {
        	withKeyList_.add(" and ");
    	}
		if (value.toString().trim().startsWith("'") && value.toString().trim().endsWith("'")) {
	    	withKeyList_.add(fieldID_ + operand_ + value);
		} else {
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
    					if (workElement.getAttribute("Type").contains("VARCHAR")) {
    						length = value.toString().length();
    					}
    					withKeyList_.add(fieldID_ + operand_ + getLiteraledStringValue(value.toString(), length));
    				} else {
    					withKeyList_.add(fieldID_ + operand_ + value);
    				}
    			}
    		}
		}
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
		sqlText_ = "";
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
		org.w3c.dom.Element workElement = session_.getFieldElement(tableID_, fieldID_);
		//if (workElement != null && !workElement.getAttribute("PhysicalID").equals("")) {
	    //	fieldID_ = workElement.getAttribute("PhysicalID");
		//}

		if (value.toString().trim().startsWith("'") && value.toString().trim().endsWith("'")) {
	    	withKeyList_.add(prefix + " " + fieldID_ + operand_ + value + " " + postfix);
		} else {
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
    				if (XFUtility.isLiteralRequiredBasicType(basicType)) {
    					int length = Integer.parseInt(workElement.getAttribute("Size"));
    					if (workElement.getAttribute("Type").contains("VARCHAR")) {
    						length = value.toString().length();
    					}
    					withKeyList_.add(prefix + " " + fieldID_ + operand_ + getLiteraledStringValue(value.toString(), length) + " " + postfix);
    				} else {
    					withKeyList_.add(prefix + " " + fieldID_ + operand_ + XFUtility.getTableOperationValue(basicType, value, dbName) + " " + postfix);
    				}
    			}
    		}
		}
    }

    public void setOrderBy(String text) {
		sqlText_ = "";
    	orderBy_ = text;
    }

    public void addWhere(String text) {
    	additionalWhere_ = text;
    }

    public void setSqlText(String text) {
    	sqlText_ = text;
    }
    
    public String getSqlText() {
    	String text = sqlText_;
        if (text.equals("")) {

            StringBuffer bf = new StringBuffer();

        	if (operation_.toUpperCase().equals("SELECT")) {
        		bf.append("select ");
        		if (selectFields_.equals("")) {
        			bf.append("*");
        		} else {
        			bf.append(selectFields_);
        		}
        		bf.append(" from ");
        		bf.append(moduleID);
        		if (withKeyList_.size() > 0) {
        			bf.append(" where ");
        			for (int i = 0; i < withKeyList_.size(); i++) {
        				bf.append(withKeyList_.get(i));
        			}
        		}
        		if (!additionalWhere_.equals("")) {
        			if (!bf.toString().contains(" where ")) {
            			bf.append(" where ");
        			}
        			if (additionalWhere_.toLowerCase().startsWith("AND ")
        					|| additionalWhere_.toLowerCase().startsWith("AND(")) {
        				bf.append(" ");
        				bf.append(additionalWhere_);
        			} else {
        				if (additionalWhere_.toLowerCase().startsWith(" AND ")
        						|| additionalWhere_.toLowerCase().startsWith(" AND(")) {
        					bf.append(additionalWhere_);
        				} else {
        					bf.append(" and (");
        					bf.append(additionalWhere_);
        					bf.append(")");
        				}
        			}
        		}
        		if (!orderBy_.equals("")) {
        			bf.append(" order by ");
        			bf.append(orderBy_);
        		}
        	}

        	if (operation_.toUpperCase().equals("COUNT")) {
        		bf.append("select count(*) from ");
        		bf.append(moduleID);
        		if (withKeyList_.size() > 0) {
        			bf.append(" where ");
        			for (int i = 0; i < withKeyList_.size(); i++) {
        				bf.append(withKeyList_.get(i));
        			}
        		}
        		if (!additionalWhere_.equals("")) {
        			if (!bf.toString().contains(" where ")) {
            			bf.append(" where ");
        			}
        			if (additionalWhere_.toLowerCase().startsWith("AND ")
        					|| additionalWhere_.toLowerCase().startsWith("AND(")) {
        				bf.append(" ");
        				bf.append(additionalWhere_);
        			} else {
        				if (additionalWhere_.toLowerCase().startsWith(" AND ")
        						|| additionalWhere_.toLowerCase().startsWith(" AND(")) {
        					bf.append(additionalWhere_);
        				} else {
        					bf.append(" and (");
        					bf.append(additionalWhere_);
        					bf.append(")");
        				}
        			}
        		}
        	}

        	if (operation_.toUpperCase().equals("INSERT")) {
        		bf.append(operation_);
        		bf.append(" ");
        		bf.append(" into ");
        		bf.append(moduleID);
        		bf.append(" (");
        		for (int i = 0; i < fieldIDList_.size(); i++) {
        			if (i > 0) {
        				bf.append(", ");
        			}
        			bf.append(fieldIDList_.get(i));
        		}
        		bf.append(") ");
        		bf.append(" values(");
        		for (int i = 0; i < fieldIDList_.size(); i++) {
        			if (i > 0) {
        				bf.append(", ");
        			}
        			bf.append(fieldValueList_.get(i));
        		}
        		bf.append(") ");
        	}

        	if (operation_.toUpperCase().equals("UPDATE")) {
        		bf.append(operation_);
        		bf.append(" ");
        		bf.append(moduleID);
        		bf.append(" set ");
        		for (int i = 0; i < fieldIDList_.size(); i++) {
        			if (i > 0) {
        				bf.append(", ");
        			}
        			bf.append(fieldIDList_.get(i));
        			bf.append(" = ");
        			bf.append(fieldValueList_.get(i));
        		}
        		if (withKeyList_.size() > 0) {
        			bf.append(" where ");
        			for (int i = 0; i < withKeyList_.size(); i++) {
        				bf.append(withKeyList_.get(i));
        			}
        		}
        		if (!additionalWhere_.equals("")) {
        			if (!bf.toString().contains(" where ")) {
            			bf.append(" where ");
        			}
        			if (additionalWhere_.toLowerCase().startsWith("AND ")
        					|| additionalWhere_.toLowerCase().startsWith("AND(")) {
        				bf.append(" ");
        				bf.append(additionalWhere_);
        			} else {
        				if (additionalWhere_.toLowerCase().startsWith(" AND ")
        						|| additionalWhere_.toLowerCase().startsWith(" AND(")) {
        					bf.append(additionalWhere_);
        				} else {
        					bf.append(" and (");
        					bf.append(additionalWhere_);
        					bf.append(")");
        				}
        			}
        		}
        	}

        	if (operation_.toUpperCase().equals("DELETE")) {
        		bf.append(operation_);
        		bf.append(" ");
        		bf.append(" from ");
        		bf.append(moduleID);
        		if (withKeyList_.size() > 0) {
        			bf.append(" where ");
        			for (int i = 0; i < withKeyList_.size(); i++) {
        				bf.append(withKeyList_.get(i));
        			}
        		}
        		if (!additionalWhere_.equals("")) {
        			if (!bf.toString().contains(" where ")) {
            			bf.append(" where ");
        			}
        			if (additionalWhere_.toLowerCase().startsWith("AND ")
        					|| additionalWhere_.toLowerCase().startsWith("AND(")) {
        				bf.append(" ");
        				bf.append(additionalWhere_);
        			} else {
        				if (additionalWhere_.toLowerCase().startsWith(" AND ")
        						|| additionalWhere_.toLowerCase().startsWith(" AND(")) {
        					bf.append(additionalWhere_);
        				} else {
        					bf.append(" and (");
        					bf.append(additionalWhere_);
        					bf.append(")");
        				}
        			}
        		}
        	}

        	text = bf.toString();
    		if (dbName.contains("jdbc:mysql:")) {
    			text = text.replaceAll("\\\\", "\\\\\\\\");
    		}
        } else {
        	text = text.replaceFirst(" " + tableID_ + " ", " " + moduleID + " ");
        }

        return text;
    }
    
    public int execute() throws Exception {
    	int count = -1;
        if (logBuf_ != null) {
        	String text = this.getSqlText();
        	if (text.length() > 1000) {
        		XFUtility.appendLog(text.substring(0, 1000) + "...", logBuf_);
        	} else {
        		XFUtility.appendLog(text, logBuf_);
        	}
        }
		if (session_.getAppServerName().equals("")) {
			try {
				count = executeOnLocal();
			} catch (SQLException e) {
			}
		} else {
			count = executeOnServer();
		}
    	return count;
    }
    
    private int executeOnLocal() throws Exception {
    	int processRowCount = -1;
		Connection connection = null;
		Statement statement = null;

		if (!dbID.equals("") && !operation_.toUpperCase().equals("SELECT") && !operation_.toUpperCase().equals("COUNT")) {
			String message = "Update/Insert/Delete are not permitted to the read-only database.";
			if (session_.isClientSession()) {
				JOptionPane.showMessageDialog(null, message);
			}
			throw new Exception(message);

		} else {
			////////////////////////////////////
			// Loop to retry connecting to DB //
			////////////////////////////////////
			for (;;) {
				try {
					if (dbID.equals("")) {
						if (isAutoCommit_) {
							connection = session_.getConnectionAutoCommit();
						} else {
							connection = session_.getConnectionManualCommit();
						}
					} else {
						connection = session_.getConnectionReadOnly(dbID);
					}
					if (connection != null) {
						if (operation_.toUpperCase().equals("SELECT")) {
							statement = connection.createStatement();
							ResultSet result = statement.executeQuery(this.getSqlText());
							relation_ = new Relation(result);
							processRowCount = relation_.getRowCount();
							result.close();
						} else {
							if (operation_.toUpperCase().equals("COUNT")) {
								statement = connection.createStatement();
								ResultSet result = statement.executeQuery(this.getSqlText());
								if (result.next()) {
									processRowCount = result.getInt(1);
								}
								result.close();
							} else {
								statement = connection.createStatement();
								processRowCount = statement.executeUpdate(this.getSqlText());
							}
						}
						statement.close();
						break;
					}

				} catch (SQLException e) {
					if (session_.isClientSession()) {
						JOptionPane.showMessageDialog(null, "SQL error detected. Process will be canceled.\n" + e.getMessage());
					}
					if (logBuf_ != null) {
						XFUtility.appendLog(e.getMessage(), logBuf_);
					}
					throw e;
//					try {
//						statement.executeQuery("SELECT * FROM " + session_.taxTable);
//						throw new Exception(e.getMessage());
//					} catch (SQLException e1) {
//						if (session_.isClientSession()) {
//							Object[] bts = {XFUtility.RESOURCE.getString("DBConnectMessage1"), XFUtility.RESOURCE.getString("DBConnectMessage2")};
//							int reply = JOptionPane.showOptionDialog(null, XFUtility.RESOURCE.getString("DBConnectMessage3"),
//									XFUtility.RESOURCE.getString("DBConnectMessage4"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[1]);
//							if (reply == 0) {
//								System.exit(0);
//							}
//							if (reply == 1) {
//								boolean isOkay = session_.setupConnectionToDatabase(false);
//								if (isOkay) {
//									break;
//								}
//							}
//						} else {
//							System.exit(0);
//						}
//					}

				} catch (OutOfMemoryError e) {
					if (logBuf_ != null) {
						XFUtility.appendLog(e.getMessage(), logBuf_);
					}
					throw new Exception(e.getMessage());

				} catch (Exception e) {
					if (logBuf_ != null) {
						XFUtility.appendLog(e.getMessage(), logBuf_);
					}
					throw e;
				}
			}
		}

    	return processRowCount;
    }
//    private int executeOnLocal() throws Exception {
//    	int processRowCount = -1;
//
//		Connection connection = null;
//		Statement statement = null;
//		boolean isExecutePending = true;
//
//		if (!dbID.equals("") && !operation_.toUpperCase().equals("SELECT") && !operation_.toUpperCase().equals("COUNT")) {
//			String message = "Update/Insert/Delete are not permitted to the read-only database.";
//			if (session_.isClientSession()) {
//				JOptionPane.showMessageDialog(null, message);
//			}
//			throw new Exception(message);
//		} else {
//			while (isExecutePending) {
//				try {
//					if (dbID.equals("")) {
//						if (isAutoCommit_) {
//							connection = session_.getConnectionAutoCommit();
//						} else {
//							connection = session_.getConnectionManualCommit();
//						}
//					} else {
//						connection = session_.getConnectionReadOnly(dbID);
//					}
//					if (connection != null) {
//						if (operation_.toUpperCase().equals("SELECT")) {
//							statement = connection.createStatement();
//							ResultSet result = statement.executeQuery(this.getSqlText());
//							relation_ = new Relation(result);
//							processRowCount = relation_.getRowCount();
//							result.close();
//						} else {
//							if (operation_.toUpperCase().equals("COUNT")) {
//								statement = connection.createStatement();
//								ResultSet result = statement.executeQuery(this.getSqlText());
//								if (result.next()) {
//									processRowCount = result.getInt(1);
//								}
//								result.close();
//							} else {
//								statement = connection.createStatement();
//								processRowCount = statement.executeUpdate(this.getSqlText());
//							}
//						}
//						statement.close();
//						isExecutePending = false;
//					}
//				} catch (SQLException e) {
//					if (logBuf_ != null) {
//						XFUtility.appendLog(e.getMessage(), logBuf_);
//					}
//					try {
//						statement.executeQuery("SELECT * FROM " + session_.taxTable);
//						isExecutePending = false;
//						throw new Exception(e.getMessage());
//					} catch (SQLException e1) {
//						if (session_.isClientSession()) {
//							Object[] bts = {XFUtility.RESOURCE.getString("DBConnectMessage1"), XFUtility.RESOURCE.getString("DBConnectMessage2")};
//							int reply = JOptionPane.showOptionDialog(null, XFUtility.RESOURCE.getString("DBConnectMessage3"),
//									XFUtility.RESOURCE.getString("DBConnectMessage4"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[1]);
//							if (reply == 0) {
//								System.exit(0);
//							}
//							if (reply == 1) {
//								boolean isOkay = session_.setupConnectionToDatabase(false);
//								if (isOkay) {
//									isExecutePending = false;
//								}
//							}
//						} else {
//							System.exit(0);
//						}
//					}
//				} catch (OutOfMemoryError e) {
//					if (logBuf_ != null) {
//						XFUtility.appendLog(e.getMessage(), logBuf_);
//					}
//					throw new Exception(e.getMessage());
//				} catch (Exception e) {
//					if (logBuf_ != null) {
//						XFUtility.appendLog(e.getMessage(), logBuf_);
//					}
//					throw e;
//				}
//			}
//		}
//
//    	return processRowCount;
//    }

    private int executeOnServer() throws Exception {
    	int processRowCount = -1;
        HttpPost httpPost = null;
		List<NameValuePair> objValuePairs = null;
		ObjectInputStream inputStream = null;
		HttpClient httpClient = null;

		if (!dbID.equals("") && !operation_.toUpperCase().equals("SELECT") && !operation_.toUpperCase().equals("COUNT")) {
			String message = "Update/Insert/Delete are not permitted to the read-only database.";
			if (session_.isClientSession()) {
				JOptionPane.showMessageDialog(null, message);
			}
			throw new Exception(message);

		} else {
			if (!isAutoCommit_ && session_.getSessionID().equals("")) {
				String message = "Manual-commit transaction is not allowed with blank session ID.";
				if (session_.isClientSession()) {
					JOptionPane.showMessageDialog(null, message);
				}
				throw new Exception(message);

			} else {
				try {
					////////////////////////////////////
					// Loop to retry connecting to DB //
					////////////////////////////////////
					for (;;) {

						////////////////////////////////
						// Setup Objects for HttpPost //
						////////////////////////////////
						httpPost = new HttpPost(session_.getAppServerName());
						if (!isAutoCommit_) {
							objValuePairs = new ArrayList<NameValuePair>(3);
							objValuePairs.add(new BasicNameValuePair("SESSION", session_.getSessionID()));
							objValuePairs.add(new BasicNameValuePair("METHOD", this.getSqlText()));
							objValuePairs.add(new BasicNameValuePair("DB", dbID));

						} else {
							objValuePairs = new ArrayList<NameValuePair>(2);
							objValuePairs.add(new BasicNameValuePair("METHOD", this.getSqlText()));
							objValuePairs.add(new BasicNameValuePair("DB", dbID));
						}
						httpPost.setEntity(new UrlEncodedFormEntity(objValuePairs, "UTF-8"));  
						httpClient = new DefaultHttpClient();

						//////////////////////
						// Execute HttpPost //
						//////////////////////
						try {
							HttpResponse response = httpClient.execute(httpPost);
							HttpEntity responseEntity = response.getEntity();
							if (responseEntity == null) {
								if (logBuf_ != null) {
									XFUtility.appendLog("Response is NULL.", logBuf_);
								}

							} else {
								if (operation_.toUpperCase().equals("SELECT")) {
									inputStream = new ObjectInputStream(responseEntity.getContent());
									relation_ = (Relation)inputStream.readObject();
									processRowCount = relation_.getRowCount();
								} else {
									processRowCount = Integer.parseInt(EntityUtils.toString(responseEntity));
								}
							}
							break;

						} catch(Exception ex) {
							if (session_.isClientSession()) {
								Object[] bts = {XFUtility.RESOURCE.getString("DBConnectMessage1"), XFUtility.RESOURCE.getString("DBConnectMessage2")};
								int reply = JOptionPane.showOptionDialog(null, XFUtility.RESOURCE.getString("DBConnectMessage3"),
										XFUtility.RESOURCE.getString("DBConnectMessage4"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[1]);
								if (reply == 0) {
									System.exit(0);
								}
							} else {
								System.exit(0);
							}
						}
					}

					try {
						if (inputStream != null) {
							inputStream.close();
						}
					} catch(IOException e) {
					}
					if (httpClient != null) {
						httpClient.getConnectionManager().shutdown();
					}

				} catch (Exception e) {
					String message = "HttpClient error with DB server.Å@Session is aborted.";
					if (logBuf_ != null) {
						XFUtility.appendLog(message + "\n" + e.getMessage(), logBuf_);
					}
					throw new Exception(message + "\n" + e.getMessage());

				} finally {
					if (httpPost != null) {
						httpPost.abort();
					}
				}
			}
		}

    	return processRowCount;
    }
//    private int executeOnServer() throws Exception {
//    	int processRowCount = -1;
//
//        HttpPost httpPost = null;
//		List<NameValuePair> objValuePairs = null;
//		ObjectInputStream inputStream = null;
//		HttpClient httpClient = null;
//
//		if (!dbID.equals("") && !operation_.toUpperCase().equals("SELECT") && !operation_.toUpperCase().equals("COUNT")) {
//			String message = "Update/Insert/Delete are not permitted to the read-only database.";
//			JOptionPane.showMessageDialog(null, message);
//			throw new Exception(message);
//
//		} else {
//			if (!isAutoCommit_ && session_.getSessionID().equals("")) {
//				String message = "Manual-commit transaction is not allowed with blank session ID.";
//				JOptionPane.showMessageDialog(null, message);
//				throw new Exception(message);
//
//			} else {
//				try {
//					httpPost = new HttpPost(session_.getAppServerName());
//					if (!isAutoCommit_) {
//						objValuePairs = new ArrayList<NameValuePair>(3);
//						objValuePairs.add(new BasicNameValuePair("SESSION", session_.getSessionID()));
//						objValuePairs.add(new BasicNameValuePair("METHOD", this.getSqlText()));
//						objValuePairs.add(new BasicNameValuePair("DB", dbID));
//
//					} else {
//						objValuePairs = new ArrayList<NameValuePair>(2);
//						objValuePairs.add(new BasicNameValuePair("METHOD", this.getSqlText()));
//						objValuePairs.add(new BasicNameValuePair("DB", dbID));
//					}
//
//					httpPost.setEntity(new UrlEncodedFormEntity(objValuePairs, "UTF-8"));  
//					try {
//						httpClient = new DefaultHttpClient();
//						HttpResponse response = httpClient.execute(httpPost);
//						HttpEntity responseEntity = response.getEntity();
//						if (responseEntity == null) {
//							if (logBuf_ != null) {
//								XFUtility.appendLog("Response is NULL.", logBuf_);
//							}
//
//						} else {
//							if (operation_.toUpperCase().equals("SELECT")) {
//								inputStream = new ObjectInputStream(responseEntity.getContent());
//								relation_ = (Relation)inputStream.readObject();
//								processRowCount = relation_.getRowCount();
//							} else {
//								processRowCount = Integer.parseInt(EntityUtils.toString(responseEntity));
//							}
//						}
//
//					} catch(Exception ex) {
//						throw new Exception();
//
//					} finally {
//						try {
//							if (inputStream != null) {
//								inputStream.close();
//							}
//						} catch(IOException e) {
//						}
//						if (httpClient != null) {
//							httpClient.getConnectionManager().shutdown();
//						}
//					}
//
//				} catch(Exception ex) {
////					String message = "HttpClient error with DB server.Å@Session is aborted.";
////					if (logBuf_ != null) {
////						XFUtility.appendLog(message + "\n" + ex.getMessage(), logBuf_);
////					}
////					throw new Exception(message + "\n" + ex.getMessage());
//					JOptionPane.showMessageDialog(null, "HttpClient error with DB server.Å@Session is aborted.");
//					System.exit(0);
//
//				} finally {
//					if (httpPost != null) {
//						httpPost.abort();
//					}
//				}
//			}
//		}
//
//    	return processRowCount;
//    }
    
    public void resetCursor() {
    	relation_.setRowIndex(-1);
    }
    
    public boolean next() throws Exception {
    	boolean hasNext = false;
		if (operation_.toUpperCase().equals("SELECT")) {
			if (relation_ == null || !hasFoundRecord_) {
				this.execute();
			}
			if (relation_ != null) {
				hasNext = relation_.next();
				if (hasNext) {
					hasFoundRecord_ = true;
				}
			}
		}
    	return hasNext;
    }
    
    public void setCursorAt(int index) {
    	if (relation_ != null) {
    		relation_.setRowIndex(index);
    	}
    }
    
    public boolean previous() {
    	boolean hasPrevious = false;
		if (operation_.toUpperCase().equals("SELECT")) {
			if (relation_ != null) {
				hasPrevious = relation_.previous();
			}
		}
    	return hasPrevious;
    }
    
    public int getRowCount() {
    	return relation_.getRowCount();
    }
    
    public Object getValueOf(String fieldID) throws Exception {
    	Object value = "";
    	if (relation_ != null) {
    		org.w3c.dom.Element workElement = session_.getFieldElement(tableID_, fieldID);
    		if (workElement != null && !workElement.getAttribute("PhysicalID").equals("")) {
    	    	fieldID = workElement.getAttribute("PhysicalID");
    		}
    		value = relation_.getValueOf(fieldID);
    	}
    	if (value == null) {
    		if (value instanceof Blob) {
    		} else {
    			value = "";
    		}
    	} else {
    		if (value instanceof String) {
    			value = value.toString().trim();
    		}
    	}
    	return value;
    }
    
    public boolean hasValueOf(String tableID, String fieldID) {
    	if (tableID.equals(tableID_)) {
    		if (relation_ == null) {
    			return false;
    		} else {
        		org.w3c.dom.Element workElement = session_.getFieldElement(tableID_, fieldID);
        		if (workElement != null && !workElement.getAttribute("PhysicalID").equals("")) {
        	    	fieldID = workElement.getAttribute("PhysicalID");
        		}
    			return relation_.hasValueOf(fieldID);
    		}
    	} else {
    		return false;
    	}
    }
}