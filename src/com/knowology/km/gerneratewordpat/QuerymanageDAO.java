package com.knowology.km.gerneratewordpat;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.ImportNormalqueryBean;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibInteractiveSceneDAO;
import com.knowology.bll.CommonLibKbDataDAO;
import com.knowology.bll.CommonLibKbdataAttrDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.bll.CommonLibQuestionUploadDao;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.dal.Database;
import com.knowology.km.NLPAppWS.AnalyzeEnterDelegate;
import com.knowology.km.NLPCallerWS.NLPCaller4WSDelegate;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.entity.CheckInforef;
import com.knowology.km.util.Check;
import com.knowology.km.util.CheckInputFromAutoWordpat;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.GlobalValues;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.ReadExcel;
import com.knowology.km.util.getServiceClient;
import com.str.NewEquals;

public class QuerymanageDAO {

	public static Logger logger = Logger.getLogger("querymanage");

	public static String regressTestPath = System.getProperty("os.name")
			.toLowerCase().startsWith("win") ? Database
			.getCommmonLibJDBCValues("winDir")
			+ File.separator + "qatraining" + File.separator + "regresstest"
			: Database.getCommmonLibJDBCValues("linDir") + File.separator
					+ "qatraining" + File.separator + "regresstest";

	public static String FILE_PATH_EXPORT = regressTestPath + File.separator + "export" + File.separator;

	/**
	 * 定义全局 cityCodeToCityName字典
	 */
	public static Map<String, String> cityCodeToCityName = new HashMap<String, String>();

	/**
	 * 定义全局 cityNameToCityCode 字典
	 */
	public static Map<String, String> cityNameToCityCode = new HashMap<String, String>();
	
	/**
	 * 回复类型
	 */
	public static List<String> responseTypes = new ArrayList<String>();
	
	/**
	 * 交互类型
	 */
	public static List<String> interactTypes = new ArrayList<String>();
	
	public static JSONArray SERVICE_TREE_MENU = new JSONArray();
	
	/**
	 *创建字典
	 */
	static {
		Result r = CommonLibMetafieldmappingDAO.getConfigMinValue("地市编码配置");
		if (r != null && r.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < r.getRowCount(); i++) {
				String key = r.getRows()[i].get("k") == null ? ""
						: r.getRows()[i].get("k").toString();
				String value = r.getRows()[i].get("name") == null ? "" : r
						.getRows()[i].get("name").toString();
				cityCodeToCityName.put(value, key);
				cityNameToCityCode.put(key, value);
			}
		}
	}
	
	/**
	 * 构造业务树
	 * 
	 * @param serviceid
	 * @return Object
	 */
	public static Object createServiceTree(String serviceid,String citySelect) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		String brand = serviceType.split("->")[1] + "问题库";
		// String brand ="安徽电信";
		// 定义返回的json串
		JSONArray array = new JSONArray();
		Result rs = CommonLibQueryManageDAO.createServiceTree(serviceid, brand,citySelect);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String sid = rs.getRows()[i].get("serviceid").toString();
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", sid);
				jsonObj.put("text", rs.getRows()[i].get("service").toString());
				if (CommonLibQueryManageDAO.hasChild(sid) == 0) {// 如果没有子业务
					// jsonObj.put("iconCls", "icon-servicehit");
					jsonObj.put("leaf", true);
				} else {
					// jsonObj.put("expanded","true");
					jsonObj.put("cls", "folder");
					jsonObj.put("leaf", false);
					jsonObj.put("state", "closed");
				}

				array.add(jsonObj);
			}
		}
		return array;
	}
	
	/**
	 * 构建业务树
	 * @return
	 */
	public static Object createServiceTree(String citySelect) {
		JSONArray jsonArray = new JSONArray();
		
		User user = (User) GetSession.getSessionByKey("accessUser");
		if(user == null){
			JSONObject rst = new JSONObject();
			rst.put("success", false);
			rst.put("message", "重新登录");
			return rst;
		}
		String serviceType = user.getIndustryOrganizationApplication();
		String brand = serviceType.split("->")[1] + "问题库";
		// 根业务
		String[] rootService = {brand};
		String rootserviceId = "";
		Result rs = CommonLibQueryManageDAO.createServiceTree(rootService, brand,citySelect);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String sid = rs.getRows()[i].get("serviceid").toString();
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", sid);
				jsonObj.put("text", rs.getRows()[i].get("service").toString());
				jsonObj.put("parentid", rs.getRows()[i].get("parentid").toString());
				jsonObj.put("state", "closed");
//				if (CommonLibQueryManageDAO.hasChild(sid) == 0) {// 如果没有子业务
//					// jsonObj.put("iconCls", "icon-servicehit");
//					jsonObj.put("leaf", true);
//				} else {
//					// jsonObj.put("expanded","true");
//					jsonObj.put("cls", "folder");
//					jsonObj.put("leaf", false);
//					jsonObj.put("state", "closed");
//				}
				jsonArray.add(jsonObj);
			}
		}
		
