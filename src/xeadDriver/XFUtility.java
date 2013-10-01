package xeadDriver;

/*
 * Copyright (c) 2013 WATANABE kozo <qyf05466@nifty.com>,
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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.script.ScriptException;
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
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
	public static final int FIELD_UNIT_HEIGHT = 24;
	public static final int FIELD_VERTICAL_MARGIN = 5;
	public static final int ROW_UNIT_HEIGHT = 24;
	public static final int SEQUENCE_WIDTH = 30;
	public static final String DEFAULT_UPDATE_COUNTER = "UPDCOUNTER";
	public static final Color ERROR_COLOR = new Color(238,187,203);
	public static final Color ACTIVE_COLOR = SystemColor.white;
	public static final Color INACTIVE_COLOR = SystemColor.control;
	public static final Color ODD_ROW_COLOR = new Color(240, 240, 255);
	public static final Color SELECTED_ACTIVE_COLOR = new Color(49,106,197);
	public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss.SSS");
	public static final ImageIcon ICON_CHECK_0A = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck0A.PNG")));
	public static final ImageIcon ICON_CHECK_1A = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck1A.PNG")));
	public static final ImageIcon ICON_CHECK_0D = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck0D.PNG")));
	public static final ImageIcon ICON_CHECK_1D = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck1D.PNG")));
	public static final ImageIcon ICON_CHECK_0R = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck0R.PNG")));
	public static final ImageIcon ICON_CHECK_1R = new ImageIcon(Toolkit.getDefaultToolkit().createImage(xeadDriver.XFUtility.class.getResource("iCheck1R.PNG")));
	public static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("#,##0");
	public static final DecimalFormat FLOAT_FORMAT0 = new DecimalFormat("#,##0");
	public static final DecimalFormat FLOAT_FORMAT1 = new DecimalFormat("#,##0.0");
	public static final DecimalFormat FLOAT_FORMAT2 = new DecimalFormat("#,##0.00");
	public static final DecimalFormat FLOAT_FORMAT3 = new DecimalFormat("#,##0.000");
	public static final DecimalFormat FLOAT_FORMAT4 = new DecimalFormat("#,##0.0000");
	public static final DecimalFormat FLOAT_FORMAT5 = new DecimalFormat("#,##0.00000");
	public static final DecimalFormat FLOAT_FORMAT6 = new DecimalFormat("#,##0.000000");
	
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
					//numberString = Double.toString(doubleValue);
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
		//
		int pos = value.indexOf(".");
		if (pos >= 0) {
			wrkValue = value.substring(0, pos);
		} else {
			wrkValue = value;
		}
		wrkValue = wrkValue.replace("-", "");
		//
		if (dataTypeOptionList.contains("NO_EDIT")) {
			StringBuffer bf = new StringBuffer();
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
				StringBuffer bf = new StringBuffer();
				if (value.startsWith("-")) {
					bf.append("-");
				}
				bf.append(XFUtility.INTEGER_FORMAT.format(Long.parseLong(wrkValue)));
				if (value.endsWith("-")) {
					bf.append("-");
				}
				returnValue = bf.toString();
			}
		}
		//
		return returnValue;
	}

	static String getFormattedFloatValue(String value, int decimalSize) {
		String returnValue = "";
		double doubleWrk;
		//	
		try {
			String wrkStr = value.toString();
			wrkStr = wrkStr.replace("-", "");
			doubleWrk = Double.parseDouble(wrkStr);
		} catch (NumberFormatException e) {
			doubleWrk = 0;
		}
		//
		StringBuffer bf = new StringBuffer();
		if (value.startsWith("-")) {
			bf.append("-");
		}
		//
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
		//
		if (value.endsWith("-")) {
			bf.append("-");
		}
		//
		returnValue = bf.toString();
		//
		return returnValue;
	}

	
	static Object getNullValueOfBasicType(String basicType){
		Object value = null;
		//
		if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
			value = 0;
		}
		//
		if (basicType.equals("STRING") || basicType.equals("DATETIME") || basicType.equals("TIME") || basicType.equals("DATE")) {
			value = "";
		}
		//
		if (basicType.equals("DATE")) {
			value = null;
		}
		//
		return value;
	}
	
	static boolean isNullValue(String basicType, Object value){
		boolean isNull = false;
		//
		if (basicType.equals("INTEGER") || basicType.equals("FLOAT")) {
			String strValue = value.toString();
			if (strValue == null || strValue.equals("") || strValue.equals("0") || strValue.equals("0.0")) {
				isNull = true;
			}
		}
		//
		if (basicType.equals("STRING") || basicType.equals("DATETIME") || basicType.equals("TIME")) {
			String strValue = value.toString();
			if (strValue == null || strValue.equals("")) {
				isNull = true;
			}
		}
		//
		if (basicType.equals("DATE")) {
			String strValue = value.toString();
			if (strValue == null || strValue.equals("")) {
				isNull = true;
			}
		}
		//
		return isNull;
	}

	static ArrayList<String> getDSNameListInScriptText(String scriptText, NodeList tableList) {
		ArrayList<String> fieldList = new ArrayList<String>();
		int pos, posWrk, wrkInt, posWrk2;
		String[] sectionDigit = {"(", ")", "{", "}", "+", "-", "/", "*", "=", "<", ">", ";", "|", "&", "\n", "\t", ",", " ", "!"};
		String[] fieldProperty = {"value", "oldValue", "color", "editable", "error", "valueChanged"};
		boolean isFirstDigitOfField;
		String variantExpression, wrkStr1, wrkStr2, dataSource;
		org.w3c.dom.Element element;
		//
		for (int i = 0; i < fieldProperty.length; i++) {
			//
			pos = 0;
			//
			while (pos < scriptText.length()) {
				//
				posWrk = scriptText.indexOf("." + fieldProperty[i], pos);
				if (posWrk == -1) {
					pos = scriptText.length();
				}
				//
				if (posWrk != -1) {
					//
					wrkInt = posWrk - 1;
					//
					while (wrkInt > -1) {
						//
						wrkInt--;
						isFirstDigitOfField = false;
						//
						if (wrkInt > -1) {
							wrkStr1 = scriptText.substring(wrkInt, wrkInt+1);
							for (int j = 0; j < sectionDigit.length; j++) {
								if (wrkStr1.equals(sectionDigit[j])) {
									isFirstDigitOfField = true;
									break;
								}
							}
						}
						//
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
									dataSource = variantExpression.replace(wrkStr2+"_", wrkStr2+".");
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
		//
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

	static ImageIcon createSmallIcon(String fileName, int iconHeight) {
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
				File imageFile = new File(fileName);
				if (imageFile.exists()) {
					image = ImageIO.read(imageFile);
				}
			}
			/////////////////////////////////////////////////////
			// Setup small icon image with buffered image data //
			/////////////////////////////////////////////////////
			if (image != null) {
				float rate = (float)iconHeight / image.getHeight();
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
	
	static String removeCommentsFromScriptText(String scriptText) {
		StringBuffer buf = new StringBuffer();
		int posCommentStart;
		int posCommentEnd = 0;
		//
		int pos = 0;
		while (pos < scriptText.length()) {
			//
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
			//
			pos = posCommentEnd;
		}
		//
		return buf.toString();
	}
	

	static Object parseObjectAccordingToType(Object value, String basicType) {
		Object returnValue = "";
		//
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
		//
		return returnValue;
	}
	
	static ArrayList<String> getOptionList(String options) {
		ArrayList<String> typeOptionList = new ArrayList<String>();
		StringTokenizer workTokenizer = new StringTokenizer(options, ",");
		while (workTokenizer.hasMoreTokens()) {
			typeOptionList.add(workTokenizer.nextToken());
		}
		return typeOptionList;
	}
	
	static String getOptionValueWithKeyword(String options, String keyword) {
		String value = "";
		int pos1, pos2;
		//
		if (!keyword.equals("")) {
			int lengthOfKeyword = keyword.length() + 1;
			ArrayList<String> typeOptionList = getOptionList(options);
			for (int i = 0; i < typeOptionList.size(); i++) {
				pos1 = typeOptionList.get(i).indexOf(keyword + "(");
				if (pos1 > -1) {
					pos2 = typeOptionList.get(i).length() - 1;
					value = typeOptionList.get(i).substring(pos1 + lengthOfKeyword, pos2);
					break;
				}
			}
		}
	    //
		return value;
	}
	
//	static String getLongestSegment(String caption) {
//		String value = "";
//		ArrayList<String> stringList = new ArrayList<String>();
//		String wrkStr = caption.toUpperCase();
//		wrkStr = wrkStr.replace("<HTML>", "");
//		wrkStr = wrkStr.replace("</HTML>", "");
//		StringTokenizer workTokenizer = new StringTokenizer(wrkStr, "<BR>");
//		while (workTokenizer.hasMoreTokens()) {
//			stringList.add(workTokenizer.nextToken());
//		}
//		for (int i = 0; i < stringList.size(); i++) {
//			if (stringList.get(i).length() > value.length()) {
//				value = stringList.get(i);
//			}
//		}
//		return value;
//	}
	
	static String getDefaultValueOfFilterField(String keywordValue, Session session){
		String defaultValue = null;
		//
		StringTokenizer workTokenizer = new StringTokenizer(keywordValue, ":" );
		if (workTokenizer.countTokens() == 1) {
			defaultValue = workTokenizer.nextToken().trim();
		}
		if (workTokenizer.countTokens() == 2) {
			//
			String keyword = workTokenizer.nextToken();
			String value = workTokenizer.nextToken().trim();
			//
			if (keyword.equals("VALUE")) {
				defaultValue = value;
			}
			//
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
			//
			// Note that if attribute not found, null will be returned //
			if (keyword.equals("SESSION_ATTRIBUTE")) {
				defaultValue = session.getAttribute(value);
			}
		}
		//
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
	
//	static String getFixedWhereValue(String keywordValue, Session session){
//		int pos1, pos2;
//		String wrkStr, id, value;
//		StringTokenizer workTokenizer;
//		String fixedWhereValue = keywordValue.replace("\"", "'");;
//		if (keywordValue.contains("SESSION_ATTRIBUTE:")) {
//			for (int i = 0; i < keywordValue.length(); i++) {
//				pos1 = keywordValue.indexOf("SESSION_ATTRIBUTE:", i);
//				if (pos1 == -1) {
//					i = keywordValue.length();
//				} else {
//					pos2 = keywordValue.indexOf(" ", pos1);
//					if (pos2 == -1) {
//						pos2 = keywordValue.length();
//						i = keywordValue.length();
//					}
//					wrkStr = keywordValue.substring(pos1, pos2);
//					wrkStr = wrkStr.replace("'", "");
//					wrkStr = wrkStr.replace("\"", "");
//					workTokenizer = new StringTokenizer(wrkStr, ":" );
//					if (workTokenizer.countTokens() == 2) {
//						workTokenizer.nextToken();
//						id = workTokenizer.nextToken().trim();
//						value = session.getAttribute(id);
//						if (value == null) {
//							JOptionPane.showMessageDialog(null, "Session attribute not found with id of '" + id + "'.");
//						} else {
//							fixedWhereValue = fixedWhereValue.replace(wrkStr, value);
//						}
//					}
//					i = pos1 + 1;
//				}
//			}
//		}
//		return fixedWhereValue;
//	}
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
	
	static String getBasicTypeOf(String dataType){
		String basicType = "STRING";
		if (dataType.equals("INTEGER")
				|| dataType.equals("SMALLINT")
				|| dataType.equals("BIGINT")
					) {
			basicType = "INTEGER";
		}
		if (dataType.equals("DOUBLE")
				|| dataType.equals("DECIMAL")
				|| dataType.equals("FLOAT")
				|| dataType.equals("DOUBLE PRECISION")
				|| dataType.equals("NUMERIC")
				|| dataType.equals("REAL")
					) {
			basicType = "FLOAT";
		}
		if (dataType.equals("CHAR")
				|| dataType.equals("TEXT")
				|| dataType.equals("VARCHAR")
				|| dataType.equals("LONG VARCHAR")
					) {
			basicType = "STRING";
		}
		if (dataType.equals("BINARY")
				|| dataType.equals("VARBINARY")
					) {
			basicType = "BINARY";
		}
		if (dataType.equals("CLOB")) {
			basicType = "CLOB";
		}
		if (dataType.equals("BLOB")) {
			basicType = "BLOB";
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
		return basicType;
	}
	
	static boolean isLiteralRequiredBasicType(String basicType) {
		if (basicType.equals("STRING")
				|| basicType.equals("DATE")
				|| basicType.equals("TIME")
				|| basicType.equals("DATETIME")) {
			return true;
		} else {
			return false;
		}
	}
	
	static Object getTableOperationValue(String basicType, Object value){
		Object returnValue = null;
		//
		if (basicType.equals("INTEGER")) {
			if (value == null || value.toString().equals("")) {
				value = 0;
			}
			returnValue = Long.parseLong(value.toString());
		}
		if (basicType.equals("FLOAT")) {
			if (value == null || value.toString().equals("")) {
				value = 0.0;
			}
			returnValue = Double.parseDouble(value.toString());
		}
		if (basicType.equals("STRING")) {
			String strValue = value.toString().replaceAll("'","''");
			returnValue = "'" + strValue + "'";
		}
		if (basicType.equals("DATE")) {
			if (value == null) {
				returnValue = "NULL";
			} else {
				String strDate = value.toString();
				if (strDate == null || strDate.equals("")) {
					returnValue = "NULL";
				} else {
					returnValue = "'" + strDate + "'";
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
					returnValue = "'" + timeDate + "'";
				}
			}
		}
		//
		return returnValue;
	}

	static String getTableIDOfTableAlias(String tableAlias, NodeList referList1, NodeList referList2) {
		String tableID = tableAlias;
		org.w3c.dom.Element workElement;
		//
	    for (int j = 0; j < referList1.getLength(); j++) {
			workElement = (org.w3c.dom.Element)referList1.item(j);
			if (workElement.getAttribute("TableAlias").equals(tableAlias) ||
					(workElement.getAttribute("ToTable").equals(tableAlias) && workElement.getAttribute("TableAlias").equals(""))) {
				tableID = workElement.getAttribute("ToTable");
				break;
			}
	    }
	    //
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
	    //
		return tableID;
	}

	static Object getValueAccordingToBasicType(String basicType, Object value){
		String wrkStr;
		Object valueReturn = null;
		//
		if (isLiteralRequiredBasicType(basicType)) {
			if (value == null) {
				if (basicType.equals("DATE")) {
					valueReturn = "";
				} else {
					valueReturn = null;
				}
			} else {
				valueReturn = value.toString().trim();
			}
		} else {
			if (basicType.equals("INTEGER")) {
				if (value == null || value.equals("")) {
					valueReturn = "";
				} else {
					wrkStr = value.toString();
//					int pos = wrkStr.indexOf(".");
//					if (pos >= 0) {
//						wrkStr = wrkStr.substring(0, pos);
//					}
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
		//
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
		if (basicType.equals("DATE")) {
			if (valueObject == null) {
				editableField.setValue(null);
			} else {
				editableField.setValue(valueObject);
			}
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
		//
		if (basicType.equals("INTEGER")) {
			if (valueObject == null || valueObject.equals("")) {
				editableField.setOldValue("");
			} else {
				wrkStr = valueObject.toString();
//				int pos = wrkStr.indexOf(".");
//				if (pos >= 0) {
//					wrkStr = wrkStr.substring(0, pos);
//				}
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
		if (basicType.equals("DATE")) {
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
	
	static short getFloatFormat(HSSFWorkbook workbook, int decimalSize) {
		HSSFDataFormat format = workbook.createDataFormat();
		StringBuffer buf = new StringBuffer();
		//
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
		//
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
		//
		String tableID = tableElement.getAttribute("ID");
		String activeWhere = tableElement.getAttribute("ActiveWhere");
		//
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
		//
		workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
		rangeKeyFieldValid = workTokenizer.nextToken();
		//
		buf.append("select ");
		buf.append(rangeKeyFieldValid);
		buf.append(" from ");
		buf.append(tableID);
		//
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
		//
		if (!activeWhere.equals("")) {
			buf.append(" and ");
			buf.append(activeWhere);
		}
		//
		buf.append(" order by ");
		buf.append(rangeKeyFieldValid);
		//
		XFTableOperator operatorExpire = new XFTableOperator(session, logBuf, buf.toString());
		if (operatorExpire.next()) {
			object = operatorExpire.getValueOf(rangeKeyFieldValid);
		}
		//
		return object;
	}

	/**
	 * Method to process String value with EOL mark
	 * @param originalString :original string value to be processed
	 * @param stringToBeInserted :value to be replaced into "#EOL#"(usually it's "\n")
	 * @return String :string value of processed string
	 */
	static String substringLinesWithTokenOfEOL(String originalString, String stringToBeInserted) {
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

	static String getEditValueOfLong(long value, String editCode, int size) {
		DecimalFormat integerFormat = new DecimalFormat("#,##0;#,##0-");
		//
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
		//
		return integerFormat.format(value);
	}

	static String getEditValueOfDouble(double value, String editCode, int decimal) {
		DecimalFormat floatFormat = new DecimalFormat("#,##0.0;#,##0.0-");
		//
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
		//
		return floatFormat.format(value);
	}
	
	static String getUserExpressionOfUtilDate(java.util.Date date, String dateFormat, boolean isWithTime) {
		//
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
		//
		Calendar cal = Calendar.getInstance();
		if (date != null) { 
			cal.setTime(date);
		}
		//
		StringBuffer buf = new StringBuffer();
		SimpleDateFormat formatter;
		//
		if (dateFormat.equals("")) {
			if (isWithTime) {
				formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", new Locale("en", "US", "US"));
			} else {
				formatter = new SimpleDateFormat("yyyy-MM-dd", new Locale("en", "US", "US"));
			}
			buf.append(formatter.format(cal.getTime()));
		}
		//
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
		//
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
		//
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
		//
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
		//
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
		//
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
		//
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
		//
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
		//
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
		//
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
		//
		return image;
	}

//	static void setupImageCellForDetailColumn(HSSFWorkbook workBook, HSSFSheet workSheet, int rowNumber, int columnIndex, String fileName, HSSFPatriarch patriarch) {
//		HSSFClientAnchor anchor = null;
//		int imageType = -1;
//		File imageFile = new File(fileName);
//		if (imageFile.exists()) {
//			boolean isValidFileType = false;
//			if (fileName.contains(".png") || fileName.contains(".PNG")) {
//				imageType = HSSFWorkbook.PICTURE_TYPE_PNG;
//				isValidFileType = true;
//			}
//			if (fileName.contains(".jpg") || fileName.contains(".JPG") || fileName.contains(".jpeg") || fileName.contains(".JPEG")) {
//				imageType = HSSFWorkbook.PICTURE_TYPE_JPEG;
//				isValidFileType = true;
//			}
//			if (isValidFileType) {
//				FileInputStream fis = null;
//				ByteArrayOutputStream bos = null;
//				try {
//					// read in the image file and copy the image bytes into the ByteArrayOutputStream//
//					fis = new FileInputStream(imageFile);
//					bos = new ByteArrayOutputStream();
//					int c;
//					while ((c = fis.read()) != -1) {
//						bos.write(c);
//					}
//					// add the image bytes to the workbook //
//					int pictureIndex = workBook.addPicture(bos.toByteArray(), imageType);
//					anchor = new HSSFClientAnchor(0,0,0,0,
//							(short)columnIndex, rowNumber, (short)(columnIndex+1), rowNumber+1);
//					anchor.setAnchorType(0);
//					anchor.setDx1(20);
//					anchor.setDy1(20);
//					anchor.setDx2(0);
//					anchor.setDy2(0);
//					patriarch.createPicture(anchor, pictureIndex);
//				} catch(Exception e) {
//					e.printStackTrace();
//				} finally {
//					try {
//						if (fis != null) {
//							fis.close();
//						}
//						if (bos != null) {
//							bos.close();
//						}
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//	}
	static void setupImageCellForDetailColumn(HSSFWorkbook workBook, HSSFSheet workSheet, int rowNumber, int columnIndex, String fileName, HSSFPatriarch patriarch) {
		HSSFClientAnchor anchor = null;
		int imageTypeInt = -1;
		String imageTypeString = "";
		ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
		boolean isValidFileType = false;
		//////////////////////
		// Check image type //
		//////////////////////
		if (fileName.contains(".png") || fileName.contains(".PNG")) {
			imageTypeInt = HSSFWorkbook.PICTURE_TYPE_PNG;
			imageTypeString = "png";
			isValidFileType = true;
		}
		if (fileName.contains(".jpg") || fileName.contains(".JPG") || fileName.contains(".jpeg") || fileName.contains(".JPEG")) {
			imageTypeInt = HSSFWorkbook.PICTURE_TYPE_JPEG;
			imageTypeString = "jpg";
			isValidFileType = true;
		}
		if (isValidFileType) {
			//////////////////////////////////////////////
			// Setup image byte data with its file name //
			//////////////////////////////////////////////
			if (fileName.startsWith("http://")) {
				fileName = fileName.replace("\\", "/");
				try {
					URL url = new URL(fileName);
					BufferedImage image = ImageIO.read(url);
					ImageIO.write(image, imageTypeString, imageBytes);
					imageBytes.flush();
				} catch (Exception e) { //required as URL can be invalid //
				} finally {
					try {
						imageBytes.close();
					} catch (IOException e) {
					}
				}
			} else {
				File imageFile = new File(fileName);
				if (imageFile.exists()) {
					FileInputStream fis = null;
					try {
						fis = new FileInputStream(imageFile);
						int c;
						while ((c = fis.read()) != -1) {
							imageBytes.write(c);
						}
					} catch (Exception e) {
					} finally {
						try {
							if (fis != null) {
								fis.close();
							}
							imageBytes.close();
						} catch (IOException e) {
						}
					}
				}
			}
			/////////////////////////////////////////
			// Add image byte data to the workbook //
			/////////////////////////////////////////
			int pictureIndex = workBook.addPicture(imageBytes.toByteArray(), imageTypeInt);
			anchor = new HSSFClientAnchor(0,0,0,0, (short)columnIndex, rowNumber, (short)(columnIndex+1), rowNumber+1);
			anchor.setAnchorType(0);
			anchor.setDx1(20);
			anchor.setDy1(20);
			anchor.setDx2(0);
			anchor.setDy2(0);
			patriarch.createPicture(anchor, pictureIndex);
		}
	}
	
	static void setupImageCellForField(HSSFWorkbook workBook, HSSFSheet workSheet, int columnIndex, int rowNumber, int cellWidth, int cellHeight, String fileName, HSSFPatriarch patriarch) throws Exception{
		//HSSFClientAnchor anchor = null;
		int imageTypeInt = -1;
		String imageTypeString = "";
		boolean isValidFileType = false;
		ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
		//////////////////////
		// Check image type //
		//////////////////////
		if (fileName.contains(".png") || fileName.contains(".PNG")) {
			imageTypeInt = HSSFWorkbook.PICTURE_TYPE_PNG;
			imageTypeString = "png";
			isValidFileType = true;
		}
		if (fileName.contains(".jpg") || fileName.contains(".JPG") || fileName.contains(".jpeg") || fileName.contains(".JPEG")) {
			imageTypeInt = HSSFWorkbook.PICTURE_TYPE_JPEG;
			imageTypeString = "jpg";
			isValidFileType = true;
		}
		if (isValidFileType) {
			//////////////////////////////////////////////
			// Setup image byte data with its file name //
			//////////////////////////////////////////////
			if (fileName.startsWith("http://")) {
				try {
					fileName = fileName.replace("\\", "/");
					URL url;
					url = new URL(fileName);
					BufferedImage image = ImageIO.read(url);
					ImageIO.write(image, imageTypeString, imageBytes);
					imageBytes.flush();
				} catch (Exception e) { //required as URL can be invalid //
				} finally {
					imageBytes.close();
				}
			} else {
				File imageFile = new File(fileName);
				if (imageFile.exists()) {
					FileInputStream fis = null;
					try {
						fis = new FileInputStream(imageFile);
						int c;
						while ((c = fis.read()) != -1) {
							imageBytes.write(c);
						}
					} finally {
						if (fis != null) {
							fis.close();
						}
						imageBytes.close();
					}
				}
			}
			/////////////////////////////////////////
			// Add image byte data to the workbook //
			/////////////////////////////////////////
			int pictureIndex = workBook.addPicture(imageBytes.toByteArray(), imageTypeInt);
			HSSFClientAnchor anchor = new HSSFClientAnchor(0,0,0,0, (short)columnIndex, rowNumber, (short)(columnIndex + cellWidth), rowNumber + cellHeight);
			anchor.setAnchorType(0);
			anchor.setDx1(30);
			anchor.setDy1(30);
			anchor.setDx2(-30);
			anchor.setDy2(-250);
			patriarch.createPicture(anchor, pictureIndex);
		}
	}
	
	static com.lowagie.text.Rectangle getPageSize(String size, String direction) {
		//
		com.lowagie.text.Rectangle rec = PageSize.A4; //Default//
		//
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
			//
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
		//
		return rec;
	}
	
	static void drawLineOrRect(String keyword, PdfContentByte cb) {
		float x1, y1, x2, y2;
		//
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
		//
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
		//
		return chunk;
	}
	
//	static int getLengthOfEdittedNumericValue(int dataSize, int decimalSize, boolean acceptMinus) {
//		int length = dataSize;
//		//
//		if (decimalSize > 0) {
//			length = length + 1;
//		}
//		if (acceptMinus) {
//			length = length + 1;
//		}
//		//
//		int intSize = dataSize - decimalSize;
//		while (intSize > 3) {
//			length = length + 1;
//			intSize = intSize - 3;
//		}
//		//
//		return length;
//	}
	static int getLengthOfEdittedNumericValue(int dataSize, int decimalSize, ArrayList<String> dataTypeOptionList) {
		int length = dataSize;
		//
		if (decimalSize > 0) {
			length = length + 1;
		}
		if (dataTypeOptionList.contains("ACCEPT_MINUS")) {
			length = length + 1;
		}
		//
		if (!dataTypeOptionList.contains("NO_EDIT")) {
			int intSize = dataSize - decimalSize;
			while (intSize > 3) {
				length = length + 1;
				intSize = intSize - 3;
			}
		}
		//
		return length;
	}
	
	static int getWidthOfDateValue(String dateFormat, int fontSize) {
		//
		int width = 133;
		JTextField textField = new JTextField();
		FontMetrics metrics = textField.getFontMetrics(new java.awt.Font("Dialog", 0, fontSize));
		//
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
		//
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
		//
		return width + 10;
	}
	
	static String getUserExpressionOfYearMonth(String yearMonth, String dateFormat) {
		//
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
		//
		String result = "";
		//
		if (yearMonth.length() == 6) {
			int year = Integer.parseInt(yearMonth.substring(0, 4));
			int month = Integer.parseInt(yearMonth.substring(4, 6)) - 1;
			SimpleDateFormat formatter;
			Calendar cal = Calendar.getInstance();
			cal.set(year, month, 1);
			//
			if (dateFormat.equals("en00")
					|| dateFormat.equals("en01")
					|| dateFormat.equals("en10")
					|| dateFormat.equals("en11")) {
				formatter = new SimpleDateFormat("MMM,yyyy", new Locale("en", "US", "US"));
				result = formatter.format(cal.getTime());
			}
			//
			if (dateFormat.equals("jp00")
					|| dateFormat.equals("jp01")
					|| dateFormat.equals("jp10")
					|| dateFormat.equals("jp11")
					|| dateFormat.equals("jp20")
					|| dateFormat.equals("jp21")) {
				//formatter = new SimpleDateFormat("yyyy年MM月", new Locale("ja", "JP", "JP"));
				formatter = new SimpleDateFormat("yyyy年MM月", new Locale("ja", "US", "US"));
				result = formatter.format(cal.getTime());
			}
			//
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
		//
		if (yearMonth.length() == 4) {
			int year = Integer.parseInt(yearMonth.substring(0, 4));
			int month = 0;
			SimpleDateFormat formatter;
			Calendar cal = Calendar.getInstance();
			cal.set(year, month, 1);
			//
			if (dateFormat.equals("en00")
					|| dateFormat.equals("en01")
					|| dateFormat.equals("en10")
					|| dateFormat.equals("en11")) {
				formatter = new SimpleDateFormat("fiscal yyyy", new Locale("en", "US", "US"));
				result = formatter.format(cal.getTime());
			}
			//
			if (dateFormat.equals("jp00")
					|| dateFormat.equals("jp01")
					|| dateFormat.equals("jp10")
					|| dateFormat.equals("jp11")
					|| dateFormat.equals("jp20")
					|| dateFormat.equals("jp21")) {
				//formatter = new SimpleDateFormat("yyyy年度", new Locale("ja", "JP", "JP"));
				formatter = new SimpleDateFormat("yyyy年度", new Locale("ja", "US", "US"));
				result = formatter.format(cal.getTime());
			}
			//
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
		//
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
		//
		startMonth = session.getSystemVariantInteger("FIRST_MONTH");
		//
		if (mSeq >= 1 && mSeq <= 12) {
			if (language.equals("en")) {
				result = monthArrayEn[mSeq + startMonth - 2];
			}
			if (language.equals("jp")) {
				result = monthArrayJp[mSeq + startMonth - 2];
			}
		}
		//
		return result;
	}

	static String getDayOfWeek(Calendar cal, String dateFormat) {
		String result = "";
		String language = dateFormat.substring(0, 2);
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		//
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
		//
		return result;
	}
	
	static java.util.Date convertDateFromStringToUtil(String strDate) {
		java.util.Date utilDate = null;
		int pos1, pos2;
		int year = 0;
		int month = -1;
		int date = 0;
		Calendar cal = Calendar.getInstance();
		//
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
			//
			if (year > 0 && month > -1 && date > 0) {
				cal.set(year, month, date);
				utilDate = cal.getTime();
			}
		}
		//
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
		//
		if (utilDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(utilDate);
			strDate = yyyyMMdd.format(cal.getTime());
		}
		//
		return  strDate;
	}
	
	static Color convertStringToColor(String color) {
		Color colorConverted = Color.black; //Default//
		//
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
			colorConverted = Color.green;
		}
		if (color.equals("orange")) {
			colorConverted = Color.orange;
		}
		//
		return colorConverted;
	}
	//
	static String convertColorToString(Color color) {
		String colorConverted = "black"; //Default//
		//
		if (color.equals(Color.black)) {
			colorConverted = "black";
		}
		if (color.equals(Color.red)) {
			colorConverted = "red";
		}
		if (color.equals(Color.blue)) {
			colorConverted = "blue";
		}
		if (color.equals(Color.green)) {
			colorConverted = "green";
		}
		if (color.equals(Color.orange)) {
			colorConverted = "orange";
		}
		//
		return colorConverted;
	}
	
	static SortableDomElementListModel getSortedListModel(NodeList list, String attName) {
		SortableDomElementListModel sortableDomElementListModel = new SortableDomElementListModel(attName);
		for (int i = 0; i < list.getLength(); i++) {
			sortableDomElementListModel.addElement((Object)list.item(i));
		}
		sortableDomElementListModel.sortElements();
		return sortableDomElementListModel;
	}

	static void setCaptionToButton(JButton button, org.w3c.dom.Element element, String buttonText) {
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
	}
	
	static void appendLog(String text, StringBuffer logBuf) {
		if (logBuf != null) {
			StringBuffer buf = new StringBuffer();
			buf.append("> ");
			buf.append(text);
			buf.append("  (");
			buf.append(TIME_FORMATTER.format(Calendar.getInstance().getTime()));
			buf.append(")\n");
			logBuf.append(buf.toString());
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
		//
		if (code.equals("00")) {
			message = RESOURCE.getString("ReturnMessage00");
		}
		//
		if (code.equals("01")) {
			message = RESOURCE.getString("ReturnMessage01");
		}
		//
		if (code.equals("10")) {
			message = RESOURCE.getString("ReturnMessage10");
		}
		if (code.equals("11")) {
			message = RESOURCE.getString("ReturnMessage11");
		}
		//
		if (code.equals("20")) {
			message = RESOURCE.getString("ReturnMessage20");
		}
		if (code.equals("21")) {
			message = RESOURCE.getString("ReturnMessage21");
		}
		//
		if (code.equals("30")) {
			message = RESOURCE.getString("ReturnMessage30");
		}
		//
		if (code.equals("99")) {
			message = RESOURCE.getString("ReturnMessage99");
		}
		//
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
	private static final long serialVersionUID = 1L;
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private ArrayList<Object> keyValueList = new ArrayList<Object>();
	public void addValue(String fieldID, Object value) {
		keyFieldList.add(fieldID);
		keyValueList.add(value);
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

class XFSessionForScript {
	private Session session_;
	
    public XFSessionForScript(Session session) {
        session_ = session;
    }

    public String getAttribute(String id) {
		return session_.getAttribute(id);
	}
    public void setAttribute(String id, String value) {
		session_.setAttribute(id, value);
	}

    public String getImageFileFolder() {
		return session_.getImageFileFolder();
	}
	public String getDatabaseUser() {
		return session_.getDatabaseUser();
	}
	public String getSessionID() {
		return session_.getSessionID();
	}
	public String getUserEmployeeNo() {
		return session_.getUserEmployeeNo();
	}
	public String getUserEmailAddress() {
		return session_.getUserEmailAddress();
	}
	public String getUserID() {
		return session_.getUserID();
	}
	public String getUserName() {
		return session_.getUserName();
	}

	public void compressTable(String tableID) throws Exception {
		session_.compressTable(tableID);
	}

	public float getAnnualExchangeRate(String currency, int fYear, String type) {
		return session_.getAnnualExchangeRate(currency, fYear, type);
	}
	public float getMonthlyExchangeRate(String currency, int fYear, int mSeq, String type) {
		return session_.getMonthlyExchangeRate(currency, fYear, mSeq, type);
	}

	public String getNextNumber(String id) {
		return session_.getNextNumber(id);
	}
	public void setNextNumber(String id, int nextNumber) {
		session_.setNextNumber(id, nextNumber);
	}
	public float getSystemVariantFloat(String id) {
		return session_.getSystemVariantFloat(id);
	}
	public int getSystemVariantInteger(String id) {
		return session_.getSystemVariantInteger(id);
	}
	public String getSystemVariantString(String id) {
		return session_.getSystemVariantString(id);
	}
	public void setSystemVariant(String id, String value) {
		session_.setSystemVariant(id, value);
	}
	public int getTaxAmount(String date, int amount) {
		return session_.getTaxAmount(date, amount);
	}

	public int getDaysBetweenDates(String dateFrom, String dateThru, int countType) {
		return session_.getDaysBetweenDates(dateFrom, dateThru, countType);
	}
	public int getDaysBetweenDates(String dateFrom, String dateThru, int countType, String kbCalendar) {
		return session_.getDaysBetweenDates(dateFrom, dateThru, countType, kbCalendar);
	}
	public Object getMinutesBetweenTimes(String timeFrom, String timeThru) {
		return session_.getMinutesBetweenTimes(timeFrom, timeThru);
	}
	public String getOffsetDate(String date, int days, int countType) {
		return session_.getOffsetDate(date, days, countType);
	}
	public String getOffsetDate(String date, int days, int countType, String kbCalendar) {
		return session_.getOffsetDate(date, days, countType, kbCalendar);
	}
	public String getOffsetDateTime(String dateFrom, String timeFrom, int minutes, int countType) {
		return session_.getOffsetDateTime(dateFrom, timeFrom, minutes, countType);
	}
	public String getOffsetYearMonth(String yearMonthFrom, int offsetMonths) {
		return session_.getOffsetYearMonth(yearMonthFrom, offsetMonths);
	}
	public String getTimeStamp() {
		return session_.getTimeStamp();
	}
	public String getThisMonth() {
		return session_.getThisMonth();
	}
	public String getToday() {
		return session_.getToday();
	}
	public boolean isOffDate(String date) {
		return session_.isOffDate(date);
	}
	public boolean isOffDate(String date, String kbCalendar) {
		return session_.isOffDate(date, kbCalendar);
	}
	public boolean isValidDate(String date) {
		return session_.isValidDate(date);
	}
	public boolean isValidDateFormat(String date, String separator) {
		return session_.isValidDateFormat(date, separator);
	}
	public boolean isValidTime(String time, String format) {
		return session_.isValidTime(time, format);
	}
	public int getMSeqOfDate(String date) {
		return session_.getMSeqOfDate(date);
	}
	public int getFYearOfDate(String date) {
		return session_.getFYearOfDate(date);
	}
	public String getYearMonthOfFYearMSeq(String fYearMSeq) {
		return session_.getYearMonthOfFYearMSeq(fYearMSeq);
	}
	public String getErrorOfAccountDate(String date) {
		return session_.getErrorOfAccountDate(date);
	}

	public void executeProgram(String programName) {
		session_.executeProgram(programName);
	}
	public void browseFile(String fileName) {
		session_.browseFile(fileName);
	}
	public void editFile(String fileName) {
		session_.editFile(fileName);
	}
	public void sendMail(String addressFrom, String addressTo, String addressCc,
			String subject, String message,
			String fileName, String attachedName, String charset) {
		session_.sendMail(addressFrom, addressTo, addressCc,
				subject, message, fileName, attachedName, charset);
	}

	public XFTableOperator createTableOperator(String oparation, String tableID) {
		//return new XFTableOperator(session_, null, oparation, tableID);
		XFTableOperator operator = null;
		try {
			operator = new XFTableOperator(session_, null, oparation, tableID);
		} catch (Exception e) {
		}
		return operator;
	}
	public XFTableOperator createTableOperator(String sqlText) {
		return new XFTableOperator(session_, null, sqlText);
	}
}

interface XFExecutable {
	public boolean isAvailable();
	public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parameterList);
}

interface XFScriptable {
	public void cancelWithMessage(String message);
	public void cancelWithException(Exception e);
	public void cancelWithScriptException(ScriptException e, String scriptName);
	public void callFunction(String functionID);
	public void commit();
	public void rollback();
	public String getFunctionID();
	public HashMap<String, Object> getParmMap();
	public HashMap<String, Object> getReturnMap();
	public void setProcessLog(String value);
	public StringBuffer getProcessLog();
	public void startProgress(int maxValue);
	public void incrementProgress();
	public XFTableOperator createTableOperator(String oparation, String tableID);
	public XFTableOperator createTableOperator(String sqlText);
	public Object getFieldObjectByID(String tableID, String fieldID);
	public boolean isAvailable();
}

interface XFTableColumnEditor {
	public Object getInternalValue();
	public Object getExternalValue();
	public void setBorder(Border border);
	public void setBackground(Color color);
	public void setHorizontalAlignment(int alignment);
	public void setBounds(Rectangle rec);
	public void setColorOfError();
	public void setColorOfNormal(int row);
	public void setEditable(boolean isEditable);
	public void setFocusable(boolean isFocusable);
	public void setValue(Object value);
	public void requestFocus();
	public boolean hasFocus();
	public boolean isEditable();
}

interface XFEditableField {
	public void setEditable(boolean editable);
	public void setFocusable(boolean focusable);
	public void setToolTipText(String text);
	public void requestFocus();
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
	private String oldValue = "";

	public XFImageField(String fieldOptions, int size, String imageFileFolder){
		//
		super();
		//
		String wrkStr;
		//
		fieldOptions_ = fieldOptions;
		size_ = size;
		imageFileFolder_ = imageFileFolder;
		//
		jTextField.setEditable(false);
		Border workBorder = jTextField.getBorder();
		normalModeColor = jTextField.getBackground();
		jTextField.setBorder(BorderFactory.createLineBorder(normalModeColor));
		jTextField.setBackground(Color.white);
		jTextField.setEditable(true);
		jTextField.setFont(new java.awt.Font("Dialog", 0, 14));
		jTextField.setDocument(new LimitDocument());
		//
		jButton.setFont(new java.awt.Font("Dialog", 0, 14));
		jButton.setPreferredSize(new Dimension(80, XFUtility.FIELD_UNIT_HEIGHT));
		jButton.setText(XFUtility.RESOURCE.getString("Refresh"));
		jButton.addActionListener(new XFImageField_jButton_actionAdapter(this));
		jButton.addKeyListener(new XFImageField_jButton_keyAdapter(this));
		jPanelBottom.setPreferredSize(new Dimension(200, XFUtility.FIELD_UNIT_HEIGHT));
	    jScrollPane.setBorder(null);
	    jPanelBottom.setLayout(new BorderLayout());
	    jPanelBottom.add(jTextField, BorderLayout.CENTER);
	    jPanelBottom.add(jButton, BorderLayout.EAST);
		//
	    this.setBorder(workBorder);
		this.setLayout(new BorderLayout());
		this.add(jPanelBottom, BorderLayout.SOUTH);
		this.add(jScrollPane, BorderLayout.CENTER);
		//
		rows_ = DEFAULT_ROWS;
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "ROWS");
		if (!wrkStr.equals("")) {
			rows_ = Integer.parseInt(wrkStr);
		}
		int fieldHeight = rows_ * XFUtility.FIELD_UNIT_HEIGHT - FIELD_VERTICAL_MARGIN - 3;
		//
		int fieldWidth = DEFAULT_WIDTH;
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}
		//
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
		//return imageFileFolder_ + "\\" + jTextField.getText();
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
		String fullName = imageFileFolder_ + imageFileName;
		if (imageFileName.equals("")) {
			jLabelImage = new JLabel();
		} else {
			if (fullName.startsWith("http://")) {
				try{
					fullName = fullName.replace("\\", "/");
					URL url = new URL(fullName);
					imageIcon = new ImageIcon(url);
				}catch(Exception e){
					jLabelImage.setText(XFUtility.RESOURCE.getString("ImageFileNotFound1") + fullName + XFUtility.RESOURCE.getString("ImageFileNotFound2"));
				}
			} else {
				File imageFile = new File(fullName);
				if (imageFile.exists()) {
					imageIcon = new ImageIcon(fullName);
				} else {
					jLabelImage.setText(XFUtility.RESOURCE.getString("ImageFileNotFound1") + fullName + XFUtility.RESOURCE.getString("ImageFileNotFound2"));
				}
			}
			jLabelImage = new JLabel("", imageIcon, JLabel.CENTER);
		}
        jLabelImage.setOpaque(true);
		jLabelImage.setText("");
		jLabelImage.setToolTipText(imageFileName);
        if (!jTextField.getText().equals("") && imageIcon == null) {
			jLabelImage.setText(XFUtility.RESOURCE.getString("ImageFileNotFound1") + fullName + XFUtility.RESOURCE.getString("ImageFileNotFound2"));
        }
        //
		int wrkWidth = this.getWidth() - 50;
		int wrkHeight = this.getHeight() - 50;
		if (imageIcon != null) {
			if (imageIcon.getIconWidth() > wrkWidth) {
				wrkWidth = imageIcon.getIconWidth() + 10;
			}
			if (imageIcon.getIconHeight() > wrkHeight) {
				wrkHeight = imageIcon.getIconHeight() + 10;
			}
		}
        jLabelImage.setPreferredSize(new Dimension(wrkWidth, wrkHeight));
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
	//private XFCalendar xFCalendar;

	public XFDateField(Session session){
		super();
		session_ = session;
		dateTextField.setFont(new java.awt.Font("Monospaced", 0, 14));
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
		//
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
		//
		this.setSize(new Dimension(XFUtility.getWidthOfDateValue(session.getDateFormat(), 14) + 26, XFUtility.FIELD_UNIT_HEIGHT));
		this.setLayout(new BorderLayout());
		this.add(dateTextField, BorderLayout.CENTER);
		this.add(jButton, BorderLayout.EAST);
		//
		//xFCalendar = new XFCalendar(session_, this);
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
			dateTextField.setFont(new java.awt.Font("SansSerif", 0, 12));
			dateTextField.setBackground(SystemColor.text);
			dateTextField.setFocusable(true);
		} else {
			this.remove(jButton);
			dateTextField.setFont(new java.awt.Font("Monospaced", 0, 14));
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
		//
		if (obj != null) {
			if (obj.getClass().getName().equals("java.sql.Date")) {
				this.date = XFUtility.convertDateFromSqlToUtil((java.sql.Date)obj);
				dateTextField.setText(XFUtility.getUserExpressionOfUtilDate(this.date, session_.getDateFormat(), false));
			}
			if (obj.getClass().getName().equals("java.util.Date")) {
				this.date = (java.util.Date)obj;
				dateTextField.setText(XFUtility.getUserExpressionOfUtilDate(this.date, session_.getDateFormat(), false));
			}
			if (obj.getClass().getName().equals("java.lang.String")) {
				if (!obj.equals("")) {
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
			for (int fontSize = 13; fontSize >= 9; fontSize--) {
				workWidth = XFUtility.getWidthOfDateValue(session_.getDateFormat(), fontSize) + 26;
				if (workWidth <= width || fontSize == 9) {
					dateTextField.setFont(new java.awt.Font("Dialog", 0, fontSize));
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
		//
		if (date == null) {
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

	public void setBackground(Color color) {
		if (dateTextField != null) {
			dateTextField.setBackground(color);
		}
	}
	
	public java.util.Date getDateOnCalendar(java.util.Date date) {
		//return xFCalendar.getDateOnCalendar(date);
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

class XFTextField extends JTextField implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private String basicType_ = "";
	private int digits_ = 5;
	private int decimal_ = 0;
	private int rows_ = 1;
	private ArrayList<String> dataTypeOptionList;
	private String fieldOptions_;
	private String autoNumberKey = "";
	private String oldValue = "";
	//
	public XFTextField(String basicType, int digits, int decimal, String dataTypeOptions, String fieldOptions) {
		//
		super();
		//
		basicType_ = basicType;
		digits_ = digits;
		decimal_ = decimal;
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		fieldOptions_ = fieldOptions;
		//
		if (basicType_.equals("INTEGER")) {
			this.setHorizontalAlignment(SwingConstants.RIGHT);
			this.setText(this.getFormattedNumber("0"));
		} else {
			if (basicType_.equals("FLOAT")) {
				this.setHorizontalAlignment(SwingConstants.RIGHT);
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
				this.setText(this.getFormattedNumber(wrkStr));
			} else {
				this.setHorizontalAlignment(SwingConstants.LEFT);
			}
		}
		this.addFocusListener(new ComponentFocusListener());
		this.setFont(new java.awt.Font("Monospaced", 0, 14));
		this.setDocument(new LimitedDocument(this));
		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					if (basicType_.equals("STRING") && !autoNumberKey.equals("")) {
						fillZero();
					}
				}
			} 
		});
		//
		int fieldWidth, fieldHeight;
		if (dataTypeOptionList.contains("KANJI") || dataTypeOptionList.contains("ZIPADRS")) {
			fieldWidth = digits_ * 14 + 10;
		} else {
			if (basicType_.equals("INTEGER") || basicType_.equals("FLOAT")) {
				fieldWidth = XFUtility.getLengthOfEdittedNumericValue(digits_, decimal_, dataTypeOptionList) * 7 + 21;
			} else {
				if (basicType_.equals("DATETIME")) {
					fieldWidth = 24 * 7;
				} else {
					fieldWidth = digits_ * 7 + 10;
				}
			}
		}
		if (fieldWidth > 800) {
			fieldWidth = 800;
		}
		fieldHeight = XFUtility.FIELD_UNIT_HEIGHT;
		//
		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "AUTO_NUMBER");
		if (!wrkStr.equals("")) {
			autoNumberKey = wrkStr;
		}
		//
		this.setSize(new Dimension(fieldWidth, fieldHeight));
	}

	public boolean isComponentFocusable() {
		return this.isFocusable();
	}
	
	public void setWidth(int width) {
		this.setSize(width, this.getHeight());
	}
	
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		super.setFocusable(editable);
	}

	public String getStringNumber(String text) {
		String numberString = XFUtility.getStringNumber(text);
		//
		if (numberString.equals("")) {
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
		//
		return numberString;
	}

	public Object getInternalValue() {
		String text = "";
		if (basicType_.equals("INTEGER")
			|| basicType_.equals("FLOAT")
			|| dataTypeOptionList.contains("DIAL")
			|| dataTypeOptionList.contains("ZIPNO")) {
			//
			if (this.getText().equals("*AUTO")) {
				text = this.getText();
			} else {
				text = this.getStringNumber(this.getText());
			}
		} else {
			text = this.getText();
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
					this.setText(text);
				} else {
					this.setText(getFormattedNumber(text));
				}
			}
			if (basicType_.equals("FLOAT")) {
				this.setText(getFormattedNumber(text));
			}
			if (basicType_.equals("DATE")) {
				this.setText(text);
			}
			if (basicType_.equals("TIME")) {
				this.setText(text);
			}
			if (basicType_.equals("DATETIME")) {
				text = text.replace("-", "/");
				this.setText(text);
			}
			if (basicType_.equals("STRING")) {
				text = text.trim();
				this.setText(text);
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
		}
		return value;
	}
	
	public int getRows() {
		return rows_;
	}
	
	private void fillZero() {
		int stringDigitFrom = -1;
		int numberDigitFrom = 0;
		if (getText().length() < digits_ && !getText().equals("")) {
			for( int i = 0; i < getText().length() ; i++) {
				try {
					Integer.parseInt(getText().substring(i, i+1));
					numberDigitFrom = i;
					break;
				} catch(NumberFormatException e) {
					stringDigitFrom = i;
				}
			}
			StringBuffer sb = new StringBuffer();
			if (stringDigitFrom > -1) {
				sb.append(getText().substring(stringDigitFrom, numberDigitFrom));
			}
			int zeroLen = digits_ - getText().length();
			for( int i = 0; i < zeroLen ; i++) {
				sb.append("0");
			}
			sb.append(getText().substring(numberDigitFrom, getText().length()));
			setText(sb.toString());
		}
	}

	class ComponentFocusListener implements FocusListener{
		public void focusLost(FocusEvent event){
			if (basicType_.equals("INTEGER")) {
				if (!getText().equals("*AUTO")) {
					setText(getFormattedNumber(getText()));
				}
			}
			if (basicType_.equals("FLOAT")) {
				setText(getFormattedNumber(getText()));
			}
			if (getInputContext() != null) {
				getInputContext().setCompositionEnabled(false);
			}
			if (basicType_.equals("STRING") && !autoNumberKey.equals("")) {
				fillZero();
			}
		}
		public void focusGained(FocusEvent event){
			Character.Subset[] subsets  = new Character.Subset[] {java.awt.im.InputSubset.LATIN_DIGITS};
			String lang = Locale.getDefault().getLanguage();
			//
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
						//getInputContext().setCharacterSubsets(subsets);
						//getInputContext().setCompositionEnabled(false);
						InputContext ic = getInputContext();
						if (ic != null) {
							ic.setCharacterSubsets(subsets);
							ic.setCompositionEnabled(false);
						}
					}
				}
			} else {
				//getInputContext().setCharacterSubsets(subsets);
				//getInputContext().setCompositionEnabled(false);
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
		//
		LimitedDocument(XFTextField adaptee) {
		  this.adaptee = adaptee;
		}
		//
		public void insertString(int offset, String str, AttributeSet attr) {
			try {
				int integerSizeOfField = adaptee.digits_ - adaptee.decimal_;
				//
				if (adaptee.decimal_ > 0 && str.length() == 1) {
//					if (adaptee.isEditable()
//							&& !basicType_.equals("DATE")
//							&& !basicType_.equals("TIME")
//							&& !dataTypeOptionList.contains("ACCEPT_MINUS")
//							&& str.contains("-")) {
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
					//if (adaptee.isEditable() && dataTypeOptionList.contains("NO_MINUS") && str.contains("-")) {
//					if (adaptee.isEditable()
//							&& !basicType_.equals("DATE")
//							&& !basicType_.equals("TIME")
//							&& !dataTypeOptionList.contains("ACCEPT_MINUS")
//							&& str.contains("-")) {
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
	private String oldValue = "";
	private int digits_ = 5;

	public XFTextArea(){
		this(5, "", "");
	}

	public XFTextArea(int digits, String dataTypeOptions, String fieldOptions){
		//
		super();
		//
		digits_ = digits;
		String wrkStr;
		//
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		fieldOptions_ = fieldOptions;
		//
		InputMap inputMap  = jTextArea.getInputMap(JTextArea.WHEN_FOCUSED);
		ActionMap actionMap = jTextArea.getActionMap();
		//
		KeyStroke tab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
		KeyStroke shiftTab = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);
		KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
		KeyStroke altEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_MASK);
		//
		inputMap.put(altEnter, inputMap.get(enter));
		inputMap.put(enter, "Exit");
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
		//
		jTextArea.addFocusListener(new ComponentFocusListener());
		jTextArea.setFont(new java.awt.Font("Dialog", 0, 14));
		jTextArea.setLineWrap(true);
		jTextArea.setWrapStyleWord(true);
		jTextArea.setDocument(new LimitedDocument(this));
		this.getViewport().add(jTextArea, null);
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "ROWS");
		if (!wrkStr.equals("")) {
			rows_ = Integer.parseInt(wrkStr);
		}
		//
		int fieldWidth = 800;
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions_, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		}
		//
		int fieldHeight = rows_ * XFUtility.FIELD_UNIT_HEIGHT + (rows_-1) * XFUtility.FIELD_VERTICAL_MARGIN;
		if (rows_ == 1) {
			fieldHeight = XFUtility.FIELD_UNIT_HEIGHT + XFUtility.FIELD_VERTICAL_MARGIN;
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
		//
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
		//
		LimitedDocument(XFTextArea adaptee) {
		  this.adaptee = adaptee;
		}
		//
		public void insertString(int offset, String str, AttributeSet a) {
			try {
				if (offset < adaptee.digits_ && super.getLength() < adaptee.digits_) {
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
	private Vector<String> valueList = new Vector<String>();
	private int rows_ = 1;
    private Session session_;
    private String oldValue = "";

	public XFInputAssistField(String tableID, String fieldID, Session session){
		super();
		tableID_ = tableID;
		fieldID_ = fieldID;
		session_ = session;

		org.w3c.dom.Element workElement = session_.getFieldElement(tableID, fieldID);
		if (workElement == null) {
			JOptionPane.showMessageDialog(this, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		int dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (dataSize > 50) {
			dataSize = 50;
		}
		String wrkStr;
		int fieldWidth = dataSize * 7;
		FontMetrics metrics = this.getFontMetrics(new java.awt.Font("Dialog", 0, 14));
		String sql = "select distinct " + fieldID_ + " from " + tableID_ + " order by " + fieldID;
		XFTableOperator operator = new XFTableOperator(session_, null, sql, true);
		try {
			while (operator.next()) {
				wrkStr = operator.getValueOf(fieldID).toString().trim();
				valueList.add(wrkStr);
				if (metrics.stringWidth(wrkStr) > fieldWidth) {
					fieldWidth = metrics.stringWidth(wrkStr);
				}
			}
		} catch (Exception e) {
		}
		if (fieldWidth > 200) {
			fieldWidth = 200;
		}
		DefaultComboBoxModel model = new DefaultComboBoxModel(valueList);
		this.setModel(model);
		this.setSize(new Dimension(fieldWidth, XFUtility.FIELD_UNIT_HEIGHT));
		this.setFont(new java.awt.Font("Dialog", 0, 14));
		this.setEditable(true);
		this.setSelectedIndex(-1);
		JTextField field = (JTextField)this.getEditor().getEditorComponent();
		field.setText("");
		field.addKeyListener(new ComboKeyHandler(this));
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
		private final JComboBox comboBox;
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
	private JComboBox jComboBoxYear = new JComboBox();
	private ArrayList<String> listYear = new ArrayList<String>();
	private JComboBox jComboBoxMonth = new JComboBox();
	private ArrayList<String> listMonth = new ArrayList<String>();
	private boolean isEditable = false;
	private String oldValue = "";
    private Session session_;
    private String dateFormat = "";
    private String language = "";
	
	public XFYMonthBox(Session session){
		//
		super();
		//
		session_ = session;
		//
		dateFormat = session_.getDateFormat();
		language = session_.getDateFormat().substring(0, 2);
		//
		jTextField.setFont(new java.awt.Font("Dialog", 0, 14));
		jTextField.setBounds(new Rectangle(0, 0, 85, XFUtility.FIELD_UNIT_HEIGHT));
		jTextField.setEditable(false);
		jTextField.setFocusable(false);
		//
		GregorianCalendar calendar = new GregorianCalendar();
		int currentYear = calendar.get(Calendar.YEAR);
		int minimumYear = currentYear - 30;
		int maximumYear = currentYear + 10;
		jComboBoxYear.setFont(new java.awt.Font("Dialog", 0, 14));
		jComboBoxYear.addKeyListener(new XFYMonthBox_Year_keyAdapter(this));
		//
		SimpleDateFormat gengoFormatter = new SimpleDateFormat("Gyy", new Locale("ja", "JP", "JP"));
		Calendar cal = Calendar.getInstance();
		listYear.add("");
		jComboBoxYear.addItem("");
		for (int i = minimumYear; i <= maximumYear; i++) {
			listYear.add(String.valueOf(i));
			if (language.equals("en")
					|| dateFormat.equals("jp00")
					|| dateFormat.equals("jp01")
					|| dateFormat.equals("jp10")
					|| dateFormat.equals("jp11")
					|| dateFormat.equals("jp20")
					|| dateFormat.equals("jp21")) {
				jComboBoxYear.addItem(String.valueOf(i));
			} else {
				cal.set(i, 0, 1);
				jComboBoxYear.addItem(gengoFormatter.format(cal.getTime()));
			}
		}
		listYear.add("9999");
		if (language.equals("en")
				|| dateFormat.equals("jp00")
				|| dateFormat.equals("jp01")
				|| dateFormat.equals("jp10")
				|| dateFormat.equals("jp11")
				|| dateFormat.equals("jp20")
				|| dateFormat.equals("jp21")) {
			jComboBoxYear.addItem("9999");
		} else {
			jComboBoxYear.addItem("H99");
		}
		//
		jComboBoxMonth.setFont(new java.awt.Font("Dialog", 0, 14));
		jComboBoxMonth.addKeyListener(new XFYMonthBox_Month_keyAdapter(this));
		//
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
			jComboBoxMonth.setBounds(new Rectangle(0, 0, 55, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxYear.setBounds(new Rectangle(56, 0, 60, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(116, XFUtility.FIELD_UNIT_HEIGHT));
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
			jComboBoxYear.setBounds(new Rectangle(0, 0, 60, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMonth.setBounds(new Rectangle(61, 0, 45, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(106, XFUtility.FIELD_UNIT_HEIGHT));
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
		//
		this.setLayout(null);
	}
	
	public void addActionListener(ActionListener listener) {
		jComboBoxYear.addActionListener(listener);
		jComboBoxMonth.addActionListener(listener);
	}

	public void setEditable(boolean editable) {
		this.removeAll();
		if (editable) {
			this.add(jComboBoxYear);
			this.add(jComboBoxMonth);
		} else {
			this.add(jTextField);
			this.setFocusable(false);
		}
		isEditable = editable;
	}

	public Object getInternalValue() {
		String year = listYear.get(jComboBoxYear.getSelectedIndex());
		String month = listMonth.get(jComboBoxMonth.getSelectedIndex());
		return year + month;
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
		String value = (String)obj;
		if (value != null) {
			value = value.trim();
		}
		if (value == null || value.equals("")) {
			jComboBoxYear.setSelectedIndex(0);
			jComboBoxMonth.setSelectedIndex(0);
			jTextField.setText("");
		} else {
			if (value.length() == 6) {
				String yearValue = value.substring(0, 4);
				String monthValue = value.substring(4, 6);
				int index = listYear.indexOf(yearValue);
				if (index == -1) {
					jComboBoxYear.setSelectedIndex(0);
				} else {
					jComboBoxYear.setSelectedIndex(index);
				}
				index = listMonth.indexOf(monthValue);
				if (index == -1) {
					jComboBoxMonth.setSelectedIndex(0);
				} else {
					jComboBoxMonth.setSelectedIndex(index);
				}
				if (language.equals("en")) {
					jTextField.setText(jComboBoxMonth.getItemAt(jComboBoxMonth.getSelectedIndex()).toString() + ", " + jComboBoxYear.getItemAt(jComboBoxYear.getSelectedIndex()).toString());
				}
				if (language.equals("jp")) {
					jTextField.setText(jComboBoxYear.getItemAt(jComboBoxYear.getSelectedIndex()).toString() + "年" + jComboBoxMonth.getItemAt(jComboBoxMonth.getSelectedIndex()).toString() + "月");
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

	public void setBackground(Color color) {
		if (jComboBoxYear != null && jComboBoxMonth != null) {
			jComboBoxYear.setBackground(color);
			jComboBoxMonth.setBackground(color);
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
}

class XFFYearBox extends JPanel implements XFEditableField {
	private static final long serialVersionUID = 1L;
	private int rows_ = 1;
	private JTextField jTextField = new JTextField();
	private JComboBox jComboBoxYear = new JComboBox();
	private ArrayList<String> listYear = new ArrayList<String>();
	private boolean isEditable = false;
	private String oldValue = "";
    private Session session_;
    private String dateFormat = "";
    private String language = "";
	
	public XFFYearBox(Session session){
		//
		super();
		//
		session_ = session;
		//
		dateFormat = session_.getDateFormat();
		language = session_.getDateFormat().substring(0, 2);
		//
		jTextField.setFont(new java.awt.Font("Dialog", 0, 14));
		jTextField.setEditable(false);
		jTextField.setFocusable(false);
		jTextField.setBounds(new Rectangle(0, 0, 80, XFUtility.FIELD_UNIT_HEIGHT));
		//
		GregorianCalendar calendar = new GregorianCalendar();
		int currentYear = calendar.get(Calendar.YEAR);
		int minimumYear = currentYear - 30;
		int maximumYear = currentYear + 10;
		//
		jComboBoxYear.setFont(new java.awt.Font("Dialog", 0, 12));
		jComboBoxYear.addKeyListener(new XFFYearBox_keyAdapter(this));
		jComboBoxYear.setBounds(new Rectangle(0, 0, 80, XFUtility.FIELD_UNIT_HEIGHT));
		//
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
		//
		this.setLayout(null);
		this.setSize(new Dimension(80, XFUtility.FIELD_UNIT_HEIGHT));
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
	private JComboBox jComboBoxMSeq = new JComboBox();
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
		//
		super();
		//
		session_ = session;
		language = session_.getDateFormat().substring(0, 2);
		startMonth = session_.getSystemVariantInteger("FIRST_MONTH");
		//
		jTextField.setFont(new java.awt.Font("Dialog", 0, 14));
		jTextField.setEditable(false);
		jTextField.setFocusable(false);
		//
		jComboBoxMSeq.setFont(new java.awt.Font("Dialog", 0, 12));
		jComboBoxMSeq.addKeyListener(new XFMSeqBox_keyAdapter(this));
		//
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
		//
		if (language.equals("en")) {
			jComboBoxMSeq.setBounds(new Rectangle(0, 0, 50, XFUtility.FIELD_UNIT_HEIGHT));
			jTextField.setBounds(new Rectangle(0, 0, 50, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(50, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMSeq.addItem("");
			for (int i = startMonth -1; i < startMonth + 11; i++) {
				jComboBoxMSeq.addItem(monthArrayEn[i]);
			}
		}
		//
		if (language.equals("jp")) {
			jComboBoxMSeq.setBounds(new Rectangle(0, 0, 62, XFUtility.FIELD_UNIT_HEIGHT));
			jTextField.setBounds(new Rectangle(0, 0, 62, XFUtility.FIELD_UNIT_HEIGHT));
			this.setSize(new Dimension(62, XFUtility.FIELD_UNIT_HEIGHT));
			jComboBoxMSeq.addItem("");
			for (int i = startMonth -1; i < startMonth + 11; i++) {
				jComboBoxMSeq.addItem(monthArrayJp[i]);
			}
		}
		//
		this.setLayout(null);
	}
	
	public void addActionListener(ActionListener listener) {
		jComboBoxMSeq.addActionListener(listener);
	}

	public void setEditable(boolean editable) {
		this.removeAll();
		//
		if (editable) {
			this.add(jComboBoxMSeq);
		} else {
			this.add(jTextField);
			this.setFocusable(false);
		}
		//
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
		//
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
		//
	    this.setDisabledSelectedIcon(XFUtility.ICON_CHECK_1D);
	    this.setDisabledIcon(XFUtility.ICON_CHECK_0D);
	    this.setSelectedIcon(XFUtility.ICON_CHECK_1A);
	    this.setIcon(XFUtility.ICON_CHECK_0A);
	    this.setRolloverSelectedIcon(XFUtility.ICON_CHECK_1R);
	    this.setRolloverIcon(XFUtility.ICON_CHECK_0R);
		//
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

	public XFUrlField(){
		this(40, "");
	}

	public XFUrlField(int digits, String fieldOptions){
		super();

		jTextField.setFont(new java.awt.Font(XFUtility.RESOURCE.getString("URLFont"), 0, 14));
		jTextField.setDocument(new LimitedDocument(digits));
		jTextField.setEditable(false);
		jTextField.setFocusable(false);
		Font labelFont = new java.awt.Font(XFUtility.RESOURCE.getString("URLFont"), 0, 14);
		jLabel.setFont(labelFont);
		jLabel.setForeground(Color.blue);
		jLabel.setHorizontalAlignment(SwingConstants.LEFT);
		jLabel.addMouseListener(new jLabel_mouseAdapter(this));
		jLabel.setBorder(jTextField.getBorder());
		this.setLayout(new BorderLayout());
		this.add(jLabel, BorderLayout.CENTER);

		int fieldWidth,  fieldHeight;
		String wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "WIDTH");
		if (!wrkStr.equals("")) {
			fieldWidth = Integer.parseInt(wrkStr);
		} else {
			fieldWidth = digits * 7 + 10;
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
		jTextField.setFocusable(editable);
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
							fileName = fileName.replace("file:", "");
							adaptee.desktop.browse(new URI("file:" + fileName));
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
	private static final long serialVersionUID = 1L;
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

class ElementComparator implements java.util.Comparator<org.w3c.dom.Element> {
	private String attName_ = "";
	public ElementComparator (String attName) {
		super();
		attName_ = attName;
	}
    public int compare(org.w3c.dom.Element element1, org.w3c.dom.Element element2 ) {
      String value1, value2;
      value1 = element1.getAttribute(attName_);
      value2 = element2.getAttribute(attName_);
      int compareResult = value1.compareTo(value2);
      if (compareResult == 0) {
          compareResult = 1;
      }
      return(compareResult);
    }
}

class SortableDomElementListModel extends DefaultListModel {
	private static final long serialVersionUID = 1L;
	private String attName_ = "";
	public SortableDomElementListModel(String attName) {
		super();
		attName_ = attName;
	}
    public void sortElements() {
      TreeSet<org.w3c.dom.Element> treeSet = new TreeSet<org.w3c.dom.Element>(new ElementComparator(attName_));
      int elementCount = this.getSize();
      org.w3c.dom.Element domElement;
      for (int i = 0; i < elementCount; i++) {
        domElement = (org.w3c.dom.Element)this.getElementAt(i);
        treeSet.add(domElement);
      }
      this.removeAllElements();
      Iterator<org.w3c.dom.Element> it = treeSet.iterator();
      while( it.hasNext() ){
        domElement = (org.w3c.dom.Element)it.next();
        this.addElement(domElement);
      }
    }
}

class XFScript extends Object {
	private static final long serialVersionUID = 1L;
	private ArrayList<String> fieldList = new ArrayList<String>();
	private String tableID = "";
	private String scriptName = "";
	private String scriptText = "";
	private String eventP = "";
	private String eventR = "";

	public XFScript(String tableID, org.w3c.dom.Element scriptElement, NodeList tableNodeList) {
		super();
		this.tableID = tableID;
		eventP = scriptElement.getAttribute("EventP");
		eventR = scriptElement.getAttribute("EventR");
		scriptName = scriptElement.getAttribute("Name");
		scriptText = XFUtility.substringLinesWithTokenOfEOL(scriptElement.getAttribute("Text"), "\n");
		fieldList = XFUtility.getDSNameListInScriptText(XFUtility.removeCommentsFromScriptText(scriptText), tableNodeList);
	}
	
	public String getScriptText() {
		return scriptText;
	}
	
	public boolean isToBeRunAtEvent(String event1, String event2) {
		boolean result = false;
		//
		ArrayList<String> event1List = new ArrayList<String>();
		StringTokenizer workTokenizer = new StringTokenizer(event1, ",");
		while (workTokenizer.hasMoreTokens()) {
			event1List.add(workTokenizer.nextToken());
		}
		//
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
		//
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
    private String normalMessage;
    //private Component parent_;
	private Color lightRedGray = new Color(228, 192, 192);
	//private Color lightGreenGray = new Color(185, 216, 165);

    //public XFCalendar(Session session, Component parent) {
    public XFCalendar(Session session) {
		super();
		this.setModal(true);
		this.setTitle(XFUtility.RESOURCE.getString("Calendar"));
		this.session_ = session;
		//this.parent_ = parent;
		jPanelMain.setLayout(new BorderLayout());
		scrSize = Toolkit.getDefaultToolkit().getScreenSize();

		jPanelTop.setPreferredSize(new Dimension(346, 20));
		jPanelTop.setLayout(null);
		jLabelYearMonth.setFont(new java.awt.Font("Dialog", 0, 18));
		jLabelYearMonth.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelYearMonth.setBounds(0, 0, 344, 20);
		jPanelTop.add(jLabelYearMonth);

		jPanelCenter.setBackground(Color.white);
		jPanelCenter.setBorder(null);
		jPanelCenter.setLayout(null);
		jPanelCenter.setPreferredSize(new Dimension(332, 158));
		jLabelSun.setFont(new java.awt.Font("Dialog", 0, 12));
		jLabelSun.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelSun.setBounds(3, 2, 48, 13);
		jPanelCenter.add(jLabelSun);
		jLabelMon.setFont(new java.awt.Font("Dialog", 0, 12));
		jLabelMon.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelMon.setBounds(50, 2, 48, 13);
		jPanelCenter.add(jLabelMon);
		jLabelTue.setFont(new java.awt.Font("Dialog", 0, 12));
		jLabelTue.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelTue.setBounds(98, 2, 48, 13);
		jPanelCenter.add(jLabelTue);
		jLabelWed.setFont(new java.awt.Font("Dialog", 0, 12));
		jLabelWed.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelWed.setBounds(146, 2, 48, 13);
		jPanelCenter.add(jLabelWed);
		jLabelThu.setFont(new java.awt.Font("Dialog", 0, 12));
		jLabelThu.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelThu.setBounds(194, 2, 48, 13);
		jPanelCenter.add(jLabelThu);
		jLabelFri.setFont(new java.awt.Font("Dialog", 0, 12));
		jLabelFri.setHorizontalAlignment(SwingConstants.CENTER);
		jLabelFri.setBounds(242, 2, 48, 13);
		jPanelCenter.add(jLabelFri);
		jLabelSat.setFont(new java.awt.Font("Dialog", 0, 12));
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
		jTextAreaBottom.setFont(new java.awt.Font("Dialog", 0, 12));
		jTextAreaBottom.setEditable(false);
		jTextAreaBottom.setBackground(SystemColor.control);
		normalMessage = XFUtility.RESOURCE.getString("CalendarComment");
		jTextAreaBottom.setText(normalMessage);

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
		jTabbedPaneCenter.setTabPlacement(JTabbedPane.BOTTOM);
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
		//dlgSize = new Dimension(346,240);
		dlgSize = new Dimension(352,259);
		this.setPreferredSize(dlgSize);
		this.setResizable(false);
//		int posY;
//    	if (parent_ != null && parent_.isValid()) {
//    		Rectangle rec = parent_.getBounds();
//    		Point point = parent_.getLocationOnScreen();
//    		posX = point.x;
//    		posY = point.y + rec.height;
//    		if (posY + dlgSize.height > scrSize.height) {
//    			posY = point.y - dlgSize.height;
//    		}
//    		this.setLocation(posX, posY);
//    	} else {
//    		this.setLocation((scrSize.width - dlgSize.width) / 2, (scrSize.height - dlgSize.height) / 2);
//    	}
		this.pack();
    }

    public Date getDateOnCalendar(Date date, String kbCalendar, Point position) {
    	selectedDate = date;
    	this.date = date;

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
		if (dateButtonArray[indexOfFocusedDate].getToolTipText().equals("")) {
			jTextAreaBottom.setText(normalMessage);
		} else {
			jTextAreaBottom.setText(dateButtonArray[indexOfFocusedDate].getToolTipText());
		}
	}
	
	String getYearMonthText(Calendar cal) {

		////////////////////////////
		//en00 06/17/10           //
		//en01 Thur,06/17/01      //
		//en10 Jun17,2010         //
		//en11 Thur,Jun17,2001    //
		////////////////////////////
		//jp00 10/06/17           //
		//jp01 10/06/17(木)       //
		//jp10 2010/06/17         //
		//jp11 2010/06/17(木)     //
		//jp20 2010年6月17日                //
		//jp21 2010年6月17日(木)  //
		//jp30 H22/06/17          //
		//jp31 H22/06/17(水)      //
		//jp40 H22年06月17日                //
		//jp41 H22年06月17日(水)  //
		//jp50 平成22年06月17日          //
		//jp51 平成22年06月17日(水)//
		///////////////////////////

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
			this.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 12));
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
						if (dateButtonArray[i].getToolTipText().equals("")) {
							jTextAreaBottom.setText(normalMessage);
						} else {
							jTextAreaBottom.setText(dateButtonArray[i].getToolTipText());
						}
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

class XFYMonthBox_Year_keyAdapter extends java.awt.event.KeyAdapter {
	  XFYMonthBox adaptee;
	  XFYMonthBox_Year_keyAdapter(XFYMonthBox adaptee) {
	    this.adaptee = adaptee;
	  }
	  public void keyPressed(KeyEvent e) {
	    adaptee.jComboBoxYear_keyPressed(e);
	  }
}

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