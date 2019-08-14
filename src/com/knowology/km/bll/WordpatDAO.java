package com.knowology.km.bll;



import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibKbDataDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.CommonLibServiceDAO;
import com.knowology.bll.CommonLibWordpatDAO;
import com.knowology.dal.Database;
import com.knowology.km.NLPCallerWS.NLPCaller4WSDelegate;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.common.util.CommonUtils;
import com.knowology.km.entity.CheckInforef;
import com.knowology.km.entity.InsertOrUpdateParam;
import com.knowology.km.util.CheckInput;
import com.knowology.km.util.GetLoadbalancingConfig;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyClass;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.SimpleString;
import com.knowology.km.util.getConfigValue;
import com.knowology.km.util.getServiceClient;
import com.str.NewEquals;

public class WordpatDAO {
	public static Logger logger = Logger.getLogger("querymanage");
	
	/**
	 * 定义全局 city字典
	 */
	public static Map<String, String> cityCodeToCityName = new HashMap<String, String>();
	/**
	 *创建字典
	 */

	static {
		Result r = CommonLibMetafieldmappingDAO.getConfigMinValue("地市编码配置");

		if (r != null && r.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < r.getRowCount(); i++) {
				String key = r.getRows()[i].get("k") == null ? ""
						: r.getRows()[i].get("k").toString();
				String value = r.getRows()[i].get("name") == null ? "" : r
						.getRows()[i].get("name").toString();
				cityCodeToCityName.put(value, key);
			}
		}

	}
	/**
	 * 根据地市id获取地市名称
	 * 
	 * @param cityids参数地市ids
	 * @return 地市名称
	 */
	public static Object GetCitysFromCityids(String cityids) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义SQL语句
		StringBuilder sql = new StringBuilder();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		if( "".equals(cityids) || "null".equals(cityids)||cityids == null){
			jsonObj.clear();
			// 将空字符串放入jsonObj的citys对象中
			jsonObj.put("citys", "");
			return jsonObj;
		}
		try {
			// 将地市id按照逗号拆分
			String[] cityidArr = cityids.split(",");
			// 获取查询地市名称的SQL语句
			sql.append("select city from city where cityid in (");
			// 循环遍历地市id数组
			for (int i = 0; i < cityidArr.length; i++) {
				// 判断是否是最后一个地市id
				if (i != cityidArr.length - 1) {
					// 除了最后一个不加逗号，其他都加逗号
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?)");
				}
				// 绑定地市id参数
				lstpara.add(cityidArr[i]);
			}
			// 执行SQL语句，获取相应的数据源
			Result rs = Database
					.executeQuery(sql.toString(), lstpara.toArray());
			// 定义存放地市名称的集合
			List<Object> cityLst = new ArrayList<Object>();
			// 判断数据源是否为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 将地市名称放入集合中
					cityLst.add(rs.getRows()[i].get("city"));
				}
			}
			// 将地市集合放入到jsonObj的citys对象中
			jsonObj.put("citys", cityLst.toString().replace("[", "").replace(
					"]", "").replace("\"", ""));
		} catch (Exception e) {
			// 出现错误,清空jsonObj
			jsonObj.clear();
			// 将空字符串放入jsonObj的citys对象中
			jsonObj.put("citys", "");
		}
		return jsonObj;
	}

	/**
	 * 分页查询满足条件的模板数据
	 * 
	 * @param param参数对象
	 * @return json串
	 */
	public static Object Select(InsertOrUpdateParam param) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		JSONObject object = QuerymanageDAO.findConfigure();
		List<String> item = (List<String>) object.get("customItem");
		boolean showAuto = false;
		for(String str :item){
			if(str.equals("auto词模=显示")){
				showAuto = true;
			}
		}
		Result  rs = null;
		if(showAuto){
			rs = CommonLibWordpatDAO.getWordpatCount_old(param.brand, param.service, param.kbdataids, param._wordpat);
		}else{
			rs = UserOperResource.getWordpatCount(param.brand, param.service, param.kbdataids, param._wordpat);
		}
			// 判断数据源是否为null,且数量不为0
			if (rs != null && !NewEquals.equals("0",rs.getRowsByIndex()[0][0].toString())) {
				// 将数量放入jsonObj的total对象中
				jsonObj.put("total", rs.getRowsByIndex()[0][0].toString());
				if(showAuto){
					rs = CommonLibWordpatDAO.select_old(param.brand, param.service, param.kbdataids, param._wordpat,param.start, param.limit);
				}else{
					rs = UserOperResource.selectWordpat(param.brand, param.service, param.kbdataids,param._wordpat ,param.start, param.limit);
				}
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					for (int i = 0; i < rs.getRowCount(); i++) {
						String id = String.valueOf((i + 1)
								+ Integer.parseInt(param.start));
						String _wordpat = rs.getRows()[i].get("wordpat") != null ? rs
								.getRows()[i].get("wordpat").toString()
								: "";
						_wordpat = MyUtil.ToString4JSON(_wordpat);
						String city = rs.getRows()[i].get("city") != null ? rs
								.getRows()[i].get("city").toString() : "";
//						String correctratio = rs.getRows()[i]
//								.get("correctratio") != null ? rs.getRows()[i]
//								.get("correctratio").toString() : "";
//						String callnumber = rs.getRows()[i].get("callnumber") != null ? rs
//								.getRows()[i].get("callnumber").toString()
//								: "";
						String autosendswitch = rs.getRows()[i]
								.get("autosendswitch") != null ? rs.getRows()[i]
								.get("autosendswitch").toString()
								: "";
						String wordpatid = rs.getRows()[i].get("wordpatid") != null ? rs
								.getRows()[i].get("wordpatid").toString()
								: "";
						String simplewordpatall = rs.getRows()[i]
								.get("simplewordpat") != null ? rs.getRows()[i]
								.get("simplewordpat").toString() : "";
						String wordpattype = rs.getRows()[i].get("wordpattype") != null ? rs
								.getRows()[i].get("wordpattype").toString()
								: "";
						String simplewordpat = "";
						String issequence = "";
						String returnvalues = "";
						if (simplewordpatall != null
								&& !"".equals(simplewordpatall)
								&& simplewordpatall.length() > 0) {
							simplewordpat = simplewordpatall.split("#")[0]
									.toString().replace("\"", "");
							issequence = simplewordpatall.split("#")[1]
									.toString();
							returnvalues = simplewordpatall.split("#")[2]
									.toString();
						} else {
							simplewordpat = "";
							issequence = "";
							returnvalues = "";
						}
						if (NewEquals.equals("1",autosendswitch)) {
							autosendswitch = "开";
						} else {
							autosendswitch = "关";
						}
						String number = "1";
						if (!"".equals(_wordpat)) {
							String simplewordpatstr = SimpleString
									.worpattosimworpat(_wordpat);
							simplewordpat = simplewordpatstr.split("#")[0];
							issequence = simplewordpatstr.split("#")[1];
							returnvalues = simplewordpatstr.split("#")[2];
						}
						// wordpattype --词模类型，只做页面显示用，类型包括（普通、排除、选择、特征）
						String wordpatTypeName = "";
						if ((simplewordpat.indexOf("-") != -1 && simplewordpat
								.indexOf("*") == -1)
								|| NewEquals.equals("1",wordpattype)) {
							wordpatTypeName = "等于词模";
						} else if (_wordpat.startsWith("~")
								|| NewEquals.equals("2",wordpattype)) {
							wordpatTypeName = "排除词模";
							simplewordpat = simplewordpat.replace("~*", "");
						} else if (_wordpat.startsWith("++")
								|| NewEquals.equals("3",wordpattype)) {
							wordpatTypeName = "选择词模";
							simplewordpat = simplewordpat.replace("++*", "");
						} else if ((_wordpat.startsWith("+") && !_wordpat
								.startsWith("++"))
								|| NewEquals.equals("4",wordpattype)) {
							wordpatTypeName = "特征词模";
							simplewordpat = simplewordpat.replace("+*", "");
						} else if (NewEquals.equals("5",wordpattype)) {
							wordpatTypeName = "自学习词模";
						}else if (NewEquals.equals("6",wordpattype)) {
							wordpatTypeName = "问题库词模";
						}
						else {
							wordpatTypeName = "普通词模";
						}
						// 定义一个json对象
						JSONObject obj = new JSONObject();
						// 生成id对象
						obj.put("id", id);
						// 生成wordpat对象
						obj.put("wordpat", _wordpat);
						// 生成wordpatid对象
						obj.put("wordpatid", wordpatid);
						// 生成correctratio对象
						obj.put("correctratio", "0");
						// 生成callnumber对象
						obj.put("callnumber", "0");
						// 生成autosendswitch对象
						obj.put("autosendswitch", autosendswitch);
						// 生成city对象
						obj.put("city", city);
						// 生成number对象
						obj.put("number", number);
						// 生成simplewordpat对象
						obj.put("simplewordpat", simplewordpat);
						// 生成issequence对象
						obj.put("issequence", issequence);
						// 生成wordpattypename对象
						obj.put("wordpattypename", wordpatTypeName);
						// 生成returnvalues对象
						obj.put("returnvalues", returnvalues);
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
	 * 分页查询满足条件的模板数据
	 * 
	 * @param param参数对象
	 * @return json串
	 */
	public static Object SelectOld(InsertOrUpdateParam param) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义条件的SQL语句
		StringBuilder paramSql = new StringBuilder();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		// 将地市id按照逗号拆分
		String[] cityidArr = param.cityids.split(",");
		// 将摘要id按照逗号拆分
		String[] kbdataidArr = param.kbdataids.split(",");
		// 定义查询的条件的SQL语句
		paramSql
				.append(" from service a,kbdata b,wordpat c,wordpatprecision d where a.serviceid=b.serviceid and b.kbdataid=c.kbdataid and c.wordpatid=d.wordpatid and a.cityid in (");
		// 循环遍历地市id数组
		for (int i = 0; i < cityidArr.length; i++) {
			// 判断是否是最后一个地市id
			if (i != cityidArr.length - 1) {
				// 除了最后一个不加逗号，其他都加逗号
				paramSql.append("?,");
			} else {
				// 最后一个加上右括号，将SQL语句补充完整
				paramSql.append("?) ");
			}
			// 绑定地市id参数
			lstpara.add(cityidArr[i]);
		}
		paramSql
				.append(" and a.brand=? and a.service=? and b.topic=? and b.kbdataid in(");
		// 绑定品牌参数
		lstpara.add(param.brand);
		// 绑定业务名称参数
		lstpara.add(param.service);
		// 绑定主题参数
		lstpara.add(param.topic);
		// 循环遍历摘要id数组
		for (int i = 0; i < kbdataidArr.length; i++) {
			// 判断是否是最后一个摘要id
			if (i != kbdataidArr.length - 1) {
				// 除了最后一个不加逗号，其他都加逗号
				paramSql.append("?,");
			} else {
				// 最后一个加上右括号，将SQL语句补充完整
				paramSql.append("?) ");
			}
			// 绑定摘要id参数
			lstpara.add(kbdataidArr[i]);
		}
		// 判断词模是否为空，null
		if (param._wordpat != null && !"".equals(param._wordpat)
				&& param._wordpat.length() > 0) {
			// 加上词模的查询条件
			paramSql.append(" and c.wordpat like '%'||?||'%' ");
			// 绑定词模参数
			lstpara.add(param._wordpat);
		}
		// 判断correctratiofrom是否为空，null
		if (param.correctratiofrom != null
				&& !"".equals(param.correctratiofrom)
				&& param.correctratiofrom.length() > 0) {
			// 加上correctratiofrom条件
			paramSql.append(" and d.wpprecision>=? ");
			// 绑定correctratiofrom参数
			lstpara.add(param.correctratiofrom);
		}
		// 判断correctratioto是否为空，null
		if (param.correctratioto != null && !"".equals(param.correctratioto)
				&& param.correctratioto.length() > 0) {
			// 加上correctratioto条件
			paramSql.append(" and d.wpprecision<=? ");
			// 绑定correctratioto参数
			lstpara.add(param.correctratioto);
		}
		// 判断callnumberfrom是否为空，null
		if (param.callnumberfrom != null && !"".equals(param.callnumberfrom)
				&& param.callnumberfrom.length() > 0) {
			// 加上callnumberfrom条件
			paramSql.append(" and d.callvolume>=? ");
			// 绑定callnumberfrom参数
			lstpara.add(param.callnumberfrom);
		}
		// 判断callnumberto是否为空，null
		if (param.callnumberto != null && !"".equals(param.callnumberto)
				&& param.callnumberto.length() > 0) {
			// 加上callnumberto条件
			paramSql.append(" and d.callvolume<=? ");
			// 绑定callnumberto参数
			lstpara.add(param.callnumberto);
		}
		// 判断autosendswitch是否为空，null
		if (param.autosendswitch != null && !"".equals(param.autosendswitch)
				&& param.autosendswitch.length() > 0) {
			// 加上autosendswitch条件
			paramSql.append(" and c.autosendswitch=?  ");
			// 判断autosendswitch是否为开
			if ("开".equals(param.autosendswitch)) {
				lstpara.add("1");
			} else {
				lstpara.add("0");
			}
		}

		try {
			// 获取查询满足条件的数量的SQL语句
			String countSql = "select count(*) from (select distinct c.wordpat,c.autosendswitch autosendswitch "
					+ paramSql.toString() + ") t1";
			// 执行SQL语句，获取相应的数据源
			Result rs = Database.executeQuery(countSql, lstpara.toArray());
			// 判断数据源是否为null,且数量不为0
			if (rs != null && !NewEquals.equals("0",rs.getRowsByIndex()[0][0].toString())) {
				// 将数量放入jsonObj的total对象中
				jsonObj.put("total", rs.getRowsByIndex()[0][0].toString());
				// 获取分页查询满足条件的SQL语句
				String sql = "select t2.* from(select t1.*,rownum rn from (select  c.simplewordpat,c.wordpat,c.wordpatid wordpatid,c.city city,d.wpprecision correctratio,d.callvolume callnumber,c.wordpattype wordpattype ,c.autosendswitch autosendswitch "
						+ paramSql.toString()
						+ " )t1)t2 where t2.rn>? and t2.rn<=? ";
				// 获取开始条数参数
				int start = Integer.parseInt(param.start);
				// 获取没有条数参数
				int limit = Integer.parseInt(param.limit);
				// 绑定开始条数参数
				lstpara.add(start);
				// 绑定截止条数参数
				lstpara.add(start + limit);
				// 执行SQL语句，获取相应的数据源
				rs = Database.executeQuery(sql, lstpara.toArray());
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					for (int i = 0; i < rs.getRowCount(); i++) {
						String id = String.valueOf((i + 1)
								+ Integer.parseInt(param.start));
						String _wordpat = rs.getRows()[i].get("wordpat") != null ? rs
								.getRows()[i].get("wordpat").toString()
								: "";
						_wordpat = MyUtil.ToString4JSON(_wordpat);
						String city = rs.getRows()[i].get("city") != null ? rs
								.getRows()[i].get("city").toString() : "";
						String correctratio = rs.getRows()[i]
								.get("correctratio") != null ? rs.getRows()[i]
								.get("correctratio").toString() : "";
						String callnumber = rs.getRows()[i].get("callnumber") != null ? rs
								.getRows()[i].get("callnumber").toString()
								: "";
						String autosendswitch = rs.getRows()[i]
								.get("autosendswitch") != null ? rs.getRows()[i]
								.get("autosendswitch").toString()
								: "";
						String wordpatid = rs.getRows()[i].get("wordpatid") != null ? rs
								.getRows()[i].get("wordpatid").toString()
								: "";
						String simplewordpatall = rs.getRows()[i]
								.get("simplewordpat") != null ? rs.getRows()[i]
								.get("simplewordpat").toString() : "";
						String wordpattype = rs.getRows()[i].get("wordpattype") != null ? rs
								.getRows()[i].get("wordpattype").toString()
								: "";
						String simplewordpat = "";
						String issequence = "";
						String returnvalues = "";
						if (simplewordpatall != null
								&& !"".equals(simplewordpatall)
								&& simplewordpatall.length() > 0) {
							simplewordpat = simplewordpatall.split("#")[0]
									.toString().replace("\"", "");
							issequence = simplewordpatall.split("#")[1]
									.toString();
							returnvalues = simplewordpatall.split("#")[2]
									.toString();
						} else {
							simplewordpat = "";
							issequence = "";
							returnvalues = "";
						}
						if (NewEquals.equals("1",autosendswitch)) {
							autosendswitch = "开";
						} else {
							autosendswitch = "关";
						}
						String number = "1";
						if (!"".equals(_wordpat)) {
							String simplewordpatstr = SimpleString
									.worpattosimworpat(_wordpat);
							simplewordpat = simplewordpatstr.split("#")[0];
							issequence = simplewordpatstr.split("#")[1];
							returnvalues = simplewordpatstr.split("#")[2];
						}
						// wordpattype --词模类型，只做页面显示用，类型包括（普通、排除、选择、特征）
						String wordpatTypeName = "";
						if ((simplewordpat.indexOf("-") != -1 && simplewordpat
								.indexOf("*") == -1)
								|| NewEquals.equals("1",wordpattype)) {
							wordpatTypeName = "等于词模";
						} else if (_wordpat.startsWith("~")
								|| NewEquals.equals("2",wordpattype)) {
							wordpatTypeName = "排除词模";
							simplewordpat = simplewordpat.replace("~*", "");
						} else if (_wordpat.startsWith("++")
								|| NewEquals.equals("3",wordpattype)) {
							wordpatTypeName = "选择词模";
							simplewordpat = simplewordpat.replace("++*", "");
						} else if ((_wordpat.startsWith("+") && !_wordpat
								.startsWith("++"))
								|| NewEquals.equals("4",wordpattype)) {
							wordpatTypeName = "特征词模";
							simplewordpat = simplewordpat.replace("+*", "");
						} else if (NewEquals.equals("5",wordpattype)) {
							wordpatTypeName = "自学习词模";
						} else {
							wordpatTypeName = "普通词模";
						}
						// 定义一个json对象
						JSONObject obj = new JSONObject();
						// 生成id对象
						obj.put("id", id);
						// 生成wordpat对象
						obj.put("wordpat", _wordpat);
						// 生成wordpatid对象
						obj.put("wordpatid", wordpatid);
						// 生成correctratio对象
						obj.put("correctratio", correctratio);
						// 生成callnumber对象
						obj.put("callnumber", callnumber);
						// 生成autosendswitch对象
						obj.put("autosendswitch", autosendswitch);
						// 生成city对象
						obj.put("city", city);
						// 生成number对象
						obj.put("number", number);
						// 生成simplewordpat对象
						obj.put("simplewordpat", simplewordpat);
						// 生成issequence对象
						obj.put("issequence", issequence);
						// 生成wordpattypename对象
						obj.put("wordpattypename", wordpatTypeName);
						// 生成returnvalues对象
						obj.put("returnvalues", returnvalues);
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
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误，将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 清空jsonArr数组
			jsonArr.clear();
			// 将空的jsonArr数组放入jsonObj的root对象中
			jsonObj.put("root", jsonArr);
		}
		return jsonObj;
	}

	/**
	 * 获取咨询渠道
	 * 
	 * @return json串
	 */
	public static Object QueryChannel() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 判断是否进行定制
		if (getConfigValue.isCustom) {
			// 定义一个json对象
			JSONObject obj = new JSONObject();
			// 生成id对象
			obj.put("id", 1);
			// 生成text对象
			obj.put("text", getConfigValue.service);
			// 将生成的对象放入jsonArr数组中
			jsonArr.add(obj);
		} else {
			// 不进行定制，查询配置表的SQL语句
			// 查询配置表的SQL语句
			String sql = "select t1.metafieldid,t1.name from metafield t1,metafieldmapping t2 where t2.metafieldmappingid=t1.metafieldmappingid and t1.stdmetafieldid is null and t2.name=? order by t1.name asc";
			// 定义绑定参数集合
			List<Object> lstpara = new ArrayList<Object>();
			// 绑定参数
			lstpara.add(com.knowology.km.dal.Database.getJDBCValues("AutoGenerateWordpat"));
			try {
				// 执行SQL语句，获取相应的数据源
				Result rs = Database.executeQuery(sql, lstpara.toArray());
				// 判断数据源不为null，且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 循环遍历数据源
					for (int i = 0; i < rs.getRowCount(); i++) {
						// 定义一个json对象
						JSONObject obj = new JSONObject();
						// 生成id对象
						obj.put("id", rs.getRows()[i].get("metafieldid"));
						// 生成text对象
						obj.put("text", rs.getRows()[i].get("name"));
						// 将生成的对象放入jsonArr数组中
						jsonArr.add(obj);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				// 出现错误，清空jsonArr数组
				jsonArr.clear();
			}
		}
		// 将jsonArr数组放入jsonObj的root对象中
		jsonObj.put("root", jsonArr);
		return jsonObj;
	}

	/**
	 * 自动生成词模
	 * 
	 * @param param参数对象
	 * @return 词模
	 */
	public static Object AutoGenerateWordpat(InsertOrUpdateParam param) {
		Object sre = GetSession.getSessionByKey("accessUser");
		 User user = (User)sre;
		 // 获取行业
		 String servicetype = user.getIndustryOrganizationApplication();
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断servicetype为空串、空、null
		if (" ".equals(servicetype) || "".equals(servicetype)
				|| servicetype == null) {
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("result", "登录信息已失效,请注销后重新登录!");
			return jsonObj;
		}
		String url ="";
		String queryCityCode = CommonLibKbDataDAO.getCityByAbstractid(param.kbdataids);
		String provinceCode="全国";
		Map<String, Map<String, String >> provinceToUrl  = GetLoadbalancingConfig.provinceToUrl;
		if ("全国".equals(queryCityCode)||"电渠".equals(queryCityCode)||"集团".equals(queryCityCode) 
				|| "".equals(queryCityCode) || queryCityCode == null) {
			queryCityCode = "全国";
			url = provinceToUrl.get("默认").get("高级分析");
//			url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
		} else {
			queryCityCode = queryCityCode.replace(",", "|");
			provinceCode = queryCityCode.split("\\|")[0];
			provinceCode = provinceCode.substring(0,2)+"0000";
			if("010000".equals(provinceCode)||"000000".equals(provinceCode)){//如何为集团、电渠编码 去默认url
//				url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
				url = provinceToUrl.get("默认").get("高级分析");
			}else{
				String province = GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode);
				if(provinceToUrl.containsKey(province)){
//					url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvinceCode(province);
					url = provinceToUrl.get(province).get("高级分析");
				}else{
					jsonObj.put("result", "ERROR:未找到【"+province+"】高级分析负载均衡服务器!");
					return jsonObj;
				}
			}
			
//			url = "http://222.186.101.212:8282/NLPWebService/NLPCallerWS?wsdl";//ghj Update
//			url = getConfigValue.gaoxidizhi;
		}
		
//		// 获取高级分析的接口串中的serviceInfo
//		String serviceInfo = MyUtil.getServiceInfo(servicetype, "生成词模", "",
//				false);
//		// 获取高级分析接口的入参字符串
//		String queryObject = MyUtil.getDAnalyzeQueryObject("生成词模",
//				param.autowordpat, servicetype, serviceInfo);
		
		// 获取高级分析的接口串中的serviceInfo
		String serviceInfo = MyUtil.getServiceInfo(servicetype, "高级分析", "",
				false,provinceCode);
		// 获取高级分析接口的入参字符串
		String queryObject = MyUtil.getDAnalyzeQueryObject(user.getUserID(),
				param.autowordpat, servicetype, serviceInfo);
		logger.info("生成词模高级分析【"+GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode)+"】接口地址：" + url);
		logger.info("生成词模高级分析接口的输入串：" + queryObject);
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient(url);
		// 判断客户端是否为null
		if (NLPCaller4WSClient == null) {
			// 将无放入jsonObj的result对象中
//			jsonObj.put("result", "无");
			jsonObj.put("result", "ERROR:生成词模高级分析【"+GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode)+"】接口异常。");
			return jsonObj;
		}
		
		String result = "";
		try {
			// 调用接口的方法获取词模
//			result = NLPCaller4WSClient.kAnalyze(queryObject);
			result = NLPCaller4WSClient.detailAnalyze(queryObject);
//			result = FileUtils.readFileToString(new File("C:\\Users\\cwy-pc\\Desktop\\test.txt"), "utf-8");
			logger.info("生成词模高级分析接口的输出串：" + result);
			// 替换掉返回串中的回车符
			result = result.replace("\n", "");
		} catch (Exception e) {
			e.printStackTrace();
			// 将无放入jsonObj的result对象中
			jsonObj.put("result", "ERROR:生成词模高级分析【"+GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode)+"】接口调用失败。");
			return jsonObj;
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 将无放入jsonObj的result对象中
			jsonObj.put("result", "无");
			return jsonObj;
		}
		
		
