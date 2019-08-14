package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.CommonLibQuestionUploadDao;
import com.knowology.dal.Database;
import com.knowology.km.NLPAppWS.AnalyzeEnterDelegate;
import com.knowology.km.NLPCallerWS.NLPCaller4WSDelegate;
import com.knowology.km.util.Check;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyClass;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.getConfigValue;
import com.knowology.km.util.getServiceClient;
import com.util.XMLResourceBundleControl;

public class QATrainingDAO {
	public static Logger logger = Logger.getLogger("train");
	/**
	 * 获取有商家、组织、应用组成的服务
	 * 
	 * @return 服务的json串
	 */
	public static Object QueryService() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 判断是否需要定制，定制的意思是在服务的下框中，只显示一个值
		if (getConfigValue.isCustom) {
			// 定义一个json对象
			JSONObject obj = new JSONObject();
			// 将1放入id对象中
			obj.put("id", 1);
			// 将定制的服务放入name对象中
			obj.put("name", getConfigValue.service);
			// 将生成的对象放入jsonArr数组中
			jsonArr.add(obj);
			// 将jsonArr数组放入jsonObj的result对象中
			jsonObj.put("result", jsonArr);
		} else {
			// 不需要定制
			// 查询服务商家应用配置表的SQL语句
			String sql = "select * from m_industryapplication2services ";
			try {
				// 执行SQL语句，获取相应的数据源
				Result rs = Database.executeQuery(sql);
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 循环遍历数据源
					for (int i = 0; i < rs.getRowCount(); i++) {
						// 获取商家信息
						String industry = rs.getRows()[i].get("industry")
								.toString();
						// 获取组织信息
						String organization = rs.getRows()[i].get(
								"organization").toString();
						// 获取应用信息
						String application = rs.getRows()[i].get("application")
								.toString();
						// 定义一个json对象
						JSONObject obj = new JSONObject();
						// 生成id对象
						obj.put("id", (i + 1));
						// 生成name对象
						obj.put("name", industry + "->" + organization + "->"
								+ application);
						// 将生成的对象放入jsonArr数组中
						jsonArr.add(obj);
					}
				}
				// 将jsonArr数组放入jsonObj的resu对象中
				jsonObj.put("result", jsonArr);
			} catch (Exception e) {
				e.printStackTrace();
				// 出现错误
				// 清空jsonObj对象
				jsonObj.clear();
				// 清空jsonArr数组
				jsonArr.clear();
				// 将空的jsonArr数组放入jsonObj的result对象中
				jsonObj.put("result", jsonArr);
			}
		}
		return jsonObj;
	}

	/**
	 * 获取咨询的所有渠道
	 * 
	 * @return 渠道json串
	 */
	public static Object GetChannel() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 查询渠道的SQL语句
//		String sql = "select channel from t_channel";
		try {
			// 执行SQL语句，获取相应的数据源
			Result rs = CommonLibMetafieldmappingDAO.getConfigValue("渠道参数配置", "渠道");
//			Result rs = Database.executeQuery(sql);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj.put("id", (i + 1));
					// 生成channel对象
					obj.put("channel",
							rs.getRows()[i].get("name") != null ? rs
									.getRows()[i].get("name").toString()
									: "");
					// 将删除的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的result对象中
			jsonObj.put("result", jsonArr);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 清空jsonObj对象
			jsonObj.clear();
			// 清空jsonArr数组
			jsonArr.clear();
			// 将空的jsonArr数组放入jsonObj的result对象中
			jsonObj.put("result", jsonArr);
		}
		return jsonObj;
	}
	
	/**
	 * 获取应用
	 * 
	 * @return json串
	 */
	public static Object getApp() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		try {
			// 执行SQL语句，获取相应的数据源
			Result rs = CommonLibMetafieldmappingDAO.getConfigKey("applyCode业务渠道编码表配置");
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj.put("id", (i + 1));
					// 生成channel对象
					obj.put("app",
							rs.getRows()[i].get("name") != null ? rs
									.getRows()[i].get("name").toString()
									: "");
					// 将删除的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的result对象中
			jsonObj.put("result", jsonArr);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 清空jsonObj对象
			jsonObj.clear();
			// 清空jsonArr数组
			jsonArr.clear();
			// 将空的jsonArr数组放入jsonObj的result对象中
			jsonObj.put("result", jsonArr);
		}
		return jsonObj;
	}
	
	/**
	 * 获取省份
	 * 
	 * @return json串
	 */
	public static Object getProvince() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		String cityCode ="";
		Result rs =null;
		if (!"全行业".equals(customer)) {
				HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
				.resourseAccess(user.getUserID(), "querymanage", "S");
		     // 该操作类型用户能够操作的资源
		    List<String> cityList = resourseMap.get("地市");
		     if (cityList != null) {
		    cityCode = cityList.get(0);
		    if(!cityCode.endsWith("0000")){//非省份
		    	cityCode = cityCode.substring(0, 2)+"0000";
		    }
		 // 执行SQL语句，获取相应的数据源
		 rs = CommonLibQuestionUploadDao.selProvince(cityCode);
		}	
		}else{
			// 执行SQL语句，获取相应的数据源
			 rs = CommonLibQuestionUploadDao.selProvince();	
		}
		
			
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj.put("id", rs.getRows()[i].get("id"));
					// 生成channel对象
					obj.put("province",
							rs.getRows()[i].get("province") != null ? rs
									.getRows()[i].get("province").toString()
									: "");
					// 将删除的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的result对象中
			jsonObj.put("result", jsonArr);
		
		return jsonObj;
	}

	
	/**
	 * 获取地市
	 * 
	 * @return json串
	 */
	public static Object getCity(String province) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		String cityCode ="";
		Result rs =null;
		if (!"全行业".equals(customer)) {
				HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
				.resourseAccess(user.getUserID(), "scenariosrules", "S");
		     // 该操作类型用户能够操作的资源
		    List<String> cityList = resourseMap.get("地市");
		     if (cityList != null) {
		    cityCode = cityList.get(0);
		    if(!cityCode.endsWith("0000")){//非省份
		    	rs = CommonLibQuestionUploadDao.getOneCity(cityCode);
		    }else{
		    	// 执行SQL语句，获取相应的数据源
				 rs = CommonLibQuestionUploadDao.getCityByProvince(province);
		    	}
		    }
		}else{
			// 执行SQL语句，获取相应的数据源
			 rs = CommonLibQuestionUploadDao.getCityByProvince(province);
			
		}
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj.put("id", rs.getRows()[i].get("id"));
					// 生成channel对象
					obj.put("city",
							rs.getRows()[i].get("city") != null ? rs
									.getRows()[i].get("city").toString()
									: "");
					// 将删除的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的result对象中
			jsonObj.put("result", jsonArr);
		return jsonObj;
	}
	
	/**
	 * 获取分析的接口地址
	 * 
	 * @return json串
	 */
	public static Object GetAnalyzeHttp() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义jsonArrKAnalyze数组
		JSONArray jsonArrKAnalyze = new JSONArray();
		// 定义jsonArrDAnalyze数组
		JSONArray jsonArrDAnalyze = new JSONArray();
		// 获取MsgQueue的配置文件对象
		ResourceBundle resourcesTable = ResourceBundle.getBundle("MsgQueue",XMLResourceBundleControl.INSTANCE);
		try {
			// 定义查询简要分析的接口地址的SQL语句
			String sql = "select t.name,s.name http from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name=?";
			// 定义绑定参数集合
			List<String> lstpara = new ArrayList<String>();
			// 绑定配置名参数
			lstpara.add("简要分析服务地址配置");
			// 执行SQL语句，获取相应的数据源
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义一个json对象
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj.put("id", rs.getRows()[i].get("http"));
					// 生成text对象
					obj.put("text", rs.getRows()[i].get("name"));
					// 将生成的对象放入jsonArrKAnalyze数组中
					jsonArrKAnalyze.add(obj);
				}
			} else {
				// 定义一个json对象
				JSONObject obj = new JSONObject();
				// 生成id对象
				obj.put("id", resourcesTable.getString("NLPAppWSURL"));
				// 生成text对象
				obj.put("text", "本地服务");
				// 将生成的对象放入jsonArrKAnalyze数组中
				jsonArrKAnalyze.add(obj);
			}
			// 将jsonArrKAnalyze数组放入jsonObj的kanalyze对象中
			jsonObj.put("kanalyze", jsonArrKAnalyze);

			// 定义查询高级分析的接口地址的SQL语句
			sql = "select t.name,s.name http from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name=?";
			// 定义绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定配置名参数
			lstpara.add("高级分析服务地址配置");
			// 执行SQL语句，获取相应的数据源
			rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义一个json对象
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj.put("id", rs.getRows()[i].get("http"));
					// 生成text对象
					obj.put("text", rs.getRows()[i].get("name"));
					// 将生成的对象放入jsonArrDAnalyze数组中
					jsonArrDAnalyze.add(obj);
				}
			} else {
				// 定义一个json对象
				JSONObject obj = new JSONObject();
				// 生成id对象
				obj.put("id", resourcesTable.getString("NLPCaller4WSURL"));
				// 生成text对象
				obj.put("text", "本地服务");
				// 将生成的对象放入jsonArrDAnalyze数组中
				jsonArrDAnalyze.add(obj);
			}
			// 将jsonArrDAnalyze数组放入jsonObj的kanalyze对象中
			jsonObj.put("danalyze", jsonArrDAnalyze);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 清空jsonArrDAnalyze数组
			jsonArrDAnalyze.clear();
			// 定义一个json对象
			JSONObject obj = new JSONObject();
			// 生成id对象
			obj.put("id", resourcesTable.getString("NLPAppWSURL"));
			// 生成text对象
			obj.put("text", "本地服务");
			// 将生成的对象放入jsonArrKAnalyze数组中
			jsonArrKAnalyze.add(obj);
			// 将jsonArrKAnalyze数组放入jsonObj的kanalyze对象中
			jsonObj.put("kanalyze", jsonArrKAnalyze);

			// 清空jsonArrDAnalyze数组
			jsonArrDAnalyze.clear();
			// 定义一个json对象
			obj = new JSONObject();
			// 生成id对象
			obj.put("id", resourcesTable.getString("NLPCaller4WSURL"));
			// 生成text对象
			obj.put("text", "本地服务");
			// 将生成的对象放入jsonArrDAnalyze数组中
			jsonArrDAnalyze.add(obj);
			// 将jsonArrDAnalyze数组放入jsonObj的kanalyze对象中
			jsonObj.put("danalyze", jsonArrDAnalyze);
		}
		return jsonObj;
	}

	/**
	 * 根据服务、渠道、用户、问题来获取高级分析的结果
	 * 
	 * @param phone参数用户
	 * @param channel参数服务
	 * @param query参数咨询问题
	 * @param ip参数高级分析地址
	 * @return 分析结果的json串
	 */
	public static Object DetailAnalyzeResult(String phone, String service,
			String query, String ip,String city,String typeName,String isdebug,String province) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 判断用户的咨询是否是乱码
