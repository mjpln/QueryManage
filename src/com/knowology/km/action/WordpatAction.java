package com.knowology.km.action;



import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.knowology.km.bll.WordpatDAO;
import com.knowology.km.entity.InsertOrUpdateParam;

public class WordpatAction implements ServletRequestAware {
	private HttpServletRequest request;

	public String action;
	public String cityids;

	public String start;
	public String limit;
	public String param;
	public Object m_result;
	public String wordpatids;
	public String citycode;
	public String type;
	public String  resourcetype;
	public String  operationtype;
	public String  resourceid;
	public String kbdataid;
	

	public String execute() {
		InsertOrUpdateParam m_param = null;
		if (param != null) {
			// 将前台串的param参数用JSONObject转化为InsertOrUpdateParam对象
			m_param = JSONObject.parseObject(param, InsertOrUpdateParam.class);
		}
		if ("city".equals(action)) {// 获取地市名称
			m_result = WordpatDAO.GetCitysFromCityids(cityids);
		} else if ("select".equals(action)) {// 分页查询满足条件的词模
			m_param.limit = limit;
			m_param.start = start;
			m_result = WordpatDAO.Select(m_param);
		} else if ("querychannel".equals(action)) {// 获取服务信息
			m_result = WordpatDAO.QueryChannel();
		} else if ("autowordpat".equals(action)) {// 生成词模
			m_result = WordpatDAO.AutoGenerateWordpat(m_param);
		} else if ("update".equals(action)) {// 更新词模
			m_result = WordpatDAO.UpdateWordpat(m_param, request,operationtype,resourceid);
		} else if ("delete".equals(action)) {// 删除词模
			m_result = WordpatDAO.Delete(m_param,operationtype,resourceid);
		} else if ("insert".equals(action)) {// 新增词模
			m_result = WordpatDAO.InsertWordpat(m_param, request, operationtype,resourceid);
		} else if ("simpleInsert".equals(action)) {// 简单新增词模
			m_result = WordpatDAO.SimpleInsertWordpat(m_param, request);
		}else if ("selectWordpatCity".equals(type)){
			m_result = WordpatDAO.selectWordpatCity(wordpatids);
		} else if ("updateWordpatCity".equals(type)){
			m_result = WordpatDAO.updateWordpatCity(wordpatids,citycode);
		}else if("isshowbutton".equals(action)){
			m_result = WordpatDAO.isShowButton(kbdataid);
		}else if("addreturnvalue".equals(action)){
			m_result = WordpatDAO.getInteractiveReturnValue(kbdataid);
		}else if("addreturnvalue_loadkeys".equals(action)){
			m_result = WordpatDAO.getKeys();
		}else if("addreturnvalue_loadValues".equals(action)){
			m_result = WordpatDAO.getValues(request);
		}else if("addreturnvalue_loadWordclasses".equals(action)){
			m_result = WordpatDAO.getWordclasses(request);
		}else if ("transferWordpat".equals(action)) {// 迁移词模
			m_result = WordpatDAO.transferWordpat(wordpatids,operationtype,resourceid,m_param.service,kbdataid);
		} else if ("batchDelete".equals(action)) {// 批量删除词模
			m_result = WordpatDAO.BatchDelete(m_param,operationtype,resourceid);
		} 
		return "success";
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getCityids() {
		return cityids;
	}

	public void setCityids(String cityids) {
		this.cityids = cityids;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public Object getM_result() {
		return m_result;
	}

	public void setM_result(Object mResult) {
		m_result = mResult;
	}

	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String getKbdataid() {
		return kbdataid;
	}

	public void setKbdataid(String kbdataid) {
		this.kbdataid = kbdataid;
	}
	
}
