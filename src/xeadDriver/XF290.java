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

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.w3c.dom.*;

import com.lowagie.text.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

public class XF290 extends Component implements XFExecutable, XFScriptable {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element functionElement_ = null;
	private HashMap<String, Object> parmMap_ = null;
	private HashMap<String, Object> returnMap_ = new HashMap<String, Object>();
	private XF290_PrimaryTable primaryTable_ = null;
	private Session session_ = null;
	private boolean instanceIsAvailable_ = true;
	private boolean isClosing = false;
	private int programSequence;
	private StringBuffer processLog = new StringBuffer();
	private ArrayList<XF290_Phrase> headerPhraseList = new ArrayList<XF290_Phrase>();
	private ArrayList<XF290_Phrase> paragraphList = new ArrayList<XF290_Phrase>();
	private ArrayList<XF290_ReferTable> referTableList = new ArrayList<XF290_ReferTable>();
	private ArrayList<XF290_Field> fieldList = new ArrayList<XF290_Field>();
	private ScriptEngine scriptEngine;
	private Bindings scriptBindings;
	private String scriptNameRunning = "";
	private ByteArrayOutputStream exceptionLog;
	private PrintStream exceptionStream;
	private String exceptionHeader = "";
	private HashMap<String, Object> variantMap = new HashMap<String, Object>();

	public XF290(Session session, int instanceArrayIndex) {
		super();
		session_ = session;
	}

	public boolean isAvailable() {
		return instanceIsAvailable_;
	}

	public HashMap<String, Object> execute(HashMap<String, Object> parmMap) {
		if (functionElement_ == null) {
			JOptionPane.showMessageDialog(null, "Calling function without specifications.");
			return parmMap;
		} else {
			return this.execute(null, parmMap);
		}
	}

