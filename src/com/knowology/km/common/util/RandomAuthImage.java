package com.knowology.km.common.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class RandomAuthImage extends javax.servlet.http.HttpServlet implements
		javax.servlet.Servlet {
	static final long serialVersionUID = 1L;

	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		response.setContentType("image/jpeg");

		// 生成随机字串
		String randomCode = RandomCodeUtils.generateRandomCode(4);
		// 存入会话session
		HttpSession session = request.getSession(true);
		session.setAttribute("randcode", randomCode.toLowerCase());
		// 生成图片
		int w = 200, h = 80;
		RandomCodeUtils.outputImage(w, h, response.getOutputStream(),
				randomCode);

	}
}