//		// 将结果转化为json对象
//		JSONObject obj = JSONObject.parseObject(result);
//		// 定义返回值后面的编者
		String autor = "编者=\"" + user.getUserName() + "\"";
//		// 获取词模
//		String wordpat = obj.getString("autoLearnedPat").split("#")[0] + "#"
//				+ autor;
//		String studyWordpat = SimpleString.worpattosimworpat(wordpat);
		/**
		 * 此段代码生成子句条词模 效率差
		// 获取词模 
		String studyWordpat = SimpleString.worpattosimworpat(wordpat) + "$_$";
		// 获取serviceids
		String serviceids = JSONObject.parseObject(serviceInfo).getString(
				"ServiceRootIDs").replace("[", "").replace("]", "").replace(
				"\"", "");
		// 将词模进行分词在组成词模并去重
		List<String> list = quChong(getResult(param.autowordpat, serviceids,
				autor));
		int index = list.size();
		// 取结果中前8个
		if (index > 8) { 
			for (int i = 0; i < 8; i++) {
				studyWordpat += list.get(i) + "$_$";
			}
		} else {
			for (int i = 0; i < index; i++) {
				studyWordpat += list.get(i) + "$_$";
			}
		}
		// 去掉最后的$_$
		studyWordpat = studyWordpat.substring(0, studyWordpat.length() - 3);
		// 将studyWordpat放入jsonObj的result对象中 * */
