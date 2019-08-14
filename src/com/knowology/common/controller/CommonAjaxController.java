/**  
 * @Project: JfinalDemo 
 * @Title: CommonController.java
 * @Package com.knowology.common.controller
 * @author c_wolf your emai address
 * @date 2014-9-1 下午3:41:00
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.CacheName;
import com.knowology.UtilityOperate.GetConfigValue;
import com.knowology.common.intercepter.CommonCacheInterceptor;
import com.knowology.common.intercepter.CommonEvictInterceptor;
import com.knowology.common.intercepter.OperateLogIntercepter;
import com.knowology.common.intercepter.SessionParaInterceptor;
import com.knowology.common.model.CommonModel;
import com.knowology.common.plugin.SqlQueryPlugin;

/**
 * 内容摘要 ： 这个是作为通用的ajax请求处理的控制器，主要包含增删查改等 类修改者 修改日期 修改说明
 * 
 * @ClassName CommonController
 * @Company: knowology
 * @author c_wolf your emai address
 * @date 2014-9-1 下午3:41:00
 * @version V1.0
 */

public class CommonAjaxController extends Controller {
	// 用来存放响应结果
	JSONObject result = new JSONObject();
	

	/**
	 * 
	 * 方法名称： index 内容摘要：默认路径 修改者名字 修改日期 修改说明
	 * 
	 * @author c_wolf 2014-9-2 void
	 * @throws
	 */
	public void index() {

	}

	/**
	 * 为了兼顾extjs等前端框架，返回数据格式为： {root:[],total:199}
	 * 
	 * 这个方法用于单条sql查询，并将返回结果做了缓存，缓存名字为"ajax_query", 缓存内容为result。键值为，请求路径+参数
	 */
	@SuppressWarnings("unchecked")
	@Before( { SessionParaInterceptor.class, OperateLogIntercepter.class,
			CommonCacheInterceptor.class })
	@CacheName("ajax_query")
	public void queryPages() {
		// 获取请求字符串
		String request = getAttr("request");
		int page = Integer.parseInt(getPara("page"));
		int rows = Integer.parseInt(getPara("rows"));
		// 将字符串转为json对象，使用fastjson
		JSONObject obj = JSONObject.parseObject(request);
		// 后台获取到sqlid和参数,并获取到sql语句
		//String sqlid = obj.getString("sqlid");
		String sqlid ="";
		if(GetConfigValue.isOracle){
			sqlid = obj.getString("sqlid");
			if(GetConfigValue.isToMysql){
				sqlid = obj.getString("sqlid")+"_mysql";////ghj 
			}
		}else if(GetConfigValue.isMySQL){
			sqlid = obj.getString("sqlid")+"_mysql";
		}
		
		Object[] paras = null;
		try {
			paras = obj.getJSONArray("paras").toArray();
		} catch (NullPointerException ne) {
			paras = new Object[] {};
		}
		Map sqlmap = SqlQueryPlugin.getSql(sqlid);
		String select = sqlmap.get("select").toString();
		String from = sqlmap.get("from").toString();
		int ignor = 0;
		if (obj.containsKey("ignore")) {
			// String sql = select +" "+ from;
			List igs = obj.getJSONArray("ignore");// 获取前台传过来需要忽略的参数
			ignor = igs.size();
			// JSONObject ignore = (JSONObject)
			// sqlmap.get("ignore");//获取sql中可以被忽略的参数名
			from = SqlQueryPlugin.getRealSql(from, igs);
			// from = "from "+newfrom.substring(newfrom.indexOf("from"));
		}
		int sqlPN = Integer.parseInt(sqlmap.get("paramNum").toString());
		int sqlPan = paras.length + ignor;
		if (sqlPN != sqlPan) {
			result.put("state", "fail");
			result.put("message", "参数个数不正确！");
		} else {
			Page pageObj = CommonModel.executeQueryPage(page, rows, select,
					from, paras);
			result.put("total", pageObj.getTotalRow());
			result.put("rows", pageObj.getList());
			setAttr(request, result);
		}
		renderJson(result);
	}

