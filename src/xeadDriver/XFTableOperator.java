package xeadDriver;

/*
 * Copyright (c) 2012 WATANABE kozo <qyf05466@nifty.com>,
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

////////////////////////////////////////////////////////////////
// This is a public class used in Table-Script.               //
// Note that public classes are defined in its own java file. //
////////////////////////////////////////////////////////////////
public class XFTableOperator {
    private Session session_ = null;
    private Relation relation_ = null;
    private String operation_ = "";
    private String tableID_ = "";
    private String dbID = "";
    private String dbName = "";
    private String selectFields_ = "";
    private String orderBy_ = "";
    private String sqlText_ = "";
    private ArrayList<String> fieldIDList_ = new ArrayList<String>();
    private ArrayList<Object> fieldValueList_ = new ArrayList<Object>();
    private ArrayList<String> withKeyList_ = new ArrayList<String>();
    private StringBuffer logBuf_ = null;
    private boolean hasFoundRecord_ = false;
    private boolean isAutoCommit_ = false;

	public XFTableOperator(Session session, StringBuffer logBuf, String operation, String tableID) {
		this(session, logBuf, operation, tableID, false);
	}
	
	public XFTableOperator(Session session, StringBuffer logBuf, String operation, String tableID, boolean isAutoCommit) {
		super();
		session_ = session;
		logBuf_ = logBuf;
        operation_ = operation;
    	tableID_ = tableID;
    	isAutoCommit_ = isAutoCommit;
    	//
		dbID = session_.getTableElement(tableID_).getAttribute("DB");
		if (dbID.equals("")) {
			dbName = session_.getDatabaseName();
		} else {
			dbName = session_.getSubDBName(dbID);
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
		//
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
    	//
		dbID = session_.getTableElement(tableID_).getAttribute("DB");
		if (dbID.equals("")) {
			dbName = session_.getDatabaseName();
		} else {
			dbName = session_.getSubDBName(dbID);
		}
		//
		if (dbName.contains("jdbc:mysql:")) {
			sqlText_ = sqlText_.replaceAll("\\\\", "\\\\\\\\");
		}
	}

    public void setSelectFields(String fields) {
    	selectFields_ = fields;
    }

    public void addValue(String fieldID, Object value) {
    	int index = fieldIDList_.indexOf(fieldID);
    	if (index > -1) {
    		fieldIDList_.remove(index);
    		fieldValueList_.remove(index);
    	}
    	//
    	fieldIDList_.add(fieldID);
    	if (value.toString().trim().startsWith("'") && value.toString().trim().endsWith("'")) {
    		fieldValueList_.add(value);
    	} else {
    		org.w3c.dom.Element workElement = session_.getFieldElement(tableID_, fieldID);
    		if (workElement == null) { //UPDCOUNTER //
				fieldValueList_.add(value);
    		} else {
    			String basicType = XFUtility.getBasicTypeOf(workElement.getAttribute("Type"));
    			if (basicType.equals("DATETIME") && value.equals("CURRENT_TIMESTAMP")) {
    				fieldValueList_.add(value);
    			} else {
    				if (XFUtility.isLiteralRequiredBasicType(basicType)) {
    					fieldValueList_.add("'" + value + "'");
    				} else {
    					fieldValueList_.add(value);
    				}
    			}
    		}
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
    	//
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
    			String basicType = XFUtility.getBasicTypeOf(workElement.getAttribute("Type"));
    			if (XFUtility.isLiteralRequiredBasicType(basicType)) {
    				withKeyList_.add(fieldID_ + operand_ + "'" + value + "'");
    			} else {
    				withKeyList_.add(fieldID_ + operand_ + value);
    			}
    		}
		}
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
		//
		if (value.toString().trim().startsWith("'") && value.toString().trim().endsWith("'")) {
	    	withKeyList_.add(prefix + " " + fieldID_ + operand_ + value + " " + postfix);
		} else {
			org.w3c.dom.Element workElement = session_.getFieldElement(tableID_, fieldID_);
    		if (workElement == null) { //UPDCOUNTER//
				withKeyList_.add(prefix + " " + fieldID_ + operand_ + value + " " + postfix);
    		} else {
    			String basicType = XFUtility.getBasicTypeOf(workElement.getAttribute("Type"));
    			if (XFUtility.isLiteralRequiredBasicType(basicType)) {
    				withKeyList_.add(prefix + " " + fieldID_ + operand_ + "'" + value + "' " + postfix);
    			} else {
    				withKeyList_.add(prefix + " " + fieldID_ + operand_ + value + " " + postfix);
    			}
    		}
		}
    }

    public void setOrderBy(String text) {
    	orderBy_ = text;
    }

    public void setSqlText(String text) {
    	sqlText_ = text;
    }
    
    public String getSqlText() {
        if (sqlText_.equals("")) {
        	//
            StringBuffer bf = new StringBuffer();
            //
        	if (operation_.toUpperCase().equals("SELECT")) {
        		bf.append("select ");
        		if (selectFields_.equals("")) {
        			bf.append("*");
        		} else {
        			bf.append(selectFields_);
        		}
        		bf.append(" from ");
        		bf.append(tableID_);
        		if (withKeyList_.size() > 0) {
        			bf.append(" where ");
        			for (int i = 0; i < withKeyList_.size(); i++) {
        				bf.append(withKeyList_.get(i));
        			}
        		}
        		if (!orderBy_.equals("")) {
        			bf.append(" order by ");
        			bf.append(orderBy_);
        		}
        	}
        	//
        	if (operation_.toUpperCase().equals("COUNT")) {
        		bf.append("select count(*) from ");
        		bf.append(tableID_);
        		if (withKeyList_.size() > 0) {
        			bf.append(" where ");
        			for (int i = 0; i < withKeyList_.size(); i++) {
        				bf.append(withKeyList_.get(i));
        			}
        		}
        	}
        	//
        	if (operation_.toUpperCase().equals("INSERT")) {
        		bf.append(operation_);
        		bf.append(" ");
        		bf.append(" into ");
        		bf.append(tableID_);
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
        	//
        	if (operation_.toUpperCase().equals("UPDATE")) {
        		bf.append(operation_);
        		bf.append(" ");
        		bf.append(tableID_);
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
        	}
        	//
        	if (operation_.toUpperCase().equals("DELETE")) {
        		bf.append(operation_);
        		bf.append(" ");
        		bf.append(" from ");
        		bf.append(tableID_);
        		if (withKeyList_.size() > 0) {
        			bf.append(" where ");
        			for (int i = 0; i < withKeyList_.size(); i++) {
        				bf.append(withKeyList_.get(i));
        			}
        		}
        	}
        	//
        	sqlText_ = bf.toString();
    		if (dbName.contains("jdbc:mysql:")) {
    			sqlText_ = sqlText_.replaceAll("\\\\", "\\\\\\\\");
    		}
        }
        //
        return sqlText_;
    }
    
    public int execute() throws Exception {
    	int count = -1;
        //
        if (logBuf_ != null) {
        	XFUtility.appendLog(this.getSqlText(), logBuf_);
        }
    	//
		if (session_.getAppServerName().equals("")) {
			count = executeOnLocal();
		} else {
			count = executeOnServer();
		}
		//
    	return count;
    }
    
    private int executeOnLocal() throws Exception {
    	int processRowCount = -1;
    	//
		Connection connection = null;
		Statement statement = null;
        //
		if (!dbID.equals("") && !operation_.toUpperCase().equals("SELECT") && !operation_.toUpperCase().equals("COUNT")) {
			String message = "Update/Insert/Delete are not permitted to the read-only database.";
			JOptionPane.showMessageDialog(null, message);
			throw new Exception(message);
		} else {
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
				}
			} catch (SQLException e) {
				if (logBuf_ != null) {
					XFUtility.appendLog(e.getMessage(), logBuf_);
				}
				throw new Exception(e.getMessage());
			}
		}
		//
    	return processRowCount;
    }

    private int executeOnServer() throws Exception {
    	int processRowCount = -1;
    	//
        HttpPost httpPost = null;
		List<NameValuePair> objValuePairs = null;
		ObjectInputStream inputStream = null;
		HttpClient httpClient = null;
        //
		if (!dbID.equals("") && !operation_.toUpperCase().equals("SELECT") && !operation_.toUpperCase().equals("COUNT")) {
			String message = "Update/Insert/Delete are not permitted to the read-only database.";
			JOptionPane.showMessageDialog(null, message);
			throw new Exception(message);
		} else {
			if (!isAutoCommit_ && session_.getSessionID().equals("")) {
				String message = "Manual-commit transaction is not allowed with blank session ID.";
				JOptionPane.showMessageDialog(null, message);
				throw new Exception(message);
			} else {
				try {
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
					//
					try {
						httpClient = new DefaultHttpClient();
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
					} catch(Exception ex) {
						String message = "HttpClient error with the application server.\n" + ex.getMessage();
						if (logBuf_ != null) {
							XFUtility.appendLog(message, logBuf_);
						}
						throw new Exception(message);
					} finally {
						try {
							if (inputStream != null) {
								inputStream.close();
							}
						} catch(IOException e) {
						}
						if (httpClient != null) {
							httpClient.getConnectionManager().shutdown();
						}
					}
				} catch(Exception ex) {
					String message = "HttpPost error with the application server.\n" + ex.getMessage();
					if (logBuf_ != null) {
						XFUtility.appendLog(message, logBuf_);
					}
					throw new Exception(message);
				} finally {
					if (httpPost != null) {
						httpPost.abort();
					}
				}
			}
		}
		//
    	return processRowCount;
    }
    
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
    
    public boolean previous() {
    	boolean hasPrevious = false;
		if (operation_.toUpperCase().equals("SELECT")) {
			if (relation_ != null) {
				hasPrevious = relation_.previous();
			}
		}
    	return hasPrevious;
    }
    
    public Object getValueOf(String fieldID) throws Exception {
    	Object value = null;
    	if (relation_ != null) {
    		value = relation_.getValueOf(fieldID);
    	}
    	return value;
    }
    
    public boolean hasValueOf(String tableID, String fieldID) {
    	if (tableID.equals(tableID_)) {
    		if (relation_ == null) {
    			return false;
    		} else {
    			return relation_.hasValueOf(fieldID);
    		}
    	} else {
    		return false;
    	}
    }
}
