package com.knowology.km.action;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.knowology.km.bll.AnalyzeDAO;

/**
 * 对外接口
 * @author cwy-pc
 *
 */
public class ExternalInterfaceAction extends BaseAction implements ServletRequestAware{
	private Object m_result;
	private String query;
	private String queryCityCode;
	private String servicetype;
	public String generateWordpat() {
		m_result = AnalyzeDAO.getWordpat(servicetype, query, queryCityCode, "自学习");
		return "success";
	}
	public Object getM_result() {
		return m_result;
	}
	public void setM_result(Object m_result) {
		this.m_result = m_result;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getQueryCityCode() {
		return queryCityCode;
	}
	public void setQueryCityCode(String queryCityCode) {
		this.queryCityCode = queryCityCode;
	}
	public String getServicetype() {
		return servicetype;
	}
	public void setServicetype(String servicetype) {
		this.servicetype = servicetype;
	}
	
}