	@SuppressWarnings("unchecked")
	@Before( { SessionParaInterceptor.class, OperateLogIntercepter.class,
			CommonCacheInterceptor.class })
	@CacheName("ajax_query")
	public void query() {
		// 获取请求字符串
		String request = getAttr("request");
		// 将字符串转为json对象，使用fastjson
		JSONObject obj = JSONObject.parseObject(request);
		// 后台获取到sqlid和参数,并获取到sql语句
		//String sqlid = obj.getString("sqlid");
		String sqlid ="";
		if(GetConfigValue.isOracle){
			sqlid = obj.getString("sqlid");
			if(GetConfigValue.isToMysql){
				sqlid = obj.getString("sqlid")+"_mysql";////ghj 
			}
		}else if(GetConfigValue.isMySQL){
			sqlid = obj.getString("sqlid")+"_mysql";
		}

		Object[] paras = null;
		try {
			paras = obj.getJSONArray("paras").toArray();
		} catch (NullPointerException ne) {
			paras = new Object[] {};
		}
		Map sqlmap = SqlQueryPlugin.getSql(sqlid);
		String sql = sqlmap.get("sql").toString();
		int ignor = 0;
		if (obj.containsKey("ignore")) {
			List igs = obj.getJSONArray("ignore");// 获取前台传过来需要忽略的参数
			ignor = igs.size();
			// JSONObject ignore = (JSONObject)
			// sqlmap.get("ignore");//获取sql中可以被忽略的参数名
			sql = SqlQueryPlugin.getRealSql(sql, igs);
		}
		int paramNum = Integer.parseInt(sqlmap.get("paramNum").toString());
		if (paramNum != paras.length + ignor) {
			result.put("state", "fail");
			result.put("message", "参数个数不正确！");
		} else {
			List list = CommonModel.executeQuery(sql, paras);
			result.put("rows", list);
			result.put("state", "success");
			setAttr(request, result);
		}
		renderJson(result);
	}

	/**
	 * 
	 * 方法名称： noquery 内容摘要：该方法用于执行单条的sql，用于update，insert,delete等
	 * 执行完后，需要清空该次操作设计到的表的缓存
	 */
	@SuppressWarnings("unchecked")
	@Before( { SessionParaInterceptor.class, CommonEvictInterceptor.class,
			OperateLogIntercepter.class })
	@CacheName("ajax_query")
	public void noquery() {
		// 获取请求字符串
		String request = getAttr("request");
		// 将字符串转为json对象，使用fastjson
		JSONObject obj = JSONObject.parseObject(request);
		// 后台获取到sqlid和参数,并获取到sql语句
		//String sqlid = obj.getString("sqlid");
		String sqlid ="";
		if(GetConfigValue.isOracle){
			sqlid = obj.getString("sqlid");
			if(GetConfigValue.isToMysql){
				sqlid = obj.getString("sqlid")+"_mysql";////ghj 
			}
		}else if(GetConfigValue.isMySQL){
			sqlid = obj.getString("sqlid")+"_mysql";
		}

		Map sqlmap = SqlQueryPlugin.getSql(sqlid);
		String sql = sqlmap.get("sql").toString();
		int ignor = 0;
		if (obj.containsKey("ignore")) {
			List igs = obj.getJSONArray("ignore");// 获取前台传过来需要忽略的参数
			ignor = igs.size();
			// JSONObject ignore = (JSONObject)
			// sqlmap.get("ignore");//获取sql中可以被忽略的参数名
			sql = SqlQueryPlugin.getRealSql(sql, igs);
		}
		Object[] paras = null;
		try {
			paras = obj.getJSONArray("paras").toArray();
		} catch (NullPointerException ne) {
			paras = new Object[] {};
		}
		int paramNum = Integer.parseInt(sqlmap.get("paramNum").toString());
		if (paramNum != paras.length + ignor) {
			result.put("state", "fail");
			result.put("message", "参数个数不正确！");
		} else {
			Boolean b = CommonModel.executeNonQuery(sql, paras);
			if (b) {
				result.put("state", "success");
			} else {
				result.put("state", "fail");
				result.put("message", "操作失败！");
			}
		}
		renderJson(result);
	}

