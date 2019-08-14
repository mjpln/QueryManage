package com.knowology.km.entity;

public class BaseModel {
	private Integer page;
	private Integer pageSize;
	private Integer startRow;
	
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public Integer getPage() {
		return page;
	}
	public void setPage(Integer page) {
		this.page = page;
	}
	public Integer getStartRow() {
		return startRow;
	}
	public void setStartRow(Integer startRow) {
		this.startRow = (this.page-1) * this.pageSize;
	}
	
}
