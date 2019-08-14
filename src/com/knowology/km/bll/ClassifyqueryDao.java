package com.knowology.km.bll;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibClassifyqueryDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.dal.Database;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.ReadExcel;
import com.str.NewEquals;

public class ClassifyqueryDao {
	/**
	 * 文件路径
	 */
	public static String FILE_PATH = System.getProperty("os.name")
	.toLowerCase().startsWith("win") ? 
			  Database.getCommmonLibJDBCValues("winDir") + File.separator + "classifyquery" + File.separator
			: Database.getCommmonLibJDBCValues("linDir") + File.separator + "classifyquery" + File.separator;
	public static String FILE_PATH_EXPORT = FILE_PATH + "export" + File.separator;
	/**
	 * Excel文件列数
	 */
	public static final int XLS_COL_NUM = 5;
	/**
	 * 分类问题表的内容映射
	 */
	private static Result CLASSIFYQUERY_RECORDS;
	/**
	 * applycode业务渠道字典
	 */
	private static Map<String,String> APPLYNAME_APPLYCODE_DIC = new HashMap<String,String>();
	/**
	 * 业务模型路径字典
	 */
	private static Map<String,String> SERVICE_PATH_DIC = new HashMap<String, String>();
	
	static {
		// 启动时清空文件目录
		FileUtils.deleteQuietly(new File(FILE_PATH_EXPORT));
		
		Result r = CommonLibMetafieldmappingDAO.getConfigMinValue("applyCode业务渠道编码表配置");
		if (r != null && r.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < r.getRowCount(); i++) {
				String key = r.getRows()[i].get("k") == null ? ""
						: r.getRows()[i].get("k").toString();
				String value = r.getRows()[i].get("name") == null ? "" : r
						.getRows()[i].get("name").toString();
				APPLYNAME_APPLYCODE_DIC.put(key, value);
			}
		}
		
