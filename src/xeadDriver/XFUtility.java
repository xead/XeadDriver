package xeadDriver;

/*
 * Copyright (c) 2018 WATANABE kozo <qyf05466@nifty.com>,
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.*;
import java.awt.im.InputContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.sql.Blob;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.*;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.Barcode39;
import com.lowagie.text.pdf.BarcodeCodabar;
import com.lowagie.text.pdf.BarcodeEAN;
import com.lowagie.text.pdf.BarcodePDF417;
import com.lowagie.text.pdf.PdfContentByte;

public class XFUtility {
	public static final ResourceBundle RESOURCE = ResourceBundle.getBundle("xeadDriver.Res");

	public static final int FIELD_UNIT_HEIGHT = 25;
	public static final int FIELD_HORIZONTAL_MARGIN = 1;
	public static final int FIELD_VERTICAL_MARGIN = 5;
	public static final int DEFAULT_LABEL_WIDTH = 150;
	public static final int ROW_UNIT_HEIGHT = 25;
	public static final int ROW_UNIT_HEIGHT_EDITABLE = 29;
	public static final int SEQUENCE_WIDTH = 45;
	public static final int FONT_SIZE = 18;
	
	public static final String DEFAULT_UPDATE_COUNTER = "UPDCOUNTER";

	public static final Color ERROR_COLOR = new Color(238,187,203);
	public static final Color ACTIVE_COLOR = SystemColor.white;
	public static final Color INACTIVE_COLOR = SystemColor.control;
	public static final Color ODD_ROW_COLOR = new Color(231, 231, 255);
	public static final Color SELECTED_ACTIVE_COLOR = new Color(49,106,197);

	public static final ImageIcon ICON_CHECK_0A = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck0A.PNG")));
	public static final ImageIcon ICON_CHECK_1A = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck1A.PNG")));
	public static final ImageIcon ICON_CHECK_0D = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck0D.PNG")));
	public static final ImageIcon ICON_CHECK_1D = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck1D.PNG")));
	public static final ImageIcon ICON_CHECK_0R = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck0R.PNG")));
	public static final ImageIcon ICON_CHECK_1R = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck1R.PNG")));
	public static final ImageIcon ICON_NOT_AVAILABLE = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iNotAvailable.PNG")));
	public static final ImageIcon ICON_REFRESH = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("refresh.PNG")));

	public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");
	public static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("#,##0");
	public static final DecimalFormat FLOAT_FORMAT0 = new DecimalFormat("#,##0");
	public static final DecimalFormat FLOAT_FORMAT1 = new DecimalFormat("#,##0.0");
	public static final DecimalFormat FLOAT_FORMAT2 = new DecimalFormat("#,##0.00");
	public static final DecimalFormat FLOAT_FORMAT3 = new DecimalFormat("#,##0.000");
	public static final DecimalFormat FLOAT_FORMAT4 = new DecimalFormat("#,##0.0000");
	public static final DecimalFormat FLOAT_FORMAT5 = new DecimalFormat("#,##0.00000");
	public static final DecimalFormat FLOAT_FORMAT6 = new DecimalFormat("#,##0.000000");
	public static final DecimalFormat FLOAT_FORMAT7 = new DecimalFormat("#,##0.0000000");
	public static final DecimalFormat FLOAT_FORMAT8 = new DecimalFormat("#,##0.00000000");
	public static final DecimalFormat FLOAT_FORMAT9 = new DecimalFormat("#,##0.000000000");

	private static final String DRIVER_DERBY = "org.apache.derby.jdbc.ClientDriver";
	private static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
	private static final String DRIVER_POSTGRESQL = "org.postgresql.Driver";
	private static final String DRIVER_ORACLE = "oracle.jdbc.driver.OracleDriver";
	private static final String DRIVER_SQLSERVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static final String DRIVER_H2 = "org.h2.Driver";
	private static final String DRIVER_ACCESS = "net.ucanaccess.jdbc.UcanaccessDriver";
	
	static String getStringNumber(String text) {
		char[] numberDigit = {'-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.'};
		boolean charValidated = false;
		String numberString = "";
		if (text != null) {
			if (text.contains("E")) {
				/////////////
				// IEEE754 //
				/////////////
				try {
					double doubleValue = Double.parseDouble(text);
					numberString = new java.text.DecimalFormat("####0.0#############################").format(doubleValue);
				} catch (NumberFormatException e) {
					numberString = "0";
				}
			} else {
				for (int i = 0; i < text.length(); i++) {
					charValidated = false;
					for (int j = 0; j < numberDigit.length; j++) {
						if (text.charAt(i) == numberDigit[j]) {
							charValidated = true;
							break;
						}
					}
					if (charValidated) {
						numberString = numberString + text.charAt(i);
					}
				}
			}
		}
		return numberString;
	}
	
	static String getFormattedIntegerValue(String value, ArrayList<String> dataTypeOptionList, int size) {
		String wrkValue, returnValue = "";

		int pos = value.indexOf(".");
		if (pos >= 0) {
			wrkValue = value.substring(0, pos);
		} else {
			wrkValue = value;
		}
		wrkValue = wrkValue.replace("-", "").replace("%", "");

		if (dataTypeOptionList.contains("NO_EDIT")) {
			StringBuffer bf = new StringBuffer();
			int intValue = Integer.parseInt(wrkValue);
			if (intValue == 0) {
				for (int i = 0; i < size; i++) {
					bf.append("0");
				}
				returnValue = bf.toString();
				
			} else {
				if (value.startsWith("-")) {
					bf.append("-");
				}
				int extra = size - wrkValue.length();
				for (int i = 0; i < extra; i++) {
					bf.append("0");
				}
				bf.append(wrkValue);
				if (value.endsWith("-")) {
					bf.append("-");
				}
				returnValue = bf.toString();
			}

		} else {
			if (dataTypeOptionList.contains("ZERO_SUPPRESS")) {
				StringBuffer bf = new StringBuffer();
				if (value.startsWith("-")) {
					bf.append("-");
				}
				bf.append(wrkValue);
				if (value.endsWith("-")) {
					bf.append("-");
				}
				returnValue = bf.toString();
			} else {
				if (dataTypeOptionList.contains("HH_MM")) {
					String hourStr = "00";
					String minuiteStr = "00";
					int wrkInt = Integer.parseInt(wrkValue);
					int hour = wrkInt / 100;
					if (hour > 9) {
						hourStr = Integer.toString(hour);
					} else {
						hourStr = "0" + Integer.toString(hour);
					}
					if (wrkValue.length() == 1) {
						minuiteStr = "0" + wrkValue;
					}
					if (wrkValue.length() == 2) {
						minuiteStr = wrkValue;
					}
					if (wrkValue.length() >= 3) {
						minuiteStr = wrkValue.substring(wrkValue.length()-2, wrkValue.length());
					}
					returnValue = hourStr + ":" + minuiteStr;
				} else {
					StringBuffer bf = new StringBuffer();
					if (value.startsWith("-")) {
						bf.append("-");
					}
					if (!wrkValue.equals("")) {
						bf.append(XFUtility.INTEGER_FORMAT.format(Long.parseLong(wrkValue)));
					}
					if (value.endsWith("-")) {
						bf.append("-");
					}
					if (dataTypeOptionList.contains("PERCENT")) {
						returnValue = bf.toString() + "%";
					} else {
						returnValue = bf.toString();
					}
				}
			}
		}

		return returnValue;
	}

	static String getFormattedFloatValue(String value, int decimalSize) {
		String returnValue = "";
		double doubleWrk;

		try {
			String wrkStr = value.toString();
			wrkStr = wrkStr.replace("-", "");
			doubleWrk = Double.parseDouble(wrkStr);
		} catch (NumberFormatException e) {
			doubleWrk = 0;
		}

		StringBuffer bf = new StringBuffer();
		if (value.startsWith("-")) {
			bf.append("-");
		}

		if (decimalSize == 0) {
			bf.append(XFUtility.FLOAT_FORMAT0.format(doubleWrk));
		}
		if (decimalSize == 1) {
			bf.append(XFUtility.FLOAT_FORMAT1.format(doubleWrk));
		}
		if (decimalSize == 2) {
			bf.append(XFUtility.FLOAT_FORMAT2.format(doubleWrk));
		}
		if (decimalSize == 3) {
			bf.append(XFUtility.FLOAT_FORMAT3.format(doubleWrk));
		}
		if (decimalSize == 4) {
			bf.append(XFUtility.FLOAT_FORMAT4.format(doubleWrk));
		}
		if (decimalSize == 5) {
			bf.append(XFUtility.FLOAT_FORMAT5.format(doubleWrk));
		}
		if (decimalSize == 6) {
			bf.append(XFUtility.FLOAT_FORMAT6.format(doubleWrk));
		}
		if (decimalSize == 7) {
			bf.append(XFUtility.FLOAT_FORMAT7.format(doubleWrk));
		}
		if (decimalSize == 8) {
			bf.append(XFUtility.FLOAT_FORMAT8.format(doubleWrk));
		}
		if (decimalSize == 9) {
			bf.append(XFUtility.FLOAT_FORMAT9.format(doubleWrk));
		}

		if (value.endsWith("-")) {
			bf.append("-");
		}

		returnValue = bf.toString();

		return returnValue;
	}

	
	public static Object getNullValueOfBasicType(String basicType){
		Object value = null;
		if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
			value = 0;
		}
		if (basicType.equals("STRING") || basicType.equals("DATETIME") || basicType.equals("TIME") || basicType.equals("DATE")) {
			value = "";
		}
//		if (basicType.equals("BYTEA")) {
//			value = new XFByteArray(null);
//		}
		return value;
	}
	
	public static boolean isNullValue(String basicType, Object value){
		boolean isNull = false;
		if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
			String strValue = value.toString();
			if (strValue == null || strValue.equals("") || strValue.equals("0") || strValue.equals("0.0")) {
				isNull = true;
			}
		}
		if (basicType.equals("STRING") || basicType.equals("DATETIME") || basicType.equals("TIME")) {
			String strValue = value.toString();
			if (strValue == null || strValue.equals("")) {
				isNull = true;
			}
		}
//		if (basicType.equals("DATE")) {
//			String strValue = value.toString();
//			if (strValue == null || strValue.equals("")) {
//				isNull = true;
//			}
//		}
		if (basicType.equals("DATE")) {
			if (value == null) {
				isNull = true;
			} else {
				String strValue = value.toString();
				if (strValue.equals("")) {
					isNull = true;
				}
			}
		}
		if (basicType.equals("BYTEA")) {
			if (value == null) {
				isNull = true;
			} else {
//				XFByteArray byteArray = (XFByteArray)value;
//				if (byteArray.getInternalValue() == null || byteArray.getInternalValue().equals("")) {
//					isNull = true;
//				}
				if (value instanceof byte[]) {
					isNull = false;
				} else {
					isNull = true;
				}
			}
		}
		return isNull;
	}

	static boolean isStaticRefer(String tableID, org.w3c.dom.Element referElement) {
		boolean reply = true;
		String wrkStr;
		StringTokenizer workTokenizer1, workTokenizer2;
		workTokenizer1 = new StringTokenizer(referElement.getAttribute("WithKeyFields"), ";" );
		while (workTokenizer1.hasMoreTokens()) {
			wrkStr = workTokenizer1.nextToken();
			workTokenizer2 = new StringTokenizer(wrkStr, "." );
			if (!workTokenizer2.nextToken().equals(tableID)) {
				reply = false;
				break;
			}
		}
		return reply;
	}

	public static ArrayList<String> getDSNameListInScriptText(String scriptText, NodeList tableList) {
		ArrayList<String> fieldList = new ArrayList<String>();
		int pos, posWrk, wrkInt, posWrk2;
		String[] sectionDigit = {"(", ")", "{", "}", "+", "-", "/", "*", "=", "<", ">", ";", "|", "&", "\n", "\t", ",", " ", "!"};
		String[] fieldProperty = {"value", "oldValue", "color", "enabled", "editable", "error", "valueChanged"};
		boolean isFirstDigitOfField;
		String variantExpression, wrkStr1, wrkStr2, dataSource;
		org.w3c.dom.Element element;

		for (int i = 0; i < fieldProperty.length; i++) {
			pos = 0;
			while (pos < scriptText.length()) {
				posWrk = scriptText.indexOf("." + fieldProperty[i], pos);
				if (posWrk == -1) {
					pos = scriptText.length();
				}

				if (posWrk != -1) {
					wrkInt = posWrk - 1;
					while (wrkInt > -1) {
						wrkInt--;
						isFirstDigitOfField = false;
						if (wrkInt > -1) {
							wrkStr1 = scriptText.substring(wrkInt, wrkInt+1);
							for (int j = 0; j < sectionDigit.length; j++) {
								if (wrkStr1.equals(sectionDigit[j])) {
									isFirstDigitOfField = true;
									break;
								}
							}
						}
						if (wrkInt == -1 || isFirstDigitOfField) {
							dataSource = "";
							variantExpression = scriptText.substring(wrkInt + 1, posWrk);
							if (countStringInText(variantExpression, "_") > 1) {
								wrkStr2 = "";
								for (int j = 0; j < tableList.getLength(); j++) {
									element = (org.w3c.dom.Element)tableList.item(j);
									wrkStr1 = element.getAttribute("ID") + "_";
									if (variantExpression.startsWith(wrkStr1)) {
										if (wrkStr1.length() > wrkStr2.length()+1) {
											wrkStr2 = element.getAttribute("ID");
										}
									}
								}
								if (!wrkStr2.equals("")) {
									dataSource = variantExpression.replaceFirst(wrkStr2+"_", wrkStr2+".");
									if (!fieldList.contains(dataSource)) {
										fieldList.add(dataSource);
									}
								}
							}
							if (dataSource.equals("")) {
								posWrk2 = variantExpression.indexOf("_");
								if (posWrk2 > -1) {
									dataSource = variantExpression.substring(0, posWrk2) + "." + variantExpression.substring(posWrk2 + 1, variantExpression.length());
									if (!fieldList.contains(dataSource)) {
										fieldList.add(dataSource);
									}
								}
							}
							wrkInt = -1;
							pos = posWrk + 6;
						}
					}
				}
			}
		}
		return fieldList;
	}
	
	static public int countStringInText(String text, String searchString) {
        return (text.length() - text.replaceAll(searchString, "").length()) / searchString.length();
    }	
	
	static public int countNumberOfDisplayedFilters(NodeList filterFieldList) {
		int count = 0;
		String fieldOptions = "";
		ArrayList<String> fieldOptionList;
		org.w3c.dom.Element element;
		for (int i = 0; i < filterFieldList.getLength(); i++) {
			element = (org.w3c.dom.Element)filterFieldList.item(i);
			fieldOptions = element.getAttribute("FieldOptions");
			fieldOptionList = XFUtility.getOptionList(fieldOptions);
			if (!fieldOptionList.contains("HIDDEN")) {
				count++;
			}
		}
		return count;
	}

	static ImageIcon createSmallIcon(String fileName, int iconWidth, int iconHeight) {
		ImageIcon icon = new ImageIcon();
		BufferedImage image = null;
		try{
			//////////////////////////////////////////////////
			// Setup buffered image data with its file name //
			//////////////////////////////////////////////////
			if (fileName.startsWith("http://")) {
				fileName = fileName.replace("\\", "/");
				URL url = new URL(fileName);
				image = ImageIO.read(url);
			} else {
				if (fileName.toUpperCase().contains(".GIF")
						|| fileName.toUpperCase().contains(".BMP")
						|| fileName.toUpperCase().contains(".JPEG")
						|| fileName.toUpperCase().contains(".JPG")
						|| fileName.toUpperCase().contains(".JPE")
						|| fileName.toUpperCase().contains(".PNG")) {
					File imageFile = new File(fileName);
					if (imageFile.exists()) {
						image = ImageIO.read(imageFile);
					}
				}
			}
			/////////////////////////////////////////////////////
			// Setup small icon image with buffered image data //
			/////////////////////////////////////////////////////
			if (image == null) {
				icon = XFUtility.ICON_NOT_AVAILABLE;
			} else {
				float rate = 0;
				float rateWidth = (float)iconWidth / image.getWidth();
				float rateHeight = (float)iconHeight / image.getHeight();
				if (rateWidth > rateHeight) {
					rate = rateHeight;
				} else {
					rate = rateWidth;
				}
				int width = Math.round(image.getWidth()*rate);
				int height = Math.round(image.getHeight()*rate);
				BufferedImage shrinkImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				Graphics2D g2d = shrinkImage.createGraphics();
				g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
				g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
				g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);													g2d.drawImage(image, 0, 0, width, height, null);
				icon.setImage(shrinkImage);
			}
		}catch(Exception e){
		}
		return icon;
	}
	
	public static String removeCommentsFromScriptText(String scriptText) {
		StringBuffer buf = new StringBuffer();
		int posCommentStart;
		int posCommentEnd = 0;
		int pos = 0;
		while (pos < scriptText.length()) {
			posCommentStart = scriptText.indexOf("//", pos);
			if (posCommentStart == -1) {
				posCommentStart = scriptText.indexOf("/*", pos);
				if (posCommentStart == -1) {
					posCommentStart = scriptText.length();
					posCommentEnd = scriptText.length();
				} else {
					posCommentEnd = scriptText.indexOf("*/", posCommentStart);
					if (posCommentEnd == -1) {
						posCommentEnd = scriptText.length();
					} else {
						posCommentEnd = posCommentEnd + 2;
					}
				}
			} else {
				posCommentEnd = scriptText.indexOf("\n", posCommentStart);
				if (posCommentEnd == -1) {
					posCommentEnd = scriptText.length();
				} else {
					posCommentEnd = posCommentEnd + 1;
				}
			}
			buf.append(scriptText.substring(pos, posCommentStart));
			pos = posCommentEnd;
		}
		return buf.toString();
	}
	

	static Object parseObjectAccordingToType(Object value, String basicType) {
		Object returnValue = "";
		if (basicType.equals("INTEGER")) {
			if (value != null && !value.toString().equals("")) {
				if (value.equals("*Auto")) {
					returnValue = value;
				} else {
					returnValue = Long.parseLong(value.toString());
				}
			} else {
				returnValue = 0;
			}
		} else {
			if (basicType.equals("FLOAT")) {
				if (value != null && !value.toString().equals("")) {
					returnValue = Double.parseDouble(value.toString());
				} else {
					returnValue = 0.0f;
				}
			} else {
				if (value != null) {
					if (basicType.equals("STRING")) {
						value = value.toString().trim();
					}
					returnValue = value;
				}
			}
		}
		return returnValue;
	}
	
	///////////////////////////////////////////////////////
	// Loading Class of Database Driver                  //
	// This step is required for Tomcat7 not for Tomcat6 //
	///////////////////////////////////////////////////////
	public static void loadDriverClass(String dbName) throws ClassNotFoundException {
		if (dbName.contains("derby:")) {
			Class.forName(DRIVER_DERBY);
			return;
		}
		if (dbName.contains("mysql:")) {
			Class.forName(DRIVER_MYSQL);
			return;
		}
		if (dbName.contains("postgresql:")) {
			Class.forName(DRIVER_POSTGRESQL);
			return;
		}
		if (dbName.contains("oracle:")) {
			Class.forName(DRIVER_ORACLE);
			return;
		}
		if (dbName.contains("sqlserver:")) {
			Class.forName(DRIVER_SQLSERVER);
			return;
		}
		if (dbName.contains("h2:")) {
			Class.forName(DRIVER_H2);
			return;
		}
		if (dbName.contains("ucanaccess:")) {
			Class.forName(DRIVER_ACCESS);
			return;
		}
	}

	public static ArrayList<String> getOptionList(String options) {
		ArrayList<String> typeOptionList = new ArrayList<String>();

		int index = options.indexOf("VALUES(");
		if (index > -1) {
			int index2 = options.indexOf(")", index);
			if (index2 > -1) {
				String wrkStr = options.substring(index, index2+1);
				typeOptionList.add(wrkStr);
				options.replace(wrkStr+",", "").replace(wrkStr, "");
			}
		}
		
		StringTokenizer workTokenizer = new StringTokenizer(options, ",");
		while (workTokenizer.hasMoreTokens()) {
			typeOptionList.add(workTokenizer.nextToken());
		}

		return typeOptionList;
	}
	
	public static String getOptionValueWithKeyword(String options, String keyword) {
		String value = "";
		int pos1, pos2;
		if (!keyword.equals("")) {
			int lengthOfKeyword = keyword.length() + 1;
			ArrayList<String> typeOptionList = getOptionList(options);
			for (int i = 0; i < typeOptionList.size(); i++) {
				pos1 = typeOptionList.get(i).indexOf(keyword + "(");
				//if (pos1 > -1) {
				if (pos1 == 0) {
					pos2 = typeOptionList.get(i).length() - 1;
					value = typeOptionList.get(i).substring(pos1 + lengthOfKeyword, pos2);
					break;
				}
			}
		}
		return value;
	}
	
	static String getDefaultValueOfFilterField(String keywordValue, Session session){
		String defaultValue = null;

		StringTokenizer workTokenizer = new StringTokenizer(keywordValue, ":" );
		if (workTokenizer.countTokens() == 1) {
			defaultValue = workTokenizer.nextToken().trim();
		}
		if (workTokenizer.countTokens() == 2) {

			String keyword = workTokenizer.nextToken();
			String value = workTokenizer.nextToken().trim();

			if (keyword.equals("VALUE")) {
				defaultValue = value;
			}
			if (keyword.equals("USER_ATTRIBUTE")) {
				if (value.equals("ID")) {
					defaultValue = session.getUserID();
				}
				if (value.equals("Name")) {
					defaultValue = session.getUserName();
				}
				if (value.equals("EmployeeNo")) {
					defaultValue = session.getUserEmployeeNo();
				}
			}
			/////////////////////////////////////////////////////////////
			// Note that if attribute not found, null will be returned //
			/////////////////////////////////////////////////////////////
			if (keyword.equals("SESSION_ATTRIBUTE")) {
				defaultValue = session.getAttribute(value);
			}
		}
		return defaultValue;
	}
	
	static String getCaptionValue(String keywordValue, Session session){
		String captionValue = keywordValue;
		StringTokenizer workTokenizer = new StringTokenizer(keywordValue, ":" );
		if (workTokenizer.countTokens() == 2) {
			String keyword = workTokenizer.nextToken();
			String value = workTokenizer.nextToken().trim();
			if (keyword.equals("SESSION_ATTRIBUTE")) {
				captionValue = session.getAttribute(value);
			}
		}
		return captionValue;
	}
	
	static String getFixedWhereValue(String keywordValue, Session session){
		int pos1, pos2;
		String wrkStr, id, value;
		StringTokenizer workTokenizer;
		String fixedWhereValue = keywordValue.replace("\"", "'");;
		if (keywordValue.contains("SESSION_ATTRIBUTE:")) {
			for (int i = 0; i < keywordValue.length(); i++) {
				pos1 = keywordValue.indexOf("SESSION_ATTRIBUTE:", i);
				if (pos1 == -1) {
					i = keywordValue.length();
				} else {
					pos2 = keywordValue.indexOf(" ", pos1);
					if (pos2 == -1) {
						pos2 = keywordValue.length();
						i = keywordValue.length();
					}
					wrkStr = keywordValue.substring(pos1, pos2);
					wrkStr = wrkStr.replace("'", "");
					wrkStr = wrkStr.replace("\"", "");
					workTokenizer = new StringTokenizer(wrkStr, ":" );
					if (workTokenizer.countTokens() == 2) {
						workTokenizer.nextToken();
						id = workTokenizer.nextToken().trim();
						value = session.getAttribute(id);
						if (value == null) {
							JOptionPane.showMessageDialog(null, "Session attribute not found with id of '" + id + "'.");
						} else {
							fixedWhereValue = fixedWhereValue.replace(wrkStr, value);
						}
					}
					i = pos1 + 1;
				}
			}
		}
		if (keywordValue.contains("USER_ATTRIBUTE:")) {
			for (int i = 0; i < keywordValue.length(); i++) {
				pos1 = keywordValue.indexOf("USER_ATTRIBUTE:", i);
				if (pos1 == -1) {
					i = keywordValue.length();
				} else {
					pos2 = keywordValue.indexOf(" ", pos1);
					if (pos2 == -1) {
						pos2 = keywordValue.length();
						i = keywordValue.length();
					}
					wrkStr = keywordValue.substring(pos1, pos2);
					wrkStr = wrkStr.replace("'", "");
					wrkStr = wrkStr.replace("\"", "");
					workTokenizer = new StringTokenizer(wrkStr, ":" );
					if (workTokenizer.countTokens() == 2) {
						workTokenizer.nextToken();
						id = workTokenizer.nextToken().trim();
						value = session.getAttribute(id);
						if (value == null) {
							JOptionPane.showMessageDialog(null, "User attribute not found with type of '" + id + "'.");
						} else {
							fixedWhereValue = fixedWhereValue.replace(wrkStr, value);
						}
					}
					i = pos1 + 1;
				}
			}
		}
		return fixedWhereValue;
	}
	
	public static String getBasicTypeOf(String dataType){
		String basicType = "STRING";
		if (dataType.equals("INTEGER")
				|| dataType.equals("SMALLINT")
				|| dataType.equals("BIGINT")) {
			basicType = "INTEGER";
		}
		if (dataType.equals("DOUBLE")
				|| dataType.equals("DECIMAL")
				|| dataType.equals("FLOAT")
				|| dataType.equals("DOUBLE PRECISION")
				|| dataType.equals("NUMERIC")
				|| dataType.equals("REAL")) {
			basicType = "FLOAT";
		}
		if (dataType.equals("CHAR")
				|| dataType.equals("TEXT")
				|| dataType.equals("VARCHAR")
				|| dataType.equals("LONG VARCHAR")) {
			basicType = "STRING";
		}
		if (dataType.equals("BINARY")
				|| dataType.equals("VARBINARY")
				|| dataType.equals("BLOB")) {
			basicType = "BINARY";
		}
		if (dataType.equals("CLOB")) {
			basicType = "CLOB";
		}
		if (dataType.equals("BYTEA")) {
			basicType = "BYTEA";
		}
		if (dataType.equals("DATE")) {
			basicType = "DATE";
		}
		if (dataType.startsWith("TIME")) {
			basicType = "TIME";
		}
		if (dataType.startsWith("TIMESTAMP")) {
			basicType = "DATETIME";
		}
		if (dataType.equals("DATETIME")) {
			basicType = "DATETIME";
		}
		return basicType;
	}
	
	public static boolean isLiteralRequiredBasicType(String basicType) {
		if (basicType.equals("STRING")
				|| basicType.equals("DATE")
				|| basicType.equals("TIME")
				|| basicType.equals("DATETIME")
				|| basicType.equals("BYTEA")) {
			return true;
		} else {
			return false;
		}
	}
	
	public static Object getTableOperationValue(String basicType, Object value, String dbName){
		Object returnValue = null;
		if (basicType.equals("INTEGER")) {
			if (value == null || value.toString().equals("")) {
				value = 0;
			}
			//returnValue = Long.parseLong(value.toString());
			String strInt = value.toString();
			if (strInt.endsWith(".0")) {
				strInt = strInt.replace(".0", ""); //step for value of variant defined in JavaScript//
			}
			if (strInt.contains("E")) {
				double doubleValue = new Double(strInt);
				strInt = Integer.toString((int)Math.floor(doubleValue));
			}
			returnValue = Long.parseLong(strInt);
		}
		if (basicType.equals("FLOAT")) {
			if (value == null || value.toString().equals("")) {
				value = 0.0;
			}
			returnValue = Double.parseDouble(value.toString());
		}
		if (basicType.equals("STRING")) {
//			String strValue = value.toString().replaceAll("'","''");
//			returnValue = "'" + strValue + "'";
			if (value.toString().contains("''")) {
				returnValue = "'" + value.toString() + "'";
			} else {
				String strValue = value.toString().replaceAll("'","''");
				returnValue = "'" + strValue + "'";
			}
		}
		if (basicType.equals("DATE")) {
			if (value == null) {
				returnValue = "NULL";
			} else {
				String strDate = value.toString();
				if (strDate == null || strDate.equals("")) {
					returnValue = "NULL";
				} else {
					if (dbName.contains("ucanaccess:")) {
						returnValue = "#" + strDate + "#";
					} else {
						returnValue = "'" + strDate + "'";
					}
				}
			}
		}
		if (basicType.equals("DATETIME")) {
			String timeDate = value.toString();
			if (timeDate == null || timeDate.equals("")) {
				returnValue = "NULL";
			} else {
				if (timeDate.equals("CURRENT_TIMESTAMP")) {
					returnValue = timeDate;
				} else {
					timeDate = timeDate.replace("/", "-");
					if (dbName.contains("ucanaccess:")) {
						returnValue = "#" + timeDate + "#";
					} else {
						returnValue = "'" + timeDate + "'";
					}
				}
			}
		}
		if (basicType.equals("BYTEA")) {
			if (value == null) {
				returnValue = "''";
			} else {
				XFByteArray xfByteArray = (XFByteArray)value;
				byte[] byteArray = (byte[])xfByteArray.getInternalValue();
				String stringByteArray = new String(byteArray);
				returnValue = "'"+ stringByteArray + "'";
			}
		}
		return returnValue;
	}

	static String getTableIDOfTableAlias(String tableAlias, NodeList referList1, NodeList referList2) {
		String tableID = tableAlias;
		org.w3c.dom.Element workElement;
	    for (int j = 0; j < referList1.getLength(); j++) {
			workElement = (org.w3c.dom.Element)referList1.item(j);
			if (workElement.getAttribute("TableAlias").equals(tableAlias) ||
					(workElement.getAttribute("ToTable").equals(tableAlias) && workElement.getAttribute("TableAlias").equals(""))) {
				tableID = workElement.getAttribute("ToTable");
				break;
			}
	    }
	    if (referList2 != null) {
	    	for (int j = 0; j < referList2.getLength(); j++) {
	    		workElement = (org.w3c.dom.Element)referList2.item(j);
	    		if (workElement.getAttribute("TableAlias").equals(tableAlias) ||
	    				(workElement.getAttribute("ToTable").equals(tableAlias) && workElement.getAttribute("TableAlias").equals(""))) {
	    			tableID = workElement.getAttribute("ToTable");
	    			break;
	    		}
	    	}
	    }
		return tableID;
	}

	static Object getValueAccordingToBasicType(String basicType, Object value){
		String wrkStr;
		Object valueReturn = null;
		if (isLiteralRequiredBasicType(basicType)) {
			if (basicType.equals("BYTEA")) {
				valueReturn = new XFByteArray(value);
			} else {
				if (value == null) {
					if (basicType.equals("DATE")) {
						valueReturn = "";
					} else {
						valueReturn = null;
					}
				} else {
					valueReturn = value.toString().trim();
				}
			}
		} else {
			if (basicType.equals("INTEGER")) {
				if (value == null || value.equals("")) {
					valueReturn = "";
				} else {
					wrkStr = value.toString();
					wrkStr = XFUtility.getStringNumber(wrkStr);
					wrkStr = wrkStr.replace(".0", "");
					if (wrkStr.equals("")) {
						valueReturn = "";
					} else {
						valueReturn = Long.parseLong(wrkStr);
					}
				}
			} else {
				if (basicType.equals("FLOAT")) {
					if (value == null || value.equals("")) {
						valueReturn = "";
					} else {
						wrkStr = XFUtility.getStringNumber(value.toString());
						if (wrkStr.equals("")) {
							valueReturn = "";
						} else {
							valueReturn = Double.parseDouble(wrkStr);
						}
					}
				} else {
					valueReturn = value;
				}
			}
		}
		return valueReturn;
	}
	
	static void setValueToEditableField(String basicType, Object valueObject, XFEditableField editableField){
		String wrkStr;
		if (basicType.equals("INTEGER")) {
			if (valueObject == null || valueObject.equals("")) {
				editableField.setValue("");
			} else {
				if (valueObject.equals("*AUTO")) {
					editableField.setValue(valueObject);
				} else {
					wrkStr = valueObject.toString();
					wrkStr = XFUtility.getStringNumber(wrkStr);
					wrkStr = wrkStr.replace(".0", "");
					if (wrkStr.equals("")) {
						editableField.setValue("");
					} else {
						editableField.setValue(Long.parseLong(wrkStr));
					}
				}
			}
		}
		if (basicType.equals("FLOAT")) {
			if (valueObject == null || valueObject.equals("")) {
				editableField.setValue("");
			} else {
				wrkStr = XFUtility.getStringNumber(valueObject.toString());
				if (wrkStr.equals("")) {
					editableField.setValue("");
				} else {
					editableField.setValue(Double.parseDouble(wrkStr));
				}
			}
		}
		if (basicType.equals("STRING")) {
			if (valueObject == null) {
				editableField.setValue("");
			} else {
				String value = valueObject.toString();
				if (value == null) {
					editableField.setValue("");
				} else {
					editableField.setValue(value);
				}
			}
		}
//		if (basicType.equals("DATE")) {
//			if (valueObject == null) {
//				editableField.setValue(null);
//			} else {
//				editableField.setValue(valueObject);
//			}
//		}
		if (basicType.equals("DATE") || basicType.equals("BYTEA") || basicType.equals("BINARY")) {
			editableField.setValue(valueObject);
		}
		if (basicType.equals("DATETIME") || basicType.equals("TIME")) {
			if (valueObject == null) {
				editableField.setValue("");
			} else {
				editableField.setValue(valueObject.toString());
			}
		}
	}

	static void setOldValueToEditableField(String basicType, Object valueObject, XFEditableField editableField){
		String wrkStr;
		if (basicType.equals("INTEGER")) {
			if (valueObject == null || valueObject.equals("")) {
				editableField.setOldValue("");
			} else {
				wrkStr = valueObject.toString();
				wrkStr = XFUtility.getStringNumber(wrkStr);
				wrkStr = wrkStr.replace(".0", "");
				if (wrkStr.equals("")) {
					editableField.setOldValue("");
				} else {
					editableField.setOldValue(Long.parseLong(wrkStr));
				}

			}
		}
		if (basicType.equals("FLOAT")) {
			if (valueObject == null || valueObject.equals("")) {
				editableField.setOldValue("");
			} else {
				wrkStr = XFUtility.getStringNumber(valueObject.toString());
				if (wrkStr.equals("")) {
					editableField.setOldValue("");
				} else {
					editableField.setOldValue(Double.parseDouble(wrkStr));
				}
			}
		}
		if (basicType.equals("STRING")) {
			if (valueObject == null) {
				editableField.setOldValue("");
			} else {
				String value = valueObject.toString();
				if (value == null) {
					editableField.setOldValue("");
				} else {
					editableField.setOldValue(value);
				}
			}
		}
		if (basicType.equals("DATE") || basicType.equals("BINARY")) {
			if (valueObject == null) {
				editableField.setOldValue(null);
			} else {
				editableField.setOldValue(valueObject);
			}
		}
		if (basicType.equals("DATETIME") || basicType.equals("TIME")) {
			if (valueObject == null) {
				editableField.setOldValue("");
			} else {
				editableField.setOldValue(valueObject.toString());
			}
		}
	}
	