	public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parmMap) {
		try {
			setCursor(new Cursor(Cursor.WAIT_CURSOR));

			////////////////////////
			// Process parameters //
			////////////////////////
			parmMap_ = parmMap;
			if (parmMap_ == null) {
				parmMap_ = new HashMap<String, Object>();
			}
			returnMap_.clear();
			returnMap_.putAll(parmMap_);
			returnMap_.put("RETURN_CODE", "00");

			///////////////////////////
			// Initializing variants //
			///////////////////////////
			instanceIsAvailable_ = false;
			isClosing = false;
			exceptionLog = new ByteArrayOutputStream();
			exceptionStream = new PrintStream(exceptionLog);
			exceptionHeader = "";
			processLog.delete(0, processLog.length());
			variantMap.clear();
			
			///////////////////////////////////////////
			// Setup specifications for the function //
			///////////////////////////////////////////
			if (functionElement != null
					&& (functionElement_ == null || !functionElement_.getAttribute("ID").equals(functionElement.getAttribute("ID")))) {
				setFunctionSpecifications(functionElement);
			}

			/////////////////////////////////
			// Write log to start function //
			/////////////////////////////////
			programSequence = session_.writeLogOfFunctionStarted(functionElement_.getAttribute("ID"), functionElement_.getAttribute("Name"));

			///////////////////////////////////////
			// Fetch record of the primary table //
			///////////////////////////////////////
			fetchTableRecord();
			if (!this.isClosing) {

				///////////////////////////////////
				// Create PDF file and browse it //
				///////////////////////////////////
				session_.browseFile(createPDFFileAndGetURI());
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError5"));
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		} finally {
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		}

		///////////////////
		// CloseFunction //
		///////////////////
		closeFunction();

		return returnMap_;
	}

	public void setFunctionSpecifications(org.w3c.dom.Element functionElement) throws Exception {
		SortableDomElementListModel sortedList;
		String workAlias, workTableID, workFieldID;
		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;

		/////////////////////////////////////////////
		// Set specifications to the inner variant //
		/////////////////////////////////////////////
		functionElement_ = functionElement;

		//////////////////////////////////////////////
		// Setup the primary table and refer tables //
		//////////////////////////////////////////////
		primaryTable_ = new XF290_PrimaryTable(functionElement_, this);
		referTableList.clear();
		NodeList referNodeList = primaryTable_.getTableElement().getElementsByTagName("Refer");
		sortedList = XFUtility.getSortedListModel(referNodeList, "Order");
		for (int i = 0; i < sortedList.getSize(); i++) {
			org.w3c.dom.Element element = (org.w3c.dom.Element)sortedList.getElementAt(i);
			referTableList.add(new XF290_ReferTable(element, this));
		}

		/////////////////////////////////
		// Setup phrase and field List //
		/////////////////////////////////
		XF290_Phrase phrase;
		headerPhraseList.clear();
		fieldList.clear();
		paragraphList.clear();
		NodeList nodeList = functionElement_.getElementsByTagName("Phrase");
		SortableDomElementListModel sortableList = XFUtility.getSortedListModel(nodeList, "Order");
		for (int i = 0; i < sortableList.getSize(); i++) {
			phrase = new XF290_Phrase((org.w3c.dom.Element)sortableList.getElementAt(i));
			if (phrase.getBlock().equals("HEADER")) {
				headerPhraseList.add(phrase);
			}
			if (phrase.getBlock().equals("PARAGRAPH")) {
				paragraphList.add(phrase);
			}
		}

		//////////////////////////////////////////////////////////////
		// Add fields on phrases if they are not on the column list //
		//////////////////////////////////////////////////////////////
		for (int i = 0; i < headerPhraseList.size(); i++) {
			for (int j = 0; j < headerPhraseList.get(i).getDataSourceNameList().size(); j++) {
				if (!existsInFieldList(headerPhraseList.get(i).getDataSourceNameList().get(j))) {
					fieldList.add(new XF290_Field(headerPhraseList.get(i).getDataSourceNameList().get(j), this));
				}
			}
		}
		for (int i = 0; i < paragraphList.size(); i++) {
			for (int j = 0; j < paragraphList.get(i).getDataSourceNameList().size(); j++) {
				if (!existsInFieldList(paragraphList.get(i).getDataSourceNameList().get(j))) {
					fieldList.add(new XF290_Field(paragraphList.get(i).getDataSourceNameList().get(j), this));
				}
			}
		}

		/////////////////////////////////////////////////////////////////////
		// Add primary table key fields if they are not on the column list //
		/////////////////////////////////////////////////////////////////////
		for (int i = 0; i < primaryTable_.getKeyFieldList().size(); i++) {
			if (!existsInFieldList(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldList().get(i))) {
				fieldList.add(new XF290_Field(primaryTable_.getTableID(), "", primaryTable_.getKeyFieldList().get(i), this));
			}
		}

		////////////////////////////////////////////////////////
		// Analyze fields in script and add them if necessary //
		////////////////////////////////////////////////////////
		for (int i = 0; i < primaryTable_.getScriptList().size(); i++) {
			if	(primaryTable_.getScriptList().get(i).isToBeRunAtEvent("BR", "")
					|| primaryTable_.getScriptList().get(i).isToBeRunAtEvent("AR", "")) {
				for (int j = 0; j < primaryTable_.getScriptList().get(i).getFieldList().size(); j++) {
					workTokenizer = new StringTokenizer(primaryTable_.getScriptList().get(i).getFieldList().get(j), "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!existsInFieldList(workTableID, workAlias, workFieldID)) {
						workElement = session_.getFieldElement(workTableID, workFieldID);
						if (workElement == null) {
							String msg = XFUtility.RESOURCE.getString("FunctionError1") + primaryTable_.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + primaryTable_.getScriptList().get(i).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
							JOptionPane.showMessageDialog(null, msg);
							throw new Exception(msg);
						} else {
							if (primaryTable_.isValidDataSource(workTableID, workAlias, workFieldID)) {
								fieldList.add(new XF290_Field(workTableID, workAlias, workFieldID, this));
							}
						}
					}
				}
			}
		}

		///////////////////////////////////////////////////////////
		// Add BYTEA-type-field for BYTEA field as HIDDEN fields //
		///////////////////////////////////////////////////////////
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getBasicType().equals("BYTEA") && !fieldList.get(i).getByteaTypeFieldID().equals("")) {
				if (!existsInFieldList(primaryTable_.getTableID(), "", fieldList.get(i).getByteaTypeFieldID())) {
					fieldList.add(new XF290_Field(primaryTable_.getTableID(), "", fieldList.get(i).getByteaTypeFieldID(), this));
				}
			}
		}

		////////////////////////////////////////////////////////////
		// Analyze refer tables and add their fields if necessary //
		////////////////////////////////////////////////////////////
		for (int i = referTableList.size()-1; i > -1; i--) {
			for (int j = 0; j < referTableList.get(i).getFieldIDList().size(); j++) {
				if (existsInFieldList(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j))) {
					referTableList.get(i).setToBeExecuted(true);
					break;
				}
			}
			if (referTableList.get(i).isToBeExecuted()) {
				for (int j = 0; j < referTableList.get(i).getFieldIDList().size(); j++) {
					if (!existsInFieldList(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j))) {
						fieldList.add(new XF290_Field(referTableList.get(i).getTableID(), referTableList.get(i).getTableAlias(), referTableList.get(i).getFieldIDList().get(j), this));
					}
				}
				for (int j = 0; j < referTableList.get(i).getWithKeyFieldIDList().size(); j++) {
					workTokenizer = new StringTokenizer(referTableList.get(i).getWithKeyFieldIDList().get(j), "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!existsInFieldList(workTableID, workAlias, workFieldID)) {
						fieldList.add(new XF290_Field(workTableID, workAlias, workFieldID, this));
					}
				}
			}
		}

		//////////////////////////////////////
		// Setup Script Engine and Bindings //
		//////////////////////////////////////
		scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
		scriptBindings = scriptEngine.createBindings();
		scriptBindings.put("instance", (XFScriptable)this);
		for (int i = 0; i < fieldList.size(); i++) {
			scriptBindings.put(fieldList.get(i).getDataSourceID(), fieldList.get(i));
		}
	}

	public void setErrorAndCloseFunction() {
		returnMap_.put("RETURN_CODE", "99");
		this.rollback();
		closeFunction();
	}

	void closeFunction() {
		if (!returnMap_.get("RETURN_CODE").equals("99")) {
			this.commit();
		}
		instanceIsAvailable_ = true;
		String errorLog = "";
		if (exceptionLog.size() > 0 || !exceptionHeader.equals("")) {
			errorLog = exceptionHeader + exceptionLog.toString();
		}
		session_.writeLogOfFunctionClosed(programSequence, returnMap_.get("RETURN_CODE").toString(), processLog.toString(), errorLog);
	}
	
	public void cancelWithMessage(String message) {
		if (!message.equals("")) {
			JOptionPane.showMessageDialog(null, message);
		}
		returnMap_.put("RETURN_CODE", "01");
		isClosing = true;
	}
	
	public void cancelWithScriptException(ScriptException e, String scriptName) {
		JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError7") + scriptName + XFUtility.RESOURCE.getString("FunctionError8"));
		exceptionHeader = "'" + scriptName + "' Script error\n";
		e.printStackTrace(exceptionStream);
		this.rollback();
		setErrorAndCloseFunction();
	}
	
	public void cancelWithException(Exception e) {
		JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError5") + "\n" + e.getMessage());
		e.printStackTrace(exceptionStream);
		this.rollback();
		setErrorAndCloseFunction();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// Function calling without committing as this is the method used by 'Table-Script' //
	//////////////////////////////////////////////////////////////////////////////////////
	public void callFunction(String functionID) {
		try {
			returnMap_ = session_.executeFunction(functionID, parmMap_);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			exceptionHeader = e.getMessage();
			setErrorAndCloseFunction();
		}
	}
	
	public void startProgress(String text, int maxValue) {
	}
	
	public void incrementProgress() {
	}
	
	public void endProgress() {
	}
	
	public void commit() {
		session_.commit(true, processLog);
	}
	
	public void rollback() {
		session_.commit(false, processLog);
	}

	private URI createPDFFileAndGetURI() {
		File pdfFile = null;
		String pdfFileName = "";
		FileOutputStream fileOutputStream = null;
		com.lowagie.text.Font font, chunkFont;
		Chunk chunk;
		Phrase phrase;

		///////////////////
		// Setup margins //
		///////////////////
		float leftMargin = 50;
		float rightMargin = 50;
		float topMargin = 50;
		float bottomMargin = 50;
		StringTokenizer workTokenizer = new StringTokenizer(functionElement_.getAttribute("Margins"), ";" );
		try {
			leftMargin = Float.parseFloat(workTokenizer.nextToken());
			rightMargin = Float.parseFloat(workTokenizer.nextToken());
			topMargin = Float.parseFloat(workTokenizer.nextToken());
			bottomMargin = Float.parseFloat(workTokenizer.nextToken());
		} catch (Exception e) {
		}

		//////////////////////////////////////
		// Generate PDF file and PDF writer //
		//////////////////////////////////////
		com.lowagie.text.Rectangle pageSize = XFUtility.getPageSize(functionElement_.getAttribute("PageSize"), functionElement_.getAttribute("Direction"));
		com.lowagie.text.Document pdfDoc = new com.lowagie.text.Document(pageSize, leftMargin, rightMargin, topMargin, bottomMargin); 
		try {
			pdfFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".pdf");
			pdfFileName = pdfFile.getPath();
			returnMap_.put("FILE_NAME", pdfFileName);
			fileOutputStream = new FileOutputStream(pdfFileName);
			PdfWriter writer = PdfWriter.getInstance(pdfDoc, fileOutputStream);

			/////////////////////////////
			// Set document attributes //
			/////////////////////////////
			pdfDoc.addTitle(functionElement_.getAttribute("Name"));
			pdfDoc.addAuthor(session_.getUserName()); 
			pdfDoc.addCreator("XEAD Driver");

			////////////////////////////////
			// Add header to the document //
			////////////////////////////////
			addHeaderToDocument(pdfDoc);

			////////////////////////////////////////////
			// Add page-number-footer to the document //
			////////////////////////////////////////////
			if (functionElement_.getAttribute("WithPageNumber").equals("T")) {
				HeaderFooter footer = new HeaderFooter(new Phrase("--"), new Phrase("--"));
				footer.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
				footer.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
				pdfDoc.setFooter(footer);
			}

			/////////////////////////////////
			// Add phrases to the document //
			/////////////////////////////////
			pdfDoc.open();
			PdfContentByte cb = writer.getDirectContent();
			Paragraph paragraph = null;
			String keyword = "";
			for (int i = 0; i < paragraphList.size(); i++) {
				if (paragraphList.get(i).getBlock().equals("PARAGRAPH")) {
					paragraph = new Paragraph(new Phrase(""));
					paragraph.setAlignment(paragraphList.get(i).getAlignment());
					paragraph.setIndentationRight(paragraphList.get(i).getMarginRight());
					paragraph.setIndentationLeft(paragraphList.get(i).getMarginLeft());
					paragraph.setSpacingAfter(paragraphList.get(i).getSpacingAfter());

					font = new com.lowagie.text.Font(session_.getBaseFontWithID(paragraphList.get(i).getFontID()), paragraphList.get(i).getFontSize(), paragraphList.get(i).getFontStyle());
					phrase = new Phrase("", font);
					for (int j = 0; j < paragraphList.get(i).getValueKeywordList().size(); j++) {
						keyword = paragraphList.get(i).getValueKeywordList().get(j);
						if ((keyword.contains("&Line(") || keyword.contains("&Rect(")) && cb != null) {
							XFUtility.drawLineOrRect(keyword, cb);
						} else {
							chunkFont = new com.lowagie.text.Font(session_.getBaseFontWithID(paragraphList.get(i).getFontID()), paragraphList.get(i).getFontSize(), paragraphList.get(i).getFontStyle());
							chunk = getChunkForKeyword(keyword, cb, chunkFont);
							if (chunk != null) {
								phrase.add(chunk);
							}
						}
					}
					paragraph.add(phrase);
					pdfDoc.add(paragraph);
				}
			}

		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pdfDoc.close();
		}
		//
		return pdfFile.toURI();
	}
	private void addHeaderToDocument(com.lowagie.text.Document pdfDoc) {
		com.lowagie.text.Font font, chunkFont;
		Chunk chunk;
		Phrase phrase;
		if (headerPhraseList.size() == 1) {
			font = new com.lowagie.text.Font(session_.getBaseFontWithID(headerPhraseList.get(0).getFontID()), headerPhraseList.get(0).getFontSize(), headerPhraseList.get(0).getFontStyle());
			phrase = new Phrase("", font);
			for (int i = 0; i < headerPhraseList.get(0).getValueKeywordList().size(); i++) {
				chunkFont = new com.lowagie.text.Font(session_.getBaseFontWithID(headerPhraseList.get(0).getFontID()), headerPhraseList.get(0).getFontSize(), headerPhraseList.get(0).getFontStyle());
				chunk = getChunkForKeyword(headerPhraseList.get(0).getValueKeywordList().get(i), null, chunkFont);
				if (chunk != null) {
					phrase.add(chunk);
				}
			}
			HeaderFooter header = new HeaderFooter(phrase,	false);
			header.setAlignment(headerPhraseList.get(0).getAlignment());
			pdfDoc.setHeader(header);
		}
		if (headerPhraseList.size() > 1) {
			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(100.0f);
			table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
			for (int i = 0; i < headerPhraseList.size(); i++) {
				font = new com.lowagie.text.Font(session_.getBaseFontWithID(headerPhraseList.get(i).getFontID()), headerPhraseList.get(i).getFontSize(), headerPhraseList.get(i).getFontStyle());
				phrase = new Phrase("", font);
				for (int j = 0; j < headerPhraseList.get(i).getValueKeywordList().size(); j++) {
					chunkFont = new com.lowagie.text.Font(session_.getBaseFontWithID(headerPhraseList.get(i).getFontID()), headerPhraseList.get(i).getFontSize(), headerPhraseList.get(i).getFontStyle());
					chunk = getChunkForKeyword(headerPhraseList.get(i).getValueKeywordList().get(j), null, chunkFont);
					if (chunk != null) {
						phrase.add(chunk);
					}
				}
				PdfPCell cell = new PdfPCell(table.getDefaultCell());
				cell.setHorizontalAlignment(headerPhraseList.get(i).getAlignment());
				cell.setPhrase(phrase);
				table.addCell(cell);
			}
			if (headerPhraseList.size() % 2 != 0) {
				phrase = new Phrase("");
				PdfPCell cell = new PdfPCell(table.getDefaultCell());
				cell.setPhrase(phrase);
				table.addCell(cell);
			}
			Paragraph paragraph = new Paragraph();
			paragraph.add(table);
			HeaderFooter header = new HeaderFooter(paragraph, false);
			header.setBorder(Rectangle.NO_BORDER);
			pdfDoc.setHeader(header);
		}
	}

	private Chunk getChunkForKeyword(String keyword, PdfContentByte cb, com.lowagie.text.Font chunkFont) {
		StringTokenizer workTokenizer;
		String wrkStr = "";
		com.lowagie.text.Image image;
		int pos, x, y, w, h;
		Chunk chunk = null;

		try {
			if (keyword.contains("&Text(")) {
				pos = keyword.lastIndexOf(")");
				chunk = new Chunk(keyword.substring(6, pos));
			}

			if (keyword.contains("&DataSource(")) {
				pos = keyword.lastIndexOf(")");
				wrkStr = getExternalStringValueOfFieldByName(keyword.substring(12, pos));
				if (wrkStr.equals("")) {
					wrkStr = " ";
				}
				chunkFont.setColor(getColorOfDataSource(keyword.substring(12, pos)));
				chunk = new Chunk(wrkStr, chunkFont);
			}

			if (keyword.contains("&DataSourceImage(")) {
				x = 0; y = 0; w = 0; h = 0;
				try {
					pos = keyword.lastIndexOf(")");
					workTokenizer = new StringTokenizer(keyword.substring(17, pos), ";" );
					wrkStr = workTokenizer.nextToken().trim();
					x = Integer.parseInt(workTokenizer.nextToken().trim());
					y = Integer.parseInt(workTokenizer.nextToken().trim());
					w = Integer.parseInt(workTokenizer.nextToken().trim());
					h = Integer.parseInt(workTokenizer.nextToken().trim());
				} catch (Exception e) {
				}
				image = XFUtility.getImageForPDF(session_.getImageFileFolder() + getExternalStringValueOfFieldByName(wrkStr), w, h);
				chunk = new Chunk(image, x, y, true);
			}

			if (keyword.contains("&Barcode(") && cb != null) {
				pos = keyword.lastIndexOf(")");
				workTokenizer = new StringTokenizer(keyword.substring(9, pos), ";" );
				wrkStr = workTokenizer.nextToken().trim();
				String type = "NW7";
				if (workTokenizer.hasMoreTokens()) {
					type = workTokenizer.nextToken().trim();
				}
				chunk = XFUtility.getBarcodeChunkOfValue(getExternalStringValueOfFieldByName(wrkStr), type, cb);
			}

			if (keyword.contains("&SystemVariant(")) {
				pos = keyword.lastIndexOf(")");
				chunk = new Chunk(session_.getSystemVariantString(keyword.substring(15, pos)));
			}

			if (keyword.contains("&DateTime(")) {
				pos = keyword.lastIndexOf(")");
				chunk = new Chunk(session_.getTodayTime(keyword.substring(10, pos)));
			}

			if (keyword.contains("&Date(")) {
				pos = keyword.lastIndexOf(")");
				chunk = new Chunk(session_.getToday(keyword.substring(6, pos)));
			}

			if (keyword.contains("&Image(")) {
				x = 0; y = 0; w = 0; h = 0;
				try {
					pos = keyword.lastIndexOf(")");
					workTokenizer = new StringTokenizer(keyword.substring(7, pos), ";" );
					wrkStr = workTokenizer.nextToken().trim();
					x = Integer.parseInt(workTokenizer.nextToken().trim());
					y = Integer.parseInt(workTokenizer.nextToken().trim());
					w = Integer.parseInt(workTokenizer.nextToken().trim());
					h = Integer.parseInt(workTokenizer.nextToken().trim());
				} catch (Exception e) {
				}
				image = XFUtility.getImageForPDF(session_.getImageFileFolder() + wrkStr, w, h);
				chunk = new Chunk(image, x, y, true);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError39") + keyword + XFUtility.RESOURCE.getString("FunctionError40") + e.toString());
		}

		return chunk;
	}
	
	void fetchTableRecord() {
		try {
			for (int i = 0; i < fieldList.size(); i++) {
				if (parmMap_.containsKey(fieldList.get(i).getFieldID())) {
					fieldList.get(i).setValue(parmMap_.get(fieldList.get(i).getFieldID()));
				}
			}
			primaryTable_.runScript("BR", "");
			XFTableOperator operator = createTableOperator(primaryTable_.getSQLToSelect());
			if (operator.next()) {
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).getTableAlias().equals(primaryTable_.getTableID())) {
						fieldList.get(i).setValueOfResultSet(operator);
					}
				}
				fetchReferTableRecords("AR", "");
				for (int i = 0; i < fieldList.size(); i++) {
					if (fieldList.get(i).getBasicType().equals("BYTEA")) {
						fieldList.get(i).setupByteaTypeField(fieldList);
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError30"));
				returnMap_.put("RETURN_CODE", "01");
				isClosing = true;
			}
		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		}
	}

	protected void fetchReferTableRecords(String event, String specificReferTable) {
		XFTableOperator operator;
		try {

			primaryTable_.runScript(event, "BR()");

			for (int i = 0; i < referTableList.size(); i++) {
				if (specificReferTable.equals("") || specificReferTable.equals(referTableList.get(i).getTableAlias())) {

					if (referTableList.get(i).isToBeExecuted()) {

						primaryTable_.runScript(event, "BR(" + referTableList.get(i).getTableAlias() + ")");

						for (int j = 0; j < fieldList.size(); j++) {
							if (fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
								fieldList.get(j).setValue(null);
							}
						}

						if (!referTableList.get(i).isKeyNullable() || !referTableList.get(i).isKeyNull()) {

							operator = createTableOperator(referTableList.get(i).getSelectSQL(false));
							if (operator.next()) {
								for (int j = 0; j < fieldList.size(); j++) {
									if (fieldList.get(j).getTableAlias().equals(referTableList.get(i).getTableAlias())) {
										fieldList.get(j).setValueOfResultSet(operator);
									}
								}
								primaryTable_.runScript(event, "AR(" + referTableList.get(i).getTableAlias() + ")");
							}
						}
					}
				}
			}

			primaryTable_.runScript(event, "AR()");

		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		}
	}

	public String getFunctionID() {
		return functionElement_.getAttribute("ID");
	}

	public HashMap<String, Object> getParmMap() {
		return parmMap_;
	}
	
	public void setProcessLog(String text) {
		XFUtility.appendLog(text, processLog);
	}
	
	public StringBuffer getProcessLog() {
		return processLog;
	}

	public Object getVariant(String variantID) {
		if (variantMap.containsKey(variantID)) {
			return variantMap.get(variantID);
		} else {
			return "";
		}
	}

	public void setVariant(String variantID, Object value) {
		variantMap.put(variantID, value);
	}

	public XFTableOperator createTableOperator(String oparation, String tableID) {
		XFTableOperator operator = null;
		try {
			operator = new XFTableOperator(session_, processLog, oparation, tableID);
		} catch (Exception e) {
			e.printStackTrace(exceptionStream);
			setErrorAndCloseFunction();
		}
		return operator;
	}

	public XFTableOperator createTableOperator(String sqlText) {
		return new XFTableOperator(session_, processLog, sqlText);
	}

	public XFTableEvaluator createTableEvaluator(String tableID) {
		return new XFTableEvaluator(this, tableID);
	}

	public HashMap<String, Object> getReturnMap() {
		return returnMap_;
	}

	public String getScriptNameRunning() {
		return scriptNameRunning;
	}

	public Session getSession() {
		return session_;
	}

	public PrintStream getExceptionStream() {
		return exceptionStream;
	}
	
	public Object getFieldObjectByID(String tableID, String fieldID) {
		String id = tableID + "_" + fieldID;
		if (scriptBindings.containsKey(id)) {
			return scriptBindings.get(id);
		} else {
			JOptionPane.showMessageDialog(null, "Field object " + id + " is not found.");
			return null;
		}
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

	public XF290_PrimaryTable getPrimaryTable() {
		return primaryTable_;
	}

	public ArrayList<XF290_Field> getFieldList() {
		return fieldList;
	}

	public ArrayList<String> getKeyFieldList() {
		return primaryTable_.getKeyFieldList();
	}

	public ArrayList<XF290_ReferTable> getReferTableList() {
		return referTableList;
	}

	public boolean existsInFieldList(String tableID, String tableAlias, String fieldID) {
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

	public boolean existsInFieldList(String dataSourceName) {
		boolean result = false;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getDataSourceName().equals(dataSourceName)) {
				result = true;
			}
		}
		return result;
	}

	public String getTableIDOfTableAlias(String tableAlias) {
		String tableID = tableAlias;
		XF290_ReferTable referTable;
		for (int j = 0; j < referTableList.size(); j++) {
			referTable = referTableList.get(j);
			referTable.getTableAlias();
			if (referTable.getTableAlias().equals(tableAlias)) {
				tableID = referTable.getTableID();
				break;
			}
		}
		return tableID;
	}

	public Object getInternalValueOfFieldByName(String dataSourceName) {
		Object obj = null;
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getDataSourceName().equals(dataSourceName)) {
				obj = fieldList.get(i).getInternalValue();
				break;
			}
		}
		return obj;
	}

	public String getExternalStringValueOfFieldByName(String dataSourceName) {
		String value = "";
		String wrkStr, basicType, fmt = "";

		StringTokenizer workTokenizer = new StringTokenizer(dataSourceName, ";");
		wrkStr = workTokenizer.nextToken().trim();
		if (workTokenizer.hasMoreTokens()) {
			fmt = workTokenizer.nextToken().trim();
		}

		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getDataSourceName().equals(wrkStr)) {
				if (fieldList.get(i).isKubunField()
				  || fieldList.get(i).getBasicType().equals("BYTEA")
				  || fieldList.get(i).getDataTypeOptionList().contains("MSEQ")
				  || fieldList.get(i).getDataTypeOptionList().contains("YMONTH")
				  || fieldList.get(i).getDataTypeOptionList().contains("FYEAR")) {
					value = fieldList.get(i).getExternalValue().toString();
				} else {
					value = fieldList.get(i).getInternalValue().toString();
					basicType = fieldList.get(i).getBasicType();
					if (basicType.equals("DATE")) {
						if (fmt.equals("")) {
							fmt = session_.getDateFormat();
						}
						wrkStr = value.toString().substring(0, 10);
						value = XFUtility.getUserExpressionOfUtilDate(XFUtility.convertDateFromSqlToUtil(java.sql.Date.valueOf(wrkStr)), fmt, false);
					}
					if (basicType.equals("INTEGER")) {
						if (fieldList.get(i).getDataTypeOptionList().contains("NO_EDIT")
								|| fieldList.get(i).getDataTypeOptionList().contains("ZERO_SUPPRESS")) {
							value = XFUtility.getFormattedIntegerValue(value.toString(), fieldList.get(i).getDataTypeOptionList(), fieldList.get(i).getDataSize());
						} else {
							value = XFUtility.getEditValueOfLong(Long.parseLong(value), fmt, fieldList.get(i).getDataSize());
						}
					}
					if (basicType.equals("FLOAT")) {
						value = XFUtility.getEditValueOfDouble(Double.parseDouble(value), fmt, fieldList.get(i).getDecimalSize());
					}
					if (fieldList.get(i).getDataTypeOptionList().contains("YMONTH") || fieldList.get(i).getDataTypeOptionList().contains("FYEAR")) {
						if (fmt.equals("")) {
							fmt = session_.getDateFormat();
						}
						value = XFUtility.getUserExpressionOfYearMonth(value.toString(), fmt);
					}
					if (fieldList.get(i).getDataTypeOptionList().contains("MSEQ")) {
						wrkStr = value.toString();
						if (!wrkStr.equals("")) {
							value = XFUtility.getUserExpressionOfMSeq(Integer.parseInt(wrkStr), session_);
						}
					}

				}
				break;
			}
		}
		return value;
	}

	public Color getColorOfDataSource(String dataSourceName) {
		Color color = Color.black;
		StringTokenizer workTokenizer = new StringTokenizer(dataSourceName, ";");
		String wrkStr = workTokenizer.nextToken().trim();
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getDataSourceName().equals(wrkStr)) {
				color = fieldList.get(i).getForeground();
				break;
			}
		}
		return color;
	}
}