//		if (isMessyCode(query)) {
//			// 将false放入jsonObj的success对象中
//			jsonObj.put("success", false);
//			// 将分析失败信息放入jsonObj的result对象中
//			jsonObj.put("result", "咨询为乱码,请重新输入");
//			return jsonObj;
//		}
		
		String cityCode ="";
		if("".equals(city)||city ==null){
			city =province;	
		}
			Result rs = getConfigValue("地市编码配置",city);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 获取serviceroot信息
				cityCode = rs.getRows()[0].get("name")
						.toString();
			}else{
				cityCode ="全国";
			}
		//ip="http://222.186.101.212:8282/NLPWebService/NLPCallerWS?wsdl"; 
		
		//处理业务库服务名称
		if("serviceDanalyze".equals(typeName)){
			if(service.indexOf("->")!=-1){
				String[] businessArr = service.split("->", 3);
				String industry = businessArr[0];
				service = industry+"->通用商家->业务库应用";
			}
		}
		// 获取高级分析的客户端
		logger.info("高级分析请求地址：" + ip);
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient(ip);
		// 根据服务获取相应的四层结构的接口入参的serviceinfo串
		String serviceInfo = MyUtil.getServiceInfo(service, "高级分析", "", false,cityCode,isdebug);
		//String serviceInfo = MyUtil.getServiceInfo(service, "高级分析", "", false);
		// 获取调用高级分析接口的接口串
		String queryObject = MyUtil.getDAnalyzeQueryObject(phone, query,
				service, serviceInfo);
		if(service.contains("业务库应用")){
			logger.info("业务库高级分析接口的输入串：" + queryObject);
		}else{
			logger.info("高级分析接口的输入串：" + queryObject);
		}
		
		// 定义接口的返回串变量
		String result = "";
		// 判断接口是否为null
		if (NLPCaller4WSClient == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "分析失败");
			return jsonObj;
		}
		try {
			// 定义接口的detailAnalyze方法获取返回串
			result = NLPCaller4WSClient.detailAnalyze(queryObject);
			// 替换掉返回串中的回车符
			result = result.replace("\n", "");
			if(service.contains("业务库应用")){
				logger.info("业务库高级分析接口的输出串：" + result);
			}else{
				logger.info("高级分析接口的输出串：" + result);
			}
			
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
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "结果出现错误");
			return jsonObj;
		}
		try {
			// 将接口返回的json串,反序列化为json数组
			JSONArray jsonArray = JSONArray.parseArray(result);
			// 循环遍历jsonArray数组
			for (int i = 0; i < jsonArray.size(); i++) {
				// 将jsonArray数组中的第i个转换成json对象
				JSONObject obj = JSONObject.parseObject(jsonArray.get(i)
						.toString());
				// 得到多个分词的json串
				// 定义分词的json数组
				JSONArray allSegments = new JSONArray();
				// 将obj对象中key为AllSegments的value变成json数组
				JSONArray allSegmentsArray = obj.getJSONArray("allSegments");
				// 遍历循环arrayAllSegments数组
				for (int j = 0; j < allSegmentsArray.size(); j++) {
					// 获取arrayAllSegments数组中的每一个值
					String segments = allSegmentsArray.getString(j);
					// 判断分词是否含有..(
					if (!segments.contains("...(")) {
						// 根据分词得到分词数
						String wordnum = segments.split("\\)  \\(")[1].replace(
								" words)", "");
						// 得到分词的内容
						String word = segments.split("\\)  \\(")[0] + ")";
						// 定义json对象
						JSONObject o = new JSONObject();
						// 生成wordnum对象
						o.put("wordnum", wordnum);
						// 生成word对象
						o.put("word", word);
						// 将生成的对象放入allSegments数组中
						allSegments.add(o);
					}
				}
				// 得到多个结果的json串
				JSONArray creditResults = new JSONArray();
				// 将obj对象中key为CreditResults的value变成json数组
				JSONArray creditResultsArray = obj
						.getJSONArray("creditResults");
				// 遍历循环creditResultsArray数组
				for (int k = 0; k < creditResultsArray.size(); k++) {
					// 将creditResultsArray数组中第k个转换为json对象
					JSONObject creditResultsObj = JSONObject
							.parseObject(creditResultsArray.get(k).toString());
					// 获取creditResultsObj对象中credit
					String credit = creditResultsObj.getString("credit");
					// 将creditResultsObj对象中key为abstracts的value变成abstractsArray数组
					JSONArray abstractsArray = creditResultsObj
							.getJSONArray("abstracts");
					// 遍历循环abstractsArray数组
					for (int m = 0; m < abstractsArray.size(); m++) {
						// 将abstractsArray数组中第m个转换为json对象
						JSONObject abstractsObj = JSONObject
								.parseObject(abstractsArray.get(m).toString());
						// 定义json对象
						JSONObject o = new JSONObject();
						// 生成分值对象
						o.put("credit", credit);
						// 获取abstractsObj对象中abstractString，并生成摘要对象
						o.put("abstract", abstractsObj
								.getString("abstractString"));
						// 获取abstractsObj对象中topic，并生成主题对象
						o.put("topic", abstractsObj.getString("topic"));
						// 获取abstractsObj对象中service，并生成业务对象
						o.put("service", abstractsObj.getString("service"));
						// 将abstractsObj对象中key为WordPatterns的value变成wordPatternsArray数组
						JSONArray wordPatternsArray = abstractsObj
								.getJSONArray("wordPatterns");
						// 遍历循环abstractsArray数组，只取第一个
						for (int n = 0; n < 1; n++) {
							// 将wordPatternsArray数组中第n个转换为json对象
							JSONObject wordPatternsObj = JSONObject
									.parseObject(wordPatternsArray.get(n)
											.toString());
							// 获取wordPatternsObj对象中patternID，并替换掉双引号
							String patternId = wordPatternsObj.getString(
									"patternID").replace("\"", "");
							// 判断patternId不为空且含有+号
							if (!"".equals(patternId)
									&& patternId.contains("+")) {
								// 将patternId按照+号拆分
								String[] patternids = patternId.split("\\+");
								// 获取模板id，并生成patternid对象
								o.put("patternid", GlobalValues
										.html(patternids[0]));
								// 获取第几个分词，生成segmentstring对象
								o.put("segmentstring", "分词" + patternids[1]);
							} else {
								// patternId为空，不含有+号
								// 生成空的patternid对象
								o.put("patternid", "");
								// 生成分词数为空的segmentstring对象
								o.put("segmentstring", "分词");
							}
							// 获取wordPatternsObj对象中patternString，并替换掉双引号
							String wordpat = wordPatternsObj.getString(
									"patternString").replace("\"", "");
							// 将词模转义一下，并生成patternstring对象
							o.put("patternstring", GlobalValues.html(wordpat));
							// 将词模转换成简单词模(模板)
							String simplewordpat = SimpleString
									.worpattosimworpat(wordpat
											+ "@2#编者=\"自学习\"");
							// 获取简单词模#前面的内容，并生成simplewordpat对象
							o.put("simplewordpat", simplewordpat.split("#")[0]);
							// 获取wordPatternsObj对象中patternType，并生成patterntype对象
							o.put("patterntype", wordPatternsObj
									.getString("patternType"));
							// 获取wordPatternsObj对象中needOrdered，如果NeedOrdered=false，返回无序，true返回有序
							if ("false".equalsIgnoreCase(wordPatternsObj
									.getString("needOrdered"))) {
								// 生成needordered对象
								o.put("needordered", "无序");
							} else {
								// 生成needordered对象
								o.put("needordered", "有序");
							}
							// 定义返回值变量，业务返回值可能是多个
							StringBuilder sb_returnValue = new StringBuilder();
							// 将wordPatternsObj对象中key为entities的value变成entitiesArray数组
							JSONArray entitiesArray = wordPatternsObj
									.getJSONArray("entities");
							// 遍历循环abstractsArray数组
							for (int r = 0; r < entitiesArray.size(); r++) {
								// 将entitiesArray数组中第r个转换为json对象
								JSONObject entitiesObj = JSONObject
										.parseObject(entitiesArray.get(r)
												.toString());
								if (r != 0) {
									// 多个之间有逗号连接
									sb_returnValue.append(",");
								}
								// 将key和value用=连接在一起，作为返回值，可能有多个
								sb_returnValue.append(entitiesObj
										.getString("key")
										+ "=" + entitiesObj.getString("value"));
							}
							// 得到返回值内容，并生成returnvalue对象
							o.put("returnvalue", sb_returnValue.toString());
						}
						// 将删除的对象放入creditResults数组中
						creditResults.add(o);
					}
				}
				// 得到被排除的知识的json串
				// 定义negAbsjson数组
				JSONArray negAbs = new JSONArray();
				// 将obj对象中key为negAbs的value变成negAbsArray数组
				JSONArray negAbsArray = obj.getJSONArray("negAbs");
				// 循环遍历negAbsArray数组
				for (int x = 0; x < negAbsArray.size(); x++) {
					// 获取negAbsArray数组中的第x个
					String negAb = negAbsArray.getString(x);
					// 判断negAb不含有...（
					if (!negAb.contains("...（")) {
						// 定义model1变量
						String model1 = "";
						// 定义业务变量
						String service1 = "";
						// 定义主题变量
						String topic1 = "";
						// 定义摘要变量
						String abstr1 = "";
						// 判断拆分后的个数大于等于2
						if (negAb.split(":").length == 2) {
							// 获取model1，并进行转义
							model1 = GlobalValues.html(negAb.split(":")[1]);
							// 获取摘要
							abstr1 = negAb.split(":")[0];
							// 将摘要按照>拆分，并且拆分后的个数大于等于2
							if (abstr1.split(">").length >= 2) {
								// 获取主题
								topic1 = abstr1.split(">")[1];
								// 将摘要的>的前面内容按照<拆分，并且拆分后的个数大于等于2
								if (abstr1.split(">")[0].split("<").length >= 2) {
									// 获取业务
									service1 = abstr1.split(">")[0].split("<")[1];
								}
							}
						} else {
							// 获取model1，并进行转义
							model1 = GlobalValues.html(negAb.substring(negAb
									.lastIndexOf(":") + 1));
							// 获取摘要
							abstr1 = negAb.substring(0, negAb.lastIndexOf(":"));
							// 将摘要按照>拆分，并且拆分后的个数大于等于2
							if (abstr1.split(">").length >= 2) {
								// 获取主题
								topic1 = abstr1.split(">")[1];
								// 将摘要的>的前面内容按照<拆分，并且拆分后的个数大于等于2
								if (abstr1.split(">")[0].split("<").length >= 2) {
									// 获取业务
									service1 = abstr1.split(">")[0].split("<")[1];
								}
							}
						}
						// 定义json对象
						JSONObject o = new JSONObject();
						// 生成service1对象
						o.put("service1", service1);
						// 生成topic1对象
						o.put("topic1", topic1);
						// 生成abstract1对象
						o.put("abstract1", abstr1);
						// 生成model1对象
						o.put("model1", model1);
						// 将生成的对象放入negAbs数组中
						negAbs.add(o);
					}
				}
				// 得到被加分的知识的json串
				// 定义feaAbsjson数组
				JSONArray feaAbs = new JSONArray();
				// 将obj对象中key为feaAbs的value变成feaAbsArray数组
				JSONArray feaAbsArray = obj.getJSONArray("feaAbs");
				// 循环遍历feaAbsArray数组
				for (int y = 0; y < feaAbsArray.size(); y++) {
					// 获取feaAbsArray数组中的第y个
					String feaAb = feaAbsArray.getString(y);
					// 定义model2变量
					String model2 = "";
					// 定义业务变量
					String service2 = "";
					// 定义主题变量
					String topic2 = "";
					// 定义摘要变量
					String abstr2 = "";
					// 判断feaAb含有：号
					if (feaAb.contains(":")) {
						// 判断按照：拆分后的个数大于等于2
						if (feaAb.split(":").length >= 2) {
							// 获取model2
							model2 = feaAb.split(":")[1];
							// 获取摘要
							abstr2 = feaAb.split(":")[0];
							// 将摘要按照>拆分，并且拆分后的个数大于等于2
							if (abstr2.split(">").length >= 2) {
								// 获取主题
								topic2 = abstr2.split(">")[1];
								// 将摘要的>的前面内容按照<拆分，并且拆分后的个数大于等于2
								if (abstr2.split(">")[0].split("<").length >= 2) {
									// 获取业务
									service2 = abstr2.split(">")[0].split("<")[1];
								}
							}
						}
					} else {
						// feaAb不含有：号
						// 判断按照\\(拆分后的个数大于等于2
						if (feaAb.split("\\(").length >= 2) {
							// 获取model2
							model2 = feaAb.split("\\(")[1].replace(")", "");
							// 获取摘要
							abstr2 = feaAb.split("\\(")[0];
							// 将摘要按照>拆分，并且拆分后的个数大于等于2
							if (abstr2.split(">").length >= 2) {
								// 获取主题
								topic2 = abstr2.split(">")[1];
								// 将摘要的>的前面内容按照<拆分，并且拆分后的个数大于等于2
								if (abstr2.split(">")[0].split("<").length >= 2) {
									// 获取业务
									service2 = abstr2.split(">")[0].split("<")[1];
								}
							}
						}
					}
					// 定义json对象
					JSONObject o = new JSONObject();
					// 生成service1对象
					o.put("service2", service2);
					// 生成topic1对象
					o.put("topic2", topic2);
					// 生成abstract1对象
					o.put("abstract2", abstr2);
					// 生成model1对象
					o.put("model2", model2);
					// 将生成的对象放入feaAbs数组中
					feaAbs.add(o);
				}
				// 得到被置顶的知识的json串
				// 定义seaAbsjson数组
				JSONArray seaAbs = new JSONArray();
				// 将obj对象中key为seaAbs的value变成seaAbsArray数组
				JSONArray seaAbsArray = obj.getJSONArray("seaAbs");
				// 循环遍历seaAbsArray数组
				for (int z = 0; z < seaAbsArray.size(); z++) {
					// 获取seaAbsArray数组中的第y个
					String seaAb = seaAbsArray.getString(z);
					// 定义model3变量
					String model3 = "";
					// 定义业务变量
					String service3 = "";
					// 定义主题变量
					String topic3 = "";
					// 定义摘要变量
					String abstr3 = "";
					// 判断feaAb含有：号
					if (seaAb.indexOf(":") != -1) {
						// 判断按照：拆分后的个数大于等于2
						if (seaAb.split(":").length >= 2) {
							// 获取model3
							model3 = seaAb.split(":")[1];
							// 获取摘要
							abstr3 = seaAb.split(":")[0];
							// 将摘要按照>拆分，并且拆分后的个数大于等于2
							if (abstr3.split(">").length >= 2) {
								// 获取主题
								topic3 = abstr3.split(">")[1];
								// 将摘要的>的前面内容按照<拆分，并且拆分后的个数大于等于2
								if (abstr3.split(">")[0].split("<").length >= 2) {
									// 获取业务
									service3 = abstr3.split(">")[0].split("<")[1];
								}
							}
						}
					}
					// 定义json对象
					JSONObject o = new JSONObject();
					// 生成service1对象
					o.put("service3", service3);
					// 生成topic1对象
					o.put("topic3", topic3);
					// 生成abstract1对象
					o.put("abstract3", abstr3);
					// 生成model1对象
					o.put("model3", model3);
					// 将生成的对象放入seaAbs数组中
					seaAbs.add(o);
				}
				// 得到被排除子句的json串
				// 定义negClauseNamejson数组
				JSONArray negClauseName = new JSONArray();
				// 将obj对象中key为NegClauseName的value变成negClauseNameArray数组
				JSONArray negClauseNameArray = obj
						.getJSONArray("NegClauseName");
				// 判断negClauseNameArray数组是否为null
				if (negClauseNameArray != null) {
					// 获取遍历negClauseNameArray数组
					for (int r = 0; r < negClauseNameArray.size(); r++) {
						// 将第r个被排除的子句转义后放入negClauseNamejson数组中
						negClauseName.add(GlobalValues.html(negClauseNameArray
								.getString(r)));
					}
				}
				// 定义json对象
				JSONObject o = new JSONObject();
				// 生成allsegments对象
				o.put("allsegments", allSegments);
				// 生成creditresults对象
				o.put("creditresults", creditResults);
				// 生成negabs对象
				o.put("negabs", negAbs);
				// 生成feaabs对象
				o.put("feaabs", feaAbs);
				// 生成seaabs对象
				o.put("seaabs", seaAbs);
				// 生成被排除子句对象
				o.put("negClauseName", negClauseName);
				// 将生成的对象放入jsonArr数组中
				jsonArr.add(o);
			}
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将jsonArr放入jsonObj的result对象中
			jsonObj.put("result", jsonArr);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将返回结果解析失败放入jsonObj的result对象中
			jsonObj.put("result", "返回结果解析失败");
		}
		return jsonObj;
	}

	
	

	/**
	 * 通过扩展问题调用高级分析接口，返回摘要
	 * 
	 * @param queryObject参数接口的入参
	 * @return 摘要字符串
	 */
	public static String DetailAnalyzeResultAbs(String queryObject) {
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient();
		// 定义接口的返回串内容变量
		String result = "";
		// 判断接口为null
		if (NLPCaller4WSClient == null) {
			// 返回空字符串
			return "";
		}
		try {
			// 定义接口的oldDetailAnalyze，并返回接口的返回串
			result = NLPCaller4WSClient.oldDetailAnalyze(queryObject);
			// 将返回串中的回车符替换为空
			result = result.replace("\n", "");
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误，返回空字符串
			return "";
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 返回空字符串
			return "";
		}
		// 定义摘要变量
		String sb_abstract = "";
		try {
			// 将接口返回的json串,反序列化为json数组
			JSONArray jsonArray = JSONArray.parseArray(result);
			// 循环遍历jsonArray数组
			for (int i = 0; i < jsonArray.size(); i++) {
				// 将jsonArray数组中 的第i个转换为json对象
				JSONObject obj = JSONObject.parseObject(jsonArray.get(i)
						.toString());
				// 将obj对象中key为creditResults的value变成json数组
				JSONArray creditResultsArray = obj
						.getJSONArray("creditResults");
				// 判断creditResultsArray数组的数组是否大于0
				if (creditResultsArray.size() > 0) {
					// 遍历循环creditResultsArray数组，只取第一个
					for (int k = 0; k < 1; k++) {
						// 将creditResultsArray数组中的第k个转换为json对象
						JSONObject creditResultsObj = JSONObject
								.parseObject(creditResultsArray.get(k)
										.toString());
						// 将creditResultsObj对象中key为Abstracts的value变成abstractsArray数组
						JSONArray abstractsArray = creditResultsObj
								.getJSONArray("abstracts");
						// 判断abstractsArray数组的个数是否大于0
						if (abstractsArray.size() > 0) {
							// 遍历循环abstractsArray数组，只取第一个
							for (int m = 0; m < 1; m++) {
								// 将abstractsArray数组中的第m个转换为json对象
								JSONObject abstractsObj = JSONObject
										.parseObject(abstractsArray.get(m)
												.toString());
								// 获取abstractsObj对象中AbstractString
								sb_abstract = abstractsObj
										.getString("abstractString");
							}
						}
					}
				}
			}
			return sb_abstract;
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误，返回空字符串
			return "";
		}
	}

	/**
	 *@description 非问题库摘要调用高级分析接口，返回问题库理解信息
	 *@param queryObject 参数接口的入参
	 *@return 
	 *@returnType String 
	 */
	public static JSONObject DetailAnalyzeResultForInherit(String queryObject){
		// 定义json对象
		JSONObject o = new JSONObject();
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient();
		// 定义接口的返回串内容变量
		String result = "";
		// 判断接口为null
		if (NLPCaller4WSClient == null) {
			// 返回空字符串
			return o;
		}
		try {
			// 定义接口的oldDetailAnalyze，并返回接口的返回串
			result = NLPCaller4WSClient.detailAnalyze(queryObject);
			// 将返回串中的回车符替换为空
			result = result.replace("\n", "");
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误，返回空字符串
			return o;
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 返回空字符串
			return o;
		}
	
		//获得继承信息返回值key
		//获取参数配置知识点抽取信息value值
		Result rs = MetafieldmappingDAO.getConfigValue("知识点继承抽取信息配置","抽取信息补全");
		Map<String,String> configValue = new HashMap<String,String>();
		for (int n = 0; n < rs.getRowCount(); n++) {
			String key = rs.getRows()[n].get("name").toString();
			configValue.put(key, "");
		}
		
		// 定义摘要变量
		String sb_abstract = "";
		try {
			// 将接口返回的json串,反序列化为json数组
			JSONArray jsonArray = JSONArray.parseArray(result);
			// 循环遍历jsonArray数组
			for (int i = 0; i < jsonArray.size(); i++) {
				// 将jsonArray数组中 的第i个转换为json对象
				JSONObject obj = JSONObject.parseObject(jsonArray.get(i)
						.toString());
				// 将obj对象中key为creditResults的value变成json数组
				JSONArray creditResultsArray = obj
						.getJSONArray("creditResults");
				// 遍历循环creditResultsArray数组
//				for (int k = 0; k < creditResultsArray.size(); k++){
					for (int k = 0; k < 1; k++){
					// 将creditResultsArray数组中第k个转换为json对象
					JSONObject creditResultsObj = JSONObject
							.parseObject(creditResultsArray.get(k).toString());
					// 获取creditResultsObj对象中credit
					String credit = creditResultsObj.getString("credit");
					// 将creditResultsObj对象中key为abstracts的value变成abstractsArray数组
					JSONArray abstractsArray = creditResultsObj
							.getJSONArray("abstracts");
					// 遍历循环abstractsArray数组
					for (int m = 0; m < abstractsArray.size(); m++){
//						for (int m = 0; m < 1; m++) {
						// 将abstractsArray数组中第m个转换为json对象
						JSONObject abstractsObj = JSONObject
								.parseObject(abstractsArray.get(m).toString());
						
						// 生成分值对象
						o.put("credit", credit);
						// 获取abstractsObj对象中abstractString，并生成摘要对象
						o.put("abstract", abstractsObj
								.getString("abstractString"));
						o.put("abstractID", abstractsObj
								.getString("abstractID"));
						// 获取abstractsObj对象中topic，并生成主题对象
						o.put("topic", abstractsObj.getString("topic"));
						// 获取abstractsObj对象中service，并生成业务对象
						o.put("service", abstractsObj.getString("service"));
						// 将abstractsObj对象中key为WordPatterns的value变成wordPatternsArray数组
						JSONArray wordPatternsArray = abstractsObj
								.getJSONArray("wordPatterns");
						// 遍历循环abstractsArray数组，只取第一个
						for (int n = 0; n < 1; n++) {
							// 将wordPatternsArray数组中第n个转换为json对象
							JSONObject wordPatternsObj = JSONObject
									.parseObject(wordPatternsArray.get(n)
											.toString());
							// 获取wordPatternsObj对象中patternID，并替换掉双引号
							String patternId = wordPatternsObj.getString(
									"patternID").replace("\"", "");
							// 判断patternId不为空且含有+号
							if (!"".equals(patternId)
									&& patternId.contains("+")) {
								// 将patternId按照+号拆分
								String[] patternids = patternId.split("\\+");
								// 获取模板id，并生成patternid对象
								o.put("patternid", GlobalValues
										.html(patternids[0]));
								// 获取第几个分词，生成segmentstring对象
								o.put("segmentstring", "分词" + patternids[1]);
							} else {
								// patternId为空，不含有+号
								// 生成空的patternid对象
								o.put("patternid", "");
								// 生成分词数为空的segmentstring对象
								o.put("segmentstring", "分词");
							}
							// 获取wordPatternsObj对象中patternString，并替换掉双引号
							String wordpat = wordPatternsObj.getString(
									"patternString").replace("\"", "");
							// 将词模转义一下，并生成patternstring对象
							o.put("patternstring", GlobalValues.html(wordpat));
							// 将词模转换成简单词模(模板)
							String simplewordpat = SimpleString
									.worpattosimworpat(wordpat
											+ "@2#编者=\"自学习\"");
							// 获取简单词模#前面的内容，并生成simplewordpat对象
							o.put("simplewordpat", simplewordpat.split("#")[0]);
							// 获取wordPatternsObj对象中patternType，并生成patterntype对象
							o.put("patterntype", wordPatternsObj
									.getString("patternType"));
							// 获取wordPatternsObj对象中needOrdered，如果NeedOrdered=false，返回无序，true返回有序
							if ("false".equalsIgnoreCase(wordPatternsObj
									.getString("needOrdered"))) {
								// 生成needordered对象
								o.put("needordered", "无序");
							} else {
								// 生成needordered对象
								o.put("needordered", "有序");
							}
							// 定义返回值变量，业务返回值可能是多个
							StringBuilder sb_returnValue = new StringBuilder();
							// 将wordPatternsObj对象中key为entities的value变成entitiesArray数组
							JSONArray entitiesArray = wordPatternsObj
									.getJSONArray("entities");
							// 遍历循环abstractsArray数组
							for (int r = 0; r < entitiesArray.size(); r++) {	// 将entitiesArray数组中第r个转换为json对象
								JSONObject entitiesObj = JSONObject
								.parseObject(entitiesArray.get(r)
										.toString());
//						if (r != 0) {
//							// 多个之间有逗号连接
//							sb_returnValue.append(",");
//						}
						// 将key和value用=连接在一起，作为返回值，可能有多个
						//取配置表中返回值key包含的键 key
						String rkey = entitiesObj.getString("key");
						if(configValue.containsKey(rkey)){
							sb_returnValue.append(rkey+ "=" + entitiesObj.getString("value")+",");
						}
					  }
							
							// 得到返回值内容，并生成returnvalue对象
							o.put("returnvalue", sb_returnValue.toString());
						}
					}
				}
				
			}
			return o;
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误，返回空字符串
			return o;
		}
	
	}
	
	
	/**
	 * 根据服务、渠道、用户、问题来获取简要分析的结果
	 * 
	 * @param user参数用户
	 * @param service参数服务
	 * @param channel参数渠道
	 * @param question参数咨询问题
	 * @param ip参数简要分析的地址
	 * @param app 应用
	 * @param province 省份
	 * @return 分析结果的json串
	 */
	public static Object KAnalyzeResult(String user, String service,
			String channel, String question, String ip,String city,String app,String province) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray kNLPResults = new JSONArray();
		// 判断用户的咨询是否是乱码