//	static short getFloatFormat(HSSFWorkbook workbook, int decimalSize) {
//		HSSFDataFormat format = workbook.createDataFormat();
//		StringBuffer buf = new StringBuffer();
//		for (int i = 0; i <= decimalSize; i++) {
//			if (i == 0) {
//				buf.append("#,##0");
//			} else {
//				if (i == 1) {
//					buf.append(".");
//				}
//				buf.append("0");
//			}
//		}
//		return format.getFormat(buf.toString());
//	}
	
	static short getFloatFormat(XSSFWorkbook workbook, int decimalSize) {
		XSSFDataFormat format = workbook.createDataFormat();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i <= decimalSize; i++) {
			if (i == 0) {
				buf.append("#,##0");
			} else {
				if (i == 1) {
					buf.append(".");
				}
				buf.append("0");
			}
		}
		return format.getFormat(buf.toString());
	}

	static Object calculateExpireValue(org.w3c.dom.Element tableElement, XFTableOperator operator, Session session, StringBuffer logBuf) throws Exception {
		Object object = null;
		String rangeKeyFieldValid, wrkStr;
		int count;
		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;
		StringBuffer buf = new StringBuffer();
		ArrayList<String> keyFieldIDList = new ArrayList<String>();
		ArrayList<Boolean> keyFieldIsLiteralRequiredList = new ArrayList<Boolean>();

		String tableID = tableElement.getAttribute("ID");
		String activeWhere = tableElement.getAttribute("ActiveWhere");

		NodeList nodeList = tableElement.getElementsByTagName("Key");
		for (int i = 0; i < nodeList.getLength(); i++) {
			workElement = (org.w3c.dom.Element)nodeList.item(i);
			if (workElement.getAttribute("Type").equals("PK")) {
				workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
				while (workTokenizer.hasMoreTokens()) {
					wrkStr = workTokenizer.nextToken();
					keyFieldIDList.add(wrkStr);
					workElement = session.getFieldElement(tableID, wrkStr);
					wrkStr = workElement.getAttribute("Type");
					if (wrkStr.equals("CHAR")
							|| wrkStr.equals("VARCHAR")
							|| wrkStr.equals("LONG VARCHAR")
							|| wrkStr.equals("DATE")
							|| wrkStr.equals("DATETIME")
							|| wrkStr.startsWith("TIME")
							|| wrkStr.startsWith("TIMESTAMP")) {
						keyFieldIsLiteralRequiredList.add(true);
					} else {
						keyFieldIsLiteralRequiredList.add(false);
					}
				}
				break;
			}
		}

		workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
		rangeKeyFieldValid = workTokenizer.nextToken();

		buf.append("select ");
		buf.append(rangeKeyFieldValid);
		buf.append(" from ");
		buf.append(tableID);
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < keyFieldIDList.size(); i++) {
				count++;
				if (count > 0) {
					buf.append(" and ") ;
				}
				buf.append(keyFieldIDList.get(i)) ;
				if (keyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
					buf.append(">") ;
				} else {
					buf.append("=") ;
				}
				if (keyFieldIsLiteralRequiredList.get(i)) {
					buf.append("'");
					buf.append(operator.getValueOf(keyFieldIDList.get(i)).toString());
					buf.append("'");
				} else {
					buf.append(operator.getValueOf(keyFieldIDList.get(i)).toString());
				}
		}
		if (!activeWhere.equals("")) {
			buf.append(" and ");
			buf.append(activeWhere);
		}
		buf.append(" order by ");
		buf.append(rangeKeyFieldValid);
		XFTableOperator operatorExpire = new XFTableOperator(session, logBuf, buf.toString());
		if (operatorExpire.next()) {
			object = operatorExpire.getValueOf(rangeKeyFieldValid);
		}

		return object;
	}
	
	static int adjustFontSizeToGetPreferredWidthOfLabel(JLabel jLabel, int initialWidth) {
		int width = initialWidth;
//		if (metrics.stringWidth(jLabel.getText()) > width) {
//		jLabel.setFont(new java.awt.Font(jLabel.getFont().getFontName(), 0, jLabel.getFont().getSize()-1));
//		metrics = jLabel.getFontMetrics(jLabel.getFont());
//		if (metrics.stringWidth(jLabel.getText()) > width) {
//			jLabel.setFont(new java.awt.Font(jLabel.getFont().getFontName(), 0, jLabel.getFont().getSize()-1));
//			metrics = jLabel.getFontMetrics(jLabel.getFont());
//			if (metrics.stringWidth(jLabel.getText()) > width) {
//				jLabel.setFont(new java.awt.Font(jLabel.getFont().getFontName(), 0, jLabel.getFont().getSize()-1));
//				metrics = jLabel.getFontMetrics(jLabel.getFont());
//				if (metrics.stringWidth(jLabel.getText()) > width) {
//					jLabel.setFont(new java.awt.Font(jLabel.getFont().getFontName(), 0, jLabel.getFont().getSize()-1));
//					metrics = jLabel.getFontMetrics(jLabel.getFont());
//					if (metrics.stringWidth(jLabel.getText()) > width) {
//						jLabel.setFont(new java.awt.Font(jLabel.getFont().getFontName(), 0, jLabel.getFont().getSize()-1));
//						metrics = jLabel.getFontMetrics(jLabel.getFont());
//					}
//				}
//			}
//		}
//	}
		int initialFontSize = jLabel.getFont().getSize();
		FontMetrics metrics = jLabel.getFontMetrics(jLabel.getFont());
		if (metrics.stringWidth(jLabel.getText()) > width) {
			for (int i = initialFontSize; i > 10; i--) {
				jLabel.setFont(new java.awt.Font(jLabel.getFont().getFontName(), 0, jLabel.getFont().getSize()-1));
				metrics = jLabel.getFontMetrics(jLabel.getFont());
				if (metrics.stringWidth(jLabel.getText()) <= width) {
					break;
				}
			}
		}
		return metrics.stringWidth(jLabel.getText());
	}

	/**
	 * Method to process String value with EOL mark
	 * @param originalString :original string value to be processed
	 * @param stringToBeInserted :value to be replaced into "#EOL#"(usually it's "\n")
	 * @return String :string value of processed string
	 */
	public static String substringLinesWithTokenOfEOL(String originalString, String stringToBeInserted) {
		StringBuffer processedString = new StringBuffer();
		int lastEnd = 0;
		for (int i = 0; i <= originalString.length(); i++) {
			if (i+5 <= originalString.length()) {
				if (originalString.substring(i,i+5).equals("#EOL#")) {
					processedString.append(originalString.substring(lastEnd, i));
					processedString.append(stringToBeInserted);
					lastEnd = i+5;
				}
			} else {
				if (i == originalString.length()) {
					processedString.append(originalString.substring(lastEnd, i));
				}
			}
		}
		return processedString.toString();
	}

	public static String getLayoutedString(String originalString, String stringToBeInserted, String fontName) {
		StringBuffer processedString = new StringBuffer();
		int lastEnd = 0;
		int maxRowLength = 0;
		int pixcelLengthOfFirstRow = 0;
		String wrkStr;
		JLabel label = new JLabel();
		FontMetrics metrics = label.getFontMetrics(new java.awt.Font(fontName, 0, XFUtility.FONT_SIZE));
		
		if ((originalString.getBytes().length) == originalString.length()) {
			maxRowLength = 40;
		} else {
			maxRowLength = 30;
		}

		String adjustedString = originalString.replaceAll("#EOL#", "");
		if (adjustedString.length() > maxRowLength) {
			wrkStr = adjustedString.substring(0, maxRowLength);
			pixcelLengthOfFirstRow = metrics.stringWidth(wrkStr);

			adjustedString = originalString.replaceAll("#EOL#", "\n");
			for (int i = 0; i < adjustedString.length(); i++) {
				wrkStr = adjustedString.substring(lastEnd, i+1);
				if (metrics.stringWidth(wrkStr) < pixcelLengthOfFirstRow) {
					if (i == (adjustedString.length()-1)) {
						wrkStr = adjustedString.substring(lastEnd, i+1);
						processedString.append(adjustedString.substring(lastEnd, i+1));
						lastEnd = i;
					} else {
						if (adjustedString.substring(i, i+1).equals("\n")) {
							wrkStr = adjustedString.substring(lastEnd, i);
							processedString.append(wrkStr + stringToBeInserted);
							lastEnd = i+1;
						}
					}
				} else {
					wrkStr = adjustedString.substring(lastEnd, i);
					processedString.append(wrkStr + stringToBeInserted);
					lastEnd = i;
				}
			}
		} else {
			processedString.append(adjustedString.replaceAll("\n", stringToBeInserted));
		}

		return processedString.toString();
	}

	static String getEditValueOfLong(long value, String editCode, int size) {
		DecimalFormat integerFormat = new DecimalFormat("#,##0;#,##0-");

		//a0 1,234,567.0000-
		if (editCode.equals("a0")) {
			integerFormat = new DecimalFormat("#,##0;#,##0-");
		}
		//a1 -1,234,567.0000
		if (editCode.equals("a1")) {
			integerFormat = new DecimalFormat("#,##0;-#,##0");
		}
		//g0 1.234.567,0000-
		if (editCode.equals("g0")) {
			integerFormat = new DecimalFormat("#.##0;#.##0-");
		}
		//g1 -1.234.567,0000
		if (editCode.equals("g1")) {
			integerFormat = new DecimalFormat("#.##0;-#.##0");
		}
		//j0 1,234,567.0000▲
		if (editCode.equals("j0")) {
			integerFormat = new DecimalFormat("#,##0;#,##0▲");
		}
		//j1 ▲1,234,567.0000
		if (editCode.equals("j1")) {
			integerFormat = new DecimalFormat("#,##0;▲#,##0");
		}

		return integerFormat.format(value);
	}

	static String getEditValueOfDouble(double value, String editCode, int decimal) {
		DecimalFormat floatFormat = new DecimalFormat("#,##0.0;#,##0.0-");

		//a0 1,234,567.0000-
		if (editCode.equals("a0")) {
			if (decimal == 1) {
				floatFormat = new DecimalFormat("#,##0.0;#,##0.0-");
			}
			if (decimal == 2) {
				floatFormat = new DecimalFormat("#,##0.00;#,##0.00-");
			}
			if (decimal == 3) {
				floatFormat = new DecimalFormat("#,##0.000;#,##0.000-");
			}
			if (decimal == 4) {
				floatFormat = new DecimalFormat("#,##0.0000;#,##0.0000-");
			}
			if (decimal >= 5) {
				floatFormat = new DecimalFormat("#,##0.00000;#,##0.00000-");
			}
		}
		//a1 -1,234,567.0000
		if (editCode.equals("a1")) {
			if (decimal == 1) {
				floatFormat = new DecimalFormat("#,##0.0;-#,##0.0");
			}
			if (decimal == 2) {
				floatFormat = new DecimalFormat("#,##0.00;-#,##0.00");
			}
			if (decimal == 3) {
				floatFormat = new DecimalFormat("#,##0.000;-#,##0.000");
			}
			if (decimal == 4) {
				floatFormat = new DecimalFormat("#,##0.0000;-#,##0.0000");
			}
			if (decimal >= 5) {
				floatFormat = new DecimalFormat("#,##0.00000;#,-##0.00000");
			}
		}
		//g0 1.234.567,0000-
		if (editCode.equals("g0")) {
			if (decimal == 1) {
				floatFormat = new DecimalFormat("#.##0,0;#.##0,0-");
			}
			if (decimal == 2) {
				floatFormat = new DecimalFormat("#.##0,00;#.##0,00-");
			}
			if (decimal == 3) {
				floatFormat = new DecimalFormat("#.##0,000;#.##0,000-");
			}
			if (decimal == 4) {
				floatFormat = new DecimalFormat("#.##0,0000;#.##0,0000-");
			}
			if (decimal >= 5) {
				floatFormat = new DecimalFormat("#.##0,00000;#.##0,00000-");
			}
		}
		//g1 -1.234.567,0000
		if (editCode.equals("g1")) {
			if (decimal == 1) {
				floatFormat = new DecimalFormat("#.##0,0;-#.##0,0");
			}
			if (decimal == 2) {
				floatFormat = new DecimalFormat("#.##0,00;-#.##0,00");
			}
			if (decimal == 3) {
				floatFormat = new DecimalFormat("#.##0,000;-#.##0,000");
			}
			if (decimal == 4) {
				floatFormat = new DecimalFormat("#.##0,0000;-#.##0,0000");
			}
			if (decimal >= 5) {
				floatFormat = new DecimalFormat("#.##0,00000;-#.##0,00000");
			}
		}
		//j0 1,234,567.0000▲
		if (editCode.equals("j0")) {
			if (decimal == 1) {
				floatFormat = new DecimalFormat("#,##0.0;#,##0.0▲");
			}
			if (decimal == 2) {
				floatFormat = new DecimalFormat("#,##0.00;#,##0.00▲");
			}
			if (decimal == 3) {
				floatFormat = new DecimalFormat("#,##0.000;#,##0.000▲");
			}
			if (decimal == 4) {
				floatFormat = new DecimalFormat("#,##0.0000;#,##0.0000▲");
			}
			if (decimal >= 5) {
				floatFormat = new DecimalFormat("#,##0.00000;#,##0.00000▲");
			}
		}
		//j1 ▲1,234,567.0000
		if (editCode.equals("j1")) {
			if (decimal == 1) {
				floatFormat = new DecimalFormat("#,##0.0;▲#,##0.0");
			}
			if (decimal == 2) {
				floatFormat = new DecimalFormat("#,##0.00;▲#,##0.00");
			}
			if (decimal == 3) {
				floatFormat = new DecimalFormat("#,##0.000;▲#,##0.000");
			}
			if (decimal == 4) {
				floatFormat = new DecimalFormat("#,##0.0000;▲#,##0.0000");
			}
			if (decimal >= 5) {
				floatFormat = new DecimalFormat("#,##0.00000;#,▲##0.00000");
			}
		}

		return floatFormat.format(value);
	}
	
	public static String getUserExpressionOfUtilDate(java.util.Date date, String dateFormat, boolean isWithTime) {

		//en00 06/17/10
		//en01 Thur,06/17/01
		//en10 Jun17,2010
		//en11 Thur,Jun17,2001
		//
		//jp00 10/06/17
		//jp01 10/06/17(木)
		//jp10 2010/06/17
		//jp11 2010/06/17(木)
		//jp20 2010年6月17日
		//jp21 2010年6月17日(木)
		//jp30 H22/06/17
		//jp31 H22/06/17(水)
		//jp40 H22年06月17日 
		//jp41 H22年06月17日(水)
		//jp50 平成22年06月17日
		//jp51 平成22年06月17日(水)

		Calendar cal = Calendar.getInstance();
		if (date != null) { 
			cal.setTime(date);
		}

		StringBuffer buf = new StringBuffer();
		SimpleDateFormat formatter;

		if (dateFormat.equals("")) {
			if (isWithTime) {
				formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", new Locale("en", "US", "US"));
			} else {
				formatter = new SimpleDateFormat("yyyy-MM-dd", new Locale("en", "US", "US"));
			}
			buf.append(formatter.format(cal.getTime()));
		}

		if (dateFormat.equals("en00") || dateFormat.equals("en01")) {
			if (dateFormat.equals("en01")) {
				buf.append(getDayOfWeek(cal, dateFormat));
			}
			if (isWithTime) {
				formatter = new SimpleDateFormat("MM/dd/yy HH:mm", new Locale("en", "US", "US"));
			} else {
				formatter = new SimpleDateFormat("MM/dd/yy", new Locale("en", "US", "US"));
			}
			buf.append(formatter.format(cal.getTime()));
		}

		if (dateFormat.equals("en10") || dateFormat.equals("en11")) {
			if (dateFormat.equals("en11")) {
				buf.append(getDayOfWeek(cal, dateFormat));
			}
			if (isWithTime) {
				formatter = new SimpleDateFormat("MMMdd,yyyy HH:mm", new Locale("en", "US", "US"));
			} else {
				formatter = new SimpleDateFormat("MMMdd,yyyy", new Locale("en", "US", "US"));
			}
			buf.append(formatter.format(cal.getTime()));
		}

		if (dateFormat.equals("jp00") || dateFormat.equals("jp01")) {
			if (isWithTime) {
				formatter = new SimpleDateFormat("yy/MM/dd HH:mm");
			} else {
				formatter = new SimpleDateFormat("yy/MM/dd");
			}
			buf.append(formatter.format(cal.getTime()));
			if (dateFormat.equals("jp01")) {
				buf.append(getDayOfWeek(cal, dateFormat));
			}
		}

		if (dateFormat.equals("jp10") || dateFormat.equals("jp11")) {
			if (isWithTime) {
				formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			} else {
				formatter = new SimpleDateFormat("yyyy/MM/dd");
			}
			buf.append(formatter.format(cal.getTime()));
			if (dateFormat.equals("jp11")) {
				buf.append(getDayOfWeek(cal, dateFormat));
			}
		}

		if (dateFormat.equals("jp20") || dateFormat.equals("jp21")) {
			if (isWithTime) {
				formatter = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
			} else {
				formatter = new SimpleDateFormat("yyyy年MM月dd日");
			}
			buf.append(formatter.format(cal.getTime()));
			if (dateFormat.equals("jp21")) {
				buf.append(getDayOfWeek(cal, dateFormat));
			}
		}

		if (dateFormat.equals("jp30") || dateFormat.equals("jp31")) {
			if (isWithTime) {
				formatter = new SimpleDateFormat("Gyy/MM/dd HH:mm", new Locale("ja", "JP", "JP"));
			} else {
				formatter = new SimpleDateFormat("Gyy/MM/dd", new Locale("ja", "JP", "JP"));
			}
			buf.append(formatter.format(cal.getTime()));
			if (dateFormat.equals("jp31")) {
				buf.append(getDayOfWeek(cal, dateFormat));
			}
		}

		if (dateFormat.equals("jp40") || dateFormat.equals("jp41")) {
			if (isWithTime) {
				formatter = new SimpleDateFormat("Gyy年MM月dd日 HH:mm", new Locale("ja", "JP", "JP"));
			} else {
				formatter = new SimpleDateFormat("Gyy年MM月dd日", new Locale("ja", "JP", "JP"));
			}
			buf.append(formatter.format(cal.getTime()));
			if (dateFormat.equals("jp41")) {
				buf.append(getDayOfWeek(cal, dateFormat));
			}
		}

		if (dateFormat.equals("jp50") || dateFormat.equals("jp51")) {
			if (isWithTime) {
				formatter = new SimpleDateFormat("GGGGyy年MM月dd日 HH:mm", new Locale("ja", "JP", "JP"));
			} else {
				formatter = new SimpleDateFormat("GGGGyy年MM月dd日", new Locale("ja", "JP", "JP"));
			}
			buf.append(formatter.format(cal.getTime()));
			if (dateFormat.equals("jp51")) {
				buf.append(getDayOfWeek(cal, dateFormat));
			}
		}

		return buf.toString();
	}
	
	static String getCaptionForCell(String caption) {
		String value = caption;
		if (value.toUpperCase().contains("<HTML>")) {
			value = value.replace("<HTML>", "");
			value = value.replace("<html>", "");
			value = value.replace("<BR>", "\n");
			value = value.replace("<br>", "\n");
			value = value.replace("</HTML>", "");
			value = value.replace("</html>", "");
		}
		return value;
	}
	
	static com.lowagie.text.Image getImageForPDF(String fileName, float newWidth, float newHeight) {
		com.lowagie.text.Image image = null;
		try {
			float percentWidth = 100f;
			float percentHeight = 100f;
			BufferedImage bi = null;
			if (fileName.startsWith("http://")) {
					fileName = fileName.replace("\\", "/");
					URL url = new URL(fileName);
					bi = ImageIO.read(url);
			} else {
				File imageFile = new File(fileName);
				if (imageFile.exists()) {
					bi = ImageIO.read(imageFile);
				}
			}
			if (bi != null) {
				image = com.lowagie.text.Image.getInstance(bi, null);
				if (newWidth > 0 && newHeight > 0) {
					image.scaleToFit(newWidth, newHeight);
				} else {
					if (newWidth > 0) {
						percentWidth = newWidth / image.getWidth() * 100.0f;
					}
					if (newHeight > 0) {
						percentHeight = newHeight / image.getHeight() * 100.0f;
					}
					if (percentWidth < percentHeight) {
						image.scalePercent(percentWidth);
					} else {
						image.scalePercent(percentHeight);
					}
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage() + "\n" + fileName);
		}
		return image;
	}

	static void setupImageCell(XSSFWorkbook workBook, XSSFSheet workSheet, int rowIndexFrom, int rowIndexThru, int columnIndexFrom, int columnIndexThru, String fileName) {
		//////////////////////////
		// Determine Image Type //
		//////////////////////////
		int imageTypeIndex = -1;
		String fileNameCapital = fileName.toUpperCase();
		if (fileNameCapital.contains(".PNG")) {
			imageTypeIndex = XSSFWorkbook.PICTURE_TYPE_PNG;
		}
		if (fileNameCapital.contains(".JPG")
				|| fileNameCapital.contains(".JPE")
				|| fileNameCapital.contains(".JPEG")) {
			imageTypeIndex = XSSFWorkbook.PICTURE_TYPE_JPEG;
		}
		if (fileNameCapital.contains(".GIF")) {
			imageTypeIndex = XSSFWorkbook.PICTURE_TYPE_GIF;
		}
		if (fileNameCapital.contains(".BMP")) {
			imageTypeIndex = XSSFWorkbook.PICTURE_TYPE_BMP;
		}

		/////////////////////////////////////////
		// Add image byte data to the workbook //
		/////////////////////////////////////////
		if (imageTypeIndex != -1) {
			try {
				InputStream inputStream = new FileInputStream(fileName);
				byte[] bytes = IOUtils.toByteArray(inputStream);
				int pictureIndex = workBook.addPicture(bytes, imageTypeIndex);
				inputStream.close();

				CreationHelper helper = workBook.getCreationHelper();
				Drawing drawing = workSheet.createDrawingPatriarch();
				ClientAnchor anchor = helper.createClientAnchor();
				anchor.setCol1(columnIndexFrom);
				anchor.setRow1(rowIndexFrom);
				anchor.setCol2(columnIndexThru);
				anchor.setRow2(rowIndexThru);
				anchor.setDx1(20);
				anchor.setDy1(20);
				anchor.setDx2(0);
				anchor.setDy2(0);
				drawing.createPicture(anchor, pictureIndex);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	static com.lowagie.text.Rectangle getPageSize(String size, String direction) {
		com.lowagie.text.Rectangle rec = PageSize.A4; //Default//

		if (direction.equals("LANDSCAPE")) {
			if (size.equals("A2")) {
				rec = PageSize.A2.rotate();
			}
			if (size.equals("A3")) {
				rec = PageSize.A3.rotate();
			}
			if (size.equals("A4")) {
				rec = PageSize.A4.rotate();
			}
			if (size.equals("A5")) {
				rec = PageSize.A5.rotate();
			}
			if (size.equals("A6")) {
				rec = PageSize.A6.rotate();
			}
			//
			if (size.equals("B2")) {
				rec = PageSize.B2.rotate();
			}
			if (size.equals("B3")) {
				rec = PageSize.B3.rotate();
			}
			if (size.equals("B4")) {
				rec = PageSize.B4.rotate();
			}
			if (size.equals("B5")) {
				rec = PageSize.B5.rotate();
			}
			if (size.equals("B6")) {
				rec = PageSize.B6.rotate();
			}
		} else {
			if (size.equals("A2")) {
				rec = PageSize.A2;
			}
			if (size.equals("A3")) {
				rec = PageSize.A3;
			}
			if (size.equals("A4")) {
				rec = PageSize.A4;
			}
			if (size.equals("A5")) {
				rec = PageSize.A5;
			}
			if (size.equals("A6")) {
				rec = PageSize.A6;
			}

			if (size.equals("B2")) {
				rec = PageSize.B2;
			}
			if (size.equals("B3")) {
				rec = PageSize.B3;
			}
			if (size.equals("B4")) {
				rec = PageSize.B4;
			}
			if (size.equals("B5")) {
				rec = PageSize.B5;
			}
			if (size.equals("B6")) {
				rec = PageSize.B6;
			}
		}

		return rec;
	}
	
	static void drawLineOrRect(String keyword, PdfContentByte cb) {
		float x1, y1, x2, y2;
		try {
			if (keyword.contains("&Line(")) {
				int pos = keyword.lastIndexOf(")");
				StringTokenizer workTokenizer = new StringTokenizer(keyword.substring(6, pos), ";" );
				if (workTokenizer.countTokens() >= 4) {
					x1 = Float.parseFloat(workTokenizer.nextToken().trim());
					y1 = Float.parseFloat(workTokenizer.nextToken().trim());
					x2 = Float.parseFloat(workTokenizer.nextToken().trim());
					y2 = Float.parseFloat(workTokenizer.nextToken().trim());
					cb.moveTo(x1, y1);
					cb.lineTo(x2, y2);
					cb.stroke();
				}
			}
			if (keyword.contains("&Rect(")) {
				int pos = keyword.lastIndexOf(")");
				StringTokenizer workTokenizer = new StringTokenizer(keyword.substring(6, pos), ";" );
				if (workTokenizer.countTokens() == 4) {
					x1 = Float.parseFloat(workTokenizer.nextToken().trim());
					y1 = Float.parseFloat(workTokenizer.nextToken().trim());
					x2 = Float.parseFloat(workTokenizer.nextToken().trim());
					y2 = Float.parseFloat(workTokenizer.nextToken().trim());
					cb.moveTo(x1, y1);
					cb.lineTo(x2, y1);
					cb.lineTo(x2, y2);
					cb.lineTo(x1, y2);
					cb.closePathStroke();
				}
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, RESOURCE.getString("FunctionError39") + keyword + RESOURCE.getString("FunctionError40") + e.toString());
		}
	}

	static Chunk getBarcodeChunkOfValue(String value, String type, PdfContentByte cb) throws BadElementException {
		Chunk chunk = null;
		com.lowagie.text.Image image;

		if (type.equals("EAN8")) {
			BarcodeEAN barcode = new BarcodeEAN();
			barcode.setCodeType(BarcodeEAN.EAN8);
			barcode.setCode(value);
			image = barcode.createImageWithBarcode(cb, null, null);
			chunk = new Chunk(image, 0, 0);
		}
		if (type.equals("EAN13")) {
			BarcodeEAN barcode = new BarcodeEAN();
			barcode.setCodeType(BarcodeEAN.EAN13);
			barcode.setCode(value);
			image = barcode.createImageWithBarcode(cb, null, null);
			chunk = new Chunk(image, 0, 0);
		}
		if (type.equals("39")) {
			Barcode39 barcode = new Barcode39();
			barcode.setCode(value);
			image = barcode.createImageWithBarcode(cb, null, null);
			chunk = new Chunk(image, 0, 0);
		}
		if (type.equals("128")) {
			Barcode128 barcode = new Barcode128();
			barcode.setCode(value);
			image = barcode.createImageWithBarcode(cb, null, null);
			chunk = new Chunk(image, 0, 0);
		}
		if (type.equals("NW7")) {
			BarcodeCodabar barcode = new BarcodeCodabar();
			barcode.setCode(value);
			image = barcode.createImageWithBarcode(cb, null, null);
			chunk = new Chunk(image, 0, 0);
		}
		if (type.equals("PDF417")) {
			BarcodePDF417 barcode = new BarcodePDF417();
			barcode.setText(value);
			image = barcode.getImage();
			chunk = new Chunk(image, 0, 0);
		}

		return chunk;
	}
	
	static int getLengthOfEdittedNumericValue(int dataSize, int decimalSize, ArrayList<String> dataTypeOptionList) {
		int length = dataSize;
		if (decimalSize > 0) {
			length = length + 1;
		}
		if (dataTypeOptionList.contains("ACCEPT_MINUS") || dataTypeOptionList.contains("HH_MM")) {
			length = length + 1;
		}
		if (!dataTypeOptionList.contains("NO_EDIT")
				&& !dataTypeOptionList.contains("ZERO_SUPPRESS")
				&& !dataTypeOptionList.contains("HH_MM")) {
			int intSize = dataSize - decimalSize;
			while (intSize > 3) {
				length = length + 1;
				intSize = intSize - 3;
			}
		}
		return length;
	}
	
	static int getWidthOfDateValue(String dateFormat, String fontName, int fontSize) {
		int width = 133;
		JTextField textField = new JTextField();
		FontMetrics metrics = textField.getFontMetrics(new java.awt.Font(fontName, 0, fontSize));

		if (dateFormat.equals("en00")) {
			width = metrics.stringWidth("06/17/10");
		}
		if (dateFormat.equals("en01")) {
			width = metrics.stringWidth("Thur,06/17/01");
		}
		if (dateFormat.equals("en10")) {
			width = metrics.stringWidth("Jun17,2010");
		}
		if (dateFormat.equals("en11")) {
			width = metrics.stringWidth("Thur,Jun17,2001");
		}

		if (dateFormat.equals("jp00")) {
			width = metrics.stringWidth("10/06/17");
		}
		if (dateFormat.equals("jp01")) {
			width = metrics.stringWidth("10/06/17(木)");
		}
		if (dateFormat.equals("jp10")) {
			width = metrics.stringWidth("2010/06/17");
		}
		if (dateFormat.equals("jp11")) {
			width = metrics.stringWidth("2010/06/17(木)");
		}
		if (dateFormat.equals("jp20")) {
			width = metrics.stringWidth("2010年6月17日");
		}
		if (dateFormat.equals("jp21")) {
			width = metrics.stringWidth("2010年6月17日(木)");
		}
		if (dateFormat.equals("jp30")) {
			width = metrics.stringWidth("H22/06/17");
		}
		if (dateFormat.equals("jp31")) {
			width = metrics.stringWidth("H22/06/17(木)");
		}
		if (dateFormat.equals("jp40")) {
			width = metrics.stringWidth("H22年06月17日");
		}
		if (dateFormat.equals("jp41")) {
			width = metrics.stringWidth("H22年06月17日(木)");
		}
		if (dateFormat.equals("jp50")) {
			width = metrics.stringWidth("平成22年06月17日");
		}
		if (dateFormat.equals("jp51")) {
			width = metrics.stringWidth("平成22年06月17日(木)");
		}

		return width + 10;
	}
	
	static String getUserExpressionOfYearMonth(String yearMonth, String dateFormat) {

		//en00 06/17/10
		//en01 Thur,06/17/01
		//en10 Jun17,2010
		//en11 Thur,Jun17,2001
		//
		//jp00 10/06/17
		//jp01 10/06/17(木)
		//jp10 2010/06/17
		//jp11 2010/06/17(木)
		//jp20 2010年6月17日
		//jp21 2010年6月17日(木)
		//jp30 H22/06/17
		//jp31 H22/06/17(水)
		//jp40 H22年06月17日
		//jp41 H22年06月17日(水)
		//jp50 平成22年06月17日
		//jp51 平成22年06月17日(水)

		String result = "";

		if (yearMonth.length() == 6) {
			int year = Integer.parseInt(yearMonth.substring(0, 4));
			int month = Integer.parseInt(yearMonth.substring(4, 6)) - 1;
			SimpleDateFormat formatter;
			Calendar cal = Calendar.getInstance();
			cal.set(year, month, 1);

			if (dateFormat.equals("en00")
					|| dateFormat.equals("en01")
					|| dateFormat.equals("en10")
					|| dateFormat.equals("en11")) {
				formatter = new SimpleDateFormat("MMM,yyyy", new Locale("en", "US", "US"));
				result = formatter.format(cal.getTime());
			}
			if (dateFormat.equals("jp00")
					|| dateFormat.equals("jp01")
					|| dateFormat.equals("jp10")
					|| dateFormat.equals("jp11")
					|| dateFormat.equals("jp20")
					|| dateFormat.equals("jp21")) {
				formatter = new SimpleDateFormat("yyyy年MM月", new Locale("ja", "US", "US"));
				result = formatter.format(cal.getTime());
			}
			if (dateFormat.equals("jp30")
					|| dateFormat.equals("jp31")
					|| dateFormat.equals("jp40")
					|| dateFormat.equals("jp41")
					|| dateFormat.equals("jp50")
					|| dateFormat.equals("jp51")) {
				formatter = new SimpleDateFormat("Gyy年MM月", new Locale("ja", "JP", "JP"));
				result = formatter.format(cal.getTime());
			}
		}

		if (yearMonth.length() == 4) {
			int year = Integer.parseInt(yearMonth.substring(0, 4));
			int month = 0;
			SimpleDateFormat formatter;
			Calendar cal = Calendar.getInstance();
			cal.set(year, month, 1);

			if (dateFormat.equals("en00")
					|| dateFormat.equals("en01")
					|| dateFormat.equals("en10")
					|| dateFormat.equals("en11")) {
				formatter = new SimpleDateFormat("fiscal yyyy", new Locale("en", "US", "US"));
				result = formatter.format(cal.getTime());
			}
			if (dateFormat.equals("jp00")
					|| dateFormat.equals("jp01")
					|| dateFormat.equals("jp10")
					|| dateFormat.equals("jp11")
					|| dateFormat.equals("jp20")
					|| dateFormat.equals("jp21")) {
				formatter = new SimpleDateFormat("yyyy年度", new Locale("ja", "US", "US"));
				result = formatter.format(cal.getTime());
			}
			if (dateFormat.equals("jp30")
					|| dateFormat.equals("jp31")
					|| dateFormat.equals("jp40")
					|| dateFormat.equals("jp41")
					|| dateFormat.equals("jp50")
					|| dateFormat.equals("jp51")) {
				formatter = new SimpleDateFormat("Gyy年度", new Locale("ja", "JP", "JP"));
				result = formatter.format(cal.getTime());
			}
		}

		return result;
	}
	
	static String getUserExpressionOfMSeq(int mSeq, Session session) {
	    String language = "";
	    String dateFormat = session.getDateFormat();
		if (dateFormat != null && dateFormat.length() > 1) {
			language = dateFormat.substring(0, 2);
		}
	    String[] monthArrayEn = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov"};
	    String[] monthArrayJp = {"１月度","２月度","３月度","４月度","５月度","６月度","７月度","８月度","９月度","10月度","11月度","12月度","１月度","２月度","３月度","４月度","５月度","６月度","７月度","８月度","９月度","10月度","11月度"};
		String result = "";
		int startMonth = 1;
		startMonth = session.getSystemVariantInteger("FIRST_MONTH");
		if (mSeq >= 1 && mSeq <= 12) {
			if (language.equals("en")) {
				result = monthArrayEn[mSeq + startMonth - 2];
			}
			if (language.equals("jp")) {
				result = monthArrayJp[mSeq + startMonth - 2];
			}
		}
		return result;
	}
	
	static String getWhereValueOfDateTimeSegment(String operandType, String originalSegment) {
		String segment = originalSegment.replace("/", "-");
		String valueMin = "";
		String valueMax = "";
		StringBuffer bf = new StringBuffer();

		//////////////////////////////////////////////////////////////
		// Compensate value into the format YYYY-MM-DD hh:mm:ss.sss //
		//////////////////////////////////////////////////////////////
		if (segment.length() == 0) {
			valueMin = "0000-01-01 00:00:00.0";
			valueMax = "9999-12-31 23:59:59.999";
		}
		if (segment.length() == 1) {
			valueMin = segment + "000-01-01 00:00:00.0";
			valueMax = segment + "999-12-31 23:59:59.999";
		}
		if (segment.length() == 2) {
			valueMin = segment + "00-01-01 00:00:00.0";
			valueMax = segment + "99-12-31 23:59:59.999";
		}
		if (segment.length() == 3) {
			valueMin = segment + "0-01-01 00:00:00.0";
			valueMax = segment + "9-12-31 23:59:59.999";
		}
		if (segment.length() == 4) {
			valueMin = segment + "-01-01 00:00:00.0";
			valueMax = segment + "-12-31 23:59:59.999";
		}
		if (segment.length() == 5) {
			valueMin = segment + "01-01 00:00:00.0";
			valueMax = segment + "12-31 23:59:59.999";
		}
		if (segment.length() == 6) {
			String lastChar = segment.substring(segment.length()-1, segment.length());
			if (lastChar.equals("0")) {
				valueMin = segment + "1-01 00:00:00.0";
				valueMax = segment + "9-30 23:59:59.999";
			} else {
				valueMin = segment + "0-01 00:00:00.0";
				valueMax = segment + "2-31 23:59:59.999";
			}
		}
		if (segment.length() == 7) {
			String month = segment.substring(segment.length()-2, segment.length());
			valueMin = segment + "-01 00:00:00.0";
			if (month.equals("04")
					|| month.equals("06")
					|| month.equals("09")
					|| month.equals("11")) {
				valueMax = segment + "-30 23:59:59.999";
			}
			if (month.equals("01")
					|| month.equals("03")
					|| month.equals("05")
					|| month.equals("07")
					|| month.equals("08")
					|| month.equals("10")
					|| month.equals("12")) {
				valueMax = segment + "-31 23:59:59.999";
			}
			if (month.equals("02")) {
				valueMax = segment + "-" + getLastDateOfFebruary(segment.substring(0, 4)) + " 23:59:59.999";
			}
		}
		if (segment.length() == 8) {
			String month = segment.substring(segment.length()-3, segment.length()-1);
			valueMin = segment + "01 00:00:00.0";
			if (month.equals("04")
					|| month.equals("06")
					|| month.equals("09")
					|| month.equals("11")) {
				valueMax = segment + "30 23:59:59.999";
			}
			if (month.equals("01")
					|| month.equals("03")
					|| month.equals("05")
					|| month.equals("07")
					|| month.equals("08")
					|| month.equals("10")
					|| month.equals("12")) {
				valueMax = segment + "31 23:59:59.999";
			}
			if (month.equals("02")) {
				valueMax = segment + getLastDateOfFebruary(segment.substring(0, 4)) + " 23:59:59.999";
			}
		}
		if (segment.length() == 9) {
			String month = segment.substring(segment.length()-4, segment.length()-2);
			String lastChar = segment.substring(segment.length()-1, segment.length());
			if (lastChar.equals("0")) {
				valueMin = segment + "1 00:00:00.0";
				valueMax = segment + "9 23:59:59.999";
			}
			if (lastChar.equals("1")) {
				valueMin = segment + "0 00:00:00.0";
				valueMax = segment + "9 23:59:59.999";
			}
			if (lastChar.equals("2")) {
				valueMin = segment + "0 00:00:00.0";
				if (month.equals("02")) {
					String date = getLastDateOfFebruary(segment.substring(0, 4));
					valueMax = segment + date.substring(1, 2) + " 23:59:59.999";
				} else {
					valueMax = segment + "9 23:59:59.999";
				}
			}
			if (lastChar.equals("3")) {
				valueMin = segment + "0 00:00:00.0";
				if (month.equals("04")
						|| month.equals("06")
						|| month.equals("09")
						|| month.equals("11")) {
					valueMax = segment + "0 23:59:59.999";
				}
				if (month.equals("01")
						|| month.equals("03")
						|| month.equals("05")
						|| month.equals("07")
						|| month.equals("08")
						|| month.equals("10")
						|| month.equals("12")) {
					valueMax = segment + "1 23:59:59.999";
				}
			}
		}
		if (segment.length() == 10) {
			valueMin = segment + " 00:00:00.0";
			valueMax = segment + " 23:59:59.999";
		}
		if (segment.length() == 11) {
			valueMin = segment + "00:00:00.0";
			valueMax = segment + "23:59:59.999";
		}
		if (segment.length() == 12) {
			String lastChar = segment.substring(segment.length()-1, segment.length());
			if (lastChar.equals("0") || lastChar.equals("1")) {
				valueMin = segment + "0:00:00.0";
				valueMax = segment + "9:59:59.999";
			}
			if (lastChar.equals("2")) {
				valueMin = segment + "0:00:00.0";
				valueMax = segment + "3:59:59.999";
			}
		}
		if (segment.length() == 13) {
			valueMin = segment + ":00:00.0";
			valueMax = segment + ":59:59.999";
		}
		if (segment.length() == 14) {
			valueMin = segment + "00:00.0";
			valueMax = segment + "59:59.999";
		}
		if (segment.length() == 15) {
			valueMin = segment + "0:00.0";
			valueMax = segment + "9:59.999";
		}
		if (segment.length() == 16) {
			valueMin = segment + ":00.0";
			valueMax = segment + ":59.999";
		}
		if (segment.length() == 17) {
			valueMin = segment + "00.0";
			valueMax = segment + "59.999";
		}
		if (segment.length() == 18) {
			valueMin = segment + "0.0";
			valueMax = segment + "9.999";
		}
		if (segment.length() == 19) {
			valueMin = segment + ".0";
			valueMax = segment + ".999";
		}
		if (segment.length() == 20) {
			valueMin = segment + "0";
			valueMax = segment + "999";
		}
		if (segment.length() == 21) {
			valueMin = segment;
			valueMax = segment + "99";
		}
		if (segment.length() == 22) {
			valueMin = segment;
			valueMax = segment + "9";
		}
		if (segment.length() == 23) {
			valueMin = segment;
			valueMax = segment;
		}

		/////////////////////////////////////////////////
		// Setup where value according to operand type //
		/////////////////////////////////////////////////
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		format.setLenient(false);
		try {
			format.parse(valueMin);
			format.parse(valueMax);

			if (operandType.equals("GENERIC")) {
				bf.append(" BETWEEN '");
				bf.append(valueMin);
				bf.append("' AND '");
				bf.append(valueMax);
				bf.append("'");
			}
			if (operandType.equals("GE")) {
				bf.append(" >= '");
				bf.append(valueMin);
				bf.append("'");
			}
			if (operandType.equals("GT")) {
				bf.append(" > '");
				bf.append(valueMax);
				bf.append("'");
			}
			if (operandType.equals("LE")) {
				bf.append(" <= '");
				bf.append(valueMax);
				bf.append("'");
			}
			if (operandType.equals("LT")) {
				bf.append(" < '");
				bf.append(valueMin);
				bf.append("'");
			}
		} catch (ParseException e) {
		}

		return bf.toString();
	}
	
	static String getLastDateOfFebruary(String year) {
		Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(year));
        cal.set(Calendar.MONTH, Calendar.FEBRUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int date = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        return Integer.toString(date);
	}

	static public String getStringValueOfDateTime() {
		String monthStr = "";
		String dayStr = "";
		String underbar = "";
		String hourStr = "";
		String minStr = "";
		String secStr = "";
		String returnValue = "";

		GregorianCalendar calendar = new GregorianCalendar();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;
		if (month < 10) {
			monthStr = "0" + Integer.toString(month);
		} else {
			monthStr = Integer.toString(month);
		}
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if (day < 10) {
			dayStr = "0" + Integer.toString(day);
		} else {
			dayStr = Integer.toString(day);
		}
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour < 10) {
			hourStr = "0" + Integer.toString(hour);
		} else {
			hourStr = Integer.toString(hour);
		}
		int minute = calendar.get(Calendar.MINUTE);
		if (minute < 10) {
			minStr = "0" + Integer.toString(minute);
		} else {
			minStr = Integer.toString(minute);
		}
		int second = calendar.get(Calendar.SECOND);
		if (second < 10) {
			secStr = "0" + Integer.toString(second);
		} else {
			secStr = Integer.toString(second);
		}

		returnValue = Integer.toString(year) + monthStr + dayStr + underbar + hourStr + minStr + secStr;
		return returnValue;
	}

	static String getDayOfWeek(Calendar cal, String dateFormat) {
		String result = "";
		String language = dateFormat.substring(0, 2);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

		if (dayOfWeek == 1) {
			if (language.equals("jp")) {
				result = "(日)";
			}
			if (language.equals("en")) {
				result = "Sun,";
			}
		}
		if (dayOfWeek == 2) {
			if (language.equals("jp")) {
				result = "(月)";
			}
			if (language.equals("en")) {
				result = "Mon,";
			}
		}
		if (dayOfWeek == 3) {
			if (language.equals("jp")) {
				result = "(火)";
			}
			if (language.equals("en")) {
				result = "Tue,";
			}
		}
		if (dayOfWeek == 4) {
			if (language.equals("jp")) {
				result = "(水)";
			}
			if (language.equals("en")) {
				result = "Wed,";
			}
		}
		if (dayOfWeek == 5) {
			if (language.equals("jp")) {
				result = "(木)";
			}
			if (language.equals("en")) {
				result = "Thur,";
			}
		}
		if (dayOfWeek == 6) {
			if (language.equals("jp")) {
				result = "(金)";
			}
			if (language.equals("en")) {
				result = "Fri,";
			}
		}
		if (dayOfWeek == 7) {
			if (language.equals("jp")) {
				result = "(土)";
			}
			if (language.equals("en")) {
				result = "Sat,";
			}
		}

		return result;
	}
	
	static java.util.Date convertDateFromStringToUtil(String strDate) {
		java.util.Date utilDate = null;
		int pos1, pos2;
		int year = 0;
		int month = -1;
		int date = 0;
		Calendar cal = Calendar.getInstance();

		if (strDate != null) {
			try {
				pos1 = strDate.indexOf("-");
				if (pos1 > 0) {
					year = Integer.parseInt(strDate.substring(0, pos1));
					pos2 = strDate.indexOf("-", pos1 + 1);
					if (pos2 >= 0) {
						month = Integer.parseInt(strDate.substring(pos1+1, pos2)) - 1;
						date = Integer.parseInt(strDate.substring(pos2+1, pos2+3));
					}
				} else {
					pos1 = strDate.indexOf("/");
					if (pos1 > 0) {
						year = Integer.parseInt(strDate.substring(0, pos1));
						pos2 = strDate.indexOf("/", pos1 + 1);
						if (pos2 >= 0) {
							month = Integer.parseInt(strDate.substring(pos1+1, pos2)) - 1;
							date = Integer.parseInt(strDate.substring(pos2+1, pos2+3));
							cal.set(year, month, date);
							utilDate = cal.getTime();
						}
					} else {
						if (strDate.length() == 8) {
							year = Integer.parseInt(strDate.substring(0, 4));
							month = Integer.parseInt(strDate.substring(4, 6)) - 1;
							date = Integer.parseInt(strDate.substring(6, 8));
						}
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			if (year > 0 && month > -1 && date > 0) {
				cal.set(year, month, date);
				utilDate = cal.getTime();
			}
		}

		return utilDate;
	}
	
	static java.util.Date convertDateFromSqlToUtil(java.sql.Date sqlDate) {
		java.util.Date utilDate = null;
		if (sqlDate != null) {
			utilDate = new java.util.Date();
			long longTime = sqlDate.getTime();
			utilDate.setTime(longTime);
		}
		return utilDate;
	}

	static java.sql.Date convertDateFromUtilToSql(java.util.Date utilDate) {
		java.sql.Date sqlDate = null;
		if (utilDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(utilDate);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			sqlDate = new java.sql.Date(cal.getTimeInMillis());
		}
		return  sqlDate;
	}

	static String convertDateFromUtilToString(java.util.Date utilDate) {
		String strDate = "";
		SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
		if (utilDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(utilDate);
			strDate = yyyyMMdd.format(cal.getTime());
		}
		return  strDate;
	}
	
	public static Color convertStringToColor(String color) {
		Color colorConverted = Color.black; //Default//
		if (color.equals("black")) {
			colorConverted = Color.black;
		}
		if (color.equals("red")) {
			colorConverted = Color.red;
		}
		if (color.equals("blue")) {
			colorConverted = Color.blue;
		}
		if (color.equals("green")) {
			colorConverted = Color.green.darker();
		}
		if (color.equals("orange")) {
			colorConverted = Color.orange.darker();
		}
		if (color.equals("magenta")) {
			colorConverted = Color.magenta;
		}
		return colorConverted;
	}

	public static String convertColorToString(Color color) {
		String colorConverted = "black"; //Default//
		if (color.equals(Color.black)) {
			colorConverted = "black";
		}
		if (color.equals(Color.red)) {
			colorConverted = "red";
		}
		if (color.equals(Color.blue)) {
			colorConverted = "blue";
		}
		if (color.equals(Color.green.darker())) {
			colorConverted = "green";
		}
		if (color.equals(Color.orange.darker())) {
			colorConverted = "orange";
		}
		if (color.equals(Color.magenta)) {
			colorConverted = "magenta";
		}
		return colorConverted;
	}
	
	public static SortableDomElementListModel getSortedListModel(NodeList list, String attName) {
		SortableDomElementListModel sortableDomElementListModel = new SortableDomElementListModel(attName);
		for (int i = 0; i < list.getLength(); i++) {
			sortableDomElementListModel.addElement((Object)list.item(i));
		}
		sortableDomElementListModel.sortElements();
		return sortableDomElementListModel;
	}

	static void setCaptionToButton(JButton button, org.w3c.dom.Element element, String buttonText, int buttonWidth, Session session) {
        StringBuffer bf = new StringBuffer();
        bf.append("F");
		bf.append(element.getAttribute("Number"));
		bf.append(" ");
		if (buttonText.equals("")) {
			bf.append(element.getAttribute("Caption"));
		} else {
			bf.append(buttonText);
		}
		button.setText(bf.toString());
		button.setToolTipText(bf.toString());

		if (element.getAttribute("Action").contains("CALL")) {
			int pos1 = element.getAttribute("Action").indexOf("CALL(");
			if (pos1 >= 0) {
				int pos2 = element.getAttribute("Action").indexOf(")", pos1);
				String callAction = element.getAttribute("Action").substring(pos1+5, pos2);
				StringTokenizer workTokenizer1 = new StringTokenizer(callAction, "," );
				String functionID = "";
				if (workTokenizer1.countTokens() >= 1) {
					functionID = workTokenizer1.nextToken();
				}
				String functionName = session.getFunctionName(functionID);
				if (functionName.equals("")) {
					button.setToolTipText(functionID + " N/A");
				} else {
					button.setToolTipText(functionID + " " + functionName);
				}
			}
		}

		JButton dummy;
		for (int i = XFUtility.FONT_SIZE; i > 8; i--) {
			dummy = new JButton(bf.toString());
			dummy.setFont(new java.awt.Font(button.getFont().getFontName(), 0 , i));
			if (dummy.getPreferredSize().width <= buttonWidth-2) {
				button.setFont(new java.awt.Font(button.getFont().getFontName(), 0 , i));
				break;
			}
		}
	}
	
	public static void appendLog(String text, StringBuffer logBuf) {
		if (logBuf != null) {
			String wrkStr = logBuf.toString();
			if (!text.startsWith("select") || !wrkStr.contains(text)) {
				StringBuffer buf = new StringBuffer();
				buf.append("> ");
				buf.append(text);
				buf.append("  (");
				buf.append(TIME_FORMATTER.format(Calendar.getInstance().getTime()));
				buf.append(")\n");
				logBuf.append(buf.toString());
			}
		}
	}
	
	static KeyStroke getKeyStroke(String functionNumber) {
		KeyStroke keyStroke = null;
		if (functionNumber.equals("2")) {
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
		}
		if (functionNumber.equals("3")) {
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
		}
		if (functionNumber.equals("4")) {
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
		}
		if (functionNumber.equals("5")) {
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		}
		if (functionNumber.equals("6")) {
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
		}
		if (functionNumber.equals("7")) {
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
		}
		if (functionNumber.equals("8")) {
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
		}
		if (functionNumber.equals("9")) {
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
		}
		if (functionNumber.equals("10")) {
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
		}
		if (functionNumber.equals("11")) {
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0);
		}
		if (functionNumber.equals("12")) {
			keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
		}
		return keyStroke;
	}
	
	static String getMessageOfReturnCode(String code) {
		String message = "";
		if (code.equals("00")) {
			message = RESOURCE.getString("ReturnMessage00");
		}
		if (code.equals("01")) {
			message = RESOURCE.getString("ReturnMessage01");
		}
		if (code.equals("10")) {
			message = RESOURCE.getString("ReturnMessage10");
		}
		if (code.equals("11")) {
			message = RESOURCE.getString("ReturnMessage11");
		}
		if (code.equals("20")) {
			message = RESOURCE.getString("ReturnMessage20");
		}
		if (code.equals("21")) {
			message = RESOURCE.getString("ReturnMessage21");
		}
		if (code.equals("30")) {
			message = RESOURCE.getString("ReturnMessage30");
		}
		if (code.equals("99")) {
			message = RESOURCE.getString("ReturnMessage99");
		}
		return message;
	}
}

class ButtonAction extends AbstractAction {
	private static final long serialVersionUID = 1L;
	private JButton button_;
	public ButtonAction(JButton button){
		super();
		button_ = button;
	}
	public void actionPerformed(ActionEvent e){
		button_.doClick();
	}
}

class HorizontalAlignmentHeaderRenderer implements TableCellRenderer{
	private int horizontalAlignment = SwingConstants.LEFT;
	public HorizontalAlignmentHeaderRenderer(int horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
		JLabel l = (JLabel)r.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
		l.setHorizontalAlignment(horizontalAlignment);
		return l;
	}
}

class XFHashMap extends Object {
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private ArrayList<Object> keyValueList = new ArrayList<Object>();
	public void addValue(String fieldID, Object value) {
		if (keyFieldList.contains(fieldID)) {
			keyValueList.add(keyFieldList.indexOf(fieldID), value);
		} else {
			keyFieldList.add(fieldID);
			keyValueList.add(value);
		}
	}
	public Object getValue(String fieldID) {
		int index = keyFieldList.indexOf(fieldID);
		if (index == -1) {
			return null;
		} else {
			return keyValueList.get(index);
		}
	}
	public int size() {
		return keyFieldList.size();
	}
	public String getKeyIDByIndex(int index) {
		return keyFieldList.get(index);
	}
	public Object getValueByIndex(int index) {
		return keyValueList.get(index);
	}
	public HashMap<String, Object> getHashMap() {
		HashMap<String, Object> hashMap = new HashMap<String, Object>();
		for (int i = 0; i < keyFieldList.size(); i++) {
			hashMap.put(keyFieldList.get(i), keyValueList.get(i));
		}
		return hashMap;
	}
}

//class XFSessionForScript {
//	private Session session_;
//	
//    public XFSessionForScript(Session session) {
//        session_ = session;
//    }
//    
//    public String getAttribute(String id) {
//		return session_.getAttribute(id);
//	}
//    public void setAttribute(String id, String value) {
//		session_.setAttribute(id, value);
//	}
//
//    public String getImageFileFolder() {
//		return session_.getImageFileFolder();
//	}
//	public String getDatabaseUser() {
//		return session_.getDatabaseUser();
//	}
//	public String getSessionID() {
//		return session_.getSessionID();
//	}
//	public String getUserEmployeeNo() {
//		return session_.getUserEmployeeNo();
//	}
//	public String getUserEmailAddress() {
//		return session_.getUserEmailAddress();
//	}
//	public String getUserID() {
//		return session_.getUserID();
//	}
//	public String getUserName() {
//		return session_.getUserName();
//	}
//
//	public float getAnnualExchangeRate(String currency, int fYear, String type) {
//		return session_.getAnnualExchangeRate(currency, fYear, type);
//	}
//	public float getMonthlyExchangeRate(String currency, int fYear, int mSeq, String type) {
//		return session_.getMonthlyExchangeRate(currency, fYear, mSeq, type);
//	}
//	public float getMonthlyExchangeRate(String currency, String date, String type) {
//		return session_.getMonthlyExchangeRate(currency, date, type);
//	}
//
//	public String getNextNumber(String id) {
//		return session_.getNextNumber(id);
//	}
//	public void setNextNumber(String id, int nextNumber) {
//		session_.setNextNumber(id, nextNumber);
//	}
//	public float getSystemVariantFloat(String id) {
//		return session_.getSystemVariantFloat(id);
//	}
//	public int getSystemVariantInteger(String id) {
//		return session_.getSystemVariantInteger(id);
//	}
//	public String getSystemVariantString(String id) {
//		return session_.getSystemVariantString(id);
//	}
//	public void setSystemVariant(String id, String value) {
//		session_.setSystemVariant(id, value);
//	}
//	public int getTaxAmount(String date, int amount) {
//		return session_.getTaxAmount(date, amount);
//	}
//
//	public int getDaysBetweenDates(String dateFrom, String dateThru, int countType) {
//		return session_.getDaysBetweenDates(dateFrom, dateThru, countType);
//	}
//	public int getDaysBetweenDates(String dateFrom, String dateThru, int countType, String kbCalendar) {
//		return session_.getDaysBetweenDates(dateFrom, dateThru, countType, kbCalendar);
//	}
//	public Object getMinutesBetweenTimes(String timeFrom, String timeThru) {
//		return session_.getMinutesBetweenTimes(timeFrom, timeThru);
//	}
//	public String getOffsetDate(String date, int days, int countType) {
//		return session_.getOffsetDate(date, days, countType);
//	}
//	public String getOffsetDate(String date, int days, int countType, String kbCalendar) {
//		return session_.getOffsetDate(date, days, countType, kbCalendar);
//	}
//	public String getOffsetDateTime(String dateFrom, String timeFrom, int minutes, int countType) {
//		return session_.getOffsetDateTime(dateFrom, timeFrom, minutes, countType);
//	}
//	public String getOffsetYearMonth(String yearMonthFrom, int offsetMonths) {
//		return session_.getOffsetYearMonth(yearMonthFrom, offsetMonths);
//	}
//	public String getTimeStamp() {
//		return session_.getTimeStamp();
//	}
//	public String getThisMonth() {
//		return session_.getThisMonth();
//	}
//	public String getToday() {
//		return session_.getToday();
//	}
//	public boolean isOffDate(String date) {
//		return session_.isOffDate(date);
//	}
//	public boolean isOffDate(String date, String kbCalendar) {
//		return session_.isOffDate(date, kbCalendar);
//	}
//	public boolean isValidDate(String date) {
//		return session_.isValidDate(date);
//	}
//	public boolean isValidDateFormat(String date, String separator) {
//		return session_.isValidDateFormat(date, separator);
//	}
//	public boolean isValidTime(String time, String format) {
//		return session_.isValidTime(time, format);
//	}
//	public int getMSeqOfDate(String date) {
//		return session_.getMSeqOfDate(date);
//	}
//	public int getFYearOfDate(String date) {
//		return session_.getFYearOfDate(date);
//	}
//	public String getYearMonthOfFYearMSeq(String fYearMSeq) {
//		return session_.getYearMonthOfFYearMSeq(fYearMSeq);
//	}
//	public String getErrorOfAccountDate(String date) {
//		return session_.getErrorOfAccountDate(date);
//	}
//	
//	public boolean existsFile(String fileName) {
//		return session_.existsFile(fileName);
//	}
//	public boolean deleteFile(String fileName) {
//		return session_.deleteFile(fileName);
//	}
//	public boolean renameFile(String currentName, String newName) {
//		return session_.renameFile(currentName, newName);
//	}
//	public XFTextFileOperator createTextFileOperator(String oparation, String fileName, String separator, String charset) {
//		return session_.createTextFileOperator(oparation, fileName, separator, charset);
//	}
//	public XFTextFileOperator createTextFileOperator(String oparation, String fileName, String separator) {
//		return session_.createTextFileOperator(oparation, fileName, separator, "");
//	}
//
//	public void executeProgram(String programName) {
//		session_.executeProgram(programName);
//	}
//	public void browseFile(String fileName) {
//		session_.browseFile(fileName);
//	}
//	public void editFile(String fileName) {
//		session_.editFile(fileName);
//	}
//	public void sendMail(String addressFrom, String addressTo, String addressCc,
//			String subject, String message,
//			String fileName, String attachedName, String charset) {
//		session_.sendMail(addressFrom, addressTo, addressCc,
//				subject, message, fileName, attachedName, charset);
//	}
//	
//	public void startProgress(String text, int max) {
//		session_.startProgress(text, max);
//	}
//	public void incrementProgress() {
//		session_.incrementProgress();
//	}
//	public void endProgress() {
//		session_.endProgress();
//	}
//
//	public XFTableOperator createTableOperator(String oparation, String tableID) {
//		XFTableOperator operator = null;
//		try {
//			operator = new XFTableOperator(session_, null, oparation, tableID);
//		} catch (Exception e) {
//		}
//		return operator;
//	}
//	public XFTableOperator createTableOperator(String sqlText) {
//		return new XFTableOperator(session_, null, sqlText);
//	}
//	public void compressTable(String tableID) throws Exception {
//		session_.compressTable(tableID);
//	}
//    public void commit() {
//    	session_.commit();
//    }
//}

//public interface XFExecutable {
//	public String getFunctionID();
//	public boolean isAvailable();
//	public HashMap<String, Object> execute(HashMap<String, Object> parameterList);
//	public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parameterList);
//	public void setFunctionSpecifications(org.w3c.dom.Element functionElement) throws Exception;
//	public void startProgress(String text, int maxValue);
//	public void incrementProgress();
//	public void endProgress();
//}

//public interface XFScriptable {
//	public void cancelWithMessage(String message);
//	public void cancelWithException(Exception e);
//	public void cancelWithScriptException(ScriptException e, String scriptName);
//	public void callFunction(String functionID);
//	public void commit();
//	public void rollback();
//	public String getFunctionID();
//	public HashMap<String, Object> getParmMap();
//	public HashMap<String, Object> getReturnMap();
//	public void setProcessLog(String value);
//	public StringBuffer getProcessLog();
//	public XFTableOperator createTableOperator(String oparation, String tableID);
//	public XFTableOperator createTableOperator(String sqlText);
//	public Object getFieldObjectByID(String tableID, String fieldID);
//	public boolean isAvailable();
//}

interface XFTableColumnEditor {
	public Object getInternalValue();
	public Object getExternalValue();
	public void setBorder(Border border);
	public void setBackground(Color color);
	public void setHorizontalAlignment(int alignment);
	public void setBounds(Rectangle rec);
	public void setColorOfError();
	public void setColorOfNormal(int row);
	public void setEnabled(boolean ednabled);
	public void setEditable(boolean isEditable);
	public void setFocusable(boolean isFocusable);
	public void setValue(Object value);
	public void requestFocus();
	public boolean hasFocus();
	public boolean isEnabled();
	public boolean isEditable();
}

interface XFEditableField {
	public void setEnabled(boolean ednabled);
	public void setEditable(boolean editable);
	public void setFocusable(boolean focusable);
	public void setToolTipText(String text);
	public void requestFocus();
	public boolean isEnabled();
	public boolean isEditable();
	public boolean isComponentFocusable();
	public void setValue(Object obj);
	public void setOldValue(Object obj);
	public Object getInternalValue();
	public Object getOldValue();
	public Object getExternalValue();
	public void setBackground(Color color);
	public void setForeground(Color color);
	public int getWidth();
	public void setWidth(int width);
	public int getHeight();
	public int getRows();
	public void setLocation(int x, int y);
}

class XFImageField extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private int size_ = 10;
	private int rows_;
	private JTextField jTextField = new JTextField();
	private JScrollPane jScrollPane = new JScrollPane();
	private JPanel jPanelBottom = new JPanel();
	private JLabel jLabelImage;
	private ImageIcon imageIcon = null;
	private JButton jButton = new JButton();
	private boolean isEditable = false;
	private String fieldOptions_ = "";
	private String imageFileFolder_ = "";
    private Color normalModeColor = null;
    private static int DEFAULT_ROWS = 11;
    private static int DEFAULT_WIDTH = 400;
	private final int FIELD_VERTICAL_MARGIN = 5;
	private int fieldHeight = 0;
	private int fieldWidth = 0;
	private String oldValue = "";
	private String fontName_ = "";
	private Desktop desktop = Desktop.getDesktop();

	public XFImageField(String fieldOptions, int size, String imageFileFolder, String fontName){
		super();

		String wrkStr;

		fieldOptions_ = fieldOptions;
		size_ = size;
		imageFileFolder_ = imageFileFolder;
		fontName_ = fontName;

		jTextField.setEditable(false);
		Border workBorder = jTextField.getBorder();
		normalModeColor = jTextField.getBackground();
		jTextField.setBorder(BorderFactory.createLineBorder(normalModeColor));
		jTextField.setBackground(Color.white);
		jTextField.setEditable(true);
		jTextField.setFont(new java.awt.Font(fontName_, 0, XFUtility.FONT_SIZE));
		jTextField.setDocument(new LimitDocument());

		jButton.setFont(new java.awt.Font(fontName_, 0, XFUtility.FONT_SIZE));
		jButton.setPreferredSize(new Dimension(35, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.setIcon(XFUtility.ICON_REFRESH);
		jButton.addActionListener(new XFImageField_jButton_actionAdapter(this));
		jButton.addKeyListener(new XFImageField_jButton_keyAdapter(this));
		jPanelBottom.setPreferredSize(new Dimension(200, XFUtility.FIELD_UNIT_HEIGHT));
	    jScrollPane.setBorder(null);
	    jPanelBottom.setLayout(new BorderLayout());
	    jPanelBottom.add(jTextField, BorderLayout.CENTER);
	    jPanelBottom.add(jButton, BorderLayout.EAST);

	    this.setBorder(workBorder);
		this.setLayout(new BorderLayout());
		this.add(jPanelBottom, BorderLayout.SOUTH);
		this.add(jScrollPane, BorderLayout.CENTER);

		rows_ = DEFAULT_ROWS;
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "ROWS");
		if (!wrkStr.equals("")) {
			rows_ = Integer.parseInt(wrkStr);
		}
		fieldHeight = rows_ * (XFUtility.FIELD_UNIT_HEIGHT + FIELD_VERTICAL_MARGIN) - FIELD_VERTICAL_MARGIN;

		fieldWidth = DEFAULT_WIDTH;
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}

		this.setSize(fieldWidth, fieldHeight);
		this.setEditable(false);
	}

	public void setEditable(boolean editable) {
		if (editable) {
			this.add(jPanelBottom, BorderLayout.SOUTH);
		} else {
			this.remove(jPanelBottom);
			this.setFocusable(false);
		}
		isEditable = editable;
	}

	public void setWidth(int width) {
		this.setSize(width, this.getHeight());
	}

	public void setToolTipText(String text) {
		jTextField.setToolTipText(text);
	}

	public Object getInternalValue() {
		return jTextField.getText();
	}

	public void setOldValue(Object value) {
		oldValue = value.toString();
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getExternalValue() {
		return imageFileFolder_ + jTextField.getText();
	}

	public void setValue(Object obj) {
		if (obj == null) {
			setupImage("");
		} else {
			setupImage(obj.toString().trim());
		}
	}

	void setupImage(String imageFileName) {
		jTextField.setText(imageFileName);
		imageIcon = null;
		boolean isExistingFile = true;

		////////////////////////////
		// Setup JLabel with Icon //
		////////////////////////////
		String fullName = imageFileFolder_ + imageFileName;
		if (imageFileName.equals("")) {
			jLabelImage = new JLabel();
			jLabelImage.setHorizontalAlignment(SwingConstants.CENTER);
			isExistingFile = false;
		} else {
			if (fullName.startsWith("http://")) {
				try{
					imageIcon = XFUtility.createSmallIcon(fullName, fieldWidth-15, fieldHeight-30);
				}catch(Exception e){
				}
			} else {
				File imageFile = new File(fullName);
				if (imageFile.exists()) {
					imageIcon = XFUtility.createSmallIcon(fullName, fieldWidth-15, fieldHeight-30);
				} else {
					isExistingFile = false;
				}
			}
			jLabelImage = new JLabel("", imageIcon, JLabel.CENTER);
		}
		jLabelImage.setFont(new java.awt.Font(fontName_, 0, XFUtility.FONT_SIZE));
		jLabelImage.setOpaque(true);

		/////////////////////////////////////
		// Setup text and bounds of JLabel //
		/////////////////////////////////////
		if (isExistingFile) {
			if (imageFileName.toUpperCase().contains(".GIF")
					|| imageFileName.toUpperCase().contains(".BMP")
					|| imageFileName.toUpperCase().contains(".JPG")
					|| imageFileName.toUpperCase().contains(".JPE")
					|| imageFileName.toUpperCase().contains(".JPEG")
					|| imageFileName.toUpperCase().contains(".PNG")) {
				jLabelImage.setText("");
				int wrkWidth = this.getWidth() - 50;
				int wrkHeight = this.getHeight() - 50;
				if (imageIcon.getIconWidth() > wrkWidth) {
					wrkWidth = imageIcon.getIconWidth();
				}
				if (imageIcon.getIconHeight() > wrkHeight) {
					wrkHeight = imageIcon.getIconHeight();
				}
	    		jLabelImage.setToolTipText("<html>" + imageFileName + "<br>" + XFUtility.RESOURCE.getString("ImageFileShown"));
		        jLabelImage.setPreferredSize(new Dimension(wrkWidth, wrkHeight));
			} else {
				jLabelImage.setText("<html>" + imageFileName + XFUtility.RESOURCE.getString("Colon") + XFUtility.RESOURCE.getString("ImageFileMessage1"));
		        jLabelImage.setPreferredSize(new Dimension(this.getWidth() - 50, this.getHeight() - 50));
			}
        	jLabelImage.addMouseListener(new jLabel_mouseAdapter(this));
        } else {
    		if (imageFileName.equals("")) {
    			jLabelImage.setText(XFUtility.RESOURCE.getString("ImageFileNotSpecified"));
    		} else {
    			jLabelImage.setText(XFUtility.RESOURCE.getString("ImageFileNotFound1") + fullName + XFUtility.RESOURCE.getString("ImageFileNotFound2"));
    		}
        }
		jScrollPane.getViewport().add(jLabelImage, null);
	}

	public int getRows() {
		return rows_;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public boolean isComponentFocusable() {
		return isEditable;
	}

	public boolean isFocusable() {
		return false;
	}

	public void setBackground(Color color) {
		if (jTextField != null) {
			jTextField.setBackground(color);
		}
	}

	void jButton_actionPerformed(ActionEvent e) {
		setupImage(jTextField.getText());
	}

	void jButton_keyPressed(KeyEvent e) {
	    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				jButton.doClick();
			}
		}
	}

	protected class LimitDocument extends PlainDocument{
		private static final long serialVersionUID = 1L;
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
			if(str == null){
				return ;
			}
			if(a != null) {
				super.insertString(offs, str, a);
				return;
			}
			if(!canInsertString(str)){
				return ;
			}
			super.insertString(offs, str, a);
		}
	}

	private boolean canInsertString(String str){
		if((jTextField.getText().length() + str.length()) > size_){
			return false;
		}
		return true;
	}

	class jLabel_mouseAdapter extends java.awt.event.MouseAdapter {
		XFImageField adaptee;
		jLabel_mouseAdapter(XFImageField adaptee) {
			this.adaptee = adaptee;
		}
		public void mouseClicked(MouseEvent e) {
			try {
				setCursor(new Cursor(Cursor.WAIT_CURSOR));
				String fullName = imageFileFolder_ + jTextField.getText();
				if (fullName.startsWith("http://")) {
					desktop.browse(new URI(fullName));
				} else {
					if (!fullName.equals("")) {
						File file = new File(fullName);
						desktop.browse(file.toURI());
					}
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Failed to browse the file.\n" + ex.getMessage());
			} finally {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		}
		public void mousePressed(MouseEvent e) {
		}
		public void mouseReleased(MouseEvent e) {
		}
		public void mouseEntered(MouseEvent e) {
			setCursor(new Cursor(Cursor.HAND_CURSOR));
		}
		public void mouseExited(MouseEvent e) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}
}

class XFDateField extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private int rows_ = 1;
	private DateTextField dateTextField = new DateTextField();
	private JButton jButton = new JButton();
	private boolean isEditable = false;
    private java.util.Date date;
    private Session session_;
    private Object oldValue = null;

	public XFDateField(Session session){
		super();

		session_ = session;
		dateTextField.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		dateTextField.setBorder(null);
		dateTextField.setEditable(false);
		dateTextField.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent event) {
				if (dateTextField.getText().equals("")) {
					jButton.requestFocus();
				} else {
					if (isEditable) {
						dateTextField.selectAll();
					}
				}
			} 
			public void focusLost(FocusEvent event) {
				dateTextField.select(0, 0);
			} 
		});
		dateTextField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (isEditable && event.getKeyCode() == KeyEvent.VK_DELETE) {
					setUtilDateValue(null);
					jButton.requestFocus();
				}
				if (isEditable && event.getKeyCode() == KeyEvent.VK_ENTER) {
					jButton.requestFocus();
				}
			} 
		});

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.util.Date selectedValue = getDateOnCalendar(date);
				setUtilDateValue(selectedValue);
			}
		});
		jButton.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jButton.doClick();
					}
				}
			} 
		});

		this.setSize(new Dimension(XFUtility.getWidthOfDateValue(session.getDateFormat(), session_.systemFont, XFUtility.FONT_SIZE) + 26, XFUtility.FIELD_UNIT_HEIGHT));
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		this.add(dateTextField, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
	}
	
	public void setInternalBorder(Border border) {
		dateTextField.setBorder(border);
	}
	
	public void addActionListener(ActionListener listener) {
		jButton.addActionListener(listener);
	}
	
	public void addKeyListener(KeyAdapter adapter) {
		jButton.addKeyListener(adapter);
		dateTextField.addKeyListener(adapter);
	}

	public void setEditable(boolean editable) {
		if (editable) {
			this.add(jButton, BorderLayout.EAST);
			dateTextField.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE-2));
			dateTextField.setBackground(SystemColor.text);
			dateTextField.setFocusable(true);
		} else {
			this.remove(jButton);
			dateTextField.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
			dateTextField.setBackground(SystemColor.control);
			dateTextField.setFocusable(false);
		}
		isEditable = editable;
	}
	
	public void setToolTipText(String text) {
		dateTextField.setToolTipText(text);
	}

	public void setFont(java.awt.Font font) {
		if (dateTextField != null) {
			dateTextField.setFont(font);
		}
	}
	
	public void requestFocus() {
		jButton.requestFocus();
	}

	public Object getInternalValue() {
		if (dateTextField.getText().equals("")) {
			return null;
		} else {
			return XFUtility.convertDateFromUtilToString(this.date);
		}
	}

	public Object getExternalValue() {
		return dateTextField.getText();
	}

	public java.util.Date getDate() {
		return this.date;
	}
	
	public void setValue(Object obj) {
		dateTextField.setText("");
		if (obj == null) {
			this.date = null;
		} else {
			if (obj.getClass().getName().equals("java.sql.Date")) {
				this.date = XFUtility.convertDateFromSqlToUtil((java.sql.Date)obj);
				dateTextField.setText(XFUtility.getUserExpressionOfUtilDate(this.date, session_.getDateFormat(), false));
			}
			if (obj.getClass().getName().equals("java.sql.Timestamp")) {
				this.date = XFUtility.convertDateFromStringToUtil(obj.toString().substring(0, 10));
				dateTextField.setText(XFUtility.getUserExpressionOfUtilDate(this.date, session_.getDateFormat(), false));
			}
			if (obj.getClass().getName().equals("java.util.Date")) {
				this.date = (java.util.Date)obj;
				dateTextField.setText(XFUtility.getUserExpressionOfUtilDate(this.date, session_.getDateFormat(), false));
			}
			if (obj.getClass().getName().equals("java.lang.String")) {
				if (obj.equals("")) {
					this.date = null;
				} else {
					this.date = XFUtility.convertDateFromStringToUtil((String)obj);
					dateTextField.setText(XFUtility.getUserExpressionOfUtilDate(this.date, session_.getDateFormat(), false));
				}
			}
		}
		jButton.setToolTipText(dateTextField.getText());
	}
	
	public void setWidth(int width) {
	}
	
	public void setNarrower(int width) {
		int workWidth;
		if (this.getWidth() > width) {
			for (int fontSize = XFUtility.FONT_SIZE; fontSize >= 9; fontSize--) {
				workWidth = XFUtility.getWidthOfDateValue(session_.getDateFormat(), session_.systemFont, fontSize) + 26;
				if (workWidth <= width || fontSize == 9) {
					dateTextField.setFont(new java.awt.Font(session_.systemFont, 0, fontSize));
					break;
				}
			}
			this.setSize(new Dimension(width, XFUtility.FIELD_UNIT_HEIGHT));
		}
	}

	public void setOldValue(Object obj) {
		oldValue = obj;
	}

	public Object getOldValue() {
		return oldValue;
	}
	
	public void setUtilDateValue(java.util.Date utilDate) {
		this.date = utilDate;
		if (this.date == null) {
			dateTextField.setText("");
		} else {
			dateTextField.setText(XFUtility.getUserExpressionOfUtilDate(utilDate, session_.getDateFormat(), false));
		}
		jButton.setToolTipText(dateTextField.getText());
	}

	public int getRows() {
		return rows_;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public boolean isComponentFocusable() {
		return isEditable;
	}

	public boolean isFocusable() {
		return false;
	}

	public void setForeground(Color color) {
		if (dateTextField != null) {
			dateTextField.setForeground(color);
		}
	}

	public void setBackground(Color color) {
		if (dateTextField != null) {
			dateTextField.setBackground(color);
		}
	}
	
	public java.util.Date getDateOnCalendar(java.util.Date date) {
		Point position = jButton.getLocationOnScreen();
		position.x = position.x + Math.round(jButton.getWidth() / 2);
		position.y = position.y + Math.round(jButton.getHeight() / 2);
		return session_.getDateOnCalendar(date, "", position);
	}
	
	class DateTextField extends JTextField {
		private static final long serialVersionUID = 1L;
		public boolean isFocusable() {
			if (getText().equals("")) {
				return false;
			} else {
				return true;
			}
		}
	}
}

