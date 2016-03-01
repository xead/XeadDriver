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

//import javax.swing.*;

////////////////////////////////////////////////////////////////////////
// This is a public interface used in Table-Script.                   //
// Note that public interface should be defined in its own java file. //
////////////////////////////////////////////////////////////////////////
public interface XFFieldScriptable {
	public String getFieldID();
	public String getTableID();
	public String getDataSourceID();
	public String getName();
	public Object getValue();
	public void setValue(Object value);
	public Object getOldValue();
	public void setOldValue(Object value);
	public boolean isValueChanged();
	public String getColor();
	public void setColor(String colorName);
	public boolean isEnabled();
	public void setEnabled(boolean enabled);
	public boolean isEditable();
	public void setEditable(boolean isEditable);
	public String getError();
	public void setError(String message);
	public void setValueList(String[] valueList);
	public String[] getValueList();
}
//public class XFFieldScriptable extends JPanel {
//	private static final long serialVersionUID = 1L;
//	public String getFieldID(){
//		return "";
//	}
//	public String getTableID(){
//		return "";
//	}
//	public String getDataSourceID(){
//		return "";
//	}
//	public String getName(){
//		return "";
//	}
//	public Object getValue(){
//		return null;
//	}
//	public void setValue(Object value) {
//	}
//	public Object getOldValue(){
//		return null;
//	}
//	public void setOldValue(Object value) {
//	}
//	public boolean isValueChanged() {
//		return false;
//	}
//	public String getColor() {
//		return "";
//	}
//	public void setColor(String colorName) {
//	}
//	public boolean isEnabled() {
//		return true;
//	}
//	public void setEnabled(boolean enabled) {
//	}
//	public boolean isEditable() {
//		return false;
//	}
//	public void setEditable(boolean isEditable) {
//	}
//	public String getError() {
//		return "";
//	}
//	public void setError(String message) {
//	}
//	public void setValueList(String[] valueList) {
//	}
//	public String[] getValueList() {
//		return new String[0];
//	}
//}
