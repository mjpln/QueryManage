package com.knowology.km.bll;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.imageio.ImageIO;
import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import com.alibaba.fastjson.JSONObject;
import com.knowology.GlobalValue;
import com.knowology.UtilityOperate.GetConfigValue;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.bll.ConstructSerialNum;
import com.knowology.dal.Database;
import com.knowology.km.NLPCallerWS.NLPCaller4WSDelegate;
import com.knowology.km.bll.QuerymanageDAO;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.ReadExcel;
import com.knowology.km.util.getServiceClient;

public class TestDAO {
	
	public static String BASE_PATH = "Z:\\query";
//	public static String BASE_PATH = "/app/ICSR4TEST/cwy";
	public static JSONObject  main1(){
		JSONObject json = new JSONObject();
		try {
			List<String> lines = FileUtils.readLines(new File("C:\\Users\\cwy-pc\\Desktop\\wordclass2patternidset\\wordclass2patternidset.txt"), "gbk");
//			List<String> lines = FileUtils.readLines(new File("/app/ICSR4TEST/wordclass2patternidset/wordclass2patternidset.txt"), "gbk");
			
			List<String> filter = FileUtils.readLines(new File("C:\\Users\\cwy-pc\\Desktop\\wordclass2patternidset\\filter.txt"), "utf-8");
//			List<String> filter = FileUtils.readLines(new File("/app/ICSR4TEST/wordclass2patternidset/filter.txt"), "utf-8");
	//		List<String> exist = new ArrayList<String>();
			int count = 0;
			Map<String,List<String>> wordpat = new HashMap<String,List<String>>();
			Map<String,List<String>> wordpatToWord = new HashMap<String,List<String>>();
			for(String str:lines){
				int indexOf = str.indexOf("#_#");
				String word = str.substring(0, indexOf);
				if(filter.contains(word) || wordpat.containsKey(word))
					continue;
				
				List<String> wordpatIdList = new ArrayList<String>((str.length() - indexOf) % 9 + 3);
				String wordpatId = "";
				for(int i = indexOf+3,l = str.length() ;i<l;i++){
					char c = str.charAt(i);
					switch (str.charAt(i)) {
						case ',':
						case ']':
							if(wordpatId != ""){
								wordpatIdList.add(wordpatId);
								List<String> list = wordpatToWord.get(wordpatId);
								if(list == null){
									list = new ArrayList<String>();
									wordpatToWord.put(wordpatId, list);
								}
								list.add(word);
								wordpatId = "";
							}
							break;
						case ' ':
						case '[':
							break;
						default:
							wordpatId += c;
							break;
					}
				}
				
				if(wordpatIdList.size() > 0){
					count += wordpatIdList.size();
					System.out.println("词类【"+word+"】所使用的词模数量为："+wordpatIdList.size());
					wordpat.put(word, wordpatIdList);
				}
				
	//			if(wordpatIdList.size() < 1000){
	//				System.out.println("词类【"+word+"】所使用的词模为："+wordpatIdList);
	//			}
			}
			Set<String> set = wordpatToWord.keySet();
			System.out.println("去重后词模数量为："+set.size());
			Iterator<String> iterator = set.iterator();
			while(iterator.hasNext()){
				String next = iterator.next();
				List<String> wordList = wordpatToWord.get(next);
				if(wordList.size() > 1){
					System.out.println("词模【"+next+"】使用的词类为："+wordList);
				}
			}
			
			System.out.println("词类总数："+wordpat.size()+"，词模总数量为："+count);
//			List<String> asList = Arrays.asList(set.toArray(new String[]{}));
			List<String> asList = new ArrayList<String>();
			int num = 1000;
			int size = asList.size();
			int times = size / num + 1;//执行sql次数
			Map<String,JSONObject> serviceMap = new HashMap<String,JSONObject>();//业务集映射
			// 问题库目录，摘要，词模，返回值，地市，删除的词类
			List<String> colTitle = Arrays.asList("问题库目录","摘要","词模","返回值","词模类型","客户问","地市","删除的词类");
			List<List<String>> text = new ArrayList<List<String>>();
			
			//分批查询数据
			for(int i =0;i<times;i++){
				int start = i * num;
				int end = (i +1)* num > size ? size : (i+1)* num;
				Result rsData = selectWordpat(asList, start, end);
				Set<String> serviceSet = new HashSet<String>();
				for(int n =0 ;n<rsData.getRowCount();n++){
					serviceSet.add(Objects.toString(rsData.getRows()[n].get("serviceid"), ""));
				}
				Result rs = selectService(Arrays.asList(serviceSet.toArray(new String[]{})));
				for(int n =0 ;n<rs.getRowCount();n++){
					
					String serviceid = Objects.toString(rs.getRows()[n].get("serviceid"));
					String service = Objects.toString(rs.getRows()[n].get("service"));
					String parentid = Objects.toString(rs.getRows()[n].get("parentid"));
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("service", service);
					jsonObject.put("parentid", parentid);
					jsonObject.put("serviceid", serviceid);
					serviceMap.put(serviceid, jsonObject);
				}
				List<List<String>> excel = structureExcel(rsData, serviceMap,wordpatToWord);
				text.addAll(excel);
			}
			
			String filename = "wordpat_";
			filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			boolean isWritten = ReadExcel.writeExcel(QuerymanageDAO.FILE_PATH_EXPORT, filename, null, null, colTitle, text);
			if(isWritten){
	//			file = new File(FILE_PATH_EXPORT + filename + ".xls");
				json.put("success", true);
				json.put("fileName", filename+ ".xls");
			}else{
				json.put("success", false);
				json.put("msg", "生成文件失败");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}
	public static Result selectWordpat(List<String> wordpatIdList,int start,int end){
		StringBuilder sql = new StringBuilder();
		sql.append("select w.* , k.serviceid,k.abstract from wordpat w,kbdata k where (");
		if(end - start > 1){
			sql.append("w.wordpatid="+wordpatIdList.get(start));
		}
		for(int i = start+1;i<end;i++){
			sql.append(" or w.wordpatid="+wordpatIdList.get(i));
		}
		sql.append(") and k.kbdataid = w.kbdataid");
		String sqlStr = sql.toString();
		
		GlobalValue.myLog.info(sqlStr);
		Result executeQuery = Database.executeQuery(sqlStr);
		return executeQuery;
	}
	
	public static Result selectService(List<String> serviceIdList){
		StringBuilder sql = new StringBuilder();
		sql.append("select distinct * from (");
		sql.append("select serviceid,service,parentid ");
		sql.append(" from service");
		sql.append(" start with (");
		if(serviceIdList.size() > 0){
			sql.append("serviceid = "+serviceIdList.get(0));
		}
		for(int i = 1,l=serviceIdList.size();i<l;i++){
			sql.append(" or serviceid="+serviceIdList.get(i));
		}
		sql.append(") connect by   serviceid =  prior parentid and serviceid <> 0) ");
		String sqlStr = sql.toString();
		
		GlobalValue.myLog.info(sqlStr);
		Result executeQuery = Database.executeQuery(sqlStr);
		return executeQuery;
	}
	
	public static List<List<String>> structureExcel(Result rs,Map<String,JSONObject> serviceMap, Map<String, List<String>> wordpatToWord){
		List<List<String>> text = new ArrayList<List<String>>();
		for (int i = 0; i < rs.getRowCount(); i++) {
			List<String> line = new ArrayList<String>();
			String serviceId = rs.getRows()[i].get("serviceid").toString();
			JSONObject jsonObject = serviceMap.get(serviceId);
			String path = jsonObject.getString("service");
			while(jsonObject != null){
				String parentid = jsonObject.getString("parentid");
				path = jsonObject.getString("service")+"->"+path;
				jsonObject = serviceMap.get(parentid);
			}
			// 问题库目录，摘要，词模，返回值，地市，删除的词类
			line.add(path);//问题库路径
			String normalquery = ObjectUtils.toString(rs.getRows()[i].get("abstract"),"");
			line.add(StringUtils.substringAfter(normalquery, ">"));//标准问题
			String wordpattype = QuerymanageDAO.wordpatType.get(Objects.toString(rs.getRows()[i].get("wordpattype"), ""));
			wordpattype = wordpattype == null ? "" : wordpattype;
			String wordpat = ObjectUtils.toString(rs.getRows()[i].get("wordpat"),"");
			String returnBody = StringUtils.substringAfter(wordpat, "#");
			
			if(wordpattype != "自学习词模"){
				line.add(ObjectUtils.toString(wordpat, "" ));//词模
				line.add(returnBody);//返回值
				line.add(wordpattype);//词模类型
				line.add("");//客户问
				
			}else{
				line.add("");//词模
				line.add("");//返回值
				line.add("");//词模类型
				for(String str:StringUtils.split(returnBody, "&")){
					if(str.startsWith("来源=")){
						String query = str.substring(4, str.length()-1);
						line.add(query);//客户问
						break;
					}
				}
			}
			
			
			line.add(QuerymanageDAO.getCityName(ObjectUtils.toString(rs.getRows()[i].get("city"),"")));//来源地市
			
			line.add(wordpatToWord.get(ObjectUtils.toString(rs.getRows()[i].get("wordpatid"),"")).toString());//删除的词类
			text.add(line);
		}
		return text;
	}
	
	public static void main5(){
		int count = 0;
		List<JSONObject> queryData = new ArrayList<JSONObject>();
		File[] files = FileUtils.getFile(BASE_PATH).listFiles();
		for(File file : files){
			if(file.getName().startsWith("error-query")){
				List<String> lines = new ArrayList<String>();
				try {
					lines = FileUtils.readLines(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 循环遍历数据源
				for (int i = 0 ,l = lines.size(); i < l; i++) {
					String line = lines.get(i);
					String trim = line.trim();
					if(StringUtils.isEmpty(line)) continue;
					count ++;
					JSONObject parse = JSONObject.parseObject(trim);
					if(StringUtils.isEmpty(Objects.toString(parse.get("query")))){
						System.out.println("数据："+parse+"------------ 为空");
	            		continue;
	            	}
					
					System.out.println("读取数据："+parse);
					queryData.add(parse);
					
				}
				
			}
		}
		GlobalValue.myLog.info("读取文件中的数据总量为："+count);
		insertQuery(queryData);
	}
	
	public static void main6(){
		JSONObject json = new JSONObject();
//		File file = FileUtils.getFile(BASE_PATH,"动卡标准问客户问190430.xlsx");
		File file = FileUtils.getFile(BASE_PATH,"190430.xlsx");
//		File file = FileUtils.getFile(BASE_PATH,"测试文件.xlsx");
		String extension = FilenameUtils.getExtension(file.getName());
		// 定义当前文件后得到的集合
		List<List<Object>> info = new ArrayList<List<Object>>();
		if ("xls".equalsIgnoreCase(extension)) {
			// 读取2003版的Excel
			info = MyUtil.read2003Excel(file, 2);
		} else if ("xlsx".equalsIgnoreCase(extension)) {
			// 读取2007版的Excel
			info = MyUtil.read2007Excel(file, 2);
		}
		String serviceid = "1835748.24";
		String serviceType = "银行行业->中信银行->多渠道应用";
		String workerid = "181";
		 // 获得商家标识符
		String bussinessFlag = CommonLibMetafieldmappingDAO
				.getBussinessFlag(serviceType);
		
		GlobalValue.myLog.info("初始数据大小："+info.size());
		String cloum1 = info.get(0).get(0)+"";
		if("标准问题".equals(cloum1)){//忽略Excel列名
			info.remove(0);
		}
		List<Object> list = info.get(109994);
		System.out.println(list);
	}
	
	/**
	 * 第一步：读取客户问文件，导入客户问。
	 * @return
	 */
	public static JSONObject main2(){
		JSONObject json = new JSONObject();
//		File file = FileUtils.getFile(BASE_PATH,"动卡标准问客户问190430.xlsx");
		File file = FileUtils.getFile(BASE_PATH,"190430.xlsx");
//		File file = FileUtils.getFile(BASE_PATH,"测试文件.xlsx");
		String extension = FilenameUtils.getExtension(file.getName());
		// 定义当前文件后得到的集合
		List<List<Object>> info = new ArrayList<List<Object>>();
		if ("xls".equalsIgnoreCase(extension)) {
			// 读取2003版的Excel
			info = MyUtil.read2003Excel(file, 2);
		} else if ("xlsx".equalsIgnoreCase(extension)) {
			// 读取2007版的Excel
			info = MyUtil.read2007Excel(file, 2);
		}
		String serviceid = "1835748.24";
		String serviceType = "银行行业->中信银行->多渠道应用";
		String workerid = "181";
		 // 获得商家标识符
		String bussinessFlag = CommonLibMetafieldmappingDAO
				.getBussinessFlag(serviceType);
		
		GlobalValue.myLog.info("初始数据大小："+info.size());
		String cloum1 = info.get(0).get(0)+"";
		if("标准问题".equals(cloum1)){//忽略Excel列名
			info.remove(0);
		}
		
		if (info.size() > 0) {
			String serviceCity = "";
			Result rs = CommonLibQueryManageDAO.getServiceCitys(serviceid);
			if (rs != null && rs.getRowCount() > 0) {
				serviceCity = rs.getRows()[0].get("city").toString();
			}
			GlobalValue.myLog.info("业务地市："+serviceCity);
			
			List<JSONObject> queryData = new ArrayList<JSONObject>();
			List<JSONObject> kbdataData = new ArrayList<JSONObject>();
			
			Map<String, Map<String, String>> queryDic = getQueryDic2();
			int num = 0;
			int size = info.size();
			int repeat = 0;
			for(int index = 109994;index<size;index++){
				List<Object> line = info.get(index);
				
				String normalquery = line.get(0)!=null?  line.get(0).toString():"";
				String customerquery = line.get(1)!=null?  line.get(1).toString():"";
				String kbdataid = null;
				String abscity = null;
				if(StringUtils.isEmpty(customerquery)){
					GlobalValue.myLog.info("标准问："+normalquery+"，客户问  --- 为空");
					continue;
				}
				if(queryDic.containsKey(normalquery)){
					Map<String, String> tempMap = queryDic.get(normalquery);
					kbdataid  = tempMap.get("kbdataid");
					abscity = tempMap.get("abscity");
					if(tempMap.containsKey(customerquery)){
						GlobalValue.myLog.info("标准问："+normalquery+"，客户问："+customerquery +" --- 重复");
						repeat++;
						continue; //跳过插入
					}
				}else{
					GlobalValue.myLog.info("标准问："+normalquery+" --- 缺失");
					//新增摘要
					if (GetConfigValue.isOracle) {
						kbdataid = ConstructSerialNum.GetOracleNextValNew(
								"SEQ_KBDATA_ID", bussinessFlag);
					} else if (GetConfigValue.isMySQL) {
						kbdataid = ConstructSerialNum.getSerialIDNew("kbdata",
								"kbdataid", bussinessFlag);
					}
					abscity = serviceCity;
					String service = CommonLibServiceDAO.getNameByserviceid(serviceid);
					String abs = "<" + service + ">" + normalquery;
					
					JSONObject kbdata = new JSONObject();
					kbdata.put("serviceid", serviceid);
					kbdata.put("kbdataid", kbdataid);
					kbdata.put("topic", "常见问题");
					kbdata.put("abstract", abs);
					kbdata.put("city", abscity);
					kbdata.put("responsetype", "未知");
					kbdata.put("interacttype", "未交互");
					kbdataData.add(kbdata);
					
					Map<String, String> queryMap = new HashMap<String, String>();
					queryMap.put("kbdataid", kbdataid);
					queryMap.put("abscity", abscity);
					queryDic.put(normalquery, queryMap);
				}
				
				JSONObject query = new JSONObject();
				String querymanageId = "";
				if (GetConfigValue.isOracle) {
					querymanageId = ConstructSerialNum
							.GetOracleNextValNew("seq_querymanage_id",
									bussinessFlag);
				} else if (GetConfigValue.isMySQL) {
					querymanageId = ConstructSerialNum.getSerialIDNew(
							"querymanage", "id", bussinessFlag);
				}
				query.put("id", querymanageId);
				query.put("kbdataid", kbdataid);
				query.put("query", customerquery);
				query.put("city", abscity);
				query.put("workerid", workerid);
				queryData.add(query);
				GlobalValue.myLog.info("构建客户问插入数据："+query.toString());
				
				num ++;
				if(num > 1000){
					GlobalValue.myLog.info("开始插入数据，处理数据量为："+num);
					if(kbdataData.size() > 0){
						GlobalValue.myLog.info("处理标准问数据量为："+kbdataData.size());
						insertKbdata(kbdataData);
					}
					
					if(queryData.size() > 0){
						GlobalValue.myLog.info("处理客户问数据量为："+queryData.size());
						insertQuery(queryData);
					}
					
					queryData = new ArrayList<JSONObject>();
					kbdataData = new ArrayList<JSONObject>();
					num = 0;
					GlobalValue.myLog.info("重置，继续执行");
				}
			}
			if(num > 0){
				GlobalValue.myLog.info("开始插入数据，处理数据量为："+num);
				if(queryData.size() > 0){
					GlobalValue.myLog.info("处理客户问数据量为："+queryData.size());
					insertQuery(queryData);
				}
				if(kbdataData.size() > 0){
					GlobalValue.myLog.info("处理标准问数据量为："+kbdataData.size());
					insertKbdata(kbdataData);
				}
				queryData = new ArrayList<JSONObject>();
				kbdataData = new ArrayList<JSONObject>();
				num = 0;
				
				GlobalValue.myLog.info("执行结束");
				GlobalValue.myLog.info("总数据量为:" + size);
				GlobalValue.myLog.info("重复数据量为:" + repeat);
				GlobalValue.myLog.info("共插入数据量为:" + (size-repeat));
			}
		}
		return json;
	}
	
	public static void insertQuery(List<JSONObject> datas){

		long startTime=System.currentTimeMillis();
       
        Connection conn=null;
        PreparedStatement stmt = null;
        try{
            conn=Database.getCon();
            conn.setAutoCommit(false);
           
            stmt=conn.prepareStatement("insert into querymanage(ID,KBDATAID,QUERY,CITY,WORKERID) values(?,?,?,?,?)");
 
            int num=0;
            for(JSONObject v:datas){
            	stmt.setObject(1, v.get("id"));
            	stmt.setObject(2, v.get("kbdataid"));
            	if(StringUtils.isEmpty(Objects.toString(v.get("query")))){
            		continue;
            	}
            	stmt.setObject(3, v.get("query"));
            	stmt.setObject(4, v.get("city"));
            	stmt.setObject(5, v.get("workerid"));
                num++;
                stmt.addBatch();
                //注意: 每5万，提交一次;这里不能一次提交过多的数据,我测试了一下，6万5000是极 
               //限，6万6000就会出问题，插入的数据量不对。
                if(num>500){
                    stmt.executeBatch();
                    conn.commit();
                    GlobalValue.myLog.info("提交事务，数据量为："+num);
                    num=0;
                }
            }
            stmt.executeBatch();
            conn.commit();
            GlobalValue.myLog.info("提交事务，数据量为："+num);
        }catch(Exception e){
        	try {
				conn.rollback();// 出现异常后，事务回滚
			} catch (SQLException e1) {
				GlobalValue.myLog.error(e1.toString());
				e1.printStackTrace();
			}
        	try {
				FileUtils.writeLines(new File(BASE_PATH, "error-query"+DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")+".txt"), datas);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
        	
            e.printStackTrace();
        }finally{
			close(stmt);
			close(conn);
            long endTime=System.currentTimeMillis();
            GlobalValue.myLog.info("方法执行时间："+(endTime-startTime)+"ms");
        }
	}
	
	public static void insertKbdata(List<JSONObject> datas){

		long startTime=System.currentTimeMillis();
       
        Connection conn=null;
        PreparedStatement stmt = null;
        try{
            conn=Database.getCon();
            conn.setAutoCommit(false);
           
            stmt=conn.prepareStatement("insert into kbdata(serviceid,kbdataid,topic,abstract,city,responsetype,interacttype) values (?,?,?,?,?,?,?)");
            System.out.println("数据大小："+datas.size());        //1000000
 
            int num=0;
            for(JSONObject v:datas){
            	stmt.setObject(1, v.get("serviceid"));
            	stmt.setObject(2, v.get("kbdataid"));
            	stmt.setObject(3, v.get("topic"));
            	stmt.setObject(4, v.get("abstract"));
            	stmt.setObject(5, v.get("city"));
            	stmt.setObject(6, v.get("responsetype"));
            	stmt.setObject(7, v.get("interacttype"));
                num++;
                stmt.addBatch();
                //注意: 每5万，提交一次;这里不能一次提交过多的数据,我测试了一下，6万5000是极 
               //限，6万6000就会出问题，插入的数据量不对。
                if(num>1000){
                    stmt.executeBatch();
                    conn.commit();
                    num=0;
                }
            }
            stmt.executeBatch();
            conn.commit();
        }catch(Exception e){
        	try {
				conn.rollback();// 出现异常后，事务回滚
			} catch (SQLException e1) {
				GlobalValue.myLog.error(e1.toString());
				e1.printStackTrace();
			}
        	try {
				FileUtils.writeLines(new File(BASE_PATH, "error-kbdata"+DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")+".txt"), datas);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
            e.printStackTrace();
        }finally{
			close(stmt);
			close(conn);
            long endTime=System.currentTimeMillis();
            GlobalValue.myLog.info("方法执行时间："+(endTime-startTime)+"ms");
        }
	}
	public static void insertWordpat(List<JSONObject> datas){
		
		
		long startTime=System.currentTimeMillis();
	       
        Connection conn=null;
        PreparedStatement stmt = null;
        try{
            conn=Database.getCon();
            conn.setAutoCommit(false);
           
            stmt=conn.prepareStatement("insert into wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand,edittime,workerid) values(?,?,?,?,?,?,?,sysdate,?)");
            System.out.println("数据大小："+datas.size());        //1000000
 
            int num=0;
            for(JSONObject v:datas){
            	stmt.setObject(1, v.get("wordpatid"));
            	stmt.setObject(2, v.get("wordpat"));
            	stmt.setObject(3, v.get("city"));
            	stmt.setObject(4, v.get("autosendswitch"));
            	stmt.setObject(5, v.get("wordpattype"));
            	stmt.setObject(6, v.get("kbdataid"));
            	stmt.setObject(7, v.get("brand"));
            	stmt.setObject(8, v.get("workerid"));
                num++;
                stmt.addBatch();
                //注意: 每5万，提交一次;这里不能一次提交过多的数据,我测试了一下，6万5000是极 
               //限，6万6000就会出问题，插入的数据量不对。
                if(num>500){
                    stmt.executeBatch();
                    conn.commit();
                    num=0;
                }
            }
            stmt.executeBatch();
            conn.commit();
        }catch(Exception e){
        	try {
				conn.rollback();// 出现异常后，事务回滚
			} catch (SQLException e1) {
				GlobalValue.myLog.error(e1.toString());
				e1.printStackTrace();
			}
        	try {
				FileUtils.writeLines(new File(BASE_PATH, "error-wordpat"+DateFormatUtils.format(new Date(), "yyyyMMddHHmmss")+".txt"), datas);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
            e.printStackTrace();
        }finally{
			close(stmt);
			close(conn);
            long endTime=System.currentTimeMillis();
            GlobalValue.myLog.info("方法执行时间："+(endTime-startTime)+"ms");
        }
        
	}
	/**
	 * 第二步：查询所有的客户问，并保存在本地文件中
	 */
	public static void selectQuery(){
		try {
			String serviceid="1835748.24";
			
			int count = 0;
			String countSql = "select count(*) as total from kbdata k,querymanage q where k.serviceid=? and q.kbdataid = k.kbdataid";
			Result rs = Database.executeQuery(countSql,serviceid);
			if(rs != null && rs.getRowCount()>0){
				String total = rs.getRows()[0].get("total").toString();
				count = Integer.parseInt(total);
			}
			
			String sql ="select  k.kbdataid, k.abstract,k.city abscity,q.query from kbdata k,querymanage q where k.serviceid=? and q.kbdataid = k.kbdataid ";
			
			int times = 20;
			int num = (count + 20) / times;
			for(int i = 0 ;i<times;i++){
				int start = i * num;
				int end = (i + 1)* num > count ? count : (i + 1)* num;
				String sql2 = "SELECT * FROM   (   SELECT A.*, ROWNUM RN   FROM ( "+sql+") A   WHERE ROWNUM <= "+end+"   )   WHERE RN > "+start;
				rs = Database.executeQuery(sql2,serviceid);
				File file = FileUtils.getFile(BASE_PATH,"query-"+start+"-"+end+".txt");
				if(!file.exists()){
					file.createNewFile();
				}
				//写入文件
				List<String> querys = new ArrayList<String>();
				if(rs != null && rs.getRowCount() > 0){
					for(int n =0 ;n<rs.getRowCount();n++){
						String kbdataid = Objects.toString(rs.getRows()[n].get("kbdataid"), "");
						
						String abs = Objects.toString(rs.getRows()[n].get("abstract"), "");
						
						String abscity = Objects.toString(rs.getRows()[n].get("abscity"), "");
						
						String query = Objects.toString(rs.getRows()[n].get("query"), "");
						querys.add(kbdataid+"#_#"+abs+"#_#"+abscity+"#_#"+query);
					}
				}
				FileUtils.writeLines(file, querys, false);
				System.out.println("生成成功！");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void selectKbdata(){
		try {
			String serviceid="1835748.24";
			
			String sql ="select kbdataid,abstract,city from kbdata k where k.serviceid=? and k.kbdataid  not in (select kbdataid from wordpat w where k.kbdataid = w.kbdataid and w.wordpattype = 5 ) ";
			Result rs = Database.executeQuery(sql,serviceid);
			File file = FileUtils.getFile(BASE_PATH,"kbdata-1.txt");
			if(!file.exists()){
				file.createNewFile();
			}
			//写入文件
			List<String> querys = new ArrayList<String>();
			if(rs != null && rs.getRowCount() > 0){
				for(int n =0 ;n<rs.getRowCount();n++){
					String kbdataid = Objects.toString(rs.getRows()[n].get("kbdataid"), "");
					
					String abs = Objects.toString(rs.getRows()[n].get("abstract"), "");
					
					String abscity = Objects.toString(rs.getRows()[n].get("city"), "");
					
					String query = abs.split(">")[1];
					querys.add(kbdataid+"#_#"+abs+"#_#"+abscity+"#_#"+query);
				}
			}
			FileUtils.writeLines(file, querys, false);
			System.out.println("写入数据："+rs.getRowCount());
			System.out.println("生成成功！");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 第三步：读取客户问文件，生成自学习词模，并生成本地词模文件
	 */
	public static void main3(){
		File[] files = FileUtils.getFile(BASE_PATH).listFiles();
		List<File> list = new ArrayList<>();
		for(File file : files){
			if(file.getName().startsWith("query-")){
				list.add(file);
			}
		}
		CountDownLatch countDownLatch = new CountDownLatch(1);
		for(int i = 0;i<= 0;i++){
			File file = list.get(i);
			AnalyzeThread analyzeThread = new AnalyzeThread(file.getName(), i+"");
			analyzeThread.setDownLatch(countDownLatch);
			analyzeThread.start();
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GlobalValue.myLog.info("所有线程执行完毕！");
	}
	

	public static void main7(){
		File[] files = FileUtils.getFile(BASE_PATH).listFiles();
		List<String> list = new ArrayList<>();
		for(File file : files){
			if(file.getName().startsWith("wordpat-save-")){
				try {
					List<String> readLines = FileUtils.readLines(file);
					String string = readLines.get(1) + "--" + readLines.get(0);
					list.add(string);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		try {
			FileUtils.writeLines(new File(BASE_PATH,"test.txt"),list);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// 颜色列表，用于验证码、噪线、噪点
	public static Color[] color = { Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
			Color.ORANGE, Color.PINK, Color.CYAN, Color.DARK_GRAY,
			Color.GRAY, Color.LIGHT_GRAY, Color.MAGENTA };
	// 字体列表，用于验证码
	public static String[] font = { "Times New Roman", "MS Mincho", "Book Antiqua",
			"Gungsuh", "PMingLiU", "Impact", "Georgia", "Verdana", "Arial",
			"Tahoma", "Courier New", "Arial Black", "Quantzite" };
	
	public static BufferedImage createImage(String imagesCodes) {
		if(StringUtils.isEmpty(imagesCodes)){
			return null;
		}
		Random rnd = new Random();
		StringBuffer sb = new StringBuffer();
		BufferedImage image = new BufferedImage(100, 40,
				BufferedImage.TYPE_INT_RGB);
		Graphics graphic = image.getGraphics();
		graphic.setColor(Color.WHITE);
		graphic.fillRect(0, 0, 100, 40);
		// 画随机字符
		for (int i = 0; i < imagesCodes.length(); i++) {
			graphic.setColor(color[rnd.nextInt(color.length)]);
			// 设置字体大小
			String fnt = font[rnd.nextInt(font.length)];
			Font ft = new Font(fnt, Font.PLAIN, 22);
			graphic.setFont(ft);
			graphic.drawString(imagesCodes.charAt(i) + "", i * 20 + 10, 27);
		}
		return image;
	}

	/** 验证码图片 */
	public static void main8(){
		// 验证码的字符集，去掉了一些容易混淆的字符
		char[] character = { '2', '3', '4', '5', '6', '8', '9', 'A', 'B', 'C',
				'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R',
				'S', 'T', 'W', 'X', 'Y', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
				'h', 'j', 'k', 'm', 'n', 'p', 'r', 's', 't', 'w', 'x', 'y' };
		int size = 300;
		Set<String> imagesCodes = new HashSet<String>(50000);
		Random rnd = new Random();
		while(imagesCodes.size() < size){
			String str = "";
			// 随机字符
			for (int i = 0; i < 4; i++) {
				String chkCode = String.valueOf(character[rnd
						.nextInt(character.length)]);
				str+=chkCode;
			}
			imagesCodes.add(str);
		}
		GlobalValue.myLog.info("生成图片总数为："+imagesCodes.size());
		File imageFile = new File(BASE_PATH,"images.txt");
		Iterator<String> iterator = imagesCodes.iterator();
		while (iterator.hasNext()) {
			try{
				String string = (String) iterator.next();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				BufferedImage image = createImage(string);
				ImageIO.write(image, "jpeg", bos);
				byte[] imageBts = bos.toByteArray();
				String filename =  string+".jpeg";
				File file = new File(BASE_PATH, filename);
				FileUtils.writeByteArrayToFile(file, imageBts);
				FileUtils.writeStringToFile(imageFile,filename+IOUtils.LINE_SEPARATOR , true);
				GlobalValue.myLog.info("生成图片："+filename);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 第四步：读取本地词模文件，执行导入词模操作。
	 */
	public static void main4(){
		File file = FileUtils.getFile(BASE_PATH,"190430.xlsx");
//		File file = FileUtils.getFile(BASE_PATH,"测试文件.xlsx");
		String extension = FilenameUtils.getExtension(file.getName());
		// 定义当前文件后得到的集合
		List<List<Object>> info = new ArrayList<List<Object>>();
		if ("xls".equalsIgnoreCase(extension)) {
			// 读取2003版的Excel
			info = MyUtil.read2003Excel(file, 2);
		} else if ("xlsx".equalsIgnoreCase(extension)) {
			// 读取2007版的Excel
			info = MyUtil.read2007Excel(file, 2);
		}
		String serviceid = "1835748.24";
		String serviceType = "银行行业->中信银行->多渠道应用";
		String workerid = "181";
		 // 获得商家标识符
		String bussinessFlag = CommonLibMetafieldmappingDAO
				.getBussinessFlag(serviceType);
		
		GlobalValue.myLog.info("初始数据大小："+info.size());
		String cloum1 = info.get(0).get(0)+"";
		if("标准问题".equals(cloum1)){//忽略Excel列名
			info.remove(0);
		}
		Map<String,String> map = new HashMap<String,String>();
		int size = info.size();
		for(int index = 0;index<size;index++){
			List<Object> line = info.get(index);
			String customerquery = line.get(1)!=null?  line.get(1).toString():"";
			if(StringUtils.isEmpty(customerquery)){
				GlobalValue.myLog.info("客户问  --- 为空");
				continue;
			}
			map.put(customerquery, customerquery);
		}
		try {
			File file2 = new File(BASE_PATH,"query.txt");
			if(file2.exists()) {
				List<String> lines = FileUtils.readLines(file2);
				for(String line :lines){
					if(StringUtils.isEmpty(line)) continue;
					if(StringUtils.isEmpty(line.trim())) continue;
					map.put(line, line);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GlobalValue.myLog.info("客户问总数为："+map.size());
		
		File[] files = FileUtils.getFile(BASE_PATH).listFiles();
		List<File> list = new ArrayList<>();
		for(File file2 : files){
			if(file2.getName().startsWith("wordpat-out-")){
				list.add(file2);
			}
		}
		CountDownLatch countDownLatch = new CountDownLatch(list.size());
		for(int i = 0;i< list.size();i++){
			File file2 = list.get(i);
			WordpatThread analyzeThread = new WordpatThread(file2.getName(), i+"");
			analyzeThread.setDownLatch(countDownLatch);
			analyzeThread.setMap(map);
			analyzeThread.start();
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GlobalValue.myLog.info("所有线程执行完毕！");
		
	}

	public static void main9(){
		File[] files = FileUtils.getFile(BASE_PATH).listFiles();
		List<File> list = new ArrayList<>();
		for(File file2 : files){
			if(file2.getName().startsWith("wordpat-out-")){
				list.add(file2);
			}
		}
		CountDownLatch countDownLatch = new CountDownLatch(list.size());
		for(int i = 0;i< list.size();i++){
			File file2 = list.get(i);
			WordpatThread analyzeThread = new WordpatThread(file2.getName(), i+"");
			analyzeThread.setDownLatch(countDownLatch);
			analyzeThread.setMap(null);
			analyzeThread.start();
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GlobalValue.myLog.info("所有线程执行完毕！");
	}
	
	public static void testAnalyze(){
		String serviceid = "1835748.24";
		String serviceType = "银行行业->中信银行->多渠道应用";
		String serviceCity = "中信银行";
		String query = "我的推荐码是啥";
		Result rs = CommonLibQueryManageDAO.getServiceCitys(serviceid);
		if (rs != null && rs.getRowCount() > 0) {
			serviceCity = rs.getRows()[0].get("city").toString();
		}
		
		String url = "http://km.knowology.cn:8082/NLPWebService/NLPCallerWS?wsdl";
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient(url);
		
		// 获取高级分析的接口串中的serviceInfo
		String serviceInfo = MyUtil.getServiceInfo(serviceType, "问题生成词模", "",
				false,serviceCity);
		// 获取高级分析的串
		String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模",
				query, serviceType, serviceInfo);
		
		GlobalValue.myLog.info("问题库自学习词模调用接口输入串：" + queryObject);
		// 获取接口的返回json串
		String result = NLPCaller4WSClient.kAnalyze(queryObject);
		
		GlobalValue.myLog.info("问题库自学习词模调用接口返回：" + result);
		
		
		// 获取高级分析的接口串中的serviceInfo
		serviceInfo = MyUtil.getServiceInfo(serviceType, "生成词模", "",
				false,serviceCity);
		// 获取高级分析的串
		queryObject = MyUtil.getDAnalyzeQueryObject("生成词模",
				query, serviceType, serviceInfo);
		GlobalValue.myLog.info("生成词模调用接口输入串：" + queryObject);
		// 获取接口的返回json串
		result = NLPCaller4WSClient.kAnalyze(queryObject);
		
		GlobalValue.myLog.info("生成词模调用接口返回：" + result);
		
	}
	protected static void close(ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (Exception ex) {
			GlobalValue.myLog.error("关闭连接异常信息==>" + ex);
		}
	}

	/**
	 *@description 关闭 Statement
	 *@param stmt
	 *@returnType void
	 */
	protected static void close(Statement stmt) {
		try {
			if (stmt != null)
				stmt.close();
		} catch (Exception ex) {
			// 写异常日志
			GlobalValue.myLog.error(ex.getMessage(), ex);
		}
	}

	/**
	 *@description 关闭 Connection
	 *@param con
	 *@returnType void
	 */
	protected static void close(Connection con) {
		try {
			if (con != null && !con.isClosed())
				con.close();

		} catch (Exception ex) {
			GlobalValue.myLog.error("关闭连接异常信息==>" + ex);
		}
	}
	/**
	 *@description 获得业务下客户问题字典
	 *@param serviceid
	 *@return
	 *@returnType Map<String,Map<String,String>>
	 */
 	public static Map<String, Map<String, String>> getQueryDic(String serviceid) {
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		Result rs = CommonLibQueryManageDAO.getQuery(serviceid,0);
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				Map<String, String> queryMap = new HashMap<String, String>();
				String normalquery = rs.getRows()[i].get("abstract").toString()
						.split(">")[1];
				String query = rs.getRows()[i].get("query") != null ? rs
						.getRows()[i].get("query").toString() : "";
				String city = rs.getRows()[i].get("city") != null ? rs
						.getRows()[i].get("city").toString() : "";
				String abscity = rs.getRows()[i].get("abscity") != null ? rs
						.getRows()[i].get("abscity").toString() : "";
				String kbdataid = rs.getRows()[i].get("kbdataid").toString();
				if (map.containsKey(normalquery)) {
					Map<String, String> tempMap = map.get(normalquery);
					tempMap.put(query, city);
					map.put(normalquery, tempMap);

				} else {
					queryMap.put(query, city);
					queryMap.put("kbdataid", kbdataid);
					queryMap.put("abscity", abscity);
					map.put(normalquery, queryMap);
				}
			}
		}
		return map;
	}
 	/**
	 *@description 获得业务下客户问题字典
	 *@param serviceid
	 *@return
	 *@returnType Map<String,Map<String,String>>
	 */
 	public static Map<String, Map<String, String>> getQueryDic2() {
 		int count = 0;
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		File[] files = FileUtils.getFile(BASE_PATH).listFiles();
		for(File file : files){
			if(file.getName().startsWith("query-")){
				List<String> lines = new ArrayList<String>();
				try {
					lines = FileUtils.readLines(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// 循环遍历数据源
				for (int i = 0 ,l = lines.size(); i < l; i++) {
					String line = lines.get(i);
					if(StringUtils.isEmpty(line)) continue;
					
					count ++;
					String[] split = line.split("#_#");
//					if(split.length > 3){
						Map<String, String> queryMap = new HashMap<String, String>();
						String kbdataid = split[0];
						
						String normalquery = split[1].split(">")[1];
						String abscity = split[2];
						String query = split[3];
						
						if (map.containsKey(normalquery)) {
							Map<String, String> tempMap = map.get(normalquery);
							tempMap.put(query, query);
							map.put(normalquery, tempMap);

						} else {
							queryMap.put(query, query);
							queryMap.put("kbdataid", kbdataid);
							queryMap.put("abscity", abscity);
							map.put(normalquery, queryMap);
						}
//					}
				}
				
			}
		}
		GlobalValue.myLog.info("读取文件中的数据总量为："+count);
		return map;
	}
	public static void main(String[] args) {
		try {
//			testAnalyze();
			
//			String query = "我的推荐码是啥";
//			String wordpat = "[<我的|!My近类|!本人近类>]*<推荐码|!推荐码近类>*[<是|!是近类>]*[<啥|!哪种近类|!什么近类>]@2#编者=\"自学习\"";
//			wordpat = wordpat.replace("编者=\"自学习\"", "编者=\"问题库\"&来源=\""+ query.replace("&", "\\and") + "\"");
//			System.out.println(wordpat);
			
			readProperties();
			
//			File path = new File("./WebRoot/");
//			if(path.exists()){
//				System.out.println(path.getPath());
//			}
			
//			List<JSONObject> jsonObjects = new ArrayList<>();
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("wordpatid", "2275212.24");
//			jsonObject.put("wordpat", "[我的|本人近类]*推荐码近类*[是|是近类]*[多少|多少近类]*在哪里|哪里近类*找|寻找近类|*搜索近类*#@2#编者=\"问题库\"&来源=\"我的推荐码是多少，在哪里找\"");
//			jsonObject.put("city", "180000");
//			jsonObject.put("autosendswitch", "0");
//			jsonObject.put("wordpattype", "5");
//			jsonObject.put("kbdataid", "11887791.24");
//			jsonObject.put("brand", "中信银行问题库");
//			jsonObject.put("workerid", "0723");
//			jsonObjects.add(jsonObject);
//			insertWordpat(jsonObjects);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static class AnalyzeThread extends Thread{
		public String fileName;
		public String outFileName;
		public String saveFileName;
		public String index;
		public int start;
		public CountDownLatch downLatch;
		public CountDownLatch getDownLatch() {
			return downLatch;
		}

		public void setDownLatch(CountDownLatch downLatch) {
			this.downLatch = downLatch;
		}

		public AnalyzeThread(String filename,String index) {
			this.saveFileName = "wordpat-save-"+index+".txt";
			this.fileName = filename;
			this.index = index;
			this.start = 0;
			this.outFileName = "wordpat-out-"+index+".txt";
			try {
				File file = new File(BASE_PATH,saveFileName);
				if(file.exists()){;
					List<String> readLines = FileUtils.readLines(file);
					if(readLines.size() > 1){
						this.start = Integer.parseInt(readLines.get(0));
						this.fileName = readLines.get(1);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			GlobalValue.myLog.info("当前线程起始位置："+start);
		}
		
		@Override
		public void run() {
			try {
				GlobalValue.myLog.info("线程"+index+"启动");
				String serviceType = "银行行业->中信银行->多渠道应用";
				String serviceid = "1835748.24";
				String serviceCity = "";
				Result rs = CommonLibQueryManageDAO.getServiceCitys(serviceid);
				if (rs != null && rs.getRowCount() > 0) {
					serviceCity = rs.getRows()[0].get("city").toString();
				}
				GlobalValue.myLog.info("业务地市："+serviceCity);
	
				// 获取高级分析的接口串中的serviceInfo
				String serviceInfo = MyUtil.getServiceInfo(serviceType, "问题生成词模", "",
						false,serviceCity);
				String url = "http://211.157.179.211:8082/NLPWebService/NLPCallerWS?wsdl";
				
				// 获取高级分析的客户端
				NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
						.NLPCaller4WSClient(url);
				// 判断接口是否连接是否为null
				if (NLPCaller4WSClient == null) {
					save();
					return;
				}
						
			
				List<String> lines = FileUtils.readLines(new File(BASE_PATH,fileName));
				int size = lines.size();
				GlobalValue.myLog.info("总数据量："+size);
				int num = 0;
				List<String> wordpatList = new ArrayList<String>();
				for(int i = start;i<size;i++){
					String line  = lines.get(i);
					if(StringUtils.isEmpty(line)) continue;
					if(StringUtils.isEmpty(line.trim())) continue;
					
					String[] split = line.split("#_#");
					String kbdataid = split[0];
					
					String query = split[3];
						
					// 获取高级分析的串
					String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模",
							query, serviceType, serviceInfo);
					
					// 获取接口的返回json串
					String result = NLPCaller4WSClient.kAnalyze(queryObject);
					
					// add by zhao lipeng. 20170210 START
					GlobalValue.myLog.info("问题库自学习词模调用接口返回：" + result);
					// add by zhao lipeng. 20170210 END
					
					// 替换掉回车符和空格
					result = result.replace("\n", "").replace(" ", "").trim();
					String wordpat = "";
					// 判断返回串是否为"接口请求参数不合规范！"、""、null
					if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
						// 返回的词模为空
					} else {
						try {
							// 将接口的返回串用JSONObject转换为JSONObject对象
							JSONObject obj = JSONObject.parseObject(result);
							wordpat = obj.getString("autoLearnedPat");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
//					logger.info("问题库自学习词模：" + wordpat);
					if (wordpat != null && !"".equals(wordpat)) {
						// 判断词模是否含有@_@
						if (wordpat.contains("@_@")) {
							// 有的话，按照@_@进行拆分,并只取第一个
							wordpat = wordpat.split("@_@")[0];
						}
						wordpat = wordpat.replace("编者=\"自学习\"", "编者=\"问题库\"&来源=\""+ query.replace("&", "\\and") + "\"");
						String str = kbdataid+"#__#"+query+"#__#"+wordpat;
						wordpatList.add(str);
						num++;
					}
					if(num >= 50){
						GlobalValue.myLog.info("生成词模数量："+num);
						try {
							FileUtils.writeLines(new File(BASE_PATH,outFileName), wordpatList,true);
						} catch (Exception e) {
							e.printStackTrace();
						}
						wordpatList = new ArrayList<String>();
						num = 0;
						this.start = i;
						save();
					}
				}
				GlobalValue.myLog.info("生成词模数量："+num);
				try {
					FileUtils.writeLines(new File(BASE_PATH,outFileName), wordpatList,true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				this.start = size;
				save();
			} catch (IOException e) {
				GlobalValue.myLog.info("线程"+index+"异常结束");
				e.printStackTrace();
			}finally{
				 this.downLatch.countDown();
			}
		}
		public void save(){
			try {
				List<String> list = new ArrayList<String>();
				list.add(start+"");
				list.add(this.fileName);
				FileUtils.writeLines(new File(BASE_PATH,this.saveFileName ),list ,false);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
		
		
	}
	
	public static class WordpatThread extends Thread{
		public String fileName;
		public String saveFileName;
		public String index;
		public int start;
		public int skips = 0;
		public Map<String,String> map;
		public Map<String, String> getMap() {
			return map;
		}

		public void setMap(Map<String, String> map) {
			this.map = map;
		}
		public CountDownLatch downLatch;
		public CountDownLatch getDownLatch() {
			return downLatch;
		}

		public void setDownLatch(CountDownLatch downLatch) {
			this.downLatch = downLatch;
		}

		public WordpatThread(String filename,String index) {
			this.saveFileName = "wordpat-insert-save-"+index+".txt";
			this.fileName = filename;
			this.index = index;
			this.start = 0;
			
			try {
				File file = new File(BASE_PATH,saveFileName);
				if(file.exists()){
					List<String> readLines = FileUtils.readLines(file);
					if(readLines.size() > 1){
						this.start = Integer.parseInt(readLines.get(0));
						this.fileName = readLines.get(1);
						this.skips = Integer.parseInt(readLines.get(3));
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			GlobalValue.myLog.info("当前线程起始位置："+start);
		}
		
		@Override
		public void run() {
			try {
				GlobalValue.myLog.info("线程"+index+"启动");
				String serviceType = "银行行业->中信银行->多渠道应用";
				String serviceid = "1835748.24";
				String serviceCity = "";
				String brand = "";
				String bussinessFlag = CommonLibMetafieldmappingDAO
						.getBussinessFlag(serviceType);
				String sql = "select * from service where serviceid = ?";
				Result rs = Database.executeQuery(sql, serviceid);
				if (rs != null && rs.getRowCount() > 0) {
					serviceCity = rs.getRows()[0].get("city").toString();
					brand = rs.getRows()[0].get("brand").toString();
				}
				GlobalValue.myLog.info("业务地市："+serviceCity);
	
			
				List<String> lines = FileUtils.readLines(new File(BASE_PATH,fileName));
				int size = lines.size();
				GlobalValue.myLog.info("总数据量："+size);
				int num = 0;
				List<JSONObject> wordpatList = new ArrayList<JSONObject>();
				for(int i = start;i<size;i++){
					String line  = lines.get(i);
					if(StringUtils.isEmpty(line)) continue;
					if(StringUtils.isEmpty(line.trim())) continue;
					
					String[] split = line.split("#__#");
					String kbdataid = split[0];
					String query = split[1];
					String wordpat = split[2];
					if(map !=null && !map.containsKey(query)){
						skips++;
						continue;
					}
					String wordpatid="";
					if (GetConfigValue.isOracle) {
						wordpatid = ConstructSerialNum
								.GetOracleNextValNew("SEQ_WORDPATTERN_ID",
										bussinessFlag);
					} else if (GetConfigValue.isMySQL) {
						wordpatid = ConstructSerialNum.getSerialIDNew(
								"wordpat", "wordpatid", bussinessFlag);
					}
					
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("wordpatid", wordpatid);
					jsonObject.put("wordpat", wordpat);
					jsonObject.put("city", serviceCity);
					jsonObject.put("autosendswitch", "0");
					jsonObject.put("wordpattype", "5");
					jsonObject.put("kbdataid", kbdataid);
					jsonObject.put("brand", brand);
					jsonObject.put("workerid", "181123");
					wordpatList.add(jsonObject);
					GlobalValue.myLog.info("构建词模数据："+jsonObject);
					if(num >= 1000){
						GlobalValue.myLog.info("处理词模数量："+num);
						if(wordpatList.size() > 0){
							insertWordpat(wordpatList);
						}
						wordpatList = new ArrayList<JSONObject>();
						num = 0;
						this.start = i;
						save();
					}
				}
				GlobalValue.myLog.info("处理词模数量："+num);
				if(wordpatList.size() > 0){
					insertWordpat(wordpatList);
				}
				this.start = size;
				save();
			} catch (IOException e) {
				GlobalValue.myLog.info("线程"+index+"异常结束");
				e.printStackTrace();
			}finally{
				 this.downLatch.countDown();
			}
		}
		public void save(){
			try {
				List<String> list = new ArrayList<String>();
				list.add(start+"");
				list.add(this.fileName);
				list.add(this.skips + "");
				FileUtils.writeLines(new File(BASE_PATH,this.saveFileName ),list ,false);
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
		
		
	}
	
	public static void  propertiesToXML() throws Exception{
		Properties props = new Properties();
		String name = "jdbc_oracle";
        InputStream inputStream = TestDAO.class.getClassLoader().getResourceAsStream(name+".properties");
        props.load(inputStream);
        //where to store?
        OutputStream os = new FileOutputStream("z:\\"+name+".xml");
        //store the properties detail into a pre-defined XML file
        props.storeToXML(os, null);
		
	}
	
	public static void readProperties(){
		String conObject = "oracle";
		
//		String jdbcProPath = "jdbc_" + conObject + ".properties";
		String jdbcProPath = "jdbc_" + conObject + ".xml";
		URL resource = Database.class.getClassLoader().getResource(jdbcProPath);
		InputStream inputStream = Database.class.getClassLoader().getResourceAsStream(jdbcProPath);
		if(inputStream != null){
			System.out.println("true");
		}else{
			System.out.println("false");
		}

		String file = resource.getFile();
		File testFile = new File(file);
		if(testFile.exists()){
			System.out.println("true");
		}else{
			System.out.println("false");
		}
	}
}