class XFByteArray extends Object {
    private String type_ = "null";
    private Object value_ = null;
    private XFFieldScriptable typeColumn_ = null;
    private XFFieldScriptable typeField_ = null;

	public XFByteArray(Object value){
		value_ = value;
	}

	public void setValue(Object value) {
		value_ = value;
	}
	
	public void setType(String type) {
		type_ = type;
		if (typeColumn_ != null) {
			typeColumn_.setValue(type_);
		}
		if (typeField_ != null) {
			typeField_.setValue(type_);
		}
	}
	
	public String getType() {
		return type_;
	}

	public Object getInternalValue() {
		return value_;
	}

	public void setInternalValue(Object value){
		value_ = value;
	}

	public Object getExternalValue() {
		return "<"+ type_ +">";
	}
	
	public void setTypeColumn(XFFieldScriptable typeColumn) {
		typeColumn_ = typeColumn;
		type_ = typeColumn_.getValue().toString();
	}
	
	public XFFieldScriptable getTypeColumn() {
		return typeColumn_;
	}
	
	public void setTypeField(XFFieldScriptable typeField) {
		typeField_ = typeField;
		type_ = typeField_.getValue().toString();
	}
	
	public XFFieldScriptable getTypeField() {
		return typeField_;
	}
}

//class XFByteaField extends JPanel implements XFEditableField {
//	private static final long serialVersionUID = 1L;
//	private int rows_ = 1;
//	private String typeFieldID_ = "";
//	private XFFieldScriptable typeField_ = null;
//	private XFColumnScriptable typeColumn_ = null;
//	private String fieldOptions_ = "";
//	private JLabel jLabel = new JLabel();
//	private JButton jButton = new JButton();
//	private String type_ = "null";
//	private Object value_ = null;
//	private Object oldValue_ = null;
//	private boolean isEditable_ = false;
//	private Session session_;
//
//	public XFByteaField(String typeFieldID, String fieldOptions, Session session) {
//		super();
//
//		typeFieldID_ = typeFieldID;
//		fieldOptions_ = fieldOptions;
//		session_ = session;
//
//		jLabel.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
//		jLabel.setText(this.getExternalValue().toString());
//
//		ImageIcon imageIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("prompt.png"));
//	 	jButton.setIcon(imageIcon);
//		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
//		jButton.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				processBytea();
//			}
//		});
//		jButton.addKeyListener(new KeyAdapter() {
//			public void keyPressed(KeyEvent e) {
//			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
//					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//						jButton.doClick();
//					}
//				}
//			} 
//		});
//
//		int fieldWidth = 100;
//		int fieldHeight = XFUtility.FIELD_UNIT_HEIGHT;
//		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "WIDTH");
//		if (!wrkStr.equals("")) {
//			fieldWidth = Integer.parseInt(wrkStr);
//		}
//		JTextField field = new JTextField();
//		this.setBorder(field.getBorder());
//		this.setLayout(new BorderLayout());
//		this.add(jLabel, BorderLayout.CENTER);
//		this.add(jButton, BorderLayout.EAST);
//		this.setSize(new Dimension(fieldWidth, fieldHeight));
//		this.setFocusable(true);
//		this.addFocusListener(new java.awt.event.FocusAdapter() {
//			public void focusGained(FocusEvent e) {
//				jButton.requestFocus();
//			}
//		});
//	}
//
//	public boolean isComponentFocusable() {
//		return this.isFocusable();
//	}
//
//	public boolean isEditable() {
//		return isEditable_;
//	}
//	
//	public void setWidth(int width) {
//		this.setSize(width, this.getHeight());
//	}
//	
//	public void setEditable(boolean isEditable) {
//		isEditable_ = isEditable;
//	}
//	
//	public void setTypeField(XFFieldScriptable typeField) {
//		if (typeField != null) {
//			typeField_ = typeField;
//			String work = typeField_.getValue().toString();
//			if (!work.equals("")) {
//				type_ = work;
//				jLabel.setText(this.getExternalValue().toString());
//			}
//		}
//	}
//	
//	public void setTypeColumn(XFColumnScriptable typeColumn) {
//		if (typeColumn != null) {
//			typeColumn_ = typeColumn;
//			String work = typeColumn_.getValue().toString();
//			if (!work.equals("")) {
//				type_ = work;
//				jLabel.setText(this.getExternalValue().toString());
//			}
//		}
//	}
//
//	public Object getInternalValue() {
//		return value_;
//	}
//	
//	public void setOldValue(Object value) {
//		oldValue_ = (XFByteArray)value;
//	}
//
//	public Object getOldValue() {
//		return oldValue_;
//	}
//
//	public Object getExternalValue() {
//		return "<"+ type_ + ">";
//	}
//	
//	public void setValue(Object value) {
////		value_ = value;
//		//value_.setValue(value);
////		if (value_ == null) {
////			type_ = "null";
////			if (typeField_ != null) {
////				typeField_.setValue("");
////			}
////			if (typeColumn_ != null) {
////				typeColumn_.setValue("");
////			}
////			jLabel.setText(this.getExternalValue().toString());
////		} else {
////			if (value_.toString().equals("")) {
////				type_ = "null";
////				if (typeField_ != null) {
////					typeField_.setValue("");
////				}
////				if (typeColumn_ != null) {
////					typeColumn_.setValue("");
////				}
////				jLabel.setText(this.getExternalValue().toString());
////			} else {
////				value_ = new XFByteArray(value);
////			}
////		}
//		if (value == null) {
//			type_ = "null";
//			if (typeField_ != null) {
//				typeField_.setValue("");
//			}
//			if (typeColumn_ != null) {
//				typeColumn_.setValue("");
//			}
//			jLabel.setText(this.getExternalValue().toString());
//		} else {
//			if (value.toString().equals("")) {
//				type_ = "null";
//				if (typeField_ != null) {
//					typeField_.setValue("");
//				}
//				if (typeColumn_ != null) {
//					typeColumn_.setValue("");
//				}
//				jLabel.setText(this.getExternalValue().toString());
//			}
//		}
//		value_ = new XFByteArray(value);
//	}
//	
//	public int getRows() {
//		return rows_;
//	}
//
//	public String getTypeFieldID() {
//		return typeFieldID_;
//	}
//	
//	public boolean isUpdated() {
//		return (oldValue_ != value_);
//	}
//	
//	private void processBytea() {
//		if (isEditable_) {
//			Object[] bts = {XFUtility.RESOURCE.getString("ByteaFieldMessage1"), XFUtility.RESOURCE.getString("ByteaFieldMessage2"), XFUtility.RESOURCE.getString("ByteaFieldMessage3"), XFUtility.RESOURCE.getString("Close")};
//			int rtn = JOptionPane.showOptionDialog(session_, XFUtility.RESOURCE.getString("ByteaFieldMessage5"),
//					XFUtility.RESOURCE.getString("ByteaFieldMessage4"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
//			
//			///////////////////////////
//			// Browsing file content //
//			///////////////////////////
//			if (rtn == 0) {
//				browseBytea();
//			}
//
//			/////////////////////////////////////////////
//			// Uploading file content into BYTEA field //
//			/////////////////////////////////////////////
//			if (rtn == 1) {
//				session_.jFileChooser.resetChoosableFileFilters();
//				session_.jFileChooser.setDialogTitle(XFUtility.RESOURCE.getString("ByteaFieldMessage6"));
//				int reply = session_.jFileChooser.showDialog(session_, XFUtility.RESOURCE.getString("ByteaFieldMessage7"));
//				if (reply == JFileChooser.APPROVE_OPTION) {
//					try {
//						File file = new File(session_.jFileChooser.getSelectedFile().getPath());
//						int point = file.getAbsolutePath().lastIndexOf(".");
//						if (point == -1) {
//							JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage8"));
//						} else {
//							type_ = file.getAbsolutePath().substring(point + 1);
//							if (typeField_ != null) {
//								typeField_.setValue(type_);
//							}
//							if (typeColumn_ != null) {
//								typeColumn_.setValue(type_);
//							}
//							jLabel.setText(this.getExternalValue().toString());
//							FileInputStream inputStream = new FileInputStream(file);
//							byte[] binaryByteArray = new byte[(int) file.length()];
//							inputStream.read(binaryByteArray);
//							inputStream.close();
//							setValue(Base64.encodeBase64(binaryByteArray));
//						}
//					} catch (Exception e) {
//						JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage9") + e.getMessage());
//						e.printStackTrace();
//					}
//				}
//			}
//
//			///////////////////
//			// Clear content //
//			///////////////////
//			if (rtn == 2) {
//				setValue(null);
//			}
//		} else {
//
//			///////////////////////////
//			// Browsing file content //
//			///////////////////////////
//			browseBytea();
//		}
//	}
//
//	private void browseBytea() {
//		if (type_.equals("bin")) {
//			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage10"));
//		} else {
//		    try {
//		    	byte[] base64ByteArray = null;
//		    	if (value_ instanceof XFByteArray) {
//			    	XFByteArray xfByteArray = (XFByteArray)value_;
//			    	base64ByteArray = (byte[])xfByteArray.getInternalValue();
//		    	} else {
//		    		base64ByteArray = (byte[])value_;
//		    	}
//				byte[] binaryByteArray = Base64.decodeBase64(base64ByteArray);
//				File binaryFile = session_.createTempFile("BINARY", "." + type_);
//				FileOutputStream outputStream = new FileOutputStream(binaryFile);
//				outputStream.write(binaryByteArray);
//				outputStream.close();	
//				session_.getDesktop().browse(binaryFile.toURI());
//			} catch (Exception e) {
//				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage11") + e.getMessage());
//			}
//		}
//	}
//}

