package com.knowology.km.action;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.km.access.UserManager;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.bll.AnalyzeDAO;
import com.knowology.km.bll.ClassifyqueryDao;
import com.knowology.km.bll.ImportExportDAO;
import com.knowology.km.bll.KnowledgeRetrievalDAO;
import com.knowology.km.bll.QuerymanageDAO;
import com.knowology.km.bll.QuestionUploadDao;

public class QuerymanageAction extends BaseAction implements ServletRequestAware   {
	private HttpServletRequest request;
	private String type;
	private String m_request;
	private Object m_result;
	private String serviceid;
	private String parentid;//父节点
	private String userid;
	private String ioa;
	private String flag;
	private String querytype;
	private String normalquery;
	private String citycode;
	private String customerquery;
	private String responsetype;
	private String interacttype;
	private String combition;
	private String queryid;
	private String oldcustomerquery;
	private String oldcitycode;
	private String oldnormalquery;
	private String service;
	private String kbdataid;
	private String filename;
	private String relatequerytokbdataid;
	private String relatequery;
	private String key;
	private String classifyquery;
	private String city;
	private String classified;
	private String checked;
	private String checktimeStart;
	private String checktimeEnd;
	private String inserttimeStart;
	private String inserttimeEnd;
	private String abs;
	private String citySelect;
	private String local;
	private String scenariosid;
	private String understrandinfo;
	private String multinormalquery;
	private String content;
	private String searchtype;
	private String istrain;
	private String understandstatus;
	
	private String[] ids;
	private String[] kbdataids;
	private String[] abses;
	
	private int page;
	private int rows;
	private String q;
	/*
	 * 去除新词后的标准问
	 */
	private String newnormalquery;
	/*
	 * 业务词，多个逗号分隔
	 */
	private String businesswords; 
	/*
	 * 是否严格排除状态
	 */
	private String removequerystatus;
	
