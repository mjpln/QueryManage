/**  
 * @Project: JfinalDemo 
 * @Title: TestTask.java
 * @Package com.knowology.common.tasks
 * @author c_wolf your emai address
 * @date 2014-10-16 上午11:17:53
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.tasks;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @内容摘要 ：
 * <p>Company: knowology </p>
 * @author c_wolf your emai address
 * @date 2014-10-16 上午11:17:53
 */

public class TestTask implements Job {

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		
		System.out.println(context.getJobDetail().getKey());
	}

}