class XFByteaField extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private int rows_ = 1;
	private String typeFieldID_ = "";
	private XFFieldScriptable typeField_ = null;
	//private XFColumnScriptable typeColumn_ = null;
	private String fieldOptions_ = "";
	private JLabel jLabel = new JLabel();
	private JButton jButton = new JButton();
	private String type_ = "null";
	private Object value_ = null;
	private Object oldValue_ = null;
	private boolean isEditable_ = false;
	private Session session_;

	public XFByteaField(String typeFieldID, String fieldOptions, Session session) {
		super();

		typeFieldID_ = typeFieldID;
		fieldOptions_ = fieldOptions;
		session_ = session;

		jLabel.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jLabel.setText(this.getExternalValue().toString());

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				processBytea();
			}
		});
		jButton.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jButton.doClick();
					}
				}
			} 
		});

		int fieldWidth = 100;
		int fieldHeight = XFUtility.FIELD_UNIT_HEIGHT;
		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}
		JTextField field = new JTextField();
		this.setBorder(field.getBorder());
		this.setLayout(new BorderLayout());
		this.add(jLabel, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
		this.setSize(new Dimension(fieldWidth, fieldHeight));
		this.setFocusable(true);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				jButton.requestFocus();
			}
		});
	}

	public boolean isComponentFocusable() {
		return this.isFocusable();
	}

	public boolean isEditable() {
		return isEditable_;
	}
	
	public void setWidth(int width) {
		this.setSize(width, this.getHeight());
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
	}
	
	public void setTypeField(XFFieldScriptable typeField) {
		if (typeField != null) {
			typeField_ = typeField;
			String work = typeField_.getValue().toString();
			if (!work.equals("")) {
				type_ = work;
				jLabel.setText(this.getExternalValue().toString());
			}
		}
	}
	
