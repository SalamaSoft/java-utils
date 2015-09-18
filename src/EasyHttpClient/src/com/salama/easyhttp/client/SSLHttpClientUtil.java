package com.salama.easyhttp.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/**
 * 
 * @author XingGu Liu
 *
 */
public class SSLHttpClientUtil {
	public static final String DEFAULT_CHARSET = "utf-8";
	public static final int DEFAULT_CONNECTION_POOL_TIMEOUT_MS = 10000;
	public static final int DEFAULT_CONNECTION_TIMEOUT_MS = 10000;
	public static final int DEFAULT_REQUEST_TIMEOUT_MS = 30000;
	
	public final static TrustStrategy TrustStrategyTrustAnyServer = new TrustStrategy() {
		
		@Override
		public boolean isTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
			return true;
		}
	};

	private final PoolingClientConnectionManager _connectionManager;
	private final HttpParams _httpParams;

	
	public PoolingClientConnectionManager getConnectionManager() {
		return _connectionManager;
	}

	public SSLHttpClientUtil(KeyStore keystore, String storePassword) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, KeyManagementException, UnrecoverableKeyException {
		this(keystore, storePassword, null, null);
	}

	/**
	 * 
	 * @param keystore Required. Usually it is a PKCS12(*.p12 file) for that server verify client. 
	 * @param storePassword Required. Password for keystore
	 * @param trustStore Optional Usually it is a keystore containing certificate for that client verify server.
	 * @param trustStrategy Optional. Predefined TrustStrategyTrustAnyServer is for ignoring if server's SSL Cert is published by ROOT SSL Organization.
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws KeyManagementException
	 * @throws UnrecoverableKeyException
	 */
	public SSLHttpClientUtil(KeyStore keystore, String storePassword,
			KeyStore trustStore,
			TrustStrategy trustStrategy) throws NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, KeyManagementException, UnrecoverableKeyException {
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		
		SSLSocketFactory socketFactory = new SSLSocketFactory(
				SSLSocketFactory.TLS, 
				keystore, storePassword, trustStore, 
				null,
				trustStrategy,
				SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		
		schReg.register(new Scheme("https", 443, socketFactory));
		
		_connectionManager = new PoolingClientConnectionManager(schReg);
		
		
		//Http setting -----------------------------------------------
		_httpParams = new BasicHttpParams();
		// 设置一些基本参数
		HttpProtocolParams.setVersion(_httpParams, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(_httpParams, DEFAULT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(_httpParams, true);
				
		// HttpProtocolParams
		// .setUserAgent(
		// params,
		// "Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
		// +
		// "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
		// 超时设置
		setTimeout(DEFAULT_CONNECTION_POOL_TIMEOUT_MS, DEFAULT_CONNECTION_TIMEOUT_MS, DEFAULT_REQUEST_TIMEOUT_MS);
	}

	public HttpClient getHttpClient() throws IOException {
		HttpClient customerHttpClient = new DefaultHttpClient(_connectionManager,
				_httpParams);

		return customerHttpClient;
	}
	
	public void setTimeout(int connectionPooltimeoutMS,
			int httpConnectionTimeoutMS, int httpRequestTimeoutMS) {
		//ConnManagerParams.setTimeout(httpParams, connectionPooltimeoutMS);
		
		/* 连接超时 */
		HttpConnectionParams.setConnectionTimeout(_httpParams,
				httpConnectionTimeoutMS);
		
		/* 请求超时 */
		HttpConnectionParams.setSoTimeout(_httpParams, httpRequestTimeoutMS);
	}
	
	public final static KeyStore readPKCS12(InputStream p12, String storePassword) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
		KeyStore ks = KeyStore.getInstance("PKCS12");

		ks.load(p12, storePassword.toCharArray());
		
		return ks;
	}
	
	
	public String doGet(String url,
			List<String> paramNames, List<String> paramValues) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		HttpClientUtil.doBasicGet(getHttpClient(), url, pairs, output);
		return new String(output.toByteArray(), DEFAULT_CHARSET);
	}
	
	public String doGet(String url,
			String[] paramNames, String[] paramValues) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		HttpClientUtil.doBasicGet(getHttpClient(), url, pairs, output);
		return new String(output.toByteArray(), DEFAULT_CHARSET);
	}

	public byte[] doGetDownload(String url,
			String[] paramNames, String[] paramValues) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		HttpClientUtil.doBasicGet(getHttpClient(), url, pairs, output);
		return output.toByteArray();
	}
	
	public byte[] doGetDownload(String url,
			List<String> paramNames, List<String> paramValues) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		HttpClientUtil.doBasicGet(getHttpClient(), url, pairs, output);
		return output.toByteArray();
	}

	public int doGetDownload(String url,
			String[] paramNames, String[] paramValues, OutputStream output) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);
		return HttpClientUtil.doBasicGet(getHttpClient(), url, pairs, output);
	}
	
	public int doGetDownload(String url,
			List<String> paramNames, List<String> paramValues, OutputStream output) throws ClientProtocolException, IOException {
		List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);
		return HttpClientUtil.doBasicGet(getHttpClient(), url, pairs, output);
	}

	public String doPost(String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues) throws ClientProtocolException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		if (filePartValues == null || filePartValues.size() == 0) {
			// 封装请求参数
			List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);

			HttpClientUtil.doBasicPost(getHttpClient(), url, pairs, output);
		} else {
			HttpClientUtil.doBasicPostMultipart(getHttpClient(), url, paramNames, paramValues, filePartValues, output);
		}

		return new String(output.toByteArray(), DEFAULT_CHARSET);
	}

	public String doPost(String url,
			String[] paramNames, String[] paramValues,
			MultiPartFile[] filePartValues) throws ClientProtocolException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		if (filePartValues == null || filePartValues.length == 0) {
			// 封装请求参数
			List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);

			HttpClientUtil.doBasicPost(getHttpClient(), url, pairs, output);
		} else {
			HttpClientUtil.doBasicPostMultipart(getHttpClient(), url, paramNames, paramValues, filePartValues, output);
		}

		return new String(output.toByteArray(), DEFAULT_CHARSET);
	}
	
	public byte[] doPostDownload(String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues) throws ClientProtocolException, IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		if (filePartValues == null || filePartValues.size() == 0) {
			// 封装请求参数
			List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);

			HttpClientUtil.doBasicPost(getHttpClient(), url, pairs, output);
		} else {
			HttpClientUtil.doBasicPostMultipart(getHttpClient(), url, paramNames, paramValues, filePartValues, output);
		}

		return output.toByteArray();
	}

	public int doPostDownload(String url,
			List<String> paramNames, List<String> paramValues,
			List<MultiPartFile> filePartValues, OutputStream output) throws ClientProtocolException, IOException {
		if (filePartValues == null || filePartValues.size() == 0) {
			// 封装请求参数
			List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);

			return HttpClientUtil.doBasicPost(getHttpClient(), url, pairs, output);
		} else {
			return HttpClientUtil.doBasicPostMultipart(getHttpClient(), url, paramNames, paramValues, filePartValues, output);
		}
	}
	
}
