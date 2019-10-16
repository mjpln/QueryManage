package com.knowology.km.bll;

import java.io.File;
import java.io.IOException;
import java.sql.Clob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.GlobalValue;
import com.knowology.Bean.ImportNormalqueryBean;
import com.knowology.Bean.User;
import com.knowology.UtilityOperate.GetConfigValue;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.bll.CommonLibWordpatDAO;
import com.knowology.bll.ConstructSerialNum;
import com.knowology.dal.Database;
import com.knowology.km.entity.CheckInforef;
import com.knowology.km.entity.ImportKbData;
import com.knowology.km.entity.ImportQuery;
import com.knowology.km.entity.ImportService;
import com.knowology.km.entity.ImportWordpat;
import com.knowology.km.util.Check;
import com.knowology.km.util.CheckInput;
import com.knowology.km.util.GetLoadbalancingConfig;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.ReadExcel;
import com.knowology.km.util.SimpleString;

public class ImportExportDAO {
	public static String FILE_PATH_EXPORT = QuerymanageDAO.FILE_PATH_EXPORT;
	public static String regressTestPath = QuerymanageDAO.regressTestPath;
	public static Logger logger = Logger.getLogger(ImportExportDAO.class);
	
	/**
	 * 定义全局 cityCodeToCityName字典
	 */
	public static Map<String, String> cityCodeToCityName = QuerymanageDAO.cityCodeToCityName;

	/**
	 * 定义全局 cityNameToCityCode 字典
	 */
	public static Map<String, String> cityNameToCityCode = QuerymanageDAO.cityNameToCityCode;
	
	
	/**
	 * 导入客户问题到数据库中
	 * 
	 * @param filename参数文件名称
	 * @param serviceid
	 *            业务ID
	 * @return
	 */
	public static Object importFile(String filename, String serviceid,int queryType) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
//		String brand = serviceType.split("->")[1] + "问题库";
		List<String> userCityList = new ArrayList<String>();
		//业务地市
		List<String> serviceCityList = new ArrayList<String>();
//		Result rs = CommonLibQueryManageDAO.getServiceCitys(serviceid,brand);
		Result rs = CommonLibQueryManageDAO.getServiceCitys(serviceid);
		if (rs != null && rs.getRowCount() > 0) {
			String city = rs.getRows()[0].get("city").toString();
			serviceCityList=Arrays.asList(city.split(","));
		}
		
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
			
