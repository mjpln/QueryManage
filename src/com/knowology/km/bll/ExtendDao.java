package com.knowology.km.bll;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

import javax.servlet.jsp.jstl.sql.Result;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.UtilityOperate.GetConfigValue;
import com.knowology.bll.CommonLibKbDataDAO;
import com.knowology.bll.CommonLibKbdataAttrDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibPermissionDAO;
import com.knowology.bll.ConstructSerialNum;
import com.knowology.dal.Database;
import com.knowology.km.NLPCallerWS.NLPCaller4WSDelegate;
import com.knowology.km.access.UserOperResource;
import com.knowology.km.common.util.HttpClientUtil;
import com.knowology.km.dto.ExtendsDto;
import com.knowology.km.util.GetLoadbalancingConfig;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.getConfigValue;
import com.knowology.km.util.getServiceClient;
import com.str.NewEquals;
public class ExtendDao {
	public static Logger logger = Logger.getLogger("querymanage");
	public static String[] names = { "父亲业务", "父亲摘要ID", "父亲摘要", "儿子业务", "儿子摘要",
		"儿子摘要ID", "Business","业务X","业务Y","业务Z","业务L","业务M","业务N","相关度"};
	/**
	 * 高级分析
	 * @param phone
	 * @param service
	 * @param query
	 * @param ip
	 * @param city
	 * @param typeName
	 * @param isdebug
	 * @param province
	 * @param attr6
	 * @return
	 */
	public static Object analysis(String phone, String service,String query,String city,String typeName,String isdebug,String province,String attr6) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		String cityCode ="";
		String url ="";
		String provinceCode ="";
		//商家行业
		String serviceApplication = service;
		//处理业务库服务名称
		if(service.indexOf("->")!=-1){
			String[] businessArr = service.split("->", 3);
			String industry = businessArr[0];
			service = industry+"->通用商家->问题库应用";
		}
		

		// 根据服务获取相应的四层结构的接口入参的serviceinfo串
		String serviceInfo = MyUtil.getServiceInfo(service, "继承高级分析", "", false,cityCode);
		// 获取调用高级分析接口的接口串
		String queryObject = MyUtil.getDAnalyzeQueryObject(phone, query,service, serviceInfo);

		// add by sundj 20191022 新增根据本行业商家获取业务X--------------------------------------
		//根据本商家去高级分析 
		Result configResult = CommonLibMetafieldmappingDAO.getConfigValue("知识点继承业务配置", serviceApplication);
		String returnvalue = null;
		if(configResult != null && configResult.getRowCount() > 0){
			String id = configResult.getRows()[0].get("name").toString();
			String businessServiceInfo = MyUtil.getServiceInfoWithID(serviceApplication, "继承高级分析", "",
					id, false,city.replace(",", "|"));
			// 获取调用高级分析接口的接口串
			String businessQueryObject = MyUtil.getDAnalyzeQueryObject(phone, query,serviceApplication, businessServiceInfo);
			logger.info("本商家继承高级分析接口的输入串：" + businessQueryObject);
			JSONObject resJson = (JSONObject)DetailAnalyzeResultByLocal(businessQueryObject,city,attr6);
			 returnvalue = resJson.getString("result");
//			if(StringUtils.isNotBlank(result)){
//				//行业商家的高级分析结果
//				JSONArray resultJsonIns = jsonObj.getJSONArray("result");
//				JSONArray ja = new JSONArray();
//				for(int i=0;i<resultJsonIns.size();i++){
//					//
//					JSONObject jsonObjIns = resultJsonIns.getJSONObject(i);
//					JSONArray jsonObjArray = jsonObjIns.getJSONArray("creditresults");
//					if(jsonObjArray != null & jsonObjArray.size()>0){//替换业务X						
//						JSONArray busiArray = new JSONArray();
//						for(int j=0;j<jsonObjArray.size();j++){
//							JSONObject json = jsonObjArray.getJSONObject(i);
//							if(json.containsKey("attr8")){
//								json.put("attr8", result);
//							}
//							busiArray.add(json);
//						}
//						jsonObjIns.put("creditresults", busiArray);
//					}
//					ja.add(jsonObjIns);
//					
//				}
//				
//				jsonObj.put("result", ja);
//			}
//			
		}
		logger.info("行业商家继承高级分析接口的输入串：" + queryObject);
		jsonObj = (JSONObject)DetailAnalyzeResult(queryObject,city,attr6,returnvalue);
		if(!jsonObj.getBoolean("success")){
		   return jsonObj;
		}
		
