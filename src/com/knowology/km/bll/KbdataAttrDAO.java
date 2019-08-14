package com.knowology.km.bll;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.UtilityOperate.GetConfigValue;
import com.knowology.bll.CommonLibKbdataAttrDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.km.NLPAppWS.AnalyzeEnterDelegate;
import com.knowology.km.dal.Database;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.getServiceClient;

public class KbdataAttrDAO {
	public static Logger logger = Logger.getLogger("querymanage");
	public static String[] names = { "父亲业务", "父亲摘要ID", "父亲摘要", "儿子业务", "儿子摘要",
		"儿子摘要ID", "Business","业务X","业务Y","业务Z","业务L","业务M","业务N","相关度"};

	private static Map<String,String> cityMap;
	private static Map<String,String> cityMap2;
	static{
		Result rs = CommonLibKbdataAttrDAO.constructCitys();
		if (rs != null) {
			int length = rs.getRowCount();
			cityMap = new HashMap<String, String>();
			cityMap2 = new HashMap<String, String>();
			for (int i = 0; i < length; i++) {
				String cityCode = rs.getRows()[i].get("code").toString();
				String cityName = rs.getRows()[i].get("city").toString();
				cityMap.put(cityCode, cityName);
				cityMap2.put(cityName, cityCode);
			}
		}
	}
	
	/**
	 * 根据地市编码获取地市信息
	 * @param cityCode 地市编码
	 * @return
	 */
	public static String getCityInfo(String cityCode) {
		String citys = "";
		if (cityCode==null || cityCode.equals("")) {// 如果地市编码不存在
			return citys;
		}
		String[] cityArray = cityCode.split(",");
		for (String city : cityArray) {
			if (city.equals("全国")) {
				citys += "全国,";
			} else if(cityMap.get(city) != null){
				citys += cityMap.get(city) + ",";
			}
		}
		if (citys.endsWith(",")) {
			citys = citys.substring(0, citys.lastIndexOf(","));
		}
		return citys;
	}
	
	/**
	 * 根据地市名称获取地市编码
	 * @param cityName
	 * @return
	 */
	public static String getCityCode(String cityName) {
		String cityCode = "";
		if (cityName.equals("全国")) {
			cityCode = "全国";
			return cityCode;
		}
		for (Entry<String,String> entry : cityMap.entrySet()) {
			String code = entry.getKey();
			String city = entry.getValue();
			if (!cityName.equals("") && city.contains(cityName)) {
				cityCode = code;
				return cityCode;
			}
		}
		return cityCode;
	}
	
	/**
	 * 构造业务树
	 * @param serviceid 业务id
	 * @return
	 */
	public static Object createServiceTree(String serviceid) {
		// 获取行业
		String industry = MyClass.IndustryOrganizationApplication();
		industry = industry.substring(0, industry.indexOf("-")) + "问题库";
		// 定义返回的json串
		JSONArray array = new JSONArray();  
		Result rs = CommonLibKbdataAttrDAO.createServiceTree(serviceid, industry);
		if (rs!=null && rs.getRowCount()>0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", rs.getRows()[i].get("serviceid").toString());
				jsonObj.put("text", rs.getRows()[i].get("service").toString());
				if (hasChild(rs.getRows()[i].get("serviceid").toString())==0) {// 如果没有子业务
					jsonObj.put("iconCls", "icon-servicehit");
				}
				jsonObj.put("state", "closed");
				array.add(jsonObj);
			}
		}
		
