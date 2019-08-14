package com.knowology.km.bll;

import javax.servlet.jsp.jstl.sql.Result;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.bll.CommonLibServiceWordDao;
import com.knowology.km.access.UserOperResource;

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


}
