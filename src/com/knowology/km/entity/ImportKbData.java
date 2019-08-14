package com.knowology.km.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.annotation.JSONField;

public class ImportKbData {
	private String kbdataId;
	private String abs;
	private String serviceId;
	private String city;
	private String responsetype;
	private String interacttype;
	
	@JSONField(serialize = false)
	private ImportService service;
	private Map<String,ImportWordpat> wordpatMap;
	private Map<String,ImportQuery> queryMap;
	
	public ImportKbData() {
	}
	
	public ImportKbData(Map map) {
		this.abs = StringUtils.substringAfter(map.get("abstract").toString(), ">");
		this.kbdataId = map.get("kbdataid").toString();
		this.city = map.get("city").toString();
		this.responsetype = Objects.toString(map.get("responsetype"),null);
		this.interacttype = Objects.toString(map.get("interacttype"),null);
	}

	public void merge(ImportKbData kbData){
		this.kbdataId = StringUtils.defaultIfEmpty(kbData.getKbdataId(), serviceId);
		this.abs = StringUtils.defaultIfEmpty(kbData.getAbs(), abs);
		this.city = StringUtils.defaultIfEmpty(kbData.getCity(), city);
		this.responsetype = StringUtils.defaultIfEmpty(kbData.getResponsetype(), responsetype);
		this.interacttype = StringUtils.defaultIfEmpty(kbData.getInteracttype(), interacttype);
	}
	
	public String getKbdataId() {
		return kbdataId;
	}
	public void setKbdataId(String kbdataId) {
		this.kbdataId = kbdataId;
	}
	public String getAbs() {
		return abs;
	}
	public void setAbs(String abs) {
		this.abs = abs;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public ImportService getService() {
		return service;
	}
	public void setService(ImportService service) {
		this.service = service;
	}

	
	public String getResponsetype() {
		return responsetype;
	}

	public void setResponsetype(String responsetype) {
		this.responsetype = responsetype;
	}

	public String getInteracttype() {
		return interacttype;
	}

	public void setInteracttype(String interacttype) {
		this.interacttype = interacttype;
	}

	public Map<String,ImportWordpat> getWordpatMap() {
		return wordpatMap;
	}
	public void setWordpatMap(Map<String,ImportWordpat> wordpatMap) {
		this.wordpatMap = wordpatMap;
	}
	
	public void addWordpat(ImportWordpat wordpat){
		Map<String, ImportWordpat> map = getWordpatMap();
		if(map == null){
			map = new HashMap<String, ImportWordpat>();
			setWordpatMap(map);
		}
		wordpat.setKbData(this);
		map.put(wordpat.getWordpat(), wordpat);
	}
	public Map<String,ImportQuery> getQueryMap() {
		return queryMap;
	}
	public void setQueryMap(Map<String,ImportQuery> queryMap) {
		this.queryMap = queryMap;
	}
	
	public void addQuery(ImportQuery query){
		Map<String, ImportQuery> map = getQueryMap();
		if(map == null){
			map = new HashMap<String, ImportQuery>();
			setQueryMap(map);
		}
		query.setKbData(this);
		ImportQuery importQuery = map.get(query.getQuery());
		if(importQuery != null && !StringUtils.isEmpty(query.getCity())){
			importQuery.setCity(query.getCity());
		}else{
			map.put(query.getQuery(), query);
		}
		
	}
	@Override
	public String toString() {
		return "ImportKbData [kbdataId=" + kbdataId + ", abs=" + abs
				+ ", serviceId=" + serviceId + ", responsetype=" + responsetype 
				+ ", interacttype=" + interacttype + ", city=" + city + "]";
	}
	
}