class XF290_Field implements XFFieldScriptable {
	org.w3c.dom.Element tableElement = null;
	private String dataSourceName_ = "";
	private String tableID_ = "";
	private String tableAlias_ = "";
	private String fieldID_ = "";
	private String fieldName = "";
	private String dataType = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private XFTextField xFTextField = null;
	private XFEditableField component = null;
	private String byteaTypeFieldID = "";
	private boolean isNullable = true;
	private boolean isKey = false;
	private boolean isFieldOnPrimaryTable = false;
	private boolean isVirtualField = false;
	private boolean isImage = false;
	private boolean isKubunField = false;
	private XF290 dialog_;
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private Color foreground = Color.black;

	public XF290_Field(String dataSourceName, XF290 dialog){
		super();

		dataSourceName_ = dataSourceName;
		dialog_ = dialog;
		StringTokenizer workTokenizer = new StringTokenizer(dataSourceName_, ";" );
		dataSourceName_ = workTokenizer.nextToken().trim();
		workTokenizer = new StringTokenizer(dataSourceName_, "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog_.getTableIDOfTableAlias(tableAlias_);
		fieldID_ =workTokenizer.nextToken();

		setupVariants();
	}

	public XF290_Field(String tableID, String tableAlias, String fieldID, XF290 dialog){
		super();

		tableID_ = tableID;
		fieldID_ = fieldID;
		dialog_ = dialog;
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}
		dataSourceName_ = tableAlias_ + "." + fieldID_;

		setupVariants();
	}