			Map<ImportNormalqueryBean, Map<String, List<String>>> map = new LinkedHashMap<ImportNormalqueryBean, Map<String, List<String>>>();
			try {
				 String result = getImportQueryDic(map ,info,serviceCityList);
				 if(!StringUtils.isEmpty(result)){
					 jsonObj.put("errorMsg", result.replaceFirst(",", ""));	
				 }
			} catch (Exception e) {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", e.getMessage());	
				return jsonObj;
			}
			int count  = CommonLibQueryManageDAO.importQuery(map,getQueryDic(serviceid,queryType),serviceCityList, serviceid,bussinessFlag,workerid,queryType);
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
	 *@description 获得导入客户问题字典
	 *@param info
	 *@param serviceCityList
	 *@return 
	 * @throws Exception 
	 *@returnType Map<String,Map<String,List<String>>> 
	 */
	public static String getImportQueryDic(Map<ImportNormalqueryBean, Map<String, List<String>>> map, List<List<Object>> info,List<String> serviceCityList ) throws Exception{
//		Map<ImportNormalqueryBean, Map<String, List<String>>> map = new LinkedHashMap<ImportNormalqueryBean, Map<String, List<String>>>();
		String errorMsg = "";
		
		for (int i = 0; i < info.size(); i++) {
			List<String> cityList = new ArrayList<String>();
			String normalquery = info.get(i).get(0)!=null?  info.get(i).get(0).toString().replace(" ", ""):"";
			String customerquery = info.get(i).get(1)!=null?  info.get(i).get(1).toString().replace(" ", ""):"";
			String responsetype = info.get(i).get(2)!=null?  info.get(i).get(2).toString().replace(" ", ""):"";
			String interacttype = info.get(i).get(3)!=null?  info.get(i).get(3).toString().replace(" ", ""):"";
			ImportNormalqueryBean normalqueryBean = new ImportNormalqueryBean(normalquery, QuerymanageDAO.getResponseType(responsetype), QuerymanageDAO.getInteractType(interacttype));
			if("".equals(normalquery)){//如果标准问题为空，不做处理
				continue;
			}
			
			String line = ( i+1 ) + "";
			if(normalquery.length() > 50){
				errorMsg += ","+line;
				continue;
			}
			
			if(customerquery.length() > 50){
				if(errorMsg.indexOf(line) < 0){
					errorMsg += ","+line;
				}
				continue;
			}
			String city = info.get(i).get(4)!=null?  info.get(i).get(4).toString().replace(" ", "").replace("，", ","):"";
			if (!"".equals(city) && city != null) {//如果客户问题来源地市不为空通过地市名称取地址编码
				city = city.replace("省", "").replace("市", "");
				String cityArray[] = city.split(",");
				for (int m = 0; m < cityArray.length; m++) {
					if (cityNameToCityCode.containsKey(cityArray[m])) {
						String cityCode = getCityCodeFromServiceCityCodes(serviceCityList, cityArray[m]);
						//扩展问地市来源不在业务地市范围内
						if(StringUtils.isEmpty(cityCode)){
							throw new Exception("客户问题“"+customerquery+"”地市来源“"+cityArray[m]+"”，不在业务地市范围内！");
						}
						cityList.add(cityCode);
					}
				}
			}else{//如果客户问题来源地市为空，取当前用户关联地市
				cityList.addAll(serviceCityList);
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
		return errorMsg;
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
	 * 从业务地市集合中获取传入地市的CODE
	 * @param serviceCityCodeList
	 * @param city
	 * @return
	 */
	private static String getCityCodeFromServiceCityCodes(List<String> serviceCityCodeList, String city){
		for(String cityCode : serviceCityCodeList){
			if("全国".equals(cityCode)){
				return cityNameToCityCode.get(city);
			}
			
			String cityName = cityCodeToCityName.get(cityCode);
			if(StringUtils.isNotEmpty(cityName)){
				if(cityName.equals(city)){
					return cityCode;
				}
			}
		}
		return null;
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
				line.add(QuerymanageDAO.getInteractType(ObjectUtils.toString(result.getRows()[i].get("interacttype")), false));
				line.add(QuerymanageDAO.getCityName(result.getRows()[i].get("city") == null ? "" : result.getRows()[i].get("city").toString()));
				text.add(line);
			}
			String filename = "customerquery_";
			filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			boolean isWritten = ReadExcel.writeExcel(FILE_PATH_EXPORT, filename, null, null, colTitle, text);
			if(isWritten){
				file = new File(FILE_PATH_EXPORT + filename + ".xls");
			}
		}
		return file;
		
	}
	
	/**
	 * 导出业务树信息
	 * @param serviceid
	 * @return
	 */
	public static Object exportService(String serviceid){
		JSONObject jsonObject = new JSONObject();
		
		Result rs = CommonLibServiceDAO.getServiceInfoByPid(serviceid);
		Map<String,JSONObject> map = new HashMap<String, JSONObject>();
		if(rs != null && rs.getRowCount() > 0){
			for(int i = 0, l = rs.getRowCount(); i< l;i++){
				String sid = rs.getRows()[i].get("serviceid") == null ? "" : rs.getRows()[i].get("serviceid").toString();
				String pid = rs.getRows()[i].get("parentid") == null ? "" : rs.getRows()[i].get("parentid").toString();
				String service = rs.getRows()[i].get("service") == null ? "" : rs.getRows()[i].get("service").toString();
				String city = rs.getRows()[i].get("city") == null ? "" : rs.getRows()[i].get("city").toString();
				String cityid = rs.getRows()[i].get("cityid") == null ? "" : rs.getRows()[i].get("cityid").toString();
				String brand = rs.getRows()[i].get("brand") == null ? "" : rs.getRows()[i].get("brand").toString();
				JSONObject node = map.get(sid);
				if(node == null){
					node = new JSONObject();
					map.put(sid, node);
				}
				node.put("service", service);
				node.put("city", city);
				node.put("cityid", cityid);
				node.put("brand", brand);
				JSONObject parent = map.get(pid);
				if(parent == null){
					parent = new JSONObject();
					map.put(pid, parent);
				}
				if(parent.getJSONArray("children") == null){
					parent.put("children", new JSONArray());
				}
				parent.getJSONArray("children").add(node);
			}
		}
		JSONArray array = new JSONArray();
		array.add(map.get(serviceid));
		String filename = "service_";
		filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss") +".json";
		try {
			FileUtils.writeStringToFile(FileUtils.getFile(FILE_PATH_EXPORT,filename), array.toJSONString(),"utf-8");
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
	 * @param serviceid
	 * @return
	 */
	public static Object importService(String filename, String serviceid,  HttpServletRequest request){
		// 定义返回的json串
		JSONObject jsonObject = new JSONObject();
		User user = (User) GetSession.getSessionByKey("accessUser");
		String serviceType = user.getIndustryOrganizationApplication();
		String bussinessFlag = CommonLibMetafieldmappingDAO
				.getBussinessFlag(serviceType);
		
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
		if(array.size() > 0){
			JSONObject root = array.getJSONObject(0);
			JSONArray children = root.getJSONArray("children");
			Stack<JSONObject> nodeStack = new Stack<JSONObject>();
			
			//获取根业务信息
			Result rs = CommonLibServiceDAO.getServiceInfoByserviceid(serviceid);
			String rootName = "";
			String brand = "";
			String rootCity = "";
			String rootCityId = "";
			if (rs != null && rs.getRowCount() > 0){
				rootName = rs.getRows()[0].get("service") == null ? "" : rs.getRows()[0].get("service").toString();
				brand = rs.getRows()[0].get("brand") == null ? "" : rs.getRows()[0].get("brand").toString();
				rootCity = rs.getRows()[0].get("city") == null ? "" : rs.getRows()[0].get("city").toString();
				rootCityId = rs.getRows()[0].get("cityid") == null ? "" : rs.getRows()[0].get("cityid").toString();
			}else{
				jsonObject.put("success", false);
				jsonObject.put("msg", "业务不存在！");
				return jsonObject;
			}
			
			//查询父节点下所有子节点名称及编号，用于查重
			rs = CommonLibServiceDAO.getChildServiceByParentID(serviceid);
			Map<String,String> map = new HashMap<String,String>();
			if (rs != null && rs.getRowCount() > 0){
				for(int i =0;i<rs.getRowCount();i++){
					String name = rs.getRows()[i].get("service") == null ? "" : rs.getRows()[i].get("service").toString();
					String id = rs.getRows()[i].get("serviceid") == null ? "" : rs.getRows()[i].get("serviceid").toString();
					map.put(name, id);
				}
			}
			
			//根节点下的子节点先入栈，排除根节点的遍历
		    if ( children != null && children.size() > 0) {
	            for (int i =0;i<children.size();i++) {
	            	//补充业务父节点信息
	            	JSONObject object = children.getJSONObject(i);
	            	object.put("parentId", serviceid);
	            	object.put("parentName", rootName);
	            	//如果重复，记录重复的业务id，并且不进行插入操作
	            	if(map.containsKey(object.getString("service"))){
	            		object.put("repeatid", map.get(object.getString("service")));
	            	}
	                nodeStack.push(object);
	            }
	        }
		    JSONObject node = null;
		    //业务树的遍历
		    while (!nodeStack.isEmpty()) {
		        node = nodeStack.pop();
		        String newServiceId = "";
		        String serviceName = node.getString("service");
		        Map<String,String> map2 = null;
		        //操作业务节点的代码
		        if(!node.containsKey("repeatid")){
		        	//业务父节点信息
			        String parentId = node.getString("parentId");
			        String parentName = node.getString("parentName");
			      //业务本身信息
			        String city = "";
			        String cityid ="";
			        if(node.containsKey("city") && !StringUtils.isEmpty(node.getString("city"))){
			        	city = node.getString("city");
			        }else{//city使用根节点city
			        	city = rootCity;
			        }
			        if(node.containsKey("cityid") && !StringUtils.isEmpty(node.getString("cityid"))){
			        	cityid = node.getString("cityid");
			        }else{//city使用根节点city
			        	cityid = rootCityId;
			        }
			        logger.info("插入业务节点："+node);
			        newServiceId = CommonLibQueryManageDAO.insertServiceNotLog(parentId,parentName, serviceName, brand, bussinessFlag,cityid,city);
			        if(StringUtils.isEmpty(newServiceId)){//插入失败，跳过
			        	continue;
			        }
		        }else{//重复
		        	logger.info("业务节点重复："+node);
		        	newServiceId = node.getString("repeatid");
		        	//如果当前节点重复，则继续进行节点查重
					rs = CommonLibServiceDAO.getChildServiceByParentID(newServiceId);
					map2 = new HashMap<String,String>();
					if (rs != null && rs.getRowCount() > 0){
						for(int i =0;i<rs.getRowCount();i++){
							String name = rs.getRows()[i].get("service") == null ? "" : rs.getRows()[i].get("service").toString();
							String id = rs.getRows()[i].get("serviceid") == null ? "" : rs.getRows()[i].get("serviceid").toString();
							map2.put(name, id);
						}
					}
		        }
		      
		        //获得节点的子节点，压入栈中继续遍历
		        children = node.getJSONArray("children");
		        if (children != null && children.size() > 0) {
		            for (int i =0;i<children.size();i++) {
		            	JSONObject object = children.getJSONObject(i);
		            	//补充子节点的父节点信息
		            	object.put("parentId", newServiceId);
		            	object.put("parentName", serviceName);
		            	//查重
		            	if(map2 != null && map2.containsKey(object.getString("service"))){
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
	 * 导出词模
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object exportWordpat(String serviceid,String flag){
		JSONObject jsonObject = new JSONObject();
		Map<String,String> servicePathMap = getServicePathMap(serviceid, "-");
		
		Result result = CommonLibQueryManageDAO.exportWordpat(serviceid,flag);
		if (result != null && result.getRowCount() > 0){
			List<String> colTitle = Arrays.asList("问题库路径","标准问题","回复类型","交互类型","词模","词模针对问题","词模类型","来源地市","添加时间","场景所在目录","场景名称","场景详情");
			List<List<String>> text = new ArrayList<List<String>>();
			String normalquery = "";
			String wordpat="";
			String relationserviceid="";
			Map<String,String> scenariosNameMap = new HashMap<String,String>();
			for (int i = 0; i < result.getRowCount(); i++) {
				List<String> line = new ArrayList<String>();
				line.add(servicePathMap.get(result.getRows()[i].get("serviceid").toString()));//问题库路径
				normalquery = (result.getRows()[i].get("abstract") == null ? "" : result.getRows()[i].get("abstract").toString());
				line.add(StringUtils.substringAfter(normalquery, ">"));//标准问题
				line.add(ObjectUtils.toString(result.getRows()[i].get("responsetype"), "未知" ));//回复类型
				line.add(QuerymanageDAO.getInteractType(ObjectUtils.toString(result.getRows()[i].get("interacttype")), false));//交互类型
				wordpat = result.getRows()[i].get("wordpat") == null ? "" : result.getRows()[i].get("wordpat").toString();
				String simworpat = wordpat == "" ? "":SimpleString.worpattosimworpat(wordpat);
				line.add(simworpat);//词模
				String q = SimpleString.getQueryBySimpleWordpat(simworpat);
				line.add(q ==null ?"":q);//词模针对问题
				String wordpattype = QuerymanageDAO.wordpatType.get(result.getRows()[i].get("wordpattype") == null? "": result.getRows()[i].get("wordpattype").toString());
				line.add(wordpattype == null ? "" : wordpattype);//词模类型
				line.add(QuerymanageDAO.getCityName(result.getRows()[i].get("city") == null ? "" : result.getRows()[i].get("city").toString()));//来源地市
				line.add(result.getRows()[i].get("edittime") == null ? "" : result.getRows()[i].get("edittime").toString());//添加时间
//				Object time = result.getRows()[i].get("edittime");
//				if(time != null){//添加时间
//					line.add(DateFormatUtils.format(((Timestamp) time).getTime(), "yyyy/MM/dd HH:mm"));
//				}else{
//					line.add("");
//				}
				relationserviceid = result.getRows()[i].get("relationserviceid") == null ? "" : result.getRows()[i].get("relationserviceid").toString();
				//查找场景目录
				String scenariosName = "";
				if(relationserviceid.length()>0 && !servicePathMap.containsKey(relationserviceid)){
					ArrayList<String> path = CommonLibServiceDAO.getServicePath(relationserviceid);
					path.remove("知识库");// 移除根节点
					if(path.size() > 0 )//防止查不出业务
						scenariosName = path.remove(path.size()-1);
					String servicePath = StringUtils.join(path.toArray(), "-");
					servicePathMap.put(relationserviceid, servicePath);
					scenariosNameMap.put(relationserviceid, scenariosName);
				}
				line.add(servicePathMap.get(relationserviceid)==null ?"":servicePathMap.get(relationserviceid));//场景所在目录
				line.add(scenariosNameMap.get(relationserviceid)==null ?"":scenariosNameMap.get(relationserviceid));//场景名称
				Object ruleresponse = result.getRows()[i].get("ruleresponse");
				line.add(StringEscapeUtils.unescapeHtml(ObjectUtils.toString(MyUtil.oracleClob2Str((Clob) ruleresponse))));//场景详情
				text.add(line);
			}
			String filename = "wordpat_";
			filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			boolean isWritten = ReadExcel.writeExcel(FILE_PATH_EXPORT, filename, null, null, colTitle, text);
			if(isWritten){
//				file = new File(FILE_PATH_EXPORT + filename + ".xls");
				jsonObject.put("success", true);
				jsonObject.put("fileName", filename+ ".xls");
			}else{
				jsonObject.put("success", false);
				jsonObject.put("msg", "生成文件失败");
			}
		}
		return jsonObject;
		
	}

	/**
	 * 导出客户问
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object createQueryExport(String serviceid,String flag){
		JSONObject jsonObject = new JSONObject();
		Map<String,String> servicePathMap = getServicePathMap(serviceid, "-");
		
		Result result = CommonLibQueryManageDAO.exportCustomerQuery(serviceid,flag);
		if (result != null && result.getRowCount() > 0){
			List<String> colTitle = Arrays.asList("问题库路径","标准问题","客户问题","回复类型","交互类型","来源地市","客户问添加时间","场景所在目录","场景名称","场景详情");
			List<List<String>> text = new ArrayList<List<String>>();
			String normalquery = "";
			String wordpat="";
			String relationserviceid="";
			Map<String,String> scenariosNameMap = new HashMap<String,String>();
			for (int i = 0; i < result.getRowCount(); i++) {
				List<String> line = new ArrayList<String>();
				line.add(servicePathMap.get(result.getRows()[i].get("serviceid").toString()));//问题库路径
				normalquery = (result.getRows()[i].get("abstract") == null ? "" : result.getRows()[i].get("abstract").toString());
				line.add(StringUtils.substringAfter(normalquery, ">"));//标准问题
				line.add(result.getRows()[i].get("query") == null ? "" : result.getRows()[i].get("query").toString());//回复类型
				line.add(ObjectUtils.toString(result.getRows()[i].get("responsetype"), "未知" ));//回复类型
				line.add(QuerymanageDAO.getInteractType(ObjectUtils.toString(result.getRows()[i].get("interacttype")), false));//交互类型
				line.add(QuerymanageDAO.getCityName(result.getRows()[i].get("city") == null ? "" : result.getRows()[i].get("city").toString()));//来源地市
				line.add(result.getRows()[i].get("edittime") == null ? "" : result.getRows()[i].get("edittime").toString());//添加时间
				relationserviceid = result.getRows()[i].get("relationserviceid") == null ? "" : result.getRows()[i].get("relationserviceid").toString();
				//查找场景目录
				String scenariosName = "";
				if(relationserviceid.length()>0 && !servicePathMap.containsKey(relationserviceid)){
					ArrayList<String> path = CommonLibServiceDAO.getServicePath(relationserviceid);
					path.remove("知识库");// 移除根节点
					if(path.size() > 0 )//防止查不出业务
						scenariosName = path.remove(path.size()-1);
					String servicePath = StringUtils.join(path.toArray(), "-");
					servicePathMap.put(relationserviceid, servicePath);
					scenariosNameMap.put(relationserviceid, scenariosName);
				}
				line.add(servicePathMap.get(relationserviceid)==null ?"":servicePathMap.get(relationserviceid));//场景所在目录
				line.add(scenariosNameMap.get(relationserviceid)==null ?"":scenariosNameMap.get(relationserviceid));//场景名称
				Object ruleresponse = result.getRows()[i].get("ruleresponse");
				line.add(StringEscapeUtils.unescapeHtml(ObjectUtils.toString(MyUtil.oracleClob2Str((Clob) ruleresponse))));//场景详情
				text.add(line);
			}
			String filename = "customerquery_";
			filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			boolean isWritten = ReadExcel.writeExcel(FILE_PATH_EXPORT, filename, null, null, colTitle, text);
			if(isWritten){
//				file = new File(FILE_PATH_EXPORT + filename + ".xls");
				jsonObject.put("success", true);
				jsonObject.put("fileName", filename+ ".xls");
				return jsonObject;
			}
		}
		jsonObject.put("success", false);
		jsonObject.put("msg", "生成文件失败");
		return jsonObject;
		
	}
	/**
	 * 导出客户问
	 * @param serviceid
	 * @param normalQuery
	 * @param customerQuery
	 * @param cityCode
	 * @param responseType
	 * @param interactType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Object exportWordpatAndQuery(String serviceid,String flag){
		JSONObject jsonObject = new JSONObject();
		long startTime = System.currentTimeMillis();
		long logTime = startTime;
		Map<String,String> servicePathMap = getServicePathMap(serviceid, "-");
		Map<String,String> scenariosNameMap = new HashMap<String,String>();
		//excel表头数据
		Map<String,List<String>> columnTitleMap = new HashMap<String,List<String>>();
		//excel表数据
		Map<String,List<List<Object>>> textMap = new HashMap<String,List<List<Object>>>();
		Result result = CommonLibQueryManageDAO.exportWordpat(serviceid,flag);
		if (result != null && result.getRowCount() > 0){
			List<String> colTitle = Arrays.asList("问题库路径","标准问题","回复类型","交互类型","词模","词模针对问题","词模类型","来源地市","添加时间","场景所在目录","场景名称","场景详情");
			columnTitleMap.put("场景-标准问-词模针对问题", colTitle);
			List<List<Object>> text = new ArrayList<List<Object>>();
			String normalquery = "";
			String wordpat="";
			String relationserviceid="";
			
			for (int i = 0; i < result.getRowCount(); i++) {
				List<Object> line = new ArrayList<Object>();
				line.add(servicePathMap.get(result.getRows()[i].get("serviceid").toString()));//问题库路径
				normalquery = (result.getRows()[i].get("abstract") == null ? "" : result.getRows()[i].get("abstract").toString());
				line.add(StringUtils.substringAfter(normalquery, ">"));//标准问题
				line.add(ObjectUtils.toString(result.getRows()[i].get("responsetype"), "未知" ));//回复类型
				line.add(QuerymanageDAO.getInteractType(ObjectUtils.toString(result.getRows()[i].get("interacttype")), false));//交互类型
				wordpat = result.getRows()[i].get("wordpat") == null ? "" : result.getRows()[i].get("wordpat").toString();
				String simworpat = wordpat == "" ? "":SimpleString.worpattosimworpat(wordpat);
				line.add(simworpat);//词模
				String q = SimpleString.getQueryBySimpleWordpat(simworpat);
				line.add(q ==null ?"":q);//词模针对问题
				String wordpattype = QuerymanageDAO.wordpatType.get(result.getRows()[i].get("wordpattype") == null? "": result.getRows()[i].get("wordpattype").toString());
				line.add(wordpattype == null ? "" : wordpattype);//词模类型
				line.add(QuerymanageDAO.getCityName(result.getRows()[i].get("city") == null ? "" : result.getRows()[i].get("city").toString()));//来源地市
				line.add(result.getRows()[i].get("edittime") == null ? "" : result.getRows()[i].get("edittime").toString());//添加时间
//				Object time = result.getRows()[i].get("edittime");
//				if(time != null){//添加时间
//					line.add(DateFormatUtils.format(((Timestamp) time).getTime(), "yyyy/MM/dd HH:mm"));
//				}else{
//					line.add("");
//				}
				relationserviceid = result.getRows()[i].get("relationserviceid") == null ? "" : result.getRows()[i].get("relationserviceid").toString();
				//查找场景目录
				String scenariosName = "";
				if(relationserviceid.length()>0 && !servicePathMap.containsKey(relationserviceid)){
					ArrayList<String> path = CommonLibServiceDAO.getServicePath(relationserviceid);
					path.remove("知识库");// 移除根节点
					if(path.size() > 0 )//防止查不出业务
						scenariosName = path.remove(path.size()-1);
					String servicePath = StringUtils.join(path.toArray(), "-");
					servicePathMap.put(relationserviceid, servicePath);
					scenariosNameMap.put(relationserviceid, scenariosName);
				}
				line.add(servicePathMap.get(relationserviceid)==null ?"":servicePathMap.get(relationserviceid));//场景所在目录
				line.add(scenariosNameMap.get(relationserviceid)==null ?"":scenariosNameMap.get(relationserviceid));//场景名称
				Object ruleresponse = result.getRows()[i].get("ruleresponse");
				line.add(StringEscapeUtils.unescapeHtml(ObjectUtils.toString(MyUtil.oracleClob2Str((Clob) ruleresponse))));//场景详情
				text.add(line);
			}
			textMap.put("场景-标准问-词模针对问题", text);
			logger.info("词模语义数据总数："+text.size()+"行");
			logger.info("词模语义数据耗时："+(System.currentTimeMillis()-logTime)+"ms");
			logTime = System.currentTimeMillis();
		}
		
		result = CommonLibQueryManageDAO.exportCustomerQuery(serviceid,flag);
		if (result != null && result.getRowCount() > 0){
			List<String> colTitle = Arrays.asList("问题库路径","标准问题","客户问题","回复类型","交互类型","来源地市","客户问添加时间","场景所在目录","场景名称","场景详情");
			columnTitleMap.put("场景-标准问-客户问", colTitle);
			List<List<Object>> text = new ArrayList<List<Object>>();
			String normalquery = "";
			String relationserviceid="";
			for (int i = 0; i < result.getRowCount(); i++) {
				List<Object> line = new ArrayList<Object>();
				line.add(servicePathMap.get(result.getRows()[i].get("serviceid").toString()));//问题库路径
				normalquery = (result.getRows()[i].get("abstract") == null ? "" : result.getRows()[i].get("abstract").toString());
				line.add(StringUtils.substringAfter(normalquery, ">"));//标准问题
				line.add(result.getRows()[i].get("query") == null ? "" : result.getRows()[i].get("query").toString());//回复类型
				line.add(ObjectUtils.toString(result.getRows()[i].get("responsetype"), "未知" ));//回复类型
				line.add(QuerymanageDAO.getInteractType(ObjectUtils.toString(result.getRows()[i].get("interacttype")), false));//交互类型
				line.add(QuerymanageDAO.getCityName(result.getRows()[i].get("city") == null ? "" : result.getRows()[i].get("city").toString()));//来源地市
				line.add(result.getRows()[i].get("edittime") == null ? "" : result.getRows()[i].get("edittime").toString());//添加时间
				relationserviceid = result.getRows()[i].get("relationserviceid") == null ? "" : result.getRows()[i].get("relationserviceid").toString();
				//查找场景目录
				String scenariosName = "";
				if(relationserviceid.length()>0 && !servicePathMap.containsKey(relationserviceid)){
					ArrayList<String> path = CommonLibServiceDAO.getServicePath(relationserviceid);
					path.remove("知识库");// 移除根节点
					if(path.size() > 0 )//防止查不出业务
						scenariosName = path.remove(path.size()-1);
					String servicePath = StringUtils.join(path.toArray(), "-");
					servicePathMap.put(relationserviceid, servicePath);
					scenariosNameMap.put(relationserviceid, scenariosName);
				}
				line.add(servicePathMap.get(relationserviceid)==null ?"":servicePathMap.get(relationserviceid));//场景所在目录
				line.add(scenariosNameMap.get(relationserviceid)==null ?"":scenariosNameMap.get(relationserviceid));//场景名称
				Object ruleresponse = result.getRows()[i].get("ruleresponse");
				line.add(StringEscapeUtils.unescapeHtml(ObjectUtils.toString(MyUtil.oracleClob2Str((Clob) ruleresponse))));//场景详情
				text.add(line);
			}
			textMap.put("场景-标准问-客户问", text);
			logger.info("客户问数据总数："+text.size()+"行");
			logger.info("客户问数据耗时："+(System.currentTimeMillis()-logTime)+"ms");
			logTime = System.currentTimeMillis(); 
		}
		
		
		String filename = "wordpatandquery_";
		filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
		boolean isWritten = ReadExcel.writeExcelBySheet(FILE_PATH_EXPORT, filename, columnTitleMap, textMap);
		
		logger.info("生成excel文件耗时："+(System.currentTimeMillis()-logTime)+"ms");
		logger.info("本次导出请求总耗时："+(System.currentTimeMillis()-startTime)+"ms");
		if(isWritten){
//			file = new File(FILE_PATH_EXPORT + filename + ".xls");
			jsonObject.put("success", true);
			jsonObject.put("fileName", filename+ ".xls");
			return jsonObject;
		}
		jsonObject.put("success", false);
		jsonObject.put("msg", "生成文件失败");
		return jsonObject;
		
	}
	
	/**
	 * 批量构建service路径的工具函数
	 * 
	 * 返回map为 key:serviceid ,value:业务路径
	 * @param id 父节点路径
	 * @param separator 分隔符
	 * @return
	 */
	private static Map<String,String> getServicePathMap(String id,String separator){
		List<String> pathList = CommonLibServiceDAO.getServicePath(id);
		if(pathList.size() < 4){
			pathList.clear();//去除知识库和业务根节点
		}else{
			pathList = pathList.subList(2, pathList.size()-1);
		}
		String prefix = StringUtils.join(pathList, separator);
		if(!StringUtils.isEmpty(prefix)){
			prefix += separator;
		}
		Result rs = CommonLibServiceDAO.getServiceInfoByPid(id);
		Map<String,JSONObject> serviceMap = new HashMap<String,JSONObject>();
		if(rs != null && rs.getRowCount()>0){
			for(int i =0;i<rs.getRowCount();i++){
				String name = rs.getRows()[i].get("service") == null ? "" : rs.getRows()[i].get("service").toString();
				String serviceid = rs.getRows()[i].get("serviceid") == null ? "" : rs.getRows()[i].get("serviceid").toString();
				String parentid = rs.getRows()[i].get("parentid") == null ? "" : rs.getRows()[i].get("parentid").toString();
				if("0".equals(serviceid) || "0.000".equals(serviceid)){
					continue;
				}
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("service", name);
				jsonObject.put("parentid", parentid);
				jsonObject.put("serviceid", serviceid);
				serviceMap.put(serviceid, jsonObject);
			}
		}
		
		Map<String,String> pathMap = new HashMap<String,String>();
		String parentid = null;
		for(Map.Entry<String, JSONObject> entry : serviceMap.entrySet()){
			String serviceid = entry.getKey();
			parentid = entry.getValue().getString("parentid");
			String path = entry.getValue().getString("service");
			JSONObject jsonObject = serviceMap.get(parentid);
			while(jsonObject != null){
				parentid = jsonObject.getString("parentid");
				if(!"0".equals(parentid) && !"0.000".equals(parentid)){
					path = jsonObject.getString("service")+separator+path;
				}
				jsonObject = serviceMap.get(parentid);
			}
			pathMap.put(serviceid, prefix + path);
		}
		return pathMap;
	}

	/**
	 * 导入FAQ
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
		String extension = filename.lastIndexOf(".") == -1 ? "" : filename
				.substring(filename.lastIndexOf(".") + 1);
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
	 * @param user
	 * @param info
	 * @return
	 */
	public static String getFaqInsert(User user, List<List<Object>> info, String serviceid, String service, HttpServletRequest request) {
		
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
//		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库FAQ导入渠道配置", serviceType);
		List<String> channelList = new ArrayList<String>();
		if(rs!=null && rs.getRowCount()>0){
			for (int n = 0; n < rs.getRowCount(); n++) {
				String value = rs.getRows()[n].get("name").toString();
				channelList.add(value);
			}
		}else{
			channelList.add("Web");
		}

		// 问题库FAQ导入配置
		List<String> learnWayList = new ArrayList<String>();
		rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库FAQ导入配置", serviceType);
		if(rs!=null && rs.getRowCount()>0){
			for (int n = 0; n < rs.getRowCount(); n++) {
				String value = rs.getRows()[n].get("name").toString();
				learnWayList.add(value);
			}
		}
		
		rs = CommonLibQueryManageDAO.getCityFromService(serviceid);
		String city = "";
		if (rs != null&&rs.getRowCount() > 0){
			city = rs.getRows()[0].get("city") == null ? "全国" : rs.getRows()[0].get("city").toString();
		}
		
		rs = null;
		rs = CommonLibQueryManageDAO.getInfoFromKbdataByServiceid(serviceid);
		
		if (rs != null&&rs.getRowCount() > 0){
			for (int i = 0;i < rs.getRowCount();i++){
				map.put(rs.getRows()[i].get("abstract"),rs.getRows()[i].get("kbdataid"));
				map2.put(rs.getRows()[i].get("kbdataid"),rs.getRows()[i].get("city"));
			}
		}
		
		int count = -1;
		int index = 0;
		// 遍历每一行
		for (List<Object> line : info){
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
			if ("".equals(kbdata) || "".equals(answer) || "".equals(channel)){
				returnMsg = returnMsg + "<br/>第" + index + "条FAQ存在空白列";
				continue;
			}
			List<String> configValueList = new ArrayList<String>();
			if (channelList.contains(channel)){
				configValueList.add(channel);
			} else {
				returnMsg = returnMsg + "<br/>第" + index + "条FAQ渠道不合法";
				continue;
			}
			
			// 标准问不存在
			if (!map.containsKey("<" + service + ">"+kbdata)){
				String kbdataid = "";
				kbdataid = String.valueOf(ConstructSerialNum.GetOracleNextValNew("SEQ_KBDATA_ID", bussinessFlag));
				String querymanageId = "";
				querymanageId = ConstructSerialNum.GetOracleNextValNew("seq_querymanage_id",bussinessFlag);
				count = CommonLibQueryManageDAO.insertKbdata(user, serviceid, service, kbdataid, kbdata, answer, city, configValueList ,serviceType ,querymanageId);
				if (count > 0){
					if (learnWay.contains("意图识别") && learnWayList.contains("意图识别")){ //继承
						Map<String, String> learnWayMap = new HashMap<String, String>();
//						String cityName = "全国";
						String cityName = "";
						if(!"全国".equals(city)){
							String[] cityArray=city.split(",");
							for (int i=0;i<cityArray.length;i++){
								cityName=cityName+cityCodeToCityName.get(cityArray[i])+",";
							}
							cityName=cityName.substring(0, cityName.lastIndexOf(","));
						}else{
							cityName = "全国";
						}
						learnWayMap.put(kbdataid+"--"+cityName, "<"+service+">"+kbdata);
						Object obj = KbdataAttrDAO.inheritAllAbstract(learnWayMap);
					}
					if (learnWay.contains("深度学习") && learnWayList.contains("深度学习")){
						Object obj = QuerymanageDAO.produceWordpat(city + "@#@" + kbdata + "@#@" + kbdataid + "@#@" + querymanageId,request);
					}
					if (learnWay.contains("语义文法") && learnWayList.contains("语义文法")){
//						JSONObject jsonObj = new JSONObject();
//						jsonObj.put("autowordpat", kbdata);
//						jsonObj.put("channel", channel);
//						Object obj = WordpatDAO.AutoGenerateWordpat2(JSONObject.parseObject(jsonObj.toString(), InsertOrUpdateParam.class));
//						JSONObject ReJsonObj = new JSONObject();
//						ReJsonObj = JSONObject.parseObject(obj.toString());
//						String wordpat = ReJsonObj.get("result").toString().split("@_@")[0];
////						String s_wordpat = "";
//						// 将简单词模转化为普通词模，并返回转换结果
//						s_wordpat = SimpleString.SimpleWordPatToWordPat(wordpat);
						
//						String[] cityArray=city.split(",");
//						String cityCode = "全国";
//						if(cityArray.length>0){
//							cityCode = cityArray[0];
//						}
//						List<String> list  = WordpatDAO.AutoGenerateOrdinaryWordpat(kbdata,cityCode);
//						if(list!=null &list.size()>0){
//						 CommonLibQueryManageDAO.insertbyExcel(list.get(0),city.replace(",", "|"),kbdataid,user);
//						}
						
						List<List<String>> list = new ArrayList<List<String>>();
						String userid = user.getUserID();
						// 获取行业
						String servicetype = user.getIndustryOrganizationApplication();
						String url ="";
						String provinceCode="全国";
						
						String queryCityCode = city;
						if ("全国".equals(queryCityCode)||"".equals(queryCityCode) || queryCityCode == null) {
							queryCityCode = "全国";
							url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
						} else {
							queryCityCode = queryCityCode.replace(",", "|");
							provinceCode = queryCityCode.split("\\|")[0];
							provinceCode = provinceCode.substring(0,2)+"0000";
							if("010000".equals(provinceCode)||"000000".equals(provinceCode)){//如何为集团、电渠编码 去默认url
								url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
							}else{
								url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvinceCode(provinceCode);
							}
						}
						
						//测试使用
						//url = "http://180.153.59.28:8082/NLPWebService/NLPCallerWS?wsdl";
						
						// 获取高级分析的接口串中的serviceInfo
						String serviceInfo = MyUtil.getServiceInfo(servicetype, "问题生成词模", "",
								false,queryCityCode);
						String query = kbdata;
						String queryid = querymanageId;
						// 获取高级分析的串
						String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模",
								kbdata, servicetype, serviceInfo);
						logger.info("问题库自学习词模调用【"+GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode)+"】接口地址：" + url);
						logger.info("问题库自学习词模接口的输入串：" + queryObject);
						// 调用生成词模的接口生成词模,可能是多个，以@_@分隔
						String wordpat = QuerymanageDAO.getWordpat(queryObject,url);
//							logger.info("问题库自学习词模：" + wordpat);
						if (wordpat != null && !"".equals(wordpat)) {
							// 判断词模是否含有@_@
							if (wordpat.contains("@_@")) {
								// 有的话，按照@_@进行拆分,并只取第一个
								wordpat = wordpat.split("@_@")[0];
							}
							// 获取词模中@前面的词模题，在加上@2#编者="问题库"&来源="(当前问题)"
//								wordpat = wordpat.split("@")[0] + "@2#编者=\"问题库\"&来源=\""
//										+ query.replace("&", "\\and") + "\"";
							
			                //保留自学习词模返回值，并替换 编者=\"自学习\""=>编者="问题库"&来源="(当前问题)" ---> modify 2017-05-24
							wordpat = wordpat.replace("编者=\"自学习\"", "编者=\"问题库\"&来源=\""+ query.replace("&", "\\and") + "\"");
							
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
							 CommonLibQueryManageDAO.insertWordpat(list, servicetype, userid,"0");
						}
						
//						Object obj = produceWordpat(city + "@#@" + kbdata + "@#@" + kbdataid + "@#@" + querymanageId,request);
						
					}
					map.put("<" + service + ">"+kbdata,kbdataid);
					map2.put(kbdataid,city);
				} else {
					returnMsg = returnMsg + "<br/>第" + index + "条FAQ插入失败";
				}
			}else {// 标准问已存在
				city  = map2.get(map.get("<" + service + ">"+kbdata))==null ? "全国" : map2.get(map.get("<" + service + ">"+kbdata)).toString();
				String kbdataid = map.get("<" + service + ">"+kbdata).toString();
				count = CommonLibQueryManageDAO.updateKbdataAnswer(user, serviceid, service, kbdata, answer, city, configValueList ,serviceType, kbdataid);
				if (count < 1){
					returnMsg = returnMsg + "<br/>第" + index + "条FAQ插入失败";
				}
			}
		}
		ExtendDao.updateKB();
		return returnMsg;
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
		String extension = filename.lastIndexOf(".") == -1 ? "" : filename
				.substring(filename.lastIndexOf(".") + 1);
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
		 String cloum1 =info.get(0).get(0)+"";
		 if(info.size()==1 && "业务词".equals(cloum1)){
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "文件内容为空!");
				return jsonObj;
		}else{
			if("业务词".equals(cloum1)){//忽略Excel列名
			 info.remove(0);
			}
			
			String count = CommonLibQueryManageDAO.getWordInsert(user,info);
			
			if(count.contains("成功")){ 
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", count);	
			}else{
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
	 * 导入语义
	 * @return
	 */
	public static Object importKBData(String filename, String serviceid){
		JSONObject jsonObject = new JSONObject();
		ImportService serviceRoot = new ImportService();
		Result rs = CommonLibServiceDAO.getServiceInfoByserviceid(serviceid);
		if(rs != null && rs.getRowCount() > 0){
			String parentId = Objects.toString(rs.getRows()[0].get("parentid"), "");
			if(!"0".equals(parentId) && !"0.0".equals(parentId)){
				jsonObject.put("success", false);
				jsonObject.put("msg", "请从根节点导入文件！");
				return jsonObject;
			}
			serviceRoot.setService(rs.getRows()[0].get("service").toString());
			serviceRoot.setCity(rs.getRows()[0].get("city").toString());
			serviceRoot.setBrand(rs.getRows()[0].get("brand").toString());
			serviceRoot.setServiceId(rs.getRows()[0].get("serviceid").toString());
			serviceRoot.setParentId(parentId);
		}else{
			jsonObject.put("success", false);
			jsonObject.put("msg", "根节点不存在！");
			return jsonObject;
		}
		// 获取文件的路径
		String pathName = regressTestPath + File.separator + filename;
		// 获取上传文件的file
		File file = new File(pathName);
		String extension = FilenameUtils.getExtension(file.getName());
		List<List<List<Object>>> list = null;
		if ("xls".equalsIgnoreCase(extension)) {
			// 读取2003版的Excel
			list = ReadExcel.read2003ExcelBySheet(file);
		} else if ("xlsx".equalsIgnoreCase(extension)) {
			// 读取2007版的Excel
			list = ReadExcel.read2007ExcelBySheet(file);
		}
		
		//TODO 检查和构建导入数据
		if(list == null || list.size() < 1){
			jsonObject.put("success", false);
			jsonObject.put("msg", "文件格式有问题或内容为空!");
			return jsonObject;
		}
		//获取第一个sheet，导入词模文件
		List<List<Object>> sheetInfo = list.get(0);

		//获取第二个sheet，导入客户问文件
		List<List<Object>> sheetInfo2 = list.get(1);
		
		if(sheetInfo == null 
				|| sheetInfo.size() < 1
				|| sheetInfo.get(0) == null 
				|| (sheetInfo.size() == 1 
					&& "目录路径".equals(sheetInfo.get(0).get(0)))){
			jsonObject.put("success", false);
			jsonObject.put("msg", "词模工作表内容为空!");
			return jsonObject;
		}
		if(sheetInfo2 == null 
				|| sheetInfo2.size() < 1
				|| sheetInfo2.get(0) == null 
				|| (sheetInfo2.size() == 1 
					&& "目录路径".equals(sheetInfo2.get(0).get(0)))){
			jsonObject.put("success", false);
			jsonObject.put("msg", "客户问工作表内容为空!");
			return jsonObject;
		}
		
		//去除标题
		sheetInfo.remove(0);
		sheetInfo2.remove(0);
		
		if(sheetInfo.size() > 200){
			jsonObject.put("success", false);
			jsonObject.put("errorCode","1"); // 导入词模数量限制
			return jsonObject;
		}
		if(sheetInfo2.size() > 200){
			jsonObject.put("success", false);
			jsonObject.put("errorCode","2"); // 导入客户问数量限制
			return jsonObject;
		}
		
		//excel表头数据
		Map<String,List<String>> columnTitleMap = new HashMap<String,List<String>>();
		//excel表数据
		Map<String,List<List<Object>>> textMap = new HashMap<String,List<List<Object>>>();
		if(!checkImport(sheetInfo,sheetInfo2, serviceRoot)){
			columnTitleMap.put("导入词模", Arrays.asList("目录路径","标准问","词模","词模类型","错误原因"));
			columnTitleMap.put("导入客户问", Arrays.asList("目录路径","标准问","客户问","回复类型","交互类型","来源地市","错误原因"));
			textMap.put("导入词模", sheetInfo);
			textMap.put("导入客户问", sheetInfo2);
			String fileName = "importWordpat_";
			fileName += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			boolean isWritten = ReadExcel.writeExcelBySheet(FILE_PATH_EXPORT, fileName, columnTitleMap, textMap);
			if(isWritten){
//				file = new File(FILE_PATH_EXPORT + filename + ".xls");
				jsonObject.put("success", false);
				jsonObject.put("fileName", fileName+ ".xls");
				return jsonObject;
			}
		}
		logger.info("去重前的导入数据："+JSONObject.toJSON(serviceRoot));
		//TODO 去重和构建数据
		Stack<ImportService> stack = new Stack<ImportService>();
		stack.addAll(serviceRoot.getChildService().values());
		Map<String,ImportService> serviceMap = new HashMap<String,ImportService>();
		Set<String> wordpatSet = new HashSet<String>(); //当前导入的词模
		String brand = serviceRoot.getBrand();
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User)sre;
		
		// 获取行业
		String serviceRootStr = "'";
		if(user.getServiceRoot() != null){
			for(int i =0;i<user.getServiceRoot().length;i++){
				if(i == user.getServiceRoot().length -1){
					serviceRootStr+=user.getServiceRoot()[i];
				}else{
					serviceRootStr+=user.getServiceRoot()[i]+"','";
				}
			}
			serviceRootStr+="'";
		}
		
		String serviceType = user.getIndustryOrganizationApplication();
//		String serviceRootStr = "'个性化业务','电信集团模板业务','电信集团问题库','电信行业问题库'";
//		String serviceRootStr = "'测试商家问题库','测试商家模板业务'";
		rs = CommonLibMetafieldmappingDAO.getConfigValue("问题库语义导入配置", serviceType);
		boolean removeRepeat = true;
		if(rs != null && rs.getRowCount() > 0){
			for(int i =0 ;i<rs.getRowCount();i++){
				String name  = Objects.toString(rs.getRows()[i].get("name"), "");
				if(name.contains("去重")&& name.contains("否")){
					removeRepeat = false;
				}
			}
		}
		while (!stack.empty()) {
			ImportService pop = stack.pop();
			String curServiceId = pop.getServiceId();
			
			if(!StringUtils.isEmpty(curServiceId)){
				serviceMap.clear();
				rs = CommonLibServiceDAO.getChildServiceByParentID(curServiceId);
				if(rs != null && rs.getRowCount() > 0){
					for(int i =0 ;i<rs.getRowCount();i++){
						ImportService service = new ImportService(rs.getRows()[i]);
						serviceMap.put(service.getService(), service);
					}
				}
				//对子节点进行判重处理
				if(pop.getChildService() != null){
					for(ImportService importService : pop.getChildService().values()){
						ImportService service = serviceMap.get(importService.getService());
						if(service != null){
							importService.merge(service);
						}
					}
				}
				
				//当前节点下标准问判重
				if(pop.getKbDataMap() != null){
					Map<String, Map<String, String>> normalQueryDic = CommonLibQueryManageDAO.getNormalQueryDic(curServiceId);
					//标准问判重
					for(ImportKbData importKbData : pop.getKbDataMap().values()){
						Map<String, String> map = normalQueryDic.get(importKbData.getAbs());
						if(map != null){ //标准问重复
							ImportKbData originKbdata = new ImportKbData(map);
							importKbData.setServiceId(serviceid);
							importKbData.merge(originKbdata);
						}
						
						//客户问判重
						if(importKbData.getKbdataId() != null){
							Map<String, ImportQuery> queryMap = importKbData.getQueryMap();
							if(queryMap != null){
								importKbData.setQueryMap(null);
								Map<String, String> queryDic = CommonLibQueryManageDAO.getCustomerQueryDic(importKbData.getKbdataId(),0);
								for(ImportQuery importQuery : queryMap.values()){
									String query = importQuery.getQuery();
									//客户问不重复
									if(!queryDic.containsKey(query)){
										importKbData.addQuery(importQuery);
									}
								}
							}
						}
					}
				}
			}
			
			if(pop.getKbDataMap() != null){
				//词模判重，判重逻辑，
				for(ImportKbData importKbData : pop.getKbDataMap().values()){
					
					Map<String, ImportWordpat> wordpatMap = importKbData.getWordpatMap();
					if(wordpatMap == null){
						continue;
					}
					importKbData.setWordpatMap(null);//重置
					for(ImportWordpat importWordpat : wordpatMap.values()){
						String wordpat = importWordpat.getWordpat();
						// 1. 当前导入文件中是否重复
						if(removeRepeat && wordpatSet.contains(wordpat)){
							continue;
						}
						//如果配置不去重，则新增的标准问下所有词模都不进行重复判断
						if(!removeRepeat && StringUtils.isEmpty(importKbData.getKbdataId())){
							logger.info("导入词模："+wordpat);
							importKbData.addWordpat(importWordpat);
							continue;
						}
						// 2. 是否与数据库中重复
						Result exist = CommonLibWordpatDAO.exist(brand, importKbData.getKbdataId(), wordpat, serviceRootStr);
						if(exist != null && exist.getRowCount() > 0){
							wordpatSet.add(wordpat);
							continue;
						}
						logger.info("导入词模："+wordpat);
						//3.不重复
						wordpatSet.add(wordpat);
						importKbData.addWordpat(importWordpat);
					}
				}
			}
			//对子节点进行判重处理
			if(pop.getChildService() != null){
				stack.addAll(pop.getChildService().values());
			}
			
		}
		logger.info("去重后的导入数据："+JSONObject.toJSON(serviceRoot));
		int execImport = execImport(serviceRoot);
		if(execImport > 0){
			jsonObject.put("success", true);
			jsonObject.put("msg", "导入成功");
		}else{
			jsonObject.put("success", false);
			jsonObject.put("msg", "导入失败");
		}
		return jsonObject;
	}
	private static int execImport(ImportService serviceRoot){
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User)sre;
		String userId = user.getUserID();
		String serviceType = user.getIndustryOrganizationApplication();
//		String userId = "179";
//		String serviceType = "电信行业->电信集团->4G业务客服应用";
		 // 获得商家标识符
		String bussinessFlag = CommonLibMetafieldmappingDAO
				.getBussinessFlag(serviceType);
		List<String> cityList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
				.resourseAccess(userId, "querymanage", "S");
		// 该操作类型用户能够操作的资源
		cityList = resourseMap.get("地市");
		logger.info("用户【"+userId+"】导入语义，商家为【"+serviceType+"】，用户地市为【"+cityList+"】");
		
		boolean isOracle = GetConfigValue.isOracle;
		
		List<ImportService> serviceData = new ArrayList<ImportService>();
		List<ImportKbData> kbdata = new ArrayList<ImportKbData>();
		List<ImportWordpat> wordpatData = new ArrayList<ImportWordpat>();
		List<ImportQuery> queryData = new ArrayList<ImportQuery>();
		
		Stack<ImportService> stack = new Stack<ImportService>();
		stack.addAll(serviceRoot.getChildService().values());
		while (!stack.empty()) {
			ImportService pop = stack.pop();
			String curServiceId = pop.getServiceId();
			//新增业务
			if(StringUtils.isEmpty(curServiceId)){
				if (isOracle) {
					curServiceId = ConstructSerialNum.GetOracleNextValNew(
							"SEQ_SERVICE_ID", bussinessFlag);
				} else if (GetConfigValue.isMySQL) {
					curServiceId = ConstructSerialNum.getSerialIDNew("kbdata",
							"kbdataid", bussinessFlag);
				}
				pop.setServiceId(curServiceId);
				serviceData.add(pop);
			}
			if(pop.getKbDataMap() != null){
				//新增摘要
				for(ImportKbData importKbData : pop.getKbDataMap().values()){
					String kbdataId = importKbData.getKbdataId();
					//新增摘要
					if(StringUtils.isEmpty(kbdataId)){
						if (isOracle) {
							kbdataId = ConstructSerialNum.GetOracleNextValNew(
									"SEQ_KBDATA_ID", bussinessFlag);
						} else if (GetConfigValue.isMySQL) {
							kbdataId = ConstructSerialNum.getSerialIDNew("kbdata",
									"kbdataid", bussinessFlag);
						}
						importKbData.setCity(pop.getCity());
						importKbData.setServiceId(curServiceId);
						importKbData.setKbdataId(kbdataId);
						importKbData.setService(pop);
						ImportQuery kbDataQuery = new ImportQuery(); 
						kbDataQuery.setQuery(importKbData.getAbs());
						importKbData.addQuery(kbDataQuery);// 添加与标准问同名的客户问
						kbdata.add(importKbData);
					}
					Map<String, ImportWordpat> wordpatMap = importKbData.getWordpatMap();
					if(wordpatMap != null){
						for(ImportWordpat importWordpat : wordpatMap.values()){
							String wordpatId = null;
							if (isOracle) {
								wordpatId = ConstructSerialNum .GetOracleNextValNew("SEQ_WORDPATTERN_ID", bussinessFlag);
							} else if (GetConfigValue.isMySQL) {
								wordpatId = ConstructSerialNum.getSerialIDNew( "wordpat", "wordpatid", bussinessFlag);
							}
							importWordpat.setWordpatId(wordpatId);
							importWordpat.setKbdataId(kbdataId);
							importWordpat.setBrand(pop.getBrand());
							//全国的摘要的词模地市是当前用户的地市
							if("".equals(importKbData.getCity())||"全国".equals(importKbData.getCity())){
								importWordpat.setCity(StringUtils.join( cityList.toArray(),"|"));
							}else{
								importWordpat.setCity(importKbData.getCity().replace(",", "|"));
							}
//							importWordpat.setCity(importKbData.getCity());
						}
						wordpatData.addAll(wordpatMap.values());
					}
					Map<String, ImportQuery> queryMap = importKbData.getQueryMap();
					if(queryMap != null){
						for(ImportQuery importQuery : queryMap.values()){
							String queryId = null;
							if (isOracle) {
								queryId = ConstructSerialNum .GetOracleNextValNew("seq_querymanage_id", bussinessFlag);
							} else if (GetConfigValue.isMySQL) {
								queryId = ConstructSerialNum.getSerialIDNew( "querymanage", "id", bussinessFlag);
							}
							importQuery.setQueryId(queryId);
							if(StringUtils.isEmpty(importQuery.getCity())){
								importQuery.setCity(importKbData.getCity());
							}
							importQuery.setKbdataId(kbdataId);
						}
						queryData.addAll(queryMap.values());
					}
				}
			}
			if(pop.getChildService() != null){
				for(ImportService importService : pop.getChildService().values()){
					importService.setParentId(curServiceId);
					if(StringUtils.isEmpty(importService.getServiceId())){
						importService.setBrand(pop.getBrand());
						importService.setCity(pop.getCity());
						importService.setCityid(pop.getCityid());
					}
					stack.push(importService);
				}
			}
		}
		//执行导入，批量导入
		String serviceSql = "insert into service(serviceid, service, parentid, parentname, brand, cityid,city) values(?,?,?,?,?,?,?)";

		List<String> sqlList = new ArrayList<String>();
		List<List<?>> paramList = new ArrayList<List<?>>();
		for(ImportService importService :serviceData){
			List<Object> param = new ArrayList<Object>();
			param.add(importService.getServiceId());
			param.add(importService.getService());
			param.add(importService.getParentId());
			param.add(importService.getParentName());
			param.add(importService.getBrand());
			param.add(importService.getCityid());
			param.add(importService.getCity());
			paramList.add(param);
			sqlList.add(serviceSql);
			logger.info("导入业务数据："+param);
		}
		String kbdataSql = "insert into kbdata(serviceid,kbdataid,topic,abstract,city,responsetype,interacttype) values (?,?,?,?,?,?,?)";
		for(ImportKbData importKbData :kbdata){
			List<Object> param = new ArrayList<Object>();
			param.add(importKbData.getServiceId());
			param.add(importKbData.getKbdataId());
			param.add("常见问题");
			String abs =  "<" + importKbData.getService().getService() + ">" + importKbData.getAbs();
			param.add(abs);
			param.add(importKbData.getCity());
			param.add(importKbData.getResponsetype());
			param.add(importKbData.getInteracttype());
			paramList.add(param);
			sqlList.add(kbdataSql);
			logger.info("导入摘要数据："+param);
		}
		String wordpatSql = null;
		if(isOracle){
			wordpatSql = "insert into wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand,edittime,workerid) values(?,?,?,?,?,?,?,sysdate,?)";
		}else{
			wordpatSql = "insert into wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand,edittime,workerid) values(?,?,?,?,?,?,?,now(),?)";
		}
		
		for(ImportWordpat importWordpat :wordpatData){
			List<Object> param = new ArrayList<Object>();
			param.add(importWordpat.getWordpatId());
			param.add(importWordpat.getWordpat());
			param.add(importWordpat.getCity());
			param.add("0");
			param.add(importWordpat.getWordpatType());
			param.add(importWordpat.getKbdataId());
			param.add(importWordpat.getBrand());
			param.add(userId);
			paramList.add(param);
			sqlList.add(wordpatSql);
			logger.info("导入词模数据："+param);
		}
		String querySql = "insert into querymanage(ID,KBDATAID,QUERY,CITY,WORKERID) values(?,?,?,?,?)";
		for(ImportQuery importQuery :queryData){
			List<Object> param = new ArrayList<Object>();
			param.add(importQuery.getQueryId());
			param.add(importQuery.getKbdataId());
			param.add(importQuery.getQuery());
			param.add(importQuery.getCity());
			param.add(userId);
			paramList.add(param);
			sqlList.add(querySql);
			logger.info("导入客户问数据："+param);
		}
		logger.info("导入语义总数："+sqlList.size());
		return Database.executeNonQueryTransaction(sqlList, paramList);
//		return -1;
	}
	private static boolean checkImport(List<List<Object>> sheetInfo,List<List<Object>> sheetInfo2,ImportService serviceRoot){
		boolean flag = true;
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User)sre;
		if(user == null){
			return false;
		}
		String userId = user.getUserID();
//		String userId = "179";
//		String path = "./WebRoot/";
		String path = ServletActionContext.getRequest().getServletContext().getRealPath("/");
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
				.resourseAccess(userId, "querymanage", "S");
		// 该操作类型用户能够操作的资源
		List<String> cityList = resourseMap.get("地市");
		
		Set<String> provinceSet  = new HashSet<String>();
		for(String cityStr : cityList){
			if(cityStr.length() == 6){
				provinceSet.add(cityStr.substring(2)+"0000");
			}else{
				provinceSet.add(cityStr);
			}
		}
		
		Map<String, String> wordpatType = QuerymanageDAO.wordpatType;
		Map<String,ImportService> serviceMap = new HashMap<String,ImportService>();
//		List<List<Object>> result = new ArrayList<List<Object>>();
		Result rs = CommonLibServiceDAO.getChildServiceByParentID(serviceRoot.getServiceId());
//		Set<String> serviceSet = new HashSet<String>();
		if(rs != null && rs.getRowCount() > 0){
			for(int i =0 ;i<rs.getRowCount();i++){
				ImportService service = new ImportService(rs.getRows()[i]);
//				serviceRoot.addChildService(service);
				serviceMap.put(service.getService(), service);
			}
		}
		
		int count = 4;
		Iterator<List<Object>> iterator = sheetInfo.iterator();
		while (iterator.hasNext()) {
			List<Object> list = (List<Object>) iterator.next();
			if(list.size() != count){
				list.add(count, "数据列不符合规则！");
				flag = false;
				continue;
			}
			String servicePath = Objects.toString(list.get(0),"");
			if(StringUtils.isEmpty(servicePath)){
				list.add(count, "业务路径不能为空！");
				flag = false;
				continue;
			}
			String abs = Objects.toString(list.get(1),"");
			if(StringUtils.isEmpty(abs)){
				list.add(count, "摘要不能为空！");
				flag = false;
				continue;
			}
			if(abs.length() >= 50){
				list.add(count, "摘要不能超过50个字！");
				flag = false;
				continue;
			}
			String wordpat = Objects.toString(list.get(2),"");
			if(StringUtils.isEmpty(wordpat)){
				list.add(count, "词模不能为空！");
				flag = false;
				continue;
			}
			String wordpattype = Objects.toString(list.get(3),"");
			if(StringUtils.isEmpty(wordpattype)){
				list.add(count, "词模类型不能为空！");
				flag = false;
				continue;
			}
			String type = null;
			for(Map.Entry<String, String> entry : wordpatType.entrySet()){
				if(entry.getValue().equals(wordpattype)){
					type = entry.getKey();
				}
			}
			if(StringUtils.isEmpty(type)){
				list.add(count, "词模类型不存在！");
				flag = false;
				continue;
			}
			String[] split = servicePath.split("-");
			if(!serviceMap.containsKey(split[0])){
				list.add(count, "业务路径首节点必须在系统中存在！");
				flag = false;
				continue;
			}
			ImportService node = serviceMap.get(split[0]);
			if(StringUtils.isEmpty(node.getCity())){
				list.add(count, "业务路径首节点地市不存在！");
				flag = false;
				continue;
			}
			List<String> list2 = Arrays.asList(StringUtils.split(node.getCity(), ","));
			String city = list2.get(0);
			if(city.length() == 6){
				city = city.substring(0,2)+"0000";
			}
			if(!(provinceSet.contains("全国") || list2.contains("全国"))){ //非全国用户和全国节点
				if(!provinceSet.contains(city)){ //省账号不能导入其他省份数据
					list.add(count, "不能导入其他省份数据！");
					flag = false;
					continue;
				}
			}

			try {
				// 获取Web服务器上指定的虚拟路径对应的物理文件路径
				wordpat = SimpleString.SimpleWordPatToWordPat(wordpat);
				// 判断转换结果是否含有checkInfo
				if (wordpat.indexOf("checkInfo") != -1) {
					// 将信息赋值给checkInfo变量
					list.add(count, wordpat.split("=>")[1]);
					// 词模有误
					flag = false;
					continue;
				}
				CheckInforef curcheckInfo = new CheckInforef();
				// 调用词模检查函数
				if (!CheckInput.CheckGrammer(path, wordpat, 0, curcheckInfo)){
					list.add(count, curcheckInfo.curcheckInfo);
					// 词模有误
					flag = false;
					continue;
				}
					
			} catch (Exception ex) {
				ex.printStackTrace();
				// 检查过程中出现异常，则报错
				flag = false;
				list.add(count,"模板语法有误！");
				continue;
			}
			ImportService curService = serviceRoot.addChildService(serviceMap.get(split[0]));
			for(int i =1 ;i<split.length ;i++){
				String service = split[i];
				ImportService childService = curService.getChildService(service);
				if(childService == null){
					childService = new ImportService();
					childService.setService(service);
					curService.addChildService(childService);
				}
				curService = childService;
			}
			ImportKbData kbData = curService.getKbdata(abs);
			if(kbData == null){
				kbData = new ImportKbData();
				kbData.setAbs(abs);
				curService.addKbData(kbData);
			}
			ImportWordpat importWordpat = new ImportWordpat();
			importWordpat.setWordpat(wordpat);
			importWordpat.setWordpatType(type);
			kbData.addWordpat(importWordpat);
		}
		
		Iterator<List<Object>> iterator2 = sheetInfo2.iterator();
		count = 6;
		while (iterator2.hasNext()) {
			List<Object> list = (List<Object>) iterator2.next();
			if(list.size() != count){
				list.add(count, "数据列不符合规则！");
				flag = false;
				continue;
			}
			String servicePath = Objects.toString(list.get(0),"");
			if(StringUtils.isEmpty(servicePath)){
				list.add(count, "业务路径不能为空！");
				flag = false;
				continue;
			}
			String abs = Objects.toString(list.get(1),"");
			if(StringUtils.isEmpty(abs)){
				list.add(count, "摘要不能为空！");
				flag = false;
				continue;
			}
			if(abs.length() >= 50){
				list.add(count, "摘要不能超过50个字！");
				flag = false;
				continue;
			}
			String query = Objects.toString(list.get(2),"");
			if(StringUtils.isEmpty(query)){
				list.add(count, "客户问不能为空！");
				flag = false;
				continue;
			}
			if(query.length() >= 50){
				list.add(count, "客户问不能超过50个字！");
				flag = false;
				continue;
			}
			String responsetype = Objects.toString(list.get(3),"").trim();
			String interacttype = Objects.toString(list.get(4),"").trim();
			responsetype = QuerymanageDAO.getResponseType(responsetype);
			interacttype = QuerymanageDAO.getInteractType(interacttype);
			
			String queryCity = Objects.toString(list.get(5),"");
			if(StringUtils.isEmpty(queryCity)){
				list.add(count, "来源地市不能为空！");
				flag = false;
				continue;
			}
			
			
			String[] split = servicePath.split("-");
			if(!serviceMap.containsKey(split[0])){
				list.add(count, "业务路径首节点必须在系统中存在！");
				flag = false;
				continue;
			}
			
			ImportService node = serviceMap.get(split[0]);
			if(StringUtils.isEmpty(node.getCity())){
				list.add(count, "业务路径首节点地市不存在！");
				flag = false;
				continue;
			}
			
			List<String> list2 = Arrays.asList(StringUtils.split(node.getCity(), ","));
			String city = list2.get(0);
			if(city.length() == 6){
				city = city.substring(0,2)+"0000";
			}
			if(!(provinceSet.contains("全国") || list2.contains("全国"))){ //非全国用户和全国节点
				if(!provinceSet.contains(city)){ //省账号不能导入其他省份数据
					list.add(count, "不能导入其他省份数据！");
					flag = false;
					continue;
				}
			}
			
			//处理地市
			List<String> queryCityList = new ArrayList<String>();
			queryCity = queryCity.replace("省", "").replace("市", "");
			String cityArray[] = queryCity.split(",");
			for (int m = 0; m < cityArray.length; m++) {
				if (cityNameToCityCode.containsKey(cityArray[m])) {
					String cityCode = getCityCodeFromServiceCityCodes(list2, cityArray[m]);
					//扩展问地市来源不在业务地市范围内
					if(!StringUtils.isEmpty(cityCode)){
						queryCityList.add(cityCode);
					}
				}
			}
			if(queryCityList.size() < 1){
				list.add(count, "扩展问地市来源不在业务地市范围内！");
				flag = false;
				continue;
			}
			ImportService curService = serviceRoot.addChildService(serviceMap.get(split[0]));
			for(int i =1 ;i<split.length ;i++){
				String service = split[i];
				ImportService childService = curService.getChildService(service);
				if(childService == null){
					childService = new ImportService();
					childService.setService(service);
					curService.addChildService(childService);
				}
				curService = childService;
			}
			ImportKbData kbData = curService.getKbdata(abs);
			if(kbData == null){
				kbData = new ImportKbData();
				kbData.setAbs(abs);
				kbData.setInteracttype(interacttype);
				kbData.setResponsetype(responsetype);
				curService.addKbData(kbData);
			}
			ImportQuery queryEntity = new ImportQuery();
			queryEntity.setQuery(query);
			queryEntity.setCity(StringUtils.join(queryCityList,","));
			kbData.addQuery(queryEntity);
		}
		return flag;
	}
	public static void main(String[] args) {
		Object kbData = importKBData("质检语义词模、客户问导入模板 - 副本.xlsx", "1831954");
//		Object kbData = importKBData("质检语义词模、客户问导入模板.xlsx", "1834873.7");
		
		System.out.println(kbData);
	}
}