//	public void setTypeColumn(XFColumnScriptable typeColumn) {
//		if (typeColumn != null) {
//			typeColumn_ = typeColumn;
//			String work = typeColumn_.getValue().toString();
//			if (!work.equals("")) {
//				type_ = work;
//				jLabel.setText(this.getExternalValue().toString());
//			}
//		}
//	}

	public Object getInternalValue() {
		return value_;
	}
	
	public void setOldValue(Object value) {
		oldValue_ = value;
	}

	public Object getOldValue() {
		return oldValue_;
	}

	public Object getExternalValue() {
		return "<"+ type_ + ">";
	}
	
	public void setValue(Object value) {
		value_ = value;
		if (value_ == null) {
			type_ = "null";
			if (typeField_ != null) {
				typeField_.setValue("");
			}
//			if (typeColumn_ != null) {
//				typeColumn_.setValue("");
//			}
			jLabel.setText(this.getExternalValue().toString());
		} else {
			if (value_.toString().equals("")) {
				type_ = "null";
				if (typeField_ != null) {
					typeField_.setValue("");
				}
//				if (typeColumn_ != null) {
//					typeColumn_.setValue("");
//				}
				jLabel.setText(this.getExternalValue().toString());
			}
		}
	}
	
	public int getRows() {
		return rows_;
	}

	public String getTypeFieldID() {
		return typeFieldID_;
	}
	
	public boolean isUpdated() {
		return (oldValue_ != value_);
	}
	
	private void processBytea() {
		if (isEditable_) {
			Object[] bts = {XFUtility.RESOURCE.getString("ByteaFieldMessage1"), XFUtility.RESOURCE.getString("ByteaFieldMessage2"), XFUtility.RESOURCE.getString("ByteaFieldMessage3")};
			int rtn = JOptionPane.showOptionDialog(session_, XFUtility.RESOURCE.getString("ByteaFieldMessage5"),
					XFUtility.RESOURCE.getString("ByteaFieldMessage4"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
			
			///////////////////////////
			// Browsing file content //
			///////////////////////////
			if (rtn == 0) {
				browseBytea();
			}

			/////////////////////////////////////////////
			// Uploading file content into BYTEA field //
			/////////////////////////////////////////////
			if (rtn == 1) {
				session_.jFileChooser.resetChoosableFileFilters();
				session_.jFileChooser.setDialogTitle(XFUtility.RESOURCE.getString("ByteaFieldMessage6"));
				int reply = session_.jFileChooser.showDialog(session_, XFUtility.RESOURCE.getString("ByteaFieldMessage7"));
				if (reply == JFileChooser.APPROVE_OPTION) {
					try {
						File file = new File(session_.jFileChooser.getSelectedFile().getPath());
						int point = file.getAbsolutePath().lastIndexOf(".");
						if (point == -1) {
							JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage8"));
						} else {
							type_ = file.getAbsolutePath().substring(point + 1);
							if (typeField_ != null) {
								typeField_.setValue(type_);
							}
							jLabel.setText(this.getExternalValue().toString());
							FileInputStream inputStream = new FileInputStream(file);
							byte[] binaryByteArray = new byte[(int) file.length()];
							inputStream.read(binaryByteArray);
							inputStream.close();
							setValue(Base64.encodeBase64(binaryByteArray));
						}
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage9") + e.getMessage());
						e.printStackTrace();
					}
				}
			}

			///////////////////
			// Clear content //
			///////////////////
			if (rtn == 2) {
				setValue(null);
			}
		} else {

			///////////////////////////
			// Browsing file content //
			///////////////////////////
			browseBytea();
		}
	}

	private void browseBytea() {
		if (type_.equals("") || type_.equals("null")) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage10"));
		} else {
		    try {
	    		byte[] base64ByteArray = (byte[])value_;
	    		byte[] binaryByteArray = Base64.decodeBase64(base64ByteArray);
				File binaryFile = session_.createTempFile("BYTEA", "." + type_);
				FileOutputStream outputStream = new FileOutputStream(binaryFile);
				outputStream.write(binaryByteArray);
				outputStream.close();	
				session_.getDesktop().browse(binaryFile.toURI());
		    } catch (Exception e) {
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage11") + e.getMessage());
			}
		}
	}
}

class XFByteaColumn extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private int rows_ = 1;
	private String typeColumnID_ = "";
	private XFFieldScriptable typeColumn_ = null;
	private String fieldOptions_ = "";
	private JLabel jLabel = new JLabel();
	private JButton jButton = new JButton();
	private XFByteArray value_ = null;
	private XFByteArray oldValue_ = null;
	private boolean isEditable_ = false;
	private Session session_;

	public XFByteaColumn(String typeFieldID, String fieldOptions, Session session) {
		super();

		typeColumnID_ = typeFieldID;
		fieldOptions_ = fieldOptions;
		session_ = session;

		jLabel.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jLabel.setText(this.getExternalValue().toString());

		ImageIcon imageIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.setPreferredSize(new Dimension(26, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				processBytea();
			}
		});
		jButton.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
			    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						jButton.doClick();
					}
				}
			} 
		});

		int fieldWidth = 100;
		int fieldHeight = XFUtility.FIELD_UNIT_HEIGHT;
		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}
		JTextField field = new JTextField();
		this.setBorder(field.getBorder());
		this.setLayout(new BorderLayout());
		this.add(jLabel, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
		this.setSize(new Dimension(fieldWidth, fieldHeight));
		this.setFocusable(true);
		this.addFocusListener(new java.awt.event.FocusAdapter() {
			public void focusGained(FocusEvent e) {
				jButton.requestFocus();
			}
		});
	}

	public boolean isComponentFocusable() {
		return this.isFocusable();
	}

	public boolean isEditable() {
		return isEditable_;
	}
	
	public void setWidth(int width) {
		this.setSize(width, this.getHeight());
	}
	
	public void setEditable(boolean isEditable) {
		isEditable_ = isEditable;
	}
	
	public void setTypeColumn(XFFieldScriptable typeColumn) {
		if (typeColumn != null) {
			typeColumn_ = typeColumn;
			jLabel.setText(this.getExternalValue().toString());
		}
	}

	public Object getInternalValue() {
		return value_;
	}
	
	public void setOldValue(Object value) {
		oldValue_ = (XFByteArray)value;
	}

	public Object getOldValue() {
		return oldValue_;
	}

	public Object getExternalValue() {
		if (typeColumn_ == null) {
			return "<null>";
		} else {
			return "<"+ typeColumn_.getValue().toString() + ">";
		}
	}
	
	public void setValue(Object value) {
		value_ = (XFByteArray)value;
		if (value_ == null) {
			typeColumn_.setValue("null");
		} else {
			typeColumn_ = value_.getTypeColumn();
			if (value_.getInternalValue().equals("")) {
				typeColumn_.setValue("null");
			}
		}
		jLabel.setText(this.getExternalValue().toString());
	}
	
	public int getRows() {
		return rows_;
	}

	public String getTypeColumnID() {
		return typeColumnID_;
	}
	
	public boolean isUpdated() {
		return !oldValue_.getInternalValue().equals(value_.getInternalValue());
	}
	
	private void processBytea() {
		if (isEditable_) {
			Object[] bts = {XFUtility.RESOURCE.getString("ByteaFieldMessage1"), XFUtility.RESOURCE.getString("ByteaFieldMessage2"), XFUtility.RESOURCE.getString("ByteaFieldMessage3")};
			int rtn = JOptionPane.showOptionDialog(session_, XFUtility.RESOURCE.getString("ByteaFieldMessage5"),
					XFUtility.RESOURCE.getString("ByteaFieldMessage4"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, bts, bts[0]);
			
			///////////////////////////
			// Browsing file content //
			///////////////////////////
			if (rtn == 0) {
				browseBytea();
			}

			/////////////////////////////////////////////
			// Uploading file content into BYTEA field //
			/////////////////////////////////////////////
			if (rtn == 1) {
				session_.jFileChooser.resetChoosableFileFilters();
				session_.jFileChooser.setDialogTitle(XFUtility.RESOURCE.getString("ByteaFieldMessage6"));
				int reply = session_.jFileChooser.showDialog(session_, XFUtility.RESOURCE.getString("ByteaFieldMessage7"));
				if (reply == JFileChooser.APPROVE_OPTION) {
					try {
						File file = new File(session_.jFileChooser.getSelectedFile().getPath());
						int point = file.getAbsolutePath().lastIndexOf(".");
						if (point == -1) {
							JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage8"));
						} else {
							if (typeColumn_ != null) {
								typeColumn_.setValue(file.getAbsolutePath().substring(point + 1));
							}
							jLabel.setText(this.getExternalValue().toString());
							FileInputStream inputStream = new FileInputStream(file);
							byte[] binaryByteArray = new byte[(int) file.length()];
							inputStream.read(binaryByteArray);
							inputStream.close();
							setValue(Base64.encodeBase64(binaryByteArray));
						}
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage9") + e.getMessage());
						e.printStackTrace();
					}
				}
			}

			///////////////////
			// Clear content //
			///////////////////
			if (rtn == 2) {
				setValue(null);
			}
		} else {

			///////////////////////////
			// Browsing file content //
			///////////////////////////
			browseBytea();
		}
	}

	private void browseBytea() {
		if (typeColumn_ == null
				|| typeColumn_.getValue().equals("")
				|| typeColumn_.getValue().equals("null")) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage10"));
		} else {
		    try {
		    	XFByteArray byteArray = (XFByteArray)value_;
		    	byte[] base64ByteArray = (byte[])byteArray.getInternalValue();
				byte[] binaryByteArray = Base64.decodeBase64(base64ByteArray);
				File binaryFile = session_.createTempFile("BYTEA", "." + typeColumn_.getValue());
				FileOutputStream outputStream = new FileOutputStream(binaryFile);
				outputStream.write(binaryByteArray);
				outputStream.close();	
				session_.getDesktop().browse(binaryFile.toURI());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("ByteaFieldMessage11") + e.getMessage());
			}
		}
	}
}

//class XFTextField extends JTextField implements XFEditableField {
//	private static final long serialVersionUID = 1L;
//	private String basicType_ = "";
//	private int digits_ = 5;
//	private int decimal_ = 0;
//	private int rows_ = 1;
//	private ArrayList<String> dataTypeOptionList;
//	private String fieldOptions_;
//	private String autoNumberKey = "";
//	private String oldValue = "";
//
//	public XFTextField(String basicType, int digits, int decimal, String dataTypeOptions, String fieldOptions, String fontName) {
//		super();
//
//		basicType_ = basicType;
//
//		////////////////////////////
//		// digits of VARCHAR is 0 //
//		////////////////////////////
//		if (digits == 0) {
//			digits_ = 10;
//		} else {
//			digits_ = digits;
//		}
//		decimal_ = decimal;
//		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
//		fieldOptions_ = fieldOptions;
//
//		if (basicType_.equals("INTEGER")) {
//			this.setHorizontalAlignment(SwingConstants.RIGHT);
//			this.setText(this.getFormattedNumber("0"));
//		} else {
//			if (basicType_.equals("FLOAT")) {
//				this.setHorizontalAlignment(SwingConstants.RIGHT);
//				String wrkStr = "";
//				if (decimal_ == 0) {
//					wrkStr = "0";
//				}
//				if (decimal_ == 1) {
//					wrkStr = "0.0";
//				}
//				if (decimal_ == 2) {
//					wrkStr = "0.00";
//				}
//				if (decimal_ == 3) {
//					wrkStr = "0.000";
//				}
//				if (decimal_ == 4) {
//					wrkStr = "0.0000";
//				}
//				if (decimal_ == 5) {
//					wrkStr = "0.00000";
//				}
//				if (decimal_ == 6) {
//					wrkStr = "0.000000";
//				}
//				this.setText(this.getFormattedNumber(wrkStr));
//			} else {
//				this.setHorizontalAlignment(SwingConstants.LEFT);
//			}
//		}
//		this.addFocusListener(new ComponentFocusListener());
//		this.setFont(new java.awt.Font(fontName, 0, XFUtility.FONT_SIZE));
//		this.setDocument(new LimitedDocument(this));
//		this.addKeyListener(new KeyAdapter() {
//			public void keyPressed(KeyEvent e) {
//				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//					if (basicType_.equals("STRING") && !autoNumberKey.equals("")) {
//						fillZero();
//					}
//				}
//			} 
//		});
//
//		int fieldWidth, fieldHeight;
//		if (dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS")) {
//			fieldWidth = digits_ * XFUtility.FONT_SIZE + 10;
//		} else {
//			if (basicType_.equals("INTEGER") || basicType_.equals("FLOAT")) {
//				fieldWidth = XFUtility.getLengthOfEdittedNumericValue(digits_, decimal_, dataTypeOptionList) * (XFUtility.FONT_SIZE/2 + 2) + 15;
//			} else {
//				if (basicType_.equals("DATETIME")) {
//					fieldWidth = 24 * (XFUtility.FONT_SIZE/2 + 2);
//				} else {
//					fieldWidth = digits_ * (XFUtility.FONT_SIZE/2 + 2) + 10;
//				}
//			}
//		}
//		if (fieldWidth > 800) {
//			fieldWidth = 800;
//		}
//		fieldHeight = XFUtility.FIELD_UNIT_HEIGHT;
//
//		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "WIDTH");
//		if (!wrkStr.equals("")) {
//			fieldWidth = Integer.parseInt(wrkStr);
//		}
//		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
//		if (!wrkStr.equals("")) {
//			autoNumberKey = wrkStr;
//		}
//
//		this.setSize(new Dimension(fieldWidth, fieldHeight));
//	}
//
//	public boolean isComponentFocusable() {
//		return this.isFocusable();
//	}
//	
//	public void setWidth(int width) {
//		this.setSize(width, this.getHeight());
//	}
//	
//	public void setEditable(boolean editable) {
//		super.setEditable(editable);
//		super.setFocusable(editable);
//	}
//
//	public String getStringNumber(String text) {
//		String numberString = XFUtility.getStringNumber(text);
//		if (numberString.equals("")) {
//			if (basicType_.equals("INTEGER")) {
//				numberString = "0";
//			}
//			if (basicType_.equals("FLOAT")) {
//				if (decimal_ == 0) {
//					numberString = "0";
//				}
//				if (decimal_ == 1) {
//					numberString = "0.0";
//				}
//				if (decimal_ == 2) {
//					numberString = "0.00";
//				}
//				if (decimal_ == 3) {
//					numberString = "0.000";
//				}
//				if (decimal_ == 4) {
//					numberString = "0.0000";
//				}
//				if (decimal_ == 5) {
//					numberString = "0.00000";
//				}
//				if (decimal_ == 6) {
//					numberString = "0.000000";
//				}
//			}
//		} else {
//			if (dataTypeOptionList.contains("ZIPNO")) {
//				numberString = numberString.replace("-", "");
//				int workLength = 7 - numberString.length();
//				for (int i = 0; i < workLength; i++) {
//					numberString = numberString + "0";
//				}
//				numberString = numberString.substring(0, 3) + "-" + numberString.substring(3, 7); 
//			}
//		}
//		return numberString;
//	}
//
//	public Object getInternalValue() {
//		String text = "";
//		if (basicType_.equals("INTEGER")
//			|| basicType_.equals("FLOAT")
//			|| dataTypeOptionList.contains("DIAL")
//			|| dataTypeOptionList.contains("ZIPNO")) {
//			if (this.getText().equals("*AUTO")) {
//				text = this.getText();
//			} else {
//				text = this.getStringNumber(this.getText());
//			}
//		} else {
//			text = this.getText();
//		}
//		return text;
//	}
//	
//	public void setOldValue(Object obj) {
//		oldValue = obj.toString();
//	}
//
//	public Object getOldValue() {
//		String text = "";
//		if (basicType_.equals("INTEGER")
//			|| basicType_.equals("FLOAT")
//			|| dataTypeOptionList.contains("DIAL")
//			|| dataTypeOptionList.contains("ZIPNO")) {
//			text = this.getStringNumber(oldValue);
//		} else {
//			text = oldValue;
//		}
//		return text;
//	}
//
//	public Object getExternalValue() {
//		return this.getInternalValue();
//	}
//	
//	public void setValue(Object obj) {
//		String text = null;
//		if (obj != null) {
//			text = obj.toString();
//		}
//		if (text != null) {
//			if (basicType_.equals("INTEGER")) {
//				if (text.equals("*AUTO")) {
//					this.setText(text);
//				} else {
//					this.setText(getFormattedNumber(text));
//				}
//			}
//			if (basicType_.equals("FLOAT")) {
//				this.setText(getFormattedNumber(text));
//			}
//			if (basicType_.equals("DATE")) {
//				this.setText(text);
//			}
//			if (basicType_.equals("TIME")) {
//				this.setText(text);
//			}
//			if (basicType_.equals("DATETIME")) {
//				text = text.replace("-", "/");
//				this.setText(text);
//			}
//			if (basicType_.equals("STRING")
//					|| basicType_.equals("BYTEA")) {
//				text = text.trim();
//				this.setText(text);
//			}
//		}
//	}
//	
//	public String getFormattedNumber(String text) {
//		String value = "0";
//		if (text != null) {
//			if (basicType_.equals("INTEGER")) {
//				value = XFUtility.getFormattedIntegerValue(getStringNumber(text), dataTypeOptionList, digits_);
//			}
//			if (basicType_.equals("FLOAT")) {
//				value = XFUtility.getFormattedFloatValue(getStringNumber(text), decimal_);
//			}
//		}
//		return value;
//	}
//	
//	public int getRows() {
//		return rows_;
//	}
//	
//	private void fillZero() {
//		int stringDigitFrom = -1;
//		int numberDigitFrom = 0;
//		if (getText().length() < digits_ && !getText().equals("")) {
//			for( int i = 0; i < getText().length() ; i++) {
//				try {
//					Integer.parseInt(getText().substring(i, i+1));
//					numberDigitFrom = i;
//					break;
//				} catch(NumberFormatException e) {
//					stringDigitFrom = i;
//				}
//			}
//			StringBuffer sb = new StringBuffer();
//			if (stringDigitFrom > -1 && numberDigitFrom > stringDigitFrom) {
//				sb.append(getText().substring(stringDigitFrom, numberDigitFrom));
//			}
//			int zeroLen = digits_ - getText().length();
//			for( int i = 0; i < zeroLen ; i++) {
//				sb.append("0");
//			}
//			sb.append(getText().substring(numberDigitFrom, getText().length()));
//			setText(sb.toString());
//		}
//	}
//
//	class ComponentFocusListener implements FocusListener{
//		public void focusLost(FocusEvent event){
//			if (basicType_.equals("INTEGER")) {
//				if (!getText().equals("*AUTO")) {
//					setText(getFormattedNumber(getText()));
//				}
//			}
//			if (basicType_.equals("FLOAT")) {
//				setText(getFormattedNumber(getText()));
//			}
//			if (getInputContext() != null) {
//				getInputContext().setCompositionEnabled(false);
//			}
//			if (basicType_.equals("STRING") && !autoNumberKey.equals("")) {
//				fillZero();
//			}
//		}
//		public void focusGained(FocusEvent event){
//			selectAll();
//			Character.Subset[] subsets  = new Character.Subset[] {java.awt.im.InputSubset.LATIN_DIGITS};
//			String lang = Locale.getDefault().getLanguage();
//			if (basicType_.equals("STRING")) {
//				if (dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS")) {
//					if (lang.equals("ja")) {
//						subsets = new Character.Subset[] {java.awt.im.InputSubset.KANJI};
//					}
//					if (lang.equals("ko")) {
//						subsets = new Character.Subset[] {java.awt.im.InputSubset.HANJA};
//					}
//					if (lang.equals("zh")) {
//						subsets = new Character.Subset[] {java.awt.im.InputSubset.TRADITIONAL_HANZI};
//					}
//					getInputContext().setCharacterSubsets(subsets);
//					getInputContext().setCompositionEnabled(true);
//				} else {
//					if (dataTypeOptionList.contains("KATAKANA") && lang.equals("ja")) {
//						subsets = new Character.Subset[] {java.awt.im.InputSubset.HALFWIDTH_KATAKANA};
//						getInputContext().setCharacterSubsets(subsets);
//						getInputContext().setCompositionEnabled(true);
//					} else {
//						InputContext ic = getInputContext();
//						if (ic != null) {
//							ic.setCharacterSubsets(subsets);
//							ic.setCompositionEnabled(false);
//						}
//					}
//				}
//			} else {
//				InputContext ic = getInputContext();
//				if (ic != null) {
//					ic.setCharacterSubsets(subsets);
//					ic.setCompositionEnabled(false);
//				}
//			}
//		}
//	}
//
//	class LimitedDocument extends PlainDocument {
//		private static final long serialVersionUID = 1L;
//		XFTextField adaptee;
//		LimitedDocument(XFTextField adaptee) {
//		  this.adaptee = adaptee;
//		}
//		public void insertString(int offset, String str, AttributeSet attr) {
//			try {
//				int integerSizeOfField = adaptee.digits_ - adaptee.decimal_;
//
//				if (adaptee.decimal_ > 0 && str.length() == 1) {
//					if (adaptee.isEditable()
//							&& (basicType_.equals("INTEGER") || basicType_.equals("FLOAT"))
//							&& !dataTypeOptionList.contains("ACCEPT_MINUS")
//							&& str.contains("-")) {
//						JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("MinusError") + ": " + str);
//					} else {
//						String wrkStr0 = super.getText(0, super.getLength());
//						wrkStr0 = wrkStr0.substring(0, offset) + str + wrkStr0.substring(offset, wrkStr0.length());
//						String wrkStr1 = wrkStr0.replace(".", "");
//						wrkStr1 = wrkStr1.replace(",", "");
//						wrkStr1 = wrkStr1.replace("-", "");
//						if (wrkStr1.length() > adaptee.digits_) {
//							wrkStr1 = wrkStr1.substring(0, integerSizeOfField) + "." + wrkStr1.substring(integerSizeOfField, wrkStr1.length() - 1);
//							super.replace(0, super.getLength(), wrkStr1, attr);
//						} else {
//							if (basicType_.equals("INTEGER") && str.contains(".")) {
//								JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("NumberFormatError"));
//							} else {
//								int posOfDecimal = wrkStr0.indexOf(".");
//								if (posOfDecimal == -1) {
//									if (wrkStr1.length() > integerSizeOfField) {
//										wrkStr1 = wrkStr1.substring(0, integerSizeOfField) + "." + wrkStr1.substring(integerSizeOfField, wrkStr1.length());
//										super.replace(0, super.getLength(), wrkStr1, attr);
//									} else {
//										super.insertString( offset, str, attr );
//									}
//								} else {
//									int decimalLengthOfInputData = wrkStr0.length() - posOfDecimal - 1;
//									if (decimalLengthOfInputData <= adaptee.decimal_) {
//										super.insertString( offset, str, attr );
//									}
//								}
//							}
//						}
//					}
//				} else {
//					if (adaptee.isEditable()
//							&& (basicType_.equals("INTEGER") || basicType_.equals("FLOAT"))
//							&& !dataTypeOptionList.contains("ACCEPT_MINUS")
//							&& str.contains("-")) {
//						JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("MinusError") + ": " + str);
//					} else {
//						if (basicType_.equals("INTEGER")) {
//							if (str.contains(".")) {
//								JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("NumberFormatError"));
//							} else {
//								String wrkStr0 = super.getText(0, super.getLength());
//								wrkStr0 = wrkStr0.substring(0, offset) + str + wrkStr0.substring(offset, wrkStr0.length());
//								String wrkStr1 = wrkStr0.replace(".", "");
//								wrkStr1 = wrkStr1.replace(",", "");
//								wrkStr1 = wrkStr1.replace("-", "");
//								if (wrkStr1.length() <= adaptee.digits_ || wrkStr1.equals("*AUTO")) {
//									super.insertString(offset, str, attr );
//								}
//							}
//						} else {
//							if (basicType_.equals("FLOAT")) {
//								String wrkStr0 = super.getText(0, super.getLength());
//								wrkStr0 = wrkStr0.substring(0, offset) + str + wrkStr0.substring(offset, wrkStr0.length());
//								String wrkStr1 = wrkStr0.replace(".", "");
//								wrkStr1 = wrkStr1.replace(",", "");
//								wrkStr1 = wrkStr1.replace("-", "");
//								if (wrkStr1.length() <= adaptee.digits_) {
//									super.insertString( offset, str, attr );
//								}
//							} else {
//								if (offset < adaptee.digits_ && super.getLength() < adaptee.digits_) {
//									super.insertString( offset, str, attr );
//								}
//							}
//						}
//					}
//				}
//			} catch (BadLocationException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//}

