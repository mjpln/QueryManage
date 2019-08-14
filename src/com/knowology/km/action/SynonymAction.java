package com.knowology.km.action;

import com.knowology.km.bll.SynonymDAO;

public class SynonymAction {
	private int start;
	private int limit;
	private String synonym;
	private Boolean isprecise;
	private Boolean iscurrentworditem;
	private String type;
	private String curworditem;
	private String curwordclass;

	private String oldsynonym;
	private String newsynonym;
	private String oldtype;
	private String newtype;
	private String wordid;
	private String stdwordid;

	private String wordclassid;

	private String stdwordids;

	private String action;
	private Object m_result;

	public String execute() {
		if ("select".equals(action)) {// 查询别名信息
			m_result = SynonymDAO.select(start, limit, synonym, isprecise,
					iscurrentworditem, type, curworditem, curwordclass);
		} else if ("update".equals(action)) {// 更新别名
			m_result = SynonymDAO.update(oldsynonym, newsynonym, oldtype,
					newtype, wordid, stdwordid,curwordclass);
		} else if ("insert".equals(action)) {// 新增别名
			m_result = SynonymDAO.insert(wordclassid, synonym, type, stdwordid,
					curworditem, curwordclass);
		} else if ("delete".equals(action)) {// 删除别名
			m_result = SynonymDAO.delete(stdwordid, synonym, curworditem,
					curwordclass);
		}
		return "success";
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public String getSynonym() {
		return synonym;
	}

	public void setSynonym(String synonym) {
		this.synonym = synonym;
	}

	public Boolean getIsprecise() {
		return isprecise;
	}

	public void setIsprecise(Boolean isprecise) {
		this.isprecise = isprecise;
	}

	public Boolean getIscurrentworditem() {
		return iscurrentworditem;
	}

	public void setIscurrentworditem(Boolean iscurrentworditem) {
		this.iscurrentworditem = iscurrentworditem;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCurworditem() {
		return curworditem;
	}

	public void setCurworditem(String curworditem) {
		this.curworditem = curworditem;
	}

	public String getCurwordclass() {
		return curwordclass;
	}

	public void setCurwordclass(String curwordclass) {
		this.curwordclass = curwordclass;
	}

	public String getOldsynonym() {
		return oldsynonym;
	}

	public void setOldsynonym(String oldsynonym) {
		this.oldsynonym = oldsynonym;
	}

	public String getNewsynonym() {
		return newsynonym;
	}

	public void setNewsynonym(String newsynonym) {
		this.newsynonym = newsynonym;
	}

	public String getOldtype() {
		return oldtype;
	}

	public void setOldtype(String oldtype) {
		this.oldtype = oldtype;
	}

	public String getNewtype() {
		return newtype;
	}

	public void setNewtype(String newtype) {
		this.newtype = newtype;
	}

	public String getWordid() {
		return wordid;
	}

	public void setWordid(String wordid) {
		this.wordid = wordid;
	}

	public String getStdwordid() {
		return stdwordid;
	}

	public void setStdwordid(String stdwordid) {
		this.stdwordid = stdwordid;
	}

	public String getWordclassid() {
		return wordclassid;
	}

	public void setWordclassid(String wordclassid) {
		this.wordclassid = wordclassid;
	}

	public String getStdwordids() {
		return stdwordids;
	}

	public void setStdwordids(String stdwordids) {
		this.stdwordids = stdwordids;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Object getM_result() {
		return m_result;
	}

	public void setM_result(Object mResult) {
		m_result = mResult;
	}
}
