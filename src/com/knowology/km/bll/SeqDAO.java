package com.knowology.km.bll;

import java.sql.SQLException;

import javax.servlet.jsp.jstl.sql.Result;

import com.knowology.km.dal.Database;
import com.knowology.km.util.getConfigValue;

public class SeqDAO {
	// @已经转移到CommonLib工程中, 此处废用改为bll/PKNextVal.getNextVal
	
	// / <summary>
	// / 获取序列的下一个值
	// / </summary>
	// / <param name="seqName">序列名</param>
	// / <returns>下一个值</returns>
	
	public static int GetNextVal(String seqName) {
		String sql = null;
		// 组成sql语句
		if(getConfigValue.isMySQL){
			sql = "select " +"nextval('"+seqName+"') as seq";
		}else{
			sql = "select " + seqName + ".nextval  seq from dual";
		}
		
		Result rs = null;
		try {
			rs = Database.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (rs == null || rs.getRows().length == 0) {
			return Integer.MAX_VALUE;
		}
		return Integer.parseInt(rs.getRows()[0].get("seq").toString());
	}

}
