package com.knowology.km.dto;

import java.util.List;

public class DataGridResponse extends Response{
	private List<?> rows;
	private Integer total;
	
	public DataGridResponse(List<?> rows, Integer total, boolean success, String message) {
		super(null, success, message);
		this.rows = rows;
		this.total = total;
	}
	
	public DataGridResponse(List<?> rows, Integer total, boolean success) {
		super(null, success);
		this.rows = rows;
		this.total = total;
	}
	
	public List<?> getRows() {
		return rows;
	}
	public void setRows(List<?> rows) {
		this.rows = rows;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}
	
}
