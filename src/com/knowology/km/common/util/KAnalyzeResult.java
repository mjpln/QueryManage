package com.knowology.km.common.util;

import com.knowology.km.NLPAppWS.AnalyzeEnterDelegate;
import com.knowology.km.NLPCallerWS.NLPCaller4WSDelegate;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.getServiceClient;

/**
 * 封装返回给NPL客户端的json数据
 */
public class KAnalyzeResult {
	public static NLPCaller4WSDelegate NLPAppWebClient = getServiceClient
			.NLPCaller4WSClient();

	/**
	 * 获取简要分析的结果
	 * 
	 * @param query参数问题
	 * @return 处理完成，封装好的数据
	 */
	public static String getNLPAppWSClientResult(String query) {
		String service = "基金行业->通用组织->演示类应用";
		String channel = "短信";
		// 获取调用接口的入参字符串
		String queryObject = MyUtil.getKAnalyzeQueryObject("userID", query,
				service, channel);
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient.NLPAppWSClient();
		String result = "";
		// 判断接口为null
		if (NLPAppWSClient == null) {
			result = "";
		}
		try {
			result = NLPAppWSClient.analyze(queryObject);
			// 将返回串按照||||来拆分，前一部分当作简要分析的json串
			result = result.split("\\|\\|\\|\\|")[0].replaceAll(
					"(\r\n|\r|\n|\n\r|\t)", "");
		} catch (Exception e) {
			e.printStackTrace();
			result = "";
		}
		return result;
	}

	/**
	 * 调用简要分析(通用组织)结果
	 * 
	 * @param query参数问题
	 * @param channel参数渠道
	 * @param business参数服务
	 * @return 接口入参
	 */
	public static String getNLPAppWSClientResult(String query, String channel,
			String business) {
		business = business.split("->")[0] + "->通用组织->演示类应用";
		// 获取调用接口的入参字符串
		String queryObject = MyUtil.getKAnalyzeQueryObject("userID", query,
				business, channel);
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient.NLPAppWSClient();
		String result = "";
		// 判断接口为null
		if (NLPAppWSClient == null) {
			result = "";
		}
		try {
			result = NLPAppWSClient.analyze(queryObject);
			// 将返回串按照||||来拆分，前一部分当作简要分析的json串
			result = result.split("\\|\\|\\|\\|")[0].replaceAll(
					"(\r\n|\r|\n|\n\r|\t)", "");
		} catch (Exception e) {
			e.printStackTrace();
			result = "";
		}
		return result;
	}

	/**
	 * 通过问题调用高级分析获取结果
	 * 
	 * @param query参数问题
	 * @return 接口返回的结果
	 */
	public static String getNLPAppWebClientResult(String query) {
		// 获取serviceInfo
		String serviceInfo = "{\"MinCredit\":[\"0.1\"],\"MaxAbstractsNumber\":[\"10\"],\"MaxNLPResultPatternNum\":[\"10\"],\"MaxSegmentResultsCnt\":[\"10\"],\"MaxAnalyseSeconds\":[\"60\"],\"ServiceRootIDs\":[\"1804103\",\"1820432\",\"1820132\"]}";
		// 获取四层结构
		String business = "基金行业->嘉实基金->多渠道应用";
		// 获取高级分析的接口串
		String queryObject = MyUtil.getDAnalyzeQueryObject("179", query,
				business, serviceInfo);
		// 调用结果变量
		String result = "";
		// 调用高级分析接口客户端
		NLPCaller4WSDelegate NLPAppWebClient = getServiceClient
				.NLPCaller4WSClient();
		if (NLPAppWebClient == null) {
			result = "";
		} else {
			// 调用接口获取返回值
			result = NLPAppWebClient.detailAnalyze(queryObject);
		}
		return result;
	}

	/**
	 * 通过问题调用高级分析获取结果
	 * 
	 * @param query参数问题
	 * @param business参数服务
	 * @param serviceRootIDs参数业务根ids
	 * @return 接口返回的结果
	 */
	public static String getNLPAppWebClientResult(String query,
			String business, String serviceRootIDs) {
		// 获取serviceInfo
		String serviceInfo = "{\"MinCredit\":[\"0.1\"],\"MaxAbstractsNumber\":[\"10\"],\"MaxNLPResultPatternNum\":[\"10\"],\"MaxSegmentResultsCnt\":[\"10\"],\"MaxAnalyseSeconds\":[\"60\"],\"ServiceRootIDs\":[\""
				+ serviceRootIDs.replace(",", "\",\"") + "\"]}";
		// 获取高级分析的接口串
		String queryObject = MyUtil.getDAnalyzeQueryObject("179", query,
				business, serviceInfo);
		// 调用结果变量
		String result = "";
		// 调用高级分析接口客户端
		NLPCaller4WSDelegate NLPAppWebClient = getServiceClient
				.NLPCaller4WSClient();
		if (NLPAppWebClient == null) {
			result = "";
		} else {
			// 调用接口获取返回值
			result = NLPAppWebClient.detailAnalyze(queryObject);
		}
		return result;
	}
}
