package com.ebupt.webjoin.insight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.NameValuePair;

import com.ebupt.webjoin.insight.json.InsightJsonObject;

public class HttpClientSender {
	private HttpClient client;
	private HttpPost request;

	public HttpClientSender(String url) {
		init(url);
	}

	public HttpClientSender() {
		String url = PropertiesReader.getProps().getProperty("server.addr");
		System.out.println("URL " + url);
		init(url);
	}

	private void init(String url) {
		client = new DefaultHttpClient();

		request = new HttpPost(url);
	}

	public void closeConn() {
		this.request.abort();
		this.request.releaseConnection();
		this.client.getConnectionManager().shutdown();
		// logger.info("close httpclient...");
	}

	public void post(String key, InsightJsonObject val) {
		BasicHttpParams param = new BasicHttpParams();
		NameValuePair nameVal = new BasicNameValuePair(key, val.toString());
		param.setParameter(key, val);
		List<NameValuePair> list = new ArrayList<NameValuePair>();
		list.add(nameVal);

		try {
			request.setEntity(new UrlEncodedFormEntity(list));
			HttpResponse response = client.execute(request);
			/*
			 * if(200 != response.getStatusLine().getStatusCode())
			 * logger.warn("send "+"["+key+":"+val+"] FAILED."); else
			 * if(logger.isDebugEnabled())
			 * logger.debug("send "+"["+key+":"+val+"] SUCCEED.");
			 */

			response.getEntity().getContent().close();
		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
