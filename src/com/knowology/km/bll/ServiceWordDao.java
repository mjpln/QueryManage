package com.knowology.km.bll;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibNewWordInfoDAO;
import com.knowology.bll.CommonLibServiceWordDao;
import com.knowology.bll.CommonLibWordDAO;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.util.GetSession;

public class ServiceWordDao {

	public static Object getStandardWord(String ioa, String wordclass, String word, int rows, int page) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 电信行业
		String band="";
		// 查询配置
		Result rs2 = UserOperResource.getConfigValue("行业业务词对应父类配置", ioa);
		if (rs2 != null && rs2.getRowCount() > 0) {
			// 获取配置表的ip
			band = rs2.getRows()[0].get("name").toString();
		}
		if (wordclass != null && !"".equals(wordclass)){
			band = wordclass;
		}
		// 获取总数
		Result rs =  CommonLibServiceWordDao.getStandardWordCount(band,word);
		if (rs != null && rs.getRowCount() > 0){
			jsonObj.put("total", rs.getRows()[0].get("total").toString());
			// 分页查询标准词
			rs = CommonLibServiceWordDao.getStandardWord(band, word, rows, page);
			for (int i = 0;i < rs.getRowCount();i++){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("wordid", rs.getRows()[i].get("wordid"));
				jsonObject.put("stdwordid", rs.getRows()[i].get("stdwordid"));
				jsonObject.put("rn", rs.getRows()[i].get("rn"));
				jsonObject.put("word", rs.getRows()[i].get("word"));
				jsonObject.put("type", rs.getRows()[i].get("type"));
				jsonObject.put("wordclass", rs.getRows()[i].get("wordclass"));
				jsonObject.put("wordclassid", rs.getRows()[i].get("wordclassid"));
				jsonObject.put("citycode", rs.getRows()[i].get("city"));
				jsonObject.put("cityname", rs.getRows()[i].get("cityname"));
				
				jsonArr.add(jsonObject);
			}
			jsonObj.put("rows", jsonArr);
		} else {
			jsonObj.put("total", "0");
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	public static Object delStandardWord(String wordid) {
		JSONObject jsonObj = new JSONObject();
		int rs = -1;
		rs = CommonLibServiceWordDao.delStandardWord(wordid);
		if (rs > -1){
			jsonObj.put("success", true);
			jsonObj.put("msg", "删除词条成功！");
		} else{
			jsonObj.put("success", false);
			jsonObj.put("msg", "删除词条失败！");
		}
		return jsonObj;
	}

	public static Object getOtherWordByStandardWord(String ioa, String wordclass, String wordid, int rows,
			int page) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
//		// 电信行业
//		String band="";
//		// 查询配置
//		Result rs2 = UserOperResource.getConfigValue("行业业务词对应父类配置", ioa);
//		if (rs2 != null && rs2.getRowCount() > 0) {
//			// 获取配置表的ip
//			band = rs2.getRows()[0].get("name").toString();
//		}
		// 获取总数
		Result rs =  CommonLibServiceWordDao.getOtherWordCount(wordid);
		if (rs != null && rs.getRowCount() > 0){
			jsonObj.put("total", rs.getRows()[0].get("total").toString());
			// 分页查询标准词
			rs = CommonLibServiceWordDao.getOtherWord(wordid, rows, page);
			for (int i = 0;i < rs.getRowCount();i++){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("wordid", rs.getRows()[i].get("wordid"));
				jsonObject.put("stdwordid", rs.getRows()[i].get("stdwordid"));
				jsonObject.put("rn", rs.getRows()[i].get("rn"));
				jsonObject.put("word", rs.getRows()[i].get("word"));
				jsonObject.put("type", rs.getRows()[i].get("type"));
				jsonObject.put("wordclass", rs.getRows()[i].get("wordclass"));
				jsonObject.put("wordclassid", rs.getRows()[i].get("wordclassid"));
				jsonObject.put("citycode", rs.getRows()[i].get("city"));
				jsonObject.put("cityname", rs.getRows()[i].get("cityname"));
				
				jsonArr.add(jsonObject);
			}
			jsonObj.put("rows", jsonArr);
		} else {
			jsonObj.put("total", "0");
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	public static Object selOtherWord(String ioa, String wordclass, String word, int rows,
			int page) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 电信行业
		String band="";
		// 查询配置
		Result rs2 = UserOperResource.getConfigValue("行业业务词对应父类配置", ioa);
		if (rs2 != null && rs2.getRowCount() > 0) {
			// 获取配置表的ip
			band = rs2.getRows()[0].get("name").toString();
		}
		if (wordclass != null && !"".equals(wordclass)){
			band = wordclass;
		}
		// 获取总数
		Result rs =  CommonLibServiceWordDao.getOtherWordCount(band,word);
		if (rs != null && rs.getRowCount() > 0){
			jsonObj.put("total", rs.getRows()[0].get("total").toString());
			// 分页查询标准词
			rs = CommonLibServiceWordDao.getotherWord(band, word, rows, page);
			for (int i = 0;i < rs.getRowCount();i++){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("wordid", rs.getRows()[i].get("wordid"));
				jsonObject.put("stdwordid", rs.getRows()[i].get("stdwordid"));
				jsonObject.put("rn", rs.getRows()[i].get("rn"));
				jsonObject.put("word", rs.getRows()[i].get("word"));
				jsonObject.put("type", rs.getRows()[i].get("type"));
				jsonObject.put("wordclass", rs.getRows()[i].get("wordclass"));
				jsonObject.put("wordclassid", rs.getRows()[i].get("wordclassid"));
				jsonObject.put("citycode", rs.getRows()[i].get("city"));
				jsonObject.put("cityname", rs.getRows()[i].get("cityname"));
				
				jsonArr.add(jsonObject);
			}
			jsonObj.put("rows", jsonArr);
		} else {
			jsonObj.put("total", "0");
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	public static Object getStandardWordByOtherWord(String ioa, String wordclass, String word,
			int rows, int page) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 电信行业
		String band="";
		// 查询配置
		Result rs2 = UserOperResource.getConfigValue("行业业务词对应父类配置", ioa);
		if (rs2 != null && rs2.getRowCount() > 0) {
			// 获取配置表的ip
			band = rs2.getRows()[0].get("name").toString();
		}
		if (wordclass != null && !"".equals(wordclass)){
			band = wordclass;
		}
		// 获取总数
		Result rs =  CommonLibServiceWordDao.getStandardWordByOtherWordCount(band,word);
		if (rs != null && rs.getRowCount() > 0){
			jsonObj.put("total", rs.getRows()[0].get("total").toString());
			// 分页查询标准词
			rs = CommonLibServiceWordDao.getStandardWordByOtherWord(band, word, rows, page);
			for (int i = 0;i < rs.getRowCount();i++){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("wordid", rs.getRows()[i].get("wordid"));
				jsonObject.put("stdwordid", rs.getRows()[i].get("stdwordid"));
				jsonObject.put("rn", rs.getRows()[i].get("rn"));
				jsonObject.put("word", rs.getRows()[i].get("word"));
				jsonObject.put("type", rs.getRows()[i].get("type"));
				jsonObject.put("wordclass", rs.getRows()[i].get("wordclass"));
				jsonObject.put("wordclassid", rs.getRows()[i].get("wordclassid"));
				jsonObject.put("citycode", rs.getRows()[i].get("city"));
				jsonObject.put("cityname", rs.getRows()[i].get("cityname"));
				
				jsonArr.add(jsonObject);
			}
			jsonObj.put("rows", jsonArr);
		} else {
			jsonObj.put("total", "0");
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	public static Object getStandardWordClass(String ioa) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 查询配置
		Result rs = UserOperResource.getConfigValue("行业业务词对应父类配置", ioa);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0;i < rs.getRowCount();i++){
				jsonObj = new JSONObject();
				jsonObj.put("id",rs.getRows()[i].get("name").toString());
				jsonObj.put("text",rs.getRows()[i].get("name").toString());
				jsonArr.add(jsonObj);
			}
		}
		return jsonArr;
	}
	
	public static Object getServiceWord(String serviceword, int rows, int page){
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 获取总数
		Result rs =  CommonLibNewWordInfoDAO.listServiceWordCount(serviceword, null);
		if (rs != null && rs.getRowCount() > 0){
			jsonObj.put("total", rs.getRows()[0].get("total").toString());
			// 分页查询标准词
			rs = CommonLibNewWordInfoDAO.listServiceWord(serviceword, null, rows, page);
			for (int i = 0;i < rs.getRowCount();i++){
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("businessid", rs.getRows()[i].get("businessid"));
				jsonObject.put("serviceword", rs.getRows()[i].get("newword"));
				jsonObject.put("rn", rs.getRows()[i].get("rn"));
				jsonObject.put("wordpatid", rs.getRows()[i].get("wordpatid"));
				jsonObject.put("wordclassid", rs.getRows()[i].get("wordclassid"));
				JSONArray otherword = new JSONArray();
				//根据词类查询别名
				if(rs.getRows()[i].get("newword") != null && rs.getRows()[i].get("wordclassid") != null){
					Result res = CommonLibWordDAO.getOtherWordByWordClass(rs.getRows()[i].get("wordclassid").toString(), rs.getRows()[i].get("newword").toString());
					if(res != null && res.getRowCount()> 0){
						for(int j=0;j<res.getRowCount();j++){
							otherword.add(res.getRows()[j].get("word").toString());
						}
					}

				}
				jsonObject.put("otherword", StringUtils.join(otherword,"|"));

				jsonArr.add(jsonObject);
			}
			jsonObj.put("rows", jsonArr);
		}else{
			jsonObj.put("total", "0");
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}
	/**
	 * 添加业务词
	 * @param serviceword
	 * @param otherword
	 * @return
	 */
	public static Object insertServiceWord(String serviceword,String otherword,String serviceid,HttpServletRequest request){
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String serviceType = user.getIndustryOrganizationApplication();
		if(StringUtils.isBlank(serviceword)){
			jsonObj.put("success", false);
			jsonObj.put("msg", "新增失败,必要参数为空！");
			return jsonObj;
		}
		//查询业务词是否存在
		Result rs = CommonLibNewWordInfoDAO.getNewWordInfo(serviceType, serviceword, "是");

		if (rs != null && rs.getRowCount() > 0){
			jsonObj.put("success", false);
			jsonObj.put("msg", "新增失败,业务词已存在！");
			return jsonObj;
		} 
		//新增词类别名
		//新增业务词别名
		String combition = serviceword+"# #"+otherword;
		JSONObject addobj =(JSONObject)QuerymanageDAO.addOtherWord(combition, false);

		if (!addobj.getBoolean("success")){
			jsonObj.put("success", false);
			jsonObj.put("msg", "新增失败,新增业务词别名失败！");
			return jsonObj;
		} 
	    String wordclassId = addobj.getString("wordclassId");
		//新增词模	    
		JSONObject updateObj = (JSONObject)QuerymanageDAO.addBusinessWordpat(serviceword, serviceid, request);
		
		if (!updateObj.getBoolean("success")){
			jsonObj.put("success", false);
			jsonObj.put("msg", "新增失败,新增业务词词模失败！");
			return jsonObj;
		}
	    //增加新词
		String newWords = serviceword +",是";
		int countNewWord = QuerymanageDAO.addNewWordInfo(newWords, updateObj.get("kbdataid").toString());
		if(countNewWord > 0){
			jsonObj.put("success", true);
			jsonObj.put("msg", "新增业务词成功！");
		}else{
			jsonObj.put("success", false);
			jsonObj.put("msg", "新增业务词失败！");
		}
		return jsonObj;
		
	}


}
