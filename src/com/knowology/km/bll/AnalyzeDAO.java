package com.knowology.km.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.jstl.sql.Result;



import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.knowology.Bean.User;
import com.knowology.bll.CommonLibKbDataDAO;
import com.knowology.bll.CommonLibMetafieldmappingDAO;
import com.knowology.bll.CommonLibQueryManageDAO;
import com.knowology.km.NLPCallerWS.NLPCaller4WSDelegate;
import com.knowology.km.entity.InsertOrUpdateParam;
import com.knowology.km.util.Check;
import com.knowology.km.util.GetLoadbalancingConfig;
import com.knowology.km.util.GetSession;
import com.knowology.km.util.MyUtil;
import com.knowology.km.util.ReadExcel;
import com.knowology.km.util.SimpleString;
import com.knowology.km.util.getServiceClient;

/**
 * 语义训练业务
 * 
 * @author cwy-pc
 * 
 */
public class AnalyzeDAO {
	public static Logger logger = Logger.getLogger("querymanage");
	public static Map<String,Set<String>> optionWordMap = new ConcurrentHashMap<>();
	public static Map<String,List<String>> replaceWordMap = new ConcurrentHashMap<>();
	
	/**
	 *@description 生成词模
	 *@param combition
	 *@param request
	 *@return
	 *@returnType Object
	 */
	public static Object produceWordpat(String combition,
			HttpServletRequest request) {
		return produceWordpat(combition, "5", request);
	}
	/**
	 *@description 生成词模
	 *@param combition
	 *@param removeFlag 排除问题-是否严格排除标识
	 *@param request
	 *@return
	 *@returnType Object
	 */
	public static Object produceWordpat(String combition,String wordpattype,
			HttpServletRequest request) {
		JSONObject jsonObj = new JSONObject();
		
		Object sre = GetSession.getSessionByKey("accessUser");
		User user = (User) sre;
		String userid = user.getUserID();
		// 获取行业
		String servicetype = user.getIndustryOrganizationApplication();
		String combitionArray[] = combition.split("@@");
		
		List<List<String>> list = new ArrayList<List<String>>();//待插入的词模数据
		List<List<String>> text = new ArrayList<List<String>>();//错误报告数据
		
		int filterCount = 0;//过滤的自学习词模数量
		if(StringUtils.isEmpty(wordpattype)){
			wordpattype = "5";
		}
//		String wordpattype = "5";//词模类型
		String autoWordpatRule = "默认方式";//客户问自学习规则
		//读取自学习规则配置，商家默认配置
		Result configValue = CommonLibMetafieldmappingDAO.getConfigValue("客户问自学习规则", servicetype);
		if(configValue != null && configValue.getRowCount() > 0){
			autoWordpatRule = Objects.toString(configValue.getRows()[0].get("name"),"");
			if("分词学习".equals(autoWordpatRule)){
				wordpattype = "0";
			}
		}
		//可选词配置
		Set<String> set = optionWordMap.get(servicetype);
		if(set == null){
			set = new HashSet<>();
			Result optionWordRs = CommonLibMetafieldmappingDAO.getConfigValue("文法学习可选词类配置", servicetype);
			if(optionWordRs != null && optionWordRs.getRowCount() > 0){
				for(int i =0 ;i< optionWordRs.getRowCount();i++){
					String optionWord = Objects.toString(optionWordRs.getRows()[i].get("name"),"");
					set.add(optionWord);
				}
			}
			optionWordMap.put(servicetype, set);
		}
		//可选词配置
		List<String> replaceList = replaceWordMap.get(servicetype);
		if(replaceList == null){
			replaceList = new ArrayList<>();
			Result option = CommonLibMetafieldmappingDAO.getConfigValue("文法学习词类替换配置", servicetype);
			if(option != null && option.getRowCount() > 0){
				for(int i =0 ;i< option.getRowCount();i++){
					String s = Objects.toString(option.getRows()[i].get("name"),"");
					replaceList.add(s);
				}
			}
			replaceWordMap.put(servicetype, replaceList);
		}
		
		//获取摘要
		List<String> kbIdList = new ArrayList<String>();
		for(int i = 0; i < combitionArray.length; i++){
			String queryArray[] = combitionArray[i].split("@#@");
			kbIdList.add(queryArray[2]);
		}
		//生成客户问和对应的自学习词模  <客户问，词模>
		Map<String,String> wordpatMap = getAutoWordpatMap(kbIdList,wordpattype);
		List<String> oovWordList = new ArrayList<String>();
		List<String> wordpatResultList = new ArrayList<String>();
		
		for (int i = 0; i < combitionArray.length; i++) { 
			String queryArray[] = combitionArray[i].split("@#@");
			String queryCityCode = queryArray[0];
			String query = queryArray[1];
			String kbdataid = queryArray[2];
			String queryid = queryArray[3];
			//排除问题的严格排除状态
			String isstrictexclusion = queryArray[4];
			
			List<String> wordpatList = new ArrayList<String>();
			String wordpat = null;
			//调用高析接口生成词模
			if("0".equals(wordpattype)){
				JSONObject jsonObject = getWordpat2(servicetype,query,queryCityCode);
				if(jsonObject.getBooleanValue("success")){
					// 将简单词模转化为普通词模，并返回转换结果
					wordpat = SimpleString.SimpleWordPatToWordPat(jsonObject.getString("wordpat"));
					wordpatList.add(wordpat);
					wordpat = SimpleString.SimpleWordPatToWordPat(jsonObject.getString("lockWordpat"));
					wordpatList.add(wordpat);
					oovWordList.add(StringUtils.join(jsonObject.getJSONArray("OOVWord"), "$_$"));// 放入OOV分词
				}
			}else{
				// 调用自学习生成词模的接口生成词模,可能是多个，以@_@分隔
				wordpat = callKAnalyze(servicetype, query, queryCityCode);
				wordpatList.add(wordpat);
			}
			
			for(int j =0 ;j<wordpatList.size();j++){
				wordpat = wordpatList.get(j);
				for(String rep : replaceList){
					if(rep.contains("=>")){
						String[] split = rep.split("=>");
						wordpat.replaceAll(split[0], split[1]);
					}
				}
				List<String> rows = new ArrayList<String>();//生成词模失败报告
//				logger.info("问题库自学习词模：" + wordpat);
				if (wordpat != null && !"".equals(wordpat)) {
					// 判断词模是否含有@_@
					if (wordpat.contains("@_@")) {
						// 有的话，按照@_@进行拆分,并只取第一个
						wordpat = wordpat.split("@_@")[0];
					}
					// 获取词模中@前面的词模题，在加上@2#编者="问题库"&来源="(当前问题)"
//					wordpat = wordpat.split("@")[0] + "@2#编者=\"问题库\"&来源=\""
//							+ query.replace("&", "\\and") + "\"";
					
	                //保留自学习词模返回值，并替换 编者=\"自学习\""=>编者="问题库"&来源="(当前问题)" ---> modify 2017-05-24
					wordpat = wordpat.replace("编者=\"自学习\"", "编者=\"问题库\"&来源=\""+ query.replace("&", "\\and") + "\"");
					
					if (isstrictexclusion != null && "是".equals(isstrictexclusion) && "2".equals(wordpattype)) {
						wordpat = wordpat + "&最大未匹配字数=\"0\"";
					}
					// 校验自动生成的词模是否符合规范
					if (Check.CheckWordpat(wordpat, request)) {
						//获取客户问对应的旧词模
						String oldWordpat = wordpatMap.get(query);
						//存在旧词模
						if(oldWordpat != null && !"".equals(oldWordpat)){
							String newWordpat = wordpat.split("@2#")[0];
							logger.info("新旧词模比较 ----新词模：\""+newWordpat+"\"，旧词模：\""+oldWordpat+"\"，针对问题：\""+query+"\"");
							//新旧词模不相同，执行插入
							if(!oldWordpat.equals(newWordpat)){
								List<String> tempList = new ArrayList<String>();
								tempList.add(wordpat);
								tempList.add(queryCityCode);
								tempList.add(query);
								tempList.add(kbdataid);
								tempList.add(queryid);
								list.add(tempList);
								wordpatResultList.add(wordpat);
							}else{//新旧词模相同，不进行插入操作
								filterCount ++;//记录过滤的词模数量
							}
						}else{//不存在旧词模
							List<String> tempList = new ArrayList<String>();
							tempList.add(wordpat);
							tempList.add(queryCityCode);
							tempList.add(query);
							tempList.add(kbdataid);
							tempList.add(queryid);
							list.add(tempList);
							wordpatResultList.add(wordpat);
						}
					}else{
						rows.add(query);
						rows.add("生成词模【"+wordpat+"】格式不符合！");
						text.add(rows);
					}
				}else{
					rows.add(query);
					rows.add("生成失败！");
					text.add(rows);
				}
			}
		}
		
		logger.info("批量训练----客户问个数："+combitionArray.length +"，插入词模个数："+list.size() +"，过滤词模的个数："+filterCount);
		
		
		// 插入问题库自动学习词模
		int count = -1;
		if (list.size() > 0) {
			count = CommonLibQueryManageDAO.insertWordpat(list, servicetype, userid,wordpattype);
			if (count > 0) {
				jsonObj.put("success", true);
				jsonObj.put("msg", "生成成功!");
				jsonObj.put("wordpatList", StringUtils.join(wordpatResultList, "@_@"));
				jsonObj.put("OOVWord", StringUtils.join(oovWordList, "$_$"));
			} else {
				jsonObj.put("success", false);
				jsonObj.put("msg", "生成失败!");
			}
		} else if(combitionArray.length >= list.size() + filterCount
				&& list.size() + filterCount > 0) {//有成功处理的词模就算生成成功
			jsonObj.put("success", true);
			jsonObj.put("msg", "生成成功!");
			jsonObj.put("wordpatList", StringUtils.join(wordpatResultList, "@_@"));
			jsonObj.put("OOVWord", StringUtils.join(oovWordList, "$_$"));
		}else {
			jsonObj.put("success", false);
			jsonObj.put("msg", "生成失败!!");
		}
		if(text.size() > 0 && "分词学习".equals(autoWordpatRule)){
			//错误报告
			String filename = "produceWordpat_";
			filename += DateFormatUtils.format(new Date(), "yyyyMMddHHmmss");
			List<String> colTitle = new ArrayList<String>();
			colTitle.add("客户问");
			colTitle.add("生成结果");
			boolean isWritten = ReadExcel.writeExcel(QuerymanageDAO.FILE_PATH_EXPORT, filename, null, null, colTitle, text);
			if(isWritten){
//				file = new File(FILE_PATH_EXPORT + filename + ".xls");
				jsonObj.put("fileName", filename+ ".xls");
			}
			
		}
		return jsonObj;

	}
	
