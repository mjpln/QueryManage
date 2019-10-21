package com.knowology.km.action;

import com.knowology.km.bll.ServiceWordDao;


public class ServiceWordAction extends BaseAction{
	private Object m_result;
	private int page;
	private int rows;
	
	private String type;
	
	private String ioa;
	private String wordid;
	private String word;
	private String wordclass;
	//业务词
	private String serviceword;
	//其他别名
	private String otherword;
	//业务名
	private String serviceid;
	
	public String execute() {
		if ("getStandardWordClass".equals(type)){
			m_result = ServiceWordDao.getStandardWordClass(ioa);
		}else if ("getStandardWord".equals(type)){//获取标准词
			m_result = ServiceWordDao.getStandardWord(ioa,wordclass,word,rows,page);
		} else if ("deleteStandardWord".equals(type)){// 删除词条
			m_result = ServiceWordDao.delStandardWord(wordid);
		} else if ("getOtherWord".equals(type)){// 点击标准词，获取其他词类
			m_result = ServiceWordDao.getOtherWordByStandardWord(ioa,wordclass,wordid,rows,page);
		} else if ("selOtherWord".equals(type)){// 搜索其他词类
			m_result = ServiceWordDao.selOtherWord(ioa,wordclass,word,rows,page);
		} else if ("getStandardWordByOtherWord".equals(type)){
			m_result = ServiceWordDao.getStandardWordByOtherWord(ioa,wordclass,word,rows,page);
		} else if ("getServiceWord".equals(type)){//查找业务词
			m_result = ServiceWordDao.getServiceWord(serviceword,rows,page);
		} else if ("insertServiceWord".equals(type)){//查找业务词
			m_result = ServiceWordDao.insertServiceWord(serviceword,otherword,serviceid,httpRequest);
		}
		return "success";
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Object getM_result() {
		return m_result;
	}
	public void setM_result(Object mResult) {
		m_result = mResult;
	}
	public String getIoa() {
		return ioa;
	}
	public void setIoa(String ioa) {
		this.ioa = ioa;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	public String getWordid() {
		return wordid;
	}
	public void setWordid(String wordid) {
		this.wordid = wordid;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getWordclass() {
		return wordclass;
	}
	public void setWordclass(String wordclass) {
		this.wordclass = wordclass;
	}
	public String getServiceword() {
		return serviceword;
	}
	public void setServiceword(String serviceword) {
		this.serviceword = serviceword;
	}
	public String getOtherword() {
		return otherword;
	}
	public void setOtherword(String otherword) {
		this.otherword = otherword;
	}
	public String getServiceid() {
		return serviceid;
	}
	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}
	
	
}
