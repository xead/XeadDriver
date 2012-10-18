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
import javax.script.ScriptContext;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.w3c.dom.*;
import com.lowagie.text.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

public class XF390 extends Component implements XFExecutable, XFScriptable {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element functionElement_ = null;
	private HashMap<String, Object> parmMap_ = null;
	private HashMap<String, Object> returnMap_ = new HashMap<String, Object>();
	private XF390_HeaderTable headerTable_ = null;
	private XF390_DetailTable detailTable_ = null;
	private String tableFontID = "";
	private int tableFontSize = 12;
	private int tableRowNoWidth = 0;
	private Session session_ = null;
	private boolean instanceIsAvailable_ = true;
	private boolean isToBeCanceled = false;
	private int programSequence;
	private StringBuffer processLog = new StringBuffer();
	private ArrayList<XF390_Phrase> headerPhraseList = new ArrayList<XF390_Phrase>();
	private ArrayList<XF390_Phrase> paragraphList = new ArrayList<XF390_Phrase>();
	private ArrayList<XF390_HeaderReferTable> headerReferTableList = new ArrayList<XF390_HeaderReferTable>();
	private ArrayList<XF390_DetailReferTable> detailReferTableList = new ArrayList<XF390_DetailReferTable>();
	private NodeList detailReferElementList = null;
	private ArrayList<XF390_HeaderField> headerFieldList = new ArrayList<XF390_HeaderField>();
	private ArrayList<XF390_DetailColumn> detailColumnList = new ArrayList<XF390_DetailColumn>();
	private ScriptEngine scriptEngine;
	private Bindings engineScriptBindings;
	private String scriptNameRunning = "";
	private ByteArrayOutputStream exceptionLog;
	private PrintStream exceptionStream;
	private String exceptionHeader = "";
	private SortableDomElementListModel sortingList1;
	private SortableDomElementListModel sortingList2;
	private ArrayList<WorkingRow> workingRowList = new ArrayList<WorkingRow>();

	public XF390(Session session, int instanceArrayIndex) {
		super();
		try {
			session_ = session;
			//
			initComponentsAndVariants();
			//
		} catch(Exception e) {
			e.printStackTrace(exceptionStream);
		}
	}

	void initComponentsAndVariants() throws Exception {
	}

	public boolean isAvailable() {
		return instanceIsAvailable_;
	}

	public HashMap<String, Object> execute(org.w3c.dom.Element functionElement, HashMap<String, Object> parmMap) {
		SortableDomElementListModel sortedList;
		String workAlias, workTableID, workFieldID, workStr;
		StringTokenizer workTokenizer;
		org.w3c.dom.Element workElement;

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
			isToBeCanceled = false;
			exceptionLog = new ByteArrayOutputStream();
			exceptionStream = new PrintStream(exceptionLog);
			exceptionHeader = "";
			functionElement_ = functionElement;
			processLog.delete(0, processLog.length());
			programSequence = session_.writeLogOfFunctionStarted(functionElement_.getAttribute("ID"), functionElement_.getAttribute("Name"));

			//////////////////////////////////////
			// Setup Script Engine and Bindings //
			//////////////////////////////////////
			scriptEngine = session_.getScriptEngineManager().getEngineByName("js");
			engineScriptBindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
			engineScriptBindings.clear();
			engineScriptBindings.put("instance", (XFScriptable)this);
			
			//////////////////////////////////////////////
			// Setup the primary table and refer tables //
			//////////////////////////////////////////////
			headerTable_ = new XF390_HeaderTable(functionElement_, this);
			headerReferTableList.clear();
			NodeList referNodeList = headerTable_.getTableElement().getElementsByTagName("Refer");
			sortedList = XFUtility.getSortedListModel(referNodeList, "Order");
			for (int i = 0; i < sortedList.getSize(); i++) {
				org.w3c.dom.Element element = (org.w3c.dom.Element)sortedList.getElementAt(i);
				headerReferTableList.add(new XF390_HeaderReferTable(element, this));
			}

			/////////////////////////////////
			// Setup phrase and field List //
			/////////////////////////////////
			XF390_Phrase phrase;
			headerPhraseList.clear();
			headerFieldList.clear();
			paragraphList.clear();
			NodeList nodeList = functionElement_.getElementsByTagName("HeaderPhrase");
			SortableDomElementListModel sortableList = XFUtility.getSortedListModel(nodeList, "Order");
			for (int i = 0; i < sortableList.getSize(); i++) {
				phrase = new XF390_Phrase((org.w3c.dom.Element)sortableList.getElementAt(i));
				if (phrase.getBlock().equals("HEADER")) {
					headerPhraseList.add(phrase);
				} else {
					paragraphList.add(phrase);
				}
			}
			//
			// Add fields on phrases if they are not on the column list //
			for (int i = 0; i < headerPhraseList.size(); i++) {
				for (int j = 0; j < headerPhraseList.get(i).getDataSourceNameList().size(); j++) {
					if (!existsInFieldList(headerPhraseList.get(i).getDataSourceNameList().get(j))) {
						headerFieldList.add(new XF390_HeaderField(headerPhraseList.get(i).getDataSourceNameList().get(j), this));
					}
				}
			}
			for (int i = 0; i < paragraphList.size(); i++) {
				for (int j = 0; j < paragraphList.get(i).getDataSourceNameList().size(); j++) {
					if (!existsInFieldList(paragraphList.get(i).getDataSourceNameList().get(j))) {
						headerFieldList.add(new XF390_HeaderField(paragraphList.get(i).getDataSourceNameList().get(j), this));
					}
				}
			}
			//
			// Add primary table key fields if they are not on the column list //
			for (int i = 0; i < headerTable_.getKeyFieldList().size(); i++) {
				if (!existsInFieldList(headerTable_.getTableID(), "", headerTable_.getKeyFieldList().get(i))) {
					headerFieldList.add(new XF390_HeaderField(headerTable_.getTableID(), "", headerTable_.getKeyFieldList().get(i), this));
				}
			}
			//
			// Analyze fields in script and add them if necessary //
			for (int i = 0; i < headerTable_.getScriptList().size(); i++) {
				if	(headerTable_.getScriptList().get(i).isToBeRunAtEvent("BR", "")
						|| headerTable_.getScriptList().get(i).isToBeRunAtEvent("AR", "")) {
					for (int j = 0; j < headerTable_.getScriptList().get(i).getFieldList().size(); j++) {
						workTokenizer = new StringTokenizer(headerTable_.getScriptList().get(i).getFieldList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workTableID = getTableIDOfTableAlias(workAlias);
						workFieldID = workTokenizer.nextToken();
						if (!existsInFieldList(workTableID, workAlias, workFieldID)) {
							workElement = session_.getFieldElement(workTableID, workFieldID);
							if (workElement == null) {
								String msg = XFUtility.RESOURCE.getString("FunctionError1") + headerTable_.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + headerTable_.getScriptList().get(i).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
								JOptionPane.showMessageDialog(null, msg);
								throw new Exception(msg);
							} else {
								if (headerTable_.isValidDataSource(workTableID, workAlias, workFieldID)) {
									headerFieldList.add(new XF390_HeaderField(workTableID, workAlias, workFieldID, this));
								}
							}
						}
					}
				}
			}
			//
			// Analyze refer tables and add their fields if necessary //
			for (int i = headerReferTableList.size()-1; i > -1; i--) {
				for (int j = 0; j < headerReferTableList.get(i).getFieldIDList().size(); j++) {
					if (existsInFieldList(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j))) {
						headerReferTableList.get(i).setToBeExecuted(true);
						break;
					}
				}
				if (headerReferTableList.get(i).isToBeExecuted()) {
					for (int j = 0; j < headerReferTableList.get(i).getFieldIDList().size(); j++) {
						if (!existsInFieldList(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j))) {
							headerFieldList.add(new XF390_HeaderField(headerReferTableList.get(i).getTableID(), headerReferTableList.get(i).getTableAlias(), headerReferTableList.get(i).getFieldIDList().get(j), this));
						}
					}
					for (int j = 0; j < headerReferTableList.get(i).getWithKeyFieldIDList().size(); j++) {
						workTokenizer = new StringTokenizer(headerReferTableList.get(i).getWithKeyFieldIDList().get(j), "." );
						workAlias = workTokenizer.nextToken();
						workTableID = getTableIDOfTableAlias(workAlias);
						workFieldID = workTokenizer.nextToken();
						if (!existsInFieldList(workTableID, workAlias, workFieldID)) {
							headerFieldList.add(new XF390_HeaderField(workTableID, workAlias, workFieldID, this));
						}
					}
				}
			}

			fetchHeaderTableRecord();

