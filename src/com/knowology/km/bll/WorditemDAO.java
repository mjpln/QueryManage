package com.knowology.km.bll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.jstl.sql.Result;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibQuestionUploadDao;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.dal.Database;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;

public class WorditemDAO {
	
	public static Map<String,String> cityMap = new LinkedHashMap<String,String>();
	// 省市区树结构
	static{
		Result cityRs = null;
		String citySql = "";
		citySql = "select t.name as id,min(s.name) as city from metafield t,metafield s,metafieldmapping a where a.name='地市编码配置' and t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and  t.name like '______' group by t.name order by id";
		try {
			cityRs = Database.executeQuery(citySql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (cityRs != null & cityRs.getRowCount()>0){
			for (int i = 0;i < cityRs.getRowCount();i++){
				if (cityRs.getRows()[i].get("id").toString().length() == 6){
					cityMap.put(cityRs.getRows()[i].get("id").toString(), cityRs.getRows()[i].get("city").toString());
				}
			}
		}
		//System.out.println(cityMap);
	}
	
	public static Object getCityTree(String local) {
		String cityname[] = local.split(",");
		Map<String,String> map = new HashMap<String,String>();
		for(int m=0;m<cityname.length;m++){
			map.put(cityname[m], "");
		}
		JSONArray jsonAr = new JSONArray();
		
		Result rs =null;
		if (!cityMap.isEmpty()){
			JSONObject allJsonObj = new JSONObject();
			allJsonObj.put("id", "全国");
			allJsonObj.put("text", "全国");
			if (map.containsKey("全国")){
				allJsonObj.put("checked", true);
			}
			jsonAr.add(allJsonObj);
			for (Map.Entry<String, String> pro : cityMap.entrySet()){
				if (pro.getKey().contains("0000")){
					JSONObject jsonObj = new JSONObject();
					String id = pro.getKey();
					String province = pro.getValue();
					jsonObj.put("id", id);
					jsonObj.put("text", province);
					if (map.containsKey(province)){
						jsonObj.put("checked", true);
					}
					if (province.indexOf("市")<0){ //省
						String innerid = id.substring(0, 2);
						JSONArray jsonArrSon = new JSONArray();
						for (Map.Entry<String, String> proSon : cityMap.entrySet()){//地级市
							if (proSon.getKey().startsWith(innerid) && proSon.getKey().endsWith("00") && !proSon.getKey().endsWith("0000")){
								JSONObject jsonObjSon = new JSONObject();
								jsonObjSon.put("id", proSon.getKey());
								jsonObjSon.put("text", proSon.getValue());
							
								String innerInnerId = proSon.getKey().substring(0, 4);
								JSONArray jsonArrSonSon = new JSONArray();
								for (Map.Entry<String, String> proSonSon : cityMap.entrySet()){
									if (proSonSon.getKey().startsWith(innerInnerId) && !proSonSon.getKey().endsWith("00") && !proSonSon.getKey().endsWith("01")){
										JSONObject jsonObjSonSon = new JSONObject();
										jsonObjSonSon.put("id", proSonSon.getKey());
										jsonObjSonSon.put("text", proSonSon.getValue());
										if (map.containsKey(proSonSon.getValue())){
											jsonObjSonSon.put("checked", true);
										}
										jsonArrSonSon.add(jsonObjSonSon);
									}
								}
								
								if(!jsonArrSonSon.isEmpty()){
									jsonObjSon.put("children", jsonArrSonSon);
									jsonObjSon.put("state", "closed");
								}
								if (map.containsKey(proSon.getValue())){
									jsonObjSon.put("checked", true);
								}
								jsonArrSon.add(jsonObjSon);
							}
						}
						if (!jsonArrSon.isEmpty()){
							jsonObj.put("children",jsonArrSon);
							jsonObj.put("state", "closed");
						}
					} else { // 直辖市
						String innerid = id.substring(0, 2);
						JSONArray jsonArrSon = new JSONArray();
						for (Map.Entry<String, String> proSon : cityMap.entrySet()){
							if (proSon.getKey().startsWith(innerid) && !proSon.getKey().endsWith("00")){
								JSONObject jsonObjSon = new JSONObject();
								jsonObjSon.put("id", proSon.getKey());
								jsonObjSon.put("text", proSon.getValue());
								if (map.containsKey(proSon.getValue())){
									jsonObjSon.put("checked", true);
								}
								jsonArrSon.add(jsonObjSon);
							}
						}
						if (!jsonArrSon.isEmpty()){
							jsonObj.put("children",jsonArrSon);
							jsonObj.put("state", "closed");
						}
					}
					jsonAr.add(jsonObj);
				}
			}
		}
		//System.out.println(jsonAr);
		return jsonAr;
	}
	
	/**
	 * 带分页的查询满足条件的词条信息
	 * 
	 * @param start参数开始条数
	 * @param limit参数每页条数
	 * @param worditem参数词条
	 * @param worditemprecise参数是否精确
	 * @param iscurrentwordclass参数是否当前词类
	 * @param worditemtype参数词条类型
	 * @param curwordclass参数当前词类名称
	 * @return json串
	 */
	public static Object select(int start, int limit, String worditem,
			Boolean worditemprecise, Boolean iscurrentwordclass,
			String worditemtype, String curwordclass) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		int count = UserOperResource.getWordCount(worditem, worditemprecise, iscurrentwordclass, worditemtype, curwordclass, "基础");
			// 判断数据源不为null且含有数据
			if (count  > 0) {
				// 将条数放入jsonObj的total对象中
				jsonObj.put("total", count);
				Result rs = UserOperResource.selectWord(start, limit, worditem, worditemprecise, iscurrentwordclass, worditemtype, curwordclass, "基础");
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 循环遍历数据源
					for (int i = 0; i < rs.getRowCount(); i++) {
						// 定义json对象
						JSONObject obj = new JSONObject();
						// 生成id对象
						obj.put("id", start + i + 1);
						// 生成worditem对象
						obj.put("worditem", rs.getRows()[i].get("word"));
						// 生成wordclass对象
						obj.put("wordclass", rs.getRows()[i].get("wordclass"));
						// 生成type对象
						obj.put("type", rs.getRows()[i].get("type"));
						// 生成wordid对象
						obj.put("wordid", rs.getRows()[i].get("wordid"));
						// 生成wordclassid对象
						obj.put("wordclassid", rs.getRows()[i]
								.get("wordclassid"));
						// 将生成的对象放入jsonArr数组中
						jsonArr.add(obj);
					}
				}
				// 将jsonArr数组放入jsonObj的root对象中
				jsonObj.put("root", jsonArr);
			} else {
				// 将0放入jsonObj的total对象中
				jsonObj.put("total", 0);
				// 清空jsonArr数组
				jsonArr.clear();
				// 将空的jsonArr数组放入jsonObj的root对象中
				jsonObj.put("root", jsonArr);
			}
		
		return jsonObj;
	}

