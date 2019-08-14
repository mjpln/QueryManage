package com.knowology.km.bll;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.jsp.jstl.sql.Result;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.dal.Database;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;

public class SynonymDAO {
	/**
	 * 带分页的查询满足条件的别名名称
	 * 
	 * @param start参数开始条数
	 * @param limit参数每页条数
	 * @param synonym参数别名名称
	 * @param isprecise参数是否精确查询
	 * @param iscurrentworditem参数是否当前词条
	 * @param type参数别名类型
	 * @param curworditem参数当前词条名称
	 * @param curwordclass参数当前词类名称
	 * @return json串
	 */
	public static Object select(int start, int limit, String synonym,
			Boolean isprecise, Boolean iscurrentworditem, String type,
			String curworditem, String curwordclass) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		Result rs  = UserOperResource.getSynonymCount(synonym, isprecise, iscurrentworditem, type, curworditem, curwordclass,"基础");
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 将条数放入jsonObj的total对象中
				jsonObj.put("total", rs.getRowCount());
				// 执行带分页的查询满足条件的SQL语句
				rs = UserOperResource.selectSynonym(start, limit, synonym, isprecise, iscurrentworditem, type, curworditem, curwordclass,"基础");
				if (rs != null && rs.getRowCount() > 0) {
					// 循环遍历数据源
					for (int i = 0; i < rs.getRowCount(); i++) {
						// 定义json对象
						JSONObject obj = new JSONObject();
						// 生成id对象
						obj.put("id", start + i + 1);
						// 生成worditem对象
						obj.put("worditem", rs.getRows()[i].get("worditem"));
						// 生成synonym对象
						obj.put("synonym", rs.getRows()[i].get("word"));
						// 生成type对象
						obj.put("type", rs.getRows()[i].get("type"));
						// 生成wordclass对象
						obj.put("wordclass", rs.getRows()[i].get("wordclass"));
						// 生成wordid对象
						obj.put("wordid", rs.getRows()[i].get("wordid"));
						// 生成stdwordid对象
						obj.put("stdwordid", rs.getRows()[i].get("stdwordid"));
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
	 * 更新别名
	 * 
	 * @param oldsynonym参数原有别名
	 * @param newsynonym参数新的别名
	 * @param oldtype参数原有类型
	 * @param newtype参数新的类型
	 * @param wordid参数别名id
	 * @param stdwordid参数词条id
	 * @return 更新返回的json串
	 */
	public static Object update(String oldsynonym, String newsynonym,
			String oldtype, String newtype, String wordid, String stdwordid,String curwordclass) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断是否更新的是别名名称
		if (!newsynonym.equals(oldsynonym)) {
			// 判断是否已存在相同词类
//			if (Exists(stdwordid, newsynonym)) {
//				// 将false放入jsonObj的success对象中
//				jsonObj.put("success", false);
//				// 将别名重复信息放入jsonObj的msg对象中
//				jsonObj.put("msg", "别名已存在!");
//				return jsonObj;
//			}
			if(UserOperResource.isExistSynonym(stdwordid, newsynonym)){
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将别名重复信息放入jsonObj的msg对象中
				jsonObj.put("msg", "别名已存在!");
				return jsonObj;
			}
			
		}
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User)sre;
		int c = UserOperResource.updateSynonym(user, oldsynonym, newsynonym, oldtype, newtype, wordid, stdwordid, curwordclass);
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
	 * 判断别名是否重复
	 * 
	 * @param stdwordid参数词条id
	 * @param synonym参数别名名称
	 * @return 是否重复
	 */
	public static Boolean Exists(String stdwordid, String synonym) {
		// 对应绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 定义查询别名的SQL语句
		String sql = "select word from word where rownum<2 and word=? and stdwordid=?";
		// 绑定别名名称参数
		lstpara.add(synonym);
		// 绑定词条id参数
		lstpara.add(stdwordid);
		try {
			// 执行SQL语句，获取相应的数据源
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 有数据，表示重复
				return true;
			} else {
				// 没有数据，不是不重复
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			return false;
		}
	}

	/**
	 * 新增别名
	 * 
	 * @param wordclassid参数词类id
	 * @param synonyms参数多个别名名称
	 * @param type参数别名类型
	 * @param stdwordid参数词条ID
	 * @param curworditem参数当前词条名称
	 * @param curwordclass参数当前词类名称
	 * @return 新增返回的json串
	 */
	public static Object insert(String wordclassid, String synonym,
			String type, String stdwordid, String curworditem,
			String curwordclass) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 用换行符拆分用户输入的多条别名,去除空字符串和空格等空白字符
		List<String> lstSynonym = Arrays.asList(synonym.split("\n"));
		// 循环遍历别名集合
		for (int i = 0; i < lstSynonym.size(); i++) {
			// 判断是否已存在相同别名
//			if (Exists(stdwordid, lstSynonym.get(i))) {
//				// 将false放入jsonObj的success对象中
//				jsonObj.put("success", false);
//				// 将重复信息放入jsonObj的msg对象中
//				jsonObj.put("msg", "第" + (i + 1) + "条别名已存在!");
//				return jsonObj;
//			}
			if(UserOperResource.isExistSynonym(stdwordid,lstSynonym.get(i))){
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将别名重复信息放入jsonObj的msg对象中
				jsonObj.put("msg", "第" + (i + 1) + "条别名已存在!");
				return jsonObj;
			}
		}
		// 执行新增操作，返回新增事务的结果
//		int c = _insert(wordclassid, lstSynonym, stdwordid, type, curworditem,
//				curwordclass);
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User)sre;
		int c = UserOperResource.insertSynonym(user, wordclassid, lstSynonym, stdwordid, type, curworditem, curwordclass);
		// 判断事务处理结果
		if (c > 0) {
			// 事务处理成功
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "保存成功!");
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
	 * 新增别名的操作
	 * 
	 * @param wordclassid参数词类id
	 * @param lstSynonym参数别名集合
	 * @param stdwordid参数词条id
	 * @param type参数别名类型
	 * @param curworditem参数当前词条名称
	 * @param curwordclass参数当前词类名称
	 * @return 新增返回的结果
	 */
	private static int _insert(String wordclassid, List<String> lstSynonym,
			String stdwordid, String type, String curworditem,
			String curwordclass) {
		// 定义多条SQL语句集合
		List<String> lstsql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstlstpara = new ArrayList<List<?>>();
		// 定义保存别名的SQL语句
		String sql = "";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 循环遍历别名集合
		for (int i = 0; i < lstSynonym.size(); i++) {
			// 定义新增别名的SQL语句
			sql = "insert into word(wordid,wordclassid,word,stdwordid,type) values(?,?,?,?,?)";
			// 定义绑定参数集合
			lstpara = new ArrayList<String>();
			// 获取别名的序列值
			String id = String.valueOf(SeqDAO.GetNextVal("seq_word_id"));
			// 绑定id参数
			lstpara.add(id);
			// 绑定词类id参数
			lstpara.add(wordclassid);
			// 绑定别名参数
			lstpara.add(lstSynonym.get(i));
			// 绑定词条id参数
			lstpara.add(stdwordid);
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
			lstlstpara.add(MyUtil.LogParam(" ", " ", "增加别名", "上海", curwordclass
					+ "==>" + curworditem + "==>" + lstSynonym.get(i), "WORD"));
		}
		
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
		
		// 执行SQL语句，绑定事务处理，返回事务处理结果
		return Database.executeNonQueryTransaction(lstsql, lstlstpara);
	}

