package com.knowology.km.entity;

import com.alibaba.fastjson.annotation.JSONField;

public class ImportWordpat {
	private String wordpatId;
	private String wordpat;
	private String wordpatType;
	private String kbdataId;
	private String city;
	private String brand;
	@JSONField(serialize = false)
	private ImportKbData kbData;
	public String getWordpatId() {
		return wordpatId;
	}
	public void setWordpatId(String wordpatId) {
		this.wordpatId = wordpatId;
	}
	public String getWordpat() {
		return wordpat;
	}
	public void setWordpat(String wordpat) {
		this.wordpat = wordpat;
	}
	public String getWordpatType() {
		return wordpatType;
	}
	public void setWordpatType(String wordpatType) {
		this.wordpatType = wordpatType;
	}
	public String getKbdataId() {
		return kbdataId;
	}
	public void setKbdataId(String kbdataId) {
		this.kbdataId = kbdataId;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public ImportKbData getKbData() {
		return kbData;
	}
	public void setKbData(ImportKbData kbData) {
		this.kbData = kbData;
	}
	@Override
	public String toString() {
		return "ImportWordpat [wordpatId=" + wordpatId + ", wordpat=" + wordpat
				+ ", wordpatType=" + wordpatType + ", kbdataId=" + kbdataId
				+ ", city=" + city + ", brand=" + brand + "]";
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	
}