	/**
	 *@description 全量生成词模
	 *@param serviceid
	 *@param request
	 *@return
	 *@returnType Object
	 */
	public static Object produceAllWordpat(String serviceid,
			HttpServletRequest request) {
		List<String> combitionArray = getAllQuery(serviceid,0);
		return produceWordpat(StringUtils.join(combitionArray, "@@"), request);
	}
	
	/**
	 *@description 全量生成词模
	 *@param serviceid
	 *@param request
	 *@return
	 *@returnType Object
	 */
	public static Object produceAllWordpat(String serviceid,String wordpattype,
			HttpServletRequest request) {
		List<String> combitionArray = getAllQuery(serviceid,0);
		List<String> RemoveCombitionArray = getAllQuery(serviceid,1);
		Object obj = produceWordpat(StringUtils.join(RemoveCombitionArray, "@@"),"2", request);
		logger.info("标准问全量生成排除词模结果："+JSONObject.toJSONString(obj));
		return produceWordpat(StringUtils.join(combitionArray, "@@"),wordpattype, request);
	}
	
	
	/**
	 * 调用高析接口生成自学习词模
	 * <br>
	 * 如果没有有效的词模，则返回空
	 * @param servicetype 行业商家
	 * @param query 问题
	 * @param queryCityCode 问题地市
	 * @return
	 * 	success : 生成结果
	 * 	如果生成成功，结果中wordpat为词模<br>
	 * 	如果生成失败，结果中detailAnalyzeResult和wordpatResult分别存高析结果和生成词模结果
	 */
	public static JSONObject getWordpat(String servicetype,String query,String queryCityCode,String autor) {
		JSONObject result = new JSONObject();
		result.put("success", false);
		//调用高析接口
		JSONObject jsonObject = callDetailAnalyze(servicetype, query, queryCityCode,autor);
		
		//解析高析接口结果，生成词模
		List<JSONObject> list = null;
		if(jsonObject.containsKey("detailAnalyze")){
			list = detailAnalyze2Wordpat(jsonObject.getString("detailAnalyze"), autor);
		}else{
			result.put("detailAnalyzeResult", jsonObject);
		}
		//判断是否有有效词模
		if(list != null && list.size() > 0 ){
			result.put("success", true);
			result.put("wordpatResult", list);
		}
		logger.info("分词结果："+result);
		return result;
	}
	
