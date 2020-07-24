package com.salama.easyhttp.client.junittest;

import com.salama.easyhttp.client.HttpClientUtil;
import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import java.io.IOException;

public class HttpClientUtilTest {

	public void test1() {
		for(int i = 0; i < 10; i++) {
			testDoGet();
		}
	}

	@Test
	public void testDoGet() {
		//String url = "http://api.dianping.com/v1/business/find_businesses?format=json&city=上海&latitude=31.293330359945465&longitude=121.51796146987469&sort=7&offset_type=1&radius=5000&out_offset_type=1&platform=2&has_deal=1&limit=5&page=1";
		String url = "https://test6.e-pointchina.com/srcb_seckill_web/addlottery/?";
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
