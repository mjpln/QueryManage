package com.knowology.km.bll;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibPatternkeyDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.bll.CommonLibQuestionUploadDao;
import com.knowology.dal.Database;
import com.knowology.km.NLPAppWS.AnalyzeEnterDelegate;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.util.GetLoadbalancingConfig;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.getConfigValue;
import com.knowology.km.util.getServiceClient;

public class QuestionUploadDao {
	// 日志
	public static Logger logger = Logger.getLogger("querymanage");
	// 下载路径
	public static String regressTestPath = System.getProperty("os.name")
			.toLowerCase().startsWith("win") ? Database
			.getCommmonLibJDBCValues("winDir") : Database
			.getCommmonLibJDBCValues("linDir");

		
	public static Result LocalRs = null;
	// 省<中文,编码>
	public static Map<String, String> ProvinceLocalMap = new HashMap<String, String>();
	// 市<中文,编码>
	public static Map<String, String> CityLocalMap = new HashMap<String, String>();
	// 地市<编码,中文>
	public static Map<String, String> LocalMap = new HashMap<String, String>();

	// 静态代码块
	static {
		// 地市map<编码,中文>中文取最短
		LocalRs = CommonLibQuestionUploadDao.createLocal();
		if (LocalRs != null && LocalRs.getRowCount() > 0) {
			for (int i = 0; i < LocalRs.getRowCount(); i++) {
				if (LocalMap.containsKey(LocalRs.getRows()[i].get("id")
						.toString().replace(" ", ""))) {
					// 判断长度
					if (LocalRs.getRows()[i].get("province").toString()
							.replace(" ", "").length() < LocalMap.get(
							LocalRs.getRows()[i].get("id").toString().replace(
									" ", "")).toString().replace(" ", "")
							.length()) {
						LocalMap.put(LocalRs.getRows()[i].get("id").toString()
								.replace(" ", ""), LocalRs.getRows()[i].get(
								"province").toString().replace(" ", ""));
					}
				} else {
					LocalMap.put(LocalRs.getRows()[i].get("id").toString()
							.replace(" ", ""), LocalRs.getRows()[i].get(
							"province").toString().replace(" ", ""));
				}
			}
			// 特殊处理
			LocalMap.put("433100", "自治州");
		}

		
		// 省<中文,编码>
		LocalRs = null;
		LocalRs = CommonLibQuestionUploadDao.createLocalProvince();
		if (LocalRs != null && LocalRs.getRowCount() > 0) {
			for (int i = 0; i < LocalRs.getRowCount(); i++) {
				ProvinceLocalMap.put(LocalRs.getRows()[i].get("province")
						.toString().replace(" ", ""), LocalRs.getRows()[i].get(
						"id").toString().replace(" ", ""));
			}
		}

		// 市<中文,编码>
		LocalRs = null;
		LocalRs = CommonLibQuestionUploadDao.createLocalCity();
		if (LocalRs != null && LocalRs.getRowCount() > 0) {
			for (int i = 0; i < LocalRs.getRowCount(); i++) {
				CityLocalMap.put(LocalRs.getRows()[i].get("province")
						.toString().replace(" ", ""), LocalRs.getRows()[i].get(
						"id").toString().replace(" ", ""));
			}
		}

		// for (Entry<String, String> map : localMap.entrySet()){
		// System.out.println("key:"+map.getKey()+" value:"+map.getValue());
		// }
	}

