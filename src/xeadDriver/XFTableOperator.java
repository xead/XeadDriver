package xeadDriver;

import java.io.ObjectInputStream;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import xeadServer.Relation;

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
			pos1 = sqlText_.toUpperCase().indexOf("FROM ") + 5;
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
    	int count = -1;
		Connection connection = null;
		Statement statement = null;
        //
		if (!dbID.equals("") && !operation_.toUpperCase().equals("SELECT") && !operation_.toUpperCase().equals("COUNT")) {
			//JOptionPane.showMessageDialog(null, "Update/Insert/Delete are not permitted to the read-only database.");
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
						//connection.setReadOnly(true);
						statement = connection.createStatement();
						ResultSet result = statement.executeQuery(this.getSqlText());
						relation_ = new Relation(result);
						count = relation_.getRowCount();
						result.close();
					} else {
						if (operation_.toUpperCase().equals("COUNT")) {
							//connection.setReadOnly(true);
							statement = connection.createStatement();
							ResultSet result = statement.executeQuery(this.getSqlText());
							if (result.next()) {
								count = result.getInt(1);
							}
							result.close();
						} else {
							//connection.setReadOnly(false);
							statement = connection.createStatement();
							count = statement.executeUpdate(this.getSqlText());
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
    	return count;
    }

    private int executeOnServer() throws Exception {
    	int rowCount = -1;
		int retryCount = -1;
        HttpPost httpPost = null;
		List<NameValuePair> objValuePairs = null;
        //
		if (!dbID.equals("") && !operation_.toUpperCase().equals("SELECT") && !operation_.toUpperCase().equals("COUNT")) {
			//JOptionPane.showMessageDialog(null, "Update/Insert/Delete are not permitted to the read-only database.");
		} else {
			if (!isAutoCommit_ && session_.getSessionID().equals("")) {
				throw new Exception("Manual-commit transaction is not allowed with blank session ID.");
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
					while (retryCount < 3) {
						try {
							retryCount++;
							HttpResponse response = session_.getHttpClient().execute(httpPost);
							HttpEntity responseEntity = response.getEntity();
							if (responseEntity == null) {
								if (logBuf_ != null) {
									XFUtility.appendLog("Response is NULL.", logBuf_);
								}
							} else {
								if (operation_.toUpperCase().equals("SELECT")) {
									ObjectInputStream ois = new ObjectInputStream(responseEntity.getContent());
									relation_ = (Relation)ois.readObject();
									rowCount = relation_.getRowCount();
								} else {
									rowCount = Integer.parseInt(EntityUtils.toString(responseEntity));
								}
							}
							retryCount = 3;
						} catch(SocketException ex) {
							if (retryCount < 3) {
								Thread.sleep(1000);
							} else {
								if (logBuf_ != null) {
									XFUtility.appendLog(ex.getMessage(), logBuf_);
								}
								throw new Exception("Communication error with the application server.\n" + ex.getMessage());
							}
						}
					}
				} catch(Exception ex) {
					if (logBuf_ != null) {
						XFUtility.appendLog(ex.getMessage(), logBuf_);
					}
					throw ex;
				} finally {
					if (httpPost != null) {
						httpPost.abort();
					}
				}
			}
		}
		//
    	return rowCount;
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
