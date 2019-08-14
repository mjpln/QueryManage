/**  
 * @Project: JfinalDemo 
 * @Title: ChildCacheInterceptor.java
 * @Package com.knowology.common.intercepter
 * @author c_wolf your emai address
 * @date 2014-9-1 下午10:20:44
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.intercepter;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.CacheName;
import com.jfinal.render.Render;
import com.knowology.common.plugin.SqlQueryPlugin;

import demo.DemoConfig;

/**
 * 内容摘要 ：
 *
 * 类修改者	修改日期
 * 修改说明
 * @ClassName ChildCacheInterceptor
 * <p>Company: knowology </p>
 * @author c_wolf your emai address
 * @date 2014-9-1 下午10:20:44
 * @version V1.0
 */

public class CommonCacheInterceptor implements Interceptor{
	private static final String renderKey = "$renderKey$";
	private static volatile ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<String, ReentrantLock>();
	//存放缓存和表的关系
	public static volatile ConcurrentHashMap<String, HashSet<String>> cache_Tables = new ConcurrentHashMap<String,HashSet<String>>();
	
	private ReentrantLock getLock(String key) {
		ReentrantLock lock = lockMap.get(key);
		if (lock != null)
			return lock;
		
		lock = new ReentrantLock();
		ReentrantLock previousLock = lockMap.putIfAbsent(key, lock);
		return previousLock == null ? lock : previousLock;
	}
	
	final public void intercept(ActionInvocation ai) {
		if(DemoConfig.cache!=1){
			ai.invoke();
			return;
		}
		Controller controller = ai.getController();
		String cacheName = buildCacheName(ai, controller);
		String cacheKey = buildCacheKey(ai, controller);
		Map<String, Object> cacheData = CacheKit.get(cacheName, cacheKey);
		if (cacheData == null) {
			Lock lock = getLock(cacheName);
			lock.lock();					// prevent cache snowslide
			try {
				cacheData = CacheKit.get(cacheName, cacheKey);
				if (cacheData == null) {
					ai.invoke();
					cacheAction(cacheName, cacheKey, controller);
					return ;
				}
			}
			finally {
				lock.unlock();
			}
		}else{
			System.out.println("使用缓存"+cacheKey);
		}
		
		useCacheDataAndRender(cacheData, controller);
	}
	
	// TODO 考虑与 EvictInterceptor 一样强制使用  @CacheName
	private String buildCacheName(ActionInvocation ai, Controller controller) {
		CacheName cacheName = ai.getMethod().getAnnotation(CacheName.class);
		if (cacheName != null)
			return cacheName.value();
		cacheName = controller.getClass().getAnnotation(CacheName.class);
		return (cacheName != null) ? cacheName.value() : ai.getActionKey();
	}
	
	private String buildCacheKey(ActionInvocation ai, Controller controller) {
		//StringBuilder sb = new StringBuilder(ai.getActionKey());
		StringBuilder sb = new StringBuilder();
		/*String urlPara = controller.getPara();
		if (urlPara != null)
			sb.append("/").append(urlPara);
		
		String queryString = controller.getRequest().getQueryString();		
		if (queryString != null)
			sb.append("?").append(queryString);
		
	*/
		String parameter = controller.getAttr("request");
		try{
		int page = Integer.parseInt(controller.getPara("page"));
		int rows = Integer.parseInt(controller.getPara("rows"));
		parameter+="@"+page+"@"+rows;
		}catch(Exception e){
			
		}
		sb.append(parameter);
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	private void cacheAction(String cacheName, String cacheKey, Controller controller) {
		HttpServletRequest request = controller.getRequest();
		Map<String, Object> cacheData = new HashMap<String, Object>();
		for (Enumeration<String> names=request.getAttributeNames(); names.hasMoreElements();) {
			String name = names.nextElement();
			cacheData.put(name, request.getAttribute(name));
		}		
		cacheData.put(renderKey, controller.getRender());		// cache render
		CacheKit.put(cacheName, cacheKey, cacheData);
		
		//获取请求字符串
		String requestStr = controller.getAttr("request");
		//将字符串转为json对象，使用fastjson
		JSONObject obj = JSONObject.parseObject(requestStr);
		String sqlid = obj.getString("sqlid");
		Map map = SqlQueryPlugin.getSql(sqlid);
		HashSet tableNames = (HashSet) map.get("tableNames");	
		cache_Tables.put(cacheKey,tableNames);
		
		
	}
	
	private void useCacheDataAndRender(Map<String, Object> cacheData, Controller controller) {
		HttpServletRequest request = controller.getRequest();
		Set<Entry<String, Object>> set = cacheData.entrySet();
		for (Iterator<Entry<String, Object>> it=set.iterator(); it.hasNext();) {
			Entry<String, Object> entry = it.next();
			request.setAttribute(entry.getKey(), entry.getValue());
		}
		request.removeAttribute(renderKey);
		
		controller.render((Render)cacheData.get(renderKey));		// set render from cacheData
	}
}