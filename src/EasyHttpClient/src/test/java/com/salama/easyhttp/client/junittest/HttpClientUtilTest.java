package com.salama.easyhttp.client.junittest;

import com.salama.easyhttp.client.HttpClientUtil;
import com.salama.easyhttp.client.MultiPartFile;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientUtilTest {

	public void test1() {
		for(int i = 0; i < 10; i++) {
			testDoGet();
		}
	}

	@Test
	public void testDoGet() {
		String url = "http://api.dianping.com/v1/business/find_businesses?format=json&city=上海&latitude=31.293330359945465&longitude=121.51796146987469&sort=7&offset_type=1&radius=5000&out_offset_type=1&platform=2&has_deal=1&limit=5&page=1";
		try {
			String responseStr = HttpClientUtil.doGet(url, new String[0], new String[0]);
			System.out.println(responseStr);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testDoGet_connection_close() {
		String url = "http://api.dianping.com/v1/business/find_businesses?format=json&city=上海&latitude=31.293330359945465&longitude=121.51796146987469&sort=7&offset_type=1&radius=5000&out_offset_type=1&platform=2&has_deal=1&limit=5&page=1";
		try {
			String responseStr;
			{
				Map<String, String> headers = new HashMap<>();
				headers.put("Connection", "close");

				ByteArrayOutputStream output = new ByteArrayOutputStream();
				responseStr = new String(output.toByteArray(), StandardCharsets.UTF_8);
				HttpClientUtil.doBasicGet(
						HttpClientUtil.getHttpClient(),
						url,
						headers,
						null,
						output
				);
			}
			System.out.println(responseStr);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String doPost(String url,
								List<String> paramNames, List<String> paramValues,
								Map<String, String> headers,
								List<MultiPartFile> filePartValues) throws ClientProtocolException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		if (filePartValues == null || filePartValues.size() == 0) {
			// 封装请求参数
			List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);
			HttpClientUtil.doBasicPost(HttpClientUtil.getHttpClient(), url, headers, pairs, output);
		}

		return new String(output.toByteArray(), StandardCharsets.UTF_8);
	}

	/*
	public static void test_no_pool(int connectTimeout, int requestTimeout) {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, connectTimeout);
		HttpConnectionParams.setSoTimeout(httpParams, requestTimeout);

//		HttpClientUtil.getConnectionManager().set


	}
	*/

	private static void setTimeout(
			HttpParams httpParams,
			int connectionPooltimeoutMS,
			int httpConnectionTimeoutMS, int httpRequestTimeoutMS
	) {
		ConnManagerParams.setTimeout(httpParams, connectionPooltimeoutMS);
		httpParams.setLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, connectionPooltimeoutMS);

		/* 连接超时 */
		HttpConnectionParams.setConnectionTimeout(httpParams,
				httpConnectionTimeoutMS);

		/* 请求超时 */
		HttpConnectionParams.setSoTimeout(httpParams, httpRequestTimeoutMS);
	}

}
