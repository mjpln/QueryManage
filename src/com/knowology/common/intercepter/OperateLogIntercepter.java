/**  
 * @Project: JfinalDemo 
 * @Title: OperateLogIntercepter.java
 * @Package com.knowology.common.intercepter
 * @author c_wolf your emai address
 * @date 2014-9-4 上午11:52:25
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.intercepter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.knowology.common.plugin.SqlQueryPlugin;
import com.knowology.common.thread.LogWriteThread;

/**
 * 内容摘要 ：
 * 
 * 类修改者 修改日期 修改说明
 * 
 * @ClassName OperateLogIntercepter
 * @Company: knowology
 * @author c_wolf your emai address
 * @date 2014-9-4 上午11:52:25
 * @version V1.0
 */

public class OperateLogIntercepter implements Interceptor {
	// 用来记录日志的线程集合，最大数量为30个
	public static ExecutorService execs = Executors.newCachedThreadPool();

	@SuppressWarnings("unchecked")
	public void intercept(ActionInvocation ai) {
		ai.invoke();// 执行完成后，去判断是否记录日志
		String request = ai.getController().getAttr("request");
		// 将字符串转为json对象，使用fastjson
		JSONObject sqldata = JSONObject.parseObject(request);
		String sqlid = sqldata.getString("sqlid");
		Map map = SqlQueryPlugin.getSql(sqlid);
		if (map.get("needLog") != null) {
			LogWriteThread logT = new LogWriteThread();
			// 获取页面路径
			String pagePath = ai.getController().getRequest().getHeader(
					"referer");
			List paras = sqldata.getJSONArray("paras");
			logT.setParas(paras);
			logT.setSqldata(map);
			HttpSession session = ai.getController().getSession();
			logT.setObj(session);
			logT.setReferer(pagePath);
			execs.execute(logT);
		}
	}
}
