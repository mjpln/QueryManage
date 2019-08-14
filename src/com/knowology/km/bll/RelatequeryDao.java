package com.knowology.km.bll;

import java.util.List;

import com.knowology.km.dto.CombData;
import com.knowology.km.entity.Relatequery;
import com.knowology.km.excepion.RelateException;

public interface RelatequeryDao {
	
	Relatequery getById(Long id);
	
	List<Relatequery> getByKbdataidWithFilter(Long kbdataid, String brand, String relatequery, int page, int rows);
	
	Integer count(Long kbdataid, String brand, String relatequery);
	
	void delete(Long id) throws RelateException;
	
	void deleteBatch(Long[] ids) throws RelateException;
	
	void add(Relatequery rq) throws RelateException;
	
	void update(Relatequery rq) throws RelateException;
	
	List<CombData> listRelatequreies(String brand);
	
}
