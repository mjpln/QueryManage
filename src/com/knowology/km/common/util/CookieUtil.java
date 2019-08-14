package com.knowology.km.common.util;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import com.knowology.km.dal.Database;
import com.knowology.km.entity.GetUserParam;
import com.knowology.km.util.MyUtil;

/**
 * Cookie的工具类
 * 
 * @author Administrator
 * 
 */
public class CookieUtil {
	public static final String USER_COOKIE = "user.cookie";
	private static Logger logger = Logger.getLogger(MyUtil.class);
	/**
	 * cookie的创建(有时间限制)
	 * 
	 * @param response
	 * @param name
	 * @param value
	 * @param seconds
	 * @throws UnsupportedEncodingException
	 */
	public static void addCookieOfSeconds(GetUserParam user)
			throws UnsupportedEncodingException {
		InetAddress addr;
		String ip = " ";
		try {
			addr = InetAddress.getLocalHost();
			ip = addr.getHostAddress().toString();
		} catch (UnknownHostException e) {
			logger.error(" 获取IP异常信息==>" + e);
		}
		String userInfo = user.getWorkerid() + "," + user.getName() + "," + ip+","+user.getRole()+","+user.sessionid+","+user.industryOrganizationApplication+","+user.serviceroot;
		Cookie c = new Cookie(USER_COOKIE, URLEncoder.encode(userInfo, "utf-8"));
		c.setPath("/");
		c.setMaxAge(Integer.parseInt(Database.getJDBCValues("cookie.seconds")));
		ServletActionContext.getResponse().addCookie(c);
	}

	/**
	 * cookie的查询
	 * 
	 * @param cookies
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String findCookie() throws UnsupportedEncodingException {
		Cookie[] cookies = null;
		try {
			cookies = ServletActionContext.getRequest().getCookies();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String value = "";
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (USER_COOKIE.equals(cookie.getName())) {
					value = URLDecoder.decode(cookie.getValue(), "utf-8");
				}
			}
		}
		return value;
	}

	/**
	 * cookie的删除
	 * 
	 * @param response
	 * @param name
	 */
	public static void delCookie() {
		Cookie c = new Cookie(USER_COOKIE, null);
		c.setMaxAge(0);
		c.setPath("/");
		ServletActionContext.getResponse().addCookie(c);
	}
}
