package com.knowology.km.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.dal.Database;


public class MyUtil {
	private static Logger logger = Logger.getLogger(MyUtil.class);

	/**
	 * 将字符串转换为json串
	 * 
	 * @param ors参数字符串
	 * @return 满足json格式的字符串
	 */
	public static String ToString4JSON(String ors) {
		ors = ors == null ? "" : ors;
		StringBuilder buffer = new StringBuilder(ors);
		// /在替换的时候不要使用 String.replaceAll("\\","\\\\"); 这样不会达到替换的效果
		// 因为这些符号有特殊的意义,在正则 ///表达式里要用到
		int i = 0;
		while (i < buffer.length()) {
			if (buffer.charAt(i) == '\'' || buffer.charAt(i) == '\\') {
				buffer.insert(i, '\\');
				i += 2;
			} else {
				i++;
			}
		}
		return buffer.toString().replace("\r", "").replace("\n", "");
	}

	/**
	 * 将字符串转换为json串
	 * 
	 * @param json参数字符串
	 * @return 满足json格式的字符串
	 */
	public static String ToStringJSON(String json) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < json.length(); i++) {
			switch (json.charAt(i)) {
			case '\"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '/':
				sb.append("\\/");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				sb.append(json.charAt(i));
				break;
			}
		}
		return sb.toString();

	}

	/**
	 * 日志信息的存储SQL
	 * 
	 * @return
	 */
	public static String LogSql() {
		return "insert into operationlog(ip,brand,service,operation,city,workerid,workername,object,tablename,time) values(?,?,?,?,?,?,?,?,?,systimestamp)";
	}



	/**
	 * 中金的日志参数列表
	 * 
	 * @param brand品牌
	 * @param service业务
	 * @param operation数据操作类型
	 * @param city城市
	 * @param _object操作数据对象
	 * @param table对应操作表
	 * @param ip参数ip
	 * @param userid参数用户id
	 * @param username参数用户名称
	 * @return 数据参数集合
	 */
	public static List<String> LogParam_zj(String brand, String service,
			String operation, String city, String _object, String table,
			String ip, String userid, String username) {
		List<String> lstpara = new ArrayList<String>();
		lstpara.add(ip);
		lstpara.add(brand);
		lstpara.add(service);
		lstpara.add(operation);
		lstpara.add(city);
		lstpara.add(userid);
		lstpara.add(username);
		lstpara.add(_object);
		lstpara.add(table);
		return lstpara;
	}

	/**
	 * 获取用户ip
	 * 
	 * @return ip
	 */
	public static String GetIp() {
		InetAddress addr;
		String ip = " ";
		try {
			addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress().toString();
		} catch (UnknownHostException e) {
			logger.error(" 获取IP异常信息==>" + e);
		}
		return ip;
	}

	/**
	 * 将字符所占中替换为空
	 * 
	 * @param oldStr参数字符串
	 * @return 替换后的字符串
	 */
	public static String replaceAllIdent(String oldStr) {
		String[] repStr = new String[] { "?", "？", "/", "\\", "[", "]", "【",
				"】", ",", "，", "。", ".", "\"", "“", "”", "、", "(", ")", "（",
				"）", "！", "!", " ", ";", "；", "(", ")" };
		for (int i = 0; i < repStr.length; i++) {
			oldStr = oldStr.replace(repStr[i], "");
		}
		return oldStr;
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
	public static String getKAnalyzeQueryObject(String userid, String question,
			String business, String channel) {
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
		// 定义city的json数组
		JSONArray cityJsonArr = new JSONArray();
		// 将地市放入cityJsonArr数组中
		cityJsonArr.add("");
		// 定义city的json对象
		JSONObject cityJsonObj = new JSONObject();
		// 将cityJsonArr数组放入cityJsonObj对象中
		cityJsonObj.put("city", cityJsonArr);
		// 定义isRecordDB的json数组
		JSONArray isRecordDBJsonArr = new JSONArray();
		// 将是否保存数据放入isRecordDBJsonArr数组中
		isRecordDBJsonArr.add(getConfigValue.isRecordDB);
		// 定义isRecordDB的json对象
		JSONObject isRecordDBJsonObj = new JSONObject();
		// 将isRecordDBJsonArr放入isRecordDBJsonObj中
		isRecordDBJsonObj.put("isRecordDB", isRecordDBJsonArr);
		// 定义parasjson数组
		JSONArray parasJsonArr = new JSONArray();
		// 将cityJsonObj放入parasJsonArr中
		parasJsonArr.add(cityJsonObj);
		// 将isRecordDBJsonObj放入parasJsonArr中
		parasJsonArr.add(isRecordDBJsonObj);
		// 将parasJsonArr放入queryJsonObj中
		queryJsonObj.put("paras", parasJsonArr);
		return queryJsonObj.toJSONString();
	}
	
	/**
	 * 获取简要分析的入参字符串方法
	 * 
	 * @param userid参数用户id
	 * @param question参数问题
	 * @param business参数服务
	 * @param channel参数渠道 
	 * @param app 应用
	 * @return 入参字符串
	 */
	public static String getKAnalyzeQueryObject_new(String userid, String question,
			String business, String channel,String city,String app) {
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
		
		
		
		// 定义city的json数组
		JSONArray cityJsonArr = new JSONArray();
		// 将地市放入cityJsonArr数组中
		cityJsonArr.add(city);
		// 定义city的json对象
		JSONObject cityJsonObj = new JSONObject();
		// 将cityJsonArr数组放入cityJsonObj对象中
		cityJsonObj.put("city", cityJsonArr);
		
		// 定义app的json数组
		JSONArray appJsonArr = new JSONArray();
		// 将地市放入appJsonArr数组中
		appJsonArr.add(app);
		// 定义city的json对象
		JSONObject appJsonObj = new JSONObject();
		// 将appJsonArr数组放入appJsonObj对象中
		appJsonObj.put("applyCode", appJsonArr);
		
		
		// 定义isRecordDB的json数组
		JSONArray isRecordDBJsonArr = new JSONArray();
		// 将是否保存数据放入isRecordDBJsonArr数组中
		isRecordDBJsonArr.add(getConfigValue.isRecordDB);
		// 定义isRecordDB的json对象
		JSONObject isRecordDBJsonObj = new JSONObject();
		// 将isRecordDBJsonArr放入isRecordDBJsonObj中
		isRecordDBJsonObj.put("isRecordDB", isRecordDBJsonArr);
		// 定义parasjson数组
		JSONArray parasJsonArr = new JSONArray();
		// 将cityJsonObj放入parasJsonArr中
		parasJsonArr.add(cityJsonObj);
		
		// 将appjsonObj放入parasJsonArr中
		parasJsonArr.add(appJsonObj);
		
		// 将isRecordDBJsonObj放入parasJsonArr中
		parasJsonArr.add(isRecordDBJsonObj);
		// 将parasJsonArr放入queryJsonObj中
		queryJsonObj.put("paras", parasJsonArr);
		return queryJsonObj.toJSONString();
	}

	
	/**
	 * 获取简要分析的入参字符串方法
	 * 
	 * @param userid参数用户id
	 * @param question参数问题
	 * @param business参数服务
	 * @param channel参数渠道 
	 * @param app 应用
	 * @param 省份
	 * @return 入参字符串
	 */
	public static String getKAnalyzeQueryObject_new(String userid, String question,
			String business, String channel,String city,String app,String province) {
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
		
		// 定义province的json数组
		JSONArray provinceJsonArr = new JSONArray();
		// 将地市放入provinceJsonArr数组中
		provinceJsonArr.add(province);
		// 定义Province的json对象
		JSONObject provinceJsonObj = new JSONObject();
		// 将ProvinceJsonArr数组放入cityJsonObj对象中
		provinceJsonObj.put("Province", provinceJsonArr);
		
		
		// 定义city的json数组
		JSONArray cityJsonArr = new JSONArray();
		// 将地市放入cityJsonArr数组中
		cityJsonArr.add(city);
		// 定义city的json对象
		JSONObject cityJsonObj = new JSONObject();
		// 将cityJsonArr数组放入cityJsonObj对象中
		cityJsonObj.put("city", cityJsonArr);
		
		// 定义app的json数组
		JSONArray appJsonArr = new JSONArray();
		// 将地市放入appJsonArr数组中
		appJsonArr.add(app);
		// 定义city的json对象
		JSONObject appJsonObj = new JSONObject();
		// 将appJsonArr数组放入appJsonObj对象中
		appJsonObj.put("applyCode", appJsonArr);
		
		
		// 定义isRecordDB的json数组
		JSONArray isRecordDBJsonArr = new JSONArray();
		// 将是否保存数据放入isRecordDBJsonArr数组中
		isRecordDBJsonArr.add(getConfigValue.isRecordDB);
		// 定义isRecordDB的json对象
		JSONObject isRecordDBJsonObj = new JSONObject();
		// 将isRecordDBJsonArr放入isRecordDBJsonObj中
		isRecordDBJsonObj.put("isRecordDB", isRecordDBJsonArr);
		// 定义parasjson数组
		JSONArray parasJsonArr = new JSONArray();
		// 将cityJsonObj放入parasJsonArr中
		parasJsonArr.add(cityJsonObj);
		
		// 将appjsonObj放入parasJsonArr中
		parasJsonArr.add(appJsonObj);
		
		parasJsonArr.add(provinceJsonObj);
		
		// 将isRecordDBJsonObj放入parasJsonArr中
		parasJsonArr.add(isRecordDBJsonObj);
		// 将parasJsonArr放入queryJsonObj中
		queryJsonObj.put("paras", parasJsonArr);
		return queryJsonObj.toJSONString();
	}
	/**
	 * 获取高级分析的入参字符串方法
	 * 
	 * @param phone参数用户
	 * @param question参数问题
	 * @param business参数服务
	 * @param serviceInfo参数接口串中的serviceInfo
	 * @return 接口串
	 */
	public static String getDAnalyzeQueryObject(String phone, String question,
			String business, String serviceInfo,String isdebug) {
		// 定义一个json对象
		JSONObject queryJsonObj = new JSONObject();
		// 将phone放入queryJsonObj中
		queryJsonObj.put("phone", phone.trim());
		// 将用户咨询的问题放入queryJsonObj中
		queryJsonObj.put("query", question.replace("\"", ".").replace("•", ".")
				.replace("·", ".").replace("\\", "\\\\").trim());
		// 将四层结构放入queryJsonObj中
		queryJsonObj.put("channel", business);
		// 获取当前时间
		String callTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
				.format(new Date());
		// 将时间放入queryJsonObj中
		queryJsonObj.put("callTime", callTime);
		if("是".equals(isdebug)){
		 queryJsonObj.put("isDebug", true);	
		}else{
		 queryJsonObj.put("isDebug", false);	
		}
		// 定义serviceInfoJsonObj对象
		JSONObject serviceInfoJsonObj = new JSONObject();
		try {
			// 将serviceInfo转化为json对象
			serviceInfoJsonObj = JSONObject.parseObject(serviceInfo);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			serviceInfoJsonObj = JSONObject.parseObject("{}"); 
		}
		// 将serviceInfoJsonObj放入queryJsonObj中
		queryJsonObj.put("serviceInfo", serviceInfoJsonObj);
		return queryJsonObj.toJSONString();
	}
	
	
	/**
	 * 获取高级分析的入参字符串方法
	 * 
	 * @param phone参数用户
	 * @param question参数问题
	 * @param business参数服务
	 * @param serviceInfo参数接口串中的serviceInfo
	 * @return 接口串
	 */
	public static String getDAnalyzeQueryObject(String phone, String question,
			String business, String serviceInfo) {
		// 定义一个json对象
		JSONObject queryJsonObj = new JSONObject();
		// 将phone放入queryJsonObj中
		queryJsonObj.put("phone", phone.trim());
		// 将用户咨询的问题放入queryJsonObj中
		queryJsonObj.put("query", question.replace("\"", ".").replace("•", ".")
				.replace("·", ".").replace("\\", "\\\\").trim());
		// 将四层结构放入queryJsonObj中
		queryJsonObj.put("channel", business);
		// 获取当前时间
		String callTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
				.format(new Date());
		// 将时间放入queryJsonObj中
		queryJsonObj.put("callTime", callTime);
		// 定义serviceInfoJsonObj对象
		JSONObject serviceInfoJsonObj = new JSONObject();
		try {
			// 将serviceInfo转化为json对象
			serviceInfoJsonObj = JSONObject.parseObject(serviceInfo);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			serviceInfoJsonObj = JSONObject.parseObject("{}");
		}
		// 将serviceInfoJsonObj放入queryJsonObj中
		queryJsonObj.put("serviceInfo", serviceInfoJsonObj);
		return queryJsonObj.toJSONString();
	}
	/**
	 * 获取高级分析的入参字符串方法
	 * 
	 * @param phone参数用户
	 * @param question参数问题
	 * @param business参数服务
	 * @param city 城市参数
	 * @param serviceInfo参数接口串中的serviceInfo
	 * @return 接口串
	 */
	public static String getDAnalyzeQueryObject_new(String phone, String question,
			String business,String city,String serviceInfo) {
		// 定义一个json对象
		JSONObject queryJsonObj = new JSONObject();
		// 将phone放入queryJsonObj中
		queryJsonObj.put("phone", phone.trim());
		// 将用户咨询的问题放入queryJsonObj中
		queryJsonObj.put("query", question.replace("\"", ".").replace("•", ".")
				.replace("·", ".").replace("\\", "\\\\").trim());
		// 将四层结构放入queryJsonObj中
		queryJsonObj.put("channel", business);
		// 获取当前时间
		String callTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
				.format(new Date());
		// 将时间放入queryJsonObj中
		queryJsonObj.put("callTime", callTime);
		
		//添加地市参数
		// 定义city的json数组
		JSONArray cityJsonArr = new JSONArray();
		// 将地市放入cityJsonArr数组中
		cityJsonArr.add(city);
		// 定义city的json对象
		JSONObject cityJsonObj = new JSONObject();
		// 将cityJsonArr数组放入cityJsonObj对象中
		cityJsonObj.put("city", cityJsonArr);
		JSONArray parasJsonArr = new JSONArray();
		// 将cityJsonObj放入parasJsonArr中
		parasJsonArr.add(cityJsonObj);
		// 将parasJsonArr放入queryJsonObj中
		queryJsonObj.put("paras", parasJsonArr);
		// 定义serviceInfoJsonObj对象
		JSONObject serviceInfoJsonObj = new JSONObject();
		try {
			// 将serviceInfo转化为json对象
			serviceInfoJsonObj = JSONObject.parseObject(serviceInfo);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			serviceInfoJsonObj = JSONObject.parseObject("{}");
		}
		// 将serviceInfoJsonObj放入queryJsonObj中
		queryJsonObj.put("serviceInfo", serviceInfoJsonObj);
		return queryJsonObj.toJSONString();
	}

	/**
	 * 根据服务获取相应的四层结构的接口入参的serviceinfo串
	 * 
	 * @param business参数服务
	 * @param type参数不同的调用接口类型
	 * @param service参数type为同义词模时才需要使用的参数其他的不起作用
	 * @param isall参数是否使用全行业
	 * @param city 城市编码
	 * @return 接口入参的serviceinfo串
	 */
	public static String getServiceInfo(String business, String type,
			String service, boolean isall,String city,String isdebug) {
		// 定义接口入参的serviceInfo的变量
		String serviceInfo = "";
		// 判断服务是否含有->
		if (business.contains("->")) {
			// 将服务按照->拆分
			String[] businessArr = business.split("->", 3);
			// 定义SQL语句
			StringBuilder sql = new StringBuilder();
			// 查询商家、组织、应用配置表的SQL语句
			sql
					.append("select serviceroot,compservice,analyzeconfig from m_industryapplication2services where industry=? and organization=? and application=?");
			// 定义绑定参数集合
			List<String> lstpara = new ArrayList<String>();
			// 绑定商家参数
			lstpara.add(businessArr[0]);
			// 绑定组织参数
			lstpara.add(businessArr[1]);
			// 绑定应用参数
			lstpara.add(businessArr[2]);
			try {
				// 执行SQL语句，获取相应的数据源
				Result rs = Database.executeQuery(sql.toString(), lstpara
						.toArray());
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 获取serviceroot信息
					String serviceroot = rs.getRows()[0].get("serviceroot")
							.toString();
					// 获取compservice信息
					String compservice = rs.getRows()[0].get("compservice") != null ? rs
							.getRows()[0].get("compservice").toString()
							: "";
					// 获取analyzeconfig信息
					String analyzeconfig = rs.getRows()[0].get("analyzeconfig") != null ? rs
							.getRows()[0].get("analyzeconfig").toString()
							: "";
					// 将serviceroot按照|拆分
					String[] servicerootArr = serviceroot.split("\\|");
					// 定义去除行业主题的后的集合
					List<String> servicerootList = new ArrayList<String>();
					// 判断当前是否为同义词模，同义词模需要加上新增的业务"相似问题-xxx"对应的serviceID
					if ("同义词模".equals(type)) {
						servicerootList.add(service);
					}

					// 循环遍历serviceroot数组
					for (String ser : servicerootArr) {
						// 判断业务是否含有行业主题，且组织不为通用组织
						if (ser.contains("行业主题")
								&& !servicerootArr[1].equals("通用组织")) {
						} else {
							// 将业务放入集合中
							servicerootList.add(ser);
						}
					}
					// 生成auto词模时个性化业务不参与匹配
					if ("问题生成词模".equals(type) || ("生成词模").equals(type)) {
						servicerootList.remove("个性化业务");
					}
					// 获取业务根的业务id集合
					List<String> lstServiceRootId = getServiceIDByServiceLst(servicerootList);
					// 定义存放对比业务的集合
					List<String> compserviceList = new ArrayList<String>();
					// 定义存放对比业务的业务id集合
					List<String> lstCompServiceId = new ArrayList<String>();
					// 判断compservice是否为空
					if (compservice != null && !"".equals(compservice)) {
						// 将compservice按照|拆分，并存放到集合中
						compserviceList = Arrays.asList(compservice
								.split("\\|"));
						// 获取对比业务的业务id集合
						lstCompServiceId = getServiceIDByServiceLst(compserviceList);
					}
					// 判断业务根id的个数是否为0
					if (lstServiceRootId.size() > 0) {
						// 根据不同接口类型获取不同的接口串中的serviceInfo
						serviceInfo = getServiceInfoByType(type, analyzeconfig,
								lstServiceRootId, lstCompServiceId, isall,city,isdebug);
						
					} else {
						// 没有查询到相应的业务id，直接返回空的serviceInfo字符串
						serviceInfo = "{}";
					}
				} else {
					// 没有查询到，直接返回空的serviceInfo字符串
					serviceInfo = "{}";
				}
			} catch (Exception e) {
				e.printStackTrace();
				// 出现错误，直接返回空的serviceInfo字符串
				serviceInfo = "{}";
			}
		} else {
			// 服务不含有->，直接返回空的serviceInfo字符串
			serviceInfo = "{}";
		}
		return serviceInfo;
	}

	
	/**
	 * 根据服务获取相应的四层结构的接口入参的serviceinfo串
	 * 
	 * @param business参数服务
	 * @param type参数不同的调用接口类型
	 * @param service参数type为同义词模时才需要使用的参数其他的不起作用
	 * @param isall参数是否使用全行业
	 * @param city 城市编码
	 * @return 接口入参的serviceinfo串
	 */
	public static String getServiceInfo(String business, String type,
			String service, boolean isall,String city) {
		// 定义接口入参的serviceInfo的变量
		String serviceInfo = "";
		// 判断服务是否含有->
		if (business.contains("->")) {
			// 将服务按照->拆分
			String[] businessArr = business.split("->", 3);
			// 定义SQL语句
			StringBuilder sql = new StringBuilder();
			// 查询商家、组织、应用配置表的SQL语句
			sql
					.append("select serviceroot,compservice,analyzeconfig from m_industryapplication2services where industry=? and organization=? and application=?");
			// 定义绑定参数集合
			List<String> lstpara = new ArrayList<String>();
			// 绑定商家参数
			lstpara.add(businessArr[0]);
			// 绑定组织参数
			lstpara.add(businessArr[1]);
			// 绑定应用参数
			lstpara.add(businessArr[2]);
			try {
				// 执行SQL语句，获取相应的数据源
				Result rs = Database.executeQuery(sql.toString(), lstpara
						.toArray());
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 获取serviceroot信息
					String serviceroot = rs.getRows()[0].get("serviceroot")
							.toString();
					// 获取compservice信息
					String compservice = rs.getRows()[0].get("compservice") != null ? rs
							.getRows()[0].get("compservice").toString()
							: "";
					// 获取analyzeconfig信息
					String analyzeconfig = rs.getRows()[0].get("analyzeconfig") != null ? rs
							.getRows()[0].get("analyzeconfig").toString()
							: "";
					// 将serviceroot按照|拆分
					String[] servicerootArr = serviceroot.split("\\|");
					// 定义去除行业主题的后的集合
					List<String> servicerootList = new ArrayList<String>();
					// 判断当前是否为同义词模，同义词模需要加上新增的业务"相似问题-xxx"对应的serviceID
					if ("同义词模".equals(type)) {
						servicerootList.add(service);
					}

					// 循环遍历serviceroot数组
					for (String ser : servicerootArr) {
						// 判断业务是否含有行业主题，且组织不为通用组织
						if (ser.contains("行业主题")
								&& !servicerootArr[1].equals("通用组织")) {
						} else {
							// 将业务放入集合中
							servicerootList.add(ser);
						}
					}
					// 生成auto词模时个性化业务不参与匹配 ---> modify by hw
					if ("问题生成词模".equals(type) || ("生成词模").equals(type)) {
						servicerootList.remove("个性化业务");
					}
					// 获取业务根的业务id集合
					List<String> lstServiceRootId = getServiceIDByServiceLst(servicerootList);
					// 定义存放对比业务的集合
					List<String> compserviceList = new ArrayList<String>();
					// 定义存放对比业务的业务id集合
					List<String> lstCompServiceId = new ArrayList<String>();
					// 判断compservice是否为空
					if (compservice != null && !"".equals(compservice)) {
						// 将compservice按照|拆分，并存放到集合中
						compserviceList = Arrays.asList(compservice
								.split("\\|"));
						// 获取对比业务的业务id集合
						lstCompServiceId = getServiceIDByServiceLst(compserviceList);
					}
					// 判断业务根id的个数是否为0
					if (lstServiceRootId.size() > 0) {
						// 根据不同接口类型获取不同的接口串中的serviceInfo
						serviceInfo = getServiceInfoByType(type, analyzeconfig,
								lstServiceRootId, lstCompServiceId, isall,city);
						
					} else {
						// 没有查询到相应的业务id，直接返回空的serviceInfo字符串
						serviceInfo = "{}";
					}
				} else {
					// 没有查询到，直接返回空的serviceInfo字符串
					serviceInfo = "{}";
				}
			} catch (Exception e) {
				e.printStackTrace();
				// 出现错误，直接返回空的serviceInfo字符串
				serviceInfo = "{}";
			}
		} else {
			// 服务不含有->，直接返回空的serviceInfo字符串
			serviceInfo = "{}";
		}
		return serviceInfo;
	}
	
	/**
	 * 根据服务获取相应的四层结构的接口入参的serviceinfo串
	 * 
	 * @param business参数服务
	 * @param type参数不同的调用接口类型
	 * @param service参数type为同义词模时才需要使用的参数其他的不起作用
	 * @param isall参数是否使用全行业
	 * @return 接口入参的serviceinfo串
	 */
	public static String getServiceInfo(String business, String type,
			String service, boolean isall) {
		// 定义接口入参的serviceInfo的变量
		String serviceInfo = "";
		// 判断服务是否含有->
		if (business.contains("->")) {
			// 将服务按照->拆分
			String[] businessArr = business.split("->", 3);
			// 定义SQL语句
			StringBuilder sql = new StringBuilder();
			// 查询商家、组织、应用配置表的SQL语句
			sql
					.append("select serviceroot,compservice,analyzeconfig from m_industryapplication2services where industry=? and organization=? and application=?");
			// 定义绑定参数集合
			List<String> lstpara = new ArrayList<String>();
			// 绑定商家参数
			lstpara.add(businessArr[0]);
			// 绑定组织参数
			lstpara.add(businessArr[1]);
			// 绑定应用参数
			lstpara.add(businessArr[2]);
			try {
				// 执行SQL语句，获取相应的数据源
				Result rs = Database.executeQuery(sql.toString(), lstpara
						.toArray());
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 获取serviceroot信息
					String serviceroot = rs.getRows()[0].get("serviceroot")
							.toString();
					// 获取compservice信息
					String compservice = rs.getRows()[0].get("compservice") != null ? rs
							.getRows()[0].get("compservice").toString()
							: "";
					// 获取analyzeconfig信息
					String analyzeconfig = rs.getRows()[0].get("analyzeconfig") != null ? rs
							.getRows()[0].get("analyzeconfig").toString()
							: "";
					// 将serviceroot按照|拆分
					String[] servicerootArr = serviceroot.split("\\|");
					// 定义去除行业主题的后的集合
					List<String> servicerootList = new ArrayList<String>();
					// 判断当前是否为同义词模，同义词模需要加上新增的业务"相似问题-xxx"对应的serviceID
					if ("同义词模".equals(type)) {
						servicerootList.add(service);
					}

					// 循环遍历serviceroot数组
					for (String ser : servicerootArr) {
						// 判断业务是否含有行业主题，且组织不为通用组织
						if (ser.contains("行业主题")
								&& !servicerootArr[1].equals("通用组织")) {
						} else {
							// 将业务放入集合中
							servicerootList.add(ser);
						}
					}
					// 生成auto词模时个性化业务不参与匹配 
					if ("问题生成词模".equals(type) || ("生成词模").equals(type)) {
						servicerootList.remove("个性化业务");
					}
					//如果是高级分析非业务库的分析时过滤 行业业务库ID
					if ("高级分析".equals(type)) {
						if(!"通用商家".equals(businessArr[1])&&!"业务库应用".equals(businessArr[2])){
							String serviceString = businessArr[0]+"业务库";
							servicerootList.remove(serviceString);	
						}
						
					}
					// 获取业务根的业务id集合
					List<String> lstServiceRootId = getServiceIDByServiceLst(servicerootList);
					// 定义存放对比业务的集合
					List<String> compserviceList = new ArrayList<String>();
					// 定义存放对比业务的业务id集合
					List<String> lstCompServiceId = new ArrayList<String>();
					// 判断compservice是否为空
					if (compservice != null && !"".equals(compservice)) {
						// 将compservice按照|拆分，并存放到集合中
						compserviceList = Arrays.asList(compservice
								.split("\\|"));
						// 获取对比业务的业务id集合
						lstCompServiceId = getServiceIDByServiceLst(compserviceList);
					}
					// 判断业务根id的个数是否为0
					if (lstServiceRootId.size() > 0) {
						// 根据不同接口类型获取不同的接口串中的serviceInfo
						serviceInfo = getServiceInfoByType(type, analyzeconfig,
								lstServiceRootId, lstCompServiceId, isall,"");
						
					} else {
						// 没有查询到相应的业务id，直接返回空的serviceInfo字符串
						serviceInfo = "{}";
					}
				} else {
					// 没有查询到，直接返回空的serviceInfo字符串
					serviceInfo = "{}";
				}
			} catch (Exception e) {
				e.printStackTrace();
				// 出现错误，直接返回空的serviceInfo字符串
				serviceInfo = "{}";
			}
		} else {
			// 服务不含有->，直接返回空的serviceInfo字符串
			serviceInfo = "{}";
		}
		return serviceInfo;
	}
	/**
	 * 根据业务名称集合获取对应的业务id集合
	 * 
	 * @param serviceList参数业务名称集合
	 * @return 业务id集合
	 */
	private static List<String> getServiceIDByServiceLst(
			List<String> serviceList) {
		// 定义业务id集合
		List<String> lstserviceid = new ArrayList<String>();
		try {
			// 定义SQL语句
			StringBuilder sql = new StringBuilder();
			// 查询service对应的serviceid的SQL语句
			sql
					.append("select serviceid from service where parentid=0 and service in (");
			// // 定义绑定参数集合
			List<String> lstpara = new ArrayList<String>();
			// 循环遍历业务名称集合
			for (int i = 0; i < serviceList.size(); i++) {
				if (i != serviceList.size() - 1) {
					// 除了集合的最后一个绑定参数不需要加上逗号，其他的都要加上
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?)");
				}
				// 绑定id参数
				lstpara.add(serviceList.get(i));
			}
			// 执行SQL语句，获取相应的数据源
			Result rs = Database
					.executeQuery(sql.toString(), lstpara.toArray());
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 获取业务id
					String sid = rs.getRows()[i].get("serviceid") != null ? rs
							.getRows()[i].get("serviceid").toString() : "";
					// 判断id是否为空，null
					if (sid != null && !"".equals(sid)) {
						// 将业务id加上双引号放入集合中
						lstserviceid.add("\"" + sid + "\"");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误,清空集合
			lstserviceid.clear();
		}
		return lstserviceid;
	}

	/**
	 * 根据不同接口类型获取不同的接口串中的serviceInfo
	 * 
	 * @param type参数接口类型
	 * @param analyzeconfig参数NLP分析配置
	 * @param lstServiceRootId参数业务根的业务id集合
	 * @param lstCompServiceId参数对比业务的业务id集合
	 * @param isall参数是否使用全行业
	 * @return 接口串中的serviceInfo
	 */
	private static String getServiceInfoByType(String type,
			String analyzeconfig, List<String> lstServiceRootId,
			List<String> lstCompServiceId, boolean isall,String city,String isdebug) {
		// 定义存放去掉后的集合
		List<String> analyzeconfigLst = new ArrayList<String>();
		
		String analyzeconfigResult ="";
		// 将analyzeconfig按照$_$拆分
		if("问题生成词模".equals(type)||"生成词模".equals(type)){
			String[] analyzeconfigArr = analyzeconfig.split("\\$_\\$");
			// 循环遍历analyzeconfigArr数组
			for (int i = 0; i < analyzeconfigArr.length; i++) {
				// 判断是否包含analyzeconfig
					if (!analyzeconfigArr[i].contains("CompServiceIDs4FAQ=")) {
						// 将不包含CompServiceIDs4FAQ的放入集合中
						analyzeconfigLst.add(analyzeconfigArr[i]);
				}
			}	
			// 定义存放去掉analyzeconfig中的CompServiceIDs4FAQ后的变量
			analyzeconfigResult = StringUtils.join(analyzeconfigLst, "$_$");
		}else{
			analyzeconfigResult = analyzeconfig;
		}
		

		// 定义返回的接口串中的serviceInfo变量
		String serviceInfo = "{}";
		if (isall) {
			serviceInfo = "{\""
					+ analyzeconfigResult.replace("=", "\":[\"").replace("$_$",
							"\"],\"")
					+ "\"],\"MatchPattern4FAQ\":[\"true\"],\"isGetPat4FAQ\":[\"true\"],\"ServiceRootIDs\":[\"ALL\"]";
			// 判断对比业务的集合的个数是否大于0
			if (lstCompServiceId.size() > 0) {
				serviceInfo += ",\"CompServiceIDs4FAQ\":[\"NO\"]";
			} else {
				serviceInfo += ",\"CompServiceIDs4FAQ\":"
						+ lstCompServiceId.toString();
			}
			serviceInfo += "}";
		} else {
			serviceInfo = "{\""
					+ analyzeconfigResult.replace("=", "\":[\"").replace("$_$",
							"\"],\"") + "\"],\"ServiceRootIDs\":"
					+ lstServiceRootId.toString();
			// 根据不同的类型获取接口的入参串
			if ("相似问题".equals(type)) {
				//serviceInfo += ",\"MatchPattern4FAQ\":[\"true\"]}";
				serviceInfo += ",\"MatchPattern4FAQ\":[\"true\"],\"City\":[\""+city+"\"]}";
				//特殊处理相似问题测试阀值 MinCredit 设定为 0.1
				JSONObject obj = JSONObject.parseObject(serviceInfo);
				JSONArray ary = new  JSONArray(); 
				ary.add("0");
				obj.put("MinCredit", ary);
				serviceInfo = obj.toString();
				
			}
			else if("高级分析".equals(type)) {
				if("是".equals(isdebug)){
					serviceInfo += ",\"IsDebug\":[\"true\"],\"City\":[\""+city+"\"]}";	
				}else{
					serviceInfo += ",\"IsDebug\":[\"false\"],\"City\":[\""+city+"\"]}";	
				}
				
			}
			
			else if ("同义词模".equals(type)) {
				serviceInfo += ",\"isGetPat4FAQ\":[\"true\"]}";
			} else if ("问题生成词模".equals(type)) {
				serviceInfo += ",\"MatchPattern4FAQ\":[\"true\"],\"isGetPat4FAQ\":[\"true\"],\"CompServiceIDs4FAQ\":"
						+ lstCompServiceId.toString() + "}";
			} else if ("生成词模".equals(type)) {
				serviceInfo += ",\"MatchPattern4FAQ\":[\"true\"],\"isGetPat4FAQ\":[\"false\"],\"CompServiceIDs4FAQ\":[\"NO\"]}";
			} else {
				serviceInfo += "}";
			}
		}
		return serviceInfo;
	}
	
	/**
	 * 根据不同接口类型获取不同的接口串中的serviceInfo
	 * 
	 * @param type参数接口类型
	 * @param analyzeconfig参数NLP分析配置
	 * @param lstServiceRootId参数业务根的业务id集合
	 * @param lstCompServiceId参数对比业务的业务id集合
	 * @param isall参数是否使用全行业
	 * @return 接口串中的serviceInfo
	 */
	private static String getServiceInfoByType(String type,
			String analyzeconfig, List<String> lstServiceRootId,
			List<String> lstCompServiceId, boolean isall,String city) {
		// 定义存放去掉后的集合
		List<String> analyzeconfigLst = new ArrayList<String>();
		
		String analyzeconfigResult ="";
		// 将analyzeconfig按照$_$拆分
		if("问题生成词模".equals(type)||"生成词模".equals(type)){
			String[] analyzeconfigArr = analyzeconfig.split("\\$_\\$");
			// 循环遍历analyzeconfigArr数组
			for (int i = 0; i < analyzeconfigArr.length; i++) {
				// 判断是否包含analyzeconfig
					if (!analyzeconfigArr[i].contains("CompServiceIDs4FAQ=")) {
						// 将不包含CompServiceIDs4FAQ的放入集合中
						analyzeconfigLst.add(analyzeconfigArr[i]);
				}
			}	
			// 定义存放去掉analyzeconfig中的CompServiceIDs4FAQ后的变量
			analyzeconfigResult = StringUtils.join(analyzeconfigLst, "$_$");
		}else{
			analyzeconfigResult = analyzeconfig;
		}
		

		// 定义返回的接口串中的serviceInfo变量
		String serviceInfo = "{}";
		if (isall) {
			serviceInfo = "{\""
					+ analyzeconfigResult.replace("=", "\":[\"").replace("$_$",
							"\"],\"")
					+ "\"],\"MatchPattern4FAQ\":[\"true\"],\"isGetPat4FAQ\":[\"true\"],\"ServiceRootIDs\":[\"ALL\"]";
			// 判断对比业务的集合的个数是否大于0
			if (lstCompServiceId.size() > 0) {
				serviceInfo += ",\"CompServiceIDs4FAQ\":[\"NO\"]";
			} else {
				serviceInfo += ",\"CompServiceIDs4FAQ\":"
						+ lstCompServiceId.toString();
			}
			serviceInfo += "}";
		} else {
			serviceInfo = "{\""
					+ analyzeconfigResult.replace("=", "\":[\"").replace("$_$",
							"\"],\"") + "\"],\"ServiceRootIDs\":"
					+ lstServiceRootId.toString();
			// 根据不同的类型获取接口的入参串
			if ("相似问题".equals(type)) {
				//serviceInfo += ",\"MatchPattern4FAQ\":[\"true\"]}";
//				serviceInfo += ",\"MatchPattern4FAQ\":[\"true\"],\"City\":[\""+city+"\"]}";
				serviceInfo += ",\"MatchPattern4FAQ\":[\"true\"],\"City\":[\""+city+"\"]}";
				//特殊处理相似问题测试阀值 MinCredit 设定为 0.1
				JSONObject obj = JSONObject.parseObject(serviceInfo);
				JSONArray ary = new  JSONArray(); 
				ary.add("0");
				obj.put("MinCredit", ary);
				serviceInfo = obj.toString();
				
			}
			else if("高级分析".equals(type)) {
				serviceInfo += ",\"City\":[\""+city+"\"]}";
			}else if("继承高级分析".equals(type)){
				city = city.replace("|", "\",\"");
				serviceInfo += ",\"City\":[\""+city+"\"]}";
			}
			
			else if ("同义词模".equals(type)) {
				serviceInfo += ",\"isGetPat4FAQ\":[\"true\"]}";
			} else if ("问题生成词模".equals(type)) {
				city = city.replace("|", "\",\"");
				serviceInfo += ",\"City\":[\""+city+"\"]";
//				serviceInfo += ",\"MatchPattern4FAQ\":[\"true\"],\"isGetPat4FAQ\":[\"true\"]}";
				serviceInfo += ",\"MatchPattern4FAQ\":[\"false\"],\"isGetPat4FAQ\":[\"true\"]}";
//				serviceInfo += ",\"MatchPattern4FAQ\":[\"true\"],\"isGetPat4FAQ\":[\"true\"],\"CompServiceIDs4FAQ\":"
//					+ lstCompServiceId.toString() + "}";
//				serviceInfo += ",\"MatchPattern4FAQ\":[\"true\"],\"isGetPat4FAQ\":[\"false\"],\"CompServiceIDs4FAQ\":"
//					+ lstCompServiceId.toString() + "}";
				
			} else if ("生成词模".equals(type)) {
				serviceInfo += ",\"MatchPattern4FAQ\":[\"false\"],\"isGetPat4FAQ\":[\"true\"],\"CompServiceIDs4FAQ\":[\"NO\"]}";
			} else {
				serviceInfo += "}";
			}
		}
		return serviceInfo;
	}
	
	/**
	 * 日志信息的参数列表
	 * 
	 * @param brand品牌
	 * @param service业务
	 * @param operation数据操作类型
	 * @param city城市
	 * @param _object操作数据对象
	 * @param table对应操作表
	 * @return 数据参数集合
	 */
	public static List<String> LogParam(String brand, String service,
			String operation, String city, String _object, String table) {

		List<String> lstpara = new ArrayList<String>();
		lstpara.add(MyClass.LoginUserIp());
		lstpara.add(brand);
		lstpara.add(service);
		lstpara.add(operation);
		lstpara.add(city);
		lstpara.add(MyClass.LoginUserId());
		lstpara.add(MyClass.LoginUserName());
		lstpara.add(_object);
		lstpara.add(table);
		return lstpara;
	}

	/**
	 * 
	 * Description:将Clob对象转换为String对象,Blob处理方式与此相同
	 * 
	 * @param clob
	 */
	public static String oracleClob2Str(Clob clob) {
		try {
			return (clob != null ? clob.getSubString((long) 1, (int) clob
					.length()) : null);
		} catch (SQLException e) {
			return null;
		}
	}

	/**
	 * 读取问题文件
	 * @param path 
	 * @param name参数文件名称
	 * @return 数据
	 */
	public static List<List<String>> readQueryTxt(String path,String name) {
		// 定义内容的集合
		List<List<String>> info = new ArrayList<List<String>>();
		// 获取文件的完整路径
		  String fileName  =  path + File.separator + name;
		// 定义读文件流
		BufferedReader reader = null;
		// 定义文件每一行的内容变量
		String s = null;
		//定义错误标识
		String wrongInfo ="";
		//定义文件行数
		int i =0;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(fileName)), com.knowology.km.dal.Database
							.getJDBCValues("regressqueryreadencoding")));
			// 循环遍历文件的每一行
			while ((s = reader.readLine()) != null) {
				i++;
				try {
					// 判断每一行是否为空
					if (!"".equals(s.trim())) {
						// 判断每一行是否含有\t
						if (s.indexOf("\t") != -1) {
							// 定义存放每一行内容的集合
							List<String> content = new ArrayList<String>();
							// 将每一行的内容按照\t拆分
							String[] arr = s.split("\t");
							//通过@#@拆分后制定值小于3，提示文件格式错误
							//文件行记录内容为： customerquery@#@city@#@normalquery
							if (arr.length < 3) {
								wrongInfo ="文件第"+i+"行数据格式不对,请处理后重新上传导入!";	
								content.add(wrongInfo);
								info.add(0, content);
								break;
							} else {
								// 将数组转换为集合并放入集合中
								content = new ArrayList<String>(Arrays.asList(arr));
								//通过\t拆分内容存在空的情况，提示数据不完整
								for(int k =0;k<content.size();k++){
									String c = content.get(k);
									if("".equals(c)||c==null){
										wrongInfo = "文件第"+i+"行数据不完整,请处理后重新上传导入!";	
										break;
									}
								}
								if(!"".equals(wrongInfo)){
									content = new ArrayList<String>();
									content.add(wrongInfo);
									info.add(0, content);
									break;
								}else{
									info.add(content);	
								}
								
							}
							
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			// 关闭文件流
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return info;
	}
	
	/**
	 * 读取 office 2003 excel
	 * 
	 * @param file参数导入文件
	 * @param count 读取列数
	 * @return 读取文件后的集合
	 */
	public static List<List<Object>> read2003Excel(File file,int count) {
		// 定义返回的集合
		List<List<Object>> list = new ArrayList<List<Object>>();
		// 定义每一行组成的集合
		List<Object> param = new ArrayList<Object>();
		try {
			// 将导入的文件变成工作簿对象
			HSSFWorkbook hwb = new HSSFWorkbook(new FileInputStream(file));
			// 获取工作簿的第一个sheet
			HSSFSheet sheet = hwb.getSheetAt(0);
			// 定义每一个单元格的值变量
			Object value = null;
			// 定义每一行的变量
			HSSFRow row = null;
			// 定义每一个单元格变量
			HSSFCell cell = null;
			// 读取第一行
			row = sheet.getRow(0);
			// 判断第一行是否为null
			if (row != null) {
				// 第一行不为null，循环变量第一行的每个单元格
				for (int j = 0; j <= row.getLastCellNum(); j++) {
					// 获取第j个单元格
					cell = row.getCell(j);
					// 判断第j个单元格是否为null
					if (cell == null) {
						// 为null，继续下一个单元格
						continue;
					}
					// 获取第j个单元格的值
					value = cell.getStringCellValue().trim();
					// 将值放入集合中
					param.add(value);
				}
				// 将读取的第一列组成的集合放入集合中
				list.add(param);
			}
			// 读取第一行以下的部分
			// 循环遍历当前sheet的除第一行以下的行数
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				// 获取第i行，赋值给行变量
				row = sheet.getRow(i);
				// 判断第i行是否为null
				if (row == null) {
					// 第i行为null，继续读取下一行
					continue;
				}
				// 定义每一行组成的集合
				param = new ArrayList<Object>();
				int flag = 0;
				// 循环遍历每一行的列数
				for (int j = 0; j < count; j++) {
					// 获取第j个单元格
					cell = row.getCell(j);
					// 判断第j个单元格是否为null
					if (cell == null) {
						// 将null放入集合中
						param.add(null);
					} else {
						// 第j个单元格不为null，判断当前单元格的类型是什么
						switch (cell.getCellType()) {
						case XSSFCell.CELL_TYPE_STRING:// 字符串单元格
							// 获取当前单元格的值
							value = cell.getStringCellValue().trim();
							flag = 1;
							break;
						case XSSFCell.CELL_TYPE_BLANK:// 空单元格
							// 将null赋值给当前单元格的值变量
							value = null;
							break;
						default:// 缺省类型
							// 直接转换为字符串
							value = cell.toString();
							flag = 1;
						}
						// 将当前单元格的值放入集合中
						param.add(value);
					}
				}
				// 将读取的每一列组成的集合放入集合中
				if (flag == 1){
					list.add(param);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误,返回空集合
			// 定义每一行组成的集合
			param = new LinkedList<Object>();
			// 将空的集合放入集合中
			list.add(param);
		}
		return list;
	}
	
	/**
	 * 读取Office 2007 excel
	 * 
	 * @param file参数导入的文件
	 * @param count 读取列数
	 * @return 读取文件后的集合
	 */
	public static List<List<Object>> read2007Excel(File file,int count) {
		// 定义返回的集合
		List<List<Object>> list = new ArrayList<List<Object>>();
		// 定义每一行组成的集合
		List<Object> param = new ArrayList<Object>();
		try {
			// 将导入的文件变成工作簿对象
			XSSFWorkbook xwb = new XSSFWorkbook(new FileInputStream(file));
			// 获取工作簿的第一个sheet
			XSSFSheet sheet = xwb.getSheetAt(0);
			// 定义每一个单元格的值变量
			Object value = null;
			// 定义每一行的变量
			XSSFRow row = null;
			// 定义每一个单元格变量
			XSSFCell cell = null;
			// 读取第一行
			row = sheet.getRow(0);
			// 判断第一行是否为null
			if (row != null) {
				// 第一行不为null，循环变量第一行的每个单元格
				for (int j = 0; j <= row.getLastCellNum(); j++) {
					// 获取第j个单元格
					cell = row.getCell(j);
					// 判断第j个单元格是否为null
					if (cell == null) {
						// 为null，继续下一个单元格
						continue;
					}
					// 获取第j个单元格的值
					value = cell.getStringCellValue().trim();
					// 将值放入集合中
					param.add(value);
				}
				// 将读取的第一列组成的集合放入集合中
				list.add(param);
			}
			// 读取第一行以下的部分
			// 循环遍历当前sheet的除第一行以下的行数
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				// 获取第i行，赋值给行变量
				row = sheet.getRow(i);
				// 判断第i行是否为null
				if (row == null) {
					// 第i行为null，继续读取下一行
					continue;
				}
				// 定义每一行组成的集合
				param = new ArrayList<Object>();
				int flag = 0;
				// 循环遍历每一行的列数
				for (int j = 0; j < count; j++) {
					// 获取第j个单元格
					cell = row.getCell(j);
					// 判断第j个单元格是否为null
					if (cell == null) {
						// 将null放入集合中
						param.add(null);
					} else {
						// 第j个单元格不为null，判断当前单元格的类型是什么
						switch (cell.getCellType()) {
						case XSSFCell.CELL_TYPE_STRING:// 字符串单元格
							// 获取当前单元格的值
							value = cell.getStringCellValue().trim();
							flag = 1;
							break;
						case XSSFCell.CELL_TYPE_BLANK:// 空单元格
							// 将null赋值给当前单元格的值变量
							value = null;
							break;
						default:// 缺省类型
							// 直接转换为字符串
							value = cell.toString();
							flag = 1;
						}
						// 将当前单元格的值放入集合中
						param.add(value);
					}
				}
				// 将读取的每一列组成的集合放入集合中
				if (flag == 1){
					list.add(param);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误,返回空集合
			// 定义每一行组成的集合
			param = new LinkedList<Object>();
			// 将空的集合放入集合中
			list.add(param);
		}
		return list;
	}
	
	
	/**
	 * Map按key升序排列
	 * 
	 *@param map
	 *@return Map<String, String>
	 * 
	 **/
	public static Map<String, String> sortMapByKey(Map<String, String> map) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		Map<String, String> sortMap = new TreeMap<String, String>(
				new MapKeyComparator());
		sortMap.putAll(map);
		return sortMap;
	}

	/** Map按照value排序 */
	@SuppressWarnings("unchecked")
	public static Map sortMap(Map oldMap) {
		ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
				oldMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> arg0,
					Entry<java.lang.String, Integer> arg1) {
				return arg1.getValue() - arg0.getValue();
			}
		});
		Map newMap = new LinkedHashMap();
		for (int i = 0; i < list.size(); i++) {
			newMap.put(list.get(i).getKey(), list.get(i).getValue());
		}
		return newMap;
	}
	
}

class MapKeyComparator implements Comparator<String> {
	public int compare(String str1, String str2) {
		return str1.compareTo(str2);
	}
}
