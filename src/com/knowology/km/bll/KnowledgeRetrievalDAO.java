package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.knowology.Bean.User;
import com.knowology.bll.CommonLibFaqDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.util.GetSession;

public class KnowledgeRetrievalDAO {

	public static JSONObject getKnowledge(String content, String type, String citySelect, int page, int rows){
		int start = (page-1) * rows + 1;
		int limit = rows-1;
		
		if("咨询".equals(type)){
			return KnowledgeRetrievalDAO.getKnowledgeByQuestion(content, citySelect, start, limit); 
		}else if("业务".equals(type)){
			return KnowledgeRetrievalDAO.getKnowledgeByService(content, citySelect, start, limit);
		}else if("标准问".equals(type)){
			return KnowledgeRetrievalDAO.getKnowledgeByAbstract(content, citySelect, start, limit);
		}else if("扩展问".equals(type)){
			return KnowledgeRetrievalDAO.getKnowledgeByExtendQuery(content, citySelect, start, limit);
		}else if("答案".equals(type)){
			return  KnowledgeRetrievalDAO.getKnowledgeByAnswer(content, citySelect, start, limit);
		}
		return null;
	}
	
	/**
	 *@description 通过摘要名查询关联知识
	 *@param sevicetype  四层结构串
	 *@param content 模糊查询内容
	 *@param start 起始记录数
	 *@param limit 间隔记录数
	 *@param serviceRoot  业务根串
	 *@param sevicecontainer 业务树所属标识 
	 *@return 
	 *@returnType JSONObject 
	 */
	public static JSONObject getKnowledgeByAbstract(String content, String citySelect, int start, int limit) {
		JSONObject result = new JSONObject();
		JSONArray arr = new JSONArray();
		
		User user = (User) GetSession.getSessionByKey("accessUser");
		String sevicetype = user.getIndustryOrganizationApplication();
		String rootServicesStr = getRootServicesStr(sevicetype); 
		
		// 获取允许的Service
//		List<String> permitServices = getPermitCityList(user, citySelect);
		
		int count  = UserOperResource.getKnowledgeByAbstractCount(sevicetype, content, rootServicesStr, "", user.getUserID());
			if (count>0) {
				JSONObject obj = new JSONObject();
				Result rs = UserOperResource.getKnowledgeByAbstract(sevicetype, content, rootServicesStr, "", user.getUserID(), start, limit);
				for (int i = 0; i < rs.getRows().length; i++) {
					Object o = rs.getRows()[i].get("SERVICEID");
					String serviceid = o == null ? "" : o.toString();
				    o = rs.getRows()[i].get("SERVICE");
					String service = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("ABSTRACT");
					String abstractstr = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("KBDATAID");
					String kbdataid = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("TOPIC");
					String toipc = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("ANSWERCONTENT");
					String answer = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("PARENTNAME");
					String pname = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("PARENTID");
					String pid = o == null ? "" : o.toString();
					obj.put("service", service);
					obj.put("serviceid", serviceid);
					obj.put("parentname", pname);
					obj.put("parentid", pid);
					obj.put("abstractstr", abstractstr);
					obj.put("kbdataid", kbdataid);
					obj.put("topic", toipc);
					obj.put("answer", answer);
					arr.add(obj);
				}
				result.put("total", count);
				result.put("rows", arr);
			} else {
				result.put("rows", arr);
				result.put("total", 0);
			}
		return  result;
	}

	
	/**
	 *@description  通过业务名查询关联知识
	 *@param sevicetype  四层结构串
	 *@param content 模糊查询内容
	 *@param start  起始记录数
	 *@param limit  间隔记录数
	 *@param serviceRoot 业务根串
	 *@param sevicecontainer  业务树所属标识 
	 *@return 
	 *@returnType JSONObject 
	 */
	public static JSONObject getKnowledgeByService(String content, String citySelect, int start, int limit) {
		JSONObject result = new JSONObject();
		JSONArray arr = new JSONArray();
		
		User user = (User) GetSession.getSessionByKey("accessUser");
		String sevicetype = user.getIndustryOrganizationApplication();
		String rootServicesStr = getRootServicesStr(sevicetype); 
		
		// 获取允许的Service
		//List<String> permitServices = getPermitCityList(user, citySelect);
		
		int count = UserOperResource.getKnowledgeByServiceCount(sevicetype, content, rootServicesStr, user.getUserID());
			if (count>0) {
				Result rs = UserOperResource.getKnowledgeByService(sevicetype, content, rootServicesStr, user.getUserID(), start, limit);
				JSONObject obj = new JSONObject();
				for (int i = 0; i < rs.getRows().length; i++) {
					Object o = rs.getRows()[i].get("SERVICEID");
					String serviceid = o == null ? "" : o.toString();
				    o = rs.getRows()[i].get("SERVICE");
					String service = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("ABSTRACT");
					String abstractstr = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("KBDATAID");
					String kbdataid = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("TOPIC");
					String toipc = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("ANSWERCONTENT");
					String answer = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("PARENTNAME");
					String pname = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("PARENTID");
					String pid = o == null ? "" : o.toString();
					obj.put("service", service);
					obj.put("serviceid", serviceid);
					obj.put("parentname", pname);
					obj.put("parentid", pid);
					obj.put("abstractstr", abstractstr);
					obj.put("kbdataid", kbdataid);
					obj.put("topic", toipc);
					obj.put("answer", answer);
					arr.add(obj);
				}
				result.put("total", count);
				result.put("rows", arr);
			
			} else {
				result.put("rows", arr);
				result.put("total", 0);
			}
		
		
		return  result;
	}
	
