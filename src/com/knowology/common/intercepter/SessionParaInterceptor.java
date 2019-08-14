/**  
 * @Project: JfinalDemo 
 * @Title: SessionParaInterceptor.java
 * @Package com.knowology.common.intercepter
 * @author c_wolf your emai address
 * @date 2014-11-5 下午3:20:55
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.intercepter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Interceptor;
import com.jfinal.core.ActionInvocation;
import com.jfinal.core.Controller;

/**
 * @内容摘要 ：这个是用来过滤参数的，当参数中包含 $session.workerid 时，将这个替换成session中的workerid
 * <p>Company: knowology </p>
 * @author c_wolf your emai address
 * @date 2014-11-5 下午3:20:55
 */

public class SessionParaInterceptor implements Interceptor {
	JSONObject result = new JSONObject();//如果出错了，返回这个错误
	public void intercept(ActionInvocation ai) {
		Controller c = ai.getController();
		String request = c.getPara("request");
		String regex = "\\$session.[^\"]+";
		Pattern pattern = Pattern.compile(regex);//使用正则将$session提取出来
		Matcher m= pattern.matcher(request);
		HttpSession session = c.getRequest().getSession(false);
	    while(m.find()){
	    	String para = m.group();
	    	String sessionKey = para.replace("$session.", "");
	    	System.out.print("需要转换session的字段"+para+">>"+sessionKey);
	    	if(session==null||session.getAttribute("user")==null){//未获取到用户信息，可能用户未登陆
	    		result.put("state", "fail");
	    		result.put("message", "未获取到用户信息，可能用户未登陆");
	    		c.renderJson(result);
	    		return;
	    	}
	    	JSONObject user = (JSONObject) session.getAttribute("user");
	    	Object val = user.get(sessionKey);
	    	if(val==null){//如果用户数据为空
	    		result.put("state", "fail");
	    		result.put("message", "当前key:"+sessionKey+"未找到session中对应的值");
	    		c.renderJson(result);
	    		return;
	    	}
	    	System.out.println(": "+val);
	    	request = request.replace(para, val.toString());
	    }  
	    
	    c.setAttr("request", request);//将新的请求字符串重新放入Controller中
	    ai.invoke();//执行ai
	}
	public static void main(String[] args) {
		String request = "{\"request\":{\"sqlid\":$session.ee2,\"select_wordpat\",\"paras\":[$session.ee,10522136,$session.hello],\"ignore\":[\"wordpat\"]}}";
		String ses = "\\$session.[^,^\\]]+";
		Pattern pattern = Pattern.compile(ses);
		Matcher m= pattern.matcher(request);
	    while(m.find()){  
	    System.out.println(m.group());  
	    }  
	}
		
}
