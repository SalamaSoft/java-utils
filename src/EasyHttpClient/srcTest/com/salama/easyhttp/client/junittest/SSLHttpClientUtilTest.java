package com.salama.easyhttp.client.junittest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.List;

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
			
			String url = "https://github.com/";
			
			ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
			HttpClientUtil.doBasicGet(sslClientUtil.createHttpClient(), 
					url, 
					(List<BasicNameValuePair>)null, responseBytes);
			
			String response = new String(responseBytes.toByteArray(), "utf-8");
			System.out.println("response:-----------------\n" + response);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
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
			
			ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
			HttpClientUtil.doBasicGet(sslClientUtil.createHttpClient(), 
					url, 
					(List<BasicNameValuePair>)null, responseBytes);
			
			String response = new String(responseBytes.toByteArray(), "utf-8");
			System.out.println("response:-----------------\n" + response);
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
	
}