	/**
	 *@description  通过答案内容获得关联知识
	 *@param sevicetype 四层结构串
	 *@param content 模糊查询内容
	 *@param start 起始记录数
	 *@param limit 间隔记录数
	 *@param serviceRoot 业务根串
	 *@param sevicecontainer 业务树所属标识 
	 *@return 
	 *@returnType JSONObject 
	 */
	public static JSONObject getKnowledgeByAnswer(String content, String citySelect, int start, int limit) {
		JSONObject result = new JSONObject();
		JSONArray arr = new JSONArray();
		
		User user = (User) GetSession.getSessionByKey("accessUser");
		String sevicetype = user.getIndustryOrganizationApplication();
		String rootServicesStr = getRootServicesStr(sevicetype); 
		
		// 获取允许的Service
//		List<String> permitServices = getPermitCityList(user, citySelect);
		
		Result rs = null;
		int count  =  UserOperResource.getKnowledgeByAnswerCount(sevicetype, content, rootServicesStr, user.getUserID());
			if (count >0 ) {
				rs  = UserOperResource.getKnowledgeByAnswer(sevicetype, content, start, limit, rootServicesStr, user.getUserID());
				JSONObject obj = new JSONObject();
				for (int i = 0; i < rs.getRows().length; i++) {
					Object o = rs.getRows()[i].get("SERVICEID");
					String serviceid = o == null ? "" : o.toString();
				    o = rs.getRows()[i].get("SERVICE");
					String service = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("ABSTRACT");
					String abstractstr = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("KBDATAID");
					String kbdataid = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("TOPIC");
					String toipc = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("ANSWERCONTENT");
					String answer = o == null ? "" : o.toString();
					
					o = rs.getRows()[i].get("PARENTNAME");
					String pname = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("PARENTID");
					String pid = o == null ? "" : o.toString();
					obj.put("service", service);
					obj.put("serviceid", serviceid);
					obj.put("parentname", pname);
					obj.put("parentid", pid);
					obj.put("abstractstr", abstractstr);
					obj.put("kbdataid", kbdataid);
					obj.put("topic", toipc);
					obj.put("answer", answer);
					arr.add(obj);
				}
				result.put("total", count);
				result.put("rows", arr);
			} else {
				result.put("rows", arr);
				result.put("total", 0);
			}
		
		return  result;
	}