	public String execute() {
		 if(!"".equals(m_request)&&m_request!=null){// 解析参数 m_request
			JSONObject json = JSONObject.parseObject(m_request);
			normalquery = json.getString("normalquery");
			multinormalquery = json.getString("multinormalquery");
			customerquery = json.getString("customerquery");
			querytype = json.getString("querytype");
			serviceid = json.getString("serviceid");
			citycode = json.getString("citycode");
			citycode = citycode.replace("\"", "").replace("[", "").replace("]", "");
			type = json.getString("type");
			removequerystatus = json.getString("removequerystatus");
		 }
		 if ("createuserinfo".equals(type)) {// 创建用户信息
			JSONObject jsonObj = new JSONObject();
			User user = UserManager.constructLoginUser(userid, ioa);
			session.put("accessUser", user);
			session.put("customItem", null);
			JSONObject object = QuerymanageDAO.findConfigure();
			session.put("customItem", object.get("customItem"));
			jsonObj.putAll(object);//查询定制化信息
			if (session != null && session.size() > 0) {
//				QuerymanageDAO.refreshServiceTree(citySelect);
				jsonObj.put("msg", true);
			} else {
				jsonObj.put("msg", false);
			}
			m_result = jsonObj;
		}else if("selectquery".equals(type)){//查询客户问题
			m_result = QuerymanageDAO.selectQuery(serviceid, normalquery, customerquery, citycode, responsetype, interacttype,page,rows);
		}else if("selectnormalquery".equals(type)){//查询标准问题
			m_result = QuerymanageDAO.selectNormalQuery_New(serviceid, normalquery, responsetype, interacttype, page, rows);
		}else if("selectcustomerquery".equals(type)){//查询客户问题
			m_result = QuerymanageDAO.selectCustomerQuery(serviceid,kbdataid, normalquery, customerquery, citycode, istrain, understandstatus, page,rows);	
		} else if ("createservicetree".equals(type)) {// 构造模型业务树
			m_result = QuerymanageDAO.createServiceTreeNew(scenariosid,citySelect);
		}else if("createinteractivescenetree".equals(type)){//构造场景树
			m_result = QuerymanageDAO.createInteractiveSceneTree(scenariosid,citySelect);	
		}else if("getinteractivescenetree".equals(type)){// 获取场景树
			m_result = QuerymanageDAO.getInteractiveSceneTree();
		}else if("createcitytreebylogininfo".equals(type)){//根据用户信息创建地市树
			m_result = QuerymanageDAO.createCityTreeByLoginInfo(flag);
		}else if("createnormalquerycombobox".equals(type)){//创建标准问题下拉框
			m_result = QuerymanageDAO.createAbstractCombobox(serviceid,flag);
		}else if("createresponsetypecombobox".equals(type)){//创建回复类型下拉框
			m_result = QuerymanageDAO.createResponseTypeCombobox(flag);
		}else if("createinteracttypecombobox".equals(type)){//创建交互类型下拉框
			m_result = QuerymanageDAO.createInteractTypeCombobox(flag);
		}else if("createproduceWordpatcombobox".equals(type)){//创建训练模式下拉框
			m_result = QuerymanageDAO.createProduceWordpatCombobox(flag);
		}else if("findnormalquery".equals(type)){// 查找标准问
			m_result = QuerymanageDAO.findNormalquery(normalquery.trim(), citySelect);
		}else if("findcustomerquery".equals(type)){// 查找客户问
			m_result = QuerymanageDAO.findCustomerquery(customerquery.trim(), citySelect,0);
		}else if("addquery".equals(type)){//新增问题
			m_result = QuerymanageDAO.addQuery(serviceid,querytype.trim(), normalquery.trim(), multinormalquery, customerquery.trim(), citycode, request);
		}else if("producewordpat".equals(type)){//生成词模
			m_result = AnalyzeDAO.produceWordpat(combition,flag,request);
		}else if("produceallwordpat".equals(type)){//全量生成词模
			m_result = AnalyzeDAO.produceAllWordpat(serviceid,flag,request);
		}else if("updatecustomerquery".equals(type)){//修改客户问题
			m_result = QuerymanageDAO.updateQuery(service,normalquery,oldnormalquery,responsetype,interacttype,kbdataid, queryid, oldcitycode, citycode, oldcustomerquery,customerquery,request);
		}else if("deletecustomerquery".equals(type)){//删除客户问题
			m_result = QuerymanageDAO.deleteCustomerQuery(combition);
		}else if("deletenormalquery".equals(type)){//删除标准问题
			m_result = QuerymanageDAO.deleteNormalQuery(kbdataid);
		}else if ("import".equals(type)) {// 导入客户问题
			m_result = ImportExportDAO.importFile(filename,serviceid,0);
		}else if ("importwordxls".equals(type)) {// 导入词类词条
			m_result = QuerymanageDAO.importwordxls(filename);
		}else if ("importfaqxls".equals(type)) {// 导入FAQ
			m_result = QuerymanageDAO.importfaqxls(filename,serviceid,service,request);
		}else if("findscenarios".equals(type)){//查找场景
			m_result = QuerymanageDAO.findScenarios(kbdataid,key);
		}else if("insertscenarios2kbdata".equals(type)){//标准问题场景配置
			m_result = QuerymanageDAO.bindNormalQuery2Scenorio(kbdataid, scenariosid);
		}else if("createtree".equals(type)){//构造业务树
			m_result = QuerymanageDAO.createTree(serviceid);
		}else if("createabscombobox".equals(type)){//构造摘要下拉
			m_result = QuerymanageDAO.createAbstractCombobox(serviceid);
		}else if("insertrelatequery".equals(type)){//新增相关问题
			m_result = QuerymanageDAO.insertRelateQuery(relatequerytokbdataid, relatequery.trim(), kbdataid);
		}else if("selectrelatequery".equals(type)){//查询相关问题
			m_result = QuerymanageDAO.selectRelateQuery(kbdataid, relatequery,page,rows);
		}else if("deleterelatequery".equals(type)){//删除相关问题
			m_result = QuerymanageDAO.deleteRelateQuery(combition);
		}else if("transfernormalquery".equals(type)){//迁移标准问题
			m_result = QuerymanageDAO.transferNormalQuery(serviceid,kbdataids,abses);
		}else if("classifyqueryimport".equals(type)){//导入分类问题
			m_result = ClassifyqueryDao.importFile(filename,serviceid);
		}else if("selectclassifyquery".equals(type)){//分类问题查询
			m_result = ClassifyqueryDao.selectclassifyquery(classifyquery, city, serviceid, normalquery, classified, checked, checktimeStart, checktimeEnd, inserttimeStart, inserttimeEnd, page, rows);
		}else if("deleteclassifyquery".equals(type)){//删除分类问题
			m_result = ClassifyqueryDao.batchDelete(ids);
		}else if("autoclassifybatch".equals(type)){//分类问题批量自动分配
			m_result = ClassifyqueryDao.autoBatchClassify(ids);
		}else if("autoclassifyall".equals(type)){//分类问题全量自动分配
			m_result = ClassifyqueryDao.autoAllClassify(serviceid);
		}else if("insertclassifyquery".equals(type)){//分类问题批量手动分配
			m_result = ClassifyqueryDao.manualClassify(ids, serviceid, service, kbdataid, abs);
		}else if("checkclassifyquery".equals(type)){//分类问题批量审核
			m_result = ClassifyqueryDao.batchCheck(ids);
		}else if("searchservice".equals(type)){// 查询业务
			if(q!=null){
				m_result = QuerymanageDAO.searchService(q,citySelect);
			}
		}else if("refreshservicetree".equals(type)){ // 刷新业务树
			m_result = QuerymanageDAO.refreshServiceTree(citySelect);
		}else if("getservicetree".equals(type)){ // 获取业务树（无刷新）
			m_result = QuerymanageDAO.getServiceTree(citySelect);
		}else if("appendservice".equals(type)){ // 增加业务
			m_result = QuerymanageDAO.appendService(serviceid, service); 
		}else if("renameservice".equals(type)){ // 修改业务名称
			m_result = QuerymanageDAO.renameService(serviceid,parentid,service);
		}else if("deleteservice".equals(type)){ // 删除业务
			m_result = QuerymanageDAO.deleteService(serviceid);
		}else if("getCityTreeByLoginInfo".equals(type)){ // 获取区域树
			m_result = QuestionUploadDao.getCityTreeByLoginInfo(local);//省市二级
		}else if ("understand".equals(type)){ // 批量理解
			m_result = QuestionUploadDao.understand(understrandinfo);
		}else if("searchall".equals(type)){
			m_result = KnowledgeRetrievalDAO.getKnowledge(content, searchtype, citySelect, page, rows);
		}else if("findServiceParent".equals(type)){//递归查找业务父节点
			m_result = QuerymanageDAO.findServiceParent(serviceid);
		}else if("appendNodeByServiceId".equals(type)){//拼接业务内容
			m_result = QuerymanageDAO.appendNodeByServiceId(serviceid,parentid,citySelect);
		}else if("getaddress".equals(type)){//获取参数配置中菜单地址
			m_result = QuerymanageDAO.getAddressNew(key);
		}else if("createwordpatexport".equals(type)){//生成词模导出文件
			m_result = QuerymanageDAO.exportWordpat(serviceid,flag);
		}else if("createqueryexport".equals(type)){//生成客户问导出文件
			m_result = QuerymanageDAO.createQueryExport(serviceid,flag);
		}else if("exportwordpatandquery".equals(type)){//生成词模和客户问导出文件
			m_result = QuerymanageDAO.exportWordpatAndQuery(serviceid,flag);
		}else if("exportservice".equals(type)){//导出业务树
			m_result = QuerymanageDAO.exportService(serviceid);
		}else if("importservice".equals(type)){//导入业务树
			m_result = QuerymanageDAO.importService(filename, serviceid, request);
		}else if("configure".equals(type)){//查询定制化信息
			m_result = QuerymanageDAO.findConfigure();
		}else if("getKMUrl".equals(type)){//查询定制化信息
			m_result = QuerymanageDAO.getKMUrl();
		}else if ("importkb".equals(type)) {// 导入语义
			m_result = ImportExportDAO.importKBData(filename, serviceid);
		} else if ("addWord".equals(type)) {// 新增词条
			m_result = QuerymanageDAO.addWord(combition, flag, normalquery,newnormalquery, serviceid,businesswords, request);
		} else if ("selectremovequery".equals(type)) {// 查询排除问题
			m_result = QuerymanageDAO.selectRemoveQuery(serviceid, kbdataid, normalquery, customerquery, citycode, istrain, removequerystatus, page, rows);
		} else if ("findremovequery".equals(type)) {// 查找排除问
			m_result = QuerymanageDAO.findCustomerquery(customerquery.trim(), citySelect, 1);
		} else if ("addremovequery".equals(type)) {// 新增排除问题
			m_result = QuerymanageDAO.addRemoveQueryWordpat(serviceid, querytype.trim(), normalquery.trim(), customerquery.trim(), citycode, removequerystatus, request);
		} else if("importremove".equals(type)){
			m_result = ImportExportDAO.importFile(filename,serviceid,1);
		} else if ("removeproducewordpat".equals(type)) {// 显示别名
			m_result = QuerymanageDAO.removeProduceWordpat(combition, flag, request);
		} else if ("addOtherWord".equals(type)) {// 新增别名
			m_result = QuerymanageDAO.addOtherWord(combition, content);
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

	public String getServiceid() {
		return serviceid;
	}

	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getIoa() {
		return ioa;
	}

	public void setIoa(String ioa) {
		this.ioa = ioa;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getQuerytype() {
		return querytype;
	}

	public void setQuerytype(String querytype) {
		this.querytype = querytype;
	}

	public String getNormalquery() {
		return normalquery;
	}

	public void setNormalquery(String normalquery) {
		this.normalquery = normalquery;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public String getCustomerquery() {
		return customerquery;
	}

	public void setCustomerquery(String customerquery) {
		this.customerquery = customerquery;
	}

	public String getM_request() {
		return m_request;
	}

	public void setM_request(String mRequest) {
		m_request = mRequest;
	}

	public String getResponsetype() {
		return responsetype;
	}

	public void setResponsetype(String responsetype) {
		this.responsetype = responsetype;
	}

	public String getInteracttype() {
		return interacttype;
	}

	public void setInteracttype(String interacttype) {
		this.interacttype = interacttype;
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

	public String getCombition() {
		return combition;
	}

	public void setCombition(String combition) {
		this.combition = combition;
	}
	public void setServletRequest(HttpServletRequest request) {
		this.request = request;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public String getQueryid() {
		return queryid;
	}

	public void setQueryid(String queryid) {
		this.queryid = queryid;
	}

	public String getOldcustomerquery() {
		return oldcustomerquery;
	}

	public void setOldcustomerquery(String oldcustomerquery) {
		this.oldcustomerquery = oldcustomerquery;
	}

	public String getOldcitycode() {
		return oldcitycode;
	}

	public void setOldcitycode(String oldcitycode) {
		this.oldcitycode = oldcitycode;
	}

	public String getKbdataid() {
		return kbdataid;
	}

	public void setKbdataid(String kbdataid) {
		this.kbdataid = kbdataid;
	}

	public String getOldnormalquery() {
		return oldnormalquery;
	}

	public void setOldnormalquery(String oldnormalquery) {
		this.oldnormalquery = oldnormalquery;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getRelatequerytokbdataid() {
		return relatequerytokbdataid;
	}

	public void setRelatequerytokbdataid(String relatequerytokbdataid) {
		this.relatequerytokbdataid = relatequerytokbdataid;
	}

	public String getRelatequery() {
		return relatequery;
	}

	public void setRelatequery(String relatequery) {
		this.relatequery = relatequery;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getClassifyquery() {
		return classifyquery;
	}

	public void setClassifyquery(String classifyquery) {
		this.classifyquery = classifyquery;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getClassified() {
		return classified;
	}

	public void setClassified(String classified) {
		this.classified = classified;
	}

	public String getChecked() {
		return checked;
	}

	public void setChecked(String checked) {
		this.checked = checked;
	}

	public String getChecktimeStart() {
		return checktimeStart;
	}

	public void setChecktimeStart(String checktimeStart) {
		this.checktimeStart = checktimeStart;
	}

	public String getChecktimeEnd() {
		return checktimeEnd;
	}

	public void setChecktimeEnd(String checktimeEnd) {
		this.checktimeEnd = checktimeEnd;
	}

	public String getInserttimeStart() {
		return inserttimeStart;
	}

	public void setInserttimeStart(String inserttimeStart) {
		this.inserttimeStart = inserttimeStart;
	}

	public String getInserttimeEnd() {
		return inserttimeEnd;
	}

	public void setInserttimeEnd(String inserttimeEnd) {
		this.inserttimeEnd = inserttimeEnd;
	}
	
	public String getAbs() {
		return abs;
	}

	public void setAbs(String abs) {
		this.abs = abs;
	}

	public String getScenariosid() {
		return scenariosid;
	}

	public String[] getIds() {
		return ids;
	}

	public void setScenariosid(String scenariosid) {
		this.scenariosid = scenariosid;
	}

	public void setIds(String[] ids) {
		this.ids = ids;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}

	public String[] getKbdataids() {
		return kbdataids;
	}

	public void setKbdataids(String[] kbdataids) {
		this.kbdataids = kbdataids;
	}

	public String[] getAbses() {
		return abses;
	}

	public void setAbses(String[] abses) {
		this.abses = abses;
	}

	public String getCitySelect() {
		return citySelect;
	}

	public void setCitySelect(String citySelect) {
		this.citySelect = citySelect;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}

	public String getUnderstrandinfo() {
		return understrandinfo;
	}

	public void setUnderstrandinfo(String understrandinfo) {
		this.understrandinfo = understrandinfo;
	}

	public String getMultinormalquery() {
		return multinormalquery;
	}

	public void setMultinormalquery(String multinormalquery) {
		this.multinormalquery = multinormalquery;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSearchtype() {
		return searchtype;
	}

	public void setSearchtype(String searchtype) {
		this.searchtype = searchtype;
	}

	public String getIstrain() {
		return istrain;
	}

	public void setIstrain(String istrain) {
		this.istrain = istrain;
	}

	public String getUnderstandstatus() {
		return understandstatus;
	}

	public void setUnderstandstatus(String understandstatus) {
		this.understandstatus = understandstatus;
	}

	public String getNewnormalquery() {
		return newnormalquery;
	}

	public void setNewnormalquery(String newnormalquery) {
		this.newnormalquery = newnormalquery;
	}

	public String getBusinesswords() {
		return businesswords;
	}

	public void setBusinesswords(String businesswords) {
		this.businesswords = businesswords;
	}

	public String getRemovequerystatus() {
		return removequerystatus;
	}

	public void setRemovequerystatus(String removequerystatus) {
		this.removequerystatus = removequerystatus;
	}
	

}
