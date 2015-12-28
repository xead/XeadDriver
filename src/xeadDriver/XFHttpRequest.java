package xeadDriver;

/*
 * Copyright (c) 2015 WATANABE kozo <qyf05466@nifty.com>,
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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
//import org.apache.xerces.parsers.DOMParser;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;

public class XFHttpRequest {
	private HttpPost httpPost = null;
	private String encoding_ = "";
	//private DOMParser domParser = null;
	private List<NameValuePair> params = new ArrayList<NameValuePair>();

    public XFHttpRequest(String uri, String encoding) {
    	httpPost = new HttpPost(uri);
    	encoding_ = encoding;
    	//domParser = parser;
	}

    public void setParameter(String name, String value) {
    	params.add(new BasicNameValuePair(name, value));
    }

    public void setHeader(String name, String value) {
    	httpPost.setHeader(name, value);
    }

    public Object execute() {
    	Object response = null;
		HttpResponse httpResponse = null;
		InputStream inputStream = null;
		HttpClient httpClient = new DefaultHttpClient();
    	try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, encoding_));
			httpResponse = httpClient.execute(httpPost);
			if (httpResponse == null) {
				response = "Response is null.";
			} else {
//			String responseContentType = httpResponse.getEntity().getContentType().getValue();
//			if (responseContentType.contains("text/xml")) {
//				inputStream = httpResponse.getEntity().getContent();
//				domParser.parse(new InputSource(inputStream));
//				response = domParser.getDocument();
//			}
//			if (responseContentType.contains("application/json")) {
//				String text = EntityUtils.toString(httpResponse.getEntity(), encoding);
//				if (text.startsWith("[")) {
//					response = new JSONArray(text);
//				} else {
//					response = text;
//				}
//			}
//			if (response == null) {
//				//response = EntityUtils.toString(httpResponse.getEntity());
//				response = EntityUtils.toString(httpResponse.getEntity(), encoding_);
//			}
				response = EntityUtils.toString(httpResponse.getEntity(), encoding_);
			}
		} catch (UnsupportedEncodingException e) {
			response = "ERROR: "+e.getMessage();
		} catch (ClientProtocolException e) {
			response = "ERROR: "+e.getMessage();
		} catch (IllegalStateException e) {
			response = "ERROR: "+e.getMessage();
		} catch (ParseException e) {
			response = "ERROR: "+e.getMessage();
		} catch (IOException e) {
			response = "ERROR: "+e.getMessage();
		//} catch (SAXException e) {
		//	response = "ERROR: "+e.getMessage();
		//} catch (JSONException e) {
		//	response = "ERROR: "+e.getMessage();
		} finally {
			httpClient.getConnectionManager().shutdown();
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch(Exception e) {}
		}
    	return response;
    }
}