	/**
	 *@description  通过问题理解后的在摘要获得关联知识
	 *@param sevicetype 四层结构串
	 *@param content 模糊查询内容
	 *@param start 起始记录数
	 *@param limit 间隔记录数
	 *@param serviceRoot 业务根串
	 *@param sevicecontainer 业务树所属标识 
	 *@return 
	 *@returnType JSONObject 
	 */
	public static JSONObject getKnowledgeByQuestion(String content, String citySelect, int start, int limit) {
		User user = (User) GetSession.getSessionByKey("accessUser");
		String sevicetype = user.getIndustryOrganizationApplication();
		String rootServicesStr = getRootServicesStr(sevicetype); 
		
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
				.resourseAccess(user.getUserID(), "querymanage", "S");
		List<String> userCityCodeList = resourseMap.get("地市");
		String userCity = "";
		String userProvince = "";
		if(userCityCodeList != null && userCityCodeList.size() > 0 ){
			if(userCityCodeList.contains("全国")){
				userProvince = "全国";
			}else{
				userProvince = QuerymanageDAO.getCityName(userCityCodeList.get(0)); // 省
				userCity = QuerymanageDAO.getCityName(userCityCodeList.get(1)); // 市
			}
		}
		
		String question = content.replace("%", "");
		List<String> allList = new ArrayList<String>();
		List<String> listChannel = getChanalList();
		String channel = "";
		List<String> list =null;
//		for(int i =0;i<listChannel.size();i++){
//			channel = listChannel.get(i);
			list = QATrainingDAO.KAnalyzeListAbs(user.getUserID(), userCity, userProvince, sevicetype, "", "Web", question);
		if(list.size()>0){
			allList.addAll(list);
			}
//		}
		Set<String> set = new HashSet<String>(allList);
		String abs = "";
		for (String str : set) {  
			abs +="'"+str+"',";
		} 
		abs = abs.substring(0,abs.lastIndexOf(","));
		JSONObject result = new JSONObject();
		JSONArray arr = new JSONArray();
		Result rs = null;
		
		// 获取允许的Service
//		List<String> permitServices = getPermitCityList(user, citySelect);
		
		int count = UserOperResource.getKnowledgeByAbstractCount(sevicetype, "", rootServicesStr, abs, user.getUserID());
			if (count>0) {
				result.put("rows", arr);
				result.put("total", 0);
				JSONObject obj = new JSONObject();
				rs = UserOperResource.getKnowledgeByAbstract(sevicetype, content, rootServicesStr, abs, user.getUserID(), start, limit);
				for (int i = 0; i < rs.getRows().length; i++) {
					Object o = rs.getRows()[i].get("SERVICEID");
					String serviceid = o == null ? "" : o.toString();
				    o = rs.getRows()[i].get("SERVICE");
					String service = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("ABSTRACT");
					String abstractstr = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("KBDATAID");
					String kbdataid = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("TOPIC");
					String toipc = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("ANSWERCONTENT");
					String answer = o == null ? "" : o.toString();
					
					o = rs.getRows()[i].get("PARENTNAME");
					String pname = o == null ? "" : o.toString();
					o = rs.getRows()[i].get("PARENTID");
					String pid = o == null ? "" : o.toString();
					obj.put("service", service);
					obj.put("serviceid", serviceid);
					obj.put("parentname", pname);
					obj.put("parentid", pid);
					obj.put("abstractstr", abstractstr);
					obj.put("kbdataid", kbdataid);
					obj.put("topic", toipc);
					obj.put("answer", answer);
					arr.add(obj);
				}
				result.put("total", count);
				result.put("rows", arr);
			} else {
				result.put("rows", arr);
				result.put("total", 0);
			}
		
		return  result;
	}
	
	public static JSONObject getKnowledgeByStatus(String servicetype,
			String content, int start, int limit, String serviceRoot,
			String sevicecontainer) {
		JSONObject result = new JSONObject();
		JSONArray arr = new JSONArray();
		Result rs = null;
		rs = UserOperResource.getKnowledgeByStatus(servicetype,content,serviceRoot,sevicecontainer);
		if (rs != null && rs.getRowCount() > 0){
			int count = rs.getRowCount();
			result.put("total", count);
			rs = UserOperResource.getKnowledgeByStatus(servicetype,content,start,limit,serviceRoot,sevicecontainer);
			for (int i = 0 ; i < rs.getRowCount() ; i++){
				JSONObject obj = new JSONObject();
				Object o = rs.getRows()[i].get("SERVICEID");
				String serviceid = o == null ? "" : o.toString();
			    o = rs.getRows()[i].get("SERVICE");
				String service = o == null ? "" : o.toString();
				o = rs.getRows()[i].get("ABSTRACT");
				String abstractstr = o == null ? "" : o.toString();
				o = rs.getRows()[i].get("TOPIC");
				String toipc = o == null ? "" : o.toString();
				o = rs.getRows()[i].get("ANSWERCONTENT");
				String answer = o == null ? "" : o.toString();
				
				o = rs.getRows()[i].get("PARENTNAME");
				String pname = o == null ? "" : o.toString();
				o = rs.getRows()[i].get("PARENTID");
				String pid = o == null ? "" : o.toString();
				obj.put("service", service);
				obj.put("serviceid", serviceid);
				obj.put("parentname", pname);
				obj.put("parentid", pid);
				obj.put("abstractstr", abstractstr);
				obj.put("topic", toipc);
				obj.put("answer", answer);
				arr.add(obj);
			}
			result.put("rows", arr);
		} else {
			result.put("total", 0);
		}
		return result;
	}
    
