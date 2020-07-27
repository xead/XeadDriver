package xeadDriver;

import java.io.PrintStream;
import java.util.HashMap;

import javax.script.ScriptException;

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

public interface XFScriptable {
	public void cancelWithMessage(String message);
	public void cancelWithException(Exception e);
	public void cancelWithScriptException(ScriptException e, String scriptName);
	public void callFunction(String functionID);
	public void commit();
	public void rollback();
	public String getFunctionID();
	public Session getSession();
	public HashMap<String, Object> getParmMap();
	public HashMap<String, Object> getReturnMap();
	public void setProcessLog(String value);
	public StringBuffer getProcessLog();
	public XFTableOperator createTableOperator(String oparation, String tableID);
	public XFTableOperator createTableOperator(String sqlText);
	public XFTableEvaluator createTableEvaluator(String tableID);
	public Object getFieldObjectByID(String tableID, String fieldID);
	public String getUserValueOf(String DataSourceName);
	public void setUserValueOf(String DataSourceName, Object value);
	public boolean isAvailable();
	public Object getVariant(String variantID);
	public void setVariant(String variantID, Object value);
	public PrintStream getExceptionStream();
	public void setErrorAndCloseFunction();
	public void setStatusMessage(String message);
	public void setStatusMessage(String message, boolean isToReplaceLastLine);
	public void executeScript(String scriptText);
}
