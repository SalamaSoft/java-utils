package com.salama.easyhttp.client.junittest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import com.salama.easyhttp.client.HttpClientUtil;
import com.salama.easyhttp.client.SSLHttpClientUtil;

public class SSLHttpClientUtilTest {

	@Test
	public void test1() {
		try {
			SSLHttpClientUtil sslClientUtil = new SSLHttpClientUtil(
					null, null, null, 
					SSLHttpClientUtil.TrustStrategyTrustAnyServer);
			
			sslClientUtil.setTimeout(5000, 5000, 10000);
			String url = "https://github.com/";
			
			
			String response = sslClientUtil.doGet(url, (String[])null, (String[])null);
			System.out.println("response:-----------------\n" + response);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test2() {
		try {
			File p12File = new File("test.p12");
			String storePassword = "123";
			KeyStore keystore = null;
			FileInputStream p12Input = new FileInputStream(p12File);
			try {
				keystore = SSLHttpClientUtil.readPKCS12(p12Input, storePassword);
			} finally {
				
			}
			
			SSLHttpClientUtil sslClientUtil = new SSLHttpClientUtil(
					keystore, storePassword, null, 
					SSLHttpClientUtil.TrustStrategyTrustAnyServer);
			
			String url = "https://testssl.com/";
			
			String response = sslClientUtil.doGet(url, (String[])null, (String[])null);
			System.out.println("response:-----------------\n" + response);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}


	@Test
	public void test3() {
		String url = "https://127.0.0.1/test/test1?sleep=16200";

		try {
			SSLHttpClientUtil sslHttpClientUtil = new SSLHttpClientUtil(
					null, null,null,
					SSLHttpClientUtil.TrustStrategyTrustAnyServer
			);
			sslHttpClientUtil.setTimeout(
					3*1000,
					10*1000,
					15*1000
			);

			final long opBegin = System.currentTimeMillis();

			try {
				//String resp = sslHttpClientUtil.doGet(url, (String[]) null, null);
				String resp = doGet(sslHttpClientUtil.getHttpClient(), url, null, null);
				System.out.println(" cost(ms):" + (System.currentTimeMillis() - opBegin)
						+ " resp:" + resp
				);
			} catch (Throwable e) {
				e.printStackTrace();
				System.out.println(" cost(ms):" + (System.currentTimeMillis() - opBegin));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test4() {
		String url = "http://127.0.0.1/test/test1?sleep=3000";

		try {
			HttpClientUtil.getConnectionManager().setDefaultMaxPerRoute(1);
			HttpClientUtil.getConnectionManager().setMaxTotal(1);

			HttpClientUtil.setTimeout(
					2*1000,
					6 * 1000,
					6*1000
			);

			ExecutorService workPool = Executors.newFixedThreadPool(4);
			for(int i = 0; i < 8; i++) {
				workPool.execute(() -> {
					final long opBegin = System.currentTimeMillis();

					try {
						//String resp = sslHttpClientUtil.doGet(url, (String[]) null, null);
						String resp = doGet(HttpClientUtil.getHttpClient(), url, null, null);
						System.out.println(" cost(ms):" + (System.currentTimeMillis() - opBegin)
								+ " resp:" + resp
						);
					} catch (Throwable e) {
						System.out.println(" cost(ms):" + (System.currentTimeMillis() - opBegin)
								+ " " + e.getClass().getName() + " " + e.getMessage()
								);
					}
				});
			}

			Thread.sleep(30L * 1000);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test5() {
		String url = "https://127.0.0.1/test/test1?sleep=3000";

		try {
			SSLHttpClientUtil sslHttpClientUtil = new SSLHttpClientUtil(
					null, null,null,
					SSLHttpClientUtil.TrustStrategyTrustAnyServer
			);

			sslHttpClientUtil.getConnectionManager().setDefaultMaxPerRoute(1);
			sslHttpClientUtil.getConnectionManager().setMaxTotal(1);

			sslHttpClientUtil.setTimeout(
					2*1000,
					6 * 1000,
					6*1000
			);

			ExecutorService workPool = Executors.newFixedThreadPool(4);
			for(int i = 0; i < 8; i++) {
				workPool.execute(() -> {
					final long opBegin = System.currentTimeMillis();

					try {
						//String resp = sslHttpClientUtil.doGet(url, (String[]) null, null);
						String resp = doGet(sslHttpClientUtil.getHttpClient(), url, null, null);
						System.out.println(" cost(ms):" + (System.currentTimeMillis() - opBegin)
								+ " resp:" + resp
						);
					} catch (Throwable e) {
						System.out.println(" cost(ms):" + (System.currentTimeMillis() - opBegin)
								+ " " + e.getClass().getName() + " " + e.getMessage()
						);
					}
				});
			}

			Thread.sleep(30L * 1000);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test6() {
		String url = "https://127.0.0.1/test/test1?sleep=500";

		try {
			SSLHttpClientUtil sslHttpClientUtil = new SSLHttpClientUtil(
					null, null,null,
					SSLHttpClientUtil.TrustStrategyTrustAnyServer
			);

			final int maxTotal = 16;

			sslHttpClientUtil.getConnectionManager().setDefaultMaxPerRoute(maxTotal);
			sslHttpClientUtil.getConnectionManager().setMaxTotal(maxTotal);

			sslHttpClientUtil.setTimeout(
					2*1000,
					6 * 1000,
					6*1000
			);

			ExecutorService workPool = Executors.newFixedThreadPool(8);
			for(int i = 0; i < 8; i++) {
				workPool.execute(() -> {
					final long opBegin = System.currentTimeMillis();

					try {
						//String resp = sslHttpClientUtil.doGet(url, (String[]) null, null);
						String resp = doGet(sslHttpClientUtil.getHttpClient(), url, null, null);
						System.out.println(" cost(ms):" + (System.currentTimeMillis() - opBegin)
								+ " resp:" + resp
						);
					} catch (Throwable e) {
						System.out.println(" cost(ms):" + (System.currentTimeMillis() - opBegin)
								+ " " + e.getClass().getName() + " " + e.getMessage()
						);
					}
				});
			}

			Thread.sleep(10L * 1000);

			sslHttpClientUtil.getConnectionManager().closeIdleConnections(60L * 1000, TimeUnit.MILLISECONDS);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static String doGet(
			HttpClient httpClient, String url,
			String[] paramNames, String[] paramValues
	) throws IOException {
		List<BasicNameValuePair> pairs = HttpClientUtil.makeDoGetParamPairs(paramNames, paramValues);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		String urlStrWithParams = HttpClientUtil.makeDoGetUrlWithParams(url, pairs);
		HttpGet request = new HttpGet(urlStrWithParams);
		request.setHeader("User-Agent", "test userAgent");

		try {
			HttpClientUtil.addDefaultHeaders(request);
			HttpResponse response = httpClient.execute(request);

			if (response.getStatusLine().getStatusCode() == HttpClientUtil.RESPONSE_STATUS_SUCCESS) {
				HttpClientUtil.getResponseContent(response.getEntity(), output);
				return new String(output.toByteArray(), "utf-8");
			}
			else{
				return null;
			}
		} finally {
			request.releaseConnection();
		}
	}

}
