package com.knowology.km.allunderstanding;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.km.NLPAppWS.AnalyzeEnterDelegate;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.bll.GlobalValues;
import com.knowology.km.bll.MetafieldmappingDAO;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.getServiceClient;

public class QueryUnderstander extends Thread{
	private boolean needStop = false;
	private final LinkedBlockingQueue<String> queryQueue;
	private final AtomicInteger counter = new AtomicInteger(0);
	
	private String userid;
	private String wordpatConfPath;
	
	public QueryUnderstander(LinkedBlockingQueue<String> queryQueue) {
		this.queryQueue = queryQueue;
	}

	private void execute(){
		String material = queryQueue.poll();
		if(material == null){
			needStop = true;
			return;
		}

		String queryArray[] = material.split("@#@");
		String queryCityName = queryArray[0];
//		if ("".equals(queryCityCode) || queryCityCode == null) {
//			queryCityCode = "全国";
//		} else {
//			queryCityCode = queryCityCode.replace(",", "|");
//		}
		String query = queryArray[1];
		String kbdataid = queryArray[2];
		String province = queryArray[3];
		String servicetype = queryArray[4];
		String querymanageid = queryArray[5];
		String standAbstract = queryArray[6];
		String nlpServerIP = UnderstandMain.NLP_SERVER_IP_MAP.get(province);
		if(StringUtils.isEmpty(nlpServerIP)){
			nlpServerIP = UnderstandMain.NLP_SERVER_IP_MAP.get("全国");
		}
		
		// 渠道
		String channel = "Web";
		String applycode = "wenfa";
		
		Result rs3 = UserOperResource.getConfigValue("问题库批量理解接口入参配置", servicetype);
		if (rs3 != null && rs3.getRowCount() > 0) {
			Map<String ,String> caMap = new HashMap<String, String>();
			for (int i = 0;i < rs3.getRowCount();i++){
				caMap.put(rs3.getRows()[i].get("name").toString().split("->")[0]
				        , rs3.getRows()[i].get("name").toString().split("->")[1]);
			}
			if (caMap.containsKey("applyCode")){
				applycode = caMap.get("applyCode");
			}
			if (caMap.containsKey("channel")){
				channel = caMap.get("channel");
			}
		}
		
		Map<String,String> tempUr = new HashMap<String,String>();
		
//		// 标准问理解
//		Object object = KAnalyzeByFistResult(userid+Math.random(), servicetype, channel,
//				standAbstract, nlpServerIP, null, queryCityName, "问题库问法测试",applycode);
//		// 同义问理解
//		Object object2 = KAnalyzeByFistResult(userid+Math.random(), servicetype, channel,
//				query, nlpServerIP, null, queryCityName, "问题库问法测试",applycode);
//
//		JSONObject jsonObj = (JSONObject) object;
//		JSONObject jsonObj2 = (JSONObject) object2;
//		
//		String answer1 = "";
//		String answer2 = "";
//		if (jsonObj.getBooleanValue("success")&&jsonObj2.getBooleanValue("success")){
//			// 获取同义问答案
//			answer1 = jsonObj.getJSONArray("result").size()==0 ? "无理解结果" : jsonObj.getJSONArray("result").getJSONObject(0)
//					.getString("answer");
//			// 获取同义问答案
//			answer2 = jsonObj2.getJSONArray("result").size()==0 ? "无理解结果" : jsonObj2.getJSONArray("result").getJSONObject(0)
//					.getString("answer");
//		}
//
//		// 去除<html>标签
//		answer1 = HtmlText(answer1);
//		answer2 = HtmlText(answer2);
//		
//		// 答案过长处理
//		if (answer1.length() > 950) {
//			answer1 = answer1.substring(0, 900) + "...";
//		}
//		if (answer2.length() > 950) {
//			answer2 = answer2.substring(0, 900) + "...";
//		}
		String answer1 = "";
		
		if (tempUr.containsKey(standAbstract + "-" + queryCityName)){
			answer1 = tempUr.get(standAbstract + "-" + queryCityName);
		} else {
			// 标准问理解
			Object object = KAnalyzeByFistResult(userid+Math.random(), servicetype, channel,
					standAbstract, nlpServerIP, null, queryCityName, "问题库问法测试",applycode);
			JSONObject jsonObj = (JSONObject) object;
			// 获取标准问答案
			try{
				answer1 = jsonObj.getJSONArray("result").size()==0 ? "" : jsonObj.getJSONArray("result").getJSONObject(0).getString("answer");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if ("".equals(answer1)){
				answer1 = "无理解结果";
			}
			// 去除<html>标签
			answer1 = HtmlText(answer1);
			// 答案过长处理
			if (answer1.length() > 950) {
				answer1 = answer1.substring(0, 900) + "...";
			}
			tempUr.put(standAbstract + "-" + queryCityName, answer1);
		}
		
		
		// 同义问理解
		Object object2 = KAnalyzeByFistResult(userid+Math.random(), servicetype, channel,
				query, nlpServerIP, null, queryCityName, "问题库问法测试",applycode);
		JSONObject jsonObj2 = (JSONObject) object2;
		// 获取同义问答案
		String answer2 = "";
		try{
			answer2 = jsonObj2.getJSONArray("result").size()==0 ? "" : jsonObj2.getJSONArray("result").getJSONObject(0).getString("answer");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if ("".equals(answer2)){
			answer2 = "无理解结果";
		}
		// 去除<html>标签
		answer2 = HtmlText(answer2);
		// 答案过长处理
		if (answer2.length() > 950) {
			answer2 = answer2.substring(0, 900) + "...";
		}
		// 拼成最后存入数据库的字符串
		String result = "<b style=\"color:red;\">标准问理解结果：</b><br>"
				+ answer1
				+ "<br><br><b style=\"color:red;\">客户问理解结果：</b><br>"
				+ answer2;
		
		String flag = "0";
		
		if (answer1.contains("小知还在成长，没能为您找到答案，不过您可以拨打10000去咨询小知的姐姐哦")){
			flag = "-2";// 无法获取结果
		}else if (answer1.contains("无理解结果")){
			flag = "-2";// 无法获取结果
		}else if (!answer1.equals(answer2)) {
			// 标准问/同义问理解不一致情况
			flag = "-1";
		}
		// 更新到数据库
		int count = CommonLibQueryManageDAO.understand(result, querymanageid,
				flag);
		
		if(count > 0){
			counter.incrementAndGet();
		}
		
		
		
		
		
		
		
		
		
		
//		// 获取高级分析的接口串中的serviceInfo
//		String serviceInfo = MyUtil.getServiceInfo(servicetype, "问题生成词模", "", false, queryCityCode);
//		// 获取高级分析的串
//		String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模",
//				queryArray[1], servicetype, serviceInfo);
//		// 调用生成词模的接口生成词模,可能是多个，以@_@分隔
//		
//		String now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
//		System.out.println(MessageFormat.format("{0} [{1}] 调用NLP: {2}\n参数：{3}", now, getName(), nlpServerIP, queryObject));
//		String wordpat = QuerymanageDAO.getWordpat(queryObject, nlpServerIP);
//		now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
//		System.out.println(MessageFormat.format("{0} [{1}] 调用NLP返回：{2}", now, getName(), wordpat));
//		
//		if (wordpat != null && !"".equals(wordpat)) {
//			// 判断词模是否含有@_@
//			if (wordpat.contains("@_@")) {
//				// 有的话，按照@_@进行拆分,并只取第一个
//				wordpat = wordpat.split("@_@")[0];
//			}
////			// 获取词模中@前面的词模题，在加上@2#编者="问题库"&来源="(当前问题)"
////			wordpat = wordpat.split("@")[0] + "@2#编者=\"问题库\"&来源=\""
////					+ query.replace("&", "\\and") + "\"";
//
//			 //保留自学习词模返回值，并替换 编者=\"自学习\""=>编者="问题库"&来源="(当前问题)" ---> modify 2017-05-24
//			wordpat = wordpat.replace("编者=\"自学习\"", "编者=\"问题库\"&来源=\""+ query.replace("&", "\\and") + "\"");
//			
//			// 校验自动生成的词模是否符合规范
//			if (QuerymanageDAO.CheckWordpat(wordpat, wordpatConfPath)) {
//				List<String> tempList = new ArrayList<String>();
//				tempList.add(wordpat);
//				tempList.add(queryCityCode);
//				tempList.add(query);
//				tempList.add(kbdataid);
//
//				int count = CommonLibQueryManageDAO.insertWordpat2(tempList, servicetype, userid);
//				if(count > 0){
//					counter.incrementAndGet();
//					now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
//					System.out.println(MessageFormat.format("{0} [{1}] 生成成功【kbdataid:{2}, query:{3}, wordpat:{4}】", now, getName(), kbdataid,query,wordpat));
//				}else{
//					now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
//					System.out.println(MessageFormat.format("{0} [{1}] 生成失败【kbdataid:{2}, query:{3}, wordpat:{4}】", now, getName(),  kbdataid,query,wordpat));
//				}
//				
//			} else{
//				now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
//				System.out.println(MessageFormat.format("{0} [{1}] 词模不符合规范【kbdataid:{2}, query:{3}, wordpat:{4}】", now, getName(),  kbdataid,query,wordpat));
//			}
//		}else{
//			now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
//			System.out.println(MessageFormat.format("{0} [{1}] 接口返回词模为空【kbdataid:{2}, query:{3}, wordpat:{4}】", now, getName(),  kbdataid,query,wordpat));
//		}
	}
	
	/**
	 * 根据服务、渠道、用户、问法来获取简要分析的结果
	 * 
	 * @param user参数用户
	 * @param service参数服务
	 * @param channel参数渠道
	 * @param question参数咨询问法
	 * @param ip参数简要分析的地址
	 * @param type测试类型
	 *            如：回归测试
	 * @return 分析结果中的摘要答案json串
	 */
	public static Object KAnalyzeByFistResult(String user, String service,
			String channel, String question, String ip, String province,
			String city, String type, String applycode) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray kNLPResults = new JSONArray();
		// 获取参数配置知识点抽取信息value值
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("知识点继承抽取信息配置", "抽取信息过滤");
		List<String> configValueList = new ArrayList<String>();
		for (int n = 0; n < rs.getRowCount(); n++) {
			String value = rs.getRows()[n].get("name").toString();
			configValueList.add(value);
		}
		// 获取简要分析的客户端
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient
				.NLPAppWSClient(ip);
		
		System.out.println("nlp地址："+ip);
		// 获取调用接口的入参字符串
		String queryObject = getKAnalyzeQueryObject_new(user, question,
				service, channel, province, city, applycode);
		
		System.out.println("入参：" + queryObject);
		// String queryObject = MyUtil.getKAnalyzeQueryObject(user, question,
		// service, channel);
		// 定义返回串的变量
		String result0 = "";
		String result1 = "";
		// 判断接口为null
		if (NLPAppWSClient == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "分析失败");
			return jsonObj;
		}
		try {
			// 定义接口的analyze方法，并返回相应的返回串
			String result = NLPAppWSClient.analyze(queryObject);
			System.out.println("出参：" + result);
			// 将返回串按照||||来拆分，前一部分当作简要分析的json串
			result0 = result.split("\\|\\|\\|\\|")[0].replaceAll(
					"(\r\n|\r|\n|\n\r|\t)", "");
			// 后面一部分当作流程日志的json串
			result1 = result.split("\\|\\|\\|\\|").length < 2 ? "" : result.split("\\|\\|\\|\\|")[1];
			// 流程日志的json串需要进行转义
			result1 = GlobalValues.html(result1);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "分析失败");
			return jsonObj;
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result0) || "".equals(result0)
				|| result0 == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将结果为空信息放入jsonObj的result对象中
			jsonObj.put("result", "结果为空");
			// 将结果为空信息放入jsonObj的result1对象中
			jsonObj.put("result1", "结果为空");
			return jsonObj;
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result1) || "".equals(result1)
				|| result1 == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将结果为空信息放入jsonObj的result对象中
			jsonObj.put("result", "结果为空");
			// 将结果为空信息放入jsonObj的result1对象中
			jsonObj.put("result1", "结果为空");
			return jsonObj;
		}
		try {
			// 将接口返回的json串反序列化为json对象
			JSONObject obj = JSONObject.parseObject(result0);
			// 将obj对象中key为kNLPResults的value变成json数组
			JSONArray kNLPResultsArray = obj.getJSONArray("kNLPResults");
			// 遍历循环kNLPResultsArray数组
			 for (int i = 0; i < kNLPResultsArray.size(); i++){
//			for (int i = 0; i < 1; i++) {
				// 定义一个json对象
				JSONObject o = new JSONObject();
				// 将kNLPResultsArray数组中的第i个转换为json对象
				JSONObject kNLPResultsObj = JSONObject
						.parseObject(kNLPResultsArray.get(i).toString());
				// 遍历取继承词模返回值
				String retrnKeyValue = "";
				JSONArray parasArray = kNLPResultsObj.getJSONArray("paras");
				JSONObject parasKeyValueArray = kNLPResultsObj
						.getJSONObject("parasKeyValue");
				for (int l = 0; l < parasArray.size(); l++) {
					JSONObject parasObj = JSONObject.parseObject(parasArray
							.get(l).toString());
					for (int j = 0; j < configValueList.size(); j++) {
						String key = configValueList.get(j);
						String value = parasObj.getString(key);
						if (value != null && !"".equals(value)) {
							retrnKeyValue = retrnKeyValue + key + "=" + value
									+ "->>";
						}
					}
				}

				// 放入继承词模返回值
				o.put("retrnkeyvalue", retrnKeyValue);
				// 获取kNLPResultsObj对象中credit，并生成credit对象
				// o.put("credit", kNLPResultsObj.getString("credit"));
				// 获取kNLPResultsObj对象中service，并生成service对象
				o.put("service", kNLPResultsObj.getString("service"));
				// 获取kNLPResultsObj对象中answer，并生成answer对象
				o.put("answer", kNLPResultsObj.getString("answer"));
				// 获取kNLPResultsObj对象中abstractStr，并生成abstract对象
				o.put("abstract", kNLPResultsObj.getString("abstractStr"));
				// 获取kNLPResultsObj对象中abstractID，并生成absid对象
				o.put("absid", kNLPResultsObj.getString("abstractID"));
				// 获取kNLPResultsObj对象中abstractStr，并生成topic对象
				o.put("topic", kNLPResultsObj.getString("topic"));
//				o.put("业务路径", parasKeyValueArray.getString("业务路径"));
				// 将生成的对象放入kNLPResults数组中
				kNLPResults.add(o);
			}
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将kNLPResults数组放入jsonObj的result对象中
			jsonObj.put("result", kNLPResults);
			// 将result1放入jsonObj的result1对象中
			jsonObj.put("result1", result1);
			return jsonObj;
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将返回结果解析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "返回结果解析失败");
			// 将返回结果解析失败信息放入jsonObj的result1对象中
			jsonObj.put("result1", "返回结果解析失败");
			return jsonObj;
		}
	}
	
	/**
	 * 获取简要分析的入参字符串方法
	 * 
	 * @param userid参数用户id
	 * @param question参数问题
	 * @param business参数服务
	 * @param channel参数渠道
	 * @return 入参字符串
	 */
	public static String getKAnalyzeQueryObject_new(String userid,
			String question, String business, String channel, String province,
			String city, String applycode) {
		// 定义一个json对象
		JSONObject queryJsonObj = new JSONObject();
		// 将用户id放入queryJsonObj中
		queryJsonObj.put("userID", userid.trim());
		// 将用户咨询的问题放入queryJsonObj中
		queryJsonObj.put("query", question.replace("\"", ".").replace("•", ".")
				.replace("·", ".").replace("\\", "\\\\").trim());
		// 将四层结构放入queryJsonObj中
		queryJsonObj.put("business", business);
		// 将渠道放入queryJsonObj中
		queryJsonObj.put("channel", channel);
		// 获取当前时间
		String callTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
				.format(new Date());
		// 将时间放入queryJsonObj中
		queryJsonObj.put("callTime", callTime);

//		// 定义province的json数组
//		JSONArray provinceJsonArr = new JSONArray();
//		// 将数据放入provinceJsonArr数组中
//		provinceJsonArr.add(province);
//		// 定义provinceJsonObj的json对象
//		JSONObject provinceJsonObj = new JSONObject();
//		// 将provinceJsonArr放入provinceJsonObj中
//		provinceJsonObj.put("Province", provinceJsonArr);

		// 定义city的json数组
		JSONArray cityJsonArr = new JSONArray();
		// 将地市放入cityJsonArr数组中
		cityJsonArr.add(city);
		// 定义city的json对象
		JSONObject cityJsonObj = new JSONObject();
		// 将cityJsonArr数组放入cityJsonObj对象中
		cityJsonObj.put("city", cityJsonArr);

		// 定义applyCode的json数组
		JSONArray applyCodeJsonArr = new JSONArray();
		// 将wenfa放入applyCodeJsonArr数组中
		applyCodeJsonArr.add(applycode);
		// 定义applyCode的json对象
		JSONObject applyCodeJsonObj = new JSONObject();
		// 将applyCodeJsonArr数组放入applyCodeJsonObj对象中
		applyCodeJsonObj.put("applyCode", applyCodeJsonArr);

		// 定义parasjson数组
		JSONArray parasJsonArr = new JSONArray();
		// 将isRecordDBJsonObj放入parasJsonArr中
		parasJsonArr.add(applyCodeJsonObj);
		// 将isRecordDBJsonObj放入parasJsonArr中
//		parasJsonArr.add(provinceJsonObj);
//		 将cityJsonObj放入parasJsonArr中
		parasJsonArr.add(cityJsonObj);
		// 将parasJsonArr放入queryJsonObj中
		queryJsonObj.put("paras", parasJsonArr);

		return queryJsonObj.toJSONString();
	}
	
	/*
	 * 将字符串里面的所有的html标签去掉
	 */
	public static String HtmlText(String inputString) {
		String htmlStr = inputString; // 含html标签的字符串
		String textStr = "";
		java.util.regex.Pattern p_script;
		java.util.regex.Matcher m_script;
		java.util.regex.Pattern p_style;
		java.util.regex.Matcher m_style;
		java.util.regex.Pattern p_html;
		java.util.regex.Matcher m_html;
		try {
			String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; // 定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
																										// }
			String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; // 定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
																									// }
			String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式

			p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
			m_script = p_script.matcher(htmlStr);
			htmlStr = m_script.replaceAll(""); // 过滤script标签

			p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
			m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll(""); // 过滤style标签

			p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll(""); // 过滤html标签

			/* 空格 —— */
			// p_html = Pattern.compile("\\ ", Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = htmlStr.replaceAll(" ", " ");

			textStr = htmlStr;

		} catch (Exception e) {
		}
		return textStr;
	}
	
	
	@Override
	public void run() {
		while(!needStop){
			execute();
		}
		System.out.println("线程[" + getName() + "]运行结束");
	}

	public boolean isNeedStop() {
		return needStop;
	}

	public void setNeedStop(boolean needStop) {
		this.needStop = needStop;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getWordpatConfPath() {
		return wordpatConfPath;
	}

	public void setWordpatConfPath(String wordpatConfPath) {
		this.wordpatConfPath = wordpatConfPath;
	}

	public LinkedBlockingQueue<String> getQueryQueue() {
		return queryQueue;
	}

	public int getCounter() {
		return counter.get();
	}
}