//		if (isMessyCode(question)) {
//			// 将false放入jsonObj的success对象中
//			jsonObj.put("success", false);
//			// 将分析失败信息放入jsonObj的result对象中
//			jsonObj.put("result", "咨询为乱码,请重新输入");
//			return jsonObj;
//		}
		if("".equals(city)||city==null){
			city=province;
		}
		//获取参数配置知识点抽取信息value值
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("知识点继承抽取信息配置","抽取信息过滤");
		List<String> configValueList = new ArrayList<String>();
		for (int n = 0; n < rs.getRowCount(); n++) {
			String value = rs.getRows()[n].get("name").toString();
			configValueList.add(value);
		}
		//ip = "http://222.186.101.212:8282/NLPAppWS/AnalyzeEnterPort?wsdl";
//		ip ="http://221.230.19.75:9191/NLPAppWS/AnalyzeEnterPort?wsdl";
//		ip = getConfigValue.jianxidizhi;
		// 获取简要分析的客户端
		logger.info("简要分析请求地址：" + ip);
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient
				.NLPAppWSClient(ip);
		// 获取调用接口的入参字符串
		String queryObject = MyUtil.getKAnalyzeQueryObject_new(user, question,
				service, channel, city,app,province);
//		String queryObject = MyUtil.getKAnalyzeQueryObject(user, question,
//				service, channel);
		logger.info("简要分析接口的输入串：" + queryObject);
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
//		String username = MyClass.LoginUserName();
		try {
			// 定义接口的analyze方法，并返回相应的返回串
			String result = NLPAppWSClient.analyze(queryObject);
			logger.info("简要分析接口的输出串：" + result);
			// 将返回串按照||||来拆分，前一部分当作简要分析的json串
			result0 = result.split("\\|\\|\\|\\|")[0].replaceAll(
					"(\r\n|\r|\n|\n\r|\t)", "");
			// 后面一部分当作流程日志的json串
			result1 = result.split("\\|\\|\\|\\|")[1];
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
			for (int i = 0; i < kNLPResultsArray.size(); i++) {
				// 定义一个json对象
				JSONObject o = new JSONObject();
				// 将kNLPResultsArray数组中的第i个转换为json对象
				JSONObject kNLPResultsObj = JSONObject
						.parseObject(kNLPResultsArray.get(i).toString());
				//遍历取继承词模返回值
				String retrnKeyValue ="";
				JSONArray parasArray = kNLPResultsObj.getJSONArray("paras");
//				for(int l = 0; l < parasArray.size(); l++){
//					JSONObject parasObj = JSONObject.parseObject(parasArray.get(i).toString());
//					for(int j =0 ;j<configValueList.size();j++){
//						String key = configValueList.get(j);
//						JSONArray value = parasObj.getJSONArray(key);
//						if(value!=null &&!"".equals(value)){
//						 retrnKeyValue = retrnKeyValue + key+"="+value.get(0)+"->>";	
//						}
//					}
//				}
				
				for(int l = 0; l < parasArray.size(); l++){
					JSONObject parasObj = JSONObject.parseObject(parasArray.get(l).toString());
					for(int j =0 ;j<configValueList.size();j++){
						String key = configValueList.get(j);
						String value = parasObj.getString(key);
						if(value!=null &&!"".equals(value)){
						 retrnKeyValue = retrnKeyValue + key+"="+value+"->>";	
						}
					}
				}
				
				//放入继承词模返回值
				o.put("retrnkeyvalue", retrnKeyValue);	
				// 获取kNLPResultsObj对象中credit，并生成credit对象
				o.put("credit", kNLPResultsObj.getString("credit"));
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
	 * 扩展问题咨询，返回摘要
	 * 
	 * @param user参数用户
	 * @param service参数服务
	 * @param channel参数渠道
	 * @param question参数咨询问题
	 * @return 转义字符串
	 */
	public static String KAnalyzeAbs(String user, String service,
			String channel, String question) {
		// 获取简要分析的客户端
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient.NLPAppWSClient();
		// 获取接口的入参字符串
		String queryObject = MyUtil.getKAnalyzeQueryObject(user, question,
				service, channel);
		// 定义接口的返回串变量
		String result = "";
		// 判断接口为null
		if (NLPAppWSClient == null) {
			// 返回空字符串
			return "";
		}
		try {
			// 定义接口的analyze方法，并返回相应的返回串
			result = NLPAppWSClient.analyze(queryObject);
			// 将返回串按照||||拆分，并只取前面一部分，并且替换掉所有的回车符、换行符、tab键
			result = result.split("\\|\\|\\|\\|")[0].replaceAll(
					"(\r\n|\r|\n|\n\r|\t)", "");
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误，返回空字符串
			return "";
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 返回空字符串
			return "";
		}
		try {
			// 将接口返回的json串反序列化为json对象
			JSONObject obj = JSONObject.parseObject(result);
			// 将obj对象中key为kNLPResults的value变成json数组
			JSONArray kNLPResultsArray = obj.getJSONArray("kNLPResults");
			// 定义摘要变量
			String _abstract = "";
			// 遍历循环kNLPResultsArray数组，只取第一个
			for (int i = 0; i < 1; i++) {
				// 将kNLPResultsArray中的第i个转换为json对象
				JSONObject kNLPResultsObj = JSONObject
						.parseObject(kNLPResultsArray.get(i).toString());
				// 获取kNLPResultsObj对象中abstractStr
				_abstract = kNLPResultsObj.getString("abstractStr");
			}
			return _abstract;
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误，返回空字符串
			return "";
		}
	}

	/**
	 * 扩展问题咨询，返回摘要结合
	 * 
	 * @param user参数用户
	 * @param service参数服务
	 * @param channel参数渠道
	 * @param question参数咨询问题
	 * @return 转义字符串
	 */
	public static List<String> KAnalyzeListAbs(String user, String userCity, String userProvince, String service, String app,
			String channel, String question) {
		// 获取简要分析的客户端
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient.NLPAppWSClient();
		// 定义存放摘要集合变量
		List<String> list = new ArrayList<String>();
		// 获取接口的入参字符串
		String queryObject = MyUtil.getKAnalyzeQueryObject_new(user, question, service, channel, userCity, app, userProvince);
		// 定义接口的返回串变量
		String result = "";
		// 判断接口为null
		if (NLPAppWSClient == null) {
			// 返回空结合
			return list;
		}
		try {
			// 定义接口的analyze方法，并返回相应的返回串
			result = NLPAppWSClient.analyze(queryObject);
			// 将返回串按照||||拆分，并只取前面一部分，并且替换掉所有的回车符、换行符、tab键
			result = result.split("\\|\\|\\|\\|")[0].replaceAll(
					"(\r\n|\r|\n|\n\r|\t)", "");
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误，返回空结合
			return list;
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 返回空结合
			return list;
		}
		try {
			// 将接口返回的json串反序列化为json对象
			JSONObject obj = JSONObject.parseObject(result);
			// 将obj对象中key为kNLPResults的value变成json数组
			JSONArray kNLPResultsArray = obj.getJSONArray("kNLPResults");
			// 定义摘要变量
			String _abstract = "";
			// 遍历循环kNLPResultsArray数组，只取第一个
			for (int i = 0; i < kNLPResultsArray.size(); i++) {
				// 将kNLPResultsArray中的第i个转换为json对象
				JSONObject kNLPResultsObj = JSONObject
						.parseObject(kNLPResultsArray.get(i).toString());
				// 获取kNLPResultsObj对象中abstractStr
				_abstract = kNLPResultsObj.getString("abstractStr");
				// 将摘要放入集合中
				list.add(_abstract);
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误，返回空字符串
			return list;
		}
	}

	/**
	 * 调用接口，更新知识库，并返回相应的信息
	 * 
	 * @param ip参数ip地址
	 * @return json串
	 */
	public static Object updateKB(String ip) {
		//ip="http://222.186.101.212:8282/NLPWebService/NLPCallerWS?wsdl";
		logger.info("更新知识库地址: "+ip);
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient(ip);
		// 判断接口为null
		if (NLPCaller4WSClient == null) {
			// 将分析失败放入jsonObj的result对象中
			jsonObj.put("result", "分析失败！");
			return jsonObj;
		}
		String username = MyClass.LoginUserName();
		try {
			logger.info("【"+username+"】更新知识库请求开始......");
			// 调用接口的updateKB方法
			NLPCaller4WSClient.updateKB();
			logger.info("【"+username+"】更新知识库请求结束......");
			// 将更新完毕放入jsonObj的result对象中
			jsonObj.put("result", "更新完毕！");
			return jsonObj;
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将更新出现错误放入jsonObj的result对象中
			jsonObj.put("result", "更新出现错误！");
			logger.info("【"+username+"】更新知识库请求异常......");
			return jsonObj;
		}
	}

	/**
	 * 更新线上知识库
	 * 
	 * @return json串
	 */
	public static Object updateOnline() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义存放更新线上知识库的信息的集合
		List<String> infoList = new ArrayList<String>();
		// 定义存放服务器名称和接口地址的集合变量
		Map<String, String> ipHttpMap = new HashMap<String, String>();
		// 获取MsgQueue的配置文件对象
		ResourceBundle resourcesTable = ResourceBundle.getBundle("MsgQueue",XMLResourceBundleControl.INSTANCE);
		try {
			// 定义查询高级分析的接口地址的SQL语句
			String sql = "select t.name,s.name http from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name=? and t.name!=?";
			// 定义绑定参数集合
			List<String> lstpara = new ArrayList<String>();
			// 绑定配置名参数
			lstpara.add("高级分析服务地址配置");
			// 绑定本地服务参数
			lstpara.add("本地服务");
			// 执行SQL语句，获取相应的数据源
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 获取服务器名称
					String name = rs.getRows()[i].get("name").toString();
					// 获取接口地址
					String http = rs.getRows()[i].get("http").toString();
					// 判断集合是否含有当前服务器名称
					if (!ipHttpMap.containsKey(name)) {
						// 将服务器名称和接口地址放入集合中
						ipHttpMap.put(name, http);
					}
				}
			} else {
				// 将服务器名称和接口地址放入集合中
				ipHttpMap.put("本地服务", resourcesTable
						.getString("NLPCaller4WSURL"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 将服务器名称和接口地址放入集合中
			ipHttpMap.put("本地服务", resourcesTable.getString("NLPCaller4WSURL"));
		}
		// 循环遍历服务器名称和接口地址集合
		for (String key : ipHttpMap.keySet()) {
			// 获取高级分析的客户端
			NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
					.NLPCaller4WSClient(ipHttpMap.get(key));
			// 判断接口为null
			if (NLPCaller4WSClient == null) {
				// 将分析失败放入集合中
				infoList.add(key + "分析失败！");
			}
			try {
				// 调用接口的updateKB方法
				NLPCaller4WSClient.updateKB();
				// 将更新完毕放入集合中
				infoList.add(key + "更新完毕！");
			} catch (Exception e) {
				e.printStackTrace();
				// 出现错误，将更新出现错误放入集合中
				infoList.add(key + "更新出现错误！");
			}
		}
		// 将信息集合放入jsonObj的result对象中
		jsonObj.put("result", StringUtils.join(infoList, "<br/>"));
		return jsonObj;
	}

	/**
	 * 判断是否要定制
	 * 
	 * @return
	 */
	public static Object findConfigure() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获取的登录时的服务
		String service = MyClass.IndustryOrganizationApplication();
		// 判断登录时服务为null或者为" "
		if (service == null || " ".equals(service)) {
			// 从jdbc.properties配置文件获取key为regress.channel的value，赋值给服务
			service = com.knowology.km.dal.Database.getJDBCValues("regress.channel");
		}
		// 判断是否需要定制
		if (getConfigValue.isCustom) {
			// 定义一个json对象
			JSONObject obj = new JSONObject();
			// 生成channel对象
			obj.put("channel", getConfigValue.channel);
			// 生成service对象
			obj.put("service", service);
			// 将生成的对象放入jsonObj的result对象中
			jsonObj.put("result", obj);
		} else {
			// 定义一个json对象
			JSONObject obj = new JSONObject();
			// 生成channel对象
			obj.put("channel", "Web");
			// 生成service对象
			obj.put("service", service);
			// 将生成的对象放入jsonObj的result对象中
			jsonObj.put("result", obj);
		}
		return jsonObj;
	}

	/**
	 * 将简要分析的第一个摘要的咨询问题保存到回归库中
	 * 
	 * @param extendquestion参数扩展问题
	 *            (咨询问题)
	 * @param question参数标准问题
	 *            (摘要>后面的内容)
	 * @return 添加后的json串
	 */
	public static Object Regresstest(String extendquestion, String question) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义查询回归库中扩展问题和标准问题是否存在的SQL语句
		String sql = "select * from regressquery where extendquestion=? and question=?";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定扩展问题参数
		lstpara.add(extendquestion);
		// 绑定标准问题参数
		lstpara.add(question);
		try {
			// 执行SQL语句，获取相应的数据源
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 将扩展问题已存在放入jsonObj的result对象中
				jsonObj.put("result", "扩展问题已存在");
				return jsonObj;
			}
			// 获取的登录时的服务
			String servicetype = MyClass.IndustryOrganizationApplication();
			// 判断登录时服务为null或者为" "
			if (servicetype == null || " ".equals(servicetype)) {
				// 从jdbc.properties配置文件获取key为regress.channel的value，赋值给服务
				servicetype = com.knowology.km.dal.Database.getJDBCValues("regress.channel");
			}
			// 定义插入回归库的SQL语句
			sql = "insert into regressquery(question,extendquestion,servicetype) values(?,?,?) ";
			// 定义绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定标准问题参数
			lstpara.add(question);
			// 绑定扩展问题参数
			lstpara.add(extendquestion);
			// 绑定业务类型参数
			lstpara.add(servicetype);
			// 执行SQL语句，绑定事务处理，并返回事务处理的结果
			int c = Database.executeNonQuery(sql, lstpara.toArray());
			// 判断事务处理的结果
			if (c > 0) {
				// 事务处理成功
				// 将添加到回归测试库成功放入jsonObj的result对象中
				jsonObj.put("result", "添加到回归测试库成功");
			} else {
				// 事务处理失败
				// 将添加到回归测试库失败放入jsonObj的result对象中
				jsonObj.put("result", "添加到回归测试库失败");
			}
			return jsonObj;
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将添加到回归测试库失败放入jsonObj的result对象中
			jsonObj.put("result", "添加到回归测试库失败");
			return jsonObj;
		}
	}

	/**
	 * 根据服务、用户、问题来获取相似问题分析的结果
	 * 
	 * @param phone参数用户
	 * @param service参数服务
	 * @param query参数咨询问题
	 * @param ip参数接口地址
	 * @return 分析的json串
	 */
	public static Object similarDetailAnalyzeResult(String phone,
			String service, String query, String ip,String city) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 获取高级分析的客户端
		//ip="http://222.186.101.213:8282/NLPWebService/NLPCallerWS?wsdl";
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient(ip);
		String cityCode ="";
		if("".equals(city)||city ==null){
			cityCode ="全国";
		}else{
			Result rs = getConfigValue("地市编码配置",city);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 获取serviceroot信息
				cityCode = rs.getRows()[0].get("name")
						.toString();
			}else{
				cityCode ="全国";
			}
		}
		// 获取调用高级分析接口的接口串
		String serviceInfo = MyUtil.getServiceInfo(service, "相似问题", "", false,cityCode);
		// 获取调用接口的入参字符串
		String queryObject = MyUtil.getDAnalyzeQueryObject(phone, query,
				service, serviceInfo);
		logger.info("相似问题分析接口的输入串：" + queryObject);
		// 定义接口的返回串变量
		String result = "";
		// 判断接口是否为null
		if (NLPCaller4WSClient == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "分析失败");
			return jsonObj;
		}
		try {
			// 定义接口的detailAnalyze方法获取返回串
			result = NLPCaller4WSClient.detailAnalyze(queryObject);
			// 替换掉返回串中的回车符
			result = result.replace("\n", "");
			logger.info("相似问题分析接口的输出串：" + result);
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
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "结果出现错误");
			return jsonObj;
		}
		try {
			// 将接口返回的json串,反序列化为json数组
			JSONArray jsonArray = JSONArray.parseArray(result);
			// 循环遍历jsonArray数组
			for (int i = 0; i < jsonArray.size(); i++) {
				// 将jsonArray数组中的第i个转换成json对象
				JSONObject obj = JSONObject.parseObject(jsonArray.get(i)
						.toString());
				// 得到多个分词的json串
				// 定义分词的json数组
				JSONArray allSegments = new JSONArray();
				// 将obj对象中key为AllSegments的value变成json数组
				JSONArray allSegmentsArray = obj.getJSONArray("allSegments");
				// 遍历循环arrayAllSegments数组
				for (int j = 0; j < allSegmentsArray.size(); j++) {
					// 获取arrayAllSegments数组中的每一个值
					String segments = allSegmentsArray.getString(j);
					// 判断分词是否含有..(
					if (!segments.contains("...(")) {
						// 根据分词得到分词数
						String wordnum = segments.split("\\)  \\(")[1].replace(
								" words)", "");
						// 得到分词的内容
						String word = segments.split("\\)  \\(")[0] + ")";
						// 定义json对象
						JSONObject o = new JSONObject();
						// 生成wordnum对象
						o.put("wordnum", wordnum);
						// 生成word对象
						o.put("word", word);
						// 将生成的对象放入allSegments数组中
						allSegments.add(o);
					}
				}
				// 得到多个结果的json串
				JSONArray creditResults = new JSONArray();
				// 将obj对象中key为CreditResults的value变成json数组
				JSONArray creditResultsArray = obj
						.getJSONArray("creditResults");
				// 遍历循环creditResultsArray数组
				for (int k = 0; k < creditResultsArray.size(); k++) {
					// 将creditResultsArray数组中第k个转换为json对象
					JSONObject creditResultsObj = JSONObject
							.parseObject(creditResultsArray.get(k).toString());
					// 获取creditResultsObj对象中credit
					String credit = creditResultsObj.getString("credit");
					// 将creditResultsObj对象中key为abstracts的value变成abstractsArray数组
					JSONArray abstractsArray = creditResultsObj
							.getJSONArray("abstracts");
					// 遍历循环abstractsArray数组
					for (int m = 0; m < abstractsArray.size(); m++) {
						// 将abstractsArray数组中第m个转换为json对象
						JSONObject abstractsObj = JSONObject
								.parseObject(abstractsArray.get(m).toString());
						// 定义json对象
						JSONObject o = new JSONObject();
						// 生成分值对象
						o.put("credit", credit);
						// 获取abstractsObj对象中abstractString，并生成摘要对象
						o.put("abstract", abstractsObj
								.getString("abstractString"));
						// 获取abstractsObj对象中topic，并生成主题对象
						o.put("topic", abstractsObj.getString("topic"));
						// 获取abstractsObj对象中service，并生成业务对象
						o.put("service", abstractsObj.getString("service"));
						// 将abstractsObj对象中key为WordPatterns的value变成wordPatternsArray数组
						JSONArray wordPatternsArray = abstractsObj
								.getJSONArray("wordPatterns");
						// 遍历循环abstractsArray数组，只取第一个
						for (int n = 0; n < 1; n++) {
							// 将wordPatternsArray数组中第n个转换为json对象
							JSONObject wordPatternsObj = JSONObject
									.parseObject(wordPatternsArray.get(n)
											.toString());
							// 获取wordPatternsObj对象中patternID，并替换掉双引号
							String patternId = wordPatternsObj.getString(
									"patternID").replace("\"", "");
							// 判断patternId不为空且含有+号
							if (!"".equals(patternId)
									&& patternId.contains("+")) {
								// 将patternId按照+号拆分
								String[] patternids = patternId.split("\\+");
								// 获取模板id，并生成patternid对象
								o.put("patternid", GlobalValues
										.html(patternids[0]));
								// 获取第几个分词，生成segmentstring对象
								o.put("segmentstring", "分词" + patternids[1]);
							} else {
								// patternId为空，不含有+号
								// 生成空的patternid对象
								o.put("patternid", "");
								// 生成分词数为空的segmentstring对象
								o.put("segmentstring", "分词");
							}
							// 获取wordPatternsObj对象中patternString，并替换掉双引号
							String wordpat = wordPatternsObj.getString(
									"patternString").replace("\"", "");
							// 将词模转义一下，并生成patternstring对象
							o.put("patternstring", GlobalValues.html(wordpat));
							// 将词模转换成简单词模(模板)
							String simplewordpat = SimpleString
									.worpattosimworpat(wordpat
											+ "@2#编者=\"自学习\"");
							// 获取简单词模#前面的内容，并生成simplewordpat对象
							o.put("simplewordpat", simplewordpat.split("#")[0]);
							// 获取wordPatternsObj对象中patternType，并生成patterntype对象
							o.put("patterntype", wordPatternsObj
									.getString("patternType"));
							// 获取wordPatternsObj对象中needOrdered，如果NeedOrdered=false，返回无序，true返回有序
							if ("false".equalsIgnoreCase(wordPatternsObj
									.getString("needOrdered"))) {
								// 生成needordered对象
								o.put("needordered", "无序");
							} else {
								// 生成needordered对象
								o.put("needordered", "有序");
							}
							// 定义返回值变量，业务返回值可能是多个
							StringBuilder sb_returnValue = new StringBuilder();
							// 将wordPatternsObj对象中key为entities的value变成entitiesArray数组
							JSONArray entitiesArray = wordPatternsObj
									.getJSONArray("entities");
							// 遍历循环abstractsArray数组
							for (int r = 0; r < entitiesArray.size(); r++) {
								// 将entitiesArray数组中第r个转换为json对象
								JSONObject entitiesObj = JSONObject
										.parseObject(entitiesArray.get(r)
												.toString());
								if (r != 0) {
									// 多个之间有逗号连接
									sb_returnValue.append(",");
								}
								// 将key和value用=连接在一起，作为返回值，可能有多个
								sb_returnValue.append(entitiesObj
										.getString("key")
										+ "=" + entitiesObj.getString("value"));
							}
							// 得到返回值内容，并生成returnvalue对象
							o.put("returnvalue", sb_returnValue.toString());
						}
						// 将删除的对象放入creditResults数组中
						creditResults.add(o);
					}
				}
				// 得到被排除的知识的json串
				// 定义negAbsjson数组
				JSONArray negAbs = new JSONArray();
				// 将obj对象中key为negAbs的value变成negAbsArray数组
				JSONArray negAbsArray = obj.getJSONArray("negAbs");
				// 循环遍历negAbsArray数组
				for (int x = 0; x < negAbsArray.size(); x++) {
					// 获取negAbsArray数组中的第x个
					String negAb = negAbsArray.getString(x);
					// 判断negAb不含有...（
					if (!negAb.contains("...（")) {
						// 定义model1变量
						String model1 = "";
						// 定义业务变量
						String service1 = "";
						// 定义主题变量
						String topic1 = "";
						// 定义摘要变量
						String abstr1 = "";
						// 判断拆分后的个数大于等于2
						if (negAb.split(":").length >= 2) {
							// 获取model1，并进行转义
							model1 = GlobalValues.html(negAb.split(":")[1]);
							// 获取摘要
							abstr1 = negAb.split(":")[0];
							// 将摘要按照>拆分，并且拆分后的个数大于等于2
							if (abstr1.split(">").length >= 2) {
								// 获取主题
								topic1 = abstr1.split(">")[1];
								// 将摘要的>的前面内容按照<拆分，并且拆分后的个数大于等于2
								if (abstr1.split(">")[0].split("<").length >= 2) {
									// 获取业务
									service1 = abstr1.split(">")[0].split("<")[1];
								}
							}
						}
						// 定义json对象
						JSONObject o = new JSONObject();
						// 生成service1对象
						o.put("service1", service1);
						// 生成topic1对象
						o.put("topic1", topic1);
						// 生成abstract1对象
						o.put("abstract1", abstr1);
						// 生成model1对象
						o.put("model1", model1);
						// 将生成的对象放入negAbs数组中
						negAbs.add(o);
					}
				}
				// 得到被加分的知识的json串
				// 定义feaAbsjson数组
				JSONArray feaAbs = new JSONArray();
				// 将obj对象中key为feaAbs的value变成feaAbsArray数组
				JSONArray feaAbsArray = obj.getJSONArray("feaAbs");
				// 循环遍历feaAbsArray数组
				for (int y = 0; y < feaAbsArray.size(); y++) {
					// 获取feaAbsArray数组中的第y个
					String feaAb = feaAbsArray.getString(y);
					// 定义model2变量
					String model2 = "";
					// 定义业务变量
					String service2 = "";
					// 定义主题变量
					String topic2 = "";
					// 定义摘要变量
					String abstr2 = "";
					// 判断feaAb含有：号
					if (feaAb.contains(":")) {
						// 判断按照：拆分后的个数大于等于2
						if (feaAb.split(":").length >= 2) {
							// 获取model2
							model2 = feaAb.split(":")[1];
							// 获取摘要
							abstr2 = feaAb.split(":")[0];
							// 将摘要按照>拆分，并且拆分后的个数大于等于2
							if (abstr2.split(">").length >= 2) {
								// 获取主题
								topic2 = abstr2.split(">")[1];
								// 将摘要的>的前面内容按照<拆分，并且拆分后的个数大于等于2
								if (abstr2.split(">")[0].split("<").length >= 2) {
									// 获取业务
									service2 = abstr2.split(">")[0].split("<")[1];
								}
							}
						}
					} else {
						// feaAb不含有：号
						// 判断按照\\(拆分后的个数大于等于2
						if (feaAb.split("\\(").length >= 2) {
							// 获取model2
							model2 = feaAb.split("\\(")[1].replace(")", "");
							// 获取摘要
							abstr2 = feaAb.split("\\(")[0];
							// 将摘要按照>拆分，并且拆分后的个数大于等于2
							if (abstr2.split(">").length >= 2) {
								// 获取主题
								topic2 = abstr2.split(">")[1];
								// 将摘要的>的前面内容按照<拆分，并且拆分后的个数大于等于2
								if (abstr2.split(">")[0].split("<").length >= 2) {
									// 获取业务
									service2 = abstr2.split(">")[0].split("<")[1];
								}
							}
						}
					}
					// 定义json对象
					JSONObject o = new JSONObject();
					// 生成service1对象
					o.put("service2", service2);
					// 生成topic1对象
					o.put("topic2", topic2);
					// 生成abstract1对象
					o.put("abstract2", abstr2);
					// 生成model1对象
					o.put("model2", model2);
					// 将生成的对象放入feaAbs数组中
					feaAbs.add(o);
				}
				// 得到被置顶的知识的json串
				// 定义seaAbsjson数组
				JSONArray seaAbs = new JSONArray();
				// 将obj对象中key为seaAbs的value变成seaAbsArray数组
				JSONArray seaAbsArray = obj.getJSONArray("seaAbs");
				// 循环遍历seaAbsArray数组
				for (int z = 0; z < seaAbsArray.size(); z++) {
					// 获取seaAbsArray数组中的第y个
					String seaAb = seaAbsArray.getString(z);
					// 定义model3变量
					String model3 = "";
					// 定义业务变量
					String service3 = "";
					// 定义主题变量
					String topic3 = "";
					// 定义摘要变量
					String abstr3 = "";
					// 判断feaAb含有：号
					if (seaAb.indexOf(":") != -1) {
						// 判断按照：拆分后的个数大于等于2
						if (seaAb.split(":").length >= 2) {
							// 获取model3
							model3 = seaAb.split(":")[1];
							// 获取摘要
							abstr3 = seaAb.split(":")[0];
							// 将摘要按照>拆分，并且拆分后的个数大于等于2
							if (abstr3.split(">").length >= 2) {
								// 获取主题
								topic3 = abstr3.split(">")[1];
								// 将摘要的>的前面内容按照<拆分，并且拆分后的个数大于等于2
								if (abstr3.split(">")[0].split("<").length >= 2) {
									// 获取业务
									service3 = abstr3.split(">")[0].split("<")[1];
								}
							}
						}
					}
					// 定义json对象
					JSONObject o = new JSONObject();
					// 生成service1对象
					o.put("service3", service3);
					// 生成topic1对象
					o.put("topic3", topic3);
					// 生成abstract1对象
					o.put("abstract3", abstr3);
					// 生成model1对象
					o.put("model3", model3);
					// 将生成的对象放入seaAbs数组中
					seaAbs.add(o);
				}
				// 得到被排除子句的json串
				// 定义negClauseNamejson数组
				JSONArray negClauseName = new JSONArray();
				// 将obj对象中key为NegClauseName的value变成negClauseNameArray数组
				JSONArray negClauseNameArray = obj
						.getJSONArray("NegClauseName");
				// 判断negClauseNameArray数组是否为null
				if (negClauseNameArray != null) {
					// 获取遍历negClauseNameArray数组
					for (int r = 0; r < negClauseNameArray.size(); r++) {
						// 将第r个被排除的子句转义后放入negClauseNamejson数组中
						negClauseName.add(GlobalValues.html(negClauseNameArray
								.getString(r)));
					}
				}
				// 定义json对象
				JSONObject o = new JSONObject();
				// 生成allsegments对象
				o.put("allsegments", allSegments);
				// 生成creditresults对象
				o.put("creditresults", creditResults);
				// 生成negabs对象
				o.put("negabs", negAbs);
				// 生成feaabs对象
				o.put("feaabs", feaAbs);
				// 生成seaabs对象
				o.put("seaabs", seaAbs);
				// 生成被排除子句对象
				o.put("negClauseName", negClauseName);
				// 将生成的对象放入jsonArr数组中
				jsonArr.add(o);
			}
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将jsonArr放入jsonObj的result对象中
			jsonObj.put("result", jsonArr);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将返回结果解析失败放入jsonObj的result对象中
			jsonObj.put("result", "返回结果解析失败");
		}
		return jsonObj;
	}

	/**
	 * 判断字符串是否是乱码
	 * 
	 * @param strName参数字符串
	 * @return 是否是乱码
	 */
	public static boolean isMessyCode(String strName) {
		Pattern p = Pattern.compile("\\s*|\t*|\r*|\n*");
		Matcher m = p.matcher(strName);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float chLength = ch.length;
		float count = 0;
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!Character.isLetterOrDigit(c)) {
				if (!isChinese(c)) {
					if (c != '+') {
						count = count + 1;
					}
				}
			}
		}
		float result = count / chLength;
		if (result > 0.4) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断字符是否是中文
	 * 
	 * @param c参数字符
	 * @return 是否是中文
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * 语义锁定
	 * 
	 * @param service参数业务
	 * @param abs参数摘要
	 * @param question参数问题
	 * @return 返回的json串
	 */
	public static Object AutoWordpatLock(String service, String abs,
			String question, HttpServletRequest request, String absid, String ip) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 获取登录时的四层结构
		String servicetype = MyClass.IndustryOrganizationApplication();
		// 判断servicetype为空串、空、null
		if (" ".equals(servicetype) || "".equals(servicetype)
				|| servicetype == null) {
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("msg", "登录信息已失效,请注销后重新登录!");
			return jsonObj;
		}
		// 先查询当前业务和摘要是否存在
		// 定义查询业务和摘要是否存在的SQL语句
		String sql = "select distinct s.brand,k.kbdataid from service s,kbdata k where s.serviceid=k.serviceid and s.service=? and k.abstract=? and k.kbdataid=?";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定业务名称参数
		lstpara.add(service);
		// 绑定摘要名称参数
		lstpara.add(abs);
		// 绑定摘要id参数
		lstpara.add(absid);
		// 执行SQL语句，获取相应的数据源
		try {
			Result rs = Database.executeQuery(sql, lstpara.toArray());

			// 判断数据源是否为空，null
			if (rs != null && rs.getRowCount() > 0) {
				// 获取品牌
				String brand = rs.getRows()[0].get("brand").toString();
				// 获取高级分析的接口串中的serviceInfo
				String serviceInfo = MyUtil.getServiceInfo(servicetype, "生成词模",
						"", false);
				// 获取高级分析接口的入参字符串
				String queryObject = MyUtil.getDAnalyzeQueryObject("生成词模",
						question, servicetype, serviceInfo);
				// 获取高级分析的客户端
				NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
						.NLPCaller4WSClient(ip);
				// 判断客户端是否为null
				if (NLPCaller4WSClient == null) {
					// 将接口连接失败放入jsonObj的msg对象中
					jsonObj.put("msg", "接口连接失败!");
					return jsonObj;
				}
				String result = "";
				try {
					// 调用接口的方法获取词模
					result = NLPCaller4WSClient.kAnalyze(queryObject);
					// 替换掉返回串中的回车符
					result = result.replace("\n", "");
				} catch (Exception e) {
					e.printStackTrace();
					// 将语义锁定失败放入jsonObj的msg对象中
					jsonObj.put("msg", "语义锁定失败!");
					return jsonObj;
				}
				// 判断返回串是否为"接口请求参数不合规范！"、""、null
				if ("接口请求参数不合规范！".equals(result) || "".equals(result)
						|| result == null) {
					// 将接口返回串为空放入jsonObj的msg对象中
					jsonObj.put("msg", "接口返回串为空!");
					return jsonObj;
				}
				// 将结果转化为json对象,调用生成词模的接口生成词模,可能是多个，以@_@分隔
				JSONObject obj = JSONObject.parseObject(result);
				// 获取词模
				String wordpat = obj.getString("autoLearnedPat");
				// 判断生成的词模不为null且不为空
				if (wordpat != null && !"".equals(wordpat)) {
					// 判断词模是否含有@_@
					if (wordpat.contains("@_@")) {
						// 有的话，按照@_@进行拆分,并只取第一个
						wordpat = wordpat.split("@_@")[0];
					}
					// 获取词模中@前面的词模题，在加上@2#编者="auto"&来源="(当前问题)"
					wordpat = "++" + wordpat.split("@")[0] + "@2#编者=\""
							+ MyClass.LoginUserName() + "\"&最大未匹配字数=\"0\"";
					// 校验自动生成的词模是否符合规范
					if (Check.CheckWordpat(wordpat, request)) {
						// 定义简单词模
						String simplewordpat = "";
						// 判断词模是否包含@
						if (wordpat.contains("@")) {
							// 需要将词模转换为简单词模，在转换过程中，需要对词模按照@进行拆分
							simplewordpat = SimpleString
									.worpattosimworpat(wordpat);
						}
						// 查询当前摘要id下是否存在相同词模
						// 定义查询词模是否存在的SQL语句
						sql = "select * from wordpat where wordpat=? and kbdataid=?";
						// 定义绑定参数集合
						lstpara = new ArrayList<String>();
						// 绑定词模参数
						lstpara.add(wordpat);
						// 绑定摘要id参数
						lstpara.add(absid);
						// 执行SQL语句，获取相应的数据源
						Result rs1 = Database.executeQuery(sql, lstpara
								.toArray());
						// 判断数据源是否为null，且含有数据
						if (rs1 != null && rs1.getRowCount() > 0) {
							// 将当前语义已存在,无需锁定放入jsonObj的msg对象中
							jsonObj.put("msg", "当前语义已存在,无需锁定!");
							return jsonObj;
						} else {
							// 获取插入词模的序列
							String wordpatid = String.valueOf(SeqDAO
									.GetNextVal("SEQ_WORDPATTERN_ID"));
							// 定义插入词模的SQL语句
							sql = "insert into wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand,edittime,simplewordpat) values(?,?,?,?,?,?,?,sysdate,?)";
							// 定义绑定参数集合
							lstpara = new ArrayList<String>();
							// 绑定词模id参数
							lstpara.add(wordpatid);
							// 绑定词模参数
							lstpara.add(wordpat);
							// 绑定城市名称参数
							lstpara.add("上海");
							// 绑定自动开关参数
							lstpara.add("0");
							// 绑定词模类型参数,3代表选择词模
							lstpara.add("3");
							// 绑定摘要id参数
							lstpara.add(absid);
							// 绑定品牌参数
							lstpara.add(brand);
							// 绑定简单词模参数
							lstpara.add(simplewordpat);
							// 将插入词模的SQL语句放入集合中
							lstSql.add(sql);
							// 将对应的绑定参数集合放入集合中
							lstLstpara.add(lstpara);

							// 插入wordpatprecision的SQL语句
							sql = "insert into wordpatprecision(wordpatid,city,brand,correctnum,callvolume,wpprecision,autoreplyflag,projectflag) values(?,?,?,0,0,0,0,0 )";
							// 定义绑定参数集合
							lstpara = new ArrayList<String>();
							// 绑定词模id参数
							lstpara.add(wordpatid);
							// 绑定参数名称参数
							lstpara.add("上海");
							// 绑定品牌参数
							lstpara.add(brand);
							// 将插入wordpatprecision的SQL语句放入集合中
							lstSql.add(sql);
							// 将对应的绑定参数集合放入集合中
							lstLstpara.add(lstpara);

							// 生成添加简单词模操作日志记录
							lstSql.add(MyUtil.LogSql());
							lstLstpara.add(MyUtil.LogParam(brand, service,
									"增加模板", "上海", simplewordpat, "WORDPAT"));

							// 执行多条SQL语句的绑定事务处理，并获取事务处理的结果
							int c = Database.executeNonQueryTransaction(lstSql,
									lstLstpara);
							// 判断事务处理的结果
							if (c > 0) {
								// 事务处理成功将成功信息放入jsonObj的msg对象中
								jsonObj.put("msg", "语义锁定成功!");
							} else {
								// 判断事务处理失败,将失败信息放入jsonObj的msg对象中
								jsonObj.put("msg", "语义锁定失败!");
							}
							return jsonObj;
						}
					} else {
						// 将生成模板格式校验失败放入jsonObj的msg对象中
						jsonObj.put("msg", "由【" + question + "】生成的模板【"
								+ wordpat + "】格式校验出现错误");
						return jsonObj;
					}
				} else {
					// 将生成模板信息为空放入jsonObj的msg对象中
					jsonObj.put("msg", "由【" + question + "】生成的模板为空!");
					return jsonObj;
				}
			} else {
				// 将当前业务和摘要信息不存在放入jsonObj的msg对象中
				jsonObj.put("msg", "语义锁定失败,摘要信息不存在!");
				return jsonObj;
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 将语义锁定出现错误放入jsonObj的msg对象中
			jsonObj.put("msg", "语义锁定过程中出现错误!");
			return jsonObj;
		}
	}
	
	   /**
	 *@description 获得参数配置表具体值数据源
	 *@param name  配置参数名
	 *@param key   配置参数名对应key
	 *@return 
	 *@returnType Result 
	 */
	public static Result getConfigValue(String name ,String key){
	    List<Object> lstpara = new ArrayList<Object>();
		String sql ="select distinct t.name from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name =? and s.name like ? ";
		lstpara = new ArrayList<Object>();
		//根据四层结构串获得brand
		lstpara.add(name);
		lstpara.add("%"+key+"%");
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, lstpara.toArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs;
	   
	}
	
	/**
	 * 根据服务、渠道、用户、问题来获取简要分析的结果
	 * 
	 * @param user参数用户
	 * @param service参数服务
	 * @param channel参数渠道
	 * @param question参数咨询问题
	 * @param ip参数简要分析的地址
	 * @param type测试类型 如：回归测试
	 * @return 分析结果中的摘要答案json串
	 */
	public static Object KAnalyzeByFistResult(String user, String service,
			String channel, String question, String ip,String city ,String type ) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray kNLPResults = new JSONArray();
		if("".equals(city)||city==null){
			city="全国";
		}
		//获取参数配置知识点抽取信息value值
		Result rs = MetafieldmappingDAO.getConfigValue("知识点继承抽取信息配置","抽取信息过滤");
		List<String> configValueList = new ArrayList<String>();
		for (int n = 0; n < rs.getRowCount(); n++) {
			String value = rs.getRows()[n].get("name").toString();
			configValueList.add(value);
		}
		//ip = "http://222.186.101.213:8282/NLPAppWS/AnalyzeEnterPort?wsdl";
		// 获取简要分析的客户端
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient
				.NLPAppWSClient(ip);
		// 获取调用接口的入参字符串
		String queryObject = MyUtil.getKAnalyzeQueryObject_new(user, question,
				service, channel, city,"");