	/**
	 * 采用文法训练策略
	 * <br>
	 * 如果没有有效的词模，则返回空
	 * @param servicetype 行业商家
	 * @param query 问题
	 * @param queryCityCode 问题地市
	 * @return
	 * 	success : 生成结果
	 * 	如果生成成功，结果中wordpat为词模和lockWordpat为语义锁定词模<br>
	 * 	如果生成失败，结果中detailAnalyzeResult和wordpatResult分别存高析结果和生成词模结果
	 */
	public static JSONObject getWordpat2(String servicetype,String query,String queryCityCode) {
		JSONObject result = new JSONObject();
		result.put("success", false);
		//调用高析接口
		JSONObject jsonObject = callDetailAnalyze(servicetype, query, queryCityCode,"自学习");
		
		//解析高析接口结果，生成词模
		List<JSONObject> list = null;
		if(jsonObject.containsKey("detailAnalyze")){
			list = detailAnalyze4Wordpat(jsonObject.getString("detailAnalyze"), "自学习",optionWordMap.get(servicetype));
		}else{
			result.put("detailAnalyzeResult", jsonObject);
		}
		
		//取第一个词模
		if(list != null && list.size() > 0 ){
			JSONObject object = list.get(0);
			result.put("success", true);
			result.put("wordpat", object.getString("wordpat"));
			result.put("lockWordpat", object.getString("lockWordpat"));
			result.put("OOVWord", object.getJSONArray("OOVWord"));
			return result;
		}
		return result;
	}
	