			if (!this.isToBeCanceled) {

				///////////////////////////////////////
				// Setup information of detail table //
				///////////////////////////////////////
				tableFontID = functionElement_.getAttribute("TableFontID");
				tableFontSize = Integer.parseInt(functionElement_.getAttribute("TableFontSize"));
				try {
					tableRowNoWidth = Integer.parseInt(functionElement_.getAttribute("TableRowNoWidth"));
				} catch (Exception e) {
					tableRowNoWidth = 0;
				}
				detailTable_ = new XF390_DetailTable(functionElement_, this);
				detailReferTableList.clear();
				detailReferElementList = detailTable_.getTableElement().getElementsByTagName("Refer");
				sortingList2 = XFUtility.getSortedListModel(detailReferElementList, "Order");
				for (int j = 0; j < sortingList2.getSize(); j++) {
					detailReferTableList.add(new XF390_DetailReferTable((org.w3c.dom.Element)sortingList2.getElementAt(j), this));
				}
				//
				// Add detail column on list //
				detailColumnList.clear();
				NodeList columnFieldList = functionElement_.getElementsByTagName("Column");
				sortingList1 = XFUtility.getSortedListModel(columnFieldList, "Order");
				for (int j = 0; j < sortingList1.getSize(); j++) {
					detailColumnList.add(new XF390_DetailColumn((org.w3c.dom.Element)sortingList1.getElementAt(j), this));
				}
				//
				// Add detail table key fields as HIDDEN column if they are not on column list //
				for (int j = 0; j < detailTable_.getKeyFieldIDList().size(); j++) {
					if (!containsDetailField(detailTable_.getTableID(), "", detailTable_.getKeyFieldIDList().get(j))) {
						detailColumnList.add(new XF390_DetailColumn(detailTable_.getTableID(), "", detailTable_.getKeyFieldIDList().get(j), this));
					}
				}
				//
				// Add order-By fields as HIDDEN column if they are not on the column list //
				for (int j = 0; j < detailTable_.getOrderByFieldIDList().size(); j++) {
					workStr = detailTable_.getOrderByFieldIDList().get(j).replace("(D)", "");
					workStr = workStr.replace("(A)", "");
					workTokenizer = new StringTokenizer(workStr, "." );
					workAlias = workTokenizer.nextToken();
					workTableID = getTableIDOfTableAlias(workAlias);
					workFieldID = workTokenizer.nextToken();
					if (!containsDetailField(workTableID, workAlias, workFieldID)) {
						detailColumnList.add(new XF390_DetailColumn(workTableID, workAlias, workFieldID, this));
					}
				}
				//
				// Analyze fields in script and add them as HIDDEN columns if necessary //
				for (int j = 0; j < detailTable_.getScriptList().size(); j++) {
					if	(detailTable_.getScriptList().get(j).isToBeRunAtEvent("BR", "")
							|| detailTable_.getScriptList().get(j).isToBeRunAtEvent("AR", "")) {
						for (int k = 0; k < detailTable_.getScriptList().get(j).getFieldList().size(); k++) {
							workTokenizer = new StringTokenizer(detailTable_.getScriptList().get(j).getFieldList().get(k), "." );
							workAlias = workTokenizer.nextToken();
							workTableID = getTableIDOfTableAlias(workAlias);
							workFieldID = workTokenizer.nextToken();
							if (!containsDetailField(workTableID, workAlias, workFieldID)) {
								workElement = session_.getFieldElement(workTableID, workFieldID);
								if (workElement == null) {
									String msg = XFUtility.RESOURCE.getString("FunctionError1") + detailTable_.getTableID() + XFUtility.RESOURCE.getString("FunctionError2") + detailTable_.getScriptList().get(j).getName() + XFUtility.RESOURCE.getString("FunctionError3") + workAlias + "_" + workFieldID + XFUtility.RESOURCE.getString("FunctionError4");
									JOptionPane.showMessageDialog(null, msg);
									throw new Exception(msg);
								} else {
									if (detailTable_.isValidDataSource(workTableID, workAlias, workFieldID)) {
										detailColumnList.add(new XF390_DetailColumn(workTableID, workAlias, workFieldID, this));
									}
								}
							}
						}
					}
				}
				//
				// Analyze detail refer tables and add their fields as HIDDEN columns if necessary //
				for (int j = detailReferTableList.size()-1; j > -1; j--) {
					for (int k = 0; k < detailReferTableList.get(j).getFieldIDList().size(); k++) {
						if (containsDetailField(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k))) {
							detailReferTableList.get(j).setToBeExecuted(true);
							break;
						}
					}
					if (detailReferTableList.get(j).isToBeExecuted()) {
						for (int k = 0; k < detailReferTableList.get(j).getFieldIDList().size(); k++) {
							if (!containsDetailField(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k))) {
								detailColumnList.add(new XF390_DetailColumn(detailReferTableList.get(j).getTableID(), detailReferTableList.get(j).getTableAlias(), detailReferTableList.get(j).getFieldIDList().get(k), this));
							}
						}
						for (int k = 0; k < detailReferTableList.get(j).getWithKeyFieldIDList().size(); k++) {
							workTokenizer = new StringTokenizer(detailReferTableList.get(j).getWithKeyFieldIDList().get(k), "." );
							workAlias = workTokenizer.nextToken();
							workTableID = getTableIDOfTableAlias(workAlias);
							workFieldID = workTokenizer.nextToken();
							if (!containsDetailField(workTableID, workAlias, workFieldID)) {
								detailColumnList.add(new XF390_DetailColumn(workTableID, workAlias, workFieldID, this));
							}
						}
					}
				}

				/////////////////////
				// Create PDF file //
				/////////////////////
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

	void setErrorAndCloseFunction() {
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
		isToBeCanceled = true;
	}
	
	public void cancelWithScriptException(ScriptException e, String scriptName) {
		JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError7") + scriptName + XFUtility.RESOURCE.getString("FunctionError8"));
		exceptionHeader = "'" + scriptName + "' Script error\n";
		e.printStackTrace(exceptionStream);
		this.rollback();
		setErrorAndCloseFunction();
	}
	
	public void cancelWithException(Exception e) {
		JOptionPane.showMessageDialog(this, XFUtility.RESOURCE.getString("FunctionError5") + "\n" + e.getMessage());
		e.printStackTrace(exceptionStream);
		this.rollback();
		setErrorAndCloseFunction();
	}

	public void callFunction(String functionID) {
		try {
			returnMap_ = session_.executeFunction(functionID, parmMap_);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			exceptionHeader = e.getMessage();
			setErrorAndCloseFunction();
		}
	}
	
	public void startProgress(int maxValue) {
	}
	
	public void incrementProgress() {
	}
	
	public void commit() {
		session_.commit(true, processLog);
	}
	
	public void rollback() {
		session_.commit(false, processLog);
	}

	URI createPDFFileAndGetURI() {
		File pdfFile = null;
		String pdfFileName = "";
		FileOutputStream fileOutputStream = null;
		//
		//Setup margins//
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
		//
		//Generate PDF file and PDF writer//
		com.lowagie.text.Rectangle pageSize = XFUtility.getPageSize(functionElement_.getAttribute("PageSize"), functionElement_.getAttribute("Direction"));
		com.lowagie.text.Document pdfDoc = new com.lowagie.text.Document(pageSize, leftMargin, rightMargin, topMargin, bottomMargin); 
		//
		try {
			pdfFile = session_.createTempFile(functionElement_.getAttribute("ID"), ".pdf");
			pdfFileName = pdfFile.getPath();
			returnMap_.put("FILE_NAME", pdfFileName);
			fileOutputStream = new FileOutputStream(pdfFileName);
			PdfWriter writer = PdfWriter.getInstance(pdfDoc, fileOutputStream);
			writer.setStrictImageSequence(true);
			//
			//Set document attributes//
			pdfDoc.addTitle(functionElement_.getAttribute("Name"));
			pdfDoc.addAuthor(session_.getUserName()); 
			pdfDoc.addCreator("XEAD Driver");
			//
			//Add header to the document//
			addHeaderToDocument(pdfDoc);
			//
			//Add page-number-footer to the document//
			if (functionElement_.getAttribute("WithPageNumber").equals("T")) {
				HeaderFooter footer = new HeaderFooter(
						new Phrase("--"), new Phrase("--"));
				footer.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
				footer.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
				pdfDoc.setFooter(footer);
			}
			//
			//Prepare document//
			pdfDoc.open();
			PdfContentByte cb = writer.getDirectContent();
			//
			//Add Paragraph block//
			addParagraphToDocument(pdfDoc, "PARAGRAPH", cb);
			//
			//Add Table block//
			addTableToDocument(pdfDoc, cb);
			//
			//Add Footer block//
			addParagraphToDocument(pdfDoc, "FOOTER", cb);
			//
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
	
	private void addParagraphToDocument(com.lowagie.text.Document pdfDoc, String blockType, PdfContentByte cb) throws IOException, DocumentException {
		com.lowagie.text.Font font, chunkFont;
		Chunk chunk;
		Phrase phrase;
		Paragraph paragraph = null;
		String keyword = "";
		//
		for (int i = 0; i < paragraphList.size(); i++) {
			//
			if (paragraphList.get(i).getBlock().equals(blockType)) {
				//
				paragraph = new Paragraph(new Phrase(""));
				paragraph.setAlignment(paragraphList.get(i).getAlignment());
				paragraph.setIndentationRight(paragraphList.get(i).getMarginRight());
				paragraph.setIndentationLeft(paragraphList.get(i).getMarginLeft());
				paragraph.setSpacingAfter(paragraphList.get(i).getSpacingAfter());
				//
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
	}
	
	private void addTableToDocument(com.lowagie.text.Document pdfDoc, PdfContentByte cb) throws IOException, DocumentException {
		com.lowagie.text.Font fontTableCell, fontTableCellData;
		Chunk chunk;
		Phrase phrase;
		HashMap<String, Object> keyMap;
		ArrayList<String> columnValueList;
		ArrayList<String> orderByValueList;
		String wrkStr;
		int wrkInt;
		XFTableOperator operatorDetail, operatorRefer;
		String imageFileName = "";
		//
		try {
			//
			fontTableCell = new com.lowagie.text.Font(session_.getBaseFontWithID(tableFontID), tableFontSize, com.lowagie.text.Font.BOLD);
			//
			int totalWidth = 0;
			int width[] = null;
			int columnCount = 0;
			for (int i = 0; i < detailColumnList.size(); i++) {
				if (detailColumnList.get(i).isVisibleColumn()) {
					columnCount++;
				}
			}
			//
			Table table = new Table(columnCount + 1);
			if (tableRowNoWidth == 0) {
				table = new Table(columnCount);
				width = new int[columnCount];
			} else {
				width = new int[columnCount + 1];
				totalWidth = tableRowNoWidth;
				width[0] = tableRowNoWidth;
				Cell cell = new Cell(new Phrase("No", fontTableCell));
				cell.setBackgroundColor(Color.lightGray);
				cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
				table.addCell(cell);
			}
			table.setAlignment(com.lowagie.text.Element.ALIGN_LEFT);
			//
			wrkInt = -1;
			for (int i = 0; i < detailColumnList.size(); i++) {
				if (detailColumnList.get(i).isVisibleColumn()) {
					wrkInt++;
					if (tableRowNoWidth == 0) {
						width[wrkInt] = detailColumnList.get(i).getWidth();
					} else {
						width[wrkInt+1] = detailColumnList.get(i).getWidth();
					}
					totalWidth = totalWidth + detailColumnList.get(i).getWidth();
					Cell cell = new Cell(new Phrase(detailColumnList.get(i).getCaption(), fontTableCell));
					cell.setBackgroundColor(Color.lightGray);
					cell.setHorizontalAlignment(detailColumnList.get(i).getAlignment());
					table.addCell(cell);
				}
			}
			table.setWidths(width);
			table.setWidth(totalWidth);
			//
			table.setBorderWidth(1);
			table.setBorderColor(new Color(0, 0, 0));
			table.setPadding(2);
			table.setSpacing(0);
			//
			Cell cell;
			workingRowList.clear();
			int countOfRows = 0;
			int rowNo = 0;
			//
			operatorDetail = createTableOperator(detailTable_.getSQLToSelect());
			while (operatorDetail.next()) {
				//
				detailTable_.runScript("BR", ""); /* Script to be run BEFORE READ */
				//
				for (int i = 0; i < detailColumnList.size(); i++) {
					detailColumnList.get(i).initialize();
					if (detailColumnList.get(i).getTableID().equals(detailTable_.getTableID())) {
						detailColumnList.get(i).setValueOfResultSet(operatorDetail);
					}
				}
				//
				detailTable_.runScript("AR", "BR()"); /* Script to be run AFTER READ primary table */
				//
				for (int i = 0; i < detailReferTableList.size(); i++) {
					if (detailReferTableList.get(i).isToBeExecuted()) {
						detailTable_.runScript("AR", "BR(" + detailReferTableList.get(i).getTableAlias() + ")"); /* Script to be run BEFORE READ */
						operatorRefer = createTableOperator(detailReferTableList.get(i).getSelectSQL());
						while (operatorRefer.next()) {
							if (detailReferTableList.get(i).isRecordToBeSelected(operatorRefer)) {
								for (int j = 0; j < detailColumnList.size(); j++) {
									if (detailColumnList.get(j).getTableAlias().equals(detailReferTableList.get(i).getTableAlias())) {
										detailColumnList.get(j).setValueOfResultSet(operatorRefer);
									}
								}
								detailTable_.runScript("AR", "AR(" + detailReferTableList.get(i).getTableAlias() + ")"); /* Script to be run AFTER READ */
							}
						}
					}
				}
				//
				detailTable_.runScript("AR", "AR()"); /* Script to be run AFTER READ */
				//
				keyMap = new HashMap<String, Object>();
				for (int i = 0; i < detailTable_.getKeyFieldIDList().size(); i++) {
					keyMap.put(detailTable_.getKeyFieldIDList().get(i), operatorDetail.getValueOf(detailTable_.getKeyFieldIDList().get(i)));
				}
				//
				if (detailTable_.hasOrderByAsItsOwnFields()) {
					//
					if (tableRowNoWidth > 0) {
						rowNo++;
						fontTableCellData = new com.lowagie.text.Font(session_.getBaseFontWithID(tableFontID), tableFontSize, com.lowagie.text.Font.NORMAL);
						cell = new Cell(new Phrase("" + rowNo, fontTableCellData));
						cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
						if (countOfRows % 2 != 0) {
							cell.setBackgroundColor(XFUtility.ODD_ROW_COLOR);
						}
						table.addCell(cell);
					}
					//
					for (int i = 0; i < detailColumnList.size(); i++) {
						if (detailColumnList.get(i).isVisibleColumn()) {
							fontTableCellData = new com.lowagie.text.Font(session_.getBaseFontWithID(tableFontID), tableFontSize, com.lowagie.text.Font.NORMAL);
							if (detailColumnList.get(i).getBarcodeType().equals("")) {
								if (detailColumnList.get(i).isImage()) {
									imageFileName = session_.getImageFileFolder() + detailColumnList.get(i).getExternalValue().toString();
									if (imageFileName.startsWith("http://")) {
										imageFileName = imageFileName.replace("\\", "/");
									}
									try {
										cell = new Cell(com.lowagie.text.Image.getInstance(imageFileName));
									} catch (Exception e) {
										cell = new Cell(detailColumnList.get(i).getExternalValue().toString());
									}
								} else {
									fontTableCellData.setColor(detailColumnList.get(i).getForeground());
									cell = new Cell(new Phrase(detailColumnList.get(i).getExternalValue().toString(), fontTableCellData));
								}
								cell.setHorizontalAlignment(detailColumnList.get(i).getAlignment());
								if (countOfRows % 2 != 0) {
									cell.setBackgroundColor(XFUtility.ODD_ROW_COLOR);
								}
								table.addCell(cell);
							} else {
								chunk = XFUtility.getBarcodeChunkOfValue(detailColumnList.get(i).getExternalValue().toString(), detailColumnList.get(i).getBarcodeType(), cb);
								phrase = new Phrase("", fontTableCellData);
								phrase.add(chunk);
								cell = new Cell(phrase);
								cell.setHorizontalAlignment(detailColumnList.get(i).getAlignment());
								table.addCell(cell);
							}
							if (detailColumnList.get(i).isWithTotal()) {
								detailColumnList.get(i).summarize();
							}
						}
					}
				} else {
					columnValueList = new ArrayList<String>();
					for (int i = 0; i < detailColumnList.size(); i++) {
						if (detailColumnList.get(i).isVisibleColumn()) {
							columnValueList.add(detailColumnList.get(i).getExternalValue().toString());
						}
					}
					orderByValueList = new ArrayList<String>();
					for (int i = 0; i < detailTable_.getOrderByFieldIDList().size(); i++) {
						wrkStr = detailTable_.getOrderByFieldIDList().get(i).replace("(D)", "");
						wrkStr = wrkStr.replace("(A)", "");
						for (int j = 0; j < detailColumnList.size(); j++) {
							if (detailColumnList.get(j).getDataSourceName().equals(wrkStr)) {
								orderByValueList.add(detailColumnList.get(j).getExternalValue().toString());
								break;
							}
						}
					}
					workingRowList.add(new WorkingRow(columnValueList, keyMap, orderByValueList));
				}
				//
				countOfRows++;
			}
			//
			if (!detailTable_.hasOrderByAsItsOwnFields()) {
				WorkingRow[] workingRowArray = workingRowList.toArray(new WorkingRow[0]);
				Arrays.sort(workingRowArray, new WorkingRowComparator());
				for (int i = 0; i < workingRowArray.length; i++) {
					//
					if (tableRowNoWidth > 0) {
						rowNo = i+1;
						fontTableCellData = new com.lowagie.text.Font(session_.getBaseFontWithID(tableFontID), tableFontSize, com.lowagie.text.Font.NORMAL);
						cell = new Cell(new Phrase("" + rowNo, fontTableCellData));
						cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_CENTER);
						if (i % 2 != 0) {
							cell.setBackgroundColor(XFUtility.ODD_ROW_COLOR);
						}
						table.addCell(cell);
					}
					//
					for (int j = 0; j < detailColumnList.size(); j++) {
						if (detailColumnList.get(j).isVisibleColumn()) {
							fontTableCellData = new com.lowagie.text.Font(session_.getBaseFontWithID(tableFontID), tableFontSize, com.lowagie.text.Font.NORMAL);
							if (detailColumnList.get(j).getBarcodeType().equals("")) {
								if (detailColumnList.get(j).isImage()) {
									imageFileName = session_.getImageFileFolder() + detailColumnList.get(j).getExternalValue().toString();
									if (imageFileName.startsWith("http://")) {
										imageFileName = imageFileName.replace("\\", "/");
									}
									cell = new Cell(com.lowagie.text.Image.getInstance(imageFileName));
								} else {
									fontTableCellData.setColor(detailColumnList.get(j).getForeground());
									cell = new Cell(new Phrase(workingRowArray[i].getColumnValueList().get(j), fontTableCellData));
								}
								cell.setHorizontalAlignment(detailColumnList.get(j).getAlignment());
								if (i % 2 != 0) {
									cell.setBackgroundColor(XFUtility.ODD_ROW_COLOR);
								}
								table.addCell(cell);
							} else {
								chunk = XFUtility.getBarcodeChunkOfValue(workingRowArray[i].getColumnValueList().get(j), detailColumnList.get(j).getBarcodeType(), cb);
								phrase = new Phrase("", fontTableCellData);
								phrase.add(chunk);
								cell = new Cell(phrase);
								cell.setHorizontalAlignment(detailColumnList.get(j).getAlignment());
								table.addCell(cell);
							}
							if (detailColumnList.get(i).isWithTotal()) {
								detailColumnList.get(i).summarize();
							}
						}
					}
				}
			}
			//
			boolean isWithTotalHeading = false;
			wrkInt = 0;
			for (int i = 0; i < detailColumnList.size(); i++) {
				if (detailColumnList.get(i).isVisibleColumn()) {
					fontTableCellData = new com.lowagie.text.Font(session_.getBaseFontWithID(tableFontID), tableFontSize, com.lowagie.text.Font.NORMAL);
					wrkInt++;
					if (detailColumnList.get(i).isWithTotal()) {
						if (!isWithTotalHeading) {
							cell = new Cell(new Phrase(XFUtility.RESOURCE.getString("Total"), fontTableCell));
							cell.setColspan(wrkInt);
							cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
							table.addCell(cell);
							isWithTotalHeading = true;
						}
						cell = new Cell(new Phrase(detailColumnList.get(i).getSummary().toString(), fontTableCellData));
						cell.setHorizontalAlignment(detailColumnList.get(i).getAlignment());
						table.addCell(cell);
					} else {
						if (isWithTotalHeading) {
							cell = new Cell(new Phrase(" ", fontTableCellData));
							table.addCell(cell);
						}
					}
				}
			}
			//
			pdfDoc.add(table);
			//
		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		}
	}

	private Chunk getChunkForKeyword(String keyword, PdfContentByte cb, com.lowagie.text.Font chunkFont) {
		StringTokenizer workTokenizer;
		String wrkStr = "";
		com.lowagie.text.Image image;
		int pos, x, y, w, h;
		Chunk chunk = null;
		//
		try {
			//
			if (keyword.contains("&Text(")) {
				pos = keyword.lastIndexOf(")");
				chunk = new Chunk(keyword.substring(6, pos));
			}
			//
			if (keyword.contains("&DataSource(")) {
				pos = keyword.lastIndexOf(")");
				wrkStr = getExternalStringValueOfFieldByName(keyword.substring(12, pos));
				if (wrkStr.equals("")) {
					wrkStr = " ";
				}
				chunkFont.setColor(getColorOfDataSource(keyword.substring(12, pos)));
				chunk = new Chunk(wrkStr, chunkFont);
			}
			//
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
				if (image != null) {
					chunk = new Chunk(image, x, y, true);
				}
			}
			//
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
			//
			if (keyword.contains("&SystemVariant(")) {
				pos = keyword.lastIndexOf(")");
				chunk = new Chunk(session_.getSystemVariantString(keyword.substring(15, pos)));
			}
			//
			if (keyword.contains("&DateTime(")) {
				pos = keyword.lastIndexOf(")");
				chunk = new Chunk(session_.getTodayTime(keyword.substring(10, pos)));
			}
			//
			if (keyword.contains("&Date(")) {
				pos = keyword.lastIndexOf(")");
				chunk = new Chunk(session_.getToday(keyword.substring(6, pos)));
			}
			//
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
		//
		return chunk;
	}
	
	void fetchHeaderTableRecord() {
		try {
			for (int i = 0; i < headerFieldList.size(); i++) {
				if (parmMap_.containsKey(headerFieldList.get(i).getFieldID())) {
					headerFieldList.get(i).setValue(parmMap_.get(headerFieldList.get(i).getFieldID()));
				} else {
					headerFieldList.get(i).setValue(headerFieldList.get(i).getNullValue());
				}
			}
			//
			headerTable_.runScript("BR", "");
			//
			XFTableOperator operatorHeader = createTableOperator(headerTable_.getSQLToSelect());
			if (operatorHeader.next()) {
				//
				for (int i = 0; i < headerFieldList.size(); i++) {
					if (headerFieldList.get(i).getTableID().equals(headerTable_.getTableID())) {
						headerFieldList.get(i).setValueOfResultSet(operatorHeader);
					}
				}
				//
				fetchReferTableRecords("AR", "");
				//
			} else {
				JOptionPane.showMessageDialog(null, XFUtility.RESOURCE.getString("FunctionError30"));
				returnMap_.put("RETURN_CODE", "01");
				isToBeCanceled = true;
			}
			//
		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		}
	}

	protected void fetchReferTableRecords(String event, String specificReferTable) {
		XFTableOperator operator;
		//
		try {
			headerTable_.runScript(event, "BR()");
			//
			for (int i = 0; i < headerReferTableList.size(); i++) {
				if (specificReferTable.equals("") || specificReferTable.equals(headerReferTableList.get(i).getTableAlias())) {
					if (headerReferTableList.get(i).isToBeExecuted()) {
						headerTable_.runScript(event, "BR(" + headerReferTableList.get(i).getTableAlias() + ")");
						if (!headerReferTableList.get(i).isKeyNullable() || !headerReferTableList.get(i).isKeyNull()) {
							operator = createTableOperator(headerReferTableList.get(i).getSelectSQL());
							while (operator.next()) {
								if (headerReferTableList.get(i).isRecordToBeSelected(operator)) {
									for (int j = 0; j < headerFieldList.size(); j++) {
										if (headerFieldList.get(j).getTableAlias().equals(headerReferTableList.get(i).getTableAlias())) {
											headerFieldList.get(j).setValueOfResultSet(operator);
										}
									}
									headerTable_.runScript(event, "AR(" + headerReferTableList.get(i).getTableAlias() + ")");
								}
							}
						}
					}
				}
			}
			//
			headerTable_.runScript(event, "AR()");
			//
		} catch(ScriptException e) {
			cancelWithScriptException(e, this.getScriptNameRunning());
		} catch (Exception e) {
			cancelWithException(e);
		}
	}

	public String getFunctionID() {
		return functionElement_.getAttribute("ID");
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

	public Bindings getEngineScriptBindings() {
		return 	engineScriptBindings;
	}
	
	public void evalScript(String scriptName, String scriptText) throws ScriptException {
		if (!scriptText.equals("")) {
			scriptNameRunning = scriptName;
			StringBuffer bf = new StringBuffer();
			bf.append(scriptText);
			bf.append(session_.getScriptFunctions());
			scriptEngine.eval(bf.toString());
		}
	}

	public XF390_HeaderTable getHeaderTable() {
		return headerTable_;
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

	public XFTableOperator createTableOperator(String oparation, String tableID) {
		return new XFTableOperator(session_, processLog, oparation, tableID);
	}

	public XFTableOperator createTableOperator(String sqlText) {
		return new XFTableOperator(session_, processLog, sqlText);
	}

	public HashMap<String, Object> getReturnMap() {
		return returnMap_;
	}

	public XF390_DetailTable getDetailTable() {
		return detailTable_;
	}

	public ArrayList<XF390_DetailColumn> getDetailColumnList() {
		return detailColumnList;
	}

	public XF390_DetailColumn getDetailColumnObjectByID(String tableID, String tableAlias, String fieldID) {
		XF390_DetailColumn detailColumnField = null;
		for (int i = 0; i < detailColumnList.size(); i++) {
			if (tableID.equals("")) {
				if (detailColumnList.get(i).getTableAlias().equals(tableAlias) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID) && detailColumnList.get(i).getTableAlias().equals(tableAlias) && detailColumnList.get(i).getFieldID().equals(fieldID)) {
					detailColumnField = detailColumnList.get(i);
					break;
				}
			}
		}
		return detailColumnField;
	}

	public ArrayList<XF390_DetailReferTable> getDetailReferTableList() {
		return detailReferTableList;
	}

	public ArrayList<XF390_HeaderField> getFieldList() {
		return headerFieldList;
	}

	public XF390_HeaderField getFieldObjectByID(String tableID, String tableAlias, String fieldID) {
		XF390_HeaderField field = null;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (tableID.equals("")) {
				if (headerFieldList.get(i).getTableAlias().equals(tableAlias) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					field = headerFieldList.get(i);
					break;
				}
			}
			if (tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					field = headerFieldList.get(i);
					break;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID) && headerFieldList.get(i).getTableAlias().equals(tableAlias) && headerFieldList.get(i).getFieldID().equals(fieldID)) {
					field = headerFieldList.get(i);
					break;
				}
			}
		}
		return field;
	}

	public ArrayList<String> getKeyFieldList() {
		return headerTable_.getKeyFieldList();
	}

	public ArrayList<XF390_HeaderReferTable> getHeaderReferTableList() {
		return headerReferTableList;
	}

	public boolean existsInFieldList(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (tableID.equals("")) {
				if (headerFieldList.get(i).getTableAlias().equals(tableAlias)
						&& headerFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID)
						&& headerFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (headerFieldList.get(i).getTableID().equals(tableID)
						&& headerFieldList.get(i).getTableAlias().equals(tableAlias)
						&& headerFieldList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public boolean containsDetailField(String tableID, String tableAlias, String fieldID) {
		boolean result = false;
		for (int i = 0; i < detailColumnList.size(); i++) {
			if (tableID.equals("")) {
				if (detailColumnList.get(i).getTableAlias().equals(tableAlias)
						&& detailColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID)
						&& detailColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
			if (!tableID.equals("") && !tableAlias.equals("")) {
				if (detailColumnList.get(i).getTableID().equals(tableID)
						&& detailColumnList.get(i).getTableAlias().equals(tableAlias)
						&& detailColumnList.get(i).getFieldID().equals(fieldID)) {
					result = true;
				}
			}
		}
		return result;
	}

	public boolean existsInFieldList(String dataSourceName) {
		boolean result = false;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (headerFieldList.get(i).getDataSourceName().equals(dataSourceName)) {
				result = true;
			}
		}
		return result;
	}

	public String getTableIDOfTableAlias(String tableAlias) {
		String tableID = tableAlias;
		XF390_HeaderReferTable referTable;
		org.w3c.dom.Element workElement;
		//
		for (int j = 0; j < headerReferTableList.size(); j++) {
			referTable = headerReferTableList.get(j);
			referTable.getTableAlias();
			if (referTable.getTableAlias().equals(tableAlias)) {
				tableID = referTable.getTableID();
				break;
			}
		}
		//
		if (detailReferElementList != null) {
			for (int j = 0; j < detailReferElementList.getLength(); j++) {
				workElement = (org.w3c.dom.Element)detailReferElementList.item(j);
				if (workElement.getAttribute("TableAlias").equals(tableAlias)) {
					tableID = workElement.getAttribute("ToTable");
					break;
				}
			}
		}
		//
		return tableID;
	}

	public Object getInternalValueOfFieldByName(String dataSourceName) {
		Object obj = null;
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (headerFieldList.get(i).getDataSourceName().equals(dataSourceName)) {
				obj = headerFieldList.get(i).getInternalValue();
				break;
			}
		}
		return obj;
	}

	public String getExternalStringValueOfFieldByName(String dataSourceName) {
		String value = "";
		String wrkStr, basicType, fmt = "";
		//
		StringTokenizer workTokenizer = new StringTokenizer(dataSourceName, ";");
		wrkStr = workTokenizer.nextToken().trim();
		if (workTokenizer.hasMoreTokens()) {
			fmt = workTokenizer.nextToken().trim();
		}
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (headerFieldList.get(i).getDataSourceName().equals(wrkStr)) {
				if (headerFieldList.get(i).isKubunField()
						  || headerFieldList.get(i).getDataTypeOptionList().contains("MSEQ")
						  || headerFieldList.get(i).getDataTypeOptionList().contains("YMONTH")
						  || headerFieldList.get(i).getDataTypeOptionList().contains("FYEAR")) {
					value = headerFieldList.get(i).getExternalValue().toString();
				} else {
					value = headerFieldList.get(i).getInternalValue().toString();
					//
					basicType = headerFieldList.get(i).getBasicType();
					if (basicType.equals("DATE")) {
						if (fmt.equals("")) {
							fmt = session_.getDateFormat();
						}
						value = XFUtility.getUserExpressionOfUtilDate(XFUtility.convertDateFromSqlToUtil(java.sql.Date.valueOf(value)), fmt, false);
					}
					if (basicType.equals("INTEGER")) {
						//value = XFUtility.getEditValueOfInteger(Integer.parseInt(value), fmt, headerFieldList.get(i).getDataSize());
						value = XFUtility.getEditValueOfLong(Long.parseLong(value), fmt, headerFieldList.get(i).getDataSize());
					}
					if (basicType.equals("FLOAT")) {
						//value = XFUtility.getEditValueOfFloat(Float.parseFloat(value), fmt, headerFieldList.get(i).getDecimalSize());
						value = XFUtility.getEditValueOfDouble(Double.parseDouble(value), fmt, headerFieldList.get(i).getDecimalSize());
					}
					if (headerFieldList.get(i).getDataTypeOptionList().contains("YMONTH") || headerFieldList.get(i).getDataTypeOptionList().contains("FYEAR")) {
						if (fmt.equals("")) {
							fmt = session_.getDateFormat();
						}
						value = XFUtility.getUserExpressionOfYearMonth(value.toString(), fmt);
					}
					if (headerFieldList.get(i).getDataTypeOptionList().contains("MSEQ")) {
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
		for (int i = 0; i < headerFieldList.size(); i++) {
			if (headerFieldList.get(i).getDataSourceName().equals(wrkStr)) {
				color = headerFieldList.get(i).getForeground();
				break;
			}
		}
		return color;
	}
	
	 class WorkingRow extends Object {
		private ArrayList<String> columnValueList_ = new ArrayList<String>();
		private ArrayList<String> orderByValueList_ = new ArrayList<String>();
		private HashMap<String, Object> keyMap_ = new HashMap<String, Object>();
		//
		public WorkingRow(ArrayList<String> columnValueList, HashMap<String, Object> keyMap, ArrayList<String> orderByValueList) {
			columnValueList_ = columnValueList;
			keyMap_ = keyMap;
			orderByValueList_ = orderByValueList;
		}
		public HashMap<String, Object> getKeyMap() {
			return keyMap_;
		}
		public ArrayList<String> getColumnValueList() {
			return columnValueList_;
		}
		public ArrayList<String> getOrderByValueList() {
			return orderByValueList_;
		}
	}
		
	 class WorkingRowComparator implements java.util.Comparator<WorkingRow>{
		 public int compare(WorkingRow row1, WorkingRow row2){
			 int compareResult = 0;
			 for (int i = 0; i < row1.getOrderByValueList().size(); i++) {
				 compareResult = row1.getOrderByValueList().get(i).toString().compareTo(row2.getOrderByValueList().get(i).toString());
				if (detailTable_.getOrderByFieldIDList().get(i).contains("(D)")) {
					 compareResult = compareResult * -1;
				 }
				 if (compareResult != 0) {
					 break;
				 }
			 }
			 return compareResult;
		 }
	 }
}

class XF390_HeaderField extends XFColumnScriptable {
	private static final long serialVersionUID = 1L;
	org.w3c.dom.Element tableElement = null;
	private String dataSourceName_ = "";
	private String tableID_ = "";
	private String tableAlias_ = "";
	private String fieldID_ = "";
	private String dataType = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private XFTextField xFTextField = null;
	private XFEditableField component = null;
	private boolean isKubunField = false;
	private boolean isNullable = true;
	private boolean isKey = false;
	private boolean isFieldOnPrimaryTable = false;
	private boolean isVirtualField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private boolean isImage = false;
	private XF390 dialog_;
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private Color foreground = Color.black;

	public XF390_HeaderField(String dataSourceName, XF390 dialog){
		super();
		//
		dataSourceName_ = dataSourceName;
		dialog_ = dialog;
		//
		StringTokenizer workTokenizer = new StringTokenizer(dataSourceName_, ";" );
		dataSourceName_ = workTokenizer.nextToken().trim();
		workTokenizer = new StringTokenizer(dataSourceName_, "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog_.getTableIDOfTableAlias(tableAlias_);
		fieldID_ =workTokenizer.nextToken();
		//
		setupVariants();
		//
		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), this);
	}

	public XF390_HeaderField(String tableID, String tableAlias, String fieldID, XF390 dialog){
		super();
		//
		tableID_ = tableID;
		fieldID_ = fieldID;
		dialog_ = dialog;
		//
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}
		dataSourceName_ = tableAlias_ + "." + fieldID_;
		//
		setupVariants();
		//
		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), this);
	}

	public void setupVariants(){
		//
		if (tableID_.equals(dialog_.getHeaderTable().getTableID()) && tableAlias_.equals(tableID_)) {
			isFieldOnPrimaryTable = true;
			//
			ArrayList<String> keyNameList = dialog_.getKeyFieldList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
		}
		//
		org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
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
		//
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
		//
		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			StringTokenizer workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}
		//
		xFTextField = new XFTextField(this.getBasicType(), dataSize, decimalSize, dataTypeOptions, "");
		xFTextField.setLocation(5, 0);
		component = xFTextField;
	}

	public String getDataSourceName(){
		return dataSourceName_;
	}

	public String getFieldID(){
		return fieldID_;
	}

	public String getFieldIDInScript(){
		return tableAlias_ + "_" + fieldID_;
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

	public boolean isRangeKeyFieldValid(){
		return isRangeKeyFieldValid;
	}

	public boolean isRangeKeyFieldExpire(){
		return isRangeKeyFieldExpire;
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
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					component.setValue(XFUtility.calculateExpireValue(this.getTableElement(), operator, dialog_.getSession(), dialog_.getProcessLog()));
				}
			} else {
				String basicType = this.getBasicType();
				//
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
				//
				if (basicType.equals("FLOAT")) {
					String value = operator.getValueOf(this.getFieldID()).toString();
					if (value == null || value.equals("")) {
						component.setValue("");
					} else {
						component.setValue(Double.parseDouble(value));
					}
				}
				//
				if (basicType.equals("STRING") || basicType.equals("TIME") || basicType.equals("DATETIME")) {
					Object value = operator.getValueOf(this.getFieldID());
					if (value == null) {
						component.setValue("");
					} else {
						component.setValue(value.toString().trim());
					}
				}
				//
				if (basicType.equals("DATE")) {
					component.setValue(operator.getValueOf(this.getFieldID()));
				}
				//
				component.setOldValue(component.getInternalValue());
			}
		} catch (Exception e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public void setValue(Object object){
		XFUtility.setValueToEditableField(this.getBasicType(), object, component);
	}

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public Object getInternalValue(){
		Object returnObj = null;
		String basicType = this.getBasicType();
		//
		if (basicType.equals("INTEGER") || basicType.equals("FLOAT") || basicType.equals("STRING")) {
			String value = (String)component.getInternalValue();
			if (value == null) {
				value = "";
			}
			returnObj = value;
		}
		//
		if (basicType.equals("DATE")) {
			returnObj = (String)component.getInternalValue();
		}
		//
		if (basicType.equals("DATETIME")) {
			returnObj = (String)component.getInternalValue();
		}
		//
		return returnObj;
	}

	public Object getExternalValue() {
		Object returnObj = getInternalValue();
		//
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
		}
		//
		return returnObj;
	}

	public Object getValue() {
		Object returnObj = null;
		//
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
		//
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

class XF390_Phrase extends Object {
	private static final long serialVersionUID = 1L;
	org.w3c.dom.Element phraseElement_ = null;
	private String block = "";
	private String alignment = "";
	private ArrayList<String> valueKeywordList = new ArrayList<String>();
	private ArrayList<String> dataSourceNameList = new ArrayList<String>();
	private String fontID = "";
	private int fontSize = 0;
	private float alignmentMargin;
	private float marginRight;
	private float marginLeft;
	private float spacingAfter;
	private String fontStyle = "";

	public XF390_Phrase(org.w3c.dom.Element phraseElement){
		super();
		//
		phraseElement_ = phraseElement;
		//
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
		//
		try {
			spacingAfter = Float.parseFloat(phraseElement_.getAttribute("SpacingAfter"));
		} catch (NumberFormatException e) {
			spacingAfter = 0f;
		}
		//
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
		//
		fontID = phraseElement_.getAttribute("FontID");
		fontSize = Integer.parseInt(phraseElement_.getAttribute("FontSize"));
		fontStyle = phraseElement_.getAttribute("FontStyle");
	}

	public String getBlock(){
		return block;
	}

	//public float getAlignmentMargin(){
	//	return alignmentMargin;
	//}

	public float getMarginRight(){
		return marginRight;
	}

	public float getMarginLeft(){
		return marginLeft;
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
		//
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
		//
		return result;
	}
}

class XF390_HeaderTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID = "";
	private String activeWhere = "";
	private ArrayList<String> keyFieldList = new ArrayList<String>();
	private ArrayList<String> orderByFieldList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF390 dialog_;
	private StringTokenizer workTokenizer;
	private String updateCounterID = "";

	public XF390_HeaderTable(org.w3c.dom.Element functionElement, XF390 dialog){
		super();
		//
		functionElement_ = functionElement;
		dialog_ = dialog;
		//
		tableID = functionElement_.getAttribute("HeaderTable");
		tableElement = dialog_.getSession().getTableElement(tableID);
		activeWhere = tableElement.getAttribute("ActiveWhere");
		updateCounterID = tableElement.getAttribute("UpdateCounter");
		if (updateCounterID.equals("")) {
			updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
		}
		//
		String wrkStr1;
		org.w3c.dom.Element workElement;
		//
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
		//
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldList.add(workTokenizer.nextToken());
		}
		//
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
		//
		buf.append("select ");
		//
		boolean firstField = true;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isFieldOnPrimaryTable()
			&& !dialog_.getFieldList().get(i).isVirtualField()) {
				if (!firstField) {
					buf.append(",");
				}
				buf.append(dialog_.getFieldList().get(i).getFieldID());
				firstField = false;
			}
		}
		buf.append(",");
		buf.append(updateCounterID);
		//
		buf.append(" from ");
		buf.append(tableID);
		//
		if (orderByFieldList.size() > 0) {
			buf.append(" order by ");
			for (int i = 0; i < orderByFieldList.size(); i++) {
				if (i > 0) {
					buf.append(",");
				}
				buf.append(orderByFieldList.get(i));
			}
		}
		//
		buf.append(" where ") ;
		//
		int orderOfFieldInKey = 0;
		for (int i = 0; i < dialog_.getFieldList().size(); i++) {
			if (dialog_.getFieldList().get(i).isKey()) {
				if (orderOfFieldInKey > 0) {
					buf.append(" and ") ;
				}
				buf.append(dialog_.getFieldList().get(i).getFieldID()) ;
				buf.append("=") ;
				if (XFUtility.isLiteralRequiredBasicType(dialog_.getFieldList().get(i).getBasicType())) {
					buf.append("'") ;
					buf.append(dialog_.getFieldList().get(i).getInternalValue()) ;
					buf.append("'") ;
				} else {
					buf.append(dialog_.getFieldList().get(i).getInternalValue()) ;
				}
				orderOfFieldInKey++;
			}
		}
		//
		if (!activeWhere.equals("")) {
			buf.append(" and (");
			buf.append(activeWhere);
			buf.append(") ");
		}
		//
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
		XF390_HeaderReferTable referTable;
		org.w3c.dom.Element workElement;
		//
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
			for (int i = 0; i < dialog_.getHeaderReferTableList().size(); i++) {
				referTable = dialog_.getHeaderReferTableList().get(i);
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
		//
		return isValid;
	}

	public void runScript(String event1, String event2) throws ScriptException {
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		//
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		//
		for (int i = 0; i < validScriptList.size(); i++) {
			dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
		}
	}
}

