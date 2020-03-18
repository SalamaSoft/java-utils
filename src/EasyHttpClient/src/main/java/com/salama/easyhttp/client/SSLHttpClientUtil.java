package com.salama.easyhttp.client;

import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

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
		
		public boolean isTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
			return true;
		}
	};
	public final static X509HostnameVerifier HostnameVerifierTrustAnyHost = new X509HostnameVerifier() {
		
		public boolean verify(String arg0, SSLSession arg1) {
			return true;
		}
		
		public void verify(String arg0, String[] arg1, String[] arg2)
				throws SSLException {
		}
		
		public void verify(String arg0, X509Certificate arg1) throws SSLException {
		}
		
		public void verify(String arg0, SSLSocket arg1) throws IOException {
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
		
		SSLSocketFactory socketFactory;
		
		/* deprecated way
		if(keystore == null && trustStore == null) {
			SSLContext sslCtx = SSLContext.getInstance("SSLv3");
			X509TrustManager trustManager = new X509TrustManager() {
				
				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				
				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
				}
				
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
				}
			};
			
			sslCtx.init(
					null,
					new TrustManager[] {trustManager},
					null);
			
			socketFactory = new SSLSocketFactory(sslCtx);
			socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		} else {
			*/
			socketFactory = new SSLSocketFactory(
					//SSLSocketFactory.TLS,
					"TLS",
					keystore, storePassword, trustStore, 
					null,
					trustStrategy,
					//SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
					HostnameVerifierTrustAnyHost
					);
		//}
		
		
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
		ConnManagerParams.setTimeout(_httpParams, connectionPooltimeoutMS);
		
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
	
	public final static KeyStore readJKS(InputStream p12, String storePassword) throws NoSuchAlgorithmException, CertificateException, IOException, KeyStoreException {
		KeyStore ks = KeyStore.getInstance("JKS");

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
