package com.knowology.km.entity;

public class RealRobotInfo {
	
	private String id;
	private String name;
	private String city;
	private String cityCode;
	private String servicePosition;
	private String mac;
	
	
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCityCode() {
		return cityCode;
	}
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}
	public String getServicePosition() {
		return servicePosition;
	}
	public void setServicePosition(String servicePosition) {
		this.servicePosition = servicePosition;
	}
	@Override
	public String toString() {
		return "RealRobotInfo [city=" + city + ", cityCode=" + cityCode + ", id=" + id + ", mac=" + mac + ", name=" + name + ", servicePosition=" + servicePosition + "]";
	}
	
}