	// 一个负责清除缓存，一个负责事务回滚
	@SuppressWarnings("unchecked")
	@Before( { SessionParaInterceptor.class, Tx.class,
			CommonEvictInterceptor.class, OperateLogIntercepter.class })
	@CacheName("ajax_query")
	public void trans() {
		// 获取请求字符串
		String request = getAttr("request");
		// 将字符串转为json对象，使用fastjson
		JSONObject obj = JSONObject.parseObject(request);
		// 后台获取到sqlid和参数,并获取到sql语句
		//String sqlid = obj.getString("sqlid");
		String sqlid ="";
		if(GetConfigValue.isOracle){
			sqlid = obj.getString("sqlid");
			if(GetConfigValue.isToMysql){
				sqlid = obj.getString("sqlid")+"_mysql";////ghj 
			}
		}else if(GetConfigValue.isMySQL){
			sqlid = obj.getString("sqlid")+"_mysql";
		}
		
		Map sqlmap = SqlQueryPlugin.getSql(sqlid);
		List sqllist = (List) sqlmap.get("sqllist");
		List parasList = obj.getJSONArray("paras");
		Boolean b = CommonModel.executeTrans(sqllist, parasList);
		if (b) {
			result.put("state", "success");
		} else {
			result.put("state", "fail");
			result.put("message", "操作失败！");
		}
		renderJson(result);
	}

	/**
	 * 删除所有的缓存
	 * 
	 * @内容摘要：
	 * @author c_wolf 2014-10-16 void
	 */
	public void removeCahce() {
		CacheKit.removeAll("ajax_query");
		result.put("state", "success");
		renderJson(result);
	}

	/**
	 * 序列有时候比自增要方便
	 * 
	 * @内容摘要：根据名字获取下一个序列，适用于Oracle，由于mysql中没有序列，所以，我们需要给mysql添加表
	 * 
	 * 
	 * @author c_wolf 2014-10-21 void
	 */
	public void seq_nextval() {
		String seqName = getAttr("seq");
		String sql = "select " + seqName + ".nextval from dual";
		renderJson(Db.findFirst(sql));
	}

	@SuppressWarnings("unchecked")
	public void test() {
		// 获取请求字符串
		String request = getPara("request");
		// 将字符串转为json对象，使用fastjson
		JSONObject obj = JSONObject.parseObject(request);
		// 后台获取到sqlid和参数,并获取到sql语句
		//String sqlid = obj.getString("sqlid");
		String sqlid ="";
		if(GetConfigValue.isOracle){
			sqlid = obj.getString("sqlid");
			if(GetConfigValue.isToMysql){
				sqlid = obj.getString("sqlid")+"_mysql";////ghj 
			}
		}else if(GetConfigValue.isMySQL){
			sqlid = obj.getString("sqlid")+"_mysql";
		}

		Map sqlmap = SqlQueryPlugin.getSql(sqlid);
		String sql = sqlmap.get("sql").toString();
		if (obj.containsKey("ignore")) {
			List igs = obj.getJSONArray("ignore");// 获取前台传过来需要忽略的参数
			// JSONObject ignore = (JSONObject)
			// sqlmap.get("ignore");//获取sql中可以被忽略的参数名
			sql = SqlQueryPlugin.getRealSql(sql, igs);
		}
	}

	public void testlogin() {
		int id = getParaToInt("id");
		HttpSession session = getRequest().getSession();
		JSONObject user = new JSONObject();
		user.put("workerid", id);
		session.setAttribute("user", user);
		renderJson(result);
	}

	public void error() {
		result.put("state", "fail");
		result.put("message", getAttr("errorMsg"));
		renderJson(result);

	}
	/**
	 * mysql中实现seq 1、 drop table if exists sequence; create table sequence (
	 * seq_name VARCHAR(50) NOT NULL, -- 序列名称 current_val INT NOT NULL, --当前值
	 * increment_val INT NOT NULL DEFAULT 1, --步长(跨度) PRIMARY KEY (seq_name) );
	 * 2、模拟实现currval [sql] create function currval(v_seq_name VARCHAR(50))
	 * returns integer begin declare value integer; set value = 0; select
	 * current_value into value from sequence where seq_name = v_seq_name;
	 * return value; end;
	 * 
	 * 3、实现nextval [sql] create function nextval (v_seq_name VARCHAR(50)) return
	 * integer begin update sequence set current_val = current_val +
	 * increment_val where seq_name = v_seq_name; return currval(v_seq_name);
	 * end;
	 * 
	 * 4、[sql]实现自动set值 create function setval(v_seq_name VARCHAR(50), v_new_val
	 * INTEGER) returns integer begin update sequence set current_val =
	 * v_new_val where seq_name = v_seq_name; return currval(seq_name);
	 */
}
