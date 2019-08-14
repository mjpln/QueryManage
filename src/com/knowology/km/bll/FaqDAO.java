package com.knowology.km.bll;

import java.sql.Clob;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeSet;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.impl.xb.xsdschema.All;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibFaqDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibServiceAttrDao;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.bll.CommonLibWordDAO;
import com.knowology.dal.Database;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.entity.RealRobotInfo;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyClass;
import com.knowology.km.util.MyUtil;
import com.str.NewEquals;

public class FaqDAO {
	
	public static Map<String, RealRobotInfo> REALROBOTID_DIC = new HashMap<String, RealRobotInfo>();
	
	
	// 初始化一些数据
	static {
		initRealRobotParams();
	}
	
	/**
	 * 初始化实体机器人参数
	 */
	public static void initRealRobotParams(){
		Result rs = CommonLibFaqDAO.getRobotID();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String id = rs.getRows()[i].get("name") == null ? "" : rs.getRows()[i].get("name").toString();
				String name = rs.getRows()[i].get("k") == null ? "" : rs.getRows()[i].get("k").toString();
				
				if (StringUtils.isBlank(id) || StringUtils.isBlank(name) || !StringUtils.contains(name, ":")) {
					continue;
				}
				
				String code = name.split(":")[0];
				String value = name.split(":")[1];
				
				RealRobotInfo robot = null;
				if ( !REALROBOTID_DIC.containsKey(id) ){
					robot = new RealRobotInfo();
				} else {
					robot = REALROBOTID_DIC.get(id);
				}
				
				if ("name".equalsIgnoreCase(code)){
					robot.setName(value);
				} else if ("city".equalsIgnoreCase(code)) {
					robot.setCity(value);
				} else if ("citycode".equalsIgnoreCase(code)) {
					robot.setCityCode(value);
				} else if ("mac".equalsIgnoreCase(code)) {
					robot.setMac(value);
				} else if ("serviceposition".equalsIgnoreCase(code)) {
					robot.setServicePosition(value);
				}
				REALROBOTID_DIC.put(id, robot);
			}
		}
	}
	
	/**
	 *@description  查询答案信息
	 *@param kbdataid
	 *@param page
	 *@param rows
	 *@return 
	 *@returnType Object 
	 */
	public static Object select(String kbdataid,int page,int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		int count = CommonLibFaqDAO.getAnswerCountNew(serviceType, kbdataid);
		// 判断数据源不为null且含有数据
		if (count > 0) {
			// 将数据源的条数放入jsonObj的total对象中
			jsonObj.put("total", count);
			Result rs = CommonLibFaqDAO.selectNew(serviceType, kbdataid, page,
					rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					Object answerStr = rs.getRows()[i].get("answercontent");
					Object answerStr_clob = rs.getRows()[i].get("answer_clob");
					if (answerStr_clob != null && !"".equals(answerStr_clob)) {
						answerStr_clob = MyUtil.oracleClob2Str((Clob) answerStr_clob);
					}
					if (answerStr_clob != null && !"".equals(answerStr_clob)) {
						// 生成答案对象
						obj.put("answer", answerStr_clob);
					} else {
						obj.put("answer", answerStr);
					}

					// 生成渠道对象
					obj.put("channel", rs.getRows()[i].get("channel"));
					// 生成业务类型对象
					obj.put("servicetype", rs.getRows()[i].get("servicetype"));
					// 生成客户类型对象
					obj
							.put("customertype", rs.getRows()[i]
									.get("customertype"));
					// 生成kbansvaliddateid对象
					obj.put("kbansvaliddateid", rs.getRows()[i]
							.get("kbansvaliddateid"));
					// 生成开始时间对象
					obj.put("starttime", rs.getRows()[i].get("begintime"));
					// 生成就是数据对象
					obj.put("endtime", rs.getRows()[i].get("endtime"));
					// 生成答案类型对象
					obj
							.put("answertype", rs.getRows()[i]
									.get("answercategory"));
					// 生成kbanswerid对象
					obj.put("kbanswerid", rs.getRows()[i].get("kbanswerid"));
					// 生成kbcontentid对象
					obj.put("kbcontentid", rs.getRows()[i].get("kbcontentid"));
					// 生成city对象
					obj.put("city", rs.getRows()[i].get("city") == null ? "" : QuerymanageDAO.getCityName(rs.getRows()[i].get("city")+""));
					obj.put("citycode", rs.getRows()[i].get("city"));
					// 生成userid对象
					obj.put("userid", rs.getRows()[i].get("userid") == null ? "" : rs.getRows()[i].get("userid").toString());
					obj.put("robotname", rs.getRows()[i].get("userid") == null ? "" : getRobotName(rs.getRows()[i].get("userid").toString()));
					// 生成excludedcity对象
					//obj.put("excludedcity", rs.getRows()[i].get("excludedcity") == null ? "" : QuerymanageDAO.getCityName(rs.getRows()[i].get("excludedcity").toString()));
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的root对象中
			jsonObj.put("rows", jsonArr);
		} else {
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 将空jsonArr数组放入jsonObj的root对象中
			jsonObj.put("rows", jsonArr);
		}

		return jsonObj;
	}
	

	/**
	 * 获取客户类型
	 * 
	 * @return 客户类型的json串
	 */
	public static Object getCustomerOld() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义查询客户类型的SQL语句
		String sql = "select distinct customertype from customertype";
		try {
			// 执行SQL语句获取相应的数据源
			Result rs = Database.executeQuery(sql);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成客户类型对象
					obj
							.put("customertype", rs.getRows()[i]
									.get("customertype"));
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将数组放入jsonObj的root对象中
			jsonObj.put("root", jsonArr);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将空数组放入jsonObj的root对象中
			jsonObj.put("root", jsonArr);
		}
		return jsonObj;
	}

	/**
	 * 获取客户类型
	 * 
	 * @return 客户类型的json串
	 */
	public static Object getCustomer() {

		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库参数配置",
				"客户类型");
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String name = rs.getRows()[i].get("name").toString();
				jsonObj.put("id", name);
				jsonObj.put("text", name);
				array.add(jsonObj);
			}
		}
		return array;
	
	}
	
	
	/**
	 * 根据地市id获取地市名称
	 * 
	 * @param param参数json串
	 * @return 地市名称的json串
	 */
	public static Object getCity(JSONObject param) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义查询地市的SQL语句
		String sql = "select city from city where cityid=? ";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定地市id参数
		lstpara.add(param.getString("cityid"));
		try {
			// 执行SQL语句获取相应的数据源
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成地市名称对象
					obj.put("city", rs.getRows()[i].get("city"));
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将数组放入jsonObj的root对象中
			jsonObj.put("root", jsonArr);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将空数组放入jsonObj的root对象中
			jsonObj.put("root", jsonArr);
		}
		return jsonObj;
	}

	/**
	 * 获取渠道信息
	 * 
	 * @return 渠道的json串
	 */
	public static Object getChannel() {

		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("渠道参数配置",
				"渠道");
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String name = rs.getRows()[i].get("name").toString();
				jsonObj.put("id", name);
				jsonObj.put("text", name);
				array.add(jsonObj);
			}
		}
		return array;
	
	}
	
	/**
	 * 获取答案类型下拉
	 * 
	 * @return 渠道的json串
	 */
	public static Object getAnswerType() {

		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库参数配置",
				"答案类型");
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String [] name  = rs.getRows()[i].get("name").toString().split("#");
				String code = name[1];
				String text = name[0];
				jsonObj.put("id", code);
				jsonObj.put("text", text);
				array.add(jsonObj);
			}
		}
		return array;
	
	}
	
	/**
	 * 获取回复口径类型下拉
	 * 
	 * @return 渠道的json串
	 */
	public static Object getReplyCaliberType() {

		Result rs = CommonLibMetafieldmappingDAO.getConfigKey2("安徽电信口径库");
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String name  = rs.getRows()[i].get("name").toString();
				jsonObj.put("id", name);
				jsonObj.put("text", name);
				array.add(jsonObj);
			}
		}
		return array;
	
	}
	
	/**
	 * 获取口径文本下拉
	 * 
	 * @return 渠道的json串
	 */
	public static Object getCaliberText(String caliberTypeId) {
		
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("安徽电信口径库",caliberTypeId);
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String name  = rs.getRows()[i].get("name").toString();
				jsonObj.put("id", name);
				jsonObj.put("text", name);
				array.add(jsonObj);
			}
		}
		return array;
	
	}
	
	
	/**
	 * 获取答案类型下拉
	 * 
	 * @return 渠道的json串
	 */
	public static Object getServiceInfo() {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		Result rsConfig = CommonLibMetafieldmappingDAO.getConfigValue("模板业务根对应配置",serviceType);
		// 定义返回的json串
		JSONArray array = new JSONArray();
		if(rsConfig != null && rsConfig.getRowCount()>0){
			for (int n = 0; n < rsConfig.getRowCount(); n++) {
				String brand = rsConfig.getRows()[n].get("name").toString();
				Result rs = CommonLibServiceDAO.getServiceInfoName(brand);
				
				if (rs != null && rs.getRowCount() > 0) {
					for (int i = 0; i < rs.getRowCount(); i++) {
						JSONObject jsonObj = new JSONObject();
						String service = rs.getRows()[i].get("service").toString();
						String serviceid = rs.getRows()[i].get("serviceid").toString();
						jsonObj.put("id", serviceid);
						jsonObj.put("text", service);
						array.add(jsonObj);
					}
				}
			}
		}
		
		return array;
	
	}

	public static void main(String[] args) {
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("模板业务根对应配置",
		"基金行业->华夏基金->多渠道应用");
		System.out.println(rs.getRows()[0].get("name").toString());
	}

	/**
	 *@description  获得业务下文档名称
	 *@param serviceid
	 *@return 
	 *@returnType Object 
	 */
	public static Object getKnoName(String serviceid) {
		Result rs = CommonLibServiceAttrDao.selectDocname(serviceid, "docname");
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String docName = rs.getRows()[i].get("attr").toString();
				jsonObj.put("id", docName);
				jsonObj.put("text", docName);
				array.add(jsonObj);
			}
		}
		return array;
	}
	
	/**
	 * 获取所有业务表下属性名对应的内容
	 * @param city
	 * @return
	 */
	public static Object getAttrValuesAll(String city){
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("模板业务根对应配置",serviceType);
		String brand = rs.getRows()[0].get("name").toString();
		// 获取所有信息表
		rs = CommonLibServiceDAO.getServiceInfoName(brand);
		
		Result colRs = null;
		Result attrValues = null;
		// 定义返回的json串
		JSONArray array = new JSONArray();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String service = rs.getRows()[i].get("service").toString();
				String serviceid = rs.getRows()[i].get("serviceid").toString();
				
				// 获取所有属性
				colRs = CommonLibServiceAttrDao.getServiceAttributions2(serviceid);
				if(colRs != null && colRs.getRowCount() > 0){
					for(int j = 0; j < colRs.getRowCount(); j++){
						String name = (colRs.getRows()[j].get("name") == null ? "" : colRs.getRows()[j].get("name").toString());
						String semanticskeyword = (colRs.getRows()[j].get("semanticskeyword") == null ? "" : colRs.getRows()[j].get("semanticskeyword").toString());
						if(StringUtils.isNotEmpty(semanticskeyword)){
							name = semanticskeyword;
						}
						String column = (colRs.getRows()[j].get("columnnum") == null ? null : colRs.getRows()[j].get("columnnum").toString());
						if(StringUtils.isNumeric(column)){
							
							// 获取信息表下的所有属性
							if("all".equals(city)){
								attrValues = CommonLibServiceAttrDao.selectColumnValue(serviceid,column);
							}else{
								String newCity = city.split(",")[0].substring(0,2);
								attrValues = CommonLibServiceAttrDao.selectColumnValue(serviceid,column,newCity);
							}
							if (attrValues != null && rs.getRowCount() > 0) {
								for (int k = 0; k < attrValues.getRowCount(); k++) {
									JSONObject jsonObj = new JSONObject();
									String attrvalue = attrValues.getRows()[k].get("attr").toString();
									jsonObj.put("id", attrvalue);
									jsonObj.put("text", attrvalue);
									jsonObj.put("service", service);
									jsonObj.put("serviceid", serviceid);
									jsonObj.put("attrname", name);
									array.add(jsonObj);
								}
							}
						}
					}
					
				}
			}
		}
		return array;
	}
	
	/**
	 * 获取业务表下属性名对应的内容
	 * @param serviceid
	 * @param column
	 * @return
	 */
	public static Object getAttrValues(String serviceid, String column,String city){
		if(StringUtils.isEmpty(serviceid) && StringUtils.isEmpty(column)){
			return getAttrValuesAll(city);
		}
		
		Result rs  =null;
		if("all".equals(city)){
			rs = CommonLibServiceAttrDao.selectColumnValue(serviceid,column);
		}else{
			String newCity = city.split(",")[0].substring(0,2);
			rs = CommonLibServiceAttrDao.selectColumnValue(serviceid,column,newCity);
		}
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String attrvalue = rs.getRows()[i].get("attr").toString();
				jsonObj.put("id", attrvalue);
				jsonObj.put("text", attrvalue);
				array.add(jsonObj);
			}
		}
		return array;
	}
	
	/**
	 *@description  获得业务下模板列
	 *@param serviceid
	 *@return 
	 *@returnType Object 
	 */
	public static Object getTemplateColumn(String serviceid) {
		Result rs = CommonLibServiceAttrDao.getColumnName(serviceid);
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String columnnum = rs.getRows()[i].get("name").toString();
				jsonObj.put("id", columnnum);
				jsonObj.put("text", columnnum);
				array.add(jsonObj);
			}
		}
		return array;
	}
	
	
	/**
	 *@description 通过业务ID，列元素查询 列元素语义关键词
	 *@param serviceid
	 *@param name
	 *@return 
	 *@returnType Object 
	 */
	public static Object getSemanticsKeyWordName(String serviceid) {
		Result rs = CommonLibServiceAttrDao.getSemanticsKeyWordName(serviceid, "docName");
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
				jsonObj = new JSONObject();
				String semanticskeyword = rs.getRows()[0].get("name") == null ? "":rs.getRows()[0].get("name").toString();
				jsonObj.put("success", true);
				jsonObj.put("name", semanticskeyword);
		}else{
			jsonObj.put("success", false);
			jsonObj.put("name", "");
		}
		return jsonObj;
	
	}
	
	
	/**
	 * 获取渠道信息
	 * 
	 * @return 渠道的json串
	 */
	public static Object getChannelOld() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义查询渠道的SQL语句
		String sql = "select distinct channel from t_channel ";
		try {
			// 执行SQL语句获取相应的数据源
			Result rs = Database.executeQuery(sql);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成渠道对象
					obj.put("channel", rs.getRows()[i].get("channel"));
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将数组放入jsonObj的root对象中
			jsonObj.put("root", jsonArr);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将空数组放入jsonObj的root对象中
			jsonObj.put("root", jsonArr);
		}
		return jsonObj;
	}

	
	/**
	 * 插入答案知识
	 * 
	 * @param param参数json串
	 * @return 插入返回的json串
	 */
	@SuppressWarnings("unchecked")
	public static Object insertOrUpdate(String channel,String customerType,String starttime,String endtime,String answerType,String answer, String service,String brand,String kbdataid,String kbansvaliddateid,String type,String city, String userid) {
		answer = answer.replaceAll("<p>", "").replaceAll("</p>","");
		
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray msgs = new JSONArray();
		channel = channel.replace("@@", "");
		// 将渠道按照逗号拆分
		String[] channels = channel.split(",");
		city = city.replace("@@", "");
		if(city.contains("全国")){
			city = "全国";
		}
		// 定义多条SQL语句结果
		List<String> listSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> listParam = new ArrayList<List<?>>();
		// 定义绑定参数集合 
		List<String> lstpara = new ArrayList<String>();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			// 将0放入jsonObj的total对象中
			jsonObj.put("success", false);
			// 将重复信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时，请注销后重新登录");
			return jsonObj;
		}
		User user = (User)sre;
		// 获取登录的商家组织应用
		String servicetype = user.getIndustryOrganizationApplication();
		// 定义SQL语句
		String sql = "";
		// 判断有效期是否符合规范
		if (!"".equals(starttime) && !"".equals(endtime) && starttime != null
				&& endtime != null) {
			starttime = starttime.split("T")[0] + " 00:00:00";
			endtime = endtime.split("T")[0] + " 23:59:59";
			JSONObject obj = compareTime(starttime, endtime);
			if (obj.containsKey("success")) {
				return obj;
			}
		}

		Map channelPersonalityMap = new HashMap<String, Result>();
		// 判断当前摘要下商家 +客户类型+渠道 是否已经存在。
		// 循环遍历渠道数组
		for (int j = 0; j < channels.length; j++) {
			Result sameChannelAnswers = UserOperResource.isExistAnswer("insert", kbdataid, kbansvaliddateid, channels[j], servicetype, userid, customerType, starttime, endtime);
			// 判断数据源不为null且含有数据
			if (sameChannelAnswers != null && sameChannelAnswers.getRowCount() > 0) {
				List<SortedMap> cityDupRows = getSameCityAnswers(sameChannelAnswers, city, kbansvaliddateid, type);
				
				String answerTypeName = "";
				// 库中已经有地市相同的答案了
				if(cityDupRows.size() > 0){
					// 获取答案类型信息
					if (NewEquals.equals(answerType,"0")) {
						answerTypeName = "普通文字";
					} else if (NewEquals.equals(answerType,"1")) {
						answerTypeName = "答案模型";
					} else if (NewEquals.equals(answerType,"2")) {
						answerTypeName = "触发动作";
					} else if (NewEquals.equals(answerType,"3")) {
						answerTypeName = "普通文字+触发动作";
					} else if (NewEquals.equals(answerType,"4")) {
						answerTypeName = "答案模型+触发动作";
					} else if(NewEquals.equals(answerType,"8")){
						answerTypeName = "FAQ";
					} else if(NewEquals.equals(answerType,"9")){
						answerTypeName = "结构化知识";
					}
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					
					for(SortedMap dupAnswer : cityDupRows){
						msgs.add("当前商家下客户类型：" + customerType 
								+ "，渠道：" + channels[j] 
								+ "，地市：" + QuerymanageDAO.getCityName(dupAnswer.get("existCity").toString()) 
								+ "，Robot ID：" + (dupAnswer.get("userid") == null ? "" : getRobotName(dupAnswer.get("userid").toString()))
								+ "  已存在");
					}
				}
				
				// 新增答案时获取相同条件的答案
				if("insert".equals(type)){
					channelPersonalityMap.put(channels[j], sameChannelAnswers);
				}
				
			}
		}
		
		// 修改时获取相同条件的答案
		if("update".equals(type)){
			Result updateAnswer = CommonLibFaqDAO.getAnswerByKbansvaliddateid(kbansvaliddateid);
			if(updateAnswer != null && updateAnswer.getRowCount() > 0){
				String updateAnswerChannel = (updateAnswer.getRows()[0].get("channel") == null ? "" : updateAnswer.getRows()[0].get("channel").toString());
				Result rs = CommonLibFaqDAO.getSameChannelAnswers(kbansvaliddateid);
				channelPersonalityMap.put(updateAnswerChannel, rs);
			} else {
				jsonObj.put("success", false);
				msgs.add("该答案不存在");
			}
		}
		
		if(!jsonObj.isEmpty() && !jsonObj.getBooleanValue("success")){
			jsonObj.put("msg", StringUtils.join(msgs, "</br>"));
			return jsonObj;
		}
		
		
		int c = UserOperResource.insertOrUpdateAnswer(user, type, kbdataid, kbansvaliddateid, channels, servicetype, customerType, starttime, endtime, answer, answerType, brand, servicetype, city, userid, channelPersonalityMap);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "失败!");
		}
		return jsonObj;
	}

	@SuppressWarnings("unchecked")
	private static List<SortedMap> getSameCityAnswers(Result rs, String value, String kbansvaliddateid, String type){
		List<SortedMap> dupRows = new ArrayList<SortedMap>();
		String city = "";
		String excludedCity = "";
		String kbansvaliddateID = "";
		TreeSet<String> citySet = new TreeSet<String>();
		TreeSet<String> excludedCitySet = new TreeSet<String>();
		TreeSet<String> valueSet = new TreeSet<String>(Arrays.asList(value.split(",")));
		SortedMap updatingRow = null;
		
		if("update".equals(type)){
			for(SortedMap row : rs.getRows()){
				kbansvaliddateID  = (row.get("kbansvaliddateid") == null ? "" : row.get("kbansvaliddateid").toString());
				if(NewEquals.equals(kbansvaliddateID,kbansvaliddateid)){
					updatingRow = row;
					break;
				}
			}
		}
		
		
		for(SortedMap row : rs.getRows()){
			if("update".equals(type) && row == updatingRow){
				continue;
			}
			
			city = (row.get("city") == null ? "" : row.get("city").toString());
			excludedCity = (row.get("excludedcity") == null ? "" : row.get("excludedcity").toString());
			citySet.clear();
			excludedCitySet.clear();
			citySet.addAll(Arrays.asList(city.split(",")));
			excludedCitySet.addAll(Arrays.asList(excludedCity.split(",")));
			citySet.removeAll(excludedCitySet);
			
			// 该row为有效答案
			if(citySet.size() > 0 ){
				// 排除掉地市包含省或全国的答案
				if((citySet.contains("全国") || isContainProvince(citySet)) && !valueSet.contains("全国") && !isContainProvince(valueSet)){
					continue;
				}

				if("update".equals(type)){
					String updatingExcludedCity =  (updatingRow.get("excludedcity") == null ? "" : updatingRow.get("excludedcity").toString());
					TreeSet<String> updatingExcludedCitySet = new TreeSet<String>(Arrays.asList(updatingExcludedCity.split(",")));
					valueSet.removeAll(updatingExcludedCitySet);
				}
				
				// 取出交集地市
				citySet.retainAll(valueSet);
				// 新增地市在原答案中已经存在
				if(citySet.size() > 0){
					row.put("existCity", StringUtils.join(citySet, ","));
					dupRows.add(row);
				}
			}
		}
		return dupRows;
	}
	
	private static boolean isContainProvince(TreeSet<String> citySet){
		for(String cityCode : citySet){
			if(cityCode.endsWith("0000")){
				return true;
			}
		}
		return false;
	}
	
	
	
	/**
	 * 插入答案知识
	 * 
	 * @param param参数json串
	 * @return 插入返回的json串
	 */
	public static Object insertOrUpdateNew(JSONObject param, String type) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获取渠道
		String channel = param.getString("channel");
		// 获取customertype
		String customertype = param.getString("customertype");
		// 获取starttime
		String starttime = param.getString("starttime");
		// 获取endtime
		String endtime = param.getString("endtime");
		// 获取answerType
		String answerType = param.getString("answerType");
		// 获取answer
		String answer = param.getString("answer");
		// 获取service
		String service = param.getString("service");
		// 获取brand
		String brand = param.getString("brand");
		// 获取kbdataid
		String kbdataid = param.getString("kbdataid");
		// 将渠道按照逗号拆分
		String[] channels = channel.split(",");
		// 定义多条SQL语句结果
		List<String> listSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> listParam = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 获取登录的商家组织应用
		String servicetype = MyClass.IndustryOrganizationApplication();
		// 判断servicetype为空串、空、null
		if (" ".equals(servicetype) || "".equals(servicetype)
				|| servicetype == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("msg", "登录信息已失效,请注销后重新登录");
			return jsonObj;
		}
		// 定义SQL语句
		String sql = "";

		// 判断有效期是否符合规范
		if (!"".equals(starttime) && !"".equals(endtime) && starttime != null
				&& endtime != null) {
			starttime = starttime.split("T")[0] + " 00:00:00";
			endtime = endtime.split("T")[0] + " 23:59:59";
			JSONObject obj = compareTime(starttime, endtime);
			if (obj.containsKey("success")) {
				return obj;
			}
		}

		// 判断当前摘要下商家 +渠道+答案类型 是否已经存在
		// 循环遍历渠道数组
		for (int j = 0; j < channels.length; j++) {
			// 定义查询是否重复的SQL语句
			if ("".equals(starttime) || "".equals(endtime) || starttime == null
					|| endtime == null) {

				if ("update".equals(type)) {
					// 获取kbansvaliddateid
					String kvdid = param.getString("kbansvaliddateid");
					sql = "select * from kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f where b.kbdataid=c.kbdataid "
							+ "and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid "
							+ "and f.channel=? and f.answercategory=? and f.customertype=? and f.servicetype=? and b.kbdataid=?  and c.kbansvaliddateid !="
							+ kvdid + "";
					// 删除答案知识的SQL语句
					String deletesql = "delete from kbansvaliddate where kbansvaliddateid = ?";
					// 定义绑定参数集合
					lstpara = new ArrayList<String>();
					// 绑定kbansvaliddateid参数
					lstpara.add(kvdid);
					// 将SQL语句放入SQL语句集合中
					listSql.add(deletesql);
					// 将对应的绑定参数集合放入集合中
					listParam.add(lstpara);
				} else {
					sql = "select * from kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f where b.kbdataid=c.kbdataid "
							+ "and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid "
							+ "and f.channel=? and f.answercategory=? and f.customertype=? and f.servicetype=? and b.kbdataid=? ";

				}
			} else {
				if ("update".equals(type)) {
					// 获取kbansvaliddateid
					String kvdid = param.getString("kbansvaliddateid");
					sql = "select * from kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f where b.kbdataid=c.kbdataid "
							+ "and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid "
							+ "and f.channel=? and f.answercategory=? and f.customertype=? and f.servicetype=? and b.kbdataid=?  and c.kbansvaliddateid !="
							+ kvdid
							+ "   and  c.BEGINTIME is null and c.ENDTIME is null ";
					// 删除答案知识的SQL语句
					String deletesql = "delete from kbansvaliddate where kbansvaliddateid = ?";
					// 定义绑定参数集合
					lstpara = new ArrayList<String>();
					// 绑定kbansvaliddateid参数
					lstpara.add(kvdid);
					// 将SQL语句放入SQL语句集合中
					listSql.add(deletesql);
					// 将对应的绑定参数集合放入集合中
					listParam.add(lstpara);
				} else {
					sql = "select * from kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f where b.kbdataid=c.kbdataid "
							+ "and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid "
							+ "and f.channel=? and f.answercategory=? and f.customertype=? and f.servicetype=? and b.kbdataid=? and  c.BEGINTIME is null and c.ENDTIME is null";

				}

			}

			// 定义绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定渠道参数
			lstpara.add(channels[j]);
			// 绑定答案类型参数
			lstpara.add(answerType);
			// 绑定客户类型参数
			lstpara.add(customertype);
			// 绑定业务类型参数
			lstpara.add(servicetype);
			// 绑定摘要id参数
			lstpara.add(kbdataid);
			// 定义数据源
			Result isExitTable = null;
			try {
				// 执行SQL语句获取相应的数据源
				isExitTable = Database.executeQuery(sql, lstpara.toArray());
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 判断数据源不为null且含有数据
			if (isExitTable != null && isExitTable.getRowCount() > 0) {
				// 获取答案类型信息
				if (NewEquals.equals(answerType,"0")) {
					answerType = "普通文字";
				} else if (NewEquals.equals(answerType,"1")) {
					answerType = "答案模型";
				} else if (NewEquals.equals(answerType,"2")) {
					answerType = "触发动作";
				} else if (NewEquals.equals(answerType,"3")) {
					answerType = "普通文字+触发动作";
				} else if (NewEquals.equals(answerType,"4")) {
					answerType = "答案模型+触发动作";
				}
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将重复信息放入jsonObj的msg对象中
				jsonObj.put("msg", "当前商家下客户类型：" + customertype + "，渠道："
						+ channels[j] + "，答案类型:" + answerType + " 已存在");
				return jsonObj;
			}

		}

		// 渠道多个，插入kbcontent表的数据就有多条
		// 循环遍历渠道数组
		for (int j = 0; j < channels.length; j++) {
			// 插入kbansvaliddate
			// 获取kbansvaliddate的序列
			String kbansvaliddateid = String.valueOf(SeqDAO
					.GetNextVal("KBANSVALIDDATE_SEQ"));
			// 判断开始时间、结束时间不为空、null
			if ("".equals(starttime) || "".equals(endtime) || starttime == null
					|| endtime == null) {
				// 插入kbansvaliddate的SQL语句
				sql = "insert into kbansvaliddate values(?,?,null,null)";
				// 定义绑定参数集合
				lstpara = new ArrayList<String>();
				// 绑定kbansvaliddateid参数
				lstpara.add(kbansvaliddateid);
				// 绑定摘要id参数
				lstpara.add(kbdataid);
				// 将SQL语句放入SQL语句集合中
				listSql.add(sql);
				// 将对应的绑定参数集合放入集合中
				listParam.add(lstpara);
			} else {
				// 开始时间和结束时间不为空、null
				// // 获取开始时间
				// starttime = starttime.replace("T", " ");
				// // 获取结束时间
				// endtime = endtime.replace("T", " ");
				// 插入kbansvaliddate的SQL语句
				sql = "insert into kbansvaliddate values(?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),to_date(?,'yyyy-mm-dd hh24:mi:ss'))";
				// 定义绑定参数集合
				lstpara = new ArrayList<String>();
				// 绑定kbansvaliddateid参数
				lstpara.add(kbansvaliddateid);
				// 绑定摘要id参数
				lstpara.add(kbdataid);
				// 绑定开始时间参数
				lstpara.add(starttime);
				// 绑定结束时间参数
				lstpara.add(endtime);
				// 将SQL语句放入SQL语句集合中
				listSql.add(sql);
				// 将对应的绑定参数集合放入集合中
				listParam.add(lstpara);
			}

			// 插入kbanspak表
			// 获取kbanspak的序列
			String kbanspakid = String.valueOf(SeqDAO
					.GetNextVal("KBANSPAK_SEQ"));
			// 插入kbanspak的SQL语句
			sql = "insert into kbanspak values(?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定kbanspakid参数
			lstpara.add(kbanspakid);
			// 绑定kbansvaliddateid参数
			lstpara.add(kbansvaliddateid);
			// 绑定package参数
			lstpara.add("空号码包");
			// 绑定packagecode参数
			lstpara.add(null);
			// 绑定paktype参数
			lstpara.add("0");
			// 将SQL语句放入SQL语句集合中
			listSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			listParam.add(lstpara);

			// 插入kbansqryins表
			// 获取kbansqryins的序列
			String kbansqryinsid = String.valueOf(SeqDAO
					.GetNextVal("KBANSQRYINS_SEQ"));
			// 插入kbansqryins的SQL语句
			sql = "insert into kbansqryins values(?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定kbansqryinsid参数
			lstpara.add(kbansqryinsid);
			// 绑定kbanspakid参数
			lstpara.add(kbanspakid);
			// 绑定qryins参数
			lstpara.add("查询指令无关");
			// 将SQL语句放入SQL语句集合中
			listSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			listParam.add(lstpara);

			// 获取kbcontent的序列
			String kbcontentid = String.valueOf(SeqDAO
					.GetNextVal("SEQ_KBCONTENT_ID"));
			// 插入kbcontent的SQL语句
			sql = "insert into kbcontent values(?,?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定kbcontentid参数
			lstpara.add(kbcontentid);
			// 绑定kbansqryinsid参数
			lstpara.add(kbansqryinsid);
			// 绑定渠道参数
			lstpara.add(channels[j]);
			// 绑定answerType参数
			lstpara.add(answerType);
			// 绑定servicetype参数
			lstpara.add(servicetype);
			// 绑定customertype参数
			lstpara.add(customertype);
			// 将SQL语句放入SQL语句集合中
			listSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			listParam.add(lstpara);

			// 插入kbanswer表
			// 过去kbanswer的序列
			String kbanswerid = String.valueOf(SeqDAO
					.GetNextVal("KBANSWER_SEQ"));
			// 插入kbanswer的SQL语句
			sql = "insert into kbanswer(kbanswerid,kbcontentid,answercontent,servicehallstatus,city,customertype,brand) values(?,?,?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定kbanswerid参数
			lstpara.add(kbanswerid);
			// 绑定kbcontentid参数
			lstpara.add(kbcontentid);
			// 绑定答案参数
			lstpara.add(answer);
			// 绑定servicehallstatus参数
			lstpara.add("无关");
			// 绑定城市参数
			lstpara.add("上海");
			// 绑定customertype参数
			lstpara.add("所有客户");
			// 绑定品牌参数
			lstpara.add(brand);
			// 将SQL语句放入SQL语句集合中
			listSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			listParam.add(lstpara);

			// 生成操作日志记录
			listSql.add(MyUtil.LogSql());
			listParam.add(MyUtil.LogParam(brand, service, "增加答案", "上海", answer,
					"KBANSWER"));
		}
		// 执行SQL语句，绑定事务处理，并返回事务处理的结果
		int c = Database.executeNonQueryTransaction(listSql, listParam);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "失败!");
		}
		return jsonObj;
	}

	/**
	 * 删除答案知识
	 * 
	 * @param kbansvaliddateids参数json串
	 * @return 删除返回的json串
	 */
	public static Object deleteBath(String kbansvaliddateids) {
		
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User)sre;
		int c = CommonLibFaqDAO.bathDelete(kbansvaliddateids, user); 
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "删除成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将错误信息放入jsonObj的msg对象中
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}

	/**
	 * 页面的问题元素组合信息功能
	 * 
	 * @param param参数json串
	 * @return 返回json串
	 */
	public static Object QueryElement(JSONObject param) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获取摘要id
		String kbdataid = param.getString("kbdataid");
		// 获取channel
		String channel = param.getString("channel").split(",")[0];
		// 获取customertype
		String customertype = param.getString("customertype");
		// 获取starttime
		String starttime = param.getString("starttime");
		// 获取endtime
		String endtime = param.getString("endtime");
		// 获取brand
		String brand = param.getString("brand");
		// 定义多条SQL语句结果
		List<String> listSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> listParam = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 获取登录的商家组织应用
		String servicetype = MyClass.IndustryOrganizationApplication();
		// 判断servicetype为空串、空、null
		if (" ".equals(servicetype) || "".equals(servicetype)
				|| servicetype == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("msg", "登录信息已失效,请注销后重新登录");
			return jsonObj;
		}
		try {
			// 定义查询答案知识的SQL语句
			String sql = "select f.kbcontentid,g.kbanswerid from kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,"
					+ "kbcontent f,kbanswer g where b.kbdataid=c.kbdataid and c.kbansvaliddateid=d.kbansvaliddateid "
					+ "and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid and f.kbcontentid=g.kbcontentid "
					+ "and f.channel=? and f.answercategory=? and f.servicetype=? and f.customertype=? and b.kbdataid=? ";
			// 定义绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定渠道参数
			lstpara.add(channel);
			// 绑定答案类型参数
			lstpara.add("6");
			// 绑定业务类型参数
			lstpara.add(servicetype);
			// 绑定客户类型参数
			lstpara.add(customertype);
			// 绑定摘要id参数
			lstpara.add(kbdataid);
			// 执行SQL语句，获取相应的数据源
			Result isExitTable = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源不为null，且含有数据
			if (isExitTable != null && isExitTable.getRowCount() > 0) {
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将kbcontentid放入jsonObj的kbcontentid对象中
				jsonObj.put("kbcontentid", isExitTable.getRows()[0].get(
						"kbcontentid").toString());
				// 将kbanswerid放入jsonObj的kbanswerid对象中
				jsonObj.put("kbanswerid", isExitTable.getRows()[0].get(
						"kbanswerid").toString());
				return jsonObj;
			}

			// 插入kbansvaliddate
			// 获取kbansvaliddate的序列
			String kbansvaliddateid = String.valueOf(SeqDAO
					.GetNextVal("KBANSVALIDDATE_SEQ"));
			// 判断开始时间、结束时间不为空、null
			if ("".equals(starttime) || "".equals(endtime) || starttime == null
					|| endtime == null) {
				// 插入kbansvaliddate的SQL语句
				sql = "insert into kbansvaliddate values(?,?,null,null)";
				// 定义绑定参数集合
				lstpara = new ArrayList<String>();
				// 绑定kbansvaliddateid参数
				lstpara.add(kbansvaliddateid);
				// 绑定摘要id参数
				lstpara.add(kbdataid);
				// 将SQL语句放入SQL语句集合中
				listSql.add(sql);
				// 将对应的绑定参数集合放入集合中
				listParam.add(lstpara);
			} else {
				// 开始时间和结束时间不为空、null
				// 获取开始时间
				starttime = starttime.replace("T", " ");
				// 获取结束时间
				endtime = endtime.replace("T", " ");
				// 插入kbansvaliddate的SQL语句
				sql = "insert into kbansvaliddate values(?,?,to_date(?,'yyyy-mm-dd'),to_date(?,'yyyy-mm-dd'))";
				// 定义绑定参数集合
				lstpara = new ArrayList<String>();
				// 绑定kbansvaliddateid参数
				lstpara.add(kbansvaliddateid);
				// 绑定摘要id参数
				lstpara.add(kbdataid);
				// 绑定开始时间参数
				lstpara.add(starttime);
				// 绑定结束时间参数
				lstpara.add(endtime);
				// 将SQL语句放入SQL语句集合中
				listSql.add(sql);
				// 将对应的绑定参数集合放入集合中
				listParam.add(lstpara);
			}

			// 插入kbanspak表
			// 获取kbanspak的序列
			String kbanspakid = String.valueOf(SeqDAO
					.GetNextVal("KBANSPAK_SEQ"));
			// 插入kbanspak的SQL语句
			sql = "insert into kbanspak values(?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定kbanspakid参数
			lstpara.add(kbanspakid);
			// 绑定kbansvaliddateid参数
			lstpara.add(kbansvaliddateid);
			// 绑定package参数
			lstpara.add("空号码包");
			// 绑定packagecode参数
			lstpara.add(null);
			// 绑定paktype参数
			lstpara.add("0");
			// 将SQL语句放入SQL语句集合中
			listSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			listParam.add(lstpara);

			// 插入kbansqryins表
			// 获取kbansqryins的序列
			String kbansqryinsid = String.valueOf(SeqDAO
					.GetNextVal("KBANSQRYINS_SEQ"));
			// 插入kbansqryins的SQL语句
			sql = "insert into kbansqryins values(?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定kbansqryinsid参数
			lstpara.add(kbansqryinsid);
			// 绑定kbanspakid参数
			lstpara.add(kbanspakid);
			// 绑定qryins参数
			lstpara.add("查询指令无关");
			// 将SQL语句放入SQL语句集合中
			listSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			listParam.add(lstpara);

			// 渠道多个，插入kbcontent表的数据就有多条
			// 获取kbcontent的序列
			String kbcontentid = String.valueOf(SeqDAO
					.GetNextVal("SEQ_KBCONTENT_ID"));
			// 插入kbcontent的SQL语句
			sql = "insert into kbcontent values(?,?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定kbcontentid参数
			lstpara.add(kbcontentid);
			// 绑定kbansqryinsid参数
			lstpara.add(kbansqryinsid);
			// 绑定渠道参数
			lstpara.add(channel);
			// 绑定answerType参数
			lstpara.add("6");
			// 绑定servicetype参数
			lstpara.add(servicetype);
			// 绑定customertype参数
			lstpara.add(customertype);
			// 将SQL语句放入SQL语句集合中
			listSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			listParam.add(lstpara);

			// 插入kbanswer表
			// 过去kbanswer的序列
			String kbanswerid = String.valueOf(SeqDAO
					.GetNextVal("KBANSWER_SEQ"));
			// 插入kbanswer的SQL语句
			sql = "insert into kbanswer(kbanswerid,kbcontentid,answercontent,servicehallstatus,city,customertype,brand) values(?,?,?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<String>();
			// 绑定kbanswerid参数
			lstpara.add(kbanswerid);
			// 绑定kbcontentid参数
			lstpara.add(kbcontentid);
			// 绑定答案参数
			lstpara.add("");
			// 绑定servicehallstatus参数
			lstpara.add("无关");
			// 绑定城市参数
			lstpara.add("上海");
			// 绑定customertype参数
			lstpara.add("所有客户");
			// 绑定品牌参数
			lstpara.add(brand);
			// 将SQL语句放入SQL语句集合中
			listSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			listParam.add(lstpara);
			// 执行SQL语句，绑定事务处理，返回事务处理的结果
			int c = Database.executeNonQueryTransaction(listSql, listParam);
			// 判断事务处理的结果
			if (c > 0) {
				// 事务处理成功
				// 将true放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将kbcontentid放入jsonObj的kbcontentid对象中
				jsonObj.put("kbcontentid", kbcontentid);
				// 将kbanswerid放入jsonObj的kbanswerid对象中
				jsonObj.put("kbanswerid", kbanswerid);
			} else {
				// 事务处理失败
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将问题要素组合点击失败放入jsonObj的msg对象中
				jsonObj.put("msg", "问题要素组合点击失败");
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将问题要素组合点击失败放入jsonObj的msg对象中
			jsonObj.put("msg", "问题要素组合点击失败");
		}
		return jsonObj;
	}

	public static JSONObject compareTime(String starttime, String endtime) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断有效期是否符合规范
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();
		String systime = df.format(now) + " 00:00:00";

		int a = starttime.compareTo(systime);
		int b = starttime.compareTo(endtime);

		if (a < 0) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "当前有效期起始时间小于系统时间");

		} else if (b > 0) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "当前有效期起始时间大于截止时间");
		}

		return jsonObj;

	}
	
	/**
	 * 获取标准问地市集合
	 * @param kbdataid
	 * @return
	 */
	public static JSONArray getNormalQueryCities(String kbdataid) {
		JSONArray jsonArr = new JSONArray();
		
		Result rs = CommonLibFaqDAO.getKbdataCities(kbdataid);
		if(rs != null && rs.getRowCount() > 0){
			String cityStr = rs.getRows()[0].get("city") == null ? "" : rs.getRows()[0].get("city").toString();
			if(!"".equals(cityStr.trim())){
				String[] cityCodes = cityStr.split(",");
				for(String cityCode : cityCodes){
					String cityName = QuerymanageDAO.cityCodeToCityName.get(cityCode);
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("id", cityCode);
					jsonObj.put("text", cityName);
					jsonArr.add(jsonObj);
				}
			}
		}
		
		return jsonArr;
	}


	public static Object getAttrName(String serviceid) {
		Result rs = CommonLibServiceAttrDao.getServiceAttributions(serviceid);
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String cm = rs.getRows()[i].get("columnnum").toString();
				String attrName = rs.getRows()[i].get("name").toString();
				Object attrKeyword = rs.getRows()[i].get("semanticskeyword");
				jsonObj.put("id", cm);
				jsonObj.put("text", attrKeyword == null || "".equals(attrKeyword.toString()) ? attrName : attrKeyword.toString());
				array.add(jsonObj);
			}
		}
		return array;
	}
	
	/**
	 * 获取指定地市范围内的所有Robot ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object getRobotID(String cityCode) {
		if (StringUtils.isBlank(cityCode) ) {
			cityCode = "全国";
		}
		
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = null;
		for (Map.Entry entry : REALROBOTID_DIC.entrySet()) {
			String id = (String) entry.getKey();
			RealRobotInfo robot = (RealRobotInfo) entry.getValue();
			if (robot != null 
					&& StringUtils.isNotBlank(robot.getCityCode())
					&& StringUtils.isNotBlank(cityCode)
					&& ( cityCode.contains("全国") || cityCode.contains(robot.getCityCode()) )
					) {
				jsonObj = new JSONObject();
				jsonObj.put("id", id);
				jsonObj.put("text", robot == null ? "" : robot.getName());
				array.add(jsonObj);
			}
		}
		return array;
	}
	
	public static Object getResConfig() {
		// 定义json串的格式
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		// 获得用户登录信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		Result result = CommonLibMetafieldmappingDAO.getConfigValue("场景其他形式答案配置", serviceType);
		if (result != null && result.getRowCount() > 0){
			for (int i = 0 ; i < result.getRowCount() ; i++){
				JSONObject jsonObjPart = new JSONObject();
				String info = result.getRows()[i].get("name").toString();
				String key = "";
				String value = "";
				// 获取配置
				if (info.contains("::")){
					String infos[] = info.split("::");
					key = infos[0];
					value = infos[1];
				}else{
					key = info;
					value = "自定义";
				}
				// 有对应词类
				JSONArray jsonArrWord = new JSONArray();
				if (!"自定义".equals(value)){
					Result wordResult = CommonLibWordDAO.select("", false, true, "", value, "基础");
					if (wordResult != null && wordResult.getRowCount() > 0){
						for (int k = 0 ; k < wordResult.getRowCount() ; k++){
							String word = wordResult.getRows()[k].get("word").toString();
							JSONObject jsonObjWord = new JSONObject();
							jsonObjWord.put("id", word);
							jsonObjWord.put("text", word);
							jsonArrWord.add(jsonObjWord);
						}
					}
				}
				jsonObjPart.put("weight", i+1);
				jsonObjPart.put("key", key);
				jsonObjPart.put("value", value);
				jsonObjPart.put("words", jsonArrWord);
				jsonArr.add(jsonObjPart);
			}
		}
		jsonObj.put("success", true);
		jsonObj.put("rows", jsonArr);
		return jsonObj;
	}
	
	/**
	 * 获取Robot 名称
	 * @param robotid
	 * @return
	 */
	public static String getRobotName(String robotid) {
		if (REALROBOTID_DIC.containsKey(robotid)){
			return REALROBOTID_DIC.get(robotid).getName();
		}
		return null;
	}

}
