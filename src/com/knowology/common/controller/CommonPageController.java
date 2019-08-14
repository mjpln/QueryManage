package com.knowology.common.controller;

import org.apache.log4j.Logger;

import com.jfinal.core.Controller;

import demo.DemoConfig;

/**
 * 内容摘要 ：
 * 
 * 类修改者 修改日期 修改说明
 * 
 * @ClassName CommonPagesController
 * @Company: knowology
 * @author c_wolf your emai address
 * @date 2014-9-1 下午4:49:23
 * @version V1.0
 */

public class CommonPageController extends Controller {
	Logger log = Logger.getLogger(Controller.class);

	public void index() {
		render(DemoConfig.home);
	}

	public void error() {
		render("/errorPages/404.html");
	}
}