	/**
	 * 获取省份
	 * 
	 * @return
	 */
	public static Object selProvince() {
		// 获取session中的用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取角色信息
		String rolename = user.getRoleList().get(0).getRoleName().toString()
				.replace("管理员", "");
		// 获取business
		String bsname = user.getIndustryOrganizationApplication().split("->")[1];
		// 定义返回的json串
		JSONArray jsonArr = new JSONArray();
		// 获取数据源
		Result rs = CommonLibQuestionUploadDao.selProvince(rolename, bsname);
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// jsonObj.put("total", rs.getRowCount());
			// 遍历循环数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 定义json对象
				JSONObject obj = new JSONObject();
				// 生成obj对象
				obj.put("id", rs.getRows()[i].get("id"));
				obj.put("province", rs.getRows()[i].get("province"));
				// 将生成的对象放入jsonArr数组中
				jsonArr.add(obj);
			}
		}
		return jsonArr;
	}

	/**
	 * 根据省份获取城市
	 * 
	 * @param id
	 *            省份id
	 * @return
	 */
	public static Object getCity(String id) {
		// 定义返回的json串
		JSONArray jsonArr = new JSONArray();
		// 获取数据源
		Result rs = CommonLibQuestionUploadDao.getCity(id);
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// jsonObj.put("total", rs.getRowCount());
			// 遍历循环数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 定义json对象
				JSONObject obj = new JSONObject();
				// 生成obj对象
				obj.put("id", rs.getRows()[i].get("id"));
				obj.put("city", rs.getRows()[i].get("city"));
				// 将生成的对象放入jsonArr数组中
				jsonArr.add(obj);
			}
		}
		return jsonArr;
	}

	/**
	 * 分页查询所有问法
	 * 
	 * @param page
	 *            页码
	 * @param rows
	 *            每页显示行数
	 * @param question
	 *            问法
	 * @param other
	 *            同义问法
	 * @param starttime
	 *            起始时间
	 * @param endtime
	 *            结束时间
	 * @param status
	 *            状态
	 * @param selProvince
	 *            省份
	 * @param selCity
	 *            城市
	 * @param hot
	 *            是否热点问法
	 * @param hot2
	 * @param pid
	 *            问法id
	 * @return
	 */
	public static Object gethotquestion(int page, int rows, String question,
			String other, String starttime, String endtime, String username,
			String status, String selProvince, String selCity, String hot,
			String hot2, Integer pid, String ids) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		// 获取用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();

		// 中文->编码
		selProvince = ProvinceLocalMap.get(selProvince);
		selCity = CityLocalMap.get(selCity);
		// 获取该用户资源权限
		HashMap<String, ArrayList<String>> locPer = CommonLibPermissionDAO
				.resourseAccess(userid, "hotquestion", "S");

		ArrayList<String> locArr = locPer.get("地市");
		// 取出资源中的所有省拼成字符串
		String locString = "";
		if (locArr != null && locArr.size() > 0) {
			for (String loc : locArr) {
				if (loc.contains("0000")) {
					locString = locString + "'" + loc + "',";
				}
			}
			locString = locString.substring(0, locString.length() - 1);
		}

		// 获取返回的数据源
		Result rs = CommonLibQuestionUploadDao.gethotquestion(question, other,
				starttime, endtime, username, status, selProvince, selCity,
				hot, hot2, pid, user, ids, locString);
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 将总条数放入jsonObj的total对象中
			jsonObj.put("total", rs.getRows()[0].get("total"));

			// 获取数据源
			rs = CommonLibQuestionUploadDao.gethotquestion(page, rows,
					question, other, starttime, endtime, username, status,
					selProvince, selCity, hot, hot2, pid, user, locString);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成对象
					obj.put("pid", rs.getRows()[i].get("pid"));
					obj.put("sid", rs.getRows()[i].get("sid"));
					obj.put("question", rs.getRows()[i].get("question"));
					obj.put("other", rs.getRows()[i].get("other"));
					obj.put("uploadtime", rs.getRows()[i].get("uploadtime"));
					obj.put("user", rs.getRows()[i].get("username"));
					obj.put("province", LocalMap.get(rs.getRows()[i]
							.get("province")));
					obj.put("city", LocalMap.get(rs.getRows()[i].get("city")));
					obj.put("result", rs.getRows()[i].get("result"));
					obj.put("status", rs.getRows()[i].get("status"));
					obj.put("hot", rs.getRows()[i].get("hot"));
					obj.put("hot2", rs.getRows()[i].get("hot2"));
					obj.put("reason", rs.getRows()[i].get("reason"));
					obj.put("solution", rs.getRows()[i].get("solution"));
					obj.put("flag", rs.getRows()[i].get("flag"));
					// 将生成的兑现放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		} else {
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 清空jsonArr数组
			jsonArr.clear();
			// 将jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	/**
	 * 分页查询热点问法
	 * 
	 * @param page
	 *            页码
	 * @param rows
	 *            每页显示行数
	 * @param question
	 *            问法
	 * @param starttime
	 *            开始时间
	 * @param endtime
	 *            结束时间
	 * @param status
	 *            状态
	 * @param selProvince
	 *            省份
	 * @param selCity
	 *            城市
	 * @param hot
	 *            是否热点问法
	 * @param hot2
	 * @param pid
	 *            问法id
	 * @return
	 */
	public static Object gethotquestion2(int page, int rows, String question,
			String starttime, String endtime, String status,
			String selProvince, String selCity, String hot, String hot2,
			Integer pid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		// 获取用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取用户id
		String userid = user.getUserID();

		// 获取用户资源配置
		HashMap<String, ArrayList<String>> locPer = CommonLibPermissionDAO
				.resourseAccess(userid, "hotquestion", "S");

		ArrayList<String> locArr = locPer.get("地市");

		// 获取地市中的省份编码并拼成字符串
		String locString = "";
		if (locArr != null && locArr.size() > 0) {
			for (String loc : locArr) {
				if (loc.contains("0000")) {
					locString = locString + "'" + loc + "',";
				}
			}
			// 去除最后一个","
			locString = locString.substring(0, locString.length() - 1);
		}

		// 获取返回的数据源
		Result rs = CommonLibQuestionUploadDao.gethotquestion2(question,
				starttime, endtime, status, selProvince, selCity, hot, hot2,
				pid, user, locString);
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 将总条数放入jsonObj的total对象中
			jsonObj.put("total", rs.getRowCount());

			// 省市中文转化为编码
			selProvince = ProvinceLocalMap.get(selProvince);
			selCity = CityLocalMap.get(selCity);

			// 获取数据源
			rs = CommonLibQuestionUploadDao.gethotquestion2(page, rows,
					question, starttime, endtime, status, selProvince, selCity,
					hot, hot2, pid, user, locString);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成对象
					obj.put("pid", rs.getRows()[i].get("pid"));
					// obj.put("pid", rs.getRows()[i].get("pid"));
					// obj.put("sid", rs.getRows()[i].get("sid"));
					obj.put("question", rs.getRows()[i].get("question"));
					// obj.put("other", rs.getRows()[i].get("other"));
					// obj.put("uploadtime", rs.getRows()[i].get("uploadtime"));
					// obj.put("user", rs.getRows()[i].get("username"));
					// obj.put("province", rs.getRows()[i].get("province"));
					// obj.put("city", rs.getRows()[i].get("city"));
					// obj.put("result", rs.getRows()[i].get("result"));
					obj.put("status", rs.getRows()[i].get("status"));
					obj.put("hot", rs.getRows()[i].get("hot"));
					// obj.put("hot2", rs.getRows()[i].get("hot2"));
					obj.put("reason", rs.getRows()[i].get("reason"));
					obj.put("solution", rs.getRows()[i].get("solution"));
					// 将生成的兑现放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		} else {
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 清空jsonArr数组
			jsonArr.clear();
			// 将jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	/**
	 * 设置热点问法
	 * 
	 * @param ids
	 * @return
	 */
	public static Object setAttr(String ids) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获取用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		try {
			// 获取用户角色
			String rolename = user.getRoleList().get(0).getRoleName()
					.toString();
			// 获取business
			String bsname = user.getIndustryOrganizationApplication().split("->")[1] + "管理员";
			// 电信集团管理员
			if (rolename.equals(bsname)) {
				// 事务处理
				int result = CommonLibQuestionUploadDao.setAttr(ids);
				// 判断事务处理结果
				if (result > 0) {
					// 将true放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将确认成功信息放入jsonObj的msg对象中
					jsonObj.put("msg", "确认成功!");
				} else {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将确认失败信息放入jsonObj的msg对象中
					jsonObj.put("msg", "确认失败!");
				}
			} else {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将提示信息放入jsonObj的msg对象中
				jsonObj.put("msg", "无权限操作，请联系" + bsname + "!");
			}
			return jsonObj;
		} catch (SQLException e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将确认失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "确认失败!");
		}
		return jsonObj;
	}

	/**
	 * 将Excel文件中的数据导入到数据库中
	 * 
	 * @param serviceid参数业务id
	 * @param service参数业务
	 * @param fileName参数文件名称
	 * @return 导入返回的json串
	 * @throws SQLException
	 */
	public static Object ImportExcel(String fileName) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获取上传文件的路径
		String pathName = regressTestPath + File.separator + fileName;
		// 获取上传文件的file
		File file = new File(pathName);
		// 获取上传文件的类型
		String extension = fileName.lastIndexOf(".") == -1 ? "" : fileName
				.substring(fileName.lastIndexOf(".") + 1);
		// 定义存放读取Excel文件中的内容的集合
		List<List<Object>> comb = new ArrayList<List<Object>>();
		// 判断上传文件的类型来调用不同的读取Excel文件的方法
		if ("xls".equalsIgnoreCase(extension)) {
			// 读取2003的Excel方法
			comb = read2003Excel(file);
		} else if ("xlsx".equalsIgnoreCase(extension)) {
			// 读取2007的Excel方法
			comb = read2007Excel(file);
		}
		// 删除文件
		file.delete();

		// 获取用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 调用新增热点问法的方法，并返回事务处理结果
		int[] count = CommonLibQuestionUploadDao.InsertHotQuestion(comb, user,
				ProvinceLocalMap, CityLocalMap);

		// 判断事务处理结果
		if (count[0] >= 0) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将导入成功信息放入jsonObj的msg对象中
			String message = "成功导入" + count[1] + "条！";
			if (count[5] + count[2] + count[3] + count[4] + count[6] > 0) {
				int total = count[5] + count[2] + count[3] + count[4]
						+ count[6];
				message = message + "(导入失败" + total + "条，";
				message += "失败原因：";
				if (count[2] > 0) {
					message += "重复、";
				}
				if (count[3] > 0) {
					message += "标准问法缺失、";
				}
				if (count[4] > 0) {
					message += "同义问法缺失、";
				}
				if (count[5] > 0) {
					message += "省份缺失、";
				}
				if (count[6] > 0) {
					message += "归属城市填写有误、";
				}
				message = message.substring(0, message.length() - 1);
				message += ")";
			}
			// 将信息放入jsonObj
			jsonObj.put("msg", message);
		} else {
			// 事务处理失败
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将导入失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "导入失败!");
		}
		return jsonObj;
	}

	/**
	 * 读取数据库，生成Excel文件，返回文件的路径
	 * 
	 * @param kbdataid参数摘要id
	 * @return 生成文件的路径
	 */
	public static Object ExportExcel() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义存放生成Excel文件的每一行内容的集合
		List<String> rowList = new ArrayList<String>();
		// 定义存放生成Excel文件的所有内容的集合
		List<List<String>> attrinfoList = new ArrayList<List<String>>();
		// 定义存放属性名称对应列值的数组
		rowList.add("标准问法(必填)");
		rowList.add("同义问法(必填)");
		rowList.add("归属省份(必选)");
		rowList.add("归属城市");

		// 将每一行的内容就会放入所有内容的集合中
		attrinfoList.add(rowList);
		// 定义文件的路径
		String pathName = "";
		// 定义文件名称
		pathName = pathName + "模板.xls";
		// 调用生成Excel2003的方法，并返回生成Excel文件的路径
		creat2003ExcelModel(attrinfoList, pathName);

		// 定义文件对象
		File file = new File(regressTestPath + File.separator + pathName);
		// 判断文件是否存在
		if (file.exists()) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将文件路径放入jsonObj的path对象中
			jsonObj.put("path", pathName);
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 * 读取 office 2003 excel
	 * 
	 * @param file参数文件
	 * @return 读取Excel文件内容的集合
	 */
	private static List<List<Object>> read2003Excel(File file) {
		
		List<List<Object>> list = new LinkedList<List<Object>>();
		try {
			HSSFWorkbook hwb = new HSSFWorkbook(new FileInputStream(file));
			HSSFSheet sheet = hwb.getSheetAt(0);
			Object value = null;
			HSSFRow row = null;
			HSSFCell cell = null;

			// 读取第一行
			row = sheet.getRow(0);
			List<Object> linked = new LinkedList<Object>();
			if (row != null) {
				for (int j = 0; j <= row.getLastCellNum(); j++) {
					cell = row.getCell(j);
					if (cell == null) {
						continue;
					}
					value = cell.getStringCellValue().trim();
					linked.add(value);
				}
				list.add(linked);
			}
			int count = linked.size();
			// 读取第一行以下的部分
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				linked = new LinkedList<Object>();
				for (int j = 0; j < count; j++) {
					cell = row.getCell(j);
					if (cell == null) {
						linked.add("");
					} else {
						switch (cell.getCellType()) {
						case XSSFCell.CELL_TYPE_STRING:
							value = cell.getStringCellValue().trim();
							break;
						case XSSFCell.CELL_TYPE_BLANK:
							value = "";
							break;
						default:
							value = cell.toString();
						}
						linked.add(value);
					}
				}
				list.add(linked);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 读取Office 2007 excel
	 * 
	 * @param file参数文件
	 * @return 读取Excel文件内容的集合
	 */
	private static List<List<Object>> read2007Excel(File file) {
		List<List<Object>> list = new LinkedList<List<Object>>();
		try {
			// 构造 XSSFWorkbook 对象，strPath 传入文件路径
			XSSFWorkbook xwb = new XSSFWorkbook(new FileInputStream(file));
			XSSFSheet sheet = xwb.getSheetAt(0);
			Object value = null;
			XSSFRow row = null;
			XSSFCell cell = null;
			// 读取第一行
			row = sheet.getRow(0);
			List<Object> linked = new LinkedList<Object>();
			if (row != null) {
				for (int j = 0; j <= row.getLastCellNum(); j++) {
					cell = row.getCell(j);
					if (cell == null || "".equals(cell)) {
						continue;
					}
					linked.add(cell.getStringCellValue().trim());
				}
				list.add(linked);
			}
			int count = linked.size();
			// 读取第一行以下的部分
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				linked = new LinkedList<Object>();
				for (int j = 0; j < count; j++) {
					cell = row.getCell(j);
					if (cell == null) {
						linked.add("");
					} else {
						switch (cell.getCellType()) {
						case XSSFCell.CELL_TYPE_STRING:
							value = cell.getStringCellValue().trim();
							break;
						case XSSFCell.CELL_TYPE_BLANK:
							value = "";
							break;
						default:
							value = cell.toString();
						}
						linked.add(value);
					}
				}
				list.add(linked);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 创建2003版本的Excel文件(用于下载模板)
	 * 
	 * @param attrinfo参数要生成文件的集合
	 * @param pathName参数文件路径
	 */
	private static void creat2003ExcelModel(List<List<String>> attrinfo,
			String pathName) {
		try {
			HSSFWorkbook workBook = new HSSFWorkbook();// 创建 一个excel文档对象
			HSSFSheet sheet = workBook.createSheet();// 创建一个工作薄对象
			// 设置列宽
			sheet.setColumnWidth(0, 30 * 256);
			sheet.setColumnWidth(1, 50 * 256);
			sheet.setColumnWidth(2, 20 * 256);
			sheet.setColumnWidth(3, 20 * 256);
			HSSFCellStyle style = workBook.createCellStyle();// 创建样式对象
			HSSFFont font = workBook.createFont();// 创建字体对象
			font.setFontHeightInPoints((short) 12);// 设置字体大小
			style.setFont(font);// 将字体加入到样式对象
			// 产生表格标题行
			for (int i = 0; i < attrinfo.size(); i++) {
				HSSFRow row = sheet.createRow(i);
				List<String> c = attrinfo.get(i);
				for (int j = 0; j < c.size(); j++) {
					HSSFCell cell = row.createCell(j);// 创建单元格
					cell.setCellValue(c.get(j));// 写入当前值
					cell.setCellStyle(style);// 应用样式对象
				}
			}
			// 获取用户信息
			Object sre = GetSession.getSessionByKey("accessUser");
			User user = (User) sre;
			// 获取角色信息
			String rolename = user.getRoleList().get(0).getRoleName()
					.toString();
			// 获取business
			String bsname = user.getIndustryOrganizationApplication().split("->")[1] + "管理员";
			
			// 普通用户
			if (!bsname.equals(rolename) && !"云平台组长".equals(rolename)) {
				// 生成下拉列表
				// 只对(x，2)单元格有效
				CellRangeAddressList regions = new CellRangeAddressList(1,
						2000, 2, 2);
				// 生成下拉框内容
				DVConstraint constraint = DVConstraint
						.createExplicitListConstraint(new String[] { rolename
								.substring(0, rolename.length() - 3) });
				// 绑定下拉框和作用区域
				HSSFDataValidation data_validation = new HSSFDataValidation(
						regions, constraint);
				// 对sheet页生效
				sheet.addValidationData(data_validation);
			} else {
				// 生成下拉列表
				// 只对(x，2)单元格有效
				CellRangeAddressList regions = new CellRangeAddressList(1,
						2000, 2, 2);
				// 获取数据源
				Result rs = CommonLibQuestionUploadDao.selProvince(rolename,
						bsname);
				// 生成下拉框内容
				String[] pp = new String[rs.getRowCount()];
				// 判断数据源不为null且含有数据
				if (rs != null && rs.getRowCount() > 0) {
					// 遍历循环数据源
					for (int i = 0; i < rs.getRowCount(); i++) {
						pp[i] = rs.getRows()[i].get("province").toString();
					}
				}
				DVConstraint constraint = DVConstraint
						.createExplicitListConstraint(pp);
				// 绑定下拉框和作用区域
				HSSFDataValidation data_validation = new HSSFDataValidation(
						regions, constraint);
				// 对sheet页生效
				sheet.addValidationData(data_validation);
			}
			// 文件流
			FileOutputStream os = new FileOutputStream(regressTestPath
					+ File.separator + pathName);
			workBook.write(os);// 将文档对象写入文件输出流
			os.close();// 关闭文件输出流
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成2003版本excel文件，用于问法下载
	 * @param attrinfo
	 * @param pathName
	 */
	private static void creat2003Excel(List<List<String>> attrinfo,
			String pathName) {
		try {
			HSSFWorkbook workBook = new HSSFWorkbook();// 创建 一个excel文档对象
			HSSFSheet sheet = workBook.createSheet();// 创建一个工作薄对象
			// 设置列宽
			sheet.setColumnWidth(0, 30 * 256);
			sheet.setColumnWidth(1, 50 * 256);
			sheet.setColumnWidth(2, 80 * 256);
			sheet.setColumnWidth(3, 20 * 256);
			sheet.setColumnWidth(4, 20 * 256);
			sheet.setColumnWidth(5, 20 * 256);
			sheet.setColumnWidth(6, 20 * 256);
			sheet.setColumnWidth(7, 20 * 256);
			sheet.setColumnWidth(8, 20 * 256);
			sheet.setColumnWidth(9, 20 * 256);
			sheet.setColumnWidth(10, 20 * 256);
			HSSFCellStyle style = workBook.createCellStyle();// 创建样式对象
			HSSFFont font = workBook.createFont();// 创建字体对象
			font.setFontHeightInPoints((short) 12);// 设置字体大小
			style.setFont(font);// 将字体加入到样式对象
			// 产生表格标题行
			for (int i = 0; i < attrinfo.size(); i++) {
				HSSFRow row = sheet.createRow(i);
				List<String> c = attrinfo.get(i);
				for (int j = 0; j < c.size(); j++) {
					HSSFCell cell = row.createCell(j);// 创建单元格
					cell.setCellValue(c.get(j));// 写入当前值
					cell.setCellStyle(style);// 应用样式对象
				}
			}
			// 文件流
			FileOutputStream os = new FileOutputStream(regressTestPath
					+ File.separator + pathName);
			workBook.write(os);// 将文档对象写入文件输出流
			os.close();// 关闭文件输出流
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存报错信息
	 * 
	 * @param sid
	 * @param reason
	 * @param solution
	 * @return
	 */
	public static Object doSaveReport(String ids, String reason, String solution) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获取用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		
		if (reason != null && !reason.equals("")) {
			// 获取角色信息
			String rolename = user.getRoleList().get(0).getRoleName()
					.toString();
			// 获取business信息
			String bsname = user.getIndustryOrganizationApplication().split("->")[1] + "管理员";

			// 报错权限
			if (!"云平台组长".equals(rolename)) {
				// if(rolename.equals(bsname)){
				// 执行SQL语句，绑定事务，返回事务处理结果
				int c = CommonLibQuestionUploadDao.doSaveReport(ids, reason,
						solution);
				// 判断事务处理结果
				if (c > 0) {
					// 将true放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将删除成功信息放入jsonObj的msg对象中
					jsonObj.put("msg", "成功!");
				} else {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将删除失败信息放入jsonObj的msg对象中
					jsonObj.put("msg", "失败!");
				}
			} else {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将删除失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "无操作权限，请联系" + bsname + "！");
			}
		} else {
			// 获取角色信息
			String rolename = user.getRoleList().get(0).getRoleName()
					.toString();
			// 获取business
			String bsname = user.getIndustryOrganizationApplication().split("->")[1] + "管理员";
			if ("云平台组长".equals(rolename) || rolename.equals(bsname)) {
				// 执行SQL语句，绑定事务，返回事务处理结果
				int c = CommonLibQuestionUploadDao.doSaveReport(ids, reason,
						solution);
				// 判断事务处理结果
				if (c > 0) {
					// 将true放入jsonObj的success对象中
					jsonObj.put("success", true);
					// 将删除成功信息放入jsonObj的msg对象中
					jsonObj.put("msg", "成功!");
				} else {
					// 将false放入jsonObj的success对象中
					jsonObj.put("success", false);
					// 将删除失败信息放入jsonObj的msg对象中
					jsonObj.put("msg", "失败!");
				}
			} else {
				// 将false放入jsonObj的success对象中
				jsonObj.put("success", false);
				// 将删除失败信息放入jsonObj的msg对象中
				jsonObj.put("msg", "无操作权限，请联系云平台组长！");
			}
		}
		return jsonObj;
	}

	/**
	 * 批量理解
	 * 
	 * @param attrArr
	 * @param url
	 * @return
	 * @throws SQLException
	 */
	public static Object understand(String attrArr, String url) {
		// 需要理解的问法按条分割
		String[] attrs = attrArr.split("xxxxxnixxxxx");
		for (int i = 0; i < attrs.length; i++) {
			// 单条问法信息分割
			String[] allattrs = attrs[i].split("@-@");
			// 获取用户信息
			Object sre = GetSession.getSessionByKey("accessUser");
			User user = (User) sre;
			// 	获取用户id
			String userid = user.getUserID();
			// 四层结构
			String servicetype = user.getIndustryOrganizationApplication();
			// 角色信息
			String rolename = user.getRoleList().get(0).getRoleName()
					.toString();
			// 查询配置
			Result rs2 = UserOperResource.getConfigValue("简要分析服务地址配置", "问法上传专用服务");
			if (rs2 != null && rs2.getRowCount() > 0) {
				// 获取配置表的ip
				url = rs2.getRows()[0].get("name").toString();
			}
			// url ="http://172.16.1.6:8082/NLPAppWS/AnalyzeEnterPort?wsdl";
			// ="http://222.186.101.213:8282/NLPAppWS/AnalyzeEnterPort?wsdl";
//			url = "http://180.153.59.28:8082/NLPAppWS/AnalyzeEnterPort?wsdl";
			// 渠道
			String channel = "Web";
			String applycode = "wenfa";
			
			Result rs3 = UserOperResource.getConfigValue("问题库批量理解接口入参配置", servicetype);
			if (rs3 != null && rs3.getRowCount() > 0) {
				Map<String ,String> caMap = new HashMap<String, String>();
				for (int j = 0;j < rs3.getRowCount();j++){
					caMap.put(rs3.getRows()[j].get("name").toString().split("->")[0]
					        , rs3.getRows()[j].get("name").toString().split("->")[1]);
				}
				if (caMap.containsKey("applycode")){
					applycode = caMap.get("applycode");
				}
				if (caMap.containsKey("channel")){
					channel = caMap.get("channel");
				}
			}
			// 处理字符串
			String question = allattrs[2].replace("\n", " ");
			String question2 = allattrs[3].replace("\n", " ");

			String province = allattrs[5];// 省份
			String city = allattrs[4];// 城市

			// 如果没有城市，则选择省会
			if (city.equals("null") || city == null || city.equals("")) {
				String nProvince = ProvinceLocalMap.get(province).replace(
						"0000", "0100");
				Result rs = CommonLibQuestionUploadDao.getCapital(nProvince);
				city = "";
				if (rs != null && rs.getRowCount() > 0) {
					for (int j = 0; j < rs.getRowCount(); j++) {
						if (city.length() < rs.getRows()[j].get("city")
								.toString().length()) {
							city = rs.getRows()[j].get("city").toString();
						}
					}
				}
			}
			// 直辖市处理
			if (province.equals("北京")) {
				city = "北京";
			} else if (province.equals("上海")) {
				city = "上海";
			} else if (province.equals("天津")) {
				city = "天津";
			} else if (province.equals("重庆")) {
				city = "重庆";
			} else if (province.equals("电渠")) {
				city = "电渠";
			} else if (province.equals("集团")) {
				// 集团做特殊处理，算电渠
				province = "电渠";
				city = "电渠";
//				city = "集团";
			}
			// 去除后缀
			city = city.replace("市", "").replace("省", "");

			// 标准问理解
			Object object = KAnalyzeByFistResult(userid, servicetype, channel,
					question, url, province, city, "热点问法测试", applycode);
			// 同义问理解
			Object object2 = KAnalyzeByFistResult(userid, servicetype, channel,
					question2, url, province, city, "热点问法测试", applycode);

			JSONObject jsonObj = (JSONObject) object;
			JSONObject jsonObj2 = (JSONObject) object2;
			// 获取同义问答案
			String answer1 = jsonObj.getJSONArray("result").getJSONObject(0)
					.getString("answer");
			// 获取同义问答案
			String answer2 = jsonObj2.getJSONArray("result").getJSONObject(0)
					.getString("answer");

			// 去除<html>标签
			answer1 = HtmlText(answer1);
			answer2 = HtmlText(answer2);
			
			// 答案过长处理
			if (answer1.length() > 950) {
				answer1 = answer1.substring(0, 900) + "...";
			}
			if (answer2.length() > 950) {
				answer2 = answer2.substring(0, 900) + "...";
			}
			// 拼成最后存入数据库的字符串
			String result = "<b style=\"color:red;\">标准问法理解结果：</b><br>"
					+ answer1
					+ "<br><br><b style=\"color:red;\">同义问法理解结果：</b><br>"
					+ answer2;
			String flag = "0";
			if (!answer1.equals(answer2)) {
				// 标准问/同义问理解不一致情况
				flag = "1";
			} else {
				// 云平台组长理解时，理解一致则自动审核
				if ("云平台组长".equals(rolename) && !answer1.contains("您可能关心的问题是")
						&& !answer1.contains("小知还在成长")) {
					int cc = CommonLibQuestionUploadDao.autosol(rolename,
							allattrs[1]);
				}
			}
			// 更新到数据库
			int c = CommonLibQuestionUploadDao.understand(result, allattrs[1],
					flag);
		}
		return null;
	}

	/**
	 * 获取热点问法的同义问法
	 * 
	 * @param question
	 * @param pid
	 * @param status
	 * @param rows
	 * @param page
	 * @return
	 */
	public static Object getsonquestion(String question, String pid,
			String status, int rows, int page) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		// 获取用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取用户id
		String userid = user.getUserID();
		// 获取用户资源
		HashMap<String, ArrayList<String>> locPer = CommonLibPermissionDAO
				.resourseAccess(userid, "hotquestion", "S");
		// 获取地市
		ArrayList<String> locArr = locPer.get("地市");

		String locString = "";
		
		// 获取其中省份编码，拼成字符串
		if (locArr != null && locArr.size() > 0) {
			for (String loc : locArr) {
				if (loc.contains("0000")) {
					locString = locString + "'" + loc + "',";
				}
			}
			locString = locString.substring(0, locString.length() - 1);
		}

		// 获取返回的数据源
		Result rs = CommonLibQuestionUploadDao.getsonquestion(question, pid,
				status, user, locString);
		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 将总条数放入jsonObj的total对象中
			jsonObj.put("total", rs.getRows()[0].get("total"));
			// 获取数据源
			rs = CommonLibQuestionUploadDao.getsonquestion(page, rows,
					question, pid, status, user, locString);
			// 判断数据源不为null且含有数据
			if (rs != null && rs.getRowCount() > 0) {
				for (int i = 0; i < rs.getRowCount(); i++) {
					// 定义json对象
					JSONObject obj = new JSONObject();
					// 生成对象
					// obj.put("pid", rs.getRows()[i].get("pid"));
					obj.put("sid", rs.getRows()[i].get("hotquestionid"));
					obj.put("question", rs.getRows()[i].get("question"));
					// obj.put("other", rs.getRows()[i].get("other"));
					// obj.put("uploadtime", rs.getRows()[i].get("uploadtime"));
					// obj.put("user", rs.getRows()[i].get("username"));
					obj.put("province", LocalMap.get(rs.getRows()[i]
							.get("province")));
					obj.put("city", LocalMap.get(rs.getRows()[i].get("city")));
					// obj.put("result", rs.getRows()[i].get("result"));
					obj.put("status", rs.getRows()[i].get("status"));
					obj.put("hot", rs.getRows()[i].get("hot"));
					// obj.put("hot2", rs.getRows()[i].get("hot2"));
					obj.put("reason", rs.getRows()[i].get("reason"));
					obj.put("solution", rs.getRows()[i].get("solution"));
					// 将生成的兑现放入jsonArr数组中
					jsonArr.add(obj);
				}
			}
			// 将jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		} else {
			// 将0放入jsonObj的total对象中
			jsonObj.put("total", 0);
			// 清空jsonArr数组
			jsonArr.clear();
			// 将jsonArr数组放入jsonObj的rows对象中
			jsonObj.put("rows", jsonArr);
		}
		return jsonObj;
	}

	/**
	 * 添加同义问法
	 * 
	 * @param question
	 * @param pid
	 * @param province
	 * @param city
	 * @return
	 */
	public static Object insertother(String question, Integer pid,
			String province, String city) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获取用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取city编码
		city = CityLocalMap.get(city);

		// 执行SQL语句，绑定事务，返回事务处理结果
		int c = CommonLibQuestionUploadDao.insertother(question, pid, user,
				province, city);
		// 判断事务处理结果
		if (c > 0) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将删除成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "成功!");
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "失败!");
		}
		return jsonObj;
	}

	/**
	 * 根据服务、渠道、用户、问法来获取简要分析的结果
	 * 
	 * @param user参数用户
	 * @param service参数服务
	 * @param channel参数渠道
	 * @param question参数咨询问法
	 * @param ip参数简要分析的地址
	 * @param type测试类型
	 *            如：回归测试
	 * @return 分析结果中的摘要答案json串
	 */
	public static Object KAnalyzeByFistResult(String user, String service,
			String channel, String question, String ip, String province,
			String city, String type, String applycode) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray kNLPResults = new JSONArray();
		// 获取参数配置知识点抽取信息value值
		Result rs = MetafieldmappingDAO.getConfigValue("知识点继承抽取信息配置", "抽取信息过滤");
		List<String> configValueList = new ArrayList<String>();
		for (int n = 0; n < rs.getRowCount(); n++) {
			String value = rs.getRows()[n].get("name").toString();
			configValueList.add(value);
		}
		// 获取简要分析的客户端
		AnalyzeEnterDelegate NLPAppWSClient = getServiceClient
				.NLPAppWSClient(ip);
		// 获取调用接口的入参字符串
