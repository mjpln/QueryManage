/**  
 * @Project: JfinalDemo 
 * @Title: ClearCacheTask.java
 * @Package com.knowology.common.tasks
 * @author c_wolf your emai address
 * @date 2014-10-16 上午9:49:03
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.tasks;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jfinal.log.Logger;
import com.jfinal.plugin.ehcache.CacheKit;

/**
 * 内容摘要 ：
 * 
 * @Company: knowology
 * @author c_wolf your emai address
 * @date 2014-10-16 上午9:49:03
 */

public class ClearCacheTask implements Job {
	public static int i = 0;
	private Logger log = Logger.getLogger(this.getClass());

	public void execute(JobExecutionContext context)

	throws JobExecutionException {
		log.info("清除ajax_query的全部缓存");
		CacheKit.removeAll("ajax_query");
	}
}