	/**
	 * 删除别名
	 * @param stdwordid参数别名id
	 * @param synonym参数别名名称
	 * @param curworditem参数词条名称
	 * @param curwordclass参数词类名称
	 * @return 删除返回的json串
	 */
	public static Object delete(String stdwordid, String synonym,
			String curworditem, String curwordclass) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
//		// 定义多条SQL语句集合
//		List<String> lstsql = new ArrayList<String>();
//		// 定义多条SQL语句对应的绑定参数集合
//		List<List<?>> lstlstpara = new ArrayList<List<?>>();
//		// 定义SQL语句
//		StringBuilder sql = new StringBuilder();
//		// 定义删除别名的SQL语句
//		sql.append("delete from word where wordid in (");
//		// 定义绑定参数集合
//		List<String> lstpara = new ArrayList<String>();
//		// 将别名id按照逗号拆分
//		String[] ids = stdwordid.split(",");
//		// 循环遍历id数组
//		for (int i = 0; i < ids.length; i++) {
//			if (i != ids.length - 1) {
//				// 除了最后一个不加逗号，其他加上逗号
//				sql.append("?,");
//			} else {
//				// 最后一个加上右括号，将SQL语句补充完整
//				sql.append("?)");
//			}
//			// 绑定参数集合
//			lstpara.add(ids[i]);
//		}
//		// 将SQL语句放入集合中
//		lstsql.add(sql.toString());
//		// 将对应的绑定参数集合放入集合中
//		lstlstpara.add(lstpara);
//		
//		// 更新当前词类编辑时间
//		String sql_update = "update wordclass set time =sysdate  where  wordclass = ? ";
//		// 定义绑定参数集合
//		lstpara = new ArrayList<String>();
//		// 绑定词条参数
//		lstpara.add(curwordclass);
//		// 将SQL语句放入集合中
//		lstsql.add(sql_update);
//		// 将对应的绑定参数集合放入集合中
//		lstlstpara.add(lstpara);
//
//		// 生成操作日志记录
//		// 将别名按照逗号拆分
//		String[] synonyms = synonym.split(",");
//		// 循环遍历别名数组
//		for (int i = 0; i < synonyms.length; i++) {
//			// 将SQL语句放入集合中
//			lstsql.add(MyUtil.LogSql());
//			// 将对应的绑定参数集合放入集合中
//			lstlstpara.add(MyUtil.LogParam(" ", " ", "删除别名", "上海", curwordclass
//					+ "==>" + curworditem + "==>" + synonyms[i], "WORD"));
//		}
//		// 执行SQL语句，绑定事务处理，返回事务处理的结果
//		int c = Database.executeNonQueryTransaction(lstsql, lstlstpara);
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			jsonObj.put("success", true);
			// 将成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "登录超时,请注销后重新登录!");
			return jsonObj;
		}
		User user = (User)sre;
		int c = UserOperResource.deleteSynonyms(user, stdwordid, synonym, curworditem, curwordclass);
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
}