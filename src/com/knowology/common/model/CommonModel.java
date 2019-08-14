/**  
 * @Project: JfinalDemo 
 * @Title: CommonModel.java
 * @Package com.knowology.common.model
 * @author c_wolf your emai address
 * @date 2014-9-1 下午3:42:54
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.common.model;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * 内容摘要 ：
 * 
 * 类修改者 修改日期 修改说明
 * 
 * @ClassName CommonModel
 * @Company: knowology
 * @author c_wolf your emai address
 * @date 2014-9-1 下午3:42:54
 * @version V1.0
 */

@SuppressWarnings("serial")
public class CommonModel extends Model<CommonModel> {
	public static CommonModel dao = new CommonModel();

	/**
	 * 
	 * 方法名称： select 内容摘要：根据sql语句和参数，执行查询语句，并查询出list集合 修改者名字 修改日期 修改说明
	 * 
	 * @author c_wolf 2014-9-1
	 * @param sql
	 * @param paras
	 */
	public static List<?> executeQuery(String sql, Object... paras) {
		return Db.find(sql, paras);
	}

	/**
	 * 
	 * 方法名称： executeNonQuery 内容摘要：无返回结果的sql执行。
	 * 
	 * @操作 update，insert，delete 修改者名字 修改日期 修改说明
	 * @author c_wolf 2014-9-2
	 * @param sql
	 */
	public static Boolean executeNonQuery(String sql, Object... paras) {
		int i = 0;
		try {
			i = Db.update(sql, paras);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (i > 0) {
			return true;
		}
		return false;
	}

	/**
	 * 多条sql事务处理
	 */
	@SuppressWarnings("unchecked")
	@Before(Tx.class)
	// 事务注解
	public static Boolean executeTrans(List sqls, List<List> parasList) {
		try {
			for (int i = 0; i < sqls.size(); i++) {
				String sql = sqls.get(i).toString();
				List paras = null;
				try {
					paras = parasList.get(i);
				} catch (Exception e) {
					paras = new ArrayList();
					e.printStackTrace();
				}
				// 如果paras为[[pp,dd,dd],[pp,dd,rr]]
				try {
					if (paras.get(0) instanceof List) {// 如果是同sql，多条插入，或者多条执行
						for (int q = 0; q < paras.size(); q++) {
							List par = (List) paras.get(q);
							Db.update(sql, par.toArray());
						}
					} else {
						Db.update(sql, paras.toArray());
					}
				} catch (Exception e) {
					System.out.println("事务处理发生异常" + e.getMessage());
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;// 出现异常就返回失败，否则视为成功
		}
		return true;
	}

	/**
	 * 
	 * 方法名称： executeQueryPage 内容摘要：分页查询，查询数据和总数
	 */
	@SuppressWarnings("unchecked")
	public static Page executeQueryPage(int page, int rows, String select,
			String from, Object[] paras) {
		Page pageObj = Db.paginate(page, rows, select, from, paras);
		return pageObj;
	}
}