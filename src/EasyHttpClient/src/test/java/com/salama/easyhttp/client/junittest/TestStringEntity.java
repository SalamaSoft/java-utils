package com.salama.easyhttp.client.junittest;

import com.salama.easyhttp.client.HttpClientUtil;
import org.apache.http.client.utils.URLEncodedUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author XingGu_Liu on 2020/1/5.
 */
public class TestStringEntity {

    @Test
    public void test1() {
        String url = "http://127.0.0.1:8070/test/test4";

        try {
            String resp = doPostForm(
                    url,
                    Arrays.asList("p1", "p2"),
                    Arrays.asList("te st+", "测 试+")
                    );
            System.out.println("resp:" + resp);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static String doPostForm(
            String url, List<String> paramNames, List<String> paramValues
    ) throws IOException {
        String formBody = URLEncodedUtils.format(
                HttpClientUtil.makeDoGetParamPairs(
                        paramNames,
                        paramValues
                ),
                "utf-8"
        ).replace("+", "%20");

        String resp = HttpClientUtil.doPostStringEntity(
                url,
                "utf-8",
                "application/x-www-form-urlencoded",
                null,
                formBody
        );
        return resp;
    }
}
