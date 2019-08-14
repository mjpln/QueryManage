package com.knowology.km.action;

import com.knowology.km.bll.FaqDAO;

public class FaqAction {
	private String type;
	private String name;
	private String serviceid;
	private Object m_result;
	private String param;
	private String start;
	private String limit;
	private String kbdataid;
	private String channel;
	private String customertype;
	private String starttime;
	private String endtime;
	private String answertype;
	private String answer;
	private String service;
	private String brand;
	private String kbansvaliddateid;
	private String combition;
	private String caliberTypeId;//回复口径类型id
	private int page;
	private int rows;
	private String city;
	private String attrname;
	private String colnum;
	private String robotid;



	public String execute() {
		if ("select".equals(type)) {// 查询答案信息
			m_result = FaqDAO.select(kbdataid,page,rows);
		}  else if ("insert".equals(type)) {// 插入答案知识
			m_result = FaqDAO.insertOrUpdate(channel, customertype, starttime, endtime, answertype, answer, service, brand, kbdataid, kbansvaliddateid, type, city, robotid);
		} else if ("update".equals(type)) {// 更新答案知识
			m_result = FaqDAO.insertOrUpdate(channel, customertype, starttime, endtime, answertype, answer, service, brand, kbdataid, kbansvaliddateid, type, city, robotid);
		} else if ("delete".equals(type)) {// 删除答案知识
			m_result = FaqDAO.deleteBath(combition);
		} else if("createcustomertypecombobox".equals(type)){//获得客户问题下拉数据
			m_result = FaqDAO.getCustomer();
		}else if("createchannelcombobox".equals(type)){//获得渠道下拉数据
			m_result = FaqDAO.getChannel();
		}else if("createanswertypecombobox".equals(type)){//获的回复类型下拉数据
			m_result = FaqDAO.getAnswerType();
		}else if("createReplyCaliberTypeCombobox".equals(type)){//获得回复口径类型下拉数据
			m_result = FaqDAO.getReplyCaliberType();
		}else if("createCaliberTextCombobox".equals(type)){//获得口径文本下拉数据
			m_result = FaqDAO.getCaliberText(caliberTypeId);
		}else if("createserviceinfocombobox".equals(type)){//获得信息表下拉数据
			m_result = FaqDAO.getServiceInfo();
		}else if("createknonamecombobox".equals(type)){//获取信息表下docname
			m_result = FaqDAO.getKnoName(serviceid);
		}else if("createattrvaluescombobox".equals(type)){//获取信息表下相应属性对应的内容
			m_result = FaqDAO.getAttrValues(serviceid, colnum,city);
		}else if("createtemplatecolumncombobox".equals(type)){//获得模板列下拉数据
			m_result = FaqDAO.getTemplateColumn(serviceid);
		}else if("selectsemanticskeyword".equals(type)){//获得信息表列元素对应的语义关键字
			m_result = FaqDAO.getSemanticsKeyWordName(serviceid);
		}else if("createcitycombobox".equals(type)){
			m_result = FaqDAO.getNormalQueryCities(kbdataid);
		}else if("createattrnamecombobox".equals(type)){ //获取属性名
			m_result = FaqDAO.getAttrName(serviceid);
		}else if("createrobotidcombobox".equals(type)){ //获取robot ID
			m_result = FaqDAO.getRobotID(city);
		}else if ("getResConfig".equals(type)){// 获取其他答案类型
			m_result = FaqDAO.getResConfig();
		}
		return "success";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public Object getM_result() {
		return m_result;
	}

	public void setM_result(Object mResult) {
		m_result = mResult;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServiceid() {
		return serviceid;
	}

	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}

	public String getKbdataid() {
		return kbdataid;
	}

	public void setKbdataid(String kbdataid) {
		this.kbdataid = kbdataid;
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

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getCustomertype() {
		return customertype;
	}

	public void setCustomertype(String customertype) {
		this.customertype = customertype;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getEndtime() {
		return endtime;
	}

	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}

	public String getAnswertype() {
		return answertype;
	}

	public void setAnswertype(String answertype) {
		this.answertype = answertype;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getKbansvaliddateid() {
		return kbansvaliddateid;
	}

	public void setKbansvaliddateid(String kbansvaliddateid) {
		this.kbansvaliddateid = kbansvaliddateid;
	}
	
	public String getCombition() {
		return combition;
	}

	public void setCombition(String combition) {
		this.combition = combition;
	}

	public String getCaliberTypeId() {
		return caliberTypeId;
	}

	public void setCaliberTypeId(String caliberTypeId) {
		this.caliberTypeId = caliberTypeId;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAttrname() {
		return attrname;
	}

	public void setAttrname(String attrname) {
		this.attrname = attrname;
	}

	public String getColnum() {
		return colnum;
	}

	public void setColnum(String colnum) {
		this.colnum = colnum;
	}

	public String getRobotid() {
		return robotid;
	}

	public void setRobotid(String robotid) {
		this.robotid = robotid;
	}
	
}