	/**
	 * 根据扩展问查找关联只是
	 * @param content
	 * @param citySelect
	 * @param start
	 * @param limit
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getKnowledgeByExtendQuery(String content, String citySelect, int start, int limit){
		JSONObject result = new JSONObject();
		JSONArray arr = new JSONArray();
		
		User user = (User) GetSession.getSessionByKey("accessUser");
		String sevicetype = user.getIndustryOrganizationApplication();
		String rootServicesStr = getRootServicesStr(sevicetype); 
		
		// 获取允许的Service
//		List<String> permitServices = getPermitCityList(user, citySelect);
		
		int count  =  UserOperResource.getKnowledgeByExtendQueryCount(content, sevicetype, rootServicesStr, user.getUserID());
		if(count > 0){
			Result rs = UserOperResource.getKnowledgeByExtendQuery(content, sevicetype, rootServicesStr, user.getUserID(), start, limit);
			result.put("total", count);
			for(SortedMap row : rs.getRows()){
				JSONObject obj = new JSONObject();
				obj.put("service", row.get("service") == null ? "" : row.get("service").toString());
				obj.put("serviceid", row.get("serviceid") == null ? "" : row.get("serviceid").toString());
				obj.put("parentid", row.get("parentid") == null ? "" : row.get("parentid").toString());
				obj.put("abstractstr", row.get("abstract") == null ? "" : row.get("abstract").toString());
				obj.put("kbdataid", row.get("kbdataid") == null ? "" : row.get("kbdataid").toString());
				obj.put("topic", row.get("topic") == null ? "" : row.get("topic").toString());
				obj.put("answer", row.get("answercontent") == null ? "" : row.get("answercontent").toString());
				arr.add(obj);
			}
			result.put("rows", arr);
		}else {
			result.put("total", 0);
		}
		
		return result;
	}
	
	
	
	/**
	 * 获取问题库根业务字符串
	 * @param servicetype
	 * @return
	 */
	private static String getRootServicesStr(String servicetype){
		// 根业务
		List<String> rootServiceList = new ArrayList<String>();
		rootServiceList.add("'电信垃圾问题库'");
		Result rootRs = CommonLibMetafieldmappingDAO.getConfigValue("问题库业务根对应关系配置", servicetype);
		if (rootRs != null && rootRs.getRowCount() > 0) {
			for (int i = 0; i < rootRs.getRowCount(); i++) {
				String rootService = rootRs.getRows()[i].get("name").toString();
				rootServiceList.add("'" + rootService + "'");
			}
		}
		return StringUtils.join(rootServiceList, ",");
	}
	
    /**
     *@description 获得配置表渠道信息 
     *@return 
     *@returnType List<String> 
     */
    public static List<String> getChanalList(){
		List<String> list  = new ArrayList<String>();
			//  获取相应的数据源
			Result rs = UserOperResource.getConfigValue("渠道参数配置", "渠道");
			// 判断数据源不为null，且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 将渠道放入jsonArr数组中
					list.add(rs.getRows()[i].get("name").toString());
				}
			}
		 return list;
 	}
    
    /**
     * 获取改用户允许的业务ID列表
     * @param user
     * @param citySelect
     * @return
     */
    private static List<String> getPermitCityList(User user, String citySelect){
		if(citySelect == null || "".equals(citySelect.trim())){
			citySelect = "全国";
		}
		List<String> cityList = new ArrayList<String>();
		List<String> serviceidList = new ArrayList<String>();
		cityList.add(citySelect);
		//获取用户业务树
		List<Map<String, String>> serviceInfos = CommonLibPermissionDAO.getServiceResource(user.getUserID(), "querymanage", user.getIndustryOrganizationApplication(), cityList);
		if (serviceInfos != null && serviceInfos.size() > 0) {
			for (int i = 0; i < serviceInfos.size(); i++) {
				String sid = serviceInfos.get(i).get("id");
				serviceidList.add(sid);
			}
		}
		
		return serviceidList;
    }
}
