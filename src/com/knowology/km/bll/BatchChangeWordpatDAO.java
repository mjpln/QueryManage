package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.UtilityOperate.GetConfigValue;
import com.knowology.dal.Database;
import com.knowology.km.util.GetSession;

public class BatchChangeWordpatDAO {
	private static Logger log = Logger.getLogger(BatchChangeWordpatDAO.class);

	/**
	 * 查询商家
	 * 
	 * @return
	 */
	public static Object getBrand() {
		JSONArray jsona = new JSONArray();
		String sql = "select DISTINCT brand from service";
		log.info("查询sql=" + sql);
		Result rs = Database.executeQuery(sql);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsono = new JSONObject();
				String text = rs.getRows()[i].get("brand") == null ? ""
						: String.valueOf(rs.getRows()[i].get("brand"));
				if ("".equals(text) || "知识库".equals(text)) {
					continue;
				}
				jsono.put("id", i);
				jsono.put("text", text);
				jsona.add(jsono);
			}
		}
		return jsona;
	}

	/**
	 * 查询所有下拉框数据
	 * 
	 * @return
	 */
	public static Object search(String brand) {
		JSONArray jsonA = new JSONArray();
		String sql = "select kbdataid,abstract from kbdata where serviceid in (select serviceid from service where brand='"
				+ brand + "')";
		log.info("执行查询：sql=" + sql);
		Result rs = Database.executeQuery(sql);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String kbdataid = rs.getRows()[i].get("kbdataid") == null ? ""
						: String.valueOf(rs.getRows()[i].get("kbdataid"));
				String abstractStr = rs.getRows()[i].get("abstract") == null ? ""
						: String.valueOf(rs.getRows()[i].get("abstract"));
				JSONObject jsonO = new JSONObject();
				jsonO.put("id", kbdataid);
				jsonO.put("text", abstractStr);
				jsonA.add(jsonO);
			}
		}
		return jsonA;
	}

	/**
	 * 根据输入文本查询下拉框数据
	 * 
	 * @param text
	 * @return
	 */
	public static Object search(String text, String brand) {
		JSONArray jsonA = new JSONArray();
		String sql = "select kbdataid,abstract from kbdata where abstract like '%"
				+ text
				+ "%'"
				+ "and serviceid in (select serviceid from service where brand='"
				+ brand + "')";
		log.info("执行查询：sql=" + sql);
		Result rs = Database.executeQuery(sql);
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				String kbdataid = rs.getRows()[i].get("kbdataid") == null ? ""
						: String.valueOf(rs.getRows()[i].get("kbdataid"));
				String abstractStr = rs.getRows()[i].get("abstract") == null ? ""
						: String.valueOf(rs.getRows()[i].get("abstract"));
				JSONObject jsonO = new JSONObject();
				jsonO.put("id", kbdataid);
				jsonO.put("text", abstractStr);
				jsonA.add(jsonO);
			}
		}
		return jsonA;
	}

	/**
	 * 批量替换词模
	 * 
	 * @param target目标词模
	 * @param replace更新词模
	 * @param kbdataid词模对应的摘要id
	 * @return
	 */
	public static Object relpace(String target, String replace,
			String kbdataid, String brand) {
		JSONObject jsono = new JSONObject();
		List<String> listSqls = new ArrayList<String>();
		List<List<?>> listParams = new ArrayList<List<?>>();
		List<String> params = new ArrayList<String>();
		String regex = "[A-Za-z0-9\u4e00-\u9fa5]+(近类|父类)";
		target = pattern(target, regex, "!");
		replace = pattern(replace, regex, "!");
		regex = "[\\|!A-Za-z0-9\u4e00-\u9fa5]*[^*\\[\\]]";
		target = pattern(target, regex, "<", true);
		replace = pattern(replace, regex, "<", true);
		regex = "[\\|!A-Za-z0-9\u4e00-\u9fa5]*[^*\\[\\]\\<]";
		target = pattern(target, regex, ">", false);
		replace = pattern(replace, regex, ">", false);
		log.info("目标词模：" + target);
		log.info("更新词模：" + replace);
		params.add(target);
		params.add(replace);

		String sql = "update wordpat set wordpat=REPLACE(wordpat,?,?)";
		if (kbdataid != null && !kbdataid.equals("")) {
			sql += " where kbdataid=?";
			params.add(kbdataid);
		}else{
			sql += " where kbdataid in (select kbdataid from kbdata where serviceid in (select serviceid from service where service=?))";
			params.add(brand);
		}
		listSqls.add(sql);
		listParams.add(params);
		log.info("更新sql=" + listSqls.toString() + ";参数："
				+ listParams.toString());
		int rs = 1;
//		int rs = Database.executeNonQueryTransaction(listSqls, listParams);

		//初始化参数
		listSqls = new ArrayList<String>();
		listParams = new ArrayList<List<?>>();
		// 操作日志
		// 获取servicetype
		User user = (User) GetSession.getSessionByKey("accessUser");
		listSqls.add(GetConfigValue.LogSql());
		List<Object> paramsOper = new ArrayList<Object>();
		paramsOper.add(user.getUserIP());
		paramsOper.add(user.getUserID());
		paramsOper.add(user.getUserName());
		paramsOper.add(user.getBrand());
		paramsOper.add("");
		paramsOper.add("批量修改词模");
		paramsOper.add("");
		paramsOper.add("批量修改了" + user.getBrand() + "下" + "kbdataid=" + kbdataid
				+ "下词模，将【" + target + "】替换成【" + replace + "】");
		paramsOper.add("");
		listParams.add(paramsOper);
		log.info("操作日志sql=" + listSqls.toString() + ";参数："
				+ listParams.toString());
//		Database.executeNonQueryTransaction(listSqls, listParams);
		
		if (rs > 0) {
			jsono.put("success", true);
			jsono.put("msg", "替换成功");
		} else {
			jsono.put("success", false);
			jsono.put("msg", "替换失败");
		}
		return jsono;
	}

	/**
	 * 正则方法
	 * 
	 * @param target字符串
	 * @param regex正则语法
	 * @param str替换字符
	 * @return
	 */
	public static String pattern(String target, String regex, String str) {
		Pattern pat = Pattern.compile(regex);
		Matcher mat = pat.matcher(target);
		int index = 0;
		StringBuilder newStr = new StringBuilder();
		newStr.append(target);
		int count = 0;
		while (mat.find()) {
			index = mat.start();
			newStr.insert(index + count, str);
			count++;
		}
		return newStr.toString();
	}

	/**
	 * 正则
	 * 
	 * @param target原串
	 * @param regex正则模板
	 * @param str替换字符
	 * @param flag获取匹配串开始
	 *            /结束位置true开始位置false结束位置
	 * @return
	 */
	public static String pattern(String target, String regex, String str,
			boolean flag) {
		Pattern pat = Pattern.compile(regex);
		Matcher mat = pat.matcher(target);
		int index = 0;
		StringBuilder newStr = new StringBuilder();
		newStr.append(target);
		int count = 0;
		while (mat.find()) {
			int offset = 0;
			if (flag)
				index = mat.start();
			else
				index = mat.end();
			newStr.insert(index + count, str);
			count++;
		}
		return newStr.toString();
	}

	/**
	 * 用于将可选词改成必选
	 * 
	 * @param replace可选词
	 * @param kbdataid客户意图id
	 * @return
	 */
	public static Object addMastWordClass(String replace, String kbdataid,
			String brand) {
		JSONObject json = new JSONObject();
		List<String> listSqls = new ArrayList<String>();
		List<List<?>> listParams = new ArrayList<List<?>>();
		String regex = "[A-Za-z0-9\u4e00-\u9fa5]+(近类|父类)";
		replace = pattern(replace, regex, "!");
		regex = "[\\|!A-Za-z0-9\u4e00-\u9fa5]*[^*\\[\\]]";
		replace = pattern(replace, regex, "<", true);
		regex = "[\\|!A-Za-z0-9\u4e00-\u9fa5]*[^*\\[\\]\\<]";
		replace = pattern(replace, regex, ">", false);
		// 操作日志
		// 获取servicetype
		User user = (User) GetSession.getSessionByKey("accessUser");
		listSqls.add(GetConfigValue.LogSql());
		List<Object> paramsOper = new ArrayList<Object>();
		paramsOper.add(user.getUserIP());
		paramsOper.add(user.getUserID());
		paramsOper.add(user.getUserName());
		paramsOper.add(user.getBrand());
		paramsOper.add("");
		paramsOper.add("批量添加必选词");
		paramsOper.add("中信银行");
		paramsOper.add("批量修改了" + user.getBrand() + "下" + "kbdataid=" + kbdataid
				+ "下词模，添加必选词【" + replace + "】");
		paramsOper.add("");
		listParams.add(paramsOper);
		log.info("添加必选词：" + replace);
		if (kbdataid != null && !"".equals(kbdataid)) {
			String sql = "update wordpat set wordpat=REPLACE(wordpat,'@2', '*"
					+ replace + "@2') where kbdataid=" + kbdataid
					+ " and brand='" + brand + "'";
			log.info("添加必选词：sql=" + sql);
			try {
				// int rs =1;
				int rs = Database.executeNonQuery(sql);
				// 加入操作日志
				Database.executeNonQueryTransaction(listSqls, listParams);
				if (rs > 0) {
					json.put("success", true);
					json.put("msg", "更新成功");
				} else {
					json.put("success", false);
					json.put("msg", "更新失败");
				}
			} catch (Exception e) {
				log.info("添加必选词异常：sql=" + sql + "，异常信息：" + e.getMessage());
			}
		} else {
			json.put("success", false);
			json.put("msg", "下拉框不能为空");
		}
		return json;
	}

	public static void main(String[] args) {
		relpace("[你好近类|你好|你好近类|你好近类]*[你好近类|你好父类]",
				"你好近类|你好|你好近类|你好近类*[你好近类|你好父类]", "", "");
	}
}