class XF390_HeaderReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF390 dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private String activeWhere = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private String rangeKeyFieldSearch = "";
	private boolean rangeValidated;

	public XF390_HeaderReferTable(org.w3c.dom.Element referElement, XF390 dialog){
		super();
		//
		referElement_ = referElement;
		dialog_ = dialog;
		//
		tableID = referElement_.getAttribute("ToTable");
		tableElement = dialog_.getSession().getTableElement(tableID);
		//
		StringTokenizer workTokenizer;
		String wrkStr = tableElement.getAttribute("RangeKey");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			rangeKeyFieldValid =workTokenizer.nextToken();
			rangeKeyFieldExpire =workTokenizer.nextToken();
			org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				rangeKeyType = 1;
			} else {
				rangeKeyType = 2;
			}
		}
		//
		activeWhere = tableElement.getAttribute("ActiveWhere");
		//
		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			fieldIDList.add(workTokenizer.nextToken());
		}
		//
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
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("WithKeyFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withKeyFieldIDList.add(workTokenizer.nextToken());
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldIDList.add(workTokenizer.nextToken());
		}
	}

	public String getSelectSQL(){
		org.w3c.dom.Element workElement;
		int count;
		StringBuffer buf = new StringBuffer();
		//
		buf.append("select ");
		//
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
		if (!rangeKeyFieldValid.equals("")) {
			if (count > 0) {
				buf.append(",");
			}
			buf.append(rangeKeyFieldValid);
			//
			workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				buf.append(",");
				buf.append(rangeKeyFieldExpire);
			}
		}
		//
		buf.append(" from ");
		buf.append(tableID);
		//
		count = 0;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				rangeKeyFieldSearch = withKeyFieldIDList.get(i);
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
						buf.append(XFUtility.getTableOperationValue(dialog_.getFieldList().get(j).getBasicType(), dialog_.getFieldList().get(j).getInternalValue()));
						break;
					}
				}
				count++;
			}
		}
		//
		if (!activeWhere.equals("")) {
			if (count == 0) {
				buf.append(" where ");
			} else {
				buf.append(" and ");
			}
			buf.append(activeWhere);
		}
		//
		if (this.rangeKeyType != 0) {
			buf.append(" order by ");
			buf.append(rangeKeyFieldValid);
			buf.append(" DESC ");
		} else {
			if (orderByFieldIDList.size() > 0) {
				int pos0,pos1;
				buf.append(" order by ");
				for (int i = 0; i < orderByFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					pos0 = orderByFieldIDList.get(i).indexOf(".");
					pos1 = orderByFieldIDList.get(i).indexOf("(A)");
					if (pos1 >= 0) {
						buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
					} else {
						pos1 = orderByFieldIDList.get(i).indexOf("(D)");
						if (pos1 >= 0) {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
							buf.append(" DESC ");
						} else {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, orderByFieldIDList.get(i).length()));
						}
					}
				}
			}
		}
		//
		rangeValidated = false;
		//
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
		//
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
		//
		return isKeyNullable;
	}

	public boolean isKeyNull() {
		boolean isKeyNull = false;
		//
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
		//
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

	public boolean isRecordToBeSelected(XFTableOperator operator) throws Exception {
		boolean returnValue = false;
		//
		if (rangeKeyType == 0) {
			returnValue = true;
		}
		//
		if (rangeKeyType == 1) {
			if (!rangeValidated) { 
				// Note that result set is ordered by rangeKeyFieldValue DESC //
				Object valueKey = dialog_.getInternalValueOfFieldByName((rangeKeyFieldSearch));
				Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
					rangeValidated = true;
				}
			}
		}
		//
		if (rangeKeyType == 2) {
			Object valueKey = dialog_.getInternalValueOfFieldByName((rangeKeyFieldSearch));
			Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
			Object valueThru = operator.getValueOf(rangeKeyFieldExpire);
			if (valueThru == null) {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
				}
			} else {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				int comp2 = valueKey.toString().compareTo(valueThru.toString());
				if (comp1 >= 0 && comp2 < 0) {
					returnValue = true;
				}
			}
		}
		//
		return returnValue;
	}
}

