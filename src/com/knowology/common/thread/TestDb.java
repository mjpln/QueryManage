package com.knowology.common.thread;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.knowology.km.dal.Database;

public class TestDb {
	public static void main(String args[]){
		Object o = "<p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\"><font face=\"Times New Roman\"> </font>华夏基金网站【</span></span><a target=\"_blank\" href=\"http://www.chinaamc.com/wodejijin/zhcx/index.shtml\"><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">基金账户查询</span></span></a><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">】和【</span></span><a target=\"_blank\" href=\"http://www.chinaamc.com/wodejijin/index.shtml\"><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">网上交易</span></span></a><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">】是两个系统。登录【基金账户查询】系统不能办理基金交易业务，可以查看直销账户和代销账户购买和持有华夏基金的情况；登录【网上交易】可以办理基金交易业务，但只能查看直销账户（通过华夏基金理财中心或【网上交易】系统）持有基金的情况。</span></span></p><p class=\"MsoNormal\" style=\"line-height: 150%; margin: 0cm 0cm 0pt\"><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\"><span lang=\"EN-US\">（在您开通网上交易并且登录网上交易能够查看到基金账号后，</span><span style=\"mso-ascii-font-family: Calibri; mso-hansi-font-family: Calibri\">可通过点击网上交易系统中的【全面基金账户查询】直接登录网上查询系统）</span></span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">具体区别如下：</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">1、系统功能不同</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">【基金账户查询】是以查询为主要功能的系统。登录【基金账户查询】后您可以查询您持有的全部华夏旗下基金情况，还可以修改您的联系方式、订制信息等，但该系统不能办理基金申购、赎回、分红方式变更等交易业务。</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">【网上交易】是以基金交易为主要功能的系统，同时也提供账户查询服务。但是在网上交易的查询功能中，您只能查询到通过网上交易或者华夏基金理财中心购买的基金情况。您也可以通过该系统修改您的联系方式。</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">2、 登录方法不同</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">【基金账户查询】系统需要您在网站首页右侧点击【账户查询】登录，首次登录需要先进行注册。</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">【网上交易】系统需要您在网站首页右侧点击【网上交易】登录，首次登录需要先开通网上交易。</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">3、更新时间不同</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">【基金账户查询】和【网上交易】属于不同的系统，因此在基金持有份额、基金交易确认、基金净值等信息的更新时间上有所不同。</span></span></p>, 华夏基金网站\"基金账户查询\"和\"网上交易\"有什么区别?, 自助查询, 网上交易与账户查询区别, ghj, 否, 434280, 否, 精准问题, <自助查询>华夏基金网站\"基金账户查询\"和\"网上交易\"有什么区别?, 识别, <!网上交易近类>*[<!和近类>]*<!账户近类>*<!查询近类>*<!区别近类|!不同近类>@2#编者=\"自学习\", 不敏感, 未匹配, 180.1.2.41, 2015-08-31 21:00:54, 否, 10658087, 1, 否, 0.894, Web, 基金行业->华夏基金->多渠道应用, 2015-08-31 21:00:54, 2015-08-31 21:00:54]'";

		// 单条SQL中参数存储集合
		List<Object> lstpara = null;
		// 保存事务处理的SQL和SQL对应的参数
		List<String> lstSQL = new ArrayList<String>();
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		
		/*String sql = "insert into aaa(c) values(?)";
		lstpara = new ArrayList<Object>();
		lstpara.add(o);
		lstSQL.add(sql);
		lstLstpara.add(lstpara);
		
		sql = "insert into aaa(c) values(?)";
		lstpara = new ArrayList<Object>();
		lstpara.add(o);
		lstSQL.add(sql);
		lstLstpara.add(lstpara);*/
		
		String sql = "insert into aaa(c) values('<p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\"><font face=\"Times New Roman\"> </font>华夏基金网站【</span></span><a target=\"_blank\" href=\"http://www.chinaamc.com/wodejijin/zhcx/index.shtml\"><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">基金账户查询</span></span></a><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">】和【</span></span><a target=\"_blank\" href=\"http://www.chinaamc.com/wodejijin/index.shtml\"><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">网上交易</span></span></a><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">】是两个系统。登录【基金账户查询】系统不能办理基金交易业务，可以查看直销账户和代销账户购买和持有华夏基金的情况；登录【网上交易】可以办理基金交易业务，但只能查看直销账户（通过华夏基金理财中心或【网上交易】系统）持有基金的情况。</span></span></p><p class=\"MsoNormal\" style=\"line-height: 150%; margin: 0cm 0cm 0pt\"><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\"><span lang=\"EN-US\">（在您开通网上交易并且登录网上交易能够查看到基金账号后，</span><span style=\"mso-ascii-font-family: Calibri; mso-hansi-font-family: Calibri\">可通过点击网上交易系统中的【全面基金账户查询】直接登录网上查询系统）</span></span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">具体区别如下：</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">1、系统功能不同</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">【基金账户查询】是以查询为主要功能的系统。登录【基金账户查询】后您可以查询您持有的全部华夏旗下基金情况，还可以修改您的联系方式、订制信息等，但该系统不能办理基金申购、赎回、分红方式变更等交易业务。</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">【网上交易】是以基金交易为主要功能的系统，同时也提供账户查询服务。但是在网上交易的查询功能中，您只能查询到通过网上交易或者华夏基金理财中心购买的基金情况。您也可以通过该系统修改您的联系方式。</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">2、 登录方法不同</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">【基金账户查询】系统需要您在网站首页右侧点击【账户查询】登录，首次登录需要先进行注册。</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">【网上交易】系统需要您在网站首页右侧点击【网上交易】登录，首次登录需要先开通网上交易。</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">3、更新时间不同</span></span></p><p><span style=\"font-size: 10.5pt\"><span style=\"font-family: 宋体\">【基金账户查询】和【网上交易】属于不同的系统，因此在基金持有份额、基金交易确认、基金净值等信息的更新时间上有所不同。</span></span></p>, 华夏基金网站\"基金账户查询\"和\"网上交易\"有什么区别?, 自助查询, 网上交易与账户查询区别, ghj, 否, 434280, 否, 精准问题, <自助查询>华夏基金网站\"基金账户查询\"和\"网上交易\"有什么区别?, 识别, <!网上交易近类>*[<!和近类>]*<!账户近类>*<!查询近类>*<!区别近类|!不同近类>@2#编者=\"自学习\", 不敏感, 未匹配, 180.1.2.41, 2015-08-31 21:00:54, 否, 10658087, 1, 否, 0.894, Web, 基金行业->华夏基金->多渠道应用, 2015-08-31 21:00:54, 2015-08-31 21:00:54]')";
		lstSQL.add(sql);
		Db.update(sql);
		System.out.println("success");
		// 执行事务处理
		// Database.executeNonQueryTransactionReport(lstSQL, lstLstpara);
	}
}