	public void setupVariants(){
		if (tableID_.equals(dialog_.getPrimaryTable().getTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnPrimaryTable = true;

			ArrayList<String> keyNameList = dialog_.getKeyFieldList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
		}

		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		fieldName = workElement.getAttribute("Name");
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		if (workElement.getAttribute("Nullable").equals("F")) {
			isNullable = false;
		}
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		byteaTypeFieldID = workElement.getAttribute("ByteaTypeField");

		String wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
		if (!wrkStr.equals("")) {
			try {
				isKubunField = true;
				XFTableOperator operator = dialog_.createTableOperator("Select", dialog_.getSession().getTableNameOfUserVariants());
				operator.addKeyValue("IDUSERKUBUN", wrkStr);
				operator.setOrderBy("SQLIST");
				while (operator.next()) {
					kubunValueList.add(operator.getValueOf("KBUSERKUBUN").toString().trim());
					kubunTextList.add(operator.getValueOf("TXUSERKUBUN").toString().trim());
				}
			} catch (Exception e) {
				e.printStackTrace(dialog_.getExceptionStream());
				dialog_.setErrorAndCloseFunction();
			}
		}

		tableElement = (org.w3c.dom.Element)workElement.getParentNode();

		xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, "", dialog_.getSession().systemFont);
		xFTextField.setLocation(5, 0);
		component = xFTextField;
	}