class XF390_DetailTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element tableElement = null;
	private org.w3c.dom.Element functionElement_ = null;
	private String tableID_ = "";
	private String activeWhere = "";
	private String fixedWhere = "";
	private ArrayList<String> keyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private ArrayList<XFScript> scriptList = new ArrayList<XFScript>();
	private XF390 dialog_;
	private StringTokenizer workTokenizer;
	private boolean hasOrderByAsItsOwnFields = true;
	private String updateCounterID = "";

	public XF390_DetailTable(org.w3c.dom.Element functionElement, XF390 dialog){
		super();
		//
		functionElement_ = functionElement;
		dialog_ = dialog;
		//
		tableID_ = functionElement_.getAttribute("DetailTable");
		tableElement = dialog_.getSession().getTableElement(tableID_);
		activeWhere = tableElement.getAttribute("ActiveWhere");
		fixedWhere = functionElement.getAttribute("DetailFixedWhere");
		updateCounterID = tableElement.getAttribute("UpdateCounter");
		if (updateCounterID.equals("")) {
			updateCounterID = XFUtility.DEFAULT_UPDATE_COUNTER;
		}
		//
		int pos1;
		String wrkStr1, wrkStr2;
		org.w3c.dom.Element workElement;
		//
		if (functionElement_.getAttribute("DetailKeyFields").equals("")) {
			NodeList nodeList = tableElement.getElementsByTagName("Key");
			for (int i = 0; i < nodeList.getLength(); i++) {
				workElement = (org.w3c.dom.Element)nodeList.item(i);
				if (workElement.getAttribute("Type").equals("PK")) {
					workTokenizer = new StringTokenizer(workElement.getAttribute("Fields"), ";" );
					while (workTokenizer.hasMoreTokens()) {
						wrkStr1 = workTokenizer.nextToken();
						keyFieldIDList.add(wrkStr1);
					}
					break;
				}
			}
		} else {
			workTokenizer = new StringTokenizer(functionElement_.getAttribute("DetailKeyFields"), ";" );
			while (workTokenizer.hasMoreTokens()) {
				keyFieldIDList.add(workTokenizer.nextToken());
			}
		}
		//
		workTokenizer = new StringTokenizer(functionElement_.getAttribute("DetailOrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			wrkStr1 = workTokenizer.nextToken();
			//
			pos1 = wrkStr1.indexOf(".");
			if (pos1 > -1) { 
				wrkStr2 = wrkStr1.substring(0, pos1);
				if (!wrkStr2.equals(tableID_)) {
					hasOrderByAsItsOwnFields = false;
				}
			}
			//
			orderByFieldIDList.add(wrkStr1);
		}
		//
		org.w3c.dom.Element element;
		NodeList workList = tableElement.getElementsByTagName("Script");
		SortableDomElementListModel sortList = XFUtility.getSortedListModel(workList, "Order");
		for (int i = 0; i < sortList.size(); i++) {
	        element = (org.w3c.dom.Element)sortList.getElementAt(i);
	        scriptList.add(new XFScript(tableID_, element, dialog_.getSession().getTableNodeList()));
		}
	}
	
	public String getSQLToSelect(){
		int count;
		StringBuffer buf = new StringBuffer();
		XF390_HeaderField headerField;
		//
		buf.append("select ");
		//
		count = -1;
		for (int i = 0; i < keyFieldIDList.size(); i++) {
			count++;
			if (count > 0) {
				buf.append(",");
			}
			buf.append(keyFieldIDList.get(i));
		}
		for (int i = 0; i < dialog_.getDetailColumnList().size(); i++) {
			if (dialog_.getDetailColumnList().get(i).getTableID().equals(tableID_) && !dialog_.getDetailColumnList().get(i).isVirtualField()) {
				count++;
				if (count > 0) {
					buf.append(",");
				}
				buf.append(dialog_.getDetailColumnList().get(i).getFieldID());
			}
		}
		if (count > 0) {
			buf.append(",");
		}
		buf.append(updateCounterID);
		//
		buf.append(" from ");
		buf.append(tableID_);
		//
		buf.append(" where ") ;
		count = -1;
		for (int i = 0; i < dialog_.getHeaderTable().getKeyFieldList().size(); i++) {
			count++;
			if (count > 0) {
				buf.append(" and ") ;
			}
			buf.append(keyFieldIDList.get(i)) ;
			buf.append("=") ;
			headerField = dialog_.getFieldObjectByID(dialog_.getHeaderTable().getTableID(), "", dialog_.getHeaderTable().getKeyFieldList().get(i));
			if (XFUtility.isLiteralRequiredBasicType(headerField.getBasicType())) {
				buf.append("'");
				buf.append(dialog_.getParmMap().get(dialog_.getHeaderTable().getKeyFieldList().get(i)));
				buf.append("'");
			} else {
				buf.append(dialog_.getParmMap().get(dialog_.getHeaderTable().getKeyFieldList().get(i)));
			}
		}
		//
		if (!activeWhere.equals("")) {
			buf.append(" and (");
			buf.append(activeWhere);
			buf.append(") ");
		}
		//
		if (!fixedWhere.equals("")) {
			buf.append(" and (");
			buf.append(fixedWhere);
			buf.append(") ");
		}
		//
		if (this.hasOrderByAsItsOwnFields) {
			if (orderByFieldIDList.size() > 0) {
				int pos0,pos1;
				buf.append(" order by ");
				for (int i = 0; i < orderByFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					pos0 = orderByFieldIDList.get(i).indexOf(".");
					pos1 = orderByFieldIDList.get(i).indexOf("(A)");
					if (pos1 >= 0) {
						buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
					} else {
						pos1 = orderByFieldIDList.get(i).indexOf("(D)");
						if (pos1 >= 0) {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
							buf.append(" DESC ");
						} else {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, orderByFieldIDList.get(i).length()));
						}
					}
				}
			} else {
				buf.append(" order by ");
				for (int i = 0; i < keyFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					buf.append(keyFieldIDList.get(i));
				}
			}
		}
		//
		return buf.toString();
	}

