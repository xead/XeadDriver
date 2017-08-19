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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import javax.swing.JOptionPane;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

////////////////////////////////////////////////////////////////////
// This is a public class that can be used in Table-Script.       //
// Note that public classes must be defined in its own java file. //
////////////////////////////////////////////////////////////////////
public class XFExcelFileOperator {
    private File file_ = null;
    private Workbook workbook_ = null;
	private boolean isError = false;
	
	public XFExcelFileOperator(String fileName) {
		super();
		file_ = new File(fileName);
		if (file_.exists()) {
			try {
				InputStream is = new FileInputStream(file_);
				workbook_ = WorkbookFactory.create(is);
				is.close();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Failed to process the file:\n"+e.getMessage());
				isError = true;
			}
		} else {
			JOptionPane.showMessageDialog(null, "Specified file does not exists:" + file_.getName());
		}
	}
    
    public boolean commit() {
    	try {
    		OutputStream os = new FileOutputStream(file_);
    		workbook_.write(os);
    	} catch (Exception e) {
    		isError = true;
    	}
    	return isError;
    }

    public int getLastRowIndex(int indexOfSheet) {
    	int num = 0;
    	try {
    		Sheet sheet = workbook_.getSheetAt(indexOfSheet);
    		num = sheet.getLastRowNum();
    	} catch (Exception e) {
    		num = -1;
    	}
		return num;
    }

    public Object getValueAt(int indexOfSheet, int indexOfRow, int indexOfColumn) {
    	Object value = "";
    	try {
			Sheet sheet = workbook_.getSheetAt(indexOfSheet);
			Row row = sheet.getRow(indexOfRow);
			Cell cell = row.getCell(indexOfColumn);
			if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
				value = cell.getStringCellValue();
			}
			if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
				if(DateUtil.isCellDateFormatted(cell)) {
					java.util.Date date = cell.getDateCellValue();

					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);

					String yearStr = Integer.toString(cal.get(Calendar.YEAR));
				    int month = cal.get(Calendar.MONTH) + 1;
				    String monthStr = Integer.toString(month);
				    if (month < 9) {
				    	monthStr = "0" + monthStr;
				    }
				    int day = cal.get(Calendar.DATE);
				    String dayStr = Integer.toString(day);
				    if (day < 9) {
				    	dayStr = "0" + dayStr;
				    }
					value = yearStr + "-" + monthStr + "-" + dayStr;
					
				} else {
					value = cell.getNumericCellValue();
				}
			}
			if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
				value = cell.getBooleanCellValue();
			}
		} catch (Exception e) {}
    	return value;
    }
    
    public boolean setValueAt(int indexOfSheet, int indexOfRow, int indexOfColumn, String value) {
    	boolean isOkay = true;
    	try {
			Sheet sheet = workbook_.getSheetAt(indexOfSheet);
			Row row = sheet.getRow(indexOfRow);
			Cell cell = row.getCell(indexOfColumn);
			int type = cell.getCellType();
			if (type == Cell.CELL_TYPE_NUMERIC) {
				if(DateUtil.isCellDateFormatted(cell)) {
					String date = value.replaceAll("/", "");
					date = value.replaceAll("-", "").trim();
					int year = Integer.parseInt(date.substring(0, 4));
					int month = Integer.parseInt(date.substring(4, 6));
					int day = Integer.parseInt(date.substring(6, 8));
					Calendar cal = Calendar.getInstance();
					cal.set(year, month-1, day);
					cell.setCellValue(cal);
				} else {
					Double doubleValue = Double.parseDouble(value);
					cell.setCellValue(doubleValue);
				}
			} else {
				cell.setCellValue(value);
			}
			cell.setCellType(type);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Failed to process the value:"+ value + "\n" +e.getMessage());
	    	isOkay = false;
		}
    	return isOkay;
    }
}