	public String getByteaTypeFieldID(){
		return byteaTypeFieldID;
	}

	public String getDataSourceName(){
		return dataSourceName_;
	}

	public void setEnabled(boolean enabled) {
	}

	public boolean isEnabled() {
		return true;
	}

	public String getFieldID(){
		return fieldID_;
	}

	public String getDataSourceID(){
		return tableAlias_ + "_" + fieldID_;
	}

	public String getName(){
		return fieldName;
	}

	public void setValueList(String[] valueList) {
	}

	public String[] getValueList() {
		return new String[0];
	}

	public String getTableAlias(){
		return tableAlias_;
	}

	public String getTableID(){
		return tableID_;
	}

	public int getDataSize(){
		return dataSize;
	}

	public int getDecimalSize(){
		return decimalSize;
	}

	public ArrayList<String> getDataTypeOptionList() {
		return dataTypeOptionList;
	}

	public boolean isNullable(){
		return isNullable;
	}

	public boolean isNull(){
		return XFUtility.isNullValue(this.getBasicType(), component.getInternalValue());
	}

	public boolean isVirtualField(){
		return isVirtualField;
	}

	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}

	public boolean isImage(){
		return isImage;
	}

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}

	public boolean isFieldOnPrimaryTable(){
		return isFieldOnPrimaryTable;
	}

	public boolean isKey(){
		return isKey;
	}

	public boolean isKubunField(){
		return isKubunField;
	}

	public void setValueOfResultSet(XFTableOperator operator){
		try {
			if (!this.isVirtualField) {
				String basicType = this.getBasicType();
				if (basicType.equals("INTEGER")) {
					String value = operator.getValueOf(this.getFieldID()).toString();
					if (value == null || value.equals("")) {
						component.setValue("");
					} else {
						int pos = value.indexOf(".");
						if (pos >= 0) {
							value = value.substring(0, pos);
						}
						component.setValue(Long.parseLong(value));
					}
				}
				if (basicType.equals("FLOAT")) {
					String value = operator.getValueOf(this.getFieldID()).toString();
					if (value == null || value.equals("")) {
						component.setValue("");
					} else {
						component.setValue(Double.parseDouble(value));
					}
				}
				if (basicType.equals("STRING")
						|| basicType.equals("TIME")
						|| basicType.equals("DATETIME")) {
					Object value = operator.getValueOf(this.getFieldID());
					if (value == null) {
						component.setValue("");
					} else {
						component.setValue(value.toString().trim());
					}
				}
				if (basicType.equals("DATE")) {
					component.setValue(operator.getValueOf(this.getFieldID()));
				}
				if (basicType.equals("BYTEA")) {
					component.setValue("<null>"); //BYTEA-field is not on SELECT-SQL//
				}
				component.setOldValue(component.getInternalValue());
			}
		} catch (Exception e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}
	
	public void setupByteaTypeField(ArrayList<XF290_Field> fieldList) {
		for (int i = 0; i < fieldList.size(); i++) {
			if (fieldList.get(i).getFieldID().equals(byteaTypeFieldID)) {
				this.setValue("<" + fieldList.get(i).getValue() + ">");
				break;
			}
		}
	}

	public void setValue(Object value){
		XFUtility.setValueToEditableField(this.getBasicType(), value, component);
	}

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public Object getInternalValue(){
		Object returnObj = null;
		String basicType = this.getBasicType();
		if (basicType.equals("INTEGER")
				|| basicType.equals("FLOAT")
				|| basicType.equals("STRING")
				|| basicType.equals("BYTEA")) {
			String value = (String)component.getInternalValue();
			if (value == null) {
				value = "";
			}
			returnObj = value;
		}
		if (basicType.equals("DATE")) {
			returnObj = (String)component.getInternalValue();
		}
		if (basicType.equals("DATETIME")) {
			returnObj = (String)component.getInternalValue();
		}
		return returnObj;
	}

	public Object getExternalValue() {
		Object returnObj = getInternalValue();
		if (this.isKubunField) {
			if (returnObj == null) {
				returnObj = "";
			} else {
				String wrkStr = returnObj.toString().trim();
				for (int i = 0; i < kubunValueList.size(); i++) {
					if (kubunValueList.get(i).equals(wrkStr)) {
						returnObj = kubunTextList.get(i);
						break;
					}
				}
			}
		} else {
			if (dataTypeOptionList.contains("MSEQ")) {
				returnObj = XFUtility.getUserExpressionOfMSeq(Integer.parseInt((String)component.getInternalValue()), dialog_.getSession());
			}
			if (dataTypeOptionList.contains("YMONTH") || dataTypeOptionList.contains("FYEAR")) {
				returnObj = XFUtility.getUserExpressionOfYearMonth(component.getInternalValue().toString(), dialog_.getSession().getDateFormat());
			}
		}
		return returnObj;
	}

	public Object getValue() {
		Object returnObj = null;
		if (this.getBasicType().equals("INTEGER")) {
			returnObj = Long.parseLong((String)component.getInternalValue());
		} else {
			if (this.getBasicType().equals("FLOAT")) {
				returnObj = Double.parseDouble((String)component.getInternalValue());
			} else {
				if (component.getInternalValue() == null) {
					returnObj = "";
				} else {
					returnObj = (String)component.getInternalValue();
				}
			}
		}
		return returnObj;
	}

	public void setOldValue(Object object){
	}

	public Object getOldValue() {
		return getValue();
	}
	
	public boolean isValueChanged() {
		return !this.getValue().equals(this.getOldValue());
	}

	public boolean isEditable() {
		return false;
	}

	public void setEditable(boolean isEditable) {
	}

	public void setError(String message) {
	}

	public String getError() {
		return "";
	}

	public void setColor(String color) {
		foreground = XFUtility.convertStringToColor(color);
	}

	public String getColor() {
		return XFUtility.convertColorToString(foreground);
	}

	public Color getForeground() {
		return foreground;
	}
}