//	public Object convertToTableOperationValue(String basicType, Object value){
//		Object returnValue = null;
//		if (basicType.equals("INTEGER")) {
//			//returnValue = Integer.parseInt(value.toString());
//			returnValue = Long.parseLong(value.toString());
//		}
//		if (basicType.equals("FLOAT")) {
//			returnValue = Double.parseDouble(value.toString());
//		}
//		if (basicType.equals("STRING")) {
//			returnValue = "'" + value.toString() + "'";
//		}
//		if (basicType.equals("DATE")) {
//			if (value == null || value.equals("")) {
//				returnValue = "NULL";
//			} else {
//				returnValue = "'" + value.toString() + "'";
//			}
//		}
//		if (basicType.equals("DATETIME")) {
//			String timeDate = value.toString();
//			if (timeDate == null || timeDate.equals("")) {
//				returnValue = "NULL";
//			} else {
//				if (timeDate.equals("CURRENT_TIMESTAMP")) {
//					returnValue = timeDate;
//				} else {
//					timeDate = timeDate.replace("/", "-");
//					returnValue = "'" + timeDate + "'";
//				}
//			}
//		}
//		return returnValue;
//	}

	public String getTableID(){
		return tableID_;
	}
	
	public String getUpdateCounterID(){
		return updateCounterID;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return keyFieldIDList;
	}
	
	public ArrayList<XFScript> getScriptList(){
		return scriptList;
	}

	public org.w3c.dom.Element getTableElement() {
		return tableElement;
	}
	
	public ArrayList<String> getOrderByFieldIDList(){
		return orderByFieldIDList;
	}
	
	public boolean hasOrderByAsItsOwnFields(){
		return hasOrderByAsItsOwnFields;
	}
	
	public boolean isValidDataSource(String tableID, String tableAlias, String fieldID) {
		boolean isValid = false;
		XF390_DetailReferTable referTable;
		org.w3c.dom.Element workElement;
		//
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
			for (int i = 0; i < dialog_.getDetailReferTableList().size(); i++) {
				referTable = dialog_.getDetailReferTableList().get(i);
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
		//
		return isValid;
	}

	public void runScript(String event1, String event2) throws ScriptException {
		XFScript script;
		ArrayList<XFScript> validScriptList = new ArrayList<XFScript>();
		//
		for (int i = 0; i < scriptList.size(); i++) {
			script = scriptList.get(i);
			if (script.isToBeRunAtEvent(event1, event2)) {
				validScriptList.add(script);
			}
		}
		//
		for (int i = 0; i < validScriptList.size(); i++) {
			dialog_.evalScript(validScriptList.get(i).getName(), validScriptList.get(i).getScriptText());
		}
	}
}

