package com.knowology.km.bll;

import java.io.File;
import java.io.IOException;
import java.sql.Clob;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.sql.Result;

import oracle.sql.CLOB;
import oracle.sql.TIMESTAMP;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.ImportNormalqueryBean;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibFaqDAO;
import com.knowology.bll.CommonLibInteractiveSceneDAO;
import com.knowology.bll.CommonLibKbDataDAO;
import com.knowology.bll.CommonLibKbdataAttrDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibNewWordInfoDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.bll.CommonLibQuestionUploadDao;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.bll.CommonLibWordDAO;
import com.knowology.bll.CommonLibWordclassDAO;
import com.knowology.bll.CommonLibWordpatDAO;
import com.knowology.bll.ConstructSerialNum;
import com.knowology.bll.UserManagerDAO;
import com.knowology.dal.Database;
import com.knowology.km.NLPAppWS.AnalyzeEnterDelegate;
import com.knowology.km.NLPCallerWS.NLPCaller4WSDelegate;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.entity.InsertOrUpdateParam;
import com.knowology.km.util.Check;
import com.knowology.km.util.GetLoadbalancingConfig;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.GlobalValues;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.ReadExcel;
import com.knowology.km.util.SimpleString;
import com.knowology.km.util.getConfigValue;
import com.knowology.km.util.getServiceClient;
import com.str.NewEquals;

public class QuerymanageDAO {

	public static Logger logger = Logger.getLogger("querymanage");

	public static String regressTestPath = System.getProperty("os.name").toLowerCase().startsWith("win")
			? Database.getCommmonLibJDBCValues("winDir") + File.separator + "qatraining" + File.separator
					+ "regresstest"
			: Database.getCommmonLibJDBCValues("linDir") + File.separator + "qatraining" + File.separator
					+ "regresstest";

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

	/**
	 * 交互类型映射 <数据库值，页面值>
	 */
	public static Map<String, String> interactTypesMap = new HashMap<String, String>();

	/**
	 * 词模类型
	 */
	public static Map<String, String> wordpatType = new HashMap<String, String>();

	// public static Map<String, Object> SERVICE_TREE_MENU_MAP = new
	// HashMap<String, Object>();

	/**
	 * 创建字典
	 */
	static {
		// 启动时清空文件目录
		FileUtils.deleteQuietly(new File(FILE_PATH_EXPORT));

		Result r = CommonLibMetafieldmappingDAO.getConfigMinValue("地市编码配置");
		if (r != null && r.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < r.getRowCount(); i++) {
				String key = r.getRows()[i].get("k") == null ? "" : r.getRows()[i].get("k").toString();
				String value = r.getRows()[i].get("name") == null ? "" : r.getRows()[i].get("name").toString();
				cityCodeToCityName.put(value, key);
				cityNameToCityCode.put(key, value);
			}
		}

		// 词模类型
		wordpatType.put("0", "普通词模");
		wordpatType.put("1", "等于词模");
		wordpatType.put("2", "排除词模");
		wordpatType.put("3", "选择词模");
		wordpatType.put("4", "特征词模");
		wordpatType.put("5", "自学习词模");

		r = CommonLibMetafieldmappingDAO.getConfigValue("问题库参数配置", "交互类型");
		if (r != null && r.getRowCount() > 0) {
			for (int i = 0; i < r.getRowCount(); i++) {
				String name = StringUtils.defaultString(r.getRows()[i].get("name").toString());
				if (name.contains("#")) {
					interactTypesMap.put(name.split("#")[1], name.split("#")[0]);
					interactTypes.add(name.split("#")[0]);
				} else {
					interactTypes.add(name);
				}

			}
		}