//		String queryObject = getKAnalyzeQueryObject_new(user, question,
//				service, channel, province, city, applycode);
		String queryObject = MyUtil.getKAnalyzeQueryObject_new(user, question, service, channel, city, applycode, province);
		// String queryObject = MyUtil.getKAnalyzeQueryObject(user, question,
		// service, channel);
		logger.info("问题库分析接口的输入串：" + queryObject);
		// 定义返回串的变量
		String result0 = "";
		String result1 = "";
		// 判断接口为null
		if (NLPAppWSClient == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "分析失败");
			return jsonObj;
		}
		try {
			// 定义接口的analyze方法，并返回相应的返回串
			String result = NLPAppWSClient.analyze(queryObject);
			logger.info("问题库分析接口的输出串：" + result);
			// 将返回串按照||||来拆分，前一部分当作简要分析的json串
			result0 = result.split("\\|\\|\\|\\|")[0].replaceAll(
					"(\r\n|\r|\n|\n\r|\t)", "");
			// 后面一部分当作流程日志的json串
			result1 = result.split("\\|\\|\\|\\|")[1];
			// 流程日志的json串需要进行转义
			result1 = GlobalValues.html(result1);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "分析失败");
			return jsonObj;
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result0) || "".equals(result0)
				|| result0 == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将结果为空信息放入jsonObj的result对象中
			jsonObj.put("result", "结果为空");
			// 将结果为空信息放入jsonObj的result1对象中
			jsonObj.put("result1", "结果为空");
			return jsonObj;
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result1) || "".equals(result1)
				|| result1 == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将结果为空信息放入jsonObj的result对象中
			jsonObj.put("result", "结果为空");
			// 将结果为空信息放入jsonObj的result1对象中
			jsonObj.put("result1", "结果为空");
			return jsonObj;
		}
		try {
			// 将接口返回的json串反序列化为json对象
			JSONObject obj = JSONObject.parseObject(result0);
			// 将obj对象中key为kNLPResults的value变成json数组
			JSONArray kNLPResultsArray = obj.getJSONArray("kNLPResults");
			// 遍历循环kNLPResultsArray数组
			 for (int i = 0; i < kNLPResultsArray.size(); i++){
//			for (int i = 0; i < 1; i++) {
				// 定义一个json对象
				JSONObject o = new JSONObject();
				// 将kNLPResultsArray数组中的第i个转换为json对象
				JSONObject kNLPResultsObj = JSONObject
						.parseObject(kNLPResultsArray.get(i).toString());
				// 遍历取继承词模返回值
				String retrnKeyValue = "";
				JSONArray parasArray = kNLPResultsObj.getJSONArray("paras");
				JSONObject parasKeyValueArray = kNLPResultsObj
						.getJSONObject("parasKeyValue");
				for (int l = 0; l < parasArray.size(); l++) {
					JSONObject parasObj = JSONObject.parseObject(parasArray
							.get(l).toString());
					for (int j = 0; j < configValueList.size(); j++) {
						String key = configValueList.get(j);
						String value = parasObj.getString(key);
						if (value != null && !"".equals(value)) {
							retrnKeyValue = retrnKeyValue + key + "=" + value
									+ "->>";
						}
					}
				}

				// 放入继承词模返回值
				o.put("retrnkeyvalue", retrnKeyValue);
				// 获取kNLPResultsObj对象中credit，并生成credit对象
				// o.put("credit", kNLPResultsObj.getString("credit"));
				// 获取kNLPResultsObj对象中service，并生成service对象
				o.put("service", kNLPResultsObj.getString("service"));
				// 获取kNLPResultsObj对象中answer，并生成answer对象
				o.put("answer", kNLPResultsObj.getString("answer"));
				// 获取kNLPResultsObj对象中abstractStr，并生成abstract对象
				o.put("abstract", kNLPResultsObj.getString("abstractStr"));
				// 获取kNLPResultsObj对象中abstractID，并生成absid对象
				o.put("absid", kNLPResultsObj.getString("abstractID"));
				// 获取kNLPResultsObj对象中abstractStr，并生成topic对象
				o.put("topic", kNLPResultsObj.getString("topic"));
//				o.put("业务路径", parasKeyValueArray.getString("业务路径"));
				// 将生成的对象放入kNLPResults数组中
				kNLPResults.add(o);
			}
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将kNLPResults数组放入jsonObj的result对象中
			jsonObj.put("result", kNLPResults);
			// 将result1放入jsonObj的result1对象中
			jsonObj.put("result1", result1);
			return jsonObj;
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将返回结果解析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "返回结果解析失败");
			// 将返回结果解析失败信息放入jsonObj的result1对象中
			jsonObj.put("result1", "返回结果解析失败");
			return jsonObj;
		}
	}

	/**
	 * 获取简要分析的入参字符串方法
	 * 
	 * @param userid参数用户id
	 * @param question参数问题
	 * @param business参数服务
	 * @param channel参数渠道
	 * @return 入参字符串
	 */
	public static String getKAnalyzeQueryObject_new(String userid,
			String question, String business, String channel, String province,
			String city, String applycode) {
		// 定义一个json对象
		JSONObject queryJsonObj = new JSONObject();
		// 将用户id放入queryJsonObj中
		queryJsonObj.put("userID", userid.trim());
		// 将用户咨询的问题放入queryJsonObj中
		queryJsonObj.put("query", question.replace("\"", ".").replace("•", ".")
				.replace("·", ".").replace("\\", "\\\\").trim());
		// 将四层结构放入queryJsonObj中
		queryJsonObj.put("business", business);
		// 将渠道放入queryJsonObj中
		queryJsonObj.put("channel", channel);
		// 获取当前时间
		String callTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
				.format(new Date());
		// 将时间放入queryJsonObj中
		queryJsonObj.put("callTime", callTime);

//		// 定义province的json数组
//		JSONArray provinceJsonArr = new JSONArray();
//		// 将数据放入provinceJsonArr数组中
//		provinceJsonArr.add(province);
//		// 定义provinceJsonObj的json对象
//		JSONObject provinceJsonObj = new JSONObject();
//		// 将provinceJsonArr放入provinceJsonObj中
//		provinceJsonObj.put("Province", provinceJsonArr);

		// 定义city的json数组
		JSONArray cityJsonArr = new JSONArray();
		// 将地市放入cityJsonArr数组中
		cityJsonArr.add(city);
		// 定义city的json对象
		JSONObject cityJsonObj = new JSONObject();
		// 将cityJsonArr数组放入cityJsonObj对象中
		cityJsonObj.put("city", cityJsonArr);

		// 定义applyCode的json数组
		JSONArray applyCodeJsonArr = new JSONArray();
		// 将wenfa放入applyCodeJsonArr数组中
		applyCodeJsonArr.add(applycode);
		// 定义applyCode的json对象
		JSONObject applyCodeJsonObj = new JSONObject();
		// 将applyCodeJsonArr数组放入applyCodeJsonObj对象中
		applyCodeJsonObj.put("applyCode", applyCodeJsonArr);

		// 定义parasjson数组
		JSONArray parasJsonArr = new JSONArray();
		// 将isRecordDBJsonObj放入parasJsonArr中
		parasJsonArr.add(applyCodeJsonObj);
		// 将isRecordDBJsonObj放入parasJsonArr中
//		parasJsonArr.add(provinceJsonObj);
//		 将cityJsonObj放入parasJsonArr中
		parasJsonArr.add(cityJsonObj);
		// 将parasJsonArr放入queryJsonObj中
		queryJsonObj.put("paras", parasJsonArr);

		return queryJsonObj.toJSONString();
	}

	/**
	 * 获取用户信息
	 * 
	 * @return
	 */
	public static Object getSession() {
		JSONObject jsonObj = new JSONObject();
		// 获取用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取角色身份
		String rolename = user.getRoleList().get(0).getRoleName().toString()
				.replace("管理员", "");
		// 获取business
		String bsname = user.getIndustryOrganizationApplication().split("->")[1];
		jsonObj.put("success", true);
		jsonObj.put("rolename", rolename);
		jsonObj.put("bsname", bsname);
		// pv量统计--卫斌
		String c = CommonLibQuestionUploadDao.pvCount(user, ProvinceLocalMap);
		return jsonObj;
	}

	/**
	 * 删除同义问法
	 * 
	 * @param sid
	 * @return
	 * @throws SQLException
	 */
	public static Object delOther(int sid) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 批量删除
		int c = CommonLibQuestionUploadDao.delOther(sid);
		if (c > 0) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将删除成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "成功!");
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "失败!");
		}
		return jsonObj;
	}

	/**
	 * 获取省市树形下拉
	 * @param local
	 * @return
	 */
	public static Object selLocal(String local) {
		// 返回的json数组
		JSONArray jsonAr = new JSONArray();
		Result rs = null;
		// 获取用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取用户角色信息
		String rolename = user.getRoleList().get(0).getRoleName().toString()
				.replace("管理员", "");
		// 获取business
		String bsname = user.getIndustryOrganizationApplication().split("->")[1];
		// 获取其可操作省
		rs = CommonLibQuestionUploadDao.selProvince(rolename, bsname);
		
		if (null != rs && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				// json对象
				JSONObject jsonObj = new JSONObject();
				// 省编码
				String id = rs.getRows()[i].get("id").toString();
				// 省中文
				String province = rs.getRows()[i].get("province").toString();
				Result innerRs = null;
				// 非直辖市
				if (province.indexOf("市") < 0) {
					innerRs = CommonLibQuestionUploadDao.getCity(id);
				}
				// json数组
				JSONArray jsonArr = new JSONArray();
				if (null != innerRs && innerRs.getRowCount() > 0) {
					for (int j = 0; j < innerRs.getRowCount(); j++) {
						JSONObject innerJsonObj = new JSONObject();
						// 省下的城市
						innerJsonObj.put("id", innerRs.getRows()[j].get("id"));
						innerJsonObj.put("text", innerRs.getRows()[j]
								.get("city"));
						// 如果该城市和入参相同，则选中
						if (local.equals(innerRs.getRows()[j].get("city"))) {
							innerJsonObj.put("checked", true);
						}
						jsonArr.add(innerJsonObj);
					}
					// 下拉收起
					jsonObj.put("state", "closed");
				}
				// 拼成前台所需的返回串格式
				jsonObj.put("id", rs.getRows()[i].get("id"));
				jsonObj.put("text", rs.getRows()[i].get("province"));
				jsonObj.put("children", jsonArr);
				jsonAr.add(jsonObj);
			}
		}
		return jsonAr;
	}

	/**
	 * 获取省市树
	 * @param local
	 * @return
	 */
	public static Object getCityTree(String local) {
		// 分割city
		String cityname[] = local.split(",");
		// 将地市做成map
		Map<String, String> map = new HashMap<String, String>();
		
		for (int m = 0; m < cityname.length; m++) {
			map.put(cityname[m], "");
		}
		// json数组
		JSONArray jsonAr = new JSONArray();

		Result rs = null;
		// 获取31省
		rs = CommonLibQuestionUploadDao.selProvince();
		if (null != rs && rs.getRowCount() > 0) {
			JSONObject allJsonObj = new JSONObject();
			allJsonObj.put("id", "全国");
			allJsonObj.put("text", "全国");
			// 是否勾选
			if (map.containsKey("全国")) {
				allJsonObj.put("checked", true);
			}
			jsonAr.add(allJsonObj);
			for (int i = 0; i < rs.getRowCount(); i++) {
				// json对象
				JSONObject jsonObj = new JSONObject();
				// 省编码
				String id = rs.getRows()[i].get("id").toString();
				// 省中文
				String province = rs.getRows()[i].get("province").toString();
				Result innerRs = null;
				Result innerRs2 = null;
				// 非直辖市
				if (province.indexOf("市") < 0) {
					innerRs = CommonLibQuestionUploadDao.getCityByTree(id);
				}
				// else {
				// innerRs2 = CommonLibQuestionUploadDao.getzCity(id);
				// }
				// 是否勾选
				if (map.containsKey(province)) {
					jsonObj.put("checked", true);
				}
				// json数组
				JSONArray jsonArr = new JSONArray();
				if (null != innerRs && innerRs.getRowCount() > 0) {
					for (int j = 0; j < innerRs.getRowCount(); j++) {

						JSONObject innerJsonObj = new JSONObject();
						// 城市编码
						innerJsonObj.put("id", innerRs.getRows()[j].get("id"));
						// 城市中文
						innerJsonObj.put("text", innerRs.getRows()[j]
								.get("city"));
						// 是否勾选
						if (map.containsKey(innerRs.getRows()[j].get("city"))) {
							innerJsonObj.put("checked", true);
						}
						jsonArr.add(innerJsonObj);
					}
					// 下拉关闭
					jsonObj.put("state", "closed");
				} else if (null != innerRs2 && innerRs2.getRowCount() > 0) {
					for (int j = 0; j < innerRs2.getRowCount(); j++) {
						// json数组
						JSONArray sJsonArr = new JSONArray();
						// json对象
						JSONObject innerJsonObj = new JSONObject();
						
						innerJsonObj.put("id", innerRs2.getRows()[j].get("id"));
						
						innerJsonObj.put("text", innerRs2.getRows()[j]
								.get("city"));
						innerJsonObj.put("children", sJsonArr);
						if (map.containsKey(innerRs2.getRows()[j].get("city"))) {
							innerJsonObj.put("checked", true);
						}
						jsonArr.add(innerJsonObj);
					}
					jsonObj.put("state", "closed");
				}
				jsonObj.put("id", rs.getRows()[i].get("id"));
				jsonObj.put("text", rs.getRows()[i].get("province"));
				jsonObj.put("children", jsonArr);
				jsonAr.add(jsonObj);
			}
		}
		return jsonAr;
	}

	/**
	 *@description 获取规则用户登录相关地市
	 *@param local
	 *@return
	 *@returnType Object
	 */
	public static Object getCityTreeByLoginInfo(String local) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String customer = user.getCustomer();
		String cityCode = "";
		String cityName = "";
		JSONArray jsonAr = new JSONArray();
		
		if (customer.equals("全行业")){
			String cityname[] = local.split(",");
			Map<String, String> map = new HashMap<String, String>();
			for (int m = 0; m < cityname.length; m++) {
				map.put(cityname[m], "");
			}
			Result rs = null;
			rs = CommonLibQuestionUploadDao.selProvince();
			if (null != rs && rs.getRowCount() > 0) {
				JSONObject allJsonObj = new JSONObject();
				allJsonObj.put("id", "全国");
				allJsonObj.put("text", "全国");
				if (map.containsKey("全国")) {
					allJsonObj.put("checked", true);
				}
				jsonAr.add(allJsonObj);
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject jsonObj = new JSONObject();
					String id = rs.getRows()[i].get("id").toString();
					String province = rs.getRows()[i].get("province").toString();
					Result innerRs = null;
					Result innerRs2 = null;
					if (province.indexOf("市") < 0) {
						innerRs = CommonLibQuestionUploadDao.getCityByTree(id);
					}
					// else {
					// innerRs2 = CommonLibQuestionUploadDao.getzCity(id);
					// }
					if (map.containsKey(province)) {
						jsonObj.put("checked", true);
					}
					JSONArray jsonArr = new JSONArray();
					if (null != innerRs && innerRs.getRowCount() > 0) {
						for (int j = 0; j < innerRs.getRowCount(); j++) {

							String cityId = innerRs.getRows()[j].get("id")
									.toString();
							// Result sinnerRs =
							// CommonLibQuestionUploadDao.getScity(cityId);
							// JSONArray sJsonArr = new JSONArray();
							JSONObject innerJsonObj = new JSONObject();
							// if (sinnerRs != null && sinnerRs.getRowCount() > 0){
							// for (int k = 0 ; k < sinnerRs.getRowCount() ; k++){
							// JSONObject sInnerJsonObj = new JSONObject();
							// sInnerJsonObj.put("id",
							// sinnerRs.getRows()[k].get("id"));
							// sInnerJsonObj.put("text",
							// sinnerRs.getRows()[k].get("city"));
							// if
							// (map.containsKey(sinnerRs.getRows()[k].get("city"))){
							// sInnerJsonObj.put("checked", true);
							// }
							// sJsonArr.add(sInnerJsonObj);
							// }
							// innerJsonObj.put("state", "closed");
							// }
							innerJsonObj.put("id", innerRs.getRows()[j].get("id"));
							innerJsonObj.put("text", innerRs.getRows()[j]
									.get("city"));
							// innerJsonObj.put("children", sJsonArr);
							// if (local.equals(innerRs.getRows()[j].get("city"))){
							// innerJsonObj.put("checked", true);
							// }

							if (map.containsKey(innerRs.getRows()[j].get("city"))) {
								innerJsonObj.put("checked", true);
							}
							jsonArr.add(innerJsonObj);
						}
						jsonObj.put("state", "closed");
					} else if (null != innerRs2 && innerRs2.getRowCount() > 0) {
						for (int j = 0; j < innerRs2.getRowCount(); j++) {

							JSONArray sJsonArr = new JSONArray();

							JSONObject innerJsonObj = new JSONObject();
							innerJsonObj.put("id", innerRs2.getRows()[j].get("id"));
							innerJsonObj.put("text", innerRs2.getRows()[j]
									.get("city"));
							innerJsonObj.put("children", sJsonArr);
							if (map.containsKey(innerRs2.getRows()[j].get("city"))) {
								innerJsonObj.put("checked", true);
							}
							jsonArr.add(innerJsonObj);
						}
						jsonObj.put("state", "closed");
					}
					jsonObj.put("id", rs.getRows()[i].get("id"));
					jsonObj.put("text", rs.getRows()[i].get("province"));
					jsonObj.put("children", jsonArr);
					jsonAr.add(jsonObj);
				}
			}
		}else{
			List<String> cityList = new ArrayList<String>();
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
			.resourseAccess(user.getUserID(), "querymanage", "S");
			// 该操作类型用户能够操作的资源
			cityList = resourseMap.get("地市");
			if (cityList != null) {
				cityCode = cityList.get(0);
//			if(cityCode.endsWith("0000")){//省级用户
//				
//			}
			}
			Map<String, String> map = new HashMap<String, String>();
			String cityname[] = local.split(",");
			for (int m = 0; m < cityname.length; m++) {
				map.put(cityname[m], "");
			}
			
			Result rs = null;
			rs = CommonLibQuestionUploadDao.selProvince(cityCode);
			JSONObject innerJsonObj =null;
			if (null != rs && rs.getRowCount() > 0) {
				JSONObject allJsonObj = new JSONObject();
				if(!local.contains("edit")){
					allJsonObj.put("id", "全国");
					allJsonObj.put("text", "全国");
					if (map.containsKey("全国")){
						allJsonObj.put("checked", true);
					}
					jsonAr.add(allJsonObj);	
				}
				
				for (int i = 0; i < rs.getRowCount(); i++) {
					JSONObject jsonObj = new JSONObject();
					String id = rs.getRows()[i].get("id").toString();
					String province = rs.getRows()[i].get("province").toString();
					Result innerRs = null;
					Result innerRs2 = null;
					if (province.indexOf("市") < 0) {
						innerRs = CommonLibQuestionUploadDao.getCityByProvince(id);
					}
					// else {
					// innerRs2 = CommonLibQuestionUploadDao.getzCity(id);
					// }
					if (map.containsKey(province)) {
						jsonObj.put("checked", true);
					}
					JSONArray jsonArr = new JSONArray();
					if (null != innerRs && innerRs.getRowCount() > 0) {
						for (int j = 0; j < innerRs.getRowCount(); j++) {
							
							String cityId = innerRs.getRows()[j].get("id")
							.toString();
							// Result sinnerRs =
							// CommonLibQuestionUploadDao.getScity(cityId);
							// JSONArray sJsonArr = new JSONArray();
							innerJsonObj = new JSONObject();
							// if (sinnerRs != null && sinnerRs.getRowCount() > 0){
							// for (int k = 0 ; k < sinnerRs.getRowCount() ; k++){
							// JSONObject sInnerJsonObj = new JSONObject();
							// sInnerJsonObj.put("id",
							// sinnerRs.getRows()[k].get("id"));
							// sInnerJsonObj.put("text",
							// sinnerRs.getRows()[k].get("city"));
							// if
							// (map.containsKey(sinnerRs.getRows()[k].get("city"))){
							// sInnerJsonObj.put("checked", true);
							// }
							// sJsonArr.add(sInnerJsonObj);
							// }
							// innerJsonObj.put("state", "closed");
							// }
							innerJsonObj.put("id", innerRs.getRows()[j].get("id"));
							innerJsonObj.put("text", innerRs.getRows()[j]
							                                           .get("city"));
							// innerJsonObj.put("children", sJsonArr);
							// if (local.equals(innerRs.getRows()[j].get("city"))){
							// innerJsonObj.put("checked", true);
							// }
							
							if (map.containsKey(innerRs.getRows()[j].get("city"))) {
								innerJsonObj.put("checked", true);
							}
							jsonArr.add(innerJsonObj);
						}
						jsonObj.put("state", "closed");
					} else if (null != innerRs2 && innerRs2.getRowCount() > 0) {
						for (int j = 0; j < innerRs2.getRowCount(); j++) {
							
							JSONArray sJsonArr = new JSONArray();
							
							innerJsonObj = new JSONObject();
							innerJsonObj.put("id", innerRs2.getRows()[j].get("id"));
							innerJsonObj.put("text", innerRs2.getRows()[j]
							                                            .get("city"));
							innerJsonObj.put("children", sJsonArr);
							if (map.containsKey(innerRs2.getRows()[j].get("city"))) {
								innerJsonObj.put("checked", true);
							}
							jsonArr.add(innerJsonObj);
						}
						jsonObj.put("state", "closed");
					}
					if(cityCode.endsWith("0000")){
						jsonObj.put("id", rs.getRows()[i].get("id"));
						jsonObj.put("text", rs.getRows()[i].get("province"));
						jsonObj.put("children", jsonArr);
						jsonAr.add(jsonObj);	
					}else{
						jsonAr.add(innerJsonObj);
					}
					
				}
			}
		}
		// System.out.println(jsonAr);
		return jsonAr;
	}

	/**
	 * 修改同义问法
	 * @param pid
	 * @param question
	 * @param sid
	 * @param other
	 * @param province
	 * @param city
	 * @return
	 */
	public static Object updateQueName(Integer pid, String question, int sid,
			String other, String province, String city) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		int c = CommonLibQuestionUploadDao.updateQueName(sid, other);
		if (c > 0) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将删除成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "成功!");
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "失败!");
		}
		return jsonObj;
	}

	/**
	 * 删除同义问法
	 * @param ids
	 * @return
	 */
	public static Object deleteOther(String ids) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		int c = CommonLibQuestionUploadDao.deleteOther(ids);
		if (c > 0) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将删除成功信息放入jsonObj的msg对象中
			jsonObj.put("msg", "成功!");
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将删除失败信息放入jsonObj的msg对象中
			jsonObj.put("msg", "失败!");
		}
		return jsonObj;
	}

	/**
	 * 导出文件
	 * @param ids
	 * @return
	 */
	public static Object ExportExcel(String ids) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义存放生成Excel文件的每一行内容的集合
		List<String> rowList = new ArrayList<String>();
		// 定义存放生成Excel文件的所有内容的集合
		List<List<String>> attrinfoList = new ArrayList<List<String>>();
		// 定义存放属性名称对应列值的数组
		String[] columnArr = { "标准问法", "同义问法", "理解结果", "报错原因", "状态", "解决方法",
				"是否热点问法", "上传时间", "上传账号", "归属省份", "归属城市" };
		for (int i = 0; i < columnArr.length; i++) {
			// 将属性名称放入Excel文件的第一行内容的集合中
			rowList.add(columnArr[i]);
		}
		attrinfoList.add(rowList);
		// 在数据库里查出数据
		Result rs = CommonLibQuestionUploadDao.gethotquestiondown(ids);

		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 定义存放生成Excel文件的每一行内容的集合
				rowList = new ArrayList<String>();
				rowList.add(rs.getRows()[i].get("question").toString());
				rowList.add(rs.getRows()[i].get("other").toString());
				rowList.add(rs.getRows()[i].get("result") != null ? rs
						.getRows()[i].get("result").toString().replace("<br>",
						"").replace("<b style=\"color:red;\">", "").replace(
						"</b>", "") : "");
				rowList.add(rs.getRows()[i].get("reason") != null ? rs
						.getRows()[i].get("reason").toString() : "");
				rowList.add(rs.getRows()[i].get("status").toString().equals(
						"-1") ? "未处理" : (rs.getRows()[i].get("status")
						.toString().equals("1") ? "已处理" : ""));
				rowList.add(rs.getRows()[i].get("solution") != null ? rs
						.getRows()[i].get("solution").toString() : "");
				rowList
						.add(rs.getRows()[i].get("hot").toString()
								.equals("yes") ? "是" : "否");
				rowList.add(rs.getRows()[i].get("uploadtime").toString());
				rowList.add(rs.getRows()[i].get("username").toString());
				rowList.add(LocalMap.get(rs.getRows()[i].get("province")
						.toString()));
				rowList.add(rs.getRows()[i].get("city") != null ? LocalMap
						.get(rs.getRows()[i].get("city").toString()) : "");
				// 将行内容的集合放入全内容集合中
				attrinfoList.add(rowList);
			}
		}
		// 定义文件的路径
		String pathName = "";
		// 定义文件名称
		pathName = pathName + "问法.xls";
		// 调用生成Excel2003的方法，并返回生成Excel文件的路径
		creat2003Excel(attrinfoList, pathName);
		// 定义文件对象
		File file = new File(regressTestPath + File.separator + pathName);
		// 判断文件是否存在
		if (file.exists()) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将文件路径放入jsonObj的path对象中
			jsonObj.put("path", pathName);
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/**
	 * 根据条件全量下载
	 * @param question
	 * @param other
	 * @param starttime
	 * @param endtime
	 * @param username
	 * @param status
	 * @param selProvince
	 * @param selCity
	 * @return
	 */
	public static Object ExportExcel2(String question, String other,
			String starttime, String endtime, String username, String status,
			String selProvince, String selCity) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义存放生成Excel文件的每一行内容的集合
		List<String> rowList = new ArrayList<String>();
		// 定义存放生成Excel文件的所有内容的集合
		List<List<String>> attrinfoList = new ArrayList<List<String>>();
		// 定义存放属性名称对应列值的数组
		String[] columnArr = { "标准问法", "同义问法", "理解结果", "报错原因", "状态", "解决方法",
				"是否热点问法", "上传时间", "上传账号", "归属省份", "归属城市" };
		for (int i = 0; i < columnArr.length; i++) {
			// 将属性名称放入Excel文件的第一行内容的集合中
			rowList.add(columnArr[i]);
		}
		attrinfoList.add(rowList);
		// 用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 用户id
		String userid = user.getUserID();
		// provinceid
		selProvince = ProvinceLocalMap.get(selProvince);
		// cityid
		selCity = CityLocalMap.get(selCity);
		// 获取权限
		HashMap<String, ArrayList<String>> locPer = CommonLibPermissionDAO
				.resourseAccess(userid, "hotquestion", "S");
		// 获取可操作地市
		ArrayList<String> locArr = locPer.get("地市");

		String locString = "";
		// 地市中的省，拼成字符串
		if (locArr != null && locArr.size() > 0) {
			for (String loc : locArr) {
				if (loc.contains("0000")) {
					locString = locString + "'" + loc + "',";
				}
			}
			locString = locString.substring(0, locString.length() - 1);
		}
		// 获取数据
		Result rs = CommonLibQuestionUploadDao.gethotquestionnondown(question,
				other, starttime, endtime, username, status, selProvince,
				selCity, user, locString);

		// 判断数据源不为null且含有数据
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				// 定义存放生成Excel文件的每一行内容的集合
				rowList = new ArrayList<String>();
				rowList.add(rs.getRows()[i].get("question").toString());
				rowList.add(rs.getRows()[i].get("other").toString());
				rowList.add(rs.getRows()[i].get("result") != null ? rs
						.getRows()[i].get("result").toString().replace("<br>",
						"").replace("<b style=\"color:red;\">", "").replace(
						"</b>", "") : "");
				rowList.add(rs.getRows()[i].get("reason") != null ? rs
						.getRows()[i].get("reason").toString() : "");
				rowList.add(rs.getRows()[i].get("status").toString().equals(
						"-1") ? "未处理" : (rs.getRows()[i].get("status")
						.toString().equals("1") ? "已处理" : ""));
				rowList.add(rs.getRows()[i].get("solution") != null ? rs
						.getRows()[i].get("solution").toString() : "");
				rowList
						.add(rs.getRows()[i].get("hot").toString()
								.equals("yes") ? "是" : "否");
				rowList.add(rs.getRows()[i].get("uploadtime").toString());
				rowList.add(rs.getRows()[i].get("username").toString());
				rowList.add(LocalMap.get(rs.getRows()[i].get("province")
						.toString()));
				rowList.add(rs.getRows()[i].get("city") != null ? LocalMap
						.get(rs.getRows()[i].get("city").toString()) : "");
				// 将行内容的集合放入全内容集合中
				attrinfoList.add(rowList);
			}
		}
		// 定义文件的路径
		String pathName = "";
		// 定义文件名称
		pathName = pathName + "问法.xls";
		// 调用生成Excel2003的方法，并返回生成Excel文件的路径
		creat2003Excel(attrinfoList, pathName);
		// 定义文件对象
		File file = new File(regressTestPath + File.separator + pathName);
		// 判断文件是否存在
		if (file.exists()) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将文件路径放入jsonObj的path对象中
			jsonObj.put("path", pathName);
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	/*
	 * 将字符串里面的所有的html标签去掉
	 */
	public static String HtmlText(String inputString) {
		String htmlStr = inputString; // 含html标签的字符串
		String textStr = "";
		java.util.regex.Pattern p_script;
		java.util.regex.Matcher m_script;
		java.util.regex.Pattern p_style;
		java.util.regex.Matcher m_style;
		java.util.regex.Pattern p_html;
		java.util.regex.Matcher m_html;
		try {
			String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; // 定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
																										// }
			String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; // 定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
																									// }
			String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式

			p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
			m_script = p_script.matcher(htmlStr);
			htmlStr = m_script.replaceAll(""); // 过滤script标签

			p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
			m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll(""); // 过滤style标签

			p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll(""); // 过滤html标签

			/* 空格 —— */
			// p_html = Pattern.compile("\\ ", Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = htmlStr.replaceAll(" ", " ");

			textStr = htmlStr;

		} catch (Exception e) {
		}
		return textStr;
	}

	/**
	 * 下载示例
	 * @return
	 */
	public static Object exportexample() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 定义文件的路径
		String pathName = "";
		// 定义文件名称
		pathName = pathName + "问法示例.xls";
		// 定义文件对象
		File file = new File(regressTestPath + File.separator + pathName);
		// 判断文件是否存在
		if (file.exists()) {
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将文件路径放入jsonObj的path对象中
			jsonObj.put("path", pathName);
		} else {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
		}
		return jsonObj;
	}

	public static Object exsitfile() {
		// 定义文件的路径
		String pathName = "";
		// 定义文件名称
		pathName = pathName + "问法示例.xls";
		// 定义文件对象
		File file = new File(regressTestPath + File.separator + pathName);
		// 判断文件是否存在
		if (file.exists()) {
			file.delete();
		}
		return null;
	}
	
	/**
	 * 批量理解
	 * @param understrandinfo
	 * @return
	 */
	public static Object understand(String understrandinfo) {
		// 返回值
		JSONObject json = new JSONObject();
		
		String url = "";
		// 获取用户信息
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 	获取用户id
		String userid = user.getUserID();
		// 四层结构
		String servicetype = user.getIndustryOrganizationApplication();
		// 查询配置
//		Result rs2 = UserOperResource.getConfigValue("简要分析服务地址配置", "本地服务");
//		if (rs2 != null && rs2.getRowCount() > 0) {
//			// 获取配置表的ip
//			url = rs2.getRows()[0].get("name").toString();
//		}
//			url = "http://222.186.101.213:8282/NLPAppWS/AnalyzeEnterPort?wsdl";
//			url = "http://180.153.59.28:8082/NLPAppWS/AnalyzeEnterPort?wsdl";
//			url = "http://180.153.69.0:8082/NLPAppWS/AnalyzeEnterPort?wsdl";
		// 渠道
		String channel = "Web";
		String applycode = "wenfa";
		
		Result rs3 = UserOperResource.getConfigValue("问题库批量理解接口入参配置", servicetype);
		if (rs3 != null && rs3.getRowCount() > 0) {
			Map<String ,String> caMap = new HashMap<String, String>();
			for (int i = 0;i < rs3.getRowCount();i++){
				caMap.put(rs3.getRows()[i].get("name").toString().split("->")[0]
				        , rs3.getRows()[i].get("name").toString().split("->")[1]);
			}
			if (caMap.containsKey("applyCode")){
				applycode = caMap.get("applyCode");
			}
			if (caMap.containsKey("channel")){
				channel = caMap.get("channel");
			}
		}
		
		Map<String,String> tempUr = new HashMap<String,String>();
		
		
		
		// 需要理解的问法按条分割
		String[] attrs = understrandinfo.split("xxxnixxx");
		for (int i = 0; i < attrs.length; i++) {
			// 单条问法信息分割
			String[] allattrs = attrs[i].split("@-@");
			
			// 处理字符串
			String question = allattrs[1].replace("\n", " ");
			String question2 = allattrs[2].replace("\n", " ");

			String citycode = allattrs[3];
			String cityname = allattrs[4];

			String citycodes[] = citycode.split(","); 
			String citynames[] = cityname.split(","); 
			
			GetLoadbalancingConfig gConfig = new GetLoadbalancingConfig();

			String city = "全国";
			String province = "全国";
			String pCode = "";
			url = gConfig.provinceToUrl.get(city).get("简要分析");//ghj update
			for (int j = 0;j < citycodes.length;j++){
				if (citycodes[j].endsWith("0000")){// 省份
					// 直辖市处理
					if (citynames[j].equals("北京")) {
						city = "北京";
						url = gConfig.provinceToUrl.get(city).get("简要分析");
						break;
					} else if (citynames[j].equals("上海")) {
						city = "上海";
						url = gConfig.provinceToUrl.get(city).get("简要分析");
						break;
					} else if (citynames[j].equals("天津")) {
						city = "天津";
						url = gConfig.provinceToUrl.get(city).get("简要分析");
						break;
					} else if (citynames[j].equals("重庆")) {
						city = "重庆";
						url = gConfig.provinceToUrl.get(city).get("简要分析");
						break;
					} else if (citynames[j].equals("电渠")) {
						city = "电渠";
						url = gConfig.provinceToUrl.get("默认").get("简要分析");
						break;
					} else if (citynames[j].equals("集团")) {
						// 集团做特殊处理，算电渠
						city = "电渠";
						url = gConfig.provinceToUrl.get("默认").get("简要分析");
						break;
					}else{
						city = citynames[j];
					}
				} else if(citycodes[j].equals("全国")){
					city = "全国";
					url = gConfig.provinceToUrl.get("默认").get("简要分析");
					break;
				} else {
					city = citynames[j];
					pCode = CityLocalMap.get(city).substring(0, 2) + "0000";
					url = gConfig.provinceToUrl.get(LocalMap.get(pCode)).get("简要分析");
					break;
				}
			}
			if("".equals(pCode)){
				province = city;
			}else{
				province = LocalMap.get(pCode);
			}
			
			logger.info("批量理解调用接口地址："+url);
			
			String answer1 = "";
			String answer2 = "";
			
			if (tempUr.containsKey(question + "-" + city)){
				answer1 = tempUr.get(question + "-" + city);
			} else {
				// 标准问理解
				Object object = KAnalyzeByFistResult(userid + Math.random(), servicetype, channel,
						question, url, province, city, "问题库问法测试",applycode);
				JSONObject jsonObj = (JSONObject) object;
				// 获取标准问答案
				if (jsonObj.get("success").equals(true)){
					answer1 = jsonObj.getJSONArray("result").size()==0 ? "" : jsonObj.getJSONArray("result").getJSONObject(0)
							.getString("answer");
				}
				if ("".equals(answer1)){
					answer1 = "无理解结果";
				}
				// 去除<html>标签
				answer1 = HtmlText(answer1);
				// 答案过长处理
				if (answer1.length() > 950) {
					answer1 = answer1.substring(0, 900) + "...";
				}
				tempUr.put(question + "-" + city, answer1);
			}
			
			
			// 同义问理解
			Object object2 = KAnalyzeByFistResult(userid + Math.random(), servicetype, channel,
					question2, url, province, city, "问题库问法测试",applycode);
			JSONObject jsonObj2 = (JSONObject) object2;
			if (jsonObj2.get("success").equals(true)){
				// 获取同义问答案
				answer2 = jsonObj2.getJSONArray("result").size()==0 ? "" : jsonObj2.getJSONArray("result").getJSONObject(0)
						.getString("answer");
			}
			if ("".equals(answer2)){
				answer2 = "无理解结果";
			}
			// 去除<html>标签
			answer2 = HtmlText(answer2);
			// 答案过长处理
			if (answer2.length() > 950) {
				answer2 = answer2.substring(0, 900) + "...";
			}
			
			
			// 拼成最后存入数据库的字符串
			String result = "<b style=\"color:red;\">标准问理解结果：</b><br>"
					+ answer1
					+ "<br><br><b style=\"color:red;\">客户问理解结果：</b><br>"
					+ answer2;
			
			String flag = "0";
			if(answer1.equals("无理解结果") && answer2.equals("无理解结果")){
				flag = "-2";// 无法获取结果
			}else 
			if (answer1.contains("小知还在成长，没能为您找到答案，不过您可以拨打10000去咨询小知的姐姐哦") && answer2.contains("小知还在成长，没能为您找到答案，不过您可以拨打10000去咨询小知的姐姐哦")){
				flag = "-2";// 无法获取结果
			}else if (!answer1.equals(answer2)) {
				// 标准问/同义问理解不一致情况
				flag = "-1";
			}
			// 更新到数据库
			int c = CommonLibQueryManageDAO.understand(result, allattrs[0],
					flag);
			if (c < 1){
				json.put("success", false);
				json.put("result", "理解失败");
				return json;
			}
		}
		json.put("success", true);
		json.put("result", "理解成功");
		return json;
		
	}
}