//		jsonObj.put("result", studyWordpat);
		
		return getResult(result,autor);
	}
	
	/**
	 * 自动生成词模
	 * 
	 * @param param参数对象
	 * @return 词模
	 */
	public static List<String> AutoGenerateOrdinaryWordpat(String question,String cityCode) {
		Object sre = GetSession.getSessionByKey("accessUser");
		 User user = (User)sre;
		 // 获取行业
		 String servicetype = user.getIndustryOrganizationApplication();
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断servicetype为空串、空、null
		if (" ".equals(servicetype) || "".equals(servicetype)
				|| servicetype == null) {
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("result", "登录信息已失效,请注销后重新登录!");
			return new ArrayList<String>();
		}
//		// 获取高级分析的接口串中的serviceInfo
//		String serviceInfo = MyUtil.getServiceInfo(servicetype, "生成词模", "",
//				false);
//		// 获取高级分析接口的入参字符串
//		String queryObject = MyUtil.getDAnalyzeQueryObject("生成词模",
//				param.autowordpat, servicetype, serviceInfo);
		
		// 获取高级分析的接口串中的serviceInfo
		String serviceInfo = MyUtil.getServiceInfo(servicetype, "高级分析", "",
				false,cityCode);
		// 获取高级分析接口的入参字符串
		String queryObject = MyUtil.getDAnalyzeQueryObject(user.getUserID(),
				question, servicetype, serviceInfo);
		logger.info("生成词模高级分析接口的输入串：" + queryObject);
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient();
		
		// 判断客户端是否为null
		if (NLPCaller4WSClient == null) {
			// 将无放入jsonObj的result对象中
			jsonObj.put("result", "无");
			return new ArrayList<String>();
		}
		String result = "";
		try {
			// 调用接口的方法获取词模
//			result = NLPCaller4WSClient.kAnalyze(queryObject);
			result = NLPCaller4WSClient.detailAnalyze(queryObject);
			logger.info("生成词模高级分析接口的输出串：" + result);
			// 替换掉返回串中的回车符
			result = result.replace("\n", "");
		} catch (Exception e) {
			e.printStackTrace();
			// 将无放入jsonObj的result对象中
			jsonObj.put("result", "无");
			return new ArrayList<String>();
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 将无放入jsonObj的result对象中
			jsonObj.put("result", "无");
			return new ArrayList<String>();
		}
		
		
//		// 将结果转化为json对象
//		JSONObject obj = JSONObject.parseObject(result);
//		// 定义返回值后面的编者
		String autor = "编者=\"" + user.getUserName() + "\"";
//		// 获取词模
//		String wordpat = obj.getString("autoLearnedPat").split("#")[0] + "#"
//				+ autor;
//		String studyWordpat = SimpleString.worpattosimworpat(wordpat);
		/**
		 * 此段代码生成子句条词模 效率差
		// 获取词模 
		String studyWordpat = SimpleString.worpattosimworpat(wordpat) + "$_$";
		// 获取serviceids
		String serviceids = JSONObject.parseObject(serviceInfo).getString(
				"ServiceRootIDs").replace("[", "").replace("]", "").replace(
				"\"", "");
		// 将词模进行分词在组成词模并去重
		List<String> list = quChong(getResult(param.autowordpat, serviceids,
				autor));
		int index = list.size();
		// 取结果中前8个
		if (index > 8) { 
			for (int i = 0; i < 8; i++) {
				studyWordpat += list.get(i) + "$_$";
			}
		} else {
			for (int i = 0; i < index; i++) {
				studyWordpat += list.get(i) + "$_$";
			}
		}
		// 去掉最后的$_$
		studyWordpat = studyWordpat.substring(0, studyWordpat.length() - 3);
		// 将studyWordpat放入jsonObj的result对象中 * */
//		jsonObj.put("result", studyWordpat);
		
		return getResult3(result,autor);
	}
	
	/**
	 * 自动生成词模
	 * 
	 * @param param参数对象
	 * @return 词模
	 */
	public static Object AutoGenerateWordpat2(InsertOrUpdateParam param) {
		Object sre = GetSession.getSessionByKey("accessUser");
		 User user = (User)sre;
		 // 获取行业
		 String servicetype = user.getIndustryOrganizationApplication();
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断servicetype为空串、空、null
		if (" ".equals(servicetype) || "".equals(servicetype)
				|| servicetype == null) {
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("result", "登录信息已失效,请注销后重新登录!");
			return jsonObj;
		}
//		// 获取高级分析的接口串中的serviceInfo
//		String serviceInfo = MyUtil.getServiceInfo(servicetype, "生成词模", "",
//				false);
//		// 获取高级分析接口的入参字符串
//		String queryObject = MyUtil.getDAnalyzeQueryObject("生成词模",
//				param.autowordpat, servicetype, serviceInfo);
		
		// 获取高级分析的接口串中的serviceInfo
		String serviceInfo = MyUtil.getServiceInfo(servicetype, "高级分析", "",
				false,"全国");
		// 获取高级分析接口的入参字符串
		String queryObject = MyUtil.getDAnalyzeQueryObject(user.getUserID(),
				param.autowordpat, servicetype, serviceInfo);
		logger.info("生成词模高级分析接口的输入串：" + queryObject);
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient();
		
		// 判断客户端是否为null
		if (NLPCaller4WSClient == null) {
			// 将无放入jsonObj的result对象中
			jsonObj.put("result", "无");
			return jsonObj;
		}
		String result = "";
		try {
			// 调用接口的方法获取词模
//			result = NLPCaller4WSClient.kAnalyze(queryObject);
			result = NLPCaller4WSClient.detailAnalyze(queryObject);
			logger.info("生成词模高级分析接口的输出串：" + result);
			// 替换掉返回串中的回车符
			result = result.replace("\n", "");
		} catch (Exception e) {
			e.printStackTrace();
			// 将无放入jsonObj的result对象中
			jsonObj.put("result", "无");
			return jsonObj;
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 将无放入jsonObj的result对象中
			jsonObj.put("result", "无");
			return jsonObj;
		}
		
		
//		// 将结果转化为json对象
//		JSONObject obj = JSONObject.parseObject(result);
//		// 定义返回值后面的编者
		String autor = "编者=\"" + user.getUserName() + "\"";
//		// 获取词模
//		String wordpat = obj.getString("autoLearnedPat").split("#")[0] + "#"
//				+ autor;
//		String studyWordpat = SimpleString.worpattosimworpat(wordpat);
		/**
		 * 此段代码生成子句条词模 效率差
		// 获取词模 
		String studyWordpat = SimpleString.worpattosimworpat(wordpat) + "$_$";
		// 获取serviceids
		String serviceids = JSONObject.parseObject(serviceInfo).getString(
				"ServiceRootIDs").replace("[", "").replace("]", "").replace(
				"\"", "");
		// 将词模进行分词在组成词模并去重
		List<String> list = quChong(getResult(param.autowordpat, serviceids,
				autor));
		int index = list.size();
		// 取结果中前8个
		if (index > 8) { 
			for (int i = 0; i < 8; i++) {
				studyWordpat += list.get(i) + "$_$";
			}
		} else {
			for (int i = 0; i < index; i++) {
				studyWordpat += list.get(i) + "$_$";
			}
		}
		// 去掉最后的$_$
		studyWordpat = studyWordpat.substring(0, studyWordpat.length() - 3);
		// 将studyWordpat放入jsonObj的result对象中 * */
//		jsonObj.put("result", studyWordpat);
		
		return getResult2(result,autor);
	}
	
	/**
	 * 获得高级分析接口分词结果
	 *@param result
	 *@return 
	 *@returnType Object 
	 *@dateTime 2017-9-1下午03:23:00
	 */
	public static Object getResult(String result,String autor){
		 String rs ="";
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Map<String,String> map = new HashMap<String,String>();
		try {
			// 将接口返回的json串,反序列化为json数组
			JSONArray jsonArray = JSONArray.parseArray(result);
			// 循环遍历jsonArray数组
			for (int i = 0; i < jsonArray.size(); i++) {
				// 将jsonArray数组中的第i个转换成json对象
				JSONObject obj = JSONObject.parseObject(jsonArray.get(i)
						.toString());
				// 得到多个分词的json串
				// 定义分词的json数组
				JSONArray allSegments = new JSONArray();
				// 将obj对象中key为AllSegments的value变成json数组
				JSONArray allSegmentsArray = obj.getJSONArray("allSegments");
				// 遍历循环arrayAllSegments数组
				for (int j = 0; j < allSegmentsArray.size(); j++) {
					// 获取arrayAllSegments数组中的每一个值
					String segments = allSegmentsArray.getString(j);
					// 判断分词是否含有..( 和 nlp版本信息
					if (!segments.contains("...(") && !segments.startsWith("NLU-Version")) {
						// 根据分词得到分词数
						String wordnum = segments.split("\\)  \\(")[1].replace(
								" words)", "");
						// 得到分词的内容
						String word = segments.split("\\)  \\(")[0] + ")";
						map.put(word, wordnum);
					}
				}
				
				 List<String> list = getAutoWordpat(map,autor);
				 List<String> rsList = new ArrayList<String>(); //正常的词模
				 List<String> erList = new ArrayList<String>(); //分词出的词模
				 Set<String> oovList = new HashSet<String>(); //出错的分词
				 if(list.size()>0){
					 for(String str : list){//检查词模处理信息
						 if(str != null && str.startsWith("ERROR")){//分词出的词模
							 erList.add(str.replaceFirst("ERROR##", ""));
						 }else if(str != null && str.startsWith("OOV")){//出错的分词
							 oovList.add(str.replaceFirst("OOV##", ""));
						 }else{//正常的词模
							 rsList.add(str);
						 }
					 }
					 if(!oovList.isEmpty()){ //如果词模有OOV问题，则提示
						 jsonObj.put("oovWord", StringUtils.join(oovList.toArray(), "$_$"));
//						 rsList.addAll(erList);
//						 rs = StringUtils.join(rsList.toArray(), "$_$");
					 }
//					 else{//返回正确的词模
//						 rs = StringUtils.join(rsList.toArray(), "$_$"); 
//					 }
					 rsList.addAll(erList);
					 rs = StringUtils.join(rsList.toArray(), "$_$");
				 }else{
					 rs ="无";
				 }
			}
			// 将jsonArr放入jsonObj的result对象中
			jsonObj.put("result", rs);
		} catch (Exception e) {
			e.printStackTrace();
			// 将返回结果解析失败放入jsonObj的result对象中
			jsonObj.put("result", "无");
		}
		return jsonObj;
	}
	
	/**
	 * 获得高级分析接口分词结果
	 *@param result
	 *@return 
	 *@returnType Object 
	 *@dateTime 2017-9-1下午03:23:00
	 */
	public static  List<String>  getResult3(String result,String autor){
		 String rs ="";
		// 定义返回的json串
		 List<String> list  = new ArrayList<String>();
		Map<String,String> map = new HashMap<String,String>();
		try {
			// 将接口返回的json串,反序列化为json数组
			JSONArray jsonArray = JSONArray.parseArray(result);
			// 循环遍历jsonArray数组
			for (int i = 0; i < jsonArray.size(); i++) {
				// 将jsonArray数组中的第i个转换成json对象
				JSONObject obj = JSONObject.parseObject(jsonArray.get(i)
						.toString());
				// 得到多个分词的json串
				// 定义分词的json数组
				JSONArray allSegments = new JSONArray();
				// 将obj对象中key为AllSegments的value变成json数组
				JSONArray allSegmentsArray = obj.getJSONArray("allSegments");
				// 遍历循环arrayAllSegments数组
				for (int j = 0; j < allSegmentsArray.size(); j++) {
					// 获取arrayAllSegments数组中的每一个值
					String segments = allSegmentsArray.getString(j);
					// 判断分词是否含有..(
					if (!segments.contains("...(")) {
						// 根据分词得到分词数
						String wordnum = segments.split("\\)  \\(")[1].replace(
								" words)", "");
						// 得到分词的内容
						String word = segments.split("\\)  \\(")[0] + ")";
						map.put(word, wordnum);
					}
				}
				
			  list = getAutoWordpat3(map,autor);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	/**
	 * 获得高级分析接口分词结果
	 *@param result
	 *@return 
	 *@returnType Object 
	 *@dateTime 2017-9-1下午03:23:00
	 */
	public static Object getResult2(String result,String autor){
		 String rs ="";
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Map<String,String> map = new HashMap<String,String>();
		try {
			// 将接口返回的json串,反序列化为json数组
			JSONArray jsonArray = JSONArray.parseArray(result);
			// 循环遍历jsonArray数组
			for (int i = 0; i < jsonArray.size(); i++) {
				// 将jsonArray数组中的第i个转换成json对象
				JSONObject obj = JSONObject.parseObject(jsonArray.get(i)
						.toString());
				// 得到多个分词的json串
				// 定义分词的json数组
				JSONArray allSegments = new JSONArray();
				// 将obj对象中key为AllSegments的value变成json数组
				JSONArray allSegmentsArray = obj.getJSONArray("allSegments");
				// 遍历循环arrayAllSegments数组
				for (int j = 0; j < allSegmentsArray.size(); j++) {
					// 获取arrayAllSegments数组中的每一个值
					String segments = allSegmentsArray.getString(j);
					// 判断分词是否含有..(
					if (!segments.contains("...(")) {
						// 根据分词得到分词数
						String wordnum = segments.split("\\)  \\(")[1].replace(
								" words)", "");
						// 得到分词的内容
						String word = segments.split("\\)  \\(")[0] + ")";
						map.put(word, wordnum);
					}
				}
				
				 List<String> list = getAutoWordpat2(map,autor);
				 if(list.size()>0){
					 rs = StringUtils.join(list.toArray(), "$_$"); 
				 }
				  
			}
			// 将jsonArr放入jsonObj的result对象中
			jsonObj.put("result", rs);
		} catch (Exception e) {
			e.printStackTrace();
			// 将返回结果解析失败放入jsonObj的result对象中
			jsonObj.put("result", "无");
		}
		return jsonObj;
	}
	
	/**
	 * 
	 * @param map
	 * @return
	 * @returnType String
	 * @dateTime 2017-9-1下午03:24:06
	 */
	public static List<String> getAutoWordpat(Map<String, String> map,String autor) {
		List<String> list = new ArrayList<String>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			String word = entry.getKey().replace(" ", "##");
			String wordArray[] = word.split("##");
			String wordpat = "";
			int flag = 0;// 词模处理结果 0 可用 1 分词中没有近类和父类 2 分词中包含OOV
			String _word = "";// 具体分词
			for (int i = 0; i < wordArray.length; i++) {
				String tempWord = wordArray[i];
				_word = tempWord.split("\\(")[0];
				if (!"".equals(tempWord) && !"".equals(_word.trim())) {//分词本身不能为空
					String dealWrod = dealWrod(tempWord);
					if (dealWrod == null) {
						flag = 1;
						//页面展示： word(OOV)
						dealWrod = _word+"(OOV)";
						//记录当前词模中的OOV分词
						list.add("OOV##"+dealWrod);
					}
					String _tempWord = "<" + dealWrod + ">";
					wordpat = wordpat + _tempWord + "*";
				}
			}
			wordpat = wordpat.substring(0, wordpat.lastIndexOf("*"))+ "@2#" + autor;
			wordpat = SimpleString.worpattosimworpat(wordpat);
			String newWordpat = wordpat.replace("近类", "");
			if(flag == 0){
				list.add(newWordpat + "@_@" + wordpat);
			}else{
				list.add("ERROR##"+newWordpat + "@_@" + wordpat);
			}
		}
		return list;
	}
	
	/**
	 *
	 *@param map
	 *@return 
	 *@returnType String 
	 *@dateTime 2017-9-1下午03:24:06
	 */
	public static List<String>  getAutoWordpat3(Map<String,String> map,String autor){
		List<String> list = new ArrayList();
		for (Map.Entry<String, String> entry : map.entrySet()) {
	      String word =entry.getKey().replace(" ", "##");
	      String wordArray[]  = word.split("##");
	      String wordpat = "";
	      for(int i = 0;i<wordArray.length;i++){
	    	 String tempWord  = wordArray[i];
	    	 if(!"".equals(tempWord)){
	    		String _tempWord = "<"+dealWrod(tempWord)+">";
	    		wordpat = wordpat+_tempWord+"*";
	    	 }
	      }
	      wordpat = wordpat.substring(0,wordpat.lastIndexOf("*"))+"@2#"+autor;
	      list.add(wordpat);
	   }
		return list;
	}
	
	/**
	 *
	 *@param map
	 *@return 
	 *@returnType String 
	 *@dateTime 2017-9-1下午03:24:06
	 */
	public static List<String>  getAutoWordpat2(Map<String,String> map,String autor){
		List<String> list = new ArrayList();
		for (Map.Entry<String, String> entry : map.entrySet()) {
	      String word =entry.getKey().replace(" ", "##");
	      String wordArray[]  = word.split("##");
	      String wordpat = "";
	      for(int i = 0;i<wordArray.length;i++){
	    	 String tempWord  = wordArray[i];
	    	 if(!"".equals(tempWord)){
	    		String _tempWord = "<"+dealWrod(tempWord)+">";
	    		wordpat = wordpat+_tempWord+"*";
	    	 }
	      }
	      wordpat = wordpat.substring(0,wordpat.lastIndexOf("*"))+"@2#"+autor;
//	      wordpat = SimpleString.worpattosimworpat(wordpat);
//	      String newWordpat = wordpat.replace("近类", "");
	      list.add(wordpat+"@_@"+wordpat);
	   }
		return list;
	}
	
	public static String dealWrod(String word ){
		String _word = word.split("\\(")[0];
		String tempWord = word.split("\\(")[1].split("\\)")[0];
		String wordArray[] = tempWord.split("\\|");
		String newWord="";
		if(tempWord.contains("近类")&&tempWord.contains("父类")){
			for(int i =0;i<wordArray.length;i++){
				String w = wordArray[i];
				if(!w.endsWith("父类")&&!w.equals("模板词")&&!w.endsWith("词类")){
					newWord = newWord+w+"|";
				}
			}
		}else{
			for(int i =0;i<wordArray.length;i++){
				String w = wordArray[i];
				if(!w.equals("模板词")&&!w.endsWith("词类")){
					newWord = newWord+w+"|";
				}
			}
		}
		if(tempWord.equals("OOV")){//如果分词中存在OOV直接过滤
			return null;
		}
		if(newWord.contains("|")){
			newWord = newWord.substring(0,newWord.lastIndexOf("|"));
			return newWord;
		}
		return null;
	}
	
	/**
	 * 将问题进行分词，组成词模集合
	 * 
	 * @param query参数问题
	 * @param serviceids参数业务ids
	 * @param autor参数编者
	 * @return 词模集合
	 */
	public static List<String> getResult(String query, String serviceids,
			String autor) {
		// 定义渠道
		String channel = "短信";
		// 获取登录是的四层服务
		String business = MyClass.IndustryOrganizationApplication();
		// 定义存放问题的集合
		List<String> list = new ArrayList<String>();
		// 将问题放入集合中
		list.add(query);
		// 获取词模的集合
		List<String> wordpatList = CommonUtils.getWordpatList(list, channel,
				business, serviceids, autor);
		return wordpatList;
	}

	/**
	 * 相连两条子句或词类去重
	 * 
	 * @param list参数集合
	 * @return 集合
	 */
	public static List<String> quChong(List<String> list) {
		List<String> ls = new ArrayList<String>();
		String retunValue = "";
		String[] wordpatContentArray;
		for (int i = 0; i < list.size(); i++) {
			if (i == 0) {
				retunValue = "#" + list.get(i).split("#")[1] + "#"
						+ list.get(i).split("#")[2];
			}
			String branch = "";
			String newWordpat = "";
			wordpatContentArray = list.get(i).split("#")[0].split("\\*");
			for (int k = 0; k < wordpatContentArray.length; k++) {
				if (wordpatContentArray[k].equals(branch)) {
					continue;
				} else {
					newWordpat += wordpatContentArray[k] + "*";
				}
				branch = wordpatContentArray[k];
			}
			newWordpat = newWordpat.substring(0, newWordpat.length() - 1)
					+ retunValue;
			if (!ls.contains(newWordpat)) {
				ls.add(newWordpat);
			}
		}
		return ls;
	}

	/**
	 * 词模转简单词模
	 * 
	 * @param wordpat参数词模
	 * @return 简单词模
	 */
	public static String worpattosimwordpat(String wordpat) {
		StringBuilder sb = new StringBuilder();
		String wordpatcontentstr = wordpat.split("@")[0];
		String wordpatsequencestr = wordpat.split("@")[1].split("#")[0];
		@SuppressWarnings("unused")
		String wordpatreturnvalue = wordpat.split("#")[1];
		if (wordpatcontentstr.indexOf("><") != -1) {
			wordpatcontentstr = wordpatcontentstr.replace("><", ">-<");
		}
		if (wordpatcontentstr.indexOf("]<") != -1) {
			wordpatcontentstr = wordpatcontentstr.replace("]<", "]-<");
		}
		if (wordpatcontentstr.indexOf(">[") != -1) {
			wordpatcontentstr = wordpatcontentstr.replace(">[", ">-[");
		}
		if (wordpatcontentstr.indexOf("][") != -1) {
			wordpatcontentstr = wordpatcontentstr.replace("][", "]-[");
		}
		if (wordpatcontentstr.indexOf('~') != -1) {
			wordpatcontentstr = wordpatcontentstr.replace("~", "~ ");
			wordpatcontentstr = wordpatcontentstr.replace("!", "").replace("<",
					"").replace(">", " ");
			if (wordpatcontentstr.indexOf("*") != -1) {
				wordpatcontentstr = wordpatcontentstr.replace(" *", "*");
				// wordpatcontentstr = wordpatcontentstr.replace("*", " ");
			}
		}
		if (wordpatcontentstr.indexOf('+') != -1) {
			wordpatcontentstr = wordpatcontentstr.replace("+", "+ ");
			wordpatcontentstr = wordpatcontentstr.replace("!", "").replace("<",
					"").replace(">", " ");
			if (wordpatcontentstr.indexOf("*") != -1) {
				wordpatcontentstr = wordpatcontentstr.replace(" *", "*");
				// wordpatcontentstr = wordpatcontentstr.replace("*", " ");
			}
		} else {
			wordpatcontentstr = wordpatcontentstr.replace("!", "").replace("<",
					"").replace(">", " ");
			if (wordpatcontentstr.indexOf("*") != -1) {
				wordpatcontentstr = wordpatcontentstr.replace(" *", "*");
				// wordpatcontentstr = wordpatcontentstr.replace("*", " ");
			}
		}
		wordpatcontentstr = wordpatcontentstr.replace(" ]", "]").replace(" -",
				"-").replace(" #", "#");
		sb.append(wordpatcontentstr);
		if ("1".equals(wordpatsequencestr) ||NewEquals.equals("1",wordpatsequencestr)) {
			sb.append("#有序#");
		} else {
			sb.append("#无序#");
		}
		sb.append("编者=\"自学习\"");

		return sb.toString().replace(" ", "");
	}

	/**
	 * 更新词模
	 * 
	 * @param param参数对象
	 * @param request参数request请求
	 * @return json串
	 */
	public static Object UpdateWordpat(InsertOrUpdateParam param,
			HttpServletRequest request,String operationType,String resourceid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			// 将0放入jsonObj的total对象中
			jsonObj.put("checkInfo", "登录超时，请注销后重新登录");
			return jsonObj;
		}
		User user = (User)sre;
	 if(!CommonLibPermissionDAO.isHaveOperationPermission(user.getUserID(), "wordpat", resourceid, operationType, param.wordpatid)){
		jsonObj.put("checkInfo", "无相应操作权限，请联系系统管理员！");
		return jsonObj;
	}
	
		
		// 将进度词模转换为词模
		String a_wordpat = SimpleString.SimpleWordPatToWordPat(param);
		// 判断是否含有checkInfo
		if (a_wordpat.indexOf("checkInfo") != -1) {
			// 将信息放入jsonObj的checkInfo对象中
			jsonObj.put("checkInfo", a_wordpat.split("=>")[1]);
			return jsonObj;
		} else {
			// 将词模赋值给对象的_wordpat属性
			param._wordpat = a_wordpat;
			// 调用Update返回json串
			return Update(param, request);
		}
	}

	/**
	 * 修改词模
	 * 
	 * @param param参数对象
	 * @param request参数request请求
	 * @return json串
	 */
	public static Object Update(InsertOrUpdateParam param,
			HttpServletRequest request) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 合法性检查
		// 获取Web服务器上指定的虚拟路径对应的物理文件路径
		String path = request.getSession().getServletContext().getRealPath("/");
		String pattern = param._wordpat;
		// 词模检查结果字符串
		CheckInforef curcheckInfo = new CheckInforef();
		try {
			// 调用词模检查函数,若出错则显示提示信息
			if (!CheckInput.CheckGrammer(path, pattern, 0, curcheckInfo)) {
				// 将错误信息放入jsonObj的checkInfo对象中
				jsonObj.put("checkInfo", curcheckInfo.curcheckInfo);
				return jsonObj;
			}
		} catch (Exception ex) {
			// 将错误信息放入jsonObj的checkInfo对象中
			jsonObj.put("checkInfo", ex.toString());
			return jsonObj;
		}

//		// 判断该城市下是否已存在相同模板
//		if (ExistsNew(param)) {
//			// 将模板已存在！信息放入jsonObj的checkInfo对象中
//			jsonObj.put("checkInfo", "模板已存在！");
//			return jsonObj;
//		}
		
		// 判断该城市下是否已存在相同模板
		String returninfo = Exists(param);
		if(!"".equals(returninfo)){
			jsonObj.put("checkInfo", "当前模板"+returninfo);
			// 存在，则不更新
			return jsonObj; 
		}
		
		
		// 将更新地市按照逗号拆分
		String[] arycity = param.city.split(",");
		// 转换为集合
		List<String> citysList = new ArrayList<String>();
		// 循环遍历更新地市数组
		for (int i = 0; i < arycity.length; i++) {
			// 将更新地市放入集合中
			citysList.add(arycity[i]);
		}
		// 将原有地市按照逗号拆分
		String[] aryoldcity = param.oldcity.split(",");
		// 转换为集合
		List<String> oldcitysList = new ArrayList<String>();
		// 循环遍历原有地市数组
		for (int i = 0; i < aryoldcity.length; i++) {
			// 将原有地市放入集合中
			oldcitysList.add(aryoldcity[i]);
		}
		// 获取需要插入的地市集合，就是更新地市数组和原有地市数组的差集
		citysList.removeAll(oldcitysList);
		List<String> arycityinsert = citysList;
		// 获取需要更新的地市集合，就是更新地市数组和原有地市数组的交集
		citysList = new ArrayList<String>();
		// 循环遍历更新地市数组
		for (int i = 0; i < arycity.length; i++) {
			// 将更新地市放入集合中
			citysList.add(arycity[i]);
		}
		oldcitysList = new ArrayList<String>();
		// 循环遍历原有地市数组
		for (int i = 0; i < aryoldcity.length; i++) {
			// 将原有地市放入集合中
			oldcitysList.add(aryoldcity[i]);
		}
		citysList.retainAll(oldcitysList);
		List<String> arycityupdate = citysList;
		// 获取需要删除的地市集合，就是原有地市数组和更新地市数组的差集
		citysList = new ArrayList<String>();
		// 循环遍历更新地市数组
		for (int i = 0; i < arycity.length; i++) {
			// 将更新地市放入集合中
			citysList.add(arycity[i]);
		}
		oldcitysList = new ArrayList<String>();
		// 循环遍历原有地市数组
		for (int i = 0; i < aryoldcity.length; i++) {
			// 将原有地市放入集合中
			oldcitysList.add(aryoldcity[i]);
		}
		oldcitysList.removeAll(citysList);
		List<String> arycitydelete = oldcitysList;
		// 执行更新操作，并返回更新结果
//		int c = _update_insert_delete_new(param, arycityinsert, arycityupdate,
//				arycitydelete);
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			// 将0放入jsonObj的total对象中
			jsonObj.put("checkInfo", "登录超时，请注销后重新登录");
			return jsonObj;
		}
		User user = (User)sre;
		int c = UserOperResource.updateByinsertAndelete(user, param.service, param.brand, param.kbdataids, param.wordpatid, param._wordpat, param.autosendswitch, param.wordpattype, param.oldsimplewordpat, param.simplewordpat);
		// 判断返回结果
		if (c > 0) {
			// 将更新成功放入jsonObj的checkInfo对象中
			jsonObj.put("checkInfo", "更新成功！");
		} else {
			// 将更新失败放入jsonObj的checkInfo对象中
			jsonObj.put("checkInfo", "更新失败！");
		}
		return jsonObj;
	}

	/**
	 * 查询模板是否存在
	 * 
	 * @param param参数对象
	 * @return 是否存在
	 */
	public static Boolean ExistsNew(InsertOrUpdateParam param) {
		// 获取模板,并赋值给变量
		String patternStr = param._wordpat;
		// 将模板按照#拆分
		String pattern[] = patternStr.split("#");
		// 获取词模体
		String patternbefore = pattern[0];
		// 将返回值按照&拆分，获取返回值数组
		String returnvalue[] = pattern[1].split("&");
		// 定义SQL语句集合
		StringBuilder sql = new StringBuilder();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		// 将地市按照逗号拆分
		String[] cityArr = param.city.split(",");
		// 将摘要按照逗号拆分
		String[] kbdataidArr = param.kbdataids.split(",");
		// 判断词模是否含有~
		if (patternStr.contains("~")) {
			// 定义SQL语句集合
			sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			sql
					.append("select wordpat from wordpat t where rownum<2 and t.brand=? and t.city in (");
			// 绑定品牌参数
			lstpara.add(param.brand);
			// 循环遍历地市数组
			for (int i = 0; i < cityArr.length; i++) {
				// 判断是否是最后一个地市
				if (i != cityArr.length - 1) {
					// 除了最后一个不加逗号，其他都加逗号
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?) ");
				}
				// 绑定地市参数
				lstpara.add(cityArr[i]);
			}
			// 加上摘要id条件
			sql.append(" and t.kbdataid in (");
			// 循环遍历摘要id数组
			for (int i = 0; i < kbdataidArr.length; i++) {
				// 判断是否是最后一个摘要id
				if (i != kbdataidArr.length - 1) {
					// 除了最后一个不加逗号，其他都加逗号
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?) ");
				}
				// 绑定摘要id参数
				lstpara.add(kbdataidArr[i]);
			}
			// 加上词模查询条件
			sql.append(" and t.wordpat like ? ");
			// 绑定词模参数
			lstpara.add(patternbefore + "%");
		} else {
			// 定义SQL语句集合
			sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			// 定义查询词模的SQL语句
			sql
					.append("select wordpat from wordpat t where rownum<2 and t.brand=? and t.city in (");
			// 绑定品牌参数
			lstpara.add(param.brand);
			// 循环遍历地市数组
			for (int i = 0; i < cityArr.length; i++) {
				// 判断是否是最后一个地市
				if (i != cityArr.length - 1) {
					// 除了最后一个不加逗号，其他都加逗号
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?) ");
				}
				// 绑定地市参数
				lstpara.add(cityArr[i]);
			}
			// 加上词模查询条件
			sql.append(" and t.wordpat like ? ");
			// 绑定词模参数
			lstpara.add(patternbefore + "%");
		}
		// 循环遍历返回值数组
		for (String s : returnvalue) {
			// 判断是否含有编者
			if (s.contains("编者")) {
				// 加上词模的条件
				sql.append(" and t.wordpat like ? ");
				// 加上编者参数
				lstpara.add("%编者%");
				continue;
			}
			// 加上词模的条件
			sql.append(" and t.wordpat like ? ");
			// 加上返回值参数
			lstpara.add("%" + s + "%");
		}
		try {
			// 执行SQL语句，获取相应的数据源
			Result rs = Database
					.executeQuery(sql.toString(), lstpara.toArray());
			// 判断数据源为null或者数据量为0
			if (rs == null || rs.getRowCount() == 0) {
				return false;
			} else {
				// 定义存放模板的集合
				List<String> ls = new ArrayList<String>();
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 获取模板
					String wordpatstr = rs.getRows()[i].get("wordpat")
							.toString();
					// 将模板按照#拆分
					String patternarry[] = wordpatstr.split("#");
					// 获取返回值，并将返回值按照&拆分
					String returnvaluearry[] = patternarry[1].split("&");
					// 判断返回值的数组长度是否相等
					if (returnvalue.length == returnvaluearry.length) {
						// 将当前模板放入集合中
						ls.add(wordpatstr);
					}
				}
				// 判断集合的个数是否大于0
				if (ls.size() > 0) {
					return true;
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误，返回false
			return false;
		}
	}

	
	/**
	 * 查询模板是否存在
	 * 
	 * @param param参数对象
	 * @return 是否存在
	 */
	public static String Exists(InsertOrUpdateParam param) {
		String checkInfo="";
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User)sre;
		
		// 获取行业
		String serviceRoot = "'";
		if(user.getServiceRoot() != null){
			for(int i =0;i<user.getServiceRoot().length;i++){
				if(i == user.getServiceRoot().length -1){
					serviceRoot+=user.getServiceRoot()[i];
				}else{
					serviceRoot+=user.getServiceRoot()[i]+"','";
				}
			}
			serviceRoot+="'";
		}
		