//		Object sre = GetSession.getSessionByKey("accessUser");
//		User user = (User) sre;
//		// 获取行业
//		String serviceType = user.getIndustryOrganizationApplication();
//		String brand = serviceType.split("->")[1] + "问题库";
		rs = CommonLibServiceDAO.getServiceIDByServiceAndBrand(rootService[0], brand);
		if(rs != null){
			rootserviceId = rs.getRows()[0].get("serviceid").toString();
		}
		
		JSONArray menuTree = new JSONArray();
		JSONArray serviceTree = treeMenuList(jsonArray, rootserviceId);
		for(Object object : jsonArray){
			JSONObject node = (JSONObject) JSONObject.toJSON(object);
			if(NewEquals.equals(node.getString("id"),rootserviceId)){
				node.put("children", serviceTree);
				menuTree.add(node);
			}
		}
		return menuTree;
	}

	/**
	 * 刷新业务树
	 */
	public static void refreshServiceTree(String citySelect){
		SERVICE_TREE_MENU = (JSONArray) createServiceTree(citySelect);
	}
	
	/**
	 * 构造业务菜单树数据
	 * @param menuList
	 * @param parentId
	 * @return
	 */
	private static JSONArray treeMenuList(JSONArray menuList, String parentId){
		JSONArray childMenu = new JSONArray();
		for (Object object : menuList) {
			JSONObject jsonMenu = (JSONObject) JSONObject.toJSON(object);
			String serviceid = jsonMenu.getString("id");
			String pid = jsonMenu.getString("parentid");
			if (NewEquals.equals(parentId,pid)) {
				JSONArray c_node = treeMenuList(menuList, serviceid);
				if(c_node == null || c_node.size() == 0){
					jsonMenu.put("state", "");
				} else{
					jsonMenu.put("children", c_node);
				}
				
				childMenu.add(jsonMenu);
			}
		}
		return childMenu;
	}
	
	/**
	 * 业务查询
	 * @param serviceStr
	 * @return
	 */
	public static Object searchService(String serviceStr){
		JSONArray jsonArray = new JSONArray();

		User user = (User) GetSession.getSessionByKey("accessUser");
		if(user == null){
			JSONObject rst = new JSONObject();
			rst.put("success", false);
			rst.put("message", "重新登录");
			return rst;
		}
		String serviceType = user.getIndustryOrganizationApplication();
		String brand = serviceType.split("->")[1] + "问题库";

		// 根业务
		List<String> rootServiceList = new ArrayList<String>();
		rootServiceList.add("电信垃圾问题库");
		rootServiceList.add(brand);
		Result rs = CommonLibQueryManageDAO.searchService(rootServiceList, serviceStr,null);
		if (rs != null && rs.getRowCount() > 0) {
			String serviceid = null;
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				serviceid = rs.getRows()[i].get("serviceid").toString();
				jsonObj.put("id", serviceid);
				jsonObj.put("text", rs.getRows()[i].get("service").toString());
				jsonObj.put("textpath", rs.getRows()[i].get("name_path").toString());
				jsonObj.put("idpath", rs.getRows()[i].get("serviceid_path").toString());
				jsonObj.put("leaf", CommonLibQueryManageDAO.hasChild(serviceid) == 0);
				jsonArray.add(jsonObj);
			}
		}
		
		return jsonArray;
	}
	
	/**
	 *@description 根据用户信息创建地市树
	 *@param flag
	 *@return
	 *@returnType Object
	 */
	public static Object createCityTreeByLoginInfo(String flag) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String cityCode = "";
		String cityName = "";
		List<String> cityList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
				.resourseAccess(user.getUserID(), "querymanage", "S");
		// 该操作类型用户能够操作的资源
		cityList = resourseMap.get("地市");
		if (cityList != null) {
			cityCode = cityList.get(0);
			// if(cityCode.endsWith("0000")){//省级用户
			//				
			// }
		}
		Map<String, String> map = new HashMap<String, String>();
		// String cityname[] = flag.split(",");
		// for (int m = 0; m < cityname.length; m++) {
		// map.put(cityname[m], "");
		// }
		JSONArray jsonAr = new JSONArray();

		Result rs = null;
		rs = CommonLibQuestionUploadDao.selProvince(cityCode);
		JSONObject innerJsonObj = null;
		if (null != rs && rs.getRowCount() > 0) {
			JSONObject allJsonObj = new JSONObject();
			if (!"edit".equals(flag)) {
				allJsonObj.put("id", "全国");
				allJsonObj.put("text", "全国");
				if (map.containsKey("全国")) {
					allJsonObj.put("checked", true);
				}
				jsonAr.add(allJsonObj);
			}

			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				String id = rs.getRows()[i].get("id").toString();
				String province = rs.getRows()[i].get("province").toString();
				Result innerRs = null;
				Result innerRs2 = null;
				if (province.indexOf("市") < 0) {
					innerRs = CommonLibQuestionUploadDao.getCityByProvince(id);
				}
				// else {
				// innerRs2 = CommonLibQuestionUploadDao.getzCity(id);
				// }
				if (map.containsKey(province)) {
					jsonObj.put("checked", true);
				}
				JSONArray jsonArr = new JSONArray();
				if (null != innerRs && innerRs.getRowCount() > 0) {
					for (int j = 0; j < innerRs.getRowCount(); j++) {

						String cityId = innerRs.getRows()[j].get("id")
								.toString();
						// Result sinnerRs =
						// CommonLibQuestionUploadDao.getScity(cityId);
						// JSONArray sJsonArr = new JSONArray();
						innerJsonObj = new JSONObject();
						// if (sinnerRs != null && sinnerRs.getRowCount() > 0){
						// for (int k = 0 ; k < sinnerRs.getRowCount() ; k++){
						// JSONObject sInnerJsonObj = new JSONObject();
						// sInnerJsonObj.put("id",
						// sinnerRs.getRows()[k].get("id"));
						// sInnerJsonObj.put("text",
						// sinnerRs.getRows()[k].get("city"));
						// if
						// (map.containsKey(sinnerRs.getRows()[k].get("city"))){
						// sInnerJsonObj.put("checked", true);
						// }
						// sJsonArr.add(sInnerJsonObj);
						// }
						// innerJsonObj.put("state", "closed");
						// }
						innerJsonObj.put("id", innerRs.getRows()[j].get("id"));
						innerJsonObj.put("text", innerRs.getRows()[j]
								.get("city"));
						// innerJsonObj.put("children", sJsonArr);
						// if (local.equals(innerRs.getRows()[j].get("city"))){
						// innerJsonObj.put("checked", true);
						// }

						if (map.containsKey(innerRs.getRows()[j].get("city"))) {
							innerJsonObj.put("checked", true);
						}
						jsonArr.add(innerJsonObj);
					}
					jsonObj.put("state", "closed");
				} else if (null != innerRs2 && innerRs2.getRowCount() > 0) {
					for (int j = 0; j < innerRs2.getRowCount(); j++) {

						JSONArray sJsonArr = new JSONArray();

						innerJsonObj = new JSONObject();
						innerJsonObj.put("id", innerRs2.getRows()[j].get("id"));
						innerJsonObj.put("text", innerRs2.getRows()[j]
								.get("city"));
						innerJsonObj.put("children", sJsonArr);
						if (map.containsKey(innerRs2.getRows()[j].get("city"))) {
							innerJsonObj.put("checked", true);
						}
						jsonArr.add(innerJsonObj);
					}
					jsonObj.put("state", "closed");
				}
				if (cityCode.endsWith("0000")) {
					jsonObj.put("id", rs.getRows()[i].get("id"));
					jsonObj.put("text", rs.getRows()[i].get("province"));
					jsonObj.put("children", jsonArr);
					jsonAr.add(jsonObj);
				} else {
					jsonAr.add(innerJsonObj);
				}

			}
		}
		return jsonAr;
	}

	/**
	 * 获得标准问题下拉框数据
	 * 
	 * @param serviceid
	 * @param flag
	 * @return
	 */
	public static Object createAbstractCombobox(String serviceid, String flag) {
		if ("".equals(serviceid)) {
			return "";
		}
		Result rs = CommonLibKbDataDAO.getAbstractInfoByServiceid(serviceid);
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (!"".equals(flag) && flag != null) {
			jsonObj.put("id", "全部");
			jsonObj.put("text", "全部");
			array.add(jsonObj);
		}
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String abs = rs.getRows()[i].get("abstract").toString();
				abs = abs.split(">")[1];
				String kbdataid = rs.getRows()[i].get("kbdataid").toString();
				jsonObj.put("id", kbdataid);
				jsonObj.put("text", abs);
				array.add(jsonObj);
			}
		}
		return array;
	}

	/**
	 * 获得回复类型下拉框数据
	 * 
	 * @param flag
	 * @return
	 */
	public static Object createResponseTypeCombobox(String flag) {
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库参数配置",
				"回复类型");
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (!"".equals(flag) && flag != null) {
			jsonObj.put("id", "全部");
			jsonObj.put("text", "全部");
			array.add(jsonObj);
		}
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
	 * 获得交互类型下拉框数据
	 * 
	 * @param flag
	 * @return
	 */
	public static Object createInteractTypeCombobox(String flag) {
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库参数配置",
				"交互类型");
		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (!"".equals(flag) && flag != null) {
			jsonObj.put("id", "全部");
			jsonObj.put("text", "全部");
			array.add(jsonObj);
		}
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
	 *@description 获取问题列表信息
	 *@param serviceid
	 *@param normalQuery
	 *@param customerQuery
	 *@param cityCode
	 *@param responseType
	 *@param interactType
	 *@param page
	 *@param rows
	 *@return
	 *@returnType Object
	 */
	public static Object selectQuery(String serviceid, String normalQuery,
			String customerQuery, String cityCode, String responseType,
			String interactType, int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int count = CommonLibQueryManageDAO.getQueryCount(serviceid,
				normalQuery, customerQuery, cityCode, responseType,
				interactType);
		if (count > 0) {
			jsonObj.put("total", count);
			Result rs = CommonLibQueryManageDAO.selectQuery(serviceid,
					normalQuery, customerQuery, cityCode, responseType,
					interactType, page, rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject obj = new JSONObject();
					obj.put("queryid", rs.getRows()[i].get("id"));
					obj.put("service", rs.getRows()[i].get("service"));
					obj.put("brand", rs.getRows()[i].get("brand"));
					obj.put("kbdataid", rs.getRows()[i].get("kbdataid"));
					String abs = rs.getRows()[i].get("abstract").toString();
					obj.put("normalquery", abs.split(">")[1]);
					obj.put("abs", abs);
					obj.put("topic", rs.getRows()[i].get("topic"));
					obj.put("abscity", rs.getRows()[i].get("abscity"));
					obj
							.put("responsetype", rs.getRows()[i]
									.get("responsetype"));
					obj
							.put("interacttype", rs.getRows()[i]
									.get("interacttype"));
					obj.put("customerquery", rs.getRows()[i].get("query"));
					String city = rs.getRows()[i].get("city") != null ? rs
							.getRows()[i].get("city").toString() : "";
					String cityName = "";
					if (!"".equals(city)) {
						String cityArray[] = city.split(",");
						List<String> cityNameList = new ArrayList<String>();
						for (int k = 0; k < cityArray.length; k++) {
							String code = cityCodeToCityName.get(cityArray[k]);
							cityNameList.add(code);
						}
						if (cityNameList.size() > 0) {
							cityName = StringUtils.join(cityNameList.toArray(),
									",");
						}
					}

					obj
							.put("wordpatcount", rs.getRows()[i]
									.get("wordpatcount"));
					obj.put("relatequerycount", rs.getRows()[i]
							.get("relatequerycount"));
					obj.put("answercount", rs.getRows()[i].get("answercount"));
					obj.put("cityname", cityName);
					obj.put("citycode", city);

					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			jsonObj.put("rows", jsonArr);
		} else {
			jsonArr.clear();
			jsonObj.put("total", 0);
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	
	
//	/**
//	 *@description 获取客户问题列表信息
//	 *@param serviceid
//	 *@param kbdataid
//	 *@param normalQuery
//	 *@param customerQuery
//	 *@param cityCode
//	 *@param page
//	 *@param rows
//	 *@return
//	 *@returnType Object
//	 */
//	public static Object selectCustomerQuery(String serviceid, String kbdataid,String normalQuery,
//			String customerQuery, String cityCode,  int page, int rows) {
//		// 定义返回的json串
//		JSONObject jsonObj = new JSONObject();
//		JSONArray jsonArr = new JSONArray();
//		int count = CommonLibQueryManageDAO.getCustomerQueryCount(serviceid,kbdataid, customerQuery, cityCode);
//		if (count > 0) {
//			jsonObj.put("total", count);
//			Result rs = CommonLibQueryManageDAO.selectCustomerQuery(serviceid,kbdataid, customerQuery, cityCode, page, rows);
//			// 判断数据源不为null且含有数据
//			if (rs != null && rs.getRowCount() > 0) {
//				// 循环遍历数据源
//				for (int i = 0; i < rs.getRowCount(); i++) {
//					JSONObject obj = new JSONObject();
//					obj.put("queryid", rs.getRows()[i].get("id"));
//					obj.put("service", rs.getRows()[i].get("service"));
//					obj.put("brand", rs.getRows()[i].get("brand"));
//					obj.put("kbdataid", rs.getRows()[i].get("kbdataid"));
//					String abs = rs.getRows()[i].get("abstract").toString();
//					obj.put("normalquery", abs.split(">")[1]);
//					obj.put("abs", abs);
//					obj.put("topic", rs.getRows()[i].get("topic"));
//					obj.put("abscity", rs.getRows()[i].get("abscity"));
//					obj
//							.put("responsetype", rs.getRows()[i]
//									.get("responsetype"));
//					obj
//							.put("interacttype", rs.getRows()[i]
//									.get("interacttype"));
//					obj.put("customerquery", rs.getRows()[i].get("query"));
//					String city = rs.getRows()[i].get("city") != null ? rs
//							.getRows()[i].get("city").toString() : "";
//					String cityName = "";
//					if (!"".equals(city)) {
//						String cityArray[] = city.split(",");
//						List<String> cityNameList = new ArrayList<String>();
//						for (int k = 0; k < cityArray.length; k++) {
//							String code = cityCodeToCityName.get(cityArray[k]);
//							cityNameList.add(code);
//						}
//						if (cityNameList.size() > 0) {
//							cityName = StringUtils.join(cityNameList.toArray(),
//									",");
//						}
//					}
//
//					obj
//							.put("wordpatcount", rs.getRows()[i]
//									.get("wordpatcount"));
//					obj.put("relatequerycount", rs.getRows()[i]
//							.get("relatequerycount"));
//					obj.put("answercount", rs.getRows()[i].get("answercount"));
//					obj.put("cityname", cityName);
//					obj.put("citycode", city);
//
//					// 将生成的对象放入jsonArr数组中
//					jsonArr.add(obj);
//				}
//			}
//			jsonObj.put("rows", jsonArr);
//		} else {
//			jsonArr.clear();
//			jsonObj.put("total", 0);
//			jsonObj.put("rows", jsonArr);
//		}
//		return jsonObj;
//	}
	
	
	/**
	 *@description 获取标准问题列表信息
	 *@param serviceid
	 *@param normalQuery
	 *@param customerQuery
	 *@param cityCode
	 *@param responseType
	 *@param interactType
	 *@param page
	 *@param rows
	 *@return
	 *@returnType Object
	 */
	public static Object selectNormalQuery(String serviceid, String normalQuery, String responseType,
			String interactType, int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int count = CommonLibQueryManageDAO.getNormalQueryCount(serviceid,
				normalQuery,  responseType,
				interactType);
		if (count > 0) {
			jsonObj.put("total", count);
			Result rs = CommonLibQueryManageDAO.selectNormalQuery(serviceid,
					normalQuery, responseType,
					interactType, page, rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject obj = new JSONObject();
					obj.put("queryid", rs.getRows()[i].get("id"));
					obj.put("service", rs.getRows()[i].get("service"));
					obj.put("brand", rs.getRows()[i].get("brand"));
					obj.put("kbdataid", rs.getRows()[i].get("kbdataid"));
					String abs = rs.getRows()[i].get("abstract").toString();
					obj.put("normalquery", abs.split(">")[1]);
					obj.put("abs", abs);
					obj.put("topic", rs.getRows()[i].get("topic"));
					obj.put("abscity", rs.getRows()[i].get("abscity"));
					obj
							.put("responsetype", rs.getRows()[i]
									.get("responsetype"));
					obj
							.put("interacttype", rs.getRows()[i]
									.get("interacttype"));
					obj.put("customerquery", rs.getRows()[i].get("query"));
					String city = rs.getRows()[i].get("city") != null ? rs
							.getRows()[i].get("city").toString() : "";
					String cityName = "";
					if (!"".equals(city)) {
						String cityArray[] = city.split(",");
						List<String> cityNameList = new ArrayList<String>();
						for (int k = 0; k < cityArray.length; k++) {
							String code = cityCodeToCityName.get(cityArray[k]);
							cityNameList.add(code);
						}
						if (cityNameList.size() > 0) {
							cityName = StringUtils.join(cityNameList.toArray(),
									",");
						}
					}

					obj
							.put("wordpatcount", rs.getRows()[i]
									.get("wordpatcount"));
					obj.put("relatequerycount", rs.getRows()[i]
							.get("relatequerycount"));
					obj.put("answercount", rs.getRows()[i].get("answercount"));
					obj.put("cityname", cityName);
					obj.put("citycode", city);

					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			jsonObj.put("rows", jsonArr);
		} else {
			jsonArr.clear();
			jsonObj.put("total", 0);
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}
	

	
	/**
	 *@description 全量生成词模
	 *@param serviceid
	 *@param request
	 *@return
	 *@returnType Object
	 */
/*	
	public static void produceAllWordpat(String serviceid, String userid, String servicetype, String path) {
		logger.info("开始生成词模=====>");
		System.out.println("开始生成词模=====>");
		
		
		List<String> combitionArray = getAllQuery(serviceid);
		if(combitionArray == null || combitionArray.size() == 0){
			System.err.println("该serivceid[" + serviceid+"]指定错误");
			return ;
		}

		logger.info("问题获得完成，共"+combitionArray.size()+"个");
		System.out.println("问题获得完成，共"+combitionArray.size()+"个");
		
		int num = 0;
		for (int i = 0; i < combitionArray.size(); i++) {
			String queryArray[] = combitionArray.get(i).split("@#@");
			String queryCityCode = queryArray[0];
			if ("".equals(queryCityCode) || queryCityCode == null) {
				queryCityCode = "全国";
			} else {
				queryCityCode = queryCityCode.replace(",", "|");
			}
			// 获取高级分析的接口串中的serviceInfo
			String serviceInfo = MyUtil.getServiceInfo(servicetype, "问题生成词模", "",
					false,queryCityCode);
			String query = queryArray[1];
			String kbdataid = queryArray[2];
			// 获取高级分析的串
			String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模",
					queryArray[1], servicetype, serviceInfo);
			// 调用生成词模的接口生成词模,可能是多个，以@_@分隔
			String wordpat = getWordpat(queryObject);
			if (wordpat != null && !"".equals(wordpat)) {
				// 判断词模是否含有@_@
				if (wordpat.contains("@_@")) {
					// 有的话，按照@_@进行拆分,并只取第一个
					wordpat = wordpat.split("@_@")[0];
				}
				// 获取词模中@前面的词模题，在加上@2#编者="问题库"&来源="(当前问题)"
				wordpat = wordpat.split("@")[0] + "@2#编者=\"问题库\"&来源=\""
						+ query.replace("&", "\\and") + "\"";

				// 校验自动生成的词模是否符合规范
				if (CheckWordpat(wordpat, path)) {
					List<String> tempList = new ArrayList<String>();
					tempList.add(wordpat);
					tempList.add(queryCityCode);
					tempList.add(query);
					tempList.add(kbdataid);
//					list.add(tempList);

					int count = CommonLibQueryManageDAO.insertWordpat2(tempList, servicetype, userid);
					if(count > 0){
						num++;
						logger.info(MessageFormat.format("生成成功【kbdataid:{0}, query:{1}, wordpat:{2}】", kbdataid,query,wordpat));
						String now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
						System.out.println(MessageFormat.format(now + " 生成成功【kbdataid:{0}, query:{1}, wordpat:{2}】", kbdataid,query,wordpat));
					}else{
						logger.warn(MessageFormat.format("生成失败【kbdataid:{0}, query:{1}, wordpat:{2}】", kbdataid,query,wordpat));
						String now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
						System.out.println(MessageFormat.format(now + " 生成失败【kbdataid:{0}, query:{1}, wordpat:{2}】", kbdataid,query,wordpat));
					}
					
				} else{
					String now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
					System.out.println(MessageFormat.format(now + " 词模不符合规范【kbdataid:{0}, query:{1}, wordpat:{2}】", kbdataid,query,wordpat));
				}
			}else{
				String now = DateFormatUtils.format(new Date(), "yyyy-dd-MM HH:mm:ss:SSS");
				System.out.println(MessageFormat.format(now + " 接口返回词模为空【kbdataid:{0}, query:{1}, wordpat:{2}】", kbdataid,query,wordpat));
			}
		}
		
		logger.info(MessageFormat.format("====》共{0}个客户问题，生成{1}个词模", combitionArray.size(), num));
		System.out.println(MessageFormat.format("====》共{0}个客户问题，生成{1}个词模", combitionArray.size(), num));
	}
*/
	public static boolean CheckWordpat(String wordpat, String path) {
		// 获取Web服务器上指定的虚拟路径对应的物理文件路径
//		String path = request.getSession().getServletContext().getRealPath("/");
		boolean checkflag = true;
		// 词模检查结果字符串
		String checkInfo = "";
		CheckInforef curcheckInfo = new CheckInforef();
		try {
			// 调用词模检查函数
			if (!CheckInputFromAutoWordpat.CheckGrammer(path, wordpat, 0,
					curcheckInfo))
				// 词模有误
				checkflag = false;
		} catch (Exception ex) {
			// 检查过程中出现异常，则报错
			checkflag = false;
			curcheckInfo.curcheckInfo = "模板语法有误！";
		}
		if (!"".equals(curcheckInfo.curcheckInfo)
				&& (!"没有语法错误".equals(curcheckInfo.curcheckInfo))) {
			checkInfo += curcheckInfo.curcheckInfo + "<br>";
		}
		// add by zhao lipeng. 20170210 START
		if(!checkflag){
			logger.info(MessageFormat.format("词模检查失败。词模={0}，原因={1}", wordpat, curcheckInfo.curcheckInfo));
		}
		// add by zhao lipeng. 20170210 END
		return checkflag;
	}
	
	private static String getRootService(String servicetype){
		if("电信行业->电信集团->4G业务客服应用".equals(servicetype)){
			return "电信集团问题库";
		}else if ("电信行业->电信集团->指令系统应用".equals(servicetype)){
			return "电信集团指令问题库";
		}
		return "";
	}
	
	/**
	 *@description 获得业务下所有问题
	 *@param service
	 *@return
	 *@returnType List<String>
	 */
	public static List<String> getAllQuery(String[] servicetypes) {
		List<String> rtnlist = new ArrayList<String>();
		
		for(String servicetype : servicetypes){
			servicetype = servicetype.trim();
			String service = getRootService(servicetype);
			List<String> list = new ArrayList<String>();
			Result rs = CommonLibQueryManageDAO.getQuery3(service);
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					String query = rs.getRows()[i].get("query") != null ? rs
							.getRows()[i].get("query").toString() : "";
					if ("".equals(query)) {
						continue;
					}
					String city = rs.getRows()[i].get("city") != null ? rs
							.getRows()[i].get("city").toString() : "";
					String kbdataid = rs.getRows()[i].get("kbdataid").toString();
//					String namepath = rs.getRows()[i].get("namepath").toString();
					String province = "";
					if(!"".equals(city)){
						String tmp ="";
						if("全国".equals(city)){
							province = cityCodeToCityName.get("全国");
						}else{
							tmp = city.split(",")[0];
							tmp = tmp.substring(0, 2)+"0000";
							province = cityCodeToCityName.get(tmp);
						}
					}
					if(StringUtils.isEmpty(province)){
						province  = "默认";
					}
					
					String content = city + "@#@" + query + "@#@" + kbdataid + "@#@" + province + "@#@" + servicetype;
					list.add(content);
				}
			}
			rtnlist.addAll(list);
		}
		
		return rtnlist;
	}
	
	public static void main(String[] args) {
		System.out.println(StringUtils.substringBetween("电信集团问题库->江苏->翼支付->1->adssd", "->"));
		System.out.println(StringUtils.substringsBetween("电信集团问题库->北京", "->", "->"));
		System.out.println(StringUtils.substringsBetween("电信集团问题库", "->", "->"));
	}

	/**
	 *@description 生成词模
	 *@param combition
	 *@param request
	 *@return
	 *@returnType Object
	 */
/*
	public static Object produceWordpat(String combition,
			HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		List<List<String>> list = new ArrayList<List<String>>();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		// 获取高级分析的接口串中的serviceInfo
		String serviceInfo = MyUtil.getServiceInfo(servicetype, "问题生成词模", "",
				false);
		String combitionArray[] = combition.split("@@");
		for (int i = 0; i < combitionArray.length; i++) {
			String queryArray[] = combitionArray[i].split("@#@");
			String queryCityCode = queryArray[0];
			if ("".equals(queryCityCode) || queryCityCode == null) {
				queryCityCode = "全国";
			} else {
				queryCityCode = queryCityCode.replace(",", "|");
			}
			String query = queryArray[1];
			String kbdataid = queryArray[2];
			// 获取高级分析的串
			String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模",
					queryArray[1], servicetype, serviceInfo);

			logger.info("问题库自学习词模接口的输入串：" + queryObject);
			// 调用生成词模的接口生成词模,可能是多个，以@_@分隔
			String wordpat = getWordpat(queryObject);
			logger.info("问题库自学习词模：" + wordpat);
			if (wordpat != null && !"".equals(wordpat)) {
				// 判断词模是否含有@_@
				if (wordpat.contains("@_@")) {
					// 有的话，按照@_@进行拆分,并只取第一个
					wordpat = wordpat.split("@_@")[0];
				}
				// 获取词模中@前面的词模题，在加上@2#编者="问题库"&来源="(当前问题)"
				wordpat = wordpat.split("@")[0] + "@2#编者=\"问题库\"&来源=\""
						+ query.replace("&", "\\and") + "\"";

				// 校验自动生成的词模是否符合规范
				if (Check.CheckWordpat(wordpat, request)) {
					List<String> tempList = new ArrayList<String>();
					tempList.add(wordpat);
					tempList.add(queryCityCode);
					tempList.add(query);
					tempList.add(kbdataid);
					list.add(tempList);

				}
			}
		}
		// 插入问题库自动学习词模
		int count = -1;
		if (list.size() > 0) {
			count = CommonLibQueryManageDAO.insertWordpat(list, servicetype,userid);
			if (count > 0) {
				jsonObj.put("success", true);
				jsonObj.put("msg", "生成成功!");
			} else {
				jsonObj.put("success", false);
				jsonObj.put("msg", "生成失败!");
			}
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "生成失败!!");
		}

		return jsonObj;

	}
*/
	/**
	 * 生成词模
	 * 
	 * @param 参数调用接口的入参
	 * @return 词模
	 */
	public static String getWordpat(String queryObject, String nlpServerIP) {
		// 获取高级分析的客户端
//		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
//				.NLPCaller4WSClient();
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient.NLPCaller4WSClient(nlpServerIP);
		
		// 判断接口是否连接是否为null
		if (NLPCaller4WSClient == null) {
			// 返回的词模为空
			return "";
		}
		logger.info("问题库自学习词模接口的输入串：" + queryObject);
		// 获取接口的返回json串
		String result = NLPCaller4WSClient.kAnalyze(queryObject);
		
		// add by zhao lipeng. 20170210 START
		logger.info("问题库自学习词模调用接口返回：" + result);
		// add by zhao lipeng. 20170210 END
		
		// 替换掉回车符和空格
		result = result.replace("\n", "").replace(" ", "").trim();
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 返回的词模为空
			return "";
		} else {
			try {
				// 将接口的返回串用JSONObject转换为JSONObject对象
				JSONObject obj = JSONObject.parseObject(result);
				// 将json串中的key为autoLearnedPat的value返回
				return obj.getString("autoLearnedPat");
			} catch (Exception e) {
				e.printStackTrace();
				// 转换json格式时出现错误
				return "";
			}
		}
	}


	/**
	 *@description 获得问题库自学习词模
	 *@param query
	 *@param servicetype
	 *@param request
	 *@return
	 *@returnType String
	 */
	/*
	public static String getAutoWordpat(String query, String servicetype,
			HttpServletRequest request) {
		String wordpat = "";
		// 获取高级分析的接口串中的serviceInfo
		String serviceInfo = MyUtil.getServiceInfo(servicetype, "问题生成词模", "",
				false);
		// 获取高级分析的串
		String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模", query,
				servicetype, serviceInfo);
		// 调用生成词模的接口生成词模,可能是多个，以@_@分隔
		wordpat = getWordpat(queryObject);
		if (wordpat != null && !"".equals(wordpat)) {
			// 判断词模是否含有@_@
			if (wordpat.contains("@_@")) {
				// 有的话，按照@_@进行拆分,并只取第一个
				wordpat = wordpat.split("@_@")[0];
			}
			// 获取词模中@前面的词模题，在加上@2#编者="问题库"&来源="(当前问题)"
			wordpat = wordpat.split("@")[0] + "@2#编者=\"问题库\"&来源=\""
					+ query.replace("&", "\\and") + "\"";

			// 校验自动生成的词模是否符合规范
			if (Check.CheckWordpat(wordpat, request)) {
				return wordpat;
			}
		}
		return "";
	}
	*/


	/**
	 * 导入客户问题到数据库中
	 * 
	 * @param filename参数文件名称
	 * @param serviceid
	 *            业务ID
	 * @return
	 */
	public static Object importFile(String filename, String serviceid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		List<String> userCityList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
				.resourseAccess(user.getUserID(), "querymanage", "S");
		userCityList = resourseMap.get("地市");
		String userCityCode = "";
		if (userCityList.size() > 0) {
			userCityCode = StringUtils.join(userCityList.toArray(), ",");
		}else{
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将内容为空放入jsonObj的msg对象中
			jsonObj.put("msg", "导入失败!");
			return jsonObj;
		}
        String workerid = user.getUserID();
        String serviceType = user.getIndustryOrganizationApplication();
     // 获得商家标识符
		String bussinessFlag = CommonLibMetafieldmappingDAO
				.getBussinessFlag(serviceType);
		
		// 获取文件的路径
		String pathName = regressTestPath + File.separator + filename;
		// 获取上传文件的file
		File file = new File(pathName);
		// 获取导入文件的类型
		String extension = filename.lastIndexOf(".") == -1 ? "" : filename
				.substring(filename.lastIndexOf(".") + 1);
		// 定义当前文件后得到的集合
		List<List<Object>> info = new ArrayList<List<Object>>();
		if ("xls".equalsIgnoreCase(extension)) {
			// 读取2003版的Excel
			info = MyUtil.read2003Excel(file, 5);
		} else if ("xlsx".equalsIgnoreCase(extension)) {
			// 读取2007版的Excel
			info = MyUtil.read2007Excel(file, 5);
		}
		// 判断文件是否存在
		if (file.exists()) {
			// 删除文件
			file.delete();
		}
		if (info.size() > 0) {
		 String cloum1 =info.get(0).get(0)+"";
		 if(info.size()==1){
			if("标准问题".equals(cloum1)){
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "文件内容为空!");
				return jsonObj;
			}
		}else{
			if("标准问题".equals(cloum1)){//忽略Excel列名
			 info.remove(0);
			}
			
			int count  = CommonLibQueryManageDAO.importQuery(getImportQueryDic(info,userCityList),getQueryDic(serviceid,0),userCityList, serviceid,bussinessFlag,workerid,0);
			if(count>0){ 
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "导入成功!");	
			}else{
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "导入失败!");	
			}
		 }
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将内容为空放入jsonObj的msg对象中
			jsonObj.put("msg", "文件内容为空!");
		}
		return jsonObj;
	}

	
	/**
	 * 导出问题
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static File exportFile(String serviceid, String normalQuery, String responseType,
			String interactType){
		File file = null;
		Result result = CommonLibQueryManageDAO.exportQuery(serviceid, normalQuery, responseType, interactType,0);
		if (result != null && result.getRowCount() > 0){
			List colTitle = Arrays.asList("标准问题","客户问题","回复类型","交互类型","来源地市");
			List text = new ArrayList();
			String normalquery = "";
			for (int i = 0; i < result.getRowCount(); i++) {
				List line = new ArrayList();
				normalquery = (result.getRows()[i].get("abstract") == null ? "" : result.getRows()[i].get("abstract").toString());
				line.add(StringUtils.substringAfterLast(normalquery, ">"));
				line.add(result.getRows()[i].get("query") == null ? "" : result.getRows()[i].get("query").toString());
				line.add(result.getRows()[i].get("responsetype") == null ? "" : result.getRows()[i].get("responsetype").toString());
				line.add(result.getRows()[i].get("interacttype") == null ? "" : result.getRows()[i].get("interacttype").toString());
				line.add(getCityName(result.getRows()[i].get("city").toString()));
				text.add(line);
			}
			String filename = "导出_客户问题_";
			filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			ReadExcel.writeExcel(FILE_PATH_EXPORT, filename, null, null, colTitle, text);
			file = new File(FILE_PATH_EXPORT + filename + ".xls");
		}
		return file;
		
	}

	/**
	 * 获取地市名集合
	 * 
	 * @param cityCode
	 * @return
	 */
	private static String getCityName(String cityCode){
		String cityName = null;
		if(StringUtils.isBlank(cityCode)){
			return "";
		}
		String[] codes = cityCode.split(",");
		String tmp = null;
		for(int i = 0; i < codes.length; i++){
			tmp = QuerymanageDAO.cityCodeToCityName.get(codes[i].trim());
			if(cityName == null){
				cityName = tmp;
			} else {
				cityName = cityName + "," + tmp;
			}
		}
		
		return cityName;
	}

	/**
	 *@description 获得导入客户问题字典
	 *@param info
	 *@param userCityList
	 *@return 
	 *@returnType Map<String,Map<String,List<String>>> 
	 */
	public static Map<ImportNormalqueryBean, Map<String, List<String>>> getImportQueryDic(List<List<Object>> info,List<String> userCityList ){
		Map<ImportNormalqueryBean, Map<String, List<String>>> map = new LinkedHashMap<ImportNormalqueryBean, Map<String, List<String>>>();
		for (int i = 0; i < info.size(); i++) {
			List<String> cityList = new ArrayList<String>();
			String normalquery = info.get(i).get(0)!=null?  info.get(i).get(0).toString().replace(" ", ""):"";
			String customerquery = info.get(i).get(1)!=null?  info.get(i).get(1).toString().replace(" ", ""):"";
			String responsetype = info.get(i).get(2)!=null?  info.get(i).get(2).toString().replace(" ", ""):"";
			String interacttype = info.get(i).get(3)!=null?  info.get(i).get(3).toString().replace(" ", ""):"";
			ImportNormalqueryBean normalqueryBean = new ImportNormalqueryBean(normalquery, getResponseType(responsetype), getInteractType(interacttype));
			if("".equals(normalquery)){//如果标准问题为空，不做处理
				continue;
			}
			String city = info.get(i).get(4)!=null?  info.get(i).get(4).toString().replace(" ", "").replace("，", ","):"";
			if (!"".equals(city) && city != null) {//如果客户问题来源地市不为空通过地市名称取地址编码
				city = city.replace("省", "").replace("市", "");
				String cityArray[] = city.split(",");
				for (int m = 0; m < cityArray.length; m++) {
					if (cityNameToCityCode.containsKey(cityArray[m])) {
						String cityCode = cityNameToCityCode.get(cityArray[m]);
						cityList.add(cityCode);
					}
				}
			}else{//如果客户问题来源地市为空，取当前用户关联地市
				cityList.addAll(userCityList);
			}
			Collections.sort(cityList);
			if(map.containsKey(normalqueryBean)){
				Map<String,List<String>>  tempMap = map.get(normalqueryBean);
				if(tempMap.containsKey(customerquery)){
					List<String> oldCityList =tempMap.get(customerquery);
					oldCityList.addAll(cityList);
					Set set = new HashSet(oldCityList);
					List<String> newCityCodelist = new ArrayList<String>(set);
					Collections.sort(newCityCodelist);
					tempMap.put(customerquery, newCityCodelist);
				}else{
					tempMap.put(customerquery, cityList);
					map.put(normalqueryBean, tempMap);
				}
			}else{
				Map<String, List<String>> queryMap = new HashMap<String,List<String>>();
				queryMap.put(customerquery, cityList);
				map.put(normalqueryBean, queryMap);
			}
		}
		return map;
		
	}
	
	/**
	 * 获取回复类型
	 * @param inStr
	 * @return
	 */
	public static String getResponseType(String inStr){
		if(inStr != null && responseTypes.contains(inStr)){
			return inStr;
		}
		return responseTypes.get(0); // Defauult value.
	}
	
	/**
	 * 获取交互类型
	 * @param inStr
	 * @return
	 */
	public static String getInteractType(String inStr){
		if(inStr != null && interactTypes.contains(inStr)){
			return inStr;
		}
		return interactTypes.get(0); // Defauult value.
	}
	
	/**
	 *@description 获得业务下客户问题字典
	 *@param serviceid
	 *@return
	 *@returnType Map<String,Map<String,String>>
	 */
 	public static Map<String, Map<String, String>> getQueryDic(String serviceid,int querytype) {
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		Result rs = CommonLibQueryManageDAO.getQuery(serviceid,querytype);
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				Map<String, String> queryMap = new HashMap<String, String>();
				String normalquery = rs.getRows()[i].get("abstract").toString()
						.split(">")[1];
				String query = rs.getRows()[i].get("query") != null ? rs
						.getRows()[i].get("query").toString() : "";
				String city = rs.getRows()[i].get("city") != null ? rs
						.getRows()[i].get("city").toString() : "";
				String abscity = rs.getRows()[i].get("abscity") != null ? rs
						.getRows()[i].get("abscity").toString() : "";
				String kbdataid = rs.getRows()[i].get("kbdataid").toString();
				if (map.containsKey(normalquery)) {
					Map<String, String> tempMap = map.get(normalquery);
					tempMap.put(query, city);
					map.put(normalquery, tempMap);

				} else {
					queryMap.put(query, city);
					queryMap.put("kbdataid", kbdataid);
					queryMap.put("abscity", abscity);
					map.put(normalquery, queryMap);
				}
			}
		}
		return map;
	}


	/**
	 *@description 通过摘要ID 获取场景信息
	 *@param kbdataid
	 *@param key
	 *@return
	 *@returnType Object
	 */
	public static Object findScenarios(String kbdataid, String key) {
		JSONObject jsonObj = new JSONObject();
		Result rs = CommonLibQueryManageDAO._findScenarios(kbdataid);
		String url = getAddress(key);
		if (rs != null && rs.getRowCount() > 0) {
			jsonObj.put("success", true);
			// 将内容为空放入jsonObj的msg对象中
			jsonObj.put("name", rs.getRows()[0].get("name"));
			jsonObj.put("scenariosid", rs.getRows()[0].get("scenariosid"));
			jsonObj.put("url", url);
		} else {
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 *@description 获取参数配置中菜单地址
	 *@return
	 *@returnType JSONObject
	 */
	public static String getAddress(String key) {
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("菜单地址配置", key);
		String url = "";
		if (rs != null && rs.getRowCount() > 0) {
			url = rs.getRows()[0].get("name").toString();
		}

		return url;
	}

	/**
	 *构造业务树
	 * 
	 * @param serviceid
	 *@return
	 *@returnType Object
	 */
	public static Object createTree(String serviceid) {

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		String brand = "'" + serviceType.split("->")[1] + "','"
				+ serviceType.split("->")[1] + "问题库'";
		// 定义返回的json串
		JSONArray array = new JSONArray();
		Result rs = CommonLibServiceDAO.createServiceTree(serviceid, brand);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String sid = rs.getRows()[i].get("serviceid").toString();
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", sid);
				jsonObj.put("text", rs.getRows()[i].get("service").toString());
				if (CommonLibKbdataAttrDAO.hasChild(sid) == 0) {// 如果没有子业务
					// jsonObj.put("iconCls", "icon-servicehit");
					jsonObj.put("leaf", true);
				} else {
					// jsonObj.put("expanded","true");
					jsonObj.put("cls", "folder");
					jsonObj.put("leaf", false);
					jsonObj.put("state", "closed");
				}

				array.add(jsonObj);
			}
		}
		return array;

	}

	/**
	 * 构造摘要下拉框
	 * 
	 * @param serviceid
	 *            业务id
	 * @return
	 */
	public static Object createAbstractCombobox(String serviceid) {
		if ("".equals(serviceid)) {
			return "";
		}
		Result rs = CommonLibKbDataDAO.getAbstractByServiceid(serviceid);
		// 定义返回的json串
		JSONArray array = new JSONArray();

		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", rs.getRows()[i].get("kbdataid").toString());
				jsonObj.put("text", rs.getRows()[i].get("abstract").toString()
						.split(">")[1]);
				array.add(jsonObj);
			}
		}

		return array;
	}

	/**
	 *@description 新增相关问题
	 *@param relatequerytokbdataid
	 *@param relatequery
	 *@param kbdataid
	 *@return
	 *@returnType Object
	 */
	public static Object insertRelateQuery(String relatequerytokbdataid,
			String relatequery, String kbdataid) {
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String workerid = user.getUserID();
		int rs = CommonLibQueryManageDAO._insertRelatequery(
				relatequerytokbdataid, relatequery, kbdataid, workerid);
		if (rs > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "保存成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "保存失败!");
		}
		return jsonObj;
	}

	/**
	 *@description 获取相关问题
	 *@param kbdataid
	 *@param relatequery
	 *@param page
	 *@param rows
	 *@return
	 *@returnType Object
	 */
	public static Object selectRelateQuery(String kbdataid, String relatequery,
			int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int count = CommonLibQueryManageDAO.getRelateQueryCount(kbdataid,
				relatequery);
		if (count > 0) {
			jsonObj.put("total", count);
			Result rs = CommonLibQueryManageDAO.selectRelateQuery(kbdataid,
					relatequery, page, rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject obj = new JSONObject();
					obj.put("relatequeryid", rs.getRows()[i].get("id"));
					String abs = rs.getRows()[i].get("abs") != null ? rs
							.getRows()[i].get("abs").toString() : "";
					String relRelatequery = "";
					if (!"".equals(abs)) {
						relRelatequery = abs.split(">")[1];
					}
					obj.put("relatequery", relRelatequery);
					obj.put("kbdataid", rs.getRows()[i].get("kbdataid"));
					obj.put("kbdataid", rs.getRows()[i].get("kbdataid"));
					obj.put("relatequerytokbdataid", rs.getRows()[i]
							.get("relatequerytokbdataid"));
					obj.put("remark", rs.getRows()[i].get("remark"));
					obj.put("service", rs.getRows()[i].get("service"));

					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			jsonObj.put("rows", jsonArr);
		} else {
			jsonArr.clear();
			jsonObj.put("total", 0);
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	/**
	 *@description 删除相关
	 *@param combition
	 *@return
	 *@returnType Object
	 */
	public static Object deleteRelateQuery(String combition) {
		JSONObject jsonObj = new JSONObject();
		List<String> tempList = new ArrayList<String>();
		String combitionArray[] = combition.split("@@");
		for (int i = 0; i < combitionArray.length; i++) {
			tempList.add(combitionArray[i]);
		}
		int rs = CommonLibQueryManageDAO._deleteRelateQuery(tempList);
		if (rs > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "删除成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}
	


	/**
	 *@description 迁移标准问
	 *@param serviceid
	 *@param kbdataid
	 *@param abs
	 *@return 
	 *@returnType Object 
	 */
	public static Object transferNormalQuery(String serviceid,String[] kbdataids,String[] abses){
		JSONObject jsonObj = new JSONObject();
		int rs = CommonLibQueryManageDAO.updateNormalQueryPath(serviceid,kbdataids,abses);
		if (rs > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "迁移成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "迁移失败!");
		}
		return jsonObj;
	}
	
	
	public static JSONArray getNewResults(JSONArray results, int getResultCount){
		if(results != null && results.size() > 0){
			JSONArray newResults = new JSONArray();
			for(int i = 0; i < getResultCount && i < results.size(); i++){
				if(newResults.isEmpty()){
					newResults.add(results.getJSONObject(i));
				}else{
					if(newResults.getJSONObject(newResults.size()-1).getDoubleValue("credit") > results.getJSONObject(i).getDoubleValue("credit") * 2){
						break;
					}else{
						newResults.add(results.getJSONObject(i));
					}
				}
			}
			return newResults;
		}
		return null;
	}
	
	/**
	 * 构造场景树
	 * 
	 * @param scenariosid
	 *            场景id
	 * @return Object
	 */
	public static Object createInteractiveSceneTree(String scenariosid) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 定义返回的json串
		JSONArray array = new JSONArray();
		Result rs = CommonLibInteractiveSceneDAO.createInteractiveSceneTree(
				scenariosid, serviceType);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String sid = rs.getRows()[i].get("scenariosid").toString();
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", sid);
				jsonObj.put("text", rs.getRows()[i].get("name").toString());
				if (CommonLibInteractiveSceneDAO.hasChild(sid) == 0) {// 如果没有子业务
					// jsonObj.put("iconCls", "icon-servicehit");
					jsonObj.put("leaf", true);
				} else {
					// jsonObj.put("expanded","true");
					jsonObj.put("cls", "folder");
					jsonObj.put("leaf", false);
					jsonObj.put("state", "closed");
				}

				array.add(jsonObj);
			}
		}
		return array;
	}
	
	public static Object bindNormalQuery2Scenorio(String kbdataid, String scenariosid){
		JSONObject jsonObj = new JSONObject();
		int n = CommonLibInteractiveSceneDAO.insertScenarios2kbdata(kbdataid, scenariosid);
		jsonObj.put("success", n == 1);
		return jsonObj;
	}
	
	
	/**
	 *@description 测试分类问题
	 *@param question
	 *            问题
	 *@param province
	 *            省份
	 *@param city
	 *            地市
	 *@return
	 *@returnType Object
	 */
	public static Object testQuery(String question, String province,
			String city) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		String serviceType = user.getIndustryOrganizationApplication();
		String array[] = serviceType.split("->") ;
		serviceType = array[0]+"->"+array[1]+"->"+array[1]+"问题库";
//		String userid ="179";
//		String serviceType="电信行业->电信集团->4G业务客服应用";
		
		String url = "";
		Result rs = UserOperResource.getConfigValue("简要分析服务地址配置", "本地服务");
		if (rs != null && rs.getRowCount() > 0) {
			// 获取配置表的服务url
			url = rs.getRows()[0].get("name").toString();
		}
//		url = "http://222.186.101.213:8282/NLPAppWS/AnalyzeEnterPort?wsdl";//TODO测试地址
//		url = "http://134.64.22.251:8042/NLPAppWS/AnalyzeEnterPort?wsdl";//TODO测试地址
		if ("".equals(city) || city == null) {
			city = province;
		}
		//默认渠道Web
		String channel = "Web";
		Object object = kanalyzeResult(userid,
				serviceType, channel, question, url, province, city, "问题分类测试");
		JSONObject jsonObj = (JSONObject) object;
		String success = jsonObj.get("success").toString();
		if ("false".equals(success)) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "理解失败");
		} else {
			//获得参数配置中问题自动分类最大结果数，默认值为1
			int getResultCount =1;
			rs = UserOperResource.getConfigValue("问题库参数配置", "问题自动分类最大结果数");
			if (rs != null && rs.getRowCount() > 0) {
				getResultCount = Integer.parseInt(rs.getRows()[0].get("name").toString());
			}
			// 取接口结果，注：接口返回结果数量集可能<getResultCount
			// 如果r(i).Credit > r(i+1).Credit * 2, 则保留r(1),r(2),...,r(i+1).
			JSONArray results = (JSONArray) jsonObj.get("result");
			if(results != null && results.size() > 0){
				JSONArray newResults = new JSONArray();
				for(int i = 0; i < getResultCount && i < results.size(); i++){
					if(newResults.isEmpty()){
						newResults.add(results.getJSONObject(i));
					}else{
						if(newResults.getJSONObject(newResults.size()-1).getDoubleValue("credit") > results.getJSONObject(i).getDoubleValue("credit") * 2){
							break;
						}else{
							newResults.add(results.getJSONObject(i));
						}
					}
				}
				jsonObj.put("result", newResults);
			}
		}
		return jsonObj;
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
	public static Object kanalyzeResult(String user, String service,
			String channel, String question, String ip, String province,
			String city, String type) {
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
		// 获取调用接口的入参字符串
		String queryObject = getServiceClient.getKAnalyzeQueryObject_new(user, question,
				service, channel, province, city);
		// String queryObject = MyUtil.getKAnalyzeQueryObject(user, question,
		// service, channel);
		logger.info("问题分类分析接口的输入串：" + queryObject);
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
			logger.info("问题分类分析接口的输出串：" + result);
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
			 for (int i = 0; i < kNLPResultsArray.size(); i++){
				// 定义一个json对象
				JSONObject o = new JSONObject();
				// 将kNLPResultsArray数组中的第i个转换为json对象
				JSONObject kNLPResultsObj = JSONObject
						.parseObject(kNLPResultsArray.get(i).toString());
				// 遍历取继承词模返回值
				String retrnKeyValue = "";
				JSONArray parasArray = kNLPResultsObj.getJSONArray("paras");
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
				// 获取kNLPResultsObj对象中credit，并生成credit对象
				o.put("credit", kNLPResultsObj.getString("credit"));
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
	
	
//	public static void main(String[] args) {
//		//testQuery("你好","","");
//		JSONArray jsonArray = SERVICE_TREE_MENU;
//		System.out.println(jsonArray.toJSONString());
//	}

}
