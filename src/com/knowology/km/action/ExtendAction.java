package com.knowology.km.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.km.bll.ExtendDao;
import com.knowology.km.bll.QuerymanageDAO;
import com.knowology.km.bll.QuestionUploadDao;
import com.knowology.km.dto.ExtendsDto;
import com.knowology.km.util.GetSession;

public class ExtendAction extends BaseAction {
	private String kbdataid;
	private String question;
	private String type;
	private String city;
	
	private String service;
	private String attr6;
	private String attr8;
	private String attr9;
	private String attr10;
	private String attr11;
	private String attr12;
	private String attr13;
	private String attr15;
	private Object m_result;
	private String serviceid;
	private String local;
	private List<ExtendsDto> listDto;
	
	private String stand_abstract;
	private String synonymy_abstract;
	private String reason;
	private String understandresult;
	
	/**
	 * 高级分析
	 * @return
	 */
	public String analysis(){
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		m_result = ExtendDao.analysis(user.getUserID(), user.getIndustryOrganizationApplication(), question,city, type, "否", "",kbdataid);
		return "success";
	}
	
	/**
	 * 
	 * @return
	 */
	public String getExtendKbdatas(){
		m_result = ExtendDao.getExtendKbdatas(kbdataid, city);
		return "success";
	}
	
	/**
	 * 获取继承信息
	 * @return
	 */
	public String getServiceInfo(){
		m_result = ExtendDao.getServiceInfo(attr6,kbdataid);
		return "success";
	}
	
	/**
	 * 保存继承
	 * @return
	 */
	public String saveExtend(){
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		m_result = ExtendDao.saveExtend(kbdataid, attr6, user, attr8, attr9, attr10, attr11, attr12, attr13, attr15);
		return "success";
	}
	
	/**
	 * 删除继承
	 * @return
	 */
	public String deleteExtend(){
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		m_result = ExtendDao.deleteExtend(user,attr6,kbdataid);
		return "success";
	}
		
	/**
	 * 批量保存继承
	 * @return
	 */
	public String batchSave(){
		m_result = ExtendDao.batchSave(listDto);
		return "success";
	}
	
	/**
	 * 更新知识库
	 * @return
	 */
	public String updateKbdata(){
		m_result = ExtendDao.updateKB();
		return "success";
	}
	/**
	 * 更新知识库新接口
	 * @return
	 */
	public String updateKbdataNew(){
		m_result = ExtendDao.updateKBNew();
		return "success";
	}
	
	/**
	 * 构造业务树
	 * @return
	 */
	public String createServiceTree() {
		m_result = ExtendDao.createServiceTree(serviceid);
		return "success";
	}
	
	/**
	 * 根据业务id构造摘要下拉框
	 * @return
	 */
	public String createCombobox() {
		m_result = ExtendDao.createCombobox(serviceid);
		return "success";
	}
	
	// 获取地市树
	public String getCityTree(){
		if(city.equals("")){
			m_result = QuerymanageDAO.createCityTreeByLoginInfo("");
		}else{
			if(city.contains("全国")){//全国
				m_result = QuestionUploadDao.getCityTree(local);//省市二级
			}else{
				//省
				String[] citycodes = city.split(",");
//			Arrays.sort(citycodes);
				JSONObject jsonObj = new JSONObject();
				JSONArray jsonArr = new JSONArray();
				for(String code:citycodes){
					if(code.endsWith("0000")){
						jsonObj.put("id", code);
						jsonObj.put("text", QuerymanageDAO.cityCodeToCityName.get(code));
					}else{
						JSONObject son = new JSONObject();
						son.put("id", code);
						son.put("text", QuerymanageDAO.cityCodeToCityName.get(code));
						jsonArr.add(son);
					}
				}
				jsonObj.put("children", jsonArr);
				JSONArray array = new JSONArray();
				array.add(jsonObj);
				m_result = array;
			}
		}
		return "success";
	}
	
	// 报错
	public String reportError(){
		m_result = ExtendDao.reportError(stand_abstract,synonymy_abstract,city,reason,understandresult);
		return "success";
	}
	

	public String getKbdataid() {
		return kbdataid;
	}

	public void setKbdataid(String kbdataid) {
		this.kbdataid = kbdataid;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getAttr6() {
		return attr6;
	}

	public void setAttr6(String attr6) {
		this.attr6 = attr6;
	}

	public String getAttr8() {
		return attr8;
	}

	public void setAttr8(String attr8) {
		this.attr8 = attr8;
	}

	public String getAttr9() {
		return attr9;
	}

	public void setAttr9(String attr9) {
		this.attr9 = attr9;
	}

	public String getAttr10() {
		return attr10;
	}

	public void setAttr10(String attr10) {
		this.attr10 = attr10;
	}

	public String getAttr11() {
		return attr11;
	}

	public void setAttr11(String attr11) {
		this.attr11 = attr11;
	}

	public String getAttr12() {
		return attr12;
	}

	public void setAttr12(String attr12) {
		this.attr12 = attr12;
	}

	public String getAttr13() {
		return attr13;
	}

	public void setAttr13(String attr13) {
		this.attr13 = attr13;
	}

	public String getAttr15() {
		return attr15;
	}

	public void setAttr15(String attr15) {
		this.attr15 = attr15;
	}

	public Object getM_result() {
		return m_result;
	}

	public void setM_result(Object m_result) {
		this.m_result = m_result;
	}

	public String getServiceid() {
		return serviceid;
	}

	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public List<ExtendsDto> getListDto() {
		return listDto;
	}

	public void setListDto(List<ExtendsDto> listDto) {
		this.listDto = listDto;
	}

	public String getStand_abstract() {
		return stand_abstract;
	}

	public void setStand_abstract(String standAbstract) {
		stand_abstract = standAbstract;
	}

	public String getSynonymy_abstract() {
		return synonymy_abstract;
	}

	public void setSynonymy_abstract(String synonymyAbstract) {
		synonymy_abstract = synonymyAbstract;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getUnderstandresult() {
		return understandresult;
	}

	public void setUnderstandresult(String understandresult) {
		this.understandresult = understandresult;
	}
	

}