//		String queryObject = MyUtil.getKAnalyzeQueryObject(user, question,
//				service, channel);
		logger.info(type +"简要分析接口的输入串：" + queryObject);
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
//		String username = MyClass.LoginUserName();
		try {
			// 定义接口的analyze方法，并返回相应的返回串
			String result = NLPAppWSClient.analyze(queryObject);
			logger.info(type +"回归测试简要分析接口的输出串：" + result);
			// 将返回串按照||||来拆分，前一部分当作简要分析的json串
			result0 = result.split("\\|\\|\\|\\|")[0].replaceAll(
					"(\r\n|\r|\n|\n\r|\t)", "");
			// 后面一部分当作流程日志的json串
			result1 = result.split("\\|\\|\\|\\|")[1];
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
//			for (int i = 0; i < kNLPResultsArray.size(); i++)
			for (int i = 0; i < 1; i++)
			{
				// 定义一个json对象
				JSONObject o = new JSONObject();
				// 将kNLPResultsArray数组中的第i个转换为json对象
				JSONObject kNLPResultsObj = JSONObject
						.parseObject(kNLPResultsArray.get(i).toString());
				//遍历取继承词模返回值
				String retrnKeyValue ="";
				JSONArray parasArray = kNLPResultsObj.getJSONArray("paras");
				for(int l = 0; l < parasArray.size(); l++){
					JSONObject parasObj = JSONObject.parseObject(parasArray.get(l).toString());
					for(int j =0 ;j<configValueList.size();j++){
						String key = configValueList.get(j);
						String value = parasObj.getString(key);
						if(value!=null &&!"".equals(value)){
						 retrnKeyValue = retrnKeyValue + key+"="+value+"->>";	
						}
					}
				}
				
				//放入继承词模返回值
				o.put("retrnkeyvalue", retrnKeyValue);	
				// 获取kNLPResultsObj对象中credit，并生成credit对象
//				o.put("credit", kNLPResultsObj.getString("credit"));
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
	 * 根据服务、渠道、用户、问题来获取简要分析的结果
	 * 
	 * @param user参数用户
	 * @param service参数服务
	 * @param channel参数渠道
	 * @param question参数咨询问题
	 * @param ip参数简要分析的地址
	 * @return 分析结果中的摘要答案json串
	 */
	public static Object KAnalyzeAbsAndAnswer(String user, String service,
			String channel, String question, String ip,String city) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray kNLPResults = new JSONArray();
		if("".equals(city)||city==null){
			city="全国";
		}
		//获取参数配置知识点抽取信息value值
		Result rs = MetafieldmappingDAO.getConfigValue("知识点继承抽取信息配置","抽取信息过滤");
		List<String> configValueList = new ArrayList<String>();
		for (int n = 0; n < rs.getRowCount(); n++) {
			String value = rs.getRows()[n].get("name").toString();
			configValueList.add(value);
		}
		//ip = "http://222.186.101.213:8282/NLPAppWS/AnalyzeEnterPort?wsdl";
		// 获取简要分析的客户端
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient
				.NLPAppWSClient(ip);
		// 获取调用接口的入参字符串
		String queryObject = MyUtil.getKAnalyzeQueryObject_new(user, question,
				service, channel, city,"h5user");
//		String queryObject = MyUtil.getKAnalyzeQueryObject(user, question,
//				service, channel);
		logger.info("回归测试简要分析接口的输入串：" + queryObject);
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
//		String username = MyClass.LoginUserName();
		try {
			// 定义接口的analyze方法，并返回相应的返回串
			String result = NLPAppWSClient.analyze(queryObject);
			logger.info("回归测试简要分析接口的输出串：" + result);
			// 将返回串按照||||来拆分，前一部分当作简要分析的json串
			result0 = result.split("\\|\\|\\|\\|")[0].replaceAll(
					"(\r\n|\r|\n|\n\r|\t)", "");
			// 后面一部分当作流程日志的json串
			result1 = result.split("\\|\\|\\|\\|")[1];
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
//			for (int i = 0; i < kNLPResultsArray.size(); i++)
			for (int i = 0; i < 1; i++)
			{
				// 定义一个json对象
				JSONObject o = new JSONObject();
				// 将kNLPResultsArray数组中的第i个转换为json对象
				JSONObject kNLPResultsObj = JSONObject
						.parseObject(kNLPResultsArray.get(i).toString());
				//遍历取继承词模返回值
				String retrnKeyValue ="";
				JSONArray parasArray = kNLPResultsObj.getJSONArray("paras");
				for(int l = 0; l < parasArray.size(); l++){
					JSONObject parasObj = JSONObject.parseObject(parasArray.get(l).toString());
					for(int j =0 ;j<configValueList.size();j++){
						String key = configValueList.get(j);
						String value = parasObj.getString(key);
						if(value!=null &&!"".equals(value)){
						 retrnKeyValue = retrnKeyValue + key+"="+value+"->>";	
						}
					}
				}
				
				//放入继承词模返回值
				o.put("retrnkeyvalue", retrnKeyValue);	
				// 获取kNLPResultsObj对象中credit，并生成credit对象
//				o.put("credit", kNLPResultsObj.getString("credit"));
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
				// 将生成的对象放入kNLPResults数组中
				kNLPResults.add(o);
			}
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将kNLPResults数组放入jsonObj的result对象中
			jsonObj.put("result", kNLPResults);
			// 将result1放入jsonObj的result1对象中
//			jsonObj.put("result1", result1);
			logger.info("回归测试简要分析接口返回串："+jsonObj);
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
			logger.info("回归测试简要分析接口返回串："+jsonObj);
			return jsonObj;
		}
	}
	
	
	
	
	
}