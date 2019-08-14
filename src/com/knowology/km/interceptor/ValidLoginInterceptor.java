package com.knowology.km.interceptor;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.knowology.Bean.User;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.km.util.GetSession;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;

/**
 * 用户信息、操作权限拦截器
 * 
 */
public class ValidLoginInterceptor implements Interceptor {

	public String intercept(ActionInvocation arg0) throws Exception {
		// 获取request域中信息
		HttpServletRequest req = ServletActionContext.getRequest();
		// 获得当前请求url
		String url = req.getServletPath();
		// 获得请求类型
		String type = req.getHeader("X-Requested-With");
		// 获得session
		Object sre = GetSession.getSessionByKey("accessUser");
		// 鉴权结果
		boolean authFlag = false;
		String userid = null;
		PrintWriter printWriter = null;
		if ("XMLHttpRequest".equalsIgnoreCase(type)) {// ajax请求
			if (sre == null || "".equals(sre)) {// session过期,返回前台自定义字符串，由js处理
				printWriter = ServletActionContext.getResponse().getWriter();
				printWriter.print("ajaxSessionTimeOut");
				printWriter.flush();
				printWriter.close();
				return null;
			} else {// session未过期，判断操作权限
				User user = (User) sre;
				userid = user.getUserID();
				String resourceType = req.getParameter("resourcetype");// 资源类型
				String operationType = req.getParameter("operationtype");// 资源操作类型
				String resourceid = req.getParameter("resourceid");// 业务资源ID
				String robotid = req.getParameter("robotid");// 实体机器人ID
				String wordpatid = req.getParameter("wordpatid");// 词模ID
				if (resourceType != null && !"".equals(resourceType)
						&& operationType != null && !"".equals(operationType)
						&& resourceid != null && !"".equals(resourceid)) {
					operationType = operationType.toUpperCase();
					if (robotid != null && !"".equals(robotid)) {// 加入实体机器人ID权限判断
						if (CommonLibPermissionDAO.isHaveOperationPermission(
								userid, resourceType, resourceid,
								operationType, robotid)) {
							authFlag = true;// 有权限
						} else {
							authFlag = false;// 无权限
						}
					} else {// 权限判断
						if (CommonLibPermissionDAO
								.isHaveOperationPermission(userid,
										resourceType, resourceid, operationType,wordpatid)) {
							authFlag = true;// 有权限
						} else {
							authFlag = false;// 无权限
						}
					}
				} else {
					return arg0.invoke();
				}
				if (authFlag) {// 权限满足
					return arg0.invoke();
				} else {
					printWriter = ServletActionContext.getResponse()
							.getWriter();
					printWriter.print("noLimit");
					printWriter.flush();
					printWriter.close();
					return null;
				}
			}
		} else {// 普通http请求 直接返回error
			if (sre == null || "".equals(sre)) {
				return "error";
			} else {
				return arg0.invoke();
			}
		}
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

	public void init() {
		// TODO Auto-generated method stub

	}

}