	/**
	 * 生成自学习词模
	 * 
	 * @param 参数调用接口的入参
	 * @return 词模
	 */
	public static String callKAnalyze(String servicetype,String query, String queryCityCode) {
		
		//获取负载均衡url
		String url = getUrlLoadbalanceUrl(queryCityCode);
		//如果未配置对应省份的负载服务器，默认使用全国
		if(StringUtils.isEmpty(url)){
			queryCityCode = "全国";
			url = getUrlLoadbalanceUrl(queryCityCode);
		}
		
		//取省份信息
		String provinceCode = "全国";
		if(!"电渠".equals(queryCityCode) && !"集团".equals(queryCityCode)){
			queryCityCode = queryCityCode.replace(",", "|");
			provinceCode = queryCityCode.split("\\|")[0];
			provinceCode = provinceCode.substring(0, 2) + "0000";
		}
		
		// 获取高级分析的接口串中的serviceInfo
		String serviceInfo = MyUtil.getServiceInfo(servicetype, "问题生成词模", "",
				false,queryCityCode);
		// 获取高级分析的串
		String queryObject = MyUtil.getDAnalyzeQueryObject("问题生成词模",
				query, servicetype, serviceInfo);
		logger.info("自学习词模高级分析接口的输入串：" + queryObject);
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient(url);
		// 判断接口是否连接是否为null
		if (NLPCaller4WSClient == null) {
			// 返回的词模为空
			return "";
		}
		// 获取接口的返回json串
		String result = NLPCaller4WSClient.kAnalyze(queryObject);
		
		// add by zhao lipeng. 20170210 START
		logger.info("问题库自学习词模调用接口返回：" + result);
		// add by zhao lipeng. 20170210 END
		
		// 替换掉回车符和空格
		result = result.replace("\n", "").replace(" ", "").trim();
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 返回的词模为空
			return "";
		} else {
			try {
				// 将接口的返回串用JSONObject转换为JSONObject对象
				JSONObject obj = JSONObject.parseObject(result);
				// 将json串中的key为autoLearnedPat的value返回
				return obj.getString("autoLearnedPat");
			} catch (Exception e) {
				e.printStackTrace();
				// 转换json格式时出现错误
				return "";
			}
		}
	}
	

	/**
	 * 调用高析接口（使用地市负载）
	 * 
	 * @param servicetype 行业
	 * 
	 * @param query
	 *            问法
	 * @param queryCityCode
	 *            地市编码，
	 * @param userId
	 * @return
	 */
	public static JSONObject callDetailAnalyze(String servicetype,String query, String queryCityCode,String userId) {
		// 定义返回的json串
		JSONObject jsonObj = new JSONObject();
		// 判断servicetype为空串、空、null
		if (" ".equals(servicetype) || "".equals(servicetype)
				|| servicetype == null) {
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("result", "登录信息已失效,请注销后重新登录!");
			return jsonObj;
		}
		if(StringUtils.isEmpty(query)){
			// 将登录信息失效放入jsonObj的msg对象中
			jsonObj.put("result", "咨询不能为空！");
			return jsonObj;
		}
		// 默认使用全国地市
		if (StringUtils.isEmpty(queryCityCode)) {
			queryCityCode = "全国";
		}

		//获取负载均衡url
		String url = getUrlLoadbalanceUrl(queryCityCode);
		//如果未配置对应省份的负载服务器，默认使用全国
		if(StringUtils.isEmpty(url)){
			queryCityCode = "全国";
			url = getUrlLoadbalanceUrl(queryCityCode);
		}
		
		//取省份信息
		String provinceCode = "";
		if("电渠".equals(queryCityCode) || "集团".equals(queryCityCode) || "全国".equals(queryCityCode)){
			provinceCode = queryCityCode;
		}else{
			queryCityCode = queryCityCode.replace(",", "|");
			provinceCode = queryCityCode.split("\\|")[0];
			provinceCode = provinceCode.substring(0, 2) + "0000";
		}
		
		// 获取高级分析的接口串中的serviceInfo
		String serviceInfo = MyUtil.getServiceInfo(servicetype, "高级分析", "",
				false, provinceCode,"否");
		// 获取高级分析接口的入参字符串
		String queryObject = MyUtil.getDAnalyzeQueryObject(userId,query, servicetype, serviceInfo);
		logger.info("生成词模高级分析【"+ GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode)+ "】接口地址：" + url);
		logger.info("生成词模高级分析接口的输入串：" + queryObject);
		
