package com.knowology.km.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.knowology.km.bll.RelatequeryDao;
import com.knowology.km.bll.RelatequeryDaoImpl;
import com.knowology.km.dto.CombData;
import com.knowology.km.dto.DataGridResponse;
import com.knowology.km.dto.Response;
import com.knowology.km.entity.Relatequery;
import com.knowology.km.excepion.RelateException;

public class RelatequeryAction extends BaseAction {
	private static final long serialVersionUID = 1L;
	
	//RelatequeryService rqService = new RelatequeryServiceImpl();
	RelatequeryDao rqDao = new RelatequeryDaoImpl();
	
	private Long id;
	private String relatequeryStr;
	private Relatequery relatequery;
	private Long[] ids;
	private Long kbdataid;
	private String ioa;
	
	public String index(){
		return "indexPage";
	}

	public String addPage(){
		return "addPage";
	}
	
	public String modPage(){
		relatequery = rqDao.getById(id);
		return "modPage";
	}
	
	public String list(){
		String brand = getBrandFromIoa(ioa);
		List<Relatequery> list = rqDao.getByKbdataidWithFilter(kbdataid,brand, relatequeryStr, page, rows);
		Integer count = rqDao.count(kbdataid, brand, relatequeryStr);
		DataGridResponse response = new DataGridResponse(list,count,true);
		setResponse(response);
		
		return "success";
	}
	
	public String listRelatequeries(){
		String brand = getBrandFromIoa(ioa);
		List<CombData> list = rqDao.listRelatequreies(brand);
		Response response = new Response(list, true);
		setResponse(response);
		return "success";
	}
	
	public String add(){
		try {
			rqDao.add(relatequery);
			setResponse(new Response(null, true));
		} catch (RelateException e) {
			setResponse(new Response(null, false, e.getMessage()));
		}
		return "success";
	}
	
	public String modify(){
		try {
			rqDao.update(relatequery);
			setResponse(new Response(null, true));
		} catch (RelateException e) {
			setResponse(new Response(null, false, e.getMessage()));
		}
		return "success";
	}
	
	public String delete(){
		try {
			rqDao.deleteBatch(ids);
			setResponse(new Response(null, true));
		} catch (RelateException e) {
			setResponse(new Response(null, false, e.getMessage()));
		}
		return "success";
	}
	
	/**
	 * 取出brand
	 * @param ioa
	 * @return
	 */
	private String getBrandFromIoa(String ioa){
		if(StringUtils.isBlank(ioa)){
			return "";
		}
		return ioa.split("->")[1];
	} 
	
	/////--getter/setter--/////
	protected int page;
	protected int rows;
	private Map<String, Object> response;

	public Map<String, Object> getResponse() {
		return response;
	}

	public void setResponse(Map<String, Object> response) {
		this.response = response;
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

	protected void setResponse(Object obj){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("response", obj);
		setResponse(map);
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Relatequery getRelatequery() {
		return relatequery;
	}

	public void setRelatequery(Relatequery relatequery) {
		this.relatequery = relatequery;
	}

	public Long[] getIds() {
		return ids;
	}

	public void setIds(Long[] ids) {
		this.ids = ids;
	}

	public Long getKbdataid() {
		return kbdataid;
	}

	public void setKbdataid(Long kbdataid) {
		this.kbdataid = kbdataid;
	}

	public String getRelatequeryStr() {
		return relatequeryStr;
	}

	public void setRelatequeryStr(String relatequeryStr) {
		this.relatequeryStr = relatequeryStr;
	}

	public RelatequeryDao getRqDao() {
		return rqDao;
	}

	public void setRqDao(RelatequeryDao rqDao) {
		this.rqDao = rqDao;
	}

	public String getIoa() {
		return ioa;
	}

	public void setIoa(String ioa) {
		this.ioa = ioa;
	}
}