class XF390_DetailColumn extends XFColumnScriptable {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element functionColumnElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF390 dialog_ = null;
	private String tableID_ = "";
	private String tableAlias_ = "";
	private String fieldID_ = "";
	private String dataType = "";
	private int dataSize = 5;
	private int decimalSize = 0;
	private String dataTypeOptions = "";
	private ArrayList<String> dataTypeOptionList;
	private String fieldOptions = "";
	private String fieldCaption = "";
	private String alignment = "CENTER";
	private String editCode = "";
	private String barcodeType = "";
	private int fieldWidth = 10;
	private int columnIndex = -1;
	private boolean isKey = false;
	private boolean isFieldOnDetailTable = false;
	private boolean isVisibleColumn = true;
	private boolean isVirtualField = false;
	private boolean isRangeKeyFieldValid = false;
	private boolean isRangeKeyFieldExpire = false;
	private boolean isWithTotal = false;
	private boolean isKubunField = false;
	private boolean isImage = false;
	private ArrayList<String> kubunValueList = new ArrayList<String>();
	private ArrayList<String> kubunTextList = new ArrayList<String>();
	private Object value_ = null;
	private long summaryLong = 0;
	private double summaryDouble = 0d;
	private Color foreground = Color.black;

	public XF390_DetailColumn(org.w3c.dom.Element functionColumnElement, XF390 dialog){
		super();
		//
		String wrkStr;
		functionColumnElement_ = functionColumnElement;
		dialog_ = dialog;
		fieldOptions = functionColumnElement_.getAttribute("FieldOptions");
		//
		alignment = functionColumnElement_.getAttribute("Alignment");
		//
		StringTokenizer workTokenizer = new StringTokenizer(functionColumnElement_.getAttribute("DataSource"), "." );
		tableAlias_ = workTokenizer.nextToken();
		tableID_ = dialog.getTableIDOfTableAlias(tableAlias_);
		fieldID_ =workTokenizer.nextToken();
		//
		if (tableID_.equals(dialog_.getDetailTable().getTableID()) && tableID_.equals(tableAlias_)) {
			isFieldOnDetailTable = true;
			ArrayList<String> keyNameList = dialog_.getDetailTable().getKeyFieldIDList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
		}
		//
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "CAPTION");
		if (!wrkStr.equals("")) {
			fieldCaption = wrkStr;
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		if (!workElement.getAttribute("Decimal").equals("")) {
			decimalSize = Integer.parseInt(workElement.getAttribute("Decimal"));
		}
		//
		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}
		//
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(dataTypeOptions, "KUBUN");
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
		if (dataTypeOptionList.contains("IMAGE")) {
			isImage = true;
		}
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "EDIT_CODE");
		if (!wrkStr.equals("")) {
			editCode = wrkStr;
		}
		//
		wrkStr = XFUtility.getOptionValueWithKeyword(fieldOptions, "BARCODE_TYPE");
		if (!wrkStr.equals("")) {
			barcodeType = wrkStr;
		}
		//
		if (fieldOptions.contains("WITH_TOTAL")) {
			isWithTotal = true;
		}
		//
		try {
			fieldWidth = Integer.parseInt(functionColumnElement_.getAttribute("Width"));
		} catch (NumberFormatException e) {
			fieldWidth = 20;
		}
		//
		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), this);
	}

	public XF390_DetailColumn(String tableID, String tableAlias, String fieldID, XF390 dialog){
		super();
		//
		functionColumnElement_ = null;
		dialog_ = dialog;
		fieldOptions = "";
		//
		isVisibleColumn = false;
		//
		tableID_ = tableID;
		if (tableAlias.equals("")) {
			tableAlias_ = tableID;
		} else {
			tableAlias_ = tableAlias;
		}
		fieldID_ = fieldID;
		//
		if (tableID_.equals(dialog_.getDetailTable().getTableID()) && tableID_.equals(tableAlias_)) {
			isFieldOnDetailTable = true;
			ArrayList<String> keyNameList = dialog_.getDetailTable().getKeyFieldIDList();
			for (int i = 0; i < keyNameList.size(); i++) {
				if (keyNameList.get(i).equals(fieldID_)) {
					isKey = true;
					break;
				}
			}
		}
		//
		org.w3c.dom.Element workElement = dialog.getSession().getFieldElement(tableID_, fieldID_);
		if (workElement == null) {
			JOptionPane.showMessageDialog(null, tableID_ + "." + fieldID_ + XFUtility.RESOURCE.getString("FunctionError11"));
		}
		dataType = workElement.getAttribute("Type");
		dataTypeOptions = workElement.getAttribute("TypeOptions");
		dataTypeOptionList = XFUtility.getOptionList(dataTypeOptions);
		if (workElement.getAttribute("Name").equals("")) {
			fieldCaption = workElement.getAttribute("ID");
		} else {
			fieldCaption = workElement.getAttribute("Name");
		}
		dataSize = Integer.parseInt(workElement.getAttribute("Size"));
		tableElement = (org.w3c.dom.Element)workElement.getParentNode();
		if (!tableElement.getAttribute("RangeKey").equals("")) {
			StringTokenizer workTokenizer = new StringTokenizer(tableElement.getAttribute("RangeKey"), ";" );
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldValid = true;
			}
			if (workTokenizer.nextToken().equals(fieldID_)) {
				isRangeKeyFieldExpire = true;
			}
		}
		//
		if (dataTypeOptionList.contains("VIRTUAL")) {
			isVirtualField = true;
		}
		//
		dialog_.getEngineScriptBindings().put(this.getFieldIDInScript(), this);
	}
	
	public boolean isImage() {
		return isImage;
	}

	public void summarize() {
		String basicType = this.getBasicType();
		if (basicType.equals("INTEGER")) {
			summaryLong = summaryLong + (Long)value_;
		}
		if (basicType.equals("FLOAT")) {
			summaryDouble = summaryDouble + (Double)value_;
		}
	}

	public Object getSummary() {
		Object value = null;
		//
		String basicType = this.getBasicType();
		if (basicType.equals("INTEGER")) {
			value = XFUtility.getEditValueOfLong(summaryLong, editCode, dataSize);
		}
		if (basicType.equals("FLOAT")) {
			value = XFUtility.getEditValueOfDouble(summaryDouble, editCode, decimalSize);
		}
		//
		return value;
	}

	public boolean isVirtualField(){
		return isVirtualField;
	}

	public boolean isRangeKeyFieldValid(){
		return isRangeKeyFieldValid;
	}

	public boolean isRangeKeyFieldExpire(){
		return isRangeKeyFieldExpire;
	}

	public boolean isWithTotal(){
		return isWithTotal;
	}

	public org.w3c.dom.Element getTableElement(){
		return tableElement;
	}

	public String getBasicType(){
		return XFUtility.getBasicTypeOf(dataType);
	}

	public String getTableID(){
		return tableID_;
	}

	public String getFieldID(){
		return fieldID_;
	}

	public String getFieldIDInScript(){
		return tableAlias_ + "_" + fieldID_;
	}

	public String getTableAlias(){
		return tableAlias_;
	}

	public String getDataSourceName(){
		return tableAlias_ + "." + fieldID_;
	}

	public boolean isVisibleColumn(){
		return isVisibleColumn;
	}

	public boolean isKey(){
		return isKey;
	}

	public boolean isFieldOnDetailTable(){
		return isFieldOnDetailTable;
	}

	public Object getInternalValue(){
		return value_;
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

	public Object getExternalValue(){
		Object value = null;
		String basicType = this.getBasicType();
		//
		if (basicType.equals("INTEGER")) {
			if (dataTypeOptionList.contains("YMONTH") || dataTypeOptionList.contains("FYEAR")) {
				if (editCode.equals("")) {
					editCode = dialog_.getSession().getDateFormat();
				}
				value = XFUtility.getUserExpressionOfYearMonth(value_.toString(), editCode);
			} else {
				if (dataTypeOptionList.contains("MSEQ")) {
					value = XFUtility.getUserExpressionOfMSeq(Integer.parseInt(value_.toString()), dialog_.getSession());
				} else {
					value = XFUtility.getEditValueOfLong((Long)value_, editCode, dataSize);
				}
			}
		} else {
			if (basicType.equals("FLOAT")) {
				value = XFUtility.getEditValueOfDouble((Double)value_, editCode, decimalSize);
			} else {
				if (basicType.equals("DATE")) {
					if (value_ == null || value_.equals("")) {
						value = "";
					} else {
						if (editCode.equals("")) {
							editCode = dialog_.getSession().getDateFormat();
						}
						value = XFUtility.getUserExpressionOfUtilDate(XFUtility.convertDateFromSqlToUtil(java.sql.Date.valueOf(value_.toString())), editCode, false);
					}
				} else {
					if (basicType.equals("DATETIME")) {
						if (value_ != null) {
							value = value_.toString().replace("-", "/");
						}
					} else {
						if (this.isKubunField) {
							if (value_ == null || value_.toString().trim().equals("")) {
								value = "";
							} else {
								String wrkStr = value_.toString().trim();
								for (int i = 0; i < kubunValueList.size(); i++) {
									if (kubunValueList.get(i).equals(wrkStr)) {
										value = kubunTextList.get(i);
										break;
									}
								}
							}
						} else {
							if (value_ == null) {
								value = "";
							} else {
								value = value_.toString().trim();
							}
						}
					}
				}
			}
		}
		//
		return value;
	}

	public boolean isNull(){
		return XFUtility.isNullValue(this.getBasicType(), value_);
	}

	public Object getNullValue(){
		return XFUtility.getNullValueOfBasicType(this.getBasicType());
	}

	public String getDataTypeOptions() {
		return dataTypeOptions;
	}

	public String getFieldOptions() {
		return fieldOptions;
	}

	public String getCaption(){
		return fieldCaption;
	}

	public String getBarcodeType(){
		return barcodeType;
	}

	public int getDataSize(){
		return dataSize;
	}

	public int getDecimalSize(){
		return decimalSize;
	}

	public int getWidth(){
		return fieldWidth;
	}

	public int getColumnIndex(){
		return columnIndex;
	}

	public void setColumnIndex(int index){
		columnIndex = index;
	}

	public void setValueOfResultSet(XFTableOperator operator) {
		String basicType = this.getBasicType();
		//
		try {
			if (this.isVirtualField) {
				if (this.isRangeKeyFieldExpire()) {
					value_ = XFUtility.calculateExpireValue(this.getTableElement(), operator, dialog_.getSession(), dialog_.getProcessLog());
				}
			} else {
				//
				Object value = operator.getValueOf(this.getFieldID()); 
				//
				if (basicType.equals("INTEGER")) {
					if (value == null || value.equals("")) {
						value_ = "";
					} else {
						String wrkStr = value.toString();
						int pos = wrkStr.indexOf(".");
						if (pos >= 0) {
							wrkStr = wrkStr.substring(0, pos);
						}
						value_ = Long.parseLong(wrkStr);
					}
				} else {
					if (basicType.equals("FLOAT")) {
						if (value == null || value.equals("")) {
							value_ = "";
						} else {
							value_ = Double.parseDouble(value.toString());
						}
					} else {
						if (value == null) {
							value_ = "";
						} else {
							value_ = value.toString().trim();
						}
					}
				}
			}
			//
		} catch (Exception e) {
			e.printStackTrace(dialog_.getExceptionStream());
			dialog_.setErrorAndCloseFunction();
		}
	}

	public void initialize() {
		foreground = Color.black;
		value_ = this.getNullValue();
	}

	public void setValue(Object value){
		value_ = XFUtility.getValueAccordingToBasicType(this.getBasicType(), value);
	}

	public Object getValue() {
		return getInternalValue();
	}

	public void setOldValue(Object value){
	}

	public Object getOldValue() {
		return getInternalValue();
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

class XF390_DetailReferTable extends Object {
	private static final long serialVersionUID = 1L;
	private org.w3c.dom.Element referElement_ = null;
	private org.w3c.dom.Element tableElement = null;
	private XF390 dialog_ = null;
	private String tableID = "";
	private String tableAlias = "";
	private String activeWhere = "";
	private ArrayList<String> fieldIDList = new ArrayList<String>();
	private ArrayList<String> toKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> withKeyFieldIDList = new ArrayList<String>();
	private ArrayList<String> orderByFieldIDList = new ArrayList<String>();
	private boolean isToBeExecuted = false;
	private int rangeKeyType = 0;
	private String rangeKeyFieldValid = "";
	private String rangeKeyFieldExpire = "";
	private String rangeKeyFieldSearch = "";
	private boolean rangeValidated;

	public XF390_DetailReferTable(org.w3c.dom.Element referElement, XF390 dialog){
		super();
		//
		referElement_ = referElement;
		dialog_ = dialog;
		//
		tableID = referElement_.getAttribute("ToTable");
		tableElement = dialog_.getSession().getTableElement(tableID);
		//
		StringTokenizer workTokenizer;
		String wrkStr = tableElement.getAttribute("RangeKey");
		if (!wrkStr.equals("")) {
			workTokenizer = new StringTokenizer(wrkStr, ";" );
			rangeKeyFieldValid =workTokenizer.nextToken();
			rangeKeyFieldExpire =workTokenizer.nextToken();
			org.w3c.dom.Element workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				rangeKeyType = 1;
			} else {
				rangeKeyType = 2;
			}
		}
		//
		activeWhere = tableElement.getAttribute("ActiveWhere");
		//
		tableAlias = referElement_.getAttribute("TableAlias");
		if (tableAlias.equals("")) {
			tableAlias = tableID;
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("Fields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			fieldIDList.add(workTokenizer.nextToken());
		}
		//
		if (referElement_.getAttribute("ToKeyFields").equals("")) {
			org.w3c.dom.Element workElement = dialog.getSession().getTablePKElement(tableID);
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
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("WithKeyFields"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			withKeyFieldIDList.add(workTokenizer.nextToken());
		}
		//
		workTokenizer = new StringTokenizer(referElement_.getAttribute("OrderBy"), ";" );
		while (workTokenizer.hasMoreTokens()) {
			orderByFieldIDList.add(workTokenizer.nextToken());
		}
	}

	public String getSelectSQL(){
		int count;
		org.w3c.dom.Element workElement;
		StringBuffer buf = new StringBuffer();
		//
		buf.append("select ");
		//
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
		if (!rangeKeyFieldValid.equals("")) {
			if (count > 0) {
				buf.append(",");
			}
			buf.append(rangeKeyFieldValid);
			//
			workElement = dialog_.getSession().getFieldElement(tableID, rangeKeyFieldExpire);
			if (!XFUtility.getOptionList(workElement.getAttribute("TypeOptions")).contains("VIRTUAL")) {
				buf.append(",");
				buf.append(rangeKeyFieldExpire);
			}
		}
		//
		buf.append(" from ");
		buf.append(tableID);
		buf.append(" where ");
		//
		count = 0;
		for (int i = 0; i < toKeyFieldIDList.size(); i++) {
			if (toKeyFieldIDList.get(i).equals(rangeKeyFieldValid)) {
				rangeKeyFieldSearch = withKeyFieldIDList.get(i);
			} else {
				if (count > 0) {
					buf.append(" and ");
				}
				buf.append(toKeyFieldIDList.get(i));
				buf.append("=");
				for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
					if (withKeyFieldIDList.get(i).equals(dialog_.getDetailColumnList().get(j).getDataSourceName())) {
						if (XFUtility.isLiteralRequiredBasicType(dialog_.getDetailColumnList().get(j).getBasicType())) {
							buf.append("'");
							buf.append(dialog_.getDetailColumnList().get(j).getInternalValue());
							buf.append("'");
						} else {
							buf.append(dialog_.getDetailColumnList().get(j).getInternalValue());
						}
						break;
					}
				}
				count++;
			}
		}
		//
		if (!activeWhere.equals("")) {
			buf.append(" and ");
			buf.append(activeWhere);
		}
		//
		if (this.rangeKeyType != 0) {
			buf.append(" order by ");
			buf.append(rangeKeyFieldValid);
			buf.append(" DESC ");
		} else {
			if (orderByFieldIDList.size() > 0) {
				int pos0,pos1;
				buf.append(" order by ");
				for (int i = 0; i < orderByFieldIDList.size(); i++) {
					if (i > 0) {
						buf.append(",");
					}
					pos0 = orderByFieldIDList.get(i).indexOf(".");
					pos1 = orderByFieldIDList.get(i).indexOf("(A)");
					if (pos1 >= 0) {
						buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
					} else {
						pos1 = orderByFieldIDList.get(i).indexOf("(D)");
						if (pos1 >= 0) {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, pos1));
							buf.append(" DESC ");
						} else {
							buf.append(orderByFieldIDList.get(i).substring(pos0+1, orderByFieldIDList.get(i).length()));
						}
					}
				}
			}
		}
		//
		rangeValidated = false;
		//
		return buf.toString();
	}

	public String getTableID(){
		return tableID;
	}

	public String getTableAlias(){
		return tableAlias;
	}
	
	public ArrayList<String> getKeyFieldIDList(){
		return toKeyFieldIDList;
	}
	
	public ArrayList<String> getWithKeyFieldIDList(){
		return withKeyFieldIDList;
	}

	public ArrayList<String> getFieldIDList(){
		return  fieldIDList;
	}
	
	public void setToBeExecuted(boolean executed){
		isToBeExecuted = executed;
	}
	
	public boolean isToBeExecuted(){
		return isToBeExecuted;
	}

	public boolean isRecordToBeSelected(XFTableOperator operator) throws Exception {
		boolean returnValue = false;
		//
		if (rangeKeyType == 0) {
			returnValue = true;
		}
		//
		if (rangeKeyType == 1) {
			// Note that result set is ordered by rangeKeyFieldValue DESC //
			if (!rangeValidated) { 
				StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
				String workTableAlias = workTokenizer.nextToken();
				String workFieldID = workTokenizer.nextToken();
				Object valueKey = dialog_.getDetailColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
				Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
					rangeValidated = true;
				}
			}
		}
		//
		if (rangeKeyType == 2) {
			StringTokenizer workTokenizer = new StringTokenizer(rangeKeyFieldSearch, "." );
			String workTableAlias = workTokenizer.nextToken();
			String workFieldID = workTokenizer.nextToken();
			Object valueKey = dialog_.getDetailColumnObjectByID("", workTableAlias, workFieldID).getInternalValue();
			Object valueFrom = operator.getValueOf(rangeKeyFieldValid);
			Object valueThru = operator.getValueOf(rangeKeyFieldExpire);
			if (valueThru == null) {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				if (comp1 >= 0) {
					returnValue = true;
				}
			} else {
				int comp1 = valueKey.toString().compareTo(valueFrom.toString());
				int comp2 = valueKey.toString().compareTo(valueThru.toString());
				if (comp1 >= 0 && comp2 < 0) {
					returnValue = true;
				}
			}
		}
		//
		return returnValue;
	}

	public void setKeyFieldValues(XFHashMap keyValues){
		for (int i = 0; i < withKeyFieldIDList.size(); i++) {
			for (int j = 0; j < dialog_.getDetailColumnList().size(); j++) {
				if (dialog_.getDetailColumnList().get(j).getDataSourceName().equals(withKeyFieldIDList.get(i))) {
					dialog_.getDetailColumnList().get(j).setValue(keyValues.getValue(toKeyFieldIDList.get(i)));
					break;
				}
			}
		}
	}
}