		return array;
	}
	
	/**
	 * 是否有子业务
	 * @param serviceid 业务id
	 * @return
	 */
	private static int hasChild(String serviceid) {
		int count = 0;
		count = CommonLibKbdataAttrDAO.hasChild(serviceid);
		return count;
	}
	
	/**
	 * 构造摘要下拉框
	 * @param serviceid 业务id
	 * @return
	 */
	public static Object createCombobox(String serviceid) {
		if ("".equals(serviceid)) {
			return "";
		}
		Result rs = CommonLibKbdataAttrDAO.createCombobox(serviceid);
		// 定义返回的json串
		JSONArray array = new JSONArray();  
			
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", rs.getRows()[i].get("kbdataid").toString());
				jsonObj.put("text", rs.getRows()[i].get("abstract").toString());
				array.add(jsonObj);
			}
		}
	
		return array;
	}
	
	/**
	 * 根据子摘要id构造父摘要下拉框
	 * @param kbdataid 子摘要id
	 * @return
	 */
	public static Object createFatherCombobox(String kbdataid) {
		// 定义返回的json串
		JSONArray array = new JSONArray();  
		Result rs = CommonLibKbdataAttrDAO.createFatherCombobox(kbdataid);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", rs.getRows()[i].get("kbdataid").toString());
				jsonObj.put("text", rs.getRows()[i].get("abstract").toString());
				array.add(jsonObj);
			}
		}
		return array;
	}
	
	/**
	 * 根据摘要id新增列以及列对应的数据
	 * @param fkbdataid 父摘要id
	 * @return
	 */
	public static Object insertColumn(String fkbdataid) {
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User)sre;
		String serviceType = user.getIndustryOrganizationApplication();
		int result = CommonLibKbdataAttrDAO.insertColumn(fkbdataid,serviceType);
		if (result == -1) {
			jsonObj.put("result", "该kbdataid已插入列");
		} else if(result == -2) {
			jsonObj.put("result", "查询该kbdataid插入列为null");
		} else if(result > 0) {
			jsonObj.put("result", "插入列成功");
		}
		return jsonObj;
	}
	
	/**
	 * 非问题库摘要是否已经继承
	 * @param kbdataid 儿子摘要id+要继承的父亲摘要id
	 * @return
	 */
	public static Object isInherited(String kbdataid) {
		// 拆分摘要id
		String cKbdataid = kbdataid.split("@")[0];// 儿子摘要id
		String fKbdataid = kbdataid.split("@")[1];// 父亲摘要id
		JSONObject jsonObj = new JSONObject();
		int result = 0;
	
		Result rs = CommonLibKbdataAttrDAO.isInherited(cKbdataid);
		if (rs != null && rs.getRowCount()>0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				Object obj = rs.getRows()[i].get("abstractid");
				if (obj != null) {
					if(obj.toString().equals(fKbdataid)) {
						result = 1;
						break;
					}
				}
			}
		}
		
		if (result>0) {// 已经有继承关系
			jsonObj.put("result", true);
		} else {
			jsonObj.put("result", false);
		}
		return jsonObj;
	}
	
	/**
	 * 根据继承关系向ServiceOrProductInfo中插入值
	 * @param fkbdataid 父kbdataid
	 * @param ckbdataid 子kbdataid
	 * @return
	 */
	public static Object insertColumnValue(String fkbdataid,String ckbdataid) {
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "添加失败!");
			return jsonObj;
		}
		User user = (User)sre;
		String serviceType = user.getIndustryOrganizationApplication();
		int result = CommonLibKbdataAttrDAO.insertColumnValue(fkbdataid, ckbdataid, MyClass.IndustryOrganizationApplication(),serviceType);
		if (result > 0) {
			jsonObj.put("result", "插入数据成功");
		} else {
			jsonObj.put("result", "插入数据失败");
		}
		return jsonObj;
	}
	
	
	/**
	 * 分页查询属性名称信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param name参数属性名称
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static Object SelectAttrName(String kbdataid, String name,
			int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		int result = CommonLibKbdataAttrDAO.SelectAttrName(kbdataid, name);
		// 判断数据源不为null且含有数据
		if (result > 0) {
			// 将总条数放入jsonObj的total对象中
			jsonObj.put("total", result);
			Result rs = CommonLibKbdataAttrDAO.SelectAttrName(kbdataid, name, page, rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj.put("id", rs.getRows()[i]
							.get("serviceattrname2colnumid"));
					// 生成name对象
					obj.put("name", rs.getRows()[i].get("name"));
					// 生成columnnum对象
					obj.put("columnnum", rs.getRows()[i].get("columnnum"));
					// 生成wordclassid对象
					obj.put("wordclassid", rs.getRows()[i]
							.get("wordclassid"));
					// 生成wordclass对象
					obj.put("wordclass", rs.getRows()[i].get("wordclass"));
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			jsonObj.put("rows", jsonArr);
		} else {
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 清空jsonArr数组
			jsonArr.clear();
			// 将空的jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		}
	
		return jsonObj;
	}
	
	/**
	 * 获取所有的列值
	 * 
	 * @param kbdataid参数摘要id
	 * @return 列值的json串
	 */
	public static Object GetColumn(String kbdataid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义属性名称的所有的列值的集合并赋值
		List<String> columnLst = new ArrayList<String>();
		// 循环遍历1-60
		for (int i = 0; i < 60; i++) {
			// 给属性名称的所有的列值赋值
			columnLst.add(String.valueOf(i + 1));
		}
		// 定义当前的列值集合
		List<String> columnNow = new ArrayList<String>();
	
		// 执行SQL语句，获取相应的数据源
		Result rs = CommonLibKbdataAttrDAO.GetColumn(kbdataid);
		// 判断数据源不为空且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 获取列值
				String columnnum = rs.getRows()[i].get("columnnum")
						.toString();
				// 将优先级放入当前的优先级集合中
				columnNow.add(columnnum);
			}
		}
	
		// 在所有的列值的集合中移除当前的列值集合
		columnLst.removeAll(columnNow);
		// 循环遍历剩余的列值集合
		for (int i = 0; i < columnLst.size(); i++) {
			JSONObject obj = new JSONObject();
			// 生成一个id对象
			obj.put("id", columnLst.get(i));
			// 生成一个text对象
			obj.put("text", columnLst.get(i));
			// 将obj的json对象放入jsonArr数组中
			jsonArr.add(obj);
		}
		// 将jsonArr数组放入jsonObj的rows对象中
		jsonObj.put("rows", jsonArr);
		return jsonObj;
	}

	/**
	 * 新增属性名称
	 * 
	 * @param kbdataid参数摘要id
	 * @param name参数属性名称
	 * @param column参数列值
	 * @param wordclass参数词类名称
	 * @return 新增是否成功
	 */
	public static Object InsertAttrName(String kbdataid, String name, String column, String wordclass) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "添加失败!");
			return jsonObj;
		}
		User user = (User)sre;
		String userIp = user.getUserIP();
		String userId =user.getUserID();
		String userName = user.getUserName();
		String serviceType = user.getIndustryOrganizationApplication();

		int result = 0;
		try {
			result = CommonLibKbdataAttrDAO.InsertAttrName(kbdataid, name, column, wordclass,serviceType);
			// 判断数据源不为null且含有数据
			if (result == -1) {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将属性名称已存在放入jsonObj的msg对象中
				jsonObj.put("msg", "属性名称已存在,请重新填写属性名称!");
				return jsonObj;
			} else {
				// 判断事务处理结果
				if (result > 0) {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将新增属性名称成功放入jsonObj的msg对象中
					jsonObj.put("msg", "新增属性名称成功!");
				} else {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将新增属性名称失败放入jsonObj的msg对象中
					jsonObj.put("msg", "新增属性名称失败!");
				}
				return jsonObj;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			// 清空jsonObj
			jsonObj.clear();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将新增属性名称失败放入jsonObj的msg对象中
			jsonObj.put("msg", "新增属性名称失败!");
			return jsonObj;
		}
	}

	/**
	 * 查询属性名称组成field和name,以及对应的属性值
	 * 
	 * @param kbdataid参数 父亲摘要id
	 * @return json串
	 */
	public static Object SelectAttrField(String kbdataid) {
		// 对应返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义属性名称和属性值的map集合
		Map<String, List<String>> attrnamevalueMap = new HashMap<String, List<String>>();

		Result rs = CommonLibKbdataAttrDAO.SelectAttrField(kbdataid);
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 获取属性名称
				String name = rs.getRows()[i].get("name").toString();
				// 获取列值
				String columnnum = rs.getRows()[i].get("columnnum").toString();

				// 对应json对象
				JSONObject obj = new JSONObject();
				// 生成name对象
				obj.put("name", name);
				// 生成wordclassid对象
				obj.put("wordclassid", rs.getRows()[i].get("wordclassid"));
				// 生成columnnum对象
				obj.put("columnnum", Integer.valueOf(columnnum));
				// 根据属性名称获取属性值(词条)集合
				List<String> valuelst = attrnamevalueMap.get(name);
				// 定义json数组
				JSONArray arr = new JSONArray();
				// 循环遍历属性值(词条)集合
				for (int j = 0; j < valuelst.size(); j++) {
					// 定义json对象
					JSONObject o = new JSONObject();
					// 生成id对象
					o.put("id", valuelst.get(j));
					// 生成text对象
					o.put("text", valuelst.get(j));
					// 将生成的对象放入arr数组中
					arr.add(o);
				}
				// 将属性值json数组放入obj的attrvalue对象中
				obj.put("attrvalue", arr);
				// 将生成的对象放入jsonArr数组中
				jsonArr.add(obj);
			}
		}
		// 将jsonArr数组放入jsonObj的rows对象中
		jsonObj.put("rows", jsonArr);
		
		return jsonObj;
	}

	/**
	 * 删除属性名称，并删除相关的信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param attrnameid参数属性名称id
	 * @param column参数对应列值
	 * @return 删除返回的json串
	 */
	public static Object DeleteAttrName(String kbdataid, String attrnameid,String column) {
		// 对应返回的json串
		JSONObject jsonObj = new JSONObject();
		//判断属性名称对应列值是否存在 若存在直接return，不予删除
		if(getAttrNameValue(kbdataid,column)){
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "属性名称对应列值已被录入,不予删除!");
			return jsonObj;
		}
		
		int result = CommonLibKbdataAttrDAO.DeleteAttrName(kbdataid, attrnameid, column);
		// 判断事务处理结果
		if (result > 0) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将删除成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "属性名称删除成功!");
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "属性名称删除失败!");
		}
		return jsonObj;
	}

	/**
	 *@description 判断属性名称对应列值是否存在
	 *@param kbdataid 参数摘要id
	 *@param column 对应列索引
	 *@return 
	 *@returnType boolean 
	 */
	public static boolean getAttrNameValue(String kbdataid,String column){
		Result rs  = CommonLibKbdataAttrDAO.getAttrNameValue(kbdataid, column);
		if(rs!=null&&rs.getRowCount()>0){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 修改属性名称
	 * 
	 * @param kbdataid参数摘要id
	 * @param attrnameid参数属性名称id
	 * @param name参数属性名称
	 * @return 修改返回的json串
	 */
	public static Object ModifyAttrName(String kbdataid, String attrnameid,
			String name) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		try {
			int result = CommonLibKbdataAttrDAO.ModifyAttrName(kbdataid, attrnameid, name);
			
			// 判断数据源不为null且含有数据
			if (result == -1) {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将属性名称已存在放入jsonObj的msg对象中
				jsonObj.put("msg", "属性名称已存在,请重新填写属性名称!");
				return jsonObj;
			} else {
				// 判断事务处理结果
				if (result > 0) {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将修改属性名称成功放入jsonObj的msg对象中
					jsonObj.put("msg", "属性名称修改成功!");
				} else {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将修改属性名称失败放入jsonObj的msg对象中
					jsonObj.put("msg", "属性名称修改失败!");
				}
				return jsonObj;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			// 清空jsonObj
			jsonObj.clear();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将修改属性名称失败放入jsonObj的msg对象中
			jsonObj.put("msg", "属性名称修改失败!");
			return jsonObj;
		}
	}

	/**
	 * 分页查询属性值
	 * 
	 * @param wordclassid参数词类id
	 * @param name参数词条名称
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static Object SelectAttrValue(String wordclassid, String name,
			int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		int result = CommonLibKbdataAttrDAO.SelectAttrValue(wordclassid, name);
		if (result > 0) {
			// 将总条数放入jsonObj的total对象中
			jsonObj.put("total", result);
			Result rs = CommonLibKbdataAttrDAO.SelectAttrValue(wordclassid, name, page, rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成wordid对象
					obj.put("wordid", rs.getRows()[i].get("wordid"));
					// 生成word对象
					obj.put("word", rs.getRows()[i].get("word"));
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		} else {
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 清空jsonArr数组
			jsonArr.clear();
			// 将空的jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		}
	
		return jsonObj;
	}

	/**
	 * 新增属性属性值
	 * 
	 * @param name参数属性值
	 * @param wordclassid参数词类名称id
	 * @param wordclass参数词类名称
	 * @return 新增返回的json串
	 */
	public static Object InsertAttrValue(String name, String wordclassid,
			String wordclass) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "添加失败!");
			return jsonObj;
		}
		User user = (User)sre;
		String userIp = user.getUserIP();
		String userId =user.getUserID();
		String userName = user.getUserName();
		String serviceType = user.getIndustryOrganizationApplication();
		int result = CommonLibKbdataAttrDAO.InsertAttrValue(name, wordclassid, wordclass, serviceType);
		// 判断数据源不为null且含有数据
		if (result == -1) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将属性名称已存在放入jsonObj的msg对象中
			jsonObj.put("msg", "属性值已存在,请重新填写属性值!");
			return jsonObj;
		} else {
			// 判断事务处理结果
			if (result > 0) {
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将属性值新增成功放入jsonObj的msg对象中
				jsonObj.put("msg", "属性值新增成功!");
			} else {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将属性值新增失败放入jsonObj的msg对象中
				jsonObj.put("msg", "属性值新增失败!");
			}
			return jsonObj;
		}
		
		
	}

	/**
	 * 修改属性值(词条)
	 * 
	 * @param attrvalueid参数属性值id
	 * @param name参数属性值
	 * @param wordclassid参数词类id
	 * @param column参数列值
	 * @param oldname参数原有的词条
	 * @param kbdataid参数摘要id
	 * @return 修改返回的json串
	 */
	public static Object ModifyAttrValue(String attrvalueid, String name,
			String wordclassid, String column, String oldname, String kbdataid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		
		int result = CommonLibKbdataAttrDAO.ModifyAttrValue(attrvalueid, name, wordclassid, column, oldname, kbdataid);
		// 判断数据源不为null且含有数据
		if (result == -1) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将属性名称已存在放入jsonObj的msg对象中
			jsonObj.put("msg", "当前词作为标准词,已经录入了别名,不能修改!");
			return jsonObj;
		} else {
			
			if (result == -2) {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将属性名称已存在放入jsonObj的msg对象中
				jsonObj.put("msg", "属性值已存在,请重新填写属性值!");
				return jsonObj;
			} else {
				// 判断事务处理结果
				if (result > 0) {
					// 将true放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将属性值修改成功放入jsonObj的msg对象中
					jsonObj.put("msg", "属性值修改成功!");
				} else {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将属性值修改失败放入jsonObj的msg对象中
					jsonObj.put("msg", "属性值修改失败!");
				}
				return jsonObj;
			}
		}
	}

	/**
	 * 删除属性值，并删除相关的信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param attrvalueid参数属性值id
	 * @param name参数词条
	 * @param column参数属性名称对应列值
	 * @param wordclass参数词类名称
	 * @return 删除返回的json串
	 */
	public static Object DeleteAttrValue(String kbdataid, String attrvalueid,
			String name, String column, String wordclass) {
		// 对应返回的json串
		JSONObject jsonObj = new JSONObject();
		int result = CommonLibKbdataAttrDAO.DeleteAttrValue(kbdataid, attrvalueid, name, column, wordclass);
		// 判断事务处理结果
		if (result > 0) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将删除成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "属性值删除成功!");
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "属性值删除失败!");
		}
		return jsonObj;
	}

	/**
	 * 分页查询服务或产品信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param topic类型
	 * @param selattr列名
	 * @param selattrValue列名及对应的值
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static Object SelectAttr(String kbdataid, String topic,String selattr, String selattrValue, int page,
			int rows) {
		// 子摘要id
		String cKbdataid = kbdataid.split("@")[0];
		// 父摘要id
		String fKbdataid = kbdataid.split("@")[1];
		String[] attrArr = selattr.split("@");
		
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		int result = CommonLibKbdataAttrDAO.SelectAttr(fKbdataid, cKbdataid, topic, selattr, selattrValue);
		// 判断数据源不为null且含有数据
		if (result > 0) {
			// 将总条数放入jsonObj的total对象中
			jsonObj.put("total", result);
			Result rs = CommonLibKbdataAttrDAO.SelectAttr(fKbdataid, cKbdataid, topic, selattr, selattrValue, page, rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成attrid对象
					obj.put("attrid", rs.getRows()[i].get("serviceorproductinfoid"));
					// 循环生成attr1-15对象
					for (String str : attrArr) {
						if (!topic.startsWith("复用-")) {
							if (str.equals("attr4") || str.equals("attr5") || str.equals("attr6")) {
								// 生成第j个attr对象
								obj.put(str, "");
							} else {
								// 生成第j个attr对象
								obj.put(str, rs.getRows()[i].get(str));
							}
						} else {
							// 生成第j个attr对象
							obj.put(str, rs.getRows()[i].get(str));
						}
					}
					// 生成status对象
					obj.put("status", rs.getRows()[i].get("status"));
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		} else {
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 清空jsonArr数组
			jsonArr.clear();
			// 将jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		}
		
		return jsonObj;
	}

	/**
	 * 新增服务或产品信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param attrArr参数属性值数组
	 * @return 新增返回的json串
	 */
	public static Object InsertAttr(String kbdataid,String[] attrArr) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "添加失败!");
			return jsonObj;
		}
		User user = (User)sre;
		String userIp = user.getUserIP();
		String userId =user.getUserID();
		String userName = user.getUserName();
		String serviceType = user.getIndustryOrganizationApplication();

		try {
			int result = CommonLibKbdataAttrDAO.InsertAttr(kbdataid, attrArr, serviceType);
			// 判断数据源不为null且含有数据
			if (result == -1) {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将重复信息放入jsonObj的msg对象中
				jsonObj.put("msg", "服务或产品信息已存在!");
				return jsonObj;
			} else {
				// 判断事务处理结果
				if (result > 0) {
					// 将true放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将新增成功放入jsonObj的msg对象中
					jsonObj.put("msg", "新增成功!");
				} else {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将新增失败放入jsonObj的msg对象中
					jsonObj.put("msg", "新增失败!");
				}
				return jsonObj;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将新增失败放入jsonObj的msg对象中
			jsonObj.put("msg", "新增失败!");
			return jsonObj;
		}
	}

	/**
	 * 删除服务或产品信息
	 * 
	 * @param attrid参数服务或产品信息id
	 * @return 删除返回的json串
	 */
	public static Object DeleteAttr(String attrid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		try {
			int result = CommonLibKbdataAttrDAO.DeleteAttr(attrid);
			// 判断事务处理结果
			if (result > 0) {
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将删除成功信息放入jsonObj的msg对象中
				jsonObj.put("msg", "删除成功!");
			} else {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将删除失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "删除失败!");
			}
			return jsonObj;
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}

	/**
	 * 确认服务或产品信息
	 * 
	 * @param attrid参数服务或产品信息id
	 * @return 确认返回的json串
	 */
	public static Object ConfirmAttr(String attrid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		try {
			int result = CommonLibKbdataAttrDAO.ConfirmAttr(attrid);
			// 判断事务处理结果
			if (result > 0) {
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将确认成功信息放入jsonObj的msg对象中
				jsonObj.put("msg", "确认成功!");
			} else {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将确认失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "确认失败!");
			}
			return jsonObj;
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将确认失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "确认失败!");
		}
		return jsonObj;
	}

	/**
	 * 更新服务或产品信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param attrArr参数属性值数组
	 * @param attrid参数服务或产品信息id
	 * @return 更新返回的json串
	 */
	public static Object UpdateAttr(String kbdataid, String[] attrArr,
			String attrid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		try {
			
			int result = CommonLibKbdataAttrDAO.UpdateAttr(kbdataid, attrArr, attrid);
			// 判断数据源不为null且含有数据
			if (result == -1) {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将重复信息放入jsonObj的msg对象中
				jsonObj.put("msg", "服务或产品信息已存在!");
				return jsonObj;
			}else if(result == -2){
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将重复信息放入jsonObj的msg对象中
				jsonObj.put("msg", "固定列不可进行修改!");
				return jsonObj;
			} else {
				// 判断事务处理结果
				if (result > 0) {
					// 将true放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将更新成功放入jsonObj的msg对象中
					jsonObj.put("msg", "更新成功!");
				} else {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将更新失败放入jsonObj的msg对象中
					jsonObj.put("msg", "更新失败!");
				}
				return jsonObj;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将更新失败放入jsonObj的msg对象中
			jsonObj.put("msg", "更新失败!");
			return jsonObj;
		}
	}

	/**
	 * 全量删除服务或产品信息
	 * 
	 * @param kbdataid参数 儿子摘要id+父亲摘要id
	 * @param topic 类型
	 * @return 全量删除返回的json串
	 */
	public static Object DeleteAllAttr(String kbdataid, String topic) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		try{
			// 执行SQL语句，绑定事务，返回事务处理结果
			int result = CommonLibKbdataAttrDAO.DeleteAllAttr(kbdataid, topic);
			// 判断事务处理结果
			if (result > 0) {
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将删除成功信息放入jsonObj的msg对象中
				jsonObj.put("msg", "全量删除成功!");
			} else {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将删除失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "全量删除失败!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "全量删除失败!");
		}
		return jsonObj;
	}

	/**
	 * 全量确认服务或产品信息
	 * 
	 * @param kbdataid参数摘要id
	 * @param 资源类型
	 * @return 全量确认返回的json串
	 */
	public static Object ConfirmAllAttr(String kbdataid,String type) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		try {
			// 执行SQL语句，绑定事务，返回事务处理结果
			int result = CommonLibKbdataAttrDAO.ConfirmAllAttr(kbdataid, type, MyClass.IndustryOrganizationApplication());
			// 判断事务处理结果
			if (result > 0) {
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将确认成功信息放入jsonObj的msg对象中
				jsonObj.put("msg", "全量确认成功!");
			} else {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将确认失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "全量确认失败!");
			}
			return jsonObj;
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将确认失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "全量确认失败!");
		}
		return jsonObj;
	}

	/**
	 * 将Excel文件中的数据导入到数据库中
	 * 
	 * @param kbdataid参数摘要id
	 * @param fileName参数文件名称
	 * @return 导入返回的json串
	 */
	public static Object ImportExcel(String kbdataid, String fileName) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获取上传文件的路径
		String pathName = Database.getJDBCValues("fileDirectory")
				+ File.separator + fileName;
		// 获取上传文件的file
		File file = new File(pathName);
		// 获取上传文件的类型
		String extension = fileName.lastIndexOf(".") == -1 ? "" : fileName
				.substring(fileName.lastIndexOf(".") + 1);
		// 定义存放读取Excel文件中的内容的集合
		List<List<Object>> comb = new ArrayList<List<Object>>();
		// 判断上传文件的类型来调用不同的读取Excel文件的方法
		if ("xls".equalsIgnoreCase(extension)) {
			// 读取2003的Excel方法
			comb = read2003Excel(file);
		} else if ("xlsx".equalsIgnoreCase(extension)) {
			// 读取2007的Excel方法
			comb = read2007Excel(file);
		}
		// 删除文件
		file.delete();
		// 调用新增服务或产品的方法，并返回事务处理结果
		int count = InsertServiceOrProductInfo(comb, kbdataid);
		// 判断事务处理结果
		if (count > 0) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将导入成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "导入成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将导入失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "导入失败!");
		}
		return jsonObj;
	}

	/**
	 * 新增服务或产品信息
	 * 
	 * @param attrinfo参数导入文件的内容
	 * @param kbdataid参数债哟id
	 * @return 返回事务处理结果
	 */
	public static int InsertServiceOrProductInfo(List<List<Object>> attrinfo, String kbdataid) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User)sre;
		String userIp = user.getUserIP();
		String userId =user.getUserID();
		String userName = user.getUserName();
		String serviceType = user.getIndustryOrganizationApplication();

		// 执行SQL语句，绑定事务返回事务处理结果
		int count = CommonLibKbdataAttrDAO.InsertServiceOrProductInfo(attrinfo, kbdataid, serviceType);
		return count;
	}

	/**
	 * 读取数据库，生成Excel文件，返回文件的路径
	 * 
	 * @param serviceid参数业务id
	 * @return 生成文件的路径
	 */
	public static Object ExportExcel(String serviceid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		
		// 定义存放生成Excel文件的所有内容的集合
		List<List<String>> attrinfoList = CommonLibKbdataAttrDAO.ExportExcel(serviceid);
		// 定义文件的路径
		String pathName = "";
		// 定义文件名称
		pathName = pathName + "服务或产品信息.xls";
		// 调用生成Excel2003的方法，并返回生成Excel文件的路径
		creat2003Excel(attrinfoList, pathName);
		// 定义文件对象
		File file = new File(Database.getJDBCValues("fileDirectory")
				+ File.separator + pathName);
		// 判断文件是否存在
		if (file.exists()) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将文件路径放入jsonObj的path对象中
			jsonObj.put("path", pathName);
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 * 读取 office 2003 excel
	 * 
	 * @param file参数文件
	 * @return 读取Excel文件内容的集合
	 */
	private static List<List<Object>> read2003Excel(File file) {
		List<List<Object>> list = new LinkedList<List<Object>>();
		try {
			HSSFWorkbook hwb = new HSSFWorkbook(new FileInputStream(file));
			HSSFSheet sheet = hwb.getSheetAt(0);
			Object value = null;
			HSSFRow row = null;
			HSSFCell cell = null;

			// 读取第一行
			row = sheet.getRow(0);
			List<Object> linked = new LinkedList<Object>();
			if (row != null) {
				for (int j = 0; j <= row.getLastCellNum(); j++) {
					cell = row.getCell(j);
					if (cell == null) {
						continue;
					}
					value = cell.getStringCellValue().trim();
					linked.add(value);
				}
				list.add(linked);
			}
			int count = linked.size();
			// 读取第一行以下的部分
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				linked = new LinkedList<Object>();
				for (int j = 0; j < count; j++) {
					cell = row.getCell(j);
					if (cell == null) {
						linked.add("");
					} else {
						switch (cell.getCellType()) {
						case XSSFCell.CELL_TYPE_STRING:
							value = cell.getStringCellValue().trim();
							break;
						case XSSFCell.CELL_TYPE_BLANK:
							value = "";
							break;
						default:
							value = cell.toString();
						}
						linked.add(value);
					}
				}
				list.add(linked);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 读取Office 2007 excel
	 * 
	 * @param file参数文件
	 * @return 读取Excel文件内容的集合
	 */
	private static List<List<Object>> read2007Excel(File file) {
		List<List<Object>> list = new LinkedList<List<Object>>();
		try {
			// 构造 XSSFWorkbook 对象，strPath 传入文件路径
			XSSFWorkbook xwb = new XSSFWorkbook(new FileInputStream(file));
			XSSFSheet sheet = xwb.getSheetAt(0);
			Object value = null;
			XSSFRow row = null;
			XSSFCell cell = null;
			// 读取第一行
			row = sheet.getRow(0);
			List<Object> linked = new LinkedList<Object>();
			if (row != null) {
				for (int j = 0; j <= row.getLastCellNum(); j++) {
					cell = row.getCell(j);
					if (cell == null || "".equals(cell)) {
						continue;
					}
					linked.add(cell.getStringCellValue().trim());
				}
				list.add(linked);
			}
			int count = linked.size();
			// 读取第一行以下的部分
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				linked = new LinkedList<Object>();
				for (int j = 0; j < count; j++) {
					cell = row.getCell(j);
					if (cell == null) {
						linked.add("");
					} else {
						switch (cell.getCellType()) {
						case XSSFCell.CELL_TYPE_STRING:
							value = cell.getStringCellValue().trim();
							break;
						case XSSFCell.CELL_TYPE_BLANK:
							value = "";
							break;
						default:
							value = cell.toString();
						}
						linked.add(value);
					}
				}
				list.add(linked);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 创建2003版本的Excel文件
	 * 
	 * @param attrinfo参数要生成文件的集合
	 * @param pathName参数文件路径
	 */
	private static void creat2003Excel(List<List<String>> attrinfo,
			String pathName) {
		try {
			HSSFWorkbook workBook = new HSSFWorkbook();// 创建 一个excel文档对象
			HSSFSheet sheet = workBook.createSheet();// 创建一个工作薄对象
			HSSFCellStyle style = workBook.createCellStyle();// 创建样式对象
			HSSFFont font = workBook.createFont();// 创建字体对象
			font.setFontHeightInPoints((short) 12);// 设置字体大小
			style.setFont(font);// 将字体加入到样式对象
			// 产生表格标题行
			for (int i = 0; i < attrinfo.size(); i++) {
				HSSFRow row = sheet.createRow(i);
				List<String> c = attrinfo.get(i);
				for (int j = 0; j < c.size(); j++) {
					HSSFCell cell = row.createCell(j);// 创建单元格
					cell.setCellValue(c.get(j));// 写入当前值
					cell.setCellStyle(style);// 应用样式对象
				}
			}
			FileOutputStream os = new FileOutputStream(Database
					.getJDBCValues("fileDirectory")
					+ File.separator + pathName);
			workBook.write(os);// 将文档对象写入文件输出流
			os.close();// 关闭文件输出流
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据服务或产品信息表更新属性值
	 * 
	 * @param kbdataid参数摘要id
	 * @return 更新是否成功
	 */
	public static Object UpdateAttrValue(String kbdataid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "添加失败!");
			return jsonObj;
		}
		User user = (User)sre;
		String userIp = user.getUserIP();
		String userId =user.getUserID();
		String userName = user.getUserName();
		String serviceType = user.getIndustryOrganizationApplication();
		// 执行SQL语句，绑定事务，返回事务处理结果
		int result = CommonLibKbdataAttrDAO.UpdateAttrValue(kbdataid,serviceType);
		if (result != -1) {
			// 判断事务处理结果
			if (result > 0) {
				// 事务处理成功
				// 判断加载属性名称是否成功
				if (loadAttrName()) {
					// 将true放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将信息放入jsonObj的msg对象中
					jsonObj.put("msg", "属性值更新成功后属性名称加载成功!");
				} else {
					// 将true放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将信息放入jsonObj的msg对象中
					jsonObj.put("msg", "属性值更新成功后属性名称加载失败!");
				}
			} else {
				// 事务处理失败
				// 判断加载属性名称是否成功
				if (loadAttrName()) {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将信息放入jsonObj的msg对象中
					jsonObj.put("msg", "属性值更新失败后属性名称加载成功!");
				} else {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将信息放入jsonObj的msg对象中
					jsonObj.put("msg", "属性值更新失败后属性名称加载失败!");
				}
			}
		} else {
			// 判断加载属性名称是否成功
			if (loadAttrName()) {
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将信息放入jsonObj的msg对象中
				jsonObj.put("msg", "属性值更新成功后属性名称加载成功!");
			} else {
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将信息放入jsonObj的msg对象中
				jsonObj.put("msg", "属性值更新成功后属性名称加载失败!");
			}
		}
		
		return jsonObj;
	}

	/**
	 * 加载属性名称
	 * 
	 * @return 加载属性名称是否成功
	 */
	public static boolean loadAttrName() {
		// 获取简要分析的客户端
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient.NLPAppWSClient();
		// 判断接口是否为null
		if (NLPAppWSClient == null) {
			// 返回加载属性名称失败
			return false;
		}
		try {
			// 调用接口的updateProcessController方法，返回加载属性名称成功
			return NLPAppWSClient.updateProcessController();
		} catch (Exception e) {
			e.printStackTrace();
			// 查询错误，返回加载属性名称失败
			return false;
		}
	}
	
	/**
	 * 构造全表查询所涉及的列
	 * @param serviceid 业务id
	 * @return
	 */
	public static Object constructColumn() { 
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获得四层结构信息，例如：电信行业->电信集团->4G业务客服应用
		String industry = MyClass.IndustryOrganizationApplication();
		// 根据业务id获得所有子业务id以及摘要
		
		Result rs = CommonLibKbdataAttrDAO.constructColumn(industry);
		if (rs != null && rs.getRowCount() > 0) {
			Object obj = rs.getRows()[0].get("maxnum");
			if (obj==null) {
				jsonObj.put("result", 13);
			} else {
				jsonObj.put("result", obj.toString());
			}
		}
		
		return jsonObj;
	}
	
	/**
	 * 构造查询所有摘要的配置所涉及的数据
	 * @param column 要查询的列数
	 * @param page 页码
	 * @param rows 每页多少条数据
	 * @param service 业务
	 * @param sabstract 问题库摘要
	 * @param cabstract 儿子摘要
	 * @param time 插入时间
	 * @return
	 */
	public static Object constructAllKbdataAttr(String column,int page,int rows,
			String service,String sabstract,String cabstract,String city,String serviceX,String extendCity) {
		Result rs = null;
		Result rs2 = null;
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 得到四层结构
		String industry = MyClass.IndustryOrganizationApplication();
		//industry ="电信行业->电信集团->4G业务客服应用";
		Map<Integer,Result> map = new HashMap<Integer, Result>();
		Map<Object,Result> map2 = new HashMap<Object, Result>();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		List<String> absCity = null;
		try {
			if(null==city||city.length()<=0){
				HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
						.resourseAccess(user.getUserID(), "querymanage", "S");
				List<String> cityList = resourseMap.get("地市");
				if(cityList.contains("全国")){
					city = null;
				}else{
					absCity = cityList;
				}
			}else{
				String[] cityStr = city.split(",");
				absCity = Arrays.asList(cityStr);
			}
			List<String> exCity = null;
			if(null!=extendCity&&extendCity.length()>0){
				String[] exCityStr = extendCity.split(",");
				exCity=Arrays.asList(exCityStr);
			}
			
			map = CommonLibKbdataAttrDAO.constructAllKbdataAttr(column, page, rows, service, sabstract, cabstract, absCity, industry, serviceX, MyClass.ServiceRoot(),exCity);
			if (!map.containsKey(0)) {
				int total = 0;
				for (Entry<Integer,Result> entry : map.entrySet()) {
					total = entry.getKey();
					rs = entry.getValue();
				}
				// json数组
				JSONArray jsonArr = new JSONArray();
				// 获得字段名
				String[] columnNames = rs.getColumnNames();
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject cJsonObj = new JSONObject();
					for (String columnName : columnNames) {
						Object obj = null;
						obj = rs.getRows()[i].get(columnName);
						if (obj == null || obj.toString().equals("")) {
							cJsonObj.put(columnName.toLowerCase(), "");
						} else {
							if (columnName.toLowerCase().equalsIgnoreCase("city")) {
								cJsonObj.put(columnName.toLowerCase(), getCityInfo(obj.toString()));
							} else if (columnName.toLowerCase().equalsIgnoreCase("attr15")){
								cJsonObj.put(columnName.toLowerCase(), getCityInfo(obj.toString()).equals("") ? obj.toString() : getCityInfo(obj.toString()));
								cJsonObj.put("attr15code", obj==null?"":obj.toString());
							} else {
								if (columnName.toLowerCase().equals("abstractid".toLowerCase())){
									map2 = CommonLibKbdataAttrDAO.constructAllKbdataAttr2(obj.toString());
									if (!map2.containsKey(0)){
										for (Entry<Object, Result> entry2 : map2.entrySet()){
											String abstractid = entry2.getKey().toString();
											rs2 = entry2.getValue();
										}
									}
									String[] columnNames2 = rs2.getColumnNames();
									for (int j = 0;j<rs2.getRowCount();j++){
										for (String columnName2 : columnNames2){
											if (!columnName2.equals("kbdataid")){
												Object obj2 = null;
												obj2 = rs2.getRows()[j].get(columnName2);
												cJsonObj.put(columnName2.toLowerCase(), obj2.toString());
											}
										}
									}
									
								}
								cJsonObj.put(columnName.toLowerCase(), obj.toString());
							}
						}
					}
					jsonArr.add(cJsonObj);
				}
				jsonObj.put("total",total);
				jsonObj.put("rows", jsonArr);
			} else {
				jsonObj.put("total",0);
				jsonObj.put("rows", new JSONArray());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
//		System.out.println(jsonObj.toString());
		return jsonObj;
	}
	
	/**
	 *@description  获得摘要字典
	 *@param serviceid 业务ID
	 *@param topic 知识条目
	 *@return 
	 *@returnType Map<String,String> 
	 */
	public static  Map<String, String> getAbstractMap(String serviceid,String topic) {
		// 获得摘要数据源
		Result rs = getAbstractByServiceidAndTopic(serviceid,topic);
		// 定存放摘要信息字典
		Map<String, String> map = new HashMap<String, String>();
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int n = 0; n < rs.getRowCount(); n++) {
				// 获取属性值
				Object abs = rs.getRows()[n].get("abstract");
				Object kbdataid = rs.getRows()[n].get("kbdataid");
				Object city = rs.getRows()[n].get("city");
				// 判断属性值是否为null
				if (abs != null && kbdataid != null) {
					// 将属性值放入新的set集合中
					map.put(kbdataid.toString() + "--" + city.toString(), abs.toString());
				}
			}
		} 
		Map<String,String> sortMap = MyUtil.sortMapByKey(map);
		return sortMap;
	}
	
	/**
	 *@description 通过业务ID查找，
	 *@param serviceid
	 *            业务ID
	 *@param topic  知识类别         
	 *@return
	 *@returnType Result
	 */
	public static Result getAbstractByServiceidAndTopic(String serviceid,String topic) {
		Result rs = CommonLibKbdataAttrDAO.getAbstractByServiceidAndTopic(serviceid, topic);
		return rs;
	}
	
	/**
	 * 获取父亲摘要id
	 * 
	 * @param kbdataid
	 *            传入的摘要id
	 * @param topic
	 *            类型
	 * @return
	 */
	public static String getFatherKbdataid(String kbdataid, String topic) {
		String fkbdataid = "";
		if (!topic.startsWith("复用-")) {// 不是问题库的摘要id
			
			Result rs = CommonLibKbdataAttrDAO.getFatherKbdataid(fkbdataid);
			if (rs != null && rs.getRowCount() > 0) {
				fkbdataid = rs.getRows()[0].get("abstractid").toString();
			}
			
		} else {
			fkbdataid = kbdataid;
		}
		return fkbdataid;
	}
	
	/**
	 *@description 批量继承非问题库某业务下所有未继承过的摘要
	 *@param  map 摘要ID和摘要字典 { 摘要ID：摘要名称}
	 *@return
	 *@returnType Object
	 */
	public static Object inheritAllAbstract(Map<String, String> map) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		//定义全局返回json
		jsonObj.put("success", false);
		// 将信息放入jsonObj的msg对象中
		jsonObj.put("msg", "无一知识条目完成继承!");
	   if(map.isEmpty()){
		jsonObj.put("success", false);
		// 将信息放入jsonObj的msg对象中
		jsonObj.put("msg", "当前业务知识类别下知识条目为空或已被删除!");
		return jsonObj;
	   }
	   //定义存放未被理解到的摘要路径
	   String path = DataSynchronizationDAO.dataSynPath + File.separator +"abstractinherit";
		// 判断文件夹是否存在,如果不存在则创建文件夹 创建失败返回
		if (!DataSynchronizationDAO.mkdir(path)) {
			jsonObj.put("success", false);
			// 将信息放入jsonObj的msg对象中
			jsonObj.put("msg", "创建记录未继承知识条目文件夹失败!");
			return jsonObj;
		}
		
		String abstractPath = path+File.separator+"abstract.txt";
		// 将业务下的摘要作为咨询依次调用高级分析接口并理解得到问题库下对应摘要信息
		// 根据服务获取相应的四层结构的接口入参的serviceinfo串
		String loginServiceType = MyClass.IndustryOrganizationApplication();
//		String serviceType = loginServiceType;
		String serviceType = loginServiceType.split("->")[0] + "->通用商家->问题库应用";
		String userId = MyClass.LoginUserId();
		// 遍历map，分别调用高级分析接口
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String abstractId = entry.getKey().split("--")[0];
			String city = entry.getKey().split("--")[1];
			
//			city ="对外";
			String citys[] = city.split(",");
			String finalcity = "";
			for (int i = 0 ; i < citys.length ; i++){
				finalcity = finalcity + cityMap2.get(citys[i]) + ",";
			}
			finalcity = finalcity.substring(0,finalcity.lastIndexOf(","));
			city = finalcity;
			String serviceInfo = MyUtil.getServiceInfo(serviceType, "继承高级分析", "",
					false,city);
//			String serviceInfo = MyUtil.getServiceInfo(serviceType, "继承高级分析", "",
//					false,city.replace(",", "|"));
			String sonAbstract = entry.getValue();
			String sonService = sonAbstract.split(">")[0].split("<")[1];
			String query = sonAbstract.split(">")[1];
			//记录继承摘要ID,方便之后跟踪继承
			String abstractidPath = path+File.separator+"abstractid.txt";
			if(!DataSynchronizationDAO.writeIntxtByline(abstractidPath, abstractId, false, "UTF-8")){
				jsonObj.put("success", false);
				// 将信息放入jsonObj的msg对象中
				jsonObj.put("msg", "写入跟踪知识条目ID失败!");
			}
			// 获取调用高级分析接口的接口串
			String queryObject = MyUtil.getDAnalyzeQueryObject(userId, query,
					serviceType, serviceInfo);
			logger.info("共享语义输入串："+queryObject);
			// 调用高级分析接口
			 JSONObject o =
			 QATrainingDAO.DetailAnalyzeResultForInherit(queryObject);
			 String newAbs=o.getString("abstract");
			 String fatherAbstractID = o.getString("abstractID");
			 String returnKeyValue = o.getString("returnvalue");
			 logger.info("共享语义输出串："+queryObject);
			if(o.isEmpty()){//如果分析结果为空，记录继承摘要
				Date currentTime = new Date();  
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String line = abstractId+"\t"+sonAbstract+"\t"+formatter.format(currentTime)+"s";
				if(!DataSynchronizationDAO.writeIntxtByline(abstractPath, line, true, "UTF-8")){
					jsonObj.put("success", false);
					// 将信息放入jsonObj的msg对象中
					jsonObj.put("msg", "写入未继承知识条目信息失败!");	
					return jsonObj;
				}
				continue;
			}
			 if("<未理解>应答".equals(newAbs)){//视为未理解
					Date currentTime = new Date();  
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String line = abstractId+"\t"+sonAbstract+"\t"+formatter.format(currentTime)+"s";
					if(!DataSynchronizationDAO.writeIntxtByline(abstractPath, line, true, "UTF-8")){
						jsonObj.put("success", false);
						// 将信息放入jsonObj的msg对象中
						jsonObj.put("msg", "写入未继承知识条目信息失败!");	
						return jsonObj;
					}
					continue; 
			 }
			//String returnValue[] = o.getString("returnvalue").split(",");
//			String newAbs = "<常用查询>测试继承";
//			String fatherAbstractID = "10684928";
//			String newService = "常用查询";
			//继承返回值默认key
			String returnValue[] = { "业务X", "业务Y","业务Z","业务L", "业务M","业务N" };

			List<String> returnValueList = new ArrayList<String>();
			for (int r = 0; r < returnValue.length; r++) {
				String returnString = returnValue[r];
				if (returnString.startsWith("业务")) {// 判断返回值key是否是继承相关返回值，如果是则加入returnValueList中
					returnValueList.add(returnString);
				}
			}
			// 查看被继承摘要 SERVICEATTRNAME2COLNUM 中是否存在列名对应关系，如果没有新建，如果和
			// returnValue比较有缺失则补全
			Result rs = getServiceattrname2colnum("abstractid", fatherAbstractID);
			// 定义存放已有列名
			List<String> colnumList = new ArrayList<String>();
			int i = 0;
			int colnum = 0;
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int n = 0; n < rs.getRowCount(); n++) {
					// 获取属性值
					Object name = rs.getRows()[n].get("name");
					colnumList.add(name.toString());
					colnum = Integer.valueOf(rs.getRows()[n].get("columnnum")
							.toString());
					if (colnum > i) {// 获得最大colnum值
						i = colnum;
					}
				}
				returnValueList.removeAll(colnumList);// 移除相同的列
				// 如果存在不同列，做补全
				if (returnValueList.size() > 0) {
					if (!KbdataAttrDAO
							.InsertAttrName(fatherAbstractID,
									(String[]) returnValueList
											.toArray(new String[returnValueList
													.size()]), i)) {
						jsonObj.put("success", false);
						// 将信息放入jsonObj的msg对象中
						jsonObj.put("msg", "插入元素对应关系表失败!");
						return jsonObj;
					}
				}
			} else {// 列不存在，新建
				List<String> nameList = Arrays.asList(names);
				returnValueList.removeAll(nameList);
				List<String> newList = new ArrayList<String>();
				newList.addAll(nameList);
				newList.addAll(returnValueList);
				if (!KbdataAttrDAO.InsertAttrName(fatherAbstractID,(String[]) newList.toArray(new String[newList.size()]),i)) {
					jsonObj.put("success", false);
					// 将信息放入jsonObj的msg对象中
					jsonObj.put("msg", "插入元素对应关系表失败!");
					return jsonObj;
				}
			}
			
			//重新加载继承信息表列对应关系
			rs = getServiceattrname2colnum("abstractid", fatherAbstractID);
			Map<String, String> nameAndcolnum = new HashMap<String,String>();
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int n = 0; n < rs.getRowCount(); n++) {
					// 获取属性值
					String name = rs.getRows()[n].get("name").toString();
					String col = rs.getRows()[n].get("columnnum").toString();
					nameAndcolnum.put(name, col);		
				}
			}
			//处理insert继承信息表参数
			Map<String,String> insertDic = new HashMap<String,String>();
			//绑定固定参数
			insertDic.put("4", sonService);//儿子业务
			insertDic.put("5", sonAbstract);//儿子摘要
			insertDic.put("6", abstractId);//儿子摘要ID
			insertDic.put("7", loginServiceType);//Business
			insertDic.put("15", city);//继承地市
			
			if(!"".equals(returnKeyValue)&&returnKeyValue!=null){
				String returnKeyValueArry [] = returnKeyValue.split(",");
				for(int x=0;x<returnKeyValueArry.length;x++){
					if("".equals(returnKeyValueArry[x])){
						continue;
					}
					String keyValueArry[] =  returnKeyValueArry[x].split("=");
					String key  = keyValueArry[0];
					String value = keyValueArry[1];
					if(nameAndcolnum.containsKey(key)){
						String index = nameAndcolnum.get(key);
						insertDic.put(index, value);
					}
				}
			}
			
			// 通过newAbstractID、abstractId及status 判断当前是否已经继承或已经继承但没审核
			// 如果没继承，建立继承关系，如果存在继承关系但没审核 ，删除当前继承关系重新建立继承关系，如继承且审核过，建立多继承关系
			if (insertAttr(abstractId,fatherAbstractID,insertDic)) {
				jsonObj.put("success", true);
				// 将信息放入jsonObj的msg对象中
				jsonObj.put("msg", "建立继承关系成功!");
			} else {
				jsonObj.put("success", false);
				// 将信息放入jsonObj的msg对象中
				jsonObj.put("msg", "建立继承关系失败!");
			}
		}
			
			// 通过newAbstractID、abstractId及status 判断当前是否已经继承或已经继承但没审核
			// 如果没继承，建立继承关系，如果存在继承关系但没审核 ，删除当前继承关系重新建立继承关系，如继承且审核过，建立多继承关系