class XF290_Phrase extends Object {
	org.w3c.dom.Element phraseElement_ = null;
	private String block = "";
	private String alignment = "";
	private ArrayList<String> valueKeywordList = new ArrayList<String>();
	private ArrayList<String> dataSourceNameList = new ArrayList<String>();
	private String fontID = "";
	private int fontSize = 0;
	private float alignmentMargin;
	private float marginLeft;
	private float marginRight;
	private float spacingAfter;
	private String fontStyle = "";

	public XF290_Phrase(org.w3c.dom.Element phraseElement){
		super();
		phraseElement_ = phraseElement;
		block = phraseElement_.getAttribute("Block");
		alignment = phraseElement_.getAttribute("Alignment");
		try {
			alignmentMargin = Float.parseFloat(phraseElement_.getAttribute("AlignmentMargin"));
		} catch (NumberFormatException e) {
			alignmentMargin = 0f;
		}
		if (alignmentMargin > 0f) {
			if (alignment.equals("LEFT")) {
				marginLeft = alignmentMargin;
				marginRight = 0f;
			}
			if (alignment.equals("RIGHT")) {
				marginLeft = 0f;
				marginRight = alignmentMargin;
			}
			if (alignment.equals("CENTER")) {
				marginLeft = alignmentMargin;
				marginRight = alignmentMargin;
			}
		} else {
			try {
				marginRight = Float.parseFloat(phraseElement_.getAttribute("MarginRight"));
			} catch (NumberFormatException e) {
				marginRight = 0f;
			}
			try {
				marginLeft = Float.parseFloat(phraseElement_.getAttribute("MarginLeft"));
			} catch (NumberFormatException e) {
				marginLeft = 0f;
			}
		}

		try {
			spacingAfter = Float.parseFloat(phraseElement_.getAttribute("SpacingAfter"));
		} catch (NumberFormatException e) {
			spacingAfter = 0f;
		}

		StringTokenizer workTokenizer;
		String wrkStr;
		String phraseValue = phraseElement_.getAttribute("Value");
		int pos1 = phraseValue.indexOf("&", 0);
		int pos2;
		while (pos1 > -1) {
			pos2 = phraseValue.indexOf("&", pos1+1);
			if (pos2 == -1) {
				valueKeywordList.add(phraseValue.substring(pos1, phraseValue.length()).trim());
				break;
			} else {
				valueKeywordList.add(phraseValue.substring(pos1, pos2).trim());
			}
			pos1 = pos2;
		}
		for (int i = 0; i < valueKeywordList.size(); i++) {
			if (valueKeywordList.get(i).contains("&DataSource(")) {
				pos1 = valueKeywordList.get(i).indexOf(")", 0);
				workTokenizer = new StringTokenizer(valueKeywordList.get(i).substring(12, pos1), ";");
				wrkStr = workTokenizer.nextToken().trim();
				dataSourceNameList.add(wrkStr);
			}
			if (valueKeywordList.get(i).contains("&DataSourceImage(")) {
				pos1 = valueKeywordList.get(i).indexOf(")", 0);
				workTokenizer = new StringTokenizer(valueKeywordList.get(i).substring(17, pos1), ";");
				wrkStr = workTokenizer.nextToken().trim();
				dataSourceNameList.add(wrkStr);
			}
			if (valueKeywordList.get(i).contains("&Barcode(")) {
				pos1 = valueKeywordList.get(i).indexOf(")", 0);
				workTokenizer = new StringTokenizer(valueKeywordList.get(i).substring(9, pos1), ";");
				wrkStr = workTokenizer.nextToken().trim();
				dataSourceNameList.add(wrkStr);
			}
		}

		fontID = phraseElement_.getAttribute("FontID");
		fontSize = Integer.parseInt(phraseElement_.getAttribute("FontSize"));
		fontStyle = phraseElement_.getAttribute("FontStyle");
	}