	/**
	 * 更新词条
	 * 
	 * @param oldworditem参数原有词条
	 * @param newworditem参数新的词条
	 * @param oldtype参数原有类型
	 * @param newtype参数新的类型
	 * @param wordclassid参数词类id
	 * @param wordid参数词条id
	 * @return 更新返回的json串
	 */
	public static Object update(String oldworditem, String newworditem,
			String oldtype, String newtype, String wordclassid, String wordid,String curwordclass,String curwordclasstype) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断是否已存在相同词条
//		if (Exists(wordclassid, newworditem, newtype)) {
//			// 将false放入jsonObj的success对象中
//			jsonObj.put("success", false);
//			// 将存在信息放入jsonObj的msg对象中
//			jsonObj.put("msg", "词条已存在!");
//			return jsonObj;
//		}
		if(UserOperResource.isExistWord(wordclassid, newworditem, newtype)){
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将存在信息放入jsonObj的msg对象中
			jsonObj.put("msg", "词条已存在!");
			return jsonObj;
		}
		
		// 需要判断该标准名称是否已经录入了别名，录入了别名则不能修改！
		if ((oldtype.equals(newtype) && "标准名称".equals(oldtype))||(!oldtype.equals(newtype) && "标准名称".equals(oldtype))) {
//			// 定义查询别名的SQL语句
//			String sql = "select wordid from word where rownum<2 and stdwordid=?";
//			// 定义绑定参数集合
//			List<String> lstpara = new ArrayList<String>();
//			lstpara.add(wordid);
//			try {
//				// 执行SQL语句，获取相应的数据源
//				Result rs = Database.executeQuery(sql, lstpara.toArray());
//				// 判断数据源不为null且含有数据
//				if (rs != null && rs.getRowCount() > 0) {
//					// 将false放入jsonObj的success对象中
//					jsonObj.put("success", false);
//					// 将存在信息放入jsonObj的msg对象中
//					jsonObj.put("msg", "当前词作为标准词，已经录入了别名，不能修改!");
//					return jsonObj;
//				}
//			} catch (SQLException e) {
//				e.printStackTrace();
//				// 出现错误
//				// 将false放入jsonObj的success对象中
//				jsonObj.put("success", false);
//				// 将存在信息放入jsonObj的msg对象中
//				jsonObj.put("msg", "当前词作为标准词，已经录入了别名，不能修改!");
//				return jsonObj;
//			}
			
			if(UserOperResource.isHaveOtherName(wordid)){
				jsonObj.put("success", false);
				// 将存在信息放入jsonObj的msg对象中
				jsonObj.put("msg", "当前词作为标准词，已经录入了别名，不能修改!");
				return jsonObj;
			}
		}
		// 执行修改操作，返回修改事务处理结果
//		int c = _update(oldworditem, newworditem, oldtype, newtype, wordid,wordclassid);
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User)sre;
		int c = UserOperResource.updateWord(user, oldworditem, newworditem, oldtype, newtype, wordid, wordclassid, curwordclass, curwordclasstype, "基础");
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "修改成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "修改失败!");
		}
		return jsonObj;
	}

	/**
	 * 判断词条是否重复
	 * 
	 * @param curwordclassid参数当前词类id
	 * @param worditem参数词条名称
	 * @param newtype参数词条类型
	 * @return 是否重复
	 */
	public static Boolean Exists(String curwordclassid, String worditem,
			String newtype) {
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 查询词条是否重复的SQL语句
		String sql = "select wordid from word where rownum<2 and wordclassid=? and word=? and type=? ";
		// 绑定词类id参数
		lstpara.add(curwordclassid);
		// 绑定词条参数
		lstpara.add(worditem);
		// 绑定词条类型参数
		lstpara.add(newtype);
		try {
			// 执行SQL语句，获取相应的数据源
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 有重复词条，返回true
				return true;
			} else {
				// 没有重复词条，返回false
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			return false;
		}
	}

	/**
	 * 修改词条操作
	 * 
	 * @param oldworditem参数原有词条
	 * @param newworditem参数新的词条
	 * @param oldtype参数原有类型
	 * @param newtype参数新的类型
	 * @param wordid参数词条id
	 * @return 修改返回的结果
	 */
	private static int _update(String oldworditem, String newworditem,
			String oldtype, String newtype, String wordid,String wordclassid) {
		// 定义多条SQL语句集合
		List<String> lstsql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstlstpara = new ArrayList<List<?>>();
		// 更新词条的SQL语句
		String sql = "update word set word=?,type=? , time = sysdate  where wordid=? ";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定词条参数
		lstpara.add(newworditem);
		// 绑定类型参数
		lstpara.add(newtype);
		// 绑定词条id参数
		lstpara.add(wordid);
		// 将SQL语句放入集合中
		lstsql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstlstpara.add(lstpara);
		
		// 更新当前词类编辑时间
		sql = "update wordclass set time =sysdate  where  wordclassid = ? ";
		// 定义绑定参数集合
		lstpara = new ArrayList<String>();
		// 绑定词条参数
		lstpara.add(wordclassid);
		// 将SQL语句放入集合中
		lstsql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstlstpara.add(lstpara);

		// 生成操作日志记录
		// 将SQL语句放入集合中
		lstsql.add(MyUtil.LogSql());
		// 将对应的绑定参数集合放入集合中
		lstlstpara
				.add(MyUtil.LogParam(" ", " ", "更新词条", "上海",
						oldworditem + "==>" + newworditem + "," + oldtype
								+ "==>" + newtype, "WORD"));
		// 执行SQL语句，绑定事务处理，返回事务处理结果
		return Database.executeNonQueryTransaction(lstsql, lstlstpara);
	}

	/**
	 * 插入词条
	 * 
	 * @param worditem参数词条
	 * @param curwordclass参数当前词类名称
	 * @param curwordclassid参数当前词类id
	 * @param isstandardword参数是否是标准词
	 * @return 插入返回的json串
	 */
	public static Object insert(String worditem, String curwordclass,
			String curwordclassid, String curwordclasstype,Boolean isstandardword) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断当前处理id是否为空，null
		if (curwordclassid == null || "".equals(curwordclassid)) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将信息放入jsonObj的msg对象中
			jsonObj.put("msg", "请选择当前词类!");
			return jsonObj;
		}
		// 用换行符拆分用户输入的多条词条,去除空字符串和空格等空白字符
		List<String> lstWorditem = new ArrayList<String>(Arrays.asList(worditem.split("\n")));
		// 获取该词条的type
		String type = "";
		if (isstandardword) {
			type = "标准名称";
		} else {
			type = "普通词";
		}
		// 循环遍历词条集合
		String msg ="";
		List<String> listWord= new ArrayList<String>();
		for (int i = 0; i < lstWorditem.size(); i++) {
			String wd = lstWorditem.get(i);
			// 判断是否已存在相同词条
			if (Exists(curwordclassid, wd , type)) {
				if("".equals(msg)){
					msg="第";	
				}
				msg = msg+(i + 1)+",";
			}else{
				listWord.add(wd);	
			}
		}
		if(msg.length()>1){
			msg = msg.substring(0, msg.lastIndexOf(","));
			msg =msg +"条词条已存在!";
		}
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User)sre;
		
		int c =-1;
		if(listWord.size()>0){
			// 执行SQL语句，获取事务处理的结果
			c = UserOperResource.insertWord(user, curwordclassid, curwordclass, curwordclasstype, listWord, type, "基础");
		}else{
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false); 
			// 将信息放入jsonObj的msg对象中
			jsonObj.put("msg", msg);
			return jsonObj;
		}
		
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "保存成功!<br>"+msg);
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "保存失败!");
		}
		return jsonObj;
	}

	/**
	 * 保存操作
	 * 
	 * @param curwordclassid参数词类id
	 * @param curwordclass参数词类名称
	 * @param lstWorditem参数词条集合
	 * @param type参数词条类型
	 * @return 保存返回的结果
	 */
	private static int _insert(String curwordclassid, String curwordclass,
			List<String> lstWorditem, String type) {
		// 定义多条SQL语句集合
		List<String> lstsql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstlstpara = new ArrayList<List<?>>();
		// 定义保存词条的SQL语句
		String sql = "";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 循环遍历词条集合
		for (int i = 0; i < lstWorditem.size(); i++) {
			// 定义保存词条的SQL语句
			sql = "insert into word(wordid,wordclassid,word,type) values(?,?,?,?) ";
			// 定义绑定参数集合
			lstpara = new ArrayList<String>();
			// 获取词条表的序列值
			String id = String.valueOf(SeqDAO.GetNextVal("seq_word_id"));
			// 绑定id参数
			lstpara.add(id);
			// 绑定词类id参数
			lstpara.add(curwordclassid);
			// 绑定词类名称参数
			lstpara.add(lstWorditem.get(i));
			// 绑定类型参数
			lstpara.add(type);
			// 将SQL语句放入集合中
			lstsql.add(sql);
			// 将对应的绑定参数集合放入集合中
			lstlstpara.add(lstpara);
			
			

			// 生成操作日志记录
			// 将SQL语句放入集合中
			lstsql.add(MyUtil.LogSql());
			// 将对应的绑定参数集合放入集合中
			lstlstpara.add(MyUtil.LogParam(" ", " ", "增加词条", "上海", curwordclass
					+ "==>" + lstWorditem.get(i), "WORD"));
		}
	
		// 更新当前词类编辑时间
		sql = "update wordclass set time =sysdate  where  wordclassid = ? ";
		// 定义绑定参数集合
		lstpara = new ArrayList<String>();
		// 绑定词条参数
		lstpara.add(curwordclassid);
		// 将SQL语句放入集合中
		lstsql.add(sql);
		// 将对应的绑定参数集合放入集合中
		lstlstpara.add(lstpara);
		
		// 执行SQL语句，绑定事务处理，返回事务处理结果
		return Database.executeNonQueryTransaction(lstsql, lstlstpara);
	}

	/**
	 * 删除词条
	 * 
	 * @param wordid参数词条id
	 * @param curwordclass参数词类名称
	 * @param worditem参数词条名称
	 * @return 删除返回的json串
	 */
	public static Object delete(String wordid, String curwordclass,String curwordclasstype,
			String worditem) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();		
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User)sre;
		int c = UserOperResource.deleteWord(user, wordid, curwordclass, curwordclasstype, worditem, "基础");
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "删除成功!");
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "删除失败!");
		}
		return jsonObj;
	}
	
	

	/**
	 *@description  查询词条city
	 *@param wordclass 词类
	 *@param wordid 词条ID
	 *@return 
	 *@returnType Object 
	 */
	public static Object selectWordCity(String wordclass ,String wordid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();		
		Result rs = UserOperResource.selectWordCity(wordclass,wordid);
		
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 生成id对象
			jsonObj.put("cityname", rs.getRows()[0].get("cityname")==null ? "":rs.getRows()[0].get("cityname"));
			jsonObj.put("citycode", rs.getRows()[0].get("city")==null ? "":rs.getRows()[0].get("city"));
			jsonObj.put("success", true);
		}else{
			jsonObj.put("cityname", "");
			jsonObj.put("citycode", "");
			jsonObj.put("success", false); 
		}
		
		return jsonObj;
	}
	
	/**
	 *@description  更新词条city
	 *@param wordclass 词类
	 *@param wordid 词条ID
	 *@param cityNme 城市名称
	 *@param cityCode 城市代码
	 *@return 
	 *@returnType Object 
	 */
	public static Object updateWordCity(String wordclass ,String wordid,String cityNme,String cityCode) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();		
		int c  = UserOperResource.updateWordCity(wordclass, wordid, cityNme, cityCode);
		
		// 判断数据源不为null且含有数据
		if (c>0) {
				// 生成id对象
			jsonObj.put("msg", "更新成功!");
			jsonObj.put("success", true);
		}else{
			jsonObj.put("msg", "更新失败!");
			jsonObj.put("success", false);
		}
		
		return jsonObj;
	}
	
}


