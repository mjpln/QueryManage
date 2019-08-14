package com.knowology.km.common.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

public class HttpClientUtil {
	public static Logger logger = Logger.getLogger(HttpClientUtil.class);
	
	public static void sendTestPost() {
		String url = "http://180.153.71.113:3456/test";
		JSONObject param = new JSONObject();
		param.put("MessageData", "");
		param.put("KEY", "TESTKEY");
		param.put("OperationName", "UpdateKbase");
		param.put("OperationCategory", "");
		sendPost(url, param);
	}
	
	public static String sendPost(String url,JSONObject param) {
		String result = "";
		// 获得Http客户端 注意:实际上HttpClient与浏览器是不一样的)
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		// 创建Post请求
		HttpPost httpPost = new HttpPost(url);
//		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(40000).build();//设置请求和传输超时时间
//		httpPost.setConfig(requestConfig);
		
		if(param == null){
			 param = new JSONObject();
		}
		// 将Object转换为json字符串;
		String jsonString = param.toJSONString();
		StringEntity entity = new StringEntity(jsonString, "UTF-8");

		// post请求是将参数放在请求体里面传过去的;这里将entity放入post请求体中
		httpPost.setEntity(entity);
		httpPost.setHeader("Content-Type", "application/json;charset=utf8");
//		httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf8");
		
		logger.info("接口："+url+";参数："+jsonString);
		// 响应模型
		CloseableHttpResponse response = null;
		try {
			// 由客户端执行(发送)Post请求
			response = httpClient.execute(httpPost);
			// 从响应模型中获取响应实体
			HttpEntity responseEntity = response.getEntity();

			logger.info("响应状态为:" + response.getStatusLine());
			if (responseEntity != null) {
				result = EntityUtils.toString(responseEntity,"utf-8");
			}
			logger.info("响应内容为:"+ result);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				// 释放资源
				if (httpClient != null) {
					httpClient.close();
				}
				if (response != null) {
					response.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	public static void main(String[] args) {
		try {
			JSONObject param = new JSONObject();
			param.put("query", "你好");
			param.put("queryCityCode", "全国");
			param.put("servicetype", "电信行业->电信集团->4G业务客服应用");
			String sendPost = sendPost("http://180.153.51.235:8082/QueryManage/interface/generateWordpat.action", param);
			System.out.println(sendPost);
			String encode = URLEncoder.encode("全国", "utf-8");
			System.out.println(encode);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
