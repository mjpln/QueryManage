/**  
 * @Project: JfinalDemo 
 * @Title: LoggingEventWrapper.java
 * @Package com.knowology.common.log
 * @author c_wolf your emai address
 * @date 2014-11-19 下午12:59:20
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.log;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 内容摘要 ：
 * 
 * @Company: knowology
 * @author c_wolf your emai address
 * @date 2014-11-19 下午12:59:20
 */

public class LoggingEventWrapper implements Serializable {

	private static final long serialVersionUID = 3281981073249085474L;
	private LoggingEvent loggingEvent;

	@SuppressWarnings("unused")
	private Long timeStamp;
	@SuppressWarnings("unused")
	private String level;
	@SuppressWarnings("unused")
	private String logger;
	@SuppressWarnings("unused")
	private String message;
	private String detail;
	@SuppressWarnings("unused")
	private String ipAddress;
	@SuppressWarnings("unused")
	private String hostName;

	public LoggingEventWrapper(LoggingEvent loggingEvent, Layout layout) {
		this.loggingEvent = loggingEvent;
		// EnhancedPatternLayout layout = new EnhancedPatternLayout();
		this.detail = layout.format(loggingEvent);
	}

	public Long getTimeStamp() {
		return this.loggingEvent.timeStamp;
	}

	public String getLevel() {
		return this.loggingEvent.getLevel().toString();
	}

	public String getLogger() {
		return this.loggingEvent.getLoggerName();
	}

	public String getMessage() {
		return this.loggingEvent.getRenderedMessage();
	}

	public String getDetail() {
		return this.detail;
	}

	public LoggingEvent getLoggingEvent() {
		return loggingEvent;
	}

	public String getIpAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "Could not determine IP";
		}
	}

	public String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "Could not determine Host Name";
		}
	}
}
