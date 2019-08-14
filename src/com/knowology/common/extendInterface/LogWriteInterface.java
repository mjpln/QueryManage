/**  
 * @Project: JfinalDemo 
 * @Title: LogTool.java
 * @Package com.knowology.common.utils
 * @author c_wolf your emai address
 * @date 2014-9-4 下午3:08:31
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.extendInterface;

import java.util.List;

import javax.servlet.http.HttpSession;

/**
 * 
 * @内容摘要 ：
 * <p>Company: knowology </p>
 * @author c_wolf your emai address
 * @date 2014-9-9 下午3:33:59
 */
public interface LogWriteInterface {
	/**
	 * 
	 * @内容摘要：写日志的接口
	 * @author c_wolf 2014-9-9
	 * @param session
	 * @param sqllist
	 * @param paras void
	 */
	public void writeLog(String tableNames,HttpSession session,String sql,List<?> paras,String referer);
	public void writePagesLog(String tableNames,HttpSession session,String sql,List<?> paras,String referer);
	public void writeTransLog(String tableNames,HttpSession session,List<?> sqllist,List<?> paras,String referer);
}