	public String getBlock(){
		return block;
	}

	public float getMarginLeft(){
		return marginLeft;
	}
	
	public float getMarginRight(){
		return marginRight;
	}

	public float getSpacingAfter(){
		return spacingAfter;
	}

	public int getAlignment(){
		int result = 0;
		if (alignment.equals("LEFT")) {
			result = com.lowagie.text.Element.ALIGN_LEFT;
		}
		if (alignment.equals("CENTER")) {
			result = com.lowagie.text.Element.ALIGN_CENTER;
		}
		if (alignment.equals("RIGHT")) {
			result = com.lowagie.text.Element.ALIGN_RIGHT;
		}
		return result;
	}

	public ArrayList<String> getValueKeywordList(){
		return valueKeywordList;
	}

	public ArrayList<String> getDataSourceNameList(){
		return dataSourceNameList;
	}

	public String getFontID(){
		return fontID;
	}

	public int getFontSize(){
		return fontSize;
	}

	public int getFontStyle(){
		int result = com.lowagie.text.Font.NORMAL;
		if (fontStyle.equals("BOLD")) {
			result = com.lowagie.text.Font.BOLD;
		}
		if (fontStyle.equals("ITALIC")) {
			result = com.lowagie.text.Font.ITALIC;
		}
		if (fontStyle.contains("BOLDITALIC")) {
			result = com.lowagie.text.Font.BOLDITALIC;
		}
		if (fontStyle.equals("UNDERLINE")) {
			result = com.lowagie.text.Font.UNDERLINE;
		}
		return result;
	}
}

class XF290_ReferTable extends Object {
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF290 dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private String dbName = "";

	public XF290_ReferTable(org.w3c.dom.Element referElement, XF290 dialog){
		super();

		referElement_ = referElement;
		dialog_ = dialog;

		tableID = referElement_.getAttribute("ToTable");
		tableElement = dialog_.getSession().getTableElement(tableID);

		if (tableElement.getAttribute("DB").equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(tableElement.getAttribute("DB"));
		}

		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}