		return jsonObj;
	}
	
	
	/**
	 * 行业商家高级分析
	 * @param queryObject
	 * @param city
	 * @param attr6
	 * @param returnvalue  本商家业务X返回值
	 * @return
	 */
	public static Object DetailAnalyzeResult(String queryObject, String city,String attr6,String returnvalue){
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		String cityCode ="";
		String url ="";
		String provinceCode ="";
		
		if(city!=null){
			cityCode = city.replace(",", "|");
			if(!"全国".equals(cityCode)){
				provinceCode = cityCode.split("\\|")[0];
				provinceCode = provinceCode.substring(0,2)+"0000";
				if("010000".equals(provinceCode)||"000000".equals(provinceCode)){//如何为集团、电渠编码 去默认url
					provinceCode="默认";
					url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
				}else{
					url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvinceCode(provinceCode);
				}
				logger.info("继承高级分析请求【"+GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode)+"】地址：" + url);
			}else{
				cityCode ="全国";
				url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
				logger.info("继承高级分析请求【默认】地址：" + url);
			}
		}else{
			cityCode ="全国";
			url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
			logger.info("继承高级分析请求【默认】地址：" + url);
		}
		
		// 获取高级分析的客户端
		
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient.NLPCaller4WSClient(url);
		
		// 定义接口的返回串变量
		String result = "";
		// 判断接口是否为null
		if (NLPCaller4WSClient == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "分析失败");
			return jsonObj;
		}
		try {
			// 定义接口的detailAnalyze方法获取返回串
			result = NLPCaller4WSClient.detailAnalyze(queryObject);
			// 替换掉返回串中的回车符
			result = result.replace("\n", "");
			logger.info("高级分析接口的输出串：" + result);
			
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
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "结果出现错误");
			return jsonObj;
		}
		try {
			// 将接口返回的json串,反序列化为json数组
			JSONArray jsonArray = JSONArray.parseArray(result);
			// 循环遍历jsonArray数组
			for (int i = 0; i < jsonArray.size(); i++) {
				// 将jsonArray数组中的第i个转换成json对象
				JSONObject obj = JSONObject.parseObject(jsonArray.get(i).toString());
				// 得到多个分词的json串
				// 定义分词的json数组
				JSONArray allSegments = new JSONArray();
				// 将obj对象中key为AllSegments的value变成json数组
				JSONArray allSegmentsArray = obj.getJSONArray("allSegments");
				// 遍历循环arrayAllSegments数组
				for (int j = 0; j < allSegmentsArray.size(); j++) {
					// 获取arrayAllSegments数组中的每一个值
					String segments = allSegmentsArray.getString(j);
					// 判断分词是否含有..(
					if (!segments.contains("...(")) {
						// 根据分词得到分词数
						String wordnum = segments.split("\\)  \\(")[1].replace(" words)", "");
						// 得到分词的内容
						String word = segments.split("\\)  \\(")[0] + ")";
						// 定义json对象
						JSONObject o = new JSONObject();
						// 生成wordnum对象
						o.put("wordnum", wordnum);
						// 生成word对象
						o.put("word", word);
						// 将生成的对象放入allSegments数组中
						allSegments.add(o);
					}
				}
				// 得到多个结果的json串
				JSONArray creditResults = new JSONArray();
				List<String[]> kblist = getExtendKBS(attr6);
				// 将obj对象中key为CreditResults的value变成json数组
				JSONArray creditResultsArray = obj.getJSONArray("creditResults");
				// 遍历循环creditResultsArray数组
				for (int k = 0; k < creditResultsArray.size(); k++) {
					// 将creditResultsArray数组中第k个转换为json对象
					JSONObject creditResultsObj = JSONObject.parseObject(creditResultsArray.get(k).toString());

					// 将creditResultsObj对象中key为abstracts的value变成abstractsArray数组
					JSONArray abstractsArray = creditResultsObj.getJSONArray("abstracts");
					// 遍历循环abstractsArray数组
					for (int m = 0; m < abstractsArray.size(); m++) {
						// 将abstractsArray数组中第m个转换为json对象
						JSONObject abstractsObj = JSONObject.parseObject(abstractsArray.get(m).toString());
						// 定义json对象
						JSONObject o = new JSONObject();

						// 获取abstractsObj对象中abstractString，并生成摘要对象
						String abs = abstractsObj.getString("abstractString");
						if(abs.contains(">")){
							abs = abstractsObj.getString("abstractString").split(">")[1];
						}
						if(abs.equals("应答")){//未理解
							continue;
						}
						
						// 将abstractsObj对象中key为WordPatterns的value变成wordPatternsArray数组
						JSONArray wordPatternsArray = abstractsObj.getJSONArray("wordPatterns");
						// 遍历循环abstractsArray数组，只取第一个
						// 将wordPatternsArray数组中第n个转换为json对象
						JSONObject wordPatternsObj = JSONObject.parseObject(wordPatternsArray.get(0).toString());

						// 将wordPatternsObj对象中key为entities的value变成entitiesArray数组
						JSONArray entitiesArray = wordPatternsObj.getJSONArray("entities");
						// 遍历循环abstractsArray数组
						String attr8X = "";
						String attr9Y = "";
						String attr10Z = "";
						String attr11L = "";
						String attr12M = "";
						String attr13N = "";
						for (int r = 0; r < entitiesArray.size(); r++) {
							// 将entitiesArray数组中第r个转换为json对象
							JSONObject entitiesObj = JSONObject.parseObject(entitiesArray.get(r).toString());
							if(entitiesObj.getString("key").contains("X")){
								if(StringUtils.isNotBlank(returnvalue)){//本商家的业务X不为空,使用本商家的业务X
									abs = abs.replace("X", "["+returnvalue+"]");
									attr8X = returnvalue;									
								}else{
									abs = abs.replace("X", "["+entitiesObj.getString("value")+"]");
									attr8X = entitiesObj.getString("value");									
								}

							}
							if(entitiesObj.getString("key").contains("Y")){
								abs = abs.replace("Y", "["+entitiesObj.getString("value")+"]");
								attr9Y = entitiesObj.getString("value");
							}
							if(entitiesObj.getString("key").contains("Z")){
								abs = abs.replace("Z", "["+entitiesObj.getString("value")+"]");
								attr10Z = entitiesObj.getString("value");
							}
							if(entitiesObj.getString("key").contains("L")){
								abs = abs.replace("L", "["+entitiesObj.getString("value")+"]");
								attr11L = entitiesObj.getString("value");
							}
							if(entitiesObj.getString("key").contains("M")){
								abs = abs.replace("M", "["+entitiesObj.getString("value")+"]");
								attr12M = entitiesObj.getString("value");
							}
							if(entitiesObj.getString("key").contains("N")){
								abs = abs.replace("N", "["+entitiesObj.getString("value")+"]");
								attr13N = entitiesObj.getString("value");
							}
						}

						o.put("attr8", attr8X);
						o.put("attr9", attr9Y);
						o.put("attr10", attr10Z);
						o.put("attr11", attr11L);
						o.put("attr12", attr12M);
						o.put("attr13", attr13N);
						// 得到返回值内容，并生成returnvalue对象
						//o.put("returnvalue", sb_returnValue.toString());
						
						String abstractID = abstractsObj.getString("abstractID");
						//被继承摘要不存在数据库中，执行过滤
						Result rs = getKBdataById(abstractID);
						if(rs == null || rs.getRowCount() < 1){ 
							continue;
						}
						o.put("kbdataid", abstractsObj.getString("abstractID"));
						o.put("abstracts", abs);
						o.put("city",getCityInfo(city));
						if(checkExtend(attr6,abstractsObj.getString("abstractID"))){
							o.put("state", "是");
							if(kblist.size()>0){
								for(String[] st:kblist){
									if(NewEquals.equals(abstractsObj.getString("abstractID"),st[0])){
//										o.put("flag", "是");
										st[3] = "是";
									}
								}
							}
						}else{
							o.put("state", "否");
						}
						// 将删除的对象放入creditResults数组中
						creditResults.add(o);
					}
				}
				//添加继承但没有分析到的摘要
				if(kblist.size()>0){
					for(String[] st:kblist){
						if(null==st[3]){
							JSONObject o = new JSONObject();
							o.put("kbdataid", st[0]);
							if(st[1].contains(">")){
								st[1] = st[1].split(">")[1];
							}
							if(st[1].contains("X")&&st[4]!=""){
								st[1] = st[1].replace("X", "["+st[4]+"]");
							}
							if(st[1].contains("Y")&&st[5]!=""){
								st[1]=st[1].replace("Y", "["+st[5]+"]");
							}
							if(st[1].contains("Z")&&st[6]!=""){
								st[1]=st[1].replace("Z", "["+st[6]+"]");
							}
							if(st[1].contains("L")&&st[7]!=""){
								st[1]=st[1].replace("L", "["+st[7]+"]");
							}
							if(st[1].contains("M")&&st[8]!=""){
								st[1]=st[1].replace("M", "["+st[8]+"]");
							}
							if(st[1].contains("N")&&st[9]!=""){
								st[1]=st[1].replace("N", "["+st[9]+"]");
							}
							o.put("abstracts", st[1]);
							o.put("city", getCityInfo(st[2]));
							o.put("state", "是");
							creditResults.add(o);
						}
					}
				}	
				// 定义json对象
				JSONObject o = new JSONObject();
				// 生成allsegments对象
				o.put("allsegments", allSegments);
				// 生成creditresults对象
				o.put("creditresults", creditResults);
				
				// 将生成的对象放入jsonArr数组中
				jsonArr.add(o);
			}
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
			// 将jsonArr放入jsonObj的result对象中
			jsonObj.put("result", jsonArr);
			jsonObj.put("abscity", getCityInfo(city));
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将返回结果解析失败放入jsonObj的result对象中
			jsonObj.put("result", "返回结果解析失败");
		}
		return jsonObj;
	}
	/**
	 * 本商家高级分析
	 * @param queryObject
	 * @param city
	 * @param attr6
	 * @return
	 */
	public static Object DetailAnalyzeResultByLocal(String queryObject, String city,String attr6){
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		String cityCode ="";
		String url ="";
		String provinceCode ="";
		
		if(city!=null){
			cityCode = city.replace(",", "|");
			if(!"全国".equals(cityCode)){
				provinceCode = cityCode.split("\\|")[0];
				provinceCode = provinceCode.substring(0,2)+"0000";
				if("010000".equals(provinceCode)||"000000".equals(provinceCode)){//如何为集团、电渠编码 去默认url
					provinceCode="默认";
					url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
				}else{
					url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvinceCode(provinceCode);
				}
				logger.info("继承高级分析请求【"+GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode)+"】地址：" + url);
			}else{
				cityCode ="全国";
				url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
				logger.info("继承高级分析请求【默认】地址：" + url);
			}
		}else{
			cityCode ="全国";
			url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
			logger.info("继承高级分析请求【默认】地址：" + url);
		}
		
		// 获取高级分析的客户端
		
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient.NLPCaller4WSClient(url);
		
		// 定义接口的返回串变量
		String result = "";
		// 判断接口是否为null
		if (NLPCaller4WSClient == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "分析失败");
			return jsonObj;
		}
		try {
			// 定义接口的detailAnalyze方法获取返回串
			result = NLPCaller4WSClient.detailAnalyze(queryObject);
			// 替换掉返回串中的回车符
			result = result.replace("\n", "");
			logger.info("高级分析接口的输出串：" + result);
			
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
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将分析失败信息放入jsonObj的result对象中
			jsonObj.put("result", "结果出现错误");
			return jsonObj;
		}
		try {
			// 将接口返回的json串,反序列化为json数组
			JSONArray jsonArray = JSONArray.parseArray(result);
			// 循环遍历jsonArray数组
			for (int i = 0; i < jsonArray.size(); i++) {
				// 将jsonArray数组中的第i个转换成json对象
				JSONObject obj = JSONObject.parseObject(jsonArray.get(i).toString());

				// 得到多个结果的json串
				JSONArray creditResults = new JSONArray();

				// 将obj对象中key为CreditResults的value变成json数组
				JSONArray creditResultsArray = obj.getJSONArray("creditResults");
				// 遍历循环creditResultsArray数组,只取第一组结果
				for (int k = 0; k < 1; k++) {
					// 将creditResultsArray数组中第k个转换为json对象
					JSONObject creditResultsObj = JSONObject.parseObject(creditResultsArray.get(k).toString());
					// 获取creditResultsObj对象中credit
				//	String credit = creditResultsObj.getString("credit");
					// 将creditResultsObj对象中key为abstracts的value变成abstractsArray数组
					JSONArray abstractsArray = creditResultsObj.getJSONArray("abstracts");
					// 遍历循环abstractsArray数组
					for (int m = 0; m < abstractsArray.size(); m++) {
						// 将abstractsArray数组中第m个转换为json对象
						JSONObject abstractsObj = JSONObject.parseObject(abstractsArray.get(m).toString());

						
						// 将abstractsObj对象中key为WordPatterns的value变成wordPatternsArray数组
						JSONArray wordPatternsArray = abstractsObj.getJSONArray("wordPatterns");
						// 遍历循环abstractsArray数组，只取第一个
						// 将wordPatternsArray数组中第n个转换为json对象
						JSONObject wordPatternsObj = JSONObject.parseObject(wordPatternsArray.get(0).toString());

						// 将wordPatternsObj对象中key为entities的value变成entitiesArray数组
						JSONArray entitiesArray = wordPatternsObj.getJSONArray("entities");

						for (int r = 0; r < entitiesArray.size(); r++) {
							// 将entitiesArray数组中第r个转换为json对象
							JSONObject entitiesObj = JSONObject.parseObject(entitiesArray.get(r).toString());
							String rkey = entitiesObj.getString("key");
							if("业务X".equals(rkey)){
								jsonObj.put("result", entitiesObj.getString("value"));
								break;
							}

						}
					}
				}
			}
			// 将true放入jsonObj的success对象中
			jsonObj.put("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将false放入jsonObj的success对象中
			jsonObj.put("success", false);
			// 将返回结果解析失败放入jsonObj的result对象中
			jsonObj.put("result", "返回结果解析失败");
		}
		return jsonObj;
	}
	
	
	   /**
	 *@description 获得参数配置表具体值数据源
	 *@param name  配置参数名
	 *@param key   配置参数名对应key
	 *@return 
	 *@returnType Result 
	 */
	public static Result getConfigValue(String name ,String key){
	    List<Object> lstpara = new ArrayList<Object>();
		String sql ="select distinct t.name from metafield t,metafield s,metafieldmapping a where t.metafieldmappingid=a.metafieldmappingid and t.metafieldid=s.stdmetafieldid and a.name =? and s.name like ? ";
		lstpara.add(name);
		lstpara.add("%"+key+"%");
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, lstpara.toArray());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	/**
	 * 判断是否已经继承
	 * @param attr6
	 * @param abstractid
	 * @return
	 */
	public static boolean checkExtend(String attr6 ,String abstractid){
	    List<Object> lstpara = new ArrayList<Object>();
		String sql ="select count(1) count from serviceorproductinfo where attr6 =? and abstractid =?";
		lstpara.add(attr6);
		lstpara.add(abstractid);
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, lstpara.toArray());
			if(rs.getRowCount()>0){
				 int count = Integer.parseInt(rs.getRows()[0].get("count").toString());
				if(count>0){
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 根据商家摘要id查找继承的业务摘要
	 * @param attr6
	 * @return
	 */
	public static List<String[]> getExtendKBS(String attr6){
		List<String[]> list = new ArrayList<String[]>();
	    List<Object> lstpara = new ArrayList<Object>();
		String sql ="select a.kbdataid,a.abstract abstracts,b.attr15 city,b.attr8,b.attr9,b.attr10,b.attr11,b.attr12,b.attr13 from kbdata a,serviceorproductinfo b where a.kbdataid = b.abstractid and b.attr6 =?";
		lstpara.add(attr6);
		Result rs = Database.executeQuery(sql, lstpara.toArray());
		if(rs.getRowCount()>0){
			for(int i=0;i<rs.getRowCount();i++){
				String [] ff = new String[10];
				ff[0] = rs.getRows()[i].get("kbdataid").toString();
				ff[1] = rs.getRows()[i].get("abstracts").toString();
				ff[2] = rs.getRows()[i].get("city")==null?"全国":rs.getRows()[i].get("city").toString();
				ff[4] = rs.getRows()[i].get("attr8")==null?"":rs.getRows()[i].get("attr8").toString();
				ff[5] = rs.getRows()[i].get("attr9")==null?"":rs.getRows()[i].get("attr9").toString();
				ff[6] = rs.getRows()[i].get("attr10")==null?"":rs.getRows()[i].get("attr10").toString();
				ff[7] = rs.getRows()[i].get("attr11")==null?"":rs.getRows()[i].get("attr11").toString();
				ff[8] = rs.getRows()[i].get("attr12")==null?"":rs.getRows()[i].get("attr12").toString();
				ff[9] = rs.getRows()[i].get("attr13")==null?"":rs.getRows()[i].get("attr13").toString();
				list.add(ff);
			}
		}
		return list;
	}
	
	/**
	 * 根据商家摘要id查找继承的业务摘要,填充datagrid
	 * @param attr6
	 * @return
	 */
	public static Object getExtendKbdatas(String attr6,String citycode){
//		System.out.println("******---->"+attr6);
		String city = getCityInfo(citycode);
	    List<Object> lstpara = new ArrayList<Object>();
		String sql ="select a.kbdataid,a.abstract abstracts,b.attr8,b.attr9,b.attr10,b.attr11,b.attr12,b.attr13,b.attr15 city from kbdata a,serviceorproductinfo b where a.kbdataid = b.abstractid and b.attr6 =?";
		lstpara.add(attr6);
		Result rs = Database.executeQuery(sql, lstpara.toArray());
		JSONObject json = new JSONObject();
		JSONArray jsonArr = new JSONArray();
		if(rs.getRowCount()>0){
			for(int i=0;i<rs.getRowCount();i++){
				JSONObject o = new JSONObject();
				String absStr = rs.getRows()[i].get("abstracts").toString();
				if(absStr.contains(">")){
					absStr = absStr.split(">")[1];
				}
				o.put("kbdataid", rs.getRows()[i].get("kbdataid").toString());
				SortedMap map = rs.getRows()[i];
				if(absStr.contains("X") && map.get("attr8")!= null){
					absStr = absStr.replace("X", "["+rs.getRows()[i].get("attr8").toString()+"]");
				}
				if(absStr.contains("Y") && map.get("attr9")!= null){
					absStr = absStr.replace("Y", "["+rs.getRows()[i].get("attr9").toString()+"]");
				}
				if(absStr.contains("Z") && map.get("attr10")!= null){
					absStr = absStr.replace("Z", "["+rs.getRows()[i].get("attr10").toString()+"]");
				}
				if(absStr.contains("L") && map.get("attr11")!= null){
					absStr = absStr.replace("L", "["+rs.getRows()[i].get("attr11").toString()+"]");
				}
				if(absStr.contains("M") && map.get("attr12")!= null){
					absStr = absStr.replace("M", "["+rs.getRows()[i].get("attr12").toString()+"]");
				}
				if(absStr.contains("N") && map.get("attr13")!= null){
					absStr = absStr.replace("N", "["+rs.getRows()[i].get("attr13").toString()+"]");
				}
				o.put("abstracts",  absStr);
				o.put("city", rs.getRows()[i].get("city")==null?city:getCityInfo(rs.getRows()[i].get("city").toString()));
				o.put("state", "是");
				jsonArr.add(o);
			}
		}
		json.put("result", jsonArr);
		json.put("abscity", city);
		json.put("success", true);
		return json;
	}

	/**
	 * 获取地市
	 * @param kbdataid
	 * @return
	 */
	public static String getCity(String kbdataid){
	    List<Object> lstpara = new ArrayList<Object>();
		String sql ="select city from kbdata where kbdataid =?";
		//根据四层结构串获得brand
		lstpara.add(kbdataid);
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, lstpara.toArray());
			if(rs.getRowCount()>0){
				 return rs.getRows()[0].get("city")==null?"全国":rs.getRows()[0].get("city").toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "全国";
	}
	
	/**
	 * 获取地市
	 * @param kbdataid
	 * @return
	 */
	public static String getAttr15City(String attr6 ,String abstractid){
	    List<Object> lstpara = new ArrayList<Object>();
		String sql ="select attr15 from serviceorproductinfo where attr6 =? and abstractid =?";
		//根据四层结构串获得brand
		lstpara.add(attr6);
		lstpara.add(abstractid);
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, lstpara.toArray());
			if(rs.getRowCount()>0){
				 return rs.getRows()[0].get("attr15")==null?"":rs.getRows()[0].get("attr15").toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 获取继承信息
	 * @param attr6
	 * @param abstractid
	 * @return
	 */
	public static Object getServiceInfo(String attr6 ,String abstractid){
		List<Object> lstpara = new ArrayList<Object>();
		String sql ="select attr6,attr8,attr9,attr10,attr11,attr12,attr13,attr15 from serviceorproductinfo where attr6 =? and abstractid =?";
		lstpara.add(attr6);
		lstpara.add(abstractid);
		Result rs = null;
		try {
			rs = Database.executeQuery(sql, lstpara.toArray());
			if(rs.getRowCount()>0){
				JSONObject jsonObj = new JSONObject();
//					jsonObj.put("attr5", rs.getRows()[0].get("attr5")==null?"":rs.getRows()[0].get("attr5").toString());
				jsonObj.put("attr6", rs.getRows()[0].get("attr6").toString());
				jsonObj.put("attr8", rs.getRows()[0].get("attr8")==null?"":rs.getRows()[0].get("attr8").toString());
				jsonObj.put("attr9", rs.getRows()[0].get("attr9")==null?"":rs.getRows()[0].get("attr9").toString());
				jsonObj.put("attr10", rs.getRows()[0].get("attr10")==null?"":rs.getRows()[0].get("attr10").toString());
				jsonObj.put("attr11", rs.getRows()[0].get("attr11")==null?"":rs.getRows()[0].get("attr11").toString());
				jsonObj.put("attr12", rs.getRows()[0].get("attr12")==null?"":rs.getRows()[0].get("attr12").toString());
				jsonObj.put("attr13", rs.getRows()[0].get("attr13")==null?"":rs.getRows()[0].get("attr13").toString());
				jsonObj.put("attr15", rs.getRows()[0].get("attr15")==null?"":getCityInfo(rs.getRows()[0].get("attr15").toString()));
				jsonObj.put("abstracts","");
				return jsonObj;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 保存继承
	 * @return
	 */
	public static Object saveExtend(String kbdataid,String attr6,User user,String attr8,String attr9,String attr10,String attr11,String attr12,String attr13,String attr15){
		int result = 0;
		if(null!=attr15&&attr15.contains("全国")){
			attr15 = "全国";
		}
		// 定义多条SQL语句定义的绑定参数集合
		List<String> lstSql = new ArrayList<String>();
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		Result rs = getKBdataById(attr6);
		String attr5 = rs.getRows()[0].get("abstract").toString();
		//先删除继承
		String delsql = "delete from serviceorproductinfo where attr6 =? and abstractid =?";
		lstSql.add(delsql);
		List<Object> delpara = new ArrayList<Object>();
		delpara.add(attr6);
		delpara.add(kbdataid);
		lstLstpara.add(delpara);
		
		String bussinessFlag = CommonLibMetafieldmappingDAO
				.getBussinessFlag(user.getIndustryOrganizationApplication());
		
		String serviceorproductinfoid = "";
		if(GetConfigValue.isOracle){
			serviceorproductinfoid = ConstructSerialNum.GetOracleNextValNew("serviceorproductinfo_seq", bussinessFlag);
		}else if (GetConfigValue.isMySQL) {
			serviceorproductinfoid = ConstructSerialNum.getSerialIDNew(
					"serviceorproductinfo", "serviceorproductinfoid", bussinessFlag);
		}
		//再保存继承
		String savesql = "insert into serviceorproductinfo(serviceorproductinfoid,attr4,attr5,attr6,attr7,attr8,attr9,attr10,attr11,attr12,attr13,attr15,abstractid) values(?,?,?,?,?,?,?,?,?,?,?,?,?) ";
		lstSql.add(savesql);
		List<Object> addpara = new ArrayList<Object>();
		addpara.add(serviceorproductinfoid);
		addpara.add(attr5.split(">")[0].replace("<", ""));
		addpara.add(attr5);
		addpara.add(attr6);
		addpara.add(user.getIndustryOrganizationApplication());
		addpara.add(attr8);
		addpara.add(attr9);
		addpara.add(attr10);
		addpara.add(attr11);
		addpara.add(attr12);
		addpara.add(attr13);
		addpara.add(attr15);
		addpara.add(kbdataid);
		lstLstpara.add(addpara);
		//最后更新kbdata flag
		String upsql = "update kbdata set flag=1 where kbdataid =?";
		lstSql.add(upsql);
		List<Object> uppara = new ArrayList<Object>();
		uppara.add(attr6);
		lstLstpara.add(uppara);
		
		// 生成操作日志记录
		// 将SQL语句放入集合中
		lstSql.add(GetConfigValue.LogSql());
		// 将定义的绑定参数集合放入集合中
		lstLstpara.add(GetConfigValue.LogParam(user.getUserIP(), user.getUserID(), user.getUserName(),
				" ", " ", "增加继承关系", attr5+","+attr6+","+user.getIndustryOrganizationApplication()+","+attr8+","+attr8+","+attr9+","+attr10+","+attr11+","+attr12+","+attr13+","+attr15,
				"QUERYMANAGE"));
		
		result = Database.executeNonQueryTransaction(lstSql, lstLstpara);
		JSONObject jsonObj = new JSONObject();
		if (result > 0) {
			jsonObj.put("result", true);
			jsonObj.put("msg", "继承成功！");
		} else {
			jsonObj.put("result", false);
			jsonObj.put("msg", "继承失败！");
		}
		return jsonObj;
	}
	

	/**
	 * 继承删除
	 * @param attr6
	 * @param ids
	 * @return
	 */
	public static Object deleteExtend(User user,String attr6,String ids){
		int result = 0;
		String[] kbdataids = ids.split(",");
		List<String> lstSql = new ArrayList<String>();
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		for(String id:kbdataids){
			String sql = "delete from serviceorproductinfo where attr6 =? and abstractid =?";
			lstSql.add(sql);
			List<Object> lstpara = new ArrayList<Object>();
			lstpara.add(attr6);
			lstpara.add(id);
			lstLstpara.add(lstpara);
			// 将SQL语句放入集合中
			lstSql.add(GetConfigValue.LogSql());
			String abs = CommonLibKbDataDAO.getKbdataByID(attr6);
			String _abs = CommonLibKbDataDAO.getKbdataByID(id);
			// 将定义的绑定参数集合放入集合中
			lstLstpara.add(GetConfigValue.LogParam(user.getUserIP(), user.getUserID(), user.getUserName(),
					" ", " ", "删除继承关系", "商家查摘要:"+abs+"&ID:"+attr6+",行业问题库摘要:"+_abs+"&ID:"+id,
					"QUERYMANAGE"));
		}
		try {
			result = Database.executeNonQueryTransaction(lstSql, lstLstpara);
			JSONObject jsonObj = new JSONObject();
			if (result > 0) {
				jsonObj.put("result", true);
				jsonObj.put("msg", "删除成功！");
			}else{
				jsonObj.put("result", false);
				jsonObj.put("msg", "删除失败！");
			}
			return jsonObj;	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 获取摘要信息
	 * @param kbdataid
	 * @return
	 */
	public static Result getKBdataById(String kbdataid){
	    List<Object> lstpara = new ArrayList<Object>();
		String sql ="select abstract,city from kbdata where kbdataid =?";
		//根据四层结构串获得brand
		lstpara.add(kbdataid);
		Result rs = null;
		rs = Database.executeQuery(sql, lstpara.toArray());
		return rs;
	}
	
	/**
	 * 批量保存继承
	 * @param attr6
	 * @param ids
	 * @return
	 */
	public static Object batchSave(List<ExtendsDto> listDto){
		JSONObject jsonObj = new JSONObject();
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		int result = 0;
//		String[] kbdataids = ids.split(",");
		List<String> lstSql = new ArrayList<String>();
		List<List<?>> lstLstpara = new ArrayList<List<?>>();
		Result rsAttr6 = getKBdataById(listDto.get(0).getAttr6());
		String bussinessFlag = CommonLibMetafieldmappingDAO
				.getBussinessFlag(user.getIndustryOrganizationApplication());
		
		
		for(ExtendsDto dto:listDto){
			
			String fatherAbstractID = dto.getId();
			//继承返回值默认key
			String returnValue[] = { "业务X", "业务Y","业务Z","业务L", "业务M","业务N" };

			List<String> returnValueList = new ArrayList<String>();
			for (int r = 0; r < returnValue.length; r++) {
				String returnString = returnValue[r];
				if (returnString.startsWith("业务")) {// 判断返回值key是否是继承相关返回值，如果是则加入returnValueList中
					returnValueList.add(returnString);
				}
			}
			
			// 查看被继承摘要 SERVICEATTRNAME2COLNUM 中是否存在列名对应关系，如果没有新建，如果和
			// returnValue比较有缺失则补全
			Result rs = CommonLibKbdataAttrDAO.getServiceattrname2colnum("abstractid",fatherAbstractID);
			// 定义存放已有列名
			List<String> colnumList = new ArrayList<String>();
			int i = 0;
			int colnum = 0;
			if (rs != null && rs.getRowCount() > 0) {
				// 循环遍历数据源
				for (int n = 0; n < rs.getRowCount(); n++) {
					// 获取属性值
					Object name = rs.getRows()[n].get("name");
					colnumList.add(name.toString());
					colnum = Integer.valueOf(rs.getRows()[n].get("columnnum")
							.toString());
					if (colnum > i) {// 获得最大colnum值
						i = colnum;
					}
				}
				returnValueList.removeAll(colnumList);// 移除相同的列
				// 如果存在不同列，做补全
				if (returnValueList.size() > 0) {
					if (!KbdataAttrDAO
							.InsertAttrName(fatherAbstractID,
									(String[]) returnValueList
											.toArray(new String[returnValueList
													.size()]), i)) {
						jsonObj.put("result", false);
						// 将信息放入jsonObj的msg对象中
						jsonObj.put("msg", "插入元素对应关系表失败!");
						return jsonObj;
					}
				}
			} else {// 列不存在，新建
				List<String> nameList = Arrays.asList(names);
				returnValueList.removeAll(nameList);
				List<String> newList = new ArrayList<String>();
				newList.addAll(nameList);
				newList.addAll(returnValueList);
				if (!KbdataAttrDAO.InsertAttrName(fatherAbstractID,(String[]) newList.toArray(new String[newList.size()]),i)) {
					jsonObj.put("result", false);
					// 将信息放入jsonObj的msg对象中
					jsonObj.put("msg", "插入元素对应关系表失败!");
					return jsonObj;
				}
			}
						
			String serviceorproductinfoid = "";
			if(GetConfigValue.isOracle){
				serviceorproductinfoid = ConstructSerialNum.GetOracleNextValNew("serviceorproductinfo_seq", bussinessFlag);
			}else if (GetConfigValue.isMySQL) {
				serviceorproductinfoid = ConstructSerialNum.getSerialIDNew(
						"serviceorproductinfo", "serviceorproductinfoid", bussinessFlag);
			}
			String sql = "insert into serviceorproductinfo(serviceorproductinfoid,attr4,attr5,attr6,attr7,attr8,attr9,attr10,attr11,attr12,attr13,attr15,abstractid) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
			lstSql.add(sql);
			List<Object> lstpara = new ArrayList<Object>();
			String abs = rsAttr6.getRows()[0].get("abstract").toString();
			String service =abs.split(">")[0].replace("<", "");
			lstpara.add(serviceorproductinfoid);
			lstpara.add(service);
			lstpara.add(abs);
			lstpara.add(dto.getAttr6());
			lstpara.add(user.getIndustryOrganizationApplication());
			lstpara.add(dto.getAttr8());
			lstpara.add(dto.getAttr9());
			lstpara.add(dto.getAttr10());
			lstpara.add(dto.getAttr11());
			lstpara.add(dto.getAttr12());
			lstpara.add(dto.getAttr13());
			lstpara.add(rsAttr6.getRows()[0].get("city").toString());
			lstpara.add(dto.getId());
			lstLstpara.add(lstpara);
			// 生成操作日志记录
			// 将SQL语句放入集合中
			lstSql.add(GetConfigValue.LogSql());
			// 将定义的绑定参数集合放入集合中
			lstLstpara.add(GetConfigValue.LogParam(user.getUserIP(), user.getUserID(), user.getUserName(),
					" ", " ", "增加继承关系", "attr4,attr5,attr6,attr7,attr8,attr9,attr10,attr11,attr12,attr13,attr15,abstractid=>" +lstpara.toString(),
					"QUERYMANAGE"));
		}
		try {
			result = Database.executeNonQueryTransaction(lstSql, lstLstpara);
			if (result > 0) {
				jsonObj.put("result", true);
				jsonObj.put("msg", "保存成功！");
			}else{
				jsonObj.put("result", false);
				jsonObj.put("msg", "保存失败！");
			}
			return jsonObj;	
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 调用高级分析接口，更新知识库，并返回相应的信息
	 * @return json串
	 */
	public static Object updateKB() {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		List<String> cityList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
				.resourseAccess(user.getUserID(), "querymanage", "S");
		cityList = resourseMap.get("地市");
		String url ="";
		String provinceCode="";
		if (cityList != null&&!cityList.contains("全国")) {
			String  cityCode = cityList.get(0);
			provinceCode  = cityCode.substring(0,2)+"0000";
			url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvinceCode(provinceCode);
			logger.info("更新知识库请求【"+GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode)+"】地址"+url);
		}else{
			url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
			logger.info("更新知识库请求【默认】地址"+url);
		}
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient(url);
		
		// 判断接口为null
		if (NLPCaller4WSClient == null) {
			// 将分析失败放入jsonObj的result对象中
			jsonObj.put("result", "分析失败！");
			return jsonObj;
		}
		try {
			logger.info("【"+userid+"】更新知识库请求开始......");
			// 调用接口的updateKB方法
			NLPCaller4WSClient.updateKB();
			logger.info("【"+userid+"】更新知识库请求结束......");
			// 将更新完毕放入jsonObj的result对象中
			jsonObj.put("result", "更新完毕！");
			return jsonObj;
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将更新出现错误放入jsonObj的result对象中
			jsonObj.put("result", "更新出现错误！");
			logger.info("【"+userid+"】更新知识库请求异常......");
			return jsonObj;
		}
	}
	
	/**
	 * 新版的nlp知识库更新接口
	 * 
	 * 输入参数内容：
	 * 	{
		    "MessageData":"",
		    "KEY":"TESTKEY",
		    "OperationName":"UpdateKbase",
		    "OperationCategory":""
		}
	 * 参数说明：
		MessageData：每次传的数据体
		KEY：一个验证请求是否合法的串
		OperationName：操作名称
		OperationCategory：操作类别
		编码 utf-8
	 * 
	 * @param ip参数ip地址  
	 * @return json串
	 */
	public static Object updateKBNew() {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		List<String> cityList = new ArrayList<String>();
		HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
				.resourseAccess(user.getUserID(), "querymanage", "S");
		cityList = resourseMap.get("地市");
		String url ="";
		String provinceCode="";
		if (cityList != null&&!cityList.contains("全国")) {
			String  cityCode = cityList.get(0);
			provinceCode  = cityCode.substring(0,2)+"0000";
			url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvinceCode(provinceCode);
			logger.info("更新知识库请求【"+GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode)+"】");
		}else{
			url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
			logger.info("更新知识库请求【默认】");
		}
		
		String port = "3456";
		String path = "/test";
		Result rs = CommonLibMetafieldmappingDAO.getConfigValue("更新知识库配置", "地址");
		if(rs != null && rs.getRowCount() > 0){
			for(int i =0 ;i<rs.getRowCount();i++){
				String value = Objects.toString(rs.getRows()[i].get("name"),"");
				if(value.startsWith("端口")){
					port = value.substring(value.lastIndexOf('=')+1);
				}
				if(value.startsWith("路径")){
					path = value.substring(value.lastIndexOf('=')+1);
				}
			}
		}
		// 获取更新服务器的ip
		String urlHost = getUrlHost(url);
		String updateKbUrl = "http://" + urlHost +":"+port +path;
		logger.info("更新知识库请求地址:"+updateKbUrl);
		String username =  user.getUserName();
		try {
			JSONObject param = new JSONObject();
			param.put("MessageData", "");
			param.put("KEY", "TESTKEY");
			param.put("OperationName", "UpdateKbase");
			param.put("OperationCategory", "");
			
			logger.info("【"+username+"】更新知识库请求开始......");
			String sendPost = HttpClientUtil.sendPost(updateKbUrl, param);
			logger.info("【"+username+"】更新知识库请求结果："+sendPost);
			JSONObject object = JSON.parseObject(sendPost);
			if(object != null && "success".equals(object.getString("Status"))){
				// 将更新完毕放入jsonObj的result对象中
				jsonObj.put("result", "更新完毕！");
			}else{
				// 将更新出现错误放入jsonObj的result对象中
				jsonObj.put("result", "更新出现错误！");
			}
			logger.info("【"+username+"】更新知识库请求结束......");
			return jsonObj;
		} catch (Exception e) {
			e.printStackTrace();
			// 出现错误
			// 将更新出现错误放入jsonObj的result对象中
			jsonObj.put("result", "知识库更新出现错误!");
			logger.info("【"+username+"】更新知识库请求异常......");
			return jsonObj;
		}
	}
	private static String getUrlHost(String url){
		if(StringUtils.isEmpty(url) && !url.contains("http"))
			return "";
		
		URI uri = null;
        try {
        	 uri = URI.create(url);
        	 return uri.getHost();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return "";
		        
	}
	private static String getCityInfo(String cityCodes) {
		Result rs = CommonLibMetafieldmappingDAO.getConfigMinValue("地市编码配置");
		Map<String,String> cityMap = null;
		if (rs != null) {
			int length = rs.getRowCount();
			cityMap = new HashMap<String, String>();
			for (int i = 0; i < length; i++) {
				String cityCode = rs.getRows()[i].get("name").toString();
				String cityName = rs.getRows()[i].get("k").toString();
				cityMap.put(cityCode, cityName);
			}
		}
		String citys = "";
		if (cityCodes==null || cityCodes.equals("")) {// 如果地市编码不存在
			return citys;
		}
		String[] cityArray = cityCodes.split(",");
		for (String city : cityArray) {
			if (city.equals("全国")) {
				citys += "全国,";
			} else if(cityMap.get(city) != null){
				citys += cityMap.get(city) + ",";
			}
		}
		if (citys.endsWith(",")) {
			citys = citys.substring(0, citys.lastIndexOf(","));
		}
		return citys;
	}

	/**
	 * 构造业务树
	 * @param serviceid 业务id
	 * @return
	 */
	public static Object createServiceTree(String serviceid) {
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		// 获取行业
		String industry = user.getIndustryOrganizationApplication();
		industry = industry.substring(0, industry.indexOf("-")) + "问题库";
		// 定义返回的json串
		JSONArray array = new JSONArray();  
		String sql = "";
		if ("".equals(serviceid) || serviceid == null) {// 加载根业务
			sql = "select serviceid,service from service where service='"+industry+"'";
		} else {// 根据父业务id，加载子业务id
			sql = "select serviceid,service from service where parentid="+serviceid;
		}
		Result rs = null;
		rs = Database.executeQuery(sql);
		if (rs!=null && rs.getRowCount()>0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", rs.getRows()[i].get("serviceid").toString());
				jsonObj.put("text", rs.getRows()[i].get("service").toString());
				if (hasChild(rs.getRows()[i].get("serviceid").toString())==0) {// 如果没有子业务
					jsonObj.put("iconCls", "icon-servicehit");
				}
				jsonObj.put("state", "closed");
				array.add(jsonObj);
			}
		}
		return array;
	}
	
	/**
	 * 是否有子业务
	 * @param serviceid 业务id
	 * @return
	 */
	private static int hasChild(String serviceid) {
		int count = 0;
		String sql = "select count(*) as nums from service where parentid="+serviceid;
		Result rs = Database.executeQuery(sql);
		if (rs != null) {
			count = Integer.parseInt(rs.getRows()[0].get("nums").toString());
		}
		return count;
	}
	
	/**
	 * 构造摘要下拉框
	 * @param serviceid 业务id
	 * @return
	 */
	public static Object createCombobox(String serviceid) {
		if ("".equals(serviceid)) {
			return "";
		}
		String sql = "select kbdataid,abstract from kbdata where serviceid=" + serviceid + " and topic like '复用-%'";
		Result rs = Database.executeQuery(sql);
		// 定义返回的json串
		JSONArray array = new JSONArray();  
		if (rs != null && rs.getRowCount() > 0) {
			for (int i = 0; i < rs.getRowCount(); i++) {
				JSONObject jsonObj = new JSONObject();
				jsonObj.put("id", rs.getRows()[i].get("kbdataid").toString());
				jsonObj.put("text", rs.getRows()[i].get("abstract").toString());
				array.add(jsonObj);
			}
		}
		return array;
	}

	/**
	 * 报错功能
	 * @param standAbstract
	 * @param synonymyAbstract
	 * @param city
	 * @return
	 */
	public static Object reportError(String standAbstract,
			String synonymyAbstract, String city, String reason, String understandresult) {
		// 返回值
		JSONObject jsonObj = new JSONObject();
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userID = user.getUserID();
		// 获取地市
		if (null == city || "".equals(city)){
			HashMap<String, ArrayList<String>> resourseMap = CommonLibPermissionDAO
			.resourseAccess(userID, "querymanage", "S");
			List<String> cityList = new ArrayList<String>();
			cityList = resourseMap.get("地市");
			if (cityList != null) {
				city = cityList.get(0);
			}
		} else {
			String citys[] = city.split(",");
			city = citys[0];
		}
		
		String insertProvince = null;
		String insertCity = null;
		if ("全国".equals(city)){
			insertProvince = city;
			insertCity = city;
		} else {
			insertProvince = city.substring(0, 2) + "0000";
		}
		
		String[] synonymyAbstracts = null;
		if (synonymyAbstract != null && !"".equals(synonymyAbstract)){
			synonymyAbstracts = synonymyAbstract.split("\\|");
		}
		
		// 定义多条SQL语句集合
		List<String> lsts = new ArrayList<String>();
		// 定义多条SQL语句对应的绑定参数集合
		List<List<?>> lstlstpara = new ArrayList<List<?>>();
		
		// 查询父亲问法是否存在
		String tempSql = "select hotquestionid from hotquestion where parentid is null and question ='"
			+ standAbstract 
			+ "'";
		Result rs = Database.executeQuery(tempSql);
		int parentid = 0;
		// 父亲问法存在
		if (rs != null && rs.getRowCount() > 0){
			parentid = Integer.valueOf(rs.getRows()[0].get("hotquestionid").toString());
		} else { // 父亲问法不存在
			if(GetConfigValue.isMySQL){
				parentid = ConstructSerialNum.getSerialID("hotquestion","hotquestionid");
			} else{
				parentid = ( ConstructSerialNum.GetOracleNextVal("hotquestionid_seq"));
			}
			// 定义sql
			String sql = "insert into hotquestion (hotquestionid,question,username,servicetype) values (?,?,?,?)";
			// 定义绑定参数集合
			List<Object> lstpara = new ArrayList<Object>();
			lstpara.add(parentid);
			lstpara.add(standAbstract);
			lstpara.add(userID);
			lstpara.add(user.getIndustryOrganizationApplication());
			
			lsts.add(sql);
			lstlstpara.add(lstpara);
		}
		
		if (null != synonymyAbstracts && synonymyAbstracts.length > 0){// 共享语义存在
			for (int i = 0;i < synonymyAbstracts.length;i++){
				
				// 去重
				tempSql = "select s.hotquestionid,s.status from hotquestion s, hotquestion p where p.hotquestionid = s.parentid and p.question = '" 
					+ standAbstract
					+ "' and s.question ='"
					+ synonymyAbstracts[i]
					+ "' and s.province='"
					+ insertProvince
					+ "'";
				Result result = Database.executeQuery(tempSql);
				if (result != null && result.getRowCount() > 0){// 重复
					// 看状态
					String status = result.getRows()[0].get("status").toString();
					String hotquestionid = result.getRows()[0].get("hotquestionid").toString();
					if ("-1".equals(status)||NewEquals.equals("-1",status)){
						continue;
					}else {
						// 定义sql
						String sql = "update hotquestion set status=-1,reason=?,result=? where hotquestionid=?";
						// 定义绑定参数集合
						List<Object> lstpara = new ArrayList<Object>();
						
						if (reason != null && !"".equals(reason)){
							if ("-1".equals(reason) || NewEquals.equals("-1",reason)){
								lstpara.add("理解不一致");
							}else if ("-2".equals(reason)||NewEquals.equals("-2",reason)){
								lstpara.add("理解无结果");
							}else {
								lstpara.add("理解不正确");
							}
						}else{
							lstpara.add("共享语义信息不正确");
						}
						lstpara.add(understandresult);
						lstpara.add(hotquestionid);
						
						lsts.add(sql);
						lstlstpara.add(lstpara);
					}
				}
				
				int seq = 0;
				if(GetConfigValue.isMySQL){
					seq = ConstructSerialNum.getSerialID("hotquestion","hotquestionid");
				} else{
					seq =  (ConstructSerialNum.GetOracleNextVal("hotquestionid_seq"));
				}
				// 定义sql
				String sql = "insert into hotquestion (hotquestionid,question,parentid,province,city,status,uploadtime,username,servicetype,reason,result) values (?,?,?,?,?,?,sysdate,?,?,?,?)";
				// 定义绑定参数集合
				List<Object> lstpara = new ArrayList<Object>();
				lstpara.add(seq);
				lstpara.add(synonymyAbstracts[i]);
				lstpara.add(parentid);
				lstpara.add(insertProvince);
				lstpara.add(insertCity);
				lstpara.add(-1);
				
				lstpara.add(userID);
				lstpara.add(user.getIndustryOrganizationApplication());
				if (reason != null && !"".equals(reason)){
					if ("-1".equals(reason) ||NewEquals.equals("-1",reason)){
						lstpara.add("理解不一致");
					}else if ("-2".equals(reason)||NewEquals.equals("-2",reason)){
						lstpara.add("理解无结果");
					}else {
						lstpara.add("理解不正确");
					}
				}else{
					lstpara.add("共享语义信息不正确");
				}
				lstpara.add(understandresult);
				
				lsts.add(sql);
				lstlstpara.add(lstpara);
			}
		} else {// 共享语义不存在
			
			// 去重
			tempSql = "select s.hotquestionid,s.status from hotquestion s, hotquestion p where p.hotquestionid = s.parentid and p.question = '" 
				+ standAbstract
				+ "' and s.question ='"
				+ standAbstract
				+ "' and s.province='"
				+ insertProvince
				+ "'";
			Result result = Database.executeQuery(tempSql);
			if (result != null && result.getRowCount() > 0){// 重复
				// 看状态
				String status = result.getRows()[0].get("status").toString();
				String hotquestionid = result.getRows()[0].get("hotquestionid").toString();
				if ("-1".equals(status) ||NewEquals.equals("-1",status)){
					jsonObj.put("success", false);
					jsonObj.put("result", "请勿重复报错！");
					return jsonObj;
				}else {
					// 定义sql
					String sql = "update hotquestion set status=-1,reason=?,result=? where hotquestionid=?";
					// 定义绑定参数集合
					List<Object> lstpara = new ArrayList<Object>();
					
					lstpara.add("缺少共享语义信息");
					lstpara.add(hotquestionid);
					lstpara.add(understandresult);
					
					lsts.add(sql);
					lstlstpara.add(lstpara);
				}
				
			}
			
			int seq = 0;
			if(GetConfigValue.isMySQL){
				seq = ConstructSerialNum.getSerialID("hotquestion","hotquestionid");
			} else{
				seq =  (ConstructSerialNum.GetOracleNextVal("hotquestionid_seq"));
			}
			// 定义sql
			String sql = "insert into hotquestion (hotquestionid,question,parentid,province,city,status,uploadtime,username,servicetype,reason,result) values (?,?,?,?,?,?,sysdate,?,?,?,?)";
			// 定义绑定参数集合
			List<Object> lstpara = new ArrayList<Object>();
			lstpara.add(seq);
			lstpara.add(standAbstract);
			lstpara.add(parentid);
			lstpara.add(insertProvince);
			lstpara.add(insertCity);
			lstpara.add(-1);
			
			lstpara.add(userID);
			lstpara.add(user.getIndustryOrganizationApplication());
			lstpara.add("缺少共享语义信息");
			lstpara.add(understandresult);
			
			lsts.add(sql);
			lstlstpara.add(lstpara);
		}
		int r = -1; 
		r = Database.executeNonQueryTransaction(lsts, lstlstpara);
		if (r > 0){
			jsonObj.put("success", true);
			jsonObj.put("result", "报错成功！");
			return jsonObj;
		} else if (r == 0){
			jsonObj.put("success", false);
			jsonObj.put("result", "请勿重复报错！");
			return jsonObj;
		}
		jsonObj.put("success", false);
		jsonObj.put("result", "报错失败，请联系管理员！");
		return jsonObj;
	}
	
}
