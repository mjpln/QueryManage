package com.knowology.km.entity;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class ImportService {
	String serviceId;
	String service;
	String parentId;
	String city;
	String brand;
	private String parentName;
	private String cityid;
	private Map<String,ImportService> childService;
	private Map<String,ImportKbData> kbDataMap;
	
	public ImportService() {
	}
	
	public ImportService(Map<Object,Object> map) {
		this.setService(map.get("service").toString());
		this.setParentId(map.get("parentid").toString());
		this.setBrand(map.get("brand").toString());
		this.setCity(map.get("city").toString());
		this.setServiceId(map.get("serviceid").toString());
		this.setCityid(map.get("cityid").toString());
	}
	public void merge(ImportService service){
		this.serviceId = StringUtils.defaultIfEmpty(service.getServiceId(), serviceId);
		this.parentId = StringUtils.defaultIfEmpty(service.getParentId(), parentId);
		this.city = StringUtils.defaultIfEmpty(service.getCity(), city);
		this.brand = StringUtils.defaultIfEmpty(service.getBrand(), brand);
		this.parentName = StringUtils.defaultIfEmpty(service.getParentName(), parentName);
		this.cityid = StringUtils.defaultIfEmpty(service.getCityid(), cityid);
	}
	
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}

	public Map<String,ImportService> getChildService() {
		return childService;
	}
	public void setChildService(Map<String,ImportService> childService) {
		this.childService = childService;
	}
	public ImportService addChildService(ImportService service){
		Map<String, ImportService> map = getChildService();
		if(map == null){
			map = new HashMap<String, ImportService>();
			setChildService(map);
		}
		//去重
		ImportService service2 = map.get(service);
		if(service2 != null){
			service2.merge(service);
			return service2;
		}else{
			service.setParentName(this.service);
			map.put(service.getService(), service);
			return service;
		}
	}
	
	public ImportService getChildService(String service){
		Map<String, ImportService> map = getChildService();
		if(map == null){
			return null;
		}
		return map.get(service);
		
	}
	public Map<String,ImportKbData> getKbDataMap() {
		return this.kbDataMap;
	}
	public void setKbDataMap(Map<String,ImportKbData> kbDataMap) {
		this.kbDataMap = kbDataMap;
	}
	
	public void addKbData(ImportKbData kbData){
		Map<String, ImportKbData> map = getKbDataMap();
		if(map == null){
			map = new HashMap<>();
			setKbDataMap(map);
		}
		kbData.setService(this);
		map.put(kbData.getAbs(), kbData);
	}
	
	public ImportKbData getKbdata(String abs){
		Map<String, ImportKbData> map = getKbDataMap();
		if(map == null){
			return null;
		}
		return map.get(abs);
	}
	@Override
	public String toString() {
		return "ImportService [serviceId=" + serviceId + ", service=" + service
				+ ", parentId=" + parentId + ", city=" + city + ", brand="
				+ brand + ", parentName=" + parentName  + "]";
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getCityid() {
		return cityid;
	}

	public void setCityid(String cityid) {
		this.cityid = cityid;
	}
}
