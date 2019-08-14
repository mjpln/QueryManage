package com.knowology.km.bll;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.knowology.km.common.util.CookieUtil;
import com.knowology.km.entity.GetUserParam;
import com.opensymphony.xwork2.ActionContext;

public class MyClass {
	/**
	 * 通过查询Cookie获取用户登录的name
	 * 
	 * @return
	 */
	public static String LoginUserName() {
		try {
			String user = CookieUtil.findCookie();
			if (user != null && !"".equals(user) && user.length() > 0) {
				return user.split(",")[1];
			} else {
				return " ";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return " ";
		}
	}

	/**
	 * 通过查询Cookie获取用户登录的id
	 * 
	 * @return
	 */
	public static String LoginUserId() {
		try {
			String user = CookieUtil.findCookie();
			if (user != null && !"".equals(user) && user.length() > 0) {
				return user.split(",")[0];
			} else {
				return " ";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return " ";
		}
	}

	/**
	 * 通过查询Cookie获取用户角色
	 * 
	 * @return
	 */
	public static String LoginUserRole() {
		try {
			String user = CookieUtil.findCookie();
			if (user != null && !"".equals(user) && user.length() > 0) {
				return user.split(",")[3];
			} else {
				return " ";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return " ";
		}
	}
	/**
	 * 通过查询Cookie获取用户登录的ip
	 * 
	 * @return
	 */
	public static String LoginUserIp() {
		try {
			String user = CookieUtil.findCookie();
			if (user != null && !"".equals(user) && user.length() > 0) {
				return user.split(",")[2];
			} else {
				return " ";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return " ";
		}
	}
	/**
	 * 通过查询Cookie获取用户登录的sessionid
	 * 
	 * @return
	 */
	public static String LoginUserSessionid() {
		try {
			String user = CookieUtil.findCookie();
			if (user != null && !"".equals(user) && user.length() > 0) {
				return user.split(",")[4];
			} else {
				return " ";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return " ";
		}
	}

	/**
	 * 通过查询Cookie获取行业商家应用
	 * 
	 * @return
	 */
	public static String IndustryOrganizationApplication() {
		try {
			String user = CookieUtil.findCookie();
			if (user != null && !"".equals(user) && user.length() > 0) {
				return user.split(",")[5];
			} else {
				return " ";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return " ";
		}
	}

	/**
	 * 通过查询Cookie获取跟业务
	 * 
	 * @return
	 */
	public static String ServiceRoot() {
		try {
			String user = CookieUtil.findCookie();
			if (user != null && !"".equals(user) && user.length() > 0) {
				return user.split(",")[6].replace("@", ",");
			} else {
				return " ";
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return " ";
		}
	}

	
	/**
	 * 删除cookie
	 */
	public static void deleteCookie() {
		CookieUtil.delCookie();
	}

	/**
	 * 添加cookie
	 * 
	 * @param user
	 */
	public static void addCookie(GetUserParam user) {
		try {
			CookieUtil.addCookieOfSeconds(user);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取session
	 * 
	 * @return
	 */
	public static String getSession() {
		return (String) ActionContext.getContext().getSession().get("chkCode");
	}

	/**
	 * MD5的加密算法
	 * 
	 * @param str
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String EncryptMD5(String password) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(password.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		byte[] byteArray = messageDigest.digest();
		StringBuffer md5StrBuff = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(
						Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return md5StrBuff.toString();
	}
}
