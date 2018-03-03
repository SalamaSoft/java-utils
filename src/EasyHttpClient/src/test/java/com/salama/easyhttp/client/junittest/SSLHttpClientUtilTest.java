package com.salama.easyhttp.client.junittest;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

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

}
