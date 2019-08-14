package com.knowology.km.bll;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.StringUtils;

import com.knowology.Bean.User;
import com.knowology.dal.Database;
import com.knowology.km.dto.CombData;
import com.knowology.km.entity.Relatequery;
import com.knowology.km.excepion.RelateException;
import com.knowology.km.util.GetSession;

public class RelatequeryDaoImpl implements RelatequeryDao {
	
	public Relatequery getById(Long id){
		Relatequery rq = null;
		String sql = "select * from relatequery where id = ?";
		Result rs = Database.executeQuery(sql, id);
		if(rs.getRowCount() == 1){
			rq = new Relatequery();
			rq.setId((BigDecimal)rs.getRows()[0].get("id"));
			rq.setRelatequery(rs.getRows()[0].get("relatequery").toString());
			rq.setRemark(rs.getRows()[0].get("remark").toString());
		}
		
		return rq;
	}
	
	public void add(Relatequery rq) throws RelateException {
		String sql = "insert into relatequery(id, kbdataid, relatequerytokbdataid, relatequery, workerid, edittime, remark) values(seq_relatequery_id.nextval,?,?,?,?,sysdate,?)";
		User user = (User)GetSession.getSessionByKey("accessUser");
		int c = Database.executeNonQuery(sql, 
				rq.getKbdataid(), rq.getRelatequerytokbdataid(), rq.getRelatequery(), "testUser", rq.getRemark());
		if(c <= 0){
			throw new RelateException("添加失败");
		} 
	}

	public void delete(Long id) throws RelateException{
		String sql = "delete from relatequery where id = ?";
		int c = Database.executeNonQuery(sql, id);
		if(c > 0){
			throw new RelateException();
		} 
	}

	public void deleteBatch(Long[] ids) throws RelateException {
		String sql = "delete from relatequery where id = ?";
		List<String> listSql = new ArrayList<String>();
		List<List<?>> listParam = new ArrayList<List<?>>();
		for (int i = 0; i < ids.length; i++) {
			listSql.add(sql);
			List<String> params = new ArrayList<String>();
			params.add(ids[i].toString());
			listParam.add(params);
		}
		int c = Database.executeNonQueryTransaction(listSql, listParam);
		if(c <= 0){
			throw new RelateException("删除失败");
		} 
	}

	public List<Relatequery> getByKbdataidWithFilter(Long kbdataid, String brand, String relatequery, int page, int rows) {
		List<Relatequery> list = new ArrayList<Relatequery>();
		String sql = "select r.* from relatequery r, kbdata k, service s where s.serviceid = k.serviceid and k.kbdataid = r.kbdataid and r.kbdataid = ? and s.brand = ? ";
		Result rs = null;
		if(StringUtils.isNotEmpty(relatequery)){
			sql += " and r.relatequery like ?";
			sql += " order by r.edittime desc";
			rs = queryByPage(sql,
					page, rows, kbdataid, brand, "%" + relatequery + "%");
		} else {
			sql += " order by r.edittime desc";
			rs = queryByPage(sql,
					page, rows, kbdataid, brand);
		}
		
		if(rs.getRowCount() > 0){
			for(int i = 0; i < rs.getRowCount(); i++){
				Relatequery rq = new Relatequery();
				rq.setId((BigDecimal)rs.getRows()[i].get("id"));
				rq.setRelatequery(rs.getRows()[i].get("relatequery").toString());
				rq.setRemark(rs.getRows()[i].get("remark").toString());
				list.add(rq);
			}
		}
		return list;
	}
	
	public Integer count(Long kbdataid, String brand, String relatequery) {
		String sql = "select count(*) from relatequery r, kbdata k, service s where s.serviceid = k.serviceid and k.kbdataid = r.kbdataid and r.kbdataid=? and s.brand=?";
		Result rs = null;
		if(StringUtils.isNotEmpty(relatequery)){
			sql += " and r.relatequery like ?";
			rs = Database.executeQuery(sql,
					kbdataid, brand, "%" + relatequery + "%");
		} else {
			rs = Database.executeQuery(sql,
					kbdataid, brand);
		}
		if(rs != null && rs.getRowCount() > 0){
			return rs.getRowCount();
		}
		return 0;
	}

	public void update(Relatequery rq) throws RelateException {
		String sql = "update relatequery set remark = ?, workerid = ?, edittime = sysdate where id = ?";
		User user = (User)GetSession.getSessionByKey("accessUser");
		int c = Database.executeNonQuery(sql, rq.getRemark(), "testUser", rq.getId());
		if(c <= 0){
			throw new RelateException("更新失败");
		} 
	}

	public List<CombData> listRelatequreies(String brand) {
		List<CombData> list = new ArrayList<CombData>();
		
		String sql = "select k.abstract,kbdataid from service s ,kbdata k where s.serviceid = k.serviceid and s.brand = ?";
		Result rs = Database.executeQuery(sql, brand);
		if(rs != null && rs.getRowCount() > 0){
			for(int i = 0; i < rs.getRowCount(); i++){
				String key = rs.getRows()[i].get("abstract").toString();
				if(StringUtils.isEmpty(key)){
					key = "";
				}else{
					key = key.split(">")[1];
				}
				CombData cd = new CombData(key, rs.getRows()[i].get("kbdataid"));
				list.add(cd);
			}
		}
		
		return list;
	}
	
	/**
	 * 分页查询
	 * @param sql
	 * @param page
	 * @param rows
	 * @param obj
	 * @return
	 */
	private Result queryByPage(String sql,int page, int rows, Object...obj){  
    	StringBuilder sb = new StringBuilder();
    	sb.append("SELECT * FROM ");
    	sb.append(" (SELECT A.*, ROWNUM RN ");
    	sb.append(" FROM (" + sql + ") A ");
    	sb.append(" WHERE ROWNUM <=" + (rows*page) + ") ");
    	sb.append(" WHERE RN >=" + (rows*(page-1) + 1));
    	
        return Database.executeQuery(sql, obj);
    }
}