//		url = "http://180.153.51.235:8082/NLPWebService/NLPCallerWS?wsdl";
		// 获取高级分析的客户端
		NLPCaller4WSDelegate NLPCaller4WSClient = getServiceClient
				.NLPCaller4WSClient(url);
		// 判断客户端是否为null
		if (NLPCaller4WSClient == null) {
			// 将无放入jsonObj的result对象中
			// jsonObj.put("result", "无");
			jsonObj.put("result","ERROR:生成词模高级分析【"+ GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode) + "】接口异常。");
			return jsonObj;
		}

		String result = "";
		try {
			// 调用接口的方法获取词模
			// result = NLPCaller4WSClient.kAnalyze(queryObject);
			result = NLPCaller4WSClient.detailAnalyze(queryObject);
			logger.info("生成词模高级分析接口的输出串：" + result);
			// 替换掉返回串中的回车符
			result = result.replace("\n", "");
		} catch (Exception e) {
			e.printStackTrace();
			// 将无放入jsonObj的result对象中
			jsonObj.put("result","ERROR:生成词模高级分析【"+ GetLoadbalancingConfig.cityCodeToCityName.get(provinceCode) + "】接口调用失败。");
			return jsonObj;
		}
		// 判断返回串是否为"接口请求参数不合规范！"、""、null
		if ("接口请求参数不合规范！".equals(result) || "".equals(result) || result == null) {
			// 将无放入jsonObj的result对象中
			jsonObj.put("result", "无");
			return jsonObj;
		}

		jsonObj.put("detailAnalyze", result);
		return jsonObj;
	}
	
	

	/**
	 * 解析高级分析接口分词结果，生成词模
	 * @param result 高析接口返回结果
	 * @param autor 词模编者，例【编者=\"自学习\"】
	 * @return
	 * 	词模集合,属性如下：<br>
	 * 		wordpat：简单词模<br>
	 * 		newWordpat：去除近类的词模，用于页面展示<br>
	 * 		isValid：词模是否有效<br>
	 * 		OOVWord：词模中OOV的分词集合
	 */
	public static List<JSONObject> detailAnalyze2Wordpat(String result,String autor){
		 String rs ="";
		// 定义返回的json串
		List<JSONObject> resultList = new ArrayList<JSONObject>();
		Map<String,String> map = new HashMap<String,String>();
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
					// 判断分词是否含有..( 和 nlp版本信息
					if (!segments.contains("...(") && !segments.startsWith("NLU-Version")) {
						// 根据分词得到分词数
						String wordnum = segments.split("\\)  \\(")[1].replace(
								" words)", "");
						// 得到分词的内容
						String word = segments.split("\\)  \\(")[0] + ")";
						map.put(word, wordnum);
					}
				}
				
				List<JSONObject> list = getAutoWordpat(map,autor);
				resultList.addAll(list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
	/**
	 * 解析高级分析接口分词结果，生成自学习词模
	 * @param result 高析接口返回结果
	 * @param autor 词模编者，例【编者=\"自学习\"】
	 * @return
	 * 	词模集合,属性如下：<br>
	 * 		wordpat：简单词模<br>
	 * 		newWordpat：去除近类的词模，用于页面展示<br>
	 * 		isValid：词模是否有效<br>
	 * 		OOVWord：词模中OOV的分词集合
	 */
	public static List<JSONObject> detailAnalyze4Wordpat(String result,String autor,Set<String> optionWord){
		 String rs ="";
		// 定义返回的json串
		List<JSONObject> resultList = new ArrayList<JSONObject>();
		Map<String,String> map = new HashMap<String,String>();
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
					// 判断分词是否含有..( 和 nlp版本信息
					if (!segments.contains("...(") && !segments.startsWith("NLU-Version")) {
						// 根据分词得到分词数
						String wordnum = segments.split("\\)  \\(")[1].replace(
								" words)", "");
						// 得到分词的内容
						String word = segments.split("\\)  \\(")[0] + ")";
						map.put(word, wordnum);
						break;//取第一组分词
					}
				}
				if(optionWord == null){
					optionWord = Collections.emptySet();
				}
				List<JSONObject> list = getLearnWordpat(map, autor, optionWord);
				resultList.addAll(list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultList;
	}
	
	/**
	 *@description 获得业务下所有问题
	 *@param serviceid
	 *@return
	 *@returnType List<String>
	 */
	public static List<String> getAllQuery(String serviceid,int querytype) {
		List<String> list = new ArrayList<String>();
		Result rs = CommonLibQueryManageDAO.getQuery(serviceid,querytype);
		if (rs != null && rs.getRowCount() > 0) {
			// 循环遍历数据源
			for (int i = 0; i < rs.getRowCount(); i++) {
				String query = rs.getRows()[i].get("query") != null ? rs
						.getRows()[i].get("query").toString() : "";
				if ("".equals(query)) {
					continue;
				}
				String city = rs.getRows()[i].get("city") != null ? rs
						.getRows()[i].get("city").toString() : "";
				String kbdataid = rs.getRows()[i].get("kbdataid").toString();
				String queryid = rs.getRows()[i].get("id").toString();
				String isserviceword = rs.getRows()[i].get("isstrictexclusion") == null ? " ":rs.getRows()[i].get("isstrictexclusion").toString();
				String content = city + "@#@" + query + "@#@" + kbdataid + "@#@" + queryid + "@#@" + isserviceword ;
				list.add(content);
			}
		}
		return list;
	}
	
	/**
	 * 
	 * @param map
	 * @return
	 * @returnType String
	 * @dateTime 2017-9-1下午03:24:06
	 */
	private static List<JSONObject> getAutoWordpat(Map<String, String> map,String autor) {
		List<JSONObject> list = new ArrayList<JSONObject>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			JSONObject jsonObject = new JSONObject();
			JSONArray array = new JSONArray();
			String word = entry.getKey().replace(" ", "##");
			String wordArray[] = word.split("##");
			String wordpat = "";
			int flag = 0;// 词模处理结果 0 可用 1 分词中没有近类和父类 2 分词中包含OOV
			String _word = "";// 具体分词
			for (int i = 0; i < wordArray.length; i++) {
				String tempWord = wordArray[i];
				_word = tempWord.split("\\(")[0];
				if (!"".equals(tempWord) && !"".equals(_word.trim())) {//分词本身不能为空
					String dealWrod = dealWrod(tempWord);
					if (dealWrod == null) {
						flag = 1;
						//页面展示： word(OOV)
						dealWrod = _word+"(OOV)";
						//记录当前词模中的OOV分词
						array.add(_word);
					}
					String _tempWord = "<" + dealWrod + ">";
					wordpat = wordpat + _tempWord + "*";
				}
			}
			wordpat = wordpat.substring(0, wordpat.lastIndexOf("*"))+ "@2#" + "编者=\""+autor+"\"";
			wordpat = SimpleString.worpattosimworpat(wordpat);
			
			String newWordpat = wordpat.replace("近类", "");
			jsonObject.put("wordpat", wordpat);
			jsonObject.put("newWordpat", newWordpat);
			jsonObject.put("isValid", flag == 0);
			jsonObject.put("OOVWord", array);
			list.add(jsonObject);
		}
		return list;
	}
	
	/**
	 * 
	 * @param map
	 * @return
	 * @returnType String
	 * @dateTime 2017-9-1下午03:24:06
	 */
	private static List<JSONObject> getLearnWordpat(Map<String, String> map,String autor,Set<String> optionWord) {
		List<JSONObject> list = new ArrayList<JSONObject>();
		for (Map.Entry<String, String> entry : map.entrySet()) {
			JSONObject jsonObject = new JSONObject();
			JSONArray array = new JSONArray();
			String word = entry.getKey().replace(" ", "##");
			String wordArray[] = word.split("##");
			StringBuilder wordpatBuilder = new StringBuilder("");
			StringBuilder lockWordpat = new StringBuilder("");
			int flag = 0;// 词模处理结果 0 可用 1 分词中没有近类和父类 2 分词中包含OOV
			boolean lastFlag = false;//上一个分词是否是OOV
			int requiredNum = 0;//必选词数量
			String _word = "";// 具体分词
			for (int i = 0; i < wordArray.length; i++) {
				String tempWord = wordArray[i];
				_word = tempWord.split("\\(")[0];
				String _tempWord = "";
				String _lockWord = "";
				
				if (!"".equals(tempWord) && !"".equals(_word.trim())) {//分词本身不能为空
					List<String> dealWrod = dealWrod2List(tempWord);
					if (dealWrod == null || dealWrod.isEmpty()) {
						//页面展示： word(OOV)
						if(i > 0){
							wordpatBuilder.append('-');
							lockWordpat.append('-');
						}
						wordpatBuilder.append(_word);
						lockWordpat.append(_word);
						//记录当前词模中的OOV分词
						array.add(_word);
						lastFlag = true;
						flag = 1;
						requiredNum++;
					}else{
						
						for(String str:dealWrod){
							//可选词
							if(optionWord.contains(str.replaceFirst("!", ""))){
								_tempWord = "[" + StringUtils.join(dealWrod, "|") + "]";
								break;
							}
						}
						//必选
						if(StringUtils.isEmpty(_tempWord)){
							_tempWord = "<" + StringUtils.join(dealWrod, "|") + ">";
							requiredNum ++;
						}
						_lockWord = "<" + StringUtils.join(dealWrod, "|") + ">";
						
						if(i > 0){
							if(lastFlag){
								wordpatBuilder.append('-');
								lockWordpat.append('-');
							}else{
								wordpatBuilder.append('*');
								lockWordpat.append('*');
							}
						}
						wordpatBuilder.append(_tempWord);
						lockWordpat.append(_lockWord);
						lastFlag = false;
					}
				}
			}
			
			String wordpat = wordpatBuilder.append("@2#")
					.append("编者=\"").append(autor).append("\"")
					.append("&最大未匹配字数=\"").append(requiredNum+1).append("\"").toString();
			wordpat = SimpleString.worpattosimworpat(wordpat);
			
			String lockWordpatStr = lockWordpat.append("@2#")
			.append("编者=\"").append(autor).append("\"")
			.append("&最大未匹配字数=\"").append(0).append("\"")
			.append("&置信度=\"").append("1.1").append("\"").toString();
			
			lockWordpatStr = SimpleString.worpattosimworpat(lockWordpatStr);
			
			String newWordpat = wordpat.replace("近类", "");
			jsonObject.put("wordpat", wordpat);
			jsonObject.put("newWordpat", newWordpat);
			jsonObject.put("lockWordpat", lockWordpatStr);
			jsonObject.put("isValid", flag == 0);
			jsonObject.put("OOVWord", array);
			list.add(jsonObject);
		}
		return list;
	}
	
	private static String dealWrod(String word ){
		String _word = word.split("\\(")[0];
		String tempWord = word.split("\\(")[1].split("\\)")[0];
		String wordArray[] = tempWord.split("\\|");
		String newWord="";
		if(tempWord.contains("近类")&&tempWord.contains("父类")){
			for(int i =0;i<wordArray.length;i++){
				String w = wordArray[i];
				if(!w.endsWith("父类")&&!w.equals("模板词")&&!w.endsWith("词类")){
					newWord = newWord+w+"|";
				}
			}
		}else{
			for(int i =0;i<wordArray.length;i++){
				String w = wordArray[i];
				if(!w.equals("模板词")&&!w.endsWith("词类")){
					newWord = newWord+w+"|";
				}
			}
		}
		if(tempWord.equals("OOV")){//如果分词中存在OOV直接过滤
			return null;
		}
		if(newWord.contains("|")){
			newWord = newWord.substring(0,newWord.lastIndexOf("|"));
			return newWord;
		}
		return null;
	}
	
	private static List<String> dealWrod2List(String word ){
		List<String> rs = new ArrayList<String>();
		String _word = word.split("\\(")[0];
		String tempWord = word.split("\\(")[1].split("\\)")[0];
		String wordArray[] = tempWord.split("\\|");
		if(tempWord.contains("近类")&&tempWord.contains("父类")){
			for(int i =0;i<wordArray.length;i++){
				String w = wordArray[i];
				if(!w.endsWith("父类")&&!w.equals("模板词")&&!w.endsWith("词类")){
					rs.add(w);
				}
			}
		}else{
			for(int i =0;i<wordArray.length;i++){
				String w = wordArray[i];
				if(!w.equals("模板词")&&!w.endsWith("词类")){
					rs.add(w);
				}
			}
		}
		if(tempWord.equals("OOV")){//如果分词中存在OOV直接过滤
			return null;
		}
		return rs;
	}
	
	
	/**
	 * 获取自学习词模及对应的客户问
	 * @param kbIdList
	 * @return
	 */
	private static Map<String,String> getAutoWordpatMap(List<String> kbIdList,String wordpattype){
		Result rs = CommonLibQueryManageDAO.selectWordpatByKbdataid(kbIdList,wordpattype);
		Map<String,String> wordpatMap = new HashMap<String,String>();
		if(rs != null && rs.getRowCount()> 0){
			for(int i =0;i<rs.getRowCount();i++){
				if(rs.getRows()[i].get("wordpat") != null){
					//自学习词模：<翼支付|!翼支付近类>*<是什>*[<么|!没有近类>]@2#编者="问题库"&来源="翼支付是什么？"&最大未匹配字数="1"
					String wordpat = rs.getRows()[i].get("wordpat").toString();
					String[] split = wordpat.split("@2#");
					if(split.length > 1){
						for(String str:split[1].split("&")){
							if(str.startsWith("来源=")){//自动生成的词模，返回值是来源
								String query = str.substring(4, str.length()-1);
								wordpatMap.put(query, split[0]);
								break;
							}
						}
					}
				}
			}
		}
		return wordpatMap;
	}
	/**
	 * 获取自学习词模及对应的客户问
	 * @param kbIdList
	 * @return
	 */
	private static Map<String,Set<String>> getAutoWordpatMap2Set(List<String> kbIdList,String wordpattype){
		Result rs = CommonLibQueryManageDAO.selectWordpatByKbdataid(kbIdList,wordpattype);
		Map<String,Set<String>> wordpatMap = new HashMap<String,Set<String>>();
		if(rs != null && rs.getRowCount()> 0){
			for(int i =0;i<rs.getRowCount();i++){
				if(rs.getRows()[i].get("wordpat") != null){
					//自学习词模：<翼支付|!翼支付近类>*<是什>*[<么|!没有近类>]@2#编者="问题库"&来源="翼支付是什么？"&最大未匹配字数="1"
					String wordpat = rs.getRows()[i].get("wordpat").toString();
					String[] split = wordpat.split("@2#");
					if(split.length > 1){
						for(String str:split[1].split("&")){
							if(str.startsWith("来源=")){//自动生成的词模，返回值是来源
								String query = str.substring(4, str.length()-1);
								Set<String> set = wordpatMap.get(query);
								if(set == null){
									set = new HashSet<String>();
									wordpatMap.put(query, set);
								}
								set.add(split[0]);
								break;
							}
						}
					}
				}
			}
		}
		return wordpatMap;
	}
	
	public static String getUrlLoadbalanceUrl(String queryCityCode){
		String url = "";
		String provinceCode = "全国";
		Map<String, Map<String, String>> provinceToUrl = GetLoadbalancingConfig.provinceToUrl;
		if ("全国".equals(queryCityCode) || "电渠".equals(queryCityCode)
				|| "集团".equals(queryCityCode) || "".equals(queryCityCode)
				|| queryCityCode == null) {
			url = provinceToUrl.get("默认").get("高级分析");
			logger.info("获取负载地址:【默认】"+url);
			// url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
		} else {
			queryCityCode = queryCityCode.replace(",", "|");
			provinceCode = queryCityCode.split("\\|")[0];
			provinceCode = provinceCode.substring(0, 2) + "0000";
			if ("010000".equals(provinceCode) || "000000".equals(provinceCode)) {// 如何为集团、电渠编码
																					// 去默认url
			// url = GetLoadbalancingConfig.getDetailAnalyzeUrlByProvince("默认");
				url = provinceToUrl.get("默认").get("高级分析");
				logger.info("获取负载地址:【默认】"+url);
			} else {
				String province = GetLoadbalancingConfig.cityCodeToCityName
						.get(provinceCode);
				if (provinceToUrl.containsKey(province)) {
					// url =
					// GetLoadbalancingConfig.getDetailAnalyzeUrlByProvinceCode(province);
					url = provinceToUrl.get(province).get("高级分析");
					logger.info("获取负载地址:【"+province+"】"+url);
				} else {
					return null;
				}
			}
		}
		return url;
	}
	
	public static void main(String[] args) {
//		String worpattosimworpat = SimpleString.worpattosimworpat("测试近类@2#编者=\"你好\"");
//		worpattosimworpat = SimpleString.worpattosimworpat("测试近类@2# ");
//		System.out.println(worpattosimworpat);
		
		Map<String,String> map = new HashMap<String,String>();
//		String segments = "我(!本人近类|!本行近类|!第一人称近类|!集团无效咨询单字父类|!冗余词词类|!我近类|模板词) 想(!集团无效咨询单字父类|!考虑近类|!梦想近类|!想近类|!想念近类|模板词) 去(!到近类|!火车到近类|!集团无效咨询单字父类|!去近类|!往近类|!走近类|模板词) 北京(!北京近类|!航班城市父类|!嘉实银行父类|!原子词词类|!中国城市父类|!中国行政城市父类|模板词) 天安(!原子词词类|模板词) 门(OOV) 看看(!电信业务父类|!看近类|模板词)  (7 words)";
		String segments = "我(!本人近类|!本行近类|!第一人称近类|!集团无效咨询单字父类|!冗余词词类|!我近类|模板词)  想(!集团无效咨询单字父类|!考虑近类|!梦想近类|!想近类|!想念近类|模板词)  去(!到近类|!火车到近类|!集团无效咨询单字父类|!去近类|!往近类|!走近类|模板词)  北京(!北京近类|!航班城市父类|!嘉实银行父类|!原子词词类|!中国城市父类|!中国行政城市父类|模板词)  天安(!原子词词类|模板词)  门(!集团无效咨询单字父类|!门近类|!商圈名父类|模板词)  看看(!电信业务父类|!看近类|模板词)  (7 words)";
		// 根据分词得到分词数
		String wordnum = segments.split("\\)  \\(")[1].replace(
				" words)", "");
		// 得到分词的内容
		String word = segments.split("\\)  \\(")[0] + ")";
		map.put(word, wordnum);
		
		Set<String> optionWord = new HashSet<String>();
		optionWord.add("想近类");
		optionWord.add("本人近类");
		
		List<JSONObject> wordpat = getLearnWordpat(map, "问题库", optionWord);
		System.out.println(wordpat);

		wordpat = getAutoWordpat(map, "问题库");
		System.out.println(wordpat);
		
		InsertOrUpdateParam param = new InsertOrUpdateParam();
		param.simplewordpat = "[本人近类|本行近类|第一人称近类|我近类]*[考虑近类|梦想近类|想近类|想念近类]*到近类|火车到近类|去近类|往近类|走近类*北京近类-天安-门-看近类#无序#编者=\"问题库\"";
		System.out.println(param.simplewordpat);
		System.out.println(SimpleString.SimpleWordPatToWordPat(param));
		
		param.simplewordpat = "[本人近类|本行近类|第一人称近类|我近类]*[考虑近类|梦想近类|想近类|想念近类]*到近类|火车到近类|去近类|往近类|走近类*北京近类-天安-看近类#无序#编者=\"问题库\"";
		System.out.println(param.simplewordpat);
		System.out.println(SimpleString.SimpleWordPatToWordPat(param));
	}
}
