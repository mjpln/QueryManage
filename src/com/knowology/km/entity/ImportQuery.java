package com.knowology.km.entity;

import com.alibaba.fastjson.annotation.JSONField;

public class ImportQuery {
	private String queryId;
	private String city;
	private String query;
	private String kbdataId;
	@JSONField(serialize = false)
	private ImportKbData kbData;
	public String getQueryId() {
		return queryId;
	}
	public void setQueryId(String queryId) {
		this.queryId = queryId;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getKbdataId() {
		return kbdataId;
	}
	public void setKbdataId(String kbdataId) {
		this.kbdataId = kbdataId;
	}
	public ImportKbData getKbData() {
		return kbData;
	}
	public void setKbData(ImportKbData kbData) {
		this.kbData = kbData;
	}
	@Override
	public String toString() {
		return "ImportQuery [queryId=" + queryId + ", city=" + city
				+ ", query=" + query + ", kbdataId=" + kbdataId + "]";
	}
	
}
