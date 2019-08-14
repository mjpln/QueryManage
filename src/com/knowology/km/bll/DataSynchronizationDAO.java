package com.knowology.km.bll;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.knowology.km.dal.Database;


public class DataSynchronizationDAO {
	// public static String dataSynPath = Database
	// .getJDBCValues("fileDirectory")
	// + File.separator + "dataSyn" ;
	public static Logger logger = Logger.getLogger("datasyn");
    //public static String dataSynPath = Database.getJDBCValues("fileDirectory").endsWith("/") ? Database.getJDBCValues("fileDirectory") : Database.getJDBCValues("fileDirectory")+"/";
     public static String dataSynPath = Database.getJDBCValues("fileDirectory").endsWith("/") ? Database.getJDBCValues("fileDirectory") : Database.getJDBCValues("fileDirectory")+File.separator;

	public static boolean flag = true;
	public static int testcount = 0;
	public static int totalCount = 0;
	public static int wrongcount = 0;
	public static String testtime = "";
	public static int status = 0;
	public static String filename = "";
	
	public static String synGeXingHuaService = Database.getJDBCValues("syngexinghuaservice");
	

	/**
	 * 分页查询满足条件的数据
	 * 
	 * @param question参数标准问题
	 * @param page参数页数
	 * @param rows参数每页条数
	 * @return json串
	 */
	public static Object selectQuery(String from,String syntype, int page, int rows) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义SQL语句
		StringBuilder sql = new StringBuilder();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		try {
			// 获取servicetype
			String servicetype = MyClass.IndustryOrganizationApplication();
			// 定义查询满足条件的数量的SQL语句
			sql
					.append(" select count(*) count from ( select * from datasyn where bakid is null   ");

			// 判断标准问题是否为null，空，
			if (syntype != null && !"".equals(syntype)
					&& !"choose".equals(syntype)) {
				// 加上标准问题的条件
				sql.append(" and synobject = ? ");
				// 绑定标准问题参数
				lstpara.add(syntype);
			}
			
			if("organization".equals(from)){
				sql.append(" and synobject not in('answer','service','similarquestion','todosth') ");
			}else if("cloud".equals(from)){
				sql.append(" and synobject not in('word') ");
			}
			sql
					.append(" ) ds  left join  ( select * from datasyn where bakid is not null) dsb  on  ds.id=dsb.bakid");

			// 执行SQL语句，获取相应的数据源
			Result rs = Database
					.executeQuery(sql.toString(), lstpara.toArray());
			// 判断数据源不为null,且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 将数量放入jsonObj的total对象中
				jsonObj.put("total", rs.getRows()[0].get("count"));
				// 定义SQL语句
				sql = new StringBuilder();
				// 定义绑定参数集合
				lstpara = new ArrayList<Object>();
				// 分页查询满足条件的SQL语句
				// sql
				// .append("select * from (select rownum rn,r.name,r.synobject,r.type,r.bakid,r.state,to_char(r.time,'yyyy-MM-dd HH24:mi:ss') time from datasyn  r where 1>0");

				sql
						.append("select * from (select A.*, rownum rn from( select ds.id id, ds.name name ,to_char(ds.time,'yyyy-MM-dd HH24:mi:ss') uploadtime ,ds.state state ,ds.synobject synobject,ds.type ,ds.servicetype, dsb.name bakname,dsb.state bakstate , dsb.type baktype  ,to_char(dsb.time,'yyyy-MM-dd HH24:mi:ss') finishtime from ( select * from datasyn where bakid is null    ");
				// 判断标准问题是否为null，空
				if (syntype != null && !"".equals(syntype)
						&& !"choose".equals(syntype)) {
					// 加上同步类型的条件
					sql.append(" and synobject = ?  ");
					// 绑定标准问题参数
					lstpara.add(syntype);
				}
				if("organization".equals(from)){
					sql.append(" and synobject not in('answer','service','similarquestion','todosth') ");
				}else if("cloud".equals(from)){
					sql.append(" and synobject not in('word') ");
				}
				
				sql
						.append("   ) ds  left join  ( select * from datasyn where bakid is not null  ) dsb  on ds.id=dsb.bakid   order by uploadtime desc  ");
				// 加上分页的条件
				sql
						.append("  ) A  where rownum<=? )  where rn >=? ");
				// 绑定截止条数参数
				lstpara.add(page * rows);
				// 绑定开始条数参数
				lstpara.add((page - 1) * rows);
				
				// 执行SQL语句，获取相应的数据源
				rs = Database.executeQuery(sql.toString(), lstpara.toArray());
				// 判断数据源不为nul且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 循环遍历数据源
					for (int i = 0; i < rs.getRowCount(); i++) {
						// 定义json对象
						JSONObject obj = new JSONObject();

						// 生成对象
						obj.put("id", rs.getRows()[i].get("id") == null ? ""
								: rs.getRows()[i].get("id").toString());
						obj.put("name",
								rs.getRows()[i].get("name") == null ? "" : rs
										.getRows()[i].get("name").toString());
						obj.put("bakname",
								rs.getRows()[i].get("bakname") == null ? ""
										: rs.getRows()[i].get("bakname")
												.toString());
						obj.put("uploadtime",
								rs.getRows()[i].get("uploadtime") == null ? ""
										: rs.getRows()[i].get("uploadtime")
												.toString());
						obj.put("finishtime",
								rs.getRows()[i].get("finishtime") == null ? ""
										: rs.getRows()[i].get("finishtime")
												.toString());
						obj.put("synobject",
								rs.getRows()[i].get("synobject") == null ? ""
										: rs.getRows()[i].get("synobject")
												.toString());
						obj.put("state",
								rs.getRows()[i].get("state") == null ? "" : rs
										.getRows()[i].get("state").toString());
						obj.put("bakstate",
								rs.getRows()[i].get("bakstate") == null ? ""
										: rs.getRows()[i].get("bakstate")
												.toString());
						obj
								.put("servicetype", rs.getRows()[i]
										.get("servicetype") == null ? "" : rs
										.getRows()[i].get("servicetype")
										.toString());
						// 将生成的对象放入jsonArr数组中
						jsonArr.add(obj);
					}
				}
				// 将jsonArr数组放入jsonObj的rows对象中
				jsonObj.put("rows", jsonArr);
			} else {
				// 将0放入jsonObj的total对象中
				jsonObj.put("total", 0);
				// 清空jsonArr数组
				jsonArr.clear();
				// 将jsonArr数组放入jsonObj的rows对象中
				jsonObj.put("rows", jsonArr);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 清空jsonArr数组
			jsonArr.clear();
			// 将jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	/**
	 * 恢复全量同步数据
	 * 
	 * @param filename参数文件名称
	 * @param type
	 *            同步对象
	 * @return recoverData
	 */
	public static Object recoverData(String name, String type) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义错误信息
		// 将false放入jsonObj的success对象中
		jsonObj.put("success", false);
		// 将内容为空放入jsonObj的msg对象中
		jsonObj.put("msg", "恢复失败!");
		String serviceRoot = MyClass.ServiceRoot();
		String industryOrganizationApplication = MyClass
				.IndustryOrganizationApplication();
		// 获取业务根
		// 判断serviceRoot、industryOrganizationApplication为空串、空、null
		if (" ".equals(serviceRoot) || "".equals(serviceRoot)
				|| serviceRoot == null
				|| " ".equals(industryOrganizationApplication)
				|| "".equals(industryOrganizationApplication)
				|| industryOrganizationApplication == null) {
			jsonObj = new JSONObject();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("msg", "登录信息已失效,请注销后重新登录!");
			return jsonObj;
		}
		// 获取文件的路径
		String 	pathName = dataSynPath  + "datasyn"
			+ File.separator + "databak" + File.separator + type
			+ File.separator + name;
		
		// 读取文件的数据
		String encoding = Database.getJDBCValues("datarecoverreadencoding");
		List<Map<String, String>> info = readTxt(pathName, type,encoding);
		
		if (info == null) {
			jsonObj = new JSONObject();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将内容为空放入jsonObj的msg对象中
			jsonObj.put("msg", "读取备份文件异常或文件不存在!");
			return jsonObj;
		}
		if(info.size()==0){
			jsonObj = new JSONObject();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将内容为空放入jsonObj的msg对象中
			jsonObj.put("msg", "备份文件内容为空!");
			return jsonObj;
		}
		// 如果文件内容不符合当前同步的对象 返回提示信息
		if (info.size() > 0) {
			if (info.get(0).containsKey("info")) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将导入失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", info.get(0).get("info"));
				return jsonObj;
			}
		}

		if ("wordpat".equals(type)) {
			// info每个元素中包含参数值：serviceid,serviceid_brand,abstract,kbdataid,brand,wordpatid,wordpat,simplewordpat,wordpattype,city,autosendswitch,edittime
			if (recoverWordpatByAll(info, name, type, serviceRoot)) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将导入失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "恢复成功!");
				return jsonObj;
			}
		}else if("word".equals(type)){
			if (recoverWordByWordpatUsed(info, name, type, serviceRoot)) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将导入失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "恢复成功!");
				return jsonObj;
			}
		}else if("answer".equals(type)){
			if (recoverAnswerByAll(info, name, type, serviceRoot,industryOrganizationApplication)) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将导入失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "恢复成功!");
				return jsonObj;
			}
			
		}else if("similarquestion".equals(type)){
			if (recoverSimilarquestionByAll(info, name, type, serviceRoot,industryOrganizationApplication)) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将导入失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "恢复成功!");
				return jsonObj;
			}
			
		}
		
		else if("todosth".equals(type)){
			if (recoverToDoSthByTime(info, name, type, serviceRoot,industryOrganizationApplication)) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将导入失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "恢复成功!");
				return jsonObj;
			}
		}
		return jsonObj;
	}

	/**
	 * 恢复增量同步数据
	 * 
	 * @param filename参数文件名称
	 * @param type
	 *            同步对象
	 * @return recoverData
	 */
	public static Object recoverDataByInc(String name, String type) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义错误信息
		// 将false放入jsonObj的success对象中
		jsonObj.put("success", false);
		// 将内容为空放入jsonObj的msg对象中
		jsonObj.put("msg", "恢复失败!");
		String serviceRoot = MyClass.ServiceRoot();
		String industryOrganizationApplication = MyClass
				.IndustryOrganizationApplication();
		// 获取业务根
		// 判断serviceRoot、industryOrganizationApplication为空串、空、null
		if (" ".equals(serviceRoot) || "".equals(serviceRoot)
				|| serviceRoot == null
				|| " ".equals(industryOrganizationApplication)
				|| "".equals(industryOrganizationApplication)
				|| industryOrganizationApplication == null) {
			jsonObj = new JSONObject();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("msg", "登录信息已失效,请注销后重新登录!");
			return jsonObj;
		}
		// 获取文件的路径
		String 	pathName = dataSynPath  + "datasyn"
			+ File.separator + "databak" + File.separator + type
			+ File.separator + name;
		
		// 读取文件的数据
		String encoding = Database.getJDBCValues("datarecoverreadencoding");
		List<Map<String, Map<String,String>>> info = readTxtByInc(pathName, type,encoding);
		
		if (info == null) {
			jsonObj = new JSONObject();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将内容为空放入jsonObj的msg对象中
			jsonObj.put("msg", "读取备份文件异常或文件不存在!");
			return jsonObj;
		}
		if(info.size()==0){
			jsonObj = new JSONObject();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将内容为空放入jsonObj的msg对象中
			jsonObj.put("msg", "备份文件内容为空,无需备份!");
			return jsonObj;
		}
		// 如果文件内容不符合当前同步的对象 返回提示信息
		if (info.size() > 0) {
			if (info.get(0).containsKey("info")) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将导入失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", info.get(0).get("info").get("info"));
				return jsonObj;
			}
		}

	   if("service".equals(type)){//恢复业务增量更新数据
		   if(recoverServiceByInc(info,name,  type, serviceRoot)){
			   jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将导入失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "恢复成功!");
				return jsonObj;
		   }
			
		}else if("kbdata".equals(type)){//恢复摘要增量更新数据
			 if(recoverKbdataByInc(info,name,  type, serviceRoot)){
				   jsonObj = new JSONObject();
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将导入失败信息放入jsonObj的msg对象中
					jsonObj.put("msg", "恢复成功!");
					return jsonObj;
			   }
		}

		return jsonObj;
	}
	
	
	/**
	 * 将对象文件记录导入同步目标库中
	 * 
	 * @param filename参数文件名称
	 * @param type
	 *            同步对象
	 * @return
	 */

	public static Object importFile(String name, String type) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义错误信息
		// 将false放入jsonObj的success对象中
		jsonObj.put("success", false);
		// 将内容为空放入jsonObj的msg对象中
		jsonObj.put("msg", "同步失败!");

		String serviceRoot = MyClass.ServiceRoot();
		String industryOrganizationApplication = MyClass
				.IndustryOrganizationApplication();
		// 获取业务根
		// 判断serviceRoot、industryOrganizationApplication为空串、空、null
		if (" ".equals(serviceRoot) || "".equals(serviceRoot)
				|| serviceRoot == null
				|| " ".equals(industryOrganizationApplication)
				|| "".equals(industryOrganizationApplication)
				|| industryOrganizationApplication == null) {
			jsonObj = new JSONObject();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("msg", "登录信息已失效,请注销后重新登录!");
			return jsonObj;
		}

		// filename = name;
		// 获取文件的路径
		String	 pathName = dataSynPath + "datasyn"
				+ File.separator + "upload" + File.separator + type
				+ File.separator + name;
		
		// 读取文件的数据
		String encoding = Database.getJDBCValues("datasynreadencoding");
		List<Map<String, String>> info = readTxt(pathName, type,encoding);
		

		if (info==null) {
			jsonObj = new JSONObject();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将内容为空放入jsonObj的msg对象中
			jsonObj.put("msg", "读取同步文件异常或文件不存在!");
			return jsonObj;
		}
		if (info.size() == 0) {
			jsonObj = new JSONObject();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将内容为空放入jsonObj的msg对象中
			jsonObj.put("msg", "同步文件内容为空，无需同步!");
			return jsonObj;
		}
		// 如果文件内容不符合当前同步的对象 返回提示信息
		if (info.get(0).containsKey("info")) {
			jsonObj = new JSONObject();
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将导入失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", info.get(0).get("info"));
			return jsonObj;
		}
		//System.out.println("读取文件------> " + info.size() + " 行");
		serviceRoot = getServiceRoot(serviceRoot);
		if ("wordpat".equals(type)) {//词模全量同步 （商家-->云平台或云平台-->商家）
			if (insertWordpatByAll(info, name, type, serviceRoot,
					industryOrganizationApplication)) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "同步成功!");
				return jsonObj;
			}
		} else if ("word".equals(type)) {//词类词条时间段全量同步（云平台->商家）
			if (insertWordByWordpatUsed(info, name, type, serviceRoot,
					industryOrganizationApplication)) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "同步成功!");
				return jsonObj;
			}
		}else if("service".equals(type)){//业务增量同步（商家-->云平台）
			  String rs = insertServiceByInc(info, name, type, serviceRoot,
						industryOrganizationApplication);
			  if("同步成功!".equals(rs)){
				  jsonObj = new JSONObject();
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将内容为空放入jsonObj的msg对象中
					jsonObj.put("msg", "同步成功!");
			  }else{
				  jsonObj = new JSONObject();
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将内容为空放入jsonObj的msg对象中
					jsonObj.put("msg", rs);
					
			  }
			  return jsonObj;
			
		}else if("kbdata".equals(type)){//摘要增量同步（商家-->云平台）
		  String rs = insertKbdataByInc(info, name, type, serviceRoot,
					industryOrganizationApplication);
		  if("同步成功!".equals(rs)){
			  jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "同步成功!");
		  }else{
			  jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", rs);
		  }
			
		  return jsonObj;
		}else if("answer".equals(type)){//答案全量同步（商家-->云平台）
			if (insertAnswerByAll(info, name, type, serviceRoot,
					industryOrganizationApplication)) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "同步成功!");
				return jsonObj;
			}
		}else if("similarquestion".equals(type)){//相似问题全量同步 （商家-->云平台）
			if (insertSimilarquestionByAll(info, name, type, serviceRoot,
					industryOrganizationApplication)) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "同步成功!");
				return jsonObj;
			}
		}else if("todosth".equals(type)){//待办区时间段同步
			if (insertTodoSthByAll(info, name, type, serviceRoot,
					industryOrganizationApplication)) {
				jsonObj = new JSONObject();
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", true);
				// 将内容为空放入jsonObj的msg对象中
				jsonObj.put("msg", "同步成功!");
				return jsonObj;
			}
		}
		
		return jsonObj;

	}

	/**
	 * 
	 * 读出文件txt数据
	 * 
	 * @param name参数文件路径
	 * @return 数据
	 */
	public static List<Map<String, String>> readTxt(
			String fileNamePath, String type,String encoding) {
		// 定义内容的集合
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Map<String, String> map;
		// 定义读文件流
		BufferedReader reader = null;
		// 定义文件每一行的内容变量
		String s = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(fileNamePath)),encoding));
			
			int i = 0;
			// 循环遍历文件的每一行
			while ((s = reader.readLine()) != null) {
				try {
					// 判断每一行是否为空
					if (!"".equals(s.trim())) {
						i++;
						if (i == 1) {// 通过第一行文件配置判断是否是type类型的文本文件
							if("word".equals(type)){//特殊处理type=word 判断标识
								type = "wordclass";
							}else if("kbdata".equals(type)){
								type = "topic";
							}else if("answer".equals(type)){
								type ="kbanswerid";
							}else if("wordpat".equals(type)){
								type = "wordpattype";
							}else if("service".equals(type)){
								type ="parentname";
							}else if("answer".equals(type)){
								type = "kbanswerid";
							}else if("similarquestion".equals(type)){
								type ="question";
							}else if("todosth".equals(type)){
								type ="inserttime";
							}
							if (s.indexOf(type.toUpperCase()) == -1) {
								map = new HashMap<String, String>();
								map.put("info", "上传文件内容和同步对象不一致，请删除后重新上传再操作!");
								list.add(map);
								return list;
							}
						}
						// 反序列化行记录
						map = JSON.parseObject(s,
								new TypeReference<Map<String, String>>() {
								});
						list.add(map);
					}
				} catch (Exception e) {
					logger.error("读取文件失败【"+fileNamePath+"】",e);
					continue;
				}
			}
			// 关闭文件流
			reader.close();
		} catch (Exception e) {
			logger.error("读取文件失败【"+fileNamePath+"】",e);
			return null;
		}
		return list;
	}

	
	/**
	 * 
	 * 读出文件txt数据
	 * 
	 * @param name参数文件路径
	 * @return 数据
	 */
	private static List<Map<String, Map<String,String>>> readTxtByInc(
			String fileNamePath, String type,String encoding) {
		// 定义内容的集合
		List<Map<String, Map<String,String>>> list = new ArrayList<Map<String, Map<String,String>>>();
		Map<String, Map<String,String>> map;
		Map<String,String> info;
		
		// 定义读文件流
		BufferedReader reader = null;
		// 定义文件每一行的内容变量
		String s = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(fileNamePath)),encoding));
			// 循环遍历文件的每一行
			while ((s = reader.readLine()) != null) {
				try {
					// 判断每一行是否为空
					if (!"".equals(s.trim())) {
						// 反序列化行记录
						map = JSON.parseObject(s,
								new TypeReference<Map<String, Map<String,String>>>() {
								});
						list.add(map);
					}
				} catch (Exception e) {
					logger.error("读取文件失败【"+fileNamePath+"】",e);
					continue;
				}
			}
			// 关闭文件流
			reader.close();
		} catch (Exception e) {
			logger.error("读取文件失败【"+fileNamePath+"】",e);
			return null;
		}
		return list;
	}

	
	
	public static JSONObject dbTotxt(String type) {
		JSONObject jsonObj = new JSONObject();
		String serviceRoot = MyClass.ServiceRoot();
		String industryOrganizationApplication =  MyClass.IndustryOrganizationApplication();
		if (" ".equals(serviceRoot) || "".equals(serviceRoot)
				|| serviceRoot == null  || " ".equals(industryOrganizationApplication) || "".equals(industryOrganizationApplication)
				|| industryOrganizationApplication == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("flag", false);
			// 将内容为空放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时，请注销后重新登录操作!");
			return jsonObj;
		}
		List<String> list = new ArrayList<String>();
		String sql = null;
		String	path = dataSynPath + "datasyn" + File.separator + "download"
			+ File.separator + type;	
		File file = new File(path);
		// 判断文件夹是否存在,如果不存在则创建文件夹 创建失败返回
		if (!mkdir(path)) {
			jsonObj.put("flag", false);
			return jsonObj; 
		}
    
		String name = null;
		
		serviceRoot = getServiceRoot(serviceRoot);

		if ("wordpat".equals(type)) {// 词模知识
			name = "词模.txt";
			path = path + File.separator + name;
				sql = "select s.serviceid serviceid ,s.serviceid_brand serviceid_brand,k.kbdataid_brand, k.abstract abstract  ,w.kbdataid kbdataid,s.brand brand,w.wordpatid wordpatid,w.wordpat wordpat ,w.simplewordpat simplewordpat,w.wordpattype wordpattype ,w.city city,w.autosendswitch autosendswitch ,w.edittime edittime  from  service s,kbdata k ,wordpat w where s.serviceid = k.serviceid  and k.kbdataid = w.kbdataid  and s.brand in("
					+ serviceRoot
					+ ") and k.abstract is not null and w.wordpat is not null";
				
//				sql = "select s.serviceid serviceid ,s.serviceid_brand serviceid_brand,k.kbdataid_brand, k.abstract abstract  ,w.kbdataid kbdataid,s.brand brand,w.wordpatid wordpatid,w.wordpat wordpat ,w.simplewordpat simplewordpat,w.wordpattype wordpattype ,w.city city,w.autosendswitch autosendswitch ,w.edittime edittime  from  service s,kbdata k ,wordpat w where s.serviceid = k.serviceid  and k.kbdataid = w.kbdataid  and s.brand in ('个性化业务') and k.abstract is not null and w.wordpat is not null";	
			
			list = select(sql);
		} else if ("word".equals(type)) {// 词条知识
			name = "词条.txt";
			path = path + File.separator + name;
			Set<String> set = new HashSet<String>();
			set = selectWordclassByWordpatUsered(serviceRoot);
			list = selectWordcalssAndWordByWordpatUsered(set);
		}else if("service".equals(type)){//业务知识
			name ="业务.txt" ;
			path = path + File.separator + name;
			sql = "select aa.serviceid ,aa.serviceid_brand,aa.service,aa.brand brand ,aa.parentname,aa.parentid   from service aa  where aa.brand in ("+serviceRoot+") ";
			list = select(sql);
		}else if("kbdata".equals(type)){//业务摘要知识
			name ="摘要.txt" ;
			path = path + File.separator + name;
			//sql = "select aa.serviceid service_id ,aa.serviceid_brand,aa.service,aa.brand ,aa.parentname,aa.parentid ,bb.* from (select * from service  where brand in ("+serviceRoot+")) aa  left join (select k.* from  service s,kbdata k  where s.serviceid = k.serviceid  and s.brand in("+serviceRoot+") and k.abstract is not null ) bb  on aa.serviceid = bb.serviceid ";
			sql = "select  aa.serviceid service_id ,aa.serviceid_brand,aa.service,aa.brand ,aa.parentid ,bb.*  from service aa ,kbdata  bb where aa.serviceid = bb.serviceid  and aa.brand in ("+serviceRoot+")";
			list = select(sql);
		}else if("answer".equals(type)){//答案知识
			name ="答案.txt" ;
			path = path + File.separator + name;
				sql = "select  a.serviceid  ,a.serviceid_brand,a.service,a.brand sbrand ,b.kbdataid,b.kbdataid_brand,b.abstract,to_char(c.begintime,'yyyy-MM-dd HH24:mi:ss') begintime  ,to_char(c.endtime,'yyyy-MM-dd HH24:mi:ss') endtime  ,f.channel,f.ANSWERCATEGORY,f.SERVICETYPE ,f.CUSTOMERTYPE KB_CUSTOMERTYPE ,g.*  from service a,kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f,kbanswer g where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid and f.kbcontentid=g.kbcontentid   and  a.brand  in("+serviceRoot+") and  (f.SERVICETYPE ='"+industryOrganizationApplication+"' or f.servicetype ='sys')";
				//sql = "select  a.serviceid  ,a.serviceid_brand,a.service,a.brand sbrand ,b.kbdataid,b.kbdataid_brand,b.abstract,to_char(c.begintime,'yyyy-MM-dd HH24:mi:ss') begintime  ,to_char(c.endtime,'yyyy-MM-dd HH24:mi:ss') endtime  ,f.channel,f.ANSWERCATEGORY,f.SERVICETYPE ,f.CUSTOMERTYPE KB_CUSTOMERTYPE ,g.*  from service a,kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f,kbanswer g where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid and f.kbcontentid=g.kbcontentid   and  a.brand  in('个性化业务') and  (f.SERVICETYPE ='"+industryOrganizationApplication+"' or f.servicetype ='sys')";
				//sql = "select  a.serviceid service_id ,a.serviceid_brand,a.service,a.brand ,b.kbdataid,b.kbdataid_brand,b.abstract,c.begintime,c.endtime ,f.channel,f.ANSWERCATEGORY,f.SERVICETYPE ,f.CUSTOMERTYPE KB_CUSTOMERTYPE ,g.*  from service a,kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f,kbanswer g where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid and f.kbcontentid=g.kbcontentid   and  a.brand  in("+serviceRoot+") and  f.SERVICETYPE ='"+industryOrganizationApplication+"' and g.answer_clob is not null";
		    
			list = select(sql);
		}else if("similarquestion".equals(type)){//相似问题
			name ="相似问题.txt" ;
			path = path + File.separator + name;
			sql = "select s.serviceid ,s.serviceid_brand ,s.service,s.brand ,k.kbdataid,k.kbdataid_brand ,k.abstract ,w.questionid,w.question,w.remark, to_char(w.time,'yyyy-MM-dd HH24:mi:ss') time ,w.questiontype  from  service s,kbdata k ,similarquestion w where s.serviceid = k.serviceid  and k.kbdataid = w.kbdataid  and s.brand in("+serviceRoot+") and k.abstract is not null  and w.question is not null ";
			list = select(sql);
		}
     
		else if("todosth".equals(type)){//代办区
			name = "代办区.txt";
			path = path + File.separator + name;
			// 获取同步向前推移天数 (词类词条同步时间一致)
			String synWordclassDay = null;
//			synWordclassDay = Database.getJDBCValues("synWordclassBeforeDay");
			Result rs = null;
			 sql = "select s.name from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name ='数据同步参数配置' and t.name ='待办区同步前移时间'";
			try {
				rs = Database.executeQuery(sql);
				if(rs!=null&&rs.getRowCount()>0){
					synWordclassDay = rs.getRows()[0].get("name").toString();
				}
			} catch (SQLException e1) {
				jsonObj.put("flag", false);
				return jsonObj; 
			}
			sql = "select serviceid, to_char(inserttime,'yyyy-MM-dd HH24:mi:ss') inserttime,state, abstracts,kbdataid,status, id, servicetype  from  t_updrec  where   servicetype ='"+industryOrganizationApplication+"' and inserttime >sysdate -"+synWordclassDay;
			list = select(sql);
			
		}
		String encoding = Database.getJDBCValues("datasynreadencoding");
		if(list!=null){
			if (list.size() > 0) {
				if (writeIntxt(path, list, true,encoding)) {
					jsonObj.put("flag", true);
					jsonObj.put("path", name);
				} else {
					jsonObj.put("flag", false);
					jsonObj.put("flag", "写文件失败!");
				}
			} else {
				jsonObj.put("flag", false);
				jsonObj.put("msg", "当前知识没有变更，无需下载同步");
			}	
		} else{
			jsonObj.put("flag", false);
			jsonObj.put("flag", "查询数据库异常!");
		}
		
		return jsonObj;
	}

	public static List<String> selectWordcalssAndWordByWordpatUsered(
			Set<String> set) {
		// 查询过滤后词类下的所有词条
		StringBuilder sb = new StringBuilder();
		sb
				.append("select wc.wordclass wordclass ,wc.wordclassid  wordclassid ,wc.container container ,w1.word word,w1.wordid wordid, w2.word synonymstr , w2.type type  from (select * from wordclass where (");
		int a = 0;
		if(set.size()>0){
		for (String s : set) {
			a++;
			if (a == 1) {
				sb.append(" wordclass ='" + s + "'");
			} else { 
				sb.append(" or wordclass ='" + s + "'");
			}
		}
		}else{
			sb.append(" 1<0 ");
		}
		sb
				.append(" )) wc  left  join (select * from word where stdwordid is null) w1 on wc.wordclassid = w1.wordclassid   left join (SELECT * FROM word WHERE stdwordid IS not NULL)  w2 on  w2.stdwordid = w1.wordid   order by wc.wordclass ");
		List<String> list = select(sb.toString());
		int si2 = list.size();
		//System.out.println("下载总记录数---------->" + si2);
		return list;
	}

	public static Set<String> selectWordclassByWordpatUsered(String serviceRoot) {
		// 获取同步词类向前推移天数
		String synWordclassDay = null;
//		synWordclassDay = Database.getJDBCValues("synWordclassBeforeDay");
		Result rs = null;
		String sql = "select s.name from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name ='数据同步参数配置' and t.name ='词类词条同步前移时间'";
		try {
			rs = Database.executeQuery(sql);
			if(rs!=null&&rs.getRowCount()>0){
				synWordclassDay = rs.getRows()[0].get("name").toString();
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return null;
		}
		// 查询库中所有词类
		 sql = "select wordclass from wordclass where wordclass is not null and time >sysdate -"
				+ synWordclassDay;
		String wordclass;
		List<String> wordclassList = new ArrayList<String>();
		
		try {
			rs = Database.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if (rs != null) {
			SortedMap<?, ?>[] maps = rs.getRows();
			for (int i = 0; i < maps.length; i++) {
				SortedMap map = maps[i];
				wordclass = map.get("WORDCLASS").toString();
				wordclassList.add(wordclass);
			}

		}
		// 查询当前四层结构下的所有词模
		sql = "select w.wordpat wordpat   from  service s,kbdata k ,wordpat w where s.serviceid = k.serviceid  and k.kbdataid = w.kbdataid  and s.brand in("
				+ serviceRoot
				+ ") and k.abstract is not null and w.wordpat is not null";
		String wordpat;
		List<String> wordpatList = new ArrayList<String>();
		try {
			rs = Database.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		if (rs != null) {
			SortedMap<?, ?>[] maps = rs.getRows();
			for (int i = 0; i < maps.length; i++) {
				SortedMap map = maps[i];
				wordpat = map.get("WORDPAT").toString();
				wordpatList.add(wordpat);
			}

		}

		// 过滤出当前四成机构词模中用到的词类
		Set<String> set = new HashSet<String>();// 存放词模中用到的词类
		for (int i = 0; i < wordpatList.size(); i++) {
			wordpat = wordpatList.get(i);
			for (int k = 0; k < wordclassList.size(); k++) {
				wordclass = wordclassList.get(k);
				if (wordpat.indexOf(wordclass) != -1) {
					set.add(wordclass);
				}
			}
		}
		int si = set.size();
		//System.out.println("词模中使用到的词类数---------->" + si);

		return set;

	}

	public static boolean writeIntxt(String path, List<String> list,
			boolean append,String encoding) {
//		FileWriter fw = null;
//		try {
//			File myFilePath = new File(path);
//			if (myFilePath.exists()) {
//				myFilePath.delete();
//				myFilePath.createNewFile();
//			} else {
//				myFilePath.createNewFile();
//			}
//			// 第二个参数 append 说明文件是重新新建或可扩充
//			fw = new FileWriter(myFilePath, append);
//			for (int i = 0; i < list.size(); i++) {
//				String line = list.get(i);
//				fw.write(line + "\n");
//			}
//			fw.close();
//		} catch (Exception e) {
//			logger.error("写文件失败【"+path+"】",e);
//			return false;
//		}
		   try {   
				File myFilePath = new File(path);
				if (myFilePath.exists()) {
					myFilePath.delete();
					myFilePath.createNewFile();
				
				} else {
					myFilePath.createNewFile();
				}  
		        OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(myFilePath,append),encoding);
		       // OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(myFilePath,append),"UTF-8"); 
		        BufferedWriter writer=new BufferedWriter(write);   
		        for (int i = 0; i < list.size(); i++) {
					String line = list.get(i);
					 writer.write(line + "\n");  
				}
		        writer.close();   
		    } catch (Exception e) {   
		    	logger.error("写文件失败【"+path+"】",e);
				return false;  
		    }  
		return true;
	}
	
	public static boolean writeIntxtBak(String path, List<String> list,
			boolean append,String encoding) {
		   try {   
				File myFilePath = new File(path);
				if (myFilePath.exists()) {
					myFilePath.delete();
					myFilePath.createNewFile();
				} else {
					myFilePath.createNewFile();
				}  
		        OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(myFilePath,append),encoding);
		        //OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(myFilePath,append),"GBK"); 
		        BufferedWriter writer=new BufferedWriter(write);   
		        for (int i = 0; i < list.size(); i++) {
					String line = list.get(i);
					 writer.write(line + "\n");  
				}
		        writer.close();   
		    } catch (Exception e) {   
		    	logger.error("写文件失败【"+path+"】",e);
				return false;  
		    }  
		return true;
	}


	public static boolean insertWordpatByAll(List<Map<String, String>> info,
			String name, String type, String serviceRoot,
			String industryOrganizationApplication) {
		// 首先查询同步库中业务ID 和摘要、 摘要ID 对应关系
		Map<String, Map<String, Map<String, String>>> brand_serviceid_abstract_kbdataid = selectServceidAbstractKbdataid(serviceRoot);

		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		String sql1 = "", sql2 = "", sql3 = "", sql4 = "", sql5 = "";
		// 备份原始数据
		String path = dataSynPath + "datasyn" + File.separator + "databak"
			+ File.separator + type;
		
		if (mkdir(path)) {
			String bakName = name.split("\\.")[0] + "_bak.txt";
			path = path + File.separator + bakName;
			File filePath = new File(path);
			if (!filePath.exists()) {// 如果备份文件不存在则创建文件
				// 查询目标库中原始数据 做同步前备份
				
				String sql0 = "select a.serviceid,a.serviceid_brand,b.abstract,c.* from service a,kbdata b ,wordpat c  where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid  and a.brand in("
						+ serviceRoot
						+ ")  and b.abstract is not null and c.wordpat is not null";
				
	//			String sql0 = "select a.serviceid,a.serviceid_brand,b.abstract,c.* from service a,kbdata b ,wordpat c  where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid  and a.brand in('个性化业务')  and b.abstract is not null and c.wordpat is not null";
				List<String> dataBak = select(sql0);
				if(dataBak==null){
					return  false;
				}
				String encoding = Database.getJDBCValues("datarecoverreadencoding");
				if (writeIntxt(path, dataBak, true,encoding)) {// 写文件成功
					// 定义备份文件 insert 语句
					if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
						sql3 = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
								+ bakName
								+ "','wordpat','备份文件','未恢复',(select id from datasyn where name='"
								+ name
								+ "'),'"
								+ industryOrganizationApplication + "')";
					} else {// 如存在备份文件记录则更新完成时间
						// sql3 =
					   // "update datasyn set time=sysdate where name ='"+bakName+"'";
					}
				} else {// 写文件失败返回
					return false;
				}
				
				
			} else {
				if (!isExist(name, bakName)) {//如果库中不存在备份文件记录则记录
					sql3 = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
							+ bakName
							+ "','wordpat','备份文件','未恢复',(select id from datasyn where name='"
							+ name
							+ "'),'"
							+ industryOrganizationApplication
							+ "')";
				} else {// 如存在备份文件记录则更新完成时间
					// sql3 =
					// "update datasyn set time=sysdate where name ='"+bakName+"'";
				}
			}
			// 定义SQL语句 删除词模
			sql1 = "delete from wordpat ww where ww.wordpatid in(select w.wordpatid from service s,kbdata k, wordpat w where s.serviceid=k.serviceid and k.kbdataid = w.kbdataid  and  s.serviceid in(SELECT serviceid  FROM  service 　　start  WITH service in("
					+ serviceRoot
					+ ")　connect BY nocycle prior serviceid = parentid))";
			
			//sql1 = "delete from wordpat ww where ww.wordpatid in(select w.wordpatid from service s,kbdata k, wordpat w where s.serviceid=k.serviceid and k.kbdataid = w.kbdataid  and  s.serviceid in(SELECT serviceid  FROM  service 　　start  WITH service in('个性化业务')　connect BY nocycle prior serviceid = parentid))";
			// 定义insert 参数
			String serviceid,serviceid_brand, _abstract, kbdataid, wordpatid, wordpat, city, autosendswitch, wordpattype, brand, edittime, simplewordpat;
			// 定义摘要字典
			Map<String, String> abstract_kbdataid;
			// 新增词模sql语句
			sql2 = "insert into wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand,edittime) values(SEQ_WORDPATTERN_ID.nextval,?,?,?,?,?,?,sysdate)";
			// 遍历需insert记录
			for (int i = 0; i < info.size(); i++) {
				Map<String, String> wordpatContent = info.get(i);
				serviceid_brand  = wordpatContent.get("SERVICEID_BRAND");
				serviceid = wordpatContent.get("SERVICEID");
				_abstract = wordpatContent.get("ABSTRACT");
				wordpat = wordpatContent.get("WORDPAT");
				city = wordpatContent.get("CITY");
				autosendswitch = wordpatContent.get("AUTOSENDSWITCH");
				wordpattype = wordpatContent.get("WORDPATTYPE");
				brand = wordpatContent.get("BRAND");
				if (brand_serviceid_abstract_kbdataid.containsKey(brand)) {// 若目标库中存在相同品牌brand
																			// 取brand
																			// 下内容
					Map<String, Map<String, String>> serviceid_abstract_kbdataid = brand_serviceid_abstract_kbdataid
							.get(brand);
					if (serviceid_brand != null) {// 若serviceid_brand不为空优先到同步目标库字典中通过serviceid_brand、_abstract
													// 寻找kbdataid
						if (serviceid_abstract_kbdataid
								.containsKey(serviceid_brand)) {
							abstract_kbdataid = serviceid_abstract_kbdataid
									.get(serviceid_brand);
							kbdataid = abstract_kbdataid.get(_abstract);
						} else {// 如果目标库中不存在 说明该业务已删除 不做同步
							logger.info("目标库中不存业务servcie_brand："+serviceid_brand);
							continue;
						}

					} else {// 若serviceid_brand 为空，通过serviceid 取内容
						if (serviceid_abstract_kbdataid.containsKey(serviceid)) {// 如果目标库字典中存在相同serviceid
																					// 取serviceid
																					// 下内容
							abstract_kbdataid = serviceid_abstract_kbdataid
									.get(serviceid);
							kbdataid = abstract_kbdataid.get(_abstract);
						} else {// 如果目标库中不存在 说明该业务已删除 不做同步
							logger.info("目标库中不存业务servcieid："+serviceid);
							continue;
						}

					}
					if (kbdataid == null) {// 字典中不存在该摘要，说明目标库中摘要或以删除, 修改了???????
						logger.info("目标库中不存在摘要abstract："+_abstract);
						continue;
					}
				} else {// 若目标库中不存在相同品牌brand 说明目标库中数据已删除不做同步
					logger.info("目标库中不存在相同品牌brand："+brand);
					continue;
					
				}

				// 对应绑定参数集合
				// insert into
				// wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand,edittime)
				// values(SEQ_WORDPATTERN_ID.nextval,?,?,?,?,?,?,sysdate)
				lstpara = new ArrayList<Object>();
				// 绑定参数
				lstpara.add(wordpat);
				lstpara.add(city);
				lstpara.add(autosendswitch);
				lstpara.add(wordpattype);
				lstpara.add(kbdataid);
				lstpara.add(brand);

				// 将SQL语句放入集合中
				lstSql.add(sql2);
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(lstpara);
				// String ss =
				// "insert into wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand,edittime) values(SEQ_WORDPATTERN_ID.nextval,'"+wordpat+"','"+city+"',"+autosendswitch+","+wordpattype+","+kbdataid+",'"+brand+"',sysdate)";
				// System.out.println(ss);
			}
			// 批量同步wordpatprecision 表
			sql4 = "insert into wordpatprecision (wordpatid,city,brand,correctnum,callvolume,wpprecision,autoreplyflag,projectflag)  select w.wordpatid wordpatid, w.city city,w.brand brand,0  as correctnum,0 as callvolume,0 as wpprecision,0 as autoreplyflag,0 as projectflag from wordpat w  where w.wordpatid  not in( select wp.wordpatid  from  wordpatprecision wp)";
			// 修改同步文件状态
			sql5 = "update datasyn set state ='同步完成' where name='" + name
					+ "' and bakid is null";
			int rs = Database.executeNonQueryTransactionBatch(lstSql,
					lstLstpara, sql1, sql2, sql3, sql4, sql5);
			if (rs > 0) {
				return true;
			}
		}
		return false;
	}
    
	
	public static boolean insertAnswerByAll(List<Map<String, String>> info,
			String name, String type, String serviceRoot,
			String industryOrganizationApplication){
		// 首先查询同步库中业务ID 和摘要、 摘要ID 对应关系
		Map<String, Map<String, Map<String, String>>> brand_serviceid_abstract_kbdataid = selectServceidAbstractKbdataid(serviceRoot);
		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		String sql ="" ;
		// 备份原始数据
		String path = dataSynPath + "datasyn" + File.separator + "databak"
			+ File.separator + type;
		
		if (mkdir(path)) {
			String bakName = name.split("\\.")[0] + "_bak.txt";
			path = path + File.separator + bakName;
			File filePath = new File(path);
			if (!filePath.exists()) {// 如果备份文件不存在则创建文件
				// 查询目标库中原始数据 做同步前备份
				String sql0 = "select  a.serviceid service ,a.serviceid_brand,a.service,a.brand sbrand ,b.kbdataid,b.kbdataid_brand,b.abstract,to_char(c.begintime,'yyyy-MM-dd HH24:mi:ss') begintime, to_char( c.endtime,'yyyy-MM-dd HH24:mi:ss') endtime ,f.channel,f.ANSWERCATEGORY,f.SERVICETYPE ,f.CUSTOMERTYPE KB_CUSTOMERTYPE ,g.*  from service a,kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f,kbanswer g where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid and f.kbcontentid=g.kbcontentid   and  a.brand  in("+serviceRoot+") and  (f.SERVICETYPE ='"+industryOrganizationApplication+"' or f.SERVICETYPE='sys' )";
				//String sql0 = "select  a.serviceid service ,a.serviceid_brand,a.service,a.brand sbrand ,b.kbdataid,b.kbdataid_brand,b.abstract,to_char(c.begintime,'yyyy-MM-dd HH24:mi:ss') begintime, to_char( c.endtime,'yyyy-MM-dd HH24:mi:ss') endtime ,f.channel,f.ANSWERCATEGORY,f.SERVICETYPE ,f.CUSTOMERTYPE KB_CUSTOMERTYPE ,g.*  from service a,kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f,kbanswer g where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid and f.kbcontentid=g.kbcontentid   and  a.brand  in('个性化业务') and  (f.SERVICETYPE ='"+industryOrganizationApplication+"' or f.SERVICETYPE='sys' )";
				List<String> dataBak = select(sql0);
				if(dataBak==null){
					return  false;
				}
				String encoding = Database.getJDBCValues("datarecoverreadencoding");
				if (writeIntxt(path, dataBak, true,encoding)) {// 写文件成功
					// 定义备份文件 insert 语句\
					if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
						sql = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
								+ bakName
								+ "','answer','备份文件',?,(select id from datasyn where name='"
								+ name
								+ "'),'"
								+ industryOrganizationApplication + "')";
						// 对应绑定参数集合
						lstpara = new ArrayList<Object>();
						lstpara.add("未恢复");
						// 将SQL语句放入SQL语句集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
						
					} else {// 如存在备份文件记录则更新完成时间
						// sql3 =
						// "update datasyn set time=sysdate where name ='"+bakName+"'";
					}
				} else {// 写不成功返回
					return false;
				}
				
				
			} else {
				if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
					sql = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
							+ bakName
							+ "','answer','备份文件',?,(select id from datasyn where name='"
							+ name
							+ "'),'"
							+ industryOrganizationApplication
							+ "')";
					// 对应绑定参数集合
					lstpara = new ArrayList<Object>();
					lstpara.add("未恢复");
					// 将SQL语句放入SQL语句集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
					
					
				} else {// 如存在备份文件记录则更新完成时间
					// sql3 =
					// "update datasyn set time=sysdate where name ='"+bakName+"'";
				}
			}
			// 定义SQL语句 删除答案
			sql =   "delete from  kbansvaliddate where kbansvaliddateid in(select  c.kbansvaliddateid  from service a,kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f,kbanswer g where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid and f.kbcontentid=g.kbcontentid   and  a.brand  in("+serviceRoot+") and  (f.SERVICETYPE ='"+industryOrganizationApplication+"' or f.SERVICETYPE=?))";
			//sql = "delete from  kbansvaliddate where kbansvaliddateid in(select  c.kbansvaliddateid  from service a,kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f,kbanswer g where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid and f.kbcontentid=g.kbcontentid   and  a.brand  in('个性化业务') and  (f.SERVICETYPE ='"+industryOrganizationApplication+"' or f.SERVICETYPE=?))";

			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			lstpara.add("sys");
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			
			// 定义insert 参数
		String brand ,serviceid,serviceid_brand,kbdataid,_abstract,begintime,endtime,channel,servicetype,answercategory,custormertype,answercontent,answer_colb,kb_custormertype;
		// 定义摘要字典
		Map<String, String> abstract_kbdataid;
		// 新增词模sql语句
		for (int i = 0; i < info.size(); i++) {

			Map<String, String> map = info.get(i);
			brand = map.get("SBRAND");
			serviceid = map.get("SERVICEID");
			serviceid_brand = map.get("SERVICEID_BRAND");
			_abstract = map.get("ABSTRACT");
			begintime = map.get("BEGINTIME");
			endtime = map.get("ENDTIME");
			channel = map.get("CHANNEL");
			servicetype = map.get("SERVICETYPE");
			answercategory = map.get("ANSWERCATEGORY");
			kb_custormertype = map.get("KB_CUSTOMERTYPE");
			custormertype = map.get("CUSTOMERTYPE");
			answercontent = map.get("ANSWERCONTENT");
			answer_colb = map.get("ANSWER_CLOB");
			//System.out.println(servicetype);
			
			
			if (brand_serviceid_abstract_kbdataid.containsKey(brand)) {// 若目标库中存在相同品牌brand
																		// 取brand
																		// 下内容
				Map<String, Map<String, String>> serviceid_abstract_kbdataid = brand_serviceid_abstract_kbdataid
						.get(brand);
				if (serviceid_brand != null) {// 若serviceid_brand不为空优先到同步目标库字典中通过serviceid_brand、_abstract
												// 寻找kbdataid
					if (serviceid_abstract_kbdataid
							.containsKey(serviceid_brand)) {
						abstract_kbdataid = serviceid_abstract_kbdataid
								.get(serviceid_brand);
						kbdataid = abstract_kbdataid.get(_abstract);
					} else {// 如果目标库中不存在 说明该业务已删除 不做同步
						logger.info("同步词模时关联业务serviceid_brand： "+serviceid_brand+" 下摘要不存在: "+_abstract);
						continue;
					}

				} else {// 若serviceid_brand 为空，通过serviceid 取内容
					if (serviceid_abstract_kbdataid.containsKey(serviceid)) {// 如果目标库字典中存在相同serviceid
																				// 取serviceid
																				// 下内容
						abstract_kbdataid = serviceid_abstract_kbdataid
								.get(serviceid);
						kbdataid = abstract_kbdataid.get(_abstract);
					} else {// 如果目标库中不存在 说明该业务已删除 不做同步
						logger.info("同步词模时关联业务ID不存在: "+serviceid);
						continue;
					}

				}
				if (kbdataid == null) {// 字典中不存在该摘要，说明目标库中摘要或以删除, 修改了???????
					logger.info("同步词模时关业务ID: "+serviceid+"或者serviceid_brand:"+serviceid_brand+" 下摘要不存在 : "+_abstract);
					continue;
				}
			} else {// 若目标库中不存在相同品牌brand 说明目标库中数据已删除不做同步
				logger.info("同步词模时关联品牌brand不存在: "+brand);
				continue;
			}

			String kbansvaliddateid = String.valueOf(SeqDAO
					.GetNextVal("KBANSVALIDDATE_SEQ"));
			// 判断开始时间、结束时间不为空、null

			// 插入kbansvaliddate的SQL语句
			if(begintime!=null&&endtime!=null){
				sql = "insert into kbansvaliddate(KBANSVALIDDATEID,KBDATAID ,BEGINTIME,ENDTIME ) values(?,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),to_date(?,'yyyy-mm-dd hh24:mi:ss'))";	
				// 定义绑定参数集合
				lstpara = new ArrayList<Object>();
				//绑定参数
				lstpara.add(kbansvaliddateid);
				lstpara.add(kbdataid);
				lstpara.add(begintime);
				lstpara.add(endtime);
				// 将SQL语句放入SQL语句集合中
				lstSql.add(sql);
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(lstpara);
			}else{
				sql = "insert into kbansvaliddate(KBANSVALIDDATEID,KBDATAID ) values(?,?)";	
				// 定义绑定参数集合
				lstpara = new ArrayList<Object>();
				//绑定参数
				lstpara.add(kbansvaliddateid);
				lstpara.add(kbdataid);
				// 将SQL语句放入SQL语句集合中
				lstSql.add(sql);
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(lstpara);
				
			}
			
			

			// 插入kbanspak表
			// 获取kbanspak的序列
			String kbanspakid = String.valueOf(SeqDAO
					.GetNextVal("KBANSPAK_SEQ"));
			// 插入kbanspak的SQL语句
			sql = "insert into kbanspak(KBANSPAKID,KBANSVALIDDATEID,PACKAGE,PACKAGECODE,PAKTYPE) values(?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			// 绑定参数
			lstpara.add(kbanspakid);
			lstpara.add(kbansvaliddateid);
			lstpara.add("空号码包");
			lstpara.add(null);
			lstpara.add("0");
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);

			// 插入kbansqryins表
			// 获取kbansqryins的序列
			String kbansqryinsid = String.valueOf(SeqDAO
					.GetNextVal("KBANSQRYINS_SEQ"));
			// 插入kbansqryins的SQL语句
			sql = "insert into kbansqryins(KBANSQRYINSID,KBANSPAKID,QRYINS) values(?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			// 绑定参数
			lstpara.add(kbansqryinsid);
			lstpara.add(kbanspakid);
			lstpara.add("查询指令无关");
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);

			// 获取kbcontent的序列
			String kbcontentid = String.valueOf(SeqDAO
					.GetNextVal("SEQ_KBCONTENT_ID"));
			// 插入kbcontent的SQL语句
			sql = "insert into kbcontent(KBCONTENTID,KBANSQRYINSID,CHANNEL,ANSWERCATEGORY,SERVICETYPE,CUSTOMERTYPE) values(?,?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			//绑定参数
			lstpara.add(kbcontentid);
			lstpara.add(kbansqryinsid);
			lstpara.add(channel);
			lstpara.add(answercategory);
			lstpara.add(servicetype);
			lstpara.add(kb_custormertype);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);

			// 插入kbanswer表
			// 过去kbanswer的序列
			String kbanswerid = String.valueOf(SeqDAO
					.GetNextVal("KBANSWER_SEQ"));
			// 插入kbanswer的SQL语句
			sql = "insert into kbanswer(kbanswerid,kbcontentid,answercontent,servicehallstatus,city,customertype,brand,answer_clob) values(?,?,?,?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			lstpara.add(kbanswerid);
			lstpara.add(kbcontentid);
			lstpara.add(answercontent);
			lstpara.add("无关");
			lstpara.add("上海");
			lstpara.add(custormertype);
			lstpara.add(brand);
			lstpara.add(answer_colb);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
		}			
		// 修改同步文件状态
			sql = "update datasyn set state ='同步完成' where name=? and bakid is null";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			lstpara.add(name);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			int rs = Database.executeNonQueryTransaction(lstSql, lstLstpara);
			if (rs > 0) {
				System.out.print(rs);
				return true;
			}
		}
		return false;
	
		
	}
	
	public static boolean insertSimilarquestionByAll(List<Map<String, String>> info,
			String name, String type, String serviceRoot,
			String industryOrganizationApplication){ 

		// 首先查询同步库中业务ID 和摘要、 摘要ID 对应关系
		Map<String, Map<String, Map<String, String>>> brand_serviceid_abstract_kbdataid = selectServceidAbstractKbdataid(serviceRoot);
		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		String sql ="" ;
		// 备份原始数据
		String path = dataSynPath + "datasyn" + File.separator + "databak"
			+ File.separator + type;
		
		if (mkdir(path)) {
			String bakName = name.split("\\.")[0] + "_bak.txt";
			path = path + File.separator + bakName;
			File filePath = new File(path);
			if (!filePath.exists()) {// 如果备份文件不存在则创建文件
				// 查询目标库中原始数据 做同步前备份
				String sql0 = "select s.serviceid ,s.serviceid_brand ,s.service,s.brand ,k.kbdataid,k.kbdataid_brand ,k.abstract ,w.questionid,w.question,w.remark, to_char(w.time,'yyyy-MM-dd HH24:mi:ss') time ,w.questiontype  from  service s,kbdata k ,similarquestion w where s.serviceid = k.serviceid  and k.kbdataid = w.kbdataid  and s.brand in("+serviceRoot+") and k.abstract is not null  and w.question is not null ";
				List<String> dataBak = select(sql0);
				if(dataBak==null){
					return  false;
				}
				String encoding = Database.getJDBCValues("datarecoverreadencoding");
				if (writeIntxt(path, dataBak, true,encoding)) {// 写文件成功
					// 定义备份文件 insert 语句\
					if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
						sql = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
								+ bakName
								+ "','similarquestion','备份文件',?,(select id from datasyn where name='"
								+ name
								+ "'),'"
								+ industryOrganizationApplication + "')";
						// 对应绑定参数集合
						lstpara = new ArrayList<Object>();
						lstpara.add("未恢复");
						// 将SQL语句放入SQL语句集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
						
					} else {// 如存在备份文件记录则更新完成时间
						// sql3 =
						// "update datasyn set time=sysdate where name ='"+bakName+"'";
					}
				} else {// 写不成功返回
					return false;
				}
				
				
			} else {
				if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
					sql = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
							+ bakName
							+ "','similarquestion','备份文件',?,(select id from datasyn where name='"
							+ name
							+ "'),'"
							+ industryOrganizationApplication
							+ "')";
					// 对应绑定参数集合
					lstpara = new ArrayList<Object>();
					lstpara.add("未恢复");
					// 将SQL语句放入SQL语句集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
					
					
				} else {// 如存在备份文件记录则更新完成时间
					// sql3 =
					// "update datasyn set time=sysdate where name ='"+bakName+"'";
				}
			}
			// 定义SQL语句 删除答案
			sql = "delete from  similarquestion where kbdataid in(select  b.kbdataid  from service a,kbdata b,similarquestion c  where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid  and  a.brand  in("+serviceRoot+"))";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			
			// 定义insert 参数
		String brand ,serviceid,serviceid_brand,kbdataid,_abstract,question,time,questiontype,remark;
		// 定义摘要字典
		Map<String, String> abstract_kbdataid;
		// 新增词模sql语句
		for (int i = 0; i < info.size(); i++) {
			Map<String, String> map = info.get(i);
			brand = map.get("BRAND");
			serviceid = map.get("SERVICEID");
			serviceid_brand = map.get("SERVICEID_BRAND");
			_abstract = map.get("ABSTRACT");
			question = map.get("QUESTION");
			questiontype = map.get("QUESTIONTYPE");
			time = map.get("TIME");
			remark = map.get("REMARK");
			if (brand_serviceid_abstract_kbdataid.containsKey(brand)) {// 若目标库中存在相同品牌brand
																		// 取brand
																		// 下内容
				Map<String, Map<String, String>> serviceid_abstract_kbdataid = brand_serviceid_abstract_kbdataid
						.get(brand);
				if (serviceid_brand != null) {// 若serviceid_brand不为空优先到同步目标库字典中通过serviceid_brand、_abstract
												// 寻找kbdataid
					if (serviceid_abstract_kbdataid
							.containsKey(serviceid_brand)) {
						abstract_kbdataid = serviceid_abstract_kbdataid
								.get(serviceid_brand);
						kbdataid = abstract_kbdataid.get(_abstract);
					} else {// 如果目标库中不存在 说明该业务已删除 不做同步
						continue;
					}

				} else {// 若serviceid_brand 为空，通过serviceid 取内容
					if (serviceid_abstract_kbdataid.containsKey(serviceid)) {// 如果目标库字典中存在相同serviceid
																				// 取serviceid
																				// 下内容
						abstract_kbdataid = serviceid_abstract_kbdataid
								.get(serviceid);
						kbdataid = abstract_kbdataid.get(_abstract);
					} else {// 如果目标库中不存在 说明该业务已删除 不做同步
						continue;
					}

				}
				if (kbdataid == null) {// 字典中不存在该摘要，说明目标库中摘要或以删除, 修改了???????
					continue;
				}
			} else {// 若目标库中不存在相同品牌brand 说明目标库中数据已删除不做同步
				continue;
			}

			sql = "insert into similarquestion(kbdataid,kbdata,questionid,question,remark,time,questiontype) values (?,?,?,?,?,sysdate,?)";
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			String similarquestionid = String.valueOf(SeqDAO
					.GetNextVal("similarquestion_sequence"));
			//绑定参数
			lstpara.add(kbdataid);
			lstpara.add(_abstract);
			lstpara.add(similarquestionid);
			lstpara.add(question);
			lstpara.add(remark);
			lstpara.add(questiontype);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
		}			
		// 修改同步文件状态
			sql = "update datasyn set state ='同步完成' where name=? and bakid is null";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			lstpara.add(name);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			int rs = Database.executeNonQueryTransaction(lstSql, lstLstpara);
			if (rs > 0) {
				System.out.print(rs);
				return true;
			}
		}
		return false;
	
		
	
	}
	
	
	public static boolean insertTodoSthByAll(List<Map<String, String>> info,
			String name, String type, String serviceRoot,
			String industryOrganizationApplication){ 
//
		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		String sql ="" ;
		String synWordclassDay =null;
		// 备份原始数据
		String path = dataSynPath + "datasyn" + File.separator + "databak"
			+ File.separator + type;
		
		if (mkdir(path)) {
			String bakName = name.split("\\.")[0] + "_bak.txt";
			path = path + File.separator + bakName;
			File filePath = new File(path);
			if (!filePath.exists()) {// 如果备份文件不存在则创建文件
				// 查询目标库中原始数据 做同步前备份
//				 synWordclassDay = Database.getJDBCValues("synWordclassBeforeDay");
					Result rs = null;
					 sql = "select s.name from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name ='数据同步参数配置' and t.name ='待办区同步前移时间'";
					try {
						rs = Database.executeQuery(sql);
						if(rs!=null&&rs.getRowCount()>0){
							synWordclassDay = rs.getRows()[0].get("name").toString();
						}
					} catch (SQLException e1) {
						return false; 
					}
				sql = "select serviceid, to_char(inserttime,'yyyy-MM-dd HH24:mi:ss') inserttime ,state, abstracts,kbdataid,status, id, servicetype  from  t_updrec  where   servicetype ='"+industryOrganizationApplication+"' and inserttime >sysdate -"+synWordclassDay;
			
				List<String> dataBak = select(sql);
				if(dataBak==null){
					return  false;
				}
				String encoding = Database.getJDBCValues("datarecoverreadencoding");
				if (writeIntxt(path, dataBak, true,encoding)) {// 写文件成功
					// 定义备份文件 insert 语句\
					if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
						sql = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
								+ bakName
								+ "','totosth','备份文件',?,(select id from datasyn where name='"
								+ name
								+ "'),'"
								+ industryOrganizationApplication + "')";
						// 对应绑定参数集合
						lstpara = new ArrayList<Object>();
						lstpara.add("未恢复");
						// 将SQL语句放入SQL语句集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
						
					} else {// 如存在备份文件记录则更新完成时间
						// sql3 =
						// "update datasyn set time=sysdate where name ='"+bakName+"'";
					}
				} else {// 写不成功返回
					return false;
				}
				
				
			} else {
				if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
					sql = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
							+ bakName
							+ "','totosth','备份文件',?,(select id from datasyn where name='"
							+ name
							+ "'),'"
							+ industryOrganizationApplication
							+ "')";
					// 对应绑定参数集合
					lstpara = new ArrayList<Object>();
					lstpara.add("未恢复");
					// 将SQL语句放入SQL语句集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
					
					
				} else {// 如存在备份文件记录则更新完成时间
					// sql3 =
					// "update datasyn set time=sysdate where name ='"+bakName+"'";
				}
			}
			// 定义SQL语句 删除答案
			sql = "delete from t_updrec where servicetype ='"+industryOrganizationApplication+"' and inserttime > sysdate -"+synWordclassDay;
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			
			// 定义insert 参数
		String serviceid,inserttime,state,abstracts,kbdataid,_status,servicetype;
		Map<String, String> abstract_kbdataid;
		// 新增词模sql语句
		for (int i = 0; i < info.size(); i++) {
			Map<String, String> map = info.get(i);
			serviceid = map.get("SERVICEID");
			inserttime = map.get("INSERTTIME");
			state = map.get("STATE");
			abstracts = map.get("ABSTRACTS");
			kbdataid = map.get("KBDATAID");
			_status = map.get("STATUS");
			servicetype = map.get("SERVICETYPE");
		
			sql = "insert into t_updrec(id,serviceid,inserttime,state,abstracts,kbdataid,status,servicetype) values (seq_t_updrec.nextval,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?,?,?,?,?)";
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			
			//绑定参数
			lstpara.add(serviceid);
			lstpara.add(inserttime);
			lstpara.add(state);
			lstpara.add(abstracts);
			lstpara.add(kbdataid);
			lstpara.add(_status);
			lstpara.add(servicetype);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
		}			
		// 修改同步文件状态
			sql = "update datasyn set state ='同步完成' where name=? and bakid is null";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			lstpara.add(name);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			int rs = Database.executeNonQueryTransaction(lstSql, lstLstpara);
			if (rs > 0) {
				System.out.print(rs);
				return true;
			}
		}
		return false;
	
		
	
	}
	
	public static boolean insertWordByWordpatUsed(
			List<Map<String, String>> info, String name, String type,
			String serviceRoot, String industryOrganizationApplication) {
		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		Set<String> set = new HashSet<String>();
		List<String> dataBak = null;
		String sql1 = "", sql2 = "", sql3 = "", sql4 = "", sql5 = "";
		// 同步的词类词条别名字典
		Map<String, Map<String, Map<String, Map<String, String>>>> wordClassAndWordAndsynonyms;
		// 开始备份文件
		// 备份原始数据
		String path = dataSynPath + "datasyn" + File.separator + "databak"
				+ File.separator + type;
		if (mkdir(path)) {
			// 获得需同步的词类词条别名字典
			wordClassAndWordAndsynonyms = selectWordClassAndWord(info);
//			System.out.println("转成字典后大小---->"
//					+ wordClassAndWordAndsynonyms.size());
			String bakName = name.split("\\.")[0] + "_bak.txt";
			path = path + File.separator + bakName;
			File filePath = new File(path);
			// 获取需要备份的词类名，即需同步文件中的词类名
			set = selectWordclass(info);
			
			if (!filePath.exists()) {// 如果备份文件不存在则创建文件
				// 查询目标库中词类对应的词条别名
				dataBak = selectWordcalssAndWordByWordpatUsered(set);
				if(dataBak==null){
					return false;
				}
				String encoding = Database.getJDBCValues("datarecoverreadencoding");
				if (writeIntxt(path, dataBak, true,encoding)) {// 写文件成功
					// 定义备份文件 insert 语句
					if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
						sql1 = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
								+ bakName
								+ "','word','备份文件',?,(select id from datasyn where name='"
								+ name
								+ "'),'"
								+ industryOrganizationApplication + "')";
						lstpara = new ArrayList<Object>();
						// 绑定参数
						lstpara.add("未恢复");
						// 将SQL语句放入集合中
						lstSql.add(sql1);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
					} else {// 如存在备份文件记录则更新完成时间
						// sql3 =
						// "update datasyn set time=sysdate where name ='"+bakName+"'";
					}
				} else {// 写不成功返回
					return false;
				}
			} else {
				if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
					sql1 = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
							+ bakName
							+ "','word','备份文件',?,(select id from datasyn where name='"
							+ name
							+ "'),'"
							+ industryOrganizationApplication
							+ "')";
					lstpara = new ArrayList<Object>();
					// 绑定参数
					lstpara.add("未恢复");
					// 将SQL语句放入集合中
					lstSql.add(sql1);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
					
				} else {// 如存在备份文件记录则更新完成时间
					// sql3 =
					// "update datasyn set time=sysdate where name ='"+bakName+"'";
				}
			}
			// 删除备份库中词模用到的词类
			sql1 = "delete from wordclass where wordclass= ?";
			for (String s : set) {
				lstpara = new ArrayList<Object>();
				// 绑定参数
				lstpara.add(s);
				// 将SQL语句放入集合中
				lstSql.add(sql1);
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(lstpara);
			}
			// 添加新的词类词条
			for (Map.Entry<String, Map<String, Map<String, Map<String, String>>>> entry : wordClassAndWordAndsynonyms
					.entrySet()) {
				String wordclass = entry.getKey();// 获取字典 中 词类名
				Map<String, Map<String, Map<String, String>>> map_wordclass_value = entry
						.getValue();// 获取词类下词条及别名字典
				Map<String, Map<String, String>> container1 = map_wordclass_value
						.get("CONTAINER");
				Map<String, String> container2 = container1.get("CONTAINER1");
				String container = container2.get("CONTAINER2");
				int wordclassid = SeqDAO.GetNextVal("seq_wordclass_id");
				// 插入词类
				sql1 = "insert into wordclass(wordclassid,wordclass,container) values(?,?,?)";
				lstpara = new ArrayList<Object>();
				// 绑定参数
				lstpara.add(wordclassid);
				lstpara.add(wordclass);
				lstpara.add(container);
				// 将SQL语句放入集合中
				lstSql.add(sql1);
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(lstpara);
				// 插入词条
				Map<String, Map<String, String>> wordDic = map_wordclass_value
						.get("WORD");
				if (!wordDic.isEmpty()) {// 如果词条不为空，遍历去词条
					for (Map.Entry<String, Map<String, String>> wordEntry : wordDic
							.entrySet()) {
						String word = wordEntry.getKey();// 获取词条名称
						int wordid = SeqDAO.GetNextVal("seq_word_id");
						sql1 = "insert into word(wordid,wordclassid,word,type) values(?,?,?,?)";
						// 定义绑定参数集合
						lstpara = new ArrayList<Object>();
						// 绑定id参数
						lstpara.add(wordid);
						// 绑定词类id参数
						lstpara.add(wordclassid);
						// 绑定词类名称参数
						lstpara.add(word);
						// 绑定类型参数
						lstpara.add("标准名称");
						// 将SQL语句放入集合中
						lstSql.add(sql1);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
						// 获取词条下别名字典
						Map<String, String> anotherNameDic = wordEntry
								.getValue();
						if (!anotherNameDic.isEmpty()) {// 如果词条别名不为空
							for (Map.Entry<String, String> anotherNameEntry : anotherNameDic
									.entrySet()) {
								String anotherName = anotherNameEntry.getKey();// 别名名称
								if("WORDID".equals(anotherName)){
									continue;
								}
								String anotherNameType = anotherNameEntry
										.getValue();// 别名类型
								// 插入别名
								int anotherNameid = SeqDAO
										.GetNextVal("seq_word_id");
								sql1 = "insert into word(wordid,wordclassid,word,stdwordid,type) values(?,?,?,?,?)";
								// 定义绑定参数集合
								lstpara = new ArrayList<Object>();
								// 绑定id参数
								lstpara.add(anotherNameid);
								// 绑定词类id参数
								lstpara.add(wordclassid);
								// 绑定别名参数
								lstpara.add(anotherName);
								// 绑定词条id参数
								lstpara.add(wordid);
								// 绑定类型参数
								lstpara.add(anotherNameType);
								// 将SQL语句放入集合中
								lstSql.add(sql1);
								// 将对应的绑定参数集合放入集合中
								lstLstpara.add(lstpara);

							}
						}

					}
				}
			}
			//修改同步文件同步状态
			sql1 = "update datasyn set state =? where name='" + name
			+ "' and bakid is null";
			lstpara = new ArrayList<Object>();
			// 绑定参数
			lstpara.add("同步完成");
			// 将SQL语句放入集合中
			lstSql.add(sql1);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			
			int rs = Database.executeNonQueryTransaction(lstSql, lstLstpara);
			if (rs > 0) {
				return true;
			}

		}
		return false;

	}
	
	/**
	 * 增量同步摘要
	 * */

	public static String insertKbdataByInc(
			List<Map<String, String>> info, String name, String type,
			String serviceRoot, String industryOrganizationApplication){
		//查询目标库摘要信息
		String sql = "select aa.serviceid serviceid ,aa.serviceid_brand,aa.service,aa.brand ,aa.parentname ,bb.KBDATAID ,bb.TOPIC,bb.ABSTRACT,bb.CITY ,bb.CUSTOMERTYPE,bb.CUSTOMER_KBDATAID,bb.FAQID_JS,bb.ISTEMPLATE,bb.KBDATACITYID,bb.KBDATAID_BRAND from (select * from service  where brand in ("+serviceRoot+")) aa  left join (select k.* from  service s,kbdata k  where s.serviceid = k.serviceid  and s.brand in("+serviceRoot+") and k.abstract is not null ) bb  on aa.serviceid = bb.serviceid ";
		//String sql  ="select * from(select aa.serviceid serviceid ,aa.serviceid_brand,aa.service,aa.brand ,aa.parentname ,bb.KBDATAID ,bb.TOPIC,bb.ABSTRACT,bb.CITY ,bb.CUSTOMERTYPE,bb.CUSTOMER_KBDATAID,bb.FAQID_JS,bb.ISTEMPLATE,bb.KBDATACITYID,bb.KBDATAID_BRAND from (select * from service  where brand in ('华夏基金')) aa  left join (select k.* from  service s,kbdata k  where s.serviceid = k.serviceid  and s.brand in('华夏基金') and k.abstract is not null ) bb  on aa.serviceid = bb.serviceid ) tt  where tt.kbdataid =10656450";
		//String sql = "select aa.serviceid serviceid ,aa.serviceid_brand,aa.service,aa.brand ,aa.parentname ,bb.KBDATAID ,bb.TOPIC,bb.ABSTRACT,bb.CITY ,bb.CUSTOMERTYPE,bb.CUSTOMER_KBDATAID,bb.FAQID_JS,bb.ISTEMPLATE,bb.KBDATACITYID,bb.KBDATAID_BRAND from (select * from service  where brand in ("+serviceRoot+") and serviceid_brand is not null) aa  left join (select k.* from  service s,kbdata k  where s.serviceid = k.serviceid  and s.brand in("+serviceRoot+") and k.abstract is not null ) bb  on aa.serviceid = bb.serviceid ";
		//String sql = "select  aa.serviceid service_id ,aa.serviceid_brand,aa.service,aa.brand ,aa.parentname,aa.parentid ,bb.*  from service aa ,kbdata  bb where aa.serviceid = bb.serviceid  and aa.brand in ("+serviceRoot+")";
		List<String> list = select(sql);
		if(list==null){
			return "查询数据库异常!";
		}
		List<Map<String, String>> servieDic = new  ArrayList<Map<String, String>>();
		for(int i=0;i<list.size();i++){//将数据库查询结果序列化之后放入集合中
		Map<String,String>	map = JSON.parseObject(list.get(i),
					new TypeReference<Map<String, String>>() {
					});
		servieDic.add(map);
		}
		Map<String,Map<String,Map<String,String>>> kbdata_target =  getKbdataDic(servieDic);//目标库业务字典
		Map<String,Map<String,Map<String,String>>> kbdata_source =  getKbdataDic(info);//需同步的业务字典
		List<Map<String,Map<String,String>>>  operationList = getComparedKbdataDic(kbdata_target,kbdata_source);

		
		
		
		if(operationList.size()==0){//没有需要增量更新的数据直接返回true
			return "没有需要更新的内容!";
		}
		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		// 备份原始数据
		String path = dataSynPath + "datasyn" + File.separator + "databak"
			+ File.separator + type;
		
		if (mkdir(path)) {
			String bakName = name.split("\\.")[0] + "_bak.txt";
			path = path + File.separator + bakName;
			File filePath = new File(path);
			if (!filePath.exists()) {// 如果备份文件不存在则创建文件
				List<String> dataBak =  new  ArrayList<String>();
				for(int i=0;i<operationList.size();i++){
					// 将行记录直接序列化之后放入list中
					String jsonString = JSON.toJSONString(operationList.get(i),
							SerializerFeature.UseSingleQuotes);
					dataBak.add(jsonString);
				}
			
				String encoding = Database.getJDBCValues("datarecoverreadencoding");
				if (writeIntxt(path, dataBak, true,encoding)) {// 写文件成功
					// 定义备份文件 insert 语句\
					if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
						sql = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
								+ bakName
								+ "','kbdata','备份文件',?,(select id from datasyn where name='"
								+ name
								+ "'),'"
								+ industryOrganizationApplication + "')";
						lstpara = new ArrayList<Object>();
						// 绑定参数
						lstpara.add("未恢复");
						// 将SQL语句放入集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
					} else {// 如存在备份文件记录则更新完成时间
						// sql3 =
						// "update datasyn set time=sysdate where name ='"+bakName+"'";
					}
				} else {// 写不成功返回
					return "备份文件写入失败!";
				}
				
				
			} else {
				if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
					sql = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
							+ bakName
							+ "','kbdata','备份文件',?,(select id from datasyn where name='"
							+ name
							+ "'),'"
							+ industryOrganizationApplication
							+ "')";
					lstpara = new ArrayList<Object>();
					// 绑定参数
					lstpara.add("未恢复");
					// 将SQL语句放入集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
				} else {// 如存在备份文件记录则更新完成时间
					// sql3 =
					// "update datasyn set time=sysdate where name ='"+bakName+"'";
				}
			}
			//定义需操作的sql
			String serviceid_brand,serviceid,topic,_abstract,city,kbdataid_brand,kbdataid,customer_kbdataid;
			String pname = industryOrganizationApplication.split("->")[1];
			for(int i =0; i<operationList.size();i++){
				Map<String,Map<String, String>>  map = operationList.get(i);
				if(map.containsKey("INSERT")){//插入 
					Map<String,String> insert = map.get("INSERT");
					serviceid = insert.get("SRVICEID");
					serviceid_brand = insert.get("SRVICEID_BRAND");
					 kbdataid =SeqDAO.GetNextVal("SEQ_KBDATA_ID")+"";
					topic = insert.get("TOPIC");
					_abstract = insert.get("ABSTRACT");
					city = insert.get("CITY");
					kbdataid_brand = insert.get("KBDATAID_BRAND");
					customer_kbdataid = insert.get("CUSTOMER_KBDATAID");
					if(serviceid_brand==null){
						sql = "insert into kbdata(serviceid,kbdataid,topic,abstract,city,kbdataid_brand,customer_kbdataid) values (?,?,?,?,?,?,?)";	
						lstpara = new ArrayList<Object>();
						// 绑定参数
						lstpara.add(serviceid);
						lstpara.add(kbdataid);
						lstpara.add(topic);
						lstpara.add(_abstract);
						lstpara.add(city);
						lstpara.add(kbdataid_brand);
						lstpara.add(customer_kbdataid);
					}else{
						sql = "insert into kbdata(serviceid,kbdataid,topic,abstract,city,kbdataid_brand,customer_kbdataid) values ((select serviceid from service where serviceid_brand=?),?,?,?,?,?,?)";	
						lstpara = new ArrayList<Object>();
						// 绑定参数
						lstpara.add(serviceid_brand);
						lstpara.add(kbdataid);
						lstpara.add(topic);
						lstpara.add(_abstract);
						lstpara.add(city);
						lstpara.add(kbdataid_brand);
						lstpara.add(customer_kbdataid);
					}
					
					
					// 将SQL语句放入集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
				}else if(map.containsKey("UPDATE")){//修改操作
					Map<String,String> update = map.get("UPDATE");
					serviceid_brand = update.get("SERVICEID_BRAND");
					kbdataid_brand  = update.get("KBDATAID_BRAND");
					topic = update.get("TOPIC");
					_abstract = update.get("ABSTRACT");
					kbdataid = update.get("KBDATAID");
					if(serviceid_brand!=null&&kbdataid_brand!=null){//业务是新增的，字典中包含 serviceid_brand , kbdataid_brand
						sql = "update kbdata set topic=?,abstract =?  where kbdataid_brand =?";
						lstpara = new ArrayList<Object>();
						// 绑定参数
						lstpara.add(topic);
						lstpara.add(_abstract);
						lstpara.add(kbdataid_brand);
						// 将SQL语句放入集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
					}else if(serviceid_brand==null&&kbdataid_brand!=null){//业务不是新增的 ，摘要是新增的 ，字典中包含  kbdataid_brand
						sql = "update kbdata set topic=?,abstract =? where kbdataid_brand =?";
						lstpara = new ArrayList<Object>();
						// 绑定参数
						lstpara.add(topic);
						lstpara.add(_abstract);
						lstpara.add(kbdataid_brand);
						// 将SQL语句放入集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
					
					}else if(serviceid_brand==null&&kbdataid_brand==null){//业务不是新增的，摘要不是新增的  字典中不包含  kbdataid_brand和serviceid_brand
					    sql = "update kbdata set topic=?,abstract =?  where kbdataid =?";
						lstpara = new ArrayList<Object>();
						// 绑定参数
						lstpara.add(topic);
						lstpara.add(_abstract);
						lstpara.add(kbdataid);
						// 将SQL语句放入集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
					
					}
				}
			}
			
			//修改同步文件同步状态
			sql = "update datasyn set state =? where name='" + name
			+ "' and bakid is null";
			lstpara = new ArrayList<Object>();
			// 绑定参数
			lstpara.add("同步完成");
			// 将SQL语句放入集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			
			int rs = Database.executeNonQueryTransaction(lstSql, lstLstpara);
			if (rs > 0) {
				return "同步成功!";
			}
			
			}
		
		return "同步失败!";
		
	}
	
	/**
	 * 增量同步业务
	 * */
	public static String  insertServiceByInc(
			List<Map<String, String>> info, String name, String type,
			String serviceRoot, String industryOrganizationApplication){
		String sql = "select aa.serviceid service ,aa.serviceid_brand,aa.service,aa.brand ,aa.parentname,aa.parentid,aa.serviceid from service aa  where aa.brand in ("+serviceRoot+") ";
		List<String> list = select(sql);
		if(list==null){
			return "查询数据异常!";
		}
		List<Map<String, String>> servieDic = new  ArrayList<Map<String, String>>();
		for(int i=0;i<list.size();i++){//将数据库查询结果序列化之后放入集合中
		Map<String,String>	map = JSON.parseObject(list.get(i),
					new TypeReference<Map<String, String>>() {
					});
		servieDic.add(map);
		}
		
		Map<String, Map<String,Map<String,String>>> servieDic_target =  getServiceDic(servieDic);//目标库中转换成字典结构
		Map<String, Map<String,Map<String,String>>> servieDic_source =  getServiceDic(info);//同步文件转换成字典结构
		List<Map<String,Map<String, String>>>  operationList = getComparedServiceDic(servieDic_target,servieDic_source);
		if(operationList.size()==0){//没有需要增量更新的数据直接返回true
			return "没有内容需要更新!";
		}
		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		String sql1 = "", sql2 = "", sql3 = "", sql4 = "", sql5 = "";
		// 备份原始数据
		String path = dataSynPath + "datasyn" + File.separator + "databak"
			+ File.separator + type;
		
		if (mkdir(path)) {
			String bakName = name.split("\\.")[0] + "_bak.txt";
			path = path + File.separator + bakName;
			File filePath = new File(path);
			if (!filePath.exists()) {// 如果备份文件不存在则创建文件
				List<String> dataBak =  new  ArrayList<String>();
				for(int i=0;i<operationList.size();i++){
					// 将行记录直接序列化之后放入list中
					String jsonString = JSON.toJSONString(operationList.get(i),
							SerializerFeature.UseSingleQuotes);
					dataBak.add(jsonString);
				}
				String encoding = Database.getJDBCValues("datarecoverreadencoding");
				if (writeIntxt(path, dataBak, true,encoding)) {// 写文件成功
					// 定义备份文件 insert 语句\
					if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
						sql = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
								+ bakName
								+ "','service','备份文件',?,(select id from datasyn where name='"
								+ name
								+ "'),'"
								+ industryOrganizationApplication + "')";
						lstpara = new ArrayList<Object>();
						// 绑定参数
						lstpara.add("未恢复");
						// 将SQL语句放入集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
					} else {// 如存在备份文件记录则更新完成时间
						// sql3 =
						// "update datasyn set time=sysdate where name ='"+bakName+"'";
					}
				} else {// 写不成功返回
					return "备份文件写入失败!";
				}
				
				
			} else {
				if (!isExist(name, bakName)) {// 如果库中不存在备份文件记录则记录
					sql = "insert into datasyn(id,name,synobject,type,state,bakid,servicetype) values(seq_datasyn_id.nextval,'"
							+ bakName
							+ "','service','备份文件',?,(select id from datasyn where name='"
							+ name
							+ "'),'"
							+ industryOrganizationApplication
							+ "')";
					lstpara = new ArrayList<Object>();
					// 绑定参数
					lstpara.add("未恢复");
					// 将SQL语句放入集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
				} else {// 如存在备份文件记录则更新完成时间
					// sql3 =
					// "update datasyn set time=sysdate where name ='"+bakName+"'";
				}
			}
			//定义需操作的sql
			String service,parentname,serviceid_brand,serviceid,parentid,brand;
			String pname = industryOrganizationApplication.split("->")[1];
			for(int i =0; i<operationList.size();i++){
				Map<String,Map<String, String>>  map = operationList.get(i);
				if(map.containsKey("INSERT")){//插入 
				Map<String,String> insert =map.get("INSERT");
				sql = "insert into service(serviceid,service,parentname,brand,cityid,parentid,serviceid_brand) values (?,?,?,?,?,(select serviceid from service where service=?),?)";
				service= insert.get("SERVICE");
				brand  = insert.get("BRAND"); 
				//parentname= insert.get("PARENTNAME");
				serviceid_brand = insert.get("SERVICEID_BRAND");
				parentname= insert.get("PARENTNAME");
				String service_parentname = service +"_" + parentname;
				int id = SeqDAO.GetNextVal("SEQ_SERVICE_ID");
				lstpara = new ArrayList<Object>();
				// 绑定参数
				lstpara.add(id);
				//lstpara.add(service_parentname);
				lstpara.add(service);
				lstpara.add(brand);
				lstpara.add(brand);
				lstpara.add(284);
				lstpara.add(brand);
				lstpara.add(serviceid_brand);
				// 将SQL语句放入集合中
				lstSql.add(sql);
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(lstpara);
				}else if(map.containsKey("UPDATE")){
				 Map<String,String> update =map.get("UPDATE");
				service= update.get("SERVICE");
				serviceid = update.get("SERVICEID"); 
				parentid = update.get("PARENTID"); 
				parentname= update.get("PARENTNAME");
				serviceid_brand = update.get("SERVICEID_BRAND");
				String service_parentname = service +"_" + parentname;
				if(serviceid_brand==null){//说明是原始的一些业务需要修改
				sql = "update service set service =? , parentname=? , parentid =?  where serviceid =? ";	
				lstpara = new ArrayList<Object>();
				// 绑定参数
				lstpara.add(service);
				lstpara.add(parentname);
				lstpara.add(parentid);
				lstpara.add(serviceid);
				// 将SQL语句放入集合中
				lstSql.add(sql);
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(lstpara);
				}else{//说明是商家新增的一些业务需要修改
				sql = "update service set service =?  where serviceid_brand =? ";
				// 绑定参数
				lstpara = new ArrayList<Object>();
				lstpara.add(service);
				lstpara.add(serviceid_brand);
				// 将SQL语句放入集合中
				lstSql.add(sql);
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(lstpara);
				}
				
				}
			}
			
			//修改同步文件同步状态
			sql = "update datasyn set state =? where name='" + name
			+ "' and bakid is null";
			lstpara = new ArrayList<Object>();
			// 绑定参数
			lstpara.add("同步完成");
			// 将SQL语句放入集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			
			int rs = Database.executeNonQueryTransaction(lstSql, lstLstpara);
			if (rs > 0) {
				return "同步成功!";
			}
			
			}
		return "同步失败!";
		
	}
	
	
	/**
	 * 获得目标业务字典和需同步业务字典比较后结果集合
	 * 
	 * */
	public static List<Map<String,Map<String, String>>> getComparedServiceDic(Map<String, Map<String,Map<String,String>>> servieDic_target ,Map<String, Map<String,Map<String,String>>> servieDic_source ){
	
		//定义比较结果集合 
		List<Map<String,Map<String, String>>>  operationList =  new  ArrayList<Map<String,Map<String,String>>>();
		Map<String, Map<String,String>> operationMap;
		Map<String, String> map;
		String key;
		//遍历目标文件字典和同步文件字典做比较	 判断是否是商家新的业务
	
			for (Map.Entry<String, Map<String,Map<String,String>>> servieDic_Entry : servieDic_source.entrySet()){
				key = servieDic_Entry.getKey();
				if("HASSERVICEID_BRAND".equals(key)){//商家之后新增的业务，需判断是否是新增的或者变更过
					Map<String, Map<String, String>> servieDic_source_map = servieDic_source.get("HASSERVICEID_BRAND");
					for (Map.Entry<String, Map<String, String>> servieDic_source_Entry : servieDic_source_map.entrySet()){
					String serviceid_brand= servieDic_source_Entry.getKey();//获得 serviceid_brand 
					Map<String,String> service_parentname_source = servieDic_source_Entry.getValue();//获得同步文件中 业务父业务字典
					String parntname_source = service_parentname_source.get("PARENTNAME");//获得同步文件中父业务名
					String parntid_source = service_parentname_source.get("PARENTID");//获得同步文件中父业务ID
		        	String service_source = service_parentname_source.get("SERVICE");//获得同步文件中业务名
		        	String serviceid_brand_source = service_parentname_source.get("SERVICEID_BRAND");//获得同步文件中servieid_brand
		        	String brand_source = service_parentname_source.get("BRAND");//获得同步文件中brand
		        	String service_parentnameSource = service_source+"_"+parntname_source;
//		        	for (Map.Entry<String, Map<String,Map<String,String>>> servieTargetDic_Entry : servieDic_target.entrySet()){
//		        		key = servieTargetDic_Entry.getKey();}
		        		if(servieDic_target.containsKey("HASSERVICEID_BRAND")){
		        			//如果目标库中存在相同key hasserviceid_brand
				        	Map<String, Map<String, String>> servieDic_target_map = servieDic_target.get("HASSERVICEID_BRAND");//获得目标库中的 业务字典
				        	if(servieDic_target_map.containsKey(serviceid_brand_source)){//目标库中存在相同serviceid_brand  字典 取业务做比较
				        		Map<String,String> service_parentname_target = servieDic_target_map.get(serviceid_brand_source);
				        		String service_target = service_parentname_target.get("SERVICE");
				        		String parentname_target = service_parentname_target.get("PARENTNAME");
				        		String parntid_target = service_parentname_target.get("PARENTID");
				        		String brand_target  = service_parentname_source.get("BRAND");
				        		//String service_parentnameTarget = service_target+"_"+parentname_target;//合并业务父业务名 作为一个整体业务处理 （注：因为库中存在相同的业务名加父业务作为区分）
				        		//if(service_parentnameSource.equals(service_target)){//业务相同 不做更改
				        			if(service_source.equals(service_target)){//业务相同 不做更改
				        		 	continue;
				        		}else{
				        			 operationMap =  new HashMap<String, Map<String,String>>();
						        	 map =  new HashMap<String,String>();
						             map.put("SERVICE", service_source);
						             map.put("PARENTNAME", parntname_source);
						             map.put("SERVICEID_BRAND", serviceid_brand_source);
						             map.put("OLDSERVICE", service_target);
						             map.put("OLDPARENTNAME", parentname_target);
						             map.put("BRAND", brand_target);
						             operationMap.put("UPDATE", map) ;
						             operationList.add(operationMap);
				        		}
				        		
				        	}else{//目标库中不存在相同serviceid_brand 说明该业务是新增的 直接记录下来
				        		 operationMap =  new HashMap<String, Map<String,String>>();
					        	 map =  new HashMap<String,String>();
					             map.put("SERVICE", service_source);
					             map.put("PARENTNAME", parntname_source);
					             map.put("SERVICEID_BRAND", serviceid_brand_source);
					             map.put("BRAND", brand_source);
					             operationMap.put("INSERT", map) ;
					             operationList.add(operationMap);
				        	}
				        	
				        
		        		}else{//如果目标库中不存在 “hasserviceid_brand” 说明该业务是新增的 直接记录下来
		        			 operationMap =  new HashMap<String, Map<String,String>>();
				        	 map =  new HashMap<String,String>();
				             map.put("SERVICE", service_source);
				             map.put("PARENTNAME", parntname_source);
				             map.put("SERVICEID_BRAND", serviceid_brand_source);
				             map.put("BRAND", brand_source);
				             operationMap.put("INSERT", map) ;
				             operationList.add(operationMap);
		        		}
		        	
				}
					
				
					
					
				}else if("HASSERVICEID".equals(key)){//之前同步至商家的数据，需判断是否要修改
					//之前同步给商家的业务，需判断业务名有没有变更过
					//需判断是否是业务是否变更过
					Map<String, Map<String, String>> servieDic_source_map = servieDic_source.get("HASSERVICEID");
					for (Map.Entry<String, Map<String, String>> servieDic_source_Entry : servieDic_source_map.entrySet()){
					String serviceid_brand= servieDic_source_Entry.getKey();//获得 serviceid_brand 
					Map<String,String> service_parentname_source = servieDic_source_Entry.getValue();//获得同步文件中 业务父业务字典
					String parentname_source = service_parentname_source.get("PARENTNAME");//获得同步文件中父业务名
		        	String service_source = service_parentname_source.get("SERVICE");//获得同步文件中业务名
		        	String serviceid_source = service_parentname_source.get("SERVICEID");//获得同步文件中servieid
		        	String parentid_source = service_parentname_source.get("PARENTID");//获得同步文件中父业务ID
		        	
//		        	for (Map.Entry<String, Map<String,Map<String,String>>> servieTargetDic_Entry : servieDic_target.entrySet()){
//		        		key = servieTargetDic_Entry.getKey();}
		        		if(servieDic_target.containsKey("HASSERVICEID")){
		        			//如果目标库中存在相同key hasserviceid
				        	Map<String, Map<String, String>> servieDic_target_map = servieDic_target.get("HASSERVICEID");//获得目标库中的 业务字典
				        	if(servieDic_target_map.containsKey(serviceid_source)){//目标库中存在相同serviceid  字典 取业务做比较
				        		Map<String,String> service_parentname_target = servieDic_target_map.get(serviceid_source);
				        		String service_target = service_parentname_target.get("SERVICE");
				        		String parentname_target = service_parentname_target.get("PARENTNAME");
				        		String parentid_target = service_parentname_target.get("PARENTID");
				        		String brand_target = service_parentname_target.get("BRAND");
				        		//String service_parentnameTarget = service_target+"_"+parentname_target;//合并业务父业务名 作为一个整体业务处理 （注：因为库中存在相同的业务名加父业务作为区分）
				        		if(service_source.equals(service_target)&&parentid_target.equals(parentid_source)){//业务相同 且父业务id相同不做修改
				        		 	continue;
				        		}else{
				        			 operationMap =  new HashMap<String, Map<String,String>>();
						        	 map =  new HashMap<String,String>();
						             map.put("SERVICE", service_source);
						             map.put("PARENTNAME", parentname_source);
						             map.put("PARENTID", parentid_source);
						             map.put("SERVICEID", serviceid_source);
						             map.put("OLDSERVICE", service_target);
						             map.put("OLDPARENTNAME", parentname_target);
						             map.put("OLDPARENTID", parentid_target);
						             map.put("BRAND", brand_target);
						             operationMap.put("UPDATE", map) ;
						             operationList.add(operationMap);
				        		}
				        		
				        	}else{//目标库中不存在相同serviceid 说明该业务已经删除，不做处理
				        		 continue;
				        	}
				        	
				        
		        		}else{
		        			//如果目标库中不存在 “hasserviceid” 说明该业务已经删除，不做处理
				        	continue;
		        		}
		        	
				}
				}
			}
			
		return operationList;
	}
	
	/**
	 * 获得目标摘要字典和需同步摘要字典比较后结果集合
	 * 
	 * */
	public static List<Map<String,Map<String,String>>> getComparedKbdataDic(Map<String,Map<String,Map<String,String>>> kbdata_target,Map<String,Map<String,Map<String,String>>> kbdata_source){

		//定义比较结果集合 
		List<Map<String,Map<String, String>>>  operationList =  new  ArrayList<Map<String,Map<String,String>>>();
		Map<String,Map<String, String>> map;
	     Map<String, String> operationInfo;
		 for (Map.Entry<String,Map<String,Map<String,String>>> kbdata_source_Entry : kbdata_source.entrySet()){//遍历需同步摘要字典
			 String serviceidkey = kbdata_source_Entry.getKey();//获得serviceid 或者serviceid_brand
			 Map<String,Map<String,String>>  kbdataidDic_source  = kbdata_source_Entry.getValue();//获得摘要id 或者 kbdataid_brand
			 for (Map.Entry<String,Map<String,String>> kbdataid_source_Entry : kbdataidDic_source.entrySet()){//遍历每条摘要ID 对应摘要信息
				 String kbdataidKey = kbdataid_source_Entry.getKey();//摘要id 或者 kbdataid_brand
				 Map<String,String>  kbdataInfo_source = kbdataid_source_Entry.getValue();//需要同步的摘要信息
				 String serviceid_source = kbdataInfo_source.get("SERVICEID"); 
				 String serviceid_brand_source = kbdataInfo_source.get("SERVICEID_BRAND");
				 String abstract_source = kbdataInfo_source.get("ABSTRACT");
				 String topic_source = kbdataInfo_source.get("TOPIC");
				 String kbdataid_brand_source = kbdataInfo_source.get("KBDATAID_BRAND");
				 String kbdataid_source = kbdataInfo_source.get("KBDATAID");
				 String kbdatacityid_source = kbdataInfo_source.get("KBDATACITYID");
				 String istemplate_source = kbdataInfo_source.get("ISTEMPLATE");
				 String faqid_js_source = kbdataInfo_source.get("FAQID_JS");
				 String customertype_source = kbdataInfo_source.get("CUSTOMERTYPE");
				 String city_source = kbdataInfo_source.get("CITY");
				 
				 if(kbdata_target.containsKey(serviceidkey)){//如果目标库中存在相同的业务，继续向下比较
					 Map<String,Map<String,String>>  kbdataidDic_target = 	 kbdata_target.get(serviceidkey);//取目标库中的摘要id对应地点
					 if(kbdataidDic_target.containsKey(kbdataidKey)){//如果目标库中存在相同的kbdatid 或者 kbdataid_brand
						 Map<String,String>  kbdataInfo_target  = 	 kbdataidDic_target.get(kbdataidKey);//获得目标库摘要信息
						 String serviceid_target = kbdataInfo_target.get("SERVICEID");
						 String abstract_target = kbdataInfo_target.get("ABSTRACT");
						 String topic_target = kbdataInfo_target.get("TOPIC");
						 String serviceid_brand_target = kbdataInfo_target.get("SERVICEID_BRAND");
						 String kbdataid_brand_target = kbdataInfo_target.get("KBDATAID_BRAND");
						 String kbdataid_target = kbdataInfo_target.get("KBDATAID");
						 String kbdatacityid_target = kbdataInfo_target.get("KBDATACITYID");
						 String istemplate_target = kbdataInfo_target.get("ISTEMPLATE");
						 String faqid_js_target = kbdataInfo_target.get("FAQID_JS");
						 String customertype_target = kbdataInfo_target.get("CUSTOMERTYPE");
						 String city_target = kbdataInfo_target.get("CITY"); 
						 
						 if(abstract_source.equals(abstract_target)&&topic_source.equals(topic_target)){//判断摘要名和主题是否相等 若相等 默认摘要未变更过 其余字段不做比较
							 logger.info("相等摘要名--> "+abstract_target);
							 continue;
						 }else{//如不等记录update 操作
							 map  = new HashMap<String,Map<String, String>>();
							 operationInfo = new HashMap<String,String>();
							 if(serviceidkey.contains("_")){//如果有“_”  说明业务是商家后来新增的， 需记录 serviecid_brand 
								 if(kbdataidKey.contains("_")){
									 operationInfo.put("SRVICEID_BRAND", serviceidkey);
									 operationInfo.put("SERVICEID", serviceid_target);
									 operationInfo.put("KBDATAID", kbdataid_target);
									 operationInfo.put("KBDATAID_BRAND", kbdataid_brand_target);
									 operationInfo.put("ABSTRACT", abstract_source);
									 operationInfo.put("TOPIC", topic_source);
									 operationInfo.put("OLDABSTRACT", abstract_target);
									 operationInfo.put("OLDTOPIC", topic_target);
									 map.put("UPDATE", operationInfo);
									 operationList.add(map); 
								 }else{//如果 serviceidkey不为空即业务是商家后来新增的， kbdataid_brand为空 (这种情况 kbdataid_brand应该也是不为空的，若出现这种情况 ，数据同步出现问题) 不做处理
									 logger.info("不正常情况业务表中 service_brand不为空，摘要表中kbdataid_brand为空  -->  "+serviceidkey +" " + kbdataidKey);
									 continue;
								 }
								 
							 }else{//如果没 有“_” ，说明业务是之前云平台同步过去，需记录 serviecid 
								 if(kbdataidKey.contains("_")){//说明摘要是之后商家同步的
									 operationInfo.put("SERVICEID", serviceid_target);
									 operationInfo.put("KBDATAID_BRAND", kbdataid_brand_target);
									 operationInfo.put("ABSTRACT", abstract_source);
									 operationInfo.put("TOPIC", topic_source);
									 operationInfo.put("OLDABSTRACT", abstract_target);
									 operationInfo.put("OLDTOPIC", topic_target);
									 map.put("UPDATE", operationInfo);
									 operationList.add(map); 
								 }else{
									 operationInfo.put("SERVICEID", serviceid_target);
									 operationInfo.put("KBDATAID", kbdataid_target);
									 operationInfo.put("ABSTRACT", abstract_source);
									 operationInfo.put("TOPIC", topic_source);
									 operationInfo.put("OLDABSTRACT", abstract_target);
									 operationInfo.put("OLDTOPIC", topic_target);
									 map.put("UPDATE", operationInfo);
									 operationList.add(map); 
								 }
								 
							 }
							 
							 
						 }
						 
					 }else{//如果不存在相同的kbdataid 或者kbdataid_brand ，则新增
						 
						 if(kbdataidKey.contains("_")){
							 
							 map  = new HashMap<String,Map<String, String>>();
							 operationInfo = new HashMap<String,String>(); 
							 if(serviceid_brand_source!=null){
								 operationInfo.put("SRVICEID_BRAND", serviceid_brand_source);  
							 }else{
								 operationInfo.put("SRVICEID", serviceid_source);   
							 }
							
							 operationInfo.put("KBDATAID_BRAND", kbdataid_brand_source);
							 operationInfo.put("ABSTRACT", abstract_source);
							 operationInfo.put("TOPIC", topic_source);
							 operationInfo.put("CITY", city_source);
							 map.put("INSERT", operationInfo); 
							 operationList.add(map); 
						 }else{//新增的 kbdataid_brand 为空 (这种情况是不存在的 除非数据问题) 不做处理,或者 摘要路径被移动过
							 logger.info("新增的摘要 kbdataid_brand 为空，或者摘要路径被移动过-->  " + kbdataidKey +" "+ abstract_source);
							 continue;
						 }
						 
					 }
					 
				 }else{//如果目标库中不存在相同业务，则不做同步
					 logger.info("不存在业务ID--> "+serviceidkey);
					 continue;
				 }

			 }
			
			 
		 }
		 
		 return operationList;
	}
	
	public static Map<String, Map<String,Map<String,String>>> getServiceDic(List<Map<String, String>> list){
		Map<String, Map<String,Map<String,String>>>  returnMap =  new HashMap<String, Map<String,Map<String,String>>>();
		Map<String,Map<String,String>> serviceDic = new HashMap<String,Map<String,String>>();
		Map<String, String>  service_parentname;
		for(int i=0;i<list.size();i++){
			Map<String, String> map =list.get(i);
			String serviceid_brand = map.get("SERVICEID_BRAND");
			String serviceid = map.get("SERVICEID");
			String service = map.get("SERVICE");
			String parentname = map.get("PARENTNAME");
			String parentid = map.get("PARENTID");
			String brand = map.get("BRAND");
			if(serviceid_brand!=null){
				if(returnMap.containsKey("HASSERVICEID_BRAND")){
					serviceDic = returnMap.get("HASSERVICEID_BRAND");
					service_parentname  = new HashMap<String,String>();
					service_parentname.put("SERVICE",service);
					service_parentname.put("PARENTNAME", parentname);
					service_parentname.put("SERVICEID", serviceid);
					service_parentname.put("PARENTID", parentid);
					service_parentname.put("SERVICEID_BRAND", serviceid_brand);
					service_parentname.put("BRAND", brand);
					serviceDic.put(serviceid_brand, service_parentname);
				}else{
					serviceDic = new HashMap<String,Map<String,String>>();
					service_parentname  = new HashMap<String,String>();
					service_parentname.put("SERVICE",service);
					service_parentname.put("PARENTNAME", parentname);
					service_parentname.put("SERVICEID", serviceid);
					service_parentname.put("PARENTID", parentid);
					service_parentname.put("SERVICEID_BRAND", serviceid_brand);
					service_parentname.put("BRAND", brand);
					serviceDic.put(serviceid_brand, service_parentname);
				}
				returnMap.put("HASSERVICEID_BRAND", serviceDic);
			}else{
				if(returnMap.containsKey("HASSERVICEID")){
					serviceDic = returnMap.get("HASSERVICEID");
					service_parentname  = new HashMap<String,String>();
					service_parentname.put("SERVICE",service);
					service_parentname.put("PARENTNAME", parentname);
					service_parentname.put("SERVICEID", serviceid);
					service_parentname.put("PARENTID", parentid);
					service_parentname.put("BRAND", brand);
					serviceDic.put(serviceid, service_parentname);
				}else{
					serviceDic = new HashMap<String,Map<String,String>>();
					service_parentname  = new HashMap<String,String>();
					service_parentname.put("SERVICE",service);
					service_parentname.put("PARENTNAME", parentname);
					service_parentname.put("SERVICEID", serviceid);
					service_parentname.put("PARENTID", parentid);
					service_parentname.put("BRAND", brand);
					serviceDic.put(serviceid, service_parentname);
				}
				returnMap.put("HASSERVICEID", serviceDic);	
			}
			
		}
		return returnMap;
	}
	public static Map<String,Map<String,Map<String,String>>> getKbdataDic(List<Map<String, String>> list){
		    Map<String,Map<String,Map<String,String>>> kbdataDic = new HashMap<String,Map<String,Map<String,String>>>();
			Map<String,Map<String,String>> kbdataidDic =null;
			Map<String,String> info =null;
			for(int i=0;i< list.size();i++){
				Map<String, String> kbdata_kbdataid;
				Map<String, String> map =list.get(i);
				String serviceid_brand = map.get("SERVICEID_BRAND");
				String serviceid = map.get("SERVICEID");
				String kbdataid = map.get("KBDATAID");
				String topic = map.get("TOPIC");
				String _abstract = map.get("ABSTRACT");
				String city = map.get("CITY");
				String customertype = map.get("CUSTOMERTYPE");
				String customer_kbdataid = map.get("CUSTOMER_KBDATAID");
				String faqid_js = map.get("FAQID_JS");
				String istemplate = map.get("ISTEMPLATE");
				String kbdatacityid = map.get("KBDATACITYID");
				String kbdataid_brand = map.get("KBDATAID_BRAND");
				if(serviceid_brand!=null){//如果serviceid_brand 不为空
					if(kbdataDic.containsKey(serviceid_brand)){//字典中包含 serviceid_brand
						if(kbdataid_brand!=null){ 
							if(kbdataidDic.containsKey(kbdataid_brand)){
								continue;
//								kbdataidDic = kbdataDic.get(serviceid_brand);//取 kbdataidDic
//								info  = new HashMap<String,String>();
//								info.put("SERVICEID", serviceid);
//								info.put("SERVICEID_BRAND", serviceid_brand);
//								info.put("KBDATAID_BRAND", kbdataid_brand);
//								info.put("KBDATAID", kbdataid);
//								info.put("KBDATACITYID", kbdatacityid);
//								info.put("ISTEMPLATE", istemplate);
//								info.put("FAQID_JS", faqid_js);
//								info.put("CUSTOMER_KBDATAID", customer_kbdataid);
//								info.put("CUSTOMERTYPE", customertype);
//								info.put("CITY", city);
//								info.put("ABSTRACT", _abstract);
//								info.put("TOPIC", topic);
//								kbdataidDic.put(kbdataid_brand, info);
//								kbdataDic.put(serviceid_brand, kbdataidDic);	
							}else{
								kbdataidDic = kbdataDic.get(serviceid_brand);//取 kbdataidDic
								info  = new HashMap<String,String>();
								info.put("SERVICEID", serviceid);
								info.put("SERVICEID_BRAND", serviceid_brand);
								info.put("KBDATAID_BRAND", kbdataid_brand);
								info.put("KBDATAID", kbdataid);
								info.put("KBDATACITYID", kbdatacityid);
								info.put("ISTEMPLATE", istemplate);
								info.put("FAQID_JS", faqid_js);
								info.put("CUSTOMER_KBDATAID", customer_kbdataid);
								info.put("CUSTOMERTYPE", customertype);
								info.put("CITY", city);
								info.put("ABSTRACT", _abstract);
								info.put("TOPIC", topic);
								kbdataidDic.put(kbdataid_brand, info);
								kbdataDic.put(serviceid_brand, kbdataidDic);
							}
							
							
						}else{//如果serviceid_brand 不为空 
							if(kbdataid!=null){//但kbdataid_brand 为空，kbdtaid 不为 空 则视为不符要求数据
								continue;
							}else{//如果都为空 表示该业务下没有任何摘要
								kbdataidDic  = new HashMap<String, Map<String,String>>();
								kbdataDic.put(serviceid_brand, kbdataidDic);
							}
							
						}
						
					}else{//如果字典中不包含 serviecid_brand
						if(kbdataid_brand!=null){
							kbdataidDic  = new HashMap<String, Map<String,String>>();
							info  = new HashMap<String,String>();
							info.put("SERVICEID", serviceid);
							info.put("SERVICEID_BRAND", serviceid_brand);
							info.put("KBDATAID_BRAND", kbdataid_brand);
							info.put("KBDATAID", kbdataid);
							info.put("KBDATACITYID", kbdatacityid);
							info.put("ISTEMPLATE", istemplate);
							info.put("FAQID_JS", faqid_js);
							info.put("CUSTOMER_KBDATAID", customer_kbdataid);
							info.put("CUSTOMERTYPE", customertype);
							info.put("CITY", city);
							info.put("ABSTRACT", _abstract);
							info.put("TOPIC", topic);
							kbdataidDic.put(kbdataid_brand, info);
							kbdataDic.put(serviceid_brand, kbdataidDic);
						}else{
							if(kbdataid!=null){
								continue;	
							}else{
								kbdataidDic  = new HashMap<String, Map<String,String>>();
								kbdataDic.put(serviceid_brand, kbdataidDic);
							}
							
						}
					}
				}else{//如果serviceid_brand为空,取servieid
					if(kbdataDic.containsKey(serviceid)){
						kbdataidDic = kbdataDic.get(serviceid);
						if(kbdataid_brand!=null){//如果kbdataid_brand 不为空， 即有新增的摘要
							info  = new HashMap<String,String>();
							info.put("SERVICEID", serviceid);
							info.put("KBDATAID_BRAND", kbdataid_brand);
							info.put("KBDATAID", kbdataid);
							info.put("KBDATACITYID", kbdatacityid);
							info.put("ISTEMPLATE", istemplate);
							info.put("FAQID_JS", faqid_js);
							info.put("CUSTOMER_KBDATAID", customer_kbdataid);
							info.put("CUSTOMERTYPE", customertype);
							info.put("CITY", city);
							info.put("ABSTRACT", _abstract);
							info.put("TOPIC", topic);
							kbdataidDic.put(kbdataid_brand, info);
							kbdataDic.put(serviceid, kbdataidDic);
						}else{
							if(kbdataid!=null){//如果kbdataid_brand为空， kbdataid 不为空，即为之前的摘要
								info  = new HashMap<String,String>();
								info.put("SERVICEID", serviceid);
								info.put("KBDATAID", kbdataid);
								info.put("KBDATACITYID", kbdatacityid);
								info.put("ISTEMPLATE", istemplate);
								info.put("FAQID_JS", faqid_js);
								info.put("CUSTOMER_KBDATAID", customer_kbdataid);
								info.put("CUSTOMERTYPE", customertype);
								info.put("CITY", city);
								info.put("ABSTRACT", _abstract);
								info.put("TOPIC", topic);
								kbdataidDic.put(kbdataid, info);
								kbdataDic.put(serviceid, kbdataidDic);
							}else{//如两者都为空 说明该业务下没有摘要
								kbdataidDic  = new HashMap<String, Map<String,String>>();
								kbdataDic.put(serviceid, kbdataidDic);
							}
							
						}
						
					}else{//如果字典中不包含serviceid 
						if(kbdataid_brand!=null){
							kbdataidDic  = new HashMap<String, Map<String,String>>();
							info  = new HashMap<String,String>();
							info.put("SERVICEID", serviceid);
							info.put("KBDATAID_BRAND", kbdataid_brand);
							info.put("KBDATAID", kbdataid);
							info.put("KBDATACITYID", kbdatacityid);
							info.put("ISTEMPLATE", istemplate);
							info.put("FAQID_JS", faqid_js);
							info.put("CUSTOMER_KBDATAID", customer_kbdataid);
							info.put("CUSTOMERTYPE", customertype);
							info.put("CITY", city);
							info.put("ABSTRACT", _abstract);
							info.put("TOPIC", topic);
							kbdataidDic.put(kbdataid_brand, info);
							kbdataDic.put(serviceid, kbdataidDic);
						}else{
							if(kbdataid!=null){
								kbdataidDic  = new HashMap<String, Map<String,String>>();
								info  = new HashMap<String,String>();
								info.put("SERVICEID", serviceid);
								info.put("KBDATAID", kbdataid);
								info.put("KBDATACITYID", kbdatacityid);
								info.put("ISTEMPLATE", istemplate);
								info.put("FAQID_JS", faqid_js);
								info.put("CUSTOMER_KBDATAID", customer_kbdataid);
								info.put("CUSTOMERTYPE", customertype);
								info.put("CITY", city);
								info.put("ABSTRACT", _abstract);
								info.put("TOPIC", topic);
								kbdataidDic.put(kbdataid, info);
								kbdataDic.put(serviceid, kbdataidDic);	
							}else{
								kbdataidDic  = new HashMap<String, Map<String,String>>();
								kbdataDic.put(serviceid, kbdataidDic);
							}
							
							
						}
					}
				}
			}
		System.out.println("getKbdataDic()-->"+kbdataDic.size());
		return kbdataDic;
		
	}
	
	/**
	 * 恢复全量更新的词模
	 * */
	public static boolean recoverWordpatByAll(List<Map<String, String>> info,
			String name, String type, String serviceRoot) {

		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		String sql1 = "", sql2 = "", sql3 = "", sql4 = "", sql5 = "";

		// 定义SQL语句 删除词模
		sql1 = "delete from wordpat ww where ww.wordpatid in(select w.wordpatid from service s,kbdata k, wordpat w where s.serviceid=k.serviceid and k.kbdataid = w.kbdataid  and  s.serviceid in(SELECT serviceid  FROM  service 　　start  WITH service in("
				+ serviceRoot
				+ ")　connect BY nocycle prior serviceid = parentid))";
		// 定义insert 参数
		String serviceid, serviceid_brand, _abstract, kbdataid, wordpatid, wordpat, city, autosendswitch, wordpattype, brand, edittime, simplewordpat;
		// 定义摘要字典
		Map<String, String> abstract_kbdataid;
		// 新增词模sql语句
		sql2 = "insert into wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand,edittime) values(SEQ_WORDPATTERN_ID.nextval,?,?,?,?,?,?,sysdate)";
		// 遍历需insert记录
		for (int i = 0; i < info.size(); i++) {
			Map<String, String> wordpatContent = info.get(i);
			serviceid = wordpatContent.get("SERVICEID");
			_abstract = wordpatContent.get("ABSTRACT");
			wordpat = wordpatContent.get("WORDPAT");
			city = wordpatContent.get("CITY");
			autosendswitch = wordpatContent.get("AUTOSENDSWITCH");
			wordpattype = wordpatContent.get("WORDPATTYPE");
			brand = wordpatContent.get("BRAND");
			kbdataid = wordpatContent.get("KBDATAID");

			// 对应绑定参数集合
			// insert into
			// wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand,edittime)
			// values(SEQ_WORDPATTERN_ID.nextval,?,?,?,?,?,?,sysdate)
			lstpara = new ArrayList<Object>();
			// 绑定参数
			lstpara.add(wordpat);
			lstpara.add(city);
			lstpara.add(autosendswitch);
			lstpara.add(wordpattype);
			lstpara.add(kbdataid);
			lstpara.add(brand);

			// 将SQL语句放入集合中
			lstSql.add(sql2);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			// String ss =
			// "insert into wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand,edittime) values(SEQ_WORDPATTERN_ID.nextval,'"+wordpat+"','"+city+"',"+autosendswitch+","+wordpattype+","+kbdataid+",'"+brand+"',sysdate)";
			// System.out.println(ss);
		}
		// 批量同步wordpatprecision 表
		sql4 = "insert into wordpatprecision (wordpatid,city,brand,correctnum,callvolume,wpprecision,autoreplyflag,projectflag)  select w.wordpatid wordpatid, w.city city,w.brand brand,0  as correctnum,0 as callvolume,0 as wpprecision,0 as autoreplyflag,0 as projectflag from wordpat w  where w.wordpatid  not in( select wp.wordpatid  from  wordpatprecision wp)";
		// 修改恢复文件状态
		sql5 = "update datasyn set state ='恢复完成' where name='" + name
				+ "' and bakid is not null";
		int rs = Database.executeNonQueryTransactionBatch(lstSql, lstLstpara,
				sql1, sql2, sql3, sql4, sql5);
		if (rs > 0) {
			return true;
		}

		return false;
	}
	
	/**
	 * 恢复全量更新的答案
	 * */
	
	public static boolean recoverAnswerByAll(List<Map<String, String>> info,
			String name, String type, String serviceRoot,String industryOrganizationApplication){

		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		String sql ="" ;
		// 备份原始数据
		String path = dataSynPath + "datasyn" + File.separator + "databak"
			+ File.separator + type;
		
			// 定义SQL语句 删除答案
			sql = "delete from  kbansvaliddate where kbansvaliddateid in(select  c.kbansvaliddateid  from service a,kbdata b,kbansvaliddate c,kbanspak d,kbansqryins e,kbcontent f,kbanswer g where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid and c.kbansvaliddateid=d.kbansvaliddateid and d.kbanspakid=e.kbanspakid and e.kbansqryinsid=f.kbansqryinsid and f.kbcontentid=g.kbcontentid   and  a.brand  in("+serviceRoot+") and  (f.SERVICETYPE ='"+industryOrganizationApplication+"' or f.SERVICETYPE=?))";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			lstpara.add("sys");
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			
			// 定义insert 参数
		String brand ,serviceid,serviceid_brand,kbdataid,_abstract,begintime,endtime,channel,servicetype,answercategory,custormertype,answercontent,answer_colb,kb_custormertype;
		// 定义摘要字典
		Map<String, String> abstract_kbdataid;
		// 新增词模sql语句
		for (int i = 0; i < info.size(); i++) {

			Map<String, String> map = info.get(i);
			brand = map.get("SBRAND");
			serviceid = map.get("SERVICEID");
			serviceid_brand = map.get("SERVICEID_BRAND");
			_abstract = map.get("ABSTRACT");
			kbdataid = map.get("KBDATAID");
			begintime = map.get("BEGINTIME");
			endtime = map.get("ENDTIME");
			channel = map.get("CHANNEL");
			servicetype = map.get("SERVICETYPE");
			answercategory = map.get("ANSWERCATEGORY");
			kb_custormertype = map.get("KB_CUSTOMERTYPE");
			custormertype = map.get("CUSTOMERTYPE");
			answercontent = map.get("ANSWERCONTENT");
			answer_colb = map.get("ANSWER_CLOB");

			String kbansvaliddateid = String.valueOf(SeqDAO
					.GetNextVal("KBANSVALIDDATE_SEQ"));
			// 判断开始时间、结束时间不为空、null

			// 插入kbansvaliddate的SQL语句
			sql = "insert into kbansvaliddate(KBANSVALIDDATEID,KBDATAID,BEGINTIME,ENDTIME ) values(?,?,?,?)";
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			//绑定参数
			lstpara.add(kbansvaliddateid);
			lstpara.add(kbdataid);
			lstpara.add(begintime);
			lstpara.add(endtime);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);

			// 插入kbanspak表
			// 获取kbanspak的序列
			String kbanspakid = String.valueOf(SeqDAO
					.GetNextVal("KBANSPAK_SEQ"));
			// 插入kbanspak的SQL语句
			sql = "insert into kbanspak(KBANSPAKID,KBANSVALIDDATEID,PACKAGE,PACKAGECODE,PAKTYPE) values(?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			// 绑定参数
			lstpara.add(kbanspakid);
			lstpara.add(kbansvaliddateid);
			lstpara.add("空号码包");
			lstpara.add(null);
			lstpara.add("0");
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);

			// 插入kbansqryins表
			// 获取kbansqryins的序列
			String kbansqryinsid = String.valueOf(SeqDAO
					.GetNextVal("KBANSQRYINS_SEQ"));
			// 插入kbansqryins的SQL语句
			sql = "insert into kbansqryins(KBANSQRYINSID,KBANSPAKID,QRYINS) values(?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			// 绑定参数
			lstpara.add(kbansqryinsid);
			lstpara.add(kbanspakid);
			lstpara.add("查询指令无关");
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);

			// 获取kbcontent的序列
			String kbcontentid = String.valueOf(SeqDAO
					.GetNextVal("SEQ_KBCONTENT_ID"));
			// 插入kbcontent的SQL语句
			sql = "insert into kbcontent(KBCONTENTID,KBANSQRYINSID,CHANNEL,ANSWERCATEGORY,SERVICETYPE,CUSTOMERTYPE) values(?,?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			//绑定参数
			lstpara.add(kbcontentid);
			lstpara.add(kbansqryinsid);
			lstpara.add(channel);
			lstpara.add(answercategory);
			lstpara.add(servicetype);
			lstpara.add(kb_custormertype);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);

			// 插入kbanswer表
			// 过去kbanswer的序列
			String kbanswerid = String.valueOf(SeqDAO
					.GetNextVal("KBANSWER_SEQ"));
			// 插入kbanswer的SQL语句
			sql = "insert into kbanswer(kbanswerid,kbcontentid,answercontent,servicehallstatus,city,customertype,brand,answer_clob) values(?,?,?,?,?,?,?,?)";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			lstpara.add(kbanswerid);
			lstpara.add(kbcontentid);
			lstpara.add(answercontent);
			lstpara.add("无关");
			lstpara.add("上海");
			lstpara.add(custormertype);
			lstpara.add(brand);
			lstpara.add(answer_colb);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
		}			
		// 修改同步文件状态
			sql = "update datasyn set state ='恢复完成' where name=? and bakid is not null";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			lstpara.add(name);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			int rs = Database.executeNonQueryTransaction(lstSql, lstLstpara);
			if (rs > 0) {
				return true;
			}
		
		return false;
	
		
	
	}
	/***
	 * 恢复全量更新的相似问题
	 * */
	public static boolean recoverSimilarquestionByAll(List<Map<String, String>> info,
			String name, String type, String serviceRoot,String industryOrganizationApplication){
		 

		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		String sql ="" ;
		// 定义SQL语句 删除答案
		sql = "delete from  similarquestion where kbdataid in(select  b.kbdataid  from service a,kbdata b,similarquestion c  where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid  and  a.brand  in("+serviceRoot+"))";
		// 对应绑定参数集合
		lstpara = new ArrayList<Object>();
		// 将SQL语句放入SQL语句集合中
		lstSql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstLstpara.add(lstpara);
			
			// 定义insert 参数
		String brand ,serviceid,serviceid_brand,kbdataid,_abstract,question,time,questiontype,remark;
		// 定义摘要字典
		Map<String, String> abstract_kbdataid;
		// 新增词模sql语句
		for (int i = 0; i < info.size(); i++) {
			Map<String, String> map = info.get(i);
			brand = map.get("BRAND");
			serviceid = map.get("SERVICEID");
			serviceid_brand = map.get("SERVICEID_BRAND");
			_abstract = map.get("ABSTRACT");
			question = map.get("QUESTION");
			questiontype = map.get("QUESTIONTYPE");
			time = map.get("TIME");
			remark = map.get("REMARK");
			kbdataid = map.get("KBDATAID");
		

			sql = "insert into similarquestion(kbdataid,kbdata,questionid,question,remark,time,questiontype) values (?,?,?,?,?,sysdate,?)";
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			String similarquestionid = String.valueOf(SeqDAO
					.GetNextVal("similarquestion_sequence"));
			//绑定参数
			lstpara.add(kbdataid);
			lstpara.add(_abstract);
			lstpara.add(similarquestionid);
			lstpara.add(question);
			lstpara.add(remark);
			lstpara.add(questiontype);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
		}			
		// 修改同步文件状态
			sql = "update datasyn set state ='恢复完成' where name=? and bakid is  not null";
			// 对应绑定参数集合
			lstpara = new ArrayList<Object>();
			lstpara.add(name);
			// 将SQL语句放入SQL语句集合中
			lstSql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			int rs = Database.executeNonQueryTransaction(lstSql, lstLstpara);
			if (rs > 0) {
				System.out.print(rs);
				return true;
			}
		
		return false;
	
		
	
	
	}
	
	
	/**
	 * 通过迁移时间恢复待办区记录
	 * 
	 * **/
	public static boolean recoverToDoSthByTime(List<Map<String, String>> info,
			String name, String type, String serviceRoot,String industryOrganizationApplication){
		 
		//
				// 定义多条SQL语句集合
				List<String> lstSql = new ArrayList<String>();
				// 定义多条SQL语句对应的绑定参数集合
				List<List<?>> lstLstpara = new ArrayList<List<?>>();
				// 定义绑定参数集合
				List<Object> lstpara = new ArrayList<Object>();
				String sql ="" ;
				String synWordclassDay =null;
				// 备份原始数据
				String path = dataSynPath + "datasyn" + File.separator + "databak"
					+ File.separator + type;

				Result rs = null;
				 sql = "select s.name from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name ='数据同步参数配置' and t.name ='待办区同步前移时间'";
				try {
					rs = Database.executeQuery(sql);
					if(rs!=null&&rs.getRowCount()>0){
						synWordclassDay = rs.getRows()[0].get("name").toString();
					}
				} catch (SQLException e1) {
					return false; 
				}
				
					// 定义SQL语句 
					sql = "delete from t_updrec where servicetype ='"+industryOrganizationApplication+"' and inserttime > sysdate -"+synWordclassDay;
					// 对应绑定参数集合
					lstpara = new ArrayList<Object>();
					// 将SQL语句放入SQL语句集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
					
					// 定义insert 参数
				String serviceid,inserttime,state,abstracts,kbdataid,_status,servicetype;
				Map<String, String> abstract_kbdataid;
				// 新增词模sql语句
				for (int i = 0; i < info.size(); i++) {
					Map<String, String> map = info.get(i);
					serviceid = map.get("SERVICEID");
					inserttime = map.get("INSERTTIME");
					state = map.get("STATE");
					abstracts = map.get("ABSTRACTS");
					kbdataid = map.get("KBDATAID");
					_status = map.get("STATUS");
					servicetype = map.get("SERVICETYPE");
				
					sql = "insert into t_updrec(id,serviceid,inserttime,state,abstracts,kbdataid,status,servicetype) values (seq_t_updrec.nextval,?,to_date(?,'yyyy-mm-dd hh24:mi:ss'),?,?,?,?,?)";
					// 定义绑定参数集合
					lstpara = new ArrayList<Object>();
					
					//绑定参数
					lstpara.add(serviceid);
					lstpara.add(inserttime);
					lstpara.add(state);
					lstpara.add(abstracts);
					lstpara.add(kbdataid);
					lstpara.add(_status);
					lstpara.add(servicetype);
					// 将SQL语句放入SQL语句集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
				}			
				// 修改同步文件状态
					sql = "update datasyn set state ='恢复完成' where name=? and bakid is  not null ";
					// 对应绑定参数集合
					lstpara = new ArrayList<Object>();
					lstpara.add(name);
					// 将SQL语句放入SQL语句集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
					int r = Database.executeNonQueryTransaction(lstSql, lstLstpara);
					if (r > 0) {
						//System.out.print(r);
						return true;
					}
				
				return false;
			
				
			
			
		
	}
	
	
	/**
	 * 恢复词模中用到的词类
	 * */
	
	public static boolean recoverWordByWordpatUsed(List<Map<String, String>> info,
			String name, String type, String serviceRoot) {
		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		Set<String> set =  new HashSet<String>();
		// 获取需要备份的词类名，即需同步文件中的词类名
		set = selectWordclass(info);
		// 同步的词类词条别名字典
		Map<String, Map<String, Map<String, Map<String, String>>>> wordClassAndWordAndsynonyms;
		// 获得需备份的词类词条别名字典
		wordClassAndWordAndsynonyms = selectWordClassAndWord(info);
		String sql1;
		sql1 = "delete from wordclass where wordclass= ?";
		for (String s : set) {
			lstpara = new ArrayList<Object>();
			// 绑定参数
			lstpara.add(s);
			// 将SQL语句放入集合中
			lstSql.add(sql1);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
		}
		// 添加新的词类词条
		for (Map.Entry<String, Map<String, Map<String, Map<String, String>>>> entry : wordClassAndWordAndsynonyms
				.entrySet()) {
			String wordclass = entry.getKey();// 获取字典 中 词类名
			Map<String, Map<String, Map<String, String>>> map_wordclass_value = entry
					.getValue();// 获取词类下词条及别名字典
			Map<String, Map<String, String>> container1 = map_wordclass_value
					.get("CONTAINER");
			Map<String, String> container2 = container1.get("CONTAINER1");
			String container = container2.get("CONTAINER2");
			int wordclassid = SeqDAO.GetNextVal("seq_wordclass_id");
			// 插入词类
			sql1 = "insert into wordclass(wordclassid,wordclass,container) values(?,?,?)";
			lstpara = new ArrayList<Object>();
			// 绑定参数
			lstpara.add(wordclassid);
			lstpara.add(wordclass);
			lstpara.add(container);
			// 将SQL语句放入集合中
			lstSql.add(sql1);
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
			// 插入词条
			Map<String, Map<String, String>> wordDic = map_wordclass_value
					.get("WORD");
			if (!wordDic.isEmpty()) {// 如果词条不为空，遍历去词条
				for (Map.Entry<String, Map<String, String>> wordEntry : wordDic
						.entrySet()) {
					String word = wordEntry.getKey();// 获取词条名称
					if(word==null || "".equals(word)){
					 continue;
					}
					
					int wordid = SeqDAO.GetNextVal("seq_word_id");
					sql1 = "insert into word(wordid,wordclassid,word,type) values(?,?,?,?)";
					// 定义绑定参数集合
					lstpara = new ArrayList<Object>();
					// 绑定id参数
					lstpara.add(wordid);
					// 绑定词类id参数
					lstpara.add(wordclassid);
					// 绑定词类名称参数
					lstpara.add(word);
					// 绑定类型参数
					lstpara.add("标准名称");
					// 将SQL语句放入集合中
					lstSql.add(sql1);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
					// 获取词条下别名字典
					Map<String, String> anotherNameDic = wordEntry
							.getValue();
					if (!anotherNameDic.isEmpty()) {// 如果词条别名不为空
						for (Map.Entry<String, String> anotherNameEntry : anotherNameDic
								.entrySet()) {
							String anotherName = anotherNameEntry.getKey();// 别名名称
							if("WORDID".equals(anotherName)){
								continue;
							}
							if(word==null || "".equals(word)){
							continue;	
								}
								
							String anotherNameType = anotherNameEntry
									.getValue();// 别名类型
							// 插入别名
							int anotherNameid = SeqDAO
									.GetNextVal("seq_word_id");
							sql1 = "insert into word(wordid,wordclassid,word,stdwordid,type) values(?,?,?,?,?)";
							// 定义绑定参数集合
							lstpara = new ArrayList<Object>();
							// 绑定id参数
							lstpara.add(anotherNameid);
							// 绑定词类id参数
							lstpara.add(wordclassid);
							// 绑定别名参数
							lstpara.add(anotherName);
							// 绑定词条id参数
							lstpara.add(wordid);
							// 绑定类型参数
							lstpara.add(anotherNameType);
							// 将SQL语句放入集合中
							lstSql.add(sql1);
							// 将对应的绑定参数集合放入集合中
							lstLstpara.add(lstpara);

						}
					}

				}
			}
		}
		
		// 修改恢复文件状态
		sql1 = "update datasyn set state =? where name='" + name
				+ "' and bakid is not null";
		lstpara = new ArrayList<Object>();
		// 绑定参数
		lstpara.add("恢复完成");
		// 将SQL语句放入集合中
		lstSql.add(sql1);
		// 将对应的绑定参数集合放入集合中
		lstLstpara.add(lstpara);
		
		int rs = Database.executeNonQueryTransaction(lstSql, lstLstpara);
		if (rs > 0) {
			return true;
		}
		
		return false;

	}

	
	/**
	 *恢复增量更新的业务
	 * */
	
	public static boolean recoverServiceByInc(List<Map<String, Map<String,String>>> list,
			String name, String type, String serviceRoot){

		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		String sql;
		for(int i=0;i<list.size();i++){
			Map<String, Map<String, String>>  map = list.get(i);
			for (Map.Entry<String, Map<String, String>> entry : map
					.entrySet()){
				String key = entry.getKey();
				Map<String,String>  info = entry.getValue();
				String oldService = info.get("OLDSERVICE");
				String oldParentname = info.get("OLDPARENTNAME");
				String oldParentid = info.get("OLDPARENTID");
				String serviceid_brand = info.get("SERVICEID_BRAND");
				String serviceid = info.get("SERVICEID");
				if("UPDATE".equals(key)){//获得备份文件中修改标识，即将业务恢复至修改前
					
					if(serviceid_brand!=null){
					sql = "update service set service=?, parentname=?,parentid=? where serviceid_brand=?";
					lstpara = new ArrayList<Object>();
					// 绑定参数
					lstpara.add(oldService);
					lstpara.add(oldParentname);
					lstpara.add(oldParentid);
					lstpara.add(serviceid_brand);
					// 将SQL语句放入集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
					}else {
						sql = "update service set service=?, parentname=?,parentid=? where serviceid=?";
						lstpara = new ArrayList<Object>();
						// 绑定参数
						lstpara.add(oldService);
						lstpara.add(oldParentname);
						lstpara.add(oldParentid);
						lstpara.add(serviceid);
						// 将SQL语句放入集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
					}
					
					
				}else if("INSERT".equals(key)){//获得备份文件新增标识，即需删除新增的业务
					if(serviceid_brand!=null){//判断条件特别重要，因为目前库中serviceid_brand 大多数为null
						sql = "delete from service where serviceid_brand = ? ";
						lstpara = new ArrayList<Object>();
						// 绑定参数
						lstpara.add(serviceid_brand);
						// 将SQL语句放入集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);	
					}
				
				}
			}
			
		}
		
		
		
		// 修改恢复文件状态
		sql = "update datasyn set state =? where name='" + name
				+ "' and bakid is not null";
		lstpara = new ArrayList<Object>();
		// 绑定参数
		lstpara.add("恢复完成");
		// 将SQL语句放入集合中
		lstSql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstLstpara.add(lstpara);
		
		int rs = Database.executeNonQueryTransaction(lstSql, lstLstpara);
		if (rs > 0) {
			return true;
		}
		
		return false;

	
		
	}
	
	

	
	
	
	/**
	 *恢复增量更新的摘要
	 * */
	
	public static boolean recoverKbdataByInc(List<Map<String, Map<String,String>>> list,
			String name, String type, String serviceRoot){
		// 定义多条SQL语句集合
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		String sql;
		for(int i=0;i<list.size();i++){
			Map<String, Map<String, String>>  map = list.get(i);
			for (Map.Entry<String, Map<String, String>> entry : map
					.entrySet()){
				String key = entry.getKey();
				Map<String,String>  info = entry.getValue();
				String _abstract = info.get("ABSTRACT");
				String kbdataid_brand = info.get("KBDATAID_BRAND");
				String oldabstract = info.get("OLDABSTRACT");
				String serviceid = info.get("SERVICEID");
				String oldtopic = info.get("OLDTOPIC");
				String serviceid_brand = info.get("SRVICEID_BRAND");
				String topic = info.get("TOPIC");
				String kbdataid = info.get("KBDATAID");
				
				if("UPDATE".equals(key)){//获得备份文件中修改标识，即将修改恢复至修改前
				if(kbdataid_brand!=null){
					sql = " update kbdata set topic=?, abstract=? where kbdataid_brand =?";
					lstpara = new ArrayList<Object>();
					// 绑定参数
					lstpara.add(oldtopic);
					lstpara.add(oldabstract);
					lstpara.add(kbdataid_brand);
					// 将SQL语句放入集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
				}else{
					sql = " update kbdata set topic=?, abstract=? where kbdataid =?";
					lstpara = new ArrayList<Object>();
					// 绑定参数
					lstpara.add(oldtopic);
					lstpara.add(oldabstract);
					lstpara.add(kbdataid);
					// 将SQL语句放入集合中
					lstSql.add(sql);
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
				}
					
				}else if("INSERT".equals(key)){//获得备份文件新增标识，即需删除新增的业务
					if(kbdataid_brand!=null){
					 sql =" delete from  kbdata where kbdataid_brand=?  ";
					 lstpara = new ArrayList<Object>();
						lstpara.add(kbdataid_brand);
						// 将SQL语句放入集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
					}else{
					 sql =" delete from  kbdata where kbdataid=?  ";	
					 lstpara.add(kbdataid);
						// 将SQL语句放入集合中
						lstSql.add(sql);
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
					}
					
				
				}
			}
			
		}
		
		
		
		// 修改恢复文件状态
		sql = "update datasyn set state =? where name='" + name
				+ "' and bakid is not null";
		lstpara = new ArrayList<Object>();
		// 绑定参数
		lstpara.add("恢复完成");
		// 将SQL语句放入集合中
		lstSql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstLstpara.add(lstpara);
		
		int rs = Database.executeNonQueryTransaction(lstSql, lstLstpara);
		if (rs > 0) {
			return true;
		}
		
		return false;

	
		
	}
	public static Map<String, Map<String, Map<String, String>>> selectServceidAbstractKbdataid(
			String serviceRoot) {
		Map<String, Map<String, Map<String, String>>> brand_serviceid_abstract_kbdataid = new HashMap<String, Map<String, Map<String, String>>>();
		Map<String, Map<String, String>> serviceid_abstract_kbdataid;
		Map<String, String> abstract_kbdataid; 
		 String sql =
		 "select a.serviceid,a.serviceid_brand,a.brand,b.abstract,b.kbdataid from service a,kbdata b where a.serviceid=b.serviceid and a.brand in("+serviceRoot+") and b.abstract is not null  order by a.serviceid";
//		 sql =
//			 "select a.serviceid,a.serviceid_brand,a.brand,b.abstract,b.kbdataid from service a,kbdata b where a.serviceid=b.serviceid and a.brand in("+serviceRoot+") and b.abstract is not null and a.serviceid = 1825396 order by a.serviceid";
	     //测试sql
//		 String sql =
//			 "select a.serviceid,a.serviceid_brand,a.brand,b.abstract,b.kbdataid from service a,kbdata b where a.serviceid=b.serviceid and a.brand in('个性化业务') and b.abstract is not null  order by a.serviceid";
		Result rs = null;
		try {
			rs = Database.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (rs != null) {
			SortedMap<?, ?>[] maps = rs.getRows();
			String serviceid, serviceid_brand, brand, _abstract, kbdataid;
			for (int i = 0; i < maps.length; i++) {
				SortedMap map = maps[i];
				serviceid_brand = map.get("serviceid_brand") == null ? "nocontent"
						: map.get("serviceid_brand").toString();
				serviceid = map.get("serviceid").toString();
				brand = map.get("brand").toString();
				_abstract = map.get("abstract").toString();
				kbdataid = map.get("kbdataid").toString();

				if (!brand_serviceid_abstract_kbdataid.containsKey(brand)) {// 如果字典中不存在品牌
					serviceid_abstract_kbdataid = new HashMap<String, Map<String, String>>();
					if (!"nocontent".equals(serviceid_brand)) {
						if (!serviceid_abstract_kbdataid
								.containsKey(serviceid_brand)) {
							abstract_kbdataid = new HashMap<String, String>();
							abstract_kbdataid.put(_abstract, kbdataid);
							serviceid_abstract_kbdataid.put(serviceid_brand,
									abstract_kbdataid);
						} else {
							abstract_kbdataid = serviceid_abstract_kbdataid
									.get(serviceid_brand);
							abstract_kbdataid.put(_abstract, kbdataid);
							serviceid_abstract_kbdataid.put(serviceid_brand,
									abstract_kbdataid);
						}

					} else {
						if (!serviceid_abstract_kbdataid.containsKey(serviceid)) {
							abstract_kbdataid = new HashMap<String, String>();
							abstract_kbdataid.put(_abstract, kbdataid);
							serviceid_abstract_kbdataid.put(serviceid,
									abstract_kbdataid);
						} else {
							abstract_kbdataid = serviceid_abstract_kbdataid
									.get(serviceid);
							abstract_kbdataid.put(_abstract, kbdataid);
							serviceid_abstract_kbdataid.put(serviceid,
									abstract_kbdataid);
						}

					}
					brand_serviceid_abstract_kbdataid.put(brand,
							serviceid_abstract_kbdataid);

				} else {// 如果字典中存在品牌

					serviceid_abstract_kbdataid = brand_serviceid_abstract_kbdataid
							.get(brand);
					if (!"nocontent".equals(serviceid_brand)) {
						if (!serviceid_abstract_kbdataid
								.containsKey(serviceid_brand)) {
							abstract_kbdataid = new HashMap<String, String>();
							abstract_kbdataid.put(_abstract, kbdataid);
							serviceid_abstract_kbdataid.put(serviceid_brand,
									abstract_kbdataid);
						} else {
							abstract_kbdataid = serviceid_abstract_kbdataid
									.get(serviceid_brand);
							abstract_kbdataid.put(_abstract, kbdataid);
							serviceid_abstract_kbdataid.put(serviceid_brand,
									abstract_kbdataid);
						}

					} else {
						if (!serviceid_abstract_kbdataid.containsKey(serviceid)) {
							abstract_kbdataid = new HashMap<String, String>();
							abstract_kbdataid.put(_abstract, kbdataid);
							serviceid_abstract_kbdataid.put(serviceid,
									abstract_kbdataid);
						} else {
							abstract_kbdataid = serviceid_abstract_kbdataid
									.get(serviceid);
							abstract_kbdataid.put(_abstract, kbdataid);
							serviceid_abstract_kbdataid.put(serviceid,
									abstract_kbdataid);
						}

					}
					brand_serviceid_abstract_kbdataid.put(brand,
							serviceid_abstract_kbdataid);

				}

			}
		}

		return brand_serviceid_abstract_kbdataid;
	}

	/*
     * 
     */
	public static List<String> select(String sql) {
		List<String> list = new ArrayList<String>();
		Result rs = null;
		try {
			rs = Database.executeQuery(sql);
		} catch (SQLException e) {
			return null;
		}
		if (rs != null) {
			SortedMap<?, ?>[] maps = rs.getRows();
			for (int i = 0; i < maps.length; i++) {
				SortedMap map = maps[i];
				// 将行记录直接序列化之后放入list中
				String jsonString = JSON.toJSONString(map,
						SerializerFeature.UseSingleQuotes);
				list.add(jsonString);
			}

		}
		return list;

	}

	public static Boolean mkdir(String path) {
		File file = new File(path);
		// 判断文件夹是否存在,如果不存在则创建文件夹
		if (!file.exists()) {
			if (file.mkdirs()) {
				return true;
			}else{
				logger.error("创建文件夹失失败【"+path+"】");
				return false;
			}
		} else {
			return true;
		}

	}

	public static JSONObject insert(String name, String type) {
		JSONObject jsonObj = new JSONObject();
		String industryOrganizationApplication = MyClass
				.IndustryOrganizationApplication();
		// 判断industryOrganizationApplication为空串、空、null
		if (" ".equals(industryOrganizationApplication)
				|| "".equals(industryOrganizationApplication)
				|| industryOrganizationApplication == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("msg", "登录信息已失效,请注销后重新登录操作!");
			return jsonObj;
		}
		String sql = "insert into datasyn(id,name,synobject,type,state,servicetype) values(seq_datasyn_id.nextval,'"
				+ name
				+ "','"
				+ type
				+ "','同步文件','未同步','"
				+ industryOrganizationApplication + "')";
		int rs = -1;
		try {
			rs = Database.executeNonQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (rs > 0) {
			jsonObj.put("success", true);
			jsonObj.put("msg", "保存成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "保存失败!");
		}
		return jsonObj;
	}

	public static JSONObject delete(String id, String name, String bakName,
			String type, String serviceType) {
		JSONObject jsonObj = new JSONObject();
		String sql = "delete from datasyn where id =" + id + " and synobject='"
				+ type + "' and servicetype ='" + serviceType + "'";
		int rs = -1;
		try {
			rs = Database.executeNonQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (rs > 0) {
			String path;
			if (!"".equals(bakName) && bakName != null) {// 删除备份文件
				path = dataSynPath + "datasyn" + File.separator + "databak"
						+ File.separator + type + File.separator + bakName;
				deleteFile(path);
			}
			if (!"".equals(name) && name != null) {// /删除备份文件
				path = dataSynPath + "datasyn" + File.separator + "upload"
						+ File.separator + type + File.separator + name;
				deleteFile(path);
			}
			jsonObj.put("success", true);
			jsonObj.put("msg", "删除成功!");
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}

	public static boolean isExist(String name, String bakName) {
		String sql = "select ds.name name ,ds.time uploadtime ,ds.state,ds.synobject synobject,ds.type, dsb.name bakname,dsb.state bakstate from datasyn ds ,datasyn dsb where ds.id=dsb.bakid and ds.name ='"
				+ name + "' and dsb.name='" + bakName + "'";
		try {
			Result rs = Database.executeQuery(sql);
			if (rs != null && rs.getRowCount() > 0) {
				return true;
			}
		} catch (SQLException e) {
			return false;
		}
		return false;
	}

	public static boolean deleteFile(String path) {
		boolean r = false;
		File file = new File(path);
		if (file.exists()) {
			r = file.delete();
		}
		return r;
	}

	/**
	 * 分别查询需同步的词库 并将词类词条别名分装成结构体Map<String, Map<String, Map<String, Map<String,
	 * String>>>> ，如下：
	 * 
	 *'工资近类':{ 'word':{'~人工':{}, '工资':{ 'wordid':'977374', '人工':'其他别名',
	 * '公子':'其他别名', '公资':'其他别名', '奖金':'其他别名', '工钱':'其他别名', '待遇':'其他别名',
	 * '白干':'其他别名', '福利':'其他别名', '绩效工资':'其他别名', '薪':'其他别名', '薪水':'其他别名',
	 * '薪资':'其他别名' } }, 'wordclassid':{'wordclassid1':{'wordclassid2':'12413'}}
	 * }
	 * */
	public static Map<String, Map<String, Map<String, Map<String, String>>>> selectWordClassAndWord(
			List<Map<String, String>> info) {
		Map<String, Map<String, Map<String, Map<String, String>>>> wordClassAndWordAndsynonyms = new HashMap<String, Map<String, Map<String, Map<String, String>>>>();

		for (int i = 0; i < info.size(); i++) {
			Map<String, Map<String, Map<String, String>>> wordAndsynonym = new HashMap<String, Map<String, Map<String, String>>>();
			Map<String, Map<String, String>> wordAndsynonymAndType = new HashMap<String, Map<String, String>>();
			Map<String, String> synonymAndType = new HashMap<String, String>();
			Map<String, String> map = info.get(i);
			String wordclass;
			String word;
			String synonym;
			String type = null;
			String wordclassid;
			String wordid;
			String container;
			wordclass = map.get("WORDCLASS").toString();
			wordclassid = map.get("WORDCLASSID").toString();
			container = map.get("CONTAINER") == null ? "" : map
					.get("CONTAINER").toString();
			if (wordClassAndWordAndsynonyms.containsKey(wordclass)) {
				word = map.get("WORD") == null ? "" : map.get("WORD")
						.toString();
				wordid = map.get("WORDID") == null ? "" : map.get("WORDID")
						.toString();
				synonym = map.get("SYNONYMSTR") == null ? "" : map.get(
						"SYNONYMSTR").toString();
				type = map.get("TYPE") == null ? "" : map.get("TYPE")
						.toString();
				wordAndsynonym = wordClassAndWordAndsynonyms.get(wordclass);
				if (wordAndsynonym.containsKey("WORD")) {
					wordAndsynonymAndType = wordAndsynonym.get("WORD");
					if (wordAndsynonymAndType.containsKey(word)) {
						if (!"".equals(synonym)) {
							wordAndsynonymAndType.get(word).put(synonym, type);
							wordAndsynonymAndType.get(word).put("WORDID",
									wordid);
							wordAndsynonym.put("WORD", wordAndsynonymAndType);
						}

					} else {
						if (!"".equals(synonym)) {
							synonymAndType = new HashMap<String, String>();
							synonymAndType.put(synonym, type);
							synonymAndType.put("WORDID", wordid);
							wordAndsynonymAndType.put(word, synonymAndType);
						} else {
							synonymAndType = new HashMap<String, String>();
							wordAndsynonymAndType.put(word, synonymAndType);
						}
						wordAndsynonym.put("WORD", wordAndsynonymAndType);
					}
				}

			} else {
				word = map.get("WORD") == null ? "" : map.get("WORD")
						.toString();
				wordid = map.get("WORDID") == null ? "" : map.get("WORDID")
						.toString();
				synonym = map.get("SYNONYMSTR") == null ? "" : map.get(
						"SYNONYMSTR").toString();
				if (!"".equals(word)) {
					if (!"".equals(synonym)) {

						type = map.get("TYPE") == null ? "" : map.get("TYPE")
								.toString();
						synonymAndType.put(synonym, type);
						synonymAndType.put("WORDID", wordid);
						wordAndsynonymAndType.put(word, synonymAndType);
					} else {
						wordAndsynonymAndType.put(word, synonymAndType);
					}
				}
				wordAndsynonym.put("WORD", wordAndsynonymAndType);
				// wordAndsynonymAndType = new HashMap<String, Map<String,
				// String>>();
				// synonymAndType = new HashMap<String, String>();
				// synonymAndType.put("WORDCLASSID2", wordclassid);
				// wordAndsynonymAndType.put("WORDCLASSID1", synonymAndType);
				// wordAndsynonym.put("WORDCLASSID", wordAndsynonymAndType);
				wordAndsynonymAndType = new HashMap<String, Map<String, String>>();
				synonymAndType = new HashMap<String, String>();
				synonymAndType.put("CONTAINER2", container);
				wordAndsynonymAndType.put("CONTAINER1", synonymAndType);
				wordAndsynonym.put("CONTAINER", wordAndsynonymAndType);

			}

			wordClassAndWordAndsynonyms.put(wordclass, wordAndsynonym);

		}

		return wordClassAndWordAndsynonyms;
	}

	public static Set<String> selectWordclass(List<Map<String, String>> info) {
		Set<String> set = new HashSet<String>();
		String wordclass;
		for (int i = 0; i < info.size(); i++) {
			Map<String, String> map = info.get(i);
			wordclass = map.get("WORDCLASS").toString();
			set.add(wordclass);
		}
		return set;
	}
	
	public static String getServiceRoot(String serviceRoot){
		String rs = "";
		if(!"yes".equals(synGeXingHuaService)){
			serviceRoot = serviceRoot.replace("'", "");
			String arry[] = serviceRoot.split(",");
			for(String service:arry){
				if("个性化业务".equals(service)||"".equals(service)){
					continue;
				}else{
					rs += "'" +service +"',";
				}
			}
			rs = rs.substring(0, rs.lastIndexOf(","));
			return rs;
		}else{
			return serviceRoot;
		}
		
		
	}

	public static boolean writeIntxtByline(String path, String line,
			boolean append,String encoding) {
		   try {   
				File myFilePath = new File(path);
				if (!myFilePath.exists()) {
					myFilePath.createNewFile();
				}  
		        OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(myFilePath,append),encoding);
		       // OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(myFilePath,append),"UTF-8"); 
		        BufferedWriter writer=new BufferedWriter(write);   
				writer.write(line + "\n");  
		        writer.close();   
		    } catch (Exception e) {   
		    	logger.error("写文件失败【"+path+"】",e);
				return false;  
		    }  
		return true;
	}
	
}