class XFTextField extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private String basicType_ = "";
	private String fontName_ = "";
	private int digits_ = 5;
	private int decimal_ = 0;
	private int rows_ = 1;
	private ArrayList<String> dataTypeOptionList;
	private String fieldOptions_;
	private String autoNumberKey = "";
	private String oldValue = "";
	private JTextField jTextField = new JTextField();
	private JComboBox<String> jComboBox = null;
	private String[] valueList_ = null;
	private boolean itemSelectionControled = true;
	private boolean isFilter_ = true;

	public XFTextField(String basicType, int digits, int decimal, String dataTypeOptions, String fieldOptions, String fontName) {
		this(basicType, digits, decimal, dataTypeOptions, fieldOptions, fontName, false);
	}

	public XFTextField(String basicType, int digits, int decimal, String dataTypeOptions, String fieldOptions, String fontName, boolean isFilter) {
		super();

		basicType_ = basicType;
		fontName_ = fontName;
		isFilter_ = isFilter;

		////////////////////////////
		// digits of VARCHAR is 0 //
		////////////////////////////
		if (digits == 0) {
			digits_ = 10;
		} else {
			digits_ = digits;
		}
		decimal_ = decimal;
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		fieldOptions_ = fieldOptions;
		if (dataTypeOptionList.contains("PERCENT")) {
			digits_++;
		}
		String alignment = XFUtility.getOptionValueWithKeyword(fieldOptions_, "ALIGNMENT");

		if (basicType_.equals("INTEGER")) {
			if (alignment.equals("") || alignment.equals("RIGHT")) {
				jTextField.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			if (alignment.equals("CENTER")) {
				jTextField.setHorizontalAlignment(SwingConstants.CENTER);
			}
			if (alignment.equals("LEFT")) {
				jTextField.setHorizontalAlignment(SwingConstants.LEFT);
			}
			if (!isFilter_) {
				jTextField.setText(this.getFormattedNumber("0"));
			}
		} else {
			if (basicType_.equals("FLOAT")) {
				if (alignment.equals("") || alignment.equals("RIGHT")) {
					jTextField.setHorizontalAlignment(SwingConstants.RIGHT);
				}
				if (alignment.equals("CENTER")) {
					jTextField.setHorizontalAlignment(SwingConstants.CENTER);
				}
				if (alignment.equals("LEFT")) {
					jTextField.setHorizontalAlignment(SwingConstants.LEFT);
				}

				if (!isFilter_) {
					String wrkStr = "";
					if (decimal_ == 0) {
						wrkStr = "0";
					}
					if (decimal_ == 1) {
						wrkStr = "0.0";
					}
					if (decimal_ == 2) {
						wrkStr = "0.00";
					}
					if (decimal_ == 3) {
						wrkStr = "0.000";
					}
					if (decimal_ == 4) {
						wrkStr = "0.0000";
					}
					if (decimal_ == 5) {
						wrkStr = "0.00000";
					}
					if (decimal_ == 6) {
						wrkStr = "0.000000";
					}
					jTextField.setText(this.getFormattedNumber(wrkStr));
				}
			} else {
				if (alignment.equals("") || alignment.equals("LEFT")) {
					jTextField.setHorizontalAlignment(SwingConstants.LEFT);
				}
				if (alignment.equals("CENTER")) {
					jTextField.setHorizontalAlignment(SwingConstants.CENTER);
				}
				if (alignment.equals("RIGHT")) {
					jTextField.setHorizontalAlignment(SwingConstants.RIGHT);
				}
			}
		}
		jTextField.addFocusListener(new ComponentFocusListener());
		jTextField.setFont(new java.awt.Font(fontName_, 0, XFUtility.FONT_SIZE));
		jTextField.setDocument(new LimitedDocument(this));
		jTextField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (basicType_.equals("STRING") && !autoNumberKey.equals("")) {
						fillZero();
					}
				}
			} 
		});

		int fieldWidth, fieldHeight;
		if (dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS")) {
			fieldWidth = digits_ * XFUtility.FONT_SIZE + 10;
		} else {
			if (basicType_.equals("INTEGER") || basicType_.equals("FLOAT")) {
				fieldWidth = XFUtility.getLengthOfEdittedNumericValue(digits_, decimal_, dataTypeOptionList) * (XFUtility.FONT_SIZE/2 + 2) + 15;
			} else {
				if (basicType_.equals("DATETIME")) {
					fieldWidth = 24 * (XFUtility.FONT_SIZE/2 + 2);
				} else {
					fieldWidth = digits_ * (XFUtility.FONT_SIZE/2 + 2) + 10;
				}
			}
		}
		if (fieldWidth > 800) {
			fieldWidth = 800;
		}
		fieldHeight = XFUtility.FIELD_UNIT_HEIGHT;

		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			autoNumberKey = wrkStr;
		}

		this.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent event) {
				jTextField.requestFocus();
				if (jComboBox != null) {
					jComboBox.requestFocus();
				}
			} 
		});
		this.setSize(new Dimension(fieldWidth, fieldHeight));
		jTextField.setBorder(null);
		this.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		this.setLayout(new BorderLayout());
		this.add(jTextField, BorderLayout.CENTER);
	}

	public void addKeyListener(KeyAdapter adapter) {
		jTextField.addKeyListener(adapter);
	}

	public boolean isComponentFocusable() {
		return jTextField.isFocusable();
	}

	public boolean isEditable() {
		return jTextField.isEditable();
	}
	
	public String[] getValueList() {
		return valueList_;
	}
	
	public void setForeground(Color color) {
		if (jComboBox == null) {
			if (jTextField != null) {
				jTextField.setForeground(color);
			}
		} else {
			jComboBox.setForeground(color);
		}
	}
	
	public void setBackground(Color color) {
		if (jComboBox == null) {
			if (jTextField != null) {
				jTextField.setBackground(color);
			}
		} else {
			jComboBox.setBackground(color);
		}
	}
	
	public void setValueList(String[] valueList) {
		if (!java.util.Arrays.equals(valueList, valueList_)) {
			valueList_ = valueList;
			if (valueList_ == null || valueList_.length == 0) {
				if (jComboBox != null) {
					this.remove(jComboBox);
				}
				this.add(jTextField);
				jComboBox = null;

			} else {
				itemSelectionControled = true;
				jComboBox = new JComboBox<String>();
				jComboBox.setBorder(null);
				ListCellRenderer renderer = new DefaultListCellRenderer();
				((JLabel)renderer).setHorizontalAlignment(jTextField.getHorizontalAlignment());
				jComboBox.setRenderer(renderer);
				jComboBox.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (!itemSelectionControled || jTextField.getText().equals("")){
							jTextField.setText((String)jComboBox.getSelectedItem());
						}
					} 
				});
				InputMap inputMap  = jComboBox.getInputMap(JComboBox.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
				ActionMap actionMap = jComboBox.getActionMap();
				inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "CHECK");
				actionMap.put("CHECK", null);
				for (int i = 0; i < valueList_.length; i++) {
					jComboBox.addItem(valueList_[i]);
				}
				int index = -1;
				for (int i = 0; i < jComboBox.getItemCount(); i++) {
					if (jComboBox.getItemAt(i).toString().equals(jTextField.getText())) {
						index = i;
						break;
					}
				}
				if (index > -1) {
					jComboBox.setSelectedIndex(index);
				}
				itemSelectionControled = false;
				this.remove(jTextField);
				this.add(jComboBox);
			}
		}
	}
	
	public void setHorizontalAlignment(int alignment) {
		jTextField.setHorizontalAlignment(alignment);
		if (jComboBox != null) {
			((JLabel)jComboBox.getRenderer()).setHorizontalAlignment(alignment);
		}
	}
	
	public void setWidth(int width) {
		this.setSize(width, this.getHeight());
	}
	
	public void setEditable(boolean editable) {
		jTextField.setEditable(editable);
		if (jComboBox == null) {
			if (editable) {
				jTextField.setFont(new java.awt.Font(fontName_, 0, XFUtility.FONT_SIZE - 2));
			} else {
				jTextField.setFont(new java.awt.Font(fontName_, 0, XFUtility.FONT_SIZE));
			}
		} else {
			jComboBox.setFocusable(editable);
			if (editable) {
				this.remove(jTextField);
				this.add(jComboBox);
			} else {
				this.remove(jComboBox);
				this.add(jTextField);
			}
		}
	}

	public String getStringNumber(String text) {
		String numberString = XFUtility.getStringNumber(text);
		if (numberString.equals("")) {
			if (!isFilter_) {
				if (basicType_.equals("INTEGER")) {
					numberString = "0";
				}
				if (basicType_.equals("FLOAT")) {
					if (decimal_ == 0) {
						numberString = "0";
					}
					if (decimal_ == 1) {
						numberString = "0.0";
					}
					if (decimal_ == 2) {
						numberString = "0.00";
					}
					if (decimal_ == 3) {
						numberString = "0.000";
					}
					if (decimal_ == 4) {
						numberString = "0.0000";
					}
					if (decimal_ == 5) {
						numberString = "0.00000";
					}
					if (decimal_ == 6) {
						numberString = "0.000000";
					}
				}
			}
		} else {
			if (dataTypeOptionList.contains("ZIPNO")) {
				numberString = numberString.replace("-", "");
				int workLength = 7 - numberString.length();
				for (int i = 0; i < workLength; i++) {
					numberString = numberString + "0";
				}
				numberString = numberString.substring(0, 3) + "-" + numberString.substring(3, 7); 
			}
		}
		return numberString;
	}

	public Object getInternalValue() {
		String text = "";
		if (basicType_.equals("INTEGER")
			|| basicType_.equals("FLOAT")
			|| dataTypeOptionList.contains("DIAL")
			|| dataTypeOptionList.contains("ZIPNO")) {
			if (jTextField.getText().equals("*AUTO")) {
				text = jTextField.getText();
			} else {
				text = this.getStringNumber(jTextField.getText());
			}
		} else {
			text = jTextField.getText();
		}
		return text;
	}
	
	public void setOldValue(Object obj) {
		oldValue = obj.toString();
	}
	
	public Object getOldValue() {
		String text = "";
		if (basicType_.equals("INTEGER")
			|| basicType_.equals("FLOAT")
			|| dataTypeOptionList.contains("DIAL")
			|| dataTypeOptionList.contains("ZIPNO")) {
			text = this.getStringNumber(oldValue);
		} else {
			text = oldValue;
		}
		return text;
	}

	public String getText() {
		return jTextField.getText();
	}

	public void setText(String text) {
		jTextField.setText(text);
	}
	
	public Object getExternalValue() {
		return this.getInternalValue();
	}
	
	public void setValue(Object obj) {
		String text = null;
		if (obj != null) {
			text = obj.toString();
		}
		if (text != null) {
			if (basicType_.equals("INTEGER")) {
				if (text.equals("*AUTO")) {
					jTextField.setText(text);
				} else {
					jTextField.setText(getFormattedNumber(text));
				}
			}
			if (basicType_.equals("FLOAT")) {
				jTextField.setText(getFormattedNumber(text));
			}
			if (basicType_.equals("DATE")) {
				jTextField.setText(text);
			}
			if (basicType_.equals("TIME")) {
				jTextField.setText(text);
			}
			if (basicType_.equals("DATETIME")) {
				text = text.replace("-", "/");
				jTextField.setText(text);
			}
			if (basicType_.equals("STRING")
					|| basicType_.equals("BYTEA")) {
				text = text.trim();
				jTextField.setText(text);
			}
		}
	}
	
	public String getFormattedNumber(String text) {
		String value = "0";
		if (text != null) {
			if (basicType_.equals("INTEGER")) {
				value = XFUtility.getFormattedIntegerValue(getStringNumber(text), dataTypeOptionList, digits_);
			}
			if (basicType_.equals("FLOAT")) {
				value = XFUtility.getFormattedFloatValue(getStringNumber(text), decimal_);
			}
			if (isFilter_) {
				if (text.equals("")) {
					value = "";
				} 
			}
		}
		return value;
	}
	
	public int getRows() {
		return rows_;
	}
	
	private void fillZero() {
		int stringDigitFrom = -1;
		int numberDigitFrom = 0;
		if (jTextField.getText().length() < digits_ && !jTextField.getText().equals("")) {
			for( int i = 0; i < jTextField.getText().length() ; i++) {
				try {
					Integer.parseInt(jTextField.getText().substring(i, i+1));
					numberDigitFrom = i;
					break;
				} catch(NumberFormatException e) {
					stringDigitFrom = i;
				}
			}
			StringBuffer sb = new StringBuffer();
			if (stringDigitFrom > -1 && numberDigitFrom > stringDigitFrom) {
				sb.append(jTextField.getText().substring(stringDigitFrom, numberDigitFrom));
			}
			int zeroLen = digits_ - jTextField.getText().length();
			for( int i = 0; i < zeroLen ; i++) {
				sb.append("0");
			}
			sb.append(jTextField.getText().substring(numberDigitFrom, jTextField.getText().length()));
			jTextField.setText(sb.toString());
		}
	}

	class ComponentFocusListener implements FocusListener{
		public void focusLost(FocusEvent event){
			if (basicType_.equals("INTEGER")) {
				if (!jTextField.getText().equals("*AUTO")) {
					jTextField.setText(getFormattedNumber(jTextField.getText()));
				}
			}
			if (basicType_.equals("FLOAT")) {
				jTextField.setText(getFormattedNumber(jTextField.getText()));
			}
			if (getInputContext() != null) {
				getInputContext().setCompositionEnabled(false);
			}
			if (basicType_.equals("STRING") && !autoNumberKey.equals("")) {
				fillZero();
			}
		}
		public void focusGained(FocusEvent event){
			if (jTextField.isEditable()) {
				jTextField.selectAll();
			}
			Character.Subset[] subsets  = new Character.Subset[] {java.awt.im.InputSubset.LATIN_DIGITS};
			String lang = Locale.getDefault().getLanguage();
			if (basicType_.equals("STRING")) {
				if (dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS")) {
					if (lang.equals("ja")) {
						subsets = new Character.Subset[] {java.awt.im.InputSubset.KANJI};
					}
					if (lang.equals("ko")) {
						subsets = new Character.Subset[] {java.awt.im.InputSubset.HANJA};
					}
					if (lang.equals("zh")) {
						subsets = new Character.Subset[] {java.awt.im.InputSubset.TRADITIONAL_HANZI};
					}
					getInputContext().setCharacterSubsets(subsets);
					getInputContext().setCompositionEnabled(true);
				} else {
					if (dataTypeOptionList.contains("KATAKANA") && lang.equals("ja")) {
						subsets = new Character.Subset[] {java.awt.im.InputSubset.HALFWIDTH_KATAKANA};
						getInputContext().setCharacterSubsets(subsets);
						getInputContext().setCompositionEnabled(true);
					} else {
						InputContext ic = getInputContext();
						if (ic != null) {
							ic.setCharacterSubsets(subsets);
							ic.setCompositionEnabled(false);
						}
					}
				}
			} else {
				InputContext ic = getInputContext();
				if (ic != null) {
					ic.setCharacterSubsets(subsets);
					ic.setCompositionEnabled(false);
				}
			}
		}
	}

	class LimitedDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;
		XFTextField adaptee;
		LimitedDocument(XFTextField adaptee) {
		  this.adaptee = adaptee;
		}
		public void insertString(int offset, String str, AttributeSet attr) {
			try {
				int integerSizeOfField = adaptee.digits_ - adaptee.decimal_;

				if (adaptee.decimal_ > 0 && str.length() == 1) {
					if (adaptee.isEditable()
							&& (basicType_.equals("INTEGER") || basicType_.equals("FLOAT"))
							&& !dataTypeOptionList.contains("ACCEPT_MINUS")
							&& str.contains("-")) {
						JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("MinusError") + ": " + str);
					} else {
						String wrkStr0 = super.getText(0, super.getLength());
						wrkStr0 = wrkStr0.substring(0, offset) + str + wrkStr0.substring(offset, wrkStr0.length());
						String wrkStr1 = wrkStr0.replace(".", "");
						wrkStr1 = wrkStr1.replace(",", "");
						wrkStr1 = wrkStr1.replace("-", "");
						wrkStr1 = wrkStr1.replace(":", "");
						if (wrkStr1.length() > adaptee.digits_) {
							wrkStr1 = wrkStr1.substring(0, integerSizeOfField) + "." + wrkStr1.substring(integerSizeOfField, wrkStr1.length() - 1);
							super.replace(0, super.getLength(), wrkStr1, attr);
						} else {
							if (basicType_.equals("INTEGER") && str.contains(".")) {
								JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("NumberFormatError"));
							} else {
								int posOfDecimal = wrkStr0.indexOf(".");
								if (posOfDecimal == -1) {
									if (wrkStr1.length() > integerSizeOfField) {
										wrkStr1 = wrkStr1.substring(0, integerSizeOfField) + "." + wrkStr1.substring(integerSizeOfField, wrkStr1.length());
										super.replace(0, super.getLength(), wrkStr1, attr);
									} else {
										super.insertString( offset, str, attr );
									}
								} else {
									int decimalLengthOfInputData = wrkStr0.length() - posOfDecimal - 1;
									if (decimalLengthOfInputData <= adaptee.decimal_) {
										super.insertString( offset, str, attr );
									}
								}
							}
						}
					}
				} else {
					if (adaptee.isEditable()
							&& (basicType_.equals("INTEGER") || basicType_.equals("FLOAT"))
							&& !dataTypeOptionList.contains("ACCEPT_MINUS")
							&& str.contains("-")) {
						JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("MinusError") + ": " + str);
					} else {
						if (basicType_.equals("INTEGER")) {
							if (str.contains(".")) {
								JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("NumberFormatError"));
							} else {
								String wrkStr0 = super.getText(0, super.getLength());
								wrkStr0 = wrkStr0.substring(0, offset) + str + wrkStr0.substring(offset, wrkStr0.length());
								String wrkStr1 = wrkStr0.replace(".", "");
								wrkStr1 = wrkStr1.replace(",", "");
								wrkStr1 = wrkStr1.replace("-", "");
								wrkStr1 = wrkStr1.replace(":", "");
								if (wrkStr1.length() <= adaptee.digits_ || wrkStr1.equals("*AUTO")) {
									super.insertString(offset, str, attr );
								}
							}
						} else {
							if (basicType_.equals("FLOAT")) {
								String wrkStr0 = super.getText(0, super.getLength());
								wrkStr0 = wrkStr0.substring(0, offset) + str + wrkStr0.substring(offset, wrkStr0.length());
								String wrkStr1 = wrkStr0.replace(".", "");
								wrkStr1 = wrkStr1.replace(",", "");
								wrkStr1 = wrkStr1.replace("-", "");
								if (wrkStr1.length() <= adaptee.digits_) {
									super.insertString( offset, str, attr );
								}
							} else {
								if (offset < adaptee.digits_ && super.getLength() < adaptee.digits_) {
									super.insertString( offset, str, attr );
								}
							}
						}
					}
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
}

class XFTextArea extends JScrollPane implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private String fieldOptions_ = "";
	private ArrayList<String> dataTypeOptionList;
	private JTextArea jTextArea = new JTextArea();
	private int rows_ = 2;
	private int digits_ = 5;
	private String oldValue = "";

	public XFTextArea(int digits, String dataTypeOptions, String fieldOptions, String fontName){
		super();
		digits_ = digits;
		if (digits_ == 0) {
			digits_ = 2147483647;
		}
		String wrkStr;
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		fieldOptions_ = fieldOptions;
		InputMap inputMap  = jTextArea.getInputMap(JTextArea.WHEN_FOCUSED);
		ActionMap actionMap = jTextArea.getActionMap();

		KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		KeyStroke shiftTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		KeyStroke altEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK);

		inputMap.put(altEnter, inputMap.get(enter));
		inputMap.put(enter, "none");
		inputMap.put(tab, "Exit");
		inputMap.put(shiftTab, "Backward");
		Action transferFocusAction = new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e){
				jTextArea.transferFocus();
			}
		};
		actionMap.put("Exit", transferFocusAction);
		Action transferFocusBackwardAction = new AbstractAction(){
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent e){
				jTextArea.transferFocusBackward();
			}
		};
		actionMap.put("Backward", transferFocusBackwardAction);

		jTextArea.addFocusListener(new ComponentFocusListener());
		jTextArea.setFont(new java.awt.Font(fontName, 0, XFUtility.FONT_SIZE));
		jTextArea.setLineWrap(true);
		jTextArea.setWrapStyleWord(false);
		jTextArea.setDocument(new LimitedDocument(this));
		this.getViewport().add(jTextArea, null);

		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "ROWS");
		if (!wrkStr.equals("")) {
			rows_ = Integer.parseInt(wrkStr);
		}

		int fieldWidth = 800;
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}

		int fieldHeight = rows_ * XFUtility.FIELD_UNIT_HEIGHT + (rows_-1) * XFUtility.FIELD_VERTICAL_MARGIN;
		if (rows_ == 1) {
			//fieldHeight = XFUtility.FIELD_UNIT_HEIGHT + XFUtility.FIELD_VERTICAL_MARGIN;
			fieldHeight = XFUtility.FIELD_UNIT_HEIGHT;
		}
		this.setSize(fieldWidth, fieldHeight);
		this.addFocusListener(new ScrollPaneFocusListener());
	}
	
	public void setWidth(int width) {
		this.setSize(width, this.getHeight());
	}

	public boolean isEditable() {
		return jTextArea.isEditable();
	}

	public boolean isComponentFocusable() {
		return jTextArea.isFocusable();
	}

	public boolean isFocusable() {
		return false;
	}

	public void setForeground(Color color) {
		if (jTextArea != null) {
			jTextArea.setForeground(color);
		}
	}

	public void setBackground(Color color) {
		if (jTextArea != null) {
			jTextArea.setBackground(color);
		}
	}

	public void setValue(Object text) {
		jTextArea.setText((String)text);
		jTextArea.setCaretPosition(0);
	}

	public Object getInternalValue() {
		return jTextArea.getText();
	}

	public Object getExternalValue() {
		return this.getInternalValue();
	}
	
	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}
	
	public void setToolTipText(String text) {
		jTextArea.setToolTipText(text);
	}
	
	public void setEditable(boolean editable) {
		jTextArea.setEditable(editable);
		if (editable) {
			jTextArea.setBackground(XFUtility.ACTIVE_COLOR);
		} else {
			jTextArea.setBackground(XFUtility.INACTIVE_COLOR);
		}
	}

	class ComponentFocusListener implements FocusListener{
		public void focusLost(FocusEvent event){
			if (getInputContext() != null) {
				getInputContext().setCompositionEnabled(false);
			}
		}
		public void focusGained(FocusEvent event){
			if (jTextArea.isEditable()) {
				jTextArea.selectAll();
			}
			Character.Subset[] subsets  = new Character.Subset[] {java.awt.im.InputSubset.LATIN_DIGITS};
			String lang = Locale.getDefault().getLanguage();
			if (dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS")) {
				if (lang.equals("ja")) {
					subsets = new Character.Subset[] {java.awt.im.InputSubset.KANJI};
				}
				if (lang.equals("ko")) {
					subsets = new Character.Subset[] {java.awt.im.InputSubset.HANJA};
				}
				if (lang.equals("zh")) {
					subsets = new Character.Subset[] {java.awt.im.InputSubset.TRADITIONAL_HANZI};
				}
				getInputContext().setCharacterSubsets(subsets);
				getInputContext().setCompositionEnabled(true);
			} else {
				if (dataTypeOptionList.contains("KATAKANA") && lang.equals("ja")) {
					subsets = new Character.Subset[] {java.awt.im.InputSubset.HALFWIDTH_KATAKANA};
					getInputContext().setCharacterSubsets(subsets);
					getInputContext().setCompositionEnabled(true);
				} else {
					getInputContext().setCharacterSubsets(subsets);
					getInputContext().setCompositionEnabled(false);
				}
			}
		}
	}

	class ScrollPaneFocusListener implements FocusListener{
		public void focusLost(FocusEvent event){
		}
		public void focusGained(FocusEvent event){
			jTextArea.requestFocus();
		}
	}

	public int getRows() {
		return rows_;
	}

	class LimitedDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;
		XFTextArea adaptee;
		LimitedDocument(XFTextArea adaptee) {
		  this.adaptee = adaptee;
		}
		public void insertString(int offset, String str, AttributeSet a) {
			try {
//				if (offset < adaptee.digits_ && super.getLength() < adaptee.digits_) {
//					super.insertString( offset, str, a );
//				}
				if (offset + str.length() <= adaptee.digits_ && super.getLength() + str.length() <= adaptee.digits_ ) {
					super.insertString( offset, str, a );
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}
}

class XFInputAssistField extends JComboBox implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private String tableID_ = "";
	private String fieldID_ = "";
	private ArrayList<String> dataTypeOptionList;
	private Vector<String> valueList = new Vector<String>();
	private int rows_ = 1;
    private Session session_;
    private String oldValue = "";

	public XFInputAssistField(String tableID, String fieldID, int digits, String dataTypeOptions, Session session){
		super();
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		tableID_ = tableID;
		fieldID_ = fieldID;
		session_ = session;

		int fieldWidth = 100;
		this.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		if (dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS")) {
			fieldWidth = digits * XFUtility.FONT_SIZE + 10;
		} else {
			fieldWidth = digits * (XFUtility.FONT_SIZE/2 + 2) + 10;
		}
		if (fieldWidth > 800) {
			fieldWidth = 800;
		}
		updateList();

		DefaultComboBoxModel model = new DefaultComboBoxModel(valueList);
		this.setModel(model);
		this.setSize(new Dimension(fieldWidth, XFUtility.FIELD_UNIT_HEIGHT));
		this.setEditable(true);
		this.setSelectedIndex(-1);
		JTextField field = (JTextField)this.getEditor().getEditorComponent();
		field.setText("");
		field.addKeyListener(new ComboKeyHandler(this));
	}

	public void updateList() {
		String wrkStr;
		valueList.removeAllElements();
		String sql = "select distinct " + fieldID_ + " from " + tableID_ + " order by " + fieldID_;
		try {
			XFTableOperator operator = new XFTableOperator(session_, null, sql, true);
			while (operator.next()) {
				wrkStr = operator.getValueOf(fieldID_).toString().trim();
				valueList.add(wrkStr);
			}
		} catch (Exception e) {
		}
	}

	@Override public void updateUI() {
		super.updateUI();
		setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
			@Override protected JButton createArrowButton() {
				JButton button = new JButton() {
					private static final long serialVersionUID = 1L;
					@Override public int getWidth() {
						return 0;
					}
				};
				button.setBorder(BorderFactory.createEmptyBorder());
				button.setVisible(false);
				return button;
			}
			@Override public void configureArrowButton() {}
		});
		for(MouseListener ml:getMouseListeners()) {
			removeMouseListener(ml);
		}
	}
	
	class ComboKeyHandler extends KeyAdapter{
		private final JComboBox<String> comboBox;
		private final Vector<String> list = new Vector<String>();
		public ComboKeyHandler(JComboBox combo) {
			this.comboBox = combo;
			for(int i=0;i<comboBox.getModel().getSize();i++) {
				list.addElement((String)comboBox.getItemAt(i));
			}
		}
		private boolean shouldHide = false;
		@Override public void keyTyped(final KeyEvent e) {
			EventQueue.invokeLater(new Runnable() {
				@Override public void run() {
					String text = ((JTextField)e.getSource()).getText();
					if(text.length()==0) {
						setSuggestionModel(comboBox, new DefaultComboBoxModel(list), "");
						comboBox.hidePopup();
					}else{
						ComboBoxModel m = getSuggestedModel(list, text);
						if(m.getSize()==0 || shouldHide) {
							comboBox.hidePopup();
						}else{
							setSuggestionModel(comboBox, m, text);
							comboBox.showPopup();
						}
					}
				}
			});
		}
		@Override public void keyPressed(KeyEvent e) {
			JTextField textField = (JTextField)e.getSource();
			String text = textField.getText();
			shouldHide = false;
			switch(e.getKeyCode()) {
			case KeyEvent.VK_RIGHT:
				for(String s: list) {
					if(s.startsWith(text)) {
						textField.setText(s);
						return;
					}
				}
				break;
			case KeyEvent.VK_ENTER:
				if(!list.contains(text)) {
					list.addElement(text);
					Collections.sort(list);
					setSuggestionModel(comboBox, getSuggestedModel(list, text), text);
				}
				shouldHide = true;
				break;
			case KeyEvent.VK_ESCAPE:
				shouldHide = true;
				break;
			}
		}
		private void setSuggestionModel(JComboBox comboBox, ComboBoxModel mdl, String str) {
			comboBox.setModel(mdl);
			comboBox.setSelectedIndex(-1);
			((JTextField)comboBox.getEditor().getEditorComponent()).setText(str);
		}
		private ComboBoxModel getSuggestedModel(Vector<String> list, String text) {
			DefaultComboBoxModel m = new DefaultComboBoxModel();
			for(String s: list) {
				if(s.startsWith(text)) m.addElement(s);
			}
			return m;
		}
	}

	public Object getInternalValue() {
		Object value = this.getItemAt(this.getSelectedIndex());
		if (value == null) {
			value = "";
		}
		return value;
	}

	public Object getExternalValue() {
		return this.getInternalValue();
	}
	
	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public String getValueClass() {
		return "String";
	}
	
	public void setValue(Object obj) {
		JTextField field = (JTextField)this.getEditor().getEditorComponent();
		field.setText(obj.toString());
	}
	
	public void setWidth(int width) {
		setWidth(width);
	}

	public int getRows() {
		return rows_;
	}

	public boolean isComponentFocusable() {
		return isFocusable();
	}
}

