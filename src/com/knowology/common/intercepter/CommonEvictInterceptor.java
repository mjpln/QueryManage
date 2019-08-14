/**  
 * @Project: JfinalDemo 
 * @Title: CommonEvictInterceptor.java
 * @Package com.knowology.common.intercepter
 * @author c_wolf your emai address
 * @date 2014-9-1 下午10:28:54
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.intercepter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.plugin.ehcache.CacheName;
import com.knowology.common.plugin.SqlQueryPlugin;

import demo.DemoConfig;

/**
 * 内容摘要 ：
 * 
 * 类修改者 修改日期 修改说明
 * 
 * @ClassName CommonEvictInterceptor
 * @Company: knowology
 * @author c_wolf your emai address
 * @date 2014-9-1 下午10:28:54
 * @version V1.0
 */

public class CommonEvictInterceptor implements Interceptor {

	final public void intercept(ActionInvocation ai) {
		ai.invoke();
		if (DemoConfig.cache == 1)
			clearCacheByKey(ai);
		// CacheKit.remove(buildCacheName(ai), buildCacheKey(ai));
		// CacheKit.removeAll(buildCacheName(ai));
	}

	/**
	 * 方法名称： clearCacheByKey 内容摘要： 修改者名字 修改日期 修改说明
	 * 
	 * @author c_wolf 2014-9-2 void
	 * @throws
	 * 
	 */
	@SuppressWarnings("unchecked")
	private void clearCacheByKey(ActionInvocation ai) {
		// 获取当前缓存名下的所有键值
		Set<String> cacheKeyList = CommonCacheInterceptor.cache_Tables.keySet();
		// 获取到当前操作涉及到的表，然后清除这些表涉及到的缓存
		String request = ai.getController().getAttr("request");
		// 将字符串转为json对象，使用fastjson
		JSONObject sqldata = JSONObject.parseObject(request);
		String sqlid = sqldata.getString("sqlid");
		Map map = SqlQueryPlugin.getSql(sqlid);
		HashSet tableNames = (HashSet) map.get("tableNames");
		String cacheName = buildCacheName(ai);
		// 遍历所有的缓存名下的key，找到涉及到当前表的缓存，进行删除
		for (String cacheKey : cacheKeyList) {
			HashSet cacheTableNames = (HashSet) CommonCacheInterceptor.cache_Tables
					.get(cacheKey).clone();
			// 判断是否修改了该缓存中设计到的表

			Boolean havaUpdate = cacheTableNames.removeAll(tableNames);
			if (havaUpdate) {
				CacheKit.remove(cacheName, cacheKey);
			}
		}
	}

	private String buildCacheName(ActionInvocation ai) {
		CacheName cacheName = ai.getMethod().getAnnotation(CacheName.class);
		if (cacheName != null)
			return cacheName.value();

		cacheName = ai.getController().getClass()
				.getAnnotation(CacheName.class);
		if (cacheName == null)
			throw new RuntimeException(
					"EvictInterceptor need CacheName annotation in controller.");
		return cacheName.value();
	}
}