		StringTokenizer workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			fieldIDList.add(workTokenizer.nextToken());
		}

		if (referElement_.getAttribute("ToKeyFields").equals("")) {
			org.w3c.dom.Element workElement = dialog_.getSession().getTablePKElement(tableID);
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
	}

	public String getSelectSQL(boolean isToGetRecordsForComboBox){
		org.w3c.dom.Element workElement;
		int count;
		StringBuffer buf = new StringBuffer();

		buf.append("select ");
		count = 0;
		for (int i = 0; i < fieldIDList.size(); i++) {
			workElement = dialog_.getSession().getFieldElement(tableID, fieldIDList.get(i));
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
		buf.append(tableID);

		StringTokenizer workTokenizer;
		String keyFieldID, keyFieldTableID;
		count = 0;
		boolean isToBeWithValue;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (isToGetRecordsForComboBox) {

				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				// If the field is part of PK of the primary table or one of other join table, value of field should be within WHERE to SELECT records. //
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				for (int j = 0; j < dialog_.getFieldList().size(); j++) {
					if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getDataSourceName())) {
						isToBeWithValue = false;

						workTokenizer = new StringTokenizer(withKeyFieldIDList.get(i), "." );
						keyFieldTableID = workTokenizer.nextToken();
						keyFieldID = workTokenizer.nextToken();
						if (keyFieldTableID.equals(dialog_.getPrimaryTable().getTableID())) {
							for (int k = 0; k < dialog_.getKeyFieldList().size(); k++) {
								if (keyFieldID.equals(dialog_.getKeyFieldList().get(k))) {
									isToBeWithValue = true;
								}
							}
						} else {
							if (!keyFieldTableID.equals(this.tableAlias)) {
								isToBeWithValue = true;
							}
						}

						if (isToBeWithValue) {
							if (count == 0) {
								buf.append(" where ");
							} else {
								buf.append(" and ");
							}
							buf.append(toKeyFieldIDList.get(i));
							buf.append("=");
							buf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(j).getBasicType(), dialog_.getFieldList().get(j).getInternalValue(), dbName)) ;
							count++;
							break;
						}
					}
				}
			} else {
				if (count == 0) {
					buf.append(" where ");
				} else {
					buf.append(" and ");
				}
				buf.append(toKeyFieldIDList.get(i));
				buf.append("=");
				for (int j = 0; j < dialog_.getFieldList().size(); j++) {
					if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getDataSourceName())) {
						buf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(j).getBasicType(), dialog_.getFieldList().get(j).getInternalValue(), dbName)) ;
						break;
					}
				}
				count++;
			}
		}

		return buf.toString();
	}

	public String getTableID(){
		return tableID;
	}

	public String getTableAlias(){
		return tableAlias;
	}

	public boolean isKeyNullable() {
		boolean isKeyNullable = false;
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getTableAlias() + "." + dialog_.getFieldList().get(j).getFieldID())) {
					if (dialog_.getFieldList().get(j).isNullable()) {
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
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
				if (withKeyFieldIDList.get(i).equals(dialog_.getFieldList().get(j).getTableAlias() + "." + dialog_.getFieldList().get(j).getFieldID())) {
					if (dialog_.getFieldList().get(j).isNull()) {
						isKeyNull = true;
						break;
					}
				}
			}
		}
		return isKeyNull;
	}

	public ArrayList<String> getKeyFieldIDList(){
		return toKeyFieldIDList;
	}

	public ArrayList<String> getFieldIDList(){
		return fieldIDList;
	}

	public ArrayList<String> getWithKeyFieldIDList(){
		return withKeyFieldIDList;
	}

	public void setKeyFieldValues(XFHashMap keyValues){
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getFieldList().size(); j++) {
				if (dialog_.getFieldList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))) {
					dialog_.getFieldList().get(j).setValue(keyValues.getValue(withKeyFieldIDList.get(i)));
					break;
				}
			}
		}
	}
	
	public void setToBeExecuted(boolean executed){
		isToBeExecuted = executed;
	}
	
	public boolean isToBeExecuted(){
		return isToBeExecuted;
	}
}

class XF290_PrimaryTable extends Object {
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID = "";
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF290 dialog_;
	private StringTokenizer workTokenizer;
	private String updateCounterID = "";
	private String dbName = "";

	public XF290_PrimaryTable(org.w3c.dom.Element functionElement, XF290 dialog){
		super();

		functionElement_ = functionElement;
		dialog_ = dialog;

		tableID = functionElement_.getAttribute("PrimaryTable");
		tableElement = dialog_.getSession().getTableElement(tableID);

		if (tableElement.getAttribute("DB").equals("")) {
			dbName = dialog_.getSession().getDatabaseName();
		} else {
			dbName = dialog_.getSession().getSubDBName(tableElement.getAttribute("DB"));
		}

		if (!tableElement.getAttribute("UpdateCounter").toUpperCase().equals("*NONE")) {
			updateCounterID = tableElement.getAttribute("UpdateCounter");
			if (updateCounterID.equals("")) {
				updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
			}
		}

		String wrkStr1;
		org.w3c.dom.Element workElement;

		if (functionElement_.getAttribute("KeyFields").equals("")) {
			NodeList nodeList = tableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer.hasMoreTokens()) {
						wrkStr1 = workTokenizer.nextToken();
						keyFieldList.add(wrkStr1);
					}
				}
			}
		} else {
			workTokenizer = new StringTokenizer(functionElement_.getAttribute("KeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				keyFieldList.add(workTokenizer.nextToken());
			}
		}

		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID, element, dialog_.getSession().getTableNodeList()));
		}
	}
	
	public String getName() {
		return tableElement.getAttribute("Name");
	}
	
	public String getSQLToSelect(){
		StringBuffer buf = new StringBuffer();

		buf.append("select ");

		boolean firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isFieldOnPrimaryTable()
					&& !dialog_.getFieldList().get(i).isVirtualField()
					&& !dialog_.getFieldList().get(i).getBasicType().equals("BYTEA")) {
				if (!firstField) {
					buf.append(",");
				}
				buf.append(dialog_.getFieldList().get(i).getFieldID());
				firstField = false;
			}
		}
		if (!updateCounterID.equals("")) {
			buf.append(",");
			buf.append(updateCounterID);
		}

		buf.append(" from ");
		buf.append(tableID);

		buf.append(" where ") ;

		int orderOfFieldInKey = 0;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (orderOfFieldInKey > 0) {
					buf.append(" and ") ;
				}
				buf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				buf.append("=") ;
				buf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(i).getBasicType(), dialog_.getFieldList().get(i).getInternalValue(), dbName)) ;
				orderOfFieldInKey++;
			}
		}

		return buf.toString();
	}
	
	public String getTableID(){
		return tableID;
	}
	
	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}
	
	public ArrayList<String> getKeyFieldList(){
		return keyFieldList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF290_ReferTable referTable;
		org.w3c.dom.Element workElement;
		if (this.getTableID().equals(tableID) && this.getTableID().equals(tableAlias)) {
			NodeList nodeList = tableElement.getElementsByTagName("Field");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("ID").equals(fieldID)) {
					isValid = true;
					break;
				}
			}
		} else {
			for (int i = 0; i < dialog_.getReferTableList().size(); i++) {
				referTable = dialog_.getReferTableList().get(i);
				if (referTable.getTableID().equals(tableID) && referTable.getTableAlias().equals(tableAlias)) {
					for (int j = 0; j < referTable.getFieldIDList().size(); j++) {
						if (referTable.getFieldIDList().get(j).equals(fieldID)) {
							isValid = true;
							break;
						}
					}
				}
				if (isValid) {
					break;
				}
			}
		}
		return isValid;
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
		for (int i = 0; i < validScriptList.size(); i++) {
			dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
		}
	}
}