class XFYMonthBox extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private int rows_ = 1;
	private JTextField jTextField = new JTextField();
	private JTextField jTextFieldYear = new JTextField();
	private JComboBox<String> jComboBoxMonth = new JComboBox<String>();
	private ArrayList<String> listMonth = new ArrayList<String>();
	private boolean isEditable = false;
	private String oldValue = "";
    private Session session_;
    private String language = "";
	
	public XFYMonthBox(Session session){
		super();

		session_ = session;
		language = session_.getDateFormat().substring(0, 2);

		jTextField.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTextField.setEditable(false);
		jTextFieldYear.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTextFieldYear.setEditable(true);
		jTextFieldYear.setDocument(new LimitedDocument());

		jComboBoxMonth.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jComboBoxMonth.addKeyListener(new XFYMonthBox_Month_keyAdapter(this));

		listMonth.add("");
		listMonth.add("01");
		listMonth.add("02");
		listMonth.add("03");
		listMonth.add("04");
		listMonth.add("05");
		listMonth.add("06");
		listMonth.add("07");
		listMonth.add("08");
		listMonth.add("09");
		listMonth.add("10");
		listMonth.add("11");
		listMonth.add("12");
		if (language.equals("en")) {
			jTextFieldYear.setBounds(new Rectangle(71, 0, 80, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMonth.setBounds(new Rectangle(0, 0, 70, XFUtility.FIELD_UNIT_HEIGHT));
			jTextField.setBounds(new Rectangle(0, 0, 151, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(151, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMonth.addItem("");
			jComboBoxMonth.addItem("Jan");
			jComboBoxMonth.addItem("Feb");
			jComboBoxMonth.addItem("Mar");
			jComboBoxMonth.addItem("Apr");
			jComboBoxMonth.addItem("May");
			jComboBoxMonth.addItem("Jun");
			jComboBoxMonth.addItem("Jul");
			jComboBoxMonth.addItem("Aug");
			jComboBoxMonth.addItem("Sep");
			jComboBoxMonth.addItem("Oct");
			jComboBoxMonth.addItem("Nov");
			jComboBoxMonth.addItem("Dec");
		}
		if (language.equals("jp")) {
			jTextFieldYear.setBounds(new Rectangle(0, 0, 80, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMonth.setBounds(new Rectangle(81, 0, 60, XFUtility.FIELD_UNIT_HEIGHT));
			jTextField.setBounds(new Rectangle(0, 0, 141, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(141, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMonth.addItem("");
			jComboBoxMonth.addItem("01");
			jComboBoxMonth.addItem("02");
			jComboBoxMonth.addItem("03");
			jComboBoxMonth.addItem("04");
			jComboBoxMonth.addItem("05");
			jComboBoxMonth.addItem("06");
			jComboBoxMonth.addItem("07");
			jComboBoxMonth.addItem("08");
			jComboBoxMonth.addItem("09");
			jComboBoxMonth.addItem("10");
			jComboBoxMonth.addItem("11");
			jComboBoxMonth.addItem("12");
		}

		this.setLayout(null);
	}
	
	public void addActionListener(ActionListener listener) {
		jComboBoxMonth.addActionListener(listener);
	}

	public void setEditable(boolean editable) {
		this.removeAll();
		if (editable) {
			this.add(jTextFieldYear);
			this.add(jComboBoxMonth);
		} else {
			this.add(jTextField);
			this.setFocusable(false);
		}
		isEditable = editable;
	}

	public Object getInternalValue() {
		String value = "";
		try {
			if (!jTextFieldYear.getText().equals("") && jComboBoxMonth.getSelectedIndex() > 0) {
				int workInt = Integer.parseInt(jTextFieldYear.getText());
				value = Integer.toString(workInt) + listMonth.get(jComboBoxMonth.getSelectedIndex());
				value = "000" + value;
				value = value.substring(value.length() - 6, value.length());
			}
		} catch (NumberFormatException e) {}
		return value;
	}

	public Object getExternalValue() {
		return this.getInternalValue();
	}
	
	public boolean isEditable() {
		return isEditable;
	}

	public boolean isComponentFocusable() {
		return isEditable;
	}

	public boolean isFocusable() {
		return false;
	}

	public void setValue(Object obj) {
		jComboBoxMonth.setSelectedIndex(0);
		jTextField.setText("");
		jTextFieldYear.setText("");

		String value = (String)obj;
		if (value != null) {
			value = value.trim();
		}
		if (value != null && !value.equals("")) {
			if (value.length() == 6) {
				String yearValue = value.substring(0, 4);
				String monthValue = value.substring(4, 6);
				jTextFieldYear.setText(yearValue);
				int index = listMonth.indexOf(monthValue);
				if (index == -1) {
					jComboBoxMonth.setSelectedIndex(0);
				} else {
					jComboBoxMonth.setSelectedIndex(index);
				}
				if (language.equals("en")) {
					jTextField.setText(jComboBoxMonth.getItemAt(jComboBoxMonth.getSelectedIndex()).toString() + ", " + jTextFieldYear.getText());
				}
				if (language.equals("jp")) {
					jTextField.setText(jTextFieldYear.getText() + "年" + jComboBoxMonth.getItemAt(jComboBoxMonth.getSelectedIndex()).toString() + "月");
				}
			}
		}
	}
	
	public void setWidth(int width) {
	}
	
	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setForeground(Color color) {
		if (jTextFieldYear != null) {
			jTextFieldYear.setForeground(color);
		}
		if (jComboBoxMonth != null) {
			jComboBoxMonth.setForeground(color);
		}
		if (jTextField != null) {
			jTextField.setForeground(color);
		}
	}

	public void setBackground(Color color) {
		if (jTextFieldYear != null) {
			jTextFieldYear.setBackground(color);
		}
		if (jComboBoxMonth != null) {
			jComboBoxMonth.setBackground(color);
		}
	}

	public int getRows() {
		return rows_;
	}

	void jComboBoxMonth_keyPressed(KeyEvent e) {
	    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				jComboBoxMonth.showPopup();
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER && !jComboBoxMonth.isPopupVisible()) {
			this.requestFocus();
			this.dispatchEvent(e);
		}
	}

	class LimitedDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;
		public void insertString(int offset, String str, AttributeSet attr) {
			try {
				String preStr = jTextFieldYear.getText();
				int workInt = Integer.parseInt(preStr + str);
				if (workInt >= 0 && workInt <= 9999) {
					super.insertString( offset, str, attr );
				}
			} catch (Exception e) {}
		}
	}
}

class XFFYearBox extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private int rows_ = 1;
	private JTextField jTextField = new JTextField();
	private JComboBox<String> jComboBoxYear = new JComboBox<String>();
	private ArrayList<String> listYear = new ArrayList<String>();
	private boolean isEditable = false;
	private String oldValue = "";
    private Session session_;
    private String dateFormat = "";
    private String language = "";
	
	public XFFYearBox(Session session){
		super();

		session_ = session;
		dateFormat = session_.getDateFormat();
		language = session_.getDateFormat().substring(0, 2);

		jTextField.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTextField.setEditable(false);
		//jTextField.setFocusable(false);
		jTextField.setBounds(new Rectangle(0, 0, 110, XFUtility.FIELD_UNIT_HEIGHT));

		GregorianCalendar calendar = new GregorianCalendar();
		int currentYear = calendar.get(Calendar.YEAR);
		int minimumYear = currentYear - 30;
		int maximumYear = currentYear + 10;

		jComboBoxYear.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE-2));
		jComboBoxYear.addKeyListener(new XFFYearBox_keyAdapter(this));
		jComboBoxYear.setBounds(new Rectangle(0, 0, 110, XFUtility.FIELD_UNIT_HEIGHT));

		listYear.add("");
		jComboBoxYear.addItem("");
		for (int i = minimumYear; i <= maximumYear; i++) {
			listYear.add(String.valueOf(i));
			jComboBoxYear.addItem(XFUtility.getUserExpressionOfYearMonth(String.valueOf(i), dateFormat));
		}
		listYear.add("9999");
		if (language.equals("en")
				|| dateFormat.equals("jp00")
				|| dateFormat.equals("jp01")
				|| dateFormat.equals("jp10")
				|| dateFormat.equals("jp11")
				|| dateFormat.equals("jp20")
				|| dateFormat.equals("jp21")) {
			jComboBoxYear.addItem(XFUtility.getUserExpressionOfYearMonth("9999", session_.getDateFormat()));
		} else {
			jComboBoxYear.addItem("H99");
		}

		this.setLayout(null);
		this.setSize(new Dimension(110, XFUtility.FIELD_UNIT_HEIGHT));
	}
	
	public void addActionListener(ActionListener listener) {
		jComboBoxYear.addActionListener(listener);
	}

	public void setEditable(boolean editable) {
		this.removeAll();
		if (editable) {
			this.add(jComboBoxYear);
		} else {
			this.add(jTextField);
			this.setFocusable(false);
		}
		isEditable = editable;
	}
	
	public void setToolTipText(String text) {
		jTextField.setToolTipText(text);
		jComboBoxYear.setToolTipText(text);
	}

	public Object getInternalValue() {
		String year = listYear.get(jComboBoxYear.getSelectedIndex());
		return year;
	}

	public Object getExternalValue() {
		return jComboBoxYear.getSelectedItem();
	}
	
	public boolean isEditable() {
		return isEditable;
	}

	public boolean isComponentFocusable() {
		return isEditable;
	}

	public boolean isFocusable() {
		return false;
	}
	
	public void setValue(Object obj) {
		String value = obj.toString();
		if (value != null) {
			value = value.trim();
		}
		if (value == null || value.equals("")) {
			jComboBoxYear.setSelectedIndex(0);
			jTextField.setText("");
		} else {
			if (value.length() == 4) {
				String yearValue = value.substring(0, 4);
				int index = listYear.indexOf(yearValue);
				if (index == -1) {
					jComboBoxYear.setSelectedIndex(0);
				} else {
					jComboBoxYear.setSelectedIndex(index);
				}
				jTextField.setText(jComboBoxYear.getItemAt(jComboBoxYear.getSelectedIndex()).toString());
			}
		}
	}
	
	public void setWidth(int width) {
	}
	
	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setForeground(Color color) {
		if (jComboBoxYear != null) {
			jComboBoxYear.setForeground(color);
		}
		if (jTextField != null) {
			jTextField.setForeground(color);
		}
	}

	public void setBackground(Color color) {
		if (jComboBoxYear != null) {
			jComboBoxYear.setBackground(color);
		}
	}

	public int getRows() {
		return rows_;
	}

	void jComboBoxYear_keyPressed(KeyEvent e) {
	    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				jComboBoxYear.showPopup();
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER && !jComboBoxYear.isPopupVisible()) {
			this.requestFocus();
			this.dispatchEvent(e);
		}
	}
}

class XFMSeqBox extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private int rows_ = 1;
	private JTextField jTextField = new JTextField();
	private JComboBox<String> jComboBoxMSeq = new JComboBox<String>();
	private ArrayList<Integer> listMSeq = new ArrayList<Integer>();
	private boolean isEditable = false;
    private Session session_;
    private String language = "";
    private String[] monthArrayEn = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec","Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov"};
    private String[] monthArrayJp = {"１月度","２月度","３月度","４月度","５月度","６月度","７月度","８月度","９月度","10月度","11月度","12月度","１月度","２月度","３月度","４月度","５月度","６月度","７月度","８月度","９月度","10月度","11月度"};
    private int startMonth = 1;
    private int value_ = 0;
	private int oldValue_ = 0;
	
	public XFMSeqBox(Session session){
		super();

		session_ = session;
		language = session_.getDateFormat().substring(0, 2);
		startMonth = session_.getSystemVariantInteger("FIRST_MONTH");

		jTextField.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jTextField.setEditable(false);
		//jTextField.setFocusable(false);

		jComboBoxMSeq.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE-2));
		jComboBoxMSeq.addKeyListener(new XFMSeqBox_keyAdapter(this));

		listMSeq.add(0);
		listMSeq.add(1);
		listMSeq.add(2);
		listMSeq.add(3);
		listMSeq.add(4);
		listMSeq.add(5);
		listMSeq.add(6);
		listMSeq.add(7);
		listMSeq.add(8);
		listMSeq.add(9);
		listMSeq.add(10);
		listMSeq.add(11);
		listMSeq.add(12);

		if (language.equals("en")) {
			jComboBoxMSeq.setBounds(new Rectangle(0, 0, 60, XFUtility.FIELD_UNIT_HEIGHT));
			jTextField.setBounds(new Rectangle(0, 0, 60, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(60, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMSeq.addItem("");
			for (int i = startMonth -1; i < startMonth + 11; i++) {
				jComboBoxMSeq.addItem(monthArrayEn[i]);
			}
		}

		if (language.equals("jp")) {
			jComboBoxMSeq.setBounds(new Rectangle(0, 0, 80, XFUtility.FIELD_UNIT_HEIGHT));
			jTextField.setBounds(new Rectangle(0, 0, 80, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(80, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMSeq.addItem("");
			for (int i = startMonth -1; i < startMonth + 11; i++) {
				jComboBoxMSeq.addItem(monthArrayJp[i]);
			}
		}

		this.setLayout(null);
	}
	
	public void addActionListener(ActionListener listener) {
		jComboBoxMSeq.addActionListener(listener);
	}

	public void setEditable(boolean editable) {
		this.removeAll();
		if (editable) {
			this.add(jComboBoxMSeq);
		} else {
			this.add(jTextField);
			this.setFocusable(false);
		}
		isEditable = editable;
	}
	
	public void setToolTipText(String text) {
		jTextField.setToolTipText(text);
		jComboBoxMSeq.setToolTipText(text);
	}

	public Object getInternalValue() {
		return listMSeq.get(jComboBoxMSeq.getSelectedIndex()).toString();
	}

	public Object getExternalValue() {
		return jComboBoxMSeq.getSelectedItem();
	}
	
	public boolean isEditable() {
		return isEditable;
	}

	public boolean isComponentFocusable() {
		return isEditable;
	}

	public boolean isFocusable() {
		return false;
	}
	
	public void setValue(Object obj) {
		try {
			value_ = Integer.parseInt(obj.toString());
		} catch (NumberFormatException e) {
			value_ = 0;
		}
		jComboBoxMSeq.setSelectedIndex(value_);
		jTextField.setText(jComboBoxMSeq.getItemAt(value_).toString());
	}
	
	public void setWidth(int width) {
	}
	
	public void setOldValue(Object obj) {
		try {
			oldValue_ = Integer.parseInt(obj.toString());
		} catch (NumberFormatException e) {
			oldValue_ = 0;
		}
	}

	public Object getOldValue() {
		return Integer.toString(oldValue_);
	}

	public void setForeground(Color color) {
		if (jComboBoxMSeq != null) {
			jComboBoxMSeq.setForeground(color);
		}
		if (jTextField != null) {
			jTextField.setForeground(color);
		}
	}

	public void setBackground(Color color) {
		if (jComboBoxMSeq != null) {
			jComboBoxMSeq.setBackground(color);
		}
	}

	public int getRows() {
		return rows_;
	}

	void jComboBoxMSeq_keyPressed(KeyEvent e) {
	    if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0){
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				jComboBoxMSeq.showPopup();
			}
		}
		if (e.getKeyCode() == KeyEvent.VK_ENTER && !jComboBoxMSeq.isPopupVisible()) {
			this.requestFocus();
			this.dispatchEvent(e);
		}
	}
}


class XFCheckBox extends JCheckBox implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private int rows_ = 1;
	private String dataTypeOptions_ = "";
    private String valueTrue = "";
    private String valueFalse = "";
    private String value_ = "";
	private String oldValue_ = "";
	
	public XFCheckBox(String dataTypeOptions){
		super();

		dataTypeOptions_ = dataTypeOptions;
		String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions_, "BOOLEAN");
		if (!wrkStr.equals("")) {
			StringTokenizer workTokenizer = new StringTokenizer(wrkStr, ";");
			if (workTokenizer.countTokens() == 1) {
			    valueTrue = workTokenizer.nextToken();
			    valueFalse = "";
			}
			if (workTokenizer.countTokens() == 2) {
			    valueTrue = workTokenizer.nextToken();
			    valueFalse = workTokenizer.nextToken();
			}
		}

	    this.setDisabledSelectedIcon(XFUtility.ICON_CHECK_1D);
	    this.setDisabledIcon(XFUtility.ICON_CHECK_0D);
	    this.setSelectedIcon(XFUtility.ICON_CHECK_1A);
	    this.setIcon(XFUtility.ICON_CHECK_0A);
	    this.setRolloverSelectedIcon(XFUtility.ICON_CHECK_1R);
	    this.setRolloverIcon(XFUtility.ICON_CHECK_0R);
		this.setText("");
		this.setOpaque(false);
		this.addActionListener(new XFCheckBox_actionAdapter());
		this.addFocusListener(new XFCheckBox_focusListener());
		this.setSize(20, XFUtility.FIELD_UNIT_HEIGHT);
	}

	public void setEditable(boolean editable) {
		super.setEnabled(editable);
		super.setFocusable(editable);
	}

	public Object getInternalValue() {
		return value_;
	}

	public Object getExternalValue() {
		return value_;
	}

	public String getTrueValue() {
		return valueTrue;
	}

	public String getFalseValue() {
		return valueFalse;
	}
	
	public boolean isEditable() {
		return super.isEnabled();
	}
	
	public boolean isComponentFocusable() {
		return this.isFocusable();
	}
	
	public void setValue(Object obj) {
		this.setSelected(false);
		value_ = obj.toString();
		if (value_.equals(valueTrue)) {
			this.setSelected(true);
		}
		if (value_.equals(valueFalse)) {
			this.setSelected(false);
		}
	}
	
	public void setWidth(int width) {
	}
	
	public void setOldValue(Object obj) {
		oldValue_ = obj.toString();
	}

	public Object getOldValue() {
		return oldValue_;
	}

	public int getRows() {
		return rows_;
	}

	class XFCheckBox_actionAdapter implements java.awt.event.ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (isSelected()) {
				value_ = valueTrue;
			} else {
				value_ = valueFalse;
			}
		}
	}
	class XFCheckBox_focusListener implements FocusListener{
		public void focusLost(FocusEvent event){
			setSelectedIcon(XFUtility.ICON_CHECK_1A);
			setIcon(XFUtility.ICON_CHECK_0A);
		}
		public void focusGained(FocusEvent event){
			setSelectedIcon(XFUtility.ICON_CHECK_1R);
			setIcon(XFUtility.ICON_CHECK_0R);
		}
	}
}

class XFUrlField extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private JTextField jTextField = new JTextField();
	private JLabel jLabel = new JLabel();
	private Desktop desktop = Desktop.getDesktop();
	private int rows_ = 1;
	private String oldValue = "";

	public XFUrlField(int digits, String fieldOptions, String fontName){
		super();

		jTextField.setFont(new java.awt.Font(fontName, 0, XFUtility.FONT_SIZE));
		jTextField.setDocument(new LimitedDocument(digits));
		jTextField.setEditable(false);
		Font labelFont = new java.awt.Font(fontName, 0, XFUtility.FONT_SIZE);
		jLabel.setFont(labelFont);
		jLabel.setForeground(Color.blue);
		jLabel.setHorizontalAlignment(SwingConstants.LEFT);
		jLabel.addMouseListener(new jLabel_mouseAdapter(this));
		jLabel.setBorder(jTextField.getBorder());
		jLabel.setFocusable(false);
		this.setLayout(new BorderLayout());
		this.add(jLabel, BorderLayout.CENTER);

		int fieldWidth,  fieldHeight;
		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		} else {
			fieldWidth = digits * (XFUtility.FONT_SIZE/2) + 10;
		}
		if (fieldWidth > 800) {
			fieldWidth = 800;
		}
		fieldHeight = XFUtility.FIELD_UNIT_HEIGHT;
		this.setSize(fieldWidth, fieldHeight);
	}
	
	public void setWidth(int width) {
		this.setSize(width, this.getHeight());
	}

	public void exitFromComponent() {
		jTextField.transferFocus();
	}

	public boolean isEditable() {
		return jTextField.isEditable();
	}

	public boolean isFocusable() {
		return false;
	}

	public boolean isComponentFocusable() {
		return jTextField.isFocusable();
	}

	public void setForeground(Color color) {
		if (jTextField != null) {
			jTextField.setForeground(color);
		}
	}

	public void setBackground(Color color) {
		if (jTextField != null) {
			jTextField.setBackground(color);
		}
	}

	public void setValue(Object obj) {
		String text = (String)obj;
		text = text.trim();
		jLabel.setText("<html><u>" + text);
		jTextField.setText(text);
	}

	public String getInternalValue() {
		return jTextField.getText();
	}

	public Object getExternalValue() {
		return this.getInternalValue();
	}
	
	public void setOldValue(Object obj) {
		oldValue = (String)obj;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void requestFocus() {
		jTextField.requestFocus();
	}
	
	public void setEditable(boolean editable) {
		if (editable) {
			this.removeAll();
			this.setLayout(new BorderLayout());
			this.add(jTextField, BorderLayout.CENTER);
		}
		jTextField.setEditable(editable);
		//jTextField.setFocusable(editable);
	}
	
	public void setToolTipText(String text) {
		jTextField.setToolTipText(text);
	}

	class LimitedDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;
		int limit;
		LimitedDocument(int limit) {
			this.limit = limit; 
		}
		public void insertString(int offset, String str, AttributeSet a) {
			if (offset >= limit ) {
				return;
			}
			try {
				super.insertString( offset, str, a );
			} catch(Exception e ) {
			}
		}
	}
	class jLabel_mouseAdapter extends java.awt.event.MouseAdapter {
		XFUrlField adaptee;
		jLabel_mouseAdapter(XFUrlField adaptee) {
			this.adaptee = adaptee;
		}
		public void mousePressed(MouseEvent e) {
			String fileName = jTextField.getText();
			try {
				if (!fileName.equals("")) {
					if (fileName.contains("@")) {
						adaptee.desktop.browse(new URI("mailto:" + fileName));
					} else {
						if (fileName.contains("http://") || fileName.contains("https://")) {
							adaptee.desktop.browse(new URI(fileName));
						} else {
							fileName = fileName.replaceAll(" ", "%20");
							fileName = fileName.replaceAll("\\\\", "/");
							fileName = fileName.replace("file://", "");
							adaptee.desktop.browse(new URI("file://" + fileName));
						}
					}
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Failed to browse the file called '" + jTextField.getText() + "'.");
			}
		}
		public void mouseReleased(MouseEvent e) {
		}
		public void mouseEntered(MouseEvent e) {
			if (!jTextField.getText().equals("")) {
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}
		}
		public void mouseExited(MouseEvent e) {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}
	}

	public int getRows() {
		return rows_;
	}
}