//		String serviceRoot = MyClass.ServiceRoot();
//		// 获取模板,并赋值给变量
		String patternStr = param._wordpat;
//		// 将模板按照#拆分
		String pattern[] = patternStr.split("#");
//		// 获取词模体
//		String patternbefore = pattern[0];
//		// 将返回值按照&拆分，获取返回值数组
		String returnvalue[] = pattern[1].split("&");
//		// 定义SQL语句集合
//		StringBuilder sql = new StringBuilder();
//		// 定义绑定参数集合
//		List<Object> lstpara = new ArrayList<Object>();
//		// 将地市按照逗号拆分
//		String[] cityArr = param.city.split(",");
//		// 将摘要按照逗号拆分
//		String[] kbdataidArr = param.kbdataids.split(",");
//		
//		// 判断词模是否含有~
//		if (patternStr.contains("~")) {
//			// 定义SQL语句集合
//			sql = new StringBuilder();
//			// 定义绑定参数集合
//			lstpara = new ArrayList<Object>();
//			sql
//					.append("select s.service,s.brand,k.topic,k.abstract,t.wordpat from service s,kbdata k,wordpat t where  s.serviceid=k.serviceid and k.kbdataid=t.kbdataid  and rownum<2 and t.brand=? and t.city in (");
//			// 绑定品牌参数
//			lstpara.add(param.brand);
//			// 循环遍历地市数组
//			for (int i = 0; i < cityArr.length; i++) {
//				// 判断是否是最后一个地市
//				if (i != cityArr.length - 1) {
//					// 除了最后一个不加逗号，其他都加逗号
//					sql.append("?,");
//				} else {
//					// 最后一个加上右括号，将SQL语句补充完整
//					sql.append("?) ");
//				}
//				// 绑定地市参数
//				lstpara.add(cityArr[i]);
//			}
//			// 加上摘要id条件
//			sql.append(" and t.kbdataid in (");
//			// 循环遍历摘要id数组
//			for (int i = 0; i < kbdataidArr.length; i++) {
//				// 判断是否是最后一个摘要id
//				if (i != kbdataidArr.length - 1) {
//					// 除了最后一个不加逗号，其他都加逗号
//					sql.append("?,");
//				} else {
//					// 最后一个加上右括号，将SQL语句补充完整
//					sql.append("?) ");
//				}
//				// 绑定摘要id参数
//				lstpara.add(kbdataidArr[i]);
//			}
//			// 加上词模查询条件
//			sql.append(" and t.wordpat like ? ");
//			// 绑定词模参数
//			lstpara.add(patternbefore + "%");
//		} else {
//			// 定义SQL语句集合
//			sql = new StringBuilder();
//			// 定义绑定参数集合
//			lstpara = new ArrayList<Object>();
//			// 定义查询词模的SQL语句
//			sql
//					.append("select s.service,s.brand,k.topic,k.abstract,t.wordpat from service s,kbdata k,wordpat t where  s.serviceid=k.serviceid and k.kbdataid=t.kbdataid   and rownum<2 and t.brand in("+serviceRoot+") and t.city in (");
////			// 绑定品牌参数
////			lstpara.add(param.brand);
//			// 循环遍历地市数组
//			for (int i = 0; i < cityArr.length; i++) {
//				// 判断是否是最后一个地市
//				if (i != cityArr.length - 1) {
//					// 除了最后一个不加逗号，其他都加逗号
//					sql.append("?,");
//				} else {
//					// 最后一个加上右括号，将SQL语句补充完整
//					sql.append("?) ");
//				}
//				// 绑定地市参数
//				lstpara.add(cityArr[i]);
//			}
//			// 加上词模查询条件
//			sql.append(" and t.wordpat like ? ");
//			// 绑定词模参数
//			lstpara.add(patternbefore + "%");
//		}
//		// 循环遍历返回值数组
//		for (String s : returnvalue) {
//			// 判断是否含有编者
//			if (s.contains("编者")) {
//				// 加上词模的条件
//				sql.append(" and t.wordpat like ? ");
//				// 加上编者参数
//				lstpara.add("%编者%");
//				continue;
//			}
//			// 加上词模的条件
//			sql.append(" and t.wordpat like ? ");
//			// 加上返回值参数
//			lstpara.add("%" + s + "%");
//		}

			// 执行SQL语句，获取相应的数据源
			Result  rs = UserOperResource.isExistWordpat(param.brand, param.service, param.kbdataids, param._wordpat, serviceRoot);
			// 判断数据源为null或者数据量为0
			if (rs == null || rs.getRowCount() == 0) {
				return checkInfo;
			} else {
				// 定义存放模板的集合
				List<String> ls = new ArrayList<String>();
				List<String> lservice = new ArrayList<String>();
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 获取模板
					String wordpatstr = rs.getRows()[i].get("wordpat")
							.toString();
					String servicestr = rs.getRows()[i].get("service")
					.toString();
					// 将模板按照#拆分
					String patternarry[] = wordpatstr.split("#");
					// 获取返回值，并将返回值按照&拆分
					String returnvaluearry[] = patternarry[1].split("&");
					// 判断返回值的数组长度是否相等
					if (returnvalue.length == returnvaluearry.length) {
						// 将当前模板放入集合中
						ls.add(wordpatstr);
						lservice.add(servicestr);
					}
				}
				// 判断集合的个数是否大于0
				if (ls.size() > 0) {
					checkInfo = "知识文档：("+lservice.get(0)+") 下已存在";
					return checkInfo;
				} else {
					return checkInfo;
				}
			}
	}
	/**
	 * 更新词模的具体方法
	 * 
	 * @param param参数对象
	 * @param arycityinsert参数需要插入的集合
	 * @param arycityupdate参数需要更新的集合
	 * @param arycitydelete参数需要删除的集合
	 * @return 事务处理结果
	 */
	private static int _update_insert_delete(InsertOrUpdateParam param,
			List<String> arycityinsert, List<String> arycityupdate,
			List<String> arycitydelete) {
		// 定义SQL语句
		StringBuilder sql = new StringBuilder();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		// 定义多条SQL语句
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 将地市id按照逗号拆分
		String[] cityidArr = param.cityids.split(",");
		// 将摘要id按照逗号拆分
		String[] kbdataidArr = param.kbdataids.split(",");
		Result rs = null;
		// 判断需要修改的集合的个数大于0
		if (arycityupdate.size() > 0) {
			// 定义SQL语句
			sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			// 查询摘要id和地市对应关系的SQL语句
			sql
					.append("select a.kbdataid,b.cityid,c.city from kbdata a,service b,city c where a.serviceid=b.serviceid and b.cityid=c.cityid and b.cityid in(");
			// 循环遍历地市id数组
			for (int i = 0; i < cityidArr.length; i++) {
				// 判断是否是最后一个地市id
				if (i != cityidArr.length - 1) {
					// 除了最后一个不加逗号，其他都加逗号
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?) ");
				}
				// 绑定地市参数
				lstpara.add(cityidArr[i]);
			}
			// 加上条件
			sql
					.append(" and b.brand=? and b.service=? and a.topic=? and a.kbdataid in(");
			// 绑定品牌参数
			lstpara.add(param.brand);
			// 绑定业务名称参数
			lstpara.add(param.service);
			// 绑定主题参数
			lstpara.add(param.topic);
			// 循环遍历摘要id数组
			for (int i = 0; i < kbdataidArr.length; i++) {
				// 判断是否是最后一个摘要id
				if (i != kbdataidArr.length - 1) {
					// 除了最后一个不加逗号，其他都加逗号
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?) ");
				}
				// 绑定摘要id参数
				lstpara.add(kbdataidArr[i]);
			}
			// 加上地市名称条件
			sql.append(" and c.city in (");
			// 循环遍历地市数组
			for (int i = 0; i < arycityupdate.size(); i++) {
				// 判断是否是最后一个地市
				if (i != arycityupdate.size() - 1) {
					// 除了最后一个不加逗号，其他都加逗号
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?) ");
				}
				// 绑定地市参数
				lstpara.add(arycityupdate.get(i));
			}
			try {
				// 执行SQL语句，获取相应的数据源
				rs = Database.executeQuery(sql.toString(), lstpara.toArray());
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 循环遍历数据源
					for (int i = 0; i < rs.getRowCount(); i++) {
						// 定义SQL语句
						sql = new StringBuilder();
						// 定义绑定参数集合
						lstpara = new ArrayList<Object>();
						// 定义更新模板的SQL语句
						sql
								.append("update wordpat t set t.simplewordpat=?,t.wordpat=?,t.city=?,t.autosendswitch=?,t.wordpattype=?,t.brand=? where t.kbdataid=? and wordpat=? ");
						// 绑定简单模板参数
						lstpara.add(param.simplewordpat);
						// 绑定模板参数
						lstpara.add(param._wordpat);
						// 绑定地市名称参数
						lstpara.add(rs.getRows()[i].get("city").toString());
						// 绑定autosendswitch参数
						lstpara.add(param.autosendswitch);
						// 绑定词模类型参数
						lstpara.add(param.wordpattype);
						// 绑定品牌参数
						lstpara.add(param.brand);
						// 绑定摘要id参数
						lstpara.add(rs.getRows()[i].get("kbdataid").toString());
						// 绑定旧的模板参数
						lstpara.add(param.oldwordpat);
						// 将SQL语句放入集合中
						lstSql.add(sql.toString());
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 判断需要删除的集合个大于0
		if (arycitydelete.size() > 0) {
			// 定义SQL语句
			sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			// 对应删除模板的SQL语句
			sql
					.append("delete from wordpat t where t.wordpat=? and t.city in (");
			// 绑定旧的模板参数
			lstpara.add(param.oldwordpat);
			// 循环遍历地市数组
			for (int i = 0; i < arycitydelete.size(); i++) {
				// 判断是否是最后一个地市
				if (i != arycitydelete.size() - 1) {
					// 除了最后一个不加逗号，其他都加逗号
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?) ");
				}
				// 绑定地市参数
				lstpara.add(arycitydelete.get(i));
			}
			// 加上条件
			sql
					.append(" and t.kbdataid in (select a.kbdataid from kbdata a,service b where a.serviceid=b.serviceid and b.cityid in (");
			// 循环遍历地市id数组
			for (int i = 0; i < cityidArr.length; i++) {
				// 判断是否是最后一个地市id
				if (i != cityidArr.length - 1) {
					// 除了最后一个不加逗号，其他都加逗号
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?) ");
				}
				// 绑定地市参数
				lstpara.add(cityidArr[i]);
			}
			// 加上条件
			sql
					.append(" and b.brand=? and b.service=? and a.topic=? and a.abstract=?) ");
			// 绑定品牌参数
			lstpara.add(param.brand);
			// 绑定业务名称参数
			lstpara.add(param.service);
			// 绑定主题参数
			lstpara.add(param.topic);
			// 绑定摘要名称参数
			lstpara.add(param._abstract);
			// 将SQL语句放入集合中
			lstSql.add(sql.toString());
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
		}
		// 判断需要插入的集合的个数大于0
		if (arycityinsert.size() > 0) {
			// 定义SQL语句
			sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			// 定义查询摘要id和地市对应关系的SQL语句
			sql
					.append("select a.kbdataid,b.cityid,c.city from kbdata a,service b,city c where a.serviceid=b.serviceid and b.cityid=c.cityid and b.cityid in (");
			// 循环遍历地市id数组
			for (int i = 0; i < cityidArr.length; i++) {
				// 判断是否是最后一个地市id
				if (i != cityidArr.length - 1) {
					// 除了最后一个不加逗号，其他都加逗号
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?) ");
				}
				// 绑定地市参数
				lstpara.add(cityidArr[i]);
			}
			// 加上条件
			sql
					.append(" and b.brand=? and b.service=? and a.topic=? and a.abstract=? and c.city in(");
			// 绑定品牌参数
			lstpara.add(param.brand);
			// 绑定业务名称参数
			lstpara.add(param.service);
			// 绑定主题参数
			lstpara.add(param.topic);
			// 绑定摘要名称参数
			lstpara.add(param._abstract);
			// 循环遍历地市数组
			for (int i = 0; i < arycityinsert.size(); i++) {
				// 判断是否是最后一个地市
				if (i != arycityinsert.size() - 1) {
					// 除了最后一个不加逗号，其他都加逗号
					sql.append("?,");
				} else {
					// 最后一个加上右括号，将SQL语句补充完整
					sql.append("?) ");
				}
				// 绑定地市参数
				lstpara.add(arycityinsert.get(i));
			}
			try {
				// 执行SQL语句，获取相应的数据源
				rs = Database.executeQuery(sql.toString(), lstpara.toArray());
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 循环遍历数据源
					for (int i = 0; i < rs.getRowCount(); i++) {
						// 定义SQL语句
						sql = new StringBuilder();
						// 定义绑定参数集合
						lstpara = new ArrayList<Object>();
						// 获取模板的序列值
						int wordpatid = SeqDAO.GetNextVal("SEQ_WORDPATTERN_ID");
						// 获取地市名称
						String city = rs.getRows()[i].get("city").toString();
						// 定义新增模板的SQL语句
						sql
								.append("insert into wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid) values(?,?,?,?,?,?) ");
						// 绑定模板id参数
						lstpara.add(wordpatid);
						// 绑定模板参数
						lstpara.add(param._wordpat);
						// 绑定地市参数
						lstpara.add(city);
						// 绑定自动开关参数
						lstpara.add(param.autosendswitch);
						// 绑定模板类型参数
						lstpara.add(param.wordpattype);
						// 绑定摘要id参数
						lstpara.add(rs.getRows()[i].get("kbdataid").toString());
						// 将SQL语句放入集合中
						lstSql.add(sql.toString());
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);

						// 定义SQL语句
						sql = new StringBuilder();
						// 定义绑定参数集合
						lstpara = new ArrayList<Object>();
						// 定义新增模板备份表
						sql
								.append("insert into wordpatprecision(wordpatid,city,brand,correctnum,callvolume,wpprecision,autoreplyflag,projectflag) values(?,?,?,0,0,0,0,0)");
						// 绑定模板id参数
						lstpara.add(wordpatid);
						// 绑定地市参数
						lstpara.add(city);
						// 绑定品牌参数
						lstpara.add(param.brand);
						// 将SQL语句放入集合中
						lstSql.add(sql.toString());
						// 将对应的绑定参数集合放入集合中
						lstLstpara.add(lstpara);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 判断是否是通用主题
		if ("通用".equals(param.container)) {
			// 定义SQL语句
			sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			// 通用主题下修改词模，级联修改子句词库中的对应子句条
			sql
					.append("update word set word=? where word=? and wordclassid=(select wordclassid from wordclass where wordclass=? and container=?)");
			// 获取子句词类
			String zijuString = param._abstract.split(">")[1] + "子句";
			// 获取新的子句词条
			String newzijuwordString = param._wordpat.split("@")[0];
			// 获取旧的子句词条
			String oldzijuwordString = param.oldwordpat.split("@")[0];
			// 绑定新的子句词条参数
			lstpara.add(newzijuwordString);
			// 绑定旧的子句词条参数
			lstpara.add(oldzijuwordString);
			// 绑定子句词类
			lstpara.add(zijuString);
			// 绑定词库类型
			lstpara.add("子句");
			// 将SQL语句放入集合中
			lstSql.add(sql.toString());
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
		}

		// 生成操作日志记录
		String _object = " ";
		if (!param.oldcity.equals(param.city)
				&& !param.oldwordpat.equals(param._wordpat)) {
			_object = param.oldsimplewordpat + "==>" + param.simplewordpat
					+ "," + param.oldcity + "==>" + param.city;
		} else if (!param.oldcity.equals(param.city)
				&& param.oldwordpat.equals(param._wordpat)) {
			_object = param.oldcity + "==>" + param.city;
		} else if (param.oldcity.equals(param.city)
				&& !param.oldwordpat.equals(param._wordpat)) {
			_object = param.oldsimplewordpat + "==>" + param.simplewordpat;
		}
		// 将SQL语句放入集合中
		lstSql.add(MyUtil.LogSql());
		// 将对应的绑定参数集合放入集合中
		lstLstpara.add(MyUtil.LogParam(param.brand, param.service, "更新模板",
				param.city.replace("'", ""), _object, "WORDPAT"));
		// 执行SQL语句，绑定事务，返回事务处理结果
		return Database.executeNonQueryTransaction(lstSql, lstLstpara);
	}

	
	
	
	/**
	 * 更新词模的具体方法
	 * 
	 * @param param参数对象
	 * @param arycityinsert参数需要插入的集合
	 * @param arycityupdate参数需要更新的集合
	 * @param arycitydelete参数需要删除的集合
	 * @return 事务处理结果
	 */
	private static int _update_insert_delete_new(InsertOrUpdateParam param,
			List<String> arycityinsert, List<String> arycityupdate,
			List<String> arycitydelete) {
		// 定义SQL语句
		StringBuilder sql = new StringBuilder();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		// 定义多条SQL语句
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		Result rs = null;
	         //删除后添加
			// 定义SQL语句
			sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			// 对应删除模板的SQL语句
			sql
					.append("delete from wordpat t where t.wordpatid= ?");
			// 绑定旧的模板参数
			lstpara.add(param.wordpatid);
			// 将SQL语句放入集合中
			lstSql.add(sql.toString());
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);

			// 定义SQL语句
			sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			// 获取模板的序列值
			int wordpatid = SeqDAO.GetNextVal("SEQ_WORDPATTERN_ID");
			// 定义新增模板的SQL语句
			sql
					.append("insert into wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand) values(?,?,?,?,?,?,?) ");
			// 绑定模板id参数
			lstpara.add(wordpatid);
			// 绑定模板参数
			lstpara.add(param._wordpat);
			// 绑定地市参数
			lstpara.add("上海");
			// 绑定自动开关参数
			lstpara.add(param.autosendswitch);
			// 绑定模板类型参数
			lstpara.add(param.wordpattype);
			// 绑定摘要id参数
			lstpara.add(param.kbdataids);
			lstpara.add(param.brand);
			// 将SQL语句放入集合中
			lstSql.add(sql.toString());
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);

			// 定义SQL语句
			sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			// 定义新增模板备份表
			sql
					.append("insert into wordpatprecision(wordpatid,city,brand,correctnum,callvolume,wpprecision,autoreplyflag,projectflag) values(?,?,?,0,0,0,0,0)");
			// 绑定模板id参数
			lstpara.add(wordpatid);
			// 绑定地市参数
			lstpara.add(param.city);
			// 绑定品牌参数
			lstpara.add(param.brand);
			// 将SQL语句放入集合中
			lstSql.add(sql.toString());
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
		
		// 判断是否是通用主题
		if ("通用".equals(param.container)) {
			// 定义SQL语句
			sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			// 通用主题下修改词模，级联修改子句词库中的对应子句条
			sql
					.append("update word set word=? where word=? and wordclassid=(select wordclassid from wordclass where wordclass=? and container=?)");
			// 获取子句词类
			String zijuString = param._abstract.split(">")[1] + "子句";
			// 获取新的子句词条
			String newzijuwordString = param._wordpat.split("@")[0];
			// 获取旧的子句词条
			String oldzijuwordString = param.oldwordpat.split("@")[0];
			// 绑定新的子句词条参数
			lstpara.add(newzijuwordString);
			// 绑定旧的子句词条参数
			lstpara.add(oldzijuwordString);
			// 绑定子句词类
			lstpara.add(zijuString);
			// 绑定词库类型
			lstpara.add("子句");
			// 将SQL语句放入集合中
			lstSql.add(sql.toString());
			// 将对应的绑定参数集合放入集合中
			lstLstpara.add(lstpara);
		}

		// 生成操作日志记录
		String _object = " ";
		if (!param.oldcity.equals(param.city)
				&& !param.oldwordpat.equals(param._wordpat)) {
			_object = param.oldsimplewordpat + "==>" + param.simplewordpat
					+ "," + param.oldcity + "==>" + param.city;
		} else if (!param.oldcity.equals(param.city)
				&& param.oldwordpat.equals(param._wordpat)) {
			_object = param.oldcity + "==>" + param.city;
		} else if (param.oldcity.equals(param.city)
				&& !param.oldwordpat.equals(param._wordpat)) {
			_object = param.oldsimplewordpat + "==>" + param.simplewordpat;
		}
		// 将SQL语句放入集合中
		lstSql.add(MyUtil.LogSql());
		// 将对应的绑定参数集合放入集合中
		lstLstpara.add(MyUtil.LogParam(param.brand, param.service, "更新模板",
				param.city.replace("'", ""), _object, "WORDPAT"));
		
		// 执行SQL语句，绑定事务，返回事务处理结果
		return Database.executeNonQueryTransaction(lstSql, lstLstpara);
	}

	/**
	 * 删除词模
	 * 
	 * @param param参数对象
	 * @return json串
	 */
	public static Object Delete(InsertOrUpdateParam param,String operationType,String resourceid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		String  checkInfo ="";
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			// 将0放入jsonObj的total对象中
			jsonObj.put("checkInfo", "登录超时，请注销后重新登录");
			return jsonObj;
		}
		User user = (User)sre;
	 if(!CommonLibPermissionDAO.isHaveOperationPermission(user.getUserID(), "wordpat", resourceid, operationType, param.wordpatid)){
		jsonObj.put("checkInfo", "无相应操作权限，请联系系统管理员！");
		return jsonObj;
	}
		int c = UserOperResource.deleteWordpat(user, param.brand, param.service, param.wordpatid, param._wordpat, param.simplewordpat);
		// 判断事务处理结果
		if (c <= 0) {
			checkInfo += "删除失败！";
		}
		if ("".equals(checkInfo)) {
			checkInfo = "删除成功!";
		}
		// 将信息放入jsonObj的checkInfo对象中
		jsonObj.put("checkInfo", checkInfo);
		return jsonObj;
	}
	
	/**
	 * 批量删除词模
	 * 
	 * @param param参数对象
	 * @return json串
	 */
	public static Object BatchDelete(InsertOrUpdateParam param,String operationType,String resourceid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		String  checkInfo ="";
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre==null||"".equals(sre)){
			// 将0放入jsonObj的total对象中
			jsonObj.put("checkInfo", "登录超时，请注销后重新登录");
			return jsonObj;
		}
		User user = (User)sre;
		if(!CommonLibPermissionDAO.isHaveOperationPermission(user.getUserID(), "wordpat", resourceid, operationType, param.wordpatid)){
			jsonObj.put("checkInfo", "无相应操作权限，请联系系统管理员！");
			return jsonObj;
		}
		List<String> wordpatIdList = new ArrayList<String>();
		List<String> wordpatList = new ArrayList<String>();
		List<String> simpleWordpatList = new ArrayList<String>();
		try{
			wordpatIdList = JSONArray.parseArray(param.wordpatid, String.class);
			wordpatList = JSONArray.parseArray(param._wordpat, String.class);
			simpleWordpatList = JSONArray.parseArray(param.simplewordpat, String.class);
		}catch(Exception e){
			e.printStackTrace();
			jsonObj.put("checkInfo", "请求参数错误，请重试！");
			return jsonObj;
		}
		int c = CommonLibWordpatDAO.batchDelete(user, param.brand, param.service, wordpatIdList, wordpatList, simpleWordpatList);
//		int c = UserOperResource.deleteWordpat(user, param.brand, param.service, param.wordpatid, param._wordpat, param.simplewordpat);
		// 判断事务处理结果
		if (c <= 0) {
			checkInfo += "删除失败！";
		}
		if ("".equals(checkInfo)) {
			checkInfo = "删除成功!";
		}
		// 将信息放入jsonObj的checkInfo对象中
		jsonObj.put("checkInfo", checkInfo);
		return jsonObj;
	}

	/**
	 * 新增词模
	 * 
	 * @param param参数对象
	 * @param request参数request请求
	 * @return json串
	 */
	public static Object InsertWordpat(InsertOrUpdateParam param,
			HttpServletRequest request,String operationType,String resourceid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
			if(sre==null||"".equals(sre)){
				// 将0放入jsonObj的total对象中
				jsonObj.put("checkInfo", "登录超时，请注销后重新登录");
				return jsonObj;
			}
			User user = (User)sre;
		if(!CommonLibPermissionDAO.isHaveOperationPermission(user.getUserID(), "wordpat", resourceid, operationType, param.wordpatid)){
			jsonObj.put("checkInfo", "无相应操作权限，请联系系统管理员！");
			return jsonObj;
		}
		
		// 判断进度词模是否为空，null
		if (!"".equals(param.simplewordpat) && param.simplewordpat != null) {
			// 将简单词模按照回车符拆分
			String simplewordpatArry[] = param.simplewordpat.split("\n");
			String s_wordpat = "";
			String checkInfo = "";
			List<String> list = new ArrayList<String>();
			// 循环遍历简单词模数组
			for (int i = 0; i < simplewordpatArry.length; i++) {
				// 判断是否为空
				if ("".equals(simplewordpatArry[i])) {
					continue;
				}
				// 将第i个词模赋值给对象的属性
				param.simplewordpat = simplewordpatArry[i];
				// 将简单词模转化为普通词模，并返回转换结果
				s_wordpat = SimpleString.SimpleWordPatToWordPat(param);
				// 判断转换结果是否含有checkInfo
				if (s_wordpat.indexOf("checkInfo") != -1) {
					// 将信息赋值给checkInfo变量
					checkInfo += "第" + (i + 1) + "条："
							+ s_wordpat.split("=>")[1] + "<br/>";
				} else {
					// 将简单词模和信息放入集合中
					list.add(simplewordpatArry[i] + "<=>" + s_wordpat);
				}
			}
			// 判断checkInfo是否为空
			if ("".equals(checkInfo)) {
				// 将集合赋值给对象的属性
				param.simplewordpatandwordpat = list;
				// 获取补充词模并赋值给对象的属性
				param.simplewordpatandwordpat = WordpatDAO
						.AddReturnValues(param);
				// 新增词模并返回json串
				return WordpatDAO.insert(param, request);
			} else {
				// 将信息放入jsonObj的checkInfo对象中
				jsonObj.put("checkInfo", checkInfo);
			}
		}
		return jsonObj;
	}

	/**
	 * 新增词模的具体方法
	 * 
	 * @param param参数对象
	 * @param request参数request请求
	 * @return json串
	 */
	public static Object insert(InsertOrUpdateParam param,
			HttpServletRequest request) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 合法性检查
		// 获取Web服务器上指定的虚拟路径对应的物理文件路径
		String path = request.getSession().getServletContext().getRealPath("/");
		// 词模检查结果字符串
		String checkInfo = "";
		List<String> patternList = param.simplewordpatandwordpat;
		// 词模检查结果
		Boolean checkflag = true;// 语法检查过程出现异常！
		String pattern = "";
		CheckInforef curcheckInfo = new CheckInforef();
		// 循环添加每条词模
		for (int i = 0; i < patternList.size(); i++) {
			// 递增词模索引
			pattern = patternList.get(i).split("<=>")[1];
			try {
				// 调用词模检查函数
				if (!CheckInput.CheckGrammer(path, pattern, 0, curcheckInfo))
					// 词模有误
					checkflag = false;
			} catch (Exception ex) {
				// 检查过程中出现异常，则报错
				checkflag = false;
				curcheckInfo.curcheckInfo = "模板语法有误！";
			}
			// 判断curcheckInfo
			if (!"".equals(curcheckInfo.curcheckInfo)
					&& (!"没有语法错误".equals(curcheckInfo.curcheckInfo))) {
				checkInfo += "第" + (i + 1) + "条：" + curcheckInfo.curcheckInfo
						+ "<br>";
			}
		}

		// 词模检查失败，则报错
		if (!checkflag) {
			// 将信息放入jsonObj的checkInfo对象中
			jsonObj.put("checkInfo", checkInfo);
			return jsonObj;
		}

		// 循环添加每条词模
		for (int i = 0; i < patternList.size(); i++) {
			// 递增词模索引
			pattern = patternList.get(i);
			param._wordpat = pattern.split("<=>")[1];
			param.simplewordpat = pattern.split("<=>")[0];
			// 判断是否是当前行业主题树
			if ("".equals(param.container)) {
				// 判断该城市下是否已存在相同模板
//				if (ExistsNew(param)) {
//					checkInfo += "第" + (i + 1) + "条模板已存在！" + "<br>";
//					// 存在，则不更新
//					continue;
//				}
				// 判断是否已存在相同模板
				String returninfo = Exists(param);
				if(!"".equals(returninfo)){
					checkInfo += "第" + (i + 1) +"条模板" +returninfo + "<br>";
					// 存在，则不更新
					continue;	
				}
			} else {
				// 如果是当前行业主题树下添加词模，则判断对应子句下是否有相同的子句条即词模体
				if (UserOperResource.isExistZijuWordpat(param._wordpat, param._abstract)) {
					checkInfo += "第" + (i + 1) + "条模板子句条已存在！" + "<br>";
					// 存在，则不更新
					continue;
				}
			}
//			int c = _insert(param);
			Object sre = GetSession.getSessionByKey("accessUser");
			if(sre==null||"".equals(sre)){
				// 将0放入jsonObj的total对象中
				jsonObj.put("checkInfo", "登录超时，请注销后重新登录");
				return jsonObj;
			}
			User user = (User)sre;
			
//			Map<String, String> map = CommonLibWordpatDAO.getKbdataid();
//			if(map.containsKey(param.kbdataids)){
//				if (!CommonLibWordpatDAO.getRetunValueLog(param.kbdataids)) {//判断返回值KEY唯一性
//					Map<String, String> keyValue = CommonLibWordpatDAO.getReturnKeyValue(param._wordpat);
//					for (Map.Entry<String, String> entry : keyValue.entrySet()) {
//						String key = entry.getKey();
//						if(CommonLibWordpatDAO.getRetunValueLogByElementname(key)){
//							checkInfo+="交互返回值["+key+"]已存在，请重新命名!";
//							jsonObj.put("checkInfo", checkInfo);
//							return jsonObj;
//						}
//					}
//				}
//			}
			
			String city = CommonLibKbDataDAO.getCityByAbstractid(param.kbdataids).replace(",", "|");
			if("".equals(city)||"全国".equals(city)){
				List<String> cityList = new ArrayList<String>();
				HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
						.resourseAccess(user.getUserID(), "querymanage", "S");
				// 该操作类型用户能够操作的资源
				cityList = resourseMap.get("地市");
				city = StringUtils.join( cityList.toArray(),"|");
			}
//			int c = UserOperResource.insertWordpat(user, param.service, param.brand, param.kbdataids, param._wordpat, param.simplewordpat, param.wordpattype);
			int c = CommonLibWordpatDAO.insert(user, param.service, param.brand, param.kbdataids, param._wordpat, param.simplewordpat, param.wordpattype,city);
			if (c <= 0) {
				checkInfo += "第" + (i + 1) + "条模板插入失败！" + "<br>";
			}
		}
		if ("".equals(checkInfo)) {
			checkInfo = "插入成功!";
		}
		// 将信息放入jsonObj的checkInfo对象中
		jsonObj.put("checkInfo", checkInfo);
		return jsonObj;
	}

	/**
	 * 新增词模的方法
	 * 
	 * @param param参数对象
	 * @return 事务处理结果
	 */
	private static int _insert(InsertOrUpdateParam param) {
		// 定义SQL语句
		StringBuilder sql = new StringBuilder();
		// 定义绑定参数集合
		List<Object> lstpara = new ArrayList<Object>();
		// 定义多条SQL语句
		List<String> lstSql = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		// 将地市id按照逗号拆分
		String[] cityidArr = param.cityids.split(",");
		// 将地市按照逗号拆分
		String[] cityArr = param.city.split(",");
		// 将摘要id按照逗号拆分
		String[] kbdataidArr = param.kbdataids.split(",");
		String wordpat = param._wordpat;
		// 判断是否在主题摘要下添加词模，如果是则在对应的摘要子句下添加词条
		if (!"".equals(param.container)) {
			// 通用主题下添加词模,将词模体添加到当前“摘要子句”的词条下
			String wordpatString = param._wordpat.split("@")[0].replace("\"",
					"");
			String wordclassName = param._abstract.split(">")[1] + "子句";
			// 定义SQL语句
			sql = new StringBuilder();
			// 定义绑定参数集合
			lstpara = new ArrayList<Object>();
			// 定义新增词条的SQL语句
			sql
					.append("insert into word(wordid,wordclassid,word,type) values(seq_word_id.nextval,(select wordclassid from  wordclass where wordclass=?),?,?)");
			// 绑定词类名称参数
			lstpara.add(wordclassName);
			// 绑定词条名称参数
			lstpara.add(wordpatString);
			// 绑定词条类型参数
			lstpara.add("标准名称");
			// 将SQL语句放入语句集合中
			lstSql.add(sql.toString());
			// 将对应的参数集合放入集合中
			lstLstpara.add(lstpara);

			// 生成添加子句词条操作日志记录
			// 将SQL语句放入语句集合中
			lstSql.add(MyUtil.LogSql());
			// 将对应的参数集合放入集合中
			lstLstpara.add(MyUtil.LogParam(param.brand, param.service,
					"增加子句词条", param.city.replace("'", ""), wordpatString,
					"WORD"));
		}
		// 定义SQL语句
		sql = new StringBuilder();
		// 定义绑定参数集合
		lstpara = new ArrayList<Object>();
		// 查询摘要id、地市id、地市对应关系的SQL语句
		sql
				.append("select a.kbdataid,b.cityid,c.city from kbdata a,service b,city c where a.serviceid=b.serviceid and b.cityid=c.cityid and b.cityid in (");
		// 循环遍历地市id数组
		for (int i = 0; i < cityidArr.length; i++) {
			// 判断是否是最后一个地市id
			if (i != cityidArr.length - 1) {
				// 除了最后一个不加逗号，其他都加逗号
				sql.append("?,");
			} else {
				// 最后一个加上右括号，将SQL语句补充完整
				sql.append("?) ");
			}
			// 绑定地市参数
			lstpara.add(cityidArr[i]);
		}
		// 加上条件
		sql
				.append(" and b.brand=? and b.service=? and a.topic=? and a.kbdataid in(");
		// 绑定品牌参数
		lstpara.add(param.brand);
		// 绑定业务名称参数
		lstpara.add(param.service);
		// 绑定主题参数
		lstpara.add(param.topic);
		// 循环遍历摘要id数组
		for (int i = 0; i < kbdataidArr.length; i++) {
			// 判断是否是最后一个摘要id
			if (i != kbdataidArr.length - 1) {
				// 除了最后一个不加逗号，其他都加逗号
				sql.append("?,");
			} else {
				// 最后一个加上右括号，将SQL语句补充完整
				sql.append("?) ");
			}
			// 绑定摘要id参数
			lstpara.add(kbdataidArr[i]);
		}
		// 加上地市条件
		sql.append(" and c.city in(");
		// 循环遍历地市数组
		for (int i = 0; i < cityArr.length; i++) {
			// 判断是否是最后一个地市
			if (i != cityArr.length - 1) {
				// 除了最后一个不加逗号，其他都加逗号
				sql.append("?,");
			} else {
				// 最后一个加上右括号，将SQL语句补充完整
				sql.append("?) ");
			}
			// 绑定地市参数
			lstpara.add(cityArr[i]);
		}
		try {
			// 执行SQL语句，获取相应的数据源
			Result rs = Database
					.executeQuery(sql.toString(), lstpara.toArray());
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义SQL语句
					sql = new StringBuilder();
					// 定义绑定参数集合
					lstpara = new ArrayList<Object>();
					// 获取词模表的序列值
					int wordpatid = SeqDAO.GetNextVal("SEQ_WORDPATTERN_ID");
					// 获取地市名称
					String city = rs.getRows()[i].get("city").toString();
					// 定义新增模板的SQL语句
					sql
							.append("insert into wordpat(wordpatid,wordpat,city,autosendswitch,wordpattype,kbdataid,brand,edittime,simplewordpat) values(?,?,?,?,?,?,?,sysdate,?)");
					// 绑定模板id参数
					lstpara.add(wordpatid);
					// 绑定模板参数
					lstpara.add(wordpat);
					// 绑定地市参数
					lstpara.add(city);
					// 绑定自动开关参数
					lstpara.add(param.autosendswitch);
					// 绑定模板类型参数
					lstpara.add(param.wordpattype);
					// 绑定摘要id参数
					lstpara.add(rs.getRows()[i].get("kbdataid").toString());
					// 绑定品牌参数
					lstpara.add(param.brand);
					// 绑定简单词模参数
					lstpara.add(param.simplewordpat);
					// 将SQL语句放入集合中
					lstSql.add(sql.toString());
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);

					// 定义SQL语句
					sql = new StringBuilder();
					// 定义绑定参数集合
					lstpara = new ArrayList<Object>();
					// 定义新增模板备份表
					sql
							.append("insert into wordpatprecision(wordpatid,city,brand,correctnum,callvolume,wpprecision,autoreplyflag,projectflag) values(?,?,?,0,0,0,0,0)");
					// 绑定模板id参数
					lstpara.add(wordpatid);
					// 绑定地市参数
					lstpara.add(city);
					// 绑定品牌参数
					lstpara.add(param.brand);
					// 将SQL语句放入集合中
					lstSql.add(sql.toString());
					// 将对应的绑定参数集合放入集合中
					lstLstpara.add(lstpara);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 添加相似问题
		if (!"".equals(param.question) && param.question != null) {
			// 判断当前摘要下相似问题是否存在
			if (!IsQuestionExist(param)) {
				// 定义SQL语句
				sql = new StringBuilder();
				// 定义绑定参数集合
				lstpara = new ArrayList<Object>();
				// 定义新增相似问题的SQL语句
				sql
						.append("insert into similarquestion(kbdataid,kbdata,questionid,question,remark,time,questiontype) values (?,?,?,?,'',sysdate,?)");
				// 绑定摘要id
				lstpara.add(param.kbdataids);
				// 绑定摘要名称
				lstpara.add(param._abstract);
				// 获取相似问题表的序列值并绑定参数
				lstpara.add(SeqDAO.GetNextVal("SIMILARQUESTION_SEQUENCE"));
				// 绑定问题测试
				lstpara.add(param.question);
				// 绑定问题类型参数
				lstpara.add("普通问题");
				// 将SQL语句放入集合中
				lstSql.add(sql.toString());
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(lstpara);

				// 生成添加相似问题操作日志记录
				// 将SQL语句放入集合中
				lstSql.add(MyUtil.LogSql());
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(MyUtil.LogParam(param.brand, param.service,
						"增加相似问题", param.city.replace("'", ""), param.question,
						"SIMILARQUESTION"));
			}
		}
		// 添加简单词模操作日志记录
		lstSql.add(MyUtil.LogSql());
		// 将SQL语句放入集合中
		// 将对应的绑定参数集合放入集合中
		lstLstpara.add(MyUtil.LogParam(param.brand, param.service, "增加模板",
				param.city.replace("'", ""), param.simplewordpat, "WORDPAT"));
		return Database.executeNonQueryTransaction(lstSql, lstLstpara);
	}

	/**
	 * 判断当前摘要下相似问题是否存在
	 * 
	 * @param param参数对象
	 * @return 是否存在
	 */
	public static Boolean IsQuestionExist(InsertOrUpdateParam param) {
		// 定义SQL语句
		String sql = "select * from similarquestion where question=? and kbdataid=?";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定问题参数
		lstpara.add(param.question);
		// 绑定摘要参数
		lstpara.add(param.kbdataids);
		try {
			// 执行SQL语句，获取相应的数据源
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			// 判断简要不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 简单条件词模
	 * 
	 * @param param参数对象
	 * @param request参数request对象
	 * @return json串
	 */
	public static Object SimpleInsertWordpat(InsertOrUpdateParam param,
			HttpServletRequest request) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断词模是否为空，null气人返回值的个数不为0
		if (!"".equals(param._wordpat) && param._wordpat != null
				&& param.returnValues.length != 0) {
			// 合并词模并返回结果
			String wordPatDel = SimpleString.CombineWordPat(param);
			// 判断结果是否含有checkInfo
			if (wordPatDel.indexOf("checkInfo") != -1) {
				// 将结果转化为json对象
				jsonObj = JSONObject.parseObject(wordPatDel);
			} else {
				// 定义集合
				List<String> list = new ArrayList<String>();
				// 将信息放入集合中
				list.add(wordPatDel.split("\\^")[1] + "<=>"
						+ wordPatDel.split("\\^")[0]);
				// 将集合赋值给对象的属性
				param.simplewordpatandwordpat = list;
				// 获取补充词模并赋值给对象的属性
				param.simplewordpatandwordpat = WordpatDAO
						.AddReturnValues(param);
				// m_param._wordpat = wordPatDel.split("\\^")[0];
				// m_param._wordpat = WordpatDAO.AddReturnValues(m_param);
				// m_param.simplewordpat = wordPatDel.split("\\^")[1];
				// m_param.simplewordpat =
				// WordpatDAO.AddSimpleReturnValues(m_param);
				jsonObj = JSONObject.parseObject(WordpatDAO.insert(param,
						request).toString());
			}
		}
		// 添加相似问题
		if (!"".equals(param.question) && param.question != null
				&& "".equals(param._wordpat) && param.returnValues.length == 0) {
			jsonObj = JSONObject.parseObject(WordpatDAO.insertQuestion(param)
					.toString());
		}
		return jsonObj;
	}

	/**
	 * 插入相似问题
	 * 
	 * @param param参数对象
	 * @return json串
	 */
	public static Object insertQuestion(InsertOrUpdateParam param) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断问题是否为空，null
		if (!"".equals(param.question) && param.question != null) {
			// 判断问题是否存在
			if (IsQuestionExist(param)) {
				// 将信息放入jsonObj的checkInfo对象中
				jsonObj.put("checkInfo", "相似问题已存在！");
			} else {
				// 定义多条SQL语句集合
				List<String> lstSql = new ArrayList<String>();
				// 定义多条SQL语句集合对应的绑定参数集合
				List<List<?>> lstLstpara = new ArrayList<List<?>>();
				// 定义新增相似问题的SQL语句
				String sql = "insert into similarquestion(kbdataid,kbdata,questionid,question,remark,time) values (?,?,?,?,'',sysdate)";
				// 定义绑定参数集合
				List<Object> lstpara = new ArrayList<Object>();
				// 绑定摘要id
				lstpara.add(param.kbdataids);
				// 绑定摘要名称参数
				lstpara.add(param._abstract);
				// 获取相似问题表的序列值并绑定参数
				lstpara.add(SeqDAO.GetNextVal("SIMILARQUESTION_SEQUENCE"));
				// 绑定问题参数
				lstpara.add(param.question);
				// 将SQL语句放入集合中
				lstSql.add(sql);
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(lstpara);

				// 生成操作日志记录
				// 将SQL语句放入集合中
				lstSql.add(MyUtil.LogSql());
				// 将对应的绑定参数集合放入集合中
				lstLstpara.add(MyUtil.LogParam(param.brand, param.service,
						"增加相似问题", param.city.replace("'", ""), param.question,
						"SIMILARQUESTION"));

				int c = Database.executeNonQueryTransaction(lstSql, lstLstpara);
				// 判断新增问题是否成功
				if (c > 0) {
					// 将插入成功信息放入jsonObj的checkInfo对象中
					jsonObj.put("checkInfo", "相似问题插入成功！");
				} else {
					// 将插入失败信息放入jsonObj的checkInfo对象中
					jsonObj.put("checkInfo", "相似问题插入失败！");
				}
			}
		}
		return jsonObj;
	}

	/**
	 * 补充交互词模
	 * 
	 * @param param参数对象
	 * @return 集合
	 */
	public static List<String> AddReturnValues(InsertOrUpdateParam param) {
		List<String> list = new ArrayList<String>();
		String simplewordpat = "";
		String wordpat = "";
		List<String> simplewordpatandwordpat = param.simplewordpatandwordpat;
		for (int i = 0; i < simplewordpatandwordpat.size(); i++) {
			simplewordpat = simplewordpatandwordpat.get(i).split("<=>")[0];
			wordpat = simplewordpatandwordpat.get(i).split("<=>")[1];
			if ("".equals(param.container)) {
				if ("flowchart".equals(param.chartaction)
						&& !"".equals(param.queryorresponse)) {
					// &匹配要求=要求相等($摘要$,"《健康顾问场景》健康顾问业务.1_咨询健康顾问（上文摘要）")
					// &补全后咨询="健康顾问介绍（用户选择的摘要名称）"
					if ("应答".equals(param.queryorresponse)) {
						if (!"".equals(param.abs_name)) {
							String pre_abs = "《" + param.service + "》" + "咨询"
									+ param.abs_name;// 上文摘要
							String returnvalues_1 = "&匹配要求=要求相等($摘要$,\""
									+ pre_abs + "\")";
							simplewordpat = simplewordpat + returnvalues_1;
							wordpat = wordpat + returnvalues_1;
						}
						if (!"".equals(param.next_abs_name)) {
							String abs = "咨询" + param.next_abs_name;
							String returnvalues_2 = "&补全后咨询=\"" + "《"
									+ param.service + "》" + abs + "\"";
							simplewordpat = simplewordpat + returnvalues_2;
							wordpat = wordpat + returnvalues_2;
						}
					}
				}
			}
			list.add(simplewordpat + "<=>" + wordpat);
		}
		return list;
	}

	/**
	 * 补充交互词模(简单词模)
	 * 
	 * @param param参数对象
	 * @return 集合
	 */
	public static List<String> AddSimpleReturnValues(InsertOrUpdateParam param) {
		List<String> list = new ArrayList<String>();
		String wordpat = "";
		List<String> wordpatArry = param.simplewordpatArry;
		for (int i = 0; i < wordpatArry.size(); i++) {
			if ("".equals(param.container)) {
				// 判断是否是流程图页面右击添加词模，如果是添加相应的返回值函数
				if ("flowchart".equals(param.chartaction)
						&& !"".equals(param.queryorresponse)) {

					// &匹配要求=要求相等($摘要$,"《健康顾问场景》健康顾问业务.1_咨询健康顾问（上文摘要）")
					// &补全后咨询="健康顾问介绍（用户选择的摘要名称）"
					if ("应答".equals(param.queryorresponse)) {
						if (!"".equals(param.abs_name)) {
							String pre_abs = "《" + param.service + "》" + "咨询"
									+ param.abs_name;// 上文摘要
							String returnvalues_1 = "&匹配要求=要求相等($摘要$,\""
									+ pre_abs + "\")";
							wordpat = wordpatArry.get(i) + returnvalues_1;
						}

						if (!"".equals(param.next_abs_name)) {
							String abs = "咨询" + param.next_abs_name;
							String returnvalues_2 = "&补全后咨询=\"" + "《"
									+ param.service + "》" + abs + "\"";
							wordpat = wordpatArry.get(i) + returnvalues_2;
						}

					}
				}
			}
			list.add(wordpat);
		}
		return list;
	}
	
	public static Object selectWordpatCity(String wordpatid) {
		JSONObject jsonObj = new JSONObject();
		String sql = "";
		// 定义SQL语句
		sql = "select * from wordpat where wordpatid=?";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定id参数
		lstpara.add(wordpatid);
		try {
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			if (rs != null && rs.getRowCount() > 0){
				String cityid = rs.getRows()[0].get("city")==null?"":rs.getRows()[0].get("city").toString().replace("|", ",");
				String cityname = "";
				String[] cityids = cityid.split(",");
				for (int i = 0;i < cityids.length;i++){
					cityname = cityname + cityCodeToCityName.get(cityids[i]) + ",";
				}
				cityname = cityname.substring(0, cityname.lastIndexOf(","));
				jsonObj.put("success", true);
				jsonObj.put("cityid", cityid);
				jsonObj.put("cityname", cityname);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return jsonObj;
	}
	
	
	
	public static Object updateWordpatCity(String wordpatids, String citycode) {
		JSONObject jsonObj = new JSONObject();
		
		if (citycode == null || "".equals(citycode)){
			citycode = "全国";
		}
		String sql = "";
		sql = "update wordpat set city=?,wordpat=(replace(wordpat,'&人工地市=\"是\"','')||'&人工地市=\"是\"') where wordpatid=?";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定city参数
		lstpara.add(citycode);
		// 绑定id参数
		lstpara.add(wordpatids);
		int rs = 0;
		try {
			rs = Database.executeNonQuery(sql, lstpara.toArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (rs > 0){
			jsonObj.put("success", true);
			jsonObj.put("msg", "更新成功!");
		} else {
			jsonObj.put("success", true);
			jsonObj.put("msg", "更新失败!");
		}
		return jsonObj;
	}
	public static Object isShowButton(String kbdataid) {
		JSONObject jsonObj = new JSONObject();
		Map<String,String> map = CommonLibWordpatDAO.getKbdataid();
		if (map.containsKey(kbdataid)){
			jsonObj.put("success", true);
			jsonObj.put("msg", "是");
		} else {
			jsonObj.put("success", true);
			jsonObj.put("msg", "否");
		}
		return jsonObj;
	}
	public static Object getReturnValue(String kbdataid) {
		JSONObject jsonObj = new JSONObject();
		Result rs = CommonLibWordpatDAO.getRetunValue(kbdataid);
		String returnvalue ="";
		if(rs!=null&&rs.getRowCount()>0){
			for(int i=0;i<rs.getRowCount();i++){
				if("".equals(returnvalue)){
					returnvalue = rs.getRows()[i].get("returnvalue").toString();
				}else{
					returnvalue = returnvalue +"&" +rs.getRows()[i].get("returnvalue").toString();
				}
				
			}
		}
		if (!"".equals(returnvalue)){
			jsonObj.put("success", true);
			jsonObj.put("msg", returnvalue);
		} else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "系统还未记录返回值!");
		}
		return jsonObj;
	}
		
	public static Object getInteractiveReturnValue(String kbdataid) {
		JSONObject jsonObj = new JSONObject();
		Map<String, String> map = CommonLibWordpatDAO.getKbdataid();
		if(map.containsKey(kbdataid)){//判断当前摘要补充返回值是否是在识别业务规则下，如果是进行识别业务下补充
			return getReturnValue(kbdataid);
		}else{//反之在商家摘要下补充
			/*
			Result rs = CommonLibWordpatDAO.getScenarios2kbdata(kbdataid);
			String returnvalue ="";
			if(rs!=null&&rs.getRowCount()>0){
				rs = CommonLibWordpatDAO.getInteractiveElements(kbdataid);
				if(rs!=null&&rs.getRowCount()>0){
					for(int i=0;i<rs.getRowCount();i++){
						String tempstr = rs.getRows()[i].get("name").toString()+"=<!"+rs.getRows()[i].get("wordclass").toString()+">";
						if("".equals(returnvalue)){
							returnvalue = tempstr;
						}else{
							returnvalue = returnvalue +"&" +tempstr;
						}
						
					}
				}else{
					jsonObj.put("success", false);
					jsonObj.put("msg", "请创建当前标准问对应场景交互要素!");	
				}
				
			}else{
				jsonObj.put("success", false);
				jsonObj.put("msg", "请创建标准问场景对应关系!");
				
			}
			if (!"".equals(returnvalue)){
				jsonObj.put("success", true);
				jsonObj.put("msg", returnvalue);
			} 
			*/
			jsonObj.put("success", false);
			jsonObj.put("msg", "");
			return jsonObj;
		}
	}

	public static Object getKeys() {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义SQL语句
		String sql = "select distinct(elementname) key from scenariosinteractiveelement";

		try {
			Result rs = Database.executeQuery(sql);
			if (rs != null && rs.getRowCount() > 0){
				SortedMap[] rows = rs.getRows();
				for(SortedMap row : rows) {
					String key = row.get("key").toString();
					// 定义一个json对象
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj.put("id", key);
					// 生成text对象
					obj.put("text", key);
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jsonObj.put("root", jsonArr);
		return jsonObj;
	}

	public static Object getValues(HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义SQL语句
		int type = 0;
		String sql = "";
		String sql1 = "select value as v from scenariosinteractiveelement " +
				"where wordclassid is null and elementname = ?";
		String sql2 = "select word as v from word where wordclassid = ? and stdwordid is null";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定参数
		String elementname = request.getParameter("elementname");
		String wordclassid = request.getParameter("wordclassid");
		if(elementname==null||"".equals(elementname)) {
			if(wordclassid==null||"".equals(wordclassid)) {
				return jsonObj;
			} else {
				sql = sql2;
				lstpara.add(wordclassid);
				type = 1;
			}
		} else {
			sql = sql1;
			lstpara.add(elementname);
		}
		
		try {
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			if (rs != null && rs.getRowCount() > 0){
				SortedMap[] rows = rs.getRows();
				for(SortedMap row : rows) {
					String value = row.get("v").toString();
					if(value.startsWith("<!")&&value.endsWith(">")) {
						continue;
					}
					
					// 定义一个json对象
					JSONObject obj = new JSONObject();				
					switch(type) {
					case 0:
						// 生成id对象
						obj.put("id", value);
						// 生成text对象
						if(value.startsWith("\"")&&value.endsWith("\"")) {
							obj.put("text", value.substring(1, value.length()-1));
						} else {
							obj.put("text", value);
						}
						break;
					case 1:
						obj.put("id", "\"" + value + "\"");
						obj.put("text", value);
					}					
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jsonObj.put("root", jsonArr);
		return jsonObj;
	}
	
	public static Object getWordclasses(HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		// 定义SQL语句
		String sql = "select wordclassid,wordclass from wordclass b " +
				"where exists " +
				"(select 1 from scenariosinteractiveelement a " +
					"where a.wordclassid = b.wordclassid and a.elementname = ?)";
		// 定义绑定参数集合
		List<String> lstpara = new ArrayList<String>();
		// 绑定参数
		String elementname = request.getParameter("elementname");
		if(elementname==null||"".equals(elementname))
			return jsonObj;
		lstpara.add(elementname);
		try {
			Result rs = Database.executeQuery(sql, lstpara.toArray());
			if (rs != null && rs.getRowCount() > 0){
				SortedMap[] rows = rs.getRows();
				for(SortedMap row : rows) {
					String wordclassid = row.get("wordclassid").toString();
					String wordclass = row.get("wordclass").toString();
					// 定义一个json对象
					JSONObject obj = new JSONObject();
					// 生成id对象
					obj.put("id", wordclassid);
					// 生成text对象
					obj.put("text", wordclass);
					// 将生成的对象放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		jsonObj.put("root", jsonArr);
		return jsonObj;
	}

	public static Object transferWordpat(String wordpatid,String operationType, String resourceid, String serviceid, String kbdataid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("success", false);
		
		Object sre = GetSession.getSessionByKey("accessUser");
		if(sre == null|| "".equals(sre)){
			// 将0放入jsonObj的total对象中
			jsonObj.put("checkInfo", "登录超时，请注销后重新登录");
			return jsonObj;
		}
		User user = (User)sre;
		if(!CommonLibPermissionDAO.isHaveOperationPermission(user.getUserID(), "wordpat", resourceid, "D", wordpatid)){
			jsonObj.put("checkInfo", "无删除词模操作权限，请联系系统管理员！");
			return jsonObj;
		}
		//是否有迁移到目的标准问的权限
		if(!CommonLibPermissionDAO.isHaveOperationPermission(user.getUserID(), "querymanage", serviceid, "A",null)){
			jsonObj.put("checkInfo", "无添加词模操作权限，请联系系统管理员！");
			return jsonObj;
		}
		
		// 判断要迁移的目的标准问是否有重复词模
		Result rs = CommonLibWordpatDAO.getWordpatById(wordpatid);
		if(rs == null || rs.getRowCount() < 1){
			jsonObj.put("checkInfo", "该数据不存在，请刷新页面重试。");
			return jsonObj;
		}
		
		String wordpat = Objects.toString(rs.getRows()[0].get("wordpat"), "");
		String wordpatCity = Objects.toString(rs.getRows()[0].get("city"), "");
		rs = CommonLibWordpatDAO.exist(kbdataid, wordpat);
		
		if (rs != null && rs.getRowCount() > 0) {
			jsonObj.put("checkInfo", "该词模在此标准问下已重复，请选择其他数据。");
			return jsonObj;
		}
		
		// 执行迁移，修改词模地市为目的标准问的地市
		String city = CommonLibKbDataDAO.getCityByAbstractid(kbdataid);
		
		int count = CommonLibWordpatDAO.transferWordpat(kbdataid, wordpatid, city);
		if(count > 0){
			jsonObj.put("success", true);
			return jsonObj;
		}
//		if(!StringUtils.equals(wordpatCity, city)){
//		}
//		if("".equals(city)||"全国".equals(city)){
//			List<String> cityList = new ArrayList<String>();
//			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
//					.resourseAccess(user.getUserID(), "querymanage", "S");
//			// 该操作类型用户能够操作的资源
//			cityList = resourseMap.get("地市");
//			city = StringUtils.join( cityList.toArray(),"|");
//		}
		
		return jsonObj;
	}	
}