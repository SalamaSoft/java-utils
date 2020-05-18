package com.salama.easyhttp.client;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.*;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * 
 * @author XingGu Liu
 *
 */
public class HttpClientUtil {
	public static final String DEFAULT_CHARSET = "utf-8";
	public static final Charset DefaultCharset = Charset.forName(DEFAULT_CHARSET);
	
	public static final int DEFAULT_CONNECTION_POOL_TIMEOUT_MS = 500;
	public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 5000;
	public static final int DEFAULT_REQUEST_TIMEOUT_MS = 60000;
	
	public static final int RESPONSE_STATUS_SUCCESS = 200;
	
	//private static ClientConnectionManager connectionManager;
	private static PoolingClientConnectionManager connectionManager;
	
	public static PoolingClientConnectionManager getConnectionManager() {
		return connectionManager;
	}

	private static HttpParams httpParams;
	
	//20K byte
	private static final int TEMP_BUFFER_DEFAULT_LENGTH = 20480;
	
	private static List<BasicNameValuePair> _defaultHeaders  = new ArrayList<BasicNameValuePair>();
	private static List<BasicNameValuePair> _defaultHeadersForMultipartPost = new ArrayList<BasicNameValuePair>();
	
	static {
		//Http setting -----------------------------------------------
		httpParams = new BasicHttpParams();
		//httpParams.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);

		// 设置一些基本参数
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(httpParams, DEFAULT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(httpParams, true);
				
		// HttpProtocolParams
		// .setUserAgent(
		// params,
		// "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
		// +
		// "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
		// 超时设置
		setTimeout(DEFAULT_CONNECTION_POOL_TIMEOUT_MS, DEFAULT_CONNECTION_TIMEOUT_MS, DEFAULT_REQUEST_TIMEOUT_MS);

		// 设置我们的HttpClient支持HTTP和HTTPS两种模式
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schReg.register(new Scheme("https", 443, SSLSocketFactory.getSocketFactory()));
		
		connectionManager = new PoolingClientConnectionManager(schReg);

		//default pool size
        final int maxPerRoute = 16384;
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        connectionManager.setMaxTotal(maxPerRoute * 2);


        //Default headers ------------------------------------------
		_defaultHeaders.add(new BasicNameValuePair("Content-Type", "application/x-www-form-urlencoded"));
		_defaultHeaders.add(new BasicNameValuePair("Accept-Encoding", "gzip"));
		_defaultHeaders.add(new BasicNameValuePair("accept", "*/*"));
		
		
		_defaultHeadersForMultipartPost.add(new BasicNameValuePair("Accept-Encoding", "gzip"));
		_defaultHeadersForMultipartPost.add(new BasicNameValuePair("accept", "*/*"));
	};

	private HttpClientUtil() {

	}

	public static void setConnetionTimeout(int httpConnectionTimeoutMS) {
		HttpConnectionParams.setConnectionTimeout(httpParams,
				httpConnectionTimeoutMS);
	}

	public static void setRequestTimeout(int httpRequestTimeoutMS) {
		HttpConnectionParams.setSoTimeout(httpParams, httpRequestTimeoutMS);
	}
	
	public static void setTimeout(int connectionPooltimeoutMS,
			int httpConnectionTimeoutMS, int httpRequestTimeoutMS) {
		ConnManagerParams.setTimeout(httpParams, connectionPooltimeoutMS);
		httpParams.setLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, connectionPooltimeoutMS);

		/* 连接超时 */
		HttpConnectionParams.setConnectionTimeout(httpParams,
				httpConnectionTimeoutMS);
		
		/* 请求超时 */
		HttpConnectionParams.setSoTimeout(httpParams, httpRequestTimeoutMS);
	}

	public static HttpClient getHttpClient() {
		HttpClient customerHttpClient = new DefaultHttpClient(connectionManager,
				httpParams);

		return customerHttpClient;

	}
	
	/**
	 * Warning: It's supposed to be invoked only once for one name-value. 
	 */ 
	public static void addDefaultHeader(String name, String value) {
		_defaultHeaders.add(new BasicNameValuePair(name, value));
		_defaultHeadersForMultipartPost.add(new BasicNameValuePair(name, value));
	}
	
	public static void addDefaultHeaders(HttpRequestBase request) {
//		request.addHeader("Content-Type", "application/x-www-form-urlencoded");
//		request.addHeader("Accept-Encoding", "gzip");
//		request.addHeader("accept", "*/*");

		BasicNameValuePair pair = null;
		for(int i = 0; i < _defaultHeaders.size(); i++) {
			pair = _defaultHeaders.get(i);
			request.addHeader(pair.getName(), pair.getValue());
		}
	}

