package com.knowology.km.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.knowology.km.bll.ClassifyqueryDao;
import com.knowology.km.bll.QuerymanageDAO;

public class FileDownloadAction extends BaseAction{
	private String type;
	private InputStream fileInputStream;
	private String fileName;
	private String classifyquery;
	private String city;
	private String serviceid;
	private String normalquery;
	private String classified;
	private String checked;
	private String checktimeStart;
	private String checktimeEnd;
	private String inserttimeEnd;
	private String inserttimeStart;
	private String responsetype;
	private String interacttype;
	
	public String execute(){
		File file = null;
		if("classifyqueryexport".equals(type)){//分类问题导出
			file = ClassifyqueryDao.exportFile(classifyquery, city, serviceid, normalquery, classified, checked, checktimeStart, checktimeEnd, inserttimeStart, inserttimeEnd);
		}else if("querymanageexport".equals(type)){//导出问题
			file = QuerymanageDAO.exportFile(serviceid, normalquery, responsetype, interacttype);
		}else if("wordpatexport".equals(type)){//导出词模
			file = new File(QuerymanageDAO.FILE_PATH_EXPORT + fileName);
		}else if("removequerymanageexport".equals(type)){//导出排除问题
			file = QuerymanageDAO.exportFileRemove(serviceid, normalquery, responsetype, interacttype);
		}
		try {
			if(file != null){
				fileInputStream = new FileInputStream(file);
				//fileName = new String(file.getName().getBytes(), "ISO8859-1");
				fileName = file.getName();
				return "export";
			}else{
				fileInputStream = IOUtils.toInputStream("{'success':false, 'msg':'No data.'}");
				return "jsonstring";
			}
		} catch (Exception e) {
			fileInputStream = IOUtils.toInputStream("{'success':false, 'msg':'Internal Error.'}");
			return "jsonstring";
		}
	}
	
	public InputStream getFileInputStream() {
		return fileInputStream;
	}
	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getServiceid() {
		return serviceid;
	}

	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}

	public String getNormalquery() {
		return normalquery;
	}

	public void setNormalquery(String normalquery) {
		this.normalquery = normalquery;
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

	public String getInserttimeEnd() {
		return inserttimeEnd;
	}

	public void setInserttimeEnd(String inserttimeEnd) {
		this.inserttimeEnd = inserttimeEnd;
	}

	public String getInserttimeStart() {
		return inserttimeStart;
	}

	public void setInserttimeStart(String inserttimeStart) {
		this.inserttimeStart = inserttimeStart;
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

	
}
