/**  
 * @Project: JfinalDemo 
 * @Title: FileController.java
 * @Package com.knowology.common.controller
 * @author c_wolf your emai address
 * @date 2014-9-9 下午1:18:08
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.upload.UploadFile;
import com.knowology.common.model.CommonModel;
import com.knowology.common.plugin.SqlQueryPlugin;
import com.knowology.common.render.BaseFileRender;
import com.knowology.common.render.ExcelRender;

import demo.DemoConfig;

/**
 * @内容摘要 ：
 * @ClassName FileController
 * @Company: knowology
 * @author c_wolf your emai address
 * @date 2014-9-9 下午1:18:08
 */

public class FileController extends Controller {
	// 定义日志
	Logger log = Logger.getLogger(FileController.class);
	// 定义返回的json串
	JSONObject result = new JSONObject();
	// 定义集合
	List<Object> listAll = new ArrayList<Object>();
	// 定义导出的map集合
	public static Map<String, JSONObject> exportMap = new ConcurrentHashMap<String, JSONObject>();

	/**
	 * 上传文件,前台的请求可以加上文件上传的路径(不包括初始路径)，这样就可以将文件上传到不同的路径下
	 * 
	 * @param path参数文件上传的路径
	 */
	public void upload() {
		// 获取上传文件的路径
		String path = getPara("path");
		String type = getPara("type");
		// 定义文件上传的集合
		List<UploadFile> uf = null;
		try {
			// 获取文件上传的初始路径
			String pathName = DemoConfig.fileDirectory;
			if (path != null && !"".equals(path)) {
				// 若结尾没有/，那么加上反斜杠
				if (!path.endsWith("/")) {
					path += "/";
				}
				pathName += path;
			}
			if("html".equals(type)){
				String sysPath = System.getProperty("user.dir");
				if(System.getProperty("os.name").toLowerCase().startsWith("win")){
					
					sysPath = sysPath.substring(0,sysPath.lastIndexOf("\\"));
					pathName =sysPath+"/"+"webapps/KM/"+path;	
				}else{
					if(sysPath.endsWith("\\")){
						sysPath = sysPath.substring(0,sysPath.lastIndexOf("\\"));
						pathName =sysPath+"/"+"webapps/KM/"+path;
					}else{
						sysPath = sysPath.substring(0,sysPath.lastIndexOf("/"));
						pathName =sysPath+"/"+"webapps/KM/"+path;
					}
					
						
				}
				
			}
			
			// 将文件上传
			//uf = getFiles(pathName, DemoConfig.maxPostSize, DemoConfig.encoding);
			uf = getFiles(pathName,188599694, DemoConfig.encoding);
		} catch (Exception e) {
			// 出现错误 
			log.error(e.getMessage());
			result.put("state", "fail");
			result.put("message", "文件太大！");
			renderText(result.toJSONString());
			return;
		}
		result.put("state", "success");
		List<Object> names = new ArrayList<Object>();
		for (int i = 0; i < uf.size(); i++) {
			names.add(uf.get(i).getFileName());
		}
		result.put("names", names);
		renderText(result.toJSONString());
	}

	/**
	 * 删除文件
	 * 
	 * @param path参数文件上传的路径
	 */
	public void delete() {
		String fileName = getPara("filename");
		if (fileName == null) {
			fileName = getPara("fileName");
		}
		String pathName = DemoConfig.fileDirectory;
		String path = getPara("path");
		if (path != null && !"".equals(path)) {
			// 若结尾没有/，那么加上反斜杠
			if (!path.endsWith("/")) {
				path += "/";
			}
			pathName += path;
		}
		String abPath = pathName + fileName;
		File file = new File(abPath);
		if (file.isFile() && file.exists()) {
			file.delete();
		}
		result.put("state", "success");
		renderJson(result);
	}

	/**
	 * 下载文件
	 * 
	 * @param path参数文件上传的路径
	 */
	public void download() {
		String fileName = getPara("fileName");
		if (fileName == null) {
			fileName = getPara("filename");
		}
		String path = getPara("path");
		if (path != null && !"".equals(path)) {
			// 若结尾没有/，那么加上反斜杠
			if (!path.endsWith("/")) {
				path += "/";
			}
			fileName = path + fileName;
		}
		render(new BaseFileRender(fileName));
	}

