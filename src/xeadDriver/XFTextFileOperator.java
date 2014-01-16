package xeadDriver;

/*
 * Copyright (c) 2014 WATANABE kozo <qyf05466@nifty.com>,
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

////////////////////////////////////////////////////////////////////
// This is a public class that can be used in Table-Script.       //
// Note that public classes must be defined in its own java file. //
////////////////////////////////////////////////////////////////////
public class XFTextFileOperator {
    private String operation_ = "";
    private String fileName_ = "";
    private String separator_ = "";
    private String charset_ = "";
    private ArrayList<String> rowList = new ArrayList<String>();
    private ArrayList<Object> columnList = new ArrayList<Object>();
    private int cursorPos = -1;
	private FileInputStream inputFile = null;
	private InputStreamReader reader = null;
	private FileOutputStream outputFile = null;
	private OutputStreamWriter writer = null;
	private boolean isExecuted = false;
	
	public XFTextFileOperator(String operation, String fileName, String separator, String charset) {
		super();

		operation_ = operation;
		fileName_ = fileName;
		separator_ = separator;
		charset_ = charset;
		if (charset_.equals("")) {
            if (operation_.equals("Read")) {
            	charset_ = "JISAutoDetect";
            }
            if (operation_.equals("Write")) {
            	charset_ = "UTF-8";
            }
        }

		try {
			if (operation_.equals("Read")) {
				inputFile = new FileInputStream(fileName_);
				reader = new InputStreamReader(inputFile, charset_);
			}
			if (operation_.equals("Write")) {
				outputFile = new FileOutputStream(fileName_);
				writer = new OutputStreamWriter(outputFile, charset_);
			}
		} catch (Exception e) {
		}
	}
    
    public int execute() throws Exception {
    	int count = 0;
    	if (operation_.equals("Read")) {
			BufferedReader bfReader = new BufferedReader(reader);
    		try {
    			String line;
    			while ((line = bfReader.readLine()) != null) {
    				rowList.add(line);
    			}
    			count = rowList.size();
    		} catch(Exception e) {
    			count = -1;
    		} finally {
    			bfReader.close();
    			inputFile.close();
    		}        
    	}
    	if (operation_.equals("Write")) {
    		BufferedWriter bfWriter = new BufferedWriter(writer);
    		try {
    			for (int i = 0; i < rowList.size(); i++) {
    				bfWriter.write(rowList.get(i));
    				bfWriter.write("\n");
    			}
    			count = rowList.size();
    		} catch(Exception e) {
    			count = -1;
    		} finally {
    			bfWriter.close();
    			outputFile.close();
    		}        
    	}
    	isExecuted = true;
    	return count;
    }
    
    public boolean next() throws Exception {
    	if (!isExecuted) {
    		execute();
    		cursorPos = 0;
    	} else {
    		cursorPos++;
    	}
       	return isValidCursorPos();
    }
    
    public int getRowIndex() {
    	return cursorPos;
    }
    
    public boolean previous() throws Exception {
    	if (!isExecuted) {
    		execute();
    		cursorPos = -1;
    	} else {
    		cursorPos--;
    	}
       	return isValidCursorPos();
    }

    private boolean isValidCursorPos() {
    	if (operation_.equals("Read")) {
    		columnList.clear();
    		boolean hasNext = false;
    		if (cursorPos >= 0 && cursorPos < rowList.size()) {
    			hasNext = true;
    			columnList = processRowIntoColumnList(rowList.get(cursorPos));
    		}
    		return hasNext;
    	} else {
    		return false;
    	}
    }

    private ArrayList<Object> processRowIntoColumnList(String rowData) {
    	ArrayList<Object> array = new ArrayList<Object>();
    	if (separator_.equals("")) {
    		array.add(rowData);
    	} else {
    		StringTokenizer tokenizer = new StringTokenizer(rowData, separator_);
    		while (tokenizer.hasMoreTokens()) {
        		array.add(tokenizer.nextToken());
    		}
    	}
    	return array;
    }

    public int getColumnCount() {
    	return columnList.size();
    }
    
    public Object getValueAt(int index) {
    	Object value = "";
    	if (index >= 0 && index < columnList.size() ) {
    		value = columnList.get(index);
    	}
    	return value;
    }
    
    public int addValue(Object value) {
    	columnList.add(value);
    	return columnList.size()-1;
    }
    
    public int addRow() {
    	StringBuffer bf = new StringBuffer();
		for (int i = 0; i < columnList.size(); i++) {
			if (i > 0) {
				bf.append(separator_);
			}
			bf.append(columnList.get(i));
		}
    	rowList.add(bf.toString());
    	columnList.clear();
    	return rowList.size()-1;
    }
}