	public static void addPostMultipartHeaders(HttpRequestBase request) {
//		request.addHeader("Accept-Encoding", "gzip");
//		request.addHeader("accept", "*/*");

		BasicNameValuePair pair = null;
		for(int i = 0; i < _defaultHeadersForMultipartPost.size(); i++) {
			pair = _defaultHeadersForMultipartPost.get(i);
			request.addHeader(pair.getName(), pair.getValue());
		}
	}
	
	public static String doGet(String url,
			List<String> paramNames, List<String> paramValues) throws ClientProtocolException, IOException {
		return doGet(getHttpClient(), url, paramNames, paramValues);
	}
	
	public static String doGet(
			HttpClient httpClient,
			String url,
			List<String> paramNames, List<String> paramValues) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = makeDoGetParamPairs(paramNames, paramValues);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		doBasicGet(httpClient, url, pairs, output);
		return new String(output.toByteArray(), DEFAULT_CHARSET);
	}
	
	public static String doGet(String url,
			String[] paramNames, String[] paramValues) throws ClientProtocolException, IOException {
		return doGet(getHttpClient(), url, paramNames, paramValues);
	}

	public static String doGet(
			HttpClient httpClient,
			String url,
			String[] paramNames, String[] paramValues) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = makeDoGetParamPairs(paramNames, paramValues);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		doBasicGet(httpClient, url, pairs, output);
		return new String(output.toByteArray(), DEFAULT_CHARSET);
	}
	
	public static byte[] doGetDownload(String url,
			String[] paramNames, String[] paramValues) throws ClientProtocolException, IOException {
		return doGetDownload(getHttpClient(), url, paramNames, paramValues);
	}
	
	public static byte[] doGetDownload(
			HttpClient httpClient,
			String url,
			String[] paramNames, String[] paramValues) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = makeDoGetParamPairs(paramNames, paramValues);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		doBasicGet(httpClient, url, pairs, output);
		return output.toByteArray();
	}
	
	public static byte[] doGetDownload(String url,
			List<String> paramNames, List<String> paramValues) throws ClientProtocolException, IOException {
		return doGetDownload(getHttpClient(), url, paramNames, paramValues);
	}

	public static int doGetDownload(
			HttpClient httpClient,
			String url,
			String[] paramNames, String[] paramValues, OutputStream output) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = makeDoGetParamPairs(paramNames, paramValues);
		return doBasicGet(httpClient, url, pairs, output);
	}
	
	public static int doGetDownload(String url,
			String[] paramNames, String[] paramValues, OutputStream output) throws ClientProtocolException, IOException {
		return doGetDownload(getHttpClient(), url, paramNames, paramValues, output);
	}
	
	public static byte[] doGetDownload(
			HttpClient httpClient,
			String url,
			List<String> paramNames, List<String> paramValues) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = makeDoGetParamPairs(paramNames, paramValues);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		doBasicGet(httpClient, url, pairs, output);
		return output.toByteArray();
	}
	
	public static int doGetDownload(String url,
			List<String> paramNames, List<String> paramValues, OutputStream output) throws ClientProtocolException, IOException {
		return doGetDownload(getHttpClient(), url, paramNames, paramValues, output);
	}
	
	public static int doGetDownload(
			HttpClient httpClient,
			String url,
			List<String> paramNames, List<String> paramValues, OutputStream output) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = makeDoGetParamPairs(paramNames, paramValues);
		return doBasicGet(httpClient, url, pairs, output);
	}
	
	public static List<BasicNameValuePair> makeDoGetParamPairs(List<String> paramNames, List<String> paramValues) {
		List<BasicNameValuePair> pairs = null;
		if(paramNames != null && paramValues != null) {
			pairs = new ArrayList<BasicNameValuePair>();
			//封装请求参数
			for (int i = 0; i < paramNames.size(); i++) {
				pairs.add(new BasicNameValuePair(paramNames.get(i), paramValues.get(i)));
			}
		}
		
		return pairs;
	}
	
	public static List<BasicNameValuePair> makeDoGetParamPairs(String[] paramNames, String[] paramValues) {
		List<BasicNameValuePair> pairs = null;
		
		if(paramNames != null && paramValues != null) {
			pairs = new ArrayList<BasicNameValuePair>();
			//封装请求参数
			for (int i = 0; i < paramNames.length; i++) {
				pairs.add(new BasicNameValuePair(paramNames[i], paramValues[i]));
			}
		}
		
		return pairs;
	}

	public static String makeDoGetUrlWithParams(String url, List<BasicNameValuePair> pairs) {
		//URL Params
		StringBuilder urlWithParams = new StringBuilder(url);
		
		if(pairs != null && pairs.size() > 0) {
			int indexOfQuestion = url.indexOf("?");
			
			if(indexOfQuestion < 0) {
				urlWithParams.append("?");
			} else {
				if(indexOfQuestion != (url.length() - 1)) {
					urlWithParams.append("&");
				}
			}
			
			urlWithParams.append(URLEncodedUtils.format(pairs, DEFAULT_CHARSET));
		}
		
		return urlWithParams.toString();
	}

	public static int doBasicGet(
			HttpClient httpClient,
			String url,
			List<BasicNameValuePair> pairs,
			OutputStream output) throws ClientProtocolException, IOException {
		return doBasicGet(httpClient,
				url, null,
				pairs,
				output);
	}

	public static int doBasicGet(
			HttpClient httpClient,
			String url, Map<String, String> headers,
			List<BasicNameValuePair> pairs,
			OutputStream output) throws ClientProtocolException, IOException {
		//Http client
		String urlStrWithParams = makeDoGetUrlWithParams(url, pairs);
		HttpGet request = new HttpGet(urlStrWithParams);
		
		try {
			addDefaultHeaders(request);
			if(headers != null) {
				for(Map.Entry<String, String> kv : headers.entrySet()) {
					request.addHeader(kv.getKey(), kv.getValue());
				}
			}

			HttpResponse response = httpClient.execute(request);

			if (response.getStatusLine().getStatusCode() == RESPONSE_STATUS_SUCCESS) {
				return getResponseContent(response.getEntity(), output);
			} else {
				return 0;
			}
		} finally {
			try {
				request.releaseConnection();
			} catch(Exception e) {
			}
		}
	}
	
	public static String doPost(String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues) throws ClientProtocolException, IOException {
		return doPost(getHttpClient(), url, paramNames, paramValues, filePartValues);
	}

	public static String doPost(
			HttpClient httpClient,
			String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues) throws ClientProtocolException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		if (filePartValues == null || filePartValues.size() == 0) {
			// 封装请求参数
			List<BasicNameValuePair> pairs = makeDoGetParamPairs(paramNames, paramValues);

			doBasicPost(httpClient, url, pairs, output);
		} else {
			doBasicPostMultipart(httpClient, url, paramNames, paramValues, filePartValues, output);
		}

		return new String(output.toByteArray(), DEFAULT_CHARSET);
	}

	public static String doPost(String url,
			String[] paramNames, String[] paramValues,
			MultiPartFile[] filePartValues) throws ClientProtocolException, IOException {
		return doPost(getHttpClient(), url, paramNames, paramValues, filePartValues);
	}
	
	public static String doPost(
			HttpClient httpClient,
			String url,
			String[] paramNames, String[] paramValues,
			MultiPartFile[] filePartValues) throws ClientProtocolException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		if (filePartValues == null || filePartValues.length == 0) {
			// 封装请求参数
			List<BasicNameValuePair> pairs = makeDoGetParamPairs(paramNames, paramValues);

			doBasicPost(httpClient, url, pairs, output);
		} else {
			doBasicPostMultipart(httpClient, url, paramNames, paramValues, filePartValues, output);
		}

		return new String(output.toByteArray(), DEFAULT_CHARSET);
	}
	
	public static byte[] doPostDownload(String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues) throws ClientProtocolException, IOException {
		return doPostDownload(getHttpClient(), url, paramNames, paramValues, filePartValues);
	}
	
	public static byte[] doPostDownload(
			HttpClient httpClient,
			String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues) throws ClientProtocolException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		if (filePartValues == null || filePartValues.size() == 0) {
			// 封装请求参数
			List<BasicNameValuePair> pairs = makeDoGetParamPairs(paramNames, paramValues);

			doBasicPost(httpClient, url, pairs, output);
		} else {
			doBasicPostMultipart(httpClient, url, paramNames, paramValues, filePartValues, output);
		}

		return output.toByteArray();
	}

	public static int doPostDownload(String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues, OutputStream output) throws ClientProtocolException, IOException {
		return doPostDownload(getHttpClient(), url, paramNames, paramValues, filePartValues, output);
	}
	
	public static int doPostDownload(
			HttpClient httpClient,
			String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues, OutputStream output) throws ClientProtocolException, IOException {
		if (filePartValues == null || filePartValues.size() == 0) {
			// 封装请求参数
			List<BasicNameValuePair> pairs = makeDoGetParamPairs(paramNames, paramValues);

			return doBasicPost(httpClient, url, pairs, output);
		} else {
			return doBasicPostMultipart(httpClient, url, paramNames, paramValues, filePartValues, output);
		}
	}

	public static int doBasicPost(
			HttpClient httpClient,
			String url,
			List<BasicNameValuePair> pairs, OutputStream output
	) throws ClientProtocolException, IOException {
		return doBasicPost(httpClient, url, null, pairs, output);
	}

	public static int doBasicPost(
			HttpClient httpClient,
			String url, Map<String, String> headers,
			List<BasicNameValuePair> pairs, OutputStream output
	) throws ClientProtocolException, IOException {
		
		HttpPost request = new HttpPost(url);

		try {
			addDefaultHeaders(request);
			if(headers != null) {
				for(Map.Entry<String, String> kv : headers.entrySet()) {
					request.addHeader(kv.getKey(), kv.getValue());
				}
			}

			if(pairs != null && pairs.size() > 0) {
				// 把请求参数变成请求体部分
				UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(pairs, DEFAULT_CHARSET);
				request.setEntity(formEntity);
			}
			
			HttpResponse response = httpClient.execute(request);
			
			if (response.getStatusLine().getStatusCode() == RESPONSE_STATUS_SUCCESS) {
				return getResponseContent(response.getEntity(), output);
			} else {
				return 0;
			}
		} finally {
			try {
				request.releaseConnection();
			} catch(Exception e) {
			}
		}
		
	}

	public static int doBasicPostMultipart(
			HttpClient httpClient,
			String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues, OutputStream output) throws ClientProtocolException, IOException {
		return doBasicPostMultipart(
				httpClient,
				url, null,
				makeDoGetParamPairs(paramNames, paramValues),
				filePartValues, output
		);
	}

	public static int doBasicPostMultipart(
			HttpClient httpClient,
			String url,
			String[] paramNames, String[] paramValues,
			MultiPartFile[] filePartValues, OutputStream output) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> params = makeDoGetParamPairs(paramNames, paramValues);
		return doBasicPostMultipart(
				httpClient,
				url, null,
				makeDoGetParamPairs(paramNames, paramValues),
				arrayToList(filePartValues), output
		);
	}

	public static int doBasicPostMultipart(
			HttpClient httpClient,
			String url, Map<String, String> headers,
			List<BasicNameValuePair> params,
			List<MultiPartFile> filePartValues, OutputStream output
	) throws ClientProtocolException, IOException {

		MultipartEntity multipartEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		// 封装请求参数
		if(params != null) {
			for(BasicNameValuePair pair : params) {
				multipartEntity.addPart(
						pair.getName(),
						new StringBody(pair.getValue(), DefaultCharset)
				);
			}
		}

		// 上传文件
		if(filePartValues != null) {
			for(MultiPartFile multiPartFile : filePartValues) {
				if(multiPartFile.isUseInputStream()) {
					multipartEntity.addPart(multiPartFile.getName(),
							new InputStreamBody(
									multiPartFile.getInputStream(),
									multiPartFile.getName()));
				} else {
					multipartEntity.addPart(multiPartFile.getName(),
							new FileBody(multiPartFile.getFile()));
				}
			}
		}

		return doMultipartPost(httpClient, url, headers, multipartEntity, output);
	}

	public static int doMultipartPost(
			HttpClient httpClient,
			String url, Map<String, String> headers,
			MultipartEntity multipartEntity, OutputStream output) throws ClientProtocolException, IOException {
		// 使用HttpPost对象设置发送的URL路径
		HttpPost request = new HttpPost(url);

		try {
			addPostMultipartHeaders(request);
			if(headers != null) {
				for(Map.Entry<String, String> kv : headers.entrySet()) {
					request.addHeader(kv.getKey(), kv.getValue());
				}
			}

			request.setEntity(multipartEntity);
			HttpResponse response = httpClient.execute(request);

			if (response.getStatusLine().getStatusCode() == RESPONSE_STATUS_SUCCESS) {
				return getResponseContent(response.getEntity(), output);
			} else {
				return 0;
			}
		} finally {
			try {
				request.releaseConnection();
			} catch(Exception e) {
			}
		}
	}

    public static String doPostStringEntity(
            String url,
            String charset,
            String contentType,
            Map<String, String> headers,
            String content
    ) throws IOException {
        return doPostStringEntity(HttpClientUtil.getHttpClient(), url, charset, contentType, headers, content);
    }

	public static String doPostStringEntity(
	        HttpClient httpClient,
            String url,
            String charset,
            String contentType,
            Map<String, String> headers,
            String content
    ) throws IOException {
		HttpPost httpost = new HttpPost(url);

		try {
			httpost.setHeader(HTTP.CONTENT_TYPE, contentType);

			if(headers != null) {
                for(Map.Entry<String, String> entry : headers.entrySet()) {
                    httpost.addHeader(entry.getKey(), entry.getValue());
                }
            }

			httpost.setEntity(new StringEntity(content, charset));

			HttpResponse response = httpClient.execute(httpost);
			if(response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return "";
			}

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int len = HttpClientUtil.getResponseContent(response.getEntity(), bos);
			String responseStr = new String(bos.toByteArray(), charset);

			return responseStr;
		} finally {
			httpost.releaseConnection();
		}
	}

	public static int getResponseContent(HttpEntity httpEntity, OutputStream output) throws IOException {
		InputStream is = null;
		BufferedInputStream bis = null;
		
		try {
			is = httpEntity.getContent();
			bis = new BufferedInputStream(is);
			
			boolean isGzip = false;
			if(httpEntity.getContentEncoding() != null) {
				HeaderElement[] headers = httpEntity.getContentEncoding().getElements();
				if(headers != null) {
					for(int i = 0; i < headers.length; i++) {
						//SSLog.d("HttpClientUtil", "HttpHeader " + headers[i].getName() + ":" + headers[i].getValue());
						if("gzip".equalsIgnoreCase(headers[i].getName())) {
							isGzip = true;
						}
					}
				}
			}
			/*
			byte[] gzipSignBytes = new byte[2];
			bis.mark(2);
			
			int readed = bis.read(gzipSignBytes, 0, 2);
			
			if(readed == 0) {
				return 0;
			} else 
			*/	
			{
				//bis.reset();
				
				InputStream is2 = null;
				
				try {
					//if((readed == 2) && (gzipSignBytes[0] == 31) && (gzipSignBytes[1] == 139)) {
					if(isGzip) {
						//gzip
						is2 = new GZIPInputStream(bis);
					} else {
						is2 = bis;
					}
					
					return readAllBytes(is2, output);
				} finally {
					try {
						is2.close();
					} catch(Exception e) {
					}
				}
			}
		} finally {
			try {
				bis.close();
			} catch(Exception e) {
			}
			try {
				is.close();
			} catch(Exception e) {
			}
			try {
				EntityUtils.consume(httpEntity);
			} catch(Exception e) {
			}
		}
		
	}

	public static int readAllBytes(InputStream input, OutputStream output) throws IOException {
		byte[] tempBuffer = new byte[TEMP_BUFFER_DEFAULT_LENGTH];
		int readCnt;
		int contentLen = 0;
		
		while(true) {
			readCnt = input.read(tempBuffer, 0, TEMP_BUFFER_DEFAULT_LENGTH);
			if(readCnt < 0) {
				//no more to read
				break;
			}

			if(readCnt != 0) {
				contentLen += readCnt;
				
				output.write(tempBuffer, 0, readCnt);
				output.flush();
			}
		}
		
		return contentLen;
	}

	public static byte[] readAllBytes(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		readAllBytes(input, output);
		
		return output.toByteArray();
	}

	private static FormBodyPart createFormBodyPart(String[] paramNames, String[] paramValues) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < paramNames.length; i++) {
			if(i > 0) {
				sb.append("&");
			}
			sb.append(paramNames[i]).append("=").append(urlComponentEncode(paramValues[i], DEFAULT_CHARSET));
		}
		
		FormBodyPart bodyPart = new FormBodyPart("rawData", 
				new StringBody(sb.toString(), "application/x-www-form-urlencoded", DefaultCharset));

		return bodyPart;
	}

	private static String urlComponentEncode(String strVal, String encode) throws UnsupportedEncodingException {
		return URLEncoder.encode(strVal, encode).replaceAll("\\+", "%20");
	}

	private static <T> List<T> arrayToList(T[] objs) {
		if(objs == null) {
			return null;
		} else {
			List<T> list = new ArrayList<T>();
			for(T o : objs) {
				list.add(o);
			}
			return list;
		}
	}

}
