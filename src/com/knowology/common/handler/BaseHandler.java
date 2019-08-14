/**  
 * @Project: JfinalDemo 
 * @Title: BaseHandler.java
 * @Package com.knowology.common.handler
 * @author c_wolf your emai address
 * @date 2014-9-4 下午11:40:41
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.handler.Handler;
import com.knowology.common.plugin.SqlQueryPlugin;

/**
 * 内容摘要 ：
 * 
 * 类修改者 修改日期 修改说明
 * 
 * @ClassName BaseHandler
 * @Company: knowology
 * @author c_wolf your emai address
 * @date 2014-9-4 下午11:40:41
 * @version V1.0
 */

public class BaseHandler extends Handler {
	/*
	 * (non-Javadoc) <p>Title: handle</p> <p>Description: </p>
	 * 
	 * @param target
	 * 
	 * @param request
	 * 
	 * @param response
	 * 
	 * @param isHandled
	 * 
	 * @see com.jfinal.handler.Handler#handle(java.lang.String,
	 * javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, boolean[])
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void handle(String target, HttpServletRequest request,
			HttpServletResponse response, boolean[] isHandled) {
		if (target.endsWith("common/index")) {
			// System.out.println("使用公共请求路径！");
			// 获取请求字符串
			String req = request.getParameter("request");
			// 将字符串转为json对象，使用fastjson
			JSONObject obj = JSONObject.parseObject(req);
			// 后台获取到sqlid和参数,并获取到sql语句
			String sqlid = obj.getString("sqlid");

			Map sqldata = SqlQueryPlugin.getSql(sqlid);
			if (sqldata == null) {
				request.setAttribute("errorMsg", "不存在sqlid:" + sqlid
						+ "，请检查sql文件");
				target = "/common/error";
			} else {
				if (sqldata.containsKey("sqllist")) {
					target = "/common/trans";
				} else if (sqldata.containsKey("operate")) {
					String operate = sqldata.get("operate").toString();
					if ("select".equals(operate)) {
						target = "/common/query";
					} else if ("queryPages".equals(operate)) {
						target = "/common/queryPages";
					} else {
						target = "/common/noquery";
					}
				}
			}
		}
		nextHandler.handle(target, request, response, isHandled);
	}
}