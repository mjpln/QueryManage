/**  
 * @Project: JfinalDemo 
 * @Title: baseRoute.java
 * @Package com.knowology.common.route
 * @author c_wolf your emai address
 * @date 2014-9-1 下午3:38:51
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.route;

import com.jfinal.config.Routes;
import com.knowology.common.controller.CommonAjaxController;
import com.knowology.common.controller.CommonPageController;
import com.knowology.common.controller.FileController;

/**
 * 内容摘要 ：
 *
 * 类修改者	修改日期
 * 修改说明
 * @ClassName baseRoute
 * <p>Company: knowology </p>
 * @author c_wolf your emai address
 * @date 2014-9-1 下午3:38:51
 * @version V1.0
 */

public class baseRoute extends Routes {
	/**
	 * 配置路由，路径为/common的请求由CommonController来处理
	 */
	@Override
	public void config() {
		//common下为前端Ajax请求
		add("/common",CommonAjaxController.class);
		//其他由页面控制器来控制
		add("/",CommonPageController.class);
		add("/file",FileController.class);		
	}	
}
