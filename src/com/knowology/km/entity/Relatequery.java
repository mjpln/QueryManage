package com.knowology.km.entity;

import java.math.BigDecimal;
import java.util.Date;

public class Relatequery extends BaseModel{
	private BigDecimal id;
	private BigDecimal kbdataid;
	private BigDecimal relatequerytokbdataid;
	private String relatequery;
	private Long workid;
	private Date edittime;
	private String remark;
	public BigDecimal getId() {
		return id;
	}
	public void setId(BigDecimal id) {
		this.id = id;
	}
	public BigDecimal getKbdataid() {
		return kbdataid;
	}
	public void setKbdataid(BigDecimal kbdataid) {
		this.kbdataid = kbdataid;
	}
	public BigDecimal getRelatequerytokbdataid() {
		return relatequerytokbdataid;
	}
	public void setRelatequerytokbdataid(BigDecimal relatequerytokbdataid) {
		this.relatequerytokbdataid = relatequerytokbdataid;
	}
	public String getRelatequery() {
		return relatequery;
	}
	public void setRelatequery(String relatequery) {
		this.relatequery = relatequery;
	}
	public Long getWorkid() {
		return workid;
	}
	public void setWorkid(Long workid) {
		this.workid = workid;
	}
	public Date getEdittime() {
		return edittime;
	}
	public void setEdittime(Date edittime) {
		this.edittime = edittime;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