class XFBlobField extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private JButton jButton = new JButton();
	private Desktop desktop = Desktop.getDesktop();
	private int rows_ = 1;
	private Blob value_ = null;
	private Blob oldValue_ = null;
	private boolean isEditable;
	private String fileType = "png";
	private File tempFile = null;

	public XFBlobField(String fieldOptions){
		super();
		ImageIcon imageIcon = new ImageIcon(xeadDriver.XFUtility.class.getResource("prompt.png"));
	 	jButton.setIcon(imageIcon);
		jButton.addActionListener(new jButton_actionAdapter(this));
		this.setLayout(new BorderLayout());
		this.add(jButton, BorderLayout.CENTER);
		this.setSize(80, XFUtility.FIELD_UNIT_HEIGHT);
	}
	
	public void setWidth(int width) {
		this.setSize(width, this.getHeight());
	}

	public void exitFromComponent() {
		jButton.transferFocus();
	}

	public boolean isEditable() {
		return isEditable;
	}

	public boolean isFocusable() {
		return true;
	}

	public boolean isComponentFocusable() {
		return true;
	}

	public void setBackground(Color color) {
		if (jButton != null) {
			jButton.setBackground(color);
		}
	}

	public void requestFocus() {
		if (jButton != null) {
			jButton.requestFocus();;
		}
	}

	public void setValue(Object value) {
		try {
			value_ = (Blob)value;
		} catch (Exception e) {
			value_ = null;
		}
	}

	public Object getInternalValue() {
		if (isEditable && tempFile != null) {
			try {
				FileInputStream fis = new FileInputStream(tempFile);
				OutputStream os = value_.setBinaryStream(1);
				byte[] buffer = new byte[4096];
				int length = -1;
				while((length = fis.read(buffer)) != -1) {
					os.write(buffer ,0 ,length );
				}
				fis.close();
				os.close();
			} catch (Exception e) {
			}
		}
		return value_;
	}

	public Object getExternalValue() {
		return "";
	}
	
	public void setOldValue(Object value) {
		try {
			oldValue_ = (Blob)value;
		} catch (Exception e) {
			oldValue_ = null;
		}
	}

	public Object getOldValue() {
		return oldValue_;
	}
	
	public void setEditable(boolean editable) {
		isEditable = editable;
		if (jButton != null) {
			jButton.setFocusable(isEditable);
		}
	}
	
	public void setToolTipText(String text) {
		jButton.setToolTipText(text);
	}

	class jButton_actionAdapter implements java.awt.event.ActionListener {
		XFBlobField adaptee;
		jButton_actionAdapter(XFBlobField adaptee) {
			this.adaptee = adaptee;
		}
		public void actionPerformed(ActionEvent e) {
			try {
				if (tempFile == null) {
					tempFile = File.createTempFile("XTEADriver", "." + fileType);
					tempFile.deleteOnExit();
				}
				if (value_ != null) {
					FileOutputStream fos = new FileOutputStream(tempFile);
					InputStream is = value_.getBinaryStream();
					if(is != null ){
						byte[] buffer = new byte[4096];
						int length = -1;
						while((length = is.read(buffer)) != -1) {
							fos.write(buffer ,0 ,length );
						}
					}
					is.close();
					fos.close();
				}

				if (isEditable) {
					adaptee.desktop.edit(tempFile);
				} else {
					if (value_ == null) {
						JOptionPane.showMessageDialog(null, "NULL!");
					} else {
						adaptee.desktop.browse(new URI("file:" + tempFile.getPath()));
					}
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Failed to process BLOB data.\n" + ex.getMessage());
			}
		}
	}

	public int getRows() {
		return rows_;
	}
}

class TableModelReadOnly extends DefaultTableModel {
	private static final long serialVersionUID = 1L;
	public boolean isCellEditable(int row, int col) {return false;}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int col){
		return getValueAt(0, col).getClass();
	}
}

class HeaderBorder implements Border {
	public Insets getBorderInsets(Component c){
		return new Insets(2, 2, 2, 2);
	}
	public boolean isBorderOpaque(){
		return false;
	}
	public void paintBorder (Component c, Graphics g, int x, int y, int width, int height){
		g.setColor(Color.white);
		g.drawLine(x, y, x+width, y);
		g.drawLine(x, y, x, y+height);
		g.setColor(Color.gray);
		g.drawLine(x, y+height-1, x+width, y+height-1);
		g.drawLine(x+width-1, y+height, x+width-1, y);
	}
}

class CellBorder implements Border {
	public Insets getBorderInsets(Component c){
		return new Insets(0, 0, 0, 1);
	}
	public boolean isBorderOpaque(){
		return false;
	}
	public void paintBorder (Component c, Graphics g, int x, int y, int width, int height){
		g.setColor(Color.gray);
		g.drawLine(x+width-1, y+height+1, x+width-1, y);
	}
}

class TableCellReadOnly extends Object {
	private Object internalValue_ = null;
	private Object externalValue_ = null;
	private Color color_ = null;
	private String valueType_ = "";
	public TableCellReadOnly(Object internalValue, Object externalValue, Color color, String valueType) {
		internalValue_ = internalValue;
		externalValue_ = externalValue;
		color_ = color;
		valueType_ = valueType;
	}
	public Object getInternalValue() {
		return internalValue_;
	}
	public Object getExternalValue() {
		return externalValue_;
	}
	public Color getColor() {
		return color_;
	}
	public String getValueType() {
		return valueType_;
	}
}

//class ElementComparator implements java.util.Comparator<org.w3c.dom.Element> {
//	private String attName_ = "";
//	public ElementComparator (String attName) {
//		super();
//		attName_ = attName;
//	}
//    public int compare(org.w3c.dom.Element element1, org.w3c.dom.Element element2 ) {
//      String value1, value2;
//      value1 = element1.getAttribute(attName_);
//      value2 = element2.getAttribute(attName_);
//      int compareResult = value1.compareTo(value2);
//      if (compareResult == 0) {
//          compareResult = 1;
//      }
//      return(compareResult);
//    }
//}

class SortableDomElementListModel extends DefaultListModel {
	private static final long serialVersionUID = 1L;
	private String attName_ = "";
	public SortableDomElementListModel(String attName) {
		super();
		attName_ = attName;
	}
	public void addElement(Object object) {
		XeadElement element = new XeadElement((org.w3c.dom.Element)object, attName_);
		super.addElement(element);
	}
    public void sortElements() {
//      TreeSet<org.w3c.dom.Element> treeSet = new TreeSet<org.w3c.dom.Element>(new ElementComparator(attName_));
//      int elementCount = this.getSize();
//      org.w3c.dom.Element domElement;
//      for (int i = 0; i < elementCount; i++) {
//        domElement = (org.w3c.dom.Element)this.getElementAt(i);
//        treeSet.add(domElement);
//      }
//      this.removeAllElements();
//      Iterator<org.w3c.dom.Element> it = treeSet.iterator();
//      while( it.hasNext() ){
//        domElement = (org.w3c.dom.Element)it.next();
//        this.addElement(domElement);
//      }
		ArrayList<XeadElement> list = new ArrayList<XeadElement>();
		for (int i = 0; i < this.getSize(); i++) {
			list.add((XeadElement)super.getElementAt(i));
		}
		this.removeAllElements();
		Collections.sort(list);
		Iterator<XeadElement> it = list.iterator();
		while(it.hasNext()){
			super.addElement(it.next());
		}
    }
	public Object getElementAt(int index) {
		XeadElement element = (XeadElement)super.getElementAt(index);
		return element.getElement();
	}
}

/**
 * Class of Comparable Element
 */
class XeadElement implements Comparable {
	private org.w3c.dom.Element domNode_;
	private String attName_ = "";
	public XeadElement(org.w3c.dom.Element node, String attName) {
		super();
		domNode_ = node;
		attName_ = attName;
	}
	public org.w3c.dom.Element getElement() {
		return domNode_;
	}
    public int compareTo(Object other) {
        XeadElement otherNode = (XeadElement)other;
        return domNode_.getAttribute(attName_).compareTo(otherNode.getElement().getAttribute(attName_));
    }
}

class XFScript extends Object {
	private ArrayList<String> fieldList = new ArrayList<String>();
	private String tableID = "";
	private String scriptName = "";
	private String scriptText = "";
	private String eventP = "";
	private String eventR = "";
	private boolean isSuspended = false;

	public XFScript(String tableID, org.w3c.dom.Element scriptElement, NodeList tableNodeList) {
		super();
		this.tableID = tableID;
		eventP = scriptElement.getAttribute("EventP");
		eventR = scriptElement.getAttribute("EventR");
		scriptName = scriptElement.getAttribute("Name");
		scriptText = XFUtility.substringLinesWithTokenOfEOL(scriptElement.getAttribute("Text"), "\n");
		fieldList = XFUtility.getDSNameListInScriptText(XFUtility.removeCommentsFromScriptText(scriptText), tableNodeList);
		if (scriptElement.getAttribute("Hold").equals("T")) {
			isSuspended = true;
		}
	}
	
	public String getScriptText() {
		return scriptText;
	}
	
	public boolean isToBeRunAtEvent(String event1, String event2) {
		boolean result = false;
		if (!isSuspended) {
			ArrayList<String> event1List = new ArrayList<String>();
			StringTokenizer workTokenizer = new StringTokenizer(event1, ",");
			while (workTokenizer.hasMoreTokens()) {
				event1List.add(workTokenizer.nextToken());
			}

			for (int i = 0; i < event1List.size(); i++) {
				if (eventP.contains(event1List.get(i))) {
					if (event2.equals("")) {
						result = true;
						break;
					} else {
						if (event2.equals(eventR)) {
							result = true;
							break;
						}
					}
				}
			}
		}
		return result;
	}
	
	public ArrayList<String> getFieldList() {
		return fieldList;
	}

	public String getName() {
		return tableID + " " + scriptName;
	}
}
	
class XFCalendar extends JDialog {
	private static final long serialVersionUID = 1L;
	private Session session_;
    private DateButton[] dateButtonArray = new DateButton[42];
    private JPanel jPanelMain = new JPanel();
    private JPanel jPanelTop = new JPanel();
    private JTextArea jTextAreaBottom = new JTextArea();
    private JPanel jPanelCenter = new JPanel();
    private JTabbedPane jTabbedPaneCenter = new JTabbedPane();
    private JLabel jLabelYearMonth = new JLabel();
    private JLabel jLabelDateComment = new JLabel();
    private JLabel jLabelSun = new JLabel();
    private JLabel jLabelMon = new JLabel();
    private JLabel jLabelTue = new JLabel();
    private JLabel jLabelWed = new JLabel();
    private JLabel jLabelThu = new JLabel();
    private JLabel jLabelFri = new JLabel();
    private JLabel jLabelSat = new JLabel();
    private Calendar calendarForToday;
    private Date maxValueDate = new Date();
    private Dimension scrSize, dlgSize;
    private SimpleDateFormat day = new SimpleDateFormat("d");
    private SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");
    private Date date = new Date();
    private Date selectedDate = null;
    private ArrayList<String> kbCalendarList = new ArrayList<String>(); 
    private HashMap<String, String> offDateMap = new HashMap<String,String>();
	private Color lightRedGray = new Color(228, 192, 192);

    public XFCalendar(Session session) {
		super();

		this.setModal(true);
		this.setTitle(XFUtility.RESOURCE.getString("Calendar"));
		this.session_ = session;

		jPanelMain.setLayout(new BorderLayout());
		scrSize = Toolkit.getDefaultToolkit().getScreenSize();

		jPanelTop.setPreferredSize(new Dimension(346, 20));
		jPanelTop.setLayout(null);
		jLabelYearMonth.setFont(new java.awt.Font(session_.systemFont, 0, XFUtility.FONT_SIZE));
		jLabelYearMonth.setBounds(5, 0, 200, 20);
		jLabelDateComment.setFont(new java.awt.Font(session_.systemFont, 0, 12));
		jLabelDateComment.setHorizontalAlignment(SwingConstants.RIGHT);
		jLabelDateComment.setForeground(Color.gray);
		jLabelDateComment.setBounds(200, 3, 142, 17);
		jPanelTop.add(jLabelYearMonth);
		jPanelTop.add(jLabelDateComment);

		jPanelCenter.setBackground(Color.white);
		jPanelCenter.setBorder(null);
		jPanelCenter.setLayout(null);
		jPanelCenter.setPreferredSize(new Dimension(332, 158));
		jLabelSun.setFont(new java.awt.Font(session_.systemFont, 0, 12));
		jLabelSun.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelSun.setBounds(3, 2, 48, 13);
		jPanelCenter.add(jLabelSun);
		jLabelMon.setFont(new java.awt.Font(session_.systemFont, 0, 12));
		jLabelMon.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelMon.setBounds(50, 2, 48, 13);
		jPanelCenter.add(jLabelMon);
		jLabelTue.setFont(new java.awt.Font(session_.systemFont, 0, 12));
		jLabelTue.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelTue.setBounds(98, 2, 48, 13);
		jPanelCenter.add(jLabelTue);
		jLabelWed.setFont(new java.awt.Font(session_.systemFont, 0, 12));
		jLabelWed.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelWed.setBounds(146, 2, 48, 13);
		jPanelCenter.add(jLabelWed);
		jLabelThu.setFont(new java.awt.Font(session_.systemFont, 0, 12));
		jLabelThu.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelThu.setBounds(194, 2, 48, 13);
		jPanelCenter.add(jLabelThu);
		jLabelFri.setFont(new java.awt.Font(session_.systemFont, 0, 12));
		jLabelFri.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelFri.setBounds(242, 2, 48, 13);
		jPanelCenter.add(jLabelFri);
		jLabelSat.setFont(new java.awt.Font(session_.systemFont, 0, 12));
		jLabelSat.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelSat.setBounds(290, 2, 48, 13);
		jPanelCenter.add(jLabelSat);

		String language = session_.getDateFormat().substring(0, 2);
		if (language.equals("en")) {
			jLabelSun.setText("Sun");
			jLabelMon.setText("Mon");
			jLabelTue.setText("Tue");
			jLabelWed.setText("Wed");
			jLabelThu.setText("Thur");
			jLabelFri.setText("Fri");
			jLabelSat.setText("Sat");
		}
		if (language.equals("jp")) {
			jLabelSun.setText("日");
			jLabelMon.setText("月");
			jLabelTue.setText("火");
			jLabelWed.setText("水");
			jLabelThu.setText("木");
			jLabelFri.setText("金");
			jLabelSat.setText("土");
		}

		int posX = 2;
		for(int i = 0; i < 42; i++) {
			dateButtonArray[i] = new DateButton();
			dateButtonArray[i].addActionListener(new XFCalendar_actionAdapter(this));
			dateButtonArray[i].addKeyListener(new XFCalendar_keyAdapter(this));
			if (i == 7 || i == 14 || i == 21 || i == 28 || i == 35) {
				posX = 2;
			}
			if (i >= 0 && i <= 6) {
				dateButtonArray[i].setBounds(posX, 15, 48, 20);
			}
			if (i >= 7 && i <= 13) {
				dateButtonArray[i].setBounds(posX, 36, 48, 20);
			}
			if (i >= 14 && i <= 20) {
				dateButtonArray[i].setBounds(posX, 57, 48, 20);
			}
			if (i >= 21 && i <= 27) {
				dateButtonArray[i].setBounds(posX, 78, 48, 20);
			}
			if (i >= 28 && i <= 34) {
				dateButtonArray[i].setBounds(posX, 99, 48, 20);
			}
			if (i >= 35 && i <= 41) {
				dateButtonArray[i].setBounds(posX, 120, 48, 20);
			}
			jPanelCenter.add(dateButtonArray[i]);
			posX = posX + 48;
		}

		calendarForToday = Calendar.getInstance();
		calendarForToday.setTime(new Date());

		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
			String str1 = "9999年12月31日";
			maxValueDate = sdf1.parse(str1);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		jTextAreaBottom.setPreferredSize(new Dimension(346, 40));
		jTextAreaBottom.setFont(new java.awt.Font(session_.systemFont, 0, 12));
		jTextAreaBottom.setEditable(false);
		jTextAreaBottom.setBackground(SystemColor.control);
		jTextAreaBottom.setText(XFUtility.RESOURCE.getString("CalendarComment"));

		try {
			StringBuffer statementBuf = new StringBuffer();
			statementBuf.append("select * from ");
			statementBuf.append(session_.getTableNameOfCalendar());
			XFTableOperator operator = new XFTableOperator(session_, null, statementBuf.toString(), true);
			while (operator.next()) {
				offDateMap.put(operator.getValueOf("KBCALENDAR").toString().trim()+";"+operator.getValueOf("DTOFF").toString(), operator.getValueOf("TXOFF").toString().trim());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		int count = 0;
		jTabbedPaneCenter.setFont(new java.awt.Font(session_.systemFont, 0, 14));
		jTabbedPaneCenter.setTabPlacement(JTabbedPane.BOTTOM);
		jTabbedPaneCenter.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		jTabbedPaneCenter.addChangeListener(new XFCalendar_changeAdapter(this));
		try {
			StringBuffer statementBuf = new StringBuffer();
			statementBuf.append("select * from ");
			statementBuf.append(session_.getTableNameOfUserVariants());
			statementBuf.append(" where IDUSERKUBUN = 'KBCALENDAR' order by SQLIST");
			XFTableOperator operator = new XFTableOperator(session_, null, statementBuf.toString(), true);
			while (operator.next()) {
				kbCalendarList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
				if (count == 0) {
					jTabbedPaneCenter.addTab(operator.getValueOf("TXUSERKUBUN").toString().trim(), jPanelCenter);
				} else {
					jTabbedPaneCenter.addTab(operator.getValueOf("TXUSERKUBUN").toString().trim(), null);
				}
				count++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (count == 0) {
			jTabbedPaneCenter.addTab("User variants for calendar are missing", jPanelCenter);
		}

		jPanelMain.add(jPanelTop, BorderLayout.NORTH);
		jPanelMain.add(jTabbedPaneCenter, BorderLayout.CENTER);
		jPanelMain.add(jTextAreaBottom, BorderLayout.SOUTH);

		this.getContentPane().add(jPanelMain);
		dlgSize = new Dimension(352,272);
		this.setPreferredSize(dlgSize);
		this.setResizable(false);
		this.pack();
    }

    public Date getDateOnCalendar(Date date, String kbCalendar, Point position) {
    	selectedDate = date;
    	this.date = date;

    	jLabelDateComment.setText("");
    	int index = kbCalendarList.indexOf(kbCalendar);
    	if (index >= 0) {
    		jTabbedPaneCenter.setSelectedIndex(index);
    	}
    	setupDates(this.date, 0);

		int posX = position.x;
		int posY = position.y;
		if (position.y + dlgSize.height > scrSize.height) {
			posY = position.y - dlgSize.height;
		}
		if (position.x + dlgSize.width > scrSize.width) {
			posX = position.x - dlgSize.width;
		}
		this.setLocation(posX, posY);
    	
    	super.setVisible(true);
    	return selectedDate;
    }
    
	public void setupDates(Date focusedDate, int offset) {
		int shownMonth = 0;
		int indexOfFocusedDate = 0;

		Calendar cal = Calendar.getInstance();
		if (focusedDate == null) {
			focusedDate = cal.getTime();
		}

		cal.setTime(focusedDate);
		if (offset < 0) {
			cal.add(Calendar.DATE, offset);
			focusedDate = cal.getTime();
		}
		if (offset == 0) {
		}
		if (offset > 0) {
			cal.add(Calendar.DATE, offset);
			focusedDate = cal.getTime();
		}
		cal.set(Calendar.DATE, 1);
		shownMonth = cal.get(Calendar.MONTH);

		jLabelYearMonth.setText(getYearMonthText(cal));

		String strWrk = "";
		cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);
		for (int i = 0; i < 42; i++) {
			dateButtonArray[i].setEnabled(true);
			strWrk = kbCalendarList.get(jTabbedPaneCenter.getSelectedIndex());
			strWrk = strWrk + ";" +yyyyMMdd.format(cal.getTime());
			if (offDateMap.containsKey(strWrk)) {
				dateButtonArray[i].setToolTipText(offDateMap.get(strWrk));
				if (cal.get(Calendar.MONTH) == shownMonth) {
					dateButtonArray[i].setForeground(Color.red);
				} else {
					dateButtonArray[i].setEnabled(false);
					dateButtonArray[i].setForeground(lightRedGray);
				}
			} else {
				dateButtonArray[i].setToolTipText("");
				if (cal.get(Calendar.MONTH) == shownMonth) {
					dateButtonArray[i].setForeground(Color.black);
				} else {
					dateButtonArray[i].setEnabled(false);
					dateButtonArray[i].setForeground(Color.lightGray);
				}
			}

			if(cal.getTime().equals(focusedDate)) {
				indexOfFocusedDate = i;
			}
			dateButtonArray[i].setDate(cal.getTime());
			if(cal.get(Calendar.YEAR) == calendarForToday.get(Calendar.YEAR) &&
					cal.get(Calendar.MONTH) == calendarForToday.get(Calendar.MONTH) &&
					cal.get(Calendar.DATE) == calendarForToday.get(Calendar.DATE)) {
				dateButtonArray[i].setText("<html><u>" + dateButtonArray[i].getText());
			}

			cal.add(Calendar.DATE,+1);
		}

		dateButtonArray[indexOfFocusedDate].requestFocus();
    	jLabelDateComment.setText(dateButtonArray[indexOfFocusedDate].getToolTipText());
	}
	
	String getYearMonthText(Calendar cal) {

		//en00 06/17/10
		//en01 Thur,06/17/01
		//en10 Jun17,2010
		//en11 Thur,Jun17,2001
		//
		//jp00 10/06/17
		//jp01 10/06/17(木)
		//jp10 2010/06/17
		//jp11 2010/06/17(木)
		//jp20 2010年6月17日
		//jp21 2010年6月17日(木)
		//jp30 H22/06/17
		//jp31 H22/06/17(水)
		//jp40 H22年06月17日
		//jp41 H22年06月17日(水)
		//jp50 平成22年06月17日
		//jp51 平成22年06月17日(水)

		String result = "";
		String dateFormat = session_.getDateFormat();
		String language = session_.getDateFormat().substring(0, 2);
		SimpleDateFormat formatter;

		if (language.equals("en")) {
			formatter = new SimpleDateFormat("MMMMM, yyyy", new Locale("en", "US", "US"));
			result = formatter.format(cal.getTime());
		}

		if (dateFormat.equals("jp00")
				|| dateFormat.equals("jp01")
				|| dateFormat.equals("jp10")
				|| dateFormat.equals("jp11")
				|| dateFormat.equals("jp20")
				|| dateFormat.equals("jp21")) {
			formatter = new SimpleDateFormat("yyyy年 M月");
			result = formatter.format(cal.getTime());
		}
		if (dateFormat.equals("jp30")
				|| dateFormat.equals("jp31")
				|| dateFormat.equals("jp40")
				|| dateFormat.equals("jp41")
				|| dateFormat.equals("jp50")
				|| dateFormat.equals("jp51")) {
			formatter = new SimpleDateFormat("GGGGy年 M月", new Locale("ja", "JP", "JP"));
			result = formatter.format(cal.getTime());
		}

		return result;
	}
    
	void jTabbedPaneCenter_stateChanged(ChangeEvent e) {
		if (this.isVisible()) {
			Component com = getFocusOwner();
			for (int i = 0; i < 42; i++) {
				if (com.equals(dateButtonArray[i])) {
					DateButton button = (DateButton)dateButtonArray[i];
					setupDates(button.getDate(), 0);
					break;
				}
			}
		}
	}
   
	void jButton_actionPerformed(ActionEvent e) {
		DateButton button = (DateButton)e.getSource();
		selectedDate = button.getDate();
		this.setVisible(false);
	}
	
	void jButton_keyPressed(KeyEvent e) {

		////////////
		// Ctrl+9 //
		////////////
		if (e.getKeyCode() == KeyEvent.VK_9 && e.isControlDown()){
			selectedDate = maxValueDate;
			this.setVisible(false);
		}

		////////////
		// Ctrl+P //
		////////////
		if (e.getKeyCode() == KeyEvent.VK_P && e.isControlDown()){
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
			boolean ready = false;
			String answer = "2000-01-01";
	    	if (this.date != null) {
				answer = sdf1.format(this.date);
			}
			String message = XFUtility.RESOURCE.getString("CalendarMessage1");
			while (!ready) {
				answer = JOptionPane.showInputDialog(null, message, answer);
				if (answer == null || answer.equals("")) {
					ready = true;
				} else {
					try {
						selectedDate = sdf1.parse(answer);
						ready = true;
						this.setVisible(false);
					} catch (ParseException e1) {
						message = XFUtility.RESOURCE.getString("CalendarMessage2");
					}
				}
			}
		}

		////////////
		// Arrows //
		////////////
		if (e.getKeyCode() == KeyEvent.VK_RIGHT
				|| e.getKeyCode() == KeyEvent.VK_LEFT
				|| e.getKeyCode() == KeyEvent.VK_UP
				|| e.getKeyCode() == KeyEvent.VK_DOWN){
			Component com = getFocusOwner();
			for (int i = 0; i < 42; i++) {
				if (com.equals(dateButtonArray[i])) {
					if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0){
						if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
							setFocusOnNextButton(i, "RIGHT");
						}
						if (e.getKeyCode() == KeyEvent.VK_LEFT) {
							setFocusOnNextButton(i, "LEFT");
						}
						if (e.getKeyCode() == KeyEvent.VK_UP) {
							setFocusOnNextButton(i, "UP");
						}
						if (e.getKeyCode() == KeyEvent.VK_DOWN) {
							setFocusOnNextButton(i, "DOWN");
						}
					} else {
						if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
							gotoNextMonth(dateButtonArray[i].getDate());
						}
						if (e.getKeyCode() == KeyEvent.VK_LEFT) {
							gotoPreviousMonth(dateButtonArray[i].getDate());
						}
					}
					break;
				}
			}
		}
	}
	
	void gotoPreviousMonth(Date currentDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		cal.set(Calendar.DATE, 1);
		cal.add(Calendar.DATE, -1);
		cal.set(Calendar.DATE, 1);
		setupDates(cal.getTime(), 0);
	}
	
	void gotoNextMonth(Date currentDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentDate);
		int month = cal.get(Calendar.MONTH);
		while (cal.get(Calendar.MONTH) == month) {
			cal.add(Calendar.DATE, 1);
		}
		setupDates(cal.getTime(), 0);
	}
	
	void setFocusOnNextButton(int index, String direction) {
		int i= index;
		if (direction.equals("RIGHT")) {
	    	setupDates(dateButtonArray[i].getDate(), 1);
		}
		if (direction.equals("LEFT")) {
	    	setupDates(dateButtonArray[i].getDate(), -1);
		}
		if (direction.equals("DOWN")) {
	    	setupDates(dateButtonArray[i].getDate(), 7);
		}
		if (direction.equals("UP")) {
	    	setupDates(dateButtonArray[i].getDate(), -7);
		}
	}
	
	class DateButton extends JButton {
		private static final long serialVersionUID = 1L;
		private Date date;
		public DateButton() {
			super();
//			this.setFont(new java.awt.Font(session_.systemFont, java.awt.Font.BOLD, 12));
			this.setFont(new java.awt.Font(Font.SANS_SERIF, java.awt.Font.BOLD, 12));
			this.addFocusListener(new DateButton_FocusAdapter());
		}
		public void setDate(Date date) {
			this.date = date;
			this.setText(day.format(date));
		}
		public Date getDate() {
			return date;
		}
	}

	class DateButton_FocusAdapter implements java.awt.event.FocusListener {
		  public void focusGained(FocusEvent e) {
				Component com = getFocusOwner();
				for (int i = 0; i < 42; i++) {
					if (com.equals(dateButtonArray[i])) {
						jLabelDateComment.setText(dateButtonArray[i].getToolTipText());
					}
				}
		  }
		  public void focusLost(FocusEvent e) {
		  }
	}
}

class XFImageField_jButton_actionAdapter implements java.awt.event.ActionListener {
	XFImageField adaptee;
	  XFImageField_jButton_actionAdapter(XFImageField adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void actionPerformed(ActionEvent e) {
	    adaptee.jButton_actionPerformed(e);
	  }
}

class XFImageField_jButton_keyAdapter extends java.awt.event.KeyAdapter {
	  XFImageField adaptee;
	  XFImageField_jButton_keyAdapter(XFImageField adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void keyPressed(KeyEvent e) {
	    adaptee.jButton_keyPressed(e);
	  }
}

class XFCalendar_changeAdapter implements ChangeListener {
	  XFCalendar adaptee;
	  XFCalendar_changeAdapter(XFCalendar adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void stateChanged(ChangeEvent e) {
	    adaptee.jTabbedPaneCenter_stateChanged(e);
	  }
}

class XFCalendar_actionAdapter implements java.awt.event.ActionListener {
	  XFCalendar adaptee;
	  XFCalendar_actionAdapter(XFCalendar adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void actionPerformed(ActionEvent e) {
	    adaptee.jButton_actionPerformed(e);
	  }
}

class XFCalendar_keyAdapter extends java.awt.event.KeyAdapter {
	  XFCalendar adaptee;
	  XFCalendar_keyAdapter(XFCalendar adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void keyPressed(KeyEvent e) {
	    adaptee.jButton_keyPressed(e);
	  }
}

//class XFYMonthBox_Year_keyAdapter extends java.awt.event.KeyAdapter {
//	  XFYMonthBox adaptee;
//	  XFYMonthBox_Year_keyAdapter(XFYMonthBox adaptee) {
//	    this.adaptee = adaptee;
//	  }
//	  public void keyPressed(KeyEvent e) {
//	    adaptee.jComboBoxYear_keyPressed(e);
//	  }
//}

class XFYMonthBox_Month_keyAdapter extends java.awt.event.KeyAdapter {
	  XFYMonthBox adaptee;
	  XFYMonthBox_Month_keyAdapter(XFYMonthBox adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void keyPressed(KeyEvent e) {
	    adaptee.jComboBoxMonth_keyPressed(e);
	  }
}

class XFFYearBox_keyAdapter extends java.awt.event.KeyAdapter {
	  XFFYearBox adaptee;
	  XFFYearBox_keyAdapter(XFFYearBox adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void keyPressed(KeyEvent e) {
	    adaptee.jComboBoxYear_keyPressed(e);
	  }
}

class XFMSeqBox_keyAdapter extends java.awt.event.KeyAdapter {
	  XFMSeqBox adaptee;
	  XFMSeqBox_keyAdapter(XFMSeqBox adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void keyPressed(KeyEvent e) {
	    adaptee.jComboBoxMSeq_keyPressed(e);
	  }
}