		r = CommonLibMetafieldmappingDAO.getConfigValue("问题库参数配置", "回复类型");
		if (r != null && r.getRowCount() > 0) {
			for (int i = 0; i < r.getRowCount(); i++) {
				responseTypes.add(r.getRows()[i].get("name") == null ? null : r.getRows()[i].get("name").toString());
			}
		}

	}

	/**
	 * 构造业务树。该方法在加入权限控制后变为过期方法。使用
	 * {@link com.knowology.km.bll.QuerymanageDAO#createServiceTree(String, String, String)}
	 * 代替此方法
	 * 
	 * @param serviceid
	 * @return Object
	 */
	@Deprecated
	public static Object createServiceTree(String serviceid, String citySelect) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		String brand = serviceType.split("->")[1] + "问题库";
		// String brand ="安徽电信";
		// 定义返回的json串
		JSONArray array = new JSONArray();
		Result rs = CommonLibQueryManageDAO.createServiceTree(serviceid, brand, citySelect);
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
	 * 构造业务树， createServiceTreeNew 实现了延时加载方式，其中根据用户ID实现权限控制，减少加载内容。
	 * 另外citySelect用来过滤无关的节点
	 * 
	 * @param serviceid
	 *            用户选定业务的ID
	 * @param citySelect
	 * @return
	 */
	public static Object createServiceTreeNew(String scenariosid, String citySelect) {

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 定义返回的json串
		JSONArray array = new JSONArray();
		Result rs = CommonLibQueryManageDAO.createServiceTreeNew(user.getUserID(), serviceType, "querymanage",
				citySelect, scenariosid);
		// 根节点不过滤
		if (StringUtils.isEmpty(scenariosid)) {
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 判断用户是否拥有根节点权限
					String sid = rs.getRows()[i].get("serviceid").toString();
					boolean permission = CommonLibPermissionDAO.isHaveOperationPermission(user.getUserID(),
							"querymanage", sid, "S", "");
					if (!permission)
						continue;
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("id", sid);
					jsonObj.put("text", rs.getRows()[i].get("service").toString());
					jsonObj.put("parentid", "0");
					// 如果有子节点
					if (CommonLibQueryManageDAO.hasChild(sid) > 0) {
						jsonObj.put("cls", "folder");
						jsonObj.put("leaf", false);
						jsonObj.put("state", "closed");
					} else {
						jsonObj.put("leaf", true);
					}

					array.add(jsonObj);
				}
			}
		} else {
			// 有子节点的业务
			Set<String> parentLevel3 = getServiceParentLevel3(scenariosid, "");
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String sid = rs.getRows()[i].get("serviceid").toString();
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("id", sid);
					jsonObj.put("text", rs.getRows()[i].get("service").toString());
					jsonObj.put("parentid", scenariosid);
					if (!parentLevel3.contains(sid)) {// 如果没有子业务
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
		}

		return array;
	}

	/**
	 * 查找父节点下子节点和孙节点，返回具有孙节点的子节点ID，该子节点会在页面上显示为文件夹的形式。
	 * 
	 * @param parentId
	 * @param brand
	 * @return
	 */
	private static Set<String> getServiceParentLevel3(String parentId, String brand) {
		Set<String> set = new HashSet<String>();
		Result rs = CommonLibQueryManageDAO.getServiceByPId(parentId, "");

		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				if (rs.getRows()[i].get("parentid") != null) {
					set.add(rs.getRows()[i].get("parentid").toString());
				}
			}
		}
		return set;
	}

	/**
	 * 构建业务树
	 * 
	 * @return
	 */
	public static Object createServiceTree(String userId, String resourceType, String serviceType, String citySelect) {
		JSONArray menuTree = new JSONArray();
		if (citySelect == null || "".equals(citySelect.trim())) {
			citySelect = "全国";
		}
		List<String> cityList = new ArrayList<String>();
		cityList.add(citySelect);
		List<Map<String, String>> serviceInfos = CommonLibPermissionDAO.getServiceResource(userId, resourceType,
				serviceType, cityList);

		// 根业务
		List<String> roots = new ArrayList<String>();

		// 递归获取根业务下所有子孙节点业务及根业务本身
		JSONArray jsonArray = new JSONArray();
		if (serviceInfos != null && serviceInfos.size() > 0) {
			for (int i = 0; i < serviceInfos.size(); i++) {
				String sid = serviceInfos.get(i).get("id");
				String text = serviceInfos.get(i).get("name");
				String parentid = serviceInfos.get(i).get("pid");
				// 父节点为0的节点视为根节点
				if ("0".equals(parentid)) {
					roots.add(sid);
				}
				if ("0.000".equals(parentid)) {
					roots.add(sid);
				}
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", sid);
				jsonObj.put("text", text);
				jsonObj.put("parentid", parentid);
				jsonObj.put("state", "closed");
				jsonArray.add(jsonObj);
			}

			// 构建业务树（可能多颗，依据根节点数目）
			for (String rootserviceId : roots) {
				JSONArray serviceTree = treeMenuList(jsonArray, rootserviceId);
				for (Object object : jsonArray) {
					JSONObject node = (JSONObject) JSONObject.toJSON(object);
					if (NewEquals.equals(node.getString("id"), rootserviceId)) {
						if (serviceTree.size() > 0) {
							node.put("children", serviceTree);
						} else {
							node.put("state", "");
						}

						menuTree.add(node);
					}
				}
			}

		}

		return menuTree;
	}

	public static void main(String[] args) {
		// System.out.println(createServiceTree("179", "querymanage",
		// "电信行业->电信集团->4G业务客服应用", "全国"));
		// System.out.println(createServiceTree("186", "scenariosrules",
		// "电信行业->电信集团->4G业务客服应用", "全国"));
		// System.out.println(exportService("1804103.1"));
		System.out.println(importService("service_20190514110506.json", "1837123.36", "通用行业->通用商家->个性化应用", "36"));
	}

	/**
	 * 获取业务树
	 */
	public static Object getServiceTree(String citySelect) {
		User user = (User) GetSession.getSessionByKey("accessUser");
		String serviceType = user.getIndustryOrganizationApplication();
		Object menu = createServiceTree(user.getUserID(), "querymanage", serviceType, citySelect);
		return menu;
	}

	/**
	 * 刷新业务树
	 */
	public static Object refreshServiceTree(String citySelect) {
		return createServiceTreeNew(null, citySelect);
		// return getServiceTree(citySelect);
	}

	/**
	 * 构造业务菜单树数据
	 * 
	 * @param menuList
	 * @param parentId
	 * @return
	 */
	private static JSONArray treeMenuList(JSONArray menuList, String parentId) {
		JSONArray childMenu = new JSONArray();
		for (Object object : menuList) {
			JSONObject jsonMenu = (JSONObject) JSONObject.toJSON(object);
			String serviceid = jsonMenu.getString("id");
			String pid = jsonMenu.getString("parentid");
			if (NewEquals.equals(parentId, pid)) {
				JSONArray c_node = treeMenuList(menuList, serviceid);
				if (c_node == null || c_node.size() == 0) {
					jsonMenu.put("state", "");
				} else {
					jsonMenu.put("children", c_node);
				}

				childMenu.add(jsonMenu);
			}
		}
		return childMenu;
	}

	/**
	 * 业务查询
	 * 
	 * @param serviceStr
	 * @return
	 */
	public static Object searchService(String serviceStr, String citySelect) {
		JSONArray jsonArray = new JSONArray();

		User user = (User) GetSession.getSessionByKey("accessUser");
		if (user == null) {
			JSONObject rst = new JSONObject();
			rst.put("success", false);
			rst.put("message", "重新登录");
			return rst;
		}
		String serviceType = user.getIndustryOrganizationApplication();

		if (citySelect == null || "".equals(citySelect.trim())) {
			citySelect = "全国";
		}
		List<String> cityList = new ArrayList<String>();
		// List<String> serviceidList = new ArrayList<String>();
		cityList.add(citySelect);

		// mod by zhao lipeng.20170821.START
		// //获取用户业务树
		// List<Map<String, String>> serviceInfos =
		// CommonLibPermissionDAO.getServiceResource(user.getUserID(),
		// "querymanage", serviceType, cityList);
		// if (serviceInfos != null && serviceInfos.size() > 0) {
		// for (int i = 0; i < serviceInfos.size(); i++) {
		// String sid = serviceInfos.get(i).get("id");
		// serviceidList.add(sid);
		// }
		// }
		// mod by zhaolipeng.20170821.END

		// String brand = serviceType.split("->")[1] + "问题库";
		// String brand2 = serviceType.split("->")[1] + "模板业务";

		// 根业务
		List<String> rootServiceList = new ArrayList<String>();
		rootServiceList.add("电信垃圾问题库");
		Result rootRs = CommonLibMetafieldmappingDAO.getConfigValue("问题库业务根对应关系配置", serviceType);
		if (rootRs != null && rootRs.getRowCount() > 0) {
			for (int i = 0; i < rootRs.getRowCount(); i++) {
				String rootService = rootRs.getRows()[i].get("name").toString();
				rootServiceList.add(rootService);
			}
		}

		// Result rs = CommonLibQueryManageDAO.searchService(rootServiceList,
		// serviceStr, serviceidList);
		Result rs = CommonLibQueryManageDAO.searchService(rootServiceList, serviceStr, citySelect, user);
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
	 * 添加业务节点，brand与父节点一致
	 * 
	 * @param preServiceId
	 *            前一个业务节点ID
	 * @param serviceName
	 *            待添加业务
	 * @return
	 */
	public static Object appendService(String preServiceId, String serviceName) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		String serviceType = user.getIndustryOrganizationApplication();
		String brand = "";
		Result rs = CommonLibServiceDAO.getServiceInfoByserviceid(preServiceId);
		if (rs != null && rs.getRowCount() > 0) {
			Object obj = rs.getRows()[0].get("brand");
			if (obj == null || "".equals(obj)) {
				jsonObj.put("success", false);
				jsonObj.put("msg", "业务根不存在！");
				return jsonObj;
			}
			brand = obj.toString();
		}

		String bussinessFlag = CommonLibMetafieldmappingDAO.getBussinessFlag(serviceType);

		if (CommonLibQueryManageDAO.isExistServiceNameNew(preServiceId, serviceName, brand)) {// 判断是否存在相同名称业务
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "业务名称已存在!");
			return jsonObj;
		}

		String serviceId = CommonLibQueryManageDAO.insertService(preServiceId, serviceName, brand, bussinessFlag, user);
		if (serviceId != null) {
			jsonObj.put("success", true);
			jsonObj.put("serviceid", serviceId);
			jsonObj.put("msg", "新业务添加成功");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "新业务添加失败");
		}
		return jsonObj;
	}

	/**
	 * 修改业务节点名称
	 * 
	 * @param serviceId
	 *            业务节点ID
	 * @param parentId
	 *            业务父节点ID
	 * @param newServiceName
	 *            新业务节点名称
	 * @return
	 */
	public static Object renameService(String serviceId, String parentId, String newServiceName) {
		JSONObject jsonObj = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		String serviceType = user.getIndustryOrganizationApplication();
		if (CommonLibQueryManageDAO.isExistServiceName(parentId, newServiceName, serviceType)) {// 判断是否存在相同名称业务
			// 事务处理失败
			jsonObj.put("success", false);
			jsonObj.put("msg", "业务名称已存在!");
			return jsonObj;
		}

		int n = CommonLibQueryManageDAO.renameService(serviceId, newServiceName, user);
		if (n > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "业务名称修改成功");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "业务名称修改失败");
		}
		return jsonObj;
	}

	/**
	 * 删除业务节点
	 * 
	 * @param serviceId
	 *            业务节点ID
	 * @return
	 */
	public static Object deleteService(String serviceId) {
		User user = (User) GetSession.getSessionByKey("accessUser");
		JSONObject jsonObj = new JSONObject();

		// 判断业务是否是叶子节点
		int n = CommonLibQueryManageDAO.hasChild(serviceId);
		if (n > 0) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "只能删除叶子节点业务");
			return jsonObj;
		}

		n = CommonLibQueryManageDAO.deleteService(serviceId, user);
		if (n > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "业务删除成功");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "业务删除失败");
		}
		return jsonObj;
	}

	/**
	 * @description 根据用户信息创建地市树
	 * @param flag
	 * @return
	 * @returnType Object
	 */
	public static Object createCityTreeByLoginInfo(String flag) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String cityCode = "";
		String cityName = "";
		List<String> cityList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(),
				"querymanage", "S");
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
		if (!cityCode.contains("全国")) {
			rs = CommonLibQuestionUploadDao.selProvince(cityCode);
		} else {
			rs = CommonLibQuestionUploadDao.selProvince();
		}

		JSONObject innerJsonObj = null;
		if (null != rs && rs.getRowCount() > 0) {
			JSONObject allJsonObj = new JSONObject();
			// if (!"edit".equals(flag)) {
			if (cityCode.contains("全国")) {
				allJsonObj.put("id", "全国");
				allJsonObj.put("text", "全国");
				if (map.containsKey("全国")) {
					allJsonObj.put("checked", true);
				}
				jsonAr.add(allJsonObj);
			}

			// }

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

						String cityId = innerRs.getRows()[j].get("id").toString();
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
						innerJsonObj.put("text", innerRs.getRows()[j].get("city"));
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
						innerJsonObj.put("text", innerRs2.getRows()[j].get("city"));
						innerJsonObj.put("children", sJsonArr);
						if (map.containsKey(innerRs2.getRows()[j].get("city"))) {
							innerJsonObj.put("checked", true);
						}
						jsonArr.add(innerJsonObj);
					}
					jsonObj.put("state", "closed");
				}
				// if (cityCode.endsWith("0000")) {
				jsonObj.put("id", rs.getRows()[i].get("id"));
				jsonObj.put("text", rs.getRows()[i].get("province"));
				jsonObj.put("children", jsonArr);
				jsonAr.add(jsonObj);
				// } else {
				// jsonAr.add(innerJsonObj);
				// }

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
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库参数配置", "回复类型");
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

		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (!"".equals(flag) && flag != null) {
			jsonObj.put("id", "全部");
			jsonObj.put("text", "全部");
			array.add(jsonObj);
		}
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库参数配置", "交互类型");
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String name = rs.getRows()[i].get("name").toString();
				if (name.contains("#")) {
					jsonObj.put("id", name.split("#")[1]);
					jsonObj.put("text", name.split("#")[0]);
				} else {
					jsonObj.put("id", name);
					jsonObj.put("text", name);
				}
				array.add(jsonObj);
			}
		}
		return array;
	}

	/**
	 * 获得训练模式下拉框数据
	 * 
	 * @param flag
	 * @return
	 */
	public static Object createProduceWordpatCombobox(String flag) {

		// 定义返回的json串
		JSONArray array = new JSONArray();
		JSONObject jsonObj = new JSONObject();
		if (!"".equals(flag) && flag != null) {
			jsonObj.put("id", "全部");
			jsonObj.put("text", "全部");
			array.add(jsonObj);
		}
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库参数配置", "训练模式");
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				jsonObj = new JSONObject();
				String name = rs.getRows()[i].get("name").toString();
				if (name.contains("#")) {
					jsonObj.put("id", name.split("#")[1]);
					jsonObj.put("text", name.split("#")[0]);
				} else {
					jsonObj.put("id", name);
					jsonObj.put("text", name);
				}
				array.add(jsonObj);
			}
		}
		return array;
	}

	/**
	 * @description 新增问题
	 * @param serviceid
	 * @param queryType
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @returnType void
	 */
	public static Object addQuery(String serviceid, String queryType, String normalQuery, String multinormalquery,
			String customerQuery, String cityCode, HttpServletRequest request) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		List<String> cityList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(),
				"querymanage", "S");
		cityList = resourseMap.get("地市");
		String userCityCode = "";
		if (cityList.size() > 0) {
			userCityCode = StringUtils.join(cityList.toArray(), ",");
		}

		// 业务地市
		List<String> serviceCityList = new ArrayList<String>();
		String serviceCityCode = "";
		Result scityRs = CommonLibQueryManageDAO.getServiceCitys(serviceid);
		if (scityRs != null && scityRs.getRowCount() > 0) {
			String city = scityRs.getRows()[0].get("city").toString();
			serviceCityList = Arrays.asList(city.split(","));
		}
		if (serviceCityList.size() > 0) {
			serviceCityCode = StringUtils.join(serviceCityList.toArray(), ",");
		}

		JSONObject jsonObj = new JSONObject();
		int rs = -1;
		if ("客户问题".equals(queryType)) {// 新增客户问题
			rs = CommonLibQueryManageDAO.addCustomerQuery(normalQuery, customerQuery, cityCode, user);
		} else {// 新增标准问及客户问题
			if ("true".equals(multinormalquery)) {
				String msgs = "";
				List<String> oovWordList = new ArrayList<String>();
				for (String normalquery : normalQuery.split("\n")) {
					rs = CommonLibQueryManageDAO.addNormalQueryAndCustomerQuery(serviceid, normalquery, customerQuery,
							cityCode, user, userCityCode, serviceCityCode);
					if (rs < 0) {
						if (rs == -2) {
							msgs += "标准问题【" + normalquery + "】已存在<br>";
						} else {
							msgs += "【" + normalquery + "】保存失败<br>";
						}
					} else {
						msgs = "保存成功!";
						JSONObject oovWordObj = (JSONObject) getOOVWord(serviceid, normalquery, request);
						oovWordList.add(Objects.toString(oovWordObj.get("oovWord"), ""));
						jsonObj.put("oovWord", StringUtils.join(oovWordList, "$_$"));
					}
				}
				jsonObj.put("success", true);
				jsonObj.put("msg", msgs);

				return jsonObj;
			} else {
				rs = CommonLibQueryManageDAO.addNormalQueryAndCustomerQuery(serviceid, normalQuery, customerQuery,
						cityCode, user, userCityCode, serviceCityCode);
				if (rs > 0) {
					JSONObject oovWordObj = (JSONObject) getOOVWord(serviceid, normalQuery, request);
					jsonObj.put("oovWord", oovWordObj.get("oovWord"));
				}
			}

		}

		if (rs > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "保存成功!");
		} else {
			if (rs == -2) {
				jsonObj.put("success", false);
				jsonObj.put("success", "标准问题已存在!");
			} else {
				jsonObj.put("success", false);
				jsonObj.put("success", "保存失败!");
			}
		}
		return jsonObj;

	}

	/**
	 * 获取OOV分词
	 * 
	 * @param serviceid
	 * @param normalQuery
	 * @return
	 */
	public static Object getOOVWord(String serviceid, String normalQuery, HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		List<String> oovWordList = new ArrayList<String>();
		List<String> wordpatList = new ArrayList<String>();
		List<String> combitionList = new ArrayList<String>();
		// 查询当前kbdataid
		String kbdataid = "";
		String city = "";
		String queryid = "";
		Map<String, Map<String, String>> map = CommonLibQueryManageDAO.getNormalQueryDic(serviceid);
		if (normalQuery.contains("\n")) {
			String[] normalQueryArray = normalQuery.split("\n");
			for (int i = 0; i < normalQueryArray.length; i++) {
				if (map.containsKey(normalQuery)) {
					Map<String, String> tempMap = map.get(normalQuery);
					kbdataid = tempMap.get("kbdataid");
					city = tempMap.get("city");
					Result queryRs = CommonLibQueryManageDAO.getQueryIdByQuery(normalQuery, kbdataid);
					queryid = queryRs.getRows()[0].get("id").toString();
					String combition = city + "@#@" + normalQuery + "@#@" + kbdataid + "@#@" + queryid + "@#@";
					JSONObject obj = (JSONObject) AnalyzeDAO.produceWordpat(combition, "0", request);
					if (obj.containsKey("OOVWord")) {
						oovWordList.add(obj.getString("OOVWord"));
					}
					if (obj.containsKey("wordpatList")) {
						wordpatList.add(obj.getString("wordpatList"));
					}
					combitionList.add(combition);
				}
			}
		} else {
			if (map.containsKey(normalQuery)) {
				Map<String, String> tempMap = map.get(normalQuery);
				kbdataid = tempMap.get("kbdataid");
				city = tempMap.get("city");
				Result queryRs = CommonLibQueryManageDAO.getQueryIdByQuery(normalQuery, kbdataid);
				queryid = queryRs.getRows()[0].get("id").toString();
				String combition = city + "@#@" + normalQuery + "@#@" + kbdataid + "@#@" + queryid + "@#@";
				JSONObject obj = (JSONObject) AnalyzeDAO.produceWordpat(combition, "0", request);
				if (obj.containsKey("OOVWord")) {
					oovWordList.add(obj.getString("OOVWord"));
				}
				if (obj.containsKey("wordpatList")) {
					wordpatList.add(obj.getString("wordpatList"));
				}
				combitionList.add(combition);
			}
		}

		jsonObj.put("oovWord", StringUtils.join(oovWordList, "$_$"));
		jsonObj.put("wordpatList", StringUtils.join(wordpatList, "@_@"));
		jsonObj.put("combition", StringUtils.join(combitionList, "&_&"));
		return jsonObj;
	}

	/**
	 * 查找标准问
	 * 
	 * @param normalquery
	 *            标准问
	 * @return
	 */
	public static Object findNormalquery(String normalqueries, String selectCity) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		List<String> citylist = new ArrayList<String>();
		citylist.add(selectCity);
		List<String> roleidList = UserManagerDAO.getRoleIDListByUserId(user.getUserID());

		JSONArray jsonArr = new JSONArray();
		String[] normalQueries = normalqueries.split("\n");
		Result rs = CommonLibQueryManageDAO.findNormalquery(Arrays.asList(normalQueries), serviceType, roleidList,
				citylist);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject node = new JSONObject();
				JSONArray arr = new JSONArray();
				node.put("normalquery", Objects.toString(rs.getRows()[i].get("normalquery"), ""));
				String serviceid = Objects.toString(rs.getRows()[i].get("serviceid"), "");
				ArrayList<String> list = CommonLibServiceDAO.getServicePath(serviceid);
				list.remove("知识库");// 移除根节点
				String servicePath = StringUtils.join(list.toArray(), "->");
				JSONObject obj = new JSONObject();
				obj.put("servicepath", servicePath);
				arr.add(obj);
				node.put("duplicate", arr);
				jsonArr.add(node);
			}
		}

		return jsonArr;
	}

	/**
	 * 查找客户问
	 * 
	 * @param customerquery
	 *            客户问
	 * @return
	 */
	public static Object findCustomerquery(String customerqueries, String selectCity, int queryType) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		List<String> roleidList = UserManagerDAO.getRoleIDListByUserId(user.getUserID());

		List<String> citylist = new ArrayList<String>();
		citylist.add(selectCity);
		JSONArray result = new JSONArray();
		String customerQueryArray[] = customerqueries.split("\n");
		Result rs = CommonLibQueryManageDAO.findCustomerquery(Arrays.asList(customerQueryArray), serviceType,
				roleidList, citylist, queryType);
		if (rs != null && rs.getRowCount() > 0) {

			for (int j = 0; j < rs.getRowCount(); j++) {
				JSONObject obj = new JSONObject();
				JSONArray jsonArr = new JSONArray();
				obj.put("customerquery", Objects.toString(rs.getRows()[j].get("query")));
				JSONObject jsonObj = new JSONObject();
				String serviceid = Objects.toString(rs.getRows()[j].get("serviceid"), "");
				ArrayList<String> list = CommonLibServiceDAO.getServicePath(serviceid);
				list.remove("知识库");// 移除根节点
				String servicePath = StringUtils.join(list.toArray(), "->");

				String abs = rs.getRows()[j].get("abstract").toString();
				String normalquery = StringUtils.substringAfterLast(abs, ">");
				jsonObj.put("servicepath", servicePath);
				jsonObj.put("normalquery", normalquery);
				jsonArr.add(jsonObj);
				obj.put("duplicate", jsonArr);
				result.add(obj);
			}

		}
		return result;
	}

	/**
	 * @description 获取问题列表信息
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @param page
	 * @param rows
	 * @return
	 * @returnType Object
	 */
	public static Object selectQuery(String serviceid, String normalQuery, String customerQuery, String cityCode,
			String responseType, String interactType, int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int count = CommonLibQueryManageDAO.getQueryCount(serviceid, normalQuery, customerQuery, cityCode, responseType,
				interactType);
		if (count > 0) {
			jsonObj.put("total", count);
			Result rs = CommonLibQueryManageDAO.selectQuery(serviceid, normalQuery, customerQuery, cityCode,
					responseType, interactType, page, rows);
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
					obj.put("responsetype", rs.getRows()[i].get("responsetype"));
					obj.put("interacttype", rs.getRows()[i].get("interacttype"));
					obj.put("customerquery", rs.getRows()[i].get("query"));
					String city = rs.getRows()[i].get("city") != null ? rs.getRows()[i].get("city").toString() : "";
					String cityName = "";
					if (!"".equals(city)) {
						String cityArray[] = city.split(",");
						List<String> cityNameList = new ArrayList<String>();
						for (int k = 0; k < cityArray.length; k++) {
							String code = cityCodeToCityName.get(cityArray[k]);
							cityNameList.add(code);
						}
						if (cityNameList.size() > 0) {
							cityName = StringUtils.join(cityNameList.toArray(), ",");
						}
					}

					obj.put("wordpatcount", rs.getRows()[i].get("wordpatcount"));
					obj.put("relatequerycount", rs.getRows()[i].get("relatequerycount"));
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
	 * @description 获取客户问题列表信息
	 * @param serviceid
	 * @param kbdataid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param page
	 * @param rows
	 * @return
	 * @returnType Object
	 */
	public static Object selectCustomerQuery(String serviceid, String kbdataid, String normalQuery,
			String customerQuery, String cityCode, String isTrain, String understandStatus, int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int count = CommonLibQueryManageDAO.getCustomerQueryCount(serviceid, kbdataid, customerQuery, cityCode, isTrain,
				understandStatus);
		if (count > 0) {
			jsonObj.put("total", count);
			Result rs = CommonLibQueryManageDAO.selectCustomerQuery(serviceid, kbdataid, customerQuery, cityCode,
					isTrain, understandStatus, page, rows);
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
					obj.put("responsetype", rs.getRows()[i].get("responsetype"));
					obj.put("interacttype", rs.getRows()[i].get("interacttype"));
					obj.put("customerquery", rs.getRows()[i].get("query"));
					String city = rs.getRows()[i].get("city") != null ? rs.getRows()[i].get("city").toString() : "";
					String cityName = "";
					if (!"".equals(city)) {
						String cityArray[] = city.split(",");
						List<String> cityNameList = new ArrayList<String>();
						for (int k = 0; k < cityArray.length; k++) {
							String code = cityCodeToCityName.get(cityArray[k]);
							cityNameList.add(code);
						}
						if (cityNameList.size() > 0) {
							cityName = StringUtils.join(cityNameList.toArray(), ",");
						}
					}

					// obj
					// .put("wordpatcount", rs.getRows()[i]
					// .get("wordpatcount"));
					// obj.put("relatequerycount", rs.getRows()[i]
					// .get("relatequerycount"));
					// obj.put("answercount",
					// rs.getRows()[i].get("answercount"));
					obj.put("cityname", cityName);
					obj.put("citycode", city);
					obj.put("status", rs.getRows()[i].get("status"));
					obj.put("result", rs.getRows()[i].get("result"));
					obj.put("istrain", rs.getRows()[i].get("istrain"));
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
	 * @description 获取标准问题列表信息
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @param page
	 * @param rows
	 * @return
	 * @returnType Object
	 */
	public static Object selectNormalQuery(String serviceid, String normalQuery, String responseType,
			String interactType, int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		interactType = getInteractType(interactType);
		int count = CommonLibQueryManageDAO.getNormalQueryCount(serviceid, normalQuery, responseType, interactType);
		if (count > 0) {
			Result rs = CommonLibServiceDAO.getServiceInfoByserviceid(serviceid);
			String brand = "";
			if (rs != null && rs.getRowCount() > 0) {
				brand = rs.getRows()[0].get("brand") == null ? "" : rs.getRows()[0].get("brand").toString();
			}
			jsonObj.put("total", count);
			if ("个性化业务".equals(brand)) {
				rs = CommonLibQueryManageDAO.selectNormalQueryByIoa(serviceid, servicetype, normalQuery, responseType,
						interactType, page, rows);
			} else {
				rs = CommonLibQueryManageDAO.selectNormalQuery(serviceid, normalQuery, responseType, interactType, page,
						rows);
			}

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
					obj.put("responsetype", rs.getRows()[i].get("responsetype"));
					obj.put("interacttype",
							getInteractType(ObjectUtils.toString(rs.getRows()[i].get("interacttype")), false));
					obj.put("customerquery", rs.getRows()[i].get("query"));
					String city = rs.getRows()[i].get("city") != null ? rs.getRows()[i].get("city").toString() : "";
					String cityName = "";
					if (!"".equals(city)) {
						String cityArray[] = city.split(",");
						List<String> cityNameList = new ArrayList<String>();
						for (int k = 0; k < cityArray.length; k++) {
							String code = cityCodeToCityName.get(cityArray[k]);
							cityNameList.add(code);
						}
						if (cityNameList.size() > 0) {
							cityName = StringUtils.join(cityNameList.toArray(), ",");
						}
					}

					obj.put("wordpatcount", rs.getRows()[i].get("wordpatcount"));
					obj.put("relatequerycount", rs.getRows()[i].get("relatequerycount"));
					obj.put("answercount", rs.getRows()[i].get("answercount"));
					// 解决：问题库摘要和答案标签内的数据个数不一致
					// String kbdataid =
					// Objects.toString(rs.getRows()[i].get("kbdataid"),"0");
					// int answercount =
					// CommonLibFaqDAO.getAnswerCountNew(servicetype, kbdataid);
					// obj.put("answercount", answercount);

					obj.put("extendcount", rs.getRows()[i].get("extendcount"));
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
	 * @description 获取标准问题列表信息
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @param page
	 * @param rows
	 * @return
	 * @returnType Object
	 */
	public static Object selectNormalQuery_New(String serviceid, String normalQuery, String responseType,
			String interactType, int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		interactType = getInteractType(interactType);
		int count = CommonLibQueryManageDAO.getNormalQueryCount_New(serviceid, normalQuery, responseType, interactType);
		if (count > 0) {
			Result rs = CommonLibServiceDAO.getServiceInfoByserviceid(serviceid);
			String brand = "";
			String service = "";
			if (rs != null && rs.getRowCount() > 0) {
				brand = Objects.toString(rs.getRows()[0].get("brand"), "");
				service = Objects.toString(rs.getRows()[0].get("service"), "");
			}
			jsonObj.put("total", count);
			rs = CommonLibQueryManageDAO.selectNormalQuery_New(serviceid, normalQuery, responseType, interactType, page,
					rows);
			Map<String, JSONObject> kbdataMap = new HashMap<String, JSONObject>();
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject obj = new JSONObject();
					String kbdataId = Objects.toString(rs.getRows()[i].get("kbdataid"), "");
					obj.put("queryid", rs.getRows()[i].get("id"));// 无效数据
					obj.put("service", service);
					obj.put("brand", brand);
					obj.put("kbdataid", kbdataId);
					String abs = rs.getRows()[i].get("abstract").toString();
					obj.put("normalquery", abs.split(">")[1]);
					obj.put("abs", abs);
					obj.put("topic", rs.getRows()[i].get("topic"));
					obj.put("abscity", rs.getRows()[i].get("abscity"));
					obj.put("responsetype", rs.getRows()[i].get("responsetype"));
					obj.put("interacttype",
							getInteractType(ObjectUtils.toString(rs.getRows()[i].get("interacttype")), false));
					obj.put("customerquery", rs.getRows()[i].get("query"));// 无效数据
					String city = rs.getRows()[i].get("city") != null ? rs.getRows()[i].get("city").toString() : "";
					String cityName = "";
					if (!"".equals(city)) {
						String cityArray[] = city.split(",");
						List<String> cityNameList = new ArrayList<String>();
						for (int k = 0; k < cityArray.length; k++) {
							String code = cityCodeToCityName.get(cityArray[k]);
							cityNameList.add(code);
						}
						if (cityNameList.size() > 0) {
							cityName = StringUtils.join(cityNameList.toArray(), ",");
						}
					}
					int answercount = CommonLibFaqDAO.getAnswerCountNew(servicetype, kbdataId);
					obj.put("answercount", answercount);
					// 临时占位数据
					obj.put("wordpatcount", "0");
					obj.put("relatequerycount", "0");
					obj.put("extendcount", "0");
					obj.put("cityname", cityName);
					obj.put("citycode", city);

					kbdataMap.put(kbdataId, obj);
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			List<String> kbdataIdList = new ArrayList<String>();
			kbdataIdList.addAll(kbdataMap.keySet());
			// 获取定制化信息
			JSONObject object = QuerymanageDAO.findConfigure();
			List<String> item = (List<String>) object.get("customItem");
			boolean showAuto = false;
			for (String str : item) {
				if (str.equals("auto词模=显示")) {
					showAuto = true;
				}
			}

			rs = CommonLibQueryManageDAO.getWordpatCountByKbdataId(kbdataIdList, showAuto);
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String kbdataId = Objects.toString(rs.getRows()[i].get("kbdataid"), "");
					String wordpatcount = Objects.toString(rs.getRows()[i].get("count"), "0");
					JSONObject obj = kbdataMap.get(kbdataId);
					if (obj != null) {
						obj.put("wordpatcount", wordpatcount);
					}
				}
			}
			rs = CommonLibQueryManageDAO.getRelatequeryCountByKbdataId(kbdataIdList);
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String kbdataId = Objects.toString(rs.getRows()[i].get("kbdataid"), "");
					String relatequerycount = Objects.toString(rs.getRows()[i].get("count"), "0");
					JSONObject obj = kbdataMap.get(kbdataId);
					if (obj != null) {
						obj.put("relatequerycount", relatequerycount);
					}
				}
			}
			rs = CommonLibQueryManageDAO.getExtendCountByKbdataId(kbdataIdList);
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String kbdataId = Objects.toString(rs.getRows()[i].get("kbdataid"), "");
					String extendcount = Objects.toString(rs.getRows()[i].get("count"), "0");
					JSONObject obj = kbdataMap.get(kbdataId);
					if (obj != null) {
						obj.put("extendcount", extendcount);
					}
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
	 * @description 全量生成词模
	 * @param serviceid
	 * @param request
	 * @return
	 * @returnType Object
	 */
	public static Object produceAllWordpat(String serviceid, HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		List<List<String>> list = new ArrayList<List<String>>();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		List<String> combitionArray = getAllQuery(serviceid,0);
		String url = "";
		String provinceCode = "全国";
		int filterCount = 0;// 过滤的自学习词模数量
		// 获取摘要
		List<String> kbIdList = new ArrayList<String>();
		for (int i = 0; i < combitionArray.size(); i++) {
			String queryArray[] = combitionArray.get(i).split("@#@");
			kbIdList.add(queryArray[2]);
		}

		// 生成客户问和对应的自学习词模 <客户问，词模>
		Map<String, String> wordpatMap = getAutoWordpatMap(kbIdList);
		for (int i = 0; i < combitionArray.size(); i++) {
			String queryArray[] = combitionArray.get(i).split("@#@");
			String queryCityCode = queryArray[0];
			if ("全国".equals(queryCityCode) || "".equals(queryCityCode) || queryCityCode == null) {
				queryCityCode = "全国";
				url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
			} else {
				queryCityCode = queryCityCode.replace(",", "|");
				provinceCode = queryCityCode.split("\\|")[0];
				provinceCode = provinceCode.substring(0, 2) + "0000";
				if ("010000".equals(provinceCode) || "000000".equals(provinceCode)) {// 如何为集团、电渠编码
																						// 去默认url
					url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
				} else {
					url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvinceCode(provinceCode);
				}
			}
			// url = "http://180.153.59.28:8082/NLPWebService/NLPCallerWS?wsdl";

			// 获取高级分析的接口串中的serviceInfo
			String serviceInfo = MyUtil.getServiceInfo(servicetype, "问题生成词模", "", false, queryCityCode);
			String query = queryArray[1];
			String kbdataid = queryArray[2];
			String queryid = queryArray[3];
			// 获取高级分析的串
			String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模", queryArray[1], servicetype, serviceInfo);
			logger.info("问题库自学习词模调用【" + GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode) + "】接口地址：" + url);
			logger.info("问题库自学习词模接口的输入串：" + queryObject);
			// 调用生成词模的接口生成词模,可能是多个，以@_@分隔
			String wordpat = getWordpat(queryObject, url);
			if (wordpat != null && !"".equals(wordpat)) {
				// 判断词模是否含有@_@
				if (wordpat.contains("@_@")) {
					// 有的话，按照@_@进行拆分,并只取第一个
					wordpat = wordpat.split("@_@")[0];
				}
				// 获取词模中@前面的词模题，在加上@2#编者="问题库"&来源="(当前问题)"
				// wordpat = wordpat.split("@")[0] + "@2#编者=\"问题库\"&来源=\""
				// + query.replace("&", "\\and") + "\"";
				// 保留自学习词模返回值，并替换 编者=\"自学习\""=>编者="问题库"&来源="(当前问题)" ---> modify
				// 2017-05-24
				wordpat = wordpat.replace("编者=\"自学习\"", "编者=\"问题库\"&来源=\"" + query.replace("&", "\\and") + "\"");

				// 校验自动生成的词模是否符合规范
				if (Check.CheckWordpat(wordpat, request)) {
					// 获取客户问对应的旧词模
					String oldWordpat = wordpatMap.get(query);
					// 存在旧词模
					if (oldWordpat != null && !"".equals(oldWordpat)) {
						String newWordpat = wordpat.split("@2#")[0];
						logger.info("新旧词模比较 ----新词模：\"" + newWordpat + "\"，旧词模：\"" + oldWordpat + "\"，针对问题：\"" + query
								+ "\"");
						// 新旧词模不相同，执行插入
						if (!oldWordpat.equals(newWordpat)) {
							List<String> tempList = new ArrayList<String>();
							tempList.add(wordpat);
							tempList.add(queryCityCode);
							tempList.add(query);
							tempList.add(kbdataid);
							tempList.add(queryid);
							list.add(tempList);
						} else {// 新旧词模相同，不进行插入操作
							filterCount++;// 记录过滤的词模数量
						}
					} else {// 不存在旧词模
						List<String> tempList = new ArrayList<String>();
						tempList.add(wordpat);
						tempList.add(queryCityCode);
						tempList.add(query);
						tempList.add(kbdataid);
						tempList.add(queryid);
						list.add(tempList);
					}
				}
			}
		}
		logger.info("全量训练----客户问个数：" + combitionArray.size() + "，插入词模个数：" + list.size() + "，过滤词模的个数：" + filterCount);

		// 插入问题库自动学习词模
		int count = -1;
		if (list.size() > 0) {
			count = CommonLibQueryManageDAO.insertWordpat(list, servicetype, userid, null);
			if (count > 0) {
				jsonObj.put("success", true);
				jsonObj.put("msg", "生成成功!");
			} else {
				jsonObj.put("success", false);
				jsonObj.put("msg", "生成失败!");
			}
		} else if (combitionArray.size() >= list.size() + filterCount && list.size() + filterCount > 0) {// 有成功插入的词模就算生成成功？
			jsonObj.put("success", true);
			jsonObj.put("msg", "生成成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "生成失败!!");
		}

		return jsonObj;

	}

	/**
	 * @description 获得业务下所有问题
	 * @param serviceid
	 * @return
	 * @returnType List<String>
	 */
	public static List<String> getAllQuery(String serviceid,int querytype) {
		List<String> list = new ArrayList<String>();
		Result rs = CommonLibQueryManageDAO.getQuery(serviceid,querytype);
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				String query = rs.getRows()[i].get("query") != null ? rs.getRows()[i].get("query").toString() : "";
				if ("".equals(query)) {
					continue;
				}
				String city = rs.getRows()[i].get("city") != null ? rs.getRows()[i].get("city").toString() : "";
				String kbdataid = rs.getRows()[i].get("kbdataid").toString();
				String queryid = rs.getRows()[i].get("id").toString();
				String content = city + "@#@" + query + "@#@" + kbdataid + "@#@" + queryid;
				list.add(content);
			}
		}
		return list;
	}

	/**
	 * @description 生成词模
	 * @param combition
	 * @param request
	 * @return
	 * @returnType Object
	 */
	public static Object produceWordpat(String combition, HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		List<List<String>> list = new ArrayList<List<String>>();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		String combitionArray[] = combition.split("@@");
		String url = "";
		String provinceCode = "全国";
		int filterCount = 0;// 过滤的自学习词模数量
		// 获取摘要
		List<String> kbIdList = new ArrayList<String>();
		for (int i = 0; i < combitionArray.length; i++) {
			String queryArray[] = combitionArray[i].split("@#@");
			kbIdList.add(queryArray[2]);
		}

		// 生成客户问和对应的自学习词模 <客户问，词模>
		Map<String, String> wordpatMap = getAutoWordpatMap(kbIdList);

		for (int i = 0; i < combitionArray.length; i++) {
			String queryArray[] = combitionArray[i].split("@#@");
			String queryCityCode = queryArray[0];
			if ("全国".equals(queryCityCode) || "".equals(queryCityCode) || queryCityCode == null) {
				queryCityCode = "全国";
				url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
			} else {
				queryCityCode = queryCityCode.replace(",", "|");
				provinceCode = queryCityCode.split("\\|")[0];
				provinceCode = provinceCode.substring(0, 2) + "0000";
				if ("010000".equals(provinceCode) || "000000".equals(provinceCode)) {// 如何为集团、电渠编码
																						// 去默认url
					url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
				} else {
					url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvinceCode(provinceCode);
				}
				// url =
				// "http://222.186.101.212:8282/NLPWebService/NLPCallerWS?wsdl";//ghj
				// Update
				// url = getConfigValue.gaoxidizhi;
			}

			// url = "http://180.153.59.28:8082/NLPWebService/NLPCallerWS?wsdl";
			// url =
			// "http://180.153.63.100:8082/NLPWebService/NLPCallerWS?wsdl";
			// 获取高级分析的接口串中的serviceInfo
			String serviceInfo = MyUtil.getServiceInfo(servicetype, "问题生成词模", "", false, queryCityCode);
			String query = queryArray[1];
			String kbdataid = queryArray[2];
			String queryid = queryArray[3];
			// 获取高级分析的串
			String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模", queryArray[1], servicetype, serviceInfo);
			logger.info("问题库自学习词模调用【" + GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode) + "】接口地址：" + url);
			logger.info("问题库自学习词模接口的输入串：" + queryObject);
			// 调用生成词模的接口生成词模,可能是多个，以@_@分隔
			String wordpat = getWordpat(queryObject, url);
			// logger.info("问题库自学习词模：" + wordpat);
			if (wordpat != null && !"".equals(wordpat)) {
				// 判断词模是否含有@_@
				if (wordpat.contains("@_@")) {
					// 有的话，按照@_@进行拆分,并只取第一个
					wordpat = wordpat.split("@_@")[0];
				}
				// 获取词模中@前面的词模题，在加上@2#编者="问题库"&来源="(当前问题)"
				// wordpat = wordpat.split("@")[0] + "@2#编者=\"问题库\"&来源=\""
				// + query.replace("&", "\\and") + "\"";

				// 保留自学习词模返回值，并替换 编者=\"自学习\""=>编者="问题库"&来源="(当前问题)" ---> modify
				// 2017-05-24
				wordpat = wordpat.replace("编者=\"自学习\"", "编者=\"问题库\"&来源=\"" + query.replace("&", "\\and") + "\"");

				// 校验自动生成的词模是否符合规范
				if (Check.CheckWordpat(wordpat, request)) {
					// 获取客户问对应的旧词模
					String oldWordpat = wordpatMap.get(query);
					// 存在旧词模
					if (oldWordpat != null && !"".equals(oldWordpat)) {
						String newWordpat = wordpat.split("@2#")[0];
						logger.info("新旧词模比较 ----新词模：\"" + newWordpat + "\"，旧词模：\"" + oldWordpat + "\"，针对问题：\"" + query
								+ "\"");
						// 新旧词模不相同，执行插入
						if (!oldWordpat.equals(newWordpat)) {
							List<String> tempList = new ArrayList<String>();
							tempList.add(wordpat);
							tempList.add(queryCityCode);
							tempList.add(query);
							tempList.add(kbdataid);
							tempList.add(queryid);
							list.add(tempList);
						} else {// 新旧词模相同，不进行插入操作
							filterCount++;// 记录过滤的词模数量
						}
					} else {// 不存在旧词模
						List<String> tempList = new ArrayList<String>();
						tempList.add(wordpat);
						tempList.add(queryCityCode);
						tempList.add(query);
						tempList.add(kbdataid);
						tempList.add(queryid);
						list.add(tempList);
					}
				}
			}
		}

		logger.info("批量训练----客户问个数：" + combitionArray.length + "，插入词模个数：" + list.size() + "，过滤词模的个数：" + filterCount);
		// 插入问题库自动学习词模
		int count = -1;
		if (list.size() > 0) {
			count = CommonLibQueryManageDAO.insertWordpat(list, servicetype, userid, null);
			if (count > 0) {
				jsonObj.put("success", true);
				jsonObj.put("msg", "生成成功!");
			} else {
				jsonObj.put("success", false);
				jsonObj.put("msg", "生成失败!");
			}
		} else if (combitionArray.length >= list.size() + filterCount && list.size() + filterCount > 0) {// 有成功处理的词模就算生成成功
			jsonObj.put("success", true);
			jsonObj.put("msg", "生成成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "生成失败!!");
		}

		return jsonObj;

	}

	/**
	 * 生成词模
	 * 
	 * @param 参数调用接口的入参
	 * @return 词模
	 */
	public static String getWordpat(String queryObject, String url) {
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient.NLPCaller4WSClient(url);
		// 判断接口是否连接是否为null
		if (NLPCaller4WSClient == null) {
			// 返回的词模为空
			return "";
		}
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
	 * 生成词模
	 * 
	 * @param 参数调用接口的入参
	 * @return 词模
	 */
	public static String getWordpat(String queryObject) {
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient.NLPCaller4WSClient();
		// 判断接口是否连接是否为null
		if (NLPCaller4WSClient == null) {
			// 返回的词模为空
			return "";
		}
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
	 * @description 更新问题
	 * @param service
	 * @param normalquery
	 * @param oldnormalquery
	 * @param responsetype
	 * @param interacttype
	 * @param kbdataid
	 * @param queryid
	 * @param oldCityCode
	 * @param cityCode
	 * @param oldCustomerQuery
	 * @param customerQuery
	 * @param request
	 * @return
	 * @returnType Object
	 */
	public static Object updateQuery(String service, String normalquery, String oldnormalquery, String responsetype,
			String interacttype, String kbdataid, String queryid, String oldCityCode, String cityCode,
			String oldCustomerQuery, String customerQuery, HttpServletRequest request) {
		int rs = -1;
		JSONObject jsonObj = new JSONObject();
		oldCityCode = oldCityCode.replace("@@", "");
		cityCode = cityCode.replace("@@", "");
		responsetype = responsetype.replace("@@", "");
		interacttype = interacttype.replace("@@", "");
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		List<String> cityList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(),
				"querymanage", "S");
		cityList = resourseMap.get("地市");
		String userCityCode = "";
		if (cityList.size() > 0) {
			userCityCode = StringUtils.join(cityList.toArray(), ",");
		}
		if ("".equals(queryid) || queryid == null) {// 如果客户问题ID为空，新增客户问题
			CommonLibQueryManageDAO.addCustomerQuery(kbdataid, customerQuery, cityCode, user);
		}

		// 获得客户问题词模
		String customerQueryWordpat = "";
		if (!"".equals(customerQuery) && customerQuery != null) {
			customerQueryWordpat = getAutoWordpat(customerQuery, servicetype, cityCode, request);
		}
		// 获得标准问题词模 ==> 修改标准问不更新词模
		// String normalqueryWordpat = getAutoWordpat(normalquery, servicetype,
		// cityCode,
		// request);
		String normalqueryWordpat = null;
		rs = CommonLibQueryManageDAO._updateQuery(userCityCode, service, normalquery, normalqueryWordpat,
				oldnormalquery, responsetype, interacttype, kbdataid, queryid, oldCustomerQuery, customerQuery,
				customerQueryWordpat, cityCode, servicetype, user);
		if (rs > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "更新成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "更新失败!");
		}
		return jsonObj;
	}

	/**
	 * @description 获得问题库自学习词模
	 * @param query
	 * @param servicetype
	 * @param request
	 * @return
	 * @returnType String
	 */
	public static String getAutoWordpat(String query, String servicetype, String cityCode, HttpServletRequest request) {
		String url = "";
		if ("".equals(cityCode) || cityCode == null || "全国".equals(cityCode)) {
			url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
		} else {
			cityCode = cityCode.split(",")[0];
			cityCode = cityCode.substring(0, 2) + "0000";
			if ("010000".equals(cityCode) || "000000".equals(cityCode)) {// 如何为集团、电渠编码
																			// 去默认url
				url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
			} else {
				url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvinceCode(cityCode);
			}
		}
		String wordpat = "";
		// 获取高级分析的接口串中的serviceInfo
		String serviceInfo = MyUtil.getServiceInfo(servicetype, "问题生成词模", "", false, cityCode);
		// 获取高级分析的串
		String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模", query, servicetype, serviceInfo);
		// 调用生成词模的接口生成词模,可能是多个，以@_@分隔
		wordpat = getWordpat(queryObject, url);
		if (wordpat != null && !"".equals(wordpat)) {
			// 判断词模是否含有@_@
			if (wordpat.contains("@_@")) {
				// 有的话，按照@_@进行拆分,并只取第一个
				wordpat = wordpat.split("@_@")[0];
			}
			// 获取词模中@前面的词模题，在加上@2#编者="问题库"&来源="(当前问题)"
			wordpat = wordpat.split("@")[0] + "@2#编者=\"问题库\"&来源=\"" + query.replace("&", "\\and") + "\"";

			// 校验自动生成的词模是否符合规范
			if (Check.CheckWordpat(wordpat, request)) {
				return wordpat;
			}
		}
		return "";
	}

	/**
	 * @description 删除客户问题
	 * @param combition
	 * @return
	 * @returnType Object
	 */
	public static Object deleteCustomerQuery(String combition) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		JSONObject jsonObj = new JSONObject();
		List<List<String>> list = new ArrayList<List<String>>();
		String combitionArray[] = combition.split("@@");
		for (int i = 0; i < combitionArray.length; i++) {
			String queryArray[] = combitionArray[i].split("@#@");
			String queryid = queryArray[0];
			String query = queryArray[1];
			String kbdataid = queryArray[2];
			List<String> tempList = new ArrayList<String>();
			tempList.add(queryid);
			tempList.add(query);
			tempList.add(kbdataid);
			list.add(tempList);
		}
		int rs = CommonLibQueryManageDAO._deleteCustomerQuery(list, user);
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
	 * @description 删除标准问题
	 * @param kbdataid
	 * @return
	 * @returnType Object
	 */
	public static Object deleteNormalQuery(String kbdataid) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		JSONObject jsonObj = new JSONObject();
		List<String> list = new ArrayList<String>();
		kbdataid = kbdataid.replace("@@", "");
		String kbdataids[] = kbdataid.split(",");
		for (int i = 0; i < kbdataids.length; i++) {
			if ("".equals(kbdataids[i])) {
				continue;
			}
			list.add(kbdataids[i]);
		}
		int rs = CommonLibQueryManageDAO._deleteNormalQuery(list, user);
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
		String serviceType = user.getIndustryOrganizationApplication();
		// String brand = serviceType.split("->")[1] + "问题库";
		List<String> userCityList = new ArrayList<String>();
		// 业务地市
		List<String> serviceCityList = new ArrayList<String>();
		// Result rs = CommonLibQueryManageDAO.getServiceCitys(serviceid,brand);
		Result rs = CommonLibQueryManageDAO.getServiceCitys(serviceid);
		if (rs != null && rs.getRowCount() > 0) {
			String city = rs.getRows()[0].get("city").toString();
			serviceCityList = Arrays.asList(city.split(","));
		}

		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(),
				"querymanage", "S");
		userCityList = resourseMap.get("地市");
		String userCityCode = "";
		if (userCityList.size() > 0) {
			userCityCode = StringUtils.join(userCityList.toArray(), ",");
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将内容为空放入jsonObj的msg对象中
			jsonObj.put("msg", "导入失败!");
			return jsonObj;
		}
		String workerid = user.getUserID();
		// 获得商家标识符
		String bussinessFlag = CommonLibMetafieldmappingDAO.getBussinessFlag(serviceType);

		// 获取文件的路径
		String pathName = regressTestPath + File.separator + filename;
		// 获取上传文件的file
		File file = new File(pathName);
		// 获取导入文件的类型
		String extension = filename.lastIndexOf(".") == -1 ? "" : filename.substring(filename.lastIndexOf(".") + 1);
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
			String cloum1 = info.get(0).get(0) + "";
			if (info.size() == 1) {
				if ("标准问题".equals(cloum1)) {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将内容为空放入jsonObj的msg对象中
					jsonObj.put("msg", "文件内容为空!");
					return jsonObj;
				}
			} else {
				if ("标准问题".equals(cloum1)) {// 忽略Excel列名
					info.remove(0);
				}

				Map<ImportNormalqueryBean, Map<String, List<String>>> map = new LinkedHashMap<ImportNormalqueryBean, Map<String, List<String>>>();
				try {
					int result = getImportQueryDic(map, info, serviceCityList);
					jsonObj.put("status", result);
				} catch (Exception e) {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将内容为空放入jsonObj的msg对象中
					jsonObj.put("msg", e.getMessage());
					return jsonObj;
				}
				int count = CommonLibQueryManageDAO.importQuery(map, getQueryDic(serviceid,0), serviceCityList, serviceid,
						bussinessFlag, workerid,0);
				if (count > 0) {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将内容为空放入jsonObj的msg对象中
					jsonObj.put("msg", "导入成功!");
				} else {
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
	 * 导出客户问题
	 * 
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static File exportFile(String serviceid, String normalQuery, String responseType, String interactType) {
		File file = null;
		Result result = CommonLibQueryManageDAO.exportQuery(serviceid, normalQuery, responseType, interactType,0);
		if (result != null && result.getRowCount() > 0) {
			List colTitle = Arrays.asList("标准问题", "客户问题", "回复类型", "交互类型", "来源地市");
			List text = new ArrayList();
			String normalquery = "";
			for (int i = 0; i < result.getRowCount(); i++) {
				List line = new ArrayList();
				normalquery = (result.getRows()[i].get("abstract") == null ? ""
						: result.getRows()[i].get("abstract").toString());
				line.add(StringUtils.substringAfterLast(normalquery, ">"));
				line.add(result.getRows()[i].get("query") == null ? "" : result.getRows()[i].get("query").toString());
				line.add(result.getRows()[i].get("responsetype") == null ? ""
						: result.getRows()[i].get("responsetype").toString());
				line.add(getInteractType(ObjectUtils.toString(result.getRows()[i].get("interacttype")), false));
				line.add(getCityName(
						result.getRows()[i].get("city") == null ? "" : result.getRows()[i].get("city").toString()));
				text.add(line);
			}
			String filename = "customerquery_";
			filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			boolean isWritten = ReadExcel.writeExcel(FILE_PATH_EXPORT, filename, null, null, colTitle, text);
			if (isWritten) {
				file = new File(FILE_PATH_EXPORT + filename + ".xls");
			}
		}
		return file;

	}
	/**
	 * 导出排除问题
	 * 
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static File exportFileRemove(String serviceid, String normalQuery, String responseType, String interactType) {
		File file = null;
		Result result = CommonLibQueryManageDAO.exportQuery(serviceid, normalQuery, responseType, interactType,1);
		if (result != null && result.getRowCount() > 0) {
			List colTitle = Arrays.asList("标准问题", "排除问题", "回复类型", "交互类型", "来源地市");
			List text = new ArrayList();
			String normalquery = "";
			for (int i = 0; i < result.getRowCount(); i++) {
				List line = new ArrayList();
				normalquery = (result.getRows()[i].get("abstract") == null ? ""
						: result.getRows()[i].get("abstract").toString());
				line.add(StringUtils.substringAfterLast(normalquery, ">"));
				line.add(result.getRows()[i].get("query") == null ? "" : result.getRows()[i].get("query").toString());
				line.add(result.getRows()[i].get("responsetype") == null ? ""
						: result.getRows()[i].get("responsetype").toString());
				line.add(getInteractType(ObjectUtils.toString(result.getRows()[i].get("interacttype")), false));
				line.add(getCityName(
						result.getRows()[i].get("city") == null ? "" : result.getRows()[i].get("city").toString()));
				text.add(line);
			}
			String filename = "removequery_";
			filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			boolean isWritten = ReadExcel.writeExcel(FILE_PATH_EXPORT, filename, null, null, colTitle, text);
			if (isWritten) {
				file = new File(FILE_PATH_EXPORT + filename + ".xls");
			}
		}
		return file;

	}

	/**
	 * 导出词模
	 * 
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object exportWordpat(String serviceid, String flag) {
		JSONObject jsonObject = new JSONObject();
		Map<String, String> servicePathMap = getServicePathMap(serviceid, "-");

		Result result = CommonLibQueryManageDAO.exportWordpat(serviceid, flag);
		if (result != null && result.getRowCount() > 0) {
			List<String> colTitle = Arrays.asList("问题库路径", "标准问题", "回复类型", "交互类型", "词模", "词模针对问题", "词模类型", "来源地市",
					"添加时间", "场景所在目录", "场景名称", "场景详情");
			List<List<String>> text = new ArrayList<List<String>>();
			String normalquery = "";
			String wordpat = "";
			String relationserviceid = "";
			Map<String, String> scenariosNameMap = new HashMap<String, String>();
			for (int i = 0; i < result.getRowCount(); i++) {
				List<String> line = new ArrayList<String>();
				line.add(servicePathMap.get(result.getRows()[i].get("serviceid").toString()));// 问题库路径
				normalquery = (result.getRows()[i].get("abstract") == null ? ""
						: result.getRows()[i].get("abstract").toString());
				line.add(StringUtils.substringAfter(normalquery, ">"));// 标准问题
				line.add(ObjectUtils.toString(result.getRows()[i].get("responsetype"), "未知"));// 回复类型
				line.add(getInteractType(ObjectUtils.toString(result.getRows()[i].get("interacttype")), false));// 交互类型
				wordpat = result.getRows()[i].get("wordpat") == null ? ""
						: result.getRows()[i].get("wordpat").toString();
				String simworpat = wordpat == "" ? "" : SimpleString.worpattosimworpat(wordpat);
				line.add(simworpat);// 词模
				String q = SimpleString.getQueryBySimpleWordpat(simworpat);
				line.add(q == null ? "" : q);// 词模针对问题
				String wordpattype = wordpatType.get(result.getRows()[i].get("wordpattype") == null ? ""
						: result.getRows()[i].get("wordpattype").toString());
				line.add(wordpattype == null ? "" : wordpattype);// 词模类型
				line.add(getCityName(
						result.getRows()[i].get("city") == null ? "" : result.getRows()[i].get("city").toString()));// 来源地市
				line.add(result.getRows()[i].get("edittime") == null ? ""
						: result.getRows()[i].get("edittime").toString());// 添加时间
				// Object time = result.getRows()[i].get("edittime");
				// if(time != null){//添加时间
				// line.add(DateFormatUtils.format(((Timestamp) time).getTime(),
				// "yyyy/MM/dd HH:mm"));
				// }else{
				// line.add("");
				// }
				relationserviceid = result.getRows()[i].get("relationserviceid") == null ? ""
						: result.getRows()[i].get("relationserviceid").toString();
				// 查找场景目录
				String scenariosName = "";
				if (relationserviceid.length() > 0 && !servicePathMap.containsKey(relationserviceid)) {
					ArrayList<String> path = CommonLibServiceDAO.getServicePath(relationserviceid);
					path.remove("知识库");// 移除根节点
					if (path.size() > 0)// 防止查不出业务
						scenariosName = path.remove(path.size() - 1);
					String servicePath = StringUtils.join(path.toArray(), "-");
					servicePathMap.put(relationserviceid, servicePath);
					scenariosNameMap.put(relationserviceid, scenariosName);
				}
				line.add(servicePathMap.get(relationserviceid) == null ? "" : servicePathMap.get(relationserviceid));// 场景所在目录
				line.add(
						scenariosNameMap.get(relationserviceid) == null ? "" : scenariosNameMap.get(relationserviceid));// 场景名称
				Object ruleresponse = result.getRows()[i].get("ruleresponse");
				line.add(StringEscapeUtils
						.unescapeHtml(ObjectUtils.toString(MyUtil.oracleClob2Str((Clob) ruleresponse))));// 场景详情
				text.add(line);
			}
			String filename = "wordpat_";
			filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			boolean isWritten = ReadExcel.writeExcel(FILE_PATH_EXPORT, filename, null, null, colTitle, text);
			if (isWritten) {
				// file = new File(FILE_PATH_EXPORT + filename + ".xls");
				jsonObject.put("success", true);
				jsonObject.put("fileName", filename + ".xls");
			} else {
				jsonObject.put("success", false);
				jsonObject.put("msg", "生成文件失败");
			}
		}
		return jsonObject;

	}

	/**
	 * 导出客户问
	 * 
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object createQueryExport(String serviceid, String flag) {
		JSONObject jsonObject = new JSONObject();
		Map<String, String> servicePathMap = getServicePathMap(serviceid, "-");

		Result result = CommonLibQueryManageDAO.exportCustomerQuery(serviceid, flag);
		if (result != null && result.getRowCount() > 0) {
			List<String> colTitle = Arrays.asList("问题库路径", "标准问题", "客户问题", "回复类型", "交互类型", "来源地市", "客户问添加时间", "场景所在目录",
					"场景名称", "场景详情");
			List<List<String>> text = new ArrayList<List<String>>();
			String normalquery = "";
			String wordpat = "";
			String relationserviceid = "";
			Map<String, String> scenariosNameMap = new HashMap<String, String>();
			for (int i = 0; i < result.getRowCount(); i++) {
				List<String> line = new ArrayList<String>();
				line.add(servicePathMap.get(result.getRows()[i].get("serviceid").toString()));// 问题库路径
				normalquery = (result.getRows()[i].get("abstract") == null ? ""
						: result.getRows()[i].get("abstract").toString());
				line.add(StringUtils.substringAfter(normalquery, ">"));// 标准问题
				line.add(result.getRows()[i].get("query") == null ? "" : result.getRows()[i].get("query").toString());// 回复类型
				line.add(ObjectUtils.toString(result.getRows()[i].get("responsetype"), "未知"));// 回复类型
				line.add(getInteractType(ObjectUtils.toString(result.getRows()[i].get("interacttype")), false));// 交互类型
				line.add(getCityName(
						result.getRows()[i].get("city") == null ? "" : result.getRows()[i].get("city").toString()));// 来源地市
				line.add(result.getRows()[i].get("edittime") == null ? ""
						: result.getRows()[i].get("edittime").toString());// 添加时间
				relationserviceid = result.getRows()[i].get("relationserviceid") == null ? ""
						: result.getRows()[i].get("relationserviceid").toString();
				// 查找场景目录
				String scenariosName = "";
				if (relationserviceid.length() > 0 && !servicePathMap.containsKey(relationserviceid)) {
					ArrayList<String> path = CommonLibServiceDAO.getServicePath(relationserviceid);
					path.remove("知识库");// 移除根节点
					if (path.size() > 0)// 防止查不出业务
						scenariosName = path.remove(path.size() - 1);
					String servicePath = StringUtils.join(path.toArray(), "-");
					servicePathMap.put(relationserviceid, servicePath);
					scenariosNameMap.put(relationserviceid, scenariosName);
				}
				line.add(servicePathMap.get(relationserviceid) == null ? "" : servicePathMap.get(relationserviceid));// 场景所在目录
				line.add(
						scenariosNameMap.get(relationserviceid) == null ? "" : scenariosNameMap.get(relationserviceid));// 场景名称
				Object ruleresponse = result.getRows()[i].get("ruleresponse");
				line.add(StringEscapeUtils
						.unescapeHtml(ObjectUtils.toString(MyUtil.oracleClob2Str((Clob) ruleresponse))));// 场景详情
				text.add(line);
			}
			String filename = "customerquery_";
			filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			boolean isWritten = ReadExcel.writeExcel(FILE_PATH_EXPORT, filename, null, null, colTitle, text);
			if (isWritten) {
				// file = new File(FILE_PATH_EXPORT + filename + ".xls");
				jsonObject.put("success", true);
				jsonObject.put("fileName", filename + ".xls");
				return jsonObject;
			}
		}
		jsonObject.put("success", false);
		jsonObject.put("msg", "生成文件失败");
		return jsonObject;

	}

	/**
	 * 导出客户问
	 * 
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object exportWordpatAndQuery(String serviceid, String flag) {
		JSONObject jsonObject = new JSONObject();
		long startTime = System.currentTimeMillis();
		long logTime = startTime;
		Map<String, String> servicePathMap = getServicePathMap(serviceid, "-");
		Map<String, String> scenariosNameMap = new HashMap<String, String>();
		// excel表头数据
		Map<String, List<String>> columnTitleMap = new HashMap<String, List<String>>();
		// excel表数据
		Map<String, List<List<Object>>> textMap = new HashMap<String, List<List<Object>>>();
		Result result = CommonLibQueryManageDAO.exportWordpat(serviceid, flag);
		if (result != null && result.getRowCount() > 0) {
			List<String> colTitle = Arrays.asList("问题库路径", "标准问题", "回复类型", "交互类型", "词模", "词模针对问题", "词模类型", "来源地市",
					"添加时间", "场景所在目录", "场景名称", "场景详情");
			columnTitleMap.put("场景-标准问-词模针对问题", colTitle);
			List<List<Object>> text = new ArrayList<List<Object>>();
			String normalquery = "";
			String wordpat = "";
			String relationserviceid = "";

			for (int i = 0; i < result.getRowCount(); i++) {
				List<Object> line = new ArrayList<Object>();
				line.add(servicePathMap.get(result.getRows()[i].get("serviceid").toString()));// 问题库路径
				normalquery = (result.getRows()[i].get("abstract") == null ? ""
						: result.getRows()[i].get("abstract").toString());
				line.add(StringUtils.substringAfter(normalquery, ">"));// 标准问题
				line.add(ObjectUtils.toString(result.getRows()[i].get("responsetype"), "未知"));// 回复类型
				line.add(getInteractType(ObjectUtils.toString(result.getRows()[i].get("interacttype")), false));// 交互类型
				wordpat = result.getRows()[i].get("wordpat") == null ? ""
						: result.getRows()[i].get("wordpat").toString();
				String simworpat = wordpat == "" ? "" : SimpleString.worpattosimworpat(wordpat);
				line.add(simworpat);// 词模
				String q = SimpleString.getQueryBySimpleWordpat(simworpat);
				line.add(q == null ? "" : q);// 词模针对问题
				String wordpattype = wordpatType.get(result.getRows()[i].get("wordpattype") == null ? ""
						: result.getRows()[i].get("wordpattype").toString());
				line.add(wordpattype == null ? "" : wordpattype);// 词模类型
				line.add(getCityName(
						result.getRows()[i].get("city") == null ? "" : result.getRows()[i].get("city").toString()));// 来源地市
				line.add(result.getRows()[i].get("edittime") == null ? ""
						: result.getRows()[i].get("edittime").toString());// 添加时间
				// Object time = result.getRows()[i].get("edittime");
				// if(time != null){//添加时间
				// line.add(DateFormatUtils.format(((Timestamp) time).getTime(),
				// "yyyy/MM/dd HH:mm"));
				// }else{
				// line.add("");
				// }
				relationserviceid = result.getRows()[i].get("relationserviceid") == null ? ""
						: result.getRows()[i].get("relationserviceid").toString();
				// 查找场景目录
				String scenariosName = "";
				if (relationserviceid.length() > 0 && !servicePathMap.containsKey(relationserviceid)) {
					ArrayList<String> path = CommonLibServiceDAO.getServicePath(relationserviceid);
					path.remove("知识库");// 移除根节点
					if (path.size() > 0)// 防止查不出业务
						scenariosName = path.remove(path.size() - 1);
					String servicePath = StringUtils.join(path.toArray(), "-");
					servicePathMap.put(relationserviceid, servicePath);
					scenariosNameMap.put(relationserviceid, scenariosName);
				}
				line.add(servicePathMap.get(relationserviceid) == null ? "" : servicePathMap.get(relationserviceid));// 场景所在目录
				line.add(
						scenariosNameMap.get(relationserviceid) == null ? "" : scenariosNameMap.get(relationserviceid));// 场景名称
				Object ruleresponse = result.getRows()[i].get("ruleresponse");
				line.add(StringEscapeUtils
						.unescapeHtml(ObjectUtils.toString(MyUtil.oracleClob2Str((Clob) ruleresponse))));// 场景详情
				text.add(line);
			}
			textMap.put("场景-标准问-词模针对问题", text);
			logger.info("词模语义数据总数：" + text.size() + "行");
			logger.info("词模语义数据耗时：" + (System.currentTimeMillis() - logTime) + "ms");
			logTime = System.currentTimeMillis();
		}

		result = CommonLibQueryManageDAO.exportCustomerQuery(serviceid, flag);
		if (result != null && result.getRowCount() > 0) {
			List<String> colTitle = Arrays.asList("问题库路径", "标准问题", "客户问题", "回复类型", "交互类型", "来源地市", "客户问添加时间", "场景所在目录",
					"场景名称", "场景详情");
			columnTitleMap.put("场景-标准问-客户问", colTitle);
			List<List<Object>> text = new ArrayList<List<Object>>();
			String normalquery = "";
			String relationserviceid = "";
			for (int i = 0; i < result.getRowCount(); i++) {
				List<Object> line = new ArrayList<Object>();
				line.add(servicePathMap.get(result.getRows()[i].get("serviceid").toString()));// 问题库路径
				normalquery = (result.getRows()[i].get("abstract") == null ? ""
						: result.getRows()[i].get("abstract").toString());
				line.add(StringUtils.substringAfter(normalquery, ">"));// 标准问题
				line.add(result.getRows()[i].get("query") == null ? "" : result.getRows()[i].get("query").toString());// 回复类型
				line.add(ObjectUtils.toString(result.getRows()[i].get("responsetype"), "未知"));// 回复类型
				line.add(getInteractType(ObjectUtils.toString(result.getRows()[i].get("interacttype")), false));// 交互类型
				line.add(getCityName(
						result.getRows()[i].get("city") == null ? "" : result.getRows()[i].get("city").toString()));// 来源地市
				line.add(result.getRows()[i].get("edittime") == null ? ""
						: result.getRows()[i].get("edittime").toString());// 添加时间
				relationserviceid = result.getRows()[i].get("relationserviceid") == null ? ""
						: result.getRows()[i].get("relationserviceid").toString();
				// 查找场景目录
				String scenariosName = "";
				if (relationserviceid.length() > 0 && !servicePathMap.containsKey(relationserviceid)) {
					ArrayList<String> path = CommonLibServiceDAO.getServicePath(relationserviceid);
					path.remove("知识库");// 移除根节点
					if (path.size() > 0)// 防止查不出业务
						scenariosName = path.remove(path.size() - 1);
					String servicePath = StringUtils.join(path.toArray(), "-");
					servicePathMap.put(relationserviceid, servicePath);
					scenariosNameMap.put(relationserviceid, scenariosName);
				}
				line.add(servicePathMap.get(relationserviceid) == null ? "" : servicePathMap.get(relationserviceid));// 场景所在目录
				line.add(
						scenariosNameMap.get(relationserviceid) == null ? "" : scenariosNameMap.get(relationserviceid));// 场景名称
				Object ruleresponse = result.getRows()[i].get("ruleresponse");
				line.add(StringEscapeUtils
						.unescapeHtml(ObjectUtils.toString(MyUtil.oracleClob2Str((Clob) ruleresponse))));// 场景详情
				text.add(line);
			}
			textMap.put("场景-标准问-客户问", text);
			logger.info("客户问数据总数：" + text.size() + "行");
			logger.info("客户问数据耗时：" + (System.currentTimeMillis() - logTime) + "ms");
			logTime = System.currentTimeMillis();
		}

		String filename = "wordpatandquery_";
		filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
		boolean isWritten = ReadExcel.writeExcelBySheet(FILE_PATH_EXPORT, filename, columnTitleMap, textMap);

		logger.info("生成excel文件耗时：" + (System.currentTimeMillis() - logTime) + "ms");
		logger.info("本次导出请求总耗时：" + (System.currentTimeMillis() - startTime) + "ms");
		if (isWritten) {
			// file = new File(FILE_PATH_EXPORT + filename + ".xls");
			jsonObject.put("success", true);
			jsonObject.put("fileName", filename + ".xls");
			return jsonObject;
		}
		jsonObject.put("success", false);
		jsonObject.put("msg", "生成文件失败");
		return jsonObject;

	}

	/**
	 * 获取地市名集合
	 * 
	 * @param cityCode
	 * @return
	 */
	public static String getCityName(String cityCode) {
		String cityName = null;
		if (StringUtils.isBlank(cityCode)) {
			return "";
		}
		String[] codes = null;
		if (cityCode.contains(",")) {
			codes = cityCode.split(",");
		} else if (cityCode.contains("|")) {
			codes = cityCode.split("\\|");
		} else {
			codes = new String[] { cityCode };
		}
		String tmp = null;
		for (int i = 0; i < codes.length; i++) {
			tmp = QuerymanageDAO.cityCodeToCityName.get(codes[i].trim());
			if (cityName == null) {
				cityName = tmp;
			} else {
				cityName = cityName + "," + tmp;
			}
		}

		return cityName;
	}

	/**
	 * @description 获得导入客户问题字典
	 * @param info
	 * @param serviceCityList
	 * @return
	 * @throws Exception
	 * @returnType Map<String,Map<String,List<String>>>
	 */
	public static int getImportQueryDic(Map<ImportNormalqueryBean, Map<String, List<String>>> map,
			List<List<Object>> info, List<String> serviceCityList) throws Exception {
		// Map<ImportNormalqueryBean, Map<String, List<String>>> map = new
		// LinkedHashMap<ImportNormalqueryBean, Map<String, List<String>>>();
		int s = 0;
		boolean flag1 = false;
		boolean flag2 = false;

		for (int i = 0; i < info.size(); i++) {
			List<String> cityList = new ArrayList<String>();
			String normalquery = info.get(i).get(0) != null ? info.get(i).get(0).toString().replace(" ", "") : "";
			String customerquery = info.get(i).get(1) != null ? info.get(i).get(1).toString().replace(" ", "") : "";
			String responsetype = info.get(i).get(2) != null ? info.get(i).get(2).toString().replace(" ", "") : "";
			String interacttype = info.get(i).get(3) != null ? info.get(i).get(3).toString().replace(" ", "") : "";
			ImportNormalqueryBean normalqueryBean = new ImportNormalqueryBean(normalquery,
					getResponseType(responsetype), getInteractType(interacttype));
			if ("".equals(normalquery)) {// 如果标准问题为空，不做处理
				continue;
			}

			if (normalquery.length() > 50 && !flag1) {
				s += 2;
				flag1 = true;
				continue;
			}

			if (customerquery.length() > 50 && !flag2) {
				s += 1; // 导入的文件中存在超过50字的客户问，不做处理
				flag2 = true;
				continue;
			}
			String city = info.get(i).get(4) != null ? info.get(i).get(4).toString().replace(" ", "").replace("，", ",")
					: "";
			if (!"".equals(city) && city != null) {// 如果客户问题来源地市不为空通过地市名称取地址编码
				city = city.replace("省", "").replace("市", "");
				String cityArray[] = city.split(",");
				for (int m = 0; m < cityArray.length; m++) {
					if (cityNameToCityCode.containsKey(cityArray[m])) {
						String cityCode = getCityCodeFromServiceCityCodes(serviceCityList, cityArray[m]);
						// 扩展问地市来源不在业务地市范围内
						if (StringUtils.isEmpty(cityCode)) {
							throw new Exception("客户问题“" + customerquery + "”地市来源“" + cityArray[m] + "”，不在业务地市范围内！");
						}
						cityList.add(cityCode);
					}
				}
			} else {// 如果客户问题来源地市为空，取当前用户关联地市
				cityList.addAll(serviceCityList);
			}
			Collections.sort(cityList);
			if (map.containsKey(normalqueryBean)) {
				Map<String, List<String>> tempMap = map.get(normalqueryBean);
				if (tempMap.containsKey(customerquery)) {
					List<String> oldCityList = tempMap.get(customerquery);
					oldCityList.addAll(cityList);
					Set set = new HashSet(oldCityList);
					List<String> newCityCodelist = new ArrayList<String>(set);
					Collections.sort(newCityCodelist);
					tempMap.put(customerquery, newCityCodelist);
				} else {
					tempMap.put(customerquery, cityList);
					map.put(normalqueryBean, tempMap);
				}
			} else {
				Map<String, List<String>> queryMap = new HashMap<String, List<String>>();
				queryMap.put(customerquery, cityList);
				map.put(normalqueryBean, queryMap);
			}
		}
		return s;

	}

	/**
	 * 获取回复类型
	 * 
	 * @param inStr
	 * @return
	 */
	public static String getResponseType(String inStr) {
		if (inStr != null && responseTypes.contains(inStr)) {
			return inStr;
		}
		return responseTypes.get(0); // Defauult value.
	}

	/**
	 * 获取交互类型
	 * 
	 * @param inStr
	 * @return
	 */
	public static String getInteractType(String inStr) {
		return getInteractType(inStr, true);
	}

	/**
	 * 获取交互类型 ，该方法兼容旧配置<br>
	 * 新交互配置如下：已交互#1 未交互#0 (展示值，数据库值)<br>
	 * 旧交互配置如下：已交互，未交互（展示值=数据库值）
	 * 
	 * @param inStr
	 * @param flag
	 *            true:将展示值转成数据库值 fasle:将数据库值转成展示值
	 * @return
	 */
	public static String getInteractType(String inStr, boolean flag) {
		if (flag) {// 将展示值转成数据库值
			// 遍历map中是否有对应的映射关系
			for (Map.Entry<String, String> entry : interactTypesMap.entrySet()) {
				if (entry.getValue().equals(inStr)) {
					return entry.getKey();
				}
			}
			// 未配置对应关系在判断是否存在交互类型中(兼容旧项目)
			if (inStr != null && interactTypes.contains(inStr)) {
				return inStr;
			}
			return ""; // 数据库默认值
		} else {// 将数据库值转成展示值
			if (StringUtils.isEmpty(inStr)) {
				return "未交互";// 展示默认值
			}
			// 映射转换
			if (interactTypesMap.containsKey(inStr)) {
				return interactTypesMap.get(inStr);
			}

			// 直接返回数据库值进行展示
			return inStr;
		}

	}

	/**
	 * @description 获得业务下客户问题字典
	 * @param serviceid
	 * @return
	 * @returnType Map<String,Map<String,String>>
	 */
	public static Map<String, Map<String, String>> getQueryDic(String serviceid,int querytype) {
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		Result rs = CommonLibQueryManageDAO.getQuery(serviceid,querytype);
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				Map<String, String> queryMap = new HashMap<String, String>();
				String normalquery = rs.getRows()[i].get("abstract").toString().split(">")[1];
				String query = rs.getRows()[i].get("query") != null ? rs.getRows()[i].get("query").toString() : "";
				String city = rs.getRows()[i].get("city") != null ? rs.getRows()[i].get("city").toString() : "";
				String abscity = rs.getRows()[i].get("abscity") != null ? rs.getRows()[i].get("abscity").toString()
						: "";
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
	 * @description 通过摘要ID 获取场景信息
	 * @param kbdataid
	 * @param key
	 * @return
	 * @returnType Object
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
	 * @description 获取参数配置中菜单地址
	 * @return
	 * @returnType JSONObject
	 */
	public static String getAddress(String key) {
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("菜单地址配置", key);
		String url = "";
		if (rs != null && rs.getRowCount() > 0) {
			url = rs.getRows()[0].get("name").toString();
			if (!url.startsWith("http:")) {
				url = "http://" + getConfigValue.ipAndPort + url;
			}
		}
		logger.info("菜单地址配置->" + key + "->" + url);
		return url;
	}

	/**
	 * @description 获取参数配置中菜单地址
	 * @return
	 * @returnType Object
	 */
	public static Object getAddressNew(String key) {
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("菜单地址配置", key);
		String url = "";
		JSONObject jsonObject = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			url = rs.getRows()[0].get("name").toString();
			if (!url.startsWith("http:")) {
				url = "http://" + getConfigValue.ipAndPort + url;
			}
			jsonObject.put("url", url);
			jsonObject.put("success", true);
		} else {
			jsonObject.put("success", false);
		}
		logger.info("菜单地址配置->" + key + "->" + url);
		return jsonObject;
	}

	/**
	 * 构造业务树
	 * 
	 * @param serviceid
	 * @return
	 * @returnType Object
	 */
	public static Object createTree(String serviceid) {

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		String brand = "'" + serviceType.split("->")[1] + "','" + serviceType.split("->")[1] + "问题库'";
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
				jsonObj.put("text", rs.getRows()[i].get("abstract").toString().split(">")[1]);
				array.add(jsonObj);
			}
		}

		return array;
	}

	/**
	 * @description 新增相关问题
	 * @param relatequerytokbdataid
	 * @param relatequery
	 * @param kbdataid
	 * @return
	 * @returnType Object
	 */
	public static Object insertRelateQuery(String relatequerytokbdataid, String relatequery, String kbdataid) {
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String workerid = user.getUserID();
		int rs = CommonLibQueryManageDAO._insertRelatequery(relatequerytokbdataid, relatequery, kbdataid, workerid);
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
	 * @description 获取相关问题
	 * @param kbdataid
	 * @param relatequery
	 * @param page
	 * @param rows
	 * @return
	 * @returnType Object
	 */
	public static Object selectRelateQuery(String kbdataid, String relatequery, int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int count = CommonLibQueryManageDAO.getRelateQueryCount(kbdataid, relatequery);
		if (count > 0) {
			jsonObj.put("total", count);
			Result rs = CommonLibQueryManageDAO.selectRelateQuery(kbdataid, relatequery, page, rows);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject obj = new JSONObject();
					obj.put("relatequeryid", rs.getRows()[i].get("id"));
					String abs = rs.getRows()[i].get("abs") != null ? rs.getRows()[i].get("abs").toString() : "";
					String relRelatequery = "";
					if (!"".equals(abs)) {
						relRelatequery = abs.split(">")[1];
					}
					obj.put("relatequery", relRelatequery);
					obj.put("kbdataid", rs.getRows()[i].get("kbdataid"));
					obj.put("kbdataid", rs.getRows()[i].get("kbdataid"));
					obj.put("relatequerytokbdataid", rs.getRows()[i].get("relatequerytokbdataid"));
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
	 * @description 删除相关
	 * @param combition
	 * @return
	 * @returnType Object
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
	 * @description 迁移标准问
	 * @param serviceid
	 * @param kbdataid
	 * @param abs
	 * @return
	 * @returnType Object
	 */
	public static Object transferNormalQuery(String serviceid, String[] kbdataids, String[] abses) {
		JSONObject jsonObj = new JSONObject();
		int rs = CommonLibQueryManageDAO.updateNormalQueryPath(serviceid, kbdataids, abses);
		if (rs > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "迁移成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "迁移失败!");
		}
		return jsonObj;
	}

	public static JSONArray getNewResults(JSONArray results, int getResultCount) {
		if (results != null && results.size() > 0) {
			JSONArray newResults = new JSONArray();
			for (int i = 0; i < getResultCount && i < results.size(); i++) {
				if (newResults.isEmpty()) {
					newResults.add(results.getJSONObject(i));
				} else {
					if (newResults.getJSONObject(newResults.size() - 1)
							.getDoubleValue("credit") > results.getJSONObject(i).getDoubleValue("credit") * 2) {
						break;
					} else {
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
	public static Object createInteractiveSceneTree(String scenariosid, String citySelect) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String serviceType = user.getIndustryOrganizationApplication();
		// 定义返回的json串
		JSONArray array = new JSONArray();
		Result rs = CommonLibInteractiveSceneDAO.createInteractiveSceneTreeNew(scenariosid, serviceType, citySelect,
				user.getUserID());

		// 根节点不过滤
		if (StringUtils.isEmpty(scenariosid)) {
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String sid = rs.getRows()[i].get("scenariosid").toString();
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("id", sid);
					jsonObj.put("text", rs.getRows()[i].get("name").toString());
					jsonObj.put("cls", "folder");
					jsonObj.put("leaf", false);
					jsonObj.put("state", "closed");
					array.add(jsonObj);
				}
			}
		} else {
			Set<String> serviceParentLevel3 = getServiceParentLevel3(scenariosid, null);
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String sid = rs.getRows()[i].get("scenariosid").toString();
					JSONObject jsonObj = new JSONObject();
					jsonObj.put("id", sid);
					jsonObj.put("text", rs.getRows()[i].get("name").toString());
					if (!serviceParentLevel3.contains(sid)) {// 如果没有子业务
						jsonObj.put("leaf", true);
					} else {
						jsonObj.put("cls", "folder");
						jsonObj.put("leaf", false);
						jsonObj.put("state", "closed");
					}
					array.add(jsonObj);
				}
			}
		}

		return array;
	}

	/**
	 * 获取场景树
	 * 
	 * @param userId
	 * @return
	 */
	public static Object getInteractiveSceneTree() {
		User user = (User) GetSession.getSessionByKey("accessUser");
		String serviceType = user.getIndustryOrganizationApplication();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(),
				"scenariosrules", "S");
		List<String> cityList = resourseMap.get("地市");
		Object menu = createServiceTree(user.getUserID(), "scenariosrules", serviceType, cityList.get(0));
		return menu;
	}

	public static Object bindNormalQuery2Scenorio(String kbdataid, String scenariosid) {
		User user = (User) GetSession.getSessionByKey("accessUser");
		JSONObject jsonObj = new JSONObject();
		int n = CommonLibInteractiveSceneDAO.insertScenarios2kbdataNew(kbdataid, scenariosid, user);
		jsonObj.put("success", n > 0);
		return jsonObj;
	}

	/**
	 * @description 测试分类问题
	 * @param question
	 *            问题
	 * @param province
	 *            省份
	 * @param city
	 *            地市
	 * @return
	 * @returnType Object
	 */
	public static Object testQuery(String question, String province, String city) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		String serviceType = user.getIndustryOrganizationApplication();
		String array[] = serviceType.split("->");
		serviceType = array[0] + "->" + array[1] + "->" + array[1] + "问题库";
		// String userid ="179";
		// String serviceType="电信行业->电信集团->4G业务客服应用";

		String url = "";
		Result rs = UserOperResource.getConfigValue("简要分析服务地址配置", "本地服务");
		if (rs != null && rs.getRowCount() > 0) {
			// 获取配置表的服务url
			url = rs.getRows()[0].get("name").toString();
		}
		// url =
		// "http://222.186.101.213:8282/NLPAppWS/AnalyzeEnterPort?wsdl";//TODO测试地址
		// url =
		// "http://134.64.22.251:8042/NLPAppWS/AnalyzeEnterPort?wsdl";//TODO测试地址
		if ("".equals(city) || city == null) {
			city = province;
		}
		// 默认渠道Web
		String channel = "Web";
		Object object = kanalyzeResult(userid, serviceType, channel, question, url, province, city, "问题分类测试");
		JSONObject jsonObj = (JSONObject) object;
		String success = jsonObj.get("success").toString();
		if ("false".equals(success)) {
			jsonObj.put("success", false);
			jsonObj.put("msg", "理解失败");
		} else {
			// 获得参数配置中问题自动分类最大结果数，默认值为1
			int getResultCount = 1;
			rs = UserOperResource.getConfigValue("问题库参数配置", "问题自动分类最大结果数");
			if (rs != null && rs.getRowCount() > 0) {
				getResultCount = Integer.parseInt(rs.getRows()[0].get("name").toString());
			}
			// 取接口结果，注：接口返回结果数量集可能<getResultCount
			// 如果r(i).Credit > r(i+1).Credit * 2, 则保留r(1),r(2),...,r(i+1).
			JSONArray results = (JSONArray) jsonObj.get("result");
			if (results != null && results.size() > 0) {
				JSONArray newResults = new JSONArray();
				for (int i = 0; i < getResultCount && i < results.size(); i++) {
					if (newResults.isEmpty()) {
						newResults.add(results.getJSONObject(i));
					} else {
						if (newResults.getJSONObject(newResults.size() - 1)
								.getDoubleValue("credit") > results.getJSONObject(i).getDoubleValue("credit") * 2) {
							break;
						} else {
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
	public static Object kanalyzeResult(String user, String service, String channel, String question, String ip,
			String province, String city, String type) {
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
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient.NLPAppWSClient(ip);
		// 获取调用接口的入参字符串
		String queryObject = getServiceClient.getKAnalyzeQueryObject_new(user, question, service, channel, province,
				city);
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
			result0 = result.split("\\|\\|\\|\\|")[0].replaceAll("(\r\n|\r|\n|\n\r|\t)", "");
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
		if ("接口请求参数不合规范！".equals(result0) || "".equals(result0) || result0 == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将结果为空信息放入jsonObj的result对象中
			jsonObj.put("result", "结果为空");
			// 将结果为空信息放入jsonObj的result1对象中
			jsonObj.put("result1", "结果为空");
			return jsonObj;
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result1) || "".equals(result1) || result1 == null) {
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
				JSONObject kNLPResultsObj = JSONObject.parseObject(kNLPResultsArray.get(i).toString());
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

	/**
	 * 从业务地市集合中获取传入地市的CODE
	 * 
	 * @param serviceCityCodeList
	 * @param city
	 * @return
	 */
	private static String getCityCodeFromServiceCityCodes(List<String> serviceCityCodeList, String city) {
		for (String cityCode : serviceCityCodeList) {
			if ("全国".equals(cityCode)) {
				return cityNameToCityCode.get(city);
			}

			String cityName = cityCodeToCityName.get(cityCode);
			if (StringUtils.isNotEmpty(cityName)) {
				if (cityName.equals(city)) {
					return cityCode;
				}
			}
		}
		return null;
	}

	/**
	 * 导入词类词条到库中
	 * 
	 * @param filename参数文件名称
	 * @param serviceid
	 *            业务ID
	 * @return
	 */
	public static Object importwordxls(String filename) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;

		// 获取文件的路径
		String pathName = regressTestPath + File.separator + filename;
		// 获取上传文件的file
		File file = new File(pathName);
		// 获取导入文件的类型
		String extension = filename.lastIndexOf(".") == -1 ? "" : filename.substring(filename.lastIndexOf(".") + 1);
		// 定义当前文件后得到的集合
		List<List<Object>> info = new ArrayList<List<Object>>();
		if ("xls".equalsIgnoreCase(extension)) {
			// 读取2003版的Excel
			info = MyUtil.read2003Excel(file, 2);
		} else if ("xlsx".equalsIgnoreCase(extension)) {
			// 读取2007版的Excel
			info = MyUtil.read2007Excel(file, 2);
		}
		// 判断文件是否存在
		if (file.exists()) {
			// 删除文件
			file.delete();
		}
		if (info.size() > 0) {
			String cloum1 = info.get(0).get(0) + "";
			if (info.size() == 1 && "业务词".equals(cloum1)) {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "文件内容为空!");
				return jsonObj;
			} else {
				if ("业务词".equals(cloum1)) {// 忽略Excel列名
					info.remove(0);
				}

				String count = CommonLibQueryManageDAO.getWordInsert(user, info);

				if (count.contains("成功")) {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将内容为空放入jsonObj的msg对象中
					jsonObj.put("msg", count);
				} else {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将内容为空放入jsonObj的msg对象中
					jsonObj.put("msg", count);
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
	 * 导入FAQ
	 * 
	 * @param filename
	 * @return
	 */
	public static Object importfaqxls(String filename, String serviceid, String service, HttpServletRequest request) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();

		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;

		// 获取文件的路径
		String pathName = regressTestPath + File.separator + filename;
		// 获取上传文件的file
		File file = new File(pathName);
		// 获取导入文件的类型
		String extension = filename.lastIndexOf(".") == -1 ? "" : filename.substring(filename.lastIndexOf(".") + 1);
		// 定义当前文件后得到的集合
		List<List<Object>> info = new ArrayList<List<Object>>();
		if ("xls".equalsIgnoreCase(extension)) {
			// 读取2003版的Excel
			info = MyUtil.read2003Excel(file, 4);
		} else if ("xlsx".equalsIgnoreCase(extension)) {
			// 读取2007版的Excel
			info = MyUtil.read2007Excel(file, 4);
		}
		// 判断文件是否存在
		if (file.exists()) {
			// 删除文件
			file.delete();
		}
		if (info.size() > 0) {
			String cloum1 = info.get(0).get(0) + "";
			if (info.size() == 1) {
				if ("标准问题".equals(cloum1)) {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将内容为空放入jsonObj的msg对象中
					jsonObj.put("msg", "文件内容为空!");
					return jsonObj;
				}
			} else {
				if ("标准问题".equals(cloum1)) {// 忽略Excel列名
					info.remove(0);
				}
				String returnMsg = getFaqInsert(user, info, serviceid, service, request);
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "导入成功！" + returnMsg);
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
	 * 插入FAQ
	 * 
	 * @param user
	 * @param info
	 * @return
	 */
	public static String getFaqInsert(User user, List<List<Object>> info, String serviceid, String service,
			HttpServletRequest request) {

		// 返回的message
		String returnMsg = "";

		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();

		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();

		// 获取四层结构
		String serviceType = user.getIndustryOrganizationApplication();

		// Map<abstract,kbdataid>
		Map<Object, Object> map = new LinkedHashMap<Object, Object>();
		// Map<kbdataid,city>
		Map<Object, Object> map2 = new LinkedHashMap<Object, Object>();

		// 获得商家标识符
		String bussinessFlag = CommonLibMetafieldmappingDAO.getBussinessFlag(user.getIndustryOrganizationApplication());
		// 获取配置表中的channel
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("渠道参数配置", "渠道");
		// Result rs =
		// CommonLibMetafieldmappingDAO.getConfigValue("问题库FAQ导入渠道配置",
		// serviceType);
		List<String> channelList = new ArrayList<String>();
		if (rs != null && rs.getRowCount() > 0) {
			for (int n = 0; n < rs.getRowCount(); n++) {
				String value = rs.getRows()[n].get("name").toString();
				channelList.add(value);
			}
		} else {
			channelList.add("Web");
		}

		// 问题库FAQ导入配置
		List<String> learnWayList = new ArrayList<String>();
		rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库FAQ导入配置", serviceType);
		if (rs != null && rs.getRowCount() > 0) {
			for (int n = 0; n < rs.getRowCount(); n++) {
				String value = rs.getRows()[n].get("name").toString();
				learnWayList.add(value);
			}
		}

		rs = CommonLibQueryManageDAO.getCityFromService(serviceid);
		String city = "";
		if (rs != null && rs.getRowCount() > 0) {
			city = rs.getRows()[0].get("city") == null ? "全国" : rs.getRows()[0].get("city").toString();
		}

		rs = null;
		rs = CommonLibQueryManageDAO.getInfoFromKbdataByServiceid(serviceid);

		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				map.put(rs.getRows()[i].get("abstract"), rs.getRows()[i].get("kbdataid"));
				map2.put(rs.getRows()[i].get("kbdataid"), rs.getRows()[i].get("city"));
			}
		}

		int count = -1;
		int index = 0;
		// 遍历每一行
		for (List<Object> line : info) {
			index++;
			// 摘要
			String kbdata = line.get(0) == null ? "" : line.get(0).toString().trim();
			// 答案
			String answer = line.get(1) == null ? "" : line.get(1).toString().trim();
			// 渠道
			String channel = line.get(2) == null ? "" : line.get(2).toString().trim();
			// 学习方式
			String learnWay = line.get(3) == null ? "" : line.get(3).toString().trim();
			// 如果词类/词条为空，则略过这一行
			if ("".equals(kbdata) || "".equals(answer) || "".equals(channel)) {
				returnMsg = returnMsg + "<br/>第" + index + "条FAQ存在空白列";
				continue;
			}
			List<String> configValueList = new ArrayList<String>();
			if (channelList.contains(channel)) {
				configValueList.add(channel);
			} else {
				returnMsg = returnMsg + "<br/>第" + index + "条FAQ渠道不合法";
				continue;
			}

			// 标准问不存在
			if (!map.containsKey("<" + service + ">" + kbdata)) {
				String kbdataid = "";
				kbdataid = String.valueOf(ConstructSerialNum.GetOracleNextValNew("SEQ_KBDATA_ID", bussinessFlag));
				String querymanageId = "";
				querymanageId = ConstructSerialNum.GetOracleNextValNew("seq_querymanage_id", bussinessFlag);
				count = CommonLibQueryManageDAO.insertKbdata(user, serviceid, service, kbdataid, kbdata, answer, city,
						configValueList, serviceType, querymanageId);
				if (count > 0) {
					if (learnWay.contains("意图识别") && learnWayList.contains("意图识别")) { // 继承
						Map<String, String> learnWayMap = new HashMap<String, String>();
						// String cityName = "全国";
						String cityName = "";
						if (!"全国".equals(city)) {
							String[] cityArray = city.split(",");
							for (int i = 0; i < cityArray.length; i++) {
								cityName = cityName + cityCodeToCityName.get(cityArray[i]) + ",";
							}
							cityName = cityName.substring(0, cityName.lastIndexOf(","));
						} else {
							cityName = "全国";
						}
						learnWayMap.put(kbdataid + "--" + cityName, "<" + service + ">" + kbdata);
						Object obj = KbdataAttrDAO.inheritAllAbstract(learnWayMap);
					}
					if (learnWay.contains("深度学习") && learnWayList.contains("深度学习")) {
						Object obj = produceWordpat(city + "@#@" + kbdata + "@#@" + kbdataid + "@#@" + querymanageId,
								request);
					}
					if (learnWay.contains("语义文法") && learnWayList.contains("语义文法")) {
						// JSONObject jsonObj = new JSONObject();
						// jsonObj.put("autowordpat", kbdata);
						// jsonObj.put("channel", channel);
						// Object obj =
						// WordpatDAO.AutoGenerateWordpat2(JSONObject.parseObject(jsonObj.toString(),
						// InsertOrUpdateParam.class));
						// JSONObject ReJsonObj = new JSONObject();
						// ReJsonObj = JSONObject.parseObject(obj.toString());
						// String wordpat =
						// ReJsonObj.get("result").toString().split("@_@")[0];
						//// String s_wordpat = "";
						// // 将简单词模转化为普通词模，并返回转换结果
						// s_wordpat =
						// SimpleString.SimpleWordPatToWordPat(wordpat);

						// String[] cityArray=city.split(",");
						// String cityCode = "全国";
						// if(cityArray.length>0){
						// cityCode = cityArray[0];
						// }
						// List<String> list =
						// WordpatDAO.AutoGenerateOrdinaryWordpat(kbdata,cityCode);
						// if(list!=null &list.size()>0){
						// CommonLibQueryManageDAO.insertbyExcel(list.get(0),city.replace(",",
						// "|"),kbdataid,user);
						// }

						List<List<String>> list = new ArrayList<List<String>>();
						String userid = user.getUserID();
						// 获取行业
						String servicetype = user.getIndustryOrganizationApplication();
						String url = "";
						String provinceCode = "全国";

						String queryCityCode = city;
						if ("全国".equals(queryCityCode) || "".equals(queryCityCode) || queryCityCode == null) {
							queryCityCode = "全国";
							url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
						} else {
							queryCityCode = queryCityCode.replace(",", "|");
							provinceCode = queryCityCode.split("\\|")[0];
							provinceCode = provinceCode.substring(0, 2) + "0000";
							if ("010000".equals(provinceCode) || "000000".equals(provinceCode)) {// 如何为集团、电渠编码
																									// 去默认url
								url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
							} else {
								url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvinceCode(provinceCode);
							}
						}

						// 测试使用
						// url =
						// "http://180.153.59.28:8082/NLPWebService/NLPCallerWS?wsdl";

						// 获取高级分析的接口串中的serviceInfo
						String serviceInfo = MyUtil.getServiceInfo(servicetype, "问题生成词模", "", false, queryCityCode);
						String query = kbdata;
						String queryid = querymanageId;
						// 获取高级分析的串
						String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模", kbdata, servicetype, serviceInfo);
						logger.info("问题库自学习词模调用【" + GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode)
								+ "】接口地址：" + url);
						logger.info("问题库自学习词模接口的输入串：" + queryObject);
						// 调用生成词模的接口生成词模,可能是多个，以@_@分隔
						String wordpat = getWordpat(queryObject, url);
						// logger.info("问题库自学习词模：" + wordpat);
						if (wordpat != null && !"".equals(wordpat)) {
							// 判断词模是否含有@_@
							if (wordpat.contains("@_@")) {
								// 有的话，按照@_@进行拆分,并只取第一个
								wordpat = wordpat.split("@_@")[0];
							}
							// 获取词模中@前面的词模题，在加上@2#编者="问题库"&来源="(当前问题)"
							// wordpat = wordpat.split("@")[0] +
							// "@2#编者=\"问题库\"&来源=\""
							// + query.replace("&", "\\and") + "\"";

							// 保留自学习词模返回值，并替换 编者=\"自学习\""=>编者="问题库"&来源="(当前问题)"
							// ---> modify 2017-05-24
							wordpat = wordpat.replace("编者=\"自学习\"",
									"编者=\"问题库\"&来源=\"" + query.replace("&", "\\and") + "\"");

							// 校验自动生成的词模是否符合规范
							if (Check.CheckWordpat(wordpat, request)) {
								List<String> tempList = new ArrayList<String>();
								tempList.add(wordpat);
								tempList.add(queryCityCode);
								tempList.add(query);
								tempList.add(kbdataid);
								tempList.add(queryid);
								list.add(tempList);
							}
						}
						// 插入问题库自动学习词模
						if (list.size() > 0) {
							CommonLibQueryManageDAO.insertWordpat(list, servicetype, userid, "0");
						}

						// Object obj = produceWordpat(city + "@#@" + kbdata +
						// "@#@" + kbdataid + "@#@" + querymanageId,request);

					}
					map.put("<" + service + ">" + kbdata, kbdataid);
					map2.put(kbdataid, city);
				} else {
					returnMsg = returnMsg + "<br/>第" + index + "条FAQ插入失败";
				}
			} else {// 标准问已存在
				city = map2.get(map.get("<" + service + ">" + kbdata)) == null ? "全国"
						: map2.get(map.get("<" + service + ">" + kbdata)).toString();
				String kbdataid = map.get("<" + service + ">" + kbdata).toString();
				count = CommonLibQueryManageDAO.updateKbdataAnswer(user, serviceid, service, kbdata, answer, city,
						configValueList, serviceType, kbdataid);
				if (count < 1) {
					returnMsg = returnMsg + "<br/>第" + index + "条FAQ插入失败";
				}
			}
		}
		ExtendDao.updateKB();
		return returnMsg;
	}

	/**
	 * 查找业务的所有父业务
	 * 
	 * @param serviceId
	 * @return
	 */
	public static Object findServiceParent(String serviceId) {
		Result rs = CommonLibQueryManageDAO.getServicePIdById(serviceId);
		JSONObject object = new JSONObject();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				if (rs.getRows()[i].get("parentId") != null)
					object.put("parent" + i, rs.getRows()[i].get("parentId").toString());
			}
			object.put("total", object.size());
		}
		return object;
	}

	/**
	 * 补全从父业务到子业务的业务树，用于异步的业务树的动态展开操作。
	 * 
	 * @param serviceId
	 * @param parentId
	 * @param citySelect
	 * @return
	 */
	public static Object appendNodeByServiceId(String serviceId, String parentId, String citySelect) {
		Result rs = CommonLibQueryManageDAO.getServicePIdById(serviceId);
		JSONArray result = null;
		Stack<String> stack = new Stack<String>();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				stack.push(rs.getRows()[i].get("parentId").toString());
				if (parentId.equals(rs.getRows()[i].get("parentId").toString())) {
					break;
				}
			}
		}
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String cityCode = "";
		List<String> cityList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(),
				"querymanage", "S");
		// 该操作类型用户能够操作的资源
		cityList = resourseMap.get("地市");
		if (cityList != null && cityList.size() > 0) {
			cityCode = cityList.get(0);
		}
		// 如果不包含全国地市，拼接用户地市
		if (!cityCode.equals("全国")) {
			cityCode = StringUtils.join(cityList, ",");
		}
		// 如果所选地市不为空，使用所选地市
		if (!StringUtils.isEmpty(citySelect)) {
			cityCode = citySelect;
		}
		JSONObject node = null;
		while (!stack.empty()) {

			JSONArray array = (JSONArray) createServiceTreeNew(stack.pop(), cityCode);
			if (result == null) {
				result = array;
			}
			if (node != null) {
				node.put("children", array);
			}
			if (stack.empty())
				break;

			for (int i = 0; i < array.size(); i++) {
				JSONObject object2 = array.getJSONObject(i);
				if (object2.getString("id").equals(stack.peek())) {
					node = object2;
				}
			}
		}

		return result;
	}

	/**
	 * 导出业务树信息
	 * 
	 * @param serviceid
	 * @return
	 */
	public static Object exportService(String serviceid) {
		JSONObject jsonObject = new JSONObject();

		Result rs = CommonLibServiceDAO.getServiceInfoByPid(serviceid);
		Map<String, JSONObject> map = new HashMap<String, JSONObject>();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0, l = rs.getRowCount(); i < l; i++) {
				String sid = rs.getRows()[i].get("serviceid") == null ? ""
						: rs.getRows()[i].get("serviceid").toString();
				String pid = rs.getRows()[i].get("parentid") == null ? "" : rs.getRows()[i].get("parentid").toString();
				String service = rs.getRows()[i].get("service") == null ? ""
						: rs.getRows()[i].get("service").toString();
				String city = rs.getRows()[i].get("city") == null ? "" : rs.getRows()[i].get("city").toString();
				String cityid = rs.getRows()[i].get("cityid") == null ? "" : rs.getRows()[i].get("cityid").toString();
				String brand = rs.getRows()[i].get("brand") == null ? "" : rs.getRows()[i].get("brand").toString();
				JSONObject node = map.get(sid);
				if (node == null) {
					node = new JSONObject();
					map.put(sid, node);
				}
				node.put("service", service);
				node.put("city", city);
				node.put("cityid", cityid);
				node.put("brand", brand);
				JSONObject parent = map.get(pid);
				if (parent == null) {
					parent = new JSONObject();
					map.put(pid, parent);
				}
				if (parent.getJSONArray("children") == null) {
					parent.put("children", new JSONArray());
				}
				parent.getJSONArray("children").add(node);
			}
		}
		JSONArray array = new JSONArray();
		array.add(map.get(serviceid));
		String filename = "service_";
		filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss") + ".json";
		try {
			FileUtils.writeStringToFile(FileUtils.getFile(FILE_PATH_EXPORT, filename), array.toJSONString(), "utf-8");
			jsonObject.put("success", true);
			jsonObject.put("fileName", filename);

		} catch (IOException e) {
			e.printStackTrace();
			jsonObject.put("success", false);
			jsonObject.put("msg", "生成文件失败");
		}
		return jsonObject;
	}

	/**
	 * 导出业务树信息
	 * 
	 * @param serviceid
	 * @return
	 */
	public static Object importService(String filename, String serviceid, HttpServletRequest request) {
		// 定义返回的json串
		JSONObject jsonObject = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		String serviceType = user.getIndustryOrganizationApplication();
		String bussinessFlag = CommonLibMetafieldmappingDAO.getBussinessFlag(serviceType);

		// 获取文件的路径
		String pathName = regressTestPath + File.separator + filename;
		// 获取上传文件的file
		File file = new File(pathName);
		JSONArray array = null;
		try {
			String string = FileUtils.readFileToString(file, "utf-8");
			array = JSONArray.parseArray(string);
		} catch (Exception e) {
			jsonObject.put("success", false);
			jsonObject.put("msg", "读取文件出错！");
			return jsonObject;
		}
		// 判断文件是否存在
		if (file.exists()) {
			// 删除文件
			file.delete();
		}
		if (array.size() > 0) {
			JSONObject root = array.getJSONObject(0);
			JSONArray children = root.getJSONArray("children");
			Stack<JSONObject> nodeStack = new Stack<JSONObject>();

			// 获取根业务信息
			Result rs = CommonLibServiceDAO.getServiceInfoByserviceid(serviceid);
			String rootName = "";
			String brand = "";
			String rootCity = "";
			String rootCityId = "";
			if (rs != null && rs.getRowCount() > 0) {
				rootName = rs.getRows()[0].get("service") == null ? "" : rs.getRows()[0].get("service").toString();
				brand = rs.getRows()[0].get("brand") == null ? "" : rs.getRows()[0].get("brand").toString();
				rootCity = rs.getRows()[0].get("city") == null ? "" : rs.getRows()[0].get("city").toString();
				rootCityId = rs.getRows()[0].get("cityid") == null ? "" : rs.getRows()[0].get("cityid").toString();
			} else {
				jsonObject.put("success", false);
				jsonObject.put("msg", "业务不存在！");
				return jsonObject;
			}

			// 查询父节点下所有子节点名称及编号，用于查重
			rs = CommonLibServiceDAO.getChildServiceByParentID(serviceid);
			Map<String, String> map = new HashMap<String, String>();
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String name = rs.getRows()[i].get("service") == null ? ""
							: rs.getRows()[i].get("service").toString();
					String id = rs.getRows()[i].get("serviceid") == null ? ""
							: rs.getRows()[i].get("serviceid").toString();
					map.put(name, id);
				}
			}

			// 根节点下的子节点先入栈，排除根节点的遍历
			if (children != null && children.size() > 0) {
				for (int i = 0; i < children.size(); i++) {
					// 补充业务父节点信息
					JSONObject object = children.getJSONObject(i);
					object.put("parentId", serviceid);
					object.put("parentName", rootName);
					// 如果重复，记录重复的业务id，并且不进行插入操作
					if (map.containsKey(object.getString("service"))) {
						object.put("repeatid", map.get(object.getString("service")));
					}
					nodeStack.push(object);
				}
			}
			JSONObject node = null;
			// 业务树的遍历
			while (!nodeStack.isEmpty()) {
				node = nodeStack.pop();
				String newServiceId = "";
				String serviceName = node.getString("service");
				Map<String, String> map2 = null;
				// 操作业务节点的代码
				if (!node.containsKey("repeatid")) {
					// 业务父节点信息
					String parentId = node.getString("parentId");
					String parentName = node.getString("parentName");
					// 业务本身信息
					String city = "";
					String cityid = "";
					if (node.containsKey("city") && !StringUtils.isEmpty(node.getString("city"))) {
						city = node.getString("city");
					} else {// city使用根节点city
						city = rootCity;
					}
					if (node.containsKey("cityid") && !StringUtils.isEmpty(node.getString("cityid"))) {
						cityid = node.getString("cityid");
					} else {// city使用根节点city
						cityid = rootCityId;
					}
					logger.info("插入业务节点：" + node);
					newServiceId = CommonLibQueryManageDAO.insertServiceNotLog(parentId, parentName, serviceName, brand,
							bussinessFlag, cityid, city);
					if (StringUtils.isEmpty(newServiceId)) {// 插入失败，跳过
						continue;
					}
				} else {// 重复
					logger.info("业务节点重复：" + node);
					newServiceId = node.getString("repeatid");
					// 如果当前节点重复，则继续进行节点查重
					rs = CommonLibServiceDAO.getChildServiceByParentID(newServiceId);
					map2 = new HashMap<String, String>();
					if (rs != null && rs.getRowCount() > 0) {
						for (int i = 0; i < rs.getRowCount(); i++) {
							String name = rs.getRows()[i].get("service") == null ? ""
									: rs.getRows()[i].get("service").toString();
							String id = rs.getRows()[i].get("serviceid") == null ? ""
									: rs.getRows()[i].get("serviceid").toString();
							map2.put(name, id);
						}
					}
				}

				// 获得节点的子节点，压入栈中继续遍历
				children = node.getJSONArray("children");
				if (children != null && children.size() > 0) {
					for (int i = 0; i < children.size(); i++) {
						JSONObject object = children.getJSONObject(i);
						// 补充子节点的父节点信息
						object.put("parentId", newServiceId);
						object.put("parentName", serviceName);
						// 查重
						if (map2 != null && map2.containsKey(object.getString("service"))) {
							object.put("repeatid", map2.get(object.getString("service")));
						}
						nodeStack.push(object);
					}
				}
			}
		}
		jsonObject.put("success", true);
		jsonObject.put("msg", "导入业务成功！");
		return jsonObject;
	}

	/**
	 * 导出业务树信息
	 * 
	 * @param serviceid
	 * @return
	 */
	public static Object importService(String filename, String serviceid, String serviceType, String bussinessFlag) {
		// 定义返回的json串
		JSONObject jsonObject = new JSONObject();
		// User user = (User) GetSession.getSessionByKey("accessUser");
		// String serviceType = user.getIndustryOrganizationApplication();
		// String bussinessFlag = CommonLibMetafieldmappingDAO
		// .getBussinessFlag(serviceType);

		// 获取文件的路径
		String pathName = regressTestPath + File.separator + filename;
		// 获取上传文件的file
		File file = new File(pathName);
		JSONArray array = null;
		try {
			String string = FileUtils.readFileToString(file, "utf-8");
			array = JSONArray.parseArray(string);
		} catch (Exception e) {
			jsonObject.put("success", false);
			jsonObject.put("msg", "读取文件出错！");
			return jsonObject;
		}
		// 判断文件是否存在
		if (file.exists()) {
			// 删除文件
			file.delete();
		}
		if (array.size() > 0) {
			JSONObject root = array.getJSONObject(0);
			JSONArray children = root.getJSONArray("children");
			Stack<JSONObject> nodeStack = new Stack<JSONObject>();

			// 获取根业务信息
			Result rs = CommonLibServiceDAO.getServiceInfoByserviceid(serviceid);
			String rootName = "";
			String brand = "";
			String rootCity = "";
			String rootCityId = "";
			if (rs != null && rs.getRowCount() > 0) {
				rootName = rs.getRows()[0].get("service") == null ? "" : rs.getRows()[0].get("service").toString();
				brand = rs.getRows()[0].get("brand") == null ? "" : rs.getRows()[0].get("brand").toString();
				rootCity = rs.getRows()[0].get("city") == null ? "" : rs.getRows()[0].get("city").toString();
				rootCityId = rs.getRows()[0].get("cityid") == null ? "" : rs.getRows()[0].get("cityid").toString();
			} else {
				jsonObject.put("success", false);
				jsonObject.put("msg", "业务不存在！");
				return jsonObject;
			}

			// 查询父节点下所有子节点名称及编号，用于查重
			rs = CommonLibServiceDAO.getChildServiceByParentID(serviceid);
			Map<String, String> map = new HashMap<String, String>();
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String name = rs.getRows()[i].get("service") == null ? ""
							: rs.getRows()[i].get("service").toString();
					String id = rs.getRows()[i].get("serviceid") == null ? ""
							: rs.getRows()[i].get("serviceid").toString();
					map.put(name, id);
				}
			}

			// 根节点下的子节点先入栈，排除根节点的遍历
			if (children != null && children.size() > 0) {
				for (int i = 0; i < children.size(); i++) {
					// 补充业务父节点信息
					JSONObject object = children.getJSONObject(i);
					object.put("parentId", serviceid);
					object.put("parentName", rootName);
					// 如果重复，记录重复的业务id，并且不进行插入操作
					if (map.containsKey(object.getString("service"))) {
						object.put("repeatid", map.get(object.getString("service")));
					}
					nodeStack.push(object);
				}
			}
			JSONObject node = null;
			// 业务树的遍历
			while (!nodeStack.isEmpty()) {
				node = nodeStack.pop();
				String newServiceId = "";
				String serviceName = node.getString("service");
				Map<String, String> map2 = null;
				// 操作业务节点的代码
				if (!node.containsKey("repeatid")) {
					// 业务父节点信息
					String parentId = node.getString("parentId");
					String parentName = node.getString("parentName");
					// 业务本身信息
					String city = "";
					String cityid = "";
					if (node.containsKey("city") && !StringUtils.isEmpty(node.getString("city"))) {
						city = node.getString("city");
					} else {// city使用根节点city
						city = rootCity;
					}
					if (node.containsKey("cityid") && !StringUtils.isEmpty(node.getString("cityid"))) {
						cityid = node.getString("cityid");
					} else {// city使用根节点city
						cityid = rootCityId;
					}

					logger.info("插入业务节点：" + node);
					newServiceId = CommonLibQueryManageDAO.insertServiceNotLog(parentId, parentName, serviceName, brand,
							bussinessFlag, cityid, city);
					if (StringUtils.isEmpty(newServiceId)) {// 插入失败，跳过
						continue;
					}
				} else {// 重复
					logger.info("业务节点重复：" + node);
					newServiceId = node.getString("repeatid");
					// 如果当前节点重复，则继续进行节点查重
					rs = CommonLibServiceDAO.getChildServiceByParentID(newServiceId);
					map2 = new HashMap<String, String>();
					if (rs != null && rs.getRowCount() > 0) {
						for (int i = 0; i < rs.getRowCount(); i++) {
							String name = rs.getRows()[i].get("service") == null ? ""
									: rs.getRows()[i].get("service").toString();
							String id = rs.getRows()[i].get("serviceid") == null ? ""
									: rs.getRows()[i].get("serviceid").toString();
							map2.put(name, id);
						}
					}
				}

				// 获得节点的子节点，压入栈中继续遍历
				children = node.getJSONArray("children");
				if (children != null && children.size() > 0) {
					for (int i = 0; i < children.size(); i++) {
						JSONObject object = children.getJSONObject(i);
						// 补充子节点的父节点信息
						object.put("parentId", newServiceId);
						object.put("parentName", serviceName);
						// 查重
						if (map2 != null && map2.containsKey(object.getString("service"))) {
							object.put("repeatid", map2.get(object.getString("service")));
						}
						nodeStack.push(object);
					}
				}
			}
		}
		jsonObject.put("success", true);
		jsonObject.put("msg", "导入业务成功！");
		return jsonObject;
	}

	public static JSONObject findConfigure() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		List<String> item = new ArrayList<String>();
		Set<String> keySet = new HashSet<String>();
		Object sre = GetSession.getSessionByKey("accessUser");
		if (sre == null || "".equals(sre)) {
			jsonObj.put("customItem", item);
			return jsonObj;
		}
		// 从session中获取数据
		Object customItem = GetSession.getSessionByKey("customItem");
		if (customItem != null) {
			jsonObj.put("customItem", customItem);
			return jsonObj;
		}

		User user = (User) sre;
		String customer = "";
		// 获取定制配置
		if ("全行业".equals(user.getCustomer())) {
			customer = "全行业";
		} else {
			customer = user.getIndustryOrganizationApplication();
		}
		String includeCustomer = "";
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库页面定制化配置", customer);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String configStr = rs.getRows()[i].get("name").toString();
				if (configStr.contains("=")) {
					String key = configStr.split("=")[0];
					String value = configStr.split("=")[1];
					// 判断是否有独立商家配置
					if ("include".equals(key) && value.equals(user.getIndustryOrganizationApplication())) {
						includeCustomer = value;
					}
					keySet.add(key);
				}
				item.add(configStr);
			}
		}
		if (!StringUtils.isEmpty(includeCustomer)) {
			// 读取独立商家的导入配置
			rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库页面定制化配置", includeCustomer);
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					String configStr = rs.getRows()[i].get("name").toString();
					if (configStr.contains("=")) {
						String key = configStr.split("=")[0];
						if (keySet.contains(key))
							continue;
					}
					item.add(configStr);
				}
			}
		}
		jsonObj.put("customItem", item);
		return jsonObj;
	}

	/**
	 * 获取自学习词模及对应的客户问
	 * 
	 * @param kbIdList
	 * @return
	 */
	private static Map<String, String> getAutoWordpatMap(List<String> kbIdList) {
		Result rs = CommonLibQueryManageDAO.selectWordpatByKbdataid(kbIdList, "5");
		Map<String, String> wordpatMap = new HashMap<String, String>();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				if (rs.getRows()[i].get("wordpat") != null) {
					// 自学习词模：<翼支付|!翼支付近类>*<是什>*[<么|!没有近类>]@2#编者="问题库"&来源="翼支付是什么？"&最大未匹配字数="1"
					String wordpat = rs.getRows()[i].get("wordpat").toString();
					String[] split = wordpat.split("@2#");
					if (split.length > 1) {
						for (String str : split[1].split("&")) {
							if (str.startsWith("来源=")) {
								String query = str.substring(4, str.length() - 1);
								wordpatMap.put(query, split[0]);
								break;
							}
						}
					}
				}
			}
		}
		return wordpatMap;
	}

	/**
	 * 批量构建service路径的工具函数
	 * 
	 * 返回map为 key:serviceid ,value:业务路径
	 * 
	 * @param id
	 *            父节点路径
	 * @param separator
	 *            分隔符
	 * @return
	 */
	private static Map<String, String> getServicePathMap(String id, String separator) {
		List<String> pathList = CommonLibServiceDAO.getServicePath(id);
		if (pathList.size() < 4) {
			pathList.clear();// 去除知识库和业务根节点
		} else {
			pathList = pathList.subList(2, pathList.size() - 1);
		}
		String prefix = StringUtils.join(pathList, separator);
		if (!StringUtils.isEmpty(prefix)) {
			prefix += separator;
		}
		Result rs = CommonLibServiceDAO.getServiceInfoByPid(id);
		Map<String, JSONObject> serviceMap = new HashMap<String, JSONObject>();
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String name = rs.getRows()[i].get("service") == null ? "" : rs.getRows()[i].get("service").toString();
				String serviceid = rs.getRows()[i].get("serviceid") == null ? ""
						: rs.getRows()[i].get("serviceid").toString();
				String parentid = rs.getRows()[i].get("parentid") == null ? ""
						: rs.getRows()[i].get("parentid").toString();
				if ("0".equals(serviceid) || "0.000".equals(serviceid)) {
					continue;
				}
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("service", name);
				jsonObject.put("parentid", parentid);
				jsonObject.put("serviceid", serviceid);
				serviceMap.put(serviceid, jsonObject);
			}
		}

		Map<String, String> pathMap = new HashMap<String, String>();
		String parentid = null;
		for (Map.Entry<String, JSONObject> entry : serviceMap.entrySet()) {
			String serviceid = entry.getKey();
			parentid = entry.getValue().getString("parentid");
			String path = entry.getValue().getString("service");
			JSONObject jsonObject = serviceMap.get(parentid);
			while (jsonObject != null) {
				parentid = jsonObject.getString("parentid");
				if (!"0".equals(parentid) && !"0.000".equals(parentid)) {
					path = jsonObject.getString("service") + separator + path;
				}
				jsonObject = serviceMap.get(parentid);
			}
			pathMap.put(serviceid, prefix + path);
		}
		return pathMap;
	}

	/**
	 * 获取到km的URL
	 * 
	 * @return
	 */
	public static Object getKMUrl() {
		JSONObject jsonObject = new JSONObject();
		try {
			String url = "http://" + getConfigValue.kmipAndPort;
			jsonObject.put("success", true);
			jsonObject.put("url", url);
		} catch (Throwable e) {
			jsonObject.put("success", false);
		}
		return jsonObject;
	}

	/**
	 * 增加新词，并生成词模
	 * 
	 * @param combition
	 * @param flag
	 * @param normalquery
	 * @param serviceid
	 * @return
	 */
	public static Object addWord(String combition, String flag, String normalquery, String newnormalquery,
			String serviceid, String businesswords, HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		//添加新词
		List<String> newWords = new ArrayList<String>();

		String wordpat = "";
		String lockwordpat = "";
		Map<String, Map<String, String>> map = CommonLibQueryManageDAO.getNormalQueryDic(serviceid);
		Map<String, String> maps = map.get(normalquery.trim());
		String kbdataid = maps.get("kbdataid");
		String city = maps.get("city");

		if (StringUtils.isNotBlank(newnormalquery)) {
			JSONObject jsonObject = AnalyzeDAO.getWordpat2(servicetype, newnormalquery, city);
			wordpat = jsonObject.getString("wordpat");
			wordpat = wordpat.replace("编者=\"自学习\"", "编者=\"问题库\"&来源=\"" + normalquery.replace("&", "\\and") + "\"");
			lockwordpat = jsonObject.getString("lockWordpat");
			lockwordpat = lockwordpat.replace("编者=\"自学习\"",
					"编者=\"问题库\"&来源=\"" + normalquery.replace("&", "\\and") + "\"");
		}

		List<List<Object>> info = new ArrayList<List<Object>>();
		List<Object> list = new ArrayList<Object>();
		if (StringUtils.isNotBlank(combition)) {
			if (combition.contains("#")) {
				String[] wordArray = combition.split("#");
				for (String string : wordArray) {
					list = new ArrayList<Object>();
					list.add(string);
					list.add("");
					info.add(list);
					//判断是否是业务词
					String newword = string;
					if(businesswords.contains(string)){
						newword = newword +",是";
					}else{
						newword = newword +",否";
					}
					newWords.add(newword);
				}
			} else {
				list = new ArrayList<Object>();
				list.add(combition);
				list.add("");
				info.add(list);
				String newword = combition;
				if(businesswords.contains(combition)){
					newword = newword +",是";
				}else{
					newword = newword +",否";
				}
				newWords.add(newword);
			}
			String count = CommonLibQueryManageDAO.insertWordClassAndItem(user, info);
			logger.info("标准问-新增新词【" + JSONObject.toJSONString(info) + "】结果:" + count);
		}
		// 同时根据用户选择的重要和不重要作为可选和必选的判断标准， 然后将新增加的词类用*号和词模1合并

		String[] wordArray = combition.split("#");
		String[] levelArray = flag.split("#");
		String wordClassStr = "";
		for (int i = 0; i < wordArray.length; i++) {
			if ("0".equals(levelArray[i])) {
				wordClassStr += wordArray[i] + "近类" + "*";
			}
			if ("1".equals(levelArray[i])) {
				wordClassStr += "[" + wordArray[i] + "近类" + "]*";
			}

		}
		if (StringUtils.isBlank(wordpat)) {
			wordClassStr = wordClassStr.substring(0, wordClassStr.lastIndexOf("*"));
			wordpat = "#无序#编者=\"问题库\"&来源=\"" + normalquery.replace("&", "\\and") + "\"";
		}
		String simpleWordpat = wordClassStr + wordpat;
		simpleWordpat = SimpleString.SimpleWordPatToWordPat(simpleWordpat);


		wordClassStr = "";
		for (int i = 0; i < wordArray.length; i++) {
			wordClassStr += wordArray[i] + "近类" + "*";
		}

		if (StringUtils.isBlank(lockwordpat)) {
			wordClassStr = wordClassStr.substring(0, wordClassStr.lastIndexOf("*"));
			lockwordpat = "#无序#编者=\"问题库\"&来源=\"" + normalquery.replace("&", "\\and") + "\"&最大未匹配字数=\"0\"&置信度=\"1.1\"";
		}
		String simpleLockWordpat = wordClassStr + lockwordpat;
		simpleLockWordpat = SimpleString.SimpleWordPatToWordPat(simpleLockWordpat);
		//
		Result queryRs = CommonLibQueryManageDAO.getQueryIdByQuery(normalquery, kbdataid);
		String queryid = queryRs.getRows()[0].get("id").toString();
		// 插入问题库自动学习词模
		List<List<String>> combListList = new ArrayList<List<String>>();
		List<String> combList = null;
		if (Check.CheckWordpat(simpleWordpat, request)) {
			combList = new ArrayList<String>();
			combList.add(simpleWordpat);
			combList.add(city);
			combList.add(normalquery);
			combList.add(kbdataid);
			combList.add(queryid);
			combListList.add(combList);
		}
		if (Check.CheckWordpat(simpleLockWordpat, request)) {
			combList = new ArrayList<String>();
			combList.add(simpleLockWordpat);
			combList.add(city);
			combList.add(normalquery);
			combList.add(kbdataid);
			combList.add(queryid);
			combListList.add(combList);
		}

		int count = -1;
		if (list.size() > 0) {
			count = CommonLibQueryManageDAO.insertWordpat(combListList, servicetype, userid, "0");
			if (count > 0) {
				jsonObj.put("success", true);
				jsonObj.put("msg", "生成成功!");
			} else {
				jsonObj.put("success", false);
				jsonObj.put("msg", "生成失败!");
			}
		}
		// 根节点下识别业务规则-业务名称获取-标准问：业务词获取增加业务词词模
		String ktdataid = "";//业务词标准ID
		if(StringUtils.isNotBlank(businesswords)){
			JSONObject objResult = (JSONObject)addBusinessWordpat(businesswords,serviceid,request);
			ktdataid = objResult.getString("ktdataid");
			logger.info("新增业务词词模结果:"+objResult.getString("msg"));
		}
		//新词表添加记录
		if(!CollectionUtils.isEmpty(newWords)){
			String countNewWord = addNewWordInfo(StringUtils.join(newWords,"@@"),ktdataid);
			logger.info("新增新词结果:"+countNewWord);
		}
		
		return jsonObj;
	}

	/**
	 * 增加新词
	 * @param newWords 格式：新词，是否业务词##新词,是否业务词
	 *  @param ktdataid 业务词标准问ID 
	 * @return
	 */
	public static String addNewWordInfo(String combition,String ktdataid){
		String str = "新增成功";
		List<List<String>> list = new ArrayList<List<String>>();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		//业务词词模集合
		Map<String,String> businessWordpatMap = new HashMap<String,String>();
		if(StringUtils.isNotBlank(ktdataid)){//获取标准问下所有的词模
			List<String> kbIdList = new ArrayList<String>();
			kbIdList.add(ktdataid);	
			businessWordpatMap = getBusinessWordpat(kbIdList,"0");
		}
		String[] newWords = combition.split("@@");
		List<String> wordList = null;
		for(int i=0;i<newWords.length;i++){
			wordList = new ArrayList<String>();
			
			String[] newWordInfo = newWords[i].split(",");
			String newWord = newWordInfo[0].toUpperCase();
			String isBusinessWord = newWordInfo[1];
			
			//词类ID
			String wordclassid = getWordClassId(newWord+"近类");
			//词模ID
			String wordpatId = "";
			if("是".equals(isBusinessWord)){
				wordpatId = businessWordpatMap.get(newWord);
			}
			wordList.add(servicetype);
			wordList.add(newWord);
			wordList.add(wordclassid);
			wordList.add(wordpatId);
			wordList.add(isBusinessWord);
			list.add(wordList);			  
		}
		int count = -1;
		if(!CollectionUtils.isEmpty(list)){
			count = CommonLibNewWordInfoDAO.insertNewWordInfo(list, userid);
		}
		if(count < 1){
			str = "新增失败";
		}
				
		return str;
	}
	/**
	 * 获取词类ID
	 * @param wordClass
	 * @return
	 */
	private static String getWordClassId(String wordClass){
		String wordclassid="";
		//查询新词对应的词类ID
		Result rs= CommonLibWordclassDAO.getWordclassID(wordClass);
		if(rs != null && rs.getRowCount() > 0){
			for(int i =0;i<rs.getRowCount();i++){
				wordclassid = rs.getRows()[i].get("wordclassid").toString();
				break;
			}
		}
		return wordclassid;
	}
	/**
	 * 获取业务词对应词模ID
	 * @param kdIdList
	 * @param wordpattype
	 * @return (业务词,词模ID)
	 */
	public static Map<String,String> getBusinessWordpat(List<String> kdIdList,String wordpattype){
		Map<String,String> wordpatMap = new HashMap<String,String>();
		Result rs = CommonLibQueryManageDAO.selectWordpatByKbdataid(kdIdList,wordpattype);
		if(rs != null && rs.getRowCount()> 0){
			for(int i =0;i<rs.getRowCount();i++){
				if(rs.getRows()[i].get("wordpat") != null){
					String wordpat = rs.getRows()[i].get("wordpat").toString();
					String[] split = wordpat.split("业务X=");
					if(split.length > 1){
						  String bussinessword = split[1];
						  wordpatMap.put(bussinessword, rs.getRows()[i].get("wordpatid").toString());
					}
				}
			}
		}
		return wordpatMap;
	}
	/**
	 * 添加业务词词模
	 * @param businesswords
	 * @param serviceid
	 * @param request
	 * @return
	 */
	public static Object addBusinessWordpat(String businesswords, String serviceid,HttpServletRequest request) {
		JSONObject obj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		// 根节点ID
		String rootserviceid = "";
		// 识别规则业务ID
		String ruleserviceid = "";
		// 识别规则业务ID
		String businessserviceid = "";
		// 标准问题ID
		String kbdataid = "";
		// 根据serviceid获取根节点
		Result result = CommonLibQueryManageDAO.getServicePIdById(serviceid);

		if (result != null && result.getRowCount() > 0) {
			for (int i = 0; i < result.getRowCount(); i++) {
				if ("0".equals(result.getRows()[i].get("parentid").toString())) {
					rootserviceid = result.getRows()[i].get("serviceid").toString();
					break;
				}
			}
		}
		// 根据根节点查询下级的识别规则业务节点
		if (StringUtils.isBlank(rootserviceid)) {
			obj.put("success", false);
			obj.put("msg", "业务根节点不存在");
			return obj;
		}
		//获取识别业务规则ID 
		ruleserviceid = getBusinessServiceId("识别规则业务",userid,servicetype,rootserviceid);
		// 识别规则业务节点ID不存在
		if (StringUtils.isBlank(ruleserviceid)) {
			obj.put("success", false);
			obj.put("msg", "业务根节点下【识别规则业务】不存在");
			return obj;
		}
		//获取业务名称获取ID 
		businessserviceid = getBusinessServiceId("业务名称获取",userid,servicetype,ruleserviceid);
		// 业务名称获取节点ID不存在
		if (StringUtils.isBlank(businessserviceid)) {
			obj.put("success", false);
			obj.put("msg", "业务根节点下【识别规则业务-业务名称获取】不存在");
			return obj;
		}
		
		//获取业务下的标准问：业务词获取
		Result rs = CommonLibKbDataDAO.getAbstractInfoByServiceid(businessserviceid);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String abs = rs.getRows()[i].get("abstract").toString();
				abs = abs.split(">")[1];
				if ("业务词获取".equals(abs)) {
					kbdataid = rs.getRows()[i].get("kbdataid").toString();
					break;
				}
			}
		}
		String normalquery = "业务词获取";
		if(StringUtils.isBlank(kbdataid)){			
			//新增业务词标准问
			if(!(Boolean)addQueryByBussinessWord(user,businessserviceid,normalquery)){
				obj.put("success", false);
				obj.put("msg", "业务根节点下【识别规则业务-业务名称获取】标准问【"+normalquery+"】不存在");
				return obj;
			}
			Map<String, Map<String, String>> map = CommonLibQueryManageDAO.getNormalQueryDic(businessserviceid);
			Map<String, String> maps = map.get(normalquery.trim());
			kbdataid = maps.get("kbdataid");

		}
		Result queryRs = CommonLibQueryManageDAO.getQueryIdByQuery(normalquery, kbdataid);
		String queryid = queryRs.getRows()[0].get("id").toString();
		
		// 插入问题库自动学习词模
		List<List<String>> combListList = new ArrayList<List<String>>();
		//添加词模
		String[] words= businesswords.split("-");
		String wordpat = "";
		for(int i =0;i<words.length;i++){
			 wordpat += words[i].toUpperCase()+"近类*";						
		}
		wordpat = wordpat.substring(0,wordpat.lastIndexOf("*"))+"#有序#业务X="+businesswords.replace("-", "").toUpperCase();
		wordpat =  SimpleString.SimpleWordPatToWordPat(wordpat);
		if (Check.CheckWordpat(wordpat, request)) {
		List<String> combList = new ArrayList<String>();
			combList.add(wordpat);
			combList.add("全国");
			combList.add(normalquery);
			combList.add(kbdataid);
			combList.add(queryid);
			combListList.add(combList);
		}else{
			obj.put("success", false);
			obj.put("msg", "新增业务词词模失败!");
		}

		int count = -1;
		if (combListList.size() > 0) {
			count = CommonLibQueryManageDAO.insertWordpatByBusiness(combListList, servicetype, userid, "0");
			if (count > 0) {
				obj.put("success", true);
				obj.put("msg", "新增业务词词模成功!");
			} else {
				obj.put("success", false);
				obj.put("msg", "新增业务词词模失败!");
			}
		}
		obj.put("ktdataid", kbdataid);

		return obj;

	}
	/**
	 * 增加业务词的标准问
	 * @return
	 */
	private static Object addQueryByBussinessWord(User user,String serviceid,String normalquery){
		List<String> cityList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO.resourseAccess(user.getUserID(),
				"querymanage", "S");
		cityList = resourseMap.get("地市");
		String userCityCode = "";
		if (cityList.size() > 0) {
			userCityCode = StringUtils.join(cityList.toArray(), ",");
		}

		// 业务地市
		List<String> serviceCityList = new ArrayList<String>();
		String serviceCityCode = "";
		Result scityRs = CommonLibQueryManageDAO.getServiceCitys(serviceid);
		if (scityRs != null && scityRs.getRowCount() > 0) {
			String city = scityRs.getRows()[0].get("city").toString();
			serviceCityList = Arrays.asList(city.split(","));
		}
		if (serviceCityList.size() > 0) {
			serviceCityCode = StringUtils.join(serviceCityList.toArray(), ",");
		}
		int rs = CommonLibQueryManageDAO.addNormalQueryAndCustomerQuery(serviceid, normalquery, normalquery,
				"全国", user, userCityCode, serviceCityCode);
		if(rs > 0 ){
			return true;
		}
		return false;				
	}
	
	/**
	 * 获取业务ID
	 * @param service  待获取节点的业务名称
	 * @param userid  用户ID
	 * @param servicetype  行业
	 * @param rootserviceid  上级业务节点ID 
	 * @return
	 */
	private static String getBusinessServiceId(String service,String userid,String servicetype,String rootserviceid){
		String serviceid = "";
		Result rs = CommonLibQueryManageDAO.createServiceTreeNew(userid, servicetype, "querymanage", "全国",
				rootserviceid);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				if (service.equals(rs.getRows()[i].get("service").toString())) {
					serviceid = rs.getRows()[i].get("serviceid").toString();
					break;
				}
			}
		}
		if(StringUtils.isBlank(serviceid)){//新增业务
			JSONObject jsonobj = (JSONObject) appendService(rootserviceid, service);
			serviceid = jsonobj.getString("serviceid");
		}
		return serviceid;
	}
	
	/**
	 *@description 获取排除问题列表信息
	 *@param serviceid
	 *@param kbdataid
	 *@param normalQuery
	 *@param customerQuery
	 *@param cityCode
	 *@param page
	 *@param rows
	 *@return
	 *@returnType Object
	 */
	public static Object selectRemoveQuery(String serviceid, String kbdataid,String normalQuery,
			String customerQuery, String cityCode, String isTrain, String removequerystatus, int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int count = CommonLibQueryManageDAO.getRemoveQueryCount(serviceid,kbdataid, customerQuery, cityCode, isTrain, removequerystatus);
		if (count > 0) {
			jsonObj.put("total", count);
			Result rs = CommonLibQueryManageDAO.selectRemoveQuery(serviceid,kbdataid, customerQuery, cityCode, isTrain, removequerystatus, page, rows);
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

					obj.put("cityname", cityName);
					obj.put("citycode", city);
					obj.put("isstrictexclusion", rs.getRows()[i].get("isstrictexclusion"));
					obj.put("result", rs.getRows()[i].get("result"));
					obj.put("istrain", rs.getRows()[i].get("istrain"));
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
	 * 新增排除问题
	 * @param serviceid
	 * @param queryType
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param removequerystatus
	 * @param request
	 * @return
	 */
	public static Object addRemoveQueryWordpat(String serviceid, String queryType,
			String normalQuery, String customerQuery, String cityCode, String removequerystatus,  HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		// 新增排除问题
		int rs = addRemoveQuery(serviceid, queryType, normalQuery, customerQuery, cityCode, removequerystatus);
		if (rs > 0) {
			// 新增排除词模
			Object sre = GetSession.getSessionByKey("accessUser");
			User user = (User) sre;
			String serviceType = user.getIndustryOrganizationApplication();
			List<String> roleidList = UserManagerDAO.getRoleIDListByUserId(user.getUserID());
			
			List<String> citylist = new ArrayList<String>();
			citylist.add(cityCode);
			String customerQueryArray[] = customerQuery.split("\n");
			Result res = CommonLibQueryManageDAO.findCustomerquery(Arrays.asList(customerQueryArray), serviceType,roleidList,citylist, 1);
			List<String> combitionList = new ArrayList<String>();
			if (res != null && res.getRowCount() > 0) {
				for (int j = 0; j < res.getRowCount(); j++) {					
					List<String> queryList = new ArrayList<String>();
					queryList.add(cityCode);
					queryList.add(res.getRows()[j].get("query").toString());
					queryList.add(res.getRows()[j].get("kbdataid").toString());
					queryList.add(res.getRows()[j].get("queryid").toString());
					queryList.add(removequerystatus);
					combitionList.add(StringUtils.join(queryList, "@#@"));
				}
			}
			if(!CollectionUtils.isEmpty(combitionList)){
				String combition = StringUtils.join(combitionList, "@@");
				JSONObject obj = (JSONObject) AnalyzeDAO.produceWordpat(combition, "2", request);
				if (obj.getBooleanValue("success")) {
					jsonObj.put("success", true);
					jsonObj.put("msg", "保存成功!");
				} else {
					jsonObj.put("success", false);
					jsonObj.put("msg", "保存排除问题成功，生成排除词模失败!");
				}
			}else{
				jsonObj.put("success", false);
				jsonObj.put("msg", "保存排除问题成功，生成排除词模失败!");
			}

		} else {
			if (rs == -2) {
				jsonObj.put("success", false);
				jsonObj.put("msg", "排除问题已存在!");
			} else {
				jsonObj.put("success", false);
				jsonObj.put("msg", "保存失败!");
			}
		}
		return jsonObj;
	}
	/**
	 *@description 新增排除问题
	 *@param serviceid
	 *@param queryType
	 *@param normalQuery
	 *@param customerQuery
	 *@param cityCode
	 *@returnType void
	 */
	public static int addRemoveQuery(String serviceid, String queryType,
			String normalQuery, String customerQuery, String cityCode, String removequerystatus) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		int rs = -1;
		if ("排除问题".equals(queryType)) {// 新增排除问题
			rs = CommonLibQueryManageDAO.addRemoveQuery(normalQuery,
					customerQuery, cityCode, user, removequerystatus);
		}
		return rs;
	}
	
	/**
	 * 排除问题生成词模
	 *@param combition
	 *@param request
	 *@return
	 *@returnType Object
	 */
	public static Object removeProduceWordpat(String combition,String wordpattype, HttpServletRequest request) {
		//生成词模
		JSONObject jsonObj = (JSONObject) AnalyzeDAO.produceWordpat(combition, wordpattype, request);
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		Result rs = CommonLibNewWordInfoDAO.selectNewWordInfo(servicetype, null);
		List<String> newWordList = new ArrayList<String>();
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				List<String> list = new ArrayList<String>();
				list.add(rs.getRows()[i].get("newword") != null ? rs.getRows()[i].get("newword").toString() : "");
				list.add(rs.getRows()[i].get("wordclassid") != null ? rs.getRows()[i].get("wordclassid").toString() : "");
				list.add(rs.getRows()[i].get("isserviceword") != null ? rs.getRows()[i].get("isserviceword").toString() : "");
				newWordList.add(StringUtils.join(list, "@@"));
			}
		}		
		jsonObj.put("newWord", StringUtils.join(newWordList, "##"));
		return jsonObj;
	}
	
	/**
	 * 新增别名
	 * 
	 * @param combition
	 * @param content
	 * @return
	 */
	public static Object addOtherWord(String combition, String content) {
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取词类ID和词条名称
		String[] newWordArray = combition.split("#");
		String wordClassId = newWordArray[1];
		String word = newWordArray[0];
		// 获取别名
		List<String> otherWordList = new ArrayList<String>();
		String[] otherWordArray = content.split("#");
		for (int i = 0; i < otherWordArray.length; i++) {
			otherWordList.add(otherWordArray[i]);
//			String[] wArr = otherWordArray[i].split("\\|");
//			for (int j = 0; j < wArr.length; j++) {
//				otherWordList.add(wArr[j]);
//			}
		}
		// 判断词条是否存在
		if (!CommonLibWordDAO.exist(word, wordClassId)) {
			CommonLibWordDAO.insert(word, wordClassId, user);
		}
		// 获取词条ID
		Result wordRs = CommonLibWordDAO.getWordInfo(wordClassId, word);
		String wordId = "";
		// 判断数据源不为null且含有数据
		if (wordRs != null && wordRs.getRowCount() > 0) {
			wordId = wordRs.getRows()[0].get("wordid") != null ? wordRs.getRows()[0].get("wordid").toString() : "";
		}
		// 新增别名
		int index = 0;
		if (StringUtils.isNotBlank(wordId)) {
			for (int i = 0; i < otherWordList.size(); i++) {
				if (!CommonLibWordDAO.existOtherWord(otherWordList.get(i), wordId)) {
					index = CommonLibWordDAO.insertOtherWord(otherWordList.get(i), wordId, wordClassId, user);
				}
			}
		}
		if (index > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "保存成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "保存失败!");
		}
		return jsonObj;
	}
}