		r = CommonLibClassifyqueryDAO.selectAllServicePath();
		if (r != null && r.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < r.getRowCount(); i++) {
				String key = r.getRows()[i].get("service") == null ? ""
						: r.getRows()[i].get("service").toString();
				String value = r.getRows()[i].get("name_path") == null ? "" : r
						.getRows()[i].get("name_path").toString();
				SERVICE_PATH_DIC.put(key, value);
			}
		}
	}
	
	/**
	 * 取出所有未分配的id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static String[] getIds(String serviceid){
		CLASSIFYQUERY_RECORDS = CommonLibClassifyqueryDAO.selectClassifyqueryByQuery(serviceid, null, null, false);
		if(CLASSIFYQUERY_RECORDS != null && CLASSIFYQUERY_RECORDS.getRowCount() > 0){
			List<String> ids = new ArrayList<String>();
			for(Map row : CLASSIFYQUERY_RECORDS.getRows()){
				ids.add(row.get("id").toString());
			}
			return (String[]) ids.toArray(new String[ids.size()]);
		}
		return null;
	}
	
	/**
	 * 查询分类问题
	 * 
	 * @param query
	 * @param city
	 * @param serviceid
	 * @param classified
	 * @param checked
	 * @param checktimeStart
	 * @param checktimeEnd
	 * @param inserttimeStart
	 * @param inserttimeEnd
	 * @return
	 */
	public static Object selectclassifyquery(String query, String city, String serviceid, String normalquery, String classified, String checked, String checktimeStart, String checktimeEnd, String inserttimeStart, String inserttimeEnd, int page, int rows){
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		
		int count = CommonLibClassifyqueryDAO.countClassifyquery(query, city, serviceid, normalquery, classified, checked, checktimeStart, checktimeEnd, inserttimeStart, inserttimeEnd);
		if(count > 0){
			jsonObj.put("total", count);
			Result result = CommonLibClassifyqueryDAO.selectClassifyquery(query, city, serviceid, normalquery, classified, checked, checktimeStart, checktimeEnd, inserttimeStart, inserttimeEnd, true, page, rows);
			if (result != null && result.getRowCount() > 0){
				for (int i = 0; i < result.getRowCount(); i++) {
					JSONObject obj = new JSONObject();
					obj.put("id", result.getRows()[i].get("id"));
					obj.put("query", result.getRows()[i].get("query"));
					obj.put("applycode", result.getRows()[i].get("applycode"));
					obj.put("applyname", result.getRows()[i].get("applyname"));
					obj.put("channel", result.getRows()[i].get("channel"));
					obj.put("province", result.getRows()[i].get("province"));
					obj.put("city", getCityName(result.getRows()[i].get("city").toString()));
					obj.put("serviceid", result.getRows()[i].get("serviceid"));
					obj.put("service", SERVICE_PATH_DIC.get(result.getRows()[i].get("service")));
					obj.put("kbdataid", result.getRows()[i].get("kbdataid"));
					obj.put("abstract", result.getRows()[i].get("abstract"));
					obj.put("classified", result.getRows()[i].get("classified"));
					obj.put("ischecked", result.getRows()[i].get("checked"));
					obj.put("checktime", result.getRows()[i].get("checktime"));
					obj.put("inserttime", result.getRows()[i].get("inserttime"));
					obj.put("workerid", result.getRows()[i].get("workerid"));
					obj.put("remark", result.getRows()[i].get("remark"));
					
					jsonArr.add(obj);
				}
			}
			jsonObj.put("rows", jsonArr);
		} else {
			jsonArr.clear();
			jsonObj.put("total", 0);
			jsonObj.put("rows", jsonArr);
		}
		
		return jsonObj;
	}
	
	
	/**
	 * 导入分配文件到数据库
	 * 
	 * @param filename 文件名称
	 * @return Json对象
	 */
	@SuppressWarnings("unchecked")
	public static Object importFile(String filename, String serviceid){
		JSONObject jsonObj = new JSONObject();
		List<Object> lineList = null; // excel文档内容
		try {
			String suffix = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
			if("xls".equals(suffix)){
				lineList = readXls(FILE_PATH, filename);
			}else if("xlsx".equals(suffix)){
				lineList = readXlsx(FILE_PATH, filename);
			}else{
				jsonObj.put("success", false);
				jsonObj.put("msg", "文件后缀名必须为xls或xlsx");
				return jsonObj;
			}
			File file = new File(FILE_PATH, filename);
			// 判断文件是否存在
			if (file.exists()) {
				// 删除文件
				file.delete();
			}
			
			int skips = 0; //跳过处理的行数
			List<String> errors = (List<String>) lineList.get(0);
			List<String> lineError = null;
			Object sre = GetSession.getSessionByKey("accessUser");
			User user = (User) sre;
//			User user = new User();
//			user.setUserID("testuser");
			CLASSIFYQUERY_RECORDS = CommonLibClassifyqueryDAO.selectClassifyqueryByQuery(serviceid, null, null, null);
			for(int i = 1; i < lineList.size(); i++){
				lineError = processLine((String[])lineList.get(i), user.getUserID());
				if(lineError != null){
					skips++;
				}
				errors.addAll(lineError);
			}
			
			if(errors != null && errors.size() > 0){
				jsonObj.put("success", false);
				JSONArray msgs = new JSONArray();
				msgs.add("处理完成，跳过" + skips + "行");
				msgs.addAll(errors);
				jsonObj.put("msg", msgs);
			} else {
				jsonObj.put("success", true);
				jsonObj.put("msg", "导入完成");
			}
		} catch (IOException e) {
			jsonObj.put("success", false);
			jsonObj.put("msg", e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jsonObj;
	}
	
	@SuppressWarnings("unchecked")
	public static File exportFile(String query, String city, String serviceid, String normalquery, String classified, String checked, String checktimeStart, String checktimeEnd, String inserttimeStart, String inserttimeEnd){
		File file = null;
		Result result = CommonLibClassifyqueryDAO.selectClassifyquery(query, city, serviceid, normalquery, classified, checked, checktimeStart, checktimeEnd, inserttimeStart, inserttimeEnd, false, 0, 0);
		if (result != null && result.getRowCount() > 0){
			List colTitle = Arrays.asList("NO", "客户问题", "应用渠道", "技术渠道", "地市");
			List text = new ArrayList();
			for (int i = 0; i < result.getRowCount(); i++) {
				List line = new ArrayList();
				line.add(i+1);
				line.add(result.getRows()[i].get("query"));
				line.add(result.getRows()[i].get("applyname"));
				line.add(result.getRows()[i].get("channel"));
				line.add(getCityName(result.getRows()[i].get("city").toString()));
				text.add(line);
			}
			String filename = (NewEquals.equals("0",checked) ? "分类问题_未审核_" : "分类问题_审核_");
			filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			ReadExcel.writeExcel(FILE_PATH_EXPORT, filename, null, null, colTitle, text);
			file = new File(FILE_PATH_EXPORT + filename + ".xls");
		}
		return file;
	}
	
	/**
	 * 问题批量自动分配<br>
	 * 
	 * <b>概要：</b>
	 * 对于未分配的问题根据NPL理解的结果个数n，
	 * 除了更新原问题分类数据行外，还得生成n-1行新问题分类数据行
	 * 
	 * @param ids 分类问题id数组
	 * @return
	 */
	public static Object autoBatchClassify(String[] ids) {
		JSONObject jsonObj = new JSONObject();
		
		int n = 0;
		for (String id : ids) {
			Result rs = CommonLibClassifyqueryDAO.selectClassifyqueryById(id);
			// 未审核的问题，才可以自动分配
			if (rs != null && rs.getRowCount() > 0 && NewEquals.equals("0",rs.getRows()[0].get("checked").toString())) {
				String query = rs.getRows()[0].get("query").toString();
				String province = (rs.getRows()[0].get("province") == null ? "" : rs.getRows()[0].get("province").toString());
				String city = rs.getRows()[0].get("city").toString();
				String workerid = ((User) GetSession.getSessionByKey("accessUser")).getUserID();
				province = "全国"; // TODO 测试用
				city = "全国";// TODO 测试用
				
				JSONObject rst = (JSONObject) QuerymanageDAO.testQuery(query, province, city);
				boolean classifed = CommonLibClassifyqueryDAO.classify(id, rst, workerid);
				if(classifed){
					n++;
				}
			}
		}
		jsonObj.put("success", true);
		int a = 0;
		if ((a = ids.length - n) > 0) {
			jsonObj.put("msg", a + "条分配失败");
		}else{
			jsonObj.put("msg", "分配成功");
		}
		
		return jsonObj;
	}
	
	
	/**
	 * 问题全量自动分配
	 * @return
	 */
	public static Object autoAllClassify(String serviceid){
		JSONObject jsonObj = new JSONObject();
		String[] ids = getIds(serviceid);
		if(ids == null){
			jsonObj.put("success", true);
			jsonObj.put("msg", "没有可分配的问题");
		} else{
			jsonObj = (JSONObject) autoBatchClassify(ids);
		}
		return jsonObj;
	}
	
	
	/**
	 * 问题批量手动分配
	 * @param ids 分类问题id数组
	 * @return
	 */
	public static Object manualClassify(String[] ids, String serviceid, String service, String kbdataid, String abs){
		JSONObject jsonObj = new JSONObject();
		int n = 0;
		for(String id : ids){
			String workerid = ((User) GetSession.getSessionByKey("accessUser")).getUserID();
			List<List<?>> listparams = new ArrayList<List<?>>();
			List<String> params = new ArrayList<String>();
			params.addAll(Arrays.asList(serviceid, service, kbdataid, abs, workerid, id));
			listparams.add(params);
			int cnt = CommonLibClassifyqueryDAO.updateClassfyquery(listparams);
			if (cnt > 0) {
				n++;
			}
		}
		jsonObj.put("success", true);
		int a = 0;
		if ((a = ids.length - n) > 0) {
			jsonObj.put("msg", a + "条分配失败");
		}else{
			jsonObj.put("msg", "分配成功");
		}
		return jsonObj;
	}
	
//	/**
//	 * 解析NPL结果为
//	 * @param json
//	 * @return
//	 */
//	private static String[] parseNPLResult(JSONObject json){
//		String[] rst = new String[4];
//		
//	}
	
	/**
	 * 批量删除
	 * @param ids 分类问题id数组
	 * @return
	 */
	public static Object batchDelete(String[] ids){
		JSONObject jsonObj = new JSONObject();
		
		int n = CommonLibClassifyqueryDAO.deleteClassifyquerys(ids);
		if (n > 0){
			int cnt = 0;
			if ( (cnt = ids.length - n) > 0){
				jsonObj.put("msg", cnt + "条删除失败");
			}else{
				jsonObj.put("msg", "删除成功");
			}
		}else{
			jsonObj.put("msg", "没有可删除的分类问题");
		}
		jsonObj.put("success", true);
		
		return jsonObj;
	}
	
	/**
	 * 批量审核
	 * @param ids 分类问题id数组
	 * @return
	 */
	public static Object batchCheck(String[] ids){
		User user = (User) GetSession.getSessionByKey("accessUser");
		return CommonLibClassifyqueryDAO.check(ids, user.getUserID());
	}


	/**
	 * 过滤检查
	 * @param excelLine 待检查的一行数据
	 * @return 检查信息，非空表示检查出错。
	 */
	@SuppressWarnings("unchecked")
	private static List<String> processLine(String[] excelLine, String workerid){
		List<String> result = new ArrayList<String>();
		String lineNo = excelLine[0];
		String query = excelLine[1];
		String applyname = excelLine[2];
		String channel = excelLine[3];
		String citynames = excelLine[4];
		
		// 1.非空检测
		if(StringUtils.isBlank(query)){
			result.add("客户问题不能为空");
			if(StringUtils.isBlank(excelLine[4])){
				result.add("来源地市不能为空");
			}
			result.add(0,"行：" + (lineNo == null ? "" : (int)(Double.parseDouble(lineNo))));
			return result;
		}
		
		// 2.垃圾问题过滤
//		if(!checkQuery()){
//			result.add("垃圾问题【客户问题：" + query + "】");
//			result.add(0,"行：" + (lineNo == null ? "" : (int)(Double.parseDouble(lineNo))));
//			return result;
//		}
		
		// 3.地市检测
		String cityChkRst = checkCityCodes(citynames);
		// 检测失败
		if(cityChkRst.startsWith("_false_")){
			result.add(StringUtils.substringAfter(cityChkRst, "_false_"));
			return result;
		}
		
		String citycodes = cityChkRst;
		
		// 4.逻辑检测
		//   1.数据库（classifyquery）是否存在该“客户问题”且为“未审核”的记录
		//   1.1 存在，比较地市， 如果导入的地市是数据库中地市的子集的话不处理（识别为已经存在的问题，
		//                      否则，对数据库该条数据进行并集处理
		//   1.2不存在.(直接插入该条数据)
		// 注意：地市是以code方式存储的，如果转换失败。这该条数据不处理，并将错误信息返回
		List<Map> rows = selectRowsByQueryAndChecked(query, false);
		if (rows.size() > 0){
			for(Map rc : rows){
				String dbCitycodes = rc.get("city").toString();
				String id = rc.get("id").toString();
				if(dbCitycodes.contains(citycodes)){
					result.add("该条数据已存在【客户问题：" + query + "】");
				} else {
					String afterCityCodes = unionCityCodes(citycodes, dbCitycodes);
					int n = CommonLibClassifyqueryDAO.updateClassifyquery(id, afterCityCodes, workerid);
					if (n < 1){
						result.add("修改失败");
					}
					
					System.out.println("导入【update方式】"); // TODO
				}
			}
			
			result.add(0,"行：" + (lineNo == null ? "" : (int)(Double.parseDouble(lineNo))));
			return result;
		}
		
		String applycode = APPLYNAME_APPLYCODE_DIC.get(applyname);
		if(StringUtils.isBlank(applycode)){
			applyname = "";
		}
		int n = CommonLibClassifyqueryDAO.insertClassifyquery(query, applycode, applyname, channel, citycodes, workerid);
		if (n < 1){
			result.add("保存失败");
			result.add(0,"行：" + (lineNo == null ? "" : (int)(Double.parseDouble(lineNo))));
		}
		
		System.out.println("导入【insert方式】"); // TODO
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static List<Map> selectRowsByQueryAndChecked(String query, Boolean checked){
		List<Map> list = new ArrayList<Map>();
		for(Map row : CLASSIFYQUERY_RECORDS.getRows()){
			String checkedStr = checked ? "1" : "0";
			if(query.equals(row.get("query").toString()) && NewEquals.equals(checkedStr,row.get("checked").toString())){
				list.add(row);
			}
		}
		return list;
	}
	
	
	/**
	 * 检测地市字段合法性
	 * @param citynames
	 * @return List[0]成功 存放返回的code字符串，失败 为空；List[1..n]存放错误信息
	 */
	private static String checkCityCodes(String citynames){
		String rst = null;
		String[] tmp = citynames.split(",");
		String codes = "";
		String code = "";
		String msg = "";
		for (int i = 0; i < tmp.length; i++) {
			if(StringUtils.isBlank(tmp[i])){
				continue;
			}
			code = QuerymanageDAO.cityNameToCityCode.get(tmp[i].trim());
			// 地市名不存在
			if(code == null){
				if("".equals(msg)){
					msg = "地市名不合法【" + tmp[i].trim();
				} else{
					msg = msg + "," + tmp[i].trim();
				}
			}else{
				if("".equals(codes)){
					codes = code;
				} else {
					codes = codes + "," + code;
				}
			}
		}
		
		if(!"".equals(msg)){
			msg += "】";
			rst = "_false_" + msg;
		}else{
			rst = unionCityCodes(codes, "");
		}
		
		return rst;
	}
	
	/**
	 * 取两个地市代码字符串的并集
	 * 
	 * @param codes1
	 * @param codes2
	 * @return
	 */
	private static String unionCityCodes(String codes1, String codes2){
		Set<String> set = new TreeSet<String>();
		String[] tmp1 = codes1.split(",");
		String[] tmp2 = codes2.split(",");
		for(String s : tmp1) {
			if(StringUtils.isNotBlank(s))
				set.add(s);
		}
		for(String s : tmp2) {
			if(StringUtils.isNotBlank(s))
				set.add(s);
		}
		return StringUtils.join(set, ",");
	}
	
	/**
	 * 获取地市名集合
	 * 
	 * @param cityCode
	 * @return
	 */
	private static String getCityName(String cityCode){
		String cityName = null;
		if(StringUtils.isBlank(cityCode)){
			return "";
		}
		String[] codes = cityCode.split(",");
		String tmp = null;
		for(int i = 0; i < codes.length; i++){
			tmp = QuerymanageDAO.cityCodeToCityName.get(codes[i].trim());
			if(cityName == null){
				cityName = tmp;
			} else {
				cityName = cityName + "," + tmp;
			}
		}
		
		return cityName;
	}
	
	public static void main(String[] args) throws IOException, SQLException {
		//xlsRead("D:\\StatisticPlanTask\\classifyquery\\test.xls");
//		List<Object> list = readXlsx(FILE_PATH, "test.xlsx");
//		printExcel(list);
		
//		JSONObject json = (JSONObject) importFile("导入客户问题模板.xls");
//		System.out.println("RESPONSE:\n" + json.toJSONString());
		System.out.println("sss00".substring(0, 3));
	}
	
	private static List<Object> readXls(String path, String name) throws IOException{
		POIFSFileSystem poifsFileSystem = null;
		HSSFWorkbook hssfWorkbook =  null;
		
		String filename = path + name;
		try {
			poifsFileSystem = new POIFSFileSystem(new FileInputStream(new File(filename)));
			hssfWorkbook =  new HSSFWorkbook(poifsFileSystem);
		} catch (IOException e) {
			throw new IOException("文件不存在【" + filename + "】");
		}
		HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(0);
		
		int rowstart = 1;
		int rowEnd = hssfSheet.getLastRowNum();
		
		if(rowEnd < 1){
			throw new IOException("文件不存在数据");
		}
		List<Object> result = new ArrayList<Object>();
		List<String> errors = new ArrayList<String>();
		result.add(errors);
		String[] line= null;
		String tmp = "";
		boolean lineAvailable = true;
		for(int i=rowstart;i<=rowEnd;i++)
		{
			lineAvailable = true; // 处理每行前，设定此行为可用
			
			HSSFRow row = hssfSheet.getRow(i);
			if(row == null) continue;
			
			line = new String[XLS_COL_NUM];
			for(int k = 0;k < XLS_COL_NUM ;k++)
			{
				HSSFCell cell = row.getCell(k);
				if(cell == null){
					tmp = "";
					line[k] = tmp;
					continue;
				}
				switch (cell.getCellType())
				{
					case HSSFCell.CELL_TYPE_NUMERIC: // 数字
						tmp = Double.toString(cell.getNumericCellValue());
						break;
					case HSSFCell.CELL_TYPE_STRING: // 字符串
						tmp = cell.getStringCellValue();
						break;
					case HSSFCell.CELL_TYPE_BLANK: // 空值
						tmp = "";
						break;
					default:
						tmp = "";
						lineAvailable = false; // 当前行存在不识别的单元格，跳过不处理
						errors.add("未知类型【行：" +i+ "，列：" +k+ "】");
						break;
				}
				line[k] = tmp;
			}
			// 该行数据为空，丢弃
			if (StringUtils.isBlank(line[1])){
				lineAvailable = false;
			}
			
			// 改行问题已存在，丢弃
			if (excelResultContains(result, line)){
				lineAvailable = false;
			}
			
			if (lineAvailable) {
				result.add(line);
			}
		}
		return result;
	}
	
	private static List<Object> readXlsx(String path, String name) throws IOException{
		XSSFWorkbook xssfWorkbook = null;
		XSSFSheet xssfSheet = null;
		
		String filename = path + name;
		try {
			xssfWorkbook = new XSSFWorkbook(new FileInputStream(new File(filename)));
			xssfSheet = xssfWorkbook.getSheetAt(0);
		} catch (IOException e) {
			throw new IOException("文件不存在【" + filename + "】");
		}

		int rowstart = 1;
		int rowEnd = xssfSheet.getLastRowNum();
		
		if(rowEnd < 1){
			throw new IOException("文件不存在数据");
		}
		List<Object> result = new ArrayList<Object>();
		List<String> errors = new ArrayList<String>();
		result.add(errors);
		String[] line = null;
		String tmp = "";
		boolean lineAvailable = true;
		for(int i=rowstart;i<=rowEnd;i++)
		{
			lineAvailable = true; // 处理每行前，设定此行为可用
			
			XSSFRow row = xssfSheet.getRow(i);
			if(row == null) continue;
			
			line = new String[XLS_COL_NUM];
			for(int k = 0;k < XLS_COL_NUM ;k++)
			{
				XSSFCell cell = row.getCell(k);
				if(cell == null){
					tmp = "";
					line[k] = tmp;
					continue;
				}
				switch (cell.getCellType())
				{
					case HSSFCell.CELL_TYPE_NUMERIC: // 数字
						tmp = Double.toString(cell.getNumericCellValue());
						break;
					case HSSFCell.CELL_TYPE_STRING: // 字符串
						tmp = cell.getStringCellValue();
						break;
					case HSSFCell.CELL_TYPE_BLANK: // 空值
						tmp = "";
						break;
					default:
						tmp = "";
						lineAvailable = false;// 当前行存在不识别的单元格，跳过不处理
						errors.add("未知类型【行：" +i+ "，列：" +k+ "】");
						break;
				}
				line[k] = tmp.trim();
			}
			
			// 该行问题数据为空，丢弃
			if (StringUtils.isBlank(line[1])){
				lineAvailable = false;
				break;
			}
			
			// 改行问题已存在，丢弃
			if (excelResultContains(result, line)){
				lineAvailable = false;
				break;
			}
			
			if (lineAvailable) {
				result.add(line);
			}
		}
		return result;
	}
	
	/**
	 * 检查Excel读入的List中是否包含当前问题
	 * @param result
	 * @param line
	 * @return
	 */
	private static boolean excelResultContains(List<Object> result, String[] line){
		boolean rtn = false;
		String[] resLine;
		for(int i = 1; i < result.size(); i++){
			resLine = (String[])result.get(i);
			if((line[1] + line[4]).equals(resLine[1] + resLine[4])){
				rtn =  true;
			}
		}
		return rtn;
	}
}
