package com.salama.easyhttp.client.junittest;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.salama.easyhttp.client.SSLHttpClientUtil;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Ignore;
import org.junit.Test;

import sun.util.logging.resources.logging;

import com.salama.easyhttp.client.HttpClientUtil;
import com.salama.easyhttp.client.MultiPartFile;

public class HttpClientUtilTest {

	@Test
	public void test1() {
		for(int i = 0; i < 10; i++) {
			testDoGet();
		}
	}
	
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
}