//			if (insertAttr(abstractId, fatherAbstractID, sonService,
//					sonAbstract,loginServiceType)) {
//				jsonObj.put("success", true);
//				// 将信息放入jsonObj的msg对象中
//				jsonObj.put("msg", "建立继承关系成功!");
//			} else {
//				jsonObj.put("success", false);
//				// 将信息放入jsonObj的msg对象中
//				jsonObj.put("msg", "建立继承关系失败!");
//			}
//		}
		return jsonObj;

	}
	
	/**
	 *@description 通过列名及列值获得列与元素对应数据源
	 *@param id
	 *@param colum
	 *@return
	 *@returnType Result
	 */
	public static Result getServiceattrname2colnum(String colum,
			String columValue) {
		Result rs = CommonLibKbdataAttrDAO.getServiceattrname2colnum(colum, columValue);
		return rs;
	}
	
	/**
	 *@description 获得继承摘要数据源
	 *@param id
	 *@return 
	 *@returnType Result 
	 */
	public static Result getServiceOrproductInfo(String id) {
		Result rs = CommonLibKbdataAttrDAO.getServiceOrproductInfo(id);
		return rs;
	}
	
	/**
	 *@description 插入继承摘要相关信息
	 *@param abstractId
	 *            当前继承摘要ID
	 *@param fatherAbstractID
	 *            被继承摘要ID
	 *@param sonService
	 *            儿子业务
	 *@param sonAbstract
	 *            儿子摘要
	 *@return
	 *@returnType boolean
	 */
	public static boolean insertAttr(String currAbstractId,
			String fatherAbstractID, String sonService, String sonAbstract,String serviceType) {
		int result = CommonLibKbdataAttrDAO.insertAttr(currAbstractId, fatherAbstractID, sonService, sonAbstract, serviceType);
		
		// 判断事务处理结果
		if (result > 0) {
			return true;
		} else {
			return false;
		}

	}
	
	public static boolean insertAttr(String currabstractId,String fatherAbstractID, Map<String,String> indexAndvalue) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User)sre;
		String userIp = user.getUserIP();
		String userId =user.getUserID();
		String userName = user.getUserName();
		String serviceType = user.getIndustryOrganizationApplication();

		int result = CommonLibKbdataAttrDAO.insertAttr(currabstractId, fatherAbstractID, indexAndvalue, serviceType);
		// 判断事务处理结果
		if (result > 0) {
//			int result2 = 0;
//			result2 = CommonLibKbdataAttrDAO.updateKbdataFlag(currabstractId);
//			if (result2 > 0){
				return true;
//			} else {
//				return false;
//			}
		} else {
			return false;
		}

	}
	
	/**
	 *@description  更新词模返回值KEY=VALUE
	 *@param map
	 *@return 
	 *@returnType Object 
	 */
	public static Object updateWorpatKeyValue(Map<String, String> map){
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		return jsonObj;
	}
	
	/**
	 *@description 根据四层结构串，知识类别获得行业问题库下摘要词模数据源
	 *@param serviceType 四层结构串
	 *@param topic 知识类别
	 *@param serviceid 业务ID
	 *@return 
	 *@returnType Result 
	 */
	public static Result getAbstractAndWordpat(String serviceType,String serviceid,String topic){
		Result rs = CommonLibKbdataAttrDAO.getAbstractAndWordpat(serviceType, serviceid, topic);
		return rs;
		
	}
	
	/**
	 *@description  获得摘要词模字典 例：Map<摘要ID,Map<摘要名,Map<词模ID,词模>>>  
	 *@param serviceType 四层结构串
	 *@param topic 知识类别
	 *@param serviceid 业务ID
	 *@return 
	 *@returnType Map<String,Map<String,Map<String,String>>>  
	 */
	public static Map<String,Map<String,Map<String,String>>> getAbstractAndWordpatDic(String serviceType,String serviceid, String topic){
		Result rs = getAbstractAndWordpat(serviceType,serviceid,topic); 
		Map<String,Map<String,Map<String,String>>> map =  new HashMap<String, Map<String,Map<String,String>>>();
		Map<String,Map<String,String>> absAndWordpat = new HashMap<String, Map<String,String>>();
		 Map<String,String> wordpatidAndWordpat = new HashMap<String, String>();
			 //判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				for (int n = 0; n < rs.getRowCount(); n++) {
					String abs = rs.getRows()[n].get("abstract").toString();
					String kbdataid = rs.getRows()[n].get("kbdataid").toString();
					String wordpat = rs.getRows()[n].get("wordpat").toString();
					String wordpatid = rs.getRows()[n].get("wordpatid").toString();
					if(map.containsKey(kbdataid)){//如果字典中包含当前kbdataid key ,取值补全
						absAndWordpat = map.get(kbdataid);
						wordpatidAndWordpat = absAndWordpat.get(abs);
						wordpatidAndWordpat.put(wordpatid, wordpat);
						absAndWordpat.put(abs, wordpatidAndWordpat);
						map.put(kbdataid, absAndWordpat);
					}else{//反之直接put
						wordpatidAndWordpat = new HashMap<String, String>();
						absAndWordpat = new HashMap<String, Map<String,String>>();
						wordpatidAndWordpat.put(wordpatid, wordpat);
						absAndWordpat.put(abs.toString(), wordpatidAndWordpat);
						map.put(kbdataid, absAndWordpat);	
					}
				}
			} 
			return map;
	} 
	
	/**
	 *@description  获得词模字典  Map<词模ID,词模>
	 *@param serviceType 四层结构串
	 *@param serviceid   业务ID
	 *@param topic
	 *@return Map<String,String>
	 */
	public static  Map<String,String> getWordpatDic(String serviceType,String serviceid, String topic){
		Result rs = getAbstractAndWordpat(serviceType,serviceid,topic); 
		Map<String,String> wordpatidAndWordpat = new HashMap<String, String>();
		 //判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			for (int n = 0; n < rs.getRowCount(); n++) {
				String wordpat = rs.getRows()[n].get("wordpat").toString();
				String wordpatid = rs.getRows()[n].get("wordpatid").toString();
				wordpatidAndWordpat.put(wordpatid, wordpat);
			}
		} 
		return wordpatidAndWordpat;
	}
	
	/**
	 *@description  更新问题库继承摘要下词模返回值
	 *@param serviceType 四层串
	 *@param serviceid 业务ID
	 *@param topic 知识类别
	 *@return 
	 *@returnType Object 
	 */
	public static Object updateWorpatReturnValue(String serviceType,String serviceid, String topic){
		JSONObject jsonObj = new JSONObject();
		//定义全局返回json
		jsonObj.put("success", false);
		// 将信息放入jsonObj的msg对象中
		jsonObj.put("msg", "更新失败!");
		//获取参数配置知识点抽取信息value值
		Result rs = MetafieldmappingDAO.getConfigValue("知识点继承抽取信息配置","抽取信息补全");
		List<String> configValueList = new ArrayList<String>();
		for (int n = 0; n < rs.getRowCount(); n++) {
			String value = rs.getRows()[n].get("name").toString();
			configValueList.add(value);
		}
		Map<String,String> map = new HashMap<String, String>();
		Map<String,String> wordpatidAndWordpat = getWordpatDic(serviceType,serviceid,topic);
		//String reg = "[[|]|>|<|:|(|)||]{1}";
		String str[]={} ;
	    
		for (Map.Entry<String, String> entry : wordpatidAndWordpat.entrySet()) {
			String wordpatid = entry.getKey();
			String wordpat = entry.getValue();
			wordpat = wordpat.replace(" ", "");
			String beforeWordpat =  null;
			if (wordpat.indexOf("@1#") != -1) {
				str = wordpat.split("@1#");
				beforeWordpat = str[0]+"@1#";
			}
			if (wordpat.indexOf("@2#") != -1) {
				str = wordpat.split("@2#");
				beforeWordpat = str[0]+"@2#";
			}
			String[] frontItems = str[0].split("\\*");
			String[] afterItems = str[1].split("&");
			List<String> wordclassList =  new ArrayList<String>();
//			for(int i=0;i<frontItems.length;i++){
//				String strstr = frontItems[i].replaceAll("\\+","").replaceAll("\\~", "");
//				List<String> lsttmp = GlobalValues.splitByRegx(reg, strstr);
//				//List<String> lsttmp = GlobalValues.splitByRegx(reg, strstr);
//				for(int k=0;k<lsttmp.size();k++){//遍历拆分词类或子句
//					String strtmp = lsttmp.get(k);
//					if(strtmp.contains("父类")||strtmp.contains("父子句")||strtmp.contains("业务子句")){//记录父类或者子句结尾的
//						wordclassList.add(strtmp);
//					}
//				}
//			}
			
			for(int i=0;i<frontItems.length;i++){
				String strstr = frontItems[i].replaceAll("\\+","").replaceAll("\\~", "");
			    if(!strstr.contains("|")){
			    	if(strstr.contains("父类")||strstr.contains("父子句")||strstr.contains("业务子句")){//记录父类或者子句结尾的
						wordclassList.add(strstr);
					}	
			    }
				
				
			}
			//清除之前的继承抽取信息
			List<String> afterItemsList =new ArrayList<String>(Arrays.asList(afterItems));
			for(int m =0;m<configValueList.size();m++){
			for(int l =0 ;l<afterItems.length;l++){
				String  afterItem = afterItems[l];
					 if(afterItem.startsWith(configValueList.get(m))){
						 afterItemsList.remove(afterItem);
					 }
				 }
			}
			//根据afterItemsList合并词模
			for(int n =0;n<afterItemsList.size();n++){
				if(n==0){
					beforeWordpat = beforeWordpat+afterItemsList.get(n);
				}else{
					beforeWordpat = beforeWordpat+"&"+afterItemsList.get(n);
				}
			}
			wordpat = beforeWordpat;
			if(wordclassList.size()<configValueList.size()+1){//处理集合size小于补全抽取信息集合
				for(int j =0;j<wordclassList.size();j++){
					String retrunKey = configValueList.get(j);
					String  retrunValue  = wordclassList.get(j);
//					String retruntemp = "&"+retrunKey + "=<"+retrunValue+">" ;
					String retruntemp = "&"+retrunKey + "="+retrunValue ;
					wordpat.replace(retruntemp, "");
					wordpat = wordpat + retruntemp;	
				}
				map.put(wordpatid, wordpat);
			}
		}
		//批量更新词模
		int c = updateWordpat(map,MyClass.LoginUserId());
		if(c>0){
			jsonObj.put("success", true);
			// 将信息放入jsonObj的msg对象中
			jsonObj.put("msg", "更新成功!");
		}
		
	    return  jsonObj;
		
	}
	
	/**
	 *@description 批量更新词模
	 *@param map 词模字典  map<id,wordpat>
	 *@param userid 用户ID
	 *@return 
	 *@returnType int 
	 */
	public static int updateWordpat(Map<String, String> map,String workerid){
		int result = CommonLibKbdataAttrDAO.updateWordpat(map, workerid);
		return result;
	}

	/**
	 *@description 获得参数配置表具体值数据源
	 *@param name  配置参数名
	 *@param key   配置参数名对应key
	 *@return 
	 *@returnType Result 
	 */
	public static Result getConfigValue(String name ,String key){
	   Result rs = CommonLibKbdataAttrDAO.getConfigValue(name, key);
	   return rs;
	}
	
	public static boolean InsertAttrName(String kbdataid, String columnNames[],
			int column) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User)sre;
		String userIp = user.getUserIP();
		String userId =user.getUserID();
		String userName = user.getUserName();
		String serviceType = user.getIndustryOrganizationApplication();

		int result = CommonLibKbdataAttrDAO.InsertAttrName(kbdataid, columnNames, column, serviceType);
		// 判断事务处理结果
		if (result > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 批量删除操作
	 * @return
	 */
	public static Object doDeleteInfo(String ids, String oldinfo) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		
		try {
			int result = CommonLibKbdataAttrDAO.doDeleteInfo(ids);
			if (result > 0) {
				Object sre = GetSession.getSessionByKey("accessUser");
				User user = (User)sre;
				String userIp = user.getUserIP();
				String userId =user.getUserID();
				String userName = user.getUserName();
				String serviceType = user.getIndustryOrganizationApplication();
				List<String> lstsql = new ArrayList<String>();
				List<List<?>> lstparam = new ArrayList<List<?>>();
				// 将操作日志SQL语句放入集合中
				lstsql.add(GetConfigValue.LogSql());
				// 将定义的绑定参数集合放入集合中
				String brand = serviceType.split("->")[1];
				lstparam.add(GetConfigValue.LogParam(user.getUserIP(), user
						.getUserID(), user.getUserName(), brand, " ", "删除继承",
						oldinfo, "INHERIT"));
				int result02 = Database.executeNonQueryTransaction(lstsql, lstparam);
				jsonObj.put("result", true);
			} else {
				jsonObj.put("result", false);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			jsonObj.put("result", false);
		}
		return jsonObj;
	}

	/**
	 * 复制操作
	 * @param serviceorproductinfoid 表serviceorproductinfo主键
	 * @return
	 */
	public static Object copyInfo(String serviceorproductinfoid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "添加失败!");
			return jsonObj;
		}
		User user = (User)sre;
		String userIp = user.getUserIP();
		String userId =user.getUserID();
		String userName = user.getUserName();
		String serviceType = user.getIndustryOrganizationApplication();

		int result = CommonLibKbdataAttrDAO.copyInfo(serviceorproductinfoid, serviceType);
		if (result > 0) {
			jsonObj.put("result", true);
		} else {
			jsonObj.put("result", false);
		}
		return jsonObj;
	}
	
	/**
	 * 大表更新数据操作
	 * @param array
	 * @return
	 */
	public static Object doSave(String[] array, String oldinfo) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 传入参数集合
		Map<String, Object> map = new HashMap<String, Object>();
		
		for (int i = 0; i < array.length; i++) {
			String[] str = array[i].split("-");
			if(str.length > 1) {// 有值
				map.put(str[0], str[1]);
			} else {// 无值
				map.put(str[0], null);
			}
		}	
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "添加失败!");
			return jsonObj;
		}
		User user = (User)sre;
		String userIp = user.getUserIP();
		String userId =user.getUserID();
		String userName = user.getUserName();
		String serviceType = user.getIndustryOrganizationApplication();

		int result = CommonLibKbdataAttrDAO.doSave(map, MyClass.IndustryOrganizationApplication(),serviceType);
		if (result == -1) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "已存在相同的数据！");
		} else if (result > 0) {
			List<String> lstsql = new ArrayList<String>();
			List<List<?>> lstparam = new ArrayList<List<?>>();
			// 将操作日志SQL语句放入集合中
			lstsql.add(GetConfigValue.LogSql());
			// 将定义的绑定参数集合放入集合中
			String brand = serviceType.split("->")[1];
			lstparam.add(GetConfigValue.LogParam(user.getUserIP(), user
					.getUserID(), user.getUserName(), brand, " ", "更新继承",
					oldinfo, "INHERIT"));
			int result02 = Database.executeNonQueryTransaction(lstsql, lstparam);
			jsonObj.put("success", true);
			jsonObj.put("msg", "修改成功！");
		} else if(result == 0) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "修改失败！");
		}
		return jsonObj;
	}
	
	/**
	 * 手动批量继承
	 * @param fckbdataids 父亲-儿子摘要id组合
	 * @param fkbdataid 要继承的父亲摘要id
	 * @param serviceArray 手动录入的业务X~M
	 * @return
	 */
	public static Object doManualInherit(String[] fckbdataids, String fkbdataid, String[] serviceArray, String oldinfo) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将添加成功放入jsonObj的msg对象中
			jsonObj.put("msg", "添加失败!");
			return jsonObj;
		}
		User user = (User)sre;
		String userIp = user.getUserIP();
		String userId =user.getUserID();
		String userName = user.getUserName();
		String serviceType = user.getIndustryOrganizationApplication();

		int result = CommonLibKbdataAttrDAO.doManualInherit(fckbdataids, fkbdataid, serviceArray, MyClass.IndustryOrganizationApplication(), serviceType,user,oldinfo);
			
		if (result == -1) {
			jsonObj.put("result", false);
			jsonObj.put("msg", "所选数据都已继承该问题库摘要！");
			return jsonObj;
		}
		if (result > 0) {
			jsonObj.put("result", true);
			jsonObj.put("msg", "继承成功！");
		} else {
			jsonObj.put("result", false);
			jsonObj.put("msg", "继承失败！");
		}
		return jsonObj;
	}
	
	/**
	 * 获取摘要地市
	 * @param kbdataid
	 * @return
	 */
	public static String getCities(String kbdataid){
	    List<Object> lstpara = new ArrayList<Object>();
		String sql ="select city from kbdata where kbdataid =?";
		//根据四层结构串获得brand
		lstpara.add(kbdataid);
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, lstpara.toArray());
			if(rs.getRowCount()>0){
				 return rs.getRows()[0].get("city")==null?"":rs.getRows()[0].get("city").toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 获取继承地市
	 * @param serviceorproductinfoid
	 * @return
	 */
	public static String getExtendCities(String serviceorproductinfoid){
	    List<Object> lstpara = new ArrayList<Object>();
		String sql ="SELECT attr15 FROM Serviceorproductinfo WHERE serviceorproductinfoid=?";
		//根据四层结构串获得brand
		lstpara.add(serviceorproductinfoid);
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, lstpara.toArray());
			if(rs.getRowCount()>0){
				 return rs.getRows()[0].get("attr15")==null?"":rs.getRows()[0].get("attr15").toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
}