	// @Before({CommonCacheInterceptor.class})
	// @CacheName("ajax_query")
	@SuppressWarnings("unchecked")
	public void export() {// 导出数据都采用分页sql，兼容进度条
		String fileName = getPara("fileName");// 获取导出文件名称
		if (fileName == null) {
			fileName = getPara("filename");
		}
		String exportId = creatKey();// 存储导出进度
		if (exportMap.containsKey(exportId)) {
			result = exportMap.get(exportId);
			int use = result.getIntValue("use");
			result.put("use", use + 1);//
			while (true) {
				int state = result.getIntValue("state");
				if (state >= 3) {
					listAll = (List) result.get("list");
					try {
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else {
			result.put("state", 1);// 第一步，开始，第二步数据查询过程，第三部数据生成表格，第四部数据下载过程
			result.put("list", listAll);
			result.put("use", 1);
			exportMap.put(exportId, result);

			// 获取请求字符串
			String request = getPara("request");
			// 将字符串转为json对象，使用fastjson
			JSONObject obj = JSONObject.parseObject(request);
			// 后台获取到sqlid和参数,并获取到sql语句
			String sqlid = obj.getString("sqlid");
			Object[] paras = null;
			try {
				paras = obj.getJSONArray("paras").toArray();
			} catch (NullPointerException ne) {
				paras = new Object[] {};
			}
			int rows = 5000;
			try {
				if (obj.containsKey("rows"))
					rows = obj.getIntValue("rows");
			} catch (Exception e) {
				rows = DemoConfig.rows;
			}
			if (rows <= 0) {
				rows = DemoConfig.rows;
			}
			Map sqlmap = SqlQueryPlugin.getSql(sqlid);
			int paramNum = Integer.parseInt(sqlmap.get("paramNum").toString());
			if (paramNum != paras.length) {
				result.put("state", "fail");
				result.put("message", "参数个数不正确！");
			}

			String select = sqlmap.get("select").toString();
			String from = sqlmap.get("from").toString();

			// 获取sql语句，并尝试第一次查询数据，查询10000条，这只是一步测试环节，获得总数据，并计算进度。并且后面也是每次只查询一万数据
			Page pageF = CommonModel.executeQueryPage(1, rows, select, from,
					paras);// 查询第一次的分页数据，最大为10000条
			int count = pageF.getTotalRow();// 总数据条数
			int pageNum = pageF.getTotalPage();// 总页数
			result.put("state", 2);
			result.put("total", count);
			result.put("index", pageF.getList().size());
			listAll.addAll(pageF.getList());
			if (pageNum > 1) {// 如果不止一页，那么继续查询
				for (int i = 2; i <= pageNum; i++) {
					pageF = null;
					System.gc();
					pageF = CommonModel.executeQueryPage(i, rows, select, from,
							paras);
					listAll.addAll(pageF.getList());
					result
							.put("index", rows * (i - 1)
									+ pageF.getList().size());
				}
			}
		}
		result.put("state", 3);
		render(new ExcelRender(listAll, fileName, exportId));
	}

	public void progress() {
		JSONObject o = new JSONObject();
		String exportId = creatKey();
		if (exportMap.containsKey(exportId)) {
			result = exportMap.get(exportId);
			if (result.getInteger("state") == 4) {
				int use = result.getIntValue("use");
				if (use < 2) {
					exportMap.remove(exportId);
					System.out.println("清理exportMap");
				}
				o.put("state", 4);
			} else {
				o.put("state", result.get("state"));
				o.put("index", result.get("index"));
				o.put("total", result.get("total"));
			}
		} else {
			o.put("state", 4);
		}
		renderJson(o);
	}

	public String creatKey() {
		StringBuilder sb = new StringBuilder("/file/export");
		String urlPara = getPara();
		if (urlPara != null)
			sb.append("/").append(urlPara);

		String queryString = getRequest().getQueryString();
		if (queryString != null)
			sb.append("?").append(queryString);

		String parameter = JSONObject.toJSONString(getParaMap());
		sb.append(parameter);
		return sb.toString